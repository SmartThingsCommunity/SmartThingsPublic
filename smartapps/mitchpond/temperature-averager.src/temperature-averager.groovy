/**
 *  Temperature Averager
 *
 *  Copyright 2016 Mitch Pond
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
    name: "Temperature Averager",
    namespace: "mitchpond",
    author: "Mitch Pond",
    description: "Subscribes to multiple temperature sensors and generates a child device that represents the average of the chosen sensors.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
    singleInstance: true


preferences {
	page(name: "main", title: "Virtual Temp Sensors", install: true, uninstall: true,submitOnChange: true) {
        section {
            app(name: "virtualTempSensor", appName: "TA Child", namespace: "mitchpond", title: "New Virtual Temp Sensor", multiple: true)
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

}