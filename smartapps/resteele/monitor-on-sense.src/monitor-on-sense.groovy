/**
 *  Monitor on Sense
 *
 *  Copyright 2014 Rachel Steele
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
    name: "Monitor on Sense",
    namespace: "resteele",
    author: "Rachel Steele",
    description: "Turn on switch when vibration is sensed",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "Monitor on Vibrate", displayLink: ""])


preferences {
	section("When vibration is sensed...") {
		input "accelerationSensor", "capability.accelerationSensor", title: "Which Sensor?"
	}
section("Turn on switch...") {
		input "switch1", "capability.switch"
	}
}


def installed() {
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def updated() {
	unsubscribe()
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}


def accelerationActiveHandler(evt) {
		switch1.on()
	}
