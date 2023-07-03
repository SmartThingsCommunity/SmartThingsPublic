/**
 *  Zwave Thermostat Manager
 * 
 * Credits and Kudos: 	this app is largely based on the more popular Thermostat Director SA by Tim Slagle - 
 * 						many thanks to @slagle for his continued support. 
 * 						Without his brilliance, this app would not exist!
 * 
 * 
 *  Copyright 2015 SmartThings
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
	name: "ZWave Thermostat Manager",
	namespace: "BD",
	author: "Bobby Dobrescu",
	description: "Adjust zwave thermostats based on a temperature range of a specific temperature sensor",
	category: "My apps",
	iconUrl: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",
	iconX2Url: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png"
)

preferences {
    page name:"pageSetup"
    page name:"TemperatureSettings"
    page name:"ThermostatandDoors"
    page name:"ThermostatAway"
    page name:"Settings"

}

// Show setup page
def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

	return dynamicPage(pageProperties) {
        section("General Settings") {
            href "TemperatureSettings", title: "Ambiance", description: "", state:greyedOut()
            href "ThermostatandDoors", title: "Disabled Mode", description: "", state: greyedOutTherm()
            href "ThermostatAway", title: "Away Mode", description: "", state: greyedOutTherm2()
			href "Settings", title: "Other Settings", description: "", state: greyedOutSettings()
         }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false       
        }
    }
}

// Page - Temperature Settings	
def TemperatureSettings() {

    def sensor = [
        name:       "sensor",
        type:       "capability.temperatureMeasurement",
        title:      "Which Temperature Sensor(s)?",
        multiple:   true,
        required:   false
    ]
    def thermostat = [
        name:       "thermostat",
        type:       "capability.thermostat",
        title:      "Which Thermostat?",
        multiple:   false,
        required:   true
    ]
    def setLow = [
        name:       "setLow",
        type:       "number",
        title:      "Low temp?",
        required:   true
    ]
    
    def cold = [
        name:       "cold",
        type:       "enum",
        title:		"Mode?",
    	required:   false,
		metadata:   [values:["auto", "heat", "cool", "off"]]

    ]

    def SetHeatingLow = [
        name:       "SetHeatingLow",
        type:       "number",
        title:		"Heating Temperature (degrees)",
        required:   true
    ]
    
     def SetCoolingLow = [
        name:       "SetCoolingLow",
        type:       "number",
        title:		"Cooling Temperature (degrees)",
        required:   false
    ]   
    
    def setHigh = [
        name:       "setHigh",
        type:       "number",
        title:      "High temp?",
        required:   true
    ]
    
    def hot = [
        name:       "hot",
        type:       "enum",
        title:		"Mode?",
        required:   false,
        metadata:   [values:["auto", "heat", "cool", "off"]]
    ]
    
    def SetHeatingHigh = [
        name:       "SetHeatingHigh",
        type:       "number",
        title:		"Heating Temperature (degrees)",
        required:   false
    ]
    
     def SetCoolingHigh = [
        name:       "SetCoolingHigh",
        type:       "number",
        title:		"Cooling Temperature (degrees)",
        required:   true
    ] 
  
    def pageName = "Ambiance"
    
    def pageProperties = [
        name:       "TemperatureSettings",
        title:      "",
        //nextPage:   "ThermostatandDoors"
    ]
    
    return dynamicPage(pageProperties) {
        section("Select the main thermostat") {
			input thermostat
		}
        section("Use remote sensors to adjust the thermostat (by default the app is using the internal sensor of the thermostat)") {
			input "remoteSensors", "bool", title: "Enable remote sensor(s)", required: false, defaultValue: false, submitOnChange: true
			if (remoteSensors) {
            	input sensor 
            	paragraph "If multiple sensors are selected, the average temperature is automatically calculated"	
			}
        }
		section("When the temperature falls below this temperature (Low Temperature)..."){
			input setLow
		}
        section("Adjust the thermostat to the following settings:"){
			input cold
            input SetHeatingLow
            input SetCoolingLow
		}
        section("When the temperature raises above this temperature (High Temperature)..."){
			input setHigh
		}
        section("Adjust the thermostat to the following settings:"){
			input hot
            input SetCoolingHigh
            input SetHeatingHigh
		}
        section("When temperature is neutral (between above Low and High Temperatures..."){
			input "neutral", "bool", title: "Turn off the thermostat", required: false, defaultValue: false
		}
    }  
}

// Page - Disabled Mode
def ThermostatandDoors() {
  
    def doors = [
        name:       "doors",
        type:       "capability.contactSensor",
        title:      "Which Sensor(s)?",
        multiple:	true,
        required:   false
    ]
    
    def turnOffDelay = [
        name:       "turnOffDelay",
        type:       "decimal",
        title:		"Number of minutes",
        required:	false
    ]
    
    def resetOff = [
        name:       "resetOff",
        type:       "bool",
        title:		"Reset Thermostat Settings when all Sensor(s) are closed",
        required:	false,
        defaultValue: false
    ]
       
    def pageName = "Thermostat and Doors"
    
    def pageProperties = [
        name:       "ThermostatandDoors",
        title:      "",
        //nextPage:   "ThermostatAway"
    ]

    return dynamicPage(pageProperties) {
        section("When one or more contact sensors open, then turn off the thermostat") {
			input doors
		}
		section("Wait this long before turning the thermostat off (defaults to 1 minute)") {
			input turnOffDelay
            input resetOff 
		}
    }
    
}

// Page - Thermostat Away
def ThermostatAway() {

    def modes2 = [
        name:		"modes2", 
        type:		"mode", 
        title: 		"Which Location Mode(s)?", 
        multiple: 	true, 
        required: 	false
    ]
    
    def away = [
        name:       "away",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]], 
        required: 	false
    ]

    def setAwayLow = [
        name:       "setAwayLow",
        type:       "decimal",
        title:      "Low temp?",
        required:   false
    ]
    
    def AwayCold = [
        name:       "AwayCold",
        type:       "enum",
        title:		"Mode?",
        metadata:   [values:["auto", "heat", "cool", "off"]],
        required: 	false,
    ]
    
    def setAwayHigh = [
        name:       "setAwayHigh",
        type:       "decimal",
        title:      "High temp?",
		required:   false
    ]
    
    def AwayHot = [
        name:       "AwayHot",
        type:       "enum",
        title:		"Mode?",
        required: 	false,
metadata:   [values:["auto", "heat", "cool", "off"]]
    ]
    
    def SetHeatingAway = [
        name:       "SetHeatingAway",
        type:       "number",
        title:		"Heating Temperature (degrees)",
        required: 	false
    ]   
    
    def SetCoolingAway = [
        name:       "SetCoolingAway",
        type:       "number",
        title:		"Cooling Temperature (degrees)",
        required: 	false
    ]     
    
    def fanAway = [
        name:       "fanAway",
        type:       "enum",
        title:		"Fan Mode?",
        metadata:   [values:["fanAuto", "fanOn", "fanCirculate"]],
        required: 	false
    ]

    def pageName = "Thermostat Away"
    
    def pageProperties = [
        name:       "ThermostatAway",
        title:      "",
        //nextPage:   "Settings"
    ]

    return dynamicPage(pageProperties) {
		
        section("When the Location Mode changes to 'Away'") {
   			input modes2
        }
           
        section("Adjust the thermostat to the following settings:") {
    		input away
            input fanAway                        
            input SetHeatingAway
			input SetCoolingAway
  		}	
        section("If the temperature falls below this temperature while away..."){
			input setAwayLow
		}
        
        section("Automatically adjust the thermostat to the following operating mode..."){
			input AwayCold
		}
        
        section("If the temperature raises above this temperature while away..."){
			input setAwayHigh
    	}
        section("Automatically adjust the thermostat to the following operating mode..."){
			input AwayHot
    	} 
	 }
}

// Show "Setup" page
def Settings() {
 
    def days = [
        name:       "days",
        type:       "enum",
        title:      "Only on certain days of the week",
        multiple:   true,
        required:   false,
        options: 	["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
    ]
    
    def modes = [
        name:		"modes", 
        type:		"mode", 
        title: 		"Only when mode is", 
        multiple: 	true, 
        required: 	false
    ]
    
    def pageName = ""
    
    def pageProperties = [
        name:       "Settings",
        title:      "",
        //nextPage:   "pageSetup"
    ]

    return dynamicPage(pageProperties) {    
        
        section("Notifications") {
            input("recipients", "contact", title: "Send notifications to", multiple: true, required: false) {
            paragraph 	"You may enter multiple phone numbers separated by semicolon."+
           				"E.G. 8045551122;8046663344"
            input "sms", "phone", title: "To this phone", multiple: false, required: false
            input "push", "bool", title: "Send Push Notification (optional)", required: false, defaultValue: false
            }
 }       
        section(title: "Restrictions", hideable: true) {
			href "timeIntervalInput", title: "Only during a certain time", description: getTimeLabel(starting, ending), state: greyedOutTime(starting, ending), refreshAfterSelection:true
			input days
			input modes
        }
		section(title: "Debug") {     
        	input "debug", "bool", title: "Enable debug messages in IDE for troubleshooting purposes", required: false, defaultValue: false, refreshAfterSelection:true
        	input "info", "bool", title: "Enable info messages in IDE to display actions in Live Logging", required: false, defaultValue: false, refreshAfterSelection:true
        }    
    }
    
}

def installed(){
	if (debug) log.debug "Installed called with $settings"
	init()
}

def updated(){
		if (debug) log.debug "Updated called with $settings"
	unsubscribe()
	init()
}

def init(){
	state.lastStatus = null
    runIn(60, "temperatureHandler")
    	if (debug) log.debug "Temperature will be evaluated in one minute"
     	if(sensor) {
        	subscribe(sensor, "temperature", temperatureHandler)
        }
        else {
        	subscribe(thermostat, "temperature", temperatureHandler)
    	}
    if(modes2){
    	subscribe(location, modeAwayChange)
        	if(sensor) {
            	subscribe(sensor, "temperature", modeAwayTempHandler)
            }
            else {
        		subscribe(thermostat, "temperature", modeAwayTempHandler)
           	}
    
    }
    if(doors){
            subscribe(doors, "contact.open", temperatureHandler)
            subscribe(doors, "contact.closed", doorCheck)
            state.disabledTemp = null
    		state.disabledMode = null
    		state.disableHSP = null 
    		state.disableCSP = null
	}
}

def temperatureHandler(evt) {
    
    def currentTemp
    if(modeOk && daysOk && timeOk && modeNotAwayOk)  {
    		
            if(sensor){
            	//def sensors = sensor.size()
            	//def tempAVG = sensor ? getAverage(sensor, "temperature") : "undefined device"
            	//currentTemp = tempAVG
            	currentTemp = sensor.latestValue("temperature")
		 if (debug) log.debug "Data check (avg temp: ${currentTemp}, num of sensors:${sensors}, app status: ${lastStatus})"
            }
            else {
            	currentTemp = thermostat.latestValue("temperature")
                 if (debug) log.debug "Thermostat data (curr temp: ${currentTemp},status: ${lastStatus}"
            }        
            if(setLow > setHigh){
                def temp = setLow
                	setLow = setHigh
                	setHigh = temp
                if(info) log.info "Detected ${setLow} >  ${setHigh}. Auto-adjusting setting to  ${temp}"
            }         
            if (doorsOk) {
           		def currentMode = thermostat.latestValue("thermostatMode")
                def currentHSP = thermostat.latestValue("heatingSetpoint") 
                def currentCSP = thermostat.latestValue("coolingSetpoint") 	
                	if (debug) log.debug "App data (curr temp: ${currentTemp},curr mode: ${currentMode}, currentHSP: ${currentHSP},"+
                    						" currentCSP: ${currentCSP}, last status: ${lastStatus}"
                
                if (currentTemp < setLow) {
                    if (state.lastStatus == "one" || state.lastStatus == "two" || state.lastStatus == "three" || state.lastStatus == null){
                        state.lastStatus = "one" 
                         if (currentMode == "cool" || currentMode == "off") {
                     		def msg = "Adjusting ${thermostat} operating mode and setpoints because temperature is below ${setLow}"
                           	if (cold) thermostat?."${cold}"()
                           	thermostat?.setHeatingSetpoint(SetHeatingLow)
                           	if (SetCoolingLow) thermostat?.setCoolingSetpoint(SetCoolingLow)
                           	thermostat?.poll()
                           	sendMessage(msg)
                         		if (info) log.info msg
                        }
                     	else if  (currentHSP < SetHeatingLow) {
                            def msg = "Adjusting ${thermostat} setpoints because temperature is below ${setLow}"
                     		thermostat?.setHeatingSetpoint(SetHeatingLow)
                     		if (SetCoolingLow) thermostat?.setCoolingSetpoint(SetCoolingLow)
                            thermostat?.poll()
                            sendMessage(msg)
                        		if (info) log.info msg
                        }
                    }
                }                                     
                if (currentTemp > setHigh) {
                    if (state.lastStatus == "one" || state.lastStatus == "two" || state.lastStatus == "three" || state.lastStatus == null){
                        state.lastStatus = "two"
						if (currentMode == "heat" || currentMode == "off") {
                            def msg = "Adjusting ${thermostat} operating mode and setpoints because temperature is above ${setHigh}"
                        	if (hot) thermostat?."${hot}"()
                        	if (SetHeatingHigh) thermostat?.setHeatingSetpoint(SetHeatingHigh)
                        	thermostat?.setCoolingSetpoint(SetCoolingHigh)
                        	thermostat?.poll()
                        	sendMessage(msg)
                            	if (info) log.info msg
                        }
                        else if (currentCSP > SetCoolingHigh) {
                            def msg = "Adjusting ${thermostat} setpoints because temperature is above ${setHigh}"
                    		thermostat?.setCoolingSetpoint(SetCoolingHigh)
                     		if (SetHeatingHigh) thermostat?.setHeatingSetpoint(SetHeatingHigh)
                            thermostat?.poll()
                            sendMessage(msg)   
                            	if (info) log.info msg
                       	}
                   }     
                }    
                if (currentTemp > setLow && currentTemp < setHigh) {
                        if (neutral == true) {
                            if (debug) log.debug "Neutral is ${neutral}, current temp is: ${currentTemp}"
                            if (state.lastStatus == "two" || state.lastStatus == "one" || state.lastStatus == null){
                                def msg = "Adjusting ${thermostat} mode to off because temperature is neutral"
                                    thermostat?.off()
                                    thermostat?.poll()
									sendMessage(msg)
                                    state.lastStatus = "three"
                                        if (info) log.info msg
                                        if (debug) log.debug "Data check neutral(neutral is:${neutral}, currTemp: ${currentTemp}, setLow: ${setLow}, setHigh: ${setHigh})"
                            }
                       }
                						if (info) log.info "Temperature is neutral not taking action because neutral mode is: ${neutral}"      
                }
            }
            else{
                def delay = (turnOffDelay != null && turnOffDelay != "") ? turnOffDelay * 60 : 60
               		if(info) log.info ("Detected open doors.  Checking door states again in ${delay} seconds")
                runIn(delay, "doorCheck")
            }
	}
    	if (debug) log.debug "Temperature handler called: modeOk = $modeOk, daysOk = $daysOk, timeOk = $timeOk, modeNotAwayOk = $modeNotAwayOk "
}

def modeAwayChange(evt){ 
	if(modeOk && daysOk && timeOk){
    	if (modes2){
            if(modes2.contains(location.mode)){
                    state.lastStatus = "away"
                    if (away) thermostat."${away}"()             
                    if(SetHeatingAway) thermostat.setHeatingSetpoint(SetHeatingAway)
                    if(SetCoolingAway) thermostat.setCoolingSetpoint(SetCoolingAway)
                    if(fanAway) thermostat.setThermostatFanMode(fanAway)
                    def msg = "Adjusting ${thermostat} mode and setpoints because Location Mode is set to Away"   
                    sendMessage(msg) 
                    if(info) log.info "Running AwayChange because mode is now ${away} and last staus is ${lastStatus}"
            }
            else  {
        			state.lastStatus = null
                    temperatureHandler()
                    if(info) log.info "Running Temperature Handler because Home Mode is no longer in away, and the last staus is ${lastStatus}"
			}
     	}
					if(info) log.info ("Detected temperature change while away but all settings are ok, not taking any actions.")
    }
}

def modeAwayTempHandler(evt) {
	def tempAVGaway = sensor ? getAverage(sensor, "temperature") : "undefined device"
	def currentAwayTemp = thermostat.latestValue("temperature")
	
    	if(info) log.info "Away: your average room temperature is:  ${tempAVGaway}, current temp is  ${currentAwayTemp}"
		if (sensor) currentAwayTemp = tempAVGaway		
        if(lastStatus == "away"){
        	if(modes2.contains(location.mode)){
           		if (currentAwayTemp < setAwayLow) {
					if(Awaycold) thermostat?."${Awaycold}"()
                    thermostat?.poll()
                    def msg = "I changed your ${thermostat} mode to ${Awaycold} because temperature is below ${setAwayLow}"
                    sendMessage(msg)
                    	if (info) log.info msg
  				}
				if (currentAwayTemp > setHigh) {
					if(Awayhot) thermostat?."${Awayhot}"()
                    thermostat?.poll()
					def msg = "I changed your ${thermostat} mode to ${Awayhot} because temperature is above ${setAwayHigh}"
                    sendMessage(msg)
                    	if (info) log.info msg
  				}
             }
			else {
        			state.lastStatus = null
            		temperatureHandler()
            		if(info) log.info "Temp changed while staus is ${lastStatus} but the Location Mode is no longer in away. Resetting lastStatus"
        	}
	}
}

def doorCheck(evt){	
        state.disabledTemp = sensor.latestValue("temperature")
       	state.disabledMode = thermostat.latestValue("thermostatMode")
       	state.disableHSP = thermostat.latestValue("heatingSetpoint") 
        state.disableCSP = thermostat.latestValue("coolingSetpoint") 
		if (debug) log.debug "Disable settings: ${state.disabledMode} mode, ${state.disableHSP} HSP, ${state.disableCSP} CSP"
    if (!doorsOk){
		if(info) log.info ("doors still open turning off ${thermostat}")
		def msg = "I changed your ${thermostat} mode to off because some doors are open"
        if (state.lastStatus != "off"){
        	thermostat?.off()
			sendMessage(msg)
            	if (info) log.info msg
		}
		state.lastStatus = "off"
        		if (info) log.info "Changing status to off"
	}
	else {
    	if (state.lastStatus == "off"){
			state.lastStatus = null
		    if (resetOff){
               if(debug) log.debug "Contact sensor(s) are now closed restoring ${thermostat} with settings: ${state.disabledMode} mode"+
               ", ${state.disableHSP} HSP, ${state.disableCSP} CSP"
                thermostat."${state.disabledMode}"()             
                thermostat.setHeatingSetpoint(state.disableHSP)
                thermostat.setCoolingSetpoint(state.disableCSP) 		    
	    	}
        }
        temperatureHandler()
        	if(debug) "Calling Temperature Handler"
	}
}

private getAverage(device,type){
	def total = 0
		if(debug) log.debug "calculating average temperature"  
    device.each {total += it.latestValue(type)}
    return Math.round(total/device.size())
}

private void sendText(number, message) {
    if (sms) {
        def phones = sms.split("\\;")
        for (phone in phones) {
            sendSms(phone, message)
            
        }
    }
}

private void sendMessage(message) {
    if(info) log.info "sending notification:  ${message}"
    if (recipients) { 
        sendNotificationToContacts(message, recipients)
    if(debug) log.debug "sending notification:  ${recipients}"    
    }
    if (push) {
        sendPush message
            if(info) log.info "sending push notification"
    } else {
            sendNotificationEvent(message)
             if(info) log.info "sending notification"
    }
    if (sms) {
        sendText(sms, message)
        if(debug) "Calling process to send text"
    }
}
            

private getAllOk() {
	modeOk && daysOk && timeOk && doorsOk && modeNotAwayOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
		if(debug) log.debug "modeOk = $result"
	result
}

private getModeNotAwayOk() {
	def result = !modes2 || !modes2.contains(location.mode)
		if(debug) log.debug "modeNotAwayOk = $result"
	result
}

private getDoorsOk() {
	def result = !doors || !doors.latestValue("contact").contains("open")
		if(debug) log.debug "doorsOk = $result"
	result
}


private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	if(debug) log.debug "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
    
    else if (starting){
    	result = currTime >= start
    }
    else if (ending){
    	result = currTime <= stop
    }
    
		if(debug) log.debug "timeOk = $result"
	result
}

def getTimeLabel(starting, ending){

	def timeLabel = "Tap to set"
	
    if(starting && ending){
    	timeLabel = "Between" + " " + hhmm(starting) + " "  + "and" + " " +  hhmm(ending)
    }
    else if (starting) {
		timeLabel = "Start at" + " " + hhmm(starting)
    }
    else if(ending){
    timeLabel = "End at" + hhmm(ending)
    }
	timeLabel
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}
def greyedOut(){
	def result = ""
    if (sensor) {
    	result = "complete"	
    }
    result
}

def greyedOutTherm(){
	def result = ""
    if (thermostat) {
    	result = "complete"	
    }
    result
}


def greyedOutTherm2(){
	def result = ""
    if (modes2) {
    	result = "complete"	
    }
    result
}

def greyedOutSettings(){
	def result = ""
    if (starting || ending || days || modes || push) {
    	result = "complete"	
    }
    result
}

def greyedOutTime(starting, ending){
	def result = ""
    if (starting || ending) {
    	result = "complete"	
    }
    result
}

private anyoneIsHome() {
  def result = false

  if(people.findAll { it?.currentPresence == "present" }) {
    result = true
  }

	 if(debug) log.debug("anyoneIsHome: ${result}")

  return result
}




page(name: "timeIntervalInput", title: "Only during a certain time", refreshAfterSelection:true) {
		section {
			input "starting", "time", title: "Starting (both are required)", required: false 
			input "ending", "time", title: "Ending (both are required)", required: false 
		}
        }