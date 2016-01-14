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
    definition (name: "Arrival Sensor HA", namespace: "smartthings", author: "SmartThings") {
        capability "Tone"
        capability "Actuator"
        capability "Presence Sensor"
        capability "Sensor"
        capability "Battery"
        capability "Configuration"

        fingerprint inClusters: "0000,0001,0003,000F,0020", outClusters: "0003,0019",
                        manufacturer: "SmartThings", model: "tagv4", deviceJoinName: "Arrival Sensor"
    }

    preferences {
        section {
            image(name: 'educationalcontent', multiple: true, images: [
                "http://cdn.device-gse.smartthings.com/Arrival/Arrival1.png",
                "http://cdn.device-gse.smartthings.com/Arrival/Arrival2.png"
                ])
        }
        section {
            input "checkInterval", "enum", title: "Presence timeout (minutes)",
                    defaultValue:"2", options: ["2", "3", "5"], displayDuringSetup: false
        }
    }

    tiles {
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
            state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
            state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff"
        }
        standardTile("beep", "device.beep", decoration: "flat") {
            state "beep", label:'', action:"tone.beep", icon:"st.secondary.beep", backgroundColor:"#ffffff"
        }
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        main "presence"
        details(["presence", "beep", "battery"])
    }
}

def updated() {
    startTimer()
}

def configure() {
    def cmds = zigbee.configureReporting(0x0001, 0x0020, 0x20, 20, 20, 0x01)
    log.debug "configure -- cmds: ${cmds}"
    return cmds
}

def beep() {
    log.debug "Sending Identify command to beep the sensor for 5 seconds"
    return zigbee.command(0x0003, 0x00, "0500")
}

def parse(String description) {
    state.lastCheckin = now()
    handlePresenceEvent(true)

    if (description?.startsWith('read attr -')) {
        handleReportAttributeMessage(description)
    }

    return []
}

private handleReportAttributeMessage(String description) {
    def descMap = zigbee.parseDescriptionAsMap(description)

    if (descMap.clusterInt == 0x0001 && descMap.attrInt == 0x0020) {
        handleBatteryEvent(Integer.parseInt(descMap.value, 16))
    }
}

private handleBatteryEvent(rawValue) {
    def linkText = getLinkText(device)

    def eventMap = [
        name: 'battery',
        value: '--'
    ]

    def volts = rawValue / 10
    if (volts > 0){
        def minVolts = 2.0
        def maxVolts = 2.8

        if (volts < minVolts)
            volts = minVolts
        else if (volts > maxVolts)
            volts = maxVolts
        def pct = (volts - minVolts) / (maxVolts - minVolts)

        eventMap.value = Math.round(pct * 100)
        eventMap.descriptionText = "${linkText} battery was ${eventMap.value}%"
    }

    log.debug "Creating battery event: ${eventMap}"
    sendEvent(eventMap)
}

private handlePresenceEvent(present) {
    def wasPresent = device.currentState("presence")?.value == "present"
    if (!wasPresent && present) {
        log.debug "Sensor is present"
        startTimer()
    } else if (!present) {
        log.debug "Sensor is not present"
        stopTimer()
    }
    def linkText = getLinkText(device)
    def eventMap = [
        name: "presence",
        value: present ? "present" : "not present",
        linkText: linkText,
        descriptionText: "${linkText} has ${present ? 'arrived' : 'left'}",
    ]
    log.debug "Creating presence event: ${eventMap}"
    sendEvent(eventMap)
}

private startTimer() {
    log.debug "Scheduling periodic timer"
    schedule("0 * * * * ?", checkPresenceCallback)
}

private stopTimer() {
    log.debug "Stopping periodic timer"
    unschedule()
}

def checkPresenceCallback() {
    def timeSinceLastCheckin = (now() - state.lastCheckin) / 1000
    def theCheckInterval = (checkInterval ? checkInterval as int : 2) * 60
    log.debug "Sensor checked in ${timeSinceLastCheckin} seconds ago"
    if (timeSinceLastCheckin >= theCheckInterval) {
        handlePresenceEvent(false)
    }
}
