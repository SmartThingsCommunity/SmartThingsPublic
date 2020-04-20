import groovy.json.JsonOutput
metadata {
	definition (name: "AC Remote", namespace: "smartthings", author: "Naveen Kumar") {
		capability "Actuator"
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Health Check"
        capability "Polling"

		command "generateEvent"
		command "resumeProgram"
		command "switchMode"
        command "powerMode"
		command "switchFanMode"
		command "lowerHeatingSetpoint"
		command "raiseHeatingSetpoint"
		// To satisfy some SA/rules that incorrectly using poll instead of Refresh
		command "poll"

		attribute "thermostat", "string"
		attribute "maxHeatingSetpoint", "number"
		attribute "minHeatingSetpoint", "number"
		attribute "deviceTemperatureUnit", "string"
		attribute "deviceAlive", "enum", ["true", "false"]
	}

	tiles {
		multiAttributeTile(name:"temperature", type:"generic", width:3, height:2, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', icon: "st.Home.home1",
					backgroundColors:[
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
		}
		standardTile("lowerHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"lowerHeatingSetpoint", icon:"https://b1hub.github.io/images/smartthings/down.png"
		}
		standardTile("raiseHeatingSetpoint", "device.heatingSetpoint", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpoint", action:"raiseHeatingSetpoint", icon:"https://b1hub.github.io/images/smartthings/up.png"
		}
		
		standardTile("mode", "device.thermostatMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "auto", action:"switchMode",  nextState: "updating", icon: "https://b1hub.github.io/images/smartthings/auto.png"
            state "cool", action:"switchMode", nextState: "updating", icon: "https://b1hub.github.io/images/smartthings/cool.png"
            state "dry", action:"switchMode", nextState: "updating", icon: "https://b1hub.github.io/images/smartthings/dry.png"
            state "fan", action:"switchMode",  nextState: "updating", icon: "https://b1hub.github.io/images/smartthings/fanm.png"
			state "heat", action:"switchMode",  nextState: "updating", icon: "https://b1hub.github.io/images/smartthings/heat.png"
			state "updating", label:"Updating...", icon: "st.secondary.secondary"
		}
		standardTile("fanMode", "device.thermostatFanMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "fauto", action:"switchFanMode", nextState: "updating" ,icon: "https://b1hub.github.io/images/smartthings/fauto.png"
			state "flow", action:"switchFanMode", nextState: "updating",icon: "https://b1hub.github.io/images/smartthings/flow.png"
            state "fmid", action:"switchFanMode", nextState: "updating" ,icon: "https://b1hub.github.io/images/smartthings/fmed.png"
            state "fhigh", action:"switchFanMode", nextState: "updating",icon: "https://b1hub.github.io/images/smartthings/fhigh.png"
			state "updating", label:"Updating...", icon: "st.secondary.secondary"
		}
        valueTile("thermostat", "device.acstatusvalue", width:6, height:2, decoration: "flat") {
			state "thermostat", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("powerMode", "device.thermostatPowerMode", width:2, height:2, inactiveLabel: false, decoration: "flat") {
			state "off", action:"powerMode", nextState: "updating", icon: "https://b1hub.github.io/images/smartthings/off.png"
            state "on", action:"powerMode", nextState: "updating", icon: "https://b1hub.github.io/images/smartthings/on.png"
			state "updating", label:"Updating...", icon: "st.secondary.secondary"
		}
    

		standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		main "temperature"
		details(["temperature",  "lowerHeatingSetpoint", "refresh", "raiseHeatingSetpoint",
				 "powerMode","mode", "fanMode","thermostat"
				])
	}


}



void installed() {
    // The device refreshes every 5 minutes by default so if we miss 2 refreshes we can consider it offline
    // Using 12 minutes because in testing, device health team found that there could be "jitter"
	initialize()
}
def initialize() {
	
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "cloud", scheme:"untracked"]), displayed: false)
	updateDataValue("EnrolledUTDH", "true")
}

def updated() {
	log.debug "updated()"
	//parent.setName(device.label, device.deviceNetworkId)
	initialize()
}

// Called when the DTH is uninstalled, is this true for cirrus/gadfly integrations?
// Informs parent to purge its associated data
def uninstalled() {
    log.debug "uninstalled() parent.purgeChildDevice($device.deviceNetworkId)"
    // purge DTH from parent
    //parent?.purgeChildDevice($device.deviceNetworkId)
}

def ping() {
	refresh()
	log.debug "ping() NOP"
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def refresh() {
	log.debug "refresh, calling parent poll"
    def deviceId =  device.currentValue("deviceID")
	parent.poll1(deviceId,"0")
     
}

void poll() {
	refresh()
	log.debug "poll not implemented as it is done by parent SmartApp every 5 minutes"
}


def generateEvent(Map results) {
log.debug("data chck for result:${results}")
	if(results) {
		def linkText = getLinkText(device)
		def supportedThermostatModes = ["cool","heat","auto","fan","dry"]
		def thermostatMode = null
		def locationScale = getTemperatureScale()

		results.each { name, value ->
			def event = [name: name, linkText: linkText, handlerName: name]
			def sendValue = value

			if (name=="temperature") {
				sendValue =  value  // API return temperature values in F
				event << [value: sendValue]
			}  else if (name=="heatMode" || name=="coolMode" || name=="autoMode" || name=="auxHeatMode"){
				return // as we don't want to send this event here, proceed to next name/value pair
			} else if (name=="thermostatFanMode"){
				sendEvent(name: "supportedThermostatFanModes", value: fanModes(), displayed: false)
				event << [value: value, data:[supportedThermostatFanModes: fanModes()]]
			} else if (name == "name") {
				return // as we don't want to send this event, proceed to next name/value pair
			}else if (name=="thermostatPowerMode"){
				sendEvent(name: "supportedThermostatPowerModes", value: powerModes(), displayed: false)
				event << [value: value, data:[supportedThermostatPowerModes: powerModes()]]
			} else if (name == "name") {
				return // as we don't want to send this event, proceed to next name/value pair
			}
            
            
            else {
				event << [value: value.toString()]
			}
			event << [descriptionText: getThermostatDescriptionText(name, sendValue, linkText), displayed: false]
			sendEvent(event)
		}
		if (state.supportedThermostatModes != supportedThermostatModes) {
			state.supportedThermostatModes = supportedThermostatModes
			sendEvent(name: "supportedThermostatModes", value: supportedThermostatModes, displayed: false)
		}
		
		generateSetpointEvent ()
		generateStatusEvent ()
	}
}

//return descriptionText to be shown on mobile activity feed
private getThermostatDescriptionText(name, value, linkText) {
	if(name == "temperature") {
		return "temperature is ${value}°c"

	} else if(name == "heatingSetpoint") {
		return "heating setpoint is ${value}°C"

	}  else if (name == "thermostatMode") {
		return "thermostat mode is ${value}"

	} else if (name == "thermostatFanMode") {
		return "thermostat fan mode is ${value}"

	}  else {
		return "${name} = ${value}"
	}
}


void resumeProgram() {
	log.debug "resumeProgram() is called"

	sendEvent("name":"thermostat", "value":"resuming schedule", "description":statusText, displayed: false)
	def deviceId = device.deviceNetworkId.split(/\./).last()
	if (parent.poll1(deviceId,"0")) {
		sendEvent("name":"thermostat", "value":"setpoint is updating", "description":statusText, displayed: false)
	} else {
		sendEvent("name":"thermostat", "value":"resume failed", "description":statusText, displayed: false)
		log.error "Error resumeProgram() check parent.resumeProgram(deviceId)"
	}
	// Prevent double tap and spamming of resume command
	runIn(5, "updateResume", [overwrite: true])
}

def updateResume() {
	sendEvent("name":"resumeProgram", "value":"resume", descriptionText: "resumeProgram is done", displayed: false, isStateChange: true)
	refresh()
}

def modes() {
	return state.supportedThermostatModes=["auto","cool","dry","fan","heat"]
}

def fanModes() {
	
	return ["fauto","flow","fmid","fhigh"]
}

def powerModes() {
	return ["on", "off"]
}


def powerMode(){

def currentPowerMode = device.currentValue("thermostatPowerMode")
    log.debug "Current Power Mode:${currentPowerMode}"
	def modeOrder = powerModes()
    log.debug "Current modeOrder:${modeOrder}"
	if (modeOrder) {
		def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
		def nextMode = next(currentPowerMode)
		switchToPowerMode(nextMode)
	} else {
		log.warn "supportedThermostatModes not defined"
	}

}
def switchToPowerMode(mode) {
def deviceId =  device.currentValue("deviceBID")
def deviceMId =  device.currentValue("deviceID")
def statushub=device.currentValue("deviceAlive")
	log.debug "switchToMode: ${mode} device_id:${deviceId} Status:${statushub}"
	
	// Thermostat's mode for "emergency heat" is "auxHeatOnly"
    if(statushub=="true"){
	if (!(parent.setPowerMode( mode, deviceId,deviceMId))) {
		log.warn "Error setting mode:$mode"
		// Ensure the DTH tile is reset
		mode = device.currentValue("thermostatPowerMode")
         refresh()
       
	}else
    {
    mode = device.currentValue("thermostatPowerMode")
     refresh()
    }
    }
    else
    {
    parent.messagePush("Your B.One Hub is Offline")
    }
    mode = device.currentValue("thermostatPowerMode")
	generatePowerModeEvent(mode)
	generateStatusEvent()
   
   
}


def switchMode() {
	def currentMode = device.currentValue("thermostatMode")
    log.debug "Current Mode:${currentMode}"
	def modeOrder = modes()
    log.debug "Current modeOrder:${modeOrder}"
	if (modeOrder) {
		def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
		def nextMode = next(currentMode)
		switchToMode(nextMode)
	} else {
		log.warn "supportedThermostatModes not defined"
	}
}

def switchToMode(mode) {

def powerMo=device.currentValue("thermostatPowerMode")
	log.debug "switchToMode: ${mode} ${powerMo}"
	def deviceId =  device.currentValue("deviceBID")
    def deviceMId =  device.currentValue("deviceID")
       def statushub=device.currentValue("deviceAlive")
    if(statushub=="true"){
    if(powerMo!="off")
{
	// Thermostat's mode for "emergency heat" is "auxHeatOnly"
	if (!(parent.setMode(mode,deviceId,deviceMId))) {
		log.warn "Error setting mode:$mode"
		// Ensure the DTH tile is reset
		mode = device.currentValue("thermostatMode")
	}else
    {
    mode = device.currentValue("thermostatMode")
     refresh()
    }
    mode = device.currentValue("thermostatMode")
	generateModeEvent(mode)
	generateStatusEvent()
    }
    else
    {
    log.debug "sCheck Mode ToMode: ${mode} ${powerMo}"
    mode = device.currentValue("thermostatMode")
    generateModeEvent(mode)
	generateStatusEvent()
    parent.messagePush("$device.displayName is Turned OFF. Please Turn ON and try again.")
    }
    }
    else
    {
    log.debug "sCheck Mode ToMode: ${mode} ${powerMo}"
    mode = device.currentValue("thermostatMode")
    
    generateModeEvent(mode)
	generateStatusEvent()
    parent.messagePush("Your B.One Hub is Offline")
    }
}


def switchFanMode() {
	def currentFanMode = device.currentValue("thermostatFanMode")
	def fanModeOrder = fanModes()
	def next = { fanModeOrder[fanModeOrder.indexOf(it) + 1] ?: fanModeOrder[0] }
	switchToFanMode(next(currentFanMode))
}


def switchPowerMode() {
	def currentFanMode = device.currentValue("thermostatPowerMode")
	def fanModeOrder = powerModes()
	def next = { fanModeOrder[fanModeOrder.indexOf(it) + 1] ?: fanModeOrder[0] }
	switchPowerMode(next(currentFanMode))
}

def switchToFanMode(fanMode) {
def powerMo=device.currentValue("thermostatPowerMode")
log.debug "switchToFanMode: ${fanMode} ${powerMo}"
def deviceId =  device.currentValue("deviceBID")
def deviceMId =  device.currentValue("deviceID")
  def statushub=device.currentValue("deviceAlive")
    if(statushub=="true"){
if(powerMo!="off")
{
if (!(parent.setFanMode(fanMode,deviceId,deviceMId))) {
		log.warn "Error setting fanMode:fanMode"
		// Ensure the DTH tile is reset
		fanMode = device.currentValue("thermostatFanMode")
	}else
    {
    fanMode = device.currentValue("thermostatFanMode")
     refresh()
    }
    fanMode = device.currentValue("thermostatFanMode")
	generateFanModeEvent(fanMode)
}
else
{
fanMode = device.currentValue("thermostatFanMode")
generateFanModeEvent(fanMode)
parent.messagePush("$device.displayName is Turned OFF. Please Turn ON and try again.")
}
}
else
{
fanMode = device.currentValue("thermostatFanMode")
generateFanModeEvent(fanMode)
parent.messagePush("Your B.One Hub is Offline")
}   
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def setThermostatMode(String mode) {
	log.debug "setThermostatMode($mode)"
	def supportedModes = modes()
	if (supportedModes) {
		mode = mode.toLowerCase()
		def modeIdx = supportedModes.indexOf(mode)
		if (modeIdx < 0) {
			log.warn("Thermostat mode $mode not valid for this thermostat")
			return
		}
		mode = supportedModes[modeIdx]
		switchToMode(mode)
	} else {
		log.warn "supportedThermostatModes not defined"
	}
}

def setThermostatFanMode(String mode) {
	log.debug "setThermostatFanMode($mode)"
	mode = mode.toLowerCase()
	def supportedFanModes = fanModes()
	def modeIdx = supportedFanModes.indexOf(mode)
	if (modeIdx < 0) {
		log.warn("Thermostat fan mode $mode not valid for this thermostat")
		return
	}
	mode = supportedFanModes[modeIdx]
	switchToFanMode(mode)
}
def setThermostatPowerMode(String mode) {
	log.debug "setThermostatFanMode($mode)"
	mode = mode.toLowerCase()
	def supportedFanModes = powerModes()
	def modeIdx = supportedFanModes.indexOf(mode)
	if (modeIdx < 0) {
		log.warn("Thermostat fan mode $mode not valid for this thermostat")
		return
	}
	mode = supportedFanModes[modeIdx]
	switchToPowerMode(mode)
}

def generateModeEvent(mode) {
def amode="Auto"
if(mode=="cool")
{
amode="Cool"
}
if(mode=="heat")
{
amode="Heat"
}
if(mode=="dry")
{
amode="Dry"
}
if(mode=="fan")
{
amode="Fan"
}


	sendEvent(name: "thermostatMode", value: mode, data:[supportedThermostatModes: modes()],
			isStateChange: true, descriptionText: "$device.displayName is set to ${amode}")
}
def generatePowerModeEvent(mode) {
def power="OFF"
if(mode=="on")
{
power="ON"
}
	sendEvent(name: "thermostatPowerMode", value: mode,
			isStateChange: true, descriptionText: "$device.displayName is Turned ${power}")
}

def generateFanModeEvent(fanMode) {
def fmode="Auto"
if(fanMode=="flow")
{
fmode="Low"
}
if(fanMode=="fmid")
{
fmode="Med"
}
if(fanMode=="fhigh")
{
fmode="High"
}

	sendEvent(name: "thermostatFanMode", value: fanMode, data:[supportedThermostatFanModes: fanModes()],
			isStateChange: true, descriptionText: "$device.displayName`s Fan is set to ${fmode}", displayed: true)
}

def generateOperatingStateEvent(operatingState) {
	sendEvent(name: "thermostatOperatingState", value: operatingState, descriptionText: "$device.displayName is ${operatingState}", displayed: true)
}

def off() { setThermostatPowerMode("off")}
def on() { setThermostatPowerMode("on")}


def heat() { setThermostatMode("heat") }
def cool() { setThermostatMode("cool") }
def auto() { setThermostatMode("auto") }
def dry() { setThermostatMode("dry") }
def fan() { setThermostatMode("fan") }

def fauto() { setThermostatFanMode("fauto") }
def flow() { setThermostatFanMode("flow") }
def fnmid() { setThermostatFanMode("fmid") }
def fhigh() { setThermostatFanMode("fhigh") }

// =============== Setpoints ===============
def generateSetpointEvent() {
	def mode = device.currentValue("thermostatMode")
	def setpoint = device.currentValue("temperature")  // (mode == "heat") || (mode == "emergency heat")
     sendEvent(name: "thermostatSetpoint", value: setpoint, descriptionText: "$device.displayName is set to ${setpoint}°C", displayed: true)
	//sendEvent("name":"thermostatSetpoint", "value":setpoint, "unit":"C",displayed: false)
   
}

def raiseHeatingSetpoint() {
  def statushub=device.currentValue("deviceAlive")
    if(statushub=="true"){
	alterSetpoint(true, "temperature")   
    }
    else
    {
    parent.messagePush("Your B.One Hub is Offline")
    }
}

def lowerHeatingSetpoint() {
	  def statushub=device.currentValue("deviceAlive")
    if(statushub=="true"){
	alterSetpoint(false, "temperature")   
    }
    else
    {
    parent.messagePush("Your B.One Hub is Offline")
    }
    
    
}



// Adjusts nextHeatingSetpoint either .5° C/1°F) if raise true/false
def alterSetpoint(raise, setpoint) {
	// don't allow setpoint change if thermostat is off
    def Tempstatus="0"
	if (device.currentValue("thermostatPowerMode") == "off") {
    parent.messagePush("$device.displayName is Turned OFF. Please Turn ON and try again.")
		return
	}
    else if(device.currentValue("thermostatMode")=="cool" || device.currentValue("thermostatMode")=="heat")
    {
    def temp = device.currentValue("temperature")
    def deviceId = device.currentValue("deviceBID")
    def deviceMId = device.currentValue("deviceID")
    def tempVal=""
    if(raise){
    tempVal=(temp.toInteger()-1).toString()
    }
    else
    {
     tempVal=(temp.toInteger()+1).toString()
    }
    if(tempVal.toInteger()>17 && tempVal.toInteger()<31)
    {
    tempVal=tempVal.toString()
    }
    else
    {
    if(tempVal.toInteger()<18)
    {
    Tempstatus="1"
    parent.messagePush("Can not set Temperature below 18°C.") 
    }
    if(tempVal.toInteger()>30)
    {
    Tempstatus="1"
    parent.messagePush("Can not set Temperature above 30°C.") 
    }
    }
    if(Tempstatus=="0")
    {
   parent.setTemperatueAc(tempVal,deviceId,deviceMId)
   }
    
    generateStatusEvent()
    def tempVal1 = device.currentValue("temperature")
    sendEvent("name": "temperature", "value":tempVal1 ,unit: locationScale, eventType: "ENTITY_UPDATE", displayed: false)
    refresh()
   
    }
    
	
}



def generateStatusEvent() {
	def mode = device.currentValue("thermostatMode")
	def temperature = device.currentValue("temperature")
	def statusText = "Right Now: Idle"
	def operatingState = "idle"

	if (mode == "heat") {
			statusText = "Heating to ${temperature}°C"
			operatingState = "heating"
	} else if (mode == "cool") {
		if (temperature > coolingSetpoint) {
			statusText = "Cooling to ${temperature}°C"
			operatingState = "cooling"
		}
	} else if (mode == "auto") {
		
			statusText = "auto to 25°C"
			operatingState = "auto"
		
	}else if (mode == "fan") {
		
			statusText = "fan to 25°C"
			operatingState = "fan"
		
	} else if (mode == "dry") {
		
			statusText = "dry to 25°C"
			operatingState = "dry"
		
	} else {
		statusText = "?"
	}

	sendEvent("name":"thermostat", "value":statusText, "description":statusText, displayed: false)
	sendEvent("name":"thermostatOperatingState", "value":operatingState, "description":operatingState, displayed: false)
}

def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

void setHeatingSetpoint(setpoint) {
log.debug "***setHeatingSetpoint($setpoint)"
	if (setpoint) {
    def tempVal=setpoint
    if(setpoint.toInteger()>17 && setpoint.toInteger()<31)
    {
    tempVal=setpoint
    }
    else
    {
    if(setpoint.toInteger()<18)
    {
    tempVal="18"
    }
    if(setpoint.toInteger()>30)
    {
    tempVal="30"
    }
    }
     def deviceId = device.currentValue("deviceBID")
    def deviceMId = device.currentValue("deviceID")
    parent.setActionsAC("heat",tempVal,deviceId,deviceMId)
		
	}
}

def setCoolingSetpoint(setpoint) {
log.debug "***setCoolingSetpoint($setpoint)"
	if (setpoint) {
		def tempVal=setpoint
    if(setpoint.toInteger()>17 && setpoint.toInteger()<31)
    {
    tempVal=setpoint
    }
    else
    {
    if(setpoint.toInteger()<18)
    {
    tempVal="18"
    }
    if(setpoint.toInteger()>30)
    {
    tempVal="30"
    }
    }
    def deviceId = device.currentValue("deviceBID")
    def deviceMId = device.currentValue("deviceID")
    parent.setActionsAC("cool",tempVal,deviceId,deviceMId)
	}
}
