/**
 *
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 *
 *  first release for IKEA smart window blinds
 */
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition(name: "IKEA Window Blinds", namespace: "a4refillpad", author: "Wayne Man", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-shade") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Window Shade"
        capability "Health Check"
        capability "Switch Level"
        capability "Battery"

        command "pause"
        
       	attribute "lastCheckin", "String"
        attribute "lastOpened", "String"

        fingerprint inClusters: "0000,0001,0003,0004", manufacturer: "IKEA of Sweden", model: "FYRTUR block-out roller blind"
    }


    tiles(scale: 2) {
        multiAttributeTile(name:"windowShade", type: "generic", width: 6, height: 4) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label: 'Open', action: "close", icon: "https://raw.githubusercontent.com/a4refillpad/media/master/blind-open.png", backgroundColor: "#e86d13", nextState: "closing"
                attributeState "closed", label: 'Closed', action: "open", icon: "https://raw.githubusercontent.com/a4refillpad/media/master/blind-closed.png", backgroundColor: "#00A0DC", nextState: "opening"
                attributeState "partially open", label: 'Partially open', action: "close", icon: "https://raw.githubusercontent.com/a4refillpad/media/master/blind-part-open.png", backgroundColor: "#d45614", nextState: "closing"
                attributeState "opening", label: 'Opening', action: "pause", icon: "st.thermostat.thermostat-up", backgroundColor: "#e86d13", nextState: "partially open"
                attributeState "closing", label: 'Closing', action: "pause", icon: "st.thermostat.thermostat-down", backgroundColor: "#00A0DC", nextState: "partially open"
            }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
        }
        standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "pause", label:"", icon:'st.sonos.stop-btn', action:'pause', backgroundColor:"#cccccc"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("shadeLevel", "device.level", width: 3, height: 1) {
            state "level", label: 'Blind is ${currentValue}% open', defaultState: true
        }
        controlTile("levelSliderControl", "device.level", "slider", width:3, height: 1, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }
        standardTile("resetClosed", "device.windowShade", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", action:"close", label: "Close", icon:"https://raw.githubusercontent.com/a4refillpad/media/master/blind-closed.png"
	  	}
		standardTile("resetOpen", "device.windowShade", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", action:"open", label: "Open", icon:"https://raw.githubusercontent.com/a4refillpad/media/master/blind-open.png"
	  	}
 		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

        main "windowShade"
        details(["windowShade", "shadeLevel", "levelSliderControl", "contPause", "battery", "refresh", "resetClosed", "resetOpen"])
    }
}

private getCLUSTER_BATTERY_LEVEL() { 0x0001 }
private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getCOMMAND_OPEN() { 0x00 }
private getCOMMAND_CLOSE() { 0x01 }
private getCOMMAND_PAUSE() { 0x02 }
private getCOMMAND_GOTO_LIFT_PERCENTAGE() { 0x05 }
private getATTRIBUTE_POSITION_LIFT() { 0x0008 }
private getATTRIBUTE_CURRENT_LEVEL() { 0x0000 }
private getCOMMAND_MOVE_LEVEL_ONOFF() { 0x04 }

private List<Map> collectAttributes(Map descMap) {
	List<Map> descMaps = new ArrayList<Map>()

	descMaps.add(descMap)

	if (descMap.additionalAttrs) {
		descMaps.addAll(descMap.additionalAttrs)
	}

	return descMaps
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "description:- ${description}"
    def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    //  send event for heartbeat    
    sendEvent(name: "lastCheckin", value: now)
    if (description?.startsWith("read attr -")) {
        Map descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap?.clusterInt == CLUSTER_WINDOW_COVERING && descMap.value) {
            log.debug "attr: ${descMap?.attrInt}, value: ${descMap?.value}, descValue: ${Integer.parseInt(descMap.value, 16)}, ${device.getDataValue("model")}"
            List<Map> descMaps = collectAttributes(descMap)
            def liftmap = descMaps.find { it.attrInt == ATTRIBUTE_POSITION_LIFT }
            if (liftmap && liftmap.value) {
                def newLevel = 100 - zigbee.convertHexToInt(liftmap.value)
                levelEventHandler(newLevel)
            }
        } 
        if (descMap?.clusterInt == CLUSTER_BATTERY_LEVEL && descMap.value) {
            log.debug "attr: ${descMap?.attrInt}, value: ${descMap?.value}, descValue: ${Integer.parseInt(descMap.value, 16)}"
            sendEvent(name: "battery", value: Integer.parseInt(descMap.value, 16))
        }
    }
}

def levelEventHandler(currentLevel) {
    def lastLevel = device.currentValue("level")
    log.debug "levelEventHandle - currentLevel: ${currentLevel} lastLevel: ${lastLevel}"
    if (lastLevel == "undefined" || currentLevel == lastLevel) { //Ignore invalid reports
        log.debug "Ignore invalid reports"
    } else {
        sendEvent(name: "level", value: currentLevel)
        if (currentLevel == 0 || currentLevel == 100) {
            sendEvent(name: "windowShade", value: currentLevel == 0 ? "closed" : "open")
        } else {
            if (lastLevel < currentLevel) {
                sendEvent([name:"windowShade", value: "opening"])
            } else if (lastLevel > currentLevel) {
                sendEvent([name:"windowShade", value: "closing"])
            }
            runIn(3, "updateFinalState", [overwrite:true])
        }
    }
    runIn(3, "updateFinalState", [overwrite:true])
}

def updateFinalState() {
    def level = device.currentValue("level")
    log.debug "updateFinalState: ${level}"
    if (level > 0 && level < 100) {
        sendEvent(name: "windowShade", value: "partially open")
    }
}

def close() {
    log.info "close()"
    zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_CLOSE)

}

def open() {
    log.info "open()"
    zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_OPEN)
}

def setLevel(data, rate = null) {
    data = data.toInteger()
    log.info "setLevel()"
    def cmd
    cmd = zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_GOTO_LIFT_PERCENTAGE, zigbee.convertToHexString(100 - data, 2))


    return cmd
}

def pause() {
    log.info "pause()"
    zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */

def ping() {
    return zigbee.readAttribute(CLUSTER_BATTERY_LEVEL, 0x0021) // Read the Battery Level
}

def refresh() {
    log.info "refresh()"
    
    def cmds
    cmds = zigbee.readAttribute(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT) + zigbee.readAttribute(CLUSTER_BATTERY_LEVEL, 0x0021) 

    return cmds
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    log.info "configure()"
    sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."

    def cmds
    cmds = zigbee.configureReporting(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT, DataType.UINT8, 0, 600, null) + zigbee.configureReporting(CLUSTER_BATTERY_LEVEL, 0x0021, DataType.UINT8, 600, 21600, 0x01)

    return refresh() + cmds
}