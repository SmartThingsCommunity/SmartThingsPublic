/**
 *  Copyright 2015 SmartThings
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
 *  ZigBee White Color Temperature Bulb
 *
 *  Author: SmartThings
 *  Date: 2015-09-22
 */

metadata {
    definition (name: "ZigBee White Color Temperature Bulb", namespace: "smartthings", author: "SmartThings") {

        capability "Actuator"
        capability "Color Temperature"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"

        attribute "colorName", "string"
        command "setGenericName"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04", outClusters: "0019"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY BR Tunable White", deviceJoinName: "OSRAM LIGHTIFY LED Flood BR30 Tunable White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY RT Tunable White", deviceJoinName: "OSRAM LIGHTIFY LED Recessed Kit RT 5/6 Tunable White"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic A60 TW", deviceJoinName: "OSRAM LIGHTIFY LED Tunable White 60W"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 Tunable White", deviceJoinName: "OSRAM LIGHTIFY LED Tunable White 60W"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("colorName", key: "SECONDARY_CONTROL") {
                attributeState "colorName", label:'${currentValue}'
            }
        }

        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorTemperature", label: '${currentValue} K'
        }

        main(["switch"])
        details(["switch", "colorTempSliderControl", "colorTemp", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"

    def finalResult = zigbee.getKnownDescription(description)
    if (finalResult) {
        log.info finalResult
        if (finalResult.type == "update") {
            log.info "$device updates: ${finalResult.value}"
        }
        else {
            sendEvent(name: finalResult.type, value: finalResult.value)
        }
    }
    else {
        log.warn "DID NOT PARSE MESSAGE for description : $description"
        log.debug zigbee.parseDescriptionAsMap(description)
    }
}

def off() {
    zigbee.off()
}

def on() {
    zigbee.on()
}

def setLevel(value) {
    zigbee.setLevel(value)
}

def refresh() {
    zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh() + zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.colorTemperatureConfig()
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.colorTemperatureConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh()
}

def setColorTemperature(value) {
    setGenericName(value)
    zigbee.setColorTemperature(value)
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value){
    if (value != null) {
        def genericName = "White"
        if (value < 3300) {
            genericName = "Soft White"
        } else if (value < 4150) {
            genericName = "Moonlight"
        } else if (value <= 5000) {
            genericName = "Cool White"
        } else if (value >= 5000) {
            genericName = "Daylight"
        }
        sendEvent(name: "colorName", value: genericName)
    }
}
