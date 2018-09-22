/**
 *  Ask Alexa 
 *
 *  Version 2.1.9a - 11/20/16 Copyright © 2016 Michael Struck
 *  Special thanks for Keith DeLong for overall code and assistance; Barry Burke for Weather Underground Integration; jhamstead for Ecobee climate modes, Yves Racine for My Ecobee thermostat tips
 * 
 *  Version information prior to 2.1.7 listed here: https://github.com/MichaelStruck/SmartThingsPublic/blob/master/smartapps/michaelstruck/ask-alexa.src/Ask%20Alexa%20Version%20History.md
 *
 *  Version 2.1.7 (10/9/16) Allow for flash briefing reports, added audio output devices to control macros
 *  Version 2.1.8e (10/22/16) Added option for reports from Nest Manager application; tweaking of color list to make it more user friendly, added the beginnings of a cheat sheet option
 *  Version 2.1.9a (11/20/16) Used more of the hidable elements in the new SmartThings mobile app (2.2.2+), fixed color light alias bug
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
        	page name: "pageMemorySlots"
            	page name: "pageSONOSReset"
        page name:"pageSensors"
        page name:"pageHomeControl"
        page name:"pageAliasMain"
        	page name:"pageAliasAdd"
            	page name: "pageAliasAddFinal"
            page name:"pageAliasDel"
            	page name: "pageAliasDelFinal"
        page name:"pageMacros"
        page name:"pageSettings"
            page name:"pageReset"
            	page name:"pageConfirmation"
                page name:"pageContCommands"
                page name:"pageMsgQueue"
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
            page name:"pageOutputAudio"
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
            page name:"pagePresenceReport"
            page name:"pageSpeakerReport"
}
def pageMain() { if (!parent) mainPageParent() else mainPageChild() }
def mainPageParent() {
    dynamicPage(name: "mainPageParent", install: true, uninstall: false) {
        def deviceList = getDeviceList()
        def duplicates = deviceList.name.findAll{deviceList.name.count(it)>1}.unique()
        def aliasDups = deviceAlias && state.aliasList ? deviceList.name.intersect(state.aliasList.aliasNameLC) : null
        if (duplicates || findNullDevices() || (aliasDups && deviceAlias)){
        	section ("**WARNING**"){
            	if (duplicates) paragraph "You have the following device(s) used multiple times within Ask Alexa:\n\n${getList(duplicates)}\n\nA device should be uniquely named and appear only once in the categories below.", image: imgURL() + "caution.png"
                if (aliasDups && deviceAlias) paragraph "The following alias(es) conflict with a device name already set up:\n\n${getList(aliasDups)}\n\nAliases should be uniquely named and appear only once within the Ask Alexa SmartApp.", image: imgURL() + "caution.png"
                if (findNullDevices()) paragraph findNullDevices(), image: imgURL() + "caution.png"  
            }
        }
        section("Items to interface to Alexa") {
            href "pageSwitches", title: "Switches/Dimmers/Colored Lights", description:getDesc(switchesSel() || dimmersSel() || cLightsSel()), state: switchesSel() || dimmersSel() || cLightsSel() ? "complete" : null, image:imgURL() + "power.png"
            href "pageDoors", title: "Doors/Windows/Locks", description: getDesc(doorsSel() || locksSel() || ocSensorsSel() || shadesSel()), state: doorsSel() || locksSel() || ocSensorsSel() || shadesSel() ? "complete" : null, image: imgURL() + "lock.png"
            href "pageTemps", title: "Thermostats/Temperature/Humidity", description:getDesc(tstatsSel() || tempsSel() || humidSel()), state: tstatsSel() || tempsSel() || humidSel() ? "complete" : null, image: imgURL() + "temp.png"
            href "pageSpeakers", title: "Connected Speakers", description: getDesc(speakersSel()), state: speakersSel() ? "complete" : null, image:imgURL() + "speaker.png"     
            href "pageSensors", title: "Other Sensors", description:getDesc(waterSel() || presenceSel() || motionSel() || accelerationSel()), state: waterSel() || presenceSel() || motionSel() || accelerationSel() ? "complete" : null, image: imgURL() + "sensor.png"
            href "pageHomeControl", title: "Modes/SHM/Routines", description:getDesc(listModes || listRoutine || listSHM), state: (listModes|| listRoutines|| listSHM ? "complete" : null), image: imgURL() + "modes.png"
            if (deviceAlias && mapDevices(true)) href "pageAliasMain", title: "Device Aliases", description:getDesc(state.aliasList), state: (state.aliasList ?"complete":null), image: imgURL() + "alias.png"     
        }
        section("Configure macros"){ href "pageMacros", title: "Voice Macros", description: macroDesc(), state: (childApps.size() ? "complete" : null), image: imgURL() + "speak.png" }
        section("Options") {
			href "pageSettings", title: "Settings", description: "Tap to configure app settings, get setup information or to reset the access token", image: imgURL() + "settings.png"
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get version information, license, instructions or to remove the application", image: imgURL() + "info.png"
        }
	}
}
def pageSwitches(){
    dynamicPage(name: "pageSwitches", install: false, uninstall: false) {
        section { paragraph "Switches/Dimmers/Colored lights", image: imgURL() + "power.png"} 
        section("Choose the devices to interface", hideWhenEmpty: true) {
            input "switches", "capability.switch", title: "Choose Switches (On/Off/Toggle/Status)", multiple: true, required: false
            input "dimmers", "capability.switchLevel", title: "Choose Dimmers (On/Off/Toggle/Level/Status)", multiple: true, required: false
            input "cLights", "capability.colorControl", title: "Choose Colored Lights (On/Off/Toggle/Level/Color/Status)", multiple: true, required: false, submitOnChange: true
        	
        }
        if (deviceAlias){
            section("Devices that can have aliases", hideWhenEmpty: true) {
                input "switchesAlias", "capability.switch", title: "Choose Switches", multiple: true, required: false
                input "dimmersAlias", "capability.switchLevel", title: "Choose Dimmers", multiple: true, required: false
                input "cLightsAlias", "capability.colorControl", title: "Choose Colored Lights", multiple: true, required: false, submitOnChange: true
            }
        }
        if (cLightsSel()) {
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
        section { paragraph "Doors/Windows/Locks", image: imgURL() + "lock.png" }
        section("Choose the devices to interface", hideWhenEmpty: true) {
			input "doors", "capability.doorControl", title: "Choose Door Controls (Open/Close/Status)" , multiple: true, required: false, submitOnChange: true 
            input "shades", "capability.windowShade", title: "Choose Window Shade Controls (Open/Close/Status)", multiple: true, required: false
			input "ocSensors", "capability.contactSensor", title: "Choose Open/Close Sensors (Status)", multiple: true, required: false
			input "locks", "capability.lock", title: "Choose Locks (Lock/Unlock/Status)", multiple: true, required: false, submitOnChange: true
            
        }
        if (deviceAlias){
            section("Devices that can have aliases", hideWhenEmpty: true) {
            	input "doorsAlias", "capability.doorControl", title: "Choose Door Controls" , multiple: true, required: false, submitOnChange: true 
                input "shadesAlias", "capability.windowShade", title: "Choose Window Shade Controls", multiple: true, required: false
                input "ocSensorsAlias", "capability.contactSensor", title: "Choose Open/Close Sensors", multiple: true, required: false
                input "locksAlias", "capability.lock", title: "Choose Locks", multiple: true, required: false, submitOnChange: true
            }
    	}
        if ((doorsSel() || locksSel())  && pwNeeded){
            section("Security"){
                if (doors) input "doorPW", "bool", title: "Require PIN For Door Actions", defaultValue: false
                if (locks) input "lockPW", "bool", title: "Require PIN For Lock Actions", defaultValue: false
            }
        }
	}    
}
def pageTemps(){
    dynamicPage(name: "pageTemps",  install: false, uninstall: false) {
        section {paragraph "Thermostats/Temperature/Humidity", image: imgURL() + "temp.png"}
        section("Choose the devices to interface", hideWhenEmpty: true) {
            input "tstats", "capability.thermostat", title: "Choose Thermostats (Temperature Setpoint/Status)", multiple: true, required: false, submitOnChange:true 
            input "temps", "capability.temperatureMeasurement", title: "Choose Temperature Devices (Status)", multiple: true, required: false
        	input "humid", "capability.relativeHumidityMeasurement", title: "Choose Humidity Devices (Status)", multiple: true, required: false
        }
        if (deviceAlias){
            section("Devices that can have aliases", hideWhenEmpty: true) {
            	input "tstatsAlias", "capability.thermostat", title: "Choose Thermostats", multiple: true, required: false, submitOnChange:true
            	input "tempsAlias", "capability.temperatureMeasurement", title: "Choose Temperature Devices", multiple: true, required: false
        		input "humidAlias", "capability.relativeHumidityMeasurement", title: "Choose Humidity Devices", multiple: true, required: false
            }
        }
        if (tstatsSel()){
        	section("Default Thermostat Commands"){
            	if (!tstatCool) input "tstatHeat", "bool", title: "Set Heating Setpoint By Default", defaultValue:false, submitOnChange:true
            	if (!tstatHeat) input "tstatCool", "bool", title: "Set Cooling Setpoint By Default", defaultValue:false, submitOnChange:true
    		}
    	}
    }
}
def pageSpeakers(){
    dynamicPage(name: "pageSpeakers",  install: false, uninstall: false) {
        section {paragraph "Connected Speakers", image: imgURL() + "speaker.png"}
        section("Choose the devices to interface", hideWhenEmpty: true) { input "speakers", "capability.musicPlayer", title: "Choose Speakers (Speaker Control, Status)", multiple: true, required: false, submitOnChange: true }
        if (deviceAlias) section("Devices that can have aliases", hideWhenEmpty: true) { input "speakersAlias", "capability.musicPlayer", title: "Choose Speakers", multiple: true, required: false, submitOnChange: true }
        if (sonosCMD && speakersSel() && sonosMemoryCount) section("Memory slots"){ href "pageMemorySlots", title: "SONOS Memory Slots", description: memoryDesc(), state: memoryState() } 
    }
}
def pageMemorySlots(){
    dynamicPage(name: "pageMemorySlots") {
        section {paragraph "SONOS Memory Slots", image: imgURL() + "music.png"}
        def songList = songOptions(), memCount = sonosMemoryCount as int
        for (int i=1; i<memCount+1; i++){
            section ("Memory slot ${i}"){
                input "sonosSlot${i}Name", "text", title: "Memory Slot ${i} Name", required: false
                input "sonosSlot${i}Music", "enum", title: "Song/Channel", required:false, multiple: false, options: songList
            }
        }
    }
}
def pageSensors(){
    dynamicPage(name: "pageSensors",  install: false, uninstall: false) {
        section {paragraph "Other Sensors", image: imgURL() + "sensor.png"}
        section("Choose the devices to interface", hideWhenEmpty: true) {
            input "acceleration", "capability.accelerationSensor", title: "Choose Acceleration Sensors (Status)", multiple: true, required: false
            input "motion", "capability.motionSensor", title: "Choose Motion Sensors (Status)", multiple: true, required: false
            input "presence", "capability.presenceSensor", title: "Choose Presence Sensors (Status)", multiple: true, required: false
            input "water", "capability.waterSensor", title: "Choose Water Sensors (Status)", multiple: true, required: false   
        }
        if (deviceAlias){
            section("Devices that can have aliases", hideWhenEmpty: true) {
            	input "accelerationAlias", "capability.accelerationSensor", title: "Choose Acceleration Sensors", multiple: true, required: false
            	input "motionAlias", "capability.motionSensor", title: "Choose Motion Sensors", multiple: true, required: false
            	input "presenceAlias", "capability.presenceSensor", title: "Choose Presence Sensors", multiple: true, required: false
            	input "waterAlias", "capability.waterSensor", title: "Choose Water Sensors", multiple: true, required: false
            }
		}
    }
}
def pageHomeControl(){
	dynamicPage(name: "pageHomeControl", uninstall: false) {
        def phrases =location.helloHome?.getPhrases()*.label.sort(), findNull=0, phrasesList=[]
        if (phrases) phrases.each{if (!it) findNull++}
        if (findNull) phrases.each{if (it) phrasesList<<it}
        else phrasesList=phrases
		section { paragraph "Modes/SHM/Routines", image: imgURL() + "modes.png" }
        if (findNull) {
        	section("**Warning**"){
            	paragraph "You have a null routine present in your SmartThings setup. Go to your SmartThings IDE to resolve this situation. This routine has been removed from the list below.", image: imgURL() + "caution.png"
            }
        }
        section ("Mode options", hideWhenEmpty: true) {
	        input "listModes", "enum", title: "Choose Modes (Change/Status)", options: location.modes.name.sort(), multiple: true, required: false, submitOnChange: true 
			if (pwNeeded && listModes) input "modesPW", "bool", title: "Require PIN To Change Modes", defaultValue: false
		}
		section ("Smart home monitor options"){
			input "listSHM", "enum", title: "Choose SHM Statuses (Change/Status)", options: ["Arm (Away)","Arm (Stay)","Disarm"], multiple: true, required: false
			if (pwNeeded && listSHM) input "shmPW", "bool", title: "Require PIN To Change SHM", defaultValue: false
		}
		section ("Routine options", hideWhenEmpty: true){
			input "listRoutines","enum", title: "Choose Routines (Execute)", options: phrasesList, multiple: true, required: false
			if (pwNeeded && listRoutines) input "routinesPW", "bool", title: "Require PIN To Execute Routines", defaultValue: false
		}
	}
}
def pageAliasMain(){
    dynamicPage(name: "pageAliasMain",  install: false, uninstall: false) {
        section {paragraph "Device Aliases", image: imgURL() + "alias.png"}
        section("Add / Remove device aliases") {
           	href "pageAliasAdd", title: "Add Device Alias", description: "Tap to add a device alias", image: imgURL() + "add.png"
           	if (state.aliasList) href "pageAliasDel", title: "Delete Device Alias", description: "Tap to delete a device alias", image: imgURL() + "delete.png"    
        }
        section(state.aliasList && state.aliasList.size()==1 ? "One device alias configured" : state.aliasList && state.aliasList.size()>1 ? state.aliasList.size() + " device aliases configured" : "") {
            paragraph state.aliasList && state.aliasList.size()>0 ? getAliasDisplayList(): "There are no device aliases set up yet"            	
        }
    }
}
def pageAliasAdd(){
	dynamicPage(name: "pageAliasAdd", uninstall: false) {
		def getList = []
		mapDevices(true).each {getList<<it.fullListName}        
        section ("Alias information"){
        	input "aliasName", "text", title: "Alias Name"
        	input "aliasType", "enum", title: "Alias Device Type...", options: getList,required:false, submitOnChange: true 
			if (aliasType) input "aliasDevice", "enum", title: "Select Device The Alias Name Will Use...", options: getDeviceAliasList(aliasType), required: false, submitOnChange: true
		}
        if (aliasDevice && aliasName && aliasType && getDeviceAliasList(aliasType)) {
        	section(" "){ href "pageAliasAddFinal", title: "Add Device Alias", description: "Tap to add the device alias", image: imgURL() + "add.png" }
            section("Please note") { paragraph "Do not use the \"<\" or the \"Done\" buttons on this page except to go back without adding the alias.", image: imgURL() + "caution.png" }
		}
	}
}
def pageAliasAddFinal(){
	dynamicPage(name: "pageAliasAddFinal", uninstall: false) {
        if (!state.aliasList && state.aliasList!=[]) state.aliasList=[]
        def success=false, result = ""
        if (state.aliasList.find {it.aliasName.toLowerCase()==aliasName.toLowerCase()} || getDeviceList().find{it.name==aliasName.toLowerCase()})result ="The alias name, '${aliasName}', is already in use. Please choose another name unqiue to the Ask Alexa application."
		else if (!aliasName || !aliasType || !aliasDevice) result="You did not enter all of the proper alias parameters. Go back and ensure all fields are filled in."
		else {
			def devType = [Switch:"switch", Dimmer:"level", "Colored Light":"color", "Door Control":"door", "Window Shade":"shade", "Open/Close Sensor":"contact", "Temperature Sensor":"temperature", Lock:"lock",
            	Thermostat:"thermostat", "Humidity Sensor":"humidity", Speaker:"music", "Acceleration Sensor":"acceleration", "Water Sensor":"water", "Motion Sensor":"motion", "Presence Sensor":"presence"][aliasType]?:aliasType
			result = "Alias Name: ${aliasName}\nAlias Device Type: ${devType}\nDevice: ${aliasDevice}"
            state.aliasList<<["aliasName":aliasName,"aliasType":devType,"aliasDevice":"${aliasDevice}", "aliasTypeFull": aliasType, "aliasNameLC":aliasName.toLowerCase()]
			success = true
        }
		section { paragraph success ? "Successfully Added Alias" : "Error Adding Alias", image: success ? imgURL() + "check.png" : imgURL() + "caution.png" }
        section (" "){ paragraph result }
        section { 
        	href "pageAliasMain", title: "Tap Here To Add/Delete Another Alias", description:none 
			href "mainPageParent", title: "Tap Here To Return To The Main Menu", description:none 
		}
        section("Please note") { paragraph "Do not use the \"<\" or the \"Done\" buttons on this page to go back in the interface. You may encounter undesired results. Please use the two buttons above to return to the alias area or main menu.", image: imgURL() + "caution.png" }
	}    
}
def pageAliasDel(){
	dynamicPage(name: "pageAliasDel", uninstall: false) {
		section { input "aliasDelete", "enum", title:"Choose An Alias To Delete...", options: getAliasList(), required: false, submitOnChange: true }
        if (aliasDelete){
        	section(" "){ href "pageAliasDelFinal", title: "Delete Device Alias", description: "Tap to delete the device alias", image: imgURL() + "delete.png" }
            section("Please note") { paragraph "Do not use the \"<\" or the \"Done\" buttons on this page except to go back without deleting the alias.", image: imgURL() + "caution.png" }
		}
    }
}
def pageAliasDelFinal(){
	dynamicPage(name: "pageAliasDelFinal", uninstall: false) {
        section { paragraph "Successfully Deleted '${aliasDelete}' Alias", image: imgURL() + "check.png" }
        section (" "){
			state.aliasList.removeAll{it.aliasName==aliasDelete}
            href "pageAliasMain", title: "Tap Here To Add/Delete Another Alias", description:none 
            href "mainPageParent", title: "Tap Here To Return To The Main Menu", description:none
        }
        section("Please note") { paragraph "Do not use the \"<\" or the \"Done\" buttons on this page to go back in the interface. You may encounter undesired results. Please use the two buttons above to return to the alias area or main menu.", image: imgURL() + "caution.png" }
	}    
}
def pageMacros() {
    dynamicPage(name: "pageMacros", install: false, uninstall: false) {
        section{ paragraph "Voice Macros", image: imgURL() + "speak.png" }
        if (childApps.size()) section(childApps.size()==1 ? "One voice macro configured" : childApps.size() + " voice macros configured" ){}
        def duplicates = childApps.label.findAll{childApps.label.count(it)>1}.unique()
        if (duplicates){
        	section { paragraph "You have two or more macros with the same name. Please ensure each macro has a unique name and also does not conflict with device names as well. ", image: imgURL() + "caution.png" }
        }
        section(" "){
        	app(name: "childMacros", appName: "Ask Alexa", namespace: "MichaelStruck", title: "Create A New Macro...", description: "Tap to create a new voice macro", multiple: true, image: imgURL() + "add.png")
        }
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
        section { paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa@2x.png" }
        section ("Version numbers") { paragraph "${textVersion()}" } 
        section (title: "Access token / Application ID", hideable: true, hidden: true){
            if (!state.accessToken) OAuthToken()
            def msg = state.accessToken != null ? state.accessToken : "Could not create Access Token. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
            paragraph "Access token:\n${msg}\n\nApplication ID:\n${app.id}"
    	}
        section ("Apache license"){ paragraph textLicense() }
    	section("Instructions") { paragraph textHelp() }
        section("Help") {
        	if (!state.accessToken) paragraph "**You must enable OAuth via the IDE to produce the command cheat sheet**"
            else href url:"${getApiServerUrl()}/api/smartapps/installations/${app.id}/cheat?access_token=${state.accessToken}", style:"embedded", required:false, title:"Display Device/Command Cheat Sheet", 
            	description: "Tap to display the cheat sheet.\nUse Live Logging in the SmartThings IDE to obtain the address for use on your computer broswer.", image: imgURL() + "list.png"
        }
        section("Tap below to remove the application and all macros"){}
	}
}
def pageSettings(){
    dynamicPage(name: "pageSettings", uninstall: false){
        section { paragraph "Settings", image: imgURL() + "settings.png" }
        section ("Additional voice settings"){ 
        	input "briefReply", "bool", title: "Give 'Brief' Device Action Reply", defaultValue: false
            if (briefReply) input "briefReplyTxt", "enum", title: "Brief Reply", options: ["No reply spoken", "Ok", "Done"], required:false, multiple:false, defaultValue: "Ok"
            input "flash", "bool", title: "Enable Flash Briefing", defaultValue: false, submitOnChange: true            
            if (flash) input "flashMacro", "enum", title: "Macro Used For Flash Briefing", options: getMacroList("flash"), required: false, multiple: false
            input "otherStatus", "bool", title: "Speak Additional Device Status Attributes", defaultValue: false
            input "batteryWarn", "bool", title: "Speak Battery Level When Below Threshold", defaultValue: false, submitOnChange: true
            if (batteryWarn) input "batteryThres", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: [5:"<5%",10:"<10%",20:"<20%",30:"<30%",40:"<40%",50:"<50%",60:"<60%",70:"<70%",80:"<80%",90:"<90%",101:"Always play battery level"]
        	input "eventCt", "enum", title: "Default Number Of Past Events to Report", options: optionCount(1,9), required: false, defaultValue: 1
            href "pageMsgQueue", title: "Message Queue Options", description: none, state: (msgQueue ? "complete" : null)
            href "pageContCommands", title: "Personalization", description: none, state: (contError || contStatus || contAction || contMacro ? "complete" : null)
        }
        section ("Other values / variables"){
        	if (dimmersSel() || tstatsSel() || cLightsSel() || speakersSel()) href "pageDefaultValue", title: "Default Command Values (Dimmers, Volume, etc.)", description: "", state: "complete"
            if (speakersSel() || tstatsSel()) href "pageLimitValue", title: "Device Minimum/Maximum Values", description: "", state: "complete"
        	if (!state.accessToken) OAuthToken()
            if (!state.accessToken) paragraph "**You must enable OAuth via the IDE to setup this app**"
            else href url:"${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}", style:"embedded", required:false, title:"Setup Variables", description: "For Amazon developer sites", image: imgURL() + "amazon.png"
        	href "pageGlobalVariables", title: "Text Field Variables", description: none, state: (voiceTempVar || voiceHumidVar || voicePresenceVar ? "complete" : null)
        }
        section("Security"){
            href "pageConfirmation", title: "Revoke/Reset Access Token", description: "Tap to confirm this action", image: imgURL() + "warning.png"
            input "pwNeeded", "bool", title: "Password (PIN) Option Enabled", defaultValue: false, submitOnChange: true
            if (pwNeeded) input "password", "num", title: "Numeric Password (PIN)", description: "Enter a short numeric PIN (i.e. 1234)", required: false
		}
        section ("Advanced") {
            href "pageCustomDevices", title: "Device Specific Commands", description: none, state: (nestCMD || stelproCMD || sonosCMD || ecobeeCMD ? "complete" : null)
            input "advReportOutput", "bool", title: "Advanced Voice Report Filter", defaultValue: false
            input "showURLs", "bool", title: "Display REST URL For Certain Macros", defaultValue: false
            input "deviceAlias", "bool", title: "Allow Device Aliases", defaultValue: false
            label title:"Rename App (Optional)", required:false, defaultValue: "Ask Alexa"
        }
    }
}
def pageMsgQueue(){
    dynamicPage(name: "pageMsgQueue", uninstall: false){
    	section{ paragraph "Message Queue Options", image: imgURL() + "mailbox.png" }
        section (" "){
            input "msgQueue", "bool", title: "Enable Message Queue", defaultValue: false, submitOnChange: true
            if (msgQueue) input "msgQueueOrder", "enum", title: "Message Play Back Order", options:[0:"Oldest to newest", 1:"Newest to oldest"], defaultValue: 0
            if (msgQueue) input "msgQueueNotify", "bool", title: "Notify When Messages Are Present", defaultValue: false
            if (msgQueue) input "msgQueueDelete", "bool", title: "Allow SmartApps To Delete Messages", defaultValue: false   
    	}
    }
}
def pageDefaultValue(){
    dynamicPage(name: "pageDefaultValue", uninstall: false){
        if (dimmersSel() || tstatsSel() || cLightsSel() || speakersSel()){
            section("Increase / Brighten / Decrease / Dim values\n(When no values are requested)"){
                if (dimmersSel() || cLightsSel()) input "lightAmt", "number", title: "Dimmer/Colored Lights", defaultValue: 20, required: false
                if (tstatsSel()) input "tstatAmt", "number", title: "Thermostat Temperature", defaultValue: 5, required: false
                if (speakersSel()) input "speakerAmt", "number", title: "Speaker Volume", defaultValue: 5, required: false
            }
        }
        if (dimmersSel() || cLightsSel()) {
        	section("Low / Medium / High values (For dimmers or colored lights)") {
            	input "dimmerLow", "number", title: "\"Low\" Value", defaultValue: 10, required: false
                input "dimmerMed", "number", title: "\"Medium\" Value", defaultValue: 50, required: false
                input "dimmerHigh", "number", title: "\"High\" Value", defaultValue: 100, required: false
            }
        }
    }
}
def pageContCommands(){
	dynamicPage(name: "pageContCommands", uninstall: false){
		section{ paragraph "Personalization", image: imgURL() + "personality.png" }
        section ("Continuation of commands..."){
			input "contError", "bool", title: "After Error", defaultValue: false
            input "contStatus", "bool", title: "After Status/List", defaultValue: false
            input "contAction", "bool", title: "After Action/Event History", defaultValue: false
            input "contMacro", "bool", title: "After Macro Execution", defaultValue: false
		}
        section ("Personality"){
			input "Personality", "enum", title: "Response Personality Style", options: ["Normal","Courtesy","Snarky"], defaultValue: "Normal", submitOnChange: true
            input "personalName", "text", title: "Name To Address You By (Optional)", description: "%people% variable is available if set up", required: false
            if (Personality=="Snarky") input "randomSnarkName", "bool", title: "Randomize Snarky Response Name", defaultValue: false, submitOnChange: true
			if (Personality=="Snarky" && randomSnarkName){
            	input "snarkName1", "text", title: "Random Snarky Name 1", description: "Knucklehead", required: false
                input "snarkName2", "text", title: "Random Snarky Name 2", description: "Silly", required: false
			}
        }
        section("Other options"){ input "invocationName", title: "Invocation Name (Only Used For Examples)", defaultValue: "SmartThings", required: false }
    }
}
def pageCustomDevices(){
    dynamicPage(name: "pageCustomDevices", uninstall: false){
		section("Device specific commands"){
            input "ecobeeCMD", "bool", title: "Ecobee Specific Thermostat Modes\n(Home/Away/Sleep/Resume Program)", defaultValue: false, submitOnChange: true
            if (ecobeeCMD) input "MyEcobeeCMD", "bool", title: "MyEcobee Specific Tips\n(Get Tips/Play Tips/Erase Tips)", defaultValue: false             
            input "nestCMD", "bool", title: "Nest-Specific Thermostat Presence Commands (Home/Away)", defaultValue: false, submitOnChange: true
            if (nestCMD) input "nestMGRCMD", "bool", title: "Nest Manager Specific Reports (Report)", defaultValue: false
            input "stelproCMD", "bool", title: "Stelpro Baseboard\nThermostat Modes (Eco/Comfort)", defaultValue:false
            input "sonosCMD", "bool", title: "SONOS Options (Memory Slots)", defaultValue: false, submitOnChange: true
            if (sonosCMD) {
				input "sonosMemoryCount", "enum", title: "Maximum number of SONOS memory slots", options: optionCount(2,10), defaultValue: 2, required: false 
                paragraph "To reset the database of SONOS songs listed in the memory slots, tap the area below. It is recommended you do this ONLY if you are having issues playing the songs in the list. " +
                "The database will be rebuilt from the recently played songs from the speakers upon exiting the SmartApp."
            	href "pageSONOSReset", title: "Reset Song Database", description: "Tap to reset database", image: imgURL() + "warning.png"
			}
        }
	}
}
def pageSONOSReset(){
    dynamicPage(name: "pageSONOSReset",title: "Song Database Reset", uninstall: false){
		state.songLoc = []
    	section{ paragraph "The SONOS song database has been reset. Go into the memory slots, choose the songs you wish to play in each slot, then properly exit the SmartApp. This will rebuild the database." }
	}
}
def pageLimitValue(){
    dynamicPage(name: "pageLimitValue", uninstall: false){
        if (speakersSel()){ section("Speaker volume limits"){ input "speakerHighLimit", "number", title: "Speaker Maximum Volume", defaultValue:20, required: false } }
        if (tstatsSel()){
        	section("Thermostat setpoint limits"){
            	input "tstatLowLimit", "number", title: "Thermostat Minimum Value ", defaultValue: 55, required: false
                input "tstatHighLimit", "number", title: "Thermostat Maximum Value", defaultValue: 85, required: false
            }
        }
    }
}
def pageGlobalVariables(){
    dynamicPage(name: "pageGlobalVariables", uninstall: false){
        section { paragraph "Text Field Variables", image: imgURL() + "variable.png" }
        section ("Environmental") {
            input "voiceTempVar", "capability.temperatureMeasurement", title: "Temperature Device Variable (%temp%)",multiple: true, required: false, submitOnChange: true
            input "voiceHumidVar", "capability.relativeHumidityMeasurement", title:"Humidity Device Variable (%humid%)",multiple: true, required: false, submitOnChange: true
            if ((voiceTempVar && voiceTempVar.size()>1) || (voiceHumidVar && voiceHumidVar.size()>1)) paragraph "Please note: When multiple temperature/humidity devices are selected above, the variable output will be an average of the device readings"
        }
        section ("People"){ input "voicePresenceVar", "capability.presenceSensor", title: "Presence Sensor Variable (%people%)", multiple: true, required: false }
    }
}
def pageConfirmation(){
	dynamicPage(name: "pageConfirmation", title: "Revoke/Reset Access Token Confirmation", uninstall: false){
        section {
			href "pageReset", title: "Revoke / Reset Access Token", description: "Tap to take action - READ WARNING BELOW", image: imgURL() + "warning.png"
			paragraph "PLEASE CONFIRM! By resetting the access token you will disable the ability to interface this SmartApp with your Amazon Echo. You will need to copy the new access token to your Amazon Lambda code to re-enable access." +
                "Tap below to go back to the main menu with out resetting the token. You may also tap Done in the upper left corner."
        }
        section(" "){ href "mainPageParent", title: "Cancel And Go Back To Main Menu", description: none }
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
            label title:"Voice Macro Name", required: true, image: imgURL() + "speak.png"
            input "macroType", "enum", title: "Macro Type...", options: [["Control":"Control (Run/Execute)"],["CoRE":"CoRE Trigger (Run/Execute)"],["Group":"Device Group (On/Off/Toggle, Lock/Unlock, etc.)"],["GroupM":"Macro Group (Run/Execute)"],["Voice":"Voice Reporting (Run/Execute) "]], required: false, multiple: false, submitOnChange:true
            def fullMacroName=[GroupM: "Macro Group",CoRE:"CoRE Trigger", Control:"Control", Group:"Device Group", Voice:"Voice Reporting"][macroType] ?: macroType
            if (macroType) {
            	href "page${macroType}", title: "${fullMacroName} Settings", description: macroTypeDesc(), state: greyOutMacro()
                input "noteFeed", "bool", title: "Post To Notification Feed When Triggered", defaultValue: false, submitOnChange: true
                if (noteFeed && macroType==~/CoRE|Control/) input "noteFeedAct", "bool", title: "Post When Activated (i.e. When Delayed)", defaultValue: false
                if (noteFeed) input "noteFeedData", "bool", title: "Include SmartApp's Response To Alexa", defaultValue: false
                if (parent.contMacro) input "overRideMsg", "bool", title: "Override Continuation Commands (Except Errors)" , defaultValue: false
                if (parent.showURLs && macroType==~/Control|GroupM/ &&  macroTypeDesc() !="Status: UNCONFIGURED - Tap to configure macro" && app.label !="Ask Alexa") {
                    href url:"${getApiServerUrl()}/api/smartapps/installations/${parent.app.id}/u?mName=${app.label}&access_token=${parent.state.accessToken}", style:"embedded", required:false, title:"Show REST URL For This Macro", description: none
				}
            }
        }
        if (macroType && macroType !="GroupM" && macroType !="Group"){
            section("Restrictions", hideable: true, hidden: !(runDay || timeIntervalInput || runMode)) {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: imgURL() + "calendar.png"
				href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: imgURL() + "clock.png"
				input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: imgURL() + "modes.png"
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
		section { paragraph "Device Group Settings", image: imgURL() + "folder.png" }
        section (" ") {
            input "groupType", "enum", title: "Group Type...", options: [["colorControl": "Colored Light (On/Off/Toggle/Level/Color)"],["switchLevel":"Dimmer (On/Off/Toggle/Level)"],["doorControl": "Door (Open/Close)"],["lock":"Lock (Lock/Unlock)"],
            	["switch":"Switch (On/Off/Toggle)"],["thermostat":"Thermostat (Mode/Off/Setpoint)"],["windowShade": "Window Shades (Open/Close)"]],required: false, multiple: false, submitOnChange:true
    		if (groupType) input "groupDevice${groupType}", "capability.${groupType}", title: "Choose devices...", required: false, multiple: true, submitOnChange:true
        	if (((groupType == "doorControl" && parent.pwNeeded) || (groupType=="lock" && parent.pwNeeded)) && settings."groupDevice${groupType}" ){
        		input "usePW", "bool", title: "Require PIN For Actions", defaultValue: false
        	}	
        }
        if (groupType == "thermostat"){
        	section ("Thermostat group options"){
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
		section { paragraph "CoRE Trigger Settings", image: imgURL() + "CoRE.png" }
		section (" "){
   			input "CoREName", "enum", title: "Choose CoRE Piston", options: parent.state.CoREPistons, required: false, multiple: false
        	input "cDelay", "number", title: "Default Delay (Minutes) To Trigger", defaultValue: 0, required: false
            if (parent.pwNeeded) input "usePW", "bool", title: "Require PIN To Run This CoRE Macro", defaultValue: false
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after macro runs", required: false, capitalization: "sentences"
             input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true
        }
        if (!parent.state.CoREPistons){
        	section("Missing CoRE pistons"){
				paragraph "It looks like you don't have the CoRE SmartApp installed, or you haven't created any pistons yet. To use this capability, please install CoRE or, if already installed, create some pistons, then try again."
            }
        }	
    }
}
//Group Macro----------------------------------------------------
def pageGroupM() {
	dynamicPage(name: "pageGroupM", install: false, uninstall: false) {
		section { paragraph "Macro Group Settings", image: imgURL() + "macrofolder.png" }
        section (" ") { 
        	input "groupMacros", "enum", title: "Child Macros To Run (Control/CoRE/Voice Reports)...", options: parent.getMacroList("all"), required: false, multiple: true
        	if (parent.pwNeeded){ input "usePW", "bool", title: "Require PIN To Run This Macro", defaultValue: false }
        }
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
        def phrases =location.helloHome?.getPhrases()*.label.sort(), findNull=0, phrasesList=[]
        if (phrases) phrases.each{if (!it) findNull++}
        if (findNull) phrases.each{if (it) phrasesList<<it}
        else phrasesList=phrases
        section { paragraph "Control Settings", image: imgURL() + "control.png" }
        if (findNull) {
        	section("**Warning**"){
            	paragraph "You have a null routine present in your SmartThings setup. Go to your SmartThings IDE to resolve this situation. This routine has been removed from the list below.", image: imgURL() + "caution.png"
            }
        }
        section ("When voice macro is activated...") {
            input "phrase", "enum", title: "Perform This Routine...", options: phrasesList, required: false, image: imgURL() + "routine.png" 
            input "setMode", "mode", title: "Set Mode To...", required: false, image: imgURL() + "modes.png"  
            input "SHM", "enum",title: "Set Smart Home Monitor To...", options: ["away":"Arm (Away)", "stay":"Arm (Stay)", "off":"Disarm"], required: false, image: imgURL() + "SHM.png"
            href "pageSTDevices", title: "Control These SmartThings Devices...", description: getDeviceDesc(), state: deviceGreyOut(), image: imgURL() + "smartthings.png"
            href "pageOutputAudio", title: "Output Audio...", description: getAudioDesc(), state: (ttsMsg && (ttsSpeaker || ttsSynth) ? "complete": none), image: imgURL() + "speaker.png"
            href "pageHTTP", title: "Run This HTTP Request...", description: getHTTPDesc(), state: greyOutStateHTTP(), image: imgURL() + "network.png"
            input "cDelay", "number", title: "Default Delay (Minutes) To Activate", defaultValue: 0, required: false, image: imgURL() + "stopwatch.png"
            input ("contacts", "contact", title: "Send Notifications To...", required: false, image: imgURL() + "sms.png") {
				input "smsNum", "phone", title: "Send SMS Message To (Phone Number)...", required: false, image: imgURL() + "sms.png"
				input "pushMsg", "bool", title: "Send Push Message", defaultValue: false
            }
            input "smsMsg", "text", title: "Send This Message...", required: false, capitalization: "sentences"
            if (parent.pwNeeded) input "usePW", "bool", title: "Require PIN To Run This Macro", defaultValue: false
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after macro runs", required: false, capitalization: "sentences"
             input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true    
        }
	}
}
def pageOutputAudio(){
	dynamicPage (name: "pageOutputAudio", install: false, uninstall: false) {
        section { paragraph "Output Audio", image: imgURL() + "speaker.png"}
        section ("TTS Output"){
        	input "ttsMsg", "text", title: "Speak Message", required: false, capitalization: "sentences"
            input "ttsSpeaker", "capability.musicPlayer", title: "Choose Speakers", multiple: true, required: false, submitOnChange: true
            if (ttsSpeaker) input "ttsVolume", "number", title: "Speaker Volume", description: "0-100%", required: false
            input "ttsSynth", "capability.speechSynthesis", title: "Choose Voice Synthesis Devices", multiple: true, required: false
		}
	}
}
def pageSTDevices(){
	dynamicPage (name: "pageSTDevices", install: false, uninstall: false) {
        section { paragraph "SmartThings Device Control", image: imgURL() + "smartthings.png"}
        section ("Switches", hideWhenEmpty: true){
            input "switches", "capability.switch", title: "Control These Switches...", multiple: true, required: false, submitOnChange:true
            if (switches) input "switchesCMD", "enum", title: "Command To Send To Switches", options:["on":"Turn on","off":"Turn off", "toggle":"Toggle the switches' on/off state"], multiple: false, required: false
        }
        section ("Dimmers", hideWhenEmpty: true){
            input "dimmers", "capability.switchLevel", title: "Control These Dimmers...", multiple: true, required: false , submitOnChange:true
            if (dimmers) input "dimmersCMD", "enum", title: "Command To Send To Dimmers", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the dimmers' on/off state"], multiple: false, required: false, submitOnChange:true
            if (dimmersCMD == "set" && dimmers) input "dimmersLVL", "number", title: "Dimmers Level", description: "Set dimmer level", required: false, defaultValue: 0
        }
        section ("Colored lights", hideWhenEmpty: true){
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
        section ("Thermostats", hideWhenEmpty: true){
            input "tstats", "capability.thermostat", title: "Control These Thermostats...", multiple: true, required: false, submitOnChange:true
            if (tstats) {
            	def tstatOptions=["heat":"Set heating temperature","cool":"Set cooling temperature"]
                if (parent.nestCMD) tstatOptions += ["away":"Nest 'Away' Presence","home":"Nest 'Home' Presence"]
                if (parent.ecobeeCMD) tstatOptions += ["away":"Ecobee 'Away' Climate","home":"Ecobee 'Home' Climate","sleep":"Ecobee 'Sleep' Climate","resume program":"Ecobee 'Resume Program'"]
                if (parent.MyEcobeeCMD) getEcobeeCustomList(tstats).each { tstatOptions += ["${it}":"Ecobee '${it}' Climate"] }
                input "tstatsCMD", "enum", title: "Command To Send To Thermostats", options :tstatOptions , multiple: false, required: false, submitOnChange:true
            }
            if (tstatsCMD =="heat" || tstatsCMD =="cool") input "tstatLVL", "number", title: "Temperature Level", description: "Set temperature level", required: false
        }
        section ("Locks", hideWhenEmpty: true){
            input "locks","capability.lock", title: "Control These Locks...", multiple: true, required: false, submitOnChange:true
            if (locks) input "locksCMD", "enum", title: "Command To Send To Locks", options:["lock":"Lock","unlock":"Unlock"], multiple: false, required: false
        }
        section("Garage doors", hideWhenEmpty: true){
            input "garages","capability.garageDoorControl", title: "Control These Garage Doors...", multiple: true, required: false, submitOnChange:true
            if (garages) input "garagesCMD", "enum", title: "Command To Send To Garage Doors", options:["open":"Open","close":"Close"], multiple: false, required: false
        }
        section("Window shades", hideWhenEmpty: true){
            input "shades","capability.windowShade", title: "Control These Window Shades...", multiple: true, required: false, submitOnChange:true
            if (shades) input "shadesCMD", "enum", title: "Command To Send To Window Shades", options:["open":"Open","close":"Close"], multiple: false, required: false
        }
    }
}
def pageHTTP (){
    dynamicPage(name: "pageHTTP", install: false, uninstall: false){
        section { paragraph "HTTP Request", image: imgURL() + "network.png" }
        section(" "){
            input "extInt", "enum", title: "Choose HTTP Command Type", options:[0:"External REST",1:"Internal (IP, Port, Command)"], required: false, submitOnChange:true
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
        section { paragraph "Voice Reporting Settings", image: imgURL() + "voice.png" }
        section (" ") {
            input "voicePre", "text", title: "Pre Message Before Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false, capitalization: "sentences"
            href "pageSwitchReport", title: "Switch/Dimmer Report", description: reportSwitches(), state: (voiceSwitch || voiceDimmer ? "complete" : null), image: imgURL() + "power.png"
            href "pageDoorReport", title: "Door/Window/Lock Report", description: reportDoors(), state: (voiceDoorSensors || voiceDoorControls || voiceDoorLocks || voiceWindowShades ? "complete": null), image: imgURL() + "lock.png"
            href "pageTempReport", title: "Temperature/Humidity/Thermostat Report", description: reportTemp(), state:(voiceTemperature || voiceTempSettings || voiceHumidity? "complete" : null),image: imgURL() + "temp.png"
            href "pageSpeakerReport", title: "Speaker Report", description: speakerDesc(), state: (voiceSpeaker ? "complete": null), image: imgURL() + "speaker.png"
			href "pageWeatherReport", title: "Weather Report", description: weatherDesc(), state: greyOutWeather(), image : imgURL() + "weather.png"
            href "pagePresenceReport", title: "Presence Report", description: presenceSensors(), state:(voicePresence ? "complete": null), image : imgURL() + "people.png"    
            href "pageOtherReport", title: "Other Sensors Report", description: reportSensors(), state: (voiceWater|| voiceMotion|| voicePower || voiceAccel  ? "complete" :null), image: imgURL() + "sensor.png"
            href "pageHomeReport", title: "Mode and Smart Home Monitor Report", description: reportDescMSHM(), state: (voiceMode|| voiceSHM? "complete": null), image: imgURL() + "modes.png"
            href "pageBatteryReport",title: "Battery Report", description: batteryDesc(), state: (voiceBattery ? "complete" : null), image: imgURL() + "battery.png"
            input "voicePost", "text", title: "Post Message After Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false, capitalization: "sentences"
        	input "allowNullRpt", "bool", title: "Allow For Empty Report (For Group Macros)", defaultValue: false
        }
        if (parent.getAdvEnabled()){
        	section("Advanced"){
            	input "voiceRepFilter", "text", title: "Filter Report Output", description: "Delimit items with comma (ex. xxxxx,yyyyy,zzzzz)", required: false
                input "voiceEvtTimeDate", "bool", title: "Speak Only Time/Date During Event Reports", defaultValue: false
			}
    	}
    }
}
def pagePresenceReport(){
	dynamicPage(name: "pagePresenceReport", install: false, uninstall: false){
    	section { paragraph "Presence Report", image: imgURL() + "people.png" }
        section(" ") {
            input "voicePresence", "capability.presenceSensor", title: "Presence Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true 
            if (voicePresence) input "voicePresRepType", "enum", title: "Presence Sensor Report Type", options:[0:"Each device reports status (Default)",
            	1:"Summary report (If all present/away)",2:"Sensors that are 'present' only", 3:"Sensors that are 'not present' only"], defaultValue: 0, required:false
            if (voicePresence)input "voicePresentEvt", "bool",title: "Report Time Of Last Arrival", defaultValue: false 
            if (voicePresence)input "voiceGoneEvt", "bool",title: "Report Time Of Last Departure", defaultValue: false 
        }
    }
}
def pageSwitchReport(){
    dynamicPage(name: "pageSwitchReport", install: false, uninstall: false){
        section { paragraph "Switch/Dimmer Report", image: imgURL() + "power.png" }
        section("Switch report", hideWhenEmpty: true) {
            input "voiceSwitch", "capability.switch", title: "Switches To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceSwitch) input "voiceOnSwitchOnly", "bool", title: "Report Only Switches That Are On", defaultValue: false
            if (voiceSwitch)input "voiceOnSwitchEvt", "bool",title: "Report Time Of Last On Event", defaultValue: false 
        }
        section("Dimmer report", hideWhenEmpty: true) {
            input "voiceDimmer", "capability.switchLevel", title: "Dimmers To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceDimmer) input "voiceOnDimmerOnly", "bool", title: "Report Only Dimmers That Are On", defaultValue: false
            if (voiceDimmer)input "voiceOnDimmerEvt", "bool",title: "Report Time Of Last On Event", defaultValue: false
        }
    }
}
def pageDoorReport(){
    dynamicPage(name: "pageDoorReport", install: false, uninstall: false){
        section { paragraph "Doors/Windows/Locks", image: imgURL() + "lock.png" }
        section("Doors / Windows / Locks reporting (Open / Unlocked)", hideWhenEmpty: true){
            input "voiceDoorSensors", "capability.contactSensor", title: "Doors/Windows Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            input "voiceDoorControls", "capability.doorControl", title: "Door Controls To Report Their Status...", multiple: true, required: false, submitOnChange: true
            input "voiceDoorLocks", "capability.lock", title: "Locks To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceDoorSensors || voiceDoorControls || voiceDoorLocks)input "voiceDoorAll", "bool", title: "Report Door/Window/Lock Summary Instead Of Individual Device Report", defaultValue: false
            if (voiceDoorSensors || voiceDoorControls)input "voiceDoorEvt", "bool",title: "Report Time Of Last Door/Window Opening", defaultValue: false
            if (voiceDoorLocks)input "voiceLockEvt", "bool",title: "Report Time Of Last Lock Unlocking", defaultValue: false
        }
        section("Window shades reporting", hideWhenEmpty: true){ input "voiceWindowShades", "capability.windowShade", title: "Window Shades To Report Their Status...", multiple: true, required: false } 
    }
}
def pageOtherReport(){
    dynamicPage(name: "pageOtherReport", install: false, uninstall: false){
        section { paragraph "Other Sensors Report", image: imgURL() + "sensor.png" }
        section ("Acceleration sensors", hideWhenEmpty: true){
            input "voiceAccel", "capability.accelerationSensor", title: "Acceleration Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceAccel) input "voiceAccelOnly", "bool",title: "Report Only Sensors That Read 'Active'", defaultValue: false
            if (voiceAccel) input "voiceAccelEvt", "bool",title: "Report Time Of The Last Movement", defaultValue: false 
        }   
        section ("Motion sensors", hideWhenEmpty: true){
            input "voiceMotion", "capability.motionSensor", title: "Motion Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceMotion) input "voiceMotionOnly", "bool",title: "Report Only Sensors That Read 'Active'", defaultValue: false
            if (voiceMotion) input "voiceMotionEvt", "bool",title: "Report Time Of The Last Movement", defaultValue: false 
        }
        section("Power meters", hideWhenEmpty: true){
       		input "voicePower", "capability.powerMeter", title: "Power Meters To Report Energy Use...", multiple: true, required: false
            if (voicePower) input "voicePowerOn", "bool", title: "Report Only Meters Drawing Power", defaultValue: false
        }
        section ("Water report", hideWhenEmpty: true) {
            input "voiceWater", "capability.waterSensor", title: "Water Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceWater) input "voiceWetOnly", "bool", title: "Report Only Water Sensors That Are 'Wet'", defaultValue: false 
        }
    }
}
def pageTempReport(){
    dynamicPage(name: "pageTempReport", install: false, uninstall: false){
        section { paragraph "Temperature/Humidity/Thermostat Report", image: imgURL() + "temp.png" }
        section ("Temperature reporting", hideWhenEmpty: true){
            input "voiceTemperature", "capability.temperatureMeasurement", title: "Devices To Report Temperatures...",multiple: true, required: false, submitOnChange: true
			if (voiceTemperature && voiceTemperature.size() > 1) input "voiceTempAvg", "bool", title: "Report Average Of Temperature Readings", defaultValue: false	
        }
        section ("Humidity reporting", hideWhenEmpty: true){	
            input "voiceHumidity", "capability.relativeHumidityMeasurement", title: "Devices To Report Humidity...",multiple: true, required: false, submitOnChange: true
        	if (voiceHumidity && voiceHumidity.size() > 1) input "voiceHumidAvg", "bool", title: "Report Average Of Humidity Readings", defaultValue: false
        }
        section ("Thermostat setpoint reporting", hideWhenEmpty: true) {
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
        section { paragraph "Speaker Report", image: imgURL() + "speaker.png" }
        section(" "){
            input "voiceSpeaker", "capability.musicPlayer", title: "Speakers To Report Status...", description: "Tap to choose devices", multiple: true, required: false, submitOnChange:true
        	if (voiceSpeaker) input "voiceSpeakerOn", "bool", title: "Report Only Speakers That Are Playing", defaultValue:false
        }
        section("Please Note"){ paragraph "There may be up to a 5 minute delay before SmartThings refreshes the current speaker status. This may cause the report to produce inaccurate results." }
    }
}
def pageBatteryReport(){
    dynamicPage(name: "pageBatteryReport", install: false, uninstall: false){
        section { paragraph "Battery Report", image: imgURL() + "battery.png" }
        section(" "){
            input "voiceBattery", "capability.battery", title: "Devices With Batteries To Monitor...", description: "Tap to choose devices", multiple: true, required: false, submitOnChange:true
            if (voiceBattery) input "batteryThreshold", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: [5:"<5%",10:"<10%",20:"<20%",30:"<30%",40:"<40%",50:"<50%",60:"<60%",70:"<70%",80:"<80%",90:"<90%",101:"Always play battery level"]  
        }
    }
}
def pageHomeReport(){
    dynamicPage(name: "pageHomeReport", install: false, uninstall: false){
        section { paragraph "Mode And Security Report", image: imgURL() + "modes.png" }
        section(" ") {
            input "voiceMode", "bool", title: "Report SmartThings Mode Status", defaultValue: false
            input "voiceSHM", "bool", title: "Report Smart Home Monitor Status", defaultValue: false
        }
    }
}
def pageWeatherReport(){
	dynamicPage(name: "pageWeatherReport", install: false, uninstall: false) {
    	section { paragraph "Weather report", image: imgURL() + "weather.png" }
        section("Weather reporting") {
        	 href "pageWeatherCurrent", title: "Current Weather Report Options", description: none, state: (currWeatherSel() ? "complete" : null)	
             href "pageWeatherForecast", title: "Weather Forecast Options", description: none, state: (foreWeatherSel() ? "complete" : null)	
        }
        section ("Sunrise / Sunset"){    
            input "voiceSunrise", "bool", title: "Speak Today's Sunrise", defaultValue: false
    		input "voiceSunset", "bool", title: "Speak Today's Sunset", defaultValue: false	
        }
        section ("Other Weather Underground information"){
        	input "voiceMoon", "bool", title: "Lunar Rise/Set/Phases", defaultValue:false
            input "voiceTide", "bool", title: "Tide Information", defaultValue: false
            if (currWeatherSel() || foreWeatherSel()) input "voiceWeatherWarnFull", "bool", title: "Give Full Weather Advisories (If Present)", defaultValue: false
        }
        section ("Location") {
        	if (currWeatherSel() || foreWeatherSel() || voiceSunset || voiceSunrise || voiceMoon || voiceTide) input "voiceWeatherLoc", "bool", title: "Speak Location Of Weather Report/Forecast", defaultValue: false
            input "zipCode", "text", title: "Zip Code", required: false
            paragraph "Please Note:\nYour SmartThings location is currently set to: ${location.zipCode}. If you leave the area above blank the report will use your SmartThings location. " +
            	"Enter a zip code above if you want to report on a different location.\n\nData obtained from Weather Underground."
		}
    }
}
def pageWeatherForecast(){
	dynamicPage(name: "pageWeatherForecast", install: false, uninstall: false) {
        section ("Weather forecast options") {
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
def installed() { initialize() }
def updated() { initialize() }
def childUninstalled() {
	sendLocationEvent(name: "askAlexa", value: "refresh", data: [macros: parent ? parent.getCoREMacroList() : getCoREMacroList()] , isStateChange: true, descriptionText: "Ask Alexa macro list refresh")
}
def initialize() {
	if (!parent){
        if (!state.accessToken) log.error "Access token not defined. Ensure OAuth is enabled in the SmartThings IDE."
        subscribe(location, "CoRE", coreHandler)
        if (msgQueue) subscribe(location, "AskAlexaMsgQueue", msgHandler)
        if (msgQueue && msgQueueDelete) subscribe(location, "AskAlexaMsgQueueDelete", msgDeleteHandler)
        if (sonosCMD && sonosMemoryCount) songLocations()
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
	path("/b") { action: [GET: "processBegin"] }
	path("/u") { action: [GET: "getURLs"] }
	path("/setup") { action: [GET: "setupData"] }
    path("/flash") { action: [GET: "flash"] }
    path("/cheat") { action: [GET: "cheat"] }
}
//--------------------------------------------------------------
def processBegin(){
	log.debug "--Begin commands received--"
    def ver = params.Ver 		//Lambda Code Verisons
    def lVer = params.lVer		//Version number of Lambda code
    def date = params.Date		//Version date of Lambda code
    state.lambdaCode = "Lambda Code Version: ${ver} (${date})"
    def LambdaVersion = lVer as int
    def OOD = LambdaVersion < LambdaReq() ? "true" : null
    def persType = Personality ?: "Normal"
    def pName = personalName ? personalName.replaceAll("%people%", getVariableList().people) : ""
    if (randomSnarkName && Personality=="Snarky" ) {
    	def newPName = []
        def newSName1 = snarkName1 ? snarkName1 : ""
        def newSName2 = snarkName2 ? snarkName2 : ""
    	newPName << pName << newSName1 << newSName2
        pName = newPName[Math.abs(new Random().nextInt() % 3)]
    }
    def contOption = contError ? "1" : "0"
    contOption += contStatus ? "1" : "0"
    contOption += contAction ? "1" : "0"
    contOption += contMacro ? "1" : "0"
    return ["OOD":OOD, "continue":contOption,"personality":persType, "SmartAppVer": versionLong(),"IName":invocationName,"pName":pName]
}
def sendJSON(outputTxt){
    if (outputTxt && msgQueueNotify && state.msgQueue && state.msgQueue.size() && outputTxt[-3..-1] != "%5%") {
        def msgCount = state.msgQueue.size(), msgS= msgCount==0 || msgCount>1 ? msgCount+ " messages" : msgCount+" message"
        def ending= outputTxt[-3..-1]
        outputTxt = outputTxt.replaceAll("${ending}", "Please note: you have ${msgS} in your message queue. ${ending}")
    }
    if (outputTxt && outputTxt[-3..-1] == "%5%") outputTxt = outputTxt.replaceAll("%5%","%3%")
    if (outputTxt) log.debug outputTxt[0..-4]
    return ["voiceOutput":outputTxt]
}
def processDevice() {    
	def dev = params.Device.toLowerCase() 	//Label of device
	def op = params.Operator				//Operation to perform
    def numVal = params.Num     			//Number for dimmer/PIN type settings
    def param = params.Param				//Other parameter (color)
    if (dev =~ /message|queue/) msgQueueReply(op)
    else {
        log.debug "-Device command received-"
        log.debug "Dev: " + dev
        log.debug "Op: " + op
        log.debug "Num: " + numVal
        log.debug "Param: " + param
        def num = numVal == "undefined" || numVal =="?" ? 0 : numVal as int
        String outputTxt = ""
        def deviceList, count = 0, aliasDeviceType, aliasDeviceObj, aliasDeviceList, aliasDeviceName
        getDeviceList().each{if (it.name==dev.replaceAll("[^a-zA-Z0-9 ]", "")) {deviceList=it; count++}}
        if (mapDevices(true) && deviceAlias && !count && !deviceList){
			aliasDeviceList = state.aliasList.each { 
				if (it.aliasNameLC==dev.replaceAll("[^a-zA-Z0-9 ]", "")) {
                	aliasDeviceType = it.aliasType
                    aliasDeviceName = it.aliasDevice
                    count++ 
                }
			}
			if (aliasDeviceType) {
				aliasDeviceList=mapDevices(true).find {it.type==aliasDeviceType}    
                if (aliasDeviceList) aliasDeviceObj=aliasDeviceList.devices.find{it.label==aliasDeviceName}
                else outputTxt = "I had a problem finding the alias device you specified. Please check your SmartApp settings to ensure you have a device associated with the alias name. %1%"
			}
        }
        if (count > 1) outputTxt ="The name, '${dev}', is used multiple times in your SmartThings SmartApp. Please rename or remove duplicate items so I may properly utlize them. "   
        else if (deviceList || aliasDeviceList) {
            def deviceObj=deviceList ? deviceList.devices.find{it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() == dev} : aliasDeviceObj
            def devType=deviceList? deviceList.type : aliasDeviceType  
            if (num == 0 && numValue=="0")  outputTxt = getReply (deviceObj, devType, dev, op, num, param) 
            else if (op == "status" || (op=="undefined" && param=="undefined" && num==0 && numVal=="undefined")) outputTxt = getReply (deviceObj, devType, dev, "status", "", "") 
            else if (op == "events" || op == "event") {	
                def finalCount = num != 0 ? num as int : eventCt ? eventCt as int : 0
                if (finalCount>0 && finalCount < 10) outputTxt = getLastEvent(deviceObj, finalCount, dev) + "%3%"
                else if (!finalCount) { outputTxt = "You do not have the number of events you wish to hear specified in your Ask Alexa SmartApp, and you didn't specify a number in your request. %1%" }
                else if (finalCount > 9) { outputTxt = "The maximum number of past events to list is nine. %1%" }
            }
            else outputTxt = getReply (deviceObj, devType, dev, op, num, param)
        }
        if (!count) { outputTxt = "I had some problems finding the device you specified. %1%" }
        sendJSON(outputTxt)
	}
}
//Message Queue Reply
def msgQueueReply(op){
	log.debug "-Message Queue Response-"
    def cmd = op =~ /delete|reset|clear|erase/ ? "delete" : op =~ /play|list|undefined|status/ ? "playback" : ""
    log.debug "Message Queue Command: " + cmd
    String outputTxt = ""
    if (msgQueue){
        def msgCount = state.msgQueue ? state.msgQueue.size() : 0, msgS= msgCount==0 || msgCount>1 ? " messages" : " message"
        if (cmd == "playback"){
      		if (msgCount==0) outputTxt = "You don't have any messages in your message queue. %5%"
            else {
                outputTxt = "You have " + msgCount + msgS + ": "
                state.msgQueue.sort({it.date})
                state.msgQueue.reverse(msgQueueOrder as int? true : false)
                state.msgQueue.each{
                	def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
                	def msgDay = new Date(it.date).format("EEEE, MMMM d, yyyy", location.timeZone)
                	def voiceDay = today == msgDay ? "Today" : "On " + msgDay 
                	def msgTime = new Date(it.date).format("h:mm aa", location.timeZone)
            		outputTxt += "${voiceDay} at ${msgTime}, '${it.appName}' posted the message: '${it.msg}'. "
                }
                outputTxt +="%5%"
            }
        }
        else if (cmd == "delete") {
			state.msgQueue =[]
            outputTxt="I have deleted all of the items from the message queue. %5%"
        }
        else outputTxt="For the message queue, be sure to give a 'play' or 'delete' command. %1%"
    }
    else outputTxt = "You do not have the message queue option enabled in your SmartApp. %1%"
    sendJSON(outputTxt)  
}
//List Request
def processList(){
	log.debug "-List command received-"
	def listType = params.Type	//Help type
    log.debug "List Type: " + listType
    String outputTxt = ""
    def devices, aliasType=""
	if (listType=~/mode/) outputTxt = listModes && listModes.size() >1 ? "The available modes include the following: " + getList(listModes) + ". " : listModes && listModes.size()==1 ? "You have one mode enabled for control named: " + getList(listModes) + ". " : "There are no modes defined within your SmartApp. "
    if (listType=~/security|smart home monitor|SHM/) {outputTxt= listSHM && listSHM.size() > 1? "#Smart Home Monitor commands#" : listSHM && listSHM.size() == 1 ? "@Smart Home Monitor command@":"%Smart Home Monitor commands%"; devices=listSHM }
    if (listType=~/temperature/){
    	if (temps) { outputTxt = "The devices you can get temperature readings from include the following: " + getList(temps) + ". "; aliasType="temperature" }
        if (tstats && temps) { outputTxt +="In addition, the following thermostats can also give you temperature readings: " +  getList(tstats) + ". ";  aliasType="thermostat" }
        if (tstats && tstats.size()>1 && !temps) { outputTxt ="The only devices you have selected for temperature readings are the following thermostats: " +  getList(tstats) + ". " ;aliasType="thermostat" }
        if (tstats && tstats.size()==1 && !temps) { outputTxt ="The only device you have selected for temperature readings is the " +  getList(tstats) + ". " ; aliasType="thermostat" } 
        if (!tstats && !temps) outputTxt="You don't have any devices selected that will provide temperature readings. "
    }
    if (listType=~/thermostat/) { outputTxt = tstats && tstats.size()>1 ? "#thermostats#" : tstats && tstats.size()==1 ? "@thermostat@":"%thermostats%"; devices=tstats; aliasType="thermostat"}
    if (listType=~/humidity/) { outputTxt = humid && humid.size()>1 ? "#humidity sensors#" : humid && humid.size()==1 ? "@humidity sensor@" : "%humidity sensors%"; devices=humid; aliasType="humidity" }  
    if (listType=~/presence/) { outputTxt = presence && presence.size()>1 ? "#presence sensors#" : presence && presence.size()==1 ? "@presence sensor available@" : "%presence sensors%"; devices=presence; aliasType="presence" }
    if (listType=~/acceleration/) { outputTxt = acceleration && acceleration.size()>1 ? "#acceleration sensors#" : acceleration && acceleration.size()==1 ? "@acceleration sensor@" : "%acceleration sensors%"; devices = acceleration; aliasType="acceleration"}
    if (listType=~/motion/) { outputTxt = motion && motion.size()>1 ? "#motion sensors#" : motion && motion.size()==1 ? "@motion sensor@" : "%motion sensors%" ; devices=motion; aliasType="motion"  }   
    if (listType=~/open|close/) { outputTxt = ocSensors && ocSensors.size()>1 ? "#open close sensors#" : ocSensors && ocSensors.size()==1 ? "@open close sensor@" : "%open close sensors%"; devices = ocSensors; aliasType="contact" }
    if (listType=~/dimmer/) { outputTxt = dimmers && dimmers.size()>1 ? "#dimmers#"	: dimmers && dimmers.size()==1 ? "@dimmer@" : "%dimmmers%"; devices=dimmers; aliasType="level" }
    if (listType=~/speaker/) { outputTxt = speakers && speakers.size()>1 ? "#speakers#" : speakers && speakers.size()==1 ? "@speaker@" : "%speakers%" ; devices=speakers; aliasType="music" }
    if (listType=~/door/) { outputTxt = doors && doors.size()>1 ? "You have the following doors you can open or close: " +  getList(doors) + ". " : doors && doors.size()==1 ? "You have one door, " + getList(doors)+ ", selected that you can open or close. " : "%doors%"; aliasType="door"  }
    if (listType=~/shade/) { outputTxt = shades && shades.size()>1 ? "#window shades#" : shades && shades.size()==1 ? "@window shade@" :"%window shades%"; devices = shades; aliasType="shade" }
    if (listType=~/lock/) { outputTxt = locks && locks.size()>1 ? "#locks#" : locks && locks.size()==1 ? "@lock@" :"%locks%"; devices=locks; aliasType="lock" }	
    if (listType=~/colored light/) { outputTxt = cLights && cLights.size()>1 ? "#colored lights#": cLights && cLights.size()==1 ? "@colored light@" : "%colored lights%"; devices=cLights; aliasType="color" }
    if (listType=~/switch/) { outputTxt = switches? "You can turn on, off or toggle the following switches: " +  getList(switches) + ". " : "%switches%" ; aliasType="switch" }
    if (listType=~/routine/) { outputTxt= listRoutines && listRoutines.size()>1 ? "#routines#":listRoutines && listRoutines.size()==1 ? "@routine@" : "%routines%"; devices=listRoutines }
    if (listType=~/water/) { outputTxt= water && water.size()>1 ? "#water sensors#" : water && water.size()==1 ?  "@water sensor@" : "%water sensors%" ; devices=water; aliasType="water" }
    if (listType =~/report/) outputTxt = parseMacroLists("Voice","voice report","play")
    if (listType =~/device/) outputTxt = parseMacroLists("Group","device group","control")
    if (listType =~/control/) outputTxt = parseMacroLists("Control","control macro","run")
    if (listType =~/core/) outputTxt = parseMacroLists("CoRE","core trigger","run")
    if (listType =~/macro group|group macro/) outputTxt = parseMacroLists("GroupM","macro group","run")
    if (listType =~/event/) {
    	outputTxt = "To list events for a device, you must give me the name of that device. " 
    	if (Math.abs(new Random().nextInt() % 2)==1) outputTxt += "For example, you could say, 'tell ${invocationName} to give me the last events for the Bedroom'. " +
        "You may also include the number of events you would like to hear. An example would be, 'tell ${invocationName} to give me the last 4 events for the Bedroom'. "
    }
    if (listType =~/alias/) outputTxt = "You can not list aliases directly. To hear the aliases that are available, choose a specific device catagory to list. For example, if you list the available switch devices, any switch aliases you created will be listed as well. "
    if (listType ==~/colors|color/) outputTxt = cLights ? "The available colors to use with colored lights are: " + getList(fillColorSettings().name) + ". " : "%colored lights%"
    if (listType ==~/group|groups|macro|macros/) outputTxt ="Please be a bit more specific about which groups or macros you want me to list. You can ask me about 'core triggers', 'macro groups', 'device groups', 'control macros' and 'voice reports'. %1%"
    if (listType ==~/sensor|sensors/) outputTxt ="Please be a bit more specific about what kind of sensors you want me to list. You can ask me to list items like 'water', 'open close', 'presence', 'acceleration, or 'motion sensors'. %1%"
    if (listType ==~/light|lights/) outputTxt ="Please be a bit more specific about what kind of lighting devices you want me to list. You can ask me to list devices like 'switches', 'dimmers' or 'colored lights'. %1%"
    if (outputTxt.startsWith("%") && outputTxt.endsWith("%")) outputTxt = "There are no" + outputTxt.replaceAll("%", " ") + "set up within your Ask Alexa SmartApp. "
    if (outputTxt.startsWith("@") && outputTxt.endsWith("@")){
    	if (Math.abs(new Random().nextInt() % 2)==1) outputTxt = "The only available" + outputTxt.replaceAll("@", " ")+ "is the '" +  getList(devices) + "'. "
    	else outputTxt = "You only have one" + outputTxt.replaceAll("@", " ")+ "set up in your app named: '" +  getList(devices) + "'. "
    }
    if (outputTxt.startsWith("#") && outputTxt.endsWith("#")) outputTxt = "The available" + outputTxt.replaceAll("#", " ") + "include the following: "+ getList(devices) + ". "
    if (deviceAlias && aliasType){
    	def aliases =state.aliasList.findAll{it.aliasType==aliasType}
        def ss = aliases && aliases.size()>1 ? "s" : ""
        def preText = outputTxt.startsWith("There are no") || outputTxt.startsWith("You don't") ? "However" : "In addition"
        if (aliases) outputTxt += "${preText}, you have the following alias name${ss} set up for this device catagory: " + getList(aliases.aliasName) + ". "
    }
    if (outputTxt == "") { 
    	outputTxt = "I didn't understand what you wanted information about. " 
    	if (Math.abs(new Random().nextInt() % 3)==1) outputTxt += "Be sure you have populated the developer section with the device names. "
    	outputTxt += "%1%"
    }
    else if (!outputTxt.endsWith("%")) outputTxt += "%2%"
    sendJSON(outputTxt)
}
def parseMacroLists(type, noun, action){
    def macName = "", count = getChildApps().count{it.macroType==type}
    def extraTxt = (type ==~/Control|CoRE/) && count ? "Please note: You can also delay the execution of ${noun}s by adding the number of minutes after the name. For example, you could say, 'tell ${invocationName} to run the Macro in 5 minutes'. " : ""
	macName = count==1 ? "You only have one ${noun} called: " : count> 1 ? "You can ask me to ${action} the following ${noun}s: " : "You don't have any ${noun}s for me to ${action}"
	if (count){
		childApps.each{if (it.macroType==type){
			macName += it.label ; count --
			macName += count>1 ? ", " : count==1 ? " and " : ""
			}	
		}
	}
	return macName + ". " + extraTxt
}
//Macro Group
def processMacroGroup(macroList, msg, append, noMsg, macLabel,macFeed,macFeedData){
    String result = "", feedData =""
    def runCount=0
    if (macroList){ 
        macroList.each{
            childApps.each{child->
                if (child.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){ 
                    result += child.getOkToRun() ? child.macroResults(0,"","","","")  : child.muteRestrictions ? "" : "You have restrictions on '${child.label}' that prevented it from running. %1%"             
                    runCount++
                    if (result.endsWith("%")) result = result[0..-4]
                }
            }
        }
        def extraTxt = runCount > 1 ? "macros" : "macro"
        if (runCount == macroList.size()) {
            if (!noMsg) {
                if (msg && append) result += msg
                if (msg && !append) result = msg
                if (append && !msg) result += "I ran ${runCount} ${extraTxt} in this macro group. "
                if (!append && !msg) result = "I ran ${runCount} ${extraTxt} in this macro group. "
        	}
            else result = " "
        }
        else result = "There was a problem running one or more of the macros in the macro group. %1%"
    }
    else result="There were no macros present within this macro group. Please check your Ask Alexa SmartApp and try again. %1%"
    def data = [alexaOutput: result, num: "undefined", cmd: "undefined", color: "undefined", param: "undefined"]
	sendLocationEvent(name: "askAlexaMacro", value: macLabel, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa ran '${macLabel}' macro group.")
    if (macFeed && macFeedData) feedData=result.endsWith("%") ? " Data sent to Alexa: " + result[0..-4] : " Data sent to Alexa: " + result
    if (macFeed) sendNotificationEvent("Ask Alexa activated Macro Group: '${macLabel}'.${feedData}")
    return result
}
//Macro Processing
def processMacro() {
    log.debug "-Macro command received-"
	def mac = params.Macro 		//Macro name
    def mNum = params.Num		//Number variable-Typically delay to run
    def cmd = params.Cmd		//Group Command
    def param = params.Param	//Parameter
    def mPW = params.MPW		//Macro Password
    log.debug "Macro Name: " + mac
    log.debug "mNum: " + mNum
    log.debug "Cmd: " + cmd
    log.debug "Param: " + param
    log.debug "mPW: " + mPW
    if (mNum == "0" && cmd=="undefined" && param == "undefined") cmd="off"
    def num = mNum == "undefined" || mNum =="?" || !mNum  ? 0 : mNum as int
    String outputTxt = ""
    def macroType="", colorData, err=false, playContMsg
    if (cmd ==~/low|medium|high/){
        if (cmd=="low" && dimmerLow) num = dimmerLow else if (cmd=="low" && !dimmerLow) err=true 
		if (cmd=="medium" && dimmerMed) num = dimmerMed else if (cmd=="medium" && !dimmerMed) err=true 
		if (cmd=="high" && dimmerHigh) num = dimmerHigh else if (cmd=="high" &&!dimmerhigh) err=true
        if (err) outputTxt = "You don't have a default value set up for the ${outputTxt} level. I am not making any adjustments. "
    }
    def getColorData = fillColorSettings().find {it.name.toLowerCase()==param}
    if (getColorData){
        def hueColor = getColorData.hue, satLevel = getColorData.sat
        colorData = [hue: hueColor as int, saturation: satLevel as int] 
    }
	def count = getChildApps().count {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac.toLowerCase()}
    if (!err){
        if (count == 1){
            def macProceed= true, child = getChildApps().find {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac.toLowerCase()}
            if (child.usePW && pwNeeded && password && mPW != password && ((child.macroType == "Group" && child.groupType == "lock") || child.macroType == "GroupM" ||
            	(child.macroType == "Group" && child.groupType == "doorControl") || child.macroType == "Control" || child.macroType == "CoRE")){
                macProceed = false
                if (child.macroType == "Group" && child.groupType == "lock") outputTxt = "To lock or unlock a group, you must use the proper password. %1%"
				if (child.macroType == "Group" && child.groupType == "doorControl") outputTxt = "To open or close a group, you must use the proper password. %1%"
                if (child.macroType == "CoRE") outputTxt = "To activate a Core Trigger, you must use the proper password. %1%"
                if (child.macroType == "Control") outputTxt = "To activate a Control Macro, you must use the proper password. %1%"
                if (child.macroType == "GroupM") outputTxt = "To activate a Group Macro, you must use the proper password. %1%"
			}
            if (child.macroType != "Group" && child.macroType != "GroupM" && cmd=="list" && macProceed) { outputTxt = "You can not use the list command with this type of macro. %1%"; macProceed = false }
            else if (child.macroType == "GroupM" && cmd=="list" && macProceed) {
                def gMacros= child.groupMacros.size()==1 ? "macro" : "macros"
                outputTxt="You have the following ${gMacros} within the '${child.label}' macro group: " + getList(child.groupMacros) +". "
                macProceed = false
            }
            if (macProceed){
            	playContMsg = child.overRideMsg ? false : true
            	def fullMacroName = [GroupM: "Macro Group",CoRE: "CoRE Trigger", Control:"Control Macro", Group:"Device Group", Voice:"Voice Report"][child.macroType] ?: child.macroType
            	if (child.macroType != "GroupM") outputTxt = child.getOkToRun() ? child.macroResults(num, cmd, colorData, param, mNum) : "You have restrictions within the ${fullMacroName} named, '${child.label}', that prevent it from running. Check your settings and try again. %1%"
            	else outputTxt = processMacroGroup(child.groupMacros, child.voicePost, child.addPost, child.noAck, child.label, child.noteFeed, child.noteFeedData)   
        	}
		}
        if (count > 1) outputTxt ="You have duplicate macros named '${mac}'. Please check your SmartApp to fix this conflict. %1%"
        if (!count) outputTxt = "I could not find a macro named '${mac}'. %1%" 
    }
    if (outputTxt && !outputTxt.endsWith("%") && !outputTxt.endsWith(" ")) outputTxt += " "
    if (outputTxt && !outputTxt.endsWith("%") && playContMsg) outputTxt += "%4%"
    sendJSON(outputTxt)
}
//Smart Home Commands
def processSmartHome() {
    log.debug "-Smart home command received-"
	def cmd = params.SHCmd 						//Smart Home Command
	def param = params.SHParam.toLowerCase()	//Smart Home Parameter
    def num = params.SHNum						//Smart Home Password
    log.debug "Cmd: " + cmd
    log.debug "Param: " + param
    log.debug "Num: " + num
    String outputTxt = ""
    if (cmd =="undefined") {
    	if (param=="off") outputTxt="Be sure to specify a device, or the word 'security', when using the 'off' command. %1%"
        if (listModes?.find{it.toLowerCase()==param} && param != currMode) cmd = "mode"
    	if (param==~/list|arm|undefined/) cmd = "security"
        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases.find{it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()==param}) cmd = "routine"
    }
    if (cmd == "mode"){	
		def currMode = location.mode.toLowerCase()
		if (param == "undefined") outputTxt ="The current SmartThings mode is set to, '${currMode}'. "
		if (listModes && param !="undefined"){
        	if (modesPW && pwNeeded && password && num == "undefined") outputTxt = "You must say your password to change your SmartThings mode. %1%"
            if (modesPW && pwNeeded && password && num!="undefined" && num != password) outputTxt="I did not hear the correct password to change your SmartThings mode. %1%"
            if (!modesPW || !pwNeeded || (modesPW && pwNeeded && num == password)){
                if (listModes?.find{it.toLowerCase()==param} && param != currMode) {
                    def newMode=listModes.find{it.toLowerCase()==param}
                    outputTxt ="I am setting the SmartThings mode to, '${newMode}'. "
                    setLocationMode(newMode)
                }
                else if (param == currMode) outputTxt ="The current SmartThings mode is already set to '${currMode}'. No changes are being made. "
                if (!outputTxt) outputTxt = "I did not understand the mode you wanted to set. For a list of available modes, simply say, 'ask ${invocationName} for mode list'. %1%"
			}
		}
		else if (!outputTxt) outputTxt = "You can not change your mode to '${param}' because you do not have this mode selected within your SmartApp. Please enable this mode for control. %1%" 
    }
    if (cmd==~/security|smart home|smart home monitor|SHM/){
    	def SHMstatus = location.currentState("alarmSystemStatus")?.value
		def SHMFullStat = [off : "disarmed", away: "armed away", stay: "armed stay"][SHMstatus] ?: SHMstatus
        def newSHM = "", SHMNewStat = "" 
        if (param=="undefined") outputTxt ="The Smart Home Monitor is currently set to, '${SHMFullStat}'. "
        if (listSHM && param != "undefined"){
            if (shmPW && pwNeeded && password && num == "undefined") outputTxt = "You must say your password to change the Smart Home Monitor. %1%"
            if (shmPW && pwNeeded && password && num!="undefined" && num != password) outputTxt="I did not hear the correct password to change the Smart Home Monitor. %1%"
            if (!shmPW || !pwNeeded || (shmPW && pwNeeded && num == password)){
                if (param==~/arm|armed/ && (listSHM.find{it =="Arm (Away)"} || listSHM.find{it =="Arm (Stay)"})) outputTxt ="I did not understand how you want me to arm the Smart Home Monitor. Be sure to say, 'armed stay' or 'armed away', to properly change the setting. %1%"   
                if ((param ==~/off|disarm/) && listSHM.find{it =="Disarm" }) newSHM="off"
                if ((param ==~/away|armed away|arm away/) && listSHM.find{it =="Arm (Away)"}) newSHM="away"
                if ((param ==~/stay|armed stay|arm stay/) && listSHM.find{it =="Arm (Stay)"}) newSHM="stay"
                if (newSHM && SHMstatus!=newSHM) {
                    sendLocationEvent(name: "alarmSystemStatus", value: newSHM)
                    SHMNewStat = [off : "disarmed", away: "armed away", stay: "armed stay"][newSHM] ?: newSHM
                    outputTxt ="I am setting the Smart Home monitor to, '${SHMNewStat}'. "
                }
            else if (SHMstatus==newSHM) outputTxt ="The Smart Home Monitor is already set to '${SHMFullStat}'. No changes are being made. " 
       		}
        }
        if (!outputTxt) outputTxt = "I was unable to change your Smart Home Monitor. Ensure you have the proper settings enabled within your SmartApp. %1%"
    }
    if (cmd=="routine" && listRoutines){
        if (param != "undefined") {
        	def runRoutine = listRoutines.find{it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()==param}
        	if (runRoutine) {
            	if (routinesPW && pwNeeded && password && num == "undefined") outputTxt = "You must say your password to run SmartThings routines. %1%"
            	if (routinesPW && pwNeeded && password && num!="undefined" && num != password) outputTxt="I did not hear the correct password to run the SmartThings routines. %1%"
            	if (!routinesPW || !pwNeeded || (routinesPW && pwNeeded && num == password)){
        			location.helloHome?.execute(runRoutine)
            		outputTxt="I am executing the '${param}' routine. "
                }
        	}
        	else outputTxt = "You can not run the SmartThings routine named, '${param}', because you do not have this routine enabled in the Ask Alexa SmartApp. %1%"
        }
        else outputTxt ="To run SmartThings routines, ask me to run the routine by its full name. For a list of available routines, simply say, 'ask ${invocationName} to list routines'. %1%"       
    }
    if (!outputTxt) outputTxt = "I didn't understand what you wanted me to do. %1%" 
    if (outputTxt && !outputTxt.endsWith("%") && !outputTxt.endsWith(" ")) outputTxt += " "
    if (outputTxt && !outputTxt.endsWith("%")) outputTxt +="%2%"
    sendJSON(outputTxt)
}
def getReply(devices, type, STdeviceName, op, num, param){
	String result = "", batteryWarnTxt=""
    log.debug "Type: " + type
	try {
    	def STdevice = devices
        def supportedCaps = STdevice.capabilities
        if (op=="status") {
            if (type == "temperature"){
                def temp = roundValue(STdevice.currentValue(type))
                result = "The temperature of the ${STdeviceName} is ${temp} degrees"
                if (otherStatus) {
                    def humidity = STdevice.currentValue("humidity"), wet=STdevice.currentValue("water")
                    result += humidity ? ", and the relative humidity is ${humidity}%. " : ". "
                    result += wet ? "Also, this device is a leak sensor, and it is currently ${wet}. " : ""
                }
                else result += ". "
            }
            else if (type == "presence") result = "The presence sensor, ${STdeviceName}, is showing ${STdevice.currentValue(type)}. "
            else if (type ==~/acceleration|motion/){
            	def currVal =STdevice.currentValue(type), motionStat=[active : "movement", inactive: "no movement"][currVal] ?: currVal
            	result = "The ${type} sensor, ${STdeviceName}, is currently reading '${motionStat}'. "
            }
            else if (type == "humidity"){
                result = "The relative humidity at the ${STdeviceName} is ${STdevice.currentValue(type)}%"
                if (otherStatus) {
                    def temp =roundValue(STdevice.currentValue("temperature"))
                    result += temp ? ", and the temperature is ${temp} degrees." : ". "
				}
                else result += ". "
            }
            else if (type ==~ /level|color|switch/) {
                def onOffStatus = STdevice.currentValue("switch")
                result = "The ${STdeviceName} is ${onOffStatus}"
                if (otherStatus) { 
                	def level = STdevice.currentValue("level"), power = STdevice.currentValue("power")
                    result += onOffStatus == "on" && level ? ", and it's set to ${level}%" : ""
                    result += onOffStatus=="on" && power > 0 ? ", and is currently drawing ${power} watts of power. " : ". "
            	}
                else result += ". "
            }
            else if (type == "thermostat"){
                def temp = roundValue(STdevice.currentValue("temperature"))
                result = "The ${STdeviceName} temperature reading is currently ${temp} degrees"
                if (otherStatus){
                    def humidity = STdevice.currentValue("humidity"), opState = STdevice.currentValue("thermostatMode")
                    def heat = opState=="heat" || opState =="auto" || stelproCMD ? STdevice.currentValue("heatingSetpoint") : ""
                    if (heat) heat = roundValue(heat)
                    def cool = opState=="cool" || opState =="auto" ?  STdevice.currentValue("coolingSetpoint") : "" 
                    if (cool) cool = roundValue(cool)
                    result += opState ? ", and the thermostat's mode is: '${opState}'. " : ". "
                    result += humidity ? " The relative humidity reading is ${humidity}%. " : ""
                    if (nestCMD && supportedCaps.name.contains("Presence Sensor")){
                    	result += " This thermostat's presence sensor is reading "
                        result += STdevice.currentValue("presence")=="present" ? "'Home'. " : "'Away'. "
                    }
                    if ((ecobeeCMD && !MyEcobeeCMD) && STdevice.currentValue('currentProgramId') =~ /home|away|sleep/ ) result += " This thermostat's comfort setting is set to ${STdevice.currentValue('currentProgramId')}. "
                    if (MyEcobeeCMD && STdevice.currentValue('setClimate')) {
                    	def climatename = STdevice.currentValue('setClimate'), climateList = STdevice.currentValue('climateList')
                        if (climateList.contains(climatename)) result += " This thermostat's comfort setting is set to ${climatename}. "
                    }
                    result += heat ? " The heating setpoint is set to ${heat} degrees. " : ""
                    result += heat && cool ? "And finally, " : ""
                    result += cool ? " The cooling setpoint is set to ${cool} degrees. " : ""
            	}
                else result += ". "
            }
            else if (type == "contact") result = "The ${STdeviceName} is currently ${STdevice.currentValue(type)}. "
            else if (type == "music"){
                def onOffStatus = STdevice.currentValue("status"), track = STdevice.currentValue("trackDescription"), level = STdevice.currentValue("level"), mute = STdevice.currentValue("mute")
                result = "The ${STdeviceName} is currently ${onOffStatus}"
                result += onOffStatus =="stopped" ? ". " : onOffStatus=="playing" && track ? ": '${track}'" : ""
                result += onOffStatus == "playing" && level && mute =="unmuted" ? ", and it's volume is set to ${level}%. " : mute =="muted" ? ", and it's currently muted. " :""
            }
            else if (type == "water") result = "The water sensor, '${STdeviceName}', is currently ${STdevice.currentValue(type)}. "
            else if (type == "shade") result = "The window shade, '${STdeviceName}', is currently " + STdevice.currentValue('windowShade') +". "
            else result = "The ${STdeviceName} is currently ${STdevice.currentValue(type)}. "
        }
        else {
            if (type == "thermostat"){
                if (param =~/tip/ || op=~/tip/) {
                	if (MyEcobeeCMD && ecobeeCMD) {
                        if (op ==~/repeat|replay/) {
                            def currentTipNum= state.tipCount>0 ? state.tipCount : 1 
                            def previousTipNum= currentTipNum - 1                    
                            def tipNum = previousTipNum>0 && previousTipNum <6 ? (previousTipNum) as int : 1
                            def attribute="tip${tipNum}Text"
                            if (STdevice.currentValue(attribute)) result = "My Ecobee's current tip number ${tipNum} is '" + STdevice.currentValue(attribute) + "'. "
                            else result ="Tip number is unavailable at this time for the ${STdeviceName}. Try erasing the tips, then issue the 'GET TIPS' command and try again. %1%"
                        }
                        else if (op==~/play|give/ || (op=="tip" && param=="undefined")) { 
                            def tipNum= state.tipCount>0 ? state.tipCount : 1                    
                            def attribute="tip${tipNum}Text"
                            if (!(STdevice.currentValue(attribute))) {
                                STdevice.getTips(1)
                                state.tipCount=1                       
                                tipNum= 1                           
                                attribute="tip${tipNum}Text"
                            }                            
                            if (STdevice.currentValue(attribute)) { 
                                result = "My Ecobee's current tip number ${tipNum} is '" + STdevice.currentValue(attribute) + "'. "
                                state.tipCount=state.tipCount+1
                            }
                            else result ="Tip number is unavailable at this time for the ${STdeviceName}. Try erasing the tips, then issue the 'GET TIPS' command and try again. %1%"
                        }
                        else if (op ==~/get|load|reload/) { 
                            def tipLevel = num>0 && num<5 ? num :1
                            result = "I am loading level ${tipLevel} tips to ${STdeviceName}. "
                            STdevice.getTips(tipLevel)
                            state.tipCount=1
                        } 
                        else if (op ==~/restart|erase|delete|clear|reset/) { 
                            result = "I am resetting the tips from ${STdeviceName}. Be sure to ask for the 'GET TIP' command to reload your thermostat advice. "
                            STdevice.resetTips()
                            state.tipCount=1
                        }
                        else result = "I did not understand what you wanted me to do with the Ecobee tips. Valid commands are 'get', 'give', 'repete' or 'erase' tips. %1%"
					}
                    else result = "You do not have the Ecobee tips functionality enabled in your SmartApp. %1%"
                }
                else {                   
                    if (param == "undefined") param = tstatCool ? "cool" : tstatHeat ? "heat" : param
                    if ((op ==~/increase|raise|up|decrease|down|lower/)){
                         def newValues = upDown(STdevice, type, op, num, STdeviceName)  
                         num = newValues.newLevel
                    }
                    if (num>0) {
                        if (tstatHighLimit) num = num <= tstatHighLimit ? num : tstatHighLimit 
                        if (tstatLowLimit) num = num >= tstatLowLimit ? num : tstatLowLimit
                    }
                    if (op =="maximum" && tstatHighLimit) num = tstatHighLimit
                    if (op =="minimum" && tstatLowLimit) num = tstatLowLimit
                    def ecobeeCustomRegEx = MyEcobeeCMD && ecobeeCMD ? getEcobeeCustomRegEx(STdevice) : null
                    if ((param==~/heat|heating|cool|cooling|auto|automatic|eco|AC|comfort|home|away|sleep|resume program/ || (ecobeeCustomRegEx && param =~ /${ecobeeCustomRegEx}/)) && num == 0 && op=="undefined") op="on" 
                    if (op ==~/on|off/) {
                        if (param == "undefined" && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning the ${STdeviceName} on. "
                        if (param =~/heat/) {result="I am setting the ${STdeviceName} to 'heating' mode. "; STdevice.heat()}
                        if (param =~/cool|AC/) {result="I am setting the ${STdeviceName} to 'cooling' mode. "; STdevice.cool()}
                        if (param =~/auto/) {result="I am setting the ${STdeviceName} to 'auto' mode. Please note, to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints separately. " ; STdevice.auto()}
                        if (param =="home" && nestCMD) {result = "I am setting the ${STdeviceName} to 'home'. "; STdevice.present()} 
                        if (param =="away" && nestCMD) {result = "I am setting the ${STdeviceName} to 'away'. Please note that Nest thermostats will not respond to temperature changes while in 'away' status. "; STdevice.away()} 
                        if ((param ==~/home|away|sleep/ || (ecobeeCustomRegEx && param =~ /${ecobeeCustomRegEx}/)) && ecobeeCMD) {
                            result = "I am setting the ${STdeviceName} to '" + param + "'. "
                            if (STdevice.hasCommand("setThermostatProgram")) STdevice.setThermostatProgram("${param.capitalize()}")
                            else if (STdevice.hasCommand("setClimate")) STdevice.setThisTstatClimate("${param.capitalize()}") 
                            else result ="There was an error setting your climate. %1%"
                        }
                        if (param =="resume program" && ecobeeCMD && !MyEcobeeCMD) {result = "I am resuming the climate program of the ${STdeviceName}. "; STdevice.resumeProgram()}  
                        if (param =="resume program" && MyEcobeeCMD) {result = "I am resuming the climate program of the ${STdeviceName}. "; STdevice.resumeThisTstat()}  
                        if (op =="off") { result = "I am turning the ${STdeviceName} ${op}. "; STdevice.off() }
                        if (stelproCMD && param==~/eco|comfort/) { result="I am setting the ${STdeviceName} to '${param}' mode. "; STdevice.setThermostatMode("${param}") } 
                    }
                    else if (op=~"report") {
                    	if (nestCMD && nestMGRCMD) { STdevice.updateNestReportData();  result = STdevice.currentValue("nestReportData").toString() }
                        if (!nestCMD || (nestCMD && !nestMGRCMD) ) result ="The 'report' command is reserved for Nest Thermostats using the Nest Manager SmartApp. " +
                        	"You do not have the options enabled to use this command. Please check your settings within your smartapp. %1%"
                        if (result=="null") result = "The Nest Manager returned no results for the ${STdeviceName}. Please check your settings within your smartapp. %1%"
                    }
                    else {
                        if (param == "undefined"){ 
                            if (STdevice.currentValue("thermostatMode")=="heat") param = "heat"
                            else if (STdevice.currentValue("thermostatMode")=="cool") param = "cool"
                            else result = "You must designate a 'heating' or 'cooling' parameter when setting the temperature. The thermostat will not accept a generic setpoint in its current mode. "+
                            	"For example, you could simply say, 'ask ${invocationName} to set the ${STdeviceName} heating to 65 degrees'. %1%"	
                        }
                        if ((op =="maximum" && !tstatHighLimit) || (op =="minimum" && !tstatLowLimit)) {
                            result = "You do not have a ${op} thermostat setpoint defined within your SmartApp. %1%"
                            param = "undefined"
                        }
                        if ((param =~/heat/) && num > 0) {
                            result="I am setting the heating setpoint of the ${STdeviceName} to ${num} degrees. "
                            STdevice.setHeatingSetpoint(num) 
                            if (stelproCMD) STdevice.applyNow()
                        }
                        if ((param =~/cool|AC/) && num > 0) {
                            result="I am setting the cooling setpoint of the ${STdeviceName} to ${num} degrees. "
                            STdevice.setCoolingSetpoint(num)
                        }
                        if (param != "undefined" && tstatHighLimit && num >= tstatHighLimit) result += "This is the maximum temperature I can set for this device. "
                        if (param != "undefined" && tstatLowLimit && num <= tstatLowLimit) result += "This is the minimum temperature I can set for this device. "
                    }
            	}
            }
            if (type ==~ /color|level|switch/){
                num = num < 0 ? 0 : num >99 ? 100 : num
                def overRideMsg = "" 
                if (op == "maximum") num = 100
                if ((op ==~/increase|raise|up|decrease|down|lower|brighten|dim/) && (type == "color" || type == "level")){ 
                     def newValues = upDown(STdevice, type, op, num, STdeviceName)
                     num = newValues.newLevel
                     op= num > 0 ? "on" : "off"
                     overRideMsg = newValues.msg
                }
                if (op ==~/low|medium|high/ && type ==~ /color|level/){
                	if (op=="low" && dimmerLow) num = dimmerLow else if (op=="low" && dimmerLow=="") num =0 
                    if (op=="medium" && dimmerMed) num = dimmerMed else if (op=="medium" && !dimmerMed) num = 0 
                    if (op=="high" && dimmerHigh) num = dimmerHigh else if (op=="high" && !dimmerhigh) num = 0 
                    if (num>0) overRideMsg = "I am turning the ${STdeviceName} to ${op}, or a value of ${num}%. "
                    if (num==0) overRideMsg = "You don't have a default value set up for the '${op}' level. I am not making any changes to the ${STdeviceName}. %1%"
                }
                if ((type == "switch") || (type ==~ /color|level/ && num==0 )){
                    if (type ==~ /color|level/ && num==0 && op=="undefined" && param=="undefined") op="off"
                	if (op==~/on|off/){
                		STdevice."$op"() 
                        result = overRideMsg ? overRideMsg: "I am turning the ${STdeviceName} ${op}. "
                    }
                    if (op=="toggle") {
        				def oldstate = STdevice.currentValue("switch"), newstate = oldstate == "off" ? "on" : "off"
        				STdevice."$newstate"()
                        result = "I am toggling the ${STdeviceName} from '${oldstate}' to '${newstate}'. "
                    }
            	}
                if (type ==~ /color|level/ && num > 0) {
                	STdevice.setLevel(num)
                    result = overRideMsg ? overRideMsg : num==100 ? "I am setting the ${STdeviceName} to its maximum value. " : "I am setting the ${STdeviceName} to ${num}%. "                    
				}
                if (type == "color" && param !="undefined" && supportedCaps.name.contains("Color Control")){
                    def getColorData = fillColorSettings().find {it.name.toLowerCase()==param}
                    if (getColorData){
                        def hueColor = getColorData.hue, satLevel = getColorData.sat
                        def newLevel = num > 0 ? num : STdevice.currentValue("level")
                        def newValue = [hue: hueColor as int, saturation: satLevel as int, level: newLevel]
                        STdevice?.setColor(newValue)
                        result = "I am setting the color of the ${STdeviceName} to ${param}"
                        result += num>0 ? ", at a brightness level of ${num}%. " : ". "
                	}
                }
            	if (!result){
                	if (type=="switch") result = "For the ${STdeviceName} switch, be sure to give an 'on', 'off' or 'toggle' command. %1%"
            		if (type=="level") result = overRideMsg ? overRideMsg: "For the ${STdeviceName} dimmer, be sure to use an 'on', 'off', 'toggle' command or brightness level setting. %1%"
            		if (type=="color") result = overRideMsg ? overRideMsg: "For the ${STdeviceName} color controller, remember it can be operated like a switch. You can ask me to turn it on, off, toggle "+  
                    "the on and off states, or set a brightness level. You can also set it to a variety of common colors. For listing of these colors, simply say, 'tell SmartThings to list the colors'. %1%"
                }
            }
            if (type == "music"){             
                if ((op ==~/increase|raise|up|decrease|down|lower/)){
                     def newValues = upDown(STdevice, type, op, num,STdeviceName) 
                     num = newValues.newLevel
                     if (num==0) op= "off"
                }
                if ((num != 0 && speakerHighLimit && num > speakerHighLimit)|| (op=="maximum" && speakerHighLimit)) num = speakerHighLimit    
                if (op==~/off|stop/) { STdevice.stop(); result = "I am turning off the ${STdeviceName}. " }
                else if (op ==~/play|on/ && param=="undefined") { 
                	STdevice.play()
                    result = "I am playing the ${STdeviceName}. " 
                }
                else if (op ==~/play|on|undefined/  && param!="undefined") { 
                	if (sonosCMD && sonosMemoryCount){
                        def memCount = sonosMemoryCount as int, song = ""
        				for (int i=1; i<memCount+1; i++){ 
                        	 if (settings."sonosSlot${i}Name" && settings."sonosSlot${i}Music" && settings."sonosSlot${i}Name".replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() == param.toLowerCase()) {
                             	song = settings."sonosSlot${i}Music"
                                break
                             }
						}
                        def playSong = state.songLoc.find{it.station==song}
                        if (playSong){
							STdevice.playTrack(playSong)
							result = "I am playing '${song}' on the ${STdeviceName}. "
                        }
                        else result = "I could not play the song you requested. Check your Sonos memory slots in the SmartApp. %1%"
                    }
                    else result = "You do not have the Sonos memory slots enabled in your SmartApp. %1%"
                }
                else if (op==~/mute|unmute|pause/) {STdevice."$op"(); result = "I am ${op[0..-2]}ing the ${STdeviceName}. " }
                else if (op=="next track") {  STdevice.nextTrack(); result = "I am playing the next track on the ${STdeviceName}. " }
            	else if (op=="previous track") { STdevice.previousTrack(); result = "I am playing the next track on the ${STdeviceName}. " }
                else result = "I didn't understand what you wanted me to do with the ${STdeviceName}. %1%"
                if (num > 0) { STdevice.setLevel(num); result = "I am setting the volume of the ${STdeviceName} to ${num}%. " }
                if (speakerHighLimit && num == speakerHighLimit) result += "This is the maximum volume level you have set up. "
                if (op=="maximum" && !speakerHighLimit) result = "You have not set a maximum volume level in the SmartApp. %1%"
            }
			if (type == "door"){              
                def currentDoorState = STdevice.currentValue(type)
				if (currentDoorState==op || (currentDoorState == "closed" && op=="close")) result = "The ${STdeviceName} is already ${currentDoorState}. "
                else {
                    if (op != "open" || op != "close") result ="For the ${STdeviceName}, you must give an 'open' or 'close' command. %1%"
                    if ((op==~/open|close/) && (doorPW && pwNeeded && password && num == 0)) result="You must say your password to ${op} the ${STdeviceName}. %1%"
                    if ((op==~/open|close/) && (doorPW && pwNeeded && password && num>0 && num != password as int)) result="I did not hear the correct password to ${op} the ${STdeviceName}. %1%"
                    else if ((op==~/open|close/) && (!doorPW || !pwNeeded || (password && pwNeeded && num == password as int) || !password)) {
                        STdevice."$op"() 
                        result = op=="close" ? "I am closing the ${STdeviceName}. " : "I am opening the ${STdeviceName}. "
                    }
             	}
			}
            if (type == "shade"){
                def currentShadeState = STdevice.currentValue("windowShade")
                if (currentShadeState==op || (currentShadeState == "closed" && op=="close")) result = "The ${STdeviceName} is already ${currentShadeState}. "
                else {
                    if (op != "open" && op != "close") result ="For the ${STdeviceName}, you must give an 'open' or 'close' command. %1%"
                    else { 
                        STdevice."$op"() 
                    	result = op=="close" ? "I am closing the ${STdeviceName}. " : "I am opening the ${STdeviceName}. "
                	}
                }
			}
            if (type == "lock"){
                if (STdevice.currentValue("lock") == op+"ed") result = "The ${STdeviceName} is already ${op}ed. "
                else {
                    if (op != "lock" || op != "unlock" ) result= "For the ${STdeviceName}, you must give a 'lock' or 'unlock' command. %1%"
                    if ((op==~/lock|unlock/) && (lockPW && pwNeeded && password && num ==0)) result= "You must say your password to ${op} the ${STdeviceName}. %1%"
                    if ((op==~/lock|unlock/) && (lockPW && pwNeeded && password && num>0 && num != password as int)) result="I did not hear the correct password to ${op} the ${STdeviceName}. %1%"
                    else if ((op==~/lock|unlock/) && (!lockPW || !pwNeeded || (password && pwNeeded && num ==password as int) || !password)) {
                        STdevice."$op"()
                        result = "I am ${op}ing the ${STdeviceName}. "
                    }
                }
        	}
		}
        if (otherStatus && op=="status"){
            def temp = STdevice.currentValue("temperature"), accel=STdevice.currentValue("acceleration"), motion=STdevice.currentValue("motion"), lux =STdevice.currentValue("illuminance") 
            result += lux ? "The illuminance at this device's location is ${lux} lux. " : ""
            result += temp && type != "thermostat" && type != "humidity" && type != "temperature" ? "In addition, the temperature reading from this device is ${roundValue(temp)} degrees. " : ""
			result += motion == "active" && type != "motion" ? "This device is also a motion sensor, and it is currently reading movement. " : ""
 			result += accel == "active" ? "This device has a vibration sensor, and it is currently reading movement. " : ""
        }
        if (supportedCaps.name.contains("Battery") && batteryWarn){
			def battery = STdevice.currentValue("battery"), battThresLevel = batteryThres as int
			if (battery && battery < battThresLevel) batteryWarnTxt += "Please note, the battery in this device is at ${battery}%. "
            else if (battery !=0 && battery == null) batteryWarnTxt += "Please note, the battery in this device is reading null, which may indicate an issue with the device. "
            else if (battery < 1) batteryWarnTxt += "Please note, the battery in this device is reading ${battery}%; time to change the battery. "
		}
        if (op !="status" && !result && type==~/motion|presence|humidity|water|contact|temperature/) result = "You attempted to take action on a device that can only give a status reading. %1%"
	}
	catch (e){ result = "I could not process your request for the '${STdeviceName}'. Ensure you are using the correct commands with the device. %1%" }
    if (op=="status" && result && !result.endsWith("%")) result += batteryWarnTxt + "%2%"
    else if (op!="status" && result && !result.endsWith("%")) result += batteryWarnTxt + "%3%"
    if (result.endsWith("%3%") && briefReply) {
    	def reply = briefReplyTxt && briefReplyTxt !="No reply spoken" ? briefReplyTxt : ""
        result = reply ? reply + ". " + batteryWarnTxt +"%3%" : batteryWarnTxt + " %3%"
    }
    if (!result) result = "I had a problem understanding your request. %1%"
    return result
}
def displayData(display){
	render contentType: "text/html", data: """<!DOCTYPE html><html><head><meta charset="UTF-8" /><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/></head><body style="margin: 0;">${display}</body></html>"""
}
//Child code pieces here---Macro Handler-------------------------------------
def macroResults(num, cmd, colorData, param,mNum){ 
    String result="", feedData=""
    if (macroType == "Voice") result = reportResults()      
    if (macroType == "Control") result = controlResults(num)
    if (macroType == "Group" && cmd!="status") result = groupResults(num, cmd, colorData, param, mNum)
    else if (macroType == "Group" && cmd=="status") result = "You can not get the status of items in a device group. It is recommended you use a voice report for this functionality. %1%"
	if (macroType == "CoRE") result = CoREResults(num)
    else if (macroType ==~/Voice|Group/) {
    	def data = [alexaOutput: result, num: num, cmd: cmd, color: colorData, param:param]
        sendLocationEvent(name: "askAlexaMacro", value: app.label, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa activated '${app.label}' macro.")
	}
    if (noteFeed && noteFeedData) feedData=result.endsWith("%") ? ' Data sent to Alexa: "' + result.take(result.length()-3) + '"' : ' Data sent to Alexa: "' + result + '"'
    if (noteFeed) sendNotificationEvent("Ask Alexa triggered macro: '${app.label}'. ${feedData}")
    return result
}
//Group Handler
def groupResults(num, op, colorData, param, mNum){   
    def grpType = [switch:"switch", switchLevel:"dimmer", colorControl:"colored light", windowShade:"window shade", doorControl: "door"][groupType]?:groupType
    String result = ""
    try {
        def noun= settings."groupDevice${groupType}".size()==1 ? grpType : grpType+"s"
        if (grpType=="switch" && settings."groupDevice${groupType}".size()>1) noun = "switches"
        def verb=settings."groupDevice${groupType}".size()==1 ? "is" : "are"
        String valueWord, proNoun = settings."groupDevice${groupType}".size()==1 ? "its" : "their"
        if (op == "list") result = "The following ${noun} ${verb} in the '${app.label}' device group: "+ getList(settings."groupDevice${groupType}") +". "
        else if (groupType=="switch"){
            if (op ==~/on|off/) { settings."groupDevice${groupType}"?."$op"();result = voicePost && !noAck ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am turning ${op} the ${noun} in the group named '${app.label}'. " }
            else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost && !noAck? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " } 
            else result = "For a switch group, be sure to give an 'on', 'off' or 'toggle' command. %1%"
        }
        else if (groupType==~/switchLevel|colorControl/){
            num = num < 0 ? 0 : num >99 ? 100 : num
            if (op == "maximum") { num = 100; op ="undefined"; valueWord= "${proNoun} maximum brightness" }
            else if (op==~/low|medium|high/ && groupType=="switchLevel") { valueWord="${op}, or a value of ${num}%"; op ="undefined" }
            else if (op==~/low|medium|high/ && groupType=="colorControl" && !colorData ) { valueWord="${op}, or a value of ${num}%"; op ="undefined" }
            else valueWord = "${num}%"
            if (num==0 && op=="undefined" && param=="undefined" && mNum!="undefined") op="off"
            if (op ==~/on|off/){ settings."groupDevice${groupType}"?."$op"();result = voicePost ? replaceVoiceVar(voicePost,"") : noAck ? " " :  "I am turning ${op} the ${noun} in the group named '${app.label}'. "}
            else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " }
            else if (groupType=="switchLevel" && num > 0 && op =="undefined") { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am setting the ${noun} in the group named '${app.label}' to ${valueWord}. " }
            else if (groupType=="colorControl" && num > 0 && !colorData && op =="undefined") { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost && !noAck  ? replaceVoiceVar(voicePost,"") :  noAck ? " " :"I am setting the ${noun} in the '${app.label}' group to ${valueWord}. " }
            else if (groupType=="colorControl" && colorData && param) {
                if (num>0) colorData = [hue:colorData.hue, saturation: colorData.saturation, level: num]
                settings."groupDevice${groupType}"?.setColor(colorData)
                if (!voicePost && !noAck){
                    result ="I am setting the ${noun} in the '${app.label}' group to ${param}"
                    result += num ==100 ? " and ${proNoun} maximum brightness" : num>0 ? ", at a brightness level of ${num}%" : ""
                    result += ". "
                }
                else if (voicePost && !noAck)  result = replaceVoiceVar(voicePost,"") 
                else result = " "
            }
            else if (op ==~/increase|raise|up|brighten|decrease|down|lower|dim/){
                if (parent.lightAmt){
                    settings."groupDevice${groupType}".each{ upDownChild(it, op, num) }
                    def count = 0
                    if (op ==~/increase|raise|up|brighten/) {
                        result = "I have raised the brightness of the ${noun} in the group named '${app.label}'"
                        result += num>0 ? " by ${num}%. " : ". "
                        settings."groupDevice${groupType}".each { if (it.currentValue("level")>98) count ++ }
                        if (count == settings."groupDevice${groupType}".size()) result = "The ${noun} in the group '${app.label}' ${verb} at maximum brightness. "
                        if (count > 0 && count < settings."groupDevice${groupType}".size()) result += "Some of the ${noun} ${verb} at maximum brightness. "
                    }
                    if (op ==~/decrease|down|lower|dim/){
                        result = "I have decreased the brightness of the ${noun} in the group named '${app.label}'"
                        result += num>0 ? " by ${num}%. " : ". "
                        settings."groupDevice${groupType}".each { if (it.currentValue("switch")=="off") count ++ }
                        if (count == settings."groupDevice${groupType}".size()) result = "The ${noun} in the group '${app.label}' ${verb} off. "
                        if (count > 0 && count < settings."groupDevice${groupType}".size()) result += "Some of the ${noun} ${verb} now off. "
                    }
                }
                else result = "The default increase or decrease value is set to zero within the SmartApp. I am taking no action. %1%"
            }
            else if (groupType=="switchLevel") result = "For a dimmer group, be sure to use an 'on', 'off', 'toggle' or brightness level setting. %1%" 
            else if (groupType=="colorControl") result = "For a colored light group, be sure to give me an 'on', 'off', 'toggle', brightness level or color command. %1%" 
        }
        else if (groupType=="lock"){
            noun=settings."groupDevice${groupType}".size()==1 ? "device" : "devices"
            if (op ==~ /lock|unlock/){         
                settings."groupDevice${groupType}"?."$op"()
                result = voicePost && !noAck ? replaceVoiceVar(voicePost,"") : noAck ? " " : "I am ${op}ing the ${noun} in the group named '${app.label}'. " 
            }
            else result = "For a lock group, you must use a 'lock' or 'unlock' command. %1%" 
        }
        else if (groupType==~/doorControl|windowShade/){
            if (op ==~ /open|close/){
                settings."groupDevice${groupType}"?."$op"()
                def condition = op=="close" ? "closing" : "opening"
                result = voicePost && !noAck  ? replaceVoiceVar(voicePost,"") : noAck ? " " :  "I am ${condition} the ${noun} in the group named '${app.label}'. "
            }
            else result = "For a ${grpType} group, you must use an 'open' or 'close' command. %1%"
        }
        else if (groupType=="thermostat"){
            noun=settings."groupDevice${groupType}".size()==1 ? "thermostat in the group named '${app.label}'" : "thermostats in the group named '${app.label}'"
            if (num>0) {
                if (parent.getTstatLimits().hi) num = num <= parent.getTstatLimits().hi ? num : parent.getTstatLimits().hi
                if (parent.getTstatLimits().lo) num = num >= parent.getTstatLimits().lo ? num : parent.getTstatLimits().lo
            }
            if (op =="maximum" && parent.getTstatLimits().hi) num = parent.getTstatLimits().hi
            if (op =="minimum" && parent.getTstatLimits().lo) num = parent.getTstatLimits().lo
            def ecobeeCustomRegEx = parent.MyEcobeeCMD && parent.ecobeeCMD ? getEcobeeCustomRegEx(settings."groupDevice${groupType}") : null
            if ((param==~/heat|heating|cool|cooling|auto|automatic|eco|AC|comfort|home|away|sleep|resume program/ || (ecobeeCustomRegEx && param =~ /${ecobeeCustomRegEx}/)) && num == 0 && op=="undefined") op="on"
            if (op ==~/on|off/) {
                if (param == "undefined" && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning on a thermostat group. %1%"
                if (param =~/heat/) {result="I am setting the ${noun} to 'heating' mode. "; settings."groupDevice${groupType}"?.heat()}
                if (param =~/cool|AC/) {result="I am setting the ${noun} to 'cooling' mode. "; settings."groupDevice${groupType}"?.cool()}
                if (param =~/auto/) {result="I am setting the ${noun} to 'auto' mode. Please note, to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints separately. " ; settings."groupDevice${groupType}"?.auto()}
                if (op == "off") { result = "I am turning off the ${noun}. "; settings."groupDevice${groupType}"?.off() }
                if (parent.stelproCMD && (param=="eco" || param=="comfort")){ result="I am setting the ${noun} to '${param}' mode. "; settings."groupDevice${groupType}"?.setThermostatMode("${param}") }
                if (param=="home" && parent.nestCMD) { result="I am setting the ${noun} to 'home' mode. "; settings."groupDevice${groupType}"?.present() }
                if (param=="away" && parent.nestCMD) { 
                    result="I am setting the ${noun} to 'away' mode. Please note that Nest thermostats will not accept temperature changes while in 'away' status. "
                    settings."groupDevice${groupType}"?.away()
                }
                if ((param ==~/home|away|sleep/ || (ecobeeCustomRegEx && param =~ /${ecobeeCustomRegEx}/)) && parent.ecobeeCMD){
                    result = "I am setting the ${noun} to '" + param + "'. "
                    settings."groupDevice${groupType}".each {myDevice ->
                        myDevice.supportedCommands.each {comm ->
							if(comm.name == "setThermostatProgram") myDevice.setThermostatProgram("${param.capitalize()}")
							else if(comm.name == "setClimate") myDevice.setThisTstatClimate("${param.capitalize()}")
                        }  
                        myDevice.supportedCommands.each {comm ->
                            if(comm.name == "setThermostatProgram") myDevice.setThermostatProgram("${param.capitalize()}")
                            else if(comm.name == "setClimate") myDevice.setClimate("","${param.capitalize()}")
                        }
                    }
                }
                if ((param=="resume program") && parent.ecobeeCMD && !parent.MyEcobeeCMD) {   
                    result="I am resuming the climate program of the ${noun}."
                    settings."groupDevice${groupType}"?.resumeProgram()
                }
                if ((param=="resume program") && parent.MyEcobeeCMD) {  
					result="I am resuming the climate program of the ${noun}." 
					settings."groupDevice${groupType}"?.resumeThisTstat() 
				} 
            }
            else if (op ==~/increase|raise|up|decrease|down|lower/) result = "Increase and decrease commands are not yet compatible with thermostat group macros. %1%" //need to add this soon
            else {
                param = tstatDefaultCool && param == "undefined" ? "cool" : tstatDefaultHeat && param == "undefined" ? "heat" : param
                if (param == "undefined") result = "You must designate a 'heating' or 'cooling' parameter when setting the temperature of a thermostat group. %1%"
                if ((op =="maximum" && !parent.getTstatLimits().hi) || (op =="minimum" && !parent.getTstatLimits().lo)) {
                    result = "You do not have a ${op} thermostat setpoint defined within your SmartApp. %1%"
                    param = "undefined"
                }
                if (param =~/heat/ && num > 0) {
                    result="I am setting the heating setpoint of the ${noun} to ${num} degrees. "
                    settings."groupDevice${groupType}"?.setHeatingSetpoint(num) 
                    if (parent.stelproCMD) settings."groupDevice${groupType}"?.applyNow()
                }
                if (param =~/cool|AC/ && num>0) {
                    result="I am setting the cooling setpoint of the ${noun} to ${num} degrees. "
                    settings."groupDevice${groupType}"?.setCoolingSetpoint(num)
                }
                if (param !="undefined" && parent.getTstatLimits().hi && num >= parent.getTstatLimits().hi) result += "This is the maximum temperature I can set for this device group. "
                if (param !="undefined" && parent.getTstatLimits().lo && num <= parent.getTstatLimits().lo) result += "This is the minimum temperature I can set for this device group. "
            }
        }
        result = voicePost && !noAck ? replaceVoiceVar(voicePost,"") : noAck ? " " : result
	}
    catch(e) { result = "There was a problem controlling the device group named '${app.label}'. Be sure it is configured correctly within the SmartApp. %1%" }
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
	else result = "The CORE macro, '${app.label}', is already scheduled to run. You must cancel the execution or wait until it runs before you can run it again. %1%"
	return result
}
def CoREHandler(){ 
	state.scheduled = false
    def data = [pistonName: CoREName, args: "I am activating the CoRE Macro: '${app.label}'."]
    sendLocationEvent (name: "CoRE", value: "execute", data: data, isStateChange: true, descriptionText: "Ask Alexa triggered '${CoREName}' piston.")
	if (noteFeedAct && noteFeed) sendNotificationEvent("Ask Alexa activated CoRE macro: '${app.label}'.")
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
            def data = [args: "I am cancelling the timer for Control Macro: '${app.label}'."]
    		sendLocationEvent(name: "askAlexaMacro", value: app.label, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa cancelled '${app.label}' macro timer.")	
        	unschedule()
        }
		if (!state.scheduled) {
        	if (!delay || delay == 0) controlHandler() 
            else if (delay < 9999) {
            	def data = [args: "I am starting the timer for Control Macro: '${app.label}'."]
    			sendLocationEvent(name: "askAlexaMacro", value: app.label, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa started '${app.label}' macro timer for ${delay} minute(s).")	
            	runIn(delay*60, controlHandler, [overwrite: true]) ; state.scheduled=true
            }
            if (delay < 9999) result = voicePost && !noAck ? replaceVoiceVar(voicePost, delay) : noAck ? "" : result
		}
        else result = "The control macro, '${app.label}', is already scheduled to run. You must cancel the execution or wait until it runs before you can run it again. %1%"
    }
    else result="The control macro, '${app.label}', is not properly configured. Use your SmartApp to configure the macro. %1%"
    return result
}
def controlHandler(){
   	state.scheduled = false
   	def cmd = [switch: switchesCMD, dimmer: dimmersCMD, cLight: cLightsCMD, tstat: tstatsCMD, lock: locksCMD, garage: garagesCMD, shade: shadesCMD]
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
        def ecobeeCustomRegEx = parent.MyEcobeeCMD && parent.ecobeeCMD ? getEcobeeCustomRegEx(tstats) : null 
		if (cmd.tstat =~ /home|away|sleep|resume program/ || (ecobeeCustomRegEx && cmd.tstat =~ /${ecobeeCustomRegEx}/)) tstats?."${cmd.tstat}"() 
	}
    if (garages && cmd.garage) garages?."${cmd.garage}"()
    if (shades && cmd.shade) shades?."${cmd.shade}"()
    if (extInt == "0" && http) httpGet(http) 
	if (extInt == "1" && ip && port && command){
        def deviceHexID  = convertToHex (ip, port)
        log.info "Device Network Id set to ${deviceHexID}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET /${command} HTTP/1.1\r\nHOST: ${ip}:${port}\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceHexID}"))    
    }
    if (SHM) sendLocationEvent(name: "alarmSystemStatus", value: SHM)
   	if ((pushMsg || smsNum || contacts) && smsMsg) sendMSG(smsNum, smsMsg, pushMsg, contacts)
    if (ttsMsg && ttsSpeaker){ 
    	if (ttsVolume) {ttsSpeaker?.setLevel(ttsVolume)}
        def ttsOutput = textToSpeech(ttsMsg, true)
        ttsSpeaker?.playTrack(ttsOutput.uri)
        }
    if (ttsMsg && ttsSynth) ttsSynth?.speak(ttsMsg)
    def data = [args: "I am activating the Control Macro: '${app.label}'."]
    sendLocationEvent(name: "askAlexaMacro", value: app.label, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa activated '${app.label}' macro.")	
	if (noteFeedAct && noteFeed) sendNotificationEvent("Ask Alexa activated Control Macro: '${app.label}'.")
}
private getEcobeeCustomRegEx(myEcobeeGroup){ 
    def myCustomClimate = "" 
    getEcobeeCustomList(myEcobeeGroup).each { myCustomClimate += "|${it}" } 
    myCustomClimate = myCustomClimate[1..myCustomClimate.length() - 1] 
    return myCustomClimate 
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
        fullMsg += voiceWindowShades ? shadeReport() : ""
        if (voiceTemperature && (voiceTemperature.size() == 1 || !voiceTempAvg)) fullMsg += reportStatus(voiceTemperature, "temperature")
        else if (voiceTemperature && voiceTemperature.size() > 1 && voiceTempAvg) fullMsg += "The average of the monitored temperature devices is: " + getAverage(voiceTemperature, "temperature") + " degrees. "
        if (voiceHumidity && (voiceHumidity.size() == 1 || !voiceHumidAvg)) fullMsg += reportStatus(voiceHumidity, "humidity")
        else if (voiceHumidity  && voiceHumidity.size() > 1 && voiceHumidAvg) fullMsg += "The average of the monitored humidity devices is " + getAverage(voiceHumidity, "humidity") + "%. "
        if (voiceTempSettingSummary && voiceTempSettingsType && voiceTempSettingsType !="autoAll") fullMsg += voiceTempSettings ? thermostatSummary(): ""
        else fullMsg += (voiceTempSettings && voiceTempSettingsType) ? reportStatus(voiceTempSettings, voiceTempSettingsType) : ""
        fullMsg += voiceSpeaker ? speakerReport() : ""
        if (currWeatherSel() || foreWeatherSel() || voiceSunset || voiceSunrise || voiceMoon || voiceTide ){
            if (location.timeZone || zipCode){
                Map cond = getWeatherFeature("conditions", zipCode)
                if ((cond == null) || cond.response.containsKey("error")) fullMsg += "Your hub location or supplied Zip Code is unrecognized by Weather Underground. "
				else {
					if (voiceWeatherLoc){
                        def type = currWeatherSel() || voiceSunset || voiceSunrise || voiceMoon || voiceTide ? "report" : ""
                        if (foreWeatherSel() && type) type += " and forecast"
                        else if (foreWeatherSel() && !type) type = "forecast"
                        if (type) fullMsg += "The following weather ${type} comes from " + cond.current_observation.observation_location.full.replaceAll(',', '') + ": "
                    }
                    fullMsg += currWeatherSel() || foreWeatherSel() || voiceSunset || voiceSunrise ? weatherAlerts() : ""
                    fullMsg += currWeatherSel() ? getWeatherReport() : ""
                    fullMsg += foreWeatherSel() || voiceSunset || voiceSunrise ? getWeatherForecast() : ""
                    fullMsg += voiceMoon ? getMoonInfo() : ""
                    fullMsg += voiceTide ? tideInfo(): ""
                }
            }
            else fullMsg += "Please set the location of your hub with the SmartThings mobile app, or enter a zip code to receive weather reports. "
        }
        fullMsg += voicePresence ? presenceReport() : ""
        fullMsg += voiceWater && waterReport() ? waterReport() : "" 
        fullMsg += voiceMotion && motionReport("motion") ? motionReport("motion") : ""
        fullMsg += voiceAccel && motionReport("acceleration") ? motionReport("acceleration") : ""
        fullMsg += voicePower && powerReport() ? powerReport() : ""
        fullMsg += voiceMode ? "The current SmartThings mode is set to, '${location.currentMode}'. " : ""
        fullMsg += voiceSHM ? "The current Smart Home Monitor status is '${location.currentState("alarmSystemStatus")?.value}'. " : ""
        fullMsg += voiceBattery && batteryReport() ? batteryReport() : ""
        fullMsg += voicePost ? voicePost : ""
	}
    catch(e){ fullMsg = "There was an error processing the report. Please try again. If this error continues, please contact the author of Ask Alexa. %1%" }
    if (!fullMsg && !allowNullRpt) fullMsg = "The voice report, '${app.label}', did not produce any output. Please check the configuration of the report within the SmartApp. %1%"  
    if ((parent.getAdvEnabled() && voiceRepFilter) || voicePre || voicePost) fullMsg = replaceVoiceVar(fullMsg,"")
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
	return result
}
def thermostatSummary(){
	String result = ""
    def monitorCount = voiceTempSettings.size(), matchCount = 0, err = false
    for (device in voiceTempSettings) {
        if (parent.nestCMD) try { device.poll() } catch(e) { }
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
                    if (parent.nestCMD) try { device.poll() } catch(e) { }
                    if (device.latestValue(voiceTempSettingsType) as int == voiceTempTarget as int){
                        result += "Of the ${monitorCount} monitored thermostats, only ${device} is set to ${voiceTempTarget} degrees. "
                    }
                }
            }
            else if (difCount && matchCount>1) {
                result += "Some of the thermostats are set to ${voiceTempTarget} degrees except"
                for (device in voiceTempSettings){
                    if (parent.nestCMD) try { device.poll() } catch(e) { }
                    if (device.latestValue(voiceTempSettingsType) as int != voiceTempTarget as int){
                        result += " ${device}"
                        difCount --
                        result += difCount && difCount == 1 ? " and" : difCount && difCount > 1 ? ", " : ". "
                    }
                }
            }
        }
    }
    else result="Some of your thermostats are not able to provide their setpoint. Please choose another device type to report on. %1%" 
    return result
}
def reportStatus(deviceList, type){
	String result = ""
    def appd = type==~/temperature|thermostatSetpoint|heatingSetpoint|coolingSetpoint|autoAll/ ? " degrees" : type == "humidity" ? " percent relative humidity" : ""
    if (type != "thermostatSetpoint" && type != "heatingSetpoint" && type !="coolingSetpoint" && type != "autoAll") {
        deviceList.each {deviceName->
			if (type=="level" && deviceName.latestValue("switch")=="on") result += "The ${deviceName} is on, and set to ${deviceName.latestValue(type) as int}%. "
            else if (type=="level" && deviceName.latestValue("switch")=="off") result += "The ${deviceName} is off. "
            else {
                def n = type=="temperature" ? roundValue(deviceName.latestValue(type)) : deviceName.latestValue(type)
                result += "The ${deviceName} is ${n}${appd}. " 
			}
        }
    }
	else if (type != "autoAll") deviceList.each { deviceName->
        if (parent.nestCMD) try { deviceName.poll() } catch(e) { }
        try { result += "The ${deviceName} is set to ${Math.round(deviceName.latestValue(type))}${appd}. " }
    	catch (e) { result = "The ${deviceName} is not able to provide its setpoint. Please choose another setpoint type to report on. " }
    }
    else if (type == "autoAll") deviceList.each { deviceName->
        if (parent.nestCMD) try { deviceName.poll() } catch(e) { }
        try { 
        	result += "The ${deviceName} has a cooling setpoint of ${Math.round(deviceName.latestValue("coolingSetpoint"))}${appd}, " +
        		"and a heating setpoint of ${Math.round(deviceName.latestValue("heatingSetpoint"))}${appd}. " 
        }
    	catch (e) { result += "The ${deviceName} is not able to provide one of its setpoints. Ensure you are in a thermostat mode that allows reading of these setpoints. " }
    }
    return result
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
    if (voicePresRepType=="1"){
    	def devCount = voicePresence.size()
        def presCount = voicePresence.count{it.latestValue("presence")=="present"}
        def awayCount = voicePresence.count{it.latestValue("presence")=="not present"}
        if (devCount>1 && devCount == presCount) result ="All monitored presence sensors are reading 'present'. "
        if (devCount>1 && devCount == awayCount) result ="All monitored presence sensors are reading 'not present'. "
    }
    if (voicePresRepType=="2" || voicePresRepType=="3") {
    	def type = voicePresRepType=="3" ? "not present" : voicePresRepType=="2" ? "present" : null
        if (voicePresence.latestValue("presence").contains(type)){
        	voicePresence.each { deviceName->
            	if (deviceName.latestValue("presence")==type) result += "${deviceName} is ${type}. "
    		}
        }
    }
    else if (!result) voicePresence.each {deviceName->result += "${deviceName} is " + deviceName.latestValue("presence") + ". " }
    if (voicePresentEvt) result += getLastEvt(voicePresence, "arrival", "present", "presence sensor")
    if (voiceGoneEvt) result += getLastEvt(voicePresence, "departure", "not present", "presence sensor")
    return result
}
def motionReport(type){
    def deviceList= type == "motion" ? voiceMotion : voiceAccel, result = "" 
    if ((voiceMotionOnly && type == "motion") || (voiceAccelOnly && type=="acceleration")) {
        if (deviceList.latestValue(type).contains("active")) {
        	deviceList.each { deviceName->
            	if (deviceName.latestValue(type)=="active") result += "The ${type} device, '${deviceName}', is reading motion. "
    		}
        }
    }
	else {
		deviceList.each {deviceName->
			def currVal = [active: "movement", inactive: "no movement"][deviceName.latestValue(type)] ?: deviceName.latestValue(type)
            result += "The ${type} device, '${deviceName}', is reading " + currVal + ". "
        }
	}
    if (voiceMotionEvt || voiceAccelEvt) result += getLastEvt(deviceList, "movement", "active", "sensor")
    return result 
}
def powerReport(){
	String result = ""
    voicePower.each { deviceName->
		def currVal = deviceName.currentValue("power")
        if (currVal == null) currVal=0
        if (voicePowerOn)  result += currVal>0 ? "The ${deviceName} is reading " + currVal  + " watts. " : ""
        else result += "The ${deviceName} is reading " + currVal  + " watts. "
	}
    return result 
}
def shadeReport(){
    String result = ""
    voiceWindowShades.each { deviceName->
		def currVal = deviceName.currentValue("windowShade")
        result += "The ${deviceName} is " + currVal  + ". "
	}
    return result
}
def doorWindowReport(){
	def countOpened = 0, countOpenedDoor = 0, countUnlocked = 0, listOpened = "", listUnlocked = ""
    String result = ""   
    if (voiceDoorSensors && voiceDoorSensors.latestValue("contact").contains("open")){  
        listOpened = voiceDoorSensors.findAll{it.latestValue("contact")=="open"}
        countOpened = listOpened.size()
	}
	if (voiceDoorControls && voiceDoorControls.latestValue("door").contains("open")){
        listOpened += voiceDoorControls.findAll{it.latestValue("door")=="open"}
        countOpenedDoor = listOpened.size()
    }
    if (voiceDoorLocks && voiceDoorLocks.latestValue("lock").contains("unlocked")){
        listUnlocked = voiceDoorLocks.findAll{it.latestValue("lock")=="unlocked"}
        countUnlocked = listUnlocked.size()
    }
    def totalCount = countOpenedDoor + countOpened
    if (voiceDoorAll){
        if (!totalCount && !countUnlocked) {
        	result += "All of the doors and windows are closed"
        	result += voiceDoorLocks ? " and locked. " : ". "
        }
        if (!countOpened && !countOpenedDoor && countUnlocked){
   			result += "All of the doors and windows are closed, but the "
            result += countUnlocked > 1 ? "following are unlocked: ${getList(listUnlocked)}. " :"${getList(listUnlocked)} is unlocked. "
    	}
        if ((countOpened || countOpenedDoor) && !countUnlocked){
   			result += voiceDoorLocks ? "All of the doors are locked, but the " : "The "
            result += totalCount > 1 ? "following doors or windows are open: ${getList(listOpened)}. " : "${getList(listOpened)} is open. "
    	}
    }   
	else {
		if ((countOpened || countOpenedDoor) && !countUnlocked) result += totalCount > 1 ? "The following doors or windows are currently open: ${getList(listOpened)}. " : "The ${getList(listOpened)} is open. "
        if (!countOpened && !countOpenedDoor && countUnlocked) result += countUnlocked > 1 ? "The following doors are unlocked: ${getList(listUnlocked)}. " : "The ${getList(listUnlocked)} is unlocked. "
	}
    if ((countOpened || countOpenedDoor) && countUnlocked){
		def verb = totalCount > 1 ? "following doors or windows are currently open: ${getList(listOpened)}" : "${getList(listOpened)} is open"
		def verb1 = countUnlocked > 1 ? "following are unlocked: ${getList(listUnlocked)}" : "${getList(listUnlocked)} is unlocked"
		result += "The ${verb}. Also, the ${verb1}. "
    }
    if (voiceDoorEvt && (voiceDoorSensors ||voiceDoorControls )) result += getLastEvt(voiceDoorSensors, "open", "open", "sensor")
    if (voiceLockEvt && voiceDoorLocks) result += getLastEvt(voiceDoorLocks, "unlock", "unlocked", "sensor")
    return result
}
def batteryReport(){
    String result = ""
    def batteryThresholdLevel = batteryThreshold as int, batList = voiceBattery.findAll{it.latestValue("battery")< batteryThresholdLevel}
    if (batList) {
        for (deviceName in batList){	
			if (deviceName.latestValue("battery")>1) result += "The '${deviceName}' battery is at ${deviceName.latestValue("battery")}%. "
			else if (deviceName.latestValue("battery")!=0 && deviceName.latestValue("battery")==null) result += "The '${deviceName}' battery is reading null, which may indicate an issue with the device. "
			else if (deviceName.latestValue("battery")<1) result +="The '${deviceName}' battery is at ${deviceName.latestValue("battery")}%; time to change this battery. "
        }
	}
    return result
}
def waterReport(){
    String result = ""
    def wetList = voiceWater.findAll{it.latestValue("water") != "dry"}
	if (!voiceWetOnly) for (deviceName in voiceWater) { result += "The ${deviceName} is ${deviceName.latestValue("water")}. " }
	else if (wetList) for (deviceName in wetList){ result += "The ${deviceName} is sensing water is present. " }
    return result
}
//Parent Code Access (from Child)-----------------------------------------------------------
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd) }
//Common Code (Child and Parent)		
 def imgURL() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/" }
 def getList(items){
    def result = "", itemCount=items.size() as int
	items.each{ result += it; itemCount --
		result += itemCount>1 ? ", " : itemCount==1 ? " and " : ""
    }
	return result
}
private roundValue(num){
    def result
    if (location.temperatureScale == "C") {
        String n = num as String
		if (n.endsWith(".0")) n = n - ".0"
        result=n
	}
	else result = Math.round(num)
    return result    
}
private formatURL(url){ return url.replaceAll(/\s/,"%20") }
private getEcobeeCustomList(myEcobeeGroup){ 
	def myEcobeeList = [] 
	myEcobeeGroup.each {myTstat -> 
		if (myTstat.currentValue("climateList")) { 
			def myCustomClimateList = myTstat.currentValue("climateList").toString().minus('[').minus(']').minus('Away').minus('Home').minus('Sleep').tokenize(',').unique() 
			myCustomClimateList.each { myEcobeeList += [ it.toLowerCase() ].unique() } 
		} 
	}
	return myEcobeeList 
}
//Common Code(Child)-----------------------------------------------------------
private currWeatherSel() { return voiceWeatherTemp || voiceWeatherHumid || voiceWeatherDew || voiceWeatherSolar || voiceWeatherVisiblity || voiceWeatherPrecip }
private foreWeatherSel() { return voiceWeatherToday || voiceWeatherTonight || voiceWeatherTomorrow}
def upDownChild(device, op, num){
    def numChange, newLevel, currLevel, defMove
    defMove = parent.lightAmt as int ?: 0 ; currLevel = device.currentValue("switch")=="on" ? device.currentValue("level") as int : 0
    if (op ==~/increase|raise|up|brighten/)  numChange = num == 0 ? defMove : num > 0 ? num : 0
    if (op ==~/decrease|down|lower|dim/) numChange = num == 0 ? -defMove : num > 0 ? -num  : 0
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
    return result
}
private getTimeOk(startTime, endTime) {
	def result = true, currTime = now(), start = startTime ? timeToday(startTime).time : null, stop = endTime ? timeToday(endTime).time : null
	if (startTime && endTime) result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	else if (startTime) result = currTime >= start
    else if (endTime) result = currTime <= stop
    return result
}
def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
    if(start && end) timeLabel = "Between " + timeParse("${start}", "h:mm a") + " and " +  timeParse("${end}", "h:mm a")
    else if (start) timeLabel = "Start at " + timeParse("${start}", "h:mm a")
    else if (end) timeLabel = "End at " + timeParse("${end}", "h:mm a")
	return timeLabel	
}
def macroTypeDesc(){
	def desc = "", PIN = (macroType ==~/CoRE|Control|GroupM/ || (macroType == "Group" && groupType == "lock") || (macroType == "Group" && groupType == "doorControl") || desc) && usePW ? " - PIN Required" : ""
    def customAck = !noAck && !voicePost ? "; uses standard acknowledgment message" : !noAck  && voicePost ? "; includes a custom acknowledgment message" :  "; there will be no acknowledgment messages"
    if (macroType ==~ /Control|CoRE/) customAck += cDelay>1 ? "; activates ${cDelay} minutes after triggered" : cDelay==1 ? "; activates one minute after triggered" : ""
    if (macroType == "Control" && (phrase || setMode || SHM || getDeviceDesc() != "Status: UNCONFIGURED${PIN} - Tap to configure" || getHTTPDesc() !="Status: UNCONFIGURED - Tap to configure" || (ttsMsg && (ttsSpeaker || ttsSynth))|| (contacts && smsMsg) || (smsNum && pushMsg && smsMsg))) desc= "Control Macro CONFIGURED${customAck}${PIN} - Tap to edit" 
    if (macroType =="Voice" && (voicePre || voiceSwitch || voiceDimmer || voiceDoorSensors || voiceDoorControls || voiceDoorLocks || voiceTemperature ||  
    	voiceTempSettings || voiceTempVar || voiceHumidVar || voiceHumidity || greyOutWeather()=="complete" || voiceWater || voiceMotion || voicePresence || 
        voiceBattery || voicePost || voiceMode || voiceSHM || voicePower || voiceAccel)) desc= "Voice Report CONFIGURED - Tap to edit" 
	if (macroType =="Group" && groupType && settings."groupDevice${groupType}") {
    	def groupDesc =[switch:"Switch Group",switchLevel:"Dimmer Group",thermostat:"Thermostat Group",colorControl:"Colored Light Group",lock:"Lock Group",doorControl: "Door Group",windowShade: "Window Shade Group"][groupType] ?: groupType
        def countDesc = settings."groupDevice${groupType}".size() == 1 ? "one device" : settings."groupDevice${groupType}".size() + " devices"
        if (parent.stelproCMD && groupType=="thermostat") customAck = "- Accepts Stelpro baseboard heater commands" + customAck
        if (parent.nestCMD && groupType=="thermostat") customAck = "- Accepts Nest 'Home'/'Away' commands" + customAck
        if (parent.ecobeeCMD && groupType=="thermostat") customAck = "- Accepts Ecobee 'Home'/'Away'/'Sleep'/'Resume Program' commands" + customAck
        if (parent.ecobeeCMD && parent.MyEcobeeCMD && groupType=="thermostat") customAck = "- Accepts My Ecobee 'Get Tips/'Erase Tips' commands" + customAck
        customAck = tstatDefaultHeat && groupType=="thermostat" ? "- Sends heating setpoint by default" + customAck : tstatDefaultCool && groupType=="thermostat" ? "- Sends cooling setpoint by default" + customAck : customAck
        desc = "${groupDesc} CONFIGURED with ${countDesc}${customAck}${PIN} - Tap to edit" 
    }
    if (macroType =="GroupM" &&  groupMacros) {
        customAck += addPost && !noAck ? " appended to the child macro messages" : noAck ? "" : " replacing the child macro messages"
        def countDesc = groupMacros.size() == 1 ? "one macro" : groupMacros.size() + " macros"
        desc = "Macro Group CONFIGURED with ${countDesc}${customAck}${PIN} - Tap to edit" 
    }
    if (macroType =="CoRE" && CoREName) desc = "Trigger '${CoREName}' piston${customAck}${PIN} - Tap to edit" 
    return desc ? desc : "Status: UNCONFIGURED - Tap to configure macro"
}
def greyOutMacro(){ return macroTypeDesc() == "Status: UNCONFIGURED - Tap to configure macro" ? "" : "complete" }
def greyOutStateHTTP(){ return getHTTPDesc() == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete" }
def reportSwitches(){
	def result="Status: UNCONFIGURED - Tap to configure"
    if (voiceSwitch || voiceDimmer){
        def switchEvt = voiceOnSwitchEvt ? "/Event" : "", dimmerEvt =voiceOnDimmerEvt ? "/Event" : "", switchOn = voiceOnSwitchOnly ? "(On${switchEvt})" : "(On/Off${switchEvt})"
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
    if (voiceDoorSensors || voiceDoorControls || voiceDoorLocks || voiceWindowShades ){
        def doorEvt = voiceDoorEvt ? " with door & window events" : "", lockEvt = voiceLockEvt ?"/lock events${doorEvt}":"${doorEvt}"
        def shadeRpt = voiceWindowShades && voiceWindowShades.size()>1  ? "Window shades report" : voiceWindowShades && voiceWindowShades.size()==1 ? "Window shade report" :""	
        def status = voiceDoorAll ? "full status${lockEvt}" : "open/unlocked status${lockEvt}"
        result  = voiceDoorSensors && voiceDoorSensors.size()>1 ? "Door/Window sensors" : voiceDoorSensors && voiceDoorSensors.size()==1 ? "Door/Window sensor" : ""
        if (voiceDoorControls) result  += voiceDoorSensors && voiceDoorControls.size()>1 && voiceDoorLocks ? ", door controls" : voiceDoorSensors && voiceDoorControls.size()==1 && voiceDoorLocks ? ", door control" : ""
        if (voiceDoorControls) result  += voiceDoorSensors && voiceDoorControls.size()>1 && !voiceDoorLocks ? " and door controls" : voiceDoorSensors && voiceDoorControls.size()==1 && !voiceDoorLocks ? " and door control" : ""
        if (voiceDoorControls) result  += !result && voiceDoorControls.size()>1 ? "Door controls" : !result && voiceDoorControls.size()==1 ? "Door control" : ""
		if (voiceDoorLocks) result  += result  && voiceDoorLocks.size()>1 ? " and locks" : result && voiceDoorLocks.size()==1 ? " and lock" : ""
        if (voiceDoorLocks) result  += !result && voiceDoorLocks.size()>1 ? "Locks" : !result && voiceDoorLocks.size()==1 ? "Lock" : ""
        result += (voiceDoorSensors && voiceDoorControls && voiceDoorLocks) || (voiceDoorSensors && voiceDoorControls) || (voiceDoorControls && voiceDoorLocks) || (voiceDoorSensors && voiceDoorLocks) ||
        	(voiceDoorSensors && voiceDoorSensors.size()>1) || (voiceDoorControls && voiceDoorControls.size()>1) || (voiceDoorLocks && voiceDoorLocks.size()>1 ) ? " report ${status}" : " reports ${status}"
    	result += voiceWindowShades ? ". Includes ${shadeRpt.toLowerCase()}" : ""
        if (!voiceDoorSensors && !voiceDoorControls && !voiceDoorLocks && voiceWindowShades) result = shadeRpt
    }
    return result
}
def reportSensors(){
	def result="Status: UNCONFIGURED - Tap to configure", count = 0
    if (voiceWater || voiceMotion || voicePower || voiceAccel){
        def accelEvt = voiceAccelEvt ? "/Event" : ""
        def acceleration = voiceAccelOnly ? "(Active${accelEvt})" : "(Active/Not Active${accelEvt})"
        def water = voiceWetOnly ? "(Wet)" : "(Wet/Dry)"
        def arriveEvt = voicePresentEvt ? "/Arrival" : ""
        def departEvt = voiceGoneEvt ? "/Departure" : ""
        def motionEvt = voiceMotionEvt ? "/Event" : ""
        def motion = voiceMotionOnly ? "(Active${motionEvt})" : "(Active/Not Active${motionEvt})"
        def power = voicePowerOn ? "(Active)" : "(Active/Not Active)"
        result  = voiceAccel && voiceAccel.size()>1 ? "Acceleration sensors ${acceleration}" : voiceAccel && voiceAccel.size()==1 ? "Acceleration sensor ${acceleration}" : ""
        if (voiceMotion) result += result && voiceMotion.size()>1 ? ", motion sensors ${motion}" : result && voiceMotion.size()==1 ? ", motion sensor ${motion}" : ""
        if (voiceMotion) result += !result && voiceMotion.size()>1 ? "Motion sensors ${motion}" : !result && voiceMotion.size()==1 ? "Motion sensor ${motion}" : ""
        if (voicePower) result += result && voicePower.size()>1 ? ", power meters" : result && voicePower.size()==1 ? ", power meter" : ""
        if (voicePower) result += !result && voicePower.size()>1 ? "Power meters ${power}" : !result && voicePower.size()==1 ? "Power meter ${power}" : ""
        if (voiceWater)  result += result && voiceWater && voiceWater.size()>1 ? " and water sensors ${water}" : result && voiceWater && voiceWater.size()==1 ? " and water sensor ${water}" : ""
        if (voiceWater)  result += !result && voiceWater && voiceWater.size()>1 ? "Water sensors ${water}" :!result && voiceWater && voiceWater.size()==1 ? "Water sensor ${water}" : ""
        count += voiceWater ? voiceWater.size() : 0
        count += voiceMotion ? voiceMotion.size() : 0
        count += voicePower ? voicePower.size() : 0
        count += voiceAccel ? voiceAccel.size() : 0
        result += count>1 ? " report status" : " reports status"
    }
    return result
}
def presenceSensors(){
	def result="Status: UNCONFIGURED - Tap to configure", count = 0
    if (voicePresence){
        def present = voicePresRepType=="3" ? "(Away)" : voicePresRepType=="2" ? "(Present)" : voicePresRepType=="1" ? "(Summary)" : "(Present/Away)"
		result = voicePresence.size()>1 ? "Presence sensors ${present}" : "Presence sensor ${present}" 
        count += voicePresence ? voicePresence.size() : 0
        result += count>1 ? " report status" : " reports status"
    }
    return result
}
def reportTemp(){
	def result="Status: UNCONFIGURED - Tap to configure", count = 0
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
        count += voiceHumidity ? voiceHumidity.size() : 0
        count += voiceTemperature ? voiceTemperature.size() : 0
        count += voiceTempSettings ? voiceTempSettings.size() : 0
        result += count>1 ? " report status" : " reports status"
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
	def result = currWeatherSel() ? "Current weather" : ""
	if (result && foreWeatherSel()) result +=", "
    result += (voiceWeatherToday && voiceWeatherTonight && voiceWeatherTomorrow) || (voiceWeatherToday && voiceWeatherTonight) || (voiceWeatherTonight && voiceWeatherTomorrow) || (voiceWeatherToday && voiceWeatherTomorrow) ? "Forecasts" : foreWeatherSel() ? "Forecast" : ""
    if (result && voiceWeatherWarnFull) result+=", Full weather alerts"
    if (result && (voiceSunrise || voiceSunset)) result +=", "
    result += voiceSunrise && voiceSunset ? "Sunrise/Sunset" : voiceSunrise ? "Sunrise" : voiceSunset ? "Sunset" : ""
	if (result && voiceMoon) result +=", "
    result += voiceMoon ? "Lunar rise/set/phases" : ""
    if (result && voiceTide) result +=", "
    result += voiceTide ?"Tide Information" : ""
    return result ? "Reports: ${result}" : "Status: UNCONFIGURED - Tap to configure"
}
def greyOutWeather(){ return foreWeatherSel() || voiceSunrise || voiceSunset || voiceMoon || voiceTide || currWeatherSel() ? "complete" : "" }
def deviceGreyOut(){ return getDeviceDesc() == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete" }
def getAudioDesc(){
    def result = "Status: UNCONFIGURED - Tap to configure"
    if (ttsMsg && ttsSpeaker) result = "TTS audio sent to speakers: ${ttsSpeaker}"
    if (ttsMsg && ttsSpeaker && ttsVolume) result+=", speaker volume: ${ttsVolume}"
    if (ttsMsg && ttsSynth) result = "TTS audio sent to speech devices: ${ttsSynth}"
    if (ttsMsg && ttsSynth && ttsSpeakers) result = "Text audio sent to speech and speaker devices"
    if (ttsMsg && ttsSynth && ttsSpeakers && ttsVolume) result+=", speaker volume: ${ttsVolume}"
    return result
}
def getDeviceDesc(){  
    def result, cmd = [switch: switchesCMD, dimmer: dimmersCMD, cLight: cLightsCMD, tstat: tstatsCMD, lock: locksCMD, garage: garagesCMD, shade: shadesCMD]
	def lvl = cmd.dimmer == "set" && dimmersLVL ? dimmersLVL as int : 0
	def cLvl = cmd.cLight == "set" && cLightsLVL ? cLightsLVL as int : 0
	def clr = cmd.cLight == "set" && cLightsCLR ? cLightsCLR  : ""
    def tLvl = tstats ? tstatLVL : 0
    lvl = lvl < 0 ? lvl = 0 : lvl >100 ? lvl=100 : lvl
    tLvl = tLvl < 0 ? tLvl = 0 : tLvl >100 ? tLvl=100 : tLvl
    cLvl = cLvl < 0 ? cLvl = 0 : cLvl >100 ? cLvl=100 : cLvl
    if (switches || dimmers || cLights || tstats || locks || garages || shades) {
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
        result += result && shades && cmd.shade ? "\n" : ""
        result += shades && cmd.shade ? "${shades} set to ${cmd.shade}" : ""
    }
    return result ? result : "Status: UNCONFIGURED - Tap to configure"
}
def getHTTPDesc(){
	def result = "", param = [http:http, ip:ip, port:port, cmd:command]
    if (extInt == "0" && param.http) result += param.http
    else if (extInt == "1" && param.ip && param.port && param.cmd) result += "http://${param.ip}:${param.port}/${param.cmd}"
    return result ? result : "Status: UNCONFIGURED - Tap to configure"
}
private getLastEvt(devGroup, evtTxt, searchVal, devTxt){
    def devEvt, evtLog=[],  lastEvt="I could not find any ${evtTxt} events in the log. "
	devGroup.each{ deviceName-> devEvt= deviceName.events()
		devEvt.each { if (it.value && it.value==searchVal) evtLog << [device: deviceName, time: it.date.getTime(), desc: it.descriptionText] }
	} 
    if (evtLog.size()>0){
        evtLog.sort({it.time})
        evtLog.reverse(true)
        def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
        def eventDay = new Date(evtLog.time[0]).format("EEEE, MMMM d, yyyy", location.timeZone)
        def voiceDay = today == eventDay ? "today" : "On " + eventDay  
        def evtTime = new Date(evtLog.time[0]).format("h:mm aa", location.timeZone)
        if (voiceEvtTimeDate && parent.getAdvEnabled()) lastEvt = "${voiceDay} at ${evtTime}. "
        else {
        	def multipleTxt = devGroup.size() >1 ? "within the monitored group was the ${evtLog.device[0]} ${devTxt}" : "was"
        	lastEvt = "The last ${evtTxt} event ${multipleTxt} ${voiceDay} at ${evtTime}. " 
        }
    }    
    return lastEvt
}
private replaceVoiceVar(msg, delay) {
	def df = new java.text.SimpleDateFormat("EEEE")
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date()), time = parseDate("","h:mm a"), month = parseDate("","MMMM"), year = parseDate("","yyyy"), dayNum = parseDate("","d")
    def varList = parent.getVariableList(), temp = varList.temp != "undefined device" ? roundValue(varList.temp) + " degrees" : varList.temp
    def humid = varList.humid, people = varList.people
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
    return msg
}
private timeParse(time, type) { return new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("${type}", location.timeZone)}
private parseDate(time, type){
    long longDate = time ? Long.valueOf(time).longValue() : now()
    def formattedDate = new Date(longDate).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    return new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", formattedDate).format("${type}", timeZone(formattedDate))
}
private getWeatherReport(){
	def temp, sb = new StringBuilder(), isMetric = location.temperatureScale == 'C'
	Map conditions = getWeatherFeature('conditions', zipCode)
    if ((conditions == null) || conditions.response.containsKey('error')) return "There was an error in the weather data received Weather Underground. "
	def cond = conditions.current_observation        
	if (voiceWeatherTemp){
        sb << 'The current temperature is '
        temp = isMetric ? roundValue(cond.temp_c) : Math.round(cond.temp_f)
		sb << temp + ' degrees with ' + cond.weather
        if (cond.weather =~/Overcast|Clear|Cloudy/) sb << " skies. "
        else if (cond.weather =~/Unknown|storm/) sb << " conditions. "
        else sb << ". "
	}
    if (voiceWeatherHumid){
    	sb << 'The relative humidity is ' + cond.relative_humidity + ' and the winds are '
        if ((cond.wind_kph.toFloat() + cond.wind_gust_kph.toFloat()) == 0.0) sb << 'calm. '
        else if (isMetric) {
        	sb << 'from the ' + cond.wind_dir + ' at ' + cond.wind_kph + ' km/h'
            if (cond.wind_gust_kph.toFloat() > 0) sb << ' gusting to ' + cond.wind_gust_kph + ' km/h. '
            else sb << '. '
		}
		else sb << cond.wind_string + '. '
		sb << 'The barometric pressure is '
        def pressure = isMetric ? cond.pressure_mb + ' millibars' : cond.pressure_in + ' inches'
        if (cond.pressure_trend=="+") sb << pressure + " and rising. "
        else if (cond.pressure_trend=="1") sb << pressure + " and falling. "
        else sb << "steady at " + pressure + ". "
	}
    if (voiceWeatherDew){
        temp = isMetric ? roundValue(cond.dewpoint_c) : Math.round(cond.dewpoint_f)
        def flTemp = isMetric ? roundValue(cond.feelslike_c) : Math.round(cond.feelslike_f.toFloat())
        if (temp == flTemp) sb << "The dewpoint and 'feels like' temperature are both ${temp} degrees. "
        else sb << "The dewpoint is ${temp} degrees, and the 'feels like' temperature is ${flTemp} degrees. "
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
        def precip = isMetric ? cond.precip_today_metric : cond.precip_today_in, p = 'no'
        if (!precip.isNumber()) {
        	if (precip == 'T') p = 'a trace of'
        } else if (precip) {
			if (precip.toFloat() > 0.0) {
				p = precip as String
				if (p.endsWith('.0')) p = p - '.0'
   			}
		} else precip = 0.0		
		sb << p
    	if ( (p != 'no') && (p != 'a trace of') ) {
			if (precip.toFloat() != 1.0) {
    			if (isMetric) sb << ' millimeters of' else sb << ' inches of'
    		}
            else {
              	if (isMetric) sb << ' millimeter of' else sb << ' inch of'
    		}
    	}
		sb << ' precipitation today. '
	}
	return sb.toString()
}
private getWeatherForecast(){
    def msg = "", isMetric = location.temperatureScale == 'C'
	Map weather = getWeatherFeature('forecast', zipCode)
    if ((weather == null) || weather.response.containsKey('error')) return "There was an error in the weather data received Weather Underground. "
    if (foreWeatherSel()){
		if (voiceWeatherToday){
			msg += "${weather.forecast.txt_forecast.forecastday[0].title}'s forecast calls for "
			msg += isMetric ? weather.forecast.txt_forecast.forecastday[0].fcttext_metric + " " : weather.forecast.txt_forecast.forecastday[0].fcttext + " "
        }
        if (voiceWeatherTonight){
			msg += "For ${weather.forecast.txt_forecast.forecastday[1].title}'s forecast you can expect "
			msg += isMetric ? weather.forecast.txt_forecast.forecastday[1].fcttext_metric + " " : weather.forecast.txt_forecast.forecastday[1].fcttext + " "
        }
        if (voiceWeatherTomorrow){
			msg += "Your forecast for ${weather.forecast.txt_forecast.forecastday[2].title} is "
			msg += isMetric ? weather.forecast.txt_forecast.forecastday[2].fcttext_metric + " " : weather.forecast.txt_forecast.forecastday[2].fcttext + " "
        }
		msg = msg.replaceAll( /(Cloudy|Overcast|Sunny) skies|(Cloudy|Overcast|Sunny)|(cloudy) skies|(cloudy)/, /$1 $2 $3 $4/ + " skies ")
    }
    if (voiceSunrise || voiceSunset){
    	Map astronomy = getWeatherFeature('astronomy', zipCode)
        if ((astronomy == null) || astronomy.response.containsKey('error')) return "There was an error in the sunrise or sunset data received Weather Underground. "
		Integer cur_hour = astronomy.moon_phase.current_time.hour.toInteger()				// get time at requested location
		Integer cur_min = astronomy.moon_phase.current_time.minute.toInteger()				// may not be the same as the SmartThings hub location
		Integer cur_mins = (cur_hour * 60) + cur_min
        Integer rise_hour = astronomy.moon_phase.sunrise.hour.toInteger()
        Integer rise_min = astronomy.moon_phase.sunrise.minute.toInteger()
        Integer rise_mins = (rise_hour * 60) + rise_min
        Integer set_hour = astronomy.moon_phase.sunset.hour.toInteger()
        Integer set_min = astronomy.moon_phase.sunset.minute.toInteger()
        Integer set_mins = (set_hour * 60) + set_min
        def verb1 = cur_mins >= rise_mins ? 'rose' : 'will rise'
        def verb2 = cur_mins >= set_mins ? 'set' : 'will set'
        if (rise_hour == 0) rise_hour = 12            
        if (set_hour > 12) set_hour = set_hour - 12
        String rise_minTxt = rise_min < 10 ? '0'+rise_min : rise_min
        String set_minTxt = set_min < 10 ? '0'+set_min : set_min
        if (voiceSunrise && voiceSunset) msg += "The sun ${verb1} this morning at ${rise_hour}:${rise_minTxt} am and ${verb2} tonight at ${set_hour}:${set_minTxt} pm. "
        else if (voiceSunrise && !voiceSunset) msg += "The sun ${verb1} this morning at ${rise_hour}:${rise_minTxt} am. "
        else if (!voiceSunrise && voiceSunset) msg += "The sun ${verb2} tonight at ${set_hour}:${set_minTxt} pm. "
    }
    return msg
}
def getMoonInfo(){
	def msg = "", dir, nxt, days, sss =""
   	Map astronomy = getWeatherFeature( 'astronomy', zipCode )
    if ((astronomy == null) || astronomy.response.containsKey('error')) return "There was an error in the lunar data received Weather Underground. "
	Integer cur_hour = astronomy.moon_phase.current_time.hour.toInteger()				// get time at requested location
	Integer cur_min = astronomy.moon_phase.current_time.minute.toInteger()				// may not be the same as the SmartThings hub location
	Integer cur_mins = (cur_hour * 60) + cur_min
    Integer rise_hour = astronomy.moon_phase.moonrise.hour.isInteger()? astronomy.moon_phase.moonrise.hour.toInteger() : -1
	Integer rise_min = astronomy.moon_phase.moonrise.minute.isInteger()? astronomy.moon_phase.moonrise.minute.toInteger() : -1
	Integer rise_mins = (rise_hour * 60) + rise_min
	Integer set_hour = astronomy.moon_phase.moonset.hour.isInteger()? astronomy.moon_phase.moonset.hour.toInteger() : -1
	Integer set_min = astronomy.moon_phase.moonset.minute.isInteger()? astronomy.moon_phase.moonset.minute.toInteger() : -1
    Integer set_mins = (set_hour * 60) + set_min
    String verb1 = cur_mins >= rise_mins ? 'rose' : 'will rise'
    String verb2 = cur_mins >= set_mins ? 'set' : 'will set'
    String rise_ampm = rise_hour >= 12 ? "pm" : "am"
    String set_ampm = set_hour >= 12 ? "pm" : "am"
    if (rise_hour == 0) rise_hour = 12
    if (set_hour == 0) set_hour = 12
    if (rise_hour > 12) rise_hour = rise_hour - 12
    if (set_hour > 12) set_hour = set_hour - 12
    String rise_minTxt = rise_min < 10 ? '0'+rise_min : rise_min
    String set_minTxt = set_min < 10 ? '0'+set_min : set_min
    String riseTxt = "${verb1} at ${rise_hour}:${rise_minTxt} ${rise_ampm}"
    String setTxt =  "${verb2} at ${set_hour}:${set_minTxt} ${set_ampm}"
    msg += 'The moon '
	if (set_mins < 0) msg += "${riseTxt}. "  
	else if (rise_mins < 0) msg += "${setTxt}. "
	else msg += rise_mins < set_mins ? "${riseTxt} and ${setTxt}. " : "${setTxt} and ${riseTxt}. "    
    def moon = astronomy.moon_phase
    def m = moon.ageOfMoon.toInteger()
    sss = m == 1 ? "" : "s"
	msg += "The moon is ${m} day${sss} old at ${moon.percentIlluminated}%, "
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
	sss = days.toInteger() != 1 ? "s" : ""
    switch (moon.percentIlluminated.toInteger()) {
    	case 0:
            msg += 'New Moon, and the First Quarter moon is in 7 days. '
            break
        case 1..49:
            msg += "${dir} Crescent, and the ${nxt} moon is "
            if (days == 0) msg+= "later today. " else msg+= "in ${days} day${sss}. "               
            break
		case 50:
            if (dir == "Waxing") msg += "First Quarter, " else msg += "Third Quarter, "
            msg += "and the ${nxt} Moon is in ${days} day${sss}. "
            break
        case 51..99:
            msg += "${dir} Gibbous, and the ${nxt} moon is "
            if (days == 0) msg += "later today. " else msg += "in ${days} day${sss}. "
            break
        case 100:
            msg += 'Full Moon, and the Third Quarter moon is in 7 days. '
            break
        default:
			msg += '. '
	}
    return msg
}
def weatherAlerts(){
	String msg = ""
    def brief = false
    Map advisories = getWeatherFeature('alerts', zipCode)
    if ((advisories == null) || advisories.response.containsKey('error')) return "There was an error in the weather alerts data received Weather Underground. "
	def alerts = advisories.alerts
    if ((alerts != null) && (alerts.size() > 0)) {
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
                    def desc = alert.description.startsWith("A") ? "An" : "A"
                    msg += "${desc} ${alert.description} is in effect from ${alert.date} until ${alert.expires}. "
                    if ( !brief ) {
                        warn = alert.message.replaceAll("\\.\\.\\.", ', ').replaceAll("\\* ", ' ') 				// convert "..." and "* " to a single space (" ")
                        def warnings = [] 																		// See if we need to split up the message (multiple warnings are separated by a date stamp)
                        def i = 0
                        while ( warn != "" ) {
                            def ddate = warn.replaceFirst(/(?i)(.+?)(\d{3,4} (am|pm) .{3} .{3} .{3} \d+ \d{4})(.*)/, /$2/)
                            if ( ddate && (ddate.size() != warn.size())) {
                                def d = warn.indexOf(ddate)
                                warnings[i] = warn.take(d-1)
                                warn = warn.drop(d+ddate.size())
                                i ++
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
                            } 
                            else warn = " "
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
	return msg
}
private tideInfo() {
	String msg = ""
    Map wdata = getWeatherFeature("tide/astronomy", zipcode)
    if ((wdata == null) || wdata.response.containsKey('error')) return "There was an error in the tide data received Weather Underground. "
	def tideSite = wdata.tide.tideInfo.tideSite.join(",").replaceAll(',', '' )
	if (tideSite == "") {
		msg = "No tide station found near this location"
		if (zipCode) msg += " (${zipCode}). " else msg+= '. '
		return msg
	}        
	Integer cur_hour = wdata.moon_phase.current_time.hour.toInteger()				// get time at requested location
	Integer cur_minute = wdata.moon_phase.current_time.minute.toInteger()			// may not be the same as the SmartThings hub location
	Integer cur_mins = (cur_hour * 60) + cur_minute
	String timeZoneTxt = wdata.tide.tideSummary[0].date.pretty.replaceAll(/\d+:\d+ .{2} (.{3}) .*/, /$1/)
	Integer count = 0, index = 0
	while (count < 4) {	
		def tide = wdata.tide.tideSummary[index]
        index ++
		if ((tide.data.type == 'High Tide') || (tide.data.type == 'Low Tide')) {
			count ++
			Integer tide_hour = tide.date.hour.toInteger()
			Integer tide_min = tide.date.min.toInteger()
			Integer tide_mins = (tide_hour * 60) + tide_min	               
			String dayTxt = 'this'
			if (tide_mins < cur_mins) {														// tide event is tomorrow
				dayTxt = 'tomorrow'
				tide_mins = tide_mins + 1440
			}
			Integer minsUntil = tide_mins - cur_mins
			Integer whenHour = minsUntil / 60
			Integer whenMin = minsUntil % 60				
            String ampm = 'am'
			String whenTxt = 'morning'
			if (tide_hour > 11) {
                ampm = 'pm'
				if ( tide_hour < 18) whenTxt = 'afternoon'                       
				else if (tide_hour < 20) whenTxt = 'evening'
				else {
					if (dayTxt == 'this') {
						whenTxt = 'tonight' 
						dayTxt = ''
					} else whenTxt = 'night'
				}
			}                
			if (count <= 2) msg += 'The next '
			else if (count == 3) msg += 'Then '
			else msg += 'followed by '	
			msg += tide.data.type + ' '
			if (tide_hour > 12) tide_hour = tide_hour - 12
            if (tide_hour==0) tide_hour = 12
            String tide_minTxt
            if (tide_min < 10) tide_minTxt = '0'+tide_min else tide_minTxt = tide_min                
			if (count == 1) {
				msg += "at ${tideSite} will be in "
				if (whenHour > 0) {
					msg += "${whenHour} hour"
					if (whenHour > 1) msg +='s'
					if (whenMin > 0) msg += ' and'
				}
				if (whenMin > 0) {
					msg += " ${whenMin} minute"
					if (whenMin > 1) msg +='s'
				}
				msg += " at ${tide_hour}:${tide_minTxt} ${ampm} ${dayTxt} ${whenTxt} (all times ${timeZoneTxt}). "
			} else if (count == 2) msg += "will be ${dayTxt} ${whenTxt} at ${tide_hour}:${tide_minTxt} ${ampm}. "
			else if (count == 3) msg += "again ${dayTxt} ${whenTxt} at ${tide_hour}:${tide_minTxt} ${ampm}, "
			else msg += "at ${tide_hour}:${tide_minTxt} ${ampm} ${dayTxt} ${whenTxt}. "
		}
	}
    return msg		
}
//Various Functions-----------------------------------------------------------
def sendMSG(num, msg, push, recipients){
    if (location.contactBookEnabled && recipients) sendNotificationToContacts(msg, recipients)
    else {
    	if (num) {sendSmsMessage(num,"${msg}")}
    	if (push) {sendPushMessage("${msg}")}
    }
}
def toggleState(swDevices){ swDevices.each{ it.currentValue("switch")=="off" ? it.on() : it.off() } }
private setColoredLights(switches, color, level, type){
	def getColorData = parent.fillColorSettings().find {it.name==color}
    def hueColor = getColorData? getColorData.hue : 0, satLevel = getColorData ? getColorData.sat:0
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
private switchesSel() { return switches || (deviceAlias && switchesAlias) }
private dimmersSel() { return dimmers || (deviceAlias && dimmersAlias) }
private cLightsSel() { return cLights || (deviceAlias && cLightsAlias) }
private doorsSel() { return doors || (deviceAlias && doorsAlias) }
private locksSel() { return locks || (deviceAlias && locksAlias) }
private ocSensorsSel() { return ocSensors || (deviceAlias && ocSensorsAlias) }
private shadesSel() { return shades || (deviceAlias && shadesAlias) }
private tstatsSel() { return tstats || (deviceAlias && tstatsAlias) }
private tempsSel() { return temps || (deviceAlias && tempsAlias) }
private humidSel() { return humid || (deviceAlias && humidAlias) }
private speakersSel() { return speakers || (deviceAlias && speakersAlias) }
private waterSel() { return water || (deviceAlias && waterAlias) }
private presenceSel() { return presence || (deviceAlias && presenceAlias) }
private motionSel() { return motion || (deviceAlias && motionAlias) }
private accelerationSel() { return acceleration || (deviceAlias && accelerationAlias) }
private songOptions() {
    if (speakersSel()) {
        def options = new LinkedHashSet()
        state.songLoc.each { options << it.station }
        if (speakers) {
        	speakers.each {speaker->
            	def states = speaker.statesSince("trackData", new Date(0), [max:30]), dataMaps = states.collect{it.jsonValue}
            	dataMaps.station.each{ options << it }
			}
        }
        if (deviceAlias && speakersAlias){
        	speakersAlias.each {speaker->
            	def states = speaker.statesSince("trackData", new Date(0), [max:30]), dataMaps = states.collect{it.jsonValue}
            	dataMaps.station.each{ options << it }
			}
        }
        options.take(20) as List
    }
}
private songLocations(){
    if (!state.songLoc) state.songLoc=[]
    if (speakers) getSongs(speakers)
    if (deviceAlias && speakersAlias) getSongs(speakersAlias)
}
def getSongs(device){
	def memCount = sonosMemoryCount as int
    device.each{speaker->       
		for (int i=1; i<memCount+1; i++){
			if (settings."sonosSlot${i}Music" && settings."sonosSlot${i}Name"){
				def song = settings."sonosSlot${i}Music", songs = speaker.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
				def data = songs.find {s -> s.station == song}
				if (data && !state.songLoc.find{it.station==song}){
					log.debug "I added the song '" + settings."sonosSlot${i}Music" + "' to the database."
					state.songLoc << data
				}
			}
		}
	}
}
def memoryState(){
	def slotCount = 0, memCount = sonosMemoryCount as int
	for (int i=1; i<memCount+1; i++){ if (settings."sonosSlot${i}Music" && settings."sonosSlot${i}Name") slotCount ++ }
    def result = slotCount ? "complete" : ""
}
def memoryDesc(){
	def result = ""
    if (sonosMemoryCount) {
        def memCount =sonosMemoryCount as int, duplicates =0
        for (int i =1; i < memCount +1 ;i++) {
        	result += settings."sonosSlot${i}Name" && settings."sonosSlot${i}Music" ?  "Slot ${i} Name: " + settings."sonosSlot${i}Name" : ""
        	if (i < memCount && result && settings."sonosSlot${i+1}Name" && settings."sonosSlot${i+1}Music") result +="\n"
        }
        for (int i =1; i < memCount +1 ;i++) {
        	for (int d = i+1; d < memCount+1; d ++){
            	if (settings."sonosSlot${i}Name" && settings."sonosSlot${i}Music" && settings."sonosSlot${d}Name" && settings."sonosSlot${d}Music" && settings."sonosSlot${i}Name" == settings."sonosSlot${d}Name") duplicates++
            }
        }
        if (result && duplicates>0) result += "\n\n**WARNING**\nYou have duplicate memory slot names. Each name must be unique. Please edit the names to resolve this."
    }
    return result ? result : "No memory slots configured - Tap to configure"
}
def getMacroList(type){
    def result=[]
    if (type =="all") childApps.each{ if (it.macroType !="GroupM" && it.macroType!="Group") result << ["${it.label}": "${it.label} (${it.macroType})"] }
    if (type == "flash") childApps.each{ if (it.macroType =="GroupM" || it.macroType=="Voice") result << ["${it.label}":"${it.label} (${it.macroType=="GroupM"?"Macro Group":"Voice"})"] }
    return result
}
def coreHandler(evt) {
	log.debug "Refreshing CoRE Piston List"
    if (evt.value =="refresh") { state.CoREPistons = evt.jsonData && evt.jsonData?.pistons ? evt.jsonData.pistons : [] }
}
def msgHandler(evt) {
    if (!state.msgQueue) state.msgQueue=[]
    log.debug "New message added to message queue from: " + evt.value
	state.msgQueue<<["date":evt.date.getTime(),"appName":evt.value,"msg":evt.descriptionText,"id":evt.unit]
}
def msgDeleteHandler(evt){
	if (state.msgQueue && state.msgQueue.size()>0){
    	if (evt.unit && evt.value){
        	log.debug evt.value + " is deleting messages from the message queue."
    		state.msgQueue.removeAll{it.appName==evt.value && it.id==evt.unit}
        }
        else log.debug "Incorrect delete parameters sent to message queue. Nothing was deleted"
    } 
    else log.debug "Message queue is empty. No messages were deleted."
}
def getCoREMacroList(){ return getChildApps().findAll {it.macroType !="CoRE"}.label }
def getVariableList(){
    def temp = voiceTempVar ? getAverage(voiceTempVar, "temperature") : "undefined device"
    def humid = voiceHumidVar ? getAverage(voiceHumidVar, "humidity") + " percent relative humidity" : "undefined device"
    def present = voicePresenceVar.findAll{it.currentValue("presence")=="present"}
   	def people = present ? getList(present) : "No people present"
    return [temp: temp, humid: humid, people: people]
}
private getAverage(device,type){
	def total = 0
	device.each {total += it.latestValue(type)}
    return Math.round(total/device.size())
}
def getTstatLimits() { return [hi:tstatHighLimit, lo: tstatLowLimit] }
def getAdvEnabled() { return advReportOutput }
def OAuthToken(){
	try {
        createAccessToken()
		log.debug "Creating new Access Token"
	} catch (e) { log.error "Access Token not defined. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth." }
}
def macroDesc(){def results = childApps.size() ? childApps.size()==1 ? "One Voice Macro Configured" : childApps.size() + " Voice Macros Configured" : "No Voices Macros Configured\nTap to create new macro"}
def getDesc(param){ return param  ? "Status: CONFIGURED - Tap to edit/view" : "Status: UNCONFIGURED - Tap to configure" }
def getAliasList(){
	def result =[]
    state.aliasList.each{ result << ["${it.aliasName}":"${it.aliasName} (${it.aliasTypeFull})"] }
    return result
}
def getAliasDisplayList(){
    def count = state.aliasList.size(), result=""
    for (int i=0; i < count; i++) {
    	def deviceType = mapDevices(true).find{it.fullListName==state.aliasList.aliasTypeFull[i]}, deviceName, displayName
        if (deviceType) deviceName = deviceType.devices.find {it=state.aliasList.aliasDevice[i]}
        displayName= deviceType && deviceName ? state.aliasList.aliasDevice[i] : state.aliasList.aliasDevice[i]+" (missing)"
		result += state.aliasList.aliasName[i] + " (" + state.aliasList.aliasTypeFull[i]  + ") = "+ displayName  
    	if (i < count-1) result +="\n"
    }
    return result
}
def getDeviceAliasList(aliasType){
    def result = mapDevices(true).find{it.fullListName==aliasType}, resultList =[]
    if (result) result.devices.each{ resultList <<"${it}" }
    return resultList
}
def fillColorSettings(){
	def colorData = []
    colorData << [name: "White", hue: 0, sat: 0] << [name: "Orange", hue: 11, sat: 100] << [name: "Red", hue: 100, sat: 100] << [name: "Purple", hue: 77, sat: 100]
    colorData << [name: "Green", hue: 30, sat: 100] << [name: "Blue", hue: 66, sat: 100] << [name: "Yellow", hue: 16, sat: 100] << [name: "Pink", hue: 95, sat: 100]
    colorData << [name: "Cyan", hue: 50, sat: 100] << [name: "Chartreuse", hue: 25, sat: 100] << [name: "Teal", hue: 44, sat: 100] << [name: "Magenta", hue: 92, sat: 100]
	colorData << [name: "Violet", hue: 83, sat: 100] << [name: "Indigo", hue: 70, sat: 100]<< [name: "Marigold", hue: 16, sat: 75]<< [name: "Raspberry", hue: 99, sat: 75]
    colorData << [name: "Fuchsia", hue: 92, sat: 75] << [name: "Lavender", hue: 83, sat: 75]<< [name: "Aqua", hue: 44, sat: 75]<< [name: "Amber", hue: 11, sat: 75]
    colorData << [name: "Carnation", hue: 99, sat: 50] << [name: "Periwinkle", hue: 70, sat: 50]<< [name: "Pistachio", hue: 30, sat: 50]<< [name: "Vanilla", hue: 16, sat: 50]
    if (customName && (customHue > -1 && customerHue < 101) && (customSat > -1 && customerSat < 101)) colorData << [name: customName, hue: customHue as int, sat: customSat as int]
    return colorData
}
def getDeviceList(){
	def result = []
    try {
        mapDevices(false).each{
    		def devicesGroup = it.devices, devicesType = it.type
    		devicesGroup.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type: devicesType, devices: devicesGroup] }
    	}
    }
    catch (e) { log.warn "There was an issue parsing the device labels. Be sure all of the devices are uniquely named/labeled and that none of them are blank (null). " }
    return result
}
def findNullDevices(){
	def result=""
    mapDevices(false).each{devicesGroup->
        devicesGroup.devices.each {result += !it.label ? it.name + "\n" : "" }
    }
	if (result) result = "You have the following device(s) with a blank (null) label:\n\n" + result + "\nBe sure all of the devices are uniquely labeled and that none of them are blank(null)."
    return result
}
def optionCount(start, end){
	def result =[]
    for (int i=start; i < end+1; i++){ result << i }
    return result
}
def mapDevices(isAlias){
	def result =[],  ext= isAlias ? "Alias": ""
	if (settings."switches${ext}") result << [devices: settings."switches${ext}", type : "switch",fullListName:"Switch"]
	if (settings."dimmers${ext}") result << [devices: settings."dimmers${ext}", type : "level",fullListName:"Dimmer"]
	if (settings."cLights${ext}") result << [devices: settings."cLights${ext}", type : "color",fullListName:"Colored Light"]
	if (settings."doors${ext}") result << [devices: settings."doors${ext}", type : "door",fullListName:"Door Control"]
	if (settings."shades${ext}") result << [devices: settings."shades${ext}", type : "shade",fullListName:"Window Shade"]
	if (settings."locks${ext}") result << [devices: settings."locks${ext}", type : "lock",fullListName:"Lock"]
	if (settings."tstats${ext}") result << [devices: settings."tstats${ext}", type : "thermostat",fullListName:"Thermostat"]
	if (settings."speakers${ext}") result << [devices: settings."speakers${ext}", type : "music",fullListName:"Speaker"]
	if (settings."temps${ext}") result << [devices: settings."temps${ext}", type : "temperature",fullListName:"Temperature Sensor"]
	if (settings."humid${ext}") result << [devices: settings."humid${ext}", type : "humidity",fullListName:"Humidity Sensor"]
	if (settings."ocSensors${ext}") result << [devices: settings."ocSensors${ext}", type : "contact",fullListName:"Open/Close Sensor"]
	if (settings."water${ext}") result << [devices: settings."water${ext}", type : "water",fullListName:"Water Sensor"]
	if (settings."motion${ext}") result << [devices: settings."motion${ext}", type : "motion",fullListName:"Motion Sensor"]
	if (settings."presence${ext}") result << [devices: settings."presence${ext}", type : "presence",fullListName:"Presence Sensor"]
	if (settings."acceleration${ext}") result << [devices: settings."acceleration${ext}", type : "acceleration",fullListName:"Acceleration Sensor"]	
	return result
}
def upDown(device, type, op, num, deviceName){
    def numChange, newLevel, currLevel, defMove, txtRsp = ""
    if (type=="color" || type=="level") { defMove = lightAmt as int ?: 0 ; currLevel = device.currentValue("switch")=="on" ? device.currentValue("level") as int : 0 } 
    if (type=="music") { defMove = speakerAmt as int ?: 0 ;currLevel = device.currentValue("level") as int }
    if (type=="thermostat") { defMove=tstatAmt as int ?: 0 ; currLevel =device.currentValue("temperature") as int }
    if (op ==~/increase|raise|up|brighten/)  numChange = num == 0 ? defMove : num > 0 ? num : 0
    if (op ==~/decrease|down|lower|dim/) numChange = num == 0 ? -defMove : num > 0 ? -num  : 0
    newLevel = currLevel + numChange; newLevel = newLevel > 100 ? 100 : newLevel < 0 ? 0 : newLevel
    if ((type =="level" || type=="color") && defMove > 0 ){
        if (device.currentValue("switch")=="on") {
            if (newLevel < 100 && newLevel > 0 ) txtRsp="I am setting the ${deviceName} to a new value of ${newLevel}%. "
            if (newLevel == 0) txtRsp= "The new value would be zero or below, so I am turning the ${device} off. "	
        }
        if (device.currentValue("switch")=="off") {
            if (newLevel == 0) txtRsp= "The ${deviceName} is off. I am taking no action. "
            if (newLevel < 100 && newLevel > 0 ) txtRsp="I am turning the ${deviceName} on and setting it to a level of ${newLevel}%. "
        }
    	if (newLevel == 100) txtRsp= currLevel < 99 ? "I am increasing the level of the ${deviceName} to its maximum level. " : "The ${deviceName} is at its maximum level. "      
    }
    else if (defMove == 0) txtRsp = "The default increase or decrease value is set to zero within the SmartApp. I am taking no action. "
    return [newLevel: newLevel, msg:txtRsp]
}
def getLastEvent(device, count, deviceName) {
    def lastEvt= device.events(), eDate, eDesc, today, eventDay, voiceDay, i , evtCount = 0, result = ""
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
	result = evtCount>1 ? "The following are the last ${evtCount} events for the ${deviceName}: " + result : "The last event for the ${deviceName} was: " + result
    result += diff > 1 ? "There were ${diff} items that were skipped due to the description being empty. " : diff==1 ? "There was one item that was skipped due to the description being a null value. " : ""
    if (evtCount==0) result="There were no past events in the device log. "
    return result
}
def flash(){
    String outputTxt = ""
	try {
        if (flash && flashMacro){
            def child = getChildApps().find {it.label == flashMacro}
            if (child.macroType != "GroupM") outputTxt = child.getOkToRun() ? child.macroResults("", "", "", "", "") : "You have restrictions within the ${fullMacroName} named, '${child.label}', that prevent it from running. Check your settings and try again. %1%"
            else outputTxt = processMacroGroup(child.groupMacros, child.voicePost, child.addPost, child.noAck, child.label, child.noteFeed, child.noteFeedData) 
            if (outputTxt.endsWith("%")) outputTxt=outputTxt[0..-4]
        }
        else outputTxt = "You do not have the flash briefing option enabled in your Ask Alexa Smart App, or you don't have any macros selected for the briefing output. Go to Settings in your SmartApp to fix this. "
    } catch (e) { outputTxt ="There was an error producing the flash briefing report. Check your settings and try again." }
    log.debug "Sending Flash Briefing Output: "+outputTxt
    return ["uid": "1",
		"updateDate": new Date().format("yyyy-MM-dd'T'HH:mm:ss'.0Z'"),
		"titleText": "${flashMacro} macro report",
		"mainText": outputTxt,
		"redirectionUrl": "https://graph.api.smartthings.com/",
		"description": "Ask Alexa Flash Briefing Report"]
}
def setupData(){
	log.info "Cheat sheet web page located at : ${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}"
    def result ="<div style='padding:10px'><i><b><a href='http://aws.amazon.com' target='_blank'>Lambda</a> code variables:</b></i><br><br>var STappID = '${app.id}';<br>var STtoken = '${state.accessToken}';<br>"
    result += "var url='${getApiServerUrl()}/api/smartapps/installations/' + STappID + '/' ;<br><br><hr>"
    result += flash ? "<i><b><a href='http://developer.amazon.com' target='_blank'>Amazon ASK</a> Flash Briefing Skill URL:</b></i><br><br>${getApiServerUrl()}/api/smartapps/installations/${app.id}/flash?access_token=${state.accessToken}<br><br><hr>":""
	result += "<i><b><a href='http://developer.amazon.com' target='_blank'>Amazon ASK</a> Custom Slot Information:</b></i><br><br><b>CANCEL_CMDS</b><br><br>cancel<br>stop<br>unschedule<br><br><b>DEVICE_TYPE_LIST</b><br><br>"
    fillTypeList().each{result += it + "<br>"}
    result += "<br><b>LIST_OF_DEVICES</b><br><br>"
    def DEVICES=[], deviceList = getDeviceList()
    if (deviceList) deviceList.name.each{DEVICES << it }
    if (msgQueue) DEVICES <<"message queue"<<"messages"<<"message"<<"queue"
    if (deviceAlias && state.aliasList) state.aliasList.each{DEVICES << it.aliasNameLC}
    def duplicates = DEVICES.findAll{DEVICES.count(it)>1}.unique()
    if (DEVICES && duplicates.size()){ 
    	result += "<b>**NOTICE:</b>The following duplicate(s) are only listed once below in LIST_OF_DEVICES:<br><br>"
        duplicates.each{result +="* " + it +" *<br><br>"}
        result += "Be sure to have unique names for each device/alias and only use each name once within the parent app.**<br><br>" 	
    }
    if (DEVICES) DEVICES.unique().each { result += it + "<br>" }
	else result += "none<br>"
	result += "<br><b>LIST_OF_OPERATORS</b><br><br>on<br>off<br>toggle<br>up<br>down<br>increase<br>decrease<br>lower<br>raise<br>brighten<br>dim<br>status<br>events<br>event<br>low<br>medium<br>"+
    	"high<br>lock<br>unlock<br>open<br>close<br>maximum<br>minimum<br>list<br>"
    result += speakersSel() ?"stop<br>pause<br>mute<br>unmute<br>next track<br>previous track<br>" : ""
    result += msgQueue || speakersSel() || (tstatsSel() && ecobeeCMD && MyEcobeeCMD) ?"play<br>" : ""
    result += msgQueue || (tstatsSel() && ecobeeCMD && MyEcobeeCMD) ? "erase<br>delete<br>clear<br>reset<br>" : ""
    result += tstatsSel() && ecobeeCMD && MyEcobeeCMD ? "get<br>restart<br>repeat<br>replay<br>give<br>load<br>reload<br>" : ""
    result += tstatsSel() && nestCMD && nestMGRCMD ? "report<br>" : ""
    result += "<br><b>LIST_OF_PARAMS</b><br><br>"
    def PARAMS=["heat","cool","heating","cooling","auto","automatic","AC"]
    if (tstatsSel() && stelproCMD) PARAMS<< "eco"<<"comfort"
    if (tstatsSel() && (nestCMD || ecobeeCMD)) PARAMS<<"home"<<"away"
    if (tstatsSel() && ecobeeCMD) PARAMS<<"sleep"<<"resume program"
	if (tstats && MyEcobeeCMD){  getEcobeeCustomList(tstats).each { PARAMS<<"${it}" } }     
    if (tstatsSel() && ecobeeCMD && MyEcobeeCMD) PARAMS<<"tips"<<"tip"
    if (sonosCMD && speakersSel() && sonosMemoryCount){
    	def memCount = sonosMemoryCount as int
    	for (int i=1; i<memCount+1; i++){
    		if (settings."sonosSlot${i}Name" && settings."sonosSlot${i}Music") PARAMS<<settings."sonosSlot${i}Name".replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()
    	}
    }
    if (cLightsSel() || childApps.size()) { fillColorSettings().each {PARAMS<<it.name.toLowerCase()}}
    duplicates = PARAMS.findAll{PARAMS.count(it)>1}.unique()
    if (duplicates.size()){ 
            result += "<b>**NOTICE:</b>The following duplicate(s) are only listed once below in LIST_OF_PARAMS:<br><br>"
            duplicates.each{result +="* " + it +" *<br><br>"}
            def objectName = []
            if (sonosCMD) objectName<<"SONOS memory slots"
            if (ecobeeCMD) objectName<<"Ecobee custom climates"
            objectName <<"custom colors"
            result += "Be sure to have unique names for your ${getList(objectName)}.**<br><br>"
	}
	PARAMS.unique().each {result += it + "<br>" } 
    result +="<br><b>LIST_OF_SHPARAM</b><br><br>"  
    if (listSHM || listRoutines || listModes){
        def SHPARAM =[]
        if (listSHM) SHPARAM << "arm" << "armed stay" << "armed away" << "disarm" << "off"
        if (listRoutines) listRoutines.each { if (it) SHPARAM << it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
        if (listModes) listModes.each { if (it) SHPARAM << it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
        duplicates = SHPARAM.findAll{SHPARAM.count(it)>1}.unique()
        if (duplicates.size()){ 
            result += "<b>**NOTICE:</b>The following duplicate(s) are only listed once below in LIST_OF_SHPARAM:<br><br>"
            duplicates.each{result +="* " + it +" *<br><br>"}
            result += "Be sure to have unique names for your SmartThings modes and routines and that they don't interfer with the Smart Home Monitor commands.**<br><br>"
        }
        SHPARAM.unique().each {result += it + "<br>" }
    }
    else result +="none<br>"
    result += "<br><b>LIST_OF_SHCMD</b><br><br>routine<br>security<br>smart home<br>SHM<br>smart home monitor<br>mode<br>" 
    result += "<br><b>LIST_OF_MACROS</b><br><br>"
    if (childApps.size()){
        def MACROS=[]
        childApps.each { MACROS << it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()}       
        duplicates = MACROS.findAll{MACROS.count(it)>1}.unique()
        if (duplicates.size()){ 
            result += "<b>**NOTICE:</b>The following duplicate(s) are only listed once below in LIST_OF_MACROS:<br><br>"
            duplicates.each{result +="* " + it +" *<br><br>"}
            result += "Be sure to have unique names for each macro and only use each name once within the parent app.**<br><br>" 	
        }
        MACROS.unique().each {result += it + "<br>" }
    }
    else result +="none<br>"
	result += "<br><hr><br><i><b>URL of this setup page:</b></i><br><br>${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}<br><br><hr></div>"
	displayData(result)
}
def fillTypeList(){
	return ["reports","report","switches","switch","dimmers","dimmer","colored lights","color","colors","speakers","speaker","water sensor","water sensors","water","lock","locks","thermostats","thermostat",
    	"temperature sensors","modes","routines","smart home monitor","SHM","security","temperature","door","doors", "humidity", "humidity sensor", "humidity sensors","presence", "presence sensors", "motion", 
        "motion sensor", "motion sensors", "door sensor", "door sensors", "window sensor", "window sensors", "open close sensors","colored light", "events","macro", "macros", "group", "groups", "voice reports", 
        "voice report", "device group", "device groups","control macro", "control macros","control", "controls","macro group","macro groups","device macros","device macro","device group macro","device group macros",
        "core","core trigger","core macro","core macros","core triggers","sensor", "sensors","shades", "window shades","shade", "window shade","acceleration", "acceleration sensor", "acceleration sensors", "alias","aliases"] 
}
def getURLs(){
	def mName = params.mName, url = formatURL("${getApiServerUrl()}/api/smartapps/installations/${app.id}/m?Macro=${mName}&access_token=${state.accessToken}")
    def result = "<div style='padding:10px'>Copy the URL below and paste it to your control application.</div><div style='padding:10px'>Click '<' above to return to the Ask Alexa SmartApp.</div>"
	result += "<div style='padding:10px;'><b>Macro REST URL:</b></div>"
	result += "<textarea rows='5' style='width: 99%'>${url}</textarea>"
	result += "<hr>"
    displayData(result)
}
//Version/Copyright/Information/Help-----------------------------------------------------------
private textAppName() { return "Ask Alexa" }	
private textVersion() {
    def version = "SmartApp Version: 2.1.9a (11/20/2016)", lambdaVersion = state.lambdaCode ? "\n" + state.lambdaCode : ""
    return "${version}${lambdaVersion}"
}
private versionInt(){ return 219 }
private LambdaReq() { return 122 }
private versionLong(){ return "2.1.9a" }
private textCopyright() {return "Copyright © 2016 Michael Struck" }
private textLicense() {
	def text = "Licensed under the Apache License, Version 2.0 (the 'License'); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License."
}
private textHelp() { 
	def text = "This SmartApp provides an interface to control and query the SmartThings environment via the Amazon Echo ('Alexa'). For more information, go to http://thingsthataresmart.wiki/index.php?title=Ask_Alexa."
}
def getCheatDisplayList(type){
    def count = state.aliasList ? state.aliasList.size() : 0, result=""
   	for (int i=0; i < count; i++) {
    	def deviceName = state.aliasList.aliasType[i]
		if (deviceName==type) result += state.aliasList.aliasName[i] + " (Alias Name) = "+ state.aliasList.aliasDevice[i] + " (Real Device)<br>"
    }
    return result
}
private cheat(){
	log.info "Set up web page located at : ${getApiServerUrl()}/api/smartapps/installations/${app.id}/cheat?access_token=${state.accessToken}"
    def result ="<div style='padding:10px'><h2><i><b>Ask Alexa Device/Command 'Cheat Sheet'</b></i></h2>To expand on this sheet, please see the <a href='http://thingsthataresmart.wiki/index.php?title=Ask_Alexa#Ask_Alexa_Usage' target='_blank'>Things That Are Smart Wiki</a><br>"
	result += "Most commands will begin with 'Alexa, ask ${invocationName}' or 'Alexa, tell ${invocationName}'and then the command and device. For example:<br>"
    result +="<i>Alexa, tell ${invocationName} to Open {DoorName}</i>'<br><i>'Alexa, ask ${invocationName} the {SwitchName} status'</i><br><br><hr>"
    if (switchesSel()) { result += "<h2>Switches (Valid Commands: <b>On, Off, Toggle, Status</b>)</h2>"; switches.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("switch")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("switch") +"<br>" }
    if (dimmersSel()) { result += "<h2><u>Dimmers (Valid Commands: <b>On, Off, Toggle, Status Level {number}, low, medium, high, up, down, increase, decrease</b>)</u></h2>"; dimmers.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("level")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("level") +"<br>" }
    if (cLightsSel()) { result += "<h2><u>Colored Lights (Valid Commands: <b>On, Off, Toggle, Level {number}, color {color name} low, medium, high, up, down, increase, decrease</b>)</u></h2>"; cLights.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("color")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("color") +"<br>" }
    if (doorsSel()) { result += "<h2><u>Doors (Valid Commands: <b>Open, Close, Status</b>)</u></h2>"; doors.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("door")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("door") +"<br>" }
    if (locksSel()) { result += "<h2><u>Locks (Valid Commands: <b>Lock, Unlock, Status</b>)</u></h2>"; locks.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("lock")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("lock") +"<br>" }
    if (ocSensorsSel()) { result += "<h2><u>Open/Close Sensors (Valid Command: <b>Status</b>)</u></h2>"; ocSensors.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("contact")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("contact") +"<br>" }
    if (shadesSel()) { result += "<h2><u>Doors (Valid Commands: <b>Open, Close, Status</b>)</u></h2>"; shades.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("shade")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("shade") +"<br>" }
    if (tstatsSel()) { result += "<h2><u>Thermostats (Valid Commands: <b>Open, Close, Status"
    	if (stelproCMD) result += ", Eco, Comfort"
        if (nestCMD || ecobeeCMD) result += ", Home, Away"
        if (nestCMD && nestMGRCMD) result += ", Report"
        if (ecobeeCMD) result += ", Sleep, Resume Program"
        if (ecobeeCMD && MyEcobeeCMD) result += ", Get Tips {level}, Play Tips, Erase Tips"
     	result +="</b>)</u></h2>"
        tstats.each{ result += it.label +"<br>" } 
    }
    if (getCheatDisplayList("thermostat")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("thermostat") +"<br>" }
    if (tempsSel()) { result += "<h2><u>Temperature Sensors (Valid Commands: <b>Status</b>)</u></h2>"; temps.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("temperature")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("temperature") +"<br>" }
    if (humidSel()) { result += "<h2><u>Humidity Sensors (Valid Commands: <b>Status</b>)</u></h2>"; humid.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("humidity")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("humidity") +"<br>" }
    if (speakersSel()) { result += "<h2><u>Speakers (Valid Commands: <b>Play, Mute, Next Track, Previous Track, volume {level}, increase, decrease, up, down, raise, lower</b>)</u></h2>"; if (speakers) { speakers.each{ result += it.label +"<br>" } } }
    if (getCheatDisplayList("music")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("music") +"<br>" }
    if (speakersSel() && sonosCMD && sonosMemoryCount){
    	result += "<u>Memory Slots</u><br>"
        def memCount = sonosMemoryCount as int
        for (int i=1; i<memCount+1; i++){
            result += settings."sonosSlot${i}Name" ? settings."sonosSlot${i}Name"+"<br>" : ""
        }
    }
    if (waterSel()) { result += "<h2><u>Water Sensors (Valid Command: <b>Status</b>)</u></h2>"; water.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("water")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("water") +"<br>" }
    if (presenceSel()) { result += "<h2><u>Presence Sensors (Valid Command: <b>Status</b>)</u></h2>"; presence.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("presence")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("presence") +"<br>" }
    if (accelerationSel()) { result += "<h2><u>Acceleration Sensors (Valid Command: <b>Status</b>)</u></h2>"; acceleration.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("acceleration")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("acceleration") +"<br>" }
    if (motionSel()) { result += "<h2><u>Motion Sensors (Valid Command: <b>Status</b>)</u></h2>"; motion.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("motion")) { result += "<u>Aliases</u><br>"; result += getCheatDisplayList("motion") +"<br>" }
    if (listModes) { result += "<h2><u>Modes (Valid Command: <b>Change/Status</b>)</u></h2>"; listModes.each{ result += it +"<br>" } }
	if (listSHM) { result += "<h2><u>Smart Home Monitor (Valid Command: <b>Change/Status</b>)</u></h2>"; listSHM.each{ result += it +"<br>" } }
    if (listRoutines) { result += "<h2><u>SmartThings Routines (Valid Command: <b>Run {Routine}</b>)</u></h2>"; listRoutines.each{ result += it +"<br>" } }
    if (childApps.size()) { result += "<h2><u>Ask Alexa Macros (Valid Command: <b>Run {Macro}</b>)</u></h2>"; childApps.each { result += it.label + "<br>"} }      
    displayData(result)
}