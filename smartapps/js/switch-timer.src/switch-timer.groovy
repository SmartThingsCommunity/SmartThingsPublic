/**
 *  Switch Timer
 *
 *  Copyright 2016 Joe Saporito
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
    name: "Switch Timer",
    namespace: "js",
    author: "Joe Saporito",
    description: "Turns on switches that are not already on for a determined amount of time after an event. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	
    page(name: "settings")
    page(name: "certainTime")
    page(name: "renameLabel")
}

def settings() {
	dynamicPage(name: "settings", title: "Turn switches off after some minutes, unless already on", uninstall: true, install: true) {
		section("Choose one or more, when..."){
            input "people", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
            input "doors", "capability.contactSensor", title: "Doors", required: false, multiple: true
            input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
        }
        section("Turn on switches"){
            input "switches", "capability.switch", multiple: true, required: true
        }
        section("Turn off after") {
            input "waitTime", "decimal", title: "Minutes", required: true
        }
        section(title: "Additional Options") {
            def timeLabel = timeIntervalLabel()
            href "certainTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
            def appLabel = getDefaultLabel()
            href "renameLabel", title: "Rename '" + appLabel + "'", description: ""
            
        }	
   	}
}

def certainTime() {
	dynamicPage(name:"certainTime",title: "Only during a certain time", uninstall: false) {
		section() {
			input "startEnum", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if(startEnum in [null, "A specific time"]) input "startTime", "time", title: "Start time", required: false
			else {
				if(startEnum == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(startEnum == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
		
		section() {
			input "endEnum", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if(endEnum in [null, "A specific time"]) input "endTime", "time", title: "End time", required: false
			else {
				if(endEnum == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if(endEnum == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
	}
}

def renameLabel() {
	dynamicPage(name:"renameLabel",title: "Rename", uninstall: false) {
		section() {
			input "appLabelText", "text", title: "Rename to", defaultValue: (appLabelText ? appLabelText : getDefaultLabel()), required: false
			
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
    updateLabel()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
    updateLabel()
}

def initialize() {
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(doors, "contact.open", activateHandler)
	subscribe(people, "presence.present", activateHandler)  
    subscribe(motion, "motion.active", activateHandler)
}

// TODO: implement event handlers
def activateHandler(evt) {
	if (checkTime()) {
    	log.trace("$evt.name triggered")
    	turnOn()
    }
}

def turnOn() {
    if(state.offSwitches) {
    	runIn(waitTime * 60, turnOff)
    	return
    }
    def offSwitches = getOffSwitches()
    for (s in offSwitches) {
    	s.on()
    }
    if (offSwitches.size() > 0) {
        state.offSwitches = offSwitches.displayName
        runIn(waitTime * 60, turnOff)
    }
}

def turnOff() {
	if (state.offSwitches) {
    	def offSwitches = switches.findAll { 
        	it.displayName in state.offSwitches
        }
    	for (s in offSwitches) {
        	s.off()
   		}
    	state.offSwitches = null
    }
}

def updateLabel() {
	if (appLabelText) {
    	if (appLabelText != app.label) {
    		log.trace("Renamed to $appLabelText")
        	app.updateLabel(appLabelText)
        }
    }
    else {
    	def label = getDefaultLabel()
        log.trace("Renamed to $label")
        	app.updateLabel(label)
        
    }
}

private getDefaultLabel() {
	def label = "Switch Timer"
    
    if (switches) {
    	label = "Timer for " + switches[0].displayName
    }
    
    return label
}

private getOffSwitches() {
	def offSwitches = switches.findAll {
    	it.currentSwitch == "off" ||
        it.currentSwitch == null
    }
    return offSwitches
}
private checkTime() {
	def result = true
    
    if ((startTime && endTime) ||
    	(startTime && endEnum in ["Sunrise", "Sunset"]) ||
        (startEnum in ["Sunrise", "Sunset"] && endTime) ||
        (startEnum in ["Sunrise", "Sunset"] && endEnum in ["Sunrise", "Sunset"])) {
        	def currentTime = now()
            def start = null
            def stop = null
            def sun = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
            
            if (startEnum == "Sunrise")
            	start = sun.sunrise.time
            else if (startEnum == "Sunset")
            	start = sun.sunset.time
            else if (startTime)
            	start = timeToday(startTime,location.timeZone).time
            
            sun = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
            if (endEnum == "Sunrise")
            	stop = sun.sunrise.time
            else if (endEnum == "Sunset")
            	stop = sun.sunset.time
            else if (endTime)
            	stop = timeToday(endTime,location.timeZone).time
             
            result = start < stop ? currentTime >= start && currentTime <= stop : currentTime <= stop || currentTime >= start
    }
    
    return result
}

private hideOptionsSection() {
	(startTime || endTime || startEnum || endEnum) ? false : true
}

private offset(value) {
	def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel() {
	def result = ""
	if (startEnum == "Sunrise" && endEnum == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to " + "Sunrise" + offset(endSunriseOffset)
	else if (startEnum == "Sunrise" && endEnum == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to " + "Sunset" + offset(endSunsetOffset)
	else if (startEnum == "Sunset" && endEnum == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to " + "Sunrise" + offset(endSunriseOffset)
	else if (startEnum == "Sunset" && endEnum == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to " + "Sunset" + offset(endSunsetOffset)
	else if (startEnum == "Sunrise" && endTime) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(endTime, "h:mm a z")
	else if (startEnum == "Sunset" && endTime) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(endTime, "h:mm a z")
	else if (startTime && endEnum == "Sunrise") result = hhmm(startTime) + " to " + "Sunrise" + offset(endSunriseOffset)
	else if (startTime && endEnum == "Sunset") result = hhmm(startTime) + " to " + "Sunset" + offset(endSunsetOffset)
	else if (startTime && endTime) result = hhmm(startTime) + " to " + hhmm(endTime, "h:mm a z")
}