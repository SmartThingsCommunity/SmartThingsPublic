/**
 *  Ask Alexa - Macro
 *
 *  Version 2.0.2a - 6/4/16 Copyright © 2016 Michael Struck
 *  
 *  Version 1.0.0 - Initial release
 *  Version 1.0.1 - Added motion sensor reports; added events report to various sensors
 *  Version 1.0.2c - Added weather reports which include forecast, sunrise and sunset
 *  Version 2.0.0a - Modified child app to make it a 'macro' application. Still does voice reports, includes bug fixes as well.
 *  Version 2.0.1a - Fixed an issue with dimmer voice reporting, added averages for report parameters, added thermostat device groups and Nest support, various other syntax fixes.
 *  Versopm 2.0.2a - Added speakers to the list of voice reports. Minor bug fixes. Added multiple weather reports to voice reports.
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
    name: "Ask Alexa - Macro",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Provide interfacing to control and report on SmartThings devices with the Amazon Echo ('Alexa').",
    category: "Convenience",
    parent: "MichaelStruck:Ask Alexa",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-macro.src/AskAlexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-macro.src/AskAlexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-macro.src/AskAlexa@2x.png"
    )
preferences {
    page name:"mainSelection"
    	page name:"pageGroup"
        page name:"pageGroupM"
    	page name:"pageControl"
    		page name:"pageSTDevices"
            page name:"pageHTTP"
    	page name:"pageVoice"
    		page name:"pageTempReport"
    		page name:"pageHomeReport"
    		page name:"pageOtherReport"
    		page name:"pageBatteryReport"
    		page name:"pageDoorReport"
    		page name:"pageSwitchReport"
    		page name:"pageWeatherReport"
            page name:"pageSpeakerReport"
}
def mainSelection(){
    dynamicPage(name: "mainSelection", title: "Voice Macro Settings", install: true, uninstall: true) {
    	section {
			if (parent.versionInt() < 111) paragraph "You are using a version of the parent app that is older than the recommended version. Please upgrade "+
					"to the latest version to ensure you have the latest features and bug fixes.", 
                    image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/warning.png"
            label title:"Voice Macro Name", required: true, image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speak.png"
            input "macroType", "enum", title: "Macro Type...", options: [["Control":"Control (Run/Execute)"],["Group":"Device Group (On/Off/Toggle, Lock/Unlock, etc.)"],["GroupM":"Macro Group (Run/Execute)"],["Voice":"Voice Reporting (Run/Execute) "]], required: false, multiple: false, submitOnChange:true
			def fullMacroName=[GroupM: "Macro Group", Control:"Control", Group:"Device Group", Voice:"Voice Reporting"][macroType] ?: macroType
            if (macroType) href "page${macroType}", title: "${fullMacroName} Settings", description: macroDesc(), state: greyOutMacro()
        }
        if (macroType && macroType !="GroupM" && macroType !="Group"){
            section("Restrictions") {            
                    input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false,
                        image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/calendar.png"
                    href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: greyOutState(timeStart, timeEnd,"","",""),
                        image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/clock.png"
                    input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png"
            }
        }
        section("Tap below to remove this macro"){}
    }
}
page(name: "timeIntervalInput", title: "Only during a certain time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
	}
}
//Device Macro
def pageGroup() {
	dynamicPage(name: "pageGroup", install: false, uninstall: false) {
		section { paragraph "Device Group Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/folder.png" }
        section (" ") {
            input "groupType", "enum", title: "Group Type...", options: [["colorControl": "Colored Light (On/Off/Toggle/Level/Color)"],["switchLevel":"Dimmer (On/Off/Toggle/Level)"],["doorControl": "Door (Open/Close)"],["lock":"Lock (Lock/Unlock)"],["switch":"Switch (On/Off/Toggle)"],["thermostat":"Thermostat (Mode/Off/Setpoint)"]], required: false, multiple: false, submitOnChange:true
    		if (groupType) input "groupDevice${groupType}", "capability.${groupType}", title: "Choose devices...", required: false, multiple: true
        }
        if (groupType == "thermostat"){
        	section ("Thermostat Group Options"){
                if (!tstatDefaultCool) input "tstatDefaultHeat", "bool", title: "Set Heating Setpoint By Default", defaultValue:false, submitOnChange:true
            	if (!tstatDefaultHeat) input "tstatDefaultCool", "bool", title: "Set Cooling Setpoint By Default", defaultValue:false, submitOnChange:true
            }
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgement Message", description: "Enter a short statement to play after macro runs", required: false 
             input "noAck", "bool", title: "No Acknowledgement Message", defaultValue: false, submitOnChange: true
        }
	}
}
//Group Macro
def pageGroupM() {
	dynamicPage(name: "pageGroupM", install: false, uninstall: false) {
		section { paragraph "Macro Group Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/macrofolder.png" }
        section (" ") { input "groupMacros", "enum", title: "Macros To Run (Control/Voice Reports)...", options: parent.getMacroList(app.label), required: false, multiple: true }
        section("Custom acknowledgment"){ 
            if (!noAck) input "voicePost", "text", title: "Acknowledgement Message", description: "Enter a short statement to play after macro runs", required: false
            input "noAck", "bool", title: "No Acknowledgement Message", defaultValue: false, submitOnChange: true
		}
        section("Please note"){
        	paragraph "Any acknowledgement message, or activating the 'no acknowledgement message' above will disable any output from the macros, " +
            	"including voice reports."
        }
	}
}
//Control Macro
def pageControl() {
	dynamicPage(name: "pageControl", install: false, uninstall: false) {
        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases) phrases.sort()	
        section { paragraph "Control Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/control.png" }
        section ("When voice macro is activated...") {
            if (phrases) input "phrase", "enum", title: "Perform This Routine...", options: phrases, required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/routine.png" 
            input "setMode", "mode", title: "Set Mode To...", required: false, 
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png"  
            input "SHM", "enum",title: "Set Smart Home Monitor To...", options: ["away":"Arm (Away)", "stay":"Arm (Stay)", "off":"Disarm"], required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/SHM.png"
            href "pageSTDevices", title: "Control These SmartThings Devices...", description: getDeviceDesc(), state: deviceGreyOut(),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/smartthings.png"
            href "pageHTTP", title: "Run This HTTP Request...", description: getHTTPDesc(), state: greyOutStateHTTP(),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/network.png"
            input "cDelay", "number", title: "Default Delay (Minutes) To Activate", defaultValue: 0, required: false,
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/stopwatch.png"
            input ("contacts", "contact", title: "Send Notifications To...", required: false, 
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/sms.png") {
                input "smsNum", "phone", title: "Send SMS Message To (Phone Number)...", required: false,
                	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/sms.png"
                input "pushMsg", "bool", title: "Send Push Message", defaultValue: false
            }
            input "smsMsg", "text", title: "Send This Message...", required: false
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgement Message", description: "Enter a short statement to play after macro runs", required: false
             input "noAck", "bool", title: "No Acknowledgement Message", defaultValue: false, submitOnChange: true
        }
	}
}
def pageSTDevices(){
	dynamicPage (name: "pageSTDevices", install: false, uninstall: false) {
        section { paragraph "SmartThings Device Control", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/smartthings.png"}
        section ("Switches"){
            input "switches", "capability.switch", title: "Control These Switches...", multiple: true, required: false, submitOnChange:true
            if (switches) input "switchesCMD", "enum", title: "Command To Send To Switches", options:["on":"Turn on","off":"Turn off", "toggle":"Toggle the switches' on/off state"], multiple: false, required: false
        }
        section ("Dimmers"){
            input "dimmers", "capability.switchLevel", title: "Control These Dimmers...", multiple: true, required: false , submitOnChange:true
            if (dimmers) input "dimmersCMD", "enum", title: "Command To Send To Dimmers", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the dimmers' on/off state"], multiple: false, required: false, submitOnChange:true
            if (dimmersCMD == "set" && dimmers) input "dimmersLVL", "number", title: "Dimmers Level", description: "Set dimmer level", required: false, defaultValue: 0
        }
        section ("Colored Lights"){
            input "cLights", "capability.colorControl", title: "Control These Colored Lights...", multiple: true, required: false, submitOnChange:true
            if (cLights) input "cLightsCMD", "enum", title: "Command To Send To Colored Lights", options:["on":"Turn on","off":"Turn off","set":"Set color and level", "toggle":"Toggle the lights' on/off state"], multiple: false, required: false, submitOnChange:true
            if (cLightsCMD == "set" && cLights){
                input "cLightsCLR", "enum", title: "Choose A Color...", required: false, multiple:false, options: parent.fillColorSettings().name, submitOnChange:true
                if (cLightsCLR == "Custom-User Defined"){
                    input "hueUserDefined", "number", title: "Colored Lights Hue", description: "Set colored light hue (0 to 100)", required: false, defaultValue: 0
                    input "satUserDefined", "number", title: "Colored Lights Saturation", description: "Set colored lights saturation (0 to 100)", required: false, defaultValue: 0
                }
                input "cLightsLVL", "number", title: "Colored Light Level", description: "Set colored lights level", required: false, defaultValue: 0
            }
        }
        section ("Thermostats"){
            input "tstats", "capability.thermostat", title: "Control These Thermostats...", multiple: true, required: false, submitOnChange:true
            if (tstats) {
            	def tstatOptions=["heat":"Set heating temperature","cool":"Set cooling temperature"]
                if (parent.isNest()) tstatOptions += ["away":"Nest 'Away' Presence","home":"Nest 'Home' Presence"]
                input "tstatsCMD", "enum", title: "Command To Send To Thermostats", options :tstatOptions , multiple: false, required: false, submitOnChange:true
            }
            if (tstatsCMD =="heat" || tstatsCMD =="cool") input "tstatLVL", "number", title: "Temperature Level", description: "Set temperature level", required: false
        }
        section ("Locks"){
            input "locks","capability.lock", title: "Control These Locks...", multiple: true, required: false, submitOnChange:true
            if (locks) input "locksCMD", "enum", title: "Command To Send To Locks", options:["lock":"Lock","unlock":"Unlock"], multiple: false, required: false
        }
        section("Garage Doors"){
            input "garages","capability.garageDoorControl", title: "Control These Garage Doors...", multiple: true, required: false, submitOnChange:true
            if (garages) input "garagesCMD", "enum", title: "Command To Send To Garage Doors", options:["open":"Open","close":"Close"], multiple: false, required: false
        }
    }
}
def pageHTTP (){
    dynamicPage(name: "pageHTTP", install: false, uninstall: false){
        section { paragraph "HTTP Request", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/network.png" }
        section(" "){
            input "extInt", "enum", title: "Choose HTTP Command Type", options:[0:"External REST",1:"Internal (IP, port, command)"], required: false, submitOnChange:true
            if (extInt == "0") input "http", "text", title:"HTTP Address...", required: false
            else if (extInt == "1"){
                input "ip", "text", title: "Internal IP Address", description: "IPv4 address xx.xx.xx.xx format", required: false
                input "port", "number", title: "Internal Port", description: "Enter a port number 0 to 65536", required: false 
                input "command", "text", title: "Command", description: "Enter REST commands", required: false 
            }
        }
    }
}
// Show Voice page
def pageVoice() {
	dynamicPage(name: "pageVoice", install: false, uninstall: false) {
        section { paragraph "Voice Reporting Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/voice.png" }
        section (" ") {
            input "voicePre", "text", title: "Pre Message Before Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false
            href "pageSwitchReport", title: "Switch/Dimmer Report", description: reportDesc(voiceSwitch, voiceDimmer, "", "", ""), state: greyOutState(voiceSwitch, voiceDimmer, "", "", ""),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/power.png"
            href "pageDoorReport", title: "Door/Window/Lock Report", description: reportDesc(voiceDoorSensors, voiceDoorControls, voiceDoorLocks, "", ""), state: greyOutState(voiceDoorSensors, voiceDoorControls, voiceDoorLocks, "", ""),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/lock.png"
            href "pageTempReport", title: "Temperature/Humidity/Thermostat Report", description: reportDesc(voiceTemperature, voiceTempSettings, voiceTempVar, voiceHumidVar, voiceHumidity), state: greyOutState(voiceTemperature, voiceTempSettings, voiceTempVar, voiceHumidVar, voiceHumidity),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/temp.png"
            href "pageSpeakerReport", title: "Speaker Report", description: reportDesc(voiceSpeaker, "","","",""), state: greyOutState(voiceSpeaker, "","","",""),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speaker.png"
			href "pageWeatherReport", title: "Weather Report", description: weatherDesc(), state: greyOutWeather(),
            	image : "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/weather.png"
            href "pageOtherReport", title: "Other Sensors Report", description: reportDesc(voiceWater, voiceMotion, voicePresence, "", ""), state: greyOutState(voiceWater, voiceMotion, voicePresence, "", ""),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/sensor.png"
            href "pageHomeReport", title: "Mode and Smart Home Monitor Report", description: reportDescMSHM(), state: greyOutState(voiceMode, voiceSHM, "", "", ""),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png"
            href "pageBatteryReport",title: "Battery Report", description: reportDesc(voiceBattery, "", "", "", ""), state: greyOutState(voiceBattery, "", "", "", ""),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/battery.png"
            input "voicePost", "text", title: "Post Message After Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false
        }
        if (parent.getAdvEnabled()){
        	section("Advanced"){
            	input "voiceRepFilter", "text", title: "Filter Report Output", description: "Delimit items with comma (ex. xxxxx,yyyyy,zzzzz)", required: false
			}
    	}
    }
}
def pageSwitchReport(){
    dynamicPage(name: "pageSwitchReport", install: false, uninstall: false){
        section { paragraph "Switch/Dimmer Report", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/power.png" }
        section("Switch Report") {
            input "voiceSwitch", "capability.switch", title: "Switches To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceSwitch) input "voiceOnSwitchOnly", "bool", title: "Report Only Switches That Are On", defaultValue: false
            if (voiceSwitch)input "voiceOnSwitchEvt", "bool",title: "Report The Time Of The Last On Event", defaultValue: false 
        }
        section("Dimmer Report") {
            input "voiceDimmer", "capability.switchLevel", title: "Dimmers To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceDimmer) input "voiceOnDimmerOnly", "bool", title: "Report Only Dimmers That Are On", defaultValue: false
            if (voiceDimmer)input "voiceOnDimmerEvt", "bool",title: "Report The Time Of The Last On Event", defaultValue: false
        }
    }
}
def pageDoorReport(){
    dynamicPage(name: "pageDoorReport", install: false, uninstall: false){
        section { paragraph "Doors/Windows/Locks", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/lock.png" }
        section(" ") {
            input "voiceDoorSensors", "capability.contactSensor", title: "Doors/Windows Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            input "voiceDoorControls", "capability.doorControl", title: "Door Controls To Report Their Status...", multiple: true, required: false, submitOnChange: true
            input "voiceDoorLocks", "capability.lock", title: "Locks To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceDoorSensors || voiceDoorControls || voiceDoorLocks)input "voiceDoorAll", "bool", title: "Report Door/Window Summary Even When All Are Closed And Locked", defaultValue: false
            if (voiceDoorSensors || voiceDoorControls)input "voiceDoorEvt", "bool",title: "Report The Time Of The Last Door/Window Opening", defaultValue: false
            if (voiceDoorLocks)input "voiceLockEvt", "bool",title: "Report The Time Of The Last Lock Unlocking", defaultValue: false
        }
    }
}
def pageOtherReport(){
    dynamicPage(name: "pageOtherReport", install: false, uninstall: false){
        section { paragraph "Other Sensors Report", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/sensor.png" }
        section ("Water Report") {
            input "voiceWater", "capability.waterSensor", title: "Water Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceWater) input "voiceWetOnly", "bool", title: "Report Only Water Sensors That Are 'Wet'", defaultValue: false 
        }
        section("Presence Report") {
            input "voicePresence", "capability.presenceSensor", title: "Presence Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true 
            if (voicePresence)input "voicePresentOnly", "bool", title: "Report Only Sensors That Are 'Not Present'", defaultValue: false
            if (voicePresence)input "voicePresentEvt", "bool",title: "Report The Time Of The Last Arrival", defaultValue: false 
            if (voicePresence)input "voiceGoneEvt", "bool",title: "Report The Time Of The Last Departure", defaultValue: false 
        }
        section ("Motion Sensors"){
            input "voiceMotion", "capability.motionSensor", title: "Motion Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceMotion) input "voiceMotionOnly", "bool",title: "Report Only Sensors That Read 'Active'", defaultValue: false
            if (voiceMotion) input "voiceMotionEvt", "bool",title: "Report The Time Of The Last Movement", defaultValue: false 
        }
    }
}
def pageTempReport(){
    dynamicPage(name: "pageTempReport", install: false, uninstall: false){
        section { paragraph "Temperature/Humidity/Thermostat Report", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/temp.png" }
        section ("Temperature Reporting"){
            input "voiceTemperature", "capability.temperatureMeasurement", title: "Devices To Report Temperatures...",multiple: true, required: false, submitOnChange: true
			if (voiceTemperature && voiceTemperature.size() > 1) input "voiceTempAvg", "bool", title: "Report Average Of Temperature Readings", defaultValue: false	
        }
        section ("Humidity Reporting"){	
            input "voiceHumidity", "capability.relativeHumidityMeasurement", title: "Devices To Report Humidity...",multiple: true, required: false, submitOnChange: true
        	if (voiceHumidity && voiceHumidity.size() > 1) input "voiceHumidAvg", "bool", title: "Report Average Of Humidity Readings", defaultValue: false
        }
        section ("Thermostat Setpoint Reporting") {
            input "voiceTempSettings", "capability.thermostat", title: "Thermostats To Report Their Setpoints...",multiple: true, required: false, submitOnChange:true
            if (voiceTempSettings) {
            	input "voiceTempSettingsType", "enum", title: "Which Setpoint To Report", defaultValue: "heatingSetpoint", 
                	options: ["heatingSetpoint": "Heating Setpoint","coolingSetpoint":"Cooling Setpoint","thermostatSetpoint":"Single Setpoint (Not compatible with all thermostats)"]
            	input "voiceTempSettingSummary", "bool", title: "Report Only Thermostats Not At Desired Setpoint", defaultValue: false, submitOnChange:true
            }
            if (voiceTempSettingSummary && voiceTempSettings) input "voiceTempTarget", "number", title: "Thermostat Setpoint Target", required: false, defaultValue: 50
        }
    }
}
def pageSpeakerReport(){
    dynamicPage(name: "pageSpeakerReport", install: false, uninstall: false){
        section { paragraph "Speaker Report", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speaker.png" }
        section(" "){
            input "voiceSpeaker", "capability.musicPlayer", title: "Speakers To Report Status...", description: "Tap to choose devices", multiple: true, required: false, submitOnChange:true
        	if (voiceSpeaker) input "voiceSpeakerOn", "bool", title: "Report Only Speakers That Are Playing", defaultValue:false
        }
        section("Please Note"){
        	paragraph "There may be up to 5 a minute delay before SmartThings refreshes the current speaker status. This may cause the report to produce " +
            	"inaccurate results."
        }
    }
}
def pageBatteryReport(){
    dynamicPage(name: "pageBatteryReport", install: false, uninstall: false){
        section { paragraph "Battery Report", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/battery.png" }
        section(" "){
            input "voiceBattery", "capability.battery", title: "Devices With Batteries To Monitor...", description: "Tap to choose devices", multiple: true, required: false, submitOnChange:true
            if (voiceBattery) input "batteryThreshold", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: [5:"<5%",10:"<10%",20:"<20%",30:"<30%",40:"<40%",50:"<50%",60:"<60%",70:"<70%",80:"<80%",90:"<90%",101:"Always play battery level"]  
        }
    }
}
def pageHomeReport(){
    dynamicPage(name: "pageHomeReport", install: false, uninstall: false){
        section { paragraph "Mode And Security Report", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png" }
        section(" ") {
            input "voiceMode", "bool", title: "Report SmartThings Mode Status", defaultValue: false
            input "voiceSHM", "bool", title: "Report Smart Home Monitor Status", defaultValue: false
        }
    }
}
def pageWeatherReport(){
	dynamicPage(name: "pageWeatherReport", install: false, uninstall: false) {
    	section { paragraph "Weather Report", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/weather.png" }
        section("Weather Forecasts") {
        	input "voiceWeatherToday", "bool", title: "Speak Today's Weather Forecast", defaultValue: false
            input "voiceWeatherTonight", "bool", title: "Speak Tonight's Weather Forecast", defaultValue: false
            input "voiceWeatherTomorrow", "bool", title: "Speak Tomorrow's Weather Forecast", defaultValue: false
        }
        section ("Sunrise/Sunset"){    
            input "voiceSunrise", "bool", title: "Speak Today's Sunrise", defaultValue: false
    		input "voiceSunset", "bool", title: "Speak Today's Sunset", defaultValue: false	
        }
        section ("Location") {
        	input "zipCode", "text", title: "Zip Code", required: false
            paragraph "Please Note:\nYour SmartThings location is currently set to: ${location.zipCode}. If you leave "+
            	"the area above blank the report will use your SmartThings location. Enter a zip code above if you "+
                "want to report on a different location."
		}
    }
}
def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}
def updated() {
    log.debug "Updated with settings: ${settings}"
    initialize()
}
def initialize() {
    unschedule()
    state.scheduled=false
}
//Group Handler-----------------------------------------------------------
def groupResults(num, op, colorData, param){   
    def result= "", noun="", valueWord, proNoun=""
    proNoun = settings."groupDevice${groupType}".size()==1 ? "its" : "their"
    if (groupType=="colorControl" || groupType=="switchLevel"){
    	num = num < 0 ? 0 : num >99 ? 100 : num
    	valueWord = num ==100 ? "${proNoun} maximum brightness" :  op =="low" || op=="medium" || op=="high" ? "${op}, or a value of ${num}%" : "${num}%"
    }
    if (groupType=="switch"){
    	noun=settings."groupDevice${groupType}".size()==1 ? "device" : "devices"
        if (op == "on" || op == "off") { settings."groupDevice${groupType}"?."$op"();result = voicePost && !noAck ? replaceVoiceVar(voicePost) : noAck ? " " : "I am turning ${op} the ${noun} in the group named '${app.label}'. " }
        else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost && !noAck? replaceVoiceVar(voicePost) : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " }
        else { result = "For a switch group, be sure to give an 'on', 'off' or 'toggle' command. "}
    }
    else if (groupType=="switchLevel"){
        noun=settings."groupDevice${groupType}".size()==1 ? "dimmer" : "dimmers"
        if (num==0 && op=="undefined") op="off"
        if (op=="on" || op=="off"){ settings."groupDevice${groupType}"?."$op"();result = voicePost ? replaceVoiceVar(voicePost) : noAck ? " " :  "I am turning ${op} the ${noun} in the group named '${app.label}'. "}
        else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost ? replaceVoiceVar(voicePost) : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " }
		else if (num >0 && cmd =="undefined") { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost ? replaceVoiceVar(voicePost) : noAck ? " " : "I am setting the ${noun} in the ${app.label} to ${valueWord}. " }
		else if (op == "increase" || op=="raise" || op=="up" || op == "decrease" || op=="down" || op=="lower"){
        	settings."groupDevice${groupType}".each{
            	upDown(it, op, num)
                if (op == "increase" || op=="raise" || op=="up") result = "I have raised the brightness of the ${noun} in the group named '${app.label}'. "
                if (op == "decrease" || op=="down" || op=="lower") result = "I have decreased the brightness of the ${noun} in the group named '${app.label}'. "
            }    
        }
        else result = "For a dimmer group, be sure to use an 'on', 'off', 'toggle' or brightness level setting. " 
	}
    else if (groupType=="colorControl"){
        noun=settings."groupDevice${groupType}".size()==1 ? "colored light" : "colored lights"
        if (op=="on" || op=="off"){ settings."groupDevice${groupType}"?."$op"();result = voicePost && !noAck ? replaceVoiceVar(voicePost) :  noAck ? " " : "I am turning ${op} the ${noun} in the group named '${app.label}'. "}
        else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost && !noAck ? replaceVoiceVar(voicePost) :  noAck ? " " :"I am toggling the ${noun} in the group named '${app.label}'. " }
		else if (num > 0 && !colorData && op =="undefined") { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost && !noAck  ? replaceVoiceVar(voicePost) :  noAck ? " " :"I am setting the ${noun} in the '${app.label}' group to ${valueWord}. " }
		else if (colorData && param) { 
        	settings."groupDevice${groupType}"?.setColor(colorData)
            if (!voicePost && !noAck){
            	result ="I am setting the ${noun} in the ${app.label} to ${param}"
    			valueWord = num ==100 ? " and ${proNoun} maximum brightness" : op =="low" || op=="medium" || op=="high" ? ",and to ${op}, or a brightness level of ${num}%" : ", at a brightness level of ${num}%"
                result += num > 0 ? "${valueWord}. " : ". "
            }
            else if (voicePost && !noAck)  result = replaceVoiceVar(voicePost) 
            else result = " "
        }
        else if (op == "increase" || op=="raise" || op=="up" || op == "decrease" || op=="down" || op=="lower"){
        	settings."groupDevice${groupType}".each{
            	upDown(it, op, num)
                if (op == "increase" || op=="raise" || op=="up") result = "I have raised the brightness of the ${noun} in the group named '${app.label}'"
                if (op == "decrease" || op=="down" || op=="lower") result = "I have decreased the brightness of the ${noun} in the group named '${app.label}'"
            	result += num>0 ? " by ${num}%. " : ". "
            }    
        }
        else result = "For a colored light group, be sure to give me an 'on', 'off', 'toggle', brightness level or color command. " 
	}
    else if (groupType=="lock"){
    	noun=settings."groupDevice${groupType}".size()==1 ? "device" : "devices"
            if (op == "lock"|| op == "unlock" ){ 
                if (param =="undefined" || (param !="undefined" && param == num)){
				settings."groupDevice${groupType}"?."$op"()
				result = voicePost && !noAck ? replaceVoiceVar(voicePost) : noAck ? " " : "I am ${op}ing the ${noun} in the group named '${app.label}'. " 
			}
			else result = "To lock or unlock a group, you must say use the proper password. "
        }
        else { result = "For a lock group, you must use a 'lock' or 'unlock' command. " }
    }
    else if (groupType=="doorControl"){
     	noun=settings."groupDevice${groupType}".size()==1 ? "door" : "doors"
        if (op == "open"|| op == "close" ){
        	if (param && param == num){
            	settings."groupDevice${groupType}"?."$cmd"()
            	def condition = op=="close" ? "closing" : "opening"
            	result = voicePost && !noAck  ? replaceVoiceVar(voicePost) : noAck ? " " :  "I am ${condition} the ${noun} in the group named '${app.label}'. "
        	}
            else result = "To open or close a group of doors, you must say use the proper password. "
        }
        else { result = "For a door group, you must use an 'open' or 'close' command. "}
    }
    else if (groupType=="thermostat"){
        noun=settings."groupDevice${groupType}".size()==1 ? "thermostat in the group named '${app.label}'" : "thermostats in the group named '${app.label}'"
        if (num>0) num = num < parent.getTstatLimits().hi && num > parent.getTstatLimits().lo ? num : num > parent.getTstatLimits().hi ? parent.getTstatLimits().hi : parent.getTstatLimits().lo 
        if (op == "on" || op=="off") {
        	if (param == "undefined" && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning on a thermostat group. "
            if (param =="heat" || param=="heating") {result="I am setting the ${noun} to 'heating' mode. "; settings."groupDevice${groupType}"?.heat()}
			if (param =="cool" || param=="cooling") {result="I am setting the ${noun} to 'cooling' mode. "; settings."groupDevice${groupType}"?.cool()}
			if (param =="auto" || param=="automatic") {result="I am setting the ${noun} to 'auto' mode. Please note, "+
				"to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints separately. " ; settings."groupDevice${groupType}"?.auto()}
            if (op == "off") result = "I am turning off the ${noun}. "
            if (parent.isStelpro() && param=="eco"){ result="I am setting the ${noun} to 'eco' mode. "; settings."groupDevice${groupType}"?.setThermostatMode("eco") }
			if (parent.isStelpro()  && param=="comfort"){ result="I am setting the ${noun} to 'comfort' mode. "; settings."groupDevice${groupType}"?.setThermostatMode("comfort") }
        	if (param=="home" && parent.isNest()) { result="I am setting the ${noun} to 'home' mode. "; settings."groupDevice${groupType}"?.present() }
            if (param=="away" && parent.isNest()) { 
            	result="I am setting the ${noun} to 'away' mode. Please note that Nest thermostats will not temperature changes while in 'away' status."
                settings."groupDevice${groupType}"?.away()
            }
        }
        else {
            param = tstatDefaultCool && param == "undefined" ? "cool" : tstatDefaultHeat && param == "undefined" ? "heat" : param
			if (param == "undefined") result = "You must designate a 'heating' or 'cooling' parameter when setting the temperature of a thermostat group. "
            if ((param =="heat" || param =="heating") && num > 0) {
				result="I am setting the heating setpoint of the ${noun} to ${num} degrees. "
				settings."groupDevice${groupType}"?.setHeatingSetpoint(num) 
				if (parent.isStelpro()) settings."groupDevice${groupType}"?.applyNow()
			}
			if ((param =="cool" || param =="cooling") && num>0) {
				result="I am setting the cooling setpoint of the ${noun} to ${num} degrees. "
				settings."groupDevice${groupType}"?.setCoolingSetpoint(num)
			}
            if (num == parent.getTstatLimits().hi) result += "This is the maximum temperature I can set for this device group. "
            if (num == parent.getTstatLimits().lo) result += "This is the minimum temperature I can set for this device group. "
		} 
    }
    else result = "I did not understand what you are attempting to do with the group named '${app.label}'. Be sure it is configured correctly within the SmartApp. " 
    result
}
//Control Handler-----------------------------------------------------------
def controlResults(sDelay){	
	def result = "", delay
    if (cDelay>0 || sDelay>0) delay = sDelay==0 ? cDelay as int : sDelay as int
    if (macroDesc() !="Status: UNCONFIGURED - Tap to configure macro"){	
		result = (!delay || delay == 0) ? "I am running the '${app.label}' control macro. " : delay==1 ? "I'll run the '${app.label}' control macro in ${delay} minute. " : "I'll run the '${app.label}' control macro in ${delay} minutes. "
		if (sDelay == 9999) { 
        	result = "I am cancelling all scheduled executions of the control macro, '${app.label}'. "  
            state.scheduled = false
            unschedule() 
        }
		if (!state.scheduled) {
        	if (!delay || delay == 0) controlHandler() 
            else if (delay < 9999) { runIn(delay*60, controlHandler, [overwrite: true]) ; state.scheduled=true}
            result = voicePost && !noAck ? replaceVoiceVar(voicePost) : noAck ? " " : result
		}
        else result = "The control macro, '${app.label}', is already scheduled to run. You must cancel the execution or wait until it runs before you can run it again. "
    }
    else result="The control macro, '${app.label}' is not properly configured. Use your SmartApp to configure the macro. "
	return result
}
def controlHandler(){
   	state.scheduled = false
   	def cmd = [switch: switchesCMD, dimmer: dimmersCMD, cLight: cLightsCMD, tstat: tstatsCMD, lock: locksCMD, garage: garagesCMD]
    if (phrase) location.helloHome.execute(phrase)
	if (setMode && location.mode != setMode) {
		if (location.modes?.find{it.name == setMode}) setLocationMode(setMode)
		else log.warn "Unable to change to undefined mode '${setMode}'"
	}
    if (switches && cmd.switch) cmd.switch == "toggle" ? toggleState(switches) : switches?."${cmd.switch}"()
    if (dimmers && cmd.dimmer){
    	if (cmd.dimmer == "set"){
        	def level = dimmersLVL < 0 || !dimmersLVL ?  0 : dimmersLVL >100 ? 100 : dimmersLVL as int
        	dimmers?.setLevel(level)
        }
        else cmd.dimmer == "toggle" ? toggleState(dimmers) : dimmers?."${cmd.dimmer}"()
    }
    if (cLights && cmd.cLight){
    	if (cmd.cLight == "set"){
            def level = !cLightsLVL || cLightsLVL < 0 ? 0 : cLightsLVL >100 ? 100 : cLightsLVL as int
            cLightsCLR ? setColoredLights(cLights, cLightsCLR, level, type) : cLights?.setLevel(level)
        }
        else if (cmd.cLight == "toggle") toggleState(cLights)	
        else cLights?."${cmd.cLight}"()
    }
    if (locks && cmd.lock) locks?."${cmd.lock}"()
    if (tstats && cmd.tstat){
        if ((cmd.tstat == "heat" || cmd.tstat == "cool") && tstatLVL) {
        	def tLevel = tstatLVL < 0 ?  0 : tstatLVL > 100 ? 100 : tstatLVL as int
    		cmd.tstat == "heat" ? tstats?.setHeatingSetpoint(tLevel) : tstats?.setCoolingSetpoint(tLevel)
        }
        if (cmd.tstat == "home" || cmd.tstat == "away") tstats?."${cmd.tstat}"()
	}
    if (extInt == "0" && http){
        log.info "Attempting to run: ${http}"
        httpGet(http)
    }
	if (extInt == "1" && ip && port && command){
        def deviceHexID  = convertToHex (ip, port)
        log.info "Device Network Id set to ${deviceHexID}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET /${command} HTTP/1.1\r\nHOST: ${ip}:${port}\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceHexID}"))    
    }
    if (SHM){
    	log.info "Setting Smart Home Monitor to " + SHM
        sendLocationEvent(name: "alarmSystemStatus", value: SHM)
    }
    if (garages && cmd.garage) garages?."${cmd.garage}"()
   	if ((pushMsg || smsNum || contacts) && smsMsg) sendMSG(smsNum, smsMsg, pushMsg, contacts)
}
//Macro Handler-------------------------------------------------------------
def macroResults(num, cmd, colorData, param){ def result = macroType == "Voice" ? reportResults() : macroType == "Control" ? controlResults(num) : groupResults(num, cmd, colorData, param) }
//Report Handler-------------------------------------------------------------
def reportResults(){
    def fullMsg=""
	try {
        fullMsg = voicePre ?  voicePre + " ": ""
        if (voiceOnSwitchOnly) fullMsg += voiceSwitch ? switchOnReport(voiceSwitch, "switches") : ""
        else fullMsg += voiceSwitch ? reportStatus(voiceSwitch, "switch") : ""
        if (voiceOnSwitchEvt) fullMsg += getLastEvt(voiceSwitch, "'switch on'", "on", "switch")
        if (voiceOnDimmerOnly) fullMsg += voiceDimmer ? switchOnReport(voiceDimmer, "dimmers") : ""
        else fullMsg += voiceDimmer ? reportStatus(voiceDimmer, "level") : ""
 		if (voiceOnDimmerEvt) fullMsg += getLastEvt(voiceDimmer, "'dimmer on'", "on", "dimmer")
        fullMsg += voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? doorWindowReport() : ""
        if (voiceTemperature && (voiceTemperature.size() == 1 || !voiceTempAvg)) fullMsg += reportStatus(voiceTemperature, "temperature")
        else if (voiceTemperature && voiceTemperature.size() > 1 && voiceTempAvg) fullMsg += "The average of the monitored temperature devices is: " + parent.getAverage(voiceTemperature, "temperature") + " degrees. "
        if (voiceHumidity && (voiceHumidity.size() == 1 || !voiceHumidAvg)) fullMsg += reportStatus(voiceHumidity, "humidity")
        else if (voiceHumidity  && voiceHumidity.size() > 1 && voiceHumidAvg) fullMsg += "The average of the monitored humidity devices is " + parent.getAverage(voiceHumidity, "humidity") + "%. "
        if (voiceTempSettingSummary && voiceTempSettingsType) fullMsg += voiceTempSettings ? thermostatSummary(): ""
        else fullMsg += (voiceTempSettings && voiceTempSettingsType) ? reportStatus(voiceTempSettings, voiceTempSettingsType) : ""
        fullMsg += voiceSpeaker ? speakerReport() : ""
        fullMsg += voiceWeather || voiceSunset || voiceSunrise ? getWeatherReport() : ""
        fullMsg += voiceWater && waterReport() ? waterReport() : ""
        fullMsg += voicePresence ? presenceReport() : ""
        fullMsg += voiceMotion && motionReport() ? motionReport() : ""
        fullMsg += voiceMode ? "The current SmartThings mode is set to, '${location.currentMode}'. " : ""
        fullMsg += voiceSHM ? "The current Smart Home Monitor status is '${location.currentState("alarmSystemStatus")?.value}'. " : ""
        fullMsg += voiceBattery && batteryReport() ? batteryReport() : ""
        fullMsg += voicePost ? voicePost : ""
	}
    catch(e){ fullMsg = "There was an error processing the report. Please try again. If this error continues, please contact the author of Ask Alexa. " }
    if (!fullMsg) fullMsg = "The voice report, '${app.label}', did not produce any output. Please check the configuration of the report within the SmartApp. "  
    if ((parent.getAdvEnabled() && voiceRepFilter) || voicePre || voicePost) fullMsg = replaceVoiceVar(fullMsg)
    return fullMsg
}
//Voice report sections---------------------------------------------------
def switchOnReport(devices, type){
	def result = ""
    if (devices.latestValue("switch").contains("on")) devices.each { deviceName->
    	if (deviceName.latestValue("switch")=="on") {
        	result += "The ${deviceName} is on"
        	result += type == "dimmers" && deviceName.latestValue("level") ? " and set to ${deviceName.latestValue("level") as int}%. " : ". "
		}
    }
	result
}
def thermostatSummary(){
	def result = "", monitorCount = voiceTempSettings.size(), matchCount = 0, err = false
    for (device in voiceTempSettings) {
    	try{ if (device.latestValue(voiceTempSettingsType) as int == voiceTempTarget as int)  matchCount ++ }
        catch (e) { err=true }
    }
    if (!err){
        def difCount = monitorCount - matchCount
        if (monitorCount == 1 &&  difCount==1) result +="The monitored thermostat, ${voiceTempSettings}, is not set to ${voiceTempTarget} degrees. "
        else if (monitorCount == 1 && !difCount) result +="The monitored thermostat, ${voiceTempSettings}, is set to ${voiceTempTarget} degrees. "
        if (monitorCount > 1) {
            if (difCount==monitorCount) result += "None of the thermostats are set to ${voiceTempTarget} degrees. "
            else if (matchCount==1) {
                for (device in voiceTempSettings){
                    if (device.latestValue(voiceTempSettingsType) as int == voiceTempTarget as int){
                        result += "Of the ${monitorCount} monitored thermostats, only ${device} is set to ${voiceTempTarget} degrees. "
                    }
                }
            }
            else if (difCount && matchCount>1) {
                result += "Some of the thermostats are set to ${voiceTempTarget} degrees except"
                for (device in voiceTempSettings){
                    if (device.latestValue(voiceTempSettingsType) as int != voiceTempTarget as int){
                        result += " ${device}"
                        difCount = difCount -1
                        result += difCount && difCount == 1 ? " and" : difCount && difCount > 1 ? ", " : ". "
                    }
                }
            }
        }
    }
    else result="Some of your thermostats are not able to provide their setpoint. Please choose another device type to report on. " 
    result
}
def reportStatus(deviceList, type){
	def result = ""
    def appd = type=="temperature" || type=="thermostatSetpoint" || type == "heatingSetpoint" || type=="coolingSetpoint" ? " degrees" : type == "humidity" ? " percent relative humidity" : ""
    if (type != "thermostatSetpoint" && type != "heatingSetpoint" && type !="coolingSetpoint") {
		deviceList.each {deviceName->
			if (type=="level" && deviceName.latestValue("switch")=="on") result += "The ${deviceName} is on and set to ${deviceName.latestValue(type) as int}%. "
            else if (type=="level" && deviceName.latestValue("switch")=="off") result += "The ${deviceName} is off. "
            else result += "The ${deviceName} is ${deviceName.latestValue(type)}${appd}. " 
		}
    }
	else deviceList.each { deviceName->
    	try { result += "The ${deviceName} is set to ${deviceName.latestValue(type) as int}${appd}. " }
    	catch (e) { result = "The ${deviceName} is not able to provide its setpoint. Please choose another setpoint type to report on. " }
    }
    result
}
def speakerReport(){
	def result = ""
    if (voiceSpeakerOn) {
        if (voiceSpeaker.latestValue("status").contains("playing")) {
        	voiceSpeaker.each { deviceName->
            	def level = deviceName.currentValue("level") as int
                def track = deviceName.currentValue("trackDescription")
                def mute = deviceName.currentValue("mute")
                if (deviceName.latestValue("status")=="playing") {
                	result += "${deviceName} is playing"
                    if (track) result += ": ${track}"
                    result += mute == "unmuted" ? " at a volume level of ${level}%. ":" but is currently muted. "
    			}
        	}
		}
	}
	else{
    	voiceSpeaker.each { deviceName->
			def onOffStatus = deviceName.currentValue("status")
            def level = deviceName.currentValue("level") as int
            def track = deviceName.currentValue("trackDescription")
			def mute = deviceName.currentValue("mute")
			result += "${deviceName} is ${onOffStatus}"
			result += onOffStatus =="stopped" ? ". " : onOffStatus=="playing" && track ? ": '${track}'" : ""
			result += onOffStatus == "playing" && level && mute =="unmuted" ? ", and it's volume is set to ${level}%. " : mute =="muted" ? ", and it's currently muted. " :""
		}
	}
    result
}
def presenceReport(){
	def result = ""
    if (voicePresentOnly) {
        if (voicePresence.latestValue("presence").contains("not present")) {
        	voicePresence.each { deviceName->
            	if (deviceName.latestValue("presence")=="not present") result += "${deviceName} is not present. "
    		}
        }
	}
    else voicePresence.each {deviceName->result += "${deviceName} is " + deviceName.latestValue("presence") + ". " }
    if (voicePresentEvt) result += getLastEvt(voicePresence, "arrival", "present", "presence sensor")
    if (voiceGoneEvt) result += getLastEvt(voicePresence, "departure", "not present", "presence sensor")
    result
}
def motionReport(){
	def result = "", currVal
    if (voiceMotionOnly) {
        if (voiceMotion.latestValue("motion").contains("active")) {
        	voiceMotion.each { deviceName->
            	if (deviceName.latestValue("motion")=="active") result += "${deviceName} is reading motion. "
    		}
        }
    }
	else {
		voiceMotion.each {deviceName->
			currVal = [active: "movement", inactive: "no movement"][deviceName.latestValue("motion")] ?: deviceName.latestValue("motion")
        	result += "${deviceName} is reading " + currVal + ". "}
	}
    if (voiceMotionEvt) result += getLastEvt(voiceMotion, "movement", "active", "sensor")	
    result 
}
def doorWindowReport(){
	def countOpened = 0, countOpenedDoor = 0, countUnlocked = 0
    def result = "", listOpened = "", listUnlocked = ""
    if (voiceDoorSensors && voiceDoorSensors.latestValue("contact").contains("open")){
    	for (sensor in voiceDoorSensors) if (sensor.latestValue("contact")=="open") countOpened ++
        listOpened = listDevices(voiceDoorSensors, "contact", "open", countOpened )
	}
	if (voiceDoorControls && voiceDoorControls.latestValue("door").contains("open")){
        for (door in voiceDoorControls) if (door.latestValue("door") == "open") countOpenedDoor ++
        listOpened += listDevices(voiceDoorControls, "door", "open", countOpenedDoor)
    }
    if (voiceDoorLocks && voiceDoorLocks.latestValue("lock").contains("unlocked")){
        for (doorLock in voiceDoorLocks) if (doorLock.latestValue("lock")=="unlocked") countUnlocked ++
        listUnlocked = listDevices(voiceDoorLocks, "lock", "unlocked", countUnlocked)
    }
    def totalCount = countOpenedDoor + countOpened
    if (voiceDoorAll){
    	if (!totalCount) {
        	result += "All of the doors and windows are closed"
        	result += voiceDoorLocks ? " and locked. " : ". "
        }
        if (!countOpened && !countOpenedDoor && countUnlocked){
   			result += "All of the doors and windows are closed, but the "
            result += countUnlocked > 1 ? "following are unlocked: ${listUnlocked}. " :"${listUnlocked} is unlocked. "
    	}
        if ((countOpened || countOpenedDoor) && !countUnlocked){
   			result += voiceDoorLocks ? "All of the doors are locked, but the " : "The"
            result += totalCount > 1 ? "following doors or windows are open: ${listOpened}. " : "${listOpened} is open. "
    	}
    }   
	else {
		if ((countOpened || countOpenedDoor) && !countUnlocked) result += totalCount > 1 ? "The following doors or windows are currently open: ${listOpened}. " : "${listOpened} is open. "
        if (!countOpened && !countOpenedDoor && countUnlocked) result += countUnlocked > 1 ? "The following doors are unlocked: ${listUnlocked}. " : "The ${listUnlocked} is unlocked. "
	}
    if ((countOpened || countOpenedDoor) && countUnlocked){
		def verb = totalCount > 1 ? "following doors or windows are currently open: ${listOpened}" : "${listOpened} is open"
		def verb1 = countUnlocked > 1 ? "following are unlocked: ${listUnlocked}" : "${listUnlocked} is unlocked"
		result += "The ${verb}. Also, the ${verb1}. "
    }
    if (voiceDoorEvt && (voiceDoorSensors ||voiceDoorControls )) result += getLastEvt(voiceDoorSensors, "open", "open", "sensor")
    if (voiceLockEvt && voiceDoorLocks) result += getLastEvt(voiceDoorLocks, "unlock", "unlocked", "sensor")
    result
}
def listDevices(devices, type, condition, count){
    def result = ""
	for (deviceName in devices){	
		if (deviceName.latestValue("${type}") == "${condition}"){
			result += " ${deviceName}"
			count = count - 1
			if (count == 1) result += " and the "
			else if (count> 1) result += ", "
		}
	}
    result
}
def batteryReport(){
    def result = "", count = 0, batteryThresholdLevel = batteryThreshold as int, nullCount =0
	for (device in voiceBattery) if (device.latestValue("battery")< batteryThresholdLevel) count ++
    for (deviceName in voiceBattery){	
		if (deviceName.latestValue("battery") < batteryThresholdLevel){
			result += deviceName.latestValue("battery") ? "The ${deviceName} battery is at ${deviceName.latestValue("battery")}%. " : ""
			result += !deviceName.latestValue("battery") ? "The ${deviceName} battery is reading null, which may indicate an issue with the device. " : ""
            count = count - 1
		}
	}
    result
}
def waterReport(){
    def result = "", count = 0
	for (device in voiceWater) if (device.latestValue("water") != "dry") count ++
        if (!voiceWetOnly) for (deviceName in voiceWater) { result += "The ${deviceName} is ${deviceName.latestValue("water")}. " }
        else if (count){
        	for (deviceName in voiceWater){
            	if (deviceName.latestValue("water") != "dry"){
                	result += "The ${deviceName} is sensing water is present. "
                    count=count-1
                }
         	}
        }
    result
}
//Parent Code Access
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd) }
def getType(){ return macroType }
def groupMacroList(){ return groupMacros }
//Common Code
def upDown(device, op, num){
    def numChange, newLevel, currLevel, defMove
    defMove = parent.getLightIncVal() ; currLevel = device.currentValue("switch")=="on" ? device.currentValue("level") as int : 0
    if (op == "increase" || op=="raise" || op=="up")  numChange = num == 0 ? defMove : num > 0 ? num : 0
    if (op == "decrease" || op=="down" || op=="lower") numChange = num == 0 ? -defMove : num > 0 ? -num  : 0
    newLevel = currLevel + numChange; newLevel = newLevel > 100 ? 100 : newLevel < 0 ? 0 : newLevel
    device?.setLevel(newLevel)
}
private getDayOk(dayList) {
	def result = true
    if (dayList) {
		def df = new java.text.SimpleDateFormat("EEEE")
		location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		def day = df.format(new Date())
		result = dayList.contains(day)
	}
    result
}
private getTimeOk(startTime, endTime) {
	def result = true, currTime = now()
	def start = startTime ? timeToday(startTime).time : null
	def stop = endTime ? timeToday(endTime).time : null
	if (startTime && endTime) result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	else if (startTime) result = currTime >= start
    else if (endTime) result = currTime <= stop
    result
}
def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
    if(start && end) timeLabel = "Between " + timeParse("${start}", "h:mm a") + " and " +  timeParse("${end}", "h:mm a")
    else if (start) timeLabel = "Start at " + timeParse("${start}", "h:mm a")
    else if (end) timeLabel = "End at " + timeParse("${end}", "h:mm a")
	timeLabel	
}
def macroDesc(){
	def desc = ""
    def customAck = voicePost && !noAck ? "; includes a custom acknowledgement message" : noAck ? "; there will be no acknowledgement message" : ""
    if (macroType == "Control" && (phrase || setMode || SHM || getDeviceDesc() != "Status: UNCONFIGURED - Tap to configure" || 
    	getHTTPDesc() !="Status: UNCONFIGURED - Tap to configure" || (contacts && smsMsg) || (smsNum && pushMsg && smsMsg))) 
        	desc= "Control Macro CONFIGURED${customAck} - Tap to edit" 
    if (macroType =="Voice" && (voicePre || voiceSwitch || voiceDimmer || voiceDoorSensors || voiceDoorControls || voiceDoorLocks || 
    	voiceTemperature ||  voiceTempSettings || voiceTempVar || voiceHumidVar || voiceHumidity || weatherDesc()=="Status: CONFIGURED - Tap to edit" ||
        voiceWater || voiceMotion || voicePresence || voiceBattery || voicePost || voiceMode || voiceSHM)) { 
        	desc= "Voice Report CONFIGURED - Tap to edit" 
	}
	if (macroType =="Group" && groupType && settings."groupDevice${groupType}") {
    	def groupDesc =[switch:"Switch Group",switchLevel:"Dimmer Group",thermostat:"Thermostat Group",colorControl:"Colored Light Group",lock:"Lock Group",doorControl: "Door Group"][groupType] ?: groupType
        def countDesc = settings."groupDevice${groupType}".size() == 1 ? "one device" : settings."groupDevice${groupType}".size() + " devices"
        if (parent.isStelpro() && groupType=="thermostat") customAck = "- Accepts Stelpro baseboard heater commands" + customAck
        if (parent.isNest() && groupType=="thermostat") customAck = "- Accepts Nest 'Home'/'Away' commands" + customAck
        customAck = tstatDefaultHeat && groupType=="thermostat" ? "- Sends heating setpoint by default" + customAck : tstatDefaultCool && groupType=="thermostat" ? "- Sends cooling setpoint by default" + customAck : customAck
        desc = "${groupDesc} CONFIGURED with ${countDesc}${customAck} - Tap to edit" 
    }
    if (macroType =="GroupM" &&  groupMacros) {
    	def countDesc = groupMacros.size() == 1 ? "one macro" : groupMacros.size() + " macros"
        desc = "Macro Group CONFIGURED with ${countDesc}${customAck} - Tap to edit" 
    }
    desc = desc ? desc : "Status: UNCONFIGURED - Tap to configure macro"
}
def greyOutMacro(){ def result = macroDesc() == "Status: UNCONFIGURED - Tap to configure macro" ? "" : "complete" }
def greyOutStateHTTP(){ def result = getHTTPDesc() == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete" }
def reportDesc(param1, param2, param3, param4, param5) {def result = param1 || param2 || param3 || param4 || param5  ? "Status: CONFIGURED - Tap to edit" : "Status: UNCONFIGURED - Tap to configure"}
def reportDescMSHM() {
	def result= "Status: "
    result += voiceMode ? "Mode: On" : "Mode: Off"
    result += voiceSHM ? ", SHM: On" : ", SHM: Off"
}
def greyOutState(param1, param2, param3, param4, param5){def result = param1 || param2 || param3 || param4 || param5 ? "complete" : ""}
def weatherDesc(){ def result = voiceWeatherToday || voiceWeatherTonight || voiceWeatherTomorrow || voiceSunrise || voiceSunset ? "Status: CONFIGURED - Tap to edit" : "Status: UNCONFIGURED - Tap to configure" }
def greyOutWeather(){ def result = voiceWeatherToday || voiceWeatherTonight || voiceWeatherTomorrow || voiceSunrise || voiceSunset ? "complete" : "" }
def deviceGreyOut(){ def result = getDeviceDesc() == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete" }
def getDeviceDesc(){  
    def result,lvl, cLvl, clr, tLvl
    def cmd = [switch: switchesCMD, dimmer: dimmersCMD, cLight: coloredLightsCMD, tstat: tstatsCMD, lock: locksCMD, garage: garagesCMD]
	lvl = cmd.dimmer == "set" && dimmersLVL ? dimmersLVL as int : 0
	cLvl = cmd.cLight == "set" && cLightsLVL ? cLightsLVL as int : 0
	clr = cmd.cLight == "set" && cLightsCLR ? cLightsCLR  : ""
    tLvl = tstats ? tstatLVL : 0
    lvl = lvl < 0 ? lvl = 0 : lvl >100 ? lvl=100 : lvl
    tLvl = tLvl < 0 ? tLvl = 0 : tLvl >100 ? tLvl=100 : tLvl
    cLvl = cLvl < 0 ? cLvl = 0 : cLvl >100 ? cLvl=100 : cLvl
    if (switches || dimmers || cLights || tstats || locks || garages) {
    	result = switches && cmd.switch ? "${switches} set to ${cmd.switch}" : ""
        result += result && dimmers && cmd.dimmer ? "\n" : ""
        result += dimmers && cmd.dimmer && cmd.dimmer  != "set" ? "${dimmers} set to ${cmd.dimmer}" : ""
        result += dimmers && cmd.dimmer && cmd.dimmer == "set" ? "${dimmers} set to ${lvl}%" : ""	
        result += result && cLights && cmd.cLight ? "\n" : ""
    	result += cLights && cmd.cLight && cmd.cLight != "set" ? "${cLights} set to ${cmd.cLight}":""
        result += cLights && cmd.cLight && cmd.cLight == "set" ? "${cLights} set to " : ""
        result += cLights && cmd.cLight && cmd.cLight== "set" && clr ? "${clr} and " : ""
        result += cLights && cmd.cLight && cmd.cLight == "set" ? "${cLvl}%" : ""
        result += result && tstats && tLvl ? "\n" : ""
        result += tstats && cmd.tstat && tLvl ? "${tstats} set to ${cmd.tstat} : ${tLvl}" : ""
        result += result && locks && cmd.lock ? "\n":""
        result += locks && cmd.lock ? "${locks} set to ${cmd.lock}" : ""
        result += result && garages && cmd.garage ? "\n" : ""
        result += garages && cmd.garage ? "${garages} set to ${cmd.garage}" : ""
    }
    result = result ? result : "Status: UNCONFIGURED - Tap to configure"
}
def getHTTPDesc(){
	def result = "", param = [http:http, ip:ip, port:port, cmd:command]
    if (extInt == "0" && param.http) result += param.http
    else if (extInt == "1" && param.ip && param.port && param.cmd) result += "http://${param.ip}:${param.port}/${param.cmd}"
    result = result ? result : "Status: UNCONFIGURED - Tap to configure"
}
private getLastEvt(devGroup, evtTxt, searchVal, devTxt){
    def devEvt, evtLog=[],  lastEvt="I could not find any ${evtTxt} events in the log. "
        devGroup.each{ deviceName-> devEvt= deviceName.events()
            devEvt.each {
                if (it.value && it.value==searchVal) evtLog << [device: deviceName, time: it.date.getTime(), desc: it.descriptionText]
            }
       	} 
    if (evtLog.size()>0){
        evtLog.sort({it.time})
        evtLog.reverse(true)
        def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
        def eventDay = new Date(evtLog.time[0]).format("EEEE, MMMM d, yyyy", location.timeZone)
        def voiceDay = today == eventDay ? "today" : "On " + eventDay  
        def evtTime = new Date(evtLog.time[0]).format("h:mm aa", location.timeZone)
        def multipleTxt = devGroup.size() >1 ? "within the monitored group was the ${evtLog.device[0]} ${devTxt}" : "was"
        lastEvt = "The last ${evtTxt} event ${multipleTxt} ${voiceDay} at ${evtTime}. " 
    }    
    return lastEvt
}
private replaceVoiceVar(msg) {
    def df = new java.text.SimpleDateFormat("EEEE")
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date()), time = parseDate("","h:mm a"), month = parseDate("","MMMM"), year = parseDate("","yyyy"), dayNum = parseDate("","d")
    def varList = parent.getVariableList()
    def temp = varList[0].temp
    def humid = varList[0].humid
    def people = varList[0].people
    def fullMacroType=[GroupM: "Macro Group", Control:"Control Macro", Group:"Device Group", Voice:"Voice Reporting"][macroType] ?: macroType
	def fullDeviceType=[colorControl: "Colored Light",switchLevel:"Dimmer" ,doorControl:"Door",lock:"Lock",switch:"Switch",thermostat:"Thermostat"][groupType] ?: groupType
    msg = macroType=="Group" ? msg.replace('%dtype%', "${fullDeviceType}") : msg.replace('%dtype%', "")
    msg = macroType=="Group" && groupType =="switch" ? msg.replace('%dtypes%', "switches") :  macroType=="Group" && groupType !="switch" ? msg.replace('%dtypes%', "${fullDeviceType}s") : msg.replace('%dtypes%', "")
    msg = msg.replace('%mtype%', "${fullMacroType}")
    msg = msg.replace('%macro%', "${app.label}")
    msg = msg.replace('%day%', day)
    msg = msg.replace('%date%', "${month} ${dayNum}, ${year}")
    msg = msg.replace('%time%', "${time}")
    msg = msg.replace('%temp%', "${temp}")
    msg = msg.replace('%humid%', "${humid}")
    msg = msg.replace('%people%', "${people}")
    if (parent.getAdvEnabled() && voiceRepFilter) {
    	def textFilter=voiceRepFilter.toLowerCase().tokenize(",")
    	textFilter.each{ msg = msg.toLowerCase().replace("${it}","") }
    }
    msg
}
private timeParse(time, type) { new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("${type}", timeZone(formattedDate))}
private parseDate(time, type){
    long longDate = time ? Long.valueOf(time).longValue() : now()
    def formattedDate = new Date(longDate).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", formattedDate).format("${type}", timeZone(formattedDate))
}
private getWeatherReport(){
    def msg = ""
    if (location.timeZone || zipCode) {
		def sb = new StringBuilder(), weatherName=""
        def isMetric = location.temperatureScale == "C"
		def weather = getWeatherFeature("forecast", zipCode)
        if (voiceWeatherToday  || voiceWeatherTonight || voiceWeatherTomorrow ){
            if (voiceWeatherToday){
                sb << "Today's forecast is "
                if (isMetric) sb << weather.forecast.txt_forecast.forecastday[0].fcttext_metric else sb << weather.forecast.txt_forecast.forecastday[0].fcttext + " "
            }
            if (voiceWeatherTonight){
                sb << "Tonight's forecast is "
                if (isMetric) sb << weather.forecast.txt_forecast.forecastday[1].fcttext_metric else sb << weather.forecast.txt_forecast.forecastday[1].fcttext + " "
            }
            if (voiceWeatherTomorrow){
                sb << "Tomorrow's forecast is "
                if (isMetric) sb << weather.forecast.txt_forecast.forecastday[2].fcttext_metric  else sb << weather.forecast.txt_forecast.forecastday[2].fcttext + " "
            }
            msg = sb.toString()
            translateTxt().each {msg = msg.replaceAll(it.txt,it.cvt)}
        }
        if (voiceSunrise || voiceSunset){
            def todayDate = new Date()
            def s = getSunriseAndSunset(zipcode: zipCode, date: todayDate)	
            def riseTime = parseDate(s.sunrise.time, "h:mm a")
            def setTime = parseDate(s.sunset.time, "h:mm a")
            def currTime = now()
            def verb1 = currTime >= s.sunrise.time ? "rose" : "will rise"
            def verb2 = currTime >= s.sunset.time ? "set" : "will set"
            if (voiceSunrise && voiceSunset) msg += "The sun ${verb1} this morning at ${riseTime} and ${verb2} at ${setTime}. "
            else if (voiceSunrise && !voiceSunset) msg += "The sun ${verb1} this morning at ${riseTime}. "
            else if (!voiceSunrise && voiceSunset) msg += "The sun ${verb2} tonight at ${setTime}. "
        }
    }
    else  msg = "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather forecasts."
    return msg
}
//Translate Maxtrix
def translateTxt(){
	def wordCvt=[]
    wordCvt <<[txt:" N ",cvt: " North "] << [txt:" S ",cvt: " South "] << [txt:" E ",cvt: " East "] << [txt:" W ",cvt: " West "]
    wordCvt <<[txt:" NW ",cvt: " North West "] << [txt:" SW ",cvt: " South West "] << [txt:" NE ",cvt: " North East "] << [txt:" SE ",cvt: " South East "]
	wordCvt <<[txt:" NNW ",cvt: " North-North West "] << [txt:" SSW ",cvt: " South-South West "] << [txt:" NNE ",cvt: " North-North East "] << [txt:" SSE ",cvt: " South-South East "]
	wordCvt <<[txt:" WNW ",cvt: " West-North West "] << [txt:" WSW ",cvt: " West-South West "] << [txt:" ENE ",cvt: " East-North East "] << [txt:" ESE ",cvt: " East-South East "]
	wordCvt <<[txt: /([0-9]+)C/, cvt: '$1 degrees'] << [txt: /([0-9]+)F/, cvt: '$1 degrees']<<[txt: "mph", cvt: "miles per hour"]

}
//Send Messages
def sendMSG(num, msg, push, recipients){
    if (location.contactBookEnabled && recipients) sendNotificationToContacts(msg, recipients)
    else {
    	if (num) {sendSmsMessage(num,"${msg}")}
    	if (push) {sendPushMessage("${msg}")}
    }
}
//Toggle states (off -> on, on -> off)
def toggleState(swDevices){
	swDevices.each{
    	def currState = it.currentValue("switch")
        def newstate = currState == "off" ? "on" : "off"
        it?."$newstate"()
    }
}
private setColoredLights(switches, color, level, type){
	def getColorData = parent.fillColorSettings().find {it.name==color}
    def hueColor = getColorData.hue
	def satLevel = getColorData.sat
	if (color == "Custom-User Defined"){
		hueColor = hueUserDefined ?  hueUserDefined  : 0
		satLevel = satUserDefined ? satUserDefined : 0
		hueColor = hueColor > 100 ? 100 : hueColor < 0 ? 0 : hueColor
		satLevel = satLevel > 100 ? 100 : satLevel < 0 ? 0 : satLevel
	}
    def newValue = [hue: hueColor as int, saturation: satLevel as int, level: level as int]
	switches?.setColor(newValue)
}
//Version 
private def textVersion() {return "Voice Macros Version: 2.0.2a (06/04/2016)"}
private def versionInt() {return 202}
private def versionLong() {return "2.0.2a"}
