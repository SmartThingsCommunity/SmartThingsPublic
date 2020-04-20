/**
 *  Turn Off When Wet
 *
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
 */
definition(
    name: "Turn Off When Wet",
    namespace: "techwithjake",
    author: "Tech With Jake",
    description: "Turns switch off based on moisture sensor input.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/dry-the-wet-spot.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/dry-the-wet-spot@2x.png"
)

preferences {
	section("When water is sensed...") {
		input "sensor", "capability.waterSensor", title: "Where?", required: true, multiple: true
	}
	section("Turn off a switch...") {
		input "light", "capability.switch", title: "Which?", required: true, multiple: true
	}
}

def installed() {
	subscribe(sensor, "water.dry", waterHandler)
	subscribe(sensor, "water.wet", waterHandler)
}

def updated() {
	unsubscribe()
	subscribe(sensor, "water.dry", waterHandler)
	subscribe(sensor, "water.wet", waterHandler)
}

def waterHandler(evt) {
	log.debug "Sensor says ${evt.value}"
	if (evt.value == "wet") {
		light.off()
	}
}
