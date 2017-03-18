/**
 *  Temperature Based Device Control
 *
 *  Copyright 2017 Jason Schollenberger
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
	name: "Temperature Based Device Control",
	namespace: "jschollenberger",
	author: "Jason Schollenberger",
	description: "Turn on and off devices based on outside temperature. No sensors required.",
	category: "Convenience",
	iconUrl: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
	iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png",
	iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@3x.png")

preferences {
	section("Device Configuration") {
		input (name: "mechanism", type: "enum", required: true, title: "Turn on or off at low temperature?", metadata: [values: ["Off", "On"]])
		input (name:"switches", type: "capability.switch", required: true, multiple:true)
	}
	
	section("Temperature Configuation") {
		input(name: "minTemp", type: "number", required: true, title: "Low Temperature (F*)")
		input(name: "maxTemp", type: "number", required: true, title: "High Temperature (F*)")
	}
	
	section("Location") {
		input(name: "zipCode", type: "text", range:"(00001..99999)", required: false, title: "Zip Code for Weather (leave blank to autodetect)")
		input (name: "pollRate", type: "enum", required: false, title: "Update Weather Every (1 hour default)", defaultValue: "5", metadata: [values: ["5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]])
	}
	
	section("Extras") {
	}
}

def installed() {
	log.debug "Installed with settings: $settings"
	initialize()
}

def updated() {
	log.debug "Updated with settings: $settings"

	unsubscribe()
	initialize()
}

def initialize() {
	checkTemperature()

	switch (pollRate) {
		case "5 Minutes":
			runEvery5Minutes(checkTemperature)
			break
		case "10 Minutes":
			runEvery10Minutes(checkTemperature)
			break
		case "15 Minutes":
			runEvery15Minutes(checkTemperature)
			break
		case "30 Minutes":
			runEvery30Minutes(checkTemperature)
			break
		case "3 Hours":
			runEvery3Hours(checkTemperature)
			break
		default: // "1 Hour":
			runEvery1Hour(checkTemperature)
	}
	
	log.debug "Temperature Based Device Control: updating weather every $pollRate"
}

def checkTemperature() {
	def currTemp = getCurrTemp()

	if(mechanism == "On") {
		log.debug("Temperature Based Device Control: Using on at low temperatures")
		if (currTemp <= minTemp) {
			log.debug("Temperature Based Device Control: currTemp $currTemp is less than min on temp $minTemp. Ensuring devices are on.")
			deviceHandler("on")
		} else if (currTemp > maxTemp) {
			log.debug("Temperature Based Device Control: currTemp $currTemp is greater than min off temp $maxTemp. Ensuring devices are off.")
			deviceHandler("off")
		} else {
			log.debug("Temperature Based Device Control: currTemp $currTemp is between setpoints $minTemp and $maxTemp. Doing nothing.")
		}
	} else if(mechanism == "Off") {
		log.debug("Temperature Based Device Control: Using off at low temperatures")
		if (currTemp <= minTemp) {
			log.debug("Temperature Based Device Control: currTemp $currTemp is less than min off temp $minTemp. Ensuring devices are off.")
			deviceHandler("off")
		} else if (currTemp > maxTemp) {
			log.debug("Temperature Based Device Control: currTemp $currTemp is greater than min on temp $maxTemp. Ensuring devices are on.")
			deviceHandler("on")
		} else {
			log.debug("Temperature Based Device Control: currTemp $currTemp is between setpoints $minTemp and $maxTemp. Doing nothing.")
		}
	}
}

def getCurrTemp() {
	def weatherData
	def currTemp

	if(zipCodeIsValid()) {
		log.debug("Temperature Based Device Control: using zip to update weather")
		weatherData = getWeatherFeature("conditions", zipCode)
	} else {
		log.debug("Temperature Based Device Control: using hub location to update weather")
		weatherData = getWeatherFeature("conditions")
	}
	if (!weatherData) {
		log.debug( "Temperature Based Device Control: Unable to retrieve weather data." )
		return false
	} else {
		currTemp = weatherData.current_observation.temp_f
		log.debug( "Temperature Based Device Control: Current temperature is $currTemp*F")
		return currTemp
	}
}

def zipCodeIsValid() {
	zipCode && zipCode.isNumber() && zipCode.size() == 5
}

def deviceHandler(action) {
	log.debug "Temperature Based Device Control: Ensuring $action.value for ${switches}"

	if (action == "on") {
		switches.each {
			n->if (n.currentValue("switch")=="off") {
				n.on()
				sendNotificationEvent("I've turned on the $n")
			}
		}
	} else if (action == "off") {
		switches.each {
			n->if (n.currentValue("switch")=="on") {
				n.off()
				sendNotificationEvent("I've turned off the $n")
			}
		}
	}
}
