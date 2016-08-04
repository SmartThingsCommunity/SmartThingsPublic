/**
 *  Battery
 *
 *  Copyright 2015 Amol Mundayoor
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
	definition (name: "Battery", namespace: "capabilities", author: "Amol Mundayoor") {
		capability "Battery"
	}

	simulator {
    	for (i in [0,5,10,15,20,30,40,50,60,70,80,90,100]) {
			status "${i} percent": "battery:${i}"
		}
		// TODO: define status and reply messages here
        status "charging":"battery:charging"
        status "discharging":"battery:discharging"
        status "dead":"battery:dead"
        
        reply "charging":"battery:charging"
        reply "discharging":"battery:discharging"
        reply "dead":"battery:dead"
	}

	tiles {
		// TODO: define your main and details tiles here
        standardTile("mainTile","device.battery",width:2,height:2) {
        	state "capacity", label:'${currentValue} ${unit}', unit:"percent"
        	state "charging", backgroundColor:"#00933B"
            state "discharging", backgroundColor:"#F90101"
            state "dead", backgroundColor: "#000000"
        }
        main "mainTile"
        details "mainTile"
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'battery' attribute
	def pair = description.split(":")
    createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def charging() {
	log.debug "charging"
    "charging"
}

def discharging() {
	log.debug "discharging"
    "discharging"
}

def dead() {
	log.debug "dead"
    "dead"
}