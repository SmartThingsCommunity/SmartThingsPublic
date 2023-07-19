/**
 *  Porch Light
 *
 *  Copyright 2015 Joseph DiBenedetto
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
 */
 
import java.text.SimpleDateFormat
import groovy.time.TimeCategory

definition(
	name: "Smart Porch Light",
	namespace: "jdiben",
	author: "Joseph DiBenedetto",
	description: "Turn on your porch light, or any other dimmable light, dimmed to a level you set at sunset and increase to full brightness when someone arrives or some other action is triggered. After a number of minutes the light will dim back to its original level. Optionally, set the light to turn off at a specified time while still turning on when someone arrives. This app runs from sunset to sunrise.\n\nSelect as many lights as you like. Additional triggers include motion detection, door knock, door open and app button. A door bell trigger will also be added in the future.",
	category: "Safety & Security",
	iconUrl: "http://apps.shiftedpixel.com/porchlight/porchlight.png",
	iconX2Url: "http://apps.shiftedpixel.com/porchlight/porchlight@2x.png",
	iconX3Url: "http://apps.shiftedpixel.com/porchlight/porchlight@2x.png"
)

preferences {
	page(name: "mainSettingsPage")
	page(name: "scheduleSettingsPage")
}

def mainSettingsPage() {
	dynamicPage(
	name: 		"mainSettingsPage", 
		title: 		"", 
		install: 	true, 
		uninstall: 	true
	) {

    	//Lights/switches to use. Only allow dimmable lights
		section("Control these switches") {
			input (
				name:		"switches", 
				type:		"capability.switchLevel", 
				title: 		"Switches?", 
				required: 	true, 
				multiple: 	true
			)
		}

		//Presence Sensors (including phones)
		section("When these people arrive", hideable:true, hidden:(presence == null) ? true : false) {
			input (
				name:		"presence", 
				type:		"capability.presenceSensor", 
				title:		"Who?", 
				required:	false,
				multiple: 	true
			)
		
			input (
				name:			"brightnessLevelPresence", 
				type:			"number", 
				title: 			"Brightness Level (1-100)?", 
				required:		false, 
				defaultValue:	100
			)
		}
		
		//Motion sensors
		section("When motion is detected", hideable:true, hidden:(motion == null) ? true : false) {
			input (
				name:		"motion", 
				type:		"capability.motionSensor", 
				title:		"Which?", 
				required: 	false, 
				multiple: 	true
			)
			
			input (
				name:			"brightnessLevelMotion", 
				type:			"number", 
				title: 			"Brightness Level (1-100)?", 
				required: 		false, 
				defaultValue:	100
			)
		}
		
		//Contact sensors
		section("When these doors are opened", hideable:true, hidden:(contact == null) ? true : false) {
			input (
				name:		"contact", 
				type:		"capability.contactSensor", 
				title:		"Which?", 
				required: 	false, 
				multiple: 	true
			)
			
			input (
				name:			"brightnessLevelContact", 
				type:			"number", 
				title: 			"Brightness Level (1-100)?", 
				required: 		false, 
				defaultValue:	100
			)
		}

		//Vibration sensors to detect a knock
		section("When someone knocks on these doors", hideable:true, hidden:(acceleration == null) ? true : false) {
			input (
				name:		"acceleration", 
				type:		"capability.accelerationSensor", 
				title:		"Which?", 
				required: 	false, 
				multiple: 	true
			)
			
			input (
				name:			"brightnessLevelAcceleration", 
				type:			"number", 
				title: 			"Brightness Level (1-100)?", 
				required: 		false, 
				defaultValue:	100
			)
		}

		//Enable a button overlay on the app icon to trigger lights
		section("When the app button is tapped", hideable:true, hidden:(appButton != true) ? true : false) {
			input (
				name:			"appButton", 
				type:			"bool", 
				title: 			"Tap to brighten lights?", 
				defaultValue: 	false,
				required:		false
			)
			
			input (
				name:			"brightnessLevelTap", 
				type:			"number", 
				title: 			"Brightness Level (1-100)?", 
				required: 		false, 
				defaultValue:	100
			)
		}
		
		//Minutes after event is detected before lights are set to their standby levels
		section("Dim after") {
			paragraph	"The number of minutes after an event is triggered before the lights are dimmed."
			input (
				name:			"autoOffMinutes", 
				type:			"number", 
				title: 			"Minutes (0 - 30)", 
				required: 		false, 
				defaultValue:	5
			)
		}

		section("Standby Light Brightness") {
			paragraph	"The brightness level that the lights will be set to at sunset and whenever an event times out."
			input (
				name:			"brightnessLevelDefault", 
				type:			"number", 
				title: 			"Brightness Level (1-100)?", 
				required:		false, 
				defaultValue:	10
			)
			paragraph	"If the standby brightness is changed manually, remember that level and override the standby level above until the next day. Due to the delay caused by SmartThings device polling, this may not always work as expected."
			input (
				name:			"rememberLevel", 
				type:			"bool", 
				title: 			"Remember changes",
				defaultValue:	true
			)
		}
		
		//Open the scheduling page
		section("Schedule") {        
			href(
				title: 			"Active from",
				name: 			"toScheduleSettingsPage", 
				page: 			"scheduleSettingsPage", 
				description:	readableSchedule(), //Display a more readable schedule description
				state: 			"complete"
			)
		}
		
		//Enable certain events to output to hello home
		section("Use Notifications") {
			input (
				name:			"useHelloHome", 
				type:			"bool", 
				title: 			"Show events in Notifications?", 
				defaultValue: 	true
			)
		}
		
		//Specify a display name for this app (optional)
		section("Assign a Name") {
			label(
				name:		"appName",
				title:		"App Name (optional)",
				required:	false,
				multiple: 	false
			)
		}
	}
}
 
def scheduleSettingsPage() {
	dynamicPage(
		name: 		"scheduleSettingsPage", 
		install: 	false, 
		uninstall: 	false,
		nextPage: 	"mainSettingsPage"
	) {
		section("Schedule") {
			paragraph	"By default, the app runs from sunset to sunrise. You can offset both sunset and sunrise by up to +/- 2 hours"
		
			input (
				name:			"sunsetOffset", 
				type:			"enum", 
				title: 			"Sunset Offset in minutes?", 
				options: 		['-120', '-105', '-90', '-75', '-60', '-45', '-30', '-15', '0', '15', '30', '45', '60', '75', '90', '105', '120'],
				defaultValue: 	"0"
			)
			
			input (
				name:			"sunriseOffset", 
				type:			"enum", 
				title: 			"Sunrise Offset in minutes?", 
				options: 		['-120', '-105', '-90', '-75', '-60', '-45', '-30', '-15', '0', '15', '30', '45', '60', '75', '90', '105', '120'],
				defaultValue: 	"0"
			)
		}
	
		section("Lights off override") {
			paragraph	"By default, the lights will turn off at sunrise when the app goes to sleep. Here, you can override the time that those lights are turned off. This will cause the light to turn off when no one is around instead of just dimming. The lights will still come on when someone arrives until sunrise. Leave the time blank to keep the lights on until sunrise."
			input(
				name:			"timeDefaultEnd", 
				type:			"time", 
				title: 			"Turn off at", 
				required: 		false,
				defaultValue:	null
			)
		}
	}
}

def installed() {
	debug("Installed with settings: ${settings}")
	initialize()
}

def updated() {
	debug("Updated with settings: ${settings}")

	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	state.inDefault = false //Are we in the default schedule. Meaning the lights go back to standby levels after an event
	state.enabled = false //Is the app enabled for events (sunrise to sunset)
    state.active = false //Is an event currently active
    state.levels = []
    state.lastVerified = new Date()
	state.nextEvent = 'sunset'
	
	def times = getSunriseAndSunset(sunsetOffset: formatOffset(sunsetOffset), sunriseOffset: formatOffset(sunriseOffset), date: new Date())
	def now = new Date()
	if (now < times.sunrise || now > times.sunset) {
		sunsetHandler()
	} else {
		scheduleNextEvent()
	}
	
	scheduleDefaultOff()
	
	//Enable events 
	if (presence != null) subscribe(presence, "presence", presenceHandler)
	if (motion != null) subscribe(motion, "motion", motionHandler)
	if (contact != null) subscribe(contact, "contact", contactHandler)
	if (acceleration != null) subscribe(acceleration, "acceleration.active", accelerationHandler)
	if (appButton) subscribe(app, appTouchHandler)
	
	startVerificationSchedule()
}

def sunsetHandler() {

	debug('Sunset')

	state.enabled = true
    state.inDefault = true
    state.active = false
    state.levels.clear()
    state.nextEvent = 'sunrise'
	
	def output = "Smart Porch Light is now active"

	if (brightnessLevelDefault != null && brightnessLevelDefault > 0) {

		lightSet(brightnessLevelDefault)
		
		output = output + " and has turned your light" + plural(switches.size())[0] + " on to " + brightnessLevelDefault + "%"

	}
	
	helloHome(output + ".")
		
	scheduleNextEvent()
	
}

def scheduleSunset() {
	
	def runTime = getSunriseAndSunset(sunsetOffset: formatOffset(sunsetOffset))
	def sunset = runTime.sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	
	state.nextEventTime = sunset
	
	debug("Sunset scheduled for " + sunset.toString())
	
	String sunsetString = sunset.toString()
	runOnce(sunsetString, "sunsetHandler")
	
	verifySchedule()
}

def sunriseHandler() {
	
	debug('Sunrise')
	
	state.inDefault = false
	state.active = false
	state.enabled = false
	state.nextEvent = 'sunset'
	
	defaultOff()
	
	if (timeDefaultEnd == null) {
		helloHome("It's sunrise. Smart Porch Light has turned off your light" + plural(switches.size())[0] + " and is now inactive.")
	} else {
		helloHome("It's sunrise. Smart Porch Light is now inactive.")
	}	

	scheduleNextEvent()
}

def scheduleSunrise() {
	def runTime = getSunriseAndSunset(sunriseOffset: formatOffset(sunriseOffset))
	def sunrise = runTime.sunrise
	
	if (new Date() > runTime.sunrise) {
		runTime = getSunriseAndSunset(sunriseOffset: formatOffset(sunriseOffset), date: new Date() + 1)
	}

	sunrise = runTime.sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	
	state.nextEventTime = sunrise
	
	debug("Sunrise scheduled for " + sunrise.toString())
	
	String sunriseString = sunrise.toString()
	runOnce(sunriseString, "sunriseHandler")
	
	verifySchedule()
}

def defaultOffHandler() {
	state.nextEvent = 'sunrise'
	
	defaultOff()
	
	helloHome("Smart Porch Light has turned off your light" + plural(switches.size())[0] + " as scheduled.")	
    
	verifySchedule()
}

def scheduleDefaultOff() {
	if (brightnessLevelDefault != null && timeDefaultEnd != null) {
		schedule(timeDefaultEnd, defaultOffHandler)
		debug("Default off set for " + timeDefaultEnd)
	}
}

def scheduleNextEvent() {
	if (state.nextEvent == 'sunset') {
		debug("Next Event: Sunset")
		scheduleSunset()
	} else {
		debug("Next Event: Sunrise")
		scheduleSunrise()
	}
}



def presenceHandler(evt) {

	if(evt.value == "present" && state.enabled && !state.active) {
		lightOnEvent(brightnessLevelPresence)
	
		scheduleAutoOff()
		
		helloHome(evt.displayName + " arrived. Smart Porch Light has turned your light" + plural(switches.size())[0] + " on to " + brightnessLevelPresence + "%.")
	}

	verifySchedule()
}

def motionHandler(evt) {
	if (evt.value == "active" && state.enabled && !state.active) {
		lightOnEvent(brightnessLevelMotion)
	
		scheduleAutoOff()
		
		helloHome(evt.displayName + " detected motion. Smart Porch Light has turned your light" + plural(switches.size())[0] + " on to " + brightnessLevelMotion + "%.")
	} 
	verifySchedule()
}

def contactHandler(evt) {
	if (evt.value == "open" && state.enabled && !state.active) {
		def reset=100
		if (brightnessLevelContact != null) reset = brightnessLevelContact
		lightOnEvent(reset)
		scheduleAutoOff()
		
		helloHome(evt.displayName + " opened.  Smart Porch Light has turned your light" + plural(switches.size())[0] + " on to " + brightnessLevelContact + "%.")
	}
	verifySchedule()
}

def accelerationHandler(evt) {
	if (evt.value == "active" && state.enabled && !state.active) {
		def reset=100
		if (brightnessLevelAcceleration != null) reset = brightnessLevelAcceleration
		lightOnEvent(reset)
		scheduleAutoOff()
		
		helloHome("Someone knocked on " + evt.displayName + ".  Smart Porch Light has turned your light" + plural(switches.size())[0] + " on to " + brightnessLevelAcceleration + "%.")
	}
	verifySchedule()
}

def appTouchHandler(evt) {
	def reset=100
	if (brightnessLevelTap != null) reset = brightnessLevelTap
	lightOnEvent(reset)
	scheduleAutoOff()
	verifySchedule()
}



def verifySchedule() {
	//This method is run every 15 minutes. It's also run each time a trigger is fired in case the schedule that controlls this fails too

	def currentTime = new Date()
	use(TimeCategory) {
		/*
			Because we run this method at the time a schedule changes, it's possible that the next event time will not have been updated yet.
			This could cause a false positive when checking to see if a schedule was missed. So we set the current time back 1 minute to give
			it a sufficient buffer for comparison
		*/
		currentTime = currentTime - 1.minutes
	}

	if (
		(state.nextEventTime instanceof Date && new Date() > state.nextEventTime) ||
		(state.nextEventTime instanceof String && new Date() > new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", state.nextEventTime))
	) {
		//If we get here it means we missed an event and need to restart the schedules
		debug("SCHEDULE FAILED - MISSED EVENT")
		restartVerifictionSchedule()
	}
	
	if (
		(state.lastVerified instanceof Date && new Date() - state.lastVerified > 1200000) ||
		(state.lastVerified instanceof String && new Date() - new Date().parse("yyyy-MM-dd'T'HH:mm:ssZ", state.lastVerified) > 1200000)
	) {
		//If we get here it means that it's been more then 20 minutes since this method was scheduled to run, so lets restart everything
		debug("SCHEDULE FAILED - MISSED VERIFICATION")
		restartVerifictionSchedule()
	}

	state.lastVerified = currentTime
}

def startVerificationSchedule() {
	//Start the verifiction schedule that ensures everything is still on schedule
	schedule("0 0/10 * * * ?", "verifySchedule")
}
 
def restartVerifictionSchedule() {
	//We're here because a schedule failed
	helloHome("SCHEDULE FAILED - RESTARTING SMART PORCH LIGHT")
	updated() //Restart the app to reset the schedules
	
	
	//sendPushMessage('Smart Porch Ligt schedule failed and has been restarted')
}

def scheduleAutoOff() {
	//Schedule lights to dim after x minutes.
    //This is executed after every event and is reset to x minutes on all subsequent events if this schedule hasn't yet run.
	if (autoOffMinutes != null) {
		debug('Auto off is scheduled for ' + autoOffMinutes + ' minutes')
        
		//Make sure that a valid number was specified. Adjust number if needed
		if (autoOffMinutes < 1) {
			autoOffMinutes = 1
		} else if (autoOffMinutes > 30) {
			autoOffMinutes = 30
		}
		runIn(60 * autoOffMinutes, autoOff)
	}
}

def autoOff() {
	//Reset lights to default level
	
	state.active = false
	
	lightReset()
	
	def output = "It's been "+ autoOffMinutes + " minute" + plural(autoOffMinutes)[0] + ". "
	if (state.inDefault) {
		output += "Resetting your light" + plural(switches.size())[0] + " to standby."
	} else {
		output += "Turning your light" + plural(switches.size())[0] + " off."
	}
	helloHome(output)
}

def lightSet(level) {
	//Don't allow values above 100% for brightness
	if (level > 100) {
		level = 100
	} else if (level == null) {
		level = 0
	}
	
	//Set lights to specified level
	switches.setLevel(level)
	debug('brightness set to ' + level)
}

def lightOnEvent(level) {
	state.active = true
	if (rememberLevel) {
		state.levels.clear()
		switches.each {
			state.levels.add(it.currentValue('level'))
		}
	}
	lightSet(level)
}

def lightReset() {	
	if (rememberLevel && state.levels.size() == switches.size()) {
		switches.eachWithIndex { it, i -> 
			it.setLevel(state.levels[i])
			//helloHome("Light reset to " + state.levels[i] + "%")
		}
	} else {
		//set default "reset" to 0% brightness
		def reset = 0    
		
		//If brightness level is set, use that instead of the default set above
		if (state.inDefault && brightnessLevelDefault != null) reset = brightnessLevelDefault
		
		debug('Auto off executed - reset to default level')
		lightSet(reset)
	}

	debug('reset lights')
}



def defaultOn() {
	//Enables app at sunset and turns lights on to default level
	state.inDefault = true
	lightReset()
	debug('Default - schedule started')
}

def defaultOff() {
	//Disable app at sunrise or when scheduled and turn lights off
	state.inDefault = false
    state.active = false
	switches.off()

	debug('Default - schedule ended')
}



def readableSchedule() {

	//Create a more readable schedule description to display on the main settings page when setting on the schedule page are modified.

	def sunrise = (sunriseOffset == null) ? 0 : sunriseOffset.toInteger()
	def sunset = (sunsetOffset == null) ? 0 : sunsetOffset.toInteger()

	//def output = "Active from\n"
	def output = ""
	
	if (sunset != null && sunset !=0) output += convertMinutes(sunset) + ((sunset > 0) ? " after " : " before ")
	output += "sunset to"
	
	if (sunrise != null && sunrise !=0) output += " " + convertMinutes(sunrise) + ((sunrise > 0) ? " after" : " before")
	output += " sunrise."
	
	if (timeDefaultEnd != null) {
		output += "\n\nStandby light" + plural(switches.size())[0] + " turn" + plural(switches.size(), true)[0] + " off at "
		
		def outputFormat = new SimpleDateFormat("h:mm a")
		def inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
		
		def date = inputFormat.parse(timeDefaultEnd)
		output += outputFormat.format(date)
	}

	return output
}

def convertMinutes(totalMinutes) {

	totalMinutes = totalMinutes.abs()
	int hours = Math.floor(totalMinutes / 60).toInteger()
	int minutes = (totalMinutes % 60)
	
	def output = ""
	
	if (hours > 0) {
		output += hours + " hour" + plural(hours)[0]
		if (minutes > 0) output += ' and '
	}
	
	if (minutes > 0) output += minutes + " minutes"
	
	return output
}

def plural(count, r = false) {
	//return an "s" to append to a word if "count" indicates zero or more than one
	//Is this really necessary? No, but it makes me happy.
	
	def language = []
	
	if ((count == 1 && !r) || (count != 1 && r)) {
		language.addAll(['','is','was'])
	} else {
		language.addAll(['s','are','were'])
	}
	
	return language
}

def formatOffset(offset) {
	def formatted =''
	if (offset == null || offset == 0) {
		formatted = "00:00"
	} else {
		offset = offset.toInteger()
		if (offset < 0) formatted += '-'
	
		def totalMinutes = offset.abs()
		int hours = Math.floor(totalMinutes / 60).toInteger()
		int minutes = (totalMinutes % 60)
		
		formatted += "0" + hours + ":"
		
		if (minutes == 0) {
			formatted += "00"
		} else if (minutes < 10) {
			formatted += "0" + minutes
		} else {
			formatted += "" + minutes
		}
	}
	
	return formatted
}

def debug(msg) {
	//Enable debugging. Comment out line below to disable output.
	log.debug(msg)
	
	//Uncomment the next line to send debugging messages to hello, home. I use this when live logging breaks, which is often for me, and when I need a way to view data that's logged when I'm not logged in. 
	//sendNotificationEvent("DEBUG: " + msg)
}

def helloHome(msg) {
	if (useHelloHome) sendNotificationEvent(msg)
	log.debug("Hello, home: " + msg)
}