/**
 *  Ask Alexa 
 *
 *  Version 2.0.5 - 7/9/16 Copyright © 2016 Michael Struck
 *  Special thanks for Keith DeLong for overall code and assistance and Barry Burke for weather reporting/advisory/lunar phases code
 * 
 *  Version 1.0.0 - Initial release
 *  Version 1.0.0a - Same day release. Bugs fixed: nulls in the device label now trapped and ensure LIST_OF_PARAMS and LIST_OF_REPORTS is always created
 *  Version 1.0.0b - Remove punctuation from the device, mode and routine names. Fixed bug where numbers were removed in modes and routine names 
 *  Version 1.0.1c - Added presense sensors; added up/down/lower/increase/decrease as commands for various devices
 *  Version 1.0.2b - Added motion sensors and a new function, "events" to list to the last events for a device; code optimization, bugs removed
 *  Version 1.1.0a - Changed voice reports to macros, added toggle commands to switches, bug fixes and code optimization
 *  Version 1.1.1d - Added limits to temperature and speaker values; additional macros device types added
 *  Version 1.1.2 (6/5/16) Updated averages of temp/humidity with proper math function
 *  Version 2.0.0b (6/10/16) Code consolidated from Parent/Child to a single code base. Added CoRE Trigger and CoRE support. Many fixes
 *  Version 2.0.1 (6/12/16) Fixed issue with listing CoRE macros; fixed syntax issues and improved acknowledgment message in Group Macros, more CoRE output behind-the-scenes
 *  Version 2.0.2a (6/17/16) Added %delay% macro for custom acknowledgment for pre/post text areas, dimmer/group fixes and added lunar phases (thanks again to Barry Burke), 2nd level acknowledgments in Alexa
 *  Version 2.0.3a (6/21/16) Filter of power meters in reports. Added Weather Advisories.
 *  Version 2.0.4 (7/8/16) Code fixes/optimizations, added additional options for secondary responses
 *  Version 2.0.5 (7/9/16) Fix for null String issues
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
    name: "Ask Alexa${parent ? " - Macro " : ""}",
    namespace: "MichaelStruck",
    singleInstance: true,
    author: "Michael Struck",
    parent: parent ? "MichaelStruck.Ask Alexa" : null,
    description: "Provide interfacing to control and report on SmartThings devices with the Amazon Echo ('Alexa').",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa@2x.png",
  	oauth: true)
preferences {
    page name:"pageMain"
    //Parent Pages
    page name:"mainPageParent"
    	page name:"pageSwitches"
        page name:"pageDoors"
        page name:"pageTemps"
        page name:"pageSpeakers"
        page name:"pageSensors"
        page name:"pageHomeControl"
        page name:"pageMacros"
        page name:"pageSettings"
            page name:"pageReset"
            	page name:"pageConfirmation"
            page name:"pageDefaultValue"
            page name:"pageCustomDevices"
            page name:"pageLimitValue"
            page name:"pageGlobalVariables"   
        page name:"pageAbout"
	//Child Pages
    page name:"mainPageChild"
    	page name:"pageCoRE"
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
            	page name:"pageWeatherCurrent"
            	page name:"pageWeatherForecast"
            page name:"pageSpeakerReport"
}
def pageMain() { if (!parent) mainPageParent() else mainPageChild() }
//Show main page
def mainPageParent() {
    dynamicPage(name: "mainPageParent", install: true, uninstall: false) {
        if (!state.deviceTypes) fillTypeList()
        def duplicates = getDeviceList().name.findAll{getDeviceList().name.count(it)>1}.unique()
        if (duplicates){
        	section ("**WARNING**"){
            	paragraph "You have the following device(s) used multiple times within Ask Alexa:\n\n${getList(duplicates)}\n\nA device should be uniquely named and appear only once in the categories below.",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/caution.png"
            }
        }
        if (findNullDevices()) {
        	section ("**WARNING**"){
            	paragraph findNullDevices(), image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/caution.png"
            }
        }
        section("Items to interface to Alexa") {
        	href "pageSwitches", title: "Switches/Dimmers/Colored Lights", description: getDesc(switches, dimmers ,cLights), state: (switches || dimmers || cLights ? "complete" : null),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/power.png"
            href "pageDoors", title: "Doors/Windows/Locks", description: getDesc(doors, locks, ocSensors), state: (doors || locks || ocSensors ? "complete" : null),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/lock.png"
            href "pageTemps", title: "Thermostats/Temperature/Humidity", description:getDesc(tstats, temps, humid), state: (tstats || temps || humid ? "complete" : null),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/temp.png"
            href "pageSpeakers", title: "Connected Speakers", description: getDesc(speakers, "", ""), state: (speakers ? "complete" : null),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speaker.png"     
            href "pageSensors", title: "Other Sensors", description:getDesc(water, presence, motion), state: (water|| presence|| motion ? "complete" : null),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/sensor.png"
            href "pageHomeControl", title: "Modes/SHM/Routines", description: getDescMRS(), state: (modes|| routines|| SHM? "complete" : null),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png"        
        }
        section("Configure Macros"){
        	href "pageMacros", title: "Voice Macros", description: macroDesc(), state: (childApps.size() ? "complete" : null), image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speak.png"
        }
        section("Options") {
			href "pageSettings", title: "Settings", description: "Tap to configure app settings, get setup information or to reset the access token",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/settings.png"
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get version information, license, instructions or to remove the application",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/info.png"
        }
	}
}
def pageSwitches(){
    dynamicPage(name: "pageSwitches", install: false, uninstall: false) {
        section { paragraph "Switches/Dimmers/Colored Lights", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/power.png"} 
        section("Choose the devices to interface") {
            input "switches", "capability.switch", title: "Choose Switches (On/Off/Toggle/Status)", multiple: true, required: false
            input "dimmers", "capability.switchLevel", title: "Choose Dimmers (On/Off/Toggle/Level/Status)", multiple: true, required: false
            input "cLights", "capability.colorControl", title: "Choose Colored Lights (On/Off/Toggle/Level/Color/Status)", multiple: true, required: false, submitOnChange: true
        }
        if (cLights) {
            section("Custom color"){
                input "customName", "text", title: "Custom Color Name", required: false
                input "customHue", "number", title: "Custom Color Hue", required: false
                input "customSat", "number", title: "Custom Color Saturation", required: false
            }
        }
	}
}
def pageDoors() {
    dynamicPage(name: "pageDoors", install: false, uninstall: false) {
        section { paragraph "Doors/Windows/Locks", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/lock.png" }
        section("Choose the devices to interface") {
            input "doors", "capability.doorControl", title: "Choose Door Controls (Open/Close/Status)" , multiple: true, required: false
            input "ocSensors", "capability.contactSensor", title: "Open/Close Sensors (Status)", multiple: true, required: false
            input "locks", "capability.lock", title: "Choose Locks (Lock/Unlock/Status)", multiple: true, required: false  
        }
    }
}
def pageTemps(){
    dynamicPage(name: "pageTemps",  install: false, uninstall: false) {
        section {paragraph "Thermostats/Temperature/Humidity", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/temp.png"}
        section("Choose the devices to interface") {
            input "tstats", "capability.thermostat", title: "Choose Thermostats (Temperature Setpoint/Status)", multiple: true, required: false 
            input "temps", "capability.temperatureMeasurement", title: "Choose Temperature Devices (Status)", multiple: true, required: false
        	input "humid", "capability.relativeHumidityMeasurement", title: "Choose Humidity Devices (Status)", multiple: true, required: false
        }
    }
}
def pageSpeakers(){
    dynamicPage(name: "pageSpeakers",  install: false, uninstall: false) {
        section {paragraph "Connected Speakers", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speaker.png"}
        section("Choose the devices to interface") {
            input "speakers", "capability.musicPlayer", title: "Choose Speakers (Speaker Control, Status)", multiple: true, required: false 
        }
        section("Speaker Control"){
        	paragraph "Speaker control consists of the following:\nPlay, Stop, Pause\nMute, Un-mute\nNext Track, Previous Track\nLevel (Volume)"
		}
    }
}
def pageSensors(){
    dynamicPage(name: "pageSensors",  install: false, uninstall: false) {
        section {paragraph "Other Sensors", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/sensor.png"}
        section("Choose the devices to interface") {
            input "water", "capability.waterSensor", title: "Choose Water Sensors (Status)", multiple: true, required: false
            input "presence", "capability.presenceSensor", title: "Choose Presence Sensors (Status)", multiple: true, required: false
            input "motion", "capability.motionSensor", title: "Choose Motion Sensors (Status)", multiple: true, required: false
        }
    }
}
def pageHomeControl(){
	dynamicPage(name: "pageHomeControl", uninstall: false) {
        section { paragraph "Modes/Routines/SHM", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png" }
        section ("Choose the features to have voice control over") {
        	input "modes", "bool", title: "Modes (Change/Status)", defaultValue: false
            input "SHM", "bool", title: "Smart Home Monitor (Change/Status)", defaultValue: false
            input "routines", "bool", title: "Routines (Execute)", defaultValue: false
		}
    }
}
def pageMacros() {
    dynamicPage(name: "pageMacros", install: false, uninstall: false) {
        section{ paragraph "Voice Macros", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speak.png" }
        if (childApps.size()) section("Voice Macros available"){}
        def duplicates = childApps.label.findAll{childApps.label.count(it)>1}.unique()
        if (duplicates){
        	section {
        	paragraph "You have mutiple macros with the same name below. Please ensure each macro has a unique name and also does not conflict with  " +
        		"device names as well. ", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/caution.png"
        	}
        }
        section(" "){
        	app(name: "childMacros", appName: "Ask Alexa", namespace: "MichaelStruck", title: "Create A New Macro...", description: "Tap to create a new voice macro", multiple: true, 
        	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/add.png") 
        }
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
        section {paragraph "${textAppName()}\n${textCopyright()}",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa@2x.png"}
        section ("Version numbers") { paragraph "${textVersion()}" } 
        section ("Access token / Application ID"){
            if (!state.accessToken) OAuthToken()
            def msg = state.accessToken != null ? state.accessToken : "Could not create Access Token. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
            paragraph "Access token:\n${msg}\n\nApplication ID:\n${app.id}"
    	}
        section ("Apache license"){ paragraph textLicense() }
    	section("Instructions") { paragraph textHelp() }
        section("Tap below to remove the application and all macros"){}
	}
}
def pageSettings(){
    dynamicPage(name: "pageSettings", uninstall: false){
        section { paragraph "Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/settings.png" }
        section ("Additional voice settings"){ 
        	input "otherStatus", "bool", title: "Speak Additional Status Attributes Of Devices", defaultValue: false, submitOnChange: true
            input "batteryWarn", "bool", title: "Speak Battery Level When Below Threshold", defaultValue: false, submitOnChange: true
            if (batteryWarn) input "batteryThres", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: [5:"<5%",10:"<10%",20:"<20%",30:"<30%",40:"<40%",50:"<50%",60:"<60%",70:"<70%",80:"<80%",90:"<90%",101:"Always play battery level"]
			if (doors || locks) input "pwNeeded", "bool", title: "Password (PIN) Required For Lock Or Door Commands", defaultValue: false, submitOnChange: true
            if ((doors || locks) && pwNeeded) input "password", "num", title: "Numeric Password (PIN)", description: "Enter a short numeric PIN (i.e. 1234)", required: false
       		input "eventCt", "enum", title: "Default Number Of Past Events to Report", options: [[1:"1"],[2:"2"],[3:"3"],[4:"4"],[5:"5"],[6:"6"],[7:"7"],[8:"8"],[9:"9"]], required: false, defaultValue: 1 	
        }
        section ("Continuation of commands..."){
        	input "contError", "bool", title: "After Error", defaultValue: false
            input "contStatus", "bool", title: "After Status/List", defaultValue: false
            input "contAction", "bool", title: "After Action/Event History", defaultValue: false
            input "contMacro", "bool", title: "After Macro Execution", defaultValue: false
        }
        section ("Other Values/Variables"){
        	if (dimmers || tstats || cLights || speakers){
				href "pageDefaultValue", title: "Default Command Values (Dimmers, Volume, etc.)", description: "", state: "complete"
            }
            if (speakers || tstats){
            	href "pageLimitValue", title: "Device Minimum/Maximum Values", description: "", state: "complete"
            }
        	if (!state.accessToken) OAuthToken()
            if (!state.accessToken) paragraph "**You must enable OAuth via the IDE to setup this app**"
            else href url:"${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}", style:"embedded", required:false, title:"Setup Variables (For Amazon Developer sites)", description: none
        	href "pageGlobalVariables", title: "Text Field Variables", description: none, state: (voiceTempVar || voiceHumidVar ? "complete" : null)
            input "invocationName", title: "Invocation Name (Only Used For Examples)", defaultValue: "SmartThings", required: false
        }	
        section ("Advanced") { 
            href "pageConfirmation", title: "Revoke/Reset Access Token", description: "Tap to confirm this action",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/warning.png"
            href "pageCustomDevices", title: "Device Specific Commands", description: none, state: (nestCMD || stelproCMD ? "complete" : null)
            input "advReportOutput", "bool", title: "Advanced Voice Report Filter", defaultValue: false
        }
    }
}
def pageDefaultValue(){
    dynamicPage(name: "pageDefaultValue", uninstall: false){
        if (dimmers || tstats || cLights || speakers){
            section("Increase / Decrease values (When no values are requested)"){
                if (dimmers || cLights) input "lightAmt", "number", title: "Dimmer/Colored Lights", defaultValue: 20, required: false
                if (tstats) input "tstatAmt", "number", title: "Thermostat Temperature", defaultValue: 5, required: false
                if (speakers) input "speakerAmt", "number", title: "Speaker Volume", defaultValue: 5, required: false
            }
        }
        if (dimmers || cLights){
        	section("Low / Medium / High values (For dimmers or colored lights)"){
            	input "dimmerLow", "number", title: "Low Value", defaultValue: 10, required: false
                input "dimmerMed", "number", title: "Medium Value", defaultValue: 50, required: false
                input "dimmerHigh", "number", title: "High Value", defaultValue: 100, required: false
            }
        }
    }
}
def pageCustomDevices(){
    dynamicPage(name: "pageCustomDevices", uninstall: false){
		section("Device Specific Commands"){
            input "nestCMD", "bool", title: "Allow Nest-Specific Thermostat Commands (Home/Away)", defaultValue: false
            input "stelproCMD", "bool", title: "Stelpro Baseboard Thermostat Controls", defaultValue:false
    	}
	}
}
def pageLimitValue(){
    dynamicPage(name: "pageLimitValue", uninstall: false){
        if (speakers){
            section("Speaker Volume Limits"){
				input "speakerHighLimit", "number", title: "Speaker Maximum Volume", defaultValue:20, required: false
            }
        }
        if (tstats){
        	section("Thermostat Setpoint Limits"){
            	input "tstatLowLimit", "number", title: "Thermostat Minimum Value ", defaultValue: 55, required: false
                input "tstatHighLimit", "number", title: "Theremostat Maximum Value", defaultValue: 85, required: false
            }
        }
    }
}
def pageGlobalVariables(){
    dynamicPage(name: "pageGlobalVariables", uninstall: false){
        section { paragraph "Text Field Variables", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/variable.png" }
        section ("Environmental") {
            input "voiceTempVar", "capability.temperatureMeasurement", title: "Temperature Device Variable (%temp%)",multiple: true, required: false, submitOnChange: true
            input "voiceHumidVar", "capability.relativeHumidityMeasurement", title:"Humidity Device Variable (%humid%)",multiple: true, required: false, submitOnChange: true
            if ((voiceTempVar && voiceTempVar.size()>1) || (voiceHumidVar && voiceHumidVar.size()>1)) paragraph "Please note: When multiple temperature/humidity devices are selected above, the variable output will be an average of the device readings"
        }
        section ("People"){
        	input "voicePresenceVar", "capability.presenceSensor", title: "Presence Sensor Variable (%people%)", multiple: true, required: false
        }
    }
}
def pageConfirmation(){
	dynamicPage(name: "pageConfirmation", title: "Revoke/Reset Access Token Confirmation", uninstall: false){
        section {
		href "pageReset", title: "Revoke / Reset Access Token", description: "Tap to take action - READ WARNING BELOW", 
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/warning.png"
        	    paragraph "PLEASE CONFIRM! By resetting the access token you will disable the ability to interface this SmartApp with "+        	
        		"your Amazon Echo. You will need to copy the new access token to your Amazon Lambda code to re-enable access." +
                "Tap below to go back to the main menu with out resetting the token. You may also tap Done in the upper left corner."
        }
        section(" "){
        	href "mainPageParent", title: "Cancel And Go Back To Main Menu", description: " "
        }
	}
}
def pageReset(){
	dynamicPage(name: "pageReset", title: "Access Token Reset", uninstall: false){
        section{
			revokeAccessToken()
            state.accessToken = null
            OAuthToken()
            def msg = state.accessToken != null ? "New access token:\n${state.accessToken}\n\nClick 'Done' above to return to the previous menu." : "Could not reset Access Token. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
	    	paragraph "${msg}"
            href "mainPageParent", title: "Tap Here To Return To The Main Menu", description: " "
		}
	}
}
//Child Pages----------------------------------------------------
def mainPageChild(){
    dynamicPage(name: "mainPageChild", title: "Voice Macro Settings", install: true, uninstall: true) {
    	section {
            label title:"Voice Macro Name", required: true, image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speak.png"
            input "macroType", "enum", title: "Macro Type...", options: [["Control":"Control (Run/Execute)"],["CoRE":"CoRE Trigger (Run/Execute)"],["Group":"Device Group (On/Off/Toggle, Lock/Unlock, etc.)"],["GroupM":"Macro Group (Run/Execute)"],["Voice":"Voice Reporting (Run/Execute) "]], required: false, multiple: false, submitOnChange:true
            def fullMacroName=[GroupM: "Macro Group",CoRE:"CoRE Trigger", Control:"Control", Group:"Device Group", Voice:"Voice Reporting"][macroType] ?: macroType
            if (macroType) href "page${macroType}", title: "${fullMacroName} Settings", description: macroTypeDesc(), state: greyOutMacro()
        }
        if (macroType && macroType !="GroupM" && macroType !="Group"){
            section("Restrictions") {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false,
				image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/calendar.png"
				href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null),
					image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/clock.png"
				input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png"
				input "muteRestrictions", "bool", title: "Mute Restriction Messages In Macro Group", defaultValue: false
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
//Device Macro----------------------------------------------------
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
             if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after macro runs", required: false, capitalization: "sentences"
             input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true
        }
	}
}
//CoRe Macro----------------------------------------------------
def pageCoRE() {
	dynamicPage(name: "pageCoRE", install: false, uninstall: false) {
		section { paragraph "CoRE Trigger Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/CoRE.png" }
		section (" "){
   			input "CoREName", "enum", title: "Choose CoRE Piston", options: parent.listPistons(), required: false, multiple: false
        	input "cDelay", "number", title: "Default Delay (Minutes) To Trigger", defaultValue: 0, required: false
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after macro runs", required: false, capitalization: "sentences"
             input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true
        }
        if (!parent.listPistons()){
        	section("Missing CoRE Pistons"){
				paragraph "It looks like you don't have the CoRE SmartApp installed, or you haven't created any pistons yet. To use this capability, please install CoRE or, "+
                	"if already installed, create some pistons, then try again."
            }
        }	
    }
}
//Group Macro----------------------------------------------------
def pageGroupM() {
	dynamicPage(name: "pageGroupM", install: false, uninstall: false) {
		section { paragraph "Macro Group Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/macrofolder.png" }
        section (" ") { input "groupMacros", "enum", title: "Child Macros To Run (Control/CoRE/Voice Reports)...", options: parent.getMacroList(app.label), required: false, multiple: true }
        section("Acknowledgment options"){ 
            if (!noAck) input "voicePost", "text", title: "Custom Acknowledgment Message", description: "Enter a short statement to play after group macro runs", required: false, capitalization: "sentences"
            if (!noAck) input  "addPost", "bool", title: "Append Default/Custom Acknowledgment Message To Any Output Of Child Messages, Otherwise Replace Child Messages", defaultValue: false
            input "noAck", "bool", title: "No Acknowledgment Messages", defaultValue: false, submitOnChange: true
		}
	}
}
//Control Macro----------------------------------------------------
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
            input "smsMsg", "text", title: "Send This Message...", required: false, capitalization: "sentences"
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after macro runs", required: false, capitalization: "sentences"
             input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true
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
// Show Voice page----------------------------------------------------
def pageVoice() {
	dynamicPage(name: "pageVoice", install: false, uninstall: false) {
        section { paragraph "Voice Reporting Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/voice.png" }
        section (" ") {
            input "voicePre", "text", title: "Pre Message Before Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false, capitalization: "sentences"
            href "pageSwitchReport", title: "Switch/Dimmer Report", description: reportSwitches(), state: (voiceSwitch || voiceDimmer ? "complete" : null),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/power.png"
            href "pageDoorReport", title: "Door/Window/Lock Report", description: reportDoors(), state: (voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? "complete": null),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/lock.png"
            href "pageTempReport", title: "Temperature/Humidity/Thermostat Report", description: reportTemp(), state:(voiceTemperature || voiceTempSettings || voiceHumidity? "complete" : null),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/temp.png"
            href "pageSpeakerReport", title: "Speaker Report", description: speakerDesc(), state: (voiceSpeaker ? "complete": null),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speaker.png"
			href "pageWeatherReport", title: "Weather Report", description: weatherDesc(), state: greyOutWeather(),
            	image : "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/weather.png"
            href "pageOtherReport", title: "Other Sensors Report", description: reportSensors(), state: (voiceWater|| voiceMotion|| voicePresence|| voicePower  ? "complete" :null),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/sensor.png"
            href "pageHomeReport", title: "Mode and Smart Home Monitor Report", description: reportDescMSHM(), state: (voiceMode|| voiceSHM? "complete": null),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png"
            href "pageBatteryReport",title: "Battery Report", description: batteryDesc(), state: (voiceBattery ? "complete" : null),
            	image:"https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/battery.png"
            input "voicePost", "text", title: "Post Message After Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false, capitalization: "sentences"
        	input "allowNullRpt", "bool", title: "Allow For Empty Report (For Group Macros)", defaultValue: false
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
        section("Power Meters"){
       		input "voicePower", "capability.powerMeter", title: "Power Meters To Report Energy Use...", multiple: true, required: false
            if (voicePower) input "voicePowerOn", "bool", title: "Report Only Meters Drawing Power", defaultValue: false
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
            	input "voiceTempSettingsType", "enum", title: "Which Setpoint To Report", defaultValue: "heatingSetpoint", submitOnChange:true, 
                	options: ["autoAll":"Cooling & Heating Setpoints (must be in a compatible mode to read both values)","coolingSetpoint":"Cooling Setpoint Only","heatingSetpoint": "Heating Setpoint Only","thermostatSetpoint":"Single Setpoint (Not compatible with all thermostats)"]
            	if (voiceTempSettingsType !="autoAll") input "voiceTempSettingSummary", "bool", title: "Report Only Thermostats Not At Desired Setpoint", defaultValue: false, submitOnChange:true
            }
            if (voiceTempSettingSummary && voiceTempSettings && voiceTempSettingsType !="autoAll") input "voiceTempTarget", "number", title: "Thermostat Setpoint Target", required: false, defaultValue: 50
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
        	paragraph "There may be up to a 5 minute delay before SmartThings refreshes the current speaker status. This may cause the report to produce " +
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
        section("Weather Reporting") {
        	 href "pageWeatherCurrent", title: "Current Weather Report Options", description: none, state: (voiceWeatherTemp || voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ? "complete" : null)	
             href "pageWeatherForecast", title: "Weather Forecast Options", description: none, state: (voiceWeatherToday||voiceWeatherTonight||voiceWeatherTomorrow ? "complete" : null)	
        }
        section ("Sunrise/Sunset"){    
            input "voiceSunrise", "bool", title: "Speak Today's Sunrise", defaultValue: false
    		input "voiceSunset", "bool", title: "Speak Today's Sunset", defaultValue: false	
        }
        section ("Other Weather Underground Information"){
        	input "voiceMoon", "bool", title: "Lunar Phases", defaultValue:false
            if (voiceWeatherTemp || voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ||
            	voiceWeatherToday||voiceWeatherTonight||voiceWeatherTomorrow) input "voiceWeatherWarnFull", "bool", title: "Give Full Weather Advisories (If Present)", defaultValue: false
        }
        section ("Location") {
        	if (voiceWeatherTemp || voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ||
            voiceWeatherToday||voiceWeatherTonight||voiceWeatherTomorrow) input "voiceWeatherLoc", "bool", title: "Speak Location Of Weather Report/Forecast", defaultValue: false
            input "zipCode", "text", title: "Zip Code", required: false
            paragraph "Please Note:\nYour SmartThings location is currently set to: ${location.zipCode}. If you leave "+
            	"the area above blank the report will use your SmartThings location. Enter a zip code above if you "+
                "want to report on a different location.\n\nData obtained from Weather Underground."
		}
    }
}
def pageWeatherForecast(){
	dynamicPage(name: "pageWeatherForecast", install: false, uninstall: false) {
        section ("Weather Forecast Options") {
        	input "voiceWeatherToday", "bool", title: "Speak Today's Weather Forecast", defaultValue: false
            input "voiceWeatherTonight", "bool", title: "Speak Tonight's Weather Forecast", defaultValue: false
            input "voiceWeatherTomorrow", "bool", title: "Speak Tomorrow's Weather Forecast", defaultValue: false
        }
    }
}
def pageWeatherCurrent(){
	dynamicPage(name: "pageWeatherCurrent", install: false, uninstall: false) {
        section ("Items to include in current weather report") {
            input "voiceWeatherTemp", "bool", title: "Temperature (With Conditions)", defaultValue: false
            input "voiceWeatherHumid", "bool", title: "Humidity (With Winds And Pressure)", defaultValue: false
            input "voiceWeatherDew", "bool", title: "Dew Point", defaultValue: false
            input "voiceWeatherSolar", "bool", title: "Solar and UV Radiation", defaultValue: false
            input "voiceWeatherVisiblity", "bool", title: "Visibility", defaultValue: false
            input "voiceWeatherPrecip", "bool", title: "Precipitation", defaultValue: false
        }
    }
}
//---------------------------------------------------------------
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}
def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}
def childUninstalled() {
	sendLocationEvent(name: "askAlexa", value: "refresh", data: [macros: parent ? parent.getCoREMacroList() : getCoREMacroList()] , isStateChange: true, descriptionText: "Ask Alexa macro list refresh")
}
def initialize() {
	if (!parent){
        if (!state.accessToken) log.error "Access token not defined. Ensure OAuth is enabled in the SmartThings IDE."
        fillColorSettings()
        fillTypeList()
        subscribe(location, "CoRE", coreHandler)
	}
    else{
    	unschedule()
    	state.scheduled=false 
    }
	sendLocationEvent(name: "askAlexa", value: "refresh", data: [macros: parent ? parent.getCoREMacroList() : getCoREMacroList()] , isStateChange: true, descriptionText: "Ask Alexa macro list refresh")
}
//--------------------------------------------------------------
mappings {
      path("/d") { action: [GET: "processDevice"] }
      path("/m") { action: [GET: "processMacro"] }
      path("/h") { action: [GET: "processSmartHome"] }
      path("/l") { action: [GET: "processList"] }
      path("/v") { action: [GET: "processVersion"] }
      path("/setup") { action: [GET: "displayData"] }
}
//--------------------------------------------------------------
def processDevice() {    
    log.debug "Device command received with params $params"
	def dev = params.Device 	//Label of device
	def op = params.Operator	//Operation to perform
    def numVal = params.Num     //Number for dimmer/PIN type settings
    def param = params.Param	//Other parameter (color)
    def lVer = params.lVer		//Version number of Lambda code 
    log.debug "Dev: " + dev
    log.debug "Op: " + op
    log.debug "Num: " + numVal
    log.debug "Param: " + param
    log.debug "Lambda Ver: " + lVer
	def num = numVal == "undefined" ? 0 : numVal as int
    String outputTxt = ""
    def deviceList, count = 0
    getDeviceList().each{if (it.name==dev.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()) {deviceList=it; count++}}
	if (count > 1) outputTxt ="The device named '${dev}' is used multiple times in your SmartThings SmartApp. Please rename or remove duplicate items so I may properly utlize them. "   
    else if (deviceList) {
		if (num == 0 && numValue=="0")  outputTxt = getReply (deviceList.devices,deviceList.type, dev.toLowerCase(), op, num, param) 
        else if (op == "status" || (op=="undefined" && param=="undefined" && num==0 && numVal=="undefined")) outputTxt = getReply (deviceList.devices,deviceList.type, dev.toLowerCase(), "status", "", "") 
		else if (op == "events" || op == "event") {	
            def finalCount = num != 0 ? num as int : eventCt ? eventCt as int : 0
            if (finalCount>0 && finalCount < 10) outputTxt = getLastEvent(deviceList.devices?.find{it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() == dev.toLowerCase()}, finalCount) + "%3%"
            else if (!finalCount) { outputTxt = "You do not have the number of events you wish to hear specified in your Ask Alexa SmartApp, and you didn't specify a number in your request. %1%" }
        	else if (finalCount > 9) { outputTxt = "The maximum number of past events to list is nine. %1%" }
        }
        else outputTxt = getReply (deviceList.devices,deviceList.type, dev.toLowerCase(), op, num, param)
    }
    if (!count) { outputTxt = "I had some problems finding the device you specified. %1%" }
    sendJSON(outputTxt, lVer)
}
//List Request
def processList(){
	log.debug "List command received with params $params"
	def listType = params.Type	//Help type
    def lVer = params.lVer		//Version number of Lambda code
    log.debug "List Type: " + listType
    log.debug "Lambda Ver: " + lVer
    String outputTxt = ""
	if (listType=="mode" || listType=="modes" ){
		outputTxt = location.modes.size() >1 ? "The available modes include the following: " + getList(location.modes) + ". " : location.modes.size()==1 ? "You have one mode available named " + getList(location.modes) + ". " : "There are no modes defined within your SmartThings' account. "
	}
    if (listType=="security" || listType=="smart home monitor" || listType=="SHM") outputTxt = "The valid Smart Home Monitor commands are: 'disarm', 'away' or 'stay'. "
    if (listType=="temperature" || listType=="temperature sensors"){
    	if (temps) outputTxt = "The devices you can get temperature readings from include the following: " + getList(temps) + ". "
        if (tstats && temps) outputTxt +="In addition, the following thermostats can also give you temperature readings: " +  getList(tstats) + ". "
        if (tstats && tstats.size()>1 && !temps) outputTxt ="The only devices you have selected for temperature readings are the following thermostats: " +  getList(tstats) + ". "
        if (tstats && tstats.size()==1 && !temps) outputTxt ="The only device you have selected for temperature readings is the " +  getList(tstats) + ". "
        if (!tstats && !temps) outputTxt="You don't have any devices selected that will provide temperature readings. "
    }
    if (listType=="thermostats" || listType=="thermostat"){
     	outputTxt = tstats && tstats.size()>1 ? "The available thermostats are as follows: " +  getList(tstats) + ". "
        	: tstats && tstats.size()==1 ? "The only available thermostat is the " +  getList(tstats) + ". "
        		: "You don't have any thermostats defined within your Ask Alexa SmartApp. "
    }
    if (listType=="humidity"){
     	outputTxt = humid && humid.size()>1 ? "The available humidity sensors include the following: " +  getList(humid) + ". "  
        	: humid && humid.size()==1 ? "The "+ getList(humid)+ " is the only humidity sensor available. "    
       			: "You don't have any humidity sensors selected in your Ask Alexa SmartApp. "
    }
    if (listType=="presence" || listType=="presence sensor" || listType=="presence sensors"){
     	outputTxt = presence && presence.size()>1 ? "The available presence sensors are: " +  getList(presence) + ". " 
        	: presence && presence.size()==1 ? "The "+ getList(presence)+ " is the only presence sensor available. "    
       			: "You don't have any presence sensors selected in your Ask Alexa SmartApp. "
    }
    if (listType=="motion" || listType=="motion sensor" || listType=="motion sensors"){
     	outputTxt = motion && motion.size()>1 ? "The available motion sensors are: " +  getList(motion) + ". "
        	: motion && motion.size()==1 ? "The "+ getList(motion)+ " is the only motion sensor available. "    
       			: "You don't have any motion sensors selected in your Ask Alexa SmartApp. "
    }
    if (listType=="door sensor" || listType=="window sensor" || listType=="door sensors" || listType=="window sensors" || listType=="open close sensors"){
     	outputTxt = ocSensors && ocSensors.size()>1 ? "The available open close sensors are: " +  getList(ocSensors) + ". " 
        	: ocSensors && ocSensors.size()==1 ? "The "+ getList(ocSensors)+ " is the only open close sensor available. "    
       			: "You don't have any open close sensors selected in your Ask Alexa SmartApp. "
    }
    if (listType=="dimmer" || listType=="dimmers"){
     	outputTxt = dimmers && dimmers.size()>1 ? "You have the following dimmers selected in your SmartApp: " +  getList(dimmers) + ". "
        	: dimmers && dimmers.size()==1 ? "You only have one dimmer selected in your SmartApp named " +  getList(dimmers) + ". "
            	: "You don't have any dimmers selected in the Ask Alexa SmartApp. "
    }
    if (listType=="speakers" || listType=="speaker"){
     	outputTxt = speakers && speakers.size()>1 ? "You can control following speakers: " +  getList(speakers) + ". "
        	: speakers && speakers.size()==1 ? "The " + getList(speakers) + " is the only speaker you can control. "
            	:"You don't have any speakers selected in the Ask Alexa SmartApp. "
    }
    if (listType=="doors" || listType=="door"){
     	outputTxt = doors && doors.size()>1 ? "You have the following doors you can open or close: " +  getList(doors) + ". "
        	: doors && doors.size()==1 ? "You have one door, " + getList(doors)+ ", selected that you can open or close."
        		: "You don't have any doors selected in your Ask Alexa SmartApp. "	
    }
    if (listType=="locks" || listType=="lock" ){
     	outputTxt = locks && locks.size()>1 ? "You have the following locks you can lock or unlock: " +  getList(locks) + ". "
        	: locks && locks.size()==1 ? "You have one lock named '" + getList(locks)+ "' that you can control. "
        		:"You don't have any locks selected in your Ask Alexa SmartApp. "	
    }
    if (listType=="colored lights" || listType =="colored light"){
     	outputTxt = cLights && cLights.size()>1 ? "You have the following colored lights you can control: " +  getList(cLights) + ". "
        	: cLights && cLights.size()==1 ? "You have one colored light you can control named '" +  getList(cLights) + "'. "	
            	: "You don't have any colored lights selected within your Ask Alexa SmartApp. "
    }
    if (listType=="switch" || listType=="switches") outputTxt = switches? "You can turn on, off or toggle the following switches: " +  getList(switches) + ". " : "You don't have any switches defined selected your Ask Alexa SmartApp. "	
    if (listType=="routine" || listType=="routines" ){
		def phrases = location.helloHome?.getPhrases()*.label
		outputTxt= phrases.size() >0 ? "The available routines include the following: " + getList(phrases) + ". " : "There are no routines set up within your Ask Alexa SmartApp. "
	}
    if (listType=="water" || listType=="water sensor" || listType=="water sensors" ){
		outputTxt= water && water.size()>1 ? "The available water sensors include the following: " + getList(water) + ". "
        	: water && water.size()==1 ? "The only available water sensor is the " + getList(water)  + ". "
        		: "There are no water sensors set up within your Ask Alexa SmartApp. "
	}
    if (listType =="colors" || listType =="color") outputTxt = cLights ? "The available colors to use with colored lights include: " + getList(state.colorData.name) + ". " : "You don't have any colored lights selected within your Ask Alexa SmartApp. "
    if (listType == "report" || listType == "reports" || listType == "voice report" || listType == "voice reports")  outputTxt = parseMacroLists("Voice","voice report","play")
    if (listType == "device group" || listType == "device groups" || listType == "device macros" || listType == "device macro" || listType == "device group macro" || listType == "device group macros") {
        outputTxt = parseMacroLists("Group","device group","control")
	}
    if (listType == "control" ||  listType == "controls" || listType == "control macro" || listType == "control macros") outputTxt = parseMacroLists("Control","control macro","run")
    if (listType =="core" || listType =="core trigger" || listType =="core triggers" || listType =="core macro" || listType =="core macros") outputTxt = parseMacroLists("CoRE","core trigger","run")
    if (listType == "macro group" || listType == "macro groups") outputTxt = parseMacroLists("GroupM","macro group","run")
    if (listType=="events") { outputTxt = "To list events, you must give me a device name to query. For example, you could say, 'tell ${invocationName} to give me the last events for "+ 
        "the Bedroom'. You may also include the number of events you would like to hear. An example would be, 'tell ${invocationName} to give me the last 4 events for " +
        "the Bedroom'. "
    }
    if (listType == "group" || listType == "groups" || listType == "macro" || listType == "macros") outputTxt ="Please be a bit more specific about which group or macros you want me to list. You can ask me about 'macro groups', 'device groups', 'control macros' and 'voice reports'.  "
    if (listType == "sensor" || listType == "sensors") outputTxt ="Please be a bit more specific about what kind of sensors you want me to list. You can ask me to list items like 'water sensors', 'door sensors', 'presence sensors' or 'motion sensors'. "
    
    if (outputTxt == "") { 
    	outputTxt = "I didn't understand what you wanted information about. Be sure you have properly run the setup and populated the developer section with the device names and try again. %1%"
    }
    else outputTxt += "%2%"
    sendJSON(outputTxt, lVer)
}
def parseMacroLists(type, noun, action){
    def macName = "", count = 0
	childApps.each{if (it.macroType==type) count++}
    def extraTxt = (type == "Control" || type=="CoRE") && count ? "Please note: You can also delay the execution ${noun}s by adding a time, in minutes, after the name. For example,  " +
    	"you could say, 'tell ${invocationName} to run the Macro in 5 minutes'. " : ""
	macName = count==1 ? "You only have one ${noun} called: " : count> 1 ? "You can ask me to ${action} the following ${noun}s: " : "You don't have any ${noun}s for me to ${action}"
	if (count){
		childApps.each{if (it.macroType==type){
			macName += it.label ; count= count-1
			macName += count>1 ? ", " : count==1 ? " and " : ""
			}	
		}
	}
	return macName + ". " + extraTxt
}
//Macro Group
def processMacroGroup(macroList, msg, append, noMsg, macLabel){
    def result = "", runCount=0
    if (macroList){ 
        macroList.each{
            childApps.each{child->
                if (child.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){ 
                    result += child.getOkToRun() ? child.macroResults(0,"","","")  : child.muteRestrictions ? "" : "You have restrictions on '${child.label}' that prevented it from running. "             
                    runCount++
                }
            }
        }
        def extraTxt = runCount > 1 ? "macros" : "macro"
        if (runCount == macroList.size()) {
            if (!noMsg) {
                if (msg && append) result += msg
                if (msg && !append)  result = msg
                if (append && !msg) result += "I ran ${runCount} ${extraTxt} in this macro group. "
                if (!append && !msg) result = "I ran ${runCount} ${extraTxt} in this macro group. "
        	}
            else result = " "
        }
        else result = "There was a problem running one or more of the macros in the macro group. "
    }
    else result="There were no macros present within this macro group. Please check your Ask Alexa SmartApp and try again. "
    def data = [alexaOutput: result, num: "undefined", cmd: "undefined", color: "undefined", param: "undefined"]
	sendLocationEvent(name: "askAlexaMacro", value: macLabel, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa ran '${macLabel}' macro group.")	
    return result
}
//Macro Processing
def processMacro() {
    log.debug "Macro command received with params $params"
	def mac = params.Macro 		//Macro name
    def mNum = params.Num		//Number variable-Typically delay to run
    def cmd = params.Cmd		//Group Command
    def param = params.Param	//Parameter
    def lVer = params.lVer		//Version number of Lambda code
    log.debug "Macro Name: " + mac
    log.debug "mNum: " + mNum
    log.debug "Cmd: " + cmd
    log.debug "Param: " + param
    log.debug "Lambda Ver: " + lVer
    if (mNum == "0" && cmd=="undefined" && param == "undefined") cmd="off"
    def num = mNum == "undefined" ? 0 : mNum as int
    String outputTxt = ""
    def count = 0, macroType="", fullMacroName, colorData, err=false
    if (cmd == "low" || cmd=="medium" || cmd=="high"){
        if (cmd=="low" && dimmerLow) num = dimmerLow else if (cmd=="low" && !dimmerLow) err=true 
		if (cmd=="medium" && dimmerMed) num = dimmerMed else if (cmd=="medium" && !dimmerMed) err=true 
		if (cmd=="high" && dimmerHigh) num = dimmerHigh else if (cmd=="high" &&!dimmerhigh) err=true
        if (err) outputTxt = "You don't have a default value set up for the ${outputTxt} level. I am not making any adjustments. "
    }
    def getColorData = state.colorData.find {it.name.toLowerCase()==param}
    if (getColorData){
        def hueColor = getColorData.hue, satLevel = getColorData.sat
        colorData = [hue: hueColor as int, saturation: satLevel as int, level: num] 
    }   	
    if (childApps.size()){ childApps.each {child ->
            if (child.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")== mac.toLowerCase()) count++
    	}
    }
    if ((cmd == "lock" || cmd == "unlock" || cmd == "close" || cmd == "open") && pwNeeded) param = password as int
    if (!err){
        if (count == 1){
            childApps.each {child -> 
                if (child.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac.toLowerCase()) {         
                    fullMacroName = [GroupM: "Macro Group",CoRE: "CoRE Trigger", Control:"Control Macro", Group:"Device Group", Voice:"Voice Report"][child.macroType] ?: child.macroType
                    if (child.macroType != "GroupM") outputTxt = child.getOkToRun() ? child.macroResults(num, cmd, colorData, param) : "You have restrictions within the ${fullMacroName} named, '${child.label}', that prevent it from running. Check your settings and try again. "
                    else outputTxt = processMacroGroup(child.groupMacros, child.voicePost, child.addPost, child.noAck, child.label)
                }
            }
        }
        if (count > 1) outputTxt ="You have duplicate macros named '${mac}'. Please check your SmartApp and try again. "
        if (!count) { outputTxt = "I could not find a macro named '${mac}'. %1%" }
    }
    if (!outputTxt.endsWith("%")) outputTxt += "%4%"
    sendJSON(outputTxt, lVer)
}
//Smart Home Commands
def processSmartHome() {
    log.debug "Smart home command received with params $params"
	def cmd = params.SHCmd 						//Smart Home Command
	def param = params.SHParam.toLowerCase()	//Smart Home Parameter
	def lVer = params.lVer						//Version number of Lambda code
    log.debug "Cmd: " + cmd
    log.debug "Param: " + param
    log.debug "Lambda Ver: " + lVer
    String outputTxt = ""
    if (cmd =="undefined") {
    	if (param=="off") { outputTxt="Be sure to specify a device, or the word 'security', when using the 'off' command. %1%" }
        if (location.modes?.find{it.name.toLowerCase()==param} && param != currMode) cmd = "mode"
    	if (param=="list" || param=="arm" || param=="undefined") cmd = "security"
        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases.find{it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()==param}) cmd = "routine"
    }
    if (cmd == "mode"){	
            def currMode = location.mode.toLowerCase()
            if (param == "undefined") outputTxt ="The current SmartThings mode is set to, '${currMode}'. "
            if (modes && param !="undefined"){
                if (location.modes?.find{it.name.toLowerCase()==param} && param != currMode) {
                    def newMode=location.modes?.find{it.name.toLowerCase()==param}
                    outputTxt ="I am setting the SmartThings mode to, '${newMode}'. "
                    setLocationMode(newMode)
                }
                else if (param == currMode) outputTxt ="The current SmartThings mode is already set to '${currMode}'. No changes are being made. "
                if (!outputTxt) outputTxt = "I did not understand the mode you wanted to set. For a list of available modes, simply say, 'ask ${invocationName} for mode list'. %1%"
            }
			else if (!outputTxt) outputTxt = "You can not change your mode because you do not have this option enabled within your SmartApp. Please enable this and try again. %1%" 
    }
    if (cmd=="security" || cmd=="smart home" || cmd=="smart home monitor" || cmd=="SHM" ){
    	def SHMstatus = location.currentState("alarmSystemStatus")?.value
		def SHMFullStat = [off : "disarmed", away: "armed away", stay: "armed stay"][SHMstatus] ?: SHMstatus
        def newSHM = "", SHMNewStat = "" 
        if (param=="undefined") outputTxt ="The Smart Home Monitor is currently set to, '${SHMFullStat}'. "
        if (SHM){ 
            if (param=="arm") outputTxt ="I did not understand how you want me to arm the Smart Home Monitor. Be sure to say, 'armed stay' or 'armed away', to properly change the setting. %1%"   
            if (param =="off" || param =="disarm") newSHM="off"
            if (param =="away" || param =="armed away") newSHM="away"
            if (param =="stay" || param =="armed stay") newSHM="stay"
    		if (newSHM && SHMstatus!=newSHM) {
        		sendLocationEvent(name: "alarmSystemStatus", value: newSHM)
        		SHMNewStat = [off : "disarmed", away: "armed away", stay: "armed stay"][newSHM] ?: newSHM
            	outputTxt ="I am setting the Smart Home monitor to, '${SHMNewStat}'. "
        	}
            else if (SHMstatus==newSHM) outputTxt ="The Smart Home Monitor is already set to '${SHMFullStat}'. No changes are being made. " 
       	}
        else if (!outputTxt){
        	outputTxt = "You can not change your Smart Home Monitor settings because you do not have this option enabled within your SmartApp. Please enable this and try again. "
    	}
    }
    if (cmd=="routine" && routines){
    	def phrases = location.helloHome?.getPhrases()*.label
        def runRoutine = phrases.find{it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()==param}
        if (runRoutine) {
        	location.helloHome?.execute(runRoutine)
            outputTxt="I am executing the '${param}' routine. "
        }
        if (!outputTxt) outputTxt ="To run SmartThings' routines, ask me to run the routine by its full name. For a list of available routines, simply say, 'ask ${invocationName} to list routines'. %1%"
        else if (!routines) outputTxt = "You can not run SmartThings Routines because you do not have this option enabled in the Ask Alexa SmartApp. Please enable this feature and try again. "
    }
    if (!outputTxt) outputTxt = "I didn't understand what you wanted me to do. %1%" 
    if (outputTxt && !outputTxt.endsWith("%")) outputTxt +="%2%"
    sendJSON(outputTxt, lVer)
}
def getReply(devices, type, dev, op, num, param){
	String result = ""
    log.debug "Type: " + type
    try {
    	def STdevice = devices?.find{it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() == dev}
        def supportedCaps = STdevice.capabilities
		def isMetric = location.temperatureScale == "C"
        if (op=="status") {
            if (type == "temperature"){
                def temp= STdevice.currentValue(type)
            	if (isMetric){
                	String t = temp as String
            		if (t.endsWith(".0")) t = t - ".0"
					temp=t.toFloat()
     			}
                else temp = Math.round(temp)
                result = "The temperature of the ${STdevice} is ${temp} degrees"
                if (otherStatus) {
                    def humidity = STdevice.currentValue("humidity"), wet=STdevice.currentValue("water")
                    result += humidity ? ", and the relative humidity is ${humidity}%. " : ". "
                    result += wet ? "Also, this device is a leak sensor, and it is currently ${wet}. " : ""
                }
            }
            else if (type == "presence") result = "The presence sensor, ${STdevice}, is showing ${STdevice.currentValue(type)}. "
            else if (type =="motion"){
            	def currVal =STdevice.currentValue(type)
                def motionStat=[active : "movement", inactive: "no movement"][currVal] ?: currVal
                result = "The motion sensor, ${STdevice}, is currently reading '${motionStat}'. "
            }
            else if (type == "humidity"){
                result = "The relative humidity at the ${STdevice} is ${STdevice.currentValue(type)}%"
                if (otherStatus) {
                    def temp =STdevice.currentValue("temperature")
                    if (isMetric){
                		String t = temp as String
            			if (t.endsWith(".0")) t = t - ".0"
						temp=t.toFloat()
     				}
                	else temp = Math.round(temp)
                    result += temp ? ", and the temperature is ${temp} degrees." : ". "
				}
            }
            else if (type == "level" || type=="color" || type == "switch") {
                def onOffStatus = STdevice.currentValue("switch")
                result = "The ${STdevice} is ${onOffStatus}"
                if (otherStatus) { 
                	def level = STdevice.currentValue("level"), power = STdevice.currentValue("power")
                    result += onOffStatus == "on" && level ? ", and it's set to ${level}%" : ""
                    result += onOffStatus=="on" && power > 0 ? ", and is currently drawing ${power} watts of power. " : ". "
            	}
            }
            else if (type == "thermostat"){
                def temp = STdevice.currentValue("temperature")
                if (isMetric){
                	String t = temp as String
            		if (t.endsWith(".0")) t = t - ".0"
					temp=t.toFloat()
     			}
                else temp = Math.round(temp)
                result = "The ${STdevice} temperature reading is currently ${temp} degrees"
                if (otherStatus){
                    def humidity = STdevice.currentValue("humidity"), opState = STdevice.currentValue("thermostatMode")
                    def heat = opState=="heat" || opState =="auto" ? STdevice.currentValue("heatingSetpoint") : ""
                    if (heat && isMetric) {
                    	String h = heat as String
                    	if (h.endsWith(".0")) t = t - ".0"
                        heat=h.toFloat()
                    }
                    else if (heat) heat = Math.round(heat)
                    def cool = opState=="cool" || opState =="auto" ?  STdevice.currentValue("coolingSetpoint") : "" 
                    if (cool && isMetric) {
                    	String c = heat as String
                    	if (c.endsWith(".0")) t = t - ".0"
                        cool=c.toFloat()
                    }
                    else if (cool) cool = Math.round(cool)
                    result += opState ? ", and the thermostat's mode is: '${opState}'." : ". "
                    result += humidity ? " The relative humidity reading is ${humidity}%." : ""
                    if (nestCMD && supportedCaps.name.contains("Presence Sensor")){
                    	result += " This thermostat's presence sensor is reading "
                        result += STdevice.currentValue("presence")=="present" ? "'Home'. " : "'Away'. "
                    }
                    result += heat ? " The heating setpoint is set to ${heat} degrees. " : ""
                    result += heat && cool ? "And finally, " : ""
                    result += cool ? " The cooling setpoint is set to ${cool} degrees. " : ""
            	}           
            }
            else if (type == "contact") result = "The ${STdevice} is currently ${STdevice.currentValue(type)}. "
            else if (type == "music"){
                def onOffStatus = STdevice.currentValue("status"), track = STdevice.currentValue("trackDescription"), level = STdevice.currentValue("level")
                def mute = STdevice.currentValue("mute")
                result = "The ${STdevice} is currently ${onOffStatus}"
                result += onOffStatus =="stopped" ? ". " : onOffStatus=="playing" && track ? ": '${track}'" : ""
                result += onOffStatus == "playing" && level && mute =="unmuted" ? ", and it's volume is set to ${level}%. " : mute =="muted" ? ", and it's currently muted. " :""
            }
            else if (type == "water") result = "The water sensor, ${STdevice}, is currently ${STdevice.currentValue(type)}. "
            else result = "The ${STdevice} is currently ${STdevice.currentValue(type)}. "
        }
        else {
            if (type == "thermostat"){
                if ((op == "increase" || op=="raise" || op=="up" || op == "decrease" || op=="down" || op=="lower")){
                     def newValues = upDown(STdevice, type, op, num)  
                     num = newValues.newLevel
                }
                if (num>0) num = num <= tstatHighLimit && num >= tstatLowLimit ? num : num > tstatHighLimit ? tstatHighLimit : tstatLowLimit 
                if ((param=="heat" || param=="heating" || param =="cool" || param=="cooling" || param =="auto" || param=="automatic" || param=="eco" || param=="comfort" || param=="home" || param=="away") && num == 0 && op=="undefined") op="on"
                if (op == "on" || op=="off") {
                	if (param == "undefined" && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning the ${STdevice} on. "
                    if (param =="heat" || param=="heating") {result="I am setting the ${STdevice} to 'heating' mode. "; STdevice.heat()}
                    if (param =="cool" || param=="cooling") {result="I am setting the ${STdevice} to 'cooling' mode. "; STdevice.cool()}
                    if (param =="auto" || param=="automatic") {result="I am setting the ${STdevice} to 'auto' mode. Please note, "+
                    	"to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints separately. " ; STdevice.auto()}
                    if (param =="home" && nestCMD) {result = "I am setting the ${STdevice} to 'home'. "; STdevice.present()} 
                    if (param =="away" && nestCMD) {result = "I am setting the ${STdevice} to 'away'. Please note that Nest thermostats will not temperature changes while in 'away' status. "; STdevice.away()} 
                    if (op =="off") {result = "I am turning the ${STdevice} ${op}. "; STdevice.off()}
                    if (stelproCMD && param=="eco"){ result="I am setting the ${STdevice} to 'eco' mode. "; STdevice.setThermostatMode("eco") }
                    if (stelproCMD && param=="comfort"){ result="I am setting the ${STdevice} to 'comfort' mode. "; STdevice.setThermostatMode("comfort") }
                }
                else {
                    if (param == "undefined"){ 
                        if (STdevice.currentValue("thermostatMode")=="heat") param = "heat"
                        else if (STdevice.currentValue("thermostatMode")=="cool") param = "cool"
                        else result = "You must designate a 'heating' or 'cooling' parameter when setting the temperature. "+
                            "The thermostat will not accept a generic setpoint in its current mode. For example, you could simply say, 'ask ${invocationName} to set the ${STdevice} heating to 65 degrees'. "
					}
                    if ((param =="heat" || param =="heating") && num > 0) {
                        result="I am setting the heating setpoint of the ${STdevice} to ${num} degrees. "
                        STdevice.setHeatingSetpoint(num) 
                        if (stelproCMD) STdevice.applyNow()
                    }
                    if ((param =="cool" || param =="cooling") && num > 0) {
                        result="I am setting the cooling setpoint of the ${STdevice} to ${num} degrees. "
                        STdevice.setCoolingSetpoint(num)
                    }
                    if (num == tstatHighLimit) result += "This is the maximum temperature I can set for this device. "
                    if (num == tstatLowLimit) result += "This is the minimum temperature I can set for this device. "
                }
            }
            if (type == "color" || type == "level" || type=="switch"){
                num = num < 0 ? 0 : num >99 ? 100 : num
                def overRideMsg = "" 
                if (op == "maximum") num = 100
                if ((op == "increase" || op=="raise" || op=="up" || op == "decrease" || op=="down" || op=="lower") && (type == "color" || type == "level")){ 
                     def newValues = upDown(STdevice, type, op, num)
                     num = newValues.newLevel
                     op= num > 0 ? "on" : "off"
                     overRideMsg = newValues.msg
                }
                if (op == "low" || op=="medium" || op=="high" && (type == "color" || type == "level")){
                	if (op=="low" && dimmerLow) num = dimmerLow else if (op=="low" && dimmerLow=="") num =0 
                    if (op=="medium" && dimmerMed) num = dimmerMed else if (op=="medium" && !dimmerMed) num = 0 
                    if (op=="high" && dimmerHigh) num = dimmerHigh else if (op=="high" && !dimmerhigh) num = 0 
                    if (num>0) overRideMsg = "I am turning the ${STdevice} to ${op}, or a value of ${num}%. "
                    if (num==0) overRideMsg = "You don't have a default value set up for the '${op}' level. I am not making any changes to the ${STdevice}. "
                }
                if ((type == "switch") || ((type=="level" || type == "color") && num==0 )){
                    if ((type=="level" || type == "color") && num==0 && op=="undefined" && param=="undefined") op="off"
                	if (op=="on" || op=="off"){
                		STdevice."$op"() 
                        result = overRideMsg ? overRideMsg: "I am turning the ${STdevice} ${op}. "
                    }
                    if (op=="toggle") {
        				def oldstate = STdevice.currentValue("switch")
                        def newstate = oldstate == "off" ? "on" : "off"
        				STdevice."$newstate"()
                        result = "I am toggling the ${STdevice} from '${oldstate}' to '${newstate}'. "
                    }
            	}
                if ((type == "color" || type == "level") && num > 0) {
                	STdevice.setLevel(num)
                    result = overRideMsg ? overRideMsg : num==100 ? "I am setting the ${STdevice} to its maximum value. " : "I am setting the ${STdevice} to ${num}%. "                    
				}
                if (type == "color" && param !="undefined" && supportedCaps.name.contains("Color Control")){
                    def getColorData = state.colorData.find {it.name.toLowerCase()==param}
                    if (getColorData){
                        def hueColor = getColorData.hue, satLevel = getColorData.sat
                        def newLevel = num > 0 ? num : STdevice.currentValue("level")
                        def newValue = [hue: hueColor as int, saturation: satLevel as int, level: newLevel]
                        STdevice?.setColor(newValue)
                        result = "I am setting the color of the ${STdevice} to ${param}"
                        result += num>0 ? ", at a brightness level of ${num}%. " : ". "
                	}
                }
            	if (!result){
                	if (type=="switch") result = "For the ${STdevice} switch, be sure to give an 'on', 'off' or 'toggle' command. "
            		if (type=="level") result = overRideMsg ? overRideMsg: "For the ${STdevice} dimmer, be sure to use an 'on', 'off', 'toggle' command or brightness level setting. "
            		if (type=="color") result = overRideMsg ? overRideMsg: "For the ${STdevice} color controller, remember it can be operated like a switch. You can ask me to turn "+  
                    "it on, off, toggle the on and off states, or set a brightness level. You can also set it to a variety of common colors. For listing of these colors, simply "+
                    "say, 'tell SmartThings to list the colors'. "
                }
            }
            if (type == "music"){             
                if ((op == "increase" || op=="raise" || op=="up" || op == "decrease" || op=="down" || op=="lower")){
                     def newValues = upDown(STdevice, type, op, num) 
                     num = newValues.newLevel
                     if (num==0) op= "off"
                }
                if (num != 0 && num > speakerHighLimit) num = speakerHighLimit
                if (op=="off" || op=="stop") { STdevice.stop(); result = "I am turning off the ${STdevice}. " }
                else if (op == "play" || op=="on") { STdevice.play(); result = "I am playing the ${STdevice}. " }
                else if (op=="mute") { STdevice.mute(); result = "I am muting the ${STdevice}. " }
                else if (op=="unmute") { STdevice.unmute(); result = "I am unmuting the ${STdevice}. " }
                else if (op=="pause") { STdevice.pause(); result = "I am pausing the ${STdevice} speaker. " }
                else if (op=="next track") {  STdevice.nextTrack(); result = "I am playing the next track on the ${STdevice}. " }
            	else if (op=="previous track") { STdevice.previousTrack(); result = "I am playing the next track on the ${STdevice}. " }
                else result = "I didn't understand what you wanted me to do with the ${STdevice} speaker. "
                if (num > 0) { STdevice.setLevel(num); result = "I am setting the volume of the ${STdevice} to ${num}%. " }
                if (num == speakerHighLimit) result += "This is the maximum volume level you have set up. " 
            }
            if (type == "door"){
                def currentDoorState = STdevice.currentValue(type)
				if (currentDoorState==op || (currentDoorState == "closed" && op=="close")) result = "The ${STdevice} is already ${currentDoorState}. "
                else {
                    if (op != "open" || op != "close") result ="For the ${STdevice}, you must give an 'open' or 'close' command. "
                    if ((op=="open" || op=="close") && (pwNeeded && password && num == 0)) result="You must say your password to ${op} the ${STdevice}. "
                    if ((op=="open" || op=="close") && (pwNeeded && password && num>0 && num != password as int)) result="Sorry, I did not hear the correct password to ${op} the ${STdevice}. "
                    else if ((op=="open" || op=="close") && (!pwNeeded || (password && pwNeeded && num ==password as int) || !password)) {
                        STdevice."$op"() 
                        if (op == "close") op="clos"
                        result = "I am ${op}ing the ${STdevice}. "
                    }
             	}
			}
            if (type == "lock"){
                if (STdevice.currentValue("lock") == op+"ed") result = "The ${STdevice} is already ${op}ed. "
                else {
                    if (op != "lock" || op != "unlock" ) result= "For the ${STdevice}, you must give a 'lock' or 'unlock' command. "
                    if ((op=="lock" || op=="unlock") && (pwNeeded && password && num ==0)) result= "You must say your password to ${op} the ${STdevice}. "
                    if ((op=="lock" || op=="unlock") && (pwNeeded && password && num>0 && num != password as int)) result="Sorry, I did not hear the correct password to ${op} the ${STdevice}. "
                    else if ((op=="lock" || op=="unlock") && (!pwNeeded || (password && pwNeeded && num ==password as int) || !password)) {
                        STdevice."$op"()
                        result = "I am ${op}ing the ${STdevice}. "
                    }
                }
        	}
		}
        if (otherStatus && op=="status"){
            def temp = STdevice.currentValue("temperature"), accel=STdevice.currentValue("acceleration"), motion=STdevice.currentValue("motion"), lux =STdevice.currentValue("illuminance") 
            if (isMetric && temp){
                	String t = temp as String
            		if (t.endsWith(".0")) t = t - ".0"
					temp=t.toFloat()
     			}
			else if (temp) temp = Math.round(temp)
            result += lux ? "The illuminance at this device's location is ${lux} lux. " : ""
            result += temp && type != "thermostat" && type != "humidity" && type != "temperature" ? "In addition, the temperature reading from this device is ${temp} degrees. " : ""
			result += motion == "active" && type != "motion" ? "This device is also a motion sensor, and it is currently reading movement. " : ""
 			result += accel == "active" ? "This device has a vibration sensor, and it is currently reading movement. " : ""
        }
        if (STdevice.currentValue("battery") && batteryWarn){
			def battery = STdevice.currentValue("battery")
			def battThresLevel = batteryThres as int
			result += battery && battery < battThresLevel ? "Please note, the battery in this device is at ${battery}%. " : ""
		}
        if (op !="status" && !result && (type=="motion" || type=="presence" || type=="humidity" || type=="water" || type == "contact" || type == "temperature")){
        	result = "You attempted to take action on a device that can only give a status reading. %1%"
        }
	}
    catch (e){ result = "I could not process your request for the '${dev}'. Ensure you are using the correct commands with the device. %1%" }
    if (op=="status" && result && !result.endsWith("%")) result += "%2%"
    if (op!="status" && result && !result.endsWith("%")) result += "%3%"
    if (!result) result = "Sorry, I had a problem understanding your request. %1%"
    return result
}
//Version operation
def processVersion(){
	log.debug "Version command received with params $params"
	def ver = params.Ver 		//Lambda Code Verisons
    def lVer = params.lVer		//Version number of Lambda code
    def date = params.Date		//Version date of Lambda code
    state.lambdaCode = "Lambda Code Version: ${ver} (${date})"
   	String outputTxt = "The Ask Alexa SmartApp was developed by Michael Struck to intergrate the SmartThings platform with the Amazon Echo. "+
    	"The SmartApp version is: "  +  versionLong() + ". "
	outputTxt += "And the Amazon Lambda code version is: " + ver + ". %2%"
    sendJSON(outputTxt, lVer)
}
def setupData(){
	log.info "Set up web page located at : ${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}"
    def result ="<div style='padding:10px'><i><b>Lambda code variables:</b></i><br><br>var IName = '${invocationName}';<br>var STappID = '${app.id}';<br>var STtoken = '${state.accessToken}';<br>"
    result += "var url='${getApiServerUrl()}/api/smartapps/installations/' + STappID + '/' ;<br><br><hr>"
	result += "<i><b>Amazon ASK Custom Slot Information:</b></i><br><br><b>CANCEL_CMDS</b><br><br>cancel<br>stop<br>unschedule<br><br><b>DEVICE_TYPE_LIST</b><br><br>"
    state.deviceTypes.each{result += it + "<br>"}
    result += "<br><b>LIST_OF_DEVICES</b><br><br>"
    def deviceList = getDeviceList(), deviceNames = deviceList.name.unique()
    def duplicates = deviceList.name.findAll{deviceList.name.count(it)>1}.unique()
    if (deviceNames && duplicates.size()){ 
    	result += "<b>**NOTICE:</b>The following duplicate(s) are only listed once below in LIST_OF_DEVICES:<br><br>"
        duplicates.each{result +="* " + it +" *<br><br>"}
        result += "Be sure to have unique names for each device and only use each device once within the parent app.**<br><br>"
    }
    if (deviceNames) deviceNames.each{result += it + "<br>" }
	result += "<br><b>LIST_OF_OPERATORS</b><br><br>on<br>off<br>toggle<br>up<br>down<br>increase<br>decrease<br>lower<br>raise<br>" +
    	"status<br>events<br>event<br>low<br>medium<br>high<br>lock<br>unlock<br>open<br>close<br>maximum<br>"
    result += speakers ?"play<br>stop<br>pause<br>mute<br>unmute<br>next track<br>previous track<br>" : ""
    result += "<br><b>LIST_OF_PARAMS</b><br><br>"
	result += "heat<br>cool<br>heating<br>cooling<br>auto<br>automatic<br>"
    if (tstats && stelproCMD) result += "eco<br>comfort<br>"
    if (tstats && nestCMD) result += "home<br>away<br>"
    if (cLights || childApps.size()) { fillColorSettings(); state.colorData.each {result += it.name.toLowerCase()+"<br>"}}
    result +="<br><b>LIST_OF_SHPARAM</b><br><br>arm<br>disarm<br>stay<br>armed stay<br>armed away<br>off<br>"  
	def phrases = location.helloHome?.getPhrases()*.label
	if (phrases) phrases.each{result += it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() + "<br>"}
    location.modes.each {result += it.name.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() + "<br>"}
    result += "<br><b>LIST_OF_SHCMD</b><br><br>routine<br>security<br>smart home<br>SHM<br>smart home monitor<br>mode<br>" 
    result += "<br><b>LIST_OF_MACROS</b><br><br>"
    if (childApps.size()) {
    	childApps.each {
			result += it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() + "<br>"
        }       
	}
	result += "<br><hr><br><i><b>URL of this setup page:</b></i><br><br>${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}<br><br><hr></div>"
}
def displayData(){
	render contentType: "text/html", data: """<!DOCTYPE html><html><head><meta charset="UTF-8" /><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/></head><body style="margin: 0;">${setupData()}</body></html>"""
}
//Child code pieces here----------------------------------------
//Macro Handler
def macroResults(num, cmd, colorData, param){ 
	def result, data
    if (macroType == "Voice") result = reportResults()      
    if (macroType == "Control") result = controlResults(num)
    if (macroType == "Group") result = groupResults(num, cmd, colorData, param)
	if (macroType == "CoRE") {
    	result = CoREResults(num)
        data = [ pistonName: CoREName, args: result]
        sendLocationEvent (name: "CoRE", value: "execute", data: data , descriptionText: "Ask Alexa triggered '${CoREName}' piston.") 
    }
    else if (macroType == "Voice" ||  macroType == "Control" || macroType == "Group") {
    	data = [alexaOutput: result, num: num, cmd: cmd, color: colorData, param:param]
        sendLocationEvent(name: "askAlexaMacro", value: app.label, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa ran '${app.label}' macro.")
	}
    return result
}
//Group Handler
def groupResults(num, op, colorData, param){   
    String result= ""
    def noun="", valueWord, proNoun="", verb=""
    proNoun = settings."groupDevice${groupType}".size()==1 ? "its" : "their"
    if (groupType=="colorControl" || groupType=="switchLevel"){
    	num = num < 0 ? 0 : num >99 ? 100 : num
        if (op == "maximum") { num = 100; op ="undefined" }
    	valueWord = num ==100 ? "${proNoun} maximum brightness" :  op =="low" || op=="medium" || op=="high" ? "${op}, or a value of ${num}%" : "${num}%"
    }
    if (groupType=="switch"){
    	noun=settings."groupDevice${groupType}".size()==1 ? "device" : "devices"
        if (op == "on" || op == "off") { settings."groupDevice${groupType}"?."$op"();result = voicePost && !noAck ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am turning ${op} the ${noun} in the group named '${app.label}'. " }
        else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost && !noAck? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " }
        else { result = "For a switch group, be sure to give an 'on', 'off' or 'toggle' command. %1%"}
    }
    else if (groupType=="switchLevel" || groupType=="colorControl"){
        if (groupType=="switchLevel") noun=settings."groupDevice${groupType}".size()==1 ? "dimmer" : "dimmers"
        if (groupType=="colorControl") noun=settings."groupDevice${groupType}".size()==1 ? "colored light" : "colored lights"
        verb=settings."groupDevice${groupType}".size()==1 ? "is" : "are"
        if (num==0 && op=="undefined") op="off"
        if (op=="on" || op=="off"){ settings."groupDevice${groupType}"?."$op"();result = voicePost ? replaceVoiceVar(voicePost,"") : noAck ? " " :  "I am turning ${op} the ${noun} in the group named '${app.label}'. "}
        else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " }
		else if (groupType=="switchLevel" && num > 0 && op =="undefined") { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am setting the ${noun} in the ${app.label} to ${valueWord}. " }
		else if (groupType=="colorControl" && num > 0 && !colorData && op =="undefined") { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost && !noAck  ? replaceVoiceVar(voicePost,"") :  noAck ? " " :"I am setting the ${noun} in the '${app.label}' group to ${valueWord}. " }
        else if (groupType=="colorControl" && colorData && param) { 
        	settings."groupDevice${groupType}"?.setColor(colorData)
            if (!voicePost && !noAck){
            	result ="I am setting the ${noun} in the ${app.label} to ${param}"
    			valueWord = num ==100 ? " and ${proNoun} maximum brightness" : op =="low" || op=="medium" || op=="high" ? ",and to ${op}, or a brightness level of ${num}%" : ", at a brightness level of ${num}%"
                result += num > 0 ? "${valueWord}. " : ". "
            }
            else if (voicePost && !noAck)  result = replaceVoiceVar(voicePost,"") 
            else result = " "
        }
        else if (op == "increase" || op=="raise" || op=="up" || op == "decrease" || op=="down" || op=="lower"){
        	if (parent.lightAmt){
                settings."groupDevice${groupType}".each{ upDownChild(it, op, num) }
				def count = 0
                if (op == "increase" || op=="raise" || op=="up") {
                    result = "I have raised the brightness of the ${noun} in the group named '${app.label}'"
					result += num>0 ? " by ${num}%. " : ". "
                    settings."groupDevice${groupType}".each { if (it.currentValue("level")>98) count ++ }
                	if (count == settings."groupDevice${groupType}".size()) result = "The ${noun} in the group '${app.label}' ${verb} at maximum brightness. "
                	if (count > 0 && count < settings."groupDevice${groupType}".size()) result += "Some of the ${noun} ${verb} at maximum brightness. "
                }
                if (op == "decrease" || op=="down" || op=="lower"){
                	result = "I have decreased the brightness of the ${noun} in the group named '${app.label}'"
            		result += num>0 ? " by ${num}%. " : ". "
                    settings."groupDevice${groupType}".each { if (it.currentValue("switch")=="off") count ++ }
                	if (count == settings."groupDevice${groupType}".size()) result = "The ${noun} in the group '${app.label}' ${verb} off. "
                	if (count > 0 && count < settings."groupDevice${groupType}".size()) result += "Some of the ${noun} ${verb} now off. "
                }
            }
            else result = "The default increase or decrease value is set to zero within the SmartApp. I am taking no action. "
        }
        else if (groupType=="switchLevel") result = "For a dimmer group, be sure to use an 'on', 'off', 'toggle' or brightness level setting. %1%" 
		else if (groupType=="colorControl") result = "For a colored light group, be sure to give me an 'on', 'off', 'toggle', brightness level or color command. %1%" 
    }
    else if (groupType=="lock"){
    	noun=settings."groupDevice${groupType}".size()==1 ? "device" : "devices"
            if (op == "lock"|| op == "unlock" ){ 
                if (param =="undefined" || (param !="undefined" && param == num)){
				settings."groupDevice${groupType}"?."$op"()
				result = voicePost && !noAck ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am ${op}ing the ${noun} in the group named '${app.label}'. " 
			}
			else result = "To lock or unlock a group, you must say use the proper password. %1%"
        }
        else { result = "For a lock group, you must use a 'lock' or 'unlock' command. %1%" }
    }
    else if (groupType=="doorControl"){
     	noun=settings."groupDevice${groupType}".size()==1 ? "door" : "doors"
        if (op == "open"|| op == "close" ){
        	if (param && param == num){
            	settings."groupDevice${groupType}"?."$cmd"()
            	def condition = op=="close" ? "closing" : "opening"
            	result = voicePost && !noAck  ? replaceVoiceVar(voicePost,"") : noAck ? " " :  "I am ${condition} the ${noun} in the group named '${app.label}'. "
        	}
            else result = "To open or close a group of doors, you must say use the proper password. %1%"
        }
        else { result = "For a door group, you must use an 'open' or 'close' command. %1%"}
    }
    else if (groupType=="thermostat"){
        noun=settings."groupDevice${groupType}".size()==1 ? "thermostat in the group named '${app.label}'" : "thermostats in the group named '${app.label}'"
        if (num>0) num = num < parent.getTstatLimits().hi && num > parent.getTstatLimits().lo ? num : num > parent.getTstatLimits().hi ? parent.getTstatLimits().hi : parent.getTstatLimits().lo 
        if (op == "on" || op=="off") {
        	if (param == "undefined" && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning on a thermostat group. %1%"
            if (param =="heat" || param=="heating") {result="I am setting the ${noun} to 'heating' mode. "; settings."groupDevice${groupType}"?.heat()}
			if (param =="cool" || param=="cooling") {result="I am setting the ${noun} to 'cooling' mode. "; settings."groupDevice${groupType}"?.cool()}
			if (param =="auto" || param=="automatic") {result="I am setting the ${noun} to 'auto' mode. Please note, "+
				"to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints separately. " ; settings."groupDevice${groupType}"?.auto()}
            if (op == "off") result = "I am turning off the ${noun}. "
            if (parent.isStelpro() && param=="eco"){ result="I am setting the ${noun} to 'eco' mode. "; settings."groupDevice${groupType}"?.setThermostatMode("eco") }
			if (parent.isStelpro()  && param=="comfort"){ result="I am setting the ${noun} to 'comfort' mode. "; settings."groupDevice${groupType}"?.setThermostatMode("comfort") }
        	if (param=="home" && parent.isNest()) { result="I am setting the ${noun} to 'home' mode. "; settings."groupDevice${groupType}"?.present() }
            if (param=="away" && parent.isNest()) { 
            	result="I am setting the ${noun} to 'away' mode. Please note that Nest thermostats will not accept temperature changes while in 'away' status."
                settings."groupDevice${groupType}"?.away()
            }
        }
        else if (op == "increase" || op=="raise" || op=="up" || op == "decrease" || op=="down" || op=="lower") {
			result = "Increase and decrease commands are not yet compatible with thermostat group macros. %1%"
        }
        else {
            param = tstatDefaultCool && param == "undefined" ? "cool" : tstatDefaultHeat && param == "undefined" ? "heat" : param
			if (param == "undefined") result = "You must designate a 'heating' or 'cooling' parameter when setting the temperature of a thermostat group. %1%"
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
    else result = "I did not understand what you are attempting to do with the group named '${app.label}'. Be sure it is configured correctly within the SmartApp. %1%" 
    return result
}
//CoRE Handler-----------------------------------------------------------
def CoREResults(sDelay){	
	String result = ""
    def delay
    if (cDelay>0 || sDelay>0) delay = sDelay==0 ? cDelay as int : sDelay as int
	result = (!delay || delay == 0) ? "I am triggering the CORE macro named '${app.label}'. " : delay==1 ? "I'll trigger the '${app.label}' CORE macro in ${delay} minute. " : "I'll trigger the '${app.label}' CORE macro in ${delay} minutes. "
		if (sDelay == 9999) { 
		result = "I am cancelling all scheduled executions of the CORE macro, '${app.label}'. "  
		state.scheduled = false
		unschedule() 
	}
	if (!state.scheduled) {
		if (!delay || delay == 0) CoREHandler() 
		else if (delay < 9999) { runIn(delay*60, CoREHandler, [overwrite: true]) ; state.scheduled=true}
		if (delay < 9999) result = voicePost && !noAck ? replaceVoiceVar(voicePost, delay) : noAck ? " " : result
	}
	else result = "The CORE macro, '${app.label}', is already scheduled to run. You must cancel the execution or wait until it runs before you can run it again. "
	return result
}
def CoREHandler(){
   	state.scheduled = false
}
//Control Handler-----------------------------------------------------------
def controlResults(sDelay){	
	String result = ""
    def delay
    if (cDelay>0 || sDelay>0) delay = sDelay==0 ? cDelay as int : sDelay as int
    if (macroTypeDesc() !="Status: UNCONFIGURED - Tap to configure macro"){	
		result = (!delay || delay == 0) ? "I am running the '${app.label}' control macro. " : delay==1 ? "I'll run the '${app.label}' control macro in ${delay} minute. " : "I'll run the '${app.label}' control macro in ${delay} minutes. "
		if (sDelay == 9999) { 
        	result = "I am cancelling all scheduled executions of the control macro, '${app.label}'. "  
            state.scheduled = false
            unschedule() 
        }
		if (!state.scheduled) {
        	if (!delay || delay == 0) controlHandler() 
            else if (delay < 9999) { runIn(delay*60, controlHandler, [overwrite: true]) ; state.scheduled=true}
            if (delay < 9999) result = voicePost && !noAck ? replaceVoiceVar(voicePost, delay) : noAck ? "" : result
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
//Report Handler-------------------------------------------------------------
def reportResults(){
    String fullMsg=""
    try {
        fullMsg = voicePre ?  voicePre + " " : ""
        if (voiceOnSwitchOnly) fullMsg += voiceSwitch ? switchOnReport(voiceSwitch, "switches") : ""
        else fullMsg += voiceSwitch ? reportStatus(voiceSwitch, "switch") : ""
        if (voiceOnSwitchEvt) fullMsg += getLastEvt(voiceSwitch, "'switch on'", "on", "switch")
        if (voiceOnDimmerOnly) fullMsg += voiceDimmer ? switchOnReport(voiceDimmer, "dimmers") : ""
        else fullMsg += voiceDimmer ? reportStatus(voiceDimmer, "level") : ""
 		if (voiceOnDimmerEvt) fullMsg += getLastEvt(voiceDimmer, "'dimmer on'", "on", "dimmer")
        fullMsg += voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? doorWindowReport() : ""
        if (voiceTemperature && (voiceTemperature.size() == 1 || !voiceTempAvg)) fullMsg += reportStatus(voiceTemperature, "temperature")
        else if (voiceTemperature && voiceTemperature.size() > 1 && voiceTempAvg) fullMsg += "The average of the monitored temperature devices is: " + getAverage(voiceTemperature, "temperature") + " degrees. "
        if (voiceHumidity && (voiceHumidity.size() == 1 || !voiceHumidAvg)) fullMsg += reportStatus(voiceHumidity, "humidity")
        else if (voiceHumidity  && voiceHumidity.size() > 1 && voiceHumidAvg) fullMsg += "The average of the monitored humidity devices is " + getAverage(voiceHumidity, "humidity") + "%. "
        if (voiceTempSettingSummary && voiceTempSettingsType && voiceTempSettingsType !="autoAll") fullMsg += voiceTempSettings ? thermostatSummary(): ""
        else fullMsg += (voiceTempSettings && voiceTempSettingsType) ? reportStatus(voiceTempSettings, voiceTempSettingsType) : ""
        fullMsg += voiceSpeaker ? speakerReport() : ""
        if (voiceWeatherLoc && (location.timeZone || zipCode)){
        	def type = voiceWeatherTemp|| voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ? "report" : ""
            if ((voiceWeatherToday  || voiceWeatherTonight || voiceWeatherTomorrow) && type) type += " and forecast"
            else if ((voiceWeatherToday  || voiceWeatherTonight || voiceWeatherTomorrow) && !type) type = "forecast"
            def cond = getWeatherFeature("conditions", zipCode).current_observation
        	if (type) fullMsg += "The following weather ${type} comes from " + cond.observation_location.full + ": "
        }
        fullMsg += voiceWeatherTemp|| voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ||
        	voiceWeatherToday  || voiceWeatherTonight || voiceWeatherTomorrow || voiceSunset || voiceSunrise ? weatherAlerts() : ""
        fullMsg += voiceWeatherTemp|| voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ? getWeatherReport() : ""
        fullMsg += voiceWeatherToday  || voiceWeatherTonight || voiceWeatherTomorrow || voiceSunset || voiceSunrise ? getWeatherForecast() : ""
        fullMsg += voiceMoon ? getMoonInfo() : ""
        fullMsg += voiceWater && waterReport() ? waterReport() : ""
        fullMsg += voicePresence ? presenceReport() : ""
        fullMsg += voiceMotion && motionReport() ? motionReport() : ""
        fullMsg += voicePower && powerReport() ? powerReport() : ""
        fullMsg += voiceMode ? "The current SmartThings mode is set to, '${location.currentMode}'. " : ""
        fullMsg += voiceSHM ? "The current Smart Home Monitor status is '${location.currentState("alarmSystemStatus")?.value}'. " : ""
        fullMsg += voiceBattery && batteryReport() ? batteryReport() : ""
        fullMsg += voicePost ? voicePost : ""
	}
    catch(e){ fullMsg = "There was an error processing the report. Please try again. If this error continues, please contact the author of Ask Alexa. " }
    if (!fullMsg && !allowNullRpt) fullMsg = "The voice report, '${app.label}', did not produce any output. Please check the configuration of the report within the SmartApp. "  
    if ((parent.getAdvEnabled() && voiceRepFilter) || voicePre || voicePost) fullMsg = replaceVoiceVar(fullMsg,"")
    if (fullMsg && !fullMsg.endsWith(" ")) fullMsg += " "
    return fullMsg
}
//Voice report sections---------------------------------------------------
def switchOnReport(devices, type){
	String result = ""
    if (devices.latestValue("switch").contains("on")) devices.each { deviceName->
    	if (deviceName.latestValue("switch")=="on") {
        	result += "The ${deviceName} is on"
        	result += type == "dimmers" && deviceName.latestValue("level") ? " and set to ${deviceName.latestValue("level") as int}%. " : ". "
		}
    }
	result
}
def thermostatSummary(){
	String result = ""
    def monitorCount = voiceTempSettings.size(), matchCount = 0, err = false
    for (device in voiceTempSettings) {
        if (parent.isNest()) try { device.poll() } catch(e) { }
        try{ if (device.latestValue(voiceTempSettingsType) as int == voiceTempTarget as int)  matchCount ++ }
        catch (e) { err=true }
    }
    if (!err){
        def difCount = monitorCount - matchCount
        if (monitorCount == 1 &&  difCount==1) result +="The monitored thermostat, ${getList(voiceTempSettings)}, is not set to ${voiceTempTarget} degrees. "
        else if (monitorCount == 1 && !difCount) result +="The monitored thermostat, ${getList(voiceTempSettings)}, is set to ${voiceTempTarget} degrees. "
        if (monitorCount > 1) {
            if (difCount==monitorCount) result += "None of the thermostats are set to ${voiceTempTarget} degrees. "
            else if (matchCount==1) {
                for (device in voiceTempSettings){
                    if (parent.isNest()) try { device.poll() } catch(e) { }
                    if (device.latestValue(voiceTempSettingsType) as int == voiceTempTarget as int){
                        result += "Of the ${monitorCount} monitored thermostats, only ${device} is set to ${voiceTempTarget} degrees. "
                    }
                }
            }
            else if (difCount && matchCount>1) {
                result += "Some of the thermostats are set to ${voiceTempTarget} degrees except"
                for (device in voiceTempSettings){
                    if (parent.isNest()) try { device.poll() } catch(e) { }
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
    return result
}
def reportStatus(deviceList, type){
	String result = ""
    def appd = type=="temperature" || type=="thermostatSetpoint" || type == "heatingSetpoint" || type=="coolingSetpoint" || type == "autoAll" ? " degrees" : type == "humidity" ? " percent relative humidity" : ""
    if (type != "thermostatSetpoint" && type != "heatingSetpoint" && type !="coolingSetpoint" && type != "autoAll") {
		deviceList.each {deviceName->
			if (type=="level" && deviceName.latestValue("switch")=="on") result += "The ${deviceName} is on and set to ${deviceName.latestValue(type) as int}%. "
            else if (type=="level" && deviceName.latestValue("switch")=="off") result += "The ${deviceName} is off. "
            else result += "The ${deviceName} is ${deviceName.latestValue(type)}${appd}. " 
		}
    }
	else if (type != "autoAll") deviceList.each { deviceName->
        if (parent.isNest()) try { deviceName.poll() } catch(e) { }
        try { result += "The ${deviceName} is set to ${Math.round(deviceName.latestValue(type))}${appd}. " }
    	catch (e) { result = "The ${deviceName} is not able to provide its setpoint. Please choose another setpoint type to report on. " }
    }
    else if (type == "autoAll") deviceList.each { deviceName->
        if (parent.isNest()) try { deviceName.poll() } catch(e) { }
        try { 
        	result += "The ${deviceName} has a cooling setpoint of ${Math.round(deviceName.latestValue("coolingSetpoint"))}${appd}, " +
        		"and a heating setpoint of ${Math.round(deviceName.latestValue("heatingSetpoint"))}${appd}. " 
        }
    	catch (e) { result += "The ${deviceName} is not able to provide one of its setpoint. Ensure you are in a thermostat mode that allows reading of these setpoints. " }
    }
    result
}
def speakerReport(){
	String result = ""
    if (voiceSpeakerOn) {
        if (voiceSpeaker.latestValue("status").contains("playing")) {
        	voiceSpeaker.each { deviceName->
            	def level = deviceName.currentValue("level"), track = deviceName.currentValue("trackDescription"), mute = deviceName.currentValue("mute")
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
            def onOffStatus = deviceName.currentValue("status"), level = deviceName.currentValue("level"), track = deviceName.currentValue("trackDescription"), mute = deviceName.currentValue("mute")
            if (onOffStatus) {
            	result += "${deviceName} is ${onOffStatus}"
				result += onOffStatus =="stopped" ? ". " : onOffStatus=="playing" && track ? ": '${track}'" : ""
				result += onOffStatus == "playing" && level && mute =="unmuted" ? ", and it's volume is set to ${level}%. " : mute =="muted" ? ", and it's currently muted. " :""
			}
            else result += "${deviceName} is not reporting any status. "
        }
	}
    return result
}
def presenceReport(){
	String result = ""
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
	String result = "" 
    def currVal
    if (voiceMotionOnly) {
        if (voiceMotion.latestValue("motion").contains("active")) {
        	voiceMotion.each { deviceName->
            	if (deviceName.latestValue("motion")=="active") result += "The ${deviceName} is reading motion. "
    		}
        }
    }
	else {
		voiceMotion.each {deviceName->
			currVal = [active: "movement", inactive: "no movement"][deviceName.latestValue("motion")] ?: deviceName.latestValue("motion")
            result += "The ${deviceName} is reading " + currVal + ". "
        }
	}
    if (voiceMotionEvt) result += getLastEvt(voiceMotion, "movement", "active", "sensor")	
    return result 
}
def powerReport(){
	String result = ""
    def currVal
    voicePower.each { deviceName->
		currVal = deviceName.currentValue("power")
        if (currVal == null) currVal=0
        if (voicePowerOn)  result += currVal>0 ? "The ${deviceName} is reading " + currVal  + " watts. " : ""
        else result += "The ${deviceName} is reading " + currVal  + " watts. "
	}
    return result 
}
def doorWindowReport(){
	def countOpened = 0, countOpenedDoor = 0, countUnlocked = 0, listOpened = "", listUnlocked = ""
    String result = ""
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
   			result += voiceDoorLocks ? "All of the doors are locked, but the " : "The "
            result += totalCount > 1 ? "following doors or windows are open: ${listOpened}. " : "${listOpened} is open. "
    	}
    }   
	else {
		if ((countOpened || countOpenedDoor) && !countUnlocked) result += totalCount > 1 ? "The following doors or windows are currently open: ${listOpened}. " : "The ${listOpened} is open. "
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
    String result = ""
	for (deviceName in devices){	
		if (deviceName.latestValue("${type}") == "${condition}"){
			result += "${deviceName}"
			count = count - 1
			if (count == 1) result += " and the "
			else if (count> 1) result += ", "
		}
	}
    result
}
def batteryReport(){
    String result = ""
    def count = 0, batteryThresholdLevel = batteryThreshold as int, nullCount =0
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
    String result = ""
    def count = 0
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
//Parent Code Access (from Child)-----------------------------------------------------------
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd) }
def listPistons() { return state.CoREPistons }
//Common Code(Child)-----------------------------------------------------------
def upDownChild(device, op, num){
    def numChange, newLevel, currLevel, defMove
    defMove = parent.lightAmt ? parent.lightAmt :0 ; currLevel = device.currentValue("switch")=="on" ? device.currentValue("level") as int : 0
    if (op == "increase" || op=="raise" || op=="up")  numChange = num == 0 ? defMove : num > 0 ? num : 0
    if (op == "decrease" || op=="down" || op=="lower") numChange = num == 0 ? -defMove : num > 0 ? -num  : 0
    newLevel = currLevel + numChange; newLevel = newLevel > 100 ? 100 : newLevel < 0 ? 0 : newLevel
    if (defMove>0) device.setLevel(newLevel)
    if (newLevel==0) device.off()
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
def macroTypeDesc(){
	def desc = ""
    def customAck = !noAck && !voicePost ? "; uses standard acknowledgment message" : !noAck  && voicePost ? "; includes a custom acknowledgment message" :  "; there will be no acknowledgment messages"
    if (macroType == "Control" || macroType =="CoRE") customAck += cDelay>1 ? "; activates ${cDelay} minutes after triggered" : cDelay==1 ? "; activates one minute after triggered" : ""
    if (macroType == "Control" && (phrase || setMode || SHM || getDeviceDesc() != "Status: UNCONFIGURED - Tap to configure" || 
    	getHTTPDesc() !="Status: UNCONFIGURED - Tap to configure" || (contacts && smsMsg) || (smsNum && pushMsg && smsMsg))) 
        	desc= "Control Macro CONFIGURED${customAck} - Tap to edit" 
    if (macroType =="Voice" && (voicePre || voiceSwitch || voiceDimmer || voiceDoorSensors || voiceDoorControls || voiceDoorLocks || 
    	voiceTemperature ||  voiceTempSettings || voiceTempVar || voiceHumidVar || voiceHumidity || greyOutWeather()=="complete" ||
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
        customAck += addPost && !noAck ? " appended to the child macro messages" : noAck ? "" : " replacing the child macro messages"
        def countDesc = groupMacros.size() == 1 ? "one macro" : groupMacros.size() + " macros"
        desc = "Macro Group CONFIGURED with ${countDesc}${customAck} - Tap to edit" 
    }
    if (macroType =="CoRE" && CoREName) {
        def timer = !cDelay || cDelay==0 ? "no delay" : cDelay==1 ? "a one minute delay to trigger" : "a ${cDelay} minute delay to trigger"
        desc = "Trigger '${CoREName}' piston with ${timer}${customAck} - Tap to edit" 
    }
    desc = desc ? desc : "Status: UNCONFIGURED - Tap to configure macro"
}
def greyOutMacro(){ def result = macroTypeDesc() == "Status: UNCONFIGURED - Tap to configure macro" ? "" : "complete" }
def greyOutStateHTTP(){ def result = getHTTPDesc() == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete" }
def reportSwitches(){
	def result="Status: UNCONFIGURED - Tap to configure"
    if (voiceSwitch || voiceDimmer){
        def switchEvt = voiceOnSwitchEvt ? "/Event" : ""
        def dimmerEvt =voiceOnDimmerEvt ? "/Event" : ""
    	def switchOn = voiceOnSwitchOnly ? "(On${switchEvt})" : "(On/Off${switchEvt})"
        def dimmer = voiceOnDimmerOnly ? "(On${dimmerEvt})" : "(On/Off${dimmerEvt})"
        result  = voiceSwitch && voiceSwitch.size()>1 ? "Switches ${switchOn}" : voiceSwitch && voiceSwitch.size()==1 ? "Switch ${switchOn}" : ""
        if (voiceDimmer) result  += voiceSwitch && voiceDimmer.size()>1 ? " and dimmers ${dimmer}" : voiceSwitch && voiceDimmer.size()==1 ? " and dimmer ${dimmer}" : ""
        if (voiceDimmer) result  += !voiceSwitch &&  voiceDimmer.size()>1 ? "Dimmers ${dimmer}" : !voiceSwitch && voiceDimmer.size()==1 ? "Dimmer ${dimmer}" : ""
		result += (voiceSwitch && voiceDimmer) || (voiceSwitch && voiceSwitch.size()>1) || (voiceDimmer && voiceDimmer .size()>1) ? " report status" : " reports status"
    }
    return result
}
def reportDoors(){
	def result="Status: UNCONFIGURED - Tap to configure"
    if (voiceDoorSensors || voiceDoorControls || voiceDoorLocks ){
        def doorEvt = voiceDoorEvt ? " with door & window events" : ""
        def lockEvt = voiceLockEvt ?"/lock events${doorEvt}":"${doorEvt}"
        def status = voiceDoorAll ? "full status${lockEvt}" : "open/unlocked status${lockEvt}"
        result  = voiceDoorSensors && voiceDoorSensors.size()>1 ? "Door/Window sensors" : voiceDoorSensors && voiceDoorSensors.size()==1 ? "Door/Window sensor" : ""
        if (voiceDoorControls) result  += voiceDoorSensors && voiceDoorControls.size()>1 && voiceDoorLocks ? ", door controls" : voiceDoorSensors && voiceDoorControls.size()==1 && voiceDoorLocks ? ", door control" : ""
        if (voiceDoorControls) result  += voiceDoorSensors && voiceDoorControls.size()>1 && !voiceDoorLocks ? " and door controls" : voiceDoorSensors && voiceDoorControls.size()==1 && !voiceDoorLocks ? " and door control" : ""
        if (voiceDoorControls) result  += !result && voiceDoorControls.size()>1 ? "Door controls" : !result && voiceDoorControls.size()==1 ? "Door control" : ""
		if (voiceDoorLocks) result  += result  && voiceDoorLocks.size()>1 ? " and locks" : result && voiceDoorLocks.size()==1 ? " and lock" : ""
        if (voiceDoorLocks) result  += !result && voiceDoorLocks.size()>1 ? "Locks" : !result && voiceDoorLocks.size()==1 ? "Lock" : ""
        result += (voiceDoorSensors && voiceDoorControls && voiceDoorLocks) || (voiceDoorSensors && voiceDoorControls) || (voiceDoorControls && voiceDoorLocks) || (voiceDoorSensors && voiceDoorLocks) ||
        	(voiceDoorSensors && voiceDoorSensors.size()>1) || (voiceDoorControls && voiceDoorControls.size()>1) || (voiceDoorLocks && voiceDoorLocks.size()>1 ) ? " report ${status}" : " reports ${status}"
    }
    return result
}
def reportSensors(){
	def result="Status: UNCONFIGURED - Tap to configure"
    if (voiceWater || voiceMotion || voicePresence||voicePower){
    	def water = voiceWetOnly ? "(Wet)" : "(Wet/Dry)"
        def arriveEvt = voicePresentEvt ? "/Arrival" : ""
        def departEvt = voiceGoneEvt ? "/Departure" : ""
        def present = voicePresentOnly ? "(Present${arriveEvt}${departEvt})" : "(Present/Away${arriveEvt}${departEvt})"
        def motionEvt = voiceMotionEvt ? "/Event" : ""
        def motion = voiceMotionOnly ? "(Active${motionEvt})" : "(Active/Not Active${motionEvt})"
        def power = voicePowerOn ? "(Active)" : "(Active/Not Active)"
        result  = voiceWater && voiceWater.size()>1 ? "Water sensors ${water}" : voiceWater && voiceWater.size()==1 ? "Water sensor ${water}" : ""
        if (voicePresence) result  += result && voicePresence.size()>1 && !voiceMotion ? " and presence sensors ${present}" : result && voicePresence.size()==1 && !voiceMotion ? " and presence sensor ${present}" : ""
        if (voicePresence) result  += !result && voicePresence.size()>1 ? "Presence sensors ${present}" : !result && voicePresence.size()==1 ? "Presence sensor ${present}" : ""
        if (voicePresence) result  += result && voicePresence.size()>1 && voiceMotion ? ", presence sensors ${present}" : result && voicePresence.size()==1 && voiceMotion  ? ", presence sensor ${present}" : "" 
        if (voiceMotion) result += result && voiceMotion.size()>1 ? ", motion sensors ${motion}" : result && voiceMotion.size()==1 ? ", motion sensor ${motion}" : ""
        if (voiceMotion) result += !result && voiceMotion.size()>1 ? "Motion sensors ${motion}" : !result && voiceMotion.size()==1 ? "Motion sensor ${motion}" : ""
        if (voicePower) result += result && voicePower.size()>1 ? " and power meters" : result && voicePower.size()==1 ? " and power meter" : ""
        if (voicePower) result += !result && voicePower.size()>1 ? "Power meters ${power}" : !result && voicePower.size()==1 ? "Power meter ${power}" : ""
        result += (voiceWater && voicePresence && voiceMotion && voicePower) || (voiceWater && voicePresence && voiceMotion) || (voiceWater && voicePresence && voicePower) ||
        (voiceWater && voiceMotion && voicePower) && (voiceWater && voicePresence) || (voiceWater && voiceMotion) || (voiceWater && voicePower) || (voicePresence && voiceMotion)||
        (voicePresence && voicePower) || ( voiceMotion && voicePower) || (voicePower && voicePower.size()>1) || (voiceWater && voiceWater.size()>1) ||
        (voiceMotion && voiceMotion.size()>1) || (voicePresence && voicePresence.size()>1 ) ? " report status" : " reports status"
    }
    return result
}
def reportTemp(){
	def result="Status: UNCONFIGURED - Tap to configure"
    if (voiceTemperature || voiceHumidity || voiceTempSettings ){
		def tempAvg = voiceTempAvg ? " (Average)" : ""
        def humidAvg = voiceHumidAvg  ? " (Average)" : ""
        def tstatSet = voiceTempSettingSummary && voiceTempSettings && voiceTempSettingsType !="autoAll" && voiceTempTarget ? "(Setpoint summary target: ${voiceTempTarget} degrees)":"(Setpoint)" 
        result  = voiceTemperature && voiceTemperature.size()>1 ? "Temperature sensors${tempAvg}" : voiceTemperature && voiceTemperature.size()==1 ? "Temperature sensor${tempAvg}" : ""
        if (voiceHumidity) result  += result && voiceHumidity.size()>1 && voiceTempSettings ? ", humidity sensors${humidAvg}" : result && voiceHumidity.size()==1 && voiceTempSettings ? ", humidity sensor${humidAvg}" : ""
        if (voiceHumidity) result  += result && voiceHumidity.size()>1 && !voiceTempSettings ? " and humidity sensors${humidAvg}" : result && voiceHumidity.size()==1 && !voiceTempSettings ? " and humidity sensor${humidAvg}" : ""
        if (voiceHumidity) result  += !result && voiceHumidity.size()>1 ? "Humidity sensors${humidAvg}" : !result && voiceHumidity.size()==1 ? "Humidity sensor${humidAvg}" : ""
		if (voiceTempSettings) result  += result  && voiceTempSettings.size()>1 ? " and thermostats ${tstatSet}" : result && voiceTempSettings.size()==1 ? " and thermostat${tstatSet}" : ""
        if (voiceTempSettings) result  += !result && voiceTempSettings.size()>1 ? "Thermostats ${tstatSet}" : !result && voiceTempSettings.size()==1 ? "Thermostat ${tstatSet}" : ""
        result += (voiceTemperature && voiceHumidity && voiceTempSettings) || (voiceTemperature && voiceHumidity) || (voiceHumidity && voiceTempSettings) || (voiceTemperature && voiceTempSettings) || 
        	(voiceTemperature && voiceTemperature.size()>1) || (voiceHumidity && voiceHumidity.size()>1) || (voiceTempSettings && voiceTempSettings.size()>1 ) ? " report status" : " reports status"
    }
    return result
}
def batteryDesc(){
	def result = "Status: UNCONFIGURED - Tap to configure"
    if (voiceBattery && batteryThreshold) result = voiceBattery.size()>1 ? "Batteries report when level < ${batteryThreshold}%" : "One battery reports when level < ${batteryThreshold}%"
	return result
}
def reportDescMSHM() {
	def result= "Status: UNCONFIGURED - Tap to configure"
    if (voiceMode || voiceSHM){
    	result = "Report: "
        result += voiceMode ? "Mode Status" : ""
    	result += voiceMode && voiceSHM ? " and " : ""
    	result += voiceSHM ? "SHM Status" : ""
	}
    return result
}
def speakerDesc(){
	def result = "Status: UNCONFIGURED - Tap to configure"
    if (voiceSpeaker && voiceSpeakerOn) result = voiceSpeaker.size()>1 ? "The active speakers" : "The active speaker"
    else if (voiceSpeaker && !voiceSpeakerOn) result = voiceSpeaker.size()>1 ? "Speakers" : "One speaker"
    if (voiceSpeaker) result += voiceSpeaker.size()>1 ? " report status" : " reports status" 
	return result
}
def weatherDesc(){ 
	def result = voiceWeatherTemp|| voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ? "Current weather" : ""
	if (result && (voiceWeatherToday || voiceWeatherTonight || voiceWeatherTomorrow)) result +=", "
    result += (voiceWeatherToday && voiceWeatherTonight && voiceWeatherTomorrow) || (voiceWeatherToday && voiceWeatherTonight) || (voiceWeatherTonight && voiceWeatherTomorrow) ||
    	(voiceWeatherToday && voiceWeatherTomorrow) ? "Forecasts" : voiceWeatherToday || voiceWeatherTonight || voiceWeatherTomorrow ? "Forecast" : ""
    if (result && (voiceSunrise || voiceSunset)) result +=", "
    result += voiceSunrise && voiceSunset ? "Sunrise/Sunset" : voiceSunrise ? "Sunrise" : voiceSunset ? "Sunset" : ""
	if (result && voiceMoon) result +=", "
    result += voiceMoon ? "Lunar phases" : ""
    result = result ? "Reports: ${result}" : "Status: UNCONFIGURED - Tap to configure"
}
def greyOutWeather(){ def result = voiceWeatherToday || voiceWeatherTonight || voiceWeatherTomorrow || voiceSunrise || voiceSunset || voiceMoon ||
	voiceWeatherTemp|| voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip ? "complete" : "" }
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
private replaceVoiceVar(msg, delay) {
    def df = new java.text.SimpleDateFormat("EEEE")
    def isMetric = location.temperatureScale == "C"
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date()), time = parseDate("","h:mm a"), month = parseDate("","MMMM"), year = parseDate("","yyyy"), dayNum = parseDate("","d")
    def varList = parent.getVariableList()
    def temp = varList[0].temp
    if (isMetric && temp !="undefined device") {
    	String t = temp as String
        if (t.endsWith(".0")) t = t - ".0"
        temp=t.toFloat() + " degrees"
    }
    else if (temp !="undefined device") temp = Math.round(temp) + " degrees"
    def humid = varList[0].humid
    def people = varList[0].people
    def fullMacroType=[GroupM: "Macro Group", Control:"Control Macro", Group:"Device Group", Voice:"Voice Reporting"][macroType] ?: macroType
	def fullDeviceType=[colorControl: "Colored Light",switchLevel:"Dimmer" ,doorControl:"Door",lock:"Lock",switch:"Switch",thermostat:"Thermostat"][groupType] ?: groupType
    def delayMin = delay ? delay + " minutes" : "No delay specified"
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
    msg = msg.replace('%delay%',"${delayMin}")
    if (parent.getAdvEnabled() && voiceRepFilter) {
    	def textFilter=voiceRepFilter.toLowerCase().tokenize(",")
    	textFilter.each{ msg = msg.toLowerCase().replace("${it}","") }
    }
    msg
}
private timeParse(time, type) { new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("${type}", location.timeZone)}
private parseDate(time, type){
    long longDate = time ? Long.valueOf(time).longValue() : now()
    def formattedDate = new Date(longDate).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", formattedDate).format("${type}", timeZone(formattedDate))
}
private getWeatherReport(){
	def msg = "", temp
    if (location.timeZone || zipCode) {
    	def sb = new StringBuilder()
        def isMetric = location.temperatureScale == 'C'
		def cond = getWeatherFeature('conditions', zipCode).current_observation
        if (voiceWeatherTemp){
        	sb << 'The current temperature is '
            if (isMetric) { 
            	temp = cond.temp_c 
                String t = temp as String 
                if (t.endsWith('.0')) t = t - '.0' 
                temp=t.toFloat()
            } 
            else temp = Math.round(cond.temp_f) 
            sb << temp + ' degrees with ' + cond.weather
            switch (cond.weather) {
            case 'Overcast':
            case 'Clear':
            case 'Partly Cloudy':
            case 'Mostly Cloudy': 
                sb << ' skies. '
                break
            case 'Unknown':
            case 'Thunderstorm':
            case 'Light Thunderstorm':
            case 'Heavy Thunderstorm':
            case 'Sandstorm':
            case 'Light Sandstorm':
            case 'Heavy Sandstorm':
                sb << ' conditions. '
                break
            default:
                sb << '. '
    		}
        }
        if (voiceWeatherHumid){
            sb << 'The relative humidity is ' + cond.relative_humidity + ' and the winds are '
            if ((cond.wind_kph.toFloat() + cond.wind_gust_kph.toFloat()) == 0.0) sb << 'calm. '
            else if (isMetric) {
                sb << 'from the ' + cond.wind_dir + ' at ' + cond.wind_kph + ' kph'
                if (cond.wind_gust_kph.toFloat() > 0) sb << ' gusting to ' + cond.wind_gust_kph + ' kph. '
                else sb << '. '
            }
			else sb << cond.wind_string + '. '
            sb << 'The barometric pressure is '
            switch (cond.pressure_trend) {
    		case '+': 
            	if (isMetric) sb << cond.pressure_mb + ' millibars' else sb << cond.pressure_in + ' inches'
                sb << ' and rising. '
            	break
        	case '-':
            	if (isMetric) sb << cond.pressure_mb + ' millibars' else sb << cond.pressure_in + ' inches'
                sb << " and falling. "
        		break
        	default:
        		sb << "steady at "
                if (isMetric) sb << cond.pressure_mb + ' millibars. ' else sb << cond.pressure_in + ' inches. '
     		}   
       	}
        if (voiceWeatherDew){
            sb << 'The dewpoint is '
			if (isMetric) { 
            	temp = cond.dewpoint_c 
                String t = temp as String 
                if (t.endsWith('.0')) t = t - '.0' 
                temp=t.toFloat() 
            } else temp = Math.round(cond.dewpoint_f)            
            sb << temp +' degrees, and the \'feels like\' temperature is '
            if (isMetric) { 
            	temp = cond.feelslike_c.toFloat() 
                String t = temp as String 
                if (t.endsWith(".0")) t = t - ".0" 
                temp=t.toFloat() 
			} else temp = Math.round(cond.feelslike_f.toFloat()) as Integer 
            sb << temp + ' degrees. '
        }
        if (voiceWeatherSolar){
            if (cond.solarradiation != '--' && cond.UV != '') sb << 'The solar radiation level is ' + cond.solarradiation + ', and the UV index is ' + cond.UV + '. '
            if (cond.solarradiation != '--' && cond.UV == '') sb << 'The solar radiation level is ' + cond.solarradiation + '. '
            if (cond.solarradiation == '--' && cond.UV != '')  sb << 'The UV index is ' + cond.UV + '. '
		}
        if (voiceWeatherVisiblity) {
        	sb << 'Visibility is '
            def visibility = isMetric ? cond.visibility_km.toFloat() : cond.visibility_mi.toFloat()
            String t = visibility as String
            if (visibility >1  && t.endsWith('.0')) t = t - '.0'
            else if (visibility < 1 ) t=t.toFloat()
            if (visibility == 1) if (isMetric) sb << t + ' kilometer. ' else sb << t + ' mile. ' 
            else if (isMetric) sb << t + ' kilometers. ' else sb << t + ' miles. '
     	}
        if (voiceWeatherPrecip) {    
            sb << 'There has been '
            def precip = isMetric ? cond.precip_today_metric : cond.precip_today_in
            def p = 'no'
            if (precip) {
    			if (precip.toFloat() > 0.0) {
    				p = precip as String
     	   			if (p.endsWith('.0')) p = p - '.0'
   				}
    		} 
            else precip = 0.0
			sb << p
            if ( p != 'no' ) {
    			if (precip.toFloat() != 1.0) {
    				if (isMetric) sb << ' millimeters of' else sb << ' inches of'
    			}
                else {
                	if (isMetric) sb << ' millimeter of' else sb << ' inch of'
    			}
    		}
			sb << ' precipitation today. '
		}
	   	msg = sb.toString()
        translateTxt().each {msg = msg.replaceAll(it.txt,it.cvt)}
    }
    else  msg = 'Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather reports. '
    return msg
}
private getWeatherForecast(){
    def msg = ""
    if (location.timeZone || zipCode) {
		def sb = new StringBuilder()
        def isMetric = location.temperatureScale == 'C'
		def weather = getWeatherFeature('forecast', zipCode)
        if (voiceWeatherToday  || voiceWeatherTonight || voiceWeatherTomorrow ){
            if (voiceWeatherToday){
                sb << 'Today\'s forecast calls for '
                def formattedWeather = isMetric ? weather.forecast.txt_forecast.forecastday[0].fcttext_metric : weather.forecast.txt_forecast.forecastday[0].fcttext 
                sb << formattedWeather[0].toLowerCase() + formattedWeather.substring(1) + " "
            }
            if (voiceWeatherTonight){
                sb << 'For tonight\'s forecast you can expect '
                def formattedWeather = isMetric ? weather.forecast.txt_forecast.forecastday[1].fcttext_metric : weather.forecast.txt_forecast.forecastday[1].fcttext
                sb << formattedWeather[0].toLowerCase() + formattedWeather.substring(1) + " "
            }
            if (voiceWeatherTomorrow){
                sb << 'Tomorrow your forecast is '
				def formattedWeather = isMetric ? weather.forecast.txt_forecast.forecastday[2].fcttext_metric : weather.forecast.txt_forecast.forecastday[2].fcttext
                sb << formattedWeather[0].toLowerCase() + formattedWeather.substring(1) + " "
            }
            msg = sb.toString()
            translateTxt().each {msg = msg.replaceAll(it.txt,it.cvt)}
        }
        if (voiceSunrise || voiceSunset){
            def todayDate = new Date()
            def s = getSunriseAndSunset(zipcode: zipCode, date: todayDate)	
            def riseTime = parseDate(s.sunrise.time, 'h:mm a')
            def setTime = parseDate(s.sunset.time, 'h:mm a')
            def currTime = now()
            def verb1 = currTime >= s.sunrise.time ? 'rose' : 'will rise'
            def verb2 = currTime >= s.sunset.time ? 'set' : 'will set'
            if (voiceSunrise && voiceSunset) msg += "The sun ${verb1} this morning at ${riseTime} and ${verb2} at ${setTime}. "
            else if (voiceSunrise && !voiceSunset) msg += "The sun ${verb1} this morning at ${riseTime}. "
            else if (!voiceSunrise && voiceSunset) msg += "The sun ${verb2} tonight at ${setTime}. "
        }
    }
    else  msg = 'Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather forecasts. '
    return msg
}
def getMoonInfo(){
	def msg = "", dir, nxt, days, sss =""
    if (location.timeZone || zipCode) {
        def moon = getWeatherFeature( 'astronomy', zipCode ).moon_phase
        def m = moon.ageOfMoon.toInteger()
		msg += "The moon is ${m} days old at ${moon.percentIlluminated}%, "
        if (m < 8) {
            dir = 'Waxing' 
            nxt = 'First Quarter'
            days = 8 - m
        } else if (m < 15) {
        	dir = 'Waxing'
            nxt = 'Full'
            days = 15 - m
        } else if (m < 23) {
        	dir = 'Waning'
            nxt = 'Third Quarter'
			days = 22 - m
        } else {
            dir = 'Waning'
            nxt = 'New'
            days = 29 - m
        }
        if (days.toInteger() != 1) sss = 's'
        switch (moon.percentIlluminated.toInteger()) {
            case 0:
                msg += 'New Moon, and the next First Quarter moon is in 7 days. '
                break
            case 1..49:
                msg += "${dir} Crescent, and the next ${nxt} moon is "
                if (days == 0) msg+= "later today. " else msg+= " in ${days} day${sss}. "               
                break
            case 50:
                if (dir == "Waxing") msg += "First Quarter, " else msg += "Third Quarter, "
                msg += "and the next ${nxt} Moon is in ${days} day${sss}. "
                break
            case 51..99:
                msg += "${dir} Gibbous, and the next ${nxt} moon is "
                if (days == 0) msg += "later today. " else msg += "in ${days} day${sss}. "
                break
            case 100:
                msg += 'Full Moon, and the next Third Quarter moon is in 7 days. '
                break
            default:
                msg += '. '
        }
    }
    else  msg = 'Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive lunar information. '
    return msg
}
def weatherAlerts(){
	def msg = "", brief = false
    if (location.timeZone || zipCode) {
        def alerts = getWeatherFeature('alerts', zipCode).alerts
        if ( alerts.size() > 0 ) {
            if ( alerts.size() == 1 ) msg += 'There is one active advisory for this area. '
            else msg += "There are ${alerts.size()} active advisories for this area. "
            def warn
            if (voiceWeatherWarnFull) {
                if (alerts[0].date_epoch == 'NA') {
                    def explained = []
                    alerts.each {
                        msg += "${it.wtype_meteoalarm_name} Advisory"
                        if (it.level_meteoalarm != "") msg += ", level ${it.level_meteoalarm}"
                        if (it.level_meteoalarm_name != "") msg += ", color ${it.level_meteoalarm_name}"
                        msg += ". "
                        if (brief) warn = " ${it.description} Advisory issued ${it.date}, expires ${it.expires}. "
                        else {
                            if (it.level_meteoalarm == "") {
                                if (it.level_meteoalarm_description != "") msg += "${it.level_meteoalarm_description} "
                            } else if (!explained.contains(it.level_meteoalarm)) {
                                if (it.level_meteoalarm_description != "") msg += "${it.level_meteoalarm_description} "                       	
                                    explained.add(it.level_meteoalarm)
                                }
                                warn = "${it.description} This advisory was issued on ${it.date} and it expires on ${it.expires}. "
                        }
                        warn = warn.replaceAll("kn\\, ", ' knots, ').replaceAll('Bft ', ' Beaufort level ').replaceAll("\\s+", ' ').trim()
                        if (!warn.endsWith(".")) warn += '.'
                        msg += "${warn} " 
                    }
                } else {
                    alerts.each { alert ->
                    	def desc
                    	if (alert.description.startsWith("A")) desc = 'An '
                    	else desc = 'A ' 
                    	desc += "${alert.description} is in effect from ${alert.date} until ${alert.expires}. "
                        msg += desc.replaceAll(/ 0(\d,) /, / $1 /)													// Fix "July 02, 2016" --> "July 2, 2016"
                        if ( !brief ) {
                          	warn = alert.message.replaceAll("\\.\\.\\.", ', ').replaceAll("\\* ", ' ') 				// convert "..." and "* " to a single space (" ")
                            warn = warn.replaceAll( "\\s+", ' ')													// remove extra whitespace
                            def warnings = [] 																		// See if we need to split up the message (multiple warnings are separated by a date stamp)
                            def i = 0
                            while ( warn != "" ) {
                            	def ddate = warn.replaceFirst(/(?i)(.+?)(\d{3,4} (am|pm) .{3} .{3} .{3} \d? \d{4})(.*)/, /$2/)
                            	if ( ddate && (ddate.size() != warn.size())) {
                            		def d = warn.indexOf(ddate)
                                	warnings[i] = warn.take(d-1)
                                	warn = warn.drop(d+ddate.size())
                                    i=i+1
                                } else {
                                	warnings[i] = warn
                                   	warn = ""
                                }
                            }
                            def headline = ""
                            warnings.each { warning ->
                            	def b = 1
                                def e = warning.indexOf(',', b+1)
                                if (e>b) {
                                	def head = warning.substring(b, e)												// extract the advisory headline 
                                    if (head.startsWith( ', ')) head = head - ', '
                               		if (i!=0) {																		// if more than one warning, check for repeats.
                               			if (headline == "") {
                                			headline = head															// first occurance
                                			warn = head + '. '
                                            warning = warning.drop( e+2 )											// drop the headline 
                                		} else if (head != headline) {												// different headline
                                			warn = head + '. '
                                            warning = warning.drop( e+2 )											// drop the headline 
                                		} else { 
                                        	warn = ""
                                        }																			// headlines are the same, drop this warning[]
									} else {
                            			warn = head + '. '															// only 1 warning in this Advisory
                                        warning = warning.drop( e+2 )												// drop the headline 
                            		}
                                } else {																			// no headline in this message
                                	warn = ' '																		// No header, let fall through
                                }
                                if (warn != "") {																	// good warning - let's clean it up
                                	def m
                                	warning = warning.replaceAll(/(?i) (\d{1,2})(\d{2}) (am|pm) /, / $1:$2 $3 / )	// fix time for Alexa to read 
									warn = warn.replaceAll(/(?i) (\d{1,2})(\d{2}) (am|pm) /, / $1:$2 $3 / )
                                    def table = warning.replaceFirst("(?i).*(Fld\\s+observed\\s+forecast).*", /$1/)
                           			if (table && (table.size() != warning.size())) {
                                    	m = warning.indexOf( table )
                                        if (m>0) warning = warning.take(m-1)
                                    }
									def latlon = warning.replaceFirst("(?i).+(Lat, Lon).+", /$1/)
									if (latlon && (latlon.size() != warning.size())) {
                                    	m = warning.indexOf( latlon )
										if (m>0) warning = warning.take(m-1)
                                    }
                                    warning = warning.replaceFirst("(.+\\.)(.*)", /$1/)								// strip off Alert author, if present
                           			warning = warning.replaceAll(/\/[sS]/, /\'s/).trim()							// fix escaped plurals, and trim excess whitespace
									if (!warning.endsWith('.')) warning += '.'										// close off this warning with a period                            			
                           			msg += warn + warning + ' '
                           			warn = ""
                                }
                            }
                        }
                    }	
                }
            }
            else msg += 'Configure your SmartApp to give you full advisory information. '
        }
    }
    else msg = 'Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive advisory information. '
    translateTxt().each {msg = msg.replaceAll(it.txt,it.cvt)}
    return msg
}
//Translate Maxtrix-----------------------------------------------------------
def translateTxt(){
	def wordCvt=[]
    wordCvt <<[txt:" N ",cvt: " north "] << [txt:" S ",cvt: " south "] << [txt:" E ",cvt: " east "] << [txt:" W ",cvt: " west "]<< [txt:" ESE ",cvt: " east-south east "]
    wordCvt <<[txt:" NW ",cvt: " northwest "] << [txt:" SW ",cvt: " southwest "] << [txt:" NE ",cvt: " northeast "] << [txt:" SE ",cvt: " southeast "]
	wordCvt <<[txt:" NNW ",cvt: " north-north west "] << [txt:" SSW ",cvt: " south-south west "] << [txt:" NNE ",cvt: " north-north east "] << [txt:" SSE ",cvt: " south-south east "]
	wordCvt <<[txt:" WNW ",cvt: " west-north west "] << [txt:" WSW ",cvt: " west-south west "] << [txt:" ENE ",cvt: " east-north east "]
	wordCvt <<[txt: /(\d+)(C|F)/, cvt: '$1 degrees'] << [txt: "(?i)mph", cvt: 'mi/h']<<[txt: "(?i)kph", cvt: 'km/h'] <<[txt: /\\.0 /, cvt: ' '] << [txt: /(\\.\d)0 /, cvt: /$1 /]
}
//Send Messages-----------------------------------------------------------
def sendMSG(num, msg, push, recipients){
    if (location.contactBookEnabled && recipients) sendNotificationToContacts(msg, recipients)
    else {
    	if (num) {sendSmsMessage(num,"${msg}")}
    	if (push) {sendPushMessage("${msg}")}
    }
}
//Toggle states (off -> on, on -> off)-----------------------------------------------------------
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
//Common Code (Parent)---------------------------------
def getMacroList(callingGrp){
    def result = []
    childApps.each{child->
    	if (child.macroType !="GroupM" && child.macroType!="Group") result << ["${child.label}": "${child.label} (${child.macroType})"]
    }
    return result
}
def coreHandler(evt) {
	log.debug "Refreshing CoRE Piston List"
    if (evt.value =="refresh") { 
    	state.CoREPistons = evt.jsonData && evt.jsonData?.pistons ? evt.jsonData.pistons : []
	}
}
def getCoREMacroList(){
    def result =[]
	childApps.each{child-> if (child.macroType !="CoRE") result << child.label }
    return result
}
def getVariableList(){
	def result = []
	def temp = voiceTempVar ? getAverage(voiceTempVar, "temperature") : "undefined device"
    def humid = voiceHumidVar ? getAverage(voiceHumidVar, "humidity") + " percent relative humidity"	: "undefined device"
    def present = voicePresenceVar.findAll{it.currentValue("presence")=="present"}
   	def people = present ? getList(present) : "No people present"
    result << [temp: temp, humid: humid, people: people]
}
private getAverage(device,type){
	def total = 0
	device.each {total += it.latestValue(type)}
    def result = Math.round(total/device.size())
}
def getTstatLimits() { return [hi:tstatHighLimit, lo: tstatLowLimit] }
def getAdvEnabled() { return advReportOutput }
def isNest() { return nestCMD }
def isStelpro() { return stelproCMD }
def OAuthToken(){
	try {
        createAccessToken()
		log.debug "Creating new Access Token"
	} catch (e) { log.error "Access Token not defined. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth." }
}
def macroDesc(){def results = childApps.size() ? childApps.size()==1 ? "One Voice Macro Configured" : childApps.size() + " Voice Macro Configured" : "No Voices Macros Configured\nTap to create new macro"}
def getDesc(param1, param2, param3) {def result = param1 || param2 || param3 ? "Status: CONFIGURED - Tap to edit/view" : "Status: UNCONFIGURED - Tap to configure"}
def getDescMRS() {
	def mStat=modes ? "On": "Off", rStat=routines ? "On": "Off", sStat=SHM ? "On": "Off"
    def result = modes || routines || SHM ? "Modes: ${mStat}, SHM: ${sStat}, Routines: ${rStat}" : "Status: UNCONFIGURED - Tap to configure"
} 
def fillColorSettings(){ 
	def colorData = []
    colorData << [name: "White", hue: 10, sat: 56] << [name: "Orange", hue: 8, sat: 100] << [name: "Red", hue: 100, sat: 100]
    colorData << [name: "Green", hue: 37, sat: 100] << [name: "Blue", hue: 64, sat: 100] << [name: "Yellow", hue: 16, sat: 100] 
    colorData << [name: "Purple", hue: 78, sat: 100] << [name: "Pink", hue: 87, sat: 100]
    if (customName && (customHue > -1 && customerHue < 101) && (customSat > -1 && customerSat < 101)){
    	colorData << [name: customName, hue: customHue as int, sat: customSat as int]
    }
    state.colorData=colorData
}
def getList(items){
	def result = "", itemCount=items.size() as int
	items.each{ result += it; itemCount=itemCount-1	
		result += itemCount>1 ? ", " : itemCount==1 ? " and " : ""
    }
	return result
}
def getDeviceList(){
	def result = []
    try {
        if (switches) switches.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type: "switch", devices: switches] }
        if (dimmers) dimmers.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"level", devices: dimmers]  }
        if (cLights) cLights.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"color", devices: cLights] }
        if (doors) doors.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"door", devices: doors]  }
        if (locks) locks.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"lock", devices: locks] }
        if (tstats) tstats.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"thermostat", devices: tstats] }
        if (speakers) speakers.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"music", devices: speakers] }
        if (temps) temps.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"temperature", devices: temps] }
        if (humid) humid.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"humidity", devices: humid] }
        if (ocSensors) ocSensors.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"contact", devices: ocSensors] }
        if (water) water.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"water", devices: water] }
        if (motion) motion.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"motion", devices: motion] }
        if (presence) presence.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type:"presence", devices: presence] }
    }
    catch (e) {
    	log.warn "There was an issue parsing the device labels. Be sure all of the devices are uniquely named/labeled and that none of them are blank (null). "
    }
    return result
}
def findNullDevices(){
	def result=""
    if (switches) switches.each { result += !it.label ? it.name + "\n" : "" }
    if (dimmers) dimmers.each{ result += !it.label ? it.name + "\n" : "" }
	if (cLights) cLights.each{ result += !it.label ? it.name + "\n" : "" }
	if (doors) doors.each{ result += !it.label ? it.name + "\n" : "" }
	if (locks) locks.each{ result += !it.label ? it.name + "\n" : "" }
	if (tstats) tstats.each{ result += !it.label ? it.name + "\n" : "" }
	if (speakers) speakers.each{ result += !it.label ? it.name + "\n" : "" }
	if (temps) temps.each{ result += !it.label ? it.name + "\n" : "" }
	if (humid) humid.each{ result += !it.label ? it.name + "\n" : "" }
	if (ocSensors) ocSensors.each{ result += !it.label ? it.name + "\n" : "" }
	if (water) water.each{ result += !it.label ? it.name + "\n" : "" }
	if (motion) motion.each{ result += !it.label ? it.name + "\n" : "" }
	if (presence) presence.each{ result += !it.label ? it.name + "\n" : "" }
	if (result) result = "You have the following device(s) with a blank (null) label:\n\n" + result + "\nBe sure all of the devices are uniquely labeled and that none of them are blank(null)."
    return result
}
def fillTypeList(){
	state.deviceTypes =["reports","report","switches","switch","dimmers","dimmer","colored lights","color","colors","speakers","speaker","water sensor","water sensors","water",
    	"lock","locks","thermostats","thermostat","temperature sensors","modes","routines","smart home monitor","SHM","security","temperature","door","doors", "humidity", "humidity sensor", 
        "humidity sensors", "presence", "presence sensors", "motion", "motion sensor", "motion sensors", "door sensor", "door sensors", "window sensor", "window sensors", "open close sensors",
        "colored light", "events", "macro", "macros", "group", "groups", "voice reports", "voice report", "device group", "device groups","control macro", "control macros","control", "controls",
        "macro group","macro groups","device macros","device macro","device group macro","device group macros","core","core trigger","core macro","core macros","core triggers","sensor", "sensors"]    	  
}
def upDown(device, type, op, num){
    def numChange, newLevel, currLevel, defMove, txtRsp = ""
    if (type=="color" || type=="level") { defMove = lightAmt ? lightAmt : 0 ; currLevel = device.currentValue("switch")=="on" ? device.currentValue("level") as int : 0 } 
    if (type=="music") { defMove = speakerAmt ? speakerAmt : 0 ; currLevel = device.currentValue("level") as int }
    if (type=="thermostat") { defMove=tstatAmt ? tstatAmt : 0 ; currLevel =device.currentValue("temperature") as int }
    if (op == "increase" || op=="raise" || op=="up")  numChange = num == 0 ? defMove : num > 0 ? num : 0
    if (op == "decrease" || op=="down" || op=="lower") numChange = num == 0 ? -defMove : num > 0 ? -num  : 0
    newLevel = currLevel + numChange; newLevel = newLevel > 100 ? 100 : newLevel < 0 ? 0 : newLevel
    if ((type =="level" || type=="color") && defMove > 0 ){
        if (device.currentValue("switch")=="on") {
            if (newLevel < 100 && newLevel > 0 ) txtRsp="I am setting the ${device} to a new value of ${newLevel}%. "
            if (newLevel == 0) txtRsp= "The new value would be zero or below, so I am turning the ${device} off. "	
        }
        if (device.currentValue("switch")=="off") {
            if (newLevel == 0) txtRsp= "The ${device} is off. I am taking no action. "
            if (newLevel < 100 && newLevel > 0 ) txtRsp="I am turning ${device} on and setting it to level of ${newLevel}%. "
        }
    	if (newLevel == 100) txtRsp= currLevel < 99 ? "I am increasing the level of the ${device} to its maximum level. " : "The ${device} is at its maximum level. "      
    }
    else if (defMove == 0) txtRsp = "The default increase or decrease value is set to zero within the SmartApp. I am taking no action. "
    return [newLevel: newLevel, msg:txtRsp]
}
def getLastEvent(device, count) {
    def lastEvt= device.events(), eDate, eDesc, today, eventDay, voiceDay, i , evtCount = 0
    def result = ""
    for( i = 0 ; i < 10 ; i++ ) {
        eDate = lastEvt.date[i].getTime()
        eDesc = lastEvt.descriptionText[i]
    	if (eDesc) {
        	today = new Date(now()).format("EEEE, MMMM dd, yyyy", location.timeZone)
    		eventDay = new Date(eDate).format("EEEE, MMMM dd, yyyy", location.timeZone)
    		voiceDay = today == eventDay ? "Today" : "On " + eventDay
    		result += voiceDay + " at " + new Date(eDate).format("h:mm aa", location.timeZone) + " the event was: " + eDesc + ". "
   	 		evtCount ++
            if (evtCount == count) break
        }
	}
    def diff = count - evtCount
	result = evtCount>1 ? "The following are the last ${evtCount} events for the ${device}: " + result : "The last event for the ${device} was: " + result
    result += diff > 1 ? "There were ${diff} items that were skipped due to the description being empty. " : diff==1 ? "There was one item that was skipped due to the description being a null value. " : ""
    if (evtCount==0) result="There were no past events in the device log. "
    return result
}
def sendJSON(outputTxt, lVer){
    def LambdaVersion = lVer as int, contOption=""
    if (LambdaVersion < 115) outputTxt = "I am unable to complete your request. The version of the Lambda code you are using is out-of-date. Please install the latest code and try again. "
    log.debug outputTxt
    contOption = contError ? "1" : "0"
    contOption += contStatus ? "1" : "0"
    contOption += contAction ? "1" : "0"
    contOption += contMacro ? "1" : "0"
    return ["voiceOutput":outputTxt,"continue":contOption]
}
//Version/Copyright/Information/Help-----------------------------------------------------------
private def textAppName() { def text = "Ask Alexa" }	
private def textVersion() {
    def version = "SmartApp Version: 2.0.5 (07/09/2016)"
    def lambdaVersion = state.lambdaCode ? "\n" + state.lambdaCode : ""
    return "${version}${lambdaVersion}"
}
private def versionInt(){ return 205 }
private def versionLong(){ return "2.0.5" }
private def textCopyright() {return "Copyright © 2016 Michael Struck" }
private def textLicense() {
	def text = "Licensed under the Apache License, Version 2.0 (the 'License'); "+
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
	def text = "This SmartApp provides an interface to control and "+
    	"query the SmartThings environment via the Amazon Echo ('Alexa'). "+
    	"For more information, go to http://thingsthataresmart.wiki/index.php?title=Ask_Alexa."
}
