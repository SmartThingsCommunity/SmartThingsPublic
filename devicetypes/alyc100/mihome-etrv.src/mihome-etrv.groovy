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
 *				3.2 - new app terms added, nb google does not like summer mode (valve full open) - but fully functioning
 *  31.07.2018	3.1 - debugin depeciated & depeciated terms depeciated
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
	definition (name: "MiHome eTRV", namespace: "alyc100", author: "Alex Lee Yuk Cheung and updated by mark cockcroft",  ocfDeviceType: "oic.d.thermostat", mnmn: "SmartThings", vid: "SmartThings-smartthings-Z-Wave_Thermostat") {
		
//capability "Polling" not needed as refresh is schdualed in the DH
       	capability "Actuator"						// best practice
        capability "Sensor"							// best practice
        capability "Refresh"
		capability "Thermostat"		//no longer supported
        capability "Temperature Measurement" 		//attribute- temperature
        capability "Thermostat Cooling Setpoint"	//attribute- coolingSetpoint	command - setCoolingSetpoint - having both alows extra settings in routines
        capability "Thermostat Heating Setpoint" 	//attribute- heatingSetpoint 	command - setHeatingSetpoint - having both alows extra settings in routines
		capability "Thermostat Mode" 				//attribute-thermostatMode  auto/eco/rush hour/cool/emergency heat/heat/'off' command-setThermostatMode
        capability "Battery" 
        capability "Health Check"
	
        command "setThermostatMode"					//might need for google
        command "ThermostatSetMode"					//might need for google
        command "setHeatingSetpoint"				//keep main command
        command "setBoostLength"					//keep to set boost length
        command "emergencyHeat"						//used on the boost/stop boost button
        command "stopBoost"							//used on the boost/stop boost button
        command "summer"							//uesed to put in summer mode - (Stats open permainatly)
command "test"
        attribute "lastCheckin", "String"
        attribute "boostLabel", "String"
        attribute "thermostatTemperatureSetpoint", "String"						//might need for google
        attribute "ThermostatSetpoint", "String"								//might be needed for google
        attribute "boostLength", "String"
		attribute "batteryVoltage", "String"
    }

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "thermostat", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("thermostat", label: '${currentValue}°', unit:"C", icon: "https://raw.githubusercontent.com/Mark-C-uk/SmartThingsPublic/master/devicetypes/alyc100/mihome-etrv.src/radiator120x120.png" ,
                defaultState: true, backgroundColors:[
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
//last refreshed
          tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
               	attributeState("lastCheckin", label:'${currentValue}', defaultState: true)
           	}
		}
//set target temp
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state ("heatingSetpoint", label:'${currentValue}°',  defaultState: true, backgroundColors:[
					[value: 0, color: "#b8c2de"],
					[value: 10, color: "#bbe1ea"],
					[value: 13, color: "#ddf1e4"],
					[value: 17, color: "#c6e9bc"],
					[value: 20, color: "#faf3b2"],
					[value: 25, color: "#f0c9b2"],
					[value: 29, color: "#eabdbd"]
				]
                )
		}
		        valueTile("temp", "device.temperature", width: 2, height: 2, inactiveLabel: true,) { // hear to enable it to show in activity feed
					state ("temp", label:'${currentValue}°', defaultState: true, backgroundColors:[
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
//target temp slider
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 2, inactiveLabel: false, range:"(12..30)") {
			state ("heatingSetpoint", action:"setHeatingSetpoint") //need to send to a lag
		}
// battery
        valueTile("battery", "device.batteryVoltage", width: 1, height: 1) {
			state ("battery", label:'${currentValue}V',  icon:"st.samsung.da.RC_ic_charge",defaultState: true, backgroundColors:[
               					[value: 3.2, color: "#44b621"],
								[value: 2.85, color: "#f1d801"],
								[value: 2.5, color: "#bc2323"]
         		           ]
                           )
        }
        valueTile("batteryper", "device.battery", width: 1, height: 1) {
			state ("battery", label:'${currentValue}%', icon:"st.samsung.da.RC_ic_charge", defaultState: true, backgroundColors:[
               					[value: 100, color: "#44b621"],
								[value: 50, color: "#f1d801"],
								[value: 0, color: "#bc2323"]
         		           ]
                           )
        }
//refresh
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state ("refresh", label:'refresh', action:"refresh", icon:"st.secondary.refresh-icon", defaultState: true)
            state ("bad", label:'bad refresh', action:"refresh", icon:"st.secondary.refresh-icon", backgroundColor: "#bc2323")
		}
//set boost time
        controlTile("boostSliderControl", "device.boostLength", "slider", height: 2, width: 2, inactiveLabel: false, range:"(30..120)") {
			state ("boostLength", action:"setBoostLength", defaultState: true)
		}
//boost button
        standardTile("boostSwitch", "device.boostSwitch", decoration: "flat", height: 2, width: 2, inactiveLabel: false) {
			state ("stby", label:'Press to \nboost', action: "emergencyHeat", icon:"st.alarm.temperature.overheat")
            state ("emergencyHeat", label: 'Press Reboost', action:"emergencyHeat", icon:"st.Health & Wellness.health7", backgroundColor: "#bc2323")			
        }
//boost display time
        valueTile("boostLabel", "device.boostLabel", inactiveLabel: true, decoration: "flat", width: 4, height: 1) {
			state ("boostLabel", label:'${currentValue}', defaultState: true)
        }        
//mode display/buttoon
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state ("heat", label: 'Press for \nOff', action:"off" , icon:"st.thermostat.heat-auto", backgroundColor:"#00a0dc")
            state ("cool", label: 'In BOOST Press \nfor Resume', action:"stopBoost", icon:"st.thermostat.emergency-heat", backgroundColor:"#e86d13")
			state ("off", label: 'Press for Heat \n(or press boost below)', action:"heat", icon:"st.thermostat.heating-cooling-off")
			state ("summer", label: 'Valve full open \nTurn off below',  icon:"st.custom.wuk.clear")
            state ("offline", label: 'Error', action:"refresh",  icon:"st.secondary.refresh-icon")
		}
//summer mode on/off button - summer mode prevents routiens etc from changing the temp ie clossing the valve becaus its hot in summer also blocks and temp change to save battery
		standardTile("summer", "device.summer", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state ("auto", label:'Summer', action: "summer", icon:"st.thermostat.auto")
            state ("summer", label:'Deactivite', action: "heat", icon:"st.custom.wuk.clear")
 		}
        main (["thermostat"])
		details (["thermostat", "heatingSetpoint", "heatSliderControl", "thermostatMode", "boostLabel", "battery","batteryper","boostSliderControl", "boostSwitch", "refresh", "summer"])
	}

	preferences {
//setting how often to refesh with miHome
  		input (name: "checkinInfo", title: "Show last Check-in info", type: "enum", options: ["Hide", "Show"], required: false)		//setting for diplay format for last comunication/refresh
        input (name: "emergencyheattemp", title: "Temp to boost to - 13 to 30", type: "number", range: "13..30", required: false) // //setting default boost to temp for device
//help descriptions
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
	unschedule()
    state.counter = state.counter
    state.counter = 0
    state.boostLength = 60
    runIn(2, update)
}
def update() {
	log.info "update running"
	runEvery5Minutes(poll, [overwrite: true])
}

def uninstalled() {
    unschedule()
}
// ======== end off settings ========

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
}
/// ===================================================

/// ============ Set temp ==================
def setCoolingSetpoint(temp){
	log.info "Set cooling setpoint temp of ${temp}, sending temp value to setHeatingSetpoint"
	setHeatingSetpoint(temp)
}
def setHeatingSetpoint(temp) {
	unschedule(poll)
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
    def resp = parent.apiGET("subdevices/set_target_temperature?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger(), temperature: temp]).toString()))
//log.debug "setting response ${resp.status} ${resp.data}"
    if (resp.status != 200) {
    	unschedule(errordelay)
    	log.error "Unexpected result in seting temp ${resp.status}"
        if (state.counter == null || state.counter >= 6) {
			state.counter = 0
		}
        if (state.counter < 5) {
        	state.counter = state.counter + 1
        	sendEvent(name: "refresh", value: '', descriptionText: "error setting temp '${state.counter}' try", isStateChange: true)
        	log.warn "runnting set temp '${state.counter}' attempt"
        	runIn (7, errordelay,[data: [value: temp]])
        }
        else {
        	unschedule(errordelay) 			//clean up to prevent memory leak
       		unschedule(setHeatingSetpoint) 	//clean up to prevent memory leak
        	sendEvent(name: "thermostat", value: 'offline', descriptionText: "error setting temp try '${state.counter}' try. The device is offline", isStateChange: true)
            log.error "error setting temp try '${state.counter}' try. The device is offline"
            state.counter = 0
		}
    }
    else {
    	unschedule(errordelay) 				//clean up to prevent memory leak
        unschedule(setHeatingSetpoint) 		//clean up to prevent memory leak
        unschedule(stopBoost)				// leave hear so that if setting temp fails completely at least stopBoost will still be active
    	if (state.boostSwitch == 'emergencyHeat'){
        	def boosttill = new Date(now() + (state.boostLength * 60000)).format("HH:mma", location.timeZone)
        	state.boostLabel = "Boosting till ${boosttill}"
        	state.thermostatOperatingState  = 'cooling'
        	runIn(state.boostLength * 60, stopBoost)
        }
        else { //off / auto / heat
        	state.boostLabel = "${state.boostLength} Min Boost"
        	state.thermostatMode = state.summer == 'summer'  ? 'summer' : 'heat'
        	state.thermostatOperatingState  = 'heating'
        }
        
        state.counter = 0
        state.heatingSetpoint = resp.data.data.target_temperature
        state.batteryVoltage = resp.data.data.voltage
        state.updatedat = resp.data.data.updated_at
       // state.temperature = resp.data.data.last_temperature
        state.boostSwitch = 'stby'
        log.info "setHeatingSetpoint complete for '${device}' status-'${resp.status}' - with temp of '${temp}' and mode '${state.thermostatMode}', going to .... checkin ...."
        log. debug "response data updated at $resp.data.data.updated_at"
        //runEvery5Minutes(poll, [overwrite: true])
        checkin()
    	}
	}
    runIn(5*60, refresh)
} 
// =============== end set temp =================

def errordelay(temp) { // delay resending of set temprature
	unschedule(errordelay)
	unschedule(setHeatingSetpoint)
    def key = "value"
	def value = temp[key] ?: "12"
    log.warn "error delay sending back to setHeatingpoint values - '${temp}' Key-'${key}' Value-'${value}'"
	setHeatingSetpoint(value)
}

def setLastHeatingSetpoint(temp) { //this is to resume after off or boost
	if (temp < 12 || temp > 30 ) {
    	state.lastHeatingSetPoint = 16
    	log.warn "'${temp}' is out of 12-30 range setting to 16"
    }
    else if (state.thermostatMode == 'emergencyHeat' || state.thermostatMode == 'cool' ) {
		log.warn "emergencyHeat, temp not saved as its '${temp}'"
	}
	else { 									//could be 12 to turn back off
    state.lastHeatingSetPoint = temp
    }
    log.info "Saved resume temp is '${state.lastHeatingSetPoint}'"
}

def setLastmode(mode) { 					//this is to resume after off or boost
	if (state.boostSwitch == 'emergencyHeat' || mode == 'cool'){
    	log.warn "'${mode}' not wanted"
    }
    else if (state.lastmode == 'off' && mode == 'off'){
    	log.warn "last mode was off and current mode is off, not saving twice = mode HEAT"
        state.lastmode = 'heat'
    }
    else if (mode == null){ 				//heat and other modes
    	log.warn "mode is NULL seting mode to heat"
        state.lastmode = 'heat'
    }
	else {
    	state.lastmode = mode
    }
    log.info "Saved resume mode is '${state.lastmode}'"
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
    state.boostLabel = "${state.boostLength} Min Boost"
    setThermostatMode(state.lastmode)
}

def setBoostLength(minutes) { //set boost length
	if (minutes < 30) {
		minutes = 30
	}
	if (minutes > 120) {
		minutes = 120
	}
    state.boostLength = minutes
    state.boostLabel = "${state.boostLength} Min Boost"
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
	log.debug "mode in setThermostatMode with mode '${mode}'"
    mode = mode == 'auto' || mode == 'on' ? 'heat' : mode
    def temp = ''
    if (mode == 'summer'){
    	state.thermostatMode = 'summer' //open valve up
        temp = 30
        setHeatingSetpoint(temp)
    }
    else if  (mode == 'off') {
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint')) 	//save current set temp
        setLastmode(device.currentValue('thermostatMode')) 				//save current mode
        temp = 12
        setHeatingSetpoint(temp)										//labels for boostswitch-stby and mode-off are set in 'checkin' for == 12
    }
    else if (mode == 'emergencyHeat' || mode == 'cool') {
    	setLastHeatingSetpoint(device.currentValue('heatingSetpoint'))
        setLastmode(device.currentValue('thermostatMode'))
        state.boostSwitch = 'emergencyHeat'
        state.thermostatMode = 'cool' //aka boost
        temp = settings.emergencyheattemp
        setHeatingSetpoint(temp)
    }
    
    else { //auto heat resume etc
        if (state.lastHeatingSetPoint == null || state.lastHeatingSetPoint < 12 || state.lastHeatingSetPoint > 30 ) { //(lastHeatingSetPoint == null || lastHeatingSetPoint < 12 || lastHeatingSetPoint > 30 )
        	temp = 17									// capurte any errors
            log.warn " '${mode}' last temp out of range going with default temp of '${temp}'"
        }
        else {
        	temp = state.lastHeatingSetPoint
        }
		state.boostSwitch = 'stby'
        state.thermostatMode = mode
    	setHeatingSetpoint(temp)
	}
	log.info "MODE Change complete - '${mode}' mode with temp of '${temp}' going to .. setHeatingSetpoint .."    
}	//end mode setting

def refresh() {
//	log.debug "refresh triggerd for ${device}"
	unschedule(refresh)
	runEvery5Minutes(poll, [overwrite: true])
	poll()
}

def poll() {
//	unschedule(refresh)
	def resppar = parent.state.data //this is to get the data from the app insted so the app ONLY as to quiry every 5 min
	if (resppar != null){
		def dvid = device.deviceNetworkId.toInteger()
		def dvkey1 = resppar.data.id.findIndexOf { it == (dvid) }
				//log.debug "key only data = ${resppar.data[(dvkey1)]}"
        		//log.debug "mihome index - '$dvkey1', name - ${resppar.data[(dvkey1)].label}"
    	state.temperature = resppar.data[(dvkey1)].last_temperature
    	state.heatingSetpoint = resppar.data[(dvkey1)].target_temperature
    	state.batteryVoltage = resppar.data[(dvkey1)].voltage
    	state.updatedat = resppar.data[(dvkey1)].parent_device_last_seen_at // .updated_at not used as this only updates hourly for some reason
        sendEvent(name: "refresh", value: "refresh", displayed: false)
    }

	else {
    	sendEvent(name: "refresh", value: "bad", descriptionText: "The device failed POLL", isStateChange: true)
    	log.warn "POLL - ${device} failed POLL"
    }
    
    checkin()
}
    
def checkin() {
	if (state.heatingSetpoint == 12) { //from mode off or if set to 12 from another system
    	   	unschedule(stopBoost)
            state.boostSwitch = 'stby'
            state.thermostatMode = 'off'
            state.boostLabel = "${state.boostLength} Min Boost"
            state.thermostatOperatingState  = 'idle' //might be for google
    }
//bolt ons for google not sure which are needed
 	sendEvent(name: "thermostatOperatingState", value: state.thermostatOperatingState, displayed: false) 
	sendEvent(name: "ThermostatSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false)
	sendEvent(name: "thermostatTemperatureSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false)
	sendEvent(name: "thermostatTemperatureAmbient", value: state.temperature, unit: "C", displayed: false)
//main display stuff
	if (state.batteryVoltage == null){
    	state.batteryVoltage = '0'
    }
//log.debug "bat V '${state.batteryVoltage}"
    state.battery = Math.round(((state.batteryVoltage-2.5)/0.7)*100)		// 0.7 is used to calculete %, ie diferance between max-min volts (3.2-2.5)
    state.batteryVoltage = 	Math.round(state.batteryVoltage * 1000)/1000 		// *1000)/1000 to round to 3 decimal places
	sendEvent(name: "thermostatMode", value: state.thermostatMode, displayed: true) 				//mode & off icon
    sendEvent(name: "boostSwitch", value: state.boostSwitch, displayed: false)						// boost button
    sendEvent(name: "boostLabel", value: state.boostLabel, displayed: false) 						//change label back to boost time from start time        
   	sendEvent(name: "temperature", value: state.temperature, unit: "C")								// actual temp
    sendEvent(name: "heatingSetpoint", value: state.heatingSetpoint, unit: "C")						// set temp
    sendEvent(name: "coolingSetpoint", value: state.heatingSetpoint, unit: "C", displayed: false)	// set temp
    sendEvent(name: "batteryVoltage", value: state.batteryVoltage, unit: "V") 
	sendEvent(name: "battery", value: state.battery, unit: "%") 

	def setmode = state.thermostatMode // for log info below
    if (state.thermostatMode == 'cool'){
    	setmode = state.thermostatMode + ' (aka boost)'
    }
    if (checkinInfoFormat != 'Hide') {
 	   sendEvent(name: "lastCheckin", value: state.updatedat, displayed: false)    //value: now, 
    }
	log.info "CHECKIN-${device}', MODE='${setmode}', TTemp='${state.heatingSetpoint}', ATemp='${state.temperature}', BOOST='${state.boostLabel}', BAT='${state.battery}%'" 
}