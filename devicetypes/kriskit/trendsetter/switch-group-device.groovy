/**
 *  Trend Setter - Switch Group Device
 *
 *  Copyright 2015 Chris Kitch
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
	definition (name: "Switch Group Device", namespace: "kriskit.trendSetter", author: "Chris Kitch") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
        
        attribute "onPercentage", "number"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "half", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#a3d164", nextState: "turningOn"
                attributeState "mostlyOn", label: 'Onish', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#79b821", nextState: "turningOn"
                attributeState "mostlyOff", label: 'Offish', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#d1e5b5", nextState: "turninOff"
			}
            
			tileAttribute ("device.onPercentage", key: "SECONDARY_CONTROL") {
				attributeState "onPercentage", label:'${currentValue}% On'
                attributeState "100", label:'All On'
                attributeState "0", label:'All Off'
			}
		}
	}
    
    main "switch"
    details(["switch"])
}

def parse(String description) {
}

def groupSync(name, values) {
	try {
    	"sync${name.capitalize()}"(values)	
    } catch(ex) {
    	log.error "Error executing 'sync${name.capitalize()}' method: $ex"
    }
}

// SWITCH
def on() {
	on(true)
}

def on(triggerGroup) {
	sendEvent(name: "switch", value: "on")
    sendEvent(name: "onPercentage", value: 100, displayed: false)
    
    if (triggerGroup)
    	parent.performGroupCommand("on")
}

def off() {
	off(true)
}

def off(triggerGroup) {
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "onPercentage", value: 0, displayed: false)
    
    if (triggerGroup)
    	parent.performGroupCommand("off")
}

def syncSwitch(values) {
	log.debug "syncSwitch(): $values"
    
    def onCount = values?.count { it == "on" }
    def percentOn = (int)Math.floor((onCount / values?.size()) * 100)
    
    log.debug "Percent On: $percentOn"
    
    if (percentOn == 0 || percentOn == 100) {
    	if (percentOn == 0)
        	off(false)
        else
        	on(false)            
        return
    }
    
    def value = null
    
    if (percentOn == 50)
    	value = "half"
    else if (percentOn > 0 && percentOn < 50)
		value = "mostlyOff"
    else if (percentOn > 50 && percentOn < 100)
		value = "mostlyOn"
        
	sendEvent(name: "switch", value: value)
	sendEvent(name: "onPercentage", value: percentOn, displayed: false)
}
