/**
 *  Rule
 *
 *  Copyright 2015, 2016 Bruce Ravenel
 *
 *  Version 1.7.12d   20 Feb 2016
 *
 *	Version History
 *
 *	1.7.12	19 Feb 2016		Added Private Boolean enable/disable, capture/restore color hue and saturation
 *	1.7.11	15 Feb 2016		Further UI redesign to better distinguish triggers, added seconds for delayed on/off
 *	1.7.10	9 Feb 2016		Added Music player condition, fixed Days of Week schedule bug
 *	1.7.9	8 Feb 2016		Added set Boolean for other Rules, and Send notification event
 *	1.7.8	7 Feb 2016		Added Evaluate Rule after Delay (loop possible), and Private Boolean
 *	1.7.7	6 Feb 2016		UI cleanup and organization, added capture/restore for switches/dimmers
 *	1.7.6	5 Feb 2016		Added action to update rule(s) to fix broken schedules due to ST issues
 *	1.7.5	3 Feb 2016		Removed use of unschedule() for delay cancel, to avoid ST issues
 *	1.7.4	2 Feb 2016		Redesign of UI to make it clearer between Triggers and Rules
 *	1.7.3	2 Feb 2016		Bug fix for multi-button device with more than 4 buttons
 *	1.7.2	31 Jan 2016		Added mode based dimming action, and cause rule actions action
 *	1.7.1	30 Jan 2016		Added support for more buttons than 4 on button device, now as many as 20
 *	1.7.0	27 Jan 2016		Fixed thermostat mode trigger/condition, added thermostat operating state condition
 *	1.6.13	17 Jan 2016		Added TTS support
 *	1.6.12	10 Jan 2016		Bug fix re removing parts of a rule
 *	1.6.11	8 Jan 2016		Added offset to compare to device, fixed bugs in compare to device
 *	1.6.10	6 Jan 2016		Returned Delay on/off pending cancel per user request, further debug of rule evaluation
 *	1.6.9	6 Jan 2016		Fixed bugs related to presence in triggers, added Off as disable option, fixed bug in rule evaluation
 *	1.6.8	1 Jan 2016		Added version numbers to main Rule Machine page, multi SMS
 *	1.6.7	31 Dec 2015		Added speak to send message
 *	1.6.6	30 Dec 2015		Expert multi-commands added per Maxwell
 *	1.6.5	29 Dec 2015		Added action to set dimmers from a track dimmer, restored turn on/off after delay action
 *	1.6.4	29 Dec 2015		Added action to adjust dimmers +/-, fixed time bug for triggered rule, fixed dimmer level condition bug
 *	1.6.3	26 Dec 2015		Added color temperature bulb set, per John-Paul Smith
 *	1.6.2	26 Dec 2015		New delay selection, minor bug fixes, sub-rule input improvements
 *	1.6.1	24 Dec 2015		Added ability to send device name with push or SMS, show rule truth on main page
 *	1.6.0	23 Dec 2015		Added expert commands per Mike Maxwell, and actions for camera to take photo burst
 *	1.5.11	23 Dec 2015		Fixed bug that prevented old triggers from running, minor UI change for rule display
 *	1.5.10	22 Dec 2015		Require capability choice for all but last rule or trigger
 *	1.5.9	21 Dec 2015		Fixed overlap of Days of Week selection
 *	1.5.8	20 Dec 2015		More repair for that same mode bug; fixed so triggered-rule not tested at install
 *	1.5.7	19 Dec 2015		Fixed bug re: selecting mode as condition/trigger, UI display
 *	1.5.6	18 Dec 2015		Fixed bug re: old triggers not editable
 *	1.5.5	17 Dec 2015		Added milliseconds to Delayed off, uses dev.off([delay: msec]) instead of runIn()
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
	namespace: "bravenel",
	author: "Bruce Ravenel and Mike Maxwell",
	description: "Rule",
	category: "Convenience",
	parent: "bravenel:Rule Machine",
	iconUrl: "https://raw.githubusercontent.com/bravenel/Rule-Trigger/master/smartapps/bravenel/RuleMachine.png",
	iconX2Url: "https://raw.githubusercontent.com/bravenel/Rule-Trigger/master/smartapps/bravenel/RuleMachine%402x.png",
)

preferences {
	page(name: "mainPage")
	page(name: "selectTrig")
	page(name: "selectCTrig")
	page(name: "selectRule")
	page(name: "selectActions")
	page(name: "selectTriggers")
	page(name: "selectConditions")
	page(name: "defineRule")
	page(name: "certainTime")
	page(name: "certainTimeX")
	page(name: "atCertainTime")
	page(name: "selectActionsTrue")
	page(name: "selectActionsFalse")
	page(name: "delayTruePage")
	page(name: "delayFalsePage")
	page(name: "selectMsgTrue")
	page(name: "selectMsgFalse")
	page(name: "selectCustomActions")
}

//
//	
//
def mainPage() {
	//version to parent app and expert settings for rule
	try { 
		state.isExpert = parent.isExpert("1.7.12d") 
		if (state.isExpert) state.cstCmds = parent.getCommands()
		else state.cstCmds = []
	}
	catch (e) {log.error "Please update Rule Machine to V1.6 or later"}
	if(state.private == null) state.private = "true"
	def myTitle = "Define a Rule, Trigger or Actions\n"
	if(state.howManyT > 1 || state.isTrig) myTitle = "Define a Trigger"
	else if(state.howMany > 1) myTitle = "Define a Rule"
	else if(app.label != null) myTitle = "Define Actions"
	def myUninstall = state.isTrig || state.isRule || state.howManyT > 1 || state.howMany > 1 || (app.label != "Rule" && app.label != null)
	dynamicPage(name: "mainPage", title: myTitle, uninstall: myUninstall, install: myUninstall) {
		if(state.isTrig) {    // old Trigger
			section() {     
				label title: "Name the Trigger", required: true
				def condLabel = conditionLabel()
				href "selectConditions", title: "Select Trigger Events", description: condLabel ? (condLabel) : "Tap to set", required: true, state: condLabel ? "complete" : null, submitOnChange: true
				href "selectActionsTrue", title: "Select Actions", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null
			}
			section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
				def timeLabel = timeIntervalLabel()
				href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
				input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
					options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
				input "modesY", "mode", title: "Only when mode is", multiple: true, required: false            
				input "disabled", "capability.switch", title: "Switch to disable trigger when ON", required: false, multiple: false
			}    
		} else if(state.isRule) {   // old Rule
			section() { 
				label title: "Name the Rule", required: true
				def condLabel = conditionLabel()
				href "selectConditions", title: "Select Conditions", description: condLabel ? (condLabel) : "Tap to set", required: true, state: condLabel ? "complete" : null, submitOnChange: true
				href "defineRule", title: "Define the Rule", description: state.str ? (state.str) : "Tap to set", state: state.str ? "complete" : null, submitOnChange: true
				href "selectActionsTrue", title: "Select Actions for True", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null, submitOnChange: true
				href "selectActionsFalse", title: "Select Actions for False", description: state.actsFalse ? state.actsFalse : "Tap to set", state: state.actsFalse ? "complete" : null, submitOnChange: true
			}
			section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
				input "modesZ", "mode", title: "Evaluate only when mode is", multiple: true, required: false
				input "disabled", "capability.switch", title: "Switch to disable rule when ON", required: false, multiple: false
			}   
		} 
        else if(state.howMany > 1 && state.howManyT in [null, 1]) 	getRule()		// Existing Rule	
		else if(state.howManyT > 1 && state.howMany in [null, 1]) 	getTrigger()   	// Existing Trigger
		else if(state.howManyT > 1) 								getCTrigger()   // Existing Conditional Trigger
		else if(app.label != "Rule" && app.label != null) 			getActions()	// Existing Actions
        else {																		// New Rule, Trigger, Conditional Trigger or Actions
            section("A Rule uses events for conditions and then\ntests a rule to run actions") 							{href "selectRule", title: "Define a Rule", description: "Tap to set"}
            section("A Trigger uses events to run actions") 														{href "selectTrig", title: "Define a Trigger", description: "Tap to set"}
            section("A Conditional Trigger uses events to run actions\nbased on conditions tested under a rule") 	{href "selectCTrig", title: "Define a Conditional Trigger", description: "Tap to set"}
            section("Other Rules can run these Actions") 															{href "selectActions", title: "Define Actions", description: "Tap to set"}
        }
    }
}

def selectRule() {
	dynamicPage(name: "selectRule", title: "Select Conditions, Rule and Actions", uninstall: true, install: true) {
    	getRule()
	}
}

def selectTrig() {
	dynamicPage(name: "selectTrig", title: "Select Trigger Events and Actions", uninstall: true, install: true) {
		getTrigger()
	}
}

def selectCTrig() {
	dynamicPage(name: "selectCTrig", title: "Select Triggers, Conditions, Rule and Actions", uninstall: true, install: true) {
		getCTrigger()
	}
}

def selectActions() {
	dynamicPage(name: "selectActions", title: "Select Actions", uninstall: true, install: true) {
		getActions()
	}
}

def getRule() {
    section() { 
		label title: "Name the Rule", required: true
		def condLabel = conditionLabel()
		href "selectConditions", title: "Select Conditions ", description: condLabel ? (condLabel) : "Tap to set", state: condLabel ? "complete" : null, submitOnChange: true
		def ruleLabel = rulLabl()
		href "defineRule", title: "Define Rule", description: ruleLabel ? (ruleLabel) : "Tap to set", state: ruleLabel ? "complete" : null, submitOnChange: true
		href "selectActionsTrue", title: "Select Actions for True", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null, submitOnChange: true
		href "selectActionsFalse", title: "Select Actions for False", description: state.actsFalse ? state.actsFalse : "Tap to set", state: state.actsFalse ? "complete" : null, submitOnChange: true
	}
    getMoreOptions()
}

def getTrigger() {
	section() { 
		label title: "Name the Trigger", required: true
		def trigLabel = triggerLabel()
		href "selectTriggers", title: "Select Trigger Events", description: trigLabel ? (trigLabel) : "Tap to set", state: trigLabel ? "complete" : null, submitOnChange: true
		href "selectActionsTrue", title: "Select Actions", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null, submitOnChange: true
	}
    getMoreOptions()
}

def getCTrigger() {
	section() { 
		label title: "Name the Conditional Trigger", required: true
		def trigLabel = triggerLabel()
		href "selectTriggers", title: "Select Trigger Events", description: trigLabel ? (trigLabel) : "Tap to set", state: trigLabel ? "complete" : null, submitOnChange: true
		def condLabel = conditionLabel()
		href "selectConditions", title: "Select Conditions ", description: condLabel ? (condLabel) : "Tap to set", state: condLabel ? "complete" : null, submitOnChange: true
		def ruleLabel = rulLabl()
		href "defineRule", title: "Define Rule", description: ruleLabel ? (ruleLabel) : "Tap to set", state: ruleLabel ? "complete" : null, submitOnChange: true
		href "selectActionsTrue", title: "Select Actions for True", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null, submitOnChange: true
		href "selectActionsFalse", title: "Select Actions for False", description: state.actsFalse ? state.actsFalse : "Tap to set", state: state.actsFalse ? "complete" : null, submitOnChange: true
	}
    getMoreOptions()
}

def getActions() {
	section() { 
		label title: "Name the Actions", required: true
		href "selectActionsTrue", title: "Select Actions", description: state.actsTrue ? state.actsTrue : "Tap to set", state: state.actsTrue ? "complete" : null, submitOnChange: true
	}
    getMoreOptions()
}

def getMoreOptions() {
	section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
		def timeLabel = timeIntervalLabel()
		href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
		input "daysY", "enum", title: "Only on certain days of the week", multiple: true, required: false,
			options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
		input "modesY", "mode", title: "Only when mode is", multiple: true, required: false            
		input "disabled", "capability.switch", title: "Switch to disable Rule", required: false, multiple: false, submitOnChange: true
        if(disabled) input "disabledOff", "bool", title: "Disable when Off? On is default", required: false, defaultValue: false
        input "usePrivateDisable", "bool", title: "Enable/Disable with Private Boolean?", required: false
	}    
}

def certainTime() {
	dynamicPage(name: "certainTime", title: "Only during a certain time", uninstall: false) {
		section() {
			input "startingX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true, required: false
			if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false
			else {
				if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
		section() {
			input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true, required: false
			if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false
			else {
				if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
	}
}

// Trigger and Condition input code follows

def selectTriggers() {
	selectTrigCond(true)
}

def selectConditions() {
	selectTrigCond(false)
}

def selectTrigCond(isTrig) {
	def ctStr = isTrig ? "tCapab" : "rCapab"
	def ct = settings.findAll{it.key.startsWith(ctStr)}
	def howMany = ct.size() + 1
	if(isTrig) state.howManyT = howMany 
	else state.howMany = howMany
	def excludes = (state.isTrig || isTrig) ? ["Certain Time", "Mode", "Routine", "Button", "Smart Home Monitor", "Private Boolean"] : ["Time of day", "Days of week", "Mode", "Smart Home Monitor", "Private Boolean"]
	def pageName = isTrig ? "selectTriggers" : "selectConditions"
	dynamicPage(name: pageName, title: (state.isTrig || isTrig) ? "Select Trigger Events (ANY will trigger)" : "Select Conditions", uninstall: false) {
		for (int i = 1; i <= howMany; i++) {
		def thisCapab = isTrig ? "tCapab$i" : "rCapab$i"
		section((state.isTrig || isTrig) ? "Event Trigger #$i" : "Condition #$i") {            	
				getCapab(thisCapab, isTrig, i < howMany)
				def myCapab = settings.find {it.key == thisCapab}
				if(myCapab) {
					def xCapab = myCapab.value 
					if(!(xCapab in excludes)) {
						def thisDev = isTrig ? "tDev$i" : "rDev$i"
						getDevs(xCapab, thisDev, true)
						def myDev = settings.find {it.key == thisDev}
						if(myDev) if(myDev.value.size() > 1 && (xCapab != "Rule truth" || state.isRule)) getAnyAll(thisDev)
						if(xCapab in ["Temperature", "Humidity", "Illuminance", "Dimmer level", "Energy meter", "Power meter", "Battery"]) getRelational(thisDev)
					} else if(xCapab == "Button") getButton(isTrig ? "tDev$i" : "rDev$i")
					getState(xCapab, i, isTrig)
				}
			}
		}
	}
}

def getCapab(myCapab, isTrig, isReq) {  
	def myOptions = null
	if(state.isRule || !isTrig) myOptions = ["Acceleration", "Battery", "Carbon monoxide detector", "Contact", "Days of week", "Dimmer level", "Energy meter", "Garage door", "Humidity", "Illuminance", "Lock", 
    	"Mode", "Motion", "Power meter", "Presence", "Rule truth", "Smart Home Monitor", "Smoke detector", "Switch", "Temperature", "Private Boolean", 
        "Thermostat Mode", "Thermostat State", "Time of day", "Water sensor", "Music player"]
	if(state.isTrig || isTrig) myOptions = ["Acceleration", "Battery", "Button", "Carbon monoxide detector", "Certain Time", "Contact", "Dimmer level", "Energy meter", "Garage door", "Humidity", "Illuminance", 
    	"Lock", "Mode", "Motion", "Physical Switch", "Power meter", "Presence", "Routine", "Rule truth", "Smart Home Monitor", "Smoke detector", "Switch", "Temperature",
        "Thermostat Mode", "Thermostat State", "Water sensor", "Private Boolean", "Music player"]
	def result = input myCapab, "enum", title: "Select capability", required: isReq, options: myOptions.sort(), submitOnChange: true
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
		case "Garage door":
			thisName = "Garage doors"
			thisCapab = "garageDoorControl"
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
		case "Thermostat Mode":
			thisName = "Thermostat" + (multi ? "s" : "")
			thisCapab = "thermostat"
			break
		case "Thermostat State":
			thisName = "Thermostat" + (multi ? "s" : "")
			thisCapab = "thermostat"
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
		case "Music player":
			thisName = "Music player"
			thisCapab = "musicPlayer"
			break
		case "Rule truth":
			def theseRules = parent.ruleList(app.label)
			def result = input dev, "enum", title: "Rules", required: true, multiple: multi, submitOnChange: true, options: theseRules.sort()
			return result
		case "Battery":
			thisName = multi ? "Batteries" : "Battery"
			thisCapab = "battery"
	}
	def result = input dev, "capability.$thisCapab", title: thisName, required: true, multiple: multi, submitOnChange: true
}

def getAnyAll(myDev) {
	def result = input "All$myDev", "bool", title: "All of these?", required: false
}

def getRelational(myDev) {
	def result = input "Rel$myDev", "enum", title: "Choose comparison", required: true, options: ["=", "!=", "<", ">", "<=", ">="]
}

def getButton(dev) {
	def numNames = ["one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
    	"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"]
 	def result = input "$dev", "capability.button", title: "Button Device", required: true, multiple: false, submitOnChange: true
	def thisDev = settings.find{it.key == "$dev"}
	if(thisDev) {
		input "numButtons$dev", "number", title: "Number of buttons? (Default 4)", range: "1..20", required: false, submitOnChange: true, description: "4"
		def numButtons = settings.find{it.key == "numButtons$dev"}
		numButtons = numButtons ? numButtons.value : 4
		def butOpts = ["one"]
		if(numButtons > 1) for (int i = 1; i < numButtons; i++) butOpts[i] = numNames[i]
		input "Button$dev", "enum", title: "Button number", required: true, multiple: false, submitOnChange: true, options: butOpts
	}
}

def getState(myCapab, n, isTrig) {
	def result = null
	def myState = isTrig ? "tstate$n" : "state$n"
	def myIsDev = isTrig ? "istDev$n" : "isDev$n"
	def myRelDev = isTrig ? "reltDevice$n" : "relDevice$n"
	def isRule = state.isRule || (state.howMany > 1 && !isTrig)
	def phrase = isRule ? "state" : "becomes"
	def swphrase = isRule ? "state" : "turns"
	def presoptions = isRule ? ["present", "not present"] : ["arrives", "leaves"]
	def presdefault = isRule ? "present" : "arrives"
	def lockphrase = isRule ? "state" : "is"
	def days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
	if     (myCapab == "Switch")					result = input myState, "enum", title: "Switch $swphrase", 			options: ["on", "off"], 					defaultValue: "on"
	else if(myCapab == "Physical Switch")			result = input myState, "enum", title: "Switch turns ", 			options: ["on", "off"], 					defaultValue: "on"
	else if(myCapab == "Motion")					result = input myState, "enum", title: "Motion $phrase", 			options: ["active", "inactive"], 			defaultValue: "active"
	else if(myCapab == "Acceleration")				result = input myState, "enum", title: "Acceleration $phrase", 		options: ["active", "inactive"], 			defaultValue: "active"
	else if(myCapab == "Contact")					result = input myState, "enum", title: "Contact $phrase", 			options: ["open", "closed"], 				defaultValue: "open"
	else if(myCapab == "Presence")					result = input myState, "enum", title: "Presence $phrase", 			options: presoptions, 						defaultValue: presdefault
	else if(myCapab == "Garage door")				result = input myState, "enum", title: "Garage door $phrase", 		options: ["closed", "open"], 				defaultValue: "open"
	else if(myCapab == "Lock")						result = input myState, "enum", title: "Lock $lockphrase", 			options: ["locked", "unlocked"], 			defaultValue: "unlocked"
	else if(myCapab == "Thermostat Mode")			result = input myState, "enum", title: "Thermostat mode ",			options: ["heat", "cool", "auto", "off", "emergency heat"],	defaultValue: "heat"
	else if(myCapab == "Thermostat State")			result = input myState, "enum", title: "Thermostat state ",			options: ["heating", "idle", "pending cool", "vent economizer", "cooling", "pending heat", "fan only"],	defaultValue: "heating"
	else if(myCapab == "Carbon monoxide detector")	result = input myState, "enum", title: "CO $phrase ", 				options: ["clear", ,"detected", "tested"], 	defaultValue: "detected"
	else if(myCapab == "Smoke detector")			result = input myState, "enum", title: "Smoke $phrase ", 			options: ["clear", ,"detected", "tested"], 	defaultValue: "detected"
	else if(myCapab == "Water sensor")				result = input myState, "enum", title: "Water $phrase", 			options: ["dry", "wet"], 					defaultValue: "wet"
	else if(myCapab == "Button")					result = input myState, "enum", title: "Button pushed or held ", 	options: ["pushed", "held"], 				defaultValue: "pushed"
	else if(myCapab == "Rule truth")				result = input myState, "enum", title: "Rule truth $phrase ", 		options: ["true", "false"], 				defaultValue: "true"
	else if(myCapab == "Music player")				result = input myState, "enum", title: "Playing state", 			options: ["playing", "paused","stopped"], 	defaultValue: "playing"
	else if(myCapab == "Private Boolean")			result = input myState, "enum", title: "Private Boolean $phrase ", 	options: ["true", "false"], 				defaultValue: "true"
	else if(myCapab == "Smart Home Monitor")		result = input myState, "enum", title: "SHM $phrase", 				options: ["away" : "Arm (away)", "stay" : "Arm (stay)", "off" : "Disarm"]
	else if(myCapab in ["Temperature", "Humidity", "Illuminance", "Energy meter", "Power meter", "Battery", "Dimmer level"]) {
		input myIsDev, "bool", title: "Relative to another device?", multiple: false, required: false, submitOnChange: true, defaultValue: false
		def myDev = settings.find {it.key == myIsDev}
		if(myDev && myDev.value) {
			getDevs(myCapab, myRelDev, false)
			if(myCapab == "Temperature") 				result = input myState, "decimal",	title: "Temperature offset ", 	range: "*..*",		defaultValue: 0
			else if(myCapab == "Humidity") 				result = input myState, "number", 	title: "Humidity offset", 		range: "-100..100",	defaultValue: 0
			else if(myCapab == "Illuminance") 			result = input myState, "number", 	title: "Illuminance offset",	range: "*..*",		defaultValue: 0
			else if(myCapab == "Dimmer level")			result = input myState, "number", 	title: "Dimmer offset", 		range: "-100..100",	defaultValue: 0
			else if(myCapab == "Energy meter") 			result = input myState, "number", 	title: "Energy level offset",	range: "*..*",		defaultValue: 0
			else if(myCapab == "Power meter") 			result = input myState, "number", 	title: "Power level offset", 	range: "*..*",		defaultValue: 0
			else if(myCapab == "Battery") 				result = input myState, "number", 	title: "Battery level offset",	range: "-100..100",	defaultValue: 0
		}
		else if(myCapab == "Temperature") 			result = input myState, "decimal",	title: "Temperature becomes ", 	range: "*..*"
		else if(myCapab == "Humidity") 				result = input myState, "number", 	title: "Humidity becomes", 		range: "0..100"
		else if(myCapab == "Illuminance") 			result = input myState, "number", 	title: "Illuminance becomes",	range: "0..*"
		else if(myCapab == "Dimmer level")			result = input myState, "number", 	title: "Dimmer level", 			range: "0..100"
		else if(myCapab == "Energy meter") 			result = input myState, "number", 	title: "Energy level becomes",	range: "0..*"
		else if(myCapab == "Power meter") 			result = input myState, "number", 	title: "Power level becomes", 	range: "*..*"
		else if(myCapab == "Battery") 				result = input myState, "number", 	title: "Battery level becomes",	range: "0..100"
	} else if(myCapab == "Days of week") 			result = input "days",  "enum", 	title: "On certain days of the week", multiple: true, required: false, options: days
	else if(myCapab == "Mode") {
		def myModes = []
		location.modes.each {myModes << "$it"}
		def modeVar = (state.isRule || state.howMany > 1) ? "modes" : "modesX"
		result = input modeVar, "enum", title: "Select mode(s)", multiple: true, required: false, options: myModes.sort()
	} else if(myCapab == "Time of day") {
		def timeLabel = timeIntervalLabelX()
		href "certainTimeX", title: "During a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
	} else if(myCapab == "Certain Time") {
		def atTimeLabel = atTimeLabel()
		href "atCertainTime", title: "At a certain time", description: atTimeLabel ?: "Tap to set", state: atTimeLabel ? "complete" : null
	} else if(myCapab == "Routine") {
		def phrases = location.helloHome?.getPhrases()*.label
        result = input myState, "enum", title: "When this routine runs", multiple: false, required: false, options: phrases
	}
	def whatState = settings.find {it.key == myState}
}

def certainTimeX() {
	dynamicPage(name: "certainTimeX", title: "Only during a certain time", uninstall: false) {
		section() {
			input "startingXX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if(startingXX in [null, "A specific time"]) input "startingA", "time", title: "Start time", required: false
			else {
				if(startingXX == "Sunrise") input "startSunriseOffsetX", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(startingXX == "Sunset") input "startSunsetOffsetX", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
		section() {
			input "endingXX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if(endingXX in [null, "A specific time"]) input "endingA", "time", title: "End time", required: false
			else {
				if(endingXX == "Sunrise") input "endSunriseOffsetX", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(endingXX == "Sunset") input "endSunsetOffsetX", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
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

def triggerLabel() {
	def howMany = state.howManyT
	def result = ""
	if(howMany) {
		for (int i = 1; i < howMany; i++) {
        	def thisCapab = settings.find {it.key == "tCapab$i"}
			if(!thisCapab) return result
			result = result + (i > 1 ? "OR\n" : "") + conditionLabelN(i, true)
			if(i < howMany - 1) result = result + "\n"
		}
	}
	return result
}

def conditionLabel() {
	def howMany = state.howMany
	def result = ""
	if(howMany) {
		for (int i = 1; i < howMany; i++) {
			def thisCapab = settings.find {it.key == "rCapab$i"}
			if(!thisCapab) return result
			result = result + conditionLabelN(i, false) + ((state.isRule || state.isRule == null) ? (getOperand(i, true) ? " [TRUE]" : " [FALSE]") : "")
			if(i < howMany - 1) result = result + "\n"
		}
		if((state.isRule || state.isRule == null) && howMany == 2) {
			state.str = result[0..-8]
			state.eval = [1]
		}
	}
	return result
}

def conditionLabelN(i, isTrig) {
	def result = ""
	def SHMphrase = isTrig ? "becomes" : ((state.isRule || state.howMany > 1) ? "is" : "becomes")
	def phrase = isTrig ? "becomes" : ((state.isRule || state.howMany > 1) ? "of" : "becomes")
	def thisCapab = settings.find {it.key == (isTrig ? "tCapab$i" : "rCapab$i")}
	if(thisCapab.value == "Time of day") result = "Time between " + timeIntervalLabelX()
	else if(thisCapab.value == "Certain Time")  result = "When time is " + atTimeLabel()
	else if(thisCapab.value == "Smart Home Monitor") {
    		def thisState = (settings.find {it.key == (isTrig ? "tstate$i" : "state$i")}).value
    		result = "SHM state $SHMphrase " + (thisState in ["away", "stay"] ? "Arm ($thisState)" : "Disarm")
	} else if(thisCapab.value == "Days of week") result = "Day i" + (days.size() > 1 ? "n " + days : "s " + days[0])
	else if(thisCapab.value == "Mode") { 
        if((state.isTrig || isTrig) && modesX) result = "Mode becomes " + (modesX.size() > 1 ? modesX : modesX[0])
    	else if((state.isRule || state.howMany > 1) && modes) result = "Mode i" + (modes.size() > 1 ? "n " + modes : "s " + modes[0])
	} else if(thisCapab.value == "Routine") {
		result = "Routine "
		def thisState = settings.find {it.key == (isTrig ? "tstate$i" : "state$i")}
		result = result + "'" + thisState.value + "' runs"
	} else if(thisCapab.value == "Private Boolean") {
		def thisState = settings.find {it.key == (isTrig ? "tstate$i" : "state$i")}
		result = "Private Boolean $SHMphrase $thisState.value"
	} else {
		def thisDev = settings.find {it.key == (isTrig ? "tDev$i" : "rDev$i")}
		if(!thisDev) return result
		def thisAll = settings.find {it.key == (isTrig ? "AlltDev$i" : "AllrDev$i")}
		def myAny = thisAll && thisDev.value.size() > 1 ? "any " : ""
		def myButton = settings.find {it.key == (isTrig ? "ButtontDev$i" : "ButtonrDev$i")}
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
			def thisState = settings.find {it.key == (isTrig ? "tstate$i" : "state$i")}
			result = result + thisState.value
			return result
		}
		if(thisCapab.value == "Rule truth") result = result = result + (thisDev.value.size() > 1 ? ("$thisDev.value any ") : (thisDev.value[0] + " "))
		else result = result + (myAny ? thisDev.value : thisDev.value[0]) + " " + ((thisAll ? thisAll.value : false) ? "all " : myAny)
		def thisRel = settings.find {it.key == (isTrig ? "ReltDev$i" : "RelrDev$i")}
		if(thisCapab.value in ["Temperature", "Humidity", "Illuminance", "Dimmer level", "Energy meter", "Power meter", "Battery"]) result = result + " " + thisRel.value + " "
		if(thisCapab.value == "Physical Switch") result = result + "physical "
		def thisState = settings.find {it.key == (isTrig ? "tstate$i" : "state$i")}
		def thisRelDev = settings.find {it.key == (isTrig ? "reltDevice$i" : "relDevice$i")}
		if(thisRelDev) {
        	result = result + thisRelDev.value
        	if(thisState) result = result + (thisState.value > 0 ? " +" : " ") + (thisState.value != 0 ? thisState.value : "")
		}
		else result = result + thisState.value
        if(thisCapab.value == "Presence" && thisDev.value.size() > 1 && isTrig) result = result[0..-2] 
	}
	return result
}

// Rule definition code follows

def defineRule() {
	dynamicPage(name: "defineRule", title: "Define the Rule", uninstall: false) {
    	section() { 
        	paragraph "Turn on to enable parenthesized sub-rules"
        	input "advanced", "bool", title: "Complex Rule Input", required: false, submitOnChange: true 
        }
		state.n = 0
		state.str = ""
		state.eval = []
		section() {inputLeftAndRight(false)}
	}
}

def rulLabl() {
	def result = state.str
	if(state.eval && state.str) {
		state.token = 0
		def tru = eval()
		result = result + "\n[" + (tru ? "TRUE" : "FALSE") + "]"
	}
}

def inputLeft(sub) {
	def conds = []
	for (int i = 1; i < state.howMany; i++) conds << conditionLabelN(i, false)
	if(advanced) input "subCondL$state.n", "bool", title: "Enter subrule for left?", submitOnChange: true
	if(settings["subCondL$state.n"]) {
		state.str = state.str + "( "
		state.eval << "("
		paragraph(state.str)
		inputLeftAndRight(true)
	} else {
		input "condL$state.n", "enum", title: "Which condition?", options: conds, submitOnChange: true
		if(settings["condL$state.n"]) {
			state.str = state.str + settings["condL$state.n"]
			def myCond = 0
			for (int i = 1; i < state.howMany; i++) if(conditionLabelN(i, false) == settings["condL$state.n"]) myCond = i
			state.eval << myCond
			paragraph(state.str)
		}
	}
}

def inputRight(sub) {
	state.n = state.n + 1
	input "operator$state.n", "enum", title: "AND  or  OR", options: ["AND", "OR"], submitOnChange: true, required: false
	if(settings["operator$state.n"]) {
		state.str = state.str + "\n" + settings["operator$state.n"] + "\n"
		state.eval << settings["operator$state.n"]
		paragraph(state.str)
		def conds = []
		for (int i = 1; i < state.howMany; i++) conds << conditionLabelN(i, false)
		if(advanced) input "subCondR$state.n", "bool", title: "Enter subrule for right?", submitOnChange: true
		if(settings["subCondR$state.n"]) {
			state.str = state.str + "( "
			state.eval << "("
			paragraph(state.str)
			inputLeftAndRight(true)
			inputRight(sub)
			if(sub) {
				input "endOfSub$state.n", "bool", title: "End of sub-rule?", submitOnChange: true
				if(settings["endOfSub$state.n"]) {
					state.str = state.str + " )"
					state.eval << ")"
					paragraph(state.str)
					return
				}
			}
		} else {
			input "condR$state.n", "enum", title: "Which condition?", options: conds, submitOnChange: true
			if(settings["condR$state.n"]) {
				state.str = state.str + settings["condR$state.n"]
				def myCond = 0
				for (int i = 1; i < state.howMany; i++) if(conditionLabelN(i, false) == settings["condR$state.n"]) myCond = i
				state.eval << myCond
				paragraph(state.str)
			}
			if(sub) {
				input "endOfSub$state.n", "bool", title: "End of sub-rule?", submitOnChange: true
				if(settings["endOfSub$state.n"]) {
					state.str = state.str + " )"
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

// Action selection code follows

def stripBrackets(str) {
	def i = str.indexOf('[')
	def j = str.indexOf(']')
	def result = str.substring(0, i) + str.substring(i + 1, j) + str.substring(j + 1)
	return result
}

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

def delayTruePage() {
	dynamicPage(name: "delayTruePage", title: "Select Delay for Actions", uninstall: false) {
		section() {
			if(!delayMilTrue && !delaySecTrue) {
				input "delayMinTrue", "number", title: "Minutes of delay", required: false, range: "1..*", submitOnChange: true
				if(delayMinTrue > 0) {
					if(state.isRule || state.howMany > 1) input "cancelTrue", "bool", title: "Cancel on truth change?", required: false, submitOnChange: true
					paragraph "\n\n "
					input "randTrue", "bool", title: "Random delay?", required: false, submitOnChange: true
				}
			}
			if(!delayMinTrue && !delayMilTrue) {
				paragraph "\n"
				input "delaySecTrue", "number", title: "Seconds of delay", required: false, range: "1..*", submitOnChange: true
				if(delaySecTrue > 0 && (state.isRule || state.howMany > 1)) input "cancelTrue", "bool", title: "Cancel on truth change?", required: false, submitOnChange: true
			}
			if(!delayMinTrue && !delaySecTrue) {
				paragraph "\n\n Milliseconds delay works only for \n on/off/dim/toggle, open/close, lock/unlock"
				input "delayMilTrue", "number", title: "Milliseconds of delay", required: false, range: "1..*", submitOnChange: true
			}
			if(delayMinTrue > 0 || delayMilTrue > 0 || delaySecTrue > 0) {
				state.delayStrTrue = "Delay by " + (delayMilTrue ? "$delayMilTrue milliseconds" : (delaySecTrue ? "$delaySecTrue seconds" : "$delayMinTrue minute"))
				if(delayMinTrue > 1) state.delayStrTrue = state.delayStrTrue + "s"
				state.delayStrTrue = state.delayStrTrue + (cancelTrue ? " [Cancel]" : "") + (randTrue ? " [Random]" : "")
			} else state.delayStrTrue = ""
		}
	}
}

def delayFalsePage() {
	dynamicPage(name: "delayFalsePage", title: "Select Delay for Actions", uninstall: false) {
		section() {
			if(!delayMilFalse && !delaySecFalse) {
				input "delayMinFalse", "number", title: "Minutes of delay", required: false, range: "1..*", submitOnChange: true
				if(delayMinFalse > 0) {
					if(state.isRule || state.howMany > 1) input "cancelFalse", "bool", title: "Cancel on truth change?", required: false, submitOnChange: true
					paragraph "\n\n "
					input "randFalse", "bool", title: "Random delay?", required: false, submitOnChange: true
				}
			}                
			if(!delayMinFalse && !delayMilFalse) {
				paragraph "\n"
				input "delaySecFalse", "number", title: "Seconds of delay", required: false, range: "1..*", submitOnChange: true
				if(delaySecFalse > 0 && (state.isRule || state.howMany > 1)) input "cancelFalse", "bool", title: "Cancel on truth change?", required: false, submitOnChange: true
			}
			if(!delayMinFalse && !delaySecFalse) {
				paragraph "\n\n Milliseconds delay works only for \n on/off/dim/toggle, open/close, lock/unlock"
				input "delayMilFalse", "number", title: "Milliseconds of delay", required: false, range: "1..*", submitOnChange: true
			}
			if(delayMinFalse > 0 || delayMilFalse > 0 || delaySecFalse > 0) {
				state.delayStrFalse = "Delay by " + (delayMilFalse ? "$delayMilFalse milliseconds" : (delaySecFalse ? "$delaySecFalse seconds" : "$delayMinFalse minute"))
				if(delayMinFalse > 1) state.delayStrFalse = state.delayStrFalse + "s"
				state.delayStrFalse = state.delayStrFalse + (cancelFalse ? " [Cancel]" : "") + (randFalse ? " [Random]" : "")
			} else state.delayStrFalse = ""
		}
	}
}

def setModeLevel(thisMode, modeVar, trufal) {
	def result = input modeVar, "number", range: "0..100", title: "> Level for $thisMode", required: true, submitOnChange: true
	def str = settings.find{it.key == "$modeVar"}
	if(str) {
		if(trufal) addToActTrue("  $thisMode: $str.value")
		else addToActFalse("  $thisMode: $str.value")
	}
}

def selectActionsTrue() {
	def isRule = state.isRule || state.howMany > 1
	dynamicPage(name: "selectActionsTrue", title: "Select Actions" + (isRule ? " for True" : ""), uninstall: false) {
		def isTrig = state.isTrig || state.howManyT > 1
		state.actsTrue = ""
		section("") {
			href "delayTruePage", title: "Delay These Actions", description: state.delayStrTrue ? (state.delayStrTrue) : "Tap to set", state: state.delayStrTrue ? "complete" : null, submitOnChange: true
			if(state.delayStrTrue) addToActTrue(state.delayStrTrue)
			input "onSwitchTrue", "capability.switch", title: "Turn on these switches", multiple: true, required: false, submitOnChange: true
			checkActTrue(onSwitchTrue, "On: $onSwitchTrue")
			input "offSwitchTrue", "capability.switch", title: "Turn off these switches", multiple: true, required: false, submitOnChange: true
			checkActTrue(offSwitchTrue, "Off: $offSwitchTrue")
			input "toggleSwitchTrue", "capability.switch", title: "Toggle these switches", multiple: true, required: false, submitOnChange: true
			checkActTrue(toggleSwitchTrue, "Toggle: $toggleSwitchTrue")
			input "delayedOffTrue", "capability.switch", title: "Turn on or off these switches after a delay (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(delayedOffTrue) {
				input "delayOnOffTrue", "bool", title: "> Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
				if(!delayMillisTrue && !delaySecondsTrue) input "delayMinutesTrue", "number", title: "> Minutes of delay", required: false, range: "1..*", submitOnChange: true
				if(!delayMillisTrue && !delayMinutesTrue) input "delaySecondsTrue", "number", title: "> Seconds of delay", required: false, range: "1..*", submitOnChange: true
				if(!delayMinutesTrue && !delaySecondsTrue) input "delayMillisTrue", "number", title: "> Milliseconds of delay", required: false, range: "1..*", submitOnChange: true
				if(delayMinutesTrue || delaySecondsTrue || delayMillisTrue) {
					def delayStrTrue = "Delayed " + (delayOnOffTrue ? "On:" : "Off:") + " $delayedOffTrue: " 
                    if(delayMillisTrue) delayStrTrue = delayStrTrue + "$delayMillisTrue milliseconds"
                    if(delaySecondsTrue) delayStrTrue = delayStrTrue + "$delaySecondsTrue second"
                    if(delayMinutesTrue) delayStrTrue = delayStrTrue + "$delayMinutesTrue minute"
					if(delayMinutesTrue > 1 || delaySecondsTrue > 1) delayStrTrue = delayStrTrue + "s"
					setActTrue(delayStrTrue)
				}
			}
            if(state.isRule || state.howMany > 1) {
				input "pendedOffTrue", "capability.switch", title: "Turn on or off these switches after a delay, pending cancellation (default is OFF)", multiple: true, required: false, submitOnChange: true
				if(pendedOffTrue) {
					input "pendOnOffTrue", "bool", title: "> Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
					input "pendMinutesTrue", "number", title: "> Minutes of delay", required: true, range: "0..*", submitOnChange: true
					if(pendMinutesTrue != null) {
						def pendStrTrue = "Pending "+ (pendOnOffTrue ? "On:" : "Off:") + " $pendedOffTrue: $pendMinutesTrue minute"
						if(pendMinutesTrue > 1 || pendMinutesTrue == 0) pendStrTrue = pendStrTrue + "s"
						setActTrue(pendStrTrue)
					}
				}
            }
			input "dimATrue", "capability.switchLevel", title: "Set these dimmers", multiple: true, submitOnChange: true, required: false
			if(dimATrue) {
				input "dimTrackTrue", "bool", title: "> Track event dimmer?", required: false, submitOnChange: true
				if(dimTrackTrue) setActTrue("Track Dim: $dimATrue")
				else input "dimLATrue", "number", title: "> To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLATrue != null) setActTrue("Dim: $dimATrue: $dimLATrue")
			}
			input "dimBTrue", "capability.switchLevel", title: "Set these other dimmers", multiple: true, submitOnChange: true, required: false
			if(dimBTrue) {
				input "dimLBTrue", "number", title: "> To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLBTrue != null) setActTrue("Dim: $dimBTrue: $dimLBTrue")
			}
			input "toggleDimmerTrue", "capability.switchLevel", title: "Toggle these dimmers", multiple: true, required: false, submitOnChange: true
			if(toggleDimmerTrue) input "dimTogTrue", "number", title: "> To this level", range: "0..100", required: true, submitOnChange: true
			if(dimTogTrue != null) checkActTrue(toggleDimmerTrue, "Toggle Dim: $toggleDimmerTrue: $dimTogTrue")
			input "adjustDimmerTrue", "capability.switchLevel", title: "Adjust these dimmers", multiple: true, required: false, submitOnChange: true
			if(adjustDimmerTrue) input "dimAdjTrue", "number", title: "> By this amount", range: "-100..100", required: true, submitOnChange: true
			if(dimAdjTrue) checkActTrue(adjustDimmerTrue, "Adjust: $adjustDimmerTrue: $dimAdjTrue")
			def myModes = []
			location.modes.each {myModes << "$it"}
			input "dimMTrue", "capability.switchLevel", title: "Set these dimmers per mode", multiple: true, submitOnChange: true, required: false
			if(dimMTrue) {
				input "dimmerModesTrue", "enum", title: "> Select dimmer level by mode", required: true, options: myModes.sort(), multiple: true, submitOnChange: true
				if(dimmerModesTrue) {
					def sortModes = dimmerModesTrue.sort()
					checkActTrue(dimMTrue, "Dimmers per mode: $dimMTrue")
					sortModes.each {setModeLevel(it, "levelTrue$it", true)}
				}
			}
			if(captureFalse == null) {
				input "captureTrue", "capability.switch", title: "Capture the state of these switches", multiple: true, required: false, submitOnChange: true
				checkActTrue(captureTrue, "Capture: $captureTrue")
			}
			if(captureTrue || captureFalse) input "restoreTrue", "bool", title: "Restore the state of captured switches", required: false, submitOnChange: true
			if(restoreTrue && captureTrue) setActTrue("Restore: $captureTrue")
			else if(restoreTrue && captureFalse) setActTrue("Restore: $captureFalse")
			input "ctTrue", "capability.colorTemperature", title: "Set color temperature for these bulbs", multiple: true, submitOnChange: true, required: false
			if(ctTrue) input "ctLTrue", "number", title: "> To this color temperature", range: "2000..6500", required: true, submitOnChange: true
			if(ctLTrue) checkActTrue(ctTrue, "Color Temperature: $ctTrue: $ctLTrue")
			input "bulbsTrue", "capability.colorControl", title: "Set color for these bulbs", multiple: true, required: false, submitOnChange: true
			if(bulbsTrue) {
				input "colorTrue", "enum", title: "> Bulb color?", required: true, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink", "Custom color"]
				input "colorLevelTrue", "number", title: "> Bulb level?", required: false, submitOnChange: true, range: "0..100"
				buildActTrue("Color: $bulbsTrue ", true)
				if(colorTrue) {
					if(colorTrue == "Custom color") {
						input "colorHexTrue", "number", title: "> Input color value", required: true, submitOnChange: true, range: "0..100"
						input "colorSatTrue", "number", title: "> Input saturation value", required: true, submitOnChange: true, range: "0..100"
					}
					buildActTrue("$colorTrue ", false)
					if(colorHexTrue) buildActTrue("$colorHexTrue:$colorSatTrue ", false)
                }
				if(colorLevelTrue) addToActTrue("Level: $colorLevelTrue")
			}            
			input "garageOpenTrue", "capability.garageDoorControl", title: "Open these garage doors", multiple: true, required: false, submitOnChange: true
			checkActTrue(garageOpenTrue, "Garage open: $garageOpenTrue")
			input "garageCloseTrue", "capability.garageDoorControl", title: "Close these garage doors", multiple: true, required: false, submitOnChange: true
			checkActTrue(garageCloseTrue, "Garage close: $garageCloseTrue")
			input "lockTrue", "capability.lock", title: "Lock these locks", multiple: true, required: false, submitOnChange: true
			checkActTrue(lockTrue, "Lock: $lockTrue")
			input "unlockTrue", "capability.lock", title: "Unlock these locks", multiple: true, required: false, submitOnChange: true
			checkActTrue(unlockTrue, "Unlock: $unlockTrue")
			input "fanAdjustTrue", "capability.switchLevel", title: "Adjust this fan - Low, Medium, High, Off", multiple: false, required: false, submitOnChange: true
			if(fanAdjustTrue) addToActTrue("Adjust Fan: $fanAdjustTrue")
			input "openValveTrue", "capability.valve", title: "Open these valves", multiple: true, required: false, submitOnChange: true
			checkActTrue(openValveTrue, "Open: $openValveTrue")
			input "closeValveTrue", "capability.valve", title: "Close these valves", multiple: true, required: false, submitOnChange: true
			checkActTrue(closeValveTrue, "Close: $closeValveTrue")
			input "thermoTrue", "capability.thermostat", title: "Set these thermostats", multiple: true, required: false, submitOnChange: true
			if(thermoTrue) {
				input "thermoModeTrue", "enum", title: "> Select thermostat mode", multiple: false, required: false, options: ["auto", "heat", "cool", "off"], submitOnChange: true
				input "thermoSetHeatTrue", "decimal", title: "> Set heating point", multiple: false, required: false, submitOnChange: true
				input "thermoSetCoolTrue", "decimal", title: "> Set cooling point", multiple: false, required: false, submitOnChange: true 
				input "thermoFanTrue", "enum", title: "> Fan setting", multiple: false, required: false, submitOnChange: true, options: ["on", "auto"]
				buildActTrue("$thermoTrue: ", true)
				if(thermoModeTrue) buildActTrue("Mode: " + thermoModeTrue + " ", false)
				if(thermoSetHeatTrue) buildActTrue("Heat to $thermoSetHeatTrue ", false)
				if(thermoSetCoolTrue) buildActTrue("Cool to $thermoSetCoolTrue ", false)
				if(thermoFanTrue) buildActTrue("Fan setting $thermoFanTrue", false)
				addToActTrue("")
			}
			input "alarmTrue", "enum", title: "Set the alarm state", multiple: false, required: false, options: ["away" : "Arm (away)", "stay" : "Arm (stay)", "off" : "Disarm"], submitOnChange: true
			if(alarmTrue) addToActTrue("Alarm: " + (alarmTrue in ["away", "stay"] ? "Arm ($alarmTrue)" : "Disarm"))
			input "modeTrue", "enum", title: "Set the mode", multiple: false, required: false, options: myModes.sort(), submitOnChange: true
			if(modeTrue) addToActTrue("Mode: $modeTrue")
			def phrases = location.helloHome?.getPhrases()*.label
			input "myPhraseTrue", "enum", title: "Run a Routine", required: false, options: phrases.sort(), submitOnChange: true
			if(myPhraseTrue) addToActTrue("Routine: $myPhraseTrue")
			def theseRules = parent.ruleList(app.label)
			if(theseRules != null) input "ruleTrue", "enum", title: "Evaluate Rules", required: false, multiple: true, options: theseRules.sort(), submitOnChange: true
			if(ruleTrue) setActTrue("Rules: $ruleTrue")
			if(theseRules != null) input "ruleActTrue", "enum", title: "Run Rule Actions", required: false, multiple: true, options: theseRules.sort(), submitOnChange: true
			if(ruleActTrue) setActTrue("Rule Actions: $ruleActTrue")
			input "updateTrue", "enum", title: "Update Rules", required: false, multiple: true, options: theseRules.sort(), submitOnChange: true
			if(updateTrue) setActTrue("Update Rules: $updateTrue")
			def theseRules2 = parent.ruleList(app.label)
			theseRules2 << app.label
			input "ruleEvalDelayTrue", "enum", title: "Evaluate Rules after delay", required: false, multiple: true, options: theseRules2.sort(), submitOnChange: true
			if(ruleEvalDelayTrue) {
				input "delayEvalMinutesTrue", "number", title: "> Minutes of delay", required: false, range: "1..*", submitOnChange: true
				if(delayEvalMinutesTrue != null) {
					def delayStrTrue = "Delay Rule Evaluations: $ruleEvalDelayTrue: $delayEvalMinutesTrue minute"
					if(delayEvalMinutesTrue > 1) delayStrTrue = delayStrTrue + "s"
					setActTrue(delayStrTrue)
				}
			}
			href "selectMsgTrue", title: "Send or speak a message", description: state.msgTrue ? state.msgTrue : "Tap to set", state: state.msgTrue ? "complete" : null
			if(state.msgTrue) addToActTrue(state.msgTrue)
			input "cameraTrue", "capability.imageCapture", title: "Take photos", required: false, multiple: false, submitOnChange: true
			if(cameraTrue) {
				input "burstCountTrue", "number", title: "> How many? (default 5)", defaultValue:5
				addToActTrue("Photo: $cameraTrue " + (burstCountTrue ?: ""))
			}
// code below is vestigal, supports prior version of delayTrue and randomTrue            
//            if(!randomTrue) {
			if(delayTrue) {
				input "delayTrue", "number", title: "Delay " + ((state.isRule || state.howMany > 1) ? "the effect of this rule" : "this action") + " by this many minutes", required: false, submitOnChange: true
//				if(delayTrue) {
					def delayStr = "Delay Rule: $delayTrue minute"
					if(delayTrue > 1) delayStr = delayStr + "s"
					addToActTrue(delayStr)
//				}
            }
//            if(!delayTrue) {
            if(randomTrue) {
				input "randomTrue", "number", title: "Delay " + ((state.isRule || state.howMany > 1) ? "the effect of this rule" : "this action") + " by random minutes up to", required: false, submitOnChange: true
//				if(randomTrue) {
					def randomStr = "Random Delay: $randomTrue minutes"
					addToActTrue(randomStr)
//				}
            }
			input "privateTrue", "enum", title: "Set Private Boolean", required: false, submitOnChange: true, options: ["true", "false"]
			if(privateTrue) {
				input "otherTrue", "bool", title: "> For this Rule (default) or others?", required: false, submitOnChange: true
				if(otherTrue) {
					input "otherPrivateTrue", "enum", title: "> Select Rules to set Boolean", required: true, multiple: true, options: theseRules.sort(), submitOnChange: true
					if(otherPrivateTrue) setActTrue("Rule Boolean: $otherPrivateTrue: $privateTrue")
				} else addToActTrue("Private Boolean: $privateTrue")
			}
			if (state.isExpert){
				if (state.cstCmds){
					state.ccTruth = true
					def desc = state.cmdActTrue
					href( "selectCustomActions"
						,title		: "Run custom commands"
						,description: desc ?: "Tap to set"
						,state		: desc ? "complete" : null
					)
					checkActTrue(desc,"[${desc}]")
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
			href "delayFalsePage", title: "Delay These Actions", description: state.delayStrFalse ? (state.delayStrFalse) : "Tap to set", state: state.delayStrFalse ? "complete" : null, submitOnChange: true
			if(state.delayStrFalse) addToActFalse(state.delayStrFalse)
			input "onSwitchFalse", "capability.switch", title: "Turn on these switches", multiple: true, required: false, submitOnChange: true
			checkActFalse(onSwitchFalse, "On: $onSwitchFalse")
			input "offSwitchFalse", "capability.switch", title: "Turn off these switches", multiple: true, required: false, submitOnChange: true
			checkActFalse(offSwitchFalse, "Off: $offSwitchFalse")
			input "toggleSwitchFalse", "capability.switch", title: "Toggle these switches", multiple: true, required: false, submitOnChange: true
			checkActFalse(toggleSwitchFalse, "Toggle: $toggleSwitchFalse")
			input "delayedOffFalse", "capability.switch", title: "Turn on or off these switches after a delay (default is OFF)", multiple: true, required: false, submitOnChange: true
			if(delayedOffFalse) {
				input "delayOnOffFalse", "bool", title: "> Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
				if(!delayMillisFalse && !delaySecondsFalse) input "delayMinutesFalse", "number", title: "> Minutes of delay", required: false, range: "1..*", submitOnChange: true
				if(!delayMillisFalse && !delayMinutesFalse) input "delaySecondsFalse", "number", title: "> Seconds of delay", required: false, range: "1..*", submitOnChange: true
				if(!delayMinutesFalse && !delaySecondsFalse) input "delayMillisFalse", "number", title: "> Milliseconds of delay", required: false, range: "1..*", submitOnChange: true
				if(delayMinutesFalse || delaySecondsFalse || delayMillisFalse) {
					def delayStrFalse = "Delayed " + (delayOnOffFalse ? "On:" : "Off:") + " $delayedOffFalse: " 
                    if(delayMillisFalse) delayStrFalse = delayStrFalse + "$delayMillisFalse milliseconds"
                    if(delaySecondsFalse) delayStrFalse = delayStrFalse + "$delaySecondsFalse second"
                    if(delayMinutesFalse) delayStrFalse = delayStrFalse + "$delayMinutesFalse minute"
					if(delayMinutesFalse > 1 || delaySecondsFalse > 1) delayStrFalse = delayStrFalse + "s"
					setActFalse(delayStrFalse)
				}
			}
			if(state.isRule || state.howMany > 1) {
				input "pendedOffFalse", "capability.switch", title: "Turn on or off these switches after a delay, pending cancellation (default is OFF)", multiple: true, required: false, submitOnChange: true
				if(pendedOffFalse) {
					input "pendOnOffFalse", "bool", title: "> Turn ON after the delay?", multiple: false, required: false, defaultValue: false, submitOnChange: true
					input "pendMinutesFalse", "number", title: "> Minutes of delay", required: true, range: "0..*", submitOnChange: true
					if(pendMinutesFalse != null) {
						def pendStrFalse = "Pending "+ (pendOnOffFalse ? "On:" : "Off:") + " $pendedOffFalse: $pendMinutesFalse minute"
						if(pendMinutesFalse > 1 || pendMinutesFalse == 0) pendStrFalse = pendStrFalse + "s"
						setActFalse(pendStrFalse)
					}
				}
			}
			input "dimAFalse", "capability.switchLevel", title: "Set these dimmers", multiple: true, submitOnChange: true, required: false
			if(dimAFalse) {
				input "dimTrackFalse", "bool", title: "> Track event dimmer?", required: false, submitOnChange: true
				if(dimTrackFalse) setActFalse("Track Dim: $dimAFalse")
				else input "dimLAFalse", "number", title: "> To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLAFalse != null) setActFalse("Dim: $dimAFalse: $dimLAFalse")
			}
			input "dimBFalse", "capability.switchLevel", title: "Set these other dimmers", multiple: true, submitOnChange: true, required: false
			if(dimBFalse) {
				input "dimLBFalse", "number", title: "> To this level", range: "0..100", required: true, submitOnChange: true
				if(dimLBFalse != null) setActFalse("Dim: $dimBFalse: $dimLBFalse")
			}
			input "toggleDimmerFalse", "capability.switchLevel", title: "Toggle these dimmers", multiple: true, required: false, submitOnChange: true
			if(toggleDimmerFalse) input "dimTogFalse", "number", title: "> To this level", range: "0..100", required: true, submitOnChange: true
			if(dimTogFalse != null) checkActFalse(toggleDimmerFalse, "Toggle Dim: $toggleDimmerFalse: $dimTogFalse")
			input "adjustDimmerFalse", "capability.switchLevel", title: "Adjust these dimmers", multiple: true, required: false, submitOnChange: true
			if(adjustDimmerFalse) input "dimAdjFalse", "number", title: "> By this amount", range: "-100..100", required: true, submitOnChange: true
			if(dimAdjFalse) checkActFalse(adjustDimmerFalse, "Adjust: $adjustDimmerFalse: $dimAdjFalse")
			def myModes = []
			location.modes.each {myModes << "$it"}
			input "dimMFalse", "capability.switchLevel", title: "Set these dimmers per mode", multiple: true, submitOnChange: true, required: false
			if(dimMFalse) {
				input "dimmerModesFalse", "enum", title: "> Select dimmer level by mode", required: true, options: myModes.sort(), multiple: true, submitOnChange: true
				if(dimmerModesFalse) {
					def sortModes = dimmerModesFalse.sort()
					checkActFalse(dimMFalse, "Dimmers per mode: $dimMFalse")
					sortModes.each {setModeLevel(it, "levelFalse$it", false)}
				}
			}
			if(captureTrue == null) {
				input "captureFalse", "capability.switch", title: "Capture the state of these switches", multiple: true, required: false, submitOnChange: true
				checkActFalse(captureFalse, "Capture: $captureFalse")
			}
			if(captureTrue || captureFalse) input "restoreFalse", "bool", title: "Restore the state of captured switches?", required: false, submitOnChange: true
			if(restoreFalse && captureFalse) setActFalse("Restore: $captureFalse")
			else if(restoreFalse && captureTrue) setActFalse("Restore: $captureTrue")
			input "ctFalse", "capability.colorTemperature", title: "Set color temperature for these bulbs", multiple: true, submitOnChange: true, required: false
			if(ctFalse) input "ctLFalse", "number", title: "> To this color temperature", range: "2000..6500", required: true, submitOnChange: true
			if(ctLFalse) checkActFalse(ctFalse, "Color Temperature: $ctFalse: $ctLFalse")			
			input "bulbsFalse", "capability.colorControl", title: "Set color for these bulbs", multiple: true, required: false, submitOnChange: true
			if(bulbsFalse) {
				input "colorFalse", "enum", title: "> Bulb color?", required: true, multiple: false, submitOnChange: true,
					options: ["Soft White", "White", "Daylight", "Warm White", "Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink", "Custom color"]
				input "colorLevelFalse", "number", title: "> Bulb level?", required: false, submitOnChange: true, range: "0..100"
				buildActFalse("Color: $bulbsFalse ", true)
				if(colorFalse) {
					if(colorFalse == "Custom color") {
						input "colorHexFalse", "number", title: "> Input color value", required: true, submitOnChange: true, range: "0..100"
						input "colorSatFalse", "number", title: "> Input saturation value", required: true, submitOnChange: true, range: "0..100"
					}
					buildActFalse("$colorFalse ", false)
					if(colorHexFalse) buildActFalse("$colorHexFalse:$colorSatFalse ", false)
				}
				if(colorLevelFalse) addToActFalse("Level: $colorLevelFalse")
			}            
			input "garageOpenFalse", "capability.garageDoorControl", title: "Open these garage doors", multiple: true, required: false, submitOnChange: true
			checkActFalse(garageOpenFalse, "Garage open: $garageOpenFalse")
			input "garageCloseFalse", "capability.garageDoorControl", title: "Close these garage doors", multiple: true, required: false, submitOnChange: true
			checkActFalse(garageCloseFalse, "Garage close: $garageCloseFalse")
			input "lockFalse", "capability.lock", title: "Lock these locks", multiple: true, required: false, submitOnChange: true
			checkActFalse(lockFalse, "Lock: $lockFalse")
			input "unlockFalse", "capability.lock", title: "Unlock these locks", multiple: true, required: false, submitOnChange: true
			checkActFalse(unlockFalse, "Unlock: $unlockFalse")
			input "fanAdjustFalse", "capability.switchLevel", title: "Adjust this fan - Low, Medium, High, Off", multiple: false, required: false, submitOnChange: true
			if(fanAdjustFalse) addToActFalse("Adjust Fan: $fanAdjustFalse")
			input "openValveFalse", "capability.valve", title: "Open these valves", multiple: true, required: false, submitOnChange: true
			checkActFalse(openValveFalse, "Open: $openValveFalse")
			input "closeValveFalse", "capability.valve", title: "Close these valves", multiple: true, required: false, submitOnChange: true
			checkActFalse(closeValveFalse, "Close: $closeValveFalse")
			input "thermoFalse", "capability.thermostat", title: "Set these thermostats", multiple: true, required: false, submitOnChange: true
			if(thermoFalse) {
				input "thermoModeFalse", "enum", title: "> Select thermostat mode", multiple: false, required: false, options: ["auto", "heat", "cool", "off"], submitOnChange: true
				input "thermoSetHeatFalse", "decimal", title: "> Set heating point", multiple: false, required: false, submitOnChange: true
				input "thermoSetCoolFalse", "decimal", title: "> Set cooling point", multiple: false, required: false, submitOnChange: true 
				input "thermoFanFalse", "enum", title: "> Fan setting", multiple: false, required: false, submitOnChange: true, options: ["on", "auto"]
				buildActFalse("$thermoFalse: ", true)
				if(thermoModeFalse) buildActFalse("Mode: " + thermoModeFalse + " ", false)
				if(thermoSetHeatFalse) buildActFalse("Heat to $thermoSetHeatFalse ", false)
				if(thermoSetCoolFalse) buildActFalse("Cool to $thermoSetCoolFalse ", false)
				if(thermoFanFalse) buildActFalse("Fan setting $thermoFanFalse", false)
				addToActFalse("")
			}
			input "alarmFalse", "enum", title: "Set the alarm state", multiple: false, required: false, options: ["away" : "Arm (away)", "stay" : "Arm (stay)", "off" : "Disarm"], submitOnChange: true
			if(alarmFalse) addToActFalse("Alarm: " + (alarmFalse in ["away", "stay"] ? "Arm ($alarmFalse)" : "Disarm"))
			input "modeFalse", "enum", title: "Set the mode", multiple: false, required: false, options: myModes.sort(), submitOnChange: true
			if(modeFalse) addToActFalse("Mode: $modeFalse")
			def phrases = location.helloHome?.getPhrases()*.label
			input "myPhraseFalse", "enum", title: "Run a Routine", required: false, options: phrases.sort(), submitOnChange: true
			if(myPhraseFalse) addToActFalse("Routine: $myPhraseFalse")
			def theseRules = parent.ruleList(app.label)
			if(theseRules != null) input "ruleFalse", "enum", title: "Evaluate Rules", required: false, multiple: true, options: theseRules.sort(), submitOnChange: true
			if(ruleFalse) setActFalse("Rules: $ruleFalse")
			if(theseRules != null) input "ruleActFalse", "enum", title: "Run Rule Actions", required: false, multiple: true, options: theseRules.sort(), submitOnChange: true
			if(ruleActFalse) setActFalse("Rule Actions: $ruleActFalse")
			input "updateFalse", "enum", title: "Update Rules", required: false, multiple: true,options: theseRules.sort(), submitOnChange: true
			if(updateFalse) setActFalse("Update Rules: $updateFalse")
			def theseRules2 = parent.ruleList(app.label)
			theseRules2 << app.label
			input "ruleEvalDelayFalse", "enum", title: "Evaluate Rules after delay", required: false, multiple: true, options: theseRules2.sort(), submitOnChange: true
			if(ruleEvalDelayFalse) {
				input "delayEvalMinutesFalse", "number", title: "> Minutes of delay", required: false, range: "1..*", submitOnChange: true
				if(delayEvalMinutesFalse != null) {
					def delayStrFalse = "Delay Rule Evaluations: $ruleEvalDelayFalse: $delayEvalMinutesFalse minute"
					if(delayEvalMinutesFalse > 1) delayStrFalse = delayStrFalse + "s"
					setActFalse(delayStrFalse)
				}
			}
			href "selectMsgFalse", title: "Send or speak a message", description: state.msgFalse ? state.msgFalse : "Tap to set", state: state.msgFalse ? "complete" : null
			if(state.msgFalse) addToActFalse(state.msgFalse)
			input "cameraFalse", "capability.imageCapture", title: "Take photos", required: false, multiple: false, submitOnChange: true
			if(cameraFalse) {
				input "burstCountFalse", "number", title: "> How many? (default 5)", defaultValue:5
				addToActFalse("Photo: $cameraFalse " + (burstCountFalse ?: ""))
			}
// code below is vestigal, supports prior version of delayFalse and randomFalse            
			if(delayFalse) {
				input "delayFalse", "number", title: "Delay " + ((state.isRule || state.howMany > 1) ? "the effect of this rule" : "this action") + " by this many minutes", required: false, submitOnChange: true
//				if(delayFalse) {
					def delayStr = "Delay Rule: $delayFalse minute"
					if(delayFalse > 1) delayStr = delayStr + "s"
					addToActFalse(delayStr)
//				}
			}
			if(randomFalse) {
				input "randomFalse", "number", title: "Delay " + ((state.isRule || state.howMany > 1) ? "the effect of this rule" : "this action") + " by random minutes up to", required: false, submitOnChange: true
//				if(randomFalse) {
					def randomStr = "Random Delay: $randomFalse minutes"
					addToActFalse(randomStr)
//				}
			}
			input "privateFalse", "enum", title: "Set Private Boolean", required: false, submitOnChange: true, options: ["true", "false"]
			if(privateFalse) {
				input "otherFalse", "bool", title: "> For this Rule (default) or others?", required: false, submitOnChange: true
				if(otherFalse) {
					input "otherPrivateFalse", "enum", title: "> Select Rules to set Boolean", required: true, multiple: true, options: theseRules.sort(), submitOnChange: true
					if(otherPrivateFalse) setActFalse("Rule Boolean: $otherPrivateFalse: $privateFalse")
				} else addToActFalse("Private Boolean: $privateFalse")
			}
			if (state.isExpert){
				if (state.cstCmds){
					state.ccTruth = false
					def desc = state.cmdActFalse
					href( "selectCustomActions"
						,title		: "Run custom commands"
						,description: desc ?: "Tap to set"
						,state		: desc ? "complete" : null
					)
					checkActFalse(desc,"[${desc}]")
				}
			}
        }
        if(state.actsFalse) state.actsFalse = state.actsFalse[0..-2]
	}
}

def selectMsgTrue() {
	dynamicPage(name: "selectMsgTrue", title: "Select Message and Destination", uninstall: false) {
		section("") {
			if(noticeTrue in [null, false]) input "pushTrue", "bool", title: "Send Push?", required: false, submitOnChange: true
			if(pushTrue in [null, false]) input "noticeTrue", "bool", title: "Send Notification?", required: false, submitOnChange: true
			input "msgTrue", "text", title: "Custom message to send", required: false, submitOnChange: true
			input "refDevTrue", "bool", title: "Include device name?", required: false, submitOnChange: true
			input "phoneTrue", "phone", title: "Phone number for SMS", required: false, submitOnChange: true
			input "speakTrue", "bool", title: "Speak this message?", required: false, submitOnChange: true
			if(speakTrue){
				input "speakTrueDevice", title: "On this speech device", "capability.speechSynthesis", required: false, multiple: true, submitOnChange: true
				input "mediaTrueDevice", title: "On this music device", "capability.musicPlayer", required: false, multiple: true, submitOnChange: true
				if (mediaTrueDevice) input "mediaTrueVolume", title: "At this volume", "number", required: false, multiple: false, defaultValue: "50", submitOnChange: true
			}
		}
        state.msgTrue = (pushTrue ? "Push" : "") + (noticeTrue ? "Notify" : "") + (msgTrue ? " '$msgTrue'" : "") + (refDevTrue ? " [device]" : "") + (phoneTrue ? " to $phoneTrue" : "") + (speakTrue ? " [speak]" : "") + (mediaTrueDevice ? " ${mediaTrueDevice}" : "")
	}
}

def selectMsgFalse() {
	dynamicPage(name: "selectMsgFalse", title: "Select Message and Destination", uninstall: false) {
		section("") {
			if(noticeFalse in [null, false]) input "pushFalse", "bool", title: "Send Push?", required: false, submitOnChange: true
			if(pushFalse in [null, false]) input "noticeFalse", "bool", title: "Send Notification?", required: false, submitOnChange: true
			input "msgFalse", "text", title: "Custom message to send", required: false, submitOnChange: true
			input "refDevFalse", "bool", title: "Include device name?", required: false, submitOnChange: true
			input "phoneFalse", "phone", title: "Phone number for SMS", required: false, submitOnChange: true
			input "speakFalse", "bool", title: "Speak this message?", required: false, submitOnChange: true
			if(speakFalse){
				input "speakFalseDevice", title: "On this speech device", "capability.speechSynthesis", required: false, multiple: true, submitOnChange: true
				input "mediaFalseDevice", title: "On this music device", "capability.musicPlayer", required: false, multiple: true, submitOnChange: true
				if (mediaFalseDevice) input "mediaFalseVolume", title: "At this volume", "number", required: false, multiple: false, defaultValue: "50", submitOnChange: true
			}		
		}
        state.msgFalse = (pushFalse ? "Push" : "") + (noticeFalse ? "Notify" : "") + (msgFalse ? " '$msgFalse'" : "") + (refDevFalse ? " [device]" : "") + (phoneFalse ? " to $phoneFalse" : "") + (speakFalse ? " [speak]" : "") + (mediaFalseDevice ? " ${mediaFalseDevice}" : "")
	}
}
// initialization code follows

def scheduleTimeOfDay() {
	def start = null
	def stop = null
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffsetX, sunsetOffset: startSunsetOffsetX)
	if(startingXX == "Sunrise") start = s.sunrise.time
	else if(startingXX == "Sunset") start = s.sunset.time
	else if(startingA) start = timeToday(startingA,location.timeZone).time
	s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffsetX, sunsetOffset: endSunsetOffsetX)
	if(endingXX == "Sunrise") stop = s.sunrise.time
	else if(endingXX == "Sunset") stop = s.sunset.time
	else if(endingA) stop = timeToday(endingA,location.timeZone).time
	schedule(start, "startHandler")
	schedule(stop, "stopHandler")
	if(startingXX in ["Sunrise", "Sunset"] || endingXX in ["Sunrise", "Sunset"])
		schedule("2015-01-09T00:15:29.000" + gmtOffset(), "scheduleTimeOfDay") // in case sunset/sunrise; change daily
}

def scheduleAtTime() {
	def myTime = null
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: atSunriseOffset, sunsetOffset: atSunsetOffset)
	if(timeX == "Sunrise") myTime = s.sunrise.time
	else if(timeX == "Sunset") myTime = s.sunset.time
	else myTime = timeToday(atTime, location.timeZone).time
	schedule(myTime, "timeHandler")
	if(timeX in ["Sunrise", "Sunset"]) schedule("2015-01-09T00:15:29.000" + gmtOffset(), "scheduleAtTime") // in case sunset/sunrise; change daily
}

def installed() {
	initialize()
}

def updated() {
	//unschedule()
	unsubscribe()
	parent.unSubscribeRule(app.label)
	initialize()
}

def uninstalled() {
//	log.debug "uninstalled called"
	try { parent.removeChild(app.label) }
	catch (e) { log.error "No child app found" }
}

def gmtOffset() {
	def offset = location.timeZone.rawOffset
	def offsetAbs = offset < 0 ? -offset : offset
	def offsetSign = offset < 0 ? "-" : "+"
	int offsetHour = offsetAbs / 3600000
	int offsetMin = offsetAbs / 60000
//	int offsetM6 = offsetMin / 60
//	int offMin = offsetMin - (offsetM6.toInteger() * 60)
	int offMin = offsetMin % 60
	def result = String.format("%s%02d%02d", offsetSign, offsetHour, offMin);    
}

def initialize() {
	def hasTrig = state.howManyT > 1
	def howMany = hasTrig ? state.howManyT : state.howMany
	for (int i = 1; i < howMany; i++) {
		def capab = (settings.find {it.key == (hasTrig ? "tCapab$i" : "rCapab$i")}).value
		def myState = settings.find {it.key == (hasTrig ? "tstate$i" : "state$i")}
		def myRelDev = settings.find {it.key == (hasTrig ? "reltDevice$i" : "relDevice$i")}
		def myDev = settings.find {it.key == (hasTrig ? "tDev$i" : "rDev$i")}
		if(myState) myState = myState.value
		switch(capab) {
			case "Mode": 
				subscribe(location, "mode", allHandler)
				break
			case "Smart Home Monitor": 
				subscribe(location, "alarmSystemStatus" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case "Time of day":
				scheduleTimeOfDay()
				break
			case "Days of week":
				schedule("2015-01-09T00:01:00.000" + gmtOffset(), "dayHandler")
				break
			case "Certain Time":
				scheduleAtTime()
				break
			case "Dimmer level":
				subscribe(myDev.value, "level", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "level", allHandler)
				break
			case "Energy meter":
				subscribe(myDev.value, "energy", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "energy", allHandler)
				break
			case "Power meter":
				subscribe(myDev.value, "power", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "power", allHandler)
				break
			case "Temperature":
				subscribe(myDev.value, "temperature", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "temperature", allHandler)
				break
			case "Humidity":
				subscribe(myDev.value, "humidity", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "humidity", allHandler)
				break
			case "Battery":
				subscribe(myDev.value, "battery", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "battery", allHandler)
				break
			case "Illuminance":
				subscribe(myDev.value, "illuminance", allHandler)
				if(myRelDev) subscribe(myRelDev.value, "illuminance", allHandler)
				break
			case "Carbon monoxide detector":
				subscribe(myDev.value, "carbonMonoxide" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case "Smoke detector":
				subscribe(myDev.value, "smoke" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case ["Presence"]:
				subscribe(myDev.value, "presence" + ((state.isTrig || hasTrig) ? (myState == "arrives" ? ".present" : ".not present") : ""), allHandler)
				break
			case "Button":
				subscribe(myDev.value, "button", allHandler)
				break
			case "Rule truth":
				parent.subscribeRule(app.label, myDev.value, (state.isTrig || hasTrig) ? myState : null, allHandler)
				break
			case "Water sensor":
				subscribe(myDev.value, "water" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case "Garage door":
				subscribe(myDev.value, "door" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case "Thermostat Mode":
				subscribe(myDev.value, "thermostatMode" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case "Thermostat State":
				subscribe(myDev.value, "thermostatOperatingState" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case "Physical Switch":
				subscribe(myDev.value, "switch.$myState", physicalHandler)
				break
			case "Routine":
				subscribe(location, "routineExecuted", allHandler)
				break
			case "Music player":
				subscribe(myDev.value, "status" + ((state.isTrig || hasTrig) ? ".$myState" : ""), allHandler)
				break
			case "Private Boolean":
				break
			default:
				subscribe(myDev.value, (capab.toLowerCase() + ((state.isTrig || hasTrig) ? ".$myState" : "")), allHandler)
		}
	}
	state.success = null
	subscribe(disabled, "switch", disabledHandler)
	def disOnOff = disabledOff ? "off" : "on"
	if(disabled) state.disabled = disabled.currentSwitch == disOnOff
	else state.disabled = false
	parent.setRuleTruth(app.label, true)
	if(state.isTrig || hasTrig) return
	if(state.isRule || state.howMany > 1) runRule(true)
}

// Main rule evaluation code follows

def compare(a, rel, b, relDev) {
	def result = true
	if     (rel == "=") 	result = a == (relDev ? relDev + b : b)
	else if(rel == "!=") 	result = a != (relDev ? relDev + b : b)
	else if(rel == ">") 	result = a >  (relDev ? relDev + b : b)
	else if(rel == "<") 	result = a <  (relDev ? relDev + b : b)
	else if(rel == ">=") 	result = a >= (relDev ? relDev + b : b)
	else if(rel == "<=") 	result = a <= (relDev ? relDev + b : b)
	return result
}

def checkCondAny(dev, stateX, cap, rel, relDev) {
    if(stateX == "leaves") stateX = "not present"
    else if(stateX == "arrives") stateX = "present"
	def result = false
	if     (cap == "Temperature") 	dev.currentTemperature.each 	{result = result || compare(it, rel, stateX, relDev ? relDev.currentTemperature : null)}
	else if(cap == "Humidity")		dev.currentHumidity.each    	{result = result || compare(it, rel, stateX, relDev ? relDev.currentHumidity : null)}
	else if(cap == "Illuminance") 	dev.currentIlluminance.each 	{result = result || compare(it, rel, stateX, relDev ? relDev.currentIlluminance : null)}
	else if(cap == "Dimmer level")	dev.currentLevel.each			{result = result || compare(it, rel, stateX, relDev ? relDev.currentLevel : null)}
	else if(cap == "Energy meter")	dev.currentEnergy.each			{result = result || compare(it, rel, stateX, relDev ? relDev.currentEnergy : null)}
	else if(cap == "Power meter")	dev.currentPower.each			{result = result || compare(it, rel, stateX, relDev ? relDev.currentPower : null)}
	else if(cap == "Battery")		dev.currentBattery.each			{result = result || compare(it, rel, stateX, relDev ? relDev.currentBattery : null)}
	else if(cap == "Rule truth")	dev.each {
		def truth = parent.currentRule(it)
		result = result || stateX == "$truth"
	} 
	else if(cap == "Water sensor")				result = stateX in dev.currentWater
	else if(cap == "Switch") 					result = stateX in dev.currentSwitch
	else if(cap == "Motion") 					result = stateX in dev.currentMotion
	else if(cap == "Acceleration") 				result = stateX in dev.currentAcceleration
	else if(cap == "Contact") 					result = stateX in dev.currentContact
	else if(cap == "Presence") 					result = stateX in dev.currentPresence
	else if(cap == "Smoke detector") 			result = stateX in dev.currentSmoke
	else if(cap == "Carbon monoxide detector") 	result = stateX in dev.currentCarbonMonoxide
	else if(cap == "Lock") 						result = stateX in dev.currentLock
	else if(cap == "Garage door")				result = stateX in dev.currentDoor
	else if(cap == "Thermostat Mode")			result = stateX in dev.currentThermostatMode
	else if(cap == "Music player")				result = stateX in dev.currentStatus
	else if(cap == "Thermostat State")			result = stateX in dev.currentThermostatOperatingState
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
                "leaves": "present",
                "arrives": "not present",
                "locked": "unlocked",
                "unlocked": "locked"]
	def result = true
	if     (cap == "Temperature") 		dev.currentTemperature.each 	{result = result && compare(it, rel, stateX, relDev ? relDev.currentTemperature : null)}
	else if(cap == "Humidity") 			dev.currentHumidity.each    	{result = result && compare(it, rel, stateX, relDev ? relDev.currentHumidity : null)}
	else if(cap == "Illuminance") 		dev.currentIlluminance.each 	{result = result && compare(it, rel, stateX, relDev ? relDev.currentIlluminance : null)}
	else if(cap == "Dimmer level")		dev.currentLevel.each			{result = result && compare(it, rel, stateX, relDev ? relDev.currentLevel : null)}
	else if(cap == "Energy meter")		dev.currentEnergy.each			{result = result && compare(it, rel, stateX, relDev ? relDev.currentEnergy : null)}
	else if(cap == "Power meter")		dev.currentPower.each			{result = result && compare(it, rel, stateX, relDev ? relDev.currentPower : null)}
	else if(cap == "Battery")			dev.currentBattery.each			{result = result && compare(it, rel, stateX, relDev ? relDev.currentBattery : null)}
	else if(cap == "Rule truth")		dev.each {
    	def rule = parent.currentRule(it)
    	result = result && "$stateX" == "$rule"
	}
	else if(cap == "Water sensor")				result = !(flip[stateX] in dev.currentSwitch)
	else if(cap == "Switch") 					result = !(flip[stateX] in dev.currentSwitch)
	else if(cap == "Motion") 					result = !(flip[stateX] in dev.currentMotion)
	else if(cap == "Acceleration") 				result = !(flip[stateX] in dev.currentAcceleration)
	else if(cap == "Contact") 					result = !(flip[stateX] in dev.currentContact)
	else if(cap == "Presence") 					result = !(flip[stateX] in dev.currentPresence)
	else if(cap == "Smoke detector") 			result = !(flip[stateX] in dev.currentSmoke)
	else if(cap == "Carbon monoxide detector") 	result = !(flip[stateX] in dev.currentCarbonMonoxide)
	else if(cap == "Lock") 						result = !(flip[stateX] in dev.currentLock)
	else if(cap == "Garage door")				result = !(flip[stateX] in dev.currentDoor)
	else if(cap == "Music player")				result = !(flip[stateX] in dev.currentStatus)
	else if(cap == "Thermostat Mode")	dev.currentThermostatMode.each {result = result && stateX == it}
	else if(cap == "Thermostat State")	dev.currentThermmostatOperatingState.each {result = result && stateX == it}
//	log.debug "CheckAll $cap $result"
	return result
}


def getOperand(i, isR) {
	def result = true
    def foundItem = (settings.find {it.key == (isR ? "rCapab$i" : "tCapab$i")})
    if (foundItem == null) {
//        log.info "Cannot get operand for i: $i   isR: $isR"
        return null
    }
	def capab = (settings.find {it.key == (isR ? "rCapab$i" : "tCapab$i")}).value
	if     (capab == "Mode") result = modeOk
	else if(capab == "Time of day") result = timeOkX
	else if(capab == "Days of week") result = daysOk
	else if(capab == "Private Boolean") {
    	def thisState = settings.find{it.key == (isR ? "state$i" : "tstate$i")}
    	result = thisState.value == state.private //.toString()
	} else if(capab == "Smart Home Monitor") result = (settings.find {it.key == (isR ? "state$i" : "tstate$i")}).value == location.currentState("alarmSystemStatus")?.value
	else {
		def myDev = 	settings.find {it.key == (isR ? "rDev$i" : "tDev$i")}
		def myState = 	settings.find {it.key == (isR ? "state$i" : "tstate$i")}
		def myRel = 	settings.find {it.key == (isR ? "RelrDev$i" : "ReltDev$i")}
		def myAll = 	settings.find {it.key == (isR ? "AllrDev$i" : "AlltDev$i")}
		def myRelDev =  settings.find {it.key == (isR ? "relDevice$i" : "reltDevice$i")}
        if(!myDev) return false
		if(myAll) {
			if(myAll.value) result = checkCondAll(myDev.value, myState ? myState.value : 0, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : 0)
			else result = checkCondAny(myDev.value, myState ? myState.value : 0, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : 0)
		} else result = checkCondAny(myDev.value, myState ? myState.value : 0, capab, myRel ? myRel.value : 0, myRelDev ? myRelDev.value : 0)
	}
//    log.debug "operand $i is $result"
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
    state.parenLev = 0
    findRParen()
}

def evalTerm() {
	def result = true
	def thisTok = state.eval[state.token]
	if(thisTok == "(") {
		state.token = state.token + 1
		result = eval()
	} else result = getOperand(thisTok, true)
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

def toggle(devices, trufal) {
//	log.debug "toggle: $devices = ${devices*.currentValue('switch')}"
	def del = trufal ? delayMilTrue : delayMilFalse
	if (devices*.currentValue('switch').contains('on')) {
		if(del) devices.off([delay: del]) else devices.off()
	}
	else if (devices*.currentValue('switch').contains('off')) {
		if(del) devices.on([delay: del]) else devices.on()
	}
}

def dimToggle(devices, dimLevel, trufal) {
//	log.debug "dimToggle: $devices = ${devices*.currentValue('switch')}"
	def del = trufal ? delayMilTrue : delayMilFalse
	if (devices*.currentValue('switch').contains('on')) {if(del) devices.off([delay: del]) else devices.off()}
	else if(del) devices.setLevel(dimLevel, [delay: del]) else devices.setLevel(dimLevel)
}

def dimAdjust(devices, dimLevel, trufal) {
//	log.debug "dimAdjust: $devices = ${devices*.currentValue('level')}"
	def del = trufal ? delayMilTrue : delayMilFalse
    devices.each { if(del) it.setLevel(it.currentLevel + dimLevel, [delay: del]) else it.setLevel(it.currentLevel + dimLevel) }
}

def dimModes(trufal) {
	if(location.mode in (trufal ? dimmerModesTrue : dimmerModesFalse)) {
    	def lev = settings.find{it.key == ("level" + (trufal ? "True" : "False") + "$location.mode")}.value
        def dev = trufal ? dimMTrue : dimMFalse
        dev.setLevel(lev)
    }
}

def sendSmsMulti(phone, msg) {
	def num = ""
	def i = phone.indexOf('*')
	num = i > 0 ? phone.substring(0, i) : phone
	sendSms(num, msg)
	num = i > 0 ? phone.substring(i + 1) : ""
	if(num) sendSmsMulti(num, msg)
}

def doDelayTrue(time, rand, cancel) {
	def myTime = time
	if(rand) myTime = Math.random()*time
	if(cancel) {
		runIn(myTime, delayRuleTrue)
		state.delayRuleTrue = true
	} else runIn(myTime, delayRuleTrueForce)
	def isMins = time % 60 == 0
	if(isMins) time = time / 60
	def delayStr = isMins ? "minute" : "seconds"
	if(time > 1 && isMins) delayStr = delayStr + "s"
	if(state.isRule || state.howMany > 1) log.info ("$app.label is True, but " + (rand ? "random delay, up to $time minutes" : "delayed by $time $delayStr"))
	else log.info (rand ? "Random delay, up to $time minutes" : "Delayed by $time $delayStr")
}

def doDelayFalse(time, rand, cancel) {
	def myTime = time
	if(rand) myTime = Math.random()*time
	if(cancel) {
		runIn(myTime, delayRuleFalse)
		state.delayRuleFalse = true
	} else runIn(myTime, delayRuleFalseForce)
	def isMins = time % 60 == 0
	if(isMins) time = time / 60
	def delayStr = isMins ? "minute" : "seconds"
	if(time > 1 && isMins) delayStr = delayStr + "s"
	if(state.isRule || state.howMany > 1) log.info ("$app.label is False, but " + (rand ? "random delay, up to $time minutes" : "delayed by $time $delayStr"))
	else log.info (rand ? "Random delay, up to $time minutes" : "Delayed by $time $delayStr")
}

def capture(dev) {
	state.lastDevState = []
	def i = 0
	def switchState = null
	def dimmerValue = null
    def hueValue = null
    def satValue = null
	dev.each {
		switchState = it.currentSwitch
		dimmerValue = it.currentLevel 
        hueValue = it.currentHue
        satValue = it.currentSaturation
		state.lastDevState[i] = [switchState: switchState, dimmerValue: dimmerValue, hueValue: hueValue, satValue: satValue]
		i++       	
	}
}

def restoreDev(switches, switchState, dimmerValue, hueValue, satValue) {
    int hueX = hueValue in ["null", null] ? 0 : hueValue.toInteger()
    int satX = satValue in ["null", null] ? 0 : satValue.toInteger()
	if(switchState == "off") switches.off()
    else if(hueX > 0 && satX > 0) {
		def newValue = [hue: hueX, saturation: satX, level: dimmerValue]
        switches.setColor(newValue)
    } else if(dimmerValue) switches.setLevel(dimmerValue)
    else switches.on()
}

def restore() {
	def i = 0
	def switchState = null
	def dimmerValue = null
    def hueValue = null
    def satValue = null
	state.lastDevState.each {
    	switchState = it.switchState
        dimmerValue = it.dimmerValue
        hueValue = it.hueValue
        satValue = it.satValue
        if(captureTrue) restoreDev(captureTrue[i], switchState, dimmerValue, hueValue, satValue)
        if(captureFalse) restoreDev(captureFalse[i], switchState, dimmerValue, hueValue, satValue)
        i++
    }
}

def takeAction(success) {
	if(success) {
		if(captureTrue)			capture(captureTrue)
		if(onSwitchTrue) 		if(delayMilTrue) onSwitchTrue.on([delay: delayMilTrue]) else onSwitchTrue.on()
		if(toggleSwitchTrue)	toggle(toggleSwitchTrue, true)
		if(delayedOffTrue)	{   if(delayMinutesTrue) runIn(delayMinutesTrue * 60, delayOffTrue)
        						if(delaySecondsTrue) runIn(delaySecondsTrue, delayOffTrue)
								if(delayMillisTrue) {if(delayOnOffTrue) delayedOffTrue.on([delay: delayMillisTrue]) else delayedOffTrue.off([delay: delayMillisTrue])}   }
		if(pendedOffTrue)		{state.pendingOffTrue = true
								if(pendMinutesTrue > 0) 	runIn(pendMinutesTrue * 60, pendingOffTrue) else pendingOffTrue()}
		if(pendedOffFalse)  	state.pendingOffFalse = false  //unschedule(pendingOffFalse)}
		if(dimTrackTrue && dimATrue != null) if(state.lastEvtLevel != null) {if(delayMilTrue) dimATrue.setLevel(state.lastEvtLevel, [delay: delayMilTrue]) else dimATrue.setLevel(state.lastEvtLevel)}
		if(dimATrue && dimLATrue != null) if(delayMilTrue) dimATrue.setLevel(dimLATrue, [delay: delayMilTrue]) else dimATrue.setLevel(dimLATrue)
		if(dimBTrue && dimLBTrue != null) if(delayMilTrue) dimBTrue.setLevel(dimLBTrue, [delay: delayMilTrue]) else dimBTrue.setLevel(dimLBTrue)
		if(toggleDimmerTrue && dimTogTrue != null)	dimToggle(toggleDimmerTrue, dimTogTrue, true)
		if(adjustDimmerTrue && dimAdjTrue != null)	dimAdjust(adjustDimmerTrue, dimAdjTrue, true)
		if(dimmerModesTrue && dimMTrue)		dimModes(true)
		if(ctTrue && ctLTrue)   ctTrue.setColorTemperature(ctLTrue)
		if(bulbsTrue)			setColor(true)
		if(garageOpenTrue)		if(delayMilTrue) garageOpenTrue.open([delay: delayMilTrue]) else garageOpenTrue.open()
		if(garageCloseTrue)		if(delayMilTrue) garageCloseTrue.close([delay: delayMilTrue]) else garageCloseTrue.close()
		if(lockTrue) 			if(delayMilTrue) lockTrue.lock([delay: delayMilTrue]) else lockTrue.lock()
		if(unlockTrue) 			if(delayMilTrue) unlockTrue.unlock([delay: delayMilTrue]) else unlockTrue.unlock()
		if(fanAdjustTrue)		adjustFan(fanAdjustTrue)
		if(openValveTrue)		if(delayMilTrue) openValveTrue.open([delay: delayMilTrue]) else openValveTrue.open()
		if(closeValveTrue)		if(delayMilTrue) closeValveTrue.close([delay: delayMilTrue]) else closeValveTrue.close()
		if(thermoTrue)		{	if(thermoModeTrue) 	thermoTrue.setThermostatMode(thermoModeTrue)
								if(thermoSetHeatTrue)	thermoTrue.setHeatingSetpoint(thermoSetHeatTrue)
								if(thermoSetCoolTrue)	thermoTrue.setCoolingSetpoint(thermoSetCoolTrue) 	
								if(thermoFanTrue) 	thermoTrue.setThermostatFanMode(thermoFanTrue)   }
		if(alarmTrue)			sendLocationEvent(name: "alarmSystemStatus", value: "$alarmTrue")
		if(modeTrue) 			setLocationMode(modeTrue)
		if(ruleTrue)			parent.runRule(ruleTrue, app.label)
		if(ruleActTrue)			parent.runRuleAct(ruleActTrue, app.label)
		if(ruleEvalDelayTrue)	if(delayEvalMinutesTrue) runIn(delayEvalMinutesTrue * 60, delayEvalTrue)
		if(updateTrue)			parent.runUpdate(updateTrue)
		if(myPhraseTrue)		location.helloHome.execute(myPhraseTrue)
		if(cameraTrue) 		{	cameraTrue.take() 
                				(1..((burstCountTrue ?: 5) - 1)).each {cameraTrue.take(delay: (500 * it))}   }
		if(pushTrue)			sendPush((msgTrue ?: "Rule $app.label True") + (refDevTrue ? " $state.lastEvtName" : ""))
		if(noticeTrue)			sendNotificationEvent((msgTrue ?: "Rule $app.label True") + (refDevTrue ? " $state.lastEvtName" : ""))
		if(phoneTrue)			sendSmsMulti(phoneTrue, (msgTrue ?: "Rule $app.label True") + (refDevTrue ? " $state.lastEvtName" : ""))
		if(speakTrue)			speakTrueDevice?.speak((msgTrue ?: "Rule $app.label True") + (refDevTrue ? " $state.lastEvtName" : ""))
		if(mediaTrueDevice)		mediaTrueDevice.playTextAndRestore((msgTrue ?: "Rule $app.label True") + (refDevTrue ? " $state.lastEvtName" : ""), mediaTrueVolume)
		if(privateTrue)			if(otherTrue && otherPrivateTrue) parent.setRuleBoolean(otherPrivateTrue, privateTrue, app.label)
								else state.private = privateTrue // == "true"
		if(state.howManyCCtrue > 1)  execCommands(true)
		if(offSwitchTrue) 		if(delayMilTrue) offSwitchTrue.off([delay: delayMilTrue]) else offSwitchTrue.off()
        if(restoreTrue)			restore()
	} else {
		if(captureFalse)		capture(captureFalse)
		if(onSwitchFalse) 		if(delayMilFalse) onSwitchFalse.on([delay: delayMilFalse]) else onSwitchFalse.on()
		if(toggleSwitchFalse)	toggle(toggleSwitchFalse, false)
		if(delayedOffFalse)	{ 	if(delayMinutesFalse) runIn(delayMinutesFalse * 60, delayOffFalse)
        						if(delaySecondsFalse) runIn(delaySecondsFalse, delayOffFalse)
                				if(delayMillisFalse) {if(delayOnOffFalse) delayedOffFalse.on([delay: delayMillisFalse]) else delayedOffFalse.off([delay: delayMillisFalse])}   }
		if(pendedOffFalse)		{state.pendingOffFalse = true
        						if(pendMinutesFalse > 0) runIn(pendMinutesFalse * 60, pendingOffFalse) else pendingOffFalse()}
		if(pendedOffTrue)  		state.pendingOffTrue = false  //unschedule(pendingOffTrue)}
		if(dimTrackFalse && dimAFalse != null) if(state.lastEvtLevel != null) {if(delayMilFalse) dimAFalse.setLevel(state.lastEvtLevel, [delay: delayMilFalse]) else dimAFalse.setLevel(state.lastEvtLevel)}
		if(dimAFalse && dimLAFalse != null) if(delayMilFalse) dimAFalse.setLevel(dimLAFalse, [delay: delayMilFalse]) else dimAFalse.setLevel(dimLAFalse)
		if(dimBFalse && dimLBFalse != null) if(delayMilFalse) dimBFalse.setLevel(dimLBFalse, [delay: delayMilFalse]) else dimBFalse.setLevel(dimLBFalse)
		if(toggleDimmerFalse && dimTogFalse != null) dimToggle(toggleDimmerFalse, dimTogFalse, false)
		if(adjustDimmerFalse && dimAdjFalse != null) dimAdjust(adjustDimmerFalse, dimAdjFalse, false)
		if(dimmerModesFalse && dimMFalse)		dimModes(false)
		if(ctFalse)   			ctFalse.setColorTemperature(ctLFalse)
		if(bulbsFalse)			setColor(false)
		if(garageOpenFalse)		if(delayMilFalse) garageOpenFalse.open([delay: delayMilFalse]) else garageOpenFalse.open()
		if(garageCloseFalse)	if(delayMilFalse) garageCloseFalse.close([delay: delayMilFalse]) else garageCloseFalse.close()
		if(lockFalse) 			if(delayMilFalse) lockFalse.lock([delay: delayMilFalse]) else lockFalse.lock()
		if(unlockFalse) 		if(delayMilFalse) unlockFalse.unlock([delay: delayMilFalse]) else unlockFalse.unlock()
		if(fanAdjustFalse)		adjustFan(fanAdjustFalse)
		if(openValveFalse)		if(delayMilFalse) openValveFalse.open([delay: delayMilFalse]) else openValveFalse.open()
		if(closeValveFalse)		if(delayMilFalse) closeValveFalse.close([delay: delayMilFalse]) else closeValveFalse.close()
		if(thermoFalse)		{	if(thermoModeFalse) 	thermoFalse.setThermostatMode(thermoModeFalse)
								if(thermoSetHeatFalse) 	thermoFalse.setHeatingSetpoint(thermoSetHeatFalse)
								if(thermoSetCoolFalse) 	thermoFalse.setCoolingSetpoint(thermoSetCoolFalse) 	
								if(thermoFanFalse)	thermoFalse.setThermostatFanMode(thermoFanFalse)   }
		if(alarmFalse)			sendLocationEvent(name: "alarmSystemStatus", value: "$alarmFalse")
		if(modeFalse) 			setLocationMode(modeFalse)
		if(ruleFalse)			parent.runRule(ruleFalse, app.label)
		if(ruleActFalse)		parent.runRuleAct(ruleActFalse, app.label)
		if(ruleEvalDelayFalse)	if(delayEvalMinutesFalse) runIn(delayEvalMinutesFalse * 60, delayEvalFalse)
		if(updateFalse)			parent.runUpdate(updateFalse)
		if(myPhraseFalse) 		location.helloHome.execute(myPhraseFalse)
		if(cameraFalse) 	{	cameraFalse.take() 
                				(1..((burstCountFalse ?: 5) - 1)).each {cameraFalse.take(delay: (500 * it))}   }
		if(pushFalse)			sendPush((msgFalse ?: "Rule $app.label False") + (refDevFalse ? " $state.lastEvtName" : ""))
		if(noticeFalse)			sendNotificationEvent((msgFalse ?: "Rule $app.label False") + (refDevFalse ? " $state.lastEvtName" : ""))
		if(phoneFalse)			sendSmsMulti(phoneFalse, (msgFalse ?: "Rule $app.label False") + (refDevFalse ? " $state.lastEvtName" : ""))
		if(speakFalse)			speakFalseDevice?.speak((msgFalse ?: "Rule $app.label False") + (refDevFalse ? " $state.lastEvtName" : ""))
		if(mediaFalseDevice)	mediaFalseDevice.playTextAndRestore((msgFalse ?: "Rule $app.label False") + (refDevFalse ? " $state.lastEvtName" : ""), mediaFalseVolume)		
		if(privateFalse)		if(otherFalse && otherPrivateFalse) parent.setRuleBoolean(otherPrivateFalse, privateFalse, app.label)
								else state.private = privateFalse
		if(state.howManyCCfalse > 1)  	execCommands(false)
		if(offSwitchFalse) 		if(delayMilFalse) offSwitchFalse.off([delay: delayMilFalse]) else offSwitchFalse.off()
		if(restoreFalse)		restore()
	}
}

def runRule(force) {
	if(!allOk) return
	state.token = 0
	def success = eval()
	if((success != state.success) || force) {
//		unschedule(delayRuleTrue)
//		unschedule(delayRuleFalse)
		state.delayRuleTrue = false
        state.delayRuleFalse = false
		if     (delayTrue > 0 && success)		doDelayTrue(delayTrue * 60, false, true)
		else if(delayFalse > 0 && !success)		doDelayFalse(delayFalse * 60, false, true)
		else if(delayMinTrue > 0 && success) 	doDelayTrue(delayMinTrue * 60, randTrue, cancelTrue)
		else if(delayMinFalse > 0 && !success) 	doDelayFalse(delayMinFalse * 60, randFalse, cancelFalse)
		else if(delaySecTrue > 0 && success)	doDelayTrue(delaySecTrue, false, cancelTrue)
		else if(delaySecFalse > 0 && !success)	doDelayFalse(delaySecFalse, false, cancelFalse)
		else if(randomTrue > 0 && success) 		doDelayTrue(randomTrue * 60, true, true)
		else if(randomFalse > 0 && !success) 	doDelayFalse(randomFalse * 60, true, true)
		else takeAction(success)
		parent.setRuleTruth(app.label, success)
		state.success = success
		log.info (success ? "$app.label is now True" : "$app.label is now False")
//      sendNotificationEvent(success ? "$app.label is True" : "$app.label is False")
	} // else log.info "$app.label evaluated " + (success ? "true" : "false")
}

def doTrigger() {
	if(!allOk) return
	if     (delayTrue > 0)		doDelayTrue(delayTrue * 60, false, true)
	else if(delayMinTrue > 0)	doDelayTrue(delayMinTrue * 60, randTrue, true)
	else if(delaySecTrue > 0)	doDelayTrue(delaySecTrue, false, true)	 
	else if(randomTrue > 0) 	doDelayTrue(randomTrue * 60, true, true)
	else takeAction(true)
	log.info ("$app.label Triggered")
//  sendNotificationEvent("$app.label Ran")
}

def getButton(dev, evt, i) {
	def numNames = ["", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
    	"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"]
	def buttonNumber = evt.jsonData.buttonNumber.toInteger() 
    def value = evt.value
//	log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
//	log.debug "button: $buttonNumber, value: $value"
//	log.debug "button json: $evt.jsonData.buttonNumber"
	def recentEvents = dev.eventsSince(new Date(now() - 3000)).findAll{it.value == evt.value && it.data == evt.data}
//	log.debug "Found ${recentEvents.size()?:0} events in past second"
	def thisButton = 0
	def firstEventId = 0
	if (recentEvents.size() != 0) {
		firstEventId = recentEvents[0].id
	} 
	if(firstEventId == evt.id){
		thisButton = numNames[buttonNumber]
//	} else {
//		log.debug "Found recent button press events for $buttonNumber with value $value"
	}
	def myState = settings.find {it.key == (state.isTrig ? "state$i" : "tstate$i")}
	def myButton = settings.find {it.key == (state.isTrig ? "ButtonrDev$i" : "ButtontDev$i")}
    def result = true
    if(value in ["pushed", "held"]) result = (value == myState.value) && (thisButton == myButton.value)
    else if(value.startsWith("button")) result = thisButton == myButton.value // ZWN-SC7
}

def testEvt(evt) {
	def result = false
	def howMany = state.isTrig ? state.howMany : state.howManyT
	if(evt.name == "mode") return modeXOk
	if(evt.name == "routineExecuted") {
		for(int i = 1; i < howMany; i++) {
			def myCapab = (settings.find {it.key == (state.isTrig ? "rCapab$i" : "tCapab$i")}).value
			def state = settings.find {it.key == (state.isTrig ? "state$i" : "tstate$i")}
			if(myCapab == "Routine") result = result || evt.displayName == state.value
			if(result) return true
		}
        return false
	}
	for(int i = 1; i < howMany; i++) {
		def myDev = settings.find {it.key == (state.isTrig ? "rDev$i" : "tDev$i")}
		if(myDev) {
        	myDev = myDev.value
        	myDev.each {if(evt.displayName == it.displayName) {
				if(evt.name == "button") result = getButton(myDev, evt, i)
				else result = getOperand(i, state.isTrig)}
			}
        }
		if(result) return result
	}
	return result
}

def allHandler(evt) {
	if(!allOk) return
	log.info "$app.label: $evt.displayName $evt.name $evt.value"
	state.lastEvtName = evt.displayName
    if(evt.name == "level") state.lastEvtLevel = evt.value.toInteger()
	def hasTrig = state.howManyT > 1
	def hasCond = state.howMany > 1
	def doit = true
	if(state.isTrig) {
		if(evt.name in ["temperature", "humidity", "power", "energy", "battery", "illuminance", "mode", "button", "routineExecuted", "level", "presence"]) doit = testEvt(evt)
		if (doit) doTrigger() }
	else if(state.isRule) runRule(false)
	else {
		if(hasTrig) if(evt.name in ["temperature", "humidity", "power", "energy", "battery", "illuminance", "mode", "button", "routineExecuted", "level", "presence"]) doit = testEvt(evt)
		if(hasCond) {if(doit) runRule(hasTrig)}
		else if(doit) doTrigger()
	}
}

def startHandler() {
	runRule(false)
}

def stopHandler() {
	runRule(false)
}

def dayHandler() {
	runRule(false)
}

def timeHandler() {
	if(state.howMany > 1 && !state.isTrig) runRule(true)
	else if(state.isTrig || state.howManyT > 1) doTrigger()
}

def physicalHandler(evt) {
	if(evt.isPhysical()) allHandler(evt)
}

def delayOffTrue() {
	if(allOk) {if(delayOnOffTrue) delayedOffTrue.on() else delayedOffTrue.off()}
}

def pendingOffTrue() {
	if(allOk && state.pendingOffTrue) {if(pendOnOffTrue) pendedOffTrue.on() else pendedOffTrue.off()}
}

def delayEvalTrue() {
	if(allOk) parent.runRule(ruleEvalDelayTrue, app.label)
}

def delayOffFalse() {
	if(allOk) {if(delayOnOffFalse) delayedOffFalse.on() else delayedOffFalse.off()}
}

def pendingOffFalse() {
	if(allOk && state.pendingOffFalse) {if(pendOnOffFalse) pendedOffFalse.on() else pendedOffFalse.off()}
}

def delayEvalFalse() {
	if(allOk) parent.runRule(ruleEvalDelayFalse, app.label)
}

def delayRuleTrue() {
	if(allOk && state.delayRuleTrue) takeAction(true)
}

def delayRuleTrueForce() {
	if(allOk) takeAction(true)
}

def delayRuleFalse() {
	if(allOk && state.delayRuleFalse) takeAction(false)
}

def delayRuleFalseForce() {
	if(allOk) takeAction(false)
}

def disabledHandler(evt) {
	def disOnOff = disabledOff ? "off" : "on"
	state.disabled = evt.value == disOnOff
}

def ruleHandler(rule, truth) {
	log.info "$app.label: $rule is $truth"
	if(state.isRule || state.howMany > 1) runRule(false) else doTrigger()
}

def ruleEvaluator(rule) {
	log.info "$app.label: $rule evaluate"
	runRule(true)
}

def ruleActions(rule) {
	log.info "$app.label: $rule evaluate"
    if(allOk) takeAction(true)
}

def setBoolean(truth, appLabel) {
	log.info "$app.label: Set Boolean from $appLabel: $truth"
	state.private = truth // == "true"
	if(state.isRule || state.howMany > 1) runRule(false) 
    else for(int i = 1; i < state.howManyT; i++) {
		def myCap = settings.find {it.key == "tCapab$i"}
		if(myCap.value == "Private Boolean") if(getOperand(i, false)) {
        	doTrigger()
            return
        }
    }
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

private timeIntervalLabelX() {
	def result = ""
	if (startingXX == "Sunrise" && endingXX == "Sunrise") result = "Sunrise" + offset(startSunriseOffsetX) + " and Sunrise" + offset(endSunriseOffsetX)
	else if (startingXX == "Sunrise" && endingXX == "Sunset") result = "Sunrise" + offset(startSunriseOffsetX) + " and Sunset" + offset(endSunsetOffsetX)
	else if (startingXX == "Sunset" && endingXX == "Sunrise") result = "Sunset" + offset(startSunsetOffsetX) + " and Sunrise" + offset(endSunriseOffsetX)
	else if (startingXX == "Sunset" && endingXX == "Sunset") result = "Sunset" + offset(startSunsetOffsetX) + " and Sunset" + offset(endSunsetOffsetX)
	else if (startingXX == "Sunrise" && endingA) result = "Sunrise" + offset(startSunriseOffsetX) + " and " + hhmm(endingA, "h:mm a z")
	else if (startingXX == "Sunset" && endingA) result = "Sunset" + offset(startSunsetOffsetX) + " and " + hhmm(endingA, "h:mm a z")
	else if (startingA && endingXX == "Sunrise") result = hhmm(startingA) + " and Sunrise" + offset(endSunriseOffsetX)
	else if (startingA && endingXX == "Sunset") result = hhmm(startingA) + " and Sunset" + offset(endSunsetOffsetX)
	else if (startingA && endingA) result = hhmm(startingA) + " and " + hhmm(endingA, "h:mm a z")
}

private getAllOk() {
	def okay = !(usePrivateDisable && state.private == "false")
	if(state.isRule) modeZOk && !state.disabled  && okay //&& daysOk && timeOk
	else if(state.isTrig) modeYOk && daysOk && timeOk && !state.disabled && okay
	else modeYOk && daysYOk && timeOk && !state.disabled && okay
}

private hideOptionsSection() {
	if(state.isRule || state.howMany > 1) (modesZ || daysY || modesY || disabled || starting || ending || startingX || endingX) ? false : true
	else (starting || ending || daysY || modes || modesY || startingX || endingX || disabled) ? false : true
}

private getModeZOk() {
	def result = !modesZ || modesZ.contains(location.mode)
//	log.trace "modeZOk = $result"
	return result
}

private getModeYOk() {
	def result = !modesY || modesY.contains(location.mode)
//	log.trace "modeYOk = $result"
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

private getDaysYOk() {
	def result = true
	if (daysY) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) df.setTimeZone(location.timeZone)
		else df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		def day = df.format(new Date())
		result = daysY.contains(day)
	}
//	log.trace "daysYOk = $result"
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
		else if(starting) start = timeToday(starting, location.timeZone).time    //  Crash here means time zone not set!!
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
//	log.trace "getTimeOk = $result"
	return result
}

private getTimeOkX() {
	def result = true
	if((startingA && endingA) ||
	(startingA && endingXX in ["Sunrise", "Sunset"]) ||
	(startingXX in ["Sunrise", "Sunset"] && endingA) ||
	(startingXX in ["Sunrise", "Sunset"] && endingXX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffsetX, sunsetOffset: startSunsetOffsetX)
		if(startingXX == "Sunrise") start = s.sunrise.time
		else if(startingXX == "Sunset") start = s.sunset.time
		else if(startingA) start = timeToday(startingA, location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffsetX, sunsetOffset: endSunsetOffsetX)
		if(endingXX == "Sunrise") stop = s.sunrise.time
		else if(endingXX == "Sunset") stop = s.sunset.time
		else if(endingA) stop = timeToday(endingA,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
//	log.trace "getTimeOkX = $result"
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

//custom command execute method
def execCommand(devices,cmdID){
	def result = ""
	def pList = []
	def cmdMap = parent.getCommandMap(cmdID) 
	if (cmdMap) {
		def params = cmdMap.params.sort()
		params.each{ p ->
			if (p.value.type == "string"){
				pList << "${p.value.value}"
			} else if (p.value.type == "decimal"){
				pList << p.value.value.toBigDecimal()
			} else {
				pList << p.value.value.toInteger()
			}
		}
		def p = pList as Object[]
		devices.each { device ->
			try {
				device."${cmdMap.cmd}"(p)
			}
			catch (IllegalArgumentException e){
				def em = e as String
				def ems = em.split(":")
				ems = ems[2].replace(" [","").replace("]","")
				ems = ems.replaceAll(", ","\n")
				log.error "${device.displayName}, command failed, valid commands:\n${ems}"
			}
			catch (e) {
				log.error "${device.displayName}, command failed:\n${e}"
			}
		}
	}
}

def execCommands(truth){
	def devicePrefix
	def commandPrefix
	def theseDevices
	def thisCommand
	def howMany
	if (truth) {
		devicePrefix = "customDeviceTrue"
		commandPrefix = "ccTrue"
		howMany = state.howManyCCtrue
	} else {
		devicePrefix = "customDeviceFalse"
		commandPrefix = "ccFalse"
		howMany = state.howManyCCfalse
	}	
	for (int i = 1; i < howMany; i++) {
		if (i == 1) theseDevices = devicePrefix
		else theseDevices = devicePrefix + "${i}"
		if (i == 1) thisCommand = commandPrefix
		else thisCommand = commandPrefix + "${i}"
		def devices = settings."${theseDevices}"
		def cmdID = settings."${thisCommand}"
		//log.debug "truth:${truth} devices:${devices} cmdID:${cmdID}"
		 execCommand(devices,cmdID)
	}
}

def selectCustomActions(){
	def cstCmds = state.cstCmds.sort()
	def truth = state.ccTruth
	def devicePrefix
	def commandPrefix
	def theseDevices
	def thisCommand
	def allDevices
	def allCommands
	def howMany
	if (truth) {
		devicePrefix = "customDeviceTrue"
		commandPrefix = "ccTrue"
		allDevices = settings.findAll{it.key.startsWith(devicePrefix)}
		allCommands = settings.findAll{it.key.startsWith(commandPrefix)}
		if (allDevices.size() <= allCommands.size()) state.howManyCCtrue = allDevices.size() + 1
		else state.howManyCCtrue = allDevices.size()
		howMany = state.howManyCCtrue
		state.cmdActTrue = ""
	} else {
		devicePrefix = "customDeviceFalse"
		commandPrefix = "ccFalse"
		allDevices = settings.findAll{it.key.startsWith(devicePrefix)}
		allCommands = settings.findAll{it.key.startsWith(commandPrefix)}
		if (allDevices.size() <= allCommands.size()) state.howManyCCfalse = allDevices.size() + 1
		else state.howManyCCfalse = allDevices.size()
		howMany = state.howManyCCfalse
		state.cmdActFalse = ""
	}
	dynamicPage(name: "selectCustomActions", title: "Select Custom Command Actions", uninstall: false) {
		for (int i = 1; i <= howMany; i++) {
			if (i == 1) theseDevices = devicePrefix
			else theseDevices = devicePrefix + "${i}" 
			def crntDevices = settings."${theseDevices}"
			section("custom device #${i}"){
				input (name: theseDevices, type: "capability.actuator", title: "Custom devices", multiple: true, required: false, submitOnChange: true)
				if (crntDevices) {
					if (i == 1) thisCommand = commandPrefix
					else thisCommand = commandPrefix + "${i}"
					def crntCommand = settings."${thisCommand}"
					input( name: thisCommand, type: "enum", title: "Run this command", multiple: false, required: false, options: cstCmds, submitOnChange: true)
					if (crntCommand){
						if (cstCmds.find{it[crntCommand]}) {
							def c = cstCmds.find{it[crntCommand]}[crntCommand]
							cmdActions("Command: ${c} on ${crntDevices}",truth)    
						}
					}
				}
			}
		}
		if (truth) {
			if (state.cmdActTrue) state.cmdActTrue = state.cmdActTrue[0..-2]
		} else {
			if (state.cmdActFalse) state.cmdActFalse = state.cmdActFalse[0..-2]
		}   
	}
}

def cmdActions(str, truth) {
	if (truth) state.cmdActTrue = state.cmdActTrue + stripBrackets("$str") + "\n"
	else state.cmdActFalse = state.cmdActFalse + stripBrackets("$str") + "\n"
}
