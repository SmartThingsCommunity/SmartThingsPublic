/**
 *  Alexa Helper-Child
 *
 *  Copyright © 2016 Michael Struck
 *  Version 2.5.0 2/7/16
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
}
def pageStart() {
	dynamicPage(name: "pageStart", title: "Scenario Settings", uninstall: true, install: true) {
		section {
       		def parentVersion = parent.versionInt()
			if (parentVersion < 420){
				paragraph "You are using a version of the parent app that is older than the recommended version. Please upgrade "+
					"to the latest version to ensure you have the latest features and bug fixes."
			}
            label title:"Scenario Name", required:true
    	   	input "scenarioType", "enum", title: "Scenario Type...", options: [["Control":"Modes/Routines/Devices/HTTP/SHM"],["Panic":"Panic Commands"],"Speaker","Thermostat"], required: false, multiple: false, submitOnChange:true
        	if (scenarioType){
                href "page${scenarioType}", title: "${scenarioType} Scenario Settings", description: scenarioDesc(), state: greyOutScen()
			}
		}
		if (scenarioType && parent.getRestrictions()){
			section("Restrictions") {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        		href "timeIntervalInput", title: "Only during a certain time...", description: getTimeLabel(timeStart, timeEnd), state: greyOutState(timeStart, timeEnd)
            	input "runMode", "mode", title: "Only during the following modes...", multiple: true, required: false
			}
        }
        section("Tap the button below to remove this scenario only"){
		}
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
		if (phrases) {
        	phrases.sort()
		}	
        if (!showOptions || showOptions == "1") {
        	section ("When switch is on..."){
        		if (phrases) {
            		input "onPhrase", "enum", title: "Perform this routine", options: phrases, required: false
            	}
        		input "onMode", "mode", title: "Change to this mode", required: false
            	input "onSHM", "enum",title: "Change Smart Home Monitor to...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false
            	href "pageSTDevicesOn", title: "SmartThings Device Control...", description: getDeviceDesc("on")
            	input "onHTTP", "text", title:"Run this HTTP request...", required: false
            	input "delayOn", "number", title: "Delay in minutes", defaultValue: 0, required: false
                input "onSMSNum", "phone", title: "Send SMS message (phone number)...", required: false
                input "onSMSMsg", "text", title: "Message to send...", required: false
			}
        }
        if (!showOptions || showOptions == "2") {
        	section ("When switch is off..."){
        		if (phrases) {
            		input "offPhrase", "enum", title: "Perform this routine", options: phrases, required: false
            	}
        		input "offMode", "mode", title: "Change to this mode", required: false
                input "offSHM", "enum",title: "Change Smart Home Monitor to...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false
                href "pageSTDevicesOff", title: "SmartThings Device Control...", description: getDeviceDesc("off")
                input "offHTTP", "text", title:"Run this HTTP request...", required: false
                input "delayOff", "number", title: "Delay in minutes", defaultValue: 0, required: false
                input "offSMSNum", "phone", title: "Send SMS message (phone number)...", required: false
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
        	if (onSwitches){
        		input "onSwitchesCMD", "enum", title: "Switches command", options:["on":"Turn on","off":"Turn off", "toggle":"Toggle the switches' on/off state"], multiple: false, required: false
    		}
    	}
    	section ("Dimmers"){
    		input "onDimmers", "capability.switchLevel", title: "Control these dimmers...", multiple: true, required: false, submitOnChange:true
        	if (onDimmers){
        		input "onDimmersCMD", "enum", title: "Dimmer command", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the dimmers' on/off state"], multiple: false, required: false, submitOnChange:true
   			}
        	if (onDimmersCMD == "set" && onDimmers){
        		input "onDimmersLVL", "number", title: "Dimmer level", description: "Set dimmer level", required: false, defaultValue: 0
        	}
   		}
        section ("Colored Lights"){
        	input "onColoredLights", "capability.colorControl", title: "Control these colored lights...", multiple: true, required: false, submitOnChange:true
			if (onColoredLights){
        		input "onColoredLightsCMD", "enum", title: "Colored lights command", options:["on":"Turn on","off":"Turn off","set":"Set color and level", "toggle":"Toggle the lights' on/off state"], multiple: false, required: false, submitOnChange:true
   			}
            if (onColoredLightsCMD == "set" && onColoredLights){
        		input "onColoredLightsCLR", "enum", title: "Choose a color...", required: false, multiple:false, options: getColorOptions(), submitOnChange:true
                if (onColoredLightsCLR == "User Defined"){
                	input "hueUserDefined", "number", title: "Colored light hue", description: "Set colored light hue (0 to 100)", required: false, defaultValue: 0
                	input "satUserDefined", "number", title: "Colored light saturation", description: "Set colored light saturation (0 to 100)", required: false, defaultValue: 0
                }
                input "onColoredLightsLVL", "number", title: "Colored light level", description: "Set colored light level", required: false, defaultValue: 0
            }
    	}
        section ("Locks"){
        	input "onLocks","capability.lock", title: "Control these locks...", multiple: true, required: false, submitOnChange:true
        	if (onLocks){
            	input "onLocksCMD", "enum", title: "Locks command", options:["lock":"Lock","unlock":"Unlock"], multiple: false, required: false
            }
        }
        section("Garage Doors"){
        	input "onGarages","capability.garageDoorControl", title: "Control these garage doors...", multiple: true, required: false, submitOnChange:true
            if (onGarages){
            	input "onGaragesCMD", "enum", title: "Garge door command", options:["open":"Open","close":"Close"], multiple: false, required: false
            }
        }
	}
}
// Show "pageSTDevicesOff" page
def pageSTDevicesOff(){
	dynamicPage (name: "pageSTDevicesOff", title: "SmartThings Device Control", install: false, uninstall: false) {
		section ("Switches"){
    		input "offSwitches", "capability.switch", title: "Control these switches...", multiple: true, required: false, submitOnChange:true
        	if (offSwitches){
        		input "offSwitchesCMD", "enum", title: "Command sent to switches", options:["on":"Turn on","off":"Turn off", "toggle":"Toggle the switches state"], multiple: false, required: false
    		}
    	}
    	section ("Dimmers"){
    		input "offDimmers", "capability.switchLevel", title: "Control these dimmers...", multiple: true, required: false , submitOnChange:true
        	if (offDimmers){
        		input "offDimmersCMD", "enum", title: "Dimmer command", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the switches state"], multiple: false, required: false, submitOnChange:true
   			}
        	if (offDimmersCMD == "set" && offDimmers){
        		input "offDimmersLVL", "number", title: "Dimmer level", description: "Set dimmer level", required: false, defaultValue: 0
        	}
   		}
        section ("Colored Lights"){
        	input "offColoredLights", "capability.colorControl", title: "Control these colored lights...", multiple: true, required: false, submitOnChange:true
			if (offColoredLights){
        		input "offColoredLightsCMD", "enum", title: "Colored lights command", options:["on":"Turn on","off":"Turn off","set":"Set color and level", "toggle":"Toggle the switches state"], multiple: false, required: false, submitOnChange:true
   			}
            if (offColoredLightsCMD == "set" && offColoredLights){
        		input "offColoredLightsCLR", "enum", title: "Choose a color...", required: false, multiple:false, options: getColorOptions()
                input "offColoredLightsLVL", "number", title: "Colored light level", description: "Set colored light level", required: false, defaultValue: 0
        	}
    	}
        section ("Locks"){
        	input "offLocks","capability.lock", title: "Control these locks...", multiple: true, required: false, submitOnChange:true
        	if (offLocks){
            	input "offLocksCMD", "enum", title: "Locks command", options:["lock":"Lock","unlock":"Unlock"], multiple: false, required: false
            }
        }
        section("Garage Doors"){
        	input "offGarages","capability.garageDoorControl", title: "Control these garage doors...", multiple: true, required: false, submitOnChange:true
            if (offGarages){
            	input "offGaragesCMD", "enum", title: "Garge door command", options:["open":"Open","close":"Close"], multiple: false, required: false
            }
        }
	}
}
// Show "Panic" page
def pagePanic() {
	dynamicPage(name: "pagePanic", title: "Panic Scenario Settings", install: false, uninstall: false) {
        section {
			input "panicSwitchOn", "capability.momentary", title: "ON Control Switch (Momentary)", multiple: false, required: true, submitOnChange:true
          	if (panicSwitchOn){
            	input "panicSwitchOff", "capability.momentary", title: "OFF Control Switch (Momentary)", multiple: false, required: false, submitOnChange:true
            }
        }
        section ("When panic is activated..."){
        	input "alarm", "capability.alarm", title: "Activate alarms...", multiple: true, required: false, submitOnChange:true
            if (parent.getSonos()){
            	input "alarmSonos", "capability.musicPlayer", title: "Use Sonos as alarm...", multiple: false , required: false , submitOnChange:true
			}
            if (alarm){
            	input "alarmType", "enum", title: "Choose an alarm type", options: ["strobe":"Strobe light", "siren":"Siren", "both":"Both stobe and siren"], multiple: false, required: false  
            	input "alarmTimer", "number", title:"Alarm turns off automatically after (minutes)", required: false
            }
            if (alarmSonos && parent.getSonos()  && alarmSonos.name.contains("Sonos")){
            	//Speaker alarm options
                input "alarmSonosVolume", "number", title:"Sonos alarm volume", required: false
                input "alarmSonosSound", "enum", title:"Sonos alarm sound", options: [1:"Alarm 1-European Siren", 2:"Alarm 2-Sci-Fi Siren", 3:"Alarm 3-Police Car Siren", 4:"Alarm 4-Red Alert",5:"Custom-User Defined"], multiple: false, required: false, submitOnChange:true 
                if (alarmSonosSound == "5"){
                	input "alarmSonosCustom", "text", title:"URL/Location of custom sound...", required: false
                }
                input "alarmSonosTimer", "number", title:"Alarm turns off automatically after (seconds)", required: false
            }
            if (alarmSonos && parent.getSonos()  && !alarmSonos.name.contains("Sonos")){
            	paragraph "You have chosen a speaker for your alarm that is not supported. Currently, only Sonos speakers can be used as alarms. Please choose a Sonos speaker."
            }
            input "panicSMSnumberOn", "phone", title: "Send SMS message to (phone number)...", required: false
            input "panicSMSMsgOn","text",title: "Message to send...", required: false
        }
        if (panicSwitchOn && panicSwitchOff){
        	section ("When panic is deactivated..."){
        		if (alarm || alarmSonos){
            		input "alarmOff", "bool", title: "Turn off alarm?", defaultValue: false
            	}
            	input "panicSMSnumberOff", "phone", title: "Send SMS message to (phone number)...", required: false
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
					if (settings."song${i}Switch"){
						input "song${i}Station", "enum", title: "Song/Station #${i}", description: "Tap to select recently played song/station", multiple: false, 
							required: false, options: songOptions("${i}")
					}
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
        section ("Thermostat Setpoints"){
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
    childApps.each {child ->
		log.info "Installed Scenario: ${child.label}"
    }
    //--------------------------------------------------------------------
    if (scenarioType == "Control"){
        if (AlexaSwitch){
        	subscribe(AlexaSwitch, "switch", "switchHandler")
        }
	}
	//--------------------------------------------------------------------
    if (scenarioType == "Thermostat"){
    	if (vDimmerTstat && tstat){
            subscribe (vDimmerTstat, "switch.off", "thermoOffHandler")
    		subscribe (vDimmerTstat, "level", "thermoHandler")
        	if (heatingSwitch) {subscribe (heatingSwitch, "switch.on", "heatHandler")}
        	if (coolingSwitch) {subscribe (coolingSwitch, "switch.on", "coolHandler")}
        	if (autoSwitch) {subscribe (autoSwitch, "switch.on", "autoHandler")}
		}
        if (parent.getNest()){
        	if (nestHome){subscribe(nestHome, "switch.on", "nestHomeHandler")}
            if (nestAway){subscribe(nestAway, "switch.on", "nestAwayHandler")}
		}
    }
    //--------------------------------------------------------------------
    if (scenarioType == "Speaker"){
    	if (vDimmerSpeaker && speaker) {
    		subscribe (vDimmerSpeaker, "level", "speakerVolHandler")
        	subscribe (vDimmerSpeaker, "switch", "speakerOnHandler")
            if (nextSwitch) {subscribe (nextSwitch, "switch.on", "controlNextHandler")}
        	if (prevSwitch) {subscribe (prevSwitch, "switch.on", "controlPrevHandler")} 
            if (parent.getSonos() && speaker.name.contains("Sonos")){
				for (int i = 1; i <= sonosSlots(); i++) {
                	if (settings."song${i}Switch" && settings."song${i}Station"){
                    	subscribe (settings."song${i}Switch", "switch.on", "controlSong")
                    }
                }
			}
        }
    }
    //--------------------------------------------------------------------
	if (scenarioType == "Panic"){
		if (panicSwitchOn){subscribe (panicSwitchOn, "switch.on", "panicOn")}
        if (panicSwitchOff){subscribe (panicSwitchOff, "switch.on", "panicOff")}
        if (alarmSonos && parent.getSonos() && alarmSonos.name.contains("Sonos") && alarmSonosSound){
        	getAlarmSound()
		}
	}
}
//Handlers
//Mode/Routine/Devices/HTTP/SHM-----------------------------------------------------------------
def switchHandler(evt) {
    if (getOkToRun("Control Scenario on/off")) {    
        if (evt.value == "on" && getOkOnOptions()) {
            !delayOn || delayOn == 0 ? turnOn() : runIn(delayOn*60, turnOn, [overwrite: true])
    	} 
    	else if (evt.value == "off" && getOkOffOptions()) {
        	!delayOff || delayOff == 0 ? turnOff() : runIn(delayOff*60, turnOff, [overwrite: true])
    	}
	}
}
def turnOn(){
    if (onPhrase){location.helloHome.execute(onPhrase)}
	if (onMode) {changeMode(onMode)}
    if (onSwitches && onSwitchesCMD){
    	onSwitchesCMD == "toggle" ? toggleState(onSwitches) : onSwitches?."$onSwitchesCMD"()
    }
    if (onDimmers && onDimmersCMD){
        if (onDimmersCMD == "set"){
        	def level = onDimmersLVL ? onDimmersLVL as int : 0
        	if (level < 0) {level=0}
        	if (level >100) {level=100}
        	onDimmers?.setLevel(level)
        }
        else {
        	onDimmersCMD == "toggle" ? toggleState(onDimmers) : onDimmers?."$onDimmersCMD"()
		}
    }
    if (onColoredLights && onColoredLightsCMD){
    	if (onColoredLightsCMD == "set"){
        	def level = onColoredLightsLVL ? onColoredLightsLVL as int : 0
            if (level < 0) {level=0}
        	if (level >100) {level=100}
            if (onColoredLightsCLR){
            	setColoredLights(onColoredLights, onColoredLightsCLR, level)
            }
            else {
            	onColoredLights?.setLevel(level)
            }
        }
        else if (onColoredLightsCMD == "toggle"){
    		toggleState(onColoredLights)	
        }
        else {
        	onColoredLights?."$onColoredLightsCMD"()
		}
    }
    if (onLocks && onLocksCMD){onLocks?."$onLocksCMD"()}
	if (onHTTP){
    	log.debug "Attempting to run: ${onHTTP}"
        httpGet("${onHTTP}")   
    }
    if (onSHM){
    	log.debug "Setting Smart Home Monitor to ${onSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${onSHM}")
    }
    if (onGarages && onGaragesCMD){onGarages?."$onGaragesCMD"()}
    if (onSMSNum && onSMSMsg){sendMSG(onSMSNum, onSMSMsg)}
}
def turnOff(){
	if (offPhrase){location.helloHome.execute(offPhrase)}
	if (offMode) {changeMode(offMode)}
    if (offSwitches && offSwitchesCMD){
    	offSwitchesCMD == "toggle" ? toggleState(offSwitches) : offSwitches?."$offSwitchesCMD"()
    }
    if (offDimmers && offDimmersCMD){
    	if (offDimmersCMD == "set"){
        	def level = offDimmersLVL ? offDimmersLVL as int : 0
        	if (level < 0) {level=0}
        	if (level >100) {level=100}
        	offDimmers?.setLevel(level)
        }
        else {
			offDimmersCMD == "toggle" ? toggleState(offDimmers) : offDimmers?."$offDimmersCMD"()
        }
    }
    if (offColoredLights && offColoredLightsCMD){
    	if (offColoredLightsCMD == "set"){
        	def level = offColoredLightsLVL ? offColoredLightsLVL as int : 0
            if (level < 0) {level=0}
        	if (level >100) {level=100}
            if (offColoredLightsCLR){
            	setColoredLights(offColoredLights, offColoredLightsCLR, level)
            }
            else {
            	offColoredLights?.setLevel(level)
            }
        }
        else if (offColoredLightsCMD == "toggle"){
    		toggleState(offColoredLights)	
        }
        else {
        	offColoredLights?."$offColoredLightsCMD"()
        }
    }
    if (offLocks && offLocksCMD){offLocks?."$offLocksCMD"()}
    if (offHTTP){
    	log.debug "Attempting to run: ${offHTTP}"
        httpGet("${offHTTP}")
    }
    if (offSHM){
    	log.debug "Setting Smart Home Monitor to ${offSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${offSHM}")
    }
    if (offGarages && offGaragesCMD){offGarages?."$offGaragesCMD"()}
    if (offSMSNum && offSMSMsg){sendMSG(offSMSNum,offSMSMSG)}
}
//Panic Handlers-----------------------------------------------------------------
//Panic On
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
			if (alarmSonosVolume){
                alarmSonos.setLevel(alarmSonosVolume as int)
			}
            alarmSonos.playSoundAndTrack (state.alarmSound.uri, state.alarmSound.duration,"")
        }
        if (panicSMSnumberOn){ 
			def smsTxt = panicSMSMsgOn ? panicSMSMsgOn : "Panic was activated/deactivated without message text input. Please investigate"
            sendMSG(panicSMSnumberOn, smsTxt) 	
		}	
	}
}
//Panic Off
def panicOff(evt){
	if (getOkToRun("Panic actions deactivated")) {
		if (alarmOff){
			alarmTurnOff()
        	if (alarmSonos && parent.getSonos()  && alarmSonos.name.contains("Sonos") && alarmSonosSound) { 
            	alarmSonos.stop()
        	}
		}
		if (panicSMSnumberOff){
			def smsTxt = panicSMSMsgOff ? panicSMSMsgOff : "Panic was activated/deactivated without message text input. Please investigate"
            sendMSG(panicSMSnumberOff, smsTxt) 	
		}
	}
}
//AlarmOn
def alarmTurnOn(){
	alarm?."$alarmType"()
}
//AlarmOff
def alarmTurnOff(){
	alarm?.off()
}
//Speaker Handlers-----------------------------------------------------------------
def speakerControl(cmd,song){
    if (cmd=="station" || cmd=="on") {
    	//Alexa Switch should be used to prevent looping to this point when switch in turned on
        if (speakerInitial){
        	def speakerLevel = speakerInitial as int
    		vDimmerSpeaker.setLevel(speakerLevel)
        }
        else {
            vDimmerSpeaker.setLevel(speaker.currentValue("level") as int)
        }
		if (cmd=="station"){
    		log.debug "Playing: ${song}"
			speaker.playTrack(song)
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
    	if (speakerLevel == 0) {vDimmerSpeaker.off()}
    	else {
        	// Get settings between limits
        	speakerLevel = upLimitSpeaker && (vDimmerSpeaker.currentValue("level") > upLimitSpeaker) ? upLimitSpeaker : speakerLevel
			speakerLevel = lowLimitSpeaker && (vDimmerSpeaker.currentValue("level") < lowLimitSpeaker) ? lowLimitSpeaker : speakerLevel
            //Turn speaker to proper volume
    		speaker.setLevel(speakerLevel)
		}
	}
}    
//Speaker on/off
def speakerOnHandler(evt){
	if (getOkToRun("Speaker on/off")) {
    	if (evt.value == "on" || evt.value == "off" ){
    		speakerControl(evt.value,"")
    	}
	}
}
def controlNextHandler(evt){
    if (getOkToRun("Speaker next track")) {
		speaker.nextTrack()
	}
}
def controlPrevHandler(evt){
    if (getOkToRun("Speaker previous track")) {
		speaker.previousTrack()   
	}
}
def controlSong(evt){
	def trigger = evt.displayName
    if (getOkToRun("Speaker Saved Song/Station Trigger: ${trigger}")) {
       	for (int i = 1; i <= sonosSlots(); i++) {
        	if (settings."song${i}Switch" && trigger == settings."song${i}Switch".label){
        		speakerControl("station",state."selectedSong${i}")
            }
        }
	}
}
//Thermostat Handlers-----------------------------------------------------------------
//Thermostat Off
def thermoOffHandler(evt){
	if (getOkToRun("Thermostat turned off")) {
   		tstat.off()
	}
}
//Thermostat mode changes
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
def nestHomeHandler(evt){
	if (getOkToRun("Thermostat mode:Home")) {	
		tstat.present()
	}
}
def nestAwayHandler(evt){
   	if (getOkToRun("Thermostat mode:Away")) {	
		tstat.away()
	}
}
//Thermostat Temp Handler
def thermoHandler(evt){
    if (getOkToRun("Temperature change")) {
    // Get settings between limits 
    	def tstatMode=tstat.currentValue("thermostatMode")
        if (tstatMode != "auto" || (tstatMode == "auto" && autoControlTstat)){
        	def tstatLevel = vDimmerTstat.currentValue("level") as int
    		tstatLevel = upLimitTstat && vDimmerTstat.currentValue("level") > upLimitTstat ? upLimitTstat : tstatLevel
        	tstatLevel = lowLimitTstat && vDimmerTstat.currentValue("level") < lowLimitTstat ? lowLimitTstat : tstatLevel
			//Turn thermostat to proper level depending on mode	
    		if (tstatMode == "heat" || tstatMode == "auto") {
        		tstat.setHeatingSetpoint(tstatLevel)	
    		}	
    		if (tstatMode == "cool" || tstatMode == "auto") {
        		tstat.setCoolingSetpoint(tstatLevel)	
    		}
    		log.debug "Thermostat set to ${tstatLevel}"
		}
    }
}
//Common Methods-------------
def getOkToRun(module){
	def result = true
    if (parent.getRestrictions()){
        result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd)  
	}        
	if (result){
		log.debug "Alexa Helper scenario '${app.label}', '${module}' triggered"
	}
	else {
		log.debug "Alexa Helper scenario '${app.label}', '${module}' not triggered due to scenario restrictions"
	}
    result
}
def getOkOnOptions(){
	def result = (!showOptions || showOptions == "1") && (onPhrase || onMode || onSwitches || onDimmers || onColoredLights || onLocks || onGarages || onHTTP || onSHM || (onSMSNum && onSMSMsg))
}
def getOkOffOptions(){
	def result = (!showOptions || showOptions == "2") && (offPhrase || offMode || offSwitches || offDimmers || offColoredLights || offLocks || offGarages || offHTTP || offSHM || (offSMSNum && offSMSMsg))
}
def changeMode(newMode) {
    if (location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
		} 
        else {
			log.debug "Unable to change to undefined mode '${newMode}'"
		}
	}
}
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
def scenarioDesc(){
	def desc = scenarioType=="Control" && AlexaSwitch ? "'${AlexaSwitch}' switch controls the scenario" : "Tap to configure scenario..."
    if (scenarioType=="Speaker"){
    	desc = vDimmerSpeaker && speaker ? "'${vDimmerSpeaker}' dimmer controls '${speaker}' speaker": desc
        desc += nextSwitch ? "\n'${nextSwitch}' switch activates Next Track" : ""
        desc += prevSwitch ? "\n'${prevSwitch}' switch activates Previous Track" : ""
        for (int i = 1; i <= sonosSlots(); i++) {
        	desc += parent.getSonos() && (speaker && speaker.name.contains("Sonos")) && settings."song${i}Switch" && settings."song${i}Station" ? "\n'" + settings."song${i}Switch" + "' switch activates '" + settings."song${i}Station" + "'" : ""	
        }
    }
    if (scenarioType=="Thermostat"){
    	desc = vDimmerTstat && tstat ? "'${vDimmerTstat}' dimmer controls '${tstat}' thermostat" : desc
        desc += heatingSwitch ? "\n'${heatingSwitch}' switch activates 'Heat' mode" : ""
        desc += coolingSwitch ? "\n'${coolingSwitch}' switch activates 'Cool' mode" : ""
        desc += autoSwitch ? "\n'${autoSwitch}' switch activates 'Auto' mode" : ""
        desc += parent.getNest() && nestHome ? "\n'${nestHome}' switch activates 'Home' mode" : ""
        desc += parent.getNest() && nestAway ? "\n'${nestAway}' switch activates 'Away' mode" : ""
    }
    if (scenarioType=="Panic"){
    	desc = panicSwitchOn ? "'${panicSwitchOn}' switch activates panic actions." : desc
        desc += panicSwitchOn && panicSwitchOff ?"\n'${panicSwitchOff}' switch deactivates panic actions." : ""
    }
    desc
}
def getDeviceDesc(type){  
    def result, switches, dimmers, cLights, locks, garages = ""
    def lvl, cLvl, clr
    def dimmerCMD, switchCMD, cLightCMD, lockCMD, garageCMD = ""
    if (type == "on"){
        switchCMD = onSwitchesCMD
        dimmerCMD = onDimmersCMD
        cLightCMD = onColoredLightsCMD
        lockCMD = onLocksCMD
        garageCMD = onGaragesCMD
        switches = onSwitches && switchCMD ? onSwitches : ""
        lvl = dimmerCMD == "set" && onDimmersLVL ? onDimmersLVL as int : 0
        dimmers = onDimmers && dimmerCMD ? onDimmers : ""
        cLvl = cLightCMD == "set" && onColoredLightsLVL ? onColoredLightsLVL as int : 0
        clr = cLightCMD == "set" && onColoredLightsCLR ? onColoredLightsCLR  : ""
        cLights = onColoredLights && cLightCMD ? onColoredLights : ""
        locks = onLocks && lockCMD ? onLocks : ""
        garages = onGarages && garageCMD ? onGarages : ""
    }
    if (type == "off"){
    	switchCMD = offSwitchesCMD
        dimmerCMD = offDimmersCMD
        cLightCMD = offColoredLightsCMD
        lockCMD = offLocksCMD
        garageCMD = offGaragesCMD
        switches = offSwitches && switchCMD ? offSwitches : ""
        lvl = dimmerCMD == "set" && offDimmersLVL ? offDimmersLVL as int : 0
        dimmers = offDimmers && dimmerCMD ? offDimmers : ""
        cLvl = cLightCMD == "set" && offColoredLightsLVL ? offColoredLightsLVL as int : 0
        clr = cLightCMD == "set" && offColoredLightsCLR ? offColoredLightsCLR : ""
        cLights = offColoredLights && cLightCMD ? offColoredLights : ""
        locks = offLocks && lockCMD ? offLocks : ""
        garages = offGarages && garageCMD ? offGarages : ""
    }
    if (lvl < 0) {lvl = 0}
    if (lvl >100) {lvl=100}
    if (cLvl < 0) {cLvl = 0}
    if (cLvl >100) {cLvl=100}
    if (switches || dimmers || cLights || locks || garages) {
    	result = switches  ? "${switches} set to ${switchCMD}" : ""
        result += result && dimmers ? "\n" : ""
        result += dimmers && dimmerCMD != "set" ? "${dimmers} set to ${dimmerCMD}" : ""
        result += dimmers && dimmerCMD == "set" ? "${dimmers} set to ${lvl}%" : ""	
        result += result && cLights ? "\n" : ""
    	result += cLights && cLightCMD != "set" ? "${cLights} set to ${cLightCMD}":""
        result += cLights && cLightCMD == "set" ? "${cLights} set to " : ""
        result += cLights && cLightCMD == "set" && clr ? "${clr} and " : ""
        result += cLights && cLightCMD == "set" ? "${cLvl}%" : ""
        result += result && locks ? "\n":""
        result += locks ? "${locks} set to ${lockCMD}" : ""
        result += result && garages ? "\n" : ""
        result += garages ? "${garages} set to ${garageCMD}" : ""
    }
    result = result ? result : "Tap to configure SmartThings devices"
}
def greyOutState(param1, param2){
	def result = param1 || param2 ? "complete" : ""
}
def greyOutScen(){
	def result = (scenarioType=="Control" && AlexaSwitch) || (scenarioType=="Speaker" && vDimmerSpeaker && speaker) || (scenarioType=="Thermostat" && vDimmerTstat && tstat) || (scenarioType=="Panic" && panicSwitchOn && panicSwitchOff) ? "complete" : ""
}
private hhmm(time) {
	new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("h:mm a", timeZone(time))
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
	def result = true
    def currTime = now()
	def start = startTime ? timeToday(startTime).time : null
	def stop = endTime ? timeToday(endTime).time : null
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
def getColorOptions(){
	def colors=[["Soft White":"Soft White"],["White":"White - Concentrate"],["Daylight":"Daylight - Energize"],["Warm White":"Warm White - Relax"],"Red","Green","Blue","Yellow","Orange","Purple","Pink","User Defined"]
}
private setColoredLights(switches, color, level){
    def hueColor = 0
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
			hueColor = 100
			break;
		case "Custom-User Defined":
        	hueColor = hueUserDefined ? hueUserDefined : 0
            satLevel = satUserDefined ? satUserDefined : 0
            if (hueColor>100){hueColor=100}
            if (hueColor<0){hueColor=0}
            if (satLevel>100){satLevel=100}
            if (satLevel<0){satLevel=0}
            break;
	}
    def newValue = [hue: hueColor as int, saturation: satLevel as int, level: level as int]
    switches?.setColor(newValue)
}
def songOptions(slot) {
    if (speaker) {
		// Make sure current selection is in the set
		def options = new LinkedHashSet()
        if (state."selectedSong${slot}"?.station) {
			options << state."selectedSong${slot}".station
		}
		else if (state."selectedSong${slot}"?.description) {
			options << state."selectedSong${slot}".description
		}
        // Query for recent tracks
		def states = speaker.statesSince("trackData", new Date(0), [max:30])
		def dataMaps = states.collect{it.jsonValue}
		options.addAll(dataMaps.collect{it.station})

		log.trace "${options.size()} songs in list"
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
		if (data) {
			state."selectedSong${slot}"=data
		}
		else {
			log.warn "Selected song '$song' not found"
		}
	}
	catch (Throwable t) {
		log.error t
	}
}
//SendSMS
def sendMSG(num, msg){
    log.debug "SMS Message '${msg}' sent to ${num}"
    sendSms(num,"${msg}")  	
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
        	break
        case "2":
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AlarmSirens/AlarmSiren2.mp3", duration: "${soundLength}"]
        	break
        case "3":
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AlarmSirens/AlarmSiren3.mp3", duration: "${soundLength}"]
        	break
		case "4":
            soundUri = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/AlarmSirens/AlarmSiren4.mp3", duration: "${soundLength}"]
        	break
        case "5":
        	soundUri =[uri: "${alarmSonosCustom}", duration: "${soundLength}"]
            break
    }
    state.alarmSound = soundUri
}
def sonosSlots(){
    def slots = parent.getMemCount() as int
}
//Version
private def textVersion() {
    def text = "Child App Version: 2.5.0 (02/07/2016)"
}
private def versionInt(){
	def text = 250
}
