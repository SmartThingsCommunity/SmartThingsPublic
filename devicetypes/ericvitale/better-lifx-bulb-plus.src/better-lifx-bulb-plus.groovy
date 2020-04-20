/**
 *  Better LIFX Bulb Plus
 *
 *  Copyright 2016 Eric Vitale
 *
 *  Version 1.1.5a - Fixed typo in vid setting. (09/04/2018)
 *  Version 1.1.5 - Updated to support the new SmartThings mobile app. (08/20/2018)
 *  Version 1.1.4 - Updated the "on" indicator color of the DH to the standard ST Blue (#00a0dc). (08/03/2018)
 *  Version 1.1.3 - Cleaned up a bit. (06/30/2017) 
 *  Version 1.1.2 - Added the ability to use separate durations for on/off and setLevel commands. (06/26/2017)
 *  Version 1.1.1 - Added setLevelAndTemperature method to allow webCoRE set both with a single command. (06/25/2017)
 *  Version 1.1.0 - Updated to use the ST Beta Asynchronous API. (06/21/17)
 *  Version 1.0.6 - Added the transitionLevel(), apiFlash(), & runEffect() methods. (06/16/2017)
 *  Version 1.0.5 - Added saturation:0 to setColorTemperature per LIFX's recommendation. (05/22/2017)
 *  Verison 1.0.4 - Fixed an issue with setColor() introduced by an api change. (05/19/2017)
 *  Version 1.0.3 - Updated the scheduling settings (04/18/2017)
 *  Version 1.0.2 - More accuracy for setLevel (12/17/2016)
 *  Version 1.0.1 - Added additonal logging on refresh method (12/17/2016)
 *  Version 1.0.0 - Initial Release (08/08/2016)
 *
 *  This SmartThings device handler can be found @ https://github.com/ericvitale/ST-Better-LIFX-BULB
 *  You can find my other SmartApps or Device Handlers @ https://github.com/ericvitale
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

include 'asynchttp_v1'

import java.text.DecimalFormat;
 
metadata {
	definition (name: "Better LIFX Bulb Plus", namespace: "ericvitale", author: "ericvitale@gmail.com", vid: "generic-rgbw-color-bulb") {
		capability "Actuator"
		capability "Color Control"
		capability "Color Temperature"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
        capability "Infrared Level"
        
        command "transitionLevel"
        command "runEffect"
        command "apiFlash"
        command "apiBreathe"
        command "irOn", ["number"]
        command "irOff"
        command "setLevelAndTemperature"
        
        attribute "lastRefresh", "string"
        attribute "refreshText", "string"
	}
    
    preferences {
    	input "token", "text", title: "API Token", required: true
        input "bulb", "text", title: "Bulb Name", required: true
        input "defaultTransition", "decimal", title: "Level Transition Time (s)", required: true, defaultValue: 0.0
        input "defaultStateTransition", "decimal", title: "On/Off Transition Time (s)", required: true, defaultValue: 0.0
	    input "logging", "enum", title: "Log Level", required: false, defaultValue: "INFO", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
    }

	tiles(scale: 2) {
    	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#00a0dc"//, nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff"//, nextState:"turningOn"
			}
            
            tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}%'
			}
            
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
        		attributeState "default", action:"switch level.setLevel"
            }
        }
        
        multiAttributeTile(name:"switchDetails", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"http://hosted.lifx.co/smartthings/v1/196xOn.png", backgroundColor:"#00a0dc"
				attributeState "off", label:'${name}', action:"switch.on", icon:"http://hosted.lifx.co/smartthings/v1/196xOff.png", backgroundColor:"#ffffff"
			}
            
            tileAttribute ("device.lastActivity", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'Last activity: ${currentValue}', action: "refresh.refresh"
			}
        }
        
        valueTile("Brightness", "device.level", width: 2, height: 1) {
        	state "level", label: 'Brightness ${currentValue}%'
        }
        
        controlTile("levelSliderControl", "device.level", "slider", width: 4, height: 1) {
        	state "level", action:"switch level.setLevel"
        }
        
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", height: 1, width: 2) {
			state "colorTemp", label: '${currentValue}K'
		}
        
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", height: 1, width: 4, inactiveLabel: false, range:"(2500..9000)") {
			state "colorTemp", action:"color temperature.setColorTemperature"
		}
        
        controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
            state "color", action:"setColor"
        }
        
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", height: 3, width: 3) {
			state "default", label:"", action:"refresh.refresh", icon: "st.secondary.refresh"
		}

        main(["switch"])
        details(["switchDetails", "Brightness", "levelSliderControl", "colorTemp", "colorTempSliderControl", "rgbSelector", "refresh"])
    }
}

private determineLogLevel(data) {
    switch (data?.toUpperCase()) {
        case "TRACE":
            return 0
            break
        case "DEBUG":
            return 1
            break
        case "INFO":
            return 2
            break
        case "WARN":
            return 3
            break
        case "ERROR":
        	return 4
            break
        default:
            return 1
    }
}

def log(data, type) {
    data = "LIFX -- ${device.label} -- ${data ?: ''}"
        
    if (determineLogLevel(type) >= determineLogLevel(settings?.logging ?: "INFO")) {
        switch (type?.toUpperCase()) {
            case "TRACE":
                log.trace "${data}"
                break
            case "DEBUG":
                log.debug "${data}"
                break
            case "INFO":
                log.info "${data}"
                break
            case "WARN":
                log.warn "${data}"
                break
            case "ERROR":
                log.error "${data}"
                break
            default:
                log.error "LIFX -- ${device.label} -- Invalid Log Setting of ${type}."
                log.error "Message = ${data}."
        }
    }
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	log("Initializing bulb...", "DEBUG")
    
    setDefaultTransitionDuration(defaultTransition)
    setDefaultStateTransitionDuration(defaultStateTransition)
    setBulbName(bulb)
	setupSchedule()
}

def configure() {
}

def parse(String description) {
}

def setHue(val) {
	log("Setting bulb hue to ${val}.", "DEBUG")
    
    sendLIFXCommand([color: "hue:${val}"])
    
    sendEvent(name: "hue", value: val)
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "level", value: "${state.level}")
}

def setSaturation() {
	log("Setting bulb saturation to ${val}.", "DEBUG")
    
    sendLIFXCommand([color: "saturation:${val}"])
    
    sendEvent(name: "saturation", value: val)
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "level", value: "${state.level}")
}

def setColor(value) {
	log("Setting bulb color to ${value}.", "DEBUG")
    
    def data = [:]
    data.hue = value.hue
    data.saturation = value.saturation
    data.level = device.currentValue("level")
    
    sendLIFXCommand([color: "saturation:${data.saturation / 100} hue:${data.hue * 3.6}"])
    
    sendEvent(name: "hue", value: value.hue)
    sendEvent(name: "saturation", value: value.saturation)
    sendEvent(name: "color", value: value.hex)
    sendEvent(name: "switch", value: "on")
    sendEvent(name: "level", value: "${state.level}")
}

def setColorTemperature(value) {
	log("Setting bulb color temperature to ${value}.", "DEBUG")
    
    if(value < 2500) {
    	value = 2500
    } else if(value > 9000) {
    	value = 9000
    }
    
    sendLIFXCommand([color: "kelvin:${value} saturation:0"])
	sendEvent(name: "colorTemperature", value: value)
	sendEvent(name: "color", value: "#ffffff")
	sendEvent(name: "saturation", value: 0)
    sendEvent(name: "level", value: "${state.level}")
}

def setInfraredLevel(level) {
	log("Begin setting bulbs infrared level to ${level}.", "DEBUG")
    
    if (level > 100) {
		level = 100
	} else if (level <= 0 || level == null) {
		sendEvent(name: "level", value: 0)
		return off()
	}
    
    state.infraredLevel = level
	sendEvent(name: "infraredLevel", value: level)
    
    def infrared = level / 100
   
    sendLIFXCommand(["infrared": infrared])
}

def poll() {
	log("Poll().", "DEBUG")
    refresh()
}

def refresh() {
    log("Beginning device update...", "INFO")
	sendLIFXInquiry()
}

def irOn(val) {
	setInfraredLevel(val)
}

def irOff() {
	setInfraredLevel(0)
}

def on(duration=getDefaultStateTransitionDuration()) {
    log("Turning bulb on.", "INFO")
    sendLIFXCommand(["power" : "on", "duration" : duration])
    sendEvent(name: "switch", value: "on")
}

def off(duration=getDefaultStateTransitionDuration()) {
    log("Turning bulb off.", "INFO")
    sendLIFXCommand(["power" : "off", "duration" : duration])
    sendEvent(name: "switch", value: "off")
}

def transitionLevel(value, duration=getDefaultTransitionDuration()) {
	log("transitionLevel(${value}, ${duration})", "DEBUG")
	setLevel(value, duration)
}

def setLevel(level, duration=getDefaultTransitionDuration()) {
	log("Begin setting bulb level to ${level} over ${duration} seconds.", "DEBUG")
    
    if (level > 100) {
		level = 100
	} else if (level <= 0 || level == null) {
		sendEvent(name: "level", value: 0)
		return off()
	}
    
    state.level = level
	sendEvent(name: "level", value: level)
    sendEvent(name: "switch", value: "on")
    
    def brightness = level / 100
   
    sendLIFXCommand(["brightness": brightness, "power": "on", "duration" : duration])
}

def setLevelAndTemperature(level, temperature, duration=getDefaultTransitionDuration()) {
	log("Setting bulb level to ${level} and color temperature to ${temperature} over ${duration} seconds.", "INFO")
    
    if (level > 100) {
		level = 100
	} else if (level <= 0 || level == null) {
		sendEvent(name: "level", value: 0)
		return off()
	}
    
    if(temperature < 2500) {
    	temperature = 2500
    } else if(temperature > 9000) {
    	temperature = 9000
    }
    
    state.level = level
	sendEvent(name: "level", value: level)
    sendEvent(name: "switch", value: "on")
	sendEvent(name: "colorTemperature", value: temperature)
	sendEvent(name: "color", value: "#ffffff")
	sendEvent(name: "saturation", value: 0)
    
    def brightness = level / 100
    
    sendLIFXCommand([color : "kelvin:${temperature} saturation:0 brightness:${brightness}", "power" : "on", "duration" : duration])
}

def runEffect(effect="pulse", color="blue", from_color="red", cycles=5, period=0.5, brightness=0.5) {
	log("runEffect(effect=${effect}, color=${color}: 1.0, from_color=${from_color}, cycles=${cycles}, period=${period}, brightness=${brightness}.", "INFO")

	if(effect != "pulse" && effect != "breathe") {
    	log("${effect} is not a value effect, defaulting to pulse.", "ERROR")
        effect = "pulse"
    }
	
    runLIFXEffect(["color" : "${color.toLowerCase()} brightness:${brightness}".trim(), "from_color" : "${from_color.toLowerCase()} brightness:${brightness}".trim(), "cycles" : "${cycles}" ,"period" : "${period}"], effect)
}

def apiFlash(cycles=5, period=0.5, brightness1=1.0, brightness2=0.0) {
    if(brightness1 < 0.0) {
    	brightness1 = 0.0
    } else if(brightness1 > 1.0) {
    	brightness1 = 1.0
    }
    
    if(brightness2 < 0.0) {
    	brightness2 = 0.0
    } else if(brightness2 > 1.0) {
    	brightness2 = 1.0
    }
    
    runLIFXEffect(["color" : "brightness:${brightness1}", "from_color" : "brightness:${brightness2}", "cycles" : "${cycles}" ,"period" : "${period}"], "pulse")
}

def apiBreathe(cycles=3, period=2.0, brightness1=1.0, brightness2=0.0) {
    
    if(brightness1 < 0.0) {
    	brightness1 = 0.0
    } else if(brightness1 > 1.0) {
    	brightness1 = 1.0
    }
    
    if(brightness2 < 0.0) {
    	brightness2 = 0.0
    } else if(brightness2 > 1.0) {
    	brightness2 = 1.0
    }

	runLIFXEffect(["color" : "brightness:${brightness1}", "from_color" : "brightness:${brightness2}", "cycles" : "${cycles}" ,"period" : "${period}"], "breathe")
}

def setBulbName(value) {
	state.bulbName = value
}

def getBulbName() {
	if(state.bulbName == null || state.bulbName == "") {
    	setBulbName(bulb)
    }
	return state.bulbName
}

def setDefaultTransitionDuration(value) {
	state.transitionDuration = value
}

def getDefaultTransitionDuration() {
	return state.transitionDuration
}

def setDefaultStateTransitionDuration(value) {
	state.onOffTransitionDuration = value
}

def getDefaultStateTransitionDuration() {
	if(state.onOffTransitionDuration == null) {
    	state.onOffTransitionDuration = 0.0
    }
	return state.onOffTransitionDuration
}

def getLastCommand() {
	return state.lastCommand
}

def setLastCommand(command) {
	state.lastCommand = command
}

def incRetryCount() {
	state.retryCount = state.retryCount + 1
}

def resetRetryCount() {
	state.retryCount = 0
}

def getRetryCount() {
	return state.retryCount
}

def getMaxRetry() {
	return 3
}

def getRetryWait(base, count) {
	
    if(count == 0) {
    	return base
    } else {
    	return base * (6 * count)
    }
}

def retry() {
	if(getRetryCount() < getMaxRetry()) {
    	log("Retrying command...", "INFO")
		runIn( getRetryWait(5, getRetryCount() ), sendLastCommand )
    } else {
    	log("Too many retries...", "WARN")
        resetRetryCount()
    }
}

def sendLastCommand() {
	sendLIFXCommand(getLastCommand())
}

def sendLIFXCommand(commands) {

	setLastCommand(commands)
    
    def params = [
        uri: "https://api.lifx.com",
		path: "/v1/lights/label:" + getBulbName() + "/state",
        headers: ["Content-Type": "application/json", "Accept": "application/json", "Authorization": "Bearer ${token}"],
        body: commands
    ]
    
    asynchttp_v1.put('putResponseHandler', params)
}

def runLIFXEffect(commands, effect) {

	def params = [
        uri: "https://api.lifx.com",
		path: "/v1/lights/label:" + getBulbName() + "/effects/" + effect,
        headers: ["Content-Type": "application/json", "Accept": "application/json", "Authorization": "Bearer ${token}"],
        body: commands
    ]
    
    asynchttp_v1.post('postResponseHandler', params)
}

def sendLIFXInquiry() {

	def params = [
        uri: "https://api.lifx.com",
		path: "/v1/lights/label:" + getBulbName(),
        headers: ["Content-Type": "application/x-www-form-urlencoded", "Authorization": "Bearer ${token}"]
    ]
    
    asynchttp_v1.get('getResponseHandler', params)
}

def postResponseHandler(response, data) {

    if(response.getStatus() == 200 || response.getStatus() == 207) {
		log("Response received from LFIX in the postReponseHandler.", "DEBUG")
        updateDeviceLastActivity(new Date())
    } else {
    	log("LIFX failed to adjust group. LIFX returned ${response.getStatus()}.", "ERROR")
        log("Error = ${response.getErrorData()}", "ERROR")
    }
}

def putResponseHandler(response, data) {

    if(response.getStatus() == 200 || response.getStatus() == 207) {
		log("Response received from LFIX in the putReponseHandler.", "DEBUG")
        
        log("Response = ${response.getJson()}", "DEBUG")
        
        def totalBulbs = response.getJson().results.length()
        def results = response.getJson().results
        def bulbsOk = 0
        
        for(int i=0;i<totalBulbs;i++) {
        	if(results[i].status != "ok") {
        		log("${results[i].label} is ${results[i].status}.", "WARN")
            } else {
            	bulbsOk++
            	log("${results[i].label} is ${results[i].status}.", "TRACE")
            }
        }
        
        if(bulbsOk == totalBulbs) { 
            log("${bulbsOk} of ${totalBulbs} bulbs returned ok.", "INFO")
            updateDeviceLastActivity(new Date())
            resetRetryCount()
        } else {
        	log("${bulbsOk} of ${totalBulbs} bulbs returned ok.", "WARN")
            retry()
        }

        updateDeviceLastActivity(new Date())
        
    } else {
    	log("LIFX failed to adjust group. LIFX returned ${response.getStatus()}.", "ERROR")
        log("Error = ${response.getErrorData()}", "ERROR")
    }
}

def getResponseHandler(response, data) {

    if(response.getStatus() == 200 || response.getStatus() == 207) {
		log("Response received from LFIX in the GetReponseHandler.", "DEBUG")
        
        log("Response ${response.getJson()}", "DEBUG")
        
       	response.getJson().each {
        	updateDeviceLastActivity(new Date())
        	log("${it.label} is ${it.power}.", "TRACE")
        	log("Bulb Type: ${it.product.name}.", "TRACE")
        	log("Has variable color temperature = ${it.product.capabilities.has_variable_color_temp}.", "TRACE")
            log("Has color = ${it.product.capabilities.has_color}.", "TRACE")
            log("Has ir = ${it.product.capabilities.has_ir}.", "TRACE")
            log("Has Multizone = ${it.product.capabilities.has_multizone}.", "TRACE")
        	log("Brightness = ${it.brightness}.", "TRACE")
        	log("Color = [saturation:${it.color.saturation}], kelvin:${it.color.kelvin}, hue:${it.color.hue}.", "TRACE")
        
        	DecimalFormat df = new DecimalFormat("###,##0.0#")
        	DecimalFormat dfl = new DecimalFormat("###,##0.000")
        	DecimalFormat df0 = new DecimalFormat("###,##0")

            if(it.power == "on") {
                sendEvent(name: "switch", value: "on")
                if(it.color.saturation == 0.0) {
                    log("Saturation is 0.0, setting color temperature.", "TRACE")

                    def b = df0.format(it.brightness * 100)

                    sendEvent(name: "colorTemperature", value: it.color.kelvin)
                    sendEvent(name: "color", value: "#ffffff")
                    sendEvent(name: "level", value: b)
                    sendEvent(name: "switch", value: "on")
                } else {
                    log("Saturation is > 0.0, setting color.", "TRACE")
                    def h = df.format(it.color.hue)
                    def s = df.format(it.color.saturation)
                    def b = df0.format(it.brightness * 100)

                    log("h = ${h}, s = ${s}, b = ${b}.", "TRACE")

                    sendEvent(name: "hue", value: h, displayed: true)
                    sendEvent(name: "saturation", value: s, displayed: true)
                    sendEvent(name: "kelvin", value: it.color.kelvin, displayed: true)
                    sendEvent(name: "level", value: b)
                    sendEvent(name: "switch", value: "on")
                }
            } else if(it.power == "off") {
                sendEvent(name: "switch", value: "off")
            }
        }
    } else {
    	log("LIFX failed to update the group. LIFX returned ${response.getStatus()}.", "ERROR")
        log("Error = ${response.getErrorData()}", "ERROR")
    }
}

def setupSchedule() {
    try {
	    unschedule(refresh)
    } catch(e) {
    	log("Failed to unschedule!", "ERROR")
        log("Exception ${e}", "ERROR")
        return
    }
    
    runEvery1Minute(refresh)
    
    log("End setupSchedule().", "DEBUG")
}

def updateDeviceLastActivity(lastActivity) {
	def finalString = lastActivity?.format('MM/d/yyyy hh:mm a',location.timeZone)    
	sendEvent(name: "lastActivity", value: finalString, display: false , displayed: false)
}