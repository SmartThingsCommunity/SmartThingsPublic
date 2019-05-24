import physicalgraph.zigbee.zcl.DataType

// keen home smart vent
// http://www.keenhome.io
// SmartThings Device Handler v1.0.0

metadata {
    definition (name: "Keen Home Smart Vent", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.vent") {
        capability "Switch Level"
        capability "Switch"
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Battery"
        capability "Health Check"

        command "clearObstruction"

        fingerprint profileId: "0104", inClusters: "0000,0001,0003,0004,0005,0006,0008,0020,0402,0403,0B05,FC01,FC02", outClusters: "0019"
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
                // Celsius
                [value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 28, color: "#f1d801"],
                [value: 35, color: "#d04e00"],
                [value: 37, color: "#bc2323"],
                // Fahrenheit
                [value: 40, color: "#153591"],
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
        main "switch"
        details(["switch","refresh","temperature","levelSliderControl","battery"])
    }
}

/**** PARSE METHODS ****/
def parse(String description) {
    log.debug "description: $description"
    def event = zigbee.getEvent(description)
    if (!event) {
        Map descMap = zigbee.parseDescriptionAsMap(description)
        if (descMap?.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.attrInt == 0x0021) {
            event = getBatteryPercentageResult(Integer.parseInt(descMap.value, 16))
        }
    } else if (event.name == "level" && event.value > 100) {
        event.name = "switch"
        event.value = "obstructed"
    } else if (event.name == "level" && event.value > 0 && device.currentValue("switch") == "off") {
        sendEvent([name: "switch", value: "on"])
    }

    log.debug "parsed event: $event"
    createEvent(event)
}

private Map getBatteryPercentageResult(rawValue) {
    // reports raw percentage, not 2x
    def result = [:]

    if (0 <= rawValue && rawValue <= 100) {
        result.name = 'battery'
        result.translatable = true
        result.descriptionText = "$device.displayName battery was ${rawValue}%"
        result.value = Math.round(rawValue)
    }

    return result
}


/**** COMMAND METHODS ****/
def on() {
    def cmds = []
    if (isObstructed()) {
        cmds << clearObstruction()
        cmds << "delay 2000"
    }
    cmds << zigbee.on()
}

def open() {
    on()
}

def off() {
    def cmds = []
    if (isObstructed()) {
        cmds << clearObstruction()
        cmds << "delay 2000"
    }
    cmds << zigbee.off()
}

def close() {
    off()
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
        zigbee.setLevel(device.currentValue("level")), "delay 500",
        zigbee.command(zigbee.BASIC_CLUSTER, 0x00), "delay 5000"
    ] + configure()
}

def setLevel(value, rate = null) {
    log.debug "setting level: ${value}"
    def cmds = []
    if (isObstructed()) {
        cmds << clearObstruction()
        cmds << "delay 2000"
    }
    cmds << zigbee.setLevel(value)
    cmds << "delay 1000"
    cmds << zigbee.levelRefresh()
    cmds
}

def refresh() {
    zigbee.onOffRefresh() +
    zigbee.levelRefresh() +
    zigbee.readAttribute(zigbee.TEMPERATURE_MEASUREMENT_CLUSTER, 0x0000) +
    zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021)
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
    refresh()
}

def configure() {
    log.debug "CONFIGURE"

    // Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
    // enrolls with default periodic reporting until newer 5 min interval is confirmed
    sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

    def cmds = [
            zigbee.temperatureConfig(30, 300) +
            zigbee.addBinding(zigbee.ONOFF_CLUSTER) +
            zigbee.addBinding(zigbee.LEVEL_CONTROL_CLUSTER) +
            zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x0021, DataType.UINT8, 600, 21600, 0x01) // battery precentage
    ]

    return delayBetween(cmds) + refresh()
}


private boolean isObstructed() {
    def linkText = getLinkText(device)
    def currentState = device.currentValue("switch")

    if (currentState == "obstructed") {
        log.error("cannot set level because ${linkText} is obstructed")
        return true
    }
    return false
}
