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
 *  Author: SmartThings
 *  Date: 2016-01-19
 *
 *  This DTH should serve as the generic DTH to handle RGBW ZigBee HA devices
 */

metadata {
    definition (name: "ZigBee RGBW Bulb", namespace: "smartthings", author: "SmartThings") {

        capability "Actuator"
        capability "Color Control"
        capability "Color Temperature"
        capability "Configuration"
        capability "Polling"
        capability "Refresh"
        capability "Switch"
        capability "Switch Level"
        capability "Health Check"

        attribute "colorName", "string"
        command "setGenericName"

        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY Flex RGBW", deviceJoinName: "OSRAM LIGHTIFY LED FLEXIBLE STRIP RGBW"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Flex RGBW", deviceJoinName: "OSRAM LIGHTIFY LED FLEXIBLE STRIP RGBW"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 RGBW", deviceJoinName: "OSRAM LIGHTIFY LED A19 RGBW"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY BR RGBW", deviceJoinName: "OSRAM LIGHTIFY LED BR30 RGBW"
        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY RT RGBW", deviceJoinName: "OSRAM LIGHTIFY LED RT 5/6 RGBW"
    }

    // UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"color control.setColor"
            }
        }
        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorName", label: '${currentValue}'
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main(["switch"])
        details(["switch", "colorTempSliderControl", "colorName", "refresh"])
    }
}

//Globals
private getATTRIBUTE_HUE() { 0x0000 }
private getATTRIBUTE_SATURATION() { 0x0001 }
private getHUE_COMMAND() { 0x00 }
private getSATURATION_COMMAND() { 0x03 }
private getCOLOR_CONTROL_CLUSTER() { 0x0300 }
private getATTRIBUTE_COLOR_TEMPERATURE() { 0x0007 }

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description is $description"

    def event = zigbee.getEvent(description)
    if (event) {
        log.debug event
        // Temporary fix for the case when Device is OFFLINE and is connected again
        if (state.lastActivity == null){
            state.lastActivity = now()
            sendEvent(name: "deviceWatch-lastActivity", value: state.lastActivity, description: "Last Activity is on ${new Date((long)state.lastActivity)}", displayed: false, isStateChange: true)
        }
        state.lastActivity = now()
        if (event.name=="level" && event.value==0) {}
        else {
            if (event.name=="colorTemperature") {
                setGenericName(event.value)
            }
            sendEvent(event)
        }
    }
    else {
        def zigbeeMap = zigbee.parseDescriptionAsMap(description)
        log.trace "zigbeeMap : $zigbeeMap"

        if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER) {
            if(zigbeeMap.attrInt == ATTRIBUTE_HUE){  //Hue Attribute
                def hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 255 * 360)
                sendEvent(name: "hue", value: hueValue, descriptionText: "Color has changed")
            }
            else if(zigbeeMap.attrInt == ATTRIBUTE_SATURATION){ //Saturation Attribute
                def saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 255 * 100)
                sendEvent(name: "saturation", value: saturationValue, descriptionText: "Color has changed", displayed: false)
            }
        }
        else {
            log.info "DID NOT PARSE MESSAGE for description : $description"
        }
    }
}

def on() {
    zigbee.on()
}

def off() {
    zigbee.off()
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
    zigbee.onOffRefresh() + zigbee.readAttribute(0x0008, 0x00) + zigbee.readAttribute(0x0300, 0x00) + zigbee.readAttribute(0x0300, ATTRIBUTE_COLOR_TEMPERATURE) + zigbee.readAttribute(0x0300, ATTRIBUTE_HUE) + zigbee.readAttribute(0x0300, ATTRIBUTE_SATURATION) + zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.colorTemperatureConfig() + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE, 0x20, 1, 3600, 0x01) + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION, 0x20, 1, 3600, 0x01)
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    // Enrolls device to Device-Watch with 3 x Reporting interval 30min
    sendEvent(name: "checkInterval", value: 240, displayed: false, data: [protocol: "zigbee"])
    zigbee.onOffConfig(0, 120) + zigbee.levelConfig() + zigbee.colorTemperatureConfig() + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE, 0x20, 1, 3600, 0x01) + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION, 0x20, 1, 3600, 0x01) + zigbee.readAttribute(0x0006, 0x00) + zigbee.readAttribute(0x0008, 0x00) + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, 0x00) + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_COLOR_TEMPERATURE) + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE) + zigbee.readAttribute(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION)
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

def setLevel(value) {
    zigbee.setLevel(value)
}

def setColor(value){
    log.trace "setColor($value)"
    zigbee.on() + setHue(value.hue) + "delay 300" + setSaturation(value.saturation)
}

def setHue(value) {
    def scaledHueValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
    zigbee.command(COLOR_CONTROL_CLUSTER, HUE_COMMAND, scaledHueValue, "00", "0500")       //payload-> hue value, direction (00-> shortest distance), transition time (1/10th second) (0500 in U16 reads 5)
}

def setSaturation(value) {
    def scaledSatValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
    zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, scaledSatValue, "0500")      //payload-> sat value, transition time
}
