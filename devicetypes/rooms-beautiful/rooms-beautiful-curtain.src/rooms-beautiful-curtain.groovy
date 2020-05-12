/**
 *
 *	Copyright 2019 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 */

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
    definition(name: "Rooms Beautiful Curtain", namespace: "Rooms Beautiful", author: "Alex Feng", ocfDeviceType: "oic.d.blind", mnmn: "SmartThings", vid: "generic-shade-2") {
        capability "Actuator"
        capability "Battery"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"
        capability "Window Shade"
        capability "Switch"
        capability "Switch Level"

        attribute("replay", "enum")
        attribute("battLife", "enum")

        command "cont"

        fingerprint profileId: "0104", inClusters: "0000, 0001, 0003, 0006, FC00, DC00, 0102", deviceJoinName: "Rooms Beautiful Window Treatment", manufacturer: "Rooms Beautiful", model: "C001" //Curtain
    }

    preferences {
        input name: "invert", type: "bool", title: "Invert Direction", description: "Invert Curtain Direction", defaultValue: false, displayDuringSetup: false, required: true
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "windowShade", type: "generic", width: 6, height: 4) {
            tileAttribute("device.windowShade", key: "PRIMARY_CONTROL") {
                attributeState "open", label: 'Open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
                attributeState "closed", label: 'Closed', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
                attributeState "partially open", label: 'Partially open', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#d45614", nextState: "closing"
                attributeState "opening", label: 'Opening', action: "close", icon: "http://www.ezex.co.kr/img/st/window_open.png", backgroundColor: "#00A0DC", nextState: "closing"
                attributeState "closing", label: 'Closing', action: "open", icon: "http://www.ezex.co.kr/img/st/window_close.png", backgroundColor: "#ffffff", nextState: "opening"
            }
            tileAttribute("device.battLife", key: "SECONDARY_CONTROL") {
                attributeState "full", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/full.png", label: ""
                attributeState "medium", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/medium.png", label: ""
                attributeState "low", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/low.png", label: ""
                attributeState "dead", icon: "https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/dead.png", label: ""
            }
            tileAttribute("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "switch level.setLevel"
            }
        }
        standardTile("contPause", "device.replay", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "pause", label: "Pause", icon: 'https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/pause.png', action: 'pause', backgroundColor: "#e86d13", nextState: "cont"
            state "cont", label: "Cont.", icon: 'https://raw.githubusercontent.com/gearsmotion789/ST-Images/master/play.png', action: 'cont', backgroundColor: "#90d2a7", nextState: "pause"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        main "windowShade"
        details(["windowShade", "contPause", "refresh"])
    }
}

private getCLUSTER_WINDOW_COVERING() {
    0x0102
}
private getCOMMAND_GOTO_LIFT_PERCENTAGE() {
    0x05
}
private getATTRIBUTE_POSITION_LIFT() {
    0x0008
}
private getBATTERY_VOLTAGE() {
    0x0020
}

// Parse incoming device messages to generate events
def parse(String description) {
    // FYI = event.name refers to attribute name & not the tile's name

    def linkText = getLinkText(device)
    def event = zigbee.getEvent(description)
    def descMap = zigbee.parseDescriptionAsMap(description)
    def value
    def attrId

    if (event) {
        if (!descMap.attrId)
            sendEvent(name: "replay", value: "pause")

        if (event.name == "switch" || event.name == "windowShade") {
            if (event.value == "on" || event.value == "open") {
                log.info "${linkText} - Open"
                sendEvent(name: "switch", value: "on")
                sendEvent(name: "windowShade", value: "open")
            } else {
                log.info "${linkText} - Close"
                sendEvent(name: "switch", value: "off")
                sendEvent(name: "windowShade", value: "closed")
            }
        }
    } else {
        if (descMap.attrId) {
            if (descMap.clusterInt != 0xDC00) {
                value = Integer.parseInt(descMap.value, 16)
                attrId = Integer.parseInt(descMap.attrId, 16)
            }
        }

        switch (descMap.clusterInt) {
            case zigbee.POWER_CONFIGURATION_CLUSTER:
                if (attrId == BATTERY_VOLTAGE)
                    handleBatteryEvent(value)
                break;
            case CLUSTER_WINDOW_COVERING:
                if (attrId == ATTRIBUTE_POSITION_LIFT) {
                    log.info "${linkText} - Level: ${value}"
                    sendEvent(name: "level", value: value)

                    if (value == 0 || value == 100) {
                        sendEvent(name: "switch", value: value == 0 ? "off" : "on")
                        sendEvent(name: "windowShade", value: value == 0 ? "closed" : "open")
                    } else if (value > 0 && value < 100) {
                        sendEvent(name: "replay", value: "cont")
                        sendEvent(name: "windowShade", value: "partially open")
                    }
                }
                break;
            case 0xFC00:
                if (description?.startsWith('read attr -'))
                    log.info "${linkText} - Inverted: ${value}"
                else
                    log.debug "${linkText} - Inverted set to: ${invert}"
                break;
            case 0xDC00:
                value = descMap.value
                def shortAddr = value.substring(4)
                def lqi = zigbee.convertHexToInt(value.substring(2, 4))
                def rssi = (byte) zigbee.convertHexToInt(value.substring(0, 2))
                log.info "${linkText} - Parent Addr: ${shortAddr} **** LQI: ${lqi} **** RSSI: ${rssi}"
                break;
            default:
                log.warn "${linkText} - DID NOT PARSE MESSAGE for description: $description"
                log.debug descMap
                break;
        }
    }
}

def off() {
    zigbee.off() +
        sendEvent(name: "level", value: 0)
}

def on() {
    zigbee.on() +
        sendEvent(name: "level", value: 100)
}

def close() {
    zigbee.off() +
        sendEvent(name: "level", value: 0)
}

def open() {
    zigbee.on() +
        sendEvent(name: "level", value: 100)
}

def pause() {
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x02) +
        sendEvent(name: "replay", value: "cont") +
        sendEvent(name: "windowShade", value: "partially open")
}

def cont() {
    zigbee.command(CLUSTER_WINDOW_COVERING, 0x02) +
        sendEvent(name: "replay", value: "pause")
}

def setLevel(value) {
    def time
    if (state.updatedDate == null) {
        time = now()
    } else {
        time = now() - state.updatedDate
    }
    state.updatedDate = now()
    log.trace("Time: ${time}")

    if (time > 1000) {
        log.debug("Setting level to: ${value}")
        zigbee.command(CLUSTER_WINDOW_COVERING, COMMAND_GOTO_LIFT_PERCENTAGE, zigbee.convertToHexString(100 - value, 2)) +
            sendEvent(name: "level", value: value)
    }
}

private handleBatteryEvent(volts) {
    def linkText = getLinkText(device)

    if (volts > 30 || volts < 20) {
        log.warn "${linkText} - Ignoring invalid value for voltage (${volts/10}V)"
    } else {
        def batteryMap = [30: "full", 29: "full", 28: "full", 27: "medium", 26: "low", 25: "dead"]

        def value = batteryMap[volts]
        if (value != null) {
            def minVolts = 25
            def maxVolts = 30
            def pct = (volts - minVolts) / (maxVolts - minVolts)
            def roundedPct = Math.round(pct * 100)
            def percent = Math.min(100, roundedPct)

            log.info "${linkText} - Batt: ${value} **** Volts: ${volts/10}v **** Percent: ${percent}%"
            sendEvent(name: "battery", value: percent)
            sendEvent(name: "battLife", value: value)
        }
    }
}

def refresh() {
    zigbee.onOffRefresh() +
        zigbee.readAttribute(CLUSTER_WINDOW_COVERING, ATTRIBUTE_POSITION_LIFT) + // Window Lift Percentage Attribute
        zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, BATTERY_VOLTAGE) + // Battery Voltage Attribute

        // For Diagnostics
        zigbee.readAttribute(0xFC00, 0x0000) + // Invert CLuster
        zigbee.readAttribute(0xDC00, 0x0000) // Parent, LQI, RSSI Cluster
}

def ping() {
    return refresh()
}

def configure() {
    // Device-Watch allows 2 check-in misses from device + ping (plus 2 min lag time)
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
    log.debug "Configuring Reporting and Bindings."
    return refresh()
}

def installed() {
    sendEvent(name: "supportedWindowShadeCommands", value: JsonOutput.toJson(["open", "close", "pause"]), displayed: false)
    sendEvent(name: "battery", value: 100)
    sendEvent(name: "battLife", value: "full")
    response(refresh())
}

def updated() {
    if (invert.value == false)
        response(normal())
    else if (invert.value == true)
        response(reverse())
}

def normal() {
    if (device.currentState("windowShade").value == "open") {
        sendEvent(name: "switch", value: "off")
        sendEvent(name: "windowShade", value: "closed")
        sendEvent(name: "level", value: 100 - Integer.parseInt(device.currentState("level").value))
        log.debug("normal-close")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x00)
    } else {
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "windowShade", value: "open")
        sendEvent(name: "level", value: 100 - Integer.parseInt(device.currentState("level").value))
        log.debug("normal-open")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x00)
    }
}

def reverse() {
    if (device.currentState("windowShade").value == "open") {
        sendEvent(name: "switch", value: "off")
        sendEvent(name: "windowShade", value: "closed")
        sendEvent(name: "level", value: 100 - Integer.parseInt(device.currentState("level").value))
        log.debug("reverse-close")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x01)
    } else {
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "windowShade", value: "open")
        sendEvent(name: "level", value: 100 - Integer.parseInt(device.currentState("level").value))
        log.debug("reverse-open")
        zigbee.writeAttribute(0xFC00, 0x0000, DataType.BOOLEAN, 0x01)
    }
}
