/**
 *  Copyright 2017 SmartThings
 *
 *  Provides a virtual dimmer switch
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
    definition (name: "Virtual Dimmer Switch", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true, mnmn: "SmartThings", vid: "generic-dimmer") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Switch Level"
    }

    preferences {}

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOn", defaultState: true
                attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#00A0DC", nextState:"turningOn"
                attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#FFFFFF", nextState:"turningOff"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "setLevel"
            }
            tileAttribute ("brightnessLabel", key: "SECONDARY_CONTROL") {
                attributeState "Brightness", label: '${name}', defaultState: true
            }
        }

        standardTile("explicitOn", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "default", label: "On", action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff"
        }
        standardTile("explicitOff", "device.switch", width: 2, height: 2, decoration: "flat") {
            state "default", label: "Off", action: "switch.off", icon: "st.Home.home30", backgroundColor: "#ffffff"
        }
        controlTile("levelSlider", "device.level", "slider", width: 2, height: 2, inactiveLabel: false, range: "(1..100)") {
            state "physicalLevel", action: "switch level.setLevel"
        }

        main(["switch"])
        details(["switch", "explicitOn", "explicitOff", "levelSlider"])

    }
}

def parse(String description) {
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

private Map buildSetLevelEvent(value) {
    def intValue = value as Integer
    def newLevel = Math.max(Math.min(intValue, 100), 0)
    Map eventMap = [name: "level", value: newLevel, unit: "%", isStateChange: true]
    return eventMap
}
def setLevel(value, duration) {
    log.trace "Executing setLevel $value (ignoring duration)"
    setLevel(value)
}

private implicitOn() {
    if (device.currentValue("switch") != "on") {
        turnOn()
    }
}

private turnOn() {
    sendEvent(name: "switch", value: "on", isStateChange: true)
}

private turnOff() {
    sendEvent(name: "switch", value: "off", isStateChange: true)
}

def installed() {
    setLevel(100)
}
