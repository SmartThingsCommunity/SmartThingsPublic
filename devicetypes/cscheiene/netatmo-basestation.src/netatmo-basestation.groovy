/**
 *  netatmo-basestation Date: 10.07.2017
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
 * Based on Brian Steere's netatmo-basestation DTH
 */
 
 
metadata {
	definition (name: "Netatmo Basestation", namespace: "cscheiene", author: "Brian Steere,cscheiene") {
		capability "Relative Humidity Measurement"
		capability "Temperature Measurement"
        capability "Sensor"
        capability "Carbon Dioxide Measurement"
        capability "Sound Pressure Level"
        capability "Refresh"

		attribute "pressure", "number"
        attribute "min_temp", "number"
        attribute "max_temp", "number"
        attribute "temp_trend", "string"
        attribute "pressure_trend", "string"
        attribute "lastupdate", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    preferences {
        input title: "Settings", description: "To change units and time format, go to the Netatmo Connect App", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        input title: "Information", description: "Your Netatmo station updates the Netatmo servers approximately every 10 minutes. The Netatmo Connect app polls these servers every 5 minutes. If the time of last update is equal to or less than 10 minutes, pressing the refresh button will have no effect", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    }      

	tiles (scale: 2) {
		multiAttributeTile(name:"main", type:"generic", width:6, height:4) {
			tileAttribute("temperature", key: "PRIMARY_CONTROL") {
            	attributeState "temperature",label:'${currentValue}°', icon:"st.Weather.weather2", backgroundColors:[
                	[value: 32, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 92, color: "#d04e00"],
					[value: 98, color: "#bc2323"]
				]
            }
            tileAttribute ("humidity", key: "SECONDARY_CONTROL") {
				attributeState "humidity", label:'Humidity: ${currentValue}%'
			}
		} 
		valueTile("temperature", "device.temperature") {
 			state("temperature", label: '${currentValue}°', icon:"st.Weather.weather2", backgroundColors: [
 				[value: 31, color: "#153591"],
 				[value: 44, color: "#1e9cbb"],
 				[value: 59, color: "#90d2a7"],
 				[value: 74, color: "#44b621"],
 				[value: 84, color: "#f1d801"],
 				[value: 95, color: "#d04e00"],
 				[value: 96, color: "#bc2323"]
 				]
 				)
 		}
        valueTile("min_temp", "min_temp", width: 2, height: 1) {
 			state "min_temp", label: 'Min: ${currentValue}°'
 		}
        valueTile("max_temp", "max_temp", width: 2, height: 1) {
 			state "max_temp", label: 'Max: ${currentValue}°'
 		}        
 		valueTile("humidity", "device.humidity", inactiveLabel: false) {
 			state "humidity", label:'${currentValue}%'
 		}
        valueTile("temp_trend", "temp_trend", width: 4, height: 1) {
 			state "temp_trend", label: 'Temp Trend: ${currentValue}'
 		}
        valueTile("pressure_trend", "pressure_trend", width: 4, height: 1) {
 			state "pressure_trend", label: 'Press Trend: ${currentValue}'
 		}           
 		valueTile("carbonDioxide", "device.carbonDioxide", width: 2, height: 2, inactiveLabel: false) {
 			state "carbonDioxide", label:'${currentValue}ppm', backgroundColors: [
 				[value: 600, color: "#44B621"],
                [value: 999, color: "#ffcc00"],
                [value: 1000, color: "#e86d13"]
 				]
 		}
 		valueTile("soundPressureLevel", "device.soundPressureLevel", width: 2, height: 2, inactiveLabel: false) {
 			state "soundPressureLevel", label:'${currentValue}db'
 		}
 		valueTile("pressure", "device.pressure", width: 2, height: 1, inactiveLabel: false) {
 			state "pressure", label:'${currentValue}'
 		}
        valueTile("units", "units", width: 2, height: 1, inactiveLabel: false) {
 			state "default", label:'${currentValue}'            
 		}
        valueTile("lastupdate", "lastupdate", width: 4, height: 1, inactiveLabel: false) { 			
            state "default", label:"Last updated: " + '${currentValue}' 		
        }
 		valueTile("date_min_temp", "date_min_temp", width: 2, height: 1, inactiveLabel: false) { 			
          state "default", label:'${currentValue}' 		
          }
        valueTile("date_max_temp", "date_max_temp", width: 2, height: 1, inactiveLabel: false) { 			
          state "default", label:'${currentValue}' 		
          }        
        standardTile("refresh", "device.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
 			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
 		}
        
        main(["main"])
 		details(["main","min_temp","date_min_temp","carbonDioxide", "max_temp","date_max_temp", "temp_trend", "soundPressureLevel", "pressure", "units", "pressure_trend", "refresh", "lastupdate"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'pressure' attribute

}

def poll() {
	log.debug "Polling"
    parent.poll()
}

def refresh() {
    log.debug "Refreshing"
	parent.poll()
}