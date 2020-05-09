/**
 *  Copyright 2017 SmartThings
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
 *  An enhanced virtual lock that allows for testing failure modes
 *  Author: SmartThings
 *  Date: 2017-08-07
 *
 */

metadata {
    // Automatically generated. Make future change here.
    definition (name: "Simulated Lock", namespace: "smartthings/testing", author: "SmartThings") {
        capability "Actuator"
        capability "Sensor"
        capability "Health Check"

        capability "Lock"
        capability "Battery"
        capability "Refresh"
        capability "Configuration"

        command "jam"
        command "setBatteryLevel"
        command "setJamNextOperation"
        command "clearJamNextOperation"
        attribute "doesNextOperationJam", "enum", ["true", "false"]

        command    "markDeviceOnline"
        command    "markDeviceOffline"
    }

    // Simulated lock
    tiles {
        multiAttributeTile(name:"toggle", type: "generic", width: 6, height: 4){
            tileAttribute ("device.lock", key: "PRIMARY_CONTROL") {
                attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#00A0DC", nextState:"unlocking"
                attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#FFFFFF", nextState:"locking"
                attributeState "unknown", label:'jammed', action:"lock.lock", icon:"st.secondary.activity", backgroundColor:"#E86D13"
                attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#FFFFFF"
                attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#00A0DC"
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label: 'battery ${currentValue}%', unit: "%"
            }
        }

        standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label:'lock', action:"lock.lock", icon: "st.locks.lock.locked"
        }

        standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label:'unlock', action:"lock.unlock", icon: "st.locks.lock.unlocked"
        }

        valueTile("jamLabel", "device.id", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label:"Tap button to jam the lock now.\nUse main button to clear jam."
        }

        standardTile("jam", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label:'', action:"jam", nextState: "unknown", backgroundColor:"#CCCCCC", defaultState: true
            state "unknown", label:'jammed', backgroundColor:"#E86D13"
        }

        valueTile("jamToggleLabel", "device.doesNextOperationJam", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: "When button is active, lock will\njam on the next operation.", defaultState: true
        }

        standardTile("jamToggle", "device.doesNextOperationJam", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "false", label:'', action: "setJamNextOperation", backgroundColor:"#CCCCCC", defaultState: true
            state "true", label:'', action: "clearJamNextOperation", backgroundColor:"#E86D13"
        }

        valueTile("batterySliderLabel", "device.battery", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "battery", label:'battery ${currentValue}%\nUse slider to set battery level', unit:"%"
        }

        controlTile("batterySliderControl", "device.battery", "slider", width: 2, height: 1, range:"(1..100)") {
            state "battery", action:"setBatteryLevel"
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh", icon: "st.secondary.refresh"
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

        main "toggle"
        details(["toggle",
            "deviceHealthControl", "refresh", "reset",
            "jamLabel", "jam",
            "jamToggleLabel", "jamToggle",
            "batterySliderLabel", "batterySliderControl"])
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
    log.trace "installed()"
    configure()
}

def updated() {
    log.trace "updated()"
    // processPreferences()
    initialize()
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
    log.trace "initialize()"
    clearJamNextOperation()
}

private processPreferences() {
    log.debug "prefBatteryLevel: $prefBatteryLevel"
    log.debug "prefJamNextOperation: $prefJamNextOperation"
    log.debug "prefJamImmediately: $prefJamImmediately"

    String strBatteryLevel = "$prefBatteryLevel"
    Integer batteryLevel = strBatteryLevel.isInteger() ? strBatteryLevel.toInteger() : null
    if (batteryLevel) {
        setBatteryLevel(batteryLevel)
    }

    if (prefJamNextOperation) {
        setJamNextOperation()
    } else {
        clearJamNextOperation()
    }

    if (prefJamImmediately) {
        jam()
    }
}

def refresh() {
    log.trace "refresh()"
    sendEvent(name: "lock", value: device.currentValue("lock") ?: "locked")
    sendEvent(name: "battery", value: device.currentValue("battery") ?: 94)
}

def configure() {
    log.trace "configure()"
    // this would be for a physical device when it gets a handler assigned to it

    // for HealthCheck
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
    initialize()
    markDeviceOnline()
    setBatteryLevel(94)
    unlock()
}

def lock() {
    log.trace "lock()"
    if (device.currentValue("doesNextOperationJam") == "true") {
        jam()
    } else {
        sendEvent(name: "lock", value: "locked")
    }
}

def unlock() {
    log.trace "unlock()"
    if (device.currentValue("doesNextOperationJam") == "true") {
        jam()
    } else {
        sendEvent(name: "lock", value: "unlocked")
    }
}

def jam() {
    log.trace "jam()"
    sendEvent(name: "lock", value: "unknown")
    if (device.currentValue("doesNextOperationJam") == "true") {
        clearJamNextOperation()
    }
}

def setJamNextOperation() {
    log.trace "setJamNextOperation() -  next lock operation will jam"
    sendEvent(name: "doesNextOperationJam", value: "true")
}

def clearJamNextOperation() {
    log.trace "clearJamNextOperation() -  next lock operation will NOT jam"
    sendEvent(name: "doesNextOperationJam", value: "false")
}

def setBatteryLevel(Number lvl) {
    log.trace "setBatteryLevel(level)"
    sendEvent(name: "battery", value: lvl)
}
