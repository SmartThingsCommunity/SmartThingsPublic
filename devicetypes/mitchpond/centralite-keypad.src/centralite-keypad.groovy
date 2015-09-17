/**
 *  Centralite Keypad
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
	definition (name: "Centralite Keypad", namespace: "mitchpond", author: "Mitch Pond") {
		capability "Battery"
		capability "Button"
        capability "Configuration"
		capability "Sensor"
        capability "Refresh" //TODO: can be removed before publishing
        
        fingerprint endpointId: "01", profileId: "0104", deviceId: "0401", inClusters: "0000,0001,0003,0020,0402,0500,0B05", outClusters: "0019,0501"
	}

	tiles {
		// TODO: define your main and details tiles here
        valueTile("battery", "device.battery", decoration: "flat") {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) { //TODO: remove before publishing
        	state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
    	}
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
        	state "default", action:"configuration.configure", icon:"st.secondary.configure"
    	}
        main ("battery")
        details (["configure","refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
    
    def results = [];
    
	//------Miscellaneous Zigbee message------//
	if (description?.startsWith('catchall:')) {
    	//log.debug zigbee.parse(description)
		results = zigbee.parse(description)
        if (results?.command == 0x07) log.debug 'Heartbeat??'
        else if (results?.command == 0x00) {
        	log.debug 'Received arm request with keycode: '+results.data
            List cmds = ["st cmd 0x${device.deviceNetworkId} 1 0x501 0x07 {0000}"
            				//"raw 0x501 {02 00 00}", "delay 200",
							//"send 0x${device.deviceNetworkId} 1 1"
                        ]
            results = cmds?.collect { new physicalgraph.device.HubAction(it) }
        }
        else log.trace results?.command
	}
    //------IAS Zone Enroll request------//
    else if (description?.startsWith('enroll request')) {
		List cmds = enrollResponse()
		log.debug "enroll response: ${cmds}"
		results = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
    //------Read Attribute response------//
    else if (description?.startsWith('read attr -')) {
		results = parseReportAttributeMessage(description)
	}
    
	return results
}

def configure() {
	String hubZigbeeId = swapEndianHex(device.hub.zigbeeId)
	def cmd = [
    //------IAS Zone/CIE setup------//
    "zcl global write 0x500 0x10 0xf0 {${hubZigbeeId}}", "delay 200",
	"send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    
    //------Set up binding------//
    "zdo bind 0x${device.deviceNetworkId} 1 1 0x500 {${device.zigbeeId}} {}", "delay 500",
    "zdo bind 0x${device.deviceNetworkId} 1 1 0x501 {${device.zigbeeId}} {}", "delay 500",
	"zdo bind 0x${device.deviceNetworkId} 1 1 1 {${device.zigbeeId}} {}", "delay 500",
	"st rattr 0x${device.deviceNetworkId} 1 1 0x20"
	]
    log.debug location.id
    //subscribe(location,locationEventParser)
    cmd
}

def refresh() {
    ["st rattr 0x${zigbee.deviceNetworkId} 0x${zigbee.endpointId} 0 4", "delay 200",
    "st rattr 0x${zigbee.deviceNetworkId} 0x${zigbee.endpointId} 0 5"]
}

//Sends IAS Zone Enroll response
def enrollResponse() {
	log.debug "Sending enroll response"
	[	
	"raw 0x500 {01 23 00 00 00}", "delay 200",
	"send 0x${device.deviceNetworkId} 1 1"
	]
}

private armRequestResponse() {}

private parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	//log.debug "Desc Map: $descMap"

	def results = []
    
	if (descMap.cluster == "0001" && descMap.attrId == "0020") {
		log.debug "Received battery level report"
		results = createEvent(getBatteryResult(Integer.parseInt(descMap.value, 16)))
	}

	return results
}

private locationEventParser(evt) {
	if (evt?.name == "intrusionModeChange") {
    	if (evt.value.startsWith("away")) log.debug "Caught SHM armed/away change."
        else if (evt.value.startsWith("off")) log.debug "Caught SHM disarm."
        else if (evt.value.startsWith("stay")) log.debug "Caught SHM armed/stay change."
        else log.debug "Something else happened..."
    }
}

//Converts the battery level response into a percentage to display in ST
//and creates appropriate message for given level

private getBatteryResult(rawValue) {
	def linkText = getLinkText(device)

	def result = [name: 'battery']

	def volts = rawValue / 10
	def descriptionText
	if (volts > 3.5) {
		result.descriptionText = "${linkText} battery has too much power (${volts} volts)."
	}
	else {
		def minVolts = 2.4
		def maxVolts = 3.0
		def pct = (volts - minVolts) / (maxVolts - minVolts)
		result.value = Math.min(100, (int) pct * 100)
		result.descriptionText = "${linkText} battery was ${result.value}%"
	}

	return result
}


//------Utility methods------//
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