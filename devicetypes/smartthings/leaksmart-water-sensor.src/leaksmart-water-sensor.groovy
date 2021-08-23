/**
 *  leakSMART Water Sensor
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

import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Leaksmart Water Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture", mnmn: "SmartThings", vid: "generic-leak") {
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Water Sensor"
		capability "Temperature Measurement"

		fingerprint inClusters: "0000,0001,0003,0402,0B02,FC02", outClusters: "0003,0019", manufacturer: "WAXMAN", model: "leakSMART Water Sensor V2", deviceJoinName: "leakSMART Water Leak Sensor" //leakSMART Water Sensor
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState("wet", label:'${name}', icon:"st.alarm.water.wet", backgroundColor:"#00A0DC")
				attributeState("dry", label:'${name}', icon:"st.alarm.water.dry", backgroundColor:"#ffffff")
			}
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
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		main "water"
		details(["water", "temperature", "battery", "refresh"])
	}
}

private getBATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getTEMPERATURE_MEASURE_VALUE() { 0x0000 }
private getEVENTS_ALERTS_CLUSTER() { 0x0B02 }

def installed() {
	sendEvent(name: "water", value: "dry", displayed: false)
	refresh()
}

def parse(String description) {
	def map = zigbee.getEvent(description)
	if(!map) {
		map = parseAttrMessage(description)
	} else if (map.name == "temperature") {
		if (tempOffset) {
			map.value = new BigDecimal((map.value as float) + (tempOffset as float)).setScale(1, BigDecimal.ROUND_HALF_UP)
		}
		map.descriptionText = temperatureScale == 'C' ? "${device.displayName} was ${map.value}°C" : "${device.displayName} was ${map.value}°F"
		map.translatable = true
	}

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		def cmds = zigbee.enrollResponse()
		result = cmds?.collect { new physicalgraph.device.HubAction(it)}
	}
	log.debug "Description ${description} parsed to ${result}"
	return result
}

private Map parseAttrMessage(description) {
	def descMap = zigbee.parseDescriptionAsMap(description)
	def map = [:]
	if(descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap?.value) {
		map = getBatteryResult(Integer.parseInt(descMap.value, 16))
	} else if(descMap?.clusterInt == EVENTS_ALERTS_CLUSTER && descMap?.commandInt == 0x01) {
		map = descMap?.data[1] == "81" ? getWaterDetection(descMap?.data[2]) : [:]
	} else if(descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
		if (descMap.data[0] == "00") {
			sendCheckIntervalEvent()
		} else {
			log.warn "TEMP REPORTING CONFIG FAILED - error code: ${descMap.data[0]}"
		}
	}
	return map
}

private Map getWaterDetection(alertData) {
	def value = (alertData == "11") ? "wet" : "dry"
	def description = (value == "wet") ? "detected" : "not detected"
	def result = [name: "water", value: value, descriptionText: "Water was ${description}", displayed: true, isStateChanged: true]
	return result
}

private Map getBatteryResult(value) {
	def result = [:]
	result.value = value / 2
	result.name = 'battery'
	result.descriptionText = "${device.displayName} battery was ${result.value}%"
	return result
}

private sendCheckIntervalEvent() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def ping() {
	refresh()
}

def refresh() {
	return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_PERCENTAGE_REMAINING) +
			zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, TEMPERATURE_MEASURE_VALUE)
}

def configure() {
	sendCheckIntervalEvent()
	log.debug "Configuring Reporting"
	def configCmds = zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_PERCENTAGE_REMAINING, DataType.UINT8, 30, 21600, 0x01) +
			zigbee.temperatureConfig(30, 300) +
			zigbee.addBinding(EVENTS_ALERTS_CLUSTER)

	return refresh() + configCmds + refresh()
}
