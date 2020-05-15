/**
 *  Hive Heating
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
 *	v2.1 - Introducing button temperature control via improved thermostat multi attribute tile. More responsive temperature control. 
 *		   Improve Boost button behaviour and look.
 *	v2.1.1 - Tweaks to temperature control responsiveness
 *  v2.1.2 - Minor tweaks to main display
 *	v2.1.3 - Allow changing of boost interval amount in device settings.
 *  v2.1.4 - Allow changing of boost temperature in device settings.
 *	v2.1.5 - Option to disable Hive Heating Device for summer. Disable mode stops any automation commands from other smart apps reactivating Hive Heating.
 *	v2.1.5b - Bug fix when desired heat set point is null, control stops working.
 *  v2.1.5c - Fix multitile button behaviour that has changed since ST app 2.1.0. Add colour code to temperature reporting in activity feed.
 *	v2.1.5d - Fix blank temperature readings on Android ST app 
 *	v2.1.5e - Another attempt to fix blank temperature reading on Android.
 *	v2.1.5f - Allow decimal value for boost temperature. Changes to VALUE_CONTROL method to match latest ST docs.
 *	v2.1.5g - Changes to tile display for iOS app v2.1.2
 *
 *	10.09.2016
 *	v2.1.6 - Allow a maximum temperature threshold to be set.
 *	v2.1.6b - Added event for maximum temperature threshold breach.
 *
 *	30.05.2017
 *	v2.2 - Updated to use new Beekeeper API - Huge thanks to Tom Beech!
 *	v2.2b - Bug fix. Refresh bug prevents installation of Hive devices.
 *
 *	30.10.2017
 *	v3.0 - Version refactor to reflect BeeKeeper API update.
 *
 *	08.10.2017
 *	v3.1 - New Smartthing App compatability
 */
preferences 
{
	input( "boostInterval", "number", title: "Boost Interval (minutes)", description: "Boost interval amount in minutes", required: false, defaultValue: 10 )
    input( "boostTemp", "decimal", title: "Boost Temperature (°C)", description: "Boost interval amount in Centigrade", required: false, defaultValue: 22, range: "5..32" )
    input( "maxTempThreshold", "decimal", title: "Max Temperature Threshold (°C)", description: "Set the maximum temperature threshold in Centigrade", required: false, defaultValue: 32, range: "5..32" )
	input( "disableDevice", "bool", title: "Disable Hive Heating?", required: false, defaultValue: false )
}

metadata {
	definition (name: "Hive Heating", namespace: "alyc100", author: "Alex Lee Yuk Cheung", ocfDeviceType: "oic.d.thermostat", mnmn: "SmartThings", vid: "SmartThings-smartthings-Z-Wave_Thermostat") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Thermostat"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
        capability "Health Check"
        
        command "heatingSetpointUp"
		command "heatingSetpointDown"
        command "boostTimeUp"
		command "boostTimeDown"
        command "setThermostatMode"
        command "setHeatingSetpoint"
        command "setTemperatureForSlider"
        command "setBoostLength"
        command "boostButton"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
    
    	multiAttributeTile(name: "thermostat", width: 6, height: 4, type:"thermostat") {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState "default", label: '${currentValue}°', unit:"C", backgroundColor:"#ec6e05"
			}
            
            tileAttribute ("hiveHeating", key: "SECONDARY_CONTROL") {
				attributeState "hiveHeating", label:'${currentValue}'
			}
  			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
    				attributeState("VALUE_UP", action: "heatingSetpointUp")
    				attributeState("VALUE_DOWN", action: "heatingSetpointDown")
  			}
  			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
    				attributeState("idle", backgroundColor:"#bbbbbb")
    				attributeState("heating", backgroundColor:"#ec6e05")
  			}
  			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
    				attributeState("off", label:'Off')
    				attributeState("heat", label:'Manual')
    				attributeState("cool", label:'Manual')
    				attributeState("auto", label:'Schedule')
                    attributeState("emergency heat", label:'Boost')
  			}
  			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                    attributeState "default", label: '${currentValue}°', backgroundColors: [
				// Celsius Color Range
				[value: 0, color: "#50b5dd"],
                [value: 10, color: "#43a575"],
                [value: 13, color: "#c5d11b"],
                [value: 17, color: "#f4961a"],
                [value: 20, color: "#e75928"],
                [value: 25, color: "#d9372b"],
                [value: 29, color: "#b9203b"]
			]}
  			
		}
        
		controlTile("heatSliderControl", "device.desiredHeatSetpoint", "slider", height: 2, width: 3, inactiveLabel: false, range:"(5..32)") {
			state "setHeatingSetpoint", label:'Set temperature to', action:"setTemperatureForSlider"
		}
        
        controlTile("boostSliderControl", "device.boostLength", "slider", height: 2, width: 4, inactiveLabel: false, range:"(10..240)") {
			state "setBoostLength", label:'Set boost length to', action:"setBoostLength"
		}
        
		standardTile("heatingSetpointUp", "device.desiredHeatSetpoint", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
		}

		standardTile("heatingSetpointDown", "device.desiredHeatSetpoint", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
		}
        
        valueTile("device.temperature", "device.temperature", canChangeBackground: true){
			state "default", label: '${currentValue}°', unit:"C", 
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

		valueTile("heatingSetpoint", "device.desiredHeatSetpoint", width: 2, height: 2) {
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
        
        standardTile("boostTimeUp", "device.boostLength", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointUp", label:'  ', action:"boostTimeUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
		}

		standardTile("boostTimeDown", "device.boostLength", width: 1, height: 1, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointDown", label:'  ', action:"boostTimeDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
		}
   
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state "idle", label:'${currentValue}', icon: "st.Weather.weather2"
			state "heating", label:'${currentValue}', icon: "st.Weather.weather2", backgroundColor:"#EC6E05"
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
        
        valueTile("boost", "device.boostLabel", inactiveLabel: false, decoration: "flat", width: 2, height: 2, wordwrap: true) {
			state("default", label:'${currentValue}', action:"boostButton")
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

		main(["thermostatOperatingState"])
        details(["thermostat", "mode_auto", "mode_manual", "mode_off", "heatingSetpointUp", "heatingSetpoint", "boost", "boostTimeUp", "heatingSetpointDown", "boostTimeDown", "refresh"])
        
        //Uncomment below for V1 tile layout
		//details(["thermostat", "mode_auto", "mode_manual", "mode_off", "heatingSetpoint", "heatSliderControl", "boost", "boostSliderControl", "refresh"])
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
    state.boostLength = 60
    state.desiredHeatSetpoint = 7
    sendEvent(name: "checkInterval", value: 10 * 60 + 2 * 60, data: [protocol: "cloud"], displayed: false)
}

void updated() {
	log.debug "Executing 'updated'"
    sendEvent(name: "checkInterval", value: 10 * 60 + 2 * 60, data: [protocol: "cloud"], displayed: false)
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
         
    if (settings.disableDevice == null || settings.disableDevice == false) {
    	//if thermostat is off, set to manual 
        def args
   		if (latestThermostatMode.stringValue == 'off') {
    		args = [
        		mode: "SCHEDULE", target: temp
            ]
		
    	} 
    	else {
    	// {"target":7.5}
    		args = [
        		target: temp
        	]               
    	}
        //def type = device.type;
    	def resp = parent.apiPOST("/nodes/${device.deviceNetworkId}", args)    	
    }
    runIn(4, refresh)
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
    refreshBoostLabel()  
}

def getBoostIntervalValue() {
	if (settings.boostInterval == null) {
    	return 10
    } 
    return settings.boostInterval.toInteger()
}

def getBoostTempValue() {
	if (settings.boostTemp == null) {
    	return "22"
    } 
    return settings.boostTemp
}

def getMaxTempThreshold() {
	if (settings.maxTempThreshold == null) {
    	return "32"
    } 
    return settings.maxTempThreshold
}

def boostTimeUp() {
	log.debug "Executing 'boostTimeUp'"
    //Round down result
    int boostIntervalValue = getBoostIntervalValue()
    def newBoostLength = (state.boostLength + boostIntervalValue) - (state.boostLength % boostIntervalValue)
	setBoostLength(newBoostLength)
}

def boostTimeDown() {
	log.debug "Executing 'boostTimeDown'"
    //Round down result
    int boostIntervalValue = getBoostIntervalValue()
    def newBoostLength = (state.boostLength - boostIntervalValue) - (state.boostLength % boostIntervalValue)
	setBoostLength(newBoostLength)
}

def boostButton() {
	log.debug "Executing 'boostButton'"
	setThermostatMode('emergency heat')
}

def setHeatingSetpointToDesired() {
	setHeatingSetpoint(state.newSetpoint)
}

def setNewSetPointValue(newSetPointValue) {
	log.debug "Executing 'setNewSetPointValue' with value $newSetPointValue"
	unschedule('setHeatingSetpointToDesired')
    state.newSetpoint = newSetPointValue
    state.desiredHeatSetpoint = state.newSetpoint
	sendEvent("name":"desiredHeatSetpoint", "value": state.desiredHeatSetpoint, displayed: false)
	log.debug "Setting heat set point up to: ${state.newSetpoint}"
    runIn(3, setHeatingSetpointToDesired)
}

def heatingSetpointUp(){
	log.debug "Executing 'heatingSetpointUp'"
	setNewSetPointValue(getHeatTemp().toInteger() + 1)
}

def heatingSetpointDown(){
	log.debug "Executing 'heatingSetpointDown'"
	setNewSetPointValue(getHeatTemp().toInteger() - 1)
}

def setTemperatureForSlider(value) {
	log.debug "Executing 'setTemperatureForSlider with $value'"
	setNewSetPointValue(value)  
}

def getHeatTemp() { 
	return state.desiredHeatSetpoint == null ? device.currentValue("heatingSetpoint") : state.desiredHeatSetpoint
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
	if (settings.disableDevice == null || settings.disableDevice == false) {
		mode = mode == 'cool' ? 'heat' : mode
		log.debug "Executing 'setThermostatMode with mode $mode'"
    	def args = [
        		mode: "SCHEDULE"
            ]
    	if (mode == 'off') {
     		args = [
        		mode: "OFF"
            ]
    	} else if (mode == 'heat') {
        	//mode": "MANUAL", "target": 20
    		args = [
        		mode: "MANUAL", 
                target: 20
            ]
    	} else if (mode == 'emergency heat') {  
    		if (state.boostLength == null || state.boostLength == '')
        	{
        		state.boostLength = 60
            	sendEvent("name":"boostLength", "value": 60, displayed: true)
        	}
    		//"mode": "BOOST","boost": 60,"target": 22
			args = [
            	mode: "BOOST",
                boost: state.boostLength,
                target: getBoostTempValue()
        	]
   		}
    
    	def resp = parent.apiPOST("/nodes/${device.deviceNetworkId}", args)
		mode = mode == 'range' ? 'auto' : mode    	
    }
    runIn(4, refresh)
}

def refreshBoostLabel() {
	def boostLabel = "Start\n$state.boostLength Min Boost"
    def latestThermostatMode = device.latestState('thermostatMode')  
    if (latestThermostatMode.stringValue == 'emergency heat' ) {
    	boostLabel = "Restart\n$state.boostLength Min Boost"
    }
    sendEvent("name":"boostLabel", "value": boostLabel, displayed: false)
}

def poll() {
	log.debug "Executing 'poll'"
	def currentDevice = parent.getDeviceStatus(device.deviceNetworkId)
	if (currentDevice == []) {
		return []
	}
    log.debug "$device.name status: $currentDevice"
        //Construct status message
        def statusMsg = ""
        
        //Boost button label
        if (state.boostLength == null || state.boostLength == '')
        {
        	state.boostLength = 60
            sendEvent("name":"boostLength", "value": 60, displayed: true)
        }
    	def boostLabel = "Start\n$state.boostLength Min Boost"
        
        // get temperature status
        def temperature = currentDevice.props.temperature
        def heatingSetpoint = currentDevice.state.target as Double
        
        log.debug "Got Temperature ${temperature} on device ${currentDevice.state.name}"
        log.debug "Got Setpoint ${heatingSetpoint} on device ${currentDevice.state.name}"
        
        //Check heating set point against maximum threshold value.
        log.debug "Maximum temperature threshold set to: " + getMaxTempThreshold()
        if ((getMaxTempThreshold() as BigDecimal) < (heatingSetpoint as BigDecimal))
        {
        	log.debug "Maximum temperature threshold exceeded. " + heatingSetpoint + " is higher than " + getMaxTempThreshold()
            sendEvent(name: 'maxtempthresholdbreach', value: heatingSetpoint, unit: "C", displayed: false)
        	//Force temperature threshold to Hive API.
        	def args = [
        		target: getMaxTempThreshold()
            ]               
    
    		parent.apiPOST("/nodes/${device.deviceNetworkId}", args)   
            heatingSetpoint = String.format("%2.1f", getMaxTempThreshold())           
        }
        
        // convert temperature reading of 1 degree to 7 as Hive app does
        if (heatingSetpoint == "1.0") {
        	heatingSetpoint = "7.0"
        }
        sendEvent(name: 'temperature', value: temperature, unit: "C", state: "heat")
        sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: "C", state: "heat")
        sendEvent(name: 'coolingSetpoint', value: heatingSetpoint, unit: "C", state: "heat")
        sendEvent(name: 'thermostatSetpoint', value: heatingSetpoint, unit: "C", state: "heat", displayed: false)
        sendEvent(name: 'thermostatFanMode', value: "off", displayed: false)
        
        state.desiredHeatSetpoint = heatingSetpoint
        sendEvent("name":"desiredHeatSetpoint", "value": state.desiredHeatSetpoint, unit: "C", displayed: false)
        
        // determine hive operating mode
        def mode = currentDevice.state.mode.toLowerCase()
        
        //If Hive heating device is set to disabled, then force off if not already off.
        if (settings.disableDevice != null && settings.disableDevice == true && mode != "off") {
        	def args = [
        		mode: "OFF"
            ]
        	parent.apiPOST("/nodes/${device.deviceNetworkId}", args)
            mode = 'off'
        } 
        else if (mode == "boost") {
        	mode = 'emergency heat'          
            def boostTime = currentDevice.state.boost
            boostLabel = "Restart\n$state.boostLength Min Boost"
            statusMsg = "Boost " + boostTime + "min"
            sendEvent("name":"boostTimeRemaining", "value": boostTime + " mins")
        }
        else if (mode == "manual") {
        	mode = 'heat'
            statusMsg = statusMsg + " Manual"
        }
        else if (mode == "off") {
        	mode = 'off'
            statusMsg = statusMsg + " Off"
        }
        else {
        	mode = 'auto'
        	statusMsg = statusMsg + " Schedule"
        }
        
        if (settings.disableDevice != null && settings.disableDevice == true) {
        	statusMsg = "DISABLED"
        }
        
        sendEvent(name: 'thermostatMode', value: mode) 
        
        // determine if Hive heating relay is on
        def stateHeatingRelay = (heatingSetpoint as BigDecimal) > (temperature as BigDecimal)
        
        log.debug "stateHeatingRelay: $stateHeatingRelay"
        
        if (stateHeatingRelay) {
        	sendEvent(name: 'thermostatOperatingState', value: "heating")
        }       
        else {
        	sendEvent(name: 'thermostatOperatingState', value: "idle")
        }  
               
        sendEvent("name":"hiveHeating", "value": statusMsg, displayed: false)  
        sendEvent("name":"boostLabel", "value": boostLabel, displayed: false)
     
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
}