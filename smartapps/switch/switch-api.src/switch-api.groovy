/**
 *  Switch API
 *
 *  Copyright 2018 Switch Living S.L.
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
    name: "Switch API",
    namespace: "Switch",
    author: "Switch",
    description: "This API allows the Switch App to connect to a SmartThings hub and monitor and control its selected devices.",
    category: "Convenience",
    iconUrl: "https://s3.eu-central-1.amazonaws.com/switchstore/Logo+with+white+frame.jpg",
    iconX2Url: "https://s3.eu-central-1.amazonaws.com/switchstore/Logo+with+white+frame.jpg",
    iconX3Url: "https://s3.eu-central-1.amazonaws.com/switchstore/Logo+with+white+frame.jpg")


preferences {
	section ('Allow Switch to control these things...') {
        paragraph "You may find a thing in more than one category. Regardless if you select it in one or multiple categories - Switch will have access to it if you select it at least once."
		for (capability in capabilities()) {
        	input "${capability.key}", "capability.${capability.key}", multiple: true, title: capability.value.n, hideWhenEmpty: true, required: false
		}
	}		
}


mappings {

	path("/location") {
    	action: [
			GET: "renderLocation"
 		]
	}

	path("/status") {
    	action: [
			GET: "renderHubStatus"
 		]
	}


	path("/devices") {
    	action: [
			GET: "listDevices"
 		]
	}

	path("/devicesstatus") {
    	action: [
			GET: "listDevicesStatus"
 		]
	}

	path("/command") {
	    action: [
      		PUT: "commandDevice"
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
	log.debug "Initializing Switch API"
	for (capability in capabilities()) {
		settings["${capability.key}"].each { device ->
        	device["supportedAttributes"].each { attribute ->
	            if (!(attribute.name in ["active", "dpEvent", "energy", "energy1", "energy2", "lastActivity", "lastCheckin", "lastPoll", "power", "power1", "power2", "switch1", "switch2"])) {
    	    		subscribe(device, attribute.name, updateHandler)
        	    	log.debug "Subscribing updateHandler to device \"${device}\" with attribute \"${attribute.name}\""
				}
			}
		}
    }
    subscribe(location.hubs[0], "hubStatus", updateHubStatus)
}

def listDevices() {
    def resp = []
	for (capability in capabilities()) {
		settings["${capability.key}"].each {
    		resp << [name: it.displayName, 
    				 deviceid: it.id, 
    				 capabilities: deviceCapabilityList(it),
    				 commands: deviceCommandList(it),
    				 attributes: deviceAttributeList(it)
			]
	    }
    }
    return resp
}


def listDevicesStatus() {
    def resp = []
	for (capability in capabilities()) {
		settings["${capability.key}"].each {
    		resp << [deviceid: it.id, 
    				 attributes: deviceAttributeList(it)
			]
	    }
    }
    return resp
}

def renderLocation() {
	state.homeid = params.homeid
	def location = [latitude: location.latitude,
    				longitude: location.longitude,
    				name: location.name,
  				   ]
    return location
}

def renderHubStatus() {
	def hubstate = [status : location.hubs[0].status]
    return hubstate
}


def updateHandler(evt) {
	try {
    	httpPut("http://api.switch-living.com", "key=FXt5Y7vumm6PJcAdImYXH6Ck5ld38lb63hwTWc6A&technology=SmartThings&homeid=${state.homeid}&uuid=${evt.device.id}&attribute=${evt.name}&value=${evt.value}") { resp ->
		log.debug "updated ${evt.device.displayName} attribute ${evt.name} to ${evt.value}"
        // log.debug "http://api.switch-living.com?key=FXt5Y7vumm6PJcAdImYXH6Ck5ld38lb63hwTWc6A&technology=SmartThings&homeid=${state.homeid}&uuid=${evt.device.id}&attribute=${evt.name}&value=${evt.value}"
		}
	} catch (e) {
    	log.error "something went wrong: $e"
	}
}

def updateHubStatus(evt) {
	try {
    	httpPut("http://api.switch-living.com", "key=o7gn82eRKYXvL29eUmK&technology=SmartThings&homeid=${state.homeid}&hubname=${location.name}&status=${location.hubs[0].status}&event=${evt.name}&value=${evt.value}") { resp ->
		log.debug "updated ${location.name} event ${evt.name} value ${evt.value}"
		}
	} catch (e) {
    	log.error "something went wrong: $e"
	}
}

void commandDevice() {
    def command = request.JSON?.command
	def device = findDevice(request.JSON?.uuid)
	if (request.JSON?.value2) {
		device."$command"(request.JSON?.value1,request.JSON?.value2)
	} else if (request.JSON?.value1) {
	    device."$command"(request.JSON?.value1)
	} else {
		device."$command"()
	}
}



// Helpers

def findDevice(uuid) {
	def foundDevice
	for (capability in capabilities()) {
		settings["${capability.key}"].each { device ->
        	if (device.id == uuid) {
            	foundDevice = device
			}
	    }
    }
    return foundDevice
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


// Capabilities

private static Map capabilities() {
    //n = name
    //a = default attribute
	return [
		accelerationSensor			: [ n: "Acceleration Sensor",			a: "acceleration",			],
		alarm						: [ n: "Alarm",							a: "alarm",					],
		button						: [ n: "Button",						a: "button",				],
		carbonDioxideMeasurement	: [ n: "Carbon Dioxide Measurement",	a: "carbonDioxide",			],
		carbonMonoxideDetector		: [ n: "Carbon Monoxide Detector",		a: "carbonMonoxide",		],
		contactSensor				: [ n: "Contact Sensor",				a: "contact",				],
		doorControl					: [ n: "Door Control",					a: "door",					],
		garageDoorControl			: [ n: "Garage Door Control",			a: "door",					],
		illuminanceMeasurement		: [ n: "Illuminance Measurement",		a: "illuminance",			],
		imageCapture				: [ n: "Image Capture",					a: "image",					],
		lock						: [ n: "Lock",							a: "lock",					],
		mediaController				: [ n: "Media Controller",				a: "currentActivity",		],
		motionSensor				: [ n: "Motion Sensor",					a: "motion",				],
		pHMeasurement				: [ n: "pH Measurement",				a: "pH",					],
		relativeHumidityMeasurement	: [ n: "Relative Humidity Measurement",	a: "humidity",				],
		shockSensor					: [ n: "Shock Sensor",					a: "shock",					],
		sleepSensor					: [ n: "Sleep Sensor",					a: "sleeping",				],
		smokeDetector				: [ n: "Smoke Detector",				a: "smoke",					],
		soundSensor					: [ n: "Sound Sensor",					a: "sound",					],
		switch						: [ n: "Switch",						a: "switch",				],
		tamperAlert					: [ n: "Tamper Alert",					a: "tamper",				],
		temperatureMeasurement		: [ n: "Temperature Measurement",		a: "temperature",			],
		thermostat					: [ n: "Thermostat",					a: "thermostatMode",		],
		valve						: [ n: "Valve",							a: "valve",					],
		waterSensor					: [ n: "Water Sensor",					a: "water",					],
		windowShade					: [ n: "Window Shade",					a: "windowShade",			],
	]
}