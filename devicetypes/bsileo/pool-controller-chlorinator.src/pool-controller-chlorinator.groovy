/**
 *  Copyright 2020 Brad Sileo
 *
 *  Pool Controller Chlorinator
 *
 *  Author: Brad Sileo
 *
 *
 *  version: 0.9.3
 */
metadata {
	definition (name: "Pool Controller Chlorinator", namespace: "bsileo", author: "Brad Sileo" )
        {
		capability "Refresh"
        capability "Switch"

		attribute "saltLevel", "number"
        attribute "targetOutput", "number"
		attribute "currentOutput", "number"
        attribute "status", "string"
        attribute "saltRequired", "string"

		attribute "superChlorHours", "number"
        attribute "superChlor", "boolean"
		attribute "poolSetpoint", "number"
        attribute "spaSetpoint", "number"

         if (isHE) {
            command "setPoolSetpoint", [[name:"Pool Setpoint*",
                                      "type":"ENUM",
                                      "description":"Set the output level for the Pool",
                                      "constraints":[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
                                                     21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,
                                                     40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,
                                                     60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,
                                                     80,81,82,82,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,
                                                     100]
                                     ]]
            command "setSpaSetpoint", [[name:"Spa Setpoint*",
                                      "type":"ENUM",
                                      "description":"Set the output level for the Spa",
                                      "constraints":[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
                                                     21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,
                                                     40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,
                                                     60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,
                                                     80,81,82,82,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,
                                                     100]
                                     ]]

            command "setSuperChlorHours", [[name:"Super Chlor Status*",
                                        "type":"ENUM",
                                        "description":"Turn on/off Super Chlorinate",
                                        "constraints":["On","Off"]
                                        ],
                                        [name:"Super Chlor Hours*",
                                      "type":"ENUM",
                                      "description":"Set the number of hours for Super Chlorinate",
                                      "constraints":[0,1,2,3,4,5,6,7,8]
                                     ]]
         } else {
             command "setPoolSetpoint", ["number"]
             command "setSpaSetpoint", ["number"]
             command "setSuperChlorHours", ["boolean", "number"]
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
        	required: false
            )
        }
    }

    tiles (scale:1) {
            valueTile("saltLevel","saltLevel", height:2,width:2) {
            state("saltLevel", label:'${currentValue} ppm',
				backgroundColors:[
							[value: 0, color: "#ed310c"],
                            [value: 1000, color: "#ed745c"],
							[value: 2400, color: "#edad0c"],
							[value: 2800, color: "#c9cf61"],
							[value: 3000, color: "#75c987"],
							[value: 3200, color: "#61eb34"]
                     ]
				)
            }
            
            valueTile("superChlor","superChlor", height:1,width:1) {
            	state("false", label:'SuperChlor Off')
              	state("true", label:'SuperChlor On')
            }
            valueTile("superChlorHours","superChlorHour", height:1,width:1) { state("default", label:'${currentValue} hours') }
            valueTile("currentOutput","currentOutput", height:1,width:1) { state("default", label:'${currentValue}%') }
            valueTile("poolSetpoint","poolSetpoint", height:1,width:1) { state("default", label:'${currentValue}%') }
            valueTile("spaSetpoint","spaSetpoint", height:1,width:1) { state("default", label:'${currentValue}%') }
            valueTile("targetOutput","targetOutput", height:1,width:1) { state("default", label:'${currentValue}%') }
            valueTile("saltRequired","saltRequired", height:1,width:1) {
            	state("Yes", label:'Salt Required')
                state("No", label:'Salt Level OK')
                }
            valueTile("status","status", height:1,width:1) { state("default", label:'Status: ${currentValue}') }
            standardTile("refresh", "refresh", width:1, height:1, inactiveLabel: false, decoration: "flat") {
				state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
			}

    }
}


def configure() {
   state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Info'
}

def installed() {
   getHubPlatform()
   state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Info'
}

def updated() {
  getHubPlatform()
  state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Info'
}


def parse(msg) {
     sendEvent([name: "currentOutput", value: msg.currentOutput])
     sendEvent([name: "targetOutput", value: msg.targetOutput])
     sendEvent([name: "saltLevel", value: msg.saltLevel])
     sendEvent([name: "saltRequired", value: msg.saltRequired ? 'Yes' : 'No'])
     sendEvent([name: "poolSetpoint", value: msg.poolSetpoint])
     sendEvent([name: "spaSetpoint", value: msg.spaSetpoint])
     sendEvent([name: "superChlorHours", value: msg.superChlorHours])
     sendEvent([name: "superChlor", value: msg.superChlor ? true : false])
     sendEvent([name: "switch", value: msg.currentOutput>0 ? "on" : "off"])
     if (msg.status) {
         sendEvent([name: "status", value: msg.status.name, descriptionText: "Chlorinator status is ${msg.status.desc}"])
     }
}

// Command Implementations
def refresh() {
    logger("Requested a refresh","info")
    def id = getDataValue("chlorId")
    def body = ''
    def data = null
    logger("Refresh for ${id}","debug")
    sendGet("/state/chlorinator/${id}",'parseRefresh', body, data)
}

def parseRefresh (response, data=null) {
    logger("parseRefresh - ${response.json}","debug")
    try {
        def value = response.getJson()
        parse(value)
    }
    catch (e) {
        logger("Failed to refresh Chlorinator due to ${e}","error")
    }
}


def on() {
    return chlorinatorOn()
}

def off() {
   return chlorinatorOff()
}

def chlorinatorOn() {
   return chlorinatorUpdate(70,30,0)
}

def chlorinatorOff() {
   return chlorinatorUpdate(0,0,0)
}

def setPoolSetpoint(poolLevel) {
    def body = [id: getDataValue("chlorId"), setPoint: poolLevel ]
    def data = [device: device, item: 'poolSetpoint', value: poolLevel]

    logger("Update Chlorinator with poolSetpoint to ${poolLevel}","info")
    logger("Update Chlorinator with PUT ${params} - ${data}","debug")
    sendPut("/state/chlorinator/poolSetPoint",'updateCallback', body, data)
    sendEvent(name: "switch", value: "on", displayed:false,isStateChange:false)
}

def setSpaSetpoint(spaLevel) {
    def body =  [id: getDataValue("chlorId"), setPoint: spaLevel ]
    def data = [device: device, item: 'spaSetpoint', value: spaLevel]
    logger("Update Chlorinator with spaSetpoint to ${spaLevel}","info")
    logger("Update Chlorinator with PUT ${params} - ${data}","debug")
    sendPut("/state/chlorinator/spaSetPoint",'updateCallback', body, data)
    sendEvent(name: "switch", value: "on", displayed:false,isStateChange:false)
}

def setSuperChlorHours(status, hours) {
    logger("Super chlor  ${hours} ${status}","trace")
    def body =  [id: getDataValue("id"), hours: hours, superChlorinate : status == 'On' ? 1 : 0 ]
    def data = [device: device, item: 'superChlor', value: hours]
    logger("Update Chlorinator with SuperChlor to ${hours}","info")
	sendPut("/state/chlorinator/superChlorHours",'updateCallback', body, data)

    logger("Update Chlorinator with SuperChlorinate to ${status}","info")
 	sendPut("/state/chlorinator/superChlorinate",'updateCallback', body, data)
    sendEvent(name: "switch", value: "on", displayed:false,isStateChange:false)
}


def chlorinatorUpdate(poolLevel = null, spaLevel = null, superChlorHours = null) {
    def id = getDataValue("chlorId")
    def body = [
        id: id,
        poolSetPoint: poolLevel ? poolLevel : device.currentValue("poolSetpoint"),
        spaSetPoint: spaLevel ? spaLevel : device.currentValue("spaSetpoint") ,
        superChlorHours: superChlorHours ? superChlorHours : device.currentValue("superChlorHours")
    ]
    def data = [device: device, item: 'chlorinatorUpdate', value: body]
    logger("Update Chlorinator with ${body}","info")
    sendPut('/state/chlorinator/setChlor','updateCallback', body, data)
    sendEvent(name: "switch", value: "on", displayed:false,isStateChange:false)
}

def updateCallback(response, data=null) {
    if (response.getStatus() == 200) {
        logger("State Change Result ${response.getStatus()}","debug")
        logger("State change complete","info")
    } else {
        logger("State change failed - ${response.getStatus()}","error")
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
