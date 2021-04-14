/**
 *  NX-595e Alarm
 *
 *  Copyright 2016 Trent Foley
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
	definition (name: "NX-595e Alarm", namespace: "trentfoley", author: "Trent Foley") {
        capability "Actuator"
        capability "Switch"
        capability "Sensor"
        capability "Refresh"
        capability "Polling"
        
        attribute "armType", "string"
        attribute "status", "string"
    }

	tiles(scale: 2) {
    	multiAttributeTile(name:"armType", type:"generic", width: 6, height: 4) {
        	tileAttribute("device.armType", key: "PRIMARY_CONTROL") {
            	attributeState "off", label: 'Off', icon: "st.Home.home2", backgroundColor: "#79b821"
                attributeState "away", label: 'Armed Away', icon: "st.Home.home3", backgroundColor: "#bc2323"
                attributeState "stay", label: 'Armed Stay', icon: "st.Home.home4", backgroundColor: "#ffa81e"
            }
		}
        valueTile("status", "device.status", decoration: "flat", width: 6, height: 2) {
            state "status", label: '${currentValue}'
        }
        standardTile("chime", "device.switch", width: 2, height: 2) {
			state "off", label: 'Off', action: "switch.on", icon: "st.Seasonal Winter.seasonal-winter-002", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.Seasonal Winter.seasonal-winter-002", backgroundColor: "#79b821", nextState: "off"
		}
        standardTile("refresh", "device.status", decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "armType"
		details(["armType", "status", "chime", "refresh"])
	}
}

def parse(String description) {
}

def on() {
	parent.chime()
}

def off() {
	parent.chime()
}

def refresh() {
	parent.refresh()
}

def poll() {
	parent.refresh()
}