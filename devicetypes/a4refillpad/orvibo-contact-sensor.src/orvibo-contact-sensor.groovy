/**
 *  Orvibo Contact Sensor
 * 
 *  Copyright Wayne Man 2016
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
 *  Orvibo Contact Sensor Device Type
 *  Battery levels updates periodically, for an instant update press physical button on sensor once
 *  30/04/2016 fixed fingerprint
 *  09/05/2016 added heartbeat to help track if sensor is alive (recommend using a devicecheck smartapp)
 *  v1.2
 *  extra tile formatting
 *  logging of extra information including lastopened
 *  improved heartbeat date tracking
 *  added force override of open and closed states, removed non working refresh
 *  conforming to 2017 Smartthings colour standards
 *  added experimental health check as worked out by rolled54.Why
 *  compensated for yet more smartthings font size inconsistencies
 */
metadata {
	definition (name: "Orvibo Contact Sensor", namespace: "a4refillpad", author: "Wayne Man") {
		capability "Contact Sensor"
		capability "Sensor"
        capability "Battery"
		capability "Refresh"
        capability "Configuration"
        capability "Health Check"

		command "enrollResponse"
   		command "resetClosed"
   		command "resetOpen"

		attribute "lastCheckin", "String"
		attribute "lastOpened", "String"
        
		fingerprint inClusters: "0000,0001,0003,0500", manufacturer: "\u6B27\u745E", model: "75a4bfe8ef9c4350830a25d13e3ab068"

	}

	// simulator metadata
	simulator {
		// status messages
		status "open":   "zone report :: type: 19 value: 0031"
		status "closed": "zone report :: type: 19 value: 0030"
	}

    	tiles(scale: 2) {
		multiAttributeTile(name:"contact", type: "lighting", width: 6, height: 4) {
			tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
           		attributeState("open", label:'open', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
            	attributeState("closed", label:'closed', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc")   
 			}
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
    			attributeState("default", label:'Last Update: ${currentValue}',icon: "st.Health & Wellness.health9")
            }
		}
      	valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
	  	}  	        
 		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
		standardTile("configure", "device.configure", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
	  	}       
      	standardTile("icon", "device.refresh", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label:'Last Opened:', icon:"st.Entertainment.entertainment15"
      	}
		valueTile("lastopened", "device.lastOpened", decoration: "flat", inactiveLabel: false, width: 4, height: 1) {
			state "default", label:'${currentValue}'
		}
	  	standardTile("resetClosed", "device.resetClosed", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", action:"resetClosed", label: "Override Close", icon:"st.contact.contact.closed"
	  	}
		standardTile("resetOpen", "device.resetOpen", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "default", action:"resetOpen", label: "Override Open", icon:"st.contact.contact.open"
	  	}
      
      
      main (["contact"])
      details(["contact","battery","icon","lastopened","resetClosed","resetOpen"])
	}
 
}

// Parse incoming device messages to generate events
def parse(String description) {
	def name = null
	def value = description
   	def descriptionText = null
   	def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    log.debug "Parsing: ${description}"
    Map map = [:]

	List listMap = []
	List listResult = []
  
    
	if (zigbee.isZoneType19(description)) {
		name = "contact"
		value = zigbee.translateStatusZoneType19(description) ? "open" : "closed"
        sendEvent(name: "lastOpened", value: now)
	} else if(description?.startsWith("read attr -")) {
    	map = parseReportAttributeMessage(description)
    }
	
	def result = createEvent(name: name, value: value)
	log.debug "Parse returned ${result?.descriptionText}"
//  send event for heartbeat    
    sendEvent(name: "lastCheckin", value: now)
    
	listResult << result
    
   	if (listMap) {
        for (msg in listMap) {
            listResult << createEvent(msg)
        }
    }
    else if (map) {
        listResult << createEvent(map)
    }

	log.debug "Parse returned ${result?.descriptionText}"
	return listResult
} 

private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) {
		map, param -> def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	Map resultMap = [:]

	log.info "IN parseReportAttributeMessage()"
	log.debug "descMap ${descMap}"

	switch(descMap.cluster) {
		case "0001":
			log.debug "Battery status reported"

			if(descMap.attrId == "0021") {
				resultMap.name = 'battery'
                resultMap.value = (convertHexToInt(descMap.value) / 2)
                log.debug "Battery Percentage convert to ${resultMap.value}%"
			}
			break
		default:
			log.info descMap.cluster
			log.info "cluster1"
			break
	}

	log.info "OUT parseReportAttributeMessage()"
	return resultMap
}


def configure() {
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "${device.deviceNetworkId}"
    def endpointId = 1
    log.debug "${device.zigbeeId}"
    log.debug "${zigbeeEui}"
	def configCmds = [
			//battery reporting and heartbeat
			"zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 1 {${device.zigbeeId}} {}", "delay 200",
			"zcl global send-me-a-report 1 0x20 0x20 600 3600 {01}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 2000",


			// Writes CIE attribute on end device to direct reports to the hub's EUID
			"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 1", "delay 2000",
	]

	log.debug "configure: Write IAS CIE"
	return configCmds
}

def enrollResponse() {
	log.debug "Enrolling device into the IAS Zone"
	[
			// Enrolling device into the IAS Zone
			"raw 0x500 {01 23 00 00 00}", "delay 200",
			"send 0x${device.deviceNetworkId} 1 1", "delay 2000",
	]
}

/*
def refresh() {
	log.debug "refresh requested"
    zigbee.readAttribute(0x0001, 0x0021)
}
*/

def refresh() {
	log.debug "refreshing"
    [
        "st rattr 0x${device.deviceNetworkId} 1 21 0", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 1 21", "delay 250",
        "st rattr 0x${device.deviceNetworkId} 1"
    ]
}

def reset() {
	sendEvent(name:"contact", value:"closed")
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


def resetClosed() {
	sendEvent(name:"contact", value:"closed")
} 

def resetOpen() {
	sendEvent(name:"contact", value:"open")
}

def installed() {
// Device wakes up every 1 hour, this interval allows us to miss one wakeup notification before marking offline
	log.debug "Configured health checkInterval when installed()"
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def updated() {
// Device wakes up every 1 hours, this interval allows us to miss one wakeup notification before marking offline
	log.debug "Configured health checkInterval when updated()"
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}