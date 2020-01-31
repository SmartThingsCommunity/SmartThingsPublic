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
	definition(name: "Z-Wave Dimmer Switch Generic", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Z-Wave") {
		capability "Switch Level"
		capability "Actuator"
		capability "Health Check"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Light"

		fingerprint inClusters: "0x26", deviceJoinName: "Z-Wave Dimmer"
		fingerprint mfr: "001D", prod: "1902", deviceJoinName: "Z-Wave Dimmer"
		fingerprint mfr: "001D", prod: "3301", model: "0001", deviceJoinName: "Leviton Dimmer Switch"
		fingerprint mfr: "001D", prod: "3201", model: "0001", deviceJoinName: "Leviton Dimmer Switch"
		fingerprint mfr: "001D", prod: "1B03", model: "0334", deviceJoinName: "Leviton Universal Dimmer"
		fingerprint mfr: "011A", prod: "0102", model: "0201", deviceJoinName: "Enerwave In-Wall Dimmer"
		fingerprint mfr: "001D", prod: "0602", model: "0334", deviceJoinName: "Leviton Magnetic Low Voltage Dimmer"
		fingerprint mfr: "001D", prod: "0401", model: "0334", deviceJoinName: "Leviton 600W Incandescent Dimmer"
		fingerprint mfr: "0111", prod: "8200", model: "0200", deviceJoinName: "Remotec Technology Plug-In Dimmer", ocfDeviceType: "oic.d.smartplug"
		fingerprint mfr: "1104", prod: "001D", model: "0501", deviceJoinName: "Leviton 1000W Incandescant Dimmer"
		fingerprint mfr: "0039", prod: "5044", model: "3033", deviceJoinName: "Honeywell Z-Wave Plug-in Dimmer (Dual Outlet)", ocfDeviceType: "oic.d.smartplug"
		fingerprint mfr: "0039", prod: "5044", model: "3038", deviceJoinName: "Honeywell Z-Wave Plug-in Dimmer", ocfDeviceType: "oic.d.smartplug"
		fingerprint mfr: "0039", prod: "4944", model: "3038", deviceJoinName: "Honeywell Z-Wave In-Wall Smart Dimmer"
		fingerprint mfr: "0039", prod: "4944", model: "3130", deviceJoinName: "Honeywell Z-Wave In-Wall Smart Toggle Dimmer"
		fingerprint mfr: "001A", prod: "4449", model: "0101", deviceJoinName: "Eaton RF Master Dimmer"
		fingerprint mfr: "001A", prod: "4449", model: "0003", deviceJoinName: "Eaton RF Dimming Plug-In Module", ocfDeviceType: "oic.d.smartplug"
		fingerprint mfr: "014F", prod: "5744", model: "3530", deviceJoinName: "GoControl In-Wall Dimmer"
		fingerprint mfr: "0307", prod: "4447", model: "3034", deviceJoinName: "Satco In-Wall Dimmer"
		//zw:L type:1101 mfr:0184 prod:4744 model:3032 ver:5.07 zwv:3.95 lib:03 cc:5E,86,72,5A,85,59,73,26,27,70,7A role:05 ff:8600 ui:8600
		fingerprint mfr: "0184", prod: "4744", model: "3032", deviceJoinName: "Satco Plug-In Dimmer", ocfDeviceType: "oic.d.smartplug"
		fingerprint mfr: "0330", prod: "0201", model: "D002", deviceJoinName: "RGBgenie ZW-1001 Z-Wave Dimmer"
		fingerprint mfr: "027A", prod: "B112", model: "1F1C", deviceJoinName: "Zooz ZEN22 Dimmer"
		fingerprint mfr: "027A", prod: "A000", model: "A002", deviceJoinName: "Zooz ZEN27 Dimmer"
		fingerprint mfr: "027A", prod: "B112", model: "261C", deviceJoinName: "Zooz ZEN24 Dimmer"
		fingerprint mfr: "0300", prod: "0003", model: "0005", deviceJoinName: "ilumin Dimmable Bulb", ocfDeviceType: "oic.d.light"
		fingerprint mfr: "0312", prod: "FF00", model: "FF04", deviceJoinName: "Minoston Smart Dimmer Switch"
		fingerprint mfr: "0312", prod: "FF00", model: "FF02", deviceJoinName: "Minoston Toggle Dimmer Switch"
		fingerprint mfr: "0312", prod: "AA00", model: "AA02", deviceJoinName: "Evalogik Smart Dimmer Switch"
		fingerprint mfr: "0312", prod: "C000", model: "C002", deviceJoinName: "Evalogik Smart Plug Dimmer"
	}

	simulator {
		status "on": "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label: '${currentValue} %', unit: "%", backgroundColor: "#ffffff"
		}

		main(["switch"])
		details(["switch", "level", "refresh"])

	}
}

def installed() {
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	def commands = refresh()
	if (zwaveInfo?.mfr?.equals("001A")) {
		commands << "delay 100"
		//for Eaton dimmers parameter 7 is ramp time. We set it to 1s for devices to work correctly with local execution
		commands << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 7, size: 1).format()
	} else if (isHoneywellDimmer()) {
		//Set ramp time to 1s for this device to turn off dimmer correctly when current level is over 66.
		commands << "delay 100"
		//Parameter 7 - z-wave ramp up/down step size, Parameter 8 - z-wave step interval equals configurationValue times 10 ms
		commands << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 7, size: 1).format()
		commands << "delay 200"
		commands << zwave.configurationV1.configurationSet(configurationValue: [0, 1], parameterNumber: 8, size: 2).format()
		commands << "delay 200"
		//Parameter 7 - manual operation ramp up/down step size, Parameter 8 - z-wave manual operation interval equals configurationValue times 10 ms
		commands << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 9, size: 1).format()
		commands << "delay 200"
		commands << zwave.configurationV1.configurationSet(configurationValue: [0, 1], parameterNumber: 10, size: 2).format()
	}
	response(commands)
}

def updated() {
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x26: 1,  // SwitchMultilevel
		0x56: 1,  // Crc16Encap
	]
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   $cmd.manufacturerId"
	log.debug "manufacturerName: $cmd.manufacturerName"
	log.debug "productId:        $cmd.productId"
	log.debug "productTypeId:    $cmd.productTypeId"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name: "switch", value: "on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
	], 5000)
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchMultilevelV1.switchMultilevelGet().format()
	], 5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	delayBetween([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration * 1000) + 2000 : (Math.round(duration / 60) * 60 * 1000) + 2000
	delayBetween([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				  zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	refresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands, 100)
}

def isHoneywellDimmer() {
	zwaveInfo?.mfr?.equals("0039") && (
		(zwaveInfo?.prod?.equals("5044") && zwaveInfo?.model?.equals("3033")) ||
			(zwaveInfo?.prod?.equals("5044") && zwaveInfo?.model?.equals("3038")) ||
			(zwaveInfo?.prod?.equals("4944") && zwaveInfo?.model?.equals("3038")) ||
			(zwaveInfo?.prod?.equals("4944") && zwaveInfo?.model?.equals("3130"))
	)
}