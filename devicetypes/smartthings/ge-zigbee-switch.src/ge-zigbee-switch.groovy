/**
 *  Copyright 2015 SmartThings
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
 *	    GE/Jasco ZigBee Switch
 *
 *	Author: SmartThings
 *	Date: 2015-07-01
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "GE ZigBee Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B05,0702", outClusters: "0003, 000A,0019", manufacturer: "Jasco Products", model: "45853"
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0B05,0702", outClusters: "000A,0019", manufacturer: "Jasco Products", model: "45856"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("power", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "power", label:'${currentValue} Watts'
		}
		main "switch"
		details(["switch", "power", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def finalResult = isKnownDescription(description)
	if (finalResult != "false") {
		log.info finalResult
		if (finalResult.type == "update") {
			log.info "$device updates: ${finalResult.value}"
		}
		else if (finalResult.type == "power") {
			def powerValue = (finalResult.value as Integer)/10
			sendEvent(name: "power", value: powerValue)

			/*
				Dividing by 10 as the Divisor is 10000 and unit is kW for the device. AttrId: 0302 and 0300. Simplifying to 10

				power level is an integer. The exact power level with correct units needs to be handled in the device type
				to account for the different Divisor value (AttrId: 0302) and POWER Unit (AttrId: 0300). CLUSTER for simple metering is 0702
			*/
		}
		else {
			sendEvent(name: finalResult.type, value: finalResult.value)
		}
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug parseDescriptionAsMap(description)
	}
}

// Commands to device
def zigbeeCommand(cluster, attribute){
	"st cmd 0x${device.deviceNetworkId} ${endpointId} ${cluster} ${attribute} {}"
}

def off() {
	zigbeeCommand("6", "0")
}

def on() {
	zigbeeCommand("6", "1")
}

def refresh() {
	[
			"st rattr 0x${device.deviceNetworkId} ${endpointId} 6 0", "delay 500",
			"st rattr 0x${device.deviceNetworkId} ${endpointId} 8 0", "delay 500",
			"st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0702 0x0400", "delay 500"
	]

}

def configure() {
	onOffConfig() + powerConfig() + refresh()
}


private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private String swapEndianHex(String hex) {
	reverseArray(hex.decodeHex()).encodeHex()
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

//Need to reverse array of size 2
private byte[] reverseArray(byte[] array) {
	byte tmp;
	tmp = array[1];
	array[1] = array[0];
	array[0] = tmp;
	return array
}

def parseDescriptionAsMap(description) {
	if (description?.startsWith("read attr -")) {
		(description - "read attr - ").split(",").inject([:]) { map, param ->
			def nameAndValue = param.split(":")
			map += [(nameAndValue[0].trim()): nameAndValue[1].trim()]
		}
	}
	else if (description?.startsWith("catchall: ")) {
		def seg = (description - "catchall: ").split(" ")
		def zigbeeMap = [:]
		zigbeeMap += [raw: (description - "catchall: ")]
		zigbeeMap += [profileId: seg[0]]
		zigbeeMap += [clusterId: seg[1]]
		zigbeeMap += [sourceEndpoint: seg[2]]
		zigbeeMap += [destinationEndpoint: seg[3]]
		zigbeeMap += [options: seg[4]]
		zigbeeMap += [messageType: seg[5]]
		zigbeeMap += [dni: seg[6]]
		zigbeeMap += [isClusterSpecific: Short.valueOf(seg[7], 16) != 0]
		zigbeeMap += [isManufacturerSpecific: Short.valueOf(seg[8], 16) != 0]
		zigbeeMap += [manufacturerId: seg[9]]
		zigbeeMap += [command: seg[10]]
		zigbeeMap += [direction: seg[11]]
		zigbeeMap += [data: seg.size() > 12 ? seg[12].split("").findAll { it }.collate(2).collect {
			it.join('')
		} : []]

		zigbeeMap
	}
}

def isKnownDescription(description) {
	if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
		def descMap = parseDescriptionAsMap(description)
		if (descMap.cluster == "0006" || descMap.clusterId == "0006") {
			isDescriptionOnOff(descMap)
		}
		else if (descMap.cluster == "0702" || descMap.clusterId == "0702"){
			isDescriptionPower(descMap)
		}
		else {
			return "false"
		}
	}
	else if(description?.startsWith("on/off:")) {
		def switchValue = description?.endsWith("1") ? "on" : "off"
		return	[type: "switch", value : switchValue]
	}
	else {
		return "false"
	}
}

def isDescriptionOnOff(descMap) {
	def switchValue = "undefined"
	if (descMap.cluster == "0006") {				//cluster info from read attr
		value = descMap.value
		if (value == "01"){
			switchValue = "on"
		}
		else if (value == "00"){
			switchValue = "off"
		}
	}
	else if (descMap.clusterId == "0006") {
		//cluster info from catch all
		//command 0B is Default response and the last two bytes are [on/off][success]. on/off=00, success=00
		//command 01 is Read attr response. the last two bytes are [datatype][value]. boolean datatype=10; on/off value = 01/00
		if ((descMap.command=="0B" && descMap.raw.endsWith("0100")) || (descMap.command=="01" && descMap.raw.endsWith("1001"))){
			switchValue = "on"
		}
		else if ((descMap.command=="0B" && descMap.raw.endsWith("0000")) || (descMap.command=="01" && descMap.raw.endsWith("1000"))){
			switchValue = "off"
		}
		else if(descMap.command=="07"){
			return	[type: "update", value : "switch (0006) capability configured successfully"]
		}
	}

	if (switchValue != "undefined"){
		return	[type: "switch", value : switchValue]
	}
	else {
		return "false"
	}

}

def isDescriptionPower(descMap) {
	def powerValue = "undefined"
	if (descMap.cluster == "0702") {
		if (descMap.attrId == "0400") {
			powerValue = convertHexToInt(descMap.value)
		}
	}
	else if (descMap.clusterId == "0702") {
		if(descMap.command=="07"){
			return	[type: "update", value : "power (0702) capability configured successfully"]
		}
	}

	if (powerValue != "undefined"){
		return	[type: "power", value : powerValue]
	}
	else {
		return "false"
	}
}


def onOffConfig() {
	[
			"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 6 {${device.zigbeeId}} {}", "delay 200",
			"zcl global send-me-a-report 6 0 0x10 0 600 {01}",
			"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500"
	]
}

//power config for devices with min reporting interval as 1 seconds and reporting interval if no activity as 10min (600s)
//min change in value is 05
def powerConfig() {
	[
			//Meter (Power) Reporting
			"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 0x0702 {${device.zigbeeId}} {}", "delay 200",
			"zcl global send-me-a-report 0x0702 0x0400 0x2A 1 600 {05}",
			"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500"
	]
}

String convertToHexString(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}