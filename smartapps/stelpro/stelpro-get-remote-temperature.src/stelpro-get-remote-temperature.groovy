/**
 *  Copyright 2015 Stelpro
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
 *  Get Remote Temperature
 *
 *  Author: Stelpro
 */

definition(
	name: "Stelpro Get Remote Temperature",
	namespace: "stelpro",
	author: "Stelpro",
	description: "Retrieves the temperature from a sensor and sends it to a specific Stelpro thermostat.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences() {
	section("Choose remote device to read temperature from... ") {
		input "sensor", "capability.temperatureMeasurement", title: "Select a remote temperature reading device", required: true
	}
	section("Choose the Stelpro thermostats that will receive the remote device's temperature... ") {
		input "thermostats", "capability.thermostat", title: "Select Stelpro Thermostats", multiple: true, required: true
	}
}

def installed()
{
	subscribe(sensor, "temperature", temperatureHandler)
	log.debug "enter installed, state: $state"
}

def updated()
{
	log.debug "enter updated, state: $state"
	unsubscribe()
	subscribe(sensor, "temperature", temperatureHandler)
}

def temperatureHandler(event)
{
	log.debug "temperature received from remote device: ${event?.value}"
	if (event?.value) {
		thermostats?.setOutdoorTemperature(event.value)
	}
}
