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
	input "frequency", "decimal", title: "Minutes", required: false
	}
}

private String timeDifference(long timeDifference1) {
    long timeDifference = timeDifference1/1000;
    int h = (int) (timeDifference / (3600));
    int m = (int) ((timeDifference - (h * 3600)) / 60);
    int s = (int) (timeDifference - (h * 3600) - m * 60);
    return String.format("%02d:%02d:%02d", h,m,s);
}

def installed() {
	subscribe(contact, "contact.closed", eventHandler)
	subscribe(contact, "contact.open", eventHandler)
}

def updated() {
	unsubscribe()
	subscribe(contact, "contact.closed", eventHandler)
	subscribe(contact, "contact.open", eventHandler)
}

def eventHandler(evt) {    
	if (frequency) {
    log.trace "Frequency = $frequency"
		def lastTime = state[evt.deviceId]
	    log.trace "lastTime = $lastTime"
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			takeAction(evt)
		}
	}
	else {
		takeAction(evt)
	}    
}

private takeAction(evt) { //Specified contact has an event
    log.trace "$evt.value"
    //Set Timezone to New York for me!
    TimeZone.setDefault(TimeZone.getTimeZone('America/New_York'))
    def today = new Date()
    SimpleDateFormat format = new SimpleDateFormat(
                "EEE, d MMM, hh:mm a");
	def msg = "Your postal mailbox was $evt.value at ${format.format(today)}"
	log.trace "$msg"
    //Send out Notifications
	if (pushNotification) {
			sendPush(msg)
        }
    if (phone1 != null && phone1 != "") {
        sendSms(phone1,msg)
        }
    if (phone2 != null && phone2 != "") {
        sendSms(phone2,msg)
    	}
    if (frequency) {
        state[evt.deviceId] = now()
    	}
}