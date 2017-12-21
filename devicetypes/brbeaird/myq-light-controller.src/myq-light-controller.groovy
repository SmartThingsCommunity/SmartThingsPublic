/**
 *  MyQ Light Controller
 *
 *  Copyright 2017 Jason Mok/Brian Beaird
 *  Special thanks to @dcabtacoma for sending me a light controller so I could test the code.
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
 *  Last Updated : 6/6/2017
 *
 */
metadata {
	definition (name: "MyQ Light Controller", namespace: "brbeaird", author: "Jason Mok/Brian Beaird") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"      

		command "updateDeviceStatus", ["string"]
	}

	simulator {	}

	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.Lighting.light13", backgroundColor: "#ffffff", nextState: "on"
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.Lighting.light13", backgroundColor: "#00a0dc", nextState: "off"
			}			
		}              
		main "switch"
		details(["switch"])
	}
}

def on() {
	log.debug "Light turned on"    
    parent.sendCommand(this, "desiredlightstate", 1)
    updateDeviceStatus(1)
	
}
def off() {
	log.debug "Light turned off"
    parent.sendCommand(this, "desiredlightstate", 0)
    updateDeviceStatus(0)
}

def updateDeviceStatus(status) {
	if (status == 0) 
    {  
    	log.debug "Updating status to off"
        sendEvent(name: "switch", value: "off", display: true, displayed: true, isStateChange: true, descriptionText: device.displayName + " was off")        
    }   
	if (status == 1) {         
    	log.debug "Updating status to on"
        sendEvent(name: "switch", value: "on", displayed: true, display: true, isStateChange: true, descriptionText: device.displayName + " was on")  
    }
       
}

def showVersion(){
	return "1.0.1"
}