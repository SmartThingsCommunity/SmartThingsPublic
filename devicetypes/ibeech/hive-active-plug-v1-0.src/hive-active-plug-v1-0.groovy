/**
 *  Hive Active Plug v1.0
 *
 *  Copyright 2016 Tom Beech
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
	definition (name: "Hive Active Plug v1.0", namespace: "ibeech", author: "Tom Beech") {
		capability "Switch"
        capability "Power Meter"
        capability "Refresh"
		capability "Polling"
        
        command "changeSwitchState", ["string"]
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {    
        
		 standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff"
		}
        
        valueTile("power", "device.power", decoration: "flat") {
			state "default", label:'${currentValue} W'
		}
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}        

		main (["switch", "power"])
		details (["switch", "power", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "parsing '${description}'"
}


def on() {
	log.debug "Executing 'on'"	     
    
    def args = [nodes: [	[attributes: [state: [targetValue: "ON"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    if(resp.status == 404) {
		// Plug has reported it is offline, poll for more details
        poll()
        return
    }    
    
    sendEvent(name: "switch", value: "on");    
    
    runIn(5, "poll")
}

def off() {
	log.debug "Executing 'off'"
	    
    def args = [nodes: [	[attributes: [state: [targetValue: "OFF"]]]]]                
    def resp = parent.apiPUT("/nodes/${device.deviceNetworkId}", args)
    
    if(resp.status == 404) {
		// Plug has reported it is offline, poll for more details
        poll()
        return
    } 

	sendEvent(name: "power", value: 0, unit: "W")
    sendEvent(name: "switch", value: "off");   
}

def changeSwitchState(newState) {

	log.trace "Received update that this switch is now $newState"
	switch(newState) {
    	case 1:
			sendEvent(name: "switch", value: "on")
            runIn(5, "poll")
            break;
    	case 0:
        	sendEvent(name: "switch", value: "off")
            sendEvent(name: "power", value: 0, unit: "W")
            break;
    }
}

def poll() {
	log.debug "Executing 'poll'"    
    
    def resp = parent.apiGET("/nodes/${device.deviceNetworkId}")
	if (resp.status != 200) {
		log.error("Unexpected result in poll(): [${resp.status}] ${resp.data}")
		return []
	}
	data.nodes = resp.data.nodes

	def state = data.nodes.attributes.state.reportedValue[0] 
	def powerConsumption = data.nodes.attributes.powerConsumption.reportedValue[0]
    def presence = data.nodes.attributes.presence.reportedValue[0]
    powerConsumption = String.format("%.0f", powerConsumption)

	log.debug "State: $state"
    log.debug "Power Consumption: $powerConsumption"
    log.debug "Presence: $presence"
    
	if(presence == "ABSENT") {
    	// Bulb is not present (i.e. turned off at the switch or removed)
    	sendEvent(name: 'switch', value: "off")	        
    } else {    	
        sendEvent(name: 'switch', value: state.toLowerCase())
    }
    
    // Set power consumption
    log.debug "Setting power"
    if(state == "OFF") {
    	sendEvent(name: "power", value: 0, unit: "W")	
    } else {
        sendEvent(name: "power", value: powerConsumption, unit: "W")
    }
    log.debug "Power set"
}

def refresh() {
	log.debug "Executing 'refresh'"    
	poll();
}