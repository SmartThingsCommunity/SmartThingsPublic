/** Please copy the Device Handler from this line

Copyright 2016 SmartThings
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at:
 http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
for the specific language governing permissions and limitations under the License.
Updated by Kevin X- 3A Smart Home on 1st Jun 2018
*/

metadata {
definition (name: "3A NUE ZigBee RGBW Light", namespace: "smartthings", author: "Kevin X- 3A Smart Home") {

    capability "Actuator"
    capability "Color Control"
    capability "Color Temperature"
    capability "Configuration"
    capability "Polling"
    capability "Refresh"
    capability "Switch"
    capability "Switch Level"

    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0019"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019"
    fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 1000", outClusters: "0019", "manufacturer":"3A Feibit", "model":"RGBW Light", deviceJoinName: "3A-Feibit RGBW Light"
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
    valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        state "colorTemperature", label: '${currentValue} K'
    }
    standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }

    main(["switch"])
    details(["switch", "colorTempSliderControl", "colorTemp", "refresh"])
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

def finalResult = zigbee.getEvent(description)
if (finalResult) {
    log.debug finalResult
    sendEvent(finalResult)
}
else {
    def zigbeeMap = zigbee.parseDescriptionAsMap(description)
    log.trace "zigbeeMap : $zigbeeMap"

    if (zigbeeMap?.clusterInt == COLOR_CONTROL_CLUSTER) {
        if(zigbeeMap.attrInt == ATTRIBUTE_HUE){  //Hue Attribute
            def hueValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 255 * 360)
            sendEvent(name: "hue", value: hueValue, displayed:false)
        }
        else if(zigbeeMap.attrInt == ATTRIBUTE_SATURATION){ //Saturation Attribute
            def saturationValue = Math.round(zigbee.convertHexToInt(zigbeeMap.value) / 255 * 100)
            sendEvent(name: "saturation", value: saturationValue, displayed:false)
        }
    }
    else {
        log.info "DID NOT PARSE MESSAGE for description : $description"
    }
}
}

def on() {
device.endpointId ="0B"
zigbee.on()
}

def off() {
device.endpointId ="0B"
zigbee.off() //+ ["delay 20"] + zigbee.onOffRefresh()
}

def refresh() {
refreshAttributes() + configureAttributes()
}

def poll() {
refreshAttributes()
}

def configure() {
log.debug "Configuring Reporting and Bindings."
configureAttributes() + refreshAttributes()
setColor(5000)
}

def configureAttributes() {
zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.colorTemperatureConfig() + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_HUE, 0x20, 1, 3600, 0x01) + zigbee.configureReporting(COLOR_CONTROL_CLUSTER, ATTRIBUTE_SATURATION, 0x20, 1, 3600, 0x01)
}

def refreshAttributes() {
zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.colorTemperatureRefresh() + zigbee.readAttribute(0x0300, 0x00) + zigbee.readAttribute(0x0300, ATTRIBUTE_HUE) + zigbee.readAttribute(0x0300, ATTRIBUTE_SATURATION)
}

def setColorTemperature(value) {
device.endpointId ="0B"
zigbee.setColorTemperature(value) + ["delay 10"] + zigbee.colorTemperatureRefresh()
}

def setLevel(value) {
device.endpointId = "0B"
def additionalCmds = []
additionalCmds = refresh()
def hexConvertedValue = zigbee.convertToHexString((value/100) * 255)
zigbee.command(0x0008, 0x00, hexConvertedValue, "0000") + additionalCmds
}
/*def setLevel(value) {
device.endpointId ="0B"
zigbee.setLevel(value) + ["delay 100"] + zigbee.levelRefresh()
}
*/
def setColor(value){
log.trace "setColor($value)"
device.endpointId ="0B"
zigbee.on() + setHue(value.hue) + ["delay 100"] + setSaturation(value.saturation) + ["delay 100"]+ refreshAttributes()
}

def setHue(value) {
def scaledHueValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
device.endpointId ="0B"
zigbee.command(COLOR_CONTROL_CLUSTER, HUE_COMMAND, scaledHueValue, "00", "0100")
}

def setSaturation(value) {
def scaledSatValue = zigbee.convertToHexString(Math.round(value * 0xfe / 100.0), 2)
device.endpointId ="0B"
zigbee.command(COLOR_CONTROL_CLUSTER, SATURATION_COMMAND, scaledSatValue, "0100")
}

// Please copy the device handler end of this line