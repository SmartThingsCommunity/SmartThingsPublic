/**
 *  Copyright 2020 Brad Sileo
 *
 *  Pool Controller - Body
 *
 *  Author: Brad Sileo
 *
 *
 *  version: 0.9.2
 */

metadata {
	definition (name: "Pool Controller Body", namespace: "bsileo", author: "Brad Sileo") {

       capability "Refresh"
       capability "Configuration"
       capability "Switch"
       capability "TemperatureMeasurement"

       command "heaterOn"
       command "heaterOff"
       command "nextHeaterMode"

       attribute "setPoint", "Number"
       attribute "heatMode", "String"
       if (isHE) {
           command "setHeaterMode", [[name:"Heater mode*",
                                      "type":"ENUM",
                                      "description":"Heater mode to set",
                                      "constraints":["Off", "Heater", ,"Solar Pref","Solar Only"]]]

           command "setHeaterSetPoint", [[name:"Heater SetPoint*",
                                      "type":"ENUM",
                                      "description":"Set the heater set point",
                                      "constraints":[50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,82,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104]
                                     ]]
       } else {
           // ST version of commands goes here

       }

    }

	preferences {
         section("General:") {
            input (
        	name: "configLoggingLevelIDE",
        	title: "IDE Live Logging Level:\nMessages with this level and higher will be logged to the IDE.",
        	type: "enum",
        	options: [
        	    "None",
        	    "Error",
        	    "Warning",
        	    "Info",
        	    "Debug",
        	    "Trace"
        	],
        	defaultValue: "Info",
            displayDuringSetup: true,
        	required: false
            )
        }
    }

     tiles (scale:1) {
            valueTile("temperature","temperature", height:2,width:2) {
            	state("temperature", label:'${currentValue} °F',
				backgroundColors:[
							[value: 32, color: "#ed310c"],
                            [value: 45, color: "#ed745c"],
							[value: 55, color: "#edad0c"],
							[value: 65, color: "#c9cf61"],
							[value: 75, color: "#75c987"],
							[value: 85, color: "#61eb34"],
                            [value: 95, color: "#61eb34"]
                     ]
				)
            }
            standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
            	state "off", label: '${currentValue}', action: "switch.on",
                  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            	state "on", label: '${currentValue}', action: "switch.off",
                  icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
        }
            valueTile("setPoint","setPoint", height:1,width:1) { state("default", label:'Set Point: ${currentValue} °F') }


            standardTile("heatMode", "heatMode", width:1, height:1, inactiveLabel: false, decoration: "flat") {
				state "Off",  action:"nextHeaterMode",  nextState: "updating", icon: "st.thermostat.heating-cooling-off", label: "Heater"
				state "Heater", action:"nextHeaterMode", nextState: "updating", icon: "st.thermostat.heat"
        		state "Solar Only", label:'${currentValue}', action:"nextHeaterMode",  nextState: "updating", icon: "https://bsileo.github.io/SmartThings_Pentair/solar-only.png"
            	state "Solar Preferred", label:'${currentValue}', action:"nextHeaterMode",  nextState: "updating", icon: "https://bsileo.github.io/SmartThings_Pentair/solar-preferred.jpg"
				state "updating", label:"Updating...", icon: "st.Home.home1"
		}
            standardTile("refresh", "device.refresh", height:1,width:1,inactiveLabel: false) {
                state "default", label:'Refresh', action:"refresh.refresh",  icon:"st.secondary.refresh-icon"
        	}
            valueTile("dummy", "temperature", height:1,width:1,inactiveLabel: false ) {}
            
        main "temperature"
        details "temperature", "switch", "setPoint", "heatMode", "refresh"

     }


}

def configure() {
  logger( "Executing 'configure()'","info")
  state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Info'
}

def installed() {
    state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Info'
    getHubPlatform()
}

def updated() {
  state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Info'
}


def parse(body) {
    logger("Parse body - ${body}","trace")
    sendEvent([name: "setPoint", value: body.setPoint])
    if (body.heatMode instanceof java.lang.Integer) {
        sendEvent([name: "heatMode", value: body.heatMode == 1 ? "Heater" : "Off"])
    } else {
        sendEvent([name: "heatMode", value: body.heatMode.desc])
    }
    String unit = "°${location.temperatureScale}"
    if (body.containsKey('isOn')) { sendEvent([name: "switch", value: body.isOn ? "on" : "off" ]) }
    if (body.containsKey('temp')) { sendEvent([name: "temperature", value: body.temp.toInteger(), unit: unit]) }
}

def refresh() {
    logger("Requested a refresh","info")
    def body = null
    sendGet("/config/options/bodies", 'parseRefresh', body, data)
    sendGet("/state/temps", 'parseRefresh', body, data)
}

def parseRefresh (response, data=null) {
    logger("body.parseRefresh - ${response.json}","debug")
    def bodies = response.json.bodies
    if (bodies) {
        parseBodies(bodies)
    }
}

def parseBodies(bodies) {
    logger("parseBodies - ${bodies}","debug")
    bodies.each {
    	logger("Current body - ${it} id=${it.id}-","debug")
        // logger("${it.id.toInteger()} ===?== ${getDataValue('bodyID').toInteger()} --- ${it.id.toInteger() == getDataValue('bodyID').toInteger()}","trace")
        if (it.id.toInteger() == getDataValue('bodyID').toInteger()) {
            parse(it)
        }
    }
}

def getHeatMode(intModeValue) {
    if (intModeValue == 1) { return "Heat" }
    else if (intModeValue == 0) { return "Off" }
}

def getHeatModeID(mode) {
    switch(mode) {
         case "Off":
            return 0
            break;
         case "Heater":
            return 1
            break;
         case "Solar Pref":
            return 2
            break;
         case "Solar":
            return 4
            break;
         default:
            return 0
            logger("Unknown Heater mode - ${mode}","error")
            break;
      }
}

def nextHeaterMode() {
	logger("Going to nextMode()","debug")
    def currentMode = device.currentValue("heatMode")
	def supportedModes = getModeMap()
    def nextIndex = 0;
    logger("${currentMode} moving to next in ${supportedModes}","debug")
    supportedModes.eachWithIndex {name, index ->
    	//log.debug("${index}:${name} -->${nextIndex}  ${name} == ${currentMode}")
    	if (name == currentMode) {
        	nextIndex = index + 1
            return
         }
    }
    logger("nextMode id=${nextIndex}  compare to " + supportedModes.size(),"debug")
    if (nextIndex >= supportedModes.size()) {nextIndex=0 }
    log.info("Going to nextMode with id =${nextIndex}  -- ${supportedModes[nextIndex]}")
    setHeaterMode(supportedModes[nextIndex])
}

def getModeMap() {
    def mm = null
    logger("TODO-fix detecting if Solar is present on Body ModeMap","debug")
    if (true) {
    	mm =  ["Off",
            "Heater",
        	"Solar Preferred",
        	"Solar Only"
     	]
    }
    else {
     mm =
    	[
        "OFF",
        "Heater"
     	]
    }
    return mm
}


def getChildDNI(name) {
	return getParent().getChildDNI(getDataValue("bodyID") + "-" + name)
}


def on() {
    def id = getDataValue("circuitID")
    def body = [id: id, state: 1]
    logger("Turn on body with ${params} - ${body}","debug")
    sendPut("/state/circuit/setState", 'stateChangeCallback', body, data  )
    sendEvent(name: "switch", value: "on", displayed:false,isStateChange:false)
}

def off() {
    def id = getDataValue("circuitID")
    def body = [id: id, state: 0]
    logger("Turn off body with ${params}","debug")
    sendPut("/state/circuit/setState", 'stateChangeCallback', body, data  )
    sendEvent(name: "switch", value: "off", displayed:false,isStateChange:false)
}

def stateChangeCallback(response, data) {
    logger("State Change Response ${response.getStatus() == 200 ? 'Success' : 'Failed'}","info")
    logger("State Change Response ${response.getStatus()}","debug")
}

// **********************************
// Heater control functions to update the current heater state / setpoints on the poolController.
// spdevice is the child device with the correct DNI to use in referecing SPA or POOL
// **********************************
def heaterOn(spDevice) {
  setHeaterMode("Heater")
}

def heaterOff(spDevice) {
  setHeaterMode("Off")
}

def setHeaterMode(mode) {
   def id = getDataValue("bodyID")
   def body = [id: id, mode: getHeatModeID(mode)]
    logger("Set Body heatMode with ${params} and ${body}","debug")
    sendPut("/state/body/heatMode", 'setModeCallback', body, data )
    sendEvent(name: "heatMode", value: mode)
}

def setModeCallback(response, data=null) {
    logger("Set Mode Response ${response.getStatus()}==${response.getJson()}","trace")
    logger("Set Mode Data ${data}","trace")
}


def setHeaterSetPoint(setPoint) {
    def id = getDataValue("bodyID")
    def body = [id: id, setPoint: setPoint]
    logger("Set Body setPoint with ${params} to ${data}","debug")
    sendPut("/state/body/setPoint", 'setPointCallback', body, data  )
    sendEvent(name: "setPoint", value: setPoint)
}

def setPointCallback(response, data=null) {
    logger("State Change Response ${response.getStatus()}","trace")
    logger("State Change Data ${data}","trace")
}



// **********************************
// INTERNAL Methods
// **********************************
def addHESTChildDevice(namespace, deviceType, dni, options  ) {
	if (state.isHE) {
    	return addChildDevice(namespace, deviceType, dni, options)
	} else {
    	return addChildDevice(namespace, deviceType, dni, location.hubs[0]?.id, options)
    }
}

// INTERNAL Methods
private getHost() {
    return getParent().getHost()
}

def getControllerURI(){
    def host = getHost()
    return "http://${host}"
}

private sendGet(message, aCallback=generalCallback, body="", data=null) {
    def params = [
        uri: getControllerURI(),
        path: message,
        requestContentType: "application/json",
        contentType: "application/json",
        body:body
    ]
    logger("Send GET to with ${params} CB=${aCallback}","debug")
    if (state.isST) {
    	 def hubAction = physicalgraph.device.HubAction.newInstance(
               [
                method: "GET",
                path: message,
                body: body,
                headers: [
                    HOST: getHost(),
                    "Accept":"application/json"
                    ]
               ],
               null,
               [
                callback : aCallback,
                type: 'LAN_TYPE_CLIENT'
               ])
        sendHubCommand(hubAction)
    } else {
        asynchttpGet(aCallback, params, data)
    }
}

private sendPut(message, aCallback=generalCallback, body="", data=null) {
    logger("Send PUT to ${message} with ${params} and ${aCallback}","debug")
    if (state.isST) {
        def hubAction = physicalgraph.device.HubAction.newInstance(
               [
                method: "PUT",
                path: message,
                body: body,
                headers: [
                    HOST: getHost(),
                    "Accept":"application/json"
                    ]
               ],
               null,
               [
                callback : aCallback,
                type: 'LAN_TYPE_CLIENT'
               ])
        sendHubCommand(hubAction)
    } else {
     	def params = [
        	uri: getControllerURI(),
        	path: message,
        	requestContentType: "application/json",
        	contentType: "application/json",
        	body:body
    	]
        asynchttpPut(aCallback, params, data)
    }

}

def generalCallback(response, data) {
   logger("Callback(status):${response.getStatus()}","debug")
}



def toIntOrNull(it) {
   return it?.isInteger() ? it.toInteger() : null
 }


//*******************************************************
//*  logger()
//*
//*  Wrapper function for all logging.
//*******************************************************

private logger(msg, level = "debug") {
	    
    def lookup = [
        	    "None" : 0,
        	    "Error" : 1,
        	    "Warning" : 2,
        	    "Info" : 3,
        	    "Debug" : 4,
        	    "Trace" : 5]
     def logLevel = lookup[state.loggingLevelIDE ? state.loggingLevelIDE : 'Debug']
     // log.debug("Lookup is now ${logLevel} for ${state.loggingLevelIDE}")  	

    switch(level) {
        case "error":
            if (logLevel >= 1) log.error msg
            break

        case "warn":
            if (logLevel >= 2) log.warn msg
            break

        case "info":
            if (logLevel >= 3) log.info msg
            break

        case "debug":
            if (logLevel >= 4) log.debug msg
            break

        case "trace":
            if (logLevel >= 5) log.trace msg
            break

        default:
            log.debug msg
            break
    }
}

// **************************************************************************************************************************
// SmartThings/Hubitat Portability Library (SHPL)
// Copyright (c) 2019, Barry A. Burke (storageanarchy@gmail.com)
//
// The following 3 calls are safe to use anywhere within a Device Handler or Application
//  - these can be called (e.g., if (getPlatform() == 'SmartThings'), or referenced (i.e., if (platform == 'Hubitat') )
//  - performance of the non-native platform is horrendous, so it is best to use these only in the metadata{} section of a
//    Device Handler or Application
//
private String  getPlatform() { (physicalgraph?.device?.HubAction ? 'SmartThings' : 'Hubitat') }	// if (platform == 'SmartThings') ...
private Boolean getIsST()     { (physicalgraph?.device?.HubAction ? true : false) }					// if (isST) ...
private Boolean getIsHE()     { (hubitat?.device?.HubAction ? true : false) }						// if (isHE) ...
//
// The following 3 calls are ONLY for use within the Device Handler or Application runtime
//  - they will throw an error at compile time if used within metadata, usually complaining that "state" is not defined
//  - getHubPlatform() ***MUST*** be called from the installed() method, then use "state.hubPlatform" elsewhere
//  - "if (state.isST)" is more efficient than "if (isSTHub)"
//
private String getHubPlatform() {
    if (state?.hubPlatform == null) {
        state.hubPlatform = getPlatform()						// if (hubPlatform == 'Hubitat') ... or if (state.hubPlatform == 'SmartThings')...
        state.isST = state.hubPlatform.startsWith('S')			// if (state.isST) ...
        state.isHE = state.hubPlatform.startsWith('H')			// if (state.isHE) ...
    }
    return state.hubPlatform
}
private Boolean getIsSTHub() { (state.isST) }					// if (isSTHub) ...
private Boolean getIsHEHub() { (state.isHE) }