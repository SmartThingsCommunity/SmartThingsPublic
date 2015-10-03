/**
 *  Ohmconnect
 *
 *  Copyright 2015 Mark West
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
	definition (name: "OLD-Ohmconnect", namespace: "afewremarks", author: "Mark West") {
		capability "Refresh"

		attribute "ohmhour", "string"

		command "on"
		command "off"
	}

	simulator {
		// TODO: define status and reply messages here
	}
    preferences {
        input "ohmID", "text", title: "Ohm Connect ID", required: true
	}

	tiles {
		// TODO: define your main and details tiles here
        standardTile("ohmhour", "device.ohmhour", inactiveLabel: false, decoration: "flat") {
			state "on", label:'OhmHour On', action:"omhhour.on", nextState: "off"
			state "off", label:'OhmHour Off', action:"omhhour.off", nextState: "on"
	}
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'ohmhour' attribute

}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    runEvery5Minutes(updateOhmHourStatus)
}

def updateOhmHourStatus() {
	def params = [
        uri:  'https://login.ohmconnect.com/verify-ohm-hour/',
        path: ohmID,
    ]
    try {
    httpGet(params) { resp ->
    //    resp.headers.each {
    //       log.debug "${it.name} : ${it.value}"
    //    }
       
       if(resp.data.active == "True"){
       log.debug "Ohm Hour"
       
       def delay = (60 * 5)+30
		runIn(delay,endOhmHour )
       }else{
       	log.debug "Not currently an Ohm Hour"
       }
    }
} catch (e) {
    log.error "something went wrong: $e"
}

}
