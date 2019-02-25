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

metadata {
	definition (name: "Z-Wave Basic Window Shade", namespace: "smartthings", author: "SmartThings") {
		capability "Stateless Curtain Power Button"
		capability "Configuration"
		capability "Actuator"
		capability "Health Check"

		command "open"
		command "close"
		command "pause"

		fingerprint mfr:"0086", prod:"0003", model:"008D", deviceJoinName: "Aeotec Nano Shutter"
		fingerprint mfr:"0086", prod:"0103", model:"008D", deviceJoinName: "Aeotec Nano Shutter"
	}

	tiles(scale: 2) {
//		standardTile("open", "device.open", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
//			state "default", label:'Open', action:"statelessCurtainPowerButton.open"
//		}
//		standardTile("close", "device.close", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
//			state "default", label:'Close', action:"statelessCurtainPowerButton.close"
//		}
//		standardTile("close", "device.pause", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
//			state "default", label:'Pause', action:"statelessCurtainPowerButton.pause"
//		}
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
	state.shadeState = cmd.value ? "closing" : "opening"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled ${cmd}"
	createEvent(descriptionText: "An event came in")
}

def open() {
	state.shadeState = "opening"
	secure(zwave.basicV1.basicSet(value: 0x00))
}

def close() {
	state.shadeState = "closing"
	secure(zwave.basicV1.basicSet(value: 0xFF))
}

def pause() {
	def value = state.shadeState == "opening" ? 0xFF : 0x00
	def result = state.shadeState != "paused" ? secure(zwave.switchBinaryV1.switchBinarySet(switchValue: value)) : []
	state.shadeState = "paused"
	result
}

def ping() {
	secure(zwave.switchMultilevelV3.switchMultilevelGet())
}

def installed() {
	log.debug "Installed ${device.displayName}"
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	state.shadeState = "paused"
}

def configure() {
	log.debug "Configure..."
	response(secure(zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: 1)))
}

private secure(cmd) {
	if(zwaveInfo.zw.endsWith("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}
