/**
 *  Curb Smart Energy Max Plus
 *
 *  Copyright 2017 Neil Zumwalde
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

definition(name: "CURB Energy Manager", namespace: "curb", author: "Neil Zumwalde", description: "Maximize your energy savings with the Curbs!", category: "", iconUrl: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png", iconX2Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png", iconX3Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png")

preferences {
	page(name: "pageOne", nextPage: "pageTwo") {
		section("Program") {
			input("name", "text", title: "Program Name", defaultValue: "CURB Energy Manager")
			input("enabled", "bool", title: "Active", defaultValue: true)
		}
		section("When to run") {
			input("weekdays", "enum", title: "Set Days of Week", multiple: true, required: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], defaultValue: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"])
			input("hours", "enum", title: "Select Times of Day", multiple: true, required: true, options: [[0 : "12am"], [1 : "1am"], [2 : "2am"], [3 : "3am"], [4 : "4am"], [5 : "5am"], [6 : "6am"], [7 : "7am"], [8 : "8am"], [9 : "9am"], [10 : "10am"], [11 : "11am"], [12 : "12pm"], [13 : "1pm"], [14 : "2pm"], [15 : "3pm"], [16 : "4pm"], [17 : "5pm"], [18 : "6pm"], [19 : "7pm"], [20 : "8pm"], [21 : "9pm"], [22 : "10pm"], [23 : "11pm"]], defaultValue: [15, 16, 17, 18, 19])
		}
	}
	page(name: "pageTwo", nextPage: "pageThree" ) {
		section("Threshold Settings") {
			input("timeInterval", "enum", title: "Select Measurement Interval", multiple: false, options: [[15 : "15 minutes"], [30 : "30 minutes"], [60 : "60 minutes"]], defaultValue: 30)
			input("kwhThreshold", "float", title: "Set Threshold Usage (kWh)")
            input("safetyMargin", "float", title: "Set Safety Margin (%)", defaultValue: 25)
			input("projectionPeriod", "float", title: "Set Projection Period (%)", defaultValue: 50)
			input("meter", "capability.energyMeter", title: "Select Monitored Circuit", multiple: false)
		}
	}
    page(name: "pageThree", install: true, uninstall:true) {
		section("Controlled Appliances") {
			input("thermostats", "capability.thermostat", title: "Select your Thermostat", multiple: true, required: false)
			input("switches", "capability.switch", title: "Select your Load Controllers", multiple: true, required: false)
            input("cycleTimeLatency", "float", title: "Set Cycle Time Latency (minutes)", defaultValue: 5)
		}

		section("Notifications") {
			input("recipients", "contact", multiple: true) {
				input("phone", "phone", description: "Phone Number", required: false)
				input("email", "email", description: "Email Address", required: false)
			}
		}
	}
}

def installed() {
	resetClocking()
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	runAutomation()
	unsubscribe()
	initialize()
}

def initialize() {
	state.scalingFactor = 0.5
	subscribe(meter, "power", checkEnergyMonitor)
	runEvery1Minute(runAutomation)

}

def checkRunning() {
	def df = new java.text.SimpleDateFormat("EEEE")
	df.setTimeZone(location.timeZone)
	if (weekdays.contains(df.format(new Date()))) {
		def hf = new java.text.SimpleDateFormat("H")
		hf.setTimeZone(location.timeZone)
		if (hours.contains(hf.format(new Date()).toString())) {
			return true
		}
	}
	return false
}

def resetClocking() {
	log.debug("resetting the clock")
	state.readings = []
	for (int i = 0; i < Integer.parseInt(timeInterval); i++) {
		state.readings[i] = null
	}
	state.usage = 0
	log.debug(state)
	stopThrottlingUsage()
}

def runAutomation() {
	def mf = new java.text.SimpleDateFormat("m")
	def minute = Integer.parseInt(mf.format(new Date())) % Integer.parseInt(timeInterval)
	if (minute == 0) {
		resetClocking()
	}
	log.debug("automation/minute: " + minute.toString())
	state.usage = 0.0
	def samples = 0.0
	for (int i = 0; i < Integer.parseInt(timeInterval); i++) {
		if (state.readings[i] != null) {
			samples = samples + 1.0
			state.usage = state.usage + (state.readings[i] / 60 / 1000)
		}
	}
	log.debug("samples:" + samples.toString())
	log.debug("usage:" + state.usage.toString())
	if (samples != 0.0) {
    	def wt = Float.parseFloat(kwhThreshold) * (1 - ( Float.parseFloat(safetyMargin) / 100))
        def m = state.usage / samples
        def TH = Float.parseFloat(projectionPeriod) / 100
        def te = Float.parseFloat(timeInterval)
        def tCross = ( wt - (TH * m * te ) ) / ( m - (TH * m) )
        log.debug("wt: "+wt.toString()+" TH: "+TH.toString()+" m: "+m.toString()+" te: "+te.toString()+" usage: "+state.usage.toString()+" samples: "+samples.toString() + " tCross: " + tCross.toString() )

        if ( tCross < te && tCross > (te - Float.parseFloat(cycleTimeLatency) ) ) {
        	tCross = te - cycleTimeLatency
        }
		if ( tCross - 1 < minute ) {
			throttleUsage()
		}
	}
}

def checkEnergyMonitor(evt) {
	if (!checkRunning()) {
		return
	}

	def mf = new java.text.SimpleDateFormat("m")
	def minute = Integer.parseInt(mf.format(new Date())) % Integer.parseInt(timeInterval)

	def power = meter.currentState("power").value
	state.readings[minute] = Float.parseFloat(power)
	log.debug(state)
}

def captureContollerStates() {
	if (!state.throttling) {
		for (t in thermostats) {
			state[t.id] = t.currentState("thermostatMode").value
		}
		for (s in switches) {
			state[s.id] = s.currentState("switch").value
		}
		log.debug "state: ${state}"
	}
}

def restoreControllerStates() {
	if (!state.throttling) {
		for (t in thermostats) {
			if (!state[t.id]) {
				continue
			}
			t.setThermostatMode(state[t.id])
		}

		for (s in switches) {
			if (!state[s.id]) {
				continue
			}
			state[s.id] == "on" ? s.on() : s.off()
		}
	}
}

def throttleUsage() {
	if (state.throttling) {
		return
	}
	state.throttling = true
	log.debug "throttling usage"
	captureContollerStates()

	for (t in thermostats) {
		t.off()
	}

	for (s in switches) {
		s.off()
	}
}

def stopThrottlingUsage() {
	state.throttling = false
	log.debug "resuming normal operations"
	restoreControllerStates()
}
