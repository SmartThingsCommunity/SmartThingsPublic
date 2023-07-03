/**
 *  Copyright 2015 Intraix
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
 *  Billion Smart Meter
 *
 *  Author: Intraix
 *  Date: 2015-09-03
 */
metadata {
    definition(name: "Billion Smart Meter", namespace: "intraix", author: "Intraix") {
        capability "Actuator"
        capability "Sensor"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Energy Meter"
        capability "Power Meter"

        // Custom attiributes for capabilities not supported by ST
        attribute "voltage", "number"
        attribute "current", "number"
        attribute "frequency", "number"
        attribute "powerFactor", "number"
        attribute "apparentPower", "number"

        fingerprint profileId: "0104", inClusters: "0000,0003,0006,0702", outClusters: ""
    }

    // simulator metadata
    simulator {
        // status messages
        status "on": "on/off: 1"
        status "off": "on/off: 0"

        // reply messages
        reply "zcl on-off on": "on/off: 1"
        reply "zcl on-off off": "on/off: 0"
    }

    // UI tile definitions
    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
            state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
            state "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
            state "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 1, height: 1) {
            state "energy", label: '${currentValue} kWh'
        }
        valueTile("power", "device.power", decoration: "flat", width: 1, height: 1) {
            state "power", label: '${currentValue} W'
        }
        valueTile("voltage", "device.voltage", decoration: "flat", width: 1, height: 1) {
            state "voltage", label: '${currentValue} V'
        }
        valueTile("current", "device.current", decoration: "flat", width: 1, height: 1) {
            state "current", label: '${currentValue} A'
        }
        valueTile("frequency", "device.frequency", decoration: "flat", width: 1, height: 1) {
            state "frequency", label: '${currentValue} Hz'
        }
        valueTile("powerFactor", "device.powerFactor", decoration: "flat", width: 1, height: 1) {
            state "powerFactor", label: 'Power Factor ${currentValue}'
        }
        valueTile("apparentPower", "device.apparentPower", decoration: "flat", width: 1, height: 1) {
            state "apparentPower", label: '${currentValue} VA'
        }
        main(["switch", "energy", "power", "voltage", "current", "frequency", "powerFactor", "apparentPower"])
        details(["switch", "energy", "power", "voltage", "current", "frequency", "powerFactor", "apparentPower", "refresh"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    log.debug "Parse description $description"
    def events = []

    if (description?.startsWith("catchall:")) {
        def descMap = parseCatchAllAsMap(description)
        log.debug "Catch all parsing: $description"
        // Command 01 is Read Attributes response and Command 0A is Report Attributes response.
        if (descMap.command == "01" || descMap.command == "0A") {
            if (descMap.clusterId == "0006") {
                // The last byte is the on/off value = 01/00.
                def value = descMap.raw.endsWith("01") ? "on" : "off"
                def event = createEvent(name: "switch", value: value)
                events.add(event)
            } else if (descMap.clusterId == "0702") {
                // The last 21 bytes are the energy data payload, which is 42 characters.
                def payload = descMap.raw.substring(descMap.raw.length() - 42, descMap.raw.length())
                log.debug "payload is $payload"
                events.addAll(parseEnergyPayload(payload))
            }
        }
    } else if (description?.startsWith("read attr -")) {
        def descMap = parseDescriptionAsMap(description)
        log.debug "Read attr: $description"
        if (descMap.cluster == "0006" && descMap.attrId == "0000") {
            def value = descMap.value.endsWith("01") ? "on" : "off"
            def event = createEvent(name: "switch", value: value)
            events.add(event)
        } else if (descMap.cluster == "0702" && descMap.attrId == "8000") {
            // Payload length is 21 bytes, which is 42 characters
            def payload = descMap.raw.substring(descMap.raw.length() - 42, descMap.raw.length())
            log.trace "payload is $payload"
            events.addAll(parseEnergyPayload(payload))
        }
    } else if (description?.startsWith("on/off:")) {
        log.debug "Switch command"
        def value = description?.endsWith(" 1") ? "on" : "off"
        def event = createEvent(name: "switch", value: value)
        events.add(event)
    }

    log.debug "Parse returned ${events}"
    return events
}


def parseEnergyPayload(payload) {
    def events = []

    // Decode the various parameters from the payload
    def voltage = Integer.parseInt(payload.substring(0, 4), 16) / 100
    log.trace "voltage is $voltage"
    def voltageEvent = createEvent(name: "voltage", value: voltage)
    events.add(voltageEvent)

    def current = Integer.parseInt(payload.substring(4, 8), 16) / 100
    log.trace "current is $current"
    def currentEvent = createEvent(name: "current", value: current)
    events.add(currentEvent)

    def frequency = Integer.parseInt(payload.substring(8, 12), 16) / 100
    log.trace "frequency is $frequency"
    def frequencyEvent = createEvent(name: "frequency", value: frequency)
    events.add(frequencyEvent)

    def powerFactor = Integer.parseInt(payload.substring(12, 14), 16) / 100
    log.trace "powerFactor is $powerFactor"
    def powerFactorEvent = createEvent(name: "powerFactor", value: powerFactor)
    events.add(powerFactorEvent)

    def activePower = Integer.parseInt(payload.substring(14, 22), 16) / 100
    log.trace "activePower is $activePower"
    def powerEvent = createEvent(name: "power", value: activePower)
    events.add(powerEvent)

    def apparentPower = Integer.parseInt(payload.substring(22, 30), 16) / 100
    log.trace "apparentPower is $apparentPower"
    def apparentPowerEvent = createEvent(name: "apparentPower", value: apparentPower)
    events.add(apparentPowerEvent)

    def mainEnergy = Integer.parseInt(payload.substring(30, 42), 16) / 1000
    log.trace "mainEnergy is $mainEnergy"
    def energyEvent = createEvent(name: "energy", value: mainEnergy)
    events.add(energyEvent)

    return events
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()): nameAndValue[1].trim()]
    }
}

def parseCatchAllAsMap(description) {
    def seg = (description - "catchall: ").split(" ")
    def zigbeeMap = [:]
    zigbeeMap += [raw: (description - "catchall: ")]
    zigbeeMap += [profileId: seg[0]]
    zigbeeMap += [clusterId: seg[1]]
    zigbeeMap += [sourceEndpoint: seg[2]]
    zigbeeMap += [destinationEndpoint: seg[3]]
    zigbeeMap += [options: seg[4]]
    zigbeeMap += [messageType: seg[5]]
    zigbeeMap += [dni: seg[6]]
    zigbeeMap += [isClusterSpecific: Short.valueOf(seg[7], 16) != 0]
    zigbeeMap += [isManufacturerSpecific: Short.valueOf(seg[8], 16) != 0]
    zigbeeMap += [manufacturerId: seg[9]]
    zigbeeMap += [command: seg[10]]
    zigbeeMap += [direction: seg[11]]
    zigbeeMap += [data: seg.size() > 12 ? seg[12].split("").findAll { it }.collate(2).collect {
        it.join('')
    } : []]

    zigbeeMap
}

// Commands to device
def on() {
    // Fire event for on since meter doesn't suppport zigbee bind
    sendEvent(name: "switch", value: "on")
    'zcl on-off on'
}

def off() {
    // Fire event for off since meter doesn't suppport zigbee bind
    sendEvent(name: "switch", value: "off")
    'zcl on-off off'
}

def configure() {
    meterConfig() + onOffConfig() + refresh()
}

// Meter reporting, min inteval 3 min and reporting interval if no activity as 4 min
// min change in value is 01
def meterConfig() {
    [
            "zcl global send-me-a-report 0x0702 0x8000 0x41 180 240 {01}",
            "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    ]
}

// Switch reporting, min interval 5 min and reporting interval if no activity as 10 min
// min change in value is 01
def onOffConfig() {
    [
            "zcl global send-me-a-report 6 0 0x10 300 600 {01}",
            "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    ]
}

// Read the meter and on/off cluster attributes
def refresh() {
    [
            "st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",
            "st rattr 0x${device.deviceNetworkId} 1 0x0702 0x8000"
    ]
}