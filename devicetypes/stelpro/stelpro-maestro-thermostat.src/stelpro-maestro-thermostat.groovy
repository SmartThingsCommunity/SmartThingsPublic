/**
 *  Copyright 2018 Stelpro
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
 *  Stelpro Maestro Thermostat
 *
 *  Author: Stelpro
 *
 *  Date: 2018-04-05
 */
 
preferences {
            input("lock", "enum", title: "Do you want to lock your thermostat's physical keypad?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: false)
            input("heatdetails", "enum", title: "Do you want a detailed operating state notification?", options: ["No", "Yes"], defaultValue: "No", required: false, displayDuringSetup: true)
    		input("zipcode", "text", title: "ZipCode (Outdoor Temperature)", description: "[Do not use space](Blank = No Forecast)")
            /*input("away_setpoint", "enum", title: "Away setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "21", required: true)
            input("away_setpoint", "enum", title: "Away Setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "17", required: true)
            input("vacation_setpoint", "enum", title: "Vacation Setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "13", required: true)
            input("standby_setpoint", "enum", title: "Standby Setpoint", options: ["5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "5.5", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30"], defaultValue: "5", required: true)
            */
            
}

metadata {
	definition (name: "Stelpro Maestro Thermostat", namespace: "Stelpro", author: "Stelpro") {
        capability "Actuator"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Thermostat"
		capability "Configuration"
        capability "Polling"
        capability "Sensor"
		capability "Refresh"
        
        attribute "outsideTemp", "number"
        attribute "humidity", "number"
        //attribute "setpoint", "number"
        
		command "switchMode"
        command "quickSetHeat"
        command "quickSetOutTemp"
        command "increaseHeatSetpoint"
        command "decreaseHeatSetpoint"
        command "parameterSetting"
        command "setCustomThermostatMode"
        command "updateWeather"

	fingerprint profileId: "0104", endpointId: "19", inClusters: " 0000,0003,0201,0204,0405", outClusters: "0402"
}
    
	// simulator metadata
	simulator { }

	tiles(scale : 2) {
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4, canChangeIcon: true) {
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
			}/*
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("home", label:'${name}')
				attributeState("away", label:'${name}')
                attributeState("vacation", label:'${name}')
				attributeState("standby", label:'${name}')
			}*/
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
            	attributeState("heatingSetpoint", label:'${currentValue}', defaultState: true)
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("humidity", label:'${currentValue}%', unit:"%", defaultState: true)
            }
		}
        
        /*
        standardTile("mode", "device.thermostatMode", width: 2, height: 2) {
            state "home", label:'${name}', action:"switchMode", nextState:"away", icon:"http://cdn.device-icons.smartthings.com/Home/home2-icn@2x.png"
			state "away", label:'${name}', action:"switchMode", nextState:"vacation", icon:"http://cdn.device-icons.smartthings.com/Home/home15-icn@2x.png"
			state "vacation", label:'${name}', action:"switchMode", nextState:"standby", icon:"http://cdn.device-icons.smartthings.com/Transportation/transportation2-icn@2x.png"
            state "standby", label:'${name}', action:"switchMode", nextState:"home"
        }*/
        
		valueTile("humidity", "device.humidity", width: 2, height: 2) {
            state "humidity", label:'Humidity ${currentValue}%', backgroundColor:"#4286f4", defaultState: true
        }
        
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
            state "temperature", label:'Setpoint ${currentValue}', unit:"dF", backgroundColors:[
                    [value: 67, color: "#45ea1c"],
                    [value: 68, color: "#94fc1e"],
                    [value: 69, color: "#cbed25"],
                    [value: 70, color: "#edd044"],
                    [value: 71, color: "#edaf44"],
                    [value: 75, color: "#bc2323"]
            ]
            state "--", label:'--', backgroundColor:"#bdbdbd"
        }
        standardTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

        main ("thermostatMulti")
        details(["thermostatMulti", "humidity", "heatingSetpoint", "refresh", "configure"])
    }
}

def parse(String description) {
	return parseCalled(description)
}

def parseCalled(String description) {
	log.debug "Parse description $description"
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		log.debug "Desc Map: $descMap"
		if (descMap.cluster == "0201" && descMap.attrId == "0000")
        {
			map.name = "temperature"
			map.value = getTemperature(descMap.value)
            if (descMap.value == "7ffd") {		//0x7FFD
                map.value = "low"
            }
            else if (descMap.value == "7fff") {	//0x7FFF
                map.value = "high"
            }
            else if (descMap.value == "8000") {	//0x8000
                map.value = "--"
            }
            
            else if (descMap.value > "8000") {
                map.value = -(Math.round(2*(655.36 - map.value))/2)
            }
                        
            sendEvent(name:"temperature", value:map.value)
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "0012") {
			log.debug "HEATING SETPOINT"
			map.name = "heatingSetpoint"
			map.value = getTemperature(descMap.value)
            if (descMap.value == "8000") {		//0x8000
                map.value = "--"
            }
            sendEvent(name:"heatingSetpoint", value:map.value)
		}
       /* else if (descMap.cluster == "0201" && descMap.attrId == "001c") {
        	if (descMap.value.size() == 8) {
				log.debug "MODE"
				map.name = "thermostatMode"
				map.value = getModeMap()[descMap.value]
				sendEvent(name:"thermostatMode", value:map.value)
            }
            else if (descMap.value.size() == 10) {
            	log.debug "MODE & SETPOINT MODE"
                def twoModesAttributes = descMap.value[0..-9]
                map.name = "thermostatMode"
				map.value = getModeMap()[twoModesAttributes]
				sendEvent(name:"thermostatMode", value:map.value)
            }
		}
        else if (descMap.cluster == "0201" && descMap.attrId == "401c") {
            log.debug "SETPOINT MODE"
            log.debug "descMap.value $descMap.value"
            map.name = "thermostatMode"
            map.value = getModeMap()[descMap.value]
            sendEvent(name:"thermostatMode", value:map.value)
		}*/
        else if (descMap.cluster == "0201" && descMap.attrId == "0008") {
        	log.debug "HEAT DEMAND"
            map.name = "thermostatOperatingState"
            if (descMap.value < "10") {
            	map.value = "idle"
            }
            else {
            	map.value = "heating"
            }
            sendEvent(name:"thermostatOperatingState", value:map.value)
            if (settings.heatdetails == "No") {
    			map.displayed = false
    		}
        }
	}
    else if(description?.startsWith("humidity")) {
    	log.debug "DEVICE HUMIDITY"
        map.name = "humidity"
        map.value = (description - "humidity: " - "%").trim()
        sendEvent(name:"humidity", value:map.value)
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

def updateWeather() {
	log.info "updating weather"
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
        
        def tempToSend
		
        if(getTemperatureScale() == "C" ) {
        	tempToSend = weather.current_observation.temp_c
            log.debug( "Outdoor Temperature: ${tempToSend} C" )
        }
        else {
        	tempToSend = weather.current_observation.temp_f
            log.debug( "Outdoor Temperature: ${tempToSend} F" )
        }       
        sendEvent( name: 'outsideTemp', value: tempToSend )
        quickSetOutTemp(tempToSend)
    }
}

def poll() {
    return pollCalled()
}

def pollCalled() {
    delayBetween([
    			updateWeather(),
                zigbee.readAttribute(0x201, 0x0000),	//Read Local Temperature
                zigbee.readAttribute(0x201, 0x0008),	//Read PI Heating State
    			zigbee.readAttribute(0x201, 0x0012),	//Read Heat Setpoint
                zigbee.readAttribute(0x204, 0x0000),	//Read Temperature Display Mode
                zigbee.readAttribute(0x204, 0x0001),	//Read Keypad Lockout
                zigbee.readAttribute(0x405, 0x0000),	//Read Local Humidity
                sendEvent( name: 'change', value: 0 )
        ], 200)
}



def getTemperature(value) {
	if (value != null) {
    	log.debug("value $value")
		def celsius = Integer.parseInt(value, 16) / 100
		if (getTemperatureScale() == "C") {
			return celsius
		}
        else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

def refresh() {
    poll()
}

def quickSetHeat(degrees) {
    sendEvent( name: 'change', value: 1 )
    setHeatingSetpoint(degrees, 0)
}

def setHeatingSetpoint(preciseDegrees, delay = 0) {
	if (preciseDegrees != null) {
		def temperatureScale = getTemperatureScale()
		def degrees = new BigDecimal(preciseDegrees).setScale(1, BigDecimal.ROUND_HALF_UP)

		log.debug "setHeatingSetpoint({$degrees} ${temperatureScale})"
        
        sendEvent(name: "heatingSetpoint", value: degrees, unit: temperatureScale)
    	sendEvent(name: "thermostatSetpoint", value: degrees, unit: temperatureScale)
        
        def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)
        delayBetween([
        	zigbee.writeAttribute(0x201, 0x12, 0x29, hex(celsius * 100)),
        	zigbee.readAttribute(0x201, 0x12),	//Read Heat Setpoint
            zigbee.readAttribute(0x201, 0x08),	//Read PI Heat demand
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
    def p = (state.precision == null) ? 1 : state.precision
    Integer tempToSend
    def tempToSendInString
    
    def celsius = (getTemperatureScale() == "C") ? degrees : (fahrenheitToCelsius(degrees) as Float).round(2)

    if (celsius < 0) {
        tempToSend = (celsius*100) + 65536
    }
    else {
    	tempToSend = (celsius*100)
    }
    tempToSendInString = zigbee.convertToHexString(tempToSend, 4)
    my_writeAttribute(0x201, 0x4001, 0x29, tempToSendInString, ["mfgCode": "0x1185"])
}

def increaseHeatSetpoint() {
    float currentSetpoint = device.currentValue("heatingSetpoint")
    def locationScale = getTemperatureScale()
    float maxSetpoint
    float step

    if (locationScale == "C") {
        maxSetpoint = 30;
        step = 0.5
    }
    else {
        maxSetpoint = 86
        step = 1
    }

    if (currentSetpoint < maxSetpoint) {
        currentSetpoint = currentSetpoint + step
        quickSetHeat(currentSetpoint)
    }
}

def decreaseHeatSetpoint() {
	float currentSetpoint = device.currentValue("heatingSetpoint")
    def locationScale = getTemperatureScale()
    float minSetpoint
    float step

    if (locationScale == "C") {
        minSetpoint = 5;
        step = 0.5
    }
    else {
        minSetpoint = 41
        step = 1
    }

    if (currentSetpoint > minSetpoint) {
        currentSetpoint = currentSetpoint - step
        quickSetHeat(currentSetpoint)
    }
}
/*
def modes() {
	["home", "away", "vacation", "standby"]
}*/


def setThermostatMode() {
    /*log.debug "switching thermostatMode"
	def currentMode = device.currentState("thermostatMode")?.value
	def modeOrder = modes()
	def index = modeOrder.indexOf(currentMode)
	def next = index >= 0 && index < modeOrder.size() - 1 ? modeOrder[index + 1] : modeOrder[0]
	log.debug "switching mode from $currentMode to $next"
	"$next"()*/
}

def setThermostatMode(def value) {
	log.debug "setThermostatMode($value)"
	/*def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "heat"
	def modeNumber;
	Integer setpointModeNumber;
	def modeToSendInString;*/
	if (value == "heat") {
    	on()
    }
    else if (value == "cool") {
    	off()
    }
    else if (value == "on") {
    	on()
    }
    else if (value == "off") {
    	off()
    }
    else {
    	log.debug "MODE NOT SUPPORTED"
    }
    /*if (supportedModes?.contains(currentMode)) {
        while (!supportedModes.contains(value) && value != "heat") {
            value = next(value)
        }
    }
    state.lastTriedMode = value
	modeToSendInString = zigbee.convertToHexString(setpointModeNumber, 2)
    delayBetween([
    		"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x001C 0x30 {$modeNumber}",
            //my_writeAttribute(0x201, 0x401C, 0x30, modeToSendInString, ["mfgCode": "0x1185"]),
            poll()
    ], 1000)*/
}

def on() {
	log.debug "on"
	sendEvent("name":"thermostatMode", "value":"heat")
}

def off() {
	log.debug "off"
	sendEvent("name":"thermostatMode", "value":"off")
    //quickSetHeat(5)
}

def cool() {
	log.debug "cool"
	sendEvent("name":"thermostatMode", "value":"off")
}

def heat() {
	log.debug "heat"
	sendEvent("name":"thermostatMode", "value":"heat")
}

def emergencyHeat() {
	log.debug "emergencyHeat"
	sendEvent("name":"thermostatMode", "value":"emergency heat")
	//"st wattr 0x${device.deviceNetworkId} 0x19 0x201 0x1C 0x30 {05}"
}

def setCustomThermostatMode(mode) {
   setThermostatMode(mode)
}
/*
def on() {
	//fanOn()
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
	//"st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {05}"
}
*/
def configure() {
	log.debug "binding to Thermostat cluster"
	delayBetween([
    	sendEvent("name":"thermostatMode", "value":"heat"),
        "zdo bind 0x${device.deviceNetworkId} 1 0x19 0x201 {${device.zigbeeId}} {}",
        //Cluster ID (0x0201 = Thermostat Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
        zigbee.configureReporting(0x0201, 0x0000, 0x29, 10, 60, 50), 	//Attribute ID 0x0000 = local temperature, Data Type: S16BIT
    	zigbee.configureReporting(0x0201, 0x0012, 0x29, 1, 0, 50),  	//Attribute ID 0x0012 = occupied heat setpoint, Data Type: S16BIT
        zigbee.configureReporting(0x0201, 0x0008, 0x20, 300, 900, 5),   //Attribute ID 0x0008 = pi heating demand, Data Type: U8BIT
        
        //Cluster ID (0x0204 = Thermostat Ui Conf Cluster), Attribute ID, Data Type, Payload (Min report, Max report, On change trigger)
        zigbee.configureReporting(0x0204, 0x0000, 0x30, 1, 0, 1),   //Attribute ID 0x0000 = temperature display mode, Data Type: 8 bits enum
    	zigbee.configureReporting(0x0204, 0x0001, 0x30, 1, 0, 1),   //Attribute ID 0x0001 = keypad lockout, Data Type: 8 bits enum
        
        zigbee.configureReporting(0x0405, 0x0000, 0x21, 10, 300, 1), 		//Attribute ID 0x0000 = local humidity, Data Type: U16BIT
        
        //Read the configured variables
        zigbee.readAttribute(0x201, 0x0000),	//Read Local Temperature
    	zigbee.readAttribute(0x201, 0x0012),	//Read Heat Setpoint
        zigbee.readAttribute(0x201, 0x0008),	//Read PI Heating State
        zigbee.readAttribute(0x204, 0x0000),	//Read Temperature Display Mode
    	zigbee.readAttribute(0x204, 0x0001),	//Read Keypad Lockout
        zigbee.readAttribute(0x405, 0x0000),	//Read Local Humidity
	], 200)
}

def updated() {
    response(parameterSetting())
}

def parameterSetting() {
    def lockmode = null
    def valid_lock = 0

    log.info "lock : $settings.lock"
    if (settings.lock == "Yes") {
        lockmode = 0x01
        valid_lock = 1
    }
    else if (settings.lock == "No") {
        lockmode = 0x00
        valid_lock = 1
    }
    
    if (valid_lock == 1)
    {
    	log.info "lock valid"
        delayBetween([
            zigbee.writeAttribute(0x204, 0x01, 0x30, lockmode),	//Write Lock Mode
            poll(),
        ], 200)
    }
    else {
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
    	delayBetween([
        		"zcl mfg-code ${additional['mfgCode']}",
        		"zcl global read ${cluster} ${attributeId}",
        		"send 0x${device.deviceNetworkId} 1 ${endpointId}"
            ], 200)
        log.info "send 0x${device.deviceNetworkId} 1 ${endpointId}"
    }
    else {
        zigbee.readAttribute(cluster, attributeId)
    }
}

def my_writeAttribute(cluster, attributeId, dataType, value, Map additional=null)
{
	value = swapEndianHex(value)
    if (additional?.get("mfgCode")) {
    	delayBetween([
        		"zcl mfg-code ${additional['mfgCode']}",
        		"zcl global write ${cluster} ${attributeId} ${dataType} {${value}}",
        		"send 0x${device.deviceNetworkId} 1 ${endpointId}"
            ], 200)
          
    }
    else {
        zigbee.writeAttribute(cluster, attributeId, dataType, value)
    }
}
