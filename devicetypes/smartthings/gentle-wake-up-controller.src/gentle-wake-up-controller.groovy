/**
 *  Copyright 2016 SmartThings
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
metadata {
    definition (name: "Gentle Wake Up Controller", namespace: "smartthings", author: "SmartThings") {
        capability "Switch"
        capability "Timed Session"

        attribute "percentComplete", "number"

        command "setPercentComplete", ["number"]
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {

        multiAttributeTile(name: "richTile", type:"generic", width:6, height:4) {
            tileAttribute("sessionStatus", key: "PRIMARY_CONTROL") {
                attributeState "cancelled", action: "timed session.start", icon: "http://f.cl.ly/items/322n181j2K3f281r2s0A/playbutton.png", backgroundColor: "#ffffff", nextState: "running"
                attributeState "stopped", action: "timed session.start", icon: "http://f.cl.ly/items/322n181j2K3f281r2s0A/playbutton.png", backgroundColor: "#ffffff", nextState: "cancelled"
                attributeState "running", action: "timed session.stop", icon: "http://f.cl.ly/items/0B3y3p2V3X2l3P3y3W09/stopbutton.png", backgroundColor: "#00A0DC", nextState: "cancelled"
            }
            tileAttribute("timeRemaining", key: "SECONDARY_CONTROL") {
                attributeState "timeRemaining", label:'${currentValue} remaining'
            }
            tileAttribute("percentComplete", key: "SLIDER_CONTROL") {
                attributeState "percentComplete", action: "timed session.setTimeRemaining"
            }
        }

        // start/stop
        standardTile("sessionStatusTile", "sessionStatus", width: 1, height: 1, canChangeIcon: true) {
            state "cancelled", label: "Stopped", action: "timed session.start", backgroundColor: "#ffffff", icon: "http://f.cl.ly/items/1J1g0H2P0S1G1f2O1s1s/icon.png"
            state "stopped", label: "Stopped", action: "timed session.start", backgroundColor: "#ffffff", icon: "http://f.cl.ly/items/1J1g0H2P0S1G1f2O1s1s/icon.png"
            state "running", label: "Running", action: "timed session.stop", backgroundColor: "#00A0DC", icon: "http://f.cl.ly/items/1J1g0H2P0S1G1f2O1s1s/icon.png"
        }

        // duration
        valueTile("timeRemainingTile", "timeRemaining", decoration: "flat", width: 2) {
            state "timeRemaining", label:'${currentValue} left'
        }
        controlTile("percentCompleteTile", "percentComplete", "slider", height: 1, width: 3) {
            state "percentComplete", action: "timed session.setTimeRemaining"
        }

        main "sessionStatusTile"
        details "richTile"
//        details(["richTile", "sessionStatusTile", "timeRemainingTile", "percentCompleteTile"])
    }
}

// parse events into attributes
def parse(description) {
    log.debug "Parsing '${description}'"
    // TODO: handle 'switch' attribute
    // TODO: handle 'level' attribute
    // TODO: handle 'sessionStatus' attribute
    // TODO: handle 'timeRemaining' attribute

}

// handle commands
def on() {
    log.debug "Executing 'on'"
    startDimming()
}

def off() {
    log.debug "Executing 'off'"
    stopDimming()
}

def setTimeRemaining(percentComplete) {
    log.debug "Executing 'setTimeRemaining' to ${percentComplete}% complete"
    parent.jumpTo(percentComplete)
}

def start() {
    log.debug "Executing 'start'"
    startDimming()
}

def stop() {
    log.debug "Executing 'stop'"
    stopDimming()
}

def pause() {
    log.debug "Executing 'pause'"
    // TODO: handle 'pause' command
}

def cancel() {
    log.debug "Executing 'cancel'"
    stopDimming()
}

def startDimming() {
    log.trace "startDimming"
    log.debug "parent: ${parent}"
    parent.start("controller")
}

def stopDimming() {
    log.trace "stopDimming"
    log.debug "parent: ${parent}"
    parent.stop("controller")
}

def controllerEvent(eventData) {
    log.trace "controllerEvent"
    sendEvent(eventData)
}
