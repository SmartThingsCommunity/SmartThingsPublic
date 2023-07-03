/**
 *  Copyright 2017 Karl Morris
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
 *  Turn on light when door or window opens at night
 *
 *  Author: Karl Morris
 */
 
definition(
    name: "Turn On Light When Open At Night",
    namespace: "karlmorris",
    author: "Karl Morris",
    description: "Turn light(s) on when an sensor opens after sundown and before sunup.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("When what opens...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Turn on light(s)...") {
		input "switches", "capability.switch", multiple: true
	}
}

def installed() {
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated() {
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {

    def now = new Date()
	def sunTime = getSunriseAndSunset();

	log.debug "$evt.value: $evt, $settings"
	log.trace "Attemping to turn on switches: $switches"
    
    if((now > sunTime.sunset && now < sunTime.sunrise)) {
		switches.on()
		log.debug "It's dark out. Turning the light(s) on."
	} else {
    	log.trace "light(s) not turned on. It's bright out."
    }
}