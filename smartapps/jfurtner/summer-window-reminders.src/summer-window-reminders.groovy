/**
 *  Summer window reminders
 *
 *  Copyright 2017 Jamie Furtner
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
import groovy.time.TimeCategory

definition(
    name: "Summer window reminders",
    namespace: "jfurtner",
    author: "Jamie Furtner",
    description: "Remind to open windows when outside temp&lt;inside, close when inside&lt;outside.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home9-icn@3x.png"
    )


preferences {
	section('Options') {	
        input 'modes', 'mode', title: 'Run when mode is', required: false, multiple: true
    	input 'people', 
        	'capability.presenceSensor', 
        	title: 'Send push notification when any of these people present', 
            multiple: true, 
            required: true
        //mode(name: 'modesToRunIn', title: 'Select mode(s) in which to execute', multiple: true, required: true)
        input 'hoursBetweenUpdates', 'number', 
        	title: 'Number of hours between updates', 
            defaultValue:1,
            range: '0..23',
            required: true
    }
	section("Outside") {
		input "outsideTemperature", "capability.temperatureMeasurement", title: 'Select exterior temperature sensor', required: true
	}
    section("Inside") {
    	input "insideTemperature", "capability.temperatureMeasurement", title: 'Select interior temperature sensor', required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug 'smartapp updated'
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	log.debug 'smartapp init'
	runEvery1Minute(checkTemperature)
    if (state.lastNotificationOutLTIn == null) {
    	state.lastNotificationOutLTIn = initDate()
    }
    if (state.lastNotificationInLTOut == null) {
    	state.lastNotificationInLTOut = initDate()
    }
}

def initDate() {
	return now() - 86400000
}

def inValidMode() {
	Boolean inValidMode = false
    String curModeName = location.currentMode.name
    for (m in modes) {
    	if (m == curModeName) {
        	inValidMode = true
            break
        }
    }
    if (!inValidMode)
    {
    	log.debug "Mode '${curModeName}' is not a valid mode. Valid modes:${modes}"
        return false
    }
    
    return true
}

def validPersonPresent() {
    for (person in people) {
        if (person.currentPresence == "present") {
        	log.debug "${person} present"
        	return true
        }
    }
    if (!anyonePresent)
    {
        log.debug 'Not sending alert: no people present'
        return false
    }
}

def checkTemperature(evt) {
	if (!inValidMode()) {
    	return
    }
    if (!validPersonPresent) {
    	return
    }
    log.debug 'Initialization'
    def msg = ''
    def outside = outsideTemperature.currentTemperature
    def inside = insideTemperature.currentTemperature
    def last = 0
    def lastStr = ''
    def nowDate = now()
    log.debug "Checking temperatures: o:${outside} i:${inside} n:${nowDate}"
    if (outside < inside)
    {
        log.debug 'Outside < inside'
        last = state.lastNotificationOutLTIn
        msg = "Open windows, outside temperature (${outside}) lower than inside (${inside})"
    }
    else if (inside < outside)
    {    
        log.debug 'Inside < outside'
        last = state.lastNotificationInLTOut
        msg = "Close windows, outside temperature (${outside}) higher than inside (${inside})"

    }

    log.debug "tests complete: m:${msg} l:${last}"

    def addHours = hoursBetweenUpdates*3600000
    log.debug "Adding seconds: ${addHours}"
    last = last + addHours
    log.debug "times: l:${last} n:${nowDate}"
    if (msg != null && nowDate >= last)
    {
    	if (outside < inside)
        {
            state.lastNotificationOutLTIn = nowDate
            state.lastNotificationInLTOut = initDate()
        }
        else if (inside < outside)
        {
            state.lastNotificationInLTOut = nowDate
            state.lastNotificationOutLTIn = initDate()
        }
        log.debug 'Sending message'
        sendPushMessage(msg)
    }
}