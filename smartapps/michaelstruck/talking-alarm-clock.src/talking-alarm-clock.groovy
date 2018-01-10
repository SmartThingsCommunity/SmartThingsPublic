/**
 *  Talking Alarm Clock
 *
 *  Version - 1.0.0 5/23/15
 *  Version - 1.1.0 5/24/15 - A song can now be selected to play after the voice greeting and bug fixes
 *  Version - 1.2.0 5/27/15 - Added About screen and misc code clean up and GUI revisions
 *  Version - 1.3.0 5/29/15 - Further code optimizations and addition of alarm summary action
 *  Version - 1.3.1 5/30/15 - Fixed one small code syntax issue in Scenario D
 *  Version - 1.4.0 6/7/15 -  Revised About screen, enhanced the weather forecast voice summary, added a mode change option with alarm, and added secondary alarm options
 *  Version - 1.4.1 6/9/15 - Changed the mode change speech to make it clear when the mode change is taking place  
 *  Version - 1.4.2 6/10/15 - To prevent accidental triggering of summary, put in a mode switch restriction
 *  Version - 1.4.3 6/12/15 - Syntax issues and minor GUI fixes
 *  Version - 1.4.4 6/15/15 - Fixed a bug with Phrase change at alarm time
 *  Version - 1.4.5 6/17/15 - Code optimization, implemented the new submitOnChange option and a new license agreement change
 *
 *  Copyright 2015 Michael Struck - Uses code from Lighting Director by Tim Slagle & Michael Struck
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
    name: "Talking Alarm Clock",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Control up to 4 waking schedules using a Sonos speaker as an alarm.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/Talkingclock@2x.png",
    pausable: true
)

preferences {
	page name:"pageMain"
	page name:"pageSetupScenarioA"
	page name:"pageSetupScenarioB"
	page name:"pageSetupScenarioC"
	page name:"pageSetupScenarioD"
	page name:"pageWeatherSettingsA" //technically, these 4 pages should not be dynamic, but are here to work around a crash on the Andriod app
	page name:"pageWeatherSettingsB"
	page name:"pageWeatherSettingsC"
	page name:"pageWeatherSettingsD"
}

// Show setup page
def pageMain() {
	dynamicPage(name: "pageMain", install: true, uninstall: true) {
        section ("Alarms") {
            href "pageSetupScenarioA", title: getTitle(ScenarioNameA, 1), description: getDesc(A_timeStart, A_sonos, A_day, A_mode), state: greyOut(ScenarioNameA, A_sonos, A_timeStart, A_alarmOn, A_alarmType)
            if (ScenarioNameA && A_sonos && A_timeStart && A_alarmType){
            	input "A_alarmOn", "bool", title: "Enable this alarm?", defaultValue: "true", submitOnChange:true
            }
        }
        section {
            href "pageSetupScenarioB", title: getTitle(ScenarioNameB, 2), description: getDesc(B_timeStart, B_sonos, B_day, B_mode), state: greyOut(ScenarioNameB, B_sonos, B_timeStart, B_alarmOn, B_alarmType)
            if (ScenarioNameB && B_sonos && B_timeStart  && B_alarmType){
            	input "B_alarmOn", "bool", title: "Enable this alarm?", defaultValue: "false", submitOnChange:true
        	}
        }
        section {
            href "pageSetupScenarioC", title: getTitle(ScenarioNameC, 3), description: getDesc(C_timeStart, C_sonos, C_day, C_mode), state: greyOut(ScenarioNameC, C_sonos, C_timeStart, C_alarmOn, C_alarmType)
            if (ScenarioNameC && C_sonos && C_timeStart && C_alarmType){
            	input "C_alarmOn", "bool", title: "Enable this alarm?", defaultValue: "false", submitOnChange:true
        	}
        }
        section {
            href "pageSetupScenarioD", title: getTitle(ScenarioNameD, 4), description: getDesc(D_timeStart, D_sonos, D_day, D_mode), state: greyOut(ScenarioNameD, D_sonos, D_timeStart, D_alarmOn, D_alarmType)
            if (ScenarioNameD && D_sonos && D_timeStart && D_alarmType){
            	input "D_alarmOn", "bool", title: "Enable this alarm?", defaultValue: "false", submitOnChange:true
            }
        }
        section([title:"Options", mobileOnly:true]) {
            input "alarmSummary", "bool", title: "Enable Alarm Summary", defaultValue: "false", submitOnChange:true
            if (alarmSummary) {
            	href "pageAlarmSummary", title: "Alarm Summary Settings", description: "Tap to configure alarm summary settings", state: "complete"
            }
            input "zipCode", "text", title: "Zip Code", required: false
            label title:"Assign a name", required: false
            href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
        }
    }
}

page(name: "pageAlarmSummary", title: "Alarm Summary Settings") {
	section {
       	input "summarySonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: false
        input "summaryVolume", "number", title: "Set the summary volume", description: "0-100%", required: false
        input "summaryDisabled", "bool", title: "Include disabled or unconfigured alarms in summary", defaultValue: "false"
        input "summaryMode", "mode", title: "Speak summary only during the following modes...", multiple: true, required: false
	}
}
//Show "pageSetupScenarioA" page
def pageSetupScenarioA() {
    dynamicPage(name: "pageSetupScenarioA") {
		section("Alarm settings") {
        	input "ScenarioNameA", "text", title: "Scenario Name", multiple: false, required: true
			input "A_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true, submitOnChange:true
            input "A_volume", "number", title: "Alarm volume", description: "0-100%", required: false
        	input "A_timeStart", "time", title: "Time to trigger alarm", required: true
        	input "A_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Alarm on certain days of the week...", multiple: true, required: false
    		input "A_mode", "mode", title: "Alarm only during the following modes...", multiple: true, required: false
    		input "A_alarmType", "enum", title: "Select a primary alarm type...", multiple: false, required: true, options: [[1:"Alarm sound (up to 20 seconds)"],[2:"Voice Greeting"],[3:"Music track/Internet Radio"]], submitOnChange:true
            
            if (A_alarmType != "3") {
            	if (A_alarmType == "1"){
                	input "A_secondAlarm", "enum", title: "Select a second alarm after the first is completed", multiple: false, required: false, options: [[1:"Voice Greeting"],[2:"Music track/Internet Radio"]], submitOnChange:true
                }
                if (A_alarmType == "2"){
                	input "A_secondAlarmMusic", "bool", title: "Play a track after voice greeting", defaultValue: "false", required: false, submitOnChange:true
                }
        	}
        }
        if (A_alarmType == "1"){
        	section ("Alarm sound options"){
				input "A_soundAlarm", "enum", title: "Play this sound...", required:false, multiple: false, options: [[1:"Alien-8 seconds"],[2:"Bell-12 seconds"], [3:"Buzzer-20 seconds"], [4:"Fire-20 seconds"], [5:"Rooster-2 seconds"], [6:"Siren-20 seconds"]]
				input "A_soundLength", "number", title: "Maximum time to play sound (empty=use sound default)", description: "1-20", required: false        
       		}
		}
        if (A_alarmType == "2" || (A_alarmType == "1" && A_secondAlarm =="1")) {
        	section ("Voice greeting options") {
            	input "A_wakeMsg", "text", title: "Wake voice message", defaultValue: "Good morning! It is %time% on %day%, %date%.", required: false
				href "pageWeatherSettingsA", title: "Weather Reporting Settings", description: getWeatherDesc(A_weatherReport, A_includeSunrise, A_includeSunset, A_includeTemp, A_humidity, A_localTemp), state: greyOut1(A_weatherReport, A_includeSunrise, A_includeSunset, A_includeTemp, A_humidity, A_localTemp)
			}
        }
       	if (A_alarmType == "3" || (A_alarmType == "1" && A_secondAlarm =="2") || (A_alarmType == "2" && A_secondAlarmMusic)){
        	section ("Music track/internet radio options"){
            	input "A_musicTrack", "enum", title: "Play this track/internet radio station", required:false, multiple: false, options: songOptions(A_sonos, 1)
        	}
      	}
        section("Devices to control in this alarm scenario") {
			input "A_switches", "capability.switch",title: "Control the following switches...", multiple: true, required: false, submitOnChange:true
			href "pageDimmersA", title: "Dimmer Settings", description: dimmerDesc(A_dimmers), state: greyOutOption(A_dimmers), submitOnChange:true
            href "pageThermostatsA", title: "Thermostat Settings", description: thermostatDesc(A_thermostats, A_temperatureH, A_temperatureC), state: greyOutOption(A_thermostats), submitOnChange:true
        	if ((A_switches || A_dimmers || A_thermostats) && (A_alarmType == "2" || (A_alarmType == "1" && A_secondAlarm =="1"))){
            	input "A_confirmSwitches", "bool", title: "Confirm switches/thermostats status in voice message", defaultValue: "false"
            }
        }
		section ("Other actions at alarm time"){
            def phrases = location.helloHome?.getPhrases()*.label
			if (phrases) {
				phrases.sort()
				input "A_phrase", "enum", title: "Alarm triggers the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (A_phrase  && (A_alarmType == "2" || (A_alarmType == "1" && A_secondAlarm =="1"))){
                	input "A_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
            	}
            }
            input "A_triggerMode", "mode", title: "Alarm triggers the following mode", required: false, submitOnChange:true
           	if (A_triggerMode  && (A_alarmType == "2" || (A_alarmType == "1" && A_secondAlarm =="1"))){
            	input "A_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
        }
    } 
}

page(name: "pageDimmersA", title: "Dimmer Settings") {
	section {
       	input "A_dimmers", "capability.switchLevel", title: "Dim the following...", multiple: true, required: false	
		input "A_level", "enum", options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]],title: "Set dimmers to this level", multiple: false, required: false
	}
}

page(name: "pageThermostatsA", title: "Thermostat Settings") {
	section {
       	input "A_thermostats", "capability.thermostat", title: "Thermostat to control...", multiple: false, required: false
	}
    section {
        input "A_temperatureH", "number", title: "Heating setpoint", required: false, description: "Temperature when in heat mode"
		input "A_temperatureC", "number", title: "Cooling setpoint", required: false, description: "Temperature when in cool mode"
	}
}

def pageWeatherSettingsA() {
	dynamicPage(name: "pageWeatherSettingsA", title: "Weather Reporting Settings") {
		section {
			input "A_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        	input "A_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        	input "A_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        	input "A_weatherReport", "bool", title: "Speak today's weather forecast", defaultValue: "false"
        	input "A_includeSunrise", "bool", title: "Speak today's sunrise", defaultValue: "false"
    		input "A_includeSunset", "bool", title: "Speak today's sunset", defaultValue: "false"
		}
	}
}

//Show "pageSetupScenarioB" page
def pageSetupScenarioB() {
    dynamicPage(name: "pageSetupScenarioB") {
		section("Alarm settings") {
        	input "ScenarioNameB", "text", title: "Scenario Name", multiple: false, required: true
			input "B_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true, submitOnChange:true
            input "B_volume", "number", title: "Alarm volume", description: "0-100%", required: false
        	input "B_timeStart", "time", title: "Time to trigger alarm", required: true
        	input "B_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Alarm on certain days of the week...", multiple: true, required: false
    		input "B_mode", "mode", title: "Alarm only during the following modes...", multiple: true, required: false
    		input "B_alarmType", "enum", title: "Select a primary alarm type...", multiple: false, required: true, options: [[1:"Alarm sound (up to 20 seconds)"],[2:"Voice Greeting"],[3:"Music track/Internet Radio"]], submitOnChange:true
            
            if (B_alarmType != "3") {
            	if (B_alarmType == "1"){
                	input "B_secondAlarm", "enum", title: "Select a second alarm after the first is completed", multiple: false, required: false, options: [[1:"Voice Greeting"],[2:"Music track/Internet Radio"]], submitOnChange:true
                }
                if (B_alarmType == "2"){
                	input "B_secondAlarmMusic", "bool", title: "Play a track after voice greeting", defaultValue: "false", required: false, submitOnChange:true
                }
        	}
        }
        if (B_alarmType == "1"){
        	section ("Alarm sound options"){
				input "B_soundAlarm", "enum", title: "Play this sound...", required:false, multiple: false, options: [[1:"Alien-8 seconds"],[2:"Bell-12 seconds"], [3:"Buzzer-20 seconds"], [4:"Fire-20 seconds"], [5:"Rooster-2 seconds"], [6:"Siren-20 seconds"]]
				input "B_soundLength", "number", title: "Maximum time to play sound (empty=use sound default)", description: "1-20", required: false        
       		}
		}
		if (B_alarmType == "2" || (B_alarmType == "1" && B_secondAlarm =="1")){
        	section ("Voice greeting options") {
            	input "B_wakeMsg", "text", title: "Wake voice message", defaultValue: "Good morning! It is %time% on %day%, %date%.", required: false
                href "pageWeatherSettingsB", title: "Weather Reporting Settings", description: getWeatherDesc(B_weatherReport, B_includeSunrise, B_includeSunset, B_includeTemp, B_humidity, B_localTemp), state: greyOut1(B_weatherReport, B_includeSunrise, B_includeSunset, B_includeTemp, B_humidity, B_localTemp)
        	}
        }        
       	if (B_alarmType == "3" || (B_alarmType == "1" && B_secondAlarm =="2") || (B_alarmType == "2" && B_secondAlarmMusic)){
        	section ("Music track/internet radio options"){
            	input "B_musicTrack", "enum", title: "Play this track/internet radio station", required:false, multiple: false, options: songOptions(B_sonos, 1)
        	}
      	}
        section("Devices to control in this alarm scenario") {
			input "B_switches", "capability.switch",title: "Control the following switches...", multiple: true, required: false, submitOnChange:true
			href "pageDimmersB", title: "Dimmer Settings", description: dimmerDesc(B_dimmers), state: greyOutOption(B_dimmers), submitOnChange:true
            href "pageThermostatsB", title: "Thermostat Settings", description: thermostatDesc(B_thermostats, B_temperatureH, B_temperatureC), state: greyOutOption(B_thermostats), submitOnChange:true
        	if ((B_switches || B_dimmers || B_thermostats) && (B_alarmType == "2" || (B_alarmType == "1" && B_secondAlarm =="1"))){
            	input "B_confirmSwitches", "bool", title: "Confirm switches/thermostats status in voice message", defaultValue: "false"
            }
        }
        section ("Other actions at alarm time"){
            def phrases = location.helloHome?.getPhrases()*.label
			if (phrases) {
				phrases.sort()
				input "B_phrase", "enum", title: "Alarm triggers the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (B_phrase  && (B_alarmType == "2" || (B_alarmType == "1" && B_secondAlarm =="1"))){
                	input "B_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
            	}
            }
            input "B_triggerMode", "mode", title: "Alarm triggers the following mode", required: false, submitOnChange:true
           	if (B_triggerMode  && (B_alarmType == "2" || (B_alarmType == "1" && B_secondAlarm =="1"))){
            	input "B_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
        }
    } 
}

page(name: "pageDimmersB", title: "Dimmer Settings") {
	section {
       	input "B_dimmers", "capability.switchLevel", title: "Dim the following...", multiple: true, required: false	
		input "B_level", "enum", options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]],title: "Set dimmers to this level", multiple: false, required: false
	}
}

page(name: "pageThermostatsB", title: "Thermostat Settings") {
	section {
       	input "B_thermostats", "capability.thermostat", title: "Thermostat to control...", multiple: false, required: false
	}
    section {
        input "B_temperatureH", "number", title: "Heating setpoint", required: false, description: "Temperature when in heat mode"
		input "B_temperatureC", "number", title: "Cooling setpoint", required: false, description: "Temperature when in cool mode"
	}
}

def pageWeatherSettingsB() {
	dynamicPage(name: "pageWeatherSettingsB", title: "Weather Reporting Settings") {
		section {
        	input "B_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        	input "B_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        	input "B_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        	input "B_weatherReport", "bool", title: "Speak today's weather forecast", defaultValue: "false"
			input "B_includeSunrise", "bool", title: "Speak today's sunrise", defaultValue: "false"
			input "B_includeSunset", "bool", title: "Speak today's sunset", defaultValue: "false"
		}
	}
}

//Show "pageSetupScenarioC" page
def pageSetupScenarioC() {
    dynamicPage(name: "pageSetupScenarioC") {
		section("Alarm settings") {
        	input "ScenarioNameC", "text", title: "Scenario Name", multiple: false, required: true
			input "C_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true, submitOnChange:true
            input "C_volume", "number", title: "Alarm volume", description: "0-100%", required: false
        	input "C_timeStart", "time", title: "Time to trigger alarm", required: true
        	input "C_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Alarm on certain days of the week...", multiple: true, required: false
    		input "C_mode", "mode", title: "Alarm only during the following modes...", multiple: true, required: false
    		input "C_alarmType", "enum", title: "Select a primary alarm type...", multiple: false, required: true, options: [[1:"Alarm sound (up to 20 seconds)"],[2:"Voice Greeting"],[3:"Music track/Internet Radio"]], submitOnChange:true
            
            if (C_alarmType != "3") {
            	if (C_alarmType == "1"){
                	input "C_secondAlarm", "enum", title: "Select a second alarm after the first is completed", multiple: false, required: false, options: [[1:"Voice Greeting"],[2:"Music track/Internet Radio"]], submitOnChange:true
                }
                if (C_alarmType == "2"){
                	input "C_secondAlarmMusic", "bool", title: "Play a track after voice greeting", defaultValue: "false", required: false, submitOnChange:true
                }
        	}
        }
        if (C_alarmType == "1"){
        	section ("Alarm sound options"){
				input "C_soundAlarm", "enum", title: "Play this sound...", required:false, multiple: false, options: [[1:"Alien-8 seconds"],[2:"Bell-12 seconds"], [3:"Buzzer-20 seconds"], [4:"Fire-20 seconds"], [5:"Rooster-2 seconds"], [6:"Siren-20 seconds"]]
				input "C_soundLength", "number", title: "Maximum time to play sound (empty=use sound default)", description: "1-20", required: false        
       		}
		}
        
        if (C_alarmType == "2" || (C_alarmType == "1" && C_secondAlarm =="1")) {
        	section ("Voice greeting options") {
            	input "C_wakeMsg", "text", title: "Wake voice message", defaultValue: "Good morning! It is %time% on %day%, %date%.", required: false
				href "pageWeatherSettingsC", title: "Weather Reporting Settings", description: getWeatherDesc(C_weatherReport, C_includeSunrise, C_includeSunset, C_includeTemp, A_humidity, C_localTemp), state: greyOut1(C_weatherReport, C_includeSunrise, C_includeSunset, C_includeTemp, C_humidity, C_localTemp)        	}
        }
        
       	if (C_alarmType == "3" || (C_alarmType == "1" && C_secondAlarm =="2") || (C_alarmType == "2" && C_secondAlarmMusic)){
        	section ("Music track/internet radio options"){
            	input "C_musicTrack", "enum", title: "Play this track/internet radio station", required:false, multiple: false, options: songOptions(C_sonos, 1)
        	}
      	}
        section("Devices to control in this alarm scenario") {
			input "C_switches", "capability.switch",title: "Control the following switches...", multiple: true, required: false, submitOnChange:true
			href "pageDimmersC", title: "Dimmer Settings", description: dimmerDesc(C_dimmers), state: greyOutOption(C_dimmers), submitOnChange:true
            href "pageThermostatsC", title: "Thermostat Settings", description: thermostatDesc(C_thermostats, C_temperatureH, C_temperatureC), state: greyOutOption(C_thermostats), submitOnChange:true
        	if ((C_switches || C_dimmers || C_thermostats) && (C_alarmType == "2" || (C_alarmType == "1" && C_secondAlarm =="1"))){
            	input "C_confirmSwitches", "bool", title: "Confirm switches/thermostats status in voice message", defaultValue: "false"
            }
        }
        section ("Other actions at alarm time"){
            def phrases = location.helloHome?.getPhrases()*.label
			if (phrases) {
				phrases.sort()
				input "C_phrase", "enum", title: "Alarm triggers the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (C_phrase  && (C_alarmType == "2" || (C_alarmType == "1" && C_secondAlarm =="1"))){
                	input "C_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
            	}
            }
            input "C_triggerMode", "mode", title: "Alarm triggers the following mode", required: false, submitOnChange:true
           	if (C_triggerMode  && (C_alarmType == "2" || (C_alarmType == "1" && C_secondAlarm =="1"))){
            	input "C_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
        }
    } 
}

page(name: "pageDimmersC", title: "Dimmer Settings") {
	section {
       	input "C_dimmers", "capability.switchLevel", title: "Dim the following...", multiple: true, required: false	
		input "C_level", "enum", options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]],title: "Set dimmers to this level", multiple: false, required: false
	}
}

page(name: "pageThermostatsC", title: "Thermostat Settings") {
	section {
       	input "C_thermostats", "capability.thermostat", title: "Thermostat to control...", multiple: false, required: false
	}
    section {
        input "C_temperatureH", "number", title: "Heating setpoint", required: false, description: "Temperature when in heat mode"
		input "C_temperatureC", "number", title: "Cooling setpoint", required: false, description: "Temperature when in cool mode"
	}
}

def pageWeatherSettingsC() {
	dynamicPage(name: "pageWeatherSettingsC", title: "Weather Reporting Settings") {
		section {
    		input "C_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        	input "C_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        	input "C_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        	input "C_weatherReport", "bool", title: "Speak today's weather forecast", defaultValue: "false"
        	input "C_includeSunrise", "bool", title: "Speak today's sunrise", defaultValue: "false"
    		input "C_includeSunset", "bool", title: "Speak today's sunset", defaultValue: "false"
		}
	}
}

//Show "pageSetupScenarioD" page
def pageSetupScenarioD() {
    dynamicPage(name: "pageSetupScenarioD") {
		section("Alarm settings") {
        	input "ScenarioNameD", "text", title: "Scenario Name", multiple: false, required: true
			input "D_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true, submitOnChange:true
            input "D_volume", "number", title: "Alarm volume", description: "0-100%", required: false
        	input "D_timeStart", "time", title: "Time to trigger alarm", required: true
        	input "D_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Alarm on certain days of the week...", multiple: true, required: false
    		input "D_mode", "mode", title: "Alarm only during the following modes...", multiple: true, required: false
    		input "D_alarmType", "enum", title: "Select a primary alarm type...", multiple: false, required: true, options: [[1:"Alarm sound (up to 20 seconds)"],[2:"Voice Greeting"],[3:"Music track/Internet Radio"]], submitOnChange:true
            
            if (D_alarmType != "3") {
            	if (D_alarmType == "1"){
                	input "D_secondAlarm", "enum", title: "Select a second alarm after the first is completed", multiple: false, required: false, options: [[1:"Voice Greeting"],[2:"Music track/Internet Radio"]], submitOnChange:true
                }
                if (D_alarmType == "2"){
                	input "D_secondAlarmMusic", "bool", title: "Play a track after voice greeting", defaultValue: "false", required: false, submitOnChange:true
                }
        	}
        }
        if (D_alarmType == "1"){
        	section ("Alarm sound options"){
				input "D_soundAlarm", "enum", title: "Play this sound...", required:false, multiple: false, options: [[1:"Alien-8 seconds"],[2:"Bell-12 seconds"], [3:"Buzzer-20 seconds"], [4:"Fire-20 seconds"], [5:"Rooster-2 seconds"], [6:"Siren-20 seconds"]]
				input "D_soundLength", "number", title: "Maximum time to play sound (empty=use sound default)", description: "1-20", required: false        
       		}
		}
        
        if (D_alarmType == "2" || (D_alarmType == "1" && D_secondAlarm =="1")) {
        	section ("Voice greeting options") {
            	input "D_wakeMsg", "text", title: "Wake voice message", defaultValue: "Good morning! It is %time% on %day%, %date%.", required: false
				href "pageWeatherSettingsD", title: "Weather Reporting Settings", description: getWeatherDesc(D_weatherReport, D_includeSunrise, D_includeSunset, D_includeTemp, D_humidity, D_localTemp), state: greyOut1(D_weatherReport, D_includeSunrise, D_includeSunset, D_includeTemp, D_humidity, D_localTemp)        	}
        }
        
       	if (D_alarmType == "3" || (D_alarmType == "1" && D_secondAlarm =="2") || (D_alarmType == "2" && D_secondAlarmMusic)){
        	section ("Music track/internet radio options"){
            	input "D_musicTrack", "enum", title: "Play this track/internet radio station", required:false, multiple: false, options: songOptions(D_sonos, 1)
        	}
      	}
        section("Devices to control in this alarm scenario") {
			input "D_switches", "capability.switch",title: "Control the following switches...", multiple: true, required: false, submitOnChange:true
			href "pageDimmersD", title: "Dimmer Settings", description: dimmerDesc(D_dimmers), state: greyOutOption(D_dimmers), submitOnChange:true
            href "pageThermostatsD", title: "Thermostat Settings", description: thermostatDesc(D_thermostats, D_temperatureH, D_temperatureC), state: greyOutOption(D_thermostats), submitOnChange:true
        	if ((D_switches || D_dimmers || D_thermostats) && (D_alarmType == "2" || (D_alarmType == "1" && D_secondAlarm =="1"))){
            	input "D_confirmSwitches", "bool", title: "Confirm switches/thermostats status in voice message", defaultValue: "false"
            }
        }
        section ("Other actions at alarm time"){
            def phrases = location.helloHome?.getPhrases()*.label
			if (phrases) {
				phrases.sort()
				input "D_phrase", "enum", title: "Alarm triggers the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (D_phrase  && (D_alarmType == "2" || (D_alarmType == "1" && D_secondAlarm =="1"))){
                	input "D_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
            	}
            }
            input "D_triggerMode", "mode", title: "Alarm triggers the following mode", required: false, submitOnChange:true
           	if (D_triggerMode  && (D_alarmType == "2" || (D_alarmType == "1" && D_secondAlarm =="1"))){
            	input "D_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
        }
    } 
}

page(name: "pageDimmersD", title: "Dimmer Settings") {
	section {
       	input "D_dimmers", "capability.switchLevel", title: "Dim the following...", multiple: true, required: false	
		input "D_level", "enum", options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]],title: "Set dimmers to this level", multiple: false, required: false
	}
}

page(name: "pageThermostatsD", title: "Thermostat Settings") {
	section {
       	input "D_thermostats", "capability.thermostat", title: "Thermostat to control...", multiple: false, required: false
	}
    section {
        input "D_temperatureH", "number", title: "Heating setpoint", required: false, description: "Temperature when in heat mode"
		input "D_temperatureC", "number", title: "Cooling setpoint", required: false, description: "Temperature when in cool mode"
	}
}

def pageWeatherSettingsD() {
	dynamicPage(name: "pageWeatherSettingsD", title: "Weather Reporting Settings") {
		section {
        	input "D_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
			input "D_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        	input "D_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        	input "D_weatherReport", "bool", title: "Speak today's weather forecast", defaultValue: "false"
        	input "D_includeSunrise", "bool", title: "Speak today's sunrise", defaultValue: "false"
    		input "D_includeSunset", "bool", title: "Speak today's sunset", defaultValue: "false"
		}
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

//--------------------------------------

def installed() {
    initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
	if (A_alarmType =="1"){
    	alarmSoundUri(A_soundAlarm, A_soundLength, 1)
    }
    if (B_alarmType =="1"){
    	alarmSoundUri(B_soundAlarm, B_soundLength, 2)
    }
    if (C_alarmType =="1"){
    	alarmSoundUri(C_soundAlarm, C_soundLength, 3)
    }
    if (D_alarmType =="1"){
    	alarmSoundUri(D_soundAlarm, D_soundLength, 4)
    }
    
    if (alarmSummary && summarySonos) {
		subscribe(app, appTouchHandler)
    }
    if (ScenarioNameA && A_timeStart && A_sonos && A_alarmOn && A_alarmType){
		schedule (A_timeStart, alarm_A)
        if (A_musicTrack){
        	saveSelectedSong(A_sonos, A_musicTrack, 1)
        }
	}
    if (ScenarioNameB && B_timeStart && B_sonos &&B_alarmOn && B_alarmType){
		schedule (B_timeStart, alarm_B)
        if (B_musicTrack){
        	saveSelectedSong(B_sonos, B_musicTrack, 2)
        }
	}
    if (ScenarioNameC && C_timeStart && C_sonos && C_alarmOn && C_alarmType){
		schedule (C_timeStart, alarm_C)
        if (C_musicTrack){
        	saveSelectedSong(C_sonos, C_musicTrack, 3)
        }
	}
	if (ScenarioNameD && D_timeStart && D_sonos && D_alarmOn && D_alarmType){
		schedule (D_timeStart, alarm_D)
        if (D_musicTrack){
        	saveSelectedSong(D_sonos, D_musicTrack, 4)
        }
	}    
}

//--------------------------------------

def alarm_A() {
	if ((!A_mode || A_mode.contains(location.mode)) && getDayOk(A_day)) {	
        if (A_switches || A_dimmers || A_thermostats) {
        	def dimLevel = A_level as Integer
            A_switches?.on()
    		A_dimmers?.setLevel(dimLevel)
            if (A_thermostats) {
        		def thermostatState = A_thermostats.currentThermostatMode
				if (thermostatState == "auto") {
					A_thermostats.setHeatingSetpoint(A_temperatureH)
					A_thermostats.setCoolingSetpoint(A_temperatureC)
				}
				else if (thermostatState == "heat") {
					A_thermostats.setHeatingSetpoint(A_temperatureH)
        			log.info "Set $A_thermostats Heat $A_temperatureH°"
				}
				else {
					A_thermostats.setCoolingSetpoint(A_temperatureC)
        			log.info "Set $A_thermostats Cool $A_temperatureC°"
            	}         
       		}
        }
        if (A_phrase) {
        	location.helloHome.execute(A_phrase)
        }
        
        if (A_triggerMode && location.mode != A_triggerMode) {
			if (location.modes?.find{it.name == A_triggerMode}) {
				setLocationMode(A_triggerMode)
			} 
            else {
				log.debug "Unable to change to undefined mode '${A_triggerMode}'"
			}
		}
        
        if (A_volume) {
        		A_sonos.setLevel(A_volume)
		}
        
        if (A_alarmType == "2" || (A_alarmType == "1" && A_secondAlarm =="1")) {
        	state.fullMsgA = ""
        	if (A_wakeMsg) {
	       		getGreeting(A_wakeMsg, 1)
        	}
            
            if (A_weatherReport || A_humidity || A_includeTemp || A_localTemp) {
				getWeatherReport(1, A_weatherReport, A_humidity, A_includeTemp, A_localTemp)
			}
        
        	if (A_includeSunrise || A_includeSunset) {
        		getSunriseSunset(1, A_includeSunrise, A_includeSunset)
        	}
            
            if ((A_switches || A_dimmers || A_thermostats) && A_confirmSwitches) {
				getOnConfimation(A_switches, A_dimmers, A_thermostats, 1)
       		}
            
            if (A_phrase && A_confirmPhrase) {
	        	getPhraseConfirmation(1, A_phrase)
        	}
            
            if (A_triggerMode && A_confirmMode){
            	getModeConfirmation(A_triggerMode, 1)
            }
          
            state.soundA = textToSpeech(state.fullMsgA, true)
		}
        
        if (A_alarmType == "1"){
        	if (A_secondAlarm == "1" && state.soundAlarmA){
            	A_sonos.playSoundAndTrack (state.soundAlarmA.uri, state.soundAlarmA.duration, state.soundA.uri)	
        	}
            if (A_secondAlarm == "2" && state.selectedSongA && state.soundAlarmA){
            	A_sonos.playSoundAndTrack (state.soundAlarmA.uri, state.soundAlarmA.duration, state.selectedSongA)
            }
            if (!A_secondAlarm){
            	A_sonos.playTrack(state.soundAlarmA.uri)
            }
        }
        
        if (A_alarmType == "2") {
        	if (A_secondAlarmMusic && state.selectedSongA){
            	A_sonos.playSoundAndTrack (state.soundA.uri, state.soundA.duration, state.selectedSongA)
            }
            else {
            	A_sonos.playTrack(state.soundA.uri)
            }
        }
        
        if (A_alarmType == "3") {
	       	A_sonos.playTrack(state.selectedSongA)
        }
	}
}

def alarm_B() {
	if ((!B_mode || B_mode.contains(location.mode)) && getDayOk(B_day)) {	
        if (B_switches || B_dimmers || B_thermostats) {
        	def dimLevel = B_level as Integer
            B_switches?.on()
    		B_dimmers?.setLevel(dimLevel)
            if (B_thermostats) {
        		def thermostatState = B_thermostats.currentThermostatMode
				if (thermostatState == "auto") {
					B_thermostats.setHeatingSetpoint(B_temperatureH)
					B_thermostats.setCoolingSetpoint(B_temperatureC)
				}
				else if (thermostatState == "heat") {
					B_thermostats.setHeatingSetpoint(B_temperatureH)
        			log.info "Set $B_thermostats Heat $B_temperatureH°"
				}
				else {
					B_thermostats.setCoolingSetpoint(B_temperatureC)
        			log.info "Set $B_thermostats Cool $B_temperatureC°"
            	}         
       		}
        }
        if (B_phrase) {
        	location.helloHome.execute(B_phrase)
        }
        
        if (B_triggerMode && location.mode != B_triggerMode) {
			if (location.modes?.find{it.name == B_triggerMode}) {
				setLocationMode(B_triggerMode)
			} 
            else {
				log.debug "Unable to change to undefined mode '${B_triggerMode}'"
			}
		}
        
        if (B_volume) {
        		B_sonos.setLevel(B_volume)
		}
        
        if (B_alarmType == "2" || (B_alarmType == "1" && B_secondAlarm =="1")) {
        	state.fullMsgB = ""
        	if (B_wakeMsg) {
	       		getGreeting(B_wakeMsg, 2)
        	}
            
            if (B_weatherReport || B_humidity || B_includeTemp || B_localTemp) {
				getWeatherReport(2, B_weatherReport, B_humidity, B_includeTemp, B_localTemp)
			}
        
        	if (B_includeSunrise || B_includeSunset) {
        		getSunriseSunset(2, B_includeSunrise, B_includeSunset)
        	}
            
            if ((B_switches || B_dimmers || B_thermostats) && B_confirmSwitches) {
				getOnConfimation(B_switches, B_dimmers, B_thermostats, 2)
       		}
            
            if (B_phrase && B_confirmPhrase) {
	        	getPhraseConfirmation(2, B_phrase)
        	}
            
            if (B_triggerMode && B_confirmMode){
            	getModeConfirmation(B_triggerMode, 2)
            }
          
            state.soundB = textToSpeech(state.fullMsgB, true)
		}
        
        if (B_alarmType == "1"){
        	if (B_secondAlarm == "1" && state.soundAlarmB) {
            	B_sonos.playSoundAndTrack (state.soundAlarmB.uri, state.soundAlarmB.duration, state.soundB.uri)	
        	}
            if (B_secondAlarm == "2" && state.selectedSongB && state.soundAlarmB){
            	B_sonos.playSoundAndTrack (state.soundAlarmB.uri, state.soundAlarmB.duration, state.selectedSongB)
            }
            if (!B_secondAlarm){
            	B_sonos.playTrack(state.soundAlarmB.uri)
            }
        }
        
        if (B_alarmType == "2") {
        	if (B_secondAlarmMusic && state.selectedSongB){
            	B_sonos.playSoundAndTrack (state.soundB.uri, state.soundB.duration, state.selectedSongB)
            }
            else {
            	B_sonos.playTrack(state.soundB.uri)
            }
        }
        
        if (B_alarmType == "3") {
	       	B_sonos.playTrack(state.selectedSongB)
        }
	}
}

def alarm_C() {
	if ((!C_mode || C_mode.contains(location.mode)) && getDayOk(C_day)) {	
        if (C_switches || C_dimmers || C_thermostats) {
        	def dimLevel = C_level as Integer
            C_switches?.on()
    		C_dimmers?.setLevel(dimLevel)
            if (C_thermostats) {
        		def thermostatState = C_thermostats.currentThermostatMode
				if (thermostatState == "auto") {
					C_thermostats.setHeatingSetpoint(C_temperatureH)
					C_thermostats.setCoolingSetpoint(C_temperatureC)
				}
				else if (thermostatState == "heat") {
					C_thermostats.setHeatingSetpoint(C_temperatureH)
        			log.info "Set $C_thermostats Heat $C_temperatureH°"
				}
				else {
					C_thermostats.setCoolingSetpoint(C_temperatureC)
        			log.info "Set $C_thermostats Cool $C_temperatureC°"
            	}         
       		}
        }
        if (C_phrase) {
        	location.helloHome.execute(C_phrase)
        }
        
        if (C_triggerMode && location.mode != C_triggerMode) {
			if (location.modes?.find{it.name == C_triggerMode}) {
				setLocationMode(C_triggerMode)
			} 
            else {
				log.debug "Unable to change to undefined mode '${C_triggerMode}'"
			}
		}
        
        if (C_volume) {
        		C_sonos.setLevel(C_volume)
		}
        
        if (C_alarmType == "2" || (C_alarmType == "1" && C_secondAlarm =="1")) {
        	state.fullMsgC = ""
        	if (C_wakeMsg) {
	       		getGreeting(C_wakeMsg, 3)
        	}
            
            if (C_weatherReport || C_humidity || C_includeTemp || C_localTemp) {
				getWeatherReport(3, C_weatherReport, C_humidity, C_includeTemp, C_localTemp)
			}
        
        	if (C_includeSunrise || C_includeSunset) {
        		getSunriseSunset(3, C_includeSunrise, C_includeSunset)
        	}
            
            if ((C_switches || C_dimmers || C_thermostats) && C_confirmSwitches) {
				getOnConfimation(C_switches, C_dimmers, C_thermostats, 3)
       		}
            
            if (C_phrase && C_confirmPhrase) {
	        	getPhraseConfirmation(3, C_phrase)
        	}
            
            if (C_triggerMode && C_confirmMode){
            	getModeConfirmation(C_triggerMode, 3)
            }
          
            state.soundC = textToSpeech(state.fullMsgC, true)
		}
        
        if (C_alarmType == "1"){
        	if (C_secondAlarm == "1" && state.soundAlarmC){
            	C_sonos.playSoundAndTrack (state.soundAlarmC.uri, state.soundAlarmC.duration, state.soundC.uri)	
        	}
            if (C_secondAlarm == "2" && state.selectedSongC && state.soundAlarmC){
            	C_sonos.playSoundAndTrack (state.soundAlarmC.uri, state.soundAlarmC.duration, state.selectedSongC)
            }
            if (!C_secondAlarm){
            	C_sonos.playTrack(state.soundAlarmC.uri)
            }
        }
        
        if (C_alarmType == "2") {
        	if (C_secondAlarmMusic && state.selectedSongC){
            	C_sonos.playSoundAndTrack (state.soundC.uri, state.soundC.duration, state.selectedSongC)
            }
            else {
            	C_sonos.playTrack(state.soundC.uri)
            }
        }
        
        if (C_alarmType == "3") {
	       	C_sonos.playTrack(state.selectedSongC)
        }
	}
}

def alarm_D() {
	if ((!D_mode || D_mode.contains(location.mode)) && getDayOk(D_day)) {	
        if (D_switches || D_dimmers || D_thermostats) {
        	def dimLevel = D_level as Integer
            D_switches?.on()
    		D_dimmers?.setLevel(dimLevel)
            if (D_thermostats) {
        		def thermostatState = D_thermostats.currentThermostatMode
				if (thermostatState == "auto") {
					D_thermostats.setHeatingSetpoint(D_temperatureH)
					D_thermostats.setCoolingSetpoint(D_temperatureC)
				}
				else if (thermostatState == "heat") {
					D_thermostats.setHeatingSetpoint(D_temperatureH)
        			log.info "Set $D_thermostats Heat $D_temperatureH°"
				}
				else {
					D_thermostats.setCoolingSetpoint(D_temperatureC)
        			log.info "Set $D_thermostats Cool $D_temperatureC°"
            	}         
       		}
        }
        if (D_phrase) {
        	location.helloHome.execute(D_phrase)
        }
        
        if (D_triggerMode && location.mode != D_triggerMode) {
			if (location.modes?.find{it.name == D_triggerMode}) {
				setLocationMode(D_triggerMode)
			} 
            else {
				log.debug "Unable to change to undefined mode '${D_triggerMode}'"
			}
		}
        
        if (D_volume) {
        		D_sonos.setLevel(D_volume)
		}
        
        if (D_alarmType == "2" || (D_alarmType == "1" && D_secondAlarm =="1")) {
        	state.fullMsgD = ""
        	if (D_wakeMsg) {
	       		getGreeting(D_wakeMsg, 4)
        	}
            
            if (D_weatherReport || D_humidity || D_includeTemp || D_localTemp) {
				getWeatherReport(4, D_weatherReport, D_humidity, D_includeTemp, D_localTemp)
			}
        
        	if (D_includeSunrise || D_includeSunset) {
        		getSunriseSunset(4, D_includeSunrise, D_includeSunset)
        	}
            
            if ((D_switches || D_dimmers || D_thermostats) && D_confirmSwitches) {
				getOnConfimation(D_switches, D_dimmers, D_thermostats, 4)
       		}
            
            if (D_phrase && D_confirmPhrase) {
	        	getPhraseConfirmation(4, D_phrase)
        	}
            
            if (D_triggerMode && D_confirmMode){
            	getModeConfirmation(D_triggerMode, 4)
            }
          
            state.soundD = textToSpeech(state.fullMsgD, true)
		}
        
        if (D_alarmType == "1"){
        	if (D_secondAlarm == "1" && state.soundAlarmD){
            	D_sonos.playSoundAndTrack (state.soundAlarmD.uri, state.soundAlarmD.duration, state.soundD.uri)	
        	}
            if (D_secondAlarm == "2" && state.selectedSongD && state.soundAlarmD){
            	D_sonos.playSoundAndTrack (state.soundAlarmD.uri, state.soundAlarmD.duration, state.selectedSongD)
            }
            if (!D_secondAlarm){
            	D_sonos.playTrack(state.soundAlarmD.uri)
            }
        }
        
        if (D_alarmType == "2") {
        	if (D_secondAlarmMusic && state.selectedSongD){
            	D_sonos.playSoundAndTrack (state.soundD.uri, state.soundD.duration, state.selectedSongD)
            }
            else {
            	D_sonos.playTrack(state.soundD.uri)
            }
        }
        
        if (D_alarmType == "3") {
	       	D_sonos.playTrack(state.selectedSongD)
        }
	}
}

def appTouchHandler(evt){
	if (!summaryMode || summaryMode.contains(location.mode)) {
    	state.summaryMsg = "The following is a summary of the alarm settings. "
		getSummary (A_alarmOn, ScenarioNameA, A_timeStart, 1)
    	getSummary (B_alarmOn, ScenarioNameB, B_timeStart, 2)
    	getSummary (C_alarmOn, ScenarioNameC, C_timeStart, 3)
    	getSummary (D_alarmOn, ScenarioNameD, D_timeStart, 4)
	
    	log.debug "Summary message = ${state.summaryMsg}"
		def summarySound = textToSpeech(state.summaryMsg, true)
    	if (summaryVolume) {
    		summarySonos.setLevel(summaryVolume)
		}
    	summarySonos.playTrack(summarySound.uri)
	}
}

def getSummary (alarmOn, scenarioName, timeStart, num){
    if (alarmOn && scenarioName) {
        state.summaryMsg = "${state.summaryMsg} Alarm ${num}, ${scenarioName}, set for ${parseDate(timeStart,"", "h:mm a" )}, is enabled. "
    }
    else if (summaryDisabled && !alarmOn && scenarioName) {
        state.summaryMsg = "${state.summaryMsg} Alarm ${num}, ${scenarioName}, set for ${parseDate(timeStart,"", "h:mm a")}, is disabled. "
    }
    else if (summaryDisabled && !scenarioName) {
        state.summaryMsg = "${state.summaryMsg} Alarm ${num} is not configured. "
    }
}

//--------------------------------------

def getDesc(timeStart, sonos, day, mode) {
	def desc = "Tap to set alarm"
	if (timeStart) {
    	desc = "Alarm set to " + parseDate(timeStart,"", "h:mm a") +" on ${sonos}"
		
        def dayListSize = day ? day.size() : 7
             
        if (day && dayListSize < 7) {
        	desc = desc + " on"
            for (dayName in day) {
 				desc = desc + " ${dayName}"
    			dayListSize = dayListSize -1
                if (dayListSize) {
            		desc = "${desc}, "
        		}
        	}
        }
        else {
    		desc = desc + " every day"
    	}
    	
        if (mode) {
    		def modeListSize = mode.size()
        	def modePrefix =" in the following modes: "
        	if (modeListSize == 1) {
        		modePrefix = " in the following mode: "
        	}
            desc = desc + "${modePrefix}" 
       		for (modeName in mode) {
        		desc = desc + "'${modeName}'"
    			modeListSize = modeListSize -1
            	if (modeListSize) {
            		desc = "${desc}, "
            	}
            	else {
            		desc = "${desc}"
        		}
        	}
		}
     	else {
    		desc = desc + " in all modes"
        }
    }
	desc	
}
def greyOut(scenario, sonos, alarmTime, alarmOn, alarmType){
	def result = scenario && sonos  && alarmTime && alarmOn && alarmType ? "complete" : ""
}

def greyOut1(param1, param2, param3, param4, param5, param6){
	def result = param1 || param2 || param3 || param4 || param5 || param6 ? "complete" : ""
}

def getWeatherDesc(param1, param2, param3, param4, param5, param6) {
	def title = param1 || param2 || param3 || param4 || param5 || param6 ? "Tap to edit weather reporting options" : "Tap to setup weather reporting options"
}

def greyOutOption(param){
	def result = param ? "complete" : ""
}

def getTitle(scenario, num) {
	def title = scenario ? scenario : "Alarm ${num} not configured"
}

def dimmerDesc(dimmer){
	def desc = dimmer ? "Tap to edit dimmer settings" : "Tap to set dimmer setting"
}

def thermostatDesc(thermostat, heating, cooling){
	def tempText 
    if (heating || cooling){
    	if (heating){
        	tempText = "${heating} heat"
        }
        if (cooling){
        	tempText = "${cooling} cool"
        }
		if (heating && cooling) {
        	tempText ="${heating} heat / ${cooling} cool"
        }
    }
    else {
    	tempText="Tap to edit thermostat settings"
    }
    
    def desc = thermostat ? "${tempText}" : "Tap to set thermostat settings"
	return desc
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

private parseDate(date, epoch, type){
    def parseDate = ""
    if (epoch){
    	long longDate = Long.valueOf(epoch).longValue()
        parseDate = new Date(longDate).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    }
    else {
    	parseDate = date
    }
    new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", parseDate).format("${type}", timeZone(parseDate))
}

private getSunriseSunset(scenario, includeSunrise, includeSunset){
	if (location.timeZone || zipCode) {
    	def todayDate = new Date()
    	def s = getSunriseAndSunset(zipcode: zipCode, date: todayDate)	
		def riseTime = parseDate("", s.sunrise.time, "h:mm a")
		def setTime = parseDate ("", s.sunset.time, "h:mm a")
   		def msg = ""
    	def currTime = now()
        def verb1 = currTime >= s.sunrise.time ? "rose" : "will rise"
        def verb2 = currTime >= s.sunset.time ? "set" : "will set"
       
        if (includeSunrise && includeSunset) {
			msg = "The sun ${verb1} this morning at ${riseTime} and ${verb2} at ${setTime}. "
    	}
    	else if (includeSunrise && !includeSunset) {
    		msg = "The sun ${verb1} this morning at ${riseTime}. "
    	}
    	else if (!includeSunrise && includeSunset) {
    		msg = "The sun ${verb2} tonight at ${setTime}. "
    	}
    	compileMsg(msg, scenario)
	}
	else {
		msg = "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive sunset and sunrise information. "
		compileMsg(msg, scenario)
	}
}

private getGreeting(msg, scenario) {
	def day = getDay()
    def time = parseDate("", now(), "h:mm a")
    def month = parseDate("", now(), "MMMM")
    def year = parseDate("", now(), "yyyy")
    def dayNum = parseDate("", now(), "dd")
	msg = msg.replace('%day%', day)
    msg = msg.replace('%date%', "${month} ${dayNum}, ${year}")
    msg = msg.replace('%time%', "${time}")
    msg = "${msg} "
    compileMsg(msg, scenario)
}

private getWeatherReport(scenario, weatherReport, humidity, includeTemp, localTemp) {
	if (location.timeZone || zipCode) {
		def isMetric = location.temperatureScale == "C"
        def sb = new StringBuilder()
		
        if (includeTemp){
        	def current = getWeatherFeature("conditions", zipCode)
        	if (isMetric) {
        		sb << "The current temperature is ${Math.round(current.current_observation.temp_c)} degrees. "
        	}
        	else {
        		sb << "The current temperature is ${Math.round(current.current_observation.temp_f)} degrees. "
        	}
        }
        
        if (localTemp){
			sb << "The local temperature is ${Math.round(localTemp.currentTemperature)} degrees. "
        }

        if (humidity) {
        	sb << "The local relative humidity is ${humidity.currentValue("humidity")}%. "
        }
        
        if (weatherReport) {
           	def weather = getWeatherFeature("forecast", zipCode)
            
            sb << "Today's forecast is "
			if (isMetric) {
        		sb << weather.forecast.txt_forecast.forecastday[0].fcttext_metric 
        	}
        	else {
          		sb << weather.forecast.txt_forecast.forecastday[0].fcttext
        	}
        }
        
		def msg = sb.toString()
        msg = msg.replaceAll(/([0-9]+)C/,'$1 degrees')
        msg = msg.replaceAll(/([0-9]+)F/,'$1 degrees')
        compileMsg(msg, scenario)		
	}
	else {
		msg = "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather forecasts."
		compileMsg(msg, scenario)
    }
}

private getOnConfimation(switches, dimmers, thermostats, scenario) {
	def msg = ""
    if ((switches || dimmers) && !thermostats) {
    	msg = "All switches"	
    }
    if (!switches && !dimmers && thermostats) {
    	msg = "All Thermostats"
    }
    if ((switches || dimmers) && thermostats) {
    	msg = "All switches and thermostats"
    } 
    msg = "${msg} are now on and set. "
    compileMsg(msg, scenario)
}

private getPhraseConfirmation(scenario, phrase) {
	def msg="The Smart Things Hello Home phrase, ${phrase}, has been activated. "
	compileMsg(msg, scenario)
}

private getModeConfirmation(mode, scenario) {
	def msg="The Smart Things mode is now being set to, ${mode}. "
	compileMsg(msg, scenario)
}

private compileMsg(msg, scenario) {
	log.debug "msg = ${msg}"
	if (scenario == 1) {state.fullMsgA = state.fullMsgA + "${msg}"}
	if (scenario == 2) {state.fullMsgB = state.fullMsgB + "${msg}"}
	if (scenario == 3) {state.fullMsgC = state.fullMsgC + "${msg}"}
	if (scenario == 4) {state.fullMsgD = state.fullMsgD + "${msg}"}
}

private alarmSoundUri(selection, length, scenario){
	def soundUri = ""
   	def soundLength = ""
    switch(selection) {
    	case "1":
        	soundLength = length >0 && length < 8 ? length : 8
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmAlien.mp3", duration: "${soundLength}"]
        	break
        case "2":
        	soundLength = length >0 && length < 12 ? length : 12
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmBell.mp3", duration: "${soundLength}"]
        	break
        case "3":
        	soundLength = length >0 && length < 20 ? length : 20
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmBuzzer.mp3", duration: "${soundLength}"]
        	break
        case "4":
        	soundLength = length >0 && length < 20 ? length : 20
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmFire.mp3", duration: "${soundLength}"]
        	break
        case "5":
        	soundLength = length >0 && length < 2 ? length : 2
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmRooster.mp3", duration: "${soundLength}"]
        	break
        case "6":
        	soundLength = length >0 && length < 20 ? length : 20
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Talking-Alarm-Clock/AlarmSounds/AlarmSiren.mp3", duration: "${soundLength}"]
			break
    }
	if (scenario == 1) {state.soundAlarmA = soundUri}
	if (scenario == 2) {state.soundAlarmB = soundUri}
	if (scenario == 3) {state.soundAlarmC = soundUri}
	if (scenario == 4) {state.soundAlarmD = soundUri}		
}	

//Sonos Aquire Track from SmartThings code
private songOptions(sonos, scenario) {
	if (sonos){
	// Make sure current selection is in the set
		def options = new LinkedHashSet()
		if (scenario == 1){
	    	if (state.selectedSongA?.station) {
				options << state.selectedSongA.station
			}
			else if (state.selectedSongA?.description) {
				options << state.selectedSongA.description
			}
    	}
    	if (scenario == 2){
    		if (state.selectedSongB?.station) {
				options << state.selectedSongB.station
			}
			else if (state.selectedSongB?.description) {
				options << state.selectedSongB.description
			}
    	}
    	if (scenario == 3){
    		if (state.selectedSongC?.station) {
				options << state.selectedSongC.station
			}
			else if (state.selectedSongC?.description) {
				options << state.selectedSongC.description
			}
    	}
    	if (scenario == 4){
    		if (state.selectedSongD?.station) {
				options << state.selectedSongD.station
			}
			else if (state.selectedSongD?.description) {
				options << state.selectedSongD.description
			}
    	}
		// Query for recent tracks
		def states = sonos.statesSince("trackData", new Date(0), [max:30])
		def dataMaps = states.collect{it.jsonValue}
		options.addAll(dataMaps.collect{it.station})

		log.trace "${options.size()} songs in list"
		options.take(20) as List
	}
}

private saveSelectedSong(sonos, song, scenario) {
	try {
		def thisSong = song
		log.info "Looking for $thisSong"
		def songs = sonos.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"

		def data = songs.find {s -> s.station == thisSong}
		log.info "Found ${data?.station}"
		if (data) {
			if (scenario == 1) {state.selectedSongA = data}
            if (scenario == 2) {state.selectedSongB = data}
            if (scenario == 3) {state.selectedSongC = data}
            if (scenario == 4) {state.selectedSongD = data}
			log.debug "Selected song for Scenario ${scenario} = ${data}"
		}
		else if (song == state.selectedSongA?.station || song == state.selectedSongB?.station || song == state.selectedSongC?.station || song == state.selectedSongD?.station) {
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

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Talking Alarm Clock"
}	

private def textVersion() {
    def text = "Version 1.4.5 (06/17/2015)"
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
    	"Within each alarm scenario, choose a Sonos speaker, an alarm time and alarm type along with " +
        "switches, dimmers and thermostat to control when the alarm is triggered. Hello, Home phrases and modes can be triggered at alarm time. "+
        "You also have the option of setting up different alarm sounds, tracks and a personalized spoken greeting that can include a weather report. " +
        "Variables that can be used in the voice greeting include %day%, %time% and %date%.\n\n"+
        "From the main SmartApp convenience page, tapping the 'Talking Alarm Clock' icon (if enabled within the app) will "+
        "speak a summary of the alarms enabled or disabled without having to go into the application itself. This " +
        "functionality is optional and can be configured from the main setup page."
}

