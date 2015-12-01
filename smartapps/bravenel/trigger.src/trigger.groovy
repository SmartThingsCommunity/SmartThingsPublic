/**
 *  Trigger
 *
 *	Version 1.1.3   30 Nov 2015
 *
 *  Copyright 2015 Bruce Ravenel
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
    name: "Trigger",
    namespace: "bravenel",
    author: "Bruce Ravenel",
    description: "Trigger",
    category: "Convenience",
    parent: "bravenel:Trigger Happy",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps@2x.png"
)

preferences {
	page(name: "selectTriggerActs")
	page(name: "selectConditions")
	page(name: "certainTime")
	page(name: "atCertainTime")
	page(name: "selectActionsTrue")
	page(name: "selectMsgTrue")
}

def selectTriggerActs() {
	dynamicPage(name: "selectTriggerActs", title: "Select Triggers and Actions", uninstall: true, install: true) {
		section() {     
			label title: "Name the Trigger", required: true
			def condLabel = conditionLabel()
			href "selectConditions", title: "Define Triggers", description: condLabel ? (condLabel) : "Tap to set", required: true, state: condLabel ? "complete" : null, submitOnChange: true
			href "selectActionsTrue", title: "Select the Actions", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null
		}
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
			def timeLabel = timeIntervalLabel()
			href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false            
			input "disabled", "capability.switch", title: "Switch to disable trigger when ON", required: false, multiple: false
			input "logging", "bool", title: "Enable event logging", required: false, defaultValue: false
   		}    
	}
}

// Trigger input code follows

def atCertainTime() {
	dynamicPage(name: "atCertainTime", title: "At a certain time", uninstall: false) {
		section() {
			input "timeX", "enum", title: "At time or sunrise/sunset?", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if(timeX in [null, "A specific time"]) input "atTime", "time", title: "At this time", required: false
			else {
				if(timeX == "Sunrise") input "atSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(timeX == "Sunset") input "atSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}            
		}
	}
}

def selectConditions() {
    def ct = settings.findAll{it.key.startsWith("rCapab")}
    state.howMany = ct.size() + 1
    def howMany = state.howMany
	dynamicPage(name: "selectConditions", title: "Select Trigger Events", uninstall: false) {
		if(howMany) {
			for (int i = 1; i <= howMany; i++) {
				def thisCapab = "rCapab$i"
				section("Event Trigger #$i") {
					getCapab(thisCapab)
					def myCapab = settings.find {it.key == thisCapab}
					if(myCapab) {
						def xCapab = myCapab.value 
						if(!(xCapab in ["Certain Time", "Mode", "Routine", "Button"])) {
							def thisDev = "rDev$i"
							getDevs(xCapab, thisDev, true)
							def myDev = settings.find {it.key == thisDev}
							if(myDev) if(myDev.value.size() > 1) getAnyAll(thisDev)
							if(xCapab in ["Temperature", "Humidity", "Illuminance", "Dimmer level", "Energy meter", "Power meter", "Battery"]) getRelational(thisDev)
						} else if(xCapab == "Button") {
							def thisDev = "rDev$i"
							getButton(thisDev)
						}
						getState(xCapab, i)
					}
				}
			}
		}
	}
}

def getDevs(myCapab, dev, multi) {
    def thisName = ""
    def thisCapab = ""
	switch(myCapab) {
		case "Switch":
			thisName = "Switches"
			thisCapab = "switch"
			break
		case "Physical Switch":
			thisName = "Switches"
			thisCapab = "switch"
			break
		case "Motion":
			thisName = "Motion sensors"
			thisCapab = "motionSensor"
			break
		case "Acceleration":
			thisName = "Acceleration sensors"
			thisCapab = "accelerationSensor"
			break        
		case "Contact":
			thisName = "Contact sensors"
			thisCapab = "contactSensor"
			break
		case "Presence":
			thisName = "Presence sensors"
			thisCapab = "presenceSensor"
			break
		case "Lock":
			thisName = "Locks"
			thisCapab = "lock"
			break
		case "Dimmer level":
			thisName = "Dimmer" + (multi ? "s" : "")
			thisCapab = "switchLevel"
			break
		case "Temperature":
			thisName = "Temperature sensor" + (multi ? "s" : "")
			thisCapab = "temperatureMeasurement"
			break
		case "Humidity":
			thisName = "Humidity sensor" + (multi ? "s" : "")
			thisCapab = "relativeHumidityMeasurement"
			break
		case "Illuminance":
			thisName = "Illuminance sensor" + (multi ? "s" : "")
			thisCapab = "illuminanceMeasurement"
			break
		case "Energy meter":
			thisName = "Energy meter" + (multi ? "s" : "")
			thisCapab = "energyMeter"
			break
		case "Power meter":
			thisName = "Power meter" + (multi ? "s" : "")
			thisCapab = "powerMeter"
			break
		case "Carbon monoxide detector":
			thisName = "CO detector" + (multi ? "s" : "")
			thisCapab = "carbonMonoxideDetector"
			break
		case "Smoke detector":
			thisName = "Smoke detector" + (multi ? "s" : "")
			thisCapab = "smokeDetector"
			break
		case "Water sensor":
			thisName = "Water sensors"
			thisCapab = "waterSensor"
			break
		case "Button":
			thisName = "Button devices"
			thisCapab = "button"
			break
		case "Battery":
			thisName = multi ? "Batteries" : "Battery"
			thisCapab = "battery"
	}
	def result = input dev, "capability.$thisCapab", title: thisName, required: true, multiple: multi, submitOnChange: true
}

def getButton(dev) {
	def result = input "$dev", "capability.button", title: "Button Device", required: true, multiple: false, submitOnChange: true
	if(dev) input "Button$dev", "enum", title: "Button number", required: true, multiple: false, submitOnChange: true, options: ["one", "two", "three", "four"]
}

def getAnyAll(myDev) {
	def result = input "All$myDev", "bool", title: "All of these?", defaultValue: false
}

def getRelational(myDev) {
	def result = input "Rel$myDev", "enum", title: "Choose comparison", required: true, options: ["=", "!=", "<", ">", "<=", ">="]
}

def getCapab(myCapab) { 
	def myOptions = ["Switch", "Physical Switch", "Motion", "Acceleration", "Contact", "Presence", "Lock", "Temperature", "Humidity", "Illuminance", "Certain Time", 
    	"Mode", "Energy meter", "Power meter", "Water sensor", "Battery", "Routine", "Button", "Dimmer level", "Carbon monoxide detector", "Smoke detector"]
	def result = input myCapab, "enum", title: "Select capability", required: false, options: myOptions.sort(), submitOnChange: true
}

def getState(myCapab, n) {
	def result = null
	if     (myCapab == "Switch") 			result = input "state$n", "enum", title: "Switch turns ", options: ["on", "off"], defaultValue: "on"
	else if(myCapab == "Physical Switch")		result = input "state$n", "enum", title: "Switch turns ", options: ["on", "off"], defaultValue: "on"
	else if(myCapab == "Motion") 			result = input "state$n", "enum", title: "Motion becomes ", options: ["active", "inactive"], defaultValue: "active"
	else if(myCapab == "Acceleration")		result = input "state$n", "enum", title: "Acceleration becomes ", options: ["active", "inactive"], defaultValue: "active"
	else if(myCapab == "Contact") 			result = input "state$n", "enum", title: "Contact ", options: ["open", "closed"], defaultValue: "open"
	else if(myCapab == "Presence") 			result = input "state$n", "enum", title: "Presence ", options: ["arrives", "leaves"], defaultValue: "arrives"
	else if(myCapab == "Lock")			result = input "state$n", "enum", title: "Lock is ", options: ["locked", "unlocked"], defaultValue: "unlocked"
	else if(myCapab == "Carbon monoxide detector")		result = input "state$n", "enum", title: "CO becomes ", options: ["clear", ,"detected", "tested"], defaultValue: "detected"
	else if(myCapab == "Smoke detector")		result = input "state$n", "enum", title: "Smoke becomes ", options: ["clear", ,"detected", "tested"], defaultValue: "detected"
	else if(myCapab == "Water sensor")		result = input "state$n", "enum", title: "Water becomes ", options: ["dry", "wet"], defaultValue: "wet"
	else if(myCapab == "Button")			result = input "state$n", "enum", title: "Button pushed or held ", options: ["pushed", "held"], defaultValue: "pushed"
	else if(myCapab in ["Temperature", "Humidity", "Illuminance", "Energy meter", "Power meter", "Battery", "Dimmer level"]) {
    	input "isDev$n", "bool", title: "Relative to another device?", multiple: false, required: false, submitOnChange: true, defaultValue: false
        def myDev = settings.find {it.key == "isDev$n"}
        if(myDev && myDev.value) getDevs(myCapab, "relDevice$n", false)
		else if(myCapab == "Temperature") 		result = input "state$n", "decimal", title: "Temperature becomes ", range: "*..*"
		else if(myCapab == "Humidity") 			result = input "state$n", "number", title: "Humidity becomes", range: "0..100"
		else if(myCapab == "Illuminance") 		result = input "state$n", "number", title: "Illuminance becomes"
		else if(myCapab == "Dimmer level")		result = input "state$n", "number", title: "Dimmer level", range: "0..100"
		else if(myCapab == "Energy meter") 		result = input "state$n", "number", title: "Energy level becomes"
		else if(myCapab == "Power meter") 		result = input "state$n", "number", title: "Power level becomes", range: "*..*"
		else if(myCapab == "Battery") 			result = input "state$n", "number", title: "Battery level becomes"
	} else if(myCapab == "Mode") {
		def myModes = []
		location.modes.each {myModes << "$it"}
		result = input "modesX", "enum", title: "When mode becomes", multiple: true, required: false, options: myModes.sort()
	} else if(myCapab == "Certain Time") {
		def atTimeLabel = atTimeLabel()
		href "atCertainTime", title: "At a certain time", description: atTimeLabel ?: "Tap to set", state: atTimeLabel ? "complete" : null
	} else if(myCapab == "Routine") {
    	def phrases = location.helloHome?.getPhrases()*.label
        result = input "state$n", "enum", title: "When this routine runs", multiple: false, required: false, options: phrases
    }
}

def certainTime() {
	dynamicPage(name: "certainTime", title: "Only during a certain time", uninstall: false) {
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

def conditionLabel() {
	def howMany = state.howMany
	def result = ""
	if(howMany) {
		for (int i = 1; i <= howMany; i++) {
			result = result + conditionLabelN(i)
			if(i < howMany -1) result = result + "\n"
		}
	}
	return result
}

def conditionLabelN(i) {
	def result = ""
        def thisCapab = settings.find {it.key == "rCapab$i"}
        if(!thisCapab) return result
        if(thisCapab.value == "Mode") result = "Mode becomes " + (modesX.size() > 1 ? modesX : modesX[0])
        else if(thisCapab.value == "Certain Time")  result = "When time is " + atTimeLabel()
        else if(thisCapab.value == "Routine") {
        	result = "Routine "
		def thisState = settings.find {it.key == "state$i"}
		result = result + "'" + thisState.value + "' runs"        
        } else {
		def thisDev = settings.find {it.key == "rDev$i"}
		if(!thisDev) return result
		def thisAll = settings.find {it.key == "AllrDev$i"}
		def myAny = thisAll ? "any " : ""
		def myButton = settings.find {it.key == "ButtonrDev$i"}
		if     (thisCapab.value == "Temperature") 	result = "Temperature becomes "
		else if(thisCapab.value == "Humidity") 		result = "Humidity becomes "
		else if(thisCapab.value == "Illuminance")	result = "Illuminance becomes "
		else if(thisCapab.value == "Dimmer level")	result = "Dimmer level becomes " 
		else if(thisCapab.value == "Energy meter")	result = "Energy level becomes " 
		else if(thisCapab.value == "Power meter")	result = "Power level becomes " 
		else if(thisCapab.value == "Battery")		result = "Battery level becomes "
        	else if(thisCapab.value == "Button") {
            		result = "$thisDev.value button $myButton.value "                
			def thisState = settings.find {it.key == "state$i"}
			result = result + thisState.value
                	return result
		}
		result = result + (myAny ? thisDev.value : thisDev.value[0]) + " " + ((thisAll ? thisAll.value : false) ? "all " : myAny)
		def thisRel = settings.find {it.key == "RelrDev$i"}
		if(thisCapab.value in ["Temperature", "Humidity", "Illuminance", "Dimmer level", "Energy meter", "Power meter", "Battery"]) result = result + " " + thisRel.value + " "
		if(thisCapab.value == "Physical Switch") result = result + "physical "
		def thisState = settings.find {it.key == "state$i"}
		def thisRelDev = settings.find {it.key == "relDevice$i"}
		if(thisRelDev) result = result + thisRelDev.value
		else result = result + thisState.value
        }
	return result
}

// Action selection code follows

def stripBrackets(str) {
	def i = str.indexOf('[')
	def j = str.indexOf(']')
	def result = str.substring(0, i) + str.substring(i + 1, j) + str.substring(j + 1)
	return result
}

def setActTrue(dev, str) {
	if(dev) state.actsTrue = state.actsTrue + stripBrackets("$str") + "\n"
}

def addToActTrue(str) {
	state.actsTrue = state.actsTrue + str + "\n"
}

def buildActTrue(str, brackets) {
	log.debug "buildAct: $str, $brackets"
	state.actsTrue = state.actsTrue + (brackets ? stripBrackets("$str") : str)
}

def selectActionsTrue() {
	dynamicPage(name: "selectActionsTrue", title: "Select Actions", uninstall: false) {
		state.actsTrue = ""
		section("") {
			input "onSwitchTrue", "capability.switch", title: "Turn on these switches", multiple: true, required: false, submitOnChange: true
			setActTrue(onSwitchTrue, "On: $onSwitchTrue")
			input "offSwitchTrue", "capability.switch", title: "Turn off these switches", multiple: true, required: false, submitOnChange: true
			setActTrue(offSwitchTrue, "Off: $offSwitchTrue")
			input "toggleSwitchTrue", "capability.switch", title: "Toggle these switches", multiple: true, required: false, submitOnChange: true
			setActTrue(toggleSwitchTrue, "Toggle: $toggleSwitchTrue")
			input "delayedOffTrue", "capability.switch", title: "Turn on/off these switches after a delay (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(delayedOffTrue) {
				input "delayOnOffTrue", "bool", title: "Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
				input "delayMinutesTrue", "number", title: "Minutes of delay", required: true, range: "1..*", submitOnChange: true
				if(delayMinutesTrue) {
					def delayStrTrue = "Delayed " + (delayOnOffTrue ? "On:" : "Off:") + " $delayedOffTrue: $delayMinutesTrue minute"
					if(delayMinutesTrue > 1) delayStrTrue = delayStrTrue + "s"
					setActTrue(delayedOffTrue, delayStrTrue)
				}
			}
			input "dimATrue", "capability.switchLevel", title: "Set these dimmers", multiple: true, submitOnChange: true, required: false
			if(dimATrue) input "dimLATrue", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
			if(dimLATrue) setActTrue(dimATrue, "Dim: $dimATrue: $dimLATrue")
			input "dimBTrue", "capability.switchLevel", title: "Set these other dimmers", multiple: true, submitOnChange: true, required: false
			if(dimBTrue) input "dimLBTrue", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
			if(dimLBTrue) setActTrue(dimBTrue, "Dim: $dimBTrue: $dimLBTrue")
			input "toggleDimmerTrue", "capability.switchLevel", title: "Toggle these dimmers", multiple: true, required: false, submitOnChange: true
			if(toggleDimmerTrue) input "dimTogTrue", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
			if(dimTogTrue) setActTrue(toggleDimmerTrue, "Toggle: $toggleDimmerTrue: $dimTogTrue")
			input "bulbsTrue", "capability.colorControl", title: "Set color for these bulbs", multiple: true, required: false, submitOnChange: true
			if(bulbsTrue) {
				input "colorTrue", "enum", title: "Bulb color?", required: true, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink"]
				input "colorLevelTrue", "number", title: "Bulb level?", required: false, submitOnChange: true, range: "0..100"
				buildActTrue("Color: $bulbsTrue ", true)
				if(colorTrue) buildActTrue("$colorTrue ", false)
				if(colorLevelTrue) addToActTrue("Level: $colorLevelTrue")
			}            
			input "lockTrue", "capability.lock", title: "Lock these locks", multiple: true, required: false, submitOnChange: true
			setActTrue(lockTrue, "Lock: $lockTrue")
			input "unlockTrue", "capability.lock", title: "Unlock these locks", multiple: true, required: false, submitOnChange: true
			setActTrue(unlockTrue, "Unlock: $unlockTrue")
			input "fanAdjustTrue", "capability.switchLevel", title: "Adjust these fans - Low, Medium, High, Off", multiple: false, required: false, submitOnChange: true
			if(fanAdjustTrue) addToActTrue("Adjust Fan: $fanAdjustTrue")
			input "openValveTrue", "capability.valve", title: "Open these valves", multiple: true, required: false, submitOnChange: true
			setActTrue(openValveTrue, "Open: $openValveTrue")
			input "closeValveTrue", "capability.valve", title: "Close these valves", multiple: true, required: false, submitOnChange: true
			setActTrue(closeValveTrue, "Close: $closeValveTrue")
			input "thermoTrue", "capability.thermostat", title: "Set these thermostats", multiple: true, required: false, submitOnChange: true
			if(thermoTrue) {
				input "thermoModeTrue", "enum", title: "Select thermostate mode", multiple: false, required: false, options: ["auto", "heat", "cool", "off"], submitOnChange: true
				input "thermoSetHeatTrue", "decimal", title: "Set heating point", multiple: false, required: false, submitOnChange: true
				input "thermoSetCoolTrue", "decimal", title: "Set cooling point", multiple: false, required: false, submitOnChange: true 
				input "thermoFanTrue", "enum", title: "Fan setting", multiple: false, required: false, submitOnChange: true, options: ["fanOn", "fanAuto"]
				buildActTrue("$thermoTrue: ", true)
				if(thermoModeTrue) buildActTrue("Mode: " + "$thermoModeTrue ", false)
				if(thermoSetHeatTrue) buildActTrue("Heat to $thermoSetHeatTrue ", false)
				if(thermoSetCoolTrue) buildActTrue("Cool to $thermoSetCoolTrue ", false)
				if(thermoFanTrue) buildActTrue("Fan setting $thermoFanTrue", false)
				addToActTrue("")
			}
			def myModes = []
			location.modes.each {myModes << "$it"}
			input "modeTrue", "enum", title: "Set the mode", multiple: false, required: false, options: myModes.sort(), submitOnChange: true
			if(modeTrue) addToActTrue("Mode: $modeTrue")
			def phrases = location.helloHome?.getPhrases()*.label
			input "myPhraseTrue", "enum", title: "Routine to run", required: false, options: phrases.sort(), submitOnChange: true
			if(myPhraseTrue) addToActTrue("Routine: $myPhraseTrue")
			href "selectMsgTrue", title: "Send a message", description: state.msgTrue ? state.msgTrue : "Tap to set", state: state.msgTrue ? "complete" : null
			if(state.msgTrue) addToActTrue(state.msgTrue)
			input "delayTrue", "number", title: "Delay this action by this many minutes", required: false, submitOnChange: true
			if(delayTrue) {
				def delayStr = "Delay Rule: $delayTrue minute"
				if(delayTrue > 1) delayStr = delayStr + "s"
				addToActTrue(delayStr)
			}
		}
        if(state.actsTrue) state.actsTrue = state.actsTrue[0..-2]
	}
}

def selectMsgTrue() {
	dynamicPage(name: "selectMsgTrue", title: "Select Message and Destination", uninstall: false) {
		section("") {
			input "pushTrue", "bool", title: "Send Push Notification?", required: false, submitOnChange: true
			input "msgTrue", "text", title: "Custom message to send", required: false, submitOnChange: true
			input "phoneTrue", "phone", title: "Phone number for SMS", required: false, submitOnChange: true
		}
        state.msgTrue = (pushTrue ? "Push" : "") + (msgTrue ? " '$msgTrue'" : "") + (phoneTrue ? " to $phoneTrue" : "")
	}
}

// initialization code follows

def scheduleAtTime() {
	def myTime = null
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: atSunriseOffset, sunsetOffset: atSunsetOffset)
	if(timeX == "Sunrise") myTime = s.sunrise.time
	else if(timeX == "Sunset") myTime = s.sunset.time
	else myTime = timeToday(atTime, location.timeZone).time
	schedule(myTime, "timeHandler")
	if(timeX in ["Sunrise", "Sunset"]) schedule("2015-01-09T00:00:29.000-0700", "scheduleAtTime") // in case sunset/sunrise; change daily
}

def installed() {
	initialize()
}

def updated() {
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	for (int i = 1; i < state.howMany; i++) {
		def capab =   (settings.find {it.key == "rCapab$i"}).value
		def myState = (settings.find {it.key == "state$i"})
		def myRelDev = settings.find {it.key == "relDevice$i"}
		if(myState) myState = myState.value
		switch(capab) {
			case "Mode": 
				subscribe(location, "mode", allHandler)
				break
			case "Certain Time":
				scheduleAtTime()
				break
			case "Dimmer level":
				subscribe((settings.find{it.key == "rDev$i"}).value, "level", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "level", allHandler)
				break
			case "Energy meter":
				subscribe((settings.find{it.key == "rDev$i"}).value, "energy", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "energy", allHandler)
				break
			case "Power meter":
				subscribe((settings.find{it.key == "rDev$i"}).value, "power", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "power", allHandler)
				break
			case "Temperature":
				subscribe((settings.find{it.key == "rDev$i"}).value, "temperature", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "temperature", allHandler)
				break
			case "Humidity":
				subscribe((settings.find{it.key == "rDev$i"}).value, "humidity", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "humidity", allHandler)
				break
			case "Battery":
				subscribe((settings.find{it.key == "rDev$i"}).value, "battery", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "battery", allHandler)
				break
			case "Illuminance":
				subscribe((settings.find{it.key == "rDev$i"}).value, "illuminance", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "illuminance", allHandler)
				break
			case "Carbon monoxide detector":
				subscribe((settings.find{it.key == "rDev$i"}).value, "carbonMonoxide.$myState", allHandler)
				break
			case "Smoke detector":
				subscribe((settings.find{it.key == "rDev$i"}).value, "smoke.$myState", allHandler)
				break
			case "Water sensor":
				subscribe((settings.find{it.key == "rDev$i"}).value, "water.$myState", allHandler)
				break
			case ["arrives", "leaves"]:
				subscribe((settings.find{it.key == "rDev$i"}).value, "presence", allHandler)
				break
			case "Button":
				subscribe((settings.find{it.key == "rDev$i"}).value, "button", allHandler)
				break
			case "Physical Switch":
				subscribe((settings.find{it.key == "rDev$i"}).value, "switch.$myState", physicalHandler)
				break
			case "Routine":
				subscribe(location, "routineExecuted", allHandler)
				break
			default:
				subscribe((settings.find{it.key == "rDev$i"}).value, (capab.toLowerCase() + ".$myState"), allHandler)
		}
	}
	subscribe(disabled, "switch", disabledHandler)
	if(disabled) state.disabled = disabled.currentSwitch == "on"
	else state.disabled = false
}

// Trigger evaluation code follows

def compare(a, rel, b, relDev) {
	def result = true
	if     (rel == "=") 	result = a == (relDev ?: b)
	else if(rel == "!=") 	result = a != (relDev ?: b)
	else if(rel == ">") 	result = a >  (relDev ?: b)
	else if(rel == "<") 	result = a <  (relDev ?: b)
	else if(rel == ">=") 	result = a >= (relDev ?: b)
	else if(rel == "<=") 	result = a <= (relDev ?: b)
	return result
}

def checkCondAny(dev, state, cap, rel, relDev) {
	def result = false
	if     (cap == "Temperature") 	dev.currentTemperature.each 	{result = result || compare(it, rel, state, reldev ? relDev.currentTemperature : null)}
	else if(cap == "Humidity")	dev.currentHumidity.each    	{result = result || compare(it, rel, state, reldev ? relDev.currentHumidity : null)}
	else if(cap == "Illuminance") 	dev.currentIlluminance.each 	{result = result || compare(it, rel, state, reldev ? relDev.currentIlluminance : null)}
	else if(cap == "Dimmer level")	dev.currentLevel.each		{result = result || compare(it, rel, state, relDev ? relDev.currentLevel : null)}
	else if(cap == "Energy meter")	dev.currentEnergy.each		{result = result || compare(it, rel, state, relDev ? relDev.currentEnergy : null)}
	else if(cap == "Power meter")	dev.currentPower.each		{result = result || compare(it, rel, state, relDev ? relDev.currentPower : null)}
	else if(cap == "Battery")	dev.currentBattery.each		{result = result || compare(it, rel, state, relDev ? relDev.currentBattery : null)}
	else if(cap == "Water sensor")	result = state in dev.currentWater
	else if(cap == "Switch") 	result = state in dev.currentSwitch
	else if(cap == "Motion") 	result = state in dev.currentMotion
	else if(cap == "Acceleration") 	result = state in dev.currentAcceleration
	else if(cap == "Contact") 	result = state in dev.currentContact
	else if(cap == "Presence") 	result = state in dev.currentPresence
	else if(cap == "Smoke detector") 	result = state in dev.currentSmoke
	else if(cap == "Carbon monoxide detector") 	result = state in dev.currentCarbonMonoxide
	else if(cap == "Lock") 		result = state in dev.currentLock
//	log.debug "CheckAny $cap $result"
	return result
}

def checkCondAll(dev, state, cap, rel, relDev) {
	def flip = ["on": "off",
		"off": "on",
                "active": "inactive",
                "inactive": "active",
                "open": "closed",
                "closed": "open",
                "wet": "dry",
                "dry": "wet",
                "detected": "clear",
                "clear": "detected",
                "present": "not present",
                "not present": "present",
                "locked": "unlocked",
                "unlocked": "locked"]
	def result = true
	if     (cap == "Temperature") 		dev.currentTemperature.each 	{result = result && compare(it, rel, state, reldev ? relDev.currentTemperature : null)}
	else if(cap == "Humidity") 		dev.currentHumidity.each    	{result = result && compare(it, rel, state, reldev ? relDev.currentHumidity : null)}
	else if(cap == "Illuminance") 		dev.currentIlluminance.each 	{result = result && compare(it, rel, state, reldev ? relDev.currentIlluminance : null)}
	else if(cap == "Dimmer level")		dev.currentLevel.each		{result = result && compare(it, rel, state, reldev ? relDev.currentLevel : null)}
	else if(cap == "Energy meter")		dev.currentEnergy.each		{result = result && compare(it, rel, state, reldev ? relDev.currentEnergy : null)}
	else if(cap == "Power meter")		dev.currentPower.each		{result = result && compare(it, rel, state, reldev ? relDev.currentPower : null)}
	else if(cap == "Battery")		dev.currentBattery.each		{result = result && compare(it, rel, state, reldev ? relDev.currentBattery : null)}
	else if(cap == "Water sensor")		result = !(flip[state] in dev.currentSwitch)
	else if(cap == "Switch") 		result = !(flip[state] in dev.currentSwitch)
	else if(cap == "Motion") 		result = !(flip[state] in dev.currentMotion)
	else if(cap == "Acceleration") 		result = !(flip[state] in dev.currentAcceleration)
	else if(cap == "Contact") 		result = !(flip[state] in dev.currentContact)
	else if(cap == "Presence") 		result = !(flip[state] in dev.currentPresence)
	else if(cap == "Smoke detector") 	result = !(flip[state] in dev.currentSmoke)
	else if(cap == "Carbon monoxide detector") 	result = !(flip[state] in dev.currentCarbonMonoxide)
	else if(cap == "Lock") 			result = !(flip[state] in dev.currentLock)
//	log.debug "CheckAll $cap $result"
	return result
}

def getOperand(i) {
	def result = true
	def capab =    (settings.find {it.key == "rCapab$i"}).value
	def myDev = 	settings.find {it.key == "rDev$i"}
	def myState = 	settings.find {it.key == "state$i"}
	def myRel = 	settings.find {it.key == "RelrDev$i"}
	def myAll = 	settings.find {it.key == "AllrDev$i"}
	def myRelDev =  settings.find {it.key == "relDevice$i"}
	if(myAll) {
		if(myAll.value) result = checkCondAll(myDev.value, myState ? myState.value : null, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : null)
		else result = checkCondAny(myDev.value, myState ? myState.value : null, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : null)
	} else result = checkCondAny(myDev.value, myState ? myState.value : null, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : null)
//    log.debug "operand is $result"
	return result
}

// Take action code follows

def adjustFan(device) {
//	log.debug "adjust: $device = ${device.currentLevel}"
	def currentLevel = device.currentLevel
	if(device.currentSwitch == 'off') device.setLevel(15)
	else if (currentLevel < 34) device.setLevel(50)
  	else if (currentLevel < 67) device.setLevel(90)
	else device.off()
}

def adjustShade(device) {
//	log.debug "shades: $device = ${device.currentMotor} state.lastUP = $state.lastshadesUp"
	if(device.currentMotor in ["up","down"]) {
    	state.lastshadesUp = device.currentMotor == "up"
    	device.stop()
    } else {
    	state.lastshadesUp ? device.down() : device.up()
//    	if(state.lastshadesUp) device.down()
//        else device.up()
        state.lastshadesUp = !state.lastshadesUp
    }
}

def toggle(devices) {
//	log.debug "toggle: $devices = ${devices*.currentValue('switch')}"
	if (devices*.currentValue('switch').contains('on')) {
		devices.off()
	}
	else if (devices*.currentValue('switch').contains('off')) {
		devices.on()
	}
}

def dimToggle(devices, dimLevel) {
//	log.debug "dimToggle: $devices = ${devices*.currentValue('switch')}"
	if (devices*.currentValue('switch').contains('on')) devices.off()
	else devices.setLevel(dimLevel)
}

def doDelayTrue(time) {
	runIn(time * 60, delayRuleTrue)
	def delayStr = "minute"
	if(time > 1) delayStr = delayStr + "s"
	if(logging) log.info ("Delayed by $time $delayStr")
}

def doTrigger(delay) {
	if(!allOk) return
	if(delay) unschedule(delayRuleTrue)
	if(delayTrue > 0 && !delay) doDelayTrue(delayTrue)
	else {
		if(onSwitchTrue) 		onSwitchTrue.on()
		if(offSwitchTrue) 		offSwitchTrue.off()
		if(toggleSwitchTrue)		toggle(toggleSwitchTrue)
		if(delayedOffTrue)		runIn(delayMinutesTrue * 60, delayOffTrue)
		if(dimATrue) 			dimATrue.setLevel(dimLATrue)
		if(dimBTrue) 			dimBTrue.setLevel(dimLBTrue)
		if(toggleDimmerTrue)		dimToggle(toggleDimmerTrue, dimTogTrue)
		if(bulbsTrue)			setColor(true)
		if(lockTrue) 			lockTrue.lock()
		if(unlockTrue) 			unlockTrue.unlock()
		if(fanAdjustTrue)		adjustFan(fanAdjustTrue)
		if(openValveTrue)		openValveTrue.open()
		if(closeValveTrue)		closeValveTrue.close()
		if(thermoTrue)			{	if(thermoModeTrue)	thermoTrue.setThermostatMode(thermoModeTrue)
							if(thermoSetHeatTrue) 	thermoTrue.setHeatingSetpoint(thermoSetHeatTrue)
							if(thermoSetCoolTrue) 	thermoTrue.setCoolingSetpoint(thermoSetCoolTrue)
							if(thermoFanTrue)	thermoTrue.setThermostatFanMode(thermoFanTrue)   }
		if(modeTrue) 			setLocationMode(modeTrue)
		if(myPhraseTrue)		location.helloHome.execute(myPhraseTrue)
		if(pushTrue)			sendPush(msgTrue ?: "Rule $app.label True")
		if(phoneTrue)			sendSms(phoneTrue, msgTrue ?: "Rule $app.label True")
	}
}

def getButton(dev, evt, i) {
	def buttonNumber = evt.data // why doesn't jsonData work? always returning [:]
	def value = evt.value
//	log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
//	log.debug "button: $buttonNumber, value: $value"
	def recentEvents = dev.eventsSince(new Date(now() - 3000)).findAll{it.value == evt.value && it.data == evt.data}
//	log.debug "Found ${recentEvents.size()?:0} events in past 3 seconds"
	def thisButton = 0
	if(recentEvents.size <= 1){
		switch(buttonNumber) {
			case ~/.*1.*/:
				thisButton = "one"
				break
			case ~/.*2.*/:
				thisButton = "two"
				break
			case ~/.*3.*/:
				thisButton = "three"
				break
			case ~/.*4.*/:
				thisButton = "four"
				break
		}
//	} else {
//		log.debug "Found recent button press events for $buttonNumber with value $value"
	}
	def myState = settings.find {it.key == "state$i"}
	def myButton = settings.find {it.key == "ButtonrDev$i"}
	def result = (evt.value == myState.value) && (thisButton == myButton.value)
}

def testEvt(evt) {
	def result = false
	if(evt.name == "mode") return modeXOk
	if(evt.name == "routineExecuted") {
		for(int i = 1; i < state.howMany; i++) {
			def myCapab = (settings.find {it.key == "rCapab$i"}).value
			def state = settings.find {it.key == "state$i"}
			if(myCapab == "Routine") result = result || evt.displayName == state.value
		}
		return result
	}
	for(int i = 1; i < state.howMany; i++) {
		def myDev = (settings.find {it.key == "rDev$i"}).value
		myDev.each {if(evt.displayName == it.displayName) {
			if(evt.name == "button") result = getButton(myDev, evt, i)
			else result = getOperand(i)}
		}
        if(result) return result
	}
	return result
}

def allHandler(evt) {
	if(logging) log.info "$app.label: $evt.displayName $evt.name $evt.value"
	def doit = true
	if(evt.name in ["temperature", "humidity", "power", "energy", "battery", "illuminance", "mode", "presence", "button", "routineExecuted"]) doit = testEvt(evt)
	if (doit) doTrigger(false)
}

def timeHandler() {
	doTrigger(false)
}

def physicalHandler(evt) {
	if(logging) log.info "$app.label: Physical $evt.displayName $evt.name $evt.value"
	if(evt.isPhysical()) doTrigger(false)
}

def delayOffTrue() {
	if(delayOnOffTrue && allOk) delayedOffTrue.on() else delayedOffTrue.off()
}

def delayRuleTrue() {
	doTrigger(true)
}

def disabledHandler(evt) {
	state.disabled = evt.value == "on"
}


//  private execution filter methods follow

private atTimeLabel() {
	def result = ''
	if     (timeX == "Sunrise") result = "Sunrise" + offset(atSunriseOffset)
	else if(timeX == "Sunset")  result = "Sunset" + offset(atSunsetOffset)
	else if(atTime) result = hhmm(atTime)
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private offset(value) {
	def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}

private timeIntervalLabel() {
	def result = ""
	if (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " and Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " and Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " and Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " and Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " and " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " and " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " and Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " and Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " and " + hhmm(ending, "h:mm a z")
}

private getAllOk() {
	modeOk && daysOk && timeOk && !state.disabled
}

private hideOptionsSection() {
	(starting || ending || days || modes || startingX || endingX || disabled) ? false : true
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
//	log.trace "modeOk = $result"
	return result
}

private getModeXOk() {
	def result = !modesX || modesX.contains(location.mode)
//	log.trace "modeXOk = $result"
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
	if((starting && ending) ||
	(starting && endingX in ["Sunrise", "Sunset"]) ||
	(startingX in ["Sunrise", "Sunset"] && ending) ||
	(startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if(startingX == "Sunrise") start = s.sunrise.time
		else if(startingX == "Sunset") start = s.sunset.time
		else if(starting) start = timeToday(starting, location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
//	log.trace "getTimeOk = $result"
	return result
}

private setColor(trufal) {
	def hueColor = 0
	def saturation = 100
	switch(trufal ? colorTrue : colorFalse) {
		case "White":
			hueColor = 52
			saturation = 19
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 //83
			break;
		case "Blue":
			hueColor = 70
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			break;
		case "Orange":
			hueColor = 10
			break;
		case "Purple":
			hueColor = 75
			break;
		case "Pink":
			hueColor = 83
			break;
		case "Red":
			hueColor = 100
			break;
	}
	def lightLevel = trufal ? colorLevelTrue : colorLevelFalse
	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
	if(trufal) bulbsTrue.setColor(newValue) else bulbsFalse.setColor(newValue)
}
