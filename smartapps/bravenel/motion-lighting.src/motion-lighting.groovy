/**
 *  Motion Lighting
 *
 *  Copyright 2015 Bruce Ravenel
 *
 *	11-26-2015 1.1.1
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

definition(
    name: "Motion Lighting",
    namespace: "bravenel",
    author: "Bruce Ravenel",
    description: "Set Dimmer Levels by mode, with Motion on/off/disabled, button on, and Master on/off",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps@2x.png"
)

preferences {
    page(name: "selectDimmers")
    page(name: "motionSettings")
    page(name: "otherSettings")
    page(name: "certainTime")
}

def selectDimmers() {
	dynamicPage(name: "selectDimmers", title: "Dimmers, modes and levels", nextPage: "motionSettings", uninstall: true) {

		section("When this master dimmer") { 
			input "master", "capability.switchLevel", multiple: false, title: "Is turned on", required: true, submitOnChange: true
		}

		section("These other dimmers (optional)") {
			input "slaves", "capability.switchLevel", multiple: true, title: "Will also be turned on", required: false
		}

		section("Set to a dimmer level selected for each mode") {
			def m = location.mode
			def myModes = []
			location.modes.each {myModes << "$it"}
			input "modesX", "enum", multiple: true, title: "Select mode(s)", submitOnChange: true, options: myModes.sort(), defaultValue: [m]
			def sortModes = modesX
			if(!sortModes) setModeLevel(m, "level$m")
			else sortModes = sortModes.sort()
			sortModes.each {setModeLevel(it, "level$it")}
		}
	}
}

def setModeLevel(thisMode, modeVar) {
	def result = input modeVar, "number", range: "0..100", title: "Level for $thisMode", required: true
}

def motionSettings() {
	dynamicPage(name:"motionSettings",title: "Motion sensor(s) (optional)", nextPage: "otherSettings", uninstall: true) {
    
		section("Turn them on when there is motion") {
			input "motions", "capability.motionSensor", title: "Select motion sensor(s)", required: false, multiple: true, submitOnChange: true
			if (motions) {
				input "turnOff", "bool", title: "Turn off after motion stops?", required: false, submitOnChange: true
				if(turnOff) input "minutes", "number", title: "After how many minutes?", required: false, multiple: false
			}
		}
        
		section(title: "Motion options", hidden: hideOptionsSection(), hideable: true) {

			def timeLabel = timeIntervalLabel()

			href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null

			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            
			input "disabled", "capability.switch", title: "Switch to disable motion", required: false, multiple: false
   		}    
	}
}

def otherSettings() {
	dynamicPage(name:"otherSettings", uninstall: true, install: true) {

		section("Turn master and slaves on with this") {
			input "button", "capability.momentary", title: "Button", multiple: false, required: false
		}

		section("When the master is turned off") {
			input "offSwitches", "capability.switch", title: "Turn these other switches off", multiple: true, required: false
		}
        
		section {
			label title: "Assign a name:", required: false
		}
   	}
}

def certainTime() {
	dynamicPage(name:"certainTime",title: "Only during a certain time", uninstall: false) {
		section() {
			input "startingX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false
			else {
				if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
		
		section() {
			input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false
			else {
				if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}


def initialize() {
	subscribe(master, "switch.on", switchOnHandler)
	subscribe(button, "switch.on", switchOnHandler)
	subscribe(master, "switch.off", switchOffHandler)
	subscribe(master, "level", levelHandler)
	subscribe(slaves, "level", levelHandler)
	subscribe(motions, "motion.active", motionOnHandler)
	subscribe(disabled, "switch", disableHandler)
	if(turnOff) subscribe(motions, "motion.inactive", motionOffHandler)
	subscribe(location, modeChangeHandler)
    
	state.modeLevels = [:]
	for(m in modesX) {
		def level = settings.find {it.key == "level$m"}
		state.modeLevels << [(m):level.value]
	}

	state.currentMode = location.mode in modesX ? location.mode : modesX[0]
	state.dimLevel = state.modeLevels[state.currentMode]
	state.motionOffDismissed = true
	state.motionDisabled = (disabled) ? disabled.currentSwitch == "on" : false
	state.masterOff = master.currentSwitch == "off"
}

def switchesOn() {
	state.motionOffDismissed = true    		// use this variable instead of unschedule() to kill pending off()
	state.masterOff = false
	master.setLevel(state.dimLevel)
	slaves?.setLevel(state.dimLevel)
}

def switchOnHandler(evt) {
	if(state.masterOff) switchesOn() 
}

def motionOnHandler(evt) {
	if(state.motionDisabled) return
	if(allOk && state.masterOff) switchesOn() else state.motionOffDismissed = true
}

def disableHandler(evt) {
	state.motionDisabled = evt.value == "on"
}

def levelHandler(evt) {      				// allows a dimmer to change the current dimLevel
	if(evt.value == state.dimLevel) return
	if(!state.masterOff) {					// don't adjust unless master is on	
		state.dimLevel = evt.value
		switchesOn()		
    }
}

def switchOffHandler(evt) {
	switchesOffNow()
}

def switchesOffLater() {                    		
	if(state.motionOffDismissed || state.motionDisabled) return  
	if(allOk) switchesOffNow()
}

def switchesOffNow() {
	state.dimLevel = state.modeLevels[state.currentMode]
	state.masterOff = true
	master.off()
	slaves?.off()
	offSwitches?.off()
}

def motionOffHandler(evt) {  				// called when motion goes inactive, check all sensors
	if(state.motionDisabled) return
	if(allOk) {
		def someMotion = "active" in motions.currentMotion
		state.motionOffDismissed = someMotion
		if(!someMotion) {if(minutes) runIn(minutes*60, switchesOffLater) else switchesOffNow()}
	}
}

def modeChangeHandler(evt) {
	if(state.currentMode == evt.value || !(evt.value in modesX)) return   // no change or not one of our modes
	state.currentMode = evt.value			   
	state.dimLevel = state.modeLevels[evt.value]
// the next two lines brighten any lights on when new mode is brighter than previous
	if(master.currentSwitch == "on" && master.currentLevel < state.dimLevel) master.setLevel(state.dimLevel)
	slaves.each {if(it.currentSwitch == "on" && it.currentLevel < state.dimLevel) it.setLevel(state.dimLevel)}
}

// execution filter methods
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
//	log.trace "modeOk = $result"
	return result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) df.setTimeZone(location.timeZone)
		else df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		def day = df.format(new Date())
		result = days.contains(day)
	}
//	log.trace "daysOk = $result"
	return result
}

private getTimeOk() {
	def result = true
	if ((starting && ending) ||
	(starting && endingX in ["Sunrise", "Sunset"]) ||
	(startingX in ["Sunrise", "Sunset"] && ending) ||
	(startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if(startingX == "Sunrise") start = s.sunrise.time
		else if(startingX == "Sunset") start = s.sunset.time
		else if(starting) start = timeToday(starting,location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
//	log.trace "getTimeOk = $result"
	return result
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private hideOptionsSection() {
	(starting || ending || days || modes || startingX || endingX || disabled) ? false : true
}

private offset(value) {
	def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}

private timeIntervalLabel() {
	def result = ""
	if (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")
}