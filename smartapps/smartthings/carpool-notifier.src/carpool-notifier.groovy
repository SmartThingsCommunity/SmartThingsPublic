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
 * title: Carpool Notifier
 *
 * description:
 * Do you carpool to work with your spouse? Do you pick your children up from school? Have they been waiting in doors for you? Let them know you've arrived with Carpool Notifier.
 *
 * This SmartApp is designed to send notifications to your carpooling buddies when you arrive to pick them up. What separates this SmartApp from other notification SmartApps is that it will only send a notification if your carpool buddy is not with you.
 *
 * category: Family

 * icon:		https://s3.amazonaws.com/smartapp-icons/Family/App-IMadeIt.png
 * icon2X:	https://s3.amazonaws.com/smartapp-icons/Family/App-IMadeIt%402x.png
 *
 *  Author: steve
 *  Date: 2013-11-19
 */

definition(
    name: "Carpool Notifier",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Send notifications to your carpooling buddies when you arrive to pick them up. If the person you are picking up is home, and has been for 5 minutes or more, they will get a notification when you arrive.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Family/App-IMadeIt.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Family/App-IMadeIt@2x.png",
    pausable: true
)

preferences {
	section() {
		input(name: "driver", type: "capability.presenceSensor", required: true, multiple: false, title: "When this person arrives", description: "Who's driving?")
		input("recipients", "contact", title: "Notify", description: "Send notifications to") {
			input(name: "phoneNumber", type: "phone", required: true, multiple: false, title: "Send a text to", description: "Phone number")
		}
		input(name: "message", type: "text", required: false, multiple: false, title: "With the message:", description: "Your ride is here!")
		input(name: "rider", type: "capability.presenceSensor", required: true, multiple: false, title: "But only when this person is not with you", description: "Who are you picking up?")
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
	subscribe(driver, "presence.present", presence)
}

def presence(evt) {

	if (evt.value == "present" && riderIsHome())
	{
//        	log.debug "Rider Is Home; Send A Text"
		sendText()
	}

}

def riderIsHome() {

//	log.debug "rider presence: ${rider.currentPresence}"

	if (rider.currentPresence != "present")
	{
		return false
	}

	def riderState = rider.currentState("presence")
//	log.debug "riderState: ${riderState}"
	if (!riderState)
	{
		return true
	}

	def latestState = rider.latestState("presence")

	def now = new Date()
	def minusFive = new Date(minutes: now.minutes - 5)


	if (minusFive > latestState.date)
	{
		return true
	}

	return false
}

def sendText() {
	if (location.contactBookEnabled) {
		sendNotificationToContacts(message ?: "Your ride is here!", recipients)
	}
	else {
		sendSms(phoneNumber, message ?: "Your ride is here!")
	}
}
