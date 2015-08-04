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
 *  Brighten Dark Places
 *
 *  Author: SmartThings
 */
definition(
    name: "Brighten Dark Places",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights on when a open/close sensor opens and the space is dark.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance@2x.png"
)

preferences {
	section("When the door opens...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("And it's dark...") {
		input "luminance1", "capability.illuminanceMeasurement", title: "Where?"
	}
	section("Turn on a light...") {
		input "switch1", "capability.switch"
	}
}

def installed()
{
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	def lightSensorState = luminance1.currentIlluminance
	log.debug "SENSOR = $lightSensorState"
	if (lightSensorState != null && lightSensorState < 10) {
		log.trace "light.on() ... [luminance: ${lightSensorState}]"
		switch1.on()
	}
}
