/**
 *  HTTP POSTer
 *
 *  Copyright 20165 Timur Fatykhov
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
    name: "HTTP POST for Wink Node Red",
    namespace: "winknodered",
    author: "Timur Fatykhov",
    description: "Sends POST messages from ST devices to WNR URL. Accept switch control from WNR to ST",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
}


preferences {
    section ("Sensors that should go to WNR") {
        input "powers", "capability.powerMeter", title: "Power Meters", required: false, multiple: true
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required: false, multiple: true
        input "humidities", "capability.relativeHumidityMeasurement", title: "Humidities", required: false, multiple: true
        input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
        input "accelerations", "capability.accelerationSensor", title: "Accelerations", required: false, multiple: true
        input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
        input "presence", "capability.presenceSensor", title: "Presence", required: false, multiple: true
        input "switches", "capability.switch", title: "Switches", required: false, multiple: true
	    input "switchLevels", "capability.switchLevel", title: "Switch Levels", required: false, multiple: true        
        input "waterSensors", "capability.waterSensor", title: "Water sensors", required: false, multiple: true
        input "batteries", "capability.battery", title: "Batteries", required: false, multiple: true
        input "energies", "capability.energyMeter", title: "Energy Meters", required: false, multiple: true
        input "illuminances" ,"capability.illuminanceMeasurement", title: "Illuminance Meters" , required: false, multiple: true
    }
    section ("YOUR WNR URL") {
        input "url", "text", title: "Your node-red app URL", required: true
    }
    section ("YOUR WNR ST Key") {
        input "key", "text", title: "Enter same secret ST key you set in WNR", required: true
    }    
}


mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/switches/:id/:command") {
    action: [
      PUT: "updateSwitches"
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
    subscribe(powers, "power", handlePowerEvent)
    subscribe(temperatures, "temperature", handleTemperatureEvent)
    subscribe(waterSensors, "water", handleWaterEvent)
    subscribe(humidities, "humidity", handleHumidityEvent)
    subscribe(contacts, "contact", handleContactEvent)
    subscribe(accelerations, "acceleration", handleAccelerationEvent)
    subscribe(motions, "motion", handleMotionEvent)
    subscribe(presence, "presence", handlePresenceEvent)
    subscribe(switches, "switch", handleSwitchEvent)
	subscribe(switchLevels, "level", handleSwitchLevelEvent)    
    subscribe(batteries, "battery", handleBatteryEvent)
    subscribe(energies, "energy", handleEnergyEvent)
    subscribe(illuminances, "illuminance", handleIlluminanceEvent)
}

def handlePowerEvent(evt) {
    sendValue(evt) { it.toFloat() }
}

def handleTemperatureEvent(evt) {
    sendValue(evt) { it.toFloat() }
}
 
def handleWaterEvent(evt) {
    sendValue(evt) { it == "wet" ? true : false }
}
 
def handleHumidityEvent(evt) {
    sendValue(evt) { it.toFloat()/100 }
}
 
def handleContactEvent(evt) {
    sendValue(evt) { it == "open" ? true : false }
}
 
def handleAccelerationEvent(evt) {
    sendValue(evt) { it == "active" ? true : false }
}
 
def handleMotionEvent(evt) {
    sendValue(evt) { it == "active" ? true : false }
}
 
def handlePresenceEvent(evt) {
    sendValue(evt) { it == "present" ? true : false }
}
 
def handleSwitchEvent(evt) {
    sendValue(evt) { it == "on" ? true : false }
}

def handleSwitchLevelEvent(evt) {
    sendValue(evt) { it.toInteger() }
}
 
def handleBatteryEvent(evt) {
    sendValue(evt) { it.toFloat()/100 }
}
 
def handleEnergyEvent(evt) {
    sendValue(evt) { it.toFloat() }
}

def handleIlluminanceEvent(evt) {
    sendValue(evt) { it.toFloat() }
}

def listSwitches() {
    def resp = []
    switches.each {
        resp << [name: it.displayName, 
        		 id: it.id, 
        		 value: it.currentValue("switch"),
        	     capabilities: deviceCapabilityList(it),
            	 commands: deviceCommandList(it),
            	 attributes: deviceAttributeList(it) ]
    }
    return resp
}

void updateSwitches() {
    // use the built-in request object to get the command parameter
    def command = params.command
	def id = params.id
    def theSwitch = switches.find{ it.id == id }
    def level = request.JSON?.level
    // all switches have the comand
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    switch(command) {
        case "on":
        	if (level && theSwitch.hasCommand('setLevel')) {
            	log.info("level set to "+level)
                theSwitch.setLevel(level)
            } else {
            	theSwitch.on()            
            }
            break
        case "off":
            theSwitch.off()
            break
        default:
            httpError(400, "$command is not a valid command for all switches specified")
    }

}

def deviceCapabilityList(device) {
  	def i=0
  	device.capabilities.collectEntries { capability->
    	[
      		(capability.name):1
    	]
  	}
}

def deviceCommandList(device) {
  	def i=0
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

private sendValue(evt, Closure convert) {
    def compId = URLEncoder.encode(evt.displayName.trim())
    def device = evt.device
    def devId = evt.device.id
    def streamId = evt.name
    def value = convert(evt.value)
    def type = (value == true || value == false ? "boolean" : "float"); 

    log.debug "Sending ${compId}/${streamId} data to ${url}..."
    log.debug "The unit for this event: ${evt.unit}"

	def payload = [
        component: compId,
        devid: devId,
        stream: streamId,
        value: value,
        type: type,
		temperature_scale: location.temperatureScale,
        attributes: deviceAttributeList(device)
    ]

    def params = [
        uri:  url + '/red/smartthings/',
        headers: [
        'Authorization': 'Bearer '+key
        ],
        body: payload
    ]

    try {
    	log.debug params;
        httpPostJson(params) { resp ->
        	log.debug resp.status
        }
    } catch (e) {
        log.debug "something went wrong: $e"
    }

}