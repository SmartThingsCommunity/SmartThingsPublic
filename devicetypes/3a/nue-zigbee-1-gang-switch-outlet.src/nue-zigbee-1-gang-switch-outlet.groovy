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
*  Update: 07/08/2019
*/
 
metadata {
    definition (name: "Nue ZigBee 1 Gang Switch Outlet", namespace: "3A", author: "Kevin X.", ocfDeviceType: "oic.d.switch") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Health Check"
        
 
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Feibit Inc co.", model: "FNB56-SKT1EHG1.2", deviceJoinName: "Nue ZigBee 1 Gang Switch"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Feibit Inc co.", model: "FB56+ZSW05HG1.2", deviceJoinName: "Nue ZigBee Light Controller"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "3A SMart Home DE", model: "LXN56-LC27LX1.1", deviceJoinName: "3A Nue ZigBee Inline Switch"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "FeiBit", model: "FNB56-SKT1JXN1.0", deviceJoinName: "3A outlet"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "FeiBit", model: "FNB56-ZSW01LX2.0", deviceJoinName: "3A Nue ZigBee Switch"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "3A Smart Home DE", model: "LXN56-0S27LX1.1", deviceJoinName: "3A Nue ZigBee Outlet"
        fingerprint profileId: "C05E", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "3A Smart Home DE", model: "LXN-1S27LX1.0", deviceJoinName: "3A Nue ZigBee Switch"
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
 
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
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
        sendEvent(event)
    }
    else {
        log.warn "DID NOT PARSE MESSAGE for description : $description"
        log.debug zigbee.parseDescriptionAsMap(description)
    }
}
 
def off() {
 //   device.endpointId ="0B"
    zigbee.off()
}
 
def on() {
 //   device.endpointId ="0B"
    zigbee.on()
}
 
/**
* PING is used by Device-Watch in attempt to reach the Device
* */
def ping() {
    return refresh()
}
 
def refresh() {
    zigbee.onOffRefresh() + zigbee.onOffConfig()
}
 
def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."
    zigbee.onOffRefresh() + zigbee.onOffConfig()
}