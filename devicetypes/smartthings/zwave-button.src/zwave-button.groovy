/**
 *  Z-Wave Button
 *
 *  Copyright 2018 SmartThings
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
import groovy.json.JsonOutput

metadata {
	definition (name: "Z-Wave Button", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.remotecontroller", mmnm: "SmartThings", vid: "generic-button-4") {
		capability "Button"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

		fingerprint mfr: "010F", prod: "0F01", model: "1000", deviceJoinName: "Fibaro Button"
		fingerprint mfr: "010F", prod: "0F01", model: "2000", deviceJoinName: "Fibaro Button"
		fingerprint mfr: "010F", prod: "0F01", model: "3000", deviceJoinName: "Fibaro Button"
		fingerprint mfr: "0371", prod: "0102", model: "0004", deviceJoinName: "Aeotec NanoMote One" //US
		fingerprint mfr: "0371", prod: "0002", model: "0004", deviceJoinName: "Aeotec NanoMote One" //EU
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
	if (isAeotec()) {
		sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zwave", scheme:"untracked"]), displayed: false)
	} else {
		sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
	sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)
	sendEvent(name: "numberOfButtons", value: 1, displayed: false)
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
	response([
			secure(zwave.batteryV1.batteryGet()),
			"delay 2000",
			secure(zwave.wakeUpV1.wakeUpNoMoreInformation())
	])
}

def configure() {
	if (zwaveInfo.mfr?.contains("0086"))
		[
			secure(zwave.configurationV1.configurationSet(parameterNumber: 250, scaledConfigurationValue: 1)),	//makes Aeotec Panic Button communicate with primary controller
		]
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description, commandClasses)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned: ${result}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClasses)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	def value = eventsMap[(int) cmd.keyAttributes]
	createEvent(name: "button", value: value, descriptionText: "Button was ${value}", data: [buttonNumber: 1], isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	def value = cmd.sceneId % 2 ? "pushed" : "held"
	createEvent(name: "button", value: value, descriptionText: "Button was ${value}", data: [buttonNumber: 1], isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = []
	results += createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	if (!state.lastbatt || (now() - state.lastbatt) >= 56*60*60*1000) {
		results += response([
				secure(zwave.batteryV1.batteryGet()),
				"delay 2000",
				secure(zwave.wakeUpV1.wakeUpNoMoreInformation())
		])
	} else {
		results += response(secure(zwave.wakeUpV1.wakeUpNoMoreInformation()))
	}
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

private getEventsMap() {[
		0: "pushed",
		1: "held",
		2: "down_hold",
		3: "double",
		4: "pushed_3x",
		5: "pushed_4x",
		6: "pushed_5x"
]}

private getCommandClasses() {[
		0x84: 1
]}

private isAeotec() {
	zwaveInfo.mfr == "0371"
}

private getSupportedButtonValues() {
	if (isAeotec()) {
		["pushed", "held"]
	} else {
		["pushed", "held", "down_hold", "double", "pushed_3x", "pushed_4x", "pushed_5x"]
	}
}
