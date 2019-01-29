/**
 *	Copyright 2015 SmartThings
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
 */

metadata {
	definition (name: "ZigBee Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true) {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"
		capability "Light"

		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "Aurora", model: "LCBulb01UK", deviceJoinName: "Aurora AOne Control Dimmer (120w)"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "LEEDARSON LIGHTING", model: "M350ST-W1R-01", deviceJoinName: "OZOM Dimmable LED Smart Light"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300", outClusters: "0003", manufacturer: "Aurora", model: "Dimmer", deviceJoinName: "Aurora AOne Control Dimmer (320w)"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 ON/OFF/DIM", deviceJoinName: "SYLVANIA Smart A19 Soft White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY A19 ON/OFF/DIM 10 Year", deviceJoinName: "SYLVANIA Smart 10-Year A19"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FF00", outClusters: "0019", manufacturer: "MRVL", model: "MZ100", deviceJoinName: "Wemo Bulb"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05", outClusters: "0019", manufacturer: "OSRAM SYLVANIA", model: "iQBR30", deviceJoinName: "Sylvania Ultra iQ"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY PAR38 ON/OFF/DIM", deviceJoinName: "SYLVANIA Smart PAR38 Soft White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04, FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY BR ON/OFF/DIM", deviceJoinName: "SYLVANIA Smart BR30 Soft White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G13", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G14", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G23", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-G33", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E12-N13", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E12-N14", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E12-N15", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL6HD", deviceJoinName: "Leviton Dimmer Switch"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL3HL", deviceJoinName: "Leviton Lumina RF Plug-In Dimmer"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL1KD", deviceJoinName: "Leviton Lumina RF Dimmer Switch"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B04, 0B05, FC01, FC08", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "A19 W 10 year", deviceJoinName: "SYLVANIA Smart 10Y A19 Soft White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, FC01", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "BR30 W 10 year", deviceJoinName: "SYLVANIA Smart 10Y BR30 Soft White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0300, 0B05, FC01", outClusters: "0003, 0019", manufacturer: "LEDVANCE", model: "PAR38 W 10 year", deviceJoinName: "SYLVANIA Smart 10Y PAR38 Soft White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "ZSD07", deviceJoinName: "Leviton Lumina RF 0-10V Dimming Wall Switch"
		fingerprint profileId: "0104", inClusters: "0000, 0004, 0003, 0006, 0008, 0005, FFFF, 1000", outClusters: "0019", manufacturer: "LDS", model: "ZBT-DIMLight-GLS0000", deviceJoinName: "A60 Dim Bulb"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "LDS", model: "ZHA-DIMLight-GLS0000", deviceJoinName: "A60 Dim Bulb"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "LDS", model: "ZBT-DIMLight-GLS", deviceJoinName: "A60 Dim Bulb"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "LDS", model: "ZBT-DIMLight-GLS0044", deviceJoinName: "智能球泡灯(可调光版)"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0009", outClusters: "0019", manufacturer: "Aurora", model: "FWMPROZXBulb50AU", deviceJoinName: "Aurora MPro"
		fingerprint profileId: "0104", inClusters: "0000, 0004, 0003, 0006, 0008, 0005, FFFF, 1000", outClusters: "0019", manufacturer: "Aurora", model: "FWBulb51AU", deviceJoinName: "Aurora Smart Dimmable"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0019", manufacturer: "Aurora", model: "FWStrip50AU", deviceJoinName: "Aurora Dimmable Strip Controller"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FEDC", outClusters: "000A, 0019", manufacturer: "Aurora", model: "FWGU10Bulb50AU", deviceJoinName: "Aurora Smart Dimmable GU10"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B05, 1000, FEDC", outClusters: "000A, 0019", manufacturer: "Smarthome", model: "S111-201A", deviceJoinName: "Leedarson Dimmable White Bulb A19"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "innr", model: "RB 265", deviceJoinName: "Innr Smart Bulb White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "innr", model: "RB 245", deviceJoinName: "Innr Smart Candle Warm White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 1000", outClusters: "0019", manufacturer: "innr", model: "RS 225", deviceJoinName: "Innr Smart Spot White"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-N13", deviceJoinName: "Sengled Element Classic"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0702, 0B05", outClusters: "0019", manufacturer: "sengled", model: "E11-N14", deviceJoinName: "Sengled Element Classic"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
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
		if (event.name=="level" && event.value==0) {}
		else {
			sendEvent(event)
		}
	} else {
		def descMap = zigbee.parseDescriptionAsMap(description)
		if (descMap && descMap.clusterInt == 0x0006 && descMap.commandInt == 0x07) {
			if (descMap.data[0] == "00") {
				log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
			}
		} else if (device.getDataValue("manufacturer") == "sengled" && descMap && descMap.clusterInt == 0x0008 && descMap.attrInt == 0x0000) {
			// This is being done because the sengled element touch/classic incorrectly uses the value 0xFF for the max level.
			// Per the ZCL spec for the UINT8 data type 0xFF is an invalid value, and 0xFE should be the max.  Here we
			// manually handle the invalid attribute value since it will be ignored by getEvent as an invalid value.
			// We also set the level of the bulb to 0xFE so future level reports will be 0xFE until it is changed by
			// something else.
			if (descMap.value.toUpperCase() == "FF") {
				descMap.value = "FE"
			}
			sendHubCommand(zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, 0x00, "FE0000").collect { new physicalgraph.device.HubAction(it) }, 0)
			sendEvent(zigbee.getEventFromAttrData(descMap.clusterInt, descMap.attrInt, descMap.encoding, descMap.value))
		} else {
			log.warn "DID NOT PARSE MESSAGE for description : $description"
			log.debug "${descMap}"
		}
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def setLevel(value) {
	def additionalCmds = []
	if (device.getDataValue("model") == "iQBR30" && value.toInteger() > 0) { // Handle iQ bulb not following spec
		additionalCmds = zigbee.on()
	} else if (device.getDataValue("manufacturer") == "MRVL") { // Handle marvel stack not reporting
		additionalCmds = refresh()
	}
	zigbee.setLevel(value) + additionalCmds
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.levelRefresh()
}

def installed() {
	if (((device.getDataValue("manufacturer") == "MRVL") && (device.getDataValue("model") == "MZ100")) || (device.getDataValue("manufacturer") == "OSRAM SYLVANIA") || (device.getDataValue("manufacturer") == "OSRAM")) {
		if ((device.currentState("level")?.value == null) || (device.currentState("level")?.value == 0)) {
			sendEvent(name: "level", value: 100)
		}
	}
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	refresh() + zigbee.onOffConfig(0, 300) + zigbee.levelConfig()
}
