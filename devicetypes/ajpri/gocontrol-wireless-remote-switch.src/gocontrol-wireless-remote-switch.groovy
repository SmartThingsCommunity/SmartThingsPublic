/**
 *  GoControl Wireless Remote Switch WA00Z-1
 *
 *  Copyright 2016 Austin Pritchett
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
 *	
 */
 
 /*
 Version 1.2 - June 19th, 2017
 	Exposed "Actuator" & "Sensor" Capability
 Version 1.1 - December 15th, 2016
    Added Reverse Buttons functionality
    Fixed Issues with some SmartApps
 Version 1 - April 12th, 2016
    Initial Release
 */

metadata {
	definition (name: "GoControl Wireless Remote Switch", namespace: "ajpri", author: "Austin Pritchett") {
    	capability "Actuator"
		capability "Button"
        capability "Holdable Button"
		capability "Configuration"
        capability "Battery"
        capability "Sensor"


		fingerprint deviceId:"0x1801", inClusters:"0x5E, 0x86, 0x72, 0x5B, 0x85, 0x59, 0x73, 0x70, 0x80, 0x84, 0x5A, 0x7A", outClusters:"0x5B, 0x20"        
	}

	simulator {
		// TODO: define status and reply messages here
        status "button 1 pushed":  "command: 5B03, payload: 40 00 01"
		status "button 2 pushed":  "command: 5B03, payload: 41 00 02"
	}

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
		}
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        main "button"
		details(["button", "battery"])
	}
    
    preferences {
        input name: "reverse", type: "bool", title: "Reverse Buttons", description: "Reverse the top and bottom buttons", required: true
    }
}

// parse events into attributes
def parse(String description) {
	//log.debug "Parsing '${description}'"
    
    def results = []
	if (description.startsWith("Err")) {
	    results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description)

		if(cmd) results += zwaveEvent(cmd)
		if(!results) results = [ descriptionText: cmd, displayed: true ]
	}
	return results

}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    Integer button   
    if(reverse == true){
    	if(cmd.sceneNumber == 1){
        	button = 2
        }else{
        	button = 1
        }
        log.debug button
    }else{
    	button = (cmd.sceneNumber) as Integer
    }
    	

    if(cmd.keyAttributes == 0){
		createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
    }else if(cmd.keyAttributes == 1){
		createEvent(name: "button", value: "holdRelease", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was released", isStateChange: true)
    }else if(cmd.keyAttributes == 2){
		createEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
    }      
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	// Only ask for battery if we haven't had a BatteryReport in a while
	if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
		result << response(zwave.batteryV1.batteryGet())
		result << response("delay 1200")  // leave time for device to respond to batteryGet
	}
    
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
    result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {    
    def result = []
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} battery is low"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	result << createEvent(map)

	result
}

def configure() {
	
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	sendEvent(name: "numberOfButtons", value: 2)
}