/**
 *	Fibaro CO Sensor
 */
metadata {
	definition (name: "Fibaro CO Sensor ZW5", namespace: "FibarGroup", author: "Fibar Group", ocfDeviceType: "x.com.st.d.sensor.smoke") {
		capability "Carbon Monoxide Detector"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Configuration"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
		capability "Temperature Alarm"

		attribute "coLevel", "number"

		fingerprint mfr: "010F", prod: "1201", model: "1000", deviceJoinName: "Fibaro Carbon Monoxide Sensor"
		fingerprint mfr: "010F", prod: "1201", model: "1001", deviceJoinName: "Fibaro Carbon Monoxide Sensor"
		fingerprint mfr: "010F", prod: "1201", deviceJoinName: "Fibaro Carbon Monoxide Sensor"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"FGDW", type:"lighting", width:6, height:4) {
			tileAttribute("device.carbonMonoxide", key:"PRIMARY_CONTROL") {
				attributeState("clear", label:"clear", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/coSensor/device_co_1.png", backgroundColor:"#ffffff")
				attributeState("detected", label:"detected", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/coSensor/device_co_3.png", backgroundColor:"#e86d13")
				attributeState("tested", label:"tested", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/coSensor/device_co_2.png", backgroundColor:"#e86d13")
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			}
		}

		valueTile("coLevel", "device.coLevel", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "coLevel", label:'${currentValue}\nppm', unit:"ppm"
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
					backgroundColors:[
							[value: 31, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
		}

		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label:'${currentValue}%\n battery', unit:"%"
		}

		standardTile("temperatureAlarm", "device.temperatureAlarm", inactiveLabel: false, width: 3, height: 2, decoration: "flat") {
			state "cleared", label:'Clear', backgroundColor:"#ffffff" , icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/coSensor/heat_detector0.png"
			state "heat", label:'Overheat', backgroundColor:"#d04e00", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/coSensor/heat_detector1.png"
		}

		standardTile("tamper", "device.tamper", inactiveLabel: false, width: 3, height: 2, decoration: "flat") {
			state "clear", label:'', icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/coSensor/tamper_detector0.png"
			state "detected", label:'', icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/coSensor/tamper_detector100.png"
		}

		main "FGDW"
		details(["FGDW","coLevel","temperature","battery","temperatureAlarm","tamper"])
	}

	preferences {
		parameterMap().each {
			input (
					title: "${it.title}",
					description: it.descr,
					type: "paragraph",
					element: "paragraph"
			)

			def defVal = it.def as Integer
			def descrDefVal = it.options ? it.options.get(defVal) : defVal
			input (
					name: it.key,
					title: null,
					description: "$descrDefVal",
					type: it.type,
					options: it.options,
					range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
					defaultValue: it.def,
					required: false
			)
		}

		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

def installed() {
	sendEvent(name: "checkInterval", value: 12 * 60 * 60 + 8 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	logging("${device.displayName} - Executing updated()","info")

	if ( (settings.zwaveNotifications as Integer) >= 2 || !settings.zwaveNotifications) { //before any configuration change, settings have 'null' values
		sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)
	} else {
		sendEvent(name: "temperatureAlarm", value: null, displayed: false)
	}

	syncStart()
	state.lastUpdated = now()
}

def configure() {
	def cmds = []
	sendEvent(name: "coLevel", unit: "ppm", value: 0, displayed: true)
	sendEvent(name: "carbonMonoxide", value: "clear", displayed: "true")
	sendEvent(name: "tamper", value: "clear", displayed: "true")
	sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)
	// turn on tamper and temperature alarm reporting
	cmds << zwave.configurationV2.configurationSet(scaledConfigurationValue: 3, parameterNumber: 2, size: 1)
	// turn on acoustic signal on exceeding the temperature alarm
	cmds << zwave.configurationV2.configurationSet(scaledConfigurationValue: 2, parameterNumber: 4, size: 1)
	cmds << zwave.batteryV1.batteryGet()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
	cmds << zwave.wakeUpV1.wakeUpNoMoreInformation()
	encapSequence(cmds,1000)
}

private syncStart() {
	boolean syncNeeded = false
	parameterMap().each {
		if(settings."$it.key" != null || it.num == 54) {
			if (state."$it.key" == null) { state."$it.key" = [value: "$it.def", state: "synced"] }
			if (state."$it.key".value != (settings."$it.key" as Integer) || state."$it.key".state != "synced" ) {
				state."$it.key".value = (settings."$it.key" as Integer)
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}

	if ( syncNeeded ) {
		logging("${device.displayName} - sync needed.", "info")
		multiStatusEvent("Sync pending. Please wake up the device by pressing the Test button.", true)
	}
}

def syncNext() {
	logging("${device.displayName} - Executing syncNext()","info")
	def cmds = []
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"
			cmds << response(encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: state."$param.key".value, parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
			break
		}
	}
	if (cmds) {
		runIn(10, "syncCheck")
		sendHubCommand(cmds,1000)
	} else {
		runIn(1, "syncCheck")
	}
}

def syncCheck() {
	logging("${device.displayName} - Executing syncCheck()","info")
	def failed = []
	def incorrect = []
	def notSynced = []
	parameterMap().each {
		if (state."$it.key"?.state == "incorrect" ) {
			incorrect << it
		} else if ( state."$it.key"?.state == "failed" ) {
			failed << it
		} else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
			notSynced << it
		}
	}

	if (failed) {
		multiStatusEvent("Sync failed! Verify parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		multiStatusEvent("Sync mismatch! Verify parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		multiStatusEvent("Sync incomplete! Wake up the device again by pressing the tamper button.", true, true)
	} else {
		sendHubCommand(response(encap(zwave.wakeUpV1.wakeUpNoMoreInformation())))
		if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
	}
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logging("${device.displayName} woke up", "info")
	def cmds = []
	sendEvent(descriptionText: "$device.displayName woke up", isStateChange: true)

	cmds << zwave.batteryV1.batteryGet()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet()
	runIn(1, "syncNext")
	[response(encapSequence(cmds,1000))]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.state == "inProgress" ) {
			state."$param.key"?.state = "failed"
			break
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	logging("${device.displayName} - AlarmReport received, zwaveAlarmType: ${cmd.zwaveAlarmType}, zwaveAlarmEvent: ${cmd.zwaveAlarmEvent}", "info")
	def lastTime = location.timeZone ? new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone) : new Date().format("yyyy MMM dd EEE h:mm:ss")
	switch (cmd.zwaveAlarmType) {
		case 2:
			switch (cmd.zwaveAlarmEvent) {
				case 0:
					sendEvent(name: "carbonMonoxide", value: "clear");
					multiStatusEvent("CO Clear - $lastTime");
					break;
				case 2:
					sendEvent(name: "carbonMonoxide", value: "detected");
					multiStatusEvent("CO Detected - $lastTime");
					break;
				case 3:
					if ( cmd.numberOfEventParameters == 0 ) {
						sendEvent(name: "carbonMonoxide", value: "tested");
						multiStatusEvent("CO Tested - $lastTime");
					} else if (cmd.numberOfEventParameters == 1 && cmd.eventParameter == [1]) {
						sendEvent(name: "carbonMonoxide", value: "clear");
						multiStatusEvent("CO Test OK - $lastTime");
					}
					break;
			}
			break;
		case 7:
			sendEvent(name: "tamper", value: (cmd.zwaveAlarmEvent == 3)? "detected":"clear");
			if (cmd.zwaveAlarmEvent == 3) { multiStatusEvent("Tamper - $lastTime") }
			break;
		case 4:
			if (device.currentValue("temperatureAlarm")?.value != null) {
				switch (cmd.zwaveAlarmEvent) {
					case 0: sendEvent(name: "temperatureAlarm", value: "cleared"); break;
					case 2: sendEvent(name: "temperatureAlarm", value: "heat"); break;
				};
			};
			break;
		default: logging("${device.displayName} - Unknown zwaveAlarmType: ${cmd.zwaveAlarmType}","warn");
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logging("${device.displayName} - SensorMultilevelReport received, sensorType: ${cmd.sensorType}, scaledSensorValue: ${cmd.scaledSensorValue}", "info")
	switch (cmd.sensorType) {
		case 1:
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			sendEvent(name: "temperature", unit: getTemperatureScale(), value: convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), displayed: true)
			break
		case 40:
			sendEvent(name: "coLevel", unit: "ppm", value: cmd.scaledSensorValue, displayed: true)
			break
		default:
			logging("${device.displayName} - Unknown sensorType: ${cmd.sensorType}","warn")
			break
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel}", "info")
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	sendEvent(map)
}

def parse(String description) {
	def result = []
	logging("${device.displayName} - Parsing: ${description}")
	if (description.startsWith("Err 106")) {
		result = createEvent(
				descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
		)
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, cmdVersions())
		if (cmd) {
			logging("${device.displayName} - Parsed: ${cmd}")
			zwaveEvent(cmd)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract secure cmd from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Could not extract crc16 command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Unhandled: ${cmd.toString()}"
	[:]
}

private logging(text, type = "debug") {
	if (settings.logging == "true") {
		log."$type" text
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) {
		secEncap(cmd)
	} else if (zwaveInfo?.cc?.contains("56")){
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private Map cmdVersions() {
	[0x5E: 2, 0x59: 1, 0x73: 1, 0x80: 1, 0x22: 1, 0x56: 1, 0x31: 5, 0x98: 1, 0x7A: 3, 0x5A: 1, 0x85: 2, 0x84: 2, 0x71: 2, 0x70: 2, 0x8E: 2, 0x9C: 1, 0x86: 1, 0x72: 2]
}

private parameterMap() {[
		[key: "zwaveNotifications", num: 2, size: 1, type: "enum", options: [
				0: "Both actions disabled",
				1: "Tampering (opened casing)",
				2: "Exceeding the temperature",
				3: "Both actions enabled"
		],
		 def: "3", title: "Z-Wave notifications",
		 descr: "This parameter allows to set actions which result in sending notifications to the HUB"],
		[key: "highTempTreshold", num: 22, size: 1, type: "enum", options: [
				50: "120 °F / 50°C",
				55: "130 °F / 55°C",
				60: "140 °F / 60 °C",
				65: "150 °F / 65 °C",
				71: "160 °F / 71 °C",
				77: "170 °F / 77 °C",
				80: "176 °F / 80 °C"
		],
		 def: "55", title: "Threshold of exceeding the temperature",
		 descr: "This parameter defines the temperature level, which exceeding will result in sending actions set in paramater 2."]
]
}