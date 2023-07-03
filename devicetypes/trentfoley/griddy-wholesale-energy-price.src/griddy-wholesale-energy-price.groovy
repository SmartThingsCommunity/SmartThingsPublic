/**
 *  Griddy Wholesale Energy Price
 *
 *  Copyright 2018 Trent Foley
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
	definition (name: "Griddy Wholesale Energy Price", namespace: "trentfoley", author: "Trent Foley") {
		capability "Refresh"
        capability "Energy Meter"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
    	valueTile("energy", "device.energy", canChangeBackground: true) {
            state("energy", label:'${currentValue}', unit:"¢",
                backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 1, color: "#1e9cbb"],
                    [value: 2, color: "#90d2a7"],
                    [value: 3, color: "#44b621"],
                    [value: 5, color: "#f1d801"],
                    [value: 8, color: "#d04e00"],
                    [value: 13, color: "#bc2323"]
                ]
            )
        }
        multiAttributeTile(name:"energyMulti", type:"generic", width:6, height:4) {
            tileAttribute("device.energy", key: "PRIMARY_CONTROL") {
                attributeState "energy", label:'${currentValue}¢', defaultState: true, backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 1, color: "#1e9cbb"],
                    [value: 2, color: "#90d2a7"],
                    [value: 3, color: "#44b621"],
                    [value: 5, color: "#f1d801"],
                    [value: 8, color: "#d04e00"],
                    [value: 13, color: "#bc2323"]
                ]
            }
        }
        standardTile("refresh", "device.energy", height: 3, width: 3, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        main "energy"
        details(["energyMulti", "refresh"])
	}
}

def initialize() {
	log.debug "Executing 'initialize'"
    runEvery5Minutes(refresh)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def refresh() {
	log.debug "Executing 'refresh'"
	    
    def data = parent.refreshChild(this)
	if(data) {
    	sendEvent(name: "energy", value: data.price, unit: "¢")
	} else {
    	log.error "ERROR: Device connection removed? No data found for ${device.deviceNetworkId}"
    }
}
