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
 *  Z-Wave RGBW Light
 *
 *  Author: SmartThings
 *  Date: 2015-7-12
 */

metadata {
	definition (name: "RGBW Light", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", mnmn: "SmartThings", vid: "generic-rgbw-color-bulb") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"

		command "reset"

		fingerprint inClusters: "0x26,0x33"
		fingerprint inClusters: "0x33"
		fingerprint mfr: "0086", prod: "0103", model: "0079", deviceJoinName: "Aeotec LED Strip" //US
		fingerprint mfr: "0086", prod: "0003", model: "0079", deviceJoinName: "Aeotec LED Strip" //EU
		fingerprint mfr: "0086", prod: "0003", model: "0062", deviceJoinName: "Aeotec LED Bulb" //EU
		fingerprint mfr: "0086", prod: "0103", model: "0062", deviceJoinName: "Aeotec LED Bulb" //US
	}

	simulator {
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

			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
		}
	}

	standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
	}

	controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
		state "colorTemperature", action:"color temperature.setColorTemperature"
	}

	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "colorTempSliderControl", "reset"])
}

def updated() {
	log.debug "updated().."
	response(refresh())
}

def installed() {
	log.debug "installed()..."
	state.colorReceived = ["red": null, "green": null, "blue": null, "warmWhite": null, "coldWhite": null]
	sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
	sendEvent(name: "level", value: 100, unit: "%")
	response(refresh())
}

def parse(description) {
	def result = null
	if (description.startsWith("Err 106")) {
		state.sec = 0
	} else if (description != "updated") {
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
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchcolorv3.SwitchColorReport cmd) {
	log.debug "got SwitchColorReport: $cmd"
	if (!state.colorReceived)
		state.colorReceived = ["red": null, "green": null, "blue": null, "warmWhite": null, "coldWhite": null]
	state.colorReceived[cmd.colorComponent] = cmd.value
	def result = []
	def rgbNames = ["red", "green", "blue"]
	def tempNames = ["warmWhite", "coldWhite"]
	// Check if we got all the RGB color components
	if (rgbNames.every { state.colorReceived[it] != null }) {
		def colors = rgbNames.collect { state.colorReceived[it] }
		log.debug "colors: $colors"
		// Send the color as hex format
		def hexColor = "#" + colors.collect { Integer.toHexString(it).padLeft(2, "0") }.join("")
		result << createEvent(name: "color", value: hexColor)
		// Send the color as hue and saturation
		def hsv = rgbToHSV(*colors)
		result << createEvent(name: "hue", value: hsv.hue)
		result << createEvent(name: "saturation", value: hsv.saturation)
		// Reset the values
		rgbNames.collect { state.colorReceived[it] = null}
	}
	// Check if we got all the color temperature values
	if (tempNames.every { state.colorReceived[it] != null}) {
		def warmWhite = state.colorReceived["warmWhite"]
		def coldWhite = state.colorReceived["coldWhite"]
		log.debug "warmWhite: $warmWhite, coldWhite: $coldWhite"
		// When the device is first installed, warmWhite == coldWhite == 255
		//  so default to mid-range color temp.
		def colorTemp = COLOR_TEMP_MIN + (COLOR_TEMP_DIFF / 2)
		if (warmWhite != coldWhite)
			colorTemp = (COLOR_TEMP_MAX - (COLOR_TEMP_DIFF * warmWhite) / 255) as Integer
		result << createEvent(name: "colorTemperature", value: colorTemp)
		// Reset the values
		tempNames.collect { state.colorReceived[it] = null }
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

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	response(command(zwave.switchMultilevelV1.switchMultilevelGet()))
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		state.sec = 1
		def result = zwaveEvent(encapsulatedCommand)
		result = result.collect {
			if (it instanceof physicalgraph.device.HubAction && !it.toString().startsWith("9881")) {
				response(cmd.CMD + "00" + it.toString())
			} else {
				it
			}
		}
		result
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def linkText = device.label ?: device.name
	[linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

def buildOffOnEvent(cmd){
	[zwave.basicV1.basicSet(value: cmd), zwave.switchMultilevelV3.switchMultilevelGet()]
}

def on() {
	commands(buildOffOnEvent(0xFF), 3500)
}

def off() {
	commands(buildOffOnEvent(0x00), 3500)
}

def refresh() {
	commands([zwave.switchMultilevelV3.switchMultilevelGet()] + queryAllColors())
}

def ping() {
	log.debug "ping().."
	refresh()
}

def setLevel(level) {
	setLevel(level, 1)
}

def setLevel(level, duration) {
	log.debug "setLevel($level, $duration)"
	if(level > 99) level = 99
	commands([
		zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], (duration && duration < 12) ? (duration * 1000) : 3500)
}

def setSaturation(percent) {
	log.debug "setSaturation($percent)"
	setColor(saturation: percent)
}

def setHue(value) {
	log.debug "setHue($value)"
	setColor(hue: value)
}

def setColor(value) {
	log.debug "setColor($value)"
	def result = []
	if (value.hex) {
		def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		result << zwave.switchColorV3.switchColorSet(red:c[0], green:c[1], blue:c[2], warmWhite:0, coldWhite:0)
	} else {
		def rgb = huesatToRGB(value.hue, value.saturation)
		result << zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:0, coldWhite:0)
	}
	result += queryAllColors()

	commands(result)
}

private getCOLOR_TEMP_MAX() { 6500 }
private getCOLOR_TEMP_MIN() { 2700 }
private getCOLOR_TEMP_DIFF() { COLOR_TEMP_MAX - COLOR_TEMP_MIN }

def setColorTemperature(temp) {
	if(temp > COLOR_TEMP_MAX)
		temp = COLOR_TEMP_MAX
	else if(temp < COLOR_TEMP_MIN)
		temp = COLOR_TEMP_MIN
	log.debug "setColorTemperature($temp)"
	def warmValue = ((COLOR_TEMP_MAX - temp) / COLOR_TEMP_DIFF * 255) as Integer
	def coldValue = 255 - warmValue
	def cmds = [zwave.switchColorV3.switchColorSet(red: 0, green: 0, blue: 0, warmWhite: warmValue, coldWhite: coldValue)]
	cmds += queryAllColors()
	commands(cmds)
}

private queryAllColors() {
	def colors = ["red", "green", "blue", "warmWhite", "coldWhite"]
	colors.collect { zwave.switchColorV3.switchColorGet(colorComponent: it) }
}

def reset() {
	log.debug "reset()"
	setColorTemperature(COLOR_TEMP_MIN + (COLOR_TEMP_DIFF / 2))
}

private secEncap(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private command(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s") || state.sec == 1) {
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

def rgbToHSV(red, green, blue) {
	def hex = colorUtil.rgbToHex(red as int, green as int, blue as int)
	def hsv = colorUtil.hexToHsv(hex)
	return [hue: hsv[0], saturation: hsv[1], value: hsv[2]]
}

def huesatToRGB(hue, sat) {
	def color = colorUtil.hsvToHex(Math.round(hue) as int, Math.round(sat) as int)
	return colorUtil.hexToRgb(color)
}