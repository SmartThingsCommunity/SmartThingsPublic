/**
 *  Auto Mode for Thermostats
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
 *
 * 	Changes operating mode (e.g off) of selected thermostats when Smartthings hub changes into selected modes (e.g away). 
 *	Turns thermostats back into another desired operating mode (e.g Emergency Heat) when mode changes back (e.g home).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	VERSION HISTORY
 *  22.11.2015
 *	v1.0 - Initial Release
 */

definition(
    name:        "Auto Mode for Thermostats",
    namespace:   "alyc100/parent",
    author:      "Alex Lee Yuk Cheung",
    description: "Changes operating mode (e.g off) of selected thermostats when Smartthings hub changes into selected modes (e.g away). Turns thermostats back into another desired operating mode (e.g Emergency Heat) when mode changes back (e.g home).",
    category:    "My Apps",
    iconUrl:     "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url:   "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png"
)

preferences {
    page(name: "mainPage", title: "Thermostat Mode Automation", install: true, uninstall: true,submitOnChange: true) {
        section {
            app(name: "thermostatmodeautomation", appName: "Thermostat Mode Automation", namespace: "alyc100/thermostatmodeautomation", title: "Create New Thermostat Mode Rule", multiple: true)
            }
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
    // nothing needed here, since the child apps will handle preferences/subscriptions    
}

