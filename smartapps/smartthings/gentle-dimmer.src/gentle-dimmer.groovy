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
 *  Author: Benjamin Bean
 *  Date: 2016-09-24
 *  
 *  Changes to this code to make it colored light and temperature light capable, meaning
 *  you can select the color and temperature per light, specifically.
 */
definition(
	name: "Gentle Dimmer",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Dim your lights slowly, allowing you to wake up or go to bed more naturally.",
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

			section("Gentle Dimmer Has A Controller") {
				href(title: "Learn how to control Gentle Dimmer", page: "controllerExplanationPage", description: null)
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
	dynamicPage(name: "controllerExplanationPage", title: "How To Control Gentle Dimmer") {

		section("With other SmartApps", hideable: true, hidden: false) {
			paragraph "When this SmartApp is installed, it will create a controller device which you can use in other SmartApps for even more customizable automation!"
			paragraph "The controller acts like a switch so any SmartApp that can control a switch can control Gentle Dimmer, too!"
			paragraph "Routines and 'Smart Lighting' are great ways to automate Gentle Dimmer."
		}

		section("More about the controller", hideable: true, hidden: true) {
			paragraph "You can find the controller with your other 'Things'. It will look like this."
			image "http://f.cl.ly/items/2O0v0h41301U14042z3i/GentleWakeUpController-tile-stopped.png"
			paragraph "You can start and stop Gentle Dimmer by tapping the control on the right."
			image "http://f.cl.ly/items/3W323J3M1b3K0k0V3X3a/GentleWakeUpController-tile-running.png"
			paragraph "If you look at the device details screen, you will find even more information about Gentle Dimmer and more fine grain controls."
			image "http://f.cl.ly/items/291s3z2I2Q0r2q0x171H/GentleWakeUpController-richTile-stopped.png"
			paragraph "The slider allows you to jump to any point in the dimming process. Think of it as a percentage. If Gentle Dimmer is set to dim down as you fall asleep, but your book is just too good to put down; simply drag the slider to the left and Gentle Dimmer will give you more time to finish your chapter and drift off to sleep."
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
			paragraph "Unfortunately, some switches take a little time to turn off and may not finish turning off before Gentle Dimmer sets its dim level again. You may need to try a few times to get it to stop."
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
			input(name: "useCurrentLevel", type: "bool", title: "From Current Level", defaultValue: "false", required: false, submitOnChange: true)
			if (useCurrentLevel) {
				paragraph("If the light is off, then start from this level instead:")
			}
			input(name: "startLevel", type: "number", range: "0..99", title: "From this level", defaultValue: getStartLevelOrDefault(), description: "Between 0 and 99", required: true, multiple: false)
			input(name: "endLevel", type: "number", range: "0..99", title: "To this level", defaultValue: getEndLevelOrDefault(), description: "Between 0 and 99", required: true, multiple: false)
		}

		def dimmersArray = getDimmersArray()
		def colorDimmers = dimmersArray.colorDimmers
		def colorTemperatureDimmers = dimmersArray.colorTemperatureDimmersMinusColor

		if (colorDimmers) {
			section {
				input(name: "colorize", type: "bool", title: "Gradually change the color of ${fancyDeviceString(colorDimmers)}", description: null, required: false, defaultValue: "false", submitOnChange: true)
				
				if (colorize) {
					if (colorDimmers.size() > 1) {
						input(name: "individualColors", type: "bool", title: "Individual settings for each dimmer", description: null, required: false, defaultValue: "false", submitOnChange: true)
					}
					if (individualColors && colorDimmers.size() > 1) {
						colorDimmers.each { dimmer ->
							paragraph(deviceLabel(dimmer))
							addDeviceColorInput(dimmer.id)
						}
					} else {
						addDeviceColorInput()
					}
				}
			}
		}

		if (colorTemperatureDimmers) {
			section {
				input(name: "colorTemperatureize", type: "bool", title: "Gradually change the temperature of ${fancyDeviceString(colorTemperatureDimmers)}", description: null, required: false, defaultValue: "false", submitOnChange: true)
				
				if (colorTemperatureize) {
					if (colorTemperatureDimmers.size() > 1) {
						input(name: "individualColorTemperatures", type: "bool", title: "Individual settings for each dimmer", description: null, required: false, defaultValue: "false", submitOnChange: true)
					}
					if (individualColorTemperatures && colorTemperatureDimmers.size() > 1) {
						colorTemperatureDimmers.each { dimmer ->
							paragraph(deviceLabel(dimmer))
							addDeviceColorTemperatureInput(dimmer.id)
						}
					} else {
						addDeviceColorTemperatureInput()
					}
				}
			}
		}
	}
}

def addDeviceColorInput(id = null) {
	def options = [
		[("#" + tempToHex(2700)): "Soft White - Default"],
		[("#" + tempToHex(5000)): "White - Concentrate"],
		[("#" + tempToHex(6500)): "Daylight - Energize"],
		[("#" + tempToHex(3200)): "Warm White - Relax"],
		["#FF0000": "Red"],
		["#00FF00": "Green"],
		["#0000FF": "Blue"],
		["#FFFF00": "Yellow"],
		["#FFA500": "Orange"],
		["#800080": "Purple"],
		["#FFB5C5": "Pink"]
	]
	def idStr = id ?: "";
	input(name: "deviceColorUseCurrent" + idStr, type: "bool", title: "Start From Current Color", description: null, required: true, defaultValue: false, submitOnChange: true)
	def useCurrentColorToStart = settings.get("deviceColorUseCurrent" + idStr)
	if (!useCurrentColorToStart) {
		input(name: "deviceColorStart" + idStr, type: "enum", options: options, title: "Starting Color", description: null, required: true, defaultValue: "#FFA757")
	}
	input(name: "deviceColorEnd" + idStr, type: "enum", options: options, title: "Ending Color", description: null, required: true, defaultValue: "#FFA757")
}

def addDeviceColorTemperatureInput(id = null) {
	def idStr = id ?: "";
	input(name: "deviceColorTemperatureUseCurrent" + idStr, type: "bool", title: "Start From Current Temperature", description: null, required: true, defaultValue: false, submitOnChange: true)
	def useCurrentTempToStart = settings.get("deviceColorTemperatureUseCurrent" + idStr)
	if (!useCurrentTempToStart) {
		input(name: "deviceColorTemperatureStart" + idStr, type: "number", range: "2700..6500", title: "Starting Temperature", description: "Between 2700 and 6500", required: true, defaultValue: 6500)
	}
	input(name: "deviceColorTemperatureEnd" + idStr, type: "number", range: "2700..6500", title: "Ending Temperature", description: "Between 2700 and 6500", required: true, defaultValue: 3300)
}

def defaultStart() {
	return 0;
}

def defaultEnd() {
	return 99;
}

def getStartLevelOrDefault(deviceId = null, isIndividualLevelSettings = null) {
	// TODO make use of deviceId and isIndividualLevelSettings
	return startLevel ?: defaultStart()
}

def getEndLevelOrDefault(deviceId = null, isIndividualLevelSettings = null) {
	// TODO make use of deviceId and isIndividualLevelSettings
	return endLevel ?: defaultEnd()
}

def startLevelLabel() {
	def startLevelStr = "${getStartLevelOrDefault()}%"
	return isUseCurrentLevel() ? "Current Level or ${startLevelStr}" : startLevelStr
}

def isUseCurrentLevel() {
	return (useCurrentLevel != null && useCurrentLevel)
}

def endLevelLabel() {
	return "${getEndLevelOrDefault()}%"
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
			href(title: "Learn how to control Gentle Dimmer", page: "controllerExplanationPage", description: null)
		}

		section("Allow Automatic Dimming") {
			input(name: "days", type: "enum", title: "On These Days", description: "Every day", required: false, multiple: true, options: weekdays() + weekends())
			input(name: "requiredModes", type: "mode", title: "In These Modes", multiple: true, required: false, submitOnChange: true)
		}

		section("Start Dimming...") {
			input(name: "startTime", type: "time", title: "At This Time", description: null, required: false)
			if (requiredModes) {
				input(name: "modeStart", title: "When Entering This Mode", type: "enum", required: false, options: requiredModes, mutliple: false, submitOnChange: true, description: null)
			} else {
				input(name: "modeStart", title: "When Entering This Mode", type: "mode", required: false, mutliple: false, submitOnChange: true, description: null)
			}
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
	log.debug "Installing 'Gentle Dimmer' with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updating 'Gentle Dimmer' with settings: ${settings}"
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
	// Why make this optional? This is such a great feature!
	subscribe(app, appHandler)

	subscribe(location, locationHandler)

	if (manualOverride) {
		subscribe(dimmers, "switch.off", stopDimmersHandler)
	}

	removeDimmerSpecificSettings()

	initDimmerSpecificSettings()

	if (!getAllChildDevices()) {
		// create controller device and set name to the label used here
		def dni = "${new Date().getTime()}"
		log.debug "app.label: ${app.label}"
		addChildDevice("smartthings", "Gentle Wake Up Controller", dni, null, ["label": app.label])
		atomicState.controllerDni = dni
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

def start(source) {
	log.trace "START"

	sendStartEvent(source)

	setLevelsInState()

	atomicState.running = true

	atomicState.start = new Date().getTime()

	schedule("0 * * * * ?", "healthCheck")
	increment()
}

def stop(source) {
	log.trace "STOP"

	sendStopEvent(source)

	atomicState.running = false
	atomicState.start = 0

	unschedule("healthCheck")
}

def healthCheck() {
	log.trace "'Gentle Dimmer' healthCheck"

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

	sendControllerEvent(eventData)
	sendTimeRemainingEvent(0)
}

def sendTimeRemainingEvent(percentComplete) {
	log.trace "sendTimeRemainingEvent(${percentComplete})"

	def percentCompleteEventData = [
			name: "percentComplete",
			value: percentComplete as int,
			displayed: true,
			isStateChange: true
	]
	sendControllerEvent(percentCompleteEventData)

	def duration = sanitizeInt(duration, 30)
	def timeRemaining = duration - (duration * (percentComplete / 100))
	def timeRemainingEventData = [
			name: "timeRemaining",
			value: displayableTime(timeRemaining),
			displayed: true,
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

	def percentComplete = completionPercentage()

	updateDimmers(percentComplete)

	if (percentComplete < 99) {

		// if we need to take microsteps so things are smooth, then set up this app to do so
		def runAgain = stepDuration()
		def cronTime = 60.0 // at 60 second intervals the cron job will take care of this for us
		def accuracy = 0.25 // one quarter accuracy should still give a nice dimming effect
		def smallestAllowableCronStepDuration = cronTime / accuracy
		if (stepDuration < smallestAllowableCronStepDuration)
		{
			log.trace "Rescheduling to run again in ${runAgain} seconds"
			runIn(runAgain, 'increment', [overwrite: true])
		}

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
	def dimmersArray = getDimmersArray()
	def colorDimmers = dimmersArray.colorDimmers
	def colorTemperatureDimmers = dimmersArray.colorTemperatureDimmersMinusColor

	dimmers.each { dimmer ->

		def nextLevel = dynamicLevel(dimmer, percentComplete)

		if (nextLevel == 0) {

			dimmer.off()

		} else {

			def shouldChangeColors = (colorize && colorize != "false")
			def shouldChangeColorTemperatures = (colorTemperatureize && colorTemperatureize != "false")

			if (shouldChangeColors && hasSetColorCommand(dimmer)) {
				def colorMap = getColorMap(dimmer, colorDimmers.size(), percentComplete)
				log.trace "Setting ${deviceLabel(dimmer)} color to ${colorMap}"
				dimmer.setColor(colorMap)
			}
			if (hasSetLevelCommand(dimmer)) {
				def temperatureStr = ""
				if (shouldChangeColorTemperatures && hasSetColorTemperatureCommand(dimmer) && !hasSetColorCommand(dimmer)) {
					def temperature = getColorTemperature(dimmer, colorTemperatureDimmers.size(), percentComplete)
					temperatureStr = " and temperature to ${temperature}"
					dimmer.setColorTemperature(temperature)
				}
				log.trace "Setting ${deviceLabel(dimmer)} level to ${nextLevel}" + temperatureStr
				dimmer.setLevel(nextLevel)
			} else {
				log.warn "${deviceLabel(dimmer)} does not have setColor or setLevel commands."
			}

		}
	}

	sendTimeRemainingEvent(percentComplete)
}

int dynamicLevel(dimmer, percentComplete) {
	def start = atomicState.dimmerProps[dimmer.id]["startLevel"]
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

	sendTimeRemainingEvent(100)
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

/**
 * Removes from the atomic state dimmer settings for dimmers that are no longer selected.
 * <p>
 * Removes both the dimmerProps (which can only be set during initialization()) and
 * deviceIdStrings (which are hackish but must be used during the preferences page set up).
 */
def removeDimmerSpecificSettings() {
	def oldIds = atomicState.dimmerIds ?: []
	def dimmerProps = atomicState.dimmerProps ?: [:]
	def newIds = []
	def deviceIdStrings = [
		"deviceColorUseCurrent",
		"deviceColorStart",
		"deviceColorEnd",
		"deviceColorTemperatureUseCurrent",
		"deviceColorTemperatureStart",
		"deviceColorTemperatureEnd",
	]
	
	dimmers.each { dimmer ->
		newIds << dimmer.id
	}
	oldIds.removeAll(newIds)

	oldIds.each { id ->
		deviceIdStrings.each { prefix ->
			if (atomicState[prefix + id]) {
				atomicState.remove(prefix + id)
			}
		}
		dimmerProps.remove(id)
	}

	atomicState.dimmerIds = newIds
	atomicState.dimmerProps = dimmerProps
}

def initDimmerSpecificSettings() {
	def dimmerProps = atomicState.dimmerProps ?: [:]

	atomicState.dimmerIds.each { id ->
		if (!dimmerProps[id]) {
			dimmerProps[id] = [:]
		}
	}

	atomicState.dimmerProps = dimmerProps
}

def setLevelsInState() {
	def dimmerProps = atomicState.dimmerProps

	dimmers.each { dimmer ->
		if (!isUseCurrentLevel()) {
			dimmerProps[dimmer.id]["startLevel"] = getStartLevelOrDefault()
		} else {
			def dimmerIsOff = dimmer.currentValue("switch") == "off"
			dimmerProps[dimmer.id]["startLevel"] = dimmerIsOff ? getStartLevelOrDefault() : dimmer.currentValue("level")
		}

		if (hasSetColorCommand(dimmer)) {
			def hue = dimmer.currentValue("hue")
			def sat = dimmer.currentValue("saturation") ?: dimmer.currentValue("sat")
			def level = dimmer.currentValue("level")
			dimmerProps[dimmer.id]["startColor"] = [hue: hue, sat: sat, level: level]
		}
		if (hasSetColorTemperatureCommand(dimmer)) {
			dimmerProps[dimmer.id]["startColorTemperature"] = [temperature: dimmer.currentValue("colorTemperature")]
		}
	}

	atomicState.dimmerProps = dimmerProps
}

def canStartAutomatically() {

	def today = new Date().format("EEEE")
	def mode = location.mode;
	log.trace "today: ${today}, days: ${days}"

	if (days && !days.contains(today)) {// if no days, assume every day
		log.trace "should not run, wrong day"
		return false
	}

	if (requiredModes && !requiredModes.contains(mode))
	{
		log.trace "should not run, wrong mode"
		return false
	}

	return true
}

/**
 * @return The percent of dimmer completion. Should be between 0 and 100.
 */
def completionPercentage() {
	log.trace "checkingTime"

	if (!atomicState.running) {
		return
	}

	def now = new Date().getTime()
	def timeElapsed = now - atomicState.start
	def totalRunTime = totalRunTimeMillis() ?: 1
	def percentComplete = Math.min(timeElapsed / totalRunTime * 100 as double, 100 as double)
	log.trace "percentComplete: ${percentComplete}"

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
	return endLevel as int
}

private getColorTemperature(dimmer, colorTemperatureDimmerCount, percentComplete) {
	def startTemp, endTemp

	if (individualColorTemperatures && colorTemperatureDimmerCount > 1) {
		if (settings.get("deviceColorTemperatureUseCurrent" + dimmer.id)) {
			startTemp = atomicState.dimmerProps[dimmer.id]["startColorTemperature"]["temperature"]
		} else {
			startTemp = settings.get("deviceColorTemperatureStart" + dimmer.id)
		}
		endTemp = settings.get("deviceColorTemperatureEnd" + dimmer.id)
	} else {
		if (settings.get("deviceColorTemperatureUseCurrent")) {
			startTemp = atomicState.dimmerProps[dimmer.id]["startColorTemperature"]["temperature"]
		} else {
			startTemp = settings.get("deviceColorTemperatureStart")
		}
		endTemp = settings.get("deviceColorTemperatureEnd")
	}

	def currentTemp = startTemp + ((endTemp - startTemp) * percentComplete / 100.0)
	return currentTemp as int
}

def getCurrentColorPerAll(deviceId) {
	return getCurrentColorPer(deviceId, false)
}

def getCurrentColorPerDevice(deviceId) {
	return getCurrentColorPer(deviceId, true)
}

def getCurrentColorPer(deviceId, isUseDeviceSpecific) {
	def retval = [ startColor: "", doAdjustStartColor: true ]
	def dimmerProps = atomicState.dimmerProps[deviceId]
	def useCurrentStr = (isUseDeviceSpecific) ? "deviceColorUseCurrent" + deviceId : "deviceColorUseCurrent";
	def deviceColorStartStr = (isUseDeviceSpecific) ? "deviceColorStart" + deviceId : "deviceColorStart";

	if (settings.get(useCurrentStr)) {
		retval.startColor = anyToHsl( dimmerProps["startColor"] )
		retval.doAdjustStartColor = false
	} else {
		retval.startColor = hexToHsl( settings.get(deviceColorStartStr) )
	}

	return retval
}

def getColorMap(dimmer, colorDimmerCount, percentComplete) {
	def startColor, endColor, doAdjustStartColor, currentColorStats
	def doUseIndividualColors = individualColors && colorDimmerCount > 1

	// The fact that we are working with physical lightbulbs means we need a special handling of saturation
	doAdjustStartColor = true;

	if (doUseIndividualColors) {
		currentColorStats = getCurrentColorPerDevice(dimmer.id)
		startColor = currentColorStats.startColor
		doAdjustStartColor = currentColorStats.doAdjustStartColor
		endColor = hexToHsl( settings.get("deviceColorEnd" + dimmer.id) )
	} else {
		currentColorStats = getCurrentColorPerAll(dimmer.id)
		startColor = currentColorStats.startColor
		doAdjustStartColor = currentColorStats.doAdjustStartColor
		endColor = hexToHsl( settings.get("deviceColorEnd") )
	}

	// deals with the edge case of transitioning from purple-ish red to yellow-ish red
	if (Math.abs(endColor.h - startColor.h) > 50) {
		if (endColor.h > startColor.h) {
			endColor.h -= 100
		} else {
			endColor.h += 100
		}
	}

	// Here is where we actually adjust the saturation levels of our colors.
	if (doAdjustStartColor) {
		startColor.s = getAdjustedSaturationForLightbulbUse(startColor)
	}
	endColor.s = getAdjustedSaturationForLightbulbUse(endColor)

	def currentColor = [
		h: startColor.h + ((endColor.h - startColor.h) * percentComplete / 100.0),
		s: startColor.s + ((endColor.s - startColor.s) * percentComplete / 100.0),
		l: startColor.l + ((endColor.l - startColor.l) * percentComplete / 100.0)
	]

	// more code to hangle edge case of transitioning from purple-ish red to yellow-ish red
	currentColor.h %= 100

	currentColor["hue"] = currentColor["h"]
	currentColor["sat"] = currentColor["s"]
	currentColor["saturation"] = currentColor["s"]
	currentColor["lum"] = currentColor["l"]
	currentColor["luminance"] = currentColor["l"]
	currentColor["level"] = currentColor["l"]
	currentColor += hslToRgb( currentColor.h, currentColor.s, currentColor.l )

	return currentColor
}

/**
 * Returns a modified saturation value for the given color to deal with the fact that we are
 * dealing with light bulbs that don't respect luminance impacts on saturation levels.
 * <p>
 * This effectively reduces the saturation levels for higher and lower luminance levels.
 */
def getAdjustedSaturationForLightbulbUse(hslColorMap) {
	def l = hslColorMap.l
	def s = hslColorMap.s

	s = s - Math.abs(50 - l)

	return Math.max(0, Math.min(100, s))
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

private hasSetColorTemperatureCommand(device) {
	return hasCommand(device, "setColorTemperature")
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

private dimmersWithSetColorTemperatureCommand() {
	def colorTemperatureDimmers = []
	dimmers.each { dimmer ->
		if (hasSetColorTemperatureCommand(dimmer)) {
			colorTemperatureDimmers << dimmer
		}
	}
	return colorTemperatureDimmers
}

private getDimmersArray() {
	def colorDimmers = dimmersWithSetColorCommand()
	def colorTemperatureDimmers = dimmersWithSetColorTemperatureCommand()
	def colorTemperatureDimmersMinusColor = dimmersWithSetColorTemperatureCommand()
	colorTemperatureDimmersMinusColor.removeAll(colorDimmers)
	return [
		colorDimmers: colorDimmers,
		colorTemperatureDimmers: colorTemperatureDimmers,
		colorTemperatureDimmersMinusColor: colorTemperatureDimmersMinusColor
	]
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

/**
 * @return The ideal amount of time, in seconds, between each percentage of completion.
 */
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
	return device.label ?: device.displayName ?: device.name
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

	if (requiredModes) {
		if (!days) {
			descriptionParts << "In"
		} else {
			descriptionParts << "in"
		}
		if (requiredModes == location.modes) {
			descriptionParts << "any mode"
		} else {
			if (requiredModes.size() == 1) {
				descriptionParts << "mode ${requiredModes[0]},"
			} else {
				descriptionParts << "modes " + requiredModes.join(" or ") + ","
			}
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
	def colorDimmers = dimmersWithSetColorCommand()
	def colorTemperatureDimmers = dimmersWithSetColorTemperatureCommand()
	colorTemperatureDimmers.removeAll(colorDimmers)
	if (colorize && colorTemperatureize && colorDimmers && colorTemperatureDimmers) {
		def combinedDimmers = colorDimmers
		colorTemperatureDimmers.each { colorTemperatureDimmer ->
			if (!combinedDimmers.contains(colorTemperatureDimmer)) {
				combinedDimmers << colorTemperatureDimmer
			}
		}
		if (combinedDimmers == dimmers) {
			title += " and will gradually change color and color temperature."
		} else {
			title += ".\n${fancyDeviceString(combinedDimmers)} will gradually change color and color temperature."
		}
	} else if (colorize && colorDimmers) {
		if (colorDimmers == dimmers) {
			title += " and will gradually change color."
		} else {
			title += ".\n${fancyDeviceString(colorDimmers)} will gradually change color."
		}
	} else if (colorTemperatureize && colorTemperatureDimmers) {
		if (colorTemperatureDimmers == dimmers) {
			title += " and will gradually change color temperature."
		} else {
			title += ".\n${fancyDeviceString(colorTemperatureDimmers)} will gradually change color temperature."
		}
	} else {
		title += "."
	}
	return title
}

def hueSatToHex(h, s) {
	def convertedRGB = hslToRgb(h, s, 0.5)
	return rgbToHex(convertedRGB)
}

/**
 * Converts hue/saturation/level spectrum to red/green/blue spectrum.
 * 
 * @param h hue value, from 0-100
 * @param h hue value, from 0-100
 * @param h hue value, from 0-100
 * @return map of red, green, and blue values, each ranging from 0-255
 */
def hslToRgb(h, s, l) {
	def r, g, b;

	h /= 100
	s /= 100
	l /= 100

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

	r *= 255
	g *= 255
	b *= 255
	return [
		0: r,
		r: r,
		red: r,
		1: g,
		g: g,
		green: g,
		2: b,
		b: b,
		blue: b
	]
}

// from http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
/**
 * Converts red/green/blue spectrum to hue/saturation/level spectrum.
 * 
 * @param r red color, from 0-255
 * @param g green color, from 0-255
 * @param b blue color, from 0-255
 * @return map of hue, saturation, and level values, each ranging from 0-100
 */
def rgbToHsl(r, g, b){
	r /= 255.0
	g /= 255.0
	b /= 255.0
	def max = Math.max(r, Math.max(g, b))
	def min = Math.min(r, Math.min(g, b))
	def h, s, l = (max + min) / 2.0

	if(max == min){
		h = s = 0 // achromatic
	}else{
		def d = max - min
		s = d
		s /= l > 0.5 ? (2.0 - max - min) : (max + min)
		switch(max) {
		case r:
			h = (g - b) / d + (g < b ? 6 : 0)
			break
		case g:
			h = (b - r) / d + 2
			break
		case b:
			h = (r - g) / d + 4
			break
		}
		h /= 6
	}

	h *= 100.0
	s *= 100.0
	l *= 100.0

	return [
		h: h,
		hue: h,
		s: s,
		sat: s,
		saturation: s,
		l: l,
		lum: l,
		luminance: l,
		lev: l,
		level: l
	]
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

def hexToRgb(hex) {
	def toDec = {
		String h = it as String
		def hexOptions = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"]
		def retval = hexOptions.indexOf(h[0]) * 16
		retval += hexOptions.indexOf(h[1])
		return retval
	}

	def retval = [
		red: toDec(hex[1..2]),
		green: toDec(hex[3..4]),
		blue: toDec(hex[5..6]),
	]
	retval["r"] = retval["red"]
	retval["g"] = retval["green"]
	retval["b"] = retval["blue"]
	return retval
}

def hexToHsl(hex) {
	def rgb = hexToRgb(hex)
	return rgbToHsl(rgb.r, rgb.g, rgb.b)
}

def anyToHsl(colorMap) {
	def retval = []

	if (colorMap.hue || colorMap.h) {
		def h = colorMap.h ?: ( colorMap.hue ?: 0 )
		def s = colorMap.h ?: ( colorMap.sat ?: (colorMap.saturation ?: 100) )
		def l = colorMap.l ?: ( colorMap.lum ?: (colorMap.luminance ?: 60) )
		retval = [
			h: h,
			hue: h,
			s: s,
			sat: s,
			l: l,
			lum: l,
			luminance: l,
			level: l
		]
	} else if (colorMap.hex) {
		retval = hexToHsl( colorMap.hex )
	} else if (colorMap.temp || colorMap.temperature) {
		def temperature = colorMap.temp ?: colorMap.temperature
		retval = tempToHsl( temperature )
	} else {
		def red =   colorMap.red ?:   ( colorMap.r ?: ( colorMap[0] ?: 255 ) )
		def green = colorMap.green ?: ( colorMap.g ?: ( colorMap[1] ?: 255 ) )
		def blue =  colorMap.blue ?:  ( colorMap.b ?: ( colorMap[2] ?: 255 ) )
		retval = rgbToHsl(red, green, blue)
	}

	return retval
}

/**
 * Given a temperature (in Kelvin), estimate an RGB equivalent.
 * <p>
 * From http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
 */
def tempToRGB(tmpKelvin) {

	def r, g, b

	// Temperature must fall between 1000 and 40000 degrees
	tmpKelvin = Math.min(40000, Math.max(1000, tmpKelvin))
	
	// All calculations require tmpKelvin / 100, so only do the conversion once
	tmpKelvin /= 100
	
	//Calculate each color in turn
	
	//First: red
	if (tmpKelvin <= 66) {
		r = 255
	} else {
		//Note: the R-squared value for this approximation is .988
		r = tmpKelvin - 60
		r = 329.698727446 * Math.pow(r, -0.1332047592)
	}
	
	// Second: green
	if (tmpKelvin <= 66) {
		// Note: the R-squared value for this approximation is .996
		g = tmpKelvin
		g = 99.4708025861 * Math.log(g) - 161.1195681661
	} else {
		// Note: the R-squared value for this approximation is .987
		g = tmpKelvin - 60
		g = 288.1221695283 * (g ^ -0.0755148492)
	}
	
	// Third: blue
	if (tmpKelvin >= 66) {
		b = 255
	} else if (tmpKelvin <= 19) {
		b = 0
	} else {
		// Note: the R-squared value for this approximation is .998
		b = tmpKelvin - 10
		b = 138.5177312231 * Math.log(b) - 305.0447927307
	}

	r = Math.max(0, Math.min(255, r))
	g = Math.max(0, Math.min(255, g))
	b = Math.max(0, Math.min(255, b))

	return [
		0: r,
		r: r,
		red: r,
		1: g,
		g: g,
		green: g,
		2: b,
		b: b,
		blue: b
	]
}

def tempToHsl(tmpKelvin) {
	def rgb = tempToRGB(tmpKelvin)
	return rgbToHsl(rgb.r, rgb.g, rgb.b)
}

def tempToHex(tmpKelvin) {
	def rgb = tempToRGB(tmpKelvin)
	return rgbToHex(rgb.r, rgb.g, rgb.b)
}