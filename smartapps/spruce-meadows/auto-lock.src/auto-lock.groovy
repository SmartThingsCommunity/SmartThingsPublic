/**
 *  Lock After Time Delay
 *
 *  Copyright 2017 Larry Tillack
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
    name: "Auto-Lock",
    namespace: "Spruce Meadows",
    author: "Larry Tillack",
    description: "Relocks a lock after a time delay.  If seconds is < 5, then the lock will NOT auto-lock",
    category: "Convenience",
    iconUrl: "http://www.tillack.us/smart-apps/icons/doors/autoLock.png",
    iconX2Url: "http://www.tillack.us/smart-apps/icons/doors/autoLock@2x.png",
    iconX3Url: "http://www.tillack.us/smart-apps/icons/doors/autoLock@3x.png")


preferences {
	section("Lock this lock") {
		input "thelock", "capability.lock", required: true
	}
    section("Lock after (<5 won't auto-lock)") {
		input "seconds", "number", required: true, title: "Seconds?"
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
	subscribe(thelock,"lock.unlocked", lockedHandler)
}

def lockedHandler(evt) {
	runIn(seconds, checkLock)
}

def checkLock() {
	def lockState = thelock.currentState("lock")
    
    if (lockState.value == "unlocked") {
    	def elapsed = now() - lockState.date.time
       	def threshold = 1000 * seconds
        
        if (threshold >= 5000) {
			if (elapsed >= threshold) {
				thelock.lock()
			}
		}
    }
}