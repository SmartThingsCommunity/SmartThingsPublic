/**
 *  Thinking Cleanerer Neato Botvac Edition
 *  Smartthings SmartApp
 *  Copyright 2014 Sidney Johnson
 *  If you like this app, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
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
 *	Version: 1.0 - Initial Version
 *	Version: 1.2 - Added error push notifcation, and better icons
 *	Version: 1.3 - New interface, better polling, and logging. Added sms notifcations
 *	Version: 1.4 - Added bin full notifcations
 *	Version: 1.4.1 - Fixed SMS send issue
 *	Version: 1.4.2 - Fixed No such property: currentSwitch issue, added poll on initialize, locked to single instance
 *	Version: 1.5 - More robust polling, auto set Smart Home Monitor
 *
 *	Modified by Alex Lee Yuk Cheung for Neato Botvac Devices
 *	Version: 1.0 - Initial Version - Added Auto Dock On Pause, Force Clean Option, Changes to Polling behaviour
 *	Version: 1.1 - Fix to setting SHM state when error has occured whilst cleaning
 */
 
definition(
 	name: "Thinking Cleanerer Neato Botvac Edition",
	namespace: "sidjohn1",
	author: "Sidney Johnson",
	description: "Handles polling and job notification for Neato Botvac",
	category: "Convenience",
	iconUrl: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato-icons_1x.png",
	iconX2Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato-icons_1x.png",
	iconX3Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato-icons_1x.png",
	singleInstance: true)

preferences {
	page name:"pageInfo"
}

def pageInfo() {
	return dynamicPage(name: "pageInfo", title: "Thinking Cleanerer Neato Botvac Edition", install: true, uninstall: true) {    
		section("About") {
			paragraph "Thinking Cleaner(Botvac) smartapp for Smartthings. This app monitors your Botvac and provides job notifacation"
			paragraph "${textVersion()}\n${textCopyright()}"    
		}
    	section("Select Botvac(s) to monitor..."){
    		input "switch1", "device.NeatoBotvacConnected", title: "Monitored Botvac", required: true, multiple: true, submitOnChange: true
		}
		def roombaList = ""
    	settings.switch1.each() {
			try {
				roombaList += "$it.displayName is $it.currentStatus. Battery is $it.currentBattery%\n"
			}
        	catch (e) {
            	log.trace "Error checking status."
            	log.trace e
        	}
    	}
		if (roombaList) {
			section("Botvac Status:") {
				paragraph roombaList.trim()
			}
		}
        section("Preferences"){
        	input "forceClean", "bool", title: "Force clean after elapsed time?", required: false, defaultValue: false, submitOnChange: true
            if (forceClean) {
        		input ("forceCleanDelay", "number", title: "Number of days before force clean (in days)", required: false, defaultValue: 7)
            }
			input "autoDock", "bool", title: "Auto dock Botvac after pause?", required: false, defaultValue: true, submitOnChange: true
            if (autoDock) {
            	input ("autoDockDelay", "number", title: "Auto dock delay after pause (in seconds)", required: false, defaultValue: 60)
            }
		}
		section(hideable: true, hidden: true, "Auto Smart Home Monitor..."){
			input "autoSHM", "bool", title: "Auto Set Smart Home Monitor?", required: true, multiple: true, defaultValue: false, submitOnChange: true
			paragraph"Auto Set Smart Home Monitor to Arm(Stay) when cleaning and Arm(Away) when done."
		}
		section(hideable: true, hidden: true, "Event Notifications..."){
			input "sendPush", "bool", title: "Send as Push?", required: false, defaultValue: true
			input "sendSMS", "phone", title: "Send as SMS?", required: false, defaultValue: null
			input "sendRoombaOn", "bool", title: "Notify when on?", required: false, defaultValue: false
			input "sendRoombaOff", "bool", title: "Notify when off?", required: false, defaultValue: false
			input "sendRoombaError", "bool", title: "Notify on error?", required: false, defaultValue: true
			input "sendRoombaBin", "bool", title: "Notify on full bin?", required: false, defaultValue: true
		}
    }
}

def installed() {
	log.trace "Installed with settings: ${settings}"
    runEvery5Minutes('pollOn')
    state.lastClean = [:]
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
    runEvery5Minutes('pollOn')
	initialize()
}

def initialize() {
	log.info "Thinking Cleanerer Neato Botvac Edition ${textVersion()} ${textCopyright()}"
	subscribe(switch1, "status.cleaning", eventHandler)
	subscribe(switch1, "status.ready", eventHandler)
	subscribe(switch1, "status.error", eventHandler)
    subscribe(switch1, "status.paused", eventHandler)
	subscribe(switch1, "bin.full", eventHandler)
}

def uninstalled() {
    unschedule()
}

def eventHandler(evt) {
	def msg
    if (evt.value == "paused") {
    log.trace "Setting auto dock for ${evt.displayName}"
    	//If configured, set to dock automatically after one minute.
        if (autoDock == true) {
        	runIn(autoDockDelay, scheduleAutoDock)
        }
    }
	else if (evt.value == "error") {
    	unschedule()
        runEvery5Minutes('pollOn')
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"error",descriptionText:"${evt.displayName} has an error", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} has an error"
		msg = "${evt.displayName} has an error"
		if (sendRoombaError == true) {
        	if (settings.sendSMS != null) {
				sendSms(sendSMS, msg) 
			}
			if (settings.sendPush == true) {
				sendPush(msg)
			}
		}
     }
	 else if (evt.value == "cleaning") {
     	unschedule()
        //Increase poll interval during cleaning
        schedule("0 0/1 * * * ?", pollOn)
        if (state.lastClean == null) {
        	state.lastClean = [:]
        }
        //Record last cleaning time for device
        state.lastClean[evt.displayName] = now()
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"on",descriptionText:"${evt.displayName} is on", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} is on"
		msg = "${evt.displayName} is on"
		if (sendRoombaOn == true) {
			if (settings.sendSMS != null) {
				sendSms(sendSMS, msg) 
			}
			if (settings.sendPush == true) {
				sendPush(msg)
			}
		}
        if (settings.autoSHM.contains('true') ) {
        	if (location.currentState("alarmSystemStatus")?.value == "away") {
				sendEvent(linkText:app.label, name:"Smart Home Monitor", value:"stay",descriptionText:"Smart Home Monitor was set to stay", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "Smart Home Monitor is set to stay"
				sendLocationEvent(name: "alarmSystemStatus", value: "stay")
				state.autoSHMchange = "y"
                sendPush("Smart Home Monitor is set to stay as ${evt.displayName} is on")
            }
        }
     }
	 else if (evt.value == "full") {
     	unschedule('pollOn')
        runEvery5Minutes('pollOn')
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"bin full",descriptionText:"${evt.displayName} bin is full", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} bin is full"
		msg = "${evt.displayName} bin is full"
		if (sendRoombaBin == true) {
			if (settings.sendSMS != null) {
				sendSms(sendSMS, msg) 
			}
			if (settings.sendPush == true) {
				sendPush(msg)
			}
		}
	 }
     else if (evt.value == "ready") {
     	unschedule()
        runEvery5Minutes('pollOn')
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"off",descriptionText:"${evt.displayName} is off", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} is off"
		msg = "${evt.displayName} is off"
		if (sendRoombaOff == true) {
			if (settings.sendSMS != null) {
				sendSms(sendSMS, msg) 
			}
			if (settings.sendPush == true) {
				sendPush(msg)
			}
		}
	}
}

def scheduleAutoDock() {
	settings.switch1.each() {
		if (it.latestState('status').stringValue == 'paused') {
			it.dock()
		}
	}
}

def pollOn() {
	log.debug "Executing 'pollOn'"
	settings.switch1.each() {
    	state.pollState = now()
		it.poll()
        //Force on if last clean was a long time ago
        if (it.currentSwitch == "off" && forceClean && state.lastClean != null && state.lastClean[it.displayName] != null) {
        	def t = now() - state.lastClean[it.displayName]
            log.debug "$it.displayName last cleaned at " + state.lastClean[it.displayName] + ". $t milliseconds has elapsed since."
			if (t > (forceCleanDelay * 86400000)) {
            	log.debug "Force clean activated as $t milliseconds has elapsed"
				sendPush(it.displayName + " has not cleaned for " + forceCleanDelay + " days. Forcing a clean.")
                it.on()
        	}
        }
	}
    
    def activeCleaners = false
    
    for (cleaner in switch1) {
    	if (cleaner.latestState('status').stringValue == 'cleaning') {
        	activeCleaners = true
        }
    }
    
	if (!activeCleaners) {
		if (settings.autoSHM.contains('true') ) {
			if (location.currentState("alarmSystemStatus")?.value == "stay" && state.autoSHMchange == "y"){
				sendEvent(linkText:app.label, name:"Smart Home Monitor", value:"away",descriptionText:"Smart Home Monitor was set back to away", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "Smart Home Monitor is set back to away"
				sendLocationEvent(name: "alarmSystemStatus", value: "away")
				state.autoSHMchange = "n"
                sendPush("Smart Home Monitor is set to away as all cleaners are off")
			}
		}
	}
    
    //If SHM is disarmed because of external event, then disable auto SHM mode
    if (location.currentState("alarmSystemStatus")?.value == "off") {
    	state.autoSHMchange = "n"
    }
}

private def textVersion() {
    def text = "Version 1.5"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1. Modified by Alex Lee Yuk Cheung"
}