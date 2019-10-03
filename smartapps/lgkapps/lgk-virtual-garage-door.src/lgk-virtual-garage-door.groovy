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
	Version 3 made compatable with two sensors and two switches M Cockcroft 11/09/2019
 */
 
definition(
    name: "LGK Virtual Garage Door",
    namespace: "lgkapps",
    author: "lgkahn kahn-st@lgk.com & Mark-C",
    description: "Sync the Simulated garage door or gate device with either 1 or two contact sesnors and either 1 or two switchs/relays. The simulated device will then control the actual door/gate. In addition, the virtual device will sync when the door/gate is opened manually, \n It also attempts to double check the door/gate was actually closed in case the beam was crossed. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("Choose the switch/relay(s) that opens and closes the gate/door"){
		input "opener", "capability.switch", title: "Garage/Gate Opener switch (may also close)", required: true
        input "closer", "capability.switch", title: "Garage/Gate Closing switch ... optional dual switch ONLY if you have two differnt switches", required: false
	}
    
	section("Choose the sensor(s) that senses if the gate/garage is open (and closed"){
		input "sensor", "capability.contactSensor", title: "Garage/Gate Sensor for when fully CLOSED (closed when closed)", required: true
        input "sensoropen", "capability.contactSensor", title: "Garage/Gate Sensor for when fully OPEN .. optional dual sensors (closed when fully open)", required: false
	}
    
	section("Choose the Virtual Gate/Garage Device "){
		input "virtualgd", "capability.doorControl", title: "Virtual or Simumated Gate/Garage Door", required: true
        input "virtualgdbutton", "capability.contactSensor", title: "same as above device .. Virtual or Simumated Gate/Garage sensor", required: true
	}
    
    section("Timeout and delays"){
		input "checkTimeout", "number", title: "Time before checking if the opening or closeing competed correctly... default 40s", required: false //, defaultValue: 25
		input "lag", "number", title: "Pause befor close to get chance for buzzer/warning..... default 1s", required: false //, defaultValue: 5
    }
    section("Closing warning"){
		input "wlight", "capability.switch", title: "Select a warning light or switch.... only pick each device once", multiple: true, required: false
        input "walarm", "capability.alarm", title: "Select a warning alarm .... only pick each device once", multiple: true, required: false
        input "wbuzzer", "capability.tone", title: "Select a warning buzzer.... only pick each device once", multiple: true, required: false
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
        	log.trace "opening virtual Garage/Gate"
            mysend("Virtual Garage/Gate Opened!","")     
            virtualgd.open("open")
		}
        else {
        	virtualgd.close("closed")
            log.trace "closing virtual Garage/Gate"
            mysend("Virtual Garage/Gate Closed!","")   
     	}
	}
}

def contactHandleropen(evt){ //only used for 2 sensor setup (open)
	def msg = ""
    def virtualgdstate = virtualgd.currentContact
    def virtualdoorstate = virtualgd.currentDoor
    if(evt.value == "closed"){
    	msg = "sending open comand"
    	virtualgd.open("open") //full open
    }
	mysend("Open Contact sensor event, '${evt.device}' is '${evt.value}',virtual contact is '$virtualgdstate' and Garage/Gate is '$virtualdoorstate', $msg","")
	log.trace "contactHandleropen - '${evt?.device} ${evt?.name}' is '${evt.value}',virtual contact is '$virtualgdstate' and Garage/Gate is '$virtualdoorstate', '$msg'"    
}

def contactHandler(evt) {
	def virtualgdstate = virtualgd.currentContact
    def virtualdoorstate = virtualgd.currentDoor
/*    
    if(sensoropen != null){ // if using 2 sensors 'sensoropen' should be closed when the door/gate is open
		if(evt.value == "closed") {
        	log.info "Dual Sensor setup"
            log.debug "closed sesnor is closed so Garage/Gate is fully closed"
       		if (wlight) {wlight.off()}
            if (walarm) {walarm.off()}
            virtualgd.close("closed") //closed
   		}
	}
	else{
    	log.info "Single Sensor setup"
*/
    	if("open" == evt.value && sensoropen == null) { //ignore open events for dual sensors
        	log.info "Single Sensor setup Open event"
    		virtualgd.open("open")
		}
		if("closed" == evt.value) {
        log.info "Sensor close event, single or dual setup"
        	//if (wbuzzer){wbuzzer.off()} //not sue how to turn buzzer 'tone' off?
        	if (wlight) {wlight.off()}
            if (walarm) {walarm.off()}
       		virtualgd.close("closed")
   		}
//    }
	mysend("Contact sensor event, virtual contact is '$virtualgdstate' and Garage/Gate is '$virtualdoorstate', sending '${evt?.device} is ${evt?.name}' to simulated device to sync","")
	log.trace "contactHandler - '${evt?.device} ${evt?.name}' is '${evt.value}' - virtual contact is '$virtualgdstate'and Garage/Gate is '$virtualdoorstate' "
}

def virtualgdcontactHandler(evt) {
	def msg = ""
	def realgdstate = sensor.currentContact
	log.info "virtual Garage/Gate event Contact/DoorState = ${evt?.device} - ${evt?.name} - ${evt.value}" //virtualgd.label
	if("opening" == evt.value) {
    	if (realgdstate != "open") { // not open state so open
        	msg = "sending open command to ${opener.label} the actual controler"
            opener.on()
     	}
	}
	else if("closing" == evt.value) {
    	if (realgdstate != "closed") { // not closed state so close
        	if (wbuzzer){wbuzzer.beep()}
        	if (wlight) {wlight.on()}
            if (walarm) {walarm.both()}
        	if (closer != null){ //2 switches
        		msg = "sending close command to ${closer.label} the actual controler"
                runIn( lag ?: 6, "lagclose")
        	}
        	else { // single switch
        		msg = "sending close command to ${opener.label} the actual controler"
                runIn( lag ?: 6, "lagopen")
        	}
        }
	}
    else { msg = "${evt?.device} ${evt.value}"} // open/close events
    if (msg != ""){
    	mysend("$msg","")
    	log.trace "virtualgdcontactHandler - $msg"
    }
    runIn(checkTimeout ?: 40, checkIfActually)
}

def lagclose(){ 
	log.debug lag ?: 1
	closer.on()
}
def lagopen(){ 
	log.debug lag ?: 1
	opener.on()
}

def checkIfActually() {
	def msg = ""
	def realgdstate = sensor.currentContact
	def virtualgdstate = virtualgd.currentContact
    def virtualdoorstate = virtualgd.currentDoor
    def prestate = "checkIfActually .dewl is $checkTimeout sec.. Actual sensor state ${sensor.label} = $realgdstate, Virtual Garage/Gate state  = contact is '$virtualgdstate' & and Garage/Gate is '$virtualdoorstate' "
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
    	mysend("Garage/Gate is $msg - this was not the expected state of $prestate", "WARN")
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
            log.trace("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.trace("sending text message")
            sendSms(phone1, msg)
        }
    }
}