/**
 *  Simple Sync Trigger
 *
 *  Copyright 2015 Roomie Remote, Inc.
 *
 *	Date: 2015-09-22
 */
definition(
    name: "Simple Sync Trigger",
    namespace: "roomieremote-ratrigger",
    author: "Roomie Remote, Inc.",
    description: "Trigger Simple Control activities when certain actions take place in your home.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-60.png",
    iconX2Url: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-120.png",
    iconX3Url: "https://s3.amazonaws.com/roomieuser/remotes/simplesync-120.png")


preferences {
	page(name: "agentSelection", title: "Select your Simple Sync")
	page(name: "refreshActivities", title: "Updating list of Simple Sync activities")
    page(name: "control", title: "Run a Simple Control activity when something happens")
	page(name: "timeIntervalInput", title: "Only during a certain time", install: true, uninstall: true) {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def agentSelection()
{
	if (agent)
    {
		state.refreshCount = 0
    }
    
	dynamicPage(name: "agentSelection", title: "Select your Simple Sync", nextPage: "control", install: false, uninstall: true) {
		section {
			input "agent", "capability.mediaController", title: "Simple Sync", required: true, multiple: false
		}
	}
}

def control()
{
	def activities = agent.latestValue('activities')
    
    if (!activities || !state.refreshCount)
    {
	    int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
	    state.refreshCount = refreshCount + 1
	    def refreshInterval = refreshCount == 0 ? 2 : 4
		
	    // Request activities every 5th attempt
    	if((refreshCount % 5) == 0)
        {
    	    agent.getAllActivities()
		}
        
        dynamicPage(name: "control", title: "Updating list of Simple Control activities", nextPage: "", refreshInterval: refreshInterval, install: false, uninstall: true) {
        	section("") {
            	paragraph "Retrieving activities from Simple Sync"
            }
        }
	}
	else
    {
	dynamicPage(name: "control", title: "Run a Simple Control activity when something happens", nextPage: "timeIntervalInput", install: false, uninstall: true) {
		def anythingSet = anythingSet()
		if (anythingSet) {
			section("When..."){
				ifSet "motion", "capability.motionSensor", title: "Motion Detected", required: false, multiple: true
				ifSet "motionInactive", "capability.motionSensor", title: "Motion Stops", required: false, multiple: true
				ifSet "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
				ifSet "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
				ifSet "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
				ifSet "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
				ifSet "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
				ifSet "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
				ifSet "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
				ifSet "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
				ifSet "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
				ifSet "timeOfDay", "time", title: "At a Scheduled Time", required: false
			}
		}
		section(anythingSet ? "Select additional triggers" : "When...", hideable: anythingSet, hidden: true){
			ifUnset "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
			ifUnset "motionInactive", "capability.motionSensor", title: "Motion Stops", required: false, multiple: true
			ifUnset "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
			ifUnset "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
			ifUnset "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
			ifUnset "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
			ifUnset "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
			ifUnset "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
			ifUnset "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
			ifUnset "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
			ifUnset "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
			ifUnset "timeOfDay", "time", title: "At a Scheduled Time", required: false
		}
		section("Run this activity"){
	        input "activity", "enum", title: "Activity?", required: true, options: new groovy.json.JsonSlurper().parseText(activities ?: "[]").activities?.collect { ["${it.uuid}": it.name] }
		}
        
		section("More options", hideable: true, hidden: true) {
			input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
			input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
		}
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)"
		}
	}
    }
}

private anythingSet() {
	for (name in ["motion","motionInactive","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","button1","triggerModes","timeOfDay"]) {
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
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	unschedule()
	subscribeToEvents()
}

def subscribeToEvents() {
	log.trace "subscribeToEvents()"
	subscribe(app, appTouchHandler)
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(motionInactive, "motion.inactive", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(button1, "button.pushed", eventHandler)

	if (triggerModes) {
		subscribe(location, modeChangeHandler)
	}

	if (timeOfDay) {
		schedule(timeOfDay, scheduledTimeHandler)
	}
}

def eventHandler(evt) {
	if (allOk) {
		def lastTime = state[frequencyKey(evt)]
		if (oncePerDayOk(lastTime)) {
			if (frequency) {
				if (lastTime == null || now() - lastTime >= frequency * 60000) {
					startActivity(evt)
				}
				else {
					log.debug "Not taking action because $frequency minutes have not elapsed since last action"
				}
			}
			else {
				startActivity(evt)
			}
		}
		else {
			log.debug "Not taking action because it was already taken today"
		}
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
	startActivity(evt)
}

private startActivity(evt) {
	agent.startActivity(activity)

	if (frequency) {
		state.lastActionTimeStamp = now()
	}
}

private frequencyKey(evt) {
	//evt.deviceId ?: evt.value
	"lastActionTimeStamp"
}

private dayString(Date date) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	df.format(date)
}

private oncePerDayOk(Long lastTime) {
	def result = true
	if (oncePerDay) {
		result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
		log.trace "oncePerDayOk = $result"
	}
	result
}

// TODO - centralize somehow
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

private timeIntervalLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}