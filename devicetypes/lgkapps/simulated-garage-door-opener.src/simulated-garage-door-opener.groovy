/**
 *  Simulated Garage Door Opener
 *
 *  Copyright 2014 SmartThings
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
	definition (name: "Simulated Garage Door Opener", namespace: "lgkapps", author: "SmartThings & Mark-C",ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-shade") {
		capability "Actuator"
		capability "Door Control"
        capability "Garage Door Control"
		capability "Contact Sensor"
		capability "Sensor"
		capability "Health Check"
// google compatability testing        
        capability "Valve"
        capability "Window Shade"
        attribute "OpenableState", "String"
        attribute "openPercent", "number"
        command "OpenClose"
        command "up"
        command "down"
//testing end
	}

	simulator {
		
	}

	tiles {
		standardTile("toggle", "device.door", width: 3, height: 2) {
			state("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#00A0DC", nextState:"opening")
            state("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#e86d13", nextState:"closing")
            state("opening", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-opening", backgroundColor:"#e86d13", nextState:"closing")
            state("closing", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closing", backgroundColor:"#e50000", nextState:"opening")
		}
        
		standardTile("open", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open', action:"door control.open", icon:"st.doors.garage.garage-opening"
        }
        
		standardTile("close", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"door control.close", icon:"st.doors.garage.garage-closing"
		}
        
        standardTile("contact", "device.contact", width: 1, height: 1) {
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC")
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13")
		}
        
		main "toggle"
		details(["toggle", "open", "close", "contact"])
	}
}
def OpenClose(opper){
log.debug "openclose $opper"
	if (opper == "100"){ open()}
    else { closes()}
}
def setLevel(opper){
log.debug "setlevel $opper"
	if (opper == "100"){ open()}
    else { close()}
}

def parse(String description) {
	log.trace "parse($description)"
}

def open(contactvar) {
log.debug "open = $contactvar"
    if (contactvar == "open"){
    	runIn (2, finishOpening)
    }
    else {
    sendEvent(name: "door", value: "opening")
    sendEvent(name: "OpenableState", value: "opening")
    }
}

def close(contactvar) {
log.debug "close = $contactvar"
	if (contactvar == "closed"){
    	runIn (2, finishClosing)
    }
    else {
    sendEvent(name: "door", value: "closing")
    sendEvent(name: "OpenableState", value: "closing")
    }
}

def finishOpening() {
	def event = [ ]
    event <<	sendEvent(name: "door", value: "open")
    event <<	sendEvent(name: "contact", value: "open")
    event <<	sendEvent(name: "openPercent", value: "100")
    event <<	sendEvent(name: "OpenableState", value: "open")
    event
}

def finishClosing() {
	def event = [ ]
	event <<	sendEvent(name: "door", value: "closed")
    event <<	sendEvent(name: "contact", value: "closed")
    event <<	sendEvent(name: "openPercent", value: "0")
    event <<	sendEvent(name: "OpenableState", value: "closed")
	event
}

def installed() {
	log.trace "Executing 'installed'"
	initialize()
}

def updated() {
	log.trace "Executing 'updated'"
	initialize()
}

private initialize() {
	log.trace "Executing 'initialize'"
	sendEvent(name: "openPercent", value: "0")
    sendEvent(name: "OpenableState", value: "closed")
	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}