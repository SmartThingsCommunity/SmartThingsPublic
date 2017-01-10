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
 *	SmartPower Dimming Outlet (CentraLite)
 *
 *	Author: SmartThings
 *	Date: 2013-12-04
 */
metadata {
	definition (name: "SmartPower Dimming Outlet", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
		capability "Switch Level"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "4257050-ZHAC"

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
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'${currentValue} W'
			}
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def event = [:]
	def finalResult = isKnownDescription(description)
	if (finalResult) {
		log.info finalResult
		if (finalResult.type == "update") {
			log.info "$device updates: ${finalResult.value}"
			event = null
		}
		else if (finalResult.type == "power") {
			def powerValue = (finalResult.value as Integer)/10
			event = createEvent(name: "power", value: powerValue)

			/*
				Dividing by 10 as the Divisor is 10000 and unit is kW for the device. AttrId: 0302 and 0300. Simplifying to 10

				power level is an integer. The exact power level with correct units needs to be handled in the device type
				to account for the different Divisor value (AttrId: 0302) and POWER Unit (AttrId: 0300). CLUSTER for simple metering is 0702
			*/
		}
		else {
			event = createEvent(name: finalResult.type, value: finalResult.value)
		}
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug parseDescriptionAsMap(description)
	}
	return event
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

def setLevel(value) {
	value = value as Integer
	if (value == 0) {
		off()
	}
	else {
		if (device.latestValue("switch") == "off") {
			sendEvent(name: "switch", value: "on")
		}
		sendEvent(name: "level", value: value)
		setLevelWithRate(value, "0000")		//value is between 0 to 100
	}
}

def refresh() {
	[
			"st rattr 0x${device.deviceNetworkId} ${endpointId} 6 0", "delay 2000",
			"st rattr 0x${device.deviceNetworkId} ${endpointId} 8 0", "delay 2000",
			"st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0B04 0x050B", "delay 2000"
	]

}

def configure() {
	refresh() + onOffConfig() + levelConfig() + powerConfig()
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
		else if (descMap.cluster == "0008" || descMap.clusterId == "0008"){
			isDescriptionLevel(descMap)
		}
		else if (descMap.cluster == "0B04" || descMap.clusterId == "0B04"){
			isDescriptionPower(descMap)
		}
		else {
			return [:]
		}
	}
	else if(description?.startsWith("on/off:")) {
		def switchValue = description?.endsWith("1") ? "on" : "off"
		return	[type: "switch", value : switchValue]
	}
	else {
		return [:]
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
		return [:]
	}

}

//@return - false or "success" or level [0-100]
def isDescriptionLevel(descMap) {
	def dimmerValue = -1
	if (descMap.cluster == "0008"){
		//TODO: the message returned with catchall is command 0B with clusterId 0008. That is just a confirmation message
		def value = convertHexToInt(descMap.value)
		dimmerValue = Math.round(value * 100 / 255)
		if(dimmerValue==0 && value > 0) {
			dimmerValue = 1						//handling for non-zero hex value less than 3
		}
	}
	else if(descMap.clusterId == "0008") {
		if(descMap.command=="0B"){
			return	[type: "update", value : "level updated successfully"]					//device updating the level change was successful. no value sent.
		}
		else if(descMap.command=="07"){
			return	[type: "update", value : "level (0008) capability configured successfully"]
		}
	}

	if (dimmerValue != -1){
		return	[type: "level", value : dimmerValue]
	}
	else {
		return [:]
	}
}

def isDescriptionPower(descMap) {
	def powerValue = "undefined"
	if (descMap.cluster == "0B04") {
		if (descMap.attrId == "050b") {
			if(descMap.value!="ffff")
				powerValue = convertHexToInt(descMap.value)
		}
	}
	else if (descMap.clusterId == "0B04") {
		if(descMap.command=="07"){
			return	[type: "update", value : "power (0B04) capability configured successfully"]
		}
	}

	if (powerValue != "undefined"){
		return	[type: "power", value : powerValue]
	}
	else {
		return [:]
	}
}


def onOffConfig() {
	[
			"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 6 {${device.zigbeeId}} {}", "delay 2000",
			"zcl global send-me-a-report 6 0 0x10 0 600 {01}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 2000"
	]
}

//level config for devices with min reporting interval as 5 seconds and reporting interval if no activity as 1hour (3600s)
//min level change is 01
def levelConfig() {
	[
			"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 8 {${device.zigbeeId}} {}", "delay 2000",
			"zcl global send-me-a-report 8 0 0x20 5 3600 {01}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 2000"
	]
}

//power config for devices with min reporting interval as 1 seconds and reporting interval if no activity as 10min (600s)
//min change in value is 05
def powerConfig() {
	[
		"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 0x0B04 {${device.zigbeeId}} {}", "delay 2000",
		"zcl global send-me-a-report 0x0B04 0x050B 0x29 1 600 {05 00}",				//The send-me-a-report is custom to the attribute type for CentraLite
		"delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 2000"
	]
}

def setLevelWithRate(level, rate) {
	if(rate == null){
		rate = "0000"
	}
	level = convertToHexString(level * 255 / 100) 				//Converting the 0-100 range to 0-FF range in hex
	[
			"st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {$level $rate}",
			"delay 2000"
	]
}

String convertToHexString(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}
