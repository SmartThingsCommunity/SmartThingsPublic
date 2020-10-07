/**
 *  Copyright 2015 SmartThings
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
    definition (name: "Sihas USM-300Zrev2", namespace: "shinasys", author: "leewonsoo") {
    /*
        capability "Motion Sensor"
        capability "Temperature Measurement"	
        capability "Relative Humidity Measurement"
        capability "Health Check"
        capability "Sensor"
        capability "Illuminance Measurement"
        capability "Battery"

        attribute "lastCheckin", "String"
        attribute "lastCheckinDate", "String"
        attribute "maxTemp", "number"
        attribute "minTemp", "number"
        attribute "maxHumidity", "number"
        attribute "minHumidity", "number"
        attribute "multiAttributesReport", "String"
        attribute "currentDay", "String"
        attribute "batteryRuntime", "String"
*/
capability "Configuration"
capability "Motion Sensor"
 capability "Illuminance Measurement"
 capability "Battery"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Health Check"

        fingerprint profileId: "0104", deviceId: "0001", inClusters: "0000,0001,0003,0004,0020,0402,0406,0500,0B05", outClusters: "0003,0004,0019,1000", manufacturer: "ShinaSystem", model: "SiHAS-OSM300Z2", deviceJoinName: "SiHAS-OSM300Z2"
        fingerprint profileId: "0104", deviceId: "0001", inClusters: "0000,0001,0003,0004,0020,0400,0402,0406,0500,0B05", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "USM-300Zrev2", deviceJoinName: "SiHAS USM-300Zrev2"
        command "resetBatteryRuntime"
  
	}

    // simulator metadata
   simulator {
		status "active": "motion:active"
		status "inactive": "motion:inactive"
	}
/*
    tiles(scale: 2) {
   	 
    	multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
				attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
			}
		}
       
        multiAttributeTile(name:"temperature", type:"generic", width:6, height:4) {
            tileAttribute("device.temperature", key:"PRIMARY_CONTROL"){
                attributeState("temperature", label:'${currentValue}°',
					backgroundColors:[
 						[value: 31, color: "#153591"],
 						[value: 44, color: "#1e9cbb"],
 						[value: 59, color: "#90d2a7"],
 						[value: 74, color: "#44b621"],
 						[value: 84, color: "#f1d801"],
 						[value: 95, color: "#d04e00"],
 						[value: 96, color: "#bc2323"]
 					]
                )
            }
            tileAttribute("device.multiAttributesReport", key: "SECONDARY_CONTROL") {
                attributeState("multiAttributesReport", label:'${currentValue}' //icon:"st.Weather.weather12",
                )
            }
        }
       
        valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
            state "humidity", label:'${currentValue}%', unit:"%",
            backgroundColors:[
                [value: 0, color: "#FFFCDF"],
                [value: 4, color: "#FDF789"],
                [value: 20, color: "#A5CF63"],
                [value: 23, color: "#6FBD7F"],
                [value: 56, color: "#4CA98C"],
                [value: 59, color: "#0072BB"],
                [value: 76, color: "#085396"]
            ]
        }

       
        valueTile("illuminance", "device.illuminance", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "illuminance", label: '${currentValue} lux', backgroundColors: [
					[value: 40, color: "#999900"],
					[value: 100, color: "#CCCC00"],
					[value: 300, color: "#FFFF00"],
					[value: 500, color: "#FFFF33"],
					[value: 1000, color: "#FFFF66"],
					[value: 2000, color: "#FFFF99"],
					[value: 10000, color: "#FFFFCC"]
			]
		}
        
        
        valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2) {
            state "battery", label:'${currentValue}%', unit:"%",
            backgroundColors:[
                [value: 10, color: "#bc2323"],
                [value: 26, color: "#f1d801"],
                [value: 51, color: "#44b621"]
            ]
        }
        valueTile("spacer1", "spacer1", decoration: "flat", inactiveLabel: false, width: 1, height: 1) {
	    	state "default", label:'spacer1'
        }	
        valueTile("spacer2", "spacer2", decoration: "flat", inactiveLabel: false, width: 1, height: 2) {
	    	state "default", label:'spacer2'
        }
        valueTile("lastcheckin", "device.lastCheckin", inactiveLabel: false, decoration:"flat", width: 4, height: 1) {
            state "lastcheckin", label:'Last Event:\n ${currentValue}'
        }
        valueTile("batteryRuntime", "device.batteryRuntime", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "batteryRuntime", label:'Battery Changed: ${currentValue}'
        }
       
        main("motion")
        details(["motion", "temperature", "spacer2", "humidity", "spacer2", "illuminance", "spacer1", "battery", "spacer1", "lastcheckin",  "spacer1", "spacer1", "batteryRuntime", "spacer1"])
        //details(["motion", "temperature",  "humidity", "illuminance", "battery" ])
		//details(["motion", "temperature", "spacer2", "humidity", "spacer2", "battery", "spacer1", "lastcheckin",  "spacer1", "spacer1", "batteryRuntime", "spacer1"])
    }
    preferences {
		//Button Config
		input description: "The settings below customize additional infomation displayed in the main status tile.", type: "paragraph", element: "paragraph", title: "MAIN TILE1 DISPLAY"
		input name: "displayTempInteger", type: "bool", title: "Display temperature as integer?", description:"NOTE: Takes effect on the next temperature report. High/Low temperatures are always displayed as integers."
		input name: "displayTempHighLow", type: "bool", title: "Display high/low temperature?"
		input name: "displayHumidHighLow", type: "bool", title: "Display high/low humidity?"
		//Temp and Humidity Offsets
		input description: "The settings below allow correction of variations in temperature and humidity by setting an offset. Examples: If the sensor consistently reports temperature 5 degrees too warm, enter '-5' for the Temperature Offset. If it reports humidity 3% too low, enter ‘3' for the Humidity Offset. NOTE: Changes will take effect on the NEXT temperature / humidity / pressure report.", type: "paragraph", element: "paragraph", title: "OFFSETS & UNITS"
		input "tempOffset", "decimal", title:"Temperature Offset", description:"Adjust temperature by this many degrees", range:"*..*"
		input "humidOffset", "number", title:"Humidity Offset", description:"Adjust humidity by this many percent", range: "*..*"
		input description: "NOTE: The temperature unit (C / F) can be changed in the location settings for your hub.", type: "paragraph", element: "paragraph", title: ""
		//Date & Time Config
		input description: "", type: "paragraph", element: "paragraph", title: "DATE & CLOCK"
		input name: "dateformat", type: "enum", title: "Set Date Format\n US (MDY) - UK (DMY) - Other (YMD)", description: "Date Format", options:["US","UK","Other"]
		input name: "clockformat", type: "bool", title: "Use 24 hour clock?"
		//Battery Reset Config
		input description: "If you have installed a new battery, the toggle below will reset the Changed Battery date to help remember when it was changed.", type: "paragraph", element: "paragraph", title: "CHANGED BATTERY DATE RESET"
		input name: "battReset", type: "bool", title: "Battery Changed?", description: ""
		//Battery Voltage Offset
		input description: "Only change the settings below if you know what you're doing.", type: "paragraph", element: "paragraph", title: "ADVANCED SETTINGS"
		input name: "voltsmax", title: "Max Volts\nA battery is at 100% at __ volts.\nRange 2.8 to 3.4", type: "decimal", range: "2.8..3.4", defaultValue: 3
		input name: "voltsmin", title: "Min Volts\nA battery is at 0% (needs replacing)\nat __ volts.  Range 2.0 to 2.7", type: "decimal", range: "2..2.7", defaultValue: 2.5
	}
    */
    preferences {
        input "tempOffset", "number", title: "Temperature offset", description: "Select how many degrees to adjust the temperature.", range: "-100..100", displayDuringSetup: false
        input "humidityOffset", "number", title: "Humidity offset", description: "Enter a percentage to adjust the humidity.", range: "*..*", displayDuringSetup: false
    }

    tiles {
        multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState "temperature", label: '${currentValue}°',
                        backgroundColors: [
                                [value: 31, color: "#153591"],
                                [value: 44, color: "#1e9cbb"],
                                [value: 59, color: "#90d2a7"],
                                [value: 74, color: "#44b621"],
                                [value: 84, color: "#f1d801"],
                                [value: 95, color: "#d04e00"],
                                [value: 96, color: "#bc2323"]
                        ]
            }
        }
        valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
            state "humidity", label: '${currentValue}% humidity', unit: ""
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "temperature", "humidity"
        details(["temperature", "humidity", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "${device.displayName}: Parsing description: ${description}"

	// Determine current time and date in the user-selected date format and clock style
    def now = formatDate()
    def nowDate = new Date(now).getTime()

	// Any report - temp, humidity, pressure, & battery - results in a lastCheckin event and update to Last Checkin tile
	// However, only a non-parseable report results in lastCheckin being displayed in events log
    //sendEvent(name: "lastCheckin", value: now, displayed: false)
    //sendEvent(name: "lastCheckinDate", value: nowDate, displayed: false)
	//sendEvent(name: "atest", value: "3")
	// Check if the min/max temp and min/max humidity should be reset
    checkNewDay(now)

	// getEvent automatically retrieves temp and humidity in correct unit as integer
	Map map = zigbee.getEvent(description)

	// Send message data to appropriate parsing function based on the type of report
	if (map.name == "temperature") {
        def temp = parseTemperature(description)
		map.value = displayTempInteger ? (int) temp : temp
		map.descriptionText = "${device.displayName} temperature is ${map.value}°${temperatureScale}"
		map.translatable = true
		updateMinMaxTemps(map.value)
	} else if (map.name == "humidity") {
		map.value = humidOffset ? (int) map.value + (int) humidOffset : (int) map.value
		updateMinMaxHumidity(map.value)
	} else if (description?.startsWith('catchall:')) {
		//map = parseCatchAllMessage(description)
        parseCatchAllMessage(description)
        map = null
	} else if (description?.startsWith('read attr - raw:')) {
		map = parseReadAttr(description)
	} else {
		log.debug "${device.displayName}: was unable to parse ${description}"
        sendEvent(name: "lastCheckin", value: now)
	}

	if (map) {
		log.debug "${device.displayName}: Parse returned ${map}"
		return createEvent(map)
	} else
		return [:]
}

// Calculate temperature with 0.1 precision in C or F unit as set by hub location settings
private parseTemperature(String description) {
	def temp = ((description - "temperature: ").trim()) as Float
	def offset = tempOffset ? tempOffset : 0
	temp = (temp > 100) ? (100 - temp) : temp
    temp = (temperatureScale == "F") ? ((temp * 1.8) + 32) + offset : temp + offset
	return temp.round(1)
}

// Check catchall for battery voltage data to pass to getBatteryResult for conversion to percentage report
private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def catchall = zigbee.parse(description)
	log.debug catchall

	if (catchall.clusterId == 0x0000 ) {
		def MsgLength = catchall.data.size()
		
        //usm parsing 
        if (catchall.data.get(0) == 0x84 && catchall.data.get(1) == 0xff && (catchall.data.get(2) == 0x41 || catchall.data.get(2) == 0x42) && catchall.data.get(3) == 0x0C/*LENGHT*/ && catchall.data.get(4) == 0x07/*USM*/) {
        	def event = (catchall.data.get(4+7)>>4)&0x0f;
            event = event>2 ? event-2:event
            //log.debug "event= $event"
            resultMap = getMotionResult(event==2?"active":"inactive")
            log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
			
            resultMap = getBatteryResult(catchall.data.get(4+6))
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
            resultMap = getTempResult((catchall.data.get(4+9) <<1) | (catchall.data.get(4+10)>>7 & 0x01) )
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
            resultMap = getHumiResult((catchall.data.get(4+10) & 0x7f)  )
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
            resultMap = getIllumiResult(((catchall.data.get(4+7) & 0x0f )<<8) | (catchall.data.get(4+8)) )
			log.info resultMap
            if(resultMap) sendEvent(name: resultMap?.name, value: resultMap?.value) //createEvent(map)
        }
	}
	return resultMap
}

// Parse device name on short press of reset button
private Map parseReadAttr(String description) {
    Map resultMap = [:]

    def cluster = description.split(",").find {it.split(":")[0].trim() == "cluster"}?.split(":")[1].trim()
    def attrId = description.split(",").find {it.split(":")[0].trim() == "attrId"}?.split(":")[1].trim()
    def value = description.split(",").find {it.split(":")[0].trim() == "value"}?.split(":")[1].trim()

    if (cluster == "0000" && attrId == "0005")  {
        def modelName = ""

        // Parsing the model name
        for (int i = 0; i < value.length(); i+=2) {
            def str = value.substring(i, i+2);
            def NextChar = (char)Integer.parseInt(str, 16);
            modelName = modelName + NextChar
        }
        log.debug "${device.displayName} reported: cluster: ${cluster}, attrId: ${attrId}, value: ${value}, model:${modelName}"
    }
}
private Map getMotionResult(mValue) {
	
	String descriptionTextx = (mValue == 'active') ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
    //log.debug "motionx= ${mValue}"
	//log.debug descriptionTextx
    def result = [
        name: 'motion',
        value: mValue,
        translatable   : true,
        descriptionText : "${device.displayName} ${mValue}"
    ]

    return result
    
}
// Convert raw 4 digit integer voltage value into percentage based on minVolts/maxVolts range
private Map getBatteryResult(rawValue) {
    // raw voltage is normally supplied as a 4 digit integer that needs to be divided by 1000
    // but in the case the final zero is dropped then divide by 100 to get actual voltage value
    //def rawVolts = rawValue / 1000
    def rawVolts = (rawValue + 100)/ 100
    def minVolts
    def maxVolts
	//log.debug "batt = $rawValue"
    if(voltsmin == null || voltsmin == "")
    	minVolts = 2.5
    else
   		minVolts = voltsmin

    if(voltsmax == null || voltsmax == "")
    	maxVolts = 3.5
    else
		maxVolts = voltsmax

    def pct = (rawVolts - minVolts) / (maxVolts - minVolts)
    def roundedPct = Math.min(100, Math.round(pct * 100))

    def result = [
        name: 'battery',
        value: roundedPct,
        unit: "%",
        isStateChange:true,
        descriptionText : "${device.displayName} Battery at ${roundedPct}% (${rawVolts} Volts)"
    ]

    return result
}

// Convert raw 4 digit integer voltage value into percentage based on minVolts/maxVolts range
private Map getTempResult(rawValue) {
   
    def tempval = rawValue/10;
    def offset = tempOffset ? tempOffset : 0
    //tempval = (temperatureScale == "F") ? ((tempval * 1.8) + 32) + offset : tempval + offset
    tempval = tempval + offset
    //tempval = tempval.round(1)
    //log.debug "temp = $rawValue"
    
    def result = [
        name: 'temperature',
        value: displayTempInteger ? (int) tempval : tempval,
        isStateChange:true,
        descriptionText : "${device.displayName} temperature is ${tempval}°C"
    ]

	updateMinMaxTemps(tempval)
  
    return result
}
// Convert raw 4 digit integer voltage value into percentage based on minVolts/maxVolts range
private Map getHumiResult(rawValue) {
   
    def humival = rawValue;
    //log.debug "humival = $rawValue"
    def result = [
        name: 'humidity',
        value: humival,
        isStateChange:true,
        descriptionText : "${device.displayName} humidity is ${humival}%"
    ]

		updateMinMaxHumidity(humival)
        
        
    return result
}
// Convert raw 4 digit integer voltage value into percentage based on minVolts/maxVolts range
private Map getIllumiResult(rawValue) {
   
    def illumival = rawValue;
    //log.debug "illumival = $rawValue"
    def result = [
        name: 'illuminance',
        value: illumival,
        isStateChange:true,
        descriptionText : "${device.displayName} illuminance is ${illumival}lux"
    ]

		//updateMinMaxHumidity(humival)
        
        
    return result
}
// If the day of month has changed from that of previous event, reset the daily min/max temp and humidity values
def checkNewDay(now) {
	def oldDay = ((device.currentValue("currentDay")) == null) ? "32" : (device.currentValue("currentDay"))
	def newDay = new Date(now).format("dd")
	if (newDay != oldDay) {
		resetMinMax()
		sendEvent(name: "currentDay", value: newDay, displayed: false)
	}
}

// Reset daily min/max temp and humidity values to the current temp/humidity values
def resetMinMax() {
	def currentTemp = device.currentValue('temperature')
	def currentHumidity = device.currentValue('humidity')
    currentTemp = currentTemp ? (int) currentTemp : currentTemp
	log.debug "${device.displayName}: Resetting daily min/max values to current temperature of ${currentTemp}° and humidity of ${currentHumidity}%"
    sendEvent(name: "maxTemp", value: currentTemp, displayed: false)
    sendEvent(name: "minTemp", value: currentTemp, displayed: false)
    sendEvent(name: "maxHumidity", value: currentHumidity, displayed: false)
    sendEvent(name: "minHumidity", value: currentHumidity, displayed: false)
    refreshMultiAttributes()
}

// Check new min or max temp for the day
def updateMinMaxTemps(temp) {
	temp = temp ? (int) temp : temp
	if ((temp > device.currentValue('maxTemp')) || (device.currentValue('maxTemp') == null))
		sendEvent(name: "maxTemp", value: temp, displayed: false)
	if ((temp < device.currentValue('minTemp')) || (device.currentValue('minTemp') == null))
		sendEvent(name: "minTemp", value: temp, displayed: false)
	refreshMultiAttributes()
}

// Check new min or max humidity for the day
def updateMinMaxHumidity(humidity) {
	if ((humidity > device.currentValue('maxHumidity')) || (device.currentValue('maxHumidity') == null))
		sendEvent(name: "maxHumidity", value: humidity, displayed: false)
	if ((humidity < device.currentValue('minHumidity')) || (device.currentValue('minHumidity') == null))
		sendEvent(name: "minHumidity", value: humidity, displayed: false)
	refreshMultiAttributes()
}

// Update display of multiattributes in main tile
def refreshMultiAttributes() {
	def temphiloAttributes = displayTempHighLow ? (displayHumidHighLow ? "Today's High/Low:  ${device.currentState('maxTemp')?.value}° / ${device.currentState('minTemp')?.value}°" : "Today's High: ${device.currentState('maxTemp')?.value}°  /  Low: ${device.currentState('minTemp')?.value}°") : ""
	def humidhiloAttributes = displayHumidHighLow ? (displayTempHighLow ? "    ${device.currentState('maxHumidity')?.value}% / ${device.currentState('minHumidity')?.value}%" : "Today's High: ${device.currentState('maxHumidity')?.value}%  /  Low: ${device.currentState('minHumidity')?.value}%") : ""
	sendEvent(name: "multiAttributesReport", value: "${temphiloAttributes}${humidhiloAttributes}", displayed: false)
    //log.info "refresh"
}

//Reset the date displayed in Battery Changed tile to current date
def resetBatteryRuntime(paired) {
	def now = formatDate(true)
	def newlyPaired = paired ? " for newly paired sensor" : ""
	sendEvent(name: "batteryRuntime", value: now)
	log.debug "${device.displayName}: Setting Battery Changed to current date${newlyPaired}"
}

// installed() runs just after a sensor is paired using the "Add a Thing" method in the SmartThings mobile app
def installed() {
	state.battery = 0
	if (!batteryRuntime) resetBatteryRuntime(true)
	checkIntervalEvent("installed")
}

// configure() runs after installed() when a sensor is paired
def configure() {
	log.debug "${device.displayName}: configuring"
		state.battery = 0
	if (!batteryRuntime) resetBatteryRuntime(true)
	checkIntervalEvent("configured")
	return
}

// updated() will run twice every time user presses save in preference settings page
def updated() {
		checkIntervalEvent("updated")
		if(battReset){
		resetBatteryRuntime()
		device.updateSetting("battReset", false)
	}
}

private checkIntervalEvent(text) {
    // Device wakes up every 1 hours, this interval allows us to miss one wakeup notification before marking offline
    log.debug "${device.displayName}: Configured health checkInterval when ${text}()"
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def formatDate(batteryReset) {
    def correctedTimezone = ""
    def timeString = clockformat ? "HH:mm:ss" : "h:mm:ss aa"

	// If user's hub timezone is not set, display error messages in log and events log, and set timezone to GMT to avoid errors
    if (!(location.timeZone)) {
        correctedTimezone = TimeZone.getTimeZone("GMT")
        log.error "${device.displayName}: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app."
        sendEvent(name: "error", value: "", descriptionText: "ERROR: Time Zone not set, so GMT was used. Please set up your location in the SmartThings mobile app.")
    }
    else {
        correctedTimezone = location.timeZone
    }

    if (dateformat == "US" || dateformat == "" || dateformat == null) {
        if (batteryReset)
            return new Date().format("MMM dd yyyy", correctedTimezone)
        else
            return new Date().format("EEE MMM dd yyyy ${timeString}", correctedTimezone)
    }
    else if (dateformat == "UK") {
        if (batteryReset)
            return new Date().format("dd MMM yyyy", correctedTimezone)
        else
            return new Date().format("EEE dd MMM yyyy ${timeString}", correctedTimezone)
        }
    else {
        if (batteryReset)
            return new Date().format("yyyy MMM dd", correctedTimezone)
        else
            return new Date().format("EEE yyyy MMM dd ${timeString}", correctedTimezone)
    }
}