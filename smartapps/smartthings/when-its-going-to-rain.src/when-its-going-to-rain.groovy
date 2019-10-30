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
 *  When It's Going To Rain
 *
 *  Author: SmartThings
 */
definition(
    name: "When It's Going to Rain",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Is your shed closed? Are your windows shut? Is the grill covered? Are your dogs indoors? Will the lawn and plants need to be watered tomorrow?",
	category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png"
)

preferences {
	section("Zip code..."){
		input "zipcode", "text", title: "Zipcode?"
	}
	// TODO: would be nice to cron this so we could check every hour or so
	section("Check at..."){
		input "time", "time", title: "When?"
	}
	section("Things to check..."){
		input "sensors", "capability.contactSensor", multiple: true
	}
	section("Text me if I anything is open..."){
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone number?"
        }
	}
}


def installed() {
	log.debug "Installed: $settings"
	schedule(time, "scheduleCheck")
}

def updated() {
	log.debug "Updated: $settings"
	unschedule()
	schedule(time, "scheduleCheck")
}

def scheduleCheck() {
	def response = getWeatherFeature("forecast", zipcode)
	if (isStormy(response)) {
		def open = sensors.findAll { it?.latestValue("contact") == 'open' }
		if (open) {
            if (location.contactBookEnabled) {
                sendNotificationToContacts("A storm is a coming and the following things are open: ${open.join(', ')}", recipients)
            }
            else {
                sendSms(phone, "A storm is a coming and the following things are open: ${open.join(', ')}")
            }
		}
	}
}

private isStormy(json) {
	def STORMY = ['rain', 'snow', 'showers', 'sprinkles', 'precipitation']

	def forecast = json?.forecast?.txt_forecast?.forecastday?.first()
	if (forecast) {
		def text = forecast?.fcttext?.toLowerCase()
		if (text) {
			def result = false
			for (int i = 0; i < STORMY.size() && !result; i++) {
				result = text.contains(STORMY[i])
			}
			return result
		} else {
			return false
		}
	} else {
		log.warn "Did not get a forecast: $json"
		return false
	}
}
