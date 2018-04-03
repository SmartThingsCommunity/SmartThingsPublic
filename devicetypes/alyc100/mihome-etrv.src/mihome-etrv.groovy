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
 *  
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
	definition (name: "MiHome eTRV", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		//capability "Actuator"
		//capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Thermostat"
        capability "Thermostat Mode"
		capability "Thermostat Heating Setpoint"
		capability "Switch"
        capability "Battery"
        //capability "thermostatMode"
        
        command "heatingSetpointUp"
		command "heatingSetpointDown"
        command "setHeatingSetpoint"
        command "setBoostLength"
        command "heat"
        command "boostSwitch"
        command "emergencyHeat"
        command "stopBoost"
        
        attribute "lastCheckin", "String"
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
			state "off", label:'${name}', action:"heat", icon:"st.Home.home1", backgroundColor:"#ffffff" //do i need this?
            
        }
        standardTile("boostSwitch", "device.boostSwitch", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
			state ("heat", label:'Press to boost', action: "emergencyHeat", icon:"st.alarm.temperature.overheat")
            state ("emergencyheat", label: 'Press to stop boost', action:"stopBoost", icon:"st.Health & Wellness.health7", backgroundColor: "#bc2323" )
			
        }
         standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state ("auto", label: '', action:"off" , icon:"st.thermostat.auto")
            state ("cool", label: '', action:"off", icon:"st.thermostat.auto-cool")
			state ("off", label: '', action:"heat", icon:"st.thermostat.heating-cooling-off")
			state ("heat", label: 'Press for off', action:"off", icon:"st.thermostat.heat-auto")
            state ("emergencyheat", label: 'Press to stop boost', action:"stopBoost", icon:"st.thermostat.emergency-heat", backgroundColor: "#bc2323")
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
		section(title: "Check-in Interval") {
        paragraph "Run a Check-in procedure every so often."
        input name: "refreshRate", type: "enum", title: "Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
		}
        section(title: "Check-in Info") {
            paragraph "Display check-in info"
            input "checkinInfo", "enum", title: "Show last Check-in info", options: ["Hide", "MM/dd/yyyy h:mm", "MM-dd-yyyy h:mm", "dd/MM/yyyy h:mm", "dd-MM-yyyy h:mm" , "h:mm dd/MM/yyyy h:mm"], description: "Show last check-in info.", defaultValue: "dd/MM/yyyy h:mm", required: false
        }
        section(title: "emergancy heat temp") {
        paragraph "Temp to boost to"
        input "emergencyheattemp", "number", title: "fill in boost to temp 13 to 30", range: "13..30", defaultValue: "21", required: false
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
/// =================================================== //
def heatingSetpointUp(){ //this dosent do anything
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	runIn (3, tempdelay,[data: [value: newSetpoint]])
}
def heatingSetpointDown(){ //this dosent do anything
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heat set point down to: ${newSetpoint}"
	runIn (3, tempdelay,[data: [value: newSetpoint]])
}

def setHeatingSetpoint(temp) {
	log.debug "Executing setHeatingSetpoint with temp $temp"
	//def latestThermostatMode = device.latestState('thermostatMode')
    log.debug "unschedual  boost and error delay b4"
    unschedule(stopBoost)
    unschedule(errordelay)
    log.debug "unschedual  boost and error delay b4"
    log.debug "set mode heat on heat b4"
    state.thermostatMode = 'heat'
	state.switch = 'on'        
    state.boostSwitch = 'heat'
    log.debug "set mode heat on heat after"
    if (temp < 12) {
		temp = 12
	}
	if (temp > 30) {
		temp = 30
	}
    def resp = parent.apiGET("/subdevices/set_target_temperature?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger(), temperature: temp]).toString()))
	//log.debug "setting response ${resp.status} ${resp.data}"
    if (resp.status != 200) {
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
        	sendEvent(name: "thermostat", value: 'offline', descriptionText: "error setting temp try ${state.counter} try. The device is offline", isStateChange: true)
            log.error "error setting temp try ${state.counter} try. The device is offline"
            unschedule(errordelay)
            unschedule(setHeatingSetpoint)
            state.counter = 0
		}
    }
    else {
    	//tempset200.signal()
        state.counter = 0
     // state.temperature = // not sent when changing temp 
        state.heatingSetpoint = resp.data.data.target_temperature
        state.batteryVoltage = resp.data.data.voltage
		log.info "setHeatingSetpoint with temp $temp ${resp.status}"
        runIn(2, checkin)
	}
}
def errordelay(temp) {
	unschedule(errordelay)
	unschedule(setHeatingSetpoint)
    //unschedule(stopBoost)
	def key = "value"
	def value = temp[key] ?: "12"
	log.debug "errordelay temp valuse is $temp"
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
   	log.debug "done setBoostLength  ${minutes} minutes"
    def boostLabel = "\n$state.boostLength Min Boost"
    state.boostLabel = boostLabel
    sendEvent(name: "boostLabel", value: boostLabel, displayed: false)//refresh()
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
    else {
		state.lastHeatingSetPoint = temp
        log.info " last set point saved as $temp"
	}
}
def setLastmode(mode) { //this is to resume after off or boost
	if (mode == emergencyheat || mode == null ) { // do we want off in? || mode == off
    	state.lastmode = mode
        log.warn "$mode not wanted"
        state.lastmode = heat
    }
    else {
		state.lastmode = mode
        log.info "last mode saved as $mode"
	}
}


//Therm modes
def auto() { // auto - The automatic mode of the thermostat
	setThermostatMode('auto') }    
def cool() { //cool - The cool mode of the thermostat
	setThermostatMode('auto') }
def heat() { //heat - The heat mode of the thermostat
	setThermostatMode('heat') }
def off() { //off - Off mode for the thermostat
	setThermostatMode('off') }
def on() {
	setThermostatMode('auto')}
def emergencyHeat() { 
	setThermostatMode('emergencyheat')
}

def setThermostatMode(mode) {
	log.debug "incoming 'setThermostatMode mode $mode'"
    mode = mode == 'cool' || mode == 'auto' || mode == 'on' ? 'heat' : mode
	log.debug "Executing 'setThermostatMode with mode $mode'"
    
    if (mode == 'off') {
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint'))
        setLastmode(device.currentValue('thermostatMode'))
        state.switch = 'off'
        state.thermostatMode = 'off'
        state.boostSwitch = 'heat'
        state.boostLabel = "\n$state.boostLength Min Boost"
        setHeatingSetpoint(12) //labels for boostswitch-heat and mode-off set in 'checkin' for ==12
        log.info "$mode with temp 12"
    }
    else if (mode == 'emergencyheat') { 
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint'))
        setLastmode(device.currentValue('thermostatMode'))
              
        def temp = settings.emergencyheattemp
        log.debug "send set temp"
        setHeatingSetpoint(temp) // send temp to setHeatingSetpoint
        //log.debug "wait"
       	//setHeatingSetpoint.waitFor(setHeatingSetpoint(temp), 45000)
        //tempset200 = true
       //	tempset200.await(7*6, SECONDS)
        //log.debug "wait ok end"
        log.debug "temp set - carying on with emergeny boost"
        def boosttill = new Date(now() + (state.boostLength * 60000)).format("HH:mm", location.timeZone)
        state.boostLabel = "Boosting till \n$boosttill"
        state.thermostatMode = "emergencyheat"
        state.boostSwitch = 'emergencyheat'
        state.switch = 'on'
      	log.debug "boost switches set"        
        def btime = state.boostLength * 60 	//runin in seconds // can i put this in directly?
        runIn(btime, stopBoost, [delay: 20000])	//delay to make sure temp has completed
        log.info "boosting till $boosttill with $mode of $temp"
    }
    else {
    	state.boostLabel = "\n$state.boostLength Min Boost"
    	unschedule(stopBoost) // if schedualed
        //sendEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true) //mode & off icon
       	//sendEvent(name: "boostSwitch", value: "heat", displayed: false) //boost switch to press for boost
		//sendEvent(name: "boostLabel", value: state.boostLabel, displayed: false) //change label back to boost time from start time
    	def lastHeatingSetPoint = state.lastHeatingSetPoint
        if (lastHeatingSetPoint == null || lastHeatingSetPoint < 12 || lastHeatingSetPoint > 30 ) {
        	lastHeatingSetPoint = 17
        }
        setHeatingSetpoint(lastHeatingSetPoint)
        log.info "$mode mode resuming to $lastHeatingSetPoint"
	}
}

def poll() {
    //log.debug "Executing poll for ${device} ${this} ${device.deviceNetworkId}"
    def resp = parent.apiGET("/subdevices/show?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger()]).toString()))
    //log.debug "poll data ${resp.status}" // ${resp.data}"
    if (resp.status != 200) {
    	sendEvent(name: "refresh", value: '', descriptionText: "BAD Poll", isStateChange: true)
		log.error "Unexpected result in poll ${resp.status}" // end
	}
    else {
    state.temperature = resp.data.data.last_temperature
    state.heatingSetpoint = resp.data.data.target_temperature
    state.batteryVoltage = resp.data.data.voltage
    //sendEvent(name: "switch", value: 'on')
    //sendEvent(name: "temperature", value: resp.data.data.last_temperature, unit: "C", state: "heat")
    //sendEvent(name: "heatingSetpoint", value: resp.data.data.target_temperature, unit: "C", state: "heat")
    //sendEvent(name: "batteryVoltage", value: resp.data.data.voltage == null ? "Not Available" : resp.data.data.voltage)
    log.info "POLL All good ${device} ${resp.status}"
    checkin()
    }
}
    
def checkin() {
	log.debug "checkin start"
    if (state.heatingSetpoint == 12) { //from mode off or if set to 12 from another system
    	   	state.thermostatMode = 'off'
            state.switch = 'off'
    }
    sendEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true) //mode & off icon
    sendEvent(name: "boostSwitch", value: state.boostSwitch, displayed: false)
    sendEvent(name: "boostLabel", value: state.boostLabel, displayed: false) //change label back to boost time from start time        
    
    sendEvent(name: "switch", value: state.switch, displayed: false)
    
    sendEvent(name: "temperature", value: state.temperature, unit: "C", state: "heat")
    
    sendEvent(name: "heatingSetpoint", value: state.heatingSetpoint, unit: "C", state: "heat")
    sendEvent(name: "batteryVoltage", value: state.batteryVoltage == null ? "Not Available" : state.batteryVoltage)
        
    state.lastRefreshed = now()
    def lastRefreshed = state.lastRefreshed
    def checkinInfoFormat = (settings.checkinInfo ?: 'dd/MM/yyyy h:mm')
    def now = ''
    if (checkinInfoFormat != 'Hide') {
        try {
            now = 'Last Check-in: ' + new Date().format("${checkinInfoFormat}a", location.timeZone)
        } catch (all) { }
    sendEvent(name: "lastCheckin", value: now, displayed: false)    
    }
	log.info " check in done "
}

def refresh() {
	log.info "Executing refresh"
	poll()
}

//states
// def setHeatingSetpoint(temp)
//	state.lastHeatingSetPoint
// 	state.lastHeatingSetPoint


// state.counter

// state.boostLabel
// state.boostLength = minutes
// if (state.boostLength == null || state.boostLength == ''){
//        	state.boostLength = 60
//            sendEvent("name":"boostLength", "value": 60, displayed: true)

// ======== code for lag on temp == dosent wok though == //

//def setHeatingSetpointlag(temp) { //dosent work
//	log.debug "into temp lag $temp"
//	runIn (3, tempdelay,[data: [value: temp]])
//}
//def tempdelay(temp) {
//	unschedule(tempdelay)
//    unschedule(setHeatingSetpoint)
//    unschedule(errordelay)
//	def key = "value"
//	def value = newSetpoint[key] ?: "12"
//	log.debug "tempdelay temp value is $temp"
//	setHeatingSetpoint(value)
//}