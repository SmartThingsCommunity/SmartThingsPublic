/**
 *  Copyright 2017 SmartThings
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
    definition (name: "Ikea ZigBee Wireless Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light") {
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"

        fingerprint profileId: "0104", deviceId: "0810", deviceVersion: "02", inClusters: "0000, 0001, 0003, 0009, 0B05, 1000", outClusters: "0003, 0004, 0006, 0008, 0019, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI wireless dimmer"
}

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                /*
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                */
            }
            /*
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            */
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    

    def event = zigbee.getEvent(description)
    if (event) {
        log.debug "Dimmer - event: "+event
        if (event.name=="level" && event.value==0) {}
        else {
            sendEvent(event)
        }
    } else {
        def descMap = zigbee.parseDescriptionAsMap(description)
        log.debug "Dimmer - description: "+descMap
        if (descMap && descMap.clusterInt == 0x0008) {
            switch (descMap.commandInt) {
                case 0x01:
                    log.debug "Dimmer - command: move, direction: "+descMap.direction+", data: "+descMap.data
                    break;
                case 0x04:
                    log.debug "Dimmer - command: move to level (with on/off), direction: "+descMap.direction+", data: "+descMap.data
                    break;
                case 0x05:
                    log.debug "Dimmer - command: move (with on/off), direction: "+descMap.direction+", data: "+descMap.data
                    break;
                case 0x07:
                    log.debug "Dimmer - command: stop"
                    break;
                case 0x0B:
                    log.debug "Dimmer - command: direction change?, direction: "+descMap.direction+", data: "+descMap.data
                    break;
                default:
                    log.debug "Dimmer - level cluster, command: "+descMap.commandInt
                    break;
            }
        } else {
            log.warn "DID NOT PARSE MESSAGE for description : "+descMap
        }
    }
}

def off() {
    log.debug "Dimmer - off"
    zigbee.off()
}

def on() {
    log.debug "Dimmer - on"
    zigbee.on()
}

def setLevel(value) {
    def additionalCmds = []
    
    log.debug "Dimmer - set level: "+value
    zigbee.setLevel(value) + additionalCmds
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    //return zigbee.onOffRefresh()
}

def refresh() {
    //zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def installed() {
    if ((device.currentState("level")?.value == null) || (device.currentState("level")?.value == 0)) {
        sendEvent(name: "level", value: 100)
    }
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    //sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    // OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
    //refresh() + zigbee.onOffConfig(0, 300) + zigbee.levelConfig()
}