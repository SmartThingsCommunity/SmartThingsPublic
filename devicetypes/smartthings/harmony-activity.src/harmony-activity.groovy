/**
 *  Logitech Harmony Activity
 *
 *  Copyright 2015 Juan Risso
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
        definition (name: "Harmony Activity", namespace: "smartthings", author: "Juan Risso") {
        capability "Switch"
        capability "Actuator"
		capability "Refresh"

        command "huboff"
        command "alloff"
        command "refresh"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#00A0DC", nextState: "off"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("forceoff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Force End', action:"switch.off", icon:"st.secondary.off"
		}
		standardTile("huboff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'End Hub Action', action:"huboff", icon:"st.harmony.harmony-hub-icon"
		}
		standardTile("alloff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'All Actions', action:"alloff", icon:"st.secondary.off"
		}
		main "button"
		details(["button", "refresh", "forceoff", "huboff", "alloff"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
    log.trace parent.activity(device.deviceNetworkId,"start")
}

def off() {
	sendEvent(name: "switch", value: "off")
    log.trace parent.activity(device.deviceNetworkId,"end")
}

def huboff() {
	sendEvent(name: "switch", value: "off")
    log.trace parent.activity(device.deviceNetworkId,"hub")
}

def alloff() {
	sendEvent(name: "switch", value: "off")
    log.trace parent.activity("all","end")
}


def refresh() {
	log.debug "Executing 'refresh'"
	log.trace parent.poll()
}
