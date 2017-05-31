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

    definition (name: "Simulated Switch", namespace: "smartthings/testing", author: "bob") {
		capability "Switch"
        capability "Relay Switch"
		capability "Sensor"
		capability "Actuator"

		command "onPhysical"
		command "offPhysical"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}
		standardTile("on", "device.switch", decoration: "flat") {
			state "default", label: 'On', action: "onPhysical", backgroundColor: "#ffffff"
		}
		standardTile("off", "device.switch", decoration: "flat") {
			state "default", label: 'Off', action: "offPhysical", backgroundColor: "#ffffff"
		}
        main "switch"
		details(["switch","on","off"])
	}
}

def parse(description) {
}

def on() {
	log.debug "$version on()"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "$version off()"
	sendEvent(name: "switch", value: "off")
}

def onPhysical() {
	log.debug "$version onPhysical()"
	sendEvent(name: "switch", value: "on", type: "physical")
}

def offPhysical() {
	log.debug "$version offPhysical()"
	sendEvent(name: "switch", value: "off", type: "physical")
}

private getVersion() {
	"PUBLISHED"
}
