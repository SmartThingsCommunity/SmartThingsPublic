/**
 *  Medicine Management - Contact Sensor
 *
 *  Copyright 2016 Jim Mangione
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
 * Logic: 
 * --- Send notification at the medicine reminder time IF draw wasn't alread opened in past 60 minutes
 * --- If draw still isn't open 10 minutes AFTER reminder time, LED will turn RED.
 * --- ----- Once draw IS open, LED will return back to it's original color
 *
 */
import groovy.time.TimeCategory 

definition(
    name: "Medicine Management - Contact Sensor",
    namespace: "MangioneImagery",
    author: "Jim Mangione",
    description: "This supports devices with capabilities of ContactSensor and ColorControl (LED). It sends an in-app and ambient light notification if you forget to open the drawer or cabinet where meds are stored. A reminder will be set to a single time per day. If the draw or cabinet isn't opened within 60 minutes of that reminder, an in-app message will be sent. If the draw or cabinet still isn't opened after an additional 10 minutes, then an LED light turns red until the draw or cabinet is opened",
    category: "Health & Wellness",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {

	section("My Medicine Draw/Cabinet"){
		input "deviceContactSensor", "capability.contactSensor", title: "Opened Sensor" 
	} 

    section("Remind me to take my medicine at"){
        input "reminderTime", "time", title: "Time"
    }
    
    // NOTE: Use REAL device - virtual device causes compilation errors
    section("My LED Light"){
    	input "deviceLight", "capability.colorControl", title: "Smart light"
    }

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

    // will stop LED notification incase it was set by med reminder
    subscribe(deviceContactSensor, "contact", contactHandler)

    // how many minutes to look in the past from the reminder time, for an open draw
    state.minutesToCheckOpenDraw = 60
    
    // is true when LED notification is set after exceeding 10 minutes past reminder time
    state.ledNotificationTriggered = false
    
    // Set a timer to run once a day to notify if draw wasn't opened yet
    schedule(reminderTime, checkOpenDrawInPast)
   
}

// Should turn off any LED notification on OPEN state
def contactHandler(evt){
	if (evt.value == "open") {
        // if LED notification triggered, reset it.
        log.debug "Cabinet opened"
        if (state.ledNotificationTriggered) {
            resetLEDNotification()
        }
	}
}

// If the draw was NOT opened within 60 minutes of the timer send notification out.
def checkOpenDrawInPast(){
	log.debug "Checking past 60 minutes of activity from $reminderTime"
    
    // check activity of sensor for past 60 minutes for any OPENED status
    def cabinetOpened = isOpened(state.minutesToCheckOpenDraw)
	log.debug "Cabinet found opened: $cabinetOpened"
    
    // if it's opened, then do nothing and assume they took their meds
    if (!cabinetOpened) {    
    	sendNotification("Hi, please remember to take your meds in the cabinet")
       
       // if no open activity, send out notification and set new reminder    
        def reminderTimePlus10 = new Date(now() + (10 * 60000))

        // needs to be scheduled if draw wasn't already opened
        runOnce(reminderTimePlus10, checkOpenDrawAfterReminder)
    }
}

// If the draw was NOT opened after 10 minutes past reminder, use LED notification
def checkOpenDrawAfterReminder(){
	log.debug "Checking additional 10 minutes of activity from $reminderTime"
    
    // check activity of sensor for past 10 minutes for any OPENED status
    def cabinetOpened = isOpened(10)    
    
   	log.debug "Cabinet found opened: $cabinetOpened"
        
    // if no open activity, blink lights
    if (!cabinetOpened) {
    	log.debug "Set LED to Notification color"
        setLEDNotification()
    }
    
}

// Helper function for sending out an app notification
def sendNotification(msg){
        log.debug "Message Sent: $msg"
        sendPush(msg)
}

// Check if the sensor has been opened since the minutes entered
// Return true if opened found, else false.
def isOpened(minutes){
    // query last X minutes of activity log    
    def previousDateTime = new Date(now() - (minutes * 60000))
    
    // capture all events recorded
    def evts = deviceContactSensor.eventsSince(previousDateTime)   
    def cabinetOpened = false
    if (evts.size() > 0) {
        evts.each{
            if(it.value == "open") {
                cabinetOpened = true 
            }
        }
	}
    
    return cabinetOpened
}

// Saves current color and sets the light to RED
def setLEDNotification(){

	state.ledNotificationTriggered = true
    
	// turn light back off when reset is called if it was originally off
 	state.ledState = deviceLight.currentValue("switch")

	// set light to RED and store original color until stopped    
    state.origColor = deviceLight.currentValue("hue")
    deviceLight.on()
    deviceLight.setHue(100)
    
    log.debug "LED set to RED. Original color stored: $state.origColor"

}

// Sets the color back to the original saved color
def resetLEDNotification(){

	state.ledNotificationTriggered = false
    
    // return color to original
    log.debug "Reset LED color to: $state.origColor"
    if (state.origColor != null) {
    	deviceLight.setHue(state.origColor)
    }
    
    // if the light was turned on just for the notification, turn it back off now
    if (state.ledState == "off") {
    	deviceLight.off()
    }

}
