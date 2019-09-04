/**
 *  Smart Security Camera
 *  Version 1.2.9
 *  Copyright 2016 BLebson
 *  Based on Photo Burst When... Copyright 2015 SmartThings
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
 *  Photo Burst When...
 *
 *  Author: SmartThings
 *
 *  Date: 2013-09-30
 */

definition(
    name: "Smart Security Camera",
    namespace: "blebson",
    author: "Ben Lebson",
    category: "Convenience",
    description: "Move to preset position and take a burst of photos and/or video clip and send a push notification",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when@2x.png"
)

preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
	}
	section("Choose camera to use") {
		input "camera", "capability.imageCapture", description: "NOTE: Currently only compatable with D-Link Devices made by BLebson"		
         input(name: "video", title: "Record Video", type: "bool", required: false, defaultValue: "false")
         input name: "duration", title: "Duration of Video Clip (for everything other than a motion event):", type: "string", defaultValue: 30 , required: true
         input name: "length", title: "Length of Video Clip after motion stops:", type: "string", defaultValue: 15 , required: true
         input(name: "picture", title: "Take Still Picture", type: "bool", required: false, defaultValue: "true")
	}
	section("Choose which preset camera position to move to"){
    input(name: "moveEnabled", title: "Can your camera pan/tilt?", type: "bool", required: false, defaultValue: "false")
    input name: "position", title: "Preset Position Number:", type: "number", defaultValue: 1 , required: true //string
	
    }
	section("Then send this message in a push notification"){
    	input "pushmessageoff", "bool", title: "disable push messages, but still take (simply toggle)", defaultValue: false
		input "messageText", "text", title: "Message Text"
	}
	section("And as text message to this number (optional)"){
        input("recipients", "contact", title: "Send notifications to", required: false) {
            input "phone", "phone", title: "Phone Number", required: false
        }
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", sendMessage)
	subscribe(acceleration, "acceleration.active", sendMessage)
	subscribe(motion, "motion.active", sendMessage)
    subscribe(motion, "motion.inactive", sendMessage)
	subscribe(mySwitch, "switch.on", sendMessage)
	subscribe(arrivalPresence, "presence.present", sendMessage)
	subscribe(departurePresence, "presence.not present", sendMessage)
}

def sendMessage(evt) {
	unschedule(videoOff)
	log.debug "$evt.name: $evt.value, $messageText" 
    	//motion active recored or take
    if((evt.name == "motion")&&(evt.value == "active")) {
        if(video == true) {
        	log.debug "Turning Video Recording On."
    		camera.vrOn()
           }
            
        if(moveEnabled == true){
        	log.debug "moving to $position."
    		camera.presetCommand(position)
   		}
   		 
    	if(picture == true) {
        	camera.take()
            log.debug "motion take"
            camera.burst()
		}
    	
    }
    	// motion off stop recording
    else if((evt.name == "motion")&&(evt.value == "inactive")) {
        if(video == true) {
        	log.debug "Turning Video Recording Off in ${length} seconds."
    		runIn(length.toInteger(), videoOff)
        }
    }
    	//contact or switch open
    else if(evt.name != "motion") { 
    	if(video == true) {
        	camera.vrOn()
        	runIn(duration.toInteger(), videoOff)
        }
        
        if(moveEnabled == true){
        log.trace "moving to $position."
    		camera.presetCommand(position)
    	}
   
    	if(picture == true) {
        	camera.take()
            log.debug "$evt , not motion"
            camera.burst()
		}
    		// always check to see if need to send message
	}
    //log.debug "motion req satifyed  ${(!(evt.name == "motion" && evt.value == "inactive"))}  push req satifyed ${pushmessageoff == null || pushmessageoff == false} - full requierment satis ${(!(evt.name == "motion" && evt.value == "inactive") && (pushmessageoff == null || pushmessageoff == false))}"
    if(!(evt.name == "motion" && evt.value == "inactive") && (pushmessageoff == null || pushmessageoff == false)) { //as long as its not an inactive motion check to send message
    	log.debug "push if true"
    	//if (pushmessageoff == null || pushmessageoff == false){ // disable push messages, but still take
    		sendNotification()
        //}
	}  	
}

def sendNotification(){
	log.debug "sendnote start"
	if (location.contactBookEnabled) {
        sendNotificationToContacts(messageText, recipients)
    }
    else {
        sendPush(messageText)
        if (phone) {
            sendSms(phone, messageText)
        }
    }    
}

def videoOff(){
	//log.debug "Turning Video Recording Off in ${duration} seconds."
	camera.vrOff()
}