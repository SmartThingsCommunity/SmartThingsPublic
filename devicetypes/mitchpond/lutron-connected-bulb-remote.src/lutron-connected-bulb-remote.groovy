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
 */
metadata {
	definition (name: "Lutron Connected Bulb Remote", namespace: "mitchpond", author: "Mitch Pond") {
		capability "Button"
		capability "Configuration"

		fingerprint manufacturer: "Lutron", profileId: "C05E", endpointId: "01", inClusters: "0000,1000,FF00,FC44", outClusters: "1000,0003,0006,0008,0004,0005,0000,FF00"
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.button", width: 2, height: 2, canChangeIcon: true) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
            state "pushed", label:'${name}', icon:"st.unknown.zwave.remote-controller", backgroundColor:"#79b821"
		}
        standardTile("configure", "device.configure", decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main "button"
		details(["button","configure"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.info description
	if (description?.startsWith("catchall:")) {
		def msg = zigbee.parse(description)
		log.trace msg
		log.trace "data: $msg.data"
	}
	else {
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	}
}

// Commands to device

def configure() {
	String hubZigbeeId = swapEndianHex(device.hub.zigbeeId)
	[
    //"zdo bind 0x${device.deviceNetworkId} 1 1 3 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 0x01 0xFF 0x0006 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 0x01 0xFF 0x0008 {${device.zigbeeId}} {}", "delay 200",
    //"zdo bind 0x${device.deviceNetworkId} 1 1 0x08 {${device.zigbeeId}} {}", "delay 200",
    //"zdo bind 0x${device.deviceNetworkId} 1 1 5 {${device.zigbeeId}} {}", "delay 200",
    //"zdo bind 0x${device.deviceNetworkId} 1 1 0 {${device.zigbeeId}} {}", "delay 200",
    //"zdo bind 0x${device.deviceNetworkId} 1 1 0x1000 {${device.zigbeeId}} {}", "delay 200",
    //"zdo bind 0x${device.deviceNetworkId} 1 1 0xfc44 {${device.zigbeeId}} {}", "delay 200",
    //"zdo bind 0x${device.deviceNetworkId} 1 1 0xFF00 {${device.zigbeeId}} {}", "delay 200",
    //"zdo bind 0x${device.deviceNetworkId} 1 1 4 {${device.zigbeeId}} {}", "delay 200",
    //"st rattr 0x${device.deviceNetworkId} 1 0x0000 0x0007",
    //"st cmd 0x${device.deviceNetworkId} 1 0xFF00 1 {0000}",
    //"raw 0x0006 {0c 0000 FF}",
    //"zcl global send-me-a-report 0x0006 0x0000 0x10 0 0 {}", "delay 200",
    //"send 0x${device.deviceNetworkId} 1 1",
    //"zcl global discover 0xFF00 0x00 20","delay 200",
    //"send 0x${device.deviceNetworkId} 1 1"
	]

}



private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
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