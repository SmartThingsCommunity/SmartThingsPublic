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
 *	Button Controller
 *
 *	Author: SmartThings
 *	Date: 2014-5-21
 */
definition(
    name: "Dimming Button Controller",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Control devices with buttons like the Aeon Labs Minimote",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MyApps/Cat-MyApps@2x.png",
    pausable: true
)

preferences {
	page(name: "selectButton")
	for (def i=1; i<=8; i++) {
		page(name: "configureButton$i")
	}

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
		}

		section([title: " ", mobileOnly:true]) {
			label title: "Assign a name", required: false
		}
	}
}

def createPage(pageNum) {
	if ((state.numButton == pageNum) || (pageNum == 8))
		state.installCondition = true
	dynamicPage(name: "configureButton$pageNum", title: "Set up button $pageNum here",
			nextPage: "configureButton${pageNum+1}", install: state.installCondition, uninstall: configured(), getButtonSections(pageNum))
}

def configureButton1() {
	state.numButton = buttonDevice.currentState("numberOfButtons")?.longValue ?: 4
	log.debug "state variable numButton: ${state.numButton}"
	state.installCondition = false
	createPage(1)
}
def configureButton2() {
	createPage(2)
}

def configureButton3() {
	createPage(3)
}

def configureButton4() {
	createPage(4)
}

def configureButton5() {
	createPage(5)
}

def configureButton6() {
	createPage(6)
}

def configureButton7() {
	createPage(7)
}

def configureButton8() {
	createPage(8)
}

def getButtonSections(buttonNumber) {
	return {
		section("Lights") {
			input "lights_${buttonNumber}_pushed", "capability.switch", title: "Pushed", multiple: true, required: false
			input "lights_${buttonNumber}_held", "capability.switch", title: "Held", multiple: true, required: false
		}
		section("Dimmers") {
			input "dimmer_${buttonNumber}_pushed", "capability.switchLevel", title: "Pushed", multiple: true, required: false
			input "dimmerAbsolute_${buttonNumber}_pushed", "bool", title: "Absolute?", multiple: false, description: "On sets to/from level, Off adjusts brightness."
			input "dimmerVal_${buttonNumber}_pushed", "number", title: "Step", multiple: false, required: false, description: "Percent to set/brighten, 0 == toggle."
			input "dimmer_${buttonNumber}_held", "capability.switchLevel", title: "Held", multiple: true, required: false
			input "dimmerAbsolute_${buttonNumber}_held", "bool", title: "Absolute? (vs amount to brighten)", multiple: false, description: "On sets to/from level, Off adjusts brightness."
			input "dimmerVal_${buttonNumber}_held", "number", title: "Step", multiple: false, required: false, description: "Percent to set/brighten, 0 == toggle."
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
				log.trace phrases
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
		settings["dimmer_$idx_pushed"] ||
		settings["locks_$idx_pushed"] ||
		settings["sonos_$idx_pushed"] ||
		settings["mode_$idx_pushed"] ||
        settings["notifications_$idx_pushed"] ||
        settings["sirens_$idx_pushed"] ||
        settings["notifications_$idx_pushed"]   ||
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
	if (lights != null) toggle(lights)


	def dimmer = find('dimmer', buttonNumber, value)
	def dimmerAbsolute = find('dimmerAbsolute', buttonNumber, value)
	def dimmerVal = find('dimmerVal', buttonNumber, value)
	if (dimmerVal) {
		if (dimmerAbsolute) 	dimAbsolute(dimmer, dimmerVal);
		else 					dimRelative(dimmer, dimmerVal)	
	}
	
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

/** Sets the devices to the defined level.
 *	@param devices An array of devices
 *  @param dimLevel Percent to set to, or 0 for off, or 1 for "on".
 *  The assumption is that seldom will a device be told to dim to 1%
 */
def setDimmersTo(devices, dimLevel) {
	devices.eachWithIndex { device, index ->
		if (dimLevel == 0)  		device.off();
		else if (dimLevel == 1)  	device.on();
		else 						device.setLevel(dimLevel);
	}	
}

/** 
 * Detect and adjust current device level.  Using first device as a proxy for all.
 * @param dimLevel Level to adjust dim by, going up.  0 means toggle.  Wraps around.
 */
def dimRelative(devices, dimLevel) {
	log.debug "dimRelative: $devices = ${devices*.currentValue('switch')}"
	def currentLevel = devices[0].currentLevel
	if (devices[0].currentSwitch == 'off') 	currentLevel = 0;
    log.debug "Current Level: $currentLevel.  dimLevel: $dimLevel.";
    def newDimLevelNum = dimLevel + currentLevel;
	def newDimLevelPercent = Math.min(newDimLevelNum.toInteger()
    , 100)

	/* Toggle Case */
	if (dimLevel == 0){
		if (currentLevel == 0) 	setDimmersTo(devices, 1);
		else 					setDimmersTo(devices, 0);
	} 
	else if (currentLevel > 98)	setDimmersTo(devices, 0); /* Wrap around case */
	else 						setDimmersTo(devices, newDimLevelPercent); /* Do the math case */
}

/** 
 * Sets light to a specific level.
 * dimLevel is percentage.
 */
def dimAbsolute(devices, dimLevel) {
	log.debug "dimAbsolute: $devices = ${devices*.currentValue('switch')}"
	def currentLevel = devices[0].currentLevel
	if (devices[0].currentSwitch == 'off')	currentLevel = 0
	if (currentLevel == 0)	setDimmersTo(devices, dimLevel);
	else 					setDimmersTo(devices, 0); /* Do the math case */
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
	else if (devices*.currentValue('lock').contains('unlocked')) {
		devices.lock()
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
		def start = timeToday(starting, location.timeZone).time
		def stop = timeToday(ending, location.timeZone).time
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
