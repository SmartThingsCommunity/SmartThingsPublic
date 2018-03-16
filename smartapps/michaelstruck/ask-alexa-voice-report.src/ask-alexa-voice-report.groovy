/**
 *  Ask Alexa Voice Report Extension
 *
 *  Copyright © 2018 Michael Struck
 *  Version 1.0.9 3/11/18
 * 
 *  Version 1.0.0 - Initial release
 *  Version 1.0.1 - Updated icon, added restricitions 
 *  Version 1.0.2a - (6/17/17) - Small bug fixes, deprecated send to notification feed. Will add message queue functionality if feedback is given
 *  Version 1.0.3 - (6/28/17) - Added device health report, replaced notifications with Message Queue
 *  Version 1.0.4 - (7/11/17) - Added code for additional text field variables, allow suppression of continuation messages.
 *  Version 1.0.5 - (8/3/17) - Added support for Foobot Air Quality Monitor, permanently enabled voice filters
 *  Version 1.0.6a - (9/21/17) - Added UV index reporting
 *  Version 1.0.7 - (11/2/17) - Added LUX and window shade reporting along with support for custom Aeon power meter DTH
 *  Version 1.0.8 - (2/3/18) - Added Room occupancy reporting, begin adding Echo device indentification
 *  Version 1.0.9 - (3/11/18) - Implement Echo device Identification
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
    name: "Ask Alexa Voice Report",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Extension Application of Ask Alexa. Do not install directly from the Marketplace",
    category: "My Apps",
    parent: "MichaelStruck:Ask Alexa",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/ext@2x.png",
    )
preferences {
    page name:"mainPage"
    	page name:"pageEnviroReport"
        page name:"pageAirReport"
		page name:"pageHomeReport"
		page name:"pageOtherSensors"
    	page name:"pageBatteryReport"
    	page name:"pageDoorReport"
    	page name:"pageSwitchReport"
        page name:"pagePresenceReport"
        page name:"pageSpeakerReport"
        page name:"pageOtherReports"
        page name:"pageDeviceHealth"
        page name:"pageMQ"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title: "Ask Alexa Voice Report Options", install: true, uninstall: true){
        section {
        	label title:"Voice Report Name (Required)", required: true, image: parent.imgURL() + "voice.png"
            href "pageExtAliases", title: "Voice Report Aliases", description: extAliasDesc(), state: extAliasState()
        }
        section ("Voice report items") {
            input "voicePre", "text", title: "Pre Message Before Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false, capitalization: "sentences"
            href "pageSwitchReport", title: "Switch/Dimmer Report", description: getDesc("switch"), state: (voiceSwitch || voiceDimmer ? "complete" : null), image: parent.imgURL() + "power.png"
            href "pageDoorReport", title: "Door/Window/Shade/Lock Report", description: getDesc("door"), state: (voiceDoorSensors || voiceDoorControls || voiceDoorLocks || voiceWindowShades ? "complete": null), image: parent.imgURL() + "lock.png"
            href "pageEnviroReport", title: "Environmental Report", description: getDesc("temp"), state:(voiceTemperature || voiceTempSettings || voiceHumidity || voiceUV || voiceLux? "complete" : null),image: parent.imgURL() + "temp.png"
            href "pageAirReport", title: "Foobot Air Quality Report", description: getDesc("pollution"), state:(fooBot ? "complete" : null), image: parent.imgURL() + "pollution.png"
            href "pageSpeakerReport", title: "Speaker Report", description: getDesc("speaker"), state: (voiceSpeaker ? "complete": null), image: parent.imgURL() + "speaker.png"
            href "pagePresenceReport", title: "Presence Report", description:  getDesc("presence"), state:(voicePresence ? "complete": null), image : parent.imgURL() + "people.png"    
            href "pageOtherSensors", title: "Other Sensors Report", description: getDesc("sensor"), state: (voiceWater|| voiceMotion|| voicePower || voiceAccel || voiceOccupancy  ? "complete" :null), image: parent.imgURL() + "sensor.png"
            href "pageHomeReport", title: "Mode and Smart Home Monitor Report", description: getDesc("MSHM"), state: (voiceMode|| voiceSHM? "complete": null), image: parent.imgURL() + "modes.png"
            href "pageDeviceHealth", title: "Device Health Report", description:getDesc("health"), state: (voiceHealth ? "complete" : null), image: parent.imgURL() + "health.png"
            href "pageBatteryReport",title: "Battery Report", description: getDesc("battery"), state: (voiceBattery ? "complete" : null), image: parent.imgURL() + "battery.png"
            href "pageOtherReports", title: "Include Other Reports", description: getDesc("other"), state: (otherReportsList ? "complete" : null), image: parent.imgURL() + "add.png"
            input "voicePost", "text", title: "Post Message After Device Report", description: "Use variables like %time%, %day%, %date% here.", required: false, capitalization: "sentences"
        }
        section ("Output options"){
        	href "pageMQ", title: "Send Output To Message Queue(s)", description: mqDesc(), state: vrMsgQue ? "complete" : null, image: parent.imgURL()+"mailbox.png"
            input "allowNullRpt", "bool", title: "Allow For Empty Report (For Extension Groups)", defaultValue: false
            if (parent.contMacro) {
                	input "overRideMsg", "bool", title: "Override Continuation Commands (Except Errors)", defaultValue: false, submitOnChange: true
                    if (!overRideMsg) input "suppressCont", "bool", title:"Suppress Continuation Messages (But Still Allow Continuation Commands)", defaultValue: false 
            }
        }
       	section("Advanced voice report filters", hideable: true, hidden: !(voiceRepFilter || voiceEvtTimeDate)){
            input "voiceRepFilter", "text", title: "Filter Report Output", description: "Delimit items with comma (ex. xxxxx,yyyyy,zzzzz)", required: false, capitalization: "sentences"
			input "voiceEvtTimeDate", "bool", title: "Speak Only Time/Date During Event Reports", defaultValue: false
		}
        section("Restrictions", hideable: true, hidden: !(runDay || timeStart || timeEnd || runMode || runPeople || runEcho || runSwitchActive || runSwitchNotActive))  {            
			input "runDay", "enum", options:parent.dayOfWeek(), title: "Only Certain Days Of The Week...",  multiple: true, required: false, image: parent.imgURL() + "calendar.png", submitOnChange: true
			href "timeIntervalInput", title: "Only During Certain Times...", description: parent.getTimeLabel(timeStart, timeEnd), state: (timeStart || timeEnd ? "complete":null), image: parent.imgURL() + "clock.png", submitOnChange: true
			input "runMode", "mode", title: "Only In The Following Modes...", multiple: true, required: false, image: parent.imgURL() + "modes.png", submitOnChange: true
            input "runPeople", "capability.presenceSensor", title: "Only When Present...", multiple: true, required: false, submitOnChange: true, image: parent.imgURL() + "people.png"
			if (runPeople && runPeople.size()>1) input "runPresAll", "bool", title: "Off=Any Present; On=All Present", defaultValue: false
            input "runEcho", "enum", title:"Only From These Echo Devices...", options: parent.getRmLists(), multiple: true, required: false, image: parent.imgURL() + "echo.png"
            input "runSwitchActive", "capability.switch", title: "Only When Switches Are On...", multiple: true, required: false, image: parent.imgURL() + "on.png"
			input "runSwitchNotActive", "capability.switch", title: "Only When Switches Are Off...", multiple: true, required: false, image: parent.imgURL() + "off.png"
            input "muteRestrictions", "bool", title: "Mute Restriction Messages In Extension Group", defaultValue: false
        } 
        section("Tap below to remove this message queue"){ }
        remove("Remove Voice Report" + (app.label ? ": ${app.label}" : ""),"PLEASE NOTE","This action will only remove this voice report. Ask Alexa, other macros and extensions will remain.")
	}
}
def pageMQ(){
    dynamicPage(name:"pageMQ"){
        section {
        	paragraph "Message Queue Configuration", image:parent.imgURL()+"mailbox.png"
        }
        section (" "){
            input "vrMsgQue", "enum", title: "Message Queue Recipient(s)...", options: parent.getMQListID(true), multiple:true, required: false, submitOnChange: true
            input "vrMQNotify", "bool", title: "Notify Only Mode (Not Stored In Queue)", defaultValue: false, submitOnChange: true
            if (!vrMQNotify) input "vrMQExpire", "number", title: "Message Expires (Minutes)", range: "1..*", required: false, submitOnChange: true
            if (!vrMQNotify && !vrMQExpire) input "vrMQOverwrite", "bool", title: "Overwrite Other Voice Report Messages", defaultValue: false
            if (!vrMQNotify) input "vrSuppressTD", "bool", title: "Suppress Time/Date From Alexa Playback", defaultValue: false
        }
    }
}
page(name: "timeIntervalInput", title: "Only during a certain time") {
	section {
		input "timeStart", "time", title: "Starting", required: false
		input "timeEnd", "time", title: "Ending", required: false
	}
}
page(name: "pageExtAliases", title: "Enter alias names for this voice report"){
	section {
    	for (int i = 1; i < extAliasCount()+1; i++){ input "extAlias${i}", "text", title: "Voice Report Alias Name ${i}", required: false }
    }
}
def pageAirReport(){
    dynamicPage(name: "pageAirReport", install: false, uninstall: false){
        section { paragraph "Foobot Air Quality Report", image: parent.imgURL() + "pollution.png" }
        section(" ") {
            input "fooBot", "capability.carbonDioxideMeasurement", title: "Choose Foobot Air Quality Monitors", multiple: true, required: false, submitOnChange: true 
            if (fooBot){
            	input "fooBooRptLvl", "enum", title:"GPI Reporting Level", options:["All":"Always report", "Good":"'Good' or below", "Fair": "'Fair' or 'Poor'","Poor":"'Poor' Only"], defaultValue: "All", required: false
            	input "fooBotPoll", "bool", title:"Refresh Foobot Data Before Reporting", defaultValue:false
            	input "fooBotCO2", "bool", title: "Include Carbon Dioxide", defaultValue:false
                input "fooBotVOC", "bool", title: "Include Volatile Organic Compounds", defaultValue:false
                input "fooBotPart", "bool", title: "Include Particulate Count", defaultValue:false
            	input "fooBotTemp", "bool", title: "Include Temperature", defaultValue:false
                input "fooBotHum", "bool", title: "Include Humidity", defaultValue:false
            }
        }
    }
}
def pagePresenceReport(){
	dynamicPage(name: "pagePresenceReport", install: false, uninstall: false){
    	section { paragraph "Presence Report", image: parent.imgURL() + "people.png" }
        section(" ") {
            input "voicePresence", "capability.presenceSensor", title: "Presence Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true 
            if (voicePresence) input "voicePresRepType", "enum", title: "Presence Sensor Report Type", options:[0:"Each device reports status (Default)",
            	1:"Summary report (If all present/away)",2:"Sensors that are 'present' only", 3:"Sensors that are 'not present' only"], defaultValue: 0, required:false
            if (voicePresence) input "voicePresentEvt", "bool",title: "Report Time Of Last Arrival", defaultValue: false 
            if (voicePresence) input "voiceGoneEvt", "bool",title: "Report Time Of Last Departure", defaultValue: false 
        }
    }
}
def pageSwitchReport(){
    dynamicPage(name: "pageSwitchReport", install: false, uninstall: false){
        section { paragraph "Switch/Dimmer Report", image: parent.imgURL() + "power.png" }
        section("Switch report", hideWhenEmpty: true) {
            input "voiceSwitch", "capability.switch", title: "Switches To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceSwitch) {
            	input "voiceSwitchAll", "bool", title: "Consolidate Report When All Switches Are On / Off", defaultValue: false
            	input "voiceOnSwitchOnly", "bool", title: "Report Only Switches That Are On", defaultValue: false
				input "voiceOnSwitchEvt", "bool",title: "Report Time Of Last On Event", defaultValue: false 
        	}
        }
        section("Dimmer report", hideWhenEmpty: true) {
            input "voiceDimmer", "capability.switchLevel", title: "Dimmers To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceDimmer) {
            	input "voiceDimmerAll", "bool", title: "Consolidate Report When All Dimmers Are On / Off", defaultValue: false
				input "voiceOnDimmerOnly", "bool", title: "Report Only Dimmers That Are On", defaultValue: false
				input "voiceOnDimmerEvt", "bool",title: "Report Time Of Last On Event", defaultValue: false
        	}
        }
    }
}
def pageDoorReport(){
    dynamicPage(name: "pageDoorReport", install: false, uninstall: false){
        section { paragraph "Doors/Windows/Locks", image: parent.imgURL() + "lock.png" }
        section("Doors / Windows / Locks reporting (Open / Unlocked)", hideWhenEmpty: true){
            input "voiceDoorSensors", "capability.contactSensor", title: "Doors/Windows Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            input "voiceDoorControls", "capability.doorControl", title: "Door Controls To Report Their Status...", multiple: true, required: false, submitOnChange: true
            input "voiceDoorLocks", "capability.lock", title: "Locks To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceDoorSensors || voiceDoorControls || voiceDoorLocks)input "voiceDoorAll", "bool", title: "Report Door/Window/Lock Summary Instead Of Individual Device Report", defaultValue: false
            if (voiceDoorSensors || voiceDoorControls)input "voiceDoorEvt", "bool",title: "Report Time Of Last Door/Window Opening", defaultValue: false
            if (voiceDoorLocks)input "voiceLockEvt", "bool",title: "Report Time Of Last Lock Unlocking", defaultValue: false
        }
        section("Window shades reporting", hideWhenEmpty: true){ 
        	input "voiceWindowShades", "capability.windowShade", title: "Window Shades To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceWindowShades)input "voiceShadeAll", "bool", title: "Report Window Shade Summary Instead Of Individual Device Report", defaultValue: false     
        } 
    }
}
def pageOtherSensors(){
    dynamicPage(name: "pageOtherSensors", install: false, uninstall: false){
        section { paragraph "Other Sensors Report", image: parent.imgURL() + "sensor.png" }
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
       		input "voicePower", "capability.powerMeter", title: "Power Meters To Report Energy Use...", multiple: true, required: false, submitOnChange: true
            if (voicePower) {
            	input "voicePowerOn", "bool", title: "Report Only Meters Drawing Power", defaultValue: false
             	input "voiceAeon", "bool", title: "Speak kWh Usage And Cost (Custom Aeon DTH)", defaultValue: false
            }
        }
        section ("Occupancy sensors", hideWhenEmpty: true) {
            input "voiceOccupancy", "capability.beacon", title: "Occupancy Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceOccupancy) input "voiceOccupiedOnly", "bool", title: "Report Only Occupancy Sensors That Are 'Occupied'", defaultValue: false 
        }
        section ("Water sensors", hideWhenEmpty: true) {
            input "voiceWater", "capability.waterSensor", title: "Water Sensors To Report Their Status...", multiple: true, required: false, submitOnChange: true
            if (voiceWater) input "voiceWetOnly", "bool", title: "Report Only Water Sensors That Are 'Wet'", defaultValue: false 
        }
    }
}
def pageEnviroReport(){
    dynamicPage(name: "pageEnviroReport", install: false, uninstall: false){
        section { paragraph "Environmental Report", image: parent.imgURL() + "temp.png" }
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
        	if (voiceTempSettings && !voiceTempSettingSummary){
            	input "voiceTempValue", "bool", title: "Report Current Temperature (At Thermostat)", defaultValue: false
                input "voiceTempHumid", "bool", title: "Report Current Humidity (At Thermostat)", defaultValue: false
                input "voiceTempState", "bool", title: "Report Current Thermostat State", defaultValue: false
                input "voiceTempMode", "bool", title: "Report Current Thermostat Mode", defaultValue: false
        	}    
        }
        section ("Luminosity (Lux) reporting", hideWhenEmpty: true){
        	input "voiceLux", "capability.illuminanceMeasurement", title: "Devices To Report Luminosity (Lux)", multiple: true, required: false
        }
        section ("UV index reporting", hideWhenEmpty: true){
        	input "voiceUV", "capability.ultravioletIndex", title: "Devices To Report UV Index", multiple: true, required: false
        }
    }
}
def pageSpeakerReport(){
    dynamicPage(name: "pageSpeakerReport", install: false, uninstall: false){
        section { paragraph "Speaker Report", image: parent.imgURL() + "speaker.png" }
        section(" "){
            input "voiceSpeaker", "capability.musicPlayer", title: "Speakers To Report Status...", description: "Tap to choose devices", multiple: true, required: false, submitOnChange:true
        	if (voiceSpeaker) input "voiceSpeakerOn", "bool", title: "Report Only Speakers That Are Playing", defaultValue:false
        }
        section("Please Note"){ paragraph "There may be up to a 5 minute delay before SmartThings refreshes the current speaker status. This may cause the report to produce inaccurate results.", image: parent.imgURL()+"info.png" }
    }
}
def pageBatteryReport(){
    dynamicPage(name: "pageBatteryReport", install: false, uninstall: false){
        section { paragraph "Battery Report", image: parent.imgURL() + "battery.png" }
        section(" "){
            input "voiceBattery", "capability.battery", title: "Devices With Batteries To Monitor...", description: "Tap to choose devices", multiple: true, required: false, submitOnChange:true
            if (voiceBattery) input "batteryThreshold", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: parent.battOptions()
        }
    }
}
def pageDeviceHealth(){
    dynamicPage(name: "pageDeviceHealth", install: false, uninstall: false){
        section { paragraph "Device Health Report", image: parent.imgURL() + "health.png" }
        section(" "){
            input "voiceHealth", "capability.sensor", title:"Devices To Report On...", description: "Tap to choose devices", multiple: true, required: false, submitOnChange:true
            if (voiceHealth) input "healthOffline", "bool", title: "Report Only Devices Not Online", defaultValue: false, submitOnChange:true
            if (voiceHealth && !healthOffline) input "healthSummary", "bool", title:"Consolidate Report When All Device Are Online", defaultValue: false
        }
    }
}
def pageHomeReport(){
    dynamicPage(name: "pageHomeReport", install: false, uninstall: false){
        section { paragraph "Mode And Security Report", image: parent.imgURL() + "modes.png" }
        section(" ") {
            input "voiceMode", "bool", title: "Report SmartThings Mode Status", defaultValue: false
            input "voiceSHM", "bool", title: "Report Smart Home Monitor Status", defaultValue: false
        }
    }
}
def pageOtherReports(){
    dynamicPage(name: "pageOtherReports", install: false, uninstall: false){
        section { paragraph "Include Other Reports", image: parent.imgURL() + "add.png" }
        section(" "){
             input "otherReportsList", "enum", title: "Include The Following Reports", required: false, options: parent.getMacroList("other", "${app.label}"), multiple: true
        }
    }
}
//---------------------------------------------------------------
def installed() {
    initialize()
}
def updated() {
	unsubscribe() 
    initialize()
}
def initialize() {
	sendLocationEvent(name: "askAlexa", value: "refresh", data: [macros: parent.getExtList()] , isStateChange: true, descriptionText: "Ask Alexa extension list refresh")
}
//Main Handlers
def getOutput(echoID){
	String outputTxt = "", feedData = ""
    try {
        outputTxt = voicePre ?  voicePre + " " : ""
        if (!voiceSwitchAll || (voiceSwitchAll && voiceSwitch && voiceSwitch.find{it.currentValue("switch") == "on"} && voiceSwitch.find{it.currentValue("switch") == "off"})){
        	if (voiceOnSwitchOnly) outputTxt += voiceSwitch ? switchOnReport(voiceSwitch, "switches") : ""
        	else outputTxt += voiceSwitch ? reportStatus(voiceSwitch, "switch") : ""
        } 
        else if (voiceSwitchAll && voiceSwitch && voiceSwitch.find {it.currentValue("switch") != "off"}) outputTxt += "All monitored switches are on. "
		else if (voiceSwitchAll && voiceSwitch && voiceSwitch.find {it.currentValue("switch") != "on"}) outputTxt += "All monitored switches are off. "
        if (voiceOnSwitchEvt) outputTxt += getLastEvt(voiceSwitch, "'switch on'", "on", "switch")
		if (!voiceDimmerAll || (voiceDimmerAll && voiceDimmer && voiceDimmer.find{it.currentValue("switch") == "on"} && voiceDimmer.find{it.currentValue("switch") == "off"})){
       		if (voiceOnDimmerOnly) outputTxt += voiceDimmer ? switchOnReport(voiceDimmer, "dimmers") : ""
        	else outputTxt += voiceDimmer ? reportStatus(voiceDimmer, "level") : ""
        }
        else if (voiceDimmerAll && voiceDimmer && voiceDimmer.find {it.currentValue("switch") != "on"}) outputTxt += "All monitored dimmers are off. "
        else if (voiceDimmerAll && voiceDimmer && voiceDimmer.find {it.currentValue("switch") != "off"}) outputTxt += "All monitored dimmers are on. "
		if (voiceOnDimmerEvt) outputTxt += getLastEvt(voiceDimmer, "'dimmer on'", "on", "dimmer")
        outputTxt += voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? doorWindowReport() : ""
        outputTxt += voiceWindowShades ? shadeReport() : ""
        if (voiceTemperature && (voiceTemperature.size() == 1 || !voiceTempAvg)) outputTxt += reportStatus(voiceTemperature, "temperature")
        else if (voiceTemperature && voiceTemperature.size() > 1 && voiceTempAvg) outputTxt += "The average of the monitored temperature devices is: " + parent.getAverage(voiceTemperature, "temperature") + " degrees. "
        if (voiceHumidity && (voiceHumidity.size() == 1 || !voiceHumidAvg)) outputTxt += reportStatus(voiceHumidity, "humidity")
        else if (voiceHumidity  && voiceHumidity.size() > 1 && voiceHumidAvg) outputTxt += "The average of the monitored humidity devices is " + parent.getAverage(voiceHumidity, "humidity") + "%. "
        if (voiceTempSettingSummary && voiceTempSettingsType && voiceTempSettingsType !="autoAll") outputTxt += voiceTempSettings ? thermostatSummary(): ""
        else outputTxt += (voiceTempSettings && voiceTempSettingsType) ? reportStatus(voiceTempSettings, voiceTempSettingsType) : ""
        outputTxt += voiceLux ? luxReport() : ""
        outputTxt += voiceUV ? uvReport() : ""
        outputTxt += fooBot ? airReport() : ""
        outputTxt += voiceSpeaker ? speakerReport() : ""
        outputTxt += voicePresence ? presenceReport() : ""
        outputTxt += voiceWater && waterReport() ? waterReport() : "" 
        outputTxt += voiceMotion && motionReport("motion") ? motionReport("motion") : ""
        outputTxt += voiceAccel && motionReport("acceleration") ? motionReport("acceleration") : ""
        outputTxt += voicePower && powerReport() ? powerReport() : ""
        outputTxt += voiceOccupancy && occupancyReport() ? occupancyReport() : ""
        outputTxt += voiceMode ? "The current SmartThings mode is set to, '${location.currentMode}'. " : ""
        if (voiceSHM){
            def currSHM = [off : "disarmed", away: "armed away", stay: "armed home"][location.currentState("alarmSystemStatus")?.value] ?: location.currentState("alarmSystemStatus")?.value
        	outputTxt += voiceSHM ? "The current Smart Home Monitor status is '${currSHM}'. " : ""
        }
        outputTxt +=voiceHealth && healthReport() ? healthReport() : ""
        outputTxt += voiceBattery && batteryReport() ? batteryReport() : ""
        if (otherReportsList) outputTxt +=parent.processOtherRpt(otherReportsList,echoID)
        outputTxt += voicePost ? voicePost : ""
	}
	catch(e){ outputTxt = "There was an error processing the report. Please try again. If this error continues, please contact the author of Ask Alexa. %1%" }
    def playContMsg = overRideMsg ? false : true
    def suppressContMsg = suppressCont && !overRideMsg && parent.contMacro
    if (!outputTxt && !allowNullRpt) outputTxt = "The voice report, '${app.label}', did not produce any output. Please check the configuration of the report within the SmartApp. %1%"  
    if (voiceRepFilter || voicePre || voicePost) outputTxt = parent.replaceVoiceVar(outputTxt,"",voiceRepFilter,"Voice",app.label,0,"")
    if (outputTxt && !outputTxt.endsWith("%") && !outputTxt.endsWith(" ")) outputTxt += " "
    if (outputTxt && !outputTxt.endsWith("%") && playContMsg && !suppressContMsg ) outputTxt += "%4%"
    else if (outputTxt && !outputTxt.endsWith("%") && suppressContMsg ) outputTxt += "%X%"   
    if (vrMsgQue){
        def expireMin=vrMQExpire ? vrMQExpire as int : 0, expireSec=expireMin*60
        def overWrite =!vrMQNotify && !vrMQExpire && vrMQOverwrite
        def msgTxt = outputTxt.endsWith("%") ? outputTxt[0..-4] : outputTxt
        sendLocationEvent(
            name: "AskAlexaMsgQueue", 
            value: "Ask Alexa Voice Report, '${app.label}'",
            unit: "${app.id}",
            isStateChange: true, 
            descriptionText: msgTxt, 
            data:[
                queues:vrMsgQue,
                overwrite: overWrite,
                notifyOnly: vrMQNotify,
                expires: expireSec,
                suppressTimeDate:vrSuppressTD   
            ]
        )
    }
    return outputTxt
}
//Child Common modules
def mqDesc(){
    def result = "Tap to add/edit the message queue options"
    if (vrMsgQue){
    	result = "Send to: ${translateMQid(vrMsgQue)}"
        result += vrMQNotify ? "\nNotification Mode Only" : ""
        result += vrMQExpire ? "\nExpires in ${vrMQExpire} minutes" : ""
        result += vrMQOverwrite ? "\nOverwrite all previous voice report messages" : ""
        result += vrSuppressTDRemind ? "\nSuppress Time and Date from Alexa Playback" : ""
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
def getOkToRun(echoID){ def result = (!runMode || runMode.contains(location.mode)) && parent.getDayOk(runDay) && getOkEcho(echoID) && parent.getTimeOk(timeStart,timeEnd) && parent.getPeopleOk(runPeople,runPresAll && switchesOnStatus() && switchesOffStatus()) }
def getOkEcho(echoID) { return !runEcho || runEcho.contains(echoID) }
private switchesOnStatus(){ return runSwitchActive && runSwitchActive.find{it.currentValue("switch") == "off"} ? false : true }
private switchesOffStatus(){ return runSwitchNotActive && runSwitchNotActive.find{it.currentValue("switch") == "on"} ? false : true }
def extAliasCount() { return 3 }
def extAliasDesc(){
	def result =""
	for (int i= 1; i<extAliasCount()+1; i++){
		result += settings."extAlias${i}" ? settings."extAlias${i}" : ""
		result += (result && settings."extAlias${i+1}") ? "\n" : ""
	}
    result = result ? "Alias Names currently configured; Tap to edit:\n"+result :"Tap to add alias names to this voice report"
    return result
}
def extAliasState(){
	def count = 0
    for (int i= 1; i<extAliasCount()+1; i++){
    	if (settings."extAlias${i}") count ++
    }
    return count ? "complete" : null
}
private getLastEvt(devGroup, evtTxt, searchVal, devTxt){
    def devEvt, evtLog=[],  lastEvt="I could not find any ${evtTxt} events in the log. "
	devGroup.each{ deviceName-> devEvt= deviceName.events()
		devEvt.each { if (it.value && it.value==searchVal) evtLog << [device: deviceName, time: it.date.getTime(), desc: it.descriptionText] }
	} 
    if (evtLog.size()>0){
        evtLog.sort({it.time})
        evtLog.reverse(true)
        def msgData= parent.timeDate(evtLog.time[0])
        def voiceDay = msgData.msgDay=="Today"? msgData.msgDay : "On " + msgData.msgDay
        if (voiceEvtTimeDate) lastEvt = "${voiceDay} at ${msgData.msgTime}. "
        else {
        	def multipleTxt = devGroup.size() >1 ? "within the monitored group was the ${evtLog.device[0]} ${devTxt}" : "was"
        	lastEvt = "The last ${evtTxt} event ${multipleTxt} ${voiceDay} at ${msgData.msgTime}. "
        }
    }    
    return lastEvt
}
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
        if (parent.nestCMD) parent.nestCmdPrep(device)
        try{ if (device.latestValue(voiceTempSettingsType) as int == voiceTempTarget as int)  matchCount ++ }
        catch (e) { err=true }
    }
    if (!err){
        def difCount = monitorCount - matchCount
        if (monitorCount == 1 &&  difCount==1) result +="The monitored thermostat, ${parent.getList(voiceTempSettings)}, is not set to ${voiceTempTarget} degrees. "
        else if (monitorCount == 1 && !difCount) result +="The monitored thermostat, ${parent.getList(voiceTempSettings)}, is set to ${voiceTempTarget} degrees. "
        if (monitorCount > 1) {
            if (difCount==monitorCount) result += "None of the thermostats are set to ${voiceTempTarget} degrees. "
            else if (matchCount==1) {
                for (device in voiceTempSettings){
                    if (parent.nestCMD) parent.nestCmdPrep(device)
                    if (device.latestValue(voiceTempSettingsType) as int == voiceTempTarget as int){
                        result += "Of the ${monitorCount} monitored thermostats, only ${device} is set to ${voiceTempTarget} degrees. "
                    }
                }
            }
            else if (difCount && matchCount>1) {
                result += "Some of the thermostats are set to ${voiceTempTarget} degrees except"
                for (device in voiceTempSettings){
                    if (parent.nestCMD) parent.nestCmdPrep(device)
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
                def n = type=="temperature" ? parent.roundValue(deviceName.latestValue(type)) : deviceName.latestValue(type)
                result += "The ${deviceName} is ${n}${appd}. "
			}
        }
    }
	else if (type != "autoAll") deviceList.each { deviceName->
        if (parent.nestCMD) parent.nestCmdPrep(deviceName)
        try { result += "The ${deviceName} is set to ${Math.round(deviceName.latestValue(type))}${appd}. " }
    	catch (e) { result += "The ${deviceName} is not able to provide its setpoint. Please choose another setpoint type to report on. " }
    	if (voiceTempValue || voiceTempHumid || voiceTempState || voiceTempMode) result += tstatAttrib(deviceName)
    }
    else if (type == "autoAll") deviceList.each { deviceName->
        if (parent.nestCMD) parent.nestCmdPrep(deviceName)
        try { 
        	result += "The ${deviceName} has a cooling setpoint of ${Math.round(deviceName.latestValue("coolingSetpoint"))}${appd}, " +
        		"and a heating setpoint of ${Math.round(deviceName.latestValue("heatingSetpoint"))}${appd}. " 
        }
    	catch (e) { result += "The ${deviceName} is not able to provide one of its setpoints. Ensure you are in a thermostat mode that allows reading of these setpoints. " }
    	if (voiceTempValue || voiceTempHumid || voiceTempState || voiceTempMode) result += tstatAttrib(deviceName)
    }
    return result
}
def tstatAttrib(tstat){
    String result = "In addition, this thermostat is "
    if (voiceTempValue) result += "reading ${tstat.latestValue("temperature")} degrees"
    if (voiceTempHumid && voiceTempValue) result += ", the relative humidity is ${tstat.currentValue("humidity")} percent"
    if (voiceTempHumid && !voiceTempValue) result += "reading ${tstat.currentValue("humidity")} percent relative humidity"
    if (voiceTempState && (voiceTempHumid || voiceTempValue)) result += " with the  "
    if (voiceTempState) result += "current operating state being '${tstat.currentValue("thermostatOperatingState")}'"
    if (voiceTempMode && (voiceTempHumid || voiceTempValue || voiceTempState)) result += " and is "
    if (voiceTempMode) result += "in '${tstat.currentValue("thermostatMode")}' mode"
    result +=". "
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
def occupancyReport(){
	String result = ""
	voiceOccupancy.each { deviceName->
		if (voiceOccupiedOnly){
           	if (deviceName.currentValue("occupancy")=="occupied") result += "'${deviceName}' is reading 'occupied'. "
    	}
        else {
           	result += "'${deviceName}' is reading '${deviceName.currentValue("occupancy")}'. "
       	}
    }
    if (result) result = "For the occupancy sensors, " + result
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
		if (voiceAeon){
        	def currKWH = deviceName.currentValue("currentKWH"), currPCost=deviceName.currentValue("kwhCosts")
            if (currKWH && currPCost) result += "In addition, this device is reading the current power usage is ${currKWH} at a cost of " + currPCost +". "
    	}
    }
    return result 
}
def luxReport(){
	String result = ""
    voiceLux.each{
    	def currValue = it.currentValue("illuminance") as int
        result += "The ${it.label} is reading ${currValue} lux. " 
    }
    return result
}
def uvReport(){
	String result = ""
    voiceUV.each{
    	def currValue = it.currentValue("ultravioletIndex") as int
        result += "The ${it.label} is reading '${parent.uvIndexReading(currValue)}', with a UV index of ${currValue}. " 
    }
    return result
}
def shadeReport(){
    String result = ""
    if (voiceShadeAll){	
        if (voiceWindowShades?.currentValue("windowShade").contains("Closed") || voiceWindowShades?.currentValue("windowShade").contains("Open") || voiceWindowShades?.currentValue("windowShade").contains("Partially open")){
            if (!voiceWindowShades?.currentValue("windowShade").contains("Closed") && !voiceWindowShades?.currentValue("windowShade").contains("Partially open")) result += "All of the window shades are open. "
            else if (!voiceWindowShades?.currentValue("windowShade").contains("Open") && !voiceWindowShades?.currentValue("windowShade").contains("Partially open")) result += "All of the window shades are closed. "
            else if (voiceWindowShades?.currentValue("windowShade").contains("Partially open") && !voiceWindowShades?.currentValue("windowShade").contains("Closed") && !voiceWindowShades?.currentValue("windowShade").contains("Open") ) result += "All of the window shades are partially open. "
			else voiceWindowShades.each{ result += "The ${it.label} is ${it.currentValue("windowShade").toLowerCase()}. " }
        }
        else {
        	if (!voiceWindowShades?.currentValue("windowShade").contains("closed")) result += "All of the window shades are open. "
			else if (!voiceWindowShades?.currentValue("windowShade").contains("open")) result += "All of the window shades are closed. "
            else voiceWindowShades.each{ result += "The ${it.label} is ${it.currentValue("windowShade").toLowerCase()}. " }
		}
	}
	else {    
    	voiceWindowShades.each { deviceName->
			def currVal = deviceName.currentValue("windowShade").toLowerCase()
        	result += "The ${deviceName} is " + currVal  + ". "
		}
    }
    return result
}
def doorWindowReport(){
	def countOpened = 0, countOpenedDoor = 0, countUnlocked = 0, listOpened=[], listUnlocked=[]
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
            result += countUnlocked > 1 ? "following are unlocked: ${parent.getList(listUnlocked)}. " :"${parent.getList(listUnlocked)} is unlocked. "
    	}
        if ((countOpened || countOpenedDoor) && !countUnlocked){
   			result += voiceDoorLocks ? "All of the doors are locked, but the " : "The "
            result += totalCount > 1 ? "following doors or windows are open: ${parent.getList(listOpened)}. " : "${parent.getList(listOpened)} is open. "
    	}
    }   
	else {
		if ((countOpened || countOpenedDoor) && !countUnlocked) result += totalCount > 1 ? "The following doors or windows are currently open: ${parent.getList(listOpened)}. " : "The ${parent.getList(listOpened)} is open. "
        if (!countOpened && !countOpenedDoor && countUnlocked) result += countUnlocked > 1 ? "The following doors are unlocked: ${parent.getList(listUnlocked)}. " : "The ${parent.getList(listUnlocked)} is unlocked. "
	}
    if ((countOpened || countOpenedDoor) && countUnlocked){
		def verb = totalCount > 1 ? "following doors or windows are currently open: ${parent.getList(listOpened)}" : "${parent.getList(listOpened)} is open"
		def verb1 = countUnlocked > 1 ? "following are unlocked: ${parent.getList(listUnlocked)}" : "${parent.getList(listUnlocked)} is unlocked"
		result += "The ${verb}. Also, the ${verb1}. "
    }
    if (voiceDoorEvt && (voiceDoorSensors ||voiceDoorControls )) result += getLastEvt(voiceDoorSensors, "open", "open", "sensor")
    if (voiceLockEvt && voiceDoorLocks) result += getLastEvt(voiceDoorLocks, "unlock", "unlocked", "sensor")
    return result
}
def batteryReport(){
    String result = ""
    def batteryThresholdLevel = batteryThreshold as int, batList = voiceBattery.findAll{(it.latestValue("battery") as int)< batteryThresholdLevel}
    if (batList) {
        batList.each{	
			if ((it.latestValue("battery") as int) > 1) result += "The '${it.label}' battery is at ${it.latestValue("battery")}%. "
			else if ((it.latestValue("battery") as int) !=0 && it.latestValue("battery") == null) result += "The '${it.label}' battery is reading null, which may indicate an issue with the device. "
			else if ((it.latestValue("battery") as int) <=1) result +="The '${it.label}' battery is at ${it.latestValue("battery")}%; time to change this battery. "
        }
	}
    return result
}
def healthReport(){
    String result = ""
    def healthList = healthOffline? voiceHealth.findAll{it.status==~/OFFLINE|INACTIVE/} : voiceHealth
    if (healthList) healthList.each{ result += "'${it.label}' is ${it.status}. " }
    if (healthSummary && !result.contains("OFFLINE") && !result.contains("INACTIVE") && !healthOffline) result ="All of the monitored devices are online. "
    return result
}
def waterReport(){
    String result = ""
    def wetList = voiceWater.findAll{it.latestValue("water") != "dry"}
	if (!voiceWetOnly) voiceWater.each { result += "The ${it.label} is ${it.latestValue("water")}. " }
	else if (wetList) wetList.each { result += "The ${it.label} is sensing water is present. " }
    return result
}
def airReport(){
	if (fooBotPoll) fooBot.poll()
    String result = ""
    fooBot.each{
		def currGPI = it.currentValue("GPIstate")
        if ((!fooBooRptLvl || fooBooRptLvl=="All") || (fooBooRptLvl=="Good" && currGPI==~/Good|Fair|Poor/) || (fooBooRptLvl=="Fair" && currGPI==~/Fair|Poor/) || (fooBooRptLvl=="Poor" && currGPI=="Poor")){
            result = "The Foobot air quality monitor, '${it.label}', is reading: '${currGPI}', with a Global Pollution Index of ${it.currentValue("pollution")}"
            if (fooBotCO2 || fooBoVOC || fooBotPart){
                result += ". In addition, "
                result += fooBotCO2 ? "the carbon dioxide reading is ${it.currentValue("carbonDioxide")} parts per million" : "" 
                result += fooBoVOC && fooBotCO2 && !fooBotPart ? " and " : fooBoVOC && !fooBotCO2 && !fooBotPart ? ". " : fooBoVOC && fooBotCO2 && fooBotPart ? ", " : ""
                result += fooBoVOC ? "the volatile organic compounds is reading ${it.currentVoc} parts per billion" : ""
                result += (fooBoVOC || fooBotCO2) && fooBotPart ? "and the particulate matter reading is ${it.currentParticle} µg/m³" : !(fooBoVOC || fooBotCO2) && fooBotPart ? "the particulate matter reading is ${it.currentParticle} µg/m³" : ""   
                if ((fooBotCO2 || fooBoVOC || fooBotPart) && (fooBotTemp || fooBotHum)) result +=". Finally, "
                else if ((fooBotTemp || fooBotHum) && !(fooBotCO2 || fooBoVOC || fooBotPart)) result += ". In addition, "
                result += fooBotTemp ? "the temperature reading at this device is ${it.currentValue("temperature")} degrees" : ""
                result += fooBotTemp && fooBotHum ? " and the relative humidity is ${it.currentValue("humidity")}%" : !fooBotTemp && fooBotHum ? "the relative humidity reading at this device is ${it.currentValue("humidity")}%" : ""
        	}
        result += ". "
        }
    }
    return result
}
//Main Menu Items
def getDesc(type){
	def result="Status: UNCONFIGURED - Tap to configure", count= 0
    switch (type){
    	case "switch" :
            if (voiceSwitch || voiceDimmer){
                def switchEvt = voiceOnSwitchEvt ? "/Event" : "", dimmerEvt =voiceOnDimmerEvt ? "/Event" : "", switchOn = voiceOnSwitchOnly ? "(On${switchEvt})" : "(On/Off${switchEvt})"
                def dimmer = voiceOnDimmerOnly ? "(On${dimmerEvt})" : "(On/Off${dimmerEvt})"
                result  = voiceSwitch && voiceSwitch.size()>1 ? "Switches ${switchOn}" : voiceSwitch && voiceSwitch.size()==1 ? "Switch ${switchOn}" : ""
                if (voiceDimmer) result  += voiceSwitch && voiceDimmer.size()>1 ? " and dimmers ${dimmer}" : voiceSwitch && voiceDimmer.size()==1 ? " and dimmer ${dimmer}" : ""
                if (voiceDimmer) result  += !voiceSwitch &&  voiceDimmer.size()>1 ? "Dimmers ${dimmer}" : !voiceSwitch && voiceDimmer.size()==1 ? "Dimmer ${dimmer}" : ""
                result += (voiceSwitch && voiceDimmer) || (voiceSwitch && voiceSwitch.size()>1) || (voiceDimmer && voiceDimmer .size()>1) ? " report status" : " reports status"
            }
            break
		case "door":
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
            break
		case "sensor":
        	if (voiceWater || voiceMotion || voicePower || voiceAccel || voiceOccupancy){
                def accelEvt = voiceAccelEvt ? "/Event" : ""
                def acceleration = voiceAccelOnly ? "(Active${accelEvt})" : "(Active/Not Active${accelEvt})"
                def water = voiceWetOnly ? "(Wet)" : "(Wet/Dry)"
                def arriveEvt = voicePresentEvt ? "/Arrival" : ""
                def departEvt = voiceGoneEvt ? "/Departure" : ""
                def motionEvt = voiceMotionEvt ? "/Event" : ""
                def motion = voiceMotionOnly ? "(Active${motionEvt})" : "(Active/Not Active${motionEvt})"
                def power = voicePowerOn ? "(Active)" : "(Active/Not Active)"
                def occupancy = voiceOccupiedOnly ? "(Occupied)" : "(All Sensor States)"
                result  = voiceAccel && voiceAccel.size()>1 ? "Acceleration sensors ${acceleration}" : voiceAccel && voiceAccel.size()==1 ? "Acceleration sensor ${acceleration}" : ""
                if (voiceMotion) result += result && voiceMotion.size()>1 ? ", motion sensors ${motion}" : result && voiceMotion.size()==1 ? ", motion sensor ${motion}" : ""
                if (voiceMotion) result += !result && voiceMotion.size()>1 ? "Motion sensors ${motion}" : !result && voiceMotion.size()==1 ? "Motion sensor ${motion}" : ""
                if (voicePower) result += result && voicePower.size()>1 ? ", power meters" : result && voicePower.size()==1 ? ", power meter" : ""
                if (voicePower) result += !result && voicePower.size()>1 ? "Power meters ${power}" : !result && voicePower.size()==1 ? "Power meter ${power}" : ""
                if (voiceWater)  result += result && voiceWater && voiceWater.size()>1 ? " and water sensors ${water}" : result && voiceWater && voiceWater.size()==1 ? " and water sensor ${water}" : ""
                if (voiceWater)  result += !result && voiceWater && voiceWater.size()>1 ? "Water sensors ${water}" :!result && voiceWater && voiceWater.size()==1 ? "Water sensor ${water}" : ""
                if (voiceOccupancy)  result += result && voiceOccupancy && voiceOccupancy.size()>1 ? " and occupancy sensors ${occupancy}" : result && voiceOccupancy  && voiceOccupancy.size()==1 ? " and occupancy sensor ${occupancy}" : ""
                if (voiceOccupancy)  result += !result && voiceOccupancy && voiceOccupancy.size()>1 ? "Occupancy sensors ${occupancy}" :!result && voiceOccupancy  && voiceOccupancy.size()==1 ? "Occupancy sensor ${occupancy}" : ""
                count += voiceWater ? voiceWater.size() : 0
                count += voiceMotion ? voiceMotion.size() : 0
                count += voicePower ? voicePower.size() : 0
                count += voiceAccel ? voiceAccel.size() : 0
                count += voiceOccpancy ? voiceOccupancy.size() : 0
                result += count>1 ? " report status" : " reports status"
    		}
            break
		case "presence":
            if (voicePresence){
                def present = voicePresRepType=="3" ? "(Away)" : voicePresRepType=="2" ? "(Present)" : voicePresRepType=="1" ? "(Summary)" : "(Present/Away)"
                result = voicePresence.size()>1 ? "Presence sensors ${present}" : "Presence sensor ${present}" 
                count += voicePresence ? voicePresence.size() : 0
                result += count>1 ? " report status" : " reports status"
            }
            break
		case "temp":
            if (voiceTemperature || voiceHumidity || voiceTempSettings || voiceUV || voiceLux ){
                def tempAvg = voiceTempAvg ? " (Average)" : ""
                def humidAvg = voiceHumidAvg  ? " (Average)" : ""
                def tstatSet = voiceTempSettingSummary && voiceTempSettings && voiceTempSettingsType !="autoAll" && voiceTempTarget ? "(setpoint summary target: ${voiceTempTarget} degrees)":"(setpoint)" 
                result  = voiceTemperature && voiceTemperature.size()>1 ? "Temperature sensors${tempAvg}" : voiceTemperature && voiceTemperature.size()==1 ? "Temperature sensor${tempAvg}" : ""
                
                if (voiceHumidity) result  += result && voiceHumidity.size()>1 && (voiceTempSettings || voiceLux || voiceUV) ? ", humidity sensors${humidAvg}" : result && voiceHumidity.size()==1 && (voiceTempSettings || voiceLux || voiceUV) ? ", humidity sensor${humidAvg}" : ""
                if (voiceHumidity) result  += result && voiceHumidity.size()>1 && !(voiceTempSettings || voiceLux || voiceUV)  ? " and humidity sensors${humidAvg}" : result && voiceHumidity.size()==1 && !(voiceTempSettings || voiceLux || voiceUV) ? " and humidity sensor${humidAvg}" : ""
                if (voiceHumidity) result  += !result && voiceHumidity.size()>1 ? "Humidity sensors${humidAvg}" : !result && voiceHumidity.size()==1 ? "Humidity sensor${humidAvg}" : ""
                
                if (voiceTempSettings) result += result && voiceTempSettings.size()>1 && !(voiceLux || voiceUV) ? " and thermostats ${tstatSet}" : result && voiceTempSettings.size()==1 && !(voiceLux || voiceUV)? " and thermostat${tstatSet}" : ""
                if (voiceTempSettings) result += result && voiceTempSettings.size()>1 && (voiceLux || voiceUV)? ", thermostats ${tstatSet}" : result && voiceTempSettings.size()==1 && (voiceLux || voiceUV) ? ", thermostat${tstatSet}" : ""
                if (voiceTempSettings) result += !result && voiceTempSettings.size()>1 ? "Thermostats ${tstatSet}" : !result && voiceTempSettings.size()==1 ? "Thermostat ${tstatSet}" : ""
               
                if (voiceLux) result += result && voiceLux.size()>1 && !voiceUV ? " and luminosity devices" : result && voiceLux.size()==1 && !voiceUV ? " and luminosity device" : ""
                if (voiceLux) result += result && voiceLux.size()>1 && voiceUV ? ", luminosity devices" : !result && voiceLux.size()==1 && voiceUV ? ", luminosity device" : ""
                if (voiceLux) result += !result && voiceLux.size()>1  ? "Luminosity devices" : !result && voiceLux.size()==1 ? "Luminosity device" : ""
                
                if (voiceUV) result += result && voiceUV.size()>1 ? " and UV index devices" : result && voiceUV.size()==1 ? " and UV index device" : ""
                if (voiceUV) result += !result && voiceUV.size()>1 ? "UV index devices" : !result && voiceUV.size()==1 ? "UV index device" : ""
                
                count += voiceHumidity ? voiceHumidity.size() : 0
                count += voiceTemperature ? voiceTemperature.size() : 0
                count += voiceTempSettings ? voiceTempSettings.size() : 0
                count += voiceLux ? voiceLux.size() : 0
                count += voiceUV ? voiceUV.size() : 0
                result += count>1 ? " report status" : " reports status"
                result += voiceTempValue || voiceTempHumid || voiceTempState || voiceTempMode ? " along with additional attributes" : ""
            }
            break
		case "battery":
        	if (voiceBattery && batteryThreshold && batteryThreshold !="101") result = voiceBattery.size()>1 ? "Batteries report when level < ${batteryThreshold}%" : "One battery reports when level < ${batteryThreshold}%"
    		if (voiceBattery && batteryThreshold && batteryThreshold =="101") result = voiceBattery.size()>1 ? "Batteries report their levels" : "One battery reports its level"
            break
        case "health":
        	if (voiceHealth) result = voiceHealth.size()>1 ? "Devices report health" : "One device reports health"
    		if (result && healthOffline) result += " when not online"
            break
		case "MSHM":
        	if (voiceMode || voiceSHM){
                result = "Report: "
                result += voiceMode ? "Mode Status" : ""
                result += voiceMode && voiceSHM ? " and " : ""
                result += voiceSHM ? "SHM Status" : ""
			}
            break
    	case "speaker":
        	if (voiceSpeaker && voiceSpeakerOn) result = voiceSpeaker.size()>1 ? "The active speakers" : "The active speaker"
    		else if (voiceSpeaker && !voiceSpeakerOn) result = voiceSpeaker.size()>1 ? "Speakers" : "One speaker"
    		if (voiceSpeaker) result += voiceSpeaker.size()>1 ? " report status" : " reports status"
    		break
        case "pollution":
            if (fooBot){
				result = fooBot.size()>1 ? "The Foobot devices report Global Pollution Index " : "The Foobot device reports Global Pollution Index"
				result += fooBotCO2 && !(fooBotTemp || fooBotHum || fooBotPart ||fooBotVOC) ? " & carbon dioxide" : fooBotCO2 && (fooBotTemp || fooBotHum || fooBotPart ||fooBotVOC) ? ", carbon dioxide" : "" 
				result += fooBotPart && !(fooBotTemp || fooBotHum ||fooBotVOC) ? " & particulate count" : fooBotCO2 && fooBotPart && (fooBotTemp || fooBotHum ||fooBotVOC) ? ", particulate count" : ""
				result += fooBotVOC && !(fooBotTemp || fooBotHum) ? " & volatile organic compounds" : fooBotVOC && (fooBotTemp || fooBotHum) ? ", volatile organic compounds" : ""
				result += fooBotTemp && !fooBotHum ? " & temperature" : fooBotTemp && fooBotHum ? ", temperature" : ""
				if (fooBotHum)  result += " &  humidity"
                result +=  fooBooRptLvl =="Good" ? ". Report only when GPI is 'Good' or below" : fooBooRptLvl =="Fair" ? ". Report only when GPI is 'Fair' or 'Poor'" : 
                	fooBooRptLvl =="Poor" ? ". Report only when GPI is 'Poor'" : ". Reports at all GPI levels"
				if (fooBotPoll) result += ". Data refreshed before report"
            }
            break
    	case "other":
        	if (otherReportsList) result = "Includes the following report(s):\n" + parent.getList(otherReportsList)	
    }
    return result
}
//Version/Copyright/Information/Help
private versionInt(){ return 109}
private def textAppName() { return "Ask Alexa Voice Report" }	
private def textVersion() { return "Voice Report Version: 1.0.9 (03/11/2018)" }