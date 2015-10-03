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
 *  OhmConnect Device Type
 *
 *  Author: markewest@gmail.com
 *
 *  Date: 2015-10-02
 */
 
preferences {
       input "ohmid", "text", title: "Ohm Id", 
       description: "Configure your Ohmconnect 8 digit ID (end of the URL at the bottom of the account page)", 
       required: true, displayDuringSetup: true
    }
 
metadata {
	definition (name: "OhmConnect", namespace: "afewremarks", author: "Mark West") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Polling"
        
        command "refresh"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
    
    tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "thermostat", width: 6, height: 4, canChangeIcon: false, decoration: "flat") {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: 'Ohm Hour', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: 'Stable Grid', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
		}

        valueTile("ohmID", "preferences.ohmid", decoration: "flat", wordWrap: false, width: 4, height: 2) {
        	state("ohmid", label:'${currentValue}', unit:"", backgroundColor:"#ffffff")
    	}
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"polling.poll", icon:"st.secondary.refresh"
		}

		main "status"
		details(["status","ohmID","refresh"])
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

def poll() {
	log.debug "Executing 'poll'"
    
    def currentState = device.currentValue("switch")
        log.debug currentState
        
   def params = [
   	uri:  'https://login.ohmconnect.com/verify-ohm-hour/',
    path: ohmid,
    ]
    try {
    httpGet(params) { resp ->

       if(resp.data.active == "True"){
      	log.debug "Ohm Hour"
      
      	if(currentState == "off"){
       		sendEvent(name: "switch", value: "on")
        }
       
       }else{
       	log.debug "Not currently an Ohm Hour"
        
       if(currentState == "on"){
       		sendEvent(name: "switch", value: "off")
        }
        
        
       }
    }
} catch (e) {
    log.error "something went wrong: $e"
}
    
}

