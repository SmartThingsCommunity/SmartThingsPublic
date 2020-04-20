/**
 *  Automatically Close Garage Door
 *
 *  Copyright 2016 T.J. Simmons
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
    name: "Automatically Close Garage Door",
    namespace: "tjsimmons",
    author: "T.J. Simmons",
    description: "Automatically close your garage door after it's been open a set period of time.",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Transportation/transportation13-icn@2x.png")


preferences {
    section("Which garage door?") {
        input("garageDoor", "capability.garageDoorControl", required: true, title: "Which?")
    }

    section("Close after it's been open for how long?") {
        input("minutes", "number", required: true, title: "Minutes")
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
    subscribe(garageDoor, "door.open", doorOpenHandler)
}

def doorOpenHandler(evt) {
    log.debug("doorOpenHandler called: $evt")

    runIn(minutes * 60, closeGarageDoor)
}

def closeGarageDoor() {
    log.debug("closeGarageDoor called")

    def doorState = garageDoor.currentState("door")

    log.debug(doorState.value)

    if (doorState.value == "open") {
        log.debug("garage door has been left open; closing now")
        sendNotification("Garage door has been left open for $minutes minutes. Closing it now.")
        garageDoor.close()
    } else {
        log.debug("garage door is already closed")
    }
}