/**
 *  Lumi Dimmer
 *
 *  Copyright 2015 Lumi Vietnam
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
	definition (name: "Lumi Dimmer Switch", namespace: "lumi", author: "Thonv") {
		capability "Switch"
		capability "Switch Level"
		capability "Configuration"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
    
    	fingerprint profileId: "0104", deviceId: "0101", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003", manufacturer: "Lumi", model: "LM-DZ1"
		fingerprint profileId: "0104", deviceId: "0101", inClusters: "0000, 0003, 0004, 0005, 0006, 0008", outClusters: "0003", manufacturer: "Lumi R&D", model: "LM-DZ1"        
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

	tiles {
    	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: "Off", action: "switch.on",  icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on"
        	state "on",  label: "On", action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "off"
		}
		standardTile("on", "device.onAll", decoration: "flat") {
			state "default", label: 'On', action: "on",  icon: "st.switches.light.on", backgroundColor: "#ffffff"
		}
		standardTile("off", "device.offAll", decoration: "flat") {
			state "default", label: 'Off', action: "off", icon: "st.switches.light.off", backgroundColor: "#ffffff"
		}
        controlTile("levelControl", "device.levelControl", "slider", width: 2, height: 1) {
            state "default", action:"switch level.setLevel", backgroundColor:"#79b821"
        }
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch", "on", "off", "levelControl", "level", "refresh"])
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
		else {
			if (finalResult.type == "switch") {
            	sendEvent(name: finalResult.type, value: finalResult.value)
            }
            else if (finalResult.type == "level") {
            	sendEvent(name: "level", value: finalResult.value)
            	sendEvent(name: "levelControl", value: finalResult.value)                
            	if (finalResult.value == 0)
                	sendEvent(name: "switch", value: "off")
                else
					sendEvent(name: "switch", value: "on")                
            }
		}
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug parseDescriptionAsMap(description)
	}
}

def off() {
	log.debug "0x${device.deviceNetworkId} Endpoint 1"
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0006 0 {}"
	setLevel(0)
}

def on() {
	log.debug "0x${device.deviceNetworkId} Endpoint 1"
    "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0006 1 {}"
	setLevel(50)
}

def setLevel(value) {
	value = value as Integer
	sendEvent(name: "level", value: value)
	sendEvent(name: "levelControl", value: value)
		setLevelWithRate(value, "0000")// + on()
}

def refresh() {
	[
			"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",
			"st rattr 0x${device.deviceNetworkId} 1 8 0", "delay 500",
	]

}

def configure() {
	refresh()
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
		return "false"
	}
}
//level config for devices with min reporting interval as 5 seconds and reporting interval if no activity as 1hour (3600s)
//min level change is 01
def reportConfig() {
	[
			"zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 300",
			"zcl global send-me-a-report 6 0 0x10 5 600 {01}",
			"send 0x${device.deviceNetworkId} 1 1", "delay 100",

            "zdo bind 0x${device.deviceNetworkId} 1 1 8 {${device.zigbeeId}} {}", "delay 300",
            "zcl global send-me-a-report 8 0 0x20 5 600 {01}",
			"send 0x${device.deviceNetworkId} 1 1}", "delay 100"
	]
}




def setLevelWithRate(level, rate) {
	rate = "0000"
	level = convertToHexString(level * 255 / 100) 				//Converting the 0-100 range to 0-FF range in hex
	["st cmd 0x${device.deviceNetworkId} 1 8 0 {$level $rate}"]
}

String convertToHexString(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}