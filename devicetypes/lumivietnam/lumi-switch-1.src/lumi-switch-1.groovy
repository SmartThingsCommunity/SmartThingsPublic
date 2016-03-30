/**
 *  Lumi Switch 4
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
	definition (name: "Lumi Switch 1", namespace: "lumivietnam", author: "Lumi Vietnam") {
		capability "Actuator"
		//capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
        
		attribute "switch1", "string"

        command "on1"
        command "off1"
        
		fingerprint profileId: "0104", deviceId: "0100", inClusters: "0000, 0003, 0006", outClusters: "0000", manufacturer: "Lumi Vietnam", model: "LM-SZ1"
	}

	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off 1": "on/off: 1"
		reply "zcl on-off 0": "on/off: 0"
	}

	tiles {
        standardTile("switch1", "device.switch1", width: 2, height: 2, canChangeIcon: true) {
            state "on1", label: "SW1", action: "off1", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "off1"
            state "off1", label: "SW1", action: "on1", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on1"
        }
		standardTile("onAll", "device.onAll", decoration: "flat") {
			state "default", label: 'On', action: "on1",  icon: "st.switches.light.on", backgroundColor: "#ffffff"
		}
		standardTile("offAll", "device.offAll", decoration: "flat") {
			state "default", label: 'Off', action: "off1", icon: "st.switches.light.off", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
		main "switch1"
		details(["switch1", "onAll", "offAll", "refresh" ])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    def finalResult = isKnownDescription(description)
	if (finalResult != "false") {
		log.info finalResult
        if (finalResult.type == "update") {
			log.info "$device updates: ${finalResult.value}"
		}
		else if (finalResult.type == "switch") {
            if (finalResult.srcEP == "01") {
            	state.sw1 = finalResult.value;
                sendEvent(name: "switch1", value: finalResult.value=="on"?"on1":"off1")
            }
        }
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug parseDescriptionAsMap(description)
	}
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
    //Config binding and report for each endpoint
	[
   		"zdo bind 0x${device.deviceNetworkId} 1 1 0x0006 {${device.zigbeeId}} {}", "delay 200",
    	"zcl global send-me-a-report 0x0006 0 0x10 0 600 {01}",
		"send 0x${device.deviceNetworkId} 1 1"
    ]
}

def refresh() {
	log.debug "Executing 'refresh'"
    //Read Attribute On Off Value
	[
        "st rattr 0x${device.deviceNetworkId} 1 0x0006 0"
	]
}

def on1() {
	log.debug "Executing 'on1' 0x${device.deviceNetworkId} endpoint 1"
    "st cmd 0x${device.deviceNetworkId} 1 0x0006 1 {}"
}

def off1() {
	log.debug "Executing 'off1' 0x${device.deviceNetworkId} endpoint 1"
	"st cmd 0x${device.deviceNetworkId} 1 0x0006 0 {}"
}

def isKnownDescription(description) {
	if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
		def descMap = parseDescriptionAsMap(description)
		if (descMap.cluster == "0006" || descMap.clusterId == "0006") {
			isDescriptionOnOff(descMap)
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
		return	[type: "switch", value : switchValue, srcEP : descMap.sourceEndpoint]
	}
	else {
		return "false"
	}
}