/**
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 TODO:
 1) DONE 12/11/2015: Move the thermostats preference to the main parent app, allowing multiple instances of parent
 2) Implement filter change notices
 */

definition(
	name: "Brighter Thermostat Controls",
    // each thermostat can have its own instance
	singleInstance: false,
	namespace: "trentfoley64",
	author: "A. Trent Foley, Sr.",
	description: "Brighter Thermostat Controls - time, day, and presence sensitive.",
	category: "My Apps",
	iconUrl: "http://www.trentfoley.com/ST/icons/thermostat.png",
	iconX2Url: "http://www.trentfoley.com/ST/icons/thermostat@2x.png",
	iconX3Url: "http://www.trentfoley.com/ST/icons/thermostat@3x.png"
)

preferences {
	page(name: "prefsThermostat")
	page(name: "prefsChildren")
}

def prefsThermostat() {
	dynamicPage(name: "prefsThermostat", title: "Thermostat", nextPage: "prefsChildren", install: false, uninstall: true ) {
		// Let user pick thermostats
		section("Set these thermostats") {
			input "thermostats", "capability.thermostat", title: "Which?", required: true, multiple: true
		}
        // Let user enter filter info
		section("Filter monitoring", hideable: true) {
			input "filterLastChanged", "date", title: "Date last changed?", required: false
			input "filterMonthsBeforeChange", "number", title: "Lasts how many months?", required: false
		}
        // Let user name the smartapp - I'd love to be able to provide a default name
		section {
			label title: "Assign a name", required: false, description: "$thermostats", default: "$thermostats"
			mode title: "Set for specific mode(s)", required: false
		}
	}
}

def prefsChildren() {
	dynamicPage(name: "prefsChildren", title: "Brighter Thermostat Controls", install: true, uninstall: true ) { //, submitOnChange: true) {
		section {
			app(name: "childThermostatControls", appName: "New Brighter Thermostat Control", namespace: "trentfoley64", title: "new thermostat control...", multiple: true)
		}
    }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	childApps.each {child ->
			log.info "Brighter Thermostat Controls: ${child.label}"
	}
}