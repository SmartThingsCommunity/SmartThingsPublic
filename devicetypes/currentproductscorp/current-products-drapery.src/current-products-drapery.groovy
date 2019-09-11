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
    definition(name: "Current Products Drapery", namespace: "CurrentProductsCorp", author: "Current Products", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-shade") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Window Shade"
        capability "Health Check"
        capability "Switch Level"

        command "pause"
        command "reverse"
        command "smartAssistOn"
        command "smartAssistOff"

        fingerprint profileId: "0x0104", inClusters: "0x0000,0x0001,0x0003,0x0004,0x0005,0x0006,0x0008,0x0020,0x0102,0x0B05,0xFC10,0xFE00,0xFE01,0xFE03,0xFE04,0xFE05,0xFE06,0xFE08,0xFF02", outClusters: "0x0003,0x0019,0xFE03", model: "Track Drapery", deviceJoinName: "CPC Track Drapery"
    }


    tiles(scale: 2) {
        multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label: 'Open', action: "close", icon: "https://s3.amazonaws.com/current-st-icon/curtains_open.png", backgroundColor: "#00a9ff", nextState: "closing"
                attributeState "closed", label: 'Closed', action: "open", icon: "https://s3.amazonaws.com/current-st-icon/curtains_closed.png", backgroundColor: "#ffffff", nextState: "opening"
                attributeState "partially open", label: 'Partially open', action: "close", icon: "https://s3.amazonaws.com/current-st-icon/curtains_open.png", backgroundColor: "#fabe3b", nextState: "closing" //TODO: Add another state for going to opening next.
                attributeState "partially closed", label: 'Partially open', action: "open", icon: "https://s3.amazonaws.com/current-st-icon/curtains_open.png", backgroundColor: "#fabe3b", nextState: "opening" //TODO: Add another state for going to opening next.
                attributeState "opening", label: 'Opening', action: "pause", icon: "https://s3.amazonaws.com/current-st-icon/curtains_open.png", backgroundColor: "#00a9ff", nextState: "partially open"
                attributeState "closing", label: 'Closing', action: "pause", icon: "https://s3.amazonaws.com/current-st-icon/curtains_closed.png", backgroundColor: "#ffffff", nextState: "partially closed"
            }
        }
        standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "pause", label:"", icon:'st.sonos.pause-btn', action:'pause', backgroundColor:"#cccccc"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("reverse",  "device.reverse", decoration: "flat", width: 1, height: 1) {
        	state "default", label: "Reverse", action: "reverse", icon: "st.secondary.refresh"
        }
        standardTile("toggleSA", "device.switch", decoration: "flat", width: 1, height: 1) {
        	state "smartAssistOn", label: "SmartAssist Toggle", action: "smartAssistOff", /*backgroundColor: "#00a9ff",*/ nextState: "smartAssistOff"
            state "smartAssistOff", label: "SmartAssist Toggle", action: "smartAssistOn", /*backgroundColor: "#ffffff",*/ nextState: "smartAssistOn"
        }
        valueTile("shadeLevel", "device.level", width: 6, height: 1) {
            state "level", label: 'Shade is ${currentValue}% up', defaultState: true
        }//Testing with something new
        standardTile("closeButton", "device.level", width: 1, height: 1, decoration: "flat", icon: "none") {
        	state "default", label: "Close", action: "close", backgroundColor: "#555558"
        }
        standardTile("openButton", "device.level", width: 1, height: 1, decoration: "flat", icon: "none") {
        	state "default", label: "Open", action: "open", backgroundColor: "#00a0dc"
        } //
        controlTile("levelSliderControl", "device.level", "slider", width:4, height: 1, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }

        main "windowShade"
        details(["windowShade", "openButton", "levelSliderControl", "closeButton", /*"shadeLevel",*/ "contPause", "reverse", "toggleSA"])
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
                    sendEvent(name: "windowShade", value: "closed")
                    sendEvent(name: "level", value: "100")
                } else if (liftmap.value == "00") { //closed
                    sendEvent(name: "windowShade", value: "open")
                    sendEvent(name: "level", value: "0")
                } else {
                    sendEvent(name: "windowShade", value: "partially open")
                    sendEvent(name: "level", value: zigbee.convertHexToInt(liftmap.value))
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
	log.debug "ping()"
    return refresh()
}

def refresh() {
    log.info "refresh()"
    def cmds = zigbee.readAttribute(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT)
    return cmds
}

def reverse() {
	log.info "reverse()"
    zigbee.command(0xFC10, 0x23, "02", [mfgCode: 0x9999])
}

def smartAssistOn() {
	log.info "smartAssistOn()"
    zigbee.command(0xFC10, 0x24, "00", [mfgCode: 0x9999])
}

def smartAssistOff() {
	log.info "smartAssistOff()"
    zigbee.command(0xFC10, 0x24, "01", [mfgCode: 0x9999])
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    log.info "configure()"
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."
    zigbee.configureReporting(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT, DataType.UINT8, 0, 600, null)
}