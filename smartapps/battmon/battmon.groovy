/**
 *  Battery Watcher
 *
 *  Copyright 2019 Al Sene
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
    name: "Battery Watcher",
    namespace: "asene",
    author: "Al Sene",
    description: "Simple weekly battery levels monitor that alerts as needed",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("“Monitor battery levels...") {
    input "lowBatteryLevel", "number", title: "Alert when battery level falls below:", required: true
    input "batteryDevices", "capability.battery", title: "Which?", multiple: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    subscribe (batteryDevices, "battDevs", batteryThings)
    initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    subscribe (batteryDevices, "battDevs", batteryThings)
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
	schedule("0 50 8 ? * *", runIn(60, checkBattery))
}

// TODO: implement event handlers
def checkBattery() {

    //Gets the current battery levels

    log.debug "Checking battery levels..."

    def BATT_THRESHOLD = settings.lowBatteryLevel.toInteger()
    def batteryList = batteryDevices.currentValue("battery") 
    def pDevice
    def bAllGood = true

	batteryList.eachWithIndex{ battLevel, index ->
		
        pDevice = batteryDevices[index]
        
		log.debug "$pDevice battery level is at $battLevel"
        
        if (battLevel < BATT_THRESHOLD) {
            if (pDevice != null) {
            	bAllGood = false
                log.debug "The $pDevice battery is low. Currently at $battLevel"
                sendPush("The $pDevice battery is low. Currently at $battLevel")
            }
        }
	}
    
    if (bAllGood){
    	log.debug "Check Complete. All battery levels are good!"
        //sendPush("Check Complete. All battery levels are good!")
    }

}

def batteryDevices (evt) { }