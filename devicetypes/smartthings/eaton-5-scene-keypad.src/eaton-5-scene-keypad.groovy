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
	definition(name: "Eaton 5-Scene Keypad", namespace: "smartthings", author: "SmartThings", mcdSync: true, mnmn: "SmartThings", vid: "SmartThings-smartthings-Eaton_5-Scene_Keypad") {
		capability "Actuator"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		//zw:L type:0202 mfr:001A prod:574D model:0000 ver:2.05 zwv:2.78 lib:01 cc:87,77,86,22,2D,85,72,21,70
		fingerprint mfr: "001A", prod: "574D", model: "0000", deviceJoinName: "Eaton Switch" //Eaton 5-Scene Keypad
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

		childDeviceTiles("outlets")

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 6, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "switch"
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
	def indicator = 0
	for (group in 1..5) {
		cmds << zwave.indicatorV1.indicatorSet(value: indicator)
		cmds << zwave.associationV1.associationSet(groupingIdentifier: group, nodeId: [zwaveHubNodeId])
		cmds << zwave.sceneControllerConfV1.sceneControllerConfSet(dimmingDuration: 0, groupId: group, sceneId: group)
		indicator += 2 ** (5 - group)
	}
	cmds << zwave.indicatorV1.indicatorSet(value: indicator)
	cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()

	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	sendEvent(name: "switch", value: "off")

	runIn(52, "initialize", [overwrite: true])
	// Wait for set up to finish and process before proceeding to initialization

	sendHubCommand cmds, 3000
}

def updated() {
	// If not set update ManufacturerSpecific data
	if (!getDataValue("manufacturer")) {
		runIn(52, "initialize", [overwrite: true])  // installation may still be running
	} else {
		// If controller ignored some of associationSet and sceneControllerConfSet commands
		// and failsafe integrated into initialize() did not manage to fix it,
		// user can enter SmartThings Classic's device settings and press save
		// until controller starts to respond correctly
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
	// Check if Hub is associated to groups responsible for all five switches
	// We do this, because most likely some of associationSet and sceneControllerConfSet commands were ignored
	// As this process will take a while, we use controller's LED indicators to display progress.
	// Number of retries was chosen to achieve high enough success rate
	for (retries in 1..2) {
		int indicator = 0
		for (group in 1..5) {
			cmds << zwave.indicatorV1.indicatorSet(value: indicator)
			cmds << zwave.associationV1.associationGet(groupingIdentifier: group)
			cmds << zwave.sceneControllerConfV1.sceneControllerConfGet(groupId: group)
			indicator += (2 ** (5 - group))
		}
		cmds << zwave.indicatorV1.indicatorSet(value: indicator)
	}

	if (!getDataValue("manufacturer")) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}

	cmds << zwave.indicatorV1.indicatorSet(value: 0)
	// Make sure cloud is in sync with device
	cmds << zwave.indicatorV1.indicatorGet()

	// Long interval to make it possible to process association set commands if necessary
	sendHubCommand cmds, 3100
}

def on() {
	def switchId = 1
	def state = "on"
	// this may override previous state if user changes more switches before cloud state is updated
	updateLocalSwitchState(switchId, state)
}

def off() {
	def switchId = 1
	def state = "off"
	// this may override previous state if user changes more switches before cloud state is updated
	updateLocalSwitchState(switchId, state)
}

def refresh() {
	// Indicator returns number which is a bit representation of current state of all 5 switches
	response zwave.indicatorV1.indicatorGet()
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
	log.debug "Parse [$description] to \"$cmd\""
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId  : $cmd.manufacturerId"
	log.debug "manufacturerName: $cmd.manufacturerName"
	log.debug "productId       : $cmd.productId"
	log.debug "productTypeId   : $cmd.productTypeId"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	def event = [:]
	if (cmd.nodeId.any { it == zwaveHubNodeId }) {
		event = createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
	} else {
		// We're not associated properly to this group, try setting association two times
		def cmds = []
		// Set Association for this group
		cmds << zwave.associationV1.associationSet(groupingIdentifier: cmd.groupingIdentifier, nodeId: [zwaveHubNodeId])
		cmds << zwave.associationV1.associationSet(groupingIdentifier: cmd.groupingIdentifier, nodeId: [zwaveHubNodeId])
		sendHubCommand cmds, 1500
	}
	event
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	def resp = null
	if (cmd.value == 0) {
		// Device sends this command when any switch is turned off
		// Most reliable way to know which switches are still "on" is to check their status
		resp = refresh()
		// Indicator returns number which is a bit representation of current state of switch
	}
	resp
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	// Dimming duration is not supported
	setSwitchState(cmd.sceneId, "on")
}

def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
	def events = []
	// cmd.value (0-31) is a binary representation of current switch state
	// switch 1 - first bit
	events << setSwitchState(1, (cmd.value & 1) ? "on" : "off")
	// switch 2 - second bit
	events << setSwitchState(2, (cmd.value & 2) ? "on" : "off")
	// switch 3 - third bit
	events << setSwitchState(3, (cmd.value & 4) ? "on" : "off")
	// switch 4 - fourth bit
	events << setSwitchState(4, (cmd.value & 8) ? "on" : "off")
	// switch 5 - fifth bit
	events << setSwitchState(5, (cmd.value & 16) ? "on" : "off")
	events
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
	// Not supported
	// We have no way to set and/or retrieve multilevel state of each button
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStopLevelChange cmd) {
	// Not supported
	// We have no way to set and/or retrieve multilevel state of each switch
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
	// we have no way of knowing which command was ignored
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.scenecontrollerconfv1.SceneControllerConfReport cmd) {
	if (cmd.groupId != cmd.sceneId) {
		// Scene not set up properly for this association group. Try setting it two more times.
		def cmds = []
		cmds << zwave.sceneControllerConfV1.sceneControllerConfSet(dimmingDuration: 0, groupId: cmd.groupId, sceneId: cmd.groupId)
		cmds << zwave.sceneControllerConfV1.sceneControllerConfSet(dimmingDuration: 0, groupId: cmd.groupId, sceneId: cmd.groupId)
		sendHubCommand cmds, 1500
	}
	return null
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unexpected zwave command $cmd"
	return null
}

// called from child-switch's on() method
void childOn(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1] as Integer
	def state = "on"
	// this may override previous state if user changes more switches before cloud state is updated
	updateLocalSwitchState(switchId, state)
}

// called from child-switch's off() method
void childOff(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1] as Integer
	def state = "off"
	// this may override previous state if user changes more switches before cloud state is updated
	updateLocalSwitchState(switchId, state)
}

// handle switch state changes received from the device
private setSwitchState(switchId, state) {
	def event
	if (switchId == 1) {
		// switch 1 is represented by parent DTH
		event = createEvent(name: "switch", value: "$state", descriptionText: "Switch $switchId was switched $state")
	} else {
		String childDni = "${device.deviceNetworkId}/$switchId"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		if (!child) {
			log.error "Child device $childDni not found"
		}
		if (state != child?.currentState("switch")?.value) {
			// update child switch state
			child?.sendEvent(name: "switch", value: "$state")
			// this will allow SmartThings classic user to view status changes from parent's "Recently" tab
			event = createEvent(descriptionText: "Switch $switchId was switched $state", isStateChange: true)
		}
	}
	event
}

// it is not possible to set state of 1 switch, so we need to update all of them
// this may override previous state if user changes more switches before cloud state is updated
private updateLocalSwitchState(childId, state) {
	def binarySwitchState = 0

	// first apply state of switch represented by childId
	if (state == "on") {
		binarySwitchState += 2 ** (childId - 1)
	}

	// switch 1 is represented by parent DTH
	if (childId != 1 && device?.currentState("switch")?.value == "on") {
		++binarySwitchState
	}
	for (i in 2..5) {
		// childId state is already represented in binarySwitchState
		if (i != childId) {
			String childDni = "${device.deviceNetworkId}/$i"
			def child = childDevices.find { it.deviceNetworkId == childDni }
			if (child?.device?.currentState("switch")?.value == "on") {
				binarySwitchState += 2 ** (i - 1)
			}
		}
	}

	def commands = []
	commands << zwave.indicatorV1.indicatorSet(value: binarySwitchState)
	commands << zwave.indicatorV1.indicatorGet()
	sendHubCommand commands, 100
}

private addChildSwitches() {
	for (i in 2..5) {
		String childDni = "${device.deviceNetworkId}/$i"
		def child = addChildDevice("Child Switch",
				childDni,
				device.hubId,
				[completedSetup: true,
				 label         : "$device.displayName Switch $i",
				 isComponent   : true,
				 componentName : "switch$i",
				 componentLabel: "Switch $i"
				])
		child.sendEvent(name: "switch", value: "off")
	}
}
