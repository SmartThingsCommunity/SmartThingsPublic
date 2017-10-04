/**
 *  Copyright 2017 SmartThings
 *
 *  Simulates a dimmable light bulb. No color, no tunable white
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
    definition (name: "Simulated Dimmable Bulb", namespace: "smartthings/testing", author: "SmartThings") {
        capability "Health Check"
        capability "Actuator"
        capability "Sensor"
        capability "Light"

        capability "Switch"
        capability "Switch Level"
        capability "Refresh"
        capability "Configuration"
    }

    preferences {
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#FFFFFF", nextState:"turningOn"
                attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"on"
                attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#FFFFFF", nextState:"off"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "setLevel"
            }
            tileAttribute ("brightnessLabel", key: "SECONDARY_CONTROL") {
                attributeState "Brightness", label: '${name}', defaultState: true
            }
        }

        standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, decoration: "flat") {
            state "default", label: "",  action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("reset", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "default", label: "Reset", action: "configure"
        }

        main(["switch"])
        details(["switch", "refresh", "reset"])

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
    initialize()
}

def updated() {
    log.trace "Executing 'updated'"
    initialize()
}

//
// command methods
//
def ping() {
    refresh()
}

def refresh() {
    log.trace "Executing 'refresh'"
    sendEvent(name: "switch", value: getSwitch())
    sendEvent(buildSetLevelEvent(getLevel()))
}

def configure() {
    log.trace "Executing 'configure'"
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

private String getSwitch() {
    def switchState = device.currentState("switch")
    return switchState ? switchState.getStringValue() : "off"
}

private Integer getLevel() {
    def levelState = device.currentState("level")
    return levelState ? levelState.getIntegerValue() : 100
}

private initialize() {
    log.trace "Executing 'initialize'"
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "level", value: 100)
}

private Map buildSetLevelEvent(value) {
    Integer intValue = value as Integer
    Integer newLevel = Math.max(Math.min(intValue, 99), 0)
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
