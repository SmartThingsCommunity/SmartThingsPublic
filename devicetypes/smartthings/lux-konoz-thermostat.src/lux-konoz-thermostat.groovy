/**
 *  KONOZ Thermostat Sarah
 *
 *  Copyright 2018 Sarah
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
	definition (name: "LUX KONOZ Thermostat", namespace: "smartthings", author: "Sarah") {
		capability "Actuator"
		capability "Battery"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Temperature Measurement"
		capability "Thermostat"

		command "setpointUp"
		command "setpointDown"
		command "switchMode"
		command "switchFanMode"
		command "poll"

		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0020,0201,0202,0204,0B05", outClusters: "000A, 0019", manufacturer: "LUX", model: "KONOZ", deviceJoinName: "LUX KONOz Thermostat"
	}

    tiles (scale: 2){
        multiAttributeTile(name:"thermostat", type:"generic", width:6, height:4)
        {
        	tileAttribute("device.temperature", key: "PRIMARY_CONTROL")
            {
                attributeState("temperature", label:'${currentValue}°', backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
				)
            }
/*
            tileAttribute("device.batteryIcon", key: "SECONDARY_CONTROL")
            { // change to batteryIcon
                attributeState("ok_battery", label:'${currentValue}%', icon:"st.arlo.sensor_battery_4")
                attributeState("low_battery_60", label:'Low Battery', icon:"st.arlo.sensor_battery_2")
                attributeState("low_battery_30", label:'Low Battery', icon:"st.arlo.sensor_battery_1")
                attributeState("low_battery_07", label:'Low Battery', icon:"st.arlo.sensor_battery_0")
                attributeState("eol_battery", label:'Change Battery', icon:"st.arlo.sensor_battery_0")
                attributeState("no_battery", icon:"st.nest.empty")
            }
*/            
            tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
                attributeState "VALUE_UP", action: "setpointUp"
                attributeState "VALUE_DOWN", action: "setpointDown"
            }
        }
        
        
        // mode changes "off" -> "auto" -> "heat" -> "emergency heat" -> "cool" -> "off"
        standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
            state "off", action:"switchMode", nextState:"updating", icon: "st.thermostat.heating-cooling-off"
            state "auto", action:"switchMode", nextState:"updating", icon: "st.thermostat.auto"
            state "heat", action:"switchMode", nextState:"updating", icon: "st.thermostat.heat"
            state "emergency heat", action:"switchMode", nextState:"updating", icon: "st.thermostat.emergency-heat"
            state "cool", action:"switchMode", nextState:"updating", icon: "st.thermostat.cool"
            state "updating", label: "Updating...",nextState:"updating", icon: "st.secondary.secondary"
        }
        standardTile("fanMode", "device.thermostatFanMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
            //state "off",  action: "switchFanMode", nextState: "updating", icon: "st.thermostat.fan-off", backgroundColor: "#CCCCCC", defaultState: true
            state "auto", action:"switchFanMode", nextState:"updating", icon: "st.thermostat.fan-auto"
            state "on", action:"switchFanMode", nextState:"updating", icon: "st.thermostat.fan-on"
            state "updating", label: "Updating...", nextState:"updating", icon: "st.secondary.secondary"
        }
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", width: 2, height:1, decoration: "flat") {
            state "thermostatOperatingState", label:'${currentValue}', backgroundColor:"#ffffff"  
        }
        standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", label:"refresh", icon:"st.secondary.refresh-icon"
        }
/*
        standardTile("UILock", "device.lockMode", width:2, height:2, inactiveLabel: false) {
            state "lockOff", label:'UNLOCK', action:"ToggleLockMode", nextState:"updating"
            state "LockOn", label:'LOCK', action:"ToggleLockMode", nextState:"updating"
            state "updating", label: "Updating...", nextState:"updating", icon: "st.secondary.secondary"
        }
*/
		main "thermostat"
//        details(["thermostat", "refresh", "mode", "fanMode", "thermostatOperatingState", "UILock"])
        details(["thermostat", "refresh", "mode", "fanMode", "thermostatOperatingState"])
    }
    /*
    preferences {
        section() {
            input "tempOffset", "decimal", title: "Calibrate your thermostat?", description: "Adjust by this amount", range: "-5.0..5.0", displayDuringSetup: false
            input("systemModes", "enum",
                title: "Avalable thermostat control modes\nSelect the modes the thermostat has been configured for",
                description: "off, heat, cool", defaultValue: "3", required: true, multiple: false,
                options:["1":"off, heat",
                        "2":"off, cool",
                        "3":"off, heat, cool"]
            )
            input ("HeatFanOption", "boolean",
        		title: "Turn on Fan while Heating?",
        		description: "Tips: Gas Furnace / Boiler does not need Fan control", 
                options: ["Yes","No"], defaultValue: "Yes",
                displayDuringSetup: true
			)
           input ("emerHeatEnable", "boolean",
        		title: "Turn on Emergency Heat mode?",
        		description: "Tips: For Heat Pump with Auxliary Heating System Only", 
                options: ["Yes","No"],
                displayDuringSetup: true
			)
            input("pollRate", "boolean",
                title: "Turn thermostat to responsive mode?",
                description: "Tips: Set to fast only if you have C-wire otherwise the battery life will be shorten",
                options: ["Yes","No"], defaultValue: "No",
                displayDuringSetup: true
            )
           input ("statLock", "boolean",
        		title: "Lock this thermostat?",
        		description: "Tips: You can control it through the app", 
                options: ["Yes","No"], defaultValue: "No",
                displayDuringSetup: true
			)

		}
        
//        section(){
//            input "theTime1", "time", title: "Start Time 1"
//            input "theTime2", "time", title: "Start Time 2"
//		}

//		input "temp", "decimal", title: "Degrees", description: "Tset", range: "40..95", displayDuringSetup: false

    } 
    */
}

// Globals
private getIDENTIFY_CLUSTER()                      	 { 0x0003 }
private getATTRIBUTE_IDENTIFY_TIME()             	 { 0x0000 }

private getPOLL_CONTROL_CLUSTER()                    { 0x0020 }
private getCOMMAND_CHECKIN_RESPONSE()          	 	 { 0x0000 }
private getCOMMAND_FAST_POLL_STOP()         		 { 0x0001 }
private getCOMMAND_SET_LONG_POLL_INTERVAL()          { 0x0002 }
private getCOMMAND_SET_SHORT_POLL_INTERVAL()         { 0x0003 }

private getTHERMOSTAT_CLUSTER()                      { 0x0201 }
private getATTRIBUTE_LOCAL_TEMPERATURE()             { 0x0000 }
private getATTRIBUTE_HVAC_SYSTEM_TYPE_CONFIGURATION(){ 0x0009 }
private getATTRIBUTE_OCCUPIED_COOLING_SETPOINT()     { 0x0011 }
private getATTRIBUTE_OCCUPIED_HEATING_SETPOINT()     { 0x0012 }
private getATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT()       { 0x0015 }
private getATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT()       { 0x0016 }
private getATTRIBUTE_MIN_COOL_SETPOINT_LIMIT()       { 0x0017 }
private getATTRIBUTE_MAX_COOL_SETPOINT_LIMIT()       { 0x0018 }
private getATTRIBUTE_MIN_SETPOINT_DEAD_BAND()        { 0x0019 }
private getATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION() { 0x001b }
private getATTRIBUTE_SYSTEM_MODE()                   { 0x001c }
private getATTRIBUTE_THERMOSTAT_RUNNING_MODE()       { 0x001e }
private getATTRIBUTE_THERMOSTAT_RUNNING_STATE()      { 0x0029 }

private getFAN_CONTROL_CLUSTER()                     { 0x0202 }
private getATTRIBUTE_FAN_MODE()                      { 0x0000 }
private getATTRIBUTE_FAN_MODE_SEQUENCE()             { 0x0001 }

private getATTRIBUTE_BATTERY_VOLTAGE()               { 0x0020 }

private getTypeUINT16()	{ 0x21 }
private getTypeINT16() 	{ 0x29 }
private getTypeENUM8() 	{ 0x30 }


def getSupportedModes() {
    return (state.systemModes ? supportedModesMap[state.systemModes] : ["off", "heat", "cool"])
}


def getSupportedModesMap() {
    [
        "0":["off"],
        "1":["off", "heat"],
        "2":["off", "cool"],
        "3":["off", "heat", "cool"]
    ]
}

def getFanModeSequence() {
    return (state.fanModeSequence ? fanModeSequenceMap[state.fanModeSequence] : ["on", "auto"])
    //return (["on", "auto"])	// only support [on/auto] modes
}

def getFanModeSequenceMap() {
    [
        "00":["low", "medium", "high"],
        "01":["low", "high"],
        "02":["low", "medium", "high", "auto"],
        "03":["low", "high", "auto"],
        //"04":["on", "auto"]
        "04":["auto", "on"]		//force the default to "auto"
    ]
}


def installed() {
    log.debug "installed"
    // set default supportedModes as device doesn't report according to its configuration
    sendEvent(name: "supportedThermostatModes", value: ["off"], eventType: "ENTITY_UPDATE", displayed: false)

    // set default temperature to see if the update is faster after provisioning
    //sendEvent(name: "temperature", value: DEFAULT_TEMPERATURE, unit: "°F", eventType: "ENTITY_UPDATE")
    //sendEvent(name: "thermostatSetpoint", value: DEFAULT_THERMOSTAT_SETPOINT, unit: "°F", eventType: "ENTITY_UPDATE")

    // Pairing can be 'silent' meaning the mobile app will not call updated() so initialize() needs also to be called by installed()
    // make sure configuration and initial poll is done by the DTH, but to try avoiding multiple config/poll be done us runIn ;o(
    runIn(2, "initialize", [overwrite: true])  // Allow configure command to be sent and acknowledged before proceeding
    initialize()
}

def updated() {
    log.debug "updated"
    // make sure supporedModes are in sync
    sendEvent(name: "supportedThermostatModes", value: supportedModes, eventType: "ENTITY_UPDATE", displayed: false)
    // Make sure we poll all attributes from the device
    state.pollAdditionalData = state.pollAdditionalData ? state.pollAdditionalData - (24 * 60 * 60 * 1000) : null
    // initialize() needs to be called after device details has been updated() but as installed() also calls this method and
    // that LiveLogging shows updated is being called more than one time, try avoiding multiple config/poll be done us runIn ;o(
    runIn(3, "initialize", [overwrite: true])
}

def initialize() {
    log.debug "initialize() - binding & attribute report"
    
    state.isIdentify = true		//create a flag to indicate identify-period. APP should turn off the identify-period upon receiving the 1st ambient temperature
    log.debug "initialize(): state.isIdentify = $state.isIdentify"

	//default sytemModes should be "off" only
    initSupportedModes() 

	//default setPoint should be "--" 
	initSetPoint()
	
	//default fanModes should be "on, auto" (only support this modes) 
    initFanModes() 

	//default Operating State should be "--" 
	initOperatingState()

    sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    // send configure commad to the thermostat

		//def cmds = zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_CHECKIN_RESPONSE, "01F003")			//start fast-poll mode for 60s, speeding up the provisioning
		//def	cmds = zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_SET_LONG_POLL_INTERVAL, "02000000")	//set polling time to 14s; little endian of 00 00 00 38 (0x38=56qs = 14 sec)
		//cmds += zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_CHECKIN_RESPONSE, "010002")			//start fast-poll mode for 60s, speeding up the provisioning

		def cmds = [
            //Set long poll interval to 14s, which keeps the KONOZ alive by getting the MAC from KONOZ
            "raw 0x0020 {11 00 02 02 00 00 00}", 
            "send 0x${device.deviceNetworkId} 1 1",
            //Thermostat - Cluster 201
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}",
            "zcl global send-me-a-report 0x201 0 0x29 1 1620 {1400}",      // report temperature changes over 0.5°C (0x3200 in little endian)
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x0011 0x29 1 1620 {1400}", // report cooling setpoint delta: 0.2°C
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x0012 0x29 1 1620 {1400}", // report heating setpoint delta: 0.5°C
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x001C 0x30 1 1620 {}",     // report system mode
            "send 0x${device.deviceNetworkId} 1 1",
            "zcl global send-me-a-report 0x201 0x0029 0x19 1 1620 {}",     // report running state
            "send 0x${device.deviceNetworkId} 1 1",
            //Fan Control - Cluster 202
            "zdo bind 0x${device.deviceNetworkId} 1 1 0x202 {${device.zigbeeId}} {}",
            "zcl global send-me-a-report 0x202 0 0x30 1 1620 {}",          // report fan mode
            "send 0x${device.deviceNetworkId} 1 1",
        ]
    //Power Control - Cluster 0001 (report battery status)
    //cmds += zigbee.batteryConfig()

	//turn off the identify to avoid multiple "permit join" process
    //cmds += zigbee.writeAttribute(0x0003, 0x0000, 0x21, 0x0000) //identify time = 0
    //cmds += zigbee.writeAttribute(IDENTIFY_CLUSTER, ATTRIBUTE_IDENTIFY_TIME, typeUINT16, 0x0000)	//identify time = 0

	sendZigbeeCmds(cmds, 400)
    // Delay polling device attribute until the config is done
    runIn(5, "pollDevice", [overwrite: true])
}

// parse events into attributes
def parse(String description) {
	log.debug "Parse description $description"
	List result = []
    def descMap = zigbee.parseDescriptionAsMap(description)
	log.debug "Desc Map: $descMap"
	List attrData = [[cluster: descMap.cluster ,attrId: descMap.attrId, value: descMap.value]]
	descMap.additionalAttrs.each {
	    attrData << [cluster: descMap.cluster, attrId: it.attrId, value: it.value]
	}
	attrData.each {
		def map = [:]    
	    // Thermostat Cluster Attribute Read Response
	    if (it.cluster == "0201") { // THERMOSTAT_CLUSTER
	        def locationScale = getTemperatureScale() ?:"F"
	        def mode = device.currentValue("thermostatMode") ?: "off"
	        switch (it.attrId) {
	            case "0000": // ATTRIBUTE_LOCAL_TEMPERATURE
	                map.name = "temperature"
	                map.unit = locationScale
	                log.debug "attribute_0000 temperature $it.value"
	                map.value = Math.round(getTempInLocalScale(parseTemperature(it.value), "C")) // Zibee always reports in °C
	                log.debug "Temperature after getTempLocalScale $map.value"
					if (state.isIdentify)
					{	// clear the identify-period upon the 1st Ambient reading is received
						state.isIdentify = false
					    def cmds = zigbee.writeAttribute(IDENTIFY_CLUSTER, ATTRIBUTE_IDENTIFY_TIME, typeUINT16, 0x0000)	//identify time = 0
					    //cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMPERATURE)	// make the ambient temperature last to send
						//cmds += zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_CHECKIN_RESPONSE, "012800")			//start fast-poll mode for 10s, speeding up the reading attribute after startup
						cmds += zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_SET_LONG_POLL_INTERVAL, "38000000")	//set polling time to 14s; little endian of 00 00 00 38 (0x38=56qs = 14 sec)
			            sendZigbeeCmds(cmds)
					}
	                break
	            case "0009": // ATTRIBUTE_HVAC_SYSTEM_TYPE_CONFIGURATION
	                // Make sure operating state is in sync
	                map.name = "HVACsystemTypeConfig"
	                //map.data = [supportedThermostatModes: supportedModes]
	                //map.value = systemModeMap[it.value]
	                log.debug "attribute_0009 HVACsystemTypeConfig : $it.value"
					state.supportedModesReady = true	// enable mode-change after receiving the supported-modes
	                log.debug "state.supportedModesReady = $state.supportedModesReady"
	                map.value = Integer.parseInt(it.value,16)
	                updateSupportedModes(map.value)
	                log.debug "SupportedModesMap : $map.value (0x$it.value)"
	                log.debug "SupportedModes : $supportedModes"
	                break
				case "0011": // ATTRIBUTE_OCCUPIED_COOLING_SETPOINT
					log.debug "attribute_0011 : state.sendingSetpointsZB = $state.sendingSetpointsZB"
	                if (state.sendingSetpointsZB == true){
	                    log.debug "attribute_0011 update is skipped"
	                }
	                else{
	                    state.deviceCoolingSetpoint = parseTemperature(it.value)
	                    map.name = "coolingSetpoint"
	                    map.unit = locationScale
	                    log.debug "attribute_0011 coolingSetpoint $it.value"
	                    map.value = getTempInLocalScale(state.deviceCoolingSetpoint, "C") // Zibee always reports in °C
	                    runIn(2, "updateThermostatSetpoint", [overwrite: true])
	                }
	                break
	            case "0012": // ATTRIBUTE_OCCUPIED_HEATING_SETPOINT
					log.debug "attribute_0012 : state.sendingSetpointsZB = $state.sendingSetpointsZB"
	                if (state.sendingSetpointsZB == true){
	                    log.debug "attribute_0012 update is skipped"
	                }
	                else{
	                    state.deviceHeatingSetpoint = parseTemperature(it.value)
	                    map.name = "heatingSetpoint"
	                    map.unit = locationScale
	                    log.debug "attribute_0012 heatingSetpoint $it.value"
	                    map.value = getTempInLocalScale(state.deviceHeatingSetpoint, "C") // Zibee always reports in °C
	                    runIn(2, "updateThermostatSetpoint", [overwrite: true])
	                }
					break
	            case "0015": // ATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT
					log.debug "attribute_0015 : minHeatSetpointCelsius = $it.value"
	                updateMinSetpointLimit("minHeatSetpointCelsius", it.value)
	                break
	            case "0016": // ATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT
					log.debug "attribute_0016 : maxHeatSetpointCelsius = $it.value"
	                updateMaxSetpointLimit("maxHeatSetpointCelsius", it.value)
	                break
	            case "0017": // ATTRIBUTE_MIN_COOL_SETPOINT_LIMIT
					log.debug "attribute_0017 : minCoolSetpointCelsius = $it.value"
	                updateMinSetpointLimit("minCoolSetpointCelsius", it.value)
	                break
	            case "0018": // ATTRIBUTE_MAX_COOL_SETPOINT_LIMIT
					log.debug "attribute_0018 : maxCoolSetpointCelsius = $it.value"
	                updateMaxSetpointLimit("maxCoolSetpointCelsius", it.value)
	                break
	            case "001c": // ATTRIBUTE_SYSTEM_MODE
	                log.debug "attribute_001c thermostatMode : $it.value"
	                log.debug "state.supportedModesReady = $state.supportedModesReady"
					if (state.supportedModesReady) 	// ignore mode change if  the supported-modes has not yet been received 
					{
		                // Make sure operating state is in sync
		                map.name = "thermostatMode"
		                if (state.switchMode) {
		                    // set isStateChange to true to force update if switchMode failed and old mode is returned
		                    map.isStateChange = true
		                    state.switchMode = false
		                }
		                map.data = [supportedThermostatModes: supportedModes]
		                map.value = systemModeMap[it.value]
		                
		                log.debug "System Operating Mode :  $map.value"
		                // in case of refresh, allow heat/cool setpoints to be reported before updating setpoint
		                runIn(10, "updateThermostatSetpoint", [overwrite: true])
		                
                        // Make sure operating state is in sync
					}
	                ping()
	                break
	            case "001e": // ATTRIBUTE_THERMOSTAT_RUNNING_MODE
	            		def runningMode = systemModeMap[it.value] ?: "off"
	                device.updateDataValue("thermostatRunningMode", systemModeMap[it.value])
	                log.debug "attribute_001e thermostatRunningMode : $it.value"
	                log.debug "Thermostat Running Mode :  $runningMode"
	                break
	            case "0029": // ATTRIBUTE_THERMOSTAT_RUNNING_STATE
	                map.name = "thermostatOperatingState"
	                map.value = thermostatRunningStateMap[it.value]
	                log.debug "attribute_0029 thermostatRunningState : $it.value"
	                log.debug "Thermostat Running State :  $map.value"
	                break
	         } // switch (it.attrId)
	    } // THERMOSTAT_CLUSTER
	    // Fan Control Cluster Attribute Read Response
	    else if (it.cluster == "0202") {
	        def currentFanMode = device.currentValue("thermostatFanMode") ?: "auto"
	        switch (it.attrId) {
	            case "0000": // ATTRIBUTE_FAN_MODE
		            log.debug "attribute_0000 fanMode : $it.value"
		            log.debug "state.fanModeSequenceReady : $state.fanModeSequenceReady"
	                // Make sure operating state is in sync
	                ping()
	    			// fan-mode-sequence is ignored and use the default ["auto", "on"]
	    			//if (state.fanModeSequenceReady) {	// ignore fan-change-mode if the fan-mode-sequence has not yet been received 
					    map.name = "thermostatFanMode"
		                if (state.switchFanMode) {
		                    // set isStateChange to true to force update if switchMode failed and old mode is returned
		                    map.isStateChange = true  
		                    state.switchFanMode = false
		                }
		                map.data = [thermostatFanModeSequence: fanModeSequence]	
		                map.value = fanModeMap[it.value]
		                log.debug "fanModeSequence :  $fanModeSequence"
		                log.debug "fanMode :  $map.value"
					//}else {
		            //    log.debug "state.fanModeSequenceReady = false. Sequence is not ready"
					//}
	                break
	            case "0001": // ATTRIBUTE_FAN_MODE_SEQUENCE
	                log.debug "attribute_0001 fanModeSequence : $it.value"
	                map.name = "thermostatFanModeSequence"
	                map.data = [thermostatFanModeSequence: fanModeSequence]
					state.fanModeSequenceReady = true	// enable mode-change after receiving the supported-modes
	                log.debug "state.fanModeSequenceReady = $state.fanModeSequenceReady"
	                //state.fanModeSequence = it.value
	                state.fanModeSequence = "04"	//ignore the update from DTH. force to use the default sequence of ["0n, "auto"]
	                def fanModeSequence = fanModeSequence
	                log.debug "state.fanModeSequence = $state.fanModeSequence"
	                log.debug "fanModeSequence = $fanModeSequence"
	                break
	        }
	    } // Fan Control Cluster
	    // Power Configuration Cluster
	    else if (it.cluster == "0001") {
	        if (it.attrId == "0020") {
	            // removed battery indication on 2018-03-29
	            //updateBatteryStatus(it.value)
	        }
	    }
	    //log.debug "map = $map"
	    //log.debug "map.value = $map.value"
	    
	    if (map) {
	      //log.debug "createEvent(map) - to refresh the attributes !!!"
		    result << createEvent(map)
	    }
		log.debug "Parse returned $map"
	}
    return result
}

// =============== Help Functions - Don't use log.debug in all these functins ===============
def getSystemModeMap() {
    [
        "00":"off",
        "01":"auto",
        "03":"cool",
        "04":"heat",
        "05":"emergency heat",
        "06":"precooling",
        "07":"fan only",
        "08":"dry",
        "09":"sleep"
    ]
}

def getThermostatRunningStateMap() {
    /**  Bit Number
    //  0 Heat State
    //  1 Cool State
    //  2 Fan State
    //  3 Heat 2nd Stage State
    //  4 Cool 2nd Stage State
    //  5 Fan 2nd Stage State
    //  6 Fan 3rd Stage Stage
    **/
    [
        "0000":"idle",		//  (0x00) 0000 0000 idle
        "0001":"heating",	//  (0x01) 0000 0001 heat
        "0002":"cooling",	//  (0x02) 0000 0010 cool
        "0004":"fan only",	//  (0x04) 0000 0100 fan only
        "0005":"heating",	//  (0x05) 0000 0101 heat + fan
        "0006":"cooling",	//  (0x06) 0000 0110 cool + fan
        "0008":"heating",	//  (0x08) 0000 1000 heat_2nd stage
        "0009":"heating",	//  (0x09) 0000 1001 heat + heat_2nd
        "000C":"heating",	//  (0x0C) 0000 1100 heat_2nd + fan
        "000D":"heating",	//  (0x0D) 0000 1101 heat + heat_2nd + fan
        "0010":"cooling",	//  (0x10) 0001 0000 cool_2nd state 
        "0012":"cooling",	//  (0x12) 0001 0010 cool_2nd + fan
        "0014":"cooling",	//  (0x14) 0001 0100 cool + cool_2nd
        "0016":"cooling"	//  (0x16) 0001 0110 cool + cool_2nd + fan
    ]
}

def getFanMode() {
    // default fan mode is "auto"
    return (state.fanMode ? fanModeMap[state.fanMode] : ["auto"])
}

def getFanModeMap() {
    [
        "00":"off",
        "01":"low",
        "02":"medium",
        "03":"high",
        "04":"on",
        "05":"auto",
        "06":"smart"
    ]
}

def	initFanModes(){ 
    state.fanMode = "05"  //  default mode is "auto"
	state.fanModeSequence = "04"	// default fan mode sequence is ["on", "auto"]
    state.fanModeSequenceReady = false	//create a flag to enable the fan-mode-change function. if supported-modes is not received from device, don't allow user to change mode
    log.debug "initFanModes(): state.fanMode = $state.fanMode"
    log.debug "initFanModes(): state.fanModeSequence = $state.fanModeSequence"
    log.debug "initFanModes(): state.fanModeSequenceReady = $state.fanModeSequenceReady"
    log.debug "initFanModes(): fanMode = $fanMode"
    log.debug "initFanModes(): fanModeSequence = $fanModeSequence"
} 


def initSupportedModes() {
    state.systemModes = "3"  //    default supported modes : "off/heat/cold"
    state.supportedModesReady = false	//create a flag to enable the mode-change function. if supported-modes is not received from device, don't allow user to change mode
    log.debug "initSupportedModes(): state.supportedModesReady = $state.supportedModesReady"
    log.debug "initSupportedModes(): state.systemModes = $state.systemModes"
    log.debug "initSupportedModes(): supportedModes = $supportedModes"
}

def initOperatingState() {
    // display "--" when initialization
    def operatingState = device.currentValue("thermostatOperatingState")
    log.debug "initOperatingState(): thermostatOperatingState = $operatingState"
    sendEvent(name: "thermostatOperatingState", value: "--", eventType: "ENTITY_UPDATE")//, displayed: false)
}

def initSetPoint() {
    state.sendingSetpointsZB = false	//create a token for ignoring the attribut 0011 & 0012 updated while user is setting the Setpoint in APP
    log.debug "initSetPoint(): state.sendingSetpointsZB = $state.sendingSetpointsZB"

    def temperature = device.currentState("thermostatSetpoint")
    sendEvent(name: "thermostatSetpoint", value: "--", unit:"F", eventType: "ENTITY_UPDATE")//, displayed: false)
	if (temperature) {
	    log.debug "initSetPoint(): temperature of device is $temperature.value"
	}
	else {
	    log.debug "initSetPoint(): device.currentState(thermostatSetpoint) is null"
	}	
}


def updateSupportedModes(HVACsystemTypeConfig) {

    log.debug "updateSupportedModes(): HVACsystemTypeConfig = $HVACsystemTypeConfig"
	if((HVACsystemTypeConfig%16)==15){
	      state.systemModes = "0"  //    "off" mode only;neither heat nor cool is supported
    }
    else if((HVACsystemTypeConfig&0x03)==0x03){
	 		state.systemModes =  "1"  // heat only
    }else if ((HVACsystemTypeConfig&0x0c)==0x0c){
	 		state.systemModes =  "2"  // cool only
    }else{
	 		state.systemModes =  "3"  // heat + cool
    }
    log.debug "updateSupportedModes(): state.systemModes = $state.systemModes"
}



def updateMinSetpointLimit(setpoint, rawValue) {
    def min = parseTemperature(rawValue)
    if (min) {
        // Make sure min is an even number of step value (0.5°C/1°F) to nearest upper
        //min = (((long)min - min) < -0.5) ? Math.ceil(min): Math.floor(min) + 0.5*(Math.ceil(min - (long)min))
        log.debug "updateMinSetpointLimit -  min = $min"
        device.updateDataValue(setpoint, "${min}")
    } else {
        log.warn "received invalid min value for $setpoint ($rawValue)"
    }
}

def updateMaxSetpointLimit(setpoint, rawValue) {
    def max = parseTemperature(rawValue)
    if (max) {
        // Make sure max is an even number if step value (0.5°C/1°F) to nearest lower
        //max = ((max - (long)max) < 0.5) ? Math.floor(max) : Math.floor(max) + 0.5
        log.debug "updateMaxSetpointLimit -  max = $max"
        device.updateDataValue(setpoint, "${max}")
    } else {
        log.warn "received invalid max value for $setpoint ($rawValue)"
    }
}
/*
def updateBatteryStatus(rawValue) {
    if (rawValue && rawValue.matches("-?[0-9a-fA-F]+")) {
        def volts = zigbee.convertHexToInt(rawValue)
        // customAttribute in order to change UI icon/label
        def eventMap = [name: "batteryIcon", value: "err_battery", displayed: false]
        def linkText = getLinkText(device)
        if (volts != 255) {
            def minVolts = 39  // voltage when device UI starts to die, ie. when battery fails
            def maxVolts = 60  // 4 batteries at 1.5V (6.0V)
            def pct = (volts > minVolts) ? ((volts - minVolts) / (maxVolts - minVolts)) : 0
            eventMap.value = Math.min(100, (int)(pct * 100))
            // Update capability "Battery"
            sendEvent(name: "battery", value: eventMap.value, descriptionText: "${getLinkText(device)} battery was ${eventMap.value}%")
            
            //eventMap.value = eventMap.value > 15 ? eventMap.value : "low_battery"
        	if(eventMap.value == 0) 
            	eventMap.value = "eol_battery"
			else if (eventMap.value <= 2)              
            	eventMap.value = "low_battery_07"
			else if (eventMap.value <= 4)              
            	eventMap.value = "low_battery_30"
			else if (eventMap.value <= 7)              
            	eventMap.value = "low_battery_60"
        }
        sendEvent(eventMap)
    } else {
        log.warn "received invalid battery value ($rawValue)"
    }
}
*/

def updateThermostatSetpoint() {
    // Do calculation in device scale to avoid rounding errors when converting between °C->°F
    def scale = getTemperatureScale() ?: "F"
    log.debug "updateThermostatSetpoint() state.sendingSetpointsZB - $state.sendingSetpointsZB"
    log.debug "updateThermostatSetpoint() state.deviceHeatingSetpoint - $state.deviceHeatingSetpoint"
    log.debug "updateThermostatSetpoint() state.deviceCoolingSetpoint - $state.deviceCoolingSetpoint"
    log.debug "updateThermostatSetpoint() heatingSetpoint - scale:$scale"
    log.debug "updateThermostatSetpoint() coolingSetpoint - scale:$scale"
    def heatingSetpoint = state.deviceHeatingSetpoint ?:
            ((scale == "F") ? Math.round(degreeFtoCelsius(getStateTempInLocalScale("heatingSetpoint")) *100)/100 : getStateTempInLocalScale("heatingSetpoint"))
    def coolingSetpoint = state.deviceCoolingSetpoint ?:
             ((scale == "F") ? Math.round(degreeFtoCelsius(getStateTempInLocalScale("coolingSetpoint")) *100)/100 : getStateTempInLocalScale("coolingSetpoint"))
    def mode = device.currentValue("thermostatMode") ?: "off"
    state.deviceHeatingSetpoint = null
    state.deviceCoolingSetpoint = null
    def thermostatSetpoint = heatingSetpoint   // corresponds to (mode == "heat" || mode == "emergency heat")
    if (mode == "cool") {
        thermostatSetpoint = coolingSetpoint
    } 
    log.debug "updateThermostatSetpoint() - mode:$mode"
    log.debug "updateThermostatSetpoint() - heatingSetpoint:$heatingSetpoint"
    log.debug "updateThermostatSetpoint() - coolingSetpoint:$coolingSetpoint"
    log.debug "updateThermostatSetpoint() - thermostatSetpoint:$thermostatSetpoint"
       
    if (mode == "off") {
        // Set thermostatSetpoint to null
        thermostatSetpoint = null
	    log.debug "updateThermostatSetpoint() sendEvent - mode=off && thermostatSetpoint=$thermostatSetpoint"
    	//sendEvent(name: "thermostatSetpoint", value: "--", unit: scale)
        // Setpoint is not cleared in off-mode @@@@@@@@Sarah
    	sendEvent(name: "thermostatSetpoint", value: "--", unit: scale,  eventType: "ENTITY_UPDATE")
    }
    else {
	    log.debug "updateThermostatSetpoint() sendEvent - thermostatSetpoint:$thermostatSetpoint"
    	sendEvent(name: "thermostatSetpoint", value:Math.round(getTempInLocalScale(thermostatSetpoint, "C")), unit: scale, eventType: "ENTITY_UPDATE")
    }
    log.debug "updateThermostatSetpoint() sendEvent - END"
    
     // added by KK 20180501, to refresh the run mode icon after received the set point update, to solve the intermittant mode icon stuck issue
    //sendEvent(name: "thermostatMode", value: mode, data:[supportedThermostatModes: device.currentValue("supportedThermostatModes")],
	//		isStateChange: true, descriptionText: "$device.displayName is in ${mode} mode")
	sendEvent(name: "thermostatMode", value: mode)
	//log.debug "updateThermostatSetpoint() sendEvent THERMOSTAT MODE: $mode - END"
}



// parse zigbee temperaure value to °C
def parseTemperature(String value) {
    def temperature = null
    if (value!=null && value.matches("-?[0-9a-fA-F]+") && value != "8000") {
        temperature = Integer.parseInt(value, 16)
        if (temperature > 32767) {
            temperature -= 65536
        }
        temperature = temperature / 100.0 as Double
    } else {
        log.warn "received no or invalid temperature"
    }
    return temperature
}

// Get stored temperature (ZB attribute format) from currentState in current local scale 
def getStateTempInLocalScale(state) {
    
    def temperature = device.currentState(state)
		log.debug "getTempInLocalScale(state) - temperature : $temperature"
	if (temperature){
    	log.debug "getTempInLocalScale(state) - temperature.value : $temperature.value"
		log.debug "getTempInLocalScale(state) - temperature.unit : $temperature.unit"
	    if ((temperature.value!="--") && (temperature.value!=null) && (temperature.unit!=null)) {
	        // convert the String to BigDecimal for temperature.value
	        return getTempInLocalScale(temperature.value.toBigDecimal(), temperature.unit)
	    }
    }
    return null
}


// get/convert temperature (ZB attribute format) to current local scale 
def getTempInLocalScale(temperature, scale) {

	  def scaledTemp = null
	  def deviceScale = getTemperatureScale() ?: "F"
		log.debug "temperature of getTempInLocalScale : $temperature "
		log.debug "scale of getTempInLocalScale : $scale "
		log.debug "deviceScale of getTempInLocalScale : $deviceScale "

    if ((temperature!="--") && (temperature != null) && (scale!=null)) {

				//log.debug "scale = $scale; deviceScale = $deviceScale"
				//log.debug "temperature before F2C/C2F : $temperature "
		
		    scaledTemp = (deviceScale == scale) ? Math.round(temperature *100)/100:
		            (deviceScale == "F" ? Math.round(degreeCtoFahrenheit(temperature) *100)/100 : Math.round(degreeFtoCelsius(temperature) *100)/100)
				log.debug "temperature after F2C/C2F : $scaledTemp "
		    return scaledTemp
    }
    return null
}

// get the temperature value from ZB attribute and convert to the local scale (integer in °F/°C).
def getTempInDeviceScale(temp, scale) {
    def deviceScale = (state.scale == 1) ? "F" : "C"
    log.debug "getTempInDeviceScale: scale = $scale"
    log.debug "getTempInDeviceScale: deviceScale = $deviceScale"
    if ((temp!="--") && (temp!=null) && (scale!=null)) {
        //def deviceScale = (state.scale == 1) ? "F" : "C"
    	log.debug "getTempInDeviceScale: (input value) temp = $temp"
        return (deviceScale == scale) ? Math.round(temp*100)/100:
            (deviceScale == "F" ? Math.round(degreeCtoFahrenheit(temp) *100)/100 : Math.round(degreeFtoCelsius(temp) *100)/100 )
    }
    return null
}

// Round to nearest X.0 or X.5
def roundC (tempC) {
    return (Math.round(tempC.toDouble() * 2))/2
}

// interpret the temperature value from ZB attribute to an integer scale for Celsius or Fahrenheit, which is in x100 scale. e.g. 2550 => 25.5'C 
def Integer convertTempFmZBdegreeC(ZB_degreeC) {  
	if (ZB_degreeC != null) {
		if (getTemperatureScale() == "C") {
			return (ZBdegreeCtoCelsius(ZB_degreeC)).round(2)
		} else {
			return (ZBdegreeCtoFahrenheit(ZB_degreeC)).round(2)
		}
	}
}


// The scale of the degreeC_100 is (degreeC_value)x100. 
def Integer convertTempToZBdegreeC(temp) {
	if(temp!=null)
    {
        def scale = getTemperatureScale() ?: "F"
        if (scale == "F") {
			return (degreeCtoFahrenheit(temp).round(2) *100)
		}
        else{
            return (temp.round(2) *100)
        }
    }
    return 0  
}

def degreeCtoFahrenheit(degreeC) {
	def temperature = degreeC.toDouble()
  	//log.debug "degreeC before $temperature"
    if (degreeC!=null) {
    	temperature = Math.round(((degreeC.toDouble() *9/5) + 32) *100)/100
  		//log.debug "degreeC after : $temperature"
        return temperature
    }
    return null
}

def degreeFtoCelsius(degreeF) {
	def temperature = degreeF.toDouble()
  	//log.debug "degreeF before $temperature"
    if (degreeF!=null) {
    	temperature = Math.round(((degreeF.toDouble() -32)*5 /9) *100)/100
  		//log.debug "degreeF after : $temperature"
        return  temperature
    }
    return null
}



// The scale of the degreeC_100 is (degreeC_value)x100. 
// e.g. temperature is 25.50'C. degreeC_100 should be 2550.
def ZBdegreeCtoFahrenheit(degreeC_100) {
    if (degreeC_100!=null) {
        return ((degreeC_100.toDouble() *9/5) + 3200).toDecimal()
    }
    return null
}
// The scale of the degreeC_100 is (degreeC_value)x100. 
// e.g. temperature is 25.50'C. degreeC_100 should be 2550.
def ZBdegreeCtoCelsius(degreeC_100) {
    if (degreeC_100!=null) {
		return (degreeC_100)
    }
    return null
}
//========================= should never be called as ZB attributes are all in Celsius ============================
// The scale of the degreeC_100 is (degreeC_value)x100. 
// e.g. temperature is 25.50'C. degreeC_100 should be 2550.
def ZBdegreeFtoCelsius(degreeF_100) {
    if (degreeF_100!=null) {
        return ((degreeF_100-3200).toDouble() *5/9).toDecimal()
    }
    return null
}
// The scale of the degreeC_100 is (degreeC_value)x100. 
// e.g. temperature is 25.50'C. degreeC_100 should be 2550.
def ZBdegreeFtoFahrenheit(degreeF_100) {
    if (degreeF_100!=null) {
		return (degreeF_100)
    }
    return null
}
//=================================================================================================================


// =============== Setpoints ===============
def setpointUp() {
    alterSetpoint(true)
}
def setpointDown() {
    alterSetpoint(false)
}

// Adjusts nextHeatingSetpoint either 1° C/1° F) if raise true/false
def alterSetpoint(raise, targetValue = null, setpoint = null) {
	def FAHRENHEIT_DELTA = 1
	def CELSIUS_DELTA = 1	// was 0.5°C before 
	def locationScale = getTemperatureScale() ?: "F"
    def deviceScale = "C"  // Zigbee is always reporting in °C
    def currentMode = device.currentValue("thermostatMode") ?: "off"
    def delta = (locationScale == "F")? FAHRENHEIT_DELTA : CELSIUS_DELTA
    def heatingSetpoint = null
    def coolingSetpoint = null
	def dispplaySetpoint = getStateTempInLocalScale("thermostatSetpoint")	//ignore setpoint action if thermostatSetpoint is null (not ready)
    
	log.debug "alterSetpoint(...) - currentMode: $currentMode"
	log.debug "alterSetpoint(...) - targetValue:getTempInLocalScale - targetValue:$targetValue"
	if((currentMode != "off") && (dispplaySetpoint))
	{
		//targetValue = targetValue ?: (getStateTempInLocalScale("thermostatSetpoint") + (raise ? delta : - delta))  //
		targetValue = targetValue ?: (dispplaySetpoint + (raise ? delta : - delta))  //
    	log.debug "alterSetpoint(...) - targetValue:getTempInDeviceScale - targetValue:$targetValue"
    	targetValue = getTempInDeviceScale(targetValue, locationScale)
    //}

	switch (currentMode) {
        case "auto":
            def minSetpoint = device.getDataValue("minHeatSetpointCelsius")
            def maxSetpoint = device.getDataValue("maxCoolSetpointCelsius")
            minSetpoint = minSetpoint ? Double.parseDouble(minSetpoint) : 4.0    // default 4.0
            maxSetpoint = maxSetpoint ? Double.parseDouble(maxSetpoint) : 37.0   // default 37.0
            // Set both heating and cooling setpoint, 4 degrees appart (possibly user configurable)
            // thermostatSetpoint is the average of heating/cooling
            targetValue = enforceSetpointLimit(targetValue, "minHeatSetpointCelsius", "maxCoolSetpointCelsius")
            heatingSetpoint = targetValue - 2
            coolingSetpoint = targetValue + 2
            if (heatingSetpoint < minSetpoint) {
                coolingSetpoint = coolingSetpoint - (minSetpoint - heatingSetpoint)
                heatingSetpoint = minSetpoint
                targetValue = (heatingSetpoint + coolingSetpoint) / 2
            }
            if (coolingSetpoint > maxSetpoint) {
                heatingSetpoint = (coolingSetpoint < maxSetpoint + 2) ? heatingSetpoint + (coolingSetpoint - maxSetpoint) : (maxSetpoint - CELSIUS_DELTA)
                coolingSetpoint = maxSetpoint
                targetValue = (heatingSetpoint + coolingSetpoint) / 2
            }
            break
        case "heat":  // No Break
        case "emergency heat":
			//update the target value after checking limit
            targetValue = enforceSetpointLimit(targetValue, "minHeatSetpointCelsius", "maxHeatSetpointCelsius")
            heatingSetpoint = targetValue
            break
        case "cool":
            // set coolingSetpoint to thermostatSetpoint
			//update the target value after checking limit
            targetValue = enforceSetpointLimit(targetValue, "minCoolSetpointCelsius", "maxCoolSetpointCelsius") //update the target value after checking limit
            coolingSetpoint = targetValue
            break
         case "off":  // No Break
            // Do nothing, don't allow change of setpoints in off mode
        default:
            targetValue = null
            break
    }
    if (targetValue) {
		log.debug "alterSetpoint(...) - sendEvent - targetValue:$targetValue"
        sendEvent(name: "thermostatSetpoint", value: Math.round(getTempInLocalScale(targetValue, deviceScale)),
                unit: locationScale, eventType: "ENTITY_UPDATE")//, displayed: false)
        def data = [targetHeatingSetpoint:heatingSetpoint, targetCoolingSetpoint:coolingSetpoint]

		state.sendingSetpointsZB = true		// set the token for disabling Setpoint attributes received
    	log.debug "alterSetpoint(...) before updateSetpoints(data):state.sendingSetpointsZB - $state.sendingSetpointsZB"
        // Use runIn to reduce chances UI is toggling the value
        runIn(5, "updateSetpoints", [data: data, overwrite: true])
		//the token (state.sendingSetpointsZB) is clear (=false) after the ZB commands are sent
    }
    }
    else{
		log.debug "alterSetpoint(...) -  system mode is OFF"
        sendEvent(name: "thermostatSetpoint", value: "--",
                unit: locationScale, eventType: "ENTITY_UPDATE")//, displayed: false)
    }
    
}

def enforceSetpointLimit(target, min, max) {
    def minSetpoint = device.getDataValue(min)
    def maxSetpoint = device.getDataValue(max)
    minSetpoint = minSetpoint ? Double.parseDouble(minSetpoint) : 4.0    // default 4.0
    maxSetpoint = maxSetpoint ? Double.parseDouble(maxSetpoint) : 37.0   // default 37.0
  	log.debug "enforceSetpointLimit(...) minSetpoint = $minSetpoint"
  	log.debug "enforceSetpointLimit(...) maxSetpoint = $maxSetpoint"
  	log.debug "enforceSetpointLimit(...) target (in C) = $target"

    // Enforce setpoint limits
    if (target < minSetpoint) {
        target = minSetpoint
    } else if (target > maxSetpoint) {
        target = maxSetpoint
    }

  	log.debug "enforceSetpointLimit(...) limit-checked target (in C) = $target"

    return target
}

def setHeatingSetpoint(degrees) {
    def currentMode = device.currentValue("thermostatMode") ?: "off"
    if (degrees && (currentMode != "cool") && (currentMode != "off")) {
        state.heatingSetpoint = degrees.toDouble()
        // Use runIn to enable both setpoints to be changed if a routine/SA changes heating/cooling setpoint at the same time
        runIn(2, "updateSetpoints", [overwrite: true])
    }
}

def setCoolingSetpoint(degrees) {
    def currentMode = device.currentValue("thermostatMode") ?: "off"
    if (degrees && (currentMode == "cool")) {
        state.coolingSetpoint = degrees.toDouble()
        // Use runIn to enable both setpoints to be changed if a routine/SA changes heating/cooling setpoint at the same time
        runIn(2, "updateSetpoints", [overwrite: true])
    }
}

def updateSetpoints() {
    def deviceScale = "C"
    def data = [targetHeatingSetpoint: null, targetCoolingSetpoint: null]
    def targetValue = state.heatingSetpoint
    def setpoint = "heatingSetpoint"
    if (state.heatingSetpoint && state.coolingSetpoint) {
        setpoint = null
        targetValue = (state.heatingSetpoint + state.coolingSetpoint) / 2
    } else if (state.coolingSetpoint) {
        setpoint == "coolingSetpoint"
        targetValue = state.coolingSetpoint
    }
    log.debug "updateSetpoints() - state.heatingSetpoint = $state.heatingSetpoint"
    log.debug "updateSetpoints() - state.coolingSetpoint = $state.coolingSetpoint"
	state.heatingSetpoint = null
    state.coolingSetpoint = null
    log.debug "updateSetpoints() - setpoint = $setpoint"
    log.debug "updateSetpoints() - targetValue = $targetValue"
    alterSetpoint(null, targetValue, setpoint)		// no need to make null-safe of "targetValue" nor "setpoint" which are set null in alterSetpoint
    log.debug "updateSetpoints() - alterSetpointt(null, targetValue, setpoint) - DONE"
}

def updateSetpoints(data) {
    def cmds = []
    if (data.targetHeatingSetpoint) {
        cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_HEATING_SETPOINT, typeINT16,
                hexString(Math.round(data.targetHeatingSetpoint*100.0), 4))
    }
    if (data.targetCoolingSetpoint) {
        cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_COOLING_SETPOINT, typeINT16,
                hexString(Math.round(data.targetCoolingSetpoint*100.0), 4))
    }
    sendZigbeeCmds(cmds, 1000)
    state.sendingSetpointsZB = false

	log.debug "updateSetpoints(data) - data.targetHeatingSetpoint = $data.targetHeatingSetpoint"
    log.debug "updateSetpoints(data) - data.targetCoolingSetpoint = $data.targetCoolingSetpoint"
    log.debug "updateSetpoints(data) - ZB command sent"
 	log.debug "updateSetpoints(data) - clear : state.sendingSetpointsZB - $state.sendingSetpointsZB"
    log.debug "updateSetpoints(data) - END"
}

// =============== Thermostat Mode ===============
def switchMode() {
    def currentMode = device.currentValue("thermostatMode") ?: "off"
    def supportedModes = supportedModes
    if (state.supportedModesReady) {
	    if (supportedModes) {
	        def next = { supportedModes[supportedModes.indexOf(it) + 1] ?: supportedModes[0] }
	        switchToMode(next(currentMode))
		    // refresh the heat/cool setpoints
	        log.debug("Mode switched. Refresh Setpoint")
	    	//runIn(2, "updateThermostatSetpoint", [overwrite: true])
	        //readSetpoints()
	        
            // remarked by KK, testing only, try to reduce the data exchange and to fix the mode icon stuck issue
            //runIn(2, "readSetpoints", [overwrite: true])
		} else {
	        log.err "supportedModes not defined"
	    }
	} else {
		ping()	//ping for Supported modes, fan-mode sequence and operating-state
	}
    
    // added by KK, to run the refrash one for solving the icon stuck problem
    runIn(50, "readSetpoints", [overwrite: true])
}

def switchToMode(nextMode) {
    def supportedModes = supportedModes
    if (supportedModes) {
        if (supportedModes.contains(nextMode)) {
            //def cmds = []
			def cmds = zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_CHECKIN_RESPONSE, "01F000")			//start fast-poll mode for 60s, speeding up the switch-mode

				    log.debug "switchToMode(.) - setpoint - nextMode:$nextMode"
            def setpoint = getStateTempInLocalScale("thermostatSetpoint")
			def heatingSetpoint = null
            def coolingSetpoint = null
			
            if (setpoint){
				switch (nextMode) {
	                case "heat": // No break
	                case "emergency heat":
	                    heatingSetpoint = setpoint
	                    break
	                case "cool":
	                    coolingSetpoint = setpoint
	                    break
	                case "off":  // No break
                    	break
	                case "auto": // No break
	                default:
	                    def currentMode = device.currentValue("thermostatMode") ?: "off"
	                    if (currentMode != "off" && currentMode != "auto") {
	                        heatingSetpoint = setpoint - 2  // In auto/off keep heating/cooling setpoint 4° appart (customizable?)
	                        coolingSetpoint = setpoint + 2
	                    }
	                    break
	            }
            }

            if (heatingSetpoint) {
                cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_HEATING_SETPOINT, typeINT16,
                        hexString(Math.round(heatingSetpoint*100.0), 4))
            }
            if (coolingSetpoint) {
                cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_COOLING_SETPOINT, typeINT16,
                        hexString(Math.round(coolingSetpoint*100.0), 4))
            }
            def mode = Integer.parseInt(systemModeMap.find { it.value == nextMode }?.key, 16) 
            cmds += zigbee.writeAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE, typeENUM8, mode)
            sendZigbeeCmds(cmds)
            state.switchMode = true
            log.debug("ThermostatMode $nextMode : ${device.displayName}")
        } else {
            log.debug("ThermostatMode $nextMode is not supported by ${device.displayName}")
        }
    } else {
        log.err "supportedModes not defined"
    }
}

def setThermostatMode(String value) {
    switchToMode(value?.toLowerCase())
}

def off() {
    switchToMode("off")
}

def cool() {
    switchToMode("cool")
}

def heat() {
    switchToMode("heat")
}

def auto() {
    switchToMode("auto")
}

// =============== Fan Mode ===============
def switchFanMode() {
    def currentFanMode = device.currentValue("thermostatFanMode") ?: "auto"
    def fanModeSequence = fanModeSequence
    log.debug "switchFanMode() - currentFanMode:$currentFanMode"
    log.debug "switchFanMode() - fanModeSequence = $fanModeSequence"
    if (fanModeSequence) {
        def next = { fanModeSequence[fanModeSequence.indexOf(it) + 1] ?: fanModeSequence[0] }
        switchToFanMode(next(currentFanMode))
    } else {
        log.warn "supportedFanModes not defined"
    }
}

def switchToFanMode(nextMode) {
    log.debug "switchToFanMode(nextMode) - nextMode:$nextMode"
    def fanModeSequence = fanModeSequence
    if (fanModeSequence) {
        if (fanModeSequence.contains(nextMode)) {
            def cmds = []
            def mode = Integer.parseInt(fanModeMap.find { it.value == nextMode }?.key, 16)
            cmds += zigbee.writeAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE, typeENUM8, mode)
            sendZigbeeCmds(cmds)
            state.switchFanMode = true
            log.debug("ThermostatFanMode $nextMode : ${device.displayName}")
        } else {
            log.debug("ThermostatFanMode $nextMode is not supported by ${device.displayName}")
        }
    } else {
        log.err "fanModes not defined"
    }
}
/*
def getSupportedFanModes() {
    def cmds = zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE_SEQUENCE)
    sendZigbeeCmds(cmds)
}

def setThermostatFanMode(String value) {
    switchToFanMode(value?.toLowerCase())
}

def fanOn() {
    switchToFanMode("on")
}

def fanAuto() {
    switchToFanMode("auto")
}
*/

def readSetpoints() {
    def cmds = zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE)  // Current operating mode
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_HEATING_SETPOINT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_COOLING_SETPOINT)
    sendZigbeeCmds(cmds)
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    def cmds = []
	if (!state.supportedModesReady) {	// read the supported-modes if not yet received 
    	cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_HVAC_SYSTEM_TYPE_CONFIGURATION)  // The HVAC system type configuration
    	cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE)  // Current operating mode
    }
	if (!state.fanModeSequenceReady) {	// read the fan-mode-sequence if not yet received 
        cmds += zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE_SEQUENCE)
	    cmds += zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE)    // The current fan mode
    }

    // No need to send a bunch of cmd, one is enough
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_THERMOSTAT_RUNNING_STATE)
    sendZigbeeCmds(cmds)
}

def refresh() {

	// refresh screen for SmartThings bug on display
    //def currentMode = device.currentValue("thermostatMode") ?: "off"
    //def currentFanMode = device.currentValue("thermostatFanMode") ?: "auto"
    //def currentState = device.currentValue("thermostatOperatingState") ?: "--"
    //def currentSetpoint = getStateTempInLocalScale("thermostatSetpoint")
    //sendEvent(name: "thermostatMode", value: {currentMode})
    //sendEvent(name: "thermostatFanMode", value: {currentFanMode})
    //sendEvent(name: "thermostatOperatingState", value: {currentState})
    //sendEvent(name: "thermostatSetpoint", value: {currentSetpoint}, unit: "°F")

    // Only allow refresh every 1 minute to prevent flooding the Zigbee network
    def timeNow = now()
    //if (!state.refreshTriggeredAt || (1 * 60 * 1000 < (timeNow - state.refreshTriggeredAt))) {
    
    //update by KK, to reduce the refresh time for testing
    if (!state.refreshTriggeredAt || (1 * 1 * 1000 < (timeNow - state.refreshTriggeredAt))) {
        state.refreshTriggeredAt = timeNow
        // use runIn with overwrite to prevent multiple DTH instances run before state.refreshTriggeredAt has been saved
        
        //runIn(2, "pollDevice", [overwrite: true])
        // changed by KK, to reduce dubplicate function call
        runIn(3, "pollDevice", [overwrite: true])
    }
}

def pollDevice() {
    log.debug "pollDevice() - update attributes"
    // First update supported modes, min/max setpoint ranges and deadband, this is normally only needed at install/updated
    def cmds = pollAdditionalData()
    // When supported modes are known we can update current modes
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_HVAC_SYSTEM_TYPE_CONFIGURATION)  // The HVAC system type configuration
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_SYSTEM_MODE)  // Current operating mode
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_THERMOSTAT_RUNNING_MODE)  // The running thermostat mode
    // ATTRIBUTE_THERMOSTAT_RUNNING_STATE will be updated by response of ATTRIBUTE_SYSTEM_MODE and ATTRIBUTE_FAN_MODE
    // cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_THERMOSTAT_RUNNING_STATE) // The current relay state of the heat, cool, and fan relays
    cmds += zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE)    // The current fan mode
    // When system mode is known we can update current temperature and setpoints
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_HEATING_SETPOINT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_OCCUPIED_COOLING_SETPOINT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_COOL_SETPOINT_LIMIT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MAX_COOL_SETPOINT_LIMIT)
    cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_LOCAL_TEMPERATURE)	// make the ambient temperature last to send

    // Also update the current battery status
    //cmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, ATTRIBUTE_BATTERY_VOLTAGE)

	//cmds += zigbee.writeAttribute(0x0003, 0x0000, 0x21, 0x0000) //identify time = 0
    //cmds += zigbee.writeAttribute(IDENTIFY_CLUSTER, ATTRIBUTE_IDENTIFY_TIME, typeUINT16, 0x0000)	//identify time = 0

	//cmds += zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_SET_LONG_POLL_INTERVAL, "38000000")	//set polling time to 14s; little endian of 00 00 00 38 (0x38=56qs = 14 sec)
	//cmds += zigbee.command(POLL_CONTROL_CLUSTER, COMMAND_FAST_POLL_STOP)						//stop fast-poll mode

    sendZigbeeCmds(cmds)
}

def pollAdditionalData() {
    def cmds = []
    def timeNow = new Date().time
    if (!state.pollAdditionalData || (24 * 60 * 60 * 1000 < (timeNow - state.pollAdditionalData))) {
        state.pollAdditionalData = timeNow
        // Skip ATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION as it always reports the same regardless of thermostat configuration
        // cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_CONTROL_SEQUENCE_OF_OPERATION)
        cmds += zigbee.readAttribute(FAN_CONTROL_CLUSTER, ATTRIBUTE_FAN_MODE_SEQUENCE)
        //cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_HEAT_SETPOINT_LIMIT)
        //cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MAX_HEAT_SETPOINT_LIMIT)
        //cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_COOL_SETPOINT_LIMIT)
        //cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MAX_COOL_SETPOINT_LIMIT)
        // Skip ATTRIBUTE_MIN_SETPOINT_DEAD_BAND as it isn't really used in auto mode
        // cmds += zigbee.readAttribute(THERMOSTAT_CLUSTER, ATTRIBUTE_MIN_SETPOINT_DEAD_BAND)
    }

    return cmds
}

def sendZigbeeCmds(cmds, delay = 2000) {
    // remove zigbee library added "delay 2000" after each command
    // the new sendHubCommand won't honor these, instead it'll take the delay as argument
    cmds.removeAll { it.startsWith("delay") }
    // convert each command into a HubAction
    cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
    sendHubCommand(cmds, delay)
}
/*
def poll() {
    refresh()
}
*/