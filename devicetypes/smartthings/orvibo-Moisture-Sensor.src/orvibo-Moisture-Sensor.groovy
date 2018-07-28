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
 *  Orvibo Moisture Sensor
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
		capability "Battery"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0001,0009", outClusters: "0019", manufacturer: "Heiman", model: "2f077707a13f4120846e0775df7e2efe"
	}

	simulator {

		status "dry": "zone status 0x0020 -- extended status 0x00"
		status "wet": "zone status 0x0021 -- extended status 0x00"

		for (int i = 0; i <= 90; i += 10) {
			status "battery 0021 0x${i}": "read attr - raw: 8C900100010A21000020C8, dni: 8C90, endpoint: 01, cluster: 0001, size: 0A, attrId: 0021, result: success, encoding: 20, value: ${i}"
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
				attributeState "wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc"
			}
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "water"
		details(["water", "battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "description: $description"

	def result
	Map map = zigbee.getEvent(description)

	if (!map) {
		if (description?.startsWith('zone status')) {
			map = getMoistureResult(description)
		} else if(description?.startsWith('enroll request')){
			List cmds = zigbee.enrollResponse()
			log.debug "enroll response: ${cmds}"
			result = cmds?.collect { new physicalgraph.device.HubAction(it) }
		}else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
				map = getMoistureResult(description)
			} else if (descMap?.clusterInt == 0x0001 && descMap?.attrInt == 0x0021 && descMap?.commandInt != 0x07 && descMap?.value) {
				map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
			}
		}
	}
	if(map&&!result){
		result = createEvent(map)
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
	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
	refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
		zigbee.enrollResponse()

	refreshCmds
}

def installed(){
	log.debug "call installed()"
	sendEvent(name: "checkInterval", value: 20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def configure() {
	sendEvent(name: "checkInterval", value: 20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"
	def configCmds = []
	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10)
	refresh() + configCmds
}

def getMoistureResult(description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	def value = zs?.isAlarm1Set()?"wet":"dry"
	[
		name           : 'water',
		value          : value,
		descriptionText: "${device.displayName} is $value",
		translatable   : true
	]
}

def getBatteryPercentageResult(rawValue) {
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