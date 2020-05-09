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
	definition (name: "Z-Wave Controller", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {

		command "on"
		command "off"

		fingerprint deviceId: "0x02"
	}

	simulator {

	}

	tiles {
		standardTile("state", "device.state", width: 2, height: 2) {
			state 'connected', icon: "st.unknown.zwave.static-controller", backgroundColor:"#ffffff"
		}
		standardTile("basicOn", "device.switch", inactiveLabel:false, decoration:"flat") {
			state "on", label:"on", action:"on", icon:"st.switches.switch.on"
		}
		standardTile("basicOff", "device.switch", inactiveLabel: false, decoration:"flat") {
			state "off", label:"off", action:"off", icon:"st.switches.switch.off"
		}

		main "state"
		details(["state", "basicOn", "basicOff"])
	}
}

def installed() {
	if (zwaveInfo.cc?.contains("84")) {
		response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		if (description.startsWith("Err 106") && !state.sec) {
			state.sec = 0
		}
	    result = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def result = []
	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: true)
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	state.sec = 1
	createEvent(isStateChange: true, descriptionText: "$device.displayName: ${cmd.encapsulatedCommand()} [secure]")
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	createEvent(isStateChange: true, descriptionText: "$device.displayName: ${cmd.encapsulatedCommand()}")
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(isStateChange: true, descriptionText: "$device.displayName: $cmd")
}

def on() {
	command(zwave.basicV1.basicSet(value: 0xFF))
}

def off() {
	command(zwave.basicV1.basicSet(value: 0x00))
}

private command(physicalgraph.zwave.Command cmd) {
	if (deviceIsSecure) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private getDeviceIsSecure() {
	if (zwaveInfo && zwaveInfo.zw) {
		return zwaveInfo.zw.contains("s")
	} else {
		return state.sec ? true : false
	}
}
