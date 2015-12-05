/**
 *  Rule Machine
 *
 *  Copyright 2015 Bruce Ravenel and Mike Maxwell
 *
 *  Version 1.1   5 Dec 2015
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
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Rules and Triggers", install: true, uninstall: false, submitOnChange: true) {
            section {
                    app(name: "childRules", appName: "Rule", namespace: "bravenel", title: "Create New Rule...", multiple: true)
            }
            section {
                    app(name: "childTriggers", appName: "Trigr", namespace: "bravenel", title: "Create New Trigger...", multiple: true)
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
	if(!state.setup) {
		state.ruleState = [:]
    	state.ruleSubscribers = [:]
    }
    childApps.each {child ->
		if(child.name == "Rule") {
			log.info "Installed Rules: ${child.label}"
            if(!state.setup) {
				state.ruleState[child.label] = null
				state.ruleSubscribers[child.label] = [:]
            }
		}
    }
    childApps.each {child ->
            if(child.name == "Trigr") log.info "Installed Triggers: ${child.label}"
    }
    state.setup = true
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
//	log.debug "setRuleTruth1: $appLabel, $ruleTruth"
	if(!state.setup) initialize()
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
    if(state.ruleState[appLabel]) state.ruleState.remove(appLabel)
    if(state.ruleSubscribers[appLabel]) state.ruleSubscribers.remove(appLabel)
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

