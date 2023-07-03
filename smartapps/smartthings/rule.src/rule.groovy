/**
 *  Rule
 *
 *  Copyright 2015 Bruce Ravenel
 *
 *  Version 1.4.1b   8 Dec 2015
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
    name: "Rule",
    namespace: "smartthings",
    author: "Bruce Ravenel",
    description: "Rule",
    category: "Convenience",
    parent: "smartthings:Rule Machine",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps@2x.png"
)

preferences {
	page(name: "selectRT")
	page(name: "selectRule")
	page(name: "selectTriggerActs")
	page(name: "selectConditions")
	page(name: "defineRule")
	page(name: "certainTime")
	page(name: "atCertainTime")
	page(name: "selectActionsTrue")
	page(name: "selectActionsFalse")
	page(name: "selectMsgTrue")
	page(name: "selectMsgFalse")
}

def selectRT() {
	dynamicPage(name: "selectRT", title: "Select Rule or Trigger", uninstall: true) {
		section() {
        	if(app.label && state.isRule == null) {
            	state.isRule = true
                state.isTrig = false
            }
			if(state.isRule || !app.label) href "selectRule", title: ((state.isRule ? "Installed" : "Create a") + " Rule"), description: state.isRule ? (app.label) : "Tap to set", required: false, state: state.isRule ? "complete" : null
            if(state.isTrig || !app.label) href "selectTriggerActs", title: ((state.isTrig ? "Installed" : "Create a") + " Trigger"), description: state.isTrig ? (app.label) : "Tap to set", required: false, state: state.isTrig ? "complete" : null
		}
    }
}

def selectRule() {
	dynamicPage(name: "selectRule", title: "Select Conditions, Rule and Actions", uninstall: true, install: true) {
		section() {     
			label title: "Name the Rule", required: true
            if(app.label) {
				state.isRule = true
            	state.isTrig = false
            }
			def condLabel = conditionLabel()
			href "selectConditions", title: "Define Conditions", description: condLabel ? (condLabel) : "Tap to set", required: true, state: condLabel ? "complete" : null, submitOnChange: true
			href "defineRule", title: "Define the Rule", description: state.str ? (state.str) : "Tap to set", state: state.str ? "complete" : null, submitOnChange: true
			href "selectActionsTrue", title: "Select the Actions for True", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null, submitOnChange: true
			href "selectActionsFalse", title: "Select the Actions for False", description: state.actsFalse ? state.actsFalse : "Tap to set", state: state.actsFalse ? "complete" : null, submitOnChange: true
		}
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
			input "modesZ", "mode", title: "Evaluate only when mode is", multiple: true, required: false
			paragraph "Advanced Rule Input allows for parenthesized sub-rules."
			input "advanced", "bool", title: "Advanced Rule Input", required: false
			input "disabled", "capability.switch", title: "Switch to disable rule when ON", required: false, multiple: false
			input "logging", "bool", title: "Enable event and rule logging", required: false, defaultValue: true
   		}    
	}
}

def selectTriggerActs() {
	dynamicPage(name: "selectTriggerActs", title: "Select Triggers and Actions", uninstall: true, install: true) {
		section() {     
			label title: "Name the Trigger", required: true
            if(app.label) {
				state.isRule = false
            	state.isTrig = true
            }
			def condLabel = conditionLabel()
			href "selectConditions", title: "Define Triggers", description: condLabel ? (condLabel) : "Tap to set", required: true, state: condLabel ? "complete" : null, submitOnChange: true
			href "selectActionsTrue", title: "Select the Actions", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null
		}
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
			def timeLabel = timeIntervalLabel()
			href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			input "modesY", "mode", title: "Only when mode is", multiple: true, required: false            
			input "disabled", "capability.switch", title: "Switch to disable trigger when ON", required: false, multiple: false
			input "logging", "bool", title: "Enable event logging", required: false, defaultValue: true
   		}    
	}
}

// Condition input code follows

def selectConditions() {
    def ct = settings.findAll{it.key.startsWith("rCapab")}
    state.howMany = ct.size() + 1							// initial value is 1
    def excludes = null
    if(state.isRule) excludes = ["Time of day", "Days of week", "Mode", "Smart Home Monitor"]
    else excludes = ["Certain Time", "Mode", "Routine", "Button", "Smart Home Monitor"]
	dynamicPage(name: "selectConditions", title: state.isRule ? "Select Conditions" : "Select Trigger Events", uninstall: false) {
		if(state.howMany) {
			for (int i = 1; i <= state.howMany; i++) {
				def thisCapab = "rCapab$i"
				section(state.isRule ? "Condition #$i" : "Event Trigger #$i") {
					getCapab(thisCapab)
					def myCapab = settings.find {it.key == thisCapab}
					if(myCapab) {
						def xCapab = myCapab.value 
						if(!(xCapab in excludes)) {
							def thisDev = "rDev$i"
							getDevs(xCapab, thisDev, true)
							def myDev = settings.find {it.key == thisDev}
//							if(myDev) if(myDev.value.size() > 1) getAnyAll(thisDev)
							if(myDev) if(myDev.value.size() > 1 && (xCapab != "Rule truth" || state.isRule)) getAnyAll(thisDev)
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
		case "Rule truth":
        	thisName = "Rules"
            def theseRules = parent.ruleList(app.label)
        	def result = input dev, "enum", title: thisName, required: true, multiple: multi, submitOnChange: true, options: theseRules.sort()
			return result
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
	def myOptions = null
	if(state.isRule) myOptions = ["Switch", "Motion", "Acceleration", "Contact", "Presence", "Lock", "Temperature", "Humidity", "Illuminance", "Time of day", "Rule truth",
    	"Days of week", "Mode", "Dimmer level", "Energy meter", "Power meter", "Water sensor", "Battery", "Carbon monoxide detector", "Smoke detector", "Smart Home Monitor"]
    else myOptions = ["Switch", "Physical Switch", "Motion", "Acceleration", "Contact", "Presence", "Lock", "Temperature", "Humidity", "Illuminance", "Certain Time", "Rule truth",
    	"Mode", "Energy meter", "Power meter", "Water sensor", "Battery", "Routine", "Button", "Dimmer level", "Carbon monoxide detector", "Smoke detector", "Smart Home Monitor"]
	def result = input myCapab, "enum", title: "Select capability", required: false, options: myOptions.sort(), submitOnChange: true
}

def getState(myCapab, n) {
	def result = null
    def phrase = state.isRule ? "state" : "becomes"
    def swphrase = state.isRule ? "state" : "turns"
    def presoptions = state.isRule ? ["present", "not present"] : ["arrives", "leaves"]
    def presdefault = state.isRule ? "present" : "arrives"
    def lockphrase = state.isRule ? "state" : "is"
	def days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
	if     (myCapab == "Switch") 		result = input "state$n", "enum", title: "Switch $swphrase", options: ["on", "off"], defaultValue: "on"
	else if(myCapab == "Physical Switch")		result = input "state$n", "enum", title: "Switch turns ", options: ["on", "off"], defaultValue: "on"
	else if(myCapab == "Motion") 		result = input "state$n", "enum", title: "Motion $phrase", options: ["active", "inactive"], defaultValue: "active"
	else if(myCapab == "Acceleration")	result = input "state$n", "enum", title: "Acceleration $phrase", options: ["active", "inactive"], defaultValue: "active"
	else if(myCapab == "Contact") 		result = input "state$n", "enum", title: "Contact state", options: ["open", "closed"], defaultValue: "open"
	else if(myCapab == "Presence") 		result = input "state$n", "enum", title: "Presence state", options: presoptions, defaultValue: presdefault
	else if(myCapab == "Lock")		result = input "state$n", "enum", title: "Lock $lockphrase", options: ["locked", "unlocked"], defaultValue: "unlocked"
	else if(myCapab == "Carbon monoxide detector")		result = input "state$n", "enum", title: "CO $phrase ", options: ["clear", ,"detected", "tested"], defaultValue: "detected"
	else if(myCapab == "Smoke detector")		result = input "state$n", "enum", title: "Smoke $phrase ", options: ["clear", ,"detected", "tested"], defaultValue: "detected"
	else if(myCapab == "Water sensor")	result = input "state$n", "enum", title: "Water $phrase", options: ["dry", "wet"], defaultValue: "wet"
	else if(myCapab == "Button")			result = input "state$n", "enum", title: "Button pushed or held ", options: ["pushed", "held"], defaultValue: "pushed"
    else if(myCapab == "Smart Home Monitor") result = input "state$n", "enum", title: "SHM state", options: ["away" : "Arm (away)", "stay" : "Arm (stay)", "off" : "Disarm"]
    else if(myCapab == "Rule truth") 	result = input "state$n", "enum", title: "Rule truth $phrase ", options: ["true", "false"], defaultValue: "true"
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
	} else if(myCapab == "Days of week") 	result = input "days", "enum", title: "On certain days of the week", multiple: true, required: false, options: days
	else if(myCapab == "Mode") {
		def myModes = []
		location.modes.each {myModes << "$it"}
        def modeVar = state.isRule ? "modes" : "modesX"
		result = input modeVar, "enum", title: "Select mode(s)", multiple: true, required: false, options: myModes.sort()
	} else if(myCapab == "Time of day") {
		def timeLabel = timeIntervalLabel()
		href "certainTime", title: "During a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
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

def conditionLabel() {
	def howMany = state.howMany
	def result = ""
	if(howMany) {
		for (int i = 1; i < howMany; i++) {
        	def thisCapab = settings.find {it.key == "rCapab$i"}
            if(!thisCapab) return result
            result = result + conditionLabelN(i) + (state.isRule ? (getOperand(i) ? " [TRUE]" : " [FALSE]") : "")
			if(i < howMany - 1) result = result + "\n"
		}
        if(state.isRule && howMany == 2) {
        	state.str = result[0..-8]
        	state.eval = [1]
        }
    }
	return result
}

def conditionLabelN(i) {
	def result = ""
    def SHMphrase = state.isRule ? "is" : "becomes"
    def phrase = state.isRule ? "of" : "becomes"
    def thisCapab = settings.find {it.key == "rCapab$i"}
    if(!thisCapab) return result
	if(thisCapab.value == "Time of day") result = "Time between " + timeIntervalLabel()
    else if(thisCapab.value == "Certain Time")  result = "When time is " + atTimeLabel()
    else if(thisCapab.value == "Smart Home Monitor") {
    	def thisState = (settings.find {it.key == "state$i"}).value
    	result = "SHM state $SHMphrase " + (thisState in ["away", "stay"] ? "Arm ($thisState)" : "Disarm")
	} else if(thisCapab.value == "Days of week") result = "Day i" + (days.size() > 1 ? "n " + days : "s " + days[0])
	else if(thisCapab.value == "Mode") result = state.isRule ? "Mode i" + (modes.size() > 1 ? "n " + modes : "s " + modes[0]) : "Mode becomes " + (modesX.size() > 1 ? modesX : modesX[0])
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
		if     (thisCapab.value == "Temperature") 	result = "Temperature $phrase "
		else if(thisCapab.value == "Humidity") 		result = "Humidity $phrase "
		else if(thisCapab.value == "Illuminance")	result = "Illuminance $phrase "
		else if(thisCapab.value == "Dimmer level")	result = "Dimmer level $phrase " 
		else if(thisCapab.value == "Energy meter")	result = "Energy level $phrase " 
		else if(thisCapab.value == "Power meter")	result = "Power level $phrase " 
		else if(thisCapab.value == "Battery")		result = "Battery level $phrase "
        else if(thisCapab.value == "Rule truth") 	result = "Rule truth $phrase "
        else if(thisCapab.value == "Button") {
            result = "$thisDev.value button $myButton.value "                
			def thisState = settings.find {it.key == "state$i"}
			result = result + thisState.value
            return result
		}
        if(thisCapab.value == "Rule truth") result = result = result + (thisDev.value.size() > 1 ? ("$thisDev.value any ") : (thisDev.value[0] + " "))
		else result = result + (myAny ? thisDev.value : thisDev.value[0]) + " " + ((thisAll ? thisAll.value : false) ? "all " : myAny)
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

// Rule definition code follows

def defineRule() {
	dynamicPage(name: "defineRule", title: "Define the Rule", uninstall: false) {
		state.n = 0
		state.str = ""
		state.eval = []
		section() {inputLeftAndRight(false)}
	}
}

def inputLeft(sub) {
	def conds = []
	for (int i = 1; i < state.howMany; i++) conds << conditionLabelN(i)
	if(advanced) input "subCondL$state.n", "bool", title: "Enter subrule for left?", submitOnChange: true
	if(settings["subCondL$state.n"]) {
		state.str = state.str + "("
		state.eval << "("
		paragraph(state.str)
		inputLeftAndRight(true)
		input "moreConds$state.n", "bool", title: "More conditions on left?", submitOnChange: true
		if(settings["moreConds$state.n"]) inputRight(sub)
	} else {
		input "condL$state.n", "enum", title: "Which condition?", options: conds, submitOnChange: true
		if(settings["condL$state.n"]) {
			state.str = state.str + settings["condL$state.n"]
			def myCond = 0
			for (int i = 1; i < state.howMany; i++) if(conditionLabelN(i) == settings["condL$state.n"]) myCond = i
			state.eval << myCond
			paragraph(state.str)
		}
	}
}

def inputRight(sub) {
	state.n = state.n + 1
	input "operator$state.n", "enum", title: "AND  or  OR", options: ["AND", "OR"], submitOnChange: true, required: false
	if(settings["operator$state.n"]) {
		state.str = state.str + " " + settings["operator$state.n"] + " "
		state.eval << settings["operator$state.n"]
		paragraph(state.str)
		def conds = []
		for (int i = 1; i < state.howMany; i++) conds << conditionLabelN(i)
		if(advanced) input "subCondR$state.n", "bool", title: "Enter subrule for right?", submitOnChange: true
		if(settings["subCondR$state.n"]) {
			state.str = state.str + "("
			state.eval << "("
			paragraph(state.str)
			inputLeftAndRight(true)
			inputRight(sub)
		} else {
			input "condR$state.n", "enum", title: "Which condition?", options: conds, submitOnChange: true
			if(settings["condR$state.n"]) {
				state.str = state.str + settings["condR$state.n"]
				def myCond = 0
				for (int i = 1; i < state.howMany; i++) if(conditionLabelN(i) == settings["condR$state.n"]) myCond = i
				state.eval << myCond
				paragraph(state.str)
			}
			if(sub) {
				input "endOfSub$state.n", "bool", title: "End of sub-rule?", submitOnChange: true
				if(settings["endOfSub$state.n"]) {
					state.str = state.str + ")"
					state.eval << ")"
					paragraph(state.str)
					return
				}
			}
			inputRight(sub)
		}
	} 
}

def inputLeftAndRight(sub) {
	state.n = state.n + 1
	inputLeft(sub)
	inputRight(sub)
}

def stripBrackets(str) {
	def i = str.indexOf('[')
	def j = str.indexOf(']')
	def result = str.substring(0, i) + str.substring(i + 1, j) + str.substring(j + 1)
	return result
}

// Action selection code follows

def checkActTrue(dev, str) {
	if(dev) state.actsTrue = state.actsTrue + stripBrackets("$str") + "\n"
}

def setActTrue(str) {
	state.actsTrue = state.actsTrue + stripBrackets("$str") + "\n"
}

def addToActTrue(str) {
	state.actsTrue = state.actsTrue + str + "\n"
}

def buildActTrue(str, brackets) {
	state.actsTrue = state.actsTrue + (brackets ? stripBrackets("$str") : str)
}

def checkActFalse(dev, str) {
	if(dev) state.actsFalse = state.actsFalse + stripBrackets("$str") + "\n"
}

def setActFalse(str) {
	state.actsFalse = state.actsFalse + stripBrackets("$str") + "\n"
}

def addToActFalse(str) {
	state.actsFalse = state.actsFalse + str + "\n"
}

def buildActFalse(str, brackets) {
	state.actsFalse = state.actsFalse + (brackets ? stripBrackets("$str") : str)
}

def selectActionsTrue() {
	dynamicPage(name: "selectActionsTrue", title: "Select Actions for True", uninstall: false) {
		state.actsTrue = ""
		section("") {
			input "onSwitchTrue", "capability.switch", title: "Turn on these switches", multiple: true, required: false, submitOnChange: true
			checkActTrue(onSwitchTrue, "On: $onSwitchTrue")
			input "offSwitchTrue", "capability.switch", title: "Turn off these switches", multiple: true, required: false, submitOnChange: true
			checkActTrue(offSwitchTrue, "Off: $offSwitchTrue")
            if(state.isTrig) {
				input "toggleSwitchTrue", "capability.switch", title: "Toggle these switches", multiple: true, required: false, submitOnChange: true
				checkActTrue(toggleSwitchTrue, "Toggle: $toggleSwitchTrue")
            }
			input "delayedOffTrue", "capability.switch", title: "Turn on/off these switches after a delay (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(delayedOffTrue) {
				input "delayOnOffTrue", "bool", title: "Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
				input "delayMinutesTrue", "number", title: "Minutes of delay", required: true, range: "1..*", submitOnChange: true
				if(delayMinutesTrue) {
					def delayStrTrue = "Delayed " + (delayOnOffTrue ? "On:" : "Off:") + " $delayedOffTrue: $delayMinutesTrue minute"
					if(delayMinutesTrue > 1) delayStrTrue = delayStrTrue + "s"
					setActTrue(delayStrTrue)
				}
			}
            if(state.isRule) {
				input "pendedOffTrue", "capability.switch", title: "Turn on/off these switches after a delay, pending cancellation (default is OFF)", multiple: true, required: false, submitOnChange: true
				if(pendedOffTrue) {
					input "pendOnOffTrue", "bool", title: "Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
					input "pendMinutesTrue", "number", title: "Minutes of delay", required: true, range: "1..*", submitOnChange: true
					if(pendMinutesTrue) {
						def pendStrTrue = "Pending "+ (pendOnOffTrue ? "On:" : "Off:") + " $pendedOffTrue: $pendMinutesTrue minute"
						if(pendMinutesTrue > 1) pendStrTrue = pendStrTrue + "s"
						setActTrue(pendStrTrue)
					}
				}
            }
			input "dimATrue", "capability.switchLevel", title: "Set these dimmers", multiple: true, submitOnChange: true, required: false
			if(dimATrue) {
            	input "dimLATrue", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLATrue) setActTrue("Dim: $dimATrue: $dimLATrue")
            }
			input "dimBTrue", "capability.switchLevel", title: "Set these other dimmers", multiple: true, submitOnChange: true, required: false
			if(dimBTrue) {
            	input "dimLBTrue", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLBTrue) setActTrue("Dim: $dimBTrue: $dimLBTrue")
            }
            if(state.isTrig) {
				input "toggleDimmerTrue", "capability.switchLevel", title: "Toggle these dimmers", multiple: true, required: false, submitOnChange: true
				if(toggleDimmerTrue) input "dimTogTrue", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
				if(dimTogTrue) checkActTrue(toggleDimmerTrue, "Toggle: $toggleDimmerTrue: $dimTogTrue")
            }
			input "bulbsTrue", "capability.colorControl", title: "Set color for these bulbs", multiple: true, required: false, submitOnChange: true
			if(bulbsTrue) {
				input "colorTrue", "enum", title: "Bulb color?", required: true, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink", "Custom color"]
				input "colorLevelTrue", "number", title: "Bulb level?", required: false, submitOnChange: true, range: "0..100"
				buildActTrue("Color: $bulbsTrue ", true)
				if(colorTrue) {
                	if(colorTrue == "Custom color") {
                    	input "colorHexTrue", "number", title: "Input color value", required: true, submitOnChange: true, range: "0..100"
                        input "colorSatTrue", "number", title: "Input saturation value", required: true, submitOnChange: true, range: "0..100"
                    }
                	buildActTrue("$colorTrue ", false)
                    if(colorHexTrue) buildActTrue("$colorHexTrue:$colorSatTrue ", false)
                }
				if(colorLevelTrue) addToActTrue("Level: $colorLevelTrue")
			}            
			input "lockTrue", "capability.lock", title: "Lock these locks", multiple: true, required: false, submitOnChange: true
			checkActTrue(lockTrue, "Lock: $lockTrue")
			input "unlockTrue", "capability.lock", title: "Unlock these locks", multiple: true, required: false, submitOnChange: true
			checkActTrue(unlockTrue, "Unlock: $unlockTrue")
            if(state.isTrig) {
				input "fanAdjustTrue", "capability.switchLevel", title: "Adjust these fans - Low, Medium, High, Off", multiple: false, required: false, submitOnChange: true
				if(fanAdjustTrue) addToActTrue("Adjust Fan: $fanAdjustTrue")
            }
			input "openValveTrue", "capability.valve", title: "Open these valves", multiple: true, required: false, submitOnChange: true
			checkActTrue(openValveTrue, "Open: $openValveTrue")
			input "closeValveTrue", "capability.valve", title: "Close these valves", multiple: true, required: false, submitOnChange: true
			checkActTrue(closeValveTrue, "Close: $closeValveTrue")
			input "thermoTrue", "capability.thermostat", title: "Set these thermostats", multiple: true, required: false, submitOnChange: true
			if(thermoTrue) {
				input "thermoModeTrue", "enum", title: "Select thermostate mode", multiple: false, required: false, options: ["auto", "heat", "cool", "off"], submitOnChange: true
				input "thermoSetHeatTrue", "decimal", title: "Set heating point", multiple: false, required: false, submitOnChange: true
				input "thermoSetCoolTrue", "decimal", title: "Set cooling point", multiple: false, required: false, submitOnChange: true 
				input "thermoFanTrue", "enum", title: "Fan setting", multiple: false, required: false, submitOnChange: true, options: ["fanOn", "fanAuto"]
				buildActTrue("$thermoTrue: ", true)
				if(thermoModeTrue) buildActTrue("Mode: " + thermoModeTrue + " ", false)
				if(thermoSetHeatTrue) buildActTrue("Heat to $thermoSetHeatTrue ", false)
				if(thermoSetCoolTrue) buildActTrue("Cool to $thermoSetCoolTrue ", false)
				if(thermoFanTrue) buildActTrue("Fan setting $thermoFanTrue", false)
				addToActTrue("")
			}
			input "foscamMDTrue", "capability.imageCapture", title: "Turn On/Off IP Cam Motion Detection after a delay (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(foscamMDTrue) {
				input "delayMDOnOffTrue", "bool", title: "Turn ON after the delay", multiple: false, required: false, defaultValue: false, submitOnChange: true
				input "delayMDMinutesTrue", "number", title: "Minutes of delay", required: true, range: "1..*", submitOnChange: true
				if(delayMinutesTrue) {
					def delayMDStrTrue = "Delayed " + (delayMDOnOffTrue ? "On:" : "Off:") + " $foscamMDTrue: $delayMDMinutesTrue minute"
					if(delayMDMinutesTrue > 1) delayMDStrTrue = delayMDStrTrue + "s"
					setActTrue(delayMDStrTrue)
				}
			}			
			input "alarmTrue", "enum", title: "Set the alarm state", multiple: false, required: false, options: ["away" : "Arm (away)", "stay" : "Arm (stay)", "off" : "Disarm"], submitOnChange: true
			if(alarmTrue) addToActTrue("Alarm: " + (alarmTrue in ["away", "stay"] ? "Arm ($alarmTrue)" : "Disarm"))
			def myModes = []
			location.modes.each {myModes << "$it"}
			input "modeTrue", "enum", title: "Set the mode", multiple: false, required: false, options: myModes.sort(), submitOnChange: true
			if(modeTrue) addToActTrue("Mode: $modeTrue")
			def phrases = location.helloHome?.getPhrases()*.label
			input "myPhraseTrue", "enum", title: "Routine to run", required: false, options: phrases.sort(), submitOnChange: true
			if(myPhraseTrue) addToActTrue("Routine: $myPhraseTrue")
            def theseRules = parent.ruleList(app.label)
            input "ruleTrue", "enum", title: "Rules to evaluate", required: false, multiple: true, options: theseRules.sort(), submitOnChange: true
            if(ruleTrue) setActTrue("Rules: $ruleTrue")
			href "selectMsgTrue", title: "Send message", description: state.msgTrue ? state.msgTrue : "Tap to set", state: state.msgTrue ? "complete" : null
			if(state.msgTrue) addToActTrue(state.msgTrue)
            if(!randomTrue) {
				input "delayTrue", "number", title: "Delay " + (state.isRule ? "the effect of this rule" : "this action") + " by this many minutes", required: false, submitOnChange: true
				if(delayTrue) {
					def delayStr = "Delay Rule: $delayTrue minute"
					if(delayTrue > 1) delayStr = delayStr + "s"
					addToActTrue(delayStr)
				}
            }
            if(!delayTrue) {
				input "randomTrue", "number", title: "Delay " + (state.isRule ? "the effect of this rule" : "this action") + " by random minutes up to", required: false, submitOnChange: true
				if(randomTrue) {
					def randomStr = "Random Delay: $randomTrue minutes"
					addToActTrue(randomStr)
				}
            }
		}
        if(state.actsTrue) state.actsTrue = state.actsTrue[0..-2]
	}
}

def selectActionsFalse() {
	dynamicPage(name: "selectActionsFalse", title: "Select Actions for False", uninstall: false) {
		state.actsFalse = ""
		section("") {
			input "onSwitchFalse", "capability.switch", title: "Turn on these switches", multiple: true, required: false, submitOnChange: true
			checkActFalse(onSwitchFalse, "On: $onSwitchFalse")
			input "offSwitchFalse", "capability.switch", title: "Turn off these switches", multiple: true, required: false, submitOnChange: true
			checkActFalse(offSwitchFalse, "Off: $offSwitchFalse")
			input "delayedOffFalse", "capability.switch", title: "Turn on/off these switches after a delay (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(delayedOffFalse) {
				input "delayOnOffFalse", "bool", title: "Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
				input "delayMinutesFalse", "number", title: "Minutes of delay", required: true, range: "1..*", submitOnChange: true
				if(delayMinutesFalse) {
					def delayStrFalse = "Delayed " + (delayOnOffFalse ? "On:" : "Off:") + " $delayedOffFalse: $delayMinutesFalse minute"
					if(delayMinutesFalse > 1) delayStrFalse = delayStrFalse + "s"
					setActFalse(delayStrFalse)
				}
			}
			input "pendedOffFalse", "capability.switch", title: "Turn on/off these switches after a delay, pending cancellation (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(pendedOffFalse) {
				input "pendOnOffFalse", "bool", title: "Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
				input "pendMinutesFalse", "number", title: "Minutes of delay", required: true, range: "1..*", submitOnChange: true
				if(pendMinutesFalse) {
					def pendStrFalse = "Pending "+ (pendOnOffFalse ? "On:" : "Off:") + " $pendedOffFalse: $pendMinutesFalse minute"
					if(pendMinutesFalse > 1) pendStrFalse = pendStrFalse + "s"
					setActFalse(pendStrFalse)
				}
			}
			input "dimAFalse", "capability.switchLevel", title: "Set these dimmers", multiple: true, submitOnChange: true, required: false
			if(dimAFalse) {
            	input "dimLAFalse", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLAFalse) setActFalse("Dim: $dimAFalse: $dimLAFalse")
            }
			input "dimBFalse", "capability.switchLevel", title: "Set these other dimmers", multiple: true, submitOnChange: true, required: false
			if(dimBFalse) {
            	input "dimLBFalse", "number", title: "To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLBFalse) setActFalse("Dim: $dimBFalse: $dimLBFalse")
            }
			input "bulbsFalse", "capability.colorControl", title: "Set color for these bulbs", multiple: true, required: false, submitOnChange: true
			if(bulbsFalse) {
				input "colorFalse", "enum", title: "Bulb color?", required: true, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink", "Custom color"]
				input "colorLevelFalse", "number", title: "Bulb level?", required: false, submitOnChange: true, range: "0..100"
				buildActFalse("Color: $bulbsFalse ", true)
				if(colorFalse) {
                	if(colorFalse == "Custom color") {
                    	input "colorHexFalse", "number", title: "Input color value", required: true, submitOnChange: true, range: "0..100"
                        input "colorSatFalse", "number", title: "Input saturation value", required: true, submitOnChange: true, range: "0..100"
                    }
                	buildActFalse("$colorFalse ", false)
                    if(colorHexFalse) buildActFalse("$colorHexFalse:$colorSatFalse ", false)
                }
				if(colorLevelFalse) addToActFalse("Level: $colorLevelFalse")
			}            
			input "lockFalse", "capability.lock", title: "Lock these locks", multiple: true, required: false, submitOnChange: true
			checkActFalse(lockFalse, "Lock: $lockFalse")
			input "unlockFalse", "capability.lock", title: "Unlock these locks", multiple: true, required: false, submitOnChange: true
			checkActFalse(unlockFalse, "Unlock: $unlockFalse")
			input "openValveFalse", "capability.valve", title: "Open these valves", multiple: true, required: false, submitOnChange: true
			checkActFalse(openValveFalse, "Open: $openValveFalse")
			input "closeValveFalse", "capability.valve", title: "Close these valves", multiple: true, required: false, submitOnChange: true
			checkActFalse(closeValveFalse, "Close: $closeValveFalse")
			input "thermoFalse", "capability.thermostat", title: "Set these thermostats", multiple: true, required: false, submitOnChange: true
			if(thermoFalse) {
				input "thermoModeFalse", "enum", title: "Select thermostate mode", multiple: false, required: false, options: ["auto", "heat", "cool", "off"], submitOnChange: true
				input "thermoSetHeatFalse", "decimal", title: "Set heating point", multiple: false, required: false, submitOnChange: true
				input "thermoSetCoolFalse", "decimal", title: "Set cooling point", multiple: false, required: false, submitOnChange: true 
				input "thermoFanFalse", "enum", title: "Fan setting", multiple: false, required: false, submitOnChange: true, options: ["fanOn", "fanAuto"]
				buildActFalse("$thermoFalse: ", true)
				if(thermoModeFalse) buildActFalse("Mode: " + thermoModeFalse + " ", false)
				if(thermoSetHeatFalse) buildActFalse("Heat to $thermoSetHeatFalse ", false)
				if(thermoSetCoolFalse) buildActFalse("Cool to $thermoSetCoolFalse ", false)
				if(thermoFanFalse) buildActFalse("Fan setting $thermoFanFalse", false)
				addToActFalse("")
			}
			input "foscamMDFalse", "capability.switch", title: "Turn on/off IP Cam Motion Detection after a delay (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(foscamMDFalse) {
				input "delayMDOnOffFalse", "bool", title: "Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
				input "delayMDMinutesFalse", "number", title: "Minutes of delay", required: true, range: "1..*", submitOnChange: true
				if(delayMDMinutesFalse) {
					def delayMDStrFalse = "Delayed " + (delayMDOnOffFalse ? "On:" : "Off:") + " $foscamMDFalse: $delayMDMinutesFalse minute"
					if(delayMDMinutesFalse > 1) delayMDStrFalse = delayMDStrFalse + "s"
					setActFalse(delayMDStrFalse)
				}
			}			
			input "alarmFalse", "enum", title: "Set the alarm state", multiple: false, required: false, options: ["away" : "Arm (away)", "stay" : "Arm (stay)", "off" : "Disarm"], submitOnChange: true
			if(alarmFalse) addToActFalse("Alarm: " + (alarmFalse in ["away", "stay"] ? "Arm ($alarmFalse)" : "Disarm"))
			def myModes = []
			location.modes.each {myModes << "$it"}
			input "modeFalse", "enum", title: "Set the mode", multiple: false, required: false, options: myModes.sort(), submitOnChange: true
			if(modeFalse) addToActFalse("Mode: $modeFalse")
			def phrases = location.helloHome?.getPhrases()*.label
			input "myPhraseFalse", "enum", title: "Routine to run", required: false, options: phrases.sort(), submitOnChange: true
			if(myPhraseFalse) addToActFalse("Routine: $myPhraseFalse")
            def theseRules = parent.ruleList(app.label)
            input "ruleFalse", "enum", title: "Rules to evaluate", required: false, multiple: true, options: theseRules.sort(), submitOnChange: true
            if(ruleFalse) setActFalse("Rules: $ruleFalse")
			href "selectMsgFalse", title: "Send message", description: state.msgFalse ? state.msgFalse : "Tap to set", state: state.msgFalse ? "complete" : null
			if(state.msgFalse) addToActFalse(state.msgFalse)
			input "delayFalse", "number", title: "Delay the effect of this rule by this many minutes", required: false, submitOnChange: true
			if(delayFalse) {
				def delayStr = "Delay Rule: $delayFalse minute"
				if(delayFalse > 1) delayStr = delayStr + "s"
				addToActFalse(delayStr)
			}
			input "randomFalse", "number", title: "Delay the effect of this rule by random minutes up to", required: false, submitOnChange: true
			if(randomFalse) {
				def randomStr = "Random Delay: $randomFalse minutes"
				addToActFalse(randomStr)
			}
		}
        if(state.actsFalse) state.actsFalse = state.actsFalse[0..-2]
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

def selectMsgFalse() {
	dynamicPage(name: "selectMsgFalse", title: "Select Message and Destination", uninstall: false) {
		section("") {
			input "pushFalse", "bool", title: "Send Push Notification?", required: false, submitOnChange: true
			input "msgFalse", "text", title: "Custom message to send", required: false, submitOnChange: true
			input "phoneFalse", "phone", title: "Phone number for SMS", required: false, submitOnChange: true
		}
        state.msgFalse = (pushFalse ? "Push" : "") + (msgFalse ? " '$msgFalse'" : "") + (phoneFalse ? " to $phoneFalse" : "")
	}
}

// initialization code follows

def scheduleTimeOfDay() {
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
	schedule(start, "startHandler")
	schedule(stop, "stopHandler")
	if(startingX in ["Sunrise", "Sunset"] || endingX in ["Sunrise", "Sunset"])
		schedule("2015-01-09T00:00:29.000-0700", "scheduleTimeOfDay") // in case sunset/sunrise; change daily
}

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
    parent.unSubscribeRule(app.label)
	initialize()
}

def uninstalled() {
//	log.debug "uninstalled called"
	parent.removeChild(app.label)
}

def initialize() {
	for (int i = 1; i < state.howMany; i++) {
		def capab = (settings.find {it.key == "rCapab$i"}).value
		def myState = (settings.find {it.key == "state$i"})
        def myRelDev = settings.find {it.key == "relDevice$i"}
		if(myState) myState = myState.value
		switch(capab) {
			case "Mode": 
				subscribe(location, "mode", allHandler)
				break
			case "Smart Home Monitor": 
				subscribe(location, "alarmSystemStatus" + (state.isTrig ? ".$myState" : ""), allHandler)
				break
			case "Time of day":
				scheduleTimeOfDay()
				break
			case "Days of week":
				schedule("2015-01-09T00:00:10.000-0700", "runRule")
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
				subscribe((settings.find{it.key == "rDev$i"}).value, "carbonMonoxide" + (state.isTrig ? ".$myState" : ""), allHandler)
				break
			case "Smoke detector":
				subscribe((settings.find{it.key == "rDev$i"}).value, "smoke" + (state.isTrig ? ".$myState" : ""), allHandler)
				break
			case ["Presence"]:
				subscribe((settings.find{it.key == "rDev$i"}).value, "presence" + (state.isTrig ? (myState == "arrives" ? ".present" : ".not present") : ""), allHandler)
				break
			case "Button":
				subscribe((settings.find{it.key == "rDev$i"}).value, "button", allHandler)
				break
			case "Rule truth":
				parent.subscribeRule(app.label, (settings.find{it.key == "rDev$i"}).value, myState, allHandler)
				break
			case "Water sensor":
				subscribe((settings.find{it.key == "rDev$i"}).value, "water" + (state.isTrig ? ".$myState" : ""), allHandler)
				break
			case "Physical Switch":
				subscribe((settings.find{it.key == "rDev$i"}).value, "switch.$myState", physicalHandler)
				break
			case "Routine":
				subscribe(location, "routineExecuted", allHandler)
				break
			default:
				subscribe((settings.find{it.key == "rDev$i"}).value, (capab.toLowerCase() + (state.isTrig ? ".$myState" : "")), allHandler)
		}
	}
	state.success = null
	subscribe(disabled, "switch", disabledHandler)
	if(disabled) state.disabled = disabled.currentSwitch == "on"
	else state.disabled = false
	if(state.isRule) runRule(false)
}

// Main rule evaluation code follows

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

def checkCondAny(dev, stateX, cap, rel, relDev) {
	def result = false
	if     (cap == "Temperature") 	dev.currentTemperature.each 	{result = result || compare(it, rel, stateX, reldev ? relDev.currentTemperature : null)}
	else if(cap == "Humidity")	dev.currentHumidity.each    	{result = result || compare(it, rel, stateX, reldev ? relDev.currentHumidity : null)}
	else if(cap == "Illuminance") 	dev.currentIlluminance.each 	{result = result || compare(it, rel, stateX, reldev ? relDev.currentIlluminance : null)}
	else if(cap == "Dimmer level")	dev.currentLevel.each		{result = result || compare(it, rel, stateX, relDev ? relDev.currentLevel : null)}
	else if(cap == "Energy meter")	dev.currentEnergy.each		{result = result || compare(it, rel, stateX, relDev ? relDev.currentEnergy : null)}
	else if(cap == "Power meter")	dev.currentPower.each		{result = result || compare(it, rel, stateX, relDev ? relDev.currentPower : null)}
	else if(cap == "Battery")	dev.currentBattery.each		{result = result || compare(it, rel, stateX, relDev ? relDev.currentBattery : null)}
    else if(cap == "Rule truth")	dev.each {
    	def truth = null
        if(it == state.ourRule) truth = state.ourTruth
    	else truth = parent.currentRule(it)
    	result = result || "$stateX" == "$truth"
	} 
    else if(cap == "Water sensor")	result = stateX in dev.currentWater
	else if(cap == "Switch") 	result = stateX in dev.currentSwitch
	else if(cap == "Motion") 	result = stateX in dev.currentMotion
	else if(cap == "Acceleration") 	result = stateX in dev.currentAcceleration
	else if(cap == "Contact") 	result = stateX in dev.currentContact
	else if(cap == "Presence") 	result = stateX in dev.currentPresence
	else if(cap == "Smoke detector") 	result = stateX in dev.currentSmoke
	else if(cap == "Carbon monoxide detector") 	result = stateX in dev.currentCarbonMonoxide
	else if(cap == "Lock") 		result = stateX in dev.currentLock
//	log.debug "CheckAny $cap $result"
	return result
}


def checkCondAll(dev, stateX, cap, rel, relDev) {
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
	if     (cap == "Temperature") 		dev.currentTemperature.each 	{result = result && compare(it, rel, stateX, reldev ? relDev.currentTemperature : null)}
	else if(cap == "Humidity") 		dev.currentHumidity.each    	{result = result && compare(it, rel, stateX, reldev ? relDev.currentHumidity : null)}
	else if(cap == "Illuminance") 		dev.currentIlluminance.each 	{result = result && compare(it, rel, stateX, reldev ? relDev.currentIlluminance : null)}
	else if(cap == "Dimmer level")		dev.currentLevel.each		{result = result && compare(it, rel, stateX, reldev ? relDev.currentLevel : null)}
	else if(cap == "Energy meter")		dev.currentEnergy.each		{result = result && compare(it, rel, stateX, reldev ? relDev.currentEnergy : null)}
	else if(cap == "Power meter")		dev.currentPower.each		{result = result && compare(it, rel, stateX, reldev ? relDev.currentPower : null)}
	else if(cap == "Battery")		dev.currentBattery.each		{result = result && compare(it, rel, stateX, reldev ? relDev.currentBattery : null)}
    else if(cap == "Rule truth")	dev.each {
    											def rule = null
    											if(it == state.ourRule) rule = state.ourTruth
    											else rule = parent.currentRule(it)
    											result = result && "$stateX" == "$rule"
                                                }
	else if(cap == "Water sensor")		result = !(flip[stateX] in dev.currentSwitch)
	else if(cap == "Switch") 		result = !(flip[stateX] in dev.currentSwitch)
	else if(cap == "Motion") 		result = !(flip[stateX] in dev.currentMotion)
	else if(cap == "Acceleration") 		result = !(flip[stateX] in dev.currentAcceleration)
	else if(cap == "Contact") 		result = !(flip[stateX] in dev.currentContact)
	else if(cap == "Presence") 		result = !(flip[stateX] in dev.currentPresence)
	else if(cap == "Smoke detector") 	result = !(flip[stateX] in dev.currentSmoke)
	else if(cap == "Carbon monoxide detector") 	result = !(flip[stateX] in dev.currentCarbonMonoxide)
	else if(cap == "Lock") 			result = !(flip[stateX] in dev.currentLock)
//	log.debug "CheckAll $cap $result"
	return result
}


def getOperand(i) {
	def result = true
	def capab = (settings.find {it.key == "rCapab$i"}).value
	if     (capab == "Mode") result = modeOk
	else if(capab == "Time of day") result = timeOk
	else if(capab == "Days of week") result = daysOk
    else if(capab == "Smart Home Monitor") result = (settings.find {it.key == "state$i"}).value == location.currentState("alarmSystemStatus")?.value
	else {
		def myDev = 	settings.find {it.key == "rDev$i"}
		def myState = 	settings.find {it.key == "state$i"}
		def myRel = 	settings.find {it.key == "RelrDev$i"}
		def myAll = 	settings.find {it.key == "AllrDev$i"}
		def myRelDev =  settings.find {it.key == "relDevice$i"}
		if(myAll) {
			if(myAll.value) result = checkCondAll(myDev.value, myState ? myState.value : null, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : null)
			else result = checkCondAny(myDev.value, myState ? myState.value : null, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : null)
		} else result = checkCondAny(myDev.value, myState ? myState.value : null, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : null)
	}
//    log.debug "operand is $result"
	return result
}

def findRParen() {
	def noMatch = true
	while(noMatch) {
		if(state.eval[state.token] == ")") {
			if(state.parenLev == 0) return
			else state.parenLev = state.parenLev - 1
		} else if(state.eval[state.token] == "(") state.parenLev = state.parenLev + 1
		state.token = state.token + 1
		if(state.token >= state.eval.size) return
	}
}

def disEval() {
    if(state.eval[state.token] == "(") {
    	state.parenLev = 0
        findRParen()
    }
    if(state.token >= state.eval.size) return
    state.token = state.token + 1
}

def evalTerm() {
	def result = true
	def thisTok = state.eval[state.token]
	if (thisTok == "(") {
		state.token = state.token + 1
		result = eval()
	} else result = getOperand(thisTok)
	state.token = state.token + 1
	return result
}

def eval() {
	def result = evalTerm()
	while(true) {
		if(state.token >= state.eval.size) return result
		def thisTok = state.eval[state.token]
		if (thisTok == "OR") {
			if(result) {
				disEval()
				return true
			} 
		} else if (thisTok == "AND") {
			if(!result) {
				disEval()
				return false
			} 
		} else if (thisTok == ")") return result
		state.token = state.token + 1
		result = evalTerm()
	}
}

// Run the evaluation and take action code follows

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

def doDelayTrue(time, rand) {
	def myTime = time
    if(rand) myTime = Math.random()*time
	runIn(myTime * 60, delayRuleTrue)
	def delayStr = "minute"
	if(time > 1) delayStr = delayStr + "s"
	if(logging) {
    	if(state.isRule) log.info ("$app.label is True, but " + (rand ? "random delay, up to $time minutes" : "delayed by $time $delayStr"))
    	else log.info (rand ? "Random delay, up to $time minutes" : "Delayed by $time $delayStr")
    }
}

def doDelayFalse(time, rand) {
	def myTime = time
    if(rand) myTime = Math.random()*time
	runIn(myTime * 60, delayRuleFalse)
	def delayStr = "minute"
	if(time > 1) delayStr = delayStr + "s"
	if(logging) {
    	if(state.isRule) log.info ("$app.label is False, but " + (rand ? "random delay, up to $time minutes" : "delayed by $time $delayStr"))
    	else log.info (rand ? "Random delay, up to $time minutes" : "Delayed by $time $delayStr")
    }
	state.success = success
}

def runRule(delay) {
	if(!allOk) return
	state.token = 0
	def success = eval()
	if((success != state.success) || delay) {
		unschedule(delayRuleTrue)
		unschedule(delayRuleFalse)
		if     (delayTrue > 0 && !delay && success)	doDelayTrue(delayTrue, false)
		else if(delayFalse > 0 && !delay && !success)	doDelayFalse(delayFalse, false)
    	else if(randomTrue > 0 && !delay) doDelayTrue(randomTrue, true)
    	else if(randomFalse > 0 && !delay) doDelayFalse(randomFalse, true)
		else {
            parent.setRuleTruth(app.label, success)
        	if(success) {
				if(onSwitchTrue) 	onSwitchTrue.on()
				if(offSwitchTrue) 	offSwitchTrue.off()
				if(delayedOffTrue)	runIn(delayMinutesTrue * 60, delayOffTrue)
				if(foscamMDTrue)	runIn(delayMDMinutesTrue * 60, delayFoscamMDTrue)
				if(pendedOffTrue)	runIn(pendMinutesTrue * 60, pendingOffTrue)
				if(pendedOffFalse)	unschedule(pendingOffFalse)
				if(dimATrue) 		dimATrue.setLevel(dimLATrue)
				if(dimBTrue) 		dimBTrue.setLevel(dimLBTrue)
				if(bulbsTrue)		setColor(true)
				if(lockTrue) 		lockTrue.lock()
				if(unlockTrue) 		unlockTrue.unlock()
				if(openValveTrue)	openValveTrue.open()
				if(closeValveTrue)	closeValveTrue.close()
				if(thermoTrue)		{	if(thermoModeTrue) 	thermoTrue.setThermostatMode(thermoModeTrue)
								if(thermoSetHeatTrue)	thermoTrue.setHeatingSetpoint(thermoSetHeatTrue)
								if(thermoSetCoolTrue)	thermoTrue.setCoolingSetpoint(thermoSetCoolTrue) 	
                                				if(thermoFanTrue) 	thermoTrue.setThermostatFanMode(thermoFanTrue)   }
				if(alarmTrue)		sendLocationEvent(name: "alarmSystemStatus", value: "$alarmTrue")
				if(modeTrue) 		setLocationMode(modeTrue)
        		if(ruleTrue)		parent.runRule(ruleTrue, app.label)
				if(myPhraseTrue)	location.helloHome.execute(myPhraseTrue)
				if(pushTrue)		sendPush(msgTrue ?: "Rule $app.label True")
				if(phoneTrue)		sendSms(phoneTrue, msgTrue ?: "Rule $app.label True")
			} else {
				if(onSwitchFalse) 	onSwitchFalse.on()
				if(offSwitchFalse) 	offSwitchFalse.off()
				if(delayedOffFalse)	runIn(delayMinutesFalse * 60, delayOffFalse)
				if(pendedOffFalse)	runIn(pendMinutesFalse * 60, pendingOffFalse)
				if(pendedOffTrue)	unschedule(pendingOffTrue)
				if(dimAFalse) 		dimAFalse.setLevel(dimLAFalse)
				if(dimBFalse) 		dimBFalse.setLevel(dimLBFalse)
				if(bulbsFalse)		setColor(false)
				if(lockFalse) 		lockFalse.lock()
				if(unlockFalse) 	unlockFalse.unlock()
				if(openValveFalse)	openValveFalse.open()
				if(closeValveFalse)	closeValveFalse.close()
				if(thermoFalse)		{	if(thermoModeFalse) 	thermoFalse.setThermostatMode(thermoModeFalse)
								if(thermoSetHeatFalse) 	thermoFalse.setHeatingSetpoint(thermoSetHeatFalse)
								if(thermoSetCoolFalse) 	thermoFalse.setCoolingSetpoint(thermoSetCoolFalse) 	
                                				if(thermoFanFalse)	thermoFalse.setThermostatFanMode(thermoFanFalse)   }
				if(foscamMDFalse)	runIn(delayMDMinutesFalse * 60, delayFoscamMDFalse)
				if(alarmFalse)		sendLocationEvent(name: "alarmSystemStatus", value: "$alarmFalse")
				if(modeFalse) 		setLocationMode(modeFalse)
        		if(ruleFalse)		parent.runRule(ruleFalse, app.label)
				if(myPhraseFalse) 	location.helloHome.execute(myPhraseFalse)
				if(pushFalse)		sendPush(msgFalse ?: "Rule $app.label False")
				if(phoneFalse)		sendSms(phoneFalse, msgFalse ?: "Rule $app.label False")
			}
			state.success = success
			if(logging) log.info (success ? "$app.label is True" : "$app.label is False")
		}
	}
}

def doTrigger(delay) {
	if(!allOk) return
	if(delay) unschedule(delayRuleTrue)
	if(delayTrue > 0 && !delay) doDelayTrue(delayTrue, false)
    else if(randomTrue > 0 && !delay) doDelayTrue(randomTrue, true)
	else {
		if(onSwitchTrue) 		onSwitchTrue.on()
        if(refreshSwitchTrue)	refreshSwitchTrue.refresh()
        if(pollSwitchTrue)		pollSwitchTrue.poll()
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
		if(foscamMDTrue)	runIn(delayMDMinutesTrue * 60, delayFoscamMDTrue)
		if(alarmTrue)			sendLocationEvent(name: "alarmSystemStatus", value: "$alarmTrue")
		if(modeTrue) 			setLocationMode(modeTrue)
        if(ruleTrue)			parent.runRule(ruleTrue, app.label)
		if(myPhraseTrue)		location.helloHome.execute(myPhraseTrue)
		if(pushTrue)			sendPush(msgTrue ?: "Rule $app.label True")
		if(phoneTrue)			sendSms(phoneTrue, msgTrue ?: "Rule $app.label True")
	}
}

def getButton(dev, evt, i) {
	def buttonNumber = evt.data 
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
            if(result) return true
		}
        return false
    }
	for(int i = 1; i < state.howMany; i++) {
		def myDev = (settings.find {it.key == "rDev$i"}).value
        log.debug "testEvt: $myDev, $i, $evt.displayName"
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
    if(state.isTrig) {
		def doit = true
		if(evt.name in ["temperature", "humidity", "power", "energy", "battery", "illuminance", "mode", "button", "routineExecuted"]) doit = testEvt(evt)
		if (doit) doTrigger(false)
    } else runRule(false)
}

def startHandler() {
	runRule(false)
}

def stopHandler() {
	runRule(false)
}

def timeHandler() {
	if(state.isRule) runRule(false) else doTrigger(false)
}

def physicalHandler(evt) {
	if(logging) log.info "$app.label: Physical $evt.displayName $evt.name $evt.value"
	if(evt.isPhysical()) doTrigger(false)
}

def delayOffTrue() {
	if(!allOk) return
	if(delayOnOffTrue) delayedOffTrue.on() else delayedOffTrue.off()
}

def pendingOffTrue() {
	if(!allOk) return
	if(pendOnOffTrue) pendedOffTrue.on() else pendedOffTrue.off()
}

def delayFoscamMDTrue() {
	if(!allOk) return
	if(delayMDOnOffTrue) foscamMDTrue?.alarmOn() else foscamMDTrue?.alarmOff()
}

def delayOffFalse() {
	if(!allOk) return
	if(delayOnOffFalse) delayedOffFalse.on() else delayedOffFalse.off()
}

def pendingOffFalse() {
	if(!allOk) return
	if(pendOnOffFalse) pendedOffFalse.on() else pendedOffFalse.off()
}

def delayFoscamMDFalse() {
	if(!allOk) return
	if(delayMDOnOffFalse) foscamMDFalse?.alarmOn() else foscamMDFalse?.alarmOff()
}

def delayRuleTrue() {
	if(state.isRule) runRule(true) else doTrigger(true)
}

def delayRuleFalse() {
	runRule(true)
}

def disabledHandler(evt) {
	state.disabled = evt.value == "on"
}

def ruleHandler(rule, truth) {
	if(logging) log.info "$app.label: $rule is $truth"
    state.ourRule = rule
    state.ourTruth = truth
    if(state.isRule) runRule(true) else doTrigger(true)
}

def ruleEvaluator(rule) {
	if(logging) log.info "$app.label: $rule evaluate"
    runRule(true)
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
	if(state.isRule) modeZOk && !state.disabled  //&& daysOk && timeOk
    else modeYOk && daysOk && timeOk && !state.disabled
}

private hideOptionsSection() {
	if(state.isRule) (modesZ || advanced) ? false : true
    else (starting || ending || days || modes || startingX || endingX || disabled) ? false : true
}

private getModeZOk() {
	def result = !modesZ || modesZ.contains(location.mode)
//	log.trace "modeZOk = $result"
	return result
}

private getModeYOk() {
	def result = !modesY || modesY.contains(location.mode)
//	log.trace "modeZOk = $result"
	return result
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
			hueColor = 35
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
		case "Custom color":
			hueColor = trufal ? colorHexTrue : colorHexFalse
            saturation = trufal ? colorSatTrue : colorSatFalse
			break;
	}
	def lightLevel = trufal ? colorLevelTrue : colorLevelFalse
	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
	if(trufal) bulbsTrue.setColor(newValue) else bulbsFalse.setColor(newValue)
	if(lightLevel == 0) {if(trufal) bulbsTrue.off() else bulbsFalse.off()}
}