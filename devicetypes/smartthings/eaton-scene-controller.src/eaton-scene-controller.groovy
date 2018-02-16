/**
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Eaton 5-Scene Keypad", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		//zw:L type:0202 mfr:001A prod:574D model:0000 ver:2.05 zwv:2.78 lib:01 cc:87,77,86,22,2D,85,72,21,70
		fingerprint mfr: "001A", prod: "574D", model: "0000", deviceJoinName: "Eaton 5-Scene Keypad"
	}

	tiles(scale: 2) {
		standardTile("state", "device.state", width: 4, height: 2) {
			state "connected", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		childDeviceTiles("outlets")

		main "state"
	}
}

def installed() {
	log.debug "Installed $device.displayName"
	addChildSwitches()
	def cmds = []
	//Associate hub to groups 1-5, and set a scene for each group
	//Device will sometimes respond with ApplicationBusy with STATUS_TRY_AGAIN_IN_WAIT_TIME_SECONDS
	//this can happen for any of associationSet and sceneControllerConfSet commands even with intervals over 6000ms
	//As this process will take a while, we use controller's LED indicators to display progress.
	int indicator = 0;
	for (int group = 1; group < 6; ++group) {
		cmds << zwave.indicatorV1.indicatorSet(value: indicator)
		cmds << zwave.associationV1.associationSet(groupingIdentifier: group, nodeId: [zwaveHubNodeId])
		cmds << zwave.sceneControllerConfV1.sceneControllerConfSet(dimmingDuration: 0, groupId: group, sceneId: group)
		indicator += 2**(5 - group)
	}
	cmds << zwave.indicatorV1.indicatorSet(value: indicator)
	cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	//use runIn to schedule the initialize method in case updated method below is also sending commands to the device
	sendHubCommand cmds*.format(), 3100
	runIn(50, "initialize", [overwrite: true])  // Allow set up to finish and acknowledged before proceeding

	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	refresh()
}

def updated() {
	// If not set update ManufacturerSpecific data
	if (!getDataValue("manufacturer")) {
		runIn(48, "initialize", [overwrite: true])  // installation may still be running
	} else {
		//If controller ignored some of associationSet and sceneControllerConfSet commands
		//and failsafe integrated into initialize() did not manage to fix it,
		//user can enter device settings and press save until controller starts to respond
		//correctly
		initialize()
	}
	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 1 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def initialize() {
	if (!childDevices) {
		addChildSwitches()
	}
	def cmds = []
	//Check if Hub is associated to groups responsible for all five switches
	//We do this, because most likely some of associationSet and sceneControllerConfSet commands were ignored
	//As this process will take a while, we use controller's LED indicators to display progress.
	//Number of retries was chosen to achieve high enough success rate
	for (int retries = 2; retries > 0; --retries) {
		int indicator = 0;
		for (int group = 1; group < 6; ++group) {
			cmds << zwave.indicatorV1.indicatorSet(value: indicator)
			cmds << zwave.associationV1.associationGet(groupingIdentifier: group)
			cmds << zwave.sceneControllerConfV1.sceneControllerConfGet(groupId: group)
			indicator = indicator + (2**(5 - group))
		}
		cmds << zwave.indicatorV1.indicatorSet(value: indicator)
	}

	if (!getDataValue("manufacturer")) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}

	cmds << zwave.indicatorV1.indicatorSet(value: 0)
	//Make sure cloud is in sync with device
	cmds << zwave.indicatorV1.indicatorGet()

	//Long interval to make it possible to process association set commands if necessary
	sendHubCommand cmds*.format(), 3000
}

def refresh() {
	response zwave.indicatorV1.indicatorGet().format()
	//Indicator returns number which is a bit representation of current state of switches
}

def poll() {
	refresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description)
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   $cmd.manufacturerId"
	log.debug "manufacturerName: $cmd.manufacturerName"
	log.debug "productId:        $cmd.productId"
	log.debug "productTypeId:    $cmd.productTypeId"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	if (cmd.nodeId.any { it == zwaveHubNodeId }) {
		createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
	} else {
		// We're not associated properly to this group, try setting association two times
		def cmds = []
		//Set Association for this group
		cmds << zwave.associationV1.associationSet(groupingIdentifier: cmd.groupingIdentifier, nodeId: [zwaveHubNodeId])
		cmds << zwave.associationV1.associationSet(groupingIdentifier: cmd.groupingIdentifier, nodeId: [zwaveHubNodeId])
		sendHubCommand cmds*.format(), 1500
		return null
	}
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	if (cmd.value == 0) {
		//Device sends this event when any switch is turned off
		//Most reliable way to know which switches are still "on" is to check their status
		def cmds = []
		cmds << zwave.indicatorV1.indicatorGet().format()
		//Indicator returns number which is a bit representation of current state of switches
		sendHubCommand cmds
	} else {
		return null
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	//we do not support dimming duration
	setSwitchState(cmd.sceneId, "on")
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
	//cmd.value (0-31) is a binary representation of current switch state
	//switch 1 - first bit
	setSwitchState(1, (cmd.value & 1) ? "on" : "off")
	//switch 2 - second bit
	setSwitchState(2, (cmd.value & 2) ? "on" : "off")
	//switch 3 - third bit
	setSwitchState(3, (cmd.value & 4) ? "on" : "off")
	//switch 4 - fourth bit
	setSwitchState(4, (cmd.value & 8) ? "on" : "off")
	//switch 5 - fifth bit
	setSwitchState(5, (cmd.value & 16) ? "on" : "off")
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
	//Not supported
	//We have no way to set and/or retrieve multilevel state of each button
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
	//Not supported
	//We have no way to set and/or retrieve multilevel state of each switch
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	//we have no way of knowing which command was ignored
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.scenecontrollerconfv1.SceneControllerConfReport cmd) {
	if (cmd.groupId != cmd.sceneId) {
		//Scene not set up properly for this association group. Try setting it two more times.
		def cmds = []
		cmds << zwave.sceneControllerConfV1.sceneControllerConfSet(dimmingDuration: 0, groupId: cmd.groupId, sceneId: cmd.groupId)
		cmds << zwave.sceneControllerConfV1.sceneControllerConfSet(dimmingDuration: 0, groupId: cmd.groupId, sceneId: cmd.groupId)
		sendHubCommand cmds*.format(), 1500
	}
	return null
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unexpected zwave command $cmd"
	return null
}

//method created to make child class reusable
void childOn(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1]
	//this may override other local switch states if cloud is out of sync
	updateLocalSwitchState()
}

//method created to make child class reusable
void childOff(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1]
	//this may override other local switch states if cloud is out of sync
	updateLocalSwitchState()
}

private setSwitchState(switchId, state) {
	String childDni = "${device.deviceNetworkId}/$switchId"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	if (!child) {
		log.error "Child device $childDni not found"
	}
	//send event only if state changed
	if (child?.device?.currentState("switch")?.value != state) {
		child?.sendEvent(name: "switch", value: "$state", descriptionText: "$child.displayName was switched $state")
	}
}

private updateLocalSwitchState() {
	def binarySwitchState = 0;
	def multiplier = 1;
	for (int i = 1; i <= 5; ++i) {
		String childDni = "${device.deviceNetworkId}/$i"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		if (child?.device?.currentState("switch")?.value == "on") {
			binarySwitchState += multiplier
		}
		multiplier *= 2
	}
	sendHubCommand zwave.indicatorV1.indicatorSet(value: binarySwitchState).format()
}

private addChildSwitches() {
	for (i in 1..5) {
		String childDni = "${device.deviceNetworkId}/$i"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		addChildDevice("Child Switch", childDni, null,
			[completedSetup: true, label: "$device.displayName switch $i",
			 isComponent   : true, componentName: "switch$i", componentLabel: "Switch $i"])
	}
}
