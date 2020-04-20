/**
 *  Gowild Dev Test 1
 *
 *  Copyright 2018 Yee Hui Poh
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
definition(
    name: "Gowild Smart Home Dev",
    namespace: "sg.gowild.dev",
    author: "Yee Hui Poh",
    description: "Gowild XB Smart Home Dev",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Add switches") {
		input "switches", "capability.switch", multiple: true, required: false
	}
    
    section("Add door sensors") {
    	input "contacts", "capability.contactSensor", multiple: true, required: false
    }
    
    section("Add motion sensors") {
    	input "motions", "capability.motionSensor", multiple: true, required: false
    }
    
    section("Add presence sensors") {
    	input "presences", "capability.presenceSensor", multiple: true, required: false
    }
}

mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/switches/:id/:command") {
    action: [
      PUT: "updateSwitches"
    ]
  }
  path("/contacts") {
  	action: [
      GET: "listContactSensors"
    ]
  }
  path("/motions") {
  	action: [
      GET: "listMotionSensors"
    ]
  }
  path("/presences") {
  	action: [
      GET: "listPresenceSensors"
    ]
  }
}

def listContactSensors() {
	def resp = []
    contacts.each {
    	resp << [id: it.id, name: it.displayName, value: it.currentValue("contact")]
    }
}

def listMotionSensors() {
	def resp = []
    motions.each {
    	resp << [id: it.id, name: it.displayName, value: it.currentValue("motion")]
    }
}

def listPresenceSensors() {
	def resp = []
    presences.each {
    	resp << [id: it.id, name: it.displayName, value: it.currentValue("presence")]
    }
}

def listSwitches() {
    def resp = []
    switches.each {
      resp << [id: it.id, name: it.displayName, value: it.currentValue("switch")]
    }
    return resp
}

void updateSwitches() {
    // use the built-in request object to get the command parameter
    def id = params.id
    def command = params.command

    // all switches have the command
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    switch(command) {
        case "on":
        	for(switchObj in switches) {
            	if(switchObj.id == id) {
                	switchObj.on()
                }
            }
//            switches.on()
            break
        case "off":
			for(switchObj in switches) {
            	if(switchObj.id == id) {
                	switchObj.off()
                }
            }
//            switches.off()
            break
        default:
            httpError(400, "$command is not a valid command for all switches specified")
    }
}

void presenceDetectedHandlerPresent(evt) {
    log.debug "presenceDetectedHandlerPresent called: $evt"
    def params = [
        uri: "http://api.gowild.sg:58080",
        path: "/push/speak",
        body: [
            xb_id: 58,
            language: "english",
            text: "Hey, welcome back!!"
        ]
    ]
    try {
        httpPostJson(params) { resp ->
            log.debug "response status code: ${resp.status}"
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

void presenceDetectedHandlerNotPresent(evt) {
    log.debug "presenceDetectedHandlerNotPresent called: $evt"
    
    def params = [
        uri: "http://api.gowild.sg:58080",
        path: "/push/speak",
        body: [
        	xb_id: 58,
            language: "english",
            text: "Goodbye, see you again"
        ]
    ]
    try {
        httpPostJson(params) { resp ->
            log.debug "response status code: ${resp.status}"
			log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

void contactDetectedHandlerOpen(evt) {
    log.debug "contactDetectedHandlerOpen called: $evt"
    def params = [
        uri: "http://api.gowild.sg:58080",
        path: "/push/speak",
        body: [
            xb_id: 58,
            language: "english",
            text: "hello, welcome back"
        ]
    ]
    try {
        httpPostJson(params) { resp ->
            log.debug "response status code: ${resp.status}"
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

void contactDetectedHandlerClosed(evt) {
    log.debug "contactDetectedHandlerOpen called: $evt"
    
    def params = [
        uri: "http://api.gowild.sg:58080",
        path: "/push/speak",
        body: [
        	xb_id: 58,
            language: "english",
            text: "goodbye, have a nice day"
        ]
    ]
    try {
//        httpPostJson(params) { resp ->
//            log.debug "response status code: ${resp.status}"
//			log.debug "response data: ${resp.data}"
//        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

void motionDetectedHandlerActive(evt) {
    log.debug "motionDetectedHandlerActive called: $evt"
    def params = [
        uri: "http://api.gowild.sg:58080",
        path: "/push/speak",
        body: [
            xb_id: 58,
            language: "english",
            text: "Hey, you are not supposed to come here"
        ]
    ]
    try {
        httpPostJson(params) { resp ->
            log.debug "response status code: ${resp.status}"
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

void motionDetectedHandlerInactive(evt) {
    log.debug "motionDetectedHandlerInactive called: $evt"
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(contacts, "contact.open", contactDetectedHandlerOpen)
    subscribe(contacts, "contact.closed", contactDetectedHandlerClosed)
    
    subscribe(motions, "motion.active", motionDetectedHandlerActive)
    subscribe(motions, "motion.inactive", motionDetectedHandlerInactive)
    
    subscribe(presences, "presence.present", presenceDetectedHandlerPresent)
    subscribe(presences, "presence.not present", presenceDetectedHandlerNotPresent)
}

// TODO: implement event handlers