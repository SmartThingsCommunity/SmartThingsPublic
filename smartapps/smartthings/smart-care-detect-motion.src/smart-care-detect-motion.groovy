/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Smart Care - Detect Motion
 *
 *  Author: SmartThings
 *  Date: 2013-04-07
 *
 */

definition(
	name: "Smart Care - Detect Motion",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Monitors motion sensors in bedroom and bathroom during the night and detects if occupant does not return from the bathroom after a specified period of time.",
	category: "Family",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/calendar_contact-accelerometer.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/calendar_contact-accelerometer@2x.png",
	pausable: true
)
preferences {
	page(name: "configuration", title:"", content: "disclaimerPage", install: true, uninstall: true)
}

def disclaimerPage() {
	def disclaimerText = "SMARTTHINGS INC. SMART CARE SUPPLEMENTAL TERMS AND DISCLAIMER\n" +
			"SmartThings Inc. is not an emergency medical response service of any kind and does not provide " +
			"medical or health-related advice, which should be obtained from qualified medical personnel. " +
			"SmartThings Inc., the contents of the app (such as text, graphics, images, videos, data and "+
			"information contained therein) and such materials obtained from third parties are provided for " +
			"information purposes only and are not substitutes for professional medical advice, diagnosis, " +
			"examination, or treatment by a health care provider. If you think you or a loved one has a medical " +
			"emergency, call your doctor or 911 immediately. Do not rely on electronic communications or " +
			"communication through this app for immediate, urgent medical needs. " +
			"THIS APP IS NOT DESIGNED TO FACILITATE OR AID IN MEDICAL EMERGENCIES.\n\n"+ 
			"If you have any concerns or questions about your health or the health of a loved one, " +
			"you should always consult with a physician or other health care professional." +
			"You understand and acknowledge that all users of this app are responsible for their own medical care, " +
			"treatment, and oversight. You also understand and acknowledge that you should never disregard, " +
			"avoid, or delay obtaining medical or health-related advice " +
			"relating to treatment or standard of care because of information contained in or transmitted through the app. "+
			"RELIANCE ON ANY INFORMATION PROVIDED BY THE APP OR OTHER THIRD-PARTY PLATFORMS IS SOLELY AT YOUR OWN RISK.\n\n" + 
			"While SmartThings Inc. strives to make the information on the app as timely and accurate as possible, " + 
			"SmartThings Inc. makes no claims, promises, or guarantees about the accuracy, completeness, " + 
			"or adequacy of the content or information on the app. SmartThings Inc. expressly disclaims liability for any errors "+
			"and omissions in content or for the availability of content on the app. " +
			"SmartThings Inc. will not be liable for any losses, injuries, or damages arising from the display " +
			"or use of content on the app. SMARTTHINGS INC., ITS OFFICERS, " +
			"EMPLOYEES AND AGENTS DO NOT ACCEPT LIABILITY HOWEVER ARISING, INCLUDING LIABILITY FOR NEGLIGENCE, " +
			"FOR ANY LOSS RESULTING FROM THE USE OF OR RELIANCE UPON THE INFORMATION AND/OR SERVICES AT ANY TIME."

	if (disclaimerResponse && disclaimerResponse == "I agree to these terms") {
		configurationPage()
	} else {
		dynamicPage(name: "configuration") {
			section(disclaimerText){
				input "disclaimerResponse", "enum", title: "Accept terms", required: true,
						options: ["I agree to these terms", "I do not agree to these terms"],
						submitOnChange: true
			}
		}
	}
}

def configurationPage(){
	dynamicPage(name: "configuration") {
		section("Bedroom motion detector(s)") {
			input "bedroomMotion", "capability.motionSensor", multiple: true
		}
		section("Bathroom motion detector") {
			input "bathroomMotion", "capability.motionSensor"
		}
		section("Active between these times") {
			input "startTime", "time", title: "Start Time"
			input "stopTime", "time", title: "Stop Time"
		}
		section("Send message when no return within specified time period") {
			input "warnMessage", "text", title: "Warning Message"
			input "threshold", "number", title: "Minutes"
		}
		section("To these contacts") {
		input("recipients", "contact", title: "Recipients", description: "Send notifications to") {
				input "phone1", "phone", required: false
				input "phone2", "phone", required: false
				input "phone3", "phone", required: false
			}
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
	state.active = 0
	subscribe(bedroomMotion, "motion.active", bedroomActive)
	subscribe(bathroomMotion, "motion.active", bathroomActive)
}

def bedroomActive(evt) {
	def start = timeToday(startTime, location?.timeZone)
	def stop = timeToday(stopTime, location?.timeZone)
	def now = new Date()
	log.debug "bedroomActive, status: $state.ststus, start: $start, stop: $stop, now: $now"
	if (state.status == "waiting") {
		log.debug "motion detected in bedroom, disarming"
		unschedule("sendMessage")
		state.status = null
	}
	else {
		if (start.before(now) && stop.after(now)) {
			log.debug "motion in bedroom, look for bathroom motion"
			state.status = "pending"
		}
		else {
			log.debug "Not in time window"
		}
	}
}

def bathroomActive(evt) {
	log.debug "bathroomActive, status: $state.status"
	if (state.status == "pending") {
		def delay = threshold.toInteger() * 60
		state.status = "waiting"
		log.debug "runIn($delay)"
		runIn(delay, sendMessage)
	}
}

def sendMessage() {
	log.debug "sendMessage"
	def msg = warnMessage
	log.info msg

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients)
	}
	else {
		sendPush msg
		if (phone1) {
			sendSms phone1, msg
		}
		if (phone2) {
			sendSms phone2, msg
		}
		if (phone3) {
			sendSms phone3, msg
		}
	}
	state.status = null
}