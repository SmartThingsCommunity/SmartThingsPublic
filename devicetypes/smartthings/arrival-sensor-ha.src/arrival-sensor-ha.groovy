import groovy.json.JsonOutput

/**
 *  Copyright 2017 SmartThings
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
    definition (name: "Arrival Sensor HA", namespace: "smartthings", author: "SmartThings",
            runLocally: true, minHubCoreVersion: '000.025.00032', executeCommandsLocally: true) {
        capability "Tone"
        capability "Actuator"
        capability "Presence Sensor"
        capability "Sensor"
        capability "Battery"
        capability "Configuration"
        capability "Health Check"

        fingerprint inClusters: "0000,0001,0003,000F,0020", outClusters: "0003,0019", manufacturer: "SmartThings", model: "tagv4", deviceJoinName: "SmartThings Presence Sensor"
    }

    preferences {
        section {
            image(name: 'educationalcontent', multiple: true, images: [
                "http://cdn.device-gse.smartthings.com/Arrival/Arrival1.png",
                "http://cdn.device-gse.smartthings.com/Arrival/Arrival2.png"
                ])
        }
        section {
            input "checkInterval", "enum", title: "Presence timeout (minutes)", description: "Tap to set",
                    defaultValue:"2", options: ["2", "3", "5"], displayDuringSetup: false
        }
    }

    tiles {
        standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
            state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#00a0dc"
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
    stopTimer()
    startTimer()
}

def installed() {
    // Arrival sensors only goes OFFLINE when Hub is off
    sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
}

def configure() {
    def cmds = zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0020) + zigbee.batteryConfig(20, 20, 0x01)
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

/**
 * Create battery event from reported battery voltage.
 *
 * @param volts Battery voltage in .1V increments
 */
private handleBatteryEvent(volts) {
	def descriptionText
    if (volts == 0 || volts == 255) {
        log.debug "Ignoring invalid value for voltage (${volts/10}V)"
    }
    else {
        def batteryMap = [28:100, 27:100, 26:100, 25:90, 24:90, 23:70,
                          22:70, 21:50, 20:50, 19:30, 18:30, 17:15, 16:1, 15:0]
        def minVolts = 15
        def maxVolts = 28

        if (volts < minVolts)
            volts = minVolts
        else if (volts > maxVolts)
            volts = maxVolts
        def value = batteryMap[volts]
        if (value != null) {
            def linkText = getLinkText(device)
            descriptionText = '{{ linkText }} battery was {{ value }}'
            def eventMap = [
                name: 'battery',
                value: value,
                descriptionText: descriptionText,
                translatable: true
            ]
            log.debug "Creating battery event for voltage=${volts/10}V: ${linkText} ${eventMap.name} is ${eventMap.value}%"
            sendEvent(eventMap)
        }
    }
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
    def descriptionText
    if ( present )
    	descriptionText = "{{ linkText }} has arrived"
    else
    	descriptionText = "{{ linkText }} has left"
    def eventMap = [
        name: "presence",
        value: present ? "present" : "not present",
        linkText: linkText,
        descriptionText: descriptionText,
        translatable: true
    ]
    log.debug "Creating presence event: ${device.displayName} ${eventMap.name} is ${eventMap.value}"
    sendEvent(eventMap)
}

private startTimer() {
    log.debug "Scheduling periodic timer"
    // Unlike stopTimer, only schedule this when running in the cloud since the hub will take care presence detection
    // when it is running locally
    runEvery1Minute("checkPresenceCallback", [forceForLocallyExecuting: false])
}

private stopTimer() {
    log.debug "Stopping periodic timer"
    // Always unschedule to handle the case where the DTH was running in the cloud and is now running locally
    unschedule("checkPresenceCallback", [forceForLocallyExecuting: true])
}

def checkPresenceCallback() {
    def timeSinceLastCheckin = (now() - state.lastCheckin ?: 0) / 1000
    def theCheckInterval = (checkInterval ? checkInterval as int : 2) * 60
    log.debug "Sensor checked in ${timeSinceLastCheckin} seconds ago"
    if (timeSinceLastCheckin >= theCheckInterval) {
        handlePresenceEvent(false)
    }
}
