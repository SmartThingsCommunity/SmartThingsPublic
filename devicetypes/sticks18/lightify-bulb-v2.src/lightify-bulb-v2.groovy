/**
 *  Osram Lightify Bulb (Color Temp Adjustable)
 *
 *  Copyright 2014 SmartThings
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
	definition (name: "Lightify Bulb v2", namespace: "Sticks18", author: "Scott Gibson") {

		capability "Actuator"
        capability "Configuration"
		capability "Refresh"
		capability "Switch"
		capability "Switch Level"
        
        command "setColorTemp"
        
        attribute "colorTemp", "string"
		attribute "kelvin", "string"
        attribute "bulbTemp", "string"
        
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019"
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
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
        controlTile("colorSliderControl", "device.colorTemp", "slider", height: 1, width: 2, inactiveLabel: false, range: "(2700..6500)") {
			state "colorTemp", action:"setColorTemp"
		}
		valueTile("kelvin", "device.kelvin", inactiveLabel: false, decoration: "flat") {
			state "kelvin", label: 'Temp ${currentValue}K'
		}
        valueTile("bulbTemp", "device.bulbTemp", inactiveLabel: false, decoration: "flat") {
			state "bulbTemp", label: '${currentValue}'
		}
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
			state "level", label: 'Level ${currentValue}%'
		}
		

		main(["switch"])
		details(["switch", "bulbTemp", "refresh", "levelSliderControl", "level", "colorSliderControl", "kelvin"])
	}
}

// Parse incoming device messages to generate events

def parse(String description) {
	log.trace description
	def msg = zigbee.parse(description)
    
    if (description?.startsWith("catchall:")) {
		
		log.trace msg
		log.trace "data: $msg.data"
        
        if(description?.endsWith("0100") ||description?.endsWith("1001"))
        {
        	def result = createEvent(name: "switch", value: "on")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }
        
        if(description?.endsWith("0000") || description?.endsWith("1000"))
        {
        	def result = createEvent(name: "switch", value: "off")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }
	}
    
    
   if (description?.startsWith("read attr")) {
   	
        Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
		}
		
        log.debug "Desc Map: $descMap"
 
        switch (descMap.cluster) {
        
        	case "0008":
            
        		log.debug description[-2..-1]
        		def i = Math.round(convertHexToInt(descMap.value) / 256 * 100 )
        		sendEvent( name: "level", value: i )
                sendEvent( name: "switch.setLevel", value: i) //added to help subscribers
                break
                
         	case "0300":
            
            	log.debug descMap.value               
                def i = Math.round( 1000000 / convertHexToInt(descMap.value))
               	def j = i
                def bTemp = getBulbTemp(j)
                sendEvent( name: "colorTemp", value: j)
                sendEvent( name: "kelvin", value: i)
                sendEvent( name: "bulbTemp", value: bTemp)
                break
    	}            
                
    }
    
	
}

def on() {
	log.debug "on()"
	sendEvent(name: "switch", value: "on")
    
    "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 1 {}"
    }

def off() {
	log.debug "off()"
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "level", value: 99)
	"st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
 
}

def refresh() {
    
    [
	"st rattr 0x${device.deviceNetworkId} ${endpointId} 6 0", "delay 500",
    "st rattr 0x${device.deviceNetworkId} ${endpointId} 8 0", "delay 500",
    "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 7"
    ]
}

def setLevel(value) {
	log.trace "setLevel($value)"
	def cmds = []

	if (value == 0) {
		sendEvent(name: "switch", value: "off")
		sendEvent(name: "level", value: 99)
		cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
	}
	else if (device.latestValue("switch") == "off") {
		sendEvent(name: "switch", value: "on")
	}

	sendEvent(name: "level", value: value)
	def level = hex(value * 255 / 100)
	cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {${level} 1500}"

	//log.debug cmds
	cmds
}

def setColorTemp(value) {
	
    log.trace "setColorTemp($value)"
    

    
   	def degrees = Math.round(value)
    if (degrees < 2700) { degrees = 2700 }
    if (degrees > 6500) { degrees = 6500 }
	
    def bTemp = getBulbTemp(degrees)
    
    log.trace degrees
    
	def cmds = []
	
	sendEvent(name: "colorTemp", value: value)
    sendEvent(name: "kelvin", value: degrees)
    sendEvent( name: "bulbTemp", value: bTemp)
    
	def levelC = swapEndianHex(hexSixteen(1000000/degrees))
    
    log.trace levelC
    
	cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0300 0x0a {${levelC} 2000}"

	//log.debug cmds
	cmds
}

def updated() {
	
    /*
    [
	"st wattr 0x${device.deviceNetworkId} ${endpointId} 8 0x10 0x21 {0015}", "delay 500",
    "st wattr 0x${device.deviceNetworkId} ${endpointId} 8 0x11 0x20 {fe}"
    ]
	*/

}

def configure() {

	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	log.debug "Confuguring Reporting and Bindings."
	def configCmds = [	
  
        //Switch Reporting
        "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1000",
        
        //Level Control Reporting
        "zcl global send-me-a-report 8 0 0x20 5 3600 {0010}", "delay 200",
        "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",
        
        //Color Temp Reporting
        "zcl global send-me-a-report 0x0300 7 0x21 5 3600 {0010}", "delay 200",
        "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",
        
        "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 6 {${device.zigbeeId}} {}", "delay 1000",
		"zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 8 {${device.zigbeeId}} {}", "delay 1000",
        "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x0300 {${device.zigbeeId}} {}", "delay 500",
	]
    return configCmds + refresh() // send refresh cmds as part of config
}

def uninstalled() {

	log.debug "uninstalled()"
		
	response("zcl rftd")
 
}

private getBulbTemp(value) {
	
    def s = "Soft White"
    
	if (value < 2901) {
    	return s
    } 
    else if (value < 3376) {
    	s = "Warm White"
        return s
    }
    else if (value < 3916) {
    	s = "Cool White"
        return s
    }
    else if (value < 4601) {
    	s = "Bright White"
        return s
    }
    else if (value < 5751) {
    	s = "Natural"
        return s
    }
    else {
    	s = "Daylight"
        return s
    }

}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}



private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private hexSixteen(value, width=4) {
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