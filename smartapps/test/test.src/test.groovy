/**
 *  Test
 *
 *  Copyright 2015 handstudio
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
    name: "Test",
    namespace: "test",
    author: "handstudio",
    description: "test",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	section("Turn on when motion detected:") {
            input "themotion", "capability.motionSensor", required: true, title: "Where?"
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
    subscribe(themotion, "motion.active", motionDetectedHandler)
    subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt) {

    log.debug "켜짐"    
    def motionState = themotion.currentState("motion")
    log.debug motionState.value
}


def motionStoppedHandler(evt) {

    log.debug "꺼짐"
    def motionState = themotion.currentState("motion")
	log.debug motionState.value
}

def getMotionState(){

	def motionState = themotion.currentState("motion")
	log.debug motionState.value
}


mappings {
	path("/switches") {
		action: [
			GET: "getMotionState"
		]
	}
}
