/**
 *  Google Home Helper
 *
 *  Copyright © 2016 Michael Struck
 *  Version 1.0.0 12/1/16
 * 
 *  Version 1.0.0 (12/1/16) - Initial release
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
definition(  
    name: "Google Home Helper${parent ? " - Scenario " : ""}",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    parent: parent ? "MichaelStruck.Google Home Helper" : null,
    description: "Allows for various SmartThings devices to be tied to switches controlled by Google Home.",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/google-home-helper.src/GoogleHome.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/google-home-helper.src/GoogleHome@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/google-home-helper.src/GoogleHome@2x.png")
preferences {
    page name:"pageMain"
    //Parent menu pages
    page name:"mainPageParent"
    page name:"pageAbout"
    page name:"pageSettings"
    page name:"pageSwitches"
    page name:"pageAddSwitch"
    //Child menu pages
    page name: "mainPageChild"
	page name: "pageControl"
	page name: "onPageSTDevices"
	page name: "offPageSTDevices"
	page name: "pageSpeaker"
	page name: "pagePanic"
	page name: "pageThermostat"
    page name: "pageVoice"
    page name: "pageTempReport"
    page name: "pageHomeReport"
    page name: "onPageHTTP"
    page name: "offPageHTTP"
}
def pageMain() { if (!parent) mainPageParent() else mainPageChild() }
//Show main page
def mainPageParent() {
	dynamicPage(name: "mainPageParent", title: "Google Home Helper Scenarios", install: true, uninstall: false) {
		section {
			app(name: "childScenarios", appName: "Google Home Helper", namespace: "MichaelStruck", title: "Create New Google Home Scenario...", multiple: true)
		}
		section("Options") {
			href "pageSettings", title: "Configure Settings", description: "Tap to configure app settings", image: imgURL() + "settings.png"
            if (showAddSwitches) {
            	def titleTxt = getChildDevices().size() > 0 ? "Add/View Virtual Switches" : "Add Virtual Switches"
                def descTxt = getChildDevices().size() > 1 ? "${getChildDevices().size()} virtual switches created" : getChildDevices().size() == 1 ? "One virtual switch created" : "Tap to add virtual switches"
                href "pageSwitches", title: "${titleTxt}", description: "${descTxt}", image: imgURL() + "add.png"
			}
            href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application", image: imgURL() + "info.png"
		}
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
		section {
        	paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/google-home-helper.src/GoogleHome@2x.png"
        }
        section ("SmartApp/Switch Versions") { paragraph "${textVersion()}" }    
        section ("Apache License") { paragraph "${textLicense()}"}
    	section("Instructions") { paragraph textHelp()}
        section("Tap below to remove all scenarios, switches and application"){
        }
	}
}
def pageSettings(){
    dynamicPage(name: "pageSettings", install: false, uninstall: false) {
    	section { paragraph "Configure Settings", image: imgURL() + "settings.png" }
        section (" ") {
            input "speakerSonos", "bool", title: "Show Sonos options", defaultValue: false, submitOnChange:true
			if (speakerSonos) input "memoryCount", "enum", title: "Maximum number of Sonos memory slots", options: [2:"2",3:"3",4:"4",5:"5",6:"6",7:"7",8:"8"], defaultValue: 2, required: false
            input "tstatNest", "bool", title: "Show Nest options", defaultValue: false
            input "showRestrictions", "bool", title: "Show scenario restrictions", defaultValue: true
            input "showAddSwitches", "bool", title: "Allow in-app virtual switch creation", defaultValue: false
        	input "showNotifyFeed", "bool", title: "Post activity to notification feed" , defaultValue: false
        }
    }
}
def pageSwitches() {
    dynamicPage(name: "pageSwitches", install: false, uninstall: false) {
    	section { paragraph "Add/View Virtual Switches", image: imgURL() + "add.png" }
        section("New switch information"){
            input "addSwitchName", "text", title: "Switch Label", description: "Enter a unique label name for the virtual switch", required: false, submitOnChange:true
            input "addSwitchType", "enum", title: "Switch Type...", description: "Choose a switch type", options:["Google Switch","Momentary Button Tile-Google"], required: false, submitOnChange:true	
            if (addSwitchType && addSwitchName) href "pageAddSwitch",title: "Add Switch", description: "Tap to add this switch", image: imgURL() + "add.png"
        }        
        def switchList = ""
        state.sw1Ver = ""
        state.sw2Ver = ""
        def count= getChildDevices().size(), noun = "${count} switches"
        if (count > 0) {
        	if (count == 1) noun = "One switch"
            getChildDevices().each {
            	if (it.typeName=="Google Switch" && state.sw1Ver == "") state.sw1Ver = "Google Switch Version: ${it.versionNum()}"
                if (it.typeName=="Momentary Button Tile" && state.sw2Ver == "") state.sw2Ver = "Momentary Button Tile Version: ${it.versionNum()}"
                switchList += "${it.label} (${it.typeName})"
                count --
                if (count>0) switchList +="\n"
            }
			section ("${noun} created within Google Home Helper"){paragraph switchList}	
        }
    }
}
// Show "pageAddSwitch" page
def pageAddSwitch() {
	dynamicPage(name: "pageAddSwitch", title: "Add Switch", install: false, uninstall: false) {
    	def repsonse
        if (getChildDevices().find{it.label == addSwitchName}) repsonse="There is already a switch labled '${addSwitchName}'.\n\nTap Done to go back and change the switch label name."
        else repsonse = !addSwitchName || !addSwitchType ? "Switch label name or type not specified.\n\nTap Done to go back and enter the switch information" :  addChildSwitches()
        section {paragraph repsonse}
    }
}
//Child Menu Items-------------------------------------------
def mainPageChild() {
	dynamicPage(name: "mainPageChild", title: "Scenario Settings", uninstall: true, install: true) {
		section {
			label title:"Scenario Name", required:true
    	   	input "scenarioType", "enum", title: "Scenario Type...", options: [["Baseboard":"Baseboard Heater Control"],["Thermostat":"Heating/Cooling Thermostat Control"],["Control":"Modes/Routines/Devices/HTTP/SHM Control"],["Panic":"Panic Commands"],["Speaker":"Speaker Control"],["Voice":"Voice Reporting"]], required: false, multiple: false, submitOnChange:true
        	def fullScenarioName = [Baseboard:"Baseboard Heater",Thermostat:"Heating/Cooling Thermostat",Control:"Control Scenario",Panic:"Panic Commands",Speaker:"Speaker",Voice:"Voice Reporting"][scenarioType] ?: scenarioType
            if (scenarioType) href "page${scenarioType}", title: "${fullScenarioName} Settings", description: scenarioDesc(), state: greyOutScen()
		}
		if (scenarioType && parent.showRestrictions){
			section("Restrictions", hideable: true, hidden: !(runDay || timeIntervalInput || runMode)) {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: imgURL() + "calendar.png"
        		href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: greyOutState(timeStart, timeEnd,""), image: imgURL() + "clock.png"
            	input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: imgURL() + "modes.png"
			}
        }
        section("Tap below to remove this scenario"){}
	}
}
page(name: "timeIntervalInput", title: "Only during a certain time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
	}
} 
// Show "pageControl" page
def pageControl() {
	dynamicPage(name: "pageControl", install: false, uninstall: false) {
        section { paragraph "Control Scenario Settings", image: imgURL() + "control.png" }
        section ("Switch Selection"){
			input "GoogleSwitch", "capability.switch", title: "Control Switch (On/Off, Momentary)", multiple: false, required: true, image: imgURL() + "dimmer.png"
    		input "showOptions", "enum", title: "Switch States To React To...", options: ["":"On/Off", "1":"On Only", "2":"Off Only"] , required: false, submitOnChange:true, defaultValue: ""
        }
        if (!showOptions || showOptions == "1") controlOnOff("on")
        if (!showOptions || showOptions == "2") controlOnOff("off")
    }
}
def controlOnOff(type){
	def phrases = location.helloHome?.getPhrases()*.label
	if (phrases) phrases.sort()	
    section ("When switch is ${type}..."){
		if (phrases) input "${type}Phrase", "enum", title: "Perform This Routine", options: phrases, required: false, image: imgURL() + "routine.png" 
        input "${type}Mode", "mode", title: "Set Mode To...", required: false, image: imgURL() + "modes.png"  
        input "${type}SHM", "enum",title: "Set Smart Home Monitor To...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false, image: imgURL() + "SHM.png"
        href "${type}PageSTDevices", title: "SmartThings Device Control...", description: getDeviceDesc("${type}"), state: getDeviceState("${type}"), image: imgURL() + "smartthings.png"
        href "${type}PageHTTP", title: "HTTP Request...", description: getHTTPDesc("${type}"), state: greyOutStateHTTP("${type}"), image: imgURL() + "network.png"
        input "${type}Delay", "number", title: "Delay (Minutes) To Activate After Trigger", defaultValue: 0, required: false, image: imgURL() + "stopwatch.png"
        input ("${type}Contacts", "contact", title: "Send Notifications To...", required: false, image: imgURL() + "sms.png") {
        	input "${type}SMSNum", "phone", title: "Send SMS Message (Phone Number)...", required: false, image: imgURL() + "sms.png"
        	input "${type}PushMsg", "bool", title: "Send Push Message", defaultValue: false
        }
		input "${type}SMSMsg", "text", title: "Message To Send...", required: false
	}
}
//Show "onPageHTTP" page
def onPageHTTP (){
    dynamicPage(name: "onPageHTTP", title: "HTTP Request", install: false, uninstall: false){
    	pageHTTPOnOff("on")
    }
}
//Show "offPageHTTP" page
def offPageHTTP (){
    dynamicPage(name: "offPageHTTP", title: "HTTP Request", install: false, uninstall: false){
    	pageHTTPOnOff("off")
    }
}
def pageHTTPOnOff(type){
	section{
    	input "${type}ExtInt", "enum", title: "Choose HTTP Command Type", options:[0:"External REST",1:"Internal (IP, port, command)"], submitOnChange:true , required: false
        if (settings."${type}ExtInt" == "0") input "${type}HTTP", "text", title:"HTTP Address...", required: false
    	else if (settings."${type}ExtInt" == "1"){
        	input "${type}IP", "text", title: "Internal IP Address", description: "IPv4 address xx.xx.xx.xx format", required: false
            input "${type}Port", "number", title: "Internal Port", description: "Enter a port number 0 to 65536", required: false 
            input "${type}Command", "text", title: "Command", description: "Enter REST commands", required: false 
		}
    }
}
// Show "onPageSTDevices" page
def onPageSTDevices(){
	dynamicPage (name: "onPageSTDevices", install: false, uninstall: false) {
		pageSTDevicesOnOff("on")
	}
}
// Show "offPageSTDevices" page
def offPageSTDevices(){
	dynamicPage (name: "offPageSTDevices", install: false, uninstall: false) {
		pageSTDevicesOnOff("off")
	}
}
def pageSTDevicesOnOff(type){
	section { paragraph "SmartThings Device Control", image: imgURL() + "smartthings.png"}
    section ("Switches"){
		input "${type}Switches", "capability.switch", title: "Control These Switches...", multiple: true, required: false, submitOnChange:true
		if (settings."${type}Switches") input "${type}SwitchesCMD", "enum", title: "Command To Send To Switches", options:["on":"Turn on","off":"Turn off", "toggle":"Toggle the switches' on/off state"], multiple: false, required: false
	}
	section ("Dimmers"){
		input "${type}Dimmers", "capability.switchLevel", title: "Control These Dimmers...", multiple: true, required: false , submitOnChange:true
		if (settings."${type}Dimmers") input "${type}DimmersCMD", "enum", title: "Command To Send To Dimmers", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the dimmers' on/off state"], multiple: false, required: false, submitOnChange:true
		if (settings."${type}DimmersCMD" == "set" && settings."${type}Dimmers") input "${type}DimmersLVL", "number", title: "Dimmers Level", description: "Set dimmer level", required: false, defaultValue: 0
	}
	section ("Colored Lights", hideWhenEmpty: true){
		input "${type}ColoredLights", "capability.colorControl", title: "Control These Colored Lights...", multiple: true, required: false, submitOnChange:true
		if (settings."${type}ColoredLights") input "${type}ColoredLightsCMD", "enum", title: "Command To Send To Colored Lights", options:["on":"Turn on","off":"Turn off","set":"Set color and level", "toggle":"Toggle the lights' on/off state"], multiple: false, required: false, submitOnChange:true
		if (settings."${type}ColoredLightsCMD" == "set" && settings."${type}ColoredLights"){
			input "${type}ColoredLightsCLR", "enum", title: "Choose A Color...", required: false, multiple:false, options: fillColorSettings().name, submitOnChange:true
			if (settings."${type}ColoredLightsCLR" == "Custom-User Defined"){
				input "${type}HueUserDefined", "number", title: "Colored Lights Hue", description: "Set colored light hue (0 to 100)", required: false, defaultValue: 0
				input "${type}SatUserDefined", "number", title: "Colored Lights Saturation", description: "Set colored lights saturation (0 to 100)", required: false, defaultValue: 0
			}
			input "${type}ColoredLightsLVL", "number", title: "Colored Light Level", description: "Set colored lights level", required: false, defaultValue: 0
		}
	}
	section ("Thermostats"){
		input "${type}Tstats", "capability.thermostat", title: "Control These Thermostats...", multiple: true, required: false, submitOnChange:true
		if (settings."${type}Tstats") input "${type}TstatsCMD", "enum", title: "Command To Send To Thermostats", options:["heat":"Set heating temperature","cool":"Set cooling temperature"], multiple: false, required: false, submitOnChange:true
		if (settings."${type}TstatsCMD") input "${type}TstatLVL", "number", title: "Temperature Level", description: "Set temperature level", required: false
	}
	section ("Locks"){
		input "${type}Locks","capability.lock", title: "Control These Locks...", multiple: true, required: false, submitOnChange:true
		if (settings."${type}Locks") input "${type}LocksCMD", "enum", title: "Command To Send To Locks", options:["lock":"Lock","unlock":"Unlock"], multiple: false, required: false
	}
	section("Garage Doors"){
		input "${type}Garages","capability.garageDoorControl", title: "Control These Garage Doors...", multiple: true, required: false, submitOnChange:true
		if (settings."${type}Garages") input "${type}GaragesCMD", "enum", title: "Command To Send To Garage Doors", options:["open":"Open","close":"Close"], multiple: false, required: false
	}
}
// Show "Panic" page
def pagePanic() {
	dynamicPage(name: "pagePanic", install: false, uninstall: false) {
        section {paragraph "Panic Commands Settings", image: imgURL() + "emergency.png"}
        section ("Switch Selection") {
			input "panicSwitchOn", "capability.momentary", title: "ON Control Switch (Momentary)", multiple: false, required: true, submitOnChange:true,image: imgURL() + "button.png"
          	if (panicSwitchOn) input "panicSwitchOff", "capability.momentary", title: "OFF Control Switch (Momentary)", multiple: false, required: false, submitOnChange:true,image: imgURL() + "button.png"
        }
        section ("When panic is activated...", hideWhenEmpty: true){
        	input "alarm", "capability.alarm", title: "Activate Alarms...", multiple: true, required: false, submitOnChange:true
            if (parent.speakerSonos) input "alarmSonos", "capability.musicPlayer", title: "Use Sonos As Alarm...", multiple: false , required: false , submitOnChange:true,image: imgURL() + "speaker.png"
            if (alarm){
            	input "alarmType", "enum", title: "Select Alarm Type", options: ["strobe":"Strobe light", "siren":"Siren", "both":"Both stobe and siren"], multiple: false, required: false  
            	input "alarmTimer", "number", title:"Alarm Turns Off Automatically After (Minutes)", required: false,image: imgURL() + "stopwatch.png"
            }
            if (alarmSonos && parent.speakerSonos  && alarmSonos.name.contains("Sonos")){
                input "alarmSonosVolume", "number", title:"Sonos Alarm Volume", required: false, image: imgURL() + "volume.png"
                input "alarmSonosSound", "enum", title:"Sonos Alarm Sound", options: [1:"Alarm 1-European Siren", 2:"Alarm 2-Sci-Fi Siren", 3:"Alarm 3-Police Car Siren", 4:"Alarm 4-Red Alert",5:"Custom-User Defined"], multiple: false, required: false, submitOnChange:true 
                if (alarmSonosSound == "5") input "alarmSonosCustom", "text", title:"URL/Location Of Custom Sound...", required: false
                input "alarmSonosTimer", "number", title:"Alarm Turns Off Automatically After (Seconds)", required: false,image: imgURL() + "stopwatch.png"
            }
            if (alarmSonos && parent.speakerSonos  && !alarmSonos.name.contains("Sonos")){
            	paragraph "You have chosen a speaker for your alarm that is not supported. Currently, only Sonos speakers can be used as alarms. Please choose a Sonos speaker."
            }
            input ("panicContactsOn", "contact", title: "Send Notifications To...", required: false,image: imgURL() + "sms.png") {
            	input "panicSMSnumberOn", "phone", title: "Send SMS Message To (Phone Number)...", required: false,image: imgURL() + "sms.png"
            	input "panicPushOn", "bool", title: "Send Push Message", defaultValue: false
            }
            input "panicSMSMsgOn","text",title: "Message To Send...", required: false
        }
        if (panicSwitchOn && panicSwitchOff){
        	section ("When panic is deactivated..."){
        		if (alarm || alarmSonos) input "alarmOff", "bool", title: "Turn Off Alarm?", defaultValue: false
            	input ("panicContactsOff", "contact", title: "Send Notifications To...", required: false,image: imgURL() + "sms.png") {
                	input "panicSMSnumberOff", "phone", title: "Send SMS Message To (Phone Number)...", required: false,image: imgURL() + "sms.png"
                	input "panicPushOff", "bool", title: "Send Push Message", defaultValue: false
            	}
                input "panicSMSMsgOff","text",title: "Message To Send...", required: false
        	}
        }
	}
}
// Show "pageSpeaker" page
def pageSpeaker(){
	dynamicPage(name: "pageSpeaker", install: false, uninstall: false) {
		section { paragraph "Speaker Settings", image: imgURL() + "speaker.png" }
        section ("Switch/Speaker Selection"){
        	input "vDimmerSpeaker", "capability.switchLevel", title: "Control Switch (Dimmer)", multiple: false, required:false, submitOnChange:true,image: imgURL() + "dimmer.png"
            input "speaker", "capability.musicPlayer", title: "Speaker To Control", multiple: false , required: false, submitOnChange:true,image: imgURL() + "speaker.png"
        }
    	section ("Speaker Volume Limits", hideable: true, hidden: !(upLimitSpeaker || lowLimitSpeaker || speakerInitial)) {        
        	input "upLimitSpeaker", "number", title: "Volume Upper Limit", required: false
    		input "lowLimitSpeaker", "number", title: "Volume  Lower Limit", required: false
        	input "speakerInitial", "number", title: "Volume When Speaker Turned On", required: false
		}
		section ("Speaker Track Controls", hideable: true, hidden: !(nextSwitch || prevSwitch)) {    
        	input "nextSwitch", "capability.momentary", title: "Next Track Switch (Momentary)", multiple: false, required: false,image: imgURL() + "button.png"
       		input "prevSwitch", "capability.momentary", title: "Previous Track Switch (Momentary)", multiple: false, required: false,image: imgURL() + "button.png"
    	}
        if (vDimmerSpeaker){
        	section ("Other Functions/Controls", hideable: true, hidden: !(speakerOnSwitches || speakerOffSwitches || speakerOffFunction)){
        		input "speakerOnSwitches", "capability.switch", title: "When Control Switch On, Turn On...", multiple: true, required: false
                input "speakerOffSwitches", "capability.switch", title: "When Control Switch Off, Turn off...", multiple: true, required: false
                input "speakerOffFunction", "bool", title: "Control Switch off action: Pause/Stop Playback", defaultValue: false
        	}
		}
        if (speaker && songOptions(1) && parent.speakerSonos && speaker.name.contains("Sonos")){
            for (int i = 1; i <=sonosSlots(); i++) {
                section ("Sonos Saved Station ${i}", hideable: true, hidden: !(settings."song${i}Switch" || settings."song${i}Station")){
					input "song${i}Switch", "capability.momentary", title: "Saved Station Switch #${i} (Momentary)", multiple: false, required: false, submitOnChange:true,image: imgURL() + "button.png"
                    if (settings."song${i}Switch") input "song${i}Station", "enum", title: "Song/Station #${i}", description: "Tap to select recently played song/station", multiple: false, 
						required: false, options: songOptions("${i}"), submitOnChange:true
					if (settings."song${i}Station") input "announce${i}Song", "bool", title: "Announce Song Name Prior To Playing", defaultValue: false
                }
			}
		}
        if (speaker && !songOptions(1) && parent.speakerSonos && speaker.name.contains("Sonos")){
        	section ("Sonos Saved Stations", hideable: true, hidden: false) {
            	paragraph "There are currently no songs available in the Sonos memory. Play a "+
                	"song or station through SmartThings, then come back to this app and the station list should be available"
            }
        }
	}
}
// Show "pageThermostat" page
def pageThermostat(){
    dynamicPage(name: "pageThermostat", install: false, uninstall: false) {
        section {paragraph "Heating/Cooling Thermostat Settings", image: imgURL() + "temp.png"}
        section ("Switch/Thermostat Selection"){
            input "vDimmerTstat", "capability.switchLevel", title: "Control Switch (Dimmer)", multiple: false, required:false, image: imgURL() + "dimmer.png"
            input "tstat", "capability.thermostat", title: "Thermostat To Control", multiple: false , required: false, image: imgURL() + "temp.png"
            input "autoControlTstat", "bool", title: "Control Thermostat In 'Auto' Mode", defaultValue: false
        }
        section ("Thermostat Temperature Limits", hideable: true, hidden:!(upLimitTstat ||lowLimitTstat)) {
            input "upLimitTstat", "number", title: "Thermostat Upper Limit", required: false
            input "lowLimitTstat", "number", title: "Thermostat Lower Limit", required: false
        }
        section ("Thermostat Setpoints\n(When controls turned on below)", hideable: true, hidden:!(heatingSetpoint ||coolingSetpoint)){
            input "heatingSetpoint", "number", title: "Heating Setpoint", required: false
            input "coolingSetpoint", "number", title: "Cooling Setpoint", required: false
        }
         section ("Thermostat Mode Controls", hideable: true, hidden:!(heatingSwitch || coolingSwitch || autoSwitch)) {
            input "heatingSwitch", "capability.momentary", title: "Heating Mode Switch (Momentary)", multiple: false, required: false,image: imgURL() + "button.png"
            input "coolingSwitch", "capability.momentary", title: "Cooling Mode Switch (Momentary)", multiple: false, required: false,image: imgURL() + "button.png"
            input "autoSwitch", "capability.momentary", title: "Auto Mode Switch (Momentary)", multiple: false, required: false,image: imgURL() + "button.png"
        }
        if (parent.tstatNest){
            section ("Nest Home/Away Controls", hideable: true, hidden:!(nestHome || nestAway)){
                input "nestHome", "capability.momentary", title: "Home Mode Switch (Momentary)", multiple: false, required: false,image: imgURL() + "button.png"
                input "nestAway", "capability.momentary", title: "Away Mode Switch (Momentary)", multiple: false, required: false,image: imgURL() + "button.png"
            }
        }
    }
}
// Show "pageBaseboard" page
page(name: "pageBaseboard", title: "Baseboard Heater Settings", install: false, uninstall: false) {
	section {paragraph "Baseboard Heater Settings", image: imgURL() + "heating.png"}
    section ("Switch/Baseboard Selection") {
		input "vDimmerBB", "capability.switchLevel", title: "Control Switch (Dimmer)", multiple: false, required:false,image: imgURL() + "dimmer.png"
		input "tstatBB", "capability.thermostat", title: "Thermostat To Control", multiple: true, required: false, image: imgURL() + "temp.png"
	}
	section ("Baseboard Temperature Limits", hideable: true, hidden:!(upLimitTstatBB ||lowLimitTstatBB)) {
		input "upLimitTstatBB", "number", title: "Thermostat Upper Limit", required: false
		input "lowLimitTstatBB", "number", title: "Thermostat Lower Limit", required: false
	}
	section ("Baseboard On/Off Setpoints", hideable: true, hidden:!(setpointBBon ||setpointBBoff)){
		input "setpointBBon", "number", title: "Setpoint When Control Switch Turned On", required: false
		input "setpointBBoff", "number", title: "Setpoint When Control Switch Turned Off", required: false
	}
}
//Show "pageVoice" page
def pageVoice(){
    dynamicPage(name: "pageVoice", install: false, uninstall: false){
        section { paragraph "Voice Reporting Settings", image: imgURL() + "voice.png" }
        section ("Switch/Speaker Selection") {
            input "voiceControl", "capability.momentary", title: "Voice Report Control Switch (Momentary)", multiple: false, required: true,image: imgURL() + "button.png"
            input "voiceSpeaker", "capability.musicPlayer", title: "Voice Report Speaker", multiple: false, required: false, submitOnChange:true,image: imgURL() + "speaker.png"
            if (voiceSpeaker) input "voiceVolume", "number", title: "Speaker Volume", required: false, image: imgURL() + "volume.png"
            input "voiceDevice", "capability.speechSynthesis", title: "Voice Report Speech Synthesis Device", multiple: false, required: false, hideWhenEmpty: true
			input "voiceDelay", "number", title: "Delay (Minutes) After Trigger To Report", defaultValue: 0, required: false,image: imgURL() + "stopwatch.png"
            if (voiceSpeaker) input "voiceResume", "bool", title: "Resume Music/Track After Voice Report", defaultValue: false
            input "voiceNotification", "bool", title: "Push/SMS Notification Of Report", defaultValue: false, submitOnChange:true
            if (voiceNotification){
            	input ("voiceContacts", "contact", title: "Send Notifications To...", required: false) {
                	input "voiceSMSnumber", "phone", title: "Send SMS Message To (Phone Number)...", required: false
                	input "voicePush", "bool", title: "Send Push Message", defaultValue: false
            	}
			}
        }
        section ("Report Types"){
            input "voicePre", "text", title: "Pre Message Before Device Report", description: "Enter a message to play before the device report", defaultValue: "This is your SmartThings voice report for %time%, %day%, %date%.", required: false, capitalization: "sentences"
            href "pageSwitchReport", title: "Switch/Dimmer Report", description: reportDesc(voiceSwitch, voiceDimmer, ""), state: greyOutState(voiceSwitch, voiceDimmer, ""), image: imgURL() + "power.png"
            href "pagePresenceReport", title: "Presence Report", description: reportDesc(voicePresence, "", ""), state: greyOutState(voicePresence, "", ""), image : imgURL() + "people.png" 
            href "pageDoorReport", title: "Door/Window Report", description: reportDesc(voiceDoorSensors, voiceDoorControls, voiceDoorLocks), state: greyOutState(voiceDoorSensors, voiceDoorControls, voiceDoorLocks), image: imgURL() + "lock.png"
            href "pageTempReport", title: "Temperature/Thermostat Report", description: reportDesc(voiceTemperature, voiceTempSettings, voiceTempVar), state: greyOutState(voiceTemperature, voiceTempSettings, voiceTempVar),image: imgURL() + "temp.png"
            href "pageHomeReport", title: "Mode and Smart Home Monitor Report", description: reportDescMSHM(), state: greyOutState(voiceMode, voiceSHM, ""), image: imgURL() + "modes.png"
            input "voicePost", "text", title: "Post Message After Device Report", description: "Enter a message to play after the device report", required: false, capitalization: "sentences"
        }
    }
}
page(name: "pagePresenceReport", title: "Presence Report", install: false, uninstall: false){
	section {
        input "voicePresence", "capability.presenceSensor", title: "Presence Sensors To Report Their Status...", multiple: true, required: false 
        input "voicePresentOnly", "bool", title: "Report Only Sensors That Are 'Not Present'", defaultValue: false 
    }
}
page(name: "pageSwitchReport", title: "Switch/Dimmer Report", install: false, uninstall: false){
	section {
        input "voiceSwitch", "capability.switch", title: "Switches To Report Their Status...", multiple: true, required: false 
        input "voiceOnSwitchOnly", "bool", title: "Report Only Switches That Are On", defaultValue: false
        input "voiceDimmer", "capability.switchLevel", title: "Dimmers To Report Their Status...", multiple: true, required: false
        input "voiceOnDimmerOnly", "bool", title: "Report Only Dimmers That Are On", defaultValue: false 
    }
}
page(name: "pageDoorReport", title: "Door/Window Report", install: false, uninstall: false){
	section {
		input "voiceDoorSensors", "capability.contactSensor", title: "Doors/Windows Sensors To Report Their Status...", multiple: true, required: false
		input "voiceDoorControls", "capability.doorControl", title: "Door Controls To Report Their Status...", multiple: true, required: false
		input "voiceDoorLocks", "capability.lock", title: "Locks To Report Their Status...", multiple: true, required: false
        input "voiceDoorAll", "bool", title: "Report Door/Window Summary Even When All Are Closed And Locked", defaultValue: false
	}
}
def pageTempReport(){
    dynamicPage(name: "pageTempReport", title: "Temperature/Thermostat Report", install: false, uninstall: false){
        section {
            input "voiceTempVar", "capability.temperatureMeasurement", title: "Temperature Device Variable (%temp%)",multiple: false, required: false
            input "voiceTemperature", "capability.temperatureMeasurement", title: "Devices To Report Temperatures...",multiple: true, required: false
		}
        section ("Thermostat Setpoint Reporting") {
            input "voiceTempSettings", "capability.thermostat", title: "Thermostats To Report Their Setpoints...",multiple: true, required: false, submitOnChange:true
            if (voiceTempSettings) {
            	input "voiceTempSettingsType", "enum", title: "Which Setpoint To Report", defaultValue: "heatingSetpoint", 
                	options: ["heatingSetpoint": "Heating Setpoint","coolingSetpoint":"Cooling Setpoint","thermostatSetpoint":"Single Setpoint (Not compatible with all thermostats)"]
            	input "voiceTempSettingSummary", "bool", title: "Consolidate Thermostat Report", defaultValue: false, submitOnChange:true
            }
            if (voiceTempSettingSummary && voiceTempSettings) input "voiceTempTarget", "number", title: "Thermostat Setpoint Target", required: false, defaultValue: 50
        }
    }
}
def pageHomeReport(){
    dynamicPage(name: "pageHomeReport", title: "Mode And Security Report", install: false, uninstall: false){
        section {
            input "voiceMode", "bool", title: "Report SmartThings Mode Status", defaultValue: false
            input "voiceSHM", "bool", title: "Report Smart Home Monitor Status", defaultValue: false
        }
    }
}
//-----------------------------------------------------------
def installed() { initialize() }
def updated() {
    unschedule()
    unsubscribe()
    initialize()
}
def uninstalled(){ deleteChildSwitches() }
def initialize() { parent ? initializeChild() : initializeParent() }
private initializeParent(){
	childApps.each {child ->log.info "Installed Scenario: ${child.label}"}
}
private initializeChild(){
	if (scenarioType == "Control" && GoogleSwitch) subscribe(GoogleSwitch, "switch", switchHandler)
    if (scenarioType == "Thermostat"){
    	if (vDimmerTstat && tstat){
        	subscribe (vDimmerTstat, "switch.off", thermoOffHandler)
            subscribe (vDimmerTstat, "level", thermoHandler)
            if (heatingSwitch) subscribe (heatingSwitch, "switch.on", heatHandler)
            if (coolingSwitch) subscribe (coolingSwitch, "switch.on", coolHandler)
            if (autoSwitch) subscribe (autoSwitch, "switch.on", autoHandler)
		}
		if (parent.tstatNest){
			if (nestHome) subscribe(nestHome, "switch.on", nestHomeHandler)
			if (nestAway) subscribe(nestAway, "switch.on", nestAwayHandler)
		}
	}
    if (scenarioType == "Baseboard" && vDimmerBB && tstatBB){
    	subscribe (vDimmerBB, "switch", BBOnOffHandler)
    	subscribe (vDimmerBB, "level", BBHandler)
    }
	if (scenarioType == "Speaker" && vDimmerSpeaker && speaker) {
		subscribe (vDimmerSpeaker, "level", speakerVolHandler)
		subscribe (vDimmerSpeaker, "switch", speakerOnHandler)
		if (nextSwitch) subscribe (nextSwitch, "switch.on", controlNextHandler)
		if (prevSwitch) subscribe (prevSwitch, "switch.on", controlPrevHandler) 
		if (parent.speakerSonos && speaker.name.contains("Sonos")){
			for (int i = 1; i <= sonosSlots(); i++) {
				if (settings."song${i}Switch" && settings."song${i}Station"){
					saveSelectedSong(i,settings."song${i}Station")
					subscribe (settings."song${i}Switch", "switch.on", controlSong)
				}
			}
		}
	}
	if (scenarioType == "Panic"){
    	if (panicSwitchOn) subscribe (panicSwitchOn, "switch.on", panicOn)
        if (panicSwitchOff) subscribe (panicSwitchOff, "switch.on", panicOff) 
        if (alarmSonos && parent.speakerSonos && alarmSonos.name.contains("Sonos") && alarmSonosSound) getAlarmSound()
	}
    if (scenarioType == "Voice" && (voiceControl && (voiceDevice || voiceSpeaker)) || (voiceNotification && (voiceContacts || voiceSMSnumber || voicePush))) { 
    	subscribe(voiceControl, "switch.on", voiceHandler)
	}  
}
//Common modules (for adding switches)
def addChildSwitches(){
    def deviceID = "GHH_${app.id}_${getChildDevices().size()}"
    def nameSpace = "MichaelStruck"
    def result
    try {
		def childDevice = addChildDevice(nameSpace, addSwitchType, deviceID, null, [name: deviceID, label: addSwitchName, completedSetup: true])
		log.debug "Created Switch ${addSwitchName}: ${deviceID}"
        result ="The ${addSwitchType} named '${addSwitchName}' has been created.\n\nBe sure to include the switch in your Google Home app."
    } catch (e) {
		log.debug "Error creating switch: ${e}"
        result ="The ${addSwitchType} named '${addSwitchName}' could NOT be created.\n\nEnsure you have the correct device code installed and published within the IDE."
	}
	result + "\n\nTap Done to return to the switches page."   
}
def deleteChildSwitches() {
    getChildDevices().each {
    	log.debug "Deleting switch ID: " + it.deviceNetworkId
        try {
            deleteChildDevice(it.deviceNetworkId)
        } catch (e) {
            log.debug "Fatal exception ${e}"
        }
    }
}
//Child Code------------------------------------------------------------------------------------
//Mode/Routine/Devices/HTTP/SHM-----------------------------------------------------------------
def switchHandler(evt) {
    if (getOkToRun("Control Scenario on/off")) {    
        if (evt.value == "on" && getOkOnOptions()) {
            if (!onDelay || onDelay == 0) turnOnOff("on") 
            else {
            	runIn(onDelay*60, turnOn, [overwrite: true])
				if (parent.showNotifyFeed) sendNotificationEvent("Google Home Helper Scenario: '${app.label}' ON triggered. Will activate in ${onDelay} minutes.")
            }          
    	} 
    	else if (evt.value == "off" && getOkOffOptions()) {
        	if (!offDelay || offDelay == 0) turnOnOff("off") 
            else {
            	runIn(offDelay*60, turnOff , [overwrite: true])
                if (parent.showNotifyFeed) sendNotificationEvent("Google Home Helper Scenario: '${app.label}' OFF triggered. Will activate in ${offDelay} minutes.")
            }
    	}
	}
}
def turnOn() {turnOnOff("on")}
def turnOff() {turnOnOff("off")}
def turnOnOff(type){
	if (parent.showNotifyFeed) sendNotificationEvent("Google Home Helper Scenario: '${app.label}' ${type.toUpperCase()} activated.") 
    def cmd = [switch: settings."${type}SwitchesCMD", dimmer: settings."${type}DimmersCMD", cLight: settings."${type}ColoredLightsCMD", tstat: settings."${type}TstatsCMD", lock: settings."${type}LocksCMD", garage: settings."${type}GaragesCMD"]
    if (settings."${type}Phrase") location.helloHome.execute(settings."${type}Phrase")
	if (settings."${type}Mode") changeMode(settings."${type}Mode")
    if (settings."${type}Switches" && cmd.switch) cmd.switch == "toggle" ? toggleState(settings."${type}Switches") : settings."${type}Switches"?."${cmd.switch}"()
    if (settings."${type}Dimmers" && cmd.dimmer){
    	if (cmd.dimmer == "set"){
        	def level = settings."${type}DimmersLVL" < 0 || !settings."${type}DimmersLVL" ?  0 : settings."${type}DimmersLVL" >100 ? 100 : settings."${type}DimmersLVL" as int
        	settings."${type}Dimmers"?.setLevel(level)
        }
        else cmd.dimmer == "toggle" ? toggleState(settings."${type}Dimmers") : settings."${type}Dimmers"?."${cmd.dimmer}"()
    }
    if (settings."${type}ColoredLights" && cmd.cLight){
    	if (cmd.cLight == "set"){
            def level = !settings."${type}ColoredLightsLVL" || settings."${type}ColoredLightsLVL" < 0 ? 0 : settings."${type}ColoredLightsLVL" >100 ? 100 : settings."${type}ColoredLightsLVL" as int
            settings."${type}ColoredLightsCLR" ? setColoredLights(settings."${type}ColoredLights", settings."${type}ColoredLightsCLR", level, type) : settings."${type}ColoredLights"?.setLevel(level)
        }
        else if (cmd.cLight == "toggle") toggleState(settings."${type}ColoredLights")	
        else settings."${type}ColoredLights"?."${cmd.cLight}"()
    }
    if (settings."${type}Locks" && cmd.lock) settings."${type}Locks"?."${cmd.lock}"()
    if (settings."${type}Tstats" && settings."${type}TstatLVL"){
        def tLevel = settings."${type}TstatLVL" < 0 ?  0 : settings."${type}TstatLVL" >100 ? 100 : settings."${type}TstatLVL" as int
    	cmd.tstat == "heat" ? settings."${type}Tstats"?.setHeatingSetpoint(tLevel) : settings."${type}Tstats"?.setCoolingSetpoint(tLevel)
	}
    def param = [http:settings."${type}HTTP", ip:settings."${type}IP", port:settings."${type}Port", cmd:settings."${type}Command"]
    if (settings."${type}ExtInt" == "0" && param.http){
        log.info "Attempting to run: ${param.http}"
        httpGet(param.http)
    }
	if (settings."${type}ExtInt" == "1" && param.ip && param.port && param.cmd){
        def deviceHexID  = convertToHex (param.ip, param.port)
        log.info "Device Network Id set to ${deviceHexID}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET /${param.cmd} HTTP/1.1\r\nHOST: ${param.ip}:${param.port}\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceHexID}"))    
    }
    if (settings."${type}SHM"){
    	log.info "Setting Smart Home Monitor to " + settings."${type}SHM"
        sendLocationEvent(name: "alarmSystemStatus", value: settings."${type}SHM")
    }
    if (settings."${type}Garages" && cmd.garage) settings."${type}Garages"?."${cmd.garage}"()
   	if ((settings."${type}PushMsg" || settings."${type}SMSNum" || settings."${type}Contacts") && settings."${type}SMSMsg") sendMSG(settings."${type}SMSNum", settings."${type}SMSMsg", settings."${type}PushMsg", settings."${type}Contacts")
}
//Panic Handlers-----------------------------------------------------------------
def panicOn(evt){
	if (getOkToRun("Panic actions activated")) {
        if (alarm && alarmType){
            alarmTurnOn()
			if (alarmTimer && alarmTimer > 0){
				def delayOff = alarmTimer as int
				runIn(delayOff*60, alarmTurnOff, [overwrite: true])
			}
		}
        if (alarmSonos && parent.speakerSonos && alarmSonos.name.contains("Sonos") && alarmSonosSound) { 
			if (alarmSonosVolume) alarmSonos.setLevel(alarmSonosVolume as int)
            alarmSonos.playSoundAndTrack (state.alarmSound.uri, state.alarmSound.duration,"")
        }
        if (panicSMSnumberOn || panicPushOn || panicContactsOn){ 
			def smsTxt = panicSMSMsgOn ? panicSMSMsgOn : "Panic was activated without message text input. Please investigate."
            sendMSG(panicSMSnumberOn, smsTxt, panicPushOn, panicContactsOn) 	
		}
		if (parent.showNotifyFeed) sendNotificationEvent("Google Home Helper Scenario: '${app.label}' PANIC ON activated.")    
	}
}
def panicOff(evt){
	if (getOkToRun("Panic actions deactivated")) {
		if (alarmOff){
            alarmTurnOff()
        	if (alarmSonos && parent.speakerSonos && alarmSonos.name.contains("Sonos") && alarmSonosSound) alarmSonos.stop()
		}
		if (panicSMSnumberOff || panicPushOff || panicContactsOff){
			def smsTxt = panicSMSMsgOff ? panicSMSMsgOff : "Panic was deactivated without message text input. Please investigate"
            sendMSG(panicSMSnumberOff, smsTxt, panicPushOff, panicContactsOff) 	
		}
        if (parent.showNotifyFeed) sendNotificationEvent ("Google Helper Scenario: '${app.label}' PANIC OFF activated.")
	}
}
def alarmTurnOn(){alarm?."$alarmType"()}
def alarmTurnOff(){alarm?.off()}
//Speaker Handlers-----------------------------------------------------------------
def speakerControl(cmd, song, songName, announce){
    if (cmd=="station" || cmd=="on") {
    	//Google Switch should be used to prevent looping to this point when switch in turned on
        if (speakerInitial){
        	def speakerLevel = speakerInitial as int
    		vDimmerSpeaker.setLevel(speakerLevel)
        }
        else {
        	try { vDimmerSpeaker.setLevel(speaker.currentValue("level") as int) }
          	catch(e) { log.debug "Can't get current speaker level...may not be a true Sonos." }
        }    	
		if (cmd=="station"){
    		log.debug "Playing: ${song}"
            def text = textToSpeech(announce ? "Now playing: ${songName}." : " ", true)
            speaker.playSoundAndTrack(text.uri, text.duration, song)
		}
    	if (cmd=="on"){
        	speaker.play()
            speakerOnSwitches?.on()
        }
    }
	if (cmd=="off"){
    	speakerOffFunction ? speaker.stop() : speaker.pause()
     	speakerOffSwitches?.off()
    }
}
//Volume Handler
def speakerVolHandler(evt){
    if (getOkToRun("Speaker volume change")) {
        def speakerLevel = vDimmerSpeaker.currentValue("level") as int
    	if (speakerLevel == 0) vDimmerSpeaker.off()
    	else {
        	speakerLevel = upLimitSpeaker && (vDimmerSpeaker.currentValue("level") > upLimitSpeaker) ? upLimitSpeaker : lowLimitSpeaker && (vDimmerSpeaker.currentValue("level") < lowLimitSpeaker) ? lowLimitSpeaker : speakerLevel
    		speaker.setLevel(speakerLevel)
		}
	}
}    
//Speaker on/off
def speakerOnHandler(evt) {if (getOkToRun("Speaker on/off")) {if (evt.value == "on" || evt.value == "off" ) speakerControl(evt.value,"","","")}}
def controlNextHandler(evt) {if (getOkToRun("Speaker next track")) speaker.nextTrack()}
def controlPrevHandler(evt) {if (getOkToRun("Speaker previous track")) speaker.previousTrack()}
def controlSong(evt){
	def trigger = evt.displayName
    if (getOkToRun("Speaker Saved Song/Station Trigger: ${trigger}")) {
       	for (int i = 1; i <= sonosSlots(); i++) if (settings."song${i}Switch" && trigger == settings."song${i}Switch".label){speakerControl("station", state."selectedSong${i}", settings."song${i}Station", settings."announce${i}Song")}
	}
}
//Thermostat Handlers-----------------------------------------------------------------
def thermoOffHandler(evt){if (getOkToRun("Thermostat turned off")) tstat.off()}
def heatHandler(evt){
	if (getOkToRun("Thermostat mode:Heating")) {
		tstat.heat()
		def setpoint = heatingSetpoint ? heatingSetpoint : tstat.currentValue("heatingSetpoint")
		vDimmerTstat.setLevel(setpoint)
	}
}
def coolHandler(evt){
	if (getOkToRun("Thermostat mode:Cooling")) {
		tstat.cool()
		def setpoint = coolingSetpointSetpoint ? coolingSetpoint: tstat.currentValue("coolingSetpoint")
		vDimmerTstat.setLevel(setpoint)
	}
}
def autoHandler(evt){
	if (getOkToRun("Thermostat mode:Auto")) {	
		tstat.auto()
		def setpointH = heatingSetpoint ? heatingSetpoint : tstat.currentValue("heatingSetpoint")
		def setpointC = coolingSetpoint ? coolingSetpoint: tstat.currentValue("coolingSetpoint")
		vDimmerTstat?.on()
		tstat.setHeatingSetpoint(setpointH)
		tstat.setCoolingSetpoint(setpointC)
	}
}
def nestHomeHandler(evt){if (getOkToRun("Thermostat mode:Home")) tstat.present()}
def nestAwayHandler(evt){if (getOkToRun("Thermostat mode:Away")) tstat.away()}
//Thermostat Temp Handler
def thermoHandler(evt){
    if (getOkToRun("Temperature change")) {
    	def tstatMode=tstat.currentValue("thermostatMode")
        if (tstatMode != "auto" || (tstatMode == "auto" && autoControlTstat)){
        	def tstatLevel = vDimmerTstat.currentValue("level") as int
    		tstatLevel = upLimitTstat && vDimmerTstat.currentValue("level") > upLimitTstat ? upLimitTstat : lowLimitTstat && vDimmerTstat.currentValue("level") < lowLimitTstat ? lowLimitTstat : tstatLevel
    		if (tstatMode == "heat" || tstatMode == "auto") tstat.setHeatingSetpoint(tstatLevel)		
    		if (tstatMode == "cool" || tstatMode == "auto") tstat.setCoolingSetpoint(tstatLevel)	
    		log.info "Thermostat set to ${tstatLevel}"
		}
    }
}
//Baseboard Handlers-----------------------------------------------------------------
def BBOnOffHandler(evt) {if (getOkToRun("Baseboard(s) turned ${evt.value}")) BBOnOff(evt.value)}
def BBHandler(evt){
	if (getOkToRun("Baseboard Temperature change")) {
    	def tstatBBLevel = vDimmerBB.currentValue("level") as int
        tstatBBLevel = upLimitTstatBB && vDimmerBB.currentValue("level") > upLimitTstatBB ? upLimitTstatBB : lowLimitTstatBB && vDimmerBB.currentValue("level") < lowLimitTstatBB ? lowLimitTstatBB : tstatBBLevel
        tstatBB.each {
        	it.setHeatingSetpoint(tstatBBLevel)
        	log.info "${it} set to '${tstatBBLevel}'"
            if (it.name.contains("Stelpro")) {
            	log.info "Applying ${tstatBBLevel} setpoint to StelPro thermostat:'${it}'"
            	it.applyNow()
            }
        }
    }
}
def BBOnOff(status) {if (settings."setpointBB${status}") vDimmerBB.setLevel(settings."setpointBB${status}")}
//Voice Reporting Handlers-----------------------------------------------------------------
def voiceHandler(evt){
	if (!voiceDelay || voiceDelay == 0) voiceReport()
	else runIn(voiceDelay*60, voiceReport, [overwrite: true])
}
def voiceReport(){
	if (getOkToRun("Voice Reporting")) {
    	def fullMsg = voicePre ? "${replaceVoiceVar(voicePre)} " : ""
        if (voiceOnSwitchOnly) fullMsg += voiceSwitch ? switchReport(voiceSwitch, "switches") : ""
        else fullMsg += voiceSwitch ? reportStatus(voiceSwitch, "switch") : ""
        if (voiceOnDimmerOnly) fullMsg += voiceDimmer ? switchReport(voiceDimmer, "dimmers") : ""
        else fullMsg += voiceDimmer ? reportStatus(voiceDimmer, "level") : ""
        fullMsg += voicePresence ? presenceReport() : ""
        fullMsg += (voiceTemperature) ? reportStatus(voiceTemperature, "temperature") : ""
        if (voiceTempSettingSummary && voiceTempSettingsType) fullMsg += (voiceTempSettings) ? thermostatSummary(): ""
        else fullMsg += (voiceTempSettings && voiceTempSettingsType) ? reportStatus(voiceTempSettings, voiceTempSettingsType) : ""
        fullMsg += voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? doorWindowReport() : ""
        fullMsg += voiceMode ? "The current SmartThings mode is set to, '${location.currentMode}'. " : ""
        fullMsg += voiceSHM ? "The current Smart Home Monitor status is '${location.currentState("alarmSystemStatus")?.value}'. " : ""
        fullMsg += voicePost ? "${replaceVoiceVar(voicePost)} " : ""
        log.info fullMsg
    	def reportVol = voiceSpeaker ? voiceSpeaker.currentValue("level") : 0
        if (voiceVolume && voiceSpeaker) reportVol = voiceVolume as int
    	if (voiceSpeaker){
        	voiceSpeaker.refresh()
            if (voiceResume) voiceSpeaker.playTextAndResume(fullMsg, reportVol) 
            else {
            	voiceSpeaker.setLevel(reportVol)
                voiceSpeaker.playText(fullMsg)
			}
        }
		if (voiceDevice) voiceDevice?.speak("${fullMsg}")
        if (voiceNotification && (voiceContacts || voiceSMSnumber || voicePush))  sendMSG(voiceSMSnumber, fullMsg, voicePush, voiceContacts) 		
    }
}
//Common Methods-------------
private String convertToHex(ipAddress, port){
	String hexIP = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    String hexPort = port.toString().format( '%04x', port.toInteger() )
    return "${hexIP}:${hexPort}"
}
def getOkToRun(module){
	def result = true
    if (parent.showRestrictions) result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd)   
	if (result) log.info "Google Home Helper scenario '${app.label}', '${module}' triggered"
	else log.warn "Google Home Helper scenario '${app.label}', '${module}' not triggered due to scenario restrictions"
    result
}
def getOkOnOptions(){def result = (!showOptions || showOptions == "1") && (onPhrase || onMode || onSwitches || onDimmers || onColoredLights || onLocks || onGarages || onTstats || onHTTP || onIP || onSHM || onSMSMsg)}
def getOkOffOptions(){def result = (!showOptions || showOptions == "2") && (offPhrase || offMode || offSwitches || offDimmers || offColoredLights || offLocks || offGarages || offTstats || offHTTP || offIP || offSHM || offSMSMsg)}
def changeMode(newMode) {
    if (location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) setLocationMode(newMode)
        else log.warn "Unable to change to undefined mode '${newMode}'"
	}
}
def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
    if(start && end) timeLabel = "Between " + parseDate("${start}", "h:mm a") + " and " +  parseDate("${end}", "h:mm a")
    else if (start) timeLabel = "Start at " + parseDate("${start}", "h:mm a")
    else if (end) timeLabel = "End at " + parseDate("${end}", "h:mm a")
	timeLabel	
}
def scenarioDesc(){
	def desc = ""
    if (scenarioType=="Control" && GoogleSwitch){
		def onOff = !showOptions ? "On and Off" : showOptions && showOptions == "1" ? "On" : "Off"
        def delayTimeOn = onDelay && onDelay>1 ? "${onDelay} minutes" : onDelay && onDelay==1 ? "${onDelay} minute" : "immediately"
		def delayTimeOff = offDelay && offDelay>1 ? "${offDelay} minutes" : offDelay && offDelay==1 ? "${offDelay} minute" : "immediately"
        def timing = (!showOptions || showOptions == "1") ? "On scenario activates ${delayTimeOn} after triggered. " : ""
        timing += (!showOptions || showOptions == "2") ? "Off scenario activates ${delayTimeOff} after triggered." : ""
        desc = "'${GoogleSwitch}' switch (${onOff}) controls the scenario. ${timing}"
    }
    if (scenarioType=="Speaker"){
    	desc = vDimmerSpeaker && speaker ? "'${vDimmerSpeaker}' dimmer controls '${speaker}' speaker.": ""
        desc += nextSwitch && desc ? "\n'${nextSwitch}' switch activates Next Track." : ""
        desc += prevSwitch && desc ? "\n'${prevSwitch}' switch activates Previous Track." : ""
        for (int i = 1; i <= sonosSlots(); i++) {
        	desc += parent.speakerSonos && (speaker && speaker.name.contains("Sonos")) && settings."song${i}Switch" && settings."song${i}Station" && desc ? "\n'" + settings."song${i}Switch" + "' switch activates '" + settings."song${i}Station" + "'." : ""	
        }
    }
    if (scenarioType=="Thermostat"){
    	desc = vDimmerTstat && tstat ? "'${vDimmerTstat}' dimmer controls '${tstat}' thermostat." : ""
        desc += heatingSwitch && desc ? "\n'${heatingSwitch}' switch activates 'Heat' mode." : ""
        desc += coolingSwitch && desc ? "\n'${coolingSwitch}' switch activates 'Cool' mode." : ""
        desc += autoSwitch && desc ? "\n'${autoSwitch}' switch activates 'Auto' mode." : ""
        desc += parent.tstatNest && nestHome && desc ? "\n'${nestHome}' switch activates 'Home' mode." : ""
        desc += parent.tstatNest && nestAway && desc? "\n'${nestAway}' switch activates 'Away' mode." : ""
    }
    if (scenarioType=="Panic"){
    	desc = panicSwitchOn ? "'${panicSwitchOn}' switch activates panic actions." : ""
        desc += panicSwitchOn && panicSwitchOff && desc ?"\n'${panicSwitchOff}' switch deactivates panic actions." : ""
    }
    if (scenarioType=="Baseboard") {
        def noun = tstatBB && tstatBB.size() == 1 ? "baseboard heater: " : "baseboard heaters: "
        desc = vDimmerBB && tstatBB ? "'${vDimmerBB}' dimmer controls ${noun}${tstatBB}." : ""
    }
    if (scenarioType=="Voice"){
        def pushTxt = voiceNotification && (voiceContacts || voiceSMSnumber || voicePush)
        def noun = pushTxt ? "Push/SMS Notification" : ""
        noun += pushTxt && (voiceSpeaker || voiceDevice) ? ", " : ""
        noun += voiceSpeaker && !voiceDevice ? "'${voiceSpeaker}'" : ""
        noun += !voiceSpeaker && voiceDevice ? "'${voiceDevice}'" : ""
        noun += voiceSpeaker && voiceDevice ? "'${voiceSpeaker}' and '${voiceDevice}'}" : ""
        def delayTime = voiceDelay && voiceDelay>1 ? "${voiceDelay} minutes" : voiceDelay && voiceDelay==1 ? "${voiceDelay} minute" : "immediately"
    	desc = voiceControl && noun ? "'${voiceControl}' controls voice reports via ${noun}. Report generated ${delayTime} after triggered." : ""
    }
    desc = desc ? desc : "Status: UNCONFIGURED - Tap to configure scenario"
}
def reportDesc(param1, param2, param3) {def result = param1 || param2 || param3  ? "Status: CONFIGURED - Tap to edit" : "Status: UNCONFIGURED - Tap to configure"}
def reportDescMSHM() {
	def result= "Status: "
    result += voiceMode ? "Report Mode: On" : "Report Mode: Off"
    result += voiceSHM ? ", Report SHM: On" : ", Report SHM: Off"
}
def getDeviceDesc(type){  
    def result, switches, dimmers, cLights, locks, garages, tstats, lvl, cLvl, clr, tLvl
    def cmd = [switch: settings."${type}SwitchesCMD", dimmer: settings."${type}DimmersCMD", cLight: settings."${type}ColoredLightsCMD", tstat: settings."${type}TstatsCMD", lock: settings."${type}LocksCMD", garage: settings."${type}GaragesCMD"]
	switches = settings."${type}Switches" && cmd.switch ? settings."${type}Switches" : ""
	lvl = cmd.dimmer == "set" && settings."${type}DimmersLVL" ? settings."${type}DimmersLVL" as int : 0
	dimmers = settings."${type}Dimmers" && cmd.dimmer ? settings."${type}Dimmers" : ""
	cLvl = cmd.cLight == "set" && settings."${type}ColoredLightsLVL" ? settings."${type}ColoredLightsLVL" as int : 0
	clr = cmd.cLight == "set" && settings."${type}ColoredLightsCLR" ? settings."${type}ColoredLightsCLR"  : ""
	cLights = settings."${type}ColoredLights" && cmd.cLight ? settings."${type}ColoredLights" : ""
	tstats = settings."${type}Tstats" && cmd.tstat && settings."${type}TstatLVL"  ? settings."${type}Tstats" : ""
    tLvl = tstats ? settings."${type}TstatLVL" : 0
    locks = settings."${type}Locks" && cmd.lock ? settings."${type}Locks" : ""
	garages = settings."${type}Garages" && cmd.garage ? settings."${type}Garages" : ""
    lvl = lvl < 0 ? lvl = 0 : lvl >100 ? lvl=100 : lvl
    tLvl = tLvl < 0 ? tLvl = 0 : tLvl >100 ? tLvl=100 : tLvl
    cLvl = cLvl < 0 ? cLvl = 0 : cLvl >100 ? cLvl=100 : cLvl
    if (switches || dimmers || cLights || tstats || locks || garages) {
    	result = switches  ? "${switches} set to ${cmd.switch}" : ""
        result += result && dimmers ? "\n" : ""
        result += dimmers && cmd.dimmer != "set" ? "${dimmers} set to ${cmd.dimmer}" : ""
        result += dimmers && cmd.dimmer == "set" ? "${dimmers} set to ${lvl}%" : ""	
        result += result && cLights ? "\n" : ""
    	result += cLights && cmd.cLight != "set" ? "${cLights} set to ${cmd.cLight}":""
        result += cLights && cmd.cLight == "set" ? "${cLights} set to " : ""
        result += cLights && cmd.cLight == "set" && clr ? "${clr} and " : ""
        result += cLights && cmd.cLight == "set" ? "${cLvl}%" : ""
        result += result && tstats ? "\n" : ""
        result += tstats && cmd.tstat && tLvl ? "${tstats} set to ${cmd.tstat} : ${tLvl}" : ""
        result += result && locks ? "\n":""
        result += locks ? "${locks} set to ${cmd.lock}" : ""
        result += result && garages ? "\n" : ""
        result += garages ? "${garages} set to ${cmd.garage}" : ""
    }
    result = result ? result : "Status: UNCONFIGURED - Tap to configure"
}
def getDeviceState(type){
	def result = getDeviceDesc("${type}") == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete"
}
def getHTTPDesc(type){
	def result = ""
    def param = [http:settings."${type}HTTP", ip:settings."${type}IP", port:settings."${type}Port", cmd:settings."${type}Command"]
    if (settings."${type}ExtInt" == "0" && param.http) result += param.http
    else if (settings."${type}ExtInt" == "1" && param.ip && param.port && param.cmd) result += "http://${param.ip}:${param.port}/${param.cmd}"
    result = result ? result : "Status: UNCONFIGURED - Tap to configure"
}
def greyOutState(param1, param2, param3){def result = param1 || param2 || param3 ? "complete" : ""}
def greyOutStateHTTP(type){
    def param = [http:settings."${type}HTTP", ip:settings."${type}IP", port:settings."${type}Port", cmd:settings."${type}Command"]
    def result = (settings."${type}ExtInt" == "0" && param.http) || (settings."${type}ExtInt" == "1" && param.ip && param.port && param.cmd) ? "complete" : ""
}
def greyOutScen(){def result = (scenarioType=="Control" && GoogleSwitch) || (scenarioType=="Speaker" && vDimmerSpeaker && speaker) || (scenarioType=="Thermostat" && vDimmerTstat && tstat) || (scenarioType=="Panic" && panicSwitchOn && panicSwitchOff) ? "complete" : ""}
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
def fillColorSettings(){ 
	def colorData = []
    colorData << [name: "White", hue: 0, sat: 0] << [name: "Orange", hue: 11, sat: 100] << [name: "Red", hue: 100, sat: 100] << [name: "Purple", hue: 77, sat: 100]
    colorData << [name: "Green", hue: 30, sat: 100] << [name: "Blue", hue: 66, sat: 100] << [name: "Yellow", hue: 16, sat: 100] << [name: "Pink", hue: 95, sat: 100]
    colorData << [name: "Cyan", hue: 50, sat: 100] << [name: "Chartreuse", hue: 25, sat: 100] << [name: "Teal", hue: 44, sat: 100] << [name: "Magenta", hue: 92, sat: 100]
	colorData << [name: "Violet", hue: 83, sat: 100] << [name: "Indigo", hue: 70, sat: 100]<< [name: "Marigold", hue: 16, sat: 75]<< [name: "Raspberry", hue: 99, sat: 75]
    colorData << [name: "Fuchsia", hue: 92, sat: 75] << [name: "Lavender", hue: 83, sat: 75]<< [name: "Aqua", hue: 44, sat: 75]<< [name: "Amber", hue: 11, sat: 75]
    colorData << [name: "Carnation", hue: 99, sat: 50] << [name: "Periwinkle", hue: 70, sat: 50]<< [name: "Pistachio", hue: 30, sat: 50]
    colorData << [name: "Vanilla", hue: 16, sat: 50] << [name: "Custom-User Defined", hue: 0, sat: 0]
}
private setColoredLights(switches, color, level, type){
	def getColorData = fillColorSettings().find {it.name==color}
    def hueColor = getColorData.hue
	def satLevel = getColorData.sat
	if (color == "Custom-User Defined"){
		hueColor = settings."${type}HueUserDefined" ?  settings."${type}HueUserDefined"  : 0
		satLevel = settings."${type}SatUserDefined" ? settings."${type}SatUserDefined" : 0
		hueColor = hueColor > 100 ? 100 : hueColor < 0 ? 0 : hueColor
		satLevel = satLevel > 100 ? 100 : satLevel < 0 ? 0 : satLevel
	}
    def newValue = [hue: hueColor as int, saturation: satLevel as int, level: level as int]
	switches?.setColor(newValue)
}
def songOptions(slot) {
    if (speaker) {
		def options = new LinkedHashSet()
        if (state."selectedSong${slot}"?.station) options << state."selectedSong${slot}".station
		else if (state."selectedSong${slot}"?.description) options << state."selectedSong${slot}".description
		def states = speaker.statesSince("trackData", new Date(0), [max:30])
		def dataMaps = states.collect{it.jsonValue}
		options.addAll(dataMaps.collect{it.station})
		log.trace "${options.size()} song(s) in list"
        options.take(20) as List
	}
}
def saveSelectedSong(slot, song) {
	try {
		def thisSong = song
		log.info "Looking for $thisSong"
		def songs = speaker.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"
		def data = songs.find {s -> s.station == thisSong}
		log.info "Found ${data?.station}"
		if (data) state."selectedSong${slot}"=data
		else log.warn "'${song}' not found"
	}
	catch (Throwable t) { log.error t }
}
//Voice report---------------------------------------------------
def switchReport(devices, type){
	def result = ""
    if (devices.latestValue("switch").contains("on")) devices.each { deviceName->
    	if (deviceName.latestValue("switch")=="on") {
        	result += "${deviceName} is on"
        	result += type == "dimmers" ? " and set to ${deviceName.latestValue("level")}%. " : ". "
		}
    }
    else result += "All of the monitored ${type} are off. "
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
            if (!difCount) result += "All thermostats are set to ${voiceTempTarget} degrees. "
            else if (difCount==monitorCount) result += "None of the thermostats are set to ${voiceTempTarget} degrees. "
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
    else result="Some of your thermostats are not able to provide their setpoint. Please choose another setpoint type to report on. " 
    result
}
def reportStatus(deviceList, type){
	def result = ""
    def appd = type=="temperature" || type=="thermostatSetpoint" || type == "heatingSetpoint" || type=="coolingSetpoint" ? "degrees" : ""
    if (type != "thermostatSetpoint" && type != "heatingSetpoint" && type !="coolingSetpoint") deviceList.each {deviceName->result += "${deviceName} is ${deviceName.latestValue(type)} ${appd}. " }
    else deviceList.each { deviceName->
    	try { result += "${deviceName} is set to ${deviceName.latestValue(type) as int} ${appd}. " }
    	catch (e) { result = "${deviceName} is not able to provide its setpoint. Please choose another setpoint type to report on. " }
    }
    result
}
def presenceReport(){
	def result = ""
    if (voicePresentOnly){
    	if (voicePresence.latestValue("presence").contains("not present")) voicePresence.each { deviceName->
            if (deviceName.latestValue("presence")=="not present") result += "${deviceName} is not present"
    	}
    	else result += "All of the monitored presence sensors are present. "
    }
    else  voicePresence.each {deviceName->result += "${deviceName} is " + deviceName.latestValue("presence") + ". " }
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
    	if (!totalCount && !countUnlocked) {
        	result += "All of the doors and windows are closed"
        	result += voiceDoorLocks ? " and locked. " : ". "
        }
        if (!countOpened && !countOpenedDoor && countUnlocked){
   			result += "All of the doors and windows are closed, but the "
            result += countUnlocked > 1 ? "following are unlocked: ${listUnlocked}. " :"${listUnlocked} is unlocked. "
    	}
        if ((countOpened || countOpenedDoor) && !countUnlocked){
   			result += "All of the doors are locked, but the "
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
//Send Messages
def sendMSG(num, msg, push, recipients){
    def logText =""
    if (num) {logText = "SMS Message '${msg}' sent to ${num}"}
    if (push) {logText = "Message '${msg}' pushed to SmartApp"}
    if (num && push) {logText = "Message '${msg}' sent to ${num} and pushed to SmartApp"}
    if (location.contactBookEnabled && recipients) {logText = "Message '${msg}' sent to ${recipients}"}
    if (parent.showNotifyFeed) sendNotificationEvent(logText)
    log.info logText
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
//Get Sonos Alarm Sound uri
def getAlarmSound(){
    def soundLength = alarmSonosTimer && alarmSonosTimer < 60 ? alarmSonosTimer : 60   
    def soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper-scenario.src/AlarmSirens/AlarmSiren${alarmSonosSound}.mp3", duration: "${soundLength}"]
    if (alarmSonosSound == "5") soundUri =[uri: "${alarmSonosCustom}", duration: "${soundLength}"]
    state.alarmSound = soundUri
}
def sonosSlots(){def slots = parent.getMemCount() as int}
private replaceVoiceVar(msg) {
    def df = new java.text.SimpleDateFormat("EEEE")
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date()), time = parseDate("","h:mm a"), month = parseDate("","MMMM"), year = parseDate("","yyyy"), dayNum = parseDate("","d")
    def temp = voiceTempVar ? "${voiceTempVar.latestValue("temperature")} degrees" : "undefined device"
    msg = msg.replace('%day%', day)
    msg = msg.replace('%date%', "${month} ${dayNum}, ${year}")
    msg = msg.replace('%time%', "${time}")
    msg = msg.replace('%temp%', "${temp}")
    msg
}
private parseDate(time, type){
	def formattedDate = time ? time : new Date(now()).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", formattedDate).format("${type}", timeZone(formattedDate))
}
//Common modules
def imgURL() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/" }
def getMemCount(){ return memoryCount ? memoryCount : 2 }
def getSwitchAbout(){ return "Created by Google Home Helper SmartApp" }
//Version/Copyright/Information/Help
private def textAppName() { return "Google Home Helper" }	
private def textVersion() {
    def version = "SmartApp Version: 1.0.0 (12/01/2016)"
    def deviceCount= getChildDevices().size()
    def deviceVersion = state.sw1Ver && deviceCount ? "\n${state.sw1Ver}": ""
    deviceVersion += state.sw2Ver && deviceCount ? "\n${state.sw2Ver}": ""
    return "${version}${deviceVersion}"
}
private def versionInt(){return 100}
private def textCopyright() {return "Copyright © 2016 Michael Struck"}
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
		"Ties various SmartThings functions to the on/off state of specifc switches. You may also control a thermostat, baseboard heaters, "+
        "the volume of a wireless speakers, define a panic command or report on the status of various devices using " +
        "either a dimmer control or momentary button tile. Perfect for use with the Google Home.\n\n" +
		"To use, first create the required momentary button tiles or 'Google Switch' (custom switch/dimmer) from the SmartThings IDE or the SmartApp. "+
        "You may also use any physical switches already associated with SmartThings. Include these switches within the Google Home app. Then, create a "+
        "new scenario that best fits your needs, associating the switches with the various controls within the scenario.\n\n" +
        "For more information, go to http://thingsthataresmart.wiki/index.php?title=Google_Home_Helper"    
}