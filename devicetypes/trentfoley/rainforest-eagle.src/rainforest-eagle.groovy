/**
 *  Copyright 2020 Trent Foley
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
 *  Rainforest Eagle
 *
 *  Author: Trent Foley
 *  Date: 2020-04-19
 */
metadata {

	definition(name: "Rainforest Eagle", namespace: "trentfoley", author: "Trent Foley") {
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
	}

	simulator {
		// define status and reply messages here
	}

	tiles {
    	valueTile("main", "device.power", canChangeBackground: true) {
			state("power", label: '${currentValue} W',
                backgroundColors:[
                    [value: 500, color: "#153591"],
                    [value: 1000, color: "#1e9cbb"],
                    [value: 1500, color: "#90d2a7"],
                    [value: 2000, color: "#44b621"],
                    [value: 2500, color: "#f1d801"],
                    [value: 3000, color: "#d04e00"],
                    [value: 3500, color: "#bc2323"]
                ]
            )
		}

		valueTile("power", "device.power", width: 1, height: 1) {
			state("power", label: '${currentValue} W',
                backgroundColors:[
                    [value: 500, color: "#153591"],
                    [value: 1000, color: "#1e9cbb"],
                    [value: 1500, color: "#90d2a7"],
                    [value: 2000, color: "#44b621"],
                    [value: 2500, color: "#f1d801"],
                    [value: 3000, color: "#d04e00"],
                    [value: 3500, color: "#bc2323"]
                ]
            )
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "main"
		details(["power", "refresh"])
	}
}


def initialize() {
	log.debug "Executing 'initialize'"
    runEvery1Minute(refresh)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def refresh() {
	log.debug "Executing 'refresh'"
	    
    def data = parent.refreshChild(this)
    log.debug data
    
	if (data.power) {
    	sendEvent(name: "power", value: data.power, unit: data.unit)
	} else {
    	log.error "No data found for ${device.deviceNetworkId}"
    }
}
