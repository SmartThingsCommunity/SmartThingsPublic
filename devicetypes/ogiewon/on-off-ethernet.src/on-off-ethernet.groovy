/**
 *  Copyright 2015 SmartThings (original)
 *  
 *  2017-02-11 Dan Ogorchock - Since SmartThings has abandoned the ThingShield, I have created a new
 *                             Arduino "SmartThings" library with support for the ThingShield, 
 *                             W5100 Ethernet Shield, and the NodeMCU ESP8266 Board for connectivity to 
 *                             the SmartThings cloud.  This file is to be used with the On/Off Ethernet W5100
 *                             and ESP8266 examples found in this new library which can be found at
 *
 *                             https://github.com/DanielOgorchock/ST_Anything/tree/master/Arduino/libraries/SmartThings
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
	definition (name: "On/Off Ethernet", namespace: "ogiewon", author: "Dan Ogorchock") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// Simulator metadata
	simulator {

	}
    
    // Preferences
	preferences {
		input "ip", "text", title: "Arduino IP Address", description: "ip", required: true, displayDuringSetup: true
		input "port", "text", title: "Arduino Port", description: "port", required: true, displayDuringSetup: true
		input "mac", "text", title: "Arduino MAC Addr", description: "mac", required: true, displayDuringSetup: true
	}
	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main (["switch"])
		details (["switch","configure"])
	}
}

// parse events into attributes
def parse(String description) {
	//log.debug "Parsing '${description}'"
	def msg = parseLanMessage(description)
	def headerString = msg.header

	if (!headerString) {
		//log.debug "headerstring was null for some reason :("
    }

	def bodyString = msg.body

	if (bodyString) {
        //log.debug "BodyString: $bodyString"
        def value = bodyString
	    def name = value in ["on","off"] ? "switch" : null
	    def result = createEvent(name: name, value: value)
	    log.debug "Parse returned ${result?.descriptionText}"
	    return result
	}
}

private getHostAddress() {
    def ip = settings.ip
    def port = settings.port

	log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}

def sendEthernet(message) {
	log.debug "Executing 'sendEthernet' ${message}"
	new physicalgraph.device.HubAction(
    	method: "POST",
    	path: "/${message}?",
    	headers: [ HOST: "${getHostAddress()}" ]
	)
}

// Commands sent to the device
def on() {
	log.debug "Sending 'on'"
	sendEthernet("on")
}

def off() {
	log.debug "Sending 'off'"
	sendEthernet("off")
}

def configure() {
	log.debug "Executing 'configure'"
    if(device.deviceNetworkId!=settings.mac) {
    	log.debug "setting device network id"
    	device.deviceNetworkId = settings.mac
    }
}