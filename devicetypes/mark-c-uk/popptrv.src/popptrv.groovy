/*
08/11/18 code coddensed and private change to def - It’s syntactically allowed, but actually isn’t respected by Groovy (gasp!). And in SmartThings, this really isn’t necessary since we are not creating our own classes or object models. So, we typically just omit any visibility modifier for simplicity.
07/11/18 Added function to update 1)wake interval 2)set clock - during wake periods
21/10/18 Updated from https://github.com/edshaken/SmartThingsPublic/blob/1630d63192f0349b26f64deaa10403ba958a8003/devicetypes/tommysqueak/POPP%20TRV
----Mark-C-uk----
* This started off originally from the work Tom Philip did and the submission here:
 * https://github.com/tommysqueak/SmartThingsPublic/blob/master/devicetypes/tommysqueak/danfoss-living-connect-radiator-thermostat.src/danfoss-living-connect-radiator-thermostat.groovy
 * 
 * This was originally for Danfoss Living Connect Radiator Thermostat LC-13, but I want it to work with POPP POPE010101 Z-Wave Radiator Thermostat (TRV)
 *
 *  Main difference is POPP can report back actual temperature rather than just set point
 * 
 *  Original
 *  Copyright 2016 Tom Philip
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
 * To Do
 * 
 * Sort out Heating / Idle / Auto states and their impact on the TRV.
 * Perhaps writing a smartapp that does scheduling. 
 * Sort out first time device is installed 0 values screws up the device in the app.
 * Temperature offset
 */
 
 
metadata {
	definition (name: "POPPTRV", namespace: "Mark-C-uk", author: "mark C", ocfDeviceType: "oic.d.thermostat") {
		capability "Actuator"
		capability "Sensor"
		capability "Thermostat"
		capability "Battery"
		capability "Configuration"
		capability "Switch"
		capability "Temperature Measurement"
        capability "Thermostat Cooling Setpoint"	//attribute- coolingSetpoint	command - setCoolingSetpoint - having both alows extra settings in routines
        capability "Thermostat Heating Setpoint" 
		
    	command "temperatureUp"
		command "temperatureDown"
		
    	attribute "nextHeatingSetpoint", "number"
    	attribute "temperature", "number"
        attribute "battery", "string"
        	
    fingerprint type: "0804", mfr: "0002", prod: "0115", model: "A010", cc: "80,46,81,72,8F,75,31,43,86,84", ccOut:"46,81,8F"
        
		// 0x80 = Battery v1
		// 0x46 = Climate Control Schedule v1
		// 0x81 = Clock v1
		// 0x72 = Manufacturer Specific v1
		// 0x8F = Multi Cmd v1 (Multi Command Encapsulated)
		// 0x75 = Protection v2
    	// 0x31 V2 0x31 Sensor Multilevel
		// 0x43 = Thermostat Setpoint v2
		// 0x86 = Version v1
		// 0x84 = Wake Up v2
        
	}

	simulator {
	}

    tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°',defaultState: true, backgroundColors:[
						// Celsius Color Range
						[value: 0, color: "#153591"],
						[value: 10, color: "#1e9cbb"],
						[value: 13, color: "#90d2a7"],
						[value: 17, color: "#44b621"],
						[value: 20, color: "#f1d801"],
						[value: 25, color: "#d04e00"],
						[value: 29, color: "#bc2323"],
						// Fahrenheit Color Range
						[value: 40, color: "#153591"],
						[value: 44, color: "#1e9cbb"],
						[value: 59, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 84, color: "#f1d801"],
						[value: 92, color: "#d04e00"],
						[value: 96, color: "#bc2323"]
					])
            }
			tileAttribute("device.nextHeatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "temperatureUp")
				attributeState("VALUE_DOWN", action: "temperatureDown")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("default", label:'${currentValue}')
				attributeState("heating", label:"heating", backgroundColor:"#ffa81e", icon:"st.thermostat.heat")
				attributeState("idle", label:"idle", backgroundColor:"#1e9cbb", icon:"st.thermostat.heating-cooling-off")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("default", label:'${currentValue}')
				attributeState("heat", label:"heat",  icon:"st.thermostat.heat") //backgroundColor:"#ffa81e",
				attributeState("off", label:"off",  icon:"st.thermostat.heating-cooling-off") //backgroundColor:"#1e9cbb",
			}
			tileAttribute("device.thermostatSetpoint", key: "HEATING_SETPOINT") { // //nextHeatingSetpoint //setpoint as last reported by device
				attributeState("default", label:'${currentValue}', unit:"°C", defaultState: true, backgroundColors:[
				// Celsius 
				[value: 0, color: "#b8c2de"],
				[value: 10, color: "#bbe1ea"],
				[value: 13, color: "#ddf1e4"],
				[value: 17, color: "#c6e9bc"],
				[value: 20, color: "#faf3b2"],
				[value: 25, color: "#f0c9b2"],
				[value: 29, color: "#eabdbd"],
				// Fahrenheit
				[value: 40, color: "#b8c2de"],
				[value: 44, color: "#bbe1ea"],
				[value: 59, color: "#ddf1e4"],
				[value: 74, color: "#c6e9bc"],
				[value: 84, color: "#faf3b2"],
				[value: 95, color: "#f0c9b2"],
				[value: 96, color: "#eabdbd"]
			])
			}
	}

	valueTile("battery", "device.battery", inactiveLabel: true, height: 2, width: 2, decoration: "flat") {
		state ("battery", label:'${currentValue}%', icon:"st.samsung.da.RC_ic_charge", defaultState: true, backgroundColors:[
			[value: 100, color: "#44b621"],
			[value: 50, color: "#f1d801"],
			[value: 0, color: "#bc2323"],
		])
	}
        
	standardTile("switcher", "device.switch", height: 2, width: 2, decoration: "flat") {
		state("off", action:"on", label: "off", icon: "st.thermostat.heating-cooling-off", backgroundColor:"#ffffff")
		state("on", action:"off", label: "on", icon: "st.thermostat.heat", backgroundColor:"#ffa81e")
	}
    standardTile("thermostatMode", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
				state("heat", label:"press for off", action:"off", icon:"st.thermostat.heat") //backgroundColor:"#ffa81e",
				state("off", label:"Press for On", action:"on", icon:"st.thermostat.heating-cooling-off") //backgroundColor:"#1e9cbb",
    }
    valueTile("temp", "device.temperature", width: 2, height: 2, inactiveLabel: true) { // hear to enable it to show in activity feed
		state ("temp", label:'${currentValue}°', defaultState: true, backgroundColors:[
			[value: 0, color: "#153591"],
			[value: 10, color: "#1e9cbb"],
			[value: 13, color: "#90d2a7"],
			[value: 17, color: "#44b621"],
			[value: 20, color: "#f1d801"],
			[value: 25, color: "#d04e00"],
			[value: 29, color: "#bc2323"],
		])
	}
	valueTile("nextHeatingSetpoint", "device.nextHeatingSetpoint", width: 2, height: 2, inactiveLabel: true) { // hear to enable it to show in activity feed
		state ("nextHeatingSetpoint", label:'${currentValue}°', defaultState: true)
	}	
        main "temperature"
		details(["temperature", "battery", "switcher","temp", "thermostatMode"])
	}

	preferences {
		input "wakeUpIntervalInMins", "number", title: "Wake Up Interval (min). Default 5mins.", description: "Wakes up and send\receives new temperature setting", range: "1..30", displayDuringSetup: true
		input "quickOnTemperature", "number", title: "Quick On Temperature. Default 21°C.", description: "Quickly turn on the radiator to this temperature", range: "5..82", displayDuringSetup: false
		input "quickOffTemperature", "number", title: "Quick Off Temperature. Default 4°C.", description: "Quickly turn off the radiator to this temperature", range: "4..68", displayDuringSetup: false
	}
}

/*	Event order
Scenario - temp is changed via the app
	* BatteryReport (seem to get one of these everytime it wakes up! This is ood, as we don't ask for it)
	* WakeUpNotification (the new temperature, set by the app is sent)
	* ScheduleOverrideReport (we don't handle it)
	* ThermostatSetpointReport (we receivce the new temp we sent when it woke up. We also set the heatingSetpoint and next one, all is aligned :))
Scenario - temp is changed via the app - alternative (this sometimes happens instead)
	* ThermostatSetpointReport - resets the next temp back to the current temp. Not what we want :(
	* BatteryReport
	* WakeUpNotification
	* ScheduleOverrideReport
Scenario - temp is changed on the radiator thermostat
	* BatteryReport (seem to get one of these everytime it wakes up! This is ood, as we don't ask for it)
	* ThermostatSetpointReport (we receive the temp set on the thermostat. We also set the heatingSetpoint and next one. If we set a next one, it's overwritten.)
	* ScheduleOverrideReport (we don't handle it)
	* WakeUpNotification (no temp is sent, as they're both the same coz of ThermostatSetpointReport)
Scenario - wakes up
	* BatteryReport
	* WakeUpNotification

WakeUpINterval 300 (5mins) to 1800 (30 mins)
	* Installed WakeUpIntervalSeconds: 300, minimumWakeUpIntervalSeconds: 60, wakeUpIntervalStepSeconds: 60
*/

// All messages from the device are passed to the parse method. It is responsible for turning those messages into something the SmartThings platform can understand.
def parse(String description) {
	def result = null
	//	The commands in the array are to map to versions of the command class. eg physicalgraph.zwave.commands.wakeupv1 vs physicalgraph.zwave.commands.wakeupv2
	//	If none specified, it'll use the latest version of that command class.
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
		log.info "Parsed '${cmd}'" // to ${result.inspect()}" //result.inspect shows all the decoded details removed to keep debugin tidye
	}
	else {
		log.warn "Non-parsed event: ${description} --- cmd = '${cmd}'"
	}
	result
}

//	catch all unhandled events
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Uncaptured/unhandled event for ${device.displayName}: ${cmd}"
	return createEvent(descriptionText: "Uncaptured event for ${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.climatecontrolschedulev1.ScheduleOverrideReport cmd) {
	//log.debug "Schedule Override Report ${device.displayName} ${cmd} key = OVERRIDE_STATE_NO_OVERRIDE = 0"
	// dont actualy do anything with this message and messages are autmoticly retunred //return createEvent(descriptionText: "ScheduleOverrideReport ${device.displayName}: ${cmd}")
}
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd) {
	log.debug "Wake Up Interval Report recived: ${cmd.toString()}" //dont think this is used
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) {
	//	example message -- Parsed ThermostatSetpointReport(precision: 2, reserved01: 0, scale: 0, scaledValue: 21.00, setpointType: 1, size: 2, value: [8, 52])
	state.scale = cmd.scale	// So we can respond with same format later, see setHeatingSetpoint()
	state.precision = cmd.precision

	def eventList = []
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def radiatorTemperature = Double.parseDouble(convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)).round(1)
	def currentTemperature = currentDouble("heatingSetpoint")
	def nextTemperature = currentDouble("nextHeatingSetpoint")
	def discText = ""
        
	if(radiatorTemperature != currentTemperature){
		log.debug "ThermostatSetpointReport detected change, AppCurrent:${currentTemperature} AppNext:${nextTemperature} DeviceCurrent:${radiatorTemperature}"
        if(state.lastSentTemperature == radiatorTemperature) {
			discText = "Temperature changed by app to ${radiatorTemperature}°" + getTemperatureScale() + "."
        	log.debug "ThermostatSetpointReport - '${discText}'"
        }
		else {
        	discText = "Temperature changed manually to ${radiatorTemperature}°" + getTemperatureScale() + "."
			log.debug "ThermostatSetpointReport -'${discText}'"
			state.lastSentTemperature = radiatorTemperature
			buildNextState(radiatorTemperature).each { eventList << it }
		}
	}

	eventList << createEvent(name: "heatingSetpoint", value: radiatorTemperature, unit: getTemperatureScale(), displayed: false, descriptionText:discText)
	eventList << createEvent(name: "thermostatSetpoint", value: radiatorTemperature, unit: getTemperatureScale(), descriptionText:discText)
	def switchState = onOffEvent(radiatorTemperature).value
	eventList << createEvent(name: "thermostatMode", value: (switchState == "on") ? "heat" : "off") //displayed: false
    eventList << createEvent(name: "thermostatOperatingState", value: (switchState == "on") ? "heating" : "idle")
	
    if(nextTemperature == 0) { //	initialise the nextHeatingSetpoint, on the very first time we install and get the devices temp
		buildNextState(radiatorTemperature).each { eventList << it }
		state.lastSentTemperature = radiatorTemperature
	}
	return eventList
}

//Recives the actual temperature from the TRV.
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	//log.debug "Recive temparture value from device zwaveEvent $cmd)" //(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport
	def events = []
	if (cmd.sensorType == 0x01) {
		def reportedTemperatureValue = cmd.scaledSensorValue
		def reportedTemperatureUnit = cmd.scale == 1 ? "F" : "C"
		def convertedTemperatureValue = convertTemperatureIfNeeded(reportedTemperatureValue, reportedTemperatureUnit, 2)
		def descriptionText = "temperature was $convertedTemperatureValue °" + getTemperatureScale() + "." 

		events << createEvent(name: "temperature", value: convertedTemperatureValue, descriptionText: "$descriptionText")
	}
	return events
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	log.debug "Wakey wakey zwaveEvent ${cmd}"
	state.ComCount = 0
	def event = createEvent(name: "wake up", value: "${new Date().time}", descriptionText: "${device.displayName} woke up", displayed: false)
  	def cmds = []
	//battery
	if (!state.lastBatteryReportReceivedAt || (new Date().time) - state.lastBatteryReportReceivedAt > daysToTime(7)) {
		log.trace "WakeUp - Asking for battery report as over 7 days since"
		state.ComCount =  state.ComCount + 1
        cmds << zwave.batteryV1.batteryGet().format()
	}
    //time
    if (!state.lastClockSet || (new Date().time) - state.lastClockSet > daysToTime(7)) {
		log.warn "clock details state = $state.lastClockSet, new date ${new Date().time}"
        log.trace "WakeUp - Updating Clock as 7 days since"
        state.ComCount =  state.ComCount + 1
        cmds << currentTimeCommand()
	} 
	// wake up intval
    if (state.configrq == true) {
    	log.trace "WakeUp - Sending - wakeUpIntervalSet='${state.wakeUpEvery}'s or '${state.wakeUpEvery/60}'min, this normally takes a full cycle to come into effect"
    	state.ComCount =  state.ComCount + 1
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds:state.wakeUpEvery, nodeid:zwaveHubNodeId).format()
        state.configrq = false
    }
    def nextHeatingSetpoint = currentDouble("nextHeatingSetpoint")
	def heatingSetpoint = currentDouble("heatingSetpoint")
    // def thermostatMode = device.currentValue("thermostatMode")
    //log.debug "Thermostat mode is ${thermostatMode}"
	
    // temp setpoint
	if (nextHeatingSetpoint != 0 && nextHeatingSetpoint != heatingSetpoint) {
		log.trace "WakeUp - Sending new temperature ${nextHeatingSetpoint}, curent heating setpoint ${heatingSetpoint}"
		state.lastSentTemperature = nextHeatingSetpoint
		cmds << setHeatingSetpointCommand(nextHeatingSetpoint).format()
        cmds << "delay 2000"
		cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
        state.ComCount =  state.ComCount + 2
	}
    // no more - go back to sleep
    state.ComCount =  state.ComCount + 1
    cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	//log.debug "WakeUp - commands are ${cmds}"
   	cmds << zwave.multiCmdV1.multiCmdEncap(numberOfCommands: state.ComCount).format()
    log.debug "WakeUp - commands are ${cmds}, command count is $state.ComCount"
    state.ComCount = 0
  	//log.debug "WakeUp - commands are ${cmds}, command count is $state.ComCount"
    [event, response(delayBetween(cmds, 50))]
    //    state.ComCount = 0
}

def zwaveEvent(physicalgraph.zwave.commands.multicmdv1.MultiCmdEncap cmd) { //this should be used to save battery but how!
	log.debug "MultiCmd with $numberOfCommands inner commands"
	cmd.encapsulatedCommands(commandClassVersions).collect { encapsulatedCommand ->
		zwaveEvent(encapsulatedCommand)
	}.flatten()
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
		map.value = 1
		map.descriptionText = "Low Battery"
	} 
    else {
		map.value = cmd.batteryLevel
	}
	state.lastBatteryReportReceivedAt = new Date().time	// Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
	createEvent(map)
}

def currentTimeCommand() {
    def nowCalendar = Calendar.getInstance(location.timeZone)
    def weekday = nowCalendar.get(Calendar.DAY_OF_WEEK) - 1
    if (weekday == 0) {
        weekday = 7
    }
    log.debug "currentTimeCommand Setting clock to hour='${nowCalendar.get(Calendar.HOUR_OF_DAY)}', minute='${nowCalendar.get(Calendar.MINUTE)}', DayNum='${weekday}'"
    state.lastClockSet = new Date().time // Store time of last time update so we only send once a week, see WakeUpNotification handler
	return zwave.clockV1.clockSet(hour: nowCalendar.get(Calendar.HOUR_OF_DAY), minute: nowCalendar.get(Calendar.MINUTE), weekday: weekday).format()
}

//	commands (that it can handle, must implement those that match it's capabilities, so SmartApps can call these methods)
//	TODO: review the commands, do they have the right interface/params


def temperatureUp() {
	def nextTemp = currentDouble("nextHeatingSetpoint") + 0.5d
	//	TODO: deal with Farenheit?
	if(nextTemp > fromCelsiusToLocal(28)) {	//	It can't handle above 28, so don't allow it go above
		nextTemp = fromCelsiusToLocal(28)
	}
	setHeatingSetpoint(nextTemp)
}

def temperatureDown() {
	def nextTemp = currentDouble("nextHeatingSetpoint") - 0.5d
	if(nextTemp < fromCelsiusToLocal(4)) {	//	It can't go below 4, so don't allow it
		nextTemp = fromCelsiusToLocal(4)
	}
    setHeatingSetpoint(nextTemp)
}

//	this enables a cooling temparture to be in Apps but sends to setThermostat Heating Setpoint
def setCoolingSetpoint(temp){
	log.debug "setCoolingSetpoint - temp of '${temp}', sending temp value to setHeatingSetpoint"
	setHeatingSetpoint(temp)
}
def setHeatingSetpoint(degrees) {
	log.debug "setHeatingSetpoint(just degrees) - Storing temperature for next wake ${degrees}"
	setHeatingSetpoint(degrees.toDouble())
}
def setHeatingSetpoint(Double degrees) {
	log.debug "setHeatingSetpoint(Double deg) - Storing temperature for next wake ${degrees}"
	sendEvent(name:"nextHeatingSetpoint", value: degrees.round(1), unit: getTemperatureScale())
	sendEvent(onOffEvent(degrees))
}
def setHeatingSetpointCommand(Double degrees) {
	log.trace "setHeatingSetpointCOMMAND(DD) setting to '${degrees}'"
	def deviceScale = state.scale ?: 0
	def deviceScaleString = deviceScale == 1 ? "F" : "C"
	def locationScale = getTemperatureScale()
	def precision = state.precision ?: 2
	def convertedDegrees
	if (locationScale == "C" && deviceScaleString == "F") {
		convertedDegrees = celsiusToFahrenheit(degrees)
	} 
    else if (locationScale == "F" && deviceScaleString == "C") {
		convertedDegrees = fahrenheitToCelsius(degrees)
	}
    else {
		convertedDegrees = degrees
	}
	zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: precision, scaledValue: convertedDegrees)
}

def on() {
	//def nexttemp = currentDouble("nextHeatingSetpoint")
	//setHeatingSetpoint(nexttemp)
    //sendEvent(name: "thermostatMode", value: "heat", descriptionText: "turned on", displayed: true)
    setHeatingSetpoint(quickOnTemperature ?: fromCelsiusToLocal(21))
}

def off() {
	setHeatingSetpoint(quickOffTemperature ?: fromCelsiusToLocal(4))
    //sendEvent(name: "thermostatMode", value: "off", descriptionText: "turned off", displayed: true)
}

def installed() {
	log.debug "installed"
    state.configrq = false
    state.lastClockSet = ""
    state.lastBatteryReportReceivedAt = ""
	delayBetween([
  	zwave.configurationV1.configurationSet(parameterNumber:1, size:2, scaledConfigurationValue:100).format(),	// not sure if needed
    zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format(),		//	Get it's configured info, like it's scale (Celsius, Farenheit) Precicsion // 1 = SETPOINT_TYPE_HEATING_1
    zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(),
  	currentTimeCommand(), 															// Set it's time/clock. Do we need to do this periodically, like the battery check?
	zwave.wakeUpV1.wakeUpIntervalSet(seconds:300, nodeid:zwaveHubNodeId).format() 	// Make sure sleepy battery-powered sensor sends its UpNotifications to the hub every 5 mins intially
	], 1000)
    log.debug "installed finished - wakeUpIntervalSet to 300"
}

def updated() {
	log.debug "updated"
	configure()
}

def configure() {
	def wakeUpEvery = (wakeUpIntervalInMins ?: 5) * 60
	state.configrq = true
    state.lastClockSet = ""
    state.wakeUpEvery = wakeUpEvery
    log.debug "Configure - storing wakeUpInterval for next wake '$state.wakeUpEvery'seconds AND configuration flag is $state.configrq"
}

def buildNextState(Double degrees) {
	def nextStateList = []

	nextStateList << createEvent(name:"nextHeatingSetpoint", value: degrees, unit: getTemperatureScale(), displayed: false)
	nextStateList << createEvent(onOffEvent(degrees))
	nextStateList
}

def onOffEvent(Double degrees) {
	if(degrees > (quickOffTemperature ?: fromCelsiusToLocal(4))) {
		[name:"switch", value: "on", displayed: false]
        
	}
	else {
		[name:"switch", value: "off", displayed: false]
	}
}

def daysToTime(days) {
	days*24*60*60*1000
}

def fromCelsiusToLocal(Double degrees) {
	if(getTemperatureScale() == "F") {
		return celsiusToFahrenheit(degrees)
	}
	else {
		return degrees
	}
}


def currentDouble(attributeName) {
	if(device.currentValue(attributeName)) {
		return device.currentValue(attributeName).doubleValue()
	}
	else {
		return 0d
	}
}
/// misc stuff
def heat() {
	on()
}
def cool() {
	off()
}
def emergencyHeat() {
	setHeatingSetpoint(fromCelsiusToLocal(10))
}
def setThermostatMode(mode) {
}
def fanOn() {
}
def fanAuto() {
}
def fanCirculate() {
}
def setThermostatFanMode(mode) {
}
def auto() {
}
/* disabled 8/11/18 nothing calls this up
private setClock() {
	Log.warn "private - setclock is used!" // set the clock once a week
	def now = new Date().time
	if (!state.lastClockSet || now - state.lastClockSet > daysToTime(7)) {
		state.lastClockSet = now
		currentTimeCommand()
	}
	else {
		null
	}
}
*/