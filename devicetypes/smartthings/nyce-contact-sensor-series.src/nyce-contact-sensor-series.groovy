/**
 *  NYCE Open/Close Sensor
 *
 *  Copyright 2017 NYCE Sensors Inc.
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
 *	Version 1.0.4
 *	Last Edited: 25 Apr 2017
 *
 */

/**
V1.0.1
Change log #1, from SmartThings:
1. Updated the battery tile to show percentage. Added refresh button for support/debug purposes
2. Added heartbeat/battery reporting configuration. Will tie into our device health with the regular heartbeat messages
3. Added support methods to calculate battery, please verify max and min battery levels in getBatteryPercentage method

Change log #2, from NYCE, 16 July 2015:
1. Revised min and max battery level in getBatteryPercentage() to 2.3 and 3.0 respectively for 3011 & 3014 devices
2. Commented out the path to check the battery state in parseIasMessage() as we found that this path is changing
   the battery value

Change log #3, from SmartThings, 17 July 2015:
1. Added Refresh capability
2. Fixed refresh tile not working bug

Change log #4, from NYCE, 17 July 2015:
1. Added protection for reported voltage out of the min and max range
*/

/**
V1.0.2
Change log #1, from NYCE, 4 Aug 2015:
1. Added support for parsing battery percentage
2. Add refresh at configuration to retrive battery percentage info upon joining
3. Add to detect whether Battery Voltage report or Battery Percentage report should be used
4. Adjust battery voltage max to 3.0V
5. Add Attribute init function
*/

/**
V1.0.3
Skip V1.0.3 to sync with motion sensor device handler code
*/

/**
V1.0.4
Change log #1, from NYCE, 25 April 2017:
1. SmartThings updated with a new ZigBee class
2. Modify battery voltage or percentage configure reporting interval to min 10 mins and max 120 mins
*/

import physicalgraph.zigbee.clusters.iaszone.ZoneStatus


metadata {
	definition (name: "NYCE Contact Sensor Series", namespace: "smartthings", author: "NYCE") {
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
		fingerprint inClusters: "0000,0001,0003,0406,0500,0020", manufacturer: "NYCE", model: "3014", deviceJoinName: "NYCE Tile Sensor"
		fingerprint inClusters: "0000,0001,0003,0500,0020", manufacturer: "NYCE", model: "3014", deviceJoinName: "NYCE Tile Sensor"
	}

	simulator {

	}

	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc")
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", decoration: "flat", inactiveLabel: false) {
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

private boolean shouldProcessMessage(cluster) {
	// 0x0B is default response indicating message got through
	// 0x07 is bind message
	boolean ignoredMessage = cluster.profileId != 0x0104 ||
		cluster.command == 0x0B ||
		cluster.command == 0x07 ||
		(cluster.data.size() > 0 && cluster.data.first() == 0x3e)

	return !ignoredMessage
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
                    if(cluster.attrId == 0x0020) {
						if(state.batteryReportType == "voltage") {
							resultMap.name = 'battery'
							resultMap.value = getBatteryPercentage(cluster.data.last)
							log.debug "Battery Voltage convert to ${resultMap.value}%"
						}
					}
					else if(cluster.attrId == 0x0021) {
						if(state.batteryReportType == "percentage") {
                            resultMap.name = 'battery'
                            resultMap.value = (cluster.data.last / 2)
                            log.debug "Battery Percentage convert to ${resultMap.value}%"
                        }
					}
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
	def maxVolts = 3.0
	def volts = value / 10
	def pct = (volts - minVolts) / (maxVolts - minVolts)

	//for battery that may have a higher voltage than 3.0V
	if ( pct > 1 )
	{
		pct = 1
	}

	//the device actual shut off voltage is 2.25. When it drops to 2.3, there
	//is actually still 0.05V, which is about 6% of juice left.
	//setting the percentage to 6% so a battery low warning is issued
	if ( pct <= 0 )
	{
		pct = 0.06
	}
	return (int) pct * 100
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) {
    	map, param -> def nameAndValue = param.split(":")
        	map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

private Map parseReportAttributeMessage(String description) {
	Map descMap = parseDescriptionAsMap(description)
    
	Map resultMap = [:]

	log.debug "parseReportAttributeMessage: descMap ${descMap}"

	switch(descMap.cluster) {
		case "0001":
			log.debug 'Read Battery'
            
			if(descMap.attrId == "0020") {
				if(state.batteryReportType == "voltage") {
					resultMap.name = 'battery'
					resultMap.value = getBatteryPercentage(convertHexToInt(descMap.value))
					log.debug "Battery Voltage convert to ${resultMap.value}%"
				}
			}
			else if(descMap.attrId == "0021") {
				if(descMap.result != "unsupported attr") {
					state.batteryReportType = "percentage"
				}
				else {
					state.batteryReportType = "voltage"
				}

				if(state.batteryReportType == "percentage")	{
					resultMap.name = 'battery'
					resultMap.value = (convertHexToInt(descMap.value) / 2)
					log.debug "Battery Percentage convert to ${resultMap.value}%"
				}
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
	Map resultMap_battery = [:]
	Map resultMap_battery_state = [:]
	Map resultMap_sensor = [:]

	resultMap_sensor.name = "contact"
	resultMap_sensor.value = zs.isAlarm1Set() ? "open" : "closed"

	// Check each relevant bit, create map for it, and add to list
	log.debug "parseIasMessage: Battery Status ${zs.battery}"
	log.debug "parseIasMessage: Trouble Status ${zs.trouble}"
	log.debug "parseIasMessage: Sensor Status ${zs.alarm1}"

	resultMap_battery_state.name = "battery_state"
	if (zs.isTroubleSet()) {
		resultMap_battery_state.value = "failed"
	}
	else {
		if (zs.isBatterySet()) {
			resultMap_battery_state.value = "low"
		}
		else {
			resultMap_battery_state.value = "ok"
		}
	}
	
	resultListMap << resultMap_battery_state
	resultListMap << resultMap_sensor

	log.debug "parseIasMessage: resultListMap ${resultListMap}"
	return resultListMap
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.readAttribute(0x001, 0x0020) // Read the Battery Level
}

def configure() {
	attrInit()
    
	// Device-Watch allows 2 check-in misses from device
	sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)

	def enrollCmds = [
    	// Writes CIE attribute on end device to direct reports to the hub's EUID
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 500"
	]

	log.debug "configure: Write IAS CIE, configure reportings"
	return enrollCmds + refresh() +// send refresh cmds as part of config
    	zigbee.configureReporting(0x0001, 0x0021, 0x20, 600, 7200, 0x02)	// battery minReportTime 10mins(600s), maxReportTime 120mins(7200s). Reporting interval if no activity
}

def attrInit() {
	log.debug "Attr Init"
	state.batteryReportType = "init"
}

def enrollResponse() {
	[
		// Enrolling device into the IAS Zone
		"raw 0x500 {01 23 00 00 00}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1"
	]
}

def refresh() {
	log.debug "refresh()"
    
    def refreshCmds
    
    if (state.batteryReportType == "init") {
    	log.debug "refresh() - read init"
    	refreshCmds = [
        	"st rattr 0x${device.deviceNetworkId} ${endpointId} 1 0x21", "delay 200",
    		"st rattr 0x${device.deviceNetworkId} ${endpointId} 1 0x20", "delay 200"
		]
    }
    else if (state.batteryReportType == "voltage") {
    	log.debug "refresh() - read voltage"
    	refreshCmds = [
    		"st rattr 0x${device.deviceNetworkId} ${endpointId} 1 0x20", "delay 200"
		]
    }
    else {
    	log.debug "refresh() - read percentage"
    	refreshCmds = [
        	"st rattr 0x${device.deviceNetworkId} ${endpointId} 1 0x21", "delay 200"
		]
    }
    
	return refreshCmds
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
	Integer.parseInt(hex, 16)
}