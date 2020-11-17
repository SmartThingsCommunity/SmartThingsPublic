/**
 *  Copyright 2015 SmartThings
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
 *
 *  Z-Wave alliance device page: http://products.z-wavealliance.org/products/69
 *  Device homepage: http://northq.com/qpower/
 *  Technical specification: https://doc.eedomus.com/files/northq_nq-92021_manuel_us.pdf
 *
 */
metadata {
    definition(name: "NorthQ Q-Power", namespace: "smartthings.f12.no", author: "Anders Sveen <anders@f12.no>") {
        capability "Energy Meter"
        capability "Power Meter"
        capability "Configuration"
        capability "Sensor"
        capability "Battery"

        fingerprint mfr: "0096", prod: "0001", model: "0001"
    }

    // simulator metadata
    simulator {
        for (int i = 0; i <= 100; i += 10) {
            status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
                    scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
        }
    }

    // tile definitions
    tiles {
        valueTile("energy", "device.energy", width: 3, height: 2, canChangeIcon: true) {
            state "default", label: '${currentValue} kWh'
        }
        valueTile("power", "device.power") {
            state "default", label: '${currentValue} W'
        }
        valueTile("battery", "device.battery") {
            state "default", label: '${currentValue} %'
        }

        main(["energy", "power", "battery"])
        details(["energy", "power", "battery"])
    }

    preferences {
        input name: "pulsesPerKwh", type: "number", title: "Pulses/kWh", description: "The number of pulses pr. kWh on your meter", required: true, defaultValue: 1000
        input name: "wakeUpSeconds", type: "number", title: "Seconds between reports", description: "How many seconds before reporting back. WARNING: Lowering this value will impact battery life.", required: true, defaultValue: 900
        input name: "baseKwh", type: "number", title: "Meter start value", description: "The number on your meter before you add this device.", required: false, defaultValue: 0
    }
}

def parse(String description) {
    log.debug("Event received parsing: '${description}'")
    def result = null
    if (description == "updated") return
    def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x72: 2])
    if (cmd) {
        log.debug "$device.displayName: Command received: $cmd"
        result = zwaveEvent(cmd)
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    def allCommands = [
            zwave.meterV1.meterGet().format(),
            zwave.batteryV1.batteryGet().format(),
            zwave.wakeUpV1.wakeUpNoMoreInformation().format()
    ]
    if (state.configurationCommands) {
        allCommands = (state.configurationCommands + allCommands)
    }

    state.configurationCommands = null

    log.debug("Sent ${allCommands.size} commands in response to wake up")

    return [
            createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false),
            response(allCommands)
    ]
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
    def events = []
    def commandTime = new Date()
    if (cmd.scale == 0) {
        Double newValue = cmd.scaledMeterValue + baseKwh
        events << createEvent(name: "energy", value: newValue, unit: "kWh")

        if (state.previousValue) {
            def diffTime = commandTime.getTime() - state.previousValueDate
            Double diffValue = newValue - state.previousValue

            Double diffHours = diffTime / 1000.0 / 60.0 / 60.0
            Double watt = 1000.0 * diffValue / diffHours

            events << createEvent(name: "power", value: Math.round(watt), unit: "W")
        }
        state.previousValue = newValue
        state.previousValueDate = commandTime.getTime()
    } else {
        log.error("Received meter report with scale ${cmd.scale} , don't know how to interpret that")
    }
    return events
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    return createEvent(name: "battery", value: cmd.batteryLevel, unit: "percent")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug("Configuration changed. Parameter number: ${cmd.parameterNumber}")
    return []
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "$device.displayName: Unhandled: $cmd"
    return []
}

def updated() {
    return configure()
}

def configure() {
    log.debug("Preparing configuration. It will be sent next time the device wakes up and checks in...")

    state.configurationCommands = [
            zwave.configurationV1.configurationSet(parameterNumber: 1, size: 4, scaledConfigurationValue: pulsesPerKwh.toInteger() * 10).format(),    // The number of blinks pr. kwh
            zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: 1).format(),                                // The type of meter, mechanical/electric pulse
            zwave.wakeUpV1.wakeUpIntervalSet(seconds: wakeUpSeconds, nodeid: zwaveHubNodeId).format()                                                 // Set the interval between wake ups
    ]

    return []
}