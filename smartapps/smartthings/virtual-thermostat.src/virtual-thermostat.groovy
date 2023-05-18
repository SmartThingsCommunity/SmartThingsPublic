/**
 *  Copyright 2015 SmartThings
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
 *  Virtual Thermostat
 *
 *  Author: SmartThings
 */
definition(
    name: "Virtual Thermostat",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Control a space heater or window air conditioner in conjunction with any temperature sensor, like a SmartSense Multi.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",
    pausable: true
)

preferences {
	page(name: "mainPage", install: true, uninstall: true)
}

def mainPage() {
	dynamicPage(name: "mainPage", title: (state.installed ? "" : "Virtual Thermostat")) {
		section("Select 'heat' for a heater and 'cool' for an air conditioner..."){
			input "thermostatMode", "enum", title: "Heating or cooling?", options: ["heat","cool"], submitOnChange: true
		}
		if (thermostatMode) {
			section("Choose a temperature sensor... "){
				input "sensor", "capability.temperatureMeasurement", title: "Sensor", submitOnChange: true
			}
			if (sensor) {
				section("Select the ${(thermostatMode == "heat") ? "heater" : "air conditioner"} outlet(s)... "){
					input "outlets", "capability.outlet", title: "Outlets", multiple: true, submitOnChange: true
				}
			}
			if (thermostatMode && outlets) {
				section("Set the desired ${(thermostatMode == "heat") ? "heating" : "cooling"} temperature..."){
					input "setpoint", "decimal", title: "${(thermostatMode == "heat") ? "Heating" : "Cooling"} setpoint", submitOnChange: true
				}
				section("When there's been movement from (optional, leave blank to not require motion)..."){
					input "motion", "capability.motionSensor", title: "Motion", required: false, submitOnChange: true
				}
			}
			if (motion) {
				section("Within this number of minutes of motion keep the ${(thermostatMode == "heat") ? "heater" : "air conditioner"} on..."){
					input "minutes", "number", title: "Minutes", required: false
				}
				section("Never ${(thermostatMode == "heat") ? "go below" : "exceed"} this temperature, with or without motion..."){
					input "emergencySetpoint", "decimal", title: "${(thermostatMode == "heat") ? "Min" : "Max"} allowed temperature", required: false
				}
			}
			if (setpoint) {
				section([mobileOnly:true]) {
					label title: "Assign a name", required: false
				}
			}
		}
	}
}

def installed()
{
	state.installed = true
	subscribe(sensor, "temperature", temperatureHandler)
	if (motion) {
		subscribe(motion, "motion", motionHandler)
	}
}

def updated()
{
	unsubscribe()
	if (outlets) {
		subscribe(sensor, "temperature", temperatureHandler)
		if (motion) {
			subscribe(motion, "motion", motionHandler)
		}
	} else {
		sendNotificationEvent("\"${app.label}\" will no longer function as the outlet(s) has been removed")
	}
}

def temperatureHandler(evt)
{
	def isActive = hasBeenRecentMotion()
	if (isActive || emergencySetpoint) {
		evaluate(evt.doubleValue, isActive ? setpoint : emergencySetpoint)
	}
	else {
		outlets.off()
	}
}

def motionHandler(evt)
{
	if (evt.value == "active") {
		def lastTemp = sensor.currentTemperature
		if (lastTemp != null) {
			evaluate(lastTemp, setpoint)
		}
	} else if (evt.value == "inactive") {
		def isActive = hasBeenRecentMotion()
		log.debug "INACTIVE($isActive)"
		if (isActive || emergencySetpoint) {
			def lastTemp = sensor.currentTemperature
			if (lastTemp != null) {
				evaluate(lastTemp, isActive ? setpoint : emergencySetpoint)
			}
		}
		else {
			outlets.off()
		}
	}
}

private evaluate(currentTemp, desiredTemp)
{
	log.debug "EVALUATE($currentTemp, $desiredTemp)"
	def threshold = 1.0
	if (thermostatMode == "cool") {
		// air conditioner
		if (currentTemp - desiredTemp >= threshold) {
			outlets.on()
		}
		else if (desiredTemp - currentTemp >= threshold) {
			outlets.off()
		}
	}
	else {
		// heater
		if (desiredTemp - currentTemp >= threshold) {
			outlets.on()
		}
		else if (currentTemp - desiredTemp >= threshold) {
			outlets.off()
		}
	}
}

private hasBeenRecentMotion()
{
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
	}
	else {
		isActive = true
	}
	isActive
}

