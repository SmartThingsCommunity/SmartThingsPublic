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
  *  Author : Fen Mei / f.mei@samsung.com 
  *  Date : 2018-07-06
  */ 
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Orvibo Temperature&Humidity Sensor",namespace: "smartthings", author: "SmartThings", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-humidity") {
		capability "Configuration"
		capability "Sensor"
 	        capability "Temperature Measurement"
		capability "Refresh"
		capability "Health Check"
		capability "Battery"
		fingerprint profileId: "0104",deviceId: "0302", inClusters: "0000,0001,0003,0402", outClusters: ""
	}
	
	preferences {
		input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter \"-5\". If 3 degrees too cold, enter \"+3\".", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
	}
	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label: '${currentValue}F',
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

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		childDeviceTile("humidity", "ch2", height: 2, width: 2, childTileName: "humidity")
        //AddChildTile("humidity")
		main "temperature"
		details(["temperature", "battery", "refresh"])
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

def installed() { 
 	def componentLabel 

    componentLabel = "$device.displayName 1" 
 	 
 	try { 
 		String dni = "${device.deviceNetworkId}-ep2"
 		addChildDevice("Orvibo Temperature&Humidity Sensor Endpoint", dni, device.hub.id, 
 			[completedSetup: true, label: "${componentLabel}", 
 			 isComponent   : false, componentName: "ch2", componentLabel: "${componentLabel}"]) 
 		log.debug "Endpoint 2 (Orvibo Temperature&Humidity Endpoint) added as $componentLabel" 
 	} catch (e) { 
 		log.debug "Failed to add endpoint 2 ($desc) as Orvibo Temperature&Humidity Sensor Endpoint - $e" 
 	} 
 	configure() 
}

def parse(String description) {
	log.debug "description: $description"

	Map map = zigbee.getEvent(description)
	if (!map) {
		Map descMap = zigbee.parseDescriptionAsMap(description)
		if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.commandInt != 0x07 && descMap.value) {
			log.info "BATT METRICS - attr: ${descMap?.attrInt}, value: ${descMap?.value}, decValue: ${Integer.parseInt(descMap.value, 16)}, currPercent: ${device.currentState("battery")?.value}, device: ${device.getDataValue("manufacturer")} ${device.getDataValue("model")}"
			List<Map> descMaps = collectAttributes(descMap)
			def battMap = descMaps.find { it.attrInt == 0x0021 }
			if (battMap) {
				map = getBatteryPercentageResult(Integer.parseInt(battMap.value, 16))
			}
		} 
	}

	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	
	return result
}

private Map getBatteryPercentageResult(rawValue) {
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

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def refresh() {
	log.debug "Refreshing Values"
	def refreshCmds = []
		refreshCmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)+ zigbee.readAttribute(0x0402, 0x0000)+
		zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value:1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting"
	def configCmds = []

	// temperature minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
	// battery minReport 30 seconds, maxReportTime 6 hrs by default
	configCmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 30, 600, 0x10)
	return refresh() + configCmds + refresh() // send refresh cmds as part of config
}
