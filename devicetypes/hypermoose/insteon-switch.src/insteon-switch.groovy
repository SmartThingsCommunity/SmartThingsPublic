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
 *	Insteon Switch
 *
 *	Author: hypermoose
 *	Date: 2016-06-19
 *
 *  Updated by kuestess
 *  Date: 05/19/2017
 */
metadata {
	definition (name: "Insteon Switch", namespace: "kuestess", author: "kuestess") {
		//capability "Actuator"
		capability "Switch"
        //capability "Sensor"
        capability "Switch Level"
        capability "Refresh"
        capability "Polling"
	}

tiles(scale:2) {
       multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            
           tileAttribute ("device.level", key: "SLIDER_CONTROL") {
               attributeState "level", action:"switch level.setLevel"
           }

        }
        
        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
        }

        main(["switch"])
        details(["switch", "level", "refresh"])
    }
}


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	parent.pollChild()
}

def generateEvent(Map results) {
	log.debug "generateEvent: parsing data $results"
	if(results) {
    	def level = results.level
		sendEvent(name: "level", value: level, unit: "%")
        sendEvent(name: "switch", value: level > 0 ? "on" : "off")
    }
    
    return null
}


def off() {
	log.debug "off"

	if (!parent.switchOff(this, device.deviceNetworkId)) {
		log.debug "Error turning switch off"
	} else {
    	sendEvent(name: "switch", value: "off")
        sendEvent(name: "level", value: 0, unit: "%")
    }
}

def on() {
	log.debug "on"

	if (!parent.switchOn(this, device.deviceNetworkId)) {
		log.debug "Error turning switch on"
	} else {
    	sendEvent(name: "switch", value: "on")
        sendEvent(name: "level", value: 100, unit: "%")
    }
}

def setLevel(level) {
	log.debug "dim to ${level}"

	if (!parent.switchLevel(this, device.deviceNetworkId, level)) {
		log.debug "Error turning switch on"
	} else {
    	sendEvent(name: "level", value: level, unit: "%")
        sendEvent(name: "switch", value: level > 0 ? "on" : "off")
    }
}

def refresh() {
    log.debug "Refreshing.."
    poll()
}