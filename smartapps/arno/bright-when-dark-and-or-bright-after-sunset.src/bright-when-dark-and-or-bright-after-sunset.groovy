definition(
    name: "Bright When Dark And/Or Bright After Sunset",
    namespace: "Arno",
    author: "Arnaud",
    description: "Turn ON light(s) and/or dimmer(s) when there's movement and the room is dark with illuminance threshold and/or between sunset and sunrise. Then turn OFF after X minute(s) when the brightness of the room is above the illuminance threshold or turn OFF after X minute(s) when there is no movement.",
    category: "Convenience",
    iconUrl: "http://neiloseman.com/wp-content/uploads/2013/08/stockvault-bulb128619.jpg",
    iconX2Url: "http://neiloseman.com/wp-content/uploads/2013/08/stockvault-bulb128619.jpg"
)

preferences
{
	page(name: "configurations")
	page(name: "options")

	page(name: "timeIntervalInput", title: "Only during a certain time...")
    	{
		section
        	{
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
			}
		}
}

def configurations()
{
	dynamicPage(name: "configurations", title: "Configurations...", uninstall: true, nextPage: "options")
    	{
		section(title: "Turn ON lights on movement when...")
        	{
			input "dark", "bool", title: "It is dark?", required: true
            input "sun", "bool", title: "Between sunset and surise?", required: true
			}
		section(title: "More options...", hidden: hideOptionsSection(), hideable: true)
        	{
			def timeLabel = timeIntervalLabel()
			href "timeIntervalInput", title: "Only during a certain time:", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
			input "days", "enum", title: "Only on certain days of the week:", multiple: true, required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
			input "modes", "mode", title: "Only when mode is:", multiple: true, required: false
			}
		section ("Assign a name")
        	{
			label title: "Assign a name", required: false
			}
		}
}

def options()
{
	if (dark == true && sun == true)
    	{
		dynamicPage(name: "options", title: "Lights will turn ON on movement when it is dark and between sunset and sunrise...", install: true, uninstall: true)
    		{
			section("Control these light(s)...")
    			{
				input "lights", "capability.switch", title: "Light(s)?", multiple: true, required: false
    			}    
    		section("Control these dimmer(s)...")
    			{ 
        		input "dimmers", "capability.switchLevel", title: "Dimmer(s)?", multiple: true, required:false
        		input "level", "number", title: "How bright?", required:false, description: "0% to 100%"
				}
			section("Turning ON when it's dark and there's movement...")
    			{
				input "motionSensor", "capability.motionSensor", title: "Where?", multiple: true, required: true
				} 
			section("And then OFF when it's light or there's been no movement for...")
    			{
				input "delayMinutes", "number", title: "Minutes?", required: false
				}
			section("Using this light sensor...")
    			{
				input "lightSensor", "capability.illuminanceMeasurement",title: "Light Sensor?", multiple: false, required: true
        		input "luxLevel", "number", title: "Illuminance threshold? (default 50 lux)",defaultValue: "50", required: false
				}
			section ("And between sunset and sunrise...")
    			{
				input "sunriseOffsetValue", "text", title: "Sunrise offset", required: false, description: "00:00"
				input "sunriseOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
        		input "sunsetOffsetValue", "text", title: "Sunset offset", required: false, description: "00:00"
				input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
				}
			section ("Zip code (optional, defaults to location coordinates when location services are enabled)...")
        		{
				input "zipCode", "text", title: "Zip Code?", required: false, description: "Local Zip Code"
				}
			}
		}
	else if (dark == true && sun == false)
    	{
    	dynamicPage(name: "options", title: "Lights will turn ON on movement when it is dark...", install: true, uninstall: true)
    		{
			section("Control these light(s)...")
    			{
				input "lights", "capability.switch", title: "Light(s)?", multiple: true, required: false
    			}    
    		section("Control these dimmer(s)...")
    			{ 
        		input "dimmers", "capability.switchLevel", title: "Dimmer(s)?", multiple: true, required:false
        		input "level", "number", title: "How bright?", required:false, description: "0% to 100%"
				}
			section("Turning ON when it's dark and there's movement...")
    			{
				input "motionSensor", "capability.motionSensor", title: "Where?", multiple: true, required: true
				} 
			section("And then OFF when it's light or there's been no movement for...")
    			{
				input "delayMinutes", "number", title: "Minutes?", required: false
				}
			section("Using this light sensor...")
    			{
				input "lightSensor", "capability.illuminanceMeasurement",title: "Light Sensor?", multiple: false, required: true
        		input "luxLevel", "number", title: "Illuminance threshold? (default 50 lux)",defaultValue: "50", required: false
				}
			}
		}
    else if (sun == true && dark == false)
    	{
    	dynamicPage(name: "options", title: "Lights will turn ON on movement between sunset and sunrise...", install: true, uninstall: true)
    		{
			section("Control these light(s)...")
    			{
				input "lights", "capability.switch", title: "Light(s)?", multiple: true, required: false
    			}    
    		section("Control these dimmer(s)...")
    			{ 
        		input "dimmers", "capability.switchLevel", title: "Dimmer(s)?", multiple: true, required:false
        		input "level", "number", title: "How bright?", required:false, description: "0% to 100%"
				}
			section("Turning ON there's movement...")
    			{
				input "motionSensor", "capability.motionSensor", title: "Where?", multiple: true, required: true
				} 
			section("And then OFF there's been no movement for...")
    			{
				input "delayMinutes", "number", title: "Minutes?", required: false
				}
			section ("Between sunset and sunrise...")
    			{
				input "sunriseOffsetValue", "text", title: "Sunrise offset", required: false, description: "00:00"
				input "sunriseOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
        		input "sunsetOffsetValue", "text", title: "Sunset offset", required: false, description: "00:00"
				input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
				}
			section ("Zip code (optional, defaults to location coordinates when location services are enabled)...")
        		{
				input "zipCode", "text", title: "Zip Code?", required: false, description: "Local Zip Code"
				}
			}
		}
	else
    	{
    	dynamicPage(name: "options", title: "Lights will turn ON on movement...", install: true, uninstall: true)
    		{
			section("Control these light(s)...")
    			{
				input "lights", "capability.switch", title: "Light(s)?", multiple: true, required: false
    			}    
    		section("Control these dimmer(s)...")
    			{ 
        		input "dimmers", "capability.switchLevel", title: "Dimmer(s)?", multiple: true, required:false
        		input "level", "number", title: "How bright?", required:false, description: "0% to 100%"
				}
			section("Turning ON when there's movement...")
    			{
				input "motionSensor", "capability.motionSensor", title: "Where?", multiple: true, required: true
				} 
			section("And then OFF when there's been no movement for...")
    			{
				input "delayMinutes", "number", title: "Minutes?", required: false
				}
			}
    	}
}

def installed()
{
	log.debug "Installed with settings: ${settings}."
	initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}."
	unsubscribe()
	unschedule()
	initialize()
}

def initialize()
{
	subscribe(motionSensor, "motion", motionHandler)
    if (lights != null && lights != "" && dimmers != null && dimmers != "")
    	{
        log.debug "$lights subscribing..."
    	subscribe(lights, "switch", lightsHandler)
        log.debug "$dimmers subscribing..."
    	subscribe(dimmers, "switch", dimmersHandler)
        if (dark == true && lightSensor != null && lightSensor != "")
    		{
        	log.debug "$lights and $dimmers will turn ON when movement detected and when it is dark..."
			subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
			}
		if (sun == true)
    		{
        	log.debug "$lights and $dimmers will turn ON when movement detected between sunset and sunrise..."
			astroCheck()
        	subscribe(location, "position", locationPositionChange)
            subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
            subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
			}
        else if (dark != true && sun != true)
            {
            log.debug "$lights and $dimmers will turn ON when movement detected..."
            }
    	}
    else if (lights != null && lights != "")
    	{
        log.debug "$lights subscribing..."
    	subscribe(lights, "switch", lightsHandler)
        if (dark == true && lightSensor != null && lightSensor != "")
    		{
        	log.debug "$lights will turn ON when movement detected and when it is dark..."
			subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
			}
		if (sun == true)
    		{
        	log.debug "$lights will turn ON when movement detected between sunset and sunrise..."
			astroCheck()
        	subscribe(location, "position", locationPositionChange)
            subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
            subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
			}
        else if (dark != true && sun != true)
            {
            log.debug "$lights will turn ON when movement detected..."
            }
    	}
	else if (dimmers != null && dimmers != "")
    	{
        log.debug "$dimmers subscribing..."
    	subscribe(dimmers, "switch", dimmersHandler)
        if (dark == true && lightSensor != null && lightSensor != "")
    		{
        	log.debug "$dimmers will turn ON when movement detected and when it is dark..."
			subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
			}
		if (sun == true)
    		{
        	log.debug "$dimmers will turn ON when movement detected between sunset and sunrise..."
			astroCheck()
        	subscribe(location, "position", locationPositionChange)
            subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
            subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
			}
        else if (dark != true && sun != true)
            {
            log.debug "$dimmers will turn ON when movement detected..."
            }
    	}
        log.debug "Determinating lights and dimmers current value..."
        if (lights != null && lights != "")
        	{
            if (lights.currentValue("switch").toString().contains("on"))
                {
                state.lightsState = "on"
                log.debug "Lights $state.lightsState."
                }
            else if (lights.currentValue("switch").toString().contains("off"))
                {
                state.lightsState = "off"
                log.debug "Lights $state.lightsState."
                }
            else
                {
                log.debug "ERROR!"
                }
			}
		if (dimmers != null && dimmers != "")
        	{
            if (dimmers.currentValue("switch").toString().contains("on"))
                {
                state.dimmersState = "on"
                log.debug "Dimmers $state.dimmersState."
                }
            else if (dimmers.currentValue("switch").toString().contains("off"))
                {
                state.dimmersState = "off"
                log.debug "Dimmers $state.dimmersState."
                }
            else
                {
                log.debug "ERROR!"
                }
			}
}
            
def locationPositionChange(evt)
{
	log.trace "locationChange()"
	astroCheck()
}

def sunriseSunsetTimeHandler(evt)
{
	state.lastSunriseSunsetEvent = now()
	log.debug "SmartNightlight.sunriseSunsetTimeHandler($app.id)"
	astroCheck()
}

def motionHandler(evt)
{
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active")
    	{
        unschedule(turnOffLights)
    	unschedule(turnOffDimmers)
        if (dark == true && sun == true)
        	{
            if (darkOk == true && sunOk == true)
            	{
                log.debug "Lights and Dimmers will turn ON because $motionSensor detected motion and $lightSensor was dark or because $motionSensor detected motion between sunset and sunrise..."
                if (lights != null && lights != "")
                    {
                    log.debug "Lights: $lights will turn ON..."
                    turnOnLights()
                    }
                if (dimmers != null && dimmers != "")
                    {
                    log.debug "Dimmers: $dimmers will turn ON..."
                    turnOnDimmers()
                    }
				}
			else if (darkOk == true && sunOk != true)
            	{
                log.debug "Lights and Dimmers will turn ON because $motionSensor detected motion and $lightSensor was dark..."
                if (lights != null && lights != "")
                    {
                    log.debug "Lights: $lights will turn ON..."
                    turnOnLights()
                    }
                if (dimmers != null && dimmers != "")
                    {
                    log.debug "Dimmers: $dimmers will turn ON..."
                    turnOnDimmers()
                    }
				}
			else if (darkOk != true && sunOk == true)
            	{
                log.debug "Lights and dimmers will turn ON because $motionSensor detected motion between sunset and sunrise..."
                if (lights != null && lights != "")
                    {
                    log.debug "Lights: $lights will turn ON..."
                    turnOnLights()
                    }
                if (dimmers != null && dimmers != "")
                    {
                    log.debug "Dimmers: $dimmers will turn ON..."
                    turnOnDimmers()
                    }
				}
			else
            	{
				log.debug "Lights and dimmers will not turn ON because $lightSensor is too bright or because time not between sunset and surise."
                }
			}
		else if (dark == true && sun != true)
        	{
            if (darkOk == true)
            	{
                log.debug "Lights and dimmers will turn ON because $motionSensor detected motion and $lightSensor was dark..."
                if (lights != null && lights != "")
                    {
                    log.debug "Lights: $lights will turn ON..."
                    turnOnLights()
                    }
                if (dimmers != null && dimmers != "")
                    {
                    log.debug "Dimmers: $dimmers will turn ON..."
                    turnOnDimmers()
                    }
				}
			else
            	{
				log.debug "Lights and dimmers will not turn ON because $lightSensor is too bright."
                }
        	}
		else if (dark != true && sun == true)
        	{
            if (sunOk == true)
            	{
                log.debug "Lights and dimmers will turn ON because $motionSensor detected motion between sunset and sunrise..."
                if (lights != null && lights != "")
                    {
                    log.debug "Lights: $lights will turn ON..."
                    turnOnLights()
                    }
                if (dimmers != null && dimmers != "")
                    {
                    log.debug "Dimmers: $dimmers will turn ON..."
                    turnOnDimmers()
                    }
				}
			else
            	{
				log.debug "Lights and dimmers will not turn ON because time not between sunset and surise."
                }
        	}
		else if (dark != true && sun != true)
        	{
            log.debug "Lights and dimmers will turn ON because $motionSensor detected motion..."
            if (lights != null && lights != "")
				{
				log.debug "Lights: $lights will turn ON..."
				turnOnLights()
				}
			if (dimmers != null && dimmers != "")
            	{
				log.debug "Dimmers: $dimmers will turn ON..."
				turnOnDimmers()
				}
        	}
		}
	else if (evt.value == "inactive")
    	{
        unschedule(turnOffLights)
    	unschedule(turnOffDimmers)
		if (state.lightsState != "off" || state.dimmersState != "off")
        	{
            log.debug "Lights and/or dimmers are not OFF."
			if (delayMinutes)
            	{
                def delay = delayMinutes * 60
                if (dark == true && sun == true)
                    {
                    log.debug "Lights and dimmers will turn OFF in $delayMinutes minute(s) after turning ON when dark or between sunset and sunrise..."
                    if (lights != null && lights != "")
                        {
                        log.debug "Lights: $lights will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffLights)
                        }
                     if (dimmers != null && dimmers != "")
                        {
                        log.debug "Dimmers: $dimmers will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffDimmers)
                        }
                    }
                else if (dark == true && sun != true)
                    {
                    log.debug "Lights and dimmers will turn OFF in $delayMinutes minute(s) after turning ON when dark..."
                    if (lights != null && lights != "")
                        {
                        log.debug "Lights: $lights will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffLights)
                        }
                     if (dimmers != null && dimmers != "")
                        {
                        log.debug "Dimmers: $dimmers will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffDimmers)
                        }
                    }
                else if (dark != true && sun == true)
                    {
                    log.debug "Lights and dimmers will turn OFF in $delayMinutes minute(s) between sunset and sunrise..."
                    if (lights != null && lights != "")
                        {
                        log.debug "Lights: $lights will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffLights)
                        }
                     if (dimmers != null && dimmers != "")
                        {
                        log.debug "Dimmers: $dimmers will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffDimmers)
                        }
                    }
                else if (dark != true && sun != true)
                    {
                    log.debug "Lights and dimmers will turn OFF in $delayMinutes minute(s)..."
                    if (lights != null && lights != "")
                        {
                        log.debug "Lights: $lights will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffLights)
                        }
                    if (dimmers != null && dimmers != "")
                        {
                        log.debug "Dimmers: $dimmers will turn OFF in $delayMinutes minute(s)..."
                        runIn(delay, turnOffDimmers)
                        }
                    }
                }
			else
        		{
        		log.debug "Lights and dimmers will stay ON because no turn OFF delay was set..."
				}
            }
		else if (state.lightsState == "off" && state.dimmersState == "off")
        	{
        	log.debug "Lights and dimmers are already OFF and will not turn OFF in $delayMinutes minute(s)."
			}
		}
}

def lightsHandler(evt)
{
	log.debug "Lights Handler $evt.name: $evt.value"
    if (evt.value == "on")
    	{
        log.debug "Lights: $lights now ON."
        unschedule(turnOffLights)
        state.lightsState = "on"
        }
	else if (evt.value == "off")
    	{
        log.debug "Lights: $lights now OFF."
        unschedule(turnOffLights)
        state.lightsState = "off"
        }
}

def dimmersHandler(evt)
{
	log.debug "Dimmer Handler $evt.name: $evt.value"
    if (evt.value == "on")
    	{
        log.debug "Dimmers: $dimmers now ON."
        unschedule(turnOffDimmers)
        state.dimmersState = "on"
        }
	else if (evt.value == "off")
    	{
        log.debug "Dimmers: $dimmers now OFF."
        unschedule(turnOffDimmers)
        state.dimmersState = "off"
        }
}

def illuminanceHandler(evt)
{
	log.debug "$evt.name: $evt.value, lastStatus lights: $state.lightsState, lastStatus dimmers: $state.dimmersState, motionStopTime: $state.motionStopTime"
	unschedule(turnOffLights)
    unschedule(turnOffDimmers)
    if (evt.integerValue > 999)
    	{
        log.debug "Lights and dimmers will turn OFF because illuminance is superior to 999 lux..."
        if (lights != null && lights != "")
			{
			log.debug "Lights: $lights will turn OFF..."
			turnOffLights()
			}
		if (dimmers != null && dimmers != "")
			{
			log.debug "Dimmers: $dimmers will turn OFF..."
			turnOffDimmers()
			}
		}
	else if (evt.integerValue > ((luxLevel != null && luxLevel != "") ? luxLevel : 50))
		{
		log.debug "Lights and dimmers will turn OFF because illuminance is superior to $luxLevel lux..."
        if (lights != null && lights != "")
			{
			log.debug "Lights: $lights will turn OFF..."
			turnOffLights()
			}
		if (dimmers != null && dimmers != "")
			{
			log.debug "Dimmers: $dimmers will turn OFF..."
			turnOffDimmers()
			}
		}
}

def turnOnLights()
{
	if (allOk)
    	{
        if (state.lightsState != "on")
            {
            log.debug "Turning ON lights: $lights..."
            lights?.on()
            state.lightsState = "on"
            }
        else
            {
            log.debug "Lights: $lights already ON."
            }
		}
	else
    	{
        log.debug "Time, days of the week or mode out of range! $lights will not turn ON."
        }
}

def turnOnDimmers()
{
	if (allOk)
    	{
        if (state.dimmersState != "on")
            {
            log.debug "Turning ON dimmers: $dimmers..."
            settings.dimmers?.setLevel(level)
            state.dimmersState = "on"
            }
        else
            {
            log.debug "Dimmers: $dimmers already ON."
            }
		}
	else
    	{
        log.debug "Time, days of the week or mode out of range! $dimmers will not turn ON."
        }
}


def turnOffLights()
{
	if (allOk)
    	{
        if (state.lightsState != "off")
            {
            log.debug "Turning OFF lights: $lights..."
            lights?.off()
            state.lightsState = "on"
            }
        else
            {
            log.debug "Lights: $lights already OFF."
            }
		}
	else
    	{
        log.debug "Time, day of the week or mode out of range! $lights will not turn OFF."
        }
}

def turnOffDimmers()
{
	if (allOk)
    	{
        if (state.dimmersState != "off")
            {
            log.debug "Turning OFF dimmers: $dimmers..."
            dimmers?.off()
            state.dimmersState = "off"
            }
        else
            {
            log.debug "Dimmers: $dimmers already OFF."
            }
		}
	else
    	{
        log.debug "Time, day of the week or mode out of range! $dimmers will not turn OFF."
        }
}

def astroCheck()
{
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "Sunrise: ${new Date(state.riseTime)}($state.riseTime), Sunset: ${new Date(state.setTime)}($state.setTime)"
}

private getDarkOk()
{
	def result
	if (dark == true && lightSensor != null && lightSensor != "")
        {
		result = lightSensor.currentIlluminance < ((luxLevel != null && luxLevel != "") ? luxLevel : 50)
		}
	log.trace "darkOk = $result"
	result
}

private getSunOk()
{
	def result
	if (sun == true)
    	{
		def t = now()
		result = t < state.riseTime || t > state.setTime
		}
	log.trace "sunOk = $result"
	result
}

private getSunriseOffset()
{
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset()
{
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

private getAllOk()
{
	modeOk && daysOk && timeOk
}

private getModeOk()
{
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk()
{
	def result = true
	if (days)
    	{
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone)
        	{
			df.setTimeZone(location.timeZone)
			}
		else
        	{
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
			}
		def day = df.format(new Date())
		result = days.contains(day)
		}
	log.trace "daysOk = $result"
	result
}

private getTimeOk()
{
	def result = true
	if (starting && ending)
    	{
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
		}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private hideOptionsSection()
{
	(starting || ending || days || modes) ? false : true
}

private timeIntervalLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}