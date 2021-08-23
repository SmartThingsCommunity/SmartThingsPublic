/**
 *
 *  Copyright 2017 Curb, Inc
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

definition(name: "CURB Energy Manager",
  namespace: "curb",
  author: "Curb",
  description: "Maximize your energy savings with CURB",
  category: "Green Living",
  iconUrl: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
  iconX2Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
  iconX3Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png")

preferences {
  page(name: "pageOne", nextPage: "pageTwo") {
    section("Program") {
      input("name", "text", title: "Program Name", defaultValue: "CURB Energy Manager")
      input("enabled", "bool", title: "Active", defaultValue: true)
    }
    section("When to run") {
      input("weekdays", "enum", title: "Set Days of Week", multiple: true, required: true,
        options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
        defaultValue: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"])
      input("hours", "enum", title: "Select Times of Day", multiple: true, required: true,
        options: [[0 : "12am"], [1 : "1am"], [2 : "2am"],
          [3 : "3am"], [4 : "4am"], [5 : "5am"], [6 : "6am"],
          [7 : "7am"], [8 : "8am"], [9 : "9am"], [10 : "10am"],
          [11 : "11am"], [12 : "12pm"], [13 : "1pm"], [14 : "2pm"],
          [15 : "3pm"], [16 : "4pm"], [17 : "5pm"], [18 : "6pm"],
          [19 : "7pm"], [20 : "8pm"], [21 : "9pm"], [22 : "10pm"], [23 : "11pm"]])
    }
  }
  page(name: "pageTwo", nextPage: "pageThree" ) {
    section("Threshold Settings") {
      input("timeInterval", "enum", title: "Select Measurement Interval", multiple: false,
              options: [[15 : "15 minutes"], [30 : "30 minutes"], [60 : "60 minutes"]],
              defaultValue: 30)
      input("kwhThreshold", "float", title: "Set Threshold Usage (kW)")
      input("safetyMargin", "float", title: "Set Safety Margin (%)", defaultValue: 25)
      input("projectionPeriod", "float", title: "Set Projection Period (%)", defaultValue: 0)
      input("meter", "capability.powerMeter", title: "Select Power Meter to Trigger throttling on ('Net' in most cases)", multiple: false)
      input("circuits", "capability.powerMeter", title: "Circuits to send alerts on", multiple:true)
    }
  }
  page(name: "pageThree", install: true, uninstall: true) {
    section("Controlled Appliances") {
      input("thermostats", "capability.thermostat", title: "Select your Thermostat", multiple: true, required: false)
      input("switches", "capability.switch", title: "Select your Load Controllers", multiple: true, required: false)
    }

    section("Send Push Notification?") {
      input( "sendPush", "bool", required: false, title: "Send Push Notification?")
    }
  }
}

def installed() {
  resetClocking();
  initialize();
}

def updated() {
  runAutomation();
  unsubscribe();
  initialize();
}

def initialize() {
  subscribe(meter, "power", checkEnergyMonitor);
  runEvery1Minute(runAutomation);
}

// Returns true if we are in a selected automation time
def checkRunning() {
  def df = new java.text.SimpleDateFormat("EEEE");
  df.setTimeZone(location.timeZone);

  if (weekdays.contains( df.format(new Date()) )) {
    // We're in an enabled weekday
    def hf = new java.text.SimpleDateFormat("H");
    hf.setTimeZone(location.timeZone);

    if (hours.contains(hf.format(new Date()).toString())) {
      // We're in an enabled hour
      return true
    }
  }
  return false
}

// Creates the message and sends the push notification
def sendNotifications() {
  def devlist = []
  def count = 0
  def currentTotal = Float.parseFloat(meter.currentState("power").value)
  def message = "Curb Alert: Energy usage is projected to go over selected threshold."

  for(c in circuits) {
    try {
      if (c.toString() == "Total Power Usage") { continue }
      if (c.toString() == "Total Power Grid Impact") { continue }
      devlist.add([ pct: ((Float.parseFloat(c.currentState("power").value) / currentTotal) * 100).round(), name: c.toString() ])
      count += count
    } catch (e) {
      // sometimes we get circuits with no power value
      log.debug(e);
    }
  }
  if (devlist.size() > 3) {
      def sorted = devlist.sort { a, b -> b.pct <=> a.pct }
      message += "Your biggest consumers currently are: ${sorted[0].name} ${sorted[0].pct}%, ${sorted[1].name} ${sorted[1].pct}%, and ${sorted[2].name} ${sorted[2].pct}%"
  }
  sendPush(message)
}

// Resets the absolute time window
def resetClocking() {
  state.readings = []
  state.usage = 0
  if (state.throttling == true) {
    stopThrottlingUsage()
  }
}

//
def runAutomation() {
  if ( !enabled ) { return }
  if ( !checkRunning() ) { return }

  def mf = new java.text.SimpleDateFormat("m")
  def minute = Integer.parseInt(mf.format(new Date())) % Integer.parseInt(timeInterval)
  def samples = 0.0
  state.usage = 0.0

  if (minute == 0) {
    // This is the first minute of the process, reset variables
    resetClocking()
  }

  if (minute < Float.parseFloat(timeInterval) * (Float.parseFloat(projectionPeriod) / 100) ) {
    //We're in the projection period. Do not throttle
    return
  }

  for (int i = 0; i < Integer.parseInt(timeInterval); i++) {
    if (state.readings[i] != null) {
      samples = samples + 1.0
      log.debug(samples)
      state.usage = state.usage + (state.readings[i] / 1000)
      log.debug(state.usage)
    }
  }

  if (samples != 0.0) {
    def avgedUsage = minute * ( state.usage / samples ) / Float.parseFloat(timeInterval)
    log.debug("minute: " + minute)
    log.debug("usage: " + avgedUsage)
    def safetyThreshold = ( Float.parseFloat(kwhThreshold) * ( 1 - (Float.parseFloat(safetyMargin) / 100)))
    log.debug(safetyThreshold)
    if (avgedUsage > safetyThreshold) {
      throttleUsage()
    }
  }

}

// Saves power reading in circular buffer
def checkEnergyMonitor(evt) {
  def mf = new java.text.SimpleDateFormat("m")
  def minute = Integer.parseInt(mf.format(new Date())) % Integer.parseInt(timeInterval)

  def power = meter.currentState("power").value
  state.readings[minute] = Float.parseFloat(power)
}

// Gets and saves the current controller state for use during state restore
def captureContollerStates() {
  if (!state.throttling) {
    for (t in thermostats) {
      state[t.id] = t.currentState("thermostatMode").value
    }
    for (s in switches) {
      state[s.id] = s.currentState("switch").value
    }
  }
}

// Sets thermostats
def throttleUsage() {
  if (state.throttling) {
    return
  }
  captureContollerStates()
  sendNotifications()
  state.throttling = true

  for (t in thermostats) {
    t.off()
  }

  for (s in switches) {
    s.off()
  }
}

// Restores controller states to previously stored values
def stopThrottlingUsage() {
    state.throttling = false
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
