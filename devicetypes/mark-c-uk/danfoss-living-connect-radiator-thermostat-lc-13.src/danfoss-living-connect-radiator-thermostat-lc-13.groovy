/*
 *  Danfoss Living Connect Radiator Thermostat LC-13
 *
 *
 
 ----Mark-C-uk---- 
 
 10/12/18 - Standised as a thermostat, "delta, devation" helighted, mode button updated to cycle through off(settings off temp or 4), 
 			heat(settings or 21), emergency heat (settings or 25). Summer mode added to open valve (28 deg) and prevent any schedaling changes (to save battery
            and to keep valve from sticking), mannual adjustment will over ride this.
            ToDo 
            	1)add temparture report but comment out as a "delta, devation"
                2)look at multiCmdEncap running on other trv so see effect on battery life
 
 ---		------
 
 Copyright 2017 Tom Philip
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
 *  Revision History
 *  ==============================================
 *  2017-09-16 - Uses the 'official' colour range for showing off the temperature.
 *  2017-09-16 - Battery icon - Credit: Power bank by Gregor Cresnar from the Noun Project.
 *  2017-03-10 - Radiator can be turned off, whilst keeping the next temperature state. Credit: https://github.com/struna
 *
 */

metadata {
	definition (name: "Danfoss Living Connect Radiator Thermostat LC-13", namespace: "Mark-C-uk", author: "Mark C", ocfDeviceType: "oic.d.thermostat") {
		capability "Actuator"
		capability "Sensor"
		capability "Thermostat"//
		capability "Battery"
		capability "Configuration"
		capability "Switch"
        // capability "Temperature Measurement" // not used as dosnt report back
        capability "Thermostat Cooling Setpoint"	//attribute- coolingSetpoint this alows extra settings in routines
        capability "Thermostat Heating Setpoint" 
        
		command "temperatureUp"
		command "temperatureDown"
        command "summer"
       	command "emergencyHeat"
		
        attribute "summer", "String" //for feed
        attribute "nextHeatingSetpoint", "number"
        attribute "battery", "string"
        	// attribute "temperature", "number" // not used as dosnt report back
		  
		fingerprint type: "0804", mfr: "0002", prod: "0005", model: "0004", cc: "80,46,81,72,8F,75,43,86,84,8F"	//, ccOut:"46,81,8F"
		
/*   Refeance data
raw fingerprint zw:S type:0804 mfr:0002 prod:0005 model:0004 ver:1.01 zwv:3.67 lib:06 cc:80,46,81,72,8F,75,43,86,84 ccOut:46,81,8F

		0x31 V2 0x31 Sensor Multilevel - not used this device dosnt report temp			0x43 = Thermostat Setpoint v2
		0x46 = Climate Control Schedule v1												0x72 = Manufacturer Specific v1
        0x75 = Protection v2															0x80 = Battery v1	
        0x81 = Clock v1																	0x84 = Wake Up v2
        0x86 = Version v1																0x8F = Multi Cmd v1 (Multi Command Encapsulated)
*/        
	}

	simulator {
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"temperature", type:"thermostat", width:6, height:4, canChangeIcon: true) {
// delta, devation
            tileAttribute("device.nextHeatingSetpoint", key: "PRIMARY_CONTROL") { /// next heating setpoint insted of temparture as this divice dosnt report temp
// delta, devation
                attributeState ("temperature", label:'${currentValue}°', defaultState: true, backgroundColors:[
					// Celsius setpoint temp colour range
                    [value: 0, color: "#b8c2de"], [value: 10, color: "#bbe1ea"], [value: 13, color: "#ddf1e4"],	[value: 17, color: "#c6e9bc"], 
                    [value: 20, color: "#faf3b2"], [value: 25, color: "#f0c9b2"], [value: 29, color: "#eabdbd"],
                    // Fahrenheit setpoint temp colour range
					[value: 40, color: "#b8c2de"], [value: 44, color: "#bbe1ea"], [value: 59, color: "#ddf1e4"], [value: 74, color: "#c6e9bc"],
					[value: 84, color: "#faf3b2"], [value: 95, color: "#f0c9b2"], [value: 96, color: "#eabdbd"]
				/* taken out as use setpoint colours
                    // Celsius actual temp colour range
					[value: 0, color: "#153591"], [value: 10, color: "#1e9cbb"], [value: 13, color: "#90d2a7"],	[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"], [value: 25, color: "#d04e00"],	[value: 29, color: "#bc2323"],
					// Fahrenheit actual temp colour range
					[value: 40, color: "#153591"],	[value: 44, color: "#1e9cbb"],	[value: 59, color: "#90d2a7"],	[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],	[value: 92, color: "#d04e00"],	[value: 96, color: "#bc2323"]
				*/
				])
			}
			tileAttribute("device.nextHeatingSetpoint", key: "VALUE_CONTROL") {
				attributeState ("VALUE_UP", action: "temperatureUp")
				attributeState ("VALUE_DOWN", action: "temperatureDown")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
            	attributeState ("default",			icon:"st.thermostat.auto")
            	attributeState ("idle",				backgroundColor:"#00A0DC", icon:"st.thermostat.heating-cooling-off")
                attributeState ("heating",  		backgroundColor:"#e86d13", icon:"st.thermostat.heat")
				attributeState ("emergencyHeat", 	backgroundColor:"#FF0000", icon:"st.thermostat.emergency-heat")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState ("off", 				label:"off", 		icon:"st.thermostat.heating-cooling-off")
                attributeState ("heat", 			label:"heat", 		icon:"st.thermostat.heat")
				attributeState ("emergencyHeat", 	label:"boosting",	icon:"st.thermostat.emergency-heat")
			}
            tileAttribute("device.thermostatSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"°C", defaultState: true, backgroundColors:[
					// Celsius setpoint temp colour range
				[value: 0, color: "#b8c2de"],
				[value: 10, color: "#bbe1ea"],
				[value: 13, color: "#ddf1e4"],
				[value: 17, color: "#c6e9bc"],
				[value: 20, color: "#faf3b2"],
				[value: 25, color: "#f0c9b2"],
				[value: 29, color: "#eabdbd"],
					// Fahrenheit setpoint temp colour range
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
	
    standardTile("switcher", "device.switch", height: 2, width: 2, decoration: "flat", inactiveLabel: true) {
		state "off", action:"on", label: "off", icon: "st.thermostat.cool", backgroundColor:"#ffffff" 
		state "on", action:"off", label: "on", icon: "st.thermostat.heat", backgroundColor:"#ffa81e"
	}

/* delta, devation Not used as dosent report temp
	valueTile("temp", "device.temperature", width: 2, height: 2, inactiveLabel: true) { // hear to enable it to show in activity feed
		state ("temp", label:'${currentValue}°', defaultState: true, backgroundColors:[
			// Celsius actual temp colour range
				[value: 0, color: "#153591"], [value: 10, color: "#1e9cbb"], [value: 13, color: "#90d2a7"],	[value: 17, color: "#44b621"],
				[value: 20, color: "#f1d801"], [value: 25, color: "#d04e00"], [value: 29, color: "#bc2323"],
                // Fahrenheit actual temp colour range
				[value: 40, color: "#153591"], [value: 44, color: "#1e9cbb"], [value: 59, color: "#90d2a7"], [value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],	[value: 92, color: "#d04e00"], 	[value: 96, color: "#bc2323"]])	}
delta, devation */
                
	standardTile("thermostatMode", "device.thermostatMode", height: 2, width: 2, decoration: "flat") {
		state "off", 			label: "Press for \nHeat / Emergency heat", action:"heat", 			icon: "st.thermostat.heating-cooling-off"
		state "heat", 			label: "Press for \nEmergency heat / Off", 	action:"emergencyHeat", icon: "st.thermostat.heat"
        state "emergencyHeat", 	label: "Press for \nOff / Heat", 			action: "off", 			icon: "st.thermostat.emergency-heat"
	}
    
    standardTile("summer", "device.summer", height: 2, width: 2, decoration: "flat") {
		state "off", 	label: "Press for \nSummer", 		action:"summer", 	icon: "st.thermostat.auto", backgroundColor:"#d3d3d3"
		state "on", 	label: "Press to turn\n off summer", action:"summer", 	icon: "st.custom.wuk.clear"
	}   

	main "temperature"
	details(["temperature", "summer", "battery", "thermostatMode"]) //delta, devation this is not used as no reporting "temp"
	}

	preferences {
		input "wakeUpIntervalInMins", "number", title: "Wake Up Interval (min). Default 5mins.", description: "Wakes up and send\receives new temperature setting", range: "1..30", displayDuringSetup: true
		input "quickOnTemperature", "number", title: "Quick On Temperature. Default 21°C.", description: "Quickly turn on the radiator to this temperature", range: "5..82", displayDuringSetup: false
		input "quickOffTemperature", "number", title: "Quick Off Temperature. Default 4°C.", description: "Quickly turn off the radiator to this temperature", range: "4..68", displayDuringSetup: false
		input "quickemergencyTemperature", "number", title: "Quick emergency Heat Temperature. Default 25°C.", description: "Quickly boost the radiator to this temperature", range: "4..68", displayDuringSetup: false
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

def parse(String description) {		// All messages from the device are passed to the parse method. It is responsible for turning those messages into something the SmartThings platform can understand.
	def result = null
	//	The commands in the array are to map to versions of the command class. eg physicalgraph.zwave.commands.wakeupv1 vs physicalgraph.zwave.commands.wakeupv2
	//	If none specified, it'll use the latest version of that command class.
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
		log.info "Parsed '${cmd}'" // to ${result.inspect()}" //result inspect shows all the decoded details removed to keep debugin tidye
	}
	else {
		log.warn "Non-parsed event: ${description} --- cmd = '${cmd}'"
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {	//	catch all unhandled events
	log.warn "Uncaptured/unhandled event for ${device.displayName}: ${cmd}"
	return createEvent(descriptionText: "Uncaptured event for ${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.climatecontrolschedulev1.ScheduleOverrideReport cmd) {
	//log.debug "Schedule Override Report ${device.displayName} ${cmd} key = OVERRIDE_STATE_NO_OVERRIDE = 0"
	// dont actualy do anything with this message and messages are autmoticly retunred //return createEvent(descriptionText: "ScheduleOverrideReport ${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd) {
	log.debug "Wake Up Interval Report recived: ${cmd.toString()}" //not used as the interaval is not asked for
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd) { //	Parsed ThermostatSetpointReport(precision: 2, reserved01: 0, scale: 0, scaledValue: 21.00, setpointType: 1, size: 2, value: [8, 52])
    state.scale = cmd.scale // So we can respond with same format later, see setHeatingSetpoint()		
	state.precision = cmd.precision

	def eventList = []
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	def radiatorTemperature = Double.parseDouble(convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)).round(1)
	def currentTemperature = currentDouble("heatingSetpoint")
	def nextTemperature = currentDouble("nextHeatingSetpoint")
    def discText = ""

	if(radiatorTemperature != currentTemperature){	// The radiator set point temperature has changed. Why?
		if(state.lastSentTemperature == radiatorTemperature) {	//by app
			discText = "Temperature changed by app to ${radiatorTemperature}°" + getTemperatureScale() + "."
        	log.debug "ThermostatSetpointReport - '${discText}'"
        }
		else {	//manually
        	discText = "Temperature changed manually to ${radiatorTemperature}°" + getTemperatureScale() + "."
			log.debug "ThermostatSetpointReport -'${discText}'"
            if(state.summer == "on") {
            	state.summer = "off"
                sendEvent(name: "summer", value: state.summer, descriptionText: "summer mode off, temp change commands will processed as normall")
            }
			state.lastSentTemperature = radiatorTemperature
            eventList << createEvent(name:"nextHeatingSetpoint", value: radiatorTemperature, unit: getTemperatureScale(), displayed: false)
		}
	}
    
	if(nextTemperature == 0) { //	initialise the nextHeatingSetpoint, on the very first time we install and get the devices temp
		state.lastSentTemperature = radiatorTemperature
        eventList << createEvent(name:"nextHeatingSetpoint", value: state.lastSentTemperature, unit: getTemperatureScale(), displayed: false)
	}
	eventList << createEvent(name: "heatingSetpoint", value: radiatorTemperature, unit: getTemperatureScale(), displayed: false, descriptionText:discText)
	eventList << createEvent(name: "thermostatSetpoint", value: radiatorTemperature, unit: getTemperatureScale(), descriptionText:discText)
	eventList << onOffEvent(radiatorTemperature)
        
	eventList
}

def onOffEvent(Double degrees) { //set all buttons up depending on temp
    def switchV = ''
    def thermostatModeV = ''
    def thermostatOperatingStateV = ''
    def onTemperature = quickOnTemperature ?: fromCelsiusToLocal(21)
	def offTemperature = quickOffTemperature ?: fromCelsiusToLocal(4)
    def emgTemperature = quickemergencyTemperature ?:fromCelsiusToLocal(25)
    
    if(degrees == offTemperature) {
        switchV = "off"
        thermostatModeV = "off"
        thermostatOperatingStateV = "idle"
    }
    else if (degrees >= emgTemperature && degrees < 28) { //28 is summer mode so dont want to include
        switchV = "on"
        thermostatModeV = "emergencyHeat"
        thermostatOperatingStateV = "emergencyHeat"
    }
    else {
    	switchV = "on"
    	thermostatModeV = "heat"
    	thermostatOperatingStateV = "heating"
    }
    log.debug "on off event with value ${degrees} , ${thermostatModeV}, ${thermostatOperatingStateV}, ${switchV}"
    sendEvent(name: "switch", value: switchV, displayed: false)
	sendEvent(name: "thermostatMode", value: thermostatModeV, descriptionText: "Thermostat mode is changed to $thermostatModeV")
	sendEvent(name: "thermostatOperatingState", value: thermostatOperatingStateV, descriptionText: "Thermostat Operating state is changed to $thermostatOperatingStateV") //, displayed: false
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def event = createEvent(name: "wake up", value: "${new Date().time}", descriptionText: "${device.displayName} woke up", displayed: false)
	def cmds = []

//battery - get/ask
	if (!state.lastBatteryReportReceivedAt || (new Date().time) - state.lastBatteryReportReceivedAt > daysToTime(7)) {
		log.trace "WakeUp - Asking for battery report as over 7 days since"
		cmds << zwave.batteryV1.batteryGet().format()
	}
//time - set
    if (!state.lastClockSet || (new Date().time) - state.lastClockSet > daysToTime(7)) {
		log.trace "WakeUp - Updating Clock as 7 days since"
        cmds << currentTimeCommand()
	}
// Wake Up Intval - set
    if (state.configrq == true) {
    	log.trace "WakeUp - Sending - wakeUpIntervalSet='${state.wakeUpEvery}'s or '${state.wakeUpEvery/60}'min, this normally takes a full cycle to come into effect"
    	cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds:state.wakeUpEvery, nodeid:zwaveHubNodeId).format()
        state.configrq = false
    }
// temperature - set and then get
	def nextHeatingSetpoint = currentDouble("nextHeatingSetpoint")
	def heatingSetpoint = currentDouble("heatingSetpoint")
	def thermostatMode = device.currentValue("thermostatMode")

	if (nextHeatingSetpoint != 0 && nextHeatingSetpoint != heatingSetpoint) {
		state.lastSentTemperature = nextHeatingSetpoint
        log.trace "WakeUp - Sending new temperature ${nextHeatingSetpoint}, curent heating setpoint ${heatingSetpoint}"
		cmds << setHeatingSetpointCommand(nextHeatingSetpoint).format()
        cmds << "delay 2000"
		cmds << zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format()
	}
// no more - set - go back to sleep
	cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	    //log.debug "WakeUp - commands are ${cmds}"
	[event, response(delayBetween(cmds, 50))]
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
	state.lastBatteryReportReceivedAt = new Date().time // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
    createEvent(map)
}

//	commands (that it can handle, must implement those that match it's capabilities, so SmartApps can call these methods)
//	TODO: review the commands, do they have the right interface/params

def temperatureUp() { // TODO: deal with Farenheit?
	def nextTemp = currentDouble("nextHeatingSetpoint") + 0.5d
	if(nextTemp > fromCelsiusToLocal(28)) {	// It can't handle above 28, so don't allow it go above
		nextTemp = fromCelsiusToLocal(28)
	}
	setHeatingSetpoint(nextTemp)
}

def temperatureDown() {
	def nextTemp = currentDouble("nextHeatingSetpoint") - 0.5d
	if(nextTemp < fromCelsiusToLocal(4)) {	// It can't go below 4, so don't allow it
		nextTemp = fromCelsiusToLocal(4)
	}
	setHeatingSetpoint(nextTemp)
}

//	Thermostat temp Setpoint
def setCoolingSetpoint(temp){
	log.info "Set cooling setpoint temp of ${temp}, sending temp value to setHeatingSetpoint"
	setHeatingSetpoint(temp)
}

def setHeatingSetpoint(degrees) {
	log.debug "setHeatingSetpoint(degrees) - ${degrees} - going to degrees toDouble"
	setHeatingSetpoint(degrees.toDouble())
}

def setHeatingSetpoint(Double degrees) {
	log.debug "setHeatingSetpoint(Double degrees) - Storing temperature for next wake ${degrees}"
	sendEvent(name:"nextHeatingSetpoint", value: degrees.round(1), unit: getTemperatureScale(), descriptionText: "Next heating setpoint is ${degrees}")
}

def setHeatingSetpointCommand(Double degrees) {
    if (state.summer == "on" && degrees != 28){
    	degrees = 28.0
        sendEvent(name:"nextHeatingSetpoint", value: degrees, descriptionText: "Next heating setpoint over ridden to ${degrees}")
        log.warn "temp changed to ${degrees} as in summer mode"
    }
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
    log.trace "setHeatingSetpointCommand(DD) -  ${degrees}"
	zwave.thermostatSetpointV1.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: precision, scaledValue: convertedDegrees)
}

def on() {
	log.debug "on button"
	sendEvent(name: "thermostatMode", value: "heat", descriptionText: "Thermostat mode is changed to heat")
	def temp = quickOnTemperature ?: fromCelsiusToLocal(21)
	setHeatingSetpoint(fromCelsiusToLocal(temp))
	}

def off() {
	log.debug "off button"
    def temp = quickOffTemperature ?: fromCelsiusToLocal(4)
    sendEvent(name: "thermostatMode", value: "off", descriptionText: "Thermostat mode is changed to off with $temp")
    setHeatingSetpoint(fromCelsiusToLocal(temp))
}

def heat() {
	log.debug "heat button"
	on()
}

def cool() {
	log.debug "cool button"
	off()
}

def emergencyHeat() {
	log.debug "emergcy heat button"
    def temp = quickemergencyTemperature ?: fromCelsiusToLocal(25)
	sendEvent(name: "thermostatMode", value: "emergencyHeat", descriptionText: "Thermostat mode is changed to emergencyHeat with $temp")
    setHeatingSetpoint(fromCelsiusToLocal(temp))
}   

def summer() {
	def discText = " "
    def temp = 0
	def cmds = []
    if (state.summer == "on" || state.summer == null){
    	log.info "summer is on so turn off"
        temp = quickOnTemperature ?: fromCelsiusToLocal(21)
    	discText = "summer mode off, temp change commands will processed as normall"
        state.summer = "off"
    }
    else if (state.summer == "off"){
		log.info "summer is off, turning on"
		temp = 28
    	discText = "summer mode activated, all temp change commands will be blocked and the trv will stay fully open. Mannual adjustment will turn this off"
    	state.summer = "on"
    }
    else {
    	log.warn "shouldnt go here"
        temp = 20
        state.summer = "off"
    	discText = "some thing went wroung in summer mode"
    }
    cmds << sendEvent(name: "summer", value: state.summer, descriptionText: discText)
	cmds <<	setHeatingSetpoint(fromCelsiusToLocal(temp))
    cmds
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
	heat()
}

def installed() {
	log.debug "installed"
    state.configrq = false
    delayBetween([
		zwave.configurationV1.configurationSet(parameterNumber:1, size:2, scaledConfigurationValue:100).format(), // Not sure if this is needed
		zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: 1).format(), // Get it's configured info, like it's scale (Celsius, Farenheit) Precicsion // 1 = SETPOINT_TYPE_HEATING_1
		zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(),
		currentTimeCommand(), // set the time and day number on device
		zwave.wakeUpV1.wakeUpIntervalSet(seconds:300, nodeid:zwaveHubNodeId).format()  //Make sure sleepy battery-powered sensor sends its WakeUpNotifications to the hub every 5 mins intially
	], 1000)
}

def updated() {
	log.debug("updated")
	configure()
}

def configure() {
	state.configrq = true
	def wakeUpEvery = (wakeUpIntervalInMins ?: 5) * 60
	state.wakeUpEvery = wakeUpEvery
    if (state.lastClockSet == null){
    	state.lastClockSet = new Date().time
    	log.warn "last clock set time was null"
    }
    if (state.lastBatteryReportReceivedAt == null){
    	state.lastBatteryReportReceivedAt = new Date().time
        log.warn "last battery set time was null"
    }
    log.debug "Configure - storing wakeUpInterval for next wake '$state.wakeUpEvery'seconds AND configuration flag is $state.configrq AND current stateClock set is $state.lastClockSet"
}

private daysToTime(days) {
	days*24*60*60*1000
}

private fromCelsiusToLocal(Double degrees) {
	if(getTemperatureScale() == "F") {
		return celsiusToFahrenheit(degrees)
	}
	else {
		return degrees
	}
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

private currentDouble(attributeName) {
	if(device.currentValue(attributeName)) {
		return device.currentValue(attributeName).doubleValue()
	}
	else {
		return 0d
	}
}