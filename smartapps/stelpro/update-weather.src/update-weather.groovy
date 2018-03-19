/**
 *  Copyright 2015 Stelpro
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
 *  Get Remote Temperature
 *
 *  Author: Stelpro
  *
 *  Date: 2018-03-01
 */

definition(
    name: "update-weather",
    namespace: "stelpro",
    author: "Stelpro",
    description: "Force update the outside temperature of a specific device.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)



preferences() {
	section("Choose the Stelpro thermostats that will receive the weather... ") {
    	input "thermostats", "capability.refresh", title: "select thermostats", multiple: true
    }
    section("  ") { }
}
    
def installed() {
	log.debug "settings : $settings"
    initialize()
}

def updated() {
	log.debug "UPDATED! Settings : $settings"
    unschedule()
    initialize()
}

def initialize() {
    log.debug "initialize"
	runEvery15Minutes(getWeather)
}

def getWeather() {
    log.debug "getWeather"
    thermostats.updateWeather()
}
