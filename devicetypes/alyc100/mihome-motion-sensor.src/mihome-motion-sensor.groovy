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
    
	preferences {
        input "checkinInfo", "enum", title: "Show last Check-in info", options: ["Hide", "Show"], description: "Show last check-in info.", required: false
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
    runIn(2, update)
}
def update() {
	log.info "update running"
	unschedule(refresh)
    runEvery5Minutes(poll, [overwrite: true])
    
}
def uninstalled() {
    unschedule()
}

def refresh() {
	//log.debug "REFRESH -'$device'"
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
							//log.debug "ALL $dvid id '$dvkey1' - ${resppar.data[(dvkey1)]}"
    state.sensor_state = resppar.data[(dvkey1)].sensor_state
    state.updatedat = resppar.data[(dvkey1)].parent_device_last_seen_at
	}
    else {
   	sendEvent(name: "refresh", value: " ", descriptionText: "The device failed POLL")
        log.warn " POLL - ${device} failed POLL"
    }
    checkin()
}

def checkin() {
	sendEvent(name: "motion", value: state.sensor_state == 0 ? "inactive" : "active")
	if (checkinInfoFormat != 'Hide') {
    	sendEvent(name: "lastCheckin", value: state.updatedat, displayed: false)
	}
    log.info "CHECKIN -'$device', '${state.sensor_state == 0 ? "inactive" : "active"} motion' - '$state.sensor_state' all good"
}