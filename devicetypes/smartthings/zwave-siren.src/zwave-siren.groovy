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
	definition(name: "Z-Wave Siren", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Z-Wave") {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Tamper Alert"
		capability "Health Check"

		fingerprint inClusters: "0x20,0x25,0x86,0x80,0x85,0x72,0x71", deviceJoinName: "Siren"
		fingerprint mfr: "0258", prod: "0003", model: "0088", deviceJoinName: "NEO Coolcam Siren" //NEO Coolcam Siren Alarm
		fingerprint mfr: "021F", prod: "0003", model: "0088", deviceJoinName: "Dome Siren" //Dome Siren
		fingerprint mfr: "0060", prod: "000C", model: "0001", deviceJoinName: "Utilitech Siren" //Utilitech Siren
		//zw:F type:1005 mfr:0131 prod:0003 model:1083 ver:2.17 zwv:6.02 lib:06 cc:5E,9F,55,73,86,85,8E,59,72,5A,25,71,87,70,80,6C role:07 ff:8F00 ui:8F00
		fingerprint mfr: "0131", prod: "0003", model: "1083", deviceJoinName: "Zipato Siren" //Zipato Siren Alarm
		//zw:F type:1005 mfr:0258 prod:0003 model:1088 ver:2.94 zwv:4.38 lib:06 cc:5E,86,72,5A,73,70,85,59,25,71,87,80 role:07 ff:8F00 ui:8F00 (EU)
		fingerprint mfr: "0258", prod: "0003", model: "1088", deviceJoinName: "NEO Coolcam Siren" //NEO Coolcam Siren Alarm
		//zw:Fs type:1005 mfr:0129 prod:6F01 model:0001 ver:1.04 zwv:4.33 lib:03 cc:5E,80,5A,72,73,86,70,98 sec:59,2B,71,85,25,7A role:07 ff:8F00 ui:8F00
		fingerprint mfr: "0129", prod: "6F01", model: "0001", deviceJoinName: "Yale Siren" //Yale External Siren
		fingerprint mfr: "0060", prod: "000C", model: "0002", deviceJoinName: "Everspring Siren", vid: "generic-siren-12" //Everspring Outdoor Solar Siren
		fingerprint mfr: "0154", prod: "0004", model: "0002", deviceJoinName: "POPP Siren", vid: "generic-siren-12" //POPP Solar Outdoor Siren
		fingerprint mfr: "0109", prod: "2005", model: "0518", deviceJoinName: "Vision Siren" //Vision Outdoor Siren
		fingerprint mfr: "0258", prod: "0003", model: "6088", deviceJoinName: "NEO Coolcam Siren"//AU //NEO Coolcam Siren Alarm
		fingerprint mfr: "0258", prod: "0600", model: "1028", deviceJoinName: "NEO Coolcam Siren"//MY //NEO Coolcam Siren Alarm
		fingerprint mfr: "0109", prod: "2009", model: "0908", deviceJoinName: "Vision Siren" //Vision Indoor Siren
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
		valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
			state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
			state "detected", label: 'tampered', backgroundColor: "#ffffff"
		}

		// Yale siren only
		preferences {
			input name: "alarmLength", type: "number", title: "Alarm length", description: "This setting does not apply to all devices", range: "1..10"
			// defaultValue: 10
			input name: "alarmLEDflash", type: "bool", title: "Alarm LED flash", description: "This setting does not apply to all devices"
			// defaultValue: false
			input name: "comfortLED", type: "number", title: "Comfort LED (x10 sec.)", description: "This setting does not apply to all devices", range: "0..25"
			// defaultValue: 0
			input name: "tamper", type: "bool", title: "Tamper alert", description: "This setting does not apply to all devices"
			// defaultValue: false
		}

		main "alarm"
		details(["alarm", "off", "refresh", "tamper" ,"battery", "configure"])
	}
}

// Perform a periodic check to ensure that initialization of the device was successful
def getINIT_VERIFY_CHECK_PERIODIC_SECS() {30}
def getINIT_VERIFY_CHECK_MAX_ATTEMPTS() {3}

def installed() {
	log.debug "installed()"
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	state.initializeAttempts = 0
	initialize()
}

def updated() {
	log.debug "updated()"
	state.configured = false
	state.initializeAttempts = 0
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "tamper", value: "clear")
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	log.debug "updated(): Schedule in ${INIT_VERIFY_CHECK_PERIODIC_SECS} secs to verify initilization"
	runIn(INIT_VERIFY_CHECK_PERIODIC_SECS, "initializeCallback", [overwrite: true, forceForLocallyExecuting: true])
}

def initializeCallback() {
	log.debug "initializeCallback()"
	state.initializeVerifyTimerPending = false
	initialize()
}

def initialize() {
	if (state.initializeVerifyTimerPending) {
		log.warn "Initialize(): Verification is pending"
		return
	}

	log.debug "initialize (Attempt: ${state.initializeAttempts + 1}/${INIT_VERIFY_CHECK_MAX_ATTEMPTS})"
	if (state.initializeAttempts >= INIT_VERIFY_CHECK_MAX_ATTEMPTS) {
		log.warn "Initializition of ${device.displayName} has failed with too many attempts"
		return
	}

	def cmds = []

	if (!device.currentState("alarm")) {
		cmds << secure(zwave.basicV1.basicGet())
		if (isYale()) {
			cmds << secure(zwave.switchBinaryV1.switchBinaryGet())
		}
	}
	if (!device.currentState("battery")) {
		if (zwaveInfo?.cc?.contains("80") || zwaveInfo?.sec?.contains("80")) {
			cmds << secure(zwave.batteryV1.batteryGet())
		} else {
			// Right now this DTH assumes all devices are battery powered, in the event a device is wall powered we should populate something
			sendEvent(name: "battery", value: 100, unit: "%")
		}
	}
	if (!state.configured) {
		// if this flag is not set, we have not successfully configured
		cmds << getConfigurationCommands()
	}

	// if there's anything we need to send, send it now, and check again at a later time
	if (cmds.size > 0) {
		sendHubCommand(cmds)
		state.initializeAttempts = state.initializeAttempts + 1
		state.initializeVerifyTimerPending = true
		log.debug "initialize(): Schedule in ${INIT_VERIFY_CHECK_PERIODIC_SECS} secs to verify initilization"
		runIn(INIT_VERIFY_CHECK_PERIODIC_SECS, "initializeCallback", [overwrite: true, forceForLocallyExecuting: true])
	} else {
		log.debug "Initialization is complete!"
	}
}

def configure() {
	log.debug "config"
	response(getConfigurationCommands())
}

// configuration defaults indexed by parameter number
def getZipatoDefaults() {
	[1: 3,
	 2: 2,
	 5: 10]
}

def getYaleDefaults() {
	[1: 10,
	 2: true,
	 3: 0,
	 4: false]
}

def getEverspringDefaultAlarmLength() {
	return 180
}

def getConfigurationCommands() {
	log.debug "getConfigurationCommands"
	def cmds = []
	if (isZipato()) {
		// Set alarm volume to 3 (loud)
		cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: zipatoDefaults[1]))
		cmds << "delay 500"
		// Set alarm duration to 60s (default)
		cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: zipatoDefaults[2]))
		cmds << "delay 500"
		// Set alarm sound to no.10
		cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: zipatoDefaults[5]))
	} else if (isYale()) {
		if (!state.alarmLength) state.alarmLength = yaleDefaults[1]
		if (!state.alarmLEDflash) state.alarmLEDflash = yaleDefaults[2]
		if (!state.comfortLED) state.comfortLED = yaleDefaults[3]
		if (!state.tamper) state.tamper = yaleDefaults[4]

		log.debug "settings: ${settings.inspect()}"
		log.debug "state: ${state.inspect()}"

		Short alarmLength = (settings.alarmLength as Short) ?: yaleDefaults[1]
		Boolean alarmLEDflash = (settings.alarmLEDflash as Boolean) == null ? yaleDefaults[2] : settings.alarmLEDflash
		Short comfortLED = (settings.comfortLED as Short) ?: yaleDefaults[3]
		Boolean tamper = (settings.tamper as Boolean) == null ? yaleDefaults[4] : settings.tamper

		if (alarmLength != state.alarmLength || alarmLEDflash != state.alarmLEDflash || comfortLED != state.comfortLED || tamper != state.tamper) {
			state.alarmLength = alarmLength
			state.alarmLEDflash = alarmLEDflash
			state.comfortLED = comfortLED
			state.tamper = tamper

			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 1, size: 1, configurationValue: [alarmLength]))
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 2, size: 1, configurationValue: [alarmLEDflash ? 1 : 0]))
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 3, size: 1, configurationValue: [comfortLED]))
			cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 4, size: 1, configurationValue: [tamper ? 1 : 0]))
			cmds << "delay 1000"
			cmds << secure(zwave.basicV1.basicSet(value: 0x00))
		} else {
			state.configured = true
		}
	} else {
		// if there's nothing to configure, we're configured
		state.configured = true
	}

	if (isEverspring()) {
		if (!state.alarmLength) {
			state.alarmLength = everspringDefaultAlarmLength
		}
		Short alarmLength = (settings.alarmLength as Short) ?: everspringDefaultAlarmLength

		if (alarmLength != state.alarmLength) {
			alarmLength = calcEverspringAlarmLen(alarmLength)
			state.alarmLength = alarmLength
			log.debug "alarm settings: ${alarmLength}"
		}
		cmds << secure(zwave.configurationV2.configurationSet(parameterNumber: 1, size: 2, configurationValue: [0,alarmLength]))
	}

	if (cmds.size > 0) {
		// send this last to confirm we were heard
		cmds << secure(zwave.configurationV2.configurationGet(parameterNumber: 1))
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def checkVal
	// the last message sent by configure is a configuration get, so if we get a report, we succeeded in transmission
	// and if the parameter 1 values match what we expect, then the configuration probably succeeded
	if (isZipato()) {
		checkVal = zipatoDefaults[1]
	} else if (isYale()) {
		checkVal = state.alarmLength
	}
	if (checkVal != null) {
		state.configured = (checkVal == cmd.scaledConfigurationValue)
	} else {
		state.configured = true
	}
	log.debug "configuration report: ${cmd}"
	return [:]
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

	// The Utilitech siren always sends low battery events (0xFF) below 20%,
	// so we will ignore 0% events that sometimes seem to come before valid events.
	if (cmd.batteryLevel == 0 && isUtilitech()) {
		log.debug "Ignoring battery 0%"
		return [:]
	} else if (cmd.batteryLevel == 0xFF) {
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
	} else if (cmd.notificationType == 0x07) { //Tamper Alert
		switch (cmd.event) {
			case 0x00: //Tamper switch is pressed more than 3 sec
				result << createEvent([name: "tamper", value: "clear"])
				break
			case 0x03: //Tamper switch is pressed more than 3 sec and released
				result << createEvent([name: "tamper", value: "detected"])
				result << createEvent([name: "alarm", value: "both"])
				break
		}
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

def calcEverspringAlarmLen(int alarmLength) {
	//If the siren is Everspring then the alarm length can be set to 1, 2 or max 3 minutes
	def map = [1:60, 2:120, 3:180]
	if (alarmLength > 3) {
		return everspringDefaultAlarmLength
	} else {
		return map[alarmLength].value
	}
}

def isYale() {
	(zwaveInfo?.mfr == "0129" && zwaveInfo?.prod == "6F01" && zwaveInfo?.model == "0001")
}

def isZipato() {
	(zwaveInfo?.mfr == "0131" && zwaveInfo?.prod == "0003" && zwaveInfo?.model == "1083")
}

def isUtilitech() {
	(zwaveInfo?.mfr == "0060" && zwaveInfo?.prod == "000C" && zwaveInfo?.model == "0001")
}

def isEverspring() {
	(zwaveInfo?.mfr == "0060" && zwaveInfo?.prod == "000C" && zwaveInfo?.model == "0002")
}
