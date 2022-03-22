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

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "ZigBee Window Shade", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-shade") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Window Shade"
		capability "Window Shade Level"
		capability "Window Shade Preset"
		capability "Health Check"
		capability "Switch Level"

		command "pause"
        
 		// NodOn
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", outClusters: "0019", manufacturer: "NodOn", model: "SIN-4-RS-20", deviceJoinName: "NodOn Window Treatment" 
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", outClusters: "0019", manufacturer: "NodOn", model: "SIN-4-RS-20_PRO", deviceJoinName: "NodOn Window Treatment"
        
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0102", outClusters: "0019", model: "E2B0-KR000Z0-HA", deviceJoinName: "eZEX Window Treatment" // SY-IoT201-BD //SOMFY Blind Controller/eZEX
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", outClusters: "000A", manufacturer: "Feibit Co.Ltd", model: "FTB56-ZT218AK1.6", deviceJoinName: "Wistar Window Treatment" //Wistar Curtain Motor(CMJ)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0102", outClusters: "000A", manufacturer: "Feibit Co.Ltd", model: "FTB56-ZT218AK1.8", deviceJoinName: "Wistar Window Treatment" //Wistar Curtain Motor(CMJ)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0102", outClusters: "0003", manufacturer: "REXENSE", model: "KG0001", deviceJoinName: "Window Treatment" //Smart Curtain Motor(BCM300D)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0102", outClusters: "0003", manufacturer: "REXENSE", model: "DY0010", deviceJoinName: "Window Treatment" //Smart Curtain Motor(DT82TV)
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0102", outClusters: "0003", manufacturer: "SOMFY", model: "Glydea Ultra Curtain", deviceJoinName: "Somfy Window Treatment" //Somfy Glydea Ultra
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0020, 0102", outClusters: "0003", manufacturer: "SOMFY", model: "Sonesse 30 WF Roller", deviceJoinName: "Somfy Window Treatment" // Somfy Sonesse 30 Zigbee LI-ION Pack
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0020, 0102", outClusters: "0003", manufacturer: "SOMFY", model: "Sonesse 40 Roller", deviceJoinName: "Somfy Window Treatment" // Somfy Sonesse 40
		fingerprint inClusters: "0000,0001,0003,0004,0005,0102", outClusters: "0019", manufacturer: "Third Reality, Inc", model: "3RSB015BZ", deviceJoinName: "ThirdReality smart Blind" // ThirdReality
	}

	preferences {
		input "preset", "number", title: "Preset position", description: "Set the window shade preset position", defaultValue: 50, range: "1..100", required: false, displayDuringSetup: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"windowShade", type: "lighting", width: 6, height: 4) {
			tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
				attributeState "open", label: 'Open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
				attributeState "closed", label: 'Closed', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
				attributeState "partially open", label: 'Partially open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#d45614", nextState: "closing"
				attributeState "opening", label: 'Opening', action: "pause", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "partially open"
				attributeState "closing", label: 'Closing', action: "pause", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "partially open"
			}
			tileAttribute ("device.windowShadeLevel", key: "SLIDER_CONTROL") {
				attributeState "shadeLevel", action:"setShadeLevel"
			}
		}
		standardTile("contPause", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "pause", label:"", icon:'st.sonos.pause-btn', action:'pause', backgroundColor:"#cccccc"
		}
		standardTile("presetPosition", "device.presetPosition", width: 2, height: 2, decoration: "flat") {
			state "default", label: "Preset", action:"presetPosition", icon:"st.Home.home2"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "windowShade"
		details(["windowShade", "contPause", "presetPosition", "refresh"])
	}
}

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

	if (device.currentValue("shadeLevel") == null && device.currentValue("level") != null) {
		sendEvent(name: "shadeLevel", value: device.currentValue("level"), unit: "%")
	}

	if (description?.startsWith("read attr -")) {
		Map descMap = zigbee.parseDescriptionAsMap(description)

		if (isBindingTableMessage(description)) {
			parseBindingTableMessage(description)
		} else if (supportsLiftPercentage() && descMap?.clusterInt == CLUSTER_WINDOW_COVERING && descMap.value) {
			log.debug "attr: ${descMap?.attrInt}, value: ${descMap?.value}, descValue: ${Integer.parseInt(descMap.value, 16)}, ${device.getDataValue("model")}"
			List<Map> descMaps = collectAttributes(descMap)
			def liftmap = descMaps.find { it.attrInt == ATTRIBUTE_POSITION_LIFT }

			if (liftmap && liftmap.value) {
				def newLevel = zigbee.convertHexToInt(liftmap.value)

				if (shouldInvertLiftPercentage()) {
					// some devices report % level of being closed (instead of % level of being opened)
					// inverting that logic is needed here to avoid a code duplication
					newLevel = 100 - newLevel
				}
				levelEventHandler(newLevel)
			}
		} else if (!supportsLiftPercentage() && descMap?.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER && descMap.value) {
			def valueInt = Math.round((zigbee.convertHexToInt(descMap.value)) / 255 * 100)

			levelEventHandler(valueInt)
		}
	}
}

def getLastLevel() {
	device.currentState("shadeLevel") ? device.currentValue("shadeLevel") : device.currentValue("level") // Try shadeLevel, if not use level and pass to logic below
}

def levelEventHandler(currentLevel) {
	def priorLevel = lastLevel
	log.debug "levelEventHandle - currentLevel: ${currentLevel} priorLevel: ${priorLevel}"

	if ((priorLevel == "undefined" || currentLevel == priorLevel) && state.invalidSameLevelEvent) { //Ignore invalid reports
		log.debug "Ignore invalid reports"
	} else {
		state.invalidSameLevelEvent = true

		sendEvent(name: "shadeLevel", value: currentLevel, unit: "%")
		sendEvent(name: "level", value: currentLevel, unit: "%", displayed: false)

		if (currentLevel == 0 || currentLevel == 100) {
		        if (device.getDataValue("manufacturer") == "Third Reality, Inc"){
			        sendEvent(name: "windowShade", value: currentLevel == 0 ? "open" : "closed")
			} else {
			        sendEvent(name: "windowShade", value: currentLevel == 0 ? "closed" : "open")
			}
		} else {
			if (priorLevel < currentLevel) {
				sendEvent([name:"windowShade", value: "opening"])
			} else if (priorLevel > currentLevel) {
				sendEvent([name:"windowShade", value: "closing"])
			}
			runIn(1, "updateFinalState", [overwrite:true])
		}
	}
}

def updateFinalState() {
	def level = device.currentValue("shadeLevel")
	log.debug "updateFinalState: ${level}"

	if (level > 0 && level < 100) {
		sendEvent(name: "windowShade", value: "partially open")
	}
}

def supportsLiftPercentage() {
	device.getDataValue("manufacturer") != "Feibit Co.Ltd"
}

def close() {
	log.info "close()"
	zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_CLOSE)
}

def open() {
	log.info "open()"
	zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_OPEN)
}

def setLevel(value, rate = null) {
	log.info "setLevel($value)"

	setShadeLevel(value)
}

def setShadeLevel(value) {
	log.info "setShadeLevel($value)"

	Integer level = Math.max(Math.min(value as Integer, 100), 0)
	def cmd

	if (isSomfy() && Math.abs(level - lastLevel) <= GLYDEA_MOVE_THRESHOLD) {
		state.invalidSameLevelEvent = false
	}

	if (supportsLiftPercentage()) {
		if (shouldInvertLiftPercentage()) {
			// some devices keeps % level of being closed (instead of % level of being opened)
			// inverting that logic is needed here
			level = 100 - level
		}
		cmd = zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_GOTO_LIFT_PERCENTAGE, zigbee.convertToHexString(level, 2))
	} else {
		cmd = zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, COMMAND_MOVE_LEVEL_ONOFF, zigbee.convertToHexString(Math.round(level * 255 / 100), 2))
	}

	return cmd
}

def pause() {
	log.info "pause()"
	def currentShadeStatus = device.currentValue("windowShade")

        if (device.getDataValue("manufacturer") == "Third Reality, Inc") {
	        zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
	} else {
	        if (currentShadeStatus == "open" || currentShadeStatus == "closed") {
		        sendEvent(name: "windowShade", value: currentShadeStatus)
	        } else {
		        zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
	        }
	}
}

def presetPosition() {
	setShadeLevel(preset ?: 50)
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

	if (supportsLiftPercentage()) {
		cmds = zigbee.readAttribute(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT)
	} else {
		cmds = zigbee.readAttribute(zigbee.LEVEL_CONTROL_CLUSTER, ATTRIBUTE_CURRENT_LEVEL)
	}

	return cmds
}

def installed() {
	log.debug "installed"

	state.invalidSameLevelEvent = true

	sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)
}

def configure() {
	def cmds

	log.info "configure()"

	// Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	log.debug "Configuring Reporting and Bindings."

	if (supportsLiftPercentage()) {
		cmds = zigbee.configureReporting(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT, DataType.UINT8, 0, 600, null)
	} else {
		cmds = zigbee.levelConfig()
	}

	return refresh() + cmds
}

private def parseBindingTableMessage(description) {
	Integer groupAddr = getGroupAddrFromBindingTable(description)
	if (groupAddr) {
		List cmds = addHubToGroup(groupAddr)
		cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
}

private Integer getGroupAddrFromBindingTable(description) {
	log.info "Parsing binding table - '$description'"
	def btr = zigbee.parseBindingTableResponse(description)
	def groupEntry = btr?.table_entries?.find { it.dstAddrMode == 1 }

	log.info "Found ${groupEntry}"

	!groupEntry?.dstAddr ?: Integer.parseInt(groupEntry.dstAddr, 16)
}

private List addHubToGroup(Integer groupAddr) {
	["st cmd 0x0000 0x01 ${CLUSTER_GROUPS} 0x00 {${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr,4))} 00}", "delay 200"]
}

private List readDeviceBindingTable() {
	["zdo mgmt-bind 0x${device.deviceNetworkId} 0", "delay 200"]
}

def shouldInvertLiftPercentage() {
	return isSomfy()
}

def isSomfy() {
	device.getDataValue("manufacturer") == "SOMFY"
}

private getGLYDEA_MOVE_THRESHOLD() { 3 }
