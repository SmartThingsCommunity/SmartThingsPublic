/**
 *  Energy Saver
 *
 *  Copyright 2014 SmartThings
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
    name: "Energy Alerts",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Get notified if you're using too much energy",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png",
    pausable: true
)

preferences {
	section {
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "aboveThreshold", type: "number", title: "Reports Above...", required: true, description: "in either watts or kw.")
        input(name: "belowThreshold", type: "number", title: "Or Reports Below...", required: true, description: "in either watts or kw.")
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

    if (!atomicState.lastValue) {
    	atomicState.lastValue = meterValue
    }

    def lastValue = atomicState.lastValue as double
    atomicState.lastValue = meterValue

    def dUnit = evt.unit ?: "Watts"

    def aboveThresholdValue = aboveThreshold as int
    if (meterValue > aboveThresholdValue) {
    	if (lastValue < aboveThresholdValue) { // only send notifications when crossing the threshold
		    def msg = "${meter} reported ${evt.value} ${dUnit} which is above your threshold of ${aboveThreshold}."
    	    sendMessage(msg)
        } else {
//        	log.debug "not sending notification for ${evt.description} because the threshold (${aboveThreshold}) has already been crossed"
        }
    }


    def belowThresholdValue = belowThreshold as int
    if (meterValue < belowThresholdValue) {
    	if (lastValue > belowThresholdValue) { // only send notifications when crossing the threshold
		    def msg = "${meter} reported ${evt.value} ${dUnit} which is below your threshold of ${belowThreshold}."
    	    sendMessage(msg)
        } else {
//        	log.debug "not sending notification for ${evt.description} because the threshold (${belowThreshold}) has already been crossed"
        }
    }
}

def sendMessage(msg) {
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
