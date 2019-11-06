/**
 *	Copyright 2019 SmartThings
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
	definition(name: "Aeotec Child Siren", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartThings_Siren") {
		capability "Actuator"
		capability "Health Check"
		capability "Alarm"

	}
	tiles {
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "off", label: 'off', action: 'alarm.strobe', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
			state "both", label: 'alarm!', action: 'alarm.off', icon: "st.alarm.alarm.alarm", backgroundColor: "#e86d13"
		}
		standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "alarm.off", icon: "st.secondary.off"
		}

		main "alarm"
		details(["alarm", "off"])
	}
}

def installed() {
	configureDeviceHealth()
	sendEvent(name: "alarm", value: "off", isStateChange: true, displayed: false)
}

def configureDeviceHealth() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false)
}

def off() {
	parent.setOffChild(device.deviceNetworkId)
}

def on() {
	parent.setOnChild(device.deviceNetworkId)
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