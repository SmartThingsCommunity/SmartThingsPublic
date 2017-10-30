/**
 *  Toggle Me
 *  Version 1.0.0 - 07/11/16
 *
 *  1.0.1 - 07/20/16
 *   -- Bug Fix: Resolved issued with sunset / sunrise
 *  1.0.0 - 07/11/16
 *   -- Initial Release
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
 *
 *  You can find this smart app @ https://github.com/ericvitale/ST-Toggle-Me
 *  You can find my other device handlers & SmartApps @ https://github.com/ericvitale
 *
 */
 
definition(
	name: "Toggle Me",
	namespace: "ericvitale",
	author: "ericvitale@gmail.com",
	description: "Set on/off, level, color, and color temperature of a set of lights based on motion, acceleration, and a contact sensor.",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page name: "mainPage"
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
    
    	section("Switches") {
			input "switches", "capability.switch", title: "Switches", multiple: true, required: false
    	}
        
        section("Dimmers") {
        	input "dimmers", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
            input "selectedDimmersLevel", "number", title: "Dimmer Level", description: "Set your dimmers to...", required: false, defaultValue: 100
        }
        
        /*section("Color Lights") {
        	input "colorLights", "capability.colorControl", title: "Color Lights", multiple: true, required: false
            input "selectedColorLightsColor", "enum", title: "Select Color", required: false, options: ["Blue", "Green", "Red", "Yello", "Orange", "Pink", "Purple", "Random"]
            input "selectedColorLightsLevel", "number", title: "Level", required: false, defaultValue: 100
        }*/
        
        section("Color Temperature Lights") {
        	input "colorTemperatureLights", "capability.colorTemperature", title: "Color Temperature Lights", multiple: true, required: false
            input "selectedColorTemperatureLightsTemperature", "number", title: "Color Temperature", description: "2700 - 9000", range: "2700..9000", required: false
            input "selectedColorTemperatureLightsLevel", "number", title: "Level", defaultValue: 100, required: false
        }
       
        section("Schedule") {
            input "onLenght", "number", title: "Turn on for N seconds?", required: true, defaultValue: 60
            input "offLenght", "number", title: "Turn off for N seconds?", required: true, defaultValue: 60
        }
        
        section("Follow the Sun") {
        	//input "modes", "mode", title: "Only in Modes", multiple: true, required: false
            input "useTheSun", "bool", title: "Follow sunset / sunrise?", required: true, defaultValue: false
            input "sunriseOffset", "number", title: "Sunrise Offset", range: "-720..720", required: false, defaultValue: 0
           	input "sunsetOffset", "number", title: "Sunset Offset", range: "-720..720", required: false, defaultValue: 0            
        }
        
        section("Time Range") {
            input "useTimeRange", "bool", title: "Use Custom Time Range?", required: true, defaultValue: false
            input "startTime", "time", title: "Start Time", required: false
            input "endTime", "time", title: "End Time", required: false
        }

	    section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false)
            input "active", "bool", title: "Rules Active?", required: true, defaultValue: true
            input "logging", "enum", title: "Log Level", required: true, defaultValue: "INFO", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
    	}
	}
}

def determineLogLevel(data) {
	if(data.toUpperCase() == "TRACE") {
    	return 0
    } else if(data.toUpperCase() == "DEBUG") {
    	return 1
    } else if(data.toUpperCase() == "INFO") {
    	return 2
    } else if(data.toUpperCase() == "WARN") {
    	return 3
    } else {
    	return 4
    }
}

def log(data, type) {
    
    data = "Toggle Me -- " + data
    
    try {
        if(determineLogLevel(type) >= determineLogLevel(logging)) {
            if(type.toUpperCase() == "TRACE") {
                log.trace "${data}"
            } else if(type.toUpperCase() == "DEBUG") {
                log.debug "${data}"
            } else if(type.toUpperCase() == "INFO") {
                log.info "${data}"
            } else if(type.toUpperCase() == "WARN") {
                log.warn "${data}"
            } else if(type.toUpperCase() == "ERROR") {
                log.error "${data}"
            } else {
                log.error "Toggle Me -- Invalid Log Setting"
            }
        }
    } catch(e) {
    	log.error ${e}
    }
}

def installed() {   
	log("Begin installed.", "DEBUG")
	initalization() 
    log("End installed.", "DEBUG")
}

def updated() {
	log("Begin updated().", "DEBUG")
	unsubscribe()
    unschedule()
	initalization()
    setAllLights("off")
    log("End updated().", "DEBUG")
}

def initalization() {
	log("Begin intialization().", "DEBUG")
    log("active = ${active}.", "INFO")
    log("useTheSun = ${useTheSun}.", "INFO")
    log("onLength = ${onLenght}.", "INFO")
    log("offLength = ${offLenght}.", "INFO")
    
    if(useTheSun == true) {
    	if(sunriseOffset == null) { sunriseOffset = 0 }
        if(sunsetOffset == null) { sunsetOffset = 0 }
   	}
    
    log("sunsetOffset = ${sunsetOffset}.", "INFO")
    log("sunriseOffset = ${sunriseOffset}.", "INFO")
    log("Sunrise with Offset of ${sunriseOffset} = ${getSunrise(getOffsetString(sunriseOffset))}.", "INFO")
    log("Sunset with Offset of ${sunsetOffset} = ${getSunset(getOffsetString(sunsetOffset))}.", "INFO")
    
    log("Use Time Range = ${useTimeRange}.", "DEBUG")
    
    if(useTimeRange == true) {
	    if(startTime == null || endTime == null) {
    		useTimeRange = false
            log("Invalid start/end time, turning time range control off.", "ERROR")
    	} else {
        	log("Start Time = ${startTime}.", "DEBUG")
            log("End Time = ${endTime}.", "DEBUG")
        }
    }
    
    if(useTheSun == true && useTimeRange == true) {
    	log("Both 'Use the Sun' & 'Use Time Range' enabled, defaulting to 'Use the Sun', check your settings!", "ERROR")
        useTimeRange = false
    }
    
    if(active) {
        triggerLights()
    } else {
    	log("App is set to inactive in settings.", "INFO")
    }

    log("End initialization().", "DEBUG")
}

def triggerLights() {
	log("Begin triggerLights().", "DEBUG")
    
    def currentDate = new Date()
    
    if(useTheSun) {
        if(isAfter(currentDate, getSunset(getOffsetString(sunsetOffset))) || isBefore(currentDate, getSunrise(getOffsetString(sunriseOffset)))) {
        	log("The sun is down! OK!", "DEBUG")
        } else {
        	log("Does not meet useTheSun criteria!", "DEBUG")
            return
        }
    }
    
    if(useTimeRange) {
    	if(isBefore(currentDate, inputDateToDate(startTime)) && isAfter(currentDate, inputDateToDate(endTime))) {
        	log("Time is outside of time range, ignoring triggers.", "DEBUG")
            return
        } else {
        	log("Time is within time range!", "DEBUG")
        }
    }

    setSwitches()
    setDimmers(selectedDimmersLevel)
    //setColorLights(selectedColorLightsLevel, selectedColorLightsColor)
    setColorTemperatureLights(selectedColorTemperatureLightsLevel, selectedColorTemperatureLightsTemperature)
    log("Switches / Lights triggered.", "INFO")
	
    setOffSchedule()
    
	log("End triggerLights().", "DEBUG")
}

def setSwitches() {
	log("Begin setSwitches().", "DEBUG")
    
    switches.each { it->
    	it.on()
    }
    
    log("End setSwitches().", "DEBUG")
}

def setDimmers(valueLevel) {
    
    dimmers.each { it->
   		it.setLevel(valueLevel)
        it.on()
    }
    
    log("End setDimmers(onOff, value).", "DEBUG")
}

def setColorLights(valueLevel, valueColor) {
	log("Begin setColorLights(onOff, valueLevel, valueColor).", "DEBUG")
    def colorMap = getColorMap(valueColor)
    
	log("Color = ${valueColor}.", "DEBUG")
    log("Hue = ${colorMap['hue']}.", "DEBUG")
    log("Saturation = ${colorMap['saturation']}.", "DEBUG")
    
    colorLights.each { it->
        it.on()
        //it.setColor(colorMap)
    	it.setHue(colorMap['hue'])
        it.setSaturation(colorMap['saturation'])
        it.setLevel(valueLevel)
    }
    
    
    log("End setColorLights(onOff, valueLevel, valueColor).", "DEBUG")
}

def setColorTemperatureLights(valueLevel, valueColorTemperature) {
	log("Begin setColorTemperatureLights(, valueLevel, valueColorTemperature).", "DEBUG")
    
    colorTemperatureLights.each { it->
    	it.setLevel(valueLevel)
        it.setColorTemperature(valueColorTemperature)
        it.on()
    }
    
    log("End setColorTemperatureLights(onOff, valueLevel, valueColorTemperature).", "DEBUG")
}

def setAllLightsOff() {
	log("Begin setAllLightsOff().", "DEBUG")
    	setAllLights("off")
        log("Turned lights off per the schedule.", "INFO")
        setOnSchedule()
    log("End setAllLightsOff().", "DEBUG")
}

def setAllLightsOn() {
	log("Begin setAllLightsOn().", "DEBUG")
    	setAllLights("on")
        log("Turned lights on per the schedule.", "INFO")
        setOffSchedule()
    log("End setAllLightsOn().", "DEBUG")
}

def setAllLights(onOff) {
	log("Begin setAllLights(onOff)", "DEBUG")
    
    if(onOff.toLowerCase() == "off") {
    	switches?.off()
        dimmers?.off()
        colorTemperatureLights?.off()
        colorLights?.off()
    } else {
    	switches?.on()
        dimmers?.on()
        colorTemperatureLights?.on()
        colorLights?.on()
    }

    log("End setAllLights(onOff)", "DEBUG")
}

def setOffSchedule() {
	log("Begin setOffSchedule().", "DEBUG")
    
    if(active) {
    	runIn(onLenght, setAllLightsOff)
        log("Setting timer to turn off lights in ${onLenght} minutes.", "INFO")
    }
    
    log("End setOffSchedule().", "DEBUG")
}

def setOnSchedule() {
	log("Begin setOnSchedule().", "DEBUG")
    
    if(active) {
    	runIn(offLenght, triggerLights)
        log("Setting timer to turn on lights in ${offLenght} minutes.", "INFO")
    }
    
    log("End setOnSchedule().", "DEBUG")
}

def getColorMap(val) {
	
    def colorMap = [:]
    
	switch(val.toLowerCase()) {
    	case "blue":
        	colorMap['hue'] = "240"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"
            break
        case "red":
        	colorMap['hue'] = "0"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"
            break
        case "yellow":
            colorMap['hue'] = "60"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"
        default:
            colorMap['hue'] = "60"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"	
    }
    
	return colorMap
}

/////// Begin Time / Date Methods ///////////////////////////////////////////////////////////

def minutesBetween(time1, time2) {
	return (time1.getTime() - time2.getTime())/1000/60
}

def isBefore(time1, time2) {
	if(minutesBetween(time1, time2) <= 0) {
    	return true
    } else {
    	return false
    }
}

def isAfter(time1, time2) {
	if(minutesBetween(time1, time2) > 0) {
    	return true
    } else {
    	return false
    }
}

def getSunset() {
	return getSunset("00:00")
}

def getSunrise() {
	return getSunrise("00:00")
}

def getSunset(offset) {
	return getSunriseAndSunset(sunsetOffset: offset).sunset
}

def getSunrise(offset) {
	return getSunriseAndSunset(sunriseOffset: offset).sunrise
}

def getOffsetString(offsetMinutes) {
	int hours = Math.abs(offsetMinutes) / 60; //since both are ints, you get an int
	int minutes = Math.abs(offsetMinutes) % 60;
    def sign = (offsetMinutes >= 0) ? "" : "-"
	def offsetString = "${sign}${hours.toString().padLeft(2, "0")}:${minutes.toString().padLeft(2, "0")}"
	return offsetString
}

def inputDateToDate(val) {
	return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", val)
}

/////// End Time / Date Methods ///////////////////////////////////////////////////////////