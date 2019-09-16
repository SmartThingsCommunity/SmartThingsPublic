import groovy.json.JsonOutput

/**
 *  Z-Wave Multi Button
 *
 *  Copyright 2019 SmartThings
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
	definition (name: "Z-Wave Multi Button", namespace: "smartthings", author: "SmartThings", mcdSync: true) {
		capability "Button"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

		fingerprint mfr: "010F", prod: "1001", model: "1000", deviceJoinName: "Fibaro KeyFob", mnmn: "SmartThings", vid: "generic-6-button" //EU
		fingerprint mfr: "010F", prod: "1001", model: "2000", deviceJoinName: "Fibaro KeyFob", mnmn: "SmartThings", vid: "generic-6-button"  //US
		fingerprint mfr: "0371", prod: "0102", model: "0003", deviceJoinName: "Aeotec NanoMote Quad", mnmn: "SmartThings", vid: "generic-4-button" //US
		fingerprint mfr: "0371", prod: "0002", model: "0003", deviceJoinName: "Aeotec NanoMote Quad", mnmn: "SmartThings", vid: "generic-4-button" //EU
		fingerprint mfr: "0086", prod: "0101", model: "0058", deviceJoinName: "Aeotec KeyFob", mnmn: "SmartThings", vid: "generic-4-button" //US
		fingerprint mfr: "0086", prod: "0001", model: "0058", deviceJoinName: "Aeotec KeyFob", mnmn: "SmartThings", vid: "generic-4-button" //EU
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "button", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "default", label: ' ', icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "button"
		details(["button", "battery"])
	}
}

def installed() {
	sendEvent(name: "button", value: "pushed", isStateChange: true, displayed: false)
	sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
	initialize()
}

def updated() {
	runIn(2, "initialize", [overwrite: true])
}


def initialize() {
	def numberOfButtons = prodNumberOfButtons[zwaveInfo.prod]
	sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
	if(isUntrackedAeotec()) {
		sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zwave", scheme:"untracked"]), displayed: false)
	} else {
		sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 10 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
	if(!childDevices) {
		addChildButtons(numberOfButtons)
	}
	if(childDevices) {
		def event
		for(def endpoint : 1..prodNumberOfButtons[zwaveInfo.prod]) {
			event = createEvent(name: "button", value: "pushed", isStateChange: true)
			sendEventToChild(endpoint, event)
		}
	}
	response([
			secure(zwave.batteryV1.batteryGet()),
			"delay 2000",
			secure(zwave.wakeUpV1.wakeUpNoMoreInformation())
	])
}

def configure() {
	def cmds = []
	if(isAeotecKeyFob()) {
		cmds += secure(zwave.configurationV1.configurationSet(parameterNumber: 250, scaledConfigurationValue: 1))
		//makes Aeotec KeyFob communicate with primary controller
	}
	if(isFibaro()) {
		for (def parameter : 21..26) {
			cmds += secure(zwave.configurationV1.configurationSet(parameterNumber: parameter, scaledConfigurationValue: 15))
			//Makes Fibaro KeyFob buttons send all kind of supported events
		}
	}
	cmds
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description, [0x84: 1])
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned: ${result}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x84: 1])
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	def value = eventsMap[(int) cmd.keyAttributes]
	def description = "Button no. ${cmd.sceneNumber} was ${value}"
	def event = createEvent(name: "button", value: value, descriptionText: description, data: [buttonNumber: cmd.sceneNumber], isStateChange: true)
	sendEventToChild(cmd.sceneNumber, event)
	return createEvent(descriptionText: description)
}

def sendEventToChild(buttonNumber, event) {
	String childDni = "${device.deviceNetworkId}:$buttonNumber"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	child?.sendEvent(event)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = []
	results += createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	results += response([
			secure(zwave.batteryV1.batteryGet()),
			"delay 2000",
			secure(zwave.wakeUpV1.wakeUpNoMoreInformation())
	])
	results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%", isStateChange: true ]
	state.lastbatt = now()
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
}

private secure(cmd) {
	if(zwaveInfo.zw.endsWith("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private addChildButtons(numberOfButtons) {
	for(def endpoint : 1..numberOfButtons) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = (device.displayName.endsWith(' 1') ? device.displayName[0..-2] : (device.displayName + " ")) + "${endpoint}"
			def child = addChildDevice("Child Button", childDni, device.getHub().getId(), [
					completedSetup: true,
					label         : componentLabel,
					isComponent   : true,
					componentName : "button$endpoint",
					componentLabel: "Button $endpoint"
			])
			child.sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
		} catch(Exception e) {
			log.debug "Exception: ${e}"
		}
	}
}

private getEventsMap() {[
		0: "pushed",
		1: "held",
		//2: "down_hold",
		3: "double",
		//4: "pushed_3x"
]}

private getProdNumberOfButtons() {[
		"1001" : 6,
		"0102" : 4,
		"0002" : 4
]}

private getSupportedButtonValues() {
	def values = ["pushed", "held"]
	if (isFibaro()) values << "double"
	return values
}

private isFibaro() {
	zwaveInfo.mfr?.contains("010F")
}

private isUntrackedAeotec() {
	zwaveInfo.mfr?.contains("0371") && zwaveInfo.model?.contains("0003")
}

private isAeotecKeyFob() {
	zwaveInfo.mfr?.contains("0086")
}