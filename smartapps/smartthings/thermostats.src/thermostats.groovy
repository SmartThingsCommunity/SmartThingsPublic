/**
 *  Copyright 2017 SmartThings
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
 *  Thermostats
 *
 *  Author: Juan Pablo Risso
 *  Date: 2017-12-05
 *
 */

definition(
		name: "Thermostats",
		namespace: "smartthings",
		author: "SmartThings",
		description: "Receive notifications when anything happens in your home.",
		category: "SmartSolutions",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@2x.png",
		singleInstance: true
)

preferences {
	section("Choose one or more, when..."){
		input "smokeDevices", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "carbonMonoxideDevices", "capability.carbonMonoxideDetector", title: "Carbon Monoxide Detected", required: false, multiple: true
	}
	section("Turn off these thermostats"){
		input "thermostatDevices", "capability.thermostat", title: "Thermostats", required: true, multiple: true
	}
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Enter a phone number to get SMS", required: false
			paragraph "If outside the US please make sure to enter the proper country code"
			input "pushAndPhone", "enum", title: "Notify me via Push Notification", required: false, options: ["Yes", "No"]
		}
	}
	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(smokeDevices, "smoke.detected", eventHandler)
	subscribe(smokeDevices, "smoke.tested", eventHandler)
	subscribe(smokeDevices, "carbonMonoxide.detected", eventHandler)
 	subscribe(carbonMonoxideDevices, "carbonMonoxide.detected", eventHandler)
}

def eventHandler(evt) {
	log.debug "Notify got evt ${evt}"
	// Turn off thermostat
	thermostatDevices*.setThermostatMode("off")
	if (frequency) {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			sendMessage(evt)
		}
	}
	else {
		sendMessage(evt)
	}
}

private sendMessage(evt) {
	String msg = messageText
	Map options = [:]

	if (!messageText) {
		msg = '{{ triggerEvent.descriptionText }}'
		options = [translatable: true, triggerEvent: evt]
	}
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients, options)
	} else {
		if (phone) {
			options.phone = phone
			if (pushAndPhone != 'No') {
				log.debug 'Sending push and SMS'
				options.method = 'both'
			} else {
				log.debug 'Sending SMS'
				options.method = 'phone'
			}
		} else if (pushAndPhone != 'No') {
			log.debug 'Sending push'
			options.method = 'push'
		} else {
			log.debug 'Sending nothing'
			options.method = 'none'
		}
		sendNotification(msg, options)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}
