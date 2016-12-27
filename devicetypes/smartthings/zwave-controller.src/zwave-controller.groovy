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
	definition (name: "Z-Wave Controller", namespace: "smartthings", author: "SmartThings") {

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

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = createEvent(zwaveEvent(cmd))
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def event = [isStateChange: true]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: ${cmd.encapsulatedCommand()} [secure]"
	event
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def event = [isStateChange: true]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: $cmd"
	event
}

def on() {
	zwave.basicV1.basicSet(value: 0xFF).format()
}

def off() {
	zwave.basicV1.basicSet(value: 0x00).format()
}