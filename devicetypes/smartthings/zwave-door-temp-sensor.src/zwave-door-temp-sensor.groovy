/**
 *  Copyright 2018 SmartThings
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
 *  Z-Wave Door/Temp Sensor
 *
 */

metadata {
    definition (name: "Z-Wave Door/Temp Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.contact", runLocally: true, minHubCoreVersion: '000.024.0002', executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-contact-2") {
        capability "Contact Sensor"
        capability "Sensor"
        capability "Battery"
        capability "Configuration"
        capability "Health Check"
        capability "Temperature Measurement"

        fingerprint mfr:"015D", prod:"2003", model:"B41C", deviceJoinName: "Inovelli Door/Temp Sensor"
        fingerprint mfr:"0312", prod:"2003", model:"C11C", deviceJoinName: "Inovelli Door/Temp Sensor"
        fingerprint mfr:"015D", prod:"2003", model:"C11C", deviceJoinName: "Inovelli Door/Temp Sensor"
        fingerprint mfr:"015D", prod:"C100", model:"C100", deviceJoinName: "Inovelli Door/Temp Sensor"
        fingerprint mfr:"0312", prod:"C100", model:"C100", deviceJoinName: "Inovelli Door/Temp Sensor"
    }

    preferences {
        section {
            input title: "Temperature Offset", description: "This feature allows you to correct any temperature variations by selecting an offset. Ex: If your sensor consistently reports a temp that's 5 degrees too warm, you'd enter '-5'. If 3 degrees too cold, enter '+3'.", displayDuringSetup: false, type: "paragraph", element: "paragraph"
            input "tempOffset", "number", title: "Degrees", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
            tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
                attributeState("open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13")
                attributeState("closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC")
            }
            tileAttribute("device.temperature", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}°', backgroundColors: [])
            }
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label: '${currentValue}% battery', unit: ""
        }

        main "contact"
        details(["contact", "battery"])
    }
}

private getCommandClassVersions() {
    [
        0x20: 1, // Basic
        0x25: 1, // Switch Binary
        0x30: 1, // Sensor Binary
        0x31: 5, // Sensor Multilevel
        0x80: 1, // Battery
        0x84: 1, // Wake Up
        0x71: 3, // Alarm/Notification
        0x9C: 1  // Sensor Alarm
    ]
}

def parse(String description) {
    def result = null
    if (description.startsWith("Err 106")) {
        result = createEvent(
            descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
            eventType: "ALERT",
            name: "secureInclusion",
            value: "failed",
            isStateChange: true,
        )
    } else if (description != "updated") {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result = zwaveEvent(cmd)
        } else {
            log.debug "No Z-Wave Event for command ${cmd}"
        }
    }
    log.debug "parsed '$description' to $result"
    return result
}

def installed() {
    // Device-Watch simply pings if no device events received for 482min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

    // Request sensor data to initialize the UI
    def cmds = [
        zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW),
        zwave.batteryV1.batteryGet(),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 1),
        zwave.wakeUpV1.wakeUpNoMoreInformation()
    ]

    response(commands(cmds, 1000))
}

def updated() {
    configure()
}

def configure() {
    // Device-Watch simply pings if no device events received for 482min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

private getAdjustedTemp(value) {
    value = Math.round((value as Double) * 100) / 100
    if (tempOffset) {
        return value += Math.round(tempOffset * 100) / 100
    } else {
        return value
    }
}

private sensorValueEvent(value) {
    if (value) {
        createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
    } else {
        createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
    sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
    sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    def result = []
    def cmds = []

    if (cmd.notificationType == 0x06 && cmd.event == 0x16) {
        result << sensorValueEvent(1)
    } else if (cmd.notificationType == 0x06 && cmd.event == 0x17) {
        result << sensorValueEvent(0)
    } else if (cmd.notificationType == 0x07) {
        if (cmd.event == 0x00) {
            result << createEvent(descriptionText: "$device.displayName covering was restored", isStateChange: true)
            cmds = [zwave.batteryV1.batteryGet(), zwave.wakeUpV1.wakeUpNoMoreInformation()]
            result << response(commands(cmds, 1000))
        } else if (cmd.event == 0x01 || cmd.event == 0x02) {
            result << sensorValueEvent(1)
        } else if (cmd.event == 0x03) {
            result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
        }
    } else if (cmd.notificationType) {
        def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
        result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
    } else {
        def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
        result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
    }

    result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
    def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
    def cmds = []

    // If any of our initial request didn't make it, request the sensor data again
    if (device.currentValue("contact") == null) {
        cmds << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW)
    }

    if ((!state.lastbat) || ((now() - state.lastbat) > (53*60*60*1000))) {
        cmds << zwave.batteryV1.batteryGet()
    }

    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 1)
    cmds << zwave.wakeUpV1.wakeUpNoMoreInformation()

    [event, response(commands(cmds, 1000))]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    log.debug "SensorMultilevelReport: $cmd"
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            def realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.value = getAdjustedTemp(realTemperature)
            map.unit = getTemperatureScale()
            log.debug "Temperature Report: $map.value"
            break
        default:
            map.descriptionText = cmd.toString()
            break
    }

    [createEvent(map)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }

    state.lastbat = now()

    [createEvent(map)]
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)

    if (encapsulatedCommand) {
        log.debug "Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}"
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract Secure command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

private command(physicalgraph.zwave.Command cmd) {
    def zwInfo = zwaveInfo

    if (zwInfo?.zw?.contains("s") && (cmd.commandClassId == 0x20 || zwInfo.sec?.contains(String.format("%02X", cmd.commandClassId)))) {
        log.debug "securely sending $cmd"
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        log.debug "unsecurely sending $cmd"
        cmd.format()
    }
}

private commands(commands, delay=200) {
    delayBetween(commands.collect{ command(it) }, delay)
}