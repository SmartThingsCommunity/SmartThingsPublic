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
 *  Author : jinkang zhang / jk0218.zhang@samsung.com
 *  Date : 2018-07-04
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Orvibo Motion Sensor", namespace: "smartthings", author: "SmartThings", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-motion") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"

		command "enrollResponse"

		fingerprint inClusters: "0000,0001,0003,0500", outClusters: ""
	}

	simulator {
		status "active": "zone status 0x0001 -- extended status 0x00"
        for (int i = 0; i <= 100; i += 11) {
			status "battery ${i}%": "read attr - raw: 2E6D01000108210020C8, dni: 2E6D, endpoint: 01, cluster: 0001, size: 08, attrId: 0021, encoding: 20, value: ${i}"
		}
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
			input title: "Motion Timeout", description: "These devices don't report when motion stops, so it's necessary to have a timer to report that motion has stopped. You can adjust how long this is below.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
			input "motionStopTime", "number", title: "Seconds", range: "*..*", displayDuringSetup: false, defaultValue: 5
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC"
				attributeState "inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#cccccc"
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["motion"])
		details(["motion","battery", "refresh"])
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
	def value = description
	log.debug "description(): $description"
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
			//inactive
			def isActive = zigbee.translateStatusZoneType19(description)
			value = isActive ? "active" : "inactive"
			if (value == "active") {
				def timeout = 3
				if (motionStopTime)
					timeout = motionStopTime
				log.debug "Stopping motion in ${timeout} seconds"
				runIn(timeout, stopMotion)

			}
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
				log.info "BATT METRICS - attr: ${descMap?.attrInt}, value: ${descMap?.value}, decValue: ${Integer.parseInt(descMap.value, 16)}, currPercent: ${device.currentState("battery")?.value}, device: ${device.getDataValue("manufacturer")} ${device.getDataValue("model")}"
				List<Map> descMaps = collectAttributes(descMap)
                def battMap = descMaps.find { it.attrInt == 0x0021 }
                if (battMap) {
                    map = getBatteryPercentageResult(Integer.parseInt(battMap.value, 16))
                }
			} else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = translateZoneStatus(zs)
			}
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

def stopMotion() {
	log.debug "motion inactive"
	sendEvent(getMotionResult('inactive'))
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
		result.descriptionText = "${device.displayName} battery was ${value}%"
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

	return result
}

private Map getBatteryPercentageResult(rawValue) {
	log.debug "Battery Percentage rawValue = ${rawValue} -> ${rawValue / 2}%"
	def result = [:]

	if (0 <= rawValue && rawValue <= 200) {
		result.name = 'battery'
		result.translatable = true
		result.descriptionText = "${device.displayName} battery was ${value}%"
		result.value = Math.round(rawValue / 2)
	}

	return result
}

private Map getMotionResult(value) {
	String descriptionText = value == 'active' ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
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
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
		zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
		zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"
	def configCmds = []

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10)
	return refresh() + configCmds + refresh() // send refresh cmds as part of config
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
