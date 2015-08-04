/**
 *  Copyright 2015 SmartThings
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
 *  Unlock It When I Arrive
 *
 *  Author: SmartThings
 *  Date: 2013-02-11
 */

definition(
    name: "Unlock It When I Arrive",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Unlocks the door when you arrive at your location.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When I arrive..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Unlock the lock..."){
		input "lock1", "capability.lock", multiple: true
	}
}

def installed()
{
	subscribe(presence1, "presence.present", presence)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence.present", presence)
}

def presence(evt)
{
	def anyLocked = lock1.count{it.currentLock == "unlocked"} != lock1.size()
	if (anyLocked) {
		sendPush "Unlocked door due to arrival of $evt.displayName"
		lock1.unlock()
	}
}
