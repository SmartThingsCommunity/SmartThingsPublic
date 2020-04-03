/**
 *  Virtual Switch
 *
 *  Copyright 2014 Juan Risso
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
        definition (
        name: "Laundry Virtual Switch", 
        namespace: "Virtual", 
        author: "Juan Risso") 
    {
        capability "Switch"
        capability "Refresh"        
    }

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: false) {
			state "off", label: 'Off', action: "switch.on", icon: "https://s3.amazonaws.com/smartthings-device-icons/Appliances/appliances8-icn.png", backgroundColor: "#ffffff"
			state "on", label: 'Running', action: "switch.off", icon: "https://s3.amazonaws.com/smartthings-device-icons/Appliances/appliances8-icn.png", backgroundColor: "#79b821"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}        
		main "button"
		details(["button", "refresh"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
}

def off() {
	sendEvent(name: "switch", value: "off")
}