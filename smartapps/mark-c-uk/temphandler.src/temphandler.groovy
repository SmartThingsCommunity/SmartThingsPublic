/**
 *  Copyright 2015 SmartThings
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
 *  Author: mark
 *  version 2 user defineable timeout before checking if door opened or closed correctly. Raised default to 25 secs. You can reduce it to 15 secs. if you have custom simulated door with < 6 sec wait.
 */
 
definition(
    name: "tempHandler",
    namespace: "Mark-C-uk",
    author: "Mark Cockcroft",
    description: "Simulated thermostats",
    category: "Convenience",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather2",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather2"
)

preferences {
	section("Choose the thermstat to provide reading"){
		input "tempin", "capability.temperatureMeasurement", title: "temp provider", required: true
	}
    section("Choose the Virtual thermostat"){
		input "tempout", "capability.temperatureMeasurement", title: "temp reciver", required: false
	}
}

def installed() {
	installer()
}
def updated() {
	unsubscribe()
	installer()
}
def installer(){
	def Tin =   tempin.currenttemperature
	def Tout = tempout.currenttemperature
    
    subscribe(tempin, "temperature", tempHandler)
	log.debug "updated ... temp of $Tin is $tempin and $tempout is $Tout"
    
    if (Tin != Tout) { // sync them up if need be set virtual same as actual
      tempout.temperature(Tin)
	}    
}

def tempHandler(evt){
//testing
//def temps = 
log.debug "$evt.unit"
log.debug "evt.value"    
    tempout.temperature("${evt.value}")    
}