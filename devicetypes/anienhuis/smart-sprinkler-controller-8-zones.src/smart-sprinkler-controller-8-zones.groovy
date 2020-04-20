/**
 *  This is a start to porting the Arduino and SmartShield based
 *  Irrigation Controllers to an ESP8266 based controller
 *  Author:  Aaron Nienhuis (aaron.nienhuis@gmail.com)
 *
 *  Date:  2017-04-07
 *  Copyright 2017 Aaron Nienhuis
 *  
 *  Irrigation Controller 8 Zones
 *
 *  This SmartThings Device Handler (Device Type) Code Works With the ESP8266 based  Smart Sprinkler Irrigation Controllers also available at this site
 *  
 *
 *	Creates connected irrigation controller
 *
 *  ESP8266 port based on the extensive previous work of:
 *  Author: Stan Dotson (stan@dotson.info) and Matthew Nichols (matt@nichols.name)
 *
 *  Portions of this work previously copyrighted by Stan Dotson and Matthew Nichols
 *
 *	Some code and concepts incorporated from other projects by:
 *  Eric Maycock
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
 
 import groovy.json.JsonSlurper
 
 // for the UI
preferences {
	
    input("oneTimer", "text", title: "Zone One", description: "Zone One Time", required: false, defaultValue: "1")
    input("twoTimer", "text", title: "Zone Two", description: "Zone Two Time", required: false, defaultValue: "1")
    input("threeTimer", "text", title: "Zone Three", description: "Zone Three Time", required: false, defaultValue: "1")
    input("fourTimer", "text", title: "Zone Four", description: "Zone Four Time", required: false, defaultValue: "1")
    input("fiveTimer", "text", title: "Zone Five", description: "Zone Five Time", required: false, defaultValue: "1")
    input("sixTimer", "text", title: "Zone Six", description: "Zone Six Time", required: false, defaultValue: "1")
    input("sevenTimer", "text", title: "Zone Seven", description: "Zone Seven Time", required: false, defaultValue: "1")
    input("eightTimer", "text", title: "Zone Eight", description: "Zone Eight Time", required: false, defaultValue: "1")
}

metadata {
    definition (name: "Smart Sprinkler Controller 8 Zones", version: "1.0.3", author: "aaron.nienhuis@gmail.com", namespace: "anienhuis") {
        
        capability "Switch"
        capability "Momentary"
        capability "Actuator"
		capability "Refresh"
		capability "Sensor"
        capability "Configuration"
        capability "Health Check"
        command "reboot"

        
        command "OnWithZoneTimes"
        command "RelayOn1"
        command "RelayOn1For"
        command "RelayOff1"
        command "RelayOn2"
        command "RelayOn2For"
        command "RelayOff2"
        command "RelayOn3"
        command "RelayOn3For"
        command "RelayOff3"
        command "RelayOn4"
        command "RelayOn4For"
        command "RelayOff4"
        command "RelayOn5"
        command "RelayOn5For"
        command "RelayOff5"
        command "RelayOn6"
        command "RelayOn6For"
        command "RelayOff6"
        command "RelayOn7"
        command "RelayOn7For"
        command "RelayOff7"
        command "RelayOn8"
        command "RelayOn8For"
        command "RelayOff8"
        command "rainDelayed"
        command "update" 
        command "enablePump"
        command "disablePump"
        command "onPump"
        command "offPump"
        command "noEffect"
        command "skip"
        command "expedite"
        command "onHold"
        command "warning"
        attribute "effect", "string"
    }

    simulator {
          }
    
    tiles {
        standardTile("allZonesTile", "device.switch", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Start', action: "switch.on", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "starting"
            state "on", label: 'Running', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0", nextState: "stopping"
            state "starting", label: 'Starting...', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "stopping", label: 'Stopping...', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "rainDelayed", label: 'Rain Delay', action: "switch.off", icon: "st.Weather.weather10", backgroundColor: "#fff000", nextState: "off"
        	state "warning", label: 'Issue',  icon: "st.Health & Wellness.health7", backgroundColor: "#ff000f", nextState: "off"
        }
        standardTile("zoneOneTile", "device.zoneOne", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'One', action: "RelayOn1", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff",nextState: "sending1"
            state "sending1", label: 'sending', action: "RelayOff1", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'One', action: "RelayOff1",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending1"
            state "on", label: 'One', action: "RelayOff1",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending1"
        }
        standardTile("zoneTwoTile", "device.zoneTwo", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Two', action: "RelayOn2", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending2"
            state "sending2", label: 'sending', action: "RelayOff2", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'Two', action: "RelayOff2",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending2"
            state "on", label: 'Two', action: "RelayOff2",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending2"
        }
        standardTile("zoneThreeTile", "device.zoneThree", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Three', action: "RelayOn3", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending3"
            state "sending3", label: 'sending', action: "RelayOff3", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'Three', action: "RelayOff3",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending3"
            state "on", label: 'Three', action: "RelayOff3",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending3"
        }
        standardTile("zoneFourTile", "device.zoneFour", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Four', action: "RelayOn4", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending4"
            state "sending4", label: 'sending', action: "RelayOff4", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'Four', action: "RelayOff4",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending4"
            state "on", label: 'Four', action: "RelayOff4",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending4"
        }
        standardTile("zoneFiveTile", "device.zoneFive", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Five', action: "RelayOn5", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending5"
            state "sending5", label: 'sending', action: "RelayOff5", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'Five', action: "RelayOff5",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending5"
            state "on", label: 'Five', action: "RelayOff5",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending5"
        }
        standardTile("zoneSixTile", "device.zoneSix", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Six', action: "RelayOn6", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending6"
            state "sending6", label: 'sending', action: "RelayOff6", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'Six', action: "RelayOff6",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending6"
            state "on", label: 'Six', action: "RelayOff6",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending6"
        }
        standardTile("zoneSevenTile", "device.zoneSeven", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Seven', action: "RelayOn7", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending7"
            state "sending7", label: 'sending', action: "RelayOff7", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'Seven', action: "RelayOff7",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending7"
            state "on", label: 'Seven', action: "RelayOff7",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending7"
        }
        standardTile("zoneEightTile", "device.zoneEight", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Eight', action: "RelayOn8", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending8"
            state "sending8", label: 'sending', action: "RelayOff8", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q", label: 'Eight', action: "RelayOff8",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending8"
            state "on", label: 'Eight', action: "RelayOff8",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending8"
            state "havePump", label: 'Eight', action: "disablePump", icon: "st.custom.buttons.subtract-icon", backgroundColor: "#ffffff"

        }
        standardTile("pumpTile", "device.pump", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "noPump", label: 'Pump', action: "enablePump", icon: "st.custom.buttons.subtract-icon", backgroundColor: "#ffffff",nextState: "enablingPump"
         	state "offPump", label: 'Pump', action: "onPump", icon: "st.valves.water.closed", backgroundColor: "#ffffff", nextState: "sendingPump"
           	state "enablingPump", label: 'sending', action: "disablePump", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "disablingPump", label: 'sending', action: "disablePump", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "onPump", label: 'Pump', action: "offPump",icon: "st.valves.water.open", backgroundColor: "#53a7c0", nextState: "sendingPump"
            state "sendingPump", label: 'sending', action: "offPump", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
        }
        	
        standardTile("refreshTile", "device.refresh", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
            state "ok", label: "", action: "update", icon: "st.secondary.refresh", backgroundColor: "#ffffff"
        }
        standardTile("scheduleEffect", "device.effect", width: 1, height: 1) {
            state("noEffect", label: "Normal", action: "skip", icon: "st.Office.office7", backgroundColor: "#ffffff")
            state("skip", label: "Skip 1X", action: "expedite", icon: "st.Office.office7", backgroundColor: "#c0a353")
            state("expedite", label: "Expedite", action: "onHold", icon: "st.Office.office7", backgroundColor: "#53a7c0")
            state("onHold", label: "Pause", action: "noEffect", icon: "st.Office.office7", backgroundColor: "#bc2323")
        }
    	standardTile("reboot", "device.reboot", decoration: "flat", height: 1, width: 1, inactiveLabel: false) {
            state "default", label:"Reboot", action:"reboot", icon:"", backgroundColor:"#ffffff"
        }
        valueTile("ip", "ip", width: 1, height: 1) {
    		state "ip", label:'IP Address\r\n${currentValue}'
		}
        valueTile("firmware", "firmware", width: 1, height: 1) {
    		state "firmware", label:'Firmware ${currentValue}'
		}
        
        main "allZonesTile"
        details(["zoneOneTile","zoneTwoTile","zoneThreeTile","zoneFourTile","zoneFiveTile","zoneSixTile","zoneSevenTile","zoneEightTile", "pumpTile","scheduleEffect","ip","firmware","refreshTile","reboot"])
    }
}

// parse events into attributes to create events

// Original Parse from Irrigation Controller

def parse(String description) {

	//log.debug "Parsing: ${description}"
    def events = []
    def descMap = parseDescriptionAsMap(description)
    def body
    def currentVal
    def isDisplayed = true
    def isPhysical = true
    def name
    def action
    //log.debug "descMap: ${descMap}"

    if (!state.mac || state.mac != descMap["mac"]) {
		log.debug "Mac address of device found ${descMap["mac"]}"
        updateDataValue("mac", descMap["mac"])
	}
    
    if (state.mac != null && state.dni != state.mac) state.dni = setDeviceNetworkId(state.mac)
    if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) sendEvent(name: 'ip', value: getDataValue("ip"))
    
    if (descMap["body"]) body = new String(descMap["body"].decodeBase64())

    if (body && body != "") {
    
    	if(body.startsWith("{") || body.startsWith("[")) {
   
   			def slurper = new JsonSlurper()
    		def jsonResult = slurper.parseText(body)

			//log.debug "jsonResult: $jsonResult"
            
            if (jsonResult.containsKey("type")) {
        		if (jsonResult.type == "configuration")
            		events << update_current_properties(jsonResult)
                	log.debug "Events $events"
                	return events
    		}
            
            if (jsonResult.containsKey("relay")) {
            jsonResult.relay.each {  rel ->
            	name = rel.key
                action = rel.value
            
                currentVal = device?.currentValue(name)
                //log.debug "Name $name Action $action Currentval $currentVal"
        		if (action != currentVal){
                	
                    if (action == "on" ) {
            			isDisplayed = true
                		isPhysical = true
                     }
                     if (action == "off" || action == "q") {
            			isDisplayed = false
                		isPhysical = false
                     }
                     //log.debug "Executing $jsonResult.zoneOne"
                	 def result = createEvent(name: name, value: action, displayed: isDisplayed, isStateChange: true, isPhysical: isPhysical)
            		 sendEvent(result)
            	} else {
                	//log.debug "No change in value"
                }

			}
            }

            if (jsonResult.containsKey("pump")) {
            	def value = jsonResult.pump
    			if (value == "onPump") {
    				//send an event if there is a state change
        			if (device?.currentValue("pump") != "onPump") {
                    	log.debug "parsing pump onPump"
        				sendEvent (name:"pump", value:"onPump", displayed: true, isStateChange: true, isPhysical: true)
                    }
    			}
                if (value == "offPump") {
    				//send an event if there is a state change
        			if (device?.currentValue("pump") != "offPump") {
                    	log.debug "parsing pump offPump"
        				sendEvent (name:"pump", value:"offPump", displayed: true, isStateChange: true, isPhysical: true)
                    }
    			}
                if (value == "pumpAdded") {
    				//send an event if there is a state change
        			log.debug "parsing pump pumpAdded"
        			if (device?.currentValue("zoneEight") != "havePump" && device?.currentValue("pump") != "offPump") {
    					sendEvent (name:"zoneEight", value:"havePump", displayed: true, isStateChange: true, isPhysical: true)
        				sendEvent (name:"pump", value:"offPump", displayed: true, isStateChange: true, isPhysical: true)
    				}
    			}
    			if (value == "pumpRemoved") {
    				//send event if there is a state change
        			if (device?.currentValue("pump") != "noPump") {
    					sendEvent (name:"pump", value:"noPump", displayed: true, isStateChange: true, isPhysical: true)
    				}
    			}
			}
			if (jsonResult.containsKey("version")) {
            	//log.debug "firmware version: $jsonResult.version"
                if (device?.currentValue("firmware") != jsonResult.version) {
                	//log.debug "updating firmware version"
       				sendEvent(name:"firmware", value: jsonResult.version, displayed: false)
                }
    		}

			if(anyZoneOn()) {
        		//manages the state of the overall system.  Overall state is "on" if any zone is on
        		//set displayed to false; does not need to be logged in mobile app
        		if(device?.currentValue("switch") != "on") {
        			sendEvent (name: "switch", value: "on", descriptionText: "Irrigation System Is On", displayed: false)  //displayed default is false to minimize logging
        		}
    		} else if (device?.currentValue("switch") != "rainDelayed") {
        		if(device?.currentValue("switch") != "off") {
        			sendEvent (name: "switch", value: "off", descriptionText: "Irrigation System Is Off", displayed: false)  //displayed default is false to minimize logging
       			}
    		}
    	} else {
        	//log.debug "Response is not JSON: $body"
    	}
  	}          
}



def anyZoneOn() {
    if(device?.currentValue("zoneOne") in ["on","q"]) return true;
    if(device?.currentValue("zoneTwo") in ["on","q"]) return true;
    if(device?.currentValue("zoneThree") in ["o3","q"]) return true;
    if(device?.currentValue("zoneFour") in ["on","q"]) return true;
    if(device?.currentValue("zoneFive") in ["on","q"]) return true;
    if(device?.currentValue("zoneSix") in ["on","q"]) return true;
    if(device?.currentValue("zoneSeven") in ["on","q"]) return true;
    if(device?.currentValue("zoneEight") in ["on","q"]) return true;

    false;
}

// handle commands

def RelayOn1() {
    log.info "Executing 'on,1'"
    getAction("/command?command=on,1,${oneTimer}")
}


def RelayOn1For(value) {
    value = checkTime(value)
    log.info "Executing 'on,1,$value'"
    
    getAction("/command?command=on,1,${value}")
}

def RelayOff1() {
    log.info "Executing 'off,1'"
    
    getAction("/command?command=off,1")
}

def RelayOn2() {
    log.info "Executing 'on,2'"
    
    getAction("/command?command=on,2,${twoTimer}")
}

def RelayOn2For(value) {
    value = checkTime(value)
    log.info "Executing 'on,2,$value'"
    
    getAction("/command?command=on,2,${value}")
}

def RelayOff2() {
    log.info "Executing 'off,2'"
    
    getAction("/command?command=off,2")
}

def RelayOn3() {
    log.info "Executing 'on,3'"
    
    getAction("/command?command=on,3,${threeTimer}")
}

def RelayOn3For(value) {
    value = checkTime(value)
    log.info "Executing 'on,3,$value'"
    
    getAction("/command?command=on,3,${value}")
}

def RelayOff3() {
    log.info "Executing 'off,3'"
    
    getAction("/command?command=off,3")
}

def RelayOn4() {
    log.info "Executing 'on,4'"
    
    getAction("/command?command=on,4,${fourTimer}")
}

def RelayOn4For(value) {
    value = checkTime(value)
    log.info "Executing 'on,4,$value'"
    
    getAction("/command?command=on,4,${value}")
}

def RelayOff4() {
    log.info "Executing 'off,4'"
    
    getAction("/command?command=off,4")
}

def RelayOn5() {
    log.info "Executing 'on,5'"
    
    getAction("/command?command=on,5,${fiveTimer}")
}

def RelayOn5For(value) {
    value = checkTime(value)
    log.info "Executing 'on,5,$value'"
    
    getAction("/command?command=on,5,${value}")
}

def RelayOff5() {
    log.info "Executing 'off,5'"
    
    getAction("/command?command=off,5")
}

def RelayOn6() {
    log.info "Executing 'on,6'"
    
    getAction("/command?command=on,6,${sixTimer}")
}

def RelayOn6For(value) {
    value = checkTime(value)
    log.info "Executing 'on,6,$value'"
    
    getAction("/command?command=on,6,${value}")
}

def RelayOff6() {
    log.info "Executing 'off,6'"
    
    getAction("/command?command=off,6")
}

def RelayOn7() {
    log.info "Executing 'on,7'"
    
    getAction("/command?command=on,7,${sevenTimer}")
}

def RelayOn7For(value) {
    value = checkTime(value)
    log.info "Executing 'on,7,$value'"
    
    getAction("/command?command=on,7,${value}")
}

def RelayOff7() {
    log.info "Executing 'off,7'"
    
    getAction("/command?command=off,7")
}

def RelayOn8() {
    log.info "Executing 'on,8'"
    
    getAction("/command?command=on,8,${eightTimer}")
}

def RelayOn8For(value) {
    value = checkTime(value)
    log.info "Executing 'on,8,$value'"
    
    getAction("/command?command=on,8,${value}")
}

def RelayOff8() {
    log.info "Executing 'off,8'"
    
    getAction("/command?command=off,8")
}

def on() {
    log.info "Executing 'allOn'"
    
    getAction("/command?command=allOn,${oneTimer ?: 0},${twoTimer ?: 0},${threeTimer ?: 0},${fourTimer ?: 0},${fiveTimer ?: 0},${sixTimer ?: 0},${sevenTimer ?: 0},${eightTimer ?: 0}")
}

def OnWithZoneTimes(value) {
    log.info "Executing 'allOn' with zone times [$value]"
    def evt = createEvent(name: "switch", value: "starting", displayed: true)
    sendEvent(evt)
    
	def zoneTimes = [:]
    for(z in value.split(",")) {
    	def parts = z.split(":")
        zoneTimes[parts[0].toInteger()] = parts[1]
        log.info("Zone ${parts[0].toInteger()} on for ${parts[1]} minutes")
    }
    
    getAction("/command?command=allOn,${checkTime(zoneTimes[1]) ?: 0},${checkTime(zoneTimes[2]) ?: 0},${checkTime(zoneTimes[3]) ?: 0},${checkTime(zoneTimes[4]) ?: 0},${checkTime(zoneTimes[5]) ?: 0},${checkTime(zoneTimes[6]) ?: 0},${checkTime(zoneTimes[7]) ?: 0},${checkTime(zoneTimes[8]) ?: 0}")
}

def off() {
    log.info "Executing 'allOff'"
    
    getAction("/command?command=allOff")
    
}

def checkTime(t) {
	def time = (t ?: 0).toInteger()
    time > 60 ? 60 : time
}

def update() {
    log.info "Executing refresh"
    
    getAction("/status")
}

def rainDelayed() {
    log.info "rain delayed"
    if(device.currentValue("switch") != "on") {
        sendEvent (name:"switch", value:"rainDelayed", displayed: true)
    }
}

def warning() {
    log.info "Warning: Programmed Irrigation Did Not Start"
    if(device.currentValue("switch") != "on") {
        sendEvent (name:"switch", value:"warning", displayed: true)
    }
}

def enablePump() {
    log.info "Enabling Pump"
    
    getAction("/command?command=pump,3")
}
def disablePump() {
    log.info "Disabling Pump"
    
    getAction("/command?command=pump,0")
}
def onPump() {
    log.info "Turning On Pump"
    
    getAction("/command?command=pump,2")
    }

def offPump() {
	log.info "Turning Off Pump"
    
    getAction("/command?command=pump,1")
        }
def push() {
    log.info "advance to next zone"
    
    getAction("/command?command=advance")
    }

// commands that over-ride the SmartApp

// skip one scheduled watering
def	skip() {
    def evt = createEvent(name: "effect", value: "skip", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}
// over-ride rain delay and water even if it rains
def	expedite() {
    def evt = createEvent(name: "effect", value: "expedite", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

// schedule operates normally
def	noEffect() {
    def evt = createEvent(name: "effect", value: "noEffect", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

// turn schedule off indefinitely
def	onHold() {
    def evt = createEvent(name: "effect", value: "onHold", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

//Start of added functions

def reset() {
	log.debug "reset()"
	
}

def refresh() {
	log.debug "refresh()"
    getAction("/status")
}

def ping() {
    log.debug "ping()"
    refresh()
}

def reboot() {
	log.debug "reboot()"
    def uri = "/reboot"
    getAction(uri)
}

def sync(ip, port) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
    }
}
private encodeCredentials(username, password){
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    return userpass
}

private getAction(uri){ 
  updateDNI()
  def userpass
  log.debug uri
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
    
  def headers = getHeader(userpass)

  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  return hubAction    
}

private postAction(uri, data){ 
  updateDNI()
  
  def userpass
  
  if(password != null && password != "") 
    userpass = encodeCredentials("admin", password)
  
  def headers = getHeader(userpass)
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  return hubAction    
}

private setDeviceNetworkId(ip, port = null){
    def myDNI
    if (port == null) {
        myDNI = ip
    } else {
  	    def iphex = convertIPtoHex(ip)
  	    def porthex = convertPortToHex(port)
        
        myDNI = "$iphex:$porthex"
    }
    log.debug "Device Network Id set to ${myDNI}"
    return myDNI
}

private updateDNI() { 
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
       device.deviceNetworkId = state.dni
    }
}

private getHostAddress() {
    if(getDeviceDataByName("ip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("ip")}:${getDeviceDataByName("port")}"
    }else{
	    return "${ip}:80"
    }
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private getHeader(userpass = null){
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

def toAscii(s){
        StringBuilder sb = new StringBuilder();
        String ascString = null;
        long asciiInt;
                for (int i = 0; i < s.length(); i++){
                    sb.append((int)s.charAt(i));
                    sb.append("|");
                    char c = s.charAt(i);
                }
                ascString = sb.toString();
                asciiInt = Long.parseLong(ascString);
                return asciiInt;
}

def setProgram(value, program){
   state."program${program}" = value
}

def hex2int(value){
   return Integer.parseInt(value, 10)
}

def update_needed_settings()
{
    def cmds = []
    
    def isUpdateNeeded = "NO"
    
    cmds << getAction("/config?hubIp=${device.hub.getDataValue("localIP")}&hubPort=${device.hub.getDataValue("localSrvPortTCP")}")
        
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def installed() {
	log.debug "installed()"
	configure()
}

def configure() {
    log.debug "configure()"
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) cmds
}

def updated()
{
    log.debug "updated()"
    def cmds = [] 
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (cmds != []) response(cmds)
}