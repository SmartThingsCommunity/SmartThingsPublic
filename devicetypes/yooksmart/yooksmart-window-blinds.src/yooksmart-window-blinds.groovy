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
    definition(name: "YookSmart Window Blinds", namespace: "yooksmart", author: "YookSmart", ocfDeviceType: "oic.d.blind", mnmn: "SmartThingsCommunity", vid: "58b24db0-3f12-3cd1-a86f-cc435ae6f991") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Window Shade"
        capability "Window Shade Preset"
        capability "Health Check"
        capability "Switch Level"
        capability "Battery"

        command "pause"
        
       	// attribute "lastCheckin", "String"
        attribute "lastOpened", "String"

        fingerprint inClusters: "0000,0001,0003,0004,0005,0102", outClusters: "0019", manufacturer: "Yookee", model: "D10110"
        fingerprint inClusters: "0000,0001,0003,0004,0005,0102", outClusters: "0019", manufacturer: "yooksmart", model: "D10110"
    }

    preferences {
		input "preset", "number", title: "Preset position", description: "Set the window shade preset position", defaultValue: 50, range: "1..100", required: false, displayDuringSetup: false
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
    // sendEvent(name: "lastCheckin", value: now)
    if (description?.startsWith("read attr -")) {
        Map descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap?.clusterInt == CLUSTER_WINDOW_COVERING && descMap.value) {
            log.debug "attr: ${descMap?.attrInt}, value: ${descMap?.value}, descValue: ${Integer.parseInt(descMap.value, 16)}, ${device.getDataValue("model")}"
            List<Map> descMaps = collectAttributes(descMap)
            def liftmap = descMaps.find { it.attrInt == ATTRIBUTE_POSITION_LIFT }
            if (liftmap && liftmap.value) {
                def newLevel = zigbee.convertHexToInt(liftmap.value)
                levelEventHandler(newLevel)
            }
        } else  if (descMap?.clusterInt == CLUSTER_BATTERY_LEVEL && descMap.value) {
            log.debug "attr: ${descMap?.attrInt}, value: ${descMap?.value}, descValue: ${zigbee.convertHexToInt(descMap.value)}"
            batteryPercentageEventHandler(zigbee.convertHexToInt(descMap.value))
        }
    }
}

def levelEventHandler(currentLevel) {
    def lastLevel = device.currentValue("level")
    log.debug "levelEventHandle - currentLevel: ${currentLevel} lastLevel: ${lastLevel}"
    if ((lastLevel == "undefined" || currentLevel == lastLevel) && state.invalidSameLevelEvent) { //Ignore invalid reports
        log.debug "Ignore invalid reports"
    } else {
        state.invalidSameLevelEvent = true
        currentLevel = currentLevel < 0 ? 0 : currentLevel > 100 ? 100 : currentLevel
        sendEvent(name: "level", value: 100 - currentLevel)
        if (currentLevel == 0 || currentLevel == 100) {
            sendEvent(name: "windowShade", value: currentLevel == 0 ? "closed" : "open")
        } else {
            if (lastLevel < currentLevel) {
                sendEvent([name:"windowShade", value: "opening"])
            } else if (lastLevel > currentLevel) {
                sendEvent([name:"windowShade", value: "closing"])
            }
            runIn(1, "updateFinalState", [overwrite:true])
        }
    }
    // runIn(3, "updateFinalState", [overwrite:true])
}

def updateFinalState() {
    def level = device.currentValue("level")
    log.debug "updateFinalState: ${level}"
    level = level < 0 ? 0 : level > 100 ? 100 : level
    if (level > 0 && level < 100) {
        sendEvent(name: "windowShade", value: "partially open")
    }
}

def batteryPercentageEventHandler(batteryLevel) {
	if (batteryLevel != null) {
		batteryLevel = Math.min(100, Math.max(0, batteryLevel))
		sendEvent([name: "battery", value: batteryLevel, unit: "%", descriptionText: "{{ device.displayName }} battery was {{ value }}%"])
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
    cmd = zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_GOTO_LIFT_PERCENTAGE, zigbee.convertToHexString(data, 2))


    return cmd
}

def pause() {
    log.info "pause()"
    def currentShadeStatus = device.currentValue("windowShade")

	if (currentShadeStatus == "open" || currentShadeStatus == "closed") {
		sendEvent(name: "windowShade", value: currentShadeStatus)
	} else {
		zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
	}
}

def presetPosition() {
	setLevel(preset ?: 50)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */

def ping() {
    return refresh()
}

def refresh() {
    log.info "refresh()"
    
    def cmds
	cmds = zigbee.readAttribute(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT) + zigbee.readAttribute(CLUSTER_BATTERY_LEVEL, 0x0021)
    return cmds
}

def installed() {
	state.invalidSameLevelEvent = true
	sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    log.info "configure()"
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."

    def cmds
    cmds = zigbee.configureReporting(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT, DataType.UINT8, 0, 600, null) + zigbee.configureReporting(CLUSTER_BATTERY_LEVEL, 0x0021, DataType.UINT8, 600, 21600, 0x01)

    return refresh() + cmds
}