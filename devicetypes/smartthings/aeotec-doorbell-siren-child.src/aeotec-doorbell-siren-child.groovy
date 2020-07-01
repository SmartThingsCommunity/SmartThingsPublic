/**
 *	Copyright 2020 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition(name: "Aeotec Doorbell Siren Child", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Health Check"
		capability "Alarm"
		capability "Chime"

	}
	tiles {
		multiAttributeTile(name: "chime", type: "generic", width: 6, height: 2) {
			tileAttribute("device.chime", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'chime', action: 'chime.chime', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "chime", label: 'off', action: 'chime.off', icon: "st.alarm.alarm.alarm", backgroundColor: "#ff0000"
			}
		}
		multiAttributeTile(name: "alarm", type: "generic", width: 6, height: 2) {
			tileAttribute("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'off', action: 'alarm.siren', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "both", label: 'alarm', action: 'alarm.off', icon: "st.alarm.alarm.alarm", backgroundColor: "#ff0000"
			}
		}
		standardTile("off", "device.chime", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "chime.off", icon: "st.secondary.off"
		}

		main "chime"
		details(["chime", "alarm", "off"])
	}
}

def installed() {
	sendEvent(name: "chime", value: "off", isStateChange: true, displayed: false)
	sendEvent(name: "alarm", value: "off", isStateChange: true, displayed: false)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false)
}

def off() {
	parent.setOffChild(device.deviceNetworkId)
}

def on() {
	parent.setOnChild(device.deviceNetworkId)
}

def chime() {
	on()
}

def strobe() {
	on()
}

def siren() {
	on()
}

def both() {
	on()
}