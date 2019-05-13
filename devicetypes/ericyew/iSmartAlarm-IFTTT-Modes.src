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
 */
metadata {

    definition (name: "iSmartAlarm IFTTT Modes", namespace: "ericyew", author: "ericyew") {
		capability "Switch"
    capability "Relay Switch"
		capability "Sensor"
		capability "Actuator"

		command "arm"
		command "disarm"
    command "home"
	}

	tiles {
  standardTile("mode", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "armed", label: "Armed", action:"switch.arm", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "disarmed", label: "Disarmed", action: "switch.disarm", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            state "home", label: "Home", action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
   }
		standardTile("Arm", "device.switch", decoration: "flat") {
			state "default", label: 'Arm', action: "onPhysical", backgroundColor: "#ffffff"
		}
		standardTile("Disarm", "device.switch", decoration: "flat") {
			state "default", label: 'Off', action: "offPhysical", backgroundColor: "#ffffff"
		}
    standardTile("Home", "device.switch", decoration: "flat") {
			state "default", label: 'Off', action: "offPhysical", backgroundColor: "#ffffff"
		}
        main "mode"
		details(["Arm","Disarm","Home"])
	}
}

def parse(description) {
}

def arm() {
	log.debug "$version arm()"
	sendEvent(name: "switch", value: "arm")
}

def disarm() {
	log.debug "$version disarm()"
	sendEvent(name: "switch", value: "disarm")
}

def home() {
	log.debug "$version home()"
	sendEvent(name: "switch", value: "home")
}

private getVersion() {
	"PUBLISHED"
}
