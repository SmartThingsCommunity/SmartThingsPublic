// Express Controls EZMultiPli Multi-sensor 
// Motion Sensor - Temperature - Light level - 8 Color Indicator LED - Z-Wave Range Extender - Wall Powered
// driver for SmartThings
// The EZMultiPli is also known as the HSM200 from HomeSeer.com

import physicalgraph.zwave.commands.*

metadata {
	definition (name: "EZmultiPli", namespace: "DrZWave", author: "Eric Ryherd", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Actuator"
		capability "Sensor"
		capability "Motion Sensor"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Switch"
		capability "Color Control"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		fingerprint mfr: "001E", prod: "0004", model: "0001"
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "motion":	"command: 7105000000FF07, payload: 07"
		status "no motion":	"command: 7105000000FF07, payload: 00"

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV5.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 20) {
			status "luminance ${i} %": new physicalgraph.zwave.Zwave().sensorMultilevelV5.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
		}

	}

	tiles (scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL", icon: "st.Lighting.light18") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', icon:"st.switches.light.on", backgroundColor:"#00A0DC"
				attributeState "turningOff", label:'${name}', icon:"st.switches.light.off", backgroundColor:"#ffffff"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
			tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
				attributeState "statusText", label:'${currentValue}'
			}
		}

		standardTile("motion", "device.motion", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC"
			state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#cccccc"
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state "temperature", label:'${currentValue}°', icon:"",
				backgroundColors:[
					// Celsius
					[value: 0, color: "#153591"],
					[value: 7, color: "#1e9cbb"],
					[value: 15, color: "#90d2a7"],
					[value: 23, color: "#44b621"],
					[value: 28, color: "#f1d801"],
					[value: 35, color: "#d04e00"],
					[value: 37, color: "#bc2323"],
					// Fahrenheit
					[value: 40, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}

		valueTile("illuminance", "device.illuminance", width: 2, height: 2, inactiveLabel: false) {
			state "luminosity", label:'${currentValue}', unit:'${currentValue}', icon:"",
			backgroundColors:[
				//lux measurement values
				[value: 150, color: "#404040"],
				[value: 300, color: "#808080"],
				[value: 600, color: "#a0a0a0"],
				[value: 900, color: "#e0e0e0"]
			]
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main (["temperature", "motion", "switch"])
		details(["switch", "motion", "temperature", "illuminance", "refresh", "configure"])
	}

	preferences {
		section {
			input title: "Reporting Frequencies", description: "Enter values below to tune the reporting frequencies of the sensors in minutes.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "OnTime",  "number", title: "No Motion Interval", description: "N minutes lights stay on after no motion detected [0, 1-127]", range: "0..127", defaultValue: 2, displayDuringSetup: true, required: false
			input "LiteMin", "number", title: "Luminance Report Frequency", description: "Luminance report sent every N minutes [0-127]", range: "0..127", defaultValue: 6, displayDuringSetup: true, required: false
			input "TempMin", "number", title: "Temperature Report Frequency", description: "Temperature report sent every N minutes [0-127]", range: "0..127", defaultValue: 6, displayDuringSetup: true, required: false
		}
		section {
			input "TempAdj", "number", title: "Temperature Offset", description: "Adjust temperature up/down N tenths of a degree F [(-127)-(+128)]", range: "-127..128", defaultValue: 0, displayDuringSetup: true, required: false
		}
		section {
			input title: "Associated Devices", description: "The below preferences control associated node 2 devices.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "OnLevel", "number", title: "Dimmer Onlevel", description: "Dimmer OnLevel for associated node 2 lights [-1, 0, 1-99]", range: "-1..99", defaultValue: -1, displayDuringSetup: true, required: false
		}
	}
}

private getRED() { "red" }
private getGREEN() { "green" }
private getBLUE() { "blue" }
private getRGB_NAMES() { [RED, GREEN, BLUE] }

def setupHealthCheck() {
	def motionInterval = (OnTime != null ? OnTime : 2) as int
	def luminanceInterval = (LiteMin != null ? LiteMin : 6) as int
	def temperatureInterval = (TempMin != null ? TempMin : 6) as int
	def interval = Math.max(motionInterval, Math.max(luminanceInterval, temperatureInterval))

	// Device-Watch simply pings if no device events received for twice the maximum configured reporting interval + 2 minutes
	sendEvent(name: "checkInterval", value: 2 * interval * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def installed() {
	sendEvent(name: "motion", value: "inactive", displayed: false)
	state.colorReceived = [red: null, green: null, blue: null]
	setupHealthCheck()
}

def updated() {
	log.debug "updated() is being called"
	def cmds = configure()

	setupHealthCheck()

	if (cmds != []) response(cmds)
}

// Parse incoming device messages from device to generate events
def parse(description) {
	def result = []

	if (!device.currentValue("checkInterval")) {
		setupHealthCheck()
	}
	if (!state.colorReceived) {
		state.colorReceived = [red: null, green: null, blue: null]
	}

	if (description != "updated") {
		def cmd = zwave.parse(description, [0x31: 5]) // 0x31=SensorMultilevel which we force to be version 5
		if (cmd) {
			result = zwaveEvent(cmd)
		}

		def statusTextmsg = ""
		if (device.currentState("temperature") != null && device.currentState("illuminance") != null) {
			statusTextmsg = "${device.currentState("temperature").value}° - ${device.currentState("illuminance").value} lux"
			result << createEvent("name":"statusText", "value":statusTextmsg, displayed:false)
		}
	}
	log.debug "Parse returned ${result}"

	return result
}


// Event Generation
def zwaveEvent(sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	if (cmd.sensorType == sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_TEMPERATURE_VERSION_1) {
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
		map.unit = getTemperatureScale()
		map.name = "temperature"
		log.debug "Temperature report"
	} else if (cmd.sensorType == sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_LUMINANCE_VERSION_1) {
		map.value = cmd.scaledSensorValue.toInteger().toString()
		map.unit = "lux"
		map.name = "illuminance"
		log.debug "Luminance report"
	}
	return [createEvent(map)]
}

def zwaveEvent(configurationv2.ConfigurationReport cmd) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
	[]
}

def zwaveEvent(notificationv3.NotificationReport cmd) {
	def map = [:]
	if (cmd.notificationType == notificationv3.NotificationReport.NOTIFICATION_TYPE_BURGLAR) {
		if (cmd.event == 0x07 || cmd.event == 0x08) {
			map.name = "motion"
			map.value = "active"
			map.descriptionText = "$device.displayName motion detected"
			log.debug "motion recognized"
		} else if (cmd.event == 0) {
			map.name = "motion"
			map.value = "inactive"
			map.descriptionText = "$device.displayName no motion detected"
			log.debug "No motion recognized"
		}
	} 
	if (map.name != "motion") {
			log.debug "unmatched parameters for cmd: ${cmd.toString()}}"
	}
	return [createEvent(map)]
}

def zwaveEvent(basicv1.BasicReport cmd) {
	def result = []
	// The EZMultiPli sets the color back to #ffffff on "off" or at init, so update the ST device to reflect this.
	if (device.latestState("color") == null || (cmd.value == 0 && device.latestState("color").value != "#ffffff")) {
		result << createEvent(name: "color", value: "#ffffff")
		result << createEvent(name: "hue", value: 0)
		result << createEvent(name: "saturation", value: 0)
	}
	result << createEvent(name: "switch", value: cmd.value ? "on" : "off")

	result
}

def zwaveEvent(switchcolorv3.SwitchColorReport cmd) {
	log.debug "got SwitchColorReport: $cmd"
	state.colorReceived[cmd.colorComponent] = cmd.value

	def result = []
	// Check if we got all the RGB color components
	if (RGB_NAMES.every { state.colorReceived[it] != null }) {
		def colors = RGB_NAMES.collect { state.colorReceived[it] }
		log.debug "colors: $colors"
		// Send the color as hex format
		def hexColor = "#" + colors.collect { Integer.toHexString(it).padLeft(2, "0") }.join("")
		result << createEvent(name: "color", value: hexColor)
		// Send the color as hue and saturation
		def hsv = rgbToHSV(*colors)
		result << createEvent(name: "hue", value: hsv.hue)
		result << createEvent(name: "saturation", value: hsv.saturation)
		// Reset the values
		RGB_NAMES.collect { state.colorReceived[it] = null}
	}

	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Unhandled $cmd"
	[]
}

def on() {
	log.debug "Turning Light 'on'"
	commands([
		zwave.basicV1.basicSet(value: 0xFF),
		zwave.basicV1.basicGet()
	], 500)
}

def off() {
	log.debug "Turning Light 'off'"
	commands([
		zwave.basicV1.basicSet(value: 0x00),
		zwave.basicV1.basicGet()
	], 500)
}

def channelValue(channel) {
	channel >= 191 ? 255 : 0
}

def setColor(value) {
	log.debug "setColor() : ${value}"
	def hue
	def saturation
	def myred
	def mygreen
	def myblue
	def hexValue
	def cmds = []

	// The EZMultiPli has just on/off for each of the 3 channels RGB so convert the 0-255 value into 0 or 255.
	if (value.containsKey("hue") && value.containsKey("saturation")) {
		def level = (value.containsKey("level")) ? value.level : 100
		hue = value.hue as Integer
		saturation = value.saturation as Integer

		if (level == 1 && saturation > 20) {
			saturation = 100
		}

		if (level >= 1) {
			def rgb = huesatToRGB(hue, saturation)
			myred = channelValue(rgb[0])
			mygreen = channelValue(rgb[1])
			myblue = channelValue(rgb[2])
		}
	} else if (value.hex) {
		def rgb = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		myred = channelValue(rgb[0])
		mygreen = channelValue(rgb[1])
		myblue = channelValue(rgb[2])
	}
	// red, green, blue is not part of the capability definition, but it was possibly used by old SmartApps.
	// It should be safe to leave this in here for now.
	else if (value.containsKey("red") && value.containsKey("green") && value.containsKey("blue")) {
		myred = channelValue(value.red)
		mygreen = channelValue(value.green)
		myblue = channelValue(value.blue)
	} else {
		return
	}

	cmds << zwave.switchColorV3.switchColorSet(red: myred, green: mygreen, blue: myblue)
	cmds << zwave.basicV1.basicGet()

	commands(cmds + queryAllColors(), 100)
}

private queryAllColors() {
	def colors = RGB_NAMES
	colors.collect { zwave.switchColorV3.switchColorGet(colorComponent: it) }
}

def validateSetting(value, minVal, maxVal, defaultVal) {
	if (value == null) {
		value = defaultVal
	}
	def adjVal = value.toInteger()
	if (adjVal < minVal) { // bad value, set to default
		adjVal = defaultVal
	} else if (adjVal > maxVal) { // bad value, greater then MAX, set to MAX
		adjVal = maxVal
	}

	adjVal
}

def refresh() {
	def cmd = queryAllColors()
	cmd << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_TEMPERATURE_VERSION_1, scale: 1)
	cmd << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: sensormultilevelv5.SensorMultilevelReport.SENSOR_TYPE_LUMINANCE_VERSION_1, scale: 1)
	cmd << zwave.basicV1.basicGet()
	commands(cmd, 1000)
}

def ping() {
	log.debug "ping()"
	refresh()
}

def configure() {
	log.debug "OnTime=${settings.OnTime} OnLevel=${settings.OnLevel} TempAdj=${settings.TempAdj}"
	def cmds = commands([
		zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: validateSetting(settings.OnTime, 0, 127, 2)),
		zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: validateSetting(settings.OnLevel, -1, 99, -1)),
		zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: validateSetting(settings.LiteMin, 0, 127, 6)),
		zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: validateSetting(settings.TempMin, 0, 127, 6)),
		zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, scaledConfigurationValue: validateSetting(settings.TempAdj, -127, 128, 0)),
		zwave.configurationV1.configurationGet(parameterNumber: 1),
		zwave.configurationV1.configurationGet(parameterNumber: 2),
		zwave.configurationV1.configurationGet(parameterNumber: 3),
		zwave.configurationV1.configurationGet(parameterNumber: 4),
		zwave.configurationV1.configurationGet(parameterNumber: 5)
	], 100)

	cmds + refresh()
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
	} else if (zwaveInfo.cc.contains("56")) {
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
