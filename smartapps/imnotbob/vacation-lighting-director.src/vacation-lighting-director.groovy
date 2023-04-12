/**
 *	Vacation Lighting Director  (based off of tslagle's original)
 *	Supports Longer interval times (up to 180 mins)
 *	Only turns off lights it turned on (vs calling to turn all off)
 * 
 *      Updated to turn on a set of lights during active time, and turn them off at end of vacation time
 *
 *	Source code can be found here:
 *  https://github.com/imnotbob/vacation-lighting-director/blob/master/smartapps/imnotbob/vacation-lighting-director.src/vacation-lighting-director.groovy
 *
 *	Copyright 2017 Eric Schott
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

import java.text.SimpleDateFormat

// Automatically generated. Make future change here.
definition(
	name: "Vacation Lighting Director",
	namespace: "imnotbob",
	author: "ERS",
	category: "Safety & Security",
	description: "Randomly turn on/off lights to simulate the appearance of a occupied home while you are away.",
	iconUrl: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png",
	iconX2Url: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png"
)

preferences {
	page(name:"pageSetup")
	page(name:"Setup")
	page(name:"Settings")
	page(name:"timeIntervalPage")

}

// Show setup page
def pageSetup() {

	def pageProperties = [
		name:		"pageSetup",
		title:		"Status",
		nextPage:	null,
		install:	true,
		uninstall:	true
	]

	return dynamicPage(pageProperties) {
		section(""){
			paragraph "This app can be used to make your home seem occupied anytime you are away from your home. " +
				"Please use each of the the sections below to setup the different preferences to your liking. "
		}
		section("Setup Menu") {
			href "Setup", title: "Setup", description: "", state:greyedOut()
			href "Settings", title: "Settings", description: "", state: greyedOutSettings()
		}
		section([title:"Options", mobileOnly:true]) {
			label title:"Assign a name", required:false
		}
	}
}

// Show "Setup" page
def Setup() {

	def newMode = [
		name:				"newMode",
		type:				"mode",
		title:				"Modes",
		multiple:			true,
		required:			true
	]
	def switches = [
		name:				"switches",
		type:				"capability.switch",
		title:				"Switches",
		multiple:			true,
		required:			true
	]

	def frequency_minutes = [
		name:				"frequency_minutes",
		type:				"number",
		title:				"Minutes? (5-180)",
		range:				"5..180",
		required:			true
	]

	def number_of_active_lights = [
		name:				"number_of_active_lights",
		type:				"number",
		title:				"Number of active lights",
		required:			true,
	]

	def on_during_active_lights = [
		name:				"on_during_active_lights",
		type:				"capability.switch",
		title:				"On during active times",
		multiple:			true,
		required:			false
	]

	def pageName = "Setup"

	def pageProperties = [
		name:		"Setup",
		title:		"Setup",
		nextPage:	"pageSetup"
	]

	return dynamicPage(pageProperties) {

		section(""){
			paragraph "In this section you need to setup the deatils of how you want your lighting to be affected while " +
				"you are away.	All of these settings are required in order for the simulator to run correctly."
		}
		section("Simulator Triggers") {
			input newMode
			href "timeIntervalPage", title: "Times", description: timeIntervalLabel()    //, refreshAfterSelection:true
		}
		section("Light switches to cycle on/off") {
			input switches
		}
		section("How often to cycle the lights") {
			input frequency_minutes
		}
		section("Number of active lights at any given time") {
			input number_of_active_lights
		}
		section("Lights to be on during active times?") {
			input on_during_active_lights
		}
	}
}

// Show "Setup" page
def Settings() {

	def falseAlarmThreshold = [
		name:		"falseAlarmThreshold",
		type:		"decimal",
		title:		"Default is 2 minutes",
		required:	false
	]
	def days = [
		name:		"days",
		type:		"enum",
		title:		"Only on certain days of the week",
		multiple:	true,
		required:	false,
		options:	["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
	]

	def pageName = "Settings"

	def pageProperties = [
		name:		"Settings",
		title:		"Settings",
		nextPage:	"pageSetup"
	]

	def people = [
		name:		"people",
		type:		"capability.presenceSensor",
		title:		"If these people are home do not change light status",
		required:	false,
		multiple:	true
	]

	return dynamicPage(pageProperties) {

		section(""){
			paragraph "In this section you can restrict how your simulator runs.	For instance you can restrict on which days it will run " +
				"as well as a delay for the simulator to start after it is in the correct mode.	Delaying the simulator helps with false starts based on a incorrect mode change."
		}
		section("Delay to start simulator") {
			input falseAlarmThreshold
		}
		section("People") {
			paragraph "Not using this setting may cause some lights to remain on when you arrive home"
			input people
		}
		section("More options") {
			input days
		}
	}
}

def timeIntervalPage() {
	dynamicPage(name: "timeIntervalPage", title: "Only during a certain time") {
		section {
			input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], submitOnChange:true
			if (startTimeType in ["sunrise","sunset"]) {
				input "startTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "starting", "time", title: "Start time", required: false
			}
		}
		section {
			input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], submitOnChange:true
			if (endTimeType in ["sunrise","sunset"]) {
				input "endTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "ending", "time", title: "End time", required: false
			}
		}
	}
}

def installed() {
	atomicState.Running = false
	atomicState.schedRunning = false
	atomicState.startendRunning = false
	initialize()
}

def updated() {
	unsubscribe();
	clearState(true)
	initialize()
}

def initialize(){
	if (newMode != null) {
		subscribe(location, modeChangeHandler)
	}
	schedStartEnd()
	if(people) {
		subscribe(people, "presence", modeChangeHandler)
	}
	log.debug "Installed with settings: ${settings}"
	setSched()
}

def clearState(turnOff = false) {
	if(turnOff && atomicState?.Running) {
		switches.off()
		atomicState.vacactive_switches = []
		if(on_during_active_lights) {
			on_during_active_lights.off()
		}
		log.trace "All OFF"
	}
	atomicState.Running = false
	atomicState.schedRunning = false
	atomicState.startendRunning = false
	atomicState.lastUpdDt = null
	unschedule()
}

def schedStartEnd() {
	if (starting != null || startTimeType != null) {
		def start = timeWindowStart(true)
		schedule(start, startTimeCheck)
		atomicState.startendRunning = true
	}
	if (ending != null || endTimeType != null) {
		def end = timeWindowStop(true)
		schedule(end, endTimeCheck)
		atomicState.startendRunning = true
	}
}

def setSched() {
	atomicState.schedRunning = true
/*
	def maxMin = 60
	def timgcd = gcd([frequency_minutes, maxMin])
	atomicState.timegcd = timgcd
	def random = new Random()
	def random_int = random.nextInt(60)
	def random_dint = random.nextInt(timgcd.toInteger())

	def newDate = new Date()
	def curMin = newDate.format("m", getTimeZone())

	def timestr = "${random_dint}/${timgcd}"
	if(timgcd == 60) { timestr = "${curMin}" }

	log.trace "scheduled using Cron (${random_int} ${timestr} * 1/1 * ? *)"
	schedule("${random_int} ${timestr} * 1/1 * ? *", scheduleCheck)	// this runs every timgcd minutes
*/
	def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 2 * 60
	runIn(delay, initCheck)
}

private gcd(a, b) {
	while (b > 0) {
		long temp = b;
		b = a % b;
		a = temp;
	}
	return a;
}

private gcd(input = []) {
	long result = input[0];
	for(int i = 1; i < input.size; i++) result = gcd(result, input[i]);
	return result;
}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler ${evt}"
	setSched()
}

def initCheck() {
	scheduleCheck(null)
}

def failsafe() {
	scheduleCheck(null)
}

def startTimeCheck() {
	log.trace "startTimeCheck"
	setSched()
}

def endTimeCheck() {
	log.trace "endTimeCheck"
	scheduleCheck(null)
}

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def formatDt(dt) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		log.warn "SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save..."
	}
	return tf.format(dt)
}

def GetTimeDiffSeconds(lastDate) {
	if(lastDate?.contains("dtNow")) { return 10000 }
	def now = new Date()
	def lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", lastDate)
	def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(lastDt)).getTime()
	def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(now)).getTime()
	def diff = (int) (long) (stop - start) / 1000
	return diff
}

def getTimeZone() {
	def tz = null
	if (location?.timeZone) { tz = location?.timeZone }
	if(!tz) { log.warn "getTimeZone: SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save..." }
	return tz
}

def getLastUpdSec() { return !atomicState?.lastUpdDt ? 100000 : GetTimeDiffSeconds(atomicState?.lastUpdDt).toInteger() }

//Main logic to pick a random set of lights from the large set of lights to turn on and then turn the rest off

def scheduleCheck(evt) {
	if(allOk && getLastUpdSec() > ((frequency_minutes - 1) * 60) ) {
		atomicState?.lastUpdDt = getDtNow()
		log.debug("Running")
		atomicState.Running = true

		// turn off switches
		def inactive_switches = switches
		def vacactive_switches = []
		if (atomicState.Running) {
			if (atomicState?.vacactive_switches) {
				vacactive_switches = atomicState.vacactive_switches
				if (vacactive_switches?.size()) {
					for (int i = 0; i < vacactive_switches.size() ; i++) {
						inactive_switches[vacactive_switches[i]].off()
						log.trace "turned off ${inactive_switches[vacactive_switches[i]]}"
					}
				}
			}
			atomicState.vacactive_switches = []
		}

		def random = new Random()
		vacactive_switches = []
		def numlight = number_of_active_lights
		if (numlight > inactive_switches.size()) { numlight = inactive_switches.size() }
		log.trace "inactive switches: ${inactive_switches.size()} numlight: ${numlight}"
		for (int i = 0 ; i < numlight ; i++) {

			// grab a random switch to turn on
			def random_int = random.nextInt(inactive_switches.size())
			while (vacactive_switches?.contains(random_int)) {
				random_int = random.nextInt(inactive_switches.size())
			}
			vacactive_switches << random_int
		}
		for (int i = 0 ; i < vacactive_switches.size() ; i++) {
			inactive_switches[vacactive_switches[i]].on()
			log.trace "turned on ${inactive_switches[vacactive_switches[i]]}"
		}
		atomicState.vacactive_switches = vacactive_switches
		//log.trace "vacactive ${vacactive_switches} inactive ${inactive_switches}"

		if(on_during_active_lights) {
			on_during_active_lights.on()
			log.trace "turned on ${on_during_active_lights}"
		}
		def delay = frequency_minutes
		def random_int = random.nextInt(14)
		log.trace "reschedule  ${delay} + ${random_int} minutes"
		runIn( (delay+random_int)*60, initCheck, [overwrite: true])
		runIn( (delay+random_int + 10)*60, failsafe, [overwrite: true])

	} else if(allOk && getLastUpdSec() <= ((frequency_minutes - 1) * 60) ) {
		log.trace "had to reschedule  ${getLastUpdSec()}, ${frequency_minutes*60}"
		runIn( (frequency_minutes*60-getLastUpdSec()), initCheck, [overwrite: true])
	} else if(people && someoneIsHome){
		//don't turn off lights if anyone is home
		if (atomicState?.schedRunning) {
			log.debug("Someone is home - Stopping Schedule Vacation Lights")
			clearState()
		}
	} else if (!modeOk || !daysOk) {
		if (atomicState?.Running || atomicState?.schedRunning) {
			log.debug("wrong mode or day Stopping Vacation Lights")
			clearState(true)
		}
	} else if (modeOk && daysOk && !timeOk) {
		if (atomicState?.Running || atomicState?.schedRunning) {
			log.debug("wrong time - Stopping Vacation Lights")
			clearState(true)
		}
	}
	if (!atomicState.startendRunning) {
		schedStartEnd()
	}
	return true
}

//below is used to check restrictions
private getAllOk() {
	modeOk && daysOk && timeOk && homeIsEmpty
}


private getModeOk() {
	def result = !newMode || newMode.contains(location.mode)
	//log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (getTimeZone()) {
			df.setTimeZone(getTimeZone())
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	//log.trace "daysOk = $result"
	result
}

private getHomeIsEmpty() {
	def result = true

	if(people?.findAll { it?.currentPresence == "present" }) {
		result = false
	}

	//log.debug("homeIsEmpty: ${result}")

	return result
}

private getSomeoneIsHome() {
	def result = false

	if(people?.findAll { it?.currentPresence == "present" }) {
		result = true
	}
	//log.debug("someoneIsHome: ${result}")
	return result
}

private getTimeOk() {
	def result = true
	def start = timeWindowStart()
	def stop = timeWindowStop(false, true)
	if (start && stop && getTimeZone()) {
		result = timeOfDayIsBetween( (start), (stop), new Date(), getTimeZone())
	}
	//log.debug "timeOk = $result"
	result
}

private timeWindowStart(usehhmm=false) {
	def result = null
	if (startTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (startTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (starting && getTimeZone()) {
		if(usehhmm) { result = timeToday(hhmm(starting), getTimeZone()) }
		else { result = timeToday(starting, getTimeZone()) }
	}
	//log.debug "timeWindowStart = ${result}"
	result
}

private timeWindowStop(usehhmm=false, adj=false) {
	def result = null
	if (endTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (endTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (ending && getTimeZone()) {
		if(usehhmm) { result = timeToday(hhmm(ending), getTimeZone()) }
		else { result = timeToday(ending, getTimeZone()) }
	}
	def result1
	if(adj) { // small change for schedule skewing
		result1 = new Date(result.time - (2*60*1000))
		log.debug "timeWindowStop = ${result} adjusted: ${result1}"
		result = result1
	}
	//log.debug "timeWindowStop = ${result} adjusted: ${result1}"
	result
}

private hhmm(time, fmt = "HH:mm") {
	def t = timeToday(time, getTimeZone())
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(getTimeZone() ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel() {
	def start = ""
	switch (startTimeType) {
		case "time":
			if (starting) {
				start += hhmm(starting)
			}
			break
		case "sunrise":
		case "sunset":
			start += startTimeType[0].toUpperCase() + startTimeType[1..-1]
			if (startTimeOffset) {
				start += startTimeOffset > 0 ? "+${startTimeOffset} min" : "${startTimeOffset} min"
			}
			break
	}

	def finish = ""
	switch (endTimeType) {
		case "time":
			if (ending) {
				finish += hhmm(ending)
			}
			break
		case "sunrise":
		case "sunset":
			finish += endTimeType[0].toUpperCase() + endTimeType[1..-1]
			if (endTimeOffset) {
				finish += endTimeOffset > 0 ? "+${endTimeOffset} min" : "${endTimeOffset} min"
			}
			break
	}
	start && finish ? "${start} to ${finish}" : ""
}

//sets complete/not complete for the setup section on the main dynamic page
def greyedOut(){
	def result = ""
	if (switches) {
		result = "complete"		
	}
	result
}

//sets complete/not complete for the settings section on the main dynamic page
def greyedOutSettings(){
	def result = ""
	if (people || days || falseAlarmThreshold ) {
		result = "complete"		
	}
	result
}