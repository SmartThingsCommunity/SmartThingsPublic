import groovy.json.JsonOutput
/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Aeon Key Fob", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Actuator"
		capability "Button"
		capability "Holdable Button"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		fingerprint deviceId: "0x0101", inClusters: "0x86,0x72,0x70,0x80,0x84,0x85"
		fingerprint mfr: "0086", prod: "0101", model: "0058", deviceJoinName: "Aeotec Key Fob"
		fingerprint mfr: "0086", prod: "0001", model: "0026", deviceJoinName: "Aeotec Panic Button"
	}

	simulator {
		status "button 1 pushed":  "command: 2001, payload: 01"
		status "button 1 held":  "command: 2001, payload: 15"
		status "button 2 pushed":  "command: 2001, payload: 29"
		status "button 2 held":  "command: 2001, payload: 3D"
		status "button 3 pushed":  "command: 2001, payload: 51"
		status "button 3 held":  "command: 2001, payload: 65"
		status "button 4 pushed":  "command: 2001, payload: 79"
		status "button 4 held":  "command: 2001, payload: 8D"
		status "wakeup":  "command: 8407, payload: "
	}
	tiles {

		multiAttributeTile(name: "rich-control", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "default", label: ' ', action: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			}
		}
		standardTile("battery", "device.battery", inactiveLabel: false, width: 6, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		childDeviceTiles("outlets")
	}

}


def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
		results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [0x2B: 1, 0x80: 1, 0x84: 1])
		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: false ]
	}
	// log.debug("Parsed '$description' to $results")
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]

	def prevBattery = device.currentState("battery")
	if (!prevBattery || (new Date().time - prevBattery.date.time)/60000 >= 60 * 53) {
		results << response(zwave.batteryV1.batteryGet().format())
	}
	results += configurationCmds().collect{ response(it) }
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	return results
}

def buttonEvent(button, held) {
	button = button as Integer
	def child
	Integer buttons

	if (device.currentState("numberOfButtons")) {
		buttons = (device.currentState("numberOfButtons").value).toBigInteger()
	}
	else {
		def zwMap = getZwaveInfo()
		buttons = 4 // Default for Key Fob

		// Only one button for Aeon Panic Button
		if (zwMap && zwMap.mfr == "0086" && zwMap.prod == "0001" && zwMap.model == "0026") {
			buttons = 1
		}
		sendEvent(name: "numberOfButtons", value: buttons)
	}

	if(buttons > 1)
	{
		String childDni = "${device.deviceNetworkId}/${button}"
		child = childDevices.find{it.deviceNetworkId == childDni}
		if (!child) {
			log.error "Child device $childDni not found"
		}
	}

	if (held) {
		if(buttons > 1) {
			child?.sendEvent(name: "button", value: "held", data: [buttonNumber: 1], descriptionText: "$child.displayName was held", isStateChange: true)
		}
		createEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
	} else {
		if(buttons > 1) {
			child?.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$child.displayName was pushed", isStateChange: true)
		}
		createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	Integer button = ((cmd.sceneId + 1) / 2) as Integer
	Boolean held = !(cmd.sceneId % 2)
	buttonEvent(button, held)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[ descriptionText: "$device.displayName: $cmd", linkText:device.displayName, displayed: false ]
}

def configurationCmds() {
	[ zwave.configurationV1.configurationSet(parameterNumber: 250, scaledConfigurationValue: 1).format(),
	  zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format() ]
}

def configure() {
	def cmd = configurationCmds()
	log.debug("Sending configuration: $cmd")
	return cmd
}


def installed() {
	initialize()
	Integer buttons = (device.currentState("numberOfButtons").value).toBigInteger()
	if(buttons > 1) {
		createChildDevices()
	}
}

def updated() {
	initialize()
	Integer buttons = (device.currentState("numberOfButtons").value).toBigInteger()
	if(buttons > 1)
	{
		if (!childDevices) {
			createChildDevices()
		}
		else if (device.label != state.oldLabel) {
			childDevices.each {
				def segs = it.deviceNetworkId.split("/")
				def newLabel = "${device.displayName} button ${segs[-1]}"
				it.setLabel(newLabel)
			}
			state.oldLabel = device.label
		}
	}
}

def initialize() {

	def results = []

	if (zwaveInfo && zwaveInfo.mfr == "0086" && zwaveInfo.prod == "0001" && zwaveInfo.model == "0026") {
		sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
		buttons = 1 // Only one button for Aeon Panic Button
		results << response(zwave.batteryV1.batteryGet().format())
	} else {
		// Device only goes OFFLINE when Hub is off
		sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zwave", scheme:"untracked"]), displayed: false)
		def buttons = 4 // Default for Key Fob
	}
	
	sendEvent(name: "numberOfButtons", value: buttons)
	results
}

private void createChildDevices() {
	state.oldLabel = device.label
	Integer buttons = (device.currentState("numberOfButtons").value).toBigInteger()
	for (i in 1..buttons) {
		addChildDevice("Child Button", "${device.deviceNetworkId}/${i}", null,
				[completedSetup: true, label: "${device.displayName} button ${i}",
				 isComponent: true, componentName: "button$i", componentLabel: "Button $i"])
	}
}
