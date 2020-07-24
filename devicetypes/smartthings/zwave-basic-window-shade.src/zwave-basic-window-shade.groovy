/**
 *  Copyright 2019 SRPOL
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
	definition (name: "Z-Wave Basic Window Shade", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-stateless-curtain") {
		capability "Stateless Curtain Power Button"
		capability "Configuration"
		capability "Actuator"
		capability "Health Check"

		command "open"
		command "close"
		command "pause"

		fingerprint mfr:"0086", prod:"0003", model:"008D", deviceJoinName: "Aeotec Window Treatment" //Aeotec Nano Shutter
		fingerprint mfr:"0086", prod:"0103", model:"008D", deviceJoinName: "Aeotec Window Treatment" //Aeotec Nano Shutter
		fingerprint mfr:"0371", prod:"0003", model:"008D", deviceJoinName: "Aeotec Window Treatment" //Aeotec Nano Shutter
		fingerprint mfr:"0371", prod:"0103", model:"008D", deviceJoinName: "Aeotec Window Treatment" //Aeotec Nano Shutter
	}

	tiles(scale: 2) {
 		standardTile("open", "device.open", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Open', action:"open"
		}
		standardTile("close", "device.close", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Close', action:"close"
		}
		standardTile("pause", "device.pause", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'Pause', action:"pause"
		}

		details(["open","close","pause"])
	}

	preferences {
		section {
			input(title: "Aeotec Nano Shutter settings",
				description: "In case wiring is wrong, this setting can be changed to fix setup without any manual maintenance.",
				displayDuringSetup: false,
				type: "paragraph",
				element: "paragraph")

			input("reverseDirection", "bool",
				title: "Reverse working direction",
				defaultValue: false,
				displayDuringSetup: false
			)
		}
	}
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned: ${result}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	log.debug "Security Message Encap ${cmd}"
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (!state.ignoreResponse)
		state.shadeState = (cmd.value == closeValue ? "closing" : "opening")

	state.ignoreResponse = false
	[:]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled ${cmd}"
	createEvent(descriptionText: "An event came in")
}

def setButton(button) {
	switch(button) {
		case "open":
			open()
			break
		case "close":
			close()
			break
		default:
			pause()
			break
	}
}

def open() {
	state.shadeState = "opening"
	secure(zwave.basicV1.basicSet(value: openValue))
}

def close() {
	state.shadeState = "closing"
	secure(zwave.basicV1.basicSet(value: closeValue))
}

def pause() {
	def value = state.shadeState == "opening" ? closeValue : openValue
	def result = state.shadeState != "paused" ? secure(zwave.switchBinaryV1.switchBinarySet(switchValue: value)) : []
	state.ignoreResponse = true
	state.shadeState = "paused"
	result
}

def ping() {
	secure(zwave.switchMultilevelV3.switchMultilevelGet())
}

def installed() {
	log.debug "Installed ${device.displayName}"
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "availableCurtainPowerButtons", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)
	state.shadeState = "paused"
	state.reverseDirection = reverseDirection ? reverseDirection : false
}

def updated() {
	sendHubCommand(pause())
	state.reverseDirection = reverseDirection ? reverseDirection : false
}

def configure() {
	log.debug "Configure..."
	response([
			secure(zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: 1)),
			secure(zwave.configurationV1.configurationSet(parameterNumber: 85, size: 1, scaledConfigurationValue: 1))
	])
}

private secure(cmd) {
	if(zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private getOpenValue() {
	!state.reverseDirection ? 0x00 : 0xFF
}

private getCloseValue() {
	!state.reverseDirection ? 0xFF : 0x00
}