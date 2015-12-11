/**
 *  Control garage with switch
 *
 *  Copyright 2014 ObyCode
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
    name: "Control garage with switch",
    namespace: "fwump38",
    author: "David Mirch",
    description: "Use a switch to control your garage. When the switch is pressed down, the garage door will close (if its not already), and likewise, it will open when up is pressed on the switch. Additionally, the indicator light on the switch will tell you if the garage door is open or closed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    section("Use this switch...") {
        input "theSwitch", "capability.switch", multiple: false, required: true
    }
    section("to control this garage door...") {
        input "theOpener", "capability.doorControl", multiple: false, required: true
    }
    section("whose status is given by this sensor...") {
        input "theSensor", "capability.doorControl", multiple: false, required: true
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
    subscribe(theSwitch, "switch", switchHit)
    subscribe(theSensor, "status", statusChanged)
}

def switchHit(evt) {
    log.debug "in switchHit: " + evt.value
    def current = theSensor.currentState("door")
    if (evt.value == "on") {
        if (current.value == "closed") { 
            theOpener.open()
        }
    } else {
        if (current.value == "open") {
            theOpener.close()
        }
    }
}

def statusChanged(evt) {
    if (evt.value == "open") {
        theSwitch.on()
    } else if (evt.value == "closed") {
        theSwitch.off()
    }
}