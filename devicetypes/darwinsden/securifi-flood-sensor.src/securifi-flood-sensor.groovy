/**
 *  Securifi Flood Sensor - AL-WTD01, SZ-WTD01
 *
 *  For questions or to provide feedback on this device handler, 
 *  please visit: 
 *
 *      darwinsden.com
 *
 *  Copyright 2016 DarwinsDen.com
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
 *	Author: Darwin@DarwinsDen.com
 *	Date: 2016-04-02
 *
 *	Changelog:
 *
 *	0.10 (04/02/2016) -	Beta Release
 *
 */
 metadata {
	definition (name: "Securifi Flood Sensor", namespace: "darwinsden", author: "darwin@darwinsden.com") {
		capability "Water Sensor"
		capability "Sensor"
        capability "Configuration"

        attribute "tamperSwitch","ENUM",["open","closed"]

        command "enrollResponse"

		fingerprint endpointId: '08', profileId: '0104', inClusters: "0000,0003,0500", outClusters: "0003"
	}

	// simulator metadata
	simulator {
		status "active": "zone report :: type: 19 value: 0031"
		status "inactive": "zone report :: type: 19 value: 0030"
	}

	// UI tile definitions
	tiles {
    	tiles(scale: 2) {
			multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
				tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
					attributeState "dry", label: "Dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
					attributeState "wet", label: "Wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
				}
			}
        }

        standardTile("tamperSwitch", "device.tamperSwitch", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}        
		main (["water"])
		details(["water","tamperSwitch"])
	}
}

def configure() {
	log.debug("** PIR02 ** configure called for device with network ID ${device.deviceNetworkId}")

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def configCmds = [
    	"zcl global write 0x500 0x10 0xf0 {${zigbeeId}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        "zcl global send-me-a-report 1 0x20 0x20 0x3600 0x3600 {01}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

		"zdo bind 0x${device.deviceNetworkId} 1 1 0x001 {${device.zigbeeId}} {}", "delay 1500",

        "raw 0x500 {01 23 00 00 00}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
	]
    return configCmds // send refresh cmds as part of config
}
def enrollResponse() {
	log.debug "Sending enroll response"
    [	
	  "raw 0x500 {01 23 00 00 00}", "delay 200",
      "send 0x${device.deviceNetworkId} 1 1"
    ]
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug("** PIR02 parse received ** ${description}")
    def result = []        
	Map map = [:]

    if (description?.startsWith('zone status')) {
	    map = parseIasMessage(description)
    }

	log.debug "Parse returned $map"
    map.each { k, v ->
    	log.debug("sending event ${v}")
        sendEvent(v)
    }

//	def result = map ? createEvent(map) : null

    if (description?.startsWith('enroll request')) {
    	List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        result = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return result
}

private Map parseIasMessage(String description) {
    List parsedMsg = description.split(' ')
    String msgCode = parsedMsg[2]

    Map resultMap = [:]
    switch(msgCode) {
        case '0x0038': // Dry
            log.debug 'Detected Dry'
            resultMap["water"] = [name: "water", value: "dry"]
            resultMap["tamperSwitch"] = getContactResult("closed")            
            break

        case '0x0039': // Wet
            log.debug 'Detected Water'
            //resultMap["moisture"] = [name: "moisture", value: "flood"]
            resultMap["water"] = [name: "water", value: "wet"]
            resultMap["tamperSwitch"] = getContactResult("closed")            
            break

        case '0x0032': // Tamper Alarm
        	log.debug 'Detected Tamper'
            resultMap["water"] = [name: "water", value: "active"]
            resultMap["tamperSwitch"] = getContactResult("open")            
            break

        case '0x0034': // Supervision Report
        	log.debug 'No water with tamper alarm'
            resultMap["water"] = [name: "water", value: "inactive"]
            resultMap["tamperSwitch"] = getContactResult("open")            
            break

        case '0x0035': // Restore Report
        	log.debug 'Water with tamper alarm'
            resultMap["water"] = [name: "water", value: "wet"]
            resultMap["tamperSwitch"] = getContactResult("open") 
            break

        case '0x0036': // Trouble/Failure
        	log.debug 'msgCode 36 not handled'
            break
    }
    return resultMap
}

private Map getContactResult(value) {
	log.debug "Tamper Switch Status ${value}"
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	return [
		name: 'tamperSwitch',
		value: value,
		descriptionText: descriptionText
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