/**
 *  Ask Alexa 
 *
 *  Version 1.0.0 - 5/3/16 Copyright © 2016 Michael Struck
 *  Special thanks for Keith DeLong for code and assistance
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
    name: "Ask Alexa",
    namespace: "MichaelStruck",
    singleInstance: true,
    author: "Michael Struck",
    description: "Provide interfacing to control and report on SmartThings devices with the Amazon Echo ('Alexa').",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AskAlexa/AskAlexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AskAlexa/AskAlexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AskAlexa/AskAlexa@2x.png",
  	oauth: true)
preferences {
    page name:"mainPage"
    page name:"pageSwitches"
    page name:"pageReset"
    page name:"pageAbout"
    page name:"pageDoors"
    page name:"pageTemps"
    page name:"pageSettings"
    page name:"pageSpeakers"
    page name:"pageReports"
    page name:"pageSensors"
    page name:"pageHomeControl"
}
//Show main page
def mainPage() {
    dynamicPage(name: "mainPage", title:"", install: true, uninstall: false) {
		section("Items To Interface to Alexa") {
        	href "pageSwitches", title: "Switches/Dimmers/Colored Lights Control", description: getDesc(switches, dimmers ,cLights), state: getState(switches, dimmers, cLights),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/power.png"
            href "pageDoors", title: "Doors/Locks Control", description: getDesc(doors, locks, ocSensors), state: getState(doors, locks, ocSensors),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/lock.png"
            href "pageTemps", title: "Thermostats/Temperature Control", description:getDesc(tstats, temps, ""), state: getState (tstats, temps, ""),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/temp.png"
            href "pageSpeakers", title: "Connected Speakers Control", description: getDesc(speakers, "", ""), state: getState(speakers, "", ""),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speaker.png"     
            href "pageSensors", title: "Other Sensors", description:getDesc(water, "", ""), state: getState (water, "", ""),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/water.png"
            href "pageHomeControl", title: "Modes/Routines/SHM Control", description: getDesc(modes, routines, SHM), state: getState(modes, routines, SHM),
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png"        
        }
        section("Configure Voice Report"){
        	href "pageReports", title: "Voice Reports", description: reportDesc(), state: reportGrey(), image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speak.png"
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
    dynamicPage(name: "pageSwitches", title: none, install: false, uninstall: false) {
        section { paragraph "Switches/Dimmers/Colored Lights Control", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/power.png"} 
        section("Choose the devices to interface") {
            input "switches", "capability.switch", title: "Choose Switches (On/Off/Status)", multiple: true, required: false
            input "dimmers", "capability.switchLevel", title: "Choose Dimmers (On/Off/Level/Status)", multiple: true, required: false
            input "cLights", "capability.colorControl", title: "Choose Colored Lights (On/Off/Level/Color/Status)", multiple: true, required: false, submitOnChange: true
        }
        if (cLights) {
            section("Available colors"){
                def count=fillColorSettings().size-1
                def result = "Available colors are : "
                fillColorSettings().each{
                    count = count -1
                    result += it.name
                    if (count>0) result += ", "
                    if (count==0) result += " and "
                }
                paragraph result +="."
            }
            section("Custom color"){
                input "customName", "text", title: "Custom Color Name", required: false
                input "customHue", "number", title: "Custom Color Hue", required: false
                input "customSat", "number", title: "Custom Color Saturation", required: false
            }
        }
	}
}
def pageDoors() {
    dynamicPage(name: "pageDoors", title: none, install: false, uninstall: false) {
        section { paragraph "Doors/Locks Control", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/lock.png" }
        section("Choose the devices to interface") {
            input "doors", "capability.doorControl", title: "Choose Door Controls (Open/Close/Status)" , multiple: true, required: false
            input "locks", "capability.lock", title: "Choose Locks (Lock/Unlock/Status)", multiple: true, required: false
            input "ocSensors", "capability.contactSensor", title: "Open/Close Sensors (Status)", multiple: true, required: false
        }
    }
}
def pageTemps(){
    dynamicPage(name: "pageTemps", title: none, install: false, uninstall: false) {
        section {paragraph "Thermostats/Temperature Control", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/temp.png"}
        section("Choose the devices to interface") {
            input "tstats", "capability.thermostat", title: "Choose Thermostats (Temperature Setpoint/Status)", multiple: true, required: false 
            input "temps", "capability.temperatureMeasurement", title: "Choose Temperature Device (Status)", multiple: true, required: false
        }
    }
}
def pageSpeakers(){
    dynamicPage(name: "pageSpeakers", title: none, install: false, uninstall: false) {
        section {paragraph "Connected Speakers Control", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speaker.png"}
        section("Choose the devices to interface") {
            input "speakers", "capability.musicPlayer", title: "Choose Speakers (Speaker Control, Status)", multiple: true, required: false 
        }
        section("Speaker Control"){
        	paragraph "Speaker control consists of the following:\nPlay, Stop, Pause\nMute, Un-mute\nNext Track, Previous Track\nLevel (Volume)"
		}
    }
}
def pageSensors(){
    dynamicPage(name: "pageSensors", title: none, install: false, uninstall: false) {
        section {paragraph "Other Sensors", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/water.png"}
        section("Choose the devices to interface") {
            input "water", "capability.waterSensor", title: "Choose Water Sensors (Status)", multiple: true, required: false 
        }
    }
}
def pageHomeControl(){
	dynamicPage(name: "pageHomeControl", title:none, uninstall: false) {
        section { paragraph "Modes/Routines/SHM Control", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/modes.png" }
        section ("Choose the features you require voice control over") {
        	input "modes", "bool", title: "Modes (Change/Status)", defaultValue: false
            input "SHM", "bool", title: "Smart Home Monitor (Change/Status)", defaultValue: false
            input "routines", "bool", title: "Routines (Execute)", defaultValue: false
		}
    }
}
def pageReports() {
    dynamicPage(name: "pageReports", title: none, install: false, uninstall: false) {
        section{ paragraph "Voice Reports", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/speak.png" }
        if (childApps.size()) section("Reports Available"){}
        section(" "){
            app(name: "childReports", appName: "Ask Alexa - Report", namespace: "MichaelStruck", title: "Create New Voice Report...", description: "Tap to create new voice report", multiple: true, 
                    image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/add.png")
        }
  		section ("Please note") {
			paragraph "Be sure each report has a unique name."
        }
    }
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
        section {paragraph "${textAppName()}\n${textCopyright()}",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AskAlexa/AskAlexa@2x.png"}
        section ("SmartApp/Voice Report Version") { paragraph "${textVersion()}" } 
        section ("Access Token / Application ID"){
            if (!state.accessToken) OAuthToken()
            def msg = state.accessToken != null ? state.accessToken : "Could not create Access Token. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
            paragraph "Access Token:\n${msg}\n\nApplication ID:\n${app.id}"
    	}
        section ("Apache License"){ paragraph textLicense() }
    	section("Instructions") { paragraph textHelp() }
        section("Tap below to remove the application and all reports"){}
	}
}
def pageSettings(){
    dynamicPage(name: "pageSettings", title: none, uninstall: false){
        section { paragraph "Settings", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/settings.png" }
        section ("SmartApp Settings"){ 
        	input "otherStatus", "bool", title: "Speak Addition Status Attributes Of Devices", defaultValue: false, submitOnChange: true
            input "batteryWarn", "bool", title: "Speak Battery Level When Below Threshold", defaultValue: false, submitOnChange: true
            if (batteryWarn) input "batteryThres", "enum", title: "Battery Status Threshold", required: false, defaultValue: 20, options: [5:"<5%",10:"<10%",20:"<20%",30:"<30%",40:"<40%",50:"<50%",60:"<60%",70:"<70%",80:"<80%",90:"<90%",101:"Always play battery level"]  
        	input "pwNeeded", "bool", title: "Password (PIN) Required For Lock Or Door Commands", defaultValue: false, submitOnChange: true
            if (pwNeeded) input "password", "num", title: "Numeric Password(PIN)", description: "Enter a short numeric PIN (i.e. 1234)", required: false
        }	
        section ("Setup Variables (for Amazon Lambda/Developer Site)"){
        	if (!state.accessToken) OAuthToken()
            href url:"${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}", style:"embedded", required:false, title:"Show Setup Information", description: none
        }
        section ("Advanced") { 
        	href "pageReset", title: "Revoke/Reset Access Token", description: "Tap to take action - READ WARNING BELOW", 
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/warning.png"
        	    paragraph "By resetting the access token you will disable the ability to interface this SmartApp with "+        	
        		"your Amazon Echo. You will need to copy the new access token to your Amazon Lambda code to re-enable access."
        }
    }
}
def pageReset(){
	dynamicPage(name: "pageReset", title: "Access Token Reset"){
        section{
			revokeAccessToken()
            state.accessToken = null
            OAuthToken()
            def msg = state.accessToken != null ? "New access token:\n${state.accessToken}\n\nClick 'Done' above to return to the previous menu." : "Could not reset Access Token. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
	    	paragraph "${msg}"
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
def initialize() {
	if (!state.accessToken) {
		log.error "Access token not defined. Ensure OAuth is enabled in the SmartThings IDE."
	}
}
mappings {
      path("/d") { action: [GET: "processDevice"] }
      path("/setup") { action: [GET: "displayData"] }
      path("/r") { action: [GET: "processReport"] }
      path("/s") { action: [GET: "processStatus"] }
      path("/h") { action: [GET: "processSmartHome"] }
}
//--------------------------------------------------------------
def processDevice() {
    log.debug "Command received with params $params"
	def dev = params.Device 	//Label of device
	def op = params.Operator	//Operation to perform
    def numVal = params.Num     //Number for dimmer/PIN type settings
    def param = params.Param	//Other parameter (color, 
    if (numVal == "undefined" || numVal == "?" ) numVal = 0 
    def num = numVal as int
    log.debug "Dev: " + dev
    log.debug "Op: " + op
    log.debug "Num: " + num
    log.debug "Param: " + param
    def outputTxt = ""
    def count = 0
    if (switches && switches?.find{it.label.toLowerCase() == dev}){ outputTxt = getReply (switches, "switch", dev, op, num, param); count ++ }
    if (cLights && cLights?.find{it.label.toLowerCase() == dev}){ outputTxt = getReply (cLights, "color", dev, op, num, param); count ++ }
    if (doors && doors?.find{it.label.toLowerCase() == dev}) { outputTxt = getReply (doors, "door", dev, op, num, param); count ++}
	if (locks && locks?.find{it.label.toLowerCase() == dev}) { outputTxt = getReply (locks, "lock", dev, op, num, param); count ++}
    if (dimmers && dimmers?.find {it.label.toLowerCase() == dev}) { outputTxt = getReply (dimmers, "level", dev, op, num, param); count ++ }
    if (tstats && tstats?.find {it.label.toLowerCase() == dev}) { outputTxt = getReply (tstats, "thermostat" , dev, op, num, param); count ++ }
    if (speakers && speakers?.find {it.label.toLowerCase() == dev.toLowerCase()}) { outputTxt = getReply (speakers, "music" , dev.toLowerCase(), op, num, param); count ++ }
    if (outputTxt=="" && op != "report") outputTxt = "I had some problems finding the device you specified. Please try again." 
    if (count > 1) outputTxt ="You have multiple devices that are named '${dev}' in your SmartThings setup. Please rename these items to something unique so I may properly utlize them. "
    log.debug outputTxt
    return ["talk2me":outputTxt]
}
//Report Settings
def processReport() {
    log.debug "Command received with params $params"
	def rep = params.Report 	//Report name
    log.debug "Report Name: " + rep
    def outputTxt = ""
    def count =0
    if (childApps.size()){
        childApps.each {child -> if (child.label.toLowerCase().replaceAll(" report", "") == rep) { outputTxt = child.reportResults(); count ++;}}
        if (!outputTxt) outputTxt = "I could not find the ${rep} report. Please check the name of the report and try again." 
    }
    if (count > 1) outputTxt ="You have multiple reports named '${rep}' in your Ask Alexa SmartApp. Please rename these reports to something unique so I may properly utlize them. "
    log.debug outputTxt
    return ["talk2me":outputTxt]
}
def processStatus(){
	log.debug "Command received with params $params"
    def dev = params.Device     //Device asking for status
    log.debug "Device Status: " + dev
    def outputTxt = ""
    def count =0
    if (switches && switches?.find{it.label.toLowerCase() == dev}){ outputTxt = getReply (switches, "switch", dev, "status","", ""); count ++}
    if (cLights && cLights?.find{it.label.toLowerCase() == dev}){ outputTxt = getReply (cLights, "color", dev, op, num, param); count ++ }
    if (doors && doors?.find{it.label.toLowerCase() == dev}) { outputTxt = getReply (doors, "door", dev, "status", "", ""); count ++ }
	if (locks && locks?.find{it.label.toLowerCase() == dev}) { outputTxt = getReply (locks, "lock", dev, "status", "", ""); count ++ }
    if (ocSensors && ocSensors?.find{it.label.toLowerCase() == dev}) { outputTxt = getReply (ocSensors, "contact", dev, "status", "", ""); count ++ }
    if (dimmers && dimmers?.find {it.label.toLowerCase() == dev}) { outputTxt = getReply (dimmers, "level" , dev, "status", "", ""); count ++ }
    if (tstats && tstats?.find {it.label.toLowerCase() == dev}) { outputTxt = getReply (tstats, "thermostat" , dev, "status", "", ""); count ++ }
    if (temps && temps?.find {it.label.toLowerCase() == dev}) { outputTxt = getReply (temps, "temperature" , dev, "status", "", ""); count ++ }
    if (water && water?.find {it.label.toLowerCase() == dev}) { outputTxt = getReply (water, "water" , dev, "status", "", ""); count ++ }
    if (speakers && speakers?.find {it.label.toLowerCase() == dev.toLowerCase()}) { outputTxt = getReply (speakers, "music" , dev.toLowerCase(), "status", "", ""); count ++ }
    if (outputTxt=="") outputTxt = "I had some problems finding the device you specified. Please try again."   
	if (count > 1) outputTxt ="You have multiple devices that are named '${dev}' in your SmartThings setup. Please rename these items to something unique so I may properly utlize them. "
    log.debug outputTxt
    return ["talk2me":outputTxt]
}
def processSmartHome() {
    log.debug "Command received with params $params"
	def cmd = params.SHCmd 		//Smart Home Command
	def param = params.SHParam	//Smart Home Parameter
    log.debug "Cmd: " + cmd
    log.debug "Param: " + param
   	def outputTxt = ""
    if (cmd == "mode"){
        def currMode = location.mode.toLowerCase(), modeCount=location.modes.size()
        def modeNames = "The available modes include the following: "
        if (modeCount>0){ location.modes.each{ modeNames += it
            modeCount=modeCount-1
            if (modeCount>1) modeNames+=", "
            if (modeCount==1) modeNames+=" and "}
            modeNames+=". "
        }
        if (param=="undefined") {
            outputTxt ="The current SmartThings mode is set to, '${currMode}'. "
        }
        if (location.modes?.find{it.name.toLowerCase()==param} && param != currMode) {
        	def newMode=location.modes?.find{it.name.toLowerCase()==param}
            outputTxt ="I am setting the SmartThings mode to, '${newMode}'. "
            setLocationMode(newMode)
    	}
        else if (param == currMode) outputTxt ="The current SmartThings mode is set already set to, '${currMode}'. No changes are being made. "
    	if (param=="help"){
            if (location.modes.size() >0) outputTxt=modeNames
            else outputTxt = "There are no modes defined within your SmartThings' account. "
		}
        if (!outputTxt) outputTxt = "I did not understand the mode you wanted to set. For a list of available modes, simply say, 'ask SmartThings for mode help'.  "
    }
    if (cmd=="security" || cmd=="smart home" || cmd=="smart home monitor"){
    	def SHMstatus = location.currentState("alarmSystemStatus")?.value
		def SHMFullStat = [off : "disarmed", away: "armed away", stay: "armed stay"][SHMstatus] ?: SHMstatus
        def newSHM = "", SHMNewStat = "" 
        if (param=="help" || param=="arm" || param=="undefined") {
            if (param=="arm") outputTxt ="I did not understand how you want me to arm the Smart Home Monitor. Be sure to say 'armed stay' or 'armed away' to properly change the setting. "
            if (param=="help") outputTxt="The valid Smart Home Monitor commands are: 'disarm','away' or 'stay'. "
            if (param=="undefined") outputTxt ="The Smart Home Monitor is currently set to, '${SHMFullStat}'. "
        }
        else {
            if (param =="off" || param =="disarm") newSHM="off"
            if (param =="away" || param =="armed away") newSHM="away"
            if (param =="stay" || param =="armed stay") newSHM="stay"
    		if (newSHM && SHMstatus!=newSHM) {
        		sendLocationEvent(name: "alarmSystemStatus", value: newSHM)
        		SHMNewStat = [off : "disarmed", away: "armed away", stay: "armed stay"][newSHM] ?: newSHM
            	outputTxt ="I am setting the Smart Home monitor to, '${SHMNewStat}'. "
        	}
            else if (SHMstatus==newSHM) outputTxt ="The Smart Home Monitor is already set to, '${SHMFullStat}'. No changes are being made. " 
        	else if (!newSHM) outputTxt ="I did not understand how you wanted to set the Smart Home Monitor. The proper commands are 'disarm','away' or 'stay'. "
    	}
	}
    if (cmd=="routine"){
    	def phrases = location.helloHome?.getPhrases()*.label, phraseCount = phrases.size()
        def phraseNames="The available routines include the following: "
        if (phrases){ phrases.each{ phraseNames += it 	
            phraseCount=phraseCount-1
            if (phraseCount>1) phraseNames+=", "
            if (phraseCount==1) phraseNames+=" and " }
            phraseNames+=". "
        }
        def runRoutine = phrases.find{it.replaceAll("[^a-zA-Z ]", "").toLowerCase()==param}
        if (runRoutine) {
        	location.helloHome?.execute(runRoutine)
            outputTxt="I am executing the '${param}' routine. "
        }
        if (param=="help"){
            if (phrases.size() >0) outputTxt=phraseNames
            else outputTxt = "There are no routines set up within your SmartThings' account. "
		}
        if (!outputTxt) outputTxt ="To run SmartThings' routines, ask me to run the routine by its full name. For a list of available routines, simply say, 'ask SmartThings for routine help'. "
    }
	log.debug outputTxt
    return ["talk2me":outputTxt]
}
def getReply(devices, type, dev, op, num, param){
	def result = ""
    log.debug "Type: " + type
	try {
    	def STdevice = devices?.find{it.label.toLowerCase() == dev}
        def supportedCaps = STdevice.capabilities
        if (op=="status") {
            if (type == "temperature"){
                result = "The temperature of the ${STdevice} is ${STdevice.currentValue(type)} degrees"
                if (otherStatus) {
                    def humidity =STdevice.currentValue("humidity"), lux =STdevice.currentValue("illuminance"), wet=STdevice.currentValue("water")
                    result += humidity ? ", and the relative humidity is ${humidity}%." : ". "
                    result += lux ? " In addition, the illuminance is currently at ${lux} lux." : ""
                    result += wet ? "Also, this device is a leak sensor, and it is currently ${wet}. " : ""
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
                result = "The ${STdevice} temperature reading is currently ${temp as int} degrees"
                if (otherStatus){
                    def heat = STdevice.currentValue("heatingSetpoint"), cool = STdevice.currentValue("coolingSetpoint")
                    def humidity = STdevice.currentValue("humidity"), opState = STdevice.currentValue("thermostatMode")
                    result += opState ? " and the thermostat mode is, '${opState}'." : ". "
                    result += humidity ? "The relative humidity reading is ${humidity}%." : ""
                    result += heat ? " The heating setpoint is set to ${heat as int} degrees. " : ""
                    result += humidity || cool ? " and finally, " : ""
                    result += cool ? " The cooling setpoint is set to ${cool as int} degrees. " : ""
            	}
            }
            else if (type == "contact"){
                result = "The ${STdevice} is currently ${STdevice.currentValue(type)}. "
                if (otherStatus){
                	def temp = STdevice.currentValue("temperature")
                    result += "In addition, the temperature reading from this device is ${temp as int} degrees. "
                }
            }
            else if (type == "music"){
                def onOffStatus = STdevice.currentValue("status"), track = STdevice.currentValue("trackDescription"), level = STdevice.currentValue("level")
                def mute = STdevice.currentValue("mute")
                result = "The ${STdevice} is currently ${onOffStatus}"
                result += onOffStatus =="stopped" ? ". " : onOffStatus=="playing" && track ? ": '${track}'" : ""
                result += onOffStatus == "playing" && level && mute =="unmuted" ? ", and it's volume is set to ${level}%. " : mute =="muted" ? ", and it's currently muted. " :""
            }
            else if (type == "water"){
                result = "The water sensor, ${STdevice}, is currently ${STdevice.currentValue(type)}. "
                if (otherStatus){
                	def temp = STdevice.currentValue("temperature")
                    result += "In addition, the temperature reading from this device is ${temp as int} degrees. "
                }
            }
            else result = "The ${STdevice} is currently ${STdevice.currentValue(type)}. "
        }
        else {
            if (type == "thermostat"){
                if ((param=="heat" || param=="heating" || param =="cool" || param=="cooling" || param =="auto" || param=="automatic") && num == 0 && op=="undefined") op="on"
                if (op == "on" || op=="off") {
                	if (param == "undefined" && op == "on") result="You must designate 'heating mode' or 'cooling mode' when turning the ${STdevice} on. "
                    if (param =="heat" || param=="heating") result="I am setting the ${STdevice} to 'heating' mode. "
                    if (param =="cool" || param=="cooling") result="I am setting the ${STdevice} to 'cooling' mode. "
                    if (param =="auto" || param=="automatic") result="I am setting the ${STdevice} to 'auto' mode. Please note, "+
                    	"to properly set the temperature in 'auto' mode, you must specify the heating or cooling setpoints. "
                    if (op =="off") result = "I am turning the ${STdevice} thermostat ${op}. "; STdevice.off()
                }
                else {
                    //if (num == 0) result = "I didn't understand the temperature setting. Please try again. "
                    if (param == "undefined") 
                        if (STdevice.currentValue("thermostatMode")=="heat") param = "heat"
                        else if (STdevice.currentValue("thermostatMode")=="cool") param = "cool"
                        else result = "You must designate a 'heating' or 'cooling' parameter when setting the temperature. "+
                            "The thermostat will not accept a generic setpoint. For example, you could simply say, 'Set the ${STdevice} heating to 65 degrees'. "
                    if ((param =="heat" || param =="heating") && num > 0) {
                        result="I am setting the heating setpoint of the ${STdevice} to ${num} degrees. "
                        STdevice.setHeatingSetpoint(num)
                    }
                    if ((param =="cool" || param =="cooling") && num>0) {
                        result="I am setting the cooling setpoint of the ${STdevice} to ${num} degrees. "
                        STdevice.setCoolingSetpoint(num)
                    }
                }
            }
            if (type == "color" || type == "level" || type=="switch"){
            	if ((type == "switch") || ((type=="level" || type == "color") && num==0 )){
               		if ((type=="level" || type == "color") && num==0 && op=="undefined" && param=="undefined") op="off"
                	if (op=="on" || op=="off"){
                		STdevice."$op"() 
                		result = "I am turning the ${STdevice} ${op}."
                	}
            	}
                if ((type == "color" || type == "level") && num > 0 && param=="undefined") {
                	STdevice.setLevel(num)
                    result = "I am setting the ${STdevice} to ${num}%. "                    
				}
                if (type == "color" && param !="undefined" && supportedCaps.name.contains("Color Control")){
                    def getColorData = fillColorSettings().find {it.name.toLowerCase()==param}
                    if (getColorData){
                        def hueColor = getColorData.hue
                        def satLevel = getColorData.sat
                        def newLevel = num > 0 ? num : STdevice.currentValue("level")
                        def newValue = [hue: hueColor as int, saturation: satLevel as int, level: newLevel]
                        STdevice?.setColor(newValue)
                        result = "I am setting the color of the ${STdevice} to ${param}"
                        result += num>0 ? ", at a level of ${num}%. " : ". "
                	}
                }
            	if (!result){
                	if (type=="switch") result = "For the ${STdevice} switch, be sure to give an 'on' or 'off' command. "
            		if (type=="level") result = "For the ${STdevice} dimmer, be sure to use an 'on', 'off', or brightness level setting. "
            		if (type=="color") result = "For the ${STdevice} color controller, remember it can be operated like a switch. You can ask me to turn "+  
                    "it on, off, or set a brightness level. You can also set it to a variety of common colors. Please refer to your SmartApp for a listing of these colors. "
                }
            }
            if (type == "music"){
				if (op=="off" || op=="stop") {STdevice.stop(); result = "I am stopping the  ${STdevice} speaker. "}
                else if (op == "play") {STdevice.play(); result = "I am playing the ${STdevice} speaker. "}
                else if (op=="mute") {STdevice.mute(); result = "I am muting the ${STdevice} speaker. "}
                else if (op=="unmute") {STdevice.unmute(); result = "I am unmuting the ${STdevice} speaker. "}
                else if (op=="pause") {STdevice.pause(); result = "I am pausing the ${STdevice} speaker. "}
                else if (op=="next track") {STdevice.nextTrack(); result = "I am playing the next track on the ${STdevice} speaker. "}
            	else if (op=="previous track") {STdevice.previousTrack(); result = "I am playing the next track on the ${STdevice} speaker. "}
                else result = "I didn't understand what you wanted me to do with the ${STdevice} speaker. "
                if (num > 0) {STdevice.setLevel(num); result = "I am setting the volume of the ${STdevice} speaker to ${num}. "}
        	}
            if (type == "door"){
                def currentDoorState = STdevice.currentValue(type)
				if (currentDoorState==op || (currentDoorState == "closed" && op=="close")) result = "The ${STdevice} is already ${currentDoorState}. "
                else {
                    if (op != "open" || op != "close") result ="For the ${STdevice}, you must give an 'open' or 'close' command. "
                    if ((op=="open" || op=="close") && (pwNeeded && password && num != password as int)) result="You must say your password to ${op} the ${STdevice}. "
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
                    if ((op=="lock" || op=="unlock") && (pwNeeded && password && num != password as int)) result= "You must say your password to ${op} the ${STdevice}. "
                    else if ((op=="lock" || op=="unlock") && (!pwNeeded || (password && pwNeeded && num ==password as int) || !password)) {
                        STdevice."$op"()
                        result = "I am ${op}ing the ${STdevice}. "
                    }
                }
        	}
		}
        if (STdevice.currentValue("battery") && batteryWarn){
                def battery = STdevice.currentValue("battery")
                def battThresLevel = batteryThres as int
				result += battery && battery < battThresLevel ? "Please note, the battery in this device is at ${battery}%. " : ""
		}
    }
	catch (e){ result = "I could not process your request on ${dev}. Ensure you are using the correct commands with the device and try again. " }
    result
}
def setupData(){
	log.info "Set up web page located at : ${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}"
    def result ="<div style='padding:10px'><i><b>Lambda code variables:</b></i><br><br>var STappID = '${app.id}';<br>var STtoken = '${state.accessToken}';<br>"
    result += "var url='${getApiServerUrl()}/api/smartapps/installations/' + STappID + '/' ;<br><br><hr>"
    result += "<i><b>Amazon ASK Custom Slot Information:</b></i><br><br>LIST_OF_DEVICES<br><br>"
    if (switches) switches.each{ result += it.label.toLowerCase()+"<br>" }
    if (dimmers) dimmers.each{ result += it.label.toLowerCase()+"<br>" }
    if (cLights) cLights.each{ result += it.label.toLowerCase()+"<br>" }
    if (doors) doors.each{ result += it.label.toLowerCase()+"<br>" } 
    if (locks) locks.each{ result += it.label.toLowerCase()+"<br>" }
    if (ocSensors) ocSensors.each{ result += it.label.toLowerCase()+"<br>" }
    if (tstats) tstats.each{ result += it.label.toLowerCase()+"<br>" }
    if (temps) temps.each{result += it.label.toLowerCase()+"<br>" }
    if (speakers) speakers.each{result += it.label.toLowerCase()+"<br>" }
    if (water) water.each{result += it.label.toLowerCase()+"<br>" }
    result += "<br><br>LIST_OF_OPERATIONS<br><br>on<br>off<br>"
    result += locks ? "lock<br>unlock<br>" : ""
    result += doors ? "open<br>close<br>" : ""
    result += speakers ? "play<br>stop<br>pause<br>mute<br>unmute<br>next track<br>previous track<br>" : ""
    result += "<br>"
    if (childApps.size()) {
    	result += "<br>LIST_OF_REPORTS<br><br>"
    	childApps.each { result += it.label.toLowerCase().replaceAll(" report", "")+"<br>" }
	}
    if (tstats || cLights) {
    	result += "<br>LIST_OF_PARAMS<br><br>"
        if (tstats) result += "heat<br>cool<br>heating<br>cooling<br>auto<br>automatic<br>"
        if (cLights) fillColorSettings().each {result += it.name.toLowerCase()+"<br>"}
    }
    if (modes || routines || SHM) {
        result +="<br>LIST_OF_SHPARAM<br><br>help<br>"
        def cmd = "<br>LIST_OF_SHCMD<br><br>"
        if (routines){
            cmd += "routine<br>"
            def phrases = location.helloHome?.getPhrases()*.label
            if (phrases) phrases.each{result += it.replaceAll("[^a-zA-Z ]", "").toLowerCase() + "<br>"}
        }
        if (SHM) {
        	cmd += "security<br>smart home<br>smart home monitor<br>"
            result +="arm<br>disarm<br>stay<br>armed stay<br>armed away<br>off<br>"
        }
        if (modes) {
        	cmd += "mode<br>"
            location.modes.each {result += it.name.replaceAll("[^a-zA-Z ]", "").toLowerCase() + "<br>"}
        }
	result += cmd
    }	
	result += "<br><hr><br><i><b>URL of this setup page:</b></i><br><br>${getApiServerUrl()}/api/smartapps/installations/${app.id}/setup?access_token=${state.accessToken}<br><br><hr></div>"
}
def displayData(){
	render contentType: "text/html", data: """<!DOCTYPE html><html><head><meta charset="UTF-8" /><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/></head><body style="margin: 0;">${setupData()}</body></html>"""
}
//Common Code
def OAuthToken(){
	try {
        createAccessToken()
		log.debug "Creating new Access Token"
	} catch (e) { log.error "Access Token not defined. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth." }
}
def reportDesc(){def results = childApps.size() ? childApps.size()==1 ? "One Voice Report Configured" : childApps.size() + " Voice Reports Configured" : "No Voices Reports Configured\nTap to create new report"}
def reportGrey(){def results =childApps.size() ? "complete" : ""}
def getDesc(param1, param2, param3) {def result = param1 || param2 || param3 ? "Status: CONFIGURED - Tap to edit/view" : "Status: UNCONFIGURED - Tap to configure"}
def getState(param1, param2, param3) {def result = param1 || param2 || param3 ? "complete" : ""}
def fillColorSettings(){ 
	def colorData = []
    colorData << [name: "White", hue: 10, sat: 56] << [name: "Orange", hue: 8, sat: 100]
    colorData << [name: "Red", hue: 100, sat: 100] << [name: "Green", hue: 37, sat: 100]
    colorData << [name: "Blue", hue: 64, sat: 100] << [name: "Yellow", hue: 16, sat: 100] 
    colorData << [name: "Purple", hue: 78, sat: 100] << [name: "Pink", hue: 87, sat: 100]
    if (customName && (customHue > -1 && customerHue < 101) && (customSat > -1 && customerSat < 101)){
    	colorData << [name: customName, hue: customHue as int, sat: customSat as int]
    }
	return colorData
}
//Version/Copyright/Information/Help
private def textAppName() { def text = "Ask Alexa" }	
private def textVersion() {
    def version = "Parent App Version: 1.0.0 (05/03/2016)"
    def childCount = childApps.size()
    def childVersion = childCount ? childApps[0].textVersion() : "No voice reports installed"
    return "${version}\n${childVersion}"
}
private def versionInt(){ def text = 100 }
private def textCopyright() { def text = "Copyright © 2016 Michael Struck" }
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
private def textHelp() { def text = "This SmartApp allows provides an interface to control and query the SmartThings environment via the Amazon Echo ('Alexa')"}