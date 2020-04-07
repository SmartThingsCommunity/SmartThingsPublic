/**
 *  Rachio IRO2 Zone Device Handler
 *
 *  Copyright\u00A9 2017, 2018 Franz Garsombke
 *  Written by Anthony Santilli (@tonesto7)
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
metadata {
    definition (name: "Rachio Zone", namespace: "rachio", author: "Rachio") {
        capability "Refresh"
        capability "Switch"
        capability "Actuator"
        capability "Valve"
        capability "Sensor"
        capability "Health Check"
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles (scale: 2){
        multiAttributeTile(name: "valveTile", type: "generic", width: 6, height: 4) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL" ) {
                attributeState "off", label: 'Off', action: "open", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState:"on"
                attributeState "on", label: 'Watering', action: "close", icon: "st.valves.water.open", backgroundColor: "#00a0dc", nextState: "off"
            }
        }
    }
    main "valveTile"
    details(["valveTile"])
}

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
}

def initialize() {
    sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
}

void installed() {
    state.isInstalled = true
    initialize()
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
}

void updated() {
    initialize()
}

// NOP implementation of ping as health check only calls this for tracked devices
// But as capability defines this method it's implemented to avoid MissingMethodException
def ping() {
    log.info "unexpected ping call from health check"
}

def generateEvent(Map results) {
    if (results) {
        if (!results.data?.enabled) {
            sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
            return
        }
        // log.debug results
        if (results.status == "ONLINE") {
            sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
        } else {
            markOffLine()
        }
    }
}

def refresh() {
    parent?.poll(this)
}

def on() {
    log.trace "zone on..."
    if (isCmdOk2Run()) {
        if (device.currentValue("switch") == "off") {
            open()
        } else {
            log.debug "Zone is Already ON... Ignoring.."
        }
    }
}

def off() {
    log.trace "zone off..."
    if (device.currentValue("switch") == "on") {
        close()
    } else {
        log.debug "Zone is Already OFF... Ignoring..."
    }
}

def open() {
    log.trace "Zone open()..."
    if (isCmdOk2Run()) {
        if (device.currentValue("valve") == "closed") {
            startZone()
        } else {
            log.debug "Valve is Already Open... Ignoring..."
        }
    }
}

def close() {
    log.trace "Zone close()..."
    if (device.currentValue("valve") == "open") {
        if (parent?.off(this, state.deviceId)) {
            log.info "Zone was Stopped Successfully..."
            sendEvent(name:'switch', value: "off", displayed: false)
            sendEvent(name:'valve', value: "closed", displayed: false)
        }
    } else {
        log.debug "Valve is Already Closed... Ignoring..."
    }
}

def markOffLine() {
    log.trace "Watering (Offline)"
    sendEvent(name: 'valve', value: "closed", displayed: false)
    sendEvent(name: 'switch', value: "off", displayed: false)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
}

def startZone() {
    log.trace "startZone()..."
    if (isCmdOk2Run()) {
        def zoneNum = device.latestValue('zoneNumber')
        def waterTime = 10;
        log.debug("Starting Watering for Zone (${zoneNum}) for (${waterTime}) Minutes")
        if (parent?.startZone(this, state.deviceId, zoneNum, waterTime)) {
            log.debug "runThisZone was Sent Successfully"
            sendEvent(name:'switch', value: "on", displayed: false)
            sendEvent(name:'valve', value: "open", displayed: false)
        } else {
            markOffLine()
        }
    }
}

// To be used directly by smart apps
def stopWatering() {
    log.trace "stopWatering"
    close()
}

def isCmdOk2Run() {
    //log.trace "isCmdOk2Run..."
    if (device.currentValue("DeviceWatch-DeviceStatus") == "offline") {
        log.warn "Skipping the request... Because the zone is unable to send commands while it's in an Offline State."
        return false
    }
    return true
}
