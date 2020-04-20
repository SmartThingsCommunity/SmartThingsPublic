/**
 *  Auto Lock Door
 *
 *  Copyright 2017 Preston Wiley
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
    name: "Auto Lock Door",
    namespace: "prestonwii",
    author: "Preston Wiley",
    description: "Lock a door after being unlocked for a specified period of time unless a switch is turned on.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/Cat-SafetyAndSecurity@3x.png")


preferences {
	section("Select Lock") {
      input "thelock", "capability.lock", required: true, title: "Which lock do you want to configure?"
	}
    section("Set Timer") {
      input "minutes", "number", required: true, title: "How many minutes before locking the lock?"
    }
    section("Unless") {
      input "switches", "capability.switch", multiple: true, title: "If any of these switches are on, do not lock the lock"
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
    subscribe(thelock, "lock.unlocked", lockUnlockedHandler)
}

def lockUnlockedHandler(evt) {
  log.debug "lockUnlockedHandler called: $evt"
  runIn(60 * minutes, lockLock)
}

def lockLock() {
    def curVal = thelock.currentValue("lock")
    if (curVal == "unlocked") {
      def currSwitches = switches.currentSwitch
      
      def onSwitches = currSwitches.findAll { switchVal ->
        switchVal == "on" ? true : false
      }
      
      if (onSwitches.size() > 0) {
        log.debug("switch being on aborts lock event")
      } else {
        log.debug("performing lock action on lock")
        thelock.lock()
      }
    } else {
      log.debug("lock is already locked")
    }
}