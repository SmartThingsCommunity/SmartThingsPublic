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
 *	Bulb Resetter
 *
 *	Author: Donald Kirker, SmartThings
 *	Date: 2015-08-23
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Bulb Resetter", namespace: "smartthings", author: "Donald Kirker, SmartThings") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
        
        command "resetCree"
        command "resetOsram"
        command "resetBelkin"
        command "resetGELink"
        
        attribute "resetState", "String"

		// Hide fingerprint; you need to type a SmartPower outlet yourself.
		//fingerprint profileId: "0104", inClusters: "0000,0003,0006", outClusters: "0019"
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
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}
        valueTile("resetState", "device.resetState", width: 2, height: 1, decoration: "flat") {
        	state "default", label: " "
        }
        
		standardTile("cree", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "on", /*label:"Cree",*/ action:"resetCree", icon:"http://ecx.images-amazon.com/images/I/51Fvtfl%2BjjL._SL1000_.jpg", backgroundColor: "#ffffff"
		}
		standardTile("osram", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "on", /*label:"Osram",*/ action:"resetOsram", icon:"https://support.smartthings.com/hc/en-us/article_attachments/202485794/73674_Lightify_CLA_60_TW_FR_E27_60.jpg", backgroundColor: "#ffffff"
		}
		standardTile("belkin", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "on", /*label:"Belkin",*/ action:"resetBelkin", icon:"https://support.smartthings.com/hc/en-us/article_attachments/202067094/WeMo_LED_Bulb.jpg", backgroundColor: "#ffffff"
		}
		standardTile("gelink", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "on", /*label:"GE",*/ action:"resetGELink", icon:"https://support.smartthings.com/hc/en-us/article_attachments/202844204/7b692ffc-424b-46fe-a0fc-8957eb9a66cb_400.jpg", backgroundColor: "#ffffff"
		}

		main(["switch"])
		details(["switch", "resetState", "cree", "osram", "belkin", "gelink"])
	}
}

// Parse incoming device messages to generate events
def parse(String description)
{
	if (description?.startsWith("catchall: 0104 000A")) {
		log.debug "Dropping catchall for SmartPower Outlet"
		return []
	} else {
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned ${result?.descriptionText}"
		return result
	}
}

// Commands to device
def on()
{
	log.debug("on()")
	"zcl on-off on"
}

def off()
{
	log.debug("off()")
	"zcl on-off off"
}

def seconds(numSeconds)
{
	def totalMSec = numSeconds * 1000
    
    return "delay ${totalMSec}"
}

def resetStarted()
{
	sendEvent(name: "resetState", value: "Resetting...")
}

def resetCompleted()
{
	sendEvent(name: "resetState", value: "Bulb Reset!")
}

def resetIdle()
{
	sendEvent(name: "resetState", value: "--")
}

// Bulb reset sequences

def resetCree()
{
    def fourSeconds = seconds(4)
	def cmds = [off(), fourSeconds]
    def i = 0
    
    log.debug("resetCree()")
    
    while (i < 4) {
    	cmds += [
            on(),
            off(),
            fourSeconds
        ]
    	i++
    }
    
    cmds += [
        on()
    ]
    
    log.debug("cmds: ${cmds}")
    
    return response(cmds)
}

def resetOsram()
{
    def fiveSeconds = seconds(5)
	def cmds = [off(), fiveSeconds]
    def i = 0
    
    log.debug("resetOsram()")
    
    while (i < 5) {
    	cmds += [
            on(),
            fiveSeconds,
            off(),
            fiveSeconds
        ]
    	i++
    }
    
    cmds += [
        /*on(),
        fiveSeconds,
        fiveSeconds,
        off(),*/
        on()
    ]
    
    log.debug("cmds: ${cmds}")
    
    return response(cmds)
}

def resetBelkin()
{
    def fiveSeconds = seconds(5)
	def cmds = [on()]
    def i = 0
    
    log.debug("resetBelkin()")
    
    while (i < 3) {
    	cmds += [
            off(),
            fiveSeconds,
            on(),
            fiveSeconds
        ]
    	i++
    }
    
    log.debug("cmds: ${cmds}")
    
    return response(cmds)	
}

def resetGELink()
{
    def threeSeconds = seconds(3)
	def cmds = [on()]
    def i = 0
    
    log.debug("resetGELink()")
    
    while (i < 5) {
    	cmds += [
            off(),
            threeSeconds,
            on(),
            threeSeconds
        ]
    	i++
    }
    
    log.debug("cmds: ${cmds}")
    
    return response(cmds)
}


