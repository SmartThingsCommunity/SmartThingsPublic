/**
 *  netatmo-basestation
 *
 *  Copyright 2014 Brian Steere
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
	definition (name: "Netatmo Rain", namespace: "dianoga", author: "Brian Steere") {
		capability "Sensor"
	
		attribute "rain", "number"
        attribute "rainSumHour", "number"
        attribute "rainSumDay", "number"
        attribute "units", "string"
        
        command "poll"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
 		valueTile("rain", "device.rain", width: 2, height: 2, inactiveLabel: false) {
 			state "default", label:'${currentValue}'
 		}
 		valueTile("rainSumHour", "device.rainSumHour", inactiveLabel: false) {
 			state "default", label:'${currentValue}\nhour'
 		}
 		valueTile("rainSumDay", "device.rainSumDay", inactiveLabel: false) {
 			state "default", label:'${currentValue}\nday'
 		}
 		standardTile("refresh", "device.rain", inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.poll", icon:"st.secondary.refresh"
 		}
 		main (["rain", "rainSumHour", "rainSumDay"])
 		details(["rain", "rainSumHour", "rainSumDay", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def poll() {
	parent.poll()
}
