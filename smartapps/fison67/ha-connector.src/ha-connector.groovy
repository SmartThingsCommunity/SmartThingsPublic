/**
 *  HA Connector (v.0.0.8)
 *
 *  Authors
 *   - fison67@nate.com
 *  Copyright 2018
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
 
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field


@Field 
attrList = ["switch", "button", "contact", "motion", "acceleration", "presence", "carbonDioxide", "carbonMonoxide", "energy", "illuminance", "power", "shock", "humidity", "lqi",
            "rssi", "temperature", "thermostatSetpoint", "threeAxis", "touch", "voltage", "water"]
    
@Field 
CAPABILITY_MAP = [
    "accelerationSensors": [
        name: "Acceleration Sensor",
        capability: "capability.accelerationSensor",
        attributes: [
            "acceleration"
        ]
    ],
    "alarm": [
        name: "Alarm",
        capability: "capability.alarm",
        attributes: [
            "alarm"
        ],
        action: "actionAlarm"
    ],
    "beacon": [
        name: "Beacon",
        capability: "capability.beacon",
        attributes: [
            "presence"
        ]
    ],
    "button": [
        name: "Button",
        capability: "capability.button",
        attributes: [
            "button"
        ]
    ],
    "carbonDioxideMeasurement": [
        name: "Carbon Dioxide Measurement",
        capability: "capability.carbonDioxideMeasurement",
        attributes: [
            "carbonDioxide"
        ]
    ],
    "carbonMonoxideDetector": [
        name: "Carbon Monoxide Detector",
        capability: "capability.carbonMonoxideDetector",
        attributes: [
            "carbonMonoxide"
        ]
    ],
    "colorControl": [
        name: "Color Control",
        capability: "capability.colorControl",
        attributes: [
            "hue",
            "saturation",
            "color"
        ],
        action: "actionColor"
    ],
    "colorTemperature": [
        name: "Color Temperature",
        capability: "capability.colorTemperature",
        attributes: [
            "colorTemperature"
        ],
        action: "actionColorTemperature"
    ],
    "consumable": [
        name: "Consumable",
        capability: "capability.consumable",
        attributes: [
            "consumable"
        ],
        action: "actionConsumable"
    ],
    "contactSensors": [
        name: "Contact Sensor",
        capability: "capability.contactSensor",
        attributes: [
            "contact"
        ]
    ],
    "doorControl": [
        name: "Door Control",
        capability: "capability.doorControl",
        attributes: [
            "door"
        ],
        action: "actionOpenClosed"
    ],
    "energyMeter": [
        name: "Energy Meter",
        capability: "capability.energyMeter",
        attributes: [
            "energy"
        ]
    ],
    "garageDoors": [
        name: "Garage Door Control",
        capability: "capability.garageDoorControl",
        attributes: [
            "door"
        ],
        action: "actionOpenClosed"
    ],
    "illuminanceMeasurement": [
        name: "Illuminance Measurement",
        capability: "capability.illuminanceMeasurement",
        attributes: [
            "illuminance"
        ]
    ],
    "levels": [
        name: "Switch Level",
        capability: "capability.switchLevel",
        attributes: [
            "level"
        ],
        action: "actionLevel"
    ],
    "lock": [
        name: "Lock",
        capability: "capability.lock",
        attributes: [
            "lock"
        ],
        action: "actionLock"
    ],
    "motionSensors": [
        name: "Motion Sensor",
        capability: "capability.motionSensor",
        attributes: [
            "motion"
        ],
        action: "actionActiveInactive"
    ],
    "pHMeasurement": [
        name: "pH Measurement",
        capability: "capability.pHMeasurement",
        attributes: [
            "pH"
        ]
    ],
    "powerMeters": [
        name: "Power Meter",
        capability: "capability.powerMeter",
        attributes: [
            "power"
        ]
    ],
    "presenceSensors": [
        name: "Presence Sensor",
        capability: "capability.presenceSensor",
        attributes: [
            "presence"
        ],
        action: "actionPresence"
    ],
    "humiditySensors": [
        name: "Relative Humidity Measurement",
        capability: "capability.relativeHumidityMeasurement",
        attributes: [
            "humidity"
        ]
    ],
    "relaySwitch": [
        name: "Relay Switch",
        capability: "capability.relaySwitch",
        attributes: [
            "switch"
        ],
        action: "actionOnOff"
    ],
    "shockSensor": [
        name: "Shock Sensor",
        capability: "capability.shockSensor",
        attributes: [
            "shock"
        ]
    ],
    "signalStrength": [
        name: "Signal Strength",
        capability: "capability.signalStrength",
        attributes: [
            "lqi",
            "rssi"
        ]
    ],
    "sleepSensor": [
        name: "Sleep Sensor",
        capability: "capability.sleepSensor",
        attributes: [
            "sleeping"
        ]
    ],
    "smokeDetector": [
        name: "Smoke Detector",
        capability: "capability.smokeDetector",
        attributes: [
            "smoke"
        ]
    ],
    "soundSensor": [
        name: "Sound Sensor",
        capability: "capability.soundSensor",
        attributes: [
            "sound"
        ]
    ],
    "stepSensor": [
        name: "Step Sensor",
        capability: "capability.stepSensor",
        attributes: [
            "steps",
            "goal"
        ]
    ],
    "switches": [
        name: "Switch",
        capability: "capability.switch",
        attributes: [
            "switch"
        ],
        action: "actionOnOff"
    ],
    "soundPressureLevel": [
        name: "Sound Pressure Level",
        capability: "capability.soundPressureLevel",
        attributes: [
            "soundPressureLevel"
        ]
    ],
    "tamperAlert": [
        name: "Tamper Alert",
        capability: "capability.tamperAlert",
        attributes: [
            "tamper"
        ]
    ],
    "temperatureSensors": [
        name: "Temperature Measurement",
        capability: "capability.temperatureMeasurement",
        attributes: [
            "temperature"
        ]
    ],
    "thermostat": [
        name: "Thermostat",
        capability: "capability.thermostat",
        attributes: [
            "temperature",
            "heatingSetpoint",
            "coolingSetpoint",
            "thermostatSetpoint",
            "thermostatMode",
            "thermostatFanMode",
            "thermostatOperatingState"
        ],
        action: "actionThermostat"
    ],
    "thermostatCoolingSetpoint": [
        name: "Thermostat Cooling Setpoint",
        capability: "capability.thermostatCoolingSetpoint",
        attributes: [
            "coolingSetpoint"
        ],
        action: "actionCoolingThermostat"
    ],
    "thermostatFanMode": [
        name: "Thermostat Fan Mode",
        capability: "capability.thermostatFanMode",
        attributes: [
            "thermostatFanMode"
        ],
        action: "actionThermostatFan"
    ],
    "thermostatHeatingSetpoint": [
        name: "Thermostat Heating Setpoint",
        capability: "capability.thermostatHeatingSetpoint",
        attributes: [
            "heatingSetpoint"
        ],
        action: "actionHeatingThermostat"
    ],
    "thermostatMode": [
        name: "Thermostat Mode",
        capability: "capability.thermostatMode",
        attributes: [
            "thermostatMode"
        ],
        action: "actionThermostatMode"
    ],
    "thermostatOperatingState": [
        name: "Thermostat Operating State",
        capability: "capability.thermostatOperatingState",
        attributes: [
            "thermostatOperatingState"
        ]
    ],
    "thermostatSetpoint": [
        name: "Thermostat Setpoint",
        capability: "capability.thermostatSetpoint",
        attributes: [
            "thermostatSetpoint"
        ]
    ],
    "threeAxis": [
        name: "Three Axis",
        capability: "capability.threeAxis",
        attributes: [
            "threeAxis"
        ]
    ],
    "timedSession": [
        name: "Timed Session",
        capability: "capability.timedSession",
        attributes: [
            "timeRemaining",
            "sessionStatus"
        ],
        action: "actionTimedSession"
    ],
    "touchSensor": [
        name: "Touch Sensor",
        capability: "capability.touchSensor",
        attributes: [
            "touch"
        ]
    ],
    "valve": [
        name: "Valve",
        capability: "capability.valve",
        attributes: [
            "contact"
        ],
        action: "actionOpenClosed"
    ],
    "voltageMeasurement": [
        name: "Voltage Measurement",
        capability: "capability.voltageMeasurement",
        attributes: [
            "voltage"
        ]
    ],
    "waterSensors": [
        name: "Water Sensor",
        capability: "capability.waterSensor",
        attributes: [
            "water"
        ]
    ],
    "windowShades": [
        name: "Window Shade",
        capability: "capability.windowShade",
        attributes: [
            "windowShade"
        ],
        action: "actionOpenClosed"
    ]
]


definition(
    name: "HA Connector",
    namespace: "fison67",
    author: "fison67",
    description: "A Connector between HA and ST",
    category: "My Apps",
    iconUrl: "https://community-home-assistant-assets.s3.dualstack.us-west-2.amazonaws.com/original/3X/6/3/63f75921214e158bc02336dc864c096b11889f14.png",
    iconX2Url: "https://community-home-assistant-assets.s3.dualstack.us-west-2.amazonaws.com/original/3X/6/3/63f75921214e158bc02336dc864c096b11889f14.png",
    iconX3Url: "https://community-home-assistant-assets.s3.dualstack.us-west-2.amazonaws.com/original/3X/6/3/63f75921214e158bc02336dc864c096b11889f14.png",
    oauth: true
)

preferences {
   page(name: "mainPage")
   page(name: "haDevicePage")
   page(name: "haAddDevicePage")
   page(name: "haTypePage")
   page(name: "haDeleteDevicePage")
   page(name: "stAddDevicePage")
}


def mainPage() {
//    log.debug "Executing mainPage"
    dynamicPage(name: "mainPage", title: "Home Assistant Manage", nextPage: null, uninstall: true, install: true) {
        section("Configure HA API"){
           input "haAddress", "string", title: "HA address", required: true
           input "haPassword", "string", title: "HA Password", required: true
           href "haDevicePage", title: "Get HA Devices", description:""
           href "haAddDevicePage", title: "Add HA Device", description:""
           href "haDeleteDevicePage", title: "Delete HA Device", description:""
           href "stAddDevicePage", title: "Add ST Device", description:"Select ST Devices to add HA"
       }
       section() {
            paragraph "View this SmartApp's configuration to use it in other places."
            href url:"${apiServerUrl("/api/smartapps/installations/${app.id}/config?access_token=${state.accessToken}")}", style:"embedded", required:false, title:"Config", description:"Tap, select, copy, then click \"Done\""
       }
    }
}

def haTypePage() {
    dynamicPage(name: "haTypePage", title: "Select a type", nextPage: "mainPage") {
       section("Configure HA API"){
           input "haAddType", "enum", title: "type", required: true, options: ["Default Sensor", "Switch", "Color Light", "White Light", "Motion Sensor", "Power Meter", "Illuminance Sensor", "Door Sensor", "Presence Sensor", "Temperature Sensor", "Humidity Sensor", "Battery", "Vacuum", "Blind", "Air Conditioner"], defaultValue: "Default"
       }
    }
}

def stAddDevicePage(){
	dynamicPage(name: "stAddDevicePage", title:"Add ST Device") {
    	section ("Select") {
            CAPABILITY_MAP.each { key, capability ->
                input key, capability["capability"], title: capability["name"], multiple: true, required: false
            }
        }
    }
}

def haDevicePage(){
	log.debug "Executing haDevicePage"
    getDataList()
    
    dynamicPage(name: "haDevicePage", title:"Get HA Devices", refreshInterval:5) {
        section("Please wait for the API to answer, this might take a couple of seconds.") {
            if(state.latestHttpResponse) {
                if(state.latestHttpResponse == 200) {
                    paragraph "Connected \nOK: 200"
                } else {
                    paragraph "Connection error \nHTTP response code: " + state.latestHttpResponse
                }
            }
        }
    }
}

def haAddDevicePage(){
    def addedDNIList = []
	def childDevices = getAllChildDevices()
    childDevices.each {childDevice->
		addedDNIList.push(childDevice.deviceNetworkId)
    }
    
    def list = []
    list.push("None")
    state.dataList.each { 
    	def entity_id = "${it.entity_id}"
    	def friendly_name = "${it.attributes.friendly_name}"
        if(friendly_name == null){
        	friendly_name = ""
        }
       	if(!addedDNIList.contains("ha-connector-" + entity_id)){
        	if(entity_id.contains("light.") || entity_id.contains("switch.") || entity_id.contains("fan.") || entity_id.contains("cover.") || entity_id.contains("sensor.") || entity_id.contains("vacuum.") || entity_id.contains("device_tracker.") || entity_id.contains("climate.")){
            	if(!entity_id.startsWith("sensor.st_") && !entity_id.startsWith("switch.st_")){
        			list.push("${friendly_name} [ ${entity_id} ]")
                }
            }
        }
    }
   
    dynamicPage(name: "haAddDevicePage", nextPage: "haTypePage") {
        section ("Add HA Devices") {
            input(name: "selectedAddHADevice", title:"Select" , type: "enum", required: true, options: list, defaultValue: "None")
		}
	}

}

def haDeleteDevicePage(){
	log.debug "Executing Delete Page"
    
    def list = []
    list.push("None")
	def childDevices = getAllChildDevices()
    childDevices.each {childDevice->
		list.push(childDevice.label + " -> " + childDevice.deviceNetworkId)
    }
    dynamicPage(name: "haDeleteDevicePage", nextPage: "mainPage") {
        section ("Delete HA Device") {
            input(name: "selectedDeleteHADevice", title:"Select" , type: "enum", required: true, options: list, defaultValue: "None")
		}
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
    
    if (!state.accessToken) {
        createAccessToken()
    }
    
    app.updateSetting("selectedAddHADevice", "None")
    app.updateSetting("selectedDeleteHADevice", "None")
}

def stateChangeHandler(evt) {
	def device = evt.getDevice()
    if(device){
    	def type = device.hasCommand("on") ? "switch" : "sensor"
 
		def theAtts = device.supportedAttributes
        def resultMap = [:]
        resultMap["friendly_name"] = device.displayName
        theAtts.each {att ->
        	def item = {}
            try{
            	def _attr = "${att.name}State"
                def val = device."$_attr".value
                resultMap["${att.name}"] = val
            }catch(e){
            }
        }
        
        def value = "${evt.value}"
        /*
        if("${evt.name}" == "lastCheckin"){
        	def existMotion = False
        	device.capabilities.each {cap ->
            	if("Motion Sensor".equalsIgnoreCase("${cap.name}")){
                	existMotion = True
                }
            }
            if(existMotion == True){
        		value = device.motionState.value
            }
        }
        */
        String pattern = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s+/]"; 
        String idString = "${type}.st_" + device.name.toLowerCase().replaceAll(pattern, "_") + "_" + device.deviceNetworkId.toLowerCase().replaceAll(pattern, "_");
        String ids = idString.replaceAll(" ", "_")

		def options = [
            "method": "POST",
            "path": ("/api/states/" + ids),
            "headers": [
                "HOST": settings.haAddress,
                "x-ha-access": settings.haPassword,
                "Content-Type": "application/json"
            ],
            "body":[
                "state":value,
                "attributes":resultMap
            ]
        ]
        
//        log.debug "ST -> HA >> [${device.displayName}(${device.deviceNetworkId}) : ${value}]"
        def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: notifyCallback])
    	sendHubCommand(myhubAction)
    }
}

def notifyCallback(physicalgraph.device.HubResponse hubResponse) {
    def msg, json, status
    try {
        msg = parseLanMessage(hubResponse.description)
//        log.debug(msg)
    } catch (e) {
        logger('warn', "Exception caught while parsing data: "+e);
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    // Unsubscribe from all events
    unsubscribe()
    // Subscribe to stuff
    initialize()
    
    CAPABILITY_MAP.each { key, capability ->
        capability["attributes"].each { attribute ->
        	for (item in settings[key]) {
                if(settings[key]){
                    subscribe(item, attribute, stateChangeHandler)
                }
            }
        }
    }
    
    app.updateSetting("haAddType", "Default Sensor")
    app.updateSetting("selectedAddHADevice", "None")
    app.updateSetting("selectedDeleteHADevice", "None")
}

// Return list of displayNames
def getDeviceNames(devices) {
    def list = []
    devices.each{device->
        list.push(device.displayName)
    }
    list
}

def getHADeviceByEntityId(entity_id){
	def target
	state.dataList.each {haDevice -> 
         if(haDevice.entity_id == entity_id){
         	target = haDevice
         }
	}
    target
}

def addHAChildDevice(){
//	String[] dth1_list = ["active", "inactive", "open", "closed", "dry", "wet", "clear", "detected", "not present", "present", "home", "not_home", "on", "off"]
    if(settings.selectedAddHADevice){
        if(settings.selectedAddHADevice != "None"){
            log.debug "ADD >> " + settings.selectedAddHADevice

            def tmp = settings.selectedAddHADevice.split(" \\[ ")
            def tmp2 = tmp[1].split(" \\]")
            def entity_id = tmp2[0]
            def dni = "ha-connector-" + entity_id
            def haDevice = getHADeviceByEntityId(entity_id)
            if(haDevice){
            	def dth = "HA " + haAddType
                def name = haDevice.attributes.friendly_name
                if(!name){
                    name = entity_id
                }
                try{
                    def childDevice = addChildDevice("fison67", dth, dni, location.hubs[0].id, [
                        "label": name
                    ])

                    childDevice.setHASetting(settings.haAddress, settings.haPassword, entity_id)
                    childDevice.setStatus(haDevice.state)
                    if(haDevice.attributes.unit_of_measurement){
                        childDevice.setUnitOfMeasurement(haDevice.attributes.unit_of_measurement)
                    }
                    childDevice.refresh()
                }catch(err){
                	log.error "Add HA Device ERROR >> ${err}"
                }
            }
        }
	}        
}

def deleteChildDevice(){
	if(settings.selectedDeleteHADevice){
    	if(settings.selectedDeleteHADevice != "None"){
            log.debug "DELETE >> " + settings.selectedDeleteHADevice
            def nameAndDni = settings.selectedDeleteHADevice.split(" -> ")
            try{
                deleteChildDevice(nameAndDni[1])
            }catch(err){
            	
            }
     	}       
    }
}

def initialize() {
	log.debug "initialize"

	deleteChildDevice()
    addHAChildDevice()

}

def dataCallback(physicalgraph.device.HubResponse hubResponse) {
    def msg, json, status
    try {
        msg = parseLanMessage(hubResponse.description)
        status = msg.status
        json = msg.json
        state.dataList = json
    	state.latestHttpResponse = status
    } catch (e) {
        logger('warn', "Exception caught while parsing data: "+e);
    }
}

def getDataList(){
    def options = [
     	"method": "GET",
        "path": "/api/states",
        "headers": [
        	"HOST": settings.haAddress,
            "x-ha-access": settings.haPassword,
            "Content-Type": "application/json"
        ]
    ]
    
    def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: dataCallback])
    sendHubCommand(myhubAction)
}

def deviceCommandList(device) {
  	device.supportedCommands.collectEntries { command->
    	[
      		(command.name): (command.arguments)
    	]
  	}
}

def deviceAttributeList(device) {
  	device.supportedAttributes.collectEntries { attribute->
    	try {
      		[
        		(attribute.name): device.currentValue(attribute.name)
      		]
    	} catch(e) {
      		[
        		(attribute.name): null
      		]
    	}
  	}
}

def updateDevice(){
	def dni = "ha-connector-" + params.entity_id 
    try{
    	def device = getChildDevice(dni)
        if(device){
        	log.debug "HA -> ST >> [${dni} : ${params.value}]"
            device.setStatus(params.value)
            if(params.unit){
            	device.setUnitOfMeasurement(params.unit)
            }
     	}
    }catch(err){
        log.error "${err}"
    }
	
	def deviceJson = new groovy.json.JsonOutput().toJson([result: true])
	render contentType: "application/json", data: deviceJson  
}

def getHADevices(){
	def haDevices = []
	def childDevices = getAllChildDevices()
    childDevices.each {childDevice->
		haDevices.push(childDevice.deviceNetworkId.substring(13))
    }
    
	def deviceJson = new groovy.json.JsonOutput().toJson([list: haDevices])
	render contentType: "application/json", data: deviceJson  
    
}

def getSTDevices(){
	def list = []
	CAPABILITY_MAP.each { key, capability ->
        capability["attributes"].each { attribute ->
        	if(settings[key]){
            	settings[key].each {device ->
                	def obj = [:]
                    obj["dni"] = device.deviceNetworkId
                    obj["id"] = device.name
                    obj["name"] = device.displayName
                    obj["type"] = device.hasCommand("on") ? "switch" : "sensor"
                    try{
                    	def theAtts = device.supportedAttributes
                        def sList = []
                        theAtts.each {att ->
                        	sList.push(att.name)
                        }
                        obj["attr"] = sList
                    }catch(e){
                    }
                    
                    def existSameDevice = False
                    for ( item in list ) {
                    	if(item['dni'] == device.deviceNetworkId){
                    		existSameDevice = True
                            break
                        }
                    }
                    if(existSameDevice == False){
            			list.push(obj)
					}
                }
            }
        }
    }
	def deviceJson = new groovy.json.JsonOutput().toJson(list)
	render contentType: "application/json", data: deviceJson  
}

def getSTDevice(){
	def status = null
    def totalMap = [:]
    def resultMap = [:]
	CAPABILITY_MAP.each { key, capability ->
        capability["attributes"].each { attribute ->
        	if(settings[key]){
            	settings[key].each {device ->
                	def dni = device.deviceNetworkId
                    if(dni == params.dni){
                    	totalMap["entity_id"] = "sensor.st_" + dni.toLowerCase()
              //          resultMap["friendly_name"] = device.displayName
                    	def theAtts = device.supportedAttributes
                        theAtts.each {att ->
                        	def item = {}
                            try{
                          //  	if(existValueInList(attrList, att.name)){ 
                              	if(attrList.contains(att.name)){
                                	if(status == null){
                                    	status = device.currentValue(att.name)
                                    }
                                }
                                
                                def _attr = "${att.name}State"
                                def val = device."$_attr".value
                                resultMap["${att.name}"] = val
                            }catch(e){
                          //  	log.error("${e}")
                            }
                        }
                      //      log.debug "Switch:" + device.currentValue("switch")
                            
                      
                    }
                }
            }
        }
    }
    
    totalMap['state'] = status
    totalMap['attributes'] = resultMap
	def deviceJson = new groovy.json.JsonOutput().toJson(totalMap)
//	log.debug "GET =======>>> ${params}, status: ${resultMap}"
	render contentType: "application/json", data: deviceJson  
}

def existValueInList(list, value){
	for (item in list) {
    	if(item == value){
        	return True
        }
    }
    return False
}

def updateSTDevice(){
//	log.debug "POST >>>> param:${params}"
	def state = "${params.turn}"
	CAPABILITY_MAP.each { key, capability ->
        capability["attributes"].each { attribute ->
        	if(settings[key]){
            	settings[key].each {device ->
                	def dni = device.deviceNetworkId
                    if(dni == params.dni){
                    
                    	def theCommands = device.supportedCommands
                        if(existValueInList(theCommands, "on") == True || existValueInList(theCommands, "off") == True){
                        	device."$params.turn"()
                        }else if(existValueInList(theCommands, "lock") == True || existValueInList(theCommands, "unlock") == True){
                        	if(state == "on"){
                            	device.lock()
                            }else{
                            	device.unlock()
                            }
                        }else if(existValueInList(theCommands, "lock") == True || existValueInList(theCommands, "unlock") == True){
                        	if(state == "on"){
                            	device.arrived()
                            }else{
                            	device.departed();
                            }
                        }
                              
                    }
                }
            }
        }
    }
	render contentType: "text/html", data: state  
}

def authError() {
    [error: "Permission denied"]
}

def renderConfig() {
    def configJson = new groovy.json.JsonOutput().toJson([
        description: "HA Connector API",
        platforms: [
            [
                platform: "SmartThings HA Connector",
                name: "HA Connector",
                app_url: apiServerUrl("/api/smartapps/installations/"),
                app_id: app.id,
                access_token:  state.accessToken
            ]
        ],
    ])

    def configString = new groovy.json.JsonOutput().prettyPrint(configJson)
    render contentType: "text/plain", data: configString
}

mappings {
    if (!params.access_token || (params.access_token && params.access_token != state.accessToken)) {
        path("/config")                         { action: [GET: "authError"] }
        path("/update")                         { action: [GET: "authError"]  }
        path("/getSTDevices")                   { action: [GET: "authError"]  }
        path("/getHADevices")                   { action: [GET: "authError"]  }
        path("/get") {
            action: [
                GET: "authError",
                POST: "authError"
            ]
        }

    } else {
        path("/config")                         { action: [GET: "renderConfig"]  }
        path("/update")                         { action: [GET: "updateDevice"]  }
        path("/getSTDevices")                   { action: [GET: "getSTDevices"]  }
        path("/getHADevices")                   { action: [GET: "getHADevices"]  }
  		path("/get") {
            action: [
                GET: "getSTDevice",
                POST: "updateSTDevice"
            ]
        }
    }
}