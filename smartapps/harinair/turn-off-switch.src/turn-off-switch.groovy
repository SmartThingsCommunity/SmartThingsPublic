/**
 *  Turn Off Switch
 *
 *  Copyright 2016 Hari Gangadharan
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
    name: "Turn Off Switch",
    namespace: "harinair",
    author: "Hari Gangadharan",
    description: "Periodically checks a switch and turns it off after given delay",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section ("Turn off a switch...") {
		input "theSwitch", "capability.switch"
	}
    section("Turn off after") {
        input "minutes", "number", required: true, title: "Minutes?"
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
    def freq = 1
    schedule("0 0/$freq * * * ?", checkSwitch)
    state.lasOnTime = null
}

def checkSwitch() {
    def switchStatus = theSwitch.currentSwitch
    def threshold = 1000 * 60 * minutes
    if (switchStatus == "on") {
        // let us save first found time
        if (state.lastOnTime) {
            if (state.lastOnTime + threshold < now()) {
                state.lastOnTime = null
                theSwitch.off()
            }
        } else {
            state.lastOnTime = now()
        }
    } else {
        // reset last on time
        state.lastOnTime = null
    }
}
