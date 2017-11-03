/**
Copyright Sinopé Technologies
SVN-311
**/

preferences {
//	input("zipcode", "text", title: "ZipCode for setting outdoor Temp", description: "by default,use current hub location")
	input("trace", "bool", title: "trace", description:
		"Set it to true to enable tracing or leave it empty (no tracing)")
	input("logFilter", "number",title: "(1=ERROR only,2=<1+WARNING>,3=<2+INFO>,4=<3+DEBUG>,5=<4+TRACE>)",  range: "1..5",
 		description: "optional" )        
} 

metadata {
	// Automatically generated. Make future change here.


	definition(name: "TH1123ZB-TH1124ZB Sinope Thermostat", namespace: "Sinope Technologies", author: "Rejean Bouchard/Yves Racine") {
		capability "thermostatHeatingSetpoint"
		capability "thermostatMode"
		capability "thermostatOperatingState"        
		capability "thermostatSetpoint"
		capability "Actuator"
		capability "Temperature Measurement"
		capability "Thermostat"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"

		attribute "temperatureDisplayMode", "enum", ["Deg_C", "Deg_F"]
		attribute "occupancyStatus", "enum", ["unoccupy", "occupy"]
		attribute "outdoorTemp", "number"
		attribute "heatingSetpointRangeHigh", "number"
		attribute "heatingSetpointRangeLow", "number"
		attribute "heatingSetpointRange", "VECTOR3"
		attribute "verboseTrace", "string"
		attribute "presence", "enum", ["present", "non present"]
        
		command "heatLevelUp"
		command "heatLevelDown"
		command "setThermostatMode"
		command "switchMode"        
		command "setTemperatureDisplayMode"
		command "setOccupancyStatus"
		command "setThermostatSetpoint", ["number"]
		command "setHeatingSetpointRangeHigh"
		command "setHeatingSetpointRangeLow"
		command "setHeatingSetpointRange"
		command "unoccupy"        
		command "occupy"        
		command "present"        
		command "away"        

		fingerprint endpoint: "1",
			profileId: "0104",
			inClusters: "0000,0003,0004,0005,0201,0204,0402,0B04,0B05",
			outClusters: "0019"
		fingerprint endpoint: "1",
			profileId: "0104",
			inClusters: "0000,0003,0004,0005,0201,0204,0402,0B04,0B05",
			outClusters: "0019"
            
//		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0201,0204,0402,0B04,0B05", manufacturer: "Sinope Technologies", model: "TH1123ZB"
//		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0201,0204,0402,0B04,0B05", manufacturer: "Sinope Technologies", model: "TH1124ZB"
	}


	//--------------------------------------------------------------------------------------------------------

	// simulator metadata
	simulator {}

	//--------------------------------------------------------------------------------------------------------
	tiles(scale: 2) {
    
		multiAttributeTile(name:"thermostatMulti", type:"thermostat", width:6, height:4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}', unit:"dF", backgroundColor:"#269bd2") 
			}
			tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "heatLevelUp")
				attributeState("VALUE_DOWN", action: "heatLevelDown")
			}
			tileAttribute("device.heatingDemand", key: "SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue}%', unit:"%")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
				attributeState("cooling", backgroundColor:"#269bd2")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}
		}
		//-- Value Tiles -------------------------------------------------------------------------------------------

		valueTile("temperature", "device.temperature", width: 4, height: 2) {
			state("temperature", label: '${currentValue}°',
				backgroundColors: getBackgroundColors()
			)
		}

		valueTile("heatingDemand", "device.heatingDemand", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "heatingDemand", label: '${currentValue}%', unit: "%", backgroundColor: "#ffffff"
		}

		//-- Standard Tiles ----------------------------------------------------------------------------------------

		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "heat", label: '${currentValue}°', backgroundColor: "#ffffff"
		}

		standardTile("heatLevelUp", "device.heatingSetpoint", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", label: '', action: "heatLevelUp", icon: "st.thermostat.thermostat-up"
		}

		standardTile("heatLevelDown", "device.heatingSetpoint", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", label: '', action: "heatLevelDown", icon: "st.thermostat.thermostat-down"
		}


		standardTile("operatingState", "device.thermostatOperatingState", width: 2, height: 2) {
			state "idle", label:'${name}', backgroundColor:"#ffffff"
			state "heating", label:'${name}', backgroundColor:"#e86d13"
			state "cooling", label:'${name}', backgroundColor:"#00A0DC"
		}
		standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, height: 2,width: 2,decoration: "flat") {
			state "off", label:'', action:"switchMode", icon:"st.thermostat.heating-cooling-off"
			state "heat",label:'', action:"switchMode", icon:"st.thermostat.heat", defaultState: true
		}

		standardTile("temperatureDisplayMode", "device.temperatureDisplayMode", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
			state "Deg_C", label: '${name}', action: "setTemperatureDisplayMode", icon: "st.alarm.temperature.normal", defaultState: true
			state "Deg_F", label: '${name}', action: "setTemperatureDisplayMode", icon: "st.alarm.temperature.normal"
		}

		standardTile("occupancyStatus", "device.occupancyStatus", inactiveLabel: false, height: 2,width: 2,decoration: "flat") {
			state "occupy",	label:'${name}',  action:"setOccupancyStatus", 	icon:"st.presence.house.unlocked", defaultState: true
			state "unoccupy", label:'${name}', action:"setOccupancyStatus", icon:"st.presence.house.secured"
		}

		standardTile("refresh", "device.temperature", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		standardTile("weatherTemperature", "device.outdoorTemp", inactiveLabel:false, width: 2, height: 2, 
			decoration: "flat", canChangeIcon: false) {
			state "default", label: 'OutdoorTemp ${currentValue}°', unit:"dF",
			icon: "st.Weather.weather2",
			backgroundColor: "#ffffff"
		}

		standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label: '', action: "configuration.configure", icon: "st.secondary.configure"
		}

		//-- Main & Details ----------------------------------------------------------------------------------------

		main("thermostatMulti")
//		details(["thermostatMulti","heatLevelUp","heatingSetpoint","heatLevelDown","thermostatMode", "occupancyStatus", "temperatureDisplayMode", "refresh", "configure"])
		details(["thermostatMulti","thermostatMode", "refresh"])
//		details(["thermostatMulti","heatLevelUp","heatingSetpoint","heatLevelDown","thermostatMode", "refresh"]) / To uncomment if you want the heatLevelDown and heatLevelUp in the UI
	}
}

def getBackgroundColors() {
	def results
	if (state?.scale =='C') {
				// Celsius Color Range
		results=
			[        
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
	traceEvent(settings.logFilter,"installed>Device is now Installed", settings.trace)
	initialize()

}


def updated() {
	traceEvent(settings.logFilter,"updated>Device is now updated", settings.trace)
	try {
		unschedule()
	} catch (e) {
		traceEvent(settings.logFilter,"updated>exception $e, continue processing", settings.trace, get_LOG_ERROR())
	}
	initialize()
}

void initialize() {
	state?.scale=getTemperatureScale() 
  	runEvery5Minutes(refresh)
    
	def supportedThermostatModes=['off', 'heat']
	state?.supportedThermostatModes= supportedThermostatModes
	sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: (settings.trace?:false))	
	def supportedThermostatFanModes=[]
	sendEvent(name: "supportedThermostatFanModes", value: supportedThermostatFanModes, displayed: (settings.trace?:false))	
	configure()
    
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
	def scale = getTemperatureScale()
	state?.scale = scale
	traceEvent(settings.logFilter,"parse>Description :( $description )", settings.trace)
	def cluster = zigbee.parse(description)
	traceEvent(settings.logFilter,"parse>Cluster : $cluster", settings.trace)
	def map = [:]
	if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
		traceEvent(settings.logFilter,"parse>Desc Map: $descMap", settings.trace)
		if (descMap.cluster == "0201" && descMap.attrId == "0000") {
			map.name = "temperature"
			map.value = getTemperatureValue(descMap.value)
			traceEvent(settings.logFilter,"parse>ACTUAL TEMP:  ${map.value}", settings.trace)
		} else if (descMap.cluster == "0201" && descMap.attrId == "0008") {
			map.name = "heatingDemand"
			map.value = getHeatingDemand(descMap.value)			
			def operatingState=  (map.value.toInteger() < 10) ? "idle" :"heating"  
			def isChange = isStateChange(device, "thermostatOperatingState", operatingState)
			def isDisplayed = isChange        
			sendEvent(name:"thermostatOperatingState", value: operatingState, displayed: isDisplayed)
			traceEvent(settings.logFilter,"parse>HEATING DEMAND: ${map.value}, operatingState=$operatingState", settings.trace)
            
		} else if (descMap.cluster == "0201" && descMap.attrId == "0012") {
			def currentStatus = device.currentState("occupancyStatus")?.value
			if (currentStatus == "occupy") {
				map.name = "heatingSetpoint"
				map.value = getTemperatureValue(descMap.value)
				def isChange = isStateChange(device, "thermostatSetpoint", map.value.toString())
				def isDisplayed = isChange        
				sendEvent(name:"thermostatSetpoint", value: map.value, unit: scale, displayed: isDisplayed)
				traceEvent(settings.logFilter,"parse>OCCUPY HEATING SETPOINT:${map.value} ", settings.trace)
			}
		} else if (descMap.cluster == "0201" && descMap.attrId == "0014") {
			def currentStatus = device.currentState("occupancyStatus")?.value
			if (currentStatus == "unoccupy") {
				map.name = "heatingSetpoint"
				map.value = getTemperatureValue(descMap.value)
				def isChange = isStateChange(device, "thermostatSetpoint", map.value.toString())
				def isDisplayed = isChange        
				sendEvent(name:"thermostatSetpoint", value:map.value, unit: scale, displayed: isDisplayed)
				traceEvent(settings.logFilter,"parse>UNOCCUPY HEATING SETPOINT: ${map.value}", settings.trace)
			}
		} else if (descMap.cluster == "0201" && descMap.attrId == "0015") {
			map.name = "heatingSetpointRangeLow"
			map.value = getTemperatureValue(descMap.value)
			traceEvent(settings.logFilter,"parse>LOW HeatingSetpoint: ${map.value}", settings.trace)
		} else if (descMap.cluster == "0201" && descMap.attrId == "0016") {
			map.name = "heatingSetpointRangeHigh"
			map.value = getTemperatureValue(descMap.value)
			traceEvent(settings.logFilter,"parse>HIGH HeatingSetpoint: ${map.value}", settings.trace)
		} else if (descMap.cluster == "0201" && descMap.attrId == "001c") {
			map.name = "thermostatMode"
			map.value = getModeMap()[descMap.value]
			traceEvent(settings.logFilter,"MODE: ${map.value}", settings.trace)
		} else if (descMap.cluster == "0201" && descMap.attrId == "0002") {
			map.name = "occupancyStatus"
			map.value = getOccupancyMap()[descMap.value]
			traceEvent(settings.logFilter,"parse>OCCUPANCY: ${map.value}", settings.trace)
			if (map.value == "occupy") {              
				def isChange = isStateChange(device, "presence", "present")
				def isDisplayed = isChange        
				sendEvent(name:"presence", value: "present" , displayed: isDisplayed)
			} else {     		    
				def isChange = isStateChange(device, "presence", "non present")
				def isDisplayed = isChange        
				sendEvent(name:"presence", value: "non present" , displayed: isDisplayed)
			}                
		} else if (descMap.cluster == "0201" && descMap.attrId == "0400") {
			map.name = "occupancyStatus"
			map.value = getOccupancyMap()[descMap.value]
			traceEvent(settings.logFilter,"parse>MF-OCCUPANCY", settings.trace)
			if (map.value == "occupy") {              
				def isChange = isStateChange(device, "presence", "present")
				def isDisplayed = isChange        
				sendEvent(name:"presence", value: "present" , displayed: isDisplayed)
			} else {     		    
				def isChange = isStateChange(device, "presence", "non present")
				def isDisplayed = isChange        
				sendEvent(name:"presence", value: "non present" , displayed: isDisplayed)
			}                
		} else if (descMap.cluster == "0204" && descMap.attrId == "0000") {
			map.name = "temperatureDisplayMode"
			map.value = getTemperatureDisplayModeMap()[descMap.value]
			traceEvent(settings.logFilter,"parse>DISPLAY MODE: ${map.value}", settings.trace)
		}
	}        
	def result = null
	if (map) {
		def isChange = isStateChange(device, map.name, map.value.toString())
		map.displayed = isChange    
		if ((map.name.toLowerCase().contains("temp")) || (map.name.toLowerCase().contains("setpoint"))) {
			map.scale=scale  
		}
		result = createEvent(map)
//		sendEvent(map)        
        
	}
	traceEvent(settings.logFilter,"Parse returned $map", settings.trace)

	return result
}

//--------------------------------------------------------------------------------------------------------


def parseDescriptionAsMap(description) {
	traceEvent(settings.logFilter,"parseDescriptionAsMap>parsing MAP ...", settings.trace)
	(description - "read attr - ").split(",").inject([:]) {	
    	map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}
//-- Temperature -----------------------------------------------------------------------------------------

def getTemperatureValue(value) {
	def scale= state?.scale
	if (value != null) {
		def celsius = Integer.parseInt(value, 16) / 100
		if (scale == "C") {
			return celsius.toDouble().round(1)
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
	def scale = state?.scale
	def heatingSetpointRangeHigh
	double nextLevel
    
	try {
		heatingSetpointRangeHigh = device.latestValue("heatingSetpointRangeHigh")
	} catch (any) {
		traceEvent(settings.logFilter, "heatLevelUp>not able to get heatingSetpointRangeLow ($heatingSetpointRangeLow),using default value", 
			settings.trace, get_LOG_WARN())
	}
	heatingSetpointRangeHigh = (heatingSetpointRangeHigh) ?: (scale == 'C') ? 10.0 : 50
 
	if (scale == 'C') {
		nextLevel = device.currentValue("heatingSetpoint").toDouble()
		nextLevel = (nextLevel + 0.5).round(1)
		if (nextLevel > heatingSetpointRangeHigh.toDouble().round(1)) {
			nextLevel = heatingSetpointRangeHigh.toDouble().round(1)
		}
		setHeatingSetpoint(nextLevel)
	} else {
		nextLevel = device.currentValue("heatingSetpoint")
		nextLevel = (nextLevel + 1)
		if (nextLevel < heatingSetpointRangeHigh.toDouble()) {
			nextLevel = heatingSetpointRangeHigh.toDouble()
		}
		setHeatingSetpoint(nextLevel.intValue())
	}

}

def heatLevelDown() {
	def scale = state?.scale
	def heatingSetpointRangeLow
	double nextLevel

	try {
		heatingSetpointRangeLow = device.latestValue("heatingSetpointRangeLow")
	} catch (any) {
		traceEvent(settings.logFilter,"heatLevelDown>not able to get heatingSetpointRangeLow ($heatingSetpointRangeLow),using default value",
			settings.trace, get_LOG_WARN())
	}
	heatingSetpointRangeLow = (heatingSetpointRangeLow) ?: (scale == 'C') ? 10.0 : 50
	if (scale == 'C') {
		nextLevel = device.currentValue("heatingSetpoint").toDouble()
		nextLevel = (nextLevel - 0.5).round(1)
		if (nextLevel <= heatingSetpointRangeLow.toDouble().round(1)) {
			nextLevel = heatingSetpointRangeLow.toDouble().round(1)
		}
		setHeatingSetpoint(nextLevel)
	} else {
		nextLevel = device.currentValue("heatingSetpoint")
		nextLevel = (nextLevel - 1)
		if (nextLevel < heatingSetpointRangeLow.toDouble()) {
			nextLevel = heatingSetpointRangeLow.toDouble()
		}
		setHeatingSetpoint(nextLevel.intValue())
	}
    

}
void setThermostatSetpoint(temp) {
	setHeatingSetpoint(temp)
}

def setHeatingSetpoint(degrees) {
	def scale = state?.scale
	def degreesDouble = degrees as Double
	sendEvent("name":"heatingSetpoint", "value":degreesDouble, displayed:true)
	sendEvent("name":"thermostatSetpoint", "value":degreesDouble,displayed:true)
	traceEvent(settings.logFilter,"setHeatingSetpoint> new setPoint: $degreesDouble", settings.trace)
	def celsius = (scale== "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x12 0x29 {" + hex(celsius*100) + "}"
}


def setCoolingSetpoint(temp) {
	traceEvent(settings.logFilter,"coolingSetpoint is not supported by this thermostat", settings.trace, get_LOG_WARN())
}

void setHeatingSetpointRange(rangeArray=[]) {

	if ((!rangeArray) || (rangeArray.size()!=2)) {
		traceEvent(settings.logFilter,"setHeatingSetpointRange>cannot change the thermostat Range, value ($rangeArray) is null or invalid",settings.trace, get_LOG_WARN(),true)
		return    
	}    
	def temp_min=rangeArray[0]
	def temp_max=rangeArray[1]	
	setHeatingSetpointRangeLow(temp_min)    
	setHeatingSetpointRangeHigh(temp_max)    
}


def setHeatingSetpointRangeLow(degrees) {
	def scale = state?.scale
	def degreesDouble = degrees as Double
	traceEvent(settings.logFilter,"setHeatingSetpointRangeLow> new Low: $degreesDouble", settings.trace)
	def celsius = (scale== "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x15 0x29 {" + hex(celsius*100) + "}"
	
	delayBetween([
		zigbee.readAttribute(0x0201, 0x0012),
		zigbee.readAttribute(0x0201, 0x0015)
	   	], 100)   	
	float temp=degrees.toFloat().round(1)    
	sendEvent(name: 'heatingSetpointRangeLow', value: temp, isStateChange:true,unit:scale)
	def setpointRangeHigh=device.currentValue('heatingSetpointRangeHigh')
	def heatingSetpointRange= [temp,setpointRangeHigh]
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, isStateChange: true, displayed: (settings.trace?:false))
}

def setHeatingSetpointRangeHigh(degrees) {
	def scale = state?.scale
	def degreesDouble = degrees as Double
	traceEvent(settings.logFilter,"setHeatingSetpointRangeHigh> new High: $degreesDouble", settings.trace)
	def celsius = (scale== "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x16 0x29 {" + hex(celsius*100) + "}"
	delayBetween([
		zigbee.readAttribute(0x0201, 0x0012),
		zigbee.readAttribute(0x0201, 0x0016)
	   	], 100)   	
 
	float temp=degrees.toFloat().round(1)    
	sendEvent(name: 'heatingSetpointRangeHigh', value: temp, isStateChange:true,unit:scale)
	def setpointRangeLow=device.currentValue('heatingSetpointRangeLow')
	def heatingSetpointRange= [setpointRangeLow,temp]
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, isStateChange: true, displayed: (settings.trace?:false))
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
void fanOn() {
	setThermostatFanMode('on')
}
void fanAuto() {
	setThermostatFanMode('auto')
}
void fanOff() { 
	setThermostatFanMode('off')
}
def fanCirculate() {
	setThermostatFanMode('circulate')
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
			device.currentValue("supportedThermostatModes").toString().minus('[').minus(']').tokenize(',') : ['off','heat']
	}
    
	return state?.supportedThermostatModes
}
 

def setThermostatFanMode(mode) {

	traceEvent(settings.logFilter,"setThermostatFanMode to $mode is not supported by this thermostat", settings.trace, get_LOG_WARN())
}
def switchMode() {
	log.debug "switching thermostatMode"
	def currentMode = "mode_" + device.currentState("thermostatMode")?.value
	def modeOrder = modes()
	def index = modeOrder.indexOf(currentMode)
	def next = index >= 0 && index < modeOrder.size() - 1 ? modeOrder[index + 1] : modeOrder[0]
	//"st wattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x1C 0x30 {" + next + "}"
	log.debug "switching mode from $currentMode to $next"
	"$next" ()
}


def setThermostatMode(mode) {
	traceEvent(settings.logFilter,"setThermostatMode>switching thermostatMode", settings.trace)
	mode=mode?.toLowerCase()
	def supportedThermostatModes = getSupportedThermostatModes()
    
	if (mode in supportedThermostatModes) {
		"mode_$mode" ()    
	} else {
    
		traceEvent(settings.logFilter,"setThermostatMode to $mode is not supported by this thermostat", settings.trace, get_LOG_WARN())
    
	}    
}

def mode_off() {
	log.debug "---off---"
	sendEvent("name": "thermostatMode", "value": "off")
	//"st wattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x1C 0x30 {00}"
	zigbee.writeAttribute(0x0201, 0x001C, 0x30, 0) + zigbee.readAttribute(0x0201, 0x0008)
}

def mode_heat() {
	log.debug "---heat---"
	sendEvent("name": "thermostatMode", "value": "heat")
	//"st wattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x1C 0x30 {04}"
	zigbee.writeAttribute(0x0201, 0x001C, 0x30, 4) + zigbee.readAttribute(0x0201, 0x0008)
}

//-- Temperature Display Mode ----------------------------------------------------------------------------

def temperatureDisplayModes() {
	["Deg_C", "Deg_F"]
}

def getTemperatureDisplayModeMap() {
	[
		"00": "Deg_C",
		"01": "Deg_F"
	]
}

def setTemperatureDisplayMode() {
	log.debug "Switching TempDisplayMode"
	def currentMode = device.currentState("temperatureDisplayMode")?.value
	def modeOrder = temperatureDisplayModes()
	def index = modeOrder.indexOf(currentMode)
	def next = index == 0 ? modeOrder[1] : modeOrder[0]
	log.debug "switching Temp Display Mode from $currentMode to $next"
	"$next" ()
}

def Deg_C() {
	log.debug "Deg_C"
	def currentStatus = device.currentState("occupancyStatus")?.value
	sendEvent("name": "temperatureDisplayMode", "value": "Deg_C")
	if (currentStatus == "occupy") {
		zigbee.writeAttribute(0x0204, 0x0000, 0x30, 0) + zigbee.readAttribute(0x0201, 0x0000) + zigbee.readAttribute(0x0201, 0x0012)
	} else {
		zigbee.writeAttribute(0x0204, 0x0000, 0x30, 0) + zigbee.readAttribute(0x0201, 0x0000) + zigbee.readAttribute(0x0201, 0x0014)
	}
}

def Deg_F() {
	log.debug "Deg_F"
	def currentStatus = device.currentState("occupancyStatus")?.value
	sendEvent("name": "temperatureDisplayMode", "value": "Deg_F")
	if (currentStatus == "occupy") {
		zigbee.writeAttribute(0x0204, 0x0000, 0x30, 1) + zigbee.readAttribute(0x0201, 0x0000) + zigbee.readAttribute(0x0201, 0x0012)
	} else {
		zigbee.writeAttribute(0x0204, 0x0000, 0x30, 1) + zigbee.readAttribute(0x0201, 0x0000) + zigbee.readAttribute(0x0201, 0x0014)
	}
}

//-- Occupancy -------------------------------------------------------------------------------------------

def occupancySts() {
	["unoccupy", "occupy"]
}

def getOccupancyMap() {
	[
		"00": "unoccupy",
		"01": "occupy"
	]
}

def setOccupancyStatus() {
	traceEvent(settings.logFilter,"setOccupancyStatus>switching occupancy status", settings.trace)
	def currentStatus = device.currentState("occupancyStatus")?.value
	traceEvent(settings.logFilter,"setOccupancyStatus>Occupancy :$currentStatus", settings.trace)
	def statusOrder = occupancySts()
	def index = statusOrder.indexOf(currentStatus)
	traceEvent(settings.logFilter,"setOccupancyStatus>Index = $index", settings.trace)
	def next = (index >= 0 && index < (statusOrder.size() - 1)) ? statusOrder[index + 1] : statusOrder[0]
	traceEvent(settings.logFilter,"setOccupancyStatus>switching occupancy from $currentStatus to $next", settings.trace)
	"$next" ()
}
void away() {
	unoccupy()
}

void unoccupy() {
	traceEvent(settings.logFilter,"unoccupy>Set unoccupy", settings.trace)
	sendEvent(name: "occupancyStatus", value: "unoccupy",displayed: true)
	sendEvent(name:"presence", value:"non present", displayed: true)
	state?.previousOccupyTemp=device.currentValue("heatingSetpoint")    
	zigbee.writeAttribute(0x0201,0x0400,0x30,0x00,[mfgCode: 0x119C])
	def scale = state?.scale
	def heatingSetpointRangeLow

	try {
		heatingSetpointRangeLow = device.latestValue("heatingSetpointRangeLow")
	} catch (any) {
		traceEvent(settings.logFilter,"unoccupy>not able to get heatingSetpointRangeLow ($heatingSetpointRangeLow),using default value",
			settings.trace, get_LOG_WARN())
	}
	heatingSetpointRangeLow = (heatingSetpointRangeLow) ?: (scale == 'C') ? 10.0 : 50
	sendEvent(name:"thermostatSetpoint", value:heatingSetpointRangeLow, unit: scale, displayed: true)
	sendEvent(name:"heatingSetpoint", value:heatingSetpointRangeLow, unit: scale, displayed: true)
	delayBetween([
		zigbee.readAttribute(0x0201, 0x0002),
		zigbee.readAttribute(0x0201, 0x0014)
	   	], 100)   	
}

void present() {
	occupy()
}
void occupy() {
	def scale = state?.scale
	traceEvent(settings.logFilter,"occupy>Set occupy", settings.trace)
	sendEvent(name: "occupancyStatus", value: "occupy", displayed: true)
	sendEvent(name:"presence", value:"present", displayed: true)
	zigbee.writeAttribute(0x0201, 0x0400, 0x30, 0x01, [mfgCode: 0x119C]) 
	def temp=(state?.previousOccupyTemp) ? state?.previousOccupyTemp : (scale == 'C') ? 20.0 : 70
	sendEvent(name:"thermostatSetpoint", value:temp, unit: scale, displayed: true)
	sendEvent(name:"heatingSetpoint", value:temp, unit: scale, displayed: true)
	delayBetween([
		zigbee.readAttribute(0x0201, 0x0002),
		zigbee.readAttribute(0x0201, 0x0012)
	], 100)   	
}

//-- Keypad Lock -----------------------------------------------------------------------------------------
/*
def keypadLockLevel() {
	["Unlock","Lock1"]		//only those level are used for the moment
	//["Unlock","Lock1","Lock2","Lock3","Lock4","Lock5"]
}

def getOccupancyMap() { 
	[
	"00":"Unlock",
	"01":"Lock1",
	"02":"Lock2",
	"03":"Lock3",
	"04":"Lock4",
	"05":"Lock5"
	]
}

def setOccupancyStatus() {
	log.debug "set keypad lock level"
	def currentLockLevel = device.currentState("keypadLock")?.value
	log.debug "KeypadLock : $currentLockLevel"
	def lockOrder = keypadLockLevel()
	def index = lockOrder.indexOf(currentLockLevel)
	log.debug "Index = $index"
	def next = (index >= 0 && index < (lockOrder.size() - 1)) ? lockOrder[index + 1] : lockOrder[0]
	log.debug "change keypad lock level from $currentLockLevel to $next"
	"$next"()
}

def Unlock() {
	log.debug "Unlock keypad"
	sendEvent("name":"keypadLock","value":"Unlock")
	zigbee.writeAttribute(0x0204,0x0001,0x30,0x00)
}

def Lock1() {
	log.debug "Lock keypad"
	sendEvent("name":"keypadLock","value":"Lock1")
	zigbee.writeAttribute(0x0204,0x0001,0x30,0x01)
}
*/

def configure() {
	state?.scale=getTemperatureScale()
	traceEvent(settings.logFilter,"binding to Thermostat cluster",settings.trace) 
	[
		"zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}", "delay 200",	//thermostat cluster
		//"zdo bind 0x${device.deviceNetworkId} 1 1 0x204 {${device.zigbeeId}} {}"				//thermostat user interface cluster
		//"zdo bind 0x${device.deviceNetworkId} 1 1 0x202 {${device.zigbeeId}} {}"
	]
	zigbee.configureReporting(0x0201, 0x0000, 0x29, 19, 301, 50) +		//local temperature
	zigbee.configureReporting(0x0201, 0x0008, 0x20, 22, 61, 25)  +		//heating demand
	zigbee.configureReporting(0x0201, 0x0012, 0x29, 15, 0x0000, 50) +	//occupied heating setpoint reported only on change
	zigbee.readAttribute(0x0201,0x0002) +
	zigbee.readAttribute(0x0204,0x0000) +
	zigbee.readAttribute(0x0201,0x0012) +
	zigbee.readAttribute(0x0201,0x0014) +
	zigbee.readAttribute(0x0201,0x0015) +
	zigbee.readAttribute(0x0201,0x0016) +
	zigbee.readAttribute(0x0201,0x0008) +
	zigbee.readAttribute(0x0201,0x001C) + 
	refreshTime()    
	//Report for Sinope Locate
	//zigbee.configureReporting(0x0003, 0x0400, 0x10, 1, 65000, 1)
}

def refresh()
{
	traceEvent(settings.logFilter,"refresh>begin", settings.trace)
	[
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x0000", "delay 200",	//local Temp
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x0002", "delay 200",	//occupancy
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x0008", "delay 200",	//Heating Demand
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x0012", "delay 200",	//Occupied Heating Setpoint
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x0014", "delay 200",	//Unoccupied Heating Setpoint
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x0015", "delay 200",	//Min Heating Setpoint
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x0016", "delay 200",	//Max Heating Setpoint
		"st rattr 0x${device.deviceNetworkId} 0x${device.endpointId} 0x201 0x001C", "delay 200"		//System Mode
        //zigbee.readAttribute(0x0201,0x0012)
	] + refreshTime() + refresh_misc() +
	zigbee.configureReporting(0x0201, 0x0000, 0x29, 19, 301, 50) +		//local temperature
	zigbee.configureReporting(0x0201, 0x0008, 0x20, 22, 61, 25)  +		//heating demand
	zigbee.configureReporting(0x0201, 0x0012, 0x29, 15, 0x0000, 50) 	//occupied heating setpoint reported only on change

}

void refresh_misc() {

	traceEvent(settings.logFilter,"refresh_misc> about to  refresh other misc variables", settings.trace)
	state?.scale=getTemperatureScale()
	if (state?.scale=='C') {
		Deg_C() 
	} else {        
		Deg_F() 
	}
    
	def heatingSetpointRangeHigh= (device.currentValue("heatingSetpointRangeHigh")) ?: (scale=='C')?30:99
	def heatingSetpointRangeLow= (device.currentValue("heatingSetpointRangeLow")) ?: (scale=='C')?10:50

	def low = heatingSetpointRangeLow.toFloat().round(1)
	def high = heatingSetpointRangeHigh.toFloat().round(1)   
	def heatingSetpointRange= [low,high]
	def isChanged= isStateChange(device, "heatingSetpointRange", heatingSetpointRange?.toString())    
	sendEvent(name: "heatingSetpointRange", value: heatingSetpointRange, isStateChange: isChanged, displayed: (settings.trace?:false))
	traceEvent(settings.logFilter,"refresh_misc>end", settings.trace)
 
}
  
	
    
   
  





//-- Private functions -----------------------------------------------------------------------------------

private def get_weather(zipcode) {
	def weather
	if (zipcode) {
		traceEvent(settings.logFilter,"refresh>ZipCode: ${zipcode}",settings.trace)
		weather = getWeatherFeature( "conditions", zipcode.trim() )
	} else {
		traceEvent(settings.logFilter,"refresh>ZipCode: current location",settings.trace)	
		weather = getWeatherFeature( "conditions" )
	}
	return weather
}


private hex(value) {

	String hex=new BigInteger(Math.round(value).toString()).toString(16)
//	log.debug "value=$value, hex=$hex"
	return hex    
}

def refreshTime() {
	def mytimezone = location.getTimeZone()
	long secFrom2000 = (((now().toBigInteger() + mytimezone.rawOffset + mytimezone.dstSavings) / 1000) - (10956 * 24 * 3600)).toLong() //number of second from 2000-01-01 00:00:00h
	long secIndian = zigbee.convertHexToInt(swapEndianHex(hex(secFrom2000).toString())) //switcw endianess
	traceEvent(settings.logFilter, "refreshTime>myTime = ${secFrom2000}  reversed = ${secIndian}", settings.trace)
	zigbee.writeAttribute(0xFF01, 0x0020, 0x23, secIndian, [mfgCode: 0x119C])
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

private int get_LOG_ERROR() {return 1}
private int get_LOG_WARN()  {return 2}
private int get_LOG_INFO()  {return 3}
private int get_LOG_DEBUG() {return 4}
private int get_LOG_TRACE() {return 5}

def traceEvent(logFilter,message, displayEvent=false, traceLevel=4, sendMessage=true) {
	int LOG_ERROR= get_LOG_ERROR()
	int LOG_WARN=  get_LOG_WARN()
	int LOG_INFO=  get_LOG_INFO()
	int LOG_DEBUG= get_LOG_DEBUG()
	int LOG_TRACE= get_LOG_TRACE()
	int filterLevel=(logFilter)?logFilter.toInteger():get_LOG_WARN()

	if ((displayEvent) || (sendMessage)) {
		def results = [
			name: "verboseTrace",
			value: message,
			displayed: ((displayEvent)?: false)
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
					log.info  "${message}"
				break
				case LOG_TRACE:
					log.trace "${message}"
				break
				case LOG_DEBUG:
				default:
					log.debug "${message}"
				break
			}  /* end switch*/              
			if (sendMessage) sendEvent (results)
		} /* end if displayEvent*/
	}
}