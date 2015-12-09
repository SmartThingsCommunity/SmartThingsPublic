/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Brighter Button Controller
 *
 *	Author: SmartThings, trentfoley64
 *	Date: 2014-5-21, 2015-11-05
 */
definition(
	name: "New Brighter Button Controller",
	namespace: "trentfoley64",
	author: "A. Trent Foley",
	description: "Control devices with buttons like the Aeon Labs Minimote with dimming",
	parent: "trentfoley64:Brighter Button Controller",
	category: "My Apps",
	iconUrl: "http://www.trentfoley.com/ST/icons/brighter-button-controller.png",
	iconX2Url: "http://www.trentfoley.com/ST/icons/brighter-button-controller@2x.png",
	iconX3Url: "http://www.trentfoley.com/ST/icons/brighter-button-controller@3x.png"
)

preferences {
	page(name: "selectButton")
	page(name: "configureButton1")
	page(name: "configureButton2")
	page(name: "configureButton3")
	page(name: "configureButton4")

	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def selectButton() {
	dynamicPage(name: "selectButton", title: "First, select your button device", nextPage: "configureButton1", uninstall: configured()) {
		section {
			input "buttonDevice", "capability.button", title: "Button", multiple: false, required: true
		}
        
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {

			def timeLabel = timeIntervalLabel()

			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null

			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

			input "modes", "mode", title: "Only when mode is", multiple: true, required: false

			label title: "Assign a name", required: false
		}
	}
}

def configureButton1() {
	dynamicPage(name: "configureButton1", title: "Now let's decide how to use the first button",
		nextPage: "configureButton2", uninstall: configured(), getButtonSections(1))
}
def configureButton2() {
	dynamicPage(name: "configureButton2", title: "If you have a second button, set it up here",
		nextPage: "configureButton3", uninstall: configured(), getButtonSections(2))
}

def configureButton3() {
	dynamicPage(name: "configureButton3", title: "If you have a third button, you can do even more here",
		nextPage: "configureButton4", uninstall: configured(), getButtonSections(3))
}
def configureButton4() {
	dynamicPage(name: "configureButton4", title: "If you have a fourth button, you rule, and can set it up here",
		install: true, uninstall: true, getButtonSections(4))
}

def getButtonSections(buttonNumber) {
	return {
		section("Lights") {
			input "lights_${buttonNumber}_pushed", "capability.switch", title: "Pushed", multiple: true, required: false
			input "lights_${buttonNumber}_held", "capability.switch", title: "Held", multiple: true, required: false
		}
        section("Dimmers when Pushed") {
			input "dimmers_${buttonNumber}_pushed", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
			input "switchLevel_${buttonNumber}_pushed", "enum", title: "Brightness Level",
				options: ["7%", "10%", "20%", "25%", "30%", "40%", "50%", "60%", "75%", "100%"], required: false
        	input "dimmerMode_${buttonNumber}_pushed", "enum", title: "What to do if already turned on?", required: false,
            	options: ["Toggle", "Set Level"]
        }        
        section("Dimmers when Held") {
			input "dimmers_${buttonNumber}_held", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
			input "switchLevel_${buttonNumber}_held", "enum",
				options: ["7%", "10%", "20%", "25%", "30%", "40%", "50%", "60%", "75%", "100%"], title: "Brightness Level", required: false
        	input "dimmerMode_${buttonNumber}_held", "enum", title: "What to do if already turned on?", required: false,
            	options: ["Toggle", "Set Level"]
		}
		section("Locks") {
			input "locks_${buttonNumber}_pushed", "capability.lock", title: "Pushed", multiple: true, required: false
			input "locks_${buttonNumber}_held", "capability.lock", title: "Held", multiple: true, required: false
		}
		section("Sonos") {
			input "sonos_${buttonNumber}_pushed", "capability.musicPlayer", title: "Pushed", multiple: true, required: false
			input "sonos_${buttonNumber}_held", "capability.musicPlayer", title: "Held", multiple: true, required: false
		}
		section("Modes") {
			input "mode_${buttonNumber}_pushed", "mode", title: "Pushed", required: false
			input "mode_${buttonNumber}_held", "mode", title: "Held", required: false
		}
		def phrases = location.helloHome?.getPhrases()*.label
		if (phrases) {
			section("Hello Home Actions") {
				//log.trace phrases
				input "phrase_${buttonNumber}_pushed", "enum", title: "Pushed", required: false, options: phrases
				input "phrase_${buttonNumber}_held", "enum", title: "Held", required: false, options: phrases
			}
		}
		section("Sirens") {
			input "sirens_${buttonNumber}_pushed","capability.alarm" ,title: "Pushed", multiple: true, required: false
			input "sirens_${buttonNumber}_held", "capability.alarm", title: "Held", multiple: true, required: false
		}

		section("Custom Message") {
			input "textMessage_${buttonNumber}", "text", title: "Message", required: false
		}

		section("Push Notifications") {
			input "notifications_${buttonNumber}_pushed","bool" ,title: "Pushed", required: false, defaultValue: false
			input "notifications_${buttonNumber}_held", "bool", title: "Held", required: false, defaultValue: false
		}

		section("Sms Notifications") {
			input "phone_${buttonNumber}_pushed","phone" ,title: "Pushed", required: false
			input "phone_${buttonNumber}_held", "phone", title: "Held", required: false
		}
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(buttonDevice, "button", buttonEvent)
}

def configured() {
	return buttonDevice || buttonConfigured(1) || buttonConfigured(2) || buttonConfigured(3) || buttonConfigured(4)
}

def buttonConfigured(idx) {
	return settings["lights_$idx_pushed"] ||
    	settings["dimmers_$idx_pushed"] ||
		settings["locks_$idx_pushed"] ||
		settings["sonos_$idx_pushed"] ||
		settings["mode_$idx_pushed"] ||
		settings["notifications_$idx_pushed"] ||
		settings["sirens_$idx_pushed"] ||
		settings["notifications_$idx_pushed"] ||
		settings["phone_$idx_pushed"]
}

def buttonEvent(evt){
	if(allOk) {
		def buttonNumber = evt.data // why doesn't jsonData work? always returning [:]
		def value = evt.value
		log.debug "buttonEvent: $evt.name = $evt.value ($evt.data)"
		log.debug "button: $buttonNumber, value: $value"

		def recentEvents = buttonDevice.eventsSince(new Date(now() - 3000)).findAll{it.value == evt.value && it.data == evt.data}
		log.debug "Found ${recentEvents.size()?:0} events in past 3 seconds"

		if(recentEvents.size <= 1){
			switch(buttonNumber) {
				case ~/.*1.*/:
					executeHandlers(1, value)
					break
				case ~/.*2.*/:
					executeHandlers(2, value)
					break
				case ~/.*3.*/:
					executeHandlers(3, value)
					break
				case ~/.*4.*/:
					executeHandlers(4, value)
					break
			}
		} else {
			log.debug "Found recent button press events for $buttonNumber with value $value"
		}
	}
}

def executeHandlers(buttonNumber, value) {
	log.debug "executeHandlers: $buttonNumber - $value"

	def lights = find('lights', buttonNumber, value)
	def dimmers = find('dimmers', buttonNumber, value)
	def switchLevel = find('switchLevel', buttonNumber, value)
    def dimmerMode = find('dimmerMode', buttonNumber, value)

// Need to clean up this code.  It works but is convoluted from evolution.
// Basically, this code should combine dimmers and lights when figuring out
// what to do when toggling lights.  If any are on, turn them all off, otherwise
// turn them on and set levels

    def lightsAreOn = false
    if (dimmers == null) {
    	if (lights == null) {
        	lightsAreOn = false
        }
        else {
        	lightsAreOn = lights.currentValue("switch").contains("on")
        }
    }
    else {
    	if (lights == null) {
        	lightsAreOn = dimmers.currentValue("switch").contains("on")
        }
        else {
		    lightsAreOn = (lights+dimmers)*.currentValue("switch").contains("on")
        }
    }
    
	if (lights != null) toggle(lights,lightsAreOn)
	
	if ((dimmers != null) && (dimmerMode != null)) switchLevel ? toggle(dimmers,switchLevel,dimmerMode,lightsAreOn) : toggle(dimmers,lightsAreOn)

	def locks = find('locks', buttonNumber, value)
	if (locks != null) toggle(locks)

	def sonos = find('sonos', buttonNumber, value)
	if (sonos != null) toggle(sonos)

	def mode = find('mode', buttonNumber, value)
	if (mode != null) changeMode(mode)

	def phrase = find('phrase', buttonNumber, value)
	if (phrase != null) location.helloHome.execute(phrase)

	def textMessage = findMsg('textMessage', buttonNumber)

	def notifications = find('notifications', buttonNumber, value)
	if (notifications?.toBoolean()) sendPush(textMessage ?: "Button $buttonNumber was pressed" )

	def phone = find('phone', buttonNumber, value)
	if (phone != null) sendSms(phone, textMessage ?:"Button $buttonNumber was pressed")

	def sirens = find('sirens', buttonNumber, value)
	if (sirens != null) toggle(sirens)
}

def find(type, buttonNumber, value) {
	def preferenceName = type + "_" + buttonNumber + "_" + value
	def pref = settings[preferenceName]
	if(pref != null) {
		log.debug "Found: $pref for $preferenceName"
	}

	return pref
}

def findMsg(type, buttonNumber) {
	def preferenceName = type + "_" + buttonNumber
	def pref = settings[preferenceName]
	if(pref != null) {
		log.debug "Found: $pref for $preferenceName"
	}

	return pref
}

def toggle(devices,switchLevel,dimmerMode,lightsAreOn) {
	log.debug "toggle: $devices: switch=${devices*.currentValue('switch')}, switchLevel=${devices*.currentValue('switchLevel')}; parms: switchLevel=${switchLevel}, dimmerMode=${dimmerMode}, lightsAreOn=${lightsAreOn}"

// need to clean up this code.  Ugly Ugly Ugly... but it works

	if ((lightsAreOn) || !devices*.currentValue('switchLevel')) {
    	if (dimmerMode.equals('Toggle')) {
			devices.off()
        }
        else if (dimmerMode.equals('Set Level')) {
			switchLevel ? devices.setLevel( new Integer(switchLevel.substring(0,switchLevel.length()-1))) : devices.on()
        }
	}
	else if (!lightsAreOn) {
    	// if level is undefined, just turn it on.  otherwise, strip off the last char (%) and convert to integer
		switchLevel ? devices.setLevel( new Integer(switchLevel.substring(0,switchLevel.length()-1))) : devices.on()
	}
}

def toggle(devices,lightsAreOn) {
	log.debug "toggle: $devices = ${devices*.currentValue('switch')}, lightsAreOn: $lightsAreOn"

	if (lightsAreOn) {
		devices.off()
	}
	else {
		devices.on()
	}
}

def toggle(devices) {
	log.debug "toggle: $devices = ${devices*.currentValue('switch')}"

	if (devices*.currentValue('switch').contains('on')) {
		devices.off()
	}
	else if (devices*.currentValue('switch').contains('off')) {
		devices.on()
	}
	else if (devices*.currentValue('lock').contains('locked')) {
		devices.unlock()
	}
	else if (devices*.currentValue('alarm').contains('off')) {
		devices.siren()
	}
	else {
		devices.on()
	}
}

def changeMode(mode) {
	log.debug "changeMode: $mode, location.mode = $location.mode, location.modes = $location.modes"

	if (location.mode != mode && location.modes?.find { it.name == mode }) {
		setLocationMode(mode)
	}
}

// execution filter methods
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

private timeIntervalLabel() {
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}