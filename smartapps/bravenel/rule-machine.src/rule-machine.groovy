/**
 *  Rule Machine
 *
 *  Copyright 2015, 2016 Bruce Ravenel
 *
 *  Version 1.9.0a   25 Mar 2016
 *
 *	Version History
 *
 *	1.9.0	24 Mar 2016		Updates for Rule, small bug fixes
 *	1.8.2	9 Mar 2016		Changed startup page for installation
 *	1.8.1	3 Mar 2016		Changed method of getting Rule version
 *	1.8.0	2 Mar 2016		Clean up, added Door control
 *	1.7.6	24 Feb 2016		Added User Guide link, fixed Rule truth mechanism
 *	1.7.5	21 Feb 2016		Improved custom command selection
 *	1.7.4	20 Feb 2016		Added saved command display, UI improvements
 *	1.7.3	14 Feb 2016		Improved Rule Machine initialization, fixed Delete custom commands bug
 *	1.7.2	8 Feb 2016		Added set Boolean for Rules
 *	1.7.1	5 Feb 2016		Added update Rule
 *	1.7.0	31 Jan 2016		Added run Rule actions
 *	1.6.6	10 Jan 2016		Improved method of getting custom device commands
 *	1.6.5	1 Jan 2016		Added version numbers to main page
 *	1.6.4	30 Dec 2015		Multi-commands
 *	1.6.3	26 Dec 2015		UI improvements and icon per Michael Struck
 *	1.6.2	25 Dec 2015		null parameter value patch in expert, maxwell
 *	1.6.1	24 Dec 2015		UI improvement
 *	1.6.0	23 Dec 2015		Added expert commands per Mike Maxwell
 *
 *  This software if free for Private Use. You may use and modify the software without distributing it.
 *  
 *  This software and derivatives may not be used for commercial purposes.
 *  You may not modify, distribute or sublicense this software.
 *  You may not grant a sublicense to modify and distribute this software to third parties not included in the license.
 *
 *  Software is provided without warranty and the software author/license owner cannot be held liable for damages.
 *
 */

definition(
	name: "Rule Machine",
	singleInstance: true,
	namespace: "bravenel",
	author: "Bruce Ravenel and Mike Maxwell",
	description: "Rule Machine",
	category: "My Apps",
	iconUrl: "https://raw.githubusercontent.com/bravenel/Rule-Trigger/master/smartapps/bravenel/RuleMachine.png",
	iconX2Url: "https://raw.githubusercontent.com/bravenel/Rule-Trigger/master/smartapps/bravenel/RuleMachine%402x.png",
	iconX3Url: "https://raw.githubusercontent.com/bravenel/Rule-Trigger/master/smartapps/bravenel/RuleMachine%402x.png"
)

preferences {
	page(name: "mainPage")
    page(name: "firstPage")
	page(name: "removePage")
	//expert pages
	page(name: "customCommandsPAGE")
	page(name: "generalApprovalPAGE")
	page(name: "addCustomCommandPAGE")
	page(name: "customParamsPAGE")
}

def mainPage() {
	if(!state.setup) firstRun()
    else {
    	if(state.ruleState) state.ruleState = null  // obsolete
    	def nApps = childApps.size()
    	dynamicPage(name: "mainPage", title: "Installed Rules, Triggers and Actions " + (nApps > 0 ? "[$nApps]" : ""), install: true, uninstall: false) {
        	section {
            	app(name: "childRules", appName: "Rule", namespace: "bravenel", title: "Create New Rule...", multiple: true)
        	}
			section ("Expert Features") {
				href("customCommandsPAGE", title: null, description: anyCustom() ? "Custom Commands..." : "Tap to create Custom Commands", state: anyCustom())
        	}
        	section ("Rule Machine User Guide") {
				href url:"https://community.smartthings.com/t/rule-machine-user-guide/40176", style:"embedded", required:false, description:"Tap to view User Guide", title: ""
        	}
        	section ("Remove Rule Machine"){
        		href "removePage", description: "Tap to remove Rule Machine and Rules", title: ""
        	}
			section ("Version 1.9.0a/" + (nApps > 0 ? "${childApps[0].appVersion()}" : "---")) { }
    	}
    }
}

def removePage() {
	dynamicPage(name: "removePage", title: "Remove Rule Machine And All Rules", install: false, uninstall: true) {
    	section ("WARNING!\n\nRemoving Rule Machine also removes all Rules\n") {
        }
    }
}

def installed() {
}

def updated() {
}

def firstRun() {
	state.setup = true
	state.ruleSubscribers = [:]
    dynamicPage(name: "firstPage", title: "Hit Done to install Rule Machine", install: true, uninstall: false) { }
}

def childVersion() {
	def result = "---"
	if(childApps.size() > 0) result = childApps[0].appVersion()
    return result
}

def ruleList(appLabel) {
	def result = []
	childApps.each { child ->
		if(child.name == "Rule" && child.label != appLabel) result << child.label
	}
	return result
}

def subscribeRule(appLabel, ruleName, ruleTruth, childMethod) {
//	log.debug "subscribe: $appLabel, $ruleName, $ruleTruth, $childMethod"
	ruleName.each {name ->
    	state.ruleSubscribers[name].each {if(it == appLabel) return}
        if(state.ruleSubscribers[name] == null) state.ruleSubscribers[name] = ["$appLabel":ruleTruth]
    	else state.ruleSubscribers[name] << ["$appLabel":ruleTruth]
    }
}

def setRuleTruth(appLabel, ruleTruth) {
//	log.debug "setRuleTruth: $appLabel, $ruleTruth, ${state.ruleState[appLabel]}"
	def thisList = state.ruleSubscribers[appLabel]
	thisList.each {
		if(it.value == null || "$it.value" == "$ruleTruth") {
			childApps.each { child ->
				if(child.label == it.key) child.ruleHandler(appLabel, ruleTruth)
			}
		}
	}
}

def setRuleBoolean(rule, ruleBoolean, appLabel) {
//	log.debug "setRuleBoolean: $appLabel, $ruleBoolean"
	childApps.each { child ->
    	rule.each {
			if(child.label == it) child.setBoolean(ruleBoolean, appLabel)
        }
	}	
}

def currentRule(appLabel) {
	def result
	childApps.each { child ->
		if(child.label == appLabel) result = child.revealSuccess()
	}
    return result
}

def childUninstalled() {
//	log.debug "childUninstalled called"
}

def removeChild(appLabel) {
//	log.debug "removeChild: $appLabel"
	unSubscribeRule(appLabel)
	if(state.ruleSubscribers[appLabel] != null) state.ruleSubscribers.remove(appLabel)
}

def unSubscribeRule(appLabel) {
//	log.debug "unSubscribeRule: $appLabel"
	state.ruleSubscribers.each { rule ->
        def newList = [:]
        rule.value.each {list ->
        	if(list.key != appLabel) newList << list
        }
        rule.value = newList
    }
}

def runRule(rule, appLabel) {
//	log.debug "runRule: $rule, $appLabel"
	childApps.each { child ->
		rule.each {
			if(child.label == it) child.ruleEvaluator(appLabel)
		}
	}
}

def runRuleAct(rule, appLabel) {
//	log.debug "runRuleAct: $rule, $appLabel"
	childApps.each { child ->
		rule.each {
			if(child.label == it) child.ruleActions(appLabel)
		}
	}
}

def runUpdate(rule) {
//	log.debug "runUpdate: $rule"
	childApps.each { child ->
		rule.each {
			if(child.label == it) child.updated()
		}
	}
}

/*****custom command specific pages	*****/
def generalApprovalPAGE(params){
	def title = params.title
	def method = params.method
	def result
	dynamicPage(name: "generalApprovalPAGE", title: title ){
		section() {
			if (method) {
				result = app."${method}"()
				paragraph "${result}"
			}
		}
	}
}

def customCommandsPAGE() {
	if (!state.lastCmdIDX) state.lastCmdIDX = 0
	def savedCommands = getMyCommands()
	dynamicPage(name: "customCommandsPAGE", title: "Custom Commands", uninstall: false, install: false) {
    	def hasCommands = savedCommands.size() != 0
		def result = ""
		if (hasCommands){
			def cmdMaps = state.customCommands ?: []
			cmdMaps.each{ cmd ->
				def cont = cmd.value.text.size() > 30 ? "..." : ""
				result = result + "\n\t" + cmd.value.text.take(30) + cont
			}
        } else state.lastCmdIDX = 0
		section(hasCommands ? "Saved commands:\n" + result : "") {
			if (hasCommands) {
				input(
					name			: "deleteCmds"
					,title			: "Delete saved commands"
					,multiple		: true
					,required		: false
                    ,description	: ""
					,type			: "enum"
					,options		: savedCommands
					,submitOnChange	: true
				)
				if (isValidCommand(deleteCmds)){
					href( "generalApprovalPAGE"
						,title			: "Delete commands now"
						,description	: ""
						,state			: null
						,params			: [method:"deleteCommands",title:"Delete commands"]
						,submitOnChange	: true
					)
				}
                paragraph("")
			}
			getCapab()
            if(myCapab) getDevs()
            if(devices && hasCommands) {
				input(
					name			: "testCmd"
					,title			: "Test saved command on\n$devices"
					,multiple		: false
					,required		: false
                    ,description	: ""
					,type			: "enum"
					,options		: savedCommands
					,submitOnChange	: true
				)
				def res = execCommand(settings.testCmd)
				if (res) paragraph("${result}")
            }
		}
		section(){
			if (devices){
				href( "addCustomCommandPAGE"
					,title		: "New custom command..."
					,description: ""
					,state		: null
				)
			}
        }
	}
}

def getCapab() {  
	def myOptions = ["Acceleration", "Actuator", "Button", "Carbon monoxide detector", "Contact", "Dimmer", "Door", "Energy meter", "Garage door", "Humidity", "Illuminance", 
    	"Lock", "Motion", "Power meter", "Presence", "Smoke detector", "Switch", "Temperature", "Thermostat", "Water sensor", "Music player"]
	def result = input "myCapab", "enum", title: "Select capability for test device", required: false, options: myOptions.sort(), submitOnChange: true
}

def getDevs() {
	def multi = false
    def thisName = ""
    def thisCapab = ""
	switch(myCapab) {
		case "Switch":
			thisName = "switch"
			thisCapab = "switch"
			break
		case "Actuator":
			thisName = "actuator"
			thisCapab = "actuator"
			break
		case "Motion":
			thisName = "motion sensor"
			thisCapab = "motionSensor"
			break
		case "Button":
			thisName = "button device"
			thisCapab = "button"
			break
		case "Acceleration":
			thisName = "acceleration sensor"
			thisCapab = "accelerationSensor"
			break        
		case "Contact":
			thisName = "contact sensor"
			thisCapab = "contactSensor"
			break
		case "Presence":
			thisName = "presence sensor"
			thisCapab = "presenceSensor"
			break
		case "Garage door":
			thisName = "garage door"
			thisCapab = "garageDoorControl"
			break
		case "Door":
			thisName = "door"
			thisCapab = "doorControl"
			break
		case "Lock":
			thisName = "lock"
			thisCapab = "lock"
			break
		case "Dimmer":
			thisName = "dimmer" + (multi ? "s" : "")
			thisCapab = "switchLevel"
			break
		case "Temperature":
			thisName = "temperature sensor" + (multi ? "s" : "")
			thisCapab = "temperatureMeasurement"
			break
		case "Thermostat":
			thisName = "thermostat" + (multi ? "s" : "")
			thisCapab = "thermostat"
			break
		case "Humidity":
			thisName = "humidity sensor" + (multi ? "s" : "")
			thisCapab = "relativeHumidityMeasurement"
			break
		case "Illuminance":
			thisName = "illuminance sensor" + (multi ? "s" : "")
			thisCapab = "illuminanceMeasurement"
			break
		case "Energy meter":
			thisName = "energy meter" + (multi ? "s" : "")
			thisCapab = "energyMeter"
			break
		case "Power meter":
			thisName = "power meter" + (multi ? "s" : "")
			thisCapab = "powerMeter"
			break
		case "Carbon monoxide detector":
			thisName = "CO detector" + (multi ? "s" : "")
			thisCapab = "carbonMonoxideDetector"
			break
		case "Smoke detector":
			thisName = "smoke detector" + (multi ? "s" : "")
			thisCapab = "smokeDetector"
			break
		case "Water sensor":
			thisName = "water sensor"
			thisCapab = "waterSensor"
			break
		case "Music player":
			thisName = "music player"
			thisCapab = "musicPlayer"
			break
	}
	def result = input "devices", "capability.$thisCapab", title: "Select $thisName to test for commands", required: false, multiple: multi, submitOnChange: true
}

def addCustomCommandPAGE(){
	def cmdLabel = getCmdLabel()
	def complete = "" 
	def test = false
    def rest = getDeviceCommands()
    rest = "$rest"[1..-2]
	def pageTitle = "Create new custom command from\n\n${devices}\n\nAvailable commands:\n\n" + rest
	if (cmdLabel){
		complete = "complete"
		test = true
	}
	dynamicPage(name: "addCustomCommandPAGE", title: pageTitle, uninstall: false, install: false) {
		section(){
			input(
		   		name			: "cCmd"
				,title			: "Select custom command"
				,multiple		: false
				,required		: false
				,type			: "enum"
				,options		: getDeviceCommands()
				,submitOnChange	: true
			)
			href( "customParamsPAGE"
				,title: "Parameters"
				,description: parameterLabel()
				,state: null
			)
		}
		if (cCmd){
			def result = execTestCommand()
			section("Configured command: ${cmdLabel}\n${result}"){
				if (result == "succeeded"){
					if (!commandExists(cmdLabel)){
						href( "generalApprovalPAGE"
							,title		: "Save command now"
							,description: ""
							,state		: null
							,params		: [method:"addCommand",title:"Add Command"]
						)
					}
				} 
			}
		}
	}
}

def customParamsPAGE(p){
	def ct = settings.findAll{it.key.startsWith("cpType_")}
	state.howManyP = ct.size() + 1
	def howMany = state.howManyP
	dynamicPage(name: "customParamsPAGE", title: "Select parameters", uninstall: false) {
		if(howMany) {
			for (int i = 1; i <= howMany; i++) {
				def thisParam = "cpType_" + i
				def myParam = ct.find {it.key == thisParam}
				section("Parameter #${i}") {
					getParamType(thisParam, i != howMany)
					if(myParam) {
						def pType = myParam.value
						getPvalue(pType, i)
					}
				}
			}
		}
	}
}

/***** child specific methods *****/

def getCommandMap(cmdID){
	return state.customCommands["${cmdID}"]
}


/***** local custom command specific methods *****/
def anyCustom(){
	def result = null
	if (getCommands()) result = "complete"
	return result
}

def getParamType(myParam,isLast){  
	def myOptions = ["string", "number", "decimal"]
	def result = input (
					name			: myParam
					,type			: "enum"
					,title			: "parameter type"
					,required		: isLast
					,options		: myOptions
					,submitOnChange	: true
				)
	return result
}

def getPvalue(myPtype, n){
	def myVal = "cpVal_" + n
	def result = null
	if (myPtype == "string"){
		result = input(
					name		: myVal
					,title		: "string value"
					,type		: "text"
					,required	: false
				)
	} else if (myPtype == "number"){
		result = input(
					name		: myVal
					,title		: "integer value"
					,type		: "number"
					,required	: false
				)
	} else if (myPtype == "decimal"){
		result = input(
					name		: myVal
					,title		: "decimal value"
					,type		: "decimal"
					,required	: false
				)
	}
	return result
}

def getCmdLabel(){
	def cmd
	if (settings.cCmd) cmd = settings.cCmd.value
	def cpTypes = settings.findAll{it.key.startsWith("cpType_")}.sort()
	def result = null
	if (cmd) {
		result = "${cmd}("
		if (cpTypes.size() == 0){
			result = result + ")"
		} else {
        	def r = getParams(cpTypes)
            if (r == "") result = r
            else result = "${result}${r})"
		}
	}
	return result
}

def getParams(cpTypes){
	def result = ""
	def cpValue
	def badParam = false
	cpTypes.each{ cpType ->
		def i = cpType.key.replaceAll("cpType_","")
		def cpVal = settings.find{it.key == "cpVal_${i}"}
		if (cpVal){
			cpValue = cpVal.value
			if (cpType.value == "string"){
				result = result + "'${cpValue}'," 
			} else {
				if (cpValue.isNumber()){
					result = result + "${cpValue}," 
				} else {
					result = result + "[${cpValue}]: is not a number,"
				}
			}
		} else {
			badParam = true
		}
	}
	if (badParam) result = ""
	else result = result[0..-2]   
	return result
}

def parameterLabel(){
	def howMany = (state.howManyP ?: 1) - 1
	def result = ""
	if (howMany) {
		for (int i = 1; i <= howMany; i++) {
			result = result + parameterLabelN(i) + "\n"
		}
		result = result[0..-2]
	}
	return result
}

def parameterLabelN(i){
	def result = ""
	def cpType = settings.find{it.key == "cpType_${i}"}
	def cpVal = settings.find{it.key == "cpVal_${i}"}
	def cpValue
	if (cpVal) cpValue = cpVal.value
	else cpValue = "missing value"
	if (cpType){
		result = "p${i} - type:${cpType.value}, value:${cpValue}"
	} 
	return result
}

def getParamsAsList(cpTypes){
	def result = []
	cpTypes.each{ cpType ->
		def i = cpType.key.replaceAll("cpType_","")
		def cpVal = settings.find{it.key == "cpVal_${i}"}
		if (cpVal){
			if (cpType.value == "string"){
				result << "${cpVal.value}" 
			} else if (cpType.value == "decimal"){
				result << cpVal.value.toBigDecimal()
			} else { // if (cpType.value == "number" && cpVal.value.isInteger()){
				result << cpVal.value.toInteger() 
			}
		} else {
        	result << "missing value"
        }
	}
	return result
}

def getCommands(){
	def result = [] 
	def cmdMaps = state.customCommands ?: []
	cmdMaps.each{ cmd ->
		def option = [(cmd.key):([cmd.value.text, cmd.value.capab == null ? "actuator" : cmd.value.capab])]
		result.push(option)
	}
	return result
}

def getMyCommands(){
	def result = [] 
	def cmdMaps = state.customCommands ?: []
	cmdMaps.each{ cmd ->
		def option = [(cmd.key):(cmd.value.text)]
		result.push(option)
	}
	return result
}

def isValidCommand(cmdIDS){
	def result = false
	cmdIDS.each{ cmdID ->
		def cmd = state.customCommands["${cmdID}"]
		if (cmd) result = true
	}
	return result
}

def deleteCommands(){
	def result
	def cmdMaps = state.customCommands
	if (deleteCmds.size() == 1) result = "Command removed"
	else result = "Commands removed"
	deleteCmds.each{ it -> 
		cmdMaps.remove(it)
	}
	return result
}
def commandExists(cmd){
	def result = false
	if (state.customCommands){
		result = state.customCommands.find{ it.value.text == "${cmd}" }
	}
	return result
}
def addCommand(){
	def capabs = [	"Acceleration" : 				"accelerationSensor", 
    				"Button" : 						"button",
    				"Carbon monoxide detector" :	"carbonMonoxideDetector", 
                    "Contact" : 					"contactSensor", 
                    "Dimmer" : 						"switchLevel",
                    "Door" : 						"doorControl", 
                    "Energy meter" : 				"energyMeter", 
                    "Garage door" : 				"garageDoorControl", 
                    "Humidity" : 					"humiditySensor", 
                    "Illuminance" : 				"illuminanceSensor", 
    				"Lock" : 						"lock", 
                    "Motion" : 						"motionSensor", 
                    "Power meter" : 				"powerMeter", 
                    "Presence" : 					"presenceSensor", 
                    "Smoke detector" : 				"smokeDetector", 
                    "Switch" : 						"switch", 
                    "Temperature" : 				"temperatureMeasurement", 
                    "Thermostat" : 					"thermostat",
        			"Water sensor" : 				"waterSensor", 
                    "Music player" : 				"musicPlayer", 
                    "Actuator" : 					"actuator"]
	def result
	def newCmd = getCmdLabel()
	def found = commandExists(newCmd)
	def cmdMaps = state.customCommands
	//only update if not found...
	if (!found) {
		state.lastCmdIDX = state.lastCmdIDX + 1
		def nextIDX = state.lastCmdIDX
		def cmd = [text:"${newCmd}",cmd:"${cCmd}"]
		def params = [:]
		def cpTypes = settings.findAll{it.key.startsWith("cpType_")}.sort()
		cpTypes.each{ cpType ->
			def i = cpType.key.replaceAll("cpType_","")
			def cpVal = settings.find{it.key == "cpVal_${i}"}
			def param = ["type":"${cpType.value}","value":"${cpVal.value}"]
			params.put(i, param)
		}	
		cmd.put("params",params)
        if(myCapab) cmd.put("capab", capabs[myCapab]) else cmd.put("capab", "actuator")
		if (cmdMaps) cmdMaps.put((nextIDX),cmd)
		else state.customCommands = [(nextIDX):cmd]
		result = "command: ${newCmd} was added"
	} else {
		result = "command: ${newCmd} was not added, it already exists."
	}
	return result
}

def execTestCommand(){
	def result
	def cTypes = settings.findAll{it.key.startsWith("cpType_")}
	def p = getParamsAsList(cTypes.sort()) as Object[]
	devices.each { device ->
		try {
			device."${cCmd}"(p)
			result = "succeeded"
		}
		catch (IllegalArgumentException e){
			def em = e as String
			def ems = em.split(":")
			ems = ems[2].replace(" [","").replace("]","")
			ems = ems.replaceAll(", ","\n")
//			result = "failed, valid commands:\n${ems}"
			result = "failed"
		}
		catch (e){
			result = "failed with:\n${e}"
		}
	}
	return result
}

def execCommand(cmdID){
	def result = ""
	def pList = []
	if (cmdID){
		def cmdMap = state.customCommands["${cmdID}"] 
		if (testCmd && cmdMap) {
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
					result = "Command succeeded"
				}
				catch (IllegalArgumentException e){
					def em = e as String
					def ems = em.split(":")
					ems = ems[2].replace(" [","").replace("]","")
					ems = ems.replaceAll(", ","\n")
					result = "Command failed, valid commands:\n${ems}"
				}
				catch (e){
					result = "failed with:\n${e}"
				}
			}
			return result
		}
	}
}

def getDeviceCommands(){
	def result = ""
	devices.each { device ->
        result = device.supportedCommands.collect{ it as String }
        //log.debug "supportedCommands:${result}"
	}
	return result
}

def isExpert(){
	return getCommands().size() > 0
}