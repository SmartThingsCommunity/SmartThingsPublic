/**
 *  Hive Hot Water V2.0
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
 *	VERSION HISTORY
 *    25.02.2016
 *    v2.0 BETA - Initial Release
 *	  v2.0b - Fix blank temperature readings on Android ST app
 */

metadata {
	definition (name: "Hive Hot Water V2.0", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
        capability "Thermostat"
		capability "Thermostat Mode"
        
        command "setThermostatMode"
        command "setBoostLength"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {

		multiAttributeTile(name: "hotWaterRelay", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.thermostatOperatingState", key:"PRIMARY_CONTROL"){
				attributeState "heating", icon: "st.thermostat.heat", backgroundColor: "#EC6E05"
  				attributeState "idle", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#bbbbbb"
            }
            tileAttribute ("hiveHotWater", key: "SECONDARY_CONTROL") {
				attributeState "hiveHotWater", label:'${currentValue}'
			}
		}
        
        standardTile("hotWaterRelay_main", "device.thermostatOperatingState", inactiveLabel: true, width: 3, height: 3) {
			state( "heating", label:'${currentValue}', icon: "st.Bath.bath6", backgroundColor: "#EC6E05")
  			state( "idle", label:'${currentValue}', icon: "st.Bath.bath6", backgroundColor: "#ffffff")
		}
        
        standardTile("hotWaterRelay_small", "device.thermostatOperatingState", inactiveLabel: true, width: 3, height: 3) {
			state( "heating", icon: "st.thermostat.heat", backgroundColor: "#EC6E05")
  			state( "idle", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#ffffff")
		}

        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 3, height: 3) {
			state("auto", label: "SCHEDULED", action:"heat", icon:"st.Bath.bath6")
			state("off", label: "OFF", action:"auto", icon:"st.Bath.bath6")
			state("heat", label: "ON", action:"off", icon:"st.Bath.bath6")
			state("emergency heat", label: "BOOST", action:"auto", icon:"st.Bath.bath6")
		}
        

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}
        
        valueTile("boost", "device.boostLabel", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'${currentValue}', action:"emergencyHeat")
		}
        
        controlTile("boostSliderControl", "device.boostLength", "slider", height: 2, width: 4, inactiveLabel: false, range:"(10..240)") {
			state "setBoostLength", label:'Set boost length to', action:"setBoostLength"
		}
        
        standardTile("mode_auto", "device.mode_auto", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"auto", label:'Schedule', icon:"st.Office.office7"
    	}
        
        standardTile("mode_manual", "device.mode_manual", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"heat", label:'On', icon:"st.Weather.weather2"
   	 	}
        
        standardTile("mode_off", "device.mode_off", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"off", icon:"st.thermostat.heating-cooling-off"
   	 	}

		main(["hotWaterRelay_main"])	
		details(["hotWaterRelay", "mode_auto", "mode_manual", "mode_off", "boost", "boostSliderControl", "refresh"])

	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute
	// TODO: handle 'thermostatMode' attribute

}

def installed() {
	log.debug "Executing 'installed'"
    state.boostLength = 60
}

// handle commands
def setHeatingSetpoint(temp) {
	//Not implemented	
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
	//Not implemented
}

def heatingSetpointDown(){
	//Not implemented
}

def on() {
	log.debug "Executing 'on'"
	setThermostatMode('heat')
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
    def args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: false]]]]
            ]
    if (mode == 'off') {
     	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "OFF"]]]]
            ]
    } else if (mode == 'heat') {
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"HEAT"},"activeScheduleLock":{"targetValue":true},"targetHeatTemperature":{"targetValue":99}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: true], targetHeatTemperature: [targetValue: "99"]]]]
            ]
    } else if (mode == 'emergency heat') {
    	if (state.boostLength == null || state.boostLength == '')
        {
        	state.boostLength = 60
            sendEvent("name":"boostLength", "value": 60, displayed: true)
        }
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"BOOST"},"scheduleLockDuration":{"targetValue":30},"targetHeatTemperature":{"targetValue":99}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "BOOST"], scheduleLockDuration: [targetValue: state.boostLength], targetHeatTemperature: [targetValue: "99"]]]]
            ]
    }
    
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
    else {
		mode = mode == 'range' ? 'auto' : mode
        runIn(3, refresh)
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
        
        // determine hive hot water operating mode
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
            statusMsg = statusMsg + " set to ON"
        }
        else {
        	statusMsg = statusMsg + " set to SCHEDULE"
        }
        
        sendEvent(name: 'thermostatMode', value: mode) 
        
        // determine if Hive hot water relay is on
        def stateHotWaterRelay = data.nodes.attributes.stateHotWaterRelay.reportedValue[0]
        
        log.debug "stateHotWaterRelay: $stateHotWaterRelay"
        
        if (stateHotWaterRelay == "ON") {
        	sendEvent(name: 'temperature', value: 99, unit: "C", state: "heat", displayed: false)
        	sendEvent(name: 'heatingSetpoint', value: 99, unit: "C", state: "heat", displayed: false)
            sendEvent(name: 'thermostatOperatingState', value: "heating")
            statusMsg = statusMsg + " and is HEATING"
        }       
        else {
        	sendEvent(name: 'temperature', value: 0, unit: "C", state: "heat", displayed: false)
       	 	sendEvent(name: 'heatingSetpoint', value: 0, unit: "C", state: "heat", displayed: false)
            sendEvent(name: 'thermostatOperatingState', value: "idle")
            statusMsg = statusMsg + " and is IDLE"
        }
        sendEvent("name":"hiveHotWater", "value":statusMsg, displayed: false)
        sendEvent("name":"boostLabel", "value": boostLabel, displayed: false)
        
    
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}