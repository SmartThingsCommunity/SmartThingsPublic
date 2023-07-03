/**
 *  Iris Care Pendant
 *	
 *  Copyright 2015 Mitch Pond
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
	definition (name: "Iris Care Pendant", namespace: "mitchpond", author: "Mitch Pond") {
    	capability "Sensor"
    	capability "Battery"
		capability "Configuration"
        capability "Button"
        
        command "enrollResponse"

		fingerprint endpointId: "01", inClusters: "0000,0001,0003,0020,0500", outClusters: "0003,0019", manufacturer: "CentraLite", model: "3455-L"
	}

	preferences {}
 
	tiles(scale: 2) {
    	standardTile("button", "device.button", decoration: "flat", width: 2, height:2) {
        	state "default", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
            state "pushed", backgroundColor: "#79b821"
        }
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main (["battery"])
		details(["button","battery"])
	}
}
 
def parse(String description) {
	//log.debug "description: $description"
    
	def results = []
	if (description?.startsWith('catchall:')) {
		results = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr -')) {
		results = parseReportAttributeMessage(description)
	}
    else if (description?.startsWith('zone status')) {
    	results = parseIasMessage(description)
    }
    
    if (description?.startsWith('enroll request')) {
    	List cmds = enrollResponse()
        log.debug "enroll response: ${cmds}"
        results = cmds?.collect { new physicalgraph.device.HubAction(it) }
    }
    return results
}
 
private Map parseCatchAllMessage(String description) {
    Map resultMap = [:]
    def cluster = zigbee.parse(description)
    if (shouldProcessMessage(cluster)) {
        switch(cluster.clusterId) {
            case 0x0001:
            	resultMap = getBatteryResult(cluster.data.last())
                break
        }
    }

    return resultMap
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
 
private parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}

	def results = []
    
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		log.debug "Received battery level report"
		results = createEvent(getBatteryResult(Integer.parseInt(descMap.value, 16)))
	}

	return results
}

private parseIasMessage(String description) {
	List parsedMsg = description.split(' ')
	String msgCode = parsedMsg[2]
	int status = Integer.decode(msgCode)
	def linkText = getLinkText(device)

	def results = []
	//log.debug(description)
	if (status & 0b00000010) {results << createEvent(getButtonResult('pushed'))}
	else if (~status & 0b00000010) results << createEvent(getButtonResult('released'))

	if (status & 0b00000100) {
    	//tampered
	}
	else if (~status & 0b00000100) {
		//not tampered
	}
	
	if (status & 0b00001000) {
	}
	else if (~status & 0b00001000) {
		//log.debug "${linkText} battery OK"
	}
	//log.debug results
	return results
}

private Map getBatteryResult(rawValue) {
	log.debug 'Battery'
	def linkText = getLinkText(device)
    
    def result = [
    	name: 'battery'
    ]
    
	def volts = rawValue / 10
	def descriptionText
	if (volts > 3.5) {
		result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
	}
	else {
		def minVolts = 2.1
    	def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, (int) pct * 100)
		result.descriptionText = "${linkText} battery was ${result.value}%"
	}

	return result
}

private Map getButtonResult(value) {
	//log.debug 'Button Status'
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'pushed' ? 'pushed' : 'released'}"
	return [
		name: 'button',
		value: value,
        data: [buttonNumber: 1],
		descriptionText: descriptionText,
        displayed: true,
        isStateChange: true
	]
}

def configure() {

	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def configCmds = [
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 100",
		"send 0x${device.deviceNetworkId} 1 1", "delay 500",

		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 1 {${device.zigbeeId}} {}", "delay 200",
		"zcl global send-me-a-report 1 0x20 0x20 3600 21600 {01}",		//checkin time 6 hrs
		"send 0x${device.deviceNetworkId} 1 1", "delay 500",
        "st rattr 0x${device.deviceNetworkId} 1 1 0x20"
	]
    return configCmds
}

def enrollResponse() {
	log.debug "Sending enroll response"
	String zigbeeEui = swapEndianHex(device.hub.zigbeeEui)
	[
		//Resending the CIE in case the enroll request is sent before CIE is written
		"zcl global write 0x500 0x10 0xf0 {${zigbeeEui}}", "delay 200",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500",
		//Enroll Response
		"raw 0x500 {01 23 00 00 00}",
		"send 0x${device.deviceNetworkId} 1 1", "delay 200"
	]
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
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