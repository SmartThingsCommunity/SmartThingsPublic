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
 *  SmartAlert Siren
 *
 *  Author: Antonio San Roman
 *  Date: 2019-10-13
 */
metadata {
	definition (name: "SmartAlert Siren", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Actuator"
		capability "Switch"
		capability "Alarm"

		command "EstableceModoSirena"
        command "EstableceModoStrobe"
        command "EstableceModoBoth"

		attribute "modosSirena", "string"	

		//fingerprint deviceId: "0x1100", inClusters: "0x26,0x71"
		//fingerprint mfr:"0084", prod:"0313", model:"010B", deviceJoinName: "FortrezZ Siren Strobe Alarm"
        fingerprint inClusters: "0x26,0x71", manufacturer: "Sercomm Corp", model:"SZ-SRN12N", deviceJoinName: "SmartThings Siren"
	}

preferences {
    section ("Watch the contact sensor...") {
        input "theContact", "capability.contactSensor",  title: "pick a contact sensor", required: true, multiple: false
    }
     section ("Turn on/off a siren...") {
        input "theSwitch", "capability.switch"
    }
    section ("Establish modes of  siren") {
        input "theSiren", "capability.alarm"
    } 
    section ("Establish modes of  siren") {
        input "motion", "capability.motion", title: "pick a motion sensor", required: true, multiple: true
     }   
}


simulator {
        // reply messages
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"
		reply "200121,2002": "command: 2003, payload: 21"
		reply "200142,2002": "command: 2003, payload: 42"
		reply "2001FF,delay 3000,200100,2002": "command: 2003, payload: 00"
	}

	tiles(scale: 2) {
			standardTile("contact", "device.contact", width: 2, height: 2) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
		}
         multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4){
			    tileAttribute("device.alarm", key: "VALUE_CONTROL") {
                attributeState("strobe", label:'strobe!', action: "EstableceModoStrobe", icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13")
                attributeState("siren", label:'siren!', action: "EstableceModoSirena", icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13")
                attributeState("both", label:'alarm!', action: "EstableceModoBoth", icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13")			
			}
            
            tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'off', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
			}
		}

		standardTile("test", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"test", icon:"st.secondary.test"
		}
		standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"alarm.off", icon:"st.secondary.off"
		}
		main "alarm"
		details(["alarm","strobe","siren","test","off"])
	}
}

def installed() {
    subscribe(theContact, "contact", contactHandler) 
    subscribe(motion, "motion", motionHandler)
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def updated() {
   subscribe(theContact, "contact", contactHandler)
   subscribe(motion,  "motion", motionHandler)
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def EstableceModoSirena() {
     sendEvent(name: "modosSirena", value: "siren")
}

def EstableceModoStrobe() {
     sendEvent(name: "modosSirena", value: "strobe")
}

def EstableceModoBoth() {
     sendEvent(name: "modosSirena", value: "both")
}

def contactHandler(evt) {
  def modo = device.currentValue("modosSirena")
  if("open" == evt.value) {
    // contact was opened, turn on a sirenn
    switch(modo) {
        case "siren":
              theSiren.siren()
        case "strobe":
              theSiren.strobe()
        case "both":
              theSiren.both()              
    }        
    zigbee.on()       
    log.debug "Contact is in ${evt.value} state"
  }
  if("closed" == evt.value) {
     // contact was closed, turn off the light?
     theSiren.off()
   // log.debug "Contact is in ${evt.value} state"
   }
}

def motionHandler(evt) {
   if(evt.value == "active") {
       theSiren.strobe()
       zigbee.on()
   } else if(evt.value == "inactive") {
       theSiren.off()
       zigbee.off()
   }
}


