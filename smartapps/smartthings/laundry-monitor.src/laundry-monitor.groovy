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
 *  Laundry Monitor
 *
 *  Author: SmartThings
 *
 *  Sends a message and (optionally) turns on or blinks a light to indicate that laundry is done.
 *
 *  Date: 2013-02-21
 */

definition(
	name: "Laundry Monitor",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Sends a message and (optionally) turns on or blinks a light to indicate that laundry is done.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-HotTubTuner.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-HotTubTuner%402x.png"
)

preferences {
	section("Tell me when this washer/dryer has stopped..."){
		input "sensor1", "capability.accelerationSensor"
	}
	section("Via this number (optional, sends push notification if not specified)"){
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone Number", required: false
        }
	}
	section("And by turning on these lights (optional)") {
		input "switches", "capability.switch", required: false, multiple: true, title: "Which lights?"
		input "lightMode", "enum", options: ["Flash Lights", "Turn On Lights"], required: false, defaultValue: "Turn On Lights", title: "Action?"
	}
	section("Time thresholds (in minutes, optional)"){
		input "cycleTime", "decimal", title: "Minimum cycle time", required: false, defaultValue: 10
		input "fillTime", "decimal", title: "Time to fill tub", required: false, defaultValue: 5
	}
}

def installed()
{
	initialize()
}

def updated()
{
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(sensor1, "acceleration.active", accelerationActiveHandler)
	subscribe(sensor1, "acceleration.inactive", accelerationInactiveHandler)
}

def accelerationActiveHandler(evt) {
	log.trace "vibration"
	if (!state.isRunning) {
		log.info "Arming detector"
		state.isRunning = true
		state.startedAt = now()
	}
	state.stoppedAt = null
}

def accelerationInactiveHandler(evt) {
	log.trace "no vibration, isRunning: $state.isRunning"
	if (state.isRunning) {
		log.debug "startedAt: ${state.startedAt}, stoppedAt: ${state.stoppedAt}"
		if (!state.stoppedAt) {
			state.stoppedAt = now()
            def delay = Math.floor(fillTime * 60).toInteger()
			runIn(delay, checkRunning, [overwrite: false])
		}
	}
}

def checkRunning() {
	log.trace "checkRunning()"
	if (state.isRunning) {
		def fillTimeMsec = fillTime ? fillTime * 60000 : 300000
		def sensorStates = sensor1.statesSince("acceleration", new Date((now() - fillTimeMsec) as Long))

		if (!sensorStates.find{it.value == "active"}) {

			def cycleTimeMsec = cycleTime ? cycleTime * 60000 : 600000
			def duration = now() - state.startedAt
			if (duration - fillTimeMsec > cycleTimeMsec) {
				log.debug "Sending notification"

				def msg = "${sensor1.displayName} is finished"
				log.info msg

                if (location.contactBookEnabled) {
                    sendNotificationToContacts(msg, recipients)
                }
                else {

                    if (phone) {
                        sendSms phone, msg
                    } else {
                        sendPush msg
                    }

                }

				if (switches) {
					if (lightMode?.equals("Turn On Lights")) {
						switches.on()
					} else {
						flashLights()
					}
				}
			} else {
				log.debug "Not sending notification because machine wasn't running long enough $duration versus $cycleTimeMsec msec"
			}
			state.isRunning = false
			log.info "Disarming detector"
		} else {
			log.debug "skipping notification because vibration detected again"
		}
	}
	else {
		log.debug "machine no longer running"
	}
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 1000
	def offFor = offFor ?: 1000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 1L
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
				}
				else {
					s.off(delay:delay)
				}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
				}
			}
			delay += offFor
		}
	}
}
