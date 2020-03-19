/**
 *  
 *  ZXT-600 device handler for Samsung SmartThings Hub 
 *
 *  Auther: Terence Kwan (Based on Ronald Gouldner version) 
 *  Date: 2018-05-03
 *  Copyright (C) 2018 Remotec Technology Limited
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

// Revision History for V05
//
// 

// Revision History for V04
// 1) Hardcode Setpoint support "C" only
// 2) bug fixed on Thermostat Mode "heat" mode issue

// ZXT-600 limited features which ZXT-120 also supported.

// Preferences pane
//
// options that the user can directly edit in the SmartThings app
preferences {
    input description: "Press Configure after making any changes", displayDuringSetup: true,
            type: "paragraph", element: "paragraph"
    input("remoteCode", "number", title: "Remote Code", description: "The number of the remote to emulate")
    input("tempOffset", "enum", title: "Temp correction offset?", options: ["-5","-4","-3","-2","-1","0","1","2","3","4","5"])
    input("shortName", "string", title: "Short Name for Home Page Temp Icon", description: "Short Name:")
}

metadata {
    definition (name: "ZXT-600 ZWave-to-IR Extender", namespace: "remotec", author: "Remotec") {
        // Device capabilities of the ZXT-600
        capability "Actuator"
        capability "Temperature Measurement"
        capability "Thermostat"
        capability "Configuration"        
        capability "Polling"
        capability "Sensor"
        capability "Battery"
        capability "Switch"
        capability "Refresh"

        // Commands that this device-type exposes for controlling the ZXT-600 directly
        command "switchModeOff"
        command "switchModeHeat"
        command "switchModeCool"
        command "switchModeDry"
        command "switchModeAuto"
        command "switchModeFan"
        command "switchFanLow"
        command "switchFanMed"
        command "switchFanHigh"
        command "switchFanAuto"
        command "switchFanMode"
        command "switchFanOscillate"
        command "setRemoteCode"
        command "swingModeOn"
        command "swingModeOff"


        //commands for thermostat interface
        command "cool"
        command "heat"
        command "dry"
        command "fan"
        command "auto"
        command "off"
        command "setLearningPosition"
        command "issueLearningCommand"
        // how do these work....do they take arguments ?
        //command "setCoolingSetpoint"
        //command "setHeatingSetpoint"
        //command "setThermostatMode"

        //command "adjustTemperature", ["NUMBER"]

        attribute "swingMode", "STRING"
        attribute "lastPoll", "STRING"
        attribute "currentConfigCode", "STRING"
        attribute "currentTempOffset", "STRING"
        attribute "temperatureName", "STRING"
        attribute "reportedCoolingSetpoint", "STRING"
        attribute "reportedHeatingSetpoint", "STRING"
        attribute "learningPosition", "NUMBER"
        attribute "learningPositionTemp", "STRING"


        command "setMode"
        command "setTemperature"
        
                // Z-Wave description of the ZXT-600 device
        fingerprint mfr:"5254", prod:"0100", model:"8490"
    }

    // simulator metadata - for testing in the simulator
    simulator {
        // Not sure if these are correct
        status "off"			: "command: 4003, payload: 00"
        status "heat"			: "command: 4003, payload: 01"
        status "cool"			: "command: 4003, payload: 02"
        status "auto"			: "command: 4003, payload: 03"
        status "emergencyHeat"	: "command: 4003, payload: 04"

        status "fanAuto"		: "command: 4403, payload: 00"
        status "fanOn"			: "command: 4403, payload: 01"
        status "fanCirculate"	: "command: 4403, payload: 06"

        status "heat 15"        : "command: 4303, payload: 01 01 3C"
        status "heat 20"        : "command: 4303, payload: 01 01 44"
        status "heat 22"        : "command: 4303, payload: 01 01 48"

        status "cool 22"        : "command: 4303, payload: 02 01 48"
        status "cool 24"        : "command: 4303, payload: 02 01 4C"
        status "cool 27"        : "command: 4303, payload: 02 01 50"

        status "temp 14"        : "command: 3105, payload: 01 22 02 44"
        status "temp 16"        : "command: 3105, payload: 01 22 02 6C"
        status "temp 21"        : "command: 3105, payload: 01 22 02 BC"
        status "temp 23"        : "command: 3105, payload: 01 22 02 E4"
        status "temp 25"        : "command: 3105, payload: 01 22 03 0C"
        status "temp 28"        : "command: 3105, payload: 01 22 03 34"

        // reply messages
        reply "2502": "command: 2503, payload: FF"
    }

    // SmartThings app user interface
    // Note: scale: 2 if you want to see 6 tiles/line
    tiles (scale: 2) {
        // The currently detected temperature.  Show this as a large tile, changing colors as an indiciation
        // of the temperature
        valueTile("temperature", "device.temperature") {
            state("temperature", label:'${currentValue}°', unit: "C", 
                    backgroundColors:[
                            [value: 0, color: "#153591"],
                            [value: 6, color: "#1e9cbb"],
                            [value: 15, color: "#90d2a7"],
                            [value: 23, color: "#44b621"],
                            [value: 28, color: "#f1d801"],
                            [value: 35, color: "#d04e00"],
                            [value: 36, color: "#bc2323"]
                    ]
            )
        }
        // Battery Status tile
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
            state "battery", label:'${currentValue}% battery', unit:""
        }
        // Power Off Mode tile
        standardTile("off", "device.thermostatMode", height: 2, width: 4, inactiveLabel: false) {
            state "off", action:"switchModeOff", backgroundColor:"#92C081", icon: "st.thermostat.heating-cooling-off"
        }
        // Cool Mode tile
        standardTile("cool", "device.thermostatMode", height: 2, width: 2, inactiveLabel: false) {
            state "cool", action:"switchModeCool", backgroundColor:"#4A7BDE", icon: "st.thermostat.cool"
        }
        // Dry Mode tile
        standardTile("dry", "device.thermostatMode", height: 2, width: 2, inactiveLabel: false) {
            state "dry", action:"switchModeDry", backgroundColor:"#DBD099", label: "Dry", icon: "st.Weather.weather12"
        }
        // Heat Mode tile
        standardTile("heat", "device.thermostatMode", height: 2, width: 2, inactiveLabel: false) {
            state "heat", action:"switchModeHeat", backgroundColor:"#C15B47", icon: "st.thermostat.heat"
        }

        // Auto Mode tile
        standardTile("auto", "device.thermostatMode", height: 2, width: 2, inactiveLabel: false) {
            state "auto", action:"switchModeAuto", backgroundColor:"#DBD099", label: "Auto", icon: "st.Weather.weather12"
        }
        
        // Fan Mode tile
        standardTile("fan", "device.thermostatMode", height: 2, width: 2, inactiveLabel: false) {
            state "fan", action:"switchModeFan", backgroundColor:"#DBD099", label: "Fan", icon: "st.Weather.weather12"
        }

        // Low Fan Mode
        standardTile("fanModeLow", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
            state "fanLow", action:"switchFanLow", icon:"st.Appliances.appliances11", label: 'LOW'
        }

        // Medium Fan Mode
        standardTile("fanModeMed", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
            state "fanMed", action:"switchFanMed", icon:"st.Appliances.appliances11", label: 'MED'
        }

        // High Fan Mode
        standardTile("fanModeHigh", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
            state "fanHigh", action:"switchFanHigh", icon:"st.Appliances.appliances11", label: 'HIGH'
        }

        // Auto Fan Mode
        standardTile("fanModeAuto", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat") {
            state "fanAuto", action:"switchFanAuto", icon:"st.Appliances.appliances11", label: 'AUTO'
        }

        // Swing mode On.
        standardTile("swingModeOn", "device.swingMode", inactiveLabel: false, decoration: "flat") {
            state "on", action:"swingModeOn", icon:"st.secondary.refresh-icon", label: 'Swing On'
        }

        // Swing mode Off.
        standardTile("swingModeOff", "device.swingMode", inactiveLabel: false, decoration: "flat") {
            state "off", action:"swingModeOff", icon:"st.secondary.refresh-icon", label: 'Swing Off'
        }

		// Report of COOL and HEAT
        valueTile("reportedCoolingSetpoint", "device.reportedCoolingSetpoint", height: 1, width: 1, inactiveLabel: true, decoration: "flat") {
            state "reportedCoolingSetpoint", label:'${currentValue}° cool', unit:"C", backgroundColor:"#ffffff"
        }
        valueTile("reportedHeatingSetpoint", "device.reportedHeatingSetpoint", height: 1, width: 1, inactiveLabel: true, decoration: "flat") {
            state "reportedHeatingSetpoint", label:'${currentValue}° heat', unit:"C", backgroundColor:"#ffffff"
        }
        
        // Icon to trigger seekbar
        valueTile("heatingSetpoint", "device.heatingSetpoint", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "heatingSetpoint", label:'${currentValue}° heat', unit:"C", backgroundColor:"#ffffff"
        }
        valueTile("coolingSetpoint", "device.coolingSetpoint", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "coolingSetpoint", label:'${currentValue}° cool', unit:"C", backgroundColor:"#ffffff"
        }
        
        // seekbar for temperature 
        // remove F completely
        /*
        controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 1, inactiveLabel: false, range:"(67..84)") {
            state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor: "#d04e00"
        }    
        controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 1, inactiveLabel: false, range:"(67..84)") {
            state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb"
        }*/

        controlTile("heatSliderControlC", "device.heatingSetpoint", "slider", height: 1, width: 1, inactiveLabel: false, range:"(19..28)") {
            state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor: "#d04e00"
        }
        controlTile("coolSliderControlC", "device.coolingSetpoint", "slider", height: 1, width: 1, inactiveLabel: false, range:"(19..28)") {
            state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb"
        }
 
        // Mode switch.  Indicate and allow the user to change between heating/cooling modes
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", canChangeIcon: true, canChangeBackground: true) {
            state "off", icon:"st.thermostat.heating-cooling-off", label: ' '
            state "heat", icon:"st.thermostat.heat", label: ' '
            state "emergencyHeat", icon:"st.thermostat.emergency-heat", label: ' '
            state "cool", icon:"st.thermostat.cool", label: ' '
            state "auto", icon:"st.thermostat.auto", label: ' '
            state "dry", icon:"st.Weather.weather12", label: 'Dry'
            state "autoChangeover", icon:"st.thermostat.auto", label: ' '
        }

        // Fan mode switch.  Indicate and allow the user to change between fan speed settings
        standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat", canChangeIcon: true, canChangeBackground: true) {
            state "fanAuto", icon:"st.Appliances.appliances11", label: 'AUTO'
            state "fanLow", icon:"st.Appliances.appliances11", label: 'LOW'
            state "fanMedium", icon:"st.Appliances.appliances11", label: 'MED'
            state "fanHigh", icon:"st.Appliances.appliances11", label: 'HIGH'
        }

        // Swing mode switch.  Indicate and allow the user to change between fan oscillation settings
        standardTile("swingMode", "device.swingMode", inactiveLabel: false, decoration: "flat", canChangeIcon: true, canChangeBackground: true) {
            state "on", icon:"st.secondary.refresh-icon", label: 'Swing On'
            state "off", icon:"st.secondary.refresh-icon", label: 'Swing Off'
        }

        // Extra Temperature Tile with Name for Home Screen
        valueTile("temperatureName", "device.temperatureName", inactiveLabel: false, decoration: "flat") {
            state "temperatureName", label:'${currentValue}', unit:""
        }
        // Refresh command button.  Allow the user to request the device be polled and the UI be updated
        // with the current settings/sensor data
        standardTile("refresh", "device.thermostatMode", inactiveLabel: false) {
            state "default", label: poll, action:"polling.poll", icon:"st.Entertainment.entertainment15", backgroundColor:"#00bfff"
        }
        // Last Poll Tile
        valueTile("lastPoll", "device.lastPoll", inactiveLabel: false, decoration: "flat") {
            state "lastPoll", label:'Last Pool ${currentValue}', unit:""
        }
        // Configure button.  Syncronize the device capabilities that the UI provides
        standardTile("configure", "device.configure", inactiveLabel: false) {
            state "configure", label:'Config', action:"configuration.configure", 
                  icon:"st.Entertainment.entertainment15", backgroundColor:"#00bfff"
        }
        // Current Config Code
        valueTile("currentConfigCode", "device.currentConfigCode", inactiveLabel: false, decoration: "flat") {
            state "currentConfigCode", label:'Config# ${currentValue}', unit:""
        }
        // Current Temp Offset
        valueTile("currentTempOffset", "device.currentTempOffset", inactiveLabel: false, decoration: "flat") {
            state "currentTempOffset", label:'Offset ${currentValue}', unit:""
        }

        // Unused tiles from original code
        // Auto Mode tile
        standardTile("auto", "device.thermostatMode", inactiveLabel: false) {
            state "auto", action:"switchModeAuto", backgroundColor:"#b266b2", icon: "st.thermostat.auto"
        }
        // AutoChangeover Mode tile
        standardTile("autoChangeover", "device.thermostatMode", inactiveLabel: false) {
            state "autoChangeover", action:"switchModeAuto", backgroundColor:"#b266b2", icon: "st.thermostat.auto"
        }
        // EmergencyHeat Mode tile
        standardTile("emergencyHeat", "device.thermostatMode", inactiveLabel: false) {
            state "emergencyHeat", action:"switchModeAuto", backgroundColor:"#ff0000", icon: "st.thermostat.emergency-heat"
        }
        // Learning Mode Tiles
        valueTile("learningPosition", "device.learningPosition", inactiveLabel: false, decoration: "flat") {
            state "learningPosition", label:'${currentValue}', unit:""
        }      
        
        controlTile("learningPositionControl", "device.learningPosition", "slider", height: 1, width: 1, inactiveLabel: false, range:"(0..32)") {
            state "learningPosition", action:"setLearningPosition", backgroundColor: "#fffc33"
        }
        standardTile("issueLearningCommand", "issueLearningCommand", height: 2, width: 2, inactiveLabel: false) {
            state "issueLearningCommand", label:'learn', action:"issueLearningCommand", icon:"st.Kids.kids1", backgroundColor: "#fffc33"
        }
        valueTile("learningPositionTemp", "device.learningPositionTemp", inactiveLabel: false, decoration: "flat") {
            state "learningPositionTemp", label:'${currentValue}', unit:""
        }
        
        valueTile("version", "device.dhVersion", height: 1, width: 1, inactiveLabel: false, decoration: "flat") {
            state "version", label: 'Device Handler V5-L'
        }

        

        // Layout the controls on the SmartThings device UI.  The page is a 3x3 layout, tiles are layed out
        // starting in the upper left working right then down.
        //main "temperature"
        main (["temperature","temperatureName"])
        details(["temperature", "battery", "temperatureName", 
                 "thermostatMode", "fanMode", "swingMode",
                 "off", "dry", 
                 //"coolingSetpoint" tile no longer needed because new slider displays current value
                 // change "coolSliderControl" to "coolSliderControlC" for Celsius
                 "cool", "coolSliderControlC", "reportedCoolingSetpointC", 
                 //"heatingSetpoint", 
                 // change "heatSliderControl" to "heatSliderControlC" for Celsius
                 "heat", "heatSliderControlC", "reportedHeatingSetpointC",
                 "fanModeLow","fanModeMed","fanModeHigh", 
                 "fanModeAuto", "swingModeOn", "swingModeOff",
                 // SmartThings changed their slider to include the value so learningPosition
                 // Tile is now redundant.  Keeping tile in case they change this again.
                 //"learningPosition",
                 
                 "refresh", "lastPoll", "configure","currentConfigCode", 
                 "currentTempOffset", "version",
                 "issueLearningCommand", "learningPositionControl"
         ])
    }
}

def installed() {
    log.debug "ZXT-600 installed()"
    configure()
}

//***** Enumerations */
// modes - Possible heating/cooling modes for the device
def modes() {
    ["off", "auto", "heat", "emergencyHeat", "cool", "dry", "autoChangeover"]
}
// setpointModeMap - Link the possible modes the device can be in to the possible temperature setpoints.
def getSetpointModeMap() { [
        "heat": "heatingSetpoint"
        ,"cool": "coolingSetpoint"
        ,"dry": "dryingSetpoint"
        //,"autoChangeover": "autoChangeoverSetpoint"
]}
// setpointMap - Link the setpoint descriptions with ZWave id numbers
def getSetpointMap() { [
        "heatingSetpoint": physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1,
        "coolingSetpoint": physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1,
        "dryingSetpoint": physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_DRY_AIR,
        //"reportedAutoChangeoverSetpoint": physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_AUTO_CHANGEOVER
]}
// setpointReportingMap - Link the setpointReportingMap tiles with ZWave id numbers
def getSetpointReportingMap() { [
        "reportedHeatingSetpoint": physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1,
        "reportedCoolingSetpoint": physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1,
]}
// modeMap - Link the heating/cooling modes with their ZWave id numbers
def getModeMap() { [
        "off": physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_OFF,
        "heat": physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_HEAT,
        "cool": physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_COOL,
        "auto": physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_AUTO,
        "emergencyHeat": physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_AUXILIARY_HEAT,
        "dry": physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_DRY_AIR,
        "autoChangeover": physicalgraph.zwave.commands.thermostatmodev1.ThermostatModeSet.MODE_AUTO_CHANGEOVER
]}
def fanModes() {
    ["fanAuto", "fanLow", "fanMedium", "fanHigh"]
}
// fanModeMap - Link the possible fan speeds with their ZWave id numbers
def getFanModeMap() { [
        "fanAuto": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_AUTO_LOW,
        "fanLow": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_LOW,
        "fanMedium": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_MEDIUM,
        "fanHigh": physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport.FAN_MODE_HIGH
                
]}
// Command parameters
def getCommandParameters() { [
        "remoteCode": 27,
        "tempOffsetParam": 37,
        "oscillateSetting": 33,
        "learningMode": 25
]}


//***** Commands */
// parse - Handle events coming from the user and the device
def parse(String description)
{
    // If the device sent an update, interpret it
    log.info "Parsing Description=$description"
    // 0X20=Basic - V1 supported
    // 0x27=Switch All - V1 supported
    // 0X31=Sensor Multilevel - V1 supported
    // 0X40=Thermostat Mode - V2 supported
    // -- 0x42=Thermostat Operating State (NOT SUPPORTED, was in original device handler)
    // 0x43=Thermostat Setpoint - V2 supported
    // 0x44=Thermostat Fan Mode - V2 supported
    // 0x70=Configuration - V1 supported
    // 0x72=Manufacturer Specific - V1 supported
    // 0x80=Battery - V1 supported
    // 0x86=Version - V1 supported
    def cmd = zwave.parse(description, [0X20:1, 0X27:1, 0x31:1, 0x40:2, 0x43:2, 0x44:2, 0x70:1, 0x72:1, 0x80:1, 0x86:1])
    def map = []
    def result = null
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
        map = createEvent(result)
    } else {
        log.debug "Non-parsed event. Perhaps wrong version is being handled?: ${description}"
        return null
    }
    
    if (map) {
        log.debug "Parsed ${description} to command ${cmd} to result ${result.inspect()} map=${map}"
        // If the update was a change in the device's fan speed
        if (map.name == "thermostatFanMode" && map.isStateChange) {
            // store the new fan speed
            updateState("lastTriedFanMode", map.value)
        }
        return [map]
    } else {
       return null
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0X20:1, 0X27:1, 0x31:1, 0x40:2, 0x43:2, 0x44:2, 0x70:1, 0x72:1, 0x80:1, 0x86:1])
	// log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}


//***** Event Handlers */
//Handle events coming from the device

// Battery Level event
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [:]

	map.name = "battery"
    if(cmd.batteryLevel == 0xFF){
        map.value = "Low battery"
        map.descriptionText = "${device.displayName} has a low battery"
    } else {
        map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : "USB input"
    }
    map.unit = "%"
    map.displayed = false
    log.debug "Battery Level Reported=$map.value"
    map
}

// - Sensor Multilevel Report
// The device is reporting temperature readings
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv1.SensorMultilevelReport cmd)
{
    log.debug "SensorMultilevelReport reporting...cmd=$cmd"
    // Determine the temperature the device is reporting
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            // temperature
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            log.debug "cmd.scale=$cmd.scale"
            log.debug "cmd.scaledSensorValue=$cmd.scaledSensorValue"
            // converTemp returns string with two decimal places
            // convert to double then to int to drop the decimal
            Integer temp = (int) convertTempValueToCelsius(cmdScale, cmd.scaledSensorValue) //(int) convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale).toDouble()
            map.value = temp
            map.unit = getTemperatureScale()
            map.name = "temperature"
            // Send event to set ShortName + Temp tile
            def shortNameVal = shortName == null ? "ZXT-600" : shortName
            def tempName = shortNameVal + " " + map.value + "°"
            log.debug "Sensor Reporting temperatureName $tempName map.value=$map.value, cmdScale=$cmdScale"
            sendEvent("name":"temperatureName", "value":tempName)
            // Pass value converted to Fahrenheit and Unit of 1 which means Fahrenheit
            sendEvent("name":"temperature", "value":map.value, "isStateChange":true, unit:"C", displayed:true)
            //sendEvent("name":"temperature", "value":map.value, "isStateChange":true, displayed:true)
            break;
        default:
            log.warn "Unknown sensorType reading from device"
            break;
    }
}

// - Thermostat Mode Report
// The device is reporting its heating/cooling Mode
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
    def map = [:]

    // Determine the mode the device is reporting, based on its ZWave id
    map.value = modeMap.find {it.value == cmd.mode}?.key
    map.name = "thermostatMode"
    log.debug "Thermostat Mode reported : $map.value"
    // Return the interpreted report
    map
}

// - Thermostat Fan Mode Report
// The device is reporting its current fan speed
def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeReport cmd) {
    def map = [:]

    // Determine the fan speed the device is reporting, based on its ZWave id
    map.value = fanModeMap.find {it.value == cmd.fanMode}?.key
    map.name = "thermostatFanMode"
    map.displayed = false
    log.debug "Fan Mode Report=$value"
    // Return the interpreted report
    map
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    def map = [:]

    switch (cmd.parameterNumber) {
    // If the device is reporting its remote code
        case commandParameters["remoteCode"]:
            map.name = "remoteCode"
            map.displayed = false

            def short remoteCodeLow = cmd.configurationValue[1]
            def short remoteCodeHigh = cmd.configurationValue[0]
            map.value = (remoteCodeHigh << 8) + remoteCodeLow

            // Display configured code in tile
            log.debug "reported currentConfigCode=$map.value"
            sendEvent("name":"currentConfigCode", "value":map.value)

            break

    // If the device is reporting its remote code
        case commandParameters["tempOffsetParam"]:
            map.name = "tempOffset"
            map.displayed = false

            def short offset = cmd.configurationValue[0]
            if (offset >= 0xFB) {
                // Hex FB-FF represent negative offsets FF=-1 - FB=-5
                offset = offset - 256
            }
            map.value = offset
            log.debug "reported offset=$map.value"
            // Display temp offset in tile
            sendEvent("name":"currentTempOffset", "value":map.value)

            break
    // If the device is reporting its oscillate mode
        case commandParameters["oscillateSetting"]:
            // determine if the device is oscillating
            def oscillateMode = (cmd.configurationValue[0] == 0) ? "off" : "on"

            //log.debug "Updated: Oscillate " + oscillateMode
            map.name = "swingMode"
            map.value = oscillateMode
            map.displayed = false

            map.isStateChange = oscillateMode != getDataByName("swingMode")

            log.debug "reported swing mode = oscillateMode"
            // Store and report the oscillate mode
            updateState("swingMode", oscillateMode)

            break
        default:
            log.warn "Unknown configuration report cmd.parameterNumber"
            break;
    }

    map
}

// - Thermostat Supported Modes Report
// The device is reporting heating/cooling modes it supports
def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
    // Create a string with mode names for each available mode
    def supportedModes = ""
    if(cmd.off) { supportedModes += "off " }
    if(cmd.heat) { supportedModes += "heat " }
    //if(cmd.auxiliaryemergencyHeat) { supportedModes += "emergencyHeat " }
    if(cmd.cool) { supportedModes += "cool " }
    //if(cmd.auto) { supportedModes += "auto " }
    if(cmd.dryAir) { supportedModes += "dry " }
    //if(cmd.autoChangeover) { supportedModes += "autoChangeover " }

    // Report and save available modes
    log.debug "Supported Modes: ${supportedModes}"
    updateState("supportedModes", supportedModes)
}

// - Thermostat Fan Supported Modes Report
// The device is reporting fan speeds it supports
def zwaveEvent(physicalgraph.zwave.commands.thermostatfanmodev2.ThermostatFanModeSupportedReport cmd) {
    // Create a string with mode names for each available mode
    def supportedFanModes = ""
    if(cmd.auto) { supportedFanModes += "fanAuto " }
    if(cmd.low) { supportedFanModes += "fanLow " }
    if(cmd.medium) { supportedFanModes += "fanMedium " }
    if(cmd.high) { supportedFanModes += "fanHigh " }

    // Report and save available speeds
    log.debug "Supported Fan Modes: ${supportedFanModes}"
    updateState("supportedFanModes", supportedFanModes)
}

// - Basic Report
// The device is sending standard ZWave updates
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    log.debug "Zwave event received: $cmd"
}

// - Command Report
// The device is reporting parameter settings
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // simply report it
    log.warn "Unexpected zwave command $cmd"
}

// Update State
// Store mode and settings
def updateState(String name, String value) {
    state[name] = value
    device.updateDataValue(name, value)
}

def ping() {
	log.debug "ping called"
//	poll()
}


// runEvery3Hours()

// Command Implementations
// Ask the device for its current state
def lastPoll() {
    def now=new Date()
    def tz = location.timeZone
    def nowString = now.format("MMM/dd HH:mm",tz)

    sendEvent("name":"lastPoll", "value":nowString)

    log.debug "Polling now $nowString"
    // create a list of requests to send
    def commands = []

    commands <<	zwave.sensorMultilevelV3.sensorMultilevelGet()		// current temperature
    commands <<	zwave.batteryV1.batteryGet()                       // current battery level
    commands <<	zwave.thermostatModeV2.thermostatModeGet()     	// thermostat mode
    commands <<	zwave.thermostatFanModeV3.thermostatFanModeGet()	// fan speed
    commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["remoteCode"])		// remote code
    commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["tempOffsetParam"])  // temp offset
    commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["oscillateSetting"])	// oscillate setting

    // add requests for each thermostat setpoint available on the device
    def supportedModes = getDataByName("supportedModes")
    for (setpoint in setpointMap) {
        // This code doesn't work correctly....Need to fix later for now only implemented supported modes for myself
        //if (supportedModes.tokenize()?.contains(setpoint.key)) {
        log.debug "Requesting setpoint $setpoint.value"
        commands << [zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: setpoint.value)]
        //} else {
        //    log.debug "Skipping unsupported mode $setpoint.key"
        //}
    }

    // send the requests
    delayBetween(commands.collect{ secure(it)}, 1000)
}


// Command Implementations
// Ask the device for its current state
//def poll() {
//    def now=new Date()
//    def tz = location.timeZone
//    def nowString = now.format("MMM/dd HH:mm",tz)
//
//    sendEvent("name":"lastPoll", "value":nowString)
//
//    log.debug "Polling now $nowString"
//    // create a list of requests to send
//    def commands = []
//
//    commands <<	zwave.sensorMultilevelV1.sensorMultilevelGet()		// current temperature
//    commands <<	zwave.batteryV1.batteryGet()                       // current battery level
//    commands <<	zwave.thermostatModeV2.thermostatModeGet()     	// thermostat mode
//    commands <<	zwave.thermostatFanModeV2.thermostatFanModeGet()	// fan speed
//    commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["remoteCode"])		// remote code
//    commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["tempOffsetParam"])  // temp offset
//    commands <<	zwave.configurationV1.configurationGet(parameterNumber: commandParameters["oscillateSetting"])	// oscillate setting
//
//    // add requests for each thermostat setpoint available on the device
//    def supportedModes = getDataByName("supportedModes")
//    for (setpoint in setpointMap) {
//        // This code doesn't work correctly....Need to fix later for now only implemented supported modes for myself
//        //if (supportedModes.tokenize()?.contains(setpoint.key)) {
//        log.debug "Requesting setpoint $setpoint.value"
//        commands << [zwave.thermostatSetpointV1.thermostatSetpointGet(setpointType: setpoint.value)]
//        //} else {
//        //    log.debug "Skipping unsupported mode $setpoint.key"
//        //}
//    }
//
//    // send the requests
//    delayBetween(commands.collect{ secure(it)}, 2300)
//}

def setHeatingSetpoint(degrees) {
    def degreesInteger = degrees as Integer
    def temperatureScale = "C"

    if (temperatureScale == "C") {
        // ZXT-600 lowest settings is 19 C
        if (degreesInteger < 17) {
            degreesInteger = 17
        }
        // ZXT-600 highest setting is 28 C
        if (degreesInteger > 30) {
            degreesInteger = 30
        }
    } else {
        // ZXT-600 lowest settings is 67 F
        if (degreesInteger < 63) {
            degreesInteger = 63
        }
        // ZXT-600 highest setting is 84
        if (degreesInteger > 86) {
            degreesInteger = 86
        }
    }
    log.debug "setHeatingSetpoint({$degreesInteger} ${temperatureScale})"
    sendEvent("name":"heatingSetpoint", "value":degreesInteger, unit: "C")
    setThermostatMode("heat")
    //def celsius = (temperatureScale == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
    //"st wattr 0x${device.deviceNetworkId} 1 0x201 0x12 0x29 {" + hex(celsius*100) + "}"
    //def setpointMode = physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1
    //setThermostatSetpointForMode(degreesInteger.toDouble(), setpointMode)
}

def setCoolingSetpoint(degrees) {
    def degreesInteger = degrees as Integer
    def temperatureScale = "C"

    if (temperatureScale == "C") {
        // ZXT-600 lowest settings is 19 C
        if (degreesInteger < 17) {
            degreesInteger = 17
        }
        // ZXT-600 highest setting is 28 C
        if (degreesInteger > 30) {
            degreesInteger = 30
        }
    } else {
        // ZXT-600 lowest settings is 67 F
        if (degreesInteger < 63) {
            degreesInteger = 63
        }
        // ZXT-600 highest setting is 28
        if (degreesInteger > 86) {
            degreesInteger = 86
        }
    }
    log.debug "setCoolingSetpoint({$degreesInteger} ${temperatureScale})"
    sendEvent("name":"coolingSetpoint", "value":degreesInteger, unit: "C")
    setThermostatMode("cool")
    // Sending temp to zxt-600
    //def celsius = (temperatureScale == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
    //"st wattr 0x${device.deviceNetworkId} 1 0x201 0x11 0x29 {" + hex(celsius*100) + "}"
    //def setpointMode = physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1
    //setThermostatSetpointForMode(degreesInteger.toDouble(), setpointMode)
}


/*
Based on ZXT-600 user manual (IR Learning Mapping Table, Parameter number 25)

Parameter Value    |          Thermostat Command & IR Setting
(Storage Location) | Storage in Celsius Unit | Storage in Fahrenheit Unit

0| OFF | OFF
1| ON (RESUME) | ON (RESUME)
2| 17°C COOL | 63°F COOL
3| 18°C COOL | 64°F COOL
4| 19°C COOL | 66°F or 67°F COOL
5| 20°C COOL | 68°F or 69°F COOL
6| 21°C COOL | 70°F or 71°F COOL
7| 22°C COOL | 72°F or 73°F COOL
8| 23°C COOL | 74°F or 75°F COOL
9| 24°C COOL | 76°F COOL
10| 25°C COOL | 77°F or 78°F COOL
11| 26°C COOL | 79°F or 80°F COOL
12| 27°C COOL | 81°F or 82°F COOL
13| 28°C COOL | 83°F or 84°F COOL
14| 29°C COOL | 85°F COOL
15| 30°C COOL | 86°F COOL
16| 17°C HEAT | 63°F HEAT
17| 18°C HEAT | 64°F HEAT
18| 19°C HEAT | 66°F or 67°F HEAT
19| 20°C HEAT | 68°F or 69°F HEAT
20| 21°C HEAT | 70°F or 71°F HEAT
21| 22°C HEAT | 72°F or 73°F HEAT
22| 23°C HEAT | 74°F or 75°F HEAT
23| 24°C HEAT | 76°F HEAT
24| 25°C HEAT | 77°F or 78°F HEAT
25| 26°C HEAT | 79°F or 80°F HEAT
26| 27°C HEAT | 81°F or 82°F HEAT
27| 28°C HEAT | 83°F or 84°F HEAT
28| 29°C HEAT | 85°F HEAT
29| 30°C HEAT | 86°F HEAT
30| DRY MODE  |DRY MODE
31| AUTO MODE | AUTO MODE
32| FAN MODE | FAN MODE

*/
def setLearningPosition(position) {
    log.debug "Setting learning postition: $position"
    sendEvent("name":"learningPosition", "value":position)
    def ctemp = 0
    if (position < 16) {
        ctemp=position+15
    } else {
        ctemp=position+1
    }
    def ftempLow=(Math.ceil(((ctemp*9)/5)+32)).toInteger()
    def ftempHigh=ftempLow+1
    def ftempLower = ftempLow-1
    def positionTemp = "not set"
    switch (position) {
        case 0:
            positionTemp = 'Off'
            break
        case 1:
            positionTemp = 'On(resume)'
            break
        case [3,4]:
        	positionTemp = "cool ${ctemp}C ${ftempLower}-${ftempLow}F"
            break
        case [5,6,7,8,10,11,12,13]:            
            positionTemp = "cool ${ctemp}C ${ftempLow}-${ftempHigh}F"
            break
        case [2,9,14,15]:
            positionTemp = "cool ${ctemp}C ${ftempLow}F"
            break
        case [17,18]:
        	positionTemp = "heat ${ctemp}C ${ftempLower}-${ftempLow}F"
            break
        case [19,20,21,22,24,25,26,27]:
            positionTemp = "heat ${ctemp}C ${ftempLow}-${ftempHigh}F"
            break
        case [16,23,28,29]:
            positionTemp = "heat ${ctemp}C ${ftempLow}F"
            break
        case 30:
            positionTemp = 'Dry mode'
            break
        case 31:
            positionTemp = 'Auto mode'
            break
        case 32:
            positionTemp = 'Fan mode'
            break    
        default:
            positionTemp = 'Invalid'
            break
    }   
    log.debug "Ready to learn ${positionTemp}"
    sendEvent("name":"learningPositionTemp", "value":positionTemp)
}

def issueLearningCommand() {
    def position = 0
    if (device.currentValue("learningPosition"))
    	position = device.currentValue("learningPosition").toInteger()
    log.debug "Issue Learning Command pressed Position Currently: $position"

    def positionConfigArray = [position]

    log.debug "Position Config Array: ${positionConfigArray}"

    secureLearnConfigSequence ([
            // Send the new remote code
            zwave.configurationV1.configurationSet(configurationValue: positionConfigArray,
                    parameterNumber: commandParameters["learningMode"], size: 1)
    ])
}

// It is hard to define as IR signal cannot be expected within IR capturing period
private secureLearnConfigSequence(commands, delay=3000) {
	delayBetween(commands.collect{ secure(it) }, delay)
}


//***** Set the thermostat */
def setThermostatSetpoint(degrees) {
    log.debug "setThermostatSetpoint called.....want to get rid of that"
    // convert the temperature to a number and execute
    setThermostatSetpoint(degrees.toDouble())
}

// Configure
// Syncronize the device capabilities with those that the UI provides
def configure() {
 	// update the device's remote code to ensure it provides proper mode info
    // The reason why remote set code process is excluded from the below sequence is 
    // the time consuming is up to 12s based on 8490 characteristic.

    setRemoteCode()
//    secureConfigSequence([
//            setTempOffset(),
//            // Request the device's current heating/cooling mode
//            zwave.thermostatModeV2.thermostatModeSupportedGet(),
//            // Request the device's current fan speed
//            zwave.thermostatFanModeV2.thermostatFanModeSupportedGet(),
//            // Assign the device to ZWave group 1
//            zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
//   ])
}

/*
    Strongly recommend to reserve 30s delay for all parameter SET of configuration command class.
*/
private secureConfigSequence(commands, delay=3000) {
	delayBetween(commands.collect{ secure(it) }, delay)
}


// Switch Fan Mode
// Switch to the next available fan speed
def switchFanMode() {
    // Determine the current fan speed setting
    def currentMode = device.currentState("thermostatFanMode")?.value
    def lastTriedMode = getDataByName("lastTriedFanMode") ?: currentMode.value ?: "off"

    // Determine what fan speeds are available
    def supportedModes = getDataByName("supportedFanModes") ?: "fanAuto fanLow"
    def modeOrder = fanModes()
    //log.info modeOrder

    // Determine what the next fan speed should be
    def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
    def nextMode = next(lastTriedMode)
    while (!supportedModes?.contains(nextMode) && nextMode != "fanAuto") {
        nextMode = next(nextMode)
    }

    // Make it so
    switchToFanMode(nextMode)
}

// Switch to Fan Mode
// Given the name of a fan mode, make it happen
def switchToFanMode(nextMode) {
    def supportedFanModes = getDataByName("supportedFanModes")
    if(supportedFanModes && !supportedFanModes.tokenize()?.contains(nextMode)) log.warn "thermostat mode '$nextMode' is not supported"

    // If the mode is even possible
    if (nextMode in fanModes()) {
        // Try to switch to the mode
        updateState("lastTriedFanMode", nextMode)
        return "$nextMode"()  // Call the function perform the mode switch
    } else {
        // Otherwise, bail
        log.debug("no fan mode method '$nextMode'")
    }
}

// Get Data By Name
// Given the name of a setting/attribute, lookup the setting's value
def getDataByName(String name) {
    state[name] ?: device.getDataValue(name)
}


// - Thermostat Setpoint Report
// The device is telling us what temperatures it is set to for a particular mode
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
    log.info "RRG V2 ThermostatSetpointReport cmd=$cmd"
    log.debug "cmd.scale=$cmd.scale"
    log.debug "cmd.scaledValue=$cmd.scaledValue"
    // Determine the temperature and mode the device is reporting
    def cmdScale = cmd.scale
    
//    log.debug "deviceScale=${deviceScale}"
//    def deviceScaleString = deviceScale == 0 ? "C" : "F"
        
        //def deviceScale = 2
    
    log.debug "cmdScale=${cmdScale}"
    def deviceScaleString = cmdScale == 0 ? "C" : "F"
	
    //NOTE:  When temp is sent to device in Fahrenheit and returned in celsius
    //       1 degree difference is normal.  Device only has 1 degree celsius granularity
    //       issuing 80F for example returns 26C, which converts to 79F
    //       Maybe I should lie to user and report current set temp rather than reported temp
    //       to avoid confusion and false bug reports....needs to be considered.
    def degrees = cmd.scaledValue
    def reportedTemp = convertTempValueToCelsius(deviceScaleString, degrees)
    
    // Determine what mode the setpoint is for, if the mode is not valid, bail out
    def name = setpointReportingMap.find {it.value == cmd.setpointType}?.key
    if (name == null) {
        log.warn "Setpoint Report for Unknown Type $cmd.setpointType"
        return
    }
     
    // Return the interpretation of the report
    log.debug "Thermostat Setpoint Report for $name = $reportedTemp forcing state change true"
    sendEvent("name":name, "value":reportedTemp, "isStateChange":true, "unit": "C")
}

// Set Thermostat Mode
// Set the device to the named mode
def setThermostatMode(String value) {

    def commands = []
    def secureCommands = []
    def degrees=0
    def setpointMode=null

    if (value == "cool") {
        degrees = device.currentValue("coolingSetpoint")
        setpointMode = physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_COOLING_1
    } else if (value == "heat") {
        degrees = device.currentValue("heatingSetpoint")
        setpointMode = physicalgraph.zwave.commands.thermostatsetpointv1.ThermostatSetpointSet.SETPOINT_TYPE_HEATING_1
    } else if (value == "dry" || value == "fan" || value == "auto" || value == "off") {
        log.debug("Dry Mode or Auto Mode or Fan Mode or Off no need to send temp")
        degrees = 0
    } else {
        log.warn("Unknown thermostat mode set:$value")
    }

    // Send temp if degrees set
    if (degrees != 0 && setpointMode != null) {
        def deviceScale = 0
        def deviceScaleString = "C"
        log.debug "state.scale = ${state.scale}"
//        if(state.scale){
//            deviceScale = state.scale ?: 1
//            deviceScaleString = deviceScale == 0 ? "C" : "F"
//        }else{
//            deviceScaleString = airconTemperatureScale ?: "F";
//            deviceScale = deviceScaleString == "C" ? 0 : 1
 //       }
        
        deviceScale = 0
        deviceScaleString = "C"
        
        def locationScale = getTemperatureScale()
        log.debug "state.precision=${state.precision}"
        def p = (state.precision == null) ? 0 : state.precision
        log.debug "p=${p}"
        
        def convertedDegrees = convertTempValueToCelsius(deviceScaleString, degrees)
   
        log.debug "convertedDegrees=${convertedDegrees}, degrees=${degrees}"

        // Report the new temperature being set
        log.debug "new temp ${degrees}"
        log.debug("Sending Temp [$convertedDegrees] for $value mode before enabling mode")
        // Send the new temperature from the thermostat and request confirmation
        commands << zwave.thermostatSetpointV2.thermostatSetpointSet(setpointType: setpointMode, scale: deviceScale, precision: p, scaledValue: convertedDegrees)
        //secureCommands << zwave.securityV1.securityMessageEncapsulation().encapsulate(commands)
        log.debug ("Secure Command: ${commands}")
        commands << zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: setpointMode)
    }

    // Set thermostat mode and request confirmation
    commands << zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value])
    commands << zwave.thermostatModeV2.thermostatModeGet()

    // send the requests
    //delayBetween(commands, 2300)
	delayBetween(commands.collect{ secure(it)}, 100)
}

def convertTempValueToCelsius(String fromScale, def fromValue) {
    if ( fromScale == "F" ) {
        return fahrenheitToCelsius(fromValue);
    }
    return fromValue;
}

// Set Thermostat Fan Mode
// Set the device to the named fan speed
def setThermostatFanMode(String value) {

    log.debug "Thermostat FAN Mode SET ${value} value:  ${fanModeMap[value]}"
     
     secureSequence([
            // Command the device to change the fan speed
            //zwave.thermostatFanModeV2.thermostatFanModeSet(fanMode: fanModeMap[value]).format(),
            zwave.thermostatFanModeV2.thermostatFanModeSet(fanMode: fanModeMap[value]),
            // Request an update to make sure it worked
            //zwave.thermostatFanModeV2.thermostatFanModeGet().format()
            zwave.thermostatFanModeV2.thermostatFanModeGet()
    ])
     
    //delayBetween([
            // Command the device to change the fan speed
    //        zwave.thermostatFanModeV2.thermostatFanModeSet(fanMode: fanModeMap[value]).format(),
            // Request an update to make sure it worked
    //        zwave.thermostatFanModeV2.thermostatFanModeGet().format()
    //])
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}


// Mode Commands 
// provide simple access to mode changes

// public interface commands for Thermostat
def cool() {
    switchModeCool()
}

def heat() {
    switchModeHeat()
}

def dry() {
    switchModeDry()
}

def auto() {
    switchModeAuto()
}

def fan() {
    switchModeFan()
}

def off() {
    log.debug "${device.name} received off request"
    switchModeOff()
}

def on() {
    log.debug "${device.name} received on request"
    // Added "Switch Attribute on/off for Harmony Remote
    // TODO: RRG add preference for on turns on heat or AC hard code to ac for now
    switchModeCool()
}

// switchModeCommands
def switchModeOff() {
    setThermostatMode("off")
}

def switchModeHeat() {
    setThermostatMode("heat")
}

def emergencyHeat() {
    setThermostatMode("emergencyHeat")
}

def switchModeDry() {
    setThermostatMode("dry")
}

def switchModeFan() {
    setThermostatMode("fan")
}

def switchModeCool() {
    setThermostatMode("cool")
}

def switchModeAuto() {
    setThermostatMode("auto")
}

def autoChangeover() {
    setThermostatMode("autoChangeover")
}

def switchFanLow() {
    log.debug "setting fan mode low"
    setThermostatFanMode("fanLow")
}

def switchFanMed() {
    log.debug "setting fan mode med"
    setThermostatFanMode("fanMedium")
}

def switchFanHigh() {
    log.debug "setting fan mode high"
    setThermostatFanMode("fanHigh")
}

def switchFanAuto() {
    log.debug "setting fan mode auto"
    setThermostatFanMode("fanAuto")
}

// Set Remote Code
// tell the ZXT-600 what remote code to use when communicating with the A/C
def setRemoteCode() {
    // Load the user's remote code setting
    def remoteCodeVal = remoteCode.toInteger()

    // Divide the remote code into a 2 byte value
    def short remoteCodeLow = remoteCodeVal & 0xFF
    def short remoteCodeHigh = (remoteCodeVal >> 8) & 0xFF
    def remoteBytes = [remoteCodeHigh, remoteCodeLow]

    log.debug "New Remote Code: ${remoteBytes}"

    setRemoteSequence([
            // Send the new remote code
            zwave.configurationV1.configurationSet(configurationValue: remoteBytes,
                    parameterNumber: commandParameters["remoteCode"], size: 2),
            // Request the device's remote code to make sure the new setting worked
            zwave.configurationV1.configurationGet(parameterNumber: commandParameters["remoteCode"])
    ])
}

private setRemoteSequence(commands, delay=12000) {
	delayBetween(commands.collect{ secure(it) }, delay)
}


def setTempOffset() {
    // Load the user's remote code setting
    def tempOffsetVal = tempOffset == null ? 0 : tempOffset.toInteger()
    // Convert negative values into hex value for this param -1 = 0xFF -5 = 0xFB
    if (tempOffsetVal < 0) {
        tempOffsetVal = 256 + tempOffsetVal
    }

    def configArray = [tempOffsetVal]

    log.debug "TempOffset: ${tempOffsetVal}"

    setTempOffsetSequence ([
            // Send the new remote code
            zwave.configurationV1.configurationSet(configurationValue: configArray,
                    parameterNumber: commandParameters["tempOffsetParam"], size: 1),
            // Request the device's remote code to make sure the new setting worked
            zwave.configurationV1.configurationGet(parameterNumber: commandParameters["tempOffsetParam"])
    ])
}

private setTempOffsetSequence(commands, delay=100) {
	delayBetween(commands.collect{ secure(it) }, delay)
}


// Switch Fan Oscillate
// Toggle fan oscillation on and off
def switchFanOscillate() {
    // Load the current swingmode and invert it (Off becomes true, On becomes false)
    def swingMode = (getDataByName("swingMode") == "off")

    // Make the new swingMode happen
    setFanOscillate(swingMode)
}

def swingModeOn() {
    log.debug "Setting Swing mode On"
    setFanOscillate(true)
}

def swingModeOff() {
    log.debug "Setting Swing mode Off"
    setFanOscillate(false)
}

// Set Fan Oscillate
// Set the fan oscillation to On (swingMode == true) or Off (swingMode == false)
def setFanOscillate(swingMode) {
    // Convert the swing mode requested to 1 for on, 0 for off
    def swingValue = swingMode ? 1 : 0

    delayBetween ([
            // Command the new Swing Mode
            zwave.configurationV1.configurationSet(configurationValue: [swingValue],
                    parameterNumber: commandParameters["oscillateSetting"], size: 1).format(),
            // Request the device's swing mode to make sure the new setting was accepted
            zwave.configurationV1.configurationGet(parameterNumber: commandParameters["oscillateSetting"]).format()
    ], 100)
}

/** additional method for habitap **/
def setMode(newMode){
    if(newMode == "cool"){
        cool();
    }else if(newMode == "dry"){
        dry();
    }else{
        switchModeAuto();
    }
    sendEvent("name":"mode", "value": newMode);
}

//this is used by API
def setTemperature(def actualTemp){
   log.debug "temperature ${actualTemp}"
   double temperatureValue = actualTemp as double
   sendEvent(name: "temperature", value: (int)temperatureValue)
   setCoolingSetpoint((int)temperatureValue);
}

def refresh(){
    lastPoll();
}