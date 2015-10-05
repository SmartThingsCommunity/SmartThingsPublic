/**
 *  Keen Home Smart Vent
 *
 *  Author: Keen Home
 *  Date: 2015-06-23
 */

metadata {
    definition (name: "Keen Home Smart Vent", namespace: "Keen Home", author: "Gregg Altschul") {
        capability "Switch Level"
        capability "Switch"
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Battery"

        command "getLevel"
        command "getOnOff"
        command "getPressure"
        command "getBattery"
        command "getTemperature"
        command "setZigBeeIdTile"

        fingerprint endpoint: "1",
        profileId: "0104",
        inClusters: "0000,0001,0003,0004,0005,0006,0008,0020,0402,0403,0B05,FC01,FC02",
        outClusters: "0019"
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
            state "on", action:"switch.off", icon:"st.vents.vent-open-text", backgroundColor:"#53a7c0"
            state "off", action:"switch.on", icon:"st.vents.vent-closed", backgroundColor:"#ffffff"
            state "obstructed", action: "switch.off", icon:"st.vents.vent-closed", backgroundColor:"#ff0000"
        }
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
            state "level", action:"switch level.setLevel"
        }
        standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false) {
            state "temperature", label:'${currentValue}°',
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
            state "battery", label: 'Battery \n${currentValue}%', backgroundColor:"#ffffff"
        }
        valueTile("zigbeeId", "device.zigbeeId", inactiveLabel: true, decoration: "flat") {
            state "serial", label:'${currentValue}', backgroundColor:"#ffffff"
        }
        main "switch"
        details(["switch","refresh","temperature","levelSliderControl","battery"])
    }
}

/**** PARSE METHODS ****/
def parse(String description) {
    log.debug "description: $description"

    Map map = [:]
    if (description?.startsWith('catchall:')) {
        map = parseCatchAllMessage(description)
    }
    else if (description?.startsWith('read attr -')) {
        map = parseReportAttributeMessage(description)
    }
    else if (description?.startsWith('temperature: ') || description?.startsWith('humidity: ')) {
        map = parseCustomMessage(description)
    }
    else if (description?.startsWith('on/off: ')) {
        map = parseOnOffMessage(description)
    }

    log.debug "Parse returned $map"
    return map ? createEvent(map) : null
}

private Map parseCatchAllMessage(String description) {
    log.debug "parseCatchAllMessage"

    def cluster = zigbee.parse(description)
    log.debug "cluster: ${cluster}"
    if (shouldProcessMessage(cluster)) {
        log.debug "processing message"
        switch(cluster.clusterId) {
            case 0x0001:
                return makeBatteryResult(cluster.data.last())
                break

            case 0x0402:
                // temp is last 2 data values. reverse to swap endian
                String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
                def value = convertTemperatureHex(temp)
                return makeTemperatureResult(value)
                break

            case 0x0006:
                return makeOnOffResult(cluster.data[-1])
                break
        }
    }

    return [:]
}

private boolean shouldProcessMessage(cluster) {
    // 0x0B is default response indicating message got through
    // 0x07 is bind message
    if (cluster.profileId != 0x0104 ||
        cluster.command == 0x0B ||
        cluster.command == 0x07 ||
        (cluster.data.size() > 0 && cluster.data.first() == 0x3e)) {
        return false
    }

    return true
}

private Map parseReportAttributeMessage(String description) {
    log.debug "parseReportAttributeMessage"

    Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
    log.debug "Desc Map: $descMap"

    if (descMap.cluster == "0006" && descMap.attrId == "0000") {
        return makeOnOffResult(Int.parseInt(descMap.value));
    }
    else if (descMap.cluster == "0008" && descMap.attrId == "0000") {
        return makeLevelResult(descMap.value)
    }
    else if (descMap.cluster == "0402" && descMap.attrId == "0000") {
        def value = convertTemperatureHex(descMap.value)
        return makeTemperatureResult(value)
    }
    else if (descMap.cluster == "0001" && descMap.attrId == "0021") {
        return makeBatteryResult(Integer.parseInt(descMap.value, 16))
    }
    else if (descMap.cluster == "0403" && descMap.attrId == "0020") {
        return makePressureResult(Integer.parseInt(descMap.value, 16))
    }
    else if (descMap.cluster == "0000" && descMap.attrId == "0006") {
        return makeSerialResult(new String(descMap.value.decodeHex()))
    }

    // shouldn't get here
    return [:]
}

private Map parseCustomMessage(String description) {
    Map resultMap = [:]
    if (description?.startsWith('temperature: ')) {
        // log.debug "${description}"
        // def value = zigbee.parseHATemperatureValue(description, "temperature: ", getTemperatureScale())
        // log.debug "split: " + description.split(": ")
        def value = Double.parseDouble(description.split(": ")[1])
        // log.debug "${value}"
        resultMap = makeTemperatureResult(convertTemperature(value))
    }
    return resultMap
}

private Map parseOnOffMessage(String description) {
    Map resultMap = [:]
    if (description?.startsWith('on/off: ')) {
        def value = Integer.parseInt(description - "on/off: ")
        resultMap = makeOnOffResult(value)
    }
    return resultMap
}

private Map makeOnOffResult(rawValue) {
    log.debug "makeOnOffResult: ${rawValue}"
    def linkText = getLinkText(device)
    def value = rawValue == 1 ? "on" : "off"
    return [
        name: "switch",
        value: value,
        descriptionText: "${linkText} is ${value}"
    ]
}

private Map makeLevelResult(rawValue) {
    def linkText = getLinkText(device)
    // log.debug "rawValue: ${rawValue}"
    def value = Integer.parseInt(rawValue, 16)
    def rangeMax = 254

    if (value == 255) {
        log.debug "obstructed"
        // Just return here. Once the vent is power cycled
        // it will go back to the previous level before obstruction.
        // Therefore, no need to update level on the display.
        return [
            name: "switch",
            value: "obstructed",
            descriptionText: "${linkText} is obstructed. Please power cycle."
        ]
    } else if ( device.currentValue("switch") == "obstructed" &&
                value == 254)  {
        // When the device is reset after an obstruction, the switch
        // state will be obstructed and the value coming from the device
        // will be 254. Since we're not using heating/cooling mode from
        // the device type handler, we need to bump it down to the lower
        // (cooling) range
        sendEvent(makeOnOffResult(1)) // clear the obstructed switch state
        value = rangeMax
    }
    // else if (device.currentValue("switch") == "off") {
    //     sendEvent(makeOnOffResult(1)) // turn back on if in off state
    // }


    // log.debug "pre-value: ${value}"
    value = Math.floor(value / rangeMax * 100)
    // log.debug "post-value: ${value}"

    return [
        name: "level",
        value: value,
        descriptionText: "${linkText} level is ${value}%"
    ]
}

private Map makePressureResult(rawValue) {
    log.debug 'makePressureResut'
    def linkText = getLinkText(device)

    def pascals = rawValue / 10
    def result = [
        name: 'pressure',
        descriptionText: "${linkText} pressure is ${pascals}Pa",
        value: pascals
    ]

    return result
}

private Map makeBatteryResult(rawValue) {
    // log.debug 'makeBatteryResult'
    def linkText = getLinkText(device)

    // log.debug
    [
        name: 'battery',
        value: rawValue,
        descriptionText: "${linkText} battery is at ${rawValue}%"
    ]
}

private Map makeTemperatureResult(value) {
    // log.debug 'makeTemperatureResult'
    def linkText = getLinkText(device)

    // log.debug "tempOffset: ${tempOffset}"
    if (tempOffset) {
        def offset = tempOffset as int
        // log.debug "offset: ${offset}"
        def v = value as int
        // log.debug "v: ${v}"
        value = v + offset
        // log.debug "value: ${value}"
    }

    return [
        name: 'temperature',
        value: "" + value,
        descriptionText: "${linkText} is ${value}°${temperatureScale}",
    ]
}

/**** HELPER METHODS ****/
private def convertTemperatureHex(value) {
    // log.debug "convertTemperatureHex(${value})"
    def celsius = Integer.parseInt(value, 16).shortValue() / 100
    // log.debug "celsius: ${celsius}"

    return convertTemperature(celsius)
}

private def convertTemperature(celsius) {
    // log.debug "convertTemperature()"

    if(getTemperatureScale() == "C"){
        return celsius
    } else {
        def fahrenheit = Math.round(celsiusToFahrenheit(celsius) * 100) /100
        // log.debug "converted to F: ${fahrenheit}"
        return fahrenheit
    }
}

private def makeSerialResult(serial) {
    log.debug "makeSerialResult: " + serial

    def linkText = getLinkText(device)
    sendEvent([
        name: "serial",
        value: serial,
        descriptionText: "${linkText} has serial ${serial}" ])
    return [
        name: "serial",
        value: serial,
        descriptionText: "${linkText} has serial ${serial}" ]
}
/**** COMMAND METHODS ****/
// def mfgCode() {
// 	 ["zcl mfg-code 0x115B", "delay 200"]
// }

def on() {
    log.debug "on()"
    sendEvent(makeOnOffResult(1))
    "st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
    log.debug "off()"
    sendEvent(makeOnOffResult(0))
    "st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

// does this work?
def toggle() {
    log.debug "toggle()"

    "st cmd 0x${device.deviceNetworkId} 1 6 2 {}"
}

def setLevel(value) {
    log.debug "setting level: ${value}"

    def linkText = getLinkText(device)

    sendEvent(name: "level", value: value)
    if (value > 0) {
        sendEvent(name: "switch", value: "on", descriptionText: "${linkText} is on by setting a level")
    }
    else {
        sendEvent(name: "switch", value: "off", descriptionText: "${linkText} is off by setting level to 0")
    }
    def rangeMax = 254
    def computedLevel = Math.round(value * rangeMax / 100)
    log.debug "computedLevel: ${computedLevel}"

    def level = new BigInteger(computedLevel.toString()).toString(16)
    log.debug "level: ${level}"

    if (level.size() < 2){
        level = '0' + level
    }

    "st cmd 0x${device.deviceNetworkId} 1 8 4 {${level} 0000}"
}


def getOnOff() {
    log.debug "getOnOff()"

    ["st rattr 0x${device.deviceNetworkId} 1 0x0006 0"]
}

def getPressure() {
    log.debug "getPressure()"
    [
        "zcl mfg-code 0x115B", "delay 200",
        "zcl global read 0x0403 0x20", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 200"
    ]
}

def getLevel() {
    log.debug "getLevel()"
    // rattr = read attribute
    // 0x${} = device net id
    // 1 = endpoint
    // 8 = cluster id (level control, in this case)
    // 0 = attribute within cluster
    // sendEvent(name: "level", value: value)
    ["st rattr 0x${device.deviceNetworkId} 1 0x0008 0x0000"]
}

def getTemperature() {
    log.debug "getTemperature()"

    ["st rattr 0x${device.deviceNetworkId} 1 0x0402 0"]
}

def getBattery() {
    log.debug "getBattery()"

    ["st rattr 0x${device.deviceNetworkId} 1 0x0001 0x0021"]
}

def setZigBeeIdTile() {
    log.debug "setZigBeeIdTile() - ${device.zigbeeId}"

    def linkText = getLinkText(device)

    sendEvent([
        name: "zigbeeId",
        value: device.zigbeeId,
        descriptionText: "${linkText} has zigbeeId ${device.zigbeeId}" ])
	return [
        name: "zigbeeId",
        value: device.zigbeeId,
        descriptionText: "${linkText} has zigbeeId ${device.zigbeeId}" ]
}

def refresh() {
	getOnOff() +
    getLevel() +
    getTemperature() +
    getPressure() +
    getBattery()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

def configure() {
    log.debug "CONFIGURE"
    log.debug "zigbeeId: ${device.hub.zigbeeId}"

    setZigBeeIdTile()

    def configCmds = [
        // binding commands
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0006 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0008 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0402 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0403 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0001 {${device.zigbeeId}} {}", "delay 500",
        
        // configure report commands
        // [cluster] [attr] [type] [min-interval] [max-interval] [min-change]

        // mike 2015/06/22: preconfigured; see tech spec
        // vent on/off state - type: boolean, change: 1
        // "zcl global send-me-a-report 6 0 0x10 5 60 {01}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        // mike 2015/06/22: preconfigured; see tech spec
        // vent level - type: int8u, change: 1
        // "zcl global send-me-a-report 8 0 0x20 5 60 {01}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        // mike 2015/06/22: temp and pressure reports are preconfigured, but
        //   we'd like to override their settings for our own purposes
        // temperature - type: int16s, change: 0xA = 10 = 0.1C
        "zcl global send-me-a-report 0x0402 0 0x29 10 60 {0A00}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        // mike 2015/06/22: use new custom pressure attribute
        // pressure - type: int32u, change: 1 = 0.1Pa
        "zcl mfg-code 0x115B", "delay 200",
        "zcl global send-me-a-report 0x0403 0x20 0x22 10 60 {010000}", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 1500"

        // mike 2015/06/22: preconfigured; see tech spec
        // battery - type: int8u, change: 1
        // "zcl global send-me-a-report 1 0x21 0x20 60 3600 {01}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    ]

    return configCmds + refresh()
}
