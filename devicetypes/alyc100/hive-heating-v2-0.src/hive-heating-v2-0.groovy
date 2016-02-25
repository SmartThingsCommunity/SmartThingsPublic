/**
 *  Hive Heating V2.0
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
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
 *
 *  VERSION HISTORY
 *  25.02.2016
 *  v2.0 BETA - Initial Release
 */
 
metadata {
	definition (name: "Hive Heating V2.0", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Thermostat"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
        
        command "heatingSetpointUp"
		command "heatingSetpointDown"
        command "setThermostatMode"
        command "setHeatingSetpoint"
        command "setBoostLength"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {

		multiAttributeTile(name: "thermostat", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState "default", label: '${currentValue}°', unit:"C", backgroundColors: [
				// Celsius Color Range
				[value: 0, color: "#50b5dd"],
                [value: 10, color: "#43a575"],
                [value: 13, color: "#c5d11b"],
                [value: 17, color: "#f4961a"],
                [value: 20, color: "#e75928"],
                [value: 25, color: "#d9372b"],
                [value: 29, color: "#b9203b"]
			]}
            tileAttribute ("hiveHeating", key: "SECONDARY_CONTROL") {
				attributeState "hiveHeating", label:'${currentValue}'
			}
		}
        
        valueTile("thermostat_small", "device.temperature", width: 4, height: 4) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
                [value: 0, color: "#50b5dd"],
                [value: 10, color: "#43a575"],
                [value: 13, color: "#c5d11b"],
                [value: 17, color: "#f4961a"],
                [value: 20, color: "#e75928"],
                [value: 25, color: "#d9372b"],
                [value: 29, color: "#b9203b"]
            ]
		}
        
        standardTile("thermostat_main", "device.thermostatOperatingState", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state "idle", label:'${currentValue}', icon: "st.Weather.weather2"
			state "heating", label:'${currentValue}', icon: "st.Weather.weather2", backgroundColor:"#EC6E05"
		}
        
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(5..32)") {
			state "setHeatingSetpoint", label:'Set temperature to', action:"setHeatingSetpoint"
		}
        
        controlTile("boostSliderControl", "device.boostLength", "slider", height: 2, width: 4, inactiveLabel: false, range:"(10..240)") {
			state "setBoostLength", label:'Set boost length to', action:"setBoostLength"
		}
        
		standardTile("heatingSetpointUp", "device.heatingSetpoint", width: 2, height: 2, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
		}

		standardTile("heatingSetpointDown", "device.heatingSetpoint", width: 2, height: 2, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
		}

		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
                [value: 0, color: "#50b5dd"],
                [value: 10, color: "#43a575"],
                [value: 13, color: "#c5d11b"],
                [value: 17, color: "#f4961a"],
                [value: 20, color: "#e75928"],
                [value: 25, color: "#d9372b"],
                [value: 29, color: "#b9203b"]
            ]
		}
   
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state "idle", action:"polling.poll", label:'${name}', icon: "st.sonos.pause-icon"
			state "heating", action:"polling.poll", label:'  ', icon: "st.thermostat.heating", backgroundColor:"#EC6E05"
		}
        
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state("auto", label: "SCHEDULED", icon:"st.Office.office7")
			state("off", label: "OFF", icon:"st.thermostat.heating-cooling-off")
			state("heat", label: "MANUAL", icon:"st.Weather.weather2")
			state("emergency heat", label: "BOOST", icon:"st.Health & Wellness.health7")
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}
        
        valueTile("boost", "device.boostLabel", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'${currentValue}', action:"emergencyHeat")
		}
        
        standardTile("mode_auto", "device.mode_auto", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"auto", label:'Schedule', icon:"st.Office.office7"
    	}
        
        standardTile("mode_manual", "device.mode_manual", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"heat", label:'Manual', icon:"st.Weather.weather2"
   	 	}
        
        standardTile("mode_off", "device.mode_off", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"off", icon:"st.thermostat.heating-cooling-off"
   	 	}

		main(["thermostat_main"])
		details(["thermostat", "mode_auto", "mode_manual", "mode_off", "heatingSetpoint", "heatSliderControl", "boost", "boostSliderControl", "refresh"])		
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
}

def installed() {
	log.debug "Executing 'installed'"
	// execute handlerMethod every 10 minutes.
    state.boostLength = 60
}

// handle commands
def setHeatingSetpoint(temp) {
	log.debug "Executing 'setHeatingSetpoint with temp $temp'"
	def latestThermostatMode = device.latestState('thermostatMode')
    
    if (temp < 5) {
		temp = 5
	}
	if (temp > 32) {
		temp = 32
	}
    
	
    //if thermostat is off, set to manual    
   	if (latestThermostatMode.stringValue == 'off') {
    	def args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: true]]]]
            ]
		def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
		if (resp.status != 200) {
			log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
			return []
		}
    }
    
    // {"nodes":[{"attributes":{"targetHeatTemperature":{"targetValue":11}}}]}    
    def args = [
        	nodes: [	[attributes: [targetHeatTemperature: [targetValue: temp]]]]
            ]               
    
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
    else {
        runIn(4, refresh)
	}
}

def setBoostLength(minutes) {
	log.debug "Executing 'setBoostLength with length $minutes minutes'"
    if (minutes < 10) {
		minutes = 10
	}
	if (minutes > 240) {
		minutes = 240
	}
    state.boostLength = minutes
    sendEvent("name":"boostLength", "value": state.boostLength, displayed: true)
    
    def latestThermostatMode = device.latestState('thermostatMode')
    
    //If already in BOOST mode, send updated boost length to Hive.
	if (latestThermostatMode.stringValue == 'emergency heat') {
		setThermostatMode('emergency heat')
    }
    else {
    	refresh()
    }
    
    
    
}

def heatingSetpointUp(){
	log.debug "Executing 'heatingSetpointUp'"
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	log.debug "Executing 'heatingSetpointDown'"
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heat set point down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def off() {
	setThermostatMode('off')
}

def heat() {
	setThermostatMode('heat')
}

def emergencyHeat() {
	log.debug "Executing 'boost'"
	
    def latestThermostatMode = device.latestState('thermostatMode')
    
    //Don't do if already in BOOST mode.
	if (latestThermostatMode.stringValue != 'emergency heat') {
		setThermostatMode('emergency heat')
    }
    else {
    	log.debug "Already in boost mode."
    }

}

def auto() {
	setThermostatMode('auto')
}

def setThermostatMode(mode) {
	mode = mode == 'cool' ? 'heat' : mode
	log.debug "Executing 'setThermostatMode with mode $mode'"
    def args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], scheduleLockDuration: [targetValue: 0], activeScheduleLock: [targetValue: false]]]]
            ]
    if (mode == 'off') {
     	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "OFF"], scheduleLockDuration: [targetValue: 0], activeScheduleLock: [targetValue: true]]]]
            ]
    } else if (mode == 'heat') {
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"HEAT"},"activeScheduleLock":{"targetValue":true}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], scheduleLockDuration: [targetValue: 0], activeScheduleLock: [targetValue: true]]]]
            ]
    } else if (mode == 'emergency heat') {  
    	if (state.boostLength == null || state.boostLength == '')
        {
        	state.boostLength = 60
            sendEvent("name":"boostLength", "value": 60, displayed: true)
        }
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"BOOST"},"scheduleLockDuration":{"targetValue":30},"targetHeatTemperature":{"targetValue":22}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "BOOST"], scheduleLockDuration: [targetValue: state.boostLength], targetHeatTemperature: [targetValue: "22"]]]]
            ]
    }
    
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
    else {
		mode = mode == 'range' ? 'auto' : mode
        runIn(4, refresh)
	}
}

def poll() {
	log.debug "Executing 'poll'"
	def resp = parent.apiGET("/nodes/${device.deviceNetworkId}")
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
    
    	data.nodes = resp.data.nodes
        
        //Construct status message
        def statusMsg = "Currently"
        
        //Boost button label
        if (state.boostLength == null || state.boostLength == '')
        {
        	state.boostLength = 60
            sendEvent("name":"boostLength", "value": 60, displayed: true)
        }
    	def boostLabel = "Start\n$state.boostLength Min Boost"
        
        // get temperature status
        def temperature = data.nodes.attributes.temperature.reportedValue[0]
        def heatingSetpoint = data.nodes.attributes.targetHeatTemperature.reportedValue[0]
        temperature = String.format("%2.1f",temperature)
       	heatingSetpoint = convertTemperatureIfNeeded(heatingSetpoint, "C", 1)
        
        // convert temperature reading of 1 degree to 7 as Hive app does
        if (heatingSetpoint == "1.0") {
        	heatingSetpoint = "7.0"
        }
        
        sendEvent(name: 'temperature', value: temperature, unit: "C", state: "heat")
        sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: "C", state: "heat")
        sendEvent(name: 'thermostatSetpoint', value: heatingSetpoint, unit: "C", state: "heat", displayed: false)
        sendEvent(name: 'thermostatFanMode', value: "off", displayed: false)
        
        // determine hive operating mode
        def activeHeatCoolMode = data.nodes.attributes.activeHeatCoolMode.reportedValue[0]
        def activeScheduleLock = data.nodes.attributes.activeScheduleLock.targetValue[0]
        
        log.debug "activeHeatCoolMode: $activeHeatCoolMode"
        log.debug "activeScheduleLock: $activeScheduleLock"
        
        def mode = 'auto'
        
        if (activeHeatCoolMode == "OFF") {
        	mode = 'off'
            statusMsg = statusMsg + " set to OFF"
        }
        else if (activeHeatCoolMode == "BOOST") {
        	mode = 'emergency heat'
            statusMsg = statusMsg + " set to BOOST"
            def boostTime = data.nodes.attributes.scheduleLockDuration.reportedValue[0]
            boostLabel = "Boosting for \n" + boostTime + " mins"
            sendEvent("name":"boostTimeRemaining", "value": boostTime + " mins")
        }
        else if (activeHeatCoolMode == "HEAT" && activeScheduleLock) {
        	mode = 'heat'
            statusMsg = statusMsg + " set to MANUAL"
        }
        else {
        	statusMsg = statusMsg + " set to SCHEDULE"
        }
        sendEvent(name: 'thermostatMode', value: mode) 
        
        // determine if Hive heating relay is on
        def stateHeatingRelay = data.nodes.attributes.stateHeatingRelay.reportedValue[0]
        
        log.debug "stateHeatingRelay: $stateHeatingRelay"
        
        if (stateHeatingRelay == "ON") {
        	sendEvent(name: 'thermostatOperatingState', value: "heating")
            statusMsg = statusMsg + " and is HEATING"
        }       
        else {
        	sendEvent(name: 'thermostatOperatingState', value: "idle")
            statusMsg = statusMsg + " and is IDLE"
        }  
               
        sendEvent("name":"hiveHeating", "value": statusMsg, displayed: false)  
        sendEvent("name":"boostLabel", "value": boostLabel, displayed: false)
    
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}