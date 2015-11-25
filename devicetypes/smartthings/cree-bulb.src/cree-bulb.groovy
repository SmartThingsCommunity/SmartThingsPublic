/**
 *  Cree Bulb
 *
 *  Copyright 2014 SmartThings
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
	definition (name: "Cree Bulb", namespace: "smartthings", author: "SmartThings") {

		capability "Actuator"
        capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"

		fingerprint profileId: "C05E", inClusters: "0000,0003,0004,0005,0006,0008,1000", outClusters: "0000,0019"
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

    def resultMap = zigbee.getKnownDescription(description)
    if (resultMap) {
        log.info resultMap
        if (resultMap.type == "update") {
            log.info "$device updates: ${resultMap.value}"
        }
        else {
            sendEvent(name: resultMap.type, value: resultMap.value)
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
    zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.onOffConfig() + zigbee.levelConfig()
}

def configure() {
    log.debug "Configuring Reporting and Bindings."
    zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.onOffRefresh() + zigbee.levelRefresh()
}
