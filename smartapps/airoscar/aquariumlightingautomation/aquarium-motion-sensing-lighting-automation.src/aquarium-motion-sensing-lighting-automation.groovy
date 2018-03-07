/**
 *  Aquarium Motion Sensing Light Scheduler 
 *
 *  Copyright 2016 Oscar Chen
 *
 *	This is a child app, please also refer to parent app "airoscar/aquariumlightingparent:Aquarium Motion Sensing Lighting Scheduler"
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
    name: "Aquarium Motion Sensing Lighting Automation",
    namespace: "airoscar/aquariumlightingautomation",
    parent: "airoscar/aquariumlightingparent:Aquarium Motion Sensing Lighting Scheduler",
    author: "Oscar Chen",
    description: '''A smart aquarium lighting scheduler that conforms to a minimum and maximum set # of hours per day, 
    which also has the flexibility of triggering the lights on by motion sensor for user to view the aquarium, while 
    ensuring the maximum lighting threshold is not breached. The Smart Aquarium Lighting Scheduler behaves as the follow:
    (1) It will turn on the light in the morning at a set time ('Do not turn on before' setting) if desired;
    (2) It will turn on the light when there is motion during the day between the allowed time frame;
    (3) It will turn off the light after a certain set amount of time has lapsed since the motion has stopped;
    (4) It will monitor how many hours the light has been running today and yesterday;
    (5) It will turn on the light automatically close to end of the day if the light has not been running the desired number of hours today;
    (6) The desired number of hours of light is the average of the two settings : 'Minimum # of Hours' & 'Maximum # of Hours';
    (7) It will target today's run time based on yesterday's. Ie: if the light had ran closer to the 'Minimum # of Hours' yesterday, today it will try to be closer to 'maximum # of Hours';
    (8) However, the motion sensor will be able to turn on the light as long as 'Maximum # of Hours' is not breached, and the time is winthin the allowable time frame;
    (9) Finally, It will turn off the light in the evening at a set time ('Do not turn on after' setting); You can still manually toggle the light on/off in the after hour, but it will be turned off automatically shortly after if you leave it on;
    (10) Additional function: a notification setting can be turned on, to report at a set time once per day: run time today, and run time yesterday.
    (11) Additional function: you can toggle off the lights while the motion sensor is active, this would set off a bypass timer, during which the motion sensor would not turn on the light.
    
    ''',
    category: "Pets",
    iconUrl: "http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Outdoor/outdoor3-icn@2x.png")

preferences {

        section ("Master switch") {
        	input ("enableApp", "bool", title: "Use this to enable/disable this app without uninstalling.", defaultValue: true, required: true)
        }

		section("Aquarium light and motion sensor"){
            input ("switches", "capability.switch", title: "Select light", multiple: false, required: true)
            input ("motion1", "capability.motionSensor", title: "Select motion sensor", required: true)
            input ("minutes1", "number", title: "Keep it on for how many minutes after motion stops?", required: true, defaultValue: 30)
            input ("motionEnable", "bool", title: "Enable motion sensor", required: true, defaultValue: true)
        }

        section ("Desired amount of lighting per day:"){
            input ("minHr", "decimal", title: "Minimum # of Hours", required: true, defaultValue: 9)
            input ("maxHr", "decimal", title: "Maximum # of Hours", required: true, defaultValue: 14)
        }

        section("Turn on only during this time frame"){
            input ("dayStart", "time", title: "Day Start", defaultValue: "6:30", required: true)
            input ("dayEnd", "time", title: "Day End", defaultValue: "21:30", required: true)
            input ("turnOnDayStart", "bool", title: "Also always turn on briefly at Day Start even when there is no motion? (Wake your fish.)", defaultValue: false, required: true)
        }
        
        section ("Also keep light on during this time frame (this override all other rules)") {
        	input ("forceOnStart", "time", title: "Start time", defaultValue: "9:00", required: true)
            input ("forceOnEnd", "time", title: "End time", defaultValue: "18:00", required: true)
            input ("forceLight", "bool", title: "Enable/disable", defaultValue: false, required: true)
        }

        section("Report yesterday's lighting runtime via push notification / text message?") {
        	input ("enableReport", "bool", title: "Turn on/off notification?", defaultValue: false, required: true)
            input ("reportTime", "time", title: "What time would you like to received the notification?", defaultValue: dayEnd, required: false)
            input("recipients", "contact", title: "Send notifications to") {
                input "phone", "phone", title: "Phone number (optional)", required: false
            }
        }

}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
	state.lastScheduleCheck = 0		//persistent variable for storing the time that last time schedule checking methods were ran
    state.bypassMotion = false		//persistent bool variable indicating if motion sensor has been bypassed (disabled)
    state.bypassStartTime = 0		//persistent variable for storing the time that motion sensor is bypassed (disabled)
    state.forceLight = false		//persistent bool variable indicating if force-light-on is active
    
    def inputFault = false	//changes to true if there is an user input error
    def faultMsg = ""	//inpur error message
    
    if (minHr > maxHr) {	//if user input mininum run hour is greater than maximum run hour
    	inputFault = true
        faultMsg = "Minimum # of Hours cannot be greater than Maximum # of Hours."
        log.debug faultMsg
    }
    
    if (timeToday(dayStart).time >= timeToday(dayEnd).time) {	//if user input day start time is after day end time
    	inputFault = true
        faultMsg = "Day Start cannot be later than Day End. ${dayStart}. ${dayEnd}."
        log.debug faultMsg
    }
    
    if (timeToday(dayEnd).time - timeToday(dayStart).time < minHr * 3600000) {	//if the day start and day end gap is less than the minimum run hour specified
    	inputFault = true
        faultMsg = "Day Start and Day End times must be sufficient so Minmum # of Hours can be achieved."
        log.debug faultMsg
    }
    
    if (forceLight == true) {
    	if (forceOnStart == null || forceOnStart == "") {
        	inputFault = true
            faultMsg = "Please specify a timeframe for turning on light and override other rules."
            log.debug faultMsg
        }
        if (forceOnEnd == null || forceOnEnd == "") {
        	inputFault = true
            faultMsg = "Please specify a timeframe for turning on light and override other rules."
            log.debug faultMsg
        }
        if (timeToday(forceOnStart).time >= timeToday(forceOnEnd).time) {
        	inputFault = true
            faultMsg = "Start time must be before end time, entered for timeframe for turning on light and override other rules."
        	log.debug faultMsg
        }
    }
    
    
    if (inputFault == false) {	//check user preference input
        if (enableApp == true) {	//master switch, if app is enabled

            subscribe(motion1, "motion", motionHandler)
            subscribe(switches, "switch", switchHandler)

            //schedule schedule update and runtime refresh
            runEvery15Minutes(updateSchedule)

            //schedule to run notification report
            if (enableReport == true) {

                if (reporTime == "" || reportTime == null) {	//if reportTime is not specified, use dayEnd as reportTime
                    schedule (dayEnd, notificationReport)
                } else {
                    schedule (reportTime, notificationReport)
                }
            }

            if (turnOnDayStart == true) {
                schedule (dayStart, turnOn)
            }
            
            if (forceLight == true) {
            	schedule (forceOnStart, forceLightOnActivate)
                schedule (forceOnEnd, forceLightOnDeactivate)
            }
            
        } else {
            sendPush "${app.name} >> ${app.label} is disabled. To re-enable it, go to the app setting and turn on the master switch."
        }
    
	} else {
    	sendPush "${app.name} >> ${app.label}: preferences input error. Please check input parameters. Error: ${faultMsg}."
    }
}

def forceLightOnActivate() {
	state.forceLight = true
    turnOn()
}

def forceLightOnDeactivate() {
	state.forceLight = false
    updateSchedule()
}

def forceLightOn() {
//check the force-light-on times and sets global variable

	if (forceLight == true) {
    
    	def t0 = now()
    	def startForceLight = timeToday(forceOnStart).time
    	def endForceLight = timeToday(forceOnEnd).time
        
        if (t0 >= startForceLight && t0 < endForceLight) {
            state.forceLight = true
        } else {
            state.forceLight = false
        }
    }
    
}

def switchHandler(evt) {
//called on whenever switch state changes
	
    updateSchedule()
    bypassMotion()
}


def turnOn() {
	switches.on()
}

def turnOff() {
 	switches.off()
}

def notificationReport() {
//Generates runtime report and push notification or text message
	
    runtime()
    
    def todayRunHour
    def yesterdayRunHour
    def runtimeTarget
    
    if (state.targetToday > 0) {
    	runtimeTarget = state.targetToday / 3600000
    } else {
    	runtimeTarget = 0
    }
    
    if (state.runtimeToday > 0) {
    	todayRunHour = state.runtimeToday / 3600000
    } else {
    	todayRunHour = 0
    }
    
    if (state.runtimeYesterday > 0 ) {
    	yesterdayRunHour = state.runtimeYesterday / 3600000
    } else {
    	yesterdayRunHour = 0
    }
    
    
    def msg = "${app.name} >> ${app.label} >> Current runtime >> Today: ${todayRunHour.toDouble().round(2)} hours. Yesterday: ${yesterdayRunHour.toDouble().round(2)} hours. Today's target run time: ${runtimeTarget.toDouble().round(2)} hours."
    
    if (location.contactBookEnabled) {
    	sendNotificationToContacts(msg, recipients)
    } else {
    	if (phone) {
        	sendSms phone, msg
        } else {
        	sendPush msg
            log.debug msg
        }
   	}
    
    
}   

def updateSchedule() {
//Ensures autoScheduler is not called upon too often
//calls autoScheduler() to evaluate runtime, update target, and create schedule.

	if (state.lastScheduleCheck == null || state.lastScheduleCheck == "") {
    	state.lastScheduleCheck = 0
    }

	//check enough time has elapsed since last ran
	if (now() - state.lastScheduleCheck >= minutes1 * 30000) {
    	autoScheduler()
    } else {
    	log.debug "Not enough time since last schedule update."
    }
}

def autoScheduler() {
//this method is core of logic that turns on/off the light switches
//creates lighting schedule based on the runtime today and yesterday.
//Calls runtime() to evaluate and update runtime
//Calls targetCalc() to update runtime target
//Creates schedules
//This method is scheduled to run at a regular interval during initialisation
//this method will be called on by switches events, and some motion events.
//DO NOT make calls to this method directly unless neccessary, use updateSchedule() instead to ensure this method is not ran too frequently to reduce server load.

	forceLightOn()
	
    def t0 = now()
        
    def startTime = timeToday(dayStart)
    def endTime = timeToday(dayEnd)

    def scheduledOn
    def scheduledOff
    
    if (state.forceLight == true) {	//check if forceLight is in effect
    	
        if (switches.currentValue("switch") == "off") {
        	turnOn()
            log.debug "forceLight active, turn on lights."
        }
        
        log.debug "forceLight active, lights already on."
        
    } else {
    	runtime()
        
        state.lastScheduleCheck = t0	//last schedule check timer is only reset if this portion of code is run, to prevent running the runtime() overly
        scheduledOn = endTime.time - state.runtimeRemain
        scheduledOff = endTime.time

            //check to make sure scheduled turn on time is within time boundary
            if (scheduledOn < startTime.time) {
                scheduledOn = startTime.time
            }

            if (state.runtimeRemain > 0) { //if still has not met target yet

                if (scheduledOn < scheduledOff) { //if scheduled turn-on time is less than scheduled turn-off time (as it should be)

                    if (t0 >= startTime.time && t0 < endTime.time ) { //if now() is within allowable time boundary, create schedules

                        if (scheduledOff - scheduledOn >= minutes1 * 60000) { //if the light can be turn on for at least the duration specified by 'minutes1' input

                             if (switches.currentValue("switch") == "off") {	//siwtch is currently off

                                if (scheduledOn > t0 && scheduledOff > t0) {	//scheduledOn is in future
                                    runIn((scheduledOn-t0)/1000, turnOn, [overwrite: true])
                                    log.debug "(1) Scheduled to turn on in ${(scheduledOn-t0)/60000} minutes."
                                    runIn((scheduledOff-t0)/1000, turnOff, [overwrite: true])
                                    log.debug "(1) Scheduled to turn off in ${(scheduledOff-t0)/60000} minutes."

                                } else if (scheduledOn < t0 && scheduledOff > t0) {	//scheduledOn already passed
                                    runIn(1, turnOn, [overwrite: true])
                                    log.debug "(2) Scheduled to turn on now."
                                    runIn((scheduledOff-t0)/1000, turnOff, [overwrite: true])
                                    log.debug "(2) Scheduled to turn off in ${(scheduledOff-t0)/60000} minutes."

                                } 

                             } else if (switches.currentValue("switch") == "on") {	//switch is already on

                                if (t0 > scheduledOn) { //scheduledOn time already passed
                                    runIn((scheduledOff-t0)/1000, turnOff, [overwrite: true])
                                    log.debug "(3) Scheduled to turn off in ${(scheduledOff-t0)/60000} minutes."

                                } else if (t0 < scheduledOn) { //scheduledOn time is in future, turn off after delay for now.
                                    log.debug "ScheduledOn is in future, calling scheduledTurnOffAfterMotionStops()."
                                    scheduledTurnOffAfterMotionStops()
                                }
                             }
                        }
                    } else if (switches.currentValue("switch") == "on") {	//switch is turned on during after hours
                        log.debug "Switch is toggled on outside of allowable time frame, scheduled to turn off."
                        scheduledTurnOffAfterXMinutes()
                    }

                } else {log.debug "Scheduled on is after off."}

            } else if (switches.currentValue("switch") == "on") {	//switch is toggled on while there is no remaining target
                log.debug "No Target remain, scheduled to turn off. Calling scheduledTurnOffAfterXMinutes()"
                scheduledTurnOffAfterXMinutes()
            }
	}
}

def scheduledTurnOffAfterMotionStops() {
//called upon by autoScheduler
// Checks how long it has been since last motion sensor state, and turns off light after 'minutes1' has lapsed since motion stopped.

    def t0 = now()
    def motionState = motion1.currentState("motion")
    def elapsedMotion = t0 - motionState.rawDateCreated.time
    def switchState = switches.currentState("switch")
    def elapsedSwitch = t0 - switchState.rawDateCreated.time
	
    if (motionState.value == "inactive") {
        if (elapsedMotion < minutes1 * 60000) { //check that not enough time has elapsed since motion stopped
        	if (elapsedMotion < elapsedSwitch + 2000) { //check that switch was not toggled 2 seconds after motion stopped, (manual trigger)
            	runIn((minutes1 * 60 - elapsedMotion/1000), turnOff, [overwirte: true])
            	log.debug "(6) Scheduled to turn off in ${(minutes1 * 60 - elapsedMotion/1000)/60} minutes. Elapsed=${elapsedMotion/60000} minutes."
        	} else {	//switch was manual triggered
                log.debug "(7) Light switch triggered after last motion state. Calling scheduledTurnOffAfterXMinutes()."
                scheduledTurnOffAfterXMinutes()
            }
        } else {	//enough time has elapsed since motion stopped
            log.debug "(8) Enough time elapsed since motion stopped. Calling scheduledTurnOffAfterXMinutes()."
            scheduledTurnOffAfterXMinutes()
        }
    } else if (motionState.value == "active") {
    	runIn(minutes1 * 60, turnOff, [overwrite: true])
    	log.debug "(9) Motion is active. Scheduled turn off time in ${minutes1}."
    }

}

def scheduledTurnOffAfterXMinutes() {
//Called upon by autoScheduler.
//Checks how long it has been since last switch state, and turns off light after 'minutes1' has lapsed since it was turned on.

	def t0 = now()
    def switchState = switches.currentState("switch")
    def elapsed = t0 - switchState.rawDateCreated.time
    
    if (elapsed < minutes1 * 60000) { //check time elapsed since switch was turned on
    	runIn((minutes1 * 60 - elapsed/1000), turnOff, [overwrite: true])
        log.debug "(4) Scheduled to turn off in ${(minutes1 * 60 - elapsed/1000)/60} minutes. Elapsed=${elapsed/60000} minutes."
    } else {
        runIn(1, turnOff, [overwirte: true])
        log.debug "(5) Scheduled to turn off now. Elapsed=${elapsed/60000} minutes"
    }
}

def targetCalc() {
	//evaluates runtime remaining, saves to persistent storage
    
	def targetRuntime = (minHr + maxHr) / 2 * 3600000
	def YDayRuntime
    def TDayRuntime
    def makeUpTarget
    def remainTarget
    def minTarget = minHr * 3600000
    def maxTarget = maxHr * 3600000
    
    //default case for state.runtimeYesterday is the average desired run hours.
    if (state.runtimeYesterday == null || state.runtimeYesterday == "") {
    
    	YDayRuntime = (minHr + maxHr) / 2 * 3600000
        
    } else {
    
    	YDayRuntime = state.runtimeYesterday
        
    }
    
    //default case for state.runtimeToday is zero.
    if (state.runtimeToday == null || state.runtimeToday == "") {
    
    	TDayRuntime = 0
        
    } else {
    
    	TDayRuntime = state.runtimeToday
    }
    
    //log.debug "targetRuntime: ${targetRuntime}. YDayRuntime: ${YDayRuntime}."
    
    makeUpTarget = 0.5 * (targetRuntime - YDayRuntime) //make up today's target based on yesterday's actual runtime. ie: yesterday was over by 1 hour, today's target will be under by 0.5 hour.
    targetRuntime = targetRuntime + makeUpTarget  //adjusts today's target runtime based on yesterday's actual runtime
    
    //check target against the minimum and maximum daily limits
    if (targetRuntime < minTarget) {
    
    	targetRuntime = minTarget
        
    } else if (targetRuntime > maxTarget) {
    
    	targetRuntime = maxTarget
    }
    
    state.targetToday = targetRuntime
    
    //compare the target runtime against the runtime already occured so far today and calculate remainder
    if (targetRuntime > TDayRuntime) {
    
    	remainTarget = targetRuntime - TDayRuntime
        
    } else {
    	remainTarget = 0
    }
    
    state.runtimeRemain = remainTarget
    log.debug "state.runtimeRemain: ${state.runtimeRemain / 60000} minutes."

}

def runtime() {
//evaluates total runtime yesterday and today, saves to persistent storage
    
        def twoDaysAgo = timeToday("00:00", location.timeZone) - 1	//midnight two nights ago
        def yesterday = timeToday("00:00", location.timeZone)	//midnight last night

        //log.debug "runtime() called. yesterday: ${yesterday}. twoDaysAgo: ${twoDaysAgo}"

        state.runtimeToday = runtimeQuery(yesterday)
        state.runtimeYesterday = runtimeQuery(twoDaysAgo, yesterday)

        log.debug "runtimeToday: ${state.runtimeToday/60000} minutes. runtimeYesterday: ${state.runtimeYesterday/60000} minutes."
    	
        targetCalc()
}

def runtimeQuery(queryStartTime, queryEndTime = "") {
//this method will try query the past event, if not all result is returned, it will try run additional query and combine the results into a single list.
//the combined list is passed to runtimeTotalizer, which will add up the runtime

//log.debug "runtimeQuery called. queryStartTime: ${queryStartTime}. queryEndTime: ${queryEndTime}"
    def newQueryEndTime
    def eventList
    def newEventList
    def i = 0			//loop counter
    def startResultTime
    def continueLoop = true
    
   	if (queryEndTime == "") {	//if queryEndTime is not specified
    	eventList = switches.eventsSince(queryStartTime, [max: 1000])
    } else {
    	eventList = switches.eventsBetween(queryStartTime, queryEndTime, [max: 1000])
    }
    
    def queryResultSize = eventList.size
    if (queryResultSize > 0) {
    	startResultTime = eventList[queryResultSize - 1].date	//date of the first result (last item in the list)
    }
    //log.debug "queryResultSize=${queryResultSize}. startResultTime=${startResultTime}."
    
    //if the query did not return all the desired results, re-run the query with the earlist result from last query as new query end time, and add new list to original list
    while (continueLoop == true && queryResultSize > 0 && i < 50) {
    
    	newQueryEndTime = startResultTime	//assign the date of first result as new query's end time
        newEventList = switches.eventsBetween(queryStartTime, newQueryEndTime, [max: 1000])	//new query events list
        //log.debug "newEventList: ${newEventList}."
        
        //combines newEventList into eventList
		eventList.addAll(newEventList)
        //log.debug "eventList: ${eventList}."
        
        queryResultSize = newEventList.size
        //log.debug "queryResultSize=${queryResultSize}."
        
        if (queryResultSize > 0) {
        	startResultTime = newEventList[queryResultSize - 1].date	//update the date of the first result from the new query (last item in the list)
        } else {
        	//need to break out of the loop
            continueLoop = false
        }
               
        i++
        //log.debug "i=${i}. queryResultSize=${queryResultSize}."
    }
    
    //log.debug "i=${i}. Passing final eventList to runtimeTotalizer...eventList.size=${eventList.size()}."
    return runtimeTotalizer(eventList)
}

def runtimeTotalizer(eventList) {
//loops through a given list contain on & off events to sum up total runtime

	def thisEvent
    def lastEvent
    def onTime
    def offTime
    def evtRuntime = 0
    def evtRuntimeTotal = 0
    def i = 0
    def arraySize = eventList.size()
    //log.debug "runtimeTotalizer initiated. arraySize = ${arraySize}."
    
    if (arraySize > 0) {
    
        //looping through events array for the first switch
        for (i = 0; i < arraySize - 2; i++) {

            //log.debug "Event ${i} out of ${arraySize}. Value: ${eventList[i].value}."

            thisEvent = eventList[i]
            lastEvent = eventList[i + 1] //the event array is in reverse chronological order

            //if an on-and-off sequence is found
            if (thisEvent.value == "off" && lastEvent.value == "on") {

                onTime = lastEvent.date.getTime()
                offTime = thisEvent.date.getTime()

                if (offTime >= onTime) {
                    evtRuntime = offTime - onTime
                } else {
                    log.debug "runtimeTotalizer ERROR: switches offTime is before onTime."
                }

                evtRuntimeTotal = evtRuntimeTotal + evtRuntime
                //log.debug "On/off sequence: ${evtRuntime/60000} minutes, off at ${lastEvent.date.toString()}."
                
                //if a on-on sequence is found
            } else if (thisEvent.value == "on" && lastEvent.value == "on") {
            	
                onTime = lastEvent.date.getTime()
                offTime = thisEvent.date.getTime()
                
               	if (offTime >= onTime) {
                	evtRuntime = offTime - onTime
                } else {
                	log.debug "runtimeTotalizer ERROR: switches onTime sequence error."
                }
                
                evtRuntimeTotal = evtRuntimeTotal + evtRuntime
            }
        }

        //if the first item in the events array (latest event) was turning switches on, add time elapsed since switches were turned on
        if (eventList[0].value == "on" && now() - eventList[0].date.getTime() > 0) {
            evtRuntimeTotal = evtRuntimeTotal + now() - eventList[0].date.getTime()
        }
    
    } else {
    	evtRuntimeTotal = 0
    }
    //log.debug "Totalizer: ${evtRuntimeTotal}."
    return evtRuntimeTotal
}

def resetBypassTimer () {
//schedule to run when the bypass is set to true
//evaluates the time lapsed since motion bypass is set, setting it to false if enough time has elapsed

    def elapsed = now() - state.bypassStartTime
    
    if (elapsed > minutes1 * 60000 - 1000) {
    	log.debug "Enough time has passed since motionBypass, setting it to false."
        state.bypassMotion = false
        state.bypassStartTime = 0
    }
    
}

def bypassMotion() {
//when the switch is toggled off (manually) while the motion sensor is active, the motion sensor will be bypassed for the duration specified by 'minutes1'.
//when the motion sensor bypass is active, new motion active events will not turn on the light switches

	def t0 = now()
	def motionState = motion1.currentState("motion")
    def switchState = switches.currentState("switch")
    def startTime = timeToday(dayStart)
    def endTime = timeToday(dayEnd)
    
    if (t0 >= startTime.time && t0 <= endTime.time) {	//check that current time is within the timeframe which motion sensor can trigger on light
        if (switchState.value == "on") {	//if switch is currently turned on, reset bypass
            state.bypassMotion = false
            state.bypassStartTime = 0
            log.debug "bypassMotion set to false."
        } else if (switchState.value == "off" && motionState.value == "active") {	//if the switch is turned off while motion sensor is active, bypass motion sensor
            state.bypassMotion = true
            state.bypassStartTime = now()
            runIn(minutes1 * 60, resetBypassTimer, [overwrite: true])
            log.debug "bypassMotion set to true."
        }
    }
}

def motionHandler(evt) {
//called whenever motion sensor is triggered on or off

    def startTime = timeToday(dayStart, location.timeZone)
    def endTime = timeToday(dayEnd, location.timeZone)
    def bypassMotionActive
    
    if (motionEnable == true && state.forceLight == false) {	//skip if motion sensor is disabled or if forceLight is in effect
    
        //check if bypassMotion is active
        if (state.bypassMotion != null || state.bypassMotion != "") {
            bypassMotionActive = state.bypassMotion
        } else {
            bypassMotionActive = false
        }

        if (evt.value == "active" && switches.currentValue("switch") == "off") {	//if triggered by motion being active

                 if (now() >= startTime.time && now() < endTime.time) { //check time boundary

                        if (state.runtimeToday <= maxHr * 3600000 - minutes1 *60000) {	//check today's runtime is not greater than maximum allowable

                            if (bypassMotionActive == true) {	//check if trigger was turned off by something other than motion sensor (ie. manual toggle), in which case do not turn on
                                log.debug "bypassMotion active."
                            } else { //motion sensor is not bypassed
                                turnOn()
                            }

                       }
                 }
         } else if (evt.value == "inactive" && switches.currentValue("switch") == "on") { //if triggered by motion being inactive while switch is already on

                updateSchedule() //let updateSchedule() handle turning off lights to prevents downtime during scheduled run hours

         } else if (evt.value == "active" && switches.currentValue("switch") == "on") {	//if triggered by motion being active and switch is already on
                //delay the scheduled turn-off by another duration specified by 'minutes1'
                runIn(minutes1 * 60, turnOff, [overwrite: true])	//scheduls to run turnOff(true) with motionCheck required (in case motion sensor stays active)
                log.debug "motionHandler schedule to turn off in ${minutes1} minutes."
                //forces autoscheduler to run before the light switches turns off
                runIn(minutes1 * 60 - 1, autoScheduler, [overwrite: true])
         }
	}      
    	    
    
}
