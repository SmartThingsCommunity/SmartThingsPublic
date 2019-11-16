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
	definition(name: "eWeLink Water Sensor", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.moisture", vid: "generic-leak", genericHandler: "Zigbee") {
		capability "Water Sensor"
		capability "Configuration"
		capability "Battery"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"
        
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500", outClusters: "0019", manufacturer: "eWeLink", model: "WS01", deviceJoinName: "eWeLink Water Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3315-S", deviceJoinName: "Water Leak Sensor"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3315"
		fingerprint inClusters: "0000,0001,0003,0402,0500,0020,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3315-Seu", deviceJoinName: "Water Leak Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3315-L", deviceJoinName: "Iris Smart Water Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "3315-G", deviceJoinName: "Centralite Water Sensor"
		fingerprint inClusters: "0000,0001,0003,000F,0020,0402,0500", outClusters: "0019", manufacturer: "SmartThings", model: "moisturev4", deviceJoinName: "Water Leak Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0402,0500", outClusters: "0019", manufacturer: "Samjin", model: "water", deviceJoinName: "Water Leak Sensor"
	}

	simulator {

	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
					"http://cdn.device-gse.smartthings.com/Moisture/Moisture1.png",
					"http://cdn.device-gse.smartthings.com/Moisture/Moisture2.png",
					"http://cdn.device-gse.smartthings.com/Moisture/Moisture3.png"
			])
		}

	}

	tiles(scale: 2) {
		multiAttributeTile(name: "water", type: "generic", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", label: "Dry", icon: "st.alarm.water.dry", backgroundColor: "#ffffff"
				attributeState "wet", label: "Wet", icon: "st.alarm.water.wet", backgroundColor: "#00A0DC"
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

		main(["water", "temperature"])
		details(["water", "temperature", "battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "description: $description"

	def result = [:]
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			ZoneStatus zs = zigbee.parseZoneStatus(description)
			map = zs.isAlarm1Set() ? getMoistureResult('wet') : getMoistureResult('dry')
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
				} else {
					map = getBatteryResult(Integer.parseInt(descMap.value, 16))
				}
				result = createEvent(map)
			} else if (descMap?.clusterInt == 0x0500 && descMap?.attrInt == 0x0002) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = getMoistureResult(zs.isAlarm1Set() ? "wet" : "dry")
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
	if (getDataValue("manufacturer") == "eWeLink") {
		sendEvent(name: "checkInterval", value:2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	}else {
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

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)

	translateZoneStatus(zs)
}

private Map translateZoneStatus(ZoneStatus zs) {
	return zs.isAlarm1Set() ? getMoistureResult('wet') : getMoistureResult('dry')
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

private Map getMoistureResult(value) {
	log.debug "water"
	def descriptionText
	if (value == "wet")
		descriptionText = '{{ device.displayName }} is wet'
	else
		descriptionText = '{{ device.displayName }} is dry'
	return [
			name           : 'water',
			value          : value,
			descriptionText: descriptionText,
			translatable   : true
	]
}
def refresh() {
	log.debug "Refreshing  Battery and ZONE Status"
	def manufacturer = getDataValue("manufacturer")
	def refreshCmds =  zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
	if (manufacturer == "ORVIBO") {
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
	} else if (manufacturer == "eWeLink") { // this is actually just supposed to be for Aurora, but we'll make it the default as it's more widely supported
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
	}
	else { // this is actually just supposed to be for Aurora, but we'll make it the default as it's more widely supported
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020)
	}
	refreshCmds + zigbee.enrollResponse()
}

def configure() {
	if (getDataValue("manufacturer") == "eWeLink") {
		sendEvent(name: "checkInterval", value:2 * 60 * 60 + 5 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	} else{
		sendEvent(name: "checkInterval", value:20 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	}
	def cmds = []
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	//The electricity attribute is reported without bind and reporting CFG. The TI plan reports the power once in about 10 minutes; the NXP plan reports the electricity once in 20 minutes
	if (getDataValue("manufacturer") == "Aurora") {
		cmds = zigbee.enrollResponse() + zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 30, 60 * 5, null) + zigbee.batteryConfig()
	}
	if (getDataValue("manufacturer") == "eWeLink") {
		cmds = zigbee.enrollResponse() + 
        zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 3600, 0x10)
	}
	cmds += refresh()
	cmds
}