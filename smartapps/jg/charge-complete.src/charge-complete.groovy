/**
 *  Charge Complete
 *
 *  Copyright 2017 JG
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
    name: "Charge Complete",
    namespace: "JG",
    author: "JG",
		description: "Turn things off if charging is complete.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section {
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "threshold", type: "number", title: "Reports Below...(Default 1 Watt)", required: false, description: "in either watts or kw.")
        input (name: "delay", type: "number", title: "Delay Before Turning Off...(Default 5 Mins)", required: false, description: "delay in minutes.")
    }
    section {
    	input(name: "switches", type: "capability.switch", title: "Turn Off These Switches", required: true, multiple: true, description: null)
    }
    section {
        input("recipients", "contact", title: "Send notifications to") {
            input(name: "sms", type: "phone", title: "Send A Text To", description: null, required: false)
            input(name: "pushNotification", type: "bool", title: "Send a push notification", description: null, defaultValue: true)
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
	subscribe(meter, "power", meterHandler)
}

def meterHandler(evt) {
    def meterValue = evt.value as double
    //Give user option to input custom value.  If not, default to 1 Watt.
    def thresholdValue = (threshold != null && threshold != "") ? threshold : 1
    //Give user option to input custom value.  If not, default to 5 Minutes.
    def delayValue = (delay != null && delay != "") ? delay * 60 : 300
    if (meterValue < thresholdValue) {
	    log.debug "${meter} reported energy consumption below ${thresholdValue} Watt(s). Turning of switches in ${delayValue/60} minute(s)."
        //Give 5 (default) minutes for status to update.  In some cases, issues arrise from slow to report/update values if a delay isn't given.
        runIn(delayValue, scheduledHandler)
    }else {
    	//Check for updated values during delay time.  If found, cancel switch off command.
    	log.debug "${meter} - Charging is still in progress (${meterValue} W). Cancelling Switch Power Off (if schedule)."
        unschedule(scheduledHandler)
    }
}

def scheduledHandler() {
	log.debug "Charge Complete - SmartApp: scheduledHandler executed at ${new Date()}"    
	log.debug "Turning off ${meter}"
    sendMessage()
    switches.off()
}

void sendMessage() {
  def msg = "Charging Complete for ${meter}."
  log.info msg
  if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sms) {
            sendSms(sms, msg)
        }
        if (pushNotification) {
            sendPush(msg)
        }
    }
}