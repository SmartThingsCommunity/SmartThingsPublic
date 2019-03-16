/**
 *  Door Left Open Alert
 *
 *  Copyright 2019 Trais McAllister
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
definition(
    name: "Door Left Open Alert",
    namespace: "tbm0115",
    author: "Trais McAllister",
    description: "Receive alert when any of the following doors are left open for a given amount of time.",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances6-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances6-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances6-icn@3x.png")


preferences {
	section("Sensor(s)") {
		// TODO: put inputs here
        input "mpSensors", "capability.contactSensor", title: "Door(s)", description: "ex. Multi-Purpose Sensor", multiple: true
        input "intThreshold", "number", title: "Timeout (mins)", default: 1
	}
    section("Reminder"){
        input "sendPush", "bool", title: "Send Push Notification?", defaultValue: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(mpSensors, "contact.open", contactDetectedHandler)
    subscribe(mpSensors, "contact.closed", contactDetectedHandler)
    for(mpSensor in mpSensors){
        state[mpSensor.id + "_notified"] = false
    }
}

// TODO: implement event handlers
def contactDetectedHandler(evt){
	def stateCurrent = evt.value
    log.debug "[contactDetectedHandler] $stateCurrent"
    if (stateCurrent == "open"){
    	log.debug "[contactDetectedHandler] $evt.device.id opened."
    	if(state[evt.device.id + "_notified"] == false){
        	def timeOut = intThreshold * 60
        	log.debug "[contactDetectedHandler]\tSending push notification in $timeOut seconds"
        	runIn(timeOut, postOpenedDoor, [data: [d: evt.device]])
        }
    }else{
    	state[evt.device.id + "_notified"] = false
	}
}

def postOpenedDoor(d){
	def dev = mpSensors.find{it.id == d.d.id}
    if (dev != null){
    	def cstate = dev.currentValue("contact")
        log.debug "[postOpenedDoor] $dev.id is now $cstate"
        if (cstate == "open"){
            state[dev.id + "_notified"] = true;
            if (sendPush){
            	sendPush("$dev.displayName has been left open!")
            }else{
            	log.debug "[postOpenedDoor] $dev.displayName has been left open, but no notification options are allowed..."
            }
        }else{
            log.debug "[postOpenedDoor] $dev.id was closed, notification was aborted"
        }
    }else{
    	log.debug "[postOpenedDoor] Couldn't find device by id '$dev.id'"
    }
}