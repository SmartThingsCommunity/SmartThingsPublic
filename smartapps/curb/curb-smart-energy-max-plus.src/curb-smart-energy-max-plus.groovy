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
log.debug thermostat.currentState("thermostatMode").value
	if (state.throttling == null)
    {
    	state.throttling = false
    }
  captureContollerStates()
  subscribe(energyMonitor, "energy", checkEnergyMonitor)

  log.debug "${thermostat}"
  if(thermostat)
  {
  	log.debug "thermostat == true"
  }
}

def checkEnergyMonitor(evt) {
	log.debug("checking energy")
    log.debug state
  def currentEnergy = energyMonitor.currentState("energy")
  def highEnergyReference = Float.parseFloat(settings.maximumEnergy) * 0.85
  def lowEnergyReference = Float.parseFloat(settings.maximumEnergy) * 0.75
	log.debug("current: ${currentEnergy.value} high: ${highEnergyReference} low: ${lowEnergyReference}")
    log.debug("Throttling? ${state.throttling}")

    if(Float.parseFloat(currentEnergy.value) > highEnergyReference && !state.throttling) {
      throttleUsage()
    }

    if(Float.parseFloat(currentEnergy.value) < lowEnergyReference && state.throttling) {

      stopThrottlingUsage()
    }
}

def captureContollerStates()
{
  if (state.throttling == false)
  {
    if (thermostat){
      state.thermostatReturnMode = thermostat.currentState("thermostatMode").value
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
  if (state.throttling == false)
  {
    if (thermostat){
      thermostat.setThermostatMode(state.thermostatReturnMode)
    }
    for (s in switches){
      if (state[s.id] == "on")
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
  log.debug state
  captureContollerStates()
  log.debug state
  thermostat.off()
  for (s in switches){
    s.off()
  }
}

def stopThrottlingUsage()
{
  state.throttling = false
  log.debug "resuming normal operations"
  log.debug state
  restoreControllerStates()
}
