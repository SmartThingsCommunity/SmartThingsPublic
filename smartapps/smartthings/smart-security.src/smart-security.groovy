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
 *  Smart Security
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */
definition(
    name: "Smart Security",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Alerts you when there are intruders but not when you just got up for a glass of water in the middle of the night",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-IsItSafe.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-IsItSafe@2x.png"
)

preferences {
	section("Sensors detecting an intruder") {
		input "intrusionMotions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
		input "intrusionContacts", "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
	}
	section("Sensors detecting residents") {
		input "residentMotions", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
	}
	section("Alarm settings and actions") {
		input "alarms", "capability.alarm", title: "Which Alarm(s)", multiple: true, required: false
        input "silent", "enum", options: ["Yes","No"], title: "Silent alarm only (Yes/No)"
		input "seconds", "number", title: "Delay in seconds before siren sounds"
		input "lights", "capability.switch", title: "Flash these lights (optional)", multiple: true, required: false
		input "newMode", "mode", title: "Change to this mode (optional)", required: false
	}
	section("Notify others (optional)") {
		input "textMessage", "text", title: "Send this message", multiple: false, required: false
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "To this phone", multiple: false, required: false
        }
	}
	section("Arm system when residents quiet for (default 3 minutes)") {
		input "residentsQuietThreshold", "number", title: "Time in minutes", required: false
	}
}

def installed() {
	log.debug "INSTALLED"
	subscribeToEvents()
	state.alarmActive = null
}

def updated() {
	log.debug "UPDATED"
	unsubscribe()
	subscribeToEvents()
	unschedule()
	state.alarmActive = null
	state.residentsAreUp = null
	state.lastIntruderMotion = null
	alarms?.off()
}

private subscribeToEvents()
{
	subscribe intrusionMotions, "motion", intruderMotion
	// subscribe residentMotions, "motion", residentMotion
	subscribe intrusionContacts, "contact", contact
	subscribe alarms, "alarm", alarm
	subscribe(app, appTouch)
}

private residentsHaveBeenQuiet()
{
	def threshold = ((residentsQuietThreshold != null && residentsQuietThreshold != "") ? residentsQuietThreshold : 3) * 60 * 1000
	def result = true
	def t0 = new Date(now() - threshold)
	for (sensor in residentMotions) {
		def recentStates = sensor.statesSince("motion", t0)
		if (recentStates.find{it.value == "active"}) {
			result = false
			break
		}
	}
	log.debug "residentsHaveBeenQuiet: $result"
	result
}

private intruderMotionInactive()
{
	def result = true
	for (sensor in intrusionMotions) {
		if (sensor.currentMotion == "active") {
			result = false
			break
		}
	}
	result
}

private isResidentMotionSensor(evt)
{
	residentMotions?.find{it.id == evt.deviceId} != null
}

def appTouch(evt)
{
	alarms?.off()
	state.alarmActive = false
}

// Here to handle old subscriptions
def motion(evt)
{
	if (isResidentMotionSensor(evt)) {
		log.debug "resident motion, $evt.name: $evt.value"
		residentMotion(evt)
	}
	else {
		log.debug "intruder motion, $evt.name: $evt.value"
		intruderMotion(evt)
	}
}

def intruderMotion(evt)
{
	if (evt.value == "active") {
		log.debug "motion by potential intruder, residentsAreUp: $state.residentsAreUp"
		if (!state.residentsAreUp) {
			log.trace "checking if residents have been quiet"
			if (residentsHaveBeenQuiet()) {
				log.trace "calling startAlarmSequence"
				startAlarmSequence()
			}
			else {
				log.trace "calling disarmIntrusionDetection"
				disarmIntrusionDetection()
			}
		}
	}
	state.lastIntruderMotion = now()
}

def residentMotion(evt)
{
	// Don't think we need this any more
	//if (evt.value == "inactive") {
	//	if (state.residentsAreUp) {
	//    	startReArmSequence()
	//    }
	//}
  unsubscribe(residentMotions)
}

def contact(evt)
{
	if (evt.value == "open") {
		// TODO - check for residents being up?
		if (!state.residentsAreUp) {
			if (residentsHaveBeenQuiet()) {
				startAlarmSequence()
			}
			else {
				disarmIntrusionDetection()
			}
		}
	}
}

def alarm(evt)
{
	log.debug "$evt.name: $evt.value"
	if (evt.value == "off") {
		alarms?.off()
		state.alarmActive = false
	}
}

private disarmIntrusionDetection()
{
	log.debug "residents are up, disarming intrusion detection"
	state.residentsAreUp = true
	scheduleReArmCheck()
}

private scheduleReArmCheck()
{
	def cron = "0 * * * * ?"
	schedule(cron, "checkForReArm")
	log.debug "Starting re-arm check, cron: $cron"
}

def checkForReArm()
{
	def threshold = ((residentsQuietThreshold != null && residentsQuietThreshold != "") ? residentsQuietThreshold : 3) * 60 * 1000
	log.debug "checkForReArm: threshold is $threshold"
	// check last intruder motion
	def lastIntruderMotion = state.lastIntruderMotion
	log.debug "checkForReArm: lastIntruderMotion=$lastIntruderMotion"
	if (lastIntruderMotion != null)
	{
		log.debug "checkForReArm, time since last intruder motion: ${now() - lastIntruderMotion}"
		if (now() - lastIntruderMotion > threshold) {
			log.debug "re-arming intrusion detection"
			state.residentsAreUp = false
			unschedule()
		}
	}
	else {
		log.warn "checkForReArm: lastIntruderMotion was null, unable to check for re-arming intrusion detection"
	}
}

private startAlarmSequence()
{
	if (state.alarmActive) {
		log.debug "alarm already active"
	}
	else {
		state.alarmActive = true
		log.debug "starting alarm sequence"

		sendPush("Potential intruder detected!")

		if (newMode) {
			setLocationMode(newMode)
		}

		if (silentAlarm()) {
			log.debug "Silent alarm only"
			alarms?.strobe()
            if (location.contactBookEnabled) {
                sendNotificationToContacts(textMessage ?: "Potential intruder detected", recipients)
            }
            else {
                if (phone) {
                    sendSms(phone, textMessage ?: "Potential intruder detected")
                }
            }
		}
		else {
			def delayTime = seconds
			if (delayTime) {
				alarms?.strobe()
				runIn(delayTime, "soundSiren")
				log.debug "Sounding siren in $delayTime seconds"
			}
			else {
				soundSiren()
			}
		}

		if (lights) {
			flashLights(Math.min((seconds/2) as Integer, 10))
		}
	}
}

def soundSiren()
{
	if (state.alarmActive) {
		log.debug "Sounding siren"
        if (location.contactBookEnabled) {
            sendNotificationToContacts(textMessage ?: "Potential intruder detected", recipients)
        }
        else {
            if (phone) {
                sendSms(phone, textMessage ?: "Potential intruder detected")
            }
        }
		alarms?.both()
		if (lights) {
			log.debug "continue flashing lights"
			continueFlashing()
		}
	}
	else {
		log.debug "alarm activation aborted"
	}
	unschedule("soundSiren") // Temporary work-around to scheduling bug
}

def continueFlashing()
{
	unschedule()
	if (state.alarmActive) {
		flashLights(10)
		schedule(util.cronExpression(now() + 10000), "continueFlashing")
	}
}

private flashLights(numFlashes) {
	def onFor = 1000
	def offFor = 1000

	log.debug "FLASHING $numFlashes times"
	def delay = 1L
	numFlashes.times {
		log.trace "Switch on after  $delay msec"
		lights?.on(delay: delay)
		delay += onFor
		log.trace "Switch off after $delay msec"
		lights?.off(delay: delay)
		delay += offFor
	}
}

private silentAlarm()
{
	silent?.toLowerCase() in ["yes","true","y"]
}
