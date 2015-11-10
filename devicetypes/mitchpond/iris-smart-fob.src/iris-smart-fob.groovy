/**
 *  Iris Smart Fob
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
	definition (name: "Iris Smart Fob", namespace: "mitchpond", author: "Mitch Pond") {
		capability "Battery"
		capability "Button"
        capability "Configuration"
		capability "Presence Sensor"
		capability "Sensor"

		command "test"
        
		fingerprint profileId: "FC01", deviceId: "019A" //just out of curiousity....
		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0001,0003,0007,0020,0B05", outClusters: "0003,0006,0019", model:"3450-L", manufacturer: "CentraLite"
	}

	tiles(scale: 2) {
    	standardTile("button", "device.button", decoration: "flat", width: 2, height: 2) {
        	state "default", label: "#1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff", action: "test()"
            state "pushed", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#79b821"
        }
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
		  	state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
		  	state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ebeef2"
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main (["battery"])
		details(["button","battery","presence"])
	}
}

def parse(String description) {
	//log.debug "Parsing '${description}'"
    def descMap = zigbee.parseDescriptionAsMap(description)
    //log.debug descMap
    
	def results = []
    if (description?.startsWith('presence: 0')) results = createEvent([name: "presence", value: "not present"])
	else if (description?.startsWith('catchall:')) {
		results = parseCatchAllMessage(descMap)
	}
	else if (description?.startsWith('read attr -')) {
		results = parseReportAttributeMessage(descMap)
	}
	return results;
}

def configure(){
	[
	"zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 2 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 3 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 4 1 6 {${device.zigbeeId}} {}", "delay 200",
    "zdo bind 0x${device.deviceNetworkId} 1 1 3 {${device.zigbeeId}} {}", "delay 200", //just a test. remove before publishing
    
    "zcl global send-me-a-report 1 0x20 0x20 3600 86400 {01}", "delay 100", //battery report request
	"send 0x${device.deviceNetworkId} 1 1", "delay 200",
        
    "st rattr 0x${device.deviceNetworkId} 1 1 0x20"
    ]
}

def parseCatchAllMessage(descMap) {
	//log.debug descMap
    if (descMap?.clusterId == "0006" && descMap?.command == "00") 
    	createButtonPushedEvent(descMap.sourceEndpoint as int)
}

def parseReportAttributeMessage(descMap) {
	if (descMap?.cluster == "0001" && descMap?.attrId == "0020") createBatteryEvent(getBatteryLevel(descMap.value))
}

private createBatteryEvent(percent) {
	log.debug "Battery level at " + percent
	return createEvent([name: "battery", value: percent])
}

private createButtonPushedEvent(button) {
	return createEvent([
    	name: "button",
        value: "pushed", 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was pushed",
        isStateChange: true, 
        displayed: true])
}

private createButtonHeldEvent(button) {
	return createEvent([
    	name: "button",
        value: "held", 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was held",
        isStateChange: true])
}

private getBatteryLevel(rawValue) {
	def intValue = Integer.parseInt(rawValue,16)
	def min = 2.1
    def max = 3.0
    def vBatt = intValue / 10
    return ((vBatt - min) / (max - min) * 100) as int
}

// handle commands
def test() {
	log.debug "Test"
	zigbee.refreshData("0","4") + zigbee.refreshData("0","5") + zigbee.refreshData("1","0x0020")
}