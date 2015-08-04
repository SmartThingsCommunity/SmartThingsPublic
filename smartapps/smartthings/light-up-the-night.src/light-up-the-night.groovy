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
 *  Light Up The Night
 *
 *  Author: SmartThings
 */
definition(
    name: "Light Up the Night",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights on when it gets dark and off when it becomes light again.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance@2x.png"
)

preferences {
	section("Monitor the luminosity...") {
		input "lightSensor", "capability.illuminanceMeasurement"
	}
	section("Turn on a light...") {
		input "lights", "capability.switch", multiple: true
	}
}

def installed() {
	subscribe(lightSensor, "illuminance", illuminanceHandler)
}

def updated() {
	unsubscribe()
	subscribe(lightSensor, "illuminance", illuminanceHandler)
}

// New aeon implementation
def illuminanceHandler(evt) {
	def lastStatus = state.lastStatus
	if (lastStatus != "on" && evt.integerValue < 30) {
		lights.on()
		state.lastStatus = "on"
	}
	else if (lastStatus != "off" && evt.integerValue > 50) {
		lights.off()
		state.lastStatus = "off"
	}
}
