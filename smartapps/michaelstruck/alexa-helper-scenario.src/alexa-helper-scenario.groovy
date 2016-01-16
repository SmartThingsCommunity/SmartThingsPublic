/**
 *  Alexa Helper-Child
 *
 *  Copyright 2016 Michael Struck
 *  Version 2.0.1 1/11/16
 * 
 *  Version 1.0.0 - Initial release of child app
 *  Version 1.1.0 - Added framework to show version number of child app and copyright
 *  Version 1.1.1 - Code optimization
 *  Version 1.2.0 - Added the ability to add a HTTP request on specific actions
 *  Version 1.2.1 - Added child app version information into main app
 *  Version 1.3.0 - Added ability to change the Smart Home Monitor status and added a section for the remove button
 *  Version 2.0.0 - Added in speaker and thermostat scenarios from main app to allow for more than one of these devices
 *  Version 2.0.1 - Fixed an issue with the getTimeOk routine
 * 
 *  Uses code from Lighting Director by Tim Slagle & Michael Struck
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
    description: "Child app (do not publish) that allows various SmartThings devices to be tied to switches controlled by Amazon Echo('Alexa')..",
    category: "Convenience",
	parent: "MichaelStruck:Alexa Helper",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png")
    
preferences {
    page name:"pageStart"
    page name:"pageControl"
}

def pageStart() {
	dynamicPage(name: "pageStart", title: "Scenario Settings", uninstall: true, install: true) {
    	section {
    		label title:"Scenario Name", required:true
    	   	input "scenarioType", "enum", title: "Scenario Type...", options: ["Control":"Modes/Routines/Switches/HTTP/SHM","Speaker":"Speaker","Thermostat":"Thermostat"], required: false, multiple: false, submitOnChange:true
        	if (scenarioType){
        		href "page${scenarioType}", title: "${scenarioType} Scenario Settings", description: "Tap to edit scenario settings..."
             }
        }
        if (scenarioType){
        	section("Restrictions") {            
				input "runDay", "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], title: "Only on certain days of the week...",  multiple: true, required: false
        		href "timeIntervalInput", title: "Only during a certain time...", description: getTimeLabel(timeStart, timeEnd), state: greyedOutTime(timeStart, timeEnd)
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
			input "AlexaSwitch", "capability.switch", title: "Alexa Switch", multiple: false, required: true
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
            	input "onSwitches", "capability.switch", title: "Turn on these switches...", multiple: true, required: false
            	input "onHTTP", "text", title:"Run this HTTP request...", required: false
            	input "delayOn", "number", title: "Delay in minutes", defaultValue: 0, required: false
			}
        }
        if (!showOptions || showOptions == "2") {
        	section ("When switch is off..."){
        		if (phrases) {
            		input "offPhrase", "enum", title: "Perform this routine", options: phrases, required: false
            	}
        		input "offMode", "mode", title: "Change to this mode", required: false
                input "offSHM", "enum",title: "Change Smart Home Monitor to...", options: ["away":"Arm(Away)", "stay":"Arm(Stay)", "off":"Disarm"], required: false
                input "offSwitches", "capability.switch", title: "Turn off these switches...", multiple: true, required: false
                input "offHTTP", "text", title:"Run this HTTP request...", required: false
                input "delayOff", "number", title: "Delay in minutes", defaultValue: 0, required: false
        	}
        }
    }
}
// Show "pageSpeaker" page
page(name: "pageSpeaker", title: "Speaker Scenario Settings", install: false, uninstall: false) {
	section {
        input "vDimmerSpeaker", "capability.switchLevel", title: "Alexa Dimmer Switch", multiple: false, required:false
		input "speaker", "capability.musicPlayer", title: "Connected Speaker To Control", multiple: false , required: false
	}
    section ("Speaker Volume Controls") {        
        input "speakerInitial", "number", title: "Volume when speaker turned on", required: false
        input "upLimitSpeaker", "number", title: "Volume Upper Limit", required: false
    	input "lowLimitSpeaker", "number", title: "Volume  Lower Limit", required: false
	}
	section ("Speaker Track Controls") {    
        input "nextSwitch", "capability.switch", title: "Next Track Switch", multiple: false, required: false
       	input "prevSwitch", "capability.switch", title: "Previous Track Switch", multiple: false, required: false
    }
}

// Show "pageThermostat" page
page(name: "pageThermostat", title: "Thermostat Scenario Settings", install: false, uninstall: false) {
	section {
    	input "vDimmerTstat", "capability.switchLevel", title: "Alexa Dimmer Switch", multiple: false, required:false
		input "tstat", "capability.thermostat", title: "Thermostat To Control", multiple: false , required: false
	}
    section ("Thermostat Temperature Settings") {
        input "upLimitTstat", "number", title: "Thermostat Upper Limit", required: false
    	input "lowLimitTstat", "number", title: "Thermostat Lower Limit", required: false
        input "autoControlTstat", "bool", title: "Control when thermostat in 'Auto' mode", defaultValue: false
	}
     section ("Thermostat Mode Settings") {
        input "heatingSwitch", "capability.switch", title: "Heating Mode Switch", multiple: false, required: false
        input "coolingSwitch", "capability.switch", title: "Cooling Mode Switch", multiple: false, required: false
        input "autoSwitch", "capability.switch", title: "Auto Mode Switch", multiple: false, required: false
        input "offSwitch", "capability.switch", title: "Thermostat Off Switch", multiple: false, required: false
        input "heatingSetpoint", "number", title: "Heating setpoint when turned on or mode change", required: false
        input "coolingSetpoint", "number", title: "Cooling setpoint when turned on or mode change", required: false
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
    if (scenarioType == "Control"){
        if (AlexaSwitch){
        	subscribe(AlexaSwitch, "switch", "switchHandler")
        }
	}
	//--------------------------------------------------------------------
    if (scenarioType == "Thermostat"){
    	if (vDimmerTstat && tstat && offSwitch){
    		subscribe (offSwitch, "switch", "thermoOffHandler")
		}   
    	//Set up subscriptions to various switches
		if (vDimmerTstat && tstat) {
    		subscribe (vDimmerTstat, "level", "thermoHandler")
        	if (heatingSwitch) {
        		subscribe (heatingSwitch, "switch", "heatHandler")
        	}
        	if (coolingSwitch) {
        		subscribe (coolingSwitch, "switch", "coolHandler")
        	}
        	if (autoSwitch) {
        		subscribe (autoSwitch, "switch", "autoHandler")
        	}
		}
    }
    //--------------------------------------------------------------------
    if (scenarioType == "Speaker"){
    	if (vDimmerSpeaker && speaker) {
    		subscribe (vDimmerSpeaker, "level", "speakerVolHandler")
        	subscribe (vDimmerSpeaker, "switch", "speakerOnHandler")
        	if (nextSwitch) {
        		subscribe (nextSwitch, "switch", "controlNextHandler")
        	}
        	if (prevSwitch) {
        		subscribe (prevSwitch, "switch", "controlPrevHandler")
        	} 
		}
    }
}

//Handlers

//Mode/Routine/Switch/HTTP/SHM-----------------------------------------------------------------
def switchHandler(evt) {
    if (getOkToRun()) {    
        log.debug "Alexa Helper scenario '${label}' triggered"
        if (evt.value == "on" && (!showOptions || showOptions == "1") && (onPhrase || onMode || onSwitches || onHTTP || onSHM)) {
        	if (!delayOn || delayOn == 0) {
            	turnOn()
            }
            else {
            	unschedule 
                runIn(delayOn*60, turnOn, [overwrite: true])
            }
    	} 
    	else if (evt.value == "off" && (!showOptions || showOptions == "2") && (offPhrase || offMode || offSwitches || offHTTP || offSHM)) {
        	if (!delayOff || delayOff == 0) {
            	turnOff()
            }
            else {
            	runIn(delayOff*60, turnOff, [overwrite: true])
            }
    	}
	}
}

def turnOn(){
	if (onPhrase){
		location.helloHome.execute(onPhrase)
	}
	if (onMode) {
		changeMode(onMode)
	}
    if (onSwitches){
    	onSwitches?.on()
    }
	if (onHTTP){
    	log.debug "Attempting to run: ${onHTTP}"
        httpGet("${onHTTP}")   
    }
    if (onSHM){
    	log.debug "Setting Smart Home Monitor to ${onSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${onSHM}")
    }
}

def turnOff(){
	if (offPhrase){
		location.helloHome.execute(offPhrase)
	}
	if (offMode) {
		changeMode(offMode)
	}
    if (offSwitches){
    	offSwitches?.off()
    }
    if (offHTTP){
    	log.debug "Attempting to run: ${offHTTP}"
        httpGet("${offHTTP}")
    }
    if (offSHM){
    	log.debug "Setting Smart Home Monitor to ${offSHM}"
        sendLocationEvent(name: "alarmSystemStatus", value: "${offSHM}")
    }
}
//Speaker Handlers-----------------------------------------------------------------
//Volume Handler
def speakerVolHandler(evt){
    if (getOkToRun()) {
        def speakerLevel = vDimmerSpeaker.currentValue("level") as int
    	if (speakerLevel == 0) {
    		vDimmerSpeaker.off()	
    	}
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
	if (getOkToRun()) {
    	if (evt.value == "on"){
    		if (speakerInitial){
        		def speakerLevel = speakerInitial as int
    			vDimmerSpeaker.setLevel(speakerInitial)
        	}
    		speaker.play()
    	}
    	else {
    		speaker.stop()
    	}
	}
}

def controlNextHandler(evt){
    if (getOkToRun()) {
    	speaker.nextTrack()
	}
}

def controlPrevHandler(evt){
	if (getOkToRun()) {
    	speaker.previousTrack()
	}
}    

//Thermostat Handlers-----------------------------------------------------------------
//Thermostat Off
def thermoOffHandler(evt){
	if (getOkToRun()) {
    	if (evt.value == "off"){
    		tstat.off()
    	}
	}
}

//Thermostat mode changes
def heatHandler(evt){
	if (getOkToRun()) {
    	tstat.heat()
    	if (heatingSetpoint){
    		tstat.setHeatingSetpoint(heatingSetpoint)
    	}
	}
}

def coolHandler(evt){
	if (getOkToRun()) {
    	tstat.cool()
    	if (coolingSetpoint){
    		tstat.setCoolingSetpoint(coolingSetpoint)
    	}
	}
}

def autoHandler(evt){
	if (getOkToRun()) {	
        tstat.auto()
    	if (heatingSetpoint){
        	tstat.setHeatingSetpoint(heatingSetpoint)
    	}
    	if (coolingSetpoint){
    		tstat.setCoolingSetpoint(coolingSetpoint)
    	}
	}
}

//Thermostat Temp Handler
def thermoHandler(evt){
    if (getOkToRun()) {
    // Get settings between limits
    	def tstatLevel = vDimmerTstat.currentValue("level") as int
    	tstatLevel = upLimitTstat && vDimmerTstat.currentValue("level") > upLimitTstat ? upLimitTstat : tstatLevel
    	tstatLevel = lowLimitTstat && vDimmerTstat.currentValue("level") < lowLimitTstat ? lowLimitTstat : tstatLevel
		//Turn thermostat to proper level depending on mode
    	def tstatMode=tstat.currentValue("thermostatMode")
    	if (tstatMode == "heat" || (tstatMode == "auto" && autoControlTstat)) {
        	tstat.setHeatingSetpoint(tstatLevel)	
    	}
    	if (tstatMode == "cool" || (tstatMode == "auto" && autoControlTstat)) {
        	tstat.setCoolingSetpoint(tstatLevel)	
    	}
    	log.debug "Thermostat set to ${tstatLevel}"
	}
}

//Common Methods-------------
def getOkToRun(){
	def result = (!runMode || runMode.contains(location.mode)) && getDayOk(runDay) && getTimeOk(timeStart,timeEnd) ? true : false    
	if (result){
    	log.debug "Alexa Helper scenario '${app.label}' triggered"
    }
    result
}

def changeMode(newMode) {
    if (location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
		} else {
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

def greyedOutTime(start, end){
	def result = start || end ? "complete" : ""
}

private hhmm(time) {
	new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", time).format("h:mm a", timeZone(time))
}

private getDayOk(dayList) {
	def result = true
    if (dayList) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
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

//Version
private def textVersion() {
    def text = "Child App Version: 2.0.1 (01/11/2016)"
}
