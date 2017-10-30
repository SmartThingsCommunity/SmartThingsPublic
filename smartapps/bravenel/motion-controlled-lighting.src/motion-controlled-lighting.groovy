/**
 *  Motion Controlled Lighting
 *
 *  Copyright 2015 Bruce Ravenel
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
    name: "Motion Controlled Lighting",
    namespace: "bravenel",
    author: "Bruce Ravenel",
    description: "Turn On When There Is Motion, Off When None",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "SelectOptions")
	page(name: "SelectMotions")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
	}
}

def SelectOptions() {
	dynamicPage(name: "SelectOptions", title: "Choose switches and motion sensor(s)", nextPage: "SelectMotions", uninstall: true) {

		section {
			input "switches", "capability.switch", title: "Choose switches", required: true, multiple: true
		}
    
		section {
			input "motions", "capability.motionSensor", title: "Choose motion sensor(s) for ON", required: true, multiple: true
		}
        
		section {
			label title: "Assign a name:", required: false
		}

	}
}
    
def SelectMotions() {
	dynamicPage(name: "SelectMotions", title: "Set dimmer and motion off time", install: true, uninstall: false) {

		section("You can also:"){
			input "dimLevel", "number", title: "Set dimmers to this level", required: false, multiple: false, description: "0 to 99"
			input "turnOff", "bool", title: "Turn off when motion stops", required: false
			input "offMotions", "capability.motionSensor", title: "More motion sensor(s) for OFF?", required: false, multiple: true
			input "minutes", "number", title: "Wait this many minutes to turn off after motion stops (optional)", required: false
		}
    
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {

			def timeLabel = timeIntervalLabel()

			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null

			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
		}
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
	subscribe(switches, "switch.on", switchOnHandler)
	subscribe(switches, "switch.off", switchOffHandler)
	subscribe(motions, "motion.active", motionOnHandler)
	if(turnOff) {
		subscribe(motions, "motion.inactive", motionOffHandler)
		subscribe(offMotions, "motion.inactive", motionOffHandler)
		subscribe(offMotions, "motion.active", offMotionHandler)
	}
	state.switchesOn = false
	state.motionOffDismissed = true			// this state variable replaces unschedule()
}

def switchOnHandler(evt) {
	state.switchesOn = true
	state.motionOffDismissed = true			// disarm switchesOff()
}

def switchOffHandler(evt) {
	state.switchesOn = false
}

def motionOnHandler(evt) {
	state.motionOffDismissed = true			// disarm switchesOff()
	if(state.switchesOn) return
	if(allOk) {
		if(dimLevel) switches.each {if(it.currentLevel) it.setLevel(dimLevel) else it.on()} else switches.on()
		state.switchesOn = true
	}
}

def switchesOff() {
	if(state.motionOffDismissed) return
	if(allOk) {
		state.switchesOn = false
		switches.off()
	}
}

def motionOffHandler(evt) {
	if(allOk) {
		def noMotion = true
		(motions + offMotions).each {noMotion = noMotion && it.currentMotion == "inactive"}
		if(noMotion) {
			state.motionOffDismissed = false	// arm switchesOff()
			if(minutes) runIn(minutes*60, switchesOff) else switchesOff()
		} else state.motionOffDismissed = true
	}
}

def offMotionHandler(evt) {
	state.motionOffDismissed = true
}


// execution filter methods
private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
//	log.trace "modeOk = $result"
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
//	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
//		def start = timeToday(starting).time
		def start = timeToday(starting,location.timeZone).time
//		def stop = timeToday(ending).time
		def stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
//	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

private timeIntervalLabel() {
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}