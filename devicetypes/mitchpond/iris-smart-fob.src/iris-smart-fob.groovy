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
		capability "Sensor"

		command "test"
        
        attribute "lastPress", "string"
        
		fingerprint endpointId: "01", profileId: "0104", inClusters: "0000,0001,0003,0007,0020,0B05", outClusters: "0003,0006,0019", model:"3450-L", manufacturer: "CentraLite"
	}
    
    simulator {
   		status "button 2 pressed": "catchall: 0104 0006 02 01 0140 00 6F37 01 00 0000 01 00"
        status "button 2 released": "catchall: 0104 0006 02 01 0140 00 6F37 01 00 0000 00 00"
    }
    
    preferences{
    	input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"",
        		defaultValue: 3, displayDuringSetup: false)
    }

	tiles(scale: 2) {
    	standardTile("button", "device.button", decoration: "flat", width: 2, height: 2) {
        	state "default", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"//, action: "test()"
        }
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main (["battery"])
		details(["button","battery"])
	}
}

def parse(String description) {
	//log.debug "Parsing '${description}'"
    def descMap = zigbee.parseDescriptionAsMap(description)
    //log.debug descMap
    
	def results = []
    if (description?.startsWith('catchall:'))
		results = parseCatchAllMessage(descMap)
	else if (description?.startsWith('read attr -'))
		results = parseReportAttributeMessage(descMap)
        
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
    if (descMap?.clusterId == "0006" && descMap?.command == "01") 		//button pressed
    	createPressEvent(descMap.sourceEndpoint as int)
    else if (descMap?.clusterId == "0006" && descMap?.command == "00") 	//button released
    	createButtonEvent(descMap.sourceEndpoint as int)
}

def parseReportAttributeMessage(descMap) {
	if (descMap?.cluster == "0001" && descMap?.attrId == "0020") createBatteryEvent(getBatteryLevel(descMap.value))
}

private createBatteryEvent(percent) {
	log.debug "Battery level at " + percent
	return createEvent([name: "battery", value: percent])
}

//this method determines if a press should count as a push or a hold and returns the relevant event type
private createButtonEvent(button) {
	def currentTime = now()
    def startOfPress = device.latestState('lastPress').date.getTime()
    def timeDif = currentTime - startOfPress
    def holdTimeMillisec = (settings.holdTime?:3).toInteger() * 1000
    
    if (timeDif < 0) 
    	return []	//likely a message sequence issue. Drop this press and wait for another. Probably won't happen...
    else if (timeDif < holdTimeMillisec) 
    	return createButtonPushedEvent(button)
    else 
    	return createButtonHeldEvent(button)
}

private createPressEvent(button) {
	return createEvent([name: 'lastPress', value: now(), data:[buttonNumber: button], displayed: false])
}

private createButtonPushedEvent(button) {
	log.debug "Button ${button} pushed"
	return createEvent([
    	name: "button",
        value: "pushed", 
        data:[buttonNumber: button], 
        descriptionText: "${device.displayName} button ${button} was pushed",
        isStateChange: true, 
        displayed: true])
}

private createButtonHeldEvent(button) {
	log.debug "Button ${button} held"
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