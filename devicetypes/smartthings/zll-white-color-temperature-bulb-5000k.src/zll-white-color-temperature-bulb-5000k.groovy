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
import groovy.transform.Field

@Field Boolean hasConfiguredHealthCheck = false

metadata {
    definition (name: "ZLL White Color Temperature Bulb 5000K", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.021.00001', executeCommandsLocally: true) {

        capability "Actuator"
        capability "Color Temperature"
        capability "Configuration"
        capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"

        attribute "colorName", "string"

        fingerprint profileId: "C05E", deviceId: "0220", inClusters: "0000, 0004, 0003, 0006, 0008, 0005, 0300", outClusters: "0019", manufacturer: "Eaton", model: "Halo_RL5601", deviceJoinName: "Halo RL56"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019", manufacturer: "innr", model: "RS 128 T", deviceJoinName: "innr Smart Spot (Tunable White)"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019", manufacturer: "innr", model: "RB 178 T", deviceJoinName: "innr Smart Bulb (Tunable White)"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
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

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..5000)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorTemperature", label: '${currentValue} K'
        }

        main(["switch"])
        details(["switch", "colorTempSliderControl", "colorTemp", "refresh"])
    }
}

// Globals
private getMOVE_TO_COLOR_TEMPERATURE_COMMAND() { 0x0A }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }
private getATTRIBUTE_COLOR_TEMPERATURE() { 0x0007 }

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"
    def event = zigbee.getEvent(description)
    if (event) {
        if (event.name == "colorTemperature") {
            event.unit = "K"
            setGenericName(event.value)
        }
        sendEvent(event)
    }
    else {
        log.warn "DID NOT PARSE MESSAGE for description : $description"
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
    zigbee.setLevel(value) + zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def refresh() {
    def cmds = zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh()

    // Do NOT config if the device is the Eaton Halo_LT01, it responds with "switch:off" to onOffConfig, and maybe other weird things with the others
    if (!((device.getDataValue("manufacturer") == "Eaton") && (device.getDataValue("model") == "Halo_LT01"))) {
        cmds += zigbee.onOffConfig() + zigbee.levelConfig()
    }

    cmds
}

def poll() {
    zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return zigbee.levelRefresh()
}

def healthPoll() {
    log.debug "healthPoll()"
    def cmds = zigbee.onOffRefresh() + zigbee.levelRefresh()
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
    // Implementation note: for the Eaton Halo_LT01, it responds with "switch:off" to onOffConfig, so be sure this is before the call to onOffRefresh
    zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh()
}

def updated() {
    log.debug "updated()"
    configureHealthCheck()
}

def setColorTemperature(value) {
    value = value as Integer
    def tempInMired = Math.round(1000000 / value)
    def finalHex = zigbee.swapEndianHex(zigbee.convertToHexString(tempInMired, 4))

    zigbee.command(COLOR_CONTROL_CLUSTER, MOVE_TO_COLOR_TEMPERATURE_COMMAND, "$finalHex 0000") +
    zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE)
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value){
    if (value != null) {
        def genericName = ""
        if (value < 3300) {
            genericName = "Soft White"
        } else if (value < 4150) {
            genericName = "Moonlight"
        } else if (value <= 5000) {
            genericName = "Cool White"
        } else {
            genericName = "Daylight"
        }
        sendEvent(name: "colorName", value: genericName, displayed: false)
    }
}
