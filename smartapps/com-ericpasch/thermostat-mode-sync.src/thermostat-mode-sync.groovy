/**
 *  Thermostat Mode Sync
 *
 *  Description: Sync your thermostat's modes with your hub's modes.
 *  
 *  Copyright 2015 Eric Pasch
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
    name: "Thermostat Mode Sync",
    namespace: "com.ericpasch",
    author: "Eric Pasch",
    description: "Sync your thermostat's modes with your hub's modes.",
    category: "Green Living",
    iconUrl: "https://dl.dropboxusercontent.com/u/982179/reload-icon.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/982179/reload-icon.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/982179/reload-icon.png"
)
 
preferences {
	page( name: "rootPage" )
}

def rootPage() {
	dynamicPage(name: "rootPage", title: "", install: true, uninstall: true) {
        section("Thermostats") {
            input( name: "thermostats", type: "capability.thermostat", title: "Thermostats to Sync", multiple: true, required: true )    
        }
        
        def allModes = location.modes
        section("Modes") {
        	if(allModes) {
                allModes.each { m->
                    input( name: "baseMode${m}", type: "enum", title: "When hub is ${m}, set...", options: ["Nothing", "Away", "Home"], defaultValue: "baseMode${m}", required: true )
                }
            }
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(location, "mode", modeChangeHandler)
}

def modeChangeHandler(evt) {
	def curMode = evt.location.currentMode
    
    thermostats.each { t->
    	if("baseMode${curMode}" == "Away") {
        	t.away()
        } else if("baseMode${curMode}" == "Home") {
         	t.present()
		}
    }
}