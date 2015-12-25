/**
 *  Rule Machine
 *
 *  Copyright 2015 Bruce Ravenel and Mike Maxwell
 *
 *  Version 1.6.2   25 Dec 2015
 *
 *	Version History
 *	
 *  1.6.2	25 Dec 2015		null parameter value patch in expert, maxwell
 *	1.6.1	24 Dec 2015		UI improvement
 *	1.6		23 Dec 2015		Added expert commands per Mike Maxwell
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
    name: "Rule Machine",
    singleInstance: true,
    namespace: "bravenel",
    author: "Bruce Ravenel and Mike Maxwell",
    description: "Rule Machine",
    category: "My Apps",
  	iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@3x.png"
)

preferences {
	page(name: "mainPage")
    page(name: "removePage")
	//expert pages
	page(name: "expert")
	page(name: "generalApprovalPAGE")
	page(name: "customCommandsPAGE")
	page(name: "addCustomCommandPAGE")
	page(name: "customParamsPAGE")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Rules and Triggers", install: true, uninstall: false, submitOnChange: true) {
    	if(!state.setup) initialize(true)
        section {
            app(name: "childRules", appName: "Rule", namespace: "bravenel", title: "Create New Rule...", multiple: true)
        }
		section {
			href( "expert", title: "", description: "Expert Features", state: "")
        }
        section {
        	href "removePage", description: "Remove Rule Machine", title: ""
        }
    }
}

def removePage() {
	dynamicPage(name: "removePage", title: "Remove Rule Machine", install: false, uninstall: true) {
    	section ("WARNING! Removing Rule Machine also removes all Rules") {
        }
    }
}

def installed() {
    if(!state.setup) initialize(true) else initialize(false)
}

def updated() {
    initialize(false)
}

def initialize(first) {
	if(first) {
		state.ruleState = [:]
    	state.ruleSubscribers = [:]
    }
    childApps.each {child ->
		if(child.name == "Rule") {
			log.info "Installed Rules and Triggers: ${child.label}"
            if(first) {
				state.ruleState[child.label] = null
				state.ruleSubscribers[child.label] = [:]
            }
		} 
    }
    state.setup = true
}

def ruleList(appLabel) {
	def result = []
    childApps.each { child ->
    	if(child.name == "Rule" && child.label != appLabel && state.ruleState[child.label] != null) result << child.label
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
//	log.debug "setRuleTruth1: $appLabel, $ruleTruth"
    state.ruleState[appLabel] = ruleTruth
    def thisList = state.ruleSubscribers[appLabel]
    thisList.each {
        if(it.value == null || "$it.value" == "$ruleTruth") {
    		childApps.each { child ->
    			if(child.label == it.key) child.ruleHandler(appLabel, ruleTruth)
    		}
        }
    }
}

def currentRule(appLabel) {
//	log.debug "currentRule: $appLabel, ${state.ruleState[appLabel]}"
	def result = state.ruleState[appLabel]
}

def childUninstalled() {
//	log.debug "childUninstalled called"
}

def removeChild(appLabel) {
//	log.debug "removeChild: $appLabel"
    unSubscribeRule(appLabel)
    if(state.ruleState[appLabel] != null) state.ruleState.remove(appLabel)
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

/*****custom command specific pages	*****/
def expert(){
	dynamicPage(name: "expert", title: "Expert Features", uninstall: false, install: false) {
		section(){
			paragraph 	"Custom commands allows Rules to control devices with custom capabilities.\n" +
						"Dual dimmers and switches, ThingShields, FGBW controllers or any device you might build a " +
						"custom smartApp to utilize.\n" +
						"Custom commands that are created and saved here will become available for use in any new " +
						"or existing rules.\n" +
						"After saving at least one command, look for 'Run custom device command' in your 'Select " +
						"Actions'  sections."
			//expert hrefs...
			href( "customCommandsPAGE"
				,title		: "Configure Custom Commands..."
				,description: ""
				,state		: anyCustom()
			)
		}
	}
}

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
	def savedCommands = getCommands()
	dynamicPage(name: "customCommandsPAGE", title: "Custom Commands", uninstall: false, install: false) {
		section(){
			input(
				name			: "devices"
				,title			: "Test device"
				,multiple		: false
				,required		: false
				,type			: "capability.actuator"
				,submitOnChange	: true
			)
			if (settings.devices && savedCommands.size() != 0){
				input(
					name			: "testCmd"
					,title			: "Select saved command to test"
					,multiple		: false
					,required		: false
					,type			: "enum"
					,options		: savedCommands
					,submitOnChange	: true
				)
			}
		}
		def result = execCommand(settings.testCmd)
		if (result) {
			section("${result}"){
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
		if (getCommands()){
			section(){
				input(
					name			: "deleteCmds"
					,title			: "Delete custom commands..."
					,multiple		: true
					,required		: false
                    ,description	: ""
					,type			: "enum"
					,options		: getCommands()
					,submitOnChange	: true
				)
				if (isValidCommand(deleteCmds)){
					href( "generalApprovalPAGE"
						,title			: "Delete command(s) now"
						,description	: ""
						,state			: null
						,params			: [method:"deleteCommands",title:"Delete Command"]
						,submitOnChange	: true
					)
				}
			}
		}
	}
}

def addCustomCommandPAGE(){
	def cmdLabel = getCmdLabel()
	def complete = "" 
	def test = false
	def pageTitle = "Create new custom command for:\n${devices}" 
	if (cmdLabel){
		complete = "complete"
		test = true
	}
	dynamicPage(name: "addCustomCommandPAGE", title: pageTitle, uninstall: false, install: false) {
		section(){
			input(
		   		name			: "cCmd"
				,title			: "Available device commands"
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
		if (test){
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
			} else {
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
	if (deleteCmds.size == 1) result = "Command removed"
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
			result = "failed, valid commands:\n${ems}"
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
		try {
			device."xxx"()
			result = "Command succeeded"
		}
		catch (IllegalArgumentException e){
			def em = e as String
			def ems = em.split(":")
			ems = ems[2].replace(" [","").replace("]","")
			result = ems.split(", ").collect{it as String}
		}
	}
	return result
}

def isExpert(){
	return getCommands()
}