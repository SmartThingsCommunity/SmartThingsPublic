/**
 *  Fibaro Door/Window Sensor 2
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Fibaro Door/Window Sensor 2", namespace: "fibargroup", author: "Fibar Group S.A.") {	
		capability "Contact Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Temperature Alarm"
		capability "Configuration"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"

		attribute "multiStatus", "string"

		fingerprint mfr: "010F", prod: "0702", deviceJoinName: "Fibaro Open/Closed Sensor"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"FGDW", type:"lighting", width:6, height:4) {
			tileAttribute("device.contact", key:"PRIMARY_CONTROL") {
				attributeState("open", label:"open", icon:"st.contact.contact.open", backgroundColor:"#e86d13")
				attributeState("closed", label:"closed", icon:"st.contact.contact.closed", backgroundColor:"#00a0dc")
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			}
		}
		
		valueTile("tamper", "device.tamper", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "detected", label:'tampered'
			state "clear", label:'tamper clear'
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
		
		standardTile("temperatureAlarm", "device.temperatureAlarm", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", label: "No temp. alarm", backgroundColor:"#ffffff"
			state "cleared", label:'', backgroundColor:"#ffffff", icon: "st.alarm.temperature.normal"
			state "freeze", label:'freeze', backgroundColor:"#1e9cbb", icon: "st.alarm.temperature.freeze"
			state "heat", label:'heat', backgroundColor:"#d04e00", icon: "st.alarm.temperature.overheat"
		}

		main "FGDW"
		details(["FGDW","tamper","temperature","battery","temperatureAlarm"])
	}


	preferences {
		input (
			title: "Wake up interval",
			description: "How often should your device automatically sync with the HUB. The lower the value, the shorter the battery life.\n0 or 1-18 (in hours)",
			type: "paragraph",
			element: "paragraph"
		)
		
		input ( 
			name: "wakeUpInterval", 
			title: null, 
			type: "number", 
			range: "0..18", 
			defaultValue: 6, 
			required: false 
		)
		
		parameterMap().each {
			input (
				title: "${it.num}. ${it.title}",
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
		
		input ( name: "logging", title: "Logging", type: "boolean", defaultValue: true, required: false )
	}
}

def installed() {
	state.logging = true
	// Initial states for OCF compatibility
	sendEvent(name: "tamper", value: "clear", displayed: false)
	sendEvent(name: "contact", value: "open", displayed: false)
	sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)
	sendEvent(name: "checkInterval", value: 21600 * 4 + 120, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	if (settings.logging) {
		state.logging = settings.logging
	}
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) {
		return
	}
	logging("${device.displayName} - Executing updated()","debug")

	def tempAlarmMap = ["clear": "cleared", "overheat": "heat", "underheat": "freeze"]
	// Convert old device specific temperatureAlarm event to standard Temperature Alarm capability event
	def temperatureAlarmCurrentValue = device.currentValue("temperatureAlarm")
	if (tempAlarmMap.containsKey(temperatureAlarmCurrentValue)) {
		sendEvent(name: "temperatureAlarm", value: tempAlarmMap[temperatureAlarmCurrentValue], displayed: true)
	}

	def currentTemperature = device.currentValue("temperature")
	def alarmCleared = device.currentValue("temperatureAlarm") == "cleared"
	def alarmFreeze = device.currentValue("temperatureAlarm") == "freeze"
	def alarmHeat = device.currentValue("temperatureAlarm") == "heat"
	def temperatureHigh = (settings.temperatureHigh ? new BigDecimal(settings.temperatureHigh) * 0.1 : null)
	def temperatureLow =  (settings.temperatureLow  ? new BigDecimal(settings.temperatureLow)  * 0.1 : null)
	if (!alarmCleared) {
		if ((temperatureHigh != null && (currentTemperature < temperatureHigh) && !alarmFreeze) ||
			(temperatureLow != null && (currentTemperature > temperatureLow) && !alarmHeat)) {
			sendEvent(name: "temperatureAlarm", value: "cleared")
			}
	}

	syncStart()
	state.lastUpdated = now()
}

def configure() {
	def cmds = []
	cmds << zwave.batteryV1.batteryGet()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
	encapSequence(cmds,1000)
}

private syncStart() {
	boolean syncNeeded = false
	Integer settingValue = null
	parameterMap().each {
		if(settings."$it.key" != null || it.num == 54) {
			if (state."$it.key" == null) {
				state."$it.key" = [value: null, state: "synced"]
			} 
			if ( (it.num as Integer) == 54 ) { 
				settingValue = (((settings."temperatureHigh" as Integer) == 0) ? 0 : 1) + (((settings."temperatureLow" as Integer) == 0) ? 0 : 2)
			} else if ( (it.num as Integer) in [55,56] ) { 
				settingValue = (((settings."$it.key" as Integer) == 0) ? state."$it.key".value : settings."$it.key") as Integer
			} else {
				settingValue = settings."$it.key" as Integer
			}
			if (state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
				state."$it.key".value = settingValue
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
	
	if(settings.wakeUpInterval != null) {
		if (state.wakeUpInterval == null) {
			state.wakeUpInterval = [value: null, state: "synced"]
		} 
		if (state.wakeUpInterval.value != ((settings.wakeUpInterval as Integer) * 3600)) { 
			sendEvent(name: "checkInterval", value: (settings.wakeUpInterval as Integer) * 3600 * 4 + 120, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
			state.wakeUpInterval.value = ((settings.wakeUpInterval as Integer) * 3600)
			state.wakeUpInterval.state = "notSynced"
			syncNeeded = true
		}
	}
	
	if ( syncNeeded ) { 
		logging("${device.displayName} - sync needed.", "debug")
		multiStatusEvent("Sync pending. Please wake up the device by pressing the tamper button.", true)
	}
}

def syncNext() {
	logging("${device.displayName} - Executing syncNext()","debug")
	def cmds = []
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"
			cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
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
	logging("${device.displayName} - Executing syncCheck()","debug")
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
		if (device.currentValue("multiStatus")?.contains("Sync")) {
			multiStatusEvent("Sync OK.", true, true)
		}
	}
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logging("${device.displayName} woke up", "debug")
	def cmds = []
	sendEvent(descriptionText: "$device.displayName woke up", isStateChange: true)
	if ( state.wakeUpInterval?.state == "notSynced" && state.wakeUpInterval?.value != null ) {
		cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: state.wakeUpInterval.value as Integer, nodeid: zwaveHubNodeId)
		state.wakeUpInterval.state = "synced"
	}
	cmds << zwave.batteryV1.batteryGet()
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
	runIn(1, "syncNext")
	[response(encapSequence(cmds,1000))]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "debug")
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
	def map = [:]
	logging("${device.displayName} - AlarmReport received, zwaveAlarmType: ${cmd.zwaveAlarmType}, zwaveAlarmEvent: ${cmd.zwaveAlarmEvent}", "debug")
	switch (cmd.zwaveAlarmType) {
		case 6:
			map.name = "contact"
			switch (cmd.zwaveAlarmEvent) {
				case 22:
					map.value = "open"
					map.descriptionText = "${device.displayName} is open"
					break
				case 23:
					map.value = "closed"
					map.descriptionText = "${device.displayName} is closed"
					break
			}
			break
		case 7:
			map.name = "tamper"
			switch (cmd.zwaveAlarmEvent) {
				case 0:
					map.value = "clear"
					map.descriptionText = "Tamper alert cleared"
					break
				case 3:
					map.value = "detected"
					map.descriptionText = "Tamper alert: sensor removed or covering opened"
					break
			}
			break
		case 4:
			if (device.currentValue("temperatureAlarm")?.value != null) {
				map.name = "temperatureAlarm"
				switch (cmd.zwaveAlarmEvent) {
					case 0:
						map.value = "cleared"
						map.descriptionText = "Temperature alert cleared"
						break
					case 2:
						map.value = "heat"
						map.descriptionText = "Temperature alert: overheating detected"
						break
					case 6:
						map.value = "freeze"
						map.descriptionText = "Temperature alert: underheating detected"
						break
				}
			}
			break
		default:
			logging("${device.displayName} - Unknown zwaveAlarmType: ${cmd.zwaveAlarmType}","warn")
			break
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	logging("${device.displayName} - SensorMultilevelReport received, sensorType: ${cmd.sensorType}, scaledSensorValue: ${cmd.scaledSensorValue}", "debug")
	switch (cmd.sensorType) {
		case 1:
			def cmdScale = cmd.scale == 1 ? "F" : "C"

			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			map.displayed = true
			break
		default: 
			logging("${device.displayName} - Unknown sensorType: ${cmd.sensorType}","warn")
			break
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel}", "debug")
	sendEvent(name: "battery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logging("Catchall reached for cmd: $cmd")
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

private logging(text, type = "debug") {
	if (state.logging == true) {
		log."$type" text
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","debug")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","debug")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format() 
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) { 
		secEncap(cmd)
	} else if (zwaveInfo?.cc?.contains("56")){
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd","debug")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
	def result = []
	size.times { 
		result = result.plus(0, (value & 0xFF) as Short)
		value = (value >> 8)
	}
	return result
}

private Map cmdVersions() {
	[0x5E: 2, 0x59: 1, 0x22: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x31: 5, 0x85: 2, 0x70: 2, 0x5A: 1, 0x72: 2, 0x8E: 2, 0x71: 2, 0x86: 1, 0x84: 2]
}

private parameterMap() {[
	[key: "doorState", num: 1, size: 1, type: "enum", options: [0: "Closed when magnet near", 1: "Opened when magnet near"], def: "0", title: "Door/window state", 
		descr: "Defines the state of door/window depending on the magnet position."],
	[key: "ledIndications", num: 2, size: 1, type: "enum", options: [
		1: "Indication of opening/closing",
		2: "Indication of wake up",
		4: "Indication of device tampering",
		6: "Indication of wake up & tampering",
		], 
		def: "6", title: "Visual LED indications", 
		descr: "Defines events indicated by the visual LED indicator. Disabling events might extend battery life."],
	[key: "tamperDelay", num: 30, size: 2, type: "number", def: 5, min: 0, max: 32400, title: "Tamper - alarm cancellation delay", 
		descr: "Time period after which a tamper alarm will be cancelled.\n0-32400 - time in seconds"], 
	[key: "tamperCancelation", num: 31, size: 1, type: "enum", options: [0: "Do not send tamper cancellation report", 1: "Send tamper cancellation report"], def: "1", title: "Tamper – reporting alarm cancellation", 
		descr: "Reporting cancellation of tamper alarm to the controller and 3rd association group."],
	[key: "temperatureMeasurement", num: 50, size: 2, type: "number", def: 300, min: 0, max: 32400, title: "Interval of temperature measurements", 
		descr: "This parameter defines how often the temperature will be measured (specific time).\n0 - temperature measurements disabled\n5-32400 - time in seconds"], 
	[key: "temperatureThreshold", num: 51, size: 2, type: "enum", options: [
		0: "disabled", 
		3: "0.5°F/0.3°C", 
		6: "1°F/0.6°C",
		11: "2°F/1.1°C",
		17: "3°F/1.7°C",
		22: "4°F/2.2°C",
		28: "5°F/2.8°C"],
		def: 11, title: "Temperature reports threshold", 
		descr: "Change of temperature resulting in temperature report being sent to the HUB."],
	[key: "temperatureAlarm", num: 54, size: 1, type: "enum", options: [
		0: "Temperature alarms disabled", 
		1: "High temperature alarm",
		2: "Low temperature alarm",
		3: "High and low temperature alarms"], 
		def: "0", title: "Temperature alarm reports", 
		descr: "Temperature alarms reported to the Z-Wave controller. Thresholds are set in parameters 55 and 56"],
	[key: "temperatureHigh", num: 55, size: 2, type: "enum", options: [
		0: "disabled",
		200: "68°F/20°C",
		250: "77°F/25°C",
		300: "86°F/30°C",
		350: "95°F/35°C",
		400: "104°F/40°C",
		450: "113°F/45°C",
		500: "122°F/50°C",
		550: "131°F/55°C",
		600: "140°F/60°C"],
		def: 350, title: "High temperature alarm threshold", 
		descr: "If temperature is higher than set value, overheat high temperature alarm will be triggered."], 
	[key: "temperatureLow", num: 56, size: 2, type: "enum", options: [
		0: "disabled",
		6: "33°F/0.6°C",
		10: "34°F/1°C",
		22: "36°F/2.2°C",
		33: "38°F/3.3°C",
		44: "40°F/4.4°C",
		50: "41°F/5°C",
		100: "50°F/10°C",
		150: "59°F/15°C",
		200: "68°F/20°C",
		250: "77°F/25°C"],
		def: 100, title: "Low temperature alarm threshold", 
		descr: "If temperature is lower than set value, low temperature alarm will be triggered."]
	]
}

