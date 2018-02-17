/**
 *  Copyright 2017 Stelpro
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
 *  Stelpro Ki Thermostat
 *
 *  Author: Stelpro
 *
 *  Date: 2017-11-16
 */

preferences {
    input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "[Do not use space](Blank = No Forecast)")
    input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: true, displayDuringSetup: true)
}

metadata {
    definition (name: "Stelpro Ki Thermostat", namespace: "stelpro", author: "Stelpro") {
        capability "Actuator"
        capability "Temperature Measurement"
        capability "Thermostat"
		capability "Configuration"
        capability "Polling"
        capability "Sensor"
        capability "Refresh"

        attribute "outsideTemp", "number"

        command "switchMode"
        command "quickSetHeat"
        command "quickSetOutTemp"
        command "increaseHeatSetpoint"
        command "decreaseHeatSetpoint"
        command "setCustomThermostatMode"
        command "eco"

        fingerprint deviceId: "0x0806", inClusters: "0x5E,0x86,0x72,0x40,0x43,0x31,0x85,0x59,0x5A,0x73,0x20,0x42"
    }

    // simulator metadata
    simulator {
        //Add test code here
    }

    tiles(scale : 2) {
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temp", label:'${currentValue}')
                attributeState("high", label:'HIGH')
            	attributeState("low", label:'LOW')
            	attributeState("--", label:'--')
			}
            tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "increaseHeatSetpoint")
                attributeState("VALUE_DOWN", action: "decreaseHeatSetpoint")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("heat", label:'${name}')
				attributeState("eco", label:'${name}')
			}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT")
            {
            	attributeState("heatingSetpoint", label:'${currentValue}')
            }
		}
        standardTile("mode", "device.thermostatMode", width: 4, height: 2) {
			state "heat", label:'${name}', action:"switchMode", nextState:"eco", icon:"http://cdn.device-icons.smartthings.com/Home/home29-icn@2x.png"
			state "eco", label:'${name}', action:"switchMode", nextState:"heat", icon:"http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn@2x.png"
        }
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 4, height: 4) {
            state "temperature", label:'Setpoint ${currentValue}°', backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
            ]
            state "--", label:'--', backgroundColor:"#bdbdbd"
        }
        standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main ("thermostatMulti")
        details(["thermostatMulti", "mode", "heatingSetpoint", "refresh"])
    }
}

def parse(String description)
{
	//if (description == "updated")
    	//return []

    //Class, version
    def map = createEvent(zwaveEvent(zwave.parse(description, [0x40:2, 0x43:2, 0x31:3, 0x42:1])))
    if (!map) {
        return null
    }

    def result = [map]
    if (map.isStateChange && map.name in ["heatingSetpoint","thermostatMode"]) {
        def map2 = [
                name: "thermostatSetpoint",
                unit: getTemperatureScale()
        ]
        if (map.name == "thermostatMode") {
            state.lastTriedMode = map.value
			map2.value = device.latestValue("heatingSetpoint")
        }
        else {
            def mode = device.latestValue("thermostatMode")
            log.info "THERMOSTAT, latest mode = ${mode}"
            if (map.name == "heatingSetpoint") {
                map2.value = map.value
                map2.unit = map.unit
            }
        }
        if (map2.value != null) {
            log.debug "THERMOSTAT, adding setpoint event: $map"
            result << createEvent(map2)
        }
    }
    log.debug "Parse returned $result"
    result
}

// Event Generation
def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd)
{
    def cmdScale = cmd.scale == 1 ? "F" : "C"
    def temp;
    float tempfloat;
    def map = [:]
    if (cmd.scaledValue >= 327)
    {
    	map.value = "--"
    }
    else
    {
        temp = convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision)
        tempfloat = (Math.round(temp.toFloat() * 2)) / 2
        map.value = tempfloat
    }
    map.unit = getTemperatureScale()
    map.displayed = false
    switch (cmd.setpointType) {
        case 1:
            map.name = "heatingSetpoint"
            break;
        default:
            return [:]
    }
    // So we can respond with same format
    state.size = cmd.size
    state.scale = cmd.scale
    state.precision = cmd.precision
    //sendEvent(name:"heatingSetpoint", value:map.value)
    map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv3.SensorMultilevelReport cmd)
{
    def temp;
    float tempfloat;
    def format;
    def map = [:]
    if (cmd.sensorType == 1) {
        map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
        map.unit = getTemperatureScale()
        map.name = "temperature"

        temp = map.value
        if (temp == "32765")		//0x7FFD
        {
            map.value = "low"
        }
        else if (temp == "32767")	//0x7FFF
        {
            map.value = "high"
        }
        else if (temp == "-32768")	//0x8000
        {
            map.value = "--"
        }
        else
        {
    		tempfloat = (Math.round(temp.toFloat() * 2)) / 2
    		map.value = tempfloat
        }

    } else if (cmd.sensorType == 5) {
        map.value = cmd.scaledSensorValue
        map.unit = "%"
        map.name = "humidity"
    }
    //sendEvent(name:"temperature", value:map.value)
    map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport cmd)
{
    def map = [:]
    switch (cmd.operatingState) {
        case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_IDLE:
        map.value = "idle"
        break
        case physicalgraph.zwave.commands.thermostatoperatingstatev1.ThermostatOperatingStateReport.OPERATING_STATE_HEATING:
        map.value = "heating"
        break
    }
    map.name = "thermostatOperatingState"
    
    if (settings.heatdetails == "No") {
    	map.displayed = false
    }
    
    map
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd) {
    def map = [:]
    switch (cmd.mode) {
        //Heat mode
		case physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport.MODE_HEAT:
            map.value = "heat"
            break
        
		//Eco mode
		case '11':
            map.value = "eco"
            break
    }
    map.name = "thermostatMode"
    //sendEvent(name:"thermostatMode", value:map.value)
    map
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	delayBetween([
    	zwave.associationV1.associationRemove(groupingIdentifier:1, nodeId:0).format(),
        zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format(),
        poll()
    ], 2300)
}


def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeSupportedReport cmd) {
    log.debug "Zwave event received: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    log.debug "Zwave event received: $cmd"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.warn "Unexpected zwave command $cmd"
}

// Command Implementations
def poll() {
    def weather

    // If there is a zipcode defined, weather forecast will be sent. Otherwise, no weather forecast.
    if (settings.zipcode) {
        log.debug "ZipCode: ${settings.zipcode}"
        weather = getWeatherFeature( "conditions", settings.zipcode )

        // Check if the variable is populated, otherwise return.
        if (!weather) {
            log.debug( "Something went wrong, no data found." )
            return false
        }

        // Set the tiles
        def locationScale = getTemperatureScale()
        def tempToSend
        if (locationScale == "C")
        {
            log.debug( "Outdoor Temperature: ${weather.current_observation.temp_c}ºC" )
            sendEvent( name: 'outsideTemp', value: weather.current_observation.temp_c )
            tempToSend = weather.current_observation.temp_c
        }
        else
        {
            log.debug( "Outdoor Temperature: ${weather.current_observation.temp_f}ºF" )
            sendEvent( name: 'outsideTemp', value: weather.current_observation.temp_f )
            tempToSend = weather.current_observation.temp_f
        }


        delayBetween([
                quickSetOutTemp(tempToSend),
                zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format(),
                zwave.thermostatModeV2.thermostatModeGet().format(),
                zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format(),
                zwave.sensorMultilevelV3.sensorMultilevelGet().format(), // current temperature
                sendEvent( name: 'change', value: 0 )
        ], 100)
    } else {
        delayBetween([
                zwave.thermostatOperatingStateV1.thermostatOperatingStateGet().format(),
                zwave.thermostatModeV2.thermostatModeGet().format(),
                zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format(),
                zwave.sensorMultilevelV3.sensorMultilevelGet().format(), // current temperature
                sendEvent( name: 'change', value: 0 )
        ], 100)
    }
}

def refresh() {
	poll()
}

def configure() {
	poll()
}

def quickSetHeat(degrees) {
    setHeatingSetpoint(degrees, 0)
}

def setHeatingSetpoint(preciseDegrees, delay = 0) {
	def degrees = new BigDecimal(preciseDegrees).setScale(1, BigDecimal.ROUND_HALF_UP)
	log.trace "setHeatingSetpoint($degrees, $delay)"
    def deviceScale = state.scale ?: 1
	def deviceScaleString = deviceScale == 2 ? "C" : "F"
    def locationScale = getTemperatureScale()
	def p = (state.precision == null) ? 1 : state.precision

    def convertedDegrees
    if (locationScale == "C" && deviceScaleString == "F") {
        convertedDegrees = celsiusToFahrenheit(degrees)
    } else if (locationScale == "F" && deviceScaleString == "C") {
        convertedDegrees = fahrenheitToCelsius(degrees)
    } else {
        convertedDegrees = degrees
    }
	
	sendEvent(name: "heatingSetpoint", value: degrees, unit: locationScale)

    delayBetween([
            zwave.thermostatSetpointV2.thermostatSetpointSet(setpointType: 1, scale: deviceScale, precision: p, scaledValue: convertedDegrees).format(),
			zwave.thermostatSetpointV2.thermostatSetpointGet(setpointType: 1).format(),
            poll()
    ], 1000)
}

def quickSetOutTemp(degrees) {
    setOutdoorTemperature(degrees, 0)
}

def setOutdoorTemperature(degrees, delay = 0) {
    setOutdoorTemperature(degrees.toDouble(), delay)
}

def setOutdoorTemperature(Double degrees, Integer delay = 0) {
    def deviceScale
    def locationScale = getTemperatureScale()
    def p = (state.precision == null) ? 1 : state.precision

    if (locationScale == "C")
    {
        deviceScale = 0
    }
    else
    {
        deviceScale = 1
    }
    log.info "setOutdoorTemperature: ${degrees}"
    zwave.sensorMultilevelV3.sensorMultilevelReport(sensorType: 1, scale: deviceScale, precision: p,  scaledSensorValue: degrees).format()
}

def increaseHeatSetpoint()
{
	float currentSetpoint = device.currentValue("heatingSetpoint")
	def locationScale = getTemperatureScale()
	float maxSetpoint
	float step

	if (locationScale == "C")
	{
		maxSetpoint = 30;
		step = 0.5
	}
	else
	{
		maxSetpoint = 86
		step = 1
	}

	if (currentSetpoint < maxSetpoint)
	{
		currentSetpoint = currentSetpoint + step
		quickSetHeat(currentSetpoint)
	}
}

def decreaseHeatSetpoint()
{
	float currentSetpoint = device.currentValue("heatingSetpoint")
	def locationScale = getTemperatureScale()
	float minSetpoint
	float step

	if (locationScale == "C")
	{
		minSetpoint = 5;
		step = 0.5
	}
	else
	{
		minSetpoint = 41
		step = 1
	}

	if (currentSetpoint > minSetpoint)
	{
		currentSetpoint = currentSetpoint - step
		quickSetHeat(currentSetpoint)
    }
}

def switchMode() {
    def currentMode = device.currentState("thermostatMode")?.value
  	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "heat"
  	def modeOrder = modes()
  	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
  	def nextMode = next(lastTriedMode)
  	state.lastTriedMode = nextMode
    delayBetween([
        zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[nextMode]).format(),
    	zwave.thermostatModeV2.thermostatModeGet().format()
  ], 1000)
}

def modes() {
    ["heat", "eco"]
}

def getModeMap() { [
        "heat": 1,
        "eco": 11,
]}

def getDataByName(String name) {
    state[name] ?: device.getDataValue(name)
}

def setCoolingSetpoint(coolingSetpoint) {
    log.trace "${device.displayName} does not support cool setpoint"
}

def heat() {
	log.trace "heat mode applied"
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 1).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], 1000)
}

def eco() {
	log.trace "eco mode applied"
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: 11).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], 1000)
}

def off() {
    log.trace "${device.displayName} does not support off mode"
}

def auto() {
    log.trace "${device.displayName} does not support auto mode"
}

def emergencyHeat() {
    log.trace "${device.displayName} does not support emergency heat mode"
}

def cool() {
    log.trace "${device.displayName} does not support cool mode"
}

def setCustomThermostatMode(mode) {
   setThermostatMode(mode)
}

def setThermostatMode(String value) {
	delayBetween([
		zwave.thermostatModeV2.thermostatModeSet(mode: modeMap[value]).format(),
		zwave.thermostatModeV2.thermostatModeGet().format()
	], 1000)
}

def fanOn() {
    log.trace "${device.displayName} does not support fan on"
}

def fanAuto() {
    log.trace "${device.displayName} does not support fan auto"
}

def fanCirculate() {
    log.trace "${device.displayName} does not support fan circulate"
}

def setThermostatFanMode() {
    log.trace "${device.displayName} does not support fan mode"
}