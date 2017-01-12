/**
 *  Auto Mode for Thermostats (PARENT APP)
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
 *
 *	1. 	Save and Self-publish 'Auto Mode for Thermostats' SmartApp (https://github.com/alyc100/SmartThingsPublic/blob/master/smartapps/alyc100/parent/auto-mode-for-thermostats.src/auto-mode-for-thermostats.groovy)
 *		by creating a new SmartApp in the SmartThings IDE and pasting the source code in the "From Code" tab. 
 *
 *	2. 	Save (do not publish) 'Thermostat Mode Automation' SmartApp (https://github.com/alyc100/SmartThingsPublic/blob/master/smartapps/alyc100/thermostatmodeautomation/thermostat-mode-automation.src/thermostat-mode-automation.groovy)
 *		by creating a new SmartApp in the SmartThings IDE and pasting the source code in the "From Code" tab. 
 *
 *	3. 	Open SmartThings mobile app and locate "Auto Mode for Thermostats" SmartApp in the "My Apps" section of the Marketplace.
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
 *
 *	23.11.2015
 *	v1.1 - Now with support for Switch detection. 
 *		   Dynamic preference screen. 
 *	 	   Introduced option to disable thermostat reset.
 *
 *	24.11.2015
 *	v1.2 - 	 Extra Boost handling capabilities. 
 *		   	 Fixed bug where no reset was specified and app doesn't reset variable 'state.thermostatAltered'.
 *	v1.2.1 - Bug fixes.
 *	v1.3 -	 Option added to set mode of thermostat after boost action if reset mode is set to 'Boost for 60 minutes'.
 * 	v1.3.1 - Bug fixes.
 */

definition(
    name:        "Auto Mode for Thermostats",
    namespace:   "alyc100/parent",
    singleInstance: true,
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
