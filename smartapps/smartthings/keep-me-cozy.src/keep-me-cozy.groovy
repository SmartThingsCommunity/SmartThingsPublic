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
 *  Keep Me Cozy
 *
 *  Author: SmartThings
 */
definition(
    name: "Keep Me Cozy",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Changes your thermostat settings automatically in response to a mode change.  Often used with Bon Voyage, Rise and Shine, and other Mode Magic SmartApps to automatically keep you comfortable while you're present and save you energy and money while you are away.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat"
	}
	section("Heat setting...") {
		input "heatingSetpoint", "number", title: "Degrees?"
	}
	section("Air conditioning setting..."){
		input "coolingSetpoint", "number", title: "Degrees?"
	}
}

def installed()
{
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostat, "temperature", temperatureHandler)
	subscribe(location, changedLocationMode)
	subscribe(app, appTouch)
}

def updated()
{
	unsubscribe()
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostat, "temperature", temperatureHandler)
	subscribe(location, changedLocationMode)
	subscribe(app, appTouch)
}

def heatingSetpointHandler(evt)
{
	log.debug "heatingSetpoint: $evt, $settings"
}

def coolingSetpointHandler(evt)
{
	log.debug "coolingSetpoint: $evt, $settings"
}

def temperatureHandler(evt)
{
	log.debug "currentTemperature: $evt, $settings"
}

def changedLocationMode(evt)
{
	log.debug "changedLocationMode: $evt, $settings"

	thermostat.setHeatingSetpoint(heatingSetpoint)
	thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
}

def appTouch(evt)
{
	log.debug "appTouch: $evt, $settings"

	thermostat.setHeatingSetpoint(heatingSetpoint)
	thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
}

// catchall
def event(evt)
{
	log.debug "value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
}
