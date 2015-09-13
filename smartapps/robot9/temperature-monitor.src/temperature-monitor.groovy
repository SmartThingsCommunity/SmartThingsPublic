/**
 *  Temperature monitor 
 *
 *  Copyright 2015 Yanan
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
    name: "Temperature monitor ",
    namespace: "robot9",
    author: "Yanan",
    description: "send an notification when temperature goes up or down",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	section("Sensors to monitor...") {
		input "sensor", "capability.temperatureMeasurement", title: "Temp Sensor", required: true
	}
    section("Set a temperature threshold..."){
    	input "max_", "decimal", title: "Set Max Temp"
        input "min_", "decimal", title: "Set Min Temp"
  	}
    section("When I arrive..."){
		input "me_", "capability.presenceSensor", title: "Who?", multiple: true
	}
    section("Send Notifications?") {
        input("recipients", "contact", title: "Send notifications to")
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
    subscribe(sensor, "temperature", weatherHandler)
    subscribe(me_, "presence.present", presence)
}

def weatherHandler(evt) {
  if(evt.doubleValue > max_) {
    sendNotificationToContacts("Room too hot! Current ${evt.doubleValue}F!", recipients)
  } else if (evt.doubleValue < min_) {
  	sendNotificationToContacts("Room too cold! Current ${evt.doubleValue}F!", recipients)
  }
}

def presence(evt) {
    def currentState = sensor.currentState("temperature")
    log.debug "Arrived home [${currentState.value}]"
    sendNotificationToContacts("Welcome home! Current temperature is ${currentState.value}F!", recipients)
}