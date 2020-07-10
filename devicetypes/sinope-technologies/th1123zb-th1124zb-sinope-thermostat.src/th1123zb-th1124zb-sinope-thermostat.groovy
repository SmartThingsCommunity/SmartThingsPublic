/**
Copyright Sinopé Technologies
1.3.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
**/
metadata {
	preferences {
		input("backlightAutoDimParam", "enum", title:"Backlight setting (Default: Always ON)", multiple: false, required: false, options: ["On Demand", "Always ON"],
			description: "On Demand or Always ON")
		input("disableOutdorTemperatureParam", "enum", title: "Secondary display (Default: Outside temp.)", multiple: false, required: false, options: ["Setpoint", "Outside temp."], 
			description: "Information displayed in the secondary zone of the device")
		input("keyboardLockParam", "enum", title: "Keypad lock (Default: Unlock)", multiple: false, required: false, options: ["Lock", "Unlock"], 
			description: "Enable or disable the device's buttons")   	
		input("timeFormatParam", "enum", title:"Time Format (Default: 24h)", options:["12h AM/PM","24h"], multiple: false, required: false,
			description: "Time format displayed by the device.") 	
		input("trace", "bool", title: "Trace", 
			description:"Set it to true to enable tracing")
	}
	definition(name: "TH1123ZB-TH1124ZB Sinope Thermostat", namespace: "Sinope Technologies", author: "Sinope Technologies", ocfDeviceType: "oic.d.thermostat") {
		capability "Temperature Measurement"
 		capability "Thermostat"
 		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
 		capability "Actuator"
 		capability "Configuration"
		capability "Refresh"
		capability "Health check" 
		capability "Sensor"

        attribute "heatingSetpointRangeHigh", "number"
        attribute "heatingSetpointRangeLow", "number"
        attribute "heatingSetpointRange", "VECTOR3"
		attribute "outdoorTemp", "number"
		attribute "temperatureUnit", "string"
        
		command "heatLevelUp"
		command "heatLevelDown"

        fingerprint manufacturer: "Sinope Technologies", model: "TH1123ZB", deviceJoinName: "Sinope Thermostat" //Sinope TH1123ZB Thermostat
            
        fingerprint manufacturer: "Sinope Technologies", model: "TH1124ZB", deviceJoinName: "Sinope Thermostat"  //Sinope TH1124ZB Thermostat

 	}
	simulator { }
	//--------------------------------------------------------------------------------------------------------
	tiles(scale: 2) {

		multiAttributeTile(name: "thermostatMulti", type: "thermostat", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label: '${currentValue}', unit: "dF", backgroundColor: "#269bd2")
			}
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "heatLevelUp")
				attributeState("VALUE_DOWN", action: "heatLevelDown")
			}
			tileAttribute("device.heatingDemand", key: "SECONDARY_CONTROL") {
				attributeState("default", label: '${currentValue}%', unit: "%", icon:"st.Weather.weather2")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor: "#44b621")
				attributeState("heating", backgroundColor: "#ffa81e")
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("heatingSetpoint", label: '${currentValue}°', unit:"dF", range: "(5..30)", defaultState: true)
			}
		}
		//-- Value Tiles -------------------------------------------------------------------------------------------

		valueTile("heatingDemand", "device.heatingDemand", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "heatingDemand", label: '${currentValue}%', unit: "%", backgroundColor: "#ffffff"
		}

		//-- Standard Tiles ----------------------------------------------------------------------------------------

		standardTile("mode", "device.thermostatMode", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
			state "off", label: '', action: "heat", icon: "st.thermostat.heating-cooling-off"
			state "heat", label: '', action: "off", icon: "st.thermostat.heat", defaultState: true
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}


        controlTile("heatingSetpoint", "device.heatingSetpoint", "slider",
        	sliderType: "HEATING",
            debouncePeriod: 1500,
            range: "device.heatingSetpointRange",
            width: 2, height: 2)
        {
            state "default", action:"setHeatingSetpoint", 
            	label:'${currentValue}${unit}', backgroundColor: "#E86D13"
        }

		//-- Main & Details ----------------------------------------------------------------------------------------

		main("thermostatMulti")
        details(["thermostatMulti", "heatingSetpoint", "mode", "refresh"])
	}
}

def getSupportedThermostatModes() {
	["heat", "off"]
}

def getHeatingSetpointRange() {
		(temperatureScale == "C") ? [5.0, 30.0] : [41, 86]
}
def getThermostatSetpointRange() {
		heatingSetpointRange
}


def getSetpointStep() {
	(getTemperatureScale() == "C") ? 0.5 : 1.0
}

def configureSupportedRanges() {
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
	
	sendEvent(name: "thermostatSetpointRange", value: heatingSetpointRange, scale: temperatureScale, displayed: false)
	
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, scale: temperatureScale, displayed: false)
	
}

//-- Installation ----------------------------------------------------------------------------------------


def installed() {
	traceEvent(settings.logFilter, "installed>Device is now Installed", settings.trace)
	initialize()
}


def updated() {
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 1000) {
		state.updatedLastRanAt = now() 

        traceEvent(settings.logFilter, "updated>Device is now updated", settings.trace)
        try {
            unschedule()
        } catch (e) {
            traceEvent(settings.logFilter, "updated>exception $e, continue processing", settings.trace, get_LOG_ERROR())
        }
        runIn(1,refresh_misc)
        runEvery15Minutes(refresh_misc)
    }
}

def configure()
{
    traceEvent(settings.logFilter, "Configuring Reporting and Bindings", settings.trace, get_LOG_DEBUG())

	def cmds = []
	
	cmds += zigbee.configureReporting(0x0201, 0x0000, 0x29, 19, 301, 50) 	//local temperature
	cmds += zigbee.configureReporting(0x0201, 0x0008, 0x0020, 4, 300, 10) 	//heating demand
	cmds += zigbee.configureReporting(0x0201, 0x0012, 0x0029, 15, 302, 40) 	//occupied heating setpoint

	
    if(cmds)
    {
        sendZigbeeCommands(cmds)
    }

	//allow 5 min without receiving temperature report
	return sendEvent(name: "checkInterval", value: 300, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

void initialize() {
	state?.scale = temperatureScale
    runIn(2,refresh)
	
	runEvery15Minutes(refresh_misc)

	def supportedThermostatModes = ['off', 'heat']
    state?.supportedThermostatModes = supportedThermostatModes
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes)
    configureSupportedRanges();
	
	refresh();
}

def ping() {
	// refresh()
	def cmds = zigbee.readAttribute(0x0201, 0x0000);
	sendZigbeeCommands(cmds)
}

def uninstalled() {
	unschedule()
}

//-- Parsing ---------------------------------------------------------------------------------------------

// parse events into attributes
def parse(String description) {
    def result = []
	def scale = state?.scale
	state?.scale = scale
	traceEvent(settings.logFilter, "parse>Description :( $description )", settings.trace)
	def cluster = zigbee.parse(description)
	traceEvent(settings.logFilter, "parse>Cluster : $cluster", settings.trace)
	if (description?.startsWith("read attr -") || description?.startsWith("write attr -")) {
    	def descMap = zigbee.parseDescriptionAsMap(description)
        result += createCustomMap(descMap)
        if(descMap.additionalAttrs){
       		def mapAdditionnalAttrs = descMap.additionalAttrs
            mapAdditionnalAttrs.each{add ->
            	traceEvent(settings.logFilter,"parse> mapAdditionnalAttributes : ( ${add} )",settings.trace)
                add.cluster = descMap.cluster
                result += createCustomMap(add)
            }
        }
    }
	traceEvent(settings.logFilter, "Parse returned $result", settings.trace)
	return result
}

//--------------------------------------------------------------------------------------------------------
def createCustomMap(descMap){
	def result = null
	def map = [: ]
	def scale = temperatureScale
	
 		if (descMap.cluster == "0201" && descMap.attrId == "0000") {
			map.name = "temperature"
			map.value = getTemperatureValue(descMap.value)
			map.unit = scale
			traceEvent(settings.logFilter, "parse>${map.name}:  ${map.value}", settings.trace)
			//allow 5 min without receiving temperature report
			sendEvent(name: "checkInterval", value: 300, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0008") {
			map.name = "heatingDemand"
			map.value = getHeatingDemand(descMap.value)
            traceEvent(settings.logFilter, "parse>${map.name}: ${map.value}")

			def operatingState = (map.value.toInteger() < 10) ? "idle" : "heating"
			sendEvent(name: "thermostatOperatingState", value: operatingState)
			traceEvent(settings.logFilter,"thermostatOperatingState: ${operatingState}", settings.trace)
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0012") {
			configureSupportedRanges();
            map.name = "heatingSetpoint"
            map.value = getTemperatureValue(descMap.value, true)
			map.unit = scale
            traceEvent(settings.logFilter, "parse>OCCUPY: ${map.name}: ${map.value}, scale: ${scale} ", settings.trace)
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0014") { // UnpccupiedHeatingSetpoint
			configureSupportedRanges();
            map.name = "heatingSetpoint"
            map.value = getTemperatureValue(descMap.value, true)
			map.unit = scale
            traceEvent(settings.logFilter, "parse>UNOCCUPY: ${map.name}: ${map.value}", settings.trace)
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "001c") {
			map.name = "thermostatMode"
			map.value = getModeMap()[descMap.value]
			traceEvent(settings.logFilter, "parse>${map.name}: ${map.value}", settings.trace)
		}
		else{
			result = "{cluster:"+descMap.cluster+", attrId:"+descMap.attrId+",value:"+descMap.value+"}";
		}
	if(map){
		result = createEvent(map);
	}
    return result
}
//-- Temperature -----------------------------------------------------------------------------------------

def getTemperatureValue(value, doRounding = false) {
	def scale = temperatureScale
	if (value != null) {
		double celsius = (Integer.parseInt(value, 16) / 100).toDouble()
		if (scale == "C") {
			if (doRounding) {
				def tempValueString = String.format('%2.1f', celsius)
				if (tempValueString.matches(".*([.,][456])")) {
					tempValueString = String.format('%2d.5', celsius.intValue())
					traceEvent(settings.logFilter, "getTemperatureValue>value of $tempValueString which ends with 456=> rounded to .5", settings.trace)
				} else if (tempValueString.matches(".*([.,][789])")) {
					traceEvent(settings.logFilter, "getTemperatureValue>value of$tempValueString which ends with 789=> rounded to next .0", settings.trace)
					celsius = celsius.intValue() + 1
					tempValueString = String.format('%2d.0', celsius.intValue())
				} else {
					traceEvent(settings.logFilter, "getTemperatureValue>value of $tempValueString which ends with 0123=> rounded to previous .0", settings.trace)
					tempValueString = String.format('%2d.0', celsius.intValue())
				}
				return tempValueString.toDouble().round(1)
			} else {
				return celsius.round(1)
			}

		} else {
			return Math.round(celsiusToFahrenheit(celsius))
		}
	}
}

//-- Heating Demand --------------------------------------------------------------------------------------

def getHeatingDemand(value) {
	if (value != null) {
		def demand = Integer.parseInt(value, 16)
		return demand.toString()
	}
}

//-- Heating Setpoint ------------------------------------------------------------------------------------

def heatLevelUp() {
	def scale = temperatureScale
	double nextLevel

	if (scale == 'C') {
		nextLevel = device.currentValue("heatingSetpoint").toDouble()
		nextLevel = (nextLevel + 0.5).round(1)
		nextLevel = checkTemperature(nextLevel)
		setHeatingSetpoint(nextLevel)
	} else {
		nextLevel = device.currentValue("heatingSetpoint")
		nextLevel = (nextLevel + 1)
		nextLevel = checkTemperature(nextLevel)
		setHeatingSetpoint(nextLevel.intValue())
	}

}

def heatLevelDown() {
	def scale = temperatureScale
	double nextLevel

	if (scale == 'C') {
		nextLevel = device.currentValue("heatingSetpoint").toDouble()
		nextLevel = (nextLevel - 0.5).round(1)
		nextLevel = checkTemperature(nextLevel)
		setHeatingSetpoint(nextLevel)
	} else {
		nextLevel = device.currentValue("heatingSetpoint")
		nextLevel = (nextLevel - 1)
		nextLevel = checkTemperature(nextLevel)
		setHeatingSetpoint(nextLevel.intValue())
	}
}

def setHeatingSetpoint(degrees) {
	def scale = temperatureScale
	degrees = checkTemperature(degrees)
	def degreesDouble = degrees as Double
	String tempValueString
	if (scale == "C") {
		tempValueString = String.format('%2.1f', degreesDouble)
	} else {
		tempValueString = String.format('%2d', degreesDouble.intValue())
	}
 	traceEvent(settings.logFilter, "setHeatingSetpoint> new setPoint: $tempValueString", settings.trace)
	def celsius = (scale == "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	def cmds = []
	cmds += zigbee.writeAttribute(0x201, 0x12, 0x29, hex(celsius * 100))
	cmds += zigbee.readAttribute(0x0201, 0x0012)
	sendZigbeeCommands(cmds)
}

//-- Thermostat Modes -------------------------------------------------------------------------------------
void off() {
	setThermostatMode('off')
}

void heat() {
	setThermostatMode('heat')
}


def modes() {
	["mode_off", "mode_heat"]
}

def getModeMap() {
	[
		"00": "off",
		"04": "heat"
	]
}

def setThermostatMode(mode) {
	traceEvent(settings.logFilter, "setThermostatMode>switching thermostatMode", settings.trace)
	mode = mode?.toLowerCase()
	if (mode in supportedThermostatModes) {
 		"mode_$mode" ()
	} else {
		traceEvent(settings.logFilter, "setThermostatMode to $mode is not supported by this thermostat", settings.trace, get_LOG_WARN())
	}
}

def mode_off() {
	traceEvent(settings.logFilter, "off>begin", settings.trace)
 	def cmds = []
	cmds += zigbee.writeAttribute(0x0201, 0x001C, 0x30, 0)
	cmds += zigbee.readAttribute(0x0201, 0x001C)
	sendZigbeeCommands(cmds)
	traceEvent(settings.logFilter, "off>end", settings.trace)
}

def mode_heat() {
	traceEvent(settings.logFilter, "heat>begin", settings.trace)
 	def cmds = []
	cmds += zigbee.writeAttribute(0x0201, 0x001C, 0x30, 4)
	cmds += zigbee.readAttribute(0x0201, 0x001C)
	sendZigbeeCommands(cmds)
	traceEvent(settings.logFilter, "heat>end", settings.trace)
}

//-- Keypad Lock -----------------------------------------------------------------------------------------

def keypadLockLevel() {
	["unlock", "lock"] //only those level are used for the moment
}

def getLockMap() {
	[
		"00": "unlocked ",
		"01": "locked ",
	]
}

//---misc--------------------------------------------------------------------------------------------------------

def refresh() {
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
		def cmds = []
			
		state.updatedLastRanAt = now()  
		cmds += zigbee.readAttribute(0x0201, 0x0000)	// Rd thermostat Local temperature
		cmds += zigbee.readAttribute(0x0201, 0x0012)	// Rd thermostat Occupied heating setpoint
		cmds += zigbee.readAttribute(0x0201, 0x0008)	// Rd thermostat PI heating demand
		cmds += zigbee.readAttribute(0x0201, 0x001C)	// Rd thermostat System Mode
		cmds += zigbee.readAttribute(0x0204, 0x0001) 	// Rd thermostat Keypad lock
		
        sendZigbeeCommands(cmds)
		refresh_misc() 
	}
	else {
        traceEvent(settings.logFilter, "updated(): Ran within last 5 seconds so aborting", settings.trace, get_LOG_TRACE())
	}
}


void refresh_misc() {
	
	def constraint = ["heating","idle"]

    def weather = get_weather()
	traceEvent(settings.logFilter,"refresh_misc>begin, settings.disableOutdorTemperatureParam=${settings.disableOutdorTemperatureParam}, weather=$weather", settings.trace)
	def cmds=[]
	state?.scale = temperatureScale 
	
	traceEvent(settings.logFilter, "refresh>scale=${state.scale}", settings.trace)
	
	if (weather || weather == 0) {
		double tempValue    
		int outdoorTemp = weather.toInteger()
        if(temperatureScale == 'F')
        {//the value sent to the thermostat must be in C
        //the thermostat make the conversion to F
        	outdoorTemp = fahrenheitToCelsius(outdoorTemp).toDouble().round()
        }
		int outdoorTempValue
		int outdoorTempToSend  
        if(disableOutdorTemperatureParam == "Setpoint") {
			//delete outdoorTemp
			cmds += zigbee.writeAttribute(0xFF01, 0x0010, 0x29, 0x8000)
        }
        else
        {
			cmds += zigbee.writeAttribute(0xFF01, 0x0011, 0x21, 10800)//set the outdoor temperature timeout to 3 hours
            if (outdoorTemp < 0) {
                outdoorTempValue = -outdoorTemp*100 - 65536
                outdoorTempValue = -outdoorTempValue
                outdoorTempToSend = zigbee.convertHexToInt(swapEndianHex(hex(outdoorTempValue)))
                cmds += zigbee.writeAttribute(0xFF01, 0x0010, 0x29, outdoorTempToSend, [mfgCode: 0x119C])
            } else {
                outdoorTempValue = outdoorTemp*100
                int tempa = outdoorTempValue.intdiv(256)
                int tempb = (outdoorTempValue % 256) * 256
                outdoorTempToSend = tempa + tempb
                cmds += zigbee.writeAttribute(0xFF01, 0x0010, 0x29, outdoorTempToSend, [mfgCode: 0x119C])
            }
        }

      	def mytimezone = location.getTimeZone()
        long dstSavings = 0
        if(mytimezone.useDaylightTime() && mytimezone.inDaylightTime(new Date())) {
          dstSavings = mytimezone.getDSTSavings()
		}
		//To refresh the time
		long secFrom2000 = (((now().toBigInteger() + mytimezone.rawOffset + dstSavings ) / 1000) - (10957 * 24 * 3600)).toLong() //number of second from 2000-01-01 00:00:00h
		long secIndian = zigbee.convertHexToInt(swapEndianHex(hex(secFrom2000).toString())) //switch endianess
		cmds += zigbee.writeAttribute(0xFF01, 0x0020, 0x23, secIndian, [mfgCode: 0x119C])

	}    

    if(backlightAutoDimParam == "On Demand"){ 	//Backlight when needed
        traceEvent(settings.logFilter,"Backlight on press",settings.trace)
        cmds += zigbee.writeAttribute(0x0201, 0x0402, 0x30, 0x0000)
    }
    else{										//Backlight sensing
        traceEvent(settings.logFilter,"Backlight Always ON",settings.trace)
        cmds += zigbee.writeAttribute(0x0201, 0x0402, 0x30, 0x0001)
    }       

    traceEvent(settings.logFilter,"keyboardLockParam: ${keyboardLockParam}",settings.trace)
    if(keyboardLockParam == "Lock"){								//lock
        traceEvent(settings.logFilter,"lock",settings.trace)
		cmds += zigbee.writeAttribute(0x0204, 0x0001, 0x30, 0x01)
    }
    else{ 	//unlock
        traceEvent(settings.logFilter,"unlock",settings.trace)
		cmds += zigbee.writeAttribute(0x0204, 0x0001, 0x30, 0x00)
    }    

	if(timeFormatParam == "12h AM/PM"){//12h AM/PM
		traceEvent(settings.logFilter,"Set to 12h AM/PM",settings.trace)
		cmds += zigbee.writeAttribute(0xFF01, 0x0114, 0x30, 0x0001)
	}
	else{//24h
		traceEvent(settings.logFilter,"Set to 24h",settings.trace)
		cmds += zigbee.writeAttribute(0xFF01, 0x0114, 0x30, 0x0000)
	} 

	traceEvent(settings.logFilter,"refresh_misc> about to  refresh other misc, scale=${state.scale}", settings.trace)
   if (state?.scale == 'C') {
		cmds += zigbee.writeAttribute(0x0204, 0x0000, 0x30, 0)	// Wr °C on thermostat display
	} else {
		cmds += zigbee.writeAttribute(0x0204, 0x0000, 0x30, 1)	// Wr °F on thermostat display 
	}
    
	traceEvent(settings.logFilter,"refresh_misc>end", settings.trace)

    if(cmds)
    {
        sendZigbeeCommands(cmds)
    }
    
}


//-- Private functions -----------------------------------------------------------------------------------
void sendZigbeeCommands(cmds, delay = 250) {
	cmds.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	sendHubCommand(cmds, delay)
}

private def checkTemperature(def number)
{
	def scale = temperatureScale
	if(scale == 'F')
    {
    	if(number < 41)
        {
        	number = 41
        }
        else if(number > 86)
    	{
        	number = 86        
		}
    }
    else//scale == 'C'
    {
    	if(number < 5)
        {
        	number = 5
        }
        else if(number > 30)
    	{
        	number = 30
        }
    }
    return number
}

private def get_weather() {
	def mymap = getTwcConditions()
    traceEvent(settings.logFilter,"get_weather> $mymap",settings.trace)
    def weather = mymap.temperature
    traceEvent(settings.logFilter,"get_weather> $weather",settings.trace)
	return weather
}


private hex(value) {

	String hex=new BigInteger(Math.round(value).toString()).toString(16)
	traceEvent(settings.logFilter,"hex>value=$value, hex=$hex",settings.trace)	
	return hex    
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

private int get_LOG_ERROR() {
	return 1
}
private int get_LOG_WARN() {
	return 2
}
private int get_LOG_INFO() {
	return 3
}
private int get_LOG_DEBUG() {
	return 4
}
private int get_LOG_TRACE() {
	return 5
}

def traceEvent(logFilter, message, displayEvent = false, traceLevel = 4, sendMessage = true) {
	int LOG_ERROR = get_LOG_ERROR()
	int LOG_WARN = get_LOG_WARN()
	int LOG_INFO = get_LOG_INFO()
	int LOG_DEBUG = get_LOG_DEBUG()
	int LOG_TRACE = get_LOG_TRACE()

	if (displayEvent || traceLevel < 4) {
		switch (traceLevel) {
			case LOG_ERROR:
				log.error "${message}"
				break
			case LOG_WARN:
				log.warn "${message}"
				break
			case LOG_INFO:
				log.info "${message}"
				break
			case LOG_TRACE:
				log.trace "${message}"
				break
			case LOG_DEBUG:
			default:
				log.debug "${message}"
				break
		}
	}
} 