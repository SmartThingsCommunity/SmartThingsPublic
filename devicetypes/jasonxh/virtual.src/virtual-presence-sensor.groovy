/**
 *  Compared to the simulated presence sensor maintained by SmartThings, this DTH removes the ability to toggle state
 *  through SmartThings app. Instead, all state changes must be triggered by commands, through a SmartApp such as CoRE
 *  or webCoRE.
 *
 *  Copyright 2018 Jason Xia
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
    definition(name: "Virtual Presence Sensor", namespace: "jasonxh", author: "Jason Xia") {
        capability "Presence Sensor"
        capability "Sensor"

        command "arrived"
        command "departed"
        command "toggle"
    }

    tiles {
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
            state "not present", label: '${name}', backgroundColor: "#ffffff"
            state "present", label: '${name}', backgroundColor: "#00a0dc"
        }
        main "presence"
        details "presence"
    }
}

def arrived() {
    log.trace "Executing 'arrived'"
    sendEvent name: "presence", value: "present"
}

def departed() {
    log.trace "Executing 'departed'"
    sendEvent name: "presence", value: "not present"
}

def toggle() {
    if (device.currentValue('presence') == 'not present') {
        arrived()
    } else {
        departed()
    }
}
