/**
 * 	Fibaro Motion Sensor ZW5
 *
 * 	Copyright 2016 Fibar Group S.A.
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * 	in compliance with the License. You may obtain a copy of the License at:
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * 	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * 	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Fibaro Motion Sensor ZW5", namespace: "fibargroup", author: "Fibar Group S.A.", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Battery"
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Health Check"
		capability "Three Axis"

		fingerprint mfr: "010F", prod: "0801", model: "2001"
		fingerprint mfr: "010F", prod: "0801", model: "1001"

	}

	simulator {

	}

	tiles(scale: 2) {
		multiAttributeTile(name: "FGMS", type: "lighting", width: 6, height: 4) {
//with generic type secondary control text is not displayed in Android app
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("inactive", label: "no motion", icon: "st.motion.motion.inactive", backgroundColor: "#cccccc")
				attributeState("active", label: "motion", icon: "st.motion.motion.active", backgroundColor: "#00A0DC")
			}
			tileAttribute("device.tamper", key: "SECONDARY_CONTROL") {
				attributeState("detected", label: 'tampered', backgroundColor: "#00a0dc")
				attributeState("clear", label: 'tamper clear', backgroundColor: "#cccccc")
			}
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label: '${currentValue}°',
				backgroundColors: [
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}

		valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
			state "luminosity", label: '${currentValue} ${unit}', unit: "lux"
		}

		valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		valueTile("motionTile", "device.motionText", inactiveLabel: false, width: 3, height: 2, decoration: "flat") {
			state "motionText", label: '${currentValue}', action: "resetMotionTile"
		}

		valueTile("multiStatus", "device.multiStatus", inactiveLable: false, width: 3, height: 2, decoration: "flat") {
			state "val", label: '${currentValue}'
		}

		main "FGMS"
		details(["FGMS", "battery", "temperature", "illuminance", "motionTile", "multiStatus"])
	}
	preferences {

		input(
			title: "Fibaro Motion Sensor ZW5 manual",
			description: "Tap to view the manual.",
			image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/ms_icon.png",
			url: "http://manuals.fibaro.com/content/manuals/en/FGMS-001/FGMS-001-EN-T-v2.1.pdf",
			type: "href",
			element: "href"
		)

		parameterMap().findAll { (it.num as Integer) != 54 }.each {
			input(
				title: "${it.num}. ${it.title}",
				description: it.descr,
				type: "paragraph",
				element: "paragraph"
			)
			def defVal = it.def as Integer
			def descrDefVal = it.options ? it.options.get(defVal) : defVal
			input(
				name: it.key,
				title: null,
				description: "Default: $descrDefVal",
				type: it.type,
				options: it.options,
				range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
				defaultValue: it.def,
				required: false
			)
		}

		input(name: "logging", title: "Logging", type: "boolean", required: false)
	}
}

def installed() {
	sendEvent(name: "tamper", value: "clear", displayed: false)
	sendEvent(name: "motionText", value: "Disabled", displayed: false)
	sendEvent(name: "motion", value: "inactive", displayed: false)
	multiStatusEvent("Sync OK.", true, true)
}

def updated() {
	def tamperValue = device.latestValue("tamper")

	if (tamperValue == "active") {
		sendEvent(name: "tamper", value: "detected", displayed: false)
	} else if (tamperValue == "inactive") {
		sendEvent(name: "tamper", value: "clear", displayed: false)
	}
	if (settings.tamperOperatingMode == "0") {
		sendEvent(name: "motionText", value: "Disabled", displayed: false)
	}
	syncStart()
}

def ping() {
	def cmds = []
	cmds += zwave.batteryV1.batteryGet()
	cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
	cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1)
	encapSequence(cmds, 500)
}

// parse events into attributes
def parse(String description) {
	logging("Parsing '${description}'", "debug")
	def result = []

	if (description.startsWith("Err 106")) {
		if (state.sec) {
			result = createEvent(descriptionText: description, displayed: false)
		} else {
			result = createEvent(
				descriptionText: "FGK failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, cmdVersions())

		if (cmd) {
			logging("Parsed '${cmd}'", "debug")
			zwaveEvent(cmd)
		}
	}
}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x71: 3, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		logging("Unable to extract encapsulated cmd from $cmd", "warn")
		createEvent(descriptionText: cmd.toString())
	}
}

//crc16
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = [0x31: 5, 0x71: 3, 0x72: 2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		logging("Could not extract command from $cmd", "debug")
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logging("${device.displayName} - Sensor multi-level report: ${cmd}", "debug")
	def map = [displayed: true]
	switch (cmd.sensorType) {
		case 1:
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.name = "temperature"
			map.unit = getTemperatureScale()
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			break
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			break
	// case [25,52,53,54]: Note this valid use of a list is failing, why?
		case 25:
		case 52:
		case 53:
		case 54:
			map = [:]
			motionEvent(cmd.sensorType, cmd.scaledSensorValue)
			break
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def map = [:]
	if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				if (cmd.eventParameter[0] == 3) {
					map.name = "tamper"
					map.value = "clear"
					map.descriptionText = "Tamper alert cleared"
				}
				if (cmd.eventParameter[0] == 8) {
					map.name = "motion"
					map.value = "inactive"
					map.descriptionText = "${device.displayName} motion has stopped"
				}
				break

			case 3:
				map.name = "tamper"
				map.value = "detected"
				map.descriptionText = "Tamper alert: sensor removed or covering opened"
				break

			case 8:
				map.name = "motion"
				map.value = "active"
				map.descriptionText = "${device.displayName} detected motion"
				break
		}
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel.toString()
	map.unit = "%"
	map.displayed = true
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logging("${device.displayName} woke up", "debug")
	def cmds = []
	if (state.wakeUpInterval?.state == "notSynced" && state.wakeUpInterval?.value != null) {
		cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: state.wakeUpInterval.value as Integer, nodeid: zwaveHubNodeId)
		state.wakeUpInterval.state = "synced"
	}
	def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
	cmds << encap(zwave.batteryV1.batteryGet())
	cmds << "delay 500"
	cmds << encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0))
	cmds << "delay 500"
	cmds << encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1))
	cmds << "delay 1200"
	cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
	runIn(1, "syncNext")
	[event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logging("manufacturerId:   ${cmd.manufacturerId}", "debug")
	logging("manufacturerName: ${cmd.manufacturerName}", "debug")
	logging("productId:		   ${cmd.productId}", "debug")
	logging("productTypeId:	   ${cmd.productTypeId}", "debug")
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
	logging("deviceIdData:				  ${cmd.deviceIdData}", "debug")
	logging("deviceIdDataFormat:		  ${cmd.deviceIdDataFormat}", "debug")
	logging("deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}", "debug")
	logging("deviceIdType:				  ${cmd.deviceIdType}", "debug")

	if (cmd.deviceIdType == 1 && cmd.deviceIdDataFormat == 1) {//serial number in binary format
		String serialNumber = "h'"

		cmd.deviceIdData.each { data ->
			serialNumber += "${String.format("%02X", data)}"
		}

		updateDataValue("serialNumber", serialNumber)
		logging("${device.displayName} - serial number: ${serialNumber}", "info")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
	logging("applicationVersion:	  ${cmd.applicationVersion}", "debug")
	logging("applicationSubVersion:	  ${cmd.applicationSubVersion}", "debug")
	logging("zWaveLibraryType:		  ${cmd.zWaveLibraryType}", "debug")
	logging("zWaveProtocolVersion:	  ${cmd.zWaveProtocolVersion}", "debug")
	logging("zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}", "debug")
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	logging("${device.displayName}: received command: $cmd - device has reset itself", "info")
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	def map = [:]
	map.value = cmd.sensorValue ? "active" : "inactive"
	map.name = "motion"
	if (map.value == "active") {
		map.descriptionText = "${device.displayName} detected motion"
	} else {
		map.descriptionText = "${device.displayName} motion has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	logging("${device.displayName} - Configuration report: ${cmd}", "debug")
	def paramKey = parameterMap().find({ it.num == cmd.parameterNumber }).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "debug")
	state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logging("Catchall reached for cmd: $cmd", "debug")
}

def configure() {
	logging("Executing 'configure'", "debug")
	// Device-Watch simply pings if no device events received for 8 hrs & 2 minutes
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	def cmds = []

	cmds += zwave.wakeUpV2.wakeUpIntervalSet(seconds: 7200, nodeid: zwaveHubNodeId)//FGMS' default wake up interval
	cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
	cmds += zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId])
	cmds += zwave.batteryV1.batteryGet()
	cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
	cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1)
	cmds += zwave.sensorBinaryV2.sensorBinaryGet()
	cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()

	encapSequence(cmds, 500)
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
	//zwave.crc16encapV1.crc16Encap().encapsulate(cmd).format()
	"5601${cmd.format()}0000"
}

private encapSequence(commands, delay = 200) {
	delayBetween(commands.collect { encap(it) }, delay)
}

private encap(physicalgraph.zwave.Command cmd) {
	def secureClasses = [0x20, 0x30, 0x5A, 0x70, 0x71, 0x84, 0x85, 0x8E, 0x9C]

	//todo: check if secure inclusion was successful
	//if not do not send security-encapsulated command
	if (secureClasses.find { it == cmd.commandClassId }) {
		secure(cmd)
	} else {
		crc16(cmd)
	}
}

private motionEvent(Integer sensorType, value) {
	logging("${device.displayName} - Executing motionEvent() with parameters: ${sensorType}, ${value}", "debug")
	def axisMap = [52: "yAxis", 53: "zAxis", 54: "xAxis"]
	switch (sensorType) {
		case 25:
			sendEvent(name: "motionText", value: "Vibration:\n${value} MMI", displayed: false)
			break
		case 52..54:
			sendEvent(name: axisMap[sensorType], value: value, displayed: false)
			runIn(2, "axisEvent")
			break
	}
}

def axisEvent() {
	logging("${device.displayName} - Executing axisEvent() values are: ${device.currentValue("xAxis")}, ${device.currentValue("yAxis")}, ${device.currentValue("zAxis")}", "debug")
	def xAxis = Math.round((device.currentValue("xAxis") as Float) * 100)
	def yAxis = Math.round((device.currentValue("yAxis") as Float) * 100)
	// * 100 because from what I can tell apps expect data in cm/s2
	def zAxis = Math.round((device.currentValue("zAxis") as Float) * 100)
	sendEvent(name: "motionText", value: "X: ${device.currentValue("xAxis")}\nY: ${device.currentValue("yAxis")}\nZ: ${device.currentValue("zAxis")}", displayed: false)
	sendEvent(name: "threeAxis", value: "${xAxis},${yAxis},${zAxis}", isStateChange: true, displayed: false)
}

private syncStart() {
	boolean syncNeeded = false
	Integer settingValue = null
	parameterMap().each {
		if (settings."$it.key" != null || it.num == 54) {
			if (state."$it.key" == null) {
				state."$it.key" = [value: null, state: "synced"]
			}
			if ((it.num as Integer) == 54) {
				settingValue = (((settings."temperatureHigh" as Integer) == 0) ? 0 : 1) + (((settings."temperatureLow" as Integer) == 0) ? 0 : 2)
			} else if ((it.num as Integer) in [55, 56]) {
				settingValue = (((settings."$it.key" as Integer) == 0) ? state."$it.key".value : settings."$it.key") as Integer
			} else {
				settingValue = (settings."$it.key" instanceof Integer ? settings."$it.key" as Integer : settings."$it.key" as Float)
			}
			if (state."$it.key".value != settingValue || state."$it.key".state != "synced") {
				state."$it.key".value = settingValue
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}

	if (syncNeeded) {
		logging("${device.displayName} - sync needed.", "debug")
		multiStatusEvent("Sync pending. Please wake up the device by pressing the B button.", true)
	}
}

def syncNext() {
	logging("${device.displayName} - Executing syncNext()", "debug")
	def cmds = []
	for (param in parameterMap()) {
		if (state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced", "inProgress"]) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"
			cmds << response(encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: state."$param.key".value, parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
			break
		}
	}
	if (cmds) {
		runIn(10, "syncCheck")
		sendHubCommand(cmds, 1000)
	} else {
		runIn(1, "syncCheck")
	}
}

def syncCheck() {
	logging("${device.displayName} - Executing syncCheck()", "debug")
	def failed = []
	def incorrect = []
	def notSynced = []
	parameterMap().each {
		if (state."$it.key"?.state == "incorrect") {
			incorrect << it
		} else if (state."$it.key"?.state == "failed") {
			failed << it
		} else if (state."$it.key"?.state in ["inProgress", "notSynced"]) {
			notSynced << it
		}
	}

	if (failed) {
		multiStatusEvent("Sync failed! Verify parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		multiStatusEvent("Sync mismatch! Verify parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		multiStatusEvent("Sync incomplete! Wake up the device again by pressing the B button.", true, true)
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

private logging(text, type = "debug") {
	if (settings.logging == "true") {
		log."$type" text
	}
}

/*
##########################
## Device Configuration ##
##########################
*/

private Map cmdVersions() {
	[0x5E: 1, 0x86: 1, 0x72: 2, 0x59: 1, 0x80: 1, 0x73: 1, 0x56: 1, 0x22: 1, 0x31: 5, 0x98: 1, 0x7A: 3, 0x20: 1, 0x5A: 1, 0x85: 2, 0x84: 2, 0x71: 3, 0x8E: 1, 0x70: 2, 0x30: 1, 0x9C: 1]
	//Fibaro Motion Sensor ZW5
}

private parameterMap() {
	[
		[key   : "motionSensitivity", num: 1, size: 2, type: "enum", options: [
			15 : "High sensitivity",
			100: "Medium sensitivity",
			200: "Low sensitivity"
		], def: 15, min: 8, max: 255, title: "Motion detection - sensitivity", descr: ""],
		[key  : "motionBlindTime", num: 2, size: 1, type: "enum", options: [
			1 : "1 s",
			3 : "2 s",
			5 : "3 s",
			7 : "4 s",
			9 : "5 s",
			11: "6 s",
			13: "7 s",
			15: "8 s"
		], def: 15, title: "Motion detection - blind time",
		 descr: "PIR sensor is “blind” (insensitive) to motion after last detection for the amount of time specified in this parameter. (1-8 in sec.)"],
		[key  : "motionCancelationDelay", num: 6, size: 2, type: "number", def: 30, min: 1, max: 32767, title: "Motion detection - alarm cancellation delay",
		 descr: "Time period after which the motion alarm will be cancelled in the main controller. (1-32767 sec.)"],
		[key  : "motionOperatingMode", num: 8, size: 1, type: "enum", options: [0: "Always Active (default)", 1: "Active During Day", 2: "Active During Night"], def: "0", title: "Motion detection - operating mode",
		 descr: "This parameter determines in which part of day the PIR sensor will be active."],
		[key  : "motionNightDay", num: 9, size: 2, type: "number", def: 200, min: 1, max: 32767, title: "Motion detection - night/day",
		 descr: "This parameter defines the difference between night and day in terms of light intensity, used in parameter 8. (1-32767 lux)"],
		[key  : "tamperCancelationDelay", num: 22, size: 2, type: "number", def: 30, min: 1, max: 32767, title: "Tamper - alarm cancellation delay",
		 descr: "Time period after which a tamper alarm will be cancelled in the main controller. (1-32767 in sec.)"],
		[key  : "tamperOperatingMode", num: 24, size: 1, type: "enum", options: [0: "tamper only (default)", 1: "tamper and earthquake detector", 2: "tamper and orientation"], def: "0", title: "Tamper - operating modes",
		 descr: "This parameter determines function of the tamper and sent reports. It is an advanced feature serving much more functions than just detection of tampering."],
		[key  : "illuminanceThreshold", num: 40, size: 2, type: "number", def: 200, min: 0, max: 32767, title: "Illuminance report - threshold",
		 descr: "This parameter determines the change in light intensity level (in lux) resulting in illuminance report being sent to the main controller. (1-32767 in lux)"],
		[key  : "illuminanceInterval", num: 42, size: 2, type: "number", def: 3600, min: 0, max: 32767, title: "Illuminance report - interval",
		 descr: "Time interval between consecutive illuminance reports. The reports are sent even if there is no change in the light intensity. (1-3276 in sec)"],
		[key  : "temperatureThreshold", num: 60, size: 2, type: "enum", options: [
			3 : "0.5°F/0.3°C",
			6 : "1°F/0.6°C",
			10: "2°F/1 °C",
			17: "3°F/1.7°C",
			22: "4°F/2.2°C",
			28: "5°F/2.8°C"
		], def: 10, min: 0, max: 255, title: "Temperature report - threshold", descr: "This parameter determines the change in measured temperature that will result in new temperature report being sent to the main controller."],
		[key  : "ledMode", num: 80, size: 1, type: "enum", options: [
			0 : "LED inactive",
			1 : "Temp Dependent (1 long blink)",
			2 : "Flashlight Mode (1 long blink)",
			3 : "White (1 long blink)",
			4 : "Red (1 long blink)",
			5 : "Green (1 long blink)",
			6 : "Blue (1 long blink)",
			7 : "Yellow (1 long blink)",
			8 : "Cyan (1 long blink)",
			9 : "Magenta (1 long blink)",
			10: "Temp dependent (1 long 1 short blink) (default)",
			11: "Flashlight Mode (1 long 1 short blink)",
			12: "White (1 long 1 short blink)",
			13: "Red (1 long 1 short blink)",
			14: "Green (1 long 1 short blink)",
			15: "Blue (1 long 1 short blink)",
			16: "Yellow (1 long 1 short blink)",
			17: "Cyan (1 long 1 short blink)",
			18: "Magenta (1 long 1 short blink)",
			19: "Temp Dependent (1 long 2 short blink)",
			20: "White (1 long 2 short blinks)",
			21: "Red (1 long 2 short blinks)",
			22: "Green (1 long 2 short blinks)",
			23: "Blue (1 long 2 short blinks)",
			24: "Yellow (1 long 2 short blinks)",
			25: "Cyan (1 long 2 short blinks)",
			26: "Magenta (1 long 2 short blinks)"
		], def: "10", title: "Visual LED indicator - signalling mode", descr: "This parameter determines the way in which visual indicator behaves after motion has been detected."],
		[key  : "ledBrightness", num: 81, size: 1, type: "number", def: 50, min: 0, max: 100, title: "Visual LED indicator - brightness",
		 descr: "This parameter determines the brightness of the visual LED indicator when indicating motion. (1-100%)"],
		[key  : "ledLowBrightness", num: 82, size: 2, type: "number", def: 100, min: 0, max: 32767, title: "Visual LED indicator - illuminance for low indicator brightness",
		 descr: "Light intensity level below which brightness of visual indicator is set to 1% (1-32767 lux)"],
		[key  : "ledHighBrightness", num: 83, size: 2, type: "number", def: 1000, min: 0, max: 32767, title: "Visual LED indicator - illuminance for high indicator brightness",
		 descr: "Light intensity level above which brightness of visual indicator is set to 100%. (value of parameter 82 to 32767 in lux)"]
	]
}
