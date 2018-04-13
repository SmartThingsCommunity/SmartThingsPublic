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
 *  			2.6 - added google voice command compatibility - release for testing
 *						Turn off -			"turn off **device name/room** thermostat(s)" 				--Save last settings and turn off (aka 12deg) 
 *						Turn on - 			"set **device name/room** termostat(s) to heat/cool"		-- heat=Resume cool=boost 
 *						Set Temprature - 	"set **device name/room** thermostat(s)/temprature to **number**"
 *						Quiry Temprature - 	"what is the temprature in the **device name/room/house**" --response current temp and setpoint
 *						Up/down 3 deg - 	"turn up/down **device name/room** thermostat(s)"
 *						Boost - 			"set  **device name/room** thermostat(s) to cool?
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
	definition (name: "MiHome eTRV", namespace: "alyc100", author: "Alex Lee Yuk Cheung & updated by mark cockcroft" ) { //removed 13th 8am type: ,action.devices.types.THERMOSTAT"
		
		//capability "Polling" not needed as refresh is schdualed in the DH
       	capability "Refresh"
		capability "Thermostat"
        capability "Battery" 
 
// disabled 12th 8am command "heatingSetpointUp"
// disabled 12th 8am command "heatingSetpointDown"
        command "setCoolingSetpoint"	//for google when in cool aka boost mode
        command "setHeatingSetpoint"	//keep main command
        command "setBoostLength"		//keep to set boost length
        command "emergencyHeat"			// used on the boost/stop boost button
        command "stopBoost"				// used on the boost/stop boost button

        attribute "lastCheckin", "String"
        attribute "boostLabel", "String"
        attribute "thermostatTemperatureSetpoint", "String" //for google
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
        
        valueTile("thermostat_small", "device.temperature", width: 2, height: 2, icon:"st.alarm.temperature.normal" ,canChangeIcon: true) {
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
        //google data for boost/cool mode
//        valueTile("thermostatTemperatureSetpoint", "device.thermostatTemperatureSetpoint", inactiveLabel: true, decoration: "flat", width: 4, height: 1) {
//			state("default", label:'${currentValue}')
//        }
        
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
                
        standardTile("boostSwitch", "device.boostSwitch", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
			state ("stby", label:'Press to boost', action: "emergencyHeat", icon:"st.alarm.temperature.overheat")
            state ("emergencyHeat", label: 'Press Reboost', action:"emergencyHeat", icon:"st.Health & Wellness.health7", backgroundColor: "#bc2323" )
			
        }
         standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			// google does not recognise state ("auto", label: 'auto Press for off', action:"off" , icon:"st.thermostat.auto-cool")
            state ("heat", label: '_    Press for    _ Off', action:"off" , icon:"st.thermostat.heat-auto", backgroundColor:"#00a0dc")
            state ("cool", label: 'In BOOST Press for Resume', action:"stopBoost", icon:"st.thermostat.emergency-heat", backgroundColor:"#e86d13")
			state ("off", label: 'Press for Heat (or press boost below)', action:"heat", icon:"st.thermostat.heating-cooling-off")
			//cool used insted state ("emergencyHeat", label: 'Press to stop boost', action:"stopBoost", icon:"st.thermostat.emergency-heat", backgroundColor: "#bc2323")
		}
        
        valueTile("boostLabel", "device.boostLabel", inactiveLabel: true, decoration: "flat", width: 4, height: 1) {
			state("default", label:'${currentValue}')
        }

        main(["thermostat_small"])
		details(["thermostat", "heatingSetpoint", "heatSliderControl", "thermostatMode", "boostLabel", "battery", "boostSliderControl", "boostSwitch", "refresh"])
	}
    def rates = [:]
	rates << ["5" : "Refresh every 5 minutes (eTRVs)"]
	rates << ["10" : "Refresh every 10 minutes (power monitors)"]	
	rates << ["15" : "Refresh every 15 minutes (sockets)"]
	rates << ["30" : "Refresh every 30 minutes (default)"]

	preferences {
        input name: "refreshRate", type: "enum", title: "Check-in Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
		input name: "checkinInfo", type: "enum", title: "Show last Check-in time", options: ["Hide", "MM/dd/yyyy h:mma", "h:mma dd/mm/yyyy", "dd/MM/yyyy h:mm", "dd-MM-yyyy HH:mm" , "h:mma dd/MM/yy"], description: "Show last check-in info.", defaultValue: "dd/MM/yyyy HH:mm", required: false
        input name: "emergencyheattemp", type: "number", title: "Fill in a temp to boost to - 13 to 30", range: "13..30", defaultValue: "21", required: true
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
    unschedule(errordelay)
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
} // ======== end off settings ========

// parse events into attributes
def parse(String description) { // not req as cloud?
	log.debug "Parsing '${description}'"
}

/// ================this dosent do anything ?========================= 
def heatingSetpointUp(){
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	runIn (3, tempdelay,[data: [value: newSetpoint]])
}
def heatingSetpointDown(){
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heat set point down to: ${newSetpoint}"
	runIn (3, tempdelay,[data: [value: newSetpoint]])
} /// ===================================================


def setCoolingSetpoint(temp){ //for google when in cooling aka boost mode
	log.debug "google set cooloing temp values-'$temp'"
	setHeatingSetpoint(temp)
}

/// ============ Set temp ==================
def setHeatingSetpoint(temp) { //google uses this for temp change
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
        	sendEvent(name: "refresh", value: '', descriptionText: "error setting temp '$state.counter' try", isStateChange: true)
        	log.warn "runnting set temp '$state.counter' attempt"
        	runIn (7, errordelay,[data: [value: temp]])
        }
        else {
        	unschedule(errordelay) 			//clean up to prevent memory leak
       		unschedule(setHeatingSetpoint) 	//clean up to prevent memory leak
        	sendEvent(name: "thermostat", value: 'offline', descriptionText: "error setting temp try '$state.counter' try. The device is offline", isStateChange: true)
            log.error "error setting temp try '$state.counter' try. The device is offline"
            state.counter = 0
		}
    }
    else {
    	unschedule(errordelay) 				//clean up to prevent memory leak
        unschedule(setHeatingSetpoint) 		//clean up to prevent memory leak
        unschedule(stopBoost)				// leave hear so that if setting temp fails completely at least stopBoost will still be active
         
    	if (state.boostSwitch == 'emergencyHeat'){
        log.debug "temp @ '$temp' in boost- carying on with emergency heat set temp"
        def boosttill = new Date(now() + (state.boostLength * 60000)).format("HH:mma", location.timeZone)
        state.boostLabel = "Boosting till $boosttill"
        runIn(state.boostLength * 60, stopBoost)
        log.debug "setHeatingSetpoint for '$device' status-'$resp.status' - boosting till '$boosttill' with emergencyHeat (aka cool) temp of '$temp'"
        }
        
        else { //off / auto / heat
        state.boostLabel = "$state.boostLength Min Boost"
        state.thermostatMode = 'heat'			//reset to off as req. during check in
        log.debug "setHeatingSetpoint for '$device' status-'$resp.status' - with temp of '$temp' and '$state.thermostatMode' (should be heat/off)"
        }
        
        state.counter = 0
        state.heatingSetpoint = resp.data.data.target_temperature
        state.batteryVoltage = resp.data.data.voltage
        state.boostSwitch = 'stby'
        log.info "setHeatingSetpoint complete for '$device' status-'$resp.status' - with temp of '$temp' and mode '$state.thermostatMode', going to .... checkin ...."
        checkin()
	}
} // =============== end set temp =================

def errordelay(temp) { // delay resending of set temprature
	unschedule(errordelay)
	unschedule(setHeatingSetpoint)
    log.debug "error delay trigerd values - '$temp'"
    def key = "value"
	def value = temp[key] ?: "12"
    log.debug "error delay sending back to setHeatingpoint values - '$temp' Key-'$key' Value-'$value'"
	setHeatingSetpoint(value)
}

def setLastHeatingSetpoint(temp) { //this is to resume after off or boost
	if (temp < 12 || temp > 30 ) {
    	state.lastHeatingSetPoint = 16
    	log.warn "'$temp' is out of 12-30 range set to 16"
    }
    else if (state.thermostatMode == 'emergencyHeat' || state.thermostatMode == 'cool' ) {
		log.warn "emergencyHeat, temp not saved as its '$temp'"
	}
	else { 									//could be 12 to turn back off
    state.lastHeatingSetPoint = temp
    }
    log.info "Saved resume temp is '$state.lastHeatingSetPoint'"
}

def setLastmode(mode) { 					//this is to resume after off or boost
	if (state.boostSwitch == 'emergencyHeat' || mode == 'cool'){
    	log.warn "'$mode' not wanted"
        //state.lastmode = 'heat'
    }
    else if (state.lastmode == 'off' && mode == 'off'){
    	log.warn "last mode was off and current mode is off, not saving twice = mode HEAT"
        state.lastmode = 'heat'
    }
     
    else if (mode == null){ 				//heat and other modes
    	log.warn " mode is NULL - mode = heat"
        state.lastmode = 'heat'
    }
	else {
    	state.lastmode = mode
        log.debug "this mode is '$mode' and should be off or heat"
    }
    log.info "Saved resume mode is '$state.lastmode'"
}

// ============ button commands ===========
def heat() { 
	log.debug "def heat"
    setThermostatMode('heat') 
}
def off() { //used for off button
	log.debug "def off"
	setThermostatMode('off') 
}

def emergencyHeat() { //used
	log.debug "def emergency heat"
	setThermostatMode('cool')
}
def stopBoost() { //used
	log.debug "Executing 'stopBoost'"
    setThermostatMode(state.lastmode)
    log.debug "Boost stoped"
}

def setBoostLength(minutes) { //set boost length
	if (minutes < 30) {
		minutes = 30
	}
	if (minutes > 120) {
		minutes = 120
	}
    state.boostLength = minutes
    state.boostLabel = "$state.boostLength Min Boost"
    sendEvent(name:"boostLength", value: state.boostLength, displayed: false)
   	if (state.thermostatMode != 'cool'){
    	sendEvent(name: "boostLabel", value: state.boostLabel, displayed: false)
    }
}
//def boost() {
//	log.debug "def boost"
//	setThermostatMode('cool')
//}
//def auto() { 
//	log.debug "def auto"
//    setThermostatMode('heat') 
//}    
//def cool() { //used for boost button
//	log.debug "def cool"
//    setThermostatMode('cool') 
//}
//def on() {
//	log.debug "def on"
//	setThermostatMode('heat')
//}

def setThermostatMode(mode) { 								////requested mode NOT actualy in it yet - -google only uses this for mode change
	mode = mode == 'auto' || mode == 'on' ? 'heat' : mode
	//log.debug "Executing setThermostatMode with mode '$mode'"
    
    if (mode == 'off') {
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint')) 	//save current set temp
        setLastmode(device.currentValue('thermostatMode')) 				//save current mode
        log.info "MODE Change complete -'$mode' with default temp of 12, going to ...... setHeatingSetpoint ..... "
        setHeatingSetpoint(12)								//labels for boostswitch-stby and mode-off set in 'checkin' for == 12
    }
    else if (mode == 'emergencyHeat' || mode == 'cool') {
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint'))
        setLastmode(device.currentValue('thermostatMode'))
        state.boostSwitch = 'emergencyHeat'
        state.thermostatMode = 'cool' //aka boost
        log.info "MODE Change complete - '$mode'(aka boost/emergency heat) with temp of '$settings.emergencyheattemp' going to going to .... setHeatingSetpoint ...... "
        setHeatingSetpoint(settings.emergencyheattemp)
    }
    
    else { //auto heat resume etc
    	def lastHeatingSetPoint = state.lastHeatingSetPoint 			// recall last saved temp
        if (lastHeatingSetPoint == null || lastHeatingSetPoint < 12 || lastHeatingSetPoint > 30 ) {
        	lastHeatingSetPoint = 17									// capurte any errors
            log.warn " '$mode' last temp out of range going with default temp of '$temp'"
        }
		state.boostSwitch = 'stby'
        state.thermostatMode = mode
        log.info "MODE Change complete - '$mode' mode resuming to last temp of'$lastHeatingSetPoint' going to .... setHeatingSetpoint ...."
    	setHeatingSetpoint(lastHeatingSetPoint)
	}
}	//end mode setting

def poll() {
    def resp = parent.apiGET("/subdevices/show?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger()]).toString()))
    //log.debug "poll data ${resp.status}" // ${resp.data}"
    if (resp.status != 200) {
    	sendEvent(name: "refresh", value: '', descriptionText: "BAD Poll", isStateChange: true)
		log.error "POLL for  -'${device}' response -'${resp.status}' Unexpected Result" // end
	}
    else {
    state.temperature = resp.data.data.last_temperature
    state.heatingSetpoint = resp.data.data.target_temperature
    state.batteryVoltage = resp.data.data.voltage
    log.info "POLL for  -'${device}' response -'${resp.status}' all good"
    checkin()
    }
}
    
def checkin() {
	if (state.heatingSetpoint == 12) { //from mode off or if set to 12 from another system
    	   	unschedule(stopBoost)
            state.boostSwitch = 'stby'
            state.thermostatMode = 'off'
            state.boostLabel = "$state.boostLength Min Boost"
    }
//bolt ons for google
    state.thermostatTemperatureAmbient = state.temperature
    state.coolingSetpoint = state.heatingSetpoint 													//needed for google when in cooling aka boost
    sendEvent(name: "coolingSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false) 	//needed for google when in cooling aka boost   
        
    sendEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true) //mode & off icon - , state: state.thermostatMode
    sendEvent(name: "boostSwitch", value: state.boostSwitch, displayed: false)
    sendEvent(name: "boostLabel", value: state.boostLabel, displayed: false) //change label back to boost time from start time        
   	sendEvent(name: "temperature", value: state.temperature, unit: "C") //state: 'heat')
    sendEvent(name: "heatingSetpoint", value: state.heatingSetpoint, unit: "C") //was heat , state: 'heat'
    sendEvent(name: "batteryVoltage", value: state.batteryVoltage == null ? "Not Available" : state.batteryVoltage)
    
    def setmode = state.thermostatMode // for log info below
    if (state.thermostatMode == 'cool'){
    setmode = state.thermostatMode + ' (aka boost)'
    }
    
    def checkinInfoFormat = (settings.checkinInfo ?: 'dd/MM/yyyy h:mm')
    def now = ''
    if (checkinInfoFormat != 'Hide') {
        try {
            now = 'Last Check-in: ' + new Date().format("${checkinInfoFormat}", location.timeZone)
        } catch (all) { }
    sendEvent(name: "lastCheckin", value: now, displayed: false)    
    }
	log.info "CHECKIN -'$device',   MODE-'$setmode',   TARGET Temp-'$state.heatingSetpoint',   ACTUAL Temp-'$state.temperature',   BOOST Settings-'$state.boostLabel' all good"
}

def refresh() {
	log.info "REFRESH -'$device' @ '$settings.refreshRate' min refresh rate"
	poll()
}
