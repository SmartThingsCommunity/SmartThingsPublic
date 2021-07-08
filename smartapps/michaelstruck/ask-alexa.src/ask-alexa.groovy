/**
 *  Ask Alexa 
 *
 *  Version 2.2.9e - 7/13/17 Copyright © 2017 Michael Struck
 *  Special thanks for Keith DeLong for overall code and assistance; jhamstead for Ecobee climate modes, Yves Racine for My Ecobee thermostat tips
 * 
 *  Version information prior to 2.2.9 listed here: https://github.com/MichaelStruck/SmartThingsPublic/blob/master/smartapps/michaelstruck/ask-alexa.src/Ask%20Alexa%20Version%20History.md
 *
 *  Version 2.2.9e (7/13/17) Added additional advanced features to the WebCoRE macro, begin adding code to allow external items to send to the message queue, updated the brief reply option.
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
    //Change line below to 'false' to allow for multi app install (Advanced...see instructions)
    	singleInstance: true,
    //-----------------------------------------------------------
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
    	page name:"pageMQGUI"
        	page name:"pageMsgDelete"    
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
        page name:"pageExtensions"
        	page name:"pageMacros"
            page name:"pageMsgQue"
            	page name:"pagePriQueue"
                page name:"pageMQURL"
            page name:"pageWeather"
            page name:"pageVoiceRPT"
            page name:"pageSchdr"
            	page name: "pageSchdList"
        page name:"pageSettings"
            page name:"pageReset"
            	page name:"pageConfirmation"
                page name:"pageContCommands"
            page name:"pageDefaultValue"
            page name:"pageCustomColor"
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
            page name:"pageMQ"
            page name:"pageHTTP"
}
def pageMain() { if (!parent) mainPageParent() else mainPageChild() }
def mainPageParent() {
    dynamicPage(name: "mainPageParent", install: true, uninstall: false) {
        def duplicates = deviceList.name.findAll{deviceList.name.count(it)>1}.unique()
        def aliasDups = deviceAlias && state.aliasList ? deviceList.name.intersect(state.aliasList.aliasNameLC) : null
        def macChildren = getAskAlexa(), macroCount = macChildren.size(), mqChildren = getAAMQ(), mqCount = mqChildren.size()
        if (duplicates || findNullDevices() || (aliasDups && deviceAlias)){
        	section ("**WARNING**"){
            	if (duplicates) paragraph "You have the following device(s) used multiple times within Ask Alexa:\n\n${getList(duplicates)}\n\nA device should be uniquely named and appear only once in the categories below.", image: imgURL() + "caution.png"
                if (aliasDups && deviceAlias) paragraph "The following alias(es) conflict with a device name already set up:\n\n${getList(aliasDups)}\n\nAliases should be uniquely named and appear only once within the Ask Alexa SmartApp.", image: imgURL() + "caution.png"
                if (findNullDevices()) paragraph findNullDevices(), image: imgURL() + "caution.png"  
            }
        }
        if (msgQueueGUI && mqCounts(msgQueueGUI)) section ("Message Queues"){ href "pageMQGUI", title: "Message Queue", description: mqCounts(msgQueueGUI) + " - Tap to read", state: "complete", image:imgURL() + "mailbox.png" }            
        section("Items to interface to Alexa") {
            href "pageSwitches", title: "Switches/Dimmers/Colored Lights", description:getDesc(switchesSel() || dimmersSel() || cLightsSel() || cLightsKSel()), state: switchesSel() || dimmersSel() || cLightsSel() || cLightsKSel() ? "complete" : null, image:imgURL() + "power.png"
            href "pageDoors", title: "Doors/Windows/Locks", description: getDesc(doorsSel() || locksSel() || ocSensorsSel() || shadesSel()), state: doorsSel() || locksSel() || ocSensorsSel() || shadesSel() ? "complete" : null, image: imgURL() + "lock.png"
            href "pageTemps", title: "Thermostats/Temperature/Humidity", description:getDesc(tstatsSel() || tempsSel() || humidSel()), state: tstatsSel() || tempsSel() || humidSel() ? "complete" : null, image: imgURL() + "temp.png"
            href "pageSpeakers", title: "Connected Speakers", description: getDesc(speakersSel()), state: speakersSel() ? "complete" : null, image:imgURL() + "speaker.png"     
            href "pageSensors", title: "Other Sensors", description:getDesc(waterSel() || presenceSel() || motionSel() || accelerationSel()), state: waterSel() || presenceSel() || motionSel() || accelerationSel() ? "complete" : null, image: imgURL() + "sensor.png"
            href "pageHomeControl", title: "Modes/SHM/Routines", description:getDesc(listModes || listRoutines || listSHM), state: (listModes|| listRoutines|| listSHM ? "complete" : null), image: imgURL() + "modes.png"
            if (deviceAlias && mapDevices(true)) href "pageAliasMain", title: "Device Aliases", description:getDesc(state.aliasList), state: (state.aliasList ?"complete":null), image: imgURL() + "alias.png"     
        }
        section("Ask Alexa Extensions") {href "pageExtensions", title: "Ask Alexa Extensions", description: "Tap to add/edit Ask Alexa extensions", state: (macroCount || mqCount ? "complete" : null), image: imgURL() + "exadd.png" }
        section("Options") {
			href "pageSettings", title: "Settings", description: "Tap to configure app settings, get setup information or to reset the access token", image: imgURL() + "settings.png"
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get version information, license, instructions or to remove the application", image: imgURL() + "info.png"
        }
	}
}
def pageExtensions(){
    dynamicPage(name: "pageExtensions", install: false, uninstall: false) {
    	def macroCount = getAskAlexa().size(), mqCount = getAAMQ().size(), wrCount =getWR().size, vrCount=getVR().size, schCount =getSCHD().size()
        section { paragraph "Ask Alexa Extensions", image: imgURL() + "exadd.png" }
        section("Installed extensions"){
        	def duplicates =getAskAlexa().label.findAll{getAskAlexa().label.count(it)>1}.unique()
            duplicates +=getWR().label.findAll{getWR().label.count(it)>1}.unique()
            duplicates +=getVR().label.findAll{getVR().label.count(it)>1}.unique()
            duplicates +=getAAMQ().label.findAll{getAAMQ().label.count(it)>1}.unique()
            duplicates +=getSCHD().label.findAll{getSCHD().lable.count(it)>1}.unique()
            if (duplicates) paragraph "You have two or more extensions that have the same name. Please ensure each extension has a unique name and also does not conflict with device or other extension names.", image: imgURL() + "caution.png" 
        	href "pageMacros", title: "Macros", description: macroDesc(macroCount), state: (macroCount ? "complete" : null), image: imgURL() + "speak.png" 
     		href "pageMsgQue", title: "Message Queues", description: mqDesc(mqCount), state: "complete", image: imgURL() + "mailbox.png"
            href "pageSchdr", title: "Schedules",  description: schDesc(schCount), state: (schCount ? "complete" : null), image: imgURL() + "schedule.png"
            href "pageVoiceRPT", title: "Voice Reports",  description:voiceDesc(vrCount), state: (vrCount ? "complete" : null), image: imgURL() + "voice.png"
            href "pageWeather", title: "Weather Reports",  description:weathDesc(wrCount), state: (wrCount ? "complete" : null), image: imgURL() + "weather.png"
		}
    }
}
def pageMQGUI(){
    dynamicPage(name: "pageMQGUI", install: false, uninstall: false) {
        def msgRpt = ""
        section { paragraph "Message Queues", image: imgURL() + "mailbox.png"}
        if(msgQueueGUI.contains("Primary Message Queue") && state.msgQueue.size()){
            state.msgQueue.sort({it.date})
            if(msgQueueOrder) state.msgQueue.reverse(msgQueueOrder as int? true : false)
            state.msgQueue.each{
                def msgData= timeDate(it.date)
                msgRpt += "● ${msgData.msgDay} at ${msgData.msgTime} From: '${it.appName}' : '${it.msg}'\n"
            }
            section ("Primary Message Queue") { paragraph msgRpt }
        }
        msgQueueGUI.each{qID->
            def qNameRun = getAAMQ().find{it.id == qID}, msgCount = 0
            if (qNameRun) {
                msgCount += qNameRun.qSize()
                if (msgCount) {
                    msgRpt = qNameRun.MQGUI()
                    section ("${qNameRun.label} message queue"){ paragraph msgRpt }
                }
            }
        }
        section ("Options"){ 
            href "pageMsgQue", title: "Tap To Go To Messaging Queue Options", description: none
            href "pageMsgDelete", title: "Tap To Delete All Messages In The Message Queues Above", description: none
        }
	}
}
def pageMsgDelete(){
    dynamicPage(name: "pageMsgDelete", install: false, uninstall: false) {
    if(msgQueueGUI.contains("Primary Message Queue")) qDelete()
    childQDelete(msgQueueGUI) 
    section { paragraph "All Message Successfully Deleted", image: imgURL() + "check.png" }
        section (" "){
            href "pageMsgQue", title: "Tap To Go To Message Queue Options", description: none 
            href "mainPageParent", title: "Tap Here To Return To The Main Menu", description:none
        }
    }
}
def pageSchdr() {
    dynamicPage(name: "pageSchdr", install: false, uninstall: false) {
        section{ paragraph "Schedules", image: imgURL() + "schedule.png" }
        def children = getSCHD(), schCount=children.size(), duplicates = children.label.findAll{children.label.count(it)>1}.unique(), aaSCVer=""
        if (schCount){
        	children.each { aaSCVer=it.versionInt()} 
            section ("Schedule option"){ 
            	href "pageSchdList", title: "Tap For Schedule Status List", description: ""
                input "schDeleteTime", "number", title: "Minutes After Schedule Expires/Delete Command To Override", range:"1..10", description: "If blank, will default to 2 minutes", required: false, defaultValue: 2
        	}
        }
        if (duplicates || (schCount && (aaSCVer < schReq()))){
        	section ("Warning"){
            	if (duplicates) paragraph "You have two or more schedules with the same name. Please ensure each schedule has a unique name and also does not conflict with device or other extension names as well. ", image: imgURL() + "caution.png" 
        		if (schCount && (aaSCVer < schReq())) paragraph "You are using an outdated version of the schedules extension. Please update the software and try again.", image: imgURL() + "caution.png" 
        	}
        }
		if (schCount) section(schCount==1 ? "One schedule configured" : schCount + " schedules configured" ){}
        section(" "){
        	app(name: "childSCHD", appName: "Ask Alexa Schedule", namespace: "MichaelStruck", title: "Create A New Schedule...", description: "Tap to create a new schedule", multiple: true, image: imgURL() + "add.png")
        }
	}
}
def pageSchdList() {
    dynamicPage(name: "pageSchdList", install: false, uninstall: false) {
		section{ paragraph "Schedule List", image: imgURL() + "schedule.png" }
        section(" "){
        	getSCHD().each{
                def status = it.getStatus(), result, imageFile= status =="On" ? imgURL() + "on.png" : imgURL() + "off.png"
				if (status =~/Expired|Invalid|Incomplete/) imageFile=imgURL() +"warning.png"
                if (status==~/On|Off/) result = "${it.label}: Runs ${it.getShortDesc()}"
                else if (status =="Expired") result ="${it.label}: ${status} ${it.getShortDesc()}"
                else result ="${it.label}: ${status}"
                paragraph result, image: imageFile  
			}
        }
        section ("Help"){
        	paragraph "To change the on/off status of a schedule, either edit the schedule on the previous screen, or simple say: \"Alexa, tell ${invocationName} to turn {on/off} the {schedule name}\".\n\nIf you have "+
            	"an expired or invalid schedule you will need to edit that schedule via the mobile app.", image: imgURL()+"info.png"
        }
    }
}
def pageSwitches(){
    dynamicPage(name: "pageSwitches", install: false, uninstall: false) {
        section { paragraph "Switches/Dimmers/Colored lights", image: imgURL() + "power.png"} 
        section("Choose the devices to interface", hideWhenEmpty: true) {
            input "switches", "capability.switch", title: "Choose Switches (On/Off/Toggle/Status)", multiple: true, required: false
            input "dimmers", "capability.switchLevel", title: "Choose Dimmers (On/Off/Toggle/Level/Status)", multiple: true, required: false
            input "cLights", "capability.colorControl", title: "Choose Colored Lights (On/Off/Toggle/Level/Color/Status)", multiple: true, required: false
            input "cLightsK", "capability.colorTemperature", title: "Choose Temperature (Kelvin) Lights (On/Off/Toggle/Level/Temperature/Status)", multiple: true, required: false, submitOnChange: true
        }
        if (deviceAlias){
            section("Devices that can have aliases", hideWhenEmpty: true) {
                input "switchesAlias", "capability.switch", title: "Choose Switches", multiple: true, required: false
                input "dimmersAlias", "capability.switchLevel", title: "Choose Dimmers", multiple: true, required: false
                input "cLightsAlias", "capability.colorControl", title: "Choose Colored Lights", multiple: true, required: false
                input "cLightsKAlias", "capability.colorTemperature", title: "Choose Temperature (Kelvin) Lights", multiple: true, required: false, submitOnChange: true
            }
        }
        if (cLightsKSel()){
        	section ("Notes on Temperature (Kelvin) Lights"){
            	paragraph "The following color temperatures are valid:\nSoft White, Warm White, Cool White, Daylight White", image: imgURL() + "info.png"
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
        if ((doorsSel() || locksSel())){
            section("Security"){
                if (doorsSel()){
                	if (pwNeeded) input "doorPW", "bool", title: "Require PIN For Door Actions", defaultValue: false
                	if (!doorCloseDisable) input "doorOpenDisable", "bool", title: "Disable Door 'Open' Command", defaultValue: false, submitOnChange: true 
                	if (!doorOpenDisable) input "doorCloseDisable", "bool", title: "Disable Door 'Close' Command", defaultValue: false, submitOnChange: true 
                }
                if (locksSel()){
					if (pwNeeded) input "lockPW", "bool", title: "Require PIN For Lock Actions", defaultValue: false
					if (!lockUnLockDisable) input "lockLockDisable", "bool", title: "Disable Lock 'Lock' Command", defaultValue: false, submitOnChange: true 
					if (!lockLockDisable) input "lockUnLockDisable", "bool", title: "Disable Lock 'Unlock' Command", defaultValue: false, submitOnChange: true 
            	}
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
            input "presence", "capability.presenceSensor", title: vPresenceCMD ? "Choose Presence Sensors (Status/Home/Away)" : "Choose Presence Sensors (Status)", multiple: true, required: false
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
			input "listSHM", "enum", title: "Choose SHM Statuses (Change/Status)", options: ["Armed (Away)","Armed (Home)","Disarmed"], multiple: true, required: false
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
        	section(" "){ href "pageAliasAddFinal", title: "Add Device Alias", description: "Tap to create the device alias", image: imgURL() + "add.png" }
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
			def devType = [Switch:"switch", Dimmer:"level", "Colored Light":"color", "Temperature (Kelvin) Light": "kTemp", "Door Control":"door", "Window Shade":"shade", "Open/Close Sensor":"contact", "Temperature Sensor":"temperature", Lock:"lock",
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
def pageVoiceRPT() {
    dynamicPage(name: "pageVoiceRPT", install: false, uninstall: false) {
        section{ paragraph "Voice Reports", image: imgURL() + "voice.png" }
        section ("Voice reports global options"){
        	input "advReportOutput", "bool", title: "Advanced Voice Report Filters", defaultValue: false, submitOnChange: true
        }
        def children = getVR(), vrCount = children.size(), duplicates = children.label.findAll{children.label.count(it)>1}.unique(), aaVRVer
        if (vrCount) children.each { aaVRVer=it.versionInt()}   
		if (duplicates || (vrCount && (aaVRVer < vrReq()))){
        	section ("Warning"){
        		if (duplicates) paragraph "You have two or more voice reports with the same name. Please ensure each report has a unique name and also does not conflict with device names or other extensions.", image: imgURL() + "caution.png"
        		if (vrCount && (aaVRVer < vrReq())) paragraph "You are using an outdated version of the voice report extension. Please update the software and try again.", image: imgURL() + "caution.png" 
        	}
        }
        if (vrCount) section(vrCount==1 ? "One voice report configured" : vrCount + " voice reports configured" ){} 
        section(" "){
        	app(name: "childVR", appName: "Ask Alexa Voice Report", namespace: "MichaelStruck", title: "Create A New Voice Report...", description: "Tap to create a new report", multiple: true, image: imgURL() + "add.png")
        }
	}
}
def pageWeather() {
    dynamicPage(name: "pageWeather", install: false, uninstall: false) {
        section{ paragraph "Weather Reports", image: imgURL() + "weather.png" }
        def children = getWR(), wrCount = children.size(), duplicates = children.label.findAll{children.label.count(it)>1}.unique(), aaWRVer
        if (wrCount) children.each { aaWRVer=it.versionInt()}
        log.debug wrCount && (aaWRVer < wrReq())
        if (duplicates || (wrCount && (aaWRVer < wrReq()))){
        	section ("Warning"){
			if (duplicates) paragraph "You have two or more weather reports with the same name. Please ensure each report has a unique name and also does not conflict with device names or other extensions.", image: imgURL() + "caution.png"
        		if (wrCount && (aaWRVer < wrReq())) paragraph "You are using an outdated version of the weather report extension. Please update the software and try again.", image: imgURL() + "caution.png" 
        	}
        }
        if (wrCount) section(wrCount==1 ? "One weather report configured" : wrCount + " weather reports configured" ){}    
        section(" "){
        	app(name: "childWR", appName: "Ask Alexa Weather Report", namespace: "MichaelStruck", title: "Create A New Weather Report...", description: "Tap to create a new report", multiple: true, image: imgURL() + "add.png")
        }
	}
}
def pageMacros() {
    dynamicPage(name: "pageMacros", install: false, uninstall: false) {
        section{ paragraph "Macros", image: imgURL() + "speak.png" }
        section("Global macro options"){
			input "showURLs", "bool", title: "Display REST URL For Control and Extension Group Macros", defaultValue: false, submitOnChange: true
        }
        def children = getAskAlexa(), macroCount = children.size()
        if (macroCount) section(macroCount==1 ? "One macro configured" : macroCount + " macros configured" ){}
        def duplicates = children.label.findAll{children.label.count(it)>1}.unique()
        if (duplicates){
        	section { paragraph "You have two or more macros with the same name. Please ensure each macro has a unique name and also does not conflict with device names or other extensions.", image: imgURL() + "caution.png" }
        }
        section(" "){
        	app(name: "childMacros", appName: "Ask Alexa", namespace: "MichaelStruck", title: "Create A New Macro...", description: "Tap to create a new macro", multiple: true, image: imgURL() + "add.png")
        }
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
        section { paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/AskAlexa@2x.png" }
        section ("Version numbers", hideable: true, hidden: true) { paragraph "${textVersion()}" } 
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
        section("Tap below to remove the application and all extensions"){}
	}
}
def pageSettings(){
    dynamicPage(name: "pageSettings", uninstall: false){
        section { paragraph "Settings", image: imgURL() + "settings.png" }
        section ("Additional voice settings"){ 
        	input "briefReply", "bool", title: "Give 'Brief' Device Action Reply", defaultValue: false, submitOnChange: true            
            if (briefReply) input "briefReplyTxt", "enum", title: "Brief Reply", options: ["No reply spoken", "Ok", "Done", "User-defined"], required:false, multiple:false, defaultValue: "Ok", submitOnChange: true
            if (briefReply && briefReplyTxt=="User-defined") input "briefReplyCustom", "text", title:"User-defined Brief Reply", description:"Enter your brief reply", required: false
            input "flash", "bool", title: "Enable Flash Briefing", defaultValue: false, submitOnChange: true            
            if (flash) input "flashRPT", "enum", title: "Choose Flash Briefing Output", options: getMacroList("flash",""), required: false, multiple: false
            input "otherStatus", "bool", title: "Speak Additional Device Status Attributes", defaultValue: false
            input "healthWarn", "bool", title: "Speak Device Health When Offline", defaultValue: false
            input "batteryWarn", "bool", title: "Speak Battery Level When Below Threshold", defaultValue: false, submitOnChange: true
            if (batteryWarn) input "batteryThres", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: battOptions()
        	input "eventCt", "enum", title: "Default Number Of Past Events to Report", options: optionCount(1,9), required: false, defaultValue: 1
            href "pageContCommands", title: "Personalization", description: "Tap to personalize your voice experience", image: imgURL() + "personality.png"
        }
        section ("Other values / variables"){
        	href "pageDefaultValue", title: "Default Command Values (Dimmers, Volume, etc.)", description: "", state: "complete"
            if (speakersSel() || tstatsSel()) href "pageLimitValue", title: "Device Minimum/Maximum Values", description: "", state: "complete"
        	if (!state.accessToken) OAuthToken()
            if (!state.accessToken) paragraph "**You must enable OAuth via the IDE to setup this app**"
            else href url:"${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}", style:"embedded", required:false, title:"Setup Variables", description: "For Amazon developer sites", image: imgURL() + "amazon.png"
        	href "pageGlobalVariables", title: "Text Field Variables", description: none, state: getGlobeVarState() ? "complete" : null
            if (cLightsSel()) href "pageCustomColor", title: "Custom Color Setup", description: customName && customHue && customSat ? customName +" (Hue: "+customHue+", Saturation: "+ customSat+")":"Tap to enter custom color name and values", state: (customName && customHue && customSat ? "complete" : null), image: imgURL() + "colors.png"
        }
        section("Security"){
            href "pageConfirmation", title: "Revoke/Reset Access Token", description: "Tap to confirm this action", image: imgURL() + "warning.png"
            input "pwNeeded", "bool", title: "Password (PIN) Option Enabled", defaultValue: false, submitOnChange: true
            if (pwNeeded) input "password", "num", title: "Numeric Password (PIN)", description: "Enter a short numeric PIN (i.e. 1234)", required: false
		}
        section ("Advanced") {
            href "pageCustomDevices", title: "Device Specific Commands", description: none, state: (nestCMD || stelproCMD || sonosCMD || ecobeeCMD ||vPresenceCMD ? "complete" : null)
            input "deviceAlias", "bool", title: "Allow Device Aliases", defaultValue: false
            label title:"Rename App (For Multi Room Setup)", required:false, defaultValue: "Ask Alexa"
        }
    }
}
def pagePriQueue(){
    dynamicPage(name: "pagePriQueue", uninstall: false){
    	def children = getAAMQ(), mqCount = children.size()
        section{ paragraph "Primary Message Queue", image: imgURL() + "mailbox.png" }
        section ("Primary message queue options"){
            input "msgQueueOrder", "enum", title: "Message Play Back Order (Alexa)", options:[0:"Oldest to newest", 1:"Newest to oldest"], defaultValue: 0
            input "msgQueueDateSuppress", "bool", title: "Remove Time/Date From Message Review", defaultValue: false
		}
        section ("Message notification - Alexa", hideable: true, hidden: true){
			input "msgQueueNotifyAlexa", "bool", title: "Alexa Notifications (Audio and Visual)", defaultValue: false
            paragraph "This function is not yet available - Coming soon!"            
        }
        section ("Message notification - audio", hideable: true, hidden: !(mqSpeaker||mqSynth)){
        	input "mqSpeaker", "capability.musicPlayer", title: "Choose Speakers", multiple: true, required: false, submitOnChange: true
            if (mqSpeaker) input "mqVolume", "number", title: "Speaker Volume", description: "0-100%", range:"0..100", required: false
            input "mqSynth", "capability.speechSynthesis", title: "Choose Voice Synthesis Devices", multiple: true, required: false, hideWhenEmpty: true
            if (mqSpeaker) input "mqAlertType", "enum", title:"Notification Type...",options:[0: "Verbal Notification and Message", 1: "Verbal Notification Only", 2: "Message Only", 3:"Notification Sound Effect"], defaultValue:0 , submitOnChange: true
			if (mqSpeaker && mqAlertType != "3") input "mqAppendSound", "bool", title: "Prepend Sound To Verbal Notification", defaultValue: false, submitOnChange: true
            if (mqSpeaker && (mqAlertType == "3" || mqAppendSound)) input "mqAlertSound", "enum", title: "Sound Effect", required: mqAlertType == "3" ? true : false, options: soundFXList(), submitOnChange: true
            if (mqSpeaker && (mqAlertType == "3" || mqAppendSound) && mqAlertSound=="custom") input "mqAlertCustom", "text", title:"URL/Location Of Custom Sound (Less Than 10 Seconds)...", required: false
            if (mqSpeaker || mqSynth) input "restrictAudio", "bool", title: "Apply Restrictions To Audio Notification", defaultValue: false, submitOnChange: true
		}
        section ("Message Notification - visual", hideable: true, hidden:!(msgQueueNotifyLightsOn || msgQueueNotifycLightsOn)){
            input "msgQueueNotifyLightsOn", "capability.switch", title: "Turn On Lights When Messages Present", required:false, multiple:true, submitOnChange: true
            input "msgQueueNotifycLightsOn", "capability.colorControl", title: "Turn On/Set Colored Lights When Messages Present", required:false, multiple:true, submitOnChange: true
            if (msgQueueNotifycLightsOn) {
            	input "msgQueueNotifyColor", "enum", title: "Set Color of Message Notification", options: STColors().name, multiple:false, required:false
            	input "msgQueueNotifyLevel", "number", title: "Set Level of Message Notification", defaultValue:50, range:"1..100", required:false
            }
            if (msgQueueNotifyLightsOn || msgQueueNotifycLightsOn) {
            	input "msgQueueNotifyLightsOff", "bool", title: "Turn Off Lights When Message Queue Empty", defaultValue: false
				input "restrictVisual", "bool", title: "Apply Restrictions To Visual Notification", defaultValue: false, submitOnChange: true
            }
		}
        section ("Message notification - mobile", hideable: true, hidden:!(mqContacts||mqSMS||mqPush||mqFeed)){
        	input ("mqContacts", "contact", title: "Send Notifications To...", required: false, submitOnChange: true) {
				input "mqSMS", "phone", title: "Send SMS Message To (Phone Number)...", required: false, submitOnChange: true
				input "mqPush", "bool", title: "Send Push Message", defaultValue: false, submitOnChange: true
            }
        	input "mqFeed", "bool", title: "Post To Notification Feed", defaultValue: false, submitOnChange: true
            if (mqFeed || mqSMS || mqPush || mqContacts) input "restrictMobile", "bool", title: "Apply Restrictions To Mobile Notification", defaultValue: false, submitOnChange: true
        }
        if (restrictMobile || restrictVisual || restrictAudio){
            section("Message Queue Restrictions", hideable: true, hidden: !(runDay || timeStart || timeEnd || runMode || runPeople)) {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: imgURL() + "calendar.png"
				href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: imgURL() + "clock.png"
				input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: imgURL() + "modes.png"
                input "runPeople", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange: true, image: imgURL() + "people.png"
				if (runPeople && runPeople.size()>1) input "runPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
            }
        }
        section ("REST URL for this message queue", hideable: true, hidden:true){
        	href "pageMQURL", title:"Tap To Send REST URL For This Message Queue To Live Logging", description: none, image:imgURL()+"info.png"
        }
    }
}
def pageMQURL(){
    dynamicPage(name: "pageMQURL", install: false, uninstall: false) {
    	section{
        	paragraph "Please check your Live Logging to copy this URL to your messaging application", image: imgURL()+"info.png"
         	paragraph "${getExtAddr("Primary Message Queue")}"
        	log.info "Message Queue URL: " + getExtAddr("Primary Message Queue")
		}
	}
}   
def pageMsgQue() {
    dynamicPage(name: "pageMsgQue", install: false, uninstall: false) {
        section{ paragraph "Message Queues", image: imgURL() + "mailbox.png" }
        section ("Global message queue options"){ 
        	input "msgQueueGUI", "enum", title: "Message Queues Displayed On Main Menu (When Messages Are Present)" , options: getMQListID(true), multiple:true, required:false 
            input "msgQueueDelete", "enum", title: "Allow External SmartApps To Delete Messages From These Queues", options: getMQListID(true), multiple:true, required:false
            input "msgQueueNotify", "enum", title: "Append Alexa Output With Message Notification From These Queues", options: getMQListID(true), multiple:true, required:false
            if (getMQListID(false).size()) input "msgQueueForward", "enum", title: "Forward Messages From Primary Message Queue To", options: getMQListID(false), multiple:true, required:false, submitOnChange: true  
        }
        def children = getAAMQ(), duplicates = children.label.findAll{children.label.count(it)>1}.unique(), aaMQVer=""
        if (children.size()) children.each { aaMQVer=it.versionInt()}
        def mqCount = children.size() ? children.size() + 1 + " messsage queues configured" : "One message queue configured"
        section ("${mqCount}"){ 
        	if (duplicates) paragraph "You have two or more message queues with the same name. Please ensure each queue has a unique name and also does not conflict with device names or other extensions.", image: imgURL() + "caution.png" 
            if (children.size() && (aaMQVer < mqReq())) paragraph "You are using an outdated version of the message queue extension. Please update the software and try again.", image: imgURL() + "caution.png" 
            href "pagePriQueue", title: "Primary Message Queue", description: "", state:"complete"
        }
        section(" "){
        	app(name: "childMQ", appName: "Ask Alexa Message Queue", namespace: "MichaelStruck", title: "Create A New Message Queue...", description: "Tap to create a new message queue", multiple: true, image: imgURL() + "add.png")
        }
	}
}
def pageDefaultValue(){
    dynamicPage(name: "pageDefaultValue", uninstall: false){
		section("Increase / Brighten / Decrease / Dim values\n(When no values are requested)"){
			input "lightAmt", "number", title: "Dimmer/Colored Lights", range:"1..100", defaultValue: 20, required: false
			input "tstatAmt", "number", title: "Thermostat Temperature", range:"1..100",defaultValue: 5, required: false
			input "speakerAmt", "number", title: "Speaker Volume", range:"1..100",defaultValue: 5, required: false
		}
       	section("Low / Medium / High values (For dimmers or colored lights)") {
			input "dimmerLow", "number", title: "\"Low\" Value",range:"1..100", defaultValue: 10, required: false
			input "dimmerMed", "number", title: "\"Medium\" Value", range:"1..100", defaultValue: 50, required: false
			input "dimmerHigh", "number", title: "\"High\" Value", range:"1..100", defaultValue: 100, required: false
		}
        section("Default temperature (Kelvin) values"){
        	input "kSoftWhite", "number", title: "\"Soft White\" Value", range:"2700..6500",defaultValue: 2700, required: false
            input "kWarmWhite", "number", title:"\"Warm White\" Value", range:"2700..6500", defaultValue: 3500, required: false
            input "kCoolWhite" ,"number", title:"\"Cool White\" Value", range:"2700..6500", defaultValue: 4500, required: false
            input "kDayWhite" ,"number", title:"\"Daylight White\" Value", range:"2700..6500", defaultValue: 6500, required: false
        }
    }
}
def pageContCommands(){
	dynamicPage(name: "pageContCommands", uninstall: false){
		section{ paragraph "Personalization", image: imgURL() + "personality.png" }
        section ("Continuation of commands..."){
			input "contError", "bool", title: "After Error", defaultValue: false
            input "contStatus", "bool", title: "After Status/List", defaultValue: false
            input "contAction", "bool", title: "After Action/Event History", defaultValue: false, submitOnChange: true
            if (contAction && briefReply) paragraph "Please note that 'Brief Device Action Reply' is turned on. "+
            	"There will be no prompt for continuation commands, but Alexa will still be active waiting "+
                "for these additional commands with this option enabled.", image: imgURL()+"info.png"
            input "contMacro", "bool", title: "After Macro/Extension Execution", defaultValue: false
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
        section("Other options"){ 
        	input "speakSpeed", "enum", title: "Alexa Speaking Speed", options: ["x-slow":"Extra Slow", "slow":"Slow", "medium":"Default", "fast":"Fast", "x-fast":"Extra Fast"], defaultValue: "medium", required: false
            input "speakPitch", "enum", title: "Alexa Speaking Pitch", options: ["x-low":"Extra Low", "low":"Low", "medium":"Default", "high":"High", "x-high":"Extra High"], defaultValue: "medium", submitOnChange: true, required: false
            if (speakPitch=="medium" || !speakPitch) input "whisperMode", "bool", title: "Enable Whisper Mode", defaultValue: false, submitOnChange: true
            if ((speakPitch=="medium" || !speakPitch)  && whisperMode) {
            	href "timeIntervalInputWhisper",  title: "Whisper Only During These Times...", description: getTimeLabel(timeStartWhisper, timeEndWhisper), state: (timeStartWhisper || timeEndWhisper? "complete":null), image: imgURL() + "clock.png"
            	input "runModeWhisper", "mode", title: "Whisper Only In The Following Modes...", multiple: true, required: false, image: imgURL() + "modes.png"
            }
            input "invocationName", title: "Invocation Name (Only Used For Examples)", defaultValue: "SmartThings", required: false 
        }
    }
}
page(name: "timeIntervalInputWhisper", title: "Whisper Only During These Times...") {
	section {
		input "timeStartWhisper", "time", title: "Starting", required: false
		input "timeEndWhisper", "time", title: "Ending", required: false
	}
}
def pageCustomDevices(){
    dynamicPage(name: "pageCustomDevices", uninstall: false){
		section("Device specific commands"){
            input "ecobeeCMD", "bool", title: "Ecobee Specific Thermostat Modes\n(Home/Away/Sleep/Resume Program)", defaultValue: false, submitOnChange: true
            if (ecobeeCMD) input "MyEcobeeCMD", "bool", title: "MyEcobee Specific Tips\n(Get Tips/Play Tips/Erase Tips)", defaultValue: false             
            input "nestCMD", "bool", title: "Nest-Specific Thermostat Presence Commands (Home/Away)", defaultValue: false, submitOnChange: true
            if (nestCMD) input "nestMGRCMD", "bool", title: "NST Manager Specific Reports (Report)", defaultValue: false
            input "osramCMD", "bool", title: "OSRAM Specific Commands (Loop/Pulse)", defaultValue: false
            input "stelproCMD", "bool", title: "Stelpro Baseboard\nThermostat Modes (Eco/Comfort)", defaultValue:false
            input "sonosCMD", "bool", title: "SONOS Options (Memory Slots)", defaultValue: false, submitOnChange: true
            if (sonosCMD) {
				input "sonosMemoryCount", "enum", title: "Maximum number of SONOS memory slots", options: optionCount(2,10), defaultValue: 2, required: false 
                paragraph "To reset the database of SONOS songs listed in the memory slots, tap the area below. It is recommended you do this ONLY if you are having issues playing the songs in the list. " +
                "The database will be rebuilt from the recently played songs from the speakers upon exiting the SmartApp."
            	href "pageSONOSReset", title: "Reset Song Database", description: "Tap to reset database", image: imgURL() + "warning.png"
			}
            input "vPresenceCMD", "bool", title: "Virtual Presence (Home/Away)", defaultValue: false
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
        if (speakersSel()){ section("Speaker volume limits"){ input "speakerHighLimit", "number", title: "Speaker Maximum Volume", range:"0..100", defaultValue:20, required: false } }
        if (tstatsSel()){
        	section("Thermostat setpoint limits"){
            	input "tstatLowLimit", "number", title: "Thermostat Minimum Value", range:"0..100", defaultValue: 55, required: false
                input "tstatHighLimit", "number", title: "Thermostat Maximum Value", range:"0..100", defaultValue: 85, required: false
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
        section ("Random Responses"){
        	href "pageRandom1", title: "Random Responses 1 (%random1%)", description: getRandDesc(1), state: random1A || random1B|| random1C? "complete" : null
            href "pageRandom2", title: "Random Responses 2 (%random2%)", description: getRandDesc(2), state: random2A || random2B|| random2C? "complete" : null
            href "pageRandom2", title: "Random Responses 3 (%random3%)", description: getRandDesc(3), state: random3A || random3B|| random3C? "complete" : null
        }
        section ("Built in variables"){
        	paragraph "The following variables are built in:\n\n%time% - Time the variable is called\n%day% - Day of the week\n%date% - Full date\n" +
            	"%macro% - Macro/Extension name\n%mtype% - Macro/Extension type\n%dtype% - Device group type\n%dtypes% - Device group type (plural)\n%delay% - Control/WebCoRE macro delay"+
                "\n%age% - Schedules age number\n%xParam% - WebCoRE passed parameter"
        }
        if (getWR().size()){
        	section ("Weather Report"){
            	def list= "The following variables can be used to add weather reporting to any text field:\n\n", itemCount=getWR().size() as int
				getWR().each{ list += "%${it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")}%"; itemCount --
					list += itemCount>1 ? ", " : itemCount==1 ? " and " : ""
    			}
			paragraph list
            }
    	}
    }
}

page(name: "pageRandom1", title: "Random Responses 1"){
	section{
        input "random1A", "text", title: "Random Response", required: false, capitalization: "sentences"
        input "random1B", "text", title: "Random Response", required: false, capitalization: "sentences"
        input "random1C", "text", title: "Random Response", required: false, capitalization: "sentences"
	}
}
page(name: "pageRandom2", title: "Random Responses 2"){
	section{
        input "random2A", "text", title: "Random Response", required: false, capitalization: "sentences"
        input "random2B", "text", title: "Random Response", required: false, capitalization: "sentences"
        input "random2C", "text", title: "Random Response", required: false, capitalization: "sentences"
	}
}
page(name: "pageRandom3", title: "Random Responses 3"){
	section{
        input "random3A", "text", title: "Random Response", required: false, capitalization: "sentences"
        input "random3B", "text", title: "Random Response", required: false, capitalization: "sentences"
        input "random3C", "text", title: "Random Response", required: false, capitalization: "sentences"
	}
}
def pageCustomColor(){
	dynamicPage(name: "pageCustomColor", uninstall: false){
        section{ paragraph "Custom Color Setup", image: imgURL() + "colors.png" }
        section("Custom Color") {
            input "customName", "text", title: "Custom Color Name", required: false
            input "customHue", "number", title: "Custom Color Hue",range:"0..100",required: false
            input "customSat", "number", title: "Custom Color Saturation",range:"0..100", required: false
        }
        section("Notes about custom colors") {
        	paragraph "Remember to update your Amazon Developer Slots to ensure this custom name is available to use."
        }
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
    dynamicPage(name: "mainPageChild", title: "Macro Options", install: true, uninstall: true) {
    	section {
            label title:"Macro Name (Required)", required: true, image: imgURL() + "speak.png"
            href "pageMacroAliases", title: "Macro Aliases", description: macroAliasDesc(), state: macroAliasState()
            input "macroType", "enum", title: "Macro Type...", options: [["Control":"Control (Run/Execute)"],["CoRE":"WebCoRE Trigger (Run/Execute)"],["Group":"Device Group (On/Off/Toggle, Lock/Unlock, etc.)"],["GroupM":"Extension Group (Run/Execute)"]], required: false, multiple: false, submitOnChange:true
            def fullMacroName=[GroupM: "Extension Group",CoRE:"WebCoRE Trigger", Control:"Control", Group:"Device Group"][macroType] ?: macroType
            if (macroType) {
            	href "page${macroType}", title: "${fullMacroName} Settings", description: macroTypeDesc(), state: greyOutMacro()
                if (parent.contMacro) {
                	input "overRideMsg", "bool", title: "Override Continuation Commands (Except Errors)", defaultValue: false, submitOnChange: true
                    if (!overRideMsg) input "suppressCont", "bool", title:"Suppress Continuation Messages (But Still Allow Continuation Commands)", defaultValue: false 
                }
                if (parent.showURLs && macroType==~/Control|GroupM/ &&  macroTypeDesc() !="Status: UNCONFIGURED - Tap to configure macro" && app.label !="Ask Alexa") {
                    href url:"${getApiServerUrl()}/api/smartapps/installations/${parent.app.id}/u?mName=${app.label}&access_token=${parent.state.accessToken}", style:"embedded", required:false, title:"Show REST URL For This Macro", description: none
				}
            }
        }
        if (macroType && macroType !="GroupM" && macroType !="Group"){
            section("Restrictions", hideable: true, hidden: !(runDay || timeStart || timeEnd || runMode || runPeople)) {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: imgURL() + "calendar.png"
				href "timeIntervalInput", title: "Only During Certain Times...", description: getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: imgURL() + "clock.png"
				input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: imgURL() + "modes.png"
                input "runPeople", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange: true, image: imgURL() + "people.png"
					if (runPeople && runPeople.size()>1) input "runPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
                input "muteRestrictions", "bool", title: "Mute Restriction Messages In Extension Group", defaultValue: false
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
page(name: "pageMacroAliases", title: "Enter alias names for this macro"){
	section {
    	for (int i = 1; i<macAliasCount()+1; i++){ input "macAlias${i}", "text", title: "Macro Alias Name ${i}", required: false }
    }
}
//Device Macro----------------------------------------------------
def pageGroup() {
	dynamicPage(name: "pageGroup", install: false, uninstall: false) {
		section { paragraph "Device Group Settings", image: imgURL() + "folder.png" }
        section (" ") {
            input "groupType", "enum", title: "Group Type...", options: [["colorControl": "Colored Light (On/Off/Toggle/Level/Color)"],["switchLevel":"Dimmer (On/Off/Toggle/Level)"],["doorControl": "Door (Open/Close)"],["lock":"Lock (Lock/Unlock)"],
            	["switch":"Switch (On/Off/Toggle)"],["colorTemperature":"Temperature (Kelvin) Light (On/Off/Toggle/Level/Temperature)"],["thermostat":"Thermostat (Mode/Off/Setpoint)"],["windowShade": "Window Shades (Open/Close)"]],required: false, multiple: false, submitOnChange:true
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
//WebCoRE Macro----------------------------------------------------
def pageCoRE() {
	dynamicPage(name: "pageCoRE", install: false, uninstall: false) {
        section { paragraph "WebCoRE Trigger Settings", image: "https://cdn.rawgit.com/ady624/${webCoRE_handle()}/master/resources/icons/app-CoRE@2x.png" }
		section (" "){
   			input "CoREName", "enum", title: "Choose WebCoRE Piston", options: parent.webCoRE_list('enum'), required: false, multiple: false
        	input "cDelay", "number", title: "Default Delay (Minutes) To Trigger", range:"0..*", defaultValue: 0, required: false
            if (parent.pwNeeded) input "usePW", "bool", title: "Require PIN To Run This WebCoRE Macro", defaultValue: false
        }
        section("Advanced features"){
        	paragraph "You may pass a single parameter to the WebCoRE piston by adding your list of words to proper developer slot and speaking the addition parameter when executing the piston. "+
            	"Please see the documentation from more information.", image: parent.imgURL() + "info.png"
        	input "advWebCore", "bool", title: "Require Additional Parameter To Run Macro", defaultValue: false, submitOnChange:true
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after macro runs", required: false, capitalization: "sentences"
             if (!noAck && advWebCore ) input "voicePostAdv", "text", title: "Message When No Advanced Parameter Received", required: false, capitalization: "sentences"
             input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true
        }
        if (!parent.webCoRE_list('enum')){
        	section("Missing WebCoRE pistons"){
				paragraph "It looks like you don't have the WebCoRE SmartApp installed, or you haven't created any pistons yet. To use this capability, please install WebCoRE, or if already installed, create some pistons, then try again.", image: parent.imgURL() + "caution.png"
            }
        }	
    }
}
//Group Macro----------------------------------------------------
def pageGroupM() {
	dynamicPage(name: "pageGroupM", install: false, uninstall: false) {
		section { paragraph "Extension Group Settings", image: imgURL() + "macrofolder.png" }
        section (" ") { 
        	input "groupMacros", "enum", title: "Macros/Extensions To Run (Control/WebCoRE/Voice/Weather Reports)...", options: parent.getMacroList("all", ""), required: false, multiple: true
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
        section ("When macro is activated...") {
            input "phrase", "enum", title: "Perform This Routine...", options: phrasesList, required: false, image: imgURL() + "routine.png" 
            input "setMode", "mode", title: "Set Mode To...", required: false, image: imgURL() + "modes.png"  
            input "SHM", "enum",title: "Set Smart Home Monitor To...", options: ["away":"Armed (Away)", "stay":"Armed (Home)", "off":"Disarmed"], required: false, image: imgURL() + "SHM.png"
            href "pageMQ", title: "Send Output To Message Queue(s)", description: ctlMQDesc(), state: ctlMsgQue ? "complete" : null, image: parent.imgURL()+"mailbox.png"
            href "pageSTDevices", title: "Control These SmartThings Devices...", description: getDeviceDesc(), state: deviceGreyOut(), image: imgURL() + "smartthings.png"
            href "pageHTTP", title: "Run This HTTP Request...", description: getHTTPDesc(), state: greyOutStateHTTP(), image: imgURL() + "network.png"
            input "cDelay", "number", title: "Default Delay (Minutes) To Activate",range:"0..*", defaultValue: 0, required: false, image: imgURL() + "stopwatch.png"
            if (parent.pwNeeded) input "usePW", "bool", title: "Require PIN To Run This Macro", defaultValue: false
        }
        section("Custom acknowledgment"){
             if (!noAck) input "voicePost", "text", title: "Acknowledgment Message", description: "Enter a short statement to play after macro runs", required: false, capitalization: "sentences"
             input "noAck", "bool", title: "No Acknowledgment Message", defaultValue: false, submitOnChange: true    
        }
	}
}
def pageMQ(){
    dynamicPage(name:"pageMQ"){
        section {
        	paragraph "Message Queue Configuration", image:parent.imgURL()+"mailbox.png"
        }
        section (" "){
            input "ttsMsg", "text", title: "Message To Send to Queue(s)", required: false, capitalization: "sentences", description: "Enter a message or a default one will be used"
            input "ctlMsgQue", "enum", title: "Message Queue Recipient(s)...", options: parent.getMQListID(true), multiple:true, required: false, submitOnChange: true
            input "ctlMQNotify", "bool", title: "Notify Only Mode (Not Stored In Queue)", defaultValue: false, submitOnChange: true
            if (!ctlMQNotify) input "ctlMQExpire", "number", title: "Message Expires (Minutes)", range: "1..*", required: false, submitOnChange: true
            if (!ctlMQNotify && !ctlMQExpire) input "ctlMQOverwrite", "bool", title: "Overwrite Other Voice Report Messages", defaultValue: false
            if (!ctlMQNotify) input "ctlSuppressTD", "bool", title: "Suppress Time/Date From Alexa Playback", defaultValue: false
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
            if (dimmers) input "dimmersCMD", "enum", title: "Command To Send To Dimmers", options:["on":"Turn on","off":"Turn off","set":"Set level", "toggle":"Toggle the dimmers' on/off state","decrease": "Decrease current brightness","increase": "Increase current brightness" ], multiple: false, required: false, submitOnChange:true
            if (dimmersCMD == "set" && dimmers) input "dimmersLVL", "number", title: "Dimmers Level", description: "Set dimmer level", required: false, defaultValue: 0, range:"0..100"
            if (dimmersCMD ==~ /decrease|increase/ && dimmers) input "dimmersUpDown", number, title: "Amount of change", description: "Set the amount your want to ${dimmersCMD}", required: false, defaultValue: 20
        }
        section ("Colored lights (Hue/Saturation)", hideWhenEmpty: true){
            input "cLights", "capability.colorControl", title: "Control These Colored Lights...", multiple: true, required: false, submitOnChange:true
            if (cLights) input "cLightsCMD", "enum", title: "Command To Send To Colored Lights", options: parent.cLightsCTLOptions(), multiple: false, required: false, submitOnChange:true
            if (cLightsCMD == "set" && cLights){
                input "cLightsCLR", "enum", title: "Choose A Color...", required: false, multiple:false, options: parent.STColors().name, submitOnChange:true
                if (cLightsCLR == "Custom-User Defined"){
                    input "hueUserDefined", "number", title: "Colored Lights Hue", description: "Set colored light hue (0 to 100)", range: "0..100", required: false, defaultValue: 0
                    input "satUserDefined", "number", title: "Colored Lights Saturation", description: "Set colored lights saturation (0 to 100)", range: "0..100", required: false, defaultValue: 0
                }
                input "cLightsLVL", "number", title: "Colored Light Brightness Level", description: "Set level, otherwise default color lightness will be used", required: false,defaultValue: 0, range:"0..100"
            }
             if (cLightsCMD ==~ /decrease|increase/ && cLights) input "cLightsUpDown", number, title: "Amount of change", description: "Set the amount your want to ${cLightsCMD}", required: false, defaultValue: 20
        }
        section ("Temperature (Kelvin) lights", hideWhenEmpty: true){
            input "cLightsK", "capability.colorTemperature", title: "Control These Lights...", multiple: true, required: false, submitOnChange:true
            if (cLightsK) input "cLightsKCMD", "enum", title: "Command To Send To Lights", options: parent.cLightsKCTLOptions(), multiple: false, required: false, submitOnChange:true
            if (cLightsKCMD == "set" && cLightsK) {
            	input "cLightsKEL", "enum", title: "Choose a Kelvin Temperature", options: kelvinOptions(), required: false, multiple: false
            	input "cLightsKLVL", "number", title: "Brightness Level", description: "Set level, otherwise current level will be used", required: false,defaultValue:100, range:"0..100"
            }
            if (cLightsKCMD ==~ /decrease|increase/ && cLightsK) input "cLightsKUpDown", "number", title: "Amount of change", description: "Set the amount your want to ${cLightsKCMD}", required: false, defaultValue: 20 , range:"1..100"
        }
        section ("Thermostats", hideWhenEmpty: true){
            input "tstats", "capability.thermostat", title: "Control These Thermostats...", multiple: true, required: false, submitOnChange:true
            if (tstats) input "tstatsCMD", "enum", title: "Command To Send To Thermostats", options: parent.tStatCTLOptions() , multiple: false, required: false, submitOnChange:true
            if (tstatsCMD ==~/heat|cool/) input "tstatLVL", "number", title: "Temperature Level", description: "Set temperature level", required: false,range:"1..100"
            if (tstatsCMD =~/increase|decrease/) input "tstatUpDown", "number", title: "Amount of change", description: "Set the amount of change in the setpoint (positive numbers only)", required: false, defaultValue: 5,range:"1..100"
        	
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
                input "port", "number", title: "Internal Port", description: "Enter a port number 0 to 65536", range: "0..65536",required: false 
                input "command", "text", title: "Command", description: "Enter REST commands", required: false 
            }
        }
    }
}
//---------------------------------------------------------------
def installed() { initialize() }
def updated() { initialize() }
def childUninstalled() {
	sendLocationEvent(name: "askAlexa", value: "refresh", data: [extension_list: parent ? parent.getExtList() : getExtList()] , isStateChange: true, descriptionText: "Ask Alexa extension list refresh")
    mqRefresh()
}
def initialize() {
    unsubscribe()
    if (!parent){
        if (!state.accessToken) log.error "Access token not defined. Ensure OAuth is enabled in the SmartThings IDE."
		webCoRE_init()
        subscribe(location, "AskAlexaMsgQueue", msgHandler)
        subscribe(location, "AskAlexaMsgQueueDelete", msgDeleteHandler)
        subscribe(location, "AskAlexaMQRefresh", mqRefresh)
        if (sonosCMD && sonosMemoryCount) songLocations()
        mqRefresh()
	}
    else{
    	unschedule()
    	state.scheduled=false 
    }
	sendLocationEvent(name: "askAlexa", value: "refresh", data: [extension_list: parent ? parent.getExtList() : getExtList()] , isStateChange: true, descriptionText: "Ask Alexa extension list refresh")
}
//--------------------------------------------------------------
mappings {
	path("/d") { action: [GET: "processDevice"] }
	path("/m") { action: [GET: "processMacro"] }
	path("/h") { action: [GET: "processSmartHome"] }
	path("/l") { action: [GET: "processList"] }
	path("/b") { action: [GET: "processBegin"] }
	path("/u") { action: [GET: "getURLs"] }
    path("/f") { action: [GET: "processFollowup"] }
    path("/q") { action: [GET: "processMQ"] }
    path("/mq") {action: [GET: "extMQ"] }
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
    def enableWhisper = speakPitch=="medium" && whisperMode && getTimeOk(timeStartWhisper,timeEndWhisper) && (!runModeWhisper || runModeWhisper.contains(location.mode)) 
    def speed  = speakSpeed ?:"medium"
    def pitch = speakPitch ?:"medium"
    return ["OOD":OOD, "continue":contOption,"personality":persType, "SmartAppVer": versionLong(),"IName":invocationName,"pName":pName,"whisper": enableWhisper,"speed":speed, "pitch":pitch ]
}
def sendJSON(outputTxt, icon){
    if (outputTxt && msgQueueNotify && mqCounts(msgQueueNotify) && outputTxt.endsWith("%") && outputTxt[-3..-1] != "%M%") {
        def ending= outputTxt[-3..-1]
        outputTxt = outputTxt.replaceAll("${ending}", "Please note: ${mqCounts(msgQueueNotify)}. ${ending}")
    }
    else if (outputTxt && msgQueueNotify && mqCounts(msgQueueNotify) && !outputTxt.endsWith("%")) outputTxt = outputTxt + "Please note: ${mqCounts(msgQueueNotify)}"
    if (outputTxt && outputTxt[-3..-1] == "%M%") outputTxt = outputTxt.replaceAll("%M%","%3%")
    if (outputTxt && outputTxt.endsWith("%")) log.debug outputTxt[0..-4]
    if (state.cmdFollowup && outputTxt && outputTxt[-3..-1] != "%P%") state.cmdFollowup=""
    if (state.cmdFollowup && outputTxt && outputTxt[-3..-1] == "%P%") runIn(30, clearFollowup)
    def showIcon = icon ? icon :"AskAlexa"
    if (outputTxt && outputTxt[-3..-1] ==~/%1%|%P%/) showIcon ="caution"
    else if (outputTxt && outputTxt[-3..-1] =="%2%" && !icon) showIcon ="info"
    return ["voiceOutput":outputTxt,"icon":showIcon]
}
def processFollowup(){
	log.debug "-Processing Follow up-"
    def type = params.Type
    def data = params.Data
    log.debug "Type: " + type
    log.debug "Data: " + data
	String outputTxt = ""
    def devIcon
    if (type =~/password|pin/){
		def pw = data ==~/undefined|\?/ ? 0 : data
        if (!state.cmdFollowup && pwNeeded) outputTxt = "You issued a password but have no active commands in memory. No action is being taken. %1%"
        else if (!pwNeeded) outputTxt = "Passwords have not been enabled in your Ask Alexa SmartApp. No action is being taken. %1%"
        else if (pw && state.cmdFollowup){
        	if (state.cmdFollowup.return == "deviceAction") {
            	devIcon = state.cmdFollowup.icon
                outputTxt=processDeviceAction(state.cmdFollowup.dev, state.cmdFollowup.op, pw, state.cmdFollowup.param, true)
            }
            else if (state.cmdFollowup.return == "changeMode") {
            	devIcon = "mode"
            	outputTxt=changeSHM(state.cmdFollowup.cmd, pw, state.cmdFollowup.param)
            }
            else if (state.cmdFollowup.return == "changeSHM") {
            	devIcon = "smarthome"
                outputTxt=changeSHM(state.cmdFollowup.cmd, pw, state.cmdFollowup.param)
            }
            else if (state.cmdFollowup.return == "runRoutine") {
            	devIcon="routine"
                outputTxt=runRoutine(state.cmdFollowup.cmd, pw, state.cmdFollowup.param)
            }
            else if (state.cmdFollowup.return == "macroAction") {
            	devIcon="macro"
                outputTxt=processMacroAction(state.cmdFollowup.mac, state.cmdFollowup.mNum, state.cmdFollowup.cmd, state.cmdFollowup.param, pw, true, state.cmdFollowup.xParam)
            }
        }
    }
    else if (data == "?") outputTxt ="I did not understand the device or action you referenced. %1%"
    sendJSON(outputTxt, devIcon)
}
def clearFollowup(evt){ state.cmdFollowup="" }
def processDevice() {    
	def dev = params.Device.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") 	//Label of device
	def op = params.Operator												//Operation to perform
    def numVal = params.Num     											//Number for dimmer/PIN type settings
    def param = params.Param.toLowerCase()									//Other parameter (color)
    if (op==~/status|undefined|null/ && param==~/undefined|null/ && numVal==~/undefined|null/) op="status"
    processDeviceAction(dev, op, numVal, param, false)
}
def processDeviceAction(dev, op, numVal, param, followup){        
	log.debug "-Device command received-"
	log.debug "Dev: " + dev
	log.debug "Op: " + op
	log.debug "Num: " + numVal
	log.debug "Param: " + param
	def num = numVal ==~/undefined|null|\?/ || !numVal ? 0 : numVal as int
	String outputTxt = ""
	def deviceList, count = 0, aliasDeviceType, aliasDeviceObj, aliasDeviceList, aliasDeviceName, devIcon
	getDeviceList().each{if (it.name==dev) {deviceList=it; count++}}
	if (mapDevices(true) && deviceAlias && !count && !deviceList){
		aliasDeviceList = state.aliasList.each { 
			if (it.aliasNameLC==dev) {
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
        def proceed = true
        def deviceObj=deviceList ? deviceList.devices.find{it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() == dev} : aliasDeviceObj
		def devType=deviceList ? deviceList.type : aliasDeviceType
        devIcon=deviceList ? deviceList.type : aliasDeviceType
        if (devType =~/door|lock/ && (lockVoc().find{it==op} || doorVoc().find{it==op})) {
            def currentState = deviceObj.currentValue(devType)
            if ((devType == "door" && (currentState==op) || (currentState == "closed" && op=="close"))||(devType=="lock" && currentState == op + "ed")) outputTxt="The ${dev} is already ${currentState}. %3%" 
            else if (pwNeeded && password && num != password as int && ((devType =="door" && doorPW) || (devType =="lock" && lockPW))) {
            	if (num != password as int && num != 0) outputTxt = "I heard a password that is not valid. %P%"
                if (num==0) outputTxt = "A valid password is needed to ${op} the ${dev}. %P%"
                state.cmdFollowup = [return: "deviceAction", dev: dev, op: op, numVal: numVal, param: param, icon:devIcon]
            }
        	else if ((op=="lock" && lockLockDisable) || (op=="unlock" && lockUnLockDisable) || (op=="open" && doorOpenDisable) || (op=="open" && doorOpenDisable)) outputTxt = "There are restrictions set up preventing the '${op}' command from being used on this ${devType}. %1%"
        	proceed = outputTxt ? false : true
        }
        if (proceed) outputTxt = getReply (deviceObj, devType, dev, op, num, param)
	}
	if (!count) { outputTxt = "I had some problems finding the device you specified. %1%" }
	if (followup) return outputTxt 
    else sendJSON(outputTxt,devIcon)
}
//List Request
def processList(){
	log.debug "-List command received-"
	def listType = params.Type.toLowerCase()	//Help type
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
    if (listType=~/shade|window/) { outputTxt = shades && shades.size()>1 ? "#window shades#" : shades && shades.size()==1 ? "@window shade@" :"%window shades%"; devices = shades; aliasType="shade" }
    if (listType=~/lock/) { outputTxt = locks && locks.size()>1 ? "#locks#" : locks && locks.size()==1 ? "@lock@" :"%locks%"; devices=locks; aliasType="lock" }	
    if (listType=~/colored light/) { outputTxt = cLights && cLights.size()>1 ? "#colored lights#": cLights && cLights.size()==1 ? "@colored light@" : "%colored lights%"; devices=cLights; aliasType="color" }
    if (listType=~/temperature light|kelvin/) { outputTxt = cLightsK && cLightsK.size()>1 ? "#temperature lights#": cLightsK && cLightsK.size()==1 ? "@temperature light@" : "%temperature lights%"; devices=cLightsK; aliasType="kTemp" }
    if (listType=~/switch/) { outputTxt = switches? "You can turn on, off or toggle the following switches: " +  getList(switches) + ". " : "%switches%" ; aliasType="switch" }
    if (listType=~/routine/) { outputTxt= listRoutines && listRoutines.size()>1 ? "#routines#":listRoutines && listRoutines.size()==1 ? "@routine@" : "%routines%"; devices=listRoutines }
    if (listType=~/water/) { outputTxt= water && water.size()>1 ? "#water sensors#" : water && water.size()==1 ?  "@water sensor@" : "%water sensors%" ; devices=water; aliasType="water" }
    if (listType =~/report|voice/) outputTxt = parseMacroLists("Voice","voice report","play")
    if (listType =~/device/) outputTxt = parseMacroLists("Group","device group","control")
    if (listType =~/schedule/) outputTxt = parseMacroLists("Schedule","schedule","")
    if (listType =~/control/) outputTxt = parseMacroLists("Control","control macro","run")
    if (listType =~/core|webcore|trigger/) outputTxt = parseMacroLists("CoRE","WEBCORE trigger","run")
    if (listType =~/extension group|group extention/) outputTxt = parseMacroLists("GroupM","extension group","run")
    if (listType =~/weather/) outputTxt = parseMacroLists("Weather","weather report","play")
    if (listType =~/message|queue/) outputTxt = parseMacroLists("MQ","message queue","play")
    if (listType =~/event/) {
    	outputTxt = "To list events for a device, you must give me the name of that device. " 
    	if (Math.abs(new Random().nextInt() % 2)==1) outputTxt += "For example, you could say, 'tell ${invocationName} to give me the last events for the Bedroom'. " +
        "You may also include the number of events you would like to hear. An example would be, 'tell ${invocationName} to give me the last 4 events for the Bedroom'. "
    }
    if (listType =~/alias/) outputTxt = "You can not list aliases directly. To hear the aliases that are available, choose a specific device catagory to list. For example, if you list the available switch devices, any switch aliases you created will be listed as well. "
    if (listType ==~/colors|color/) outputTxt = cLights ? "There are too many colors to list. Basic colors like red, blue, and green are availabe. For a full list of colors, it is recommended you print the Ask Alexa cheat sheet. " : "%colored lights%"
    if (listType ==~/group|groups|macro|macros/) outputTxt ="Please be a bit more specific about which groups or macros you want me to list. You can ask me about 'webcore triggers', 'extension groups', 'device groups', and 'control macros'. %1%"
    if (listType ==~/extension|extensions/) outputTxt ="Please be a bit more specific about which extensions you want me to list. You can ask me about 'voice reports', 'weather reports', 'schedules', and 'message queues'. %1%"
    if (listType ==~/sensor|sensors/) outputTxt ="Please be a bit more specific about what kind of sensors you want me to list. You can ask me to list items like 'water', 'open close', 'presence', 'acceleration, or 'motion sensors'. %1%"
    if (listType ==~/light|lights/) outputTxt ="Please be a bit more specific about what kind of lighting devices you want me to list. You can ask me to list devices like 'switches', 'dimmers' or 'colored lights'. %1%"
    if (outputTxt.startsWith("%") && outputTxt.endsWith("%")) outputTxt = "There are no" + outputTxt.replaceAll("%", " ") + "set up within your Ask Alexa SmartApp. "
    if (outputTxt.startsWith("@") && outputTxt.endsWith("@")){
    	if (Math.abs(new Random().nextInt() % 2)==1) outputTxt = "The only available" + outputTxt.replaceAll("@", " ")+ "is named: '" +  getList(devices) + "'. "
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
    sendJSON(outputTxt,"list")
}
def parseMacroLists(type, noun, action){
    def macName = "", children = getAskAlexa(), count = children.count{it.macroType==type}
    if (type == "MQ") count = getAAMQ().size() + 1
    else if (type == "Weather") count = getWR().size()
    else if (type == "Voice") count = getVR().size()
    else if (type =="Schedule") count = getSCHD().size()
    def extraTxt = (type ==~/Control|CoRE/) && count ? "Please note: You can also delay the execution of ${noun}s by adding the number of minutes after the name. For example, you could say, 'tell ${invocationName} to run the Macro in 5 minutes'. " : ""
	if (type!="Schedule") macName = count==1 ? "You only have one ${noun} called: " : count> 1 ? "You can ask me to ${action} the following ${noun}s: " : "You don't have any ${noun}s for me to ${action}"
	else macName = count==1 ? "You only have one ${noun} called: " : count> 1 ? "You can ask me for detail about the following ${noun}s: " : "You don't have any ${noun}s created"
    if (count && type !="MQ"){
		children.each{if (it.macroType==type){
			macName += "'" + it.label + "'"  ; count --
			macName += count>1 ? ", " : count==1 ? " and " : ""
			}	
		}
	}
    if (count && type == "MQ") macName += count ==2 ? "Primary Message Queue and " + getList(getAAMQ().label) : count ==1 ? "Primary Message Queue" : "Primary Message Queue, " + getList(getAAMQ().label)
	if (count && type == "Weather") macName += getList(getWR().label)
    if (count && type == "Voice") macName += getList(getVR().label)
    if (count && type == "Schedule") macName += getList(getSCHD().label)
    return macName + ". " + extraTxt
}
//Extension Group
def processMacroGroup(macroList, msg, append, noMsg, macLabel){
    String result = "", feedData =""
    def runCount=0
    if (macroList){ 
        macroList.each{
            getAskAlexa().each{child->
                if (child.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){ 
                    result += child.getOkToRun() ? child.macroResults(0,"","","","","")  : child.muteRestrictions ? "" : "You have restrictions on '${child.label}' that prevented it from running. %1%"             
                    runCount++
                    if (result.endsWith("%")) result = result[0..-4]
                }
            }
            getWR().each{report->
                if (report.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){	
                    result += processWeatherReport(it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))
                    runCount++
                    if (result.endsWith("%")) result = result[0..-4]
        		}
        	}
            getVR().each{report->
                if (report.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){	
                    result += processVoiceReport(it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))
                    runCount++
                    if (result.endsWith("%")) result = result[0..-4]
        		}
        	}
        }
        def extraTxt = runCount > 1 ? "extensions" : "extension"
        if (runCount == macroList.size()) {
            if (!noMsg) {
                msg = replaceVoiceVar(msg,"","",macroType,app.label,0,"")
                if (msg && append) result += msg
                if (msg && !append) result = msg
                if (append && !msg) result += "I ran ${runCount} ${extraTxt} in this extension group. "
                if (!append && !msg) result = "I ran ${runCount} ${extraTxt} in this extension group. "
        	}
            else result = " "
        }
        else result = "There was a problem running one or more of the extensions in the extension group. %1%"
    }
    else result="There were no extensions present within this extension group. Please check your Ask Alexa SmartApp and try again. %1%"
    def data = result ? result.endsWith("%") ? [alexaOutput: result[0..-4]] : [alexaOutput: result] : [alexaOutput: "No Output"]
	sendLocationEvent(name: "askAlexa", value: app.id, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa ran '${macLabel}' extension group.")
    return result
}
def processOtherRpt(list){
	String result =""
    list.each{
		getWR().each{report->
			if (report.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){	
				result += processWeatherReport(it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))
				if (result.endsWith("%")) result = result[0..-4]
			}
        }
        getVR().each{report->
			if (report.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){	
				result += processVoiceReport(it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))
				if (result.endsWith("%")) result = result[0..-4]
        	}
        }
        getAAMQ().each{report->
			if (report.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == (it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))){	
				result += msgQueueReply("play",it.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))
				if (result.endsWith("%")) result = result[0..-4]
        	}
        }     
    }
	return result
}
//Message Queue Process
def processMQ() {
    def queue = params.Queue.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") 	//Queue name
    def cmd = params.MQCmd													//Command
    String outputTxt = ""
	def children = getAAMQ(), count = children.count {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == queue}
    if (count==0 && queue=~/primary/) count = 1
    if (count==0 && queue !="undefined" && queue !="null" ) outputTxt="I could not find a message queue named '${queue}'. Please check the message queue name in your SmartApp. %1%"
    else if (count<2) outputTxt = msgQueueReply(cmd,queue)
    else outputTxt ="You have multiple message queues named '${queue}'. Please check your SmartApp to fix this conflict. %1%"
    sendJSON(outputTxt,"mailbox") 
}
//External Message Queue Input
def extMQ(){
	def queue = params.queue		//Queue name
    def msg = params.msg			//Message to send	
    def source = params.source		//Sending source
    if (queue && msg && source){
    	if (queue == "Primary Message Queue") msgPMQ (new Date(now()), msg, "", source, false, 0, false, false, false)
        else if (getAAMQ().find{it.label == queue}){
        	def qNameRun = getAAMQ().find{it.label == queue}
            if (qNameRun) qNameRun.msgHandler(new Date(now()), msg, "", source, false, 0, false, false, false)
    	}
        else log.debug "An external source, '${source}', attempted to send a message to an invalid message queue name."
	} 
    else log.debug "An exteranl source attempted to send a message but did not have the correct parameters."
}
def getExtAddr(name){
	return "${getApiServerUrl()}/api/smartapps/installations/${app.id}/mq?queue=${name}&msg={Message to send}&source={Name of what is sending the message}&access_token=${state.accessToken}"
}
//Message Queue Reply
def msgQueueReply(cmd,queue){
	log.debug "-Message Queue Response-"
    log.debug "Message Queue: " + queue
    log.debug "Message Queue Command: " + cmd
    String result = ""
	def validQueue = getAAMQ().find{it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == queue}
    if (validQueue) result=validQueue.msgQueueReply(cmd)
    else {
        def msgCount = state.msgQueue ? state.msgQueue.size() : 0, msgS= msgCount==0 || msgCount>1 ? " messages" : " message"
        if (cmd =~/play|open|undefined|null/){
            if (msgCount==0) result = "You don't have any messages in the primary message queue. %M%"
            else {
                result = "You have " + msgCount + msgS + " in your primary message queue: "
                state.msgQueue.sort({it.date})
                if (msgQueueOrder) state.msgQueue.reverse(msgQueueOrder as int? true : false)
                state.msgQueue.each{
                	def msgData= timeDate(it.date), msgTimeDate = msgQueueDateSuppress || it.suppressTimeDate ? "" : "${msgData.msgDay} at ${msgData.msgTime}, "
                    result += "${msgTimeDate}'${it.appName}' posted the message: '${it.msg}' "
                }
                result +="%M%"
            }
        }
        else if (cmd =~ /clear|delete|erase/) {
            qDelete()
            result="I have deleted all of the messages from the primary message queue. %M%"
        }
        else result="For the primary message queue, be sure to give a 'play' or 'delete' command. %1%"
    }
    return result 
}
//Weather Report Reply
def processWeatherReport(rpt){
    log.debug "Weather Report Name: " + rpt
    def wr = getWR().find {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == rpt}, outputTxt
    if (wr.getOkToRun()){
    	 outputTxt = wr.getOutput()
         def data = outputTxt ? [alexaOutput: outputTxt[0..-4]] : [alexaOutput: "No Output"]
         sendLocationEvent(name: "askAlexa", value: wr.id, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa activated '${wr.label}' weather report.")
    }
    else outputTxt = wr.muteRestrictions ? "" : "You have restrictions on '${wr.label}' that prevented it from running. %1%"
    return outputTxt
}
//Voice Report Reply
def processVoiceReport(rpt){
    log.debug "Voice Report Name: " + rpt
    def vr = getVR().find {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == rpt}, outputTxt
    if (vr.getOkToRun()){
    	 outputTxt = vr.getOutput() 
         def data = outputTxt ? [alexaOutput: outputTxt[0..-4]] : [alexaOutput: "No Output"] 
         sendLocationEvent(name: "askAlexa", value: vr.id, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa activated '${vr.label}' voice report.")
    }
    else outputTxt = vr.muteRestrictions ? "" : "You have restrictions on '${vr.label}' that prevented it from running. %1%"  
    return outputTxt
}
//Schedule Reply
def processSchedule(schedule,cmd, cancel){
    log.debug "Schedule Name: " + schedule
    log.debug "Schedule Command: " + cmd
    log.debug "Schedule Cancel Delete: ${cancel=="9999" ? "True" : "False"}"
    def sd = getSCHD().find {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == schedule}, outputTxt
    if (cmd==~/status|list|undefined|null/ && cancel != "9999"  ) outputTxt = sd.getSchdDesc()
    else if (cmd==~/toggle/) outputTxt = sd.toggle()
    else if (cmd==~/on|off/) outputTxt = sd.onOff(cmd)
    else if (cmd=="delete") outputTxt = sd.deleteSch()
    else if (cmd==~/undefined|null/ && cancel == "9999") outputTxt = sd.notDeleteSch()
	else outputTxt="I did not understand what you wanted to do the the schedule, '${schedule}'. You may query this schedule, delete it, or toggle its 'on' and 'off' state. %1%"
    return outputTxt
}
//Macro Processing
def processMacro() {
    log.debug "-Macro/extension command received-"
	def mac = params.Macro.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") 	//Macro/extension name
    def mNum = params.Num													//Number variable-Typically delay to run
    def cmd = params.Cmd													//Group Command
    def param = params.Param												//Parameter
    def mPW = params.MPW													//Macro Password
    def xParam = params.xParam												//WebCoRE parameters
    String outputTxt = ""
    def count = 0, macCount=0, wrCount=0, vrCount=0, sdCount, macAlias
    macCount = getAskAlexa().count {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac}
    wrCount = getWR().count {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac}
    vrCount = getVR().count {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac}
    sdCount = getSCHD().count{it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac}
    count = macCount + wrCount + vrCount + sdCount
    if (!count) {    
        getAskAlexa().each{
        	for (int i = 1; i<macAliasCount()+1; i++){
            	if (it."macAlias${i}" && it."macAlias${i}".toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")==mac) {
                	macAlias = it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")
                	macCount ++
        		}
        	}	   
    	}
    }
    if (!count && !macCount) {    
        getWR().each{
        	for (int i = 1; i<it.extAliasCount()+1; i++){
            	if (it."extAlias${i}" && it."extAlias${i}".toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")==mac) {
                	macAlias = it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")
                	wrCount ++
        		}
        	}	   
    	}
    }
    if (!count && !macCount && !wrCount) {    
        getVR().each{
        	for (int i = 1; i<it.extAliasCount()+1; i++){
            	if (it."extAlias${i}" && it."extAlias${i}".toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")==mac) {
                	macAlias = it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")
                	vrCount ++
        		}
        	}	   
    	}
    }
    count = macCount + wrCount + vrCount + sdCount
    if (count == 1 && macAlias) mac = macAlias
    if (count > 1) outputTxt ="You have duplicate macros, aliases, or extensions named '${mac}'. Please check your SmartApp to fix this conflict. %1%"
    if (!count) outputTxt = "I could not find a macro, alias or any extension named '${mac}'. %1%"
    if (outputTxt) sendJSON(outputTxt,"caution")
    else {
    	if (macCount) processMacroAction(mac, mNum, cmd, param, mPW, false,xParam)
        else if (wrCount) sendJSON(processWeatherReport(mac),"weather")
        else if (vrCount) sendJSON(processVoiceReport(mac),"voice")
        else if (sdCount) sendJSON(processSchedule(mac,cmd,mNum),"schedule")
	}        
}
def processMacroAction(mac, mNum, cmd, param, mPW, followup,xParam){
    log.debug "Macro Name: " + mac
    log.debug "mNum: " + mNum
    log.debug "Cmd: " + cmd
    log.debug "Param: " + param
    log.debug "mPW: " + mPW
    log.debug "xParam: " + xParam
    if (mNum == "0" && cmd==~/undefined|null/ && param ==~/undefined|null/) cmd="off"
    def num = mNum ==~/undefined|null|\?/ || !mNum  ? 0 : mNum as int
    String outputTxt = ""
    def macroType="", colorData, err=false, playContMsg, suppressContMsg
    if (cmd ==~/low|medium|high/){
        if (cmd=="low" && dimmerLow) num = dimmerLow else if (cmd=="low" && !dimmerLow) err=true 
		if (cmd=="medium" && dimmerMed) num = dimmerMed else if (cmd=="medium" && !dimmerMed) err=true 
		if (cmd=="high" && dimmerHigh) num = dimmerHigh else if (cmd=="high" &&!dimmerhigh) err=true
        if (err) outputTxt = "You don't have a default value set up for the ${outputTxt} level. I am not making any adjustments. %1%"
    }
    def getColorData = STColors().find {it.name.toLowerCase()==param}
    if (getColorData){
        def hueColor = Math.round(getColorData.h / 3.6), satLevel = getColorData.s
        colorData = [hue: hueColor as int, saturation: satLevel as int] 
    }
    if (!err){
		def macProceed= true, children = getAskAlexa(), child = children.find {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == mac}
        if (child.usePW && pwNeeded && password && mPW != password && ((child.macroType == "Group" && child.groupType == "lock") || child.macroType == "GroupM" ||
			(child.macroType == "Group" && child.groupType == "doorControl") || child.macroType == "Control" || child.macroType == "CoRE")){
			macProceed = false
			def pwExtra = mPW==~/undefined|null/ ? "a" : "the proper"
			if (child.macroType == "Group" && child.groupType == "lock") outputTxt = "To lock or unlock this group, you must use ${pwExtra} password. %P%"
			if (child.macroType == "Group" && child.groupType == "doorControl") outputTxt = "To open or close this group, you must use ${pwExtra} password. %P%"
			if (child.macroType == "CoRE") outputTxt = "To activate this WebCore Trigger, you must use ${pwExtra} password. %P%"
			if (child.macroType == "Control") outputTxt = "To activate this Control Macro, you must use ${pwExtra} password. %P%"             
			if (child.macroType == "GroupM") outputTxt = "To activate this Group Macro, you must use ${pwExtra} password. %P%" 
			state.cmdFollowup=[return: "macroAction", mac:mac, mNum:mNum, cmd:cmd, param:param, mPW:0,xParam:xParam ]
		}
		if (child.macroType != "Group" && child.macroType != "GroupM" && cmd=="list" && macProceed) { outputTxt = "You can not use the list command with this type of macro. %1%"; macProceed = false }
		else if (child.macroType == "GroupM" && cmd=="list" && macProceed) {
			def gMacros= child.groupMacros.size()==1 ? "extension" : "extensions"
			outputTxt="You have the following ${gMacros} within the '${child.label}' extension group: " + getList(child.groupMacros) +". "
			macProceed = false
		}
		if (macProceed){
			playContMsg = child.overRideMsg ? false : true
            suppressContMsg = child.suppressCont && !child.overRideMsg && contMacro
			def fullMacroName = [GroupM: "Extension Group",CoRE: "WebCoRE Trigger", Control:"Control Macro", Group:"Device Group"][child.macroType] ?: child.macroType
			if (child.macroType != "GroupM") outputTxt = child.getOkToRun() ? child.macroResults(num, cmd, colorData, param, mNum,xParam) : "You have restrictions within the ${fullMacroName} named, '${child.label}', that prevent it from running. Check your settings and try again. %1%"
			else outputTxt = processMacroGroup(child.groupMacros, child.voicePost, child.addPost, child.noAck, child.label)   
        }
    }
    if (outputTxt && !outputTxt.endsWith("%") && !outputTxt.endsWith(" ")) outputTxt += " "
    if (outputTxt && !outputTxt.endsWith("%") && playContMsg && !suppressContMsg ) outputTxt += "%4%"
    else if (outputTxt && !outputTxt.endsWith("%") && suppressContMsg ) outputTxt += "%X%"
    if (followup) return outputTxt 
    else sendJSON(outputTxt,"macro")
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
    if (cmd ==~/undefined|null/) {
    	if (param=="off") outputTxt="Be sure to specify a device, or the word 'security', when using the 'off' command. %1%"
        if (listModes?.find{it.toLowerCase()==param} && param != currMode) cmd = "mode"
    	if (param==~/list|arm|undefined|null/) cmd = "security"
        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases.find{it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()==param}) cmd = "routine"
    }
    if (cmd == "mode") outputTxt=changeMode(cmd, num, param)
    if (cmd==~/security|smart home|smart home monitor|SHM/) outputTxt=changeSHM(cmd, num, param)
    if (cmd=="routine" && listRoutines) outputTxt=runRoutine(cmd, num, param)
    if (!outputTxt) outputTxt = "I didn't understand what you wanted me to do. %1%" 
    if (outputTxt && !outputTxt.endsWith("%") && !outputTxt.endsWith(" ")) outputTxt += " "
    if (outputTxt && !outputTxt.endsWith("%")) outputTxt +="%2%"
    sendJSON(outputTxt,"smarthome")
}
private changeMode(cmd, num, param){
    String outputTxt = ""
    def currMode = location.mode.toLowerCase()
	if (param ==~/undefined|null/) outputTxt ="The current SmartThings mode is set to, '${currMode}'. %2%"
	if (listModes && param !="undefined" && param !="null"){
        if (modesPW && pwNeeded && password && num ==~/undefined|null/) {
            outputTxt = "You must use a password to change your SmartThings mode. %P%"
            state.cmdFollowup=[return: "changeMode", cmd: cmd, num: num, param: param]
        }
        if (modesPW && pwNeeded && password && num!="undefined" && num!="null" && num != password) outputTxt="I did not hear the correct password to change your SmartThings mode. %1%"
        if (!modesPW || !pwNeeded || (modesPW && pwNeeded && num == password)){
            if (listModes?.find{it.toLowerCase()==param} && param != currMode) {
                def newMode=listModes.find{it.toLowerCase()==param}
                outputTxt ="I am setting the SmartThings mode to, '${newMode}'. %3%"
                setLocationMode(newMode)
            }
            else if (param == currMode) outputTxt ="The current SmartThings mode is already set to '${currMode}'. No changes are being made. %2%"
            if (!outputTxt) outputTxt = "I did not understand the mode you wanted to set. For a list of available modes, simply say, 'ask ${invocationName} for mode list'. %1%"
        }
	}
	else if (!outputTxt) outputTxt = "You can not change your mode to '${param}' because you do not have this mode selected within your SmartApp. Please enable this mode for control. %1%"
	return outputTxt
}
private changeSHM(cmd, num, param){
	String outputTxt = ""
    def SHMstatus = location.currentState("alarmSystemStatus")?.value
	def SHMFullStat = [off : "disarmed", away: "armed away", stay: "armed home"][SHMstatus] ?: SHMstatus
	def newSHM = "", SHMNewStat = "" 
	if (param==~/undefined|null/) outputTxt ="The Smart Home Monitor is currently set to, '${SHMFullStat}'. %2%"
		if (listSHM && param != "undefined" && param != "null"){
			if (shmPW && pwNeeded && password && num ==~ /undefined|null/) {
            	outputTxt = "You must use a password to change the Smart Home Monitor. %P%"
                state.cmdFollowup=[return: "changeSHM", cmd: cmd, num:num, param: param]
            }
            if (shmPW && pwNeeded && password && num!="undefined" && num!="null" && num != password) outputTxt="I did not hear the correct password to change the Smart Home Monitor. %1%"
            if (!shmPW || !pwNeeded || (shmPW && pwNeeded && num == password)){
                if (param==~/arm|armed/ && (listSHM.find{it =="Armed (Away)"} || listSHM.find{it =="Armed (Home)"})) outputTxt ="I did not understand how you want me to arm the Smart Home Monitor. Be sure to say, 'armed home' or 'armed away', to properly change the setting. %1%"   
                if ((param ==~/off|disarm/) && listSHM.find{it =="Disarmed" }) newSHM="off"
                if ((param ==~/away|armed away|arm away/) && listSHM.find{it =="Armed (Away)"}) newSHM="away"
                if ((param ==~/home|armed home|arm home|stay|armed stay|arm stay/) && listSHM.find{it =="Armed (Home)"}) newSHM="stay"
                if (newSHM && SHMstatus!=newSHM) {
                    sendLocationEvent(name: "alarmSystemStatus", value: newSHM)
                    SHMNewStat = [off : "disarmed", away: "armed away", stay: "armed home"][newSHM] ?: newSHM
                    outputTxt ="I am setting the Smart Home monitor to, '${SHMNewStat}'. %3%"
                }
            else if (SHMstatus==newSHM) outputTxt ="The Smart Home Monitor is already set to '${SHMFullStat}'. No changes are being made. %2%" 
       		}
        }
	if (!outputTxt) outputTxt = "I was unable to change your Smart Home Monitor. Ensure you have the proper settings enabled within your SmartApp. %1%"
	return outputTxt
}
private runRoutine(cmd, num, param){
    String outputTxt = ""
    if (param != "undefined" && param != "null") {
        def whichRoutine = listRoutines.find{it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()==param}
            if (whichRoutine) {
                if (routinesPW && pwNeeded && password && num ==~ /undefined|null/) {
                    outputTxt = "You must use a password to run SmartThings routines. %P%"
                    state.cmdFollowup=[return: "runRoutine", cmd: cmd, num:num, param: param]
                }
                if (routinesPW && pwNeeded && password && num!="undefined" && num!="null" && num != password) outputTxt="I did not hear the correct password to run the SmartThings routines. %1%"
                if (!routinesPW || !pwNeeded || (routinesPW && pwNeeded && num == password)){
                    location.helloHome?.execute(whichRoutine)
                    outputTxt="I am executing the '${param}' routine. %3%"
                }
            }
            else outputTxt = "You can not run the SmartThings routine named, '${param}', because you do not have this routine enabled in the Ask Alexa SmartApp. %1%"
        }
	else outputTxt ="To run SmartThings routines, ask me to run the routine by its full name. For a list of available routines, simply say, 'ask ${invocationName} to list routines'. %1%"
    return outputTxt
}
def getReply(devices, type, STdeviceName, op, num, param){
	String result = "", batteryWarnTxt=""
	try {
        def STdevice = devices
        def supportedCaps = STdevice.capabilities
        def supportedCMDs = STdevice.getSupportedCommands()
        if (op=="status") {
            if (type == "temperature"){
                def temp = roundValue(STdevice.currentValue(type))
                result = "The temperature of the ${STdeviceName} is ${temp} degrees"
                if (otherStatus) {
                    def humidity = STdevice.currentValue("humidity"), wet=STdevice.currentValue("water") ,contact=STdevice.currentValue("contact")
                    result += humidity ? ", and the relative humidity is ${humidity}%. " : ". "
                    result += wet ? "Also, this device is a leak sensor, and it is currently ${wet}. " : ""
                    result += contact ? "This device is also a contact sensor sensor, and it is currently reading ${contact}. " : ""
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
                    result += temp ? ", and the temperature is ${temp} degrees. " : ". "
				}
                else result += ". "
            }
            else if (type ==~ /level|color|switch|kTemp/) {
                def onOffStatus = STdevice.currentValue("switch")
                result = "The ${STdeviceName} is ${onOffStatus}"
                if (otherStatus) { 
                	def level = STdevice.currentValue("level"), power = STdevice.currentValue("power")
                    result += onOffStatus == "on" && level ? ", and it's set to ${level}%" : ""
                    result += onOffStatus=="on" && power > 0 ? ", and is currently drawing ${power} watts of power. " : ". "
                    result += onOffStatus=="on" && type == "kTemp" ? "This light is set to " + STdevice.currentValue("colorTemperature") + " degrees Kelvin. " : ""
            	}
                else result += ". "
            }
            else if (type == "thermostat"){
            def temp = roundValue(STdevice.currentValue("temperature"))
            result = "The ${STdeviceName} temperature reading is currently ${temp} degrees"
            if (otherStatus){
                def humidity = STdevice.currentValue("humidity"), opMode = STdevice.currentValue("thermostatMode")
                def heat = opMode==~/heat|auto/ || stelproCMD ? STdevice.currentValue("heatingSetpoint") : ""
                if (heat) heat = roundValue(heat)
                def cool = opMode==~/cool|auto/ ?  STdevice.currentValue("coolingSetpoint") : "" 
                if (cool) cool = roundValue(cool)
                result += opMode ? ", and the thermostat's mode is: '${opMode}'. " : ". "
                result += humidity ? " The relative humidity reading is ${humidity}%. " : ""
                if (nestCMD && supportedCaps.name.contains("Presence Sensor")){
                	result += " This thermostat's presence sensor is reading: "
                    result += STdevice.currentValue("presence")=="present" ? "'Home'. " : "'Away'. "
                }
                if ((ecobeeCMD && !MyEcobeeCMD) && STdevice.currentValue('currentProgramId') =~ /home|away|sleep/ ) result += " This thermostat's comfort setting is set to ${STdevice.currentValue('currentProgramId')}. "
                if (MyEcobeeCMD && STdevice.currentValue('setClimate')) {
                	def climatename = STdevice.currentValue('setClimate'), climateList = STdevice.currentValue('climateList')
                    if (climateList.contains(climatename)) result += " This thermostat's comfort setting is set to ${climatename}. "
                }
                def opState = STdevice.currentValue("thermostatOperatingState")
                if (opState) {
                	if ((opState=='fan only') || (opState=='vent economizer')) opState = 'running the ${opState}'	// idle, heating, cooling, fan only, pending heat, pending cool or vent economizer
                    result += " The thermostat is currently ${opState}. "
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
        else if (op =~/event/) {	
			def finalCount = num != 0 ? num as int : eventCt ? eventCt as int : 0
			if (finalCount>0 && finalCount < 10) result = getLastEvent(STdevice, finalCount, STdeviceName) + "%3%"
			else if (!finalCount) { result = "You do not have the number of events you wish to hear specified in your Ask Alexa SmartApp, and you didn't specify a number in your request. %1%" }
			else if (finalCount > 9) { result = "The maximum number of past events to list is nine. %1%" }
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
                            if (STdevice.currentValue(attribute)) result = "My Ecobee's current tip number ${tipNum} is '" + STdevice.currentValue(attribute) + "'. %2%"
                            else result ="Tip number is unavailable at this time for the ${STdeviceName}. Try erasing the tips, then issue the 'GET TIPS' command and try again. %1%"
                        }
                        else if (op==~/play|give/ || (op=="tip" && param==~/undefined|null/)) { 
                            def tipNum= state.tipCount>0 ? state.tipCount : 1                    
                            def attribute="tip${tipNum}Text"
                            if (!(STdevice.currentValue(attribute))) {
                                STdevice.getTips(1)
                                state.tipCount=1                       
                                tipNum= 1                           
                                attribute="tip${tipNum}Text"
                            }                            
                            if (STdevice.currentValue(attribute)) { 
                                result = "My Ecobee's current tip number ${tipNum} is '" + STdevice.currentValue(attribute) + "'. %2%"
                                state.tipCount=state.tipCount+1
                            }
                            else result ="Tip number is unavailable at this time for the ${STdeviceName}. Try erasing the tips, then issue the 'GET TIPS' command and try again. %1%"
                        }
                        else if (op ==~/get|load|reload/) { 
                            def tipLevel = num>0 && num<5 ? num :1
                            result = "I am loading level ${tipLevel} tips to ${STdeviceName}. %2%"
                            STdevice.getTips(tipLevel)
                            state.tipCount=1
                        } 
                        else if (op ==~/restart|erase|delete|clear|reset/) { 
                            result = "I am resetting the tips from ${STdeviceName}. Be sure to ask for the 'GET TIP' command to reload your thermostat advice. %2%"
                            STdevice.resetTips()
                            state.tipCount=1
                        }
                        else result = "I did not understand what you wanted me to do with the Ecobee tips. Valid commands are 'get', 'give', 'repeat' or 'erase' tips. %1%"
					}
                    else result = "You do not have the Ecobee tips functionality enabled in your SmartApp. %1%"
                }
                else {                   
                    if (param ==~/undefined|null/) param = tstatCool ? "cool" : tstatHeat ? "heat" : param
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
                    if ((param==~/heat|heating|cool|cooling|auto|automatic|eco|AC|comfort|home|away|sleep|resume program/ || (ecobeeCustomRegEx && param =~ /${ecobeeCustomRegEx}/)) && num == 0 && op==~/undefined|null/) op="on" 
                    if (op ==~/on|off/) {
                        if (param ==~/undefined|null/ && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning the ${STdeviceName} on. %1%"
                        if (param =~/heat/) {result="I am setting the ${STdeviceName} to 'heating' mode. "; STdevice.heat()}
                        if (param =~/cool|AC/) {result="I am setting the ${STdeviceName} to 'cooling' mode. "; STdevice.cool()}
                        if (param =~/auto/) {result="I am setting the ${STdeviceName} to 'auto' mode. Please note, to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints separately. " ; STdevice.auto()}
                        if (param ==~/home|present/ && nestCMD) {result = "I am setting the ${STdeviceName} to 'home'. "; STdevice.present()} 
                        if (param =="away" && nestCMD) {result = "I am setting the ${STdeviceName} to 'away'. Please note that Nest thermostats will not respond to temperature changes while in 'away' status. "; STdevice.away()} 
                        if ((param ==~/home|away|sleep/ || (ecobeeCustomRegEx && param =~ /${ecobeeCustomRegEx}/)) && ecobeeCMD) {
                            result = "I am setting the ${STdeviceName} to '" + param + "'. "
                            if (STdevice.hasCommand("setThermostatProgram")) STdevice.setThermostatProgram("${param.capitalize()}")
                            else if (STdevice.hasCommand("setClimate")) STdevice.setThisTstatClimate("${param.capitalize()}") 
                            else result ="There was an error setting your climate. %1%"
                        }
                        if (param =="resume program" && ecobeeCMD && !MyEcobeeCMD) {result = "I am resuming the climate program of the ${STdeviceName}. "; STdevice.resumeProgram()}  
                        if (param =="resume program" && ecobeeCMD && MyEcobeeCMD) {result = "I am resuming the climate program of the ${STdeviceName}. "; STdevice.resumeThisTstat()}  
                        if (op =="off") { result = "I am turning the ${STdeviceName} ${op}. "; STdevice.off() }
                        if (stelproCMD && param==~/eco|comfort/) { result="I am setting the ${STdeviceName} to '${param}' mode. "; STdevice.setThermostatMode("${param}") } 
                    }
                    else if (op=~"report") {
                    	if (nestCMD && nestMGRCMD) { STdevice.updateNestReportData();  result = STdevice.currentValue("nestReportData").toString() +" %2%" }
                        if (!nestCMD || (nestCMD && !nestMGRCMD) ) result ="The 'report' command is reserved for Nest Thermostats using the NST Manager SmartApp. " +
                        	"You do not have the options enabled to use this command. Please check your settings within your smartapp. %1%"
                        if (result=="null") result = "The NST Manager returned no results for the ${STdeviceName}. Please check your settings within your smartapp. %1%"
                    }
                    else {
                        if (param ==~/undefined|null/ ){ 
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
                            try { if (stelproCMD) STdevice.applyNow() }
                            catch (e){ log.warn "An error was encountered when attempting to send the 'applyNow()' command to the '${STdeviceName}'. If you don't have any stelPro devices please disable this feature in the Settings>>Device Specific Commands."}     
                        }
                        if ((param =~/cool|AC/) && num > 0) {
                            result="I am setting the cooling setpoint of the ${STdeviceName} to ${num} degrees. "
                            STdevice.setCoolingSetpoint(num)
                        }
                        if (param != "undefined" && param != "null" && tstatHighLimit && num >= tstatHighLimit) result += "This is the maximum temperature I can set for this device. %1%"
                        if (param != "undefined" && param != "null" && tstatLowLimit && num <= tstatLowLimit) result += "This is the minimum temperature I can set for this device. %1%"
                    }
            	}
            }
            if (type ==~ /color|level|switch|kTemp/){
                num = num < 0 ? 0 : num >99 ? 100 : num
                def overRideMsg = "" 
                if (op == "maximum") num = 100
                if ((op ==~/increase|raise|up|decrease|down|lower|brighten|dim/) && (type ==~ /color|level|kTemp/)){ 
                     def newValues = upDown(STdevice, type, op, num, STdeviceName)
                     num = newValues.newLevel
                     op= num > 0 ? "on" : "off"
                     overRideMsg = newValues.msg
                }
                if (op ==~/low|medium|high/ && type ==~ /color|level|kTemp/){
                	if (op=="low" && dimmerLow) num = dimmerLow else if (op=="low" && dimmerLow=="") num =0 
                    if (op=="medium" && dimmerMed) num = dimmerMed else if (op=="medium" && !dimmerMed) num = 0 
                    if (op=="high" && dimmerHigh) num = dimmerHigh else if (op=="high" && !dimmerhigh) num = 0 
                    if (num>0) overRideMsg = "I am turning the ${STdeviceName} to ${op}, or a value of ${num}%. "
                    if (num==0) overRideMsg = "You don't have a default value set up for the '${op}' level. I am not making any changes to the ${STdeviceName}. %1%"
                }
                if ((type == "switch") || (type ==~ /color|level|kTemp/ && num==0 )){
                    if (type ==~ /color|level|kTemp/ && num==0 && op==~/undefined|null/  && param==~/undefined|null/ ) op="off"
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
                if (type ==~ /color|level|kTemp/ && num > 0) {
                	STdevice.setLevel(num)
                    result = overRideMsg ? overRideMsg : num==100 ? "I am setting the ${STdeviceName} to its maximum value. " : "I am setting the ${STdeviceName} to ${num}%. "                    
				}
                if (type == "color" && param !="undefined" && param !="null" && supportedCaps.name.contains("Color Control")){
                    def getColorData = STColors().find {it.name.toLowerCase()==param}
                    if (getColorData){
                        def hueColor = Math.round (getColorData.h / 3.6), satLevel = getColorData.s,newLevel = num > 0 ? num : STdevice.currentValue("level")
                        def newValue = [hue: hueColor as int, saturation: satLevel as int]
                        STdevice?.setColor(newValue)
                        STdevice?.setLevel(newLevel)
                        result = "I am setting the color of the ${STdeviceName} to ${param}"
                        result += num>0 ? ", at a brightness level of ${num}%. " : ". "
                	}
                }
                if (type == "kTemp" && param !="undefined" && param !="null" && supportedCaps.name.contains("Color Temperature")){
                	def sWhite = kSoftWhite ? kSoftWhite as int : 2700, wWhite= kWarmWhite ? kWarmWhite as int: 3500, cWhite= kCoolWhite ? kCoolWhite as int: 4500, dWhite= kDayWhite ? kDayWhite as int: 6500
                    def kelvin = param=="soft white" ? sWhite : param=="warm white" ? wWhite : param=="cool white" ? cWhite : param=="daylight white" ? dWhite : 9999
                    if (kelvin <9999){
                    	STdevice?.setColorTemperature(kelvin)
                    	result = "I am setting the temperature of the ${STdeviceName} to ${param}, or ${kelvin} degrees Kelvin"
                    	result += num>0 ? ", at a brightness level of ${num}%. " : ". "
                    }
                    else result = "I didn't understand the temperature you wanted to set the ${STdeviceName}. Valid temperatures are soft white, warm white, cool white and daylight white. %1%"
                }
            	if (!result){
                	if (type=="switch") result = "For the ${STdeviceName} switch, be sure to give an 'on', 'off' or 'toggle' command. %1%"
            		if (type=="level") result = overRideMsg ? overRideMsg: "For the ${STdeviceName} dimmer, the valid commands are: " + getList(vocab) + " or brightness a level setting. %1%"
            		if (type=="color") result = overRideMsg ? overRideMsg: "For the ${STdeviceName} color controller, remember it can be operated like a switch. You can ask me to turn it on, off, toggle "+  
                    "the on and off states, or set a brightness level. You can also set it to a variety of colors. For listing of these colors, simply print out the Ask Alexa cheat sheet. %1%"
                }
            }
            if (type == "music"){             
                log.debug param==~/undefined|null/
                if ((op ==~/increase|raise|up|decrease|down|lower/)){
                     def newValues = upDown(STdevice, type, op, num,STdeviceName) 
                     num = newValues.newLevel
                     if (num==0) op= "off"
                }
                if ((num != 0 && speakerHighLimit && num > speakerHighLimit)|| (op=="maximum" && speakerHighLimit)) num = speakerHighLimit    
                if (op==~/off|stop/) { STdevice.stop(); result = "I am turning off the ${STdeviceName}. " }
                else if (op ==~/play|on/ && param==~/undefined|null/) { 
                    STdevice.play()
                    result = "I am playing the ${STdeviceName}. " 
                }
                else if (op ==~/play|on|undefined|null/  && param!="undefined" && param!="null") { 
                	if (sonosCMD && sonosMemoryCount){
                        def memCount = sonosMemoryCount as int, song = ""
        				for (int i=1; i<memCount+1; i++){ 
                        	 if (settings."sonosSlot${i}Name" && settings."sonosSlot${i}Music" && settings."sonosSlot${i}Name".replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() == param) {
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
            	else if (op=="previous track") { STdevice.previousTrack(); result = "I am playing the previous track on the ${STdeviceName}. " }
                else result = "For the ${STdeviceName}, valid commands include 'play','pause','mute'. %1%"
                if (num > 0) { STdevice.setLevel(num); result = "I am setting the volume of the ${STdeviceName} to ${num}%. " }
                if (speakerHighLimit && num == speakerHighLimit) result += "This is the maximum volume level you have set up. %1%"
                if (op=="maximum" && !speakerHighLimit) result = "You have not set a maximum volume level in the SmartApp. %1%"
            }
            if (type=~/door|lock|shade/){
                if ((type == "door" && !doorVoc().find{it==op}) || (type == "shade" && !shadeVoc().find{it==op})) result= "For the ${STdeviceName}, you must give an 'open' or 'close' command. %1%"
            	else if (type == "lock" && !lockVoc().find{it==op}) result= "For the ${STdeviceName}, you must give a 'lock' or 'unlock' command. %1%"
            	else if (type == "shade" && STdevice.currentValue("windowShade")==op || STdevice.currentValue("windowShade")=="closed" && op=="close"){
					 result = "The ${STdeviceName} is already ${currentShadeState}. %1%"
                }
                else STdevice."$op"(); result =  "I am ${op}ing the ${STdeviceName}. "
            }    
            if (type == "presence" && vPresenceCMD){
            	log.debug param
            	def currentState = STdevice.currentValue(type)
                if (((op=="on" || param=~/home|present|check in|checkin|arrive/) && currentState=="present") || ((op=="off" || param=~/away|not present|check out|checkout|depart|gone/) && currentState=="not present")) result = "The '${STdeviceName}' presence sensor is already set to '${currentState}'. %1%"
                else if ((op=="on" || param=~/home|present|check in|arrive/) && (currentState=="not present" || !currentState)) {
					if (supportedCMDs.name.contains("present")) STdevice.present() 
                    if (supportedCMDs.name.contains("arrived")) STdevice.arrived()
                    result = (supportedCMDs.find{it.name==~/present|arrived/}) ? "I am setting the presence of, '${STdeviceName}, to 'present'. ":""
                }
                else if ((op=="off" || param=~/away|not present|check out|checkout|depart|gone/) && (currentState=="present" || !currentState)) {
                    if (supportedCMDs.name.contains("away")) STdevice.away() 
                    if (supportedCMDs.name.contains("departed")) STdevice.departed()
                    result = (supportedCMDs.find{it.name==~/away|departed/})  ? "I am setting the presence of, '${STdeviceName}, to 'away'. " :""
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
        if (healthWarn && STdevice.status=="OFFLINE") result +="This device's status is reporting offline. "
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
        if (briefReplyTxt=="User-defined" && briefReplyCustom) reply = briefReplyCustom
        result = reply ? reply + ". " + batteryWarnTxt + "%7%" : batteryWarnTxt + "%7%"
    }
    if (!result) result = "I had a problem understanding your request. %1%"
    return result
}
def displayData(display){
	render contentType: "text/html", data: """<!DOCTYPE html><html><head><meta charset="UTF-8" /><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/></head><body style="margin: 0;">${display}</body></html>"""
}
//Child code pieces here---Macro Handler-------------------------------------
def macroResults(num, cmd, colorData, param, mNum,xParam){ 
    String result="", feedData=""   
    if (macroType == "Control") result = controlResults(num)
    if (macroType == "Group") result = groupResults(num, cmd, colorData, param, mNum)
	if (macroType == "CoRE") result = WebCoREResults(num, xParam)
    else if (macroType =="Group") {
    	def data = result ? result.endsWith("%") ? [alexaOutput: result[0..-4]] : [alexaOutput: result] : [alexaOutput: "No Output"]
        sendLocationEvent(name: "askAlexa", value: app.id, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa activated '${app.label}' macro.")
	}
    return result
}
//Group Handler
def groupResults(num, op, colorData, param, mNum){   
    def grpType = [switch:"switch", switchLevel:"dimmer", colorTemperature: "temperature light", colorControl:"colored light", windowShade:"window shade", doorControl: "door"][groupType]?:groupType
    String result = ""
    try {
        def noun= settings."groupDevice${groupType}"?.size()==1 ? grpType : grpType+"s"
        if (grpType=="switch" && settings."groupDevice${groupType}"?.size()>1) noun = "switches"
        def verb=settings."groupDevice${groupType}"?.size()==1 ? "is" : "are"
        String valueWord, proNoun = settings."groupDevice${groupType}"?.size()==1 ? "its" : "their"
        if (!settings."groupDevice${groupType}"?.size()) result = "There are no devices present in the device group, '${app.label}. %1%"
        else if (op == "list") result = "The following ${noun} ${verb} in the '${app.label}' device group: "+ getList(settings."groupDevice${groupType}") +". "
        else if (groupType=="switch"){
            if (op ==~/on|off/) { settings."groupDevice${groupType}"?."$op"();result = voicePost && !noAck ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " : "I am turning ${op} the ${noun} in the group named '${app.label}'. " }
            else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost && !noAck? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " } 
            else if (op==~/undefined|status|null/) {
				if (!settings."groupDevice${groupType}"?.currentValue("switch").contains("off")) result = "All of the switches in the device group, '${app.label}', are on. "
                else if (!settings."groupDevice${groupType}"?.currentValue("switch").contains("on")) result = "All of the switches in the device group, '${app.label}', are off. "
                else settings."groupDevice${groupType}".each{ result += "The ${it.label} is ${it.currentValue('switch')}. " }
            }
            else result = "For a switch device group, be sure to give an 'on', 'off', 'toggle' or 'status' command. %1%" 
        }
        else if (groupType==~/switchLevel|colorControl|colorTemperature/){
            num = num < 0 ? 0 : num >99 ? 100 : num
            if (op == "maximum") { num = 100; op ="undefined"; valueWord= "${proNoun} maximum brightness" }
            else if (op==~/low|medium|high/ && groupType=="switchLevel") { valueWord="${op}, or a value of ${num}%"; op ="undefined" }
            else if (op==~/low|medium|high/ && groupType==~/colorControl|colorTemperature/ && !colorData ) { valueWord="${op}, or a value of ${num}%"; op ="undefined" }
            else valueWord = "${num}%"
            if (num==0 && op==~/undefined|null/ && param==~/undefined|null/ && mNum!="undefined" && mNum!="null") op="off"
            if (op ==~/on|off/){ 
            	settings."groupDevice${groupType}"?."$op"()
                result = voicePost ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " :  "I am turning ${op} the ${noun} in the group named '${app.label}'. "
            }
            else if (op == "toggle") { toggleState(settings."groupDevice${groupType}");result = voicePost ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " : "I am toggling the ${noun} in the group named '${app.label}'. " }
            else if (groupType=="switchLevel" && num > 0 && op ==~/undefined|null/) { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " : "I am setting the ${noun} in the group named '${app.label}' to ${valueWord}. " }
            else if (groupType==~/colorControl|colorTemperature/ && num > 0 && !colorData && op ==~/undefined|null/) { settings."groupDevice${groupType}"?.setLevel(num); result = voicePost && !noAck  ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") :  noAck ? " " :"I am setting the ${noun} in the '${app.label}' group to ${valueWord}. " }
            else if (groupType=="colorControl" && colorData && param) {
                colorData = [hue:colorData.hue, saturation: colorData.saturation]
                settings."groupDevice${groupType}"?.setColor(colorData)
                if (num>0) settings."groupDevice${groupType}"?.setLevel(num)
                if (!voicePost && !noAck){
                    result ="I am setting the ${noun} in the '${app.label}' group to ${param}"
                    result += num ==100 ? " and ${proNoun} maximum brightness" : num>0 ? ", at a brightness level of ${num}%" : ""
                    result += ". "
                }
                else if (voicePost && !noAck)  result = parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") 
                else result = " "
            }
            else if (groupType=="colorTemperature" && param !="undefined" && param !="null") {
            	def sWhite = parent.kSoftWhite ? parent.kSoftWhite as int : 2700, wWhite= parent.kWarmWhite ? parent.kWarmWhite as int: 3500, 
                	cWhite= parent.kCoolWhite ? parent.kCoolWhite as int: 4500, dWhite= parent.kDayWhite ? parent.kDayWhite as int: 6500
                def kelvin = param=="soft white" ? sWhite : param=="warm white" ? wWhite : param=="cool white" ? cWhite : param=="daylight white" ? dWhite : 9999
                if (kelvin <9999){
					settings."groupDevice${groupType}"?.setColorTemperature(kelvin)
					if (!voicePost && !noAck){
                    	result = "I am setting the temperature of the ${noun} in the '${app.label}' group to ${param}, or ${kelvin} degrees Kelvin"
						result += num ==100 ? " and ${proNoun} maximum brightness" : num>0 ? ", at a brightness level of ${num}%" : ""
                        result += ". "
					}
                }
                else if (kelvin ==9999) result = "I didn't understand the temperature you wanted to set the '${app.label}' device group. Valid temperatures are soft white, warm white, cool white and daylight white. %1%"
                else if (voicePost && !noAck)  result = parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") 
                else result = " "
            }
            else if (op ==~/increase|raise|up|brighten|decrease|down|lower|dim/){
                if (parent.lightAmt){
                    settings."groupDevice${groupType}".each{ upDownChild(it, op, num, "level") }
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
            else if (op==~/undefined|status|null/){
            	def grpCtl = groupType=="switchLevel" ? "dimmer" : groupType=="colorControl" ? "colored light" : "temperature control light"
                if (!settings."groupDevice${groupType}"?.currentValue("switch").contains("off")) result = "All of the ${grpCtl}s in the device group, '${app.label}', are on. "
                else if (!settings."groupDevice${groupType}"?.currentValue("switch").contains("on")) result = "All of the ${grpCtl}s in the device group, '${app.label}', are off. "
                else settings."groupDevice${groupType}".each{ 
                	result += "The ${it.label} is ${it.currentValue('switch')}"
                    result += it.currentValue('switch')=="on" ? " and set to ${it.currentValue('level')}. " : ". "
                }
            }
            else {
                if (groupType=="switchLevel") result = "For a dimmer group, be sure to use an 'on', 'off', 'toggle', 'status' or brightness level setting. %1%" 
                else if (groupType=="colorControl") result = "For a colored light group, be sure to give me an 'on', 'off', 'toggle', 'status', brightness level or color command. %1%"
                else if (groupType=="colorTemperature") result = "For a temperature control light group, be sure to give me an 'on', 'off', 'toggle', 'status', brightness level or temperature command. %1%"
        	}
        }
        else if (groupType=="lock"){
            noun=settings."groupDevice${groupType}".size()==1 ? "device" : "devices"
            if (op ==~ /lock|unlock/){         
                settings."groupDevice${groupType}"?."$op"()
                result = voicePost && !noAck ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " : "I am ${op}ing the ${noun} in the group named '${app.label}'. " 
            }
            else if (op==~/undefined|status|null/) {
				if (!settings."groupDevice${groupType}"?.currentValue("lock").contains("locked")) result = "All of the locks in the device group, '${app.label}', are unlocked. "
                else if (!settings."groupDevice${groupType}"?.currentValue("lock").contains("unlocked")) result = "All of the locks in the device group, '${app.label}', are locked. "
                else settings."groupDevice${groupType}".each{ result += "The ${it.label} is ${it.currentValue('lock')}. " }
            }
            else result = "For a lock device group, you must use a 'lock', 'unlock' or 'status' command. %1%" 
        }
        else if (groupType==~/doorControl|windowShade/){
        	def grpCtl = groupType=="doorControl" ? "door" : "windowShade", grpName = groupType=="doorControl" ? "door" : "shade"
            if (op ==~ /open|close/){
                settings."groupDevice${groupType}"?."$op"()
                def condition = op=="close" ? "closing" : "opening"
                result = voicePost && !noAck  ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " :  "I am ${condition} the ${noun} in the group named '${app.label}'. "
            }
            else if (op==~/undefined|status|null/) {
                if (!settings."groupDevice${groupType}"?.currentValue("${grpCtl}").contains("open") && !settings."groupDevice${groupType}"?.currentValue("${grpCtl}").contains("Open")) result = "All of the ${grpName}s in the device group, '${app.label}', are closed. "
                else if (!settings."groupDevice${groupType}"?.currentValue("${grpCtl}").contains("closed") && !settings."groupDevice${groupType}"?.currentValue("${grpCtl}").contains("Closed")) result = "All of the ${grpName}s in the device group, '${app.label}', are open. "
                else settings."groupDevice${groupType}".each{ result += "The ${it.label} is ${it.currentValue(grpCtl).toLowerCase()}. " }
            }
            else result = "For a ${grpCtl} device group, you must use an 'open', 'close' or 'status' command. %1%" 
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
                if (param ==~/undefined|null/ && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning on a thermostat group. %1%"
                if (param =~/heat/) {result="I am setting the ${noun} to 'heating' mode. "; settings."groupDevice${groupType}"?.heat()}
                if (param =~/cool|AC/) {result="I am setting the ${noun} to 'cooling' mode. "; settings."groupDevice${groupType}"?.cool()}
                if (param =~/auto/) {result="I am setting the ${noun} to 'auto' mode. Please note, to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints separately. " ; settings."groupDevice${groupType}"?.auto()}
                if (op == "off") { result = "I am turning off the ${noun}. "; settings."groupDevice${groupType}"?.off() }
                if (parent.stelproCMD && (param=="eco" || param=="comfort")){ result="I am setting the ${noun} to '${param}' mode. "; settings."groupDevice${groupType}"?.setThermostatMode("${param}") }
                if (param==~/home|present/ && parent.nestCMD) { result="I am setting the ${noun} to 'home' mode. "; settings."groupDevice${groupType}"?.present() }
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
                    result="I am resuming the climate program of the ${noun}. "
                    parent.MyEcobeeCMD ? settings."groupDevice${groupType}"?.resumeThisTstat() : settings."groupDevice${groupType}"?.resumeProgram()
                } 
            }
            else if (op ==~/increase|raise|up|decrease|down|lower/) result = "Increase and decrease commands are not yet compatible with thermostat device group macros. %1%"
            else if (op==~/undefined|status|null/ && param ==~ /undefined|null/) settings."groupDevice${groupType}".each{ result += "The ${it.label} is reading ${it.currentValue('temperature')} degrees. " }
            else {
                param = tstatDefaultCool && param ==~/undefined|null/ ? "cool" : tstatDefaultHeat && param ==~ /undefined|null/ ? "heat" : param
                if (param  ==~ /undefined|null/) result = "You must designate a 'heating' or 'cooling' parameter when setting the temperature of a thermostat device group. %1%"
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
                if (param !="undefined" && param !="null" && parent.getTstatLimits().hi && num >= parent.getTstatLimits().hi) result += "This is the maximum temperature I can set for this device group. "
                if (param !="undefined" && param !="null"  && parent.getTstatLimits().lo && num <= parent.getTstatLimits().lo) result += "This is the minimum temperature I can set for this device group. "
            }
        }
        result = voicePost && !noAck ? parent.replaceVoiceVar(voicePost,"","",macroType,app.label,0,"") : noAck ? " " : result
	}
    catch(e) { result = "There was a problem controlling the device group named '${app.label}'. Be sure it is configured correctly within the SmartApp. %1%" }
    return result
}
//WebCoRE Handler-----------------------------------------------------------
def WebCoREResults(sDelay,xParam){	
	String result = ""
    state.xParam = (xParam !="undefined" && xParam!="?" && pxParam !="null" ) ? xParam : ""
    def xParamLog = state.xParam ? " with the extra parameter: ${state.xParam}" : "", delay
    if (cDelay>0 || sDelay>0) delay = sDelay==0 ? cDelay as int : sDelay as int
    if (!advWebCore || (advWebCore && state.xParam)){
        result = (!delay || delay == 0) ? "I am triggering the WEBCORE macro named '${app.label}'${xParamLog}. " : delay==1 ? "I'll trigger the '${app.label}' WEBCORE macro in ${delay} minute. " : "I'll trigger the '${app.label}' WEBCORE macro in ${delay} minutes. "
            if (sDelay == 9999) { 
            result = "I am cancelling all scheduled executions of the WEBCORE macro, '${app.label}'. "  
            state.scheduled = false
            unschedule() 
        }
        if (!state.scheduled) {
            def xParamVar = state.xParam
            if (!delay || delay == 0) WebCoREHandler() 
            else if (delay < 9999) { runIn(delay*60, WebCoREHandler, [overwrite: true]) ; state.scheduled=true }
            if (delay < 9999) result = voicePost && !noAck ? parent.replaceVoiceVar(voicePost, delay,"",macroType,app.label, 0, xParamVar) : noAck ? " " : result
        }
        else result = "The WEBCORE macro, '${app.label}', is already scheduled to run. You must cancel the execution or wait until it runs before you can run it again. %1%"
    }
    else {
    	if (advWebCore && voicePostAdv) result = parent.replaceVoiceVar(voicePostAdv, delay,"",macroType,app.label, 0, xParamVar)
        else result = "I did not hear the required additional parameter required to execute the macro, '${app.label}'. The execution was aborted. %1%"
	}
    return result
}
def WebCoREHandler(){ 
	state.scheduled = false
    def data = state.xParam ? [xParam : state.xParam] : null
    parent.webCoRE_execute(CoREName,data)
    state.xParam=""
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
            else if (delay < 9999) { runIn(delay*60, controlHandler, [overwrite: true]) ; state.scheduled=true }
            if (delay < 9999) result = voicePost && !noAck ? parent.replaceVoiceVar(voicePost, delay,"",macroType,app.label,0,"") : noAck ? "" : result
		}
        else result = "The control macro, '${app.label}', is already scheduled to run. You must cancel the execution or wait until it runs before you can run it again. %1%"
    }
    else result="The control macro, '${app.label}', is not properly configured. Use your SmartApp to configure the macro. %1%"
    return result
}
def controlHandler(){
   	state.scheduled = false
   	def cmd = [switch: switchesCMD, dimmer: dimmersCMD, cLight: cLightsCMD, cLightK: cLightsKCMD, tstat: tstatsCMD, lock: locksCMD, garage: garagesCMD, shade: shadesCMD]
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
        else if (cmd.dimmer ==~/increase|decrease/) dimmers.each {upDownChild(it, cmd.dimmer, dimmersUpDown as int, "level")}
        else cmd.dimmer == "toggle" ? toggleState(dimmers) : dimmers?."${cmd.dimmer}"()
    }
    if (cLights && cmd.cLight){
    	if (cmd.cLight == "set"){
            if (cLightsCLR || cLightsLVL) {
            	def level = !cLightsLVL || cLightsLVL < 0 ? 0 : cLightsLVL >100 ? 100 : cLightsLVL as int
            	cLightsCLR ? parent.setColoredLights(cLights, cLightsCLR, level) : cLights?.setLevel(level)
        	}
        }
        else if (cmd.cLight ==~/increase|decrease/) cLights.each {upDownChild(it, cmd.cLight, cLightsUpDown as int, "level")}
        else if (cmd.cLight == "toggle") toggleState(cLights)	
        else cLights?."${cmd.cLight}"()
    }
    if (cLightsK && cmd.cLightK){
    	if (cmd.cLightK == "set"){
            if (cLightsKEL || cLightsKLVL) {
                def level = !cLightsKLVL || cLightsKLVL < 0 ? 0 : cLightsKLVL >100 ? 100 : cLightsKLVL as int
                if (cLightsKEL) cLightsK?.setColorTemperature(cLightsKEL as int) 
                if (level>0) cLightsK?.setLevel(level)
            }
        }
        else if (cmd.cLightK ==~/increase|decrease/) cLightsK.each {upDownChild(it, cmd.cLightK, cLightsKUpDown as int, "level")}
        else if (cmd.cLightK == "toggle") toggleState(cLightsK)	
        else cLightsK?."${cmd.cLightK}"()
    }
    if (locks && cmd.lock) locks?."${cmd.lock}"()
    if (tstats && cmd.tstat){
        if ((cmd.tstat == "heat" || cmd.tstat == "cool") && tstatLVL) {
        	def tLevel = tstatLVL < 0 ?  0 : tstatLVL > 100 ? 100 : tstatLVL as int
    		cmd.tstat == "heat" ? tstats?.setHeatingSetpoint(tLevel) : tstats?.setCoolingSetpoint(tLevel)
        }
        else if (cmd.tstat=~/increase|decrease/) tstats.each {upDownChild(it, cmd.tstat, tstatUpDown as int, "temperature")}
        def ecobeeCustomRegEx = parent.MyEcobeeCMD && parent.ecobeeCMD ? getEcobeeCustomRegEx(tstats) : null 
		if (cmd.tstat =~ /present|away|sleep|resumeProgram/ || (ecobeeCustomRegEx && cmd.tstat =~ /${ecobeeCustomRegEx}/)) tstats?."${cmd.tstat}"() 
	}
    if (garages && cmd.garage) garages?."${cmd.garage}"()
    if (shades && cmd.shade) shades?."${cmd.shade}"()
    if (extInt == "0" && http) httpGet(http) 
	if (extInt == "1" && ip && port && command){
        String hexIP = ip.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
        String hexPort = port.toString().format( '%04x', port.toInteger() )
		def deviceHexID = hexIP +":"+ hexPort
        log.info "Device Network Id set to ${deviceHexID}"
        sendHubCommand(new physicalgraph.device.HubAction("""GET /${command} HTTP/1.1\r\nHOST: ${ip}:${port}\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceHexID}"))    
    }
    if (SHM) sendLocationEvent(name: "alarmSystemStatus", value: SHM)
    def data = [args: "I am activating the Control Macro: '${app.label}'."]
    sendLocationEvent(name: "askAlexa", value: app.id, data: data, displayed: true, isStateChange: true, descriptionText: "Ask Alexa activated '${app.label}' macro.")	
    if (ctlMsgQue){    
        def expireMin=cltMQExpire ? cltMQExpire as int : 0, expireSec=expireMin*60
        def overWrite =!ctlMQNotify && !ctlMQExpire && ctlMQOverwrite
        def msgTxt = ttsMsg?: "Ask Alexa activated Control Macro: '${app.label}'."
        sendLocationEvent(
            name: "AskAlexaMsgQueue", 
            value: "Ask Alexa Control Macro, '${app.label}'",
            unit: "${app.id}",
            isStateChange: true, 
            descriptionText: msgTxt, 
            data:[
                queues:ctlMsgQue,
                overwrite: overWrite,
                notifyOnly: ctlMQNotify,
                expires: expireSec,
                suppressTimeDate:ctlSuppressTD   
            ]
        )
	}
}
private getEcobeeCustomRegEx(myEcobeeGroup){ 
    def myCustomClimate = "" 
    try {
    	getEcobeeCustomList(myEcobeeGroup).each { myCustomClimate += "|${it}" } 
    	myCustomClimate = myCustomClimate[1..myCustomClimate.length() - 1] 
	}
    catch (e){ log.warn "An error was encountered when attempting to send commands to the '${myEcobeeGroup}'. If you don't have any Ecobee devices please disable these features in the Settings>>Device Specific Commands."}                        
    return myCustomClimate 
}
//Parent Code Access (from Child)-----------------------------------------------------------
def cLightsKCTLOptions(){
	return ["on":"Turn on","off":"Turn off","set":"Set temperature and level", "toggle":"Toggle the lights' on/off state","decrease": "Decrease current brightness","increase": "Increase current brightness"]
}
def cLightsCTLOptions(){
	def options=["on":"Turn on","off":"Turn off","set":"Set color and level", "toggle":"Toggle the lights' on/off state","decrease": "Decrease current brightness","increase": "Increase current brightness"]
    if (osramCMD) options +=["loopOn":"Turn on color loop","loopOff":"Turn off color loop","pulseOn":"Turn on pulse","pulseOff":"Turn off pulse" ]
    return options
}
def tStatCTLOptions(){
	def tstatOptions=["heat":"Set heating temperature","cool":"Set cooling temperature","increaseHeat":"Increase current heating setpoint","decreaseHeat":"Decrease current heating setpoint","increaseCool":"Increase current cooling setpoint", "decreaseCool":"Decrease current cooling setpoint"]
	if (nestCMD) tstatOptions += ["away":"Nest 'Away' Presence","present":"Nest 'Home' Presence"]
	if (ecobeeCMD) tstatOptions += ["away":"Ecobee 'Away' Climate","home":"Ecobee 'Home' Climate","sleep":"Ecobee 'Sleep' Climate","resumeProgram":"Ecobee 'Resume Program'"]
	if (MyEcobeeCMD) getEcobeeCustomList(tstats).each { tstatOptions += ["${it}":"Ecobee '${it}' Climate"] }
	return tstatOptions
}
//Common Code (Child and Parent)
def mqRefresh(evt){ sendLocationEvent(name: "askAlexaMQ", value: "refresh", data: [queues: getMQListID(false)] , isStateChange: true, descriptionText: "Ask Alexa message queue list refresh") }
def childQDelete(qList){
	qList.each{qID->
        def qNameRun = getAAMQ().find{it.id == qID}
        if (qNameRun) { qNameRun.qDelete() }
	}
}
def qDelete(){
	def deleteList = state.msgQueue.findAll{it.trackDelete}
    state.msgQueue=[]
    if (deleteList){
    	deleteList.each{
			sendLocationEvent(name:"askAlexaMQ", value: "${it.appName}.${it.id}",isStateChange: true, data:[[deleteType: "delete all"],[queue:"Primary message queue"]], descriptionText:"Ask Alexa deleted all messages from the Primary message queue")
        }
	}
	if (msgQueueNotifyLightsOn && msgQueueNotifyLightsOff) msgQueueNotifyLightsOn?.off()
    if (msgQueueNotifycLightsOn && msgQueueNotifyLightsOff) msgQueueNotifycLightsOn?.off()
}
def getOkToRun(){ def result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd) && getPeopleOk(runPeople,runPresAll) }
def battOptions() { return  [5:"<5%",10:"<10%",20:"<20%",30:"<30%",40:"<40%",50:"<50%",60:"<60%",70:"<70%",80:"<80%",90:"<90%",101:"Always play battery level"] }
def kelvinOptions(){ return ["${parent.kSoftWhite}" : "Soft White (${parent.kSoftWhite}K)", "${parent.kWarmWhite}" : "Warm White (${parent.kWarmWhite}K)", 
    "${parent.kCoolWhite}": "Cool White (${parent.kCoolWhite}K)", "${parent.kDayWhite}" : "Daylight White (${parent.kDayWhite}K)"] 
} 
def imgURL() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/" }
def getAskAlexa(){ return findAllChildAppsByNamespaceAndName("MichaelStruck", "Ask Alexa") }
def getAAMQ() { return findAllChildAppsByNamespaceAndName("MichaelStruck", "Ask Alexa Message Queue") }
def getSCHD() { return findAllChildAppsByNamespaceAndName("MichaelStruck", "Ask Alexa Schedule") }
def getWR() { return findAllChildAppsByNamespaceAndName("MichaelStruck", "Ask Alexa Weather Report") }
def getVR() { return findAllChildAppsByNamespaceAndName("MichaelStruck", "Ask Alexa Voice Report") }
def macAliasCount() { return 3 }
def getList(items){
	def result = "", itemCount=items.size() as int
	items.each{ result += it; itemCount --
		result += itemCount>1 ? ", " : itemCount==1 ? " and " : ""
    }
	return result
}
void nestCmdPrep(dev) {
    try { if(dev.currentValue("devTypeVer") != null) { return } else { dev.poll() } }
	catch(e) { log.error "nestCmdPrep Exception: $e" }
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
def sendMSG(num, msg, push, recipients){
    if (location.contactBookEnabled && recipients) sendNotificationToContacts(msg, recipients)
    else {
    	if (num) {sendSmsMessage(num,"${msg}")}
    	if (push) {sendPushMessage("${msg}")}
    }
}
def timeDate(dateNum){
	def today = new Date(now()).format("EEEE, MMMM d, yyyy", location.timeZone)
	def msgDay = new Date(dateNum).format("EEEE, MMMM d, yyyy", location.timeZone)
    def voiceDay = today == msgDay ? "Today" : msgDay
    def msgTime = new Date(dateNum).format("h:mm aa", location.timeZone)
    return ["msgTime": msgTime, "msgDay": voiceDay]
}
def soundFXList(){ 
	return [1:"Radio Announcer", 2:"Dr. Evil", 3:"George Carlin", 4:"Hal", 5:"Worf",6:"Pac Man",7:"R2-D2",8:"Yoda",9:"AOL",10:"Message Tone 1",11:"Message Tone 2",12:"Message Tone 3",13:"Message Tone 4","custom":"Custom-User Defined"] 
}
def sfxLookup(sfx){
	def result 
    if (sfx=="1") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/checkyourmailbox.mp3", duration:"6"]
	else if (sfx=="2") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/dr-evil-youve-got-freakin-mail.mp3", duration:"2"]
    else if (sfx=="3") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/georgecarlin.mp3", duration:"3"]
    else if (sfx=="4") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/hal2001.mp3", duration:"2"]
    else if (sfx=="5") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/MAILWORF.mp3", duration:"4"]
    else if (sfx=="6") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/pacman.mp3", duration:"5"]
    else if (sfx=="7") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/R2D2-yeah.mp3", duration:"2"]
    else if (sfx=="8") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/yoda-message-from-the-darkside.mp3", duration:"4"]
    else if (sfx=="9") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/youve-got-mail-sound.mp3", duration:"2"]
    else if (sfx=="10") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/Tone1.mp3", duration:"2"]
    else if (sfx=="11") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/Tone2.mp3", duration:"4"]
    else if (sfx=="12") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/Tone4.mp3", duration:"2"]
    else if (sfx=="13") result = [uri: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/media/Tone4.mp3", duration:"3"]
    else if (sfx=="custom") result = [uri:"${mqAlertCustom}",duration:"10"]
 	return result   
}
//Common Code(Child)-----------------------------------------------------------
def ctlMQDesc(){
    def result = "Tap to add/edit the message queue options"
    if (ctlMsgQue){
    	result = "Send to: ${translateMQid(ctlMsgQue)}"
        result += ctlMQNotify ? "\nNotification Mode Only" : ""
        result += ctlMQExpire ? "\nExpires in ${ctlMQExpire} seconds" : ""
        result += ctlMQOverwrite ? "\nOverwrite all previous voice report messages" : ""
        result += ctlSuppressTDRemind ? "\nSuppress Time and Date from Alexa Playback" : ""
	}
    return result
}
def translateMQid(mqIDList){
	def result=mqIDList.contains("Primary Message Queue")?["Primary Message Queue"]:[], qName
	mqIDList.each{qID->
    	qName = parent.getAAMQ().find{it.id == qID}	
    	if (qName) result += qName.label
    }
    return parent.getList(result)
}
def deleteChild(id){
    def child = getChildApps().find{ it.id == id }
	if (child) {log.info "Deleting schedule, '${child.label}'"; app.deleteChildApp(child) }
}
def upDownChild(device, op, num, type){
    def numChange, newLevel, currLevel, defMove
    if (type=="level") defMove = parent.lightAmt as int ?: 0 ; currLevel = device.currentValue("switch")=="on" ? device.currentValue("level") as int : 0
    if (type=="temperature"){
    	defMove = parent.tstatAmt as int ?: 5
        try{
        	if (parent.nestCMD) nestCmdPrep(device)
        	if (op=~/Cool/ && (device.currentValue("thermostatMode")=="auto" || device.currentValue("thermostatMode")=="cool")) currLevel = device.currentValue("coolingSetpoint")
        	if (op=~/Heat/ && (device.currentValue("thermostatMode")=="auto" || device.currentValue("thermostatMode")=="heat")) currLevel = device.currentValue("heatingSetpoint")
		}
        catch (e) { log.warn "There was an error reading the thermostat. Ensure you have the proper mode set on the thermostat for the setpoint you are attempting to set." }
    }
    if (op =~/increase|raise|up|brighten/)  numChange = num == 0 ? defMove : num > 0 ? num : 0
    if (op =~/decrease|down|lower|dim/) numChange = num == 0 ? -defMove : num > 0 ? -num  : 0
    newLevel = currLevel + numChange; newLevel = newLevel > 100 ? 100 : newLevel < 0 ? 0 : newLevel
    if (type=="level"){
    	if (defMove>0) device.setLevel(newLevel)
    	if (newLevel==0) device.off()
    }
    if (type=="temperature"){
        if (op=~/Cool/) device.setCoolingSetpoint(newLevel)
        else if (op=~/Heat/) device.setHeatingSetpoint(newLevel)       
    }
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
private getPeopleOk(peopleList,presType){
	def result = true
    if (presType && peopleList) result = peopleList.find {it.currentPresence == "not present"} ? false : true
    else if (!presType && peopleList) result = peopleList.find {it.currentPresence == "present"} ? true : false
    result
}
def getTimeLabel(start, end){
	def timeLabel = "Tap to set"
    if(start && end) timeLabel = "Between " + timeParse("${start}", "h:mm a") + " and " +  timeParse("${end}", "h:mm a")
    else if (start) timeLabel = "Start at " + timeParse("${start}", "h:mm a")
    else if (end) timeLabel = "End at " + timeParse("${end}", "h:mm a")
	return timeLabel	
}
def macroAliasDesc(){
	def result =""
	for (int i= 1; i<macAliasCount()+1; i++){
		result += settings."macAlias${i}" ? settings."macAlias${i}" : ""
		result += (result && settings."macAlias${i+1}") ? "\n" : ""
	}
    result = result ? "Alias Names currently configured; Tap to edit:\n"+result :"Tap to add alias names to this macro"
    return result
}
def macroAliasState(){
	def count = 0
    for (int i= 1; i<macAliasCount()+1; i++){
    	if (settings."macAlias${i}") count ++
    }
    return count ? "complete" : null
}
def macroTypeDesc(){
	def desc = "", PIN = (macroType ==~/CoRE|Control|GroupM/ || (macroType == "Group" && groupType == "lock") || (macroType == "Group" && groupType == "doorControl") || desc) && usePW ? " - PIN Required" : ""
    def customAck = !noAck && !voicePost ? "; uses standard acknowledgment message" : !noAck  && voicePost ? "; includes a custom acknowledgment message" :  "; there will be no acknowledgment messages"
    if (macroType ==~ /Control|CoRE/) customAck += cDelay>1 ? "; activates ${cDelay} minutes after triggered" : cDelay==1 ? "; activates one minute after triggered" : ""
    if (macroType == "Control" && (phrase || setMode || SHM || getDeviceDesc() != "Status: UNCONFIGURED${PIN} - Tap to configure" || getHTTPDesc() !="Status: UNCONFIGURED - Tap to configure" || ctlMsgQue)) desc= "Control Macro CONFIGURED${customAck}${PIN} - Tap to edit" 
	if (macroType =="Group" && groupType && settings."groupDevice${groupType}") {
    	def groupDesc =[switch:"Switch Group",switchLevel:"Dimmer Group",colorTemperature: "Temperature (Kelvin) Light Group", thermostat:"Thermostat Group",colorControl:"Colored Light Group",lock:"Lock Group",doorControl: "Door Group",windowShade: "Window Shade Group"][groupType] ?: groupType
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
        desc = "Extension Group CONFIGURED with ${countDesc}${customAck}${PIN} - Tap to edit" 
    }
    def webCoreName = parent.webCoRE_list().find{it.id==CoREName}
    if (macroType =="CoRE" && CoREName && webCoreName) desc = "Trigger '${webCoreName.name}' piston${customAck}${PIN} - Tap to edit"
    else if (macroType =="CoRE" && CoREName && !webCoreName) desc = "WebCoRE piston has changed or deleted; Macro will not operate - Tap to reselect the proper piston"
    return desc ? desc : "Status: UNCONFIGURED - Tap to configure macro"
}
def greyOutMacro(){ return macroTypeDesc() == "Status: UNCONFIGURED - Tap to configure macro" ? "" : "complete" }
def greyOutStateHTTP(){ return getHTTPDesc() == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete" }
def deviceGreyOut(){ return getDeviceDesc() == "Status: UNCONFIGURED - Tap to configure" ? "" : "complete" }
def getDeviceDesc(){
    def result, cmd = [switch: switchesCMD, dimmer: dimmersCMD, cLight: cLightsCMD, cLightK: cLightsKCMD, tstat: tstatsCMD, lock: locksCMD, garage: garagesCMD, shade: shadesCMD]
	def lvl = cmd.dimmer == "set" && dimmersLVL ? dimmersLVL as int : 0
	def cLvl = cmd.cLight == "set" && cLightsLVL ? cLightsLVL as int : 0
    def kLvl = cmd.cLightK == "set" && cLightsKLVL ? cLightsKLVL as int : 0
	def clr = cmd.cLight == "set" && cLightsCLR ? cLightsCLR  : ""
    def kTemp = cmd.cLightK == "set" && cLightsKEL ? cLightsKEL: ""
    def tLvl = tstats ? tstatLVL : 0
    def dimUpDn = cmd.dimmer==~/increase|decrease/ && dimmersUpDown ? dimmersUpDown as int : 0
    def clUpDn = cmd.cLight ==~/increase|decrease/ && cLightsUpDown? cLightsUpDown as int : 0
    def klUpDn = cmd.cLightK ==~/increase|decrease/ && cLightsKUpDown? cLightsKUpDown as int : 0
    def tUpDn = cmd.tstat =~/increase|decrease/ && tstatUpDown ? tstatUpDown as int : 0
    lvl = lvl < 0 ? lvl = 0 : lvl >100 ? 100 : lvl
    tLvl = tLvl < 0 ? 0 : tLvl >100 ? 100 : tLvl
    cLvl = cLvl < 0 ? 0 : cLvl >100 ? 100 : cLvl
    kLvl = kLvl < 0 ? 0 : kLvl >100 ? 100 : kLvl
    dimUpDn = dimUpDn <0 ? 0 : dimUpDn>100 ? 100: dimUpDn
    clUpDn == clUpDn <0 ? 0 : clUpDn>100 ? 100: clUpDn
    klUpDn == klUpDn <0 ? 0 : klUpDn>100 ? 100: klUpDn
	tUpDn == tUpDn <0 ? 0 : tUpDn>100 ? 100: tUpDn
    if (switches || dimmers || cLights || cLightsK || tstats || locks || garages || shades) {
    	result = switches && cmd.switch ? "${switches} set to ${cmd.switch}" : ""
        result += result && dimmers && cmd.dimmer ? "\n" : ""
        result += dimmers && cmd.dimmer && cmd.dimmer != "set" && cmd.dimmer !="increase" && cmd.dimmer !="decrease" ? "${dimmers} set to ${cmd.dimmer}" : ""
        result += dimmers && cmd.dimmer && cmd.dimmer == "set" ? "${dimmers} set to ${lvl}%" : ""
        result += dimmers && cmd.dimmer && cmd.dimmer ==~/increase|decrease/ && dimUpDn>0 ? "${dimmers} ${cmd.dimmer} brightness by ${dimUpDn}%" : ""
        result += result && cLights && cmd.cLight ? "\n" : ""
    	result += cLights && cmd.cLight && cmd.cLight != "set" && cmd.cLight !="increase" && cmd.cLight !="decrease" && cmd.cLight && cmd.cLight != "loopOn" && cmd.cLight != "loopOff" && cmd.cLight !="pulseOn" && cmd.cLight !="pulseOff"? "${cLights} set to ${cmd.cLight}": ""
        result += cLights && cmd.cLight && cmd.cLight == "set" && (clr || cLvl) ? "${cLights} set to " : ""
        result += cLights && cmd.cLight && cmd.cLight == "set" && clr ? "${clr} and " : ""
        result += cLights && cmd.cLight && cmd.cLight == "set" && cLvl >0 ? "${cLvl}%" : ""
        result += cLights && cmd.cLight && cmd.cLight == "set" && clr && (cLvl == 0 || !cLvl) ? "Color Default Brightness" : ""
        result += cLights && cmd.cLight && cmd.cLight ==~/increase|decrease/ && clUpDn>0 ? "${cLights} ${cmd.cLight} brightness by ${clUpDn}%" : ""
        result += cLights && cmd.cLight == "loopOn" ? "${cLights} turn on color loop" : ""
        result += cLights && cmd.cLight == "loopOff" ? "${cLights} turn off color loop" : ""
        result += cLights && cmd.cLight == "pulseOn" ? "${cLights} turn on pulse" : ""
        result += cLights && cmd.cLight == "pulseOff" ? "${cLights} turn off pulse" : ""
        result += result && cLightsK && cmd.cLightK ? "\n" : ""
        result += cLightsK && cmd.cLightK && cmd.cLightK != "set" && cmd.cLightK !="increase" && cmd.cLightK !="decrease" ? "${cLightsK} set to ${cmd.cLightK}": ""
        result += cLightsK && cmd.cLightK && cmd.cLightK == "set" ? "${cLightsK} set to " : ""
        result += cLightsK && cmd.cLightK && cmd.cLightK == "set" && kTemp ? "${kTemp}K and " : ""
        result += cLightsK && cmd.cLightK && cmd.cLightK == "set" && kLvl >0 ? "${kLvl}%" : ""
        result += cLightsK && cmd.cLightK && cmd.cLightK == "set" && kTemp && (kLvl == 0 || !kLvl)? "Current Brightness" : ""
        result += cLightsK && cmd.cLightK && cmd.cLightK ==~/increase|decrease/ && klUpDn>0 ? "${cLightsK} ${cmd.cLightK} brightness by ${klUpDn}%" : ""	
        result += result && tstats && (tLvl || tUpDn) ? "\n" : ""
        result += tstats && cmd.tstat ==~/heat|cool/ && tLvl ? "${tstats} set to ${cmd.tstat}: ${tLvl} degrees" : ""
        result += tstats && cmd.tstat =="increaseCool" && tUpDn ? "${tstats} increase cooling setpoint: ${tUpDn} degrees" : ""
        result += tstats && cmd.tstat =="decreaseCool" && tUpDn ? "${tstats} decrease cooling setpoint: ${tUpDn} degrees" : ""
        result += tstats && cmd.tstat =="increaseHeat" && tUpDn ? "${tstats} increase heating setpoint: ${tUpDn} degrees" : ""
        result += tstats && cmd.tstat =="decreaseHeat" && tUpDn ? "${tstats} decrease heating setpoint: ${tUpDn} degrees" : ""
        if (tstats && (parent.nestCMD || parent.ecobeeCMD || (parent.ecobeeCMD && parent.MyEcobeeCMD))) result += cmd.tstat && cmd.tstat =~/heat|cool|increase|decrease/ ? "": "${tstats} set to: ${cmd.tstat}"	
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
private replaceVoiceVar(msg, delay, filter, type, name, age, xParam) {
	def df = new java.text.SimpleDateFormat("EEEE")
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date()), time = parseDate("","h:mm a"), month = parseDate("","MMMM"), year = parseDate("","yyyy"), dayNum = parseDate("","d")
    def varList = getVariableList(), temp = varList.temp != "undefined device" ? roundValue(varList.temp) + " degrees" : varList.temp
    def humid = varList.humid, people = varList.people
    def fullMacroType=[GroupM: "Extension Group", Control:"Control Macro", Group:"Device Group", Voice:"Voice Report"][type] ?: type
	def fullDeviceType=[colorControl: "Colored Light",switchLevel:"Dimmer" ,doorControl:"Door",lock:"Lock",switch:"Switch",thermostat:"Thermostat"][groupType] ?: groupType
    def delayMin = delay ? delay + " minutes" : "No delay specified"
    msg = macroType=="Group" ? msg.replace('%dtype%', "${fullDeviceType}") : msg.replace('%dtype%', "")
    msg = macroType=="Group" && groupType =="switch" ? msg.replace('%dtypes%', "switches") :  macroType=="Group" && groupType !="switch" ? msg.replace('%dtypes%', "${fullDeviceType}s") : msg.replace('%dtypes%', "")
    msg = msg.replace('%mtype%', "${fullMacroType}")
    msg = msg.replace('%macro%', "${name}")
    msg = msg.replace('%day%', day)
    msg = msg.replace('%date%', "${month} ${dayNum}, ${year}")
    msg = msg.replace('%time%', "${time}")
    msg = msg.replace('%temp%', "${temp}")
    msg = msg.replace('%humid%', "${humid}")
    msg = msg.replace('%people%', "${people}")
    msg = msg.replace('%delay%',"${delayMin}")
    msg = msg.replace('%age%',"${age}")
    msg = msg.replace('%xParam%',"${xParam}")
    if (msg.contains("%random")){
    	def randomList = [], selectRand
        if ((random1A || random1B || random1C) && msg.contains("%random1%")){
        	if (random1A) randomList << random1A 
        	if (random1B) randomList << random1B
        	if (random1C) randomList << random1C 
        	selectRand = randomList[Math.abs(new Random().nextInt() % randomList.size() as int)]
        	msg = msg.replace('%random1%',"${selectRand}")
    	}
        if ((random2A || random2B || random2C) && msg.contains("%random2%")){
        	if (random2A) randomList << random2A 
        	if (random2B) randomList << random2B
        	if (random2C) randomList << random2C 
        	selectRand = randomList[Math.abs(new Random().nextInt() % randomList.size() as int)]
        	msg = msg.replace('%random2%',"${selectRand}")
    	}
        if ((random3A || random3B || random3C) && msg.contains("%random3%")){
        	if (random3A) randomList << random3A 
        	if (random3B) randomList << random3B
        	if (random3C) randomList << random3C 
        	selectRand = randomList[Math.abs(new Random().nextInt() % randomList.size() as int)]
        	msg = msg.replace('%random3%',"${selectRand}")
    	}
    }
    if (getWR().size()){	
        getWR().each{
        	def wrName = "%" + it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") + "%"
            if (msg.contains(wrName)) msg = msg.replace(wrName,processWeatherReport(it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", ""))[0..-4])	
        }
    }
    if (advReportOutput && filter) {
    	def textFilter=filter.toLowerCase().tokenize(",")
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
//Various Functions-----------------------------------------------------------
def toggleState(swDevices){ swDevices.each{ it.currentValue("switch")=="off" ? it.on() : it.off() } }
private setColoredLights(switches, color, level){
    def getColorData = parent ? parent.STColors().find {it.name==color} : STColors().find {it.name==color} 
    def hueColor = getColorData ?  Math.round(getColorData.h / 3.6) : 0, satLevel = getColorData ? getColorData.s:0, newLevel = level>0 ? level : getColorData.l 
    if (color == "Custom-User Defined"){
		hueColor = hueUserDefined ?  hueUserDefined  : 0
		satLevel = satUserDefined ? satUserDefined : 0
		hueColor = hueColor > 100 ? 100 : hueColor < 0 ? 0 : hueColor
		satLevel = satLevel > 100 ? 100 : satLevel < 0 ? 0 : satLevel
	}
    def newValue = [hue: hueColor as int, saturation: satLevel as int] 
    def isOsram = parent ? parent.osramCMD : osramCMD
    if (isOsram){
    	try {  switches?.loopOff() } 
    	catch (e) { log.warn "You have attempted a command that is not compatible with the the device handler you are using. Try to turn off the Osram functions in Settings>Device Specific Commands"  }  
    }
    switches?.setColor(newValue)
    switches?.setLevel(newLevel as int)
}
//Common Code (Parent)---------------------------------
private webCoRE_init(pistonExecutedCbk){
	state.webCoRE=(state.webCoRE instanceof Map?state.webCoRE:[:])+(pistonExecutedCbk?[cbk:pistonExecutedCbk]:[:])
    subscribe(location,"${webCoRE_handle()}.pistonList",webCoRE_handler)
    if(pistonExecutedCbk)subscribe(location,"${webCoRE_handle()}.pistonExecuted",webCoRE_handler)
    webCoRE_poll()
}
private webCoRE_poll(){ sendLocationEvent([name: webCoRE_handle(),value:'poll',isStateChange:true,displayed:false]) }
public webCoRE_execute(pistonIdOrName,Map data=[:]){
    def i=(state.webCoRE?.pistons?:[]).find{(it.name==pistonIdOrName)||(it.id==pistonIdOrName)}?.id
    if (i) sendLocationEvent([name:i,value:app.label,isStateChange:true,displayed:false,data:data])
}
public webCoRE_list(mode){
	def p=state.webCoRE?.pistons
    if (p) p.collect{ mode=='id' ? it.id :(mode=='name' ? it.name : mode=='enum' ? ["${it.id}":"${it.name}"] :[id:it.id,name:it.name]) }
}
public webCoRE_handler(evt){switch(evt.value){case 'pistonList':List p=state.webCoRE?.pistons?:[];Map d=evt.jsonData?:[:];if(d.id&&d.pistons&&(d.pistons instanceof List)){p.removeAll{it.iid==d.id};p+=d.pistons.collect{[iid:d.id]+it}.sort{it.name};state.webCoRE = [updated:now(),pistons:p];};break;case 'pistonExecuted':def cbk=state.webCoRE?.cbk;if(cbk&&evt.jsonData)"$cbk"(evt.jsonData);break;}}
def getRandDesc(num){
	def result = "Tap to add responses to %random${num}%"
    if (settings."random${num}A" || settings."random${num}B"|| settings."random${num}C"){
        result = ""
        if (settings."random${num}A") result += "1: ${settings."random${num}A"}"
        if (settings."random${num}A" && (settings."random${num}B" || settings."random${num}C")) result +="\n"
        if (settings."random${num}B") result += "2: ${settings."random${num}B"}"
        if ((settings."random${num}A" || settings."random${num}B") && settings."random${num}C") result +="\n"
        if (settings."random${num}C") result += "3: ${settings."random${num}C"}"
    }
    return result
}
def getGlobeVarState(){
	return voiceTempVar || voiceHumidVar || voicePresenceVar || getWR().size() || random1A || random2A || random3A || random1B || random2B || random3B || random1C || random2C || random3C
}
private switchesSel() { return switches || (deviceAlias && switchesAlias) }
private dimmersSel() { return dimmers || (deviceAlias && dimmersAlias) }
private cLightsSel() { return cLights || (deviceAlias && cLightsAlias) }
private cLightsKSel() { return cLightsK || (deviceAlias && cLightsKAlias) }
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
def getMacroList(type,exclude){
    def result=[]
    if (type ==~/all|other/) {
    	if (type =="all") getAskAlexa().each{ if (it.label && it.macroType !="GroupM" && it.macroType!="Group") result << ["${it.label}": "${it.label} (${it.macroType} Macro)"] }
		getVR().each{ if (it.label && it.label != exclude) result << ["${it.label}": "${it.label} (Voice Report)"]}
		getWR().each{ if (it.label) result << ["${it.label}": "${it.label} (Weather Report)"]} 
        if (type=="other") getAAMQ().each{ if (it.label) result << ["${it.label}": "${it.label} (Message Queue)"]} 
    }
    else if (type =~/sched/){
    	if (type =="schedV") getVR().each{ if (it.label) result << "${it.label}"}
        else if (type =="schedW") getWR().each{ if (it.label) result << "${it.label}"}	
        else if (type=="schedM") getAskAlexa().each{ if (it.macroType!="Group" && it.label) result << ["${it.label}": "${it.label} (${it.macroType=="GroupM"?"Extension Group":it.macroType} Macro)"] }
    }
    else if (type == "flash"){
    	getAskAlexa().each{ if (it.macroType =="GroupM") result << ["${it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")}%M%":"${it.label} (Extension Group)"] }
    	result<<["undefined%Q%": "Primary Message Queue"]
        getAAMQ().each{ if (it.label) result<< ["${it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")}%Q%": it.label +" (Message Queue)"]}
        getVR().each{ if (it.label) result<< ["${it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")}%V%": it.label +" (Voice Report)"]}
        getWR().each{ if (it.label) result<< ["${it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "")}%W%": it.label +" (Weather Report)"]}
    }
    return result	
}
def msgHandler(evt) {
    def selQueues = evt.jsonData && evt.jsonData?.queues ? evt.jsonData.queues  : []
    def overwrite = evt.jsonData && evt.jsonData?.overwrite ? true : false
    def expires = evt.jsonData && evt.jsonData?.expires ? evt.jsonData.expires as int  : 0
    def notifyOnly = evt.jsonData && evt.jsonData?.notifyOnly ? true : false
    def suppressTimeDate = evt.jsonData && evt.jsonData?.suppressTimeDate ? true : false
    def trackDelete = evt.jsonData && evt.jsonData?.trackDelete ? true : false
    def expiration = expires && expires !=0 ?  now() + (expires*1000) : 0
    if (selQueues.size()) {
		selQueues.each{qID->
			def qNameRun = getAAMQ().find{it.id == qID}
            if (qNameRun) qNameRun.msgHandler(evt.date, evt.descriptionText, evt.unit, evt.value, overwrite, expiration, notifyOnly, suppressTimeDate,trackDelete)
            else if (qID=="Primary Message Queue") msgPMQ (evt.date, evt.descriptionText, evt.unit, evt.value, overwrite, expiration, notifyOnly, suppressTimeDate,trackDelete)
        }
    }
    else msgPMQ (evt.date, evt.descriptionText, evt.unit, evt.value, overwrite, expiration, notifyOnly, suppressTimeDate,trackDelete)
}
def msgDeleteHandler(evt){
    def selQueues = evt.jsonData && evt.jsonData?.queues ? evt.jsonData.queues  : []
    if (selQueues.size()) {
	   	selQueues.each{qID-> 
			def qNameRun = getAAMQ().find{it.id == qID}
            def qName = qNameRun ? qNameRun.label : "Primary Message Queue"
        	if (qNameRun && msgQueueDelete.contains(qID)) qNameRun.msgDeleteHandler(evt.unit, evt.value)
       		else if (qID=="Primary Message Queue" && msgQueueDelete.contains("Primary Message Queue")) msgDeletePMQ(evt.unit, evt.value)
        	else log.debug "The '${qName}' message queue does not have SmartApp deletion turned on. No messages were deleted."
        }
    }
    else msgDeletePMQ(evt.unit, evt.value)
}
def msgPMQ(date,descriptionText,unit,value,overwrite, expires, notifyOnly, suppressTimeDate,trackDelete){
    if (!state.msgQueue) state.msgQueue=[]
	if (msgQueueForward){
    	msgQueueForward.each{qID->
			def qNameRun = getAAMQ().find{it.id == qID}
            if (qNameRun) qNameRun.msgHandler(date, descriptionText + " - This message was forwarded from the primary message queue.", unit, value, overwrite, expires, notifyOnly, suppressTimeDate,trackDelete);
        }
    }
    else {
        if (overwrite && msgQueueDelete && msgQueueDelete.contains("Primary Message Queue")) msgDeletePMQ(unit, value)
        else if (overwrite && (!msgQueueDelete || !msgQueueDelete.contains("Primary Message Queue"))) log.debug "An overwrite command was issued from '${value}', however, the option to allow deletions was not enabled for the Primary Message Queue."
        if (!notifyOnly) log.debug "New message added to primary message queue from: " + value
        if (!notifyOnly) state.msgQueue<<["date":date.getTime(),"appName":value,"msg":descriptionText,"id":unit, "expires": expires, "suppressTimeDate": suppressTimeDate,"trackDelete":trackDelete] 
        if (mqSpeaker && mqVolume && ((restrictAudio && getOkToRun())||!restrictAudio)) {
        	def msgVoice, msgSFX
            if (mqAlertType ==~/0|1|2/) {
            	def msgTxt= !mqAlertType ||mqAlertType as int ==0 || mqAlertType as int ==1 ? "New message received in primary message queue from : " + value : ""
            	if (!mqAlertType || mqAlertType ==~/0|2/) msgTxt += msgTxt ? ": "+ descriptionText : descriptionText
            	msgVoice = textToSpeech (msgTxt, true)
            }
            if (mqAlertType == "3" || mqAppendSound) msgSFX = sfxLookup(mqAlertSound)
			mqSpeaker?.setLevel(mqVolume as int)            
            if (mqAlertType != "3" && !mqAppendSound) mqSpeaker?.playTrack (msgVoice.uri)
            if (mqAlertType == "3") mqSpeaker?.playTrack (msgSFX.uri)
            if (mqAlertType != "3" && mqAppendSound)  mqSpeaker?.playSoundAndTrack(msgSFX.uri,msgSFX.duration,msgVoice.uri)
		}
        if (mqSynth && ((restrictAudio && getOkToRun())||!restrictAudio)) mqSynth?.speak(msgTxt)
		if (mqPush || mqSMS || mqContacts && ((restrictMobile && getOkToRun())||!restrictMobile)){
        	def mqMsg = "New message received by Ask Alexa in primary message queue from : " + value + ": "+ descriptionText
            sendMSG(mqSMS, mqMsg , mqPush, mqContacts)
		}
        if (mqFeed && ((restrictMobile && getOkToRun())||!restrictMobile)) sendNotificationEvent("New message received by Ask Alexa in primary message queue from : " + value + ": "+ descriptionText)
        if (msgQueueNotifyLightsOn && ((restrictVisual && getOkToRun())||!restrictVisual))  msgQueueNotifyLightsOn?.on()
        if (msgQueueNotifycLightsOn && (msgQueueNotifyColor || msgQueueNotifyLevel) && ((restrictVisual && getOkToRun())||!restrictVisual)) {
        	def level = !msgQueueNotifyLevel || msgQueueNotifyLevel < 0 ? 50 : msgQueueNotifyLevel >100 ? 100 : msgQueueNotifyLevel as int
        	msgQueueNotifyColor ? setColoredLights(msgQueueNotifycLightsOn, msgQueueNotifyColor, msgQueueNotifyLevel) : msgQueueNotifycLightsOn?.setLevel(level)
		}
    }
}
def msgDeletePMQ(unit,value){
	if (state.msgQueue && state.msgQueue.size()>0){
		if (unit && value){
			log.debug value + " is requesting to delete messages from the primary message queue."
			def deleteList = state.msgQueue.findAll{it.appName==value && it.id==unit && it.trackDelete}
            state.msgQueue.removeAll{it.appName==value && it.id==unit}
            if (deleteList){
            	deleteList.each{
                	sendLocationEvent(name:"askAlexaMQ", value: "${it.appName}.${it.id}",isStateChange: true,  data:[[deleteType: "delete"],[queue:"Primary message queue"]], descriptionText:"Ask Alexa deleted messages from the Primary message queue")
                }
            }
            if (msgQueueNotifyLightsOn && msgQueueNotifyLightsOff && !state.msgQueue) msgQueueNotifyLightsOn?.off()
            if (msgQueueNotifycLightsOn && msgQueueNotifyLightsOff && !state.msgQueue) msgQueueNotifycLightsOn?.off()
		}
		else log.debug "Incorrect delete parameters sent to the primary message queue. Nothing was deleted."
	} 
    else log.debug "The primary message queue is empty. No messages were deleted."
}
def mqCounts(list){
    def msgList=[], msgCountTxt="",queS=""
    purgeMQ()
    if (list){
        if (list.contains("Primary Message Queue") && state.msgQueue && state.msgQueue.size()) msgList<<"Primary Message Queue"
        list.each{qID->
            def qNameRun = getAAMQ().find{it.id == qID}
            def qName =  qNameRun ? qNameRun.label : "Primary Message Queue"
            if (qNameRun && qName !="Primary Message Queue" && qNameRun.qSize()) msgList<<qName
    		queS = msgList.size()==1 ? "queue" :  msgList.size()>1 ? "queues" : ""
    	}
    	msgCountTxt = msgList ? "You have messages present in the following message ${queS}: " + getList(msgList):""
   }
   return msgCountTxt
}
def purgeMQ(){
	if (!state.msgQueue) state.msgQueue=[]
    log.debug "Ask Alexa is purging expired messages from the Primary Message Queue."
    def deleteList = state.msgQueue.findAll{it.expires !=0 && now() > it.expires && it.trackDelete}
    state.msgQueue.removeAll{it.expires !=0 && now() > it.expires}
    if (deleteList){
    	deleteList.each{
			sendLocationEvent(name:"askAlexaMQ", value: "${it.appName}.${it.id}",isStateChange: true, data:[[deleteType: "expire"],[queue:"Primary message queue"]], descriptionText:"Ask Alexa expired messages from the Primary message queue")
        }
	}
    if (!state.msgQueue.size()){
    	if (msgQueueNotifyLightsOn && msgQueueNotifyLightsOff && !state.msgQueue) msgQueueNotifyLightsOn?.off()
    	if (msgQueueNotifycLightsOn && msgQueueNotifyLightsOff && !state.msgQueue) msgQueueNotifycLightsOn?.off()
    }
}
def getExtList(){ 
	def extList =[]
    getWR().each {extList +=["${it.id}":"${it.label}"]}
    getVR().each {extList +=["${it.id}":"${it.label}"]}
    getSCHD().each {extList +=["${it.id}":"${it.label}"]}
    getAskAlexa().each {if (it.macroType !='CoRE') extList +=["${it.id}":"${it.label}"]}
    return extList
}
def getMQListID(withPMQ){
	def outputMQlist = withPMQ ? ["Primary Message Queue":"Primary Message Queue"]:[]
    getAAMQ().each{ outputMQlist +=["${it.id}":"${it.label}"] }
    return outputMQlist
}
def getVariableList(){
    def temp = voiceTempVar ? getAverage(voiceTempVar, "temperature") : "undefined device"
    def humid = voiceHumidVar ? getAverage(voiceHumidVar, "humidity") + " percent relative humidity" : "undefined device"
    def present = voicePresenceVar.findAll{it.currentValue("presence")=="present"}
   	def people = present ? getList(present) : "No people present"
    return [temp: temp, humid: humid, people: people]
}
private getAverage(device,type){
	def total = 0
	device.each { if (it.latestValue(type)) total += it.latestValue(type) }
    return Math.round(total/device.size())
}
def getTstatLimits() { return [hi:tstatHighLimit, lo: tstatLowLimit] }
def OAuthToken(){
	try {
        createAccessToken()
		log.debug "Creating new Access Token"
	} catch (e) { log.error "Access Token not defined. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth." }
}
def macroDesc(count){def results = count ? count==1 ? "One Macro Configured" : count + " Macros Configured" : "No Macros Configured\nTap to create a new macro"}
def mqDesc(count){def results = count ? count==1 ? "Primary + One Addition Message Queue Configured" :  "Primary + " + count + " Message Queues Configured" : "Primary Messsage Queue"}
def schDesc(count) {def results = count ? count==1 ? "One Schedule Configured" : count + " Schedules Configured" : "No Schedules Configured\nTap to create a new schedule"}
def voiceDesc(count) {def results = count ? count==1 ? "One Voice Report Configured" : count + " Voice Reports Configured" : "No Voice Reports Configured\nTap to create a new report"}
def weathDesc(count){def results = count ? count==1 ? "One Weather Report Configured" : count + " Weather Reports Configured" : "No Weather Reports Configured\nTap to create a new report"}
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
private List<Map> STColors() {
    if (customName) return [customColor(), *colorUtil.ALL] else return [*colorUtil.ALL]
}
private Map customColor(){
    if (customName && (customHue > -1 && customerHue < 101) && (customSat > -1 && customerSat < 101)) return [name: customName, rgb: "#000000", h: customHue * 3.6, s: customSat, l:100]
}
def getDeviceList(){
	def result = []
    try {
        mapDevices(false).each{
    		def devicesGroup = it.devices, devicesType = it.type, devIcon=it.icon
			devicesGroup.collect{ result << [name: it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase(), type: devicesType, devices: devicesGroup, icon:devIcon] }
        }
    }
    catch (e) { log.warn "There was an issue parsing the device labels. Be sure all of the devices are uniquely named/labeled and that none of them are blank (null). " }
    return result
}
def findNullDevices(){
	def result=""
    mapDevices(false).each{devicesGroup->
		devicesGroup.devices.each { result += !it.label ? it.name + "\n" : "" }
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
	if (settings."switches${ext}") result << [devices: settings."switches${ext}", type : "switch",fullListName:"Switch", cmd:switchVoc()]
	if (settings."dimmers${ext}") result << [devices: settings."dimmers${ext}", type : "level",fullListName:"Dimmer",cmd:levelVoc()]
	if (settings."cLights${ext}") result << [devices: settings."cLights${ext}", type : "color",fullListName:"Colored Light", cmd:colorVoc()]
    if (settings."cLightsK${ext}") result << [devices: settings."cLightsK${ext}", type : "kTemp", fullListName:"Temperature (Kelvin) Light", cmd:kTempVoc()]
	if (settings."doors${ext}") result << [devices: settings."doors${ext}", type : "door",fullListName:"Door Control", cmd:doorVoc()]
	if (settings."shades${ext}") result << [devices: settings."shades${ext}", type : "shade",fullListName:"Window Shade", cmd:shadeVoc()]
	if (settings."locks${ext}") result << [devices: settings."locks${ext}", type : "lock",fullListName:"Lock", cmd:lockVoc()]
	if (settings."tstats${ext}") result << [devices: settings."tstats${ext}", type : "thermostat",fullListName:"Thermostat",cmd:tstatVoc()]
	if (settings."speakers${ext}") result << [devices: settings."speakers${ext}", type : "music",fullListName:"Speaker", cmd:speakerVoc()]
	if (settings."temps${ext}") result << [devices: settings."temps${ext}", type : "temperature",fullListName:"Temperature Sensor", cmd: tempVoc()]
	if (settings."humid${ext}") result << [devices: settings."humid${ext}", type : "humidity",fullListName:"Humidity Sensor", cmd: humidVoc()]
	if (settings."ocSensors${ext}") result << [devices: settings."ocSensors${ext}", type : "contact",fullListName:"Open/Close Sensor", cmd: contactVoc()]
	if (settings."water${ext}") result << [devices: settings."water${ext}", type : "water",fullListName:"Water Sensor", cmd: waterVoc()]
	if (settings."motion${ext}") result << [devices: settings."motion${ext}", type : "motion",fullListName:"Motion Sensor", cmd: motionVoc()]
	if (settings."presence${ext}") result << [devices: settings."presence${ext}", type : "presence",fullListName:"Presence Sensor", cmd: presenceVoc()]
	if (settings."acceleration${ext}") result << [devices: settings."acceleration${ext}", type : "acceleration",fullListName:"Acceleration Sensor", cmd: accelVoc()]
    return result
}
def basicVoc(){return ["status","event","events"]}
def switchVoc(){return ["on", "off", "toggle"]}
def levelVoc(){return switchVoc()+["low","medium","high","maximum","minimum","increase","raise","up","decrease","down","lower","brighten","dim"]}
def colorVoc(){return levelVoc()}
def kTempVoc(){return levelVoc()}
def doorVoc(){return ["open","close"]}
def lockVoc(){return ["lock","unlock"]}
def shadeVoc(){return ["open","close"]}
def presenceVoc(){return basicVoc()}
def waterVoc(){return basicVoc()}
def accelVoc(){return basicVoc()}
def motionVoc(){return basicVoc()}
def tempVoc(){return basicVoc()}
def humidVoc(){return basicVoc()}
def contactVoc(){return basicVoc()}
def speakerVoc(){return basicVoc()+["play","pause","stop","next track","previous track","off","mute","on","unmute","status","low","medium","high","maximum","increase","raise","up","decrease","down","lower"]}
def tstatVoc(){
	def result =["increase","raise","up","decrease","down","lower","maximum","minimum","off"]
	if (ecobeeCMD && MyEcobeeCMD) result+=ecobeeVOC()
    if (nestCMD && nestMGRCMD) result +=["report"]
    return result
}
def ecobeeVOC(){return ["erase","delete","clear","reset","get","restart","repeat","replay","play","give","load","reload"]}
def msgVoc(){ return ["play","open","erase","delete","clear"]}
def upDown(device, type, op, num, deviceName){
    def numChange, newLevel, currLevel, defMove, txtRsp = ""
    if (type==~/color|level|kTemp/) { defMove = lightAmt as int ?: 0 ; currLevel = device.currentValue("switch")=="on" ? device.currentValue("level") as int : 0 } 
    if (type=="music") { defMove = speakerAmt as int ?: 5 ;currLevel = device.currentValue("level") as int }
    if (type=="thermostat") { defMove=tstatAmt as int ?: 5 ; currLevel =device.currentValue("temperature") as int }
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
        	def msgData= timeDate(eDate)
    		voiceDay = msgData.msgDay == "Today" ? msgData.msgDay : "On " + msgData.msgDay
    		result += "${voiceDay} at ${msgData.msgTime} the event was: ${eDesc}. "
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
        if (flash && flashRPT && flashRPT.endsWith("%Q%")) outputTxt=msgQueueReply("play", flashRPT[0..-4])
        else if (flash && flashRPT && flashRPT.endsWith("%W%")) outputTxt=processWeatherReport(flashRPT[0..-4])
        else if (flash && flashRPT && flashRPT.endsWith("%V%")) outputTxt=processVoiceReport(flashRPT[0..-4])
        else if (flash && flashRPT && flashRPT.endsWith("%M%")){
            def child = getAskAlexa().find {it.label.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "") == flashRPT[0..-4]}
            if (child.macroType != "GroupM") outputTxt = child.getOkToRun() ? child.macroResults("", "", "", "", "","") : "You have restrictions within the ${fullMacroName} named, '${child.label}', that prevent it from running. Check your settings and try again. %1%"
            else outputTxt = processMacroGroup(child.groupMacros, child.voicePost, child.addPost, child.noAck, child.label) 
        }
        else outputTxt = "You do not have the flash briefing option enabled in your Ask Alexa Smart App, or you don't have any output selected for the briefing output. Go to Settings in your SmartApp to fix this. "
    } catch (e) { outputTxt ="There was an error producing the flash briefing report. Check your settings and try again." }
    if (outputTxt.endsWith("%")) outputTxt=outputTxt[0..-4]
    log.debug "Sending Flash Briefing Output: " + outputTxt
    return ["uid": "1", "updateDate": new Date().format("yyyy-MM-dd'T'HH:mm:ss'.0Z'"), "titleText": "Ask Alexa Flash Briefing Report", "mainText": outputTxt,
		"redirectionUrl": "https://graph.api.smartthings.com/", "description": "Ask Alexa Flash Briefing Report"]
}
def setupData(){
	def macChildren = getAskAlexa()
	log.info "Set up web page located at : ${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}"
    def result ="<div style='padding:10px'><i><b><a href='http://aws.amazon.com' target='_blank'>Lambda</a> code variables:</b></i><br><br>var STappID = '${app.id}';<br>var STtoken = '${state.accessToken}';<br>"
    result += "var url='${getApiServerUrl()}/api/smartapps/installations/' + STappID + '/' ;<br><br><hr>"
    result += flash ? "<i><b><a href='http://developer.amazon.com' target='_blank'>Amazon ASK</a> Flash Briefing Skill URL:</b></i><br><br>${getApiServerUrl()}/api/smartapps/installations/${app.id}/flash?access_token=${state.accessToken}<br><br><hr>":""
	result += "<i><b><a href='http://developer.amazon.com' target='_blank'>Amazon ASK</a> Custom Slot Information:</b></i><br><br><b>CANCEL_CMDS</b><br><br>cancel<br>stop<br>unschedule<br><br><b>DEVICE_TYPE_LIST</b><br><br>"
    fillTypeList().each{result += it + "<br>"}
    result += "<br><b>LIST_OF_DEVICES</b><br><br>"
    def DEVICES=[], deviceCMDlist = [], SHPARAM =[], MACROS=[], MQ=[]
    def deviceList = getDeviceList()
    if (deviceList) deviceList.name.each{DEVICES << it }
    if (deviceAlias && state.aliasList) state.aliasList.each{DEVICES << it.aliasNameLC}
    def duplicates = DEVICES.findAll{DEVICES.count(it)>1}.unique()
    if (DEVICES && duplicates.size()){ 
    	result += "<b>**NOTICE: </b>The following duplicate(s) are only listed once below in LIST_OF_DEVICES:<br><br>"
        duplicates.each{result +="* " + it +" *<br><br>"}
        result += "Be sure to have unique names for each device/alias and only use each name once within the parent app.**<br><br>" 	
    }
    if (DEVICES) DEVICES.unique().each { result += it + "<br>" }
	else result += "none<br>"
	result += "<br><b>LIST_OF_FOLLOWUPS</b><br><br>password<br>pin<br><br><b>LIST_OF_OPERATORS</b><br><br>"
    def getDevList=mapDevices(false)
    if (deviceAlias) getDevList+=mapDevices(true)
    basicVoc().each{deviceCMDlist<<it}
    getDevList.cmd.each{list->list.each{deviceCMDlist<<it} }
    deviceCMDlist.unique().each{result += it+"<br>"}
    result += "<br><b>LIST_OF_PARAMS</b><br><br>"
    def PARAMS=["heat","cool","heating","cooling","auto","automatic","AC"]
    if (tstatsSel() && stelproCMD) PARAMS<< "eco"<<"comfort"
    if ((tstatsSel() && nestCMD) || vPresenceCMD) PARAMS<<"present"
    if (tstatsSel() && (nestCMD || ecobeeCMD) || vPresenceCMD) PARAMS<<"home"<<"away"
    if (tstatsSel() && ecobeeCMD) PARAMS<<"sleep"<<"resume program"
	if (tstats && MyEcobeeCMD){  getEcobeeCustomList(tstats).each { PARAMS<<"${it}" } }     
    if (tstatsSel() && ecobeeCMD && MyEcobeeCMD) PARAMS<<"tips"<<"tip"
    if (vPresenceCMD) PARAMS<<"check in"<<"check out"<<"arrive"<<"depart"<<"not present"<<"gone"
    if (sonosCMD && speakersSel() && sonosMemoryCount){
    	def memCount = sonosMemoryCount as int
    	for (int i=1; i<memCount+1; i++){
    		if (settings."sonosSlot${i}Name" && settings."sonosSlot${i}Music") PARAMS<<settings."sonosSlot${i}Name".replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()
    	}
    }
    if (cLightsSel() || cLightsKSel() || macChildren.size()) { STColors().each {PARAMS<<it.name.toLowerCase()}}
    duplicates = PARAMS.findAll{PARAMS.count(it)>1}.unique()
    if (duplicates.size()){ 
            result += "<b>**NOTICE: </b>The following duplicate(s) are only listed once below in LIST_OF_PARAMS:<br><br>"
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
        if (listSHM) SHPARAM << "arm" << "armed stay" << "armed home" << "armed away" << "disarm" << "off"
        if (listRoutines) listRoutines.each { if (it) SHPARAM << it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
        if (listModes) listModes.each { if (it) SHPARAM << it.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
        duplicates = SHPARAM.findAll{SHPARAM.count(it)>1}.unique()
        if (duplicates.size()){ 
            result += "<b>**NOTICE: </b>The following duplicate(s) are only listed once below in LIST_OF_SHPARAM:<br><br>"
            duplicates.each{result +="* " + it +" *<br><br>"}
            result += "Be sure to have unique names for your SmartThings modes and routines and that they don't interfer with the Smart Home Monitor commands.**<br><br>"
        }
        SHPARAM.unique().each {result += it + "<br>" }
    }
    else result +="none<br>"
    result += "<br><b>LIST_OF_SHCMD</b><br><br>routine<br>mode<br>smart home monitor<br>" 
    if (listSHM) result +="security<br>smart home<br>SHM<br>"
    result += "<br><b>LIST_OF_MACROS</b><br><br>"
    if (macChildren.size()){
        macChildren.each { 
        	MACROS << it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase()
			for (int i = 1; i<macAliasCount()+1; i++){ if (it."macAlias${i}") MACROS << it."macAlias${i}".replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
		}
    }
    if (getVR().size()) getVR().each { 
    	MACROS << it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() 
    	for (int i = 1; i<it.extAliasCount()+1; i++){ if (it."extAlias${i}") MACROS << it."extAlias${i}".replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
    }
    if (getWR().size()) getWR().each { 
    	MACROS << it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() 
    	for (int i = 1; i<it.extAliasCount()+1; i++){ if (it."extAlias${i}") MACROS << it."extAlias${i}".replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
    } 
    if (getSCHD().size()) getSCHD().each { MACROS << it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }
    if (MACROS.size()){
        duplicates = MACROS.findAll{MACROS.count(it)>1}.unique()
        if (duplicates.size()){ 
            result += "<b>**NOTICE: </b>The following duplicate(s) are only listed once below in LIST_OF_MACROS:<br><br>"
            duplicates.each{result +="* " + it +" *<br><br>"}
            result += "Be sure to have unique names for each macro, macro alias, and weather report and only use each name once within the app.**<br><br>" 	
        }
        MACROS.unique().each {result += it + "<br>" }
    }
    else result += "none<br>"
    result += "<br><b>LIST_OF_MQ</b><br><br>"
	MQ<<"primary message queue"<<"primary"
	if (getAAMQ().size()){
        getAAMQ().each { MQ << it.label.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase() }       
        duplicates = MQ.findAll{MQ.count(it)>1}.unique()
        if (duplicates.size()){ 
            result += "<b>**NOTICE: </b>The following duplicate(s) are only listed once below in LIST_OF_MQ:<br><br>"
            duplicates.each{result +="* " + it +" *<br><br>"}
            result += "Be sure to have unique names for each message queue and only use each name once within the parent app.**<br><br>" 	
        }
    }
    MQ.unique().each {result += it + "<br>" }
    result += "<br><b>LIST_OF_MQCMD</b><br><br>"
    msgVoc().each{result +=it + "<br>" }
    result +="<br><b>LIST_OF_WCP</b><br><br>This is an advanced feature. If you use WebCoRE pistons, add your extra parameters to this slot. Otherwise, just add the word 'none' to this slot"
	result += "<br><hr><br><i><b>URL of this setup page:</b></i><br><br>${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}<br><br><hr>"
	result += "<br><i><b>Lastest version of the Lambda code:</b></i><br><br><a href='https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/Node.js'>https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/Node.js</a><br><br><hr>"
    result += "<br><i><b>Lastest version of the Sample Utterances:</b></i><br><br><a href='https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/Sample%20Utterances'>https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/Sample%20Utterances</a><br><br><hr>"
    result += "<br><i><b>Lastest version of the Intent Schema:</b></i><br><br><a href='https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/Intent%20Schema'>https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa.src/Intent%20Schema</a><br><br><hr></div>"
    displayData(result)
}
def fillTypeList(){
	return ["reports","report","switches","switch","dimmers","dimmer","colored lights","color","colors","speakers","speaker","water sensor","water sensors","water","lock","locks","thermostats","thermostat",
    	"temperature sensors","modes","routines","smart home monitor","SHM","security","temperature","door","doors", "humidity", "humidity sensor", "humidity sensors","presence", "presence sensors", "motion", 
        "motion sensor", "motion sensors", "door sensor", "door sensors", "window sensor", "window sensors", "open close sensors","colored light", "events","macro", "macros", "group", "groups", "voice reports", 
        "voice report", "device group", "device groups","control macro", "control macros","control", "controls","extension group","extension groups","device macros","device macro","device group macro","device group macros",
        "core","core trigger","core macro","core macros","core triggers","sensor", "sensors","shades", "window shades","shade", "window shade","acceleration", "acceleration sensor", "acceleration sensors", "alias","aliases",
        "temperature light","temperature lights","kelvin light","kelvin lights","message queue","queue","message queues","queues","weather", "weather report", "weather reports","schedule","schedules","webcore","webcore trigger","webcore macro",
        "webcore macros","webcore triggers"] 
}
def getURLs(){
	def mName = params.mName, url = formatURL("${getApiServerUrl()}/api/smartapps/installations/${app.id}/m?Macro=${mName}&access_token=${state.accessToken}")
    def result = "<div style='padding:10px'>Copy the URL below and paste it to your control application.</div><div style='padding:10px'>Click '<' above to return to the Ask Alexa SmartApp.</div>"
	result += "<div style='padding:10px;'><b>Macro REST URL:</b></div><textarea rows='5' style='width: 99%'>${url}</textarea><hr>"
    displayData(result)
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
	log.info "Cheat sheet web page located at : ${getApiServerUrl()}/api/smartapps/installations/${app.id}/cheat?access_token=${state.accessToken}"
    def result ="<div style='padding:10px'><h2><i><b>Ask Alexa Device/Command 'Cheat Sheet'</b></i></h2>To expand on this sheet, please see the <a href='http://thingsthataresmart.wiki/index.php?title=Ask_Alexa#Ask_Alexa_Usage' target='_blank'>Things That Are Smart Wiki</a><br>"
	result += "Most commands will begin with 'Alexa, ask ${invocationName}' or 'Alexa, tell ${invocationName}' and then the command and device. For example:<br>"
    result +="<i>Alexa, tell ${invocationName} to Open {DoorName}</i>'<br><i>'Alexa, ask ${invocationName} the {SwitchName} status'</i><br><br><hr>"
    if (switchesSel()) { result += "<h2>Switches (Valid Commands: <b>" + getList(switchVoc()+basicVoc()) + "</b>)</h2>"; switches.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("switch") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("switch") +"<br>" }
    if (dimmersSel()) { result += "<h2><u>Dimmers (Valid Commands: <b>{brightness number}, " + getList(levelVoc()+basicVoc()) + "</b>)</u></h2>"; dimmers.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("level") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("level") +"<br>" }
    if (cLightsSel()) { 
    	result += "<h2><u>Colored Lights (Valid Commands: <b>{brightness number}, " + getList(colorVoc()+basicVoc()) + "</b>)</u></h2>"; cLights.each{ result += it.label +"<br><br>" } 
    	result +="<u>Available Colors {color name}</u><br>" + getList(STColors().name) + "<br>"
    }
    if (getCheatDisplayList("color") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("color") +"<br>" }
    if (cLightsKSel()) { 
    	result += "<h2><u>Temperature (Kelvin) Lights (Valid Commands: <b>{brightness number}, " + getList(kTempVoc()+basicVoc()) + "</b>)</u></h2>"; cLightsK.each{ result += it.label +"<br><br>" } 
    	result +="<u>Available Temperatures {name}</u><br>Soft White, Warm White, Cool White, Daylight White<br>"
    }
    if (getCheatDisplayList("kTemp") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("kTemp") +"<br>" }
    if (doorsSel()) { result += "<h2><u>Doors (Valid Commands: <b>"+ getList(doorVoc()+basicVoc()) +"</b>)</u></h2>"; doors.each{ result += it.label +"<br>" } }
    if (doorsSel() && pwNeeded && doorPW) {result += "<br>* Append '<i>Password ${password}</i>'  to activate your doors<br>" }
    if (getCheatDisplayList("door") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("door") +"<br>" }
    if (locksSel()) { result += "<h2><u>Locks (Valid Commands: <b>"+ getList(lockVoc()+basicVoc()) +"</b>)</u></h2>"; locks.each{ result += it.label +"<br>" } }
    if (locksSel() && pwNeeded && lockPW) {result += "<br>* Append '<i>Password ${password}</i>' to activate your locks<br>" }
    if (getCheatDisplayList("lock") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("lock") +"<br>" }
    if (ocSensorsSel()) { result += "<h2><u>Open/Close Sensors (Valid Command: <b>"+ getList(contactVoc()) +"</b>)</u></h2>"; ocSensors.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("contact") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("contact") +"<br>" }
    if (shadesSel()) { result += "<h2><u>Doors (Valid Commands: <b>"+ getList(shadeVoc()+basicVoc()) +"</b>)</u></h2>"; shades.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("shade") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("shade") +"<br>" }
    if (tstatsSel()) { result += "<h2><u>Thermostats (Valid Commands: <b>{temperature setpoint}, "+ getList(tstatVoc()+basicVoc()) +"</b>)</u></h2>"; tstats.each{ result += it.label +"<br>" }
    	if (ecobeeCMD && MyEcobeeCMD) result +="<br><b>* Please Note:</b> Some commands are MyEcobee specific such as Get Tips {level}, Play Tips and Erase Tips<br>"
    }
    if (getCheatDisplayList("thermostat") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("thermostat") +"<br>" }
    if (tempsSel()) { result += "<h2><u>Temperature Sensors (Valid Commands: <b>"+ getList(tempVoc()) +"</b>)</u></h2>"; temps.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("temperature") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("temperature") +"<br>" }
    if (humidSel()) { result += "<h2><u>Humidity Sensors (Valid Commands: <b>"+ getList(humidVoc()) +"</b>)</u></h2>"; humid.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("humidity") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("humidity") +"<br>" }
    if (speakersSel()) { result += "<h2><u>Speakers (Valid Commands: <b>{volume level}, "+ getList(speakerVoc()+basicVoc()) +"</b>)</u></h2>"; speakers.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("music") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("music") +"<br>" }
    if (speakersSel() && sonosCMD && sonosMemoryCount){
        def memCount = sonosMemoryCount as int, slots=""
        for (int i=1; i<memCount+1; i++){
            slots += settings."sonosSlot${i}Name" ? settings."sonosSlot${i}Name"+"<br>" : ""
        }
        if (slots) result += "<br><u>Memory Slots</u><br>" + slots
    }
    if (waterSel()) { result += "<h2><u>Water Sensors (Valid Command: <b>"+ getList(waterVoc()) +"</b>)</u></h2>"; water.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("water") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("water") +"<br>" }
    if (presenceSel()) {
    	result += "<h2><u>Presence Sensors (Valid Commands: <b>"+ getList(presenceVoc())
        if (vPresenceCMD) result += ", check in, check out, arrive, depart, present, away, not present, gone"
        result +="</b>)</u></h2>"; presence.each{ result += it.label +"<br>" } 
    	if (vPresenceCMD) result +="<br><b>* Please Note:</b> Not all presence sensor may respond to the check in/check out commands<br>"
    }
    if (getCheatDisplayList("presence") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("presence") +"<br>" }
    if (accelerationSel()) { result += "<h2><u>Acceleration Sensors (Valid Command: <b>"+ getList(accelVoc()) +"</b>)</u></h2>"; acceleration.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("acceleration") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("acceleration") +"<br>" }
    if (motionSel()) { result += "<h2><u>Motion Sensors (Valid Command: <b>"+ getList(motionVoc()) +"</b>)</u></h2>"; motion.each{ result += it.label +"<br>" } }
    if (getCheatDisplayList("motion") && deviceAlias) { result += "<br><u>Aliases</u><br>"; result += getCheatDisplayList("motion") +"<br>" }
    if (listModes) { result += "<h2><u>Modes (Valid Command: <b>change/status</b>)</u></h2>"; listModes.each{ result += it +"<br>" } }
    if (listModes && pwNeeded && modePW) {result += "<br>* Append '<i>password ${password}</i>' to activate your modes<br>" }
	if (listSHM) { result += "<h2><u>Smart Home Monitor (Valid Command: <b>change/status</b>)</u></h2>"; listSHM.each{ result += it +"<br>" } }
    if (listSHM && pwNeeded && shmPW) {result += "<br>* Append '<i>password ${password}</i>' to change your Smart Home Monitor status<br>" }
    if (listRoutines) { result += "<h2><u>SmartThings Routines (Valid Command: <b>run {routine name}</b>)</u></h2>"; listRoutines.each{ result += it +"<br>" } }
    if (listRoutines && pwNeeded && routinesPW) {result += "<br>* Append '<i>password ${password}</i>' to activate your routines<br>" }
    if (getAskAlexa().size() ) { 
    	result += "<h2><u>Ask Alexa Macros (Valid Command: <b>run {macro name}</b>)</u></h2>"
        getAskAlexa().each { 
        	result += it.label
        	def aliases = ""
            for (int i = 1; i<macAliasCount()+1; i++){
            	if (it."macAlias${i}") aliases += it."macAlias${i}"
                if (it."macAlias${i+1}") aliases += ", "
        	}
            if (aliases) result += " (Aliases: " + aliases +")"
            result += "<br>"
		}
    if (pwNeeded) {result += "<br>* Append '<i>password ${password}</i>' if a macro is set up to use a password<br>" }    
    }
    if (getVR().size()) { 
    	result += "<h2><u>Voice Reports (Valid Command: <b>run {report name}</b>)</u></h2>"
        getVR().each { 
        	result += it.label
            def aliases = ""
            for (int i = 1; i<it.extAliasCount()+1; i++){
            	if (it."extAlias${i}") aliases += it."extAlias${i}"
                if (it."extAlias${i+1}") aliases += ", "
        	}
            if (aliases) result += " (Aliases: " + aliases +")"
            result += "<br>"
    	} 
    }
    if (getSCHD().size()) {
    	result += "<h2><u>Schedules (Valid Command: <b>on, off, list and status {schedule name}</b>)</u></h2>"
        getSCHD().each { result +="${it.label}<br>" } 
    }
    if (getWR().size()) { 
    	result += "<h2><u>Weather Reports (Valid Command: <b>run {report name}</b>)</u></h2>"
        getWR().each { 
        	result += it.label
            def aliases = ""
            for (int i = 1; i<it.extAliasCount()+1; i++){
            	if (it."extAlias${i}") aliases += it."extAlias${i}"
                if (it."extAlias${i+1}") aliases += ", "
        	}
            if (aliases) result += " (Aliases: " + aliases +")"
            result += "<br>"
    	} 
    }
    result += "<h2><u>Message Queues (Valid Commands: <b>"+getList(msgVoc())+"</b>)</u></h2>Primary Message Queue<br>"
    if (getAAMQ().size()) getAAMQ().each { result += it.label+"<br>" }       
    result += "<br><u><b>Examples:</b><br><i>'Alexa, ask ${invocationName} to play messages {queue name}'</i><br><i>'Alexa, ask ${invocationName} to delete messages'</i><br><i>'Alexa, ask ${invocationName} to play {queue name} messages'</i><br>"
    displayData(result)
}
//Version/Copyright/Information/Help-----------------------------------------------------------
private webCoRE_handle(){ return'webCoRE' }
private textAppName() { return "Ask Alexa" }	
private textVersion() {  
    def version = "SmartApp Version: ${versionLong()} (${versionDate()})", lambdaVersion = state.lambdaCode ? "\n" + state.lambdaCode : "", aaMQVer ="", aaWRVer ="", aaVRVer="", aaSCHVer=""
    if (getAAMQ().size()) getAAMQ().each { aaMQVer="\n"+it.textVersion() }
    if (getWR().size()) getWR().each { aaWRVer="\n"+it.textVersion() }
    if (getVR().size()) getVR().each { aaVRVer="\n"+it.textVersion() }
    if (getSCHD().size()) getSCHD().each { aaSCHVer="\n"+it.textVersion() }
    return "${version}${lambdaVersion}${aaMQVer}${aaSCHVer}${aaVRVer}${aaWRVer}"
}
private versionInt(){ return 229 }
private LambdaReq() { return 129 }
private mqReq() { return 104 }
private wrReq()  { return 104 }
private vrReq()  { return 104 }
private schReq()  { return 103 }
private versionLong(){ return "2.2.9e" }
private versionDate(){ return "07/13/17" }
private textCopyright() {return "Copyright © 2017 Michael Struck" }
private textLicense() {
	def text = "Licensed under the Apache License, Version 2.0 (the 'License'); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n\n"+
		"    http://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 'AS IS' BASIS, "+
		"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License."
}
private textHelp() { 
	return "This SmartApp provides an interface to control, query and report on your SmartThings environment via the Amazon Echo ('Alexa'). For more information, go to http://thingsthataresmart.wiki/index.php?title=Ask_Alexa."
}