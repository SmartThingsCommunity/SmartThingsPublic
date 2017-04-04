/**
 *  Dims a collection of dimmers together. Changing any one will send events to all of the others.
 *  This is unlike the "Dim With Me" app, which is one-to-many. This is many-to-many.
 *
 *  Copyright 2015 Michael Barnathan (michael@barnathan.name)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

definition(
        name: "Synchronized Dimming",
        namespace: "quantiletree",
        author: "Michael Barnathan",
        description: "Dims a collection of dimmers together. Changing any one will dim all of the others.",
        category: "Convenience",
        iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather13-icn@2x.png",
        iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather13-icn@2x.png"
)

preferences {
    section("Select devices to dim together:") {
        input "dimmers", "capability.switchLevel", required:true, title:"Dimmers", multiple:true
    }
}

def installed() {
    log.info "Dimmers tied! Settings: ${settings}"
    initialize()
}

def updated() {
    log.info "Dimmer ties updated: ${settings}"
    initialize()
}

def initialize() {
    unsubscribe()
    subscribeTo(dimmers ?: [])
    atomicState.deviceQueue = [:]
    atomicState.rootDevice = ""
}

private subscribeTo(devices) {
    subscribe(devices, "switch.on", tieOn)
    subscribe(devices, "switch.off", tieOff)
    subscribe(devices, "level", tieLevel)
    subscribe(devices, "switch.setLevel", tieLevel)
}

def tieLevel(event) {
    def level = event.value as int
    if (level == 0 && event.value != "0") {
        log.warn "Non-numeric dimmer level coming from ${event.dump()}: ${event.value}"
    } else {
        /* To avoid pingponging events forever:
             1. Wait for levels to settle before propagating events from children.
             2. No-op if the level to set is identical to the current level.
         */
        def deviceQueue = atomicState.deviceQueue

        // Because SmartThings prohibits ConcurrentHashMap.
        synchronized(this) {
            if (deviceQueue.remove(event.deviceId) == level) {
                log.trace "${event.displayName} has settled at level ${level}"

                if (!deviceQueue) {
                    log.trace "Synchronized to ${atomicState.rootDevice}: all dimmers have settled."
                    atomicState.rootDevice = ""
                }
                return
            } else if (!deviceQueue) {
                // With an empty queue, this should be the initiating device.
                atomicState.rootDevice = event.displayName
                sendNotificationEvent("${event.displayName} set to ${level}, synchronizing ${dimmers.size() - 1} dimmers.")
            }
        }

        def remainingDimmers = (dimmers ?: []).findAll { (it != event.device && it.currentValue("level") as int) != level }
        if (remainingDimmers) {
            synchronized(this) {
                deviceQueue << remainingDimmers.collectEntries { ["${it.device.id}": level] }
            }
            log.trace "${remainingDimmers.size()} dimmers remain to be updated to level ${level}, immediate trigger: ${event.displayName}, root trigger: ${atomicState.rootDevice}."
        }
        remainingDimmers*.setLevel(level)
    }
}

def tieOn(event) {
    sendNotificationEvent("${event.displayName} on, turning on ${dimmers.size()} dimmers")
    dimmers?.on()
}

def tieOff(event) {
    dimmers?.off()
}