/**
 *  Vacation Lighting Director
 * 
 * Version  2.5 - Moved scheduling over to Cron and added time as a trigger. 
 *				  Cleaned up formatting and some typos.
 *                Updated license.
 *                Made people option optional
 * 				  Added sttement to unschedule on mode change if people option is not selected
 *
 * Version  2.4 - Added information paragraphs
 * 
 *  Source code can be found here: https://github.com/tslagle13/SmartThings/blob/master/smartapps/tslagle13/vacation-lighting-director.groovy
 *
 *  Copyright 2016 Tim Slagle
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


// Automatically generated. Make future change here.
definition(
    name: "Vacation Lighting Director",
    namespace: "tslagle13",
    author: "Tim Slagle",
    category: "Safety & Security",
    description: "Randomly turn on/off lights to simulate the appearance of a occupied home while you are away.",
    iconUrl: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png",
    iconX2Url: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png"
)

preferences {
    page name:"pageSetup"
    page name:"Setup"
    page name:"Settings"
    page name: "timeIntervalInput"

}

// Show setup page
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

	return dynamicPage(pageProperties) {
    	section(""){
        	paragraph "This app can be used to make your home seem occupied anytime you are away from your home. " +
			"Please use each of the the sections below to setup the different preferences to your liking. " 
        }
        section("Setup Menu") {
            href "Setup", title: "Setup", description: "", state:greyedOut()
            href "Settings", title: "Settings", description: "", state: greyedOutSettings()
            }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

// Show "Setup" page
def Setup() {

    def newMode = [
        name:       	"newMode",
        type:       	"mode",
        title:      	"Modes",
        multiple:   	true,
        required:   	true
    ]
    def switches = [
        name:       	"switches",
        type:       	"capability.switch",
        title:      	"Switches",
        multiple:   	true,
        required:   	true
    ]
    
    def frequency_minutes = [
        name:       	"frequency_minutes",
        type:       	"number",
        title:      	"Minutes?",
        required:	true
    ]
    
    def number_of_active_lights = [
        name:       	"number_of_active_lights",
        type:       	"number",
        title:      	"Number of active lights",
        required:	true,
    ]
    
    def pageName = "Setup"
    
    def pageProperties = [
        name:       "Setup",
        title:      "Setup",
        nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {

		section(""){            
                    paragraph "In this section you need to setup the deatils of how you want your lighting to be affected while " +
                    "you are away.  All of these settings are required in order for the simulator to run correctly."
        }
        section("Simulator Triggers") {
                    input newMode  
                    href "timeIntervalInput", title: "Times", description: timeIntervalLabel(), refreshAfterSelection:true
        }
        section("Light switches to turn on/off") {
                    input switches           
        }
        section("How often to cycle the lights") {
                    input frequency_minutes            
        }
        section("Number of active lights at any given time") {
                    input number_of_active_lights           
        }    
    }
    
}

// Show "Setup" page
def Settings() {

    def falseAlarmThreshold = [
        name:       "falseAlarmThreshold",
        type:       "decimal",
        title:      "Default is 2 minutes",
        required:	false
    ]
    def days = [
        name:       "days",
        type:       "enum",
        title:      "Only on certain days of the week",
        multiple:   true,
        required:   false,
        options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    ]
    
    def pageName = "Settings"
    
    def pageProperties = [
        name:       "Settings",
        title:      "Settings",
        nextPage:   "pageSetup"
    ]
    
    def people = [
        name:       "people",
        type:       "capability.presenceSensor",
        title:      "If these people are home do not change light status",
        required:	false,
        multiple:	true
    ]

    return dynamicPage(pageProperties) {

		section(""){              
                    paragraph "In this section you can restrict how your simulator runs.  For instance you can restrict on which days it will run " +
                    "as well as a delay for the simulator to start after it is in the correct mode.  Delaying the simulator helps with false starts based on a incorrect mode change."
        }
        section("Delay to start simulator") {
                    input falseAlarmThreshold
        }
        section("People") {
        			paragraph "Not using this setting may cause some lights to remain on when you arrive home"
                    input people            
        }
        section("More options") {
                    input days
        } 
    }   
}

def timeIntervalInput() {
	dynamicPage(name: "timeIntervalInput") {
		section {
			input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
			if (startTimeType in ["sunrise","sunset"]) {
				input "startTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "starting", "time", title: "Start time", required: false
			}
		}
		section {
			input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
			if (endTimeType in ["sunrise","sunset"]) {
				input "endTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "ending", "time", title: "End time", required: false
			}
		}
	}
}


def installed() {
initialize()
}

def updated() {
  unsubscribe();
  unschedule();
  initialize()
}

def initialize(){

	if (newMode != null) {
		subscribe(location, modeChangeHandler)
    }
    if (starting != null) {
    	schedule(starting, modeChangeHandler)
    }
    log.debug "Installed with settings: ${settings}"
}

def modeChangeHandler(evt) {
		def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 2 * 60  
    	runIn(delay, scheduleCheck)
}


//Main logic to pick a random set of lights from the large set of lights to turn on and then turn the rest off
def scheduleCheck(evt) {
    if(allOk){
        log.debug("Running")
        // turn off all the switches
        switches.off()
        
        // grab a random switch
        def random = new Random()
        def inactive_switches = switches
        for (int i = 0 ; i < number_of_active_lights ; i++) {
            // if there are no inactive switches to turn on then let's break
            if (inactive_switches.size() == 0){
                break
            }
            
            // grab a random switch and turn it on
            def random_int = random.nextInt(inactive_switches.size())
            inactive_switches[random_int].on()
            
            // then remove that switch from the pool off switches that can be turned on
            inactive_switches.remove(random_int)
        }
        
        // re-run again when the frequency demands it
        schedule("0 0/${frequency_minutes} * 1/1 * ? *", scheduleCheck)
    }
    //Check to see if mode is ok but not time/day.  If mode is still ok, check again after frequency period.
    else if (modeOk) {
        log.debug("mode OK.  Running again")
        switches.off()
    }
    //if none is ok turn off frequency check and turn off lights.
    else {
    	if(people){
        	//don't turn off lights if anyone is home
        	if(someoneIsHome){
        	    log.debug("Stopping Check for Light")
        	    unschedule()
        	}
        	else{
        	    log.debug("Stopping Check for Light and turning off all lights")
        	    switches.off()
        	    unschedule()
        	}
    	}
        else if (!modeOk) {
        	unschedule()
        }
    }
}      


//below is used to check restrictions
private getAllOk() {
	modeOk && daysOk && timeOk && homeIsEmpty
}


private getModeOk() {
	def result = !newMode || newMode.contains(location.mode)
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

private getHomeIsEmpty() {
  def result = true

  if(people?.findAll { it?.currentPresence == "present" }) {
    result = false
  }

  log.debug("homeIsEmpty: ${result}")

  return result
}

private getSomeoneIsHome() {
  def result = false

  if(people?.findAll { it?.currentPresence == "present" }) {
    result = true
  }

  log.debug("anyoneIsHome: ${result}")

  return result
}

private getTimeOk() {
	def result = true
	def start = timeWindowStart()
	def stop = timeWindowStop()
	if (start && stop && location.timeZone) {
		result = timeOfDayIsBetween(start, stop, new Date(), location.timeZone)
	}
	log.trace "timeOk = $result"
	result
}

private timeWindowStart() {
	def result = null
	if (startTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (startTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (starting && location.timeZone) {
		result = timeToday(starting, location.timeZone)
	}
	log.trace "timeWindowStart = ${result}"
	result
}

private timeWindowStop() {
	def result = null
	if (endTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (endTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (ending && location.timeZone) {
		result = timeToday(ending, location.timeZone)
	}
	log.trace "timeWindowStop = ${result}"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel() {
	def start = ""
	switch (startTimeType) {
		case "time":
			if (ending) {
            	start += hhmm(starting)
            }
			break
		case "sunrise":
		case "sunset":
        	start += startTimeType[0].toUpperCase() + startTimeType[1..-1]
			if (startTimeOffset) {
				start += startTimeOffset > 0 ? "+${startTimeOffset} min" : "${startTimeOffset} min"
			}
			break
	}

    def finish = ""
	switch (endTimeType) {
		case "time":
			if (ending) {
            	finish += hhmm(ending)
            }
			break
		case "sunrise":
		case "sunset":
        	finish += endTimeType[0].toUpperCase() + endTimeType[1..-1]
			if (endTimeOffset) {
				finish += endTimeOffset > 0 ? "+${endTimeOffset} min" : "${endTimeOffset} min"
			}
			break
	}
	start && finish ? "${start} to ${finish}" : ""
}

//sets complete/not complete for the setup section on the main dynamic page
def greyedOut(){
	def result = ""
    if (switches) {
    	result = "complete"	
    }
    result
}

//sets complete/not complete for the settings section on the main dynamic page
def greyedOutSettings(){
	def result = ""
    if (people || days || falseAlarmThreshold ) {
    	result = "complete"	
    }
    result
}
