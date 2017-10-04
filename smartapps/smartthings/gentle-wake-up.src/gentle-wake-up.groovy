/**
 *  Copyright 2016 SmartThings
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
 *  Gentle Wake Up
 *
 *  Author: Steve Vlaminck
 *  Date: 2013-03-11
 *
 * 	https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime.png
 * 	https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime%402x.png
 * 	Gentle Wake Up turns on your lights slowly, allowing you to wake up more
 * 	naturally. Once your lights have reached full brightness, optionally turn on
 * 	more things, or send yourself a text for a more gentle nudge into the waking
 * 	world (you may want to set your normal alarm as a backup plan).
 *
 */
definition(
	name: "Gentle Wake Up",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Dim your lights up slowly, allowing you to wake up more naturally.",
	category: "Health & Wellness",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/HealthAndWellness/App-SleepyTime@2x.png"
)

preferences {
	page(name: "rootPage")
	page(name: "schedulingPage")
	page(name: "completionPage")
	page(name: "numbersPage")
	page(name: "controllerExplanationPage")
	page(name: "unsupportedDevicesPage")
}

def rootPage() {
	dynamicPage(name: "rootPage", title: "", install: true, uninstall: true) {

		section("What to dim") {
			input(name: "dimmers", type: "capability.switchLevel", title: "Dimmers", description: null, multiple: true, required: true, submitOnChange: true)
			if (dimmers) {
				if (dimmersContainUnsupportedDevices()) {
					href(name: "toUnsupportedDevicesPage", page: "unsupportedDevicesPage", title: "Some of your selected dimmers don't seem to be supported", description: "Tap here to fix it", required: true)
				}
				href(name: "toNumbersPage", page: "numbersPage", title: "Duration & Direction", description: numbersPageHrefDescription(), state: "complete")
			}
		}

		if (dimmers) {

			section("Gentle Wake Up Has A Controller") {
				href(title: "Learn how to control Gentle Wake Up", page: "controllerExplanationPage", description: null)
			}

			section("Rules For Dimming") {
				href(name: "toSchedulingPage", page: "schedulingPage", title: "Automation", description: schedulingHrefDescription() ?: "Set rules for when to start", state: schedulingHrefDescription() ? "complete" : "")
				input(name: "manualOverride", type: "enum", options: ["cancel": "Cancel dimming", "jumpTo": "Jump to the end"], title: "When one of the dimmers is manually turned off…", description: "dimming will continue", required: false, multiple: false)
				href(name: "toCompletionPage", title: "Completion Actions", page: "completionPage", state: completionHrefDescription() ? "complete" : "", description: completionHrefDescription() ?: "Set rules for what to do when dimming completes")
			}

			section {
				// TODO: fancy label
				label(title: "Label This SmartApp", required: false, defaultValue: "", description: "Highly recommended", submitOnChange: true)
			}
		}
	}
}

def unsupportedDevicesPage() {

	def unsupportedDimmers = dimmers.findAll { !hasSetLevelCommand(it) }

	dynamicPage(name: "unsupportedDevicesPage") {
		if (unsupportedDimmers) {
			section("These devices do not support the setLevel command") {
				unsupportedDimmers.each {
					paragraph deviceLabel(it)
				}
			}
			section {
				input(name: "dimmers", type: "capability.sensor", title: "Please remove the above devices from this list.", submitOnChange: true, multiple: true)
			}
			section {
				paragraph "If you think there is a mistake here, please contact support."
			}
		} else {
			section {
				paragraph "You're all set. You can hit the back button, now. Thanks for cleaning up your settings :)"
			}
		}
	}
}

def controllerExplanationPage() {
	dynamicPage(name: "controllerExplanationPage", title: "How To Control Gentle Wake Up") {

		section("With other SmartApps", hideable: true, hidden: false) {
			paragraph "When this SmartApp is installed, it will create a controller device which you can use in other SmartApps for even more customizable automation!"
			paragraph "The controller acts like a switch so any SmartApp that can control a switch can control Gentle Wake Up, too!"
			paragraph "Routines and 'Smart Lighting' are great ways to automate Gentle Wake Up."
		}

		section("More about the controller", hideable: true, hidden: true) {
			paragraph "You can find the controller with your other 'Things'. It will look like this."
			image "http://f.cl.ly/items/2O0v0h41301U14042z3i/GentleWakeUpController-tile-stopped.png"
			paragraph "You can start and stop Gentle Wake up by tapping the control on the right."
			image "http://f.cl.ly/items/3W323J3M1b3K0k0V3X3a/GentleWakeUpController-tile-running.png"
			paragraph "If you look at the device details screen, you will find even more information about Gentle Wake Up and more fine grain controls."
			image "http://f.cl.ly/items/291s3z2I2Q0r2q0x171H/GentleWakeUpController-richTile-stopped.png"
			paragraph "The slider allows you to jump to any point in the dimming process. Think of it as a percentage. If Gentle Wake Up is set to dim down as you fall asleep, but your book is just too good to put down; simply drag the slider to the left and Gentle Wake Up will give you more time to finish your chapter and drift off to sleep."
			image "http://f.cl.ly/items/0F0N2G0S3v1q0L0R3J3Y/GentleWakeUpController-richTile-running.png"
			paragraph "In the lower left, you will see the amount of time remaining in the dimming cycle. It does not count down evenly. Instead, it will update whenever the slider is updated; typically every 6-18 seconds depending on the duration of your dimming cycle."
			paragraph "Of course, you may also tap the middle to start or stop the dimming cycle at any time."
		}

		section("Starting and stopping the SmartApp itself", hideable: true, hidden: true) {
			paragraph "Tap the 'play' button on the SmartApp to start or stop dimming."
			image "http://f.cl.ly/items/0R2u1Z2H30393z2I2V3S/GentleWakeUp-appTouch2.png"
		}

		section("Turning off devices while dimming", hideable: true, hidden: true) {
			paragraph "It's best to use other Devices and SmartApps for triggering the Controller device. However, that isn't always an option."
			paragraph "If you turn off a switch that is being dimmed, it will either continue to dim, stop dimming, or jump to the end of the dimming cycle depending on your settings."
			paragraph "Unfortunately, some switches take a little time to turn off and may not finish turning off before Gentle Wake Up sets its dim level again. You may need to try a few times to get it to stop."
			paragraph "That's why it's best to use devices that aren't currently dimming. Remember that you can use other SmartApps to toggle the controller. :)"
		}
	}
}

def numbersPage() {
	dynamicPage(name:"numbersPage", title:"") {

		section {
			paragraph(name: "pGraph", title: "These lights will dim", fancyDeviceString(dimmers))
		}

		section {
			input(name: "duration", type: "number", title: "For this many minutes", description: "30", required: false, defaultValue: 30)
		}

		section {
			input(name: "startLevel", type: "number", range: "0..99", title: "From this level", defaultValue: defaultStart(), description: "Current Level", required: false, multiple: false)
			input(name: "endLevel", type: "number", range: "0..99", title: "To this level", defaultValue: defaultEnd(), description: "Between 0 and 99", required: true, multiple: false)
		}

		def colorDimmers = dimmersWithSetColorCommand()
		if (colorDimmers) {
			section {
				input(name: "colorize", type: "bool", title: "Gradually change the color of ${fancyDeviceString(colorDimmers)}", description: null, required: false, defaultValue: "true")
			}
		}
	}
}

def defaultStart() {
	if (usesOldSettings() && direction && direction == "Down") {
		return 99
	}
	return 0
}

def defaultEnd() {
	if (usesOldSettings() && direction && direction == "Down") {
		return 0
	}
	return 99
}

def startLevelLabel() {
	if (usesOldSettings()) { // using old settings
		if (direction && direction == "Down") { // 99 -> 1
			return "99%"
		}
		return "0%"
	}
	return hasStartLevel() ? "${startLevel}%" : "Current Level"
}

def endLevelLabel() {
	if (usesOldSettings()) {
		if (direction && direction == "Down") { // 99 -> 1
			return "0%"
		}
		return "99%"
	}
	return "${endLevel}%"
}

def weekdays() {
	["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
}

def weekends() {
	["Saturday", "Sunday"]
}

def schedulingPage() {
	dynamicPage(name: "schedulingPage", title: "Rules For Automatically Dimming Your Lights") {

		section("Use Other SmartApps!") {
			href(title: "Learn how to control Gentle Wake Up", page: "controllerExplanationPage", description: null)
		}

		section("Allow Automatic Dimming") {
			input(name: "days", type: "enum", title: "On These Days", description: "Every day", required: false, multiple: true, options: weekdays() + weekends())
		}

		section("Start Dimming...") {
			input(name: "startTime", type: "time", title: "At This Time", description: null, required: false)
			input(name: "modeStart", title: "When Entering This Mode", type: "mode", required: false, mutliple: false, submitOnChange: true, description: null)
			if (modeStart) {
				input(name: "modeStop", title: "Stop when leaving '${modeStart}' mode", type: "bool", required: false)
			}
		}

	}
}

def completionPage() {
	dynamicPage(name: "completionPage", title: "Completion Rules") {

		section("Switches") {
			input(name: "completionSwitches", type: "capability.switch", title: "Set these switches", description: null, required: false, multiple: true, submitOnChange: true)
			if (completionSwitches) {
				input(name: "completionSwitchesState", type: "enum", title: "To", description: null, required: false, multiple: false, options: ["on", "off"], defaultValue: "on")
				input(name: "completionSwitchesLevel", type: "number", title: "Optionally, Set Dimmer Levels To", description: null, required: false, multiple: false, range: "(0..99)")
			}
		}

		section("Notifications") {
			input("recipients", "contact", title: "Send notifications to", required: false) {
				input(name: "completionPhoneNumber", type: "phone", title: "Text This Number", description: "Phone number", required: false)
				input(name: "completionPush", type: "bool", title: "Send A Push Notification", description: "Phone number", required: false)
			}
			input(name: "completionMusicPlayer", type: "capability.musicPlayer", title: "Speak Using This Music Player", required: false)
			input(name: "completionMessage", type: "text", title: "With This Message", description: null, required: false)
		}

		section("Modes and Phrases") {
			input(name: "completionMode", type: "mode", title: "Change ${location.name} Mode To", description: null, required: false)
			input(name: "completionPhrase", type: "enum", title: "Execute The Phrase", description: null, required: false, multiple: false, options: location.helloHome.getPhrases().label)
		}

		section("Delay") {
			input(name: "completionDelay", type: "number", title: "Delay This Many Minutes Before Executing These Actions", description: "0", required: false)
		}
	}
}

// ========================================================
// Handlers
// ========================================================

def installed() {
	log.debug "Installing 'Gentle Wake Up' with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updating 'Gentle Wake Up' with settings: ${settings}"
	unschedule()

	def controller = getController()
	if (controller) {
		controller.label = app.label
	}

	initialize()
}

private initialize() {
	stop("settingsChange")

	if (startTime) {
		log.debug "scheduling dimming routine to run at $startTime"
		schedule(startTime, "scheduledStart")
	}

	// TODO: make this an option
	subscribe(app, appHandler)

	subscribe(location, locationHandler)

	if (manualOverride) {
		subscribe(dimmers, "switch.off", stopDimmersHandler)
	}

	if (!getAllChildDevices()) {
		// create controller device and set name to the label used here
		def dni = "${new Date().getTime()}"
		log.debug "app.label: ${app.label}"
		addChildDevice("smartthings", "Gentle Wake Up Controller", dni, null, ["label": app.label])
		state.controllerDni = dni
	}
}

def appHandler(evt) {
	log.debug "appHandler evt: ${evt.value}"
	if (evt.value == "touch") {
		if (atomicState.running) {
			stop("appTouch")
		} else {
			start("appTouch")
		}
	}
}

def locationHandler(evt) {
	log.debug "locationHandler evt: ${evt.value}"

	if (!modeStart) {
		return
	}

	def isSpecifiedMode = (evt.value == modeStart)
	def modeStopIsTrue = (modeStop && modeStop != "false")

	if (isSpecifiedMode && canStartAutomatically()) {
		start("modeChange")
	} else if (!isSpecifiedMode && modeStopIsTrue) {
		stop("modeChange")
	}

}

def stopDimmersHandler(evt) {
	log.trace "stopDimmersHandler evt: ${evt.value}"
	def percentComplete = completionPercentage()
	// Often times, the first thing we do is turn lights on or off so make sure we don't stop as soon as we start
	if (percentComplete > 2 && percentComplete < 98) {
		if (manualOverride == "cancel") {
			log.debug "STOPPING in stopDimmersHandler"
			stop("manualOverride")
		} else if (manualOverride == "jumpTo") {
			def end = dynamicEndLevel()
			log.debug "Jumping to 99% complete in stopDimmersHandler"
			jumpTo(99)
		}

	} else {
		log.debug "not stopping in stopDimmersHandler"
	}
}

// ========================================================
// Scheduling
// ========================================================

def scheduledStart() {
	if (canStartAutomatically()) {
		start("schedule")
	}
}

public def start(source) {
	log.trace "START"

	sendStartEvent(source)

	setLevelsInState()

	atomicState.running = true
	atomicState.runCounter = 0

	atomicState.start = new Date().getTime()

	schedule("0 * * * * ?", "healthCheck")
	increment()
}

public def stop(source) {
	log.trace "STOP"

	sendStopEvent(source)

	atomicState.running = false
	atomicState.start = 0
	atomicState.runCounter = 0

	unschedule("healthCheck")
}

private healthCheck() {
	log.trace "'Gentle Wake Up' healthCheck"

	if (!atomicState.running) {
		return
	}

	increment()
}

// ========================================================
// Controller
// ========================================================

def sendStartEvent(source) {
	log.trace "sendStartEvent(${source})"
	def eventData = [
			name: "sessionStatus",
			value: "running",
			descriptionText: "${app.label} has started dimming",
			displayed: true,
			linkText: app.label,
			isStateChange: true
	]
	if (source == "modeChange") {
		eventData.descriptionText += " because of a mode change"
	} else if (source == "schedule") {
		eventData.descriptionText += " as scheduled"
	} else if (source == "appTouch") {
		eventData.descriptionText += " because you pressed play on the app"
	} else if (source == "controller") {
		eventData.descriptionText += " because you pressed play on the controller"
	}

	sendControllerEvent(eventData)
}

def sendStopEvent(source) {
	log.trace "sendStopEvent(${source})"
	def eventData = [
			name: "sessionStatus",
			value: "stopped",
			descriptionText: "${app.label} has stopped dimming",
			displayed: true,
			linkText: app.label,
			isStateChange: true
	]
	if (source == "modeChange") {
		eventData.descriptionText += " because of a mode change"
		eventData.value += "cancelled"
	} else if (source == "schedule") {
		eventData.descriptionText = "${app.label} has finished dimming"
	} else if (source == "appTouch") {
		eventData.descriptionText += " because you pressed play on the app"
		eventData.value += "cancelled"
	} else if (source == "controller") {
		eventData.descriptionText += " because you pressed stop on the controller"
		eventData.value += "cancelled"
	} else if (source == "settingsChange") {
		eventData.descriptionText += " because the settings have changed"
		eventData.value += "cancelled"
	} else if (source == "manualOverride") {
		eventData.descriptionText += " because the dimmer was manually turned off"
		eventData.value += "cancelled"
	}

	// send 100% completion event
	sendTimeRemainingEvent(100)

	// send a non-displayed 0% completion to reset tiles
	sendTimeRemainingEvent(0, false)

	// send sessionStatus event last so the event feed is ordered properly
	sendControllerEvent(eventData)
}

def sendTimeRemainingEvent(percentComplete, displayed = true) {
	log.trace "sendTimeRemainingEvent(${percentComplete})"

	def percentCompleteEventData = [
			name: "percentComplete",
			value: percentComplete as int,
			displayed: displayed,
			isStateChange: true
	]
	sendControllerEvent(percentCompleteEventData)

	def duration = sanitizeInt(duration, 30)
	def timeRemaining = duration - (duration * (percentComplete / 100))
	def timeRemainingEventData = [
			name: "timeRemaining",
			value: displayableTime(timeRemaining),
			displayed: displayed,
			isStateChange: true
	]
	sendControllerEvent(timeRemainingEventData)
}

def sendControllerEvent(eventData) {
	def controller = getController()
	if (controller) {
		controller.controllerEvent(eventData)
	}
}

def getController() {
	def dni = state.controllerDni
	if (!dni) {
		log.warn "no controller dni"
		return null
	}
	def controller = getChildDevice(dni)
	if (!controller) {
		log.warn "no controller"
		return null
	}
	log.debug "controller: ${controller}"
	return controller
}

// ========================================================
// Setting levels
// ========================================================


private increment() {

	if (!atomicState.running) {
		return
	}

	if (atomicState.runCounter == null) {
		atomicState.runCounter = 1
	} else {
		atomicState.runCounter = atomicState.runCounter + 1
	}
	def percentComplete = completionPercentage()

	if (percentComplete > 99) {
		percentComplete = 99
	}

	if (atomicState.runCounter > 100) {
		log.error "Force stopping Gentle Wakeup due to too many increments"
		// If increment has already been called 100 times, then stop regardless of state
		percentComplete = 100
	} else {
		updateDimmers(percentComplete)
	}
	if (percentComplete < 99) {

		def runAgain = stepDuration()
		log.debug "Rescheduling to run again in ${runAgain} seconds"

		runIn(runAgain, 'increment', [overwrite: true])

	} else {

		int completionDelay = completionDelaySeconds()
		if (completionDelay) {
			log.debug "Finished with steps. Scheduling completion for ${completionDelay} second(s) from now"
			runIn(completionDelay, 'completion', [overwrite: true])
			unschedule("healthCheck")
			// don't let the health check start incrementing again while we wait for the delayed execution of completion
		} else {
			log.debug "Finished with steps. Execution completion"
			completion()
		}

	}
}


def updateDimmers(percentComplete) {
	dimmers.each { dimmer ->

		def nextLevel = dynamicLevel(dimmer, percentComplete)

		if (nextLevel == 0) {

			dimmer.off()

		} else {

			def shouldChangeColors = (colorize && colorize != "false")

			if (shouldChangeColors && hasSetColorCommand(dimmer)) {
				def hue = getHue(dimmer, nextLevel)
				log.debug "Setting ${deviceLabel(dimmer)} level to ${nextLevel} and hue to ${hue}"
				dimmer.setColor([hue: hue, saturation: 100, level: nextLevel])
			} else if (hasSetLevelCommand(dimmer)) {
				log.debug "Setting ${deviceLabel(dimmer)} level to ${nextLevel}"
				dimmer.setLevel(nextLevel)
			} else {
				log.warn "${deviceLabel(dimmer)} does not have setColor or setLevel commands."
			}

		}
	}

	sendTimeRemainingEvent(percentComplete)
}

int dynamicLevel(dimmer, percentComplete) {
	def start = atomicState.startLevels[dimmer.id]
	def end = dynamicEndLevel()

	if (!percentComplete) {
		return start
	}

	def totalDiff = end - start
	def actualPercentage = percentComplete / 100
	def percentOfTotalDiff = totalDiff * actualPercentage

	(start + percentOfTotalDiff) as int
}

// ========================================================
// Completion
// ========================================================

private completion() {
	log.trace "Starting completion block"

	if (!atomicState.running) {
		return
	}

	stop("schedule")

	handleCompletionSwitches()

	handleCompletionMessaging()

	handleCompletionModesAndPhrases()
}

private handleCompletionSwitches() {
	completionSwitches.each { completionSwitch ->

		def isDimmer = hasSetLevelCommand(completionSwitch)

		if (completionSwitchesLevel && isDimmer) {
			completionSwitch.setLevel(completionSwitchesLevel)
		} else {
			def command = completionSwitchesState ?: "on"
			completionSwitch."${command}"()
		}
	}
}

private handleCompletionMessaging() {
	if (completionMessage) {
		if (location.contactBookEnabled) {
			sendNotificationToContacts(completionMessage, recipients)
		} else {
			if (completionPhoneNumber) {
				sendSms(completionPhoneNumber, completionMessage)
			}
			if (completionPush) {
				sendPush(completionMessage)
			}
		}
		if (completionMusicPlayer) {
			speak(completionMessage)
		}
	}
}

private handleCompletionModesAndPhrases() {

	if (completionMode) {
		setLocationMode(completionMode)
	}

	if (completionPhrase) {
		location.helloHome.execute(completionPhrase)
	}

}

def speak(message) {
	def sound = textToSpeech(message)
	def soundDuration = (sound.duration as Integer) + 2
	log.debug "Playing $sound.uri"
	completionMusicPlayer.playTrack(sound.uri)
	log.debug "Scheduled resume in $soundDuration sec"
	runIn(soundDuration, resumePlaying, [overwrite: true])
}

def resumePlaying() {
	log.trace "resumePlaying()"
	def sonos = completionMusicPlayer
	if (sonos) {
		def currentTrack = sonos.currentState("trackData").jsonValue
		if (currentTrack.status == "playing") {
			sonos.playTrack(currentTrack)
		} else {
			sonos.setTrack(currentTrack)
		}
	}
}

// ========================================================
// Helpers
// ========================================================

def setLevelsInState() {
	def startLevels = [:]
	dimmers.each { dimmer ->
		if (usesOldSettings()) {
			startLevels[dimmer.id] = defaultStart()
		} else if (hasStartLevel()) {
			startLevels[dimmer.id] = startLevel
		} else {
			def dimmerIsOff = dimmer.currentValue("switch") == "off"
			startLevels[dimmer.id] = dimmerIsOff ? 0 : dimmer.currentValue("level")
		}
	}

	atomicState.startLevels = startLevels
}

def canStartAutomatically() {

	def today = new Date().format("EEEE")
	log.debug "today: ${today}, days: ${days}"

	if (!days || days.contains(today)) {// if no days, assume every day
		return true
	}

	log.trace "should not run"
	return false
}

def completionPercentage() {
	log.trace "checkingTime"

	if (!atomicState.running) {
		return
	}

	def now = new Date().getTime()
	def timeElapsed = now - atomicState.start
	def totalRunTime = totalRunTimeMillis() ?: 1
	def percentComplete = timeElapsed / totalRunTime * 100
	log.debug "percentComplete: ${percentComplete}"

	return percentComplete
}

int totalRunTimeMillis() {
	int minutes = sanitizeInt(duration, 30)
	convertToMillis(minutes)
}

int convertToMillis(minutes) {
	def seconds = minutes * 60
	def millis = seconds * 1000
	return millis
}

def timeRemaining(percentComplete) {
	def normalizedPercentComplete = percentComplete / 100
	def duration = sanitizeInt(duration, 30)
	def timeElapsed = duration * normalizedPercentComplete
	def timeRemaining = duration - timeElapsed
	return timeRemaining
}

int millisToEnd(percentComplete) {
	convertToMillis(timeRemaining(percentComplete))
}

String displayableTime(timeRemaining) {
	def timeString = "${timeRemaining}"
	def parts = timeString.split(/\./)
	if (!parts.size()) {
		return "0:00"
	}
	def minutes = parts[0]
	if (parts.size() == 1) {
		return "${minutes}:00"
	}
	def fraction = "0.${parts[1]}" as double
	def seconds = "${60 * fraction as int}".padLeft(2, "0")
	return "${minutes}:${seconds}"
}

def jumpTo(percentComplete) {
	def millisToEnd = millisToEnd(percentComplete)
	def endTime = new Date().getTime() + millisToEnd
	def duration = sanitizeInt(duration, 30)
	def durationMillis = convertToMillis(duration)
	def shiftedStart = endTime - durationMillis
	atomicState.start = shiftedStart
	updateDimmers(percentComplete)
	sendTimeRemainingEvent(percentComplete)
}


int dynamicEndLevel() {
	if (usesOldSettings()) {
		if (direction && direction == "Down") {
			return 0
		}
		return 99
	}
	return endLevel as int
}

def getHue(dimmer, level) {
	def start = atomicState.startLevels[dimmer.id] as int
	def end = dynamicEndLevel()
	if (start > end) {
		return getDownHue(level)
	} else {
		return getUpHue(level)
	}
}

def getUpHue(level) {
	getBlueHue(level)
}

def getDownHue(level) {
	getRedHue(level)
}

private getBlueHue(level) {
	if (level < 5) return 72
	if (level < 10) return 71
	if (level < 15) return 70
	if (level < 20) return 69
	if (level < 25) return 68
	if (level < 30) return 67
	if (level < 35) return 66
	if (level < 40) return 65
	if (level < 45) return 64
	if (level < 50) return 63
	if (level < 55) return 62
	if (level < 60) return 61
	if (level < 65) return 60
	if (level < 70) return 59
	if (level < 75) return 58
	if (level < 80) return 57
	if (level < 85) return 56
	if (level < 90) return 55
	if (level < 95) return 54
	if (level >= 95) return 53
}

private getRedHue(level) {
	if (level < 6) return 1
	if (level < 12) return 2
	if (level < 18) return 3
	if (level < 24) return 4
	if (level < 30) return 5
	if (level < 36) return 6
	if (level < 42) return 7
	if (level < 48) return 8
	if (level < 54) return 9
	if (level < 60) return 10
	if (level < 66) return 11
	if (level < 72) return 12
	if (level < 78) return 13
	if (level < 84) return 14
	if (level < 90) return 15
	if (level < 96) return 16
	if (level >= 96) return 17
}

private dimmersContainUnsupportedDevices() {
	def found = dimmers.find { hasSetLevelCommand(it) == false }
	return found != null
}

private hasSetLevelCommand(device) {
	return hasCommand(device, "setLevel")
}

private hasSetColorCommand(device) {
	return hasCommand(device, "setColor")
}

private hasCommand(device, String command) {
	return (device.supportedCommands.find { it.name == command } != null)
}

private dimmersWithSetColorCommand() {
	def colorDimmers = []
	dimmers.each { dimmer ->
		if (hasSetColorCommand(dimmer)) {
			colorDimmers << dimmer
		}
	}
	return colorDimmers
}

private int sanitizeInt(i, int defaultValue = 0) {
	try {
		if (!i) {
			return defaultValue
		} else {
			return i as int
		}
	}
	catch (Exception e) {
		log.debug e
		return defaultValue
	}
}

private completionDelaySeconds() {
	int completionDelayMinutes = sanitizeInt(completionDelay)
	int completionDelaySeconds = (completionDelayMinutes * 60)
	return completionDelaySeconds ?: 0
}

private stepDuration() {
	int minutes = sanitizeInt(duration, 30)
	int stepDuration = (minutes * 60) / 100
	return stepDuration ?: 1
}

private debug(message) {
	log.debug "${message}\nstate: ${state}"
}

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public humanReadableStartDate() {
	new Date().parse(smartThingsDateFormat(), startTime).format("h:mm a", timeZone(startTime))
}

def fancyString(listOfStrings) {

	def fancify = { list ->
		return list.collect {
			def label = it
			if (list.size() > 1 && it == list[-1]) {
				label = "and ${label}"
			}
			label
		}.join(", ")
	}

	return fancify(listOfStrings)
}

def fancyDeviceString(devices = []) {
	fancyString(devices.collect { deviceLabel(it) })
}

def deviceLabel(device) {
	return device.label ?: device.name
}

def schedulingHrefDescription() {

	def descriptionParts = []
	if (days) {
		if (days == weekdays()) {
			descriptionParts << "On weekdays,"
		} else if (days == weekends()) {
			descriptionParts << "On weekends,"
		} else {
			descriptionParts << "On ${fancyString(days)},"
		}
	}

	descriptionParts << "${fancyDeviceString(dimmers)} will start dimming"

	if (startTime) {
		descriptionParts << "at ${humanReadableStartDate()}"
	}

	if (modeStart) {
		if (startTime) {
			descriptionParts << "or"
		}
		descriptionParts << "when ${location.name} enters '${modeStart}' mode"
	}

	if (descriptionParts.size() <= 1) {
		// dimmers will be in the list no matter what. No rules are set if only dimmers are in the list
		return null
	}

	return descriptionParts.join(" ")
}

def completionHrefDescription() {

	def descriptionParts = []
	def example = "Switch1 will be turned on. Switch2, Switch3, and Switch4 will be dimmed to 50%. The message '<message>' will be spoken, sent as a text, and sent as a push notification. The mode will be changed to '<mode>'. The phrase '<phrase>' will be executed"

	if (completionSwitches) {
		def switchesList = []
		def dimmersList = []


		completionSwitches.each {
			def isDimmer = completionSwitchesLevel ? hasSetLevelCommand(it) : false

			if (isDimmer) {
				dimmersList << deviceLabel(it)
			}

			if (!isDimmer) {
				switchesList << deviceLabel(it)
			}
		}


		if (switchesList) {
			descriptionParts << "${fancyString(switchesList)} will be turned ${completionSwitchesState ?: 'on'}."
		}

		if (dimmersList) {
			descriptionParts << "${fancyString(dimmersList)} will be dimmed to ${completionSwitchesLevel}%."
		}

	}

	if (completionMessage && (completionPhoneNumber || completionPush || completionMusicPlayer)) {
		def messageParts = []

		if (completionMusicPlayer) {
			messageParts << "spoken"
		}
		if (completionPhoneNumber) {
			messageParts << "sent as a text"
		}
		if (completionPush) {
			messageParts << "sent as a push notification"
		}

		descriptionParts << "The message '${completionMessage}' will be ${fancyString(messageParts)}."
	}

	if (completionMode) {
		descriptionParts << "The mode will be changed to '${completionMode}'."
	}

	if (completionPhrase) {
		descriptionParts << "The phrase '${completionPhrase}' will be executed."
	}

	return descriptionParts.join(" ")
}

def numbersPageHrefDescription() {
	def title = "All dimmers will dim for ${duration ?: '30'} minutes from ${startLevelLabel()} to ${endLevelLabel()}"
	if (colorize) {
		def colorDimmers = dimmersWithSetColorCommand()
		if (colorDimmers == dimmers) {
			title += " and will gradually change color."
		} else {
			title += ".\n${fancyDeviceString(colorDimmers)} will gradually change color."
		}
	}
	return title
}

def hueSatToHex(h, s) {
	def convertedRGB = hslToRgb(h, s, 0.5)
	return rgbToHex(convertedRGB)
}

def hslToRgb(h, s, l) {
	def r, g, b;

	if (s == 0) {
		r = g = b = l; // achromatic
	} else {
		def hue2rgb = { p, q, t ->
			if (t < 0) t += 1;
			if (t > 1) t -= 1;
			if (t < 1 / 6) return p + (q - p) * 6 * t;
			if (t < 1 / 2) return q;
			if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
			return p;
		}

		def q = l < 0.5 ? l * (1 + s) : l + s - l * s;
		def p = 2 * l - q;

		r = hue2rgb(p, q, h + 1 / 3);
		g = hue2rgb(p, q, h);
		b = hue2rgb(p, q, h - 1 / 3);
	}

	return [r * 255, g * 255, b * 255];
}

def rgbToHex(red, green, blue) {
	def toHex = {
		int n = it as int;
		n = Math.max(0, Math.min(n, 255));
		def hexOptions = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"]

		def firstDecimal = ((n - n % 16) / 16) as int
		def secondDecimal = (n % 16) as int

		return "${hexOptions[firstDecimal]}${hexOptions[secondDecimal]}"
	}

	def rgbToHex = { r, g, b ->
		return toHex(r) + toHex(g) + toHex(b)
	}

	return rgbToHex(red, green, blue)
}

def usesOldSettings() {
	!hasEndLevel()
}

def hasStartLevel() {
	return (startLevel != null && startLevel != "")
}

def hasEndLevel() {
	return (endLevel != null && endLevel != "")
}
