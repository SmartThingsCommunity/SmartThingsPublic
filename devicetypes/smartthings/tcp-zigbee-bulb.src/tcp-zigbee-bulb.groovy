/**
 *  TCP-Zigbee-Bulb.groovy
 *
 *  Copyright 2015 SmartThings/WackWare
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
	definition (name: "TCP Zigbee Bulb", namespace: "smartthings", author: "tcpi") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "Refresh"
        capability "Switch Level"
        capability "Configuration"
        
		fingerprint profileId: "0104", inClusters: "0000,0004,0003,0006,0008,0005", outClusters: "0019"
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
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#79b821"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		

		main(["switch"])
		details(["switch", "level", "levelSliderControl", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	//log.trace description
	if (description?.startsWith("catchall:")) {
		//def msg = zigbee.parse(description)
		//log.trace msg
		//log.trace "data: $msg.data"
        if(description?.endsWith("0100"))
        {
        	def result = createEvent(name: "switch", value: "on")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }
        if(description?.endsWith("0000"))
        {
        	def result = createEvent(name: "switch", value: "off")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }
        if (description?.startsWith("read attr")) 
        {
    		log.debug description[-2..-1]
        	def i = Math.round(convertHexToInt(description[-2..-1]) / 256 * 100 )
        
			def result = createEvent( name: "level", value: i )
            sendEvent( name: "setLevel", value: i )
        	log.debug "Parse returned ${result?.descriptionText}"
        	return result
    	}
	}
}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
	"st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
	"st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def refresh() {
    [
	"st rattr 0x${device.deviceNetworkId} 1 6 0", 
    "delay 1500",
    "st rattr 0x${device.deviceNetworkId} 1 8 0"
    ]
}

def setLevel(value) {
	log.trace "setLevel($value)"
	def cmds = []

	if (value == 0) {
		sendEvent(name: "switch", value: "off")
        sendEvent(name: "level", value: value)
        sendEvent( name: "switch.setLevel", value:value )
		cmds << "st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
	}
	else if (device.latestValue("switch") == "off") {
		sendEvent(name: "switch", value: "on")
		sendEvent(name: "level", value: value)
        sendEvent( name: "switch.setLevel", value:value )
		def level = new BigInteger(Math.round(value * 255 / 100).toString()).toString(16)        
        cmds << "st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
        cmds << "st cmd 0x${device.deviceNetworkId} 1 8 4 {${level} 0600}"
	}
    else
    {
		sendEvent(name: "level", value: value)
        sendEvent( name: "switch.setLevel", value:value )
		def level = new BigInteger(Math.round(value * 255 / 100).toString()).toString(16)
		cmds << "st cmd 0x${device.deviceNetworkId} 1 8 4 {${level} 0600}"
	}
    
	//log.debug cmds
	cmds
}

def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting and Bindings."
	def configCmds = [	
  
        //Switch Reporting
        "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1000",
        
        //Level Control Reporting
        "zcl global send-me-a-report 8 0 0x20 5 3600 {0010}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
        
        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} 1 1 8 {${device.zigbeeId}} {}", "delay 500",
	]
    return configCmds + refresh() // send refresh cmds as part of config
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