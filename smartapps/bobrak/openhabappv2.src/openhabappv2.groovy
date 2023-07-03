/**
 *  OpenHabAppV2
 *
 *  Description
 *   Provides two way communications between a Smartthings Hub and OpenHAB
 *   Messages from OpenHAB with the following paths are supported and perform the following functions
 *    /state  - returns the state of the specified device and attribute, i.e. on, off, 95
 *    /update - Updates the state of the specified device and attribute
 *    /discovery - Returns a list of the devices
 *    /error - Returns error messages to OpenHAB for logging
 *   Messages are sent to OpenHAB with the following paths
 *    /smartthings/push - When an event occurs on a monitored device the new value is sent to OpenHAB
 *
 *  Authors
 *   - rjraker@gmail.com - 1/30/17 - 
 *   - st.john.johnson@gmail.com and jeremiah.wuenschel@gmail.com- original code for interface with another device
 *
 *  Copyright 2016, 2017
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

// Massive lookup tree
@Field CAPABILITY_MAP = [
    "accelerationSensor": [
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
    "battery": [
        name: "Battery",
        capability: "capability.battery",
        attributes: [
            "battery"
        ]
    ],
    "beacon": [
        name: "Beacon",
        capability: "capability.beacon",
        attributes: [
            "presence"
        ]
    ],
    "bulb": [
        name: "Bulb",
        capability: "capability.bulb",
        attributes: [
            "switch"
        ],
        action: "actionOnOff"
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
    "contactSensor": [
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
    "estimatedTimeOfArrival": [
        name: "Estimated Time Of Arrival",
        capability: "capability.estimatedTimeOfArrival",
        attributes: [
            "eta"
        ]
    ],
    "garageDoorControl": [
        name: "Garage Door Control",
        capability: "capability.garageDoorControl",
        attributes: [
            "door"
        ],
        action: "actionOpenClosed"
    ],
    "holdableButton": [
        name: "Holdable Button",
        capability: "capability.holdableButton",
        attributes: [
            "button",
            "numberOfButtons"
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
    "imageCapture": [
        name: "Image Capture",
        capability: "capability.imageCapture",
        attributes: [
            "image"
        ]
    ],
     "indicator": [
        name: "Indicator",
        capability: "capability.indicator",
        attributes: [
            "indicatorStatus"
        ],
        action: indicator
    ],
    "infraredLevel": [
        name: "Infrared Level",
        capability: "capability.infraredLevel",
        attributes: [
            "infraredLevel"
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
    "lockOnly": [
        name: "Lock Only",
        capability: "capability.lockOnly",
        attributes: [
            "lock"
        ],
        action: "actionLock"
    ],
    "mediaController": [
        name: "Media Controller",
        capability: "capability.mediaController",
        attributes: [
            "activities",
            "currentActivity"
        ]
    ],
    "motionSensor": [
        name: "Motion Sensor",
        capability: "capability.motionSensor",
        attributes: [
            "motion"
        ],
        action: "actionActiveInactive"
    ],
    "musicPlayer": [
        name: "Music Player",
        capability: "capability.musicPlayer",
        attributes: [
            "status",
            "level",
            "trackDescription",
            "trackData",
            "mute"
        ],
        action: "actionMusicPlayer"
    ],
    "outlet": [
        name: "Outlet",
        capability: "capability.outlet",
        attributes: [
            "switch"
        ],
        action: "actionOnOff"
    ],
    "pHMeasurement": [
        name: "pH Measurement",
        capability: "capability.pHMeasurement",
        attributes: [
            "pH"
        ]
    ],
    "powerMeter": [
        name: "Power Meter",
        capability: "capability.powerMeter",
        attributes: [
            "power"
        ]
    ],
    "powerSource": [
        name: "Power Source",
        capability: "capability.powerSource",
        attributes: [
            "powerSource"
        ]
    ],
    "presenceSensor": [
        name: "Presence Sensor",
        capability: "capability.presenceSensor",
        attributes: [
            "presence"
        ]
    ],
    "relativeHumidityMeasurement": [
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
            "smoke",
            "carbonMonoxide"
        ]
    ],
    "soundPressureLevel": [
        name: "Sound Pressure Level",
        capability: "capability.soundPressureLevel",
        attributes: [
            "soundPressureLevel"
        ]
    ],
    "soundSensor": [
        name: "Sound Sensor",
        capability: "capability.soundSensor",
        attributes: [
            "phraseSpoken"
        ]
    ],
    "speechRecognition": [
        name: "Speech Recognition",
        capability: "capability.speechRecognition",
        action: [
            "speak"
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
    "switch": [
        name: "Switch",
        capability: "capability.switch",
        attributes: [
            "switch"
        ],
        action: "actionOnOff"
    ],
    "switchLevel": [
        name: "Dimmer Switch",
        capability: "capability.switchLevel",
        attributes: [
            "level"
        ],
        action: "actionLevel"
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
    "temperatureMeasurement": [
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
            "valve"
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
    "waterSensor": [
        name: "Water Sensor",
        capability: "capability.waterSensor",
        attributes: [
            "water"
        ]
    ],
    "windowShade": [
        name: "Window Shade",
        capability: "capability.windowShade",
        attributes: [
            "windowShade"
        ],
        action: "actionOpenClosed"
    ]
]

definition(
    name: "OpenHabAppV2",
    namespace: "bobrak",
    author: "Bob Raker",
    description: "Provides two way communications between a Smartthings Hub and OpenHAB",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections@3x.png"
)

preferences {
    section("Send Notifications?") {
        input("recipients", "contact", title: "Send notifications to", multiple: true, required: false)
    }

    section ("Input") {
        CAPABILITY_MAP.each { key, capability ->
            input key, capability["capability"], title: capability["name"], description: capability["key"], multiple: true, required: false
        }
    }

    section ("Device") {
        input "openhabDevice", "capability.notification", title: "Notify this virtual device", required: true, multiple: false
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    // Unsubscribe from all events
    unsubscribe()
    // Subscribe to stuff
    initialize()
}

def initialize() {
    // Subscribe to new events from devices
    CAPABILITY_MAP.each { key, capability ->
        capability["attributes"].each { attribute ->
            if ( settings[key] != null ) {
                subscribe(settings[key], attribute, inputHandler)
                log.debug "Subscribing inputHandler to device \"${settings[key]}\" with attribute \"${attribute}\""
             }
        }
    }

    // Subscribe to events from the openhabDevice
    log.debug "Subscribing to event handler ${openHabDevice}"
    subscribe(openhabDevice, "message", openhabMessageHandler)
}

// Receive an event from OpenHAB via the openhabDevice
def openhabMessageHandler(evt) {
    def json = new JsonSlurper().parseText(evt.value)
    log.debug "Received device event from Message : ${json}"
    switch (json.path) {
        case "update":
            openhabUpdateHandler (evt)
            break
        case "state":
            openhabStateHandler (evt)
            break
        case "discovery":
            openhabDiscoveryHandler (evt)
            break
        default:
            log.debug "Received device event from Message **** UNEXPECTED **** : ${json}"
    }
}

// Handler for "current" state requests
def openhabStateHandler(evt) {
    def json = new JsonSlurper().parseText(evt.value)
    log.debug "Received state event from openhabDevice: ${json}"

    // Get the CAPABILITY_MAP entry for this device type
    def capability = CAPABILITY_MAP[json.capabilityKey]
    if (capability == null) {
        log.error "No capability: \"${json.capabilityKey}\" exists, make sure there is a CAPABILITY_MAP entry for this capability."
        def jsonOut = new JsonOutput().toJson([
            path: "/smartthings/error",
            body: [
                message: "Requested current state information for CAPABILITY: \"${json.capabilityKey}\" but this is not defined in the SmartApp"
            ]
        ]) 
        log.debug "Returning ${jsonOut}"
        openhabDevice.deviceNotification(jsonOut)
        return
    }
    
    // Verify the attribute is on this capability
    if (! capability.attributes.contains(json.capabilityAttribute) ) {
        log.error "Capability \"${json.capabilityKey}\" does NOT contain the expected attribute: \"${json.capabilityAttribute}\", make sure the a CAPABILITY_MAP for this capability contains the missing attribte."
        def jsonOut = new JsonOutput().toJson([
            path: "/smartthings/error",
            body: [
                message: "Requested current state information for CAPABILITY: \"${json.capabilityKey}\" with attribute: \"${json.capabilityAttribute}\" but this is attribute not defined for this capability in the SmartApp"
            ]
        ]) 
        log.debug "Returning ${jsonOut}"
        openhabDevice.deviceNotification(jsonOut)
        return
    }
    
    
    // Look for the device associated with this capability and return the value of the specified attribute
    settings[json.capabilityKey].each {device ->
        if (device.displayName == json.deviceDisplayName) {
            // Have the device, get the value and return the correct message
            def currentState = device.currentValue(json.capabilityAttribute)
            def jsonOut = new JsonOutput().toJson([
                path: "/smartthings/state",
                body: [
                    deviceDisplayName: device.displayName,
                    capabilityAttribute: json.capabilityAttribute,
                    value: currentState
                ]
            ]) 

            log.debug "Returning ${jsonOut}"
            openhabDevice.deviceNotification(jsonOut)
        }
    }
}

// Update a device when requested from OpenHAB
def openhabUpdateHandler(evt) {
    def json = new JsonSlurper().parseText(evt.value)
    log.debug "Received update event from openhabDevice: ${json}"

    if (json.type == "notify") {
        if (json.name == "Contacts") {
            sendNotificationToContacts("${json.value}", recipients)
        } else {
            sendNotificationEvent("${json.value}")
        }
        return
    }

    // Get the CAPABILITY_MAP entry for this device type
    def capability = CAPABILITY_MAP[json.capabilityKey]
    if (capability == null) {
        log.error "No capability: \"${json.capabilityKey}\" exists, make sure there is a CAPABILITY_MAP entry for this capability."
        def jsonOut = new JsonOutput().toJson([
            path: "/smartthings/error",
            body: [
                message: "Update failed device displayName of: \"${json.deviceDisplayName}\" with CAPABILITY: \"${json.capabilityKey}\" because that CAPABILTY does not exist in the SmartApp"
            ]
        ]) 
        log.debug "Returning ${jsonOut}"
        openhabDevice.deviceNotification(jsonOut)
        return
    }
    
    // Look for the device associated with this capability and perform the requested action
    settings[json.capabilityKey].each {device ->
        if (device.displayName == json.deviceDisplayName) {
            // log.debug "openhabUpdateHandler - found device for ${json.deviceDisplayName}"
            if (capability.containsKey("action")) {
                log.debug "openhabUpdateHandler - Capability ${capability.name} with device name ${device.displayName} changed to ${json.value} using action ${capability.action}"
                def action = capability["action"]
                // Yes, this is calling the method dynamically
                "$action"(device, json.capabilityAttribute, json.value)
            }
        }
    }
}

// Send a list of all devices to OpenHAB - used during OpenHAB's discovery process
def openhabDiscoveryHandler(evt) {
    log.debug "Entered discovery handler"
    def results = []

    CAPABILITY_MAP.each { key, capability ->
        capability["attributes"].each { attribute ->
            settings[key].each {device ->
                // The device info has to be returned as a string. It will be parsed into device data on the OpenHAB side
                def deviceInfo = "{\"capability\": \"${key}\", \"attribute\": \"${attribute}\", \"name\": \"${device.displayName}\", \"id\": \"${device.id}\" }" 
                results.push(deviceInfo)
            }
        }
    }

    def json = new groovy.json.JsonOutput().toJson([
        path: "/smartthings/discovery",
        body: results
    ])

    log.debug "Discovery is returning JSON: ${json}"

    openhabDevice.deviceNotification(json)
}

// Receive an event from a device and send it onto OpenHAB
def inputHandler(evt) {
    def device = evt.device
    def capabilities = device.capabilities
    
    def json = new JsonOutput().toJson([
        path: "/smartthings/state",
        body: [
            deviceDisplayName: evt.displayName,
            value: evt.value,
            capabilityAttribute: evt.name
        ]
    ])

    log.debug "Forwarding device event to openhabDevice: ${json}"
    openhabDevice.deviceNotification(json)
}


// +---------------------------------+
// | WARNING, BEYOND HERE BE DRAGONS |
// +---------------------------------+
// These are the functions that handle incoming messages from MQTT.
// I tried to put them in closures but apparently SmartThings Groovy sandbox
// restricts you from running clsures from an object (it's not safe).

def actionAlarm(device, attribute, value) {
    switch (value) {
        case "strobe":
            device.strobe()
        break
        case "siren":
            device.siren()
        break
        case "off":
            device.off()
        break
        case "both":
            device.both()
        break
    }
}

def actionColor(device, attribute, value) {
    switch (attribute) {
        case "hue":
            device.setHue(value as float)
        break
        case "saturation":
            device.setSaturation(value as float)
        break
        case "color":
            def values = value.split(',')
            def colormap = ["hue": values[0] as float, "saturation": values[1] as float]
            device.setColor(colormap)
        break
    }
}

def actionOpenClosed(device, attribute, value) {
    if (value == "open") {
        device.open()
    } else if (value == "close") {
        device.close()
    }
}

def actionOnOff(device, attribute, value) {
    if (value == "off") {
        device.off()
    } else if (value == "on") {
        device.on()
    }
}

def actionActiveInactive(device, attribute, value) {
    if (value == "active") {
        device.active()
    } else if (value == "inactive") {
        device.inactive()
    }
}

def actionThermostat(device, attribute, value) {
    switch(attribute) {
        case "heatingSetpoint":
            device.setHeatingSetpoint(value)
        break
        case "coolingSetpoint":
            device.setCoolingSetpoint(value)
        break
        case "thermostatMode":
            device.setThermostatMode(value)
        break
        case "thermostatFanMode":
            device.setThermostatFanMode(value)
        break
    }
}

def actionMusicPlayer(device, attribute, value) {
    switch(attribute) {
        case "level":
            device.setLevel(value)
        break
        case "mute":
            if (value == "muted") {
                device.mute()
            } else if (value == "unmuted") {
                device.unmute()
            }
        break
    }
}

def actionColorTemperature(device, attribute, value) {
    device.setColorTemperature(value as int)
}

def actionLevel(device, attribute, value) {
    log.debug "Setting device ${device} with attribute ${attribute} to value ${value}"
    device.setLevel(value as int)
}

def actionConsumable(device, attribute, value) {
    device.setConsumableStatus(value)
}

def actionLock(device, attribute, value) {
    if (value == "locked") {
        device.lock()
    } else if (value == "unlocked") {
        device.unlock()
    }
}

def actionCoolingThermostat(device, attribute, value) {
    device.setCoolingSetpoint(value)
}

def actionThermostatFan(device, attribute, value) {
    device.setThermostatFanMode(value)
}

def actionHeatingThermostat(device, attribute, value) {
    device.setHeatingSetpoint(value)
}

def actionThermostatMode(device, attribute, value) {
    device.setThermostatMode(value)
}

def actionTimedSession(device, attribute, value) {
    if (attribute == "timeRemaining") {
        device.setTimeRemaining(value)
    }
}

// The following functions return the current state of a device
def switchState(device, attribute) {
    device.currentValue(attribute);
}