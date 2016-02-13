/**
 *  Arduino + 8-way relay board Device Type for SmartThings
 *
 *  Author: badgermanus@gmail.com
 *  Code: https://github.com/jwsf/device-type.arduino-8-way-relay
 *
 * Copyright (C) 2014 Jonathan Wilson  <badgermanus@gmail.com>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, 
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions: The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */ 
 
metadata {

	// Automatically generated. Make future change here.
	definition (name: "Arduino Relay Fan Control", namespace: "Arduino:", author: "MikeD") {
		capability "Polling"
        capability "Refresh"
        //Fan
        attribute "speed", "string"
        command "high"
        command "med"
        command "low"
        command "off"
        command "LRLight"
        

	}

	// tile definitions
	tiles {
        // Fan Relay
        standardTile("relay1", "device.speed", canChangeIcon: true, canChangeBackground: true) {
			state "high", label: 'High', action: "med", backgroundColor: "#53a7c0", nextState:"med"
			state "med", label: 'Med', action: "low", backgroundColor: "#53a7c0", nextState:"low"  
            state "low", label: 'Low', action: "off", backgroundColor: "#53a7c0", nextState:"off"
            state "off", label: 'Off', action: "high", backgroundColor: "#ffffff", nextState:"high"
        }		
   		// Light Switch
		standardTile("relay2", "device.relay2", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "LRLight", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"off"
			state "off", label: '${name}', action: "LRLight", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"on"
		}

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
		main "relay1"
		details(["relay1", "relay2", "refresh"])
	}
}

def off() {
	log.debug"Setting fan to off"
	zigbee.smartShield(text: "LRFan:off").format()
} 
def high() {
	log.debug "Setting fan to high"
	zigbee.smartShield(text: "LRFan:high").format()
}
def med() {
	log.debug "Setting fan to med"
	zigbee.smartShield(text: "LRFan:med").format()
}
def low() {
	log.debug "Setting fan to low"
	zigbee.smartShield(text: "LRFan:low").format()
}

//Turn on off light
def LRLight() {
	log.debug "Toggle livingroom light"
    zigbee.smartShield(text: "LRLight:1").format()
} 


def poll()
{
	log.debug "Poll - getting state of all relays"
    zigbee.smartShield(text: "relaystateall").format()
}
def refresh()
{
	log.debug "Refresh - getting state of all relays"
    zigbee.smartShield(text: "relaystateall").format()
}


 
// Arduino event handlers
// =================================
 
def parse(String description) {

 	def value = zigbee.parse(description)?.text
    log.debug "Received: " + value
    log.debug value
    
    
    if (value == "relayon1") { 
	   	createEvent (name:"relay1", value:"on", isStateChange:true)
    } else
    if (value == "relayoff1") {
	   	createEvent (name:"relay1", value:"off", isStateChange:true)
    } else 
    if (value == "relayautooff1") {
	   	sendEvent (name: "alert", value: "Relay auto switchoff")
    } else    
    if (value == "relayon2") {
	   	createEvent (name:"relay2", value:"on", isStateChange:true)
    } else
    if (value == "relayoff2") {
	   	createEvent (name:"relay2", value:"off", isStateChange:true)
    } else 
    if (value == "relayon3") {
	   	createEvent (name:"relay3", value:"on", isStateChange:true)
    } else
    if (value == "relayoff3") {
	   	createEvent (name:"relay3", value:"off", isStateChange:true)
    } else 
    if (value == "relayon4") {
	   	createEvent (name:"relay4", value:"on", isStateChange:true)
    } else
    if (value == "relayoff4") {
	   	createEvent (name:"relay4", value:"off", isStateChange:true)
    } else 
    if (value == "relayon5") {
	   	createEvent (name:"relay5", value:"on", isStateChange:true)
    } else
    if (value == "relayoff5") {
	   	createEvent (name:"relay5", value:"off", isStateChange:true)
    } else 
    if (value == "relayon6") {
	   	createEvent (name:"relay6", value:"on", isStateChange:true)
    } else
    if (value == "relayoff6") {
	   	createEvent (name:"relay6", value:"off", isStateChange:true)
    } else 
    if (value == "relayon7") {
	   	createEvent (name:"relay7", value:"on", isStateChange:true)
    } else
    if (value == "relayoff7") {
	   	createEvent (name:"relay7", value:"off", isStateChange:true)
    } else 
    if (value == "relayon8") {
	   	createEvent (name:"relay8", value:"on", isStateChange:true)
    } else
    if (value == "relayoff8") {
	   	createEvent (name:"relay8", value:"off", isStateChange:true)
    } else
    if (value == "fanhigh") {
	   	createEvent (name:"speed", value:"high", isStateChange:true)
    } else
    if (value == "fanmed") {
	   	createEvent (name:"speed", value:"med", isStateChange:true)
    } else
    if (value == "fanlow") {
	   	createEvent (name:"speed", value:"low", isStateChange:true)
    } else
    if (value == "fanoff") {
	   	createEvent (name:"speed", value:"off", isStateChange:true)
    }
}