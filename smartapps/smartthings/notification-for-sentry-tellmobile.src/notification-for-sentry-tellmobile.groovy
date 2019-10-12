/**
 *  Sentry TellMobile
 *
 *  Copyright 2019 Pradeep Murugesan
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
 
final emailaddress = "<replace with your registered email address>"
 
definition(
    name: "Notification for Sentry TellMobile",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Get notification on motion and door open.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("When the door opens..."){
		input "contacts", "capability.contactSensor", multiple:true
    	input "motions", "capability.motionSensor", multiple:true
        input "email", "text", title: "E-Mail Address", required: true
	}
}

def installed()
{
	subscribe(contacts, "contact.open", contactOpenHandler)
    subscribe(contacts, "contact.closed", contactCloseHandler)
    subscribe(motions, "motion.active", notifyMotionDetected)
    
}

def updated()
{
	unsubscribe()
	subscribe(contacts, "contact.open", contactOpenHandler)
    subscribe(contacts, "contact.closed", contactCloseHandler)
    subscribe(motions, "motion.active", notifyMotionDetected)
}

def contactCloseHandler(evt) {
	log.trace "${email}"
    log.debug contact.currentValue("contact")
}

def contactOpenHandler(evt) {
	def params = [
        uri: "http://sentrytell.com",
        path: "/sentry_tell_api/api/add_event.php",
        query: [
            "email": "${email}", 
            "sensor" : evt.getDevice().getDisplayName(),
            "event" : "open",
            "date" : "$evt.date"
        ]
    ]
    try {
        httpGet(params) { resp ->   
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
	log.debug "${email} " + evt.getDevice().getDisplayName() +  " open $evt.date"
}

mappings {
    path("/Collect") {
        action: [
            GET: "getDevices",
            
        ]
    }
}

def getDevices() {
	String devices = ''
    for (it in (contacts ?: [])) {
    	if (devices.length() > 1){
        	devices = devices + ","
        }
		log.debug it.getDisplayName()
        devices = devices + it.getDisplayName()
        
	}
    for (it in (motions ?: [])) {
    	if (devices.length() > 1){
        	devices = devices + ","
        }
		log.debug it.getDisplayName()
        devices = devices + it.getDisplayName()
        
	}
    return devices
}

def notifyMotionDetected(evt) {
	def params = [
        uri: "http://sentrytell.com",
        path: "/sentry_tell_api/api/add_event.php",
        query: [
            "email": "${email}", 
            "sensor" : evt.getDevice().getDisplayName(),
            "event" : "motion",
            "date" : "$evt.date"
        ]
    ]
    try {
        httpGet(params) { resp ->   
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
	log.debug "${email} " + evt.getDevice().getDisplayName() +  " open $evt.date"
}