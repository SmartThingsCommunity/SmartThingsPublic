/**
 *  Copyright 2015 
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
 *  WatchDog
 *
 *  Author: Ule
 *  Date: 2015-11-03
 */
definition(
	name: "WatchDog",
	namespace: "mujica",
	author: "Ule",
	description: "Poll, Refresh and verify if timer works each time an event occurs.",
	category: "SmartThings Labs",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png"
)

preferences {
	page(name: "mainPage", title: "Poll, Refresh and verify if timer works each time an event occurs.", install: true, uninstall: true)
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		def anythingSet = anythingSet()

        section("Devices") {
            input "poll", "capability.polling", title:"Select devices to be polled", multiple:true, required:false
            input "refresh", "capability.refresh", title:"Select devices to be refreshed", multiple:true, required:false
            input "interval", "number", title:"Set minutes", defaultValue:5
        }
        
        if (anythingSet) {
			section("Verify Timer When"){
				ifSet "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
				ifSet "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
				ifSet "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
				ifSet "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
				ifSet "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
				ifSet "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
				ifSet "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
				ifSet "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
				ifSet "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
				ifSet "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
                ifSet "temperature", "capability.temperatureMeasurement", title: "Temperature", required: false, multiple: true
                ifSet "powerMeter", "capability.powerMeter", title: "Power Meter", required: false, multiple: true
                ifSet "energyMeter", "capability.energyMeter", title: "Energy", required: false, multiple: true
                ifSet "signalStrength", "capability.signalStrength", title: "Signal Strength", required: false, multiple: true
				ifSet "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
				ifSet "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
			}
		}
		def hideable = anythingSet || app.installationState == "COMPLETE"
		def sectionTitle = anythingSet ? "Select additional triggers" : "Verify Timer When..."

		section(sectionTitle, hideable: hideable, hidden: true){
			ifUnset "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
			ifUnset "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
			ifUnset "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
			ifUnset "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
			ifUnset "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
			ifUnset "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
			ifUnset "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
			ifUnset "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
			ifUnset "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
			ifUnset "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
			ifUnset "temperature", "capability.temperatureMeasurement", title: "Temperature", required: false, multiple: true
            ifUnset "signalStrength", "capability.signalStrength", title: "Signal Strength", required: false, multiple: true
            ifUnset "powerMeter", "capability.powerMeter", title: "Power Meter", required: false, multiple: true
            ifUnset "energyMeter", "capability.energyMeter", title: "Energy Meter", required: false, multiple: true
			ifUnset "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
			ifUnset "triggerModes", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true
		}
	}
}
private anythingSet() {
	for (name in ["motion","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","smoke","water", "temperature","signalStrength","powerMeter","energyMeter","button1","timeOfDay","triggerModes","timeOfDay"]) {
		if (settings[name]) {
			return true
		}
	}
	return false
}

private ifUnset(Map options, String name, String capability) {
	if (!settings[name]) {
		input(options, name, capability)
	}
}

private ifSet(Map options, String name, String capability) {
	if (settings[name]) {
		input(options, name, capability)
	}
}

def installed() {
	log.debug "Installed"
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    subscribeToEvents()
	unschedule()
    scheduleActions()
	scheduledActionsHandler()
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
    subscribe(temperature, "temperature", eventHandler)
    subscribe(powerMeter, "power", eventHandler)
	subscribe(energyMeter, "energy", eventHandler)
    subscribe(signalStrength, "lqi", eventHandler)
    subscribe(signalStrength, "rssi", eventHandler)
	subscribe(button1, "button.pushed", eventHandler)
	if (triggerModes) {
		subscribe(location, modeChangeHandler)
	}
}

def eventHandler(evt) {
    takeAction(evt)
}
def modeChangeHandler(evt) {
	if (evt.value in triggerModes) {
		eventHandler(evt)
	}
}

private scheduleActions() {
    def minutes = Math.max(settings.interval.toInteger(),1)
    def cron = "0 0/${minutes} * * * ?"
   	schedule(cron, scheduledActionsHandler)
}
def scheduledActionsHandler() {
    state.actionTime = new Date().time
    if (settings.poll) {
    	log.trace "poll"
        settings.poll*.poll()
    }
    if (settings.refresh) {
    	log.trace "refresh"
        settings.refresh*.refresh()
    }
}

def appTouchHandler(evt) {
	takeAction(evt)
}

private takeAction(evt) {
	def eventTime = new Date().time
	if (eventTime > ( 60000 + Math.max(settings.interval.toInteger(),5) * 1000 * 60 + state.actionTime?:0)) {
		log.trace "force update "
		updated()
	}
}