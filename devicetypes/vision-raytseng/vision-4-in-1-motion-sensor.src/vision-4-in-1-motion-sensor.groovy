/**
 *  Vision 4-in-1 Motion Sensor
 *
 *  Author: Ray Tseng
 */
metadata {
	definition (name: "Vision 4-in-1 Motion Sensor", namespace: "vision-raytseng", author: "Ray Tseng", vid: "generic-motion-8", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Battery"
		capability "Motion Sensor"
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
		capability "Illuminance Measurement"
		capability "Tamper Alert"
		capability "Health Check"
		
		fingerprint mfr:"0109", prod:"2021", model:"2112", deviceJoinName: "Vision Multipurpose Sensor" // Raw description: zw:Ss2a type:0701 mfr:0109 prod:2021 model:2112 ver:32.32 zwv:7.13 lib:03 cc:5E,22,98,9F,6C,55 sec:85,59,80,70,5A,7A,87,8E,72,71,73,31,86,84
	}
	
	preferences {
		input title: "", description: "Vision 4-in-1 Motion Sensor", type: "paragraph", element: "paragraph", displayDuringSetup: true, required: true
		parameterMap().each {
			input name: it.name,
				title: it.title,
				description: it.description,
				type: it.type, 
				options: (it.type == "enum")? it.options: null,
				range: (it.type == "number")? it.options: null,
				defaultValue: it.default,
				required: true, displayDuringSetup: true
		}
		
		input title: "", description: "Wake up settings", 
			type: "paragraph", element: "paragraph", displayDuringSetup: true, required: true
		input name: wakeUpInfoMap.name,
			title: wakeUpInfoMap.title,
			description: wakeUpInfoMap.description,
			type: wakeUpInfoMap.type, range: wakeUpInfoMap.range,
			defaultValue: wakeUpInfoMap.default,
			required: true, displayDuringSetup: true
	}
}

def installed() {
	def cmds = []
	
	parameterMap().each {
		if (state."${it.name}" == null) { state."${it.name}" = [value: it.default, refresh: true] }
	}
	
	if (state."${wakeUpInfoMap.name}" == null) { state."${wakeUpInfoMap.name}" = [value: wakeUpInfoMap.default, refresh: true] }
	
	cmds += configure()
	if (cmds) {
		cmds += ["delay 5000", zwave.wakeUpV2.wakeUpNoMoreInformation().format()]
	}
	
	sendEvent(name: "motion", value: "inactive")
	sendEvent(name: "tamper", value: "clear")
	
	response(cmds)
}

def updated() {
	parameterMap().each {
		if (settings."${it.name}" != null && settings."${it.name}" != state."${it.name}".value) {
			state."${it.name}".value = settings."${it.name}"
			state."${it.name}".refresh = true
		}
	}
	
	if (settings."${wakeUpInfoMap.name}" != null && settings."${wakeUpInfoMap.name}" != state."${wakeUpInfoMap.name}".value) {
		state."${wakeUpInfoMap.name}".value = settings."${wakeUpInfoMap.name}"
		state."${wakeUpInfoMap.name}".refresh = true
	}
}

def configure() {
	def cmds = []
	def value
	
	if (device?.currentValue("temperature") == null) {
		def param = parameterMap().find { it.num == 1 }
		if (param != null) {
			value = param.enumMap.find { it.key == state."${param.name}".value }?.value
			cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01, scale: value?:0x00).format()
		}
	}
	if (device?.currentValue("illuminance") == null) {
		cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03, scale: 0x00).format()
	}
	if (device?.currentValue("humidity") == null) {
		cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05, scale: 0x00).format()
	}
	if (canReportBattery() || device?.currentValue("battery") == null) {
		cmds << zwave.batteryV1.batteryGet().format()
	}
	
	for (param in parameterMap()) {
		if (state."${param.name}".refresh == true) {
			value = (param.type == "enum")? param.enumMap.find { it.key == state."${param.name}".value }?.value: state."${param.name}".value
			if (value != null) {
				cmds << zwave.configurationV2.configurationSet(parameterNumber: param.num, defaultValue: false, scaledConfigurationValue: value).format()
				cmds << zwave.configurationV2.configurationGet(parameterNumber: param.num).format()
				if (param.num == 1) {
					cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01, scale: value?:0x00).format()
				}
			}
		}
	}
	
	if (state."${wakeUpInfoMap.name}".refresh == true) {
		 cmds << zwave.wakeUpV2.wakeUpIntervalSet(nodeid: zwaveHubNodeId, seconds: hour2Second(state."${wakeUpInfoMap.name}".value)).format()
		 cmds << zwave.wakeUpV2.wakeUpIntervalGet().format()
	}
	
	sendEvent(name: "checkInterval", value: (hour2Second(state."${wakeUpInfoMap.name}".value) + 2 * 60) * 2, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	return cmds ? delayBetween(cmds, 500) : []
}

def parameterMap() {[
		[num: 1,
			name: "TemperatureUnit", 
			title: "Temperature Unit [°C/°F]",
			description: "", 
			type: "enum", 
			options: ["°C", "°F"], 
			enumMap: ["°C": 0, "°F": 1], 
			default: "°C", 
			size: 1
		],
		[num: 2,
			name: "TempReportWhenChanged", 
			title: "Report when temperature difference is over the setting [unit is 0.1°C/°F]",
			description: "", 
			type: "number", 
			options: "1..50", 
			enumMap: [], 
			default: 30, 
			size: 1],
		[num: 3,
			name: "HumiReportWhenChanged", 
			title: "Report when humidity difference is over the setting [%]", 
			description: "",
			type: "number", 
			options: "1..50", 
			enumMap: [], 
			default: 20, 
			size: 1
		],
		[num: 4,
			name: "LightReportWhenChanged", 
			title: "Report when illuminance difference is over the setting [%](1% is approximately equal to 4.5 lux)", 
			description: "",
			type: "number", 
			options: "5..50", 
			enumMap: [], 
			default: 25, 
			size: 1
		],
		[num: 5,
			name: "MotionRestoreTime", 
			title: "Motion inactive report time [Minutes] after active", 
			description: "",
			type: "number", 
			options: "1..127", 
			enumMap: [], 
			default: 3, 
			size: 1
		],
		[num: 6,
			name: "MotionSensitivity", 
			title: "Motion active sensitivity", 
			description: "",
			type: "enum", 
			options: ["Highest", "Higher", "High", "Medium", "Low", "Lower", "Lowest"], 
			enumMap: ["Highest": 1, "Higher": 2, "High": 3, "Medium": 4, "Low": 5, "Lower": 6, "Lowest": 7], 
			default: "Medium", 
			size: 1
		],
		[num: 7,
			name: "LedDispMode", 
			title: "LED display mode", 
			description: "",
			type: "enum", 
			options: ["LED off when Temperature report/Motion active",
				"LED blink when Temperature report/Motion active",
				"LED blink when Motion active/LED off when Temperature report"], 
			enumMap: ["LED off when Temperature report/Motion active": 1,
				"LED blink when Temperature report/Motion active": 2,
				"LED blink when Motion active/LED off when Temperature report": 3], 
			default: "LED off when Temperature report/Motion active", 
			size: 1
		],
		[num: 8,
			name: "RetryTimes", 
			title: "Motion notification retry times", 
			description: "",
			type: "number", 
			options: "0..10", 
			enumMap: [], 
			default: 3, 
			size: 1
		]
	]
}

def getWakeUpInfoMap() {
	[
		name: "wakeUpInterval",
		title: "Wake up interval [Hours]",
		description: "",
		type: "number",
		range : "1..4660",
		default: 24
	]
}

private getCommandClassVersions() {
	[
		0x80: 1,
		0x70: 2,
		0X31: 5,
		0x71: 3,
		0x84: 2 
	]
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description, commandClassVersions)
	
	if (cmd) {
		result += zwaveEvent(cmd)
	} else {
		logDebug "Unable to parse description: ${description}"
	}
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def cmds = []
	
	cmds += configure()
	if (cmds) {
		cmds << "delay 5000"
	}
	cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
	
	return cmds ? response(cmds) : []
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	if (cmd.nodeid == zwaveHubNodeId) {
		if (state."${wakeUpInfoMap.name}".value == (cmd.seconds / 3600)) {
			state."${wakeUpInfoMap.name}".refresh = false
		}
	}
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%"]
	
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	
	state.lastBatteryReport = new Date().time
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def param = parameterMap().find { it.num == cmd.parameterNumber }
	
	if (param != null && param.size != null && param.size == cmd.size) {
		def value = (param.type == "enum")? param.enumMap.find { it.value == cmd.scaledConfigurationValue }?.key: cmd.scaledConfigurationValue
		if (value != null && value == state."${param.name}".value) {
			state."${param.name}".refresh = false
		}
	}
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	
	if (cmd.notificationType == 0x07) {
		if (cmd.eventParametersLength) {
			cmd.eventParameter.each {
				if (it == 0x03) {
					result = createEvent(name: "tamper", value: "clear") 
				} else if( it == 0x08) {
					result = createEvent(name: "motion", value: "inactive") 
				}
			}
		} else if (cmd.event == 0x03) {
			result = createEvent(name: "tamper", value: "detected") 
		} else if (cmd.event == 0x08) {
			result = createEvent(name: "motion", value: "active") 
		}
	}
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	
	switch (cmd.sensorType) {
		case 0x01:
			map.name = "temperature"
			map.value = cmd.scaledSensorValue
			map.unit = cmd.scale == 0 ? "C": "F"
			break
		case 0x03:
			map.name = "illuminance"
			map.value = getLuxFromPercentage(cmd.scaledSensorValue)
			map.unit = "lux"
			break
		case 0x05:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue
			map.unit = "%"
			break
		default:
			map.descriptionText = cmd.toString()
			break
	}
	
	createEvent(map)
}

def getBatteryReportIntervalSeconds() {
	return 8 * 3600
}

def canReportBattery() {
	def reportEveryMS = (getBatteryReportIntervalSeconds() * 1000)
	
	return (!state.lastBatteryReport || ((new Date().time) - state?.lastBatteryReport > reportEveryMS))
}

def hour2Second(hour) {
	return hour * 3600
}

private getLuxFromPercentage(percentageValue) {
	def multiplier = luxConversionData.find {
		percentageValue >= it.min && percentageValue <= it.max
	}?.multiplier ?: 5.312
	def luxValue = percentageValue * multiplier
	Math.round(luxValue)
}

private getLuxConversionData() {[
	[min: 0, max: 9.99, multiplier: 3.843],
	[min: 10, max: 19.99, multiplier: 5.231],
	[min: 20, max: 29.99, multiplier: 4.999],
	[min: 30, max: 39.99, multiplier: 4.981],
	[min: 40, max: 49.99, multiplier: 5.194],
	[min: 50, max: 59.99, multiplier: 6.016],
	[min: 60, max: 69.99, multiplier: 4.852],
	[min: 70, max: 79.99, multiplier: 4.836],
	[min: 80, max: 89.99, multiplier: 4.613],
	[min: 90, max: 100, multiplier: 4.5]
]}

def logDebug(msg) {
	log.debug "${msg}"
}

