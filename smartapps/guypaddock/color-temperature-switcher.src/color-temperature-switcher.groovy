/**
 *  Color Temperature Switcher
 *
 *  Provides a way to force one or more bulbs to a certain color temperature when a certain routine is executed.
 *
 *  Copyright 2017 Guy Paddock
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
//==================================================================================================================
// SmartThings Standard Definitions
//==================================================================================================================
definition(
  name: "Color Temperature Switcher",
  namespace: "GuyPaddock",
  author: "Guy Paddock",
  description: "Provides a way to force one or more bulbs to a certain color temperature when a certain routine is executed.",
  category: "Convenience",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  page(name: "configurationPage")
}

//==================================================================================================================
// Configuration Pages
//==================================================================================================================
def configurationPage() {
  dynamicPage(name: "configurationPage", title: "", install: true, uninstall: true) {
    section {
       input name: "routine", type: "enum", title: "Which routine triggers this?", options: getAllRoutines()
       input name: "temperature", type: "number", title: "What color temperature?", description: "Desired color temperature", range: "1700..9500", required: true, displayDuringSetup: true
       input "bulbs", "capability.colorTemperature", multiple: true, title: "Which bulbs should be affected?"
    }
  }
}

//==================================================================================================================
// SmartApps Standard Events
//==================================================================================================================
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
  subscribe(location, "routineExecuted", routineChanged)
    
  log.debug "Subscribed to routine: ${routine}"
}

//==================================================================================================================
// Registered Event Callbacks
//==================================================================================================================
def routineChanged(changeEvent) {
  log.trace "event name: ${changeEvent.name}"
  log.trace "event value: ${changeEvent.value}"
  log.trace "event displayName: ${changeEvent.displayName}"
  log.trace "event descriptionText: ${changeEvent.descriptionText}"

  if (changeEvent.displayName == routine) {
    log.debug "Selected routine has been triggered. Updating bulbs."
    updateAllBulbs()
  }
}

//==================================================================================================================
// Protected API
//==================================================================================================================
def updateAllBulbs() {
  log.debug "Changing color temperature of all bulbs to: ${temperature}"

  printStatusOfAllBulbs()

  for (bulb in bulbs) {
    bulb.setColorTemperature(temperature)
  }
    
  runIn(2, printStatusOfAllBulbs)
}

def printStatusOfAllBulbs() {
  log.debug "Status of all bulbs is as follows:"
    
  for (bulb in bulbs) {
    log.debug "  - ${bulb.currentState("colorTemperature").getValue()}"
  }

  log.debug("")
}

def getAllRoutines() {
  def result  = [];
  def routines = location?.helloHome?.getPhrases()*.label

  if (routines) {
    routines.sort()
    result = routines
  }

  return routines
}