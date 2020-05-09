/**

Copyright Sinopé Technologies 2019
1.1.0
SVN-571
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
**/
preferences {
    input("BacklightAutoDimParam", "enum", title:"Backlight setting (default: sensing)", description: "On Demand or Sensing", options: ["On Demand", "Sensing"], multiple: false, required: false)
   	input("DisableOutdorTemperatureParam", "bool", title: "disable outdoor temperature", description: "Set it to true to Disable outdoor temperature on the thermostat")
    input("keyboardLockParam", "bool", title: "enable the lock", description: "Set to true to enable the lock on the thermostat")
    input("trace", "bool", title: "Trace", description:"Set it to true to enable tracing")
	input("logFilter", "number", title: "(1=ERROR only,2=<1+WARNING>,3=<2+INFO>,4=<3+DEBUG>,5=<4+TRACE>)", range: "1..5",
		description: "optional")
}

metadata {

	definition(name: "TH1123ZB-TH1124ZB Sinope Thermostat", namespace: "Sinope Technologies", author: "Sinope Technologies", ocfDeviceType: "oic.d.thermostat") {
 		capability "thermostatHeatingSetpoint"
		capability "thermostatMode"
		capability "thermostatOperatingState"
		capability "thermostatSetpoint"
 		capability "Actuator"
		capability "Temperature Measurement"
 		capability "Configuration"
		capability "Refresh"
        capability "lock"
		capability "Health check"

		attribute "temperatureDisplayMode", "enum", ["Deg_C", "Deg_F"]
        attribute "heatingSetpointRangeHigh", "number"
        attribute "heatingSetpointRangeLow", "number"
        attribute "heatingSetpointRange", "VECTOR3"
		attribute "outdoorTemp", "string"
		attribute "verboseTrace", "string"
        
		command "heatLevelUp"
		command "heatLevelDown"

        fingerprint manufacturer: "Sinope Technologies", model: "TH1123ZB", deviceJoinName: "Sinope TH1123ZB Thermostat"
            
        fingerprint manufacturer: "Sinope Technologies", model: "TH1124ZB", deviceJoinName: "Sinope TH1124ZB Thermostat" 

 	}

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
				attributeState("default", label: '${currentValue}%', unit: "%")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor: "#44b621")
				attributeState("heating", backgroundColor: "#ffa81e")
				attributeState("cooling", backgroundColor: "#269bd2")
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label: '${currentValue}', unit: "dF")
			}
		}
		//-- Value Tiles -------------------------------------------------------------------------------------------

		valueTile("heatingDemand", "device.heatingDemand", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "heatingDemand", label: '${currentValue}%', unit: "%", backgroundColor: "#ffffff"
		}

		//-- Standard Tiles ----------------------------------------------------------------------------------------

		standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
			state "off", label: '', action: "heat", icon: "st.thermostat.heating-cooling-off"
			state "heat", label: '', action: "off", icon: "st.thermostat.heat", defaultState: true
		}

		standardTile("refresh", "device.temperature", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		//-- Main & Details ----------------------------------------------------------------------------------------

        controlTile("heatingSetpointSlider", "device.heatingSetpoint", "slider",
        	sliderType: "HEATING",
            debouncePeriod: 1500,
            range: "device.heatingSetpointRange",
            width: 2, height: 2)
        {
            state "default", action:"setHeatingSetpoint", 
            	label:'${currentValue}${unit}', backgroundColor: "#E86D13"
        }


		main("thermostatMulti")
		details(["thermostatMulti",
			"heatingSetpointSlider",
			"thermostatMode",
			"refresh"
		])
	}
}

def getBackgroundColors() {
	def results
	if (state?.scale == 'C') {
		// Celsius Color Range
		results = [
			[value: 0, color: "#153591"],
			[value: 7, color: "#1e9cbb"],
			[value: 15, color: "#90d2a7"],
			[value: 23, color: "#44b621"],
			[value: 29, color: "#f1d801"],
			[value: 35, color: "#d04e00"],
			[value: 37, color: "#bc2323"]
		]
	} else {
		results =
			// Fahrenheit Color Range
			[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
	}
	return results

}

//-- Installation ----------------------------------------------------------------------------------------


def installed() {
	traceEvent(settings.logFilter, "installed>Device is now Installed", settings.trace)
	initialize()
}


def updated() {
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
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

	//allow 30 min without receiving temperature report
	return sendEvent(name: "checkInterval", value: 30*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

void initialize() {
	state?.scale = getTemperatureScale()
    runIn(2,refresh)
	
	runEvery15Minutes(refresh_misc)

	def supportedThermostatModes = ['off', 'heat']
    state?.supportedThermostatModes = supportedThermostatModes
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes)
    if(state?.scale == 'C')
    {
        sendEvent(name: "heatingSetpointRangeLow", value: 5.0, scale: scale)
        sendEvent(name: "heatingSetpointRangeHigh", value: 30.0, scale: scale)
    }
    else if(state?.scale == 'F')
    {
        sendEvent(name: "heatingSetpointRangeLow", value: 41, scale: scale)
        sendEvent(name: "heatingSetpointRangeHigh", value: 86, scale: scale)
    }
    sendEvent(name: "lock", value: "unlocked")
}

def ping() {
	refresh()
}

def uninstalled() {
	unschedule()
}

//-- Parsing ---------------------------------------------------------------------------------------------

// parse events into attributes
def parse(String description) {
    def result = []
	def scale = getTemperatureScale()
	state?.scale = scale
	traceEvent(settings.logFilter, "parse>Description :( $description )", settings.trace)
	def cluster = zigbee.parse(description)
	traceEvent(settings.logFilter, "parse>Cluster : $cluster", settings.trace)
	if (description?.startsWith("read attr -")) {
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
	def scale = getTemperatureScale()
	
 		if (descMap.cluster == "0201" && descMap.attrId == "0000") {
			map.name = "temperature"
			map.value = getTemperatureValue(descMap.value)
            sendEvent(name: map.name, value: map.value, unit: scale)
			traceEvent(settings.logFilter, "parse>${map.name}:  ${map.value}", settings.trace)
			sendEvent(name: "checkInterval", value: 30*60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0008") {
			map.name = "heatingDemand"
			map.value = getHeatingDemand(descMap.value)
            sendEvent(name: map.name, value: map.value)
            traceEvent(settings.logFilter, "parse>${map.name}: ${map.value}")
			def operatingState = (map.value.toInteger() < 10) ? "idle" : "heating"
			sendEvent(name: "thermostatOperatingState", value: operatingState)
			traceEvent(settings.logFilter,"thermostatOperatingState: ${operatingState}", settings.trace)
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0012") {
            map.name = "heatingSetpoint"
            map.value = getTemperatureValue(descMap.value, true)
            sendEvent(name: map.name, value: map.value, unit: scale)
            traceEvent(settings.logFilter, "parse>OCCUPY: ${map.name}: ${map.value}, scale: ${scale} ", settings.trace)
		} 
		else if (descMap.cluster == "0201" && descMap.attrId == "0014") {
            map.name = "heatingSetpoint"
            map.value = getTemperatureValue(descMap.value, true)
            sendEvent(name: map.name, value: map.value, unit: scale)
            traceEvent(settings.logFilter, "parse>UNOCCUPY: ${map.name}: ${map.value}", settings.trace)
		}
		else if (descMap.cluster == "0201" && descMap.attrId == "001c") {
			map.name = "thermostatMode"
			map.value = getModeMap()[descMap.value]
            sendEvent(name: map.name, value: map.value)
			traceEvent(settings.logFilter, "parse>${map.name}: ${map.value}", settings.trace)
		}
		else if (descMap.cluster == "0204" && descMap.attrId == "0001") {
			map.name = "lock"
			map.value = getLockMap()[descMap.value]
            sendEvent(name: map.name, value: map.value)
			traceEvent(settings.logFilter, "parse>${map.name}: ${map.value}", settings.trace)
		}
    return result
}
//-- Temperature -----------------------------------------------------------------------------------------

def getTemperatureValue(value, doRounding = false) {
	def scale = state?.scale
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
	def scale = getTemperatureScale()
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
	def scale = getTemperatureScale()
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
	def scale = getTemperatureScale()
	degrees = checkTemperature(degrees)
	def degreesDouble = degrees as Double
	String tempValueString
	if (scale == "C") {
		tempValueString = String.format('%2.1f', degreesDouble)
	} else {
		tempValueString = String.format('%2d', degreesDouble.intValue())
	}
	sendEvent(name: "heatingSetpoint", value: tempValueString, unit: scale)
 	traceEvent(settings.logFilter, "setHeatingSetpoint> new setPoint: $tempValueString", settings.trace)
	def celsius = (scale == "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	def cmds = []
	cmds += zigbee.writeAttribute(0x201, 0x12, 0x29, hex(celsius * 100))
	sendZigbeeCommands(cmds)
}

//-- Thermostat and Fan Modes -------------------------------------------------------------------------------------
void off() {
	setThermostatMode('off')
}
void auto() {
	setThermostatMode('auto')
}
void heat() {
	setThermostatMode('heat')
}
void emergencyHeat() {
	setThermostatMode('heat')
}
void cool() {
	setThermostatMode('cool')
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

def getSupportedThermostatModes() {

	if (!state?.supportedThermostatModes) {
		state?.supportedThermostatModes = (device.currentValue("supportedThermostatModes")) ?
			device.currentValue("supportedThermostatModes").toString().minus('[').minus(']').tokenize(',') : ['off', 'heat']
	}
	return state?.supportedThermostatModes
}

def setThermostatMode(mode) {
	traceEvent(settings.logFilter, "setThermostatMode>switching thermostatMode", settings.trace)
	mode = mode?.toLowerCase()
	def supportedThermostatModes = getSupportedThermostatModes()

	if (mode in supportedThermostatModes) {
 		"mode_$mode" ()
	} else {
		traceEvent(settings.logFilter, "setThermostatMode to $mode is not supported by this thermostat", settings.trace, get_LOG_WARN())
	}
}

def mode_off() {
	traceEvent(settings.logFilter, "off>begin", settings.trace)
	sendEvent(name: "thermostatMode", value: "off", data: [supportedThermostatModes: getSupportedThermostatModes()])
 	def cmds = []
	cmds += zigbee.writeAttribute(0x0201, 0x001C, 0x30, 0)
	cmds += zigbee.readAttribute(0x0201, 0x0008)
	sendZigbeeCommands(cmds)
	traceEvent(settings.logFilter, "off>end", settings.trace)
}

def mode_heat() {
	traceEvent(settings.logFilter, "heat>begin", settings.trace)
	sendEvent(name: "thermostatMode", value: "heat", data: [supportedThermostatModes: getSupportedThermostatModes()])
 	def cmds = []
	cmds += zigbee.writeAttribute(0x0201, 0x001C, 0x30, 4)
	cmds += zigbee.readAttribute(0x0201, 0x0008)
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

def unlock() {
	traceEvent(settings.logFilter, "unlock>begin", settings.trace)
	sendEvent(name: "lock", value: "unlocked")
	def cmds = []
	cmds += zigbee.writeAttribute(0x0204, 0x0001, 0x30, 0x00)
	sendZigbeeCommands(cmds)
	traceEvent(settings.logFilter, "unlock>end", settings.trace)
}

def lock() {
	traceEvent(settings.logFilter, "lock>begin", settings.trace)
	sendEvent(name: "lock", value: "locked")
	def cmds = []
	cmds += zigbee.writeAttribute(0x0204, 0x0001, 0x30, 0x01)
	sendZigbeeCommands(cmds)
	traceEvent(settings.logFilter, "lock>end", settings.trace)
}

//---misc--------------------------------------------------------------------------------------------------------

def refresh() {
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 20000) {
		state.updatedLastRanAt = now()   

    	state?.scale = getTemperatureScale()        
		traceEvent(settings.logFilter, "refresh>scale=${state.scale}", settings.trace)
		
        def cmds = []

		cmds += zigbee.readAttribute(0x0201, 0x0000)	// Rd thermostat Local temperature
    	cmds += zigbee.readAttribute(0x0201, 0x0012)	// Rd thermostat Occupied heating setpoint
    	cmds += zigbee.readAttribute(0x0201, 0x0008)	// Rd thermostat PI heating demand
   	 	cmds += zigbee.readAttribute(0x0201, 0x001C)	// Rd thermostat System Mode
        cmds += zigbee.readAttribute(0x0204, 0x0001) 	// Rd thermostat Keypad lock

        cmds += zigbee.configureReporting(0x0201, 0x0000, 0x29, 19, 301, 50) 	//local temperature
        cmds += zigbee.configureReporting(0x0201, 0x0008, 0x0020, 4, 300, 10) 	//heating demand
        cmds += zigbee.configureReporting(0x0201, 0x0012, 0x0029, 15, 302, 40) 	//occupied heating setpoint

        sendZigbeeCommands(cmds)
        refresh_misc()	
	}
	else {
        traceEvent(settings.logFilter, "updated(): Ran within last 20 seconds so aborting", settings.trace, get_LOG_TRACE())
	}
}


void refresh_misc() {

    def weather = get_weather()
	traceEvent(settings.logFilter,"refresh_misc>begin, settings.DisableOutdorTemperatureParam=${settings.DisableOutdorTemperatureParam}, weather=$weather", settings.trace)
	def cmds=[]

	if (weather) {
		double tempValue    
		int outdoorTemp = weather.toInteger()
        if(state?.scale == 'F')
        {//the value sent to the thermostat must be in C
        //the thermostat make the conversion to F
        	outdoorTemp = fahrenheitToCelsius(outdoorTemp).toDouble().round()
        }
		int outdoorTempValue
		int outdoorTempToSend  

        if(!settings.DisableOutdorTemperatureParam)
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
        else
        {//delete outdoorTemp
        	//the outdoor temperature cannot be directly erased from the thermostat.
            //to erase it rapidly, the external temperature timeout must be set to the minimal value (30sec)
        	cmds += zigbee.writeAttribute(0xFF01, 0x0011, 0x21, 30)//set the outdoor temperature timeout to 30sec
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

    if(BacklightAutoDimParam == "On Demand"){ 	//Backlight when needed
        traceEvent(settings.logFilter,"Backlight on press",settings.trace)
        cmds += zigbee.writeAttribute(0x0201, 0x0402, 0x30, 0x0000)
    }
    else{										//Backlight sensing
        traceEvent(settings.logFilter,"Backlight Sensing",settings.trace)
        cmds += zigbee.writeAttribute(0x0201, 0x0402, 0x30, 0x0001)
    }       

    traceEvent(settings.logFilter,"keyboardLockParam: ${keyboardLockParam}",settings.trace)
    if(keyboardLockParam != true){ 	//unlock
        traceEvent(settings.logFilter,"unlock",settings.trace)
        unlock()
    }
    else{								//lock
        traceEvent(settings.logFilter,"lock",settings.trace)
        lock()
    }       

	traceEvent(settings.logFilter,"refresh_misc> about to  refresh other misc, scale=${state.scale}", settings.trace)
    
	if (state?.scale == 'C') {
		cmds += zigbee.writeAttribute(0x0204, 0x0000, 0x30, 0)	// Wr °C on thermostat display
	} else {
		cmds += zigbee.writeAttribute(0x0204, 0x0000, 0x30, 1)	// Wr °F on thermostat display 
	}

    def heatingSetpointRangeHigh
    def heatingSetpointRangeLow
    if(state?.scale == 'C')
    {
    	heatingSetpointRangeLow = 5.0
   		heatingSetpointRangeHigh = 30.0
    }
    else if(state?.scale == 'F')
    {
    	heatingSetpointRangeLow = 41
   		heatingSetpointRangeHigh = 86
    }
    sendEvent(name: "heatingSetpointRangeLow", value: heatingSetpointRangeLow, scale: scale)
    sendEvent(name: "heatingSetpointRangeHigh", value: heatingSetpointRangeHigh, scale: scale)
	def low = heatingSetpointRangeLow.toFloat().round(1)
	def high = heatingSetpointRangeHigh.toFloat().round(1)   
	def heatingSetpointRange= [low,high]
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, scale: state.scale)
	traceEvent(settings.logFilter,"refresh_misc>end", settings.trace)

    if(cmds)
    {
        sendZigbeeCommands(cmds)
    }
    
}


//-- Private functions -----------------------------------------------------------------------------------
void sendZigbeeCommands(cmds, delay = 1000) {
	cmds.removeAll { it.startsWith("delay") }
	// convert each command into a HubAction
	cmds = cmds.collect { new physicalgraph.device.HubAction(it) }
	sendHubCommand(cmds, delay)
}

private def checkTemperature(def number)
{
	def scale = getTemperatureScale()
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
	int filterLevel = (logFilter) ? logFilter.toInteger() : get_LOG_WARN()

	if ((displayEvent) || (sendMessage)) {
		def results = [
			name: "verboseTrace",
			value: message,
			displayed: ((displayEvent) ?: false)
		]

		if ((displayEvent) && (filterLevel >= traceLevel)) {
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
			} /* end switch*/
			if (sendMessage) sendEvent(results)
		} /* end if displayEvent*/
	}
}