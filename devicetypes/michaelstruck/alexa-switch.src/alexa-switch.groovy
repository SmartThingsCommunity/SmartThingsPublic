/**
 *  Alexa Switch
 *
 *  Copyright 2016 Michael Struck
 *  Version 2.0.3 3/1/16
 *
 *  Version 1.0.0 - Initial release
 *  Version 1.1.0 - Updated the interface to better match SmartThings dimmers (thanks to @BoruGee)
 *  Version 2.0.0 - Updated to allow for sending state change regardless of switch/dimmer status;changed name of device
 *  Version 2.0.1 - No longer on/off commands sent to dimmer when level changes
 *  Version 2.0.2 - Added icons for the switch states
 *  Version 2.0.3 - Reverted back to original icons for better GUI experience
 * 
 *  Uses code from SmartThings
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
        definition (name: "Alexa Switch", namespace: "MichaelStruck", author: "SmartThings") {
        capability "Switch"
        capability "Switch Level"
        
		attribute "About", "string"
    }

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
    			attributeState "off", label: '${name}', action: "switch.on", backgroundColor: "#ffffff",icon: "st.switches.light.off", nextState: "turningOn"
		      	attributeState "on", label: '${name}', action: "switch.off", backgroundColor: "#79b821",icon: "st.switches.light.on",  nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on",backgroundColor: "#ffffff", icon: "st.switches.light.off",  nextState: "turningOn"
		      	attributeState "turningOn", label: '${name}', action: "switch.off",backgroundColor: "#79b821", icon: "st.switches.light.on", nextState: "turningOff"
        	}
        		tileAttribute("device.level", key: "SLIDER_CONTROL") {
            		attributeState "level", action:"switch level.setLevel"
        		}
        		tileAttribute("level", key: "SECONDARY_CONTROL") {
              		attributeState "level", label: 'Light dimmed to ${currentValue}%'
        		}    
		}
        valueTile("about", "device.About", inactiveLabel: false, decoration: "flat", width: 6, height:2) {
            state "default", label:"Alexa Switch\nSwitch created by Alexa Helper\nSwitch code version 2.0.3 (03/01/16)"
		}
        valueTile("lValue", "device.level", inactiveLabel: true, height:2, width:2, decoration: "flat") {  
			state "levelValue", label:'${currentValue}%', unit:"", backgroundColor: "#53a7c0"  
        }  
        standardTile("on", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'on', action:"switch.on", icon:"st.switches.light.on"
		}
		standardTile("off", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'off', action:"switch.off", icon:"st.switches.light.off"
		}
		main "switch"
		details(["switch","on","lValue","off","about"])

	}
}
def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on",isStateChange: true)
    log.info "Alexa Switch sent 'On' command"
}

def off() {
	sendEvent(name: "switch", value: "off",isStateChange: true)
    log.info "Alexa Switch sent 'Off' command"
}

def setLevel(val){
    log.info "Alexa Switch set to $val"
    
    // make sure we don't drive switches past allowed values (command will hang device waiting for it to
    // execute. Never commes back)
    if (val < 0){
    	val = 0
    }
    
    if( val > 100){
    	val = 100
    }
    
    if (val == 0){ 
    	sendEvent(name:"level",value:val)
    }
    else
    {
    	sendEvent(name:"level",value:val)
    	sendEvent(name:"switch.setLevel",value:val)
    }
}

