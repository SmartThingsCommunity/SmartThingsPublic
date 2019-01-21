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
        capability "Thermostat Setpoint"

		attribute "state", "string"
        attribute "vin", "string"
        attribute "odometer", "number"
        attribute "batteryRange", "number"
        attribute "chargingState", "string"

		command "wake"
        command "setThermostatSetpoint"
        command "startCharge"
        command "stopCharge"
        command "openFrontTrunk"
        command "openRearTrunk"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		valueTile("main", "device.battery", canChangeBackground: true) {
            state("default", label:'${currentValue}%',
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
        standardTile("state", "device.state", width: 2, height: 2) {
            state "asleep", label: "Asleep", backgroundColor: "#eeeeee", action: "wake", icon: "st.Bedroom.bedroom2"
            state "online", label: "Online", backgroundColor: "#00a0dc", icon: "st.tesla.tesla-front"
        }
        valueTile("help", "device.state", width: 4, height: 2) {
            state "default", label: 'In order to use the various commands, your vehicle must first be awakened.'
		}
        standardTile("chargingState", "device.chargingState", width: 2, height: 2) {
            state "default", label: '${currentValue}', icon: "st.Transportation.transportation6" //, backgroundColor: "#cccccc"
            state "stopped", label: '${currentValue}', icon: "st.Transportation.transportation6", action: "startCharge", backgroundColor: "#ffffff"
            state "charging", label: '${currentValue}', icon: "st.Transportation.transportation6", action: "stopCharge", backgroundColor: "#00a0dc"
            state "complete", label: '${currentValue}', icon: "st.Transportation.transportation6", backgroundColor: "#44b621"
        }
        valueTile("battery", "device.battery", width: 2, height: 1) {
            state("default", label:'${currentValue}% battery' /*
                backgroundColors:[
                    [value: 75, color: "#153591"],
                    [value: 65, color: "#1e9cbb"],
                    [value: 55, color: "#90d2a7"],
                    [value: 45, color: "#44b621"],
                    [value: 35, color: "#f1d801"],
                    [value: 25, color: "#d04e00"],
                    [value: 15, color: "#bc2323"]
                ] */
            )
        }
        valueTile("batteryRange", "device.batteryRange", width: 2, height: 1) {
            state("default", label:'${currentValue} mi range')
        }
        standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2) {
        	state "auto", label: "On", action: "off", icon: "st.tesla.tesla-hvac", backgroundColor: "#00a0dc"
            state "off", label: "Off", action: "auto", icon: "st.tesla.tesla-hvac", backgroundColor: "#ffffff"
        }
        controlTile("thermostatSetpoint", "device.thermostatSetpoint", "slider", width: 2, height: 2, range:"(60..85)") {
            state "default", action:"setThermostatSetpoint"
        }
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
            state("temperature", label: '${currentValue}°', unit:"dF",
                backgroundColors:[
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
        standardTile("lock", "device.lock", width: 2, height: 2) {
            state "locked", label: "Locked", action: "unlock", icon: "st.tesla.tesla-locked", backgroundColor: "#ffffff"
            state "unlocked", label: "Unlocked", action: "lock", icon: "st.tesla.tesla-unlocked", backgroundColor: "#00a0dc"
        }
        valueTile("trunkLabel", "device.state", width: 2, height: 1) {
			state "default", label: 'Open Trunk >'
		}
        standardTile("frontTrunk", "device.state") {
            state "default", label: "Front", action: "openFrontTrunk"
        }
        standardTile("rearTrunk", "device.state") {
        	state "default", label: "Rear", action: "openRearTrunk"
        }
		standardTile("motion", "device.motion", width: 2, height: 1) {
            state "inactive", label: "Parked", icon: "st.motion.acceleration.inactive"
            state "active", label: "Driving", icon: "st.motion.acceleration.active"
        }
        valueTile("speed", "device.speed", width: 2, height: 1) {
			state "default", label: '${currentValue} mph'
		}
		standardTile("refresh", "device.state", decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("odometer", "device.odometer", width: 2, height: 1) {
			state "default", label: 'ODO    ${currentValue}'
		}
        valueTile("vin", "device.vin", width: 4, height: 1) {
			state "default", label: '${currentValue}'
		}
        
        main("main")
        details("state", "help", "chargingState", "battery", "motion", "batteryRange", "speed", "thermostatMode", "thermostatSetpoint", "temperature", "lock", "trunkLabel", "frontTrunk", "rearTrunk", "odometer", "refresh", "vin")
	}
}

def initialize() {
	log.debug "Executing 'initialize'"
    
    sendEvent(name: "supportedThermostatModes", value: ["auto", "off"])
    
    runEvery15Minutes(refresh)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

private processData(data) {
	if(data) {
    	log.debug "processData: ${data}"
        
    	sendEvent(name: "state", value: data.state)
        sendEvent(name: "motion", value: data.motion)
        sendEvent(name: "speed", value: data.speed, unit: "mph")
        sendEvent(name: "vin", value: data.vin)
        sendEvent(name: "thermostatMode", value: data.thermostatMode)
        
        if (data.chargeState) {
        	sendEvent(name: "battery", value: data.chargeState.battery)
            sendEvent(name: "batteryRange", value: data.chargeState.batteryRange)
            sendEvent(name: "chargingState", value: data.chargeState.chargingState)
        }
        
        if (data.driveState) {
        	sendEvent(name: "latitude", value: data.driveState.latitude)
			sendEvent(name: "longitude", value: data.driveState.longitude)
            sendEvent(name: "method", value: data.driveState.method)
            sendEvent(name: "heading", value: data.driveState.heading)
            sendEvent(name: "lastUpdateTime", value: data.driveState.lastUpdateTime)
        }
        
        if (data.vehicleState) {
        	sendEvent(name: "presence", value: data.vehicleState.presence)
            sendEvent(name: "lock", value: data.vehicleState.lock)
            sendEvent(name: "odometer", value: data.vehicleState.odometer)
        }
        
        if (data.climateState) {
        	sendEvent(name: "temperature", value: data.climateState.temperature)
            sendEvent(name: "thermostatSetpoint", value: data.climateState.thermostatSetpoint)
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
    runIn(30, refresh)
}

def lock() {
	log.debug "Executing 'lock'"
	def result = parent.lock(this)
    if (result) { refresh() }
}

def unlock() {
	log.debug "Executing 'unlock'"
	def result = parent.unlock(this)
    if (result) { refresh() }
}

def auto() {
	log.debug "Executing 'auto'"
	def result = parent.climateAuto(this)
    if (result) { refresh() }
}

def off() {
	log.debug "Executing 'off'"
	def result = parent.climateOff(this)
    if (result) { refresh() }
}

def heat() {
	log.debug "Executing 'heat'"
	// Not supported
}

def emergencyHeat() {
	log.debug "Executing 'emergencyHeat'"
	// Not supported
}

def cool() {
	log.debug "Executing 'cool'"
	// Not supported
}

def setThermostatMode(mode) {
	log.debug "Executing 'setThermostatMode'"
	switch (mode) {
    	case "auto":
        	auto()
            break
        case "off":
        	off()
            break
        default:
        	log.error "setThermostatMode: Only thermostat modes Auto and Off are supported"
    }
}

def setThermostatSetpoint(setpoint) {
	log.debug "Executing 'setThermostatSetpoint'"
	def result = parent.setThermostatSetpoint(this, setpoint)
    if (result) { refresh() }
}

def startCharge() {
	log.debug "Executing 'startCharge'"
    def result = parent.startCharge(this)
    if (result) { refresh() }
}

def stopCharge() {
	log.debug "Executing 'stopCharge'"
    def result = parent.stopCharge(this)
    if (result) { refresh() }
}

def openFrontTrunk() {
	log.debug "Executing 'openFrontTrunk'"
    def result = parent.openTrunk(this, "front")
    // if (result) { refresh() }
}

def openRearTrunk() {
	log.debug "Executing 'openRearTrunk'"
    def result = parent.openTrunk(this, "rear")
    // if (result) { refresh() }
}