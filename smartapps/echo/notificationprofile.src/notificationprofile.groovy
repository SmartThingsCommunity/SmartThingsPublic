/* 
 * Notification - EchoSistant Add-on 
 *
 *		8/04/2017		Version:4.0 R.0.3.8c		Changed switch restrictions to ANY switch instead of ALL switches
 *		5/13/2017		Version:4.0 R.0.3.8b			Added more Ad-Hoc Report Variables
 *		5/11/2017		Version:4.0 R.0.3.8a			Added acceleration triggers
 *		5/04/2017		Version:4.0 R.0.3.7			Added temperature, CO, CO2 and humidity triggers
 *		5/02/2017		Version:4.0 R.0.3.5			Added Pet Note variables to Ad-hoc reports
 *		5/01/2017		Version:4.0 R.0.3.4			Added WebCoRE integration
 *		4/27/2017		Version:4.0 R.0.3.3			Retrigger bug fixe
 *		4/25/2017		Version:4.0 R.0.3.2			Minor bug fixes
 *		3/16/2017		Version:4.0 R.0.3.0	    	Cron Scheduling and Reporting
 *
 *  Copyright 2016 Jason Headley & Bobby Dobrescu
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
/**********************************************************************************************************************************************/
definition(
	name			: "NotificationProfile",
    namespace		: "Echo",
    author			: "JH/BD",
	description		: "EchoSistant Add-on",
	category		: "My Apps",
    parent			: "Echo:EchoSistant", 
	iconUrl			: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant.png",
	iconX2Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant@2x.png",
	iconX3Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant@2x.png")
/**********************************************************************************************************************************************/
private release() {
	def text = "R.0.3.8c"
}

preferences {

    page name: "mainProfilePage"
    		page name: "pNotifyScene"          
        	page name: "pNotifications"
        	page name: "pRestrict"
            page name: "pNotifyConfig"
            page name: "SMS"
            page name: "customSounds"
            page name: "certainTimeX"
            page( name: "timeIntervalInput", title: "Only during a certain time")

}
//dynamic page methods
page name: "mainProfilePage"
    def mainProfilePage() {
 	dynamicPage (name: "mainProfilePage", install: true, uninstall: true) {
		section ("Name (rename) this Profile") {
 		   	label title:"Profile Name ", required:false, defaultValue: "Notification Profile"  
		}
		section ("Create a Notification") {  
                input "actionType", "enum", title: "Choose a Notification Type", required: false, defaultValue: "Default", submitOnChange: true, 
                options: [
                "Ad-Hoc Report",
                "Triggered Report",
                "Custom",
                "Custom with Weather",
                "Default",
				"Bell 1",
				"Bell 2",
				"Dogs Barking",
				"Fire Alarm",
				"The mail has arrived",
				"A door opened",
				"There is motion",
				"Smartthings detected a flood",
				"Smartthings detected smoke",
				"Someone is arriving",
				"Piano",
				"Lightsaber"]
		}
        if (actionType == "Custom" || actionType == "Custom with Weather" || actionType == "Ad-Hoc Report") {
            section ("Send this message text...") {
                input "message", "text", title: "Play this message (optional - leave blank for a default message)", required:false, multiple: false, defaultValue: "", submitOnChange: true
            }
            if(message) {
				def report
               	section ("Preview Report with Current Data", hideable: true, hidden: true) {
                	paragraph report = runProfile("test") 
                     href "variables", title: "View other variables", description: ">> 		Click Here <<", state: "complete" 
				}
            }
			else {
                section ("Tap here to see available variables", hideable: true, hidden: true) {    
                    if (actionType != "Ad-Hoc Report") {
                        paragraph 	"CUSTOM MESSAGES: \n"+
                                    "&device, &action, &event, &time &date and &profile \n"
                        if (actionType != "Custom with Weather") href "variables", title: "View sample data for your Location", description: ">> 		Click Here <<", state: "complete"                                             
                    }
                    if(actionType == "Custom with Weather" || actionType == "Ad-Hoc Report" ){
                        paragraph 	"WEATHER: \n"+
                                    "Forecast Variables: &today, &tonight, &tomorrow,\n"+
                                    "Temperature Variables: &current, &high, &low\n"+ 
                                    "Sun State Variables: &set, &rise\n"+ 
                                    "Other Variables: &wind, &uv, &precipitation, &humidity"
                        if (actionType != "Ad-Hoc Report")  href "variables", title: "View sample data for your Location", description: ">> 		Click Here <<", state: "complete"                                             
                    }
                    if(actionType == "Ad-Hoc Report"){
                        paragraph 	"REPORTING VARIABLES: \n"+
                                    "Location: &time, &date, &profile, &mode, &shm \n"+
                                    "Device Status: &power, &lights, &unlocked \n"+
                                    "Sensors: &doors, &windows, &open, &garage, &present \n"+
                                    "Thermostats: &temperature, &running, &thermostat, &cooling, &heating"
                        href "variables", title: "View sample data for your Location", description: ">> 		Click Here <<", state: "complete"                                             
                    }
                }
        	}
        } 
		def sTitle = actionType != "Ad-Hoc Report" ? "Trigger(s)" : "Device(s)"
        section ("Using These ${sTitle}") {
			href "triggers", title: "Select ${sTitle}", description: triggersComplete(), state: triggersSettings()
        }    
        if (actionType != "Ad-Hoc Report"){
			section ("Retrigger " ) {    
                    input "retrigger", "enum", title: "Retrigger event", multiple: false, required: false, submitOnChange: true,
						options: [
							"runEvery1Minute": "Every Minute",
                            "runEvery5Minutes": "Every 5 Minutes",
                            "runEvery10Minutes": "Every 10 Minutes",
                            "runEvery15Minutes": "Every 15 Minutes",
                            "runEvery30Minutes": "Every 30 Minutes",
                            "runEvery1Hour": "Every Hour",
                            "runEvery3Hours": "Every 3 Hours",
                			//"runEvery1Day": "Every Day"
                            ]
            	if (retrigger) {
                	input "howManyTimes", "number", title: "...how many times to retrigger", required: true, description: "number of reminders"
                    input "continueOnChange", "bool", title: "Continue to deliver reminders after condition changes", required: false, defaultValue: false
            	}
            }         
            section ("With these output methods" , hideWhenEmpty: true) {    
                input "sonos", "capability.musicPlayer", title: "On this Music Player", required: false, multiple: true, submitOnChange: true
                    if (sonos) {
                        input "sonosVolume", "number", title: "Temporarily change volume", description: "0-100%", required: false
                    	input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: false
                        input "sonosDelay", "decimal", title: "(Optional) Delay delivery of second message by...", description: "seconds", required: false
                    }
                input "speechSynth", "capability.speechSynthesis", title: "On this Speech Synthesis Device", required: false, multiple: true, submitOnChange: true
                        if (speechSynth) {
                            input "speechVolume", "number", title: "Temporarily change volume", description: "0-100%", required: false
                    }
                href "SMS", title: "Send SMS & Push Messages...", description: pSendComplete(), state: pSendSettings()
            }
            if(actionType != "Default" && actionType != null){
                section ("Run actions for this Profile" ) {    
                    input "myProfile", "enum", title: "Choose Profile...", options: getProfileList(), multiple: false, required: false 
                }  
                section ("Using these Restrictions") {
                    href "pRestrict", title: "Use these restrictions...", description: pRestComplete(), state: pRestSettings()
                }
			}
        }
 	}
}
page name: "variables"
    def variables() {
        dynamicPage(name:"variables",title: "", uninstall: false) {
			def stamp = state.lastTime = new Date(now()).format("h:mm aa", location.timeZone)     
        	def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
        	def profile = app.label
            if (actionType != "Ad-Hoc Report"){
                section("Custom Message Variables") {
                    paragraph 	"&device = 	<< Label of Device >>,\n"+
                                "&action = 	<< Actual event: on/off, open/closed, locked/unlocked >>\n"+
                                "&event = 	<< Capability Type: switch, contact, motion, lock, etc >>,\n"+
                                "&time = $stamp,\n"+
                                "&date = $today,\n"+
                                "&profile = $profile"
                }  
             }
              if(actionType == "Custom with Weather" || actionType == "Ad-Hoc Report" ){
               	section("Weather Variables") {
                    paragraph 	"Forecast \n"+
                    			"&today = 			${mGetWeatherVar("today")},\n"+
                                "\n&tonight = 		${mGetWeatherVar("tonight")},\n"+
                                "\n&tomorrow = 		${mGetWeatherVar("tomorrow")},\n"+
                                "\nTemperatures \n"+
                                "&current = 		${mGetWeatherElements("current")},\n"+
                                "&high = 			${mGetWeatherVar("high")},\n"+
                                "&low = 			${mGetWeatherVar("low")},\n"+
                                "\nSun state \n"+
                                "&set = 			${mGetWeatherElements("set")},\n"+
                                "&rise = 			${mGetWeatherElements("rise")},\n"+
                                "\nOther \n"+
                                "&wind = 			${mGetWeatherElements("wind")},\n"+
                                "&precipitation = 	${mGetWeatherElements("precip")},\n"+
                                "&humidity = 		${mGetWeatherElements("hum")},\n"+
                                "&uv = 				${mGetWeatherElements("uv")}"
                }
			}
            //getVar(var)
            if(actionType == "Ad-Hoc Report"){
                section("Ad-Hoc Reporting Variables") {
                    paragraph 	"&time = 		${getVar("time")},\n"+
                                "&date = 		${getVar("date")},\n"+
                                "&mode = 		${getVar("mode")},\n"+
                                "&shm = 		${getVar("shm")},\n"+
                                "&power = 		${getVar("power")} 			(**),\n"+
                                "&lights = 		${getVar("lights")}, 		(**),\n"+
                                "&unlocked = 	${getVar("unlocked")} 		(**),\n"+
                                "&doors = 		${getVar("doors")} 			(++),\n"+
                                "&windows = 	${getVar("windows")} 		(++),\n"+
                                "&open = 		${getVar("open")} 			(**),\n"+
                                "&garage = 		${getVar("garage")} 		(++),\n"+
                                "&present = 	${getVar("present")} 		(**),\n"+
                                "&temperature = ${getVar("temperature")} 	(**),\n"+
                                "&running = 	${getVar("running")} 		(**),\n"+                            
                                "&thermostat = 	${getVar("thermostat")} 	(**),\n"+
                                "&cooling = 	${getVar("cooling")} 		(**),\n"+
                                "&heating = 	${getVar("heating")} 		(**)\n"+
                                "\nPet Notes - Cats \n"+
                                "&catshot = 	${getVar("catshot")}\n"+
                                "&catfed = 		${getVar("catfed")}\n"+
                                "&catmed = 		${getVar("catmed")}\n"+
                                "&catwalk = 	${getVar("catwalk")}\n"+
                                "&catbath = 	${getVar("catbath")}\n"+
                                "&catshot1 = 	${getVar("catshot1")}\n"+
                                "&catfed1 = 	${getVar("catfed1")}\n"+
                                "&catmed1 = 	${getVar("catmed1")}\n"+
                                "&catwalk1 = 	${getVar("catwalk1")}\n"+
                                "&catbath1 = 	${getVar("catbath1")}\n"+
                                "\nPet Notes - Dogs \n"+
                                "&dogshot = 	${getVar("dogshot")}\n"+
                                "&dogfed = 		${getVar("dogfed")}\n"+
                                "&dogmed = 		${getVar("dogmed")}\n"+
                                "&dogwalk = 	${getVar("dogwalk")}\n"+
                                "&dogbath = 	${getVar("dogbath")}\n"+
                                "&dogshot1 = 	${getVar("dogshot1")}\n"+
                                "&dogfed1 = 	${getVar("dogfed1")}\n"+
                                "&dogmed1 = 	${getVar("dogmed1")}\n"+
                                "&dogwalk1 = 	${getVar("dogwalk1")}\n"+
                                "&dogbath1 = 	${getVar("dogbath1")}\n"+
                                "\n"+
                                "(**) MUST select device(s) in the PROFILE \n"+
                                "(**) MUST select device(s) in the MAIN App"
                }
        	}
        }
    }
page name: "triggers"
	def triggers(){
		dynamicPage(name: "triggers", title: "", uninstall: false) {
            def actions = location.helloHome?.getPhrases()*.label.sort()
            if(actionType != "Default" && actionType != "Ad-Hoc Report" ){
                section("Time") {
                    input "frequency", "enum", title: "Choose a Frequency", submitOnChange: true, required: fale, 
            			options: ["Minutes", "Hourly", "Daily", "Weekly", "Monthly", "Yearly"]
                	if(frequency == "Minutes"){
                        input "xMinutes", "number", title: "Every X minute(s) - maximum 60", range: "1..59", submitOnChange: true, required: false
                    }
                    if(frequency == "Hourly"){
                        input "xHours", "number", title: "Every X hour(s) - maximum 24", range: "1..23", submitOnChange: true, required: false
                    }	
                    if(frequency == "Daily"){
                        input "xDays", "number", title: "Every X day(s) - maximum 31", range: "1..31", submitOnChange: true, required: false
						input "xDaysWeekDay", "bool", title: "OR Every Week Day (MON-FRI)", required: false, defaultValue: false
                        if(xDays || xDaysWeekDay){input "xDaysStarting", "time", title: "starting at time...", submitOnChange: true, required: true}
                    }   
                    if(frequency == "Weekly"){
						input "xWeeks", "enum", title: "Every selected day(s) of the week", submitOnChange: true, required: false, multiple: true,
							options: ["SUN": "Sunday", "MON": "Monday", "TUE": "Tuesday", "WED": "Wednesday", "THU": "Thursday", "FRI": "Friday", "SAT": "Saturday"]                        
                        if(xWeeks){input "xWeeksStarting", "time", title: "starting at time...", submitOnChange: true, required: true}
                    }
                    if(frequency == "Monthly"){
                    	//TO DO add every (First-Fourth), (Mon-Fri) of every (X) month
                        input "xMonths", "number", title: "Every X month(s) - maximum 12", range: "1..12", submitOnChange: true, required: false
                        if(xMonths){
                            input "xMonthsDay", "number", title: "...on this day of the month", range: "1..31", submitOnChange: true, required: true
                            input "xMonthsStarting", "time", title: "starting at time...", submitOnChange: true, required: true
                        }
                    }
                    if(frequency == "Yearly"){
                    	//TO DO add the (First-Fourth), (Mon-Fri) of (Jan-Dec)
                        input "xYears", "enum", title: "Every selected month of the year", submitOnChange: true, required: false, multiple: false,
                        	options: ["1": "January", "2":"February", "3":"March", "4":"April", "5":"May", "6":"June", "7":"July", "8":"August", "9":"September", "10":"October", "11":"November", "12":"December"]
                        if(xYears){
                            input "xYearsDay", "number", title: "...on this day of the month", range: "1..31", submitOnChange: true, required: true
                            input "xYearsStarting", "time", title: "starting at time...", submitOnChange: true, required: true                     
						}
                	}
                }
            }   
            if(actionType != "Default"){
                section ("Location Event", hideWhenEmpty: true) {
                    input "myMode", "enum", title: "Choose Modes...", options: location.modes.name.sort(), multiple: true, required: false 
                    if (actionType != "Ad-Hoc Report") {
                    	input "myRoutine", "enum", title: "Choose Routines...", options: actions, multiple: true, required: false            
                    	input "mySunState", "enum", title: "Choose Sunrise or Sunset...", options: ["Sunrise", "Sunset"], multiple: false, required: false, submitOnChange: true
                        	if(mySunState) input "offset", "number", range: "*..*", title: "Offset trigger this number of minutes (+/-)", required: false
                	}
                }
			}                       
            section ("Device State", , hideWhenEmpty: true) {
                input "mySwitch", "capability.switch", title: "Choose Switch(es)...", required: false, multiple: true, submitOnChange: true
                    if (mySwitch && actionType != "Ad-Hoc Report") input "mySwitchS", "enum", title: "Notify when state changes to...", options: ["on", "off", "both"], required: false
                if(actionType != "Default") {
                input "myPower", "capability.powerMeter", title: "Choose Power Meters...", required: false, multiple: false, submitOnChange: true
                    if (myPower && actionType != "Ad-Hoc Report") input "myPowerS", "enum", title: "Notify when power is...", options: ["above threshold", "below threshold"], required: false, submitOnChange: true
                        if (myPowerS) input "threshold", "number", title: "Wattage Threshold...", required: false, description: "in watts", submitOnChange: true
                        if (threshold) input "minutes", "number", title: "Threshold Delay", required: false, description: "in minutes (optional)"
                        if (threshold) input "thresholdStop", "number", title: "...but not above/below this value", required: false, description: "in watts"
                }
                input "myLocks", "capability.lock", title: "Choose Locks..", required: false, multiple: true, submitOnChange: true
                    if (myLocks && actionType != "Ad-Hoc Report") input "myLocksS", "enum", title: "Notify when state changes to...", options: ["locked", "unlocked", "both"], required: false, submitOnChange: true
                	if(myLocksS == "unlocked") input "myLocksSCode", "number", title: "With this user code...", required: false, description: "user code number (optional)"
                if(actionType != "Default"){
                input "myTstat", "capability.thermostat", title: "Choose Thermostats...", required: false, multiple: true, submitOnChange: true
                    if (myTstat && actionType != "Ad-Hoc Report") input "myTstatS", "enum", title: "Notify when set point changes for...", options: ["cooling", "heating", "both"], required: false
                    if (myTstat && actionType != "Ad-Hoc Report") input "myTstatM", "enum", title: "Notify when mode changes to...", options: ["auto", "cool", " heat", "emergency heat", "off", "every mode"], required: false
                    if (myTstat && actionType != "Ad-Hoc Report") input "myTstatOS", "enum", title: "Notify when Operating State changes to...", options: ["cooling", "heating", " idle", "every state"], required: false
            	}
            }
            section ("Sensor Status", hideWhenEmpty: true) {
                input "myContact", "capability.contactSensor", title: "Choose Doors and Windows..", required: false, multiple: true, submitOnChange: true
                    if (myContact && actionType != "Ad-Hoc Report") input "myContactS", "enum", title: "Notify when state changes to...", options: ["open", "closed", "both"], required: false
                input "myAcceleration", "capability.accelerationSensor", title: "Choose Acceleration Sensors..", required: false, multiple: true, submitOnChange: true
                    if (myAcceleration && actionType != "Ad-Hoc Report") input "myAccelerationS", "enum", title: "Notify when state changes to...", options: ["active", "inactive", "both"], required: false                   
                input "myMotion", "capability.motionSensor", title: "Choose Motion Sensors..", required: false, multiple: true, submitOnChange: true
                    if (myMotion && actionType != "Ad-Hoc Report") input "myMotionS", "enum", title: "Notify when state changes to...", options: ["active", "inactive", "both"], required: false
                input "myPresence", "capability.presenceSensor", title: "Choose Presence Sensors...", required: false, multiple: true, submitOnChange: true
                    if (myPresence && actionType != "Ad-Hoc Report") input "myPresenceS", "enum", title: "Notify when state changes to...", options: ["present", "not present", "both"], required: false
                input "mySmoke", "capability.smokeDetector", title: "Choose Smoke Detectors...", required: false, multiple: true, submitOnChange: true
                    if (mySmoke && actionType != "Ad-Hoc Report") input "mySmokeS", "enum", title: "Notify when state changes to...", options: ["detected", "clear", "both"], required: false
                input "myWater", "capability.waterSensor", title: "Choose Water Sensors...", required: false, multiple: true, submitOnChange: true
                    if (myWater && actionType != "Ad-Hoc Report") input "myWaterS", "enum", title: "Notify when state changes to...", options: ["wet", "dry", "both"], required: false		
                input "myTemperature", "capability.temperatureMeasurement", title: "Choose Temperature Sensors...", required: false, multiple: true, submitOnChange: true
					if (myTemperature && actionType != "Ad-Hoc Report") input "myTemperatureS", "enum", title: "Notify when temperature is...", options: ["above", "below"], required: false, submitOnChange: true
                        if (myTemperatureS) input "temperature", "number", title: "Temperature...", required: true, description: "degrees", submitOnChange: true
                        if (temperature) input "temperatureStop", "number", title: "...but not ${myTemperatureS} this temperature", required: false, description: "degrees"
                input "myCO2", "capability.carbonDioxideMeasurement", title: "Choose CO2 (Carbon Dioxide) Sensor...", required: false, submitOnChange: true
                    if (myCO2 && actionType != "Ad-Hoc Report") input "myCO2S", "enum", title: "Notify when CO2 is...", options: ["above", "below"], required: false, submitOnChange: true            
                    if (myCO2S) input "CO2", "number", title: "Carbon Dioxide Level...", required: true, description: "number", submitOnChange: true              
                input "myCO", "capability.carbonMonoxideDetector", title: "Choose CO (Carbon Monoxide) Sensor...", required: false, submitOnChange: true
                    if (myCO && actionType != "Ad-Hoc Report") input "myCOS", "enum", title: "Notify when ...", options: ["detected", "tested", "both"], required: false	            
                input "myHumidity", "capability.relativeHumidityMeasurement", title: "Choose Relative Humidity Sensor...", required: false, submitOnChange: true
                    if (myHumidity && actionType != "Ad-Hoc Report") input "myHumidityS", "enum", title: "Notify when Relative Humidity is...", options: ["above", "below"], required: false, submitOnChange: true            
                    if (myHumidityS) input "humidity", "number", title: "Relative Humidity...", required: true, description: "percent", submitOnChange: true            
                input "mySound", "capability.soundPressureLevel", title: "Choose Noise Sensor...", required: false, submitOnChange: true
                    if (mySound && actionType != "Ad-Hoc Report") input "mySoundS", "enum", title: "Notify when Noise is...", options: ["above", "below"], required: false, submitOnChange: true            
                    if (mySoundS) input "noise", "number", title: "Noise Level...", required: true, description: "number", submitOnChange: true              
            }
            if(actionType != "Default" && actionType != "Ad-Hoc Report"){
                section ("Weather Events") {
                    input "myWeatherAlert", "enum", title: "Choose Weather Alerts...", required: false, multiple: true, submitOnChange: true,
                            options: [
                            "TOR":	"Tornado Warning",
                            "TOW":	"Tornado Watch",
                            "WRN":	"Severe Thunderstorm Warning",
                            "SEW":	"Severe Thunderstorm Watch",
                            "WIN":	"Winter Weather Advisory",
                            "FLO":	"Flood Warning",
                            "WND":	"High Wind Advisoryt",
                            "HEA":	"Heat Advisory",
                            "FOG":	"Dense Fog Advisory",
                            "FIR":	"Fire Weather Advisory",
                            "VOL":	"Volcanic Activity Statement",
                            "HWW":	"Hurricane Wind Warning"
                            ]          
                    input "myWeather", "enum", title: "Choose Hourly Weather Forecast Updates...", required: false, multiple: false, submitOnChange: true,
                            options: ["Weather Condition Changes", "Chance of Precipitation Changes", "Wind Speed Changes", "Humidity Changes", "Any Weather Updates"]   
					input "myWeatherTriggers", "enum", title: "Choose Weather Element as Trigger...", required: false, multiple: false, submitOnChange: true,
						options: ["Chance of Precipitation (in/mm)", "Wind Gust (MPH/kPH)", "Humidity (%)", "Temperature (F/C)"]   
                        if (myWeatherTriggers) input "myWeatherTriggersS", "enum", title: "Notify when Weather Element changes...", 
                        	options: ["above", "below"], required: false, submitOnChange: true
						if (myWeatherTriggersS) input "myWeatherThreshold", "decimal", title: "Weather Variable Threshold...", required: false, submitOnChange: true
						if (myWeatherThreshold) input "myWeatherCheck", "enum", title: "How Often to Check for Weather Changes...", required: true, multiple: false, submitOnChange: true,
                				options: [
                                    "runEvery1Minute": "Every Minute",
                                    "runEvery5Minutes": "Every 5 Minutes",
                                    "runEvery10Minutes": "Every 10 Minutes",
                                    "runEvery15Minutes": "Every 15 Minutes",
                                    "runEvery30Minutes": "Every 30 Minutes",
                                    "runEvery1Hour": "Every Hour",
                                    "runEvery3Hours": "Every 3 Hours"
                					]
                } 
            }        
		}
	}
page name: "SMS"
    def SMS(){
        dynamicPage(name: "SMS", title: "Send SMS and/or Push Messages...", uninstall: false) {
        section ("Push Messages") {
            input "push", "bool", title: "Send Push Notification...", required: false, defaultValue: false
            input "timeStamp", "bool", title: "Add time stamp to Push Messages...", required: false, defaultValue: false  
            }
        section ("Text Messages" , hideWhenEmpty: true) {
            input "sendContactText", "bool", title: "Enable Text Notifications to Contact Book (if available)", required: false, submitOnChange: true
                if (sendContactText){
                    input "recipients", "contact", title: "Send text notifications to...", multiple: true, required: false
                }
            input "sendText", "bool", title: "Enable Text Notifications to non-contact book phone(s)", required: false, submitOnChange: true      
                if (sendText){      
                    paragraph "You may enter multiple phone numbers separated by comma to deliver the Alexa message as a text and a push notification. E.g. 8045551122,8046663344"
                    input name: "sms", title: "Send text notification to...", type: "phone", required: false
                }
            }    
        }        
    }
page name: "pRestrict"
    def pRestrict(){
        dynamicPage(name: "pRestrict", title: "", uninstall: false) {
			section ("Location Mode") {
                input "modes", "mode", title: "Only when mode is", multiple: true, required: false, submitOnChange: true
            }        
            section ("Certain Days"){	
                input "days", title: "Only on certain days of the week", multiple: true, required: false, submitOnChange: true,
                    "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            }
            section ("Certain Time"){
                href "certainTime", title: "Only during a certain time", description: pTimeComplete(), state: pTimeSettings()
            }
            section ("Device Status", hideable: true, hidden: false){
                input "rSwitch", "capability.switch", title: "Only when these Switch(es)", required: false, multiple: true, submitOnChange: true
                    if (rSwitch) input "rSwitchS", "enum", title: "state is...", options: ["on", "off"], required: false				
                input "rContact", "capability.contactSensor", title: "Only when these Contact Sensor(s)", required: false, multiple: true, submitOnChange: true
                    if (rContact) input "rContactS", "enum", title: "state is...", options: ["open", "closed"], required: false
                input "rMotion", "capability.motionSensor", title: "Only when these Motion Sensor(s)..", required: false, multiple: true, submitOnChange: true
                    if (rMotion) input "rMotionS", "enum", title: "state is...", options: ["active", "inactive"], required: false
                input "rPresence", "capability.presenceSensor", title: "Only when these Presence Sensor(s)...", required: false, multiple: true, submitOnChange: true
                    if (rPresence) input "rPresenceS", "enum", title: "state is...", options: ["present", "not present"], required: false
            }              
            section ("Frequency (audio only)", hideable: true, hidden: false){
                input "onceDaily", "bool", title: "Play notification only once daily", required: false, defaultValue: false
				input "everyXmin", "number", title: "Minimum time between notifications", description: "Minutes", required: false
            }              
	    }
	}
page name: "certainTime"
    def certainTime() {
        dynamicPage(name:"certainTime",title: "Only during a certain time", uninstall: false) {
            section("Beginning at....") {
                input "startingX", "enum", title: "Starting at...", options: ["A specific time", "Sunrise", "Sunset"], required: false , submitOnChange: true
                if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false, submitOnChange: true
                else {
                    if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                    else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                }
            }
            section("Ending at....") {
                input "endingX", "enum", title: "Ending at...", options: ["A specific time", "Sunrise", "Sunset"], required: false, submitOnChange: true
                if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false, submitOnChange: true
                else {
                    if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                    else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                }
            }
        }
    }

/************************************************************************************************************
		
************************************************************************************************************/
def installed() {
	log.debug "Installed with settings: ${settings}, current app version: ${release()}"
    state.NotificationRelease = "Notification: " + release()
	state.sound
	if (myWeatherAlert) {
		runEvery5Minutes(mGetWeatherAlerts)
        mGetWeatherAlerts()
	}
	if (myWeather) {
		runEvery1Hour(mGetCurrentWeather)
        mGetCurrentWeather()
	}    
}
def updated() {
	log.debug "Updated with settings: ${settings}, current app version: ${release()}"
	state.NotificationRelease = "Notification: " + release()
    state.lastPlayed = null
    state.sound
	unschedule()
    unsubscribe()
    initialize()
}
def initialize() {
	unschedule()
	state.lastTime
    state.lastWeatherCheck
    state.lastAlert
    state.cycleOnH = false
    state.cycleOnL = false
    state.cycleOnA = false
    state.cycleOnB = false
    state.savedOffset = false
    state.cycleTh = false
    state.cycleTl = false
   
   state.cycleSh = false
    state.cycleSl = false    
    
    state.cycleHh = false
    state.cycleHl = false    
    
    state.cycleCO2h = false
    state.cycleCO2l = false    
    
    state.lastWeather
	state.message = null
    state.occurrences = 0
    if (mySunState == "Sunset") {
    subscribe(location, "sunsetTime", sunsetTimeHandler)
	sunsetTimeHandler(location.currentValue("sunsetTime"))
    }
    if (mySunState == "Sunrise") {
	subscribe(location, "sunriseTime", sunriseTimeHandler)
	sunriseTimeHandler(location.currentValue("sunriseTime"))
    }
    if (frequency) cronHandler(frequency)
    if (myWeatherAlert) {
		runEvery5Minutes(mGetWeatherAlerts)
		state.weatherAlert
        mGetWeatherAlerts()
    }
    if (myWeatherTriggers) {
    	"${myWeatherCheck}"(mGetWeatherTrigger)
         mGetWeatherTrigger()
    }     
	if (myWeather) {
    	log.debug "refreshing hourly weather"
		runEvery1Hour(mGetCurrentWeather)
        state.lastWeather = null
        state.lastWeatherCheck = null
       	mGetCurrentWeather()
	}
	if (actionType && actionType != "Ad-Hoc Report") {
        if(myPower) 							subscribe(myPower, "power", meterHandler)
        if (myRoutine) 							subscribe(location, "routineExecuted",alertsHandler)
        if (myMode) 							subscribe(location, "mode", alertsHandler)
        if (mySwitch) {
            if (mySwitchS == "on")				subscribe(mySwitch, "switch.on", alertsHandler)
            if (mySwitchS == "off")				subscribe(mySwitch, "switch.off", alertsHandler)
            if (mySwitchS == "both" || mySwitchS == null )			subscribe(mySwitch, "switch", alertsHandler)
        }    
        if (myContact) {
            if (myContactS == "open")			subscribe(myContact, "contact.open", alertsHandler)
            if (myContactS == "closed")			subscribe(myContact, "contact.closed", alertsHandler)
            if (myContactS == "both" || myContactS == null)			subscribe(myContact, "contact", alertsHandler)
        }
        if (myMotion) {
            if (myMotionS == "active")			subscribe(myMotion, "motion.active", alertsHandler)
            if (myMotionS == "inactive")		subscribe(myMotion, "motion.inactive", alertsHandler)
            if (myMotionS == "both" || myMotionS == null)			subscribe(myMotion, "motion", alertsHandler)
        }    
        if (myLocks) {
            if (myLocksS == "locked")			subscribe(myLocks, "lock.locked", alertsHandler)
            if (myLocksS == "unlocked"){
            	if (myLocksSCode){
            									subscribe(myLocks, "lock", unlockedWithCodeHandler)
                }
                else 							subscribe(myLocks, "lock.unlocked", alertsHandler)                                                                     
            }
            if (myLocksS == "both" || myLocksS == null)				subscribe(myLocks, "lock", alertsHandler)
        }
        if (myPresence) {
            if (myPresenceS == "present")		subscribe(myPresence, "presence.present", alertsHandler)
            if (myPresenceS == "not present")	subscribe(myPresence, "presence.not present", alertsHandler)
            if (myPresenceS == "both" || myPresenceS == null )			subscribe(myPresence, "presence", alertsHandler)
        }
        if (myTstat) {    
            if (myTstatS == "cooling")			subscribe(myTstat, "coolingSetpoint", alertsHandler)
            if (myTstatS == "heating")			subscribe(myTstat, "heatingSetpoint", alertsHandler)
            if (myTstatS == "both" || myTstatS == "null"){
            	subscribe(myTstat, "coolingSetpoint", alertsHandler)
                subscribe(myTstat, "heatingSetpoint", alertsHandler)
            }
            if (myTstatM == "auto")				subscribe(myTstat, "thermostatMode.auto", alertsHandler)
            if (myTstatM == "cool")				subscribe(myTstat, "thermostatMode.auto.cool", alertsHandler)
            if (myTstatM == "heat")				subscribe(myTstat, "thermostatMode.heat", alertsHandler)        
            if (myTstatM == "off")				subscribe(myTstat, "thermostatMode.off", alertsHandler)
            if (myTstatM == "every mode")		subscribe(myTstat, "thermostatMode", alertsHandler)
            
            
            if (myTstatOS == "cooling")			subscribe(myTstat, "thermostatOperatingState.cooling", alertsHandler)
            if (myTstatOS == "heating")			subscribe(myTstat, "thermostatOperatingState.heating", alertsHandler)
            if (myTstatOS == "idle")			subscribe(myTstat, "thermostatOperatingState.idle", alertsHandler)
            if (myTstatOS == "every state")		subscribe(myTstat, "thermostatOperatingState", alertsHandler)
		}
        if (mySmoke) {    
            if (mySmokeS == "detected")			subscribe(mySmoke, "smoke.detected", alertsHandler)
            if (mySmokeS == "clear")			subscribe(mySmoke, "smoke.clear", alertsHandler)
            if (mySmokeS == "both" || mySmokeS == null)				subscribe(mySmoke, "smoke", alertsHandler)
        }
        if (myWater) {    
            if (myWaterS == "wet")				subscribe(myWater, "water.wet", alertsHandler)
            if (myWaterS == "dry")				subscribe(myWater, "water.dry", alertsHandler)
            if (myWaterS == "both" || myWaterS == null)				subscribe(myWater, "water", alertsHandler)
      	}
			if (myTemperature) 					subscribe(myTemperature, "temperature", tempHandler)    
            if (myCO2)							subscribe(myCO2, "carbonDioxide", CO2Handler)
            if (myCO){
            	if (myCOS== "detected")            	subscribe(myCO, "carbonMonoxide.detected", alertsHandler)
				if (myCOS== "tested")				subscribe(myCO, "carbonMonoxide.tested", alertsHandler)
    			if (myCOS== "both")					subscribe(myCO, "carbonMonoxide", alertsHandler)
            }
            if (myHumidity)						subscribe(myHumidity, "humidity", humidityHandler)
            if (mySound)						subscribe(mySound, "soundPressureLevel", soundHandler)
            
            if (myAcceleration){
            	if (myAccelerationS == "active")	subscribe(myAcceleration, "acceleration.active", alertsHandler)
                if (myAccelerationS == "inactive")	subscribe(myAcceleration, "acceleration.inactive", alertsHandler)
                if (myAccelerationS == "both")		subscribe(myAcceleration, "acceleration", alertsHandler)
    		}
    }
}
/******************************************************************************************************
   PARENT STATUS CHECKS
******************************************************************************************************/
def checkRelease() {
return state.NotificationRelease
}
/************************************************************************************************************
   RUNNING REPORT FROM PARENT
************************************************************************************************************/
def runProfile(profile) {
	log.warn "received command from the Parent for $profile"
	def result 
	if(message && actionType == "Ad-Hoc Report"){
    	// date, time and profile variables
        result = message ? "$message".replace("&date", "${getVar("date")}").replace("&time", "${getVar("time")}").replace("&profile", "${getVar("profile")}") : null
        result = result ? "$result".replace("&catfed", "${getVar("catfed")}").replace("&catshot", "${getVar("catshot")}").replace("&catmed", "${getVar("catmed")}").replace("&catbath", "${getVar("catbath")}").replace("&catwalk", "${getVar("catwalk")}").replace("&catbrush", "${getVar("catbrush")}") : null
        result = result ? "$result".replace("&dogshot", "${getVar("dogshot")}").replace("&dogfed", "${getVar("dogfed")}").replace("&dogwalk", "${getVar("dogwalk")}").replace("&dogbath", "${getVar("dogbath")}").replace("&dogmed", "${getVar("dogmed")}").replace("&dogbrush", "${getVar("dogbrush")}") : null
        result = result ? "$result".replace("&catfed1", "${getVar("catfed1")}").replace("&catshot1", "${getVar("catshot1")}").replace("&catmed1", "${getVar("catmed1")}").replace("&catbath1", "${getVar("catbath1")}").replace("&catwalk1", "${getVar("catwalk1")}").replace("&catbrush1", "${getVar("catbrush1")}") : null
        result = result ? "$result".replace("&dogshot1", "${getVar("dogshot1")}").replace("&dogfed1", "${getVar("dogfed1")}").replace("&dogwalk1", "${getVar("dogwalk1")}").replace("&dogbath1", "${getVar("dogbath1")}").replace("&dogmed1", "${getVar("dogmed1")}").replace("&dogbrush1", "${getVar("dogbrush1")}") : null
        result = result ? "$result".replace("&litterclean", "${getVar("litterclean")}").replace("&litterscoop", "${getVar("litterscoop")}") : null
        // power variables
        result = result ? "$result".replace("&power", "${getVar("power")}").replace("&lights", "${getVar("lights")}") : null
        // garage doors, locks and precence variables
        result = result ? "$result".replace("&garage", "${getVar("garage")}").replace("&unlocked", "${getVar("unlocked")}").replace("&present", "${getVar("present")}") : null
		// doors and windows variables
        result = result ? "$result".replace("&doors", "${getVar("doors")}").replace("&windows", "${getVar("windows")}").replace("&open", "${getVar("open")}") : null
		// location variables
        result = result ? "$result".replace("&mode", "${getVar("mode")}").replace("&shm", "${getVar("shm")}") : null
		//thermostat variables
        result = result ? "$result".replace("&thermostat", "${getVar("thermostat")}").replace("&running", "${getVar("running")}").replace("&temperature", "${getVar("temperature")}")  : null
		//thermostat setPoints
        result = result ? "$result".replace("&heating", "${getVar("heating")}").replace("&cooling", "${getVar("cooling")}").replace("&motion", "${getVar("motion")}")  : null
        //weather variables
        result = getWeatherVar(result) 
    }
	if(message && actionType == "Custom"){
		def device = "<< Device Label >>"
        def action = " << Action (on/off, open/closed, locked/unlocked) >>"
        def event =  " << Capability (switch, contact, motion, lock, etc) >>"
		result = message ? "$message".replace("&date", "${getVar("date")}").replace("&time", "${getVar("time")}").replace("&profile", "${getVar("profile")}") : null  
    	result = result ? "$result".replace("&device", "${device}").replace("&action", "${action}").replace("&event", "${event}") : null 
    }
    if(message && actionType == "Custom with Weather"){
		def device = "<< Device Label >>"
        def action = " << Action (on/off, open/closed, locked/unlocked) >>"
        def event =  " << Capability (switch, contact, motion, lock, etc) >>"
		result = message ? "$message".replace("&date", "${getVar("date")}").replace("&time", "${getVar("time")}").replace("&profile", "${getVar("profile")}") : null  
    	result = result ? "$result".replace("&device", "${device}").replace("&action", "${action}").replace("&event", "${event}") : null
       	result = getWeatherVar(result) 
    }
    if(message && profile == "test") {
    	result = "This is your Sample Report with current data:  \n\n"+ result
   		return result
    }
	else {
		if (actionType == "Ad-Hoc Report" && message) {
			return result
     	}
        else result = "Sorry you can only generate an ad-hoc report that has a custom message"
		log.warn "sending Report to Main App: $result"
	}
    
}
/************************************************************************************************************
   REPORT VARIABLES   
************************************************************************************************************/
//getVar(var)
private getVar(var) {
	def devList = []
    def result
    if (var == "time"){
        result = new Date(now()).format("h:mm aa", location.timeZone) 
    	return result
    }
    if (var == "date"){
        result = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
    	return result    
    }
    if (var == "catshot"){
    	result = parent.state.catShotNotify
        return result
    }    
    if (var == "catfed"){
    	result = parent.state.catFedNotify
        return result
    }    
    if (var == "catmed"){
    	result = parent.state.catMedNotify
        return result
    }    
    if (var == "catwalk"){
    	result = parent.state.catWalkNotify
        return result
    }    
    if (var == "catbath"){
    	result = parent.state.catBathNotify
        return result
	}
    if (var == "catbrush"){
    	result = parent.state.catBrushNotify
        return result
	}    
    if (var == "catshot1"){
    	result = parent.state.catShot1Notify
        return result
    }    
    if (var == "catfed1"){
    	result = parent.state.catFed1Notify
        return result
    }    
    if (var == "catmed1"){
    	result = parent.state.catMed1Notify
        return result
    }    
    if (var == "catwalk1"){
    	result = parent.state.catWalk1Notify
        return result
    }    
    if (var == "catbath1"){
    	result = parent.state.catBath1Notify
        return result
	}
    if (var == "catbrush1"){
    	result = parent.state.catBrush1Notify
        return result
	}    
    if (var == "dogshot"){
    	result = parent.state.dogShotNotify
        return result
    }    
    if (var == "dogfed"){
    	result = parent.state.dogFedNotify
        return result
    }    
    if (var == "dogmed"){
    	result = parent.state.dogMedNotify
        return result
    }    
    if (var == "dogwalk"){
    	result = parent.state.dogWalkNotify
        return result
    }    
    if (var == "dogbath"){
    	result = parent.state.dogBathNotify
        return result
    }
    if (var == "dogbrush"){
    	result = parent.state.dogBrushNotify
        return result
    }    
    if (var == "dogshot1"){
    	result = parent.state.dogShot1Notify
        return result
    }    
    if (var == "dogfed1"){
    	result = parent.state.dogFed1Notify
        return result
    }    
    if (var == "dogmed1"){
    	result = parent.state.dogMed1Notify
        return result
    }    
    if (var == "dogwalk1"){
    	result = parent.state.dogWalk1Notify
        return result
    }    
    if (var == "dogbath1"){
    	result = parent.state.dogBath1Notify
        return result
    }
    if (var == "dogbrush1"){
    	result = parent.state.dogBrush1Notify
        return result
    }
    if (var == "litterclean"){
    	result = parent.state.litterboxClean
        return result
    }
    if (var == "litterscoop"){
    	result = parent.state.litterboxScoop
        return result
    }    
    if (var == "mode"){
        result = location?.currentMode
    	return result 	    
    }
	if (var == "shm"){ 
		def sSHM = location?.currentState("alarmSystemStatus")?.value       
		sSHM = sSHM == "off" ? "disabled" : sSHM == "away" ? "Armed Away" : sSHM == "stay" ? "Armed Home" : "unknown"
		result = sSHM
        return result
    }    
    if (var == "power"){
        if (myPower){
            def meterValueRaw = myPower?.currentValue("power") as double
            int meter = meterValueRaw ?: 0 as int        
            result = meter
            return result
        }
    }
    if (var == "open"){
    	if (myContact){
            if (myContact?.latestValue("contact")?.contains("open")) {
                myContact?.each { deviceName ->
                    if (deviceName.latestValue("contact")=="open") {
                        String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " sensor"
            else if (devList?.size() > 0) result = devList?.size() + " sensors"
            else if (!devList) result = "no sensors"
            return result
    	}	
    }
    if (var == "doors"){
		if(parent.cDoor1){
			if (parent.cDoor1?.currentValue("contact").contains("open")) {
            	parent.cDoor1?.each { deviceName ->
                	if (deviceName.currentValue("contact")=="${"open"}") {
                    	String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " door"
            else if (devList?.size() > 0) result = devList?.size() + " doors"
            else if (!devList) result = "no doors"
            return result
    	}	
    }
    if (var == "windows"){                    
		if(parent.cWindow) {
			if (parent.cWindow?.currentValue("contact").contains("open")) {
            	parent.cWindow?.each { deviceName ->
                	if (deviceName.currentValue("contact")=="${"open"}") {
                    	String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " window"
            else if (devList?.size() > 0) result = devList?.size() + " windows"
            else if (!devList) result = "no windows"
            return result
    	}	
    }
    if (var == "unlocked"){                    
		if(myLocks) {
			if (myLocks.currentValue("lock").contains("unlocked")) {
            	myLocks.each { deviceName ->
                	if (deviceName.currentValue("lock")=="${"unlocked"}") {
                    	String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " door"
            else if (devList?.size() > 0) result = devList?.size() + " doors"
            else if (!devList) result = "no doors"
            return result
    	}	
    }
    if (var == "present"){                    
		if(myPresence) {
			if (myPresence.currentValue("presence").contains("present")) {
            	myPresence.each { deviceName ->
                	if (deviceName.currentValue("presence")=="${"present"}") {
                    	String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " person"
            else if (devList?.size() > 0) result = devList?.size() + " people"
            else if (!devList) result = "no people"
            return result
    	}	
    }    
    if (var == "lights"){
        if(mySwitch){
				mySwitch.each { deviceName ->
                    if (deviceName.latestValue("switch")=="on") {
                    	String device  = (String) deviceName
                        devList += device
                    }
				}
            if (devList?.size() == 1) result = devList?.size() + " switch"
            else if (devList?.size() > 0) result = devList?.size() + " switches"  
            else if (!devList) result = "no switches"
            return result
  		}
    }
    if (var == "thermostat"){    
        if(myTstat){
        def currentMode
				myTstat.each { deviceName ->
                	String device  = (String) deviceName
                    currentMode = deviceName.currentValue("thermostatMode") 
                        devList += device + " is set to "+ currentMode  
               	}
                if (!devList) result = "unknown"
                else if (devList) result = devList
                return result
   		}
   	}
    if (var == "heating"){    
        if(myTstat){
        def currentMode
				myTstat.each { deviceName ->
                	String device  = (String) deviceName
                    currentMode = deviceName.currentValue("heatingSetpoint") 
                        devList += device + " heating set point is "+ currentMode  
               	}
                if (!devList) result = "unknown"
                else if (devList) result = devList
                return result
   		}
   	}
    if (var == "cooling"){    
        if(myTstat){
        def currentMode
				myTstat.each { deviceName ->
                	String device  = (String) deviceName
                    currentMode = deviceName.currentValue("coolingSetpoint") 
                        devList += device + " cooling set point is "+ currentMode  
               	}
                if (!devList) result = "unknown"
                else if (devList) result = devList
                return result
   		}
   	}
	if (var == "running"){    
        if(myTstat){
        def currentOS
				myTstat.each { deviceName ->
                	String device  = (String) deviceName
                    currentOS = deviceName.currentValue("thermostatOperatingState")
                        devList += currentOS
               	}
                def tSize = devList?.size()
                log.warn "size = $tSize, $currentOS, $devList "
                if (devList && !devList.contains("idle")) result = "running"
                else if (devList && (devList.contains("cooling") || devList.contains("heating")))  result = "running"
                else if (devList && devList.contains("idle") && !devList.contains("cooling") && !devList.contains("heating")) result = "not running"
                else if (devList && devList.contains("fan only"))  result = "running the fan only"
                else result = "unknown"
                return result
   		}
   	}    
    if (var == "temperature"){    
    	if(myTstat){
           	def total = 0
    		myTstat?.each {total += it.currentValue("temperature")}
           int avgT = total as Integer
        result = Math.round(total/myTstat?.size())
        return result
		}
    }
    if (var == "motion"){
		if(parent.cMotion){
			if (parent.cMotion?.currentValue("motion").contains("active")) {
            	parent.cMotion?.each { deviceName ->
                	if (deviceName.currentValue("motion")=="${"active"}") {
                    	String device  = (String) deviceName
                        devList += device
                    }
                }
            }
            if (devList?.size() == 1)  result = devList?.size() + " motion sensor"
            else if (devList?.size() > 0) result = devList?.size() + " motion sensors"
            else if (!devList) result = "no motion sensors"
            return result
    	}	
    }
    if (var == "garage"){    
    	if(parent.cDoor){
            result = parent.cDoor.latestValue("contact").contains("open") ? "open" : "closed"  
    	}
        else if (parent.cRelay && parent.cContactRelay)
        	result = parent.cContactRelay.latestValue("contact").contains("open") ? "open" : "closed"  
        return result
	}
}
/***********************************************************************************************************************
    POWER HANDLER
***********************************************************************************************************************/
def meterHandler(evt) {
        def data = [:]
        def eVal = evt.value
        def eName = evt.name
        def eDev = evt.device
        def eDisplayN = evt.displayName
        int delay = minutes ?: 0
            delay = delay ?: 0 as int
        int meterValueRaw = evt.value as double
            int meterValue = meterValueRaw ?: 0 as int
        int thresholdValue = threshold == null ? 0 : threshold as int
        int thresholdStopValue = thresholdStop == null ? 0 : thresholdStop as int
        def cycleOnHigh = state.cycleOnH
        def cycleOnLow = state.cycleOnL
        //log.warn "meterValue = $meterValue , cycleOnLow = $cycleOnLow"
        if(myPowerS == "above threshold"){
            thresholdStopValue = thresholdStopValue == 0 ? 9999 :  thresholdStopValue as int
            if (meterValue >= thresholdValue && meterValue <= thresholdStopValue ) {
                if (cycleOnHigh == false){
                    state.cycleOnH = true
                    log.debug "Power meter $meterValue is above threshold $thresholdValue with threshold stop $thresholdStopValue"
                    if (delay) {
                        log.warn "scheduling delay ${delay}, ${60*delay}"
                        runIn(60*delay , bufferPendingH)
                    }
                    else {
                        log.debug "sending notification (above)" 
                        data = [value:"above threshold", name:"power", device:"power meter"]
                        alertsHandler(data)
                    }
                }
            }
            else {
                state.cycleOnH = false
                unschedule("bufferPendingH")
                //log.debug "Power exception (above) meterValue ${meterValue}, thresholdValue ${thresholdValue}, stop ${thresholdStopValue} "
            }
        }
        if(myPowerS == "below threshold"){
            if (meterValue <= thresholdValue && meterValue >= thresholdStopValue) {
                if (cycleOnLow == false){
                    state.cycleOnL = true
                    log.debug "Power meter $meterValue is below threshold $thresholdValue with threshold stop $thresholdStopValue"
                    if (delay) {
                        log.warn "scheduling delay ${delay}, ${60*delay}"
                        runIn(60*delay, bufferPendingL)
                    }
                    else {
                        log.debug "sending notification (below)" 
                        data = [value:"below threshold", name:"power", device:"power meter"]
                        alertsHandler(data)
                    }
                }
            }
            else {
                state.cycleOnL = false
                unschedule("bufferPendingL")
                //log.debug "Power exception (below) meterValue ${meterValue}, thresholdValue ${thresholdValue}, stop ${thresholdStopValue}"
            }
        }
	}
def bufferPendingH() {  
	def meterValueRaw = myPower.currentValue("power") as double
    	int meterValue = meterValueRaw ?: 0 as int
    def thresholdValue = threshold == null ? 0 : threshold as int
    if (meterValue >= thresholdValue) {
		log.debug "sending notification (above)" 
        def data = [value:"above threshold", name:"power", device:"power meter"] 
    	alertsHandler(data)
   }
}
def bufferPendingL() {  
    def meterValueRaw = myPower.currentValue("power") as double 
		int meterValue = meterValueRaw ?: 0 as int    
    def thresholdValue = threshold == null ? 0 : threshold as int
    if (meterValue <= thresholdValue) {
		log.debug "sending notification (below)" 
       	def data = [value:"below threshold", name:"power", device:"power meter"] 
    	alertsHandler(data)
  	}
}
/************************************************************************************************************
   UNLOCKED WITH USER CODE
************************************************************************************************************/
def unlockedWithCodeHandler(evt) {
	def event = evt.data
    def eVal = evt.value
    def eName = evt.name
    def eDev = evt.device
    def eDisplayN = evt.displayName
    def eDisplayT = evt.descriptionText
	def data = [:]
    def eTxt = eDisplayN + " is " + eVal //evt.descriptionText 
    log.info "unlocked event received: event = $event, eVal = $eVal, eName = $eName, eDev = $eDev, eDisplayN = $eDisplayN, eDisplayT = $eDisplayT, eTxt = $eTxt"
			if(eVal == "unlocked" && myLocksSCode && event) {
                def userCode = evt.data.replaceAll("\\D+","")
                userCode = userCode.toInteger()
       			int usedCode = userCode 
            	if(myLocksSCode == usedCode){
					eTxt = message ? "$message".replace("&device", "${eDisplayN}").replace("&event", "time").replace("&action", "executed").replace("&date", "${today}").replace("&time", "${stamp}").replace("&profile", "${eProfile}") : eDisplayT
                    data = [value:eTxt, name:"unlocked with code", device:"lock"]
					alertsHandler(data)
				} 
            }
}
/***********************************************************************************************************************
    TEMPERATURE HANDLER
***********************************************************************************************************************/
def tempHandler(evt) {
        def data = [:]
        def eVal = evt.value
        def eName = evt.name
        def eDev = evt.device
        def eDisplayN = evt.displayName
		log.info "event received: event = $event, eVal = $eVal, eName = $eName, eDev = $eDev, eDisplayN = $eDisplayN"
        def tempAVG = myTemperature ? getAverage(myTemperature, "temperature") : "undefined device"          
        def cycleThigh = state.cycleTh
        def cycleTlow = state.cycleTl        
        def currentTemp = tempAVG
        int temperatureStopVal = temperatureStop == null ? 0 : temperatureStop as int
        log.warn "currentTemp = $currentTemp"
        if(myTemperatureS == "above"){
        	temperatureStopVal = temperatureStopVal == 0 ? 999 :  temperatureStopVal as int
            if (currentTemp >= temperature && currentTemp <= temperatureStopVal) {
                if (cycleThigh == false){
                    state.cycleTh = true
                    log.debug "sending notification (above): as temperature $currentTemp is above threshold $temperature" 
                        data = [value:"above ${temperature} degrees", name:"temperature", device:"temperature sensor"]
                        alertsHandler(data)
                }
            }
            else state.cycleTh = false
        }
        if(myTemperatureS == "below"){
            if (currentTemp <= temperature && currentTemp >= temperatureStopVal) {
                if (cycleTlow == false){
                    state.cycleTl = true
					log.debug "sending notification (below): as temperature $currentTemp is below threshold $temperature"
                        data = [value:"below ${temperature} degrees", name:"temperature", device:"temperature sensor"]
                        alertsHandler(data)
                }
            }
            else state.cycleTl = false
        }
	}
/******************************************************************************
	 FEEDBACK SUPPORT - GET AVERAGE										
******************************************************************************/
def getAverage(device,type){
	def total = 0
		if(debug) log.debug "calculating average temperature"  
    device.each {total += it.latestValue(type)}
    return Math.round(total/device?.size())
}
/***********************************************************************************************************************
    HUMIDITY HANDLER
***********************************************************************************************************************/
def humidityHandler(evt){
        def data = [:]
        def eVal = evt.value
        	eVal = eVal as int
        def eName = evt.name
        def eDev = evt.device
        def eDisplayN = evt.displayName
		log.info "event received: event = $event, eVal = $eVal, eName = $eName, eDev = $eDev, eDisplayN = $eDisplayN"
        if(myHumidityS == "above"){
            if (eVal >= humidity) {
                if (state.cycleHh == false){
                    state.cycleHh = true            
                    log.debug "sending notification (above): as humidity $eVal is above threshold $humidity" 
                        data = [value:"above ${humidity}", name:"humidity", device:"humidity sensor"]
                        alertsHandler(data)
            	}
           }
            else state.cycleHh = false
        }
        else {
            if(myHumidityS == "below"){
                if (eVal <= humidity) {
                    if (state.cycleHl == false){
                        state.cycleHl = true                
                            log.debug "sending notification (below): as humidity $eVal is below threshold $humidity"
                                //data = [value:"below temperature", name:"temperature", device:"temperature sensor"]
                                data = [value:"below ${humidity}", name:"humidity", device:"humidity sensor"]
                                alertsHandler(data)
                    }
                }
          	else state.cycleHl = false
            }
    	}
	}
/***********************************************************************************************************************
    SOUND HANDLER
***********************************************************************************************************************/
def soundHandler(evt){
        def data = [:]
        def eVal = evt.value
        	eVal = eVal as int
        def eName = evt.name
        def eDev = evt.device
        def eDisplayN = evt.displayName
		log.info "event received: event = $event, eVal = $eVal, eName = $eName, eDev = $eDev, eDisplayN = $eDisplayN"
        if(mySoundS == "above"){
            if (eVal >= noise) {
                if (state.cycleSh == false){
                    state.cycleSh = true             
                    log.debug "sending notification (above): as noise $eVal is above threshold $noise" 
                        data = [value:"above ${noise}", name:"noise", device:"sound sensor"]
                        alertsHandler(data)
            	}
        	}
            else state.cycleSh = false
        }
        else {
            if(mySoundS == "below"){
                if (eVal <= noise) {
                    if (state.cycleSl == false){
                        state.cycleSl = true                
                            log.debug "sending notification (below): as noise $eVal is below threshold $noise"
                                data = [value:"below ${noise}", name:"noise", device:"sound sensor"]
                                alertsHandler(data)
                    }
            	}
                else state.cycleSl = false
    		}
        }
	}
/***********************************************************************************************************************
    CO2 HANDLER
***********************************************************************************************************************/
def CO2Handler(evt){
        def data = [:]
        def eVal = evt.value
        	eVal = eVal as int
        def eName = evt.name
        def eDev = evt.device
        def eDisplayN = evt.displayName
		log.info "event received: event = $event, eVal = $eVal, eName = $eName, eDev = $eDev, eDisplayN = $eDisplayN"
        if(myCO2S == "above"){
            if (eVal >= CO2) {
                if (state.cycleCO2h == false){
                    state.cycleCO2h = true              
                    log.debug "sending notification (above): as CO2 $eVal is above threshold $CO2" 
                        data = [value:"${eVal}", name:"CO2", device:"CO2 sensor"]
                        alertsHandler(data)
            	}
        	}
        	else state.cycleCO2h = false
        }
        else {
            if(myCO2S == "below"){
                if (eVal <= CO2) {
                    if (state.cycleCO2l == false){
                        state.cycleCO2l = true                 
                            log.debug "sending notification (below): as CO2 $eVal is below threshold $CO2" 
                                data = [value:"${eVal}", name:"CO2", device:"CO2 sensor"]
                                alertsHandler(data)
                    }
            	}
           	else state.cycleCO2l = false
    		}
        }
	}
/************************************************************************************************************
   EVENTS HANDLER
************************************************************************************************************/
def alertsHandler(evt) {
	def event = evt.data
    def eVal = evt.value
    def eName = evt.name
    def eDev = evt.device
    def eDisplayN = evt.displayName
    def eDisplayT = evt.descriptionText
	if(eDisplayN == null) eDisplayN = eName
    state.occurrences = 1
    def eTxt = eDisplayN + " is " + eVal //evt.descriptionText 
    log.info "event received: event = $event, eVal = $eVal, eName = $eName, eDev = $eDev, eDisplayN = $eDisplayN, eDisplayT = $eDisplayT, eTxt = $eTxt"
    log.warn "version number = ${release()}"
    //FAST LANE AUDIO DELIVERY METHOD
    if(actionType == "Default"){
		if(speechSynth) {
       	speechSynth.playTextAndResume(eTxt)
        sendtxt(eTxt)
        }
        else{
            if(sonos) {
                def sCommand = resumePlaying == true ? "playTrackAndResume" : "playTrackAndRestore"
                def sTxt = textToSpeech(eTxt instanceof List ? eTxt[0] : eTxt)
                def sVolume = settings.sonosVolume ?: 20
                sonos."${sCommand}"(sTxt.uri, sTxt.duration, sVolume)
                sendtxt(eTxt)
            }
        }
  	}
    else {
            if (actionType == "Triggered Report" && myProfile) {
                eTxt = parent.runReport(myProfile)
            }
            def eProfile = app.label
            def nRoutine = false
            def stamp = state.lastTime = new Date(now()).format("h:mm aa", location.timeZone)     
            def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)

            if (getDayOk()==true && getModeOk()==true && getTimeOk()==true && getFrequencyOk()==true && getConditionOk()==true) {	
                if(eName == "time of day" && message){
                        eTxt = message ? "$message".replace("&device", "${eDisplayN}").replace("&event", "time").replace("&action", "executed").replace("&date", "${today}").replace("&time", "${stamp}").replace("&profile", "${eProfile}") : null
                            if(actionType == "Custom with Weather") eTxt = getWeatherVar(eTxt)
                }
                if(eName == "coolingSetpoint" || eName == "heatingSetpoint") {
                    eVal = evt.value.toFloat()
                    eVal = Math.round(eVal)
                }
                if(eName == "routineExecuted" && myRoutine) {
                    def deviceMatch = myRoutine?.find {r -> r == eDisplayN}  
                    if (deviceMatch){
                        eTxt = message ? "$message".replace("&device", "${eDisplayN}").replace("&event", "routine").replace("&action", "executed").replace("&date", "${today}").replace("&time", "${stamp}").replace("&profile", "${eProfile}") : null
                            if(actionType == "Custom with Weather") eTxt = getWeatherVar(eTxt)
                            if (message){
                                if(recipients?.size()>0 || sms?.size()>0 || push) {
                                    sendtxt(eTxt)
                                }
                                takeAction(eTxt)
                            }
                            else {
                                eTxt = "routine was executed"
                                takeAction(eTxt) 
                            }
                    }
                }
                else {
                    if(eName == "mode" && myMode) {
                        def deviceMatch = myMode?.find {m -> m == eVal}  
                        if (deviceMatch){
                            eTxt = message ? "$message".replace("&device", "${eVal}").replace("&event", "${eName}").replace("&action", "changed").replace("&date", "${today}").replace("&time", "${stamp}").replace("&profile", "${eProfile}") : null
                            if(actionType == "Custom with Weather") eTxt = getWeatherVar(eTxt)
                            if (message){
                                if(recipients?.size()>0 || sms?.size()>0 || push) {
                                    sendtxt(eTxt)
                                }
                                takeAction(eTxt)
                            }
                            else {
                                eTxt = "location mode has changed"
                                takeAction(eTxt) 
                            }
                        }
                    }        
                    else {
                        if (message || actionType == "Triggered Report"){      
                            if(message){
                            eTxt = message ? "$message".replace("&device", "${eDev}").replace("&event", "${eName}").replace("&action", "${eVal}").replace("&date", "${today}").replace("&time", "${stamp}").replace("&profile", "${eProfile}") : null
                            if(actionType == "Custom with Weather") eTxt = getWeatherVar(eTxt)
                            }
                            if(eTxt){
                                if(recipients?.size()>0 || sms?.size()>0 || push) {
                                    sendtxt(eTxt)
                                }
                                takeAction(eTxt)
                            }
                        }
                        else {
                            if (eDev == "weather"){
                                if (eDisplayN == "weather alert"){
                                    eTxt = eVal
                                }
                                else eTxt = eDisplayN + " is " + eVal
                            }
                            if(eTxt){
                                if(recipients?.size()>0 || sms?.size()>0 || push) {
                                    log.warn "sending sms"
                                    sendtxt(eTxt)
                                }
                                log.warn "processing eTxt = $eTxt"
                                takeAction(eTxt)
                            }
                        }
                    }
                }
            }
        }
}
/***********************************************************************************************************************
    TAKE ACTIONS HANDLER
***********************************************************************************************************************/
private takeAction(eTxt) {
    //Sending Data to WebCore
    def data = [args: eTxt ]
	sendLocationEvent(name: "echoSistantProfile", value: app.label, data: data, displayed: true, isStateChange: true, descriptionText: "EchoSistant activated '${app.label}' profile.")
	if (parent.debug) log.debug "sendNotificationEvent sent to CoRE was '${app.label}' from the TTS process section"

	state.savedOffset = false
	def sVolume
    def sTxt
    //int prevDuration
    double prevDuration
    if(state.sound) prevDuration = state.sound.duration as Double
    if(sonosDelay)	prevDuration = prevDuration + sonosDelay
    if(myProfile && actionType != "Triggered Report") runProfileActions()   
    if (actionType == "Custom" || actionType == "Custom with Weather" || actionType == "Triggered Report") {
        if (speechSynth || sonos) sTxt = textToSpeech(eTxt instanceof List ? eTxt[0] : eTxt)
        state.sound = sTxt
    }
    else {
    	loadSound()
        state.lastPlayed = now()
        sTxt = state.sound
    }
    //Playing Audio Message
        if (speechSynth) {
            def currVolLevel = speechSynth.latestValue("level")
            def currMute = speechSynth.latestValue("mute")
                log.debug "vol switch = ${currVolSwitch}, vol level = ${currVolLevel}, currMute = ${currMute} "
                sVolume = settings.speechVolume ?: 30 
                speechSynth?.playTextAndResume(eTxt, sVolume)
                state.lastPlayed = now()
                log.info "Playing message on the speech synthesizer'${speechSynth}' at volume '${sVolume}'"
        }
        if (sonos) { 
            def currVolLevel = sonos.latestValue("level") //as Integer
            currVolLevel = currVolLevel[0]
            def currMuteOn = sonos.latestValue("mute").contains("muted")
                if (currMuteOn) { 
                    log.error "speaker is on mute, sending unmute command"
                    sonos.unmute()
                }
                sVolume = settings.sonosVolume ?: 20
                sVolume = (sVolume == 20 && currVolLevel == 0) ? sVolume : sVolume !=20 ? sVolume: currVolLevel
                def sCommand = resumePlaying == true ? "playTrackAndResume" : "playTrackAndRestore"
                if (!state.lastPlayed) {
					log.info "playing first message"
					sonos?."${sCommand}"(sTxt.uri, Math.max((sTxt.duration as Integer),2), sVolume)
					state.lastPlayed = now()
					state.sound.command = sCommand
					state.sound.volume = sVolume
                }
                else {
                	def elapsed = now() - state.lastPlayed
                	def elapsedSec = elapsed/1000
                	def timeCheck = prevDuration * 1000
                    if(elapsed < timeCheck){
                    	def delayNeeded = prevDuration - elapsedSec
                        if(delayNeeded > 0 ) delayNeeded = delayNeeded + 2
                        log.error "message is already playing, delaying new message by $delayNeeded seconds (raw delay = $prevDuration, elapsed time = $elapsedSec)"
                        state.sound.command = sCommand
                        state.sound.volume = sVolume
                        state.lastPlayed = now()
                        runIn(delayNeeded , delayedMessage)
                	}
                    else {
                    	log.info "playing message without delay"
                		sonos?."${sCommand}"(sTxt.uri, Math.max((sTxt.duration as Integer),2), sVolume)
                        state.lastPlayed = now()
						state.sound.command = sCommand
                        state.sound.volume = sVolume
                	}
              }
        }      
        if(retrigger){
        	if(state.occurrences == 1) {
            	log.warn "scheduling reminders"
        		"${retrigger}"(retriggerHandler)
                state.message = eTxt
        	}
        	else if (state.occurrences >= howManyTimes) {
            	unschedule("retriggerHandler")
                state.message = null
                state.occurrences = 0
                log.warn "canceling reminders"
        	}
        }
}

def delayedMessage() {
def sTxt = state.sound
sonos?."${sTxt.command}"(sTxt.uri, Math.max((sTxt.duration as Integer),2), sTxt.volume)
log.info "delayed message is now playing"
}
/***********************************************************************************************************************
    RETRIGGER
***********************************************************************************************************************/
def retriggerHandler () {
	def message = "In case you misssed it " + state.message
    state.occurrences =  state.occurrences + 1
    if(continueOnChange == true) {
		log.info "processing retrigger with message = $message"
		if(recipients?.size()>0 || sms?.size()>0 || push) {
			sendtxt(message)
  		}			
    	takeAction(message)
   	}
    else { 
   		if (getDayOk()==true && getModeOk()==true && getTimeOk()==true && getFrequencyOk()==true && getConditionOk()==true) {
			log.info "processing retrigger with message = $message"
            if(recipients?.size()>0 || sms?.size()>0 || push) {
				sendtxt(message)
            }			
            takeAction(message)
    	}
   	}
}
/***********************************************************************************************************************
    CUSTOM WEATHER VARIABLES
***********************************************************************************************************************/
private getWeatherVar(eTxt){
	def result
    // weather variables
	def weatherToday = mGetWeatherVar("today")
	def weatherTonight = mGetWeatherVar("tonight")
    def weatherTomorrow = mGetWeatherVar("tomorrow")
    def tHigh = mGetWeatherVar("high")
    def tLow = mGetWeatherVar("low")
    def tUV = mGetWeatherElements("uv")
    def tPrecip = mGetWeatherElements("precip")
    def tHum = mGetWeatherElements("hum")
    def tCond = mGetWeatherElements("cond")
    def tWind = mGetWeatherElements("wind")
    def tSunset = mGetWeatherElements("set")
    def tSunrise = mGetWeatherElements("rise")
    def tTemp = mGetWeatherElements("current")
    //def tWind = mGetWeatherElements("moonphase")

    result = eTxt.replace("&today", "${weatherToday}").replace("&tonight", "${weatherTonight}").replace("&tomorrow", "${weatherTomorrow}")
	if(result) result = result.replace("&high", "${tHigh}").replace("&low", "${tLow}").replace("&wind", "${tWind}").replace("&uv", "${tUV}").replace("&precipitation", "${tPrecip}")
	if(result) result = result.replace("&humidity", "${tHum}").replace("&conditions", "${tCond}").replace("&set", "${tSunset}").replace("&rise", "${tSunrise}").replace("&current", "${tTemp}")

	return result
}
/***********************************************************************************************************************
    WEATHER TRIGGERS
***********************************************************************************************************************/
def mGetWeatherTrigger(){
    def data = [:]
    def myTrigger
	def process = false
	try{  
        	if(getMetric() == false){
            def cWeather = getWeatherFeature("conditions", settings.wZipCode)
            def cTempF = cWeather.current_observation.temp_f.toDouble()
            	int tempF = cTempF as Integer
            def cRelativeHum = cWeather.current_observation.relative_humidity 
            	cRelativeHum = cRelativeHum?.replaceAll("%", "")
                int humid = cRelativeHum as Integer
            def cWindGustM = cWeather.current_observation.wind_gust_mph.toDouble()
            	int wind = cWindGustM as Integer
            def cPrecipIn = cWeather.current_observation.precip_1hr_in.toDouble()
            	double precip = cPrecipIn //as double
                	precip = 1 + precip //precip
                log.info "current triggers: precipitation = $precip, humidity = $humid, wind = $wind, temp = $tempF"
			myTrigger = myWeatherTriggers == "Chance of Precipitation (in/mm)" ? precip : myWeatherTriggers == "Wind Gust (MPH/kPH)" ? wind : myWeatherTriggers == "Humidity (%)" ? humid : myWeatherTriggers == "Temperature (F/C)" ? tempF : null
			}
            else {
			def cWeather = getWeatherFeature("conditions", settings.wZipCode)   
            def cTempC = cWeather.current_observation.temp_c.toDouble()
            	int tempC = cTempC as Integer
            def cRelativeHum = cWeather.current_observation.relative_humidity 
            	cRelativeHum = cRelativeHum?.replaceAll("%", "")
                int humid = cRelativeHum as Integer
            def cWindGustK = cWeather.current_observation.wind_gust_kph.toDouble()
            	int windC = cWindGustK as Integer
            def cPrecipM = cWeather.current_observation.precip_1hr_metric.toDouble()
    			double  precipC = cPrecipM as double
                
            myTrigger = myWeatherTriggers == "Chance of Precipitation (in/mm)" ? precipC : myWeatherTriggers == "Wind Gust (MPH/kPH)" ? windC : myWeatherTriggers == "Humidity (%)" ? humid : myWeatherTriggers == "Temperature (F/C)" ? tempC : null
            }
            def myTriggerName = myWeatherTriggers == "Chance of Precipitation (in/mm)" ? "Precipitation" : myWeatherTriggers == "Wind Gust (MPH/kPH)" ? "Wind Gusts" : myWeatherTriggers == "Humidity (%)" ? "Humidity" : myWeatherTriggers == "Temperature (F/C)" ? "Temperature" : null
            if (myWeatherTriggersS == "above" && state.cycleOnA == false){
            	def var = myTrigger > myWeatherThreshold
            	log.warn  " myTrigger = $myTrigger , myWeatherThreshold = $myWeatherThreshold, myWeatherTriggersS = $myWeatherTriggersS, var = $var"
                if(myTrigger > myWeatherThreshold) {
                	process = true
                    state.cycleOnA = process
                    state.cycleOnB = false
       			}
            }
     		if (myWeatherTriggersS == "below" && state.cycleOnB == false){
				def var = myTrigger <= myWeatherThreshold
            	log.warn  " myTrigger = $myTrigger , myWeatherThreshold = $myWeatherThreshold myWeatherTriggersS = $myWeatherTriggersS, var = $var"
        		if(myTrigger <= myWeatherThreshold) {
                	process = true
					state.cycleOnA = false
                    state.cycleOnB = process
       			}
            }
       		if(process == true){
				//data = [value:"${myTrigger}", name:"${myWeatherTriggers}", device:"${myWeatherTriggers}"] 4/5/17 Bobby
				data = [value:"${myTrigger}", name:"${myTriggerName}", device:"weather"] 
				alertsHandler(data)
            }
		//}
    }
	catch (Throwable t) {
	log.error t
	return result
	}  

}
/***********************************************************************************************************************
    WEATHER ALERTS
***********************************************************************************************************************/
def mGetWeatherAlerts(){
	def result
    def firstTime = false
    if(state.weatherAlert == null){	
   		result = "You are now subscribed to selected weather alerts for your area"
        firstTime = true
		state.weatherAlert = "There are no weather alerts for your area"
		state.lastAlert = new Date(now()).format("h:mm aa", location.timeZone)
    }
    else result = "There are no weather alerts for your area"
    def data = [:]
    try {
       	//if (getDayOk()==true && getModeOk()==true && getTimeOk()==true && getFrequencyOk()==true || getConditionOk()==true) { -- bobby 4/18/2017
        	def weather = getWeatherFeature("alerts", settings.wZipCode)
        	def type = weather.alerts.type[0]
            def alert = weather.alerts.description[0]
            def expire = weather.alerts.expires[0]
            def typeOk = myWeatherAlert?.find {a -> a == type}
			if(typeOk){
                if(expire != null) expire = expire?.replaceAll(~/ EST /, " ").replaceAll(~/ CST /, " ").replaceAll(~/ MST /, " ").replaceAll(~/ PST /, " ")
                if(alert != null) {
                    result = alert  + " is in effect for your area, that expires at " + expire
                    if(state.weatherAlert == null){
                        state.weatherAlert = result
                        state.lastAlert = new Date(now()).format("h:mm aa", location.timeZone)
                        data = [value: result, name: "weather alert", device:"weather"] 
                        alertsHandler(data)
                        } 
                    else {
                        log.warn "new weather alert = ${alert} , expire = ${expire}"
                        def newAlert = result != state.weatherAlert ? true : false
                        if(newAlert == true){
                            state.weatherAlert = result
                            state.lastAlert = new Date(now()).format("h:mm aa", location.timeZone)
                            data = [value: result, name: "weather alert", device:"weather"] 
                            alertsHandler(data)
                        }
                    }
                }
         	}
			else if (firstTime == true) {
				data = [value: result, name: "weather alert", device:"weather"] 
                alertsHandler(data)
         	}
    	//}
    }
	catch (Throwable t) {
	log.error t
	return result
	}
}
/***********************************************************************************************************************
    HOURLY FORECAST
***********************************************************************************************************************/
def mGetCurrentWeather(){
    def weatherData = [:]
    def data = [:]
   	def result
    try {
		//if (getDayOk()==true && getModeOk()==true && getTimeOk()==true && getFrequencyOk()==true) { 4/18/2017 Bobby
        //hourly updates
            def cWeather = getWeatherFeature("hourly", settings.wZipCode)
            def cWeatherCondition = cWeather.hourly_forecast[0].condition
            def cWeatherPrecipitation = cWeather.hourly_forecast[0].pop + " percent"
            def cWeatherWind = cWeather.hourly_forecast[0].wspd.english + " miles per hour"
            def cWeatherWindC = cWeather.hourly_forecast[0].wspd.metric + " kilometers per hour"
            	if(getMetric() == true) cWeatherWind = cWeatherWindC
            def cWeatherHum = cWeather.hourly_forecast[0].humidity + " percent"
            def cWeatherUpdate = cWeather.hourly_forecast[0].FCTTIME.civil
            //past hour's data
            def pastWeather = state.lastWeather
            //current forecast
				weatherData.wCond = cWeatherCondition
                weatherData.wWind = cWeatherWind
                weatherData.wHum = cWeatherHum
                weatherData.wPrecip = cWeatherPrecipitation
            //last weather update
            def lastUpdated = new Date(now()).format("h:mm aa", location.timeZone)
            if(myWeather) {
                if(pastWeather == null) {
                    state.lastWeather = weatherData
                    state.lastWeatherCheck = lastUpdated
                    result = "hourly weather forcast notification has been activated at " + lastUpdated + " You will now receive hourly weather updates, only if the forecast data changes" 
                    data = [value: result, name: "weather alert", device: "weather"]
                    alertsHandler(data)
                }
                else {
                    def wUpdate = pastWeather.wCond != cWeatherCondition ? "current weather condition" : pastWeather.wWind != cWeatherWind ? "wind intensity" : pastWeather.wHum != cWeatherHum ? "humidity" : pastWeather.wPrecip != cWeatherPrecipitation ? "chance of precipitation" : null
                    def wChange = wUpdate == "current weather condition" ? cWeatherCondition : wUpdate == "wind intensity" ? cWeatherWind  : wUpdate == "humidity" ? cWeatherHum : wUpdate == "chance of precipitation" ? cWeatherPrecipitation : null                    
                    //something has changed
                    if(wUpdate != null){
                        // saving update to state
                        state.lastWeather = weatherData
                        state.lastWeatherCheck = lastUpdated
                        if (myWeather == "Any Weather Updates"){
                        	def condChanged = pastWeather.wCond != cWeatherCondition
                            def windChanged = pastWeather.wWind != cWeatherWind
                            def humChanged = pastWeather.wHum != cWeatherHum
                            def precChanged = pastWeather.wPrecip != cWeatherPrecipitation
							if(condChanged){
                            	result = "The hourly weather forecast has been updated. The weather condition has been changed to "  + cWeatherCondition
                            }
                            if(windChanged){
                            	if(result) {result = result +  " , the wind intensity to "  + cWeatherWind }
                            	else result = "The hourly weather forecast has been updated. The wind intensity has been changed to "  + cWeatherWind
							}
                            if(humChanged){
                            	if(result) {result = result +  " , the humidity to "  + cWeatherHum }
                            	else result = "The hourly weather forecast has been updated. The humidity has been changed to "  + cWeatherHum
							}
                            if(precChanged){
                            	if(result) {result = result + " , the chance of rain to "  + cWeatherPrecipitation }
                            	else result = "The hourly weather forecast has been updated. The chance of rain has been changed to "  + cWeatherPrecipitation
                            }
                            data = [value: result, name: "weather alert", device: "weather"]  
                            alertsHandler(data)
                        }
                        else {
                            if (myWeather == "Weather Condition Changes" && wUpdate ==  "current weather condition"){
                                result = "The " + wUpdate + " has been updated to " + wChange
                                data = [value: result, name: "weather alert", device: "weather"]  
                                alertsHandler(data)
                            }
                            else if (myWeather == "Chance of Precipitation Changes" && wUpdate ==  "chance of precipitation"){
                                result = "The " + wUpdate + " has been updated to " + wChange
                                data = [value: result, name: "weather alert", device: "weather"] 
                                alertsHandler(data)
                            }        
                            else if (myWeather == "Wind Speed Changes" && wUpdate == "wind intensity"){
                                result = "The " + wUpdate + " has been updated to " + wChange
                                data = [value: result, name: "weather alert", device: "weather"] 
                                alertsHandler(data)
                            }         
                            else if (myWeather == "Humidity Changes" && wUpdate == "humidity"){
                                result = "The " + wUpdate + " has been updated to " + wChange
                                data = [value: result, name: "weather alert", device: "weather"] 
                                alertsHandler(data)
                            }
						}
                    }       
                }
            }
    	//}
    }
	catch (Throwable t) {
	log.error t
	return result
	}  
}
/***********************************************************************************************************************
    WEATHER ELEMENTS
***********************************************************************************************************************/
def private mGetWeatherElements(element){
	state.pTryAgain = false
    def result ="Current weather is not available at the moment, please try again later"
   	try {
        //hourly updates
        def cWeather = getWeatherFeature("hourly", settings.wZipCode)
        def cWeatherCondition = cWeather.hourly_forecast[0].condition
        def cWeatherPrecipitation = cWeather.hourly_forecast[0].pop + " percent"
        def cWeatherWind = cWeather.hourly_forecast[0].wspd.english + " miles per hour"
        def cWeatherHum = cWeather.hourly_forecast[0].humidity + " percent"
        def cWeatherUpdate = cWeather.hourly_forecast[0].FCTTIME.civil //forecast last updated time E.G "11:00 AM",
        //current conditions
        def condWeather = getWeatherFeature("conditions", settings.wZipCode)
        def condTodayUV = condWeather.current_observation.UV
  		def currentT = condWeather.current_observation.temp_f
        	int currentNow = currentT
        //forecast
        def forecastT = getWeatherFeature("forecast", settings.wZipCode)
		def fToday = forecastT.forecast.simpleforecast.forecastday[0]
        def high = fToday.high.fahrenheit.toInteger()
       		int highNow = high
        def low = fToday.low.fahrenheit.toInteger()
        	int lowNow = low
        //sunset, sunrise, moon, tide
        def s = getWeatherFeature("astronomy", settings.wZipCode)
		def sunriseHour = s.moon_phase.sunrise.hour
        def sunriseTime = s.moon_phase.sunrise.minute
        def sunrise = sunriseHour + ":" + sunriseTime
            Date date = Date.parse("HH:mm",sunrise)
            def sunriseNow = date.format( "h:mm aa" )
		def sunsetHour = s.moon_phase.sunset.hour
        def sunsetTime = s.moon_phase.sunset.minute
        def sunset = sunsetHour + ":" + sunsetTime
            date = Date.parse("HH:mm", sunset)
            def sunsetNow = date.format( "h:mm aa" ) 
            if(getMetric() == true){
                def cWeatherWindC = cWeather.hourly_forecast[0].wspd.metric + " kilometers per hour"
                    cWeatherWind = cWeatherWindC
                def currentTc = condWeather.current_observation.temp_c
                    currentNow = currentTc
                def highC = fToday.high.celsius
                    highNow = currentTc            
                def lowC = fToday.low.celsius
                    lowNow = currentTc            
            }               
        if(debug) log.debug "cWeatherUpdate = ${cWeatherUpdate}, cWeatherCondition = ${cWeatherCondition}, " +
        					"cWeatherPrecipitation = ${cWeatherPrecipitation}, cWeatherWind = ${cWeatherWind},  cWeatherHum = ${cWeatherHum}, cWeatherHum = ${condTodayUV}  "    

        	if		(element == "precip" ) {result = cWeatherPrecipitation}
        	else if	(element == "wind") {result = cWeatherWind}
        	else if	(element == "uv") {result = condTodayUV}
			else if	(element == "hum") {result = cWeatherHum}        
			else if	(element == "cond") {result = cWeatherCondition}        
			else if	(element == "current") {result = currentNow} 
			else if	(element == "rise") {result = sunriseNow } 
			else if	(element == "set") {result = sunsetNow}  
 			else if	(element == "high") {result = highNow } 
			else if	(element == "low") {result = lowNow}             
    
    		return result
	
    }catch (Throwable t) {
		log.error t
        state.pTryAgain = true
        return result
	} 
}
/***********************************************************************************************************************
    WEATHER TEMPS
***********************************************************************************************************************/
def private mGetWeatherVar(var){
	state.pTryAgain = false
    def result
	try {
		def weather = getWeatherFeature("forecast", settings.wZipCode)
        def sTodayWeather = weather.forecast.simpleforecast.forecastday[0]
        if(var =="high") result = sTodayWeather.high.fahrenheit//.toInteger()
        if(var == "low") result = sTodayWeather.low.fahrenheit//.toInteger()
        if(var =="today") result = 	weather.forecast.txt_forecast.forecastday[0].fcttext 
        if(var =="tonight") result = weather.forecast.txt_forecast.forecastday[1].fcttext 
		if(var =="tomorrow") result = weather.forecast.txt_forecast.forecastday[2].fcttext     

	if(getMetric() == true){
		if(var =="high") result = weather.forecast.simpleforecast.forecastday[0].high.celsius//.toInteger()
        if(var == "low") result = weather.forecast.simpleforecast.forecastday[0].low.celsius//.toInteger()
        if(var =="today") result = 	weather.forecast.txt_forecast.forecastday[0].fcttext_metric 
        if(var =="tonight") result = weather.forecast.txt_forecast.forecastday[1].fcttext_metric 
		if(var =="tomorrow") result = weather.forecast.txt_forecast.forecastday[2].fcttext_metric              
    	result = result?.toString()
        result = result?.replaceAll(/([0-9]+)C/,'$1 degrees')
    }
    	result = result?.toString()
        result = result?.replaceAll(/([0-9]+)F/,'$1 degrees').replaceAll(~/mph/, " miles per hour")
                // clean up wind direction (South)
            result = result?.replaceAll(~/ SSW /, " South-southwest ").replaceAll(~/ SSE /, " South-southeast ").replaceAll(~/ SE /, " Southeast ").replaceAll(~/ SW /, " Southwest ")
            // clean up wind direction (North)
            result = result?.replaceAll(~/ NNW /, " North-northwest ").replaceAll(~/ NNE /, " North-northeast ").replaceAll(~/ NE /, " Northeast ").replaceAll(~/ NW /, " Northwest ")
            // clean up wind direction (West)
            result = result?.replaceAll(~/ WNW /, " West-northwest ").replaceAll(~/ WSW /, " West-southwest ")
            // clean up wind direction (East)
            result = result?.replaceAll(~/ ENE /, " East-northeast ").replaceAll(~/ ESE /, " East-southeast ")
    
    return result
	
    }catch (Throwable t) {
        log.error t
        state.pTryAgain = true
        return result
    }
} 
/***********************************************************************************************************************
    CRON HANDLER
***********************************************************************************************************************/
def cronHandler(var) {
	def result
		if(var == "Minutes") {
        //	0 0/3 * 1/1 * ? *
        	if(xMinutes) { result = "0 0/${xMinutes} * 1/1 * ? *"
			    schedule(result, "scheduledTimeHandler")
           	}
            else log.error " unable to schedule your reminder due to missing required variables"
        }
		if(var == "Hourly") {
        //	0 0 0/6 1/1 * ? *
			if(xHours) { 
            	result = "0 0 0/${xHours} 1/1 * ? *"
            	schedule(result, "scheduledTimeHandler")
            }
            else log.error " unable to schedule your reminder due to missing required variables"
		}
		if(var == "Daily") {
        // 0 0 1 1/7 * ? *
            def hrmn = hhmm(xDaysStarting, "HH:mm")
            def hr = hrmn[0..1] 
            def mn = hrmn[3..4]
        	if(xDays && xDaysStarting) {
            	result = "0 $mn $hr 1/${xDays} * ? *"
                schedule(result, "scheduledTimeHandler")
            }
            else if(xDaysWeekDay && xDaysStarting) {
            	result = "0 $mn $hr 1/${xDays} * MON-FRI *"
                schedule(result, "scheduledTimeHandler")
			}
            else log.error " unable to schedule your reminder due to missing required variables"
            }
        if(var == "Weekly") {
        // 	0 0 2 ? * TUE,SUN *
        	def hrmn = hhmm(xWeeksStarting, "HH:mm")
            def hr = hrmn[0..1]
            def mn = hrmn[3..4]
            def weekDaysList = [] 
            	xWeeks?.each {weekDaysList << it }
            def weekDays = weekDaysList.join(",")
            if(xWeeks && xWeeksStarting) { result = "0 $mn $hr ? * ${weekDays} *" }
            else log.error " unable to schedule your reminder due to missing required variables"
		    schedule(result, "scheduledTimeHandler")
            }
		if(var == "Monthly") { 
        // 0 30 5 6 1/2 ? *
        	def hrmn = hhmm(xMonthsStarting, "HH:mm")
            def hr = hrmn[0..1]
            def mn = hrmn[3..4]
        	if(xMonths && xMonthsDay) { result = "0 $mn $hr ${xMonthsDay} 1/${xMonths} ? *"}
            else log.error "unable to schedule your reminder due to missing required variables"
		    schedule(result, "scheduledTimeHandler")
            }
		if(var == "Yearly") {
        //0 0 4 1 4 ? *
        	def hrmn = hhmm(xYearsStarting, "HH:mm")
            def hr = hrmn[0..1]
            def mn = hrmn[3..4]           
        	if(xYears) {result = "0 $mn $hr ${xYearsDay} ${xYears} ? *"}
            else log.error "unable to schedule your reminder due to missing required variables"
		    schedule(result, "scheduledTimeHandler")
            }
    log.info "scheduled $var recurring event" //time period with expression: $result"
//    schedule(result, "scheduledTimeHandler")
}
/***********************************************************************************************************************
    SUN STATE HANDLER
***********************************************************************************************************************/
def sunsetTimeHandler(evt) {
	def sunsetString = (String) evt.value
    def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
	def sunsetTime = s.sunset.time // Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", sunsetString)
    if(offset) {
    	def offsetSunset = new Date(sunsetTime - (-offset * 60 * 1000))
		log.debug "Scheduling for: $offsetSunset (sunset is $sunsetTime)"
		runOnce(offsetSunset, "scheduledTimeHandler")
    }
    else scheduledTimeHandler("sunset")
}
def sunriseTimeHandler(evt) {
    def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
	def sunriseTime = s.sunrise.time
    if(offset) {
    	def offsetSunrise = new Date(sunriseTime -(-offset * 60 * 1000))
		log.debug "Scheduling for: $offsetSunrise (sunrise is $sunriseTime)"
		runOnce(offsetSunrise, "scheduledTimeHandler")
	}
    else  scheduledTimeHandler("sunrise")
}

def scheduledTimeHandler(state) {
	def data = [:]
		//if (getDayOk()==true && getModeOk()==true && getTimeOk()==true && getFrequencyOk()==true) {	-- Bobby 4/18/2017
			data = [value: state, name:"sunstate", device:"schedule"] 
            alertsHandler(data)
    	//}
}
/***********************************************************************************************************************
    RESTRICTIONS HANDLER
***********************************************************************************************************************/
def getConditionOk() {
    def result = true
    def devList = []
	if (state.occurrences >= howManyTimes) {
		unschedule("retriggerHandler")
        state.message = null
        state.occurrences = 0
        log.warn "canceling reminders"
    }
	if(rSwitchS || rMotionS || rContactS || rPresenceS){
    result = false
        if (rSwitch) {
            rSwitch.each { deviceName ->
                if (deviceName.latestValue("switch") == "${rSwitchS}") {
                    String device  = (String) deviceName
                    devList += device
                }
            }
            log.warn "rSwitch list is ${devList?.size()} for state $rSwitchS"
            if (devList?.size() > 1) result = true
        }   
        if (rMotion){
            rMotion.each { deviceName ->
                if (deviceName.currentValue("motion")=="${rMotionS}") {
                            String device  = (String) deviceName
                            devList += device
                }
            }
            log.warn "rMotion list is ${devList} for state $rMotionS"
            if (devList?.size() > 0) result = true
        }	        
        if (rContact){
            rContact.each { deviceName ->
                if (deviceName.currentValue("contact")=="${rContactS}") {
                            String device  = (String) deviceName
                            devList += device
                }
            }
            log.warn "rContact list is ${devList} for state $rContactS"
            if (devList?.size() > 0) result = true
        }	        
        if (rPresence){                    
            rPresence.each { deviceName ->
                if (deviceName.currentValue("presence")=="${rPresenceS}") {
                    String device  = (String) deviceName
                    devList += device
                }
            }
            log.warn "rPresence list is ${devList} for state $rPresenceS"
            if (devList?.size() > 0) result = true
        }
    }
	log.debug "getConditionOk = $result"
    result
}
def getFrequencyOk() {
    def lastPlayed = state.lastPlayed
    def result = false
	if (onceDailyOk(lastPlayed)) {
			if (everyXmin) {
				if (state.lastPlayed == null) {
                	result = true 
                }
                else {
                	if (now() - state.lastPlayed >= everyXmin * 60000) {
						result = true
					}
					else {
						log.debug "Not taking action because $everyXmin minutes have not passed since last notification"
					}
				}
            }
			else {
				result = true
			}
	}
	else {
		log.debug "Not taking action because the notification was already played once today"
	}
	log.debug "frequencyOk = $result"
	result
}
private onceDailyOk(Long lastPlayed) {
	def result = true
	if (onceDaily) {
    	def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
        def lastTime = new Date(lastPlayed).format("EEEE, MMMM d, yyyy", location.timeZone) 
		result = lastPlayed ? today != lastTime : true
		log.trace "oncePerDayOk = $result"
	}
	result
}
private getMetric(){
   	def result = location.temperatureScale == "C"
    log.debug "getMetric = $result"
    result
}
private getModeOk() {
    def result = !modes || modes?.contains(location.mode)
	log.debug "modeOk = $result"
    result
} 
private getDayOk() {
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
	log.debug "daysOk = $result"
	result
}
private getTimeOk() {
	def result = true
	if ((starting && ending) ||
	(starting && endingX in ["Sunrise", "Sunset"]) ||
	(startingX in ["Sunrise", "Sunset"] && ending) ||
	(startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if(startingX == "Sunrise") start = s.sunrise.time
		else if(startingX == "Sunset") start = s.sunset.time
		else if(starting) start = timeToday(starting,location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	if (parent.debug) log.trace "getTimeOk = $result."
    }
    log.debug "timeOk = $result"
    return result
}
private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}
private offset(value) {
	log.warn "offset is $offset"
    def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}
private timeIntervalLabel() {
	def result = ""
	if      (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")
}
/***********************************************************************************************************************
    SMS HANDLER
***********************************************************************************************************************/
private void sendtxt(message) {
	def stamp = state.lastTime = new Date(now()).format("h:mm aa", location.timeZone)
    if (parent.debug) log.debug "Request to send sms received with message: '${message}'"
    if (sendContactText) { 
        sendNotificationToContacts(message, recipients)
            if (parent.debug) log.debug "Sending sms to selected reipients"
    } 
    else {
    	if (push) {
        	message = timeStamp==true ? message + " at " + stamp : message
    		sendPush message
            	if (parent.debug) log.debug "Sending push message to selected reipients"
        }
    } 
    if (notify) {
        sendNotificationEvent(message)
             	if (parent.debug) log.debug "Sending notification to mobile app"
    }
    if (sms) {
        sendText(sms, message)
	}
}
private void sendText(number, message) {
    if (sms) {
        def phones = sms.split("\\,")
        for (phone in phones) {
            sendSms(phone, message)
            if (parent.debug) log.debug "Sending sms to selected phones"
        }
    }
}
def runProfileActions() {
              	def String pintentName = (String) null
                def String pContCmdsR = (String) null
                def String ptts = (String) null
        		def pContCmds = false
        		def pTryAgain = false
        		def dataSet = [:]
           		parent.childApps.each {child ->
                        def ch = child.label
                		if (ch == settings.myProfile) { 
                    		if (debug) log.debug "Matched the profile"
                            pintentName = child.label
                    		ptts = "Running Profile actions from the Notification Add-on"
                            dataSet = [ptts:ptts, pintentName:pintentName]
                            child.profileEvaluate(dataSet)
						}
            	}
                
}
/***********************************************************************************************************************
    CUSTOM SOUNDS HANDLER
***********************************************************************************************************************/
private loadSound() {
	switch (actionType) {
		case "Bell 1":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3", duration: "10"]
			break;
		case "Bell 2":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell2.mp3", duration: "10"]
			break;
		case "Dogs Barking":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/dogs.mp3", duration: "10"]
			break;
		case "Fire Alarm":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/alarm.mp3", duration: "17"]
			break;
		case "The mail has arrived":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/the+mail+has+arrived.mp3", duration: "1"]
			break;
		case "A door opened":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/a+door+opened.mp3", duration: "1"]
			break;
		case "There is motion":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/there+is+motion.mp3", duration: "1"]
			break;
		case "Smartthings detected a flood":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+a+flood.mp3", duration: "2"]
			break;
		case "Smartthings detected smoke":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+smoke.mp3", duration: "1"]
			break;
		case "Someone is arriving":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/someone+is+arriving.mp3", duration: "1"]
			break;
		case "Piano":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/piano2.mp3", duration: "10"]
			break;
		case "Lightsaber":
			state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/lightsaber.mp3", duration: "10"]
			break;
		default:
			state?.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3", duration: "10"]
			break;
	}
}
/************************************************************************************************************
   PROFILES 
************************************************************************************************************/       
def getProfileList(){        
		return parent.getChildApps()*.label.sort()
}
/************************************************************************************************************
   Page status and descriptions 
************************************************************************************************************/       
def pSendSettings() {def result = ""
    if (sendContactText || sendText || push) {
    	result = "complete"}
   		result}
def pSendComplete() {def text = "Tap here to configure settings" 
    if (sendContactText || sendText || push) {
    	text = "Configured"}
    	else text = "Tap to Configure"
		text}
def triggersSettings() {def result = ""
    if (myWeatherTriggers || myWeather || myTemperature || myCO2 || myCO ||  myAcceleration || myHumidity || mySound || myWeatherAlert || myWater || mySmoke || myPresence || myMotion || myContact || mySwitch || myPower || myLocks || myTstat || myMode || myRoutine || frequency) {
    	result = "complete"}
   		result}
def triggersComplete() {def text = "Tap here to configure settings" 
    if (triggersSettings() == "complete") {
        text = "Configured"
    }
    else text = "Tap to Configure"
		text} 

def pRestSettings() {def result = ""
    if (modes || days ||pTimeSettings() || onceDaily || everyXmin || rSwitch || rContact || rMotion || rPresence) {
    	result = "complete"}
   		result}
def pRestComplete() {def text = "Tap here to configure settings" 
    if (pRestSettings() == "complete" ) {
    	text = "Configured"}
    	else text = "Tap to Configure"
		text}     
def pTimeSettings() {def result = ""
    if (startingX || endingX) {
    	result = "complete"}
   		result}
def pTimeComplete() {def text = "Tap here to configure settings" 
    if (startingX || endingX) {
    	text = "Configured"}
    	else text = "Tap to Configure"
		text}