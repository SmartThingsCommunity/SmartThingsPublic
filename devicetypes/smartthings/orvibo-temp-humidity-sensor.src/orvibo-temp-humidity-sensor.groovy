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
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Orvibo Temperature&Humidity Sensor",namespace: "smartthings", author: "SmartThings", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-humidity") {
		capability "Configuration"
		capability "Sensor"
		capability "Relative Humidity Measurement"
                  capability "Temperature Measurement"
		capability "Refresh"
		capability "Health Check"
		capability "Battery"
		fingerprint profileId: "0104",deviceId: "0302", inClusters: "0000,0001,0003,0402", model: "b467083cfc864f5e826459e5d8ea6079"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label: '${currentValue}F',
						backgroundColors: [
								[value: 31, color: "#153591"],
								[value: 44, color: "#1e9cbb"],
								[value: 59, color: "#90d2a7"],
								[value: 74, color: "#44b621"],
								[value: 84, color: "#f1d801"],
								[value: 95, color: "#d04e00"],
								[value: 96, color: "#bc2323"]
						]
			}
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label: '${currentValue}% humidity', unit: ""
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "temperature"
		details(["temperature", "humidity","battery", "refresh"])
	}
}

def installed() {
 	refresh()
}

def parse(String description) {
	log.debug "description: $description"

	Map map = zigbee.getEvent(description)
	if (!map) {
		Map descMap = zigbee.parseDescriptionAsMap(description)
		if (descMap.clusterInt == 0x0001 && descMap.commandInt != 0x07 && descMap?.value) {
			map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
		} else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
			if (descMap.data[0] == "00") {
				log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
			}
		}
	}else if (map.name == "temperature") {
		map.descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C' : '{{ device.displayName }} was {{ value }}°F'
		map.translatable = true
	}

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	log.debug "${map}"
	return result
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

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	def refreshCmds = []
	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021,[destEndpoint: 0x01])+
	zigbee.readAttribute(0x0402, 0x0000,[destEndpoint: 0x01])+
	zigbee.readAttribute(0x0405, 0x0000, [destEndpoint: 0x02])
	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value:20 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"

	def configCmds = []
	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10) +
	zigbee.temperatureConfig(30, 300) +
	zigbee.configureReporting(0x0405, 0x0000, DataType.UINT16, 30, 3600, 100,[destEndpoint: 0x02])

	return configCmds
}

