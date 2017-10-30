/**
 *  Motion Lighting
 *
 *  Copyright 2015, 2016 Bruce Ravenel
 *
 *	3-28-2016 1.2
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
    description: "Set Dimmer Levels by mode, with Motion on/off/disabled, button on, and Main on/off",
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

		section("When this main dimmer") { 
			input "main", "capability.switchLevel", multiple: false, title: "Is turned on", required: true, submitOnChange: true
		}

		section("These extra dimmers (optional)") {
			input "extras", "capability.switchLevel", multiple: true, title: "Will also be turned on", required: false
		}

		section("Set to a dimmer level for each mode") {
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

		section("Turn main and extras on/off with this") {
			input "buttonSw", "capability.switch", title: "Switch", multiple: false, required: false
		}
        
        section("Turn main and extras on with this") {
        	input "buttonDev", "capability.button", title: "Button Device", required: false, multiple: false, submitOnChange: true
			if(buttonDev) {
				input "buttonNbr", "enum", title: "Button number", required: true, multiple: false, submitOnChange: true, options: ["one", "two", "three", "four"], defaultValue: "one"
        		input "buttonState", "enum", title: "Button pushed or held ", options: ["pushed", "held"], defaultValue: "pushed"
			}
        }

        section("Turn main and extras off with this") {
        	input "buttonOffDev", "capability.button", title: "Button Device", required: false, multiple: false, submitOnChange: true
			if(buttonOffDev) {
				input "buttonOffNbr", "enum", title: "Button number", required: true, multiple: false, submitOnChange: true, options: ["one", "two", "three", "four"], defaultValue: "one"
        		input "buttonOffState", "enum", title: "Button pushed or held ", options: ["pushed", "held"], defaultValue: "pushed"
			}
        }

		section("When the main is turned off") {
			input "offSwitches", "capability.switch", title: "These extra switches will be turned off", multiple: true, required: false
		}
        
		section("When the main is turned on") {
			input "onSwitches", "capability.switch", title: "These extra switches will be turned on", multiple: true, required: false
		}

		section {
			label title: "Assign a name:", required: false
		}
   	}
}

def getButton() {
//	def numNames = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
//    	"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"]
 	def result = input "buttonDev", "capability.button", title: "Button Device", required: false, multiple: false, submitOnChange: true
	if(buttonDev) {
//		input "numButtons$dev", "number", title: "Number of buttons? (Default 4)", range: "1..20", required: false, submitOnChange: true, description: "4"
//		def numButtons = settings.find{it.key == "numButtons$dev"}
//		numButtons = numButtons ? numButtons.value : 4
		def numButtons = 4
//		def butOpts = ["one"]
		def butOpts = ["one", "two", "three", "four"]
//		if(numButtons > 1) {
//        	for (int i = 1; i < numButtons; i++) butOpts[i] = numNames[i]
            log.debug "getButton: $buttonDev, $numButtons"
			input "buttonNbr", "enum", title: "Button number", required: true, multiple: false, submitOnChange: true, options: butOpts, defaultValue: "one"
//        }
        input buttonState, "enum", title: "Button pushed or held ", options: ["pushed", "held"], defaultValue: "pushed"
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
	subscribe(main, "switch.on", switchOnHandler)
	subscribe(buttonSw, "switch.on", switchOnHandler)
	subscribe(main, "switch.off", switchOffHandler)
    subscribe(buttonSw, "switch.off", allSwitchOffHandler)
	subscribe(main, "level", levelHandler)
	subscribe(extras, "level", levelHandler)
	subscribe(motions, "motion.active", motionOnHandler)
	subscribe(disabled, "switch", disableHandler)
	if(turnOff) subscribe(motions, "motion.inactive", motionOffHandler)
	subscribe(location, modeChangeHandler)
    subscribe(buttonDev, "button", buttonHandler)
    subscribe(buttonOffDev, "button", buttonOffHandler)
    
	state.modeLevels = [:]
	for(m in modesX) {
		def level = settings.find {it.key == "level$m"}
		state.modeLevels << [(m):level.value]
	}

	state.currentMode = location.mode in modesX ? location.mode : modesX[0]
	state.dimLevel = state.modeLevels[state.currentMode]
	state.motionOffDismissed = true
	state.motionDisabled = (disable) ? disable.currentSwitch == "on" : false
	state.mainOff = main.currentSwitch == "off"
    state.needsReset = false
}

def switchesOn() {
	state.motionOffDismissed = true    		// use this variable instead of unschedule() to kill pending off()
	state.mainOff = false
	main.setLevel(state.dimLevel)
	extras?.setLevel(state.dimLevel)
    state.needsReset = false
    onSwitches?.on()
}

def switchOnHandler(evt) {
	if(state.mainOff) switchesOn() 
}

def motionOnHandler(evt) {
	if(state.motionDisabled) return
	if(allOk && state.mainOff) switchesOn() else state.motionOffDismissed = true
}

def disableHandler(evt) {
	state.motionDisabled = evt.value == "on"
}

def levelHandler(evt) {      				// allows a dimmer to change the current dimLevel
	if(evt.value == state.dimLevel) return
	state.dimLevel = evt.value
	switchesOn()
}

def buttonHandler(evt) {
	def numNames = ["", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
    	"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"]
	def buttonNumber = evt.jsonData.buttonNumber.toInteger() 
    def value = evt.value
	def recentEvents = buttonDev.eventsSince(new Date(now() - 3000)).findAll{it.value == evt.value && it.data == evt.data}
	def thisButton = 0
	def firstEventId = 0
	if (recentEvents.size() != 0) firstEventId = recentEvents[0].id
	if(firstEventId == evt.id) thisButton = numNames[buttonNumber]
//	def myState = settings.find {it.key == (state.isTrig ? "state$i" : "tstate$i")}
//	def myButton = settings.find {it.key == (state.isTrig ? "ButtonrDev$i" : "ButtontDev$i")}
    def result = true
    if(value in ["pushed", "held"]) result = (value == buttonState) && (thisButton == buttonNbr)
//    else if(value.startsWith("button")) result = thisButton == myButton.value // ZWN-SC7
	if(result && state.mainOff) switchesOn()
}

def buttonOffHandler(evt) {
	def numNames = ["", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
    	"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"]
	def buttonNumber = evt.jsonData.buttonNumber.toInteger() 
    def value = evt.value
	def recentEvents = buttonOffDev.eventsSince(new Date(now() - 3000)).findAll{it.value == evt.value && it.data == evt.data}
	def thisButton = 0
	def firstEventId = 0
	if (recentEvents.size() != 0) firstEventId = recentEvents[0].id
	if(firstEventId == evt.id) thisButton = numNames[buttonNumber]
//	def myState = settings.find {it.key == (state.isTrig ? "state$i" : "tstate$i")}
//	def myButton = settings.find {it.key == (state.isTrig ? "ButtonrDev$i" : "ButtontDev$i")}
    def result = true
    if(value in ["pushed", "held"]) result = (value == buttonOffState) && (thisButton == buttonOffNbr)
//    else if(value.startsWith("button")) result = thisButton == myButton.value // ZWN-SC7
	if(result) switchesOffNow(true)
}

def switchOffHandler(evt) {
	switchesOffNow(false)
}

def allSwitchOffHandler(evt) {
	switchesOffNow(true)
}

def switchesOffLater() {                    		
	if(state.motionOffDismissed || state.motionDisabled) return  
	if(allOk) switchesOffNow(true)
}

def switchesOffNow(m) {
	state.dimLevel = state.modeLevels[state.currentMode]
	state.mainOff = true
    if(state.needsReset) {
    	if(m) main.resetLevel(state.dimLevel)
		extras?.resetLevel(state.dimLevel)
        state.needsReset = false
    } else {
    	if(m) main.off()
    	extras?.off()
    }
	offSwitches?.off()
}

def motionOffHandler(evt) {  				// called when motion goes inactive, check all sensors
	if(state.motionDisabled) return
	if(allOk) {
		def someMotion = "active" in motions.currentMotion
		state.motionOffDismissed = someMotion
		if(!someMotion) {if(minutes) runIn(minutes*60, switchesOffLater) else switchesOffNow(true)}
	}
}

def modeChangeHandler(evt) {
	if(state.currentMode == evt.value || !(evt.value in modesX)) return   // no change or not one of our modes
	state.currentMode = evt.value			   
	state.dimLevel = state.modeLevels[evt.value]
// the next two lines brighten any lights on when new mode is brighter than previous
	if(main.currentSwitch == "on" && main.currentLevel < state.dimLevel) main.setLevel(state.dimLevel)
	extras.each {if(it.currentSwitch == "on" && it.currentLevel < state.dimLevel) it.setLevel(state.dimLevel)}
// the next five lines reset the dimLevel on any dimmer that is off at mode change for non-motion rooms
    if(!motions) {
    	if(state.mainOff) main.resetLevel(state.dimLevel)
    	else state.needsReset = true
    	extras.each {if(it.currentSwitch == "off") it.resetLevel(state.dimLevel)}
    }
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
	if      (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")
}