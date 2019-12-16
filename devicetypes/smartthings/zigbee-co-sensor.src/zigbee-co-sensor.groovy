 /*
  *  Copyright 2019 SmartThings
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
  *  Author : Donald Kirker, Fen Mei / f.mei@samsung.com
  *  Date : 2019-02-26
  */

import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Zigbee CO Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.smoke", vid: "generic-carbon-monoxide-3") {
		capability "Carbon Monoxide Detector"
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500", outClusters: "0000", manufacturer: "ClimaxTechnology", model: "CO_00.00.00.22TC", deviceJoinName: "Ozom Smart Carbon Monoxide Sensor", mnmn: "SmartThings", vid: "generic-carbon-monoxide"
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500", outClusters: "0000", manufacturer: "ClimaxTechnology", model: "CO_00.00.00.15TC", deviceJoinName: "Ozom Smart Carbon Monoxide Sensor", mnmn: "SmartThings", vid: "generic-carbon-monoxide"
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0500", outClusters: "0000", manufacturer: "HEIMAN", model: "COSensor-EM", deviceJoinName: "HEIMAN CO Sensor"
	}

	tiles {
		multiAttributeTile(name:"carbonMonoxide", type: "lighting", width: 6, height: 4) {
			tileAttribute ("device.carbonMonoxide", key: "PRIMARY_CONTROL") {
				attributeState("clear", label: "clear", icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff")
				attributeState("detected", label: "MONOXIDE", icon: "st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor: "#e86d13")
			}
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "carbonMonoxide"
		details(["carbonMonoxide", "battery", "refresh"])
	}
}

def installed(){
	log.debug "installed"

	if (isOzomCO()) {
		sendEvent(name: "battery", value: 100, unit: "%", displayed: false)
	}

	response(refresh())
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
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	return result
}

def parseAttrMessage(String description){
	def descMap = zigbee.parseDescriptionAsMap(description)
	def map = [:]
	if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
		map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
	} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS) {
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
	def detected = value ? 'detected' : 'clear'
	String descriptionText = "${device.displayName} smoke ${detected}"
	return [name: 'carbonMonoxide',
			value: detected,
			descriptionText: descriptionText,
			translatable: true]
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
	Integer minReportTime = 0
	Integer maxReportTime = 180
	Integer reportableChange = null
	sendEvent(name: "checkInterval", value: maxReportTime * 2 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	return refresh() + zigbee.enrollResponse() + zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10) +
				zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, minReportTime, maxReportTime, reportableChange)
}

def isOzomCO() {
	return "ClimaxTechnology" == device.getDataValue("manufacturer") && ("CO_00.00.00.22TC" == device.getDataValue("model") || "CO_00.00.00.15TC" == device.getDataValue("model"))
}
