/**
 *
 *  Copyright 2021 Jose Augusto Baranauskas
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
definition (
    name: "Fan Automation",
    namespace: "baranauskas",
    author: "Jose Augusto Baranauskas",
    version: "1.5 (2021-12-18)",
    singleInstance: true,
    description: "Create fan automation based on temperature and humidity sensors",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
        page(name: "parentPage")
        page(name: "aboutPage")
}

def parentPage() {
	return dynamicPage(name: "parentPage", title: "", nextPage: "", install: true, uninstall: true) {
        section("Create a new fan automation.") {
            app(name: "FanAutomationChild",
                appName: "Fan Automation Child App" ,
                namespace: "baranauskas",
                title: "Press here for new Fan Automation",
                multiple: true)
        }
        section("About") {
          	paragraph textHelp()
   		  }
    }
}

def aboutPage() {
	dynamicPage(name: "aboutPage", title: none, install: true, uninstall: true) {
     	section("About") {
        	paragraph textHelp()
 		}
	}
}

private def textHelp() {
	def text =
		"This smartapp provides automatic control of ceiling fan speeds using"+
    " averaged temperature from sensors based on a temperature threshold,"+
    " changing each speed automatically in differential degree increments.\n\n"+
    "If the heat index computation is enabled and temperature sensors"+
    " are also humidity sensors then the heat index will be used"+
    " instead of the actual temperature from sensors. Please refer to"+
    " https://www.wpc.ncep.noaa.gov/html/heatindex_equation.shtml"+
    " for details.\n\n"+
    "A (virtual) switch is used to enable/disable fan automation.\n\n"+
    "Notifications can be enabled for automation events or for changes"+
    " in fan speed."
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
//    unsubscribe()
    initialize()
}

def initialize() {
    // nothing needed here, since the child apps will handle preferences/subscriptions
    // this just logs some messages for demo/information purposes
    log.debug "${app.label} (${app.name}) has ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child label: ${child.label}, name: ${child.name}"
    }
}
