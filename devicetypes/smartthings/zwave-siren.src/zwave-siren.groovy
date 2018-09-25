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
			state "off", label: 'off', action: 'alarm.strobe', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
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

		main "alarm"
		details(["alarm", "off", "battery", "refresh"])
	}
}

def installed() {
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	initialize()
}

def updated() {
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	runIn(12, "initialize", [overwrite: true, forceForLocallyExecuting: true])
}

def initialize() {
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
		cmds << zwave.basicV1.basicGet().format()
		cmds << "delay 5"
	}
	if (!device.currentState("battery")) {
		if (zwaveInfo?.cc?.contains("80")) {
			cmds << zwave.batteryV1.batteryGet().format()
		} else {
			// Right now this DTH assumes all devices are battery powered, in the event a device is wall powered we should populate something
			sendEvent(name: "battery", value: 100, unit: "%")
		}
	}

	if (cmds.size()) {
		sendHubCommand(cmds)

		runIn(12, "initialize", [overwrite: true, forceForLocallyExecuting: true])
	} else {
		state.initializeCount = 0
	}
}

def configure() {
	log.debug "config"
	def cmds = []
	if (zwaveInfo.mfr == "0131" && zwaveInfo.model == "1083") {
		// Set alarm volume to 3 (loud)
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [3]).format()
		cmds << "delay 500"
		// Set alarm duration to 60s (default)
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [2]).format()
		cmds << "delay 500"
		// Set alarm sound to no.10
		cmds << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, configurationValue: [10]).format()
	}
	response(cmds)
}

def poll() {
	if (secondsPast(state.lastbatt, 36 * 60 * 60)) {
		return zwave.batteryV1.batteryGet().format()
	} else {
		return null
	}
}

def on() {
	log.debug "sending on"
	// ICP-5323: Zipato siren sometimes fails to make sound for full duration
	// Those alarms do not end with Siren Notification Report.
	// For those cases we add additional state check after alarm duration to
	// synchronize cloud state with actual device state.
	if (zwaveInfo.mfr == "0131" && zwaveInfo.model == "1083") {
		[
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.basicV1.basicGet().format(),
			"delay 63000",
			zwave.basicV1.basicGet().format()
		]
	} else {
		[
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.basicV1.basicGet().format()
		]
	}
}

def off() {
	log.debug "sending off"
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet().format()
	]
}

def strobe() {
	log.debug "sending stobe/on command"
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.basicV1.basicGet().format()
	]
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
	delayBetween([
		zwave.basicV1.basicGet().format(),
		zwave.batteryV1.batteryGet().format()
	], 2000)
}

def parse(String description) {
	log.debug "parse($description)"
	def result = null
	def cmd = zwave.parse(description, [0x20: 1])
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def switchValue = cmd.value ? "on" : "off"
	def alarmValue
	if (cmd.value == 0) {
		alarmValue = "off"
	} else if (cmd.value <= 33) {
		alarmValue = "strobe"
	} else if (cmd.value <= 66) {
		alarmValue = "siren"
	} else {
		alarmValue = "both"
	}
	[
		createEvent([name: "switch", value: switchValue, type: "digital", displayed: false]),
		createEvent([name: "alarm", value: alarmValue, type: "digital"])
	]
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
	if (cmd.notificationType == 0x0E) { //Siren notification
		switch (cmd.event) {
			case 0x00: // idle
				isActive = false
				break
			case 0x01: // active
				isActive = true
				break
		}
	}
	[
		createEvent([name: "switch", value: isActive ? "on" : "off", displayed: false]),
		createEvent([name: "alarm", value: isActive ? "both" : "off"])
	]
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
