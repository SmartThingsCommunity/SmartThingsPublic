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

definition(
    name: "CURB Energy Manager",
    namespace: "curb",
    author: "Neil Zumwalde",
    description: "Maximize your energy savings with the Curbs!",
    category: "",
    iconUrl: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX2Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX3Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png")

preferences {

  section("Configuration Name") {
    input("name", "text", title: "Name", default:"CURB Energy Manager")
  }

  section("Set your desired energy usage") {
    input "meter", "capability.energyMeter", title: "Select your circuit to monitor", multiple: false
    input "threshold", "float", title: "Select your threshold (kWh)"
  }

  section("Select your controls") {
    input "thermostats", "capability.thermostat", title: "Select your Thermostat", multiple: true, required: false
    input "switches", "capability.switch", title: "Select your Load Controllers", multiple: true, required: false
  }

  section("Select when to run"){
    input("weekdays", "enum", title: "Select days to run", multiple: true, required: true, options:[
      "Monday",
      "Tuesday",
      "Wednesday",
      "Thursday",
      "Friday",
      "Saturday",
      "Sunday"
    ])
    input("hours", "enum",
        title: "Select hours to run",
        multiple: true,
        required: true,
      options:[[0:"12am"],[1:"1am"]])
    input("timeInterval", "enum", title: "Select time interval (minutes)", multiple: false, options:[15, 30, 60])
  }
   section("Send Notifications?") {
    input("recipients", "contact", title: "Send notifications to") {
        input "phone", "phone",
            description: "Phone Number", required: false
    }
  }
    section("Enable Application") {
    input("enabled", "bool", title:"Enable")
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
    if(weekdays.contains(df.format(new Date()))){
        def hf = new java.text.SimpleDateFormat("H")
        hf.setTimeZone(location.timeZone)
        if(hours.contains(hf.format(new Date()).toString())){
        	return true
        }
    }
    log.debug("not running")
    return false
}

def resetClocking() {
  log.debug("resetting the clock")
    state.readings = []
  for (int i = 0; i <Integer.parseInt(timeInterval); i++) {
      state.readings[i] = null
    }
    state.usage = 0
    log.debug(state)
    stopThrottlingUsage()
}

def runAutomation() {
  log.debug("running automation")
  def mf = new java.text.SimpleDateFormat("m")
  def minute = Integer.parseInt(mf.format(new Date())) % Integer.parseInt(timeInterval)
    if (minute == 0) {
    	resetClocking()
    }
    log.debug("automation/minute: " + minute.toString())
    state.usage = 0.0
    def samples = 0.0
    for(int i = 0; i<Integer.parseInt(timeInterval); i++) {
    	if(state.readings[i] != null)
        {
        	samples = samples + 1.0
    		state.usage = state.usage + ( state.readings[i] / 60 / 1000 )
        }
    }
    log.debug("samples:" + samples.toString())
    log.debug("usage:" + state.usage.toString())
    if (samples != 0.0)
    {
    	def uThresh = (Float.parseFloat(threshold) * 0.95)
        def uProj = ( state.usage / samples ) * Float.parseFloat(timeInterval)
        def headroom = uThresh - ( uProj * state.scalingFactor )
        log.debug("headroom: " + headroom.toString())
        def denom = (uProj /  Float.parseFloat(timeInterval)) * (1 - state.scalingFactor)
        def t = headroom / denom

        log.debug("projected usage : " + uProj)
        log.debug("time crossing: " + t)
        if ( t < minute ) {
        	throttleUsage()
        }
    }
}

def calculateUsage() {
  // If we're not scheduled to do work, don't do it.
  if(!checkRunning()){ return }
}

def checkEnergyMonitor(evt) {
  if(!checkRunning()){ return }
       def mf = new java.text.SimpleDateFormat("m")
        def minute = Integer.parseInt(mf.format(new Date())) % Integer.parseInt(timeInterval)
        log.debug(minute)

        def power = meter.currentState("power").value
        state.readings[minute] = Float.parseFloat(power)
        log.debug(state)

}

def captureContollerStates()
{
  if (!state.throttling)
  {
    for (t in thermostats)
    {
      state[t.id] = t.currentState("thermostatMode").value
    }
    for (s in switches)
    {
    	state[s.id] = s.currentState("switch").value
    }
    log.debug "state: ${state}"
  }
}

def restoreControllerStates()
{
  if (!state.throttling)
  {
    for (t in thermostats)
    {
      if (!state[t.id]) { continue }
      t.setThermostatMode(state[t.id])
    }

    for (s in switches){
      if (!state[s.id]) { continue }
      state[s.id] == "on" ? s.on() : s.off()
    }
  }
}

def throttleUsage()
{
  if(state.throttling) { return }
  state.throttling = true
  log.debug "throttling usage"
  captureContollerStates()

  for (t in thermostats){
    t.off()
  }

  for (s in switches){
    s.off()
  }
}

def stopThrottlingUsage()
{
  state.throttling = false
  log.debug "resuming normal operations"
  restoreControllerStates()
}
