/**
 *	Smart Home Ventilation
 *	Version 2.1.2 - 5/31/15
 *
 *	Copyright 2015 Michael Struck
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
	name: "Smart Home Ventilation",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Allows for setting up various schedule scenarios for turning on and off home ventilation switches.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Home-Ventilation/HomeVent.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Home-Ventilation/HomeVent@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Home-Ventilation/HomeVent@2x.png")

preferences {
	page name: "mainPage"
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
     	section("Select ventilation switches..."){
			input "switches", title: "Switches", "capability.switch", multiple: true
		}
        section ("Scheduling scenarios...") {
        	href(name: "toA_Scenario", page: "A_Scenario", title: getTitle (titleA, "A"), description: schedDesc(timeOnA1,timeOffA1,timeOnA2,timeOffA2,timeOnA3,timeOffA3,timeOnA4,timeOffA4, modeA, daysA), state: greyOut(timeOnA1,timeOnA2,timeOnA3,timeOnA4))
        	href(name: "toB_Scenario", page: "B_Scenario", title: getTitle (titleB, "B"), description: schedDesc(timeOnB1,timeOffB1,timeOnB2,timeOffB2,timeOnB3,timeOffB3,timeOnB4,timeOffB4, modeB, daysB), state: greyOut(timeOnB1,timeOnB2,timeOnB3,timeOnB4))
        	href(name: "toC_Scenario", page: "C_Scenario", title: getTitle (titleC, "C"), description: schedDesc(timeOnC1,timeOffC1,timeOnC2,timeOffC2,timeOnC3,timeOffC3,timeOnC4,timeOffC4, modeC, daysC), state: greyOut(timeOnC1,timeOnC2,timeOnC3,timeOnC4))
        	href(name: "toD_Scenario", page: "D_Scenario", title: getTitle (titleD, "D"), description: schedDesc(timeOnD1,timeOffD1,timeOnD2,timeOffD2,timeOnD3,timeOffD3,timeOnD4,timeOffD4, modeD, daysD), state: greyOut(timeOnD1,timeOnD2,timeOnD3,timeOnD4))
        }
        section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false, defaultValue: "Smart Home Ventilation")
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
        }
    }
}
//----Scheduling Pages
page(name: "A_Scenario", title: getTitle (titleA, "A")) {
    	section{
			input "timeOnA1", title: "Schedule 1 time to turn on", "time", required: false
        	input "timeOffA1", title: "Schedule 1 time to turn off", "time", required: false
		}
    	section{
			input "timeOnA2", title: "Schedule 2 time to turn on", "time", required: false
        	input "timeOffA2", title: "Schedule 2 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnA3", title: "Schedule 3 time to turn on", "time", required: false
        	input "timeOffA3", title: "Schedule 3 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnA4", title: "Schedule 4 time to turn on", "time", required: false
        	input "timeOffA4", title: "Schedule 4 time to turn off", "time", required: false
		}
		section ("Options") {
    		input "titleA", title: "Assign a scenario name", "text", required: false
            input "modeA", "mode", required: false, multiple: true, title: "Run in specific mode(s)", description: "Choose Modes"
		   	input "daysA", "enum", multiple: true, title: "Run on specific day(s)", description: "Choose Days", required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
		}
    }

page(name: "B_Scenario", title: getTitle (titleB, "B")) {
    	section{
			input "timeOnB1", title: "Schedule 1 time to turn on", "time", required: false
        	input "timeOffB1", title: "Schedule 1 time to turn off", "time", required: false
		}
    	section{
			input "timeOnB2", title: "Schedule 2 time to turn on", "time", required: false
        	input "timeOffB2", title: "Schedule 2 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnB3", title: "Schedule 3 time to turn on", "time", required: false
        	input "timeOffB3", title: "Schedule 3 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnB4", title: "Schedule 4 time to turn on", "time", required: false
        	input "timeOffB4", title: "Schedule 4 time to turn off", "time", required: false
		}
		section("Options") {
    		input "titleB", title: "Assign a scenario name", "text", required: false
            input "modeB", "mode", required: false, multiple: true, title: "Run in specific mode(s)", description: "Choose Modes"
		   	input "daysB", "enum", multiple: true, title: "Run on specific day(s)", description: "Choose Days", required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
		}
    }

page(name: "C_Scenario", title: getTitle (titleC, "C")) {
    	section{
			input "timeOnC1", title: "Schedule 1 time to turn on", "time", required: false
        	input "timeOffC1", title: "Schedule 1 time to turn off", "time", required: false
		}
    	section{
			input "timeOnC2", title: "Schedule 2 time to turn on", "time", required: false
        	input "timeOffC2", title: "Schedule 2 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnC3", title: "Schedule 3 time to turn on", "time", required: false
        	input "timeOffC3", title: "Schedule 3 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnC4", title: "Schedule 4 time to turn on", "time", required: false
        	input "timeOffC4", title: "Schedule 4 time to turn off", "time", required: false
		}
		section("Options") {
    		input "titleC", title: "Assign a scenario name", "text", required: false
            input "modeC", "mode", required: false, multiple: true, title: "Run in specific mode(s)", description: "Choose Modes"
		   	input "daysC", "enum", multiple: true, title: "Run on specific day(s)", description: "Choose Days", required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
		}
    }


page(name: "D_Scenario", title: getTitle (titleD, "D")) {
    	section{
			input "timeOnD1", title: "Schedule 1 time to turn on", "time", required: false
        	input "timeOffD1", title: "Schedule 1 time to turn off", "time", required: false
		}
    	section{
			input "timeOnD2", title: "Schedule 2 time to turn on", "time", required: false
        	input "timeOffD2", title: "Schedule 2 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnD3", title: "Schedule 3 time to turn on", "time", required: false
        	input "timeOffD3", title: "Schedule 3 time to turn off", "time", required: false
		}
    	section{
        	input "timeOnD4", title: "Schedule 4 time to turn on", "time", required: false
        	input "timeOffD4", title: "Schedule 4 time to turn off", "time", required: false
		}
        section("Options") {
    		input "titleD", title: "Assign a scenario name", "text", required: false
            input "modeD", "mode", required: false, multiple: true, title: "Run in specific mode(s)", description: "Choose Modes"
		   	input "daysD", "enum", multiple: true, title: "Run on specific day(s)", description: "Choose Days", required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
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

// Install and initiate

def installed() {
    log.debug "Installed with settings: ${settings}"
    init()
}

def updated() {
    unschedule()
    turnOffSwitch() //Turn off all switches if the schedules are changed while in mid-schedule
    unsubscribe()
    log.debug "Updated with settings: ${settings}"
    init()
}

def init() {
	def midnightTime = timeToday("2000-01-01T00:01:00.999-0000", location.timeZone)
    schedule (midnightTime, midNight)
	subscribe(location, "mode", locationHandler)
    startProcess()
}

// Common methods

def startProcess () {
    createDayArray()
	state.dayCount=state.data.size()
    if (state.dayCount){
		state.counter = 0
        startDay()
    }
}

def startDay() {
	def start = convertEpoch(state.data[state.counter].start)
	def stop = convertEpoch(state.data[state.counter].stop)

    runOnce(start, turnOnSwitch, [overwrite: true])
    runOnce(stop, incDay, [overwrite: true])
}

def incDay() {
    turnOffSwitch()
    if (state.modeChange) {
    	startProcess()
    }
    else {
    	state.counter = state.counter + 1
    	if (state.counter < state.dayCount) {
    		startDay()
    	}
    }
}

def locationHandler(evt) {
	def result = false
    state.modeChange = true
    switches.each {
    	if (it.currentValue("switch")=="on"){
           result = true
        }
    }
	if (!result) {
    	startProcess()
    }
}

def midNight(){
    startProcess()
}

def turnOnSwitch() {
    switches.on()
    log.debug "Home ventilation switches are on."
}

def turnOffSwitch() {
    switches.each {
    	if (it.currentValue("switch")=="on"){
			it.off()
        }
    }
    log.debug "Home ventilation switches are off."
}

def schedDesc(on1, off1, on2, off2, on3, off3, on4, off4, modeList, dayList) {
	def title = ""
	def dayListClean = "On "
    def modeListClean ="Scenario runs in "
    if (dayList && dayList.size() < 7) {
    	def dayListSize = dayList.size()
        for (dayName in dayList) {
        	dayListClean = "${dayListClean}"+"${dayName}"
    		dayListSize = dayListSize -1
            if (dayListSize) {
            	dayListClean = "${dayListClean}, "
            }
        }
	}
    else {
    	dayListClean = "Every day"
    }
    if (modeList) {
    	def modeListSize = modeList.size()
        def modePrefix ="modes"
        if (modeListSize == 1) {
        	modePrefix = "mode"
        }
        for (modeName in modeList) {
        	modeListClean = "${modeListClean}"+"'${modeName}'"
    		modeListSize = modeListSize -1
            if (modeListSize) {
            	modeListClean = "${modeListClean}, "
            }
            else {
            	modeListClean = "${modeListClean} ${modePrefix}"
        	}
        }
	}
    else {
    	modeListClean = "${modeListClean}all modes"
    }
    if (on1 && off1){
    	title += "Schedule 1: ${humanReadableTime(on1)} to ${humanReadableTime(off1)}"
    }
    if (on2 && off2) {
    	title += "\nSchedule 2: ${humanReadableTime(on2)} to ${humanReadableTime(off2)}"
    }
    if (on3 && off3) {
    	title += "\nSchedule 3: ${humanReadableTime(on3)} to ${humanReadableTime(off3)}"
    }
    if (on4 && off4) {
    	title += "\nSchedule 4: ${humanReadableTime(on4)} to ${humanReadableTime(off4)}"
    }
    if (on1 || on2 || on3 || on4) {
    	title += "\n$modeListClean"
    	title += "\n$dayListClean"
    }

    if (!on1 && !on2 && !on3 && !on4) {
    	title="Click to configure scenario"
    }
    title
}

def greyOut(on1, on2, on3, on4){
    def result = on1 || on2 || on3 || on4 ? "complete" : ""
}

public humanReadableTime(dateTxt) {
	new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", dateTxt).format("h:mm a", timeZone(dateTxt))
}

public convertEpoch(epochDate) {
    new Date(epochDate).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
}

private getTitle(txt, scenario) {
    def title = txt ? txt : "Scenario ${scenario}"
}

private daysOk(dayList) {
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

private timeOk(starting, ending) {
    if (starting && ending) {
        def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
        if (start < stop && start >= currTime && stop>=currTime) {
        	state.data << [start:start, stop:stop]
        }
    }
}

def createDayArray() {
	state.modeChange = false
    state.data = []
    if (modeA && modeA.contains(location.mode)) {
        if (daysOk(daysA)){
            timeOk(timeOnA1, timeOffA1)
			timeOk(timeOnA2, timeOffA2)
			timeOk(timeOnA3, timeOffA3)
			timeOk(timeOnA4, timeOffA4)
        }
    }
    if (modeB && modeB.contains(location.mode)) {
        if (daysOk(daysB)){
			timeOk(timeOnB1, timeOffB1)
            timeOk(timeOnB2, timeOffB2)
            timeOk(timeOnB3, timeOffB3)
            timeOk(timeOnB4, timeOffB4)
        }
    }
    if (modeC && modeC.contains(location.mode)) {
        if (daysOk(daysC)){
            timeOk(timeOnC1, timeOffC1)
            timeOk(timeOnC2, timeOffC2)
            timeOk(timeOnC3, timeOffC3)
            timeOk(timeOnC4, timeOffC4)
        }
    }
    if (modeD && modeD.contains(location.mode)) {
        if (daysOk(daysD)){
           timeOk(timeOnD1, timeOffD1)
           timeOk(timeOnD2, timeOffD2)
           timeOk(timeOnD3, timeOffD3)
           timeOk(timeOnD4, timeOffD4)
        }
    }
    state.data.sort{it.start}
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Smart Home Ventilation"
}

private def textVersion() {
    def text = "Version 2.1.2 (05/31/2015)"
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
    	"Within each scenario, choose a start and end time for the ventilation fan. You can have up to 4 different " +
        "venting scenarios, and 4 schedules within each scenario. Each scenario can be restricted to specific modes or certain days of the week. It is recommended "+
        "that each scenario does not overlap and run in separate modes (i.e. Home, Out of town, etc). Also note that you should  " +
        "avoid scheduling the ventilation fan at exactly midnight; the app resets itself at that time. It is suggested to start any new schedule " +
        "at 12:15 am or later."
}
