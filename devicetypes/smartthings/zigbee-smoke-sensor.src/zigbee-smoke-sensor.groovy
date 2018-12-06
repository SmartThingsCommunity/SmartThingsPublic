 /*
  *  Copyright 2018 SmartThings
  *
  *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  *  use this file except in compliance with the License. You may obtain a copy
  *  of the License at:
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  *  License for the specific language governing permissions and limitations
  *  under the License.
  *  Author : Fen Mei / f.mei@samsung.com
  *  Date : 2018-07-06
  */

import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Zigbee Smoke Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.smoke", vid: "generic-smoke") {
		capability "Smoke Detector"
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0500,0502,0009", outClusters: "0019", manufacturer: "Heiman", model: "b5db59bfd81e4f1f95dc57fdbba17931", deviceJoinName: "Orvibo Smoke Sensor"
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0500,0502,0009", outClusters: "0019", manufacturer: "HEIMAN", model: "98293058552c49f38ad0748541ee96ba", deviceJoinName: "Orvibo Smoke Sensor"
	}

	tiles {
		standardTile("smoke", "device.smoke", width: 2, height: 2) {
			state("clear", label:"Clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
			state("detected", label:"Smoke!", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
		}
        
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "smoke"
		details(["smoke", "battery", "refresh"])
	}
}

def installed(){
	log.debug "installed"
	refresh()
}

def parse(String description) {
	log.debug "description(): $description"
	def map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} else {
			map = parseAttrMessage(description)
		}
	}
	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : [:]
	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it)}
	}
	return result
}

def parseAttrMessage(String description){
	def descMap = zigbee.parseDescriptionAsMap(description)
	def map = [:]
	if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
		map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
	} else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
		def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
		map = translateZoneStatus(zs)
	}
	return map;
}

def parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	return getDetectedResult(zs.isAlarm1Set() || zs.isAlarm2Set())
}

private Map translateZoneStatus(ZoneStatus zs) {
	return getDetectedResult(zs.isAlarm1Set() || zs.isAlarm2Set())
}

private Map getBatteryPercentageResult(rawValue) {
	log.debug "Battery Percentage rawValue = ${rawValue} -> ${rawValue / 2}%"
	def result = [:]

	if (0 <= rawValue && rawValue <= 200) {
		result.name = 'battery'
		result.translatable = true
		result.value = Math.round(rawValue / 2)
		result.descriptionText = "${device.displayName} battery was ${result.value}%"
	}

	return result
}
def getDetectedResult(value) {
	def detected = value ? 'detected': 'clear'
	String descriptionText = "${device.displayName} smoke ${detected}"
	return [name:'smoke',
			value: detected,
			descriptionText:descriptionText,
			translatable:true]
}

def refresh() {
	log.debug "Refreshing Values"
	def refreshCmds = []
	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) +
					zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
	return refreshCmds
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping "
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}
def configure() {
	log.debug "configure"
	sendEvent(name: "checkInterval", value:6 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	return refresh() + zigbee.enrollResponse() + zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10)
}

