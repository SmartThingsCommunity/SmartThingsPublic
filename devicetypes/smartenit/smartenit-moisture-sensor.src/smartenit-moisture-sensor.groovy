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
	definition(name: "Smartenit Moisture Sensor", namespace: "Smartenit", author: "Luis Contreras") {
		capability "Battery"
		capability "Configuration"
		capability "Refresh"
		capability "Water Sensor"
		capability "Health Check"
		capability "Sensor"

		fingerprint manufacturer: "Compacta", model: "ZBLIQS", deviceJoinName: "Smartenit Moisture Sensor"
	}
}

def getBATTERY_VOLTAGE_ATTR() { 0x0020 }

def installed() {
	log.debug "Installed"
	createChildDevices()
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

	// getEvent will handle temperature and humidity
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			map = parseIasMessage(description)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)

			if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap?.value) {
				map = getBatteryResult(Integer.parseInt(descMap.value, 16))
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS) {
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

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	if (zs.isAlarm2Set()) {
		setChildMoistureState("wet")
	} else {
		setChildMoistureState("dry")
	}
	return zs.isAlarm1Set() ? getMoistureResult('wet') : getMoistureResult('dry')
}

private setChildMoistureState(value) {
	def childDevice = childDevices.find {
		it.deviceNetworkId == "$device.deviceNetworkId:02" || it.deviceNetworkId == "$device.deviceNetworkId:02"
	}

	if (childDevice) {
		def map = createEvent(name: "water", value: value, translatable: true)
		childDevice.sendEvent(map)
	}
}

private Map getMoistureResult(value) {
	log.debug "water"
	def descriptionText
	if (value == "wet") {
		descriptionText = '{{ device.displayName }} is wet'
	} else {
		descriptionText = '{{ device.displayName }} is dry'
	}
	return [
			name           : 'water',
			value          : value,
			descriptionText: descriptionText,
			translatable   : true
	]
}

private Map getBatteryResult(rawValue) {
	log.debug "Battery ${rawValue}"
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
	refresh()
}

def refresh() {
	log.debug "Refreshing Values"

	return zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) +
		zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE_ATTR) +
		zigbee.enrollResponse()
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"
	return refresh() + zigbee.batteryConfig()
}

private void createChildDevices() {
	try {
		addChildDevice("Smartenit", "Moisture Sensor Child", "${device.deviceNetworkId}:02", device.hubId,
			[completedSetup: true,
			label: "${device.displayName} 2",
			isComponent: false
			])
	} catch (Exception e) {
		log.debug "Exception creating child device: ${e}"
	}
}