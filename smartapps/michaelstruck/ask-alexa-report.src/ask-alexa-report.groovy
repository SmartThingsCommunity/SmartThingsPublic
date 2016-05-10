/**
 *  Ask Alexa - Report
 *
 *  Version 1.0.0 - 5/10/16 Copyright © 2016 Michael Struck
 *  
 *  Version 1.0.0 - Initial release
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
    name: "Ask Alexa - Report",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Provide interfacing to control and report on SmartThings devices with the Amazon Echo ('Alexa').",
    category: "Convenience",
    parent: "MichaelStruck:Ask Alexa",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-report.src/AskAlexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-report.src/AskAlexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/ask-alexa-report.src/AskAlexa@2x.png"
    )
preferences {
    page name:"pageSetup"
    page name:"pageTempReport"
    page name:"pageHomeReport" 
}

// Show setup page
def pageSetup() {
	dynamicPage(name: "pageSetup", title: "Voice Reporting Settings", install: true, uninstall: true) {
        section {
			label title:"Voice Report Name", required: true,image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speak.png"
		}
        section("Voice Reports/Options") {
            input "voicePre", "text", title: "Pre Message Before Device Report", description: "Enter a message to play before the device report", defaultValue: "This is your voice report for %time%, %day%, %date%.", required: false
            href "pageSwitchReport", title: "Switch/Dimmer Report", description: reportDesc(voiceSwitch, voiceDimmer, "", "", ""), state: greyOutState(voiceSwitch, voiceDimmer, "", "", "")
            href "pagePresenceReport", title: "Presence Report", description: reportDesc(voicePresence, "", "", "", ""), state: greyOutState(voicePresence, "", "", "", "")
            href "pageDoorReport", title: "Door/Window Report", description: reportDesc(voiceDoorSensors, voiceDoorControls, voiceDoorLocks, "", ""), state: greyOutState(voiceDoorSensors, voiceDoorControls, voiceDoorLocks, "", "")
            href "pageTempReport", title: "Temperature/Humidity/Thermostat Report", description: reportDesc(voiceTemperature, voiceTempSettings, voiceTempVar, voiceHumidVar, voiceHumidity), state: greyOutState(voiceTemperature, voiceTempSettings, voiceTempVar, voiceHumidVar, voiceHumidity)
            href "pageBatteryReport",title: "Battery Report", description: reportDesc(voiceBattery, "", "", "", ""), state: greyOutState(voiceBattery, "", "", "", "")
            href "pageOtherReport", title: "Other Sensor Report", description: reportDesc(voiceWater, "", "", "", ""), state: greyOutState(voiceWater, "", "", "", "")
            href "pageHomeReport", title: "Mode and Smart Home Monitor Report", description: reportDescMSHM(), state: greyOutState(voiceMode, voiceSHM, "", "", "")
            input "voicePost", "text", title: "Post Message After Device Report", description: "Enter a message to play after the device report", required: false
        }
        section("Tap below to remove this report"){}
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
page(name: "pageOtherReport", title: "Other Sensor Report", install: false, uninstall: false){
	section {
        input "voiceWater", "capability.waterSensor", title: "Water Sensors To Report Their Status...", multiple: true, required: false 
        input "voiceWetOnly", "bool", title: "Report Only Sensors That Are 'Wet'", defaultValue: false 
    }
}
def pageTempReport(){
    dynamicPage(name: "pageTempReport", title: "Temperature/Humidity/Thermostat Report", install: false, uninstall: false){
        section ("Variables for pre/post messages") {
            input "voiceTempVar", "capability.temperatureMeasurement", title: "Temperature Device Variable (%temp%)",multiple: true, required: false, submitOnChange: true
            input "voiceHumidVar", "capability.relativeHumidityMeasurement", title:"Humidity Device Variable (%humid%)",multiple: true, required: false, submitOnChange: true
            if ((voiceTempVar && voiceTempVar.size()>1) || (voiceHumidVar && voiceHumidVar.size()>1)) paragraph "Please note: When multiple temperature/humidity devices are selected above, the variable output will be an average of the device readings"
        }
        section ("Individual devices to report on"){
            input "voiceTemperature", "capability.temperatureMeasurement", title: "Devices To Report Temperatures...",multiple: true, required: false
			input "voiceHumidity", "capability.relativeHumidityMeasurement", title: "Devices To Report Humidity...",multiple: true, required: false
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
page(name: "pageBatteryReport", title: "Battery Report", install: false, uninstall: false){
	section {
		input "voiceBattery", "capability.battery", title: "Devices With Batteries To Monitor...", description: "Tap to choose devices", multiple: true, required: false
        input "batteryThreshold", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: [5:"<5%",10:"<10%",20:"<20%",30:"<30%",40:"<40%",50:"<50%",60:"<60%",70:"<70%",80:"<80%",90:"<90%",101:"Always play battery level"]  
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
def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}
def updated() {
    log.debug "Updated with settings: ${settings}"
    initialize()
}
def initialize() {
}
//Voice Handler-------------------------------------------------------------
def reportResults(){
	def fullMsg = voicePre ? "${replaceVoiceVar(voicePre)} " : ""
	if (voiceOnSwitchOnly) fullMsg += voiceSwitch ? switchReport(voiceSwitch, "switches") : ""
	else fullMsg += voiceSwitch ? reportStatus(voiceSwitch, "switch") : ""
	if (voiceOnDimmerOnly) fullMsg += voiceDimmer ? switchReport(voiceDimmer, "dimmers") : ""
	else fullMsg += voiceDimmer ? reportStatus(voiceDimmer, "level") : ""
	fullMsg += voicePresence ? presenceReport() : ""
	fullMsg += voiceTemperature ? reportStatus(voiceTemperature, "temperature") : ""
    fullMsg += voiceHumidity ? reportStatus(voiceHumidity,"humidity") : ""
	if (voiceTempSettingSummary && voiceTempSettingsType) fullMsg += (voiceTempSettings) ? thermostatSummary(): ""
	else fullMsg += (voiceTempSettings && voiceTempSettingsType) ? reportStatus(voiceTempSettings, voiceTempSettingsType) : ""
    fullMsg += voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? doorWindowReport() : ""
    fullMsg += voiceBattery && batteryReport() ? batteryReport() : voiceBattery ? "All monitored batteries are above threshold. " : ""
    fullMsg += voiceWater && waterReport() ? waterReport() : voiceWater ? "All monitored water sensors are dry. " : ""
    fullMsg += voiceMode ? "The current SmartThings mode is set to, '${location.currentMode}'. " : ""
    fullMsg += voiceSHM ? "The current Smart Home Monitor status is '${location.currentState("alarmSystemStatus")?.value}'. " : ""
    fullMsg += voicePost ? "${replaceVoiceVar(voicePost)} " : ""
}
//Voice report sections---------------------------------------------------
def switchReport(devices, type){
	def result = ""
    if (devices.latestValue("switch").contains("on")) devices.each { deviceName->
    	if (deviceName.latestValue("switch")=="on") {
        	result += "The ${deviceName} is on"
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
    def appd = type=="temperature" || type=="thermostatSetpoint" || type == "heatingSetpoint" || type=="coolingSetpoint" ? "degrees" : type == "humidity" ? "percent relative humidity" : ""
    if (type != "thermostatSetpoint" && type != "heatingSetpoint" && type !="coolingSetpoint") deviceList.each {deviceName->result += "The ${deviceName} is ${deviceName.latestValue(type)} ${appd}. " }
    else deviceList.each { deviceName->
    	try { result += "${deviceName} is set to ${deviceName.latestValue(type) as int} ${appd}. " }
    	catch (e) { result = "${deviceName} is not able to provide its setpoint. Please choose another setpoint type to report on. " }
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
    	else result += "All of the monitored presence sensors are present. "
    	}
    else voicePresence.each {deviceName->result += "${deviceName} is " + deviceName.latestValue("presence") + ". " }
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
    	if (!totalCount) result += "All of the doors and windows are closed and locked. "
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
def batteryReport(){
    def result = "", count = 0, batteryThresholdLevel = batteryThreshold as int
	for (device in voiceBattery) if (device.latestValue("battery")< batteryThresholdLevel) count ++
    for (deviceName in voiceBattery){	
		if (deviceName.latestValue("battery") < batteryThresholdLevel){
			result += "The ${deviceName} battery is at ${deviceName.latestValue("battery")}%. "
			count = count - 1
		}
	}
    result
}
def waterReport(){
    def result = "", count = 0
	for (device in voiceWater) if (device.latestValue("water") != "dry") count ++
        if (!voiceWetOnly){
            for (deviceName in voiceWater) { result += "The ${deviceName} is ${deviceName.latestValue("water")}. " }
		}
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
//Common Code
def reportDesc(param1, param2, param3, param4, param5) {def result = param1 || param2 || param3 || param4 || param5  ? "Status: CONFIGURED - Tap to edit" : "Status: UNCONFIGURED - Tap to configure"}
def reportDescMSHM() {
	def result= "Status: "
    result += voiceMode ? "Report Mode: On" : "Report Mode: Off"
    result += voiceSHM ? ", Report SHM: On" : ", Report SHM: Off"
}
def greyOutState(param1, param2, param3, param4, param5){def result = param1 || param2 || param3 || param4 || param5 ? "complete" : ""}
def reportDesc(){
	def desc = voicePre ? "Pre-message" : ""
    desc += desc && (voiceSwitch || voiceDimmer) ? "\n" : ""
    desc += voiceSwitch || voiceDimmer ? "Switch/Dimmer" : ""
	desc += desc && voicePresence ? "\n" : ""
    desc += voicePresence ? "Presence Sensor" : ""
    desc += desc && (voiceDoorSensors || voiceDoorControls || voiceDoorLocks)  ? "\n" : ""
    desc += voiceDoorSensors || voiceDoorControls || voiceDoorLocks ? "Door/Window" : ""
    desc += desc && voiceTemperature || voiceHumidity ? "\n" : ""
    desc += voiceTemperature || voiceHumidity ? "Temperature/Humidity/Thermostat" : ""
    desc += desc && (voiceMode || voiceSHM) ? "\n" : ""
    desc += voiceMode || voiceSHM ? "Mode/Smart Home Monitor" : ""
    desc += desc && voicePost ? "\n" : ""
    desc += voicePost ? "Post-message" : ""
	desc = desc ? "Voice report includes:\n${desc}" : "UNCONFIGURED - Tap to configure"
}
def reportGreyOut(){
	def result = reportDesc() == "UNCONFIGURED - Tap to configure" ? "" : "complete"
}
private replaceVoiceVar(msg) {
    def df = new java.text.SimpleDateFormat("EEEE")
	location.timeZone ? df.setTimeZone(location.timeZone) : df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
	def day = df.format(new Date()), time = parseDate("","h:mm a"), month = parseDate("","MMMM"), year = parseDate("","yyyy"), dayNum = parseDate("","d")
    def temp = voiceTempVar ? getAverage(voiceTempVar, "temperature") + " degrees" : "undefined device"
    def humid = voiceHumidVar ? getAverage(voiceHumidVar, "humidity") + " percent relative humidity"	: "undefined device"
    msg = msg.replace('%day%', day)
    msg = msg.replace('%date%', "${month} ${dayNum}, ${year}")
    msg = msg.replace('%time%', "${time}")
    msg = msg.replace('%temp%', "${temp}")
    msg = msg.replace('%humid%', "${humid}")
    msg
}
private getAverage(device,type){
	def total = 0
	device.each {total += it.latestValue(type) }
    def result = ((total/device.size()) + 0.5) as int
}	
private parseDate(time, type){
	def formattedDate = time ? time : new Date(now()).format("yyyy-MM-dd'T'HH:mm:ss.SSSZ", location.timeZone)
    new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", formattedDate).format("${type}", timeZone(formattedDate))
}
//Version 
private def textVersion() {return "Voice Reports Version: 1.0.0 (05/10/2016)"}
private def versionInt() {return 100}
private def versionLong() {return "1.0.0"}
