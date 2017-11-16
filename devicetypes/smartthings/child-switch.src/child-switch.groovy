/**
 *  Copyright 2017 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Child Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
		capability "Actuator"
		capability "Sensor"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00a0dc", nextState: "off"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on"
			}
		}

		main "switch"
		details(["switch"])
	}
}

void on() {
	//event required for Cooper RF 5-button Controller to access updated switch state
	//before synchronizing controller with current cloud state
	sendEvent(name: "switch", value: "on", descriptionText: "$displayName was switched on")
	parent.childOn(device.deviceNetworkId)
}

void off() {
	//event required for Cooper RF 5-button Controller to access updated switch state
	//before synchronizing controller with current cloud state
	sendEvent(name: "switch", value: "off", descriptionText: "$displayName was switched off")
	parent.childOff(device.deviceNetworkId)
}
