/**
 *  Severe Weather Alert - PWS edition
 *
 *  Author: Jim Lewallen
 *  Date: 2013-03-04
 */

// Automatically generated. Make future change here.
definition(
    name: "Inclement Weather PWS",
    namespace: "",
    author: "jameslew@live.com",
    description: "Integrating a Personal Weather Station into SmartThings via Weather Underground.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section ("Weather threshholds to alert at..."){
    	input "highwindspeed", "number", title: "Avg Wind Speed", required: false
    	input "hightempf", "number", title: "High Temperature", required: false
    	input "lowtempf", "number", title: "Low Temperature", required: false
    	input "highrainfall", "number", title: "Rainfall Threshhold", required: false    
        input "apikey", "string", title: "Weather Underground API Key", required: false
    }

	section ("In addition to push notifications, send text alerts to...") {
		input "phone1", "phone", title: "Phone Number 1", required: false
		input "phone2", "phone", title: "Phone Number 2", required: false
		input "phone3", "phone", title: "Phone Number 3", required: false
	}
	section("Which people should I watch for?") {
		input "people", "capability.presenceSensor", title: "Presence sensor", description: "Which people(s)?", multiple: true, required: false
	}
	section ("Weather Underground Weather Station ID") {
		input "stationid", "text", title: "Station ID", required: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	setDefaultWeather()
    checkWeather()
	schedule("0 0/15 * * * ?", "checkWeather") //Check at top and half-past of every hour
    schedule("0 0 5,16 * * ?", "setDefaultWeather") //Reset in the morning

}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    setDefaultWeather()
    checkWeather()
	schedule("0 0/15 * * * ?", "checkWeather") //Check at top and half-past of every hour
    schedule("0 0 5,16 * * ?", "setDefaultWeather") //Reset in the morning
}

def checkWeather() {
	log.debug "Checking Weather"
	httpGet("http://api.wunderground.com/api/${apikey}/conditions/q/pws:${stationid}.json") 
    	{ response ->

		def windspeed = response.data.current_observation.wind_mph.floatValue()
        def tempf = response.data.current_observation.temp_f.floatValue()
	    def hourlyprecip = Float.parseFloat(response.data.current_observation.precip_1hr_in)
	    	    
        log.debug "Actual: ${windspeed}, ${tempf}, ${hourlyprecip}"
        log.debug "Threshholds: ${highwindspeed}, ${hightempf}, ${lowtempf}, ${hourlyprecip}"
        log.debug "State: ${state.lastHighWindSpeed}, ${state.lasthightempf}, ${state.lastlowtempf}, ${state.lasthourlyprecip}"
        
        if (windspeed > highwindspeed) {
        	if (windspeed >= state.lastHighWindSpeed + 3.0 ) {
				state.lastHighWindSpeed = windspeed
				notifyHousemates("High wind speed of ${windspeed}mph at the house")
            } else {log.debug "not enough wind speed change"}
        } else { state.lastHighWindSpeed = windspeed }
        
        if (tempf > hightempf) {
        	if (tempf >= state.lasthightempf + 3.0 ) {
                state.lasthightempf  tempf
        		notifyHousemates("Its very hot at the house: ${tempf}F")
            } else {log.debug "not enough high temp change"}
        } else { state.lasthightempf = tempf }
        
        if (tempf < lowtempf) {
        	if (tempf <= state.lastlowtempf - 3.0 ) {
        		state.lastlowtempf = tempf
				notifyHousemates("Its very cold at the house: ${tempf}F")
            } else { log.debug "not enough low temp change" }
        } else { state.lastlowtempf = tempf }
        
		if (hourlyprecip > highrainfall) {
        	if (hourlyprecip  >= state.lasthourlyprecip + 1.0 ) {
				state.lasthourlyprecip = hourlyprecip 
        		notifyHousemates("Its raining heavily at the house: ${hourlyprecip}in.")
            } else {log.debug "not enough precip change"}
        } else { state.lasthourlyprecip = hourlyprecip }
       	
    }
}

def logTraceWithPush(message) {
    sendPush message
    log.debug message
}

def setDefaultWeather() {
    state.lastHighWindSpeed = 0.0
    state.lasthightempf = 65.0
    state.lastlowtempf = 40.0
    state.lasthourlyprecip = 0.0
    log.debug "state: ${state.lastHighWindSpeed}, ${state.lasthightempf}, ${state.lastlowtempf}, ${state.lasthourlyprecip}"
}

private notifyHousemates(message) {

    def t0 = new Date(now() - (8 * 60 * 60 * 1000))
	def h0 = t0.getHours()

	if (h0 >= 6 && h0 <= 22)
    {
        log.debug "Weather event to be reported: ${message}"
        send(message)
    } else {
    	log.debug "Swallowing notification during off hours: ${t0.getHours()}"
    }
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

