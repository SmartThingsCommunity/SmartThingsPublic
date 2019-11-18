/**
 *  SmartSense Open/Closed Sensor
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
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "SmartSense Open/Closed Sensor", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Zigbee") {
		capability "Battery"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Refresh"
        capability "Temperature Measurement"
		capability "Health Check"
		capability "Sensor"

		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3300-S"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3300"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3320-L", deviceJoinName: "Iris Contact Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3323-G", deviceJoinName: "Centralite Micro Door Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "Contact Sensor-A", deviceJoinName: "Sylvania SMART+ Contact and Temperature Smart Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "Visonic", model: "MCT-340 E", deviceJoinName: "Visonic Door/Window Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "Ecolink", model: "4655BC0-R", deviceJoinName: "Ecolink Door/Window Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05,FC01,FC02", outClusters: "0003,0019", manufacturer: "iMagic by GreatStar", model: "1116-S", deviceJoinName: "Iris Contact Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "Bosch", model: "RFMS-ZBMS", deviceJoinName: "Bosch multi-sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "Megaman", model: "MS601/z1", deviceJoinName: "INGENIUM ZB Magnetic ON/OFF Sensor"
        //AduroSmart
        fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "AduroSmart Eria", model: "CSW_ADUROLIGHT", deviceJoinName: "ERIA Contact Sensor V2.1", mnmn: "SmartThings", vid: "generic-contact-3"
        fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "ADUROLIGHT", model: "CSW_ADUROLIGHT", deviceJoinName: "ERIA Contact Sensor V2.0", mnmn: "SmartThings", vid: "generic-contact-3"
	}

	simulator {

	}

	preferences {
		input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
				attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
			}
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label: '${currentValue}°',
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
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["contact", "temperature"])
		details(["contact", "temperature", "battery", "refresh"])
	}
}

private getIAS_ZONE_TYPE_ATTRIBUTE() { 0x0001 }
private getIAS_ZONE_TYPE_CONTACT_SWITCH_ATTRIBUTE_VALUE() { 0x0015 }
private getIAS_ZONE_TYPE_WATER_SENSOR_ATTRIBUTE_VALUE() { 0x002A }
private getTEMPERATURE_MEASURED_VALUE_ATTRIBUTE() { 0x0000 }
private getBATTERY_VOLTAGE_VALUE_ATTRIBUTE() { 0x0020 }
private getPOLL_CONTROL_CLUSTER() { 0x0020 }
private getCHECK_IN_INTERVAL_ATTRIBUTE() { 0x0000 }
private getFAST_POLL_TIMEOUT_ATTRIBUTE() { 0x0003 }
private getSET_LONG_POLL_INTERVAL_CMD() { 0x02 }
private getSET_SHORT_POLL_INTERVAL_CMD() { 0x03 }

def parse(String description) {
	log.debug "description: $description"

	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status') || description?.startsWith('zone report')) {
			map = parseIasMessage(description)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap?.value) {
				map = getBatteryResult(Integer.parseInt(descMap.value, 16))
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = getContactResult(zs.isAlarm1Set() ? "open" : "closed")
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.commandInt == 0x07) {
				if (descMap.data[0] == "00") {
					log.debug "IAS ZONE REPORTING CONFIG RESPONSE: $descMap"
					sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
				} else {
					log.warn "IAS ZONE REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
				}
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == IAS_ZONE_TYPE_ATTRIBUTE && isBoschRadionMultiSensor()) {
				if (Integer.parseInt(descMap.value, 16) == IAS_ZONE_TYPE_CONTACT_SWITCH_ATTRIBUTE_VALUE) {
					//multi-sensor is in contact or tilt detector mode - both act as contact sensor so no action is necessary
				} else if (Integer.parseInt(descMap.value, 16) == IAS_ZONE_TYPE_WATER_SENSOR_ATTRIBUTE_VALUE) {
					//multi-sensor is in water detector mode, DTH should be changed to water sensor DTH
					log.debug "Changing DTH type to: SmartSense Moisture Sensor"
					setDeviceType("SmartSense Moisture Sensor")
				}
			}
		}
	} else if (map.name == "temperature") {
		if (tempOffset) {
			map.value = (int) map.value + (int) tempOffset
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
	return zs.isAlarm1Set() ? getContactResult('open') : getContactResult('closed')
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

private Map getContactResult(value) {
	log.debug 'Contact Status'
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	return [
		name           : 'contact',
		value          : value,
		descriptionText: descriptionText
	]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def refresh() {
	log.debug "Refreshing Temperature and Battery"
	def refreshCmds = zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, TEMPERATURE_MEASURED_VALUE_ATTRIBUTE) +
		zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE_VALUE_ATTRIBUTE) + zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def cmds = refresh() +
		zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 30, 60 * 5, null) +
		zigbee.batteryConfig() +
		zigbee.temperatureConfig(30, 60 * 30) +
		zigbee.enrollResponse()
	if (isEcolink()) {
		cmds += configureEcolink()
	} else if (isBoschRadionMultiSensor()) {
		cmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, IAS_ZONE_TYPE_ATTRIBUTE)
	}
	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	return cmds
}

private configureEcolink() {
	sendEvent(name: "checkInterval", value: 60 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	def enrollCmds = zigbee.writeAttribute(POLL_CONTROL_CLUSTER, CHECK_IN_INTERVAL_ATTRIBUTE, DataType.UINT32, 0x00001C20) + zigbee.command(POLL_CONTROL_CLUSTER, SET_SHORT_POLL_INTERVAL_CMD, "0200") +
		zigbee.writeAttribute(POLL_CONTROL_CLUSTER, FAST_POLL_TIMEOUT_ATTRIBUTE, DataType.UINT16, 0x0028) + zigbee.command(POLL_CONTROL_CLUSTER, SET_LONG_POLL_INTERVAL_CMD, "B1040000")

	return zigbee.addBinding(POLL_CONTROL_CLUSTER) + refresh() + enrollCmds
}

private Boolean isEcolink() {
	device.getDataValue("manufacturer") == "Ecolink"
}

private Boolean isBoschRadionMultiSensor() {
	device.getDataValue("manufacturer") == "Bosch" && device.getDataValue("model") == "RFMS-ZBMS"
}