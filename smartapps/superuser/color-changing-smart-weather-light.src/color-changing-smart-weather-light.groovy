/**
 *  Color Changing Smart Weather lamp
 *
 *	
 *	This weather lantern app turns on with motion and turns a Phillips hue (or LifX) lamp different colors based on the weather.  
 *	It uses dark sky's weather API to micro-target weather. 
 *
 *	Weather Lamp Colors
 *
 *	Blinking Red 	Weather Watch, Warning or Advisory for your location
 *	Purple 		Rain: It’s raining outside. Triggers when there is greater than 15% chance of rain in this or the next hour
 *	Blue 		Cold: It’s freezing outside
 *	Pink		Snow: There is a chance of snow
 *	Orange 		Hot:  It’s hot outside -- above 90F
 *	Green  		Sneeze: Pollen is in the air - above 7 (polin requires integration with IFTTT that sets a virtual switch in ST 
 *	White		All clear
 *
 *  With special thanks to insights from the Flasher script by Bob, the SmartThings Hue mood lighting script, the light on motion script by kennyyork@centralite.com, and the Smartthings severe weather alert script 
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
    name: "Color Changing Smart Weather Light",
    namespace: "",
    author: "Jim Kohlenberger",
    description: "A color changing smart weather lantern app that turns on with motion and turns a hue lamp different colors based on the weather.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/smart-light-timer.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/smart-light-timer@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Developers/smart-light-timer@2x.png")



preferences
{
    section("Select Motion Detector") {
        input "motion_detector", "capability.motionSensor", title: "Where?"
    }
    section("Control these bulbs...") {
		input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
		}
    section("Set brightness level for lights (100 is max representing 100%, default is 60)") {
        input "brightnessLevel", "number", title: "Brightess level (without %)?", required: false
    }
    section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipcode", "text", title: "Zip Code", required: false
	}
    section ("In addition to push notifications, for emergency weather send text alerts to...") {
		input "phone1", "phone", title: "Phone Number 1", required: false
		input "phone2", "phone", title: "Phone Number 2", required: false
		input "phone3", "phone", title: "Phone Number 3", required: false
	}
    section ("Optionally set lantern to turn green once, if this switch is turned on.") {
    	input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: false
    }
    section("Icon") {
        icon(title: "Select icon for app:", required: true)
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
    scheduleJob()  
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    log.debug "Weather Light: oldKeys: $oldKeys"
 
    unsubscribe()
    unschedule()
    scheduleJob()
	initialize()
}

def scheduleJob() {
	def sec = Math.round(Math.floor(Math.random() * 60))
	def min = 4 
    def cron = "$sec $min * * * ?"
    schedule(cron, "turnOff")
}

def checkForWeather() {
	def alerts
    def conditions
    def color ="Warm White"
    def statustest
    def value
	if(locationIsDefined()) {
		if(zipcodeIsValid()) {
			alerts = getWeatherFeature("alerts", zipcode)?.alerts
            conditions = getWeatherFeature("conditions", zipcode )
                       
		} else {
			log.warn "Severe Weather Alert: Invalid zipcode entered, defaulting to location's zipcode"
			alerts = getWeatherFeature("alerts")?.alerts
            conditions = getWeatherFeature("conditions") }
	} else {
		log.warn "Severe Weather Alert: Location is not defined"
	}

	log.debug "Weather conditions temp_f: ${conditions.current_observation.temp_f.toInteger()}"

	//  FREEZING BLUE	
    if (conditions.current_observation.temp_f <= 34 ) {
        	color = "Blue"
        log.debug "Weather temp below 34F, its freezing out so setting light to blue."
    }
   	
    //  HOT ORANGE
	if (conditions.current_observation.temp_f >= 80 )  {
        color = "Orange"
        log.debug "Weather temp above 80F, setting light to orange."
    }
    
 
    //	PURPLE RAIN FORCAST
	log.debug "Checking Forecast.io Weather"
	// Visit https://developer.forecast.io/register to get a forecast.io API key and insert it below
    httpGet("https://api.forecast.io/forecast/8b533da63e8dc7e74a1aa20acaf8ac13/$location.latitude,$location.longitude") {response -> 
            
		if (response.data) {
       		def precipprob = response.data.currently.precipProbability.floatValue() // A numerical value between 0 and 1 (inclusive) representing the probability of precipitation occurring at the given time. 
			def tempFar = response.data.currently.temperature.floatValue()
			def thisHour = response.data.hourly.data[0].precipProbability.floatValue() //this top of hour  	
			def nextHour = response.data.hourly.data[1].precipProbability.floatValue() //next top of hour
			log.debug "Actual current temp: ${tempFar}, Precipitation probability now: ${precipprob}, thisHour: ${thisHour}, nextHour ${nextHour}"
    		if ((thisHour >0.15) || (nextHour >0.15)) {
	    		color = "Purple" 
    	    	log.debug "Greater than 15% chance of rain in this or the next hour, setting light to Purple."
    		}
       	}   else {
        	log.debug "HttpGet Response data unsuccesful."
        }
    }
    
    //  PINK SNOW
    def f = getWeatherFeature("forecast", zipcode) //get("forecast")
	def f1= f?.forecast?.simpleforecast?.forecastday
	if (f1) {
		value = f1[0].snow_day 
	}
	else {
		log.warn "Forecast not found"
	}
  	
  	log.debug "The chance of snow = ${value}"
    if (!value.toString().contains("0.0")) {
    	if (!value.toString().contains("null")) {
    		color = "Pink"
    		log.debug "Weather shows chance of snow, setting light to Pink."
        }
    }
   
	sendcolor(color)
      
	def newKeys = alerts?.collect{it.type + it.date_epoch} ?: []
	log.debug "Severe Weather Alert: newKeys: $newKeys"

	def oldKeys = state.alertKeys ?: []
	log.debug "Severe Weather Alert: oldKeys: $oldKeys"

	if (newKeys != oldKeys) {

		state.alertKeys = newKeys

		alerts.each {alert ->
			if (!oldKeys.contains(alert.type + alert.date_epoch) && descriptionFilter(alert.description)) {
                color = "Red"
                sendcolor(color)
				flashLights()

			}
		}
	}
}

def descriptionFilter(String description) {
	def filterList = ["special", "statement", "test"]
	def passesFilter = true
	filterList.each() { word ->
		if(description.toLowerCase().contains(word)) { passesFilter = false }
	}
	passesFilter
}


def locationIsDefined() {
	zipcodeIsValid() || location.zipCode || ( location.latitude && location.longitude )
}

def zipcodeIsValid() {
	zipcode && zipcode.isNumber() && zipcode.size() == 5
}

private send(message) {
	sendPush message
	if (settings.phone1) {
		sendSms phone1, message
	}
	if (settings.phone2) {
		sendSms phone2, message
	}
	if (settings.phone3) {
		sendSms phone3, message
	}
}

def sendcolor(color) {
	log.debug "Sendcolor = $color"
    def hueColor = 0
    def saturation = 100

	switch(color) {
		case "White":
			hueColor = 52
			saturation = 19
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 //83
			break;
		case "Blue":
			hueColor = 70
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			break;
		case "Orange":
			hueColor = 10
			break;
		case "Purple":
			hueColor = 75
			break;
		case "Pink":
			hueColor = 83
			break;
		case "Red":
			hueColor = 100
			break;
	}

	state.previous = [:]

	hues.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation")
           
		]
	}
	
	log.debug "current values = $state.previous"
    
    // CHECK for GREEN button on
    if (mySwitch != null) {
    	if (mySwitch.latestValue("switch") == "on" ) {   
    		log.debug "mySwitch is on so setting light to GREEN and closing switch"
        	if (color != "Red") { //If its red, then override green
        		hueColor = 39
            	mySwitch.off()  
        	}
    	}
    }
    
  	def lightLevel = 60
    if (brightnessLevel != null) {
    	lightLevel = brightnessLevel 
    }
     
	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel]  
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
}

/// HANDLE MOTION

def turnOff() {
	log.debug "Timer fired, turning off light(s)"
    hues.off()
}

def motionHandler(evt) {
	if (evt.value == "active") {                // If there is movement then...
        log.debug "Motion detected, turning on light and killing timer"
        checkForWeather()
        unschedule( turnOff )                   // ...we don't need to turn it off.
    }
    else {                                      // If there is no movement then...
        def delay = 100 				
        log.debug "Motion cleared, turning off switches in (${delay})."
        pause(delay)
        hues.off()
    }
}

def initialize() {
	log.info "Initializing, subscribing to motion event at ${motionHandler} on ${motion_detector}"
    subscribe(motion_detector, "motion", motionHandler)
	subscribe(app, appTouchHandler)
}
def appTouchHandler(evt) {
	checkForWeather()
    def delay = 4000 				
    log.debug "App triggered with button press, turning off switches in (${delay})."
    pause(delay)
    hues.off()
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 1000
	def offFor = offFor ?: 1000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 1L
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
            hues.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
            hues.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offFor
		}
        //delay += offFor
        //s.off(delay:delay)
	}
}