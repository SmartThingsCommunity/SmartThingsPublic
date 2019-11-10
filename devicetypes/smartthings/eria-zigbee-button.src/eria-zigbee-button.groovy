/**
 *  ZigBee Button
 *
 *  Copyright 2015 Mitch Pond
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

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition (name: "Eria ZigBee Button", namespace: "smartthings", author: "ADUROSMART", mcdSync: true) {
        capability "Actuator"
        capability "Button"
        capability "Configuration"

    	//AduroSmart
        fingerprint inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", outClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", manufacturer: "AduroSmart Eria", model: "ADUROLIGHT_CSC", deviceJoinName: "Eria scene button switch V2.1"
        fingerprint inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", outClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, FCCC, 1000", manufacturer: "ADUROLIGHT", model: "ADUROLIGHT_CSC", deviceJoinName: "Eria scene button switch V2.0"
        fingerprint inClusters: "0000, 0003, 0008, FCCC, 1000", outClusters: "0003, 0004, 0006, 0008, FCCC, 1000", manufacturer: "AduroSmart Eria", model: "Adurolight_NCC", deviceJoinName: "Eria dimming button switch V2.1"
        fingerprint inClusters: "0000, 0003, 0008, FCCC, 1000", outClusters: "0003, 0004, 0006, 0008, FCCC, 1000", manufacturer: "ADUROLIGHT", model: "Adurolight_NCC", deviceJoinName: "Eria dimming button switch V2.0"
    }

    tiles {
        standardTile("button", "device.button", width: 2, height: 1) {
            state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
        }
        main (["button"])
        details(["button"])
    }
}

def parse(String description) {
    log.debug "description is $description"
    def event = zigbee.getEvent(description)
    log.debug "event is $event"
    if ((device.getDataValue("model") == "Adurolight_NCC") || (device.getDataValue("model") == "ADUROLIGHT_CSC")) {
        def descMap = zigbee.parseDescriptionAsMap(description)
        //log.debug "descMap is ${descMap}"
        event = parseAduroSmartButtonMessage(descMap)
        log.debug "Parse returned $event"
        return event
    }
}

private Map parseAduroSmartButtonMessage(Map descMap){
    def buttonState = ""
    def buttonNumber = 0
    if (descMap.clusterInt == 0x0006) {
    	log.debug "descMap 0x0006 is $descMap"
        buttonState = "pushed"
        if (descMap.command == "01") {
            buttonNumber = 1
        }
        else if (descMap.command == "00") {
            buttonNumber = 4
        }
        if (buttonNumber !=0) {
            def descriptionText = "$device.displayName button $buttonNumber was $buttonState"
            return createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
        }
        else {
            return [:]
        }
    }
    else if (descMap.clusterInt == 0x0008) {
        if (descMap.command == "02") {
        	buttonState = "pushed"
            def data = descMap.data
            log.debug "descMap 0x0008 is $descMap"
            def d0 = data[0]
            //log.debug "d0 is $d0"
            if (d0 == "00") {
            	buttonNumber = 2
            }
            if (d0 == "01") {
                buttonNumber = 3
            }
            //state.buttonNumber = buttonNumber
            //getButtonResult("press", buttonNumber)
            state.buttonNumber = buttonNumber
        	state.pressTime = now()
        	return createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
        }
    }else if (descMap.clusterInt == 0xFCCC) {
        buttonState = "pushed"
        log.debug "descMap 0xFCCC is $descMap"
        //log.debug "buttonState is $buttonState"
        //log.debug "descMap.data is $descMap.data"
        def list2 = descMap.data
        //log.debug "list2 is $list2"
        def keynumber = list2[1]
        //log.debug "keynumber is $keynumber"
        if (keynumber == "00") {
            buttonNumber = 1
        }
        if (keynumber == "01") {
            buttonNumber = 2
        }
        if (keynumber == "02") {
            buttonNumber = 3
        }
        if (keynumber == "03") {
            buttonNumber = 4
        }
        def descriptionText = "$device.displayName button $buttonNumber was $buttonState"
        //log.debug "descriptionText is $descriptionText"
        state.buttonNumber = buttonNumber
        state.pressTime = now()
        return createEvent(name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true)
    }
    
}


def refresh() {
    log.debug "Refreshing Battery"

    return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20) +
            zigbee.enrollResponse()
}

def configure() {
    log.debug "Configuring Reporting, IAS CIE, and Bindings."
    def cmds = []
    return zigbee.onOffConfig() +
            zigbee.levelConfig() +
            zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20, DataType.UINT8, 30, 21600, 0x01) +
            zigbee.enrollResponse() +
            zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x20) +
            cmds

}



def installed() {
	sendEvent(name: "button", value: "pushed", isStateChange: true, displayed: false)
    sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJSON(), displayed: false)

    initialize()

    // Initialize default states
    device.currentValue("numberOfButtons")?.times {
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: it+1], displayed: false)
    }
}

def updated() {
    initialize()
}

def initialize() {
    // Arrival sensors only goes OFFLINE when Hub is off
    sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
    sendEvent(name: "numberOfButtons", value: 4, displayed: false)
}

private getButtonMap() {[
        "ADUROLIGHT_CSC" : [
                "01" : 1,
                "02" : 2,
                "03" : 3,
                "04" : 4
        ],
        "Adurolight_NCC" : [
                "01" : 1,
                "02" : 2,
                "03" : 3,
                "04" : 4
        ]
]}
private getSupportedButtonValues() {
    def values = ["pushed"]
    return values
}
private getModelNumberOfButtons() {[
        "ADUROLIGHT_CSC" : 4,
        "Adurolight_NCC" : 4
]}
private getBatteryVoltage() { 0x0020 }
private getSwitchType() { 0x0000 }
private getHoldTime() { 1000 }