/**
 *  Medicine Management - Temp-Motion
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
 * --- If temp > threshold set, send notification
 * --- Send in-app notification at the medicine reminder time if no motion is detected in past 60 minutes
 * --- If motion still isn't detected 10 minutes AFTER reminder time, LED will turn RED
 * --- ----- Once motion is detected, LED will turn back to it's original color
 */
import groovy.time.TimeCategory 

definition(
    name: "Medicine Management - Temp-Motion",
    namespace: "MangioneImagery",
    author: "Jim Mangione",
    description: "This only supports devices with capabilities TemperatureMeasurement, AccelerationSensor and ColorControl (LED). Supports two use cases. First, will notifies via in-app if the fridge where meds are stored exceeds a temperature threshold set in degrees. Secondly, sends an in-app and ambient light notification if you forget to take your meds by sensing movement of the medicine box in the fridge. A reminder will be set to a single time per day. If the box isn't moved within 60 minutes of that reminder, an in-app message will be sent. If the box still isn't moved after an additional 10 minutes, then an LED light turns red until the box is moved",
    category: "Health & Wellness",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
 
     section("My Medicine in the Refrigerator"){
		input "deviceAccelerationSensor", "capability.accelerationSensor", required: true, multiple: false, title: "Movement"
        input "deviceTemperatureMeasurement", "capability.temperatureMeasurement", required: true, multiple: false, title: "Temperature"
	} 

	section("Temperature Threshold"){
    	input "tempThreshold", "number", title: "Temperature Threshold"
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
	// will notify when temp exceeds max
    subscribe(deviceTemperatureMeasurement, "temperature", tempHandler)

    // will stop LED notification incase it was set by med reminder
    subscribe(deviceAccelerationSensor, "acceleration.active", motionHandler)
    
    // how many minutes to look in the past from the reminder time
    state.minutesToCheckPriorToReminder = 60
    
    // Set a timer to run once a day to notify if draw wasn't opened yet
    schedule(reminderTime, checkMotionInPast)
}


// If temp > 39 then send an app notification out.
def tempHandler(evt){
	if (evt.doubleValue > tempThreshold) {
    	log.debug "Fridge temp of $evt.value exceeded threshold"
   		sendNotification("WARNING: Fridge temp is $evt.value with threshold of $tempThreshold")
	}
}

// Should turn off any LED notification once motion detected
def motionHandler(evt){
    // always call out to stop any possible LED notification
	log.debug "Medication moved. Send stop LED notification"
    resetLEDNotification()
}

// If no motion detected within 60 minutes of the timer send notification out.
def checkMotionInPast(){
	log.debug "Checking past 60 minutes of activity from $reminderTime"
    
    // check activity of sensor for past 60 minutes for any OPENED status
    def movement = isMoved(state.minutesToCheckPriorToReminder)
	log.debug "Motion found: $movement"
    
    // if there was movement, then do nothing and assume they took their meds
    if (!movement) {    
    	sendNotification("Hi, please remember to take your meds in the fridge")
    
        // if no movement, send out notification and set new reminder    
        def reminderTimePlus10 = new Date(now() + (10 * 60000))

        // needs to be scheduled if draw wasn't already opened
        runOnce(reminderTimePlus10, checkMotionAfterReminder)
    }
}

// If still no movement after 10 minutes past reminder, use LED notification
def checkMotionAfterReminder(){
	log.debug "Checking additional 10 minutes of activity from $reminderTime"
    
    // check activity of sensor for past 10 minutes for any OPENED status
    def movement = isMoved(10)    
    
   	log.debug "Motion found: $movement"
        
    // if no open activity, blink lights
    if (!movement) {
    	log.debug "Notify LED API"
        setLEDNotification()
    }
    
}

// Helper function for sending out an app notification
def sendNotification(msg){
        log.debug "Message Sent: $msg"
        sendPush(msg)
}

// Check if the accelerometer has been activated since the minutes entered
// Return true if active, else false.
def isMoved(minutes){
    // query last X minutes of activity log    
    def previousDateTime = new Date(now() - (minutes * 60000))
    
    // capture all events recorded
    def evts = deviceAccelerationSensor.eventsSince(previousDateTime)   
    def motion = false
    if (evts.size() > 0) {
        evts.each{
            if(it.value == "active") {
                motion = true 
            }
        }
	}
    
    return motion
}

// Saves current color and sets the light to RED
def setLEDNotification(){

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

    // return color to original
    log.debug "Reset LED color to: $state.origColor"
    deviceLight.setHue(state.origColor)
    
    // if the light was turned on just for the notification, turn it back off now
    if (state.ledState == "off") {
    	deviceLight.off()
    }
}