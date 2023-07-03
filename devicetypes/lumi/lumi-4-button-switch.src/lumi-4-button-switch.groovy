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
	definition (name: "Lumi 4 Button Switch", namespace: "lumi", author: "Thonv") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
        
		attribute "switch1", "string"
        attribute "switch2", "string"
        attribute "switch3", "string"
        attribute "switch4", "string"
        attribute "ConfigureButton", "string"
        attribute "switchAll", "string"

        command "on1"
        command "off1"
        command "on2"
        command "off2"
        command "on3"
        command "off3"
        command "on4"
        command "off4"
        command "on5"
        command "off5"
        command "onAll"
        command "offAll"
        
        fingerprint profileId: "0104", deviceId: "0100", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003", manufacturer: "Lumi", model: "LM-SZ4"
		fingerprint profileId: "0104", deviceId: "0100", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003", manufacturer: "Lumi R&D", model: "LM-SZ4"
	}


	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off 1": "on/off: 1"
		reply "zcl on-off 0": "on/off: 0"
	}

	tiles (scale: 2) {
        standardTile("switch1", "device.switch1", width: 3, height: 3, canChangeIcon: true) {
            state "on1", label: "SW1", action: "off1", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "off1"
            state "off1", label: "SW1", action: "on1", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on1"
        }
        standardTile("switch2", "device.switch2", width: 3, height: 3, canChangeIcon: true) {
            state "on2", label: "SW2", action: "off2", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "off2"
            state "off2", label: "SW2", action: "on2", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on2"
        }
        standardTile("switch3", "device.switch3", width: 3, height: 3, canChangeIcon: true) {
            state "on3", label: "SW3", action: "off3", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "off3"
            state "off3", label: "SW3", action:"on3", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on3"
        }
        standardTile("switch4", "device.switch4", width: 3, height: 3, canChangeIcon: true) {
            state "on4", label: "SW4", action: "off4", icon: "st.switches.light.on", backgroundColor: "#79b821", nextState: "off4"
            state "off4", label: "SW4", action:"on4", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "on4"
        }
        standardTile("switchAll", "device.switchAll", width: 2, height: 2, canChangeIcon: false) {
            state "onAll", label: "All", action: "offAll", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState: "offAll"
            state "offAll", label: "All", action: "onAll", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff", nextState: "onAll"
        }
		standardTile("onAll", "device.onAll", width: 2, height: 2, decoration: "flat") {
			state "default", label: 'On All', action: "onAll",  icon: "st.lights.multi-light-bulb-on", backgroundColor: "#ffffff"
		}
		standardTile("offAll", "device.offAll", width: 2, height: 2, decoration: "flat") {
			state "default", label: 'Off All', action: "offAll", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("ConfigureButton", "device.switch5", canChangeIcon: true) {
            state "on5", label: "high", action: "off5", icon: "st.secondary.tools", backgroundColor: "#79b821", nextState: "off5"
            state "off5", label: "low", action:"on5", icon: "st.secondary.tools", backgroundColor: "#ffffff", nextState: "on5"
        }
        
		main (["switchAll", "switch1", "switch2", "switch3", "switch4"])
		details(["switch1", "switch2", "switch3", "switch4", "onAll","offAll", "switchAll", "refresh"])
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
            else if (finalResult.srcEP == "03") {
            	state.sw2 = finalResult.value;    	
                sendEvent(name: "switch2", value: finalResult.value=="on"?"on2":"off2")
			}
            else if (finalResult.srcEP == "05") {
            	state.sw3 = finalResult.value;
                sendEvent(name: "switch3", value: finalResult.value=="on"?"on3":"off3")
			}
            else if (finalResult.srcEP == "07") {
            	state.sw4 = finalResult.value;
                sendEvent(name: "switch4", value: finalResult.value=="on"?"on4":"off4")
			}
            else if (finalResult.srcEP == "0F") {
            	state.sw4 = finalResult.value;
                sendEvent(name: "switch5", value: finalResult.value=="on"?"on5":"off5")
			}
            //update state for switchAll Tile
            if (state.sw1 == "off" && state.sw2 == "off" && state.sw3 == "off" && state.sw4 == "off") {
            	//log.debug "offalll"
            	sendEvent(name: "switchAll", value: "offAll")
            }
            else if (state.sw1 == "on" && state.sw2 == "on" && state.sw3 == "on" && state.sw4 == "on") {
            	//log.debug "onall"
           		sendEvent(name: "switchAll", value: "onAll")
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
		"send 0x${device.deviceNetworkId} 1 1", "delay 500",
        
    	"zdo bind 0x${device.deviceNetworkId} 3 1 0x0006 {${device.zigbeeId}} {}", "delay 200",
    	"zcl global send-me-a-report 0x0006 0 0x10 0 600 {01}",
		"send 0x${device.deviceNetworkId} 1 3", "delay 500",
        
    	"zdo bind 0x${device.deviceNetworkId} 5 1 0x0006 {${device.zigbeeId}} {}", "delay 200",
    	"zcl global send-me-a-report 0x0006 0 0x10 0 600 {01}",
		"send 0x${device.deviceNetworkId} 1 5", "delay 500",
        
    	"zdo bind 0x${device.deviceNetworkId} 7 1 0x0006 {${device.zigbeeId}} {}", "delay 200",
    	"zcl global send-me-a-report 0x0006 0 0x10 0 600 {01}",
		"send 0x${device.deviceNetworkId} 1 7"
    ]
}

def refresh() {
	log.debug "Executing 'refresh'"
    //Read Attribute On Off Value of each endpoint
	[
        "st rattr 0x${device.deviceNetworkId} 1 0x0006 0", "delay 200",
        "st rattr 0x${device.deviceNetworkId} 3 0x0006 0", "delay 200",
        "st rattr 0x${device.deviceNetworkId} 5 0x0006 0", "delay 200",
        "st rattr 0x${device.deviceNetworkId} 7 0x0006 0", "delay 200",
        "st rattr 0x${device.deviceNetworkId} 15 0x0006 0"
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

def on2() {
	log.debug "Executing 'on2' 0x${device.deviceNetworkId} endpoint 3"
    "st cmd 0x${device.deviceNetworkId} 3 0x0006 1 {}"
}

def off2() {
	log.debug "Executing 'off2' 0x${device.deviceNetworkId} endpoint 3"
	"st cmd 0x${device.deviceNetworkId} 3 0x0006 0 {}"
}

def on3() {
	log.debug "Executing 'on3' 0x${device.deviceNetworkId} endpoint 5"
    "st cmd 0x${device.deviceNetworkId} 5 0x0006 1 {}"
}

def off3() {
	log.debug "Executing 'off3' 0x${device.deviceNetworkId} endpoint 5"
	"st cmd 0x${device.deviceNetworkId} 5 0x0006 0 {}"
}

def on4() {
	log.debug "Executing 'on4' 0x${device.deviceNetworkId} endpoint 7"
    "st cmd 0x${device.deviceNetworkId} 7 0x0006 1 {}"
}

def off4() {
	log.debug "Executing 'off4' 0x${device.deviceNetworkId} endpoint 7"
	"st cmd 0x${device.deviceNetworkId} 7 0x0006 0 {}"
}

def on5() {
	log.debug "Executing 'on5' 0x${device.deviceNetworkId} endpoint 15"
    "st cmd 0x${device.deviceNetworkId} 15 0x0006 1 {}"
}

def off5() {
	log.debug "Executing 'off5' 0x${device.deviceNetworkId} endpoint 15"
	"st cmd 0x${device.deviceNetworkId} 15 0x0006 0 {}"
}

def onAll() {
	log.debug "Executing 'onAll' 0x${device.deviceNetworkId} endpoint 1 3 5 7"
	[
    	"st cmd 0x${device.deviceNetworkId} 1 0x0006 1 {}", "delay 200",
        "st cmd 0x${device.deviceNetworkId} 3 0x0006 1 {}", "delay 200",
        "st cmd 0x${device.deviceNetworkId} 5 0x0006 1 {}", "delay 200",
        "st cmd 0x${device.deviceNetworkId} 7 0x0006 1 {}"
    ]
}

def offAll() {
	log.debug "Executing 'offAll' 0x${device.deviceNetworkId} endpoint 1 3 5 7"
	[
    	"st cmd 0x${device.deviceNetworkId} 1 0x0006 0 {}", "delay 200",
        "st cmd 0x${device.deviceNetworkId} 3 0x0006 0 {}", "delay 200",
        "st cmd 0x${device.deviceNetworkId} 5 0x0006 0 {}", "delay 200",
        "st cmd 0x${device.deviceNetworkId} 7 0x0006 0 {}"
    ]
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