/*
 *  ST_Anything Doors & Windows Multiplexer - ST_Anything_Doors_Windows_Multiplexer.smartapp.groovy
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
 *    2015-10-31  Dan Ogorchock  Original Creation
 *    2016-02-12  Ken Washington Adopt for my configuration
 *    2016-09-05  Ken Washington Reconfigure for additional doors/windows and fix prior basement door hack
 *
 */
 
definition(
    name: "ST_Anything Doors Multiplexer",
    namespace: "kewashi",
    author: "Ken Washington",
    description: "Connects single Arduino with multiple ContactSensor devices to their virtual device counterparts.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {

	section("Select the House Doors (Virtual Contact Sensor devices)") {
		input "kitchendoor", title: "Virtual Contact Sensor for Kitchen Door", "capability.contactSensor"
		input "basementdoor", title: "Virtual Contact Sensor for Basement Door", "capability.contactSensor"
		input "bedroomwindow", title: "Virtual Contact Sensor for Bedroom Window", "capability.contactSensor"
		input "frontdoor", title: "Virtual Contact Sensor for Front Door", "capability.contactSensor"
		input "garagedoor", title: "Virtual Contact Sensor for Garage Door", "capability.contactSensor"
		input "livingdoor", title: "Virtual Contact Sensor for Living Room Door", "capability.contactSensor"
		input "bedroomdoor", title: "Virtual Contact Sensor for Bedroom Door", "capability.contactSensor"
		input "familyroomdoor", title: "Virtual Contact Sensor for FamilyRoom Door", "capability.contactSensor"
	}

	section("Select the Arduino ST_Anything device") {
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

   
    subscribe(arduino, "kitchenDoor.open", kitchenDoorOpen)
    subscribe(arduino, "kitchenDoor.closed", kitchenDoorClosed)

    subscribe(arduino, "basementDoor.open", basementDoorOpen)
    subscribe(arduino, "basementDoor.closed", basementDoorClosed)

    subscribe(arduino, "bedroomWindow.open", bedroomWindowOpen)
    subscribe(arduino, "bedroomWindow.closed", bedroomWindowClosed)

    subscribe(arduino, "frontDoor.open", frontDoorOpen)
    subscribe(arduino, "frontDoor.closed", frontDoorClosed)
    
    subscribe(arduino, "garageDoor.open", garageDoorOpen)
    subscribe(arduino, "garageDoor.closed", garageDoorClosed)

    subscribe(arduino, "livingDoor.open", livingDoorOpen)
    subscribe(arduino, "livingDoor.closed", livingDoorClosed)
    
    subscribe(arduino, "bedroomDoor.open", bedroomDoorOpen)
    subscribe(arduino, "bedroomDoor.closed", bedroomDoorClosed)
    
    subscribe(arduino, "froomDoor.open", froomDoorOpen)
    subscribe(arduino, "froomDoor.closed", froomDoorClosed)
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

// --- Basement Door --- 
def basementDoorOpen(evt)
{
    if (basementdoor.currentValue("contact") != "open") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	basementdoor.openme()
    }
}

def basementDoorClosed(evt)
{
    if (basementdoor.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	basementdoor.closeme()
    }
}

// --- Bedroom Window  --- 
def bedroomWindowOpen(evt)
{
    if (bedroomwindow.currentValue("contact") != "open") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	bedroomwindow.openme()
    }
}

def bedroomWindowClosed(evt)
{
    if (bedroomwindow.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	bedroomwindow.closeme()
    }
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

// --- Garage Door --- 
def garageDoorOpen(evt)
{
    if (garagedoor.currentValue("contact") != "open") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
 	   garagedoor.openme()
    }
}

def garageDoorClosed(evt)
{
    if (garagedoor.currentValue("contact") != "closed") {
	    log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
	    garagedoor.closeme()
    }
}

// --- living Room Door --- 
def livingDoorOpen(evt)
{
    if (livingdoor.currentValue("contact") != "open") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	livingdoor.openme()
    }
}

def livingDoorClosed(evt)
{
    if (livingdoor.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	livingdoor.closeme()
    }
}

// --- bedroom Door --- 
def bedroomDoorOpen(evt)
{
    if (bedroomdoor.currentValue("contact") != "open") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	bedroomdoor.openme()
    }
}

def bedroomDoorClosed(evt)
{
    if (bedroomdoor.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	bedroomdoor.closeme()
    }
}


// --- familyroom Door --- 
def froomDoorOpen(evt)
{
    if (familyroomdoor.currentValue("contact") != "open") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	familyroomdoor.openme()
    }
}

def froomDoorClosed(evt)
{
    if (familyroomdoor.currentValue("contact") != "closed") {
		log.debug "arduinoevent($evt.name: $evt.value: $evt.deviceId)"
    	familyroomdoor.closeme()
    }
}



def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}