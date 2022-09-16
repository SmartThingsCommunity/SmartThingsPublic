/*
 *  Copyright 2022 SmartThings
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
	definition (name: "SiHAS Dual Motion Sensor", namespace: "shinasys", author: "SHINA SYSTEM", mnmn: "SmartThingsCommunity", vid: "868a0fcc-ae46-3a1b-9315-e342007bb3a9", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Motion Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "afterguide46998.dualMotionInSensor"
		capability "afterguide46998.dualMotionOutSensor"
		
		attribute "motionInterval","number"
		
		fingerprint inClusters: "0000,0001,0003,0020,0406,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "DMS-300Z", deviceJoinName: "SiHAS Dual Motion Sensor"
	}
	preferences {
		section {
			input "motionInterval", "number", title: "Motion Interval", description: "What is the re-sensing time (seconds) after the motion sensor is detected.", range: "1..100", defaultValue: 5, required: true, displayDuringSetup: true
		}
	}
}

private getOCCUPANCY_SENSING_CLUSTER() { 0x0406 }
private getPOWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE() { 0x0020 }
private getOCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE() { 0x0000 }
private getOCCUPIED_TO_UNOCCUPIED_DELAY_ATTRIBUTE() { 0x0010 }

private List<Map> collectAttributes(Map descMap) {
	List<Map> descMaps = new ArrayList<Map>()
	descMaps.add(descMap)
	if (descMap.additionalAttrs) {
		descMaps.addAll(descMap.additionalAttrs)
	}
	return  descMaps
}

def parse(String description) {
	log.debug "Parsing message from device: $description"

	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
				List<Map> descMaps = collectAttributes(descMap)
				def battMap = descMaps.find { it.attrInt == POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE }
				if (battMap) {
					map = getBatteryResult(Integer.parseInt(battMap.value, 16))
				}
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS && descMap.commandInt != 0x07) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 10))
				map = translateZoneStatus(zs)
			} else if (descMap?.clusterInt == OCCUPANCY_SENSING_CLUSTER && descMap.attrInt == OCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE && descMap?.value) {
				def inMotion = descMap.value == "01" ? "active" : "inactive"
				def outMotion  = device.latestState('motionOut')?.value
				sendDualMotionResult("motionIn", inMotion)
				map = (inMotion == "active" || outMotion == "active") ? getMotionResult('active') : getMotionResult('inactive')
			} else if (descMap?.clusterInt == OCCUPANCY_SENSING_CLUSTER && descMap.attrInt == OCCUPIED_TO_UNOCCUPIED_DELAY_ATTRIBUTE && descMap?.value) {
				def interval = zigbee.convertToInt(descMap.value, 10)
				log.debug "interval = [$interval]"
				map = [name:'motionInterval',value: interval]
			}
		}
	}

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	log.debug "result: $result"
	return result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	def inMotion  = device.latestState('motionIn')?.value
	def outMotion = (zs.isAlarm1Set() || zs.isAlarm2Set()) ? "active" : "inactive"
	sendDualMotionResult("motionOut", outMotion)
	return (inMotion == "active" || outMotion == "active") ? getMotionResult('active') : getMotionResult('inactive')
}

private Map getBatteryResult(rawValue) {
	def linkText = getLinkText(device)
	def result = [:]
	def volts = rawValue / 10

	if (!(rawValue == 0 || rawValue == 255)) {
		result.name = 'battery'
		result.translatable = true
		def minVolts = 2.2
		def maxVolts = 3.1
		
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		def roundedPct = Math.round(pct * 100)
		if (roundedPct <= 0)
			roundedPct = 1
		result.value = Math.min(100, roundedPct)
		result.descriptionText = "${device.displayName} battery was ${result.value}%"        
	}
	return result
}

private sendDualMotionResult(name, value) {
	String descriptionText = value == 'active' ? "${device.displayName} ${name} detected motion" : "${device.displayName}  ${name} has stopped"
	log.debug "$name = $value: $descriptionText"
	
	sendEvent(name: name, value: value, descriptionText: descriptionText,translatable   : true)    
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
	zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
}

def updated() {
	log.debug "device updated $motionInterval"
	
	//set reportingInterval = 0 to trigger update
	if (isMotionIntervalChange()) {
		sendEvent(name: "motionInterval", value: getMotionReportInterval(), descriptionText: "Motion interval set to ${getMotionReportInterval()} seconds")
		sendHubCommand(zigbee.writeAttribute(OCCUPANCY_SENSING_CLUSTER, OCCUPIED_TO_UNOCCUPIED_DELAY_ATTRIBUTE, DataType.UINT16, getMotionReportInterval()), 1)
	}
}

//has interval been updated
def isMotionIntervalChange() {
	log.debug "isMotionIntervalChange ${getMotionReportInterval()} <- ${device.latestValue("motionInterval")}"
	return (getMotionReportInterval() != device.latestValue("motionInterval"))
}

//settings default interval
def getMotionReportInterval() {
	return (motionInterval != null ? motionInterval : 5)
}

def refresh() {
	def refreshCmds = []

	refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)

	refreshCmds += zigbee.readAttribute(OCCUPANCY_SENSING_CLUSTER, OCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE)
	refreshCmds += zigbee.readAttribute(OCCUPANCY_SENSING_CLUSTER, OCCUPIED_TO_UNOCCUPIED_DELAY_ATTRIBUTE)
	refreshCmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)        
	refreshCmds += zigbee.enrollResponse()
	return refreshCmds
}

def configure() {
	def configCmds = []

	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	
	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	// humidity minReportTime 30 seconds, maxReportTime 60 min
	// illuminance minReportTime 30 seconds, maxReportTime 60 min
	// occupancy sensing minReportTime 10 seconds, maxReportTime 60 min
	// ex) zigbee.configureReporting(0x0001, 0x0020, DataType.UINT8, 600, 21600, 0x01)
	// This is for cluster 0x0001 (power cluster), attribute 0x0021 (battery level), whose type is UINT8,
	// the minimum time between reports is 10 minutes (600 seconds) and the maximum time between reports is 6 hours (21600 seconds),
	// and the amount of change needed to trigger a report is 1 unit (0x01).
	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/)

	configCmds += zigbee.configureReporting(OCCUPANCY_SENSING_CLUSTER, OCCUPANCY_SENSING_OCCUPANCY_ATTRIBUTE, DataType.BITMAP8, 1, 600, 1)
	configCmds += zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 0, 0xffff, null)
	return configCmds + refresh()
}
