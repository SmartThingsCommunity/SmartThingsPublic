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
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Choose a temperature sensor... "){
		input "sensor", "capability.temperatureMeasurement", title: "Sensor"
	}
	section("Select the heater or air conditioner outlet(s)... "){
		input "outlets", "capability.switch", title: "Outlets", multiple: true
		input "enforce", "bool", title: "Enforce Outlet State", required: true, defaultValue: false
	}
	section("Set the desired temperature..."){
		input "setpoint", "decimal", title: "Set Temp"
	}
	section("When there's been movement from (optional, leave blank to not require motion)..."){
		input "motion", "capability.motionSensor", title: "Motion", required: false
	}
	section("Within this number of minutes..."){
		input "minutes", "number", title: "Minutes", required: false
	}
	section("But never go below (or above if A/C) this value with or without motion..."){
		input "emergencySetpoint", "decimal", title: "Emer Temp", required: false
	}
	section("Select 'heat' for a heater and 'cool' for an air conditioner..."){
		input "mode", "enum", title: "Heating or cooling?", options: ["heat","cool"]
	}
}

def installed()
{
	state.lastTemp = null
	subscribe(sensor, "temperature", temperatureHandler)
	if (motion) {
		subscribe(motion, "motion", motionHandler)
	}
}

def updated()
{
	unsubscribe()
	state.lastTemp = null // addresses bug where updated values do not take effect
	subscribe(sensor, "temperature", temperatureHandler)
	if (motion) {
		subscribe(motion, "motion", motionHandler)
	}
}

def temperatureHandler(evt)
{
	def isActive = hasBeenRecentMotion()
	if (isActive || emergencySetpoint) {
		evaluate(evt.doubleValue, state.lastTemp, isActive ? setpoint : emergencySetpoint)
        state.lastTemp = evt.doubleValue
	}
	else {
		outlets.off()
	}
}

def motionHandler(evt)
{
	if (evt.value == "active") {
		def thisTemp = sensor.currentTemperature
		if (thisTemp != null) {
			evaluate(thisTemp, state.lastTemp, setpoint)
			state.lastTemp = thisTemp
		}
	} else if (evt.value == "inactive") {
		def isActive = hasBeenRecentMotion()
		log.debug "INACTIVE($isActive)"
		if (isActive || emergencySetpoint) {
			def thisTemp = sensor.currentTemperature
			if (lastTemp != null) {
				evaluate(thisTemp, state.lastTemp, isActive ? setpoint : emergencySetpoint)
				state.lastTemp = thisTemp
			}
		}
		else {
			outlets.off()
		}
	}
}

private evaluate(currentTemp, lastTemp, desiredTemp)
{
	def threshold = 1.0
	log.debug "EVALUATE($currentTemp, $lastTemp, $desiredTemp)"
	if (mode == "cool") {
		// air conditioner
		if ( (currentTemp - desiredTemp >= threshold) &&  ( (lastTemp <= desiredTemp) || (lastTemp == null) || enforce) ) {
			outlets.on()
		}
		if ( (desiredTemp - currentTemp >= threshold) &&  ( (lastTemp >= desiredTemp) || (lastTemp == null) || enforce) ) {
			outlets.off()
		}
	}
	else {
		// heater
		if ( (desiredTemp - currentTemp >= threshold) &&  ( (lastTemp >= desiredTemp) || (lastTemp == null) || enforce) ) {
			outlets.on()
		}
		if ( (currentTemp - desiredTemp >= threshold) &&  ( (lastTemp <= desiredTemp) || (lastTemp == null) || enforce) ) {
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
