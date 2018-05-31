/**
 *  MiHome Motion Sensor
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
 *	VERSION HISTORY
 *	19.04.2018:	2.1 - added contact sensor capability
 *  23.11.2016:	2.0 - Remove BETA status.
 *	08.11.2016:	2.0 BETA Release 1 - Support for MiHome (Connect) v2.0. Initial version of device.
 *
 */
metadata {
	definition (name: "MiHome Motion Sensor", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Motion Sensor"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Contact Sensor"
	}

	tiles(scale: 2) {
    	multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4 ,canChangeIcon: true){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'Motion/Open', backgroundColor: "#e86d13", icon: "st.motion.motion.inactive" 
				attributeState "inactive", label:'Still/Closed', backgroundColor:"#ffffff", icon: "st.motion.motion.inactive"
			}
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
               	attributeState("default", label:'${currentValue}')
			}
    	}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
         main "motion"
        details(["motion", "refresh"])
	}
        
    def rates = [:]
		rates << ["5" : "Refresh every 5 minutes (eTRVs)"]
		rates << ["10" : "Refresh every 10 minutes (Power Monitors)"]	
		rates << ["15" : "Refresh every 15 minutes (Sockets switched by other systems)"]
		rates << ["30" : "Refresh every 30 minutes - Default (Sockets)"]
	
	preferences {
        input name: "refreshRate", type: "enum", title: "Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
		input "checkinInfo", "enum", title: "Show last Check-in info", options: ["Hide", "MM/dd/yyyy h:mma", "h:mma dd/mm/yyyy", "dd/MM/yyyy h:mm", "dd-MM-yyyy HH:mm" , "h:mma dd/MM/yy"], description: "Show last check-in info.", required: false
        }    
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'motion' attribute
}
def installed() {
	log.info "Executing 'installed'"
    updated()
}
def updated() {
	log.info "updated running"
	runIn(2, update)
}
def initialize() {
	log.info "initialize"
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
def update() {
	log.info "update running"
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
	//log.debug "Executing 'poll' for ${device} ${this} ${device.deviceNetworkId}"
    def resp = parent.apiGET("/subdevices/show?params=" + URLEncoder.encode(new groovy.json.JsonBuilder([id: device.deviceNetworkId.toInteger()]).toString()))
	if (resp.status != 200) {
		sendEvent(name: "refresh", value: '', descriptionText: "BAD Poll", isStateChange: true)
        log.error "POLL for  -'${device}' response -'${resp.status}' Unexpected Result" // end
	}
    else {
    state.sensor_state = resp.data.data.sensor_state
//log.debug "data $resp.data.data"
//log.debug "POLL for - '${device}' response -'${resp.status}' all good"
    checkin()
    }
}
def checkin() {
	sendEvent(name: "motion", value: state.sensor_state == 0 ? "inactive" : "active")
	def checkinInfoFormat = (settings.checkinInfo ?: 'dd/MM/yyyy h:mm')
    def now = ''
    if (checkinInfoFormat != 'Hide') {
        try {
            now = 'Last Check-in: ' + new Date().format("${checkinInfoFormat}", location.timeZone)
        } catch (all) { }
    sendEvent(name: "lastCheckin", value: now, displayed: false)
	}
    log.info "CHECKIN -'$device', '$state.motion' - '$state.sensor_state' all good"
}

def refresh() {
	log.debug "REFRESH -'$device' @ '$settings.refreshRate' min refresh rate"
	poll()
}