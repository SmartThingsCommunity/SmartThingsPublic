/**
 *
 *  Adaptive Thermostat
 *
 *  Author: Santosh Nair (smartsanty@gmail.com)
 */
definition(
    name: "Adaptive Thermostat",
    namespace: "smartthings",
    author: "Santosh Nair (smartsanty@gmail.com)",
    description: "Activate heating (e.g space heater) and/or cooling sources (fan, window AC) in conjunction with any temperature sensor",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",
    pausable: true
)

preferences {
	section("Choose a temperature sensor"){
		input "sensor", "capability.temperatureMeasurement", title: "Temperature Sensor", required: true
	}
	section("Select the outlet(s) for available heating and/or cooling sources"){
		input "heatOutlets", "capability.switch", title: "Heating Outlets", multiple: true, required: false
        input "coolOutlets", "capability.switch", title: "Cooling Outlets", multiple: true, required: false
	}
	section("Set the desired temperature range"){
		input "setlowpoint", "decimal", title: "Set Lowest Temperature"
        input "sethighpoint", "decimal", title: "Set Highest Temperature"
	}
	section("When there's been movement from (optional, leave blank to not require motion)", hideWhenEmpty: true){
		input "motion", "capability.motionSensor", title: "Motion", required: false
        input "minutes", "number", title: "Minute(s)", required: false, hideWhenEmpty: "motion"
	}
}

def installed() {
	unschedule(climateCheckHandler)
	state.mode = "idle"
	subscribe(sensor, "temperature", temperatureHandler)
	if (motion) {
		subscribe(motion, "motion", motionHandler)
	}
    schedule("? 0/3 * * * ?", climateCheckHandler)
}

def updated() {
	unschedule(climateCheckHandler)
	state.mode = "idle"
	unsubscribe()
	subscribe(sensor, "temperature", temperatureHandler)
	if (motion) {
		subscribe(motion, "motion", motionHandler)
	}
    schedule("? 0/3 * * * ?", climateCheckHandler)
}

def temperatureHandler(evt) {
	if (hasBeenRecentMotion()) {
		evaluate(evt.doubleValue, setlowpoint, sethighpoint)
	} else {
    	log.debug "No Motion, Turning off all outlets"
    	setSystemIdle()
	}
}

def motionHandler(evt) {
	if (evt.value == "active" || hasBeenRecentMotion()) {
		def lastTemp = sensor.currentTemperature as Double
		if (lastTemp != null) {
			evaluate(lastTemp, setlowpoint, sethighpoint)
		}
	} else {
    	log.debug "No Motion, Turning off all outlets"
    	setSystemIdle()
	}
}

def climateCheckHandler() {
	if (hasBeenRecentMotion()) {
    	def lastTemp = sensor.currentTemperature as Double
		if (lastTemp != null) {
			evaluate(lastTemp, setlowpoint, sethighpoint)
		}
    }
}

private evaluate(currentTemp, desiredLowTemp, desiredHighTemp) {
	log.debug "EVALUATE($currentTemp, $desiredLowTemp, $desiredHighTemp)"
    def median = (desiredLowTemp + desiredHighTemp) / 2.0
    
    if (heatOutlets && currentTemp < desiredLowTemp) {
    	log.debug "Switching to Heating Mode"
    	heatOutlets.on()
        if (coolOutlets) { coolOutlets.off() }
        state.mode = "heating"
    } else if (coolOutlets && currentTemp > desiredHighTemp) {
    	log.debug "Switching to Cooling Mode"
        coolOutlets.on()
        if (heatOutlets) { heatOutlets.off() }
        state.mode = "cooling"
    } else if (state.mode == "cooling" && currentTemp < median) {
        if ((currentTemp - desiredLowTemp) < (median - currentTemp)) {
        	setSystemIdle()
        } else if (coolOutlets.size() > 1) {
        	coolOutlets[1..-1].each { coolOutlet -> coolOutlet.off() }
        }
    } else if (state.mode == "heating" && currentTemp > median) {
    	if ((desiredHighTemp - currentTemp) < (currentTemp - median)) {
        	setSystemIdle()
        } else if (heatOutlets.size() > 1) {
    		heatOutlets[1..-1].each { heatOutlet -> heatOutlet.off() }
        }
    } else {
    	log.debug "No changes required. System is ${state.mode}"
    }
}

private setSystemIdle() {
	if (heatOutlets) { heatOutlets.off() }
    if (coolOutlets) { coolOutlets.off() }
    state.mode = "idle"
}

private hasBeenRecentMotion() {
	def isActive = false
	if (motion && minutes) {
		def deltaMinutes = minutes as Long
		if (deltaMinutes) {
			def motionEvents = motion.eventsSince(new Date(now() - (60000 * deltaMinutes)))
			log.trace "Found ${motionEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
			if (motionEvents.find { it.value == "active" }) {
				isActive = true
			}
		}
	} else {
		isActive = true
	}
    
	return isActive
}
