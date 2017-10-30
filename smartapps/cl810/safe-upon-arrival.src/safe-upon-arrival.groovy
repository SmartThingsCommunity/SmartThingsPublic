/**
 *  Safe upon Arrival  - 2014.12.14
 *
 *   Written by Alexander Claytonand based on code by Aaron Herzon, Patrick Stuart, and SmartThings 
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
    name: "Safe upon Arrival",
    namespace: "cl810",
    author: "Alexander Clayton",
    description: "Turn lights on at dusk at a dimmed level, brighten when presence is sensed temporarily. Turn off at dawn.  Adjustable dim level, bright level, bright time, down offest, dusk offset. Dusk and dawn times based on location (either zip code or hub location)",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",  
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
    //todo:  replace icon with something appropriate.  Maybe a lightbulb with the moon and stars


preferences {

section("When one of these people arrive at home") {
	input "people", "capability.presenceSensor", multiple: true
}
section("Lights") {
	input "switches", "capability.switch", multiple: true, title: "Lights?"
}
section("False alarm threshold (defaults to 10 min)") {
	input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
}
    section ("Set Bright and Dim Levels and Bright Time") {
		input "DimLevelStr", "enum", title: "Dimmed Level %", required: true, 
        	options: ["10","15","20","30","50","75"], defaultValue: "20"

        input "BrightLevelStr", "enum", title: "Bright Level %", required: true, 
        	options: ["100","75","50"], defaultValue: "100"

        input "DelayMinStr", "enum", title: "Bright time, minutes", required: true, 
        	options: ["1","3","5","10","15","30","60"], defaultValue: "5"
        	}
    section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipCode", "text", title: "Enter 5-digit ZIP code", required: false
	}
    section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "Offset amount in the format HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "Offset amount in the format HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
}
def installed() {
	log.debug "Installed with settings: ${settings} DelayMin: $DelayMin"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}  DelayMin: $DelayMin"

	unsubscribe()
	//unschedule handled in astroCheck method
	initialize()
}

def initialize() {
	state.DimLevel = DimLevelStr as Integer
    if (state.DimLevel == 100) {
    	state.DimLevel = 99
    }
    state.BrightLevel = BrightLevelStr as Integer
   	if (state.BrightLevel == 100) {
    	state.BrightLevel = 99
    }
    state.DelayMin = DelayMinStr as Integer
    
    subscribe(location, "position", locationPositionChange)
	subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
	subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
	subscribe(people, "presence", presenceHandler)
	
    initialSunPosition()  
	
    astroCheck()
}

def initialSunPosition() {  
	//Determine if sun is down at time of initializtion and run sunsetHandler() if so
    //Light meter is not evaluated initially, light level first evaluated at first luminance event
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	def now = new Date()
    def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
    log.debug "setTime: $setTime"
    log.debug "Now: $now"
    state.sunPosition = "up" //initialize to "up"
	
    	if(setTime.before(now)) {   //before midnight, after sunset
	        sunsetHandler()
            log.info "Sun is already down, run sunsetHandler"
	    }
	    else 
        {	if (riseTime.after(now)) {  //after midnight, before sunset
        	sunsetHandler()
            log.info "Sun is already down, run sunsetHandler"
        	}
      	} 
}

def locationPositionChange(evt) {
	log.trace "locationChange()"
	astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
	log.trace "sunriseSunsetTimeHandler()"
	astroCheck()
}

def astroCheck() {
	//query sunset and sunrise times with offsets applied, schedule handlers for sun events
    //this method lifted from Sunrise/Sunset with some mods and error corrections
    
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"
    log.debug "Now: $now"
	
	if (state.riseTime != riseTime.time) {
		state.riseTime = riseTime.time
		
		unschedule("sunriseHandler")
		if(riseTime.before(now)) {
			state.riseTime = riseTime.next()
		}
		
        log.info "scheduling sunrise handler for $state.riseTime"
        //todo: resolve issue with date formated as Epoch sometimes in log
		schedule(state.riseTime, sunriseHandler)
	}
   
	if (state.setTime != setTime.time) {
		state.setTime = setTime.time
		unschedule("sunsetHandler")

	    if(setTime.before(now)) {
	        state.setTime = setTime.next()
	    }
	
        log.info "scheduling sunset handler for $state.setTime"
        //todo: resolve issue with date formated as Epoch sometimes in log
	    schedule(state.setTime, sunsetHandler)
	}
}

def sunriseHandler() {
	log.info "Executing sunrise handler"
    state.sunPosition = "up"  
}

def sunsetHandler() {
	//Light meter not evaluated here.  Sunset has priority over light measurement in determining state.ambient
	log.info "Executing sunset handler"
    state.sunPosition = "down"    
}

def presenceHandler(evt) {
	log.debug "$evt.name: $evt.value"
	def current = people.currentValue("presence")
	log.debug current
	def presenceValue = presence1.find{it.currentPresence == "present"}
	log.debug "presenceValue = $presenceValue"
	if (evt.value == "present") {
    	switches?.setLevel(state.BrightLevel)
        state.Level = state.BrightLevel
    	log.debug ". . . set the dimmers to level $state.BrightLevel"
    	log.debug (bnumber)
    log.debug state
    runIn((state.DelayMin*60), dimOrOffafterBright)
    }
    else 
    {
    	log.debug ". . . but its light, so do nothing"
        log.debug state
    }
}
def dimOrOffafterBright() {   
	//Handles the case where the sun comes up during bright time

	log.debug "Bright delay is complete, decide to turn off or dim based on sun position and offsets"

    if (state.sunPosition == "down") {
    	switches?.setLevel(state.DimLevel)
        state.Level = state.DimLevel
    	log.debug "Return to dimmed states since sun is down"
        log.debug state
    }
    else 
    {
    	switches?.setLevel(0)
        state.Level = 0
        log.debug "Turned off lights since sun came up during bright time"
        log.debug state
    }
}
def modeDim() {   
	//Sets light to dim stat
    
	log.debug "Set lights to dimmed state" 
    
    switches?.setLevel(state.DimLevel)
    state.Level = state.DimLevel
    log.debug state
}

def modeBright() {   
	//Sets light to bright stat
    
	log.debug "Set lights to bright state" 
    
    switches?.setLevel(state.BrightLevel)
    state.Level = state.BrightLevel
    log.debug state
}
private getLabel() {
	app.label ?: "SmartThings"
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}