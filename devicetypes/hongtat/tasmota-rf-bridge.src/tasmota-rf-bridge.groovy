/**
 *  Tasmota - RF Bridge
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

String driverVersion() { return "20201205" }
metadata {
    definition (name: "Tasmota RF Bridge", namespace: "hongtat", author: "HongTat Tan", vid: "208a0e78-3620-3eb8-8381-6066df487473", mnmn: "SmartThingsCommunity") {
        capability "Notification"
        capability "Refresh"
        capability "Health Check"
        capability "Signal Strength"
        capability "voicehouse43588.lastEvent"
        capability "voicehouse43588.lastReceived"
        capability "voicehouse43588.deviceStatus"

        attribute "rfKey", "number"
        attribute "rfData", "string"
        attribute "lastEvent", "string"
        attribute "lastSeen", "string"
        attribute "version", "string"

        command "refresh"
        command "rfSend"
    }

    // simulator metadata
    simulator {
    }

    preferences {
        section {
            input(title: "Device Settings",
                    description: "To view/update this settings, go to the Tasmota (Connect) SmartApp and select this device.",
                    displayDuringSetup: false,
                    type: "paragraph",
                    element: "paragraph")
            input(title: "", description: "Tasmota RF Bridge v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    // tile definitions
    tiles(scale: 2) {
        standardTile("icon", "icon", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "default", label: "RF Bridge", action: "", icon: "st.harmony.harmony-hub-icon", backgroundColor: "#FFFFFF"
        }
        valueTile("lastEvent", "device.lastEvent", decoration:"flat", inactiveLabel: false, width: 6, height: 2) {
            state "lastEvent", label:'Last Event:\n${currentValue}'
        }
        valueTile("rfKey", "device.rfKey", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
            state "rfKey", label:'RF Key:\n${currentValue}'
        }
        valueTile("rfData", "device.rfData", inactiveLabel: false, decoration: "flat", width: 3, height: 2) {
            state "rfData", label: 'RF Data:\n${currentValue}'
        }
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("lqi", "device.lqi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'LQI: ${currentValue}'
        }
        standardTile("rssi", "device.rssi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'RSSI: ${currentValue}dBm'
        }

        main(["icon"])
        details(["lastEvent","rfKey","rfData","refresh"])
    }
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    response(refresh())
}

def updated() {
    initialize()
}

def initialize() {
    if (device.hub == null) {
        log.error "Hub is null, must set the hub in the device settings so we can get local hub IP and port"
        return
    }

    def syncFrequency = (parent.generalSetting("frequency") ?: 'Every 1 minute').replace('Every ', 'Every').replace(' minute', 'Minute').replace(' hour', 'Hour')
    try {
        "run$syncFrequency"(refresh)
    } catch (all) { }
    sendEvent(name: "checkInterval", value: parent.checkInterval(), displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])

    parent.callTasmota(this, "Status 5")
    parent.callTasmota(this, "Backlog Rule1 ON RfReceived#Data DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"RfReceived\":{\"Data\":\"%value%\"}} ENDON ON RfReceived#RfKey DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"RfReceived\":{\"RfKey\":\"%value%\"}} ENDON ON RfRaw#Data DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"RfRaw\":{\"Data\":\"%value%\"}} ENDON;Rule1 1")
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
        String rfData = null
        String rfKey = null

        // Message
        def message = [:]
        message.dst = "*"
        message.lastSeen = now

        // RfReceived
        if (json?.RfReceived != null) {
            if (json?.RfReceived?.Data != null && json?.RfReceived?.Data.toUpperCase() != 'NONE') {
                rfData = json.RfReceived.Data.toUpperCase()
                message.rfData = rfData
                events << sendEvent(name: "rfData", value: rfData, isStateChange: true, displayed: false)
                events << sendEvent(name: "lastEvent", value: now, isStateChange: true, displayed: false)
                events << sendEvent(name: "lastReceived", value: rfData, isStateChange: true)
                log.debug "RfReceived#Data: '${rfData}'"
            }
            if (json?.RfReceived?.RfKey != null && json?.RfReceived?.RfKey != 'NONE') {
                rfKey = json.RfReceived.RfKey
                message.rfKey = rfKey
                events << sendEvent(name: "rfKey", value: rfKey, isStateChange:true, displayed: false)
                log.debug "RfReceived#RfKey: '${rfKey}'"
            }
        }
        // RfRaw
        if (json?.RfRaw != null) {
            if (json?.RfRaw?.Data != null && json?.RfRaw?.Data.toUpperCase() != 'AAA055') {
                String rawCode = json.RfRaw.Data.toUpperCase()
                if ((rawCode.startsWith("AA B1 ") && rawCode.endsWith(" 55"))) {
                    rfData = rawCode.split(" ")[-2]
                    message.rfData = rfData
                    events << sendEvent(name: "rfData", value: rfData, isStateChange: true, displayed: false)
                    events << sendEvent(name: "lastEvent", value: now, isStateChange: true, displayed: false)
                    events << sendEvent(name: "lastReceived", value: rfData, isStateChange: true)
                    log.debug "RfRaw#Data: '${rfData}'"
                }
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
            state.dni = dni
        }

        // Signal Strength
        if (json?.StatusSTS?.Wifi != null) {
            message.Wifi = [RSSI : json?.StatusSTS?.Wifi.RSSI, Signal: json?.StatusSTS?.Wifi.Signal]
            events << sendEvent(name: "lqi", value: json?.StatusSTS?.Wifi.RSSI, displayed: false)
            events << sendEvent(name: "rssi", value: json?.StatusSTS?.Wifi.Signal, displayed: false)
        }

        // Version
        if (json?.StatusFWR?.Version != null) {
            state.lastCheckedVersion = new Date().getTime()
            events << sendEvent(name: "version", value: json.StatusFWR.Version, displayed: false)
        }

        // Cross-device messaging
        events << sendEvent(name: "messenger", value: message.encodeAsJson(), isStateChange: true, displayed:false)

        // Call back
        if (json?.cb != null) {
            parent.callTasmota(this, json.cb)
        }

        // Last seen
        events << sendEvent(name: "lastSeen", value: now, displayed: false)
    }
    return events
}

def rfSend(String rf) {
    parent.callTasmota(this, rf)
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
    parent.callTasmota(this, "Status 11")
}

def ping() {
    refresh()
}

def poll() {
    refresh()
}