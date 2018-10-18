/**
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition(name: "Yale External Siren", namespace: "smartthings", mnmn: "SmartThings", vid: "generic-siren-3", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Refresh"
		capability "Switch"

		//zw:Fs type:1005 mfr:0129 prod:6F01 model:0001 ver:1.04 zwv:4.33 lib:03 cc:5E,80,5A,72,73,86,70,98 sec:59,2B,71,85,25,7A role:07 ff:8F00 ui:8F00
		fingerprint mfr: "0129", prod: "6F01", model: "0001", deviceJoinName: "Yale External Siren"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "alarm", type: "generic", width: 6, height: 4) {
			tileAttribute("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'off', action: 'alarm.siren', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "both", label: 'alarm!', action: 'alarm.off', icon: "st.alarm.alarm.alarm", backgroundColor: "#e86d13"
			}
		}
		standardTile("test", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: '', action: "test", icon: "st.secondary.test"
		}
		standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: '', action: "alarm.off", icon: "st.secondary.off"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		preferences {
			input name: "alarmLength", type: "number", title: "Alarm length (1-10 min)", range: "1..10"
			// defaultValue: 10
			input name: "alarmLEDflash", type: "bool", title: "Alarm LED flash"
			// defaultValue: false
			input name: "comfortLED", type: "number", title: "Comfort LED (0-25 x 10 sec.)", range: "0..25"
			// defaultValue: 0
			input name: "tamper", type: "bool", title: "Tamper"
			// defaultValue: false
		}

		main "alarm"
		details(["alarm", "test", "off", "battery", "refresh"])
	}
}

def installed() {
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	response([secure(zwave.basicV1.basicGet()), zwave.batteryV1.batteryGet()])
}

def updated() {
	def commands = []
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	if (!state.alarmLength) state.alarmLength = 10 // default value
	if (!state.alarmLEDflash) state.alarmLEDflash = 1 // default value
	if (!state.comfortLED) state.comfortLED = 0 // default value
	if (!state.tamper) state.tamper = 0 // default value

	log.debug "settings: ${settings.inspect()}, state: ${state.inspect()}"

	Short alarmLength = (settings.alarmLength as Short) ?: 1
	Boolean alarmLEDflash = (settings.alarmLEDflash as Boolean) ?: 0
	Short comfortLED = (settings.comfortLED as Short) ?: 0
	Boolean tamper = (settings.tamper as Boolean) ?: 0

	if (alarmLength != state.alarmLength || alarmLEDflash != state.alarmLEDflash || comfortLED != state.comfortLED || tamper != state.tamper) {
		state.alarmLength = alarmLength
		state.alarmLEDflash = alarmLEDflash
		state.comfortLED = comfortLED
		state.tamper = tamper

		commands << new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [alarmLength]).format())
		commands << new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [alarmLEDflash ? 1 : 0]).format())
		commands << new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, configurationValue: [comfortLED]).format())
		commands << new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [tamper ? 1 : 0]).format())
		commands << "delay 1000"
		commands << secure(zwave.basicV1.basicSet(value: 0x00))
	}
	response(commands)
}

def parse(String description) {
	log.debug "parse($description)"
	def result = null
	if (description.startsWith("Err")) {
		if (state.sec) {
			result = createEvent(descriptionText: description, displayed: false)
		} else {
			result = createEvent(
				descriptionText: "This device failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned ${result?.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x59: 1, 0x2B: 1, 0x71: 2, 0x85: 2, 0x25: 2, 0x7A: 1])
	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.debug "BasicReport:  $cmd"
	[
		createEvent([name: "switch", value: cmd.value ? "on" : "off", displayed: false]),
		createEvent([name: "alarm", value: cmd.value ? "both" : "off"])
	]
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.debug "BatteryReport:  $cmd"

	def batteryLevel = cmd.batteryLevel != 255 ? cmd.batteryLevel : 0
	def map = [name: "battery", unit: "%", value: batteryLevel]
	createEvent(map)
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
	log.debug "sending off()"
	[
		secure(zwave.basicV1.basicSet(value: 0x00)),
		secure(zwave.basicV1.basicGet())
	]
}

def strobe() {
	log.debug "strobe()"
	on()
}

def siren() {
	log.debug "siren()"
	on()
}

def both() {
	log.debug "both()"
	on()
}

def test() {
	log.debug "test()"
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

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping()"
	delayBetween([
		secure(zwave.basicV1.basicGet()),
		zwave.batteryV1.batteryGet().format()
	], 2000)
}

def refresh() {
	log.debug "sending battery refresh command"
	delayBetween([
		secure(zwave.basicV1.basicGet()),
		zwave.batteryV1.batteryGet().format()
	], 2000)
}