/**
 *  EkonIAirConIR
 *
 *  Copyright 2019 Adi Damty
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

import groovy.transform.Field

@Field final String AIRCONET_USERNAME = 'moshemiz@gmail.com'
@Field final String AIRCONET_PASSWORD = '04095375412'
@Field final String BASE_URL = 'https://www.airconet.xyz/' 
@Field final String IMAGES_SOURCE = 'http://80.179.8.181/smartthings-plugin/'

@Field final Integer  MIN_TEMP = 16
@Field final Integer  MAX_TEMP = 32

@Field final Integer KON_VALUE_MODE_COOL = 17
@Field final Integer EKON_VALUE_MODE_AUTO = 51
@Field final Integer EKON_VALUE_MODE_DRY = 85
@Field final Integer EKON_VALUE_MODE_HEAT = 34
@Field final Integer EKON_VALUE_MODE_FAN = 68
@Field List SUPPORTED_MODES = [KON_VALUE_MODE_COOL, EKON_VALUE_MODE_AUTO, EKON_VALUE_MODE_DRY, EKON_VALUE_MODE_HEAT, EKON_VALUE_MODE_FAN]
@Field List SUPPORTED_MODES_NAMES = ['cool', 'auto', 'dry', 'heat', 'fan']


@Field final Integer EKON_VALUE_FAN_AUTO = 0
@Field final Integer EKON_VALUE_FAN_LOW = 1
@Field final Integer EKON_VALUE_FAN_MEDIUM = 2
@Field final Integer EKON_VALUE_FAN_HIGH = 3
@Field final Integer EKON_VALUE_FAN_MAX = EKON_VALUE_FAN_HIGH
@Field final Integer EKON_VALUE_FAN_MIN = EKON_VALUE_FAN_AUTO

@Field final Integer EKON_VALUE_ON = 85
@Field final Integer EKON_VALUE_OFF = -86

@Field final Integer  DEFAULT_HEATING_SETPOINT = 28
@Field final Integer  DEFAULT_COOLING_SETPOINT = 16
@Field final Integer  DEFAULT_THERMOSTAT_SETPOINT = DEFAULT_HEATING_SETPOINT

metadata {
	definition (name: "EkonIAirConIR", namespace: "AdiDamty", author: "Adi Damty", cstHandler: true) {
		capability "Switch"        
        capability "Thermostat"        
		
        // capability "Switch Level" - Clashing with Thermostat, for now we will change temperature throw Thermostat capbility : Alexa set Air-Conditioner to 25 celsius
        
        // proposed capability doesn't support by SmartThings yet, for now we will use capability "Thermostat" instead
        // https://docs.smartthings.com/en/latest/capabilities-reference.html#id92
        //capability "Air Conditioner Mode" 
                                                            		        
        command "refresh"
        command "changeFan"        
        command "setPosition"
        command "setModeCool"
        command "setModeHeat"
        command "setModeDry"
        command "setModeAuto"
        command "setModeFan"        
        command "setFanAuto"
        command "setFanLow"
        command "setFanMedium"
        command "setFanHigh"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2) {
        multiAttributeTile(name:"windowShade", type: "generic"){
            tileAttribute ("device.onoff", key: "PRIMARY_CONTROL") {
                attributeState "unknown", label: 'Unknown', action: "refresh", backgroundColor: "#ffffff"
                attributeState "${EKON_VALUE_OFF}", label: 'Off', action: "on", backgroundColor: "#ffffff", nextState:"${EKON_VALUE_ON}", icon: "${IMAGES_SOURCE}/temp_power_off_bg.png"
                attributeState "${EKON_VALUE_ON}", label: 'On', action: "off", backgroundColor: "#00a0dc", nextState:"${EKON_VALUE_OFF}", icon: "${IMAGES_SOURCE}/temp_16_21.png"               
            }           
                                    
            tileAttribute ("device.tgtTemp", key: "SLIDER_CONTROL") {
                attributeState "tgtTemp", action:"setPosition"
            }   
        }   
        controlTile("tgtTemp", "device.tgtTemp", "slider", range:"(${MIN_TEMP}..${MAX_TEMP})", height: 2, width: 2) {
            state "tgtTemp", label:'dest\ntgtTemp', action:"setPosition"
        }
        valueTile("refresh", "device.refresh", decoration: "flat", width: 2, height: 2) {
            state "refresh", label:'Refresh', action:"refresh"
        }                                            
        valueTile("envTemp", "device.envTemp", width: 2, height: 2) {
			state("envTemp", label:'${currentValue}°', unit:"Degrees",
				backgroundColors:[
					[value: 16, color: "#66ccff"],
					[value: 17, color: "#66ccff"],
					[value: 18, color: "#66ccff"],
					[value: 19, color: "#66ccff"],
					[value: 20, color: "#66ccff"],
					[value: 21, color: "#66ccff"],
					[value: 22, color: "#ffcc66"],
                    [value: 23, color: "#ffcc66"],
                    [value: 24, color: "#ffcc66"],
                    [value: 25, color: "#ffcc66"],
                    [value: 26, color: "#ffcc66"],
                    [value: 27, color: "#ffcc66"],
                    [value: 28, color: "#ff0000"],
                    [value: 29, color: "#ff0000"],
                    [value: 30, color: "#ff0000"],
                    [value: 31, color: "#ff0000"],
                    [value: 32, color: "#ff0000"]                    
				]
			)
		}
        //standardTile("fan", "device.fan", decoration: "flat", width: 2, height: 2) {
			//state "${EKON_VALUE_FAN_AUTO}", label:'Auto Fan', action:"changeFan", icon: "${IMAGES_SOURCE}/fan_auto.png"
			//state "${EKON_VALUE_FAN_LOW}", label:'Low Fan', action:"changeFan", icon: "${IMAGES_SOURCE}/fan1.png"
			//state "${EKON_VALUE_FAN_MEDIUM}", label:'Medium Fan', action:"changeFan", icon: "${IMAGES_SOURCE}/fan2.png"
			//state "${EKON_VALUE_FAN_HIGH}", label:'High Fan', action:"changeFan", icon: "${IMAGES_SOURCE}/fan3.png"
		//}  
                        
         standardTile("modeHeat", "device.mode", decoration: "flat", width: 2, height: 2) {
			state "${EKON_VALUE_MODE_HEAT}", label:'Heat', backgroundColor: "Red", icon: "${IMAGES_SOURCE}/heating_active.png"
		    state "default", label:'Heat', action:"setModeHeat", backgroundColor: "#ffffff", icon: "${IMAGES_SOURCE}/heating.png"
		 }  
         
         standardTile("modeCool", "device.mode", decoration: "flat", width: 2, height: 2) {
			state "${KON_VALUE_MODE_COOL}", label:'Cool', backgroundColor: "Red", icon: "${IMAGES_SOURCE}/cooling_active.png"
		    state "default", label:'Cool', action:"setModeCool", backgroundColor: "#ffffff", icon: "${IMAGES_SOURCE}/cooling.png"
		 }
         
         standardTile("modeAuto", "device.mode", decoration: "flat", width: 2, height: 2) {
			state "${EKON_VALUE_MODE_AUTO}", label:'Auto', backgroundColor: "Red", icon: "${IMAGES_SOURCE}/auto_active.png"
		    state "default", label:'Auto', action:"setModeAuto", backgroundColor: "#ffffff", icon: "${IMAGES_SOURCE}/auto.png"
		 }
         
         standardTile("modeDry", "device.mode", decoration: "flat", width: 2, height: 2) {
			state "${EKON_VALUE_MODE_DRY}", label:'Dry', backgroundColor: "Red", icon: "${IMAGES_SOURCE}/dry_active.png"
		    state "default", label:'Dry', action:"setModeDry", backgroundColor: "#ffffff", icon: "${IMAGES_SOURCE}/dry.png"
		 }
         
         standardTile("modeFan", "device.mode", decoration: "flat", width: 2, height: 2) {
			state "${EKON_VALUE_MODE_FAN}", label:'Fan', backgroundColor: "Red", icon: "${IMAGES_SOURCE}/fan_active.png"
		    state "default", label:'Fan', action:"setModeFan", backgroundColor: "#ffffff", icon: "${IMAGES_SOURCE}/fan.png"
		 } 
         
          standardTile("fanAuto", "device.fan", decoration: "flat", width: 1, height: 1) {
			state "${EKON_VALUE_FAN_AUTO}", label:'Auto Fan', icon: "${IMAGES_SOURCE}/fan_auto_active.png"
			state "default", label:'Auto Fan', action:"setFanAuto", icon: "${IMAGES_SOURCE}/fan_auto.png"
		} 
         
          standardTile("fanHigh", "device.fan", decoration: "flat", width: 1, height: 1) {
			state "${EKON_VALUE_FAN_HIGH}", label:'High Fan', icon: "${IMAGES_SOURCE}/fan3_active.png"
			state "default", label:'High Fan', action:"setFanHigh", icon: "${IMAGES_SOURCE}/fan3.png"
		} 
        
         standardTile("fanMeduim", "device.fan", decoration: "flat", width: 1, height: 1) {
			state "${EKON_VALUE_FAN_MEDIUM}", label:'Medium Fan', icon: "${IMAGES_SOURCE}/fan2_active.png"
			state "default", label:'Medium Fan', action:"setFanMedium", icon: "${IMAGES_SOURCE}/fan2.png"
		} 
        
         standardTile("fanLow", "device.fan", decoration: "flat", width: 1, height: 1) {
			state "${EKON_VALUE_FAN_LOW}", label:'Low Fan', icon: "${IMAGES_SOURCE}/fan1_active.png"
			state "default", label:'Low Fan', action:"setFanLow", icon: "${IMAGES_SOURCE}/fan1.png"
		}                   
    }
}

// Switch capability command
def on() {
	log.debug "Executing 'on'"
	turnOnOrOff(true);        
}

// Switch capability command
def off() {
	log.debug "Executing 'off'"
	turnOnOrOff(false);
}

// Switch Level capability command
//def setLevel(Float value)
//{
//    log.debug "setLevel level ${value}"
	//setPosition(Math.round(value) as Integer)
//}

// Switch Level capability command
//def setLevel(Integer level, Integer rate = null)
//{
//    log.debug "setLevel level ${level} rate ${rate}"
	//setPosition(level)
//}

// Thermostat capability command
def setCoolingSetpoint(Double degreesF) {
	log.debug "In setCoolingSetpoint"  
    setPosition(Math.round(degreesF) as Integer)
}

// Thermostat capability command
def setHeatingSetpoint(Double degreesF) {
	log.debug "In setHeatingSetpoint"  
    setPosition(Math.round(degreesF) as Integer)
}

// Thermostat capability command
def setThermostatMode(String name) {    
    log.debug "In setThermostatMode ${name}"
    
    def value;
    
    SUPPORTED_MODES_NAMES.eachWithIndex { it, i -> // `it` is the current element, while `i` is the index    
        if (name == it)
        {
        	 value = SUPPORTED_MODES[i]
        }
	}
    
    if (value != null) {                	
        sendEvent(name: "thermostatMode", value: name)
        sendEvent(name: "thermostatSetpoint", value: DEFAULT_THERMOSTAT_SETPOINT, unit: "°F")
        sendEvent(name: "heatingSetpoint", value: DEFAULT_HEATING_SETPOINT, unit: "°F")        	
    	sendEvent(name: "coolingSetpoint", value: DEFAULT_COOLING_SETPOINT, unit: "°F")
		setMode(value)
    } else {
        log.warn "'$value' is not a supported mode. Please set one of ${SUPPORTED_MODES_NAMES.join(', ')}"
    }                
}

// Air Conditioner Mode capability command - proposed capability, doesn't support by SmartThings yet
//def setAirConditionerMode(String value){
	//log.debug "In setAirConditionerMode ${value}"
//}


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute
}

def setModeCool() {
	log.debug "In setModeCool"
	setMode(KON_VALUE_MODE_COOL)
}

def setModeHeat() {
	log.debug "In setModeHeat"
	setMode(EKON_VALUE_MODE_HEAT)
}

def setModeAuto() {
	log.debug "In setModeAuto"
	setMode(EKON_VALUE_MODE_AUTO)
}

def setModeDry() {
	log.debug "In setModeDry"
	setMode(EKON_VALUE_MODE_DRY)
}

def setModeFan() {
	log.debug "In setModeFan"
	setMode(EKON_VALUE_MODE_FAN)
}

def setMode(Integer value) {
    log.debug "setMode ${value}"
    if (value in SUPPORTED_MODES)
    {    	        
    	sendEvent(name: "mode", value: value)
    	setHvac();
    }
    else
    {
    	log.Debug "Invalid mode ${value} - do nothing"
    }            
}


def setPosition(Integer value) {
    log.debug "setPosition ${value}"
    if (value > MAX_TEMP) 
    {
    	value = MAX_TEMP
    }
    else if (value < MIN_TEMP)
    {
    	value = MIN_TEMP
    }
    
    sendEvent(name: "tgtTemp", value: value)      
     
    setHvac();
}

def setFanAuto(){
	setFan(EKON_VALUE_FAN_AUTO)
}

def setFanLow(){
	setFan(EKON_VALUE_FAN_LOW)
}

def setFanMedium(){
	setFan(EKON_VALUE_FAN_MEDIUM)
}

def setFanHigh(){
	setFan(EKON_VALUE_FAN_HIGH)
}

def changeFan()
{        
	def fan = device.currentValue("fan") as Integer
    
    if (fan == null)
    {
    	fan = EKON_VALUE_FAN_MIN as Integer
    }
        
    log.debug "In changeFan current value ${fan}"
    
    fan = fan + 1;
    if (fan > EKON_VALUE_FAN_MAX)
    {
    	fan = EKON_VALUE_FAN_MIN
    }
    
    setFan(fan)
}

def setFan(Integer value)
{
    log.debug "setFan was called with value ${value}"
    	    
    if (value < EKON_VALUE_FAN_MIN)
    {
    	value = EKON_VALUE_FAN_MIN
    }
    else if (value > EKON_VALUE_FAN_MAX)
    {
    	value = EKON_VALUE_FAN_MAX
    }
    
    sendEvent(name: "fan", value: value)
    
	setHvac();
}

def getStatusIfNeeded()
{       
	def mac = device.currentValue("mac");
    
    if (mac != null)
    {
    	return
    }
    
    getStatus()
}

def refresh() {
    log.debug "in refresh"
    
    getStatus()    
}

def getStatus()
{            	    
    def url = BASE_URL + "dev/allStatus"
  
    log.debug "Get to ${url}"

    def params = [
        uri: url,
        headers: [
            Cookie: getCookieValue()
        ]
    ]

    httpGet(params) { response ->
        log.debug "Getting response getStatus"
        def content = response.data
        def status = content.attachment[0]                            
        
        sendEvent(name: "mac", value: status.mac)
        sendEvent(name: "onoff", value: status.onoff)
        sendEvent(name: "mode", value: status.mode)
        sendEvent(name: "fan", value: status.fan)
        sendEvent(name: "envTemp", value: status.envTemp)
        sendEvent(name: "tgtTemp", value: status.tgtTemp)                        
        
        log.debug "response: ${content}"        
    } 
}

def turnOnOrOff(boolean on)
{   	
    getStatusIfNeeded()
    def mac = device.currentValue("mac")
	def url = BASE_URL + "dev/switchHvac/${mac}"
    def onoffValue
    
    if (on)
    {
    	url += "?on=True";
        onoffValue = EKON_VALUE_ON
    }
    else
    {
       	url += "?on=False";
        onoffValue = EKON_VALUE_OFF
    }
        
    log.debug "Get to ${url}"
           
    def params = [
        uri: url,
        headers: [
             Cookie: getCookieValue()
        ]
    ]
    
    httpGet(params) { response ->
          log.debug "Getting response turnOnOrOff"
          def content = response.data
		  log.debug "response: ${content}"
		  
          if (response.getStatus() == 200)
          {
          	  sendEvent(name: "onoff", value: onoffValue)
              log.debug "send event onoff with value ${onoffValue}"
          }
	}
    
    log.debug "HttpGet completed"        
}

private def setHvac()
{	    
    def url = BASE_URL + "dev/setHvac"
       
    def params = [
        uri: url,
        headers: [
            Cookie: getCookieValue(),
            "Content-Type": "application/json"
        ],
        body: [
        	mac: device.currentValue("mac"),
            onoff: device.currentValue("onoff"),
            mode: device.currentValue("mode"),
            fan: device.currentValue("fan"),
            envTemp: device.currentValue("envTemp"),
            tgtTemp: device.currentValue("tgtTemp")
        ]        
    ]
    
     log.debug "Post to ${url} with params ${params}"

    httpPostJson(params) { response ->
        log.debug "Getting response setHvac"
        def content = response.data        
        log.debug "response: ${content}"        
    }        
}

def getCookieValue()
{   	    
	def url = BASE_URL + "j_spring_security_check"
    def cookieValue = ''
            
    log.debug "Post to ${url}"
    
    url = url + "?username=${AIRCONET_USERNAME}&password=${AIRCONET_PASSWORD}&remember-me=true&isServer=false"

	def params = [
        uri: url          
    ]
    
    httpPost(params) { response ->
          log.debug "Getting response getCookieValue"
          def content = response.data
		  log.debug "response: ${content}"
          
          def cookie = response.headers["Set-Cookie"] as String;          
          log.debug "response cookie: ${cookie}"          
          
		  def fromIndex = cookie.indexOf(' ');
          def toIndex = cookie.indexOf(';');                    
          
          
          if (fromIndex != -1 && toIndex != -1)
          {
          	  cookieValue = cookie.substring(fromIndex + 1, toIndex)
          }
          
          log.debug "cookie value ${cookieValue}"
          
          sendEvent(name: "cookieValue", value: cookieValue)
	}
    
    log.debug "HttpPost security check completed"
    return cookieValue
}