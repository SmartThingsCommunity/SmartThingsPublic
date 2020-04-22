/**
 *  SmartLock Notification
 *
 *  Copyright 2016 Someone
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
    name: "SmartLock Notification",
    namespace: "nirajbhatt7",
    author: "Someone",
    description: "Sends push notification on the lock/unlock.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select Smart Lock/s") {
		input "theLock", "capability.lock", title:"door lock", required: true, multiple: true
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
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(theLock, "lock", lockHandler)
}

def lockHandler(evt) {
  sendPush("The ${evt.displayName} is ${evt.value}!")
  
  // just for debugging  
  log.debug "lock is ${evt.name}"
  log.debug "event data: ${evt.data}"
  log.debug "event display name: ${evt.displayName}"
  log.debug "Is this event a state change? ${evt.isStateChange()}"
  try {
        log.debug "The jsonValue of this event is ${evt.jsonValue}"
    } catch (e) {
        log.debug "Trying to get the jsonValue for ${evt.name} threw an exception: $e"
    }
  
  log.debug "The source of this event is: ${evt.source}"
  log.debug "event from physical actuation? ${evt.isPhysical()}"
  
  log.debug "lock status changed to ${evt.value}"
  
  
  
  
}

