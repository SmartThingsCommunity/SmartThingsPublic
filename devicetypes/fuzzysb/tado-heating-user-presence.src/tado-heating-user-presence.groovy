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
 *	Date: 2015-04-28 v1.3 changed Presence tile as this was reporting a bug
 *	Date: 2015-04-28 v1.2 updated API call found issue where session was closed and nothing else was returned, now add number generator to input noCache statement in the query
 *	Date: 2015-04-27 v1.1 updated API call and added refresh function
 *	Date: 2015-12-04 v1.0 Initial Release
 */
 
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.Random  

preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
	input("tadouser", "text", title: "Tado User", description: "Your Tado User")
}  
 
metadata {
	definition (name: "Tado Heating User Presence", namespace: "fuzzysb", author: "Stuart Buchanan") {
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

// Parse incoming device messages to generate events
private parseResponse(resp) {
	def result
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        def evtJson = new groovy.json.JsonOutput().toJson(resp.data)
        def json = new JsonSlurper().parseText(evtJson)
		def appuserarray = new groovy.json.JsonOutput().toJson(json.appUsers)
        def list = new JsonSlurper().parseText(appuserarray)
		list.each {
    		if ((it.nickname).capitalize() == (settings.tadouser).capitalize()) {
            	log.debug("Found Tado User : " + it.nickname)
                if (it.geoTrackingEnabled == true) {
                	log.debug("Users GeoTracking is Enabled")
                	if (it.geolocationIsStale == false){
                    	log.debug("Users Current Relative Position is : " + it.relativePosition )
                		if (it.relativePosition == 0) {
                  		  	result = arrived()
                  		}else{
                  		  	result = departed()
                 	   }
                	}else{
                	log.debug("Geolocation is Stale Skipping")
                	}
                }else{
                	log.debug("Users GeoTracking Not Enabled")
                }     
		}  
        }
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
    return result  
}

def installed() {
	log.debug "Executing 'installed'"
	statusCommand()
}

def updated() {
	log.debug "Executing 'updated'"
	statusCommand()
}

def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    statusCommand()   
}


private sendCommand(method, args = []) {
    def methods = [
		'status': [
        			uri: "https://my.tado.com", 
                    path: "/mobile/1.6/getAppUsersRelativePositions", 
                    requestContentType: "application/json", 
                    query: [username:settings.username, password:settings.password, noCache:args[0], webapp:1]
                    ],
	]

	def request = methods.getAt(method)
    
    log.debug "Http Params ("+request+")"
    
    try{
        log.debug "Executing 'sendCommand'"
        
        if (method == "status"){
            httpGet(request) { resp ->            
                parseResponse(resp)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        log.debug("___exception: " + e)
    }
}



// Commands
def statusCommand(){
	log.debug "Executing 'sendCommand.statusCommand'"
    Random rand = new Random()
    int min = 10000
    int max = 99999
    int randomNum = rand.nextInt((max - min) + 1) + min
	sendCommand("status",[randomNum])
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

