/**
 *  Tasmota - Fan Light
 *
 *  Copyright 2020 AwfullySmart.com - HongTat Tan
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

String driverVersion() { return "20201218" }
import groovy.json.JsonSlurper
metadata {
    definition(name: "Tasmota Fan Light", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.fan", vid: "beea8e9c-c35a-3f86-8be5-41113a35a700", mnmn: "SmartThingsCommunity") {
        capability "Switch Level"
        capability "Switch"
        capability "Fan Speed"
        capability "Health Check"
        capability "Actuator"
        capability "Refresh"
        capability "Sensor"
        capability "Signal Strength"

        attribute "lastSeen", "string"
        attribute "version", "string"

        command "turnOff"
        command "childTurnOff"
        command "low"
        command "medium"
        command "high"
        command "raiseFanSpeed"
        command "lowerFanSpeed"
    }

    simulator {
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota Fan Light v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "fanSpeed", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.fanSpeed", key: "PRIMARY_CONTROL") {
                attributeState "0", label: "off", action: "switch.on", icon: "st.thermostat.fan-off", backgroundColor: "#ffffff"
                attributeState "1", label: "low", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
                attributeState "2", label: "medium", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
                attributeState "3", label: "high", action: "switch.off", icon: "st.thermostat.fan-on", backgroundColor: "#00a0dc"
            }
            tileAttribute("device.fanSpeed", key: "VALUE_CONTROL") {
                attributeState "VALUE_UP", action: "raiseFanSpeed"
                attributeState "VALUE_DOWN", action: "lowerFanSpeed"
            }
        }

        standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
        }

        standardTile("lqi", "device.lqi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'LQI: ${currentValue}'
        }

        standardTile("rssi", "device.rssi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'RSSI: ${currentValue}dBm'
        }

        main "fanSpeed"
        details(["fanSpeed", "refresh", "lqi", "rssi"])
    }
}

def installed() {
    state?.lastFanSpeed = 2
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "switch", value: "off")
    response(refresh())
}

def uninstalled() {
    sendEvent(name: "epEvent", value: "delete all", isStateChange: true, displayed: false, descriptionText: "Delete endpoint devices")
}

def updated() {
    initialize()
}

def initialize() {
    if (!state?.lastFanSpeed) { state?.lastFanSpeed = 2 }
    if (device.hub == null) {
        log.error "Hub is null, must set the hub in the device settings so we can get local hub IP and port"
        return
    }
    // Child creation
    String childMeta = getDataValue("child")
    if (childMeta != null && ((childMeta.startsWith("{") && childMeta.endsWith("}")) || (childMeta.startsWith("[") && childMeta.endsWith("]")))) {
        def json = new JsonSlurper().parseText(childMeta)
        if (json != null) {
            boolean hasError = false
            json.each { i,tasmota ->
                try {
                    String dni = "${device.deviceNetworkId}-ep${i}"
                    addChildDevice(tasmota, dni, device.getHub().getId(),
                            [completedSetup: true, label: "${device.displayName} ${i}", isComponent: false])
                    log.debug "Created '${device.displayName}' - ${i}ch (${tasmota})"
                } catch (all) {
                    hasError = true
                    log.error "Error: ${(all as String).split(":")[1]}."
                }
            }
            if (hasError == false) {
                updateDataValue("child","")
            }
        }
    }

    def syncFrequency = (parent.generalSetting("frequency") ?: 'Every 1 minute').replace('Every ', 'Every').replace(' minute', 'Minute').replace(' hour', 'Hour')
    try {
        "run$syncFrequency"(refresh)
    } catch (all) { }
    sendEvent(name: "checkInterval", value: parent.checkInterval(), displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])

    parent.callTasmota(this, "Status 5")
    parent.callTasmota(this, "Backlog Rule1 ON FanSpeed#data DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"FanSpeed\":\"%value%\"}} ENDON ON Power1#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER1\":\"%value%\"}} ENDON ON Wifi#Connected DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"cb\":\"Status 11\"} ENDON;Rule1 1")
    refresh()
}

def parse(String description) {
    def events = null
    def message = parseLanMessage(description)
    def json = parent.getJson(message.header)
    if (json != null) {
        events = parseEvents(200, json)
    }
    return events
}

def calledBackHandler(physicalgraph.device.HubResponse hubResponse) {
    def events = null
    def status = hubResponse.status
    def json = hubResponse.json
    events = parseEvents(status, json)
    return events
}

def parseEvents(status, json) {
    def events = []
    if (status as Integer == 200) {
        def channel = getDataValue("endpoints")?.toInteger()
        def eventdateformat = parent.generalSetting("dateformat")
        def now = location.timeZone ? new Date().format("${eventdateformat}a", location.timeZone) : new Date().format("yyyy MMM dd EEE h:mm:ss")

        // FanSpeed
        def fanSpeed = (json?.StatusSTS?.FanSpeed != null) ? json?.StatusSTS?.FanSpeed as Integer : ((json?.FanSpeed != null) ? json?.FanSpeed as Integer : null)
        if (fanSpeed != null) {
            def rawLevel = 0
            if (fanSpeed == 1) {
                rawLevel = 32
                state.lastFanSpeed = 1
            } else if (fanSpeed == 2) {
                rawLevel = 66
                state.lastFanSpeed = 2
            } else if (fanSpeed == 3) {
                rawLevel = 100
                state.lastFanSpeed = 3
            }
            def value = (rawLevel ? "on" : "off")
            events << sendEvent(name: "switch", value: value)
            events << sendEvent(name: "level", value: rawLevel, displayed: false)
            events << sendEvent(name: "fanSpeed", value: fanSpeed)
            log.debug "Fan switch: '" + value + "', level: '${rawLevel}', fanSpeed: '${fanSpeed}'"
        }

        // Light
        def power = (json?.StatusSTS?.POWER1 != null) ? (json?.StatusSTS?.POWER1) : ((json?.POWER1 != null) ? json?.POWER1 : null)
        if (power != null) {
            if (power in ["ON", "1"]) {
                childDevices[0]?.sendEvent(name: "switch", value: "on")
                log.debug "Light switch: 'on'"
            } else if (power in ["OFF", "0"]) {
                childDevices[0]?.sendEvent(name: "switch", value: "off")
                log.debug "Light switch: 'off'"
            }
        }

        // MAC
        if (json?.StatusNET?.Mac != null) {
            def dni = parent.setNetworkAddress(json.StatusNET.Mac)
            def actualDeviceNetworkId = device.deviceNetworkId
            if (actualDeviceNetworkId != state.dni) {
                runIn(10, refresh)
            }
            log.debug "MAC: '${json.StatusNET.Mac}', DNI: '${state.dni}'"
            if (state.dni == null || state.dni == "" || dni != state.dni) {
                if (channel > 1 && childDevices) {
                    childDevices.each {
                        it.deviceNetworkId = "${dni}-ep" + parent.channelNumber(it.deviceNetworkId)
                        log.debug "Child: " + "${dni}-ep" + parent.channelNumber(it.deviceNetworkId)
                    }
                }
            }
            state.dni = dni
        }

        // Signal Strength
        if (json?.StatusSTS?.Wifi != null) {
            events << sendEvent(name: "lqi", value: json?.StatusSTS?.Wifi.RSSI, displayed: false)
            events << sendEvent(name: "rssi", value: json?.StatusSTS?.Wifi.Signal, displayed: false)
        }

        // Version
        if (json?.StatusFWR?.Version != null) {
            state.lastCheckedVersion = new Date().getTime()
            events << sendEvent(name: "version", value: json.StatusFWR.Version, displayed: false)
        }

        // Call back
        if (json?.cb != null) {
            parent.callTasmota(this, json.cb)
        }

        // Last seen
        events << sendEvent(name: "lastSeen", value: now, displayed: false)
    }
    return events
}

def on() {
    def healthState = parent.childSetting(device.id, "health_state")
    def fanSpeed = (state?.lastFanSpeed) ? state.lastFanSpeed : "2"
    if (healthState == true) {
        def rawLevel = 2
        if (fanSpeed == 1) {
            rawLevel = 32
        } else if (fanSpeed == 2) {
            rawLevel = 66
        } else if (fanSpeed == 3) {
            rawLevel = 100
        }
        sendEvent(name: "switch", value: "on")
        sendEvent(name: "level", value: rawLevel, displayed: false)
        sendEvent(name: "fanSpeed", value: fanSpeed)
    }
    setFanSpeed(fanSpeed)
}

def off() {
    def healthState = parent.childSetting(device.id, "health_state")
    if (healthState == true) {
        turnOff()
    }
    parent.callTasmota(this, "FanSpeed 0")
}

def turnOff() {
    sendEvent(name: "switch", value: "off")
    sendEvent(name: "level", value: 0, displayed: false)
    sendEvent(name: "fanSpeed", value: 0)
}

def setLevel(value, rate = null) {
    def level = value as Integer
    level = level == 255 ? level : Math.max(Math.min(level, 99), 0)

    if (1 <= level && level <= 32) {
        low()
    } else if (33 <= level && level <= 66) {
        medium()
    } else if (67 <= level && level <= 100) {
        high()
    } else {
        off()
    }
    return
}

def setFanSpeed(speed) {
    if (speed as Integer == 0) {
        off()
    } else if (speed as Integer == 1) {
        low()
    } else if (speed as Integer == 2) {
        medium()
    } else if (speed as Integer == 3) {
        high()
    } else if (speed as Integer == 4) {
        high()
    }
}

def raiseFanSpeed() {
    setFanSpeed(Math.min((device.currentValue("fanSpeed") as Integer) + 1, 3))
}

def lowerFanSpeed() {
    setFanSpeed(Math.max((device.currentValue("fanSpeed") as Integer) - 1, 0))
}

def low() {
    state.lastFanSpeed = 1
    parent.callTasmota(this, "FanSpeed 1")
}

def medium() {
    state.lastFanSpeed = 2
    parent.callTasmota(this, "FanSpeed 2")
}

def high() {
    state.lastFanSpeed = 3
    parent.callTasmota(this, "FanSpeed 3")
}

def refresh(dni=null) {
    def lastRefreshed = state.lastRefreshed
    if (lastRefreshed && (now() - lastRefreshed < 5000)) return
    state.lastRefreshed = now()

    def healthState = parent.childSetting(device.id, "health_state")
    if (healthState == true) {
        // Mark device as "always online"
        sendEvent(name: "switch", value: device.currentValue("switch"))
        childDevices[0]?.sendEvent(name: "switch", value: childDevices[0]?.currentValue("switch"))
    }

    // Check version every 30m
    def lastCheckedVersion = state.lastCheckedVersion
    if (!lastCheckedVersion || (lastCheckedVersion && (now() - lastCheckedVersion > (30 * 60 * 1000)))) {
        parent.callTasmota(this, "Status 2")
    }

    def actualDeviceNetworkId = device.deviceNetworkId
    if (state.dni == null || state.dni == "" || actualDeviceNetworkId != state.dni) {
        parent.callTasmota(this, "Status 5")
    }
    parent.callTasmota(this, "Status 11")
}

def ping() {
    refresh()
}

def childOn(dni) {
    parent.callTasmota(this, "POWER1 1")
}

def childOff(dni) {
    parent.callTasmota(this, "POWER1 0")
}

def childTurnOff() {
    childDevices[0]?.sendEvent(name: "switch", value: "off")
}