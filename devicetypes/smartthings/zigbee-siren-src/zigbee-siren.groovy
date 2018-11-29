/**
 *  Zigbee Siren
 *
 *  Copyright 2018 Samsung SRBR
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

import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "ZigBee Siren", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren") {
		capability "Actuator"
		capability "Alarm"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		fingerprint profileId: "0104", inClusters: "0000,0003,0500,0502", outClusters: "0000", manufacturer: "ClimaxTechnology", model: "SRAC_00.00.00.16TC", deviceJoinName: "Ozom Smart Siren" // Ozom Siren - SRAC-23ZBS
	}

	tiles {
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "off", label:'off', action:'alarm.siren', icon:"st.secondary.siren", backgroundColor:"#ffffff"
			state "siren", label:'siren!', action:'alarm.off', icon:"st.secondary.siren", backgroundColor:"#e86d13"
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "alarm"
		details(["alarm","refresh"])
	}
}

def installed() {
	response(refresh())
}

def parse(String description) {
	log.debug "parse ${description}"

	def map = zigbee.getEvent(description)

	if(!map) {
		if(isZoneMessage(description)) {
			map = parseIasMessage(description)
		} else {
			map = parseAttrMessage(description)
		}
	} 

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		def cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it)}
	}

	return result
}

private boolean isZoneMessage(description) {
	return (description?.startsWith('zone status') || description?.startsWith('zone report'))
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	def result = [:]
    def value = ""
	if(zs.isAlarm1Set() || zs.isAlarm2Set()) {    	
		value = "siren"
	} else {
    	value = "off"
	}
	def text = "Alarm state: ${value}"
	result = [name: "alarm", value: value, descriptionText: text, displayed: true]

	return result
}

private Map parseAttrMessage(description) {
	def descMap = zigbee.parseDescriptionAsMap(description)

	def map = [:]
	if(descMap.clusterInt == POLL_CONTROL_CLUSTER && descMap.commandInt == CHECK_IN_INTERVAL_CMD) {
		sendCheckIntervalEvent()
	} else if (descMap?.clusterInt == 0x0502 && descMap.attrInt == 0x0000) {
    	state.warningDuration = descMap.value

		map = [name: "warningDuration", value: state.warningDuration, descriptionText: "Maxwarning duration: ${state.warningDuration}", displayed: true]
	} 

	return map
}

private sendCheckIntervalEvent() {
	sendEvent(name: "checkInterval", value: 30 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def ping() {
	refresh()
}

def refresh() {
	return zigbee.readAttribute(0x0502, 0x0000)
}

def configure() {
	sendCheckIntervalEvent()

	return zigbee.enrollResponse() + refresh();
}

def siren() {	
	log.debug "sending siren on"
	def warningDuration = state.warningDuration ? state.warningDuration : "00B4"

	def cmds = []
    cmds << sendEvent(name: "alarm", value: "siren")
	cmds << zigbee.command(0x0502, 0x00, "12", warningDuration, "00", "00")
    cmds << runIn(zigbee.convertHexToInt(warningDuration), sirenTimeoutHandler)
	delayBetween(cmds, 500)
}

def off() {
	log.debug "sending siren off"
	def cmds = []
	cmds << sendEvent(name: "alarm", value: "off")
	cmds << zigbee.command(0x0502, 0x00, "00", "0000", "00", "00")
	cmds << unschedule(sirenTimeoutHandler)
	return delayBetween(cmds, 500)
}

def sirenTimeoutHandler() {
	log.debug "sirenTimeoutHandler - sending siren off"
	sendHubCommand(off())
}