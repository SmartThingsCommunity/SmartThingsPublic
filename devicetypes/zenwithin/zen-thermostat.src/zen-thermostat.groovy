/**
 *  Zen Thermostat
 *
 *  Author: Zen Within
 *  Date: 2015-02-21
 */
metadata {
  definition (name: "Zen Thermostat", namespace: "zenwithin", author: "ZenWithin") {
    capability "Actuator"
    capability "Thermostat"
    capability "Temperature Measurement"
    capability "Configuration"
    capability "Refresh"
    capability "Sensor"
    
    fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0001,0003,0004,0005,0020,0201,0202,0204,0B05", outClusters: "000A, 0019"
    
    //attribute "temperatureUnit", "number"
      
    command "setpointUp"
    command "setpointDown"
    
    command "setCelsius"
    command "setFahrenheit"
    
    // To please some of the thermostat SmartApps
    command "poll"
  }

  // simulator metadata
  simulator { }

  tiles {
        valueTile("frontTile", "device.temperature", width: 1, height: 1) {
            state("temperature", label:'${currentValue}°', backgroundColor:"#e8e3d8")
        }
    
        valueTile("temperature", "device.temperature", width: 1, height: 1) {
            state("temperature", label:'${currentValue}°', backgroundColor:"#0A1E2C")
        }
        
        standardTile("fanMode", "device.thermostatFanMode", decoration: "flat") {
            state "fanAuto", action:"thermostat.setThermostatFanMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.fan-auto"
            state "fanOn", action:"thermostat.setThermostatFanMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.fan-on"
        }
        
        
        standardTile("mode", "device.thermostatMode", decoration: "flat") {
            state "off", action:"setThermostatMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.heating-cooling-off", nextState:"heating"
            state "heat", action:"setThermostatMode", backgroundColor:"#ff6e7e", icon:"st.thermostat.heat", nextState:"cooling"
            state "cool", action:"setThermostatMode", backgroundColor:"#90d0e8", icon:"st.thermostat.cool", nextState:"..."
            //state "auto", action:"setThermostatMode", backgroundColor:"#e8e3d8", icon:"st.thermostat.auto"
            state "heating", action:"setThermostatMode", nextState:"to_cool"
            state "cooling", action:"setThermostatMode", nextState:"..."
            state "...", action:"off", nextState:"off"
        }
        
        valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 2) {
            state "off", label:'${currentValue}°', unit: "C", backgroundColor:"#e8e3d8"
            state "heat", label:'${currentValue}°', unit: "C", backgroundColor:"#e8e3d8"
            state "cool", label:'${currentValue}°', unit: "C", backgroundColor:"#e8e3d8"
        }
        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false) {
			state "heat", label:'${currentValue}° heat', unit:"F", backgroundColor:"#ffffff"
		}
        valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false) {
			state "cool", label:'${currentValue}° cool', unit:"F", backgroundColor:"#ffffff"
		}
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false) {
            state "heating", backgroundColor:"#ff6e7e"
            state "cooling", backgroundColor:"#90d0e8"
            state "fan only", backgroundColor:"#e8e3d8"
		}
        standardTile("setpointUp", "device.thermostatSetpoint", decoration: "flat") {
            state "setpointUp", action:"setpointUp", icon:"st.thermostat.thermostat-up"
        }
        
        standardTile("setpointDown", "device.thermostatSetpoint", decoration: "flat") {
            state "setpointDown", action:"setpointDown", icon:"st.thermostat.thermostat-down"
        }

        standardTile("refresh", "device.temperature", decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        standardTile("configure", "device.configure", decoration: "flat") {
            state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
        }

      main "frontTile"
      details(["temperature", "fanMode", "mode", "thermostatSetpoint", "setpointUp", "setpointDown","refresh", "configure"])
  }
}


// parse events into attributes
def parse(String description) {
    log.debug "Parse description $description"
    def map = [:]
    def activeSetpoint = "--"
    
    if (description?.startsWith("read attr -")) 
    {
        def descMap = parseDescriptionAsMap(description)
        // Thermostat Cluster Attribute Read Response
        if (descMap.cluster == "0201" && descMap.attrId == "0000") 
        {
        	log.debug "LOCAL TEMPERATURE"
            map.name = "temperature"
            map.value = getTemperature(descMap.value)
            def receivedTemperature = map.value
        } 
        else if (descMap.cluster == "0201" && descMap.attrId == "001c") 
        {
            map.name = "thermostatMode"
            map.value = getModeMap()[descMap.value]
            if (map.value == "cool") {
            	activeSetpoint = device.currentValue("coolingSetpoint")
            } else if (map.value == "heat") {
            	activeSetpoint = device.currentValue("heatingSetpoint")
            }
            sendEvent("name":"thermostatSetpoint", "value":activeSetpoint)
       } 
        else if (descMap.cluster == "0201" && descMap.attrId == "0011") 
        {
        	log.debug "COOL SET POINT"
            map.name = "coolingSetpoint"
            map.value = getTemperature(descMap.value)
            if (device.currentState("thermostatMode")?.value == "cool") {
            	activeSetpoint = map.value
                log.debug "Active set point value: $activeSetpoint"
            	sendEvent("name":"thermostatSetpoint", "value":activeSetpoint)
            }
        } 
        else if (descMap.cluster == "0201" && descMap.attrId == "0012") 
        {
        	log.debug "HEAT SET POINT"
            map.name = "heatingSetpoint"
            map.value = getTemperature(descMap.value)
            if (device.currentState("thermostatMode")?.value == "heat") {
            	activeSetpoint = map.value
            	sendEvent("name":"thermostatSetpoint", "value":activeSetpoint)
           }
        }
        else if (descMap.cluster == "0201" && descMap.attrId == "0029") 
        {
        	log.debug "OPERATING STATE"
            map.name = "thermostatOperatingState"
            map.value = getOperatingStateMap()[descMap.value]
        }
        
        // Fan Control Cluster Attribute Read Response
        else if (descMap.cluster == "0202" && descMap.attrId == "0000") 
        {
            map.name = "thermostatFanMode"
            map.value = getFanModeMap()[descMap.value]
        } 
        
    }// End of Read Attribute Response
    
    /*else if (description?.startsWith("updated")) {
		configure()
	}
*/
    def result = null
    if (map) {
      result = createEvent(map)
    }
    log.debug "Parse returned $map"
    
    return result
}

// =============== Help Functions - Don't use log.debug in all these functins ===============
def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

def getModeMap() { [
	"00":"off",
	"03":"cool",
	"04":"heat"
]}

def getOperatingStateMap() { [
	"0000":"idle",
	"0001":"heating",
	"0002":"cooling",
	"0004":"fan only",
    "0005":"heating",
    "0006":"cooling",
    "0008":"heating",
    "0009":"heating",
    "000A":"heating",
    "000D":"heating",
    "0010":"cooling",
    "0012":"cooling",
    "0014":"cooling",
    "0015":"cooling"
]}

def getFanModeMap() { [
	"04":"fanOn",
	"05":"fanAuto"
]}

def getTemperatureDisplayModeMap() { [
    "00":"C",
    "01":"F"
]}


def getTemperature(value) 
{
    def decimalFormat = new java.text.DecimalFormat("#")
    def celsius = Integer.parseInt(value, 16) / 100.0 as Double
    def returnValue
    
    // Format to support decimal with one or two 
    decimalFormat.setMaximumFractionDigits(2)
    decimalFormat.setMinimumFractionDigits(1)

	returnValue = decimalFormat.format(celsius);

    log.debug "Temperature value in C: $returnValue"
  
  if(getTemperatureScale() == "F"){
    returnValue = decimalFormat.format(Math.round(celsiusToFahrenheit(celsius)*10)/10.0)

    log.debug "Temperature value in F: $returnValue"
  }
  
  return returnValue
}



// =============== Setpoints ===============
def setpointUp()
{
    def currentMode = device.currentState("thermostatMode")?.value
    def currentUnit = getTemperatureScale()
    
    // check if heating or cooling setpoint needs to be changed
    double nextLevel = device.currentValue("thermostatSetpoint") + 1.0
	log.debug "Next level: $nextLevel"

    // check the limits
    if(currentUnit == "C")
    {
    	if (currentMode == "cool")
        {
            if(nextLevel > 36.0)
            {
                nextLevel = 36.0
            }
        } else if (currentMode == "heat")
        {
        	if(nextLevel > 32.0)
            {
                nextLevel = 32.0
            }
        }
    }
    else //in degF unit
    {
    	if (currentMode == "cool")
        {
            if(nextLevel > 96.0)
            {
                nextLevel = 96.0
            }
        } else if (currentMode == "heat")
        {
            if(nextLevel > 89.0)
            {
                nextLevel = 89.0
            }        	
        }
    }
    
    log.debug "setpointUp() - mode: ${currentMode}  unit: ${currentUnit}  value: ${nextLevel}"
    
    setSetpoint(nextLevel)
} 

def setpointDown()
{
    def currentMode = device.currentState("thermostatMode")?.value
    def currentUnit = getTemperatureScale()
    
    // check if heating or cooling setpoint needs to be changed
    double nextLevel = device.currentValue("thermostatSetpoint") - 1.0
    
    // check the limits
    if (currentUnit == "C")
    {
    	if (currentMode == "cool")
        {
            if(nextLevel < 8.0)
            {
                nextLevel = 8.0
            }
        } else if (currentMode == "heat")
        {
        	if(nextLevel < 10.0)
            {
                nextLevel = 10.0
            }
        }
    }
    else  //in degF unit
    {
      	if (currentMode == "cool")
        {
            if (nextLevel < 47.0)
            {
                nextLevel = 47.0
            }
        } else if (currentMode == "heat")
        {
        	if (nextLevel < 50.0)
            {
                nextLevel = 50.0
            }
        }
    }

    log.debug "setpointDown() - mode: ${currentMode}  unit: ${currentUnit}  value: ${nextLevel}"
    
    setSetpoint(nextLevel)
} 


def setSetpoint(degrees) 
{
	def temperatureScale = getTemperatureScale()
    def currentMode = device.currentState("thermostatMode")?.value
	
    def degreesDouble = degrees as Double
	sendEvent("name":"thermostatSetpoint", "value":degreesDouble)
    log.debug "New set point: $degreesDouble"
	
	def celsius = (getTemperatureScale() == "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	if (currentMode == "cool") {
    	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x11 0x29 {" + hex(celsius*100.0) + "}"
    }
    else if (currentMode == "heat") {
    	
   		"st wattr 0x${device.deviceNetworkId} 1 0x201 0x12 0x29 {" + hex(celsius*100.0) + "}"
	}    
}

def setHeatingSetpoint(degrees) {
	def temperatureScale = getTemperatureScale()
	
	def degreesDouble = degrees as Double
	log.debug "setHeatingSetpoint({$degreesDouble} ${temperatureScale})"
	sendEvent("name":"heatingSetpoint", "value":degreesDouble)
	
	def celsius = (temperatureScale == "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x12 0x29 {" + hex(celsius*100.0) + "}"
}

def setCoolingSetpoint(degrees) {
	def temperatureScale = getTemperatureScale()
    
	def degreesDouble = degrees as Double
	log.debug "setCoolingSetpoint({$degreesDouble} ${temperatureScale})"
	sendEvent("name":"coolingSetpoint", "value":degreesDouble)
    
	def celsius = (temperatureScale == "C") ? degreesDouble : (fahrenheitToCelsius(degreesDouble) as Double).round(1)
	"st wattr 0x${device.deviceNetworkId} 1 0x201 0x11 0x29 {" + hex(celsius*100.0) + "}"
}

// =============== Thermostat Mode ===============
def modes() {
  ["off", "heat", "cool"]
}

def setThermostatMode() 
{
    def currentMode = device.currentState("thermostatMode")?.value
    def modeOrder = modes()
    def index = modeOrder.indexOf(currentMode)
    def next = index >= 0 && index < modeOrder.size() - 1 ? modeOrder[index + 1] : modeOrder[0]
    log.debug "setThermostatMode - switching from $currentMode to $next"
    "$next"()
}

def setThermostatMode(String value) {
    "$value"()
}

def off() {
    sendEvent("name":"thermostatMode", "value":"off")
    sendEvent("name":"thermostatSetpoint","value":"--")
    "st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {00}"
}

def cool() {
	def coolingSetpoint = device.currentValue("coolingSetpoint")
    log.debug "Cool set point: $coolingSetpoint"
    sendEvent("name":"thermostatMode", "value":"cool")
    sendEvent("name":"thermostatSetpoint","value":coolingSetpoint)
    [
      "st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {03}"
    ]
}

def heat() {
	def heatingSetpoint = device.currentValue("heatingSetpoint")
    log.debug "Heat set point: $heatingSetpoint"
    sendEvent("name":"thermostatMode","value":"heat")
    sendEvent("name":"thermostatSetpoint","value":heatingSetpoint)
    [
      "st wattr 0x${device.deviceNetworkId} 1 0x201 0x1C 0x30 {04}"
    ]
}

// =============== Fan Mode ===============
def setThermostatFanMode() 
{
    def currentFanMode = device.currentState("thermostatFanMode")?.value
    def returnCommand

    switch (currentFanMode) {
    case "fanAuto":
      returnCommand = fanOn()
      break
    case "fanOn":
      returnCommand = fanAuto()
      break
    }
    
    if(!currentFanMode) { 
      returnCommand = fanAuto() 
    }
  
    log.debug "setThermostatFanMode - switching from $currentFanMode to $returnCommand"
    
    returnCommand
}

def setThermostatFanMode(String value) {
    "$value"()
}

def on() {
    fanOn()
}

def fanOn() {
    sendEvent("name":"thermostatFanMode", "value":"fanOn")
    "st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {04}"
}

def auto() {
    fanAuto()
}

def fanAuto() {
    sendEvent("name":"thermostatFanMode", "value":"fanAuto")
    "st wattr 0x${device.deviceNetworkId} 1 0x202 0 0x30 {05}"
}




// =============== SmartThings Default Fucntions: refresh, configure, poll ===============
def refresh()
{
    log.debug "refresh() - update attributes "
    [

        //Set long poll interval to 2 qs
        "raw 0x0020 {11 00 02 02 00 00 00}", 
        "send 0x${device.deviceNetworkId} 1 1", "delay 500",

        //This is sent in this specific order to ensure that the temperature values are received after the unit/mode
        "st rattr 0x${device.deviceNetworkId} 1 0x201 0x1C", "delay 800",
        "st rattr 0x${device.deviceNetworkId} 1 0x201 0", "delay 800",
        
        "st rattr 0x${device.deviceNetworkId} 1 0x201 0x11", "delay 800",
        "st rattr 0x${device.deviceNetworkId} 1 0x201 0x12", "delay 800",
        "st rattr 0x${device.deviceNetworkId} 1 0x201 0x29", "delay 800",
        "st rattr 0x${device.deviceNetworkId} 1 0x202 0", "delay 800",

        //Set long poll interval to 28 qs (7 seconds)
        "raw 0x0020 {11 00 02 1C 00 00 00}", 
        "send 0x${device.deviceNetworkId} 1 1"
    ]
}

def poll()
{
	refresh()
}

def configure() 
{
    log.debug "configure() - binding & attribute report"
    [
          //Set long poll interval to 2 qs
          "raw 0x0020 {11 00 02 02 00 00 00}", 
          "send 0x${device.deviceNetworkId} 1 1", "delay 500",
          
      	  //Thermostat - Cluster 201
          "zdo bind 0x${device.deviceNetworkId} 1 1 0x201 {${device.zigbeeId}} {}", "delay 500",
          
          "zcl global send-me-a-report 0x201 0 0x29 5 300 {3200}", 
          "send 0x${device.deviceNetworkId} 1 1", "delay 500",
          
          "zcl global send-me-a-report 0x201 0x0011 0x29 5 300 {3200}", 
          "send 0x${device.deviceNetworkId} 1 1", "delay 500",
          
          "zcl global send-me-a-report 0x201 0x0012 0x29 5 300 {3200}", 
          "send 0x${device.deviceNetworkId} 1 1", "delay 500",
          
          "zcl global send-me-a-report 0x201 0x001C 0x30 5 300 {}", 
          "send 0x${device.deviceNetworkId} 1 1", "delay 500",
          
          "zcl global send-me-a-report 0x201 0x0029 0x19 5 300 {}", 
          "send 0x${device.deviceNetworkId} 1 1", "delay 500",
    
          //Fan Control - Cluster 202
          "zdo bind 0x${device.deviceNetworkId} 1 1 0x202 {${device.zigbeeId}} {}", "delay 500",
          
          "zcl global send-me-a-report 0x202 0 0x30 5 300 {}", 
          "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    
          ] + refresh()
}



private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}
