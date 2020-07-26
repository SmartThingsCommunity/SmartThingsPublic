/**
 *  Copyright 2020 Brad Sileo
 *
 *  Pool Controller Intellichem
 *
 *  Author: Brad Sileo
 *
 *
 *  version: 0.9.1
 */
metadata {
	definition (name: "Pool Controller Intellichem", namespace: "bsileo", author: "Brad Sileo" )
        {
		capability "Refresh"
        capability "pHMeasurement"

		attribute "ORP", "string"
        attribute "waterFlow", "string"
		attribute "salt", "string"
		attribute "tank1Level", "string"
		attribute "tank2Level", "string"
        attribute "status1", "string"
        attribute "status2", "string"
        attribute "CYA", "string"
		attribute "CH", "string"
        attribute "TA", "string"
        attribute "SI", "string"

		command "refresh"

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

    if (isST) {
        tiles (scale:2) {
            valueTile("pH", "device.pH", width:1, height:1, decoration:"flat") {
                state("pH", label:'pH ${currentValue}',
                backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 7.0, color: "#44b621"],
                    [value: 7.2, color: "#f1d801"],
                    [value: 7.4, color: "#d04e00"],
                    [value: 7.5, color: "#bc2323"]
                ]
            )
            }

            valueTile("ORP", "ORP", width:1, height:1, decoration:"flat") {
            state("ORP", label:'${currentValue}',
                backgroundColors:[
                    [value: 0, color: "#153591"],
                    [value: 700, color: "#44b621"],
                    [value: 720, color: "#f1d801"],
                    [value: 740, color: "#d04e00"],
                    [value: 750, color: "#bc2323"]
                ]
            )
            }

            valueTile("waterFlow","waterFlow",width:1, height:1, decoration:"flat")  {
                state("default", label:'${currentValue}')
            }

            valueTile("SI","SI",width:1, height:1, decoration:"flat")  {
                state("SI", label:'SI: ${currentValue}')
            }

            valueTile("CYA","CYA",width:1, height:1, decoration:"flat")  {
                state("CYA", label:'CYA: ${currentValue}')
            }


            valueTile("TA","TA",width:1, height:1, decoration:"flat")  {
                state("default", label:'TA: ${currentValue}')
            }

            valueTile("salt","salt",width:1, height:1, decoration:"flat")  {
                state("default", label:'Salt: ${currentValue}')
            }

            valueTile("tank1Level","tank1Level",width:1, height:1, decoration:"flat")  {
                state("default", label:'Tank 1: ${currentValue}')
            }

            valueTile("tank2Level","tank2Level",width:1, height:1, decoration:"flat")  {
                state("default", label:'Tank 2: ${currentValue}')
            }

            valueTile("status1","status1",width:1, height:1, decoration:"flat")  {
                state("default", label:'Status 1: ${currentValue}')
            }

            valueTile("status2","status2",width:1, height:1, decoration:"flat")  {
                state("default", label:'Status 2: ${currentValue}')
            }

            standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
                state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
            }
        }
    }

}

def installed() {
    state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Debug'
    getHubPlatform()
}

def updated() {
    state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Debug'
}

def initialize() {
}

def refresh() {
    logger("Requested a refresh","info")
	def body = null
    def data = null
    logger("Refresh Intellichem with ${params} - ${data}","debug")
    sendGet("/config/intellichem",'parseRefresh', body, data)
    sendGet("/state/intellichem",'parseRefresh', body, data)
}

def parseRefresh (response, data=null) {
     if (response.getStatus() == 200) {
        def json = response.getJson()
	    json.each { key, v ->
            switch (key) {
                case "ph":
                    sendEvent(name: "pH", value: v)
                    break;
                case "ORP":
                    sendEvent(name: "ORP", value: v)
                    break;
                case "waterFlow":
                    val = v ? "NO FLOW": "Flow OK"
                    sendEvent(name: "flowAlarm", value: val)
                    break;
                 case "salt":
                    sendEvent(name: "salt", value: v)
                    break;
                case "tank1Level":
                    sendEvent(name: "tank1Level", value: v)
              	    break;
                case "tank2Level":
                    sendEvent(name: "tank2Level", value: v)
              	    break;
                case "status1":
                    sendEvent(name: "status1", value: v)
              	    break;
                case "status2":
                    sendEvent(name: "status2", value: v)
              	    break;
                // Start of "STATE" items
                case "CYA":
                    sendEvent(name: "CYA", value: v)
                    break;
                case "CH":
                    sendEvent(name: "CH", value: v)
                    break;
                case "TA":
                    sendEvent(name: "TA", value: v)
                    break;
                case "SI":
                    sendEvent(name: "SI", value: v)
                    break;
             }
        }
     } else {
         logger("Failed to refresh from server - ${response.getStatus()}","error")
         logger("Error data is ${response.getErrorMessage()}","error")
     }
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
