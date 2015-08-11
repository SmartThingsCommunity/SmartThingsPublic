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
 *	Aeon Siren
 *
 *	Author: SmartThings
 *	Date: 2014-07-15
 */
metadata {
 definition (name: "Aeon Siren", namespace: "smartthings", author: "SmartThings") {
	capability "Actuator"
	capability "Alarm"
	capability "Switch"

	command "test"

	fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98"
 }

 simulator {
	// reply messages
	reply "9881002001FF,9881002002": "command: 9881, payload: 002003FF"
	reply "988100200100,9881002002": "command: 9881, payload: 00200300"
	reply "9881002001FF,delay 3000,988100200100,9881002002": "command: 9881, payload: 00200300"
 }

 tiles(scale: 2) {
	multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4){
		tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
			attributeState "off", label:'off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
			attributeState "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
		}
	}
	standardTile("test", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:'', action:"test", icon:"st.secondary.test"
	}
	standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:'', action:"alarm.off", icon:"st.secondary.off"
	}

	preferences {
		input "sound", "number", title: "Siren sound (1-5)", defaultValue: 1, required: true//, displayDuringSetup: true  // don't display during setup until defaultValue is shown
		input "volume", "number", title: "Volume (1-3)", defaultValue: 3, required: true//, displayDuringSetup: true
	}

	main "alarm"
	details(["alarm", "test", "off"])
 }
}

def updated() {
	if(!state.sound) state.sound = 1
	if(!state.volume) state.volume = 3

	log.debug "settings: ${settings.inspect()}, state: ${state.inspect()}"

	Short sound = (settings.sound as Short) ?: 1
	Short volume = (settings.volume as Short) ?: 3

	if (sound != state.sound || volume != state.volume) {
		state.sound = sound
		state.volume = volume
		return response([
			secure(zwave.configurationV1.configurationSet(parameterNumber: 37, size: 2, configurationValue: [sound, volume])),
			"delay 1000",
			secure(zwave.basicV1.basicSet(value: 0x00)),
		])
	}
}

def parse(String description) {
	log.debug "parse($description)"
	def result = null
	def cmd = zwave.parse(description, [0x98: 1, 0x20: 1, 0x70: 1])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "Parse returned ${result?.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x85: 2, 0x70: 1])
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "rx $cmd"
	[
		createEvent([name: "switch", value: cmd.value ? "on" : "off", displayed: false]),
		createEvent([name: "alarm", value: cmd.value ? "both" : "off"])
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}

def on() {
	log.debug "sending on"
	[
		secure(zwave.basicV1.basicSet(value: 0xFF)),
		secure(zwave.basicV1.basicGet())
	]
}

def off() {
	log.debug "sending off"
	[
		secure(zwave.basicV1.basicSet(value: 0x00)),
		secure(zwave.basicV1.basicGet())
	]
}

def strobe() {
	on()
}

def siren() {
	on()
}

def both() {
	on()
}

def test() {
	[
		secure(zwave.basicV1.basicSet(value: 0xFF)),
		"delay 3000",
		secure(zwave.basicV1.basicSet(value: 0x00)),
		secure(zwave.basicV1.basicGet())
	]
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}
