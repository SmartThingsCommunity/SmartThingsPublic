/**
 *  SmartSense Temp/Humidity Sensor
 *
 *  Copyright 2014 SmartThings
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
	definition(name: "SmartSense Temp/Humidity Sensor", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, ocfDeviceType: "oic.d.thermostat") {
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Health Check"
		capability "Sensor"

		fingerprint profileId: "0104", inClusters: "0001,0003,0020,0402,0B05,FC45", outClusters: "0019,0003", manufacturer: "CentraLite", model: "3310-S", deviceJoinName: "SmartSense Temp & Humidity Sensor"
		fingerprint profileId: "0104", inClusters: "0001,0003,0020,0402,0B05,FC45", outClusters: "0019,0003", manufacturer: "CentraLite", model: "3310-G", deviceJoinName: "Centralite Temp & Humidity Sensor"
		fingerprint profileId: "0104", inClusters: "0001,0003,0020,0402,0B05,FC45", outClusters: "0019,0003", manufacturer: "CentraLite", model: "3310", deviceJoinName: "Temp & Humidity Sensor"
		fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0402", manufacturer: "Heiman", model: "b467083cfc864f5e826459e5d8ea6079", deviceJoinName: "Orvibo Temperature & Humidity Sensor"
		fingerprint profileId: "0104", deviceId: "0302", inClusters: "0000,0001,0003,0402", manufacturer: "HEIMAN", model: "888a434f3cfc47f29ec4a3a03e9fc442", deviceJoinName: "Orvibo Temperature & Humidity Sensor"
		fingerprint profileId: "0104",  inClusters: "0000, 0001, 0003, 0009, 0402", manufacturer: "HEIMAN", model: "HT-EM", deviceJoinName: "HEIMAN Temperature & Humidity Sensor"
	}

	simulator {
		status 'H 40': 'catchall: 0104 FC45 01 01 0140 00 D9B9 00 04 C2DF 0A 01 000021780F'
		status 'H 45': 'catchall: 0104 FC45 01 01 0140 00 D9B9 00 04 C2DF 0A 01 0000218911'
		status 'H 57': 'catchall: 0104 FC45 01 01 0140 00 4E55 00 04 C2DF 0A 01 0000211316'
		status 'H 53': 'catchall: 0104 FC45 01 01 0140 00 20CD 00 04 C2DF 0A 01 0000219814'
		status 'H 43': 'read attr - raw: BF7601FC450C00000021A410, dni: BF76, endpoint: 01, cluster: FC45, size: 0C, attrId: 0000, result: success, encoding: 21, value: 10a4'
	}

	preferences {
		input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		input "humidityOffset", "number", title: "Humidity Offset", description: "Adjust humidity by this percentage", range: "*..*", displayDuringSetup: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label: '${currentValue}°',
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

		main "temperature", "humidity"
		details(["temperature", "humidity", "battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "description: $description"

	// getEvent will handle temperature and humidity
	Map map = zigbee.getEvent(description)
	if (!map) {
		Map descMap = zigbee.parseDescriptionAsMap(description)
		if (descMap.clusterInt == 0x0001 && descMap.commandInt != 0x07 && descMap?.value) {
			if (descMap.attrInt == 0x0021) {
            			map = getBatteryPercentageResult(Integer.parseInt(descMap.value,16))
			} else {
				map = getBatteryResult(Integer.parseInt(descMap.value, 16))
            		}
		} else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
			if (descMap.data[0] == "00") {
				log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
				sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
			}
		}
	} else if (map.name == "temperature") {
		if (tempOffset) {
			map.value = (int) map.value + (int) tempOffset
		}
		map.descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C' : '{{ device.displayName }} was {{ value }}°F'
		map.translatable = true
	} else if (map.name == "humidity") {
		if (humidityOffset) {
			map.value = (int) map.value + (int) humidityOffset
		}
	}

	log.debug "Parse returned $map"
	return map ? createEvent(map) : [:]
}


def getBatteryPercentageResult(rawValue) {
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

	return result
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.readAttribute(0x0001, 0x0020) // Read the Battery Level
}

def refresh() {
	log.debug "refresh temperature, humidity, and battery"

	def manufacturer = device.getDataValue("manufacturer")

	if (manufacturer == "Heiman"|| manufacturer == "HEIMAN") {
		return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, [destEndpoint: 0x01])+
		        zigbee.readAttribute(0x0402, 0x0000, [destEndpoint: 0x01])+
		        zigbee.readAttribute(0x0405, 0x0000, [destEndpoint: 0x02])
	} else {
		return zigbee.readAttribute(0xFC45, 0x0000, ["mfgCode": 0x104E]) +   // New firmware
		        zigbee.readAttribute(0xFC45, 0x0000, ["mfgCode": 0xC2DF]) +   // Original firmware
		        zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) +
		        zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020)
	}
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	log.debug "Configuring Reporting and Bindings."

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	def manufacturer = device.getDataValue("manufacturer")
	if (manufacturer == "Heiman"|| manufacturer == "HEIMAN") {
		return refresh() +
		        zigbee.temperatureConfig(30, 300) +
		        zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10) +
		        zigbee.configureReporting(0x0405, 0x0000, DataType.UINT16, 30, 3600, 100, [destEndpoint: 0x02])
	} else {
		return refresh() +
		        zigbee.configureReporting(0xFC45, 0x0000, DataType.UINT16, 30, 3600, 100, ["mfgCode": 0x104E]) +   // New firmware
		        zigbee.configureReporting(0xFC45, 0x0000, DataType.UINT16, 30, 3600, 100, ["mfgCode": 0xC2DF]) +   // Original firmware
		        zigbee.batteryConfig() +
		        zigbee.temperatureConfig(30, 300)
	}
}
