/**
 *  Ecolink Zigbee Water/Freeze Sensor
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
	definition(name: "Ecolink Zigbee Water/Freeze Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture") {
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Water Sensor"
		capability "Temperature Measurement"
		capability "Temperature Alarm"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0020,0402,0500,0B05,FC01,FC02", outClusters: "0019", manufacturer: "Ecolink", model: "FLZB1-ECO", deviceJoinName: "Ecolink Water Leak Sensor" //Ecolink Water/Freeze Sensor
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState("wet", label:'${name}', icon:"st.alarm.water.wet", backgroundColor:"#00A0DC")
				attributeState("dry", label:'${name}', icon:"st.alarm.water.dry", backgroundColor:"#ffffff")
			}
		}
		standardTile("temperatureAlarm", "device.temperatureAlarm", width: 4, height: 2, decoration: "flat") {
			state "cleared", icon: "st.Weather.weather14", label: '${name}', unit: ""
			state "freeze", icon: "st.Weather.weather7", label: '${name}', unit: ""
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
		main "water"
		details(["water", "temperature", "temperatureAlarm", "battery"])
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
private getDEVICE_CHECK_IN_INTERVAL_VAL_HEX() { 0x1C20 }
private getDEVICE_CHECK_IN_INTERVAL_VAL_INT() { 30 * 60 }

def installed() {
	sendEvent(name: "water", value: "dry", displayed: false)
	sendEvent(name: "temperatureAlarm", value: "cleared", displayed: false)
	refresh()
}

def parse(String description) {
	def map = zigbee.getEvent(description)
	if(!map) {
		if(description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} else {
			map = parseAttrMessage(description)
		}
	} else if (map.name == "temperature") {
		freezeStatus(map.value)
		if (tempOffset) {
			map.value = (int) map.value + (int) tempOffset
		}
		map.descriptionText = temperatureScale == 'C' ? "${device.displayName} was ${map.value}°C" : "${device.displayName} was ${map.value}°F"
		map.translatable = true
	}

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		def cmds = zigbee.enrollResponse()
		result = cmds?.collect { new physicalgraph.device.HubAction(it)}
	}
	return result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	def result = [:]
	if(zs.isAlarm1Set() || zs.isAlarm2Set()) {
		result = getMoistureDetection("wet")
	} else if(!zs.isTamperSet()) {
		result = getMoistureDetection("dry")
	} else {
		result = [displayed: true, descriptionText: "${device.displayName}'s case is opened"]
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

private freezeStatus(temperature) {
	def result = [name: "temperatureAlarm", isStateChanged: true]
	def freezePoint = temperatureScale == 'C' ? 0 : 32
	result.value = (temperature <= freezePoint) ? "freeze" : "cleared"
	result.descriptionText = "${device.displayName}'s state is ${result.value}"
	sendEvent(result)
}

private Map getBatteryPercentageResult(rawValue) {
	def result = [:]
	def volts = rawValue / 10
	if(!(rawValue == 0 || rawValue == 255)) {
		def minVolts = 2.2
		def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		def roundedPct = Math.round(pct * 100)
		if (roundedPct <= 0) {
			roundedPct = 1
		}
		result.value = Math.min(100, roundedPct)
	} else if(rawValue == 0) {
		result.value = 100
	}
	result.name = 'battery'
	result.translatable = true
	result.descriptionText = "${device.displayName} battery was ${result.value}%"
	return result
}

private Map getMoistureDetection(value) {
	def description = (value == "wet") ? "detected" : "not detected"
	def text = "Water was ${description}"
	def result = [name: "water", value: value, descriptionText: text, displayed: true, isStateChanged: true]
	return result
}

private sendCheckIntervalEvent() {
	sendEvent(name: "checkInterval", value: DEVICE_CHECK_IN_INTERVAL_VAL_INT, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def ping() {
	refresh()
}

def refresh() {
	return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE_VALUE) +
			zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, TEMPERATURE_MEASURE_VALUE)
}

def configure() {
	sendCheckIntervalEvent()

	def createBinding = zigbee.addBinding(POLL_CONTROL_CLUSTER)

	def enrollCmds = zigbee.writeAttribute(POLL_CONTROL_CLUSTER, CHECK_IN_INTERVAL_ATTR, DataType.UINT32, DEVICE_CHECK_IN_INTERVAL_VAL_HEX) +
			zigbee.command(POLL_CONTROL_CLUSTER, SET_SHORT_POLL_INTERVAL_CMD, "0200") +
			zigbee.writeAttribute(POLL_CONTROL_CLUSTER, FAST_POLL_TIMEOUT_ATTR, DataType.UINT16, 0x0028) +
			zigbee.command(POLL_CONTROL_CLUSTER, SET_LONG_POLL_INTERVAL_CMD, "B1040000")

	return zigbee.enrollResponse() + createBinding + zigbee.batteryConfig() +
			zigbee.temperatureConfig(DEVICE_CHECK_IN_INTERVAL_VAL_INT, DEVICE_CHECK_IN_INTERVAL_VAL_INT + 1) +
			refresh() + enrollCmds
}
