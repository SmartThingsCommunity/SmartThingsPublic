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
 *  Elder Care
 *
 *  Author: SmartThings
 *  Date: 2013-03-06
 *
 *  Stay connected to your loved ones. Get notified if they are not up and moving around 
 *  by a specified time and/or if they have not opened a cabinet or door according to a set schedule. 
 */

definition(
    name: "Elder Care: Daily Routine",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Stay connected to your loved ones. Get notified if they are not up and moving around by a specified time and/or if they have not opened a cabinet or door according to a set schedule.",
    category: "Family",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/calendar_contact-accelerometer.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/calendar_contact-accelerometer@2x.png"
)

preferences {
	section("Who are you checking on?") {
		input "person1", "text", title: "Name?"
	}
	section("If there's no movement (optional, leave blank to not require)...") {
		input "motion1", "capability.motionSensor", title: "Where?", required: false
	}
	section("or a door or cabinet hasn't been opened (optional, leave blank to not require)...") {
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
		def msg = "Alert! There has been no activity at ${person}'s place ${timePhrase}"
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
			log.debug "There have been recent 'active' events"
			return false
		} else {
			log.debug "There have not been any recent 'active' events"
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
			log.debug "There have been recent 'open' events"
			return false
		} else {
			log.debug "There have not been any recent 'open' events"
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
