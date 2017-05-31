// keen home smart vent
// http://www.keenhome.io
// SmartThings Device Handler v1.0.0

metadata {
    definition (name: "Keen Home Smart Vent", namespace: "Keen Home", author: "Keen Home") {
        capability "Switch Level"
        capability "Switch"
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Battery"
        capability "Health Check"

        command "getLevel"
        command "getOnOff"
        command "getPressure"
        command "getBattery"
        command "getTemperature"
        command "setZigBeeIdTile"
        command "clearObstruction"

        fingerprint endpoint: "1", profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0006,0008,0020,0402,0403,0B05,FC01,FC02", outClusters: "0019"
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
            state "on", action: "switch.off", icon: "st.vents.vent-open-text", backgroundColor: "#00a0dc"
            state "off", action: "switch.on", icon: "st.vents.vent-closed", backgroundColor: "#ffffff"
            state "obstructed", action: "clearObstruction", icon: "st.vents.vent-closed", backgroundColor: "#e86d13"
            state "clearing", action: "", icon: "st.vents.vent-closed", backgroundColor: "#ffffff"
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
    def value = Integer.parseInt(rawValue, 16)
    def rangeMax = 254

    // catch obstruction level
    if (value == 255) {
        log.debug "${linkText} is obstructed"
        // Just return here. Once the vent is power cycled
        // it will go back to the previous level before obstruction.
        // Therefore, no need to update level on the display.
        return [
            name: "switch",
            value: "obstructed",
            descriptionText: "${linkText} is obstructed. Please power cycle."
        ]
    }

    value = Math.floor(value / rangeMax * 100)

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
        unit: temperatureScale
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

// takes a level from 0 to 100 and translates it to a ZigBee move to level with on/off command
private def makeLevelCommand(level) {
    def rangeMax = 254
    def scaledLevel = Math.round(level * rangeMax / 100)
    log.debug "scaled level for ${level}%: ${scaledLevel}"

    // convert to hex string and pad to two digits
    def hexLevel = new BigInteger(scaledLevel.toString()).toString(16).padLeft(2, '0')

    "st cmd 0x${device.deviceNetworkId} 1 8 4 {${hexLevel} 0000}"
}

/**** COMMAND METHODS ****/
def on() {
    def linkText = getLinkText(device)
    log.debug "open ${linkText}"

    // only change the state if the vent is not obstructed
    if (device.currentValue("switch") == "obstructed") {
        log.error("cannot open because ${linkText} is obstructed")
        return
    }

    sendEvent(makeOnOffResult(1))
    "st cmd 0x${device.deviceNetworkId} 1 6 1 {}"
}

def off() {
    def linkText = getLinkText(device)
    log.debug "close ${linkText}"

    // only change the state if the vent is not obstructed
    if (device.currentValue("switch") == "obstructed") {
        log.error("cannot close because ${linkText} is obstructed")
        return
    }

    sendEvent(makeOnOffResult(0))
    "st cmd 0x${device.deviceNetworkId} 1 6 0 {}"
}

def clearObstruction() {
    def linkText = getLinkText(device)
    log.debug "attempting to clear ${linkText} obstruction"

    sendEvent([
        name: "switch",
        value: "clearing",
        descriptionText: "${linkText} is clearing obstruction"
    ])

    // send a move command to ensure level attribute gets reset for old, buggy firmware
    // then send a reset to factory defaults
    // finally re-configure to ensure reports and binding is still properly set after the rtfd
    [
        makeLevelCommand(device.currentValue("level")), "delay 500",
        "st cmd 0x${device.deviceNetworkId} 1 0 0 {}", "delay 5000"
    ] + configure()
}

def setLevel(value) {
    log.debug "setting level: ${value}"
    def linkText = getLinkText(device)

    // only change the level if the vent is not obstructed
    def currentState = device.currentValue("switch")

    if (currentState == "obstructed") {
        log.error("cannot set level because ${linkText} is obstructed")
        return
    }

    sendEvent(name: "level", value: value)
    if (value > 0) {
        sendEvent(name: "switch", value: "on", descriptionText: "${linkText} is on by setting a level")
    }
    else {
        sendEvent(name: "switch", value: "off", descriptionText: "${linkText} is off by setting level to 0")
    }

    makeLevelCommand(value)
}

def getOnOff() {
    log.debug "getOnOff()"

    // disallow on/off updates while vent is obstructed
    if (device.currentValue("switch") == "obstructed") {
        log.error("cannot update open/close status because ${getLinkText(device)} is obstructed")
        return []
    }

    ["st rattr 0x${device.deviceNetworkId} 1 0x0006 0"]
}

def getPressure() {
    log.debug "getPressure()"

    // using a Keen Home specific attribute in the pressure measurement cluster
    [
        "zcl mfg-code 0x115B", "delay 200",
        "zcl global read 0x0403 0x20", "delay 200",
        "send 0x${device.deviceNetworkId} 1 1", "delay 200"
    ]
}

def getLevel() {
    log.debug "getLevel()"

    // disallow level updates while vent is obstructed
    if (device.currentValue("switch") == "obstructed") {
        log.error("cannot update level status because ${getLinkText(device)} is obstructed")
        return []
    }

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

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    return refresh()
}

def configure() {
    log.debug "CONFIGURE"

    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    // get ZigBee ID by hidden tile because that's the only way we can do it
    setZigBeeIdTile()

    def configCmds = [
        // bind reporting clusters to hub
            //commenting out switch cluster bind as using wrapper onOffConfig of zigbee class
        //"zdo bind 0x${device.deviceNetworkId} 1 1 0x0006 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0008 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0402 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0403 {${device.zigbeeId}} {}", "delay 500",
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0001 {${device.zigbeeId}} {}", "delay 500"

        // configure report commands
        // zcl global send-me-a-report [cluster] [attr] [type] [min-interval] [max-interval] [min-change]

        // report with these parameters is preconfigured in firmware, can be overridden here
        // vent on/off state - type: boolean, change: 1
        // "zcl global send-me-a-report 6 0 0x10 5 60 {01}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        // report with these parameters is preconfigured in firmware, can be overridden here
        // vent level - type: int8u, change: 1
        // "zcl global send-me-a-report 8 0 0x20 5 60 {01}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        // report with these parameters is preconfigured in firmware, can be overridden here
        // temperature - type: int16s, change: 0xA = 10 = 0.1C
        // "zcl global send-me-a-report 0x0402 0 0x29 60 60 {0A00}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        // report with these parameters is preconfigured in firmware, can be overridden here
        // keen home custom pressure (tenths of Pascals) - type: int32u, change: 1 = 0.1Pa
        // "zcl mfg-code 0x115B", "delay 200",
        // "zcl global send-me-a-report 0x0403 0x20 0x22 60 60 {010000}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",

        // report with these parameters is preconfigured in firmware, can be overridden here
        // battery - type: int8u, change: 1
        // "zcl global send-me-a-report 1 0x21 0x20 60 3600 {01}", "delay 200",
        // "send 0x${device.deviceNetworkId} 1 1", "delay 1500",
    ]

    return configCmds + zigbee.onOffConfig() + refresh()
}
