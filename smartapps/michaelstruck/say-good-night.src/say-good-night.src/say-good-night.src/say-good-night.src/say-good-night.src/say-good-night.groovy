/**
 *  Say Good Night!
 *
 *  Version - 1.0.2 6/26/15
 *  
 * 
 *  Copyright 2015 Michael Struck - Uses code from Lighting Director by Tim Slagle & Michael Struck
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

import groovy.json.JsonSlurper

definition(
    name: "Say Good Night!",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Control up to 5 'Good Night' scenarios using Sonos speakers and various triggers and actions.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Say-Good-Night/SayGoodNight.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Say-Good-Night/SayGoodNight@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Say-Good-Night/SayGoodNight@2x.png"
    )

preferences {
	page name: "pageMain"
	page name: "pageSetupScenarioA"
    page name: "pageSetupScenarioB"
    page name: "pageSetupScenarioC"
    page name: "pageSetupScenarioD"
    page name: "pageSetupScenarioE"
    page name: "pageWeatherSettingsA"
    page name: "pageWeatherSettingsB"
    page name: "pageWeatherSettingsC"
    page name: "pageWeatherSettingsD"
    page name: "pageWeatherSettingsE"
    page name: "pageDoorsWindowsA"  
    page name: "pageDoorsWindowsB"
    page name: "pageDoorsWindowsC"
    page name: "pageDoorsWindowsD"
    page name: "pageDoorsWindowsE"
}

// Show setup page
def pageMain() {
	dynamicPage(name: "pageMain", install: true, uninstall: true) {
        section ("Good Night Scenarios") {
            href "pageSetupScenarioA", title: getTitle(ScenarioNameA, 1), description: "", state: greyOut(ScenarioNameA, A_sonos)
            href "pageSetupScenarioB", title: getTitle(ScenarioNameB, 2), description: "", state: greyOut(ScenarioNameB, B_sonos)
            href "pageSetupScenarioC", title: getTitle(ScenarioNameC, 3), description: "", state: greyOut(ScenarioNameC, C_sonos)
            href "pageSetupScenarioD", title: getTitle(ScenarioNameD, 4), description: "", state: greyOut(ScenarioNameD, D_sonos)
            href "pageSetupScenarioE", title: getTitle(ScenarioNameE, 5), description: "", state: greyOut(ScenarioNameE, E_sonos)
        }
 		section([title:"Options", mobileOnly:true]) {
            input "zipCode", "text", title: "Zip Code", required: false
            label title:"Assign a name", required:false
            href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license and instructions"
        }
    }
}

// Show "pageSetupScenarioA" page
def pageSetupScenarioA() {
    dynamicPage(name: "pageSetupScenarioA") {
		section("Scenario Settings") {
        	input "ScenarioNameA", "text", title: "Scenario Name", required: false
			input "A_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true
    	}
		section("Say 'Good Night' when...") {
			input "A_switches", "capability.switch",title: "Any of these switches are turned off...", multiple: true, required: false
            href "pageButtonControlA", title: "A button is pressed on a controller...", description: buttonDesc(A_buttonDevice, A_buttonPress), state: greyOut(A_buttonDevice, A_buttonPress)
        }
        section("Voice Reporting Options") {
        	input "A_volume", "number", title: "Set the alarm volume", description: "0-100%", required: false
        	href "pageWeatherSettingsA", title: "Weather Reporting Settings", description: getWeatherDesc(A_weatherReport, A_includeSunrise, A_includeSunset, A_includeTemp, A_humidity, A_localTemp), state: greyOut1(A_weatherReport, A_includeSunrise, A_includeSunset, A_includeTemp, A_humidity, A_localTemp)
        	href "pageDoorsWindowsA", title: "Doors/Windows Reporting Settings", description: getDoorsDesc(A_contactSensors, A_doorControls, A_locks),state: greyOut3(A_contactSensors, A_doorControls, A_locks)
            input "A_msg", "text", title: "Good night message", defaultValue: "Good Night!", required: false
            input "A_msgFirst", "bool", title: "Play good night message before other voice messages", defaultValue: "false"
        }
        section("Good Night Actions"){
            def phrases = location.helloHome?.getPhrases()*.label
            if (phrases) {
				phrases.sort()
				input "A_phrase", "enum", title: "Trigger the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (A_phrase) {
                	input "A_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
                }
            }
        }
        section{
        	input "A_triggerMode", "mode", title: "Trigger the following mode", required: false, submitOnChange:true
            if (A_triggerMode){
            	input "A_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
        }
    	section("Restrictions") {            
        	input "A_triggerOnce","bool",title: "Trigger only once per day...", defaultValue: false
        	href "timeIntervalInputA", title: "Only during a certain time...", description: getTimeLabel(A_timeStart, A_timeEnd), state:greyOut2(A_timeStart, A_timeEnd)
        	input "A_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        	input "A_mode", "mode", title: "Only during certain modes...", multiple: true, required: false
        }
    } 
}

page(name: "pageButtonControlA", title: "Button Controller Setup") {
	section {
    	input "A_buttonDevice", "capability.button", title: "Button Controller", multiple: false, required: false
        input "A_buttonPress", "enum", title: "Which button...", options: [1:"1", 2:"2", 3:"3", 4:"4"], multiple: false, required: false
	}
}

page(name: "timeIntervalInputA", title: "Only during a certain time") {
	section {
		input "A_timeStart", "time", title: "Starting", required: false
		input "A_timeEnd", "time", title: "Ending", required: false
	}
}

def pageWeatherSettingsA() {
    dynamicPage(name: "pageWeatherSettingsA", title: "Weather Reporting Settings") {
		section {
        	input "A_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        	input "A_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        	input "A_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        	input "A_weatherReport", "bool", title: "Speak tomorrow's weather forecast", defaultValue: "false"
        	input "A_includeSunrise", "bool", title: "Speak tomorrow's sunrise", defaultValue: "false"
    		input "A_includeSunset", "bool", title: "Speak tomorrow's sunset", defaultValue: "false"
		}
	}
}

def pageDoorsWindowsA() {
    dynamicPage(name: "pageDoorsWindowsA", title: "Doors/Windows Reporting Settings"){
		section {
  			input "A_reportAll", "bool", title: "Report status even when closed and locked", defaultValue: "false"
        	input "A_contactSensors", "capability.contactSensor", title: "Which Doors/Windows...", multiple: true, required: false
            input "A_doorControls", "capability.doorControl", title: "Which Door Controls...", multiple: true, required: false
       		input "A_locks", "capability.lock", title: "Which Locks...", multiple: true, required: false
   		}
	}
}

// Show "pageSetupScenarioB" page
def pageSetupScenarioB() {
    dynamicPage(name: "pageSetupScenarioB") {
		section("Scenario Settings") {
        	input "ScenarioNameB", "text", title: "Scenario Name", required: false
			input "B_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true
    	}
		section("Say 'Good Night' when...") {
			input "B_switches", "capability.switch",title: "Any of these switches are turned off...", multiple: true, required: false
            href "pageButtonControlB", title: "A button is pressed on a controller...", description: buttonDesc(B_buttonDevice, B_buttonPress), state: greyOut(B_buttonDevice, B_buttonPress)
        }
        section("Voice Reporting Options") {
        	input "B_volume", "number", title: "Set the alarm volume", description: "0-100%", required: false
        	href "pageWeatherSettingsB", title: "Weather Reporting Settings", description: getWeatherDesc(B_weatherReport, B_includeSunrise, B_includeSunset, B_includeTemp, B_humidity, B_localTemp), state: greyOut1(B_weatherReport, B_includeSunrise, B_includeSunset, B_includeTemp, B_humidity, B_localTemp)
        	href "pageDoorsWindowsB", title: "Doors/Windows Reporting Settings", description: getDoorsDesc(B_contactSensors, B_doorControls, B_locks),state: greyOut3(B_contactSensors, B_doorControls, B_locks)
            input "B_msg", "text", title: "Good night message", defaultValue: "Good Night!", required: false
            input "B_msgFirst", "bool", title: "Play good night message before other voice messages", defaultValue: "false"
        }
        section("Good Night Actions"){
            def phrases = location.helloHome?.getPhrases()*.label
            if (phrases) {
				phrases.sort()
				input "B_phrase", "enum", title: "Trigger the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (B_phrase){
                	input "B_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
                }
            }
		}
        section{
            input "B_triggerMode", "mode", title: "Trigger the following mode", required: false, submitOnChange:true
            if (B_triggerMode){
            	input "B_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
		}
    	section("Restrictions") {            
        	input "B_triggerOnce", "bool",title: "Trigger only once per day...", defaultValue: false
        	href "timeIntervalInputB", title: "Only during a certain time...", description: getTimeLabel(B_timeStart, B_timeEnd), state:greyOut2(B_timeStart, B_timeEnd)
        	input "B_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        	input "B_mode", "mode", title: "Only during certain modes...", multiple: true, required: false
        }
    } 
}

page(name: "pageButtonControlB", title: "Button Controller Setup") {
	section {
    	input "B_buttonDevice", "capability.button", title: "Button Controller", multiple: false, required: false
        input "B_buttonPress", "enum", title: "Which button...", options: [1:"1", 2:"2", 3:"3", 4:"4"], multiple: false, required: false
	}
}

page(name: "timeIntervalInputB", title: "Only during a certain time") {
	section {
		input "B_timeStart", "time", title: "Starting", required: false
		input "B_timeEnd", "time", title: "Ending", required: false
	}
}

def pageWeatherSettingsB() {
    dynamicPage(name:"pageWeatherSettingsB", title: "Weather Reporting Settings") {
	section {
		input "B_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        input "B_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        input "B_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        input "B_weatherReport", "bool", title: "Speak tomorrow's weather forecast", defaultValue: "false"
        input "B_includeSunrise", "bool", title: "Speak tomorrow's sunrise", defaultValue: "false"
    	input "B_includeSunset", "bool", title: "Speak tomorrow's sunset", defaultValue: "false"
	}
}
}

def pageDoorsWindowsB() {
    dynamicPage(name: "pageDoorsWindowsB", title: "Doors/Windows Reporting Settings"){
		section {
  			input "B_reportAll", "bool", title: "Report status even when closed and locked", defaultValue: "false"
        	input "B_contactSensors", "capability.contactSensor", title: "Which Doors/Windows...", multiple: true, required: false
            input "B_doorControls", "capability.doorControl", title: "Which Door Controls...", multiple: true, required: false
       		input "B_locks", "capability.lock", title: "Which Locks...", multiple: true, required: false
   		}
	}
}
// Show "pageSetupScenarioC" page
def pageSetupScenarioC() {
    dynamicPage(name: "pageSetupScenarioC") {
		section("Scenario Settings") {
        	input "ScenarioNameC", "text", title: "Scenario Name", required: false
			input "C_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true
    	}
		section("Say 'Good Night' when...") {
			input "C_switches", "capability.switch",title: "Any of these switches are turned off...", multiple: true, required: false
            href "pageButtonControlC", title: "A button is pressed on a controller...", description: buttonDesc(C_buttonDevice, C_buttonPress), state: greyOut(C_buttonDevice, C_buttonPress)
        }
        section("Voice Reporting Options") {
        	input "C_volume", "number", title: "Set the alarm volume", description: "0-100%", required: false
            href "pageWeatherSettingsC", title: "Weather Reporting Settings",description: getWeatherDesc(C_weatherReport, C_includeSunrise, C_includeSunset, C_includeTemp, A_humidity, C_localTemp), state: greyOut1(C_weatherReport, C_includeSunrise, C_includeSunset, C_includeTemp, C_humidity, C_localTemp)
			href "pageDoorsWindowsC", title: "Doors/Windows Reporting Settings", description: getDoorsDesc(C_contactSensors, C_doorControls, C_locks),state: greyOut3(C_contactSensors, C_doorControls, C_locks)
            input "C_msg", "text", title: "Good night message", defaultValue: "Good Night!", required: false
            input "C_msgFirst", "bool", title: "Play good night message before other voice messages", defaultValue: "false"
        }
		section("Good Night Actions"){   
        	def phrases = location.helloHome?.getPhrases()*.label
            if (phrases) {
				phrases.sort()
				input "C_phrase", "enum", title: "Trigger the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (C_phrase){
                	input "C_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
                }
            }
		}
        section{
            input "C_triggerMode", "mode", title: "Trigger the following mode", required: false, submitOnChange:true
            if (C_triggerMode){
            	input "C_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
		}
    	section("Restrictions") {            
        	input "C_triggerOnce", "bool",title: "Trigger only once per day...", defaultValue: false
        	href "timeIntervalInputC", title: "Only during a certain time...", description: getTimeLabel(C_timeStart, C_timeEnd), state: greyOut2(C_timeStart, C_timeEnd)
        	input "C_day","enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        	input "C_mode", "mode", title: "Only during certain modes...", multiple: true, required: false
        }
    } 
}

page(name: "pageButtonControlC", title: "Button Controller Setup") {
	section {
    	input "C_buttonDevice", "capability.button", title: "Button Controller", multiple: false, required: false
        input "C_buttonPress", "enum", title: "Which button...", options: [1:"1", 2:"2", 3:"3", 4:"4"], multiple: false, required: false
	}
}

page(name: "timeIntervalInputC", title: "Only during a certain time") {
	section {
		input "C_timeStart", "time", title: "Starting", required: false
		input "C_timeEnd", "time", title: "Ending", required: false
	}
}

def pageWeatherSettingsC() {
    dynamicPage(name: "pageWeatherSettingsC", title: "Weather Reporting Settings") {
	section {
		input "C_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        input "C_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        input "C_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        input "C_weatherReport", "bool", title: "Speak tomorrow's weather forecast", defaultValue: "false"
        input "C_includeSunrise", "bool", title: "Speak tomorrow's sunrise", defaultValue: "false"
    	input "C_includeSunset", "bool", title: "Speak tomorrow's sunset", defaultValue: "false"
	}
}
}

def pageDoorsWindowsC() {
    dynamicPage(name: "pageDoorsWindowsC", title: "Doors/Windows Reporting Settings"){
		section {
    		input "C_reportAll", "bool", title: "Report status even when closed and locked", defaultValue: "false"
        	input "C_contactSensors", "capability.contactSensor", title: "Which Doors/Windows...", multiple: true, required: false
            input "C_doorControls", "capability.doorControl", title: "Which Door Controls...", multiple: true, required: false
       		input "C_locks", "capability.lock", title: "Which Locks...", multiple: true, required: false
   		}
	}
}

// Show "pageSetupScenarioD" page
def pageSetupScenarioD() {
    dynamicPage(name: "pageSetupScenarioD") {
		section("Scenario Settings") {
        	input "ScenarioNameD", "text", title: "Scenario Name", required: false
			input "D_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true
    	}
		section("Say 'Good Night' when...") {
			input "D_switches", "capability.switch",title: "Any of these switches are turned off...", multiple: true, required: false
            href "pageButtonControlD", title: "A button is pressed on a controller...", description: buttonDesc(D_buttonDevice, D_buttonPress), state: greyOut(D_buttonDevice, D_buttonPress)
        }
        section("Voice Reporting Options") {
        	input "D_volume", "number", title: "Set the alarm volume", description: "0-100%", required: false
        	href "pageWeatherSettingsD", title: "Weather Reporting Settings", description: getWeatherDesc(D_weatherReport, D_includeSunrise, D_includeSunset, D_includeTemp, D_humidity, D_localTemp), state: greyOut1(D_weatherReport, D_includeSunrise, D_includeSunset, D_includeTemp, D_humidity, D_localTemp)
        	href "pageDoorsWindowsD", title: "Doors/Windows Reporting Settings", description: getDoorsDesc(D_contactSensors, D_doorControls, D_locks),state: greyOut3(D_contactSensors,D_doorControls, D_locks)
            input "D_msg", "text", title: "Good night message", defaultValue: "Good Night!", required: false
            input "D_msgFirst", "bool", title: "Play good night message before other voice messages", defaultValue: "false"
        }
        section("Good Night Actions"){
            def phrases = location.helloHome?.getPhrases()*.label
            if (phrases) {
				phrases.sort()
				input "D_phrase", "enum", title: "Trigger the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (D_phrase){
                	input "D_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
                }
            }
		}
        section{
            input "D_triggerMode", "mode", title: "Trigger the following mode", required: false, submitOnChange:true
            if (D_triggerMode){
            	input "D_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
		}
    	section("Restrictions") {            
        	input "D_triggerOnce", "bool",title: "Trigger only once per day...", defaultValue: false
        	href "timeIntervalInputD", title: "Only during a certain time...", description: getTimeLabel(D_timeStart, D_timeEnd), state:greyOut2(D_timeStart, D_timeEnd)
        	input "D_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        	input "D_mode", "mode", title: "Only during certain modes...", multiple: true, required: false
        }
    } 
}

page(name: "pageButtonControlD", title: "Button Controller Setup") {
	section {
    	input "D_buttonDevice", "capability.button", title: "Button Controller", multiple: false, required: false
        input "D_buttonPress", "enum", title: "Which button...", options: [1:"1", 2:"2", 3:"3", 4:"4"], multiple: false, required: false
	}
}

page(name: "timeIntervalInputD", title: "Only during a certain time") {
	section {
		input "D_timeStart", "time", title: "Starting", required: false
		input "D_timeEnd", "time", title: "Ending", required: false
	}
}

def pageWeatherSettingsD() {
    dynamicPage(name: "pageWeatherSettingsD", title: "Weather Reporting Settings") {
	section {
		input "D_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        input "D_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        input "D_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        input "D_weatherReport", "bool", title: "Speak tomorrow's weather forecast", defaultValue: "false"
        input "D_includeSunrise", "bool", title: "Speak tomorrow's sunrise", defaultValue: "false"
    	input "D_includeSunset", "bool", title: "Speak tomorrow's sunset", defaultValue: "false"
	}
}
}

def pageDoorsWindowsD() {
    dynamicPage(name: "pageDoorsWindowsD", title: "Doors/Windows Reporting Settings"){
		section {
  			input "D_reportAll", "bool", title: "Report status even when closed and locked", defaultValue: "false"
        	input "D_contactSensors", "capability.contactSensor", title: "Which Doors/Windows...", multiple: true, required: false
            input "D_doorControls", "capability.doorControl", title: "Which Door Controls...", multiple: true, required: false
       		input "D_locks", "capability.lock", title: "Which Locks...", multiple: true, required: false
   		}
	}
}

// Show "pageSetupScenarioE" page
def pageSetupScenarioE() {
    dynamicPage(name: "pageSetupScenarioE") {
		section("Scenario Settings") {
        	input "ScenarioNameE", "text", title: "Scenario Name", required: false
			input "E_sonos", "capability.musicPlayer", title: "Choose a Sonos speaker", required: true
    	}
		section("Say 'Good Night' when...") {
			input "E_switches", "capability.switch",title: "Any of these switches are turned off...", multiple: true, required: false
            href "pageButtonControlE", title: "A button is pressed on a controller...", description: buttonDesc(E_buttonDevice, E_buttonPress), state: greyOut(E_buttonDevice, E_buttonPress)
        }
        section("Voice Reporting Options") {
        	input "E_volume", "number", title: "Set the alarm volume", description: "0-100%", required: false
        	href "pageWeatherSettingsE", title: "Weather Reporting Settings", description: getWeatherDesc(E_weatherReport, E_includeSunrise, E_includeSunset, E_includeTemp, E_humidity, E_localTemp), state: greyOut1(E_weatherReport, E_includeSunrise, E_includeSunset, E_includeTemp, E_humidity, E_localTemp)
        	href "pageDoorsWindowsE", title: "Doors/Windows Reporting Settings", description: getDoorsDesc(E_contactSensors, E_doorControls, E_locks),state: greyOut3(E_contactSensors,E_doorControls, E_locks)
            input "E_msg", "text", title: "Good night message", defaultValue: "Good Night!", required: false
            input "E_msgFirst", "bool", title: "Play good night message before other voice messages", defaultValue: "false"
        }
        section("Good Night Actions"){
            def phrases = location.helloHome?.getPhrases()*.label
            if (phrases) {
				phrases.sort()
				input "E_phrase", "enum", title: "Trigger the following phrase", required: false, options: phrases, multiple: false, submitOnChange:true
				if (E_phrase){
                	input "E_confirmPhrase", "bool", title: "Confirm Hello, Home phrase in voice message", defaultValue: "false"
                }
            }
		}
        section{
            input "E_triggerMode", "mode", title: "Trigger the following mode", required: false, submitOnChange:true
            if (E_triggerMode){
            	input "E_confirmMode", "bool", title: "Confirm mode in voice message", defaultValue: "false"
            }
		}
    	section("Restrictions") {            
        	input "E_triggerOnce", "bool",title: "Trigger only once per day...", defaultValue: false
        	href "timeIntervalInputD", title: "Only during a certain time...", description: getTimeLabel(E_timeStart, E_timeEnd), state:greyOut2(E_timeStart, E_timeEnd)
        	input "E_day", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        	input "E_mode", "mode", title: "Only during certain modes...", multiple: true, required: false
        }
    } 
}

page(name: "pageButtonControlE", title: "Button Controller Setup") {
	section {
    	input "E_buttonDevice", "capability.button", title: "Button Controller", multiple: false, required: false
        input "E_buttonPress", "enum", title: "Which button...", options: [1:"1", 2:"2", 3:"3", 4:"4"], multiple: false, required: false
	}
}

page(name: "timeIntervalInputE", title: "Only during a certain time") {
	section {
		input "E_timeStart", "time", title: "Starting", required: false
		input "E_timeEnd", "time", title: "Ending", required: false
	}
}

def pageWeatherSettingsE() {
    dynamicPage(name: "pageWeatherSettingsE", title: "Weather Reporting Settings") {
	section {
		input "E_includeTemp", "bool", title: "Speak current temperature (from local forecast)", defaultValue: "false"
        input "E_localTemp", "capability.temperatureMeasurement", title: "Speak local temperature (from device)", required: false, multiple: false
        input "E_humidity", "capability.relativeHumidityMeasurement", title: "Speak local humidity (from device)", required: false, multiple: false
        input "E_weatherReport", "bool", title: "Speak tomorrow's weather forecast", defaultValue: "false"
        input "E_includeSunrise", "bool", title: "Speak tomorrow's sunrise", defaultValue: "false"
    	input "E_includeSunset", "bool", title: "Speak tomorrow's sunset", defaultValue: "false"
	}
}
}

def pageDoorsWindowsE() {
    dynamicPage(name: "pageDoorsWindowsD", title: "Doors/Windows Reporting Settings"){
		section {
  			input "E_reportAll", "bool", title: "Report status even when closed and locked", defaultValue: "false"
        	input "E_contactSensors", "capability.contactSensor", title: "Which Doors/Windows...", multiple: true, required: false
            input "E_doorControls", "capability.doorControl", title: "Which Door Controls...", multiple: true, required: false
       		input "E_locks", "capability.lock", title: "Which Locks...", multiple: true, required: false
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
	midNightReset()
    if (A_switches) {
    	subscribe (A_switches, "switch.off", scenario_A)
    }
	if (B_switches) {
    	subscribe (B_switches, "switch.off", scenario_B)
    }
	if (C_switches) {
    	subscribe (C_switches, "switch.off", scenario_C)
    }
	if (D_switches) {
    	subscribe (D_switches, "switch.off", scenario_D)
    }
    if (E_switches) {
    	subscribe (E_switches, "switch.off", scenario_E)
    }
    if (A_buttonDevice && A_buttonPress){
    	subscribe(A_buttonDevice, "button.pushed", buttonHandler_A)
    }
    if (B_buttonDevice && B_buttonPress){
    	subscribe(B_buttonDevice, "button.pushed", buttonHandler_B)
    }
    if (C_buttonDevice && C_buttonPress){
    	subscribe(C_buttonDevice, "button.pushed", buttonHandler_C)
    }
	if (D_buttonDevice && D_buttonPress){
    	subscribe(D_buttonDevice, "button.pushed", buttonHandler_D)
    }
    if (E_buttonDevice && E_buttonPress){
    	subscribe(E_buttonDevice, "button.pushed", buttonHandler_E)
    }
}

//--------------------------------------

def scenario_A(evt) {
	if ((!A_triggerOnce || (A_triggerOnce && !A_triggered)) && getTimeOk(A_timeStart, A_timeEnd) && getDayOk(A_day) && (!A_mode || A_mode.contains(location.mode))) {
		state.fullMsgA = ""
		
        if (A_msg && A_msgFirst) {
			getGreeting(A_msg, 1)
		}
        
		if (A_weatherReport || A_humidity || A_includeTemp || A_localTemp) {
			getWeatherReport(1, A_weatherReport, A_humidity, A_includeTemp, A_localTemp)
		}
        
        if (A_includeSunrise || A_includeSunset) {
        	getSunriseSunset(1, A_includeSunrise, A_includeSunset)
        }
                   
		if (A_phrase) {
        	location.helloHome.execute(A_phrase)
        	if (A_confirmPhrase){
            	getPhraseConfirmation(A_phrase, 1)
            }
        }
        
        if (A_triggerMode && location.mode != A_triggerMode) {
			if (location.modes?.find{it.name == A_triggerMode}) {
				setLocationMode(A_triggerMode)
			} else {
				log.debug "Unable to change to undefined mode '${A_triggerMode}'"
			}
			if (A_confirmMode){
            	getModeConfirmation(A_triggerMode, 1)
        	}
        }
        
        if (A_contactSensors || A_locks || A_doorControls){
        	getDoorsConditions(A_reportAll, A_contactSensors, A_locks, A_doorControls, 1)
        }
        
        if (A_msg && !A_msgFirst) {
			getGreeting(A_msg, 1)
		} 
      
		state.soundA = textToSpeech(state.fullMsgA, true)
    	if (A_volume) {
			A_sonos.setLevel(A_volume)
		}
		A_sonos.playTrack(state.soundA.uri)
        if (A_triggerOnce) {
			state.A_triggered = true
			runOnce (getMidnight(), midNightReset)
		}
    }
}

def buttonHandler_A(evt){
    def data = new JsonSlurper().parseText(evt.data)
    def button = data.buttonNumber
	def remoteButton = A_buttonPress as Integer
   
	if (button == remoteButton) {
        scenario_A()
    }
}

def scenario_B(evt) {
	if ((!B_triggerOnce || (B_triggerOnce && !B_triggered)) && getTimeOk(B_timeStart, B_timeEnd) && getDayOk(B_day) && (!B_mode || B_mode.contains(location.mode))) {
		state.fullMsgB = ""
		
        if (B_msg && B_msgFirst) {
			getGreeting(B_msg, 2)
		}
        
		if (B_weatherReport || B_humidity || B_includeTemp || B_localTemp) {
			getWeatherReport(2, B_weatherReport, B_humidity, B_includeTemp, B_localTemp)
		}
         
        if (B_includeSunrise || B_includeSunset) {
        	getSunriseSunset(2, B_includeSunrise, B_includeSunset)
        }
         
		if (B_phrase) {
        	location.helloHome.execute(B_phrase)
        	if (B_confirmPhrase) {
            	getPhraseConfirmation(B_phrase, 2)
            }
        }
        
        if (B_triggerMode && location.mode != B_triggerMode) {
			if (location.modes?.find{it.name == B_triggerMode}) {
				setLocationMode(B_triggerMode)
			} else {
				log.debug "Unable to change to undefined mode '${B_triggerMode}'"
			}
			if (B_confirmMode){
            	getModeConfirmation(B_triggerMode, 2)
        	}
        }
        
        if (B_contactSensors || B_locks || B_doorControls){
        	getDoorsConditions(B_reportAll, B_contactSensors, B_locks, B_doorControls, 2)
        }
        
        if (B_msg && !B_msgFirst) {
			getGreeting(B_msg, 2)
		} 
      
		state.soundB = textToSpeech(state.fullMsgB, true)
    	if (B_volume) {
			B_sonos.setLevel(B_volume)
		}
		
        B_sonos.playTrack(state.soundB.uri)
		if (B_triggerOnce) {
			state.B_triggered = true
			runOnce (getMidnight(), midNightReset)
		}
    }
}

def buttonHandler_B(evt){
    def data = new JsonSlurper().parseText(evt.data)
    def button = data.buttonNumber
	def remoteButton = B_buttonPress as Integer
   
	if (button == remoteButton) {
        scenario_B()
    }
}

def scenario_C(evt) {
	if ((!C_triggerOnce || (C_triggerOnce && !C_triggered)) && getTimeOk(C_timeStart, C_timeEnd) && getDayOk(C_day) && (!C_mode || C_mode.contains(location.mode))) {
		state.fullMsgC = ""
		
        if (C_msg && C_msgFirst) {
			getGreeting(C_msg, 3)
		}
        
        if (C_weatherReport || C_humidity || C_includeTemp || C_localTemp) {
			getWeatherReport(3, C_weatherReport, C_humidity, C_includeTemp, C_localTemp)
		}
        
        if (C_includeSunrise || C_includeSunset) {
        	getSunriseSunset(3, C_includeSunrise, C_includeSunset)
        }
        
		if (C_phrase) {
        	location.helloHome.execute(C_phrase)
        	if (C_confirmPhrase){
            	getPhraseConfirmation(C_phrase, 3)
            }
        }
                
        if (C_triggerMode && location.mode != C_triggerMode) {
            if (location.modes?.find{it.name == C_triggerMode}) {
				setLocationMode(C_triggerMode)
			} else {
				log.debug "Unable to change to undefined mode '${C_triggerMode}'"
			}
			if (C_confirmMode){
            	getModeConfirmation(C_triggerMode, 3)
        	}
        }
        
        if (C_contactSensors || C_locks || C_doorControls){
        	getDoorsConditions(C_reportAll, C_contactSensors, C_locks, C_doorControls, 3)
        }
        
        if (C_msg && !C_msgFirst) {
			getGreeting(C_msg, 3)
		} 
      
		state.soundC = textToSpeech(state.fullMsgC, true)
    	if (C_volume) {
			C_sonos.setLevel(C_volume)
		}
		C_sonos.playTrack(state.soundC.uri)
		if (C_triggerOnce) {
			state.C_triggered = true
			runOnce (getMidnight(), midNightReset)
		}
    }
}

def buttonHandler_C(evt){
    def data = new JsonSlurper().parseText(evt.data)
    def button = data.buttonNumber
	def remoteButton = C_buttonPress as Integer
   
	if (button == remoteButton) {
        scenario_C()
    }
}

def scenario_D(evt) {
	if ((!D_triggerOnce || (D_triggerOnce && !D_triggered)) && getTimeOk(D_timeStart, D_timeEnd) && getDayOk(D_day) && (!D_mode || D_mode.contains(location.mode))) {
		state.fullMsgD = ""
		
        if (D_msg && D_msgFirst) {
			getGreeting(D_msg, 4)
		}
        
		if (D_weatherReport || D_humidity || D_includeTemp || D_localTemp) {
			getWeatherReport(4, D_weatherReport, D_humidity, D_includeTemp, D_localTemp)
		}
        
        if (D_includeSunrise || D_includeSunset) {
        	getSunriseSunset(4, D_includeSunrise, D_includeSunset)
        }
        
		if (D_phrase) {
        	location.helloHome.execute(D_phrase)
        	if (D_confirmPhrase){
            	getPhraseConfirmation(D_phrase, 4)
            }
        }

        if (D_triggerMode && location.mode != D_triggerMode) {
            if (location.modes?.find{it.name == D_triggerMode}) {
				setLocationMode(D_triggerMode)
			} else {
				log.debug "Unable to change to undefined mode '${D_triggerMode}'"
			}
			if (D_confirmMode){
            	getModeConfirmation(D_triggerMode, 4)
        	}
        }
        
        if (D_contactSensors || D_locks || D_doorControls){
        	getDoorsConditions(D_reportAll, D_contactSensors, D_locks, D_doorControls, 4)
        }

        if (D_msg && !D_msgFirst) {
			getGreeting(D_msg, 4)
		} 
      
		state.soundD = textToSpeech(state.fullMsgD, true)
    	if (D_volume) {
			D_sonos.setLevel(D_volume)
		}
		
        D_sonos.playTrack(state.soundD.uri)
		if (D_triggerOnce) {
			state.D_triggered = true
			runOnce (getMidnight(), midNightReset)
		}
    }
}

def buttonHandler_D(evt){
    def data = new JsonSlurper().parseText(evt.data)
    def button = data.buttonNumber
	def remoteButton = D_buttonPress as Integer
   
	if (button == remoteButton) {
        scenario_D()
    }
}

def scenario_E(evt) {
	if ((!E_triggerOnce || (E_triggerOnce && !E_triggered)) && getTimeOk(E_timeStart, E_timeEnd) && getDayOk(E_day) && (!E_mode || E_mode.contains(location.mode))) {
		state.fullMsgE = ""
		
        if (E_msg && E_msgFirst) {
			getGreeting(E_msg, 5)
		}
        
		if (E_weatherReport || E_humidity || E_includeTemp || E_localTemp) {
			getWeatherReport(5, E_weatherReport, E_humidity, E_includeTemp, E_localTemp)
		}
        
        if (E_includeSunrise || E_includeSunset) {
        	getSunriseSunset(5, E_includeSunrise, E_includeSunset)
        }
        
		if (E_phrase) {
        	location.helloHome.execute(E_phrase)
        	if (E_confirmPhrase){
            	getPhraseConfirmation(E_phrase, 5)
            }
        }

        if (E_triggerMode && location.mode != E_triggerMode) {
            if (location.modes?.find{it.name == E_triggerMode}) {
				setLocationMode(E_triggerMode)
			} else {
				log.debug "Unable to change to undefined mode '${E_triggerMode}'"
			}
			if (E_confirmMode){
            	getModeConfirmation(E_triggerMode, 5)
        	}
        }
        
        if (E_contactSensors || E_locks || E_doorControls){
        	getDoorsConditions(E_reportAll, E_contactSensors, E_locks, E_doorControls, 5)
        }

        if (E_msg && !E_msgFirst) {
			getGreeting(E_msg, 5)
		} 

		state.soundE = textToSpeech(state.fullMsgE, true)
    	if (E_volume) {
			E_sonos.setLevel(E_volume)
		}
		
        E_sonos.playTrack(state.soundE.uri)
		if (E_triggerOnce) {
			state.E_triggered = true
			runOnce (getMidnight(), midNightReset)
		}
    }
}

def buttonHandler_E(evt){
    def data = new JsonSlurper().parseText(evt.data)
    def button = data.buttonNumber
	def remoteButton = E_buttonPress as Integer
   
	if (button == remoteButton) {
        scenario_E()
    }
}

//--------------------------------------

def midNightReset() {
	state.A_triggered = false
    state.B_triggered = false
    state.C_triggered = false
    state.D_triggered = false
    state.E_triggered = false
}

//--------------------------------------

def getMidnight() {
	def midnightToday = timeToday("2000-01-01T23:59:59.999-0000", location.timeZone)
}

def greyOut(param1, param2){
	def result = param1 && param2 ? "complete" : ""
}

def greyOut1(param1, param2, param3, param4, param5, param6){
	def result = param1 || param2 || param3 || param4 || param5 || param6 ? "complete" : ""
}

def greyOut2(param1, param2){
	def result = param1 || param2 ? "complete" : ""
}

def greyOut3(param1, param2, param3){
	def result = param1 || param2 || param3 ? "complete" : ""
}

def getTitle(scenario, num) {
	def title = scenario ? scenario : "Scenario ${num} not configured"
}

def getWeatherDesc(param1, param2, param3, param4, param5, param6) {
	def title = param1 || param2 || param3 || param4 || param5 || param6 ? "Tap to edit weather reporting settings" : "Tap to setup weather reporting settings"
}

def getDoorsDesc(param1, param2, param3) {
	def title = param1 || param2  ? "Tap to edit door/window reporting settings" : "Tap to setup door/window reporting settings"
}

def buttonDesc(button, num){
	def desc = button && num ? "${button}, button: ${num}" : "Tap to set button controller settings"
}

private getDayOk(dayList) {
	def result = true
	if (dayList) {
		result = dayList.contains(getDay())
	}
	log.trace "DayOk = $result"
    result
}

def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
	
    if(start && end){
    	timeLabel = "Between" + " " + parseDate(start, "", "h:mm a") + " "  + "and" + " " +  parseDate(end, "", "h:mm a")
    }
    else if (start) {
		timeLabel = "Start at" + " " + parseDate(start, "",  "h:mm a")
    }
    else if(end){
    timeLabel = "End at" + parseDate(end, "", "h:mm a")
    }
	timeLabel	
}

private getTimeOk(starting, ending) {
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
            
            sb << "Tomorrow's forecast is "
			if (isMetric) {
        		sb << weather.forecast.txt_forecast.forecastday[2].fcttext_metric 
        	}
        	else {
          		sb << weather.forecast.txt_forecast.forecastday[2].fcttext
        	}
        }
        
		def msg = sb.toString()
        msg = msg.replaceAll(/([0-9]+)C/,'$1 degrees')
        msg = msg.replaceAll(/([0-9]+)F/,'$1 degrees')
        compileMsg(msg, scenario)
	}
	else {
		msg = "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather forecasts. "
		compileMsg(msg, scenario)
    }
}

private getSunriseSunset(scenario, includeSunrise, includeSunset){
	if (location.timeZone || zipCode) {
    	def tomorrowDate = new Date() + 1
    	def s = getSunriseAndSunset(zipcode: zipCode, date: tomorrowDate)	
             
        def riseTime = parseDate("",s.sunrise.time, "h:mm a")
		def setTime = parseDate("",s.sunset.time, "h:mm a")
        
   		def msg = ""
    	if (includeSunrise && includeSunset) {
			msg = "The sun will rise tomorrow at ${riseTime} and set at ${setTime}. "
    	}
    	else if (includeSunrise && !includeSunset) {
    		msg = "The sun will rise tomorrow at ${riseTime}. "
    	}
    	else if (!includeSunrise && includeSunset) {
    		msg = "The sun will set tomorrow at ${setTime}. "
    	}
    	compileMsg(msg, scenario)
	}
	else {
		msg = "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive sunset and sunrise information. "
		compileMsg(msg, scenario)
	}
}

private getDoorsConditions(reportAll, contactSensors, locks, doorControls, scenario){
    def msg=""
    def unlocked = locks && locks.latestValue("lock").contains("unlocked")
    def opened = contactSensors && contactSensors.latestValue("contact").contains("open")
    def openedDoor = doorControls && doorControls.latestValue("door").contains("open")
    def countUnlocked = 0
    def countOpened = 0
    def countOpenedDoor = 0
    def newCount =0
    def listOpened = ""
    def listUnlocked = ""
    
    if (opened){
        for (sensor in contactSensors){
        	if (sensor.latestValue("contact")=="open"){
                countOpened += 1
        	}
		}
    	newCount = countOpened
        for (sensor in contactSensors){	
            if (sensor.latestValue("contact")=="open"){
				listOpened = "${listOpened} ${sensor}"
                newCount = newCount - 1
                if (newCount == 1 && !openedDoor){
					listOpened = "${listOpened} and the "
                }
                else if (newCount > 1 || openedDoor) {
                	listOpened = "${listOpened},"
                }
        	}
    	}
    }
    if (openedDoor){
        for (door in doorControls){
        	if (door.latestValue("door") == "open"){
                countOpenedDoor += 1
        	}
		}
    	newCount = countOpenedDoor
        for (door in doorControls){	
            if (door.latestValue("door") == "open"){
				listOpened = "${listOpened} ${door}"
                newCount = newCount - 1
                if (newCount == 1){
					listOpened = "${listOpened} and the "
                }
                else if (newCount > 1) {
                	listOpened = "${listOpened},"
                }
        	}
    	}
    }
    if (unlocked){
        for (doorLock in locks){
        	if (doorLock.latestValue("lock")=="unlocked"){
                countUnlocked += 1
			}		
    	}
		newCount = countUnlocked
        for (doorLock in locks){	
            if (doorLock.latestValue("lock")=="unlocked"){
				listUnlocked = "${listUnlocked} ${doorLock}"
                newCount = newCount - 1
                if (newCount == 1){
					listUnlocked = "${listUnlocked} and the "
                }
        		else if (newCount > 1) {
            		listUnlocked = "${listUnlocked},"
                }
            }
    	}    
    }
    def totalCount = countOpenedDoor + countOpened
    if (reportAll){
    	if (!opened && !unlocked && !openedDoor){
   			msg = "All of the doors and windows are closed and locked. "
    	}
        if (!opened && !openedDoor && unlocked){
   			msg = "All of the doors and windows are closed, but the"
            msg = countUnlocked > 1 ? "${msg} following are unlocked: ${listUnlocked}. " :"${msg} ${listUnlocked} is unlocked. "
    	}
        if ((opened || openedDoor) && !unlocked){
   			msg = "All of the doors are locked, but the"
            msg = totalCount > 1 ? "${msg} following doors or windows are open: ${listOpened}. " : "${msg} ${listOpened} is open. "
    	}
    }   
	else {
		if ((opened || openedDoor) && !unlocked){
    		msg = totalCount > 1 ? "The following doors or windows are currently open: ${listOpened}. " : "${listOpened} is open. "
    	}
        if (!opened && !openedDoor && unlocked){
    		msg = countUnlocked > 1 ? "The following doors are unlocked: ${listUnlocked}. " : "The ${listUnlocked} is unlocked. "
    	}
	}
    if ((opened || openedDoor) && unlocked){
		def verb = totalCount > 1 ? "following doors or windows are currently open: ${listOpened}" : "${listOpened} is open"
		def verb1 = countUnlocked > 1 ? "following are unlocked: ${listUnlocked}" : "${listUnlocked} is unlocked"
		msg = "The ${verb}. Also, the ${verb1}. "
    }
 	compileMsg(msg, scenario) 
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
	compileMsg(msg, scenario)
}

private getPhraseConfirmation(phrase, scenario) {
	def msg="The Smart Things Hello Home phrase, ${phrase}, has been activated. "
	compileMsg(msg, scenario)
}

private getModeConfirmation(mode, scenario) {
	def msg="The Smart Things mode is now being set to, ${mode}. "
	compileMsg(msg, scenario)
}

private compileMsg(msg, scenario) {
	log.debug "msg = ${msg}"
    if (scenario == 1) {state.fullMsgA = state.fullMsgA + "${msg} "}
	if (scenario == 2) {state.fullMsgB = state.fullMsgB + "${msg} "}
	if (scenario == 3) {state.fullMsgC = state.fullMsgC + "${msg} "}
	if (scenario == 4) {state.fullMsgD = state.fullMsgD + "${msg} "}
    if (scenario == 5) {state.fullMsgE = state.fullMsgE + "${msg} "}
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Say Good Night!"
}	

private def textVersion() {
    def text = "Version 1.0.2 (06/26/2015)"
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
    	"Within each scenario, choose a Sonos speaker along with a trigger to have the system wish you a good night. " +
        "Triggers can be certain switches being turning off or the press of a button on a controller. You can also run a Hello, Home phrase or change mode " +
        "when the scenario is triggered. Triggers can be restricted to certain times and days of the week or modes. The voice message can be a simple text phrase, "+
        "or can include the status of doors and locks, the current temperature and tomorrow's weather forecast. Variables to use in the voice greeting include %day%, %time% and %date%."
}