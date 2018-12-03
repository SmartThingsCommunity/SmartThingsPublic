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
 *  Everspring Siren
 *
 *  Author: SmartThings
 *  Date: 2014-07-15
 */
metadata {
	definition(name: "Z-Wave Siren", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Health Check"

		fingerprint inClusters: "0x20,0x25,0x86,0x80,0x85,0x72,0x71"
		fingerprint mfr: "0258", prod: "0003", model: "0088", deviceJoinName: "NEO Coolcam Siren Alarm"
		fingerprint mfr: "021F", prod: "0003", model: "0088", deviceJoinName: "Dome Siren"
		fingerprint mfr: "0060", prod: "000C", model: "0001", deviceJoinName: "Utilitech Siren"
		//zw:F type:1005 mfr:0131 prod:0003 model:1083 ver:2.17 zwv:6.02 lib:06 cc:5E,9F,55,73,86,85,8E,59,72,5A,25,71,87,70,80,6C role:07 ff:8F00 ui:8F00
		fingerprint mfr: "0131", prod: "0003", model: "1083", deviceJoinName: "Zipato Siren Alarm"
		//zw:F type:1005 mfr:0258 prod:0003 model:1088 ver:2.94 zwv:4.38 lib:06 cc:5E,86,72,5A,73,70,85,59,25,71,87,80 role:07 ff:8F00 ui:8F00 (EU)
		fingerprint mfr: "0258", prod: "0003", model: "1088", deviceJoinName: "NEO Coolcam Siren Alarm"
		//zw:Fs type:1005 mfr:0129 prod:6F01 model:0001 ver:1.04 zwv:4.33 lib:03 cc:5E,80,5A,72,73,86,70,98 sec:59,2B,71,85,25,7A role:07 ff:8F00 ui:8F00
		fingerprint mfr: "0129", prod: "6F01", model: "0001", deviceJoinName: "Yale External Siren"
	}

	simulator {
		// reply messages
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"
		reply "200121,2002": "command: 2003, payload: 21"
		reply "200142,2002": "command: 2003, payload: 42"
		reply "2001FF,delay 3000,200100,2002": "command: 2003, payload: 00"
	}

	tiles {
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "off", label: 'off', action: 'alarm.both', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
			state "both", label: 'alarm!', action: 'alarm.off', icon: "st.alarm.alarm.alarm", backgroundColor: "#e86d13"
		}
		standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "alarm.off", icon: "st.secondary.off"
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label: '', action: "configuration.configure", icon: "st.secondary.configure"
		}

		// Yale siren only
		preferences {
			input name: "alarmLength", type: "number", title: "Alarm length (1-10 min)", range: "1..10"
			// defaultValue: 10
			input name: "alarmLEDflash", type: "bool", title: "Alarm LED flash"
			// defaultValue: false
			input name: "comfortLED", type: "number", title: "Comfort LED (0-25 x 10 sec.)", range: "0..25"
			// defaultValue: 0
			input name: "tamper", type: "bool", title: "Tamper alert"
			// defaultValue: false
		}

		main "alarm"
		details(["alarm", "off", "refresh", "battery", "configure"])
	}
}

def installed() {
	log.debug "installed()"
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	initialize()
}

def updated() {
	log.debug "updated()"
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	//sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	sendEvent(name: "checkInterval", value: 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	runIn(12, "initialize", [overwrite: true, forceForLocallyExecuting: true])
}

def initialize() {
	log.debug "initialize()"
	def cmds = []

	// Set a limit to the number of times that we run so that we don't run forever and ever
	if (!state.initializeCount) {
		state.initializeCount = 1
	} else if (state.initializeCount <= 10) { // Keep checking for ~2 mins (10 * 12 sec intervals)
		state.initializeCount = state.initializeCount + 1
	} else {
		state.initializeCount = 0
		return // TODO: This might be a good opportunity to mark the device unhealthy
	}

	if (!device.currentState("alarm")) {
		cmds << secure(zwave.basicV1.basicGet())
		if (isYale()) {
			cmds << secure(zwave.switchBinaryV1.switchBinaryGet())
		}
	}
	if (!device.currentState("battery")) {
		if (zwaveInfo?.cc?.contains("80")) {
			cmds << secure(zwave.batteryV1.batteryGet())
		} else {
			// Right now this DTH assumes all devices are battery powered, in the event a device is wall powered we should populate something
			sendEvent(name: "battery", value: 100, unit: "%")
		}
	}
	cmds << getConfigurationCommands()
	if (cmds.size()) {
		sendHubCommand(cmds)
		runIn(12, "initialize", [overwrite: true, forceForLocallyExecuting: true])
	} else {
		state.initializeCount = 0
	}
}

def configure() {
	log.debug "config"
	response(getConfigurationCommands())
}

def getConfigurationCommands() {
	log.debug "getConfigurationCommands"
	def cmds = []
	if (isZipato()) {
		// Set alarm volume to 3 (loud)
		cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [3]))
		cmds << "delay 500"
		// Set alarm duration to 60s (default)
		cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [2]))
		cmds << "delay 500"
		// Set alarm sound to no.10
		cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, configurationValue: [10]))
	} else if (isYale()) {
		if (!state.alarmLength) state.alarmLength = 10 // default value
		if (!state.alarmLEDflash) state.alarmLEDflash = true // default value
		if (!state.comfortLED) state.comfortLED = 0 // default value
		if (!state.tamper) state.tamper = false // default value

		log.debug "settings: ${settings.inspect()}"
		log.debug "state: ${state.inspect()}"

		Short alarmLength = (settings.alarmLength as Short) ?: 10
		Boolean alarmLEDflash = (settings.alarmLEDflash as Boolean) == null ? true : settings.alarmLEDflash
		Short comfortLED = (settings.comfortLED as Short) ?: 0
		Boolean tamper = (settings.tamper as Boolean) == null ? false : settings.tamper

		if (alarmLength != state.alarmLength || alarmLEDflash != state.alarmLEDflash || comfortLED != state.comfortLED || tamper != state.tamper) {
			state.alarmLength = alarmLength
			state.alarmLEDflash = alarmLEDflash
			state.comfortLED = comfortLED
			state.tamper = tamper

			cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [alarmLength]))
			cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [alarmLEDflash ? 1 : 0]))
			cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, configurationValue: [comfortLED]))
			cmds << secure(zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [tamper ? 1 : 0]))
			cmds << "delay 1000"
			cmds << secure(zwave.basicV1.basicSet(value: 0x00))
		}
	}
	cmds
}


def poll() {
	if (secondsPast(state.lastbatt, 36 * 60 * 60)) {
		return secure(zwave.batteryV1.batteryGet())
	} else {
		return null
	}
}

def on() {
	log.debug "sending on"
	def cmds = []
	cmds << secure(zwave.basicV1.basicSet(value: 0xFF))
	cmds << "delay 3000"
	cmds << secure(zwave.basicV1.basicGet())

	// ICP-5323: Zipato siren sometimes fails to make sound for full duration
	// Those alarms do not end with Siren Notification Report.
	// For those cases we add additional state check after alarm duration to
	// synchronize cloud state with actual device state.
	if (isZipato()) {
		cmds << "delay 63000"
		cmds << secure(zwave.basicV1.basicGet())
	} else if (isYale()) {
		cmds << secure(zwave.switchBinaryV1.switchBinaryGet())
	}
	return cmds
}

def off() {
	log.debug "sending off"
	def cmds = []
	cmds << secure(zwave.basicV1.basicSet(value: 0x00))
	cmds << "delay 3000"
	cmds << secure(zwave.basicV1.basicGet())

	if (isYale()) {
		cmds << secure(zwave.switchBinaryV1.switchBinaryGet())
	}
	return cmds
}

def siren() {
	on()
}

def strobe() {
	on()
}

def both() {
	on()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping() called"
	refresh()
}

def refresh() {
	log.debug "sending battery refresh command"
	def cmds = []
	cmds << secure(zwave.basicV1.basicGet())
	cmds << secure(zwave.batteryV1.batteryGet())
	if (isYale()) {
		cmds << secure(zwave.switchBinaryV1.switchBinaryGet())
	}
	return delayBetween(cmds, 2000)
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
		def cmd = zwave.parse(description, [0x20: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

private secure(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand
	if (isYale()) {
		encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1])
	}
	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	handleSwitchValue(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	handleSwitchValue(cmd.value)
}

def handleSwitchValue(value) {
	def result = []
	def switchValue = value ? "on" : "off"
	def alarmValue
	if (value == 0) {
		alarmValue = "off"
	} else if (value <= 33) {
		alarmValue = "strobe"
	} else if (value <= 66) {
		alarmValue = "siren"
	} else {
		alarmValue = "both"
	}
	result << createEvent([name: "switch", value: switchValue, displayed: true])
	result << createEvent([name: "alarm", value: alarmValue, displayed: true])
	result
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def isActive = false
	def result = []
	if (cmd.notificationType == 0x0E) { //Siren notification
		switch (cmd.event) {
			case 0x00: // idle
				isActive = false
				break
			case 0x01: // active
				isActive = true
				break
		}
		result << createEvent([name: "switch", value: isActive ? "on" : "off", displayed: true])
		result << createEvent([name: "alarm", value: isActive ? "both" : "off", displayed: true])
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "UNEXPECTED COMMAND: $cmd"
}

private Boolean secondsPast(timestamp, seconds) {
	if (!(timestamp instanceof Number)) {
		if (timestamp instanceof Date) {
			timestamp = timestamp.time
		} else if ((timestamp instanceof String) && timestamp.isNumber()) {
			timestamp = timestamp.toLong()
		} else {
			return true
		}
	}
	return (new Date().time - timestamp) > (seconds * 1000)
}

def isYale() {
	(zwaveInfo?.mfr == "0129" && zwaveInfo?.prod == "6F01" && zwaveInfo?.model == "0001")
}

def isZipato() {
	(zwaveInfo?.mfr == "0131" && zwaveInfo?.prod == "0003" && zwaveInfo?.model == "1083")
}