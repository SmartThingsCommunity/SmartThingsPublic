/**
 *  Smart Switch Routine
 *
 *  Copyright 2015 John Lynch
 *  Version 1.00 9/4/15
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
 *  Ties a Hello, Home routine to a switch's (virtual or real) on/off state and presence sensor(s).
 *  Simply define a switch to be used, the presence(s) to monitor, then tie the state of the switch & presence to a specific Hello, Home routine.
 *
 *
 * 
 *        _.-"""-,
 *      .'  ..::. `\
 *     /  .::' `'` /
 *    / .::' .--.=;
 *    | ::' /  C ..\
 *    | :: |   \  _.)
 *     \ ':|   /  \
 *      '-, \./ \)\)
 *         `-|   );/
 *            '--'-'
 */
definition(
    name: "Smart Switch Routine",
    namespace: "Ayechaw",
    author: "John Lynch",
    description: "Directly connects a switch's state and presences to a routine.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/Ayechaw/SmartThings/master/SmartSwitchRoutine/smartswitch.png",
    iconX2Url: "https://raw.githubusercontent.com/Ayechaw/SmartThings/master/SmartSwitchRoutine/smartswitch.png",
    iconX3Url: "https://raw.githubusercontent.com/Ayechaw/SmartThings/master/SmartSwitchRoutine/smartswitch.png")


preferences {
	page(name: "MainSetup")
    page(name: "RoutineSetup")
    page(name: "OptionalSetup")
}

// Main setup screen for SmartApp
def MainSetup() {    
    dynamicPage(name: "MainSetup", title: "Smart Switch Routine", install:true, uninstall: true) {
    section("About") {
        paragraph "A SmartApp that directly connects a switch's state and your presence to a Routine." 
    }
    section("Choose a switch to use...") {
		input "controlSwitch", "capability.switch", title: "Switch", multiple: false, required: true
    }
    section("Whose presence to monitor...") {
    	input "people", "capability.presenceSensor", multiple: true, required: true
    }
    section {
    	href(name: "toRoutineSetupSetup", title: "Perform routines when...", page: "RoutineSetup") 
    }
    section {
    	href(name: "toOptionalSetup", title: "Optional Settings...", page: "OptionalSetup")
        }
    }
}

// Routine setup screen for SmartApp
def RoutineSetup() {
	dynamicPage(name: "RoutineSetup", nextPage: MainSetup) {
    	section("Routine Setup") {}
		def actions = location.helloHome?.getPhrases()*.label
		if (actions) {
        	actions.sort()
			section("Perform the following routines when...") {
				log.trace actions
				input "actiononhome", "enum", title: "Switch is on - You're Home", required: true, options: actions
                input "actiononaway", "enum", title: "Switch is on - You're Away", required: true, options: actions
				input "actionoffhome", "enum", title: "Switch is off - You're Home", required: true, options: actions
                input "actionoffaway", "enum", title: "Switch is off - You're Away", required: true, options: actions
			}
		}
      	section {
        	href(name: "toMainSetupSetup", title: "Back to Main Setup Page", page: "MainSetup")
     	}
	}
}

// Optional Settings setup screen for SmartApp
def OptionalSetup() {
	dynamicPage(name: "OptionalSetup", title: "Optional Settings", nextPage: MainSetup) {
    	section("Only perform if...") {
    		mode(name: "modeMultiple", title: "Only for specific mode(s)", required: false)
            }
	section ("Send to...") {
			input("recipients", "contact", title: "Send notifications to", required: false, multiple: true) {
        		input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
        		input "phone", "phone", title: "Send a text message?", description: "Phone Number", requried: false
            	}
            }
        section ("False Alarm Thresholds...(defaults to 10 min)") {
        	input "falseAlarmThresholdHome", "decimal", title: "I've arrived - False alarm threshold", required: false
            input "falseAlarmThresholdAway", "decimal", title: "I've left - False alarm threshold", required: false
            }
        section {
        	href(name: "toMainSetupSetup", title: "Back to Main Setup Page", page: "MainSetup")
   		}
    }
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(controlSwitch, "switch", switchHandler)
    subscribe(people, "presence", presence)
    log.debug "selected on action $actiononhome"
    log.debug "selected on action $actiononaway"
    log.debug "selected off action $actionoffhome"
    log.debug "selected off action $actionoffaway"
 	}

//States what to do if there is a Presence event change
def presence(evt) {
	log.debug "evt.name: $evt.value"
    if (evt.value == "not present") {
    	log.debug "checking if everyone is away"
        if (everyoneIsAway()) {
        	if (switchIsOn()) {
        		log.debug "starting Switch On - Away sequence"
            	runIn(findFalseAlarmThresholdAway() * 60, "takeActionOnAway")
            }
            else {
            	log.debug "starting Switch Off - Away sequence"
                runIn(findFalseAlarmThresholdAway() * 60, "takeActionOffAway")
            }
        }
        else {
        	log.debug "Not everyone is currently away. Aborting Away sequence"
		}
	}
    else if (evt.value == "present") {
     	if (switchIsOn()) {
       		log.debug "starting Switch On - Home sequence"
           	runIn(findFalseAlarmThresholdHome() * 60, "takeActionOnHome")
            }
        else {
           	log.debug "starting Switch Off - Home sequence"
            runIn(findFalseAlarmThresholdHome() * 60, "takeActionOffHome")
        }
	}
}
   

//States what to do if there is a Switch event change
def switchHandler(evt) {
	log.debug "evt.name: $evt.value"
	if (evt.value == "on") {
    	log.debug "switch turned on! Checking if anyone is home..."
        if (everyoneIsAway()) {
        	log.debug "starting Switch On - Away sequence"
            takeActionOnAway()
        }
        else {
        	log.debug "starting Switch On - Home sequence"
            takeActionOnHome()
        }
    } else if (evt.value == "off") {
    	log.debug "switch turned off! Checking if anyone is home..."
        if (everyoneIsAway()) {
        	log.debug "starting Switch Off - Away sequence"
            takeActionOffAway()
        }
        else {
        	log.debug "starting Switch Off - Home sequence"
            takeActionOffHome()
        }
    }
}

//What happens if Switch is On & Currently Home
def takeActionOnHome() {
	def message = "'${app.label}' changed your routine to '${settings.actiononhome}'"
    log.info message
    send(message)
	location.helloHome.execute(settings.actiononhome)
    }

//What happens if Switch is On & Currently Away
def takeActionOnAway() {
	def message = "'${app.label}' changed your routine to '${settings.actiononaway}'"
    log.info message
    send(message)
	location.helloHome.execute(settings.actiononaway)
    }

//What happens if Switch is Off & Currently Home
def takeActionOffHome() {
	def message = "'${app.label}' changed your routine to '${settings.actionoffhome}'"
    log.info message
    send(message)
	location.helloHome.execute(settings.actionoffhome)
    }
    
//What happens if Switch is Off & Currently Away
def takeActionOffAway() {
	def message = "'${app.label}' changed your routine to '${settings.actionoffaway}'"
    log.info message
    send(message)
	location.helloHome.execute(settings.actionoffaway)
    }
    
// returns true if all configured sensors are not present,
// false otherwise.
private everyoneIsAway() {
	def result = true
    for (person in people) {
    	if (person.currentPresence == "present") {
        	result = false
    	}
	}
    log.debug "everyoneIsAway: $result"
    return result
}

// gets the false alarm threshold, in minutes. Defaults to
// 10 minutes if the preference is not defined.
private findFalseAlarmThresholdAway() {
	(falseAlarmThresholdAway !=null && falseAlarmThresholdAway != "") ? falseAlarmThresholdAway : 10
}
private findFalseAlarmThresholdHome() {
	(falseAlarmThresholdHome !=null && falseAlarmThresholdHome !="") ? falseAlarmThresholdHome : 10
}

// returns true if switch is on,
// false otherwise.
private switchIsOn() {
	def result = true
    	if (controlSwitch.currentSwitch == "off") {
        	result = false
        }
    log.debug "switchIsOn: $result"
    return result
}

// sends notifications if enabled
private send(msg) {
	if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
		sendNotificationToContacts(msg, recipients)
	}
	else  {
		if (sendPushMessage != "No") {
			log.debug("sending push message")
			sendPush(msg)
		}

		if (phone) {
			log.debug("sending text message")
			sendSms(phone, msg)
		}
	}
	log.debug msg
}