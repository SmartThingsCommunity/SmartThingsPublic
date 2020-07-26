/**
 *  Copyright 2020 Brad Sileo
 *
 *  Pool Controller LightGroup
 *
 *  Author: Brad Sileo
 *
 *
 *  version: 0.9.9
 */
metadata {
	definition (name: "Pool Controller LightGroup", namespace: "bsileo", author: "Brad Sileo" )
        {
        capability "Switch"
        capability "Configuration"
        capability "Refresh"
        attribute "swimDelay", "Boolean"
        attribute "position", "Number"
        attribute "color", "String"

        attribute "lightingTheme", "String"
        attribute "nextLightingTheme", "String"
        attribute "action", "String"
		attribute "circuitID", "Number"

        if (isHE) {
        	command "saveTheme"
        	command "nextTheme"
        	command "prevTheme"
        } else {
        	command "saveTheme"
        	command "nextTheme"
        	command "prevTheme"
        }


         if (isHE) {
            command "setLightMode", [[name:"Light mode*",
			    "type":"ENUM","description":"Select an Intellibright mode to set",
			    "constraints":["Party", "Romance","Caribbean","American","Sunset","Royal","Blue","Green","Red","White","Magenta"]
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

    if (isST) {
    	tiles (scale:2) {
            standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
                state "off", label: "Off", action: "on", icon:"st.Lighting.light21", nextState: "on", backgroundColor: "#ffffff"
                state "on", label: "On", action: "off", icon:"st.Lighting.light21",  nextState: "off", backgroundColor: "#79b821"
			}

            standardTile("theme", "lightingTheme", width: 2, height: 2, canChangeIcon: true) {
                state "party", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/party.png", backgroundColor:"#4250f4", nextState:"off"
                state "romance", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/romance.png", backgroundColor:"#d28be8", nextState:"off"
                state "caribbean", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/caribbean.png", backgroundColor:"#46f2e9", nextState:"off"
                state "american", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/american.png", backgroundColor:"#d42729", nextState:"off"
                state "sunset", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/sunset.png", backgroundColor:"#ffff00", nextState:"off"
                state "royal", label:"", action:"off", icon:"https://bsileo.github.io/SmartThings_Pentair/royal.png", backgroundColor:"#9933ff", nextState:"off"

                state "blue", label:"Blue", action: "off", icon:"st.Lighting.light21", backgroundColor:"#0000FF", nextState:"off"
                state "green", label:"Green", action: "off", icon:"st.Lighting.light21", backgroundColor:"#33cc33", nextState:"off"
                state "red", label: "Red", action: "off", icon:"st.Lighting.light21",backgroundColor: "#bc3a2f", nextState: "off"
                state "white", label:"White", action:"off", icon:"st.Lighting.light21", backgroundColor:"#ffffff", nextState:"off"
                state "magenta", label:"Magenta", action:"off", icon:"st.Lighting.light21", backgroundColor:"#ff00ff", nextState:"off"
            }

            multiAttributeTile(name:"themeSelect", type:"generic", width:6, height:4) {
            	tileAttribute("nextLightingTheme", key: "PRIMARY_CONTROL") {
                	attributeState "red", label: 'Red', action: "saveTheme", icon:"st.Lighting.light21",backgroundColor: "#bc3a2f"
                    attributeState "green", label:"Green", action: "saveTheme", icon:"st.Lighting.light21", backgroundColor:"#33cc33"
					attributeState "blue", label:"Blue", action: "saveTheme", icon:"st.Lighting.light21", backgroundColor:"#0000FF"
                	attributeState "cyan", label:'Cyan', action: "saveTheme", icon:"st.Lighting.light21", backgroundColor:"#5ef2f2", nextState:"off"
                	attributeState "white", label:"White", action:"saveTheme", icon:"st.Lighting.light21", backgroundColor:"#ffffff", nextState:"off"
                    attributeState "magenta", label:"Magenta", action:"saveTheme", icon:"st.Lighting.light21", backgroundColor:"#ff00ff", nextState:"off"
                    attributeState "lightmagenta", label:"Light Magenta", action:"saveTheme", icon:"st.Lighting.light21", backgroundColor:"#bd7de8", nextState:"off"
                    attributeState "lavender", label:"Lavender", action:"saveTheme", icon:"st.Lighting.light21", backgroundColor:"#bd7de8", nextState:"off"

                    attributeState "party", label:'Party Mode', action:"saveTheme", icon:"https://bsileo.github.io/SmartThings_Pentair/party.png", backgroundColor:"#4250f4", nextState:"off"
                	attributeState "romance", label:'Romance Mode', action:"saveTheme", icon:"https://bsileo.github.io/SmartThings_Pentair/romance.png", backgroundColor:"#d28be8", nextState:"off"
                	attributeState "caribbean", label:'Caribbean Mode', action:"saveTheme", icon:"https://bsileo.github.io/SmartThings_Pentair/caribbean.png", backgroundColor:"#46f2e9", nextState:"off"
                	attributeState "american", label:'American Mode', action:"saveTheme", icon:"https://bsileo.github.io/SmartThings_Pentair/american.png", backgroundColor:"#d42729", nextState:"off"
                	attributeState "sunset", label:'Sunset Mode', action:"saveTheme", icon:"https://bsileo.github.io/SmartThings_Pentair/sunset.png", backgroundColor:"#d2d656", nextState:"off"
                	attributeState "royal", label:'Royal Mode', action:"saveTheme", icon:"https://bsileo.github.io/SmartThings_Pentair/royal.png", backgroundColor:"#9933ff", nextState:"off"
                    attributeState "default", label:'${currentValue}', action: "saveTheme", icon:"st.Lighting.light21", backgroundColor:"#f0eae9", nextState:"off", defaultState: true
                }
                tileAttribute("saveTheme", key: "SECONDARY_CONTROL") {
        			attributeState "default", label: "Click to Save", icon: 'st.Health & Wellness.health2', defaultState: true
    			}
                 tileAttribute("themeMove", key: "VALUE_CONTROL") {
                    attributeState "VALUE_UP", action: "nextTheme"
                    attributeState "VALUE_DOWN", action: "prevTheme"
                }
            }
            standardTile("nextTheme", "nextTheme", width:2, height:2, inactiveLabel: false, decoration: "flat") {
				state "default", action:"nextTheme", icon:"st.Appliances.appliances3"
			}
            standardTile("prevTheme", "prevTheme", width:2, height:2, inactiveLabel: false, decoration: "flat") {
				state "default", action:"prevTheme", icon:"st.Appliances.appliances14"
			}
			valueTile("dummy", "temperature", height:1,width:1,inactiveLabel: false ) {}

            standardTile("refresh", "refresh", width:2, height:2, inactiveLabel: false, decoration: "flat") {
				state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
			}

            main("switch")
            details(["switch", "theme", "refresh",  "themeSelect"])
		}
    }
}

def installed() {
	log.debug("Installed Intellibrite Color Light " + device.deviceNetworkId)
    state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Trace'
    getHubPlatform()
    refreshConfiguration(true)
    manageData()
}

def updated() {
  state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Info'
  refreshConfiguration(true)
  manageData()
}


def configure() {
	getHubPlatform()
    refreshConfiguration(true)
}

def refreshConfiguration(process = false) {
    def cid = getDataValue("circuitID")
    def body = ''
    def data = null
    def aCallback = 'parseConfiguration'
    if (process) {
        aCallback = 'configurationCallback'
    }
    logger("Resfresh Config for Circuit ${cid}","debug")
    sendGet("/config/lightGroups/themes", aCallback, body, data)
}


def configurationCallback(response, data=null) {
    if (parseConfiguration(response, data)) {
        manageChildren()
    } else {
        logger("Failed to process configuration ${response}","error")
    }
}

def parseConfiguration(response, data=null) {
    if (response.getStatus() == 200) {
        def msg = response.json
        logger("parseConfiguration got back ${msg}","trace")
        state.validColors = getValidColors(msg)
        return true
    } else {
        logger("Configuration Failed with code ${response.getStatus()}","error")
        return false
    }
}

def manageData() {
 	def cid = getDataValue("circuitID")
	sendEvent(name: "circuitID", value: cid, isStateChange: true, displayed: false)
}

def getValidColors(colors) {
	def skipModes = ['unknown','save','reset','none','colorset','colorswim','colorsync', 'recall','reset','hold', 'mode']
    def valid = []
	colors.each {
    	if (! skipModes.contains(it.name)) { valid.push(it) }
    }
    return valid
}


def manageChildren() {
	def hub = location.hubs[0]
    def cid = getDataValue("circuitID")
 	def displayName
    def deviceID
    def existing
    def dni

	def namespace = state.isHE ? 'hubitat' : 'smartthings'
    def deviceType = state.isHE ? "Generic Component Switch" : "Virtual Switch"
    logger("Starting manageChildren with ${state.validColors}","debug")

	state.validColors.each {
        logger("Process ${it} ${it.val}--${it.name}->${it.desc}","trace")
        displayName = "Intellibrite ${it.desc} mode"
        deviceID = "intellibrite-${cid}-${it.name}"
        dni = parent.getChildDNI("intellibrite-${cid}",it.name)
        logger("Check existing ${dni}","trace")
        if (state.isST) {
            logger("Checking in parent - ${parent}","trace")
            existing = parent.getChild('intellibrite',it.name)
        }
        else {
            logger("Using local children - ${childDevices}","trace")
            existing = childDevices.find({it.deviceNetworkId == dni})
        }
        logger("existing = ${existing}","trace")
        if (!existing) {
            try{
                logger("Creating ${it.desc} light mode button","debug")
                logger("Namespace ${namespace} type ${deviceType}  ${dni}  with Name= ${it.name} and val=${it.val}","debug")
                if (state.isST) {
                    // Add to my parent since ST does not allow nested children.
                    def cButton = parent.addHESTChildDevice(namespace, deviceType, dni,
                                                            [ label: displayName,
                                                             componentName: deviceID,
                                                             componentLabel: displayName,
                                                             isComponent:true,
                                                             completedSetup:true,
                                                             modeName: it.name,
                                                             modeVal: it.val.toString()
                                                            ])
                } else {
                    def cButton = addHESTChildDevice(namespace, deviceType, dni,
                                                     [ label: displayName,
                                                      componentName: deviceID,
                                                      componentLabel: displayName,
                                                      isComponent:true,
                                                      completedSetup:true,
                                                      modeName: it.name,
                                                      modeVal: it.val.toString()
                                                     ])
                }
                logger( "Created Button for ${it.name} ","info")
            }
            catch(e)
            {
                logger( "Error! problem creating light mode device. Check your logs for more details - " + e ,"error")
            }
        } else {
            logger( "Existing Button for ${it} Updated","info")
            existing.updateDataValue("modeName",it.name)
            existing.updateDataValue("modeVal",it.val.toString())
        }
   }
}


def parse(json) {
    logger("Parse lightGroup - ${json}","trace")
    def circuit = json.circuits[0]
    sendEvent([[name: "swimDelay", value: circuit.swimDelay ? true : false]])
    sendEvent([[name: "position", value: circuit.position]])
    sendEvent([[name: "color", value: circuit.color]])
    sendEvent([[name: "lightingTheme", value: json.lightingTheme.name, descriptionText:"Lighting Theme is ${json.lightingTheme.desc}"]])
    sendEvent([[name: "nextLightingTheme", value: json.lightingTheme.name, descriptionText:"Lighting Theme is ${json.lightingTheme.desc}"]])
    sendEvent([[name: "action", value: json.action.name, descriptionText:"Lighting Action is ${json.action.desc}"]])
    if (circuit.circuit.id.toString() == getDataValue("circuitID").toString()) {
       sendEvent([[name: "switch", value: circuit.circuit.isOn ? 'on' : 'off', descriptionText:"Light switch is ${circuit.circuit.isOn ? 'On' : 'Off'}"]])
    }
}

def refresh() {
    logger("refresh Intellibrite - ${msg}","trace")
    def body = null
    def data = null
    sendGet("/state/lightGroups", 'parseRefresh', body, data)
}

def parseRefresh (response, data=null) {
    logger("Parse Refresh ${response.getStatus()} -- ${response.getStatus()==200}","debug")
    def id = getDataValue("lightGroupID").toString()
    if (response.getStatus() == 200) {
        def json = response.getJson()
        json.each { lg ->
            logger("Checking section ${lg.id} - ${lg}","trace")
            if (lg.id.toString() == id) {
                logger("Parse Refresh JSON ${json}","debug")
                parse(lg)
            }
        }
    } else {
        logger("Refresh Failed with code ${response.getStatus()}","error")
    }
}


def setLightState(state) {
    def id = getDataValue("circuitID")
    def body = [id: id, state: state]
    logger("Set Intellibrite mode with ${body}","debug")
    sendPut("/state/circuit/setState", 'lightModeCallback', body, data)
	sendEvent(name: "switch", value: "${state == 1 ? 'on' : 'off'}", isStateChange: true, displayed: true)
}

def on() {
    setLightState(1)
}


def off() {
    setLightState(0)
}

def saveTheme() {
    def next = device.currentValue('nextLightingTheme')
    logger("Saving Theme ${next}","info")
    def newTheme = state.validColors.find { it.name == next }
    logger("Saving Theme ${next} which is matched to ${newTheme}","debug")
    def themeID = newTheme.val
    def circuitID = getDataValue("circuitID")
    def body = [id: circuitID, theme: themeID]
    def data = body
    logger("Set Intellibrite theme with ${body}","debug")
    sendPut("/state/circuit/setTheme", 'lightModeCallback', body, data)
	sendEvent(name: "lightingTheme", value: next, isStateChange: true, displayed: true)
}

def nextTheme() {
	logger("Next Theme...","debug")
    moveTheme(true)

}

def prevTheme() {
	logger("Prev Theme...","debug")
    moveTheme(false)
}

def moveTheme(up) {
	def themes = state.validColors
    def curTheme = device.currentValue('nextLightingTheme')
    if (! themes ) {
    	logger("No Colors loaded during Configuration", "warn")
    	return
    }
    def curIdx = themes.findIndexOf { it.name == curTheme }
    logger("Looking for nextTheme in ${themes} from ${curTheme} found ${curIdx} of ${themes.size()}","trace")
    def newIdx
    if (! curIdx) { curIdx = 0 }
    if (up) {
    	if (curIdx == themes.size() - 1) {
    		newIdx = 0
            } else {
            newIdx = curIdx + 1
        }
    }
    else {
    	if (curIdx == 0) {
        	newIdx = themes.size() - 1
        } else {
        	newIdx = curIdx - 1
        }
    }
    logger("Selected idx=${newIdx} which is theme ${themes[newIdx]} when going ${up ? 'up' : 'down'} from ${curIdx}","trace")
    sendEvent(name: "nextLightingTheme", value: themes[newIdx].name)
}

def componentRefresh(device) {
    logger("Got REFRESH Request from ${device}","debug")

}

def componentOn(device) {
	logger("Got ON Request from ${device}","debug")
    def mode = device.getDataValue("modeName")
    def modeVal = device.getDataValue("modeVal")
	setLightModeByVal(modeVal)
    device.off()
}

def componentOff(device) {
	logger( "Got OFF from ${device}","debug")
	// Noop - we only operate on the ON cycle
}


def setLightMode(mode) {
    def child = childDevices.find { it -> it.getDataValue("modeName") == mode.toLowerCase() }
    logger("Getting value from child ${child}","trace")
    if (child) {
        def modeVal = child.getDataValue("modeVal")
	    setLightModeByVal(modeVal)
    }
}

def setLightModeByVal(modeVal) {
    logger("Going to light mode ${modeVal}","debug")
    def id = getDataValue("circuitID")
    def body = [id: id, theme: modeVal]
    logger("Set Intellibrite mode with ${params} - ${data}","debug")
    sendPut("/state/circuit/setTheme",'lightModeCallback', body, data)
    sendEvent(name: "switch", value: "on")
}

def lightModeCallback(response, data=null) {
    logger("LightMode Result ${response.getStatus()}","debug")
    logger("LightMode Response Data ${response.getData()}","debug")
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
    logger("Send GET to with ${body} CB=${aCallback}","debug")
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
        def params = [
            uri: getControllerURI(),
            path: message,
            requestContentType: "application/json",
            contentType: "application/json",
            body:body
        ]
        asynchttpGet(aCallback, params, data)
    }
}

private sendPut(message, aCallback=generalCallback, body="", data=null) {
    logger("Send PUT to ${message} with ${body} and ${aCallback}","debug")
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
