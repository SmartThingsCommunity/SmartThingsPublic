/**
 *	NYCE Open/Close Sensor
 *
 *	Copyright 2015 NYCE Sensors Inc.
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "NYCE Open/Closed Sensor", namespace: "smartthings", author: "NYCE", mnmn: "SmartThings", vid: "generic-contact-3") {
		capability "Battery"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"

		command "enrollResponse"

		fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3010", deviceJoinName: "NYCE Door Hinge Sensor"
		fingerprint inClusters: "0000,0001,0003,0406,0500,0020", manufacturer: "NYCE", model: "3011", deviceJoinName: "NYCE Door/Window Sensor"
		fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3011", deviceJoinName: "NYCE Door/Window Sensor"
		fingerprint inClusters: "0000,0001,0003,0406,0500,0020", manufacturer: "NYCE", model: "3014", deviceJoinName: "NYCE Tilt Sensor"
		fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3014", deviceJoinName: "NYCE Tilt Sensor"
		fingerprint inClusters: "0000,0001,0003,0020,0500,0B05,FC02", outClusters: "", manufacturer: "sengled", model: "E1D-G73", deviceJoinName: "Sengled Element Door Sensor"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
				attributeState("open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
				attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
			}
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["contact"])
		details(["contact","battery","refresh"])
	}
}

def parse(String description) {
	Map map = [:]

	List listMap = []
	List listResult = []

	log.debug "parse: Parse message: ${description}"

	if (description?.startsWith("enroll request")) {
		List cmds = zigbee.enrollResponse()

		log.debug "parse: enrollResponse() ${cmds}"
		listResult = cmds?.collect { new physicalgraph.device.HubAction(it) }
	} else {
		if (description?.startsWith("zone status")) {
			listMap = parseIasMessage(description)
		}
		else if (description?.startsWith("read attr -")) {
			map = parseReportAttributeMessage(description)
		}
		else if (description?.startsWith("catchall:")) {
			map = parseCatchAllMessage(description)
		}
		// this condition is temperary to accomodate some unknown issue caused by SmartThings
		// once they fix the bug, this condition is not needed
		// The issue is most of the time when a device is removed thru the app, it takes couple
		// times to pair again successfully
		else if (description?.startsWith("updated")) {
			List cmds = configure()
			listResult = cmds?.collect { new physicalgraph.device.HubAction(it) }
		}

		// Create events from map or list of maps, whichever was returned
		if (listMap) {
			for (msg in listMap) {
				listResult << createEvent(msg)
			}
		}
		else if (map) {
			listResult << createEvent(map)
		}
	}

	log.debug "parse: listResult ${listResult}"
	return listResult
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)

	if (shouldProcessMessage(cluster)) {
		def msgStatus = cluster.data[2]

		log.debug "parseCatchAllMessage: msgStatus: ${msgStatus}"
		if (msgStatus == 0) {
			switch(cluster.clusterId) {
				case 0x0500:
					Map descMap = zigbee.parseDescriptionAsMap(description)

					if (descMap?.attrInt == 0x0002) {
						resultMap.name = "contact"
						def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
						resultMap.value = zs.isAlarm1Set() ? "open" : "closed"
					}
					break
				case 0x0001:	// power configuration cluster
					resultMap.name = 'battery'
					log.debug "battery value: ${cluster.data.last()}"
					resultMap.value = getBatteryPercentage(cluster.data.last())
					break
				case 0x0402:	// temperature cluster
					if (cluster.command == 0x01) {
						if(cluster.data[3] == 0x29) {
							def tempC = Integer.parseInt(cluster.data[-2..-1].reverse().collect{cluster.hex1(it)}.join(), 16) / 100
							resultMap = getTemperatureResult(getConvertedTemperature(tempC))
							log.debug "parseCatchAllMessage: Temp resultMap: ${resultMap}"
						}
						else {
							log.debug "parseCatchAllMessage: Temperature cluster Wrong data type"
						}
					}
					else {
						log.debug "parseCatchAllMessage: Unhandled Temperature cluster command ${cluster.command}"
					}
					break
				case 0x0405:	// humidity cluster
					if (cluster.command == 0x01) {
						if(cluster.data[3] == 0x21) {
							def hum = Integer.parseInt(cluster.data[-2..-1].reverse().collect{cluster.hex1(it)}.join(), 16) / 100
							resultMap = getHumidityResult(hum)
							log.debug "parseCatchAllMessage: Hum resultMap: ${resultMap}"
						}
						else {
							log.debug "parseCatchAllMessage: Humidity cluster wrong data type"
						}
					}
					else {
						log.debug "parseCatchAllMessage: Unhandled Humidity cluster command ${cluster.command}"
					}
					break
				default:
					break
			}
		}
		else {
			log.debug "parseCatchAllMessage: Message error code: Error code: ${msgStatus}	 ClusterID: ${cluster.clusterId}	Command: ${cluster.command}"
		}
	}

	return resultMap
}

private int getBatteryPercentage(int value) {
	def minVolts = 2.3
	def maxVolts = 3.1
	def volts = value / 10
	def pct = (volts - minVolts) / (maxVolts - minVolts)

	//for battery that may have a higher voltage than 3.1V
	if( pct > 1 ) {
		pct = 1
	}

	//the device actual shut off voltage is 2.25. When it drops to 2.3, there
	//is actually still 0.05V, which is about 6% of juice left.
	//setting the percentage to 6% so a battery low warning is issued
	if( pct <= 0 ) {
		pct = 0.06
	}
	return (int)(pct * 100)
}

private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	// 0x07 is bind message
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
			cluster.command == 0x0B ||
			cluster.command == 0x07 ||
			(cluster.data.size() > 0 && cluster.data.first() == 0x3e)

	return !ignoredMessage
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = zigbee.parseReadAttrDescription(description)
	Map resultMap = [:]

	log.debug "parseReportAttributeMessage: descMap ${descMap}"

	switch(descMap.cluster) {
		case "0001":
			if(descMap.attrId == "0020") {
				log.debug 'Battery'
				resultMap.name = 'battery'
				resultMap.value = getBatteryPercentage(convertHexToInt(descMap.value))
			}
			break
		default:
			log.info descMap.cluster
			log.info "cluster1"
			break
	}

	return resultMap
}

private List parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	log.debug "parseIasMessage: $description"

	List resultListMap = []
	Map resultMap_sensor = [:]

	resultMap_sensor.name = "contact"
	resultMap_sensor.value = zs.isAlarm1Set() ? "open" : "closed"

	// Check each relevant bit, create map for it, and add to list
	log.debug "parseIasMessage: Battery Status ${zs.battery}"
	log.debug "parseIasMessage: Trouble Status ${zs.trouble}"
	log.debug "parseIasMessage: Sensor Status ${zs.alarm1}"

	resultListMap << resultMap_sensor

	return resultListMap
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def configure() {
	// Device-Watch allows 2 check-in misses from device
	sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	if(device.getDataValue("manufacturer") == "sengled") {
		return zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) +
		zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 30, 300, null) +
		zigbee.batteryConfig(30, 300) + zigbee.enrollResponse()
	} else {
		// battery minReportTime 30 seconds, maxReportTime 5 min. Reporting interval if no activity
		return zigbee.enrollResponse() + zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 0, 60 * 60, null) +
				zigbee.batteryConfig(30, 300) + refresh() // send refresh cmds as part of config
	}
}

Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def refresh() {
	return zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) + zigbee.enrollResponse()
}