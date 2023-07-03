/**
 *  Xiaomi Aqara Light Switch x (Zigbee)
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
 *  Based on original DH by a4refillpad 2017
 *  Based on original DH by Eric Maycock 2015 and Rave from Lazcad
 *  change log:
 *  modified to allow button capability
 *
 *  Ver 1.0  -  9-12-2017
 *    Converted to support button presses with Xiaomi Zigbee Aqara Light Switch (Single Switch)
 *
 */
metadata {
	definition (name: "Xiaomi Aqara Light Switch", namespace: "ericyew", author: "Eric Yew") {	
//    	capability "Battery"
		capability "Button"
//		capability "Holdable Button"
		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Configuration"
		capability "Sensor"
		capability "Refresh"
        
		attribute "lastPress", "string"
		attribute "batterylevel", "string"
		attribute "lastCheckin", "string"
        
    	fingerprint endpointId: "01", inClusters: "0000,FFFF,0006", outClusters: "0000,0004,FFFF"
	}
    
    	simulator {
  		status "button 1 pressed": "on/off: 1"
    	}
    
    	preferences{
//    		input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"",
//        		defaultValue: 4, displayDuringSetup: false)
    	}

	tiles(scale: 2) {

		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
           			attributeState("on", label:' push', action: "momentary.push", backgroundColor:"#53a7c0")
            			attributeState("off", label:' push', action: "momentary.push", backgroundColor:"#ffffff", nextState: "on")   
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

//        	standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
//			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
//		}
        
		main (["switch"])
			details(["switch", "refresh", "battery"]) //, "configure"
	}
}

def parse(String description) {
	log.debug "Parsing '${description}'"

	// Send event for heartbeat    
  	def now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
  	sendEvent(name: "lastCheckin", value: now)

 	def results = []
  	if (description?.startsWith('on/off: '))
        	results = parseCustomMessage(description)
 	if (description?.startsWith('catchall:')) 
		results = parseCatchAllMessage(description)
        
  	return results;
}

def configure(){
    	[
    		"zdo bind 0x${device.deviceNetworkId} 1 2 0 {${device.zigbeeId}} {}", "delay 5000",
    		"zcl global send-me-a-report 2 0 0x10 1 0 {01}", "delay 500",
    		"send 0x${device.deviceNetworkId} 1 2"
    	]
}

def refresh(){
	"st rattr 0x${device.deviceNetworkId} 1 2 0"
    	"st rattr 0x${device.deviceNetworkId} 1 0 0"
	log.debug "refreshing"
    	sendEvent(name: 'numberOfButtons', value: 1)
    	//createEvent([name: 'batterylevel', value: '100', data:[buttonNumber: 1], displayed: false])
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	log.debug cluster
	log.debug "${cluster.clusterId}"
	if (cluster) {
		switch(cluster.clusterId) {
			case 0x0000:
			resultMap = getBatteryResult(cluster.data.last())
			break

			case 0xFC02:
			log.debug 'ACCELERATION'
			break

			case 0x0402:
			log.debug 'TEMP'
				// temp is last 2 data values. reverse to swap endian
				String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
				def value = getTemperature(temp)
				resultMap = getTemperatureResult(value)
				break
		}
	}

	return resultMap
}

private Map getBatteryResult(rawValue) {
	log.debug 'Battery'
	def linkText = getLinkText(device)

	log.debug rawValue
    
    int battValue = 100
     
    def maxbatt = 100

	if (battValue > maxbatt) {
				battValue = maxbatt
    }

	def result = [
		name: 'battery',
		value: battValue,
        unit: "%",
        isStateChange:true,
        descriptionText : "${linkText} battery was ${battValue}%"
	]
    
    log.debug result.descriptionText
    state.lastbatt = new Date().time
    return createEvent(result)
}

private Map parseCustomMessage(String description) {
	if (description?.startsWith('on/off: ')) {
    	if (description == 'on/off: 1') 		//button pushed
    		return push()
	}
}

//Need to reverse array of size 2
private byte[] reverseArray(byte[] array) {
    byte tmp;
    tmp = array[1];
    array[1] = array[0];
    array[0] = tmp;
    return array
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

def push() {
//    log.debug "App Button Pressed"
//	sendEvent(name: "switch", value: "on", isStateChange: true, displayed: false)
//	sendEvent(name: "switch", value: "off", isStateChange: true, displayed: false)
//	sendEvent(name: "momentary", value: "pushed", isStateChange: true)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$device.displayName button 1 was pushed", isStateChange: true)
}

def on() {
	push()
}

def off() {
	push()
}