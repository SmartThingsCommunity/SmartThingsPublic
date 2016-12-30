/**
 *  Turn It Off When Not in Use
 *
 *  Copyright 2014 Sidney Johnson
 *
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
 *	Version: 1.1 - Added the ability to turn off a differnt switch than the one being monitored
 *	Version: 1.2 - Code clean up and better logging\scheduling. Turn on time no longer required
 *	Version: 1.3 - Polls device for latest info
 *
 */
definition(
    name: "Turn It Off When Not in Use",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Turns off device when wattage drops below a set level after a set time. Retires every 10min",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home30-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png")

preferences {
	section("About") {
		paragraph "Turn It Off When Not in Use, Turns off device when wattage drops below a set level after a set time. Retires every 10min."
		paragraph "${textVersion()}\n${textCopyright()}"
	}
	section("Select switch to monitor power...") {
		input name: "switch1", type: "capability.powerMeter", multiple: false, required: true
	}
    section("Select switch to turn off...") {
		input name: "switch2", type: "capability.switch", multiple: false, required: true
	}
    section("Turn them off at...") {
		input name: "stopTime", type: "time", multiple: false, required: true
	}
    section("When wattage drops below...") {
		input name: "wattageLow", type: "number", multiple: false, required: true, defaultValue: "90"
	}    
	section("Turn them on at...") {
		input name: "startTime", type: "time", multiple: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	log.info "Turn It Off When Not in Use ${textVersion()} ${textCopyright()}"
	schedule(stopTime, "stopTimerCallback")

}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.info "Turn It Off When Not in Use ${textVersion()} ${textCopyright()}"
	unschedule()
    unsubscribe()
	schedule(stopTime, "stopTimerCallback")
}

def startTimerCallback() {
	log.debug "Turning on switches"
	switch2.on()
    unschedule(startTimerCallback)
}

def stopTimerCallback() {
	switch1.poll()
    pause(2000)
    if (switch1.currentPower <= wattageLow) {
	    log.debug "${switch1.displayName} current Wattage: ${switch1.currentPower}, turning off ${switch2.displayName}."
		sendEvent(linkText:app.label, name:"${switch1.displayName}", value:"${switch1.currentPower}", descriptionText:"${switch1.displayName} current Wattage: ${switch1.currentPower}, turning off ${switch2.displayName}.", eventType:"SOLUTION_EVENT", displayed: true)
		switch2.off()
	    if (startTime != null) {
		    schedule(startTime, "startTimerCallback")
		}
	runIn(87000, stopTimerRescheduler, [overwrite: true])
	}
    else {
	log.debug "${switch1.displayName} current Wattage: ${switch1.currentPower}, waiting for next poll cycle."
    sendEvent(linkText:app.label, name:"${switch1.displayName}", value:"${switch1.currentPower}", descriptionText:"${switch1.displayName} current Wattage: ${switch1.currentPower}, waiting for next poll cycle.", eventType:"SOLUTION_EVENT", displayed: true)
    def timeDelay = 598
	runIn(timeDelay, stopTimerCallback, [overwrite: true])
    }
}

def stopTimerRescheduler() {
	unschedule("stopTimerCallback")
    stopTimerCallback()
    schedule(stopTime, "stopTimerCallback")
	sendEvent(linkText:app.label, name:"stopTimerRescheduler", value:"rescheduling stopTimerCallback", descriptionText:"stopTimerRescheduler is rescheduling stopTimerCallback", eventType:"SOLUTION_EVENT", displayed: true)
}

private def textVersion() {
    def text = "Version 1.3"
}

private def textCopyright() {
    def text = "Copyright © 2014 Sidjohn1"
}