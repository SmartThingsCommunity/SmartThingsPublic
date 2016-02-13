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

     preferences {
        input("autorelayoff1", "number", title: "Relay 1 auto-off", description: "Seconds until auto switch off?", required: false)
        input("autorelayoff2", "number", title: "Relay 2 auto-off", description: "Seconds until auto switch off?", required: false)
        input("autorelayoff3", "number", title: "Relay 3 auto-off", description: "Seconds until auto switch off?", required: false)
        input("autorelayoff4", "number", title: "Relay 4 auto-off", description: "Seconds until auto switch off?", required: false)
        input("autorelayoff5", "number", title: "Relay 5 auto-off", description: "Seconds until auto switch off?", required: false)
        input("autorelayoff6", "number", title: "Relay 6 auto-off", description: "Seconds until auto switch off?", required: false)
        input("autorelayoff7", "number", title: "Relay 7 auto-off", description: "Seconds until auto switch off?", required: false)
        input("autorelayoff8", "number", title: "Relay 8 auto-off", description: "Seconds until auto switch off?", required: false)
        
    }


	// Automatically generated. Make future change here.
	definition (name: "Arduino Relay Board", namespace: "Arduino:", author: "badgermanus@gmail.com") {
		capability "Polling"
        capability "Refresh"
        //Fan
        attribute "speed", "string"
        command "high"
        command "med"
        command "low"
                
		command "RelayOn1"
		command "RelayOff1"
		command "RelayOn2"
		command "RelayOff2"
		command "RelayOn3"
		command "RelayOff3"
		command "RelayOn4"
		command "RelayOff4"
		command "RelayOn5"
		command "RelayOff5"
		command "RelayOn6"
		command "RelayOff6"
		command "RelayOn7"
		command "RelayOff7"
		command "RelayOn8"
		command "RelayOff8"
        
        command "Push1"
        command "Push2"
        command "Push3"
        command "Push4"
        command "Push5"
        command "Push6"
        command "Push7"
        command "Push8"
	}

	// tile definitions
	tiles {
		standardTile("relay1", "device.relay1", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"off"
            state "off", label: '${name}', action: "Push1", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"on"
            //state "switching", label: '${name}', action: "RelayOff1", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
            
		}
		standardTile("relay2", "device.relay2", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "RelayOff2", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: '${name}', action: "RelayOn2", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"switching"
            state "switching", label: '${name}', action: "RelayOff2", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
		}
		standardTile("relay3", "device.relay3", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "RelayOff3", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: '${name}', action: "RelayOn3", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"switching"
            state "switching", label: '${name}', action: "RelayOff3", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
		}
		standardTile("relay4", "device.relay4", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "RelayOff4", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: '${name}', action: "RelayOn4", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"switching"
            state "switching", label: '${name}', action: "RelayOff4", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
		}
		standardTile("relay5", "device.relay5", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "RelayOff5", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: '${name}', action: "RelayOn5", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"switching"
            state "switching", label: '${name}', action: "RelayOff5", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
		}
		standardTile("relay6", "device.relay6", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "RelayOff6", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: '${name}', action: "RelayOn6", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"switching"
            state "switching", label: '${name}', action: "RelayOff6", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
		}
		standardTile("relay7", "device.relay7", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "RelayOff7", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: '${name}', action: "RelayOn7", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"switching"
            state "switching", label: '${name}', action: "RelayOff7", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
		}
		standardTile("relay8", "device.relay8", canChangeIcon: true, canChangeBackground: true) {
			state "on", label: '${name}', action: "RelayOff8", icon: "st.switches.switch.on", backgroundColor: "#79b821",  nextState:"switching"
			state "off", label: '${name}', action: "RelayOn8", icon: "st.switches.switch.off", backgroundColor: "#ffffff",  nextState:"switching"
            state "switching", label: '${name}', action: "RelayOff8", icon: "st.switches.switch.on", backgroundColor: "#ff8d00"
		}
        standardTile("speed", "device.speed", canChangeIcon: true, canChangeBackground: true) {
			state "high", label: 'High', backgroundColor: "#53a7c0"
			state "med", label: 'Med', backgroundColor: "#53a7c0"  
            state "low", label: 'Low', backgroundColor: "#53a7c0"
            state "off", label: 'Off', backgroundColor: "#ffffff"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
		main "speed"
		details(["relay1", "relay2", "relay3", "relay4", "relay5", "relay6", "relay7", "relay8", "speed", "refresh"])
	}
    simulator {
        status "on":  "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
        status "off": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
    
        // reply messages
        reply "raw 0x0 { 00 00 0a 0a 6f 6e }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6E"
        reply "raw 0x0 { 00 00 0a 0a 6f 66 66 }": "catchall: 0104 0000 01 01 0040 00 0A21 00 00 0000 0A 00 0A6F6666"
    }
}
 

// Commands
// =====================


def RelayOn1() {
	log.debug"Turning on relay 1"
	String msg = "relayon:1:"+settings.autorelayoff1
	zigbee.smartShield(text: msg).format()
} 
def RelayOff1() {
	log.debug "Turning off relay 1"
	zigbee.smartShield(text: "relayoff:1").format()
}
def RelayOn2() {
	log.debug "Turning on: relay 2"
	String msg = "relayon:2:"+settings.autorelayoff2
    zigbee.smartShield(text: msg).format()
} 
def RelayOff2() {
	log.debug "Turning off relay 2"
	zigbee.smartShield(text: "relayoff:2").format()
}
def RelayOn3() {
	log.debug "Turning on relay 3"
	String msg = "relayon:3:"+settings.autorelayoff3
    zigbee.smartShield(text: msg).format()
} 
def RelayOff3() {
	log.debug "Turning off relay 3"
	zigbee.smartShield(text: "relayoff:3").format()
}
def RelayOn4() {
	log.debug "Turning on relay 4"
	String msg = "relayon:4:"+settings.autorelayoff4
    zigbee.smartShield(text: msg).format()
} 
def RelayOff4() {
	log.debug "Turning off relay 4"
	zigbee.smartShield(text: "relayoff:4").format()
}
def RelayOn5() {
	log.debug "Turning on relay 5"
	String msg = "relayon:5:"+settings.autorelayoff5
    zigbee.smartShield(text: msg).format()
} 
def RelayOff5() {
	log.debug "Turning off relay 5"
	zigbee.smartShield(text: "relayoff:5").format()
}
def RelayOn6() {
	log.debug "Turning on relay 6"
	String msg = "relayon:6:"+settings.autorelayoff6
    zigbee.smartShield(text: msg).format()
} 
def RelayOff6() {
	log.debug "Turning off relay 6"
	zigbee.smartShield(text: "relayoff:6").format()
}
def RelayOn7() {
	log.debug "Turning on relay 7"
	String msg = "relayon:7:"+settings.autorelayoff7
    zigbee.smartShield(text: msg).format()
} 
def RelayOff7() {
	log.debug "Turning off relay 7"
	zigbee.smartShield(text: "relayoff:7").format()
}
def RelayOn8() {
	log.debug "Turning on relay 8"
	String msg = "relayon:8:"+settings.autorelayoff8
    zigbee.smartShield(text: msg).format()
} 
def RelayOff8() {
	log.debug "Turning off relay 8"
	zigbee.smartShield(text: "relayoff:8").format()
}
def Push1() {
	log.debug "Pushing relay 1"
	zigbee.smartShield(text: "push:1").format()
}
def Push2() {
	log.debug "Pushing relay 2"
	zigbee.smartShield(text: "push:2").format()
}
def Push3() {
	log.debug "Pushing relay 3"
	zigbee.smartShield(text: "push:3").format()
}
def Push4() {
	log.debug "Pushing relay 4"
	zigbee.smartShield(text: "push:4").format()
}
def Push5() {
	log.debug "Pushing relay 5"
	zigbee.smartShield(text: "push:5").format()
}
def Push6() {
	log.debug "Pushing relay 6"
	zigbee.smartShield(text: "push:6").format()
}
def Push7() {
	log.debug "Pushing relay 7"
	zigbee.smartShield(text: "push:7").format()
}
def Push8() {
	log.debug "Pushing relay 8"
	zigbee.smartShield(text: "push:8").format()
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