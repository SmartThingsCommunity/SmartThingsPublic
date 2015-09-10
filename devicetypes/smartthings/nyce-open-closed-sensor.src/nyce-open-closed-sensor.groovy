/**
 *  NYCE Open/Close Sensor
 *
 *  Copyright 2015 NYCE Sensors Inc.
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
 
metadata {
	definition (name: "NYCE Open/Closed Sensor", namespace: "smartthings", author: "NYCE") {
    	capability "Battery"
		capability "Configuration"
        capability "Contact Sensor"
		capability "Refresh"
        
        command "enrollResponse"
 
 
		fingerprint inClusters: "0000,0001,0003,0406,0500,0020", manufacturer: "NYCE", model: "3011", deviceJoinName: "NYCE Door/Window Sensor"
        fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3011", deviceJoinName: "NYCE Door/Window Sensor"
        fingerprint inClusters: "0000,0001,0003,0406,0500,0020", manufacturer: "NYCE", model: "3014", deviceJoinName: "NYCE Tilt Sensor"
        fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3014", deviceJoinName: "NYCE Tilt Sensor"
	}
 
	simulator {
 
	}
 
	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
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
		List cmds = enrollResponse()

		log.debug "parse: enrollResponse() ${cmds}"
		listResult = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	else {
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
				case 0x0001:
					log.debug 'Battery'
					resultMap.name = 'battery'
					log.info "in parse catch all"
					log.debug "battery value: ${cluster.data.last()}"
					resultMap.value = getBatteryPercentage(cluster.data.last())
					break
				case 0x0402:    // temperature cluster
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
				case 0x0405:    // humidity cluster
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
			log.debug "parseCatchAllMessage: Message error code: Error code: ${msgStatus}    ClusterID: ${cluster.clusterId}    Command: ${cluster.command}"
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
	if( pct > 1 )
	{
		pct = 1
	}

	//the device actual shut off voltage is 2.25. When it drops to 2.3, there
	//is actually still 0.05V, which is about 6% of juice left.
	//setting the percentage to 6% so a battery low warning is issued
	if( pct <= 0 )
	{
		pct = 0.06
	}
	return (int) pct * 100
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
	Map descMap = (description - "read attr - ").split(",").inject([:]) {
		map, param -> def nameAndValue = param.split(":")
			map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	Map resultMap = [:]

	log.debug "parseReportAttributeMessage: descMap ${descMap}"

	switch(descMap.cluster) {
		case "0001":
			log.debug 'Battery'
			resultMap.name = 'battery'
			resultMap.value = getBatteryPercentage(convertHexToInt(descMap.value))
			break
		default:
			log.info descMap.cluster
			log.info "cluster1"
			break
	}

	return resultMap
}

private List parseIasMessage(String description) {
	List parsedMsg = description.split(" ")
	String msgCode = parsedMsg[2]

	List resultListMap = []
	Map resultMap_battery = [:]
	Map resultMap_battery_state = [:]
	Map resultMap_sensor = [:]

	// Relevant bit field definitions from ZigBee spec
	def BATTERY_BIT = ( 1 << 3 )
	def TROUBLE_BIT = ( 1 << 6 )
	def SENSOR_BIT = ( 1 << 0 )		// it's ALARM1 bit from the ZCL spec

	// Convert hex string to integer
	def zoneStatus = Integer.parseInt(msgCode[-4..-1],16)

	log.debug "parseIasMessage: zoneStatus: ${zoneStatus}"

	// Check each relevant bit, create map for it, and add to list
	log.debug "parseIasMessage: Battery Status ${zoneStatus & BATTERY_BIT}"
	log.debug "parseIasMessage: Trouble Status ${zoneStatus & TROUBLE_BIT}"
	log.debug "parseIasMessage: Sensor Status ${zoneStatus & SENSOR_BIT}"

	/* 	Comment out this path to check the battery state to avoid overwriting the
		battery value (Change log #2), but keep these conditions for later use
	 resultMap_battery_state.name = "battery_state"
	 if (zoneStatus & TROUBLE_BIT) {
		 resultMap_battery_state.value = "failed"

		 resultMap_battery.name = "battery"
		 resultMap_battery.value = 0
	 }
	 else {
		 if (zoneStatus & BATTERY_BIT) {
			 resultMap_battery_state.value = "low"

			 // to generate low battery notification by the platform
			 resultMap_battery.name = "battery"
			 resultMap_battery.value = 15
		 }
		 else {
			 resultMap_battery_state.value = "ok"

			 // to clear the low battery state stored in the platform
			 // otherwise, there is no notification sent again
			 resultMap_battery.name = "battery"
			 resultMap_battery.value = 80
		 }
	 }
	*/

	resultMap_sensor.name = "contact"
	resultMap_sensor.value = (zoneStatus & SENSOR_BIT) ? "open" : "closed"

	resultListMap << resultMap_battery_state
	resultListMap << resultMap_battery
	resultListMap << resultMap_sensor

	return resultListMap
}

def configure() {
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)

	def configCmds = [
			//battery reporting and heartbeat
			"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 1 {${device.zigbeeId}} {}", "delay 200",
			"zcl global send-me-a-report 1 0x20 0x20 600 3600 {01}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500",


			// Writes CIE attribute on end device to direct reports to the hub's EUID
			"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 1", "delay 500",
	]

	log.debug "configure: Write IAS CIE"
	return configCmds
}

def enrollResponse() {
	[
			// Enrolling device into the IAS Zone
			"raw 0x500 {01 23 00 00 00}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 1"
	]
}

private hex(value) {
	new BigInteger(Math.round(value).toString()).toString(16)
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
	int i = 0;
	int j = array.length - 1;
	byte tmp;

	while (j > i) {
		tmp = array[j];
		array[j] = array[i];
		array[i] = tmp;
		j--;
		i++;
	}

	return array
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def refresh() {
	log.debug "Refreshing Battery"
	[
			"st rattr 0x${device.deviceNetworkId} ${endpointId} 1 0x20", "delay 200"
	]
}
