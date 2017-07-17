/**
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
 *  Name Presence Thermostat
 *  Based on code from: Virtual Thermostat
 *
 *  Author: Brendan Hoffmann
 */
definition(
    name: "Presence Thermostat",
    namespace: "happybuzzcut",
    author: "Brendan Hoffmann",
    description: "If a device with a presence sensor is around, allow cooling/heating to take place",
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
	}
	section("Set the desired temperature..."){
		input "setpoint", "decimal", title: "Set Temp"
	}
	section("Choose the person/device that must be present for item to power on..."){
		input "presence", "capability.presenceSensor", title: "Presence", required: false
	}
	section("Choose whether you wish to do cooling or heating."){
		input "mode", "enum", title: "Heating or cooling?", options: ["cool": "Cooling","heat": "Heating"]
	}
}

def installed()
{
	subscribe(sensor, "temperature", temperatureHandler)
	subscribe(presence, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(sensor, "temperature", temperatureHandler)
	subscribe(presence, "presence", presenceHandler)
}

def temperatureHandler(evt)
{
	if (presence.currentPresence == "present") {
		evaluate(evt.doubleValue,setpoint)
	}
	else {
		outlets.off()
	}
}

def presenceHandler(evt)
{
	if (evt.value == "present") {
		def lastTemp = sensor.currentTemperature
		if (lastTemp != null) {
			evaluate(lastTemp, setpoint)
		}
	} else {
			outlets.off()
		}
}

private evaluate(currentTemp, desiredTemp)
{
	log.debug "EVALUATE($currentTemp, $desiredTemp) $mode"
	def threshold = 1.0
	if (mode == "cool") {
		// air conditioner
		if (currentTemp - desiredTemp >= threshold) {
        log.debug "Cooling On"
			outlets.on()
		}
		else if (desiredTemp - currentTemp >= threshold) {
        log.debug "Cooling off"
			outlets.off()
		}
	}
	else {
		// heater
		if (desiredTemp - currentTemp >= threshold) {
        log.debug "Heating On"
			outlets.on()
		}
		else if (currentTemp - desiredTemp >= threshold) {
        log.debug "Heating Off"
			outlets.off()
		}
	}
}
