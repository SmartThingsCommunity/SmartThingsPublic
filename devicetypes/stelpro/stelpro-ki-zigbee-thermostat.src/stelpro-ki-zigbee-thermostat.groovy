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
 *  Stelpro Ki ZigBee Thermostat
 *
 *  Author: Stelpro
 *
 *  Date: 2017-05-08
 */
 
preferences {
	section {
 			input("unitformat", "enum", title: "What unit format do you want your thermostat to display temperature?", options: ["Celsius", "Fahrenheit"], defaultValue: "Celsius", required: false, displayDuringSetup: false)
            input("lock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
            input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true)
    		input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "[Do not use space](Blank = No Forecast)")
    }
}

metadata {
	definition (name: "Stelpro Ki ZigBee Thermostat", namespace: "Stelpro", author: "Stelpro") {
        capability "Thermostat"
        capability "Temperature Measurement"
        capability "Actuator"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Configuration"
        
		command "switchMode"
        command "quickSetHeat"
        command "quickSetOutTemp"
        command "increaseHeatSetpoint"
        command "decreaseHeatSetpoint"
        command "parameterSetting"

		fingerprint profileId: "0104", endpointId: "19", inClusters: " 0000,0003,0201,0204", outClusters: "0402"
	}

	// simulator metadata
	simulator { }

	tiles {
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
				attributeState("off", label:'Off')
				attributeState("comfort", label:'Comfort')
				attributeState("eco", label:'Eco')
			}
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT")
            {
            	attributeState("heatingSetpoint", label:'${currentValue}')
            }
		}
        standardTile("mode", "device.thermostatMode", width: 2, height: 2) {
        	state "off", label:'Off', action:"switchMode", nextState:"comfort", icon:"st.thermostat.off"
			state "comfort", label:'Comfort', action:"switchMode", nextState:"eco", icon:"http://cdn.device-icons.smartthings.com/Home/home29-icn@2x.png"
			state "eco", label:'Eco', action:"switchMode", nextState:"off", icon:"http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn@2x.png"
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
            state "temperature", label:'Setpoint\n${currentValue}°', backgroundColors:[
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
            state "default", action:"poll", icon:"st.secondary.refresh"
        }
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

        main ("thermostatMulti")
        details(["thermostatMulti", "mode", "heatingSetpoint", "refresh", "configure"])
    }
}


// parse events into attributes
def parse(String description) {
	log.debug "Parse description $description"
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.cluster == "0201" && descMap.attrId == "0000")
        {
			log.debug "TEMP"
			map.name = "temperature"
			map.value = getTemperature(descMap.value)
            if (descMap.value == "7ffd")		//0x7FFD
            {
                map.value = "low"
            }
            else if (descMap.value == "7fff")	//0x7FFF
            {
                map.value = "high"
            }
            else if (descMap.value == "8000")	//0x8000
            {
                map.value = "--"
            }
            sendEvent(name:"temperature", value:map.value)
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "0012")
        {
			log.debug "HEATING SETPOINT"
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
            if (descMap.value == "8000")		//0x8000
            {
                map.value = "--"
            }
            sendEvent(name:"heatingSetpoint", value:map.value)
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "001c")
        {
        	if (descMap.value.size() == 8)
            {
				log.debug "MODE"
				map.name = "thermostatMode"
				map.value = getModeMap()[descMap.value]
				sendEvent(name:"thermostatMode", value:map.value)
            }
            else if (descMap.value.size() == 10)
            {
            	log.debug "MODE & SETPOINT MODE"
                def twoModesAttributes = descMap.value[0..-9]
                map.name = "thermostatMode"
				map.value = getModeMap()[twoModesAttributes]
				sendEvent(name:"thermostatMode", value:map.value)
            }
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "401c")
        {
            log.debug "SETPOINT MODE"
            log.debug "descMap.value $descMap.value"
            map.name = "thermostatMode"
            map.value = getModeMap()[descMap.value]
            sendEvent(name:"thermostatMode", value:map.value)
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "0008")
        {
        	log.debug "HEAT DEMAND"
            map.name = "thermostatOperatingState"
            map.value = getModeMap()[descMap.value]
            if (map.value == "off")
            {
            	map.value = "idle"
            }
            else
            {
            	map.value = "heating"
            }
            sendEvent(name:"thermostatOperatingState", value:map.value)
            if (settings.heatdetails == "No")
            {
    			map.displayed = false
    		}
        }
	}

	def result = null
	if (map) {
		result = createEvent(map)
	}
	log.debug "Parse returned $map"
	return result
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def getModeMap() { [
	"00":"off",
    "04":"comfort",
	"05":"eco"
]}

def getFanModeMap() { [
	"04":"fanOn",
	"05":"fanAuto"
]}

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
        log.debug( "Outdoor Temperature: ${weather.current_observation.temp_c}ºC" )
        sendEvent( name: 'outsideTemp', value: weather.current_observation.temp_c )
        tempToSend = weather.current_observation.temp_c

		delayBetween([
                quickSetOutTemp(tempToSend),
                zigbee.readAttribute(0x201, 0x0000),	//Read Local Temperature
                zigbee.readAttribute(0x201, 0x0008),	//Read PI Heating State
    			zigbee.readAttribute(0x201, 0x0012),	//Read Heat Setpoint
                zigbee.readAttribute(0x201, 0x001C),	//Read System Mode
                my_readAttribute(0x201, 0x401C, ["mfgCode": "0x1185"]),	//Read Manufacturer Specific Setpoint Mode
                zigbee.readAttribute(0x204, 0x0000),	//Read Temperature Display Mode
                zigbee.readAttribute(0x204, 0x0001),	//Read Keypad Lockout
                sendEvent( name: 'change', value: 0 )
        ], 200)
    } else {
        delayBetween([
        		zigbee.readAttribute(0x201, 0x0000),	//Read Local Temperature
                zigbee.readAttribute(0x201, 0x0008),	//Read PI Heating State
    			zigbee.readAttribute(0x201, 0x0012),	//Read Heat Setpoint
                zigbee.readAttribute(0x201, 0x001C),	//Read System Mode
                my_readAttribute(0x201, 0x401C, ["mfgCode": "0x1185"]),	//Read Manufacturer Specific Setpoint Mode
                zigbee.readAttribute(0x204, 0x0000),	//Read Temperature Display Mode
                zigbee.readAttribute(0x204, 0x0001),	//Read Keypad Lockout
                sendEvent( name: 'change', value: 0 )
        ], 200)
    }
}

def getTemperature(value) {
	if (value != null) {
    	log.debug("value $value")
		def celsius = Integer.parseInt(value, 16) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		} else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

def quickSetHeat(degrees) {
    sendEvent( name: 'change', value: 1 )
    setHeatingSetpoint(degrees)
}

def setHeatingSetpoint(degrees) {
	if (degrees != null) {
		def temperatureScale = getTemperatureScale()
		def degreesInteger = Math.round(degrees)
        float tempfloat;
	    tempfloat = (Math.round(degrees.toFloat() * 2)) / 2

		log.debug "setHeatingSetpoint({$tempfloat} ${temperatureScale})"
		sendEvent("name": "heatingSetpoint", "value": tempfloat)
        def celsius = (getTemperatureScale() == "C") ? tempfloat : (fahrenheitToCelsius(tempfloat) as Float).round(2)
        delayBetween([
        	"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x12 0x29 {" + hex(celsius * 100) + "}",
        	zigbee.readAttribute(0x201, 0x12),	//Read Heat Setpoint
    	], 100)
	}
}

def setCoolingSetpoint(degrees) {
	if (degrees != null) {
		def degreesInteger = Math.round(degrees)
		log.debug "setCoolingSetpoint({$degreesInteger} ${temperatureScale})"
		sendEvent("name": "coolingSetpoint", "value": degreesInteger)
		def celsius = (getTemperatureScale() == "C") ? degreesInteger : (fahrenheitToCelsius(degreesInteger) as Double).round(2)
		"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x11 0x29 {" + hex(celsius * 100) + "}"
	}
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
    Integer tempToSend
    def tempToSendInString

    if (locationScale == "C")
    {
        deviceScale = 0
    }
    else
    {
        deviceScale = 1
    }

    if (degrees < 0)
    {
    	tempToSend = -degrees*100 - 65536
    	tempToSend = -tempToSend
    }
    else
    {
    	tempToSend = (degrees*100)
    }

    tempToSendInString = zigbee.convertToHexString(tempToSend, 4)
    my_writeAttribute(0x201, 0x4001, 0x29, tempToSendInString, ["mfgCode": "0x1185"])
}

def increaseHeatSetpoint()
{
    def currentMode = device.currentState("thermostatMode")?.value
    if (currentMode != "off")
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
}

def decreaseHeatSetpoint()
{
    def currentMode = device.currentState("thermostatMode")?.value
    if (currentMode != "off")
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
}

def modes() {
	["comfort", "eco", "off"]
}

def switchMode() {
    def currentMode = device.currentState("thermostatMode")?.value
    def lastTriedMode = state.lastTriedMode ?: currentMode ?: "comfort"
    def modeOrder = modes()
    def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(currentMode)
    def modeNumber;
    Integer setpointModeNumber;
    def modeToSendInString;

    if (nextMode == "comfort")
    {
    	modeNumber = 04
        setpointModeNumber = 04
    }
    else if (nextMode == "eco")
    {
    	modeNumber = 04
        setpointModeNumber = 05
    }
    else
    {
    	modeNumber = 00
        setpointModeNumber = 00
    }
    if (supportedModes?.contains(currentMode)) {
        while (!supportedModes.contains(nextMode) && nextMode != "comfort") {
            nextMode = next(nextMode)
        }
    }
    state.lastTriedMode = nextMode
	modeToSendInString = zigbee.convertToHexString(setpointModeNumber, 2)
    delayBetween([
    		"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x001C 0x30 {$modeNumber}",
            my_writeAttribute(0x201, 0x401C, 0x30, modeToSendInString, ["mfgCode": "0x1185"]),
            poll()
    ], 1000)
}

def setThermostatMode() {
	log.debug "switching thermostatMode"
	def currentMode = device.currentState("thermostatMode")?.value
	def modeOrder = modes()
	def index = modeOrder.indexOf(currentMode)
	def next = index >= 0 && index < modeOrder.size() - 1 ? modeOrder[index + 1] : modeOrder[0]
	log.debug "switching mode from $currentMode to $next"
	"$next"()
}

def setThermostatFanMode() {
	log.debug "Switching fan mode"
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	log.debug "switching fan from current mode: $currentFanMode"
	def returnCommand

	switch (currentFanMode) {
		case "fanAuto":
			returnCommand = fanOn()
			break
		case "fanOn":
			returnCommand = fanAuto()
			break
	}
	if(!currentFanMode) { returnCommand = fanAuto() }
	returnCommand
}

def setThermostatMode(String value) {
	log.debug "setThermostatMode({$value})"
	"$value"()
}

def setThermostatFanMode(String value) {
	log.debug "setThermostatFanMode({$value})"
	"$value"()
}

def off() {
	log.debug "off"
	sendEvent("name":"thermostatMode", "value":"off")
	"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x1C 0x30 {00}"
}

def cool() {
	log.debug "cool"
	sendEvent("name":"thermostatMode", "value":"cool")
	"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x1C 0x30 {03}"
}

def heat() {
	log.debug "heat"
	sendEvent("name":"thermostatMode", "value":"heat")
	"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x1C 0x30 {04}"
}

def emergencyHeat() {
	log.debug "emergencyHeat"
	sendEvent("name":"thermostatMode", "value":"emergency heat")
	"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x1C 0x30 {05}"
}

def on() {
	fanOn()
}

def fanOn() {
	log.debug "fanOn"
	sendEvent("name":"thermostatFanMode", "value":"fanOn")
	"st wattr 0x${device.deviceNetworkId} 0x19 0x202 0 0x30 {04}"
}

def auto() {
	fanAuto()
}

def fanAuto() {
	log.debug "fanAuto"
	sendEvent("name":"thermostatFanMode", "value":"fanAuto")
	"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {05}"
}

def configure() {
	log.debug "binding to Thermostat cluster"
	delayBetween([
        "zdo bind 0x${device.deviceNetworkId} 1 0x19 0x201 {${device.zigbeeId}} {}",
        //Cluster ID (0x0201 = Thermostat Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
        zigbee.configureReporting(0x0201, 0x0000, 0x29, 10, 60, 50), 	//Attribute ID 0x0000 = local temperature, Data Type: S16BIT
    	zigbee.configureReporting(0x0201, 0x0012, 0x29, 1, 0, 50),  	//Attribute ID 0x0012 = occupied heat setpoint, Data Type: S16BIT
        zigbee.configureReporting(0x0201, 0x001C, 0x30, 1, 0, 1),   	//Attribute ID 0x001C = system mode, Data Type: 8 bits enum
        zigbee.configureReporting(0x0201, 0x401C, 0x30, 1, 0, 1),   	//Attribute ID 0x401C = manufacturer specific setpoint mode, Data Type: 8 bits enum
        zigbee.configureReporting(0x0201, 0x0008, 0x20, 300, 900, 5),   //Attribute ID 0x0008 = pi heating demand, Data Type: U8BIT
        
        //Cluster ID (0x0204 = Thermostat Ui Conf Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
        zigbee.configureReporting(0x0204, 0x0000, 0x30, 1, 0, 1),   //Attribute ID 0x0000 = temperature display mode, Data Type: 8 bits enum
    	zigbee.configureReporting(0x0204, 0x0001, 0x30, 1, 0, 1),   //Attribute ID 0x0001 = keypad lockout, Data Type: 8 bits enum
        
        //Read the configured variables
        zigbee.readAttribute(0x201, 0x0000),	//Read Local Temperature
    	zigbee.readAttribute(0x201, 0x0012),	//Read Heat Setpoint
        zigbee.readAttribute(0x201, 0x001C),	//Read System Mode
        my_readAttribute(0x201, 0x401C, ["mfgCode": "0x1185"]),	//Read Manufacturer Specific Setpoint Mode
        zigbee.readAttribute(0x201, 0x0008),	//Read PI Heating State
        zigbee.readAttribute(0x204, 0x0000),	//Read Temperature Display Mode
    	zigbee.readAttribute(0x204, 0x0001),	//Read Keypad Lockout
	], 200)
}

def updated() {
	log.debug "updated called"
    response(parameterSetting())
}

def parameterSetting() {
	def format = null
    def lockmode = null
    def valid_format = 0
    def valid_lock = 0

    log.info "unitformat : $settings.unitformat"
    if (settings.unitformat == "Celsius") {
        format = 0x00
        valid_format = 1
    }
    else if (settings.unitformat == "Fahrenheit") {
        format = 0x01
        valid_format = 1
    }

    log.info "lock : $settings.lock"
    if (settings.lock == "Yes") {
        lockmode = 0x01
        valid_lock = 1
    }
    else if (settings.lock == "No") {
        lockmode = 0x00
        valid_lock = 1
    }
    if ((valid_format == 1) && (valid_lock == 1))
    {
    	log.info "both valid"
        delayBetween([
            zigbee.writeAttribute(0x204, 0x00, 0x30, format),	//Write Unit Format Mode
            zigbee.writeAttribute(0x204, 0x01, 0x30, lockmode),	//Write Lock Mode
            poll(),
        ], 200)
    }
    else if (valid_format == 1)
    {
    	log.info "format valid"
        delayBetween([
            zigbee.writeAttribute(0x204, 0x00, 0x30, format),	//Write Unit Format Mode
            poll(),
        ], 200)
    }
	else if (valid_lock == 1)
    {
    	log.info "lock valid"
        delayBetween([
            zigbee.writeAttribute(0x204, 0x01, 0x30, lockmode),	//Write Lock Mode
            poll(),
        ], 200)
    }
    else
    {
    	log.info "nothing valid"
    }
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private getEndpointId() {
    new BigInteger(device.endpointId, 16).toString()
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}

def my_readAttribute(cluster, attributeId, Map additional=null)
{
    if (additional?.get("mfgCode")) {
        [ "zcl mfg-code ${additional['mfgCode']}", "delay 200",
          "zcl global read ${cluster} ${attributeId}", "delay 200",
          "send 0x${device.deviceNetworkId} 1 ${endpointId}" ] 
    } else {
        zigbee.readAttribute(cluster, attributeId)
    }
}

def my_writeAttribute(cluster, attributeId, dataType, value, Map additional=null)
{
	value = swapEndianHex(value)
    if (additional ?.get("mfgCode")) {
        [ "zcl mfg-code ${additional['mfgCode']}", "delay 200",
          "zcl global write ${cluster} ${attributeId} ${dataType} {${value}}", "delay 200",
          "send 0x${device.deviceNetworkId} 1 ${endpointId}" ]
    } else {
        zigbee.writeAttribute(cluster, attributeId, dataType, value)
    }
}

