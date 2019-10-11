/**
 *  Copyright 2019 SmartThings
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
	definition(name: "Z-Wave Siren Tamper", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Z-Wave", mnmn: "SmartThings", vid: "generic-siren-12") {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Switch"
		capability "Health Check"

		fingerprint mfr: "0060", prod: "000C", model: "0002", deviceJoinName: "Everspring Outdoor Solar Siren"
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

		main "alarm"
		details(["alarm", "off", "refresh", "battery", "tamper"])
	}
}

def installed() {
	log.debug "installed()"
	sendEvent(name: "tamper", value: "clear")
	initialize()
}

def initialize() {
	log.debug "initialize()"
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, isStateChanged: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	def cmds = []
	cmds << secure(zwave.batteryV1.batteryGet())

	cmds
}

def configure() {
	log.debug "config"
	refresh()
}

def on() {
	log.debug "sending on"
	def cmds = []
	cmds << secure(zwave.basicV1.basicSet(value: 0xFF))
	cmds << "delay 3000"
	cmds << secure(zwave.basicV1.basicGet())

	return cmds
}

def off() {
	log.debug "sending off"
	//Everspring Siren doesn't send info when tamper is clear. So this is event to prevent infinit detected tamper.
	sendEvent(name: "tamper", value: "clear")
	def cmds = []
	cmds << secure(zwave.basicV1.basicSet(value: 0x00))
	cmds << "delay 3000"
	cmds << secure(zwave.basicV1.basicGet())

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

	return delayBetween(cmds, 2000)
}

def parse(String description) {
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
	log.debug "Parse returned ${result}"

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

	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def checkVal
	if (checkVal != null) {
		state.configured = (checkVal == cmd.scaledConfigurationValue)
	} else {
		state.configured = true
	}
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
	} else {
		alarmValue = "both"
	}
	result << createEvent([name: "switch", value: switchValue, displayed: true])
	result << createEvent([name: "alarm", value: alarmValue, displayed: true])
	result
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def isActive = false
	def result = []
	if (cmd.notificationType == 7)  {
		switch (cmd.event) {
			case 0x03:
				result << createEvent([name: "tamper", value: "detected"])
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

