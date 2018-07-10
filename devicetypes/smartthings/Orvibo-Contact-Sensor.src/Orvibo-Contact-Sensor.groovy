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
	definition(name: "Orvibo Contact Sensor", namespace: "smartthings", author: "biaoyi.deng@samsung.com", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn:"SmartThings", vid:"generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Battery"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000,0003,0500,0001", manufacturer: "ORVIBO", model: "e70f96b3773a4c9283c6862dbafb6a99"
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
		}else if(description?.startsWith('enroll request')){
			List cmds = zigbee.enrollResponse()
			log.debug "enroll response: ${cmds}"
			result = cmds?.collect { new physicalgraph.device.HubAction(it) }
		}else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == 0x0001 && descMap?.commandInt != 0x07 && descMap?.value) {
				if(descMap?.attrInt==0x0021){
					map = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
					result = createEvent(map)
				}
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

def installed(){
	log.debug "call installed()"
	sendEvent(name: "checkInterval", value:20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	refresh()
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping is called"
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
}

def refresh() {
	log.debug "Refreshing  Battery and ZONE Status"
	def refreshCmds = zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021) +
		zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
	refreshCmds + zigbee.enrollResponse()
}

def configure() {
	sendEvent(name: "checkInterval", value:20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting, IAS CIE, and Bindings."

	refresh() + zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 21600, 0x10) + refresh()
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

	result
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