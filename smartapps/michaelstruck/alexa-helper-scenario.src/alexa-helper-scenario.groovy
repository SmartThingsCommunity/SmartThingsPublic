/**
 *  Alexa Helper-Child
 *
 *  Copyright © 2016 Michael Struck
 *  Version 2.8.5 3/12/16
 * 
 *  Version 1.0.0 - Initial release of child app
 *  Version 1.1.0 - Added framework to show version number of child app and copyright
 *  Version 1.1.1 - Code optimization
 *  Version 1.2.0 - Added the ability to add a HTTP request on specific actions
 *  Version 1.2.1 - Added child app version information into main app
 *  Version 1.3.0 - Added ability to change the Smart Home Monitor status and added a section for the remove button
 *  Version 2.0.0 - Added in speaker and thermostat scenarios from main app to allow for more than one of these devices
 *  Version 2.0.1 - Fixed an issue with the getTimeOk routine
 *  Version 2.1.0 - Many changes; added switches, dimmers and colored lights as control devices. Modified Thermostat logic and modified GUI
 *  Version 2.2.0a - Added SMS to on/off control scenarios, and allow  'toggle' to change the lights; added Sonos as an alarm type
 *  Version 2.2.1a - Code and syntax optimization; added routine to turn off Sonos speaker if used as alarm
 *  Version 2.3.0 - Code optimization and configuration for additional memory slots for Sonos (advanced users only)
 *  Version 2.4.0 - Added GUI (in parent app) to allow for variable number of Sonos memory slots, added speaker pause toggle
 *  Version 2.5.0 - Added switch functions when speaker on/off issued
 *  Version 2.5.1 - Fixed issue with songs not initalizing
 *  Version 2.6.0 - Refined notification methods; displays action on notification feed and added push notifications; code optimization
 *  Version 2.7.0 - Added baseboard heaters scenario type and various code optimizations
 *  Version 2.7.1b - Small syntax changes
 *  Version 2.7.2 - Code optimization
 *  Version 2.8.0 - Added voice reporting options
 *  Version 2.8.1 - Syntax clean up, added dimmer to voice reporting options, added speech devices besides speakers
 *  Version 2.8.2 - Refined voice reporting (removed 'status' headers in announcement) and added date/time variables
 *  Version 2.8.3 - Minor code fixes, optimizations, adding 'Apply' heating setpoint for StelPro baseboard heaters
 *  Version 2.8.4 - Added additional voice variables (%temp%); workaround implemented for playTrack() not being operational(as of 3/11)
 *  Version 2.8.5 - Code optimization, added option to announce name of song for saved stations
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
	name: "Alexa Helper-Scenario",
	namespace: "MichaelStruck",
	author: "Michael Struck",
	description: "Child app (do not publish) that allows various SmartThings devices to be tied to switches controlled by Amazon Echo('Alexa').",
	category: "Convenience",
	parent: "MichaelStruck:Alexa Helper",
	iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa.png",
	iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png")    
preferences {
	page name: "pageStart"
	page name: "pageControl"
	page name: "pageSTDevicesOn"
	page name: "pageSTDevicesOff"
	page name: "pageSpeaker"
	page name: "pagePanic"
	page name: "pageThermostat"
    page name: "pageVoice"
    page name: "pageTempReport"
    page name: "pageHomeReport"
}
def pageStart() {
	dynamicPage(name: "pageStart", title: "Scenario Settings", uninstall: true, install: true) {
		section {
			if (parent.versionInt() < 445) paragraph "You are using a version of the parent app that is older than the recommended version. Please upgrade "+
					"to the latest version to ensure you have the latest features and bug fixes."
            label title:"Scenario Name", required:true
    	   	input "scenarioType", "enum", title: "Scenario Type...", options: [["Baseboard":"Baseboard Heater Control"],["Control":"Modes/Routines/Devices/HTTP/SHM"],["Panic":"Panic Commands"],["Speaker":"Speaker Control"],["Thermostat":"Heating/Cooling Thermostat"],["Voice":"Voice Reporting"]], required: false, multiple: false, submitOnChange:true
        	if (scenarioType){
                href "page${scenarioType}", title: "${scenarioType} Scenario Settings", description: scenarioDesc(), state: greyOutScen()
			}
		}
		if (scenarioType && parent.getRestrictions()){
			section("Restrictions") {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        		href "timeIntervalInput", title: "Only during a certain time...", description: getTimeLabel(timeStart, timeEnd), state: greyOutState(timeStart, timeEnd,"")
            	input "runMode", "mode", title: "Only during the following modes...", multiple: true, required: false
			}
        }
        section("Tap the button below to remove this scenario"){}
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
	dynamicPage(name: "pageControl", title: "Control Scenario Settings", install: false, uninstall: false) {
        section {
			input "AlexaSwitch", "capability.switch", title: "Control Switch (On/Off, Momentary)", multiple: false, required: true
    		input "showOptions", "enum", title: "Switch states to react to...", options: ["":"On/Off", "1":"On Only", "2":"Off Only"] , required: false, submitOnChange:true
        }
        def phrases = location.helloHome?.getPhrases()*.label
		if (phrases) phrases.sort()	
        if (!showOptions || showOptions == "1") {
        	section ("When switch is on..."){
        		if (phrases) input "onPhrase", "enum", title: "Perform this routine", options: phrases, required: false
        		input "onMode", "mode", title: "Change to this mode", required: false
            	input "onSHM", "enum",title: "Change Smart Home Monitor to...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false
            	href "pageSTDevicesOn", title: "SmartThings Device Control...", description: getDeviceDesc("on")
            	input "onHTTP", "text", title:"Run this HTTP request...", required: false
            	input "delayOn", "number", title: "Delay in minutes", defaultValue: 0, required: false
                input ("onContacts", "contact", title: "Send notifications to...") {
                	input "onSMSNum", "phone", title: "Send SMS message (phone number)...", required: false
                	input "onPushMsg", "bool", title: "Send Push message", defaultValue: false
				}
                input "onSMSMsg", "text", title: "Message to send...", required: false
			}
        }
        if (!showOptions || showOptions == "2") {
        	section ("When switch is off..."){
        		if (phrases) input "offPhrase", "enum", title: "Perform this routine", options: phrases, required: false
        		input "offMode", "mode", title: "Change to this mode", required: false
                input "offSHM", "enum",title: "Change Smart Home Monitor to...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false
                href "pageSTDevicesOff", title: "SmartThings Device Control...", description: getDeviceDesc("off")
                input "offHTTP", "text", title:"Run this HTTP request...", required: false
                input "delayOff", "number", title: "Delay in minutes", defaultValue: 0, required: false
                input ("offContacts", "contact", title: "Send notifications to...") {
                	input "offSMSNum", "phone", title: "Send SMS message (phone number)...", required: false
                	input "offPushMsg", "bool", title: "Send Push message", defaultValue: false
                }
                input "offSMSMsg", "text", title: "Message to send...", required: false
        	}
        }
    }
}
// Show "pageSTDevicesOn" page
def pageSTDevicesOn(){
	dynamicPage (name: "pageSTDevicesOn", title: "SmartThings Device Control", install: false, uninstall: false) {
		section ("Switches"){
    		input "onSwitches", "capability.switch", title: "Control these switches...", multiple: true, required: false, submitOnChange:true
			if (onSwitches) input "onSwitchesCMD", "enum", title: "Command to send to switches", options:["on":"Turn on","off":"Turn off", "toggle":"Toggle the switches' on/off state"], multiple: false, required: false
        }
    	section ("Dimmers"){
    		input "onDimmers", "capability.switchLevel", title: "Control these dimmers...", multiple: true, required: false, submitOnChange:true
        	if (onDimmers) input "onDimmersCMD", "enum", title: "Command to send to dimmers", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the dimmers' on/off state"], multiple: false, required: false, submitOnChange:true
        	if (onDimmersCMD == "set" && onDimmers) input "onDimmersLVL", "number", title: "Dimmers level", description: "Set dimmer level", required: false, defaultValue: 0
   		}
        section ("Colored Lights"){
        	input "onColoredLights", "capability.colorControl", title: "Control these colored lights...", multiple: true, required: false, submitOnChange:true
			if (onColoredLights) input "onColoredLightsCMD", "enum", title: "Command to send to colored lights", options:["on":"Turn on","off":"Turn off","set":"Set color and level", "toggle":"Toggle the lights' on/off state"], multiple: false, required: false, submitOnChange:true
            if (onColoredLightsCMD == "set" && onColoredLights){
        		input "onColoredLightsCLR", "enum", title: "Choose a color...", required: false, multiple:false, options: getColorOptions(), submitOnChange:true
                if (onColoredLightsCLR == "User Defined"){
                	input "hueUserDefined", "number", title: "Colored lights hue", description: "Set colored light hue (0 to 100)", required: false, defaultValue: 0
                	input "satUserDefined", "number", title: "Colored lights saturation", description: "Set colored light saturation (0 to 100)", required: false, defaultValue: 0
                }
                input "onColoredLightsLVL", "number", title: "Colored lights level", description: "Set colored light level", required: false, defaultValue: 0
            }
    	}
        section ("Locks"){
        	input "onLocks","capability.lock", title: "Control these locks...", multiple: true, required: false, submitOnChange:true
        	if (onLocks) input "onLocksCMD", "enum", title: "Command to send to locks", options:["lock":"Lock","unlock":"Unlock"], multiple: false, required: false
        }
        section("Garage Doors") {
        	input "onGarages","capability.garageDoorControl", title: "Control these garage doors...", multiple: true, required: false, submitOnChange:true
            if (onGarages) input "onGaragesCMD", "enum", title: "Command to send to garage doors", options:["open":"Open","close":"Close"], multiple: false, required: false
        }
	}
}
// Show "pageSTDevicesOff" page
def pageSTDevicesOff(){
	dynamicPage (name: "pageSTDevicesOff", title: "SmartThings Device Control", install: false, uninstall: false) {
		section ("Switches"){
    		input "offSwitches", "capability.switch", title: "Control these switches...", multiple: true, required: false, submitOnChange:true
        	if (offSwitches) input "offSwitchesCMD", "enum", title: "Command to send to switches", options:["on":"Turn on","off":"Turn off", "toggle":"Toggle the switches state"], multiple: false, required: false
    	}
    	section ("Dimmers"){
    		input "offDimmers", "capability.switchLevel", title: "Control these dimmers...", multiple: true, required: false , submitOnChange:true
        	if (offDimmers) input "offDimmersCMD", "enum", title: "Command to send to dimmers", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the switches state"], multiple: false, required: false, submitOnChange:true
        	if (offDimmersCMD == "set" && offDimmers) input "offDimmersLVL", "number", title: "Dimmers level", description: "Set dimmer level", required: false, defaultValue: 0
   		}
        section ("Colored Lights"){
        	input "offColoredLights", "capability.colorControl", title: "Control these colored lights...", multiple: true, required: false, submitOnChange:true
			if (offColoredLights) input "offColoredLightsCMD", "enum", title: "Command to send to colored lights", options:["on":"Turn on","off":"Turn off","set":"Set color and level", "toggle":"Toggle the switches state"], multiple: false, required: false, submitOnChange:true
            if (offColoredLightsCMD == "set" && offColoredLights){
        		input "offColoredLightsCLR", "enum", title: "Choose a color...", required: false, multiple:false, options: getColorOptions()
                input "offColoredLightsLVL", "number", title: "Colored light level", description: "Set colored lights level", required: false, defaultValue: 0
        	}
    	}
        section ("Locks"){
        	input "offLocks","capability.lock", title: "Control these locks...", multiple: true, required: false, submitOnChange:true
        	if (offLocks) input "offLocksCMD", "enum", title: "Command to send to locks", options:["lock":"Lock","unlock":"Unlock"], multiple: false, required: false
        }
        section("Garage Doors"){
        	input "offGarages","capability.garageDoorControl", title: "Control these garage doors...", multiple: true, required: false, submitOnChange:true
            if (offGarages) input "offGaragesCMD", "enum", title: "Command to send to garage doors", options:["open":"Open","close":"Close"], multiple: false, required: false
        }
	}
}
// Show "Panic" page
def pagePanic() {
	dynamicPage(name: "pagePanic", title: "Panic Scenario Settings", install: false, uninstall: false) {
        section {
			input "panicSwitchOn", "capability.momentary", title: "ON Control Switch (Momentary)", multiple: false, required: true, submitOnChange:true
          	if (panicSwitchOn) input "panicSwitchOff", "capability.momentary", title: "OFF Control Switch (Momentary)", multiple: false, required: false, submitOnChange:true
        }
        section ("When panic is activated..."){
        	input "alarm", "capability.alarm", title: "Activate alarms...", multiple: true, required: false, submitOnChange:true
            if (parent.getSonos()) input "alarmSonos", "capability.musicPlayer", title: "Use Sonos as alarm...", multiple: false , required: false , submitOnChange:true
            if (alarm){
            	input "alarmType", "enum", title: "Choose an alarm type", options: ["strobe":"Strobe light", "siren":"Siren", "both":"Both stobe and siren"], multiple: false, required: false  
            	input "alarmTimer", "number", title:"Alarm turns off automatically after (minutes)", required: false
            }
            if (alarmSonos && parent.getSonos()  && alarmSonos.name.contains("Sonos")){
                input "alarmSonosVolume", "number", title:"Sonos alarm volume", required: false
                input "alarmSonosSound", "enum", title:"Sonos alarm sound", options: [1:"Alarm 1-European Siren", 2:"Alarm 2-Sci-Fi Siren", 3:"Alarm 3-Police Car Siren", 4:"Alarm 4-Red Alert",5:"Custom-User Defined"], multiple: false, required: false, submitOnChange:true 
                if (alarmSonosSound == "5") input "alarmSonosCustom", "text", title:"URL/Location of custom sound...", required: false
                input "alarmSonosTimer", "number", title:"Alarm turns off automatically after (seconds)", required: false
            }
            if (alarmSonos && parent.getSonos()  && !alarmSonos.name.contains("Sonos")){
            	paragraph "You have chosen a speaker for your alarm that is not supported. Currently, only Sonos speakers can be used as alarms. Please choose a Sonos speaker."
            }
            input ("panicContactsOn", "contact", title: "Send notifications to...") {
            	input "panicSMSnumberOn", "phone", title: "Send SMS message to (phone number)...", required: false
            	input "panicPushOn", "bool", title: "Send Push Message", defaultValue: false
            }
            input "panicSMSMsgOn","text",title: "Message to send...", required: false
        }
        if (panicSwitchOn && panicSwitchOff){
        	section ("When panic is deactivated..."){
        		if (alarm || alarmSonos) input "alarmOff", "bool", title: "Turn off alarm?", defaultValue: false
            	input ("panicContactsOff", "contact", title: "Send notifications to...") {
                	input "panicSMSnumberOff", "phone", title: "Send SMS message to (phone number)...", required: false
                	input "panicPushOff", "bool", title: "Send Push Message", defaultValue: false
            	}
                input "panicSMSMsgOff","text",title: "Message to send...", required: false
        	}
        }
	}
}
// Show "pageSpeaker" page
def pageSpeaker(){
	dynamicPage(name: "pageSpeaker", title: "Speaker Scenario Settings", install: false, uninstall: false) {
		section {
        	input "vDimmerSpeaker", "capability.switchLevel", title: "Control Switch (Dimmer)", multiple: false, required:false, submitOnChange:true
            input "speaker", "capability.musicPlayer", title: "Speaker To Control", multiple: false , required: false, submitOnChange:true
        }
    	section ("Speaker Volume Limits") {        
        	input "upLimitSpeaker", "number", title: "Volume Upper Limit", required: false
    		input "lowLimitSpeaker", "number", title: "Volume  Lower Limit", required: false
        	input "speakerInitial", "number", title: "Volume When Speaker Turned On", required: false
		}
		section ("Speaker Track Controls") {    
        	input "nextSwitch", "capability.momentary", title: "Next Track Switch (Momentary)", multiple: false, required: false
       		input "prevSwitch", "capability.momentary", title: "Previous Track Switch (Momentary)", multiple: false, required: false
    	}
        if (vDimmerSpeaker){
        	section ("Other Functions/Controls"){
        		input "speakerOnSwitches", "capability.switch", title: "When Control Switch on, turn on...", multiple: true, required: false
                input "speakerOffSwitches", "capability.switch", title: "When Control Switch off, turn off...", multiple: true, required: false
                input "speakerOffFunction", "bool", title: "Control Switch off action: Pause/Stop Playback", defaultValue: false
        	}
		}
        if (speaker && songOptions(1) && parent.getSonos() && speaker.name.contains("Sonos")){
            for (int i = 1; i <=sonosSlots(); i++) {
                section ("Sonos Saved Station ${i}"){
					input "song${i}Switch", "capability.momentary", title: "Saved Station Switch #${i} (Momentary)", multiple: false, required: false, submitOnChange:true
                    if (settings."song${i}Switch") input "song${i}Station", "enum", title: "Song/Station #${i}", description: "Tap to select recently played song/station", multiple: false, 
							required: false, options: songOptions("${i}"), submitOnChange:true
					if (settings."song${i}Station") input "announce${i}Song", "bool", title: "Announce Song Name Prior To Playing", defaultValue: false
                }
			}
		}
        if (speaker && !songOptions(1) && parent.getSonos() && speaker.name.contains("Sonos")){
        	section ("Sonos Saved Stations") {
            	paragraph "There are currently no songs available in the Sonos memory. Play a "+
                	"song or station through SmartThings, then come back to this app and the "+
                    "station list should be available"
            }
        }
	}
}
// Show "pageThermostat" page
def pageThermostat(){
    dynamicPage(name: "pageThermostat", title: "Thermostat Scenario Settings", install: false, uninstall: false) {
        section {
            input "vDimmerTstat", "capability.switchLevel", title: "Control Switch (Dimmer)", multiple: false, required:false
            input "tstat", "capability.thermostat", title: "Thermostat To Control", multiple: false , required: false
            input "autoControlTstat", "bool", title: "Control thermostat in 'Auto' mode", defaultValue: false
        }
        section ("Thermostat Temperature Limits") {
            input "upLimitTstat", "number", title: "Thermostat Upper Limit", required: false
            input "lowLimitTstat", "number", title: "Thermostat Lower Limit", required: false
        }
        section ("Thermostat Setpoints (when heating, cooling or auto mode controls turned on below)"){
            input "heatingSetpoint", "number", title: "Heating setpoint", required: false
            input "coolingSetpoint", "number", title: "Cooling setpoint", required: false
        }
         section ("Thermostat Mode Controls") {
            input "heatingSwitch", "capability.momentary", title: "Heating Mode Switch (Momentary)", multiple: false, required: false
            input "coolingSwitch", "capability.momentary", title: "Cooling Mode Switch (Momentary)", multiple: false, required: false
            input "autoSwitch", "capability.momentary", title: "Auto Mode Switch (Momentary)", multiple: false, required: false
        }
        if (parent.getNest()){
            section ("Nest Home/Away Controls"){
                input "nestHome", "capability.momentary", title: "Home Mode Switch (Momentary)", multiple: false, required: false
                input "nestAway", "capability.momentary", title: "Away Mode Switch (Momentary)", multiple: false, required: false
            }
        }
    }
}
// Show "pageBaseboard" page
page(name: "pageBaseboard", title: "Baseboard Scenario Settings", install: false, uninstall: false) {
	section {
		input "vDimmerBB", "capability.switchLevel", title: "Control Switch (Dimmer)", multiple: false, required:false
		input "tstatBB", "capability.thermostat", title: "Thermostat To Control", multiple: true, required: false
	}
	section ("Baseboard Temperature Limits") {
		input "upLimitTstatBB", "number", title: "Thermostat Upper Limit", required: false
		input "lowLimitTstatBB", "number", title: "Thermostat Lower Limit", required: false
	}
	section ("Baseboard On/Off Setpoints"){
		input "setpointBBOn", "number", title: "Setpoint when Control Switch turned on", required: false
		input "setpointBBOff", "number", title: "Setpoint when Control Switch turned off", required: false
	}
}
//Show "pageVoice" page
def pageVoice(){
    dynamicPage(name: "pageVoice", title: "Voice Reporting Scenario Settings", install: false, uninstall: false){
        section {
            input "voiceControl", "capability.momentary", title: "Voice Report Control Switch (Momentary)", multiple: false, required: true
            input "voiceSpeaker", "capability.musicPlayer", title: "Voice Report Speaker", multiple: false, required: false, submitOnChange:true
            if (voiceSpeaker) input "voiceVolume", "number", title: "Speaker Volume", required: false
            input "voiceDevice", "capability.speechSynthesis", title: "Voice Report Speech Synthesis Device", multiple: false, required: false
        }
        section ("Report Types"){
            input "voicePre", "text", title: "Pre Message Before Device Report", description: "Enter a message to play before the device report", defaultValue: "This is your SmartThings voice report for %time%, %day%, %date%,.", required: false
            href "pageSwitchReport", title: "Switch/Dimmer Status Report", description: reportDesc(voiceSwitch, voiceDimmer, ""), state: greyOutState(voiceSwitch, voiceDimmer, "")
            href "pageDoorReport", title: "Doors/Windows Report", description: reportDesc(voiceDoorSensors, voiceDoorControls, voiceDoorLocks), state: greyOutState(voiceDoorSensors, voiceDoorControls, voiceDoorLocks)
            href "pageTempReport", title: "Temperatures/Thermostats Report", description: reportDesc(voiceTemperature, voiceTempSettings, voiceTempVar), state: greyOutState(voiceTemperature, voiceTempSettings, voiceTempVar)
            href "pageHomeReport", title: "Mode and Smart Home Monitor Report", description: reportDesc(voiceMode, voiceSHM, ""), state: "complete"
            input "voicePost", "text", title: "Post Message After Device Report", description: "Enter a message to play after the device report", required: false
        }
    }
}
page(name: "pageSwitchReport", title: "Switch/Dimmer Status Report", install: false, uninstall: false){
	section {
        input "voiceSwitch", "capability.switch", title: "Switches To Report Their Status...", multiple: true, required: false 
        input "voiceOnSwitchOnly", "bool", title: "Report Only Switches That Are On", defaultValue: false
        input "voiceDimmer", "capability.switchLevel", title: "Dimmers To Report Their Status...", multiple: true, required: false
        input "voiceOnDimmerOnly", "bool", title: "Report Only Dimmers That Are On", defaultValue: false 
    }
}
page(name: "pageDoorReport", title: "Doors/Windows Report", install: false, uninstall: false){
	section {
		input "voiceDoorSensors", "capability.contactSensor", title: "Check Which Doors/Windows Sensors...", multiple: true, required: false
		input "voiceDoorControls", "capability.doorControl", title: "Check Which Door Controls...", multiple: true, required: false
		input "voiceDoorLocks", "capability.lock", title: "Check Which Locks...", multiple: true, required: false
        input "voiceDoorAll", "bool", title: "Report door/window summary even when all are closed and locked", defaultValue: false
	}
}
def pageTempReport(){
    dynamicPage(name: "pageTempReport", title: "Temperature Report", install: false, uninstall: false){
        section {
            input "voiceTempVar", "capability.temperatureMeasurement", title: "Temperature Device Variable (%temp%)",multiple: false, required: false
            input "voiceTemperature", "capability.temperatureMeasurement", title: "Devices To Report Temperatures...",multiple: true, required: false
            input "voiceTempSettings", "capability.thermostat", title: "Thermostats To Report Their Setpoints...",multiple: true, required: false, submitOnChange:true
            if (voiceTempSettings) input "voiceTempSettingSummary", "bool", title: "Consolidate Thermostats Report", defaultValue: false, submitOnChange:true
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
//----------------------------------------------------
def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}
def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    unsubscribe()
    initialize()
}
def initialize() {
    childApps.each {child -> log.info "Installed Scenario: ${child.label}"}
    if (scenarioType == "Control" && AlexaSwitch) subscribe(AlexaSwitch, "switch", "switchHandler")
    if (scenarioType == "Thermostat"){
    	if (vDimmerTstat && tstat){
            subscribe (vDimmerTstat, "switch.off", "thermoOffHandler")
    		subscribe (vDimmerTstat, "level", "thermoHandler")
        	if (heatingSwitch) subscribe (heatingSwitch, "switch.on", "heatHandler")
        	if (coolingSwitch) subscribe (coolingSwitch, "switch.on", "coolHandler")
        	if (autoSwitch) subscribe (autoSwitch, "switch.on", "autoHandler")
		}
        if (parent.getNest()){
        	if (nestHome) subscribe(nestHome, "switch.on", "nestHomeHandler")
            if (nestAway) subscribe(nestAway, "switch.on", "nestAwayHandler")
		}
    }
    if (scenarioType == "Baseboard" && vDimmerBB && tstatBB){
		subscribe (vDimmerBB, "switch", "BBOnOffHandler")
    	subscribe (vDimmerBB, "level", "BBHandler")
    }
    if (scenarioType == "Speaker" && vDimmerSpeaker && speaker) {
    	subscribe (vDimmerSpeaker, "level", "speakerVolHandler")
        subscribe (vDimmerSpeaker, "switch", "speakerOnHandler")
        if (nextSwitch) subscribe (nextSwitch, "switch.on", "controlNextHandler")
        if (prevSwitch) subscribe (prevSwitch, "switch.on", "controlPrevHandler") 
        if (parent.getSonos() && speaker.name.contains("Sonos")){
			for (int i = 1; i <= sonosSlots(); i++) {
               	if (settings."song${i}Switch" && settings."song${i}Station"){
                   	saveSelectedSong(i,settings."song${i}Station")
                    subscribe (settings."song${i}Switch", "switch.on", "controlSong")
                }
            }
		}
    }
	if (scenarioType == "Panic"){
		if (panicSwitchOn) subscribe (panicSwitchOn, "switch.on", "panicOn")
        if (panicSwitchOff) subscribe (panicSwitchOff, "switch.on", "panicOff") 
        if (alarmSonos && parent.getSonos() && alarmSonos.name.contains("Sonos") && alarmSonosSound) getAlarmSound()
	}
    if (scenarioType == "Voice" && voiceControl && (voiceDevice || voiceSpeaker)) subscribe(voiceControl, "switch.on", "voiceHandler")
}
//Handlers
//Mode/Routine/Devices/HTTP/SHM-----------------------------------------------------------------
def switchHandler(evt) {
    if (getOkToRun("Control Scenario on/off")) {    
        if (evt.value == "on" && getOkOnOptions()) {
            if (!delayOn || delayOn == 0) turnOn() 
            else {
            	runIn(delayOn*60, turnOn, [overwrite: true])
				if (parent.getNotifyFeed()){
    				def logText = "Alexa Helper Scenario: '${app.label}' ON triggered. Will activate in ${delayOn} minutes."
                    sendNotificationEvent(logText)
    			}
            }          
    	} 
    	else if (evt.value == "off" && getOkOffOptions()) {
        	if (delayOff || delayOff == 0) turnOff()
            else {
            	runIn(delayOff*60, turnOff, [overwrite: true])
                if (parent.getNotifyFeed()){
    				def logText = "Alexa Helper Scenario: '${app.label}' OFF triggered. Will activate in ${delayOn} minutes."
                    sendNotificationEvent(logText)
    			}
            }
    	}
	}
}
def turnOn(){
    if (parent.getNotifyFeed()){
		def logText = "Alexa Helper Scenario: '${app.label}' ON activated."
		sendNotificationEvent(logText)
	}
    if (onPhrase) location.helloHome.execute(onPhrase)
	if (onMode) changeMode(onMode)
    if (onSwitches && onSwitchesCMD) onSwitchesCMD == "toggle" ? toggleState(onSwitches) : onSwitches?."$onSwitchesCMD"()
    if (onDimmers && onDimmersCMD){
        if (onDimmersCMD == "set"){
        	def level = onDimmersLVL ? onDimmersLVL as int : 0
        	level = level < 0 ? level=0 : level >100 ? level=100 : level
        	onDimmers?.setLevel(level)
        }
        else onDimmersCMD == "toggle" ? toggleState(onDimmers) : onDimmers?."$onDimmersCMD"()
    }
    if (onColoredLights && onColoredLightsCMD){
    	if (onColoredLightsCMD == "set"){
        	def level = onColoredLightsLVL ? onColoredLightsLVL as int : 0
            level = level < 0 ? level=0 : level >100 ? level=100 : level
            onColoredLightsCLR ? setColoredLights(onColoredLights, onColoredLightsCLR, level) : onColoredLights?.setLevel(level)
        }
        else if (onColoredLightsCMD == "toggle") toggleState(onColoredLights)	
        else onColoredLights?."$onColoredLightsCMD"()
    }
    if (onLocks && onLocksCMD){onLocks?."$onLocksCMD"()}
	if (onHTTP){
    	log.info "Attempting to run: ${onHTTP}"
        httpGet("${onHTTP}")   
    }
    if (onSHM){
    	log.info "Setting Smart Home Monitor to ${onSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${onSHM}")
    }
    if (onGarages && onGaragesCMD){onGarages?."$onGaragesCMD"()}
   	if ((onPushMsg || onSMSNum || onContacts) && onSMSMsg){sendMSG(onSMSNum, onSMSMsg, onPushMsg, onContacts)}
}
def turnOff(){
	if (parent.getNotifyFeed()){
		def logText = "Alexa Helper Scenario: '${app.label}' OFF activated."
		sendNotificationEvent(logText)
	}
    if (offPhrase) location.helloHome.execute(offPhrase)
	if (offMode) changeMode(offMode)
    if (offSwitches && offSwitchesCMD) offSwitchesCMD == "toggle" ? toggleState(offSwitches) : offSwitches?."$offSwitchesCMD"()
    if (offDimmers && offDimmersCMD){
    	if (offDimmersCMD == "set"){
        	def level = offDimmersLVL ? offDimmersLVL as int : 0
        	level = level < 0 ? level=0 : level >100 ? level=100 : level
        	offDimmers?.setLevel(level)
        }
        else offDimmersCMD == "toggle" ? toggleState(offDimmers) : offDimmers?."$offDimmersCMD"()
    }
    if (offColoredLights && offColoredLightsCMD){
    	if (offColoredLightsCMD == "set"){
        	def level = offColoredLightsLVL ? offColoredLightsLVL as int : 0
            level = level < 0 ? level=0 : level >100 ? level=100 : level
            offColoredLightsCLR ? setColoredLights(offColoredLights, offColoredLightsCLR, level) : offColoredLights?.setLevel(level)
        }
        else if (offColoredLightsCMD == "toggle") toggleState(offColoredLights)	
        else offColoredLights?."$offColoredLightsCMD"()
    }
    if (offLocks && offLocksCMD) offLocks?."$offLocksCMD"()
    if (offHTTP){
    	log.debug "Attempting to run: ${offHTTP}"
        httpGet("${offHTTP}")
    }
    if (offSHM){
    	log.debug "Setting Smart Home Monitor to ${offSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${offSHM}")
    }
    if (offGarages && offGaragesCMD) offGarages?."$offGaragesCMD"()
   	if ((offPushMsg || offSMSNum || offContacts) && offSMSMsg) sendMSG(offSMSNum, offSMSMSG, offPushMsg, offContacts)
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
        if (alarmSonos && parent.getSonos()  && alarmSonos.name.contains("Sonos") && alarmSonosSound) { 
			if (alarmSonosVolume) alarmSonos.setLevel(alarmSonosVolume as int)
            alarmSonos.playSoundAndTrack (state.alarmSound.uri, state.alarmSound.duration,"")
        }
        if (panicSMSnumberOn || panicPushOn || panicContactsOn){ 
			def smsTxt = panicSMSMsgOn ? panicSMSMsgOn : "Panic was activated without message text input. Please investigate."
            sendMSG(panicSMSnumberOn, smsTxt, panicPushOn, panicContactsOn) 	
		}
		if (parent.getNotifyFeed()){
			def logText = "Alexa Helper Scenario: '${app.label}' PANIC ON activated."
			sendNotificationEvent(logText)
		}        
	}
}
def panicOff(evt){
	if (getOkToRun("Panic actions deactivated")) {
		if (alarmOff){
            alarmTurnOff()
        	if (alarmSonos && parent.getSonos()  && alarmSonos.name.contains("Sonos") && alarmSonosSound) alarmSonos.stop()
		}
		if (panicSMSnumberOff || panicPushOff || panicContactsOff){
			def smsTxt = panicSMSMsgOff ? panicSMSMsgOff : "Panic was deactivated without message text input. Please investigate"
            sendMSG(panicSMSnumberOff, smsTxt, panicPushOff, panicContactsOff) 	
		}
        if (parent.getNotifyFeed()){
			def logText = "Alexa Helper Scenario: '${app.label}' PANIC OFF activated."
			sendNotificationEvent(logText)
		}
	}
}
def alarmTurnOn(){alarm?."$alarmType"()}
def alarmTurnOff(){alarm?.off()}
//Speaker Handlers-----------------------------------------------------------------
def speakerControl(cmd, song, songName, announce){
    if (cmd=="station" || cmd=="on") {
    	//Alexa Switch should be used to prevent looping to this point when switch in turned on
        if (speakerInitial){
        	def speakerLevel = speakerInitial as int
    		vDimmerSpeaker.setLevel(speakerLevel)
        }
        else vDimmerSpeaker.setLevel(speaker.currentValue("level") as int)
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
        	speakerLevel = upLimitSpeaker && (vDimmerSpeaker.currentValue("level") > upLimitSpeaker) ? upLimitSpeaker : speakerLevel
			speakerLevel = lowLimitSpeaker && (vDimmerSpeaker.currentValue("level") < lowLimitSpeaker) ? lowLimitSpeaker : speakerLevel
    		speaker.setLevel(speakerLevel)
		}
	}
}    
//Speaker on/off
def speakerOnHandler(evt){if (getOkToRun("Speaker on/off")) {if (evt.value == "on" || evt.value == "off" ) speakerControl(evt.value,"","","")}}
def controlNextHandler(evt){if (getOkToRun("Speaker next track")) speaker.nextTrack()}
def controlPrevHandler(evt){if (getOkToRun("Speaker previous track")) speaker.previousTrack()}
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
    		tstatLevel = upLimitTstat && vDimmerTstat.currentValue("level") > upLimitTstat ? upLimitTstat : tstatLevel
        	tstatLevel = lowLimitTstat && vDimmerTstat.currentValue("level") < lowLimitTstat ? lowLimitTstat : tstatLevel
			//Turn thermostat to proper level depending on mode	
    		if (tstatMode == "heat" || tstatMode == "auto") tstat.setHeatingSetpoint(tstatLevel)		
    		if (tstatMode == "cool" || tstatMode == "auto") tstat.setCoolingSetpoint(tstatLevel)	
    		log.info "Thermostat set to ${tstatLevel}"
		}
    }
}
//Baseboard Handlers-----------------------------------------------------------------
def BBOnOffHandler(evt){if (getOkToRun("Baseboard(s) turned ${evt.value}")) BBOnOff(evt.value)}
def BBHandler(evt){
	if (getOkToRun("Baseboard Temperature change")) {
    	def tstatBBLevel = vDimmerBB.currentValue("level") as int
        tstatBBLevel = upLimitTstatBB && vDimmerBB.currentValue("level") > upLimitTstatBB ? upLimitTstatBB : tstatBBLevel
        tstatBBLevel = lowLimitTstatBB && vDimmerBB.currentValue("level") < lowLimitTstatBB ? lowLimitTstatBB : tstatBBLevel
        tstatBB.each {
        	it.setHeatingSetpoint(tstatBBLevel)
        	log.info "${it} set to '${tstatBBLevel}'"
            if (it.name.contains("Stelpro")) {
            	log.info "Applying ${tstatBBLevel} setpoint to '${it}'"
            	it.applyNow()
            }
        }
    }
}
def BBOnOff(status){
	if (status=="on" && setpointBBOn) vDimmerBB.setLevel(setpointBBOn)
    if (status=="off" && setpointBBOff) vDimmerBB.setLevel(setpointBBOff)	
}
//Voice Reporting Handlers-----------------------------------------------------------------
def voiceHandler(evt){
	if (getOkToRun("Voice Reporting")) {
    	def fullMsg = voicePre ? "${replaceVoiceVar(voicePre)} " : ""
        if (voiceOnSwitchOnly) fullMsg += voiceSwitch ? switchOnReport() : ""
        else fullMsg += voiceSwitch ? reportStatus(voiceSwitch, "switch") : ""
        if (voiceOnDimmerOnly) fullMsg += voiceDimmer ? dimmerOnReport() : ""
        else fullMsg += voiceDimmer ? reportStatus(voiceDimmer, "level") : ""
        fullMsg += (voiceTemperature) ? reportStatus(voiceTemperature, "temperature") : ""
        if (voiceTempSettingSummary) fullMsg += (voiceTempSettings) ? thermostatSummary(): ""
        else fullMsg += (voiceTempSettings) ? reportStatus(voiceTempSettings, "thermostatSetpoint") : ""
        fullMsg += voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? doorWindowReport() : ""
        fullMsg += voiceMode ? "The current SmartThings mode is set to, '${location.currentMode}'. " : ""
        fullMsg += voiceSHM ? "The current Smart Home Monitor status is '${location.currentState("alarmSystemStatus")?.value}'. " : ""
        fullMsg += voicePost ? "${replaceVoiceVar(voicePost)} " : ""
        log.info fullMsg
    	if (voiceVolume && voiceSpeaker) voiceSpeaker.setLevel(voiceVolume as int)
    	if (voiceSpeaker) voiceSpeaker.playText(fullMsg)
        if (voiceDevice) voiceDevice?.speak("${fullMsg}")
    }
}
//Common Methods-------------
def getOkToRun(module){
	def result = true
    if (parent.getRestrictions()) result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd)   
	if (result) log.info "Alexa Helper scenario '${app.label}', '${module}' triggered"
	else log.warn "Alexa Helper scenario '${app.label}', '${module}' not triggered due to scenario restrictions"
    result
}
def getOkOnOptions(){def result = (!showOptions || showOptions == "1") && (onPhrase || onMode || onSwitches || onDimmers || onColoredLights || onLocks || onGarages || onHTTP || onSHM || onSMSMsg)}
def getOkOffOptions(){def result = (!showOptions || showOptions == "2") && (offPhrase || offMode || offSwitches || offDimmers || offColoredLights || offLocks || offGarages || offHTTP || offSHM || offSMSMsg)}
def changeMode(newMode) {
    if (location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) setLocationMode(newMode)
        else log.warn "Unable to change to undefined mode '${newMode}'"
	}
}
def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
    if(start && end) timeLabel = "Between " + hhmm(start) + " and " +  hhmm(end)
    else if (start) timeLabel = "Start at " + hhmm(start)
    else if (end) timeLabel = "End at " + hhmm(end)
	timeLabel	
}
def scenarioDesc(){
	def desc = scenarioType=="Control" && AlexaSwitch ? "'${AlexaSwitch}' switch controls the scenario" : ""
    if (scenarioType=="Speaker"){
    	desc = vDimmerSpeaker && speaker ? "'${vDimmerSpeaker}' dimmer controls '${speaker}' speaker": ""
        desc += nextSwitch && desc ? "\n'${nextSwitch}' switch activates Next Track" : ""
        desc += prevSwitch && desc ? "\n'${prevSwitch}' switch activates Previous Track" : ""
        for (int i = 1; i <= sonosSlots(); i++) {
        	desc += parent.getSonos() && (speaker && speaker.name.contains("Sonos")) && settings."song${i}Switch" && settings."song${i}Station" && desc ? "\n'" + settings."song${i}Switch" + "' switch activates '" + settings."song${i}Station" + "'" : ""	
        }
    }
    if (scenarioType=="Thermostat"){
    	desc = vDimmerTstat && tstat ? "'${vDimmerTstat}' dimmer controls '${tstat}' thermostat" : ""
        desc += heatingSwitch && desc ? "\n'${heatingSwitch}' switch activates 'Heat' mode" : ""
        desc += coolingSwitch && desc ? "\n'${coolingSwitch}' switch activates 'Cool' mode" : ""
        desc += autoSwitch && desc ? "\n'${autoSwitch}' switch activates 'Auto' mode" : ""
        desc += parent.getNest() && nestHome && desc ? "\n'${nestHome}' switch activates 'Home' mode" : ""
        desc += parent.getNest() && nestAway && desc? "\n'${nestAway}' switch activates 'Away' mode" : ""
    }
    if (scenarioType=="Panic"){
    	desc = panicSwitchOn ? "'${panicSwitchOn}' switch activates panic actions." : ""
        desc += panicSwitchOn && panicSwitchOff && desc ?"\n'${panicSwitchOff}' switch deactivates panic actions." : ""
    }
    if (scenarioType=="Baseboard") desc = vDimmerBB && tstatBB ? "'${vDimmerBB}' dimmer controls ${parseList(tstatBB)}" : ""
    if (scenarioType=="Voice"){
    	def noun = ""
        if (voiceSpeaker && !voiceDevice) noun = "'${voiceSpeaker}'"
        else if (!voiceSpeaker && voiceDevice) noun = "'${voiceDevice}'"
        else if (voiceSpeaker && voiceDevice) noun = "'${voiceSpeaker}' and '${voiceDevice}'}"
    	desc = voiceControl && noun ? "'${voiceControl}' controls voice reports on ${noun}" : ""
    }
    desc = desc ? desc : "Status: UNCONFIGURED - Tap to configure scenario"
}
def parseList(list){
    def listSize = list.size()
    def result = listSize == 1 ? "baseboard heater " : "baseboard heaters "
    for (listName in list) {
		result += "'${listName}'"
        listSize = listSize -1
        if (listSize) result += ", "
    }
    result
}
def reportDesc(param1, param2, param3) {def result = param1 || param2 || param3  ? "Status: CONFIGURED - Tap to edit" : "Status: UNCONFIGURED - Tap to configure"}
def getDeviceDesc(type){  
    def result, switches, dimmers, cLights, locks, garages
    def lvl, cLvl, clr
    def cmd = []
    if (type == "on"){
        cmd = [switch: onSwitchesCMD, dimmer: onDimmersCMD, cLight: onColoredLightsCMD, lock: onLocksCMD, garage: onGaragesCMD]
        switches = onSwitches && cmd.switch ? onSwitches : ""
        lvl = cmd.dimmer == "set" && onDimmersLVL ? onDimmersLVL as int : 0
        dimmers = onDimmers && cmd.dimmer ? onDimmers : ""
        cLvl = cmd.cLight == "set" && onColoredLightsLVL ? onColoredLightsLVL as int : 0
        clr = cmd.cLight == "set" && onColoredLightsCLR ? onColoredLightsCLR  : ""
        cLights = onColoredLights && cmd.cLight ? onColoredLights : ""
        locks = onLocks && cmd.lock ? onLocks : ""
        garages = onGarages && cmd.garage ? onGarages : ""
    }
    if (type == "off"){
		cmd = [switch: offSwitchesCMD, dimmer: offDimmersCMD, cLight: offColoredLightsCMD, lock: offLocksCMD, garage: offGaragesCMD]
        switches = offSwitches && cmd.switch ? offSwitches : ""
        lvl = cmd.dimmer == "set" && offDimmersLVL ? offDimmersLVL as int : 0
        dimmers = offDimmers && cmd.dimmer ? offDimmers : ""
        cLvl = cmd.cLight == "set" && offColoredLightsLVL ? offColoredLightsLVL as int : 0
        clr = cmd.cLight == "set" && offColoredLightsCLR ? offColoredLightsCLR : ""
        cLights = offColoredLights && cmd.cLight ? offColoredLights : ""
        locks = offLocks && cmd.lock ? offLocks : ""
        garages = offGarages && cmd.garage ? offGarages : ""
    }
    lvl = lvl < 0 ? lvl = 0 : lvl >100 ? lvl=100 : lvl
    cLvl = cLvl < 0 ? cLvl = 0 : cLvl >100 ? cLvl=100 : cLvl
    if (switches || dimmers || cLights || locks || garages) {
    	result = switches  ? "${switches} set to ${cmd.switch}" : ""
        result += result && dimmers ? "\n" : ""
        result += dimmers && cmd.dimmer != "set" ? "${dimmers} set to ${cmd.dimmer}" : ""
        result += dimmers && cmd.dimmer == "set" ? "${dimmers} set to ${lvl}%" : ""	
        result += result && cLights ? "\n" : ""
    	result += cLights && cmd.cLight != "set" ? "${cLights} set to ${cmd.cLight}":""
        result += cLights && cmd.cLight == "set" ? "${cLights} set to " : ""
        result += cLights && cmd.cLight == "set" && clr ? "${clr} and " : ""
        result += cLights && cmd.cLight == "set" ? "${cLvl}%" : ""
        result += result && locks ? "\n":""
        result += locks ? "${locks} set to ${cmd.lock}" : ""
        result += result && garages ? "\n" : ""
        result += garages ? "${garages} set to ${cmd.garage}" : ""
    }
    result = result ? result : "Status: UNCONFIGURED - Tap to configure"
}
def greyOutState(param1, param2, param3){def result = param1 || param2 || param3 ? "complete" : ""}
def greyOutScen(){def result = (scenarioType=="Control" && AlexaSwitch) || (scenarioType=="Speaker" && vDimmerSpeaker && speaker) || (scenarioType=="Thermostat" && vDimmerTstat && tstat) || (scenarioType=="Panic" && panicSwitchOn && panicSwitchOff) ? "complete" : ""}
private hhmm(time) {new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("h:mm a", timeZone(time))}
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
	def result = true
    def currTime = now()
	def start = startTime ? timeToday(startTime).time : null
	def stop = endTime ? timeToday(endTime).time : null
	if (startTime && endTime) result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	else if (startTime) result = currTime >= start
    else if (endTime) result = currTime <= stop
    result
}
def getColorOptions(){def colors=[["Soft White":"Soft White"],["White":"White - Concentrate"],["Daylight":"Daylight - Energize"],["Warm White":"Warm White - Relax"],"Red","Green","Blue","Yellow","Orange","Purple","Pink","User Defined"]}
private setColoredLights(switches, color, level){
    def hueColor = 100
	def satLevel = 100
	switch(color) {
		case "White":
			hueColor = 52
			satLevel = 19
			break;
		case "Daylight":
			hueColor = 52
			satLevel = 16
			break;
		case "Soft White":
			hueColor = 23
			satLevel = 56
			break;
		case "Warm White":
			hueColor = 13
			satLevel = 30 
			break;
		case "Blue":
			hueColor = 64
			break;
		case "Green":
			hueColor = 37
			break;
		case "Yellow":
			hueColor = 16
			break;
		case "Orange":
			hueColor = 8
			break;
		case "Purple":
			hueColor = 78
			break;
		case "Pink":
			hueColor = 87
			break;
		case "Red":
			break;
		case "Custom-User Defined":
        	hueColor = hueUserDefined ? hueUserDefined : 0
            satLevel = satUserDefined ? satUserDefined : 0
            hueColor = hueColor>100 ? hueColor=100 : hueColor<0 ? hueColor=0 : hueColor
            satLevel = satLevel>100 ? satLevel=100 : satLevel<0 ? satLevel=0 : satLevel
            break;
	}
    def newValue = [hue: hueColor as int, saturation: satLevel as int, level: level as int]
    switches?.setColor(newValue)
}
def songOptions(slot) {
    if (speaker) {
		// Make sure current selection is in the set
		def options = new LinkedHashSet()
        if (state."selectedSong${slot}"?.station) options << state."selectedSong${slot}".station
		else if (state."selectedSong${slot}"?.description) options << state."selectedSong${slot}".description
        // Query for recent tracks
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
	catch (Throwable t) {log.error t}
}
//Voice report---------------------------------------------------
def switchOnReport(){
    def result = ""
    if (voiceSwitch.latestValue("switch").contains("on")) voiceSwitch.each {deviceName-> if (deviceName.latestValue("switch")=="on") result += "${deviceName} is on. "}
    else result += "All of the monitored switches are off. "
	result
}
def dimmerOnReport(){
    def result = ""
    if (voiceDimmer.latestValue("switch").contains("on")) voiceDimmer.each {deviceName-> if (deviceName.latestValue("switch")=="on") result += "${deviceName} is on and set to ${deviceName.latestValue("level")}%. "}
    else result += "All of the monitored dimmers are off. "
	result
}
def thermostatSummary(){
	def result = ""
    def monitorCount = voiceTempSettings.size()
    def matchCount = 0
    def notMatchList = ""
    for (device in voiceTempSettings) {if (device.latestValue("thermostatSetpoint") as int != voiceTempTarget as int) matchCount += 1}
    if (!matchCount) result += "All thermostats are set to ${voiceTempTarget} degrees. "
    else if (matchCount == monitorCount) result += "None of the thermostats are set to ${voiceTempTarget} degrees. "
    else if (matchCount != monitorCount && matchCount) {
    	result += "Some of the thermostats are set to ${voiceTempTarget} degrees except"
        for (device in voiceTempSettings){
        	if (device.latestValue("thermostatSetpoint") as int != voiceTempTarget as int){
            	result += " ${device}"
                matchCount = matchCount -1
                result += matchCount && matchCount == 1 ? " and" : matchCount && matchCount > 1 ? ", " : ". "
    		}
		}
	}
    result
}
def reportStatus(deviceList, type){
	def result = ""
    def appd = type=="temperature" || type=="thermostatSetpoint" ? "degrees" : ""
    if (type!= "thermostatSetpoint") deviceList.each {deviceName->result += "${deviceName} is ${deviceName.latestValue(type)} ${appd}. " }
    else deviceList.each {deviceName->result += "${deviceName} is set to ${deviceName.latestValue(type) as int} ${appd}. " }
    result
}
def doorWindowReport(){
    def unlocked = voiceDoorLocks && voiceDoorLocks.latestValue("lock").contains("unlocked")
    def opened = voiceDoorSensors && voiceDoorSensors.latestValue("contact").contains("open")
    def openedDoor = voiceDoorControls && voiceDoorControls.latestValue("door").contains("open")
    def countOpened = 0 
    def countOpenedDoor = 0
    def countUnlocked = 0
    def result= ""
    def listOpened= "" 
    def listUnlocked = ""
    if (opened){
    	for (sensor in voiceDoorSensors) if (sensor.latestValue("contact")=="open") countOpened += 1
        for (sensor in voiceDoorSensors){
			if (sensor.latestValue("contact")=="open"){
				listOpened +=" ${sensor}"
                countOpened = countOpened - 1
				if (countOpened == 1 && !openedDoor) listOpened += "and the "
                else if (countOpened > 1 || openedDoor) listOpened += ", "
			}
		}
	}
	if (openedDoor){
        for (door in voiceDoorControls) if (door.latestValue("door") == "open") countOpenedDoor += 1
        for (door in doorControls){	
            if (door.latestValue("door") == "open"){
				listOpened += " ${door}"
                countOpenedDoor = countOpenedDoor - 1
                if (countOpenedDoor == 1) listOpened += "and the "
                else if (countOpenedDoor > 1) listOpened += ", "
        	}
    	}
    }
    if (unlocked){
        for (doorLock in voiceDoorLocks) if (doorLock.latestValue("lock")=="unlocked") countUnlocked += 1
        for (doorLock in voiceDoorLocks){	
            if (doorLock.latestValue("lock")=="unlocked"){
				listUnlocked += " ${doorLock}"
                countUnlocked = countUnlocked - 1
                if (countUnlocked == 1) listUnlocked += "and the "
        		else if (countUnlocked > 1) listUnlocked += ", "
            }
    	}    
    }
    def totalCount = countOpenedDoor + countOpened
    if (voiceDoorAll){
    	if (!opened && !unlocked && !openedDoor) result += "All of the doors and windows are closed and locked. "
        if (!opened && !openedDoor && unlocked){
   			result += "All of the doors and windows are closed, but the "
            result += countUnlocked > 1 ? "following are unlocked: ${listUnlocked}. " :"${listUnlocked} is unlocked. "
    	}
        if ((opened || openedDoor) && !unlocked){
   			result += "All of the doors are locked, but the "
            result += totalCount > 1 ? "following doors or windows are open: ${listOpened}. " : "${listOpened} is open. "
    	}
    }   
	else {
		if ((opened || openedDoor) && !unlocked) result += totalCount > 1 ? "The following doors or windows are currently open: ${listOpened}. " : "${listOpened} is open. "
        if (!opened && !openedDoor && unlocked) result += countUnlocked > 1 ? "The following doors are unlocked: ${listUnlocked}. " : "The ${listUnlocked} is unlocked. "
	}
    if ((opened || openedDoor) && unlocked){
		def verb = totalCount > 1 ? "following doors or windows are currently open: ${listOpened}" : "${listOpened} is open"
		def verb1 = countUnlocked > 1 ? "following are unlocked: ${listUnlocked}" : "${listUnlocked} is unlocked"
		result += "The ${verb}. Also, the ${verb1}. "
    }
    result
}
//Send Messages
def sendMSG(num, msg, push, recipients){
    def logText =""
    if (num){logText = "SMS Message '${msg}' sent to ${num}"}
    if (push) {logText = "Message '${msg}' pushed to SmartApp"}
    if (num && push) {logText = "Message '${msg}' sent to ${num} and pushed to SmartApp"}
    if (location.contactBookEnabled && recipients) {logText = "Message '${msg}' sent to ${recipients}"}
    if (parent.getNotifyFeed()) sendNotificationEvent(logText)
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
	def soundUri
    def soundLength = alarmSonosTimer && alarmSonosTimer < 60 ? alarmSonosTimer : 60
    switch(alarmSonosSound) {
    	case "1":
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AlarmSirens/AlarmSiren1.mp3", duration: "${soundLength}"]
        	break;
        case "2":
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AlarmSirens/AlarmSiren2.mp3", duration: "${soundLength}"]
        	break;
        case "3":
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AlarmSirens/AlarmSiren3.mp3", duration: "${soundLength}"]
        	break;
		case "4":
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AlarmSirens/AlarmSiren4.mp3", duration: "${soundLength}"]
        	break;
        case "5":
        	soundUri =[uri: "${alarmSonosCustom}", duration: "${soundLength}"]
            break;
    }
    state.alarmSound = soundUri
}
def sonosSlots(){def slots = parent.getMemCount() as int}
private replaceVoiceVar(msg) {
    def df = new java.text.SimpleDateFormat("EEEE")
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date())
    def time = parseDate(now(), "h:mm a")
    def month = parseDate(now(), "MMMM")
    def year = parseDate(now(), "yyyy")
    def dayNum = parseDate(now(), "d")
    def temp = voiceTempVar ? "${voiceTempVar.latestValue("temperature")} degrees" : "undefined device"
    msg = msg.replace('%day%', day)
    msg = msg.replace('%date%', "${month} ${dayNum}, ${year}")
    msg = msg.replace('%time%', "${time}")
    msg = msg.replace('%temp%', "${temp}")
    msg
}
private parseDate(epoch, type){
   	long longDate = Long.valueOf(epoch).longValue()
    def parseDate = new Date(longDate).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", parseDate).format("${type}", timeZone(parseDate))
}
//Version
private def textVersion() {def text = "Child App Version: 2.8.5 (03/12/2016)"}
private def versionInt() {def text = 285}