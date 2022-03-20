/*
 *  Copyright 2016 SmartThings
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
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "SmartSense Motion Sensor", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: true, mnmn: "SmartThings", vid: "generic-motion", genericHandler: "Zigbee") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Temperature Measurement"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"
		capability "Firmware Update"

		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3305-S", deviceJoinName: "Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3325-S", deviceJoinName: "Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3305", deviceJoinName: "Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3325", deviceJoinName: "Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3326", deviceJoinName: "Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3326-L", deviceJoinName: "Iris Motion Sensor" //Iris Motion Sensor
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3328-G", deviceJoinName: "Centralite Motion Sensor" //Centralite Micro Motion Sensor
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "Motion Sensor-A", deviceJoinName: "SYLVANIA Motion Sensor" //SYLVANIA SMART+ Motion and Temperature Sensor
		fingerprint inClusters: "0000,0001,0003,000F,0020,0402,0500", outClusters: "0019", manufacturer: "SmartThings", model: "motionv4", deviceJoinName: "Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor"
		fingerprint inClusters: "0000,0001,0003,000F,0020,0402,0500", outClusters: "0019", manufacturer: "SmartThings", model: "motionv5", deviceJoinName: "Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0400,0500,0B05", outClusters: "0019", manufacturer: "Bosch", model: "RFPR-ZB", deviceJoinName: "Bosch Motion Sensor" //Bosch Motion Sensor
		fingerprint inClusters: "0000,0001,0003,000F,0020,0402,0500", outClusters: "0019", manufacturer: "Bosch", model: "RFDL-ZB-MS", deviceJoinName: "Bosch Motion Sensor" //Bosch Motion Sensor
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500", outClusters: "0019", manufacturer: "Samjin", model: "motion", deviceJoinName: "Motion Sensor" // This is the only ST sensor that shouldn't use SmartThings-smartthings-SmartSense_Motion_Sensor
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "Ecolink", model: "PIRZB1-ECO", deviceJoinName: "Ecolink Motion Sensor" //Ecolink Motion Detector
		//AduroSmart
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0001,FFFF", manufacturer: "ADUROLIGHT", model: "VMS_ADUROLIGHT", deviceJoinName: "ERIA Motion Sensor", mnmn: "SmartThings", vid: "generic-motion-2" //ERIA Motion Sensor V2.0
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0001,FFFF", manufacturer: "AduroSmart Eria", model: "VMS_ADUROLIGHT", deviceJoinName: "ERIA Motion Sensor", mnmn: "SmartThings", vid: "generic-motion-2" //ERIA Motion Sensor V2.1
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,000F,0020,0500", outClusters: "000A,0019", manufacturer: "frient A/S", model :"MOSZB-140", deviceJoinName: "frient Motion Sensor"
		fingerprint manufacturer: "frient A/S", model :"MOSZB-141", deviceJoinName: "frient Motion Sensor", mnmn: "SmartThingsCommunity", vid: "87753fce-8cd6-3b91-8bde-2483e564252d" // Raw description: 22 0104 0107 00 03 0000 0003 0406 00
		//Smartenit
		fingerprint manufacturer: "Compacta", model: "ZBMS3-1", deviceJoinName: "Smartenit Motion Sensor", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Motion_Sensor" // Raw description: 01 0104 0402 00 07 0000 0001 0003 0015 0500 0020 0B05 00
	}

	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
					"http://cdn.device-gse.smartthings.com/Motion/Motion1.jpg",
					"http://cdn.device-gse.smartthings.com/Motion/Motion2.jpg",
					"http://cdn.device-gse.smartthings.com/Motion/Motion3.jpg"
			])
		}
		section {
			input "tempOffset", "number", title: "Temperature offset", description: "Select how many degrees to adjust the temperature.", range: "-100..100", displayDuringSetup: false
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
				attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
			}
		}
		valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label: '${currentValue}°', unit: "F",
					backgroundColors: [
							[value: 31, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
			)
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["motion", "temperature"])
		details(["motion", "temperature", "battery", "refresh"])
	}
}

private List<Map> collectAttributes(Map descMap) {
	List<Map> descMaps = new ArrayList<Map>()

	descMaps.add(descMap)

	if (descMap.additionalAttrs) {
		descMaps.addAll(descMap.additionalAttrs)
	}

	return  descMaps
}

def parse(String description) {
	log.debug "description: $description"
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)

			if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
				log.info "BATT METRICS - attr: ${descMap?.attrInt}, value: ${descMap?.value}, decValue: ${Integer.parseInt(descMap.value, 16)}, currPercent: ${device.currentState("battery")?.value}, device: ${device.getDataValue("manufacturer")} ${device.getDataValue("model")}"
				List<Map> descMaps = collectAttributes(descMap)

				if (device.getDataValue("manufacturer") == "Samjin") {
					def battMap = descMaps.find { it.attrInt == 0x0021 }

					if (battMap) {
						map = getBatteryPercentageResultSamjin(Integer.parseInt(battMap.value, 16))
					}
				} else {
					def battMap = descMaps.find { it.attrInt == 0x0020 }

					if (battMap) {
						map = getBatteryResult(Integer.parseInt(battMap.value, 16))
					}
				}
			} else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002 && descMap.commandInt != 0x07 && descMap.value != null) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = translateZoneStatus(zs)
			} else if (descMap?.clusterInt == zigbee.TEMPERATURE_MEASUREMENT_CLUSTER && descMap.commandInt == 0x07) {
				if (descMap.data[0] == "00") {
					log.debug "TEMP REPORTING CONFIG RESPONSE: $descMap"
					sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
				} else {
					log.warn "TEMP REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
				}
			} else if (descMap.clusterInt == 0x0406 && descMap.attrInt == 0x0000) {
				def value = descMap.value.endsWith("01") ? "active" : "inactive"
				log.debug "Doing a read attr motion event"
				map = getMotionResult(value)
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap?.value) {
				map = translateZoneStatus(new ZoneStatus(zigbee.convertToInt(descMap?.value)))
			}
		}
	} else if (map.name == "temperature") {
		if (tempOffset) {
			map.value = new BigDecimal((map.value as float) + (tempOffset as float)).setScale(1, BigDecimal.ROUND_HALF_UP)
		}
		map.descriptionText = temperatureScale == 'C' ? '{{ device.displayName }} was {{ value }}°C' : '{{ device.displayName }} was {{ value }}°F'
		map.translatable = true
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

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	// Some sensor models that use this DTH use alarm1 and some use alarm2 to signify motion
	return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getMotionResult('active') : getMotionResult('inactive')
}

private Map getBatteryResult(rawValue) {
	log.debug "Battery rawValue = ${rawValue}"
	def linkText = getLinkText(device)

	def result = [:]

	def volts = rawValue / 10

	if (!(rawValue == 0 || rawValue == 255)) {
		result.name = 'battery'
		result.translatable = true
		result.descriptionText = "{{ device.displayName }} battery was {{ value }}%"
		if (device.getDataValue("manufacturer") == "SmartThings") {
			volts = rawValue // For the batteryMap to work the key needs to be an int
			def batteryMap = [28: 100, 27: 100, 26: 100, 25: 90, 24: 90, 23: 70,
							  22: 70, 21: 50, 20: 50, 19: 30, 18: 30, 17: 15, 16: 1, 15: 0]
			def minVolts = 15
			def maxVolts = 28

			if (volts < minVolts)
				volts = minVolts
			else if (volts > maxVolts)
				volts = maxVolts
			def pct = batteryMap[volts]
			result.value = pct
		} else if (device.getDataValue("manufacturer") == "Bosch") {
			def minValue = 21
			def maxValue = 30
			def pct = Math.round((rawValue - minValue) * 100 / (maxValue - minValue))
			pct = pct > 0 ? pct : 1
			result.value = Math.min(100, pct)
		} else if (isFrientSensor()) {
			def minValue = 23
			def maxValue = 30
			def pct = Math.round((rawValue - minValue) * 100 / (maxValue - minValue))
			pct = pct > 0 ? pct : 1
			result.value = Math.min(100, pct)
		} else { // Centralite
			def useOldBatt = shouldUseOldBatteryReporting()
			def minVolts = useOldBatt ? 2.1 : 2.4
			def maxVolts = useOldBatt ? 3.0 : 2.7
			// Get the current battery percentage as a multiplier 0 - 1
			def curValVolts = Integer.parseInt(device.currentState("battery")?.value ?: "100") / 100.0
			// Find the corresponding voltage from our range
			curValVolts = curValVolts * (maxVolts - minVolts) + minVolts
			// Round to the nearest 10th of a volt
			curValVolts = Math.round(10 * curValVolts) / 10.0
			// Only update the battery reading if we don't have a last reading,
			// OR we have received the same reading twice in a row
			// OR we don't currently have a battery reading
			// OR the value we just received is at least 2 steps off from the last reported value
			// OR the device's firmware is older than 1.15.7
			if (useOldBatt || state?.lastVolts == null || state?.lastVolts == volts || device.currentState("battery")?.value == null || Math.abs(curValVolts - volts) > 0.1) {
				def pct = (volts - minVolts) / (maxVolts - minVolts)
				def roundedPct = Math.round(pct * 100)
				if (roundedPct <= 0)
					roundedPct = 1
				result.value = Math.min(100, roundedPct)
			} else {
				// Don't update as we want to smooth the battery values, but do report the last battery state for record keeping purposes
				result.value = device.currentState("battery").value
			}
			state.lastVolts = volts
		}
	}

	return result
}

private Map getBatteryPercentageResultSamjin(rawValue) {
	// This formula was provided by Samjin to effectively adjust the minimum voltage required for operation from 2.1V -> 2.4V
	BigDecimal rawPercentage = rawValue - (200 - rawValue) / 2
	Integer percentage = Math.min(100, Math.max(Math.round(rawPercentage / 2), 0))

	log.debug "Battery Percentage rawValue = ${rawValue} -> ${percentage}%"
	return [name: 'battery',
			translatable: true,
			descriptionText: "{{ device.displayName }} battery was {{ value }}%",
			value: percentage]
}

private Map getMotionResult(value) {
	log.debug 'motion'
	String descriptionText = value == 'active' ? "{{ device.displayName }} detected motion" : "{{ device.displayName }} motion has stopped"
	return [
			name           : 'motion',
			value          : value,
			descriptionText: descriptionText,
			translatable   : true
	]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def refresh() {
	log.debug "Refreshing Values"
	def refreshCmds = []

	if (device.getDataValue("manufacturer") == "Samjin") {
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
	} else {
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020)
	}
	refreshCmds += zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) +
		zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
		zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	// Sets up low battery threshold reporting
	sendEvent(name: "DeviceWatch-Enroll", displayed: false, value: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, scheme: "TRACKED", checkInterval: 2 * 60 * 60 + 1 * 60, lowBatteryThresholds: [15, 7, 3], offlinePingable: "1"].encodeAsJSON())

	log.debug "Configuring Reporting"
	def configCmds = [zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000)]
	def batteryAttr = device.getDataValue("manufacturer") == "Samjin" ? 0x0021 : 0x0020

	configCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, batteryAttr)

	configCmds += zigbee.enrollResponse()
	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	if (device.getDataValue("manufacturer") == "Samjin") {
		configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10)
	} else if (isFrientSensor()) {
		configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020, DataType.UINT8, 30, 21600, 0x1, [destEndpoint: 0x23])
	} else {
		configCmds += zigbee.batteryConfig()
	}
    
	if (isFrientSensor()) {
		configCmds += zigbee.configureReporting(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000, DataType.INT16, 30, 300, 0x64, [destEndpoint: 0x26])
	} else if (isCompactaSensor()) {
		configCmds += zigbee.configureReporting(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000, DataType.INT16, 30, 300, 0x64, [destEndpoint: 0x0003])
	} else {
		configCmds += zigbee.temperatureConfig(30, 300)
	}
	configCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
	configCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, batteryAttr)

	return configCmds
}

private shouldUseOldBatteryReporting() {
	def isFwVersionLess = true // By default use the old battery reporting
	def deviceFwVer = "${device.getFirmwareVersion()}"
	def deviceVersion = deviceFwVer.tokenize('.')  // We expect the format ###.###.### where ### is some integer

	if (deviceVersion.size() == 3) {
		def targetVersion = [1, 15, 7] // Centralite Firmware 1.15.7 contains battery smoothing fixes, so versions before that should NOT be smoothed
		def devMajor = deviceVersion[0] as int
		def devMinor = deviceVersion[1] as int
		def devBuild = deviceVersion[2] as int

		isFwVersionLess = ((devMajor < targetVersion[0]) ||
			(devMajor == targetVersion[0] && devMinor < targetVersion[1]) ||
			(devMajor == targetVersion[0] && devMinor == targetVersion[1] && devBuild < targetVersion[2]))
	}

	return isFwVersionLess // If f/w version is less than 1.15.7 then do NOT smooth battery reports and use the old reporting
}

private Boolean isFrientSensor() {
	device.getDataValue("manufacturer") == "frient A/S"
}

private Boolean isCompactaSensor() {
	device.getDataValue("manufacturer") == "Compacta"
}