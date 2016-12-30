/**
 *  Travel Time Guru
 *
 *  BETA 1.2
 * 
 *  Copyright 2015 Tim Slagle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *	The original licensing applies, with the following exceptions:
 *		1.	These modifications may NOT be used without freely distributing all these modifications freely
 *			and without limitation, in source form.	 The distribution may be met with a link to source code
 *			with these modifications.
 *		2.	These modifications may NOT be used, directly or indirectly, for the purpose of any type of
 *			monetary gain.	These modifications may not be used in a larger entity which is being sold,
 *			leased, or anything other than freely given.
 *		3.	To clarify 1 and 2 above, if you use these modifications, it must be a free project, and
 *			available to anyone with "no strings attached."	 (You may require a free registration on
 *			a free website or portal in order to distribute the modifications.)
 *		4.	The above listed exceptions to the original licensing do not apply to the holder of the
 *			copyright of the original work.	 The original copyright holder can use the modifications
 *			to hopefully improve their original work.  In that event, this author transfers all claim
 *			and ownership of the modifications to "SmartThings."
 *
 *	Original Copyright information:
 *
 *	Copyright 2015 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "Travel Time Guru",
    namespace: "tslagle13",
    author: "Tim Slagle",
    description: "Uses the Bing Maps Maps API to calculate your time of travel to a destination with current traffic and alerts you via push, sonsos, or hue bulbs when you need to leave.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
)

preferences {
	page(name: "mainPage", install: true, uninstall: true)
    page(name: "wayPoints")
    page(name: "triggers")
    page(name: "setupTimes")
    page(name: "notificationSettings")
    page(name: "apiKey")
    page(name: "appRestrictions")
}

def mainPage() { 
    dynamicPage(name: "mainPage") {
        section("About"){
            paragraph "This app will lookup and notify you of when you need to leave for work.  Provide it with two way points and it will automatically check traffic every 5 minutes.  As it gets closer to the the time you need to leave in order to arrive on time it will begin to alert you based on the alert thresholds you set.  (This does require a Bing Maps API key from bingmapsportal.com)"
        }
        section("Current travel time. (Touch the app button to update)") {
            paragraph travelParagraph()
        }
        section("Setup") {
            href "apiKey", title: "Bing Maps API Key", state: greyOutApi(), description: apiDescription()
            href "wayPoints", title: "Select Way Points", state: greyOutWayPoints(), description: waypointDescription()
            href "triggers", title: "Setup App Triggers", state: greyOutTriggers(), description: triggerDescription()
            href "setupTimes", title: "Select Start Time", state: greyOutTimes()
            href "notificationSettings", title: "Notification Settings", state: greyOutNotifications(), description: notificationsDescription()
            href "appRestrictions", title: "App Restrictions", state: greyOutRestrictions(), description: restrictionsDesription()
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }   
}

def wayPoints(){
    dynamicPage(name: "wayPoints") {
        section("About"){
            paragraph "Please set your home address and work address to calculate travel time between them. Ex. 85 Challenger Road, Ridgefield Park, NJ or  Samsung Electronics"
        }
        section("Way Points"){	
            input "location1", "text", title: "Home address", required: True
            input "location2", "text", title: "Office address", required: True
        }	  
    }
}

def triggers(){
    dynamicPage(name: "triggers") {
        section("About"){
            paragraph "Select what events will trigger the app to start running in the morning."
        }
        section("Trigger the app to start when..."){
            input "motions", "capability.motionSensor", title: "Motion is sensed here", required: false
            input "contactOpen", "capability.contactSensor", title: "The following are opened", required: false
            input "contactClosed", "capability.contactSensor", title: "The following are closed", required: false
        }	  
    }
}

def setupTimes(){
    dynamicPage(name: "setupTimes") {
        section("About"){
            paragraph "Setup the time you want to arrive at work as well as when the app should start to notify you that you need to leave."
        }
        section("Start Time"){
            input "mytime", "time", title: "When do you want to arrive at work?", required: true
        }
        section("Notify Settings"){
            input "notifyLead", "number", title: "How many minutes before your first notification to leave? (default: 15 min)", required: True, defaultValue: 15
            input "notifyLeadWarn", "number", title: "How many minutes before your second notification to leave? (default: 10 min)", required: True, defaultValue: 10
            input "notifyLeadEmergency", "number", title: "How many minutes before your last notification to leave? (default: 5 min)", required: True, defaultValue: 5
        }	  
    }
}

def notificationSettings(){
    dynamicPage(name: "notificationSettings") {
        section("About"){
            paragraph "Select the way you want to be notified when a notification is sent."
        }
        section("Alert Settings"){
            input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
            input "sonos", "capability.musicPlayer", title:"Speak message via: (optional) ", multiple: true, required: false
            input "volume", "enum", title: "at this volume...", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
            input "resumePlaying", "bool", title: "Resume currently playing music after alert?", required: false, defaultValue: true
        } 
        section("Hues"){
            input "hues", "capability.colorControl", title: "Change the color of these bulbs for each warning... (optional)", required:false, multiple:true
            input "colorNotify", "enum", title: "Change to this color on the first warning", required: false, options: [["White":"White"],["Daylight":"Daylight"],["Blue":"Blue"],["Green":"Green"],["Yellow":"Yellow"],["Orange":"Orange"],["Purple":"Purple"],["Pink":"Pink"],["Red":"Red"]]
            input "colorWarn", "enum", title: "Change to this color on the second warning", required: false, options: [["White":"White"],["Daylight":"Daylight"],["Blue":"Blue"],["Green":"Green"],["Yellow":"Yellow"],["Orange":"Orange"],["Purple":"Purple"],["Pink":"Pink"],["Red":"Red"]]
            input "colorEmergency", "enum", title: "Change to this color on the last warning", required: false, options: [["White":"White"],["Daylight":"Daylight"],["Blue":"Blue"],["Green":"Green"],["Yellow":"Yellow"],["Orange":"Orange"],["Purple":"Purple"],["Pink":"Pink"],["Red":"Red"]]
        }	  
    }
}

def apiKey(){
    dynamicPage(name: "apiKey") {
        section("About"){
            paragraph "Here you will need to provide the Bing Maps API key you got from the bingmapsportal.  You will need to go to bingmapsportal.com and sign up for a dev account which will provide you with a secret key you can use to acccess their API"
        }
        section("Bing Maps API Key"){
            input "apiKey", "text", title: "Microsoft API Secret Key", required: True
        }	  
    }
}

def appRestrictions(){
    dynamicPage(name: "appRestrictions") {
        section("About"){
            paragraph "Select when the app will stop running. If you don't select any of these the app will continue to run indefinitely.  (Pro Tip: You want to restrict it in some way.)"
        }
        section("Only run this app when"){
            input "people", "capability.presenceSensor", title: "These people are home", required: false
            input "modes", "mode", title: "The current mode is", required: false, multiple: true
            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: greyOutTimeLabel()    
        }
        
    }
}

page(name: "timeIntervalInput", title: "Only during a certain time", refreshAfterSelection:true) {
		section {
			input "starting", "time", title: "Starting", required: false, refreshAfterSelection:true
			input "ending", "time", title: "Ending", required: false, refreshAfterSelection:true
		}
}  

def installed() {
	subscribe(app, totalTravelTime)
    
	if(motions){
    	subscribe(motions, "motion.active", trafficCheck)
    }
    
    if(contactOpen){
    	subscribe(contactOpen, "contact.open", trafficCheck)
    }
    
    if(contactClosed){
    	subscribe(contactClosed, "contact.open", trafficCheck)
    }
    log.debug "installed with settings: $settings"
}

def updated() {
    unsubscribe()
    unschedule(trafficCheck)
    state.clear()
    subscribe(app, totalTravelTime)
    
	if(motions){
    	subscribe(motions, "motion.active", trafficCheck)
    }
    
    if(contactOpen){
    	subscribe(contactOpen, "contact.open", trafficCheck)
    }
    
    if(contactClosed){
    	subscribe(contactClosed, "contact.open", trafficCheck)
    }
    log.debug "installed with settings: $settings"
}

def trafficCheck(evt){
	if(allOk){
		if(state.travelTimeTraffic){
            int timeLeft = getTimeLeft()
            if(timeLeft <= 0){
            	if(state.notifyNow != "true"){
                def timeLeftFixed = -1 * timeLeft
                def msg = "Attention: With current traffic conditions you will be ${timeLeftFixed} minutes late for work."
                    if(sonos){
                        if(resumePlaying){
                            loadText(msg)
                            sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
                        }
                        else{
                            sonos.playText(msg)
                        }    
                    }
                    if(sendPushMessage == "Yes"){
                        sendPush(msg)
                    }    
                    state.check = null
                    state.notify = null
                    state.notifyWarn = null
                    state.notifyEmergency = null
                    state.notifyNow = "true"
                    if(hues){
                        sendcolor(colorEmergency)
                    }
                    if(state.trafficCheck != true){
                        runEvery5Minutes(trafficCheck)
                        state.trafficCheck = true
                    }
            	}       
            }
            else if(timeLeft <= notifyLeadEmergency){
                def msg = "You have ${timeLeft} minutes until you need to leave for work"
                if (state.notifyEmergency != "true"){
                    if(sonos){
                        if(resumePlaying){
                            loadText(msg)
                            sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
                        }
                        else{
                            sonos.playText(msg)
                        }    
                    }
                    if(sendPushMessage == "Yes"){
                        sendPush(msg)
                    }
                    state.check = null
                    state.notify = null
                    state.notifyWarn = null
                    state.notifyNow = null
                    state.notifyEmergency = "true"
                    if(hues){
                        sendcolor(colorEmergency)
                    }
                    if(state.trafficCheck != true){
                        runEvery5Minutes(trafficCheck)
                        state.trafficCheck = true
                    } 
                }
            }
            else if(timeLeft <= notifyLeadWarn){
                def msg = "You have ${timeLeft} minutes until you need to leave for work"
                if (state.notifyWarn != "true"){
                    if(sonos){
                        if(resumePlaying){
                            loadText(msg)
                            sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
                        }
                        else{
                            sonos.playText(msg)
                        }    
                    }
                    if(sendPushMessage == "Yes"){
                        sendPush(msg)
                    }
                    state.check = null
                    state.notify = null
                    state.notifyNow = null
                    state.notifyWarn = "true"
                    state.notifyEmergency = null
                    if(hues){
                        sendcolor(colorWarn)
                    }
                    if(state.trafficCheck != true){
                        runEvery5Minutes(trafficCheck)
                        state.trafficCheck = true
                    } 
                }
            }
            else if(timeLeft <= notifyLead){
                def msg = "You have ${timeLeft} minutes until you need to leave for work"
                if (state.notify != "true"){
                    if(sonos){
                        if(resumePlaying){
                            loadText(msg)
                            sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
                        }
                        else{
                            sonos.playText(msg)
                        }    
                    }
                    if(sendPushMessage == "Yes"){
                        sendPush(msg)
                    }
                    state.check = null
                    state.notify = "true"
                    state.notifyWarn = null
                    state.notifyNow = null
                    state.notifyEmergency = null
                    if(hues){
                        sendcolor(colorNotify)
                    }
                    if(state.trafficCheck != true){
                        runEvery5Minutes(trafficCheck)
                        state.trafficCheck = true
                    } 
                }
            }
            else if((state.notify == "true" || state.notifyWarn == "true" || state.notifyEmergency == "true") && state.check != "true"){
                def msg = "Traffic conditions seem to have improved.  You now have ${timeLeft} minutes to leave for work."
                if(sonos){
                        if(resumePlaying){
                            loadText(msg)
                            sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
                        }
                        else{
                            sonos.playText(msg)
                        }    
                }
                if(sendPushMessage == "Yes"){
                        sendPush(msg)
                }    
                state.check = "true"
                state.notify = null
                state.notifyWarn = null
                state.notifyNow = null
                state.notifyEmergency = null
                sendcolor(colorNormal)
                if(hues){
                    hues.off([delay:5000])
                }
                if(state.trafficCheck != true){
                    runEvery5Minutes(trafficCheck)
                    state.trafficCheck = true
                } 
            }    
            else{
                if (state.check != "greeting"){
                def msg = "Good morning.  You have ${timeLeft} minutes to leave for work."
                if(sonos){
                        if(resumePlaying){
                            loadText(msg)
                            sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
                        }
                        else{
                            sonos.playText(msg)
                        }    
                }
                if(sendPushMessage == "Yes"){
                        sendPush(msg)
                    }
                state.check = "greeting"
                state.notify = null
                state.notifyWarn = null
                state.notifyEmergency = null
                state.notifyNow = null
                if(state.trafficCheck != true){
                    runEvery5Minutes(trafficCheck)
                    state.trafficCheck = true
                }    
                }
            }
        }
        else{
    		log.debug "I do not have a travel time so I will check again in 5 minutes."
        	if(state.trafficCheck != true){
        		runEvery5Minutes(trafficCheck)
        		state.trafficCheck = true
        	}
        }    
	}
    else{
    	unschedule(trafficCheck)
    	state.clear()       
    }    
}

private getTimeLeft(){
	def location1Fixed = location1.replaceAll(" ", "%20")
	def location2Fixed = location2.replaceAll(" ", "%20")
	def result = ""
	try {
			httpGet("http://dev.virtualearth.net/REST/v1/Routes?wayPoint.1=${location1Fixed}&waypoint.2=${location2Fixed}&key=${apiKey}") { resp ->
            	resp.headers.each {
        			log.debug "${it.name} : ${it.value}"
            
    		}
    		log.debug "response contentType: ${resp.contentType}"
        	def totalTime = resp.data.resourceSets.resources.travelDurationTraffic as String
        	def totalTimeFixed = totalTime.replaceAll("\\[", "").replaceAll("\\]","") as Double
        	def travelTimeMinutes = (totalTimeFixed / 60) as Double
        	def travelTimeMinutesRounded = travelTimeMinutes.round(0)
			state.travelTimeTraffic = travelTimeMinutesRounded as Integer
        	log.info "Travel time with traffic = ${state.travelTimeTraffic}"
     	}
   	}
    catch (e) {
		log.error "HTTP Error: ${e}"
	}
    
	def getTime = timeToday(mytime)    
	def timeTillArrival = getTime.time - now()
	def timeTillArrivalMinutes = (timeTillArrival / 60000) as Double
	def timeTillArrivalMinutesRounded = timeTillArrivalMinutes.round() as Double
	log.info"Time until event = ${timeTillArrivalMinutesRounded}"
	def timeLeft = timeTillArrivalMinutesRounded - state.travelTimeTraffic
	log.info"Time Left = ${timeLeft}"
	result = timeLeft

result
}

def totalTravelTime(evt){
	getTimeLeft()
}
def sendcolor(color) {
	log.debug "Sendcolor = $color"
    def hueColor = 0
    def saturation = 100

	switch(color) {
		case "White":
			hueColor = 52
			saturation = 19
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 //83
			break;
		case "Blue":
			hueColor = 70
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			break;
		case "Orange":
			hueColor = 10
			break;
		case "Purple":
			hueColor = 75
			break;
		case "Pink":
			hueColor = 83
			break;
		case "Red":
			hueColor = 100
			break;
	}

	state.previous = [:]

	hues.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation")
           
		]
	}
	
	log.debug "current values = $state.previous"
    
    
    
  	def lightLevel = 60
    if (brightnessLevel != null) {
    	lightLevel = brightnessLevel 
    }
     
	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel]  
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
}



/* Song selection isn't working for some reason.  Will revisit.
private songOptions() {

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

private saveSelectedSong() {
	try {
		def thisSong = song
		log.info "Looking for $thisSong"
		def songs = sonos.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"

		def data = songs.find {s -> s.station == thisSong}
		log.info "Found ${data?.station}"
		if (data) {
			state.selectedSong = data
			log.debug "Selected song = $state.selectedSong"
		}
		else if (song == state.selectedSong?.station) {
			log.debug "Selected existing entry '$song', which is no longer in the last 20 list"
		}
		else {
			log.warn "Selected song '$song' not found"
		}
	}
	catch (Throwable t) {
		log.error t
	}
}
*/
private loadText(msg) {
		log.debug "msg = ${msg}"
		state.sound = textToSpeech(msg, true)
}

def greyOutApi(){
	def result = ""
    if (apiKey) {
    	result = "complete"	
    }
    result
}

def greyOutWayPoints(){
	def result = ""
    if (location1 && location2) {
    	result = "complete"	
    }
    result
}

def greyOutTriggers(){
	def result = ""
    if (motions || contactClosed || contactOpen) {
    	result = "complete"	
    }
    result
}

def greyOutTimes(){
	def result = ""
    if (mytime && notifyLead && notifyLeadWarn && notifyLeadEmergency) {
    	result = "complete"	
    }
    result
}

def greyOutNotifications(){
	def result = ""
    if (sendPushMessage || sonos || hues) {
    	result = "complete"	
    }
    result
}

def greyOutRestrictions(){
	def result = ""
    if (modes || people) {
    	result = "complete"	
    }
    result
}

def greyOutTimeLabel(){
	def result = ""
    if(starting || ending){
    	result = true
    }
    result
}

def triggerDescription(){
	def result = ""
    result = "Motion: ${motions}"  + "\n" + "Contact Open: ${contactOpen}"  + "\n" + "Contact Closed: ${contactClosed}"
}

def waypointDescription(){
	def result = ""
    if(location1 && location2){
    	result = "Calculate times between"  + "\n" + "${location1}"  + "\n" + "and ${location2}."
    }
    else if(location1){
    	result = "Destination address not set"
    }
    else if(location2){
    	result = "Starting address not set"
    }
    else{
    	result = "Tap to set"
    }
    result
}

def notificationsDescription(){
	def result = ""
    result = 	"Send Push: ${sendPushMessage}" + "\n" + "Sonos: ${sonos}"  + "\n" + "Hues: ${hues}"

}

def apiDescription(){
	def result = ""
    def length = apiKey.length()
    if (length == 64){
    	result = "Valid API Key"
	}
    else if (length != 0){
    	result = "Invalid API key"
    }
    else{
    	result = "Tap to enter API key"
    }    
}

def restrictionsDesription(){
	def result = ""
    def daysRest = ""
    def modesBracket = ""
    def peopleBracket = ""
    def timeRest = ""
    def modesFixed = "No Mode Restrictions"
    def daysFixed = "No Day Restrictions"
    def peopleFixed = "No Presence Restrictions"
    if (days){
    	daysRest = "$days"
        daysFixed = daysRest.replaceAll("\\[", "").replaceAll("\\]","")        
    }
    if(modes != null){
    	modesBracket = "${modes}"
        modesFixed = modesBracket.replaceAll("\\[", "").replaceAll("\\]","")
    }
    if(starting || ending){
    	timeRest = getTimeLabel()
    }
    if(people){
    	peopleBracket = "$people"
        peopleFixed = peopleBracket.replaceAll("\\[", "").replaceAll("\\]","")
    }
    result = "Days: ${daysFixed}" + "\n" + "Modes: ${modesFixed}" + "\n" + "Time: ${getTimeLabel()}" + "\n" + "Presence: ${peopleFixed}"
}

def travelParagraph(){
	def timeTravel = state.travelTimeTraffic as Integer
	def result = "Total travel time with traffic is $timeTravel minutes."
    return result
}

private getAllOk() {
	modeOk && daysOk && timeOk && peopleOk
}

private getModeOk(){
	def result = false
	if(modes.contains(location.mode)){
    	result = true
    }
    log.trace "modeOk: $result"
    return result	
}

private getPeopleOk() {
  def result = false

  if(people.findAll { it?.currentPresence == "present" }) {
result = true
  }

  log.debug("anyoneIsHome: ${result}")

  return result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
    
    else if (starting){
    	result = currTime >= start
    }
    else if (ending){
    	result = currTime <= stop
    }
    
	log.trace "timeOk = $result"
	result
}

private getTimeLabel(){
	def timeLabel = "No Time restrictions"
	
    if(starting && ending){
    	timeLabel = "Between" + " " + hhmm(starting) + " "  + "and" + " " +  hhmm(ending)
    }
    else if (starting) {
		timeLabel = "Start at" + " " + hhmm(starting)
    }
    else if(ending){
    timeLabel = "End at" + hhmm(ending)
    }
	timeLabel	
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private getDayOk(dayList) {
	def result = true
    if (dayList) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = dayList.contains(day)
	}
    result
}