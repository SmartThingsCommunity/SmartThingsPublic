/**
 *  Nexia Thermostat Manager
 *
 *  Copyright 2018 Trent Foley
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
    name: "Nexia Thermostat Manager",
    namespace: "trentfoley",
    author: "Trent Foley",
    description: "Connect your Nexia thermostat to SmartThings",
    category: "Convenience",
    iconUrl: "http://lh4.ggpht.com/oMx3-nlICwLmUxpDhTXWsZ6Ocuzu9P2yfz9jpXBx1rhrW_Vcj94kPl2M9ooApckK6TM1=w60",
    iconX2Url: "https://www.trane.com/content/dam/Trane/residential/products/nexia/medium/TR_Nexia%20-%20Medium.jpg",
    iconX3Url: "https://www.trane.com/content/dam/Trane/residential/products/nexia/medium/TR_Nexia%20-%20Medium.jpg")


preferences {
	section("Title") {
		// TODO: put inputs here
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

// TODO: implement event handlers