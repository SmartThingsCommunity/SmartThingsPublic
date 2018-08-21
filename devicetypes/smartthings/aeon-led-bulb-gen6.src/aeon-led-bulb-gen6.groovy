/**
 *  Copyright 2018 Samsung
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
 *  
 *
 *  Author: SmartThings
 *  Date: 2018-8-20
 */

metadata {
	definition (name: "Aeon LED Bulb Gen 6", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", mnmn: "SmartThings", vid: "generic-rgbw-color-bulb") {
		capability "Switch Level"
		capability "Color Control"
		capability "Color Temperature"
		capability "Switch"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"

		command "reset"

		fingerprint inClusters: "0x26,0x33,0x98"
		fingerprint deviceId: "0x11", inClusters: "0x98,0x33"
		fingerprint deviceId: "0x1102", inClusters: "0x98"
		fingerprint mfr: "0371", prod: "0103", model: "0002", deviceJoinName: "Aeotec LED Bulb - Gen 6" //US
		fingerprint mfr: "0371", prod: "0003", model: "0002", deviceJoinName: "Aeotec LED Bulb - Gen 6" //EU
        fingerprint mfr: "0086", prod: "0103", model: "0079", deviceJoinName: "Aeotec LED Strip" //US
		fingerprint mfr: "0086", prod: "0003", model: "0079", deviceJoinName: "Aeotec LED Strip" //EU
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

	standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", height: 2, width: 2,) {
		state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
	}
	standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", height: 2, width: 2,) {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
	}
    
	controlTile("colorTempControl", "device.colorTemperature", "slider", height: 2, width: 2, inactiveLabel: false) {
		state "colorTemperature", action:"setColorTemperature"
	}
    
	main(["switch"])
	details(["switch", "levelSliderControl", "rgbSelector", "colorTempControl", "refresh", "reset"])
}

def updated() {
	log.debug "updated().."
	response(refresh())
}

def installed() {
	log.debug "installed()..."
	sendEvent(name: "checkInterval", value: 500, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def parse(description) {
	def result = null
    log.debug "Parsing"
	if (description.startsWith("Err 106")) {
		state.sec = 0
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 3, 0x70: 1, 0x33:3])
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

private dimmerEvents(physicalgraph.zwave.Command cmd) {
    log.debug "Dimmer"
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
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x84: 1])
    
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

def on() {
	log.debug zwaveInfo
	encapSequence([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], 1000)
}

def off() {
	log.debug zwaveInfo
	encapSequence([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], 1000)
}

def refresh() {
	encapSequence([
		zwave.switchMultilevelV3.switchMultilevelGet(),
	], 1000)
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
	encapSequence([
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
	def result = []
	log.debug "setColor($value)"
	if (value.hex) {
		def c = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		result << zwave.switchColorV3.switchColorSet(red:c[0], green:c[1], blue:c[2], warmWhite:0, coldWhite:0)
	} else {
		def hue = value.hue ?: device.currentValue("hue")
		def saturation = value.saturation ?: device.currentValue("saturation")
		if(hue == null) hue = 13
		if(saturation == null) saturation = 13
		def rgb = huesatToRGB(hue, saturation)
		result << zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite:0, coldWhite:0)
	}

	if(value.hue) sendEvent(name: "hue", value: value.hue)
	if(value.hex) sendEvent(name: "color", value: value.hex)
	if(value.switch) sendEvent(name: "switch", value: value.switch)
	if(value.saturation) sendEvent(name: "saturation", value: value.saturation)

	encapSequence(result)
}

def setColorTemperature(percent) {
	log.debug "setColorTemperature($percent)"
	if(percent > 99) percent = 99
	int warmValue = percent * 255 / 99
    sendEvent(name: "colorTemperature", value: percent)
    encapSequence([
        zwave.switchColorV3.switchColorSet(red:0, green:0, blue:0, warmWhite:warmValue, coldWhite:(255 - warmValue)),
        zwave.switchMultilevelV3.switchMultilevelGet()
    ], 2000)
}

def reset() {
	log.debug "reset()"
	sendEvent(name: "color", value: "#ffffff")
	setColorTemperature(0)
}

private secEncap(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
	encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
	encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
	encap(encapMap.cmd, encapMap.ep)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) {
        log.debug "secEncap"	
		secEncap(cmd)
	} else if (zwaveInfo.cc.contains("56")){
        log.debug "crcEncap"	
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
	delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec != 0) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

def rgbToHSV(red, green, blue) {
	float r = red / 255f
	float g = green / 255f
	float b = blue / 255f
	float max = [r, g, b].max()
	float delta = max - [r, g, b].min()
	def hue = 13
	def saturation = 0
	if (max && delta) {
		saturation = 100 * delta / max
		if (r == max) {
			hue = ((g - b) / delta) * 100 / 6
		} else if (g == max) {
			hue = (2 + (b - r) / delta) * 100 / 6
		} else {
			hue = (4 + (r - g) / delta) * 100 / 6
		}
	}
	[hue: hue, saturation: saturation, value: max * 100]
}

def huesatToRGB(hue, sat) {
	while(hue >= 100) hue -= 100
	int h = (int)(hue / 100 * 6)
	float f = hue / 100 * 6 - h
	int p = Math.round(255 * (1 - (sat / 100)))
	int q = Math.round(255 * (1 - (sat / 100) * f))
	int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
	switch (h) {
		case 0: return [255, t, p]
		case 1: return [q, 255, p]
		case 2: return [p, 255, t]
		case 3: return [p, q, 255]
		case 4: return [t, p, 255]
		case 5: return [255, p, q]
	}
}