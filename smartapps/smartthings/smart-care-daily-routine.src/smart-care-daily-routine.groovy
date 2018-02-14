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
 *  Smart Care: Daily Routine
 *
 *  Author: SmartThings
 *  Date: 2013-03-06
 *
 *  Stay connected to your loved ones. Get notified if they are not up and moving around 
 *  by a specified time and/or if they have not opened a cabinet or door according to a set schedule. 
 */

definition(
	name: "Smart Care: Daily Routine",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Stay connected to your loved ones. Get notified if they are not up and moving around by a specified time and/or if they have not opened a cabinet or door according to a set schedule.",
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
		section("Who are you checking on?") {
			input "person1", "text", title: "Name?"
		}
		section("If there’s no movement (optional, leave blank to not require)...") {
			input "motion1", "capability.motionSensor", title: "Where?", required: false
		}
		section("or a door or cabinet hasn’t been opened (optional, leave blank to not require)...") {
			input "contact1", "capability.contactSensor", required: false
		}
		section("between these times...") {
			input "time0", "time", title: "From what time?"
			input "time1", "time", title: "Until what time?"
		}
		section("then alert the following people...") {
			input("recipients", "contact", title: "People to notify", description: "Send notifications to") {
				input "phone1", "phone", title: "Phone number?", required: false
			}
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(time1, "scheduleCheck")
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe() //TODO no longer subscribe like we used to - clean this up after all apps updated
	unschedule()
	schedule(time1, "scheduleCheck")
}

def scheduleCheck()
{
	if(noRecentContact() && noRecentMotion()) {
		def person = person1 ?: "your elder"
		def msg = "Alert! There has been no activity at ${person}‘s place ${timePhrase}"
		log.debug msg

		if (location.contactBookEnabled) {
			sendNotificationToContacts(msg, recipients)
		}
		else {
			if (phone1) {
				sendSms(phone1, msg)
			} else {
				sendPush(msg)
			}
		}
	} else {
		log.debug "There has been activity ${timePhrase}, not sending alert"
	}
}

private noRecentMotion()
{
	if(motion1) {
		def motionEvents = motion1.eventsSince(sinceTime)
		log.trace "Found ${motionEvents?.size() ?: 0} motion events"
		if (motionEvents.find { it.value == "active" }) {
			log.debug "There have been recent ‘active’ events"
			return false
		} else {
			log.debug "There have not been any recent ‘active’ events"
			return true
		}
	} else {
		log.debug "Motion sensor not enabled"
		return true
	}
}

private noRecentContact()
{
	if(contact1) {
		def contactEvents = contact1.eventsSince(sinceTime)
		log.trace "Found ${contactEvents?.size() ?: 0} door events"
		if (contactEvents.find { it.value == "open" }) {
			log.debug "There have been recent ‘open’ events"
			return false
		} else {
			log.debug "There have not been any recent ‘open’ events"
			return true
		}
	} else {
		log.debug "Contact sensor not enabled"
		return true
	}
}

private getSinceTime() {
	if (time0) {
		return timeToday(time0, location?.timeZone)
	}
	else {
		return new Date(now() - 21600000)
	}
}

private getTimePhrase() {
	def interval = now() - sinceTime.time
	if (interval < 3600000) {
		return "in the past ${Math.round(interval/60000)} minutes"
	}
	else if (interval < 7200000) {
		return "in the past hour"
	}
	else {
		return "in the past ${Math.round(interval/3600000)} hours"
	}
}