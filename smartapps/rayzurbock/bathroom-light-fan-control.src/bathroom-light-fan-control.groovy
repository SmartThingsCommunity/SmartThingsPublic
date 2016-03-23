/**
 *  Bathroom Light/Fan Control
 *  WARNING: This version is in testing
 *
 *  Author: Brian Lowrance  rayzur [at] rayzurbock.com
 *  Donations Accepted via PayPal:  rayzur [at] rayzurbock.com
 * 
 *  The latest version of this file can be found on GitHub at:
 *  http://github.com/rayzurbock/SmartThings-BathroomLightControl
 * 
 *  Version 1.0.2-Beta6 (2014-11-29)
 *  Controls bathroom lights:
 *  -- Turn on light when motion is detected.
 *  -- Turn off bathroom light after x minutes of no motion detection
 *  -- Extend off timer to x minutes if humidity sensor is above the average for the room and trending upward (user in the shower?)
 *  -- Optionally turn on exhaust fan (switch) when humidity is above the average for the room and turn off when it is equal to or below the average.
 *
 *  Developed / Tested with Aeon Multisensor which by default sends humidty every 8 minutes.
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2014 Rayzurbock.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *
 *  WARNING: This version is in testing
 
 */definition(
    name: "Bathroom Light/Fan Control",
    namespace: "rayzurbock",
    author: "brian@rayzurbock.com",
    description: "Control light in the bathroom based on motion, extend when humidty rises (Shower in use); optionally turn on exhaust fan",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "appStatus")
    page(name: "appConfigure")
}


def appStatus() {
    dynamicPage(name: "appStatus", title: "Status - $app.label"){ 
	    section{
	        if (state.installed){
                if (state.humiditytrend == "up"){ paragraph "Humidity is ${state.lastHumidity}% and rising. ${state.lastHumidityTimeStamp}" }
                if (state.humiditytrend == "down"){ paragraph "Humidity is ${state.lastHumidity}% and falling. ${state.lastHumidityTimeStamp}" }
                if (state.humiditytrend == "stay"){ paragraph "Humidity is ${state.lastHumidity}%. ${state.lastHumidityTimeStamp}" }
		        paragraph "Light is currently ${lightswitch.latestValue('switch')}. ${state.lastSwitchTimeStamp}"
                if (!(fanswitch == null)) {
                    paragraph "Fan is currently ${fanswitch.latestValue('switch')}. ${state.lastFanTimeStamp}"
                }
                paragraph "Using ${state.mode} based timer."
		    } else {
                paragraph "Not configured, please configure."
                paragraph ""
            }
            href "appConfigure", title:"Configure", description:"Tap to open"
            paragraph ""
            if (!(state.appversion == null)) {
                paragraph "Bathroom Light/Fan Control ${state.appversion}"
            } else {
                paragraph "Bathroom Light/Fan Control"
            }
		    paragraph "http://github.com/rayzurbock"
        }
    }
}


def appConfigure(){
    dynamicPage(name: "appConfigure", title: "Configure - $app.label", install:true, uninstall:state.installed){
        section{
	        def inputoffaftermotion = [
                name:	"motionOff",
                type:	"number",
                title:  "Turn off after motion is inactive (minutes)",
                defaultValue:	5,
                required:	true
            ]
            def inputoffafterhumidity = [
                name:	"humidityOff",
                type:	"number",
                title:  "Turn off after motion is inactive and humidity is higher than average (minutes)",
                defaultValue:	10,
                required: true
            ]
            def inputpollsforhumidity = [
                name:	"humiditypollsforaverage",
                type:	"number",
                title:  "How many humidity sensor readings should determine the average/normal humidity for the room?",
                defaultValue:	4,
                required:	true
            ]
            input "lightswitch", "capability.switch", title: "Light Switch", required: true
            input "motionSensor", "capability.motionSensor", title: "Motion Sensor(s)", required: true
            input "humiditySensor", "capability.relativeHumidityMeasurement", title: "Humidity Sensor(s)", required: true
            input "fanswitch", "capability.switch", title: "Fan Switch (optional)", required: false
            input inputoffaftermotion
            input inputoffafterhumidity
            input inputpollsforhumidity
            if (!(state.appversion == null)) {
                paragraph "Bathroom Light/Fan Control ${state.appversion}"
            } else {
                paragraph "Bathroom Light/Fan Control"
            }
            paragraph "http://github.com/rayzurbock"
        }
        section([mobileOnly:true]){
            label title: "Bathroom name", required: true
            mode title: "Set for specific mode(s)", required: false
        }
    }
}


def installed() {
    initialize()
    if (state.loglevel > 0){TRACE("Installed with settings: ${settings}")}
}


def updated() {
    unsubscribe()
    initialize()
    if (state.loglevel > 0){TRACE("Updated with settings: ${settings}")}
}


def initialize() {
    state.appversion = "1.0.2-Beta5"
    state.loglevel = 2 //0 = off, 1 = on, 2 = debug
    //Subscribe to device events
    subscribe(lightswitch, "switch", SwitchEvent)
    subscribe(motionSensor, "motion", MotionEvent)
    subscribe(humiditySensor, "humidity", HumidityEvent)
    subscribe(fanswitch, "switch", FanEvent)
    //Init state variables
    state.averagehumiditymaxpollcount = settings.humiditypollsforaverage //How many polls should we base our average off of?
    if (state.averagehumiditycurrentpollcount == null) { state.averagehumiditycurrentpollcount = 0 }
    if (state.averageHumidity == null) { 
        if (humiditySensor.latestValue("humidity") == null) { 
            state.averageHumidity = 0 
        } else {
            state.averageHumidity = humiditySensor.latestValue("humidity")
        }
    }
    if (state.lastHumidity == null) { 
        if (humiditySensor.latestValue("humidity") == null) { 
            state.lastHumidity = 0 
        } else {
            state.lastHumidity = humiditySensor.latestValue("humidity")
        }
    }
    if (state.humiditytrend == null) { state.humiditytrend = "stay" } //Trend = stay, down, or up; App starts with "stay"
    if (state.mode == null) { state.mode = "motion" }
    if (state.trigger == null) { state.trigger = "none" }
    if (state.offtime == null) { state.offtime = settings.motionOff * 60 }
    if (state.scheduled == null) { state.scheduled = false }
    if (state.lightswitchlastValue == null) { state.lightswitchlastValue = lightswitch.latestValue('switch') }
    if (state.lastHumidityTimeStamp == null) { state.lastHumidityTimeStamp = "" }
    if (state.lastSwitchTimeStamp == null) { state.lastSwitchTimeStamp = "" }
    if (state.lastMotionTimeStamp == null) { state.lastMotionTimeStamp = "" }
    if (state.lastFanTimeStamp == null) { state.lastFanTimeStamp = "" }
    state.installed = true
    if (state.loglevel == 2){DEBUG("Initialized")}
}


def SwitchEvent(evt) {
    state.lastSwitchTimeStamp = "(" + timeStampMMDDHHmm() + ")"
    if (!(fanswitch == null)) {
        //If Humidity is high and fan is configured, turn fan on, back off when it reaches the average
        if (state.lastHumidity > state.averageHumidity){
            if (fanswitch.latestValue("switch") == "off"){fanOn()}
        } else {
            if (fanswitch.latestValue("switch") == "on"){fanOff()}
        }
    }
    //DEBUG("DEBUG: Switch | Current: ${evt.value}, LastValue: ${state.lightswitchlastValue}")
    if (evt.value == "on" && state.lightswitchlastValue == "off") {
        if (state.trigger == "none") { 
            state.trigger = "manual" 
        }
        if (state.loglevel == 2){DEBUG("Switch | ${evt.value}, trigger:${state.trigger}, mode: ${state.mode}, lasthumidity:${state.lastHumidity}, avghumidity:${state.averageHumidity}, trend:${state.humiditytrend}")}
        if ((state.lastHumidity > state.averageHumidity && state.humiditytrend == "up") && state.mode == "motion" && state.trigger == "manual") {
            if (state.loglevel == 2){DEBUG("Switch | Using humidity off timer (1)")}
            unschedule()
            state.mode = "humidity"
            state.offtime = settings.humidityOff * 60
            runIn(state.offtime, "lightsOut")
            state.scheduled = true
        } else {
            if (((state.lastHumidity <= state.averageHumidity) || (state.humiditytrend == "down")) && (!(state.humiditytrend == "stay")) && (state.mode == "motion") && (state.trigger == "manual")) {
                //Light came on, but humidity is below the threshold, set off time to motion off timer.
                if (state.loglevel == 2){DEBUG("Switch | Using motion off timer (1)")}
                unschedule()
                state.mode = "motion"
                state.offtime = settings.motionOff * 60
                runIn(state.offtime, "lightsOut")
                state.scheduled = true
            }
            if (state.humiditytrend == "stay" && state.trigger == "manual") {
                if (state.mode == "motion") {
                    //Light came on, but humidity is below the threshold, set off time to motion off timer.
                    if (state.loglevel == 2){DEBUG("Switch | Using motion off timer (2)")}
                    unschedule()
                    state.offtime = settings.motionOff * 60
                    runIn(state.offtime, "lightsOut")
                    state.scheduled = true
                }
                if (state.mode == "humidity") {
                    if (state.loglevel == 2){DEBUG("Switch | Using humidity off timer (2)")}
                    unschedule()
                    state.offtime = settings.humidityOff * 60
                    runIn(state.offtime, "lightsOut")
                    state.scheduled = true
                }
            }
        }
        if ((state.lastHumidity > state.averageHumidity && state.humiditytrend == "up") && (state.mode == "humidity") && state.trigger == "manual") {
            if (state.loglevel == 2){DEBUG("Switch | Using humidity off timer (3)")}
            unschedule()
            state.mode = "humidity"
            state.offtime = settings.humidityOff * 60
            runIn(state.offtime, "lightsOut")
            state.scheduled = true
        }
        if ((state.lastHumidity <= state.averageHumidity || state.humiditytrend == "down") && !(state.humiditytrend == "stay") && (state.mode == "humidity") && state.trigger == "manual") {
            if (state.loglevel == 2){DEBUG("Switch | Using motion off timer (3)")}
            unschedule()
            state.mode = "motion"
            state.offtime = settings.motionOff * 60
            runIn(state.offtime, "lightsOut")
            state.scheduled = true
        }
    }    
    if (evt.value == "off") {
        if (state.loglevel == 2){DEBUG("Switch | Light was turned off, unscheduling off timer events")}
        if (state.scheduled) { unschedule() }
        state.trigger = "none"
    }
    state.lightswitchlastValue = lightswitch.latestValue('switch')
}


def MotionEvent(evt) {
    state.lastMotionTimeStamp = "(" + timeStampMMDDHHmm() + ")"
    if (state.loglevel == 2){DEBUG("Motion Sensor | ${evt.value}, LastTrigger:${state.trigger}")}
    if (evt.value == "inactive" && state.lastMotionStatus == "active") {
        //Motion is inactive
        if (state.loglevel == 2){DEBUG("Motion Sensor | Motion inactive triggered")}
        if (state.mode == "motion"){
            unschedule()
            state.offtime = settings.motionOff * 60
            if (state.loglevel == 2){DEBUG("Motion Sensor | Scheduling off based on motion timer")}
            runIn(state.offtime, "lightsOut")
            state.scheduled = true
        }
        if (state.mode == "humidity") {
            unschedule()
            state.offtime = settings.humidityOff * 60
            if (state.loglevel == 2){DEBUG("Motion Sensor | Scheduling off based on humidity timer")}
            runIn(state.offtime, "lightsOut")
            state.scheduled = true
        }
    } else {
        //Motion is active
        if (evt.value == "active" && state.lastMotionStatus == "inactive") {
            if (state.trigger == "none" || state.trigger == "manual") {
                if (state.loglevel == 2){DEBUG("Motion Sensor | Switching trigger from none/manual to motion")}
                state.trigger = "motion"
            }
            if (state.loglevel == 2){DEBUG("Motion Sensor | Motion active triggered")}
            if (lightswitch.latestValue("switch") == "off") { lightsOn() }
            if (state.scheduled) { unschedule() }
        }
    }
    state.lastMotionStatus = evt.value
}


def HumidityEvent(evt) {
    state.lastHumidityTimeStamp = "(" + timeStampMMDDHHmm() + ")"
    def currentHumidity = Double.parseDouble(evt.value.replace("%", ""))
    state.humiditytrend = "stay"
    if ((currentHumidity > state.lastHumidity) && (currentHumidity - state.lastHumidity > 1)) { state.humiditytrend = "up" }
    if ((currentHumidity < state.lastHumidity) && (state.lastHumidity - currentHumidity > 1)) { state.humiditytrend = "down" }
    if (!(state.averageHumidity > 0)) { state.humiditytrend = "stay" } //AverageHumidity hasn't been established yet; stay.
    if (state.loglevel == 2){DEBUG("Humidity Sensor | Current(${currentHumidity}),last(${state.lastHumidity}),trend(${state.humiditytrend})")}
    state.lastHumidity = currentHumidity
    //Start Check Humidity Average
    if (state.averagehumiditycurrentpollcount == state.averagehumiditymaxpollcount) {
        //Average the data
        state.averageHumidity = (state.averageHumidityCalc / state.averagehumiditymaxpollcount)
        if (state.loglevel == 2){DEBUG("Humidity Sensor | Average Humidity (${state.averagehumiditymaxpollcount} polls) = ${state.averageHumidity}, Trend: ${state.humiditytrend}")}
        state.averageHumidityCalc = 0
        state.averagehumiditycurrentpollcount = 0
    } else {
        //Collect more data
        if (state.averageHumidityCalc > 0) {
            state.averageHumidityCalc = (state.averageHumidityCalc + state.lastHumidity)
        } else {
            state.averageHumidityCalc = state.lastHumidity
            state.averageHumidity = state.lastHumidity
        }
        state.averagehumiditycurrentpollcount = state.averagehumiditycurrentpollcount + 1
        if (state.loglevel == 2){DEBUG("Humidity Sensor | Collected Humidity on poll # ${state.averagehumiditycurrentpollcount} of ${state.averagehumiditymaxpollcount}")}
        if (state.loglevel == 2){DEBUG("Humidity Sensor | Current Average Humidity is ${state.averageHumidityCalc / state.averagehumiditycurrentpollcount}, Trend: ${state.humiditytrend}")}
    }
    //End Check Humidity Average
    if (lightswitch.latestValue('switch') == "on") {
        if ((state.lastHumidity > state.averageHumidity && state.humiditytrend == "up") && state.mode == "motion") {
            if (state.loglevel > 0){TRACE("Humidity Sensor | Humidity > Threshold; adjusting to use humidity timer")}
            state.mode = "humidity"
            state.offtime = settings.humidityOff * 60
            if (state.loglevel == 2){
                DEBUG("Humidity Sensor | Scheduling off based on inactivity + humidity ${state.offtime}")
                sendNotificationEvent("BLC: ${app.label} switched to Humidity timer via humidity event")
            }
            unschedule()
            runIn(state.offtime, "lightsOut")
            state.scheduled = true
        }
        if ((state.lastHumidity <= state.averageHumidity || state.humiditytrend == "down") && state.mode == "humidity") {
       	    if (state.loglevel > 0){TRACE("Humidity Sensor | Humidity <= Threshold or trending down; adjusting to use motion timer")}
            state.mode = "motion"
            state.offtime = settings.motionOff * 60
            if (state.loglevel == 2){
                DEBUG("Humidity Sensor | Scheduling off based on inactivity ${state.offtime}")
                sendNotificationEvent("BLC: ${app.label} switched to Motion timer via humidity event")
            }
            unschedule()
            runIn(state.offtime, "lightsOut")
            state.scheduled = true
        }
    }
    if (!(fanswitch == null)) {
        //If Humidity is high and fan is configured, turn fan on, back off when it reaches the average
        if (state.lastHumidity > state.averageHumidity){
            if (fanswitch.latestValue("switch") == "off"){fanOn()}
        } else {
            if (fanswitch.latestValue("switch") == "on"){fanOff()}
        }
    }
}


def FanEvent(evt){
    state.lastFanTimeStamp = "(" + timeStampMMDDHHmm() + ")"
}


def lightsOn() {
	TRACE("BLC: lightsOn()")
	lightswitch.on()
}


def lightsOut() {
	TRACE("BLC: lightsOut()")
	lightswitch.off()
}


def fanOn(){
    TRACE("BLC: fanOn()")
    fanswitch.on()
}


def fanOff(){
    TRACE("BLC: fanOff()")
    fanswitch.off()
}


def TRACE(message){
  log.trace("BLC: ${message}")
}


def DEBUG(message){
  log.debug("BLC: ${message}")
}


def timeStampHHmm(){
  return (new Date(now())).format("HH:mm", location.timeZone)
}


def timeStampHHmmss(){
  return (new Date(now())).format("HH:mm:ss", location.timeZone)
}


def timeStampMMDDHHmm(){
  return (new Date(now())).format("MMM dd HH:mm", location.timeZone)
}