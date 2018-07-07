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
        command "jam"
        command "setBatteryLevel"
        command "setJamNextOperation"
        command "clearJamNextOperation"
        attribute "doesNextOperationJam", "enum", ["true", "false"]
    }

    // Simulated lock
    tiles {
        multiAttributeTile(name:"toggle", type: "generic", width: 6, height: 4){
            tileAttribute ("device.lock", key: "PRIMARY_CONTROL") {
                attributeState "locked", label:'locked', action:"lock.unlock", icon:"st.locks.lock.locked", backgroundColor:"#00A0DC", nextState:"unlocking"
                attributeState "unlocked", label:'unlocked', action:"lock.lock", icon:"st.locks.lock.unlocked", backgroundColor:"#FFFFFF", nextState:"locking"
                attributeState "unknown", label:'jammed', action:"lock.lock", icon:"st.secondary.activity", backgroundColor:"#E86D13"
                attributeState "locking", label:'locking', icon:"st.locks.lock.locked", backgroundColor:"#00A0DC"
                attributeState "unlocking", label:'unlocking', icon:"st.locks.lock.unlocked", backgroundColor:"#FFFFFF"
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label: 'battery ${currentValue}%', unit: "%"
            }
        }

        standardTile("lock", "device.lock", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
            state "default", label:'lock', action:"lock.lock", icon: "st.locks.lock.locked"
        }
        standardTile("unlock", "device.lock", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
            state "default", label:'unlock', action:"lock.unlock", icon: "st.locks.lock.unlocked"
        }
        valueTile("jamLabel", "device.id", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label:"Tap button to simulate a jam now.\nUse Lock or Unlock to clear jam."
        }
        standardTile("jam", "device.lock", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label:'', action:"jam", nextState: "unknown", backgroundColor:"#CCCCCC", defaultState: true
            state "unknown", label:'jammed', backgroundColor:"#E86D13"   
        }
        valueTile("jamToggleLabel", "device.doesNextOperationJam", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: "When button is active, lock will\nsimulate a jam on the next operation.", defaultState: true
        }
        standardTile("jamToggle", "device.doesNextOperationJam", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "false", label:'', action: "setJamNextOperation", backgroundColor:"#CCCCCC", defaultState: true
            state "true", label:'', action: "clearJamNextOperation", backgroundColor:"#E86D13"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "battery", label:'battery ${currentValue}%', unit:"%"
        }
        controlTile("batterySliderControl", "device.battery", "slider",
                    height: 1, width: 4, range:"(1..100)") {
            state "battery", action:"setBatteryLevel"
        }

        main "toggle"
        details(["toggle",
            "lock", "unlock", 
            "jamLabel", "jam",
            "jamToggleLabel", "jamToggle",
            "battery", "batterySliderControl" ])
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
    setBatteryLevel(94)
    unlock()
    initialize()
}

def updated() {
    log.trace "updated()"
    // processPreferences()
    initialize()
}

def initialize() {
    log.trace "initialize()"
    sendEvent(name: "checkInterval", value: 12 * 60, displayed: false, data: [protocol: "cloud", scheme: "untracked"])
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
    sendEvent(name: "lock", value: device.currentValue("lock"))
    sendEvent(name: "battery", value: device.currentValue("battery"))
}

def ping() {
    refresh()
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

