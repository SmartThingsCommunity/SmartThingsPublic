/**
 *  Tasmota - Generic Switch
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
    definition(name: "Tasmota Generic Switch", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.switch", vid: "af06f1a5-45b6-39ee-86bb-7cbe15062491", mnmn: "SmartThingsCommunity") {
        capability "Actuator"
        capability "Health Check"
        capability "Switch"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Signal Strength"

        attribute "lastSeen", "string"
        attribute "version", "string"
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
            input(title: "", description: "Tasmota Generic Switch v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC"
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
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

        main "switch"
        details(["switch", "refresh", "lqi", "rssi"])
    }
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "switch", value: "off")
    log.debug "Installed"
    response(refresh())
}

def uninstalled() {
    sendEvent(name: "epEvent", value: "delete all", isStateChange: true, displayed: false, descriptionText: "Delete endpoint devices")
}

def updated() {
    initialize()
}

def initialize() {
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
    parent.callTasmota(this, "Backlog Rule1 ON Power#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER\":\"%value%\"}} ENDON ON Power1#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER1\":\"%value%\"}} ENDON ON Power2#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER2\":\"%value%\"}} ENDON ON Power3#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER3\":\"%value%\"}} ENDON ON Power4#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER4\":\"%value%\"}} ENDON;Rule1 1")
    parent.callTasmota(this, "Backlog Rule2 ON Power5#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER5\":\"%value%\"}} ENDON ON Power6#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER6\":\"%value%\"}} ENDON ON Power7#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER7\":\"%value%\"}} ENDON ON Power8#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER8\":\"%value%\"}} ENDON;Rule2 1")
    parent.callTasmota(this, "Status 8")
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

        // Power
        if (channel != null) {
            for (i in 0..channel) {
                def number = (i > 0) ? i : ""
                def power = (json?.StatusSTS?."POWER${number}" != null) ? (json?.StatusSTS?."POWER${number}") : ((json?."POWER${number}" != null) ? json?."POWER${number}" : null)

                def powerStatus = null
                if (power in ["ON", "1"]) {
                    powerStatus = "on"
                } else if (power in ["OFF", "0"]) {
                    powerStatus = "off"
                }
                if (powerStatus != null) {
                    if ((channel == 1) || (channel > 1 && i == 1)) {
                        events << sendEvent(name: "switch", value: powerStatus)
                    } else {
                        String childDni = "${device.deviceNetworkId}-ep$i"
                        def child = childDevices.find { it.deviceNetworkId == childDni }
                        child?.sendEvent(name: "switch", value: powerStatus)
                    }
                    log.debug "Switch $number: '$powerStatus'"
                }
            }
        }

        // Temperature, Humidity (Status 8)
        def resultTH = null
        if (json?.StatusSNS != null) {
            for (record in json.StatusSNS) {
                if (record.value instanceof Map && (record.value.containsKey("Humidity") || record.value.containsKey("Temperature"))) {
                    resultTH = record.value
                }
            }
            if (resultTH != null) {
                def childMessage = [:]
                if (resultTH.containsKey("Humidity")) {
                    childMessage.humidity = Math.round((resultTH.Humidity as Double) * 100) / 100
                }
                if (resultTH.containsKey("Temperature")) {
                    childMessage.temperature = resultTH.Temperature.toFloat()
                }
                if (json?.StatusSNS?.TempUnit != null) {
                    childMessage.tempUnit = json?.StatusSNS?.TempUnit
                }
                def child = childDevices.find { it.typeName == "Tasmota Child Temp/Humidity Sensor" }
                child?.parseEvents(200, childMessage)
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
    def channel = getDataValue("endpoints")?.toInteger()
    parent.callTasmota(this, "POWER" + ((channel == 1) ? "" : 1) + " 1")
}

def off() {
    def channel = getDataValue("endpoints")?.toInteger()
    parent.callTasmota(this, "POWER" + ((channel == 1) ? "" : 1) + " 0")
}

def refresh(dni=null) {
    def lastRefreshed = state.lastRefreshed
    if (lastRefreshed && (now() - lastRefreshed < 5000)) return
    state.lastRefreshed = now()

    // Check version every 30m
    def lastCheckedVersion = state.lastCheckedVersion
    if (!lastCheckedVersion || (lastCheckedVersion && (now() - lastCheckedVersion > (30 * 60 * 1000)))) {
        parent.callTasmota(this, "Status 2")
    }

    def actualDeviceNetworkId = device.deviceNetworkId
    if (state.dni == null || state.dni == "" || actualDeviceNetworkId != state.dni) {
        parent.callTasmota(this, "Status 5")
    }
    parent.callTasmota(this, "Status 8")
    parent.callTasmota(this, "Status 11")
}

def ping() {
    refresh()
}

def childOn(dni) {
    parent.callTasmota(this, "POWER" + parent.channelNumber(dni) + " 1")
}

def childOff(dni) {
    parent.callTasmota(this, "POWER" + parent.channelNumber(dni) + " 0")
}