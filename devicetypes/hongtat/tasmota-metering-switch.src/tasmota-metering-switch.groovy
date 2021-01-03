/**
 *  Tasmota - Metering Switch
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
    definition(name: "Tasmota Metering Switch", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.switch", vid: "73aa37e5-de20-30e6-b36f-16837d255356", mnmn: "SmartThingsCommunity") {
        capability "Energy Meter"
        capability "Actuator"
        capability "Switch"
        capability "Power Meter"
        capability "Voltage Measurement"
        capability "Refresh"
        capability "Configuration"
        capability "Sensor"
        capability "Light"
        capability "Health Check"
        capability "Signal Strength"
        capability "voicehouse43588.currentMeasurement"

        command "reset"

        attribute "lastSeen", "string"
        attribute "version", "string"
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
            input(title: "", description: "Tasmota Metering Switch v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    // tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState:"turningOff")
                attributeState("off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn")
                attributeState("turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff")
                attributeState("turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn")
            }
        }
        valueTile("power", "device.power", width: 2, height: 2) {
            state "default", label:'${currentValue} W'
        }
        valueTile("energy", "device.energy", width: 2, height: 2) {
            state "default", label:'${currentValue} kWh'
        }
        standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'reset kWh', action:"reset"
        }
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("lqi", "device.lqi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'LQI: ${currentValue}'
        }
        standardTile("rssi", "device.rssi", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "default", label: 'RSSI: ${currentValue}dBm'
        }
        main(["switch","power","energy"])
        details(["switch","power","energy","refresh","reset", "lqi", "rssi"])
    }
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "switch", value: "off")
    log.debug "Installed"
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
    parent.callTasmota(this, "Backlog Rule3 ON Tele-Energy#Power DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSNS\":{\"ENERGY\":{\"Power\":\"%value%\"}}} ENDON ON Tele-Energy#Total DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSNS\":{\"ENERGY\":{\"Total\":\"%value%\"}}} ENDON ON Tele-Energy#Voltage DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSNS\":{\"ENERGY\":{\"Voltage\":\"%value%\"}}} ENDON ON Tele-Energy#Current DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSNS\":{\"ENERGY\":{\"Current\":\"%value%\"}}} ENDON;Rule3 1;TelePeriod 10")
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
                        if (powerStatus == "off") {
                            events << sendEvent(name: "power", value: "0", unit: "W", isStateChange:true, displayed:false)
                        }
                    } else {
                        String childDni = "${device.deviceNetworkId}-ep$i"
                        def child = childDevices.find { it.deviceNetworkId == childDni }
                        child?.sendEvent(name: "switch", value: powerStatus)
                    }
                    log.debug "Switch $number: '$powerStatus'"
                }
            }
        }

        // Energy
        if (json?.StatusSNS != null) {
            if (json?.StatusSNS?.ENERGY?.Power != null && json?.StatusSNS?.ENERGY?.Power?.trim() != "") {
                events << sendEvent(name: "power", value: json?.StatusSNS?.ENERGY?.Power, unit: "W", isStateChange:true, displayed:false)
            }
            if (json?.StatusSNS?.ENERGY?.Total != null && json?.StatusSNS?.ENERGY?.Total?.trim() != "") {
                events << sendEvent(name: "energy", value: json?.StatusSNS?.ENERGY?.Total, unit: "kWh", isStateChange:true, displayed:false)
            }
            if (json?.StatusSNS?.ENERGY?.Voltage != null && json?.StatusSNS?.ENERGY?.Voltage?.trim() != "") {
                events << sendEvent(name: "voltage", value: json?.StatusSNS?.ENERGY?.Voltage, unit: "V", isStateChange:true, displayed:false)
            }
            if (json?.StatusSNS?.ENERGY?.Current != null && json?.StatusSNS?.ENERGY?.Current?.trim() != "") {
                events << sendEvent(name: "current", value: json?.StatusSNS?.ENERGY?.Current, unit: "A", isStateChange:true, displayed:false)
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
    parent.callTasmota(this, "Power 1")
}

def off() {
    parent.callTasmota(this, "Power 0")
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

def childOn(dni) {
    parent.callTasmota(this, "POWER" + parent.channelNumber(dni) + " 1")
}

def childOff(dni) {
    parent.callTasmota(this, "POWER" + parent.channelNumber(dni) + " 0")
}

def poll() {
    refresh()
}

def reset() {
    parent.callTasmota(this, "Backlog EnergyReset1 0; EnergyReset2 0; EnergyReset3 0;")
}