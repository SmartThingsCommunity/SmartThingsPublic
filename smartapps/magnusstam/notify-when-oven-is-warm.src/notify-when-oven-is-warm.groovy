/**
 *  Energy Meter Routine
 *
 *  Copyright 2018 Magnus Stam
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
 *  This is a very simple SmartApp with 2 purposes. 
 *    1) To send a notification when an old dumb oven is heated to the right temperature, and 
 *    2) to notify if you forget to turn the oven of.
 *  
 *  To use this app you must have a power meter connected to the oven, and the oven must have a built in thermostat.
 *  The SmartApp starts in a power off mode. When the oven is tured on, the power usage (above threshold) is detected and the SmartApp enters power on mode.
 *  When the power is dropping below the threshold, it is asumed that the thermostat is signaling that the oven has reached the required temperature and a notification is sent.
 *  To again enter the power off mode the oven must not consume power (over threshold) for a predetermined time (parameter can be set by user). 
 *  If the SmartApp has not entered the power off mode before a periode of time (user configurable) and the power is turned back on by the owen, a warning notification is sent to the user.
 */
definition(
    name: "Notify when oven is warm",
    namespace: "magnusstam",
    author: "Magnus Stam",
	description: "Send a notification when heater is turned of because the right temperature is reached.",
	category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png")

preferences
{
	page(name: "getPref")
}
	
def getPref()
{
    dynamicPage(name: "getPref", title: "Choose Meter, Threshold, and Routines", install:true, uninstall: true)
    {
        section
        {
            paragraph "Power meter"
            input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        }
        section
        {
            paragraph "Threshold and notification text"
            input(name: "threshold", type: "number", title: "Energy Meter Threshold...", required: true, description: "in either watts or kw.")
            input(name: "NotificationText", type: "string", title: "Send this notification when oven is at right temperature", required: true, description: null)
        }
        section
        {
            paragraph "Warn about forgotten oven"
            input(name: "maxOnTime", type: "number", title: "Minutes before warning", required: true, description: null)
            input(name: "WarningText", type: "string", title: "Send this notification when oven has been on too long", required: true, description: null)
        }
        section
        {
            input(name: "minutesWithoutPower", type: "number", title: "Minutes after last power off detected until power is considered off", required: true, description: null)
        }
    }
}

def installed()
{
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize()
{
	subscribe(meter, "power", powerMeterHandler)
    atomicState.isOn = false
    atomicState.onDetected = now()
    atomicState.offDetected = now()
    atomicState.isFirstTransition = true
}

def powerOffHandler()
{
    atomicState.isOn = false
    log.debug "oven is off"
 }

def powerMeterHandler(evt)
{
    def powerValue = evt.value as double
        if (!atomicState.lastPowerValue)
    {
        atomicState.lastPowerValue = powerValue
    }


    def lastPowerValue = atomicState.lastPowerValue as double
        atomicState.lastPowerValue = powerValue

    def thresholdValue = threshold as int
        if (powerValue < thresholdValue)
    {
        if (lastPowerValue > thresholdValue)
        {
            atomicState.offDetected = now()
            if (atomicState.isFirstTransition == true)
            {
                sendPush(NotificationText)
                atomicState.isFirstTransition = false
            }
        }
        def minutesTilPowerConsideredOff = minutesWithoutPower as int
            runIn(60*minutesTilPowerConsideredOff, powerOffHandler)
    }
    else
    {
        if (atomicState.isOn == false)
        {
            atomicState.isOn = true
            atomicState.isFirstTransition = true
            atomicState.onDetected = now()
            log.debug "oven is on"
        }
        else
        {
            def timeSinceOnDetected = (now() - atomicState.onDetected)/(1000*60) as int
                def max = maxOnTime as int
                    log.debug "Time since oven was turned on is ${timeSinceOnDetected} minutes, max is ${max} minutes"
                if (timeSinceOnDetected > max)
            {
                sendPush(WarningText)
            }
        }
    }
}
