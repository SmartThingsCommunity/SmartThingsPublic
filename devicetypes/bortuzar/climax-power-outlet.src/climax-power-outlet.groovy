/**
 *  Climax PSM-29ZBSR - Zigbee - Power Outlet Switch and Energy Meter
 *
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
 *  Thanks to SmartThings and its community!
 * 
 *  Product Page: http://www.climax.com.tw/psm29zb-zb.php
 *  Developer manual https://fccid.io/document.php?id=2553357
 *
 */
metadata {
	definition (name: "Climax Power Outlet", namespace: "bortuzar", author: "bortuzar") {
	
    	capability "Actuator"
        capability "Configuration"
        capability "Refresh"
		capability "Sensor"
        capability "Switch"
        capability "Power Meter"

		fingerprint endpointId: "0x0A", profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0702", outClusters: "0000"
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

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		valueTile("power", "device.power", inactiveLabel: false, decoration: "flat") {
			state "power", label: '${currentValue} W'
		}
        
        //standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
		//	state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		//}

		
		main(["switch", "power"])
		details(["switch", "power", "refresh"])
	}
}


// Parse incoming device messages to generate events
def parse(String description) {

	log.debug "Parse Method Called"
	log.trace description
    
	if (description?.startsWith("catchall:")) {
		def msg = zigbee.parse(description)
		log.trace "data: $msg.data"

	} else if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
        log.debug "Desc Map:"
        log.debug descMap
		
        if (descMap.cluster == "0006" && descMap.attrId == "0000") {
                name = "switch"
                value = descMap.value.endsWith("01") ? "on" : "off"
                def result = createEvent(name: name, value: value)
                log.debug "Parse returned ${result?.descriptionText}"
                return result
        } else if(descMap.cluster =="0702" && descMap.attrId == "0400") {
            
            def value = convertHexToInt(descMap.value[-4..-1])/10 
            // Reading the last 4 characters of the string...Maybe 4 are needed. Needs further test.
            // Dividing by 10 as the Divisor is 10000 and unit is kW for the device. AttrId: 0302 and 0300. Simplifying to 10
            log.debug value
            def name = "power"
            def result = createEvent(name: name, value: value)
            log.debug "Parse returned ${result?.descriptionText}"
            return result 
        }
    } else {
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	}
}

def on() {
	log.debug "On Method called"
	sendEvent(name: "switch", value: "on")
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
	log.debug "Off Method called"
	sendEvent(name: "switch", value: "off")
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def refresh() {
  	log.debug "Refresh Method called";	
    //read attribute 1024Decimal, translates to 400Hex.
    [
		"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",    
        "st rattr 0x${device.deviceNetworkId} 1 0x0702 1024", , "delay 500" 
	]
}


def configure() {

	log.debug "Configure Method called"

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	
	def configCmds = [	
  
        //Switch Reporting
        "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1000",
        
        //bing to cluster 0x006. Switch On-Off
        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
        
        //bind to cluster 0x702. Power Consumption
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0702 {${device.zigbeeId}} {}", "delay 500",
        
	]
    return configCmds + refresh() // send refresh cmds as part of config
}


def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
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
