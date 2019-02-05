/**
 *  Copyright 2015 Stuart Buchanan
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
 *	Tado Thermostat
 *
 *	Author: Stuart Buchanan, Based on original work by Ian M with thanks
  *	Date: 2017-04-03 v1.4 updated to Tado Api V2 as the v1.6 API that was being used was deprecated by Tado.
 *	Date: 2015-04-28 v1.3 changed Presence tile as this was reporting a bug
 *	Date: 2015-04-28 v1.2 updated API call found issue where session was closed and nothing else was returned, now add number generator to input noCache statement in the query
 *	Date: 2015-04-27 v1.1 updated API call and added refresh function
 *	Date: 2015-12-04 v1.0 Initial Release
 */
 
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.Random  

preferences {
}  
 
metadata {
	definition (name: "Tado User Presence", namespace: "fuzzysb", author: "Stuart Buchanan") {
		capability "Presence Sensor"
		capability "Sensor"
		capability "Polling"
		capability "Refresh"
        
        command "arrived"
		command "departed"
        
              
	}

	// simulator metadata
	simulator {
		status "present": "presence: present"
		status "not present": "presence: not present"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.mobile-present", backgroundColor:"#53a7c0")
			state("not present", labelIcon:"st.presence.tile.mobile-not-present", backgroundColor:"#ffffff")
		}
		standardTile("refresh", "device.refresh", width: 2, height: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "presence"
		details (["presence","refresh"])
	}
}

def installed() {
	log.debug "Executing 'installed'"
	refresh()
}

def updated() {
	log.debug "Executing 'updated'"
	refresh()
}

def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def getInitialDeviceinfo() {
	log.debug "Executing 'getInitialDeviceInfo'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    parent.userStatusCommand(this)
}

def arrived() {
	log.trace "Executing 'arrived'"
    def result = sendEvent(name: "presence", value: "present")
    return result
}


def departed() {
	log.trace "Executing 'departed'"
	def result = sendEvent(name: "presence", value: "not present")
    return result
}