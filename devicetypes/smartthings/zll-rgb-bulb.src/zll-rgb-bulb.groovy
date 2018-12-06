/**
 *  Copyright 2017 SmartThings
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
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "ZLL RGB Bulb", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true) {

		capability "Actuator"
		capability "Color Control"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"

		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E27 CWS opal 600lm", deviceJoinName: "TRADFRI bulb E27 CWS opal 600lm"
		fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, 1000", outClusters: "0005, 0019, 0020, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI bulb E26 CWS opal 600lm", deviceJoinName: "TRADFRI bulb E26 CWS opal 600lm"
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch"])
		details(["switch", "refresh"])
	}
}

//Globals
private getATTRIBUTE_HUE() { 0x0000 }
private getATTRIBUTE_SATURATION() { 0x0001 }
private getATTRIBUTE_X() { 0x0003 }
private getATTRIBUTE_Y() { 0x0004 }
private getATTRIBUTE_COLOR_CAPABILITIES() { 0x400A }
private getHUE_COMMAND() { 0x00 }
private getSATURATION_COMMAND() { 0x03 }
private getMOVE_TO_HUE_AND_SATURATION_COMMAND() { 0x06 }
private getMOVE_TO_COLOR_COMMAND() { 0x07 }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }

/**
 * Check if this device can support Hue and Saturation
 *
 * Right now this is a manufacturer based check. IKEA only supports CIE xyY
 */
private shouldUseHueSaturation() {
	return device.getDataValue("manufacturer") != "IKEA of Sweden"
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def finalResult = zigbee.getEvent(description)
	if (finalResult) {
		log.debug finalResult
		sendEvent(finalResult)
	} else {
		def zigbeeMap = zigbee.parseDescriptionAsMap(description)
		log.trace "zigbeeMap : $zigbeeMap"

		if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER) {
			if(zigbeeMap.attrInt == ATTRIBUTE_HUE && shouldUseHueSaturation()){ //Hue Attribute
				def hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 0xfe * 100)
				sendEvent(name: "hue", value: hueValue, displayed:false)
			} else if(zigbeeMap.attrInt == ATTRIBUTE_SATURATION && shouldUseHueSaturation()) { //Saturation Attribute
				def saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 0xfe * 100)
				sendEvent(name: "saturation", value: saturationValue, displayed:false)
			} else if(zigbeeMap.attrInt == ATTRIBUTE_X) { //X Attribute
				state.currentRawX = zigbee.convertHexToInt(zigbeeMap.value)
				log.debug "xValue = $state.currentRawX"
			} else if(zigbeeMap.attrInt == ATTRIBUTE_Y) { //Y Attribute
				state.currentRawY = zigbee.convertHexToInt(zigbeeMap.value)
				log.debug "yValue = $state.currentRawY"
			}

			// If the device is sending us this in response to us sending a command to set these,
			// then we likely already have the corresponding hue and sat attribute values stored.
			// However, in the event an external trigger gives us new values then we'll schedule
			// something to collect them that doesn't assume both values changes and then generate
			// the appropriate hue and sat (so we don't flood the event pipeline with garbage).
			if (!shouldUseHueSaturation() && state.currentRawX && state.currentRawY) {
				runIn(5, generateHsForXyData, [forceForLocallyExecuting: true])
			}
		} else {
			log.info "DID NOT PARSE MESSAGE for description : $description"
		}
	}
}

def generateHsForXyData() {
	def hsv = safeColorXy2Hsv(state.currentRawX, state.currentRawY)
	log.debug "x: ${state.currentRawX} y: ${state.currentRawY} hue: ${hsv.hue} saturation: ${hsv.saturation}"
	sendEvent(name: "hue", value: hsv.hue, displayed:false)
	sendEvent(name: "saturation", value: hsv.saturation, displayed:false)
}

def on() {
	zigbee.on() + ["delay 1500"] + zigbee.onOffRefresh()
}

def off() {
	zigbee.off() + ["delay 1500"] + zigbee.onOffRefresh()
}

def refresh() {
	refreshAttributes() + configureAttributes()
}

def poll() {
	configureHealthCheck()

	refreshAttributes()
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	configureAttributes() + refreshAttributes()
}

def ping() {
	refreshAttributes()
}

def healthPoll() {
	log.debug "healthPoll()"
	def cmds = refreshAttributes()
	cmds.each{ sendHubCommand(new physicalgraph.device.HubAction(it))}
}

def configureHealthCheck() {
	if (!state.hasConfiguredHealthCheck) {
		log.debug "Configuring Health Check, Reporting"
		unschedule("healthPoll", [forceForLocallyExecuting: true])
		runEvery5Minutes("healthPoll", [forceForLocallyExecuting: true])
		state.hasConfiguredHealthCheck = true
	}
}

def configureAttributes() {
	def commands = zigbee.onOffConfig() +
		zigbee.levelConfig()

	if (shouldUseHueSaturation()) {
		commands += zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE, DataType.UINT16, 1, 3600, 0x10)
		commands += zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION, DataType.UINT16, 1, 3600, 0x10)
	} else {
		commands += zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_X, DataType.UINT16, 1, 3600, 0x10)
		commands += zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_Y, DataType.UINT16, 1, 3600, 0x10)
	}

	commands
}

def refreshAttributes() {
	def commands = zigbee.onOffRefresh() + zigbee.levelRefresh()

	if (shouldUseHueSaturation()) {
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE)
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
	} else {
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_X)
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_Y)
	}

	log.debug "Refreshing $commands"
	commands
}

def updated() {
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	configureHealthCheck()
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	configureHealthCheck()
}

def setLevel(value) {
	zigbee.setLevel(value) + zigbee.onOffRefresh() + zigbee.levelRefresh() //adding refresh because of ZLL bulb not conforming to send-me-a-report
}

private getScaledHue(value) {
	zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

private getScaledSaturation(value) {
	zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
}

def setColor(value) {
	log.trace "setColor($value)"
	def commands = zigbee.on()

	if (shouldUseHueSaturation()) {
		commands += zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_HUE_AND_SATURATION_COMMAND,
			getScaledHue(value.hue), getScaledSaturation(value.saturation), "0000")
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE)
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
	} else {
		def xy = safeColorHsv2Xy(value.hue, value.saturation)

		log.debug "setColor: xy ($xy.x, $xy.y)"

		commands += zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_COMMAND,
			DataType.pack(xy.x, DataType.UINT16, 1), DataType.pack(xy.y, DataType.UINT16, 1), "0000")
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_X)
		commands += zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_Y)
	}

	commands
}

def setHue(value) {
	if (shouldUseHueSaturation()) {
		//payload-> hue value, direction (00-> shortest distance), transition time (1/10th second)
		zigbee.command(COLOR_CONTROL_CLUSTER, HUE_COMMAND, getScaledHue(value), "00", "0000") +
		zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE)
	} else {
		setColor([hue: value, saturation: device.currentValue("saturation")])
	}
}

def setSaturation(value) {
	if (shouldUseHueSaturation()) {
		//payload-> sat value, transition time
		zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, getScaledSaturation(value), "0000") +
		zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
	} else {
		setColor([hue: device.currentValue("hue"), saturation: value])
	}
}

/**
 * Below code from https://github.com/puzzle-star/SmartThings-IKEA-Tradfri-RGB/blob/master/ikea-tradfri-rgb.groovy
 *  Copyright 2017 Pedro Garcia
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

private minOfSet(first, ... rest) {
	def minVal = first
	for (next in rest) {
		if (next < minVal) {
			minVal = next
		}
	}

	minVal
}

private maxOfSet(first, ... rest) {
	def maxVal = first
	for (next in rest) {
		if (next > maxVal) {
			maxVal = next
		}
	}

	maxVal
}

private colorGammaAdjust(component) {
	return (component > 0.04045) ? Math.pow((component + 0.055) / (1.0 + 0.055), 2.4) : (component / 12.92)
}

private colorGammaRevert(component) {
	return (component <= 0.0031308) ? 12.92 * component : (1.0 + 0.055) * Math.pow(component, (1.0 / 2.4)) - 0.055
}

private colorXy2Rgb(x, y) {
	log.debug "colorXy2Rgb Color xy: ($x, $y)"

	def Y = 1
	def X = (Y / y) * x
	def Z = (Y / y) * (1.0 - x - y)

	log.debug "colorXy2Rgb Color XYZ: ($X, $Y, $Z)"

	// sRGB, Reference White D65
	/*def M = [
		[  3.2410032, -1.5373990, -0.4986159 ],
		[ -0.9692243,  1.8759300,  0.0415542 ],
		[  0.0556394, -0.2040112,  1.0571490 ]
	]*/
	def M = [
		[  3.2404542, -1.5371385, -0.4985314 ],
		[ -0.9692660,  1.8760108,  0.0415560 ],
		[  0.0556434, -0.2040259,  1.0572252 ]
	]

	def r = X * M[0][0] + Y * M[0][1] + Z * M[0][2]
	def g = X * M[1][0] + Y * M[1][1] + Z * M[1][2]
	def b = X * M[2][0] + Y * M[2][1] + Z * M[2][2]

	def maxRgb = maxOfSet(r, g, b)
	r = colorGammaRevert(r / maxRgb)
	g = colorGammaRevert(g / maxRgb)
	b = colorGammaRevert(b / maxRgb)

	log.debug "colorXy2Rgb Color RGB: ($r, $g, $b)"

	[red: r, green: g, blue: b]
}

private colorRgb2Xy(r, g, b) {
	log.debug "colorRgb2Xy Color RGB: ($r, $g, $b)"

	r = colorGammaAdjust(r)
	g = colorGammaAdjust(g)
	b = colorGammaAdjust(b)

	// sRGB, Reference White D65
	// D65	0.31271	0.32902
	//  R	0.64000 0.33000
	//  G	0.30000 0.60000
	//  B	0.15000 0.06000
	/*def M = [
		[  0.4123866,  0.3575915,  0.1804505 ],
		[  0.2126368,  0.7151830,  0.0721802 ],
		[  0.0193306,  0.1191972,  0.9503726 ]
	]*/
	def M = [
		[  0.4124564,  0.3575761,  0.1804375 ],
		[  0.2126729,  0.7151522,  0.0721750 ],
		[  0.0193339,  0.1191920,  0.9503041 ]
	]

	def X = r * M[0][0] + g * M[0][1] + b * M[0][2]
	def Y = r * M[1][0] + g * M[1][1] + b * M[1][2]
	def Z = r * M[2][0] + g * M[2][1] + b * M[2][2]

	log.debug "colorRgb2Xy Color XYZ: ($X, $Y, $Z)"

	def x = X / (X + Y + Z)
	def y = Y / (X + Y + Z)

	log.debug "colorRgb2Xy Color xy: ($x, $y)"

	[x: x, y: y]
}

private colorHsv2Rgb(h, s) {
	log.debug "colorHsv2Rgb Color HSV: ($h, $s, 1)"

	def r
	def g
	def b

	if (s == 0) {
		r = 1
		g = 1
		b = 1
	} else {
		def region = (6 * h).intValue()
		def remainder = 6 * h - region

		def p = 1 - s
		def q = 1 - s * remainder
		def t = 1 - s * (1 - remainder)

		if (region == 0) {
			r = 1
			g = t
			b = p
		} else if (region == 1) {
			r = q
			g = 1
			b = p
		} else if (region == 2) {
			r = p
			g = 1
			b = t
		} else if (region == 3) {
			r = p
			g = q
			b = 1
		} else if (region == 4) {
			r = t
			g = p
			b = 1
		} else {
			r = 1
			g = p
			b = q
		}
	}

	log.debug "colorHsv2Rgb Color RGB: ($r, $g, $b)"

	[red: r, green: g, blue: b]
}

private colorRgb2Hsv(r, g, b)
{
	log.debug "colorRgb2Hsv Color RGB: ($r, $g, $b)"

	def minRgb = minOfSet(r, g, b)
	def maxRgb = maxOfSet(r, g, b)
	def delta = maxRgb - minRgb

	def h
	def s
	def v = maxRgb

	if (delta == 0) {
		h = 0
		s = 0
	} else {
		s = delta / maxRgb
		if (r == maxRgb) { // between yellow & magenta
			h = (g - b) / delta
		} else if (g == maxRgb) { // between cyan & yellow
			h = 2 + (b - r) / delta
		} else { // between magenta & cyan
			h = 4 + (r - g) / delta
		}
		h /= 6

		if (h < 0) {
			h += 1
		}
	}

	log.debug "colorRgb2Hsv Color HSV: ($h, $s, $v)"

	return [hue: h, saturation: s, level: v]
}

def safeColorHsv2Xy(h, s) {
	def safeH = h != null ? h / 100 : 0
	def safeS = s != null ? s / 100 : 0
	def rgb = colorHsv2Rgb(safeH, safeS)

	def xy = colorRgb2Xy(rgb.red, rgb.green, rgb.blue)

	return [x: Math.round(xy.x * 65536).intValue(), y: Math.round(xy.y * 65536).intValue()]
}

def safeColorXy2Hsv(x, y) {
	def safeX = x != null ? x / 65536 : 0
	def safeY = y != null ? y / 65536 : 0
	def rgb = colorXy2Rgb(safeX, safeY)

	def hsv = colorRgb2Hsv(rgb.red, rgb.green, rgb.blue)

	return [hue: Math.round(hsv.hue * 100).intValue(), saturation: Math.round(hsv.saturation * 100).intValue()]
}
