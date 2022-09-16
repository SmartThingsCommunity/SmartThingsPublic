/**
 *     Evalogik Door/Window Sensor v1.0.5
 *
 *  	Models: MSE30Z
 *
 *  Author:
 *   winnie (sky-nie)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.0.5 (07/28/2021)
 *     - omitted  all the parameters related to associations group
 *
 *    1.0.4 (07/16/2021)
 *     - Syntax format compliance adjustment
 *     - fixed a bug for order repeated
 *
 *    1.0.3 (07/16/2021)
 *     - change lastBatteryReport to record the time of fresh battery
 *     - add lastBattery to record the battery value
 *
 *    1.0.2 (07/15/2021)
 *      - update ConfigParams as product designed
 *      - update DTH name as product designed
 *
 *    1.0.1 (07/13/2021)
 *      - Syntax format compliance adjustment
 *      - delete dummy code
 *
 *    1.0.0 (04/26/2021)
 *      - Initial Release
 *
 * Reference：
 *   https://community.smartthings.com/t/release-aeotec-trisensor/140556?u=krlaframboise
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
final  int NOTIFICATION_TYPE_ACCESS_CONTROL = 0x06
final  int NOTIFICATION_TYPE_HOME_SECURITY = 0x07

final  int NOTIFICATION_EVENT_DOOR_WINDOW_OPEN = 0x16
final  int NOTIFICATION_EVENT_DOOR_WINDOW_CLOSED = 0x17

final  int NOTIFICATION_EVENT_STATE_IDLE = 0x00
final  int NOTIFICATION_EVENT_INSTRUSION_WITH_LOCATION = 0x01
final  int NOTIFICATION_EVENT_INSTRUSION = 0x02
final  int NOTIFICATION_EVENT_TEMPERING = 0x03

metadata {
	definition(name: "Evalogik Door/Window Sensor", namespace: "sky-nie", author: "winnie", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Sensor"
		capability "Contact Sensor"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		attribute "lastCheckIn", "string"
		attribute "pendingChanges", "string"

		fingerprint mfr: "0312", prod: "0713", model: "D100", deviceJoinName: "Minoston 3-in-1 Sensor"//MSE30Z
	}

	preferences {
		configParams.each {
			if (it.range) {
				input "configParam${it.num}", "number", title: "${it.name}:", required: false, defaultValue: "${it.value}", range: it.range
			} else {
				input "configParam${it.num}", "enum", title: "${it.name}:", required: false, defaultValue: "${it.value}", options:it.options
			}
		}
	}
}

def installed() {
	log.debug "installed()..."
	state.refreshConfig = true
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

private static def getCheckInterval() {
	// These are battery-powered devices, and it's not very critical
	// to know whether they're online or not – 12 hrs
	return (60 * 60 * 3) + (5 * 60)
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time

		log.trace "updated()"
		if (device.latestValue("checkInterval") != checkInterval) {
			sendEvent(name: "checkInterval", value: checkInterval, displayed: false)
		}

		refreshPendingChanges()

		logForceWakeupMessage "Configuration changes will be sent to the device the next time it wakes up."
	}
}

def configure() {
	log.trace "configure()"

	runIn(8, executeConfigure)
}

def executeConfigure() {
	def cmds = [
		sensorBinaryGetCmd(),
		batteryGetCmd()
	]

	cmds += getConfigCmds()
	sendHubCommand(cmds, 500)
}

private getConfigCmds() {
	def cmds = []
	configParams.each { param ->
		def storedVal = getParamStoredValue(param.num)
		if (state.refreshConfig) {
			cmds << configGetCmd(param)
		} else if ("${storedVal}" != "${param.value}") {
			log.debug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
			cmds << configGetCmd(param)

			if (param.num == minTemperatureOffsetParam.num) {
				cmds << "delay 3000"
				cmds << sensorMultilevelGetCmd(tempSensorType)
			} else if (param.num == minHumidityOffsetParam.num) {
				cmds << "delay 3000"
				cmds << sensorMultilevelGetCmd(lightSensorType)
			}
		}
	}
	state.refreshConfig = false
	return cmds
}

// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
	log.debug "ping()"
}

// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {
	logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
	state.lastBatteryReport = null
	state.lastBattery = null
	if (!state.refreshSensors) {
		state.refreshSensors = true
	} else {
		state.refreshConfig = true
	}
	refreshPendingChanges()
	return []
}

private logForceWakeupMessage(msg) {
	log.debug "${msg}  You can force the device to wake up immediately by holding the z-button for 2 seconds."
}

def parse(String description) {
	def result = []
	try {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result += zwaveEvent(cmd)
		} else {
			log.debug "Unable to parse description: $description"
		}

		sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
	} catch (e) {
		log.error "$e"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapCmd = cmd.encapsulatedCommand(commandClassVersions)

	def result = []
	if (encapCmd) {
		result += zwaveEvent(encapCmd)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	log.debug "Device Woke Up"

	def cmds = []
	if (state.refreshConfig || pendingChanges > 0) {
		cmds += getConfigCmds()
	}

	if (canReportBattery()) {
		cmds << batteryGetCmd()
	}

	if (state.refreshSensors) {
		cmds += [
			sensorBinaryGetCmd(),
			sensorMultilevelGetCmd(tempSensorType),
			sensorMultilevelGetCmd(lightSensorType)
		]
		state.refreshSensors = false
	}

	if (cmds) {
		cmds = delayBetween(cmds, 1000)
		cmds << "delay 3000"
	}
	cmds << secureCmd(zwave.wakeUpV1.wakeUpNoMoreInformation())
	return response(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	} else if (val < 1) {
		val = 1
	}
	state.lastBatteryReport = new Date().time
	state.lastBattery = val
	log.debug "Battery ${val}%"
	sendEvent(getEventMap("battery", val, null, null, "%"))
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	log.trace "SensorMultilevelReport: ${cmd}"
	switch (cmd.sensorType) {
		case tempSensorType:
			def unit = cmd.scale ? "F" : "C"
			def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)
			sendEvent(getEventMap("temperature", temp, true, null, getTemperatureScale()))
			break
		case lightSensorType:
			sendEvent(getEventMap( "humidity", cmd.scaledSensorValue, true, null, "%"))
			break
		default:
			log.debug "Unknown Sensor Type: ${cmd.sensorType}"
	}
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.trace "ConfigurationReport ${cmd}"

	runIn(4, refreshPendingChanges)

	def param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		def val = cmd.scaledConfigurationValue

		log.debug "${param.name}(#${param.num}) = ${val}"
		state["configParam${param.num}"] = val
	} else {
		log.debug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
	}
	return []
}

def refreshPendingChanges() {
	sendEvent(name: "pendingChanges", value: "${pendingChanges} Pending Changes", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.trace "NotificationReport: $cmd"
	def result = []

	if(cmd.notificationType == NOTIFICATION_TYPE_ACCESS_CONTROL){
		if(cmd.event == NOTIFICATION_EVENT_DOOR_WINDOW_OPEN){
			result << sensorValueEvent(1)
		} else if(cmd.event == NOTIFICATION_EVENT_DOOR_WINDOW_CLOSED) {
			result << sensorValueEvent(0)
		}
	} else if (cmd.notificationType == NOTIFICATION_TYPE_HOME_SECURITY) {
		if (cmd.event == NOTIFICATION_EVENT_STATE_IDLE) {
			result << createEvent(descriptionText: "$device.displayName covering was restored", isStateChange: true)
			cmds = [zwave.batteryV1.batteryGet(), zwave.wakeUpV1.wakeUpNoMoreInformation()]
			result << response(commands(cmds, 1000))
		} else if (cmd.event == NOTIFICATION_EVENT_INSTRUSION_WITH_LOCATION || cmd.event == NOTIFICATION_EVENT_INSTRUSION) {
			result << sensorValueEvent(1)
		} else if (cmd.event == NOTIFICATION_EVENT_TEMPERING) {
			result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}

	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	log.trace "SensorBinaryReport: $cmd"
	def map = [:]
	map.value = cmd.sensorValue ? "open" : "closed"
	map.name = "contact"
	if (map.value == "open") {
		map.descriptionText = "${device.displayName} is open"
	} else {
		map.descriptionText = "${device.displayName} is closed"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
	log.trace "${cmd}"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Ignored Command: $cmd"
	return []
}

private getEventMap(name, value, displayed=null, desc=null, unit=null) {
	def isStateChange = (device.currentValue(name) != value)
	displayed = (displayed == null ? isStateChange : displayed)
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: isStateChange,
		descriptionText: desc ?: "${device.displayName} ${name} is ${value}"
	]

	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText}${unit}"
	}
	if (displayed) {
		log.debug "${eventMap.descriptionText}"
	}
	return eventMap
}

private batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

private sensorBinaryGetCmd() {
	return secureCmd(zwave.sensorBinaryV2.sensorBinaryGet())
}

private sensorMultilevelGetCmd(sensorType) {
	def scale = (sensorType == tempSensorType) ? 0 : 1
	return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private secureCmd(cmd) {
	try {
		if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
			return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			return cmd.format()
		}
	} catch (ex) {
		throw new RuntimeException(ex)
	}
}

private static getCommandClassVersions() {
	[
		0x30: 2,  // SensorBinary
		0x31: 5,  // SensorMultilevel
		0x55: 1,  // TransportServices
		0x59: 1,  // AssociationGrpInfo
		0x5A: 1,  // DeviceResetLocally
		0x5E: 2,  // ZwaveplusInfo
		0x6C: 1,  // Supervision
		0x70: 1,  // Configuration
		0x71: 3,  // Notification
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x7A: 2,  // FirmwareUpdateMd
		0x80: 1,  // Battery
		0x84: 1,  // WakeUp
		0x85: 2,  // Association
		0x86: 1,  // Version
		0x8E: 2,  // MultChannelAssociation
		0x87: 1,  // Indicator
		0x9F: 1   // Security 2
	]
}

private canReportBattery() {
	return state.refreshSensors || (!isDuplicateCommand(state.lastBatteryReport, (12 * 60 * 60 * 1000)))
}

private getPendingChanges() {
	return configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }
}

private getParamStoredValue(paramNum) {
	return safeToInt(state["configParam${paramNum}"] , null)
}

// Sensor Types
private static getTempSensorType() { return 1 }
private static getLightSensorType() { return 5 }

// Configuration Parameters
private getConfigParams() {
	[
		batteryReportThresholdParam,
		lowBatteryAlarmReportParam,
		sensorModeWhenClosedParam,
		delayReportSecondsWhenClosedParam,
		delayReportSecondsWhenOpenedParam,
		minTemperatureOffsetParam,
		minHumidityOffsetParam,
		temperatureUpperWatermarkParam,
		temperatureLowerWatermarkParam,
		humidityUpperWatermarkParam,
		humidityLowerWatermarkParam,
		switchTemperatureUnitParam,
		temperatureOffsetParam,
		humidityOffsetParam,
		associationGroupSettingParam
	]
}

private getBatteryReportThresholdParam() {
	return getParam(1, "Battery report threshold(1% - 20%)", 1, 10, null,"1..20")
}

private getLowBatteryAlarmReportParam() {
	return getParam(2, "Low battery alarm report(5% - 20%)", 1, 5, null, "5..20")
}

private getSensorModeWhenClosedParam() {
	return getParam(3, "State of the sensor when the magnet closes the reed", 1, 0, sensorModeWhenCloseOptions)
}

private getDelayReportSecondsWhenClosedParam() {
	return getParam(4, "Delay in seconds with ON command report(door closed)", 2, 0, null, "0..3600")
}

private getDelayReportSecondsWhenOpenedParam() {
	return getParam(5, "Delay in seconds with OFF command report(door open)", 2, 0, null, "0..3600")
}

private getMinTemperatureOffsetParam() {
	return getParam(6, "Minimum Temperature change to report(0.5℃/0.9°F - 5.0℃/9°F)", 1, 10, null, "5..50")
}

private getMinHumidityOffsetParam() {
	return getParam(7, "Minimum Humidity change to report(5% - 20%)", 1, 10, null, "5..20")
}

private getTemperatureUpperWatermarkParam() {
	return getParam(8, "Temperature Upper Watermark value(0,Disabled; 1℃/33.8°F-50℃/122.0°F)", 2, 0, null, "0..50")
}

private getTemperatureLowerWatermarkParam() {
	return getParam(10, "Temperature Lower Watermark value(0,Disabled; 1℃/33.8°F - 50℃/122.0°F)", 2, 0, null, "0..50")
}

private getHumidityUpperWatermarkParam() {
	return getParam(12, "Humidity Upper Watermark value(0,Disabled; 1% - 100%)", 1, 0, null, "0..100")
}

private getHumidityLowerWatermarkParam() {
	return getParam(14, "Humidity Lower Watermark value(0,Disabled; 1%-100%)", 1, 0, null, "0..100")
}

private getSwitchTemperatureUnitParam() {
	return getParam(16, "Switch the unit of Temperature report", 1, 1,  switchTemperatureUnitOptions)
}

private getTemperatureOffsetParam() {
	return getParam(17, "Offset value for temperature(-10℃/14.0°F - 10℃/50.0°F)", 1, 0,  null, "-100..100")
}

private getHumidityOffsetParam() {
	return getParam(18, "Offset value for humidity (-20% - 20%)", 1, 0,  null, "-20..20")
}

private getAssociationGroupSettingParam() {
	return getParam(19, "Association Group 2 Setting", 1, 1, associationGroupSettingOptions)
}

private getParam(num, name, size, defaultVal, options=null, range=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	def map = [num: num, name: name, size: size, value: val]
	if (options) {
		map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
		map.options = setDefaultOption(options, defaultVal)
	}
	if (range) {
		map.range = range
	}

	return map
}

private static setDefaultOption(options, defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}

// Setting Options
private static getSwitchTemperatureUnitOptions() {
	return [
		"0":"Celsius",
		"1":"Fahrenheit"
	]
}

private static getAssociationGroupSettingOptions() {
	return [
		"0":"Disable completely",
		"1":"Send Basic SET 0xFF when Magnet is away,and send Basic SET 0x00 when Magnet is near.",
		"2":"Send Basic SET 0x00 when Magnet is away,and send Basic SET 0xFF when Magnet is near",
		"3":"Only send Basic SET 0xFF when Magnet is away",
		"4":"Only send Basic SET 0x00 when Magnet is near",
		"5":"Only send Basic SET 0x00 when Magnet is away",
		"6":"Only send Basic SET 0xFF when Magnet is near"
	]
}

private static getSensorModeWhenCloseOptions() {
	return [
		"0":"door/window closed",
		"1":"door/window opened"
	]
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

private static safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	} else {
		return "$dt"
	}
}

private static isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}