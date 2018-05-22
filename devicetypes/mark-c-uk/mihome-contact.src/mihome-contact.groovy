/**
 *  MiHome Contact
 *
 *  Copyright 2018 Mark Cockcroft
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
	definition (name: "MiHome Contact", namespace: "Mark-C-uk", author: "Mark Cockcroft") {
		capability "Refresh"
		capability "Contact Sensor"

		attribute "lastCheckin", "string"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contactSensor", type:"generic", width:6, height:4, canChangeIcon: true){
            tileAttribute ("device.contactSensor", key: "PRIMARY_CONTROL") {
                 attributeState "open", label:'${name}', action:"refresh", icon:"st.contact.contact.open", backgroundColor:"##e86d13"
                 attributeState "closed", label:'${name}', action:"refresh", icon:"st.contact.contact.closed", backgroundColor:"#00a0dc"
                 attributeState "offline", label:'${name}', action: "refresh", icon:"st.switches.switch.off", backgroundColor:"#e86d13"
 			}
            tileAttribute ("device.lastCheckin", key: "SECONDARY_CONTROL") {
               	attributeState "default", label:'${currentValue}'
           	}
        }
        standardTile("refreshTile", "capability.refresh", width: 2, height: 2) {
        	state ("default", label:'Refresh', action:"refresh", icon:"st.secondary.refresh")
    	}
        
        
        main(["contactSensor"])
        details(["contactSensor", "refreshTile"])
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

//	===== Update when installed or setting updated =====
def installed() {
	log.info "installed"
	runIn(02, initialize)
}
def updated() {
	log.info "updated"
	unschedule(refreshRate)
    unschedule(off)
    unschedule(on)
    runIn(02, initialize)
}
def initialize() {
	log.info "initialize"
	state.counter = state.counter
    state.counter = 0
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
    // to look at deleting child devices?
}
//	===== Update when installed or setting updated =====
def refresh() {
	log.info "REFRESH -'$device' @ '$settings.refreshRate' min refresh rate"
	poll()
}
def poll() {
	def body = []
    if (device.deviceNetworkId.contains("/")) {
    	body = [id: (device.deviceNetworkId.tokenize("/")[0].toInteger()), socket: (device.deviceNetworkId.tokenize("/")[1].toInteger())]
    }
    else {
    	body = [id: device.deviceNetworkId.toInteger()]
    }
    def resp = parent.apiGET("/subdevices/show?params=" + URLEncoder.encode(new groovy.json.JsonBuilder(body).toString()))
log.debug "poll status- ${resp.status} data- ${resp.data}" 
    if (resp.status != 200) {
		log.error "POLL for - $device - $resp.status Unexpected result"
        state.contactSensor = "offline"
	}
    else {
log.debug "sensor state '$resp.data.data.sensor_state'"
    state.contactSensor = resp.data.data.sensor_state == 1 ? "open" : "close"
    log.info "POLL for -'$device'-'$state.state.contactSensor' - $resp.status - all good"
    }
    checkin()
}

def checkin() {
	sendEvent(name: "contactSensor", value: state.contactSensor)
	def checkinInfoFormat = (settings.checkinInfo ?: 'dd/MM/yyyy h:mm')
    def now = ''
    if (checkinInfoFormat != 'Hide') {
        try {
            now = 'Last Check-in: ' + new Date().format("${checkinInfoFormat}", location.timeZone)
        } catch (all) { }
    sendEvent(name: "lastCheckin", value: now, displayed: false)
    }
    log.info "CHECKIN -'$device', '$state.Switch' all good"
}

// parse events into attributes // not required as cloud based
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'lastCheckin' attribute

}