/**
 *  LaMetric
 *
 *  Copyright 2016 Smart Atoms Ltd.
 *  Author: Mykola Kirichuk
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
	definition (name: "LaMetric", namespace: "com.lametric", author: "Mykola Kirichuk") {
		capability "Actuator"
		capability "Notification"
		capability "Polling"
		capability "Refresh"
        
        attribute "currentIP", "string"
        attribute "serialNumber", "string"
        attribute "volume", "string"
        attribute "mode", "enum", ["offline","online"]
        
        command "setOffline"
        command "setOnline"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		// TODO: define your main and details tiles here
         tiles(scale: 2) {
     	multiAttributeTile(name:"rich-control"){
			tileAttribute ("mode", key: "PRIMARY_CONTROL") {
	            attributeState "online", label: "LaMetric", action: "", icon:  "https://developer.lametric.com/assets/smart_things/time_100.png", backgroundColor: "#F3C200"
                attributeState "offline", label: "LaMetric", action: "", icon: "https://developer.lametric.com/assets/smart_things/time_100.png", backgroundColor: "#F3F3F3"
			}
	        tileAttribute ("serialNumber", key: "SECONDARY_CONTROL") {
	            attributeState "default", label:'SN: ${currentValue}'
			}
        }
		valueTile("serialNumber", "device.serialNumber", decoration: "flat", height: 1, width: 2, inactiveLabel: false) {
			state "default", label:'SN: ${currentValue}'
		}
		valueTile("networkAddress", "device.currentIP", decoration: "flat", height: 2, width: 4, inactiveLabel: false) {
			state "default", label:'${currentValue}', height: 1, width: 2, inactiveLabel: false
		}

		main (["rich-control"])
		details(["rich-control","networkAddress"])
	}
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    if (description)
    {
    	unschedule("setOffline")
    }
	// TODO: handle 'battery' attribute
	// TODO: handle 'button' attribute
	// TODO: handle 'status' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'level' attribute

}

// handle commands
def setOnline()
{
	log.debug("set online");
    sendEvent(name:"mode", value:"online")
  	unschedule("setOffline")
}
def setOffline(){
	log.debug("set offline");
    sendEvent(name:"mode", value:"offline")
}

def setLevel(level) {
	log.debug "Executing 'setLevel' ${level}"
	// TODO: handle 'setLevel' command
}

def deviceNotification(notif) {
	log.debug "Executing 'deviceNotification' ${notif}"
	// TODO: handle 'deviceNotification' command
    def result = parent.sendNotificationMessageToDevice(device.deviceNetworkId, notif);
    log.debug ("result ${result}");
    log.debug parent;
    return result;
}

def poll() {
	// TODO: handle 'poll' command
    log.debug "Executing 'poll'"
  	if (device.currentValue("currentIP") != "Offline")
    {
	    runIn(30, setOffline)
    }
    parent.poll(device.deviceNetworkId)
}

def refresh() {    
	log.debug "Executing 'refresh'"
//    log.debug "${device?.currentIP}"
    log.debug "${device?.currentValue("currentIP")}"
    log.debug "${device?.currentValue("serialNumber")}"
    log.debug "${device?.currentValue("volume")}"
//    poll()
	
}

/*def setLevel() {
	log.debug "Executing 'setLevel'"
	// TODO: handle 'setLevel' command
}*/