/**
 *  Pipe Freeze Preventer
 *
 *  Copyright 2015 Simon Labrecque
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
 */
definition(
    name: "Pipe Freeze Preventer",
    namespace: "simon-labrecque",
    author: "Simon Labrecque",
    description: "Pipe Freeze Preventer 'turns on' thermostats, by setting them to a configured heating setpoint, when it's been more than X minutes that they've been off. Used to make sure that hot water circulates in the pipes on a controlled schedule to prevent pipes freezing.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("About") {
        paragraph textAbout()
    }
    
    section("Schedule settings")
    {
    	input "everyXMinutes", "number", title: "Schedule to turn on at least every X minutes", defaultValue: "180"
        input "leaveOnForXMinutes", "number", title: "Leave on for X minutes", defaultValue: "5"
        input "minOutsideTemp", "text", title: "Outside temperature needs to be X or less for the schedule to run", defaultValue: "-10"
    }
    
	
    section("Thermostats settings"){
    	input "thermostats", "capability.thermostat", multiple: true, title: "Select Thermostats to monitor and control"
		input "setTemperatureToThisToTurnOn", "number", title: "Heating setpoint used to 'turn on' the thermostat", defaultValue: "40"
	}
	
    section("Settings to use for Open Weather Map API (used to get outside temperature)"){
    	input "cityID", "text", title: "City ID", defaultValue: ""
        input "apikey", "text", title: "API Key", defaultValue: ""
    }
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(thermostats, "thermostatOperatingState", thermostatChange)

	schedule("37 * * * * ?", "scheduleCheck")
    state.currentlyUnfreezing = false
    state.lastOnTime = now() - ((everyXMinutes) * 60 * 1000)
    
    subscribe(app, onAppTouch)
}

def thermostatChange(evt) {
	log.debug "thermostatChange: $evt.name: $evt.value"
    
    if(evt.value == "heating") {
    	state.lastOnTime = now()
    }
    
    log.debug "state: " + state.lastOnTime
}

def scheduleCheck() {
	log.debug "schedule check, lastOnTime = ${state.lastOnTime}, currentlyUnfreezing = ${state.currentlyUnfreezing}"

	if(state.currentlyUnfreezing == false)
    {
    	log.debug "scheduleCheck: calling checkIfNeedToUnfreeze"
		checkIfNeedToUnfreeze()
    }
    else
    {
    	log.debug "scheduleCheck: calling checkIfNeedToReturnToNormal"
       	checkIfNeedToReturnToNormal()
    }
}

def checkIfNeedToReturnToNormal()
{
   	def unfreezingForInMinutes = ((now() - state.unfreezingSince) / 1000) / 60
    log.debug "checkIfNeedToReturnToNormal: we've been unfreezing for " + unfreezingForInMinutes + " minutes"
   	if(unfreezingForInMinutes > leaveOnForXMinutes)
   	{
    	stopUnfreezing()
   	}
    else
    {
    	log.debug "checkIfNeedToReturnToNormal: continuing unfreezing"
    }
}

def stopUnfreezing() {
	log.debug "stopUnfreezing: setting back thermostats to their original heatingSetPoint"
    for (int i = 0; i < thermostats.size(); i++) {
        thermostats[i].setHeatingSetpoint(state.tstatHeatingSetpointBackup[i])
    }

    state.currentlyUnfreezing = false;
    state.lastOnTime = now()
}

def checkIfNeedToUnfreeze()
{
	def currentOutsideTemperature = getCurrentOutsideTemperature()
    BigDecimal minTemperatureDecimal = new BigDecimal(minOutsideTemp)
    log.debug "checkIfNeedToUnfreeze: current oustide temperature is " + currentOutsideTemperature + "C, min temperature to run is " + minTemperatureDecimal
    
	if(currentOutsideTemperature > minTemperatureDecimal)
    {
    	log.debug "checkIfNeedToUnfreeze: no need to unfreeze since outside temperature is more than " + minOutsideTemp
        return
    }
    
    for (tstat in thermostats) {
        if(tstat.currentTemperature < tstat.currentHeatingSetpoint)
        {
            //We suppose that the thermostatOperatingState is heating even tough it wasn't reported
            log.debug "checkIfNeedToUnfreeze: assuming that thermostat '" + tstat.label + "' is on since temperature is " + tstat.currentTemperature + " and setpoint is " + tstat.currentHeatingSetpoint
            state.lastOnTime = now()
        }
    }

    def minutesSinceLastOnTime = ((now() - state.lastOnTime) / 1000) / 60
    log.debug "checkIfNeedToUnfreeze: " + minutesSinceLastOnTime + " minutes since our last unfreeze"

    if(minutesSinceLastOnTime < everyXMinutes)
    {
        log.debug "checkIfNeedToUnfreeze: not turning on because we haven't reached " + everyXMinutes + " minutes yet"
        return
    }

    //It's been more than everyXMinutes, so turning on thermostats
	startUnfreezing()
}

def startUnfreezing() {
	log.debug "startUnfreezing: starting unfreezing for " + leaveOnForXMinutes + " minutes"

    state.currentlyUnfreezing = true
    state.unfreezingSince = now()
    state.tstatHeatingSetpointBackup = []

    for (int i = 0; i < thermostats.size(); i++) {
        log.debug "startUnfreezing: setting new heatingSetPoint for tstat " + thermostats[i].label
        state.tstatHeatingSetpointBackup.add(thermostats[i].currentHeatingSetpoint)
        thermostats[i].setHeatingSetpoint(setTemperatureToThisToTurnOn)
    }

    log.debug "startUnfreezing: turned on thermostats by setting temp to " + setTemperatureToThisToTurnOn
    log.debug "startUnfreezing: tstat heatpoint backup: " + state.tstatHeatingSetpointBackup
    log.debug "startUnfreezing: state.currentlyUnfreezing="+state.currentlyUnfreezing
}

BigDecimal getCurrentOutsideTemperature() {
    def params = [
        uri:  'http://api.openweathermap.org/data/2.5/',
        path: 'weather',
        contentType: 'application/json',
        query: [mode: 'json', units: 'metric', APPID: apikey, id: cityID]
    ]
    
    def currentTemperature = 0G
    try {
        httpGet(params) {resp ->
            log.debug "resp data: ${resp.data}"
            currentTemperature = resp.data.main.temp
        }
    } catch (e) {
        log.error "error: $e"
    }
    
    return currentTemperature
}

private def textAbout() {
    return '''\
Pipe Freeze Preventer 'turns on' thermostats, by setting them to a configured heating setpoint, \
when it's been more than X minutes that they've been off. Used to make sure that hot water circulates \
in the pipes on a controlled schedule to prevent pipes freezing. \
'''
}

def onAppTouch(event) {
    log.debug "onAppTouch: currentlyUnfreezing: ${state.currentlyUnfreezing} lastOnTime:${state.lastOnTime}"


	if(state.currentlyUnfreezing == false)
    {
    	log.debug "onAppTouch: calling startUnfreezing()"
       	startUnfreezing()
    }
    else
    {
    	log.debug "onAppTouch: calling stopUnfreezing()"
       	stopUnfreezing()
    }
}