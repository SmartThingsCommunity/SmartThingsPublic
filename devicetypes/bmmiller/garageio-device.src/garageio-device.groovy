/**
 *  Garageio Device v1.3 - 2016-03-10
 *
 * 		Changelog
 *			v1.3 	- Added watchdogTask() to restart polling if it stops, inspiration from Pollster
 *			v1.2    - Added multiAttributeTile() to make things look prettier. No functionality change.
 *			v1.1.1 	- Tiny fix for service manager failing to complete
 *			v1.1    - GarageioServiceMgr() and Device Handler impplemented to handle ChildDevice creation, deletion, 
 *				    polling, and ST suggested implementation.
 *			v1.0    - GarageioInit() implementation to handle polling in a Smart App, left this way for quite a while
 *			v0.1    - Initial working integration
 *
 *  Copyright 2016 Brandon Miller
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
	definition (name: "Garageio Device", namespace: "bmmiller", author: "Brandon Miller") {
        capability "Contact Sensor"
        capability "Sensor"
        capability "Polling"
        capability "Switch"
            
        attribute "status", "string"

        command "push"
        command "open"
        command "close"
    }
    
    tiles(scale: 2) {
    	multiAttributeTile(name:"status", type:"generic", width: 6, height: 4) {
            tileAttribute("device.status", key: "PRIMARY_CONTROL") {           
                attributeState("closed", label: '${name}', icon:"st.doors.garage.garage-closed", action: "push", backgroundColor:"#44b621", nextState:"opening")
                attributeState("open", label: '${name}', icon:"st.doors.garage.garage-open", action: "push", backgroundColor:"#ff8426", nextState:"closing")
                attributeState("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
				attributeState("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
            }
            
            tileAttribute("device.status", key: "SECONDARY_CONTROL") {
                attributeState("closed", label: '${name}')
                attributeState("open", label: '${name}')
            }
    	}    
        
        standardTile("open", "device.door", decoration: "flat", width: 2, height: 2) {
			state "default", label:'open', action:"push", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close", "device.door", decoration: "flat", width: 2, height: 2) {
			state "default", label:'close', action:"push", icon:"st.doors.garage.garage-closing"
		}
        
        standardTile("contact", "device.contact") {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
        
        standardTile("refresh", "device", decoration: "flat", width: 2, height: 2) {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
		
        main(["status"])
		details(["status","open","close","refresh"])
	}
}

def poll() {
	log.debug "Executing poll()"   
    def results = parent.pollChildren()
    parsePollData(results)
}

def parsePollData(results) {
	log.debug "Results: " + results    
    //log.debug "Door ID to Update: " + device.deviceNetworkId
    if (results != null) {
        results.each {
            if (it.id == device.deviceNetworkId)
            {
                updateStatus(it.state)
            }    
        }   
    }
    else {
    	log.debug "Something went wrong, results of poll() were null."
	}
}

def updateStatus(status) {
	if (status == "CLOSED")
    {    	
        sendEvent(name: 'status', value: 'closed')
        sendEvent(name: 'contact', value: 'closed')
    }
    else if (status == "OPEN")
    {
    	sendEvent(name: 'status', value: 'open')
        sendEvent(name: 'contact', value: 'open')
    }
    log.debug "Status Before Poll for Door Id ${device.deviceNetworkId}: ${state.status}, Status After Poll: ${status}"
    // Now update
    state.status = status
}

def open() {
	if (state.status == "CLOSED") {
    	log.debug "open(): Opening door"
		push()
    } else {
        log.debug "We're already open, doing nothing"
    }
}

def close() {
	if (state.status == "OPEN") {
    	log.debug "close(): Closing door"
		push()
    } else {
    	log.debug "We're already closed, doing nothing"
    }
}

def push() { 
    def changeState = (state.status == "OPEN") ? "CLOSED" : "OPEN"
    log.debug "Current State: ${state.status}, Change to State: ${changeState}, Door ID: ${device.deviceNetworkId}"
    log.debug "Call parent.push(dni, changeState)"
    
    def result = parent.push(device.deviceNetworkId, changeState)
    log.debug result
    if (result == 200) {
    	runIn(60, parent.pollChildren()) 
    } else if (result == 401) {
    	log.error "Authentication error, need to get/refresh token."
    }
}

