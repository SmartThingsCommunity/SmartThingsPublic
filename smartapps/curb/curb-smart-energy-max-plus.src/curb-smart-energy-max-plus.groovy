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
    name: "Curb Smart Energy Max Plus",
    namespace: "curb",
    author: "Neil Zumwalde",
    description: "Maximize your energy savings with the Curb Smart Energy Max Plus!",
    category: "",
    iconUrl: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX2Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX3Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png")


preferences {

  section("Set your desired energy usage") {
    input "energyMonitor", "capability.energyMeter", title: "Select your circuit to monitor (Probably the Main)", multiple: false
    input "maximumEnergy", "float", title: "Maximum kWh over your selected energy interval"
  }

  section("Select your controls") {
    input "thermostat", "capability.thermostat", title: "Select your Thermostat", multiple: false, required: false
    input "switches", "capability.switch", title: "Select your Load Controllers", multiple: true, required: false
  }

}

def installed() {

  log.debug "Installed with settings: ${settings}"

  initialize()
}

def updated() {

  log.debug "Updated with settings: ${settings}"

  unsubscribe()
  initialize()
}

def initialize() {

  captureContollerStates()
  subscribe(energyMonitor, "energy", checkEventEnergyMonitor)
  if(thermostat){
    subscribe(thermostat, "themostatMode", thermostatManualOverride)
  }
  for (s in switches)
  {
    subscribe(s, "switch",switchManualOverride)
  }
}

def checkEnergyMonitor(evt) {

  def currentEnergy = energyMonitor.currentState("energy")
  def highEnergyReference = Float.parseFloat(settings.maximumEnergy) * 0.85
  def lowEnergyReference = Float.parseFloat(settings.maximumEnergy) * 0.75

    if(Float.parseFloat(currentEnergy.value) > highEnergyReference && !state.throttling) {
      throttleUsage()
    }

    if(Float.parseFloat(currentEnergy.value) < lowEnergyReference && state.throttling) {
      stopThrottlingUsage()
    }

}

def thermostatManualOverride(event){
  if (event.value == "off" && state.throttling)
  {
    // We've captured the throttle off message
    return
  }
  if (event.value != state.thermostatReturnMode)
  {
    state.thermostatReturnMode = event.value
  }
}

def switchManualOverride(event){
  if (event.value == "off" && state.throttling)
  {
    // We've captured the throttle off message
    return
  }
  if (event.isStateChange())
  {
    switches.eachWithIndex { index, s ->
      if (s.getDeviceId() == event.getDeviceId()){
        state.switchReturnModes[index] = event.value
      }
  }
}
}

def captureContollerStates()
{
  if (state.throttling == false)
  {
    if (thermostat){
      state.thermostatReturnMode = thermostat.currentState("thermostatMode").value
    }
    switches.eachWithIndex { index, s ->
      state.switchReturnModes[index] = s.currentState("switch").value
    }
  }
}

def restoreControllerStates()
{
  if (state.throttling == false)
  {
    if (thermostat){
      thermostat.setThermostatMode(state.thermostatReturnMode)
    }
    switches.eachWithIndex { index, s ->
      if (state.switchReturnModes[index] == "on")
      {
        s.on()
      }
      else {
        s.off()
      }
    }
  }
}

def throttleUsage()
{
  state.throttling = true
  log.debug "throttling usage"
  captureContollerStates()
  thermostat.off()
  for (s in switches){
    s.off()
  }
}

def stopThrottlingUsage()
{
  log.debug "resuming normal operations"
  log.debug thermostat.currentState("thermostatMode").value
  restoreControllerStates()
  state.throttling = false
}
