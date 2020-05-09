/**
 *  Zigbee Sound Sensor
 *
 *  Copyright 2018 Samsung SRPOL
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
	definition(name: "ZigBee Sound Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren") {
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Sound Sensor"
		capability "Temperature Measurement"

		fingerprint profileId: "0104", inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "Ecolink", model: "FFZB1-SM-ECO", deviceJoinName: "Ecolink Firefighter"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"sound", type: "lighting", width: 6, height: 4) {
			tileAttribute ("device.sound", key: "PRIMARY_CONTROL") {
				attributeState("not detected", label:'${name}', icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
				attributeState("detected", label:'${name}', icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label: '${currentValue}°',
					backgroundColors: [
							[value: 31, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					])
		}

		main "sound"
		details(["sound", "battery", "temperature"])
	}
}

private getPOLL_CONTROL_CLUSTER() { 0x0020 }
private getFAST_POLL_TIMEOUT_ATTR() { 0x0003 }
private getCHECK_IN_INTERVAL_ATTR() { 0x0000 }
private getBATTERY_VOLTAGE_VALUE() { 0x0020 }
private getTEMPERATURE_MEASURE_VALUE() { 0x0000 }
private getSET_LONG_POLL_INTERVAL_CMD() { 0x02 }
private getSET_SHORT_POLL_INTERVAL_CMD() { 0x03 }
private getCHECK_IN_INTERVAL_CMD() { 0x00 }

def installed() {
	sendEvent(name: "sound", value: "not detected", displayed: false)
	response(refresh())
}

def parse(String description) {
	def map = zigbee.getEvent(description)

	if(!map) {
		if(isZoneMessage(description)) {
			map = parseIasMessage(description)
		} else {
			map = parseAttrMessage(description)
		}
	} else if (map.name == "temperature") {
		if (tempOffset) {
			map.value = (int) map.value + (int) tempOffset
		}
		map.descriptionText = temperatureScale == 'C' ? "${device.displayName} was ${map.value}°C" : "${device.displayName} was ${map.value}°F"
		map.translatable = true
	}

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		def cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it)}
	}
	return result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	def result = [:]
	if(zs.isAlarm1Set() || zs.isAlarm2Set()) {
		result = getSoundDetectionResult("detected")
	} else if(!zs.isTamperSet()) {
		result = getSoundDetectionResult("not detected")
	} else {
		result = [displayed: true, descriptionText: "${device.displayName}'s case is opened"]
		sendHubCommand zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE_VALUE)
	}

	return result
}

private Map parseAttrMessage(description) {
	def descMap = zigbee.parseDescriptionAsMap(description)
	def map = [:]
	if(descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap?.value) {
		map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
	} else if(descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
		if (descMap.data[0] == "00") {
			sendCheckIntervalEvent()
		} else {
			log.warn "TEMP REPORTING CONFIG FAILED - error code: ${descMap.data[0]}"
		}
	} else if(descMap.clusterInt == POLL_CONTROL_CLUSTER && descMap.commandInt == CHECK_IN_INTERVAL_CMD) {
		sendCheckIntervalEvent()
	}

	return map
}

private Map getBatteryPercentageResult(rawValue) {
	def result = [:]
	def volts = rawValue / 10
	if (!(rawValue == 0 || rawValue == 255)) {
		def minVolts = 2.2
		def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		def roundedPct = Math.round(pct * 100)
		if (roundedPct <= 0) {
			roundedPct = 1
		}
		result.value = Math.min(100, roundedPct)
	}
	result.name = 'battery'
	result.translatable = true
	result.descriptionText = "${device.displayName} battery was ${result.value}%"
	return result
}

private Map getSoundDetectionResult(value) {
	def text = "Sound was ${value}"
	def result = [name: "sound", value: value, descriptionText: text, displayed: true]
	return result
}

private sendCheckIntervalEvent() {
	sendEvent(name: "checkInterval", value: 60 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def ping() {
	refresh()
}

def refresh() {
	return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE_VALUE) +
			zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, TEMPERATURE_MEASURE_VALUE) +
			zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def configure() {
	sendCheckIntervalEvent()

	//send zone enroll response, configure short and long poll, fast poll timeout and check in interval
	def enrollCmds = (zigbee.command(POLL_CONTROL_CLUSTER, SET_LONG_POLL_INTERVAL_CMD, "B0040000") + zigbee.command(POLL_CONTROL_CLUSTER, SET_SHORT_POLL_INTERVAL_CMD, "0200") +
			zigbee.writeAttribute(POLL_CONTROL_CLUSTER, FAST_POLL_TIMEOUT_ATTR, DataType.UINT16, 0x0028) + zigbee.writeAttribute(POLL_CONTROL_CLUSTER, CHECK_IN_INTERVAL_ATTR, DataType.UINT32, 0x00001950))

	//send enroll commands, configures battery reporting to happen every 30 minutes, create binding for check in attribute so check ins will occur
	return zigbee.enrollResponse() + zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 30, 60 * 30, null) + zigbee.batteryConfig(60 * 30, 60 * 30 + 1) + zigbee.temperatureConfig(60 * 30, 60 * 30 + 1) + zigbee.configureReporting(POLL_CONTROL_CLUSTER, CHECK_IN_INTERVAL_ATTR, DataType.UINT32, 0, 3600, null) + refresh() + enrollCmds
}

private boolean isZoneMessage(description) {
	return (description?.startsWith('zone status') || description?.startsWith('zone report'))
}