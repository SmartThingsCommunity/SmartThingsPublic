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
 *  Darken Behind Me
 *
 *  Author: SmartThings
 */
definition(
    name: "Darken Behind Me",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights off after a period of no motion being observed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png"
)

preferences {
	section("When there's no movement...") {
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Turn off a light...") {
		input "switch1", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(motion1, "motion.inactive", motionInactiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.inactive", motionInactiveHandler)
}

def motionInactiveHandler(evt) {
	switch1.off()
}
