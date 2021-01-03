/**
 *  Tasmota - RGBW Light
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

import groovy.transform.Field

@Field final IntRange PERCENT_RANGE = (0..100)
@Field final IntRange COLOR_TEMP_RANGE = (2000..6536) // 153..500 (2000..6536)
@Field final Integer  COLOR_TEMP_DEFAULT = COLOR_TEMP_RANGE.getFrom() + ((COLOR_TEMP_RANGE.getTo() - COLOR_TEMP_RANGE.getFrom())/2)

String driverVersion() { return "20201205" }
metadata {
    definition (name: "Tasmota RGBW Light", namespace: "hongtat", author: "HongTat Tan", ocfDeviceType: "oic.d.light", vid: "dd2262b5-9c4a-3152-ba18-5deef6c24cdb", mnmn: "SmartThingsCommunity") {
        capability "Health Check"
        capability "Actuator"
        capability "Sensor"
        capability "Light"

        capability "Switch"
        capability "Switch Level"
        capability "Color Control"
        capability "Color Temperature"
        capability "Refresh"
        capability "Signal Strength"

        attribute "colorName", "string"

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
            input(title: "", description: "Tasmota RGBW Light v${driverVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#FFFFFF", nextState:"turningOn"
                attributeState "turningOn", label:'Turning On', icon:"st.switches.light.off", backgroundColor:"#FFFFFF", nextState:"on"
                attributeState "turningOff", label:'Turning Off', icon:"st.switches.light.on", backgroundColor:"#00A0DC", nextState:"off"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action: "setLevel", range:"(0..100)"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action: "setColor"
            }
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 1, inactiveLabel: false, range: "(2000..6536)") {
            state "colorTemperature", action: "setColorTemperature"
        }

        valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorName", label: '${currentValue}'
        }

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh", icon: "st.secondary.refresh"
        }

        main(["switch"])
        details(["switch", "colorTempSliderControl", "colorName", "refresh"])
    }
}

def installed() {
    sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "level", value: 100, unit: "%", displayed: false)
    sendEvent(name: "colorTemperature", value: COLOR_TEMP_DEFAULT, displayed: false)
    sendEvent(name: "color", value: "#000000", displayed: false)
    sendEvent(name: "hue", value: 0, displayed: false)
    sendEvent(name: "saturation", value: 0, displayed: false)
    sendEvent(name: "lqi", value: 0, displayed: false)
    sendEvent(name: "rssi", value: 0, displayed: false)
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

    def syncFrequency = (parent.generalSetting("frequency") ?: 'Every 1 minute').replace('Every ', 'Every').replace(' minute', 'Minute').replace(' hour', 'Hour')
    try {
        "run$syncFrequency"(refresh)
    } catch (all) { }
    sendEvent(name: "checkInterval", value: parent.checkInterval(), displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])

    parent.callTasmota(this, "Status 5")
    parent.callTasmota(this, "Backlog Rule1 ON Power#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER\":\"%value%\"},\"cb\":\"Status 11\"} ENDON ON Power1#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"POWER1\":\"%value%\"},\"cb\":\"Status 11\"} ENDON ON Dimmer#state DO WebSend ["+device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")+"] /?json={\"StatusSTS\":{\"Dimmer\":\"%value%\"},\"cb\":\"Status 11\"} ENDON;Rule1 1")
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
    log.debug "json: ${json}"
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

        // HSBColor
        def jsonHSBColor = (json?.StatusSTS?.HSBColor != null) ? (json?.StatusSTS?.HSBColor) : ((json?.HSBColor != null)? json?.HSBColor : null)
        if (jsonHSBColor != null) {
            def hsbColor = jsonHSBColor?.tokenize(",")

            hsbColor[0] = (Math.round((hsbColor[0] as Integer) / 3.6)) as Integer
            hsbColor[1] = hsbColor[1] as Integer
            hsbColor[2] = hsbColor[2] as Integer

            String rgbHex = colorUtil.hsvToHex(hsbColor[0], hsbColor[1])

            events << sendEvent(name: "hue", value: hsbColor[0], displayed: false)
            events << sendEvent(name: "saturation", value: hsbColor[1], displayed: false)
            events << sendEvent(name: "level", value: hsbColor[2])
            events << sendEvent(name: "color", value: rgbHex, displayed: false)
            log.debug "HSBColor: '${hsbColor[0]}, ${hsbColor[1]}, ${hsbColor[2]}', rgb: '${rgbHex}'"
        }

        // CT
        def jsonCT = (json?.StatusSTS?.CT != null) ? (json?.StatusSTS?.CT) : ((json?.CT != null)? json?.CT : null)
        if (jsonCT != null) {
            def mired = jsonCT?.toInteger()
            Integer kelvin = Math.round(1000000/mired)
            setGenericName(kelvin)
            events << sendEvent(name: "colorTemperature", value: kelvin, displayed: false)
            log.debug "colorTemperature: '$kelvin'"
        }

        // Dimmer
        def jsonDimmer = (json?.StatusSTS?.Dimmer != null) ? (json?.StatusSTS?.Dimmer) : ((json?.Dimmer != null)? json?.Dimmer : null)
        if (jsonDimmer != null) {
            def level = jsonDimmer?.toInteger()
            if (level >= 0 && level <= 100) {
                events << sendEvent(name: "level", value: level == 99 ? 100 : level)
            }
            log.debug "Dimmer: '$level'"
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

def setLevel(levelPercent, rate = null) {
    Integer boundedPercent = boundInt(levelPercent, PERCENT_RANGE)
    if (boundedPercent > 0) {
        sendEvent(name: "switch", value: "on")
    } else {
        sendEvent(name: "switch", value: "off")
    }
    log.debug "setLevel >> value: ${boundedPercent}%"
    parent.callTasmota(this, "DIMMER " + boundedPercent)
}

def setGenericName(value){
    if (value != null) {
        def genericName = "White"
        if (value < 3300) {
            genericName = "Soft White"
        } else if (value < 4150) {
            genericName = "Moonlight"
        } else if (value <= 5000) {
            genericName = "Cool White"
        } else if (value >= 5000) {
            genericName = "Daylight"
        }
        sendEvent(name: "colorName", value: genericName)
    }
}

def setColorTemperature(kelvin) {
    Integer mired = Math.round(1000000 / kelvin)
    log.debug "kelvin:  ${kelvin}, mired: ${mired}"
    sendEvent(name: "colorTemperature", value: kelvin, displayed: false)
    parent.callTasmota(this, "CT ${mired}")
}

def setSaturation(saturationPercent) {
    Integer currentHue = device.currentValue("hue")
    setColor(currentHue, saturationPercent)
}

def setHue(huePercent) {
    Integer currentSaturation = device.currentValue("saturation")
    setColor(huePercent, currentSaturation)
}

/**
 * setColor variant accepting discrete hue and saturation percentages
 * @param Integer huePercent            percentace of hue 0-100
 * @param Integer saturationPercent     percentage of saturtion 0-100
 */
def setColor(Integer huePercent, Integer saturationPercent) {
    Map colorHSMap = buildColorHSMap(huePercent, saturationPercent)
    setColor(colorHSMap) // call the capability version method overload
}

/**
 * setColor overload which accepts a hex RGB string
 * @param String hex    RGB color donoted as a hex string in format #1F1F1F
 */
def setColor(String rgbHex) {
    if (hex == "#000000") {
        off()
    } else {
        List hsvList = colorUtil.hexToHsv(rgbHex)
        Map colorHSMap = buildColorHSMap(hsvList[0], hsvList[1])
        setColor(colorHSMap) // call the capability version method overload
    }
}

/**
 * setColor as defined by the Color Control capability
 * even if we had a hex RGB value before, we convert back to it from hue and sat percentages
 * @param colorHSMap
 */
def setColor(Map colorHSMap) {
    Integer boundedHue = boundInt(colorHSMap?.hue?:0, PERCENT_RANGE)
    Integer boundedSaturation = boundInt(colorHSMap?.saturation?:0, PERCENT_RANGE)
    String rgbHex = colorUtil.hsvToHex(boundedHue, boundedSaturation)

    Integer tasmotaHue = boundedHue*3.6

    sendEvent(name: "hue", value: boundedHue, displayed: false)
    sendEvent(name: "saturation", value: boundedSaturation, displayed: false)
    sendEvent(name: "color", value: rgbHex, displayed: false)
    parent.callTasmota(this, "Scheme 0")
    parent.callTasmota(this, "HSBColor ${tasmotaHue},${boundedSaturation},${device.currentValue('level')}")
}

def poll() {
    refresh()
}

def ping() {
    refresh()
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

private Map buildColorHSMap(hue, saturation) {
    Map colorHSMap = [hue: 0, saturation: 0]
    try {
        colorHSMap.hue = hue.toFloat().toInteger()
        colorHSMap.saturation = saturation.toFloat().toInteger()
    } catch (NumberFormatException nfe) {
        log.warn "Couldn't transform one of hue ($hue) or saturation ($saturation) to integers: $nfe"
    }
    return colorHSMap
}

/**
 * Ensure an integer value is within the provided range, or set it to either extent if it is outside the range.
 * @param Number value         The integer to evaluate
 * @param IntRange theRange     The range within which the value must fall
 * @return Integer
 */
private Integer boundInt(Number value, IntRange theRange) {
    value = Math.max(theRange.getFrom(), value)
    value = Math.min(theRange.getTo(), value)
    return value.toInteger()
}