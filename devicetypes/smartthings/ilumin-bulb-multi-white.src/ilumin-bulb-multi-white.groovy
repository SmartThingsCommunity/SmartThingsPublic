/**
 *  Copyright 2020 SmartThings
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
 *  ilumin Bulb Multi-White
 *
 *  Author: SmartThings
 *  Date: 2020-3-13
 */

metadata {
	definition (name: "ilumin Bulb Multi-White", namespace: "smartthings", author: "SmartThings",
				ocfDeviceType: "oic.d.light", mnmn: "SmartThings", vid: "generic-rgbw-color-bulb",
				runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Switch Level"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"


		fingerprint mfr: "0300", prod: "0003", model: "0004", deviceJoinName: "ilumin Tunable White Bulb"
        fingerprint mfr: "031E", prod: "0006", model: "0001", deviceJoinName: "ilumin Tunable White Bulb"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x5A,0x26,0x33,0x27,0x70,0x7A,0x73,0x98,0x7A"
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x98,0x86,0x85,0x59,0x72,0x73,0x26,0x33,0x70,0x27,0x5A,0x7A" // Secure
        fingerprint deviceId: "0x1101", inClusters: "0x5E,0x85,0x59,0x86,0x72,0x5A,0x26,0x33,0x27,0x70,0x73,0x98,0x7A"
	}

	simulator {
	}
    
    preferences {
        input name: "bulbMemory", type: "enum", title: "Power outage state", options: ["0":"Return to last ON state","1":"Return to last state (ON or OFF)"], defaultValue: "0"
    }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 1, height: 1, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState("on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
				attributeState("off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
				attributeState("turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
				attributeState("turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
			}

			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
	}

	controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
		state "colorTemperature", action:"color temperature.setColorTemperature"
	}

	main(["switch"])
	details(["switch", "levelSliderControl", "colorTempSliderControl"])
}

private getWARM_WHITE_CONFIG() { 0x51 }
private getCOLD_WHITE_CONFIG() { 0x52 }
private getWARM_WHITE() { "warmWhite" }
private getCOLD_WHITE() { "coldWhite" }
private getWHITE_NAMES() { [WARM_WHITE, COLD_WHITE] }

def updated() {
	log.debug "updated().."
	response(refresh() + configure())
}

def installed() {
	log.debug "installed()..."
	sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
	sendEvent(name: "level", value: 100, unit: "%")
	sendEvent(name: "colorTemperature", value: 2700)
	response(refresh() + configure())
}

def configure() {
    log.debug "configure()..."
    def cmds = []
	cmds << zwave.configurationV1.configurationSet([scaledConfigurationValue: bulbMemory?bulbMemory.toInteger():0, parameterNumber: 2, size:1])
	cmds << zwave.configurationV1.configurationGet([parameterNumber: 2])
    commands(cmds)
}

def parse(description) {
	def result = null
	if (description != "updated") {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd)
			log.debug("'$description' parsed to $result")
		} else {
			log.debug("Couldn't zwave.parse '$description'")
		}
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	unschedule(offlinePing)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchcolorv3.SwitchColorReport cmd) {
	log.debug "got SwitchColorReport: $cmd"
	def result = []
	if (cmd.value == 255) {
		def parameterNumber = (cmd.colorComponent == WARM_WHITE) ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
		result << response(command(zwave.configurationV2.configurationGet([parameterNumber: parameterNumber])))
	}
	result
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")]
	if (cmd.value) {
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value , unit: "%")
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "got ConfigurationReport: $cmd"
	def result = null
	if (cmd.parameterNumber == WARM_WHITE_CONFIG || cmd.parameterNumber == COLD_WHITE_CONFIG)
		result = createEvent(name: "colorTemperature", value: cmd.scaledConfigurationValue)
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

def buildOffOnEvent(cmd){
	[zwave.basicV1.basicSet(value: cmd), zwave.switchMultilevelV3.switchMultilevelGet()]
}

def on() {
	commands(buildOffOnEvent(0xFF), 5000)
}

def off() {
	commands(buildOffOnEvent(0x00), 5000)
}

def refresh() {
	commands([zwave.switchMultilevelV3.switchMultilevelGet()] + queryAllColors(), 500)
}

def ping() {
	log.debug "ping().."
	unschedule(offlinePing)
	runEvery30Minutes(offlinePing)
	command(zwave.switchMultilevelV3.switchMultilevelGet())
}

def offlinePing() {
	log.debug "offlinePing()..."
	sendHubCommand(new physicalgraph.device.HubAction(command(zwave.switchMultilevelV3.switchMultilevelGet())))
}

def setLevel(level) {
	setLevel(level, 1)
}

def setLevel(level, duration) {
	log.debug "setLevel($level, $duration)"
	if(level > 99) level = 99
	commands([
		zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration)
	], (duration && duration < 12) ? (duration * 1000 + 2000) : 3500)
}

def setColorTemperature(temp) {
	log.debug "setColorTemperature($temp)"
	def warmValue = temp < 5000 ? 255 : 0
	def coldValue = temp >= 5000 ? 255 : 0
	def parameterNumber = temp < 5000 ? WARM_WHITE_CONFIG : COLD_WHITE_CONFIG
	def results = []
	results << zwave.configurationV1.configurationSet([parameterNumber: parameterNumber, size: 2, scaledConfigurationValue: temp])
	results << zwave.switchColorV3.switchColorSet(warmWhite: warmValue, coldWhite: coldValue)
	if (device.currentValue("switch") != "on") {
		results << zwave.basicV1.basicSet(value: 0xFF)
		results << zwave.switchMultilevelV3.switchMultilevelGet()
	}
	commands(results) + "delay 5000" + commands(queryAllColors(), 500)
}

private queryAllColors() {
	WHITE_NAMES.collect { zwave.switchColorV3.switchColorGet(colorComponent: it) }
}

private secEncap(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private command(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) {
		secEncap(cmd)
	} else if (zwaveInfo.cc.contains("56")){
		crcEncap(cmd)
	} else {
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}
