/**
 *  Controlled Power Off
 *
 *  Copyright 2016 Andrew Crow
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
    name: "Controlled Power Off",
    namespace: "acrow311",
    author: "Andrew Crow",
    description: "Application used to power off devices that should be allowed to finish their cycle before being shut down such as air conditions and tankless water heaters.  Application will monitor electric usage and delay shutdown until usage has returned to inactive state.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	section("Shutdown when device not active") {
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "threshold", type: "number", title: "Reports Below...", required: true, description: "In watts, enter integer value")
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
	unsubscribe()
	subscribe(meter, "power", meterHandler)
}

def meterHandler(evt) {
    def meterValue = evt.value as double
    def thresholdValue = threshold as int
	def switchState = meter.currentValue("switch") == "on"  // Get current switch status (on = true)
	
    if (switchState) {  // If the switch is already off, do nothing
        if (meterValue < thresholdValue) { // If the power consumption is low enough, turn off switch
            log.info "${meter} reported energy ${meterValue} below ${threshold}. Turning off switch."
            sendNotificationEvent("${meter} not running, shutting down.")
            meter.off()
        } else { // Power consumption too high - device connected to switch in use - do not shutdown.
            sendNotificationEvent("Power consumption too high to shut off ${meter}.  Attempting again in a moment.")
            log.info "${meter} reported energy ${meterValue} above ${threshold}. Leaving switch on."
        }
    }
}