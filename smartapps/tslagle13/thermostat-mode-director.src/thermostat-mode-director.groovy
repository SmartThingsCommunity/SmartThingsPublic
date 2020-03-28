/**
 *  Thermostat Mode Director
 *  Source: https://github.com/tslagle13/SmartThings/blob/master/Director-Series-Apps/Thermostat-Mode-Director/Thermostat%20Mode%20Director.groovy
 *
 *  Version 3.0
 *
 *  Changelog:
 *	2015-05-25
 *		--Updated UI to make it look pretty.
 *	2015-06-01
 *  	--Added option for modes to trigger thermostat boost.
 *
 *  Source code can be found here: https://github.com/tslagle13/SmartThings/blob/master/smartapps/tslagle13/vacation-lighting-director.groovy
 *
 *  Copyright 2015 Tim Slagle
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

// Automatically generated. Make future change here.
definition(
	name: "Thermostat Mode Director",
	namespace: "tslagle13",
	author: "Tim Slagle",
	description: "Changes mode of your thermostat based on the temperature range of a specified temperature sensor and shuts off the thermostat if any windows/doors are open.",
	category: "Green Living",
	iconUrl: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",
	iconX2Url: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",
	pausable: true
)

preferences {
    page(name:"pageSetup")
    page(name:"directorSettings")
    page(name:"ThermostatandDoors")
    page(name:"ThermostatBoost")
    page(name:"Settings")
    page(name: "timeIntervalInput")
}

// Show setup page
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

	return dynamicPage(pageProperties) {
    	section("About 'Thermostat Mode Director'"){
        	paragraph "Changes mode of your thermostat based on the temperature range of a specified temperature sensor and shuts off the thermostat if any windows/doors are open."
        }
        section("Setup Menu") {
            href "directorSettings", title: "Director Settings", description: "", state:greyedOut()
            href "ThermostatandDoors", title: "Thermostat and Doors", description: "", state: greyedOutTherm()
            href "ThermostatBoost", title: "Thermostat Boost", description: "", state: greyedOutTherm1()
            href "Settings", title: "Settings", description: "", state: greyedOutSettings()
            }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show "Setup" page
def directorSettings() {

    def sensor = [
        name:       "sensor",
        type:       "capability.temperatureMeasurement",
        title:      "Which?",
        multiple:   false,
        required:   true
    ]
    def setLow = [
        name:       "setLow",
        type:       "decimal",
        title:      "Low temp?",
        required:   true
    ]

    def cold = [
        name:       "cold",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]

    def setHigh = [
        name:       "setHigh",
        type:       "decimal",
        title:      "High temp?",
        required:   true
    ]

    def hot = [
        name:       "hot",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]

    def neutral = [
        name:       "neutral",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]

    def pageName = "Setup"

    def pageProperties = [
        name:       "directorSettings",
        title:      "Setup",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

		section("Which temperature sensor will control your thermostat?"){
			input sensor
		}
        section(""){
        	paragraph "Here you will setup the upper and lower thresholds for the temperature sensor that will send commands to your thermostat."
        }
		section("When the temperature falls below this tempurature set mode to..."){
			input setLow
			input cold
		}
        section("When the temperature goes above this tempurature set mode to..."){
			input setHigh
			input hot
		}
        section("When temperature is between the previous temperatures, change mode to..."){
			input neutral
		}
    }

}

def ThermostatandDoors() {

    def thermostat = [
        name:       "thermostat",
        type:       "capability.thermostat",
        title:      "Which?",
        multiple:   true,
        required:   true
    ]
    def doors = [
        name:       "doors",
        type:       "capability.contactSensor",
        title:      "Low temp?",
        multiple:	true,
        required:   true
    ]

    def turnOffDelay = [
        name:       "turnOffDelay",
        type:       "decimal",
        title:		"Number of minutes",
        required:	false
    ]

    def pageName = "Thermostat and Doors"

    def pageProperties = [
        name:       "ThermostatandDoors",
        title:      "Thermostat and Doors",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

		section(""){
        	paragraph "If any of the doors selected here are open the thermostat will automatically be turned off and this app will be 'disabled' until all the doors are closed. (This is optional)"
        }
        section("Choose thermostat...") {
			input thermostat
		}
        section("If these doors/windows are open turn off thermostat regardless of outdoor temperature") {
			input doors
		}
		section("Wait this long before turning the thermostat off (defaults to 1 minute)") {
			input turnOffDelay
		}
    }

}

def ThermostatBoost() {

    def thermostat1 = [
        name:       "thermostat1",
        type:       "capability.thermostat",
        title:      "Which?",
        multiple:   true,
        required:   true
    ]
    def turnOnTherm = [
        name: 		"turnOnTherm",
        type:		"enum",
        metadata: 	[values: ["cool", "heat"]],
        required: 	false
    ]

    def modes1 = [
        name:		"modes1",
        type:		"mode",
        title: 		"Put thermostat into boost mode when mode is...",
        multiple: 	true,
        required: 	false
    ]

    def coolingTemp = [
        name:       "coolingTemp",
        type:       "decimal",
        title:		"Cooling Temp?",
        required:	false
    ]

    def heatingTemp = [
        name:       "heatingTemp",
        type:       "decimal",
        title:		"Heating Temp?",
        required:	false
    ]

    def turnOffDelay2 = [
        name:       "turnOffDelay2",
        type:       "decimal",
        title:		"Number of minutes",
        required:	false,
        defaultValue:30
    ]

    def pageName = "Thermostat Boost"

    def pageProperties = [
        name:       "ThermostatBoost",
        title:      "Thermostat Boost",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

		section(""){
        	paragraph "Here you can setup the ability to 'boost' your thermostat. In the event that your thermostat is 'off' and you need to heat or cool your home for a little bit you can 'touch' the app in the 'My Apps' section to boost your thermostat."
        }
		section("Choose a thermostats to boost") {
   			input thermostat1
        }
        section("If thermostat is off switch to which mode?") {
    		input turnOnTherm
  		}
        section("Set the thermostat to the following temps") {
    		input coolingTemp
    		input heatingTemp
  		}
  		section("For how long?") {
    		input turnOffDelay2
  		}
        section("In addtion to 'app touch' the following modes will also boost the thermostat") {
   			input modes1
        }
    }

}

// Show "Setup" page
def Settings() {

    def sendPushMessage = [
        name: 		"sendPushMessage",
        type: 		"enum",
        title: 		"Send a push notification?",
        metadata:	[values:["Yes","No"]],
        required:	true,
        defaultValue: "Yes"
    ]

    def phoneNumber = [
        name: 		"phoneNumber",
        type:		"phone",
        title: 		"Send SMS notifications to?",
        required: 	false
    ]

    def days = [
        name:       "days",
        type:       "enum",
        title:      "Only on certain days of the week",
        multiple:   true,
        required:   false,
        options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    ]

    def modes = [
        name:		"modes",
        type:		"mode",
        title: 		"Only when mode is",
        multiple: 	true,
        required: 	false
    ]

    def pageName = "Settings"

    def pageProperties = [
        name:       "Settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {


		section( "Notifications" ) {
			input sendPushMessage
			if (settings.phoneNumber) {
				input phoneNumber
			}
		}
		section(title: "More options", hideable: true) {
			href "timeIntervalInput", title: "Only during a certain time", description: getTimeLabel(starting, ending), state: greyedOutTime(starting, ending), refreshAfterSelection:true
			input days
			input modes
		}
    }

}

def installed(){
	init()
}

def updated(){
	unsubscribe()
	init()
}

def init(){
	state.lastStatus = null
	subscribe(app, appTouch)
    runIn(60, "temperatureHandler")
    subscribe(sensor, "temperature", temperatureHandler)
    if(modes1){
    	subscribe(location, modeBoostChange)
    }
	if(doors){
		subscribe(doors, "contact.open", temperatureHandler)
        subscribe(doors, "contact.closed", doorCheck)
	}
}

def temperatureHandler(evt) {
	if(modeOk && daysOk && timeOk) {
		if(setLow > setHigh){
			def temp = setLow
			setLow = setHigh
			setHigh = temp
		}
		if (doorsOk) {
			def currentTemp = sensor.latestValue("temperature")
			if (currentTemp < setLow) {
            	if (state.lastStatus == "two" || state.lastStatus == "three" || state.lastStatus == null){
					//log.info "Setting thermostat mode to ${cold}"
					def msg = "I changed your thermostat mode to ${cold} because temperature is below ${setLow}"
					thermostat?."${cold}"()
                    sendMessage(msg)
                    }
				state.lastStatus = "one"
			}
			if (currentTemp > setHigh) {
            	if (state.lastStatus == "one" || state.lastStatus == "three" || state.lastStatus == null){
					//log.info "Setting thermostat mode to ${hot}"
					def msg = "I changed your thermostat mode to ${hot} because temperature is above ${setHigh}"
					thermostat?."${hot}"()
					sendMessage(msg)
				}
				state.lastStatus = "two"
			}
			if (currentTemp > setLow && currentTemp < setHigh) {
            	if (state.lastStatus == "two" || state.lastStatus == "one" || state.lastStatus == null){
					//log.info "Setting thermostat mode to ${neutral}"
					def msg = "I changed your thermostat mode to ${neutral} because temperature is neutral"
					thermostat?."${neutral}"()
					sendMessage(msg)
				}
				state.lastStatus = "three"
			}
		}
        else{
			def delay = (turnOffDelay != null && turnOffDelay != "") ? turnOffDelay * 60 : 60
			log.debug("Detected open doors.  Checking door states again")
			runIn(delay, "doorCheck")
		}
	}
}

def appTouch(evt) {
if(thermostat1){
	state.lastStatus = "disabled"
	def currentCoolSetpoint = thermostat1.latestValue("coolingSetpoint") as String
    def currentHeatSetpoint = thermostat1.latestValue("heatingSetpoint") as String
    def currentMode = thermostat1.latestValue("thermostatMode") as String
	def mode = turnOnTherm
    state.currentCoolSetpoint1 = currentCoolSetpoint
    state.currentHeatSetpoint1 = currentHeatSetpoint
    state.currentMode1 = currentMode

    	thermostat1."${mode}"()
    	thermostat1.setCoolingSetpoint(coolingTemp)
    	thermostat1.setHeatingSetpoint(heatingTemp)

    thermoShutOffTrigger()
    //log.debug("current coolingsetpoint is ${state.currentCoolSetpoint1}")
    //log.debug("current heatingsetpoint is ${state.currentHeatSetpoint1}")
    //log.debug("current mode is ${state.currentMode1}")
}
}

def modeBoostChange(evt) {
	if(thermostat1 && modes1.contains(location.mode)){
		state.lastStatus = "disabled"
		def currentCoolSetpoint = thermostat1.latestValue("coolingSetpoint") as String
    	def currentHeatSetpoint = thermostat1.latestValue("heatingSetpoint") as String
    	def currentMode = thermostat1.latestValue("thermostatMode") as String
		def mode = turnOnTherm
    	state.currentCoolSetpoint1 = currentCoolSetpoint
    	state.currentHeatSetpoint1 = currentHeatSetpoint
    	state.currentMode1 = currentMode

    		thermostat1."${mode}"()
    		thermostat1.setCoolingSetpoint(coolingTemp)
    		thermostat1.setHeatingSetpoint(heatingTemp)

    	log.debug("current coolingsetpoint is ${state.currentCoolSetpoint1}")
    	log.debug("current heatingsetpoint is ${state.currentHeatSetpoint1}")
    	log.debug("current mode is ${state.currentMode1}")
	}
	else{
		thermoShutOff()
    }
}

def thermoShutOffTrigger() {
    //log.info("Starting timer to turn off thermostat")
    def delay = (turnOffDelay2 != null && turnOffDelay2 != "") ? turnOffDelay2 * 60 : 60
    state.turnOffTime = now()
	log.debug ("Turn off delay is ${delay}")
    runIn(delay, "thermoShutOff")
  }

def thermoShutOff(){
	if(state.lastStatus == "disabled"){
		def coolSetpoint = state.currentCoolSetpoint1
    	def heatSetpoint = state.currentHeatSetpoint1
		def mode = state.currentMode1
    	def coolSetpoint1 = coolSetpoint.replaceAll("\\]", "").replaceAll("\\[", "")
    	def heatSetpoint1 = heatSetpoint.replaceAll("\\]", "").replaceAll("\\[", "")
    	def mode1 = mode.replaceAll("\\]", "").replaceAll("\\[", "")

		state.lastStatus = null
		//log.info("Returning thermostat back to normal")
		thermostat1.setCoolingSetpoint("${coolSetpoint1}")
    	thermostat1.setHeatingSetpoint("${heatSetpoint1}")
    	thermostat1."${mode1}"()
    	temperatureHandler()
    }
}

def doorCheck(evt){
	if (!doorsOk){
		log.debug("doors still open turning off ${thermostat}")
		def msg = "I changed your thermostat mode to off because some doors are open"

        if (state.lastStatus != "off"){
        	thermostat?.off()
			sendMessage(msg)
		}
		state.lastStatus = "off"
	}

	else{
    	if (state.lastStatus == "off"){
			state.lastStatus = null
        }
        temperatureHandler()
	}
}

private sendMessage(msg){
	if (sendPushMessage == "Yes") {
		sendPush(msg)
	}
	if (phoneNumber != null) {
		sendSms(phoneNumber, msg)
	}
}

private getAllOk() {
	modeOk && daysOk && timeOk && doorsOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDoorsOk() {
	def result = !doors || !doors.latestValue("contact").contains("open")
	log.trace "doorsOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}

    else if (starting){
    	result = currTime >= start
    }
    else if (ending){
    	result = currTime <= stop
    }

	log.trace "timeOk = $result"
	result
}

def getTimeLabel(starting, ending){

	def timeLabel = "Tap to set"

    if(starting && ending){
    	timeLabel = "Between" + " " + hhmm(starting) + " "  + "and" + " " +  hhmm(ending)
    }
    else if (starting) {
		timeLabel = "Start at" + " " + hhmm(starting)
    }
    else if(ending){
    timeLabel = "End at" + hhmm(ending)
    }
	timeLabel
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}
def greyedOut(){
	def result = ""
    if (sensor) {
    	result = "complete"
    }
    result
}

def greyedOutTherm(){
	def result = ""
    if (thermostat) {
    	result = "complete"
    }
    result
}

def greyedOutTherm1(){
	def result = ""
    if (thermostat1) {
    	result = "complete"
    }
    result
}

def greyedOutSettings(){
	def result = ""
    if (starting || ending || days || modes || sendPushMessage) {
    	result = "complete"
    }
    result
}

def greyedOutTime(starting, ending){
	def result = ""
    if (starting || ending) {
    	result = "complete"
    }
    result
}

private anyoneIsHome() {
  def result = false

  if(people.findAll { it?.currentPresence == "present" }) {
    result = true
  }

  log.debug("anyoneIsHome: ${result}")

  return result
}

def timeIntervalInput() {
    dynamicPage(name: "timeIntervalInput", title: "Only during a certain time", refreshAfterSelection:true) {
        section("") {
			input "starting", "time", title: "Starting", multiple: false, required: ending != null, submitOnChange: true
			input "ending", "time", title: "Ending", multiple: false, required: starting != null, submitOnChange: true
		}
    }
}
