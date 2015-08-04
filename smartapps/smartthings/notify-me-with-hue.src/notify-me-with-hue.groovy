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
 *  Notify Me With Hue
 *
 *  Author: SmartThings
 *  Date: 2014-01-20
 */
definition(
    name: "Notify Me With Hue",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Changes the color and brightness of Philips Hue bulbs when any of a variety of SmartThings is activated.  Supports motion, contact, acceleration, moisture and presence sensors as well as switches.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png"
)

preferences {

	section("Control these bulbs...") {
		input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
	}

	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
		input "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
		input "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
		input "triggerModes", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true
		input "timeOfDay", "time", title: "At a Scheduled Time", required: false
	}

	section("Choose light effects...")
		{
			input "color", "enum", title: "Hue Color?", required: false, multiple:false, options: ["Red","Green","Blue","Yellow","Orange","Purple","Pink"]
			input "lightLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			input "duration", "number", title: "Duration Seconds?", required: false
			//input "turnOn", "enum", title: "Turn On when Off?", required: false, options: ["Yes","No"]
		}

	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(app, appTouchHandler)
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
	subscribe(button1, "button.pushed", eventHandler)

	if (triggerModes) {
		subscribe(location, modeChangeHandler)
	}

	if (timeOfDay) {
		schedule(timeOfDay, scheduledTimeHandler)
	}
}

def eventHandler(evt) {
	if (frequency) {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			takeAction(evt)
		}
	}
	else {
		takeAction(evt)
	}
}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
	if (evt.value in triggerModes) {
		eventHandler(evt)
	}
}

def scheduledTimeHandler() {
	eventHandler(null)
}

def appTouchHandler(evt) {
	takeAction(evt)
}

private takeAction(evt) {

	if (frequency) {
		state[evt.deviceId] = now()
	}

	def hueColor = 0
	if(color == "Blue")
		hueColor = 70//60
	else if(color == "Green")
		hueColor = 39//30
	else if(color == "Yellow")
		hueColor = 25//16
	else if(color == "Orange")
		hueColor = 10
	else if(color == "Purple")
		hueColor = 75
	else if(color == "Pink")
		hueColor = 83


	state.previous = [:]

	hues.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation"),
			"color": it.currentValue("color")			
		]
	}

	log.debug "current values = $state.previous"

	def newValue = [hue: hueColor, saturation: 100, level: (lightLevel as Integer) ?: 100]
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
	setTimer()
}

def setTimer()
{
	if(!duration) //default to 10 seconds
	{
		log.debug "pause 10"
		pause(10 * 1000)
		log.debug "reset hue"
		resetHue()
	}
	else if(duration < 10)
	{
		log.debug "pause $duration"
		pause(duration * 1000)
		log.debug "resetHue"
		resetHue()
	}
	else
	{
		log.debug "runIn $duration, resetHue"
		runIn(duration,"resetHue", [overwrite: false])
	}
}


def resetHue()
{
	hues.each {
		it.setColor(state.previous[it.id])        
	}
}