/**
 *  MiHome Adapter
 *
 *  Copyright 2016 Alex Lee Yuk Cheung
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
 *	VERSION HISTORY - FORMER VERSION NOW RENAMED AS ADAPTER PLUS
 *				3.1	-	refersh period updated, new app terms added
 *				3.0 -	Code cleansed debugging removed
 *				2.5	-	Major review to move schdualing into the DH, created error handler / seetings added for refresh rate and check in time
 *	17.09.2017: 2.0a -	Disable setting device to Offline on unexpected API response.
 *	23.11.2016:	2.0 - Remove extra logging.
 *	10.11.2016:	2.0 BETA Release 3 - Merge Light Switch and Adapter functionality into one device type.
 *	10.11.2016:	2.0 BETA Release 2.1 - Bug fix. Stop NumberFormatException when creating body object.
 *	09.11.2016:	2.0 BETA Release 2 - Added support for MiHome multiple gangway devices.
 *
 *	08.11.2016:	2.0 BETA Release 1 - Support for MiHome (Connect) v2.0. Inital version of device.
 */
metadata {
	definition (name: "MiHome Adapter", namespace: "alyc100", author: "Alex Lee Yuk Cheung & updeated by Mark Cockcroft", ocfDeviceType: "oic.d.switch", mnmn: "SmartThings", vid: "generic-switch") {
		//capability "Polling" // polling disabled as refresh is schedualed in preferences (rates) 
		capability "Actuator"	// best practice
        capability "Sensor"		// best practice
        capability "Refresh"
		capability "Switch"
        capability "Health Check"
        
        command "on"
        command "off"
        
        attribute "lastCheckin", "String"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type:"lighting", width:6, height:4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                 attributeState ("on", label:'${name}', action:"off", icon:"st.Home.home30", backgroundColor:"#00a0dc", nextState:"turningOff")
                 attributeState ("off", label:'${name}', action:"on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn")
                 attributeState ("turningOn", label:'${name}', icon:"st.Home.home30", backgroundColor:"#f1d801", nextState:"on")
                 attributeState ("turningOff", label:'${name}', icon:"st.Home.home30", backgroundColor:"#f1d801", nextState:"off")
                 attributeState ("offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#e86d13")
 			}
            tileAttribute ("device.lastCheckin", key: "SECONDARY_CONTROL") {
               	attributeState ("default", label:'${currentValue}')
           	}
        }
        standardTile("refresh", "device.refresh", width: 2, height: 2) {
        	state ("default", label:'Refresh', action:"refresh", icon:"st.secondary.refresh", defaultState: true)
            state ("bad", label:'bad Refresh', action:"refresh", icon:"st.secondary.refresh", backgroundColor:"#e86d13")
    	}
        standardTile("onButton", "capability.switch", width: 2, height: 2) { //was capability.Switch
			state ("default", label: 'On', action:"on", icon:"st.Home.home30")
        }
        standardTile("offButton", "capability.switch", width: 2, height: 2) { //was capability.Switch
			state ("default", label: 'Off', action:"off", icon:"st.Home.home30")
        }
        
        main "switch"
        details(["switch", "onButton", "offButton", "refresh"])
	}
   
	preferences {
        input "checkinInfo", "enum", title: "Show last Check-in info", options: ["Hide", "Show"], description: "Show last info.", required: false
    }
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing ${description}"
	// not required as cloud based
}

//	===== Update when installed or setting updated =====
def installed() {
	log.info "installed"
	runIn(02, initialize)
}
def updated() {
	log.info "updated"
	unschedule()
	runIn(02, initialize)
}
def initialize() {
	log.info "initialize"
	state.counter = state.counter
    state.counter = 0
	runEvery5Minutes(poll, [overwrite: true])
}

def uninstalled() {
    unschedule()
    // to look at deleting child devices?
}
//	===== Update when installed or setting updated =====
def refresh() {
	//log.debug "refresh triggerd ${device}"
	unschedule(refresh)
	runEvery5Minutes(poll, [overwrite: true])
    poll()
}
def poll() {
    def resppar = parent.state.data 		//pull data from parent app
    if (resppar != null){
							// 	log.debug "full data = ${resppar}"
  		def dvid = device.deviceNetworkId.toInteger()
		def dvkey1 = resppar.data.id.findIndexOf { it == (dvid) }
							//	log.debug "ALL $dvid id '$dvkey1' - ${resppar.data[(dvkey1)]}"
        					//log.debug "mihome index - '$dvkey1', name - ${resppar.data[(dvkey1)].label}"
    	state.Switch = resppar.data[(dvkey1)].power_state == 1 ? "on" : "off"//resp.data.data.power_state == 1 ? "on" : "off"
    	state.updatedat = resppar.data[(dvkey1)].parent_device_last_seen_at
        sendEvent(name: "refresh", value: "default", displayed: false)
	}
    else {
    	sendEvent(name: "refresh", value: "bad", descriptionText: "The device failed POLL", isStateChange:true)
        log.warn " POLL - ${device} failed POLL"
    }
    checkin()
}

def checkin() {
	sendEvent(name: "switch", value: state.Switch)
	if (checkinInfoFormat != 'Hide') {
   		sendEvent(name: "lastCheckin", value: state.updatedat, displayed: false)
	}
    log.info "CHECKIN complete-'${device}', '${state.Switch}' parent device updated at ${state.updatedat}"
}

def on() {
	unschedule(poll)
	def body = []
    if (device.deviceNetworkId.contains("/")) {
    	body = [id: (device.deviceNetworkId.tokenize("/")[0].toInteger()), socket: (device.deviceNetworkId.tokenize("/")[1].toInteger())]
    }
    else {
    	body = [id: device.deviceNetworkId.toInteger()]
    }
    def resp = parent.apiGET("subdevices/power_on?params=" + URLEncoder.encode(new groovy.json.JsonBuilder(body).toString()))
    if (resp.status != 200) {
    		log.warn "ON - '${device}' response -'${resp.status}' - '${resp.data}' Unexpected result"
          	if (state.counter == null || state.counter >= 5) {
				state.counter = 0
			}
            if (state.counter < 5) {
            	state.counter = state.counter + 1
        		sendEvent(name: "switch", value: "turningOn", descriptionText: "error turning on '${state.counter}' try", isStateChange: true)
        		log.warn "RERUN ON - '${device}', '${state.counter}' attempt"
        		runIn(13, on)
            }          
            else { 
            	sendEvent(name: "switch", value: "offline", descriptionText: "Error turning On '${state.counter}' times. The on command was not actioned. The device is offline", isStateChange: true)
                unschedule(on)
                state.counter = 0
                log.error "ON ERROR - '${device}' on command was not processed"
			}
	}
	else {
    	unschedule(on)
       	state.counter = 0
//log.debug "power '$resp.data.data.power_state'"
        state.Switch = resp.data.data.power_state == true ? "on" : "off"
        log.info "ON - '${device}' '${state.Switch}' all good '${resp.status}'"
    	checkin()
    }
    runIn(5*60, refresh)
}

def off() {
	unschedule(poll)
	//log.debug "Executing off"
    def body = []
    if (device.deviceNetworkId.contains("/")) {
    	body = [id: (device.deviceNetworkId.tokenize("/")[0].toInteger()), socket: (device.deviceNetworkId.tokenize("/")[1].toInteger())]
    }
    else {
    	body = [id: device.deviceNetworkId.toInteger()]
    }
	def resp = parent.apiGET("subdevices/power_off?params=" + URLEncoder.encode(new groovy.json.JsonBuilder(body).toString()))
// log.debug "off data status- ${resp.status} data- ${resp.data}"
    if (resp.status != 200) {
    	log.error "Unexpected result in off poll ${resp.status}"
        if (state.counter == null || state.counter >= 7) {
			state.counter = 0
					}
            if (state.counter < 5) {
            state.counter = state.counter + 1
        	sendEvent(name: "switch", value: "turningOff", descriptionText: "error turning off ${state.counter} try", isStateChange: true)
        	log.warn "runnting off again ${state.counter} attempt"
        	runIn(13, off)
            }            
            else {
            	sendEvent(name: "switch", value: "offline", descriptionText: "Error turning Off ${state.counter} times. The device is offline", isStateChange: true)
                unschedule(off)
                state.counter = 0
			}
	}
	else {
    	unschedule(off)
        state.counter = 0
//log.debug "power '$resp.data.data.power_state'"
        state.Switch = resp.data.data.power_state == true ? "on" : "off"
        log.info "Off - '${device}' '${state.Switch}' all good '${resp.status}'"
    	checkin()
    }
    runIn(5*60, refresh)
}