/**
 *  MyQ Light Controller
 *
 *  Copyright 2015 Jason Mok
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
 *  Last Updated : 7/15/2015
 *
 */
metadata {
	definition (name: "MyQ Light Controller", namespace: "copy-ninja", author: "Jason Mok") {
		capability "Actuator"
		capability "Sensor"
		capability "Refresh"
		capability "Polling"
		capability "Switch"

		command "updateDeviceStatus", ["string"]
	}

	simulator {	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state("off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on")
			state("on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off")
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state("default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh")
		}
		main "switch"
		details(["switch", "refresh"])
	}
}

def parse(String description) {}

def on() { 
	parent.sendCommand(this, "desiredlightstate", 1)
	updateDeviceStatus(1)
}
def off() { 
	parent.sendCommand(this, "desiredlightstate", 0)
	updateDeviceStatus(0)
}

def refresh() { parent.refresh() }

def poll() { updateDeviceStatus(parent.getDeviceStatus(this)) }

def updateDeviceStatus(status) {
	if (status == "0") {  sendEvent(name: "switch", value: "off", display: true, descriptionText: device.displayName + " was off")  }   
	if (status == "1") { sendEvent(name: "switch", value: "on", display: true, descriptionText: device.displayName + " was on")  }
}