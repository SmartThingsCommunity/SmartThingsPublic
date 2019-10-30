/**
 *  Copyright 2017 SmartThings
 *
 *  Simulates a dimmer switch, including physical operation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition (name: "Simulated Dimmer Switch", namespace: "smartthings/testing", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: false, mnmn: "SmartThings", vid: "generic-dimmer") {
        capability "Health Check"
        capability "Actuator"
        capability "Sensor"

        capability "Switch"
        capability "Switch Level"
        capability "Refresh"
        capability "Configuration"

        command    "onPhysical"
        command    "offPhysical"
        command    "setLevelPhysical"

        command    "markDeviceOnline"
        command    "markDeviceOffline"
    }

    preferences {
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOn", defaultState: true
                attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOn"
                attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "setLevel"
            }
            tileAttribute ("brightnessLabel", key: "SECONDARY_CONTROL") {
                attributeState "Brightness", label: '${name}', defaultState: true
            }
        }


        valueTile("physicalLabel", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "label", label: "Simulate\nPhysical\nOperations", defaultState: true
        }
        standardTile("physicalOn", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Physical On", action: "onPhysical", icon: "st.Home.home30", backgroundColor: "#ffffff"
        }
        standardTile("physicalOff", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Physical Off", action: "offPhysical", icon: "st.Home.home30", backgroundColor: "#ffffff"
        }

        valueTile("physicalLevelLabel", "device.switch", width: 2, height: 1, decoration: "flat") {
            state "label", label: "Physical Level", defaultState: true
        }
        controlTile("physicalLevelSlider", "device.level", "slider", width: 4, height: 1, inactiveLabel: false, range: "(1..99)") {
            state "physicalLevel", action: "setLevelPhysical"
        }

        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: "",  action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("reset", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "Reset", action: "configure"
        }

        standardTile("deviceHealthControl", "device.healthStatus", decoration: "flat", width: 2, height: 2, inactiveLabel: false) {
            state "online",  label: "ONLINE", backgroundColor: "#00A0DC", action: "markDeviceOffline", icon: "st.Health & Wellness.health9", nextState: "goingOffline", defaultState: true
            state "offline", label: "OFFLINE", backgroundColor: "#E86D13", action: "markDeviceOnline", icon: "st.Health & Wellness.health9", nextState: "goingOnline"
            state "goingOnline", label: "Going ONLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
            state "goingOffline", label: "Going OFFLINE", backgroundColor: "#FFFFFF", icon: "st.Health & Wellness.health9"
        }

        main(["switch"])
        details(["switch", "physicalLabel", "physicalOn", "physicalOff", "physicalLevelLabel", "physicalLevelSlider", "deviceHealthControl", "refresh", "reset"])
    }
}

// parse events into attributes
def parse(String description) {
    log.trace "parse $description"
    def parsedEvents
    def pair = description?.split(":")
    if (!pair || pair.length < 2) {
        log.warn "parse() could not extract an event name and value from '$description'"
    } else {
        String name = pair[0]?.trim()
        if (name) {
            name = name.replaceAll(~/\W/, "_").replaceAll(~/_{2,}?/, "_")
        }
        parsedEvents = createEvent(name: name, value: pair[1]?.trim())
    }
    return parsedEvents
}

def installed() {
    log.trace "Executing 'installed'"
    configure()
}

def updated() {
    log.trace "Executing 'updated'"
    initialize()
}

//
// command methods
//
def refresh() {
    log.trace "Executing 'refresh'"
    // ummm.... not much to do here without a physical device
}

def configure() {
    log.trace "Executing 'configure'"
    // this would be for a physical device when it gets a handler assigned to it

    // for HealthCheck
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme: "untracked"].encodeAsJson(), displayed: false)
    markDeviceOnline()

    initialize()
}

def on() {
    log.trace "Executing 'on'"
    turnOn()
}

def off() {
    log.trace "Executing 'off'"
    turnOff()
}

def setLevel(value) {
    log.trace "Executing setLevel $value"
    Map levelEventMap = buildSetLevelEvent(value)
    if (levelEventMap.value == 0) {
        turnOff()
        // notice that we don't set the level to 0'
    } else {
        implicitOn()
        sendEvent(levelEventMap)
    }
}

def setLevel(value, duration) {
    log.trace "Executing setLevel $value (ignoring duration)"
    setLevel(value)
}

def markDeviceOnline() {
    setDeviceHealth("online")
}

def markDeviceOffline() {
    setDeviceHealth("offline")
}

private setDeviceHealth(String healthState) {
    log.debug("healthStatus: ${device.currentValue('healthStatus')}; DeviceWatch-DeviceStatus: ${device.currentValue('DeviceWatch-DeviceStatus')}")
    // ensure healthState is valid
    List validHealthStates = ["online", "offline"]
    healthState = validHealthStates.contains(healthState) ? healthState : device.currentValue("healthStatus")
    // set the healthState
    sendEvent(name: "DeviceWatch-DeviceStatus", value: healthState)
    sendEvent(name: "healthStatus", value: healthState)
}

private initialize() {
    log.trace "Executing 'initialize'"
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "level", value: 100)
}

private Map buildSetLevelEvent(value) {
    def intValue = value as Integer
    def newLevel = Math.max(Math.min(intValue, 99), 0)
    Map eventMap = [name: "level", value: newLevel, unit: "%"]
    return eventMap
}

/**
 * Turns device on if it is not already on
 */
private implicitOn() {
    if (device.currentValue("switch") != "on") {
        turnOn()
    }
}

/*
 * no-frills turn-on, no log, no simulation
 */
private turnOn() {
    sendEvent(name: "switch", value: "on")
}

/**
 * no-frills turn-off, no log, no simulation
 */
private turnOff() {
    sendEvent(name: "switch", value: "off")
}

// Generate pretend physical events
private onPhysical() {
    log.trace "Executing 'onPhysical'"
    sendEvent(name: "switch", value: "on", type: "physical")
}

private offPhysical() {
    log.trace "Executing 'offPhysical'"
    sendEvent(name: "switch", value: "off", type: "physical")
}

private setLevelPhysical(value) {
    log.trace "Executing 'setLevelPhysical'"
    Map eventMap = buildSetLevelEvent(value)
    if (eventMap.value == 0) eventMap.value = 1 // can't turn it off by physically setting level
    eventMap.type = "physical"
    sendEvent(eventMap)
}
