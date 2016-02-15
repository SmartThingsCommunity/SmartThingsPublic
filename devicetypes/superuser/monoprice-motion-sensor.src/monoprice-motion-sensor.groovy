/**
 *  Monoprice Motion Sensor
 *
 *  Capabilities: Motion Sensor, Temperature Measurement, Battery Indicator
 *
 *  Notes: For the Inactivity Timeout to update or Battery level (only for the first time),
 *    you have to open the Motion Sensor and leave it open for a few seconds and then close it.
 *    This triggers the forced Wake up so that the settings can take effect immediately.
 *
 *  Author: FlorianZ,Kranasian
 *  Date: 2014-11-26
 */
preferences {
    input("inactivityTimeout", "number", title: "Inactivity Timeout",
          description: "Number of minutes after movement is gone before its reported inactive by the sensor.")
}

metadata {
    definition (name: "Monoprice Motion Sensor", author: "florianz") {
        capability "Battery"
        capability "Motion Sensor"
        capability "Temperature Measurement"
        capability "Sensor"

        fingerprint deviceId:"0x2001", inClusters:"0x71, 0x85, 0x80, 0x72, 0x30, 0x86, 0x31, 0x70, 0x84"
    }

    simulator {
        status "inactive": "command: 2001, payload: 00"
        status "active": "command: 2001, payload: FF"

        for (int i = 0; i <= 100; i += 20) {
            status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
        }
    }

    tiles {
        standardTile("motion", "device.motion", width: 1, height: 1) {
            state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
            state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
        }

        // XXX: Add a setting for the desired temperature unit (C or F).
        //      How to change the valueTile's background color scale based
        //      on the set unit?

        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
            state "temperature", label:'${currentValue}°', unit:"°F",
            backgroundColors:[
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
            state "battery", label:'${currentValue}% battery', unit:"%"
        }
        main(["motion", "temperature"])
        details(["motion", "temperature", "battery"])
    }
}

def c2f(value) {
    // Given a value in degrees centigrade, return degrees Fahrenheit

    (value * 9/5 + 32) as Integer
}

def filterSensorValue(value) {
    // The temperature reading reported often quickly bounces between two values,
    // adding a lot of noise in the activity log. In order to filter out the
    // noise, a basic low pass filter is applied below.

    def maxHistory = 2
    if (!state.history) {
        state.history = []
    }
    state.history << value.toInteger()
    state.history = state.history.drop(state.history.size() - maxHistory)
    return (state.history.sum() / state.history.size()) as Integer
}

def parse(String description) {
    log.trace "Parse Raw: ${description}"
    def result = []
    // Using reference in: http://www.pepper1.net/zwavedb/device/197
    def cmd = zwave.parse(description, [0x20: 1, 0x80: 1, 0x31: 2, 0x84: 2, 0x71: 1, 0x30: 1])
    if (cmd) {
        if (cmd instanceof physicalgraph.zwave.commands.wakeupv2.WakeUpNotification) {
            result.addAll(sendSettingsUpdate(cmd))
        }
        result << createEvent(zwaveEvent(cmd))
        if (cmd.CMD == "8407") {
            result << new physicalgraph.device.HubAction(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
        }
    }

    log.debug "Parse returned ${result}"
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    //log.trace "Woke Up!"
    def map = [:]
    map.value = ""
    map.descriptionText = "${device.displayName} woke up."
    return map
}

def sendSettingsUpdate(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    def inactivityTimeout = (settings.inactivityTimeout == null ?
                             1 : Integer.parseInt(settings.inactivityTimeout))
    def inactivityTimeoutStr = Integer.toString(inactivityTimeout)
    def actions = []
    def lastBatteryUpdate = state.lastBatteryUpdate == null ? 0 : state.lastBatteryUpdate
    if ((new Date().time - lastBatteryUpdate) > 1000 * 60 * 60 * 24) {
        actions.addAll([
            response(zwave.batteryV1.batteryGet().format()),
            [ descriptionText: "Requested battery update from ${device.displayName}.", value: "" ],
            response("delay 600"),
        ])
    }
    actions.addAll([
        response(zwave.configurationV1.configurationSet(
            configurationValue: [inactivityTimeout], defaultValue: False, parameterNumber: 1, size: 1).format()),
        response("delay 600"),
        [ descriptionText: "${device.displayName} was sent inactivity timeout of ${inactivityTimeoutStr}.", value: "" ]
    ])
    actions
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    def map = [:]
    map.name = "motion"
    map.value = cmd.value ? "active" : "inactive"
    map.handlerName = map.value
    map.descriptionText = cmd.value ? "${device.displayName} detected motion" : "${device.displayName} motion has stopped."
    return map
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd) {
    def map = [:]
    if (cmd.sensorType == 1) {
        map.name = "temperature"

        // If the sensor returns the temperature value in degrees centigrade,
        // convert to degrees Fahrenheit. Also, apply a basic low-pass filter
        // to the scaled sensor value input.
        def filteredSensorValue = filterSensorValue(cmd.scaledSensorValue)
        if (cmd.scale == 1) {
            map.value = filteredSensorValue as String
            map.unit = "F"
        } else {
            map.value = c2f(filteredSensorValue) as String
            map.unit = "F"
        }
        map.descriptionText = "${device.displayName} temperature is ${map.value} ${map.unit}."
    }
    return map
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    state.lastBatteryUpdate = new Date().time
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF || cmd.batteryLevel == 0 ) {
        map.value = 1
        map.descriptionText = "${device.displayName} battery is almost dead!"
    } else if (cmd.batteryLevel < 15 ) {
        map.value = cmd.batteryLevel
        map.descriptionText = "${device.displayName} battery is low!"
    } else {
        map.value = cmd.batteryLevel
    }
    map
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Catch-all handler. The sensor does return some alarm values, which
    // could be useful if handled correctly (tamper alarm, etc.)
    [descriptionText: "Unhandled: ${device.displayName}: ${cmd}", displayed: false]
}