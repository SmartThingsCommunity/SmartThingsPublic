/**
 *  Talking Alarm Clock-Schedule
 *
 *  Copyright © 2016 Michael Struck
 *
 *  Version 1.5.5 (4/21/16)
 *
 *  Version 1.0.0 - Initial release
 *  Version 1.0.1 - Small syntax changes for consistency
 *  Version 1.1.0 - Added switch alarm restriction
 *  Version 1.2.0 - Added custom alarm sound selection
 *  Version 1.3.0 - Added presence voice variables (requires presence sensor used under the restrictions page), optimized code
 *  Verison 1.4.0 - Added switches that can trigger alarms in addition to time
 *  Version 1.4.1 - Added the trigger switches to the summary page
 *  Version 1.4.2 - Syntax changes and minor revisions
 *  Version 1.4.3 - Code Optimizations and work around for playTrack()
 *  Version 1.4.4 - Minor syntax updates
 *  Version 1.5.0 - Added additional triggers to set off alarm
 *  Version 1.5.1a - Added time restricitions to allow for new triggers
 *  Version 1.5.2a - Added delay after presence option
 *  Version 1.5.3c - Cleaned up reporting after addition of new triggers
 *  Version 1.5.4 - Icons for restrictions
 *  Version 1.5.5 - Minor GUI changes to accomodate new mobile app structure
 *  
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
    name: "Talking Alarm Clock-Schedule",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Child app (do not publish) to control various waking schedules using a SmartThings connected speaker as an alarm.",
    category: "Convenience",
    parent: "MichaelStruck:Talking Alarm Clock",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png"
    )
preferences {
	page name:"pageSetup"
    page name:"pageWeatherSettings"
    page name:"pageRestrictions"
    page name:"pageAlarmTriggers"
}
// Show setup page
def pageSetup() {
	dynamicPage(name: "pageSetup", title: "Alarm Settings", install: true, uninstall: true) {
        section() {
			label title:"Alarm Schedule Name", required: true
            def status = parent.getSchedStatus(app.id) ? "ENABLED" : "DISABLED"
            if (status) paragraph "This schedule is currently ${status}. To change this status, go to the 'Alarm Summary' page on the Talking Alarm Clock main page"
    	}
        section("Alarm Settings"){
        	input "alarmSpeaker", "capability.musicPlayer", title: "Choose A Speaker", required: true, submitOnChange:true
            if (alarmSpeaker){
            	if (!alarmSpeaker.name.contains("Sonos")){
                	paragraph "This application is intended to be used with Sonos speakers only. You may get undesired results if you "+
                    	"attempt to use this application with your ${alarmSpeaker} speaker."
                }
                input "alarmVolume", "number", title: "Alarm Volume", description: "0-100%", required: false
                href "pageAlarmTriggers", title: "Alarm Triggers...", description: getTriggersDesc(), state: greyOutTriggers()
                input "alarmType", "enum", title: "Select A Primary Alarm Type...", multiple: false, required: true, options: [[1:"Alarm sound (up to 20 seconds)"],[3:"Music track/internet radio"],[2:"Voice Greeting"],], submitOnChange:true
                if (alarmType != "3") {
                    if (alarmType == "1") input "secondAlarm", "enum", title: "Select A Second Alarm After The First Is Completed", multiple: false, required: false, options: [[2:"Music track/internet Radio"],[1:"Voice Greeting"]], submitOnChange:true
                    if (alarmType == "2") input "secondAlarmMusic", "bool", title: "Play A Track After Voice Greeting", defaultValue: "false", required: false, submitOnChange:true
                }
                href "pageRestrictions", title: "Alarm Restrictions", description: getRestrictionDesc(), state: greyOutRestrictions()
            }
		}
        if (alarmType == "1"){
        	section ("Alarm sound options"){
				input "soundAlarm", "enum", title: "Play This Sound...", required:false, multiple: false, 
                	options: [[1:"Alien-8 seconds"],[2:"Bell-12 seconds"], [3:"Buzzer-20 seconds"], 
                    [4:"Fire-20 seconds"], [5:"Rooster-2 seconds"], [6:"Siren-20 seconds"],[7:"Custom-User Defined"]], submitOnChange:true
				if (soundAlarm){
                	if (soundAlarm == "7") input "alarmCustom", "text", title:"URL/Location Of Custom Sound...", required: false	
                    input "soundLength", "number", title: "Maximum time to play sound (empty=use sound default)", description: "1-20", required: false        
       			}
            }
		}
        if (alarmType == "2" || (alarmType == "1" && secondAlarm =="1")) {
        	section ("Voice greeting options") {
            	input "wakeMsg", "text", title: "Wake Voice Message", defaultValue: "Good morning! It is %time% on %day%, %date%.", required: false
				href "pageWeatherSettings", title: "Weather Reporting Settings", description: getWeatherDesc(), state: greyOut()
			}
        }
        if (alarmType == "3" || (alarmType == "1" && secondAlarm =="2") || (alarmType == "2" && secondAlarmMusic)){
        	section ("Music track/internet radio options"){
            	input "musicTrack", "enum", title: "Play This Track/Internet Radio Station", required:false, multiple: false, options: songOptions()
        	}
      	}
        if (alarmSpeaker){
            section("Devices to control at alarm time") {
                input "switches", "capability.switch",title: "Turn On The Following Switches...", multiple: true, required: false, submitOnChange:true
                href "pageDimmers", title: "Dimmer Settings", description: dimmerDesc(), state: greyOutDimmer(), submitOnChange:true
                href "pageThermostats", title: "Thermostat Settings", description: tstatDesc(), state: greyOutTstat(), submitOnChange:true
                if ((switches || dimmers || thermostats) && (alarmType == "2" || (alarmType == "1" && secondAlarm =="1"))){
                    input "confirmSwitches", "bool", title: "Confirm Switches/Thermostats Status In Voice Message", defaultValue: "false"
                }
            }
            section ("Other actions at alarm time"){
                def phrases = location.helloHome?.getPhrases()*.label
                if (phrases) {
                    phrases.sort()
                    input "triggerPhrase", "enum", title: "Alarm Triggers The Following Routine", required: false, options: phrases, multiple: false, submitOnChange:true
                    if (triggerPhrase  && (alarmType == "2" || (alarmType == "1" && secondAlarm =="1"))){
                        input "confirmPhrase", "bool", title: "Confirm Routine In Voice Message", defaultValue: "false"
                    }
                }
                input "triggerMode", "mode", title: "Alarm Triggers The Following Mode", required: false, submitOnChange:true
                if (triggerMode  && (alarmType == "2" || (alarmType == "1" && secondAlarm =="1"))){
                    input "confirmMode", "bool", title: "Confirm Mode In Voice Message", defaultValue: "false"
                }
            }
        }
        section("Tap below to remove this alarm schedule only"){
		}
	}
}
def pageAlarmTriggers(){
    dynamicPage(name: "pageAlarmTriggers", title: "Alarm Triggers", install: false, uninstall: false) {
        section{
            input "alarmStart", "time", title: "At Specific Time", required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/clock.png"
            input "alarmTrigger", "capability.switch", title: "When Switches Turned On...", required: false, multiple: true,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/on.png"
            input "alarmPresenceTrigger", "capability.presenceSensor", title: "When Someone Arrives...", multiple: true, required: false, submitOnChange:true,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/people.png"
            if (alarmPresenceTrigger) input "alarmPresenceTriggerTime", "number", title: "Minutes After Presence to Alarm", defaultValue: 0, required: false
        }
    }
}
def pageRestrictions(){
    dynamicPage(name: "pageRestrictions", title: "Alarm Restrictions", install: false, uninstall: false){
        section{
            input "alarmDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...", multiple: true, required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/calendar.png"
            href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: greyOutState(timeStart, timeEnd),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/clock.png"
            input "alarmMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/modes.png"
            input "alarmPresence", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange:true,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/people.png"
            if (alarmPresence && alarmPresence.size()>1) input "alarmPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
            input "alarmSwitchActive", "capability.switch", title: "Only When Switches Are On...", multiple: true, required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/on.png"
            input "alarmSwitchNotActive", "capability.switch", title: "Only When Switches Are Off...", multiple: true, required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/off.png"
        }
    }
}
page(name: "timeIntervalInput", title: "Alarm Triggers Only During A Certain Time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
	}
} 
page(name: "pageDimmers", title: "Dimmer Settings", install: false, uninstall: false) {
	section {
       	input "dimmers", "capability.switchLevel", title: "Dim The Following...", multiple: true, required: false	
		input "dimmersLevel", "enum", options: [[5:"5%"],[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]],title: "Set dimmers to this level", multiple: false, required: false
	}
}
page(name: "pageThermostats", title: "Thermostat Settings", install: false, uninstall: false) {
	section {
       	input "thermostats", "capability.thermostat", title: "Thermostat To Control...", multiple: false, required: false
	}
    section {
        input "temperatureH", "number", title: "Heating Setpoint", required: false, description: "Temperature when in heat mode"
		input "temperatureC", "number", title: "Cooling Setpoint", required: false, description: "Temperature when in cool mode"
	}
}
def pageWeatherSettings() {
	dynamicPage(name: "pageWeatherSettings", title: "Weather Reporting Settings", install: false, uninstall: false) {
		section {
            input "includeTemp", "bool", title: "Speak Current Temperature (From Local Forecast)", defaultValue: false
        	input "localTemp", "capability.temperatureMeasurement", title: "Speak Local Temperature (From Device)", required: false, multiple: false
        	input "localHumidity", "capability.relativeHumidityMeasurement", title: "Speak Local Humidity (From Device)", required: false, multiple: false
        	input "speakWeather", "bool", title: "Speak Today's Weather Forecast", defaultValue: false
        	input "includeSunrise", "bool", title: "Speak Today's Sunrise", defaultValue: false
    		input "includeSunset", "bool", title: "Speak Today's Sunset", defaultValue: false
		}
        section ("Zip code") {
        	input "zipCode", "text", title: "Zip Code For Weather Report", required: false
		}
	}
}
def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}
def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    unschedule()
    initialize()
}
def initialize() {
	if (alarmType =="1" && soundAlarm) alarmSoundUri()
    if ((alarmStart || alarmTrigger || alarmPresenceTrigger) && alarmSpeaker && alarmType){
        if (alarmStart) schedule (alarmStart, alarmHandler)
        if (alarmTrigger) subscribe (alarmTrigger, "switch.on", alarmHandler)
        if (alarmPresenceTrigger) subscribe (alarmPresenceTrigger, "presence.present", alarmHandlerTrigger)
        if (musicTrack) saveSelectedSong()
	}
}
//Handlers----------------
def alarmHandlerTrigger(evt){
	if (!alarmPresenceTriggerTime || alarmPresenceTriggerTime == 0) alarmHandler()
    else runIn (alarmPresenceTriggerTime*60, alarmHandler, [overwrite: true])
}
def alarmHandler(evt) {
    log.debug "Alarm time: Evaluating restrictions"
    if (parent.getSchedStatus(app.id) && (!alarmMode || alarmMode.contains(location.mode)) && getDayOk() && getTimeOk(timeStart,timeEnd) && everyoneIsPresent() && switchesOnStatus() && switchesOffStatus()) {	
        if (switches || dimmers || thermostats) {
        	def dimLevel = dimmersLevel as Integer
            switches?.on()
    		dimmers?.setLevel(dimLevel)
            if (thermostats) {
        		def thermostatState = thermostats.currentThermostatMode
				if (thermostatState == "auto") {
					thermostats.setHeatingSetpoint(A_temperatureH)
					thermostats.setCoolingSetpoint(A_temperatureC)
				}
				else if (thermostatState == "heat") {
					thermostats.setHeatingSetpoint(A_temperatureH)
        			log.info "Set ${thermostats} Heat ${temperature}H°"
				}
				else {
					A_thermostats.setCoolingSetpoint(A_temperatureC)
        			log.info "Set ${thermostats} Cool ${temperature}C°"
            	}         
       		}
        }
        if (triggerPhrase) {location.helloHome.execute(triggerPhrase)}
        if (triggerMode && location.mode != triggerMode) {
			if (location.modes?.find{it.name == triggerMode}) setLocationMode(triggerMode)
            else log.debug "Unable to change to undefined mode '${triggerMode}'"
		}
        if (alarmVolume) {alarmSpeaker.setLevel(alarmVolume)}
        if (alarmType == "2" || (alarmType == "1" && secondAlarm =="1")) {
        	state.fullMsg = ""
            if (wakeMsg) getGreeting(wakeMsg)
            if (speakWeather || LocalHumidity || includeTemp || localTemp) getWeatherReport()
        	if (includeSunrise || includeSunset) getSunriseSunset()
            if ((switches || dimmers || thermostats) && confirmSwitches) {
				def msg = ""
                if ((switches || dimmers) && !thermostats) msg = "All switches"	
                if (!switches && !dimmers && thermostats) msg = "All Thermostats"
                if ((switches || dimmers) && thermostats) msg = "All switches and thermostats"
                msg = "${msg} are now on and set. "
              	compileMsg(msg)
       		}
            if (triggerPhrase && confirmPhrase) {
	        	def msg="The Smart Things routine, ${triggerPhrase}, has been activated. "
				compileMsg(msg)
        	}
            if (triggerMode && confirmMode){
            	def msg="The Smart Things mode is now being set to, ${triggerMode}. "
				compileMsg(msg)
            }
            state.sound = textToSpeech(state.fullMsg, true)
		}
        if (alarmType == "1"){
        	if (secondAlarm == "1" && state.soundAlarm) alarmSpeaker.playSoundAndTrack (state.soundAlarm.uri, state.soundAlarm.duration, state.sound.uri)	
            if (secondAlarm == "2" && state.selectedSong && state.soundAlarm) alarmSpeaker.playSoundAndTrack (state.soundAlarm.uri, state.soundAlarm.duration, state.selectedSong)
            if (!secondAlarm) alarmSpeaker.playSoundAndTrack(state.soundAlarm.uri, state.soundAlarm.duration, "")
        }
        if (alarmType == "2") {
        	if (secondAlarmMusic && state.selectedSong) alarmSpeaker.playSoundAndTrack (state.sound.uri, state.sound.duration, state.selectedSong)
            else alarmSpeaker.playTrack(state.sound.uri)
        }
        if (alarmType == "3") {
        	def text = textToSpeech(" ", true)
            alarmSpeaker.playSoundAndTrack(text.uri,text.duration,state.selectedSong)
        }
	}
}
//Common Methods-------------
def getAlarmDesc() {
	def desc = ""
	if (alarmStart || alarmTrigger || alarmPresenceTrigger) {
    	def triggerMethod = alarmStart ? "at " + parseDate(alarmStart,"", "h:mm a") : ""
        triggerMethod += triggerMethod && alarmTrigger ? " or " : ""
        triggerMethod += alarmTrigger ? "when switches are activated" : ""
        triggerMethod += triggerMethod && alarmPresenceTrigger ? " or " : ""
        if (alarmPresenceTrigger) {
        	triggerMethod += "when "
            def aptCount = alarmPresenceTrigger.size()
            def verb = aptCount == 1 ? " arrives" : " arrive"
            def nameList = ""
            for (aptName in alarmPresenceTrigger){
            	nameList += "${aptName}"
                aptCount = aptCount -1
                if (aptCount == 1) nameList += " or "
                if (aptCount > 1) nameList += ", "
            }
            triggerMethod += nameList + verb
            triggerMethod += alarmPresenceTriggerTime == 0 || !alarmPresenceTriggerTime ? "" : alarmPresenceTriggerTime > 1 ? " (after ${alarmPresenceTriggerTime} minutes)" : " (after one minute)"
        }
        desc = "Alarm plays on ${alarmSpeaker} ${triggerMethod}"		
        def dayListSize = alarmDay ? alarmDay.size() : 7            
        if (alarmDay && dayListSize < 7) {
        	desc += " on"
            for (dayName in alarmDay) {
 				desc += " ${dayName}"
    			dayListSize = dayListSize -1
                if (dayListSize) desc += ", "
        	}
        }
        else desc += " every day"   	
        if (alarmPresence){
        	def presListSize = alarmPresence.size()
            desc += " when "
            def verb = "are"
            if (presListSize > 1 && !alarmPresAll) desc += "either "
            if (presListSize > 1 && alarmPresAll) verb += " all are"
            if (presListSize == 1) verb = "is"
            desc += getPresenceNames(1)
        	desc += " ${verb} present"
        }
        if (alarmMode) {
    		def modeListSize = alarmMode.size()
        	def modePrefix = modeListSize == 1 ? " in the following mode: " : " in the following modes: "
            desc += "${modePrefix}" 
       		for (modeName in alarmMode) {
        		desc += "'${modeName}'"
    			modeListSize = modeListSize -1
            	if (modeListSize) desc += ", "
        	}
		}
     	else desc += " in all modes"
        if (alarmSwitchActive){
        	def switchOnSize = alarmSwitchActive.size()
            def switchPrefix =switchOnSize == 1 ? " when the following switch is on: " : " when the following switches are on: "
            desc += "${switchPrefix}"
            for (switchName in alarmSwitchActive) {
            	desc += "'${switchName}'"
                switchOnSize = switchOnSize -1
                if (switchOnSize) desc += ", "
			}
        }
        if (alarmSwitchNotActive){
        	def switchOffSize = alarmSwitchNotActive.size()
            def switchPrefix = switchOffSize == 1 ? " when the following switch is off: " : " when the following switches are off: "
            desc += "${switchPrefix}"
            for (switchName in alarmSwitchNotActive) {
            	desc += "'${switchName}'"
                switchOffSize = switchOffSize -1
                if (switchOffSize) desc += ", "
			}
        }
    }
    else desc = "No time or trigger for this alarm schedule set."
	desc
}
def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
    if(start && end) timeLabel = "Between " + parseDate("${start}","","h:mm a") + " and " +  parseDate("${end}","", "h:mm a")
    else if (start) timeLabel = "After " + parseDate("${start}","", "h:mm a")
    else if (end) timeLabel = "Before " + parseDate("${end}","", "h:mm a")
	timeLabel	
}
def getWeatherDesc() {
	def desc = includeTemp || localTemp ? "Speak temperature" : ""
    desc += desc && localHumidity ? " and humidity" : ""
    desc += !desc && localHumidity ? "Speak humidity" : ""
    desc += desc && (speakWeather) ? "\n" : ""
    desc += speakWeather ? "Speak the weather forecast" : ""
    desc += desc && (includeSunrise || includeSunrise) ? "\n" : ""
    desc += includeSunrise  ? "Speak sunrise" : ""
    desc += includeSunrise && includeSunset ?" and sunset" : ""
    desc += !includeSunrise && includeSunset ? "Speak sunset" : ""
    desc = desc ? desc : "Tap to setup weather reporting options"
}
def getAlarmMethod(){
	def result = ""
    if (alarmStart) result += "for ${parseDate(alarmStart, "", "h:mm a")}"
    if (alarmStart && alarmTrigger) result += ", or "
    if (alarmTrigger) result += "to trigger when switches are activated"
    if (result && alarmPresenceTrigger) result += ", or "
    if (alarmPresenceTrigger) result += "when people arrive"
	result    
}
def tstatDesc(){
	def desc = thermostats ? "${thermostats}" : "Tap to set thermostat settings"
	desc += thermostats && (temperatureH || temperatureC)  ? " set to ": ""
    desc += thermostats && temperatureH  ? "${temperatureH} (heating)" : ""
    desc += thermostats && temperatureH && temperatureC ? "/" : ""
    desc += thermostats && temperatureC ? " ${temperatureC} (cooling)" : ""
	desc
}
def getTriggersDesc(){
	def result = alarmStart ? "Time: " + parseDate(alarmStart,"", "h:mm a") : ""
    result += result && alarmTrigger   ? "\n" :""
    result += alarmTrigger ? "Switches: ${alarmTrigger}" : ""
    result += result && alarmPresenceTrigger   ? "\n" :""
    result += alarmPresenceTrigger ? "Arrival: ${alarmPresenceTrigger}" : ""
	result += alarmPresenceTrigger && alarmPresenceTriggerTime ? " (after ${alarmPresenceTriggerTime} min)" : ""
    result = result ? result : "Tap to configure alarm triggers"
}
def getRestrictionDesc(){
	def result = alarmDay ? "Days: ${alarmDay}" : ""
    result += result && getTimeLabel(timeStart, timeEnd) != "Tap to set" ? "\n" : ""
    result += getTimeLabel(timeStart, timeEnd) != "Tap to set" ? "Time: "+ getTimeLabel(timeStart, timeEnd) : ""
    result += result && alarmMode ? "\n" : ""
    result += alarmMode ? "Modes: ${alarmMode}" : ""
    result += result && alarmPresence ? "\n" : ""
    result += alarmPresence && alarmPresence.size()==1 ? "Present: ${alarmPresence}" : ""
    result += alarmPresence && alarmPresAll && alarmPresence.size()>1 ? "All Present: ${alarmPresence}" : ""
    result += alarmPresence && !alarmPresAll && alarmPresence.size()>1 ? "Any Present: ${alarmPresence}" : ""
    result += result && (alarmSwitchActive || alarmSwitchNotActive) ? "\n" : ""
    result += alarmSwitchActive ? "Switches (ON): ${alarmSwitchActive}" : ""
    result += result && alarmSwitchNotActive ? "\n" : ""
    result += alarmSwitchNotActive ? "Switches (OFF): ${alarmSwitchNotActive} " : ""
    result = result ? result : "Tap to configure alarm restrictions"
}
def greyOut(){def result = speakWeather || includeSunrise || includeSunset || includeTemp || localHumidity || localTemp? "complete" : ""}
def greyOutState(param1, param2){def result = param1 || param2 ? "complete" : ""}
def dimmerDesc(){def desc = dimmers && dimmersLevel ? "${dimmers} set to ${dimmersLevel}%" : "Tap to set dimmers settings"}
def greyOutDimmer(){def result = dimmers && dimmersLevel  ? "complete" : ""}
def greyOutRestrictions(){def result = alarmDay || alarmMode || alarmPresence || alarmSwitchActive || alarmSwitchNotActive ? "complete" : ""}
def greyOutTriggers(){def result = alarmStart || alarmTrigger || alarmPresenceTrigger ? "complete" : ""}
def greyOutTstat(){def result = thermostats && (temperatureH || temperatureC)  ? "complete" : ""}
private getPresenceNames(param){
    def nameList = ""
    if (!param){
    	def presentCount = 0
        for (sensor in alarmPresence) if (sensor.currentPresence == "present"){presentCount ++}
        for (presName in alarmPresence){
        	if (presName.currentPresence == "present"){
            	nameList += "${presName}"
                presentCount = presentCount - 1
                if (presentCount > 1) {nameList += ", "}
                if (presentCount == 1) {nameList += " and "}
            }
		}
    }
    else {
        def presListSize = alarmPresence.size()
        for (presName in alarmPresence){
            nameList += "${presName}"
            presListSize = presListSize - 1
            if (presListSize > 1) {nameList += ", "}
            if (alarmPresAll && presListSize == 1){nameList += " and "}
            if (presListSize == 1 && !alarmPresAll){nameList += " or "}
        }
	}
    nameList
}
private getDayOk() {
	def result = true
	if (alarmDay) {result = alarmDay.contains(getDay())}
    log.debug "Day ok ${result}"
    result
}
private getDay(){
	def df = new java.text.SimpleDateFormat("EEEE")
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date())
}
private everyoneIsPresent() {
    def result = true
    if (alarmPresAll && alarmPresence) result = alarmPresence.find {it.currentPresence == "not present"} ? false : true
    else if (!alarmPresAll && alarmPresence) result = alarmPresence.find {it.currentPresence == "present"}
    log.debug "Presence: ${result}"
    result
}
private switchesOnStatus(){
	def result = alarmSwitchActive && alarmSwitchActive.find{it.currentValue("switch") == "off"} ? false : true	
	log.debug "Switch on restrictions: ${result}"
    result
}
private switchesOffStatus(){
	def result = alarmSwitchNotActive && alarmSwitchNotActive.find{it.currentValue("switch") == "on"} ? false : true	
	log.debug "Switch off restrictions: ${result}"
    result
}
private getSunriseSunset(){
    if (location.timeZone || zipCode) {
    	def todayDate = new Date()
    	def s = getSunriseAndSunset(zipcode: zipCode, date: todayDate)	
		def riseTime = parseDate("", s.sunrise.time, "h:mm a")
		def setTime = parseDate ("", s.sunset.time, "h:mm a")
   		def msg = ""
    	def currTime = now()
        def verb1 = currTime >= s.sunrise.time ? "rose" : "will rise"
        def verb2 = currTime >= s.sunset.time ? "set" : "will set"
        if (includeSunrise && includeSunset) msg = "The sun ${verb1} this morning at ${riseTime} and ${verb2} at ${setTime}. "
    	else if (includeSunrise && !includeSunset) msg = "The sun ${verb1} this morning at ${riseTime}. "
    	else if (!includeSunrise && includeSunset) msg = "The sun ${verb2} tonight at ${setTime}. "
    	compileMsg(msg)
	}
	else {
		msg = "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive sunset and sunrise information. "
		compileMsg(msg)
	}
}
private getWeatherReport() {
    if (location.timeZone || zipCode) {
        def isMetric = location.temperatureScale == "C"
        def sb = new StringBuilder()
        if (includeTemp){
        	def current = getWeatherFeature("conditions", zipCode)
        	if (isMetric) sb << "The current temperature from the weather service is ${Math.round(current.current_observation.temp_c)} degrees. "
        	else sb << "The current temperature from the weather service is ${Math.round(current.current_observation.temp_f)} degrees. "
        }
        if (localTemp) sb << "The temperature from your Smart Things device, ${localTemp.label}, is ${Math.round(localTemp.currentTemperature)} degrees. "
        if (localHumidity) sb << "The relative humidity from your Smart Things device, ${localHumidity.label}, is ${localHumidity.currentValue("humidity")}%. "
        if (speakWeather) {
            def weather = getWeatherFeature("forecast", zipCode)
            sb << "Today's forecast is "
			if (isMetric) sb << weather.forecast.txt_forecast.forecastday[0].fcttext_metric 
        	else sb << weather.forecast.txt_forecast.forecastday[0].fcttext
        }
		def msg = sb.toString()
        //message pronunciation filters....
        msg = msg.replaceAll(/([0-9]+)C/,'$1 degrees')
        msg = msg.replaceAll(/([0-9]+)F/,'$1 degrees')
        msg = msg.replaceAll("0s.","0's  .")
        compileMsg(msg)
	}
	else {
		msg = "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather forecasts."
        compileMsg(msg)
    }
}
private alarmSoundUri(){
    def soundUri, newSoundLength 
    switch(soundAlarm) {
    	case "1":
        	newSoundLength = soundLength >0 && soundLength < 8 ? soundLength : 8
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmAlien.mp3", duration: "${newSoundLength}"]
        	break
        case "2":
        	newSoundLength = soundLength >0 && soundLength < 12 ? soundLength : 12
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmBell.mp3", duration: "${newSoundLength}"]
        	break
        case "3":
        	newSoundLength = soundLength >0 && soundLength < 20 ? soundLength : 20
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmBuzzer.mp3", duration: "${newSoundLength}"]
        	break
        case "4":
        	newSoundLength = soundLength >0 && soundLength < 20 ? soundLength : 20
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmFire.mp3", duration: "${newSoundLength}"]
        	break
        case "5":
        	newSoundLength = soundLength >0 && soundLength < 2 ? soundLength : 2
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmRooster.mp3", duration: "${newSoundLength}"]
        	break
        case "6":
        	newSoundLength = soundLength >0 && soundLength < 20 ? soundLength : 20
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmSiren.mp3", duration: "${newSoundLength}"]
			break
		case "7":
        	newSoundLength = soundLength >0 && soundLength < 20 ? soundLength : 20
            soundUri = [uri: "${alarmCustom}", duration: "${newSoundLength}"]
            break
    }
	state.soundAlarm = soundUri
}
private songOptions() {
    if (alarmSpeaker) {
		// Make sure current selection is in the set
		def options = new LinkedHashSet()
           	if (state.selectedSong?.station) options << state.selectedSong.station
			else if (state.selectedSong?.description) options << state.selectedSong.description
        // Query for recent tracks
		def states = alarmSpeaker.statesSince("trackData", new Date(0), [max:30])
		def dataMaps = states.collect{it.jsonValue}
		options.addAll(dataMaps.collect{it.station})
		log.trace "${options.size()} songs in list"
        options.take(20) as List
	}
}
def compileMsg(msg){
	log.debug "msg=${msg}"
    state.fullMsg = state.fullMsg + "${msg}"
}
private getGreeting(msg) {
	def day = getDay()
    def time = parseDate("", now(), "h:mm a")
    def month = parseDate("", now(), "MMMM")
    def year = parseDate("", now(), "yyyy")
    def dayNum = parseDate("", now(), "d")
	msg = msg.replace('%day%', day)
    msg = msg.replace('%date%', "${month} ${dayNum}, ${year}")
    msg = msg.replace('%time%', "${time}")
    msg = alarmPresence ? msg.replace('%people%', "${getPresenceNames()}") : msg.replace('%people%', "")
    msg = "${msg} "
    compileMsg(msg)
}
private parseDate(date, epoch, type){
    def parseDate = ""
    if (epoch){
    	long longDate = Long.valueOf(epoch).longValue()
        parseDate = new Date(longDate).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    }
    else parseDate = date
    new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", parseDate).format("${type}", timeZone(parseDate))
}
private saveSelectedSong() {
	try {
		def thisSong = musicTrack
		log.info "Looking for $thisSong"
		def songs = alarmSpeaker.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"
		def data = songs.find {s -> s.station == thisSong}
		log.info "Found ${data?.station}"
		if (data) {
			state.selectedSong = data
			log.debug "Selected song = ${data}"
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
private getTimeOk(startTime, endTime) {
	def result = true
    def currTime = now()
	def start = startTime ? timeToday(startTime).time : null
	def stop = endTime ? timeToday(endTime).time : null
	if (startTime && endTime) result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	else if (startTime) result = currTime >= start
    else if (endTime) result = currTime <= stop
    result
}
//Version
private def textVersion() {def text = "Child App Version: 1.5.5 (04/21/2016)"}