/**
 *  Tesla
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
	definition (name: "Tesla", namespace: "trentfoley", author: "Trent Foley") {
		capability "Actuator"
		capability "Battery"
		capability "Geolocation"
		capability "Lock"
		capability "Motion Sensor"
		capability "Presence Sensor"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Thermostat Mode"

		attribute "vehicleState", "string"
        attribute "vin", "string"

		command "wake"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		valueTile("battery", "device.battery", canChangeIcon: true, width: 2, height: 2, icon: "st.Transportation.transportation6") { // st.tesla.tesla-car
            state("battery", label:'${currentValue}%',
                backgroundColors:[
                    [value: 75, color: "#153591"],
                    [value: 65, color: "#1e9cbb"],
                    [value: 55, color: "#90d2a7"],
                    [value: 45, color: "#44b621"],
                    [value: 35, color: "#f1d801"],
                    [value: 25, color: "#d04e00"],
                    [value: 15, color: "#bc2323"]
                ]
            )
        }
        
        standardTile("motion", "device.motion") {
            state "inactive", label: "Parked", icon: "st.motion.acceleration.inactive", backgroundColor: "#ffffff"
            state "active", label: "Driving", icon: "st.motion.acceleration.active", backgroundColor: "#00a0dc"
        }
        standardTile("vehicleState", "device.vehicleState") {
            state "asleep", label: "Asleep", backgroundColor: "#eeeeee", action: "wake"
            state "online", label: "Online", backgroundColor: "#00a0dc"
        }
		standardTile("refresh", "device.vehicleState", decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("vin", "device.vin", width: 3, height: 1) {
			state "vin", label: '${currentValue}'
		}
        
        
        main("battery")
        details("battery", "motion", "vehicleState", "refresh", "vin")
	}
}

def initialize() {
	log.debug "Executing 'initialize'"
    runEvery5Minutes(refresh)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'latitude' attribute
	// TODO: handle 'longitude' attribute
	// TODO: handle 'method' attribute
	// TODO: handle 'accuracy' attribute
	// TODO: handle 'altitudeAccuracy' attribute
	// TODO: handle 'heading' attribute
	// TODO: handle 'speed' attribute
	// TODO: handle 'lastUpdateTime' attribute
	// TODO: handle 'lock' attribute
	// TODO: handle 'motion' attribute
	// TODO: handle 'presence' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'supportedThermostatModes' attribute

}

private processData(data) {
	if(data) {
    	log.debug "processData: ${data}"
        
    	sendEvent(name: "vehicleState", value: data.vehicleState)
        sendEvent(name: "motion", value: data.motion)
        sendEvent(name: "vin", value: data.vin)
        
        if (data.chargeState) {
        	sendEvent(name: "battery", value: data.chargeState.battery)
        }
	} else {
    	log.error "No data found for ${device.deviceNetworkId}"
    }
}

def refresh() {
	log.debug "Executing 'refresh'"
	
    def data = parent.refresh(this)
	processData(data)
}

def wake() {
	log.debug "Executing 'wake'"
	def data = parent.wake(this)
    processData(data)
}

def lock() {
	log.debug "Executing 'lock'"
	// TODO: handle 'lock' command
}

def unlock() {
	log.debug "Executing 'unlock'"
	// TODO: handle 'unlock' command
}

def heat() {
	log.debug "Executing 'heat'"
	// TODO: handle 'heat' command
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// TODO: handle 'emergencyHeat' command
}

def cool() {
	log.debug "Executing 'cool'"
	// TODO: handle 'cool' command
}

def auto() {
	log.debug "Executing 'auto'"
	// TODO: handle 'auto' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'heat' command
}

def setThermostatMode() {
	log.debug "Executing 'setThermostatMode'"
	// TODO: handle 'setThermostatMode' command
}