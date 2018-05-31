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
 *	24.04.2018	3.0	- summer mode added to over valse fully and prevent automation change the temp, debugin depeciated/removed, guides added to settings page
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
	definition (name: "MiHome eTRV", namespace: "alyc100", author: "Alex Lee Yuk Cheung and updated by mark cockcroft") {
		
//capability "Polling" not needed as refresh is schdualed in the DH
       	capability "Actuator"	// best practice
        capability "Sensor"		// best practice
        capability "Refresh"
		capability "Thermostat"
        capability "Thermostat Heating Setpoint" // alows extra settings in routines
        capability "Battery" 
        capability "Thermostat Mode"	//might be need gor google
 
        command "setThermostatMode"		//might be need gor google
        command "ThermostatSetMode"		//might be need gor google
        command "setHeatingSetpoint"	//keep main command
        command "setBoostLength"		//keep to set boost length
        command "emergencyHeat"			// used on the boost/stop boost button
        command "stopBoost"				// used on the boost/stop boost button
        command "summer"				// uesed to put in summer mode - (Stats open permainatly)

        attribute "lastCheckin", "String"
        attribute "boostLabel", "String"
        attribute "thermostatTemperatureSetpoint", "String" 									//might be need gor google
        //attribute "availableThermostatModes", "enum", ["off", "heat", "boost", "on"]	//might be need gor google
        attribute "ThermostatSetpoint", "String"												//might be need gor google
        attribute "boostLength", "String"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "thermostat", type: "generic", width: 6, height: 4, canChangeIcon: true) {  
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("thermostat", label: '${currentValue}°', unit:"C", icon: "https://raw.githubusercontent.com/Mark-C-uk/SmartThingsPublic/master/devicetypes/alyc100/mihome-etrv.src/radiator120x120.png" ,defaultState: true, backgroundColors:[
					[value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
				]
                )
			}
  
          tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
               	attributeState("lastCheckin", label:'${currentValue}', defaultState: true)
           	}
		}
 
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state ("heatingSetpoint", label:'${currentValue}°',  backgroundColors:[ //unit:"C", defaultState: true,
					[value: 0, color: "#153591"],
					[value: 10, color: "#1e9cbb"],
					[value: 13, color: "#90d2a7"],
					[value: 17, color: "#44b621"],
					[value: 20, color: "#f1d801"],
					[value: 25, color: "#d04e00"],
					[value: 29, color: "#bc2323"]
				]
                )
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state ("refresh", label:'refresh', action:"refresh", icon:"st.secondary.refresh-icon", defaultState: true)
		}
		standardTile("summer", "device.summer", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state ("auto", label:'Summer', action: "summer", icon:"st.thermostat.auto")
            state ("summer", label:'Deactivite', action: "heat", icon:"st.custom.wuk.clear")
 		}
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 2, inactiveLabel: false, range:"(12..30)") {
			state ("heatingSetpoint", action:"setHeatingSetpoint") //need to send to a lag
		}
        valueTile("battery", "device.batteryVoltage", width: 2, height: 2) {
			state ("battery", label:'Battery Voltage Is ${currentValue}', unit:"V", defaultState: true, backgroundColors:[
               					[value: 3, color: "#44b621"],
								[value: 2.8, color: "#f1d801"],
								[value: 2.78, color: "#bc2323"],
         		           ]
                           )
        }
        controlTile("boostSliderControl", "device.boostLength", "slider", height: 2, width: 2, inactiveLabel: false, range:"(30..120)") {
			state ("boostLength", action:"setBoostLength")
		}
        standardTile("boostSwitch", "device.boostSwitch", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
			state ("stby", label:'Press to \nboost', action: "emergencyHeat", icon:"st.alarm.temperature.overheat")
            state ("emergencyHeat", label: 'Press Reboost', action:"emergencyHeat", icon:"st.Health & Wellness.health7", backgroundColor: "#bc2323")
			
        }
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state ("heat", label: 'Press for \nOff', action:"off" , icon:"st.thermostat.heat-auto", backgroundColor:"#00a0dc")
            state ("cool", label: 'In BOOST Press \nfor Resume', action:"stopBoost", icon:"st.thermostat.emergency-heat", backgroundColor:"#e86d13")
			state ("off", label: 'Press for Heat \n(or press boost below)', action:"heat", icon:"st.thermostat.heating-cooling-off")
			state ("summer", label: 'Valve full open \nTurn off below',  icon:"st.custom.wuk.clear")
            state ("offline", label: 'Error', action:"refresh",  icon:"st.secondary.refresh-icon")
            // google does not recognise //state ("auto", label: 'auto Press for off', action:"off" , icon:"st.thermostat.auto-cool")
		}
        valueTile("boostLabel", "device.boostLabel", inactiveLabel: true, decoration: "flat", width: 4, height: 1) {
			state ("boostLabel", label:'${currentValue}', defaultState: true)
        }
		
        main (["thermostat"])
		details (["thermostat", "heatingSetpoint", "heatSliderControl", "thermostatMode", "boostLabel", "battery", "boostSliderControl", "boostSwitch", "refresh", "summer"])
	}

	preferences {
  		input (name: "refreshRate", title: "Refresh Rate", type: "enum", 
            options: [
            	"5":"Refresh every 5 minutes (eTRVs)",
                "10":"Refresh every 10 minutes (power monitors)",
                "15":"Refresh every 15 minutes (sockets)",
                "30":"Refresh every 30 minutes",
                "No":"Manual Refresh - Default (Sockets)"
			], defaultValue: "No", required: false) //description: "Select Refresh Rate", required: false)
		input (name: "checkinInfo", title: "Show last Check-in info", type: "enum",
            options: [
            	"Hide", 
                "MM/dd/yyyy h:mma", 
                "h:mma dd/mm/yyyy", 
                "dd/MM/yyyy h:mm", 
                "dd-MM-yyyy HH:mm", 
                "h:mma dd/MM/yy"
                ], defaultValue: "h:mma dd/MM/yy", required: false)// description: "Show last check-in info.", required: false)
        input (name: "emergencyheattemp", title: "Temp to boost to - 13 to 30", type: "number", range: "13..30", required: false) // description: "Boost aka cool to temp", required: false)
        input description: "Summer mode ---  \nOpens the valve fully & prevents any other change to state without press the auto putton", title: "Summer Button", displayDuringSetup: false, type: "paragraph", element: "paragraph"
       	input description: "---Turn On---  \n'OK google set **device name/room** termostat(s) to heat/cool' \nheat=Resume \ncool=boost \n---Turn Off--- \n'Ok google turn off **device name/room** thermostat(s)' \nSaves the last settings and turns off (aka 12deg) \n---Set Temprature--- \n'Ok google set **device name/room** thermostat(s)/temprature to **number** \n---Quiry Temprature--- \n'Ok google what is the temprature in the **device name/room/house**' \nResponse current temp and setpoint \n---Boost--- \n'Ok google set **device name/room** thermostat(s) to COOL \naka boost to temp & time set above", title: "Google Guide", displayDuringSetup: false, type: "paragraph", element: "paragraph"
  	}
}
def installed() { //	===== Update when installed or setting changed =====
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
        case "30":
			runEvery30Minutes(refresh)
			log.info "Refresh Scheduled for every 30 minutes"
			break
		default:
			log.info "Manual Refresh - No Schedule"
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

/// ============ Set temp ==================
def setHeatingSetpoint(temp) { //google uses this for temp change
	unschedule(errordelay)
    if (temp < 12) {
		temp = 12
	}
	if (temp > 30) {
		temp = 30
	}
    if (state.thermostatMode == 'summer' && temp != 30 ){
    log.info "in summer mode - need to turn it off"
    sendEvent(name: "thermostatMode", value: state.thermostatMode, isStateChange: true, displayed: true, descriptionText: "in summer mode turn summer mode off 1st")
    }
    else {
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
// log.debug "temp @ '$temp' in boost- carying on with emergency heat set temp"
        def boosttill = new Date(now() + (state.boostLength * 60000)).format("HH:mma", location.timeZone)
        state.boostLabel = "Boosting till $boosttill"
        state.thermostatOperatingState  = 'cooling'
        runIn(state.boostLength * 60, stopBoost)
// log.debug "setHeatingSetpoint for '$device' status-'$resp.status' - boosting till '$boosttill' with emergencyHeat (aka cool) temp of '$temp'"
        }
        else { //off / auto / heat
        state.boostLabel = "$state.boostLength Min Boost"
        state.thermostatMode = state.summer == 'summer'  ? 'summer' : 'heat'
        state.thermostatOperatingState  = 'heating'
// log.debug "setHeatingSetpoint for '$device' status-'$resp.status' - with temp of '$temp' and '$state.thermostatMode' (should be heat/off)"
        }
        state.counter = 0
        state.heatingSetpoint = resp.data.data.target_temperature
        state.batteryVoltage = resp.data.data.voltage
        state.boostSwitch = 'stby'
        log.info "setHeatingSetpoint complete for '$device' status-'$resp.status' - with temp of '$temp' and mode '$state.thermostatMode', going to .... checkin ...."
        checkin()
    	}
	}
} // =============== end set temp =================

def errordelay(temp) { // delay resending of set temprature
	unschedule(errordelay)
	unschedule(setHeatingSetpoint)
// log.debug "error delay trigerd values - '$temp'"
    def key = "value"
	def value = temp[key] ?: "12"
    log.warn "error delay sending back to setHeatingpoint values - '$temp' Key-'$key' Value-'$value'"
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
// log.debug "this mode is '$mode' and should be off or heat"
    }
    log.info "Saved resume mode is '$state.lastmode'"
}

// ============ button commands ===========
def summer(){ 					//used for summer button
	log.debug "def summer"
    state.summer = 'summer'
    sendEvent(name: "summer", value: state.summer, descriptionText: "summer mode button pressed", displayed: false)
    setThermostatMode('summer') 
}
def heat() { 
	log.debug "def heat"
    state.summer = 'auto'
    sendEvent(name: "summer", value: state.summer, displayed: false)
    setThermostatMode('heat') 
}
def off() { 					//used for off button
	log.debug "def off"
    state.summer = 'auto'
    sendEvent(name: "summer", value: state.summer, displayed: false)
	setThermostatMode('off') 
}
def emergencyHeat() { 			//used for boost button
	log.debug "def emergency heat"
   state.summer = 'auto'
    sendEvent(name: "summer", value: state.summer, displayed: false)
	setThermostatMode('cool')
}
def stopBoost() { 				//used for stop boots on mode
	log.debug "Executing 'stopBoost'"
    state.summer = 'auto'
    sendEvent(name: "summer", value: state.summer, displayed: false)
    setThermostatMode(state.lastmode)
// log.debug "Boost stoped"
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
def ThermostatSetMode() {		// might be used for google
	log.debug "def ThermostatSetMode google"
	setThermostatMode()
}

def setThermostatMode(mode) { 	//requested mode NOT actualy in it yet - -google only uses this for mode change
	mode = mode == 'auto' || mode == 'on' ? 'heat' : mode
    def temp = ''
// log.debug "Executing setThermostatMode with mode '$mode'"
    if (mode == 'summer'){
    	state.thermostatMode = 'summer' //open valve up
        temp = 30
//log.info "MODE Change complete -'$mode' with default temp of '$temp', going to .. setHeatingSetpoint .. "
        setHeatingSetpoint(temp)
    }
    else if  (mode == 'off') {
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint')) 	//save current set temp
        setLastmode(device.currentValue('thermostatMode')) 				//save current mode
        temp = 12
//log.info "MODE Change complete -'$mode' with default temp of 12, going to ...... setHeatingSetpoint ..... "
        setHeatingSetpoint(temp)										//labels for boostswitch-stby and mode-off set in 'checkin' for == 12
    }
    else if (mode == 'emergencyHeat' || mode == 'cool') {
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint'))
        setLastmode(device.currentValue('thermostatMode'))
        state.boostSwitch = 'emergencyHeat'
        state.thermostatMode = 'cool' //aka boost
        temp = settings.emergencyheattemp
//log.info "MODE Change complete - '$mode'(aka boost/emergency heat) with temp of '$settings.emergencyheattemp' going to going to .... setHeatingSetpoint ...... "
        setHeatingSetpoint(temp)
    }
    
    else { //auto heat resume etc
    	//def lastHeatingSetPoint = state.lastHeatingSetPoint 			// recall last saved temp
        if (state.lastHeatingSetPoint == null || state.lastHeatingSetPoint < 12 || state.lastHeatingSetPoint > 30 ) { //(lastHeatingSetPoint == null || lastHeatingSetPoint < 12 || lastHeatingSetPoint > 30 )
        	temp = 17									// capurte any errors
            log.warn " '$mode' last temp out of range going with default temp of '$temp'"
        }
        else {
        	temp = state.lastHeatingSetPoint
        }
		state.boostSwitch = 'stby'
        state.thermostatMode = mode
// log.info "MODE Change complete - '$mode' mode resuming to last temp of'$lastHeatingSetPoint' going to .... setHeatingSetpoint ...."
    	setHeatingSetpoint(temp)
	}
	log.info "MODE Change complete - '$mode' mode with temp of '$temp' going to .. setHeatingSetpoint .."    
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
//log.debug "POLL for  -'${device}' response -'${resp.status}' all good"
    checkin()
    }
}
    
def checkin() {
	if (state.heatingSetpoint == 12) { //from mode off or if set to 12 from another system
    	   	unschedule(stopBoost)
            state.boostSwitch = 'stby'
            state.thermostatMode = 'off'
            state.boostLabel = "$state.boostLength Min Boost"
            state.thermostatOperatingState  = 'idle' //might be for google
    }
//bolt ons for google not sure which are needed
 sendEvent(name: "thermostatOperatingState", value: state.thermostatOperatingState, displayed: false) 
 sendEvent(name: "thermostatSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false)
 sendEvent(name: "ThermostatSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false)
 sendEvent(name: "thermostatTemperatureSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false)
 sendEvent(name: "thermostatTemperatureAmbient", value: state.temperature, unit: "C", displayed: false)
//30-04-18 sendEvent(name: "coolingSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false) 	//needed for google when in cooling aka boost 

        
    sendEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true) 				//mode & off icon
    sendEvent(name: "boostSwitch", value: state.boostSwitch, displayed: false)						// boost button
    sendEvent(name: "boostLabel", value: state.boostLabel, displayed: false) 						//change label back to boost time from start time        
   	sendEvent(name: "temperature", value: state.temperature, unit: "C")								// actual temp
    sendEvent(name: "heatingSetpoint", value: state.heatingSetpoint, unit: "C")						// set temp
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
	log.info "CHECKIN -'$device', MODE='$setmode', TTemp='$state.heatingSetpoint', ATemp='$state.temperature', BOOST='$state.boostLabel' @ '$settings.refreshRate' min refresh rate"
}

def refresh() {
//log.debug "REFRESH -'$device' @ '$settings.refreshRate' min refresh rate"
	poll()
}
