
/**
 *  Ventilation Guru
 *
 *  Copyright 2016 Tom Lawson
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
 
 To DO:
 add air quality
 fix blank activity feed entries
 fix double messages on init

 */
 
definition(
    name: "Ventilation Guru",
    namespace: "LawsonAutomation",
    author: "Tom Lawson",
    description: "This app implements night and day venting using whole house fans and/or other ventilation equipment. " +
    			 "It can supplement or in some climates take the place of conventional air-conditioners or heaters. " +
    			 "It makes use of inside sensors along with current and forecast conditions from Weather Underground to " + 
                 "accurately predict and fully automate when and how much to ventilate. " + 
    			 "Configurations typically include an indoor temperature sensor, a whole house fan, and motorized windows/skylights " +
                 "(though manually controlled windows with contact sensors are also an option). " + 
                 "This app also automatically sets your thermostat to heating or cooling and turns it off during venting. " +
                 "A simple learning algorithm allows the app to better regulate inside temperatures over time.",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/lawsonautomation/icons/master/guru-60.png",
    iconX2Url: "https://raw.githubusercontent.com/lawsonautomation/icons/master/guru-120.png",
    iconX3Url: "https://raw.githubusercontent.com/lawsonautomation/icons/master/guru-120.png") 
    
preferences {
    page(name: "mainPage")
}

def mainPage() {
	state.debugMode = false
    
    dynamicPage(name: "mainPage", install: true, uninstall: true) {        
    	section("Miscellaneous Inside Sensors") {
    	    input "myTempSensor", "capability.temperatureMeasurement", required: true, title: "Temperature Sensor"
    	    input "myHumiditySensor", "capability.relativeHumidityMeasurement", required: false, title: "Humidity Sensor"
    	    input "mySmokeDetectors", "capability.smokeDetector", required: false, multiple: true, title: "Smoke Detectors"
    	    input "myThermostats", "capability.thermostat", required: false, multiple: true, title: "Thermostats", submitOnChange: true
    	}
    	section("Daytime Comfort Zone (at least 5 F or 3 C range)") {
        	if (state.debugMode) {
        		// these two lines are needed as a workaround for a simulator bug
          	    input "myDaytimeMin", "decimal", title: "Minimum", required: true
    	    	input "myDaytimeMax", "decimal", title: "Maximum", required: true
            } else if (myDaytimeMin == null && myDaytimeMax == null && myThermostats) {
                // use thermostat values if you have them
            	def minTemp = cAdj(70)
            	def maxTemp = cAdj(78)
                minTemp = myThermostats[0]?.currentValue("heatingSetpoint")
                maxTemp = myThermostats[0]?.currentValue("coolingSetpoint")
        		// verify that the comfort zone is at least 5 F
            	if (maxTemp < minTemp + cAdj(5)) {
                	maxTemp = minTemp + cAdj(5)
                }
          	   	input "myDaytimeMin", "decimal", title: "Minimum", required: true, submitOnChange: true, defaultValue: "${minTemp}"
    	    	input "myDaytimeMax", "decimal", title: "Maximum", required: true, submitOnChange: true, defaultValue: "${maxTemp}"
        	} else {
            	if (myDaytimeMax == null) {
          	    	input "myDaytimeMin", "decimal", title: "Minimum", required: true, submitOnChange: true
                } else {
            		def minTemp = myDaytimeMax - cAdj(5)
          	    	input "myDaytimeMin", "decimal", title: "Minimum", required: true, submitOnChange: true, range: "-50..${minTemp}"
                }
            	if (myDaytimeMin == null) {
    	    		input "myDaytimeMax", "decimal", title: "Maximum", required: true, submitOnChange: true
                } else {
            		def maxTemp = myDaytimeMin + cAdj(5)
    	    		input "myDaytimeMax", "decimal", title: "Maximum", required: true, submitOnChange: true, range: "${maxTemp}..100"
                }
            }
    	}
    	section("Ventilation Fans") {
    	    input "myWholeHouseFans", "capability.switch", required: false, multiple: true, title: "Whole House Fans", submitOnChange: true
    	    input "myAtticFans", "capability.switch", required: false, multiple: true, title: "Attic Fans"
    	    input "myGarageFans", "capability.switch", required: false, multiple: true, title: "Garage Fans", submitOnChange: true
            if (myGarageFans) {
    	    	input "myGarageTempSensor", "capability.temperatureMeasurement", required: false, title: "Garage Temperature Sensor (optional)"
            }
    	    input "myBasementFans", "capability.switch", required: false, multiple: true, title: "Basement/Crawlspace Fans"
    	}
    	section("Windows/Skylights") { 
    	    input "myWindowsSwitch", "capability.switch", required: false, multiple: true, title: "Motorized Windows/Skylights", submitOnChange: true
    	    input "myContactSensors", "capability.contactSensor", required: false, multiple: true, title: "Window Contact Sensors", submitOnChange: true
    	    input "myCoverings", "capability.windowShade", required: false, multiple: true, title: "Window Coverings"
    	}
        if (myWholeHouseFans || myWindowsSwitch) {
    		section("Whole House Fan and Window/Skylight Operation") {
        		if (myWholeHouseFans && (myWindowsSwitch || myContactSensors)) {
    	    		input "myWindowsNeeded", "number", title: "Number of Open Windows Needed for Whole House Fan (default is 1)", required: false
            	}
				input "myModes", "mode", title: "Whole House Fan/Window Operating Modes (default is All)", multiple: true, required: false
				input "mySuspendOperation", "enum", title: "Suspend Whole House Fan/Window Operation", options: ["Temporarily Suspend"], required: false
    		}
        }
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
		}
    	section("Users Guide") {
            href(name: "href",
             	title: "Descriptions, Tips and Tricks",
                required: false,
        	    image: "https://raw.githubusercontent.com/lawsonautomation/icons/master/info.png",
                page: "UsersGuide")
    	}
		section("Version 1.2.2 - Copyright © 2016 Thomas Lawson. " +
    			"If you like this app and would like to contribute to its development (and the development of similar apps), " +
        		"tap the link below to make a donation.") {
    	    href(name: "LawsonAutomation",
    	         title: "Donate via PayPal",
    	         description: "Tap to Donate",
    	         required: false,
    	         style: "external",
    	         image: "https://raw.githubusercontent.com/lawsonautomation/icons/master/guru-60.png",
    	         url: "http://PayPal.Me/LawsonAutomation")
		}
    }
}

def UsersGuide() {
    dynamicPage(name: "UsersGuide", title: "User's Guide", nextPage: "mainPage") {
        section("Overview") {
        	paragraph "This app implements night and day venting using whole house fans and/or other ventilation equipment. " +
    			 	"It can supplement or in some climates take the place of conventional air-conditioners or heaters. " +
    			 	"It makes use of inside sensors along with current and forecast conditions from Weather Underground to " + 
                 	"accurately predict and fully automate when and how much to ventilate. " + 
    			 	"Configurations typically include a whole house fan, motorized windows/skylights or window's with contact sensors, and an indoor temperature sensor. " + 
                 	"It also automatically sets your thermostat to heating or cooling and turns it off during venting. " +
                 	"A simple learning algorithm allows the app to better regulate inside temperatures over time."
        }
        section("Daytime Comfort Zone Minimum and Maximum") {
        	paragraph "The comfort zone boundaries should be the same as the daytime settings for your thermostat, " +
            		  "the minimum being the heating set point and the maximum being the cooling set point."
        }
        section("Temperature Sensor") {
        	paragraph "The temperature sensor should be placed in a central part of the house away from windows, doors, vents, or any other sources of heat or cold. " +
            		  "When in doubt, place it near your thermostat.  Best case is for your temperature sensor to also be your thermostat."
        }
        section("Humidity Sensor") {
        	paragraph "This sensor allows inside and outside air to be compared when outside air has moderate to high levels of humidity. " +
            		  "In drier climates such as California this sensor is not necessary. "
        }
        section("Smoke Detectors") {
        	paragraph "If any of these sensors detect smoke, whole house fans are turned off and will remain off until no smoke is detected. " +
            		  "Windows are not shut when smoke is present for egress purposes. This is an important, recommended safety feature."
        }
        section("Thermostats") {
        	paragraph "These thermostats will be turned off when windows are open or whole house fans are on. " +
            		  "Additionally, thermostats will be set to cooling or heating mode automatically when not venting."
        }
        section("Whole House Fans") {
        	paragraph "Whole house fans ventilate inside air into the attic, drawing air from windows or skylights. " + 
            		  "These fans will be utilized at appropriate times during the day to raise or lower inside temperatures. " +
                      "When and for how long they run is calculated using forecast and current temperatures for your area."
                      
            paragraph "WARNING: Whole house fans, automated or not, create a strong negative pressure inside the home. " +
            		  "There should be no circumstances in which flame, ash, or smoke can be drawn into the home from a fireplace. " +
                      "A glass insert or other like measure should always be installed along with a whole house fan to prevent " +
                      "fire hazard or smoke/ash related damage."
        }
        section("Attic Fans") {
        	paragraph "When the house is in cooling mode, attic fans will run continually to lower attic temperatures. " +
            		  "Attic fans are turned off during whole house fan operation to save wear and tear on attic fans. " +
            		  "No attic temperature sensor is required."
        }
        section("Garage Fans") {
        	paragraph "When in cooling mode, the garage fan will turn on in the late afternoon and off in the late morning. " +
            		  "These are the times when outside temperatures are lower than garage temperatures. " +
              		  "No garage temperature sensor is required."
        }
        section("Basement/Crawlspace Fans") {
        	paragraph "When heating is needed, the basement/crawlspace fans will run when outside temperatures exceed temperatures inside the house. " +
            		  "No basement/crawlspace temperature sensor is required."
        }
        section("Motorized Windows/Skylights") {
        	paragraph "Windows/skylights are automatically opened to accommodate heating or cooling when conditions are right."
        }
        section("Contact Sensors") {
        	paragraph "Contact sensors can be used either as an extra check that a motorized window is open or to indicate that a manual window has been opened."
        }
        section("Window Coverings") {
        	paragraph "These coverings will be opened along with windows when ventilating. " +
            		  "Otherwise, window coverings are opened at sunrise and closed at sunset."
        }
        section("Whole House Fan/Window Operating Modes") {
        	paragraph "By default, whole house fans and windows/skylights will operate in all modes. " +
            		  "However, you can specify with this option under which modes you want windows and whole house fans to automatically open/turn on."
        }
        section("Number of Open Windows Needed") {
        	paragraph "Whole house fans will not turn on automatically until the total number of window/skylights and contact sensors equals or exceeds this number. " +
            		  "Note:  If an open window is automated and has a contact sensor installed, this will count as two open windows."
        }
        section("Suspend Operation") {
        	paragraph "The automated operation of Whole House Fans and windows/skylights can be suspended temporarily by clicking the checkbox. " +
            		  "To resume operation, deselect the checkbox."
        }
        section("Tips and Tricks") {
        	paragraph "Look in 'Notifications' of the SmartThings app to see what Ventilation Guru is doing."
        	paragraph "Whole house fan automation can be achieved (for cooling) without motorized windows by using contact sensors and manually opening the windows in the evening. " +
            		  "It is not recommended that whole house fans be automated without motorized windows, contact sensors, or some other means of obtaining outside air, such as louvers."
            paragraph "The rule of thumb regarding the open window area required for a whole house fan is to take the square of the diameter of the fan and then double it."
        	paragraph "If your thermostat is not also controlled by this app, the comfort zone minimum and maximum might need to be within rather than equal to the daytime thermostat setting."
            paragraph "WARNING: Without proper ventilation, whole-house fans can create a backdraft in your furnace or water heater, potentially causing carbon monoxide to be pulled into your home. " +
            		  "Operating the fan without open windows may also cause the fan to overheat."
            paragraph "WARNING: Whole house fans, automated or not, create a strong negative pressure inside the home. " +
            		  "There should be no circumstances in which flame, ash, or smoke can be drawn into the home from a fireplace. " +
                      "A glass insert or other like measure should always be installed along with a whole house fan to prevent " +
                      "fire hazard or smoke/ash related damage."
        }
    }
}
    
def installed() {
	LOG "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	LOG "Updated with settings: ${settings}"
    unschedule()
	unsubscribe()
	initialize()
}

// initiallization methods

def initialize() { 
    initGlobals()
    subscriptions()
    def nowString = initDaytimeFlag()
    initInsideTempAndHumidity()
   
    // change the ventilation algorithm at dawn
    subscribe(location, "sunrise", sunriseHandler)
    subscribe(location, "sunset", sunsetHandler)
        
    // turn on the garage fan if conditions are right and after 7 PM
    state.oldOutsideTemp = nowString > "19:00" ? 999 : state.outsideTemp
         
    // init window coverings
    if (myCoverings) {
    	if (state.daytime) {
    		openUp(myCoverings, "coverings", "Coverings")
    	} else {
    		closeUp(myCoverings, "coverings", "Coverings")
    	}
    }

	getCurrentConditions()
    runEvery10Minutes(getCurrentConditions)
}

def initGlobals() {    
    state.morningAdjConst = 5 
    state.morningAdjustment = 0
    state.currentConditionsFailCntr = 99 // start off as if we've never talked to weather underground
    state.forecastFailCntr = 99
    state.callCntr = 99    // used to get forecast hourly
    state.rain = false
    state.curPrecip = 0.0
    state.curPrecipCntr = 0
    
    // inside mean temp and skew calculations
    state.insideTempIndex = 1 // used to update array of inside temperatures
    state.insideMeanTempReady = false // true when we have 24 inside temperatures, one per hour
    state.greenhouseEffectCntr = 0  // increments when skew is < 1 && > -1
    if (state.greenhouseEffect == null) { // keep the old greenhouse effect if we have it
    	state.greenhouseEffect = cAdj(3.0) // accounts for greenhouse effect and inside sources of heat
    } else {
    	LOG("Saved Greenhouse Effect is ${state.greenhouseEffect}") 
        sanityCheckGreenhouseEffect()
    }
    
    state.skew = 0
    state.meanRange = (myDaytimeMax + myDaytimeMin) / 2  
    state.outsideTemp = state.meanRange
    state.meanOutside = state.meanRange
    state.outsideHumidity = 20
    state.outsideHeatIndex = state.meanRange
    state.windowsNeeded = myWindowsNeeded ?: (myWindowsSwitch || myContactSensors) ? 1 : 0
    state.newOutsideTemp = state.meanRange
    state.oldOutsideTemp = state.meanRange

	initDeviceStates()
}

def initDeviceStates() {
    // these device state enumerations lock-in manual changes and prevent thrashing
    state.Unknown = 0
    state.On      = 1
    state.Off     = 2
    state.Open    = 3
    state.Closed  = 4
    state.Opening = 5
    state.Closing = 6
    state.Heating = 7
    state.Cooling = 8
    
    state.wholeHouseFans = state.Unknown
    state.atticFans = state.Unknown
    state.basementFans = state.Unknown
    state.windowsSwitch = state.Unknown
    state.coverings = state.Unknown
    state.garageFans = state.Unknown
    state.thermostats = state.Unknown
    state.modes = state.Unknown
}

def subscriptions() {
    subscribe(myTempSensor, "temperature", temperatureChangedHandler)
    if (myWindowsSwitch) {
   	 	subscribe(myWindowsSwitch, "switch.on", windowOpenedHandler)
    	subscribe(myWindowsSwitch, "switch.off", windowClosedHandler)
    }
    if (myWholeHouseFans) {
    	subscribe(myWholeHouseFans, "switch.on", whfOnHandler)
    	subscribe(myWholeHouseFans, "switch.off", whfOffHandler)
    }
    if (myModes) {
    	subscribe(location, "mode", modeChangedHandler)
    }
    if (myHumiditySensor) {
    	subscribe(myHumiditySensor, "humidity", humidityChangedHandler)
    }
    if (myContactSensors) {
    	subscribe(myContactSensors, "contact.open", windowOpenedHandler)
    	subscribe(myContactSensors, "contact.closed", windowClosedHandler)
    }
    if (mySmokeDetectors) {
    	subscribe(mySmokeDetectors, "smoke", smokeChangedHandler)
    }
}

// returns a string of current time in HH:mm
def initDaytimeFlag() {
    def myDate = new Date(now())
    def nowString = myDate?.format("HH:mm", location?.timeZone)     
    def sunriseDate = Date?.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", location?.currentValue("sunriseTime"))
    def sunsetDate = Date?.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", location?.currentValue("sunsetTime"))
    def sunriseTime = sunriseDate?.format("HH:mm", location?.timeZone) 
    def sunsetTime = sunsetDate?.format("HH:mm", location?.timeZone) 
    state.daytime = (nowString > sunriseTime && nowString < sunsetTime)
    LOG("Sunrise: ${sunriseTime}, Now: ${nowString}, Sunset: ${sunsetTime}, Daytime:  ${state.daytime}")
    return nowString
}

def initInsideTempAndHumidity() {
    // init inside temp
    def currentState = myTempSensor.temperatureState
    state.insideTemp = 72.0
    if (currentState) {
    	state.insideTemp = currentState.integerValue
    	LOG "Initial Inside Temp: ${state.insideTemp}"
    } else {
    	sendNotificationEvent("Ventilation Guru Error:  Unable to obtain initial inside temperature!")
    }
    state.insideHeatIndex = state.insideTemp
    state.insideHumidity = 20
    
    //state.highWaterMark = state.insideTemp
    
    // init the inside humidity and heatIndex if we have a humidity sensor
    if (myHumiditySensor) {
    	def response = myHumiditySensor.currentState("humidity")
        if (response) {
        	state.insideHumidity = response?.integerValue
    	    state.insideHeatIndex = getHeatIndex(state.insideTemp, state.insideHumidity)
    		LOG "Initial Inside Humidity: ${state.insideHumidity} Temp: ${state.insideTemp} HeatIndex: ${state.insideHeatIndex}"
		}
    }
}

// event handlers

def smokeChangedHandler(evt) {
	LOG "Smoke: ${evt?.stringValue}"
    makeComparisons()    
}

def whfOnHandler(evt) {
	if (state.wholeHouseFans != state.On) {
    	// WHF was manually turned on, so set other devices accordingly
    	myWindowsSwitch?.on()
    	myThermostats?.off()
    	myAtticFans?.off()
    }
}

def whfOffHandler(evt) {
	if (state.wholeHouseFans != state.Off) {
    	// reset devices that we changed when WHF was manually turn on
    	state.windowsSwitch = state.Unknown
    	state.thermostats = state.Unknown
    	state.atticFans = state.Unknown
        LOG("whfOffHandler")
    	makeComparisons()  
    }
}

def windowOpenedHandler(evt) {
	LOG("Window opened")
	// disable thermostats	
    turnOff(myThermostats, "thermostats", "Thermostats")
	def windowCnt = openWindowCnt()
	if (windowCnt >= state.windowsNeeded) {
    	makeComparisons()
    }
}

def windowClosedHandler(evt) {
	def windowCnt = openWindowCnt()
	if (windowCnt < state.windowsNeeded) {
		LOG("Windows closed")
    	// make sure the WHF is off
        turnOff(myWholeHouseFans, "wholeHouseFans", "Whole House Fans")
    }
    if (windowCnt == 0) {
		setThermostatMode()
    }
}

def setThermostatMode() {
	if (myThermostats) {
    	if (state.wholeHouseFans == state.On || openWindowCnt() != 0) {
			// disable thermostats	
    		turnOff(myThermostats, "thermostats", "Thermostats")
        } else if (state.skew > 0) {
        	if (state.thermostats != state.Heating) {
        		state.thermostats = state.Heating
    			myThermostats.heat()
    			sendNotificationEvent("Ventilation Guru:  Thermostat(s) set to Heat mode")
            }
    	} else {
        	if (state.thermostats != state.Cooling) {
        		state.thermostats = state.Cooling
        		myThermostats.cool()
    			sendNotificationEvent("Ventilation Guru:  Thermostat(s) set to Cooling mode")
            }
    	}
    }
}

def temperatureChangedHandler(evt) {
	state.insideTemp = evt?.doubleValue
    state.insideHeatIndex = getHeatIndex(state.insideTemp, state.insideHumidity)
    LOG("Inside Temp: ${state.insideTemp}")
    makeComparisons()    
}

def humidityChangedHandler(evt) {
	state.insideHumidity = evt?.doubleValue
    state.insideHeatIndex = getHeatIndex(state.insideTemp, state.insideHumidity)
    LOG "Inside Humidity Changed: ${state.insideHumidity} Inside Temp: ${state.insideTemp} HeatIndex: ${state.insideHeatIndex}"
    makeComparisons()    
}

// always open window coverings at sunrise
def sunriseHandler(evt) {
	state.morningAdjustment = state.morningAdjConst
	openUp(myCoverings, "coverings", "Coverings")
    state.daytime = true
    makeComparisons()    
}

def sunsetHandler(evt) {
	state.morningAdjustment = 0
    state.daytime = false
	// close window coverings at sunset if windows are closed
	if (openWindowCnt() == 0) {
		closeUp(myCoverings, "coverings", "Coverings")
    }
    makeComparisons()    
}

// mode changed
def modeChangedHandler(evt) {
	LOG "Mode Changed:  ${location.mode}"
    makeComparisons()    
}

// Utilities

boolean inWrongMode() {
	boolean status = false
	if (myModes) {
    	status = true
    	myModes.each {
   			LOG("mode sensor: ${mode}, it ${it}")
    		//def mode = it.currentValue("mode")
    		def mode = it
    		if (mode == location.mode) {
   				LOG("mode OK!!!!!")
    			status = false
       		}
    	}
    }
    return status
}

def openWindows() {
	// don't resend an open command
	if (myWindowsSwitch && state.windowsSwitch != state.On && state.windowsSwitch != state.Opening) {
    	LOG("Open windows start")
    	state.windowsSwitch = state.Opening
        LOG "thermo state: ${myThermostat?.currentValue('thermostatOperatingState')}"
        if (myThermostat && myThermostat.currentValue('thermostatOperatingState') != "idle") {
        	// wait 5 minutes in case the thermostat is in short cycle prevention mode
    		runIn(5 * 60, physicallyOpenWindows)
        } else {
    		physicallyOpenWindows()
        }
    }
}

def physicallyOpenWindows() {
	if (state.windowsSwitch == state.Opening) {
    	turnOn(myWindowsSwitch, "windowsSwitch", "Windows")
    	state.windowsSwitch = state.Opening
		// change state later to indicate we're done, preventing WHF from openning before window is done opening
    	runIn(90, completeOpenWindows)
    }
}

def completeOpenWindows() {
	if (state.windowsSwitch == state.Opening) {
    	state.windowsSwitch = state.On
    }
    makeComparisons()    
}

def closeWindows() {
	if (myWindowsSwitch) {
    	if (state.windowsSwitch == state.Unknown) {
    		turnOff(myWindowsSwitch, "windowsSwitch", "Windows")
    		LOG("Windows Closed")
    	} else if (state.windowsSwitch != state.Off && state.windowsSwitch != state.Closing) {
    		// close windows without thrashing (wait awhile)
    		state.windowsSwitch = state.Closing
    		runIn(45 * 60, completeCloseWindows) 
    	}
    }
}

def completeCloseWindows() {
    LOG "Complete Close windows: ${state.windowsSwitch}"
	if (state.windowsSwitch == state.Closing) {
    	turnOff(myWindowsSwitch, "windowsSwitch", "Windows")
    }
}

def turnOnWhfs() {
	if (openWindowCnt() >= state.windowsNeeded) {
        turnOn(myWholeHouseFans, "wholeHouseFans", "Whole House Fans")
    }
}

boolean smokeDetected() {
	def status = false
	if (mySmokeDetectors) {
   		mySmokeDetectors.each {
    		def smoke = it.currentValue("smoke")
   			LOG "smoke sensor: ${smoke}"
    		if (smoke == "detected") {
    			status = true
       		}
    	}
    }
    return status
}

// returns number of open windows
int openWindowCnt() {
    int cntr = 0
	if (myContactSensors) {
    	// add up contact sensors
    	myContactSensors.each {
    		def contact = it.currentValue("contact")
    		if (contact == "open") {
    			cntr++
        	}
        }
    }
    // don't count windows still opening
    if (myWindowsSwitch && state.windowsSwitch == state.On) { 
    	// add up windows
    	myWindowsSwitch.each {
    		def window = it.currentValue("switch")
    		if (window == "on") {
    			cntr++
        	}
        }
    }
    LOG "openWindowCnt: ${cntr}, windowsRequired: ${state.windowsNeeded}"
    return cntr
}

// NOAA formula most accurate from 70-80 degrees F
// HI = 0.5 * {T + 61.0 + [(T-68.0)*1.2] + (RH*0.094)}
def getHeatIndex(double temperature, double humidity) {
	// if we have no humidity sensor just deal with dry-bulb values
    if (!myHumiditySensor) {
    	return temperature
    }
    def temp = temperature
    LOG("TempScale:  ${location.temperatureScale}")
    if (location.temperatureScale == "C") {
    	// convert to F for calculation
        temp = cToF(temp)
    }
    // calculate heatIndex
    temp = 0.5 * (temp + 61.0 + ((temp-68.0) * 1.2) + (humidity * 0.094))
    if (location.temperatureScale == "C") {
    	// convert back to C
        temp = fToC(temp)
    }
    // do not let this formula lower the dry bulb temperature
    if (temp < temperature) {
    	temp = temperature
    }
    LOG("Temp: ${temperature}, Humidity: ${humidity}, HeatIndex: ${temp}")
    return temp
}

def cToF(temp) {
	return temp * 1.8 + 32
}

def fToC(temp) {
	return (temp - 32) * 0.5555555556
}

// correct an offset if using metric
def cAdj(offset) {
    if (location.temperatureScale == "C") {
    	offset = offset * 0.5555555556
    }
	return offset
}

def turnOn(device, String devName, String extName) {
	if (device) {
	// don't send an open command if closing as it's already open
    	if (state[devName] == state.Closing) {
    		state[devName] = state.On
		} else if (state[devName] != state.On) {
    		state[devName] = state.On 
   			device.on()
        	def action = (devName == "windowsSwitch") ? "Opened" : "Turned on"
    		sendNotificationEvent("Ventilation Guru: ${action} ${extName}")
    	}
    }
}

def turnOff(device, String devName, String extName) {
	// don't resend a command
	if (device && state[devName] != state.Off) {
    	state[devName] = state.Off 
    	device.off()
        def action = (devName == "windowsSwitch") ? "Closed" : "Turned off"
    	sendNotificationEvent("Ventilation Guru: ${action} ${extName}")
    }
}

def openUp(device, String devName, String extName) {
	// don't set an open command if closing as it's already open
    if (state[devName] == state.Closing) {
    	state[devName] = state.Open
    } else if (device && state[devName] != state.Open) {
    	state[devName] = state.Open 
    	device.open()
    	sendNotificationEvent("Ventilation Guru: Opened ${extName}")
    }
}

def closeUp(device, String devName, String extName) {
	// don't resend a command
	if (device && state[devName] != state.Closed) {
    	state[devName] = state.Closed    
   		device.close()
    	sendNotificationEvent("Ventilation Guru: Closed ${extName}")
    }
}

def getCurrentConditions() {
    def cond = getWeatherFeature("conditions")
    if (cond) {
        if (location.temperatureScale == "C") {
    		state.outsideTemp = cond?.current_observation?.temp_c
        } else {
    		state.outsideTemp = cond?.current_observation?.temp_f
        }        
        if (myHumiditySensor) {
        	// get the outdoor humidity only if needed
        	def humidityString = cond?.current_observation?.relative_humidity
        	// drop the % sign and convert to integer
    		state.outsideHumidity = humidityString?.substring(0, humidityString?.length() - 1)?.toInteger()
        	if (state.outsideHumidity == null) {
            	sendNotificationEvent("Ventilation Guru Error:  Unable to obtain outside humidity!")
                state.outsideHumidity = 20
        	}
        	state.outsideHeatIndex = getHeatIndex(state.outsideTemp, state.outsideHumidity)
        } else {
            state.outsideHeatIndex = state.outsideTemp
        }  
        processCurPrecip(cond?.current_observation.precip_1hr_in.toDouble())        
    	state.currentConditionsFailCntr = 0
	} else {
        state.currentConditionsFailCntr = state.currentConditionsFailCntr + 1
        if (state.currentConditionsFailCntr > 6) {
            // inside hourly values are not accurate anymore
    		state.insideTempIndex = 1
        	state.insideMeanTempReady = false
        }
    }   
    // get forecast every hour in case it has changed
    state.callCntr = state.callCntr + 1
    if (state.callCntr > 6) {
        state.callCntr = 1
    	getForecast()
    }  
    LOG "Outside Temp: ${state.outsideTemp}, Humidity: ${state.outsideHumidity}, HeatIndex: ${state.outsideHeatIndex}, cur precip: ${state.curPrecip}"
    makeComparisons()        
    setThermostatMode()
}

def processCurPrecip(double curPrecip) {
    // sanity check curPrecip 
    curPrecip = (curPrecip <= 0 || curPrecip > 999) ? 0 : curPrecip
	// prevent window thrashing, remember precip for an hour
	if (curPrecip) {
    	state.curPrecip = curPrecip
        state.curPrecipCntr = 6
    } else if (state.curPrecipCntr <= 0) {
    	state.curPrecip = 0.0
        state.curPrecipCntr = 0
    } else {
    	state.curPrecipCntr = state.curPrecipCntr - 1
    }
}

def getForecast() {
	// for garage fan, we need to know when outside temp starts to drop
    state.oldOutsideTemp = state.newOutsideTemp
    state.newOutsideTemp = state.outsideTemp

	getHourlyConditions()
    
    def forecastMeanDelta = state.meanRange - state.meanOutside
    
    // remember inside temp every hour
    state[state.insideTempIndex.toString()] = state.insideTemp
    state.insideTempIndex = state.insideTempIndex + 1
    if (state.insideTempIndex > 24) {
    	state.insideTempIndex = 1
        state.insideMeanTempReady = true
    }
    
    // get inside mean temp
    def insideMean = 0
    if (state.insideMeanTempReady) {
    	for (int i = 1; i <= 24; i++) {
    		insideMean += state[i.toString()]
    	}
        insideMean /= 24
    } else {
        insideMean = state.meanRange
	}
    
    calculateSkew(insideMean, forecastMeanDelta)
    calculateTarget()
	LOG "skew: ${state.skew}  Target: ${state.target}  meanOutside: ${state.meanOutside} insideMean: ${insideMean}" //greenhouseEffect ${state.greenhouseEffect} deltaWeight ${state.deltaWeight}"
}

// target is the goal temperature
def calculateTarget() {
	// determine target differently for heating and cooling
    if (state.skew <= 0) {
    	// in cooling mode
        state.target =  state.meanRange + cAdj(2) + state.skew
        if (state.target < myDaytimeMin) {
        	state.target = myDaytimeMin
        }
    } else {
    	// in heating mode, heat only up to daytime range mean
        state.target = state.meanRange - cAdj(3) + state.skew  
        if (state.target > state.meanRange + cAdj(1)) {
        	state.target = state.meanRange + cAdj(1)
        }
    }
}

// skew determines how much to heat/cool
def calculateSkew(insideMean, forecastMeanDelta) {
    def oldSkew = state.skew
    // to determine heating/cooling level, include forecast outside temp mean delta to inside temp mean delta, less a bit for greenhouse effect
    double totalDelta = forecastMeanDelta + (state.meanRange - insideMean) / 2
    state.skew = totalDelta - state.greenhouseEffect 

	// learn the best way to anticipate heating and cooling needs
    if (state.skew <= 2 && state.skew >= -2) {
    	// no heating or cooling so inside temp should stay near middle of range
    	state.greenhouseEffectCntr = state.greenhouseEffectCntr + 1
        if (state.greenhouseEffectCntr >= 24) {       
        	state.greenhouseEffectCntr = 0
            // no heating or cooling for 24 hours so mean inside temp should equal midpoint of range
            state.greenhouseEffect = state.greenhouseEffect + (insideMean - state.meanRange) / 2 
            // sendNotificationEvent("New Greenhouse Effect = ${state.greenhouseEffect}") 
            sanityCheckGreenhouseEffect()
            // recalculate skew
    		state.skew = totalDelta - state.greenhouseEffect 
            LOG "New greenhouse effect adjustment: ${state.greenhouseEffect},  New Skew: ${state.skew}"
        }
    } else {
    	// not a good sample, so start over
        state.greenhouseEffectCntr = 0
    }
    
    // change thermostat heat/cooling mode if things changed
    if (myThermostats && state.thermostats == state.On && (oldSkew > 0 && state.skew <= 0 || oldSkew <= 0 && state.skew > 0)) {
    	// reset thermostat to heating or cooling after making comparisons and completing any actions
        state.thermostats = state.Unknown
    }
}

def getHourlyConditions() {
    def resp = getWeatherFeature("hourly")
    if (!resp) {
    	// couldn't get the forecast so leave the old forecast unchanged
        LOG("Ventilation Guru Error: Could not obtain forecast information!")
    	state.forecastFailCntr = state.forecastFailCntr + 1
    	return 0
    }
    
    // get rain forecast for now and next hour
    def hourlyRain = resp?.hourly_forecast?.condition
    state.rain = (hourlyRain && (hourlyRain[0].contains("Rain") || hourlyRain[1].contains("Rain"))) 
    if (hourlyRain) {
    	LOG("hourlyRain ${state.rain} ${hourlyRain}")
    }
    
    // get forecast temps
    def outsideHourly
    if (location.temperatureScale == "F") {
    	outsideHourly = resp?.hourly_forecast?.temp?.english
    } else {
    	outsideHourly = resp?.hourly_forecast?.temp?.metric
    }
    double meanOutside = 0
    if (outsideHourly) {
    	state.forecastFailCntr = 0
    	outsideHourly[0..23].each {
        	meanOutside += it?.toDouble()
        }
        meanOutside /= 24
        state.meanOutside = meanOutside
    }
    else {
        LOG("Ventilation Guru Error:  Could not obtain the hourly forecast values!")
    	state.forecastFailCntr = state.forecastFailCntr + 1
        return 0
    }
    
	LOG "Weather Underground: mean Outside: ${state.meanOutside}, hourly: ${outsideHourly[0..23]}"
}

def sanityCheckGreenhouseEffect() {
    if (state.greenhouseEffect < cAdj(1)) {
        state.greenhouseEffect = cAdj(1)
    } else if (state.greenhouseEffect > cAdj(5)) {
        state.greenhouseEffect = cAdj(5)
    }
}

def turnOffWHFsAndWindows() {
    turnOff(myWholeHouseFans, "wholeHouseFans", "Whole House Fans")
	turnOff(myWindowsSwitch, "windowsSwitch", "Windows")
}

def turnOffFans() {
    turnOff(myWholeHouseFans, "wholeHouseFans", "Whole House Fans")
	turnOff(myAtticFans, "atticFans", "Attic Fans")
    turnOff(myBasementFans, "basementFans", "Basement Fans")
	turnOff(myGarageFans, "garageFans", "Garage Fans")
}

def makeComparisons() {	
    LOG("Begin makeComparisons()")
    
    // shut everything off if we can't get the forecast or current conditions for too long
    if (state.currentConditionsFailCntr > 6 || state.forecastFailCntr > 4) {
    	sendNotificationEvent("Ventilation Guru Error:  Check internet connection.  Can not access Weather Underground!")
		turnOffWHFsAndWindows()
		turnOffFans()
    } else if (smokeDetected()) { 
    	// turn off all fans and thermostats if there's smoke
	    sendNotificationEvent("Ventilation Guru Warning:  Smoke Detected!")
		turnOffFans()
    	turnOff(myThermostats, "thermostats", "Thermostats")
	} else {
    	checkWholeHouseFansAndWindows()
		checkAtticFans()
        checkGarageFans()
        checkBasementFans()
    }
}

def checkWholeHouseFansAndWindows() {
    if (myWholeHouseFans || myWindowsSwitch) { 
    	// close windows/WHF if not in a selected mode, operation suspended, or we came home and the inside temp is outside the daytime comfort zone
        if (inWrongMode() || mySuspendOperation) {
			turnOffWHFsAndWindows()
        } else if (state.rain || state.curPrecip > 0.0) { // check precipitation
           	if (state.windowsSwitch == state.On || state.wholeHouseFans == state.On) {
	    		sendNotificationEvent("Ventilation Guru:  Windows closed/Whole House Fans turned off due to rain")
            }
			turnOffWHFsAndWindows()
	    } else if (state.skew > 0) { // day venting													
	        if (state.insideTemp >= state.target || state.insideTemp + cAdj(6) > state.outsideTemp || state.outsideTemp > myDaytimeMax) { // prevent thermostat with WHF operation
           		turnOff(myWholeHouseFans, "wholeHouseFans", "Whole House Fans")
               	// leave windows open longer than WHF to prevent window thrashing
	       		if (state.insideTemp >= state.target || state.insideTemp + cAdj(5) > state.outsideTemp || state.outsideTemp > myDaytimeMax) { // prevent thermostat with window open
    				turnOff(myWindowsSwitch, "windowsSwitch", "Windows")
               	}
	        }                                                                                                     
	        else if (state.insideTemp + cAdj(1) <= state.target && state.insideTemp + cAdj(6) < state.outsideTemp) {
	            openWindows()
                if (state.insideTemp + cAdj(7) < state.outsideTemp) {
    				turnOnWhfs()
                }
	        }
	    } else { // night venting
			if (state.insideTemp <= state.target || state.outsideTemp > myDaytimeMax || state.insideHeatIndex < state.outsideHeatIndex + cAdj(state.morningAdjustment - 2)) {
            	turnOff(myWholeHouseFans, "wholeHouseFans", "Whole House Fans")
                // leave windows open longer than WHF to prevent window thrashing and close windows after a few minutes in case we just went through the morning adjustment
				if (state.insideTemp + cAdj(1) <= state.target || state.outsideTemp > myDaytimeMax || state.insideHeatIndex < state.outsideHeatIndex + cAdj(state.morningAdjustment - 3)) {
	            	closeWindows()
                }
	   		} else if (state.insideTemp > state.target + cAdj(1) && state.insideHeatIndex > state.outsideHeatIndex + cAdj(state.morningAdjustment)) {
	            openWindows()
    			turnOnWhfs()
	        }
			LOG "Inside Temp: ${state.insideTemp} Outside Temp: ${state.outsideTemp}  Target: ${state.target}"
	   	 }
    }
}

def checkAtticFans() {
    	if (myAtticFans) {
        	// run attic fans all the time when in cooling mode unless WHF is on
    		if (state.skew < 0 && state.wholeHouseFans != state.On && state.insideTemp > state.meanRange + state.skew) {
            	turnOn(myAtticFans, "atticFans", "Attic Fans")
     		}
        	else {
            	turnOff(myAtticFans, "atticFans", "Attic Fans")
        	}
    	}  	
}

def checkGarageFans() {
    if (myGarageFans) {
        if (myGarageTempSensor) {
        	// turn on garage fan if cooling mode and outside is cooler than inside
            def garageTemp = myGarageTempSensor.temperature
            if (state.skew < -1 && garageTemp < state.outsideTemp) {
    			turnOn(myGarageFans, "garageFans", "Garage Fans")
            } else {
    			turnOff(myGarageFans, "garageFans", "Garage Fans")
            }
        } else {
            // turn on the garage fan if cooling is needed and night time, it's cool outside, or outside temp is dropping fast
			if (state.skew < 0 && state.insideTemp > state.meanRange + state.skew) { 
            	if (!state.daytime || state.outsideTemp < state.meanRange) {   
    				turnOn(myGarageFans, "garageFans", "Garage Fans")
                } else {
    				def myDate = new Date(now())
    				def nowString = myDate?.format("HH:mm", location?.timeZone)     
                	if (state.newOutsideTemp + cAdj(2) <= state.oldOutsideTemp && nowString > "16:00") {
    					turnOn(myGarageFans, "garageFans", "Garage Fans")
                    } else {
    					turnOff(myGarageFans, "garageFans", "Garage Fans")
                    }
                }
    		} else {
    			turnOff(myGarageFans, "garageFans", "Garage Fans")
    		}
    	}
    }
}

def checkBasementFans() {
    if (myBasementFans) {
    	if (state.skew > 0 && state.outsideTemp >= state.insideTemp && state.insideTemp < state.meanRange + state.skew) {
     		turnOn(myBasementFans, "basementFans", "Basement Fans")
		} else {
     		turnOff(myBasementFans, "basementFans", "Basement Fans")
        }
    } 
}


def LOG(String text) {
	if (state.debugMode) {
    	log.debug(text)
    }
}
