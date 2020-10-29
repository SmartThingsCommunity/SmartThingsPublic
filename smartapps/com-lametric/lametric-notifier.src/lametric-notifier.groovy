/**
 *  Lametric Notifier
 *
 *  Copyright 2016 Smart Atoms Ltd.
 *  Author: Mykola Kirichuk
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
 import groovy.json.JsonOutput
 
definition(
    name: "LaMetric Notifier",
    namespace: "com.lametric",
    author: "Mykola Kirichuk",
    description: "Allows you to send notifications to your LaMetric Time when something happens in your home to notify the whole family.",
    category: "Family",
    iconUrl: "https://developer.lametric.com/assets/smart_things/weather_60.png",
    iconX2Url: "https://developer.lametric.com/assets/smart_things/weather_120.png",
    iconX3Url: "https://developer.lametric.com/assets/smart_things/weather_120.png")


preferences {
	page(name: "mainPage", title: "Show a message on your LaMetric when something happens", install: true, uninstall: true)
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}



           
def getSoundList() {
	[	
    	"none":"No Sound",
        "car" : "Car",
        "cash" : "Cash Register",
        "cat" : "Cat Meow",
        "dog" : "Dog Bark",
        "dog2" : "Dog Bark 2",
        "letter_email" : "The mail has arrived",
        "knock-knock" : "Knocking Sound",
        "bicycle" : "Bicycle",
        "negative1" : "Negative 1",
        "negative2" : "Negative 2",
        "negative3" : "Negative 3",
        "negative4" : "Negative 4",
        "negative5" : "Negative 5",
        "lose1" : "Lose 1",
        "lose2" : "Lose 2",
        "energy" : "Energy",
        "water1" : "Water 1",
        "water2" : "Water 2",
        "notification" : "Notification 1",
        "notification2" : "Notification 2",
        "notification3" : "Notification 3",
        "notification4" : "Notification 4",
        "open_door" : "Door unlocked",
        "win" : "Win",
        "win2" : "Win 2", 
        "positive1" : "Positive 1",
        "positive2" : "Positive 2",
        "positive3" : "Positive 3",
        "positive4" : "Positive 4",
        "positive5" : "Positive 5",
        "positive6" : "Positive 6",
        "statistic" : "Page turning",
        "wind" : "Wind",
        "wind_short" : "Small Wind",
    ] 
}

def getControlToAttributeMap(){
	[
        "motion": "motion.active",
        "contact": "contact.open",
        "contactClosed": "contact.close",
        "acceleration": "acceleration.active",
        "mySwitch": "switch.on",
        "mySwitchOff": "switch.off",
        "arrivalPresence": "presence.present",
        "departurePresence": "presence.not present",
        "smoke": "smoke.detected",
        "smoke1": "smoke.tested",
        "water": "water.wet",
        "button1": "button.pushed",
        "triggerModes": "mode",
        "timeOfDay": "time",
	]
}
                
def getPriorityList(){
	[
    	"warning":"Not So Important (may be ignored at night)",
        "critical": "Very Important"
    ]
}

def getIconsList(){
	state.icons = state.icons?:["1":"default"]
}


def getIconLabels() {
	state.iconLabels = state.iconLabels?:["1":"Default Icon"]
}

def getSortedIconLabels() {
	state.iconLabels = state.iconLabels?:["1":"Default Icon"]
    state.iconLabels.sort {a,b -> a.key.toInteger() <=> b.key.toInteger()};
}
def getLametricHost() { "https://developer.lametric.com" }
def getDefaultIconData() { """data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAgAAAAICAYAAADED76LAAAAe0lEQVQYlWNUVFBgYGBgYNi6bdt/BiTg7eXFyMDAwMCELBmz7z9DzL7/KBoYr127BpeEgbV64QzfRVYxMDAwMLAgSy5xYoSoeMPAwPkmjOG7yCqIgjf8WVC90xnQAdwKj7OZcMGD8m/hVjDBXLvDGKEbJunt5cXISMibAF0FMibYF7nMAAAAAElFTkSuQmCC""" }

def mainPage() {
    def iconRequestOptions = [headers: ["Accept": "application/json"],
						    uri: "${lametricHost}/api/v2/icons", query:["fields":"id,title,type,code", "order":"title"]]

    	def icons = getIconsList();
        def iconLabels = getIconLabels();
        if (icons?.size() <= 2)
        {
	        log.debug iconRequestOptions
            try {
				httpGet(iconRequestOptions) { resp ->
                   	int i = 2;
                    resp.data.data.each(){
                        def iconId = it?.id
                        def iconType = it?.type
                        def prefix = "i"
                        if (iconId)
                        {
                            if (iconType == "movie")
                            {
                                prefix = "a"
                            }
                            def iconurl = "${lametricHost}/content/apps/icon_thumbs/${prefix}${iconId}_icon_thumb_big.png";
                            icons["$i"] = it.code
                            iconLabels["$i"] = it.title
                        } else {
                        	log.debug "wrong id"
                        }
                        ++i;
                    }
                }
            } catch (e)
            {
                log.debug "fail ${e}";
            }
        }
	dynamicPage(name: "mainPage") {
		def anythingSet = anythingSet()
        def notificationMessage = defaultNotificationMessage();
        log.debug "set $anythingSet"
		if (anythingSet) {
			section("Show message when"){
				ifSet "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true , submitOnChange:true
				ifSet "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true, submitOnChange:true
				ifSet "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true, submitOnChange:true
				ifSet "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true, submitOnChange:true
				ifSet "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true, submitOnChange:true
				ifSet "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true, submitOnChange:true
				ifSet "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true, submitOnChange:true
				ifSet "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true, submitOnChange:true
				ifSet "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true, submitOnChange:true
				ifSet "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true, submitOnChange:true
				ifSet "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
				ifSet "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true, submitOnChange:true
				ifSet "timeOfDay", "time", title: "At a Scheduled Time", required: false, submitOnChange:true
			}
		}
		def hideable = anythingSet || app.installationState == "COMPLETE"
		def sectionTitle = anythingSet ? "Select additional triggers" : "Show message when..."

		section(sectionTitle, hideable: hideable, hidden: true){
			ifUnset "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true, submitOnChange:true
			ifUnset "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true, submitOnChange:true
			ifUnset "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true, submitOnChange:true
			ifUnset "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true, submitOnChange:true
			ifUnset "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true, submitOnChange:true
			ifUnset "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true, submitOnChange:true
			ifUnset "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true, submitOnChange:true
			ifUnset "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true, submitOnChange:true
			ifUnset "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true, submitOnChange:true
			ifUnset "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true, submitOnChange:true
			ifUnset "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
			ifUnset "triggerModes", "mode", title: "System Changes Mode", description: "Select mode(s)", required: false, multiple: true, submitOnChange:true
			ifUnset "timeOfDay", "time", title: "At a Scheduled Time", required: false, submitOnChange:true
		}

		section (title:"Select LaMetrics"){
			input "selectedDevices", "capability.notification", required: true, multiple:true
		}
        section (title: "Configure message"){
        	input "defaultMessage", "bool", title: "Use Default Text:\n\"$notificationMessage\"", required: false, defaultValue: true, submitOnChange:true
	        def showMessageInput = (settings["defaultMessage"] == null || settings["defaultMessage"] == true) ? false : true;
			if (showMessageInput)
        	{
             	input "customMessage","text",title:"Use Custom Text", defaultValue:"", required:false, multiple: false
        	}
        	input "selectedIcon", "enum", title: "With Icon", required: false, multiple: false, defaultValue:"1", options: getSortedIconLabels()
   			input "selectedSound", "enum", title: "With Sound", required: true, defaultValue:"none" , options: soundList
			input "showPriority", "enum", title: "Is This Notification Very Important?", required: true, multiple:false, defaultValue: "warning", options: priorityList
		}
		section("More options", hideable: true, hidden: true) {
			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			if (settings.modes) {
            	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            }
		}
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
}

private songOptions() {
		log.trace "song option"
	// Make sure current selection is in the set

	def options = new LinkedHashSet()
	if (state.selectedSong?.station) {
		options << state.selectedSong.station
	}
	else if (state.selectedSong?.description) {
		// TODO - Remove eventually? 'description' for backward compatibility
		options << state.selectedSong.description
	}

	// Query for recent tracks
	def states = sonos.statesSince("trackData", new Date(0), [max:30])
	def dataMaps = states.collect{it.jsonValue}
	options.addAll(dataMaps.collect{it.station})

	log.trace "${options.size()} songs in list"
	options.take(20) as List
}

private anythingSet() {
	for (it in controlToAttributeMap) {
    	log.debug ("key ${it.key} value ${settings[it.key]} ${settings[it.key]?true:false}")
		if (settings[it.key]) {
	        log.debug constructMessageFor(it.value, settings[it.key])
			return true
		}
	}
	return false
}

def defaultNotificationMessage(){
	def message = "";
	for (it in controlToAttributeMap)  {
		if (settings[it.key]) {
	        message = constructMessageFor(it.value, settings[it.key])
            break;
		}
	}
	return message;
}

def constructMessageFor(group, device)
{
	log.debug ("$group $device")
	def message;
    def firstDevice;
    if (device instanceof List)
    {
    	firstDevice = device[0];
    } else {
    	firstDevice = device;
    }
    switch(group)
    {
    	case "motion.active":
        	message = "Motion detected by $firstDevice.displayName at $location.name"
        break;
        case "contact.open":
        	message = "Openning detected by $firstDevice.displayName at $location.name"
        break;
		case "contact.closed":
        	message = "Closing detected by $firstDevice.displayName at $location.name"
        break;
        case "acceleration.active":
        	message = "Acceleration detected by $firstDevice.displayName at $location.name"
        break;
        case "switch.on":
        	message = "$firstDevice.displayName turned on at $location.name"
        break;
        case "switch.off":
        	message = "$firstDevice.displayName turned off at $location.name"
        break;
        case "presence.present":
	        message = "$firstDevice.displayName detected arrival at $location.name"
        break;
        case "presence.not present":
	        message = "$firstDevice.displayName detected departure at $location.name"
        break;
        case "smoke.detected":
        	message = "Smoke detected by $firstDevice.displayName at $location.name"
        break;
         case "smoke.tested":
        	message = "Smoke tested by $firstDevice.displayName at $location.name"
        break;
        case "water.wet":
        	message = "Dampness detected by $firstDevice.displayName at $location.name"
        break;
        case "button.pushed":
	        message = "$firstDevice.displayName pushed at $location.name"
		break;
        case "time":
        break;
//        case "mode":
//        	message = "Mode changed to ??? at $location.name"
        break;
    }
    return message;
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
	log.trace "subscribe to events"
    log.debug "${contact} ${contactClosed} ${mySwitch} ${mySwitchOff} ${acceleration}${arrivalPresence} ${button1}"
//	subscribe(app, appTouchHandler)
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
	log.trace "eventHandler(${evt?.name}: ${evt?.value})"
    def name = evt?.name;
    def value = evt?.value;
    
	if (allOk) {
			log.trace "allOk"
 			takeAction(evt)
		}
		else {
			log.debug "Not taking action because it was already taken today"
		}
}
def modeChangeHandler(evt) {
	log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
	if (evt?.value in triggerModes) {
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

	log.trace "takeAction()"
	def messageToShow
    if (defaultMessage)
    {
    	messageToShow = constructMessageFor("${evt.name}.${evt.value}", evt.device);
    } else {
    	messageToShow = customMessage;
    }
	if (messageToShow)
    {
    	log.debug "text ${messageToShow}"
    	def notification = [:];
        def frame1 = [:];
        frame1.text = messageToShow;
        if (selectedIcon != "1")
        {
        	frame1.icon = state.icons[selectedIcon];
        } else {
        	frame1.icon = defaultIconData;
        }
        def soundId = sound;
        def sound = [:];
        sound.id = selectedSound;
        sound.category = "notifications";
        def frames = [];
        frames << frame1;
        def model = [:];
        model.frames = frames;
        if (selectedSound != "none")
        {
        	model.sound = sound;
        }
        notification.model = model;
        notification.priority = showPriority;
        def serializedData = new JsonOutput().toJson(notification);

        selectedDevices.each { lametricDevice ->
            log.trace "send notification to ${lametricDevice} ${serializedData}"
        	lametricDevice.deviceNotification(serializedData)
        }
    } else {
    	log.debug "No message to show"
    }
	
	log.trace "Exiting takeAction()"
}

private frequencyKey(evt) {
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
		def start = timeToday(starting, location?.timeZone).time
		def stop = timeToday(ending, location?.timeZone).time
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

private getTimeLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
