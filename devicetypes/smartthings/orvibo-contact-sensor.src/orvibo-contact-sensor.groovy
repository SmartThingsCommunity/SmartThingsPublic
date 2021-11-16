/**
 *  Copyright 2018 SmartThings
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
 *
 *  Orvibo Contact Sensor
 *
 *  Author: Deng Biaoyi
 *
 *  Date:2018-07-03
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Orvibo Contact Sensor", namespace: "smartthings", author: "biaoyi.deng@samsung.com", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn:"SmartThings", vid:"generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Battery"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"
		
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0500", outClusters: "0003", manufacturer: "eWeLink", model: "DS01", deviceJoinName: "eWeLink Open/Closed Sensor" //eWeLink Door Sensor
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0001,0003,0020,0500,FC57", outClusters: "0003,0019", manufacturer: "eWeLink", model: "SNZB-04P", deviceJoinName: "eWeLink Open/Closed Sensor" //eWeLink Door Sensor
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0001", manufacturer: "ORVIBO", model: "e70f96b3773a4c9283c6862dbafb6a99", deviceJoinName: "Orvibo Open/Closed Sensor"
		fingerprint inClusters: "0000,0001,0003,000F,0020,0500", outClusters: "000A,0019", manufacturer: "Aurora", model: "WindowSensor51AU", deviceJoinName: "Aurora Open/Closed Sensor" //Aurora Smart Door/Window Sensor
		fingerprint manufacturer: "Aurora", model: "DoorSensor50AU", deviceJoinName: "Aurora Open/Closed Sensor" // Raw Description: 01 0104 0402 00 06 0000 0001 0003 0020 0500 0B05 01 0019 //Aurora Smart Door/Window Sensor
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0001", manufacturer: "HEIMAN", model: "DoorSensor-N", deviceJoinName: "HEIMAN Open/Closed Sensor" //HEIMAN Door Sensor
		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000, 0001, 0500", outClusters: "0019", manufacturer: "Third Reality, Inc", model: "3RDS17BZ", deviceJoinName: "ThirdReality Door Sensor" //ThirdReality Door Sensor
	}

	simulator {

		status "open": "zone status 0x0021 -- extended status 0x00"
		status "close": "zone status 0x0000 -- extended status 0x00"

		for (int i = 0; i <= 90; i += 10) {
			status "battery 0x${i}": "read attr - raw: 2E6D01000108210020C8, dni: 2E6D, endpoint: 01, cluster: 0001, size: 08, attrId: 0021, encoding: 20, value: ${i}"
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
				attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
			}
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["contact"])
		details(["contact", "battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "description: $description"

	def result = [:]
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			ZoneStatus zs = zigbee.parseZoneStatus(description)
			map = zs.isAlarm1Set() ? getContactResult('open') : getContactResult('closed')
			result = createEvent(map)
		} else if (description?.startsWith('enroll request')) {
			List cmds = zigbee.enrollResponse()
			log.debug "enroll response: ${cmds}"
			result = cmds?.collect { new physicalgraph.device.HubAction(it) }
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == 0x0001 && descMap?.commandInt != 0x07 && descMap?.value) {
				if (descMap?.attrInt==0x0021) {
					map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
				} else {
					map = getBatteryResult(Integer.parseInt(descMap.value, 16))
				}
				result = createEvent(map)
			} else if (descMap?.clusterInt == 0x0500 && descMap?.attrInt == 0x0002) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = getContactResult(zs.isAlarm1Set() ? "open" : "closed")
				result = createEvent(map)
			}
		}
	}else{
		result = createEvent(map)
	}
	log.debug "Parse returned $result"

	result
}

def installed() {
	log.debug "call installed()"
	def manufacturer = getDataValue("manufacturer")
	
	if (manufacturer == "Third Reality, Inc") {
		//ThirdReality Door Sensor do not set checkInterval for power-saving.
	} else {
		sendEvent(name: "checkInterval", value:20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	}
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping is called"
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def refresh() {
	log.debug "Refreshing  Battery and ZONE Status"
	def manufacturer = getDataValue("manufacturer")
	def refreshCmds =  zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
	if (manufacturer == "ORVIBO" || manufacturer == "eWeLink" || manufacturer == "HEIMAN" ) {
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
	} else { // this is actually just supposed to be for Aurora, but we'll make it the default as it's more widely supported
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020)
	}
	refreshCmds + zigbee.enrollResponse()
}

def configure() {
	def manufacturer = getDataValue("manufacturer")
	
	if (manufacturer == "Third Reality, Inc") {
		//ThirdReality Door Sensor do not set checkInterval for power-saving.
	} else if (manufacturer == "eWeLink") {
		sendEvent(name: "checkInterval", value:2 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	} else {
		sendEvent(name: "checkInterval", value:20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	}
	def cmds = []
	
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	//The electricity attribute is reported without bind and reporting CFG. The TI plan reports the power once in about 10 minutes; the NXP plan reports the electricity once in 20 minutes
	if (manufacturer == "Aurora") {
		cmds = zigbee.enrollResponse() + zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 30, 60 * 5, null) + zigbee.batteryConfig()
	} else if (manufacturer == "eWeLink" || manufacturer == "HEIMAN") {
		cmds = zigbee.enrollResponse() + zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 30, 60 * 5, null) + zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 600, 1)
	} else if (manufacturer == "Third Reality, Inc") {
		cmds = zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
	}
	cmds += refresh()
	cmds
}

def getBatteryPercentageResult(rawValue) {
	log.debug "Battery Percentage rawValue = ${rawValue} -> ${rawValue / 2}%"
	def result = [:]
	def manufacturer = getDataValue("manufacturer")
	if (0 <= rawValue && rawValue <= 200) {
		result.name = 'battery'
		result.translatable = true
	if (manufacturer == "Third Reality, Inc") {
		result.value = Math.round(rawValue)
	} else {
		result.value = Math.round(rawValue / 2)
	}
		result.descriptionText = "${device.displayName} battery was ${result.value}%"
	}

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

	return result
}

def getContactResult(value) {
	log.debug 'Contact Status'
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	[
		name           : 'contact',
		value          : value,
		descriptionText: descriptionText
	]
}
