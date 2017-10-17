/**
 *  Left Open
 *
 *  Copyright 2017 Joe Vanderstelt
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
    name: "Left Open",
    namespace: "thisboyiscrazy",
    author: "Joe Vanderstelt",
    description: "Simple App the notifies you at a time if a sensor is open",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section {
        input "contactSensors", "capability.contactSensor", multiple: true
        input "someTime", "time"
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
    schedule(settings.someTime,check)
}

def check() {

	log.debug contactSensors.currentContact
    
    def open = contactSensors.findAll { it.currentContact == "open" }
    
    if(open.size() > 0) {
    	def strOpen = open.join(", ")
        sendNotification "Left Open: " + strOpen
    } 
}