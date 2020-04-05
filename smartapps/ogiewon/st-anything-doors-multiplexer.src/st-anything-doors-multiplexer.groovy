/**
 *  ST_Anything Doors Multiplexer - ST_Anything_Doors_Multiplexer.smartapp.groovy
 *
 *  Copyright 2015 Daniel Ogorchock
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
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2015-01-10  Dan Ogorchock  Original Creation
 *    2015-01-11  Dan Ogorchock  Reduced unnecessary chatter to the virtual devices
 *    2015-01-18  Dan Ogorchock  Added support for Virtual Temperature/Humidity Device
 *
 */
 
definition(
    name: "ST_Anything Doors Multiplexer",
    namespace: "ogiewon",
    author: "Daniel Ogorchock",
    description: "Connects single Arduino with multiple DoorControl and ContactSensor devices to their virtual device counterparts.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Select the Garage Doors (Virtual Door Control devices)") {
		input "leftdoor", title: "Left Garage Door", "capability.doorControl"
		input "rightdoor", title: "Right Garage Door", "capability.doorControl"
	}

	section("Select the House Doors (Virtual Contact Sensor devices)") {
		input "frontdoor", title: "Virtual Contact Sensor for Front Door", "capability.contactSensor"
		input "backdoor", title: "Virtual Contact Sensor for Back Door", "capability.contactSensor"
		input "kitchendoor", title: "Virtual Contact Sensor for Kitchen Door", "capability.contactSensor"
		input "garagesidedoor", title: "Virtual Contact Sensor for Garage Side Door", "capability.contactSensor"
	}

	section("Select the Virtual Temperature/Humidity devices") {
		input "temphumid_1", title: "1st Temp-Humidity Sensor", "capability.temperatureMeasurement", required: false
	}

	section("Select the Arduino ST_Anything_Doors device") {
		input "arduino", "capability.contactSensor"
    }    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe()
}

def subscribe() {

    subscribe(arduino, "leftDoor.open", leftDoorOpen)
    subscribe(arduino, "leftDoor.opening", leftDoorOpening)
    subscribe(arduino, "leftDoor.closed", leftDoorClosed)
    subscribe(arduino, "leftDoor.closing", leftDoorClosing)
    subscribe(leftdoor, "buttonPress.true", leftDoorPushButton)
    
    subscribe(arduino, "rightDoor.open", rightDoorOpen)
    subscribe(arduino, "rightDoor.opening", rightDoorOpening)
    subscribe(arduino, "rightDoor.closed", rightDoorClosed)
    subscribe(arduino, "rightDoor.closing", rightDoorClosing)
    subscribe(rightdoor, "buttonPress.true", rightDoorPushButton)
    
    subscribe(arduino, "frontDoor.open", frontDoorOpen)
    subscribe(arduino, "frontDoor.closed", frontDoorClosed)
    
    subscribe(arduino, "backDoor.open", backDoorOpen)
    subscribe(arduino, "backDoor.closed", backDoorClosed)

    subscribe(arduino, "kitchenDoor.open", kitchenDoorOpen)
    subscribe(arduino, "kitchenDoor.closed", kitchenDoorClosed)
    
    subscribe(arduino, "garagesideDoor.open", garagesideDoorOpen)
    subscribe(arduino, "garagesideDoor.closed", garagesideDoorClosed)

	subscribe(arduino, "temperature", temphumid_1_UpdateTemp)
   	subscribe(arduino, "humidity", temphumid_1_UpdateHumid)
}

// --- Left Garage Door --- 
def leftDoorOpen(evt)
{
    if (leftdoor.currentValue("contact") != "open") {
    	log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	leftdoor.open()
	}
}

def leftDoorOpening(evt)
{
    if (leftdoor.currentValue("contact") != "opening") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	leftdoor.opening()
	}    
}

def leftDoorClosing(evt)
{
    if (leftdoor.currentValue("contact") != "closing") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	leftdoor.closing()
	}
}

def leftDoorClosed(evt)
{
    if (leftdoor.currentValue("contact") != "closed") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	leftdoor.close()
	}
}    

def leftDoorPushButton(evt)
{
    log.debug "virtualGarageDoor($evt.name: $evt.value: $evt.deviceId)"
    arduino.pushLeft()
}

// --- Right Garage Door --- 
def rightDoorOpen(evt)
{
    if (rightdoor.currentValue("contact") != "open") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
		rightdoor.open()
	}
}

def rightDoorOpening(evt)
{
    if (rightdoor.currentValue("contact") != "opening") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	rightdoor.opening()
	}    
}

def rightDoorClosing(evt)
{
    if (rightdoor.currentValue("contact") != "closing") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	rightdoor.closing()
	}
}

def rightDoorClosed(evt)
{
    if (rightdoor.currentValue("contact") != "closed") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	rightdoor.close()
	}
}

def rightDoorPushButton(evt)
{
    log.debug "virtualGarageDoor($evt.name: $evt.value: $evt.deviceId)"
    arduino.pushRight()
}

// --- Front Door --- 
def frontDoorOpen(evt)
{
    if (frontdoor.currentValue("contact") != "open") {
    	log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	frontdoor.openme()
    }
}

def frontDoorClosed(evt)
{
    if (frontdoor.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	frontdoor.closeme()
    }
}

// --- back Door --- 
def backDoorOpen(evt)
{
    if (backdoor.currentValue("contact") != "open") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	backdoor.openme()
    }
}

def backDoorClosed(evt)
{
    if (backdoor.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	backdoor.closeme()
	}
}

// --- Kitchen Door --- 
def kitchenDoorOpen(evt)
{
    if (kitchendoor.currentValue("contact") != "open") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	kitchendoor.openme()
	}
}

def kitchenDoorClosed(evt)
{
    if (kitchendoor.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	kitchendoor.closeme()
	}
}


// --- Garage Side Door --- 
def garagesideDoorOpen(evt)
{
    if (garagesidedoor.currentValue("contact") != "open") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
 	   garagesidedoor.openme()
	}
}

def garagesideDoorClosed(evt)
{
    if (garagesidedoor.currentValue("contact") != "closed") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
	    garagesidedoor.closeme()
	}
}

// --- Temperature/Humidity ---
def temphumid_1_UpdateTemp(evt)
{
    log.debug "temperature: $evt.value, $evt"
    temphumid_1.updateTemperature(evt.value)
}

def temphumid_1_UpdateHumid(evt)
{
    log.debug "humidity: $evt.value, $evt"
    temphumid_1.updateHumidity(evt.value)
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}