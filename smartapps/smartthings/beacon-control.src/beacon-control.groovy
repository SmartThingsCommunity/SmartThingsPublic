/**
 *  Beacon Control
 *
 *  Copyright 2014 Physical Graph Corporation
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
	name: "Beacon Control",
	category: "SmartThings Internal",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Execute a Hello, Home phrase, turn on or off some lights, and/or lock or unlock your door when you enter or leave a monitored region",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/mindcontrol.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/MiscHacking/mindcontrol@2x.png"
)

preferences {
	page(name: "mainPage")
	
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def mainPage() {
	dynamicPage(name: "mainPage", install: true, uninstall: true) {

		section("Where do you want to watch?") {
			input name: "beacons", type: "capability.beacon", title: "Select your beacon(s)", 
				multiple: true, required: true
		}

		section("Who do you want to watch for?") {
			input name: "phones", type: "device.mobilePresence", title: "Select your phone(s)", 
				multiple: true, required: true
		}

		section("What do you want to do on arrival?") {
			input name: "arrivalPhrase", type: "enum", title: "Execute a phrase", 
				options: listPhrases(), required: false
			input "arrivalOnSwitches", "capability.switch", title: "Turn on some switches", 
				multiple: true, required: false
			input "arrivalOffSwitches", "capability.switch", title: "Turn off some switches", 
				multiple: true, required: false
			input "arrivalLocks", "capability.lock", title: "Unlock the door",
				multiple: true, required: false
		}

		section("What do you want to do on departure?") {
			input name: "departPhrase", type: "enum", title: "Execute a phrase", 
				options: listPhrases(), required: false
			input "departOnSwitches", "capability.switch", title: "Turn on some switches", 
				multiple: true, required: false
			input "departOffSwitches", "capability.switch", title: "Turn off some switches", 
				multiple: true, required: false
			input "departLocks", "capability.lock", title: "Lock the door",
				multiple: true, required: false
		}

		section("Do you want to be notified?") {
			input "pushNotification", "bool", title: "Send a push notification"
			input "phone", "phone", title: "Send a text message", description: "Tap to enter phone number", 
				required: false
		}

		section {
			label title: "Give your automation a name", description: "e.g. Goodnight Home, Wake Up"
		}

		def timeLabel = timeIntervalLabel()
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
			href "timeIntervalInput", title: "Only during a certain time", 
				description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"

			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
		}
	}
}

// Lifecycle management
def installed() {
	log.debug "<beacon-control> Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "<beacon-control> Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(beacons, "presence", beaconHandler)
}

// Event handlers
def beaconHandler(evt) {
	log.debug "<beacon-control> beaconHandler: $evt"

	if (allOk) {
		def data = new groovy.json.JsonSlurper().parseText(evt.data)
                // removed logging of device names. can be added back for debugging
		//log.debug "<beacon-control> data: $data - phones: " + phones*.deviceNetworkId

		def beaconName = getBeaconName(evt)
                // removed logging of device names. can be added back for debugging
		//log.debug "<beacon-control> beaconName: $beaconName"

		def phoneName = getPhoneName(data)
                // removed logging of device names. can be added back for debugging
		//log.debug "<beacon-control> phoneName: $phoneName"
		if (phoneName != null) {
            def action = data.presence == "1" ? "arrived" : "left"
            def msg = "$phoneName has $action ${action == 'arrived' ? 'at ' : ''}the $beaconName"

            if (action == "arrived") {
                msg = arriveActions(msg)
            }
            else if (action == "left") {
                msg = departActions(msg)
            }
            log.debug "<beacon-control> msg: $msg"

            if (pushNotification || phone) {
                def options = [
                    method: (pushNotification && phone) ? "both" : (pushNotification ? "push" : "sms"),
                    phone: phone
                ]
                sendNotification(msg, options)
            }
        }
	}
}

// Helpers
private arriveActions(msg) {
	if (arrivalPhrase || arrivalOnSwitches || arrivalOffSwitches || arrivalLocks) msg += ", so"
	
	if (arrivalPhrase) {
		log.debug "<beacon-control> executing: $arrivalPhrase"
		executePhrase(arrivalPhrase)
		msg += " ${prefix('executed')} $arrivalPhrase."
	}
	if (arrivalOnSwitches) {
		log.debug "<beacon-control> turning on: $arrivalOnSwitches"
		arrivalOnSwitches.on()
		msg += " ${prefix('turned')} ${list(arrivalOnSwitches)} on."
	}
	if (arrivalOffSwitches) {
		log.debug "<beacon-control> turning off: $arrivalOffSwitches"
		arrivalOffSwitches.off()
		msg += " ${prefix('turned')} ${list(arrivalOffSwitches)} off."
	}
	if (arrivalLocks) {
		log.debug "<beacon-control> unlocking: $arrivalLocks"
		arrivalLocks.unlock()
		msg += " ${prefix('unlocked')} ${list(arrivalLocks)}."
	}
	msg
}

private departActions(msg) {
	if (departPhrase || departOnSwitches || departOffSwitches || departLocks) msg += ", so"
	
	if (departPhrase) {
		log.debug "<beacon-control> executing: $departPhrase"
		executePhrase(departPhrase)
		msg += " ${prefix('executed')} $departPhrase."
	}
	if (departOnSwitches) {
		log.debug "<beacon-control> turning on: $departOnSwitches"
		departOnSwitches.on()
		msg += " ${prefix('turned')} ${list(departOnSwitches)} on."
	}
	if (departOffSwitches) {
		log.debug "<beacon-control> turning off: $departOffSwitches"
		departOffSwitches.off()
		msg += " ${prefix('turned')} ${list(departOffSwitches)} off."
	}
	if (departLocks) {
		log.debug "<beacon-control> unlocking: $departLocks"
		departLocks.lock()
		msg += " ${prefix('locked')} ${list(departLocks)}."
	}
	msg
}

private prefix(word) {
	def result
	def index = settings.prefixIndex == null ? 0 : settings.prefixIndex + 1
	switch (index) {
		case 0:
			result = "I $word"
			break
		case 1:
			result = "I also $word"
			break
		case 2:
			result = "And I $word"
			break
		default:
			result = "And $word"
			break
	}

	settings.prefixIndex = index
	log.trace "prefix($word'): $result"
	result
}

private listPhrases() {
	location.helloHome.getPhrases().label
}

private executePhrase(phraseName) {
	if (phraseName) {
		location.helloHome.execute(phraseName)
		log.debug "<beacon-control> executed phrase: $phraseName"
	}
}

private getBeaconName(evt) {
	def beaconName = beacons.find { b -> b.id == evt.deviceId }
	return beaconName
}

private getPhoneName(data) {    
	def phoneName = phones.find { phone ->
		// Work around DNI bug in data
		def pParts = phone.deviceNetworkId.split('\\|')
		def dParts = data.dni.split('\\|')
        pParts[0] == dParts[0]
	}
	return phoneName
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "<beacon-control> modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "<beacon-control> daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting, location?.timeZone).time
		def stop = timeToday(ending, location?.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "<beacon-control> timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel() {
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}

private list(List names) {
	switch (names.size()) {
		case 0:
			return null
		case 1:
			return names[0]
		case 2:
			return "${names[0]} and ${names[1]}"
		default:
			return "${names[0..-2].join(', ')}, and ${names[-1]}"
	}
}
