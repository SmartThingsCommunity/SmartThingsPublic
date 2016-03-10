/**
 *  Copyright 2015 SmartThings
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
 *  Notify Me When
 *
 *  Author: SmartThings
 *  Date: 2013-03-20
 *
 * Change Log:
 *	1. Todd Wackford
 *	2014-10-03:	Added capability.button device picker and button.pushed event subscription. For Doorbell.
 */
definition(
		name: "Notify Me When",
		namespace: "smartthings",
		author: "SmartThings",
		description: "Receive notifications when anything happens in your home.",
		category: "Convenience",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("Choose one or more, when..."){
		input "button", "capability.button", title: "Button Pushed", required: false, multiple: true //tw
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
		input "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
	}
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
			paragraph "If outside the US please make sure to enter the proper country code"
			input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes", "No"]
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
	subscribe(button, "button.pushed", eventHandler) //tw
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
}

def eventHandler(evt) {
	log.debug "Notify got evt ${evt}"
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
		msg = defaultText(evt)
		options = [translatable: true, triggerEvent: evt]
	}
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients, options)
	} else {
		if (!phone || pushAndPhone != 'No') {
			log.debug 'sending push'
			options.method = 'push'
			//sendPush(msg)
		}
		if (phone) {
			options.phone = phone
			log.debug 'sending SMS'
			//sendSms(phone, msg)
		}
		sendNotification(msg, options)
	}

	if (frequency) {
		state[evt.deviceId] = now()
	}
}

private defaultText(evt) {
	if (evt.name == 'presence') {
		if (evt.value == 'present') {
			if (includeArticle) {
				'{{ triggerEvent.linkText }} has arrived at the {{ location.name }}'
			}
			else {
				'{{ triggerEvent.linkText }} has arrived at {{ location.name }}'
			}
		} else {
			if (includeArticle) {
				'{{ triggerEvent.linkText }} has left the {{ location.name }}'
			}
			else {
				'{{ triggerEvent.linkText }} has left {{ location.name }}'
			}
		}
	} else {
		'{{ triggerEvent.descriptionText }}'
	}
}

private getIncludeArticle() {
	def name = location.name.toLowerCase()
	def segs = name.split(" ")
	!(["work","home"].contains(name) || (segs.size() > 1 && (["the","my","a","an"].contains(segs[0]) || segs[0].endsWith("'s"))))
}
