/**
 *  Programmable Thermostat
 *
 *  Copyright 2016 Raymond Ciarcia
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
    name: "Programmable Thermostat",
    namespace: "smartthings",
    author: "Raymond Ciarcia",
    description: "A full-featured, easy to use interface for programming your thermostat based on schedule setpoints and mode changes",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png"
)

preferences {
	page(name:"Settings", title:"Settings", uninstall:true, install:true ) {
		section() {
			input (name:"thermostat", type: "capability.thermostat", title: "Select thermostat", required: true, multiple: false)
		}
    	section ("Scheduled Setpoints") {
	    	input (name: "son", type: "bool", title: "Run scheduled setpoints", required:true)
			input (name:"numscheduled", title: "Number of scheduled setpoints", type: "number",refreshAfterSelection: true)
			href(title: "Schedule Setpoints", page: "ScheduledChanges")
		}
    	section ("Mode-Based Setpoints") {
			input (name: "eon", type: "bool", title: "Run mode-based setpoints", required:true)
			input (name:"numevent", title: "Number of mode-based setpoints", type: "number",refreshAfterSelection: true)
			href(title: "Mode-Based Setpoints Setpoints", page: "EventBasedChanges")
		}
    	section("Auto Thermostat Mode Control") {
        	input (name: "auto", type: "enum", title: "Adjust thermostat heating/cooling mode based on current temperature and setpoint", required:true, multiple: false, options: ['Never','When setpoints are executed','Any time']) 
		}
    	section("Notifications") {
			input (name: "snotifications", type: "bool", title: "Notify when scheduled setpoints execute", required:true)
            input (name: "enotifications", type: "bool", title: "Notify when mode-based setpoints execute", required:true)
            input (name: "eventlogging", type: "enum", title: "Set the level of event logging in the notification feed", required:true, multiple: false, options: ['None','Normal','Detailed'])
		}
       section("Command Acknowledgement Failure Response and Notification") {
			input (name: "fnotifications", type: "bool", title: "Resend commands not acknowledged by the theromstat and notify after multiple failed attempts.  Increases thermostat reliability but may not be compatible with all thermostats; disable if every command results in a failure notification.", required:true)
       }
    }
    page(name: "ScheduledChanges")
    page(name: "EventBasedChanges")
}

def ScheduledChanges() {
	dynamicPage(name: "ScheduledChanges", uninstall: true, install: false) {
		for (int i = 1; i <= settings.numscheduled; i++) {
			section("Scheduled Setpoint $i") {
				input "stime${i}", "time", title: "At this time:", required: true
       			input "sheatset${i}", "decimal", title: "Set this heating temperature:", required: true
        		input "scoolset${i}", "decimal", title: "Set this cooling temperature:", required: true
        		input "sdays${i}", "enum", title: "Only on these days (no selection is equivalent to selecting all):", required: false, multiple: true, options: ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']
        		input "smodes${i}", "mode", title: "Only in these modes (no selection is equivalent to selecting all):", multiple: true, required: false       
			}
    	}
	}
}

def EventBasedChanges() {
	dynamicPage(name: "EventBasedChanges", uninstall: true, install: false) {
		for (int i = 1; i <= settings.numevent; i++) {
			section("Mode-Based Setpoint $i") {
				input "emodes${i}", "mode", title: "On transition to this mode:", multiple: false, required: true
				input "eheatset${i}", "decimal", title: "Set this heating temperature:", required: true
	    		input "ecoolset${i}", "decimal", title: "Set this cooling temperature:", required: true
                input "edays${i}", "enum", title: "Only on these days (no selection is equivalent to selecting all):", required: false, multiple: true, options: ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']
			}
		}
	}
}


//---- INSTALL AND UPDATE

def installed() {initialize()}

def updated() {initialize()}

def initialize() {
	try {
    	unschedule()
    } catch(e) {
    	try {
       		unschedule(SchedulerIntegrityChecker)
    		unschedule(MidnightRunner)        
        } catch(ev) {}    
    }    
    
    unsubscribe()
    subscribe(settings.thermostat, "temperature", tempChangeHandler)
    if ((settings.numevent > 0) && (settings.eon)) {subscribe(location, modeChangeHandler)}

    state.scheduledindex = 0
    state.pendingindex = 0
	state.dayoflastrun = TodayAsString()
    state.timeoflastevent = now()
    state.nextscheduledtime = now()
    state.failedcommandcount = 0
    state.schedulestring = ""
    state.checkcommandstring = ""
    
    state.eventlogging = 0
    if (settings.eventlogging == "Normal"){state.notificationlevel = 1}
    if (settings.eventlogging == "Detailed"){state.notificationlevel = 2}
    
    if ((settings.numscheduled > 0) && (settings.son)) {
    	schedule(timeToday("2015-08-04T00:00:00.000",location.timeZone), MidnightRunner)
    	SchedulerFunction()
    }
	log.debug "Programmable Thermostat: successfully initialized."
    if (state.notificationlevel>0) {sendNotificationEvent("Programmable Thermostat successfully initialized.$state.schedulestring.")}
	state.schedulestring = ""
}

//---- SCHEDULING FUNCTIONS

//At midnight, runs scheduler function to set the first scheduled event of the new day
def MidnightRunner() {
	state.dayoflastrun = TodayAsString()
    state.timeoflastevent = now()
    SchedulerFunction()
    def i = SearchSchedulePoints("2015-08-04T00:00:00.000")
    if (i>0) {ThermostatCommander(settings."sheatset${i}", settings."scoolset${i}", settings.snotifications, "per scheduled setpoint.$state.schedulestring")}
}

//Determines and schedules the next scheduled setpoint
def SchedulerFunction(){
	def mindiff = 60*60*1000*24*7 
    def timeNow = now()
   	def todaystring = TodayAsString()
	for (int i = 1; i <= settings.numscheduled; i++) {
        def ScheduledTime = timeToday(settings["stime$i"],location.timeZone)
        def ScheduledDays = settings["sdays$i"]
        if (ScheduledDays == null) {ScheduledDays = TodayAsString()}
 		if (ScheduledTime != null) {
        	if ((ScheduledTime.time >= timeNow) && (ScheduledDays.contains(TodayAsString())) && (ScheduledTime.time - timeNow < mindiff)){
                mindiff = ScheduledTime.time - timeNow
                state.scheduledindex = i
    		} 
    	}
	}
    if (mindiff < 60*60*1000*24*7) {
    	int i = state.scheduledindex
    	def nextrun = timeToday(settings["stime$i"],location.timeZone)
        state.nextscheduledtime = nextrun.time
        def nextrunstring = DisplayTime(nextrun)
        runOnce(nextrun, ScheduleExecuter)
		if (state.notificationlevel>1) {state.schedulestring=" Next scheduled setpoint for $thermostat.label is today at $nextrunstring"}
	} else {
        state.nextscheduledtime = -1
		if (state.notificationlevel>1) {state.schedulestring=" There are no remaining scheduled setpoints for $thermostat.label today"}
	}
	state.timeoflastevent = now()
}

def SearchSchedulePoints(time) {
    for (int i = 1; i <= settings.numscheduled; i++) {
    	def Modes = settings["smodes$i"]
        if (Modes == null) {Modes = location.mode}
        def Days = settings["sdays$i"]
        if (Days == null) {Days = TodayAsString()}
    	if(timeToday(settings["stime$i"],location.timeZone) == timeToday(time,location.timeZone) && Modes.contains(location.mode) && Days.contains(TodayAsString())) {
            return i
        }
    }
	return 0
}


//---- EXECUTION FUNCTIONS

//Runs at scheduled setpoints to determine whether a setpoint should be executed; if yes, calls thermostat commander to execute command
def ScheduleExecuter() {
	int i = state.scheduledindex
	SchedulerFunction()
    state.timeoflastevent = now()
	def valid = false
    def Modes = settings["smodes$i"]
    if (Modes == null) {Modes = location.mode}    
	if(Modes.contains(location.mode)){
    	valid = true
    } else {
    	i = SearchSchedulePoints(settings["stime$i"]) 
        if (i > 0) {valid = true}
	}
	if (valid) {
    	state.failedcommandcount = 0
        state.pendingindex = i
        ThermostatCommander(settings."sheatset${i}", settings."scoolset${i}", settings.snotifications, "per scheduled setpoint.$state.schedulestring")
	} else {
		if (state.notificationlevel>1) {sendNotificationEvent("Scheduled setpoint for $thermostat.label not executed because the current home mode, $location.mode, does not match a setpoint mode.$state.schedulestring.")}
    }
    state.schedulestring = ""
}

//Sends commands to the thermostat
def ThermostatCommander(hvalue, cvalue, notifications, notificationphrase) { 
	state.timeoflastevent = now()
	if((hvalue == null) || (cvalue == null)) {return}
    if (settings.auto != "Never") {ThermostatModeSetter(hvalue, cvalue, 0)}   

	def notificationstring = "" 
    state.checkcommandstring = ""
    def thermMode = thermostat.currentValue("thermostatMode")
    def name = thermostat.label
    
    def currentheatsetpoint = settings.thermostat.currentValue("heatingSetpoint")
    def currentcoolsetpoint = settings.thermostat.currentValue("coolingSetpoint")  

    if ("$currentcoolsetpoint" != "$cvalue") {state.checkcommandstring = "c"}
    if ("$currentheatsetpoint" != "$hvalue") {state.checkcommandstring = "h$state.checkcommandstring"}
    log.debug "Programmable Thermostat: check string is $state.checkcommandstring; values are $currentcoolsetpoint and $currentheatsetpoint"

    def primarysetpoint = hvalue
    if (thermMode == "cool") {primarysetpoint = cvalue}
	if (thermMode == "heat" || thermMode == "cool") {notificationstring = "$name set to $primarysetpoint in $thermMode mode $notificationphrase."}
    else {notificationstring = "$name set to $hvalue / $cvalue $notificationphrase."}
    
    if (settings.fnotifications && state.checkcommandstring != "") (runIn(10 + state.failedcommandcount*30, CommandIntegrityChecker))

    if (hvalue!=0) {
		log.debug "Programmable Thermostat: Heat command set to $hvalue"
		thermostat.setHeatingSetpoint(hvalue)
    }
    if (cvalue!=0) {
		log.debug "Programmable Thermostat: Cool command set to $cvalue"
		thermostat.setCoolingSetpoint(cvalue)
    }
    if (notifications && state.failedcommandcount==0) {
    	sendPush(notificationstring)
    } else if (state.notificationlevel>0 && state.failedcommandcount==0) {
    	sendNotificationEvent(notificationstring)
        if (state.checkcommandstring == "" && state.notificationlevel>2) {sendNotificationEvent("$name confirmed that it was already set to $primarysetpoint in $thermMode mode.")}
    }
    if (state.checkcommandstring == "") {log.debug "Programmable Thermostat: $name confirmed that it was already set to $primarysetpoint in $thermMode mode"}
}

//Auto Sets Thermostat Mode
def ThermostatModeSetter(hvalue, cvalue, notifications) {
	if (hvalue==0 || cvalue==0) {return}
    def currentTemp = settings.thermostat.latestValue("temperature")
    if (currentTemp > cvalue && settings.thermostat.currentValue("thermostatMode") != "cool") {
		thermostat.cool()
		if (notifications > 0) {sendNotificationEvent("$thermostat.label mode changed to cooling when temperature reached $currentTemp")}
	} else if (currentTemp < hvalue && settings.thermostat.currentValue("thermostatMode") != "heat") {
		thermostat.heat()
		if (notifications > 0) {sendNotificationEvent("$thermostat.label mode changed to heating when temperature fell to $currentTemp")}
	}        
}


//---- INTEGRITY CHECKERS

//Determines whether the last scheduled setpoint was executed; if not, reinitializes or sends missed command
def SchedulerIntegrityChecker() {
	def i = state.scheduledindex
	if ((settings.numscheduled == 0) || (settings.son == false)) {return}
	if (state.dayoflastrun != TodayAsString()) {initialize()}
}

//Determines whether commands sent to the thermostat have been properly acknowledged; if not, calls thermostat commander to reissue failed command(s)
def CommandIntegrityChecker() {
	state.timeoflastevent = now()
	if (state.pendingindex == 0) {return}
	def currentheatsetpoint = settings.thermostat.currentValue("heatingSetpoint")
    def currentcoolsetpoint = settings.thermostat.currentValue("coolingSetpoint")
	def thermMode = thermostat.currentValue("thermostatMode")
    def lastheatcommand = IndexLookUp("heat")
	def lastcoolcommand = IndexLookUp("cool")
    def failedstring = ""
    log.debug "Programmable Thermostat: $thermostat.label heating setpoint was commanded to $lastheatcommand and is currently $currentheatsetpoint; cooling setpoint was commanded to $lastcoolcommand and is currently $currentcoolsetpoint"    
  
	if (("$currentheatsetpoint" == "$lastheatcommand") && ("$currentcoolsetpoint" == "$lastcoolcommand")) {return}
    
    state.failedcommandcount = state.failedcommandcount + 1
	if ("$currentheatsetpoint" != "$lastheatcommand" && "$currentcoolsetpoint" != "$lastcoolcommand" && state.checkcommandstring == "hc") {
		failedstring = "$thermostat.label is non-responsive to setpoint commands."
		ThermostatCommander(lastheatcommand, lastcoolcommand, false, "")
	} else if ("$currentheatsetpoint" != "$lastheatcommand" && (state.checkcommandstring == "hc" || state.checkcommandstring == "h")) {
		if (thermMode == "heat") {failedstring = "$thermostat.label is non-responsive to heat setpoint commands."}
		ThermostatCommander(lastheatcommand, 0, false, "")    
    } else if ("$currentcoolsetpoint" != "$lastcoolcommand" && (state.checkcommandstring == "hc" || state.checkcommandstring == "c")) {
		if (thermMode == "cool") failedstring = "$thermostat.label is non-responsive to cool setpoint commands."
		ThermostatCommander(0, lastcoolcommand, false, "")
	}

	if (state.failedcommandcount == 4) {
		state.failedcommandcount = 0
        state.pendingindex = 0
    	if (failedstring != "") {sendPush(failedstring)}
	}
}


//---- EVENT HANDLERS

//Runs every time a mode change is detected.  Used to execute mode-based setpoints; also used to trigger schedule integrity checks in case all scheduled functions have failed
def modeChangeHandler(evt) {
    if (state.notificationlevel>2) {sendNotificationEvent("Programmable Thermostat detected home mode change to $evt.value.")}	
	for (int i = 1; i <= settings.numevent; i++) {
        def ScheduledDays = settings["edays$i"]
        if (ScheduledDays == null) {ScheduledDays = TodayAsString()}
        if ((evt.value == settings["emodes$i"]) && (ScheduledDays.contains(TodayAsString()))) {
			state.failedcommandcount = 0
            state.pendingindex = -i
            ThermostatCommander(settings."eheatset${i}", settings."ecoolset${i}", settings.enotifications, "with change to $evt.value")
        	i = settings.numevent + 1
        }        
	}
	SchedulerIntegrityChecker()
}

//Runs every time the temperature reported by the thermostat changes.  Used to trigger schedule integrity checks in case all scheduled functions have failed.
def tempChangeHandler(evt) {
	SchedulerIntegrityChecker()
    if (settings.auto == "Any time") {ThermostatModeSetter(settings.thermostat.latestValue("heatingSetpoint"), settings.thermostat.latestValue("coolingSetpoint"), state.notificationlevel)}
}

//---- OTHER

//Returns the setpoint temperature associated with a settings index
def IndexLookUp(mode) {
	def result = 0
	if (mode == "cool") {
		if (state.pendingindex > 0) {result = settings."scoolset${state.pendingindex}"}
    	if (state.pendingindex < 0) {result = settings."ecoolset${-state.pendingindex}"}
	} else if (mode == "heat") {
		if (state.pendingindex > 0) {result = settings."sheatset${state.pendingindex}"}
   		if (state.pendingindex < 0) {result = settings."eheatset${-state.pendingindex}"}
    }
	return result
}

//Returns the current day of the week as a string
def TodayAsString() {
	return (new Date(now())).format("EEEEEEE", location.timeZone)
}

//Returns time as a string in 12 hour format
def DisplayTime(time) {
	def tz = location.timeZone
    def hour = time.format("H",tz)
	def min = time.format("m",tz)
	def sec = time.format("s",tz)
	def ampm = "am"        
	def hournum = hour.toInteger()
	def minnum = min.toInteger()
	if (hournum == 0) {hournum = 12}
	if (hournum > 12) {
		hournum = hournum - 12
		ampm = "pm"
	}   
	if (minnum < 10) {min = "0$min"}   
	return "$hournum:$min $ampm"
}