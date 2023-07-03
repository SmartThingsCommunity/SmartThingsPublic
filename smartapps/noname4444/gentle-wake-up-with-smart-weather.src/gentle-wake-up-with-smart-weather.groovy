/**
 *  Gentle Wake Up With Smart Weather v1.1.1
 *
 *  Copyright 2015 Jim Worley
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
 * Description:  This app will slowly turn on lights at a designated time to act as an alarm with colors based on the forecast.  
 *   It can use two lights (or two groups of lights) and will set the color of the first group based on the forecasted high for that day, 
 *     and the second group will be colored based on the weather forecast.  If only one light (or light group) is selected then it will
 *     show the temperature for that day but not the forecast.
 *    
 *   Temperature Colors:  Blue to green to yellow to orange to red (cold to hot)
 *    Forecast colors: Sunny - Yellow; Cloudy - Pink; Rain - Purple; Thunder - orange; Snow - Blue
 *
 *   Credits to the Gental Wake Up app which was the base for this app.
 *
 * Author: Jim Worley
 *
 * Date: 2015-08-03
 */
 
definition(
    name: "Gentle Wake Up With Smart Weather",
    namespace: "NoName4444",
    author: "Jim Worley",
    description: "This app will slowly turn on lights at a designated time to act as an alarm with colors based on the forcast.",
    category: "Health & Wellness",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/smart-light-timer.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/smart-light-timer@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Developers/smart-light-timer@2x.png") 


preferences {
	page(name: "rootPage")
	page(name: "schedulingPage")
	page(name: "completionPage")
	page(name: "numbersPage")
}

/*
* This is the main page the user will see.  First, they select a bulb or group of bulbs.
* Next there are multiple options they can choose.  Ideally they will select a second bulb (or second group of bulbs)
*/
def rootPage() {
	dynamicPage(name: "rootPage", title: "", install: true, uninstall: true) {

		section {
			input(name: "tempBulbs", type: "capability.colorControl", title: "Which color bulb(s) show Temperature", description: null, multiple: true, required: true, submitOnChange: true)
		}

		if (tempBulbs) {
			section {
				input(name: "foreBulbs", type: "capability.colorControl", title: "Which color bulb(s) show the forecast (optional)", description: null, multiple: true, required: false, submitOnChange: true)
			}

			section {
				href(name: "toNumbersPage", page: "numbersPage", title: "Duration & Direction", description: numbersPageHrefDescription(), state: "complete")
			}

            //Error checking!
			if (isErrors()) {
              section(hideable: false) {
                	if (dynamicStartLevel() >= dynamicEndLevel()) {
                        paragraph(name: "error1", title: "ERROR: Dimming Levels", "The end level MUST be larger than the start level.  Please update your values.")
                    }
                    if (bulbIntersects()) {
                        paragraph(name: "error2", title: "ERROR: Bulbs in Multiple Groups", "The following bulbs are in both the temperature bulb list and the forecast bulb list: ${fancyDeviceString(bulbIntersects())}")
                    }
				    input(name: "dummy", type: "capability.nope", title: null, description: null, required: true)
			  }
            }

			section {
				href(name: "toSchedulingPage", page: "schedulingPage", title: "Rules For Automatically Dimming Your Lights", description: schedulingHrefDescription(), state: schedulingHrefDescription() ? "complete" : "")
			}

			section {
				href(name: "toCompletionPage", title: "Completion Actions (Optional)", page: "completionPage", state: completionHrefDescription() ? "complete" : "", description: completionHrefDescription())
			}

			section {
				// TODO: fancy label
				label(title: "Label this SmartApp", required: false, defaultValue: "")
			}
			if (!isErrors()) {
            	section(hideable: true, hidden: true, title: "Error Report") {
            		input(name: "dummy", type: "capability.nope", title: null, description: "No errors were found and you can install this app.", required: false, state: "complete")
            	}
            }
		}
	}
}

def isErrors() {
	return (dynamicStartLevel() >= dynamicEndLevel() || bulbIntersects())
}

/*
* Allows the user to set the time it takes for the bubls to brighten, as well as the start to end levels 
*/
def numbersPage() {
	dynamicPage(name:"numbersPage", title:"") {

		section {
			paragraph(name: "pGraph", title: "These lights will show the temperature", fancyDeviceString(tempBulbs))
			if (foreBulbs) {
			  paragraph(name: "pGraph2", title: "and these lights will show the forecast", fancyDeviceString(foreBulbs))
		    }
		}

		section {
			input(name: "duration", type: "number", title: "For this many minutes", description: "30", required: false, defaultValue: 30)
		}

		section {
			input(name: "startLevel", type: "number", range: "1..99", title: "From this level", defaultValue: 1, description: "Between 1 and 99", required: true, multiple: false)
			input(name: "endLevel", type: "number", range: "2..99", title: "To this level", defaultValue: 99, description: "Between 2 and 99, greater than the From", required: true, multiple: false)
		}
		
	}
}

/* 
* Allows the user to choose when to have the ligths come on
*/
def schedulingPage() {
	dynamicPage(name: "schedulingPage", title: "Rules For Automatically Dimming Your Lights") {

		section {
			input(name: "days", type: "enum", title: "Allow Automatic Dimming On These Days", description: "Every day", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
		}

		section {
			input(name: "modeStart", title: "Start when entering this mode", type: "mode", required: false, mutliple: false, submitOnChange: true)
			if (modeStart) {
				input(name: "modeStop", title: "Stop when leaving '${modeStart}' mode", type: "bool", required: false)
			}
		}

		section {
			input(name: "startTime", type: "time", title: "Start Dimming At This Time", description: null, required: false)
		}
		section ("Zip code (optional, defaults to location coordinates)...") {
			input "zipcode", "text", title: "Zip Code", required: false
		}

	}
}

/*
* Allows the user to set various things to happen when the lights are done dimming
*/
def completionPage() {
	dynamicPage(name: "completionPage", title: "Completion Rules") {

		section("Switches") {
			input(name: "completionSwitches", type: "capability.switch", title: "Set these switches", description: null, required: false, multiple: true, submitOnChange: true)
			if (completionSwitches || androidClient()) {
				input(name: "completionSwitchesState", type: "enum", title: "To", description: null, required: false, multiple: false, options: ["on", "off"], style: "segmented", defaultValue: "on")
				input(name: "completionSwitchesLevel", type: "number", title: "Optionally, Set Dimmer Levels To", description: null, required: false, multiple: false, range: "(0..99)")
			}
		}

		section("Notifications") {
			input("recipients", "contact", title: "Send notifications to") {
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
    unsubscribe()

	initialize()
}

private initialize() {
	stop()

	if (startTime) {
		log.debug "scheduling dimming routine to run at $startTime"
		schedule(startTime, "scheduledStart")
	}

	// TODO: make this an option
	subscribe(app, appHandler)

	subscribe(location, locationHandler)
}

def appHandler(evt) {
	log.debug "appHandler evt: ${evt.value}"
	if (evt.value == "touch") {
		if (atomicState.running) {
			stop()
		} else {
			start()
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
		start()
	} else if (!isSpecifiedMode && modeStopIsTrue) {
		stop()
	}

}

// ========================================================
// Scheduling
// ========================================================

def scheduledStart() {
	if (canStartAutomatically()) {
		start()
	}
}

def start() {
	log.trace "START"

	if (dynamicEndLevel() <= dynamicStartLevel()) {
		log.error "The end level is smaller than the start level.  Booo."
	}

	setWeatherColors()

	atomicState.running = true

	atomicState.start = new Date().getTime()

	schedule("0 * * * * ?", "healthCheck")
	increment()
}

def stop() {
	log.trace "STOP"

	atomicState.running = false
	atomicState.start = 0

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
// Setting levels
// ========================================================


private increment() {

	if (!atomicState.running) {
		return
	}

	def percentComplete = completionPercentage()

	if (percentComplete > 99) {
		percentComplete = 99
	}

	updateDimmers(percentComplete)

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
	//Set the color and brightness of the temperature bulb
	tempBulbs.each { dimmer ->
		def nextLevel = dynamicLevel(dimmer, percentComplete)
		dimmer.setColor([hue: atomicState.tempHue, saturation: 100, level: nextLevel])
	}

	if (foreBulbs){
		foreBulbs.each { dimmer ->
			def nextLevel = dynamicLevel(dimmer, percentComplete)
			dimmer.setColor([hue: atomicState.foreHue, saturation: 100, level: nextLevel])
		}
	}
}

int dynamicLevel(dimmer, percentComplete) {
	def start = dynamicStartLevel()
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

	stop()

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

	int now = new Date().getTime()
	int diff = now - atomicState.start
	int totalRunTime = totalRunTimeMillis()
	int percentOfRunTime = (diff / totalRunTime) * 100
	log.debug "percentOfRunTime: ${percentOfRunTime}"

	percentOfRunTime
}

int totalRunTimeMillis() {
	int minutes = sanitizeInt(duration, 30)
	def seconds = minutes * 60
	def millis = seconds * 1000
	return millis as int
}


int dynamicStartLevel() {
	return startLevel ?: 1 as int
}

int dynamicEndLevel() {
	return endLevel ?: 99 as int
}

/*
 * Converts the device lists to regular lists so that I can run intersect on them
 */
private bulbIntersects() {
	if (tempBulbs && foreBulbs) {
      return tempBulbs.findAll {foreBulbs*.id.contains( it.id )}
	}
	else return null
}

private hasSetLevelCommand(device) {
	def isDimmer = false
	device.supportedCommands.each {
		if (it.name.contains("setLevel")) {
			isDimmer = true
		}
	}
	return isDimmer
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
		descriptionParts << "On ${fancyString(days)},"
	}

    if (foreBulbs){
		descriptionParts << "${fancyDeviceString(tempBulbs)} and ${fancyDeviceString(foreBulbs)} will start dimming"
	}
	else {
		descriptionParts << "${fancyDeviceString(tempBulbs)} will start dimming"
	}

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
	def title = "All bulbs will dim for ${duration ?: '30'} minutes from ${startLevel ?: '0'} to ${endLevel ?: '99'} and the color will be set based on the weather."

    return title
}

//This will query for the local forecast and set the temp color and the forecast color
def setWeatherColors() {
	def forecast
  if(locationIsDefined()) {
		if(zipcodeIsValid()) {
            forecast = getWeatherFeature("forecast", zipcode )       
		} else {
			log.warn "Invalid or missing zipcode entered, defaulting to location's zipcode"
            forecast = getWeatherFeature("forecast") }
	} else {
		log.error "Location is not defined"
	}

	if (!forecast){
		atomicState.tempHue = 50
		atomicState.foreHue = 50
		return
	}

	def temp_f = forecast.forecast.simpleforecast.forecastday[0].high.fahrenheit.toInteger()
	def newforecast = forecast.forecast.simpleforecast.forecastday[0].icon
	log.debug "Today's temp will be: $temp_f"
	log.debug "and today's forecast is: $newforecast"

	switch (temp_f) {
		case Integer.MIN_VALUE..20:
		  atomicState.tempHue = 75
		  break;
		case 21..35:
		  atomicState.tempHue = 66.4
		  break;
		case 36..45:
		  atomicState.tempHue = 58.1
		  break;
		case 46..55:
		  atomicState.tempHue = 49.8
		  break;
		case 56..65:
		  atomicState.tempHue = 41.5
		  break;
		case 66..75:
		  atomicState.tempHue = 33.2
		  break;
		case 76..85:
		  atomicState.tempHue = 24.9
		  break;
		case 86..95:
		  atomicState.tempHue = 16.6
		  break;
		case 96..105:
		  atomicState.tempHue = 8.3
		  break;
		case 106..integer.MAX_VALUE:
		  atomicState.tempHue = 0
		  break;
	}
	switch (newforecast){
		case ["chanceflurries","chancesleet","chancesnow","flurries","sleet","snow"]:  //Snow
		  atomicState.foreHue = 83
		  break
		case ["chancerain","rain"]:
		  atomicState.foreHue = 75
		  break
	    case ["chancetstorms","tstorms"]:
		  atomicState.foreHue = 10
		  break
	    case ["clear","fog","mostlysunny","partlysunny","sunny","unknown"]:
		  atomicState.foreHue = 25
		  break
	    case ["cloudy","hazy","mostlycloudy","partlycloudy"]:
		  atomicState.foreHue = 37
		  break
	}

	log.debug "The temp color hue is: ${atomicState.tempHue}"
	log.debug "The forecast color hue is: ${atomicState.foreHue}"
	sendNotificationEvent("Today will be ${forecast.forecast.txt_forecast.forecastday[0].fcttext}")
}

def locationIsDefined() {
	zipcodeIsValid() || location.zipCode || ( location.latitude && location.longitude )
}

def zipcodeIsValid() {
	zipcode && zipcode.isNumber() && zipcode.size() == 5
}
