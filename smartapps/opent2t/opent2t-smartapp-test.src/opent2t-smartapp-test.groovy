/**
 *  OpenT2T SmartApp Test
 *
 *  Copyright 2016 OpenT2T
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "OpenT2T SmartApp Test",
    namespace: "opent2t",
    author: "OpenT2T",
    description: "Test app to test end to end SmartThings scenarios via OpenT2T",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

//Device Inputs
preferences {
section("Allow OpenT2T Test SmartApp to control these things...") {
		input "switches", "capability.switch", title: "Which Switches and Lights?", multiple: true, required: false
		input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
		input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors", multiple: true, required: false
        input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors", multiple: true, required: false
        input "waterSensors", "capability.waterSensor", title: "Which Water Leak Sensors?", multiple: true, required: false
        input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
    	input "garageDoors", "capability.garageDoorControl", title: "Which Garage Doors?", multiple: true, required: false
		input "cameras", "capability.videoCamera", title: "Which Cameras?",  multiple: true, required: false
    }
}

//API external Endpoints
mappings {
	path("/subscriptionURL/:url") {
        action: [
            PUT: "updateEndpointURL"
        ]
    }
    path("/connectionId/:connId") {
        action: [
            PUT: "updateConnectionId"
        ]
    }
	path("/devices") {
		action: [
			GET: "getDevices"
		]
	}
    path("/devices/:id") {
		action: [
			GET: "getDevice"
		]
	}
    path("/update/:id") {
        action: [
          PUT: "updateDevice"
        ]
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	state.connectionId = ""
    state.endpointURL = "https://ifs.windows-int.com/v1/cb/81C7E77B-EABC-488A-B2BF-FEC42F0DABD2/notify"
	registerSubscriptions()
}

//Register events for each device
def registerSubscriptions() {
	registerChangeHandler(switches)
	registerChangeHandler(motionSensors)
    registerChangeHandler(presenceSensors)
    registerChangeHandler(contactSensors)
    registerChangeHandler(waterSensors)
    registerChangeHandler(locks)
    registerChangeHandler(garageDoors)
    registerChangeHandler(cameras)
}

def registerChangeHandler(myList) {
	myList.each { myDevice ->
		def theAtts = myDevice.supportedAttributes
		theAtts.each {att ->
		    subscribe(myDevice, att.name, eventHandler)
    	log.debug "Registering ${myDevice.displayName}.${att.name}"
		}
	}
}

//When events are triggered, send HTTP post to web socket servers
def eventHandler(evt) {

	def evt_device_id = evt.deviceId
    def evt_device_value = evt.value
    def evt_name = evt.name
    def evt_device = evt.device
    def evt_deviceType = getDeviceType(evt_device);
    def params = [
    	uri: "${state.endpointURL}/${state.connectionId}",
        body: [
            name: evt_device.displayName,
            id: evt_device.id,
            deviceType:evt_deviceType, 
            attributes: deviceAttributeList(evt_device) 
        ]
    ]
    try {
        log.debug "POST URI: ${params.uri}"
        log.debug "Payload: ${params.body}"
        httpPostJson(params) { resp ->
            resp.headers.each {
                log.debug "${it.name} : ${it.value}"
            }
            log.debug "response status code: ${resp.status}"
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.debug "something went wrong: $e"
    }
}

//Endpoints function; update subcription endpoint url [state.endpoint]
void updateEndpointURL() {
    state.endpointURL = params.url
    log.debug "Updated EndpointURL to ${state.endpointURL}"
}

//Endpoints function; update global variable [state.connectionId]
void updateConnectionId() {
    def connId = params.connId
    state.connectionId = connId
    log.debug "Updated ConnectionID to ${state.connectionId}"
}

//Endpoints functions; return data from all devices supported in JSON format
def getDevices() {
	def deviceData = [] 
    switches.each {
    	def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    }     
    motionSensors.each  {
   		def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    } 
    contactSensors.each {
    	def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    }
    presenceSensors.each {
    	def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    }
    waterSensors.each {
    	def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    }
    locks.each  {  
    	def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    } 
    garageDoors.each  {  
    	def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    } 
	cameras.each  {  
    	def deviceType = getDeviceType(it)
        deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)] 
    } 
    log.debug "getDevices, return: ${deviceData}"
    return deviceData 
}

//Endpoints functions; return data from a specific device in JSON format
def getDevice() {    
    def it = findDevice(params.id)
    def deviceType = getDeviceType(it)
    def device = [name: it.displayName, id: it.id, deviceType:deviceType, attributes: deviceAttributeList(it)]
    log.debug "getDevice, return: ${device}"
    return device
}

//Endpoints functions; update device data
void updateDevice() {    
    def device = findDevice(params.id)
    request.JSON.each {   
        def command = it.key
		def value = it.value
		if (command){
            def commandList = mapDeviceCommands(command, value)
            command = commandList[0]
            value = commandList[1]
            if (!device) {
                log.debug "updateDevice, Device not found"
                httpError(404, "Device not found")
            } else if (!device.hasCommand(command)) {
                log.debug "updateDevice, Device does not have the command"        	
                httpError(404, "Device does not have the command")
            } else {
                if (command == "setColor") {
                    log.debug "Update: [${command}, ${value}], device: ${device}"
                    device."$command"(hex: value)
                } else if(value.isNumber()) {
                	def intValue = value as Integer
                    log.debug "Update: [${command}, ${intValue}(int)]"
                    device."$command"(intValue)
                } else if (value){
                    log.debug "Update: [${command}, ${value}]"
                    device."$command"(value)
                } else {
                    device."$command"()
                }
            }
        }
	}
}

//Private Functions; manually map each device to a type given it's capabilities
private getDeviceType(device) {
	def deviceType
    def caps = device.capabilities
	caps.each {
    	log.debug "capabilities: [${device}, ${caps}]"
		if(it.name.toLowerCase().contains("switch")) {
        	deviceType = "switch"
        }
        if(it.name.toLowerCase().contains("water")) {
        	deviceType = "waterSensor"
        }
        if(it.name.toLowerCase().contains("motion")) {
        	deviceType = "motionSensor"
        }
        if(it.name.toLowerCase().contains("presence")) {
        	deviceType = "presenceSensor"
        }
        if(it.name.toLowerCase().contains("contact")) {
        	deviceType = "contactSensor"
        }
        if(it.name.toLowerCase().contains("lock")) {
        	deviceType = "lock"
        }
        if(it.name.toLowerCase().contains("level")) {
        	deviceType = "light"
        }
        if(it.name.toLowerCase().contains("garageDoorControl")) {
        	deviceType = "garageDoor"
        }
        if(it.name.toLowerCase().contains("video")) {
        	deviceType = "camera"
        }	
    }
    return deviceType
}

//Private Functions; return device
private findDevice(deviceId) {
	def device = switches.find { it.id == deviceId }
  	if (device) return device
	device = motionSensors.find { it.id == deviceId }
    if (device) return device
	device = waterSensors.find { it.id == deviceId }
	if (device) return device
    device = contactSensors.find { it.id == deviceId }
	if (device) return device
    device = presenceSensors.find { it.id == deviceId }
	if (device) return device
  	device = locks.find { it.id == deviceId }
	if (device) return device
  	device = garageDoors.find { it.id == deviceId }
   	if (device) return device
    device = cameras.find { it.id == deviceId }
	return device
 }
 
//Private Functions; return a list of device attributes
private deviceAttributeList(device) {
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

//Private Functions; map device command and value. 
//input command and value are from UWP,
//returns resultCommand and resultValue that corresponds with function and value in SmartApps
private mapDeviceCommands(command, value) {
	log.debug "mapDeviceCommands: [${command}, ${value}]"
	def resultCommand = command
    def resultValue = value
    switch (command) {
        case "switch":
        	if (value == 1 || value == "1" || value == "on") {
                resultCommand = "on"
                resultValue = ""
            } else if (value == 0 || value == "0" || value == "off") {
                resultCommand = "off"
                resultValue = ""
            }
            break
    	case "level":
        	resultCommand = "setLevel"
            resultValue = value
            break
    	case "hue":
        	resultCommand = "setHue"
            resultValue = value
            Log
            break
        case "saturation":
        	resultCommand = "setSaturation"
            resultValue = value
            break
        case "color":    
        	resultCommand = "setColor"
            resultValue = value
        case "locked":
        	if (value == 1 || value == "1" || value == "lock") {
                resultCommand = "lock"
                resultValue = ""
            }
            else if (value == 0 || value == "0" || value == "unlock") {
                resultCommand = "unlock"
                resultValue = ""
            }
            break
     }
     
     return [resultCommand,resultValue]

}