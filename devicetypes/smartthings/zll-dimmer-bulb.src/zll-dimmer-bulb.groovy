/**
 *  Copyright 2016 SmartThings
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
import groovy.transform.Field

@Field Boolean hasConfiguredHealthCheck = false

metadata {
    definition (name: "ZLL Dimmer Bulb", namespace: "smartthings", author: "SmartThings") {

        capability "Actuator"
        capability "Configuration"
        capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"

        //fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0000,0019"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019"
        //fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0000,0019", manufacturer: "CREE", model: "Connected A-19 60W Equivalent", deviceJoinName: "Cree Connected Bulb"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic A60 W clear", deviceJoinName: "OSRAM LIGHTIFY LED Smart Connected Light"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Classic A60 W clear - LIGHTIFY", deviceJoinName: "OSRAM LIGHTIFY LED Smart Connected Light"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "Philips", model: "LWB006", deviceJoinName: "Philips Hue White"
    }

    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
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
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "switch"
        details(["switch", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"

    def resultMap = zigbee.getEvent(description)
    if (resultMap) {
        sendEvent(resultMap)
    }
    else {
        log.debug "DID NOT PARSE MESSAGE for description : $description"
        log.debug zigbee.parseDescriptionAsMap(description)
    }
}

def off() {
    zigbee.off() + ["delay 1500"] + zigbee.onOffRefresh()
}

def on() {
    zigbee.on() + ["delay 1500"] + zigbee.onOffRefresh()
}

def setLevel(value) {
    zigbee.setLevel(value) + zigbee.onOffRefresh() + zigbee.levelRefresh()         //adding refresh because of ZLL bulb not conforming to send-me-a-report
}

def refresh() {
    zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def poll() {
    refresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return zigbee.levelRefresh()
}

def healthPoll() {
    log.debug "healthPoll()"
    def cmds = refresh()
    cmds.each{ sendHubCommand(new physicalgraph.device.HubAction(it))}
}

def configureHealthCheck() {
    Integer hcIntervalMinutes = 12
    if (!hasConfiguredHealthCheck) {
        log.debug "Configuring Health Check, Reporting"
        unschedule("healthPoll")
        runEvery5Minutes("healthPoll")
        // Device-Watch allows 2 check-in misses from device
        sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
        hasConfiguredHealthCheck = true
    }
}

def configure() {
    log.debug "configure()"
    configureHealthCheck()
    zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def updated() {
    log.debug "updated()"
    configureHealthCheck()
}
