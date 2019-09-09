/**
 *  Copyright 2015 SmartThings
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
 *  Author: LGKahn kahn-st@lgk.com
 *  version 2 user defineable timeout before checking if door opened or closed correctly. Raised default to 25 secs. You can reduce it to 15 secs. if you have custom simulated door with < 6 sec wait.
 */
 
definition(
    name: "LGK Virtual Garage Door",
    namespace: "lgkapps",
    author: "lgkahn kahn-st@lgk.com",
    description: "Sync the Simulated garage door device with 2 actual devices, either a tilt or contact sensor and a switch or relay. The simulated device will then control the actual garage door. In addition, the virtual device will sync when the garage door is opened manually, \n It also attempts to double check the door was actually closed in case the beam was crossed. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("Choose the switch/relay that opens/closes the garage"){
		input "opener", "capability.switch", title: "Physical Garage Opener", required: true
	}
    section("Choose the switch/relay that CLOSES the garage... if different"){
		input "closer", "capability.switch", title: "Physical Garage Opener ... optional dual switch", required: false
	}
	section("Choose the sensor that senses if the garage is open/close"){
		input "sensor", "capability.contactSensor", title: "Physical Garage Door Open/Closed", required: true
	}
    section("Choose the sensor for OPEN.. optional "){
		input "sensoropen", "capability.contactSensor", title: "Physical Garage Door fully Open .. optional dual sensors", required: false
	}
    
	section("Choose the Virtual Garage Door Device "){
		input "virtualgd", "capability.doorControl", title: "Virtual Garage Door", required: true
	}
    
	section("Choose the Virtual Garage Door Device sensor ... same as above device"){
		input "virtualgdbutton", "capability.contactSensor", title: "same as above device", required: true
	}
    
    section("Timeout before checking if the door opened or closed correctly?"){
		input "checkTimeout", "number", title: "Door Operation Check Timeout?", required: true, defaultValue: 25
	}
    
     section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }

}

def installed() {
	installer()
}
def updated() {
	unsubscribe()
	installer()
}
def installer(){
	def realgdstate = sensor.currentContact
	def virtualgdstate = virtualgd.currentContact
    
    subscribe(sensor, "contact", contactHandler)
    subscribe(virtualgdbutton, "contact", virtualgdcontactHandler)
    subscribe(virtualgdbutton, "door", virtualgdcontactHandler)
    subscribe(sensoropen, "contact", contactHandleropen)
    
	log.debug "updated ... Real Contact state closed =  '$realgdstate'  ... Virtual door Contact state= $virtualgd.currentContact"
    
    if (realgdstate != virtualgdstate) { // sync them up if need be set virtual same as actual
        if (realgdstate == "open") {
        	log.debug "opening virtual door"
            mysend("Virtual Garage Door Opened!","")     
            virtualgd.open("open")
		}
        else {
        	virtualgd.close("closed")
            log.debug "closing virtual door"
            mysend("Virtual Garage Door Closed!","")   
     	}
	}
}

def contactHandleropen(evt){
	def msg = ""
    def virtualgdstate = virtualgd.currentContact
    def virtualdoorstate = virtualgd.currentDoor
	log.info "contactHandleropen '${evt.device}' it is '${evt.value}'"
    if(evt.value == "closed"){
    	msg = "sending open comand"
    	virtualgd.open("open") //open
    }
    if(evt.value == "open"){
    	msg =  "sending open command"
        virtualgd.close("closing") //closing
    }
	mysend("Open Contact sensor event, '${evt.device}' is '${evt.value}',virtual contact is '$virtualgdstate' and door is '$virtualdoorstate', $msg","")
	log.trace "contactHandleropen - '${evt?.device} ${evt?.name}' is '${evt.value}',virtual contact is '$virtualgdstate' and door is '$virtualdoorstate', '$msg'"    
}

def contactHandler(evt) {
	def virtualgdstate = virtualgd.currentContact
    def virtualdoorstate = virtualgd.currentDoor
    
    if(sensoropen != null){ // if using 2 sensors 'sensoropen' should be closed when the door/gate is open
 	   log.debug "fully closed sensor"
       if(evt.value =="open") {
			log.debug "closed sesnor is open so gate must be opening"
    		virtualgd.open("opening") //opening
		}
		if(evt.value == "closed") {
            log.debug "closed sesnor is closed so gate is fully closed"
       		virtualgd.close("closed") //closed
   		}
      }
	else{ // single sensor setup 
    	log.info "Single Sensor setup"
    	if("open" == evt.value) {
    		virtualgd.open("open")
		}
		if("closed" == evt.value) {
       		virtualgd.close("closed")
   		}
    }
	mysend("Contact sensor event, virtual contact is '$virtualgdstate' and door is '$virtualdoorstate', sending '${evt?.device} is ${evt?.name}' to simulated device to sync","")
	log.trace "contactHandler - '${evt?.device} ${evt?.name}' is '${evt.value}' - virtual contact is '$virtualgdstate'and door is '$virtualdoorstate' "
}

def virtualgdcontactHandler(evt) {
	def msg = ""
	def realgdstate = sensor.currentContact
	log.info "virtual gd event Contact/DoorState = ${evt?.device} - ${evt?.name} - ${evt.value}" //virtualgd.label
	if("opening" == evt.value) {
    	if (realgdstate != "open") { // not open state so open
        	msg = "sending open command to ${opener.label} the actual garage controler"
         	opener.on()
     	}
	}
	if("closing" == evt.value) {
    	if (realgdstate != "closed") { // not closed state so close
        	if (closer != null){
        		msg = "sending close command to ${closer.label} the actual garage controler"
        		closer.on()
        	}
        	else {
        		msg = "sending close command to ${opener.label} the actual garage controler"
        		opener.on()
        	}
        }
	}
    if("closed" == evt.value) { msg = "closed"}
    if("open" == evt.value) { msg = "open"}
    
    if (msg != ""){
    	mysend("$msg","")
    	log.trace "virtualgdcontactHandler - $msg"
    }
    runIn(checkTimeout, checkIfActually)
}

def checkIfActually() {
	def msg = ""
	def realgdstate = sensor.currentContact
	def virtualgdstate = virtualgd.currentContact
    def virtualdoorstate = virtualgd.currentDoor
    def prestate = "checkIfActually .dewl is $checkTimeout sec.. Actual sensor state ${sensor.label} = $realgdstate, Virtual door state  = contact is '$virtualgdstate' & and door is '$virtualdoorstate' "
	log.info "$prestate"
    // sync them up if need be set virtual same as actual
    if (realgdstate == "open") {
    	if (virtualdoorstate != "open" || virtualgdstate != "open") {
    		msg = realgdstate
        	virtualgd.open("open")
        }
    }
	if (realgdstate == "closed") {
    	if (virtualdoorstate != "closed" || virtualgdstate != "closed") {
			msg = realgdstate
			virtualgd.close("closed")
        }
    }
    if (msg != ""){
    	mysend("Door is $msg - this was not the expected state of $prestate", "WARN")
    	log.warn "checkIfActually $msg"
        sendEvent (name:"Checking Event",  value: prestate)
    }
}

private mysend(msg, error) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No" || error == "WARN") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }
}