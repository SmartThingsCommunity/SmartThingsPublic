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
            input "energyMonitor", "capability.energyMeter", title: "Select your Main", multiple: false
            input "thermostat", "capability.thermostat", title: "Select your Thermostat", multiple: false
            input "switches", "capability.switch", title: "Select your Load Controllers", multiple: true
		   	input(
                    name: "maximumEnergy",
                    type: "float",
                    title: "Maximum kWh over your selected energy interval"
                )
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
	state.throttling = false
    state.thermostatReturnMode = thermostat.currentState("thermostatMode").value
	subscribe(energyMonitor, "energy" ,checkEventofEnergyMonitor)
}

def checkEnergyMonitor(evt) {

    def currentEnergy = energyMonitor.currentState("energy")
	def referenceEnergyPoint = Float.parseFloat(settings.maximumEnergy) * 0.85
	log.debug "energy value as a string: ${currentEnergy.value} - reference wattage: ${referenceEnergyPoint}"



    if(Float.parseFloat(currentEnergy.value) > referenceEnergyPoint && !state.throttling) {
		log.debug "throttling usage"

        state.thermostatReturnMode = thermostat.currentState("thermostatMode").value
        state.throttling = true
        thermostat.off()
        for (s in switches){
        	s.off()
        }
    }

	referenceEnergyPoint = Float.parseFloat(settings.maximumEnergy) * 0.75

    if(Float.parseFloat(currentEnergy.value) < referenceEnergyPoint && state.throttling) {
        log.debug "resuming normal operations"

        log.debug thermostat.currentState("thermostatMode").value
        state.throttling = false
        thermostat.setThermostatMode(state.thermostatReturnMode)
        for (s in switches){
        	s.on()
        }
    }

    log.debug "throttling: ${state.throttling}"
}
