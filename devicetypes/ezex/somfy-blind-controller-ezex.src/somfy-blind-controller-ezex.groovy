/**
 *
 *	Copyright 2019 eZEX
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 */
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition(name: "SOMFY Blind Controller/eZEX", namespace: "eZEX", author: "eZEX Corp", vid:"SmartThings-smartthings-Somfy_WindowShade", ocfDeviceType: "oic.d.blind") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Window Shade"
        capability "Health Check"
        capability "Switch Level"

        command "pause"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0102", outClusters: "0019", model: "E2B0-KR000Z0-HA" // SY-IoT201-BD
    }


    tiles(scale: 2) {
        multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label: 'Open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
                attributeState "closed", label: 'Closed', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
                attributeState "partially open", label: 'Partially open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#d45614", nextState: "closing"
                attributeState "opening", label: 'Opening', action: "pause", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "partially open"
                attributeState "closing", label: 'Closing', action: "pause", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "partially open"
            }
        }
        standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "pause", label:"", icon:'st.sonos.pause-btn', action:'pause', backgroundColor:"#cccccc"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("ShadeLevel", "device.level", width: 4, height: 1) {
            state "level", label: 'Shade is ${currentValue}% up', defaultState: true
        }
        controlTile("levelSliderControl", "device.level", "slider", width:2, height: 1, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }

        main "windowShade"
        details(["windowShade", "contPause", "ShadeLevel", "levelSliderControl","refresh"])
    }
}

private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getATTRIBUTE_POSITION_LIFT() { 0x0008 }

// Parse incoming device messages to generate events
def parse(String description) {
    def parseMap = zigbee.parseDescriptionAsMap(description)
    log.info "LOG-INFO-Shade: In parse(), description:- ${description}"
    def attrs = parseMap.additionalAttrs
    log.info "LOG-INFO-Shade: In parse(), additionalAttrs: - ${attrs}"
    if ( attrs != null ) {
        attrs.each { attr ->
            if(parseMap.clusterInt == CLUSTER_WINDOW_COVERING && attr.attrInt == ATTRIBUTE_POSITION_LIFT) {
                if (attr.value == "64") { //open
                    sendEvent(name: "windowShade", value: "open")
                    sendEvent(name: "level", value: "100")
                } else if (attr.value == "00") { //closed
                    sendEvent(name: "windowShade", value: "closed")
                    sendEvent(name: "level", value: "0")
                } else {
                    log.info "Partially open state event"
                    sendEvent(name: "windowShade", value: "partially open")
                    sendEvent(name: "level", value: zigbee.convertHexToInt(attr.value))
                }
            }
        }
    }
}

def close() {
    log.info "LOG-INFO-Shade: close()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x01)
}

def open() {
    log.info "LOG-INFO-Shade: open()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x00)
}

def setLevel(data) {
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x05, zigbee.convertToHexString(data, 2))
}

def pause() {
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x02)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    def cmds =  zigbee.readAttribute(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT)
    log.info "LOG-INFO-Shade: refresh() read covering information : ${cmds}"
    return cmds
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."
    zigbee.configureReporting(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT, DataType.UINT8, 0, 600, null)
}
