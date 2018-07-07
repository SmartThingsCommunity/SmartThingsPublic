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
 *
 *  Orvibo Contact Sensor
 *
 *  Author: Deng Biaoyi/biaoyi.deng@samsung.com
 *
 *  Date:2018-07-03
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Orvibo Moisture Sensor", namespace: "smartthings", author: "SmartThings", vid: "generic-leak", mnmn:"SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture") {
		capability "Configuration"
		capability "Refresh"
		capability "Water Sensor"
		capability "Sensor"
		capability "Health Check"
		capability "Alarm"

		fingerprint profileId: "0104",deviceId: "0402",inClusters: "0000,0003,0500,0001,0009", outClusters: "0019", manufacturer: "Heiman", model: "2f077707a13f4120846e0775df7e2efe"
	}
	simulator {

		status "dry": "zone status 0x0020 -- extended status 0x00"
		status "wet": "zone status 0x0021 -- extended status 0x00"


		for (int i = 0; i <= 90; i += 10) {
			status "battery 0021 0x${i}": "read attr - raw: 2E6D01000108210020C8, dni: 2E6D, endpoint: 01, cluster: 0001, size: 08, attrId: 0021, encoding: 20, value: ${i}"
		}
		for(int i =0; i<=90;i+=10){
			status "battery 0020 0x$i":"read attr - raw: C6590100010A200000201E, dni: C659, endpoint: 01, cluster: 0001, size: 0A, attrId: 0020, result: success, encoding: 20, value: 1e"
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
				attributeState "wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc"
			}
		}

		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "off", label:'off', action:'alarm.both', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
			state "strobe", label:'strobe!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
			state "siren", label:'siren!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
			state "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "water"
		details(["water", "alarm", "battery"])
	}
}

def parse(String description) {
	log.debug "description: $description"

	def result = [:]
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			ZoneStatus zs = zigbee.parseZoneStatus(description)
			map = zs.isAlarm1Set() ? getMoistureResult('wet') : getMoistureResult('dry')
			zs.isAlarm1Set()?sendEvent(name: "alarm", value: "siren"):sendEvent(name: "alarm", value: "off")
			result = createEvent(map)
		} else if(description?.startsWith('enroll request')){
			List cmds = zigbee.enrollResponse()
			log.debug "enroll response: ${cmds}"
			result = cmds?.collect { new physicalgraph.device.HubAction(it) }
		}else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = zs.isAlarm1Set() ? getMoistureResult('wet') : getMoistureResult('dry')
				result = createEvent(map)
			} else if (descMap?.clusterInt == 0x0001 && descMap?.commandInt != 0x07 && descMap?.value) {
				if(descMap?.attrInt == 0x0021){
					map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
					result = createEvent(map)
				}else if(descMap?.attrInt == 0x0020){
					map = getBatteryResult(Integer.parseInt(descMap.value, 16))
					result = createEvent(map)
				}
			}
		}
	}

	log.debug "Parse returned $result"

	result
}

def ping() {
	refresh()
}

def refresh() {
	log.debug "Refreshing Values"
	def refreshCmds = []
	//the device support two way to get the battery value
	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020)
	refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
		zigbee.enrollResponse()

	refreshCmds
}

def configure() {
	sendEvent(name: "checkInterval", value: 20 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"
	def configCmds = []
	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10)
	//	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020, DataType.UINT8, 30, 21600, 0x10)
	refresh() + configCmds // send refresh cmds as part of config
}

def getMoistureResult(value) {
	log.debug "water"
	def descriptionText
	if (value == "wet")
		descriptionText = "${device.displayName} is wet"
	else
		descriptionText = "${device.displayName} is dry"
	[
		name           : 'water',
		value          : value,
		descriptionText: descriptionText,
		translatable   : true
	]
}
private Map getBatteryPercentageResult(rawValue) {
	log.debug "Battery Percentage"
	def result = [:]

	if (0 <= rawValue && rawValue <= 200) {
		result.name = 'battery'
		result.translatable = true
		result.value = Math.round(rawValue / 2)
		result.descriptionText = "${device.displayName} battery was ${result.value}%"
	}

	log.debug "${device.displayName} battery was ${result.value}%"
	result
}

private Map getBatteryResult(rawValue) {
	log.debug 'Battery'
	def linkText = getLinkText(device)

	def result = [:]

	def volts = rawValue / 10
	if (!(rawValue == 0 || rawValue == 255)) {
		def minVolts = 2.1
		def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		def roundedPct = Math.round(pct * 100)
		if (roundedPct <= 0)
			roundedPct = 1
		result.value = Math.min(100, roundedPct)
		result.descriptionText = "${linkText} battery was ${result.value}%"
		result.name = 'battery'
	}
	log.debug "${device.displayName} battery was ${result.value}%"
	result
}
