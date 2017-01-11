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

/** --------------------+---------------+-----------------------+------------------------------------
 *  Device Type         | Attribute Name| Commands              | Attribute Values
 *  --------------------+---------------+-----------------------+------------------------------------
 *  switches            | switch        | on, off               | on, off
 *  motionSensors       | motion        |                       | active, inactive
 *  contactSensors      | contact       |                       | open, closed
 *  presenceSensors     | presence      |                       | present, 'not present'
 *  temperatureSensors  | temperature   |                       | <numeric, F or C according to unit>
 *  accelerationSensors | acceleration  |                       | active, inactive
 *  waterSensors        | water         |                       | wet, dry
 *  lightSensors        | illuminance   |                       | <numeric, lux>
 *  humiditySensors     | humidity      |                       | <numeric, percent>
 *  locks               | lock          | lock, unlock          | locked, unlocked
 *  garageDoors         | door          | open, close           | unknown, closed, open, closing, opening
 *  cameras             | image         | take                  | <String>
 *  thermostats         | thermostat    | setHeatingSetpoint,   | temperature, heatingSetpoint, coolingSetpoint,
 *	                    |				| setCoolingSetpoint,   | thermostatSetpoint, thermostatMode,
 *                      |				| off, heat, cool, auto,| thermostatFanMode, thermostatOperatingState
 *                      |				| emergencyHeat,        |
 *                      |				| setThermostatMode,    |
 *                      |				| fanOn, fanAuto,       |
 *                      |				| fanCirculate,         |
 *                      |				| setThermostatFanMode  |
 *  --------------------+---------------+-----------------------+------------------------------------
 */

//Device Inputs
preferences {
	section("Allow <PLACEHOLDER: Your App Name> to control these things...") {
		input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors", multiple: true, required: false
		input "garageDoors", "capability.garageDoorControl", title: "Which Garage Doors?", multiple: true, required: false
 		input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
		input "cameras", "capability.videoCapture", title: "Which Cameras?",  multiple: true, required: false
		input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
		input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors", multiple: true, required: false
		input "switches", "capability.switch", title: "Which Switches and Lights?", multiple: true, required: false
		input "thermostats", "capability.thermostat", title: "Which Thermostat?", multiple: true, required: false
		input "waterSensors", "capability.waterSensor", title: "Which Water Leak Sensors?", multiple: true, required: false
    }
}

def getInputs() {
	def inputList = []
	inputList += contactSensors?: []
	inputList += garageDoors?: []
	inputList += locks?: []
	inputList += cameras?: []
	inputList += motionSensors?: []
	inputList += presenceSensors?: []
	inputList += switches?: []
	inputList += thermostats?: []
	inputList += waterSensors?: []
    return inputList
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
	 path("/subscription/:id") {
		action: [
		  POST: "registerDeviceChange",
		  DELETE: "unregisterDeviceChange"
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
	registerSubscriptions()
}

def initialize() {
	state.connectionId = ""
	state.endpointURL = "https://ifs.windows-int.com/v1/cb/81C7E77B-EABC-488A-B2BF-FEC42F0DABD2/notify"
	registerSubscriptions()
}

//Subscribe events for all devices
def registerSubscriptions() {
	registerChangeHandler(inputs)
}

//Subscribe to events from a list of devices
def registerChangeHandler(myList) {
	myList.each { myDevice ->
		def theAtts = myDevice.supportedAttributes
		theAtts.each {att ->
			subscribe(myDevice, att.name, eventHandler)
			log.info "Registering ${myDevice.displayName}.${att.name}"
		}
	}
}

//Endpoints function: Subscribe to events from a specific device
def registerDeviceChange() {
	def myDevice = findDevice(params.id)
	def theAtts = myDevice.supportedAttributes
	try {
		theAtts.each {att ->
			subscribe(myDevice, att.name, eventHandler)
			log.info "Registering ${myDevice.displayName}.${att.name}"
		}
		return ["succeed"]
	} catch (e) {
		httpError(500, "something went wrong: $e")
	}
}

//Endpoints function: Unsubscribe to events from a specific device
def unregisterDeviceChange() {
	def myDevice = findDevice(params.id)
	try {
		unsubscribe(myDevice)
		log.info "Unregistering ${myDevice.displayName}"
		return ["succeed"]
	} catch (e) {
		httpError(500, "something went wrong: $e")
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
			manufacturer:evt_device.getManufacturerName(), 
			model:evt_device.getModelName(),
			attributes: deviceAttributeList(evt_device) 
		]
	]
	try {
		log.trace "POST URI: ${params.uri}"
		log.trace "Payload: ${params.body}"
		httpPostJson(params) { resp ->
			resp.headers.each {
				log.debug "${it.name} : ${it.value}"
			}
			log.trace "response status code: ${resp.status}"
			log.trace "response data: ${resp.data}"
		}
	} catch (e) {
		log.debug "something went wrong: $e"
	}
}

//Endpoints function: update subcription endpoint url [state.endpoint]
void updateEndpointURL() {
	state.endpointURL = params.url
	log.info "Updated EndpointURL to ${state.endpointURL}"
}

//Endpoints function: update global variable [state.connectionId]
void updateConnectionId() {
	def connId = params.connId
	state.connectionId = connId
	log.info "Updated ConnectionID to ${state.connectionId}"
}

//Endpoints function: return all device data in json format
def getDevices() {
	def deviceData = [] 
    inputs?.each {
		def deviceType = getDeviceType(it)
		if(deviceType == "thermostat")
		{
			deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, manufacturer:it.getManufacturerName(), model:it.getModelName(), attributes: deviceAttributeList(it), locationMode: getLocationModeInfo()] 
		}
		else
		{
			deviceData << [name: it.displayName, id: it.id, deviceType:deviceType, manufacturer:it.getManufacturerName(), model:it.getModelName(), attributes: deviceAttributeList(it)]
		} 
    }
 
	log.debug "getDevices, return: ${deviceData}"
	return deviceData 
}

//Endpoints function: get device data
def getDevice() {	
	def it = findDevice(params.id)
	def deviceType = getDeviceType(it)
	def device
	if(deviceType == "thermostat")
	{
		device = [name: it.displayName, id: it.id, deviceType:deviceType, manufacturer:it.getManufacturerName(), model:it.getModelName(), attributes: deviceAttributeList(it), locationMode: getLocationModeInfo()] 
	}
	else
	{
		device = [name: it.displayName, id: it.id, deviceType:deviceType, manufacturer:it.getManufacturerName(), model:it.getModelName(), attributes: deviceAttributeList(it)]
	}   
	log.debug "getDevice, return: ${device}"
	return device
}

//Endpoints function: update device data
void updateDevice() {	
	def device = findDevice(params.id)
	request.JSON.each {   
		def command = it.key
		def value = it.value
		if (command){
			def commandList = mapDeviceCommands(command, value)
			command = commandList[0]
			value = commandList[1]
			
			if (command == "setAwayMode") {
				log.info "Setting away mode to ${value}"
				if (location.modes?.find {it.name == value}) {
					location.setMode(value)
				}
			}else if (command == "thermostatSetpoint"){
				switch(device.currentThermostatMode){
					case "cool":
						log.info "Update: ${device.displayName}, [${command}, ${value}]"
						device.setCoolingSetpoint(value)
						break
					case "heat":
					case "emergency heat":
						log.info "Update: ${device.displayName}, [${command}, ${value}]"
						device.setHeatingSetpoint(value)
						break
					default:
						httpError(501, "this mode: ${device.currentThermostatMode} does not allow changing thermostat setpoint.")
						break
				}
			}else if (!device) {
				log.error "updateDevice, Device not found"
				httpError(404, "Device not found")
			} else if (!device.hasCommand(command)) {
				log.error "updateDevice, Device does not have the command"			
				httpError(404, "Device does not have such command")
			} else {
				if (command == "setColor") {
					log.info "Update: ${device.displayName}, [${command}, ${value}]"
					device."$command"(hex: value)
				} else if(value.isNumber()) {
					def intValue = value as Integer
					log.info "Update: ${device.displayName}, [${command}, ${intValue}(int)]"
					device."$command"(intValue)
				} else if (value){
					log.info "Update: ${device.displayName}, [${command}, ${value}]"
					device."$command"(value)
				} else {
					log.info "Update: ${device.displayName}, [${command}]"
					device."$command"()
				}
			}
		}
	}
}

/*** Private Functions ***/

//Return current location mode info
private getLocationModeInfo() {
	return [mode: location.mode, supported: location.modes.name]
}

//Map each device to a type given it's capabilities
private getDeviceType(device) {
	def deviceType
	def caps = device.capabilities
	log.debug "capabilities: [${device}, ${caps}]"
	log.debug "supported commands: [${device}, ${device.supportedCommands}]"
	caps.each {
		switch(it.name.toLowerCase())
		{
			case "switch":
				deviceType = "switch"
				break
			case "switch level":
				deviceType = "light"
				break
			case "contact sensor":
				deviceType = "contactSensor"
				break
			case "garageDoorControl":
				deviceType = "garageDoor"
				break
			case "lock":
				deviceType = "lock"
				break
			case "video camera":
				deviceType = "camera"
				break
			case "motion sensor":
				deviceType = "motionSensor"
				break
			case "presence sensor":
				deviceType = "presenceSensor"
				break
			case "thermostat":
				deviceType = "thermostat"
				break
			case "water sensor":
				deviceType = "waterSensor"
				break   
			default:
				break
		}
	}
	return deviceType
}

//Return a specific device give the device ID.
private findDevice(deviceId) {
	return inputs?.find { it.id == deviceId }
}
 
//Return a list of device attributes
private deviceAttributeList(device) {
	device.supportedAttributes.collectEntries { attribute->
		try {
			[ (attribute.name): device.currentValue(attribute.name) ]
		} catch(e) {
			[ (attribute.name): null ]
		}
	}
}

//Map device command and value. 
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
		// light attributes
		case "level":
			resultCommand = "setLevel"
			resultValue = value
			break
		case "hue":
			resultCommand = "setHue"
			resultValue = value
			break
		case "saturation":
			resultCommand = "setSaturation"
			resultValue = value
			break
		case "ct":
			resultCommand = "setColorTemperature"
			resultValue = value
			break
		case "color":	
			resultCommand = "setColor"
			resultValue = value
		// thermostat attributes
		case "hvacMode":
			resultCommand = "setThermostatMode"
			resultValue = value
			break
		case "fanMode":
			resultCommand = "setThermostatFanMode"
			resultValue = value
			break
		case "awayMode":
			resultCommand = "setAwayMode"
			resultValue = value
			break
		case "coolingSetpoint":
			resultCommand = "setCoolingSetpoint"
			resultValue = value
			break
		case "heatingSetpoint":	
			resultCommand = "setHeatingSetpoint"
			resultValue = value
			break
		case "thermostatSetpoint":
			resultCommand = "thermostatSetpoint"
			resultValue = value
			break
		// lock attributes
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
		default:
			break
	 }
	 
	 return [resultCommand,resultValue]
}