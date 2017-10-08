/**
 *  GoControl Motion Sensor v1.2.1
 *    (Model: WAPIRZ-1)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  URL to documentation:
 *    https://community.smartthings.com/t/release-gocontrol-door-window-sensor-motion-sensor-and-siren-dth/50728?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.2.1 (07/31/2016)
 *      - Fix iOS UI bug with tamper tile.
 *      - Removed secondary tile.
 *
 *    1.1 (06/17/2016)
 *      - Fixed tamper detection
 *
 *    1.0 (06/17/2016)
 *      - Initial Release 
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
metadata {
	definition (name:"GoControl Motion Sensor", namespace:"krlaframboise", author: "Kevin LaFramboise") {
		capability "Sensor"
		capability "Battery"
		capability "Motion Sensor"
		capability "Temperature Measurement"		
		capability "Tamper Alert"
		capability "Refresh"
		capability "Configuration"

		attribute "lastPoll", "number"

		fingerprint deviceId:"0x2001", inClusters:"0x71, 0x85, 0x80, 0x72, 0x30, 0x86, 0x31, 0x70, 0x84"
	}

	preferences {		
		input "temperatureOffset", "number",
			title: "Temperature Offset:\n(Allows you to adjust the temperature being reported if it's always high or low by a specific amount.  Example: Enter -3 to make it report 3° lower or enter 3 to make it report 3° higher.)",
			range: "-100..100",
			defaultValue: 0,
			displayDuringSetup: true,
			required: false
		input "temperatureThreshold", "number",
			title: "Temperature Change Threshold:\n(You can use this setting to prevent the device from bouncing back and forth between the same two temperatures.  Example:  If the device is repeatedly reporting 68° and 69°, you can change this setting to 2 and it won't report a new temperature unless 68° changes to 66° or 70°.)",
			range: "1..100",
			defaultValue: 1,
			displayDuringSetup: true,
			required: false
		input "retriggerWaitTime", "number", 
			title: "Re-Trigger Wait Time (Minutes)\n(When the device detects motion, it waits for at least 1 minute of inactivity before sending the inactive event.  The default re-trigger wait time is 3 minutes.)", 
			range: "1..255", 
			defaultValue: 3, 
			displayDuringSetup: true,
			required: false
		input "reportBatteryEvery", "number", 
			title: "Minimum Battery Reporting Frequency? (Hours)\n(Allows you to limit the frequency of battery reporting to extend the life of the battery)", 
			defaultValue: 24,
			range: "4..167",
			displayDuringSetup: true, 
			required: false
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}			
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			])
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:"%"
		}
		standardTile("tampering", "device.tamper", width: 2, height: 2) {
			state "detected", label:"Tamper", backgroundColor: "#ff0000"
			state "clear", label:"No Tamper", backgroundColor: "#cccccc"			
		}
		standardTile("refresh", "command.refresh", width: 2, height: 2) {
			state "default", label:"Reset", action: "refresh", icon:""
		}
		main("motion")
		details(["motion", "temperature", "refresh", "tampering", "battery"])
	}
}

def updated() {
	refresh()
}

def refresh() {	
	clearTamperDetected()
	logDebug "The re-trigger wait time will be sent to the device the next time it wakes up.  If you want this change to happen immediately, open the back cover of the device until the red light turns solid and then close it."
	state.pendingConfig = true
}

private clearTamperDetected() {	
	if (device.currentValue("tamper") != "clear") {
		logDebug "Resetting Tamper"
		sendEvent(getTamperEventMap("clear"))			
	}
}

private retriggerWaitTimeSetCmd() {
	logDebug "Setting re-trigger wait time to ${getRetriggerWaitTime()} minutes"
	
	zwave.configurationV1.configurationSet(configurationValue: [getRetriggerWaitTime()], parameterNumber: 1, size: 1).format()	
}

private temperatureGetCmd() {
	zwave.sensorMultilevelV2.sensorMultilevelGet().format()
}

private batteryGetCmd() {
	zwave.batteryV1.batteryGet().format()
}

def parse(String description) {	
	def result = []
	
	def cmd = zwave.parse(description, [0x71: 2, 0x80: 1, 0x30: 1, 0x31: 2, 0x70: 1, 0x84: 1])
	if (cmd) {
		result += zwaveEvent(cmd)
	}
	else {
		logDebug "Unknown Description: $desc"
	}

	result << createEvent(name: "lastPoll",value: new Date().time, isStateChange: true, displayed: false)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	logDebug "Woke Up"

	def result = []

	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	
	if (state.pendingConfig) {
		state.pendingConfig = false
		result += response([
			retriggerWaitTimeSetCmd(),
			batteryGetCmd(),
			temperatureGetCmd(),
			"delay 3000"
		])
	}
	else if (canCheckBattery()) {
		result += response([
			batteryGetCmd(),
			"delay 2000"
		])
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())

	return result
}

private canCheckBattery() {
	def reportEveryHours = safeToInteger(settings.reportBatteryEvery, 4)
	
	return (!state.lastBatteryReport || ((new Date().time) - state.lastBatteryReport > (reportEveryHours * 60 * 60 * 1000)))
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {	
	def motionVal = cmd.value ? "active" : "inactive"
	def desc = "Motion is $motionVal"
	logDebug "$desc"
	def result = []
	result << createEvent(name: "motion", 
			value: motionVal, 
			isStateChange: true, 
			displayed: true, 
			descriptionText: "$desc")
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {	
	logDebug "Battery: ${cmd.batteryLevel}%"
	
	def result = []
	def map = [ 
		name: "battery", 		
		unit: "%"
	]
	
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "Battery is low"
		map.displayed = true
		map.isStateChange = true
	}
	else {
		map.value = cmd.batteryLevel
		map.displayed = false
	}
		
	state.lastBatteryReport = new Date().time		
	result << createEvent(map)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	def result = []
	if (cmd.alarmType == 7 && cmd.alarmLevel == 0xFF && cmd.zwaveAlarmEvent == 3) {
		logDebug "Tampering Detected"
		result << createEvent(getTamperEventMap("detected"))
	}	
	return result
}

def getTamperEventMap(val) {
	[
		name: "tamper", 
		value: val, 
		isStateChange: true, 
		displayed: (val == "detected"),
		descriptionText: "Tamper is $val"
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def result = []
	
	if (cmd.sensorType == 1) {
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		def newTemp = safeToInteger(convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), 0)
				
		if (getTempOffset() != 0) {
			newTemp = (newTemp + getTempOffset())
			logDebug "Adjusted temperature by ${getTempOffset()}°"
		}		
		
		def highTemp = (getCurrentTemp() + getTempThreshold())
		def lowTemp = (getCurrentTemp() - getTempThreshold())
		if (newTemp >= highTemp || newTemp <= lowTemp) {
			result << createEvent(
				name: "temperature",
				value: newTemp,
				unit: getTemperatureScale(),
				isStateChange: true,
				displayed: true)
		}
		else {
			logDebug "Ignoring new temperature of $newTemp° because the change is within the ${getTempThreshold()}° threshold."
		}
	}
	return result
}

private getRetriggerWaitTime() {
	return safeToInteger(settings.retriggerWaitTime, 3)
}

private getCurrentTemp() {
	return safeToInteger(device.currentValue("temperature"), 0)
}

private getTempThreshold() {
	return safeToInteger(settings.temperatureThreshold, 1)
}

private getTempOffset() {
	return safeToInteger(settings.temperatureOffset, 0)
}

private int safeToInteger(val, defaultVal=0) {
	try {
		if (val) {
			return val.toFloat().round().toInteger()
		}
		else if (defaultVal != 0){
			return safeToInteger(defaultVal, 0)
		}
		else {
			return defaultVal
		}
	}
	catch (e) {
		logDebug "safeToInteger($val, $defaultVal) failed with error $e"
		return 0
	}
}

def logDebug(msg) {
	if (settings.debugOutput == null || settings.debugOutput) {
		log.debug "${device.displayName}: $msg"
	}
}