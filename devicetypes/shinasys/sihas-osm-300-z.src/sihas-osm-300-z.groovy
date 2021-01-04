/*
 *	Copyright 2018 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *	use this file except in compliance with the License. You may obtain a copy
 *	of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *	License for the specific language governing permissions and limitations
 *	under the License.
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "SiHAS OSM-300-Z", namespace: "shinasys", author: "Shina System Co., Ltd", cstHandler: true, vid: "generic-motion-2") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"
		
		fingerprint inClusters: "0000,0001,0003,0004,0020,0406,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "OSM-300Z", deviceJoinName: "SiHAS Motion Sensor"
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
		details(["motion", "battery", "refresh"])
	}
}

private List<Map> collectAttributes(Map descMap) {
	List<Map> descMaps = new ArrayList<Map>()

	descMaps.add(descMap)

	if (descMap.additionalAttrs) {
		descMaps.addAll(descMap.additionalAttrs)
	}

	return	descMaps
}

def parse(String description) {
	log.debug "description: $description"
	Map map = zigbee.getEvent(description)
	
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} else if (description?.startsWith('read attr')) {
			Map descMap = zigbee.parseDescriptionAsMap(description)

			if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
				//log.info "BATT METRICS - attr: ${descMap?.attrInt}, value: ${descMap?.value}, decValue: ${Integer.parseInt(descMap.value, 16)}, currPercent: ${device.currentState("battery")?.value}, device: ${device.getDataValue("manufacturer")} ${device.getDataValue("model")}"
				List<Map> descMaps = collectAttributes(descMap)
				def battMap = descMaps.find { it.attrInt == 0x0020 }

				if (battMap) {
					map = getBatteryResult(Integer.parseInt(battMap.value, 16))
				}
			} else if (descMap?.clusterInt == 0x0500 && descMap.attrInt == 0x0002 && descMap.commandInt != 0x07) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = translateZoneStatus(zs)
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap?.value) {
				map = translateZoneStatus(new ZoneStatus(zigbee.convertToInt(descMap?.value)))
			} else if (descMap?.clusterInt == 0x0406 && descMap.attrInt == 0 /*occupancy*/ && descMap?.value) {
				map = getMotionResult(descMap.value == "01" ? "active" : "inactive")
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


private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	// Some sensor models that use this DTH use alarm1 and some use alarm2 to signify motion
	return (zs.isAlarm1Set() || zs.isAlarm2Set()) ? getMotionResult('active') : getMotionResult('inactive')
}

private Map getBatteryResult(rawValue) {
	//log.debug "Battery rawValue = ${rawValue}"
	def linkText = getLinkText(device)

	def result = [:]

	def volts = rawValue / 10

	if (!(rawValue == 0 || rawValue == 255)) {
		result.name = 'battery'
		result.translatable = true
		def minVolts =	2.5
		def maxVolts =	3.1
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
		if (state?.lastVolts == null || state?.lastVolts == volts || device.currentState("battery")?.value == null || Math.abs(curValVolts - volts) > 0.1) {
			def pct = (volts - minVolts) / (maxVolts - minVolts)
			def roundedPct = Math.round(pct * 100)
			if (roundedPct <= 0)
				roundedPct = 1
			result.value = Math.min(100, roundedPct)
		} else {
			// Don't update as we want to smooth the battery values, but do report the last battery state for record keeping purposes
			result.value = device.currentState("battery").value
		}
		result.descriptionText = "${device.displayName} battery was ${result.value}%"
		state.lastVolts = volts
	}

	return result
}

private Map getMotionResult(value) {
	//log.debug 'motion'
	String descriptionText = value == 'active' ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped"
	return [
		name		   : 'motion',
		value		   : value,
		descriptionText: descriptionText,
		translatable   : true
	]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(0x0406/*occupancy sensing*/, 0x0000)
}

def refresh() {
	log.debug "Refreshing Values"
	def refreshCmds = []

	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
		zigbee.readAttribute(0x0406/*occupancy sensing*/, 0x0000) +
		zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
		zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"
	
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	// occupancy sensing minReportTime 1 seconds, maxReportTime 10 min
	def configCmds = []
	
	//ex) zigbee.configureReporting(0x0001, 0x0020, DataType.UINT8, 600, 21600, 0x01)
	//This is for cluster 0x0001 (power cluster), attribute 0x0021 (battery level), whose type is UINT8, 
	// the minimum time between reports is 10 minutes (600 seconds) and the maximum time between reports is 6 hours (21600 seconds), 
	// and the amount of change needed to trigger a report is 1 unit (0x01).
	
	configCmds += 
		zigbee.configureReporting(0x0001/*power*/, 0x0020, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/) +
		zigbee.configureReporting(0x0406/*occupancy sensing*/, 0x0000, 0x18/*bitmap8*/, 1, 600, 1)

	return refresh() + configCmds
}