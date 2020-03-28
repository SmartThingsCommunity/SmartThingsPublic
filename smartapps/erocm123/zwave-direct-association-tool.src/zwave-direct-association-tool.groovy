/**
 *  Z-Wave Direct Association Tool
 *
 *  Copyright 2016 Eric Maycock (erocm123)
 * 
 *  Note: Use a "Simulated Switch" from the IDE for best results
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
   name: "Z-Wave Direct Association Tool",
   namespace: "erocm123",
   author: "Eric Maycock",
   description: "Currently a proof of concept SmartApp that allows you to directly associate Z-Wave devices with other Z-Wave devices",
   category: "My Apps",
   iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
   iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page(name: "setupPage")
}

def setupPage() {
    dynamicPage(name: "setupPage", install: true, uninstall: true) {
    section {
        input "source", "capability.switch", title: "Which Contact Sensor?", multiple: false, required: true
        input "groupNumber", "enum", title: "Which group number?", multiple: false, required: true, options: [1,2,3,4,5]
        input "destination", "capability.switch", title: "Which switch?", multiple: true, required: false
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
  log.debug "Initializing Z-Wave Direct Association Tool"
  source.associateGroup(groupNumber, destination? destination.deviceNetworkId : [])
}