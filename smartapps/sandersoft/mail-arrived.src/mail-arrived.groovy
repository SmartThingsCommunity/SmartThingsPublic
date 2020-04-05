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
 *  Mail Arrived
 *
 *  Author: SmartThings
 */

import java.text.SimpleDateFormat;
import java.util.Date;


definition(
    name: "Mail Arrived",
    namespace: "SanderSoft",
    author: "Kurt Sanders",
    description: "Send a text when mail arrives in your mailbox using a Contact Sensor on your mailbox door.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/mail_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/mail_contact@2x.png"
)

preferences {
	section("When mail arrives...") {
		input "contact", "capability.contactSensor", title: "Where?"
	}
	section("Notify me...") {
        input("recipients", "contact", title: "Send notifications to") {
            input "pushNotification", "bool", title: "Push notification(s)", required: false, defaultValue: "true"
            input "phone1", "phone", title: "Phone number #1", required: false
            input "phone2", "phone", title: "Phone number #2", required: false
        }
	}
    section("Minimum time between actions (optional, defaults to every event)") {
	input "mutePeriodMin", "decimal", title: "Minutes", required: true
	}
    section("Select Time of Day for alerting mailbox eve tnotifications") {
	input "startTime", "time", title: "Start Time", required: true
	input "endTime", "time", title: "End Time", required: true
	}
}

def installed() {
    initialize()
}

def initialize() {
	subscribe(contact, "contact.open", eventHandlerOpen)
	subscribe(contact, "contact.closed", eventHandlerClosed)
	if (state.mailboxMuteTime == null) {
//    	log.trace "Initiization: state.mailboxMuteTime initialized to 0"
	    state.mailboxMuteTime = 0
    }
	if (state.mailboxOpenTime == null) {
//    	log.trace "Initiization: state.mailboxOpenTime initialized to 0"
	    state.mailbox = 0
    }
}

def updated() {
	unsubscribe()
	initialize()
}

// Open Event *******************************************************
def eventHandlerOpen(evt) {
	if (inTheMuteZone()==false) {
        TimeZone.setDefault(TimeZone.getTimeZone('America/New_York'))
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM, hh:mm a")
		notifications("The MailMan is here at ${format.format(now())}. Yippee!")
        state.mailboxOpenTime = now()
    }
}

// Close Event ******************************************************
def eventHandlerClosed(evt) {
	if (inTheMuteZone()==false) {
        TimeZone.setDefault(TimeZone.getTimeZone('America/New_York'))
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM, hh:mm a")
        def long timeOpen = (now()-state.mailboxOpenTime)/1000
        def msg = "Your postal mailbox door was $evt.value after ${timeConversion(timeOpen)}"
		notifications(msg)
		state.mailboxMuteTime = now()
    }
}

def inTheMuteZone() {
    def lastTime = state.mailboxMuteTime
//    log.trace "lastTime: $lastTime Now(): ${now()}"
    def long zoneDeltaSecs = (now() - (lastTime + (mutePeriodMin*60000)))
//	log.trace "zoneDeltaSecs: ${zoneDeltaSecs/1000}"
	def inTheMuteZoneSecs = (zoneDeltaSecs < 0)
    if  (inTheMuteZoneSecs) {
        log.trace "In the Mute Zone, waiting for ${(zoneDeltaSecs/1000).abs()}"
        return true
    }
    else {
        log.trace "Outside the Mute Zone, OK to Notify"
        return false
	}
}

    
def notifications(msg) {
    //Send out Notifications
    def start = timeToday(startTime, location.timeZone)
	def end = timeToday(endTime, location.timeZone)
    def timeOfDay = new Date()
	def between = timeOfDayIsBetween(start, end, timeOfDay, location.timeZone)
    log.trace "Notification Msg: $msg"
    if (between) {
	    log.trace "Notification Msg: $msg"
        if (pushNotification) {
                sendPush(msg)
            }
        if (phone1 != null && phone1 != "") {
            sendSms(phone1,msg)
            }
        if (phone2 != null && phone2 != "") {
            sendSms(phone2,msg)
            }
	}
    else {
        log.trace "Notification Msg NOT SENT: $msg"
	    }
}

private static String timeConversion(long totalSeconds) {

    final int MINUTES_IN_AN_HOUR = 60;
    final int SECONDS_IN_A_MINUTE = 60;
	def HMS = "";

    int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
    int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
    int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
    int hours = totalMinutes / MINUTES_IN_AN_HOUR;
    if (hours > 0) {
	    return hours + " hrs " + minutes + " mins " + seconds + " secs";
    }
    else if (minutes>0) {
        return minutes + " mins " + seconds + " secs";
	}
    else {
	    return seconds + " secs";
    }
}