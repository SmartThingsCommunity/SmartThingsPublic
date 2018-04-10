/**
 *  MiHome eTRV
 *
 *  Copyright 2016 Alex Lee Yuk Cheung
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
 *	VERSION HISTORY
 *  			2.6 - added google voice command compatability 
 *						Turn off - "turn off **device name/room** termostat" Save last settings and turn off (aka 12deg) 
 *						Turn on - "set **device name/room** termostat to heat" Resume
 *						Set Temprature - "set **device name/room** temproture/thermostat to **number**"
 *						Quiry Temprature - "what is the temprature in the **device name/room**" - response current temp and setpoint
 *						Up/down 3 deg - "turn up/down **device name/room**"
 *						boost!!!
 *				2.5 - major review to move schdualing into the DH 
 *	21.02.2018	2.1	- re run upto 5 times when set temp is not 200
 *	23.11.2016:	2.0 - Remove BETA status.
 * 
 *	07.11.2016: 2.0 BETA Release 1.1 - Allow icon to be changed.
 *	07.11.2016: 2.0 BETA Release 1 - Version number update to match Smartapp.
 *
 *	10.01.2016: 1.1.2 - Bug fix to Boost mode not executing.
 *
 *	10.01.2016: 1.1.1 - Fixed stopBoost always returning to 'on' mode.
 *
 *	09.01.2016: 1.1 - Added BETA Boost Capability
 *
 *  09.01.2016: 1.0 - Initial Release
 *
 */
 
metadata {
	definition (name: "MiHome eTRV", namespace: "alyc100", author: "Alex Lee Yuk Cheung", type: "action.devices.types.THERMOSTAT") {
		//capability "Actuator"
		//capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Thermostat"
        capability "Thermostat Mode"
		capability "Thermostat Heating Setpoint"
		capability "Switch"
        capability "Battery"
        capability "THERMOSTAT"
        //capability "TemperatureSetting"
       // capability "thermostats"
       capability "Thermostat Setpoint"
       capability "Thermostat Operating State"
        
                
        command "heatingSetpointUp"
		command "heatingSetpointDown"
        command "setHeatingSetpoint"
        command "setBoostLength" //keep to set boost length
       // command "heat"
        command "boostSwitch"
        command "emergencyHeat"
        command "stopBoost"
        command "boost"
       	command "ThermostatMode"
        command "ThermostatTemperatureSetpoint"
       	command "ThermostatSetMode"
        command "on"
        
        attribute "lastCheckin", "String"
        attribute "boostLabel", "String"
        //attribute"setBoostLength", "number" //for boost
       
       attribute "thermostatTemperatureSetpoint", "String" //for google
       attribute "thermostatSetpoint", "number" //for google
       attribute "availableThermostatModes", "enum", ["heat,off,heatcool,on"]//for google
       attribute "thermostatTemperatureUnit", "string" //for google
       attribute "thermostatMode", "enum", ["heat,off,heatcool,on"] //for google
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {

		multiAttributeTile(name: "thermostat", type:"lighting", width:6, height:4) {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState "default", label: '${currentValue}°', unit:"C", 
                backgroundColors:[
					[value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"],
                    [value: offline, color: "#ff0000"]
				]
			}
            
          tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
               	attributeState("default", label:'${currentValue}')
           	}
		}
        
        valueTile("thermostat_small", "device.temperature", width: 2, height: 2, canChangeIcon: true) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
                [value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
            ]
		}
 
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
					[value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
				]
		}
        //google
        valueTile("thermostatSetpoint", "device.thermostatSetpoint", inactiveLabel: true, decoration: "flat", width: 4, height: 1) {
			state("thermostatSetpoint", label:'${currentValue}')
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state("default", label:'refresh', action:"refresh", icon:"st.secondary.refresh-icon")
		}
 
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 2, inactiveLabel: false, range:"(12..30)") {
			state "setHeatingSetpoint", action:"setHeatingSetpoint" //need to send to a lag
		}
        
        valueTile("battery", "device.batteryVoltage", width: 2, height: 2) {
			state "default", label:'Battery Voltage Is ${currentValue}', unit:"V",
            backgroundColors:[
               					[value: 3, color: "#44b621"],
								[value: 2.8, color: "#f1d801"],
								[value: 2.78, color: "#bc2323"],
         		           ]
        }
        controlTile("boostSliderControl", "device.boostLength", "slider", height: 2, width: 2, inactiveLabel: false, range:"(30..120)") {
			state ("setBoostLength", label:'Set boost length to', action:"setBoostLength")
		}
        
        standardTile("switch", "device.switch", decoration: "flat", height: 1, width: 1, inactiveLabel: true) {
			state "on", label:'${name}', action:"off", icon:"st.Home.home1", backgroundColor:"#f1d801" 
			state "off", label:'${name}', action:"heat", icon:"st.Home.home1", backgroundColor:"#ffffff"
            
        }
        standardTile("boostSwitch", "device.boostSwitch", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
			state ("stby", label:'Press to boost', action: "emergencyHeat", icon:"st.alarm.temperature.overheat")
            state ("emergencyHeat", label: 'Press to stop boost', action:"stopBoost", icon:"st.Health & Wellness.health7", backgroundColor: "#bc2323" )
			
        }
         standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state ("heat", label: 'Press for off', action:"off" , icon:"st.thermostat.auto")
            state ("cool", label: 'Press for off', action:"off", icon:"st.thermostat.auto-cool")
			state ("off", label: '', action:"heat", icon:"st.thermostat.heating-cooling-off")
			//state ("heat", label: 'Press to stop boost', action:"stopBoost", icon:"st.thermostat.heat-auto", backgroundColor: "#bc2323")
            state ("emergencyHeat", label: 'Press to stop boost', action:"stopBoost", icon:"st.thermostat.emergency-heat", backgroundColor: "#bc2323")
		}
        
        valueTile("boostLabel", "device.boostLabel", inactiveLabel: true, decoration: "flat", width: 4, height: 1) {
			state("default", label:'${currentValue}')
        }

        main(["thermostat_small"])
		details(["thermostat", "heatingSetpoint", "heatSliderControl", "thermostatMode", "boostLabel", "battery", "boostSliderControl", "boostSwitch", "refresh", "switch"])
	}
    def rates = [:]
	rates << ["5" : "Refresh every 5 minutes (eTRVs)"]
	rates << ["10" : "Refresh every 10 minutes (power mon.)"]	
	rates << ["15" : "Refresh every 15 minutes (socets)"]
	rates << ["30" : "Refresh every 30 minutes (default)"]

	preferences {
		section("Check-in") {
        input name: "refreshRate", type: "enum", title: "Check-in Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
		input name: "checkinInfo", type: "enum", title: "Show last Check-in time", options: ["Hide", "MM/dd/yyyy h:mma", "h:mma dd/mm/yyyy", "dd/MM/yyyy h:mm", "dd-MM-yyyy HH:mm" , "h:mma dd/MM/yy"], description: "Show last check-in info.", defaultValue: "dd/MM/yyyy HH:mm", required: false
        input name: "emergencyheattemp", type: "number", title: "fill in a temp to boost to - 13 to 30", range: "13..30", defaultValue: "21", required: true
       	}
	}
}
//	===== Update when installed or setting changed =====
def installed() {
	log.info "Executing 'installed'"
    updated()
}
def updated() {
	log.info "updated running"
	unschedule(refreshRate)
    unschedule(setHeatingSetpoint)
    unschedule(setThermostatMode)
    unschedule(stopBoost)
    state.counter = state.counter
    state.counter = 0
    state.boostLength = 60
    runIn(2, update)
}
def update() {
	log.info "update running"
	switch(refreshRate) {
		case "5":
			runEvery5Minutes(refresh)
			log.info "Refresh Scheduled for every 5 minutes"
			break
		case "10":
			runEvery10Minutes(refresh)
			log.info "Refresh Scheduled for every 10 minutes"
			break
		case "15":
			runEvery15Minutes(refresh)
			log.info "Refresh Scheduled for every 15 minutes"
			break
		default:
			runEvery30Minutes(refresh)
			log.info "Refresh Scheduled for every 30 minutes"
	}
}
def uninstalled() {
    unschedule()
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
} // not req as cloud

/// ================this dosent do anything ?========================= //
def heatingSetpointUp(){
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	runIn (3, tempdelay,[data: [value: newSetpoint]])
}
def heatingSetpointDown(){
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heat set point down to: ${newSetpoint}"
	runIn (3, tempdelay,[data: [value: newSetpoint]])
}
/// =================================================== //
def thermostatTemperatureSetpoint(){ //for google auto/heat-cool mode
	log.debug "google set temp"
	setHeatingSetpoint()
}

/// ==============================


def setHeatingSetpoint(temp) {
	unschedule(errordelay)
    if (temp < 12) {
		temp = 12
	}
	if (temp > 30) {
		temp = 30
	}
    def resp = parent.apiGET("/subdevices/set_target_temperature?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger(), temperature: temp]).toString()))
			//log.debug "setting response ${resp.status} ${resp.data}"
    if (resp.status != 200) {
    	unschedule(errordelay)
    	log.error "Unexpected result in seting temp ${resp.status}"
        if (state.counter == null || state.counter >= 6) {
			state.counter = 0
		}
        if (state.counter < 5) {
        	state.counter = state.counter + 1
        	sendEvent(name: "refresh", value: '', descriptionText: "error setting temp ${state.counter} try", isStateChange: true)
        	log.warn "runnting set temp ${state.counter} attempt"
        	runIn (7, errordelay,[data: [value: temp]])
        }
        else { //(state.counter == 6) {
        	unschedule(errordelay) //clean up to prevent memory leak
       		unschedule(setHeatingSetpoint)
        	sendEvent(name: "thermostat", value: 'offline', descriptionText: "error setting temp try ${state.counter} try. The device is offline", isStateChange: true)
            log.error "error setting temp try ${state.counter} try. The device is offline"
            state.counter = 0
		}
    }
    else {
    	unschedule(errordelay) //clean up to prevent memory leak
       	unschedule(stopBoost)
        unschedule(setHeatingSetpoint)
                
    	if (state.boostSwitch == 'emergencyHeat'){
        log.debug "temp @ $temp - carying on with $mode settings"
        def boosttill = new Date(now() + (state.boostLength * 60000)).format("HH:mma", location.timeZone)
        state.boostLabel = "Boosting till \n$boosttill"
        state.thermostatMode = 'heat'
        runIn(state.boostLength * 60, stopBoost)
        log.info "boosting till $boosttill with emergencyHeat / heat and temp of $temp - ${resp.status}"
        state.boostSwitch = 'emergencyHeat'
        }
        else { //off or auto
        state.thermostatMode = 'heat'
     	state.boostLabel = "\n$state.boostLength Min Boost"
        log.info "setHeatingSetpoint done with temp of $temp and mode auto - ${resp.status}"
        state.boostSwitch = 'stby'
        }
        state.counter = 0
        state.heatingSetpoint = resp.data.data.target_temperature
        state.thermostatSetpoint = resp.data.data.target_temperature
        //bolt ons
        //state.HeatingSetpoint = resp.data.data.target_temperature
        //state.ThermostatSetpoint = state.heatingSetpoint
        state.ThermostatMode = state.thermostatMode
        
        // bolt ons
        state.batteryVoltage = resp.data.data.voltage
        //state.boostSwitch = 'stby' 	//reset to off in check in
        state.switch = 'on' 		//reset to off in check in
        checkin()
	}
}

def errordelay(temp) {
	unschedule(errordelay)
	unschedule(setHeatingSetpoint)
    def key = "value"
	def value = temp[key] ?: "12"
				//log.debug "errordelay temp valuse is $temp"
	setHeatingSetpoint(value)
}

def setBoostLength(minutes) {
	if (minutes < 30) {
		minutes = 30
	}
	if (minutes > 120) {
		minutes = 120
	}
    state.boostLength = minutes
    sendEvent(name:"boostLength", value: state.boostLength, displayed: false)
   				//log.debug "done setBoostLength  ${minutes} minutes"
    //def boostLabel = "\n$state.boostLength Min Boost"
    //state.boostLabel = boostLabel
    state.boostLabel = "\n$state.boostLength Min Boost"
    sendEvent(name: "boostLabel", value: state.boostLabel, displayed: false)
}

def stopBoost() {
	log.debug "Executing 'stopBoost'"
    setThermostatMode(state.lastmode)
    log.debug "Boost stoped"
}

def setLastHeatingSetpoint(temp) { //this is to resume after off or boost
	if (temp < 12 || temp > 30 ) {
    	state.lastHeatingSetPoint = 16
    	log.warn "$temp is out of 12-30 range set to 16"
    }
    else if (state.boostSwitch == 'emergencyHeat') {
		log.warn "emergencyHeat / heat set point not saved is its $temp"
	}
	else {
    state.lastHeatingSetPoint = temp
    }
    //log.debug "temp saved as $temp"
    temp = state.lastHeatingSetPoint
    log.info "in the end last temp saved as $temp"
}

def setLastmode(mode) { //this is to resume after off or boost
	if (mode == null || state.boostSwitch == 'emergencyHeat'){ //mode == 'emergencyHeat' || 
    	log.warn "$mode not wanted"
        state.lastmode = 'heat'
    }
    else if (state.lastmode == 'off'){
    	log.warn "last mode was off, not saving twice"
        state.lastmode = 'heat'
    }
    else { //heat and other modes
		state.lastmode = mode
        //log.debug "$mode should be off or heat"
	}
    mode = state.lastmode
    log.info "in the end last mode saved as $mode"
}


//Therm modes
def auto() { // auto - The automatic mode of the thermostat
	setThermostatMode('heat') 
}    
def cool() { //cool - The cool mode of the thermostat
	setThermostatMode('cool') 
}
def heat() { //heat - The heat mode of the thermostat
	setThermostatMode('heat') 
}
def off() { //off - Off mode for the thermostat
	setThermostatMode('off') 
}
def on() {
	setThermostatMode('heat')
}
def emergencyHeat() { 
	log.debug "def emergency heat"
	setThermostatMode('emergencyHeat')
}
def boost() {
	log.debug "def boost"
	setThermostatMode('emergencyHeat')
}



def setThermostatMode(mode) {
	log.debug "incoming setThermostatMode with mode $mode"
	mode = mode == 'cool' || mode == 'auto' || mode == 'on' ? 'heat' : mode
	log.debug "Executing setThermostatMode with mode $mode"
    
    if (mode == 'off') { off
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint'))
        setLastmode(device.currentValue('thermostatMode'))
        log.info "$mode with temp of 12 going to setHeatingSetpoint..... "
        setHeatingSetpoint(12) //labels for boostswitch-heat and mode-off set in 'checkin' for ==12
    }
    else if (mode == 'emergencyHeat') { 
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint'))
        setLastmode(device.currentValue('thermostatMode'))
        def temp = settings.emergencyheattemp
        state.boostSwitch = 'emergencyHeat'
        log.info "$mode with a temp of $temp going to setHeatingSetpoint..... "
        setHeatingSetpoint(temp) // send temp to setHeatingSetpoint
    }
    else { //auto heat etc
    	def lastHeatingSetPoint = state.lastHeatingSetPoint
        if (lastHeatingSetPoint == null || lastHeatingSetPoint < 12 || lastHeatingSetPoint > 30 ) {
        	lastHeatingSetPoint = 17
        }
        log.info "$mode mode resuming to $lastHeatingSetPoint"
        state.boostSwitch = 'stby'
        setHeatingSetpoint(lastHeatingSetPoint)
	}
}

def poll() {
    def resp = parent.apiGET("/subdevices/show?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger()]).toString()))
    //log.debug "poll data ${resp.status}" // ${resp.data}"
    if (resp.status != 200) {
    	sendEvent(name: "refresh", value: '', descriptionText: "BAD Poll", isStateChange: true)
		log.error "Unexpected result in poll ${resp.status} - ${device}" // end
	}
    else {
    state.temperature = resp.data.data.last_temperature
    state.heatingSetpoint = resp.data.data.target_temperature
    state.batteryVoltage = resp.data.data.voltage
    log.info "POLL All good ${device} ${resp.status}"
    checkin()
    }
}
    
def checkin() {
	//log.debug "checkin start"
    if (state.heatingSetpoint == 12) { //from mode off or if set to 12 from another system
    	   	state.boostSwitch = 'stby'
            state.thermostatMode = 'off'
            state.switch = 'off'
            state.boostLabel = "\n$state.boostLength Min Boost"
            unschedule(stopBoost)
    }
    sendEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true, state: state.thermostatMode) //mode & off icon
    sendEvent(name: "boostSwitch", value: state.boostSwitch, displayed: false)
    sendEvent(name: "boostLabel", value: state.boostLabel, displayed: true) //change label back to boost time from start time        
    sendEvent(name: "switch", value: state.switch, displayed: false)
    sendEvent(name: "temperature", value: state.temperature, unit: "C", state: 'heat')
    sendEvent(name: "heatingSetpoint", value: state.heatingSetpoint, unit: "C", state: 'heat') //was heat
    sendEvent(name: "batteryVoltage", value: state.batteryVoltage == null ? "Not Available" : state.batteryVoltage)
    
   
    state.thermostatSetpoint = state.heatingSetpoint
    sendEvent(name: "thermostatSetpoint", value: state.heatingSetpoint, unit: "C", state: 'heat') //was auto
    
    state.thermostatMode = state.thermostatMode
    state.thermostatTemperatureSetpoint = state.thermostatSetpoint
    state.thermostatTemperatureAmbient = state.temperature
    
    
    //state.thermostatTemperatureSetpoint = state.heatingSetpoint //for google in auto/heat-cool mode
    //state.thermostatSetpoint = state.heatingSetpoint //for google in auto/heat-cool mode
    //device.ThermostatTemperatureSetpoint = state.heatingSetpoint
    
    def thermostatSetpoint = state.thermostatTemperatureSetpoint
    //def ThermostatTemperatureSetpoint = device.ThermostatTemperatureSetpoint
    log.debug "set point is $thermostatSetpoint and $ThermostatTemperatureSetpoint"
    
    state.lastRefreshed = now()
    def lastRefreshed = state.lastRefreshed
    def checkinInfoFormat = (settings.checkinInfo ?: 'dd/MM/yyyy h:mm')
    def now = ''
    if (checkinInfoFormat != 'Hide') {
        try {
            now = 'Last Check-in: ' + new Date().format("${checkinInfoFormat}", location.timeZone)
        } catch (all) { }
    sendEvent(name: "lastCheckin", value: now, displayed: false)    
    }
	log.info "check in done"
}

def refresh() {
	log.info "Executing refresh"
	poll()
}
