/**
 *  Laundry Monitor Using Power Meter
 *
 *  Author: Daniel De Leo
 *
 *  Sends a message and (optionally) turns on or blinks a light to indicate that laundry is done.
 *  This is a modification of the SmartThings Laundry Monitor Template App. This app uses a power meter
 *  to determine your laundry status instead of an accelerometer. 
 *
 *  Date: 2013-02-21
 */

definition (
	name: "Laundry Monitor Using Power Meter",
	namespace: "danieldeleo",
	author: "Daniel De Leo",
	description: "Sends a message and (optionally) turns on or blinks a light to indicate that laundry is done.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-HotTubTuner.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-HotTubTuner%402x.png"
)

preferences {
	section("Tell me when this washer/dryer has stopped..."){
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false)
	}
	section("Via a push notification and/or an SMS message"){
        input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
        input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes", "No"], defaultValue: "No"
	}
	section("And by turning on these lights (optional)") {
		input "switches", "capability.switch", required: false, multiple: true, title: "Which lights?"
		input "lightMode", "enum", options: ["Flash Lights", "Turn On Lights"], required: false, defaultValue: "Flash Lights", title: "Action?"
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(meter, "power", meterHandler)
}

def meterHandler(evt) {
    def meterValue = evt.value as double
	if(meterValue == 0 && state.running) {
    	// if meterValue is at 0 Watts and the state is running,
        // this means washer/dryer is either done or in a soak cycle
    	state.running = false
        def msg = "${meter.displayName} is finished"

        if(phone) {// Send SMS message if phone number provided
            sendSms phone, msg
        }
        else {// Send Push Notification if no phone number provided
            sendPush msg
        }
        if(pushAndPhone == "Yes" && phone) {//Send both Push and SMS
            sendPush msg
        }

        if (switches) {
            if (lightMode?.equals("Turn On Lights")) {
                switches.on()
            }
            else {
                flashLights()
            }
        }
    }
    else if(meterValue > 10 && !state.running) {
    	// if meterValue is greater than 10Watts and the
        // previous read was 0, this means the machine is 
        // now running
        state.running = true
    }
}

private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 2000
	def offFor = offFor ?: 2000
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