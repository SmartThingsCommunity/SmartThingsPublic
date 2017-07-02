/**
 *  Smart Bathroom Ventilation-Scenario
 *
 *  Version 1.0.0 (11/27/15) - Initial release of child app
 *  Version 1.0.1 (1/17/16) - Allow for parent app to see version of child app, added refresh of sensor
 *  Version 1.0.2 (4/4/16) - Added icons to restriction items
 * 
 * 
 *  Copyright 2016 Michael Struck - Uses code from Lighting Director by Tim Slagle & Michael Struck
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
    name: "Smart Bathroom Ventilation-Scenario",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Child app (do not publish) that allows ventilation scenarios based on humidity or certain lights being turned on.",
    category: "Convenience",
    parent: "MichaelStruck:Smart Bathroom Ventilation",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Bathroom-Ventilation/BathVent.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Bathroom-Ventilation/BathVent@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Bathroom-Ventilation/BathVent@2x.png"
    )

preferences {
	page name:"pageSetup"
}

// Show setup page
def pageSetup() {
	dynamicPage(name: "pageSetup", title: "Ventilation Scenario", install: true, uninstall: true) {
		section() {
			label title:"Scenario Name", required: true
    	}
        section("Devices included in the scenario") {
			input "A_switch","capability.switch", title: "Monitor this light switch...", multiple: false, required: true
			input "A_humidity", "capability.relativeHumidityMeasurement",title: "Monitor the following humidity sensor...", multiple: false, required: false, submitOnChange:true
			input "A_fan", "capability.switch", title: "Control the following ventilation fans...", multiple: true, required: true
		}
		section("Fan settings") {
        	if (A_humidity && A_switch){
            	input "A_humidityDelta", title: "Ventilation fans turns on when lights are on and humidity rises(%)", "number", required: false, description: "0-50%"
            	input "A_repoll", "enum", title: "Repoll humidity sensor after (minutes)...", options: [[1:"1 Minute"],[2:"2 Minutes"],[3:"3 Minutes"],[4:"4 Minutes"],[5:"5 Minutes"]], defaultValue: 5
            }
            input "A_timeOn", title: "Optionally, turn on fans after light switch is turned on (minutes, 0=immediately)", "number", required: false
            if (A_humidity) {
            	input "A_fanTime", title: "Turn off ventilation fans after...", "enum", required: false, options: [[5:"5 Minutes"],[10:"10 Minutes"],[15:"15 Minutes"],[30:"30 Minutes"],[60:"1 hour"],[98:"Light switch is turned off"],[99:"Humidity drops to or below original value"]]
			}
            else {
            	input "A_fanTime", title: "Turn off ventilation fans after...", "enum", required: false, options: [[5:"5 Minutes"],[10:"10 Minutes"],[15:"15 Minutes"],[30:"30 Minutes"],[60:"1 hour"],[98:"Light switch is turned off"]]
            }
            input "A_manualFan", title: "If ventilation fans are turned on manually, turn them off automatically using settings above", "bool", defaultValue: "false"
        }
		section("Restrictions") {            
			input "A_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/calendar.png"
        	href "timeIntervalInputA", title: "Only During Certain Times...", description: getTimeLabel(A_timeStart, A_timeEnd), state: greyedOutTime(A_timeStart, A_timeEnd),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/clock.png"
            input "A_mode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, 
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/modes.png"
		}
        section("Tap the button below to remove this scenario only"){
        }
    }
}

page(name: "timeIntervalInputA", title: "Only during a certain time") {
		section {
			input "A_timeStart", "time", title: "Starting", required: false
			input "A_timeEnd", "time", title: "Ending", required: false
		}
} 

def installed() {
    initialize()
}

def updated() {
    state.triggeredA = false
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
    if(A_switch) {
        state.A_runTime = A_fanTime ? A_fanTime as Integer : 98
        subscribe(A_switch, "switch.on", onEventA)
    	subscribe(A_switch, "switch.off", offEventA)
    	if (A_manualFan) {
        	subscribe(A_fan, "switch.on", turnOnA)
            subscribe(A_fan, "switch.off", turnOffA)
        }
        if (A_humidity && (A_humidityDelta || state.A_runTime == 99)) {
    		subscribe(A_humidity, "humidity", humidityHandlerA)
		}
	}
}

//Handlers----------------
//A Handlers
def turnOnA(evt){
    if ((!A_mode || A_mode.contains(location.mode)) && getDayOk(A_day) && A_switch.currentValue("switch")=="on" && !state.triggeredA && getTimeOk(A_timeStart,A_timeEnd)) {
        log.debug "Ventilation turned on."
    	A_fan?.on()
        state.triggeredA = true
        if (state.A_runTime < 98) {
			log.debug "Ventilation will be turned off in ${state.A_runTime} minutes."
            unschedule ()
            runIn (state.A_runTime*60, "turnOffA")
        }
	}
}

def turnOffA(evt) {
	log.debug "Ventilation turned off."
    A_fan?.off()
    unschedule ()
}

def humidityHandlerA(evt){
    def currentHumidityA =evt.value as Integer
    log.debug "Humidity value is ${currentHumidityA}."
	if (state.humidityLimitA && currentHumidityA > state.humidityLimitA) {
        turnOnA()
    }
	if (state.humidityStartA && currentHumidityA <= state.humidityStartA && state.A_runTime == 99){
    	turnOffA()
    }      
}
def rePoll(){
	def currentHumidityA =A_humidity.currentValue("humidity")
    log.debug "Humidity value before refresh ${currentHumidityA}."
    A_humidity.refresh()
}

def onEventA(evt) {   
    def humidityDelta = A_humidity && A_humidityDelta ? A_humidityDelta as Integer : 0
    def text = ""
    if (A_humidity){
    	
        state.humidityStartA = A_humidity.currentValue("humidity")
    	state.humidityLimitA = state.humidityStartA + humidityDelta
        text = "Humidity starting value is ${state.humidityStartA}. Ventilation threshold is ${state.humidityLimitA}."
    }
    if (A_repoll){
        log.debug "Re-Polling in ${(A_repoll as int)*60} seconds"
        runIn((A_repoll as int)*60, "rePoll")
    }
    log.debug "Light turned on in ${app.label}. ${text}" 
    if ((!A_humidityDelta || A_humidityDelta == 0) && (A_timeOn != "null" && A_timeOn == 0)) {
    	turnOnA()
    }
    if ((A_timeOn && A_timeOn > 0) && getDayOk(A_day) && !state.triggeredA && getTimeOk(A_timeStart,A_timeEnd)) {
        log.debug "Ventilation will start in ${timeOn} minute(s)"
        runIn (timeOn*60, "turnOnA")
    }
}

def offEventA(evt) {
    def currentHumidityA = ""
    def text = ""
    if (A_humidity) {
    	currentHumidityA = A_humidity.currentValue("humidity") 
        text = "Humidity value is ${currentHumidityA}."
    }
    log.debug "Light turned off in '${app.label}'. ${text}"
	state.triggeredA = false
    if (state.A_runTime == 98){
    	turnOffA()
    }
}

//Common Methods-------------

def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
	
    if(start && end){
    	timeLabel = "Between " + hhmm(start) + " and " +  hhmm(end)
    }
    else if (start) {
		timeLabel = "Start at " + hhmm(start)
    }
    else if(end){
    timeLabel = "End at " + hhmm(end)
    }
	timeLabel	
}

def greyedOutTime(start, end){
	def result = start || end ? "complete" : ""
}

private hhmm(time) {
	new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("h:mm a", timeZone(time))
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

private getTimeOk(startTime,endTime) {
	def result = true
	def currTime = now()
	def start = startTime ? timeToday(startTime).time : null
	def stop =  endTime ? timeToday(endTime).time: null
    if (startTime && endTime) {
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	else if (startTime){
    	result = currTime >= start
    }
    else if (endTime){
    	result = currTime <= stop
    }
    result
}
//Version
private def textVersion() {
    def text = "Child App Version: 1.0.2 (04/04/2016)"
}