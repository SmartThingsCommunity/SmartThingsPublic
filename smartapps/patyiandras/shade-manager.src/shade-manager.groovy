/**
 *  Managing Shades
 *
 *  Copyright 2018 Patyi Andr&aacute;s
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
    name: "Shade manager",
    namespace: "patyiandras",
    author: "Patyi Andr&aacute;s",
    description: "Close or open the shades at certain situations",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Shades") {
        input "shades", "capability.switchLevel", title: "Shade(s)", multiple: true, required: true
        input "orientation", "enum", title: "Open/Close", options: [ "Open":"Open", "Close":"Close" ], required: true, defaultValue: "Close"
        input "level", "number", title: "Level"
        input "type", "enum", title: "Type", options: [ "At time":"At time", "At sunrise":"At sunrise", "At sunset":"At sunset", "Light level":"Light level", "At sunrise not earlier than time":"At sunrise not earlier than time", "At sunset not later than time":"At sunset not later than time" ], required: false, defaultValue: "At sunset"
        input "time", "time", title: "Time", required: false
        input "fromTime", "time", title: "From time", required: false
        input "toTime", "time", title: "To time", required: false
        input "offset", "number", title: "Time offset", required: false, defaultValue: 0
        input "light", "capability.Illuminance Measurement", title: "Light sensor", required: false
        input "lightlevel", "number", title: "Light level", required: false
        input "thesedays", "enum", title: "On these days", options: [ "Mon":"Mon", "Tue":"Tue", "Wed":"Wed", "Thu":"Thu", "Fri":"Fri", "Sat":"Sat", "Sun":"Sun" ], multiple: true, required: false
        input "mainswitch", "capability.switch", title: "Main switch", required: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    unschedule(moveDown)
    unschedule(timeHandler)
    unsubscribe()
    initialize()
}

def initialize() {
state.lastRun="2019-06-20T06:03:24+0000"
	if (type=="At sunset" || type=="At sunset not later than time") {
    	subscribe(location, "sunsetTime", suntimeHandler)
     	log.debug "Subscribed on sunset time"        
        //schedule it to run today too
        scheduleMove(location.currentValue("sunsetTime"))
    }
    else if (type=="At sunrise" || type=="At sunrise not earlier than time") {
    	subscribe(location, "sunriseTime", suntimeHandler)
    	log.debug "Subscribed on sunrise time"        
        //schedule it to run today too
        scheduleMove(location.currentValue("sunriseTime"))
    }
    else if (type=="At time" && time!=null) {
    	def timeinput=toDateTime(time, null)
    	def offsetedTime = new Date(timeinput.time + (offset * 60 * 1000))
        schedule(offsetedTime, timeHandler)
    	log.debug "Scheduled for {$offsetedTime}"        
    }
	if (type=="Light level" && light!=null) { 
    	subscribe(light, "illuminance", lightHandler) 
    	log.debug "Subscribed on light sensor"        
    }
}


def timeHandler(evt) {
 	log.debug "It's time to move"        
    moveToLevel()
}

def suntimeHandler(evt) {
 	scheduleMove(evt.value)
}

def lightHandler(evt) {
 	log.debug "Light level changed to $evt.value"
    def lightvalue = evt.value.toInteger()
    def between = fromTime==null || toTime==null || timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
    if (!between) {
    	log.debug "Current time isn't between $fromTime and $toTime"
    }
    if (between && ((orientation=="Close" && lightvalue<=lightlevel)
    	|| (orientation=="Open" && lightvalue>=lightlevel))) {
    	moveToLevel()
    }
}

def scheduleMove(timeString) {
    //get the Date value for the string
    def timeinput = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timeString)

    //calculate the offset
    def offsetedTime = new Date(timeinput.time + (offset * 60 * 1000))
    log.debug "Time: $time, offsetedTime: $offsetedTime"
    if (time!=null)
    {
        def timeparam = toDateTime(offsetedTime.format("yyyy-MM-dd")+"T"+timeToday(time, location.timeZone).format("HH:mm:ss.SSSZ"), location.timeZone)
        if (type=="At sunset not later than time" && offsetedTime>timeparam) {
            log.debug "$offsetedTime is later than $timeparam"
            offsetedTime=timeparam
        }
        else if (type=="At sunrise not earlier than time" && offsetedTime<timeparam) {
            log.debug "$offsetedTime is earlier than $timeparam"
            offsetedTime=timeparam
        }
    }
    log.debug "Scheduling for: $offsetedTime"

    //schedule this to run one time
    runOnce(offsetedTime, moveToLevel, [overwrite: false])
}

def moveToLevel() {
	if (mainswitch!=null && mainswitch.currentSwitch=="off") 
    { 
 		log.debug "Main switch is off"            
    	return
    }
    log.debug "Checking lastRun: $state.lastRun"
    if (state.lastRun!=null) {
    	def lr=Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", state.lastRun)
    	if(lr.getDate() == new Date().getDate()) {
    		log.debug "Today it just have ran"
        	return
        }
    }

    def df = new java.text.SimpleDateFormat("EEE")
    // Ensure the new date object is set to local time zone
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())	
    def dayCheck = thesedays==null || thesedays.contains(day)
	if (dayCheck)
    {
        log.debug "moving shades to level: $level (day is $day in $thesedays)"
        state.lastRun = new Date()
        def i=0
        shades.each { shade ->
            moveShade(i, level, 0)
            i++
        }
    }
    else
    {
        log.debug "don't need to move shades to level: $level (day is $day not in $thesedays)"
    }
}

def moveShade(shadeindex, level, maxtry=1, trycount=1) {
	def shade=shades[shadeindex]
    def currentLevel = shade.currentValue("level")
    if (currentLevel == null) {
        def switchState = shade.currentValue("switch")
        if (switchState == "on") { currentLevel = 99 }
        if (switchState == "off") { currentLevel = 0 }
    }
    if ((orientation=="Close" && currentLevel > level) || (orientation=="Open" && currentLevel < level)) {
        log.debug "Let move $shade.displayName to level $level from $currentLevel"
        shade.setLevel(level)
        if (maxtry>=trycount) {
            runIn(30, checkAndTryAgainIfNeeded, [data: [shade: shadeindex, level: level, maxtry: maxtry, trycount: trycount]])
        }
    }
    else {
        log.debug "Don't need to move $shade.displayName it's already on level $currentLevel"
    }	
}

def checkAndTryAgainIfNeeded(data) {
	def shade=shades[data.shade]
    log.debug "Checking the last movement request of $shade.displayName (trycount: $data.trycount)"    
    def currentLevel = shade.currentValue("level")
    if (currentLevel == null) {
        def switchState = shade.currentValue("switch")
        if (switchState == "on") { currentLevel = 99 }
        if (switchState == "off") { currentLevel = 0 }
    }
    if ((orientation=="Close" && currentLevel > data.level) || (orientation=="Open" && currentLevel < data.level)) {
    	log.debug "Try again..."
    	moveShade(data.shade, data.level, data.maxtry, data.trycount+1)
	}
    else {
    	log.debug "Done!"
    }
}