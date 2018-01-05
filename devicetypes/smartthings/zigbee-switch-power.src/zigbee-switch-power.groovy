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
    definition (name: "ZigBee Switch Power", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true) {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Power Meter"
        capability "Sensor"
        capability "Switch"
        capability "Health Check"
        capability "Light"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B04"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B05", outClusters: "0003, 000A, 0019", manufacturer: "Jasco Products", model: "45853", deviceJoinName: "GE ZigBee Plug-In Switch"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B05", outClusters: "000A, 0019", manufacturer: "Jasco Products", model: "45856", deviceJoinName: "GE ZigBee In-Wall Switch"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("power", key: "SECONDARY_CONTROL") {
                attributeState "power", label:'${currentValue} W'
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
        if (event.name == "power") {
            def powerValue
            powerValue = (event.value as Integer)/10            //TODO: The divisor value needs to be set as part of configuration
            sendEvent(name: "power", value: powerValue)
        }
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

def refresh() {
    Integer reportIntervalMinutes = 5
    zigbee.onOffRefresh() + zigbee.simpleMeteringPowerRefresh() + zigbee.electricMeasurementPowerRefresh() + zigbee.onOffConfig(0,reportIntervalMinutes * 60) + zigbee.simpleMeteringPowerConfig() + zigbee.electricMeasurementPowerConfig()
}

def configure() {
    log.debug "in configure()"
    return configureHealthCheck()
}

def configureHealthCheck() {
    Integer hcIntervalMinutes = 12
    sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    return refresh()
}

def updated() {
    log.debug "in updated()"
    // updated() doesn't have it's return value processed as hub commands, so we have to send them explicitly
    def cmds = configureHealthCheck()
    cmds.each{ sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def ping() {
    return zigbee.onOffRefresh()
}
