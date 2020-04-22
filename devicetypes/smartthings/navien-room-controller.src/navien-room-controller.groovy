/**
 *  Navien Thermostat
 *
 *  Author: Navien Within
 *  Date: 2015-11-02
 */
metadata {
	definition (name: "Navien Room Controller", namespace: "smartthings", author: "Navien") {
        capability "Thermostat"
        capability "Polling"
        capability "Refresh"
        
        command "generateEvent"
        command "powerONOFF"
        command "setRoomTemp"
        command "setOndolTemp"
        command "controlMode1"
        command "controlMode2"
        
        attribute "setRoomSlider",  "NUMBER"
        attribute "setOndolSlider", "NUMBER"
	}

  	// simulator metadata
	simulator { }
	
    //tiles(scale: 2) {
    tiles {
    	/*
		multiAttributeTile(name: "temperature", type: "thermostat", canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("temperature", key: "PRIMARY_CONTROL") {
                attributeState "temperature", label: '${currentValue}°', icon:"st.Home.home1", backgroundColor:"#FFA81E"
            }
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
				attributeState "statusText", label: 'statusText'
			}
            tileAttribute("button", key: "VALUE_CONTROL"){
                attributeState "setUp"
                attributeState "setDown"
            }
		}
        */
        
        valueTile("thermostatStatus", "device.thermostatStatus", width: 3, height: 2) {
			state "전원 OFF",       label:'${currentValue}', icon:"st.Navien.bgs_power_off",     backgroundColor:"#BDBDBD"
            state "외출 ON",        label:'${currentValue}', icon:"st.Navien.bgs_out",           backgroundColor:"#FF8C17"
            state "실내난방",       label:'${currentValue}', icon:"st.Navien.bgs_indoor",        backgroundColor:"#FF8C17"
            state "온돌난방",       label:'${currentValue}', icon:"st.Navien.bgs_ondol",         backgroundColor:"#FF8C17"
            state "반복예약난방",   label:'${currentValue}', icon:"st.Navien.bgs_heating_again", backgroundColor:"#FF8C17"
            state "24시간예약난방", label:'${currentValue}', icon:"st.Navien.bgs_24heat",        backgroundColor:"#FF8C17"
            state "간편예약난방",   label:'${currentValue}', icon:"st.Navien.bgs_heat",          backgroundColor:"#FF8C17"
            state "온수전용",       label:'${currentValue}', icon:"st.Navien.bgs_water",         backgroundColor:"#FF8C17"
            state "빠른온수",       label:'${currentValue}', icon:"st.Navien.bgs_water_fast",    backgroundColor:"#FF8C17"
		}
        
        valueTile("temperature", "device.temperature", width: 1, height: 1, inactiveLabel: false) {
            state "OFF",     label:'',                  unit:"C", icon:"st.Navien.bg_recent_off"
            state "default", label:'${currentValue}°', unit:"C", icon:"st.Navien.bg_recent"
		}
        
        valueTile("hotWater", "device.hotWater", width: 1, height: 1) {
        	state "OFF",     label:'',                  unit:"C", icon:"st.Navien.bg_water_off"
			state "default", label:'${currentValue}°', unit:"C", icon:"st.Navien.bg_water"
		}
        
        standardTile("refresh", "device.refresh", , width: 1, height: 1, inactiveLabel: false) {
			state "default", action:"refresh.refresh", icon:"st.Navien.but_refresh"
		}
               
        valueTile("setRoomTemp", "device.setRoomTemp", width: 1, height:1) {
        	state "OFF",     label:'',                  icon:"st.Navien.bg_indoor_off"
			state "default", label:'${currentValue}°', icon:"st.Navien.bg_indoor"
		}
        
        controlTile("setRoomSlider", "device.setRoomSlider", "slider", height: 1, width: 2, inactiveLabel: false, range:"(10..40)") {
			state "default", action:"setRoomTemp", backgroundColor:"#F08C00"
		}
        
        valueTile("setOndolTemp", "device.setOndolTemp", width: 1, height:1) {
        	state "OFF",     label:'',                  icon:"st.Navien.bg_ondol"
			state "default", label:'${currentValue}°', icon:"st.Navien.bg_ondol_off"
		}
        
        controlTile("setOndolSlider", "device.setOndolSlider", "slider", height: 1, width: 2, inactiveLabel: false, range:"(40..83)") {
			state "default", action:"setOndolTemp", backgroundColor:"#F08C00"
		}
        
        standardTile("power", "device.power", width: 1, height: 1, inactiveLabel: false) {
			state "ON",      label:'', action:"powerONOFF", icon:"st.Navien.but_power"
            state "default", label:'', action:"powerONOFF", icon:"st.Navien.but_power_off"
		}
        
        standardTile("controlMode1", "device.controlMode1", width: 1, height: 1, inactiveLabel: false) {
            state "실내난방 ON", label:'', action:"controlMode1", icon:"st.Navien.but_indoor"
            state "온돌난방 ON", label:'', action:"controlMode1", icon:"st.Navien.but_ondol"
            state "default",     label:'',                        icon:"st.Navien.but_indoor_off"
		}
        
        standardTile("controlMode2", "device.controlMode2", width: 1, height: 1, inactiveLabel: false) {
            state "외출해제", label:'', action:"controlMode2", icon:"st.Navien.but_out_off"
            state "외출설정", label:'', action:"controlMode2", icon:"st.Navien.but_out"
            state "default",  label:'',                        icon:"st.Navien.but_out_dis"
		}
        
        valueTile("herotile", "device.herotile", width: 3, height:1) {
			state "default", label:'', icon:"st.Navien.bg_herotile"
		}
        
		main "thermostatStatus"
		details(["thermostatStatus", "temperature", "hotWater", "refresh", "setRoomTemp", "setRoomSlider", "setOndolTemp", "setOndolSlider", "power", "controlMode1", "controlMode2", "herotile"])
	}
}

def powerONOFF()
{
	log.debug "powerONOFF called"
    def powerStatus = device.currentValue("power")
    def results
    if(powerStatus == "ON")
    {
    	results = parent.childRequest(this, "1", "33", "0")
    }
    else if(powerStatus == "전원 OFF")
    {
    	results = parent.childRequest(this, "1", "34", "0")
    }
    generateEvent(results)
    log.debug "powerONOFF ended"
}

def setRoomTemp(degrees)
{
    def degreesInteger = degrees as Integer
	sendEvent("name":"setRoomSlider", "value":degreesInteger)
    
    def status = device.currentValue("thermostatStatus")
    if(status == "실내난방")
    {
    	def results = parent.childRequest(this, "1", "44", degreesInteger*2)
        generateEvent(results)
    }
}

def setOndolTemp(degrees)
{
    def degreesInteger = degrees as Integer
	sendEvent("name":"setOndolSlider", "value":degreesInteger)
    
    def status = device.currentValue("thermostatStatus")
    if(status == "온돌난방")
    {
    	def results = parent.childRequest(this, "1", "36", degreesInteger*2)
        generateEvent(results)
    }
}

def controlMode1()
{
	log.debug "controlMode1"
	def control = device.currentValue("controlMode1")
    controlMode(control)
}

def controlMode2()
{
	log.debug "controlMode2"
    def control = device.currentValue("controlMode2")
    controlMode(control)
}

def controlMode(control)
{    
    if(control == "실내난방 ON")
    {
    	log.debug "실내난방 ON : 제어"
    	def value   = device.currentValue("setRoomSlider")*2
    	def results = parent.childRequest(this, "1", "43", value)
        generateEvent(results)
        
    }
    else if(control == "온돌난방 ON")
    {
    	log.debug "온돌난방 ON : 제어"
		def value   = device.currentValue("setOndolSlider")*2
        def results = parent.childRequest(this, "1", "35", value)
        generateEvent(results)
        
    }
    else if(control == "외출해제")
    {
    	log.debug "외출해제 : 제어"
        def results = parent.childRequest(this, "1", "46", "0")
        generateEvent(results)
        
    }
    else if(control == "외출설정")
    {
    	log.debug "외출설정 : 제어"
        def results = parent.childRequest(this, "1", "46", "1")
        generateEvent(results)
    }
}

def refresh()
{
	log.debug "refresh called"
	poll()
	log.debug "refresh ended"
}

void poll() {
	log.debug "Executing 'poll' using parent SmartApp"
	
	def results = parent.pollChild(this)
	generateEvent(results)
}

void generateEvent(Map results)
{
	log.debug "parsing data $results"
    
	if(results)
	{
		results.each { name, value -> 
 			def linkText    = getLinkText(device)
            def isChange    = true
            def isDisplayed = true
            
            if (name=="temperature" || name=="hotWater") 
            {
				//isChange = isTemperatureStateChange(device, name, value.toString())
                isDisplayed = isChange
                   
				sendEvent(
					name: name,
					value: value,
					unit: "C",
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)                                    									 
            }
            else if (name=="thermostatStatus") 
            {
            	isChange = isStateChange(device, name, value.toString())
                isDisplayed = isChange
                
                sendEvent(
					name: name,
					value: value.toString(),
					linkText: linkText,
					descriptionText: getThermostatDescriptionText(name, value, linkText),
					handlerName: name,
					isStateChange: isChange,
					displayed: isDisplayed)
            }
		}
        generateStatusEvent(results)
	}
}

private getThermostatDescriptionText(name, value, linkText)
{
	if(name == "temperature")
	{
		return "$linkText was $value°C"
	}
	else if(name == "roomTemp")
	{
		return "latest roomTemp setpoint was $value°C"
	}
	else if(name == "ondolTemp")
	{
		return "latest ondolTemp setpoint was $value°C"
	}
    else if (name == "thermostatMode")
    {
        return "thermostat mode is ${value}"
    }
    else
    {
        return "${name} = ${value}"
    }
}

def generateStatusEvent(Map results) {
	log.debug "generateStatusEvent"
    
	def status       = results.thermostatStatus
    def setRoomTemp  = results.roomTemp
    def setOndolTemp = results.ondolTemp
    
    log.debug "status ===> ${status}"
        
    if (status == "전원 OFF") {       
        sendEvent("name":"temperature",    "value":"OFF",     "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"hotWater",       "value":"OFF",     "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"setRoomTemp",    "value":"OFF",     "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"setRoomSlider",  "value":"0",                                    displayed: true, isStateChange: true)
        sendEvent("name":"setOndolTemp",   "value":"OFF",     "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"setOndolSlider", "value":"0",                                    displayed: true, isStateChange: true)
		sendEvent("name":"power",          "value":"ON",      "description":"전원 ON",     displayed: true, isStateChange: true)
        sendEvent("name":"controlMode1",   "value":"OFF",     "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"controlMode2",   "value":"OFF",     "description":"OFF",         displayed: true, isStateChange: true)
        

    }else if(status == "온수전용"){
        sendEvent("name":"setRoomTemp",    "value":"OFF",      "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"setRoomSlider",  "value":"0",                                     displayed: true, isStateChange: true)
        sendEvent("name":"setOndolTemp",   "value":"OFF",      "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"setOndolSlider", "value":"0",                                     displayed: true, isStateChange: true)
		sendEvent("name":"power",          "value":"전원 OFF", "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"controlMode1",   "value":"실내 ON",  "description":"ON",          displayed: true, isStateChange: true)
        sendEvent("name":"controlMode2",   "value":"온돌 ON",  "description":"ON",          displayed: true, isStateChange: true)
        
    }else if(status == "외출 ON"){
    	sendEvent("name":"setRoomTemp",    "value":"OFF",      "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"setRoomSlider",  "value":"0",                                     displayed: true, isStateChange: true)
        sendEvent("name":"setOndolTemp",   "value":"OFF",      "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"setOndolSlider", "value":"0",                                     displayed: true, isStateChange: true)
        sendEvent("name":"power",          "value":"전원 OFF", "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"controlMode1",   "value":"OFF",      "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"controlMode2",   "value":"외출해제", "description":"외출해제",    displayed: true, isStateChange: true)
        
    }else if(status == "실내난방"){
        sendEvent("name":"setRoomTemp",    "value":"${setRoomTemp}",  "description":"설정온도",    displayed: true, isStateChange: true)
        sendEvent("name":"setRoomSlider",  "value":setRoomTemp,                                                  displayed: true, isStateChange: true)
        sendEvent("name":"setOndolTemp",   "value":"${setOndolTemp}", "description":"설정온도",    displayed: true, isStateChange: true)
        sendEvent("name":"setOndolSlider", "value":setOndolTemp,                                                 displayed: true, isStateChange: true)
        sendEvent("name":"power",          "value":"전원 OFF",                      "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"controlMode1",   "value":"온돌난방 ON",                   "description":"온돌난방 ON", displayed: true, isStateChange: true)
        sendEvent("name":"controlMode2",   "value":"외출설정",                      "description":"외출설정",    displayed: true, isStateChange: true)
        
    }
    else if(status == "온돌난방"){      
        sendEvent("name":"setRoomTemp",    "value":"${setRoomTemp}",  "description":"설정온도",    displayed: true, isStateChange: true)
        sendEvent("name":"setRoomSlider",  "value":setRoomTemp,                                                  displayed: true, isStateChange: true)
        sendEvent("name":"setOndolTemp",   "value":"${setOndolTemp}", "description":"설정온도",    displayed: true, isStateChange: true)
        sendEvent("name":"setOndolSlider", "value":setOndolTemp,                                                 displayed: true, isStateChange: true)
        sendEvent("name":"power",          "value":"전원 OFF",                      "description":"OFF",         displayed: true, isStateChange: true)
        sendEvent("name":"controlMode1",   "value":"실내난방 ON",                   "description":"실내난방 ON", displayed: true, isStateChange: true)
        sendEvent("name":"controlMode2",   "value":"외출설정",                      "description":"외출설정",    displayed: true, isStateChange: true)
        
    }

    log.debug "Generate Status Event = ${status}"
}