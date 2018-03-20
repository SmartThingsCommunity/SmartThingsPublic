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
 *	17.09.2017: 2.0a - Disable setting device to Offline on unexpected API response.
 *	23.11.2016:	2.0 - Remove extra logging.
 *
 *	10.11.2016:	2.0 BETA Release 3 - Merge Light Switch and Adapter functionality into one device type.
 *	10.11.2016:	2.0 BETA Release 2.1 - Bug fix. Stop NumberFormatException when creating body object.
 *	09.11.2016:	2.0 BETA Release 2 - Added support for MiHome multiple gangway devices.
 *
 *	08.11.2016:	2.0 BETA Release 1 - Support for MiHome (Connect) v2.0. Inital version of device.
 */
metadata {
	definition (name: "MiHome Adapter", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		//capability "Actuator"
		//capability "Polling"
		capability "Refresh"
		capability "Switch"
        
        command "on"
        command "off"
        
        attribute "lastCheckin", "String"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type:"lighting", width:6, height:4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                 attributeState "on", label:'${name}', action:"off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
                 attributeState "off", label:'${name}', action:"on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
                 attributeState "turningOn", label:'${name}', icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
                 attributeState "turningOff", label:'${name}', icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
                 attributeState "offline", label:'${name}', icon:"st.switches.switch.off", backgroundColor:"#ff0000"
 			}
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
               	attributeState("default", label:'${currentValue}')
           	}
        }
        standardTile("refreshTile", "capability.refresh", decoration: "ring") {
        	state "default", label:'', action:"refresh", icon:"st.secondary.refresh"
    	}
        //standardTile("onButton", "device.switch", inactiveLabel: false, width: 2, height: 2) {
		//state "default", label: 'on', action: "on"
        //}
     
        //standardTile("offButton", "device.switch", inactiveLabel: false, width: 2, height: 2) {
		//state("off", label: 'Over Ride Off', action:"off")
        //}
        
        main("switch")
        details("switch", "refreshTile") //"onButton", "offButton",
	}
    def rates = [:]
	rates << ["5" : "Refresh every 5 minutes (eTRVs)"]
	rates << ["10" : "Refresh every 10 minutes (power mon.)"]	
	rates << ["15" : "Refresh every 15 minutes (socets)"]
	rates << ["30" : "Refresh every 30 minutes (default)"]

	preferences {
		section(title: "Check-in Interval") {
        paragraph "Run a Check-in procedure every so often."
        input name: "refreshRate", type: "enum", title: "Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
		}
        section(title: "Check-in Info") {
            paragraph "Display check-in info"
            input "checkinInfo", "enum", title: "Show last Check-in info", options: ["Hide", "MM/dd/yyyy h:mm", "MM-dd-yyyy h:mm", "dd/MM/yyyy h:mm", "dd-MM-yyyy h:mm"], description: "Show last check-in info.", defaultValue: "dd/MM/yyyy h:mm", required: true
        }
	}
}
// parse events into attributes
def parse(String description) {
	log.debug "Parsing ${description}"
	// TODO: handle 'switch' attribute
}

//	===== Update when installed or setting changed =====
def installed() {
	update()
}

def updated() {
	unschedule()
    runIn(2, update)
}

def update() {
	switch(refreshRate) {
		case "5":
			runEvery5Minutes(refresh)
			log.info "Refresh Scheduled for every 5 minutes"
			break
		case "10":
			runEvery10Minutes(refresh)
			log.info "Refresh Scheduled for every 10 minutes"
			break
		case "15":
			runEvery15Minutes(refresh)
			log.info "Refresh Scheduled for every 15 minutes"
			break
		default:
			runEvery30Minutes(refresh)
			log.info "Refresh Scheduled for every 30 minutes"
	}
}

def uninstalled() {
    unschedule()
}
// handle commands
def poll() {
	log.debug "Executing poll for ${device} ${this} ${device.deviceNetworkId}"
    def body = []
    if (device.deviceNetworkId.contains("/")) {
    	body = [id: (device.deviceNetworkId.tokenize("/")[0].toInteger()), socket: (device.deviceNetworkId.tokenize("/")[1].toInteger())]
    }
    //else {
    	body = [id: device.deviceNetworkId.toInteger()]
    //}
    def resp = parent.apiGET("/subdevices/show?params=" + URLEncoder.encode(new groovy.json.JsonBuilder(body).toString()))
    if (resp.status != 200) {
		log.error "Unexpected result in poll ${resp.status} ${resp.data}"
        //sendEvent(name: "switch", value: "offline", descriptionText: "The device is offline")
		return []
	}
    def power_state = resp.data.data.power_state
    if (power_state != null) {
    	sendEvent(name: "switch", value: power_state == 0 ? "off" : "on")
    }
	log.info "POLL All good ${resp.status} ${device}"
    
    
def lastRefreshed = state.lastRefreshed
    state.lastRefreshed = now()
    def checkinInfoFormat = (settings.checkinInfo ?: 'dd/MM/yyyy h:mm')
    def now = ''
    if (checkinInfoFormat != 'Hide') {
        try {
            now = 'Last Check-in: ' + new Date().format("${checkinInfoFormat}a", location.timeZone)
        } catch (all) { }
    }
    sendEvent(name: "lastCheckin", value: now, displayed: false)
}


def refresh() {
	log.info "Executing adapter refresh"
	runIn(03, poll)
}

def on() {
	state.OnCounter = state.OnCounter
	log.info "Executing on"
    def body = []
    if (device.deviceNetworkId.contains("/")) {
    	body = [id: (device.deviceNetworkId.tokenize("/")[0].toInteger()), socket: (device.deviceNetworkId.tokenize("/")[1].toInteger())]
    } else {
    	body = [id: device.deviceNetworkId.toInteger()]
    }
    def resp = parent.apiGET("/subdevices/power_on?params=" + URLEncoder.encode(new groovy.json.JsonBuilder(body).toString()))
    if (resp.status != 200) {
    		log.error "Unexpected result in on poll ${resp.status} ${resp.data}"
          	if (state.OnCounter == null || state.OnCounter >= 7) {
				state.OnCounter = 0
			}
        	if (state.OnCounter == 6) {
            	sendEvent(name: "switch", value: "turningOn", descriptionText: "error on ${state.OnCounter} times please try again later")
                state.OnCounter = 0
                sendEvent(name: "switch", value: "offline", descriptionText: "The device is offline")
                return []
			}
		state.OnCounter = state.OnCounter + 1
        sendEvent(name: "switch", value: "turningOn", descriptionText: "error on ${state.OnCounter} try")
        log.warn "runnting on again ${state.OnCounter} attempt"
        runIn(013, on)
	}
   	else {
    	state.OnCounter = 0
        log.info "ON All good ${resp.status}"
  def power_state = resp.data.data.power_state
    		if (power_state != null) {
    			sendEvent(name: "switch", value: power_state == false ? "off" : "on")
    		} 
    }
}

def off() {
	state.Offcounter = state.Offcounter
	log.info "Executing off"
    def body = []
    if (device.deviceNetworkId.contains("/")) {
    	body = [id: (device.deviceNetworkId.tokenize("/")[0].toInteger()), socket: (device.deviceNetworkId.tokenize("/")[1].toInteger())]
    }
    else {
    	body = [id: device.deviceNetworkId.toInteger()]
    }
	def resp = parent.apiGET("/subdevices/power_off?params=" + URLEncoder.encode(new groovy.json.JsonBuilder(body).toString()))
    if (resp.status != 200) {
    	log.error "Unexpected result in off poll ${resp.status} ${resp.data}"
        if (state.Offcounter == null || state.Offcounter >= 7) {
			state.Offcounter = 0
		}
        	if (state.Offcounter == 6) {
            	sendEvent(name: "switch", value: "turningOff", descriptionText: "error off ${state.Offcounter} times please try again later")
                state.Offcounter = 0
                sendEvent(name: "switch", value: "offline", descriptionText: "The device is offline")
                return []
			}
		state.Offcounter = state.Offcounter + 1
        sendEvent(name: "switch", value: "turningOff", descriptionText: "error off ${state.Offcounter} try")
        log.warn "running off again ${state.Offcounter} attempt"
		runIn(07, off)
	}
   	
    else {
    	state.Offcounter = 0
        log.info "OFF All good ${resp.status}"
        def power_state = resp.data.data.power_state
    		if (power_state != null) {
    			sendEvent(name: "switch", value: power_state == false ? "off" : "on")
    		} 
    }
}