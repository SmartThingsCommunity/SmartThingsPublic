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
 *  Medicine Reminder
 *
 *  Author: SmartThings
 */

definition(
    name: "Medicine Reminder",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Set up a reminder so that if you forget to take your medicine (determined by whether a cabinet or drawer has been opened) by specified time you get a notification or text message.",
    category: "Health & Wellness",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text_contact@2x.png",
    pausable: true
)

preferences {
	section("Choose your medicine cabinet..."){
		input "cabinet1", "capability.contactSensor", title: "Where?"
	}
	section("Take my medicine at..."){
		input "time1", "time", title: "Time 1"
		input "time2", "time", title: "Time 2", required: false
		input "time3", "time", title: "Time 3", required: false
		input "time4", "time", title: "Time 4", required: false
	}
	section("I forget send me a notification and/or text message..."){
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPush", "enum", title: "Push Notification", required: false, options: ["Yes", "No"]
            input "phone1", "phone", title: "Phone Number", required: false
        }
	}
	section("Time window (optional, defaults to plus or minus 15 minutes") {
		input "timeWindow", "decimal", title: "Minutes", required: false
	}
}

def installed()
{
	initialize()
}

def updated()
{
	unschedule()
	initialize()
}

def initialize() {
	def window = timeWindowMsec
	[time1, time2, time3, time4].eachWithIndex {time, index ->
		if (time != null) {
			def endTime = new Date(timeToday(time, location?.timeZone).time + window)
			log.debug "Scheduling check at $endTime"
			//runDaily(endTime, "scheduleCheck${index}")
			switch (index) {
				case 0:
					schedule(endTime, scheduleCheck0)
					break
				case 1:
					schedule(endTime, scheduleCheck1)
					break
				case 2:
					schedule(endTime, scheduleCheck2)
					break
				case 3:
					schedule(endTime, scheduleCheck3)
					break
			}
		}
	}
}

def scheduleCheck0() { scheduleCheck() }
def scheduleCheck1() { scheduleCheck() }
def scheduleCheck2() { scheduleCheck() }
def scheduleCheck3() { scheduleCheck() }

def scheduleCheck()
{
	log.debug "scheduleCheck"
	def t0 = new Date(now() - (2 * timeWindowMsec))
	def t1 = new Date()
	def cabinetOpened = cabinet1.eventsBetween(t0, t1).find{it.name == "contact" && it.value == "open"}
	log.trace "Looking for events between $t0 and $t1: $cabinetOpened"

	if (cabinetOpened) {
		log.trace "Medicine cabinet was opened since $midnight, no notification required"
	} else {
		log.trace "Medicine cabinet was not opened since $midnight, sending notification"
		sendMessage()
	}
}

private sendMessage() {
	def msg = "Please remember to take your medicine"
	log.info msg
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (phone1) {
            sendSms(phone1, msg)
        }
        if (sendPush == "Yes") {
            sendPush(msg)
        }
    }
}

def getTimeWindowMsec() {
	(timeWindow ?: 15) * 60000 as Long
}
