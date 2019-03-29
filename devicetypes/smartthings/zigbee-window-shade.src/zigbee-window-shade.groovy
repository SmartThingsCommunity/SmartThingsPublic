/**
 *
 *	Copyright 2019 SmartThings
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
    definition(name: "ZigBee Window Shade", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-shade") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Window Shade"
        capability "Health Check"
        capability "Switch Level"

        command "pause"

        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0102", outClusters: "0019", model: "E2B0-KR000Z0-HA", deviceJoinName: "SOMFY Blind Controller/eZEX" // SY-IoT201-BD
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
        valueTile("shadeLevel", "device.level", width: 4, height: 1) {
            state "level", label: 'Shade is ${currentValue}% up', defaultState: true
        }
        controlTile("levelSliderControl", "device.level", "slider", width:2, height: 1, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }

        main "windowShade"
        details(["windowShade", "contPause", "shadeLevel", "levelSliderControl", "refresh"])
    }
}

private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getATTRIBUTE_POSITION_LIFT() { 0x0008 }

private List<Map> collectAttributes(Map descMap) {
	List<Map> descMaps = new ArrayList<Map>()

	descMaps.add(descMap)

	if (descMap.additionalAttrs) {
		descMaps.addAll(descMap.additionalAttrs)
	}

	return  descMaps
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description:- ${description}"
    if (description?.startsWith("read attr -")) {
        Map descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap?.clusterInt == CLUSTER_WINDOW_COVERING && descMap.value) {
            log.debug "attr: ${descMap?.attrInt}, value: ${descMap?.value}, decValue: ${Integer.parseInt(descMap.value, 16)}, ${device.getDataValue("model")}"
            List<Map> descMaps = collectAttributes(descMap)
            def liftmap = descMaps.find { it.attrInt == ATTRIBUTE_POSITION_LIFT }
            if (liftmap) {
                 if (liftmap.value == "64") { //open
                    sendEvent(name: "windowShade", value: "open")
                    sendEvent(name: "level", value: "100")
                } else if (liftmap.value == "00") { //closed
                    sendEvent(name: "windowShade", value: "closed")
                    sendEvent(name: "level", value: "0")
                } else {
                    sendEvent(name: "windowShade", value: "partially open")
                    sendEvent(name: "level", value: zigbee.convertHexToInt(attr.value))
                }
            }
        }
    }
}

def close() {
    log.info "close()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x01)
}

def open() {
    log.info "open()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x00)
}

def setLevel(data) {
    log.info "setLevel()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x05, zigbee.convertToHexString(data, 2))
}

def pause() {
    log.info "pause()"
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x02)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def refresh() {
    log.info "refresh()"
    def cmds = zigbee.readAttribute(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT)
    return cmds
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    log.info "configure()"
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."
    zigbee.configureReporting(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT, DataType.UINT8, 0, 600, null)
}