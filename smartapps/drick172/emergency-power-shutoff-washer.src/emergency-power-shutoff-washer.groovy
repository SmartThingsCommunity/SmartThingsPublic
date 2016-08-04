/**
 *  Emergency Shutoff
 *
 *  Copyright 2016 Dan Rick
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
    name: "Emergency Power Shutoff - Washer",
    namespace: "Drick172",
    author: "Dan Rick",
    description: "Emergency Power Shutoff",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-ItsLeaking.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-ItsLeaking@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-ItsLeaking@2x.png")


preferences {
	section("When I detect water here:") {
    	input "Water", "capability.waterSensor", title: "Water", required: true, multiple: true
  	}
  	section("Which power do I turn off?") {
    	input "PowerOutlet", "capability.switch", title: "Turn Off Power", required: true, multiple: true
    }  
}

def installed() {
//	log.debug "Installed with settings: ${settings}"
    subscribe(Water, "Water", myHandler)
//	initialize()
}

def updated() {
//	log.debug "Updated with settings: ${settings}"
    subscribe(Water, "Water", myHandler)
//	unsubscribe()
//	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

def myHandler(evt) {
  if("wet" == evt.value && "off" != PowerOutlet.currentSwitch) {
    PowerOutlet.off()
  } else if("dry" == evt.value && "on" != PowerOutlet.currentSwitch) {
  	PowerOutlet.on()
  }
}


