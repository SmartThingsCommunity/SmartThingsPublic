/**
 *  Copyright 2020 Brad Sileo
 *
 *  Pool Controller - Main Device
 *
 *  Author: Brad Sileo
 *
 *  Version: "0.9.8"
 *
 */

metadata {
	definition (name: "Pool Controller", namespace: "bsileo", author: "Brad Sileo") {

        capability "Refresh"
        capability "Configuration"
        capability "Actuator"

        attribute "LastUpdated", "String"
        attribute "Freeze", "Boolean"
        attribute "Mode", "String"
        attribute "ConfigControllerLastUpdated", "String"
		attribute "waterSensor1", "Number"
        attribute "Pool-temperature", "Number"
        attribute "Pool-heatStatus", "String"
        attribute "Spa-temperature", "Number"
        attribute "Spa-heatStatus", "String"


        // Not working....disable for now
        /*command "updateAllLogging",  [[name:"Update All Logging",
                                       type: "ENUM",
                                       description: "Pick a logging settings for me and all child devices",
                                       constraints: [
        	                                "0" : "None",
        	                                "1" : "Error",
        	                                "2" : "Warning",
        	                                "3" : "Info",
        	                                "4" : "Debug",
        	                                "5" : "Trace"
        	                            ]
                                      ] ]*/
    }

	preferences {
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

    if (isST) {
       tiles(scale: 2) {
            childDeviceTile("airTemp", "airTemp", height:1,width:2,childTileName:"temperature")
            childDeviceTile("solarTemp", "solarTemp", height:1,width:2,childTileName:"temperature")
            standardTile("refresh", "device.refresh", height:1,width:1,inactiveLabel: false) {
                    state "default", label:'Refresh', action:"refresh.refresh",  icon:"st.secondary.refresh-icon"
            }

            valueTile("dummy", "airTemp", height:1,width:1,inactiveLabel: false ) {}



            // Bodies
            for (i in 1..2) {
            	childDeviceTile("setPoint-${i}","body ${i}", height:1,width:1,childTileName:"setPoint")
                childDeviceTile("heatMode-${i}","body ${i}", height:1,width:1,childTileName:"heatMode")
                childDeviceTile("temperature-${i}","body ${i}", height:1,width:1,childTileName:"temperature")
                childDeviceTile("dummy-b-${i}","body ${i}", height:1,width:1,childTileName:"dummy")
            }

            // Chlorinators
            for (i in 1..2) {
                    childDeviceTile("saltLevel-${i}","chlorinator-${i}", height:1,width:1,childTileName:"saltLevel")
                    childDeviceTile("saltRequired-${i}","chlorinator-${i}", height:1,width:1,childTileName:"saltRequired")
                    childDeviceTile("currentOutput-${i}","chlorinator-${i}", height:1,width:1,childTileName:"currentOutput")
                    childDeviceTile("poolSetpoint-${i}","chlorinator-${i}", height:1,width:1,childTileName:"poolSetpoint")
                    childDeviceTile("spaSetpoint-${i}","chlorinator-${i}", height:1,width:1,childTileName:"spaSetpoint")
                    childDeviceTile("superChlorinate-${i}","chlorinator-${i}", height:1,width:1,childTileName:"superChlorinate")
                    childDeviceTile("superChlorHours-${i}","chlorinator-${i}", height:1,width:1,childTileName:"superChlorHours")
                    childDeviceTile("chlorStatus-${i}","chlorinator-${i}", height:1,width:1,childTileName:"status")
            }

            for (i in 1..8) {
                childDeviceTile("Circuit ${i} Switch", "circuit${i}", height:1,width:1,childTileName:"switch")
            }

            for (i in 11..20) {
                childDeviceTile("feature${i}", "feature${i}", height:1,width:1,childTileName:"switch")
            }

            // Can change the attribute below to decide what is shown on the "main" page in SmartThings. This must be a real local tile, not a child tile
            valueTile("waterSensor1", "waterSensor1", height:1, width:1, inactiveLabel: false ) {
            	state("default", label:'${currentValue} °F')
           	}
            valueTile("pool-temp", "Pool-temperature", height:1, width:1, inactiveLabel: false ) {
            	state("default", label:'${currentValue} °F')
           	}
            valueTile("pool-heatStatus", "Pool-heatStatus", height:1, width:1, inactiveLabel: false ) {
            	state("default", label:'${currentValue}')
           	}
            valueTile("spa-temp", "Spa-temperature", height:1, width:1, inactiveLabel: false ) {
            	state("default", label:'${currentValue} °F')
           	}
            valueTile("spa-heatStatus", "Spa-heatStatus", height:1, width:1, inactiveLabel: false ) {
            	state("default", label:'${currentValue}')
           	}

        	main("pool-temp")
            details (
                "airTemp","solarTemp","dummy","refresh",
                "setPoint-1","heatMode-1","temperature-1", "dummy-b-1", "dummy-b-1", "dummy-b-1",
                "setPoint-2","heatMode-2","temperature-2", "dummy-b-2", "dummy-b-2", "dummy-b-2",

                "saltLevel-1","saltRequired-1","superClorinate-1","superChlorHours-1","currentOutput-1","poolSetpoint-1","spaSetPoint-1","chlorStatus-1",
                "saltLevel-2","saltRequired-2","superClorinate-2","superChlorHours-2","currentOutput-2","poolSetpoint-2","spaSetPoint-2","chlorStatus-2",
                "Circuit 2 Switch","Circuit 3 Switch","Circuit 4 Switch","Circuit 5 Switch","Circuit 6 Switch","Circuit 7 Switch",
                "Circuit 8 Switch",
                "feature11","feature12","feature13","feature14","feature15","feature16","feature17","feature18","feature19","feature20"
                )
		}
    }
}

def configure() {
  getHubPlatform()
  state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Debug'
  refreshConfiguration(true)
}

def installed() {
	getHubPlatform()
    state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Debug'
    refreshConfiguration(true)
}

def updated() {
  getHubPlatform()
  state.loggingLevelIDE = (settings.configLoggingLevelIDE) ? settings.configLoggingLevelIDE : 'Debug'
  refreshConfiguration(true)
}

def manageChildren() {
	logger( "Pool Controller manageChildren starting","debug")
    manageTempSensors()
    manageBodies()
    managePumps()
    manageHeaters()
    manageCircuits()
    manageFeatureCircuits()
    manageChlorinators()
    manageIntellichem()
    manageLightGroups()
    refresh()
}

def manageTempSensors() {
    def namespace = state.isHE ? 'hubitat' : 'smartthings/testing'
    def deviceType = state.isHE ? "Generic Component Temperature Sensor" : "Simulated Temperature Sensor"
    def airTemp = childDevices.find({it.deviceNetworkId == getChildDNI("airTemp")})
    if (!airTemp) {
        	airTemp = addHESTChildDevice(namespace,deviceType, getChildDNI("airTemp"),
            	                     [ label: getChildName("Air Temperature"),
                                      componentName: "airTemp",
                                      componentLabel: getChildName("Air Temperature"),
                	                  isComponent:true,
                                      completedSetup:true])
	    logger("Created Air temperature child device","info")
    }

    def solarTemp = childDevices.find({it.deviceNetworkId == getChildDNI("solarTemp")})
    if (!solarTemp) {
       		solarTemp = addHESTChildDevice(namespace,deviceType, getChildDNI("solarTemp"),
                                  [ label: getChildName("Solar Temperature"),
                                  componentName: "solarTemp",
                                  componentLabel: getChildName("Solar Temperature"),
                                   isComponent:true,
                                   completedSetup:true])
        logger(("Created Solar temperature child device"),"info")
    }
}

def manageBodies() {
    def bodies = state.bodies
    logger("Process bodies ${bodies}","trace")
    bodies.each { value ->
        if (value.isActive) {
            def body = getChild("body",value.id)
            if (!body) {
                logger(("Create BODY child device"),"debug")
                	body = addHESTChildDevice("bsileo","Pool Controller Body", getChildDNI("body",value.id),
                    [
                        label: getChildName(value.name),
                        componentName: "body ${value.id}",
                        componentLabel: getChildName(value.name),
                        bodyID: value.id.toString(),
                        circuitID: value.circuit.toString(),
                        isComponent:false
                    ]
                )
                logger( "Created new Body called ${value.name}","info")
            } else {
                body.updateDataValue("circuitID",value.circuit.toString())
                body.updateDataValue("bodyID",value.id.toString())
                logger( "Found existing Body called ${value.name} and updated it","info")
            }
        }
    }
}

def managePumps () {
    def pumps = state.pumps
    pumps.each { value ->
        if (value.isActive) {
            def pName = "Pump ${value.id}"
            if (value.name != null) { pName = value.name }
            def pump = getChild("pump",value.id)
            def cID = value.circuits ? value.circuits[0].circuit : ''
            if (!pump) {
                pump = addHESTChildDevice("bsileo","Pool Controller Pump", getChildDNI("pump",value.id),
                            [completedSetup: true,
                                label: getChildName(pName),
                                componentLabel:getChildName(pName),
                                isComponent:false,
                                componentName: pName,
                                pumpID: value.id.toString(),
                                pumpType: value.type.toString(),
                                circuitID: cID.toString()
                            ])
                logger( "Created new Pump called ${pName}","info")
            } else {
                pump.updateDataValue("pumpType",value.type.toString())
                pump.updateDataValue("circuitID",cID.toString())
                logger( "Found existing Pump called ${pName} and updated it","info")
            }
        }
    }
}

def manageHeaters() {
    def heaters = state.heaters
    heaters.each {data ->
        if (data.isActive) {
            def heat = getChild("heater",data.id)
            def label = getChildName(data.name)
            if (!heat) {
                def name = "heater${data.id}"
                heat = addHESTChildDevice("bsileo","Pool Controller Heater", getChildDNI("heater",data.id),
                                 [completedSetup: true,
                                   label: label ,
                                   isComponent:false,
                                   componentName: name,
                                   bodyID: data.body,
                                   circuitID: data.body,
                                   heaterID: data.id,
                                   componentLabel:label
                                 ])
                logger( "Created new Heater called ${label}" ,"info")
            } else {
                heat.updateDataValue("heaterID", data.id.toString())
                heat.updateDataValue("bodyID", data.body.toString())
                heat.updateDataValue("circuitID", data.body.toString())
                logger( "Found existing Heater called ${label} and updated it" ,"info")
            }
        }
    }
}

def manageFeatureCircuits() {
    def circuits = state.features
    def namespace = state.isHE ? 'hubitat' : 'smartthings/testing'
    def deviceType = state.isHE ? "Generic Component Switch" : "Simulated Switch"

    circuits.each {data ->
        if (data.isActive) {
            def auxname = "feature${data.id}"
            try {
                def auxButton = getChild("feature",data.id)
                if (!auxButton) {
                    def auxLabel = getChildName(data.name)
                	log.info "Create Feature switch ${auxLabel} Named=${auxname}"
                    auxButton = addHESTChildDevice(namespace,deviceType, getChildDNI("feature",data.id),
                            [
                                completedSetup: true,
                                label: auxLabel,
                                isComponent:false,
                                componentName: auxname,
                                componentLabel: auxLabel,
                                typeID: data.type.toString(),
                                circuitID: data.id.toString()
                             ])
                    logger( "Success - Created Feature switch ${data.name}" ,"debug")
                }
                else {
                    auxButton.updateDataValue("typeID",data.type.toString())
                    auxButton.updateDataValue("circuitID",data.id.toString())
                    logger("Found existing Feature Switch for ${data.name} and updated it","info")
                }
                if(state.isST) {
                    getParent().subscribeToCommand(auxButton,"on",componentOn)
                    getParent().subscribeToCommand(auxButton,"off",componentOff)
                }
            }
            catch(e)
            {
                logger( "Failed to create Feature Switch for ${data.name}" + e ,"error")
            }
        }
    }
}

def manageCircuits() {
  	def namespace = state.isHE ? 'hubitat' : 'smartthings/testing'
    def deviceType = state.isHE ? "Generic Component Switch" : "Simulated Switch"
    def circuits = state.circuits
    circuits.each {data ->
        if (data.friendlyName == "NOT USED") return
        if (data.isActive) {
            def auxname = "circuit${data.id}"
            def auxLabel = getChildName(data.name)
            try {
                def auxButton = getChild("circuit",data.id)
                if (!auxButton) {
                	log.info "Create Circuit switch ${auxLabel} Named=${auxname}"
                    auxButton = addHESTChildDevice(namespace,deviceType, getChildDNI("circuit",data.id),
                            [
                                completedSetup: true,
                                label: auxLabel,
                                isComponent:false,
                                componentName: auxname,
                                componentLabel: auxLabel,
                                typeID: data.type.toString(),
                                circuitID: data.id.toString()
                             ])
                    logger( "Success - Created switch ${auxname}" ,"debug")
                }
                else {
                    auxButton.updateDataValue("typeID",data.type.toString())
                    auxButton.updateDataValue("circuitID",data.id.toString())
                    logger("Found existing Circuit for ${data.name} and updated it","info")
                }
                if (state.isST) {
                	logger("Update subscriptions for ${data.name}","info")
                    getParent().subscribeToCommand(auxButton,"on",componentOn)
                    getParent().subscribeToCommand(auxButton,"off",componentOff)
                }
            }
            catch(e)
            {
                logger( "Failed to create Pool Controller Circuit for ${data.name}" + e ,"error")
            }
        }
    }
}


def manageChlorinators() {
    def chlors = state.chlorinators
    logger("chlors->${chlors}","trace")
    if (!chlors) {
       logger("No Chlorinator devices found on Controller","info")
       return
   }
    chlors.each {data ->
        if (data.isActive) {
            def name = "chlorinator-${data.id}"
            def label = getChildName("Chlorinator ${data.id}")
            def chlor = getChild("chlorinator",data.id)
            if (!chlor) {
                	chlor = addHESTChildDevice("bsileo","Pool Controller Chlorinator", getChildDNI("chlorinator",data.id),
                                  [completedSetup: true,
                                   label: label ,
                                   isComponent:false,
                                   componentName: name,
                                   chlorId: data.id,
                                   address: data.address.toString(),
                                   componentLabel:label
                                 ])
                logger( "Created Pool Chlorinator ${label}" ,"info")
            } else {
                chlor.updateDataValue("address", data.address.toString())
                chlor.updateDataValue("chlorId", data.id.toString())
                logger( "Found existing Pool Chlorinator ${label} and updated it" ,"info")
            }
        }
    }
}

def manageIntellichem() {
   def chems = state.intellichem
   if (!chems) {
       logger("No Intellichem devices found on Controller","info")
       return
   }
   chems.each {data ->
        if (data.isActive) {
            def name = "intellichem${data.id}"
            def label = getChildName("Intellichem ${data.id}")
            try {
                def existing = getChild("intellichem",data.id)
                if (!existing) {
                	log.info "Create Intellichem ${auxLabel} Named=${name}"
                    existing = addHESTChildDevice("bsileo","Pool Controller Intellichecm", getChildDNI("intellichem",data.id),
                            [
                                completedSetup: true,
                                label: auxLabel,
                                isComponent:false,
                                componentName: name,
                                componentLabel: label
                             ])
                    logger( "Success - Created ${name}" ,"debug")
                }
                else {
                    logger("Found existing INtellichem ${name} and updated it","info")
                }
            }
            catch(e)
            {
                logger( "Failed to create Intellichem ${name}" + e ,"error")
            }
        }
    }
}

def manageLightGroups() {
	logger( "Create/Update Light Children for this device","debug")
    def lights = state.lightGroups
     if (!lights) {
       logger("No Light Groups found on Controller","info")
       return
   }
   lights.each {light ->
        if (light.isActive) {
            try {
                def cID = light.circuits ? light.circuits[0].circuit : ''
                def existing = getChild("lightGroup",light.id)
                if (!existing) {
                	def name = "lightGroup${light.id}"
                    logger("Creating Light Group Named ${name}","trace")
                    def label = getChildName(light.name)
                    existing = addHESTChildDevice("bsileo","Pool Controller LightGroup", getChildDNI("lightGroup",light.id),
                            [
                                completedSetup: true,
                                label:label,
                                isComponent:false,
                                componentName: name,
                                componentLabel: label,
                                circuitID: cID,
                                lightGroupID: light.id
                             ])
                    logger( "Created Light Group ${name}" ,"info")
                }
                else {
                    existing.updateDataValue("circuitID",cID.toString())
                    existing.updateDataValue("lightGroupID",light.id.toString())
                    logger("Found existing Light Group ${light.id} and updated it","info")
                }
            }
            catch(e)
            {
                logger( "Failed to create Light Group ${name}-" + e ,"error")
            }
        }
   }
}


def getChildName(name) {
    def result = name
    def prefix = getDataValue("prefixNames") == "true" ? true : false
    if (prefix) {
        result = "${device.displayName} (${name})"
    }
    return result
}


// ******************************************************************
// Update my configuration from the controller into my STATE
// If process is true, we also manageChildren() after the state is update
// ******************************************************************
def refreshConfiguration(process = false) {
    if (process) {
        sendGet("/config",'configurationCallback')
    } else {
        sendGet("/config",'parseConfiguration')
    }
}

def configurationCallback(response, data=null) {
    if (parseConfiguration(response, data)) {
        manageChildren()
    } else {
        logger("Failed to process configuration ${response}","error")
    }
}

def parseConfiguration(response, data=null) {
    def msg = response.json
    logger(msg,"trace")
    state.bodies = msg.bodies
    state.circuits = msg.circuits
    state.features = msg.features
    state.pumps = msg.pumps
    state.valves = msg.valves
    state.heaters = msg.heaters
    state.chlorinators = msg.chlorinators
    state.intellibrite = msg.intellibrite
    state.configLastUpdated = msg.lastUpdated
    state.lightGroups = msg.lightGroups
    state.remote = msg.remotes
    state.eggTimers = msg.eggTimers
    logger(state,"trace")
    return true
}

def updateAllLogging(level) {
    levels = ["None" : "0",
        	   "Error" : "1",
        	   "Warning" : "2",
        	   "Info" : "3",
        	   "Debug" : "4",
        	   "Trace": "5"]
    levelID = levels[level]
    logger("LEVEL=${level}->${levelID}","error")
    device.updateSetting("configLoggingLevelIDE", level)
    state.loggingLevelIDE = levelID
    childDevices.each {
        try {
            it.updateSetting("configLoggingLevelIDE", level)
            // it.updateSetting("configLoggingLevelIDE", levelID)
        }
        catch (e) {
            logger("Error setting Logging on ${it} - ${e}","trace")
        }
    }
    logger("Logging set to level ${level}(${settings.loggingLevelIDE})","info")
}

def refresh() {
    sendGet("/state/temps", parseTemps)
    sendGet("/config/all", parseConfigAll)
    childDevices.each {
        try {
            it.refresh()
        }
        catch (e) {
            logger("No refresh method on ${child} - ${e}","trace")
        }
    }
}


def parseConfigAll(response, data=null) {
    if (response.getStatus() == 200) {
        def json = response.getJson()
        def date = new Date()
        sendEvent([[name:"LastUpdated", value:"${date.format('MM/dd/yyyy')} ${date.format('HH:mm:ss')}", descriptionText:"Last updated at ${date.format('MM/dd/yyyy')} ${date.format('HH:mm:ss')}"]])
        def lastUpdated = json.lastUpdated
        sendEvent([[name:"ConfigControllerLastUpdated", value:lastUpdated, descriptionText:"Last updated time is ${lastUpdated}"]])
	}
}

def parseTemps(response, data=null) {
    logger("Parse Temps ${response.getStatus()} -- ${response.getStatus()==200}","debug")
    if (response.getStatus() == 200) {
        def at = childDevices.find({it.deviceNetworkId == getChildDNI("airTemp")})
        def solar = childDevices.find({it.deviceNetworkId == getChildDNI("solarTemp")})
        logger("Process ${response.getJson()} for '${at}' and '${solar}'","trace")
        String unit = "°${location.temperatureScale}"
        response.getJson().each {k, v ->
            logger("Process ${k} ${v}","trace")
           switch (k) {
        	 case "air":
                if (state.isHE) { at?.parse([[name:"temperature", value:v, descriptionText:"${at?.displayName} temperature is ${v}${unit}", unit: unit]]) }
                else {
                	at?.setTemperature(v)
                }
            	break
             case "solar":
                if (state.isHE) { solar?.parse([[name:"temperature", value:v, descriptionText:"${solar?.displayName} temperature is ${v}${unit}", unit: unit]])
                }else {
                	solar?.setTemperature(v)
                }
            	break
              case "waterSensor1":
              		sendEvent([[name:"waterSensor1", value: v, descriptionText:"Update temperature of Water Sensor 1 to ${v}"]])
            	break
              case "bodies":
              	logger("Got bodies","trace")
              	parseTempsBodies(v)
                break
            default:
            	break
          }
        }
	}
}

def parseTempsBodies(bodies) {
	logger("Parse Temps Bodies ${bodies}","trace")
	bodies.each {body ->
    	sendEvent([[name:"${body.name}-temperature", value: body.temp, descriptionText:"Temperature of Body ${body.name} is ${body.temp}"]])
        sendEvent([[name:"${body.name}-heatStatus", value: body.heatStatus.name, descriptionText:"Heater of Body ${body.name} is ${body.heatStatus.desc}"]])
    }
}

// **********************************************
// inbound PARSE
// **********************************************
def parse(raw) {
    logger( "Parsing ${raw}","trace")
    def msg = parseLanMessage(raw)
    logger( "Parsing ${msg}","trace")
    logger( "Full msg: ${msg}","trace")
    logger( "HEADERS: ${msg.headers}","trace")
    def type = msg.headers['X-EVENT-TYPE']
    logger("Parse event of type: ${type}","info")
    logger( "JSON: ${msg.json}","debug")
    Date date = new Date()
    sendEvent([[name:"LastUpdated", value:"${date.format('MM/dd/yyyy')} ${date.format('HH:mm:ss')}", descriptionText:"Last updated at ${date.format('MM/dd/yyyy')} ${date.format('HH:mm:ss')}"]])
    if (msg.json) {
        switch(type) {
            case "temps":
                if (msg.json.bodies) {parseDevices(msg.json.bodies, 'body')}
                break
            case "circuit":
                parseCircuit(msg.json)
                break
            case "feature":
                parseFeature(msg.json)
                break
            case "body":
                parseDevice(msg.json, 'body')
                break
            case "controller":
                parseController(msg.json)
                break
            case "virtualCircuit":
                break
            case "config":
                parseConfig(msg.json)
                break
            case "pump":
                parseDevice(msg.json, 'pump')
                break
            case "chlorinator":
                parseDevice(msg.json, 'chlorinator')
                break
            case "lightGroup":
                parseDevice(msg.json, 'lightGroup')
                break
            default:
                logger( "No handler for incoming event type '${type}'","warn")
                break
       }
    }
}


def parseDevices(msg, type) {
    logger("Parsing ${type} - ${msg}","debug")
    msg.each { section ->
       parseDevice(section, type)
    }
}

def parseDevice(section,type) {
    logger("Parse Device of ${type} from ${section}","debug")
    logger("Device is ${getChild(type, section.id)}","trace")
    getChild(type, section.id)?.parse(section)
}

def parseCircuit(msg) {
    logger("Parsing circuit - ${msg}","debug")
    def child = getChild("circuit",msg.id)
    logger("Parsing circuit ${child}")
    if (child) {
        def val = msg.isOn ? "on": "off"
        child.parse([[name:"switch",value: val, descriptionText: "Status changed from controller to ${val}" ]])
    }
}

def parseConfig(msg) {
    // No processing on config messages - these contain invalid data versus the current state so just let them go, use Configure to update
    //parseDevices(msg.bodies, 'body')
    //parseDevices(msg.pumps, 'pump')
    //parseDevices(msg.chlorinators, 'chlorinator')
    //parseDevices(msg.intellichem, 'intellichem')
}

def parseController(msg) {
    logger("Parsing controller - ${msg}","debug")

}

def parseFeature(msg) {
    logger("Parsing feature - ${msg}","debug")
    def child = getChild("feature",msg.id)
    logger("Parsing feature ${child}","trace")
    if (child) {
        def val = msg.isOn ? "on": "off"
        child.parse([[name:"switch",value: val, descriptionText: "Status changed from controller to ${val}" ]])
    }
}

def getChild(systemID) {
    logger("Find child with ${systemID}","trace")
    return getChildDevices().find { element ->
        return element.id == systemID
      }
}

def getChild(type,id) {
    def dni = getChildDNI(type,id)
    logger("Find child with ${type}-${id}","trace")
    return getChildDevices().find { element ->
        return element.deviceNetworkId == dni
      }
}

def getChildCircuit(id) {
	// get the circuit device given the ID number only (e.g. 1,2,3,4,5,6)
    // also check for features as it could be one of them!
    def child = getChild("circuit",id)
    if (!child) {
        child = getChild("feature",id)
    }
}

def getChildDNI(name) {
	return getDataValue("controllerMac") + "-" + name
}

def getChildDNI(type, name) {
    return getDataValue("controllerMac") + "-${type}-${name}"
}



// **********************************
// PUMP Control
// **********************************

def poolPumpOn() {
	return setCircuit(poolPumpCircuitID(),1)
}

def poolPumpOff() {
	return setCircuit(poolPumpCircuitID(),0)
}

def spaPumpOn() {
	logger( "SpaPump ON","debug")
	return setCircuit(spaPumpCircuitID(),1)
}

def spaPumpOff() {
	return setCircuit(spaPumpCircuitID(),0)
}

// **********************************
// Component Interfaces
// **********************************
def componentRefresh(device) {
    logger("Got REFRESH Request from ${device}","trace")
}

def componentOn(device) {
	logger("Got ON Request from ${device}","debug")
	return setCircuit(device,1)
}

def componentOff(device) {
	logger( "Got OFF from ${device}","debug")
	return setCircuit(device,0)
}

def childCircuitID(device) {
	logger("CCID---${device}","trace")
	return toIntOrNull(device.getDataValue("circuitID"))
}

def setCircuit(device, state) {
  def id = childCircuitID(device)
  logger( "Executing setCircuit with ${device} - ${id} to ${state}","debug")
  sendPut("/state/circuit/setState", setCircuitCallback, [id: id, state: state], [id: id, newState: state, device: device])
}

def setCircuitCallback(response, data=null) {
    if (response.getStatus() == 200) {
        logger("Circuit update Succeeded","info")
        logger("SetCircuitCallback(data):${data}","debug")
        if (data) {
            def dev = data.device
            def newState = data.newState.toString() == "1" ? 'on' : 'off'
            logger("SetCircuitCallback-Sending:${newState} to ${dev}","debug")
            dev.sendEvent([name:'switch', value: newState , textDescription: "Set to ${newState}"])
        }
    } else {
        logger("Ciurcuit update failed with code ${response.getStatus()}","error")
    }
}

// **********************************
// INTERNAL Methods
// **********************************
def addHESTChildDevice(namespace, deviceType, dni, options  ) {
	if (state.isHE) {
    	return addChildDevice(namespace, deviceType, dni, options)
	} else {
    	def hub = location.hubs[0]
    	return addChildDevice(namespace, deviceType, dni, hub?.id, options)
    }
}

def getHost() {
  def ip = getDataValue('controllerIP')
  def port = getDataValue('controllerPort')
  return "${ip}:${port}"
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