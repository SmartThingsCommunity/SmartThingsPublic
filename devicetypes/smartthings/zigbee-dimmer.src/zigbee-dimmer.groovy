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
 */

metadata {
    definition (name: "ZigBee Dimmer", namespace: "smartthings", author: "SmartThings") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"


        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 ON/OFF/DIM", deviceJoinName: "OSRAM LIGHTIFY LED Smart Connected Light"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FF00", outClusters: "0019", manufacturer: "MRVL", model: "MZ100", deviceJoinName: "Wemo Bulb"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05", outClusters: "0019", manufacturer: "OSRAM SYLVANIA", model: "iQBR30", deviceJoinName: "Sylvania Ultra iQ"
    }

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
    log.debug "description is $description"

    def event = zigbee.getEvent(description)
    if (event) {
        // Temporary fix for the case when Device is OFFLINE and is connected again
        if (state.lastActivity == null){
            state.lastActivity = now()
            sendEvent(name: "deviceWatch-lastActivity", value: state.lastActivity, description: "Last Activity is on ${new Date((long)state.lastActivity)}", displayed: false, isStateChange: true)
        }
        state.lastActivity = now()
        if (event.name=="level" && event.value==0) {}
        else {
            sendEvent(event)
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
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {

    if (state.lastActivity < (now() - (1000 * device.currentValue("checkInterval"))) ){
        log.info "ping, alive=no, lastActivity=${state.lastActivity}"
        state.lastActivity = null
        return zigbee.onOffRefresh()
    } else {
        log.info "ping, alive=yes, lastActivity=${state.lastActivity}"
        sendEvent(name: "deviceWatch-lastActivity", value: state.lastActivity, description: "Last Activity is on ${new Date((long)state.lastActivity)}", displayed: false, isStateChange: true)
    }
}

def refresh() {
    zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.onOffConfig() + zigbee.levelConfig()
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    // Enrolls device to Device-Watch with 3 x Reporting interval 30min
    sendEvent(name: "checkInterval", value: 240, displayed: false, data: [protocol: "zigbee"])
    zigbee.onOffConfig(0, 120) + zigbee.levelConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh()
}
