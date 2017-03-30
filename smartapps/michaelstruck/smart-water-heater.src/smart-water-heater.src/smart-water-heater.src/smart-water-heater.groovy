/**
 *  Smart Water Heater
 *  Version 1.5.0 7/27/2015
 *
 *  Version 1.0.1-Initial release
 *  Version 1.1 added a function to turn water heater back on if someone comes home early
 *  Version 1.1.1 Revised the interface for better flow
 *  Version 1.2 Revised the interface even more for better flow
 *  Version 1.2.1 Further interface revision
 *  Version 1.3 Added the option to turn off the water heater early if everyone leaves before the scheduled time and code opimization
 *  Version 1.3.1 Added About screen
 *  Version 1.3.2 Added verification of status being off to eliminate redundent commands to the switch and some code optimization 
 *  Version 1.4.0 Added a day-of-week filter instead of simply stating 'weekend' and allowed for multiple water heaters
 *  Version 1.5.0 Fixed various GUI issues and bugs that affect first time installers 
 *
 *  Copyright 2015 Michael Struck
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
definition(
    name: "Smart Water Heater",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Allows for setting up schedules for turning on and off the power to the water heaters. ",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Water-Heater/WHApp.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Water-Heater/WHApp@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Water-Heater/WHApp@2x.png")


preferences {
	page(name: "mainPage")
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
    	section("Select water heater switches..."){
			input "switchWH", title: "Water Heater Switches", "capability.switch", multiple: true
		}
    	section("Daytime Options"){
        	href(name: "toDaySchedule", page: "daySchedule", title: "Schedule", description: dayDescription(), state: "complete")
        	if (timeOffDay && timeOnDay){
            	input "presence1", "capability.presenceSensor", title: "Remain on if any of these people are present", multiple: true, required: false, submitOnChange:true
                if (presence1){
                	input "exceptionLeave", "bool", title: "Turn off when everyone leaves before ${hhmm(timeOffDay)}", defaultValue: "true"
            		input "exceptionArrive", "bool", title: "Turn on when anyone arrives before ${hhmm(timeOnDay)} ", defaultValue: "true"
                }
			}
        }
    	section("Nighttime Schedule"){
    		href(name: "toNightSchedule", page: "nightSchedule", title: "Schedule", description: nightDescription(), state: "complete")
    	}
        section([mobileOnly:true], "Other Options") {
			label(title: "Assign a name", required: false)
            mode title: "Set for specific mode(s)", required: false
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
        }
    }
}

page(name: "daySchedule", title: "Daytime Schedule") {
	section {
    	input "timeOffDay", title: "Time to turn off", "time"
        input "timeOnDay", title: "Time to turn back on", "time"
        input "dayOff", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Run on certain days of the week...", multiple: true, required: false
	}
}


page(name: "nightSchedule", title: "Nighttime Schedule") {
	section {
		input "timeOffNight", title: "Time to turn off", "time", required: false
        input "timeOnNight", title: "Time to turn back on", "time", required: false
        input "nightOff", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Run on certain days of the week...", multiple: true, required: false
	}
}


page(name: "pageAbout", title: "About ${textAppName()}") {
        section {
            paragraph "${textVersion()}\n${textCopyright()}\n\n${textLicense()}\n"
        }
        section("Instructions") {
            paragraph textHelp()
        }
}


//---------------------------------------------------

def installed() {
	log.debug "Installed with settings: ${settings}"
	init()
}

def updated() {
	unsubscribe()
	unschedule()
    log.debug "Updated with settings: ${settings}"
    init()
}

def init () {
    if (timeOffDay && timeOnDay){
    	schedule(timeOffDay, "turnOffDay")
		schedule(timeOnDay, "turnOnDay")
        if (presence1 && (exceptionArrive || exceptionLeave)){
    		subscribe(presence1, "presence", presenceHandler)
    	}
    }
    if (timeOffNight && timeOnNight){
    	schedule(timeOffNight, "turnOffNight")
		schedule(timeOnNight, "turnOnNight")
	}
}

def turnOffDay() {
    if (getDayOk(dayOff) && everyoneGone() && state.status !="Day off") {
    	state.status="Day off"
        turnOffSwitch()
   	} else {
		log.debug "Day restriction or presense is detected so the water heater(s) will remain on."
    }    
}

def turnOnDay() {
	state.status="Day on"
    turnOnSwitch()
}

def turnOnNight() {
   	state.status="Night on"
	turnOnSwitch()
}

def turnOffNight() {
	if (getDayOk(nightOff)) {
    	state.status="Night off"
    	turnOffSwitch()
    }
    else {
    	log.debug "Day restriction so the water heater(s) will remain on."
    }
}

def turnOffSwitch() {
    	switchWH?.off()
        log.debug "Water heater(s) turned off."
}
    
def turnOnSwitch() {
    	switchWH?.on()
        log.debug "Water heater(s) turned on."
}

def presenceHandler(evt) {
    if (!everyoneGone() && state.status=="Day off"){
    	log.debug "Presence detected, turning water heater(s) back on"
    	turnOnDay()
    }

	if (everyoneGone() && checkTime() && getDayOk(dayOff)){
    	log.debug "Everyone has left early, turning water heater(s) off"
		turnOffDay()
    }
}

private everyoneGone() {
    def result = presence1.find{it.currentPresence == "present"} ? false : true
	result
}

def nightDescription() {
	def title = "Tap to set night schedule (optional)"
    if (timeOffNight) {
    	title = "Turn off at ${hhmm(timeOffNight)} then turn back on at ${hhmm(timeOnNight)}"
    	def dayListSize = nightOff ? nightOff.size() : 7
    	if (nightOff && dayListSize < 7) {
        	title += " on"
            for (dayName in nightOff) {
 				title += " ${dayName}"
    			nightListSize = nightListSize -1
                if (nightListSize) {
            		title += ", "
        		}
        	}
        }
        else {
    		title += "\nevery day"
    	}
    }
    title
}

def dayDescription() {
	def title = "Tap to set day schedule (required)"
	if (timeOffDay) {
    	title = "Turn off at ${hhmm(timeOffDay)} then turn back on at ${hhmm(timeOnDay)}"
    	def dayListSize = dayOff ? dayOff.size() : 7
    	if (dayOff && dayListSize < 7) {
        	title += " on"
            for (dayName in dayOff) {
 				title += " ${dayName}"
    			dayListSize = dayListSize -1
                if (dayListSize) {
            		title += ", "
        		}
        	}
        }
        else {
    		title += "\nevery day"
    	}
	}
    title
}
//Common Methods

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public hhmm(dateTxt) {
	new Date().parse(smartThingsDateFormat(), dateTxt).format("h:mm a", timeZone(dateTxt))
}

private getDayOk(dayList) {
	def result = true
	if (dayList) {
		result = dayList.contains(getDay())
	}
	result
}

private getDay(){
	def df = new java.text.SimpleDateFormat("EEEE")
	if (location.timeZone) {
		df.setTimeZone(location.timeZone)
	}
	else {
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	}
	def day = df.format(new Date())
}

private checkTime() {
	def result = true
	if (timeOffDay) {
		def currTime = now()
		def start = timeToday(timeOffDay).time
		result = currTime < start
	}
	result
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Smart Water Heater"
}	

private def textVersion() {
    def text = "Version 1.5.0 (07/27/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2015 Michael Struck"
}

private def textLicense() {
    def text =
		"Licensed under the Apache License, Version 2.0 (the 'License'); "+
		"you may not use this file except in compliance with the License. "+
		"You may obtain a copy of the License at"+
		"\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0"+
		"\n\n"+
		"Unless required by applicable law or agreed to in writing, software "+
		"distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
		"See the License for the specific language governing permissions and "+
		"limitations under the License."
}

private def textHelp() {
	def text =
    	"Choose the day and night schedule in which the water heaters' power is turned " +
        "on and off. For the daytime schedule, you have various options to determine whether to turn the water heaters or or off " +
        "based on the status of presence sensors."
}


