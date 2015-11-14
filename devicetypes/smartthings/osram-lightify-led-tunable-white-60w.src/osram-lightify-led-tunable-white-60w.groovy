/*
    Osram Tunable White 60 A19 bulb

    Osram bulbs have a firmware issue causing it to forget its dimming level when turned off (via commands). Handling
    that issue by using state variables
*/

//DEPRECATED - Using the generic DTH for this device. Users need to be moved before deleting this DTH

metadata {
    definition (name: "OSRAM LIGHTIFY LED Tunable White 60W", namespace: "smartthings", author: "SmartThings") {

        capability "Color Temperature"
        capability "Actuator"
        capability "Switch"
        capability "Switch Level"
        capability "Configuration"
        capability "Refresh"
        capability "Sensor"

        attribute "colorName", "string"

        // indicates that device keeps track of heartbeat (in state.heartbeat)
        attribute "heartbeat", "string"
        

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
            state "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        controlTile("colorTempSliderControl", "device.colorTemperature", "slider", height: 1, width: 2, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat") {
            state "colorTemperature", label: '${currentValue} K'
        }
        valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat") {
            state "colorName", label: '${currentValue}'
        }


        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
            state "level", action:"switch level.setLevel"
        }
        valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
            state "level", label: 'Level ${currentValue}%'
        }


        main(["switch"])
        details(["switch", "refresh", "colorName", "levelSliderControl", "level", "colorTempSliderControl", "colorTemp"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    //log.trace description

    // save heartbeat (i.e. last time we got a message from device)
    state.heartbeat = Calendar.getInstance().getTimeInMillis()

    if (description?.startsWith("catchall:")) {
        if(description?.endsWith("0100") ||description?.endsWith("1001") || description?.matches("on/off\\s*:\\s*1"))
        {
            def result = createEvent(name: "switch", value: "on")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }
        else if(description?.endsWith("0000") || description?.endsWith("1000") || description?.matches("on/off\\s*:\\s*0"))
        {
            def result = createEvent(name: "switch", value: "off")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }

    }
    else if (description?.startsWith("read attr -")) {
        def descMap = parseDescriptionAsMap(description)
        log.trace "descMap : $descMap"

        if (descMap.cluster == "0300") {
            log.debug descMap.value
            def tempInMired = convertHexToInt(descMap.value)
            def tempInKelvin = Math.round(1000000/tempInMired)
            log.trace "temp in kelvin: $tempInKelvin"
            sendEvent(name: "colorTemperature", value: tempInKelvin, displayed:false)
        }
        else if(descMap.cluster == "0008"){
            def dimmerValue = Math.round(convertHexToInt(descMap.value) * 100 / 255)
            log.debug "dimmer value is $dimmerValue"
            sendEvent(name: "level", value: dimmerValue)
        }
    }
    else {
        def name = description?.startsWith("on/off: ") ? "switch" : null
        def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
        def result = createEvent(name: name, value: value)
        log.debug "Parse returned ${result?.descriptionText}"
        return result
    }
}

def on() {
    log.debug "on()"
    sendEvent(name: "switch", value: "on")
    setLevel(state?.levelValue)
}

def off() {
    log.debug "off()"
    sendEvent(name: "switch", value: "off")
    "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
}

def refresh() {
    sendEvent(name: "heartbeat", value: "alive", displayed:false)
    [
            "st rattr 0x${device.deviceNetworkId} ${endpointId} 6 0", "delay 500",
            "st rattr 0x${device.deviceNetworkId} ${endpointId} 8 0", "delay 500",
            "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 7"
    ]

}

def configure() {
    state.levelValue = 100
    log.debug "Configuring Reporting and Bindings."
    def configCmds = [
            "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x0300 {${device.zigbeeId}} {}", "delay 500"
    ]
    return onOffConfig() + levelConfig() + configCmds + refresh() // send refresh cmds as part of config
}

def onOffConfig() {
    [
            "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 6 {${device.zigbeeId}} {}", "delay 200",
            "zcl global send-me-a-report 6 0 0x10 0 300 {01}",
            "send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500"
    ]
}

//level config for devices with min reporting interval as 5 seconds and reporting interval if no activity as 1hour (3600s)
//min level change is 01
def levelConfig() {
    [
            "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 8 {${device.zigbeeId}} {}", "delay 200",
            "zcl global send-me-a-report 8 0 0x20 5 3600 {01}",
            "send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1500"
    ]
}

def setColorTemperature(value) {
    if(value<101){
        value = (value*38) + 2700		//Calculation of mapping 0-100 to 2700-6500
    }

    def tempInMired = Math.round(1000000/value)
    def finalHex = swapEndianHex(hex(tempInMired, 4))
    def genericName = getGenericName(value)
    log.debug "generic name is : $genericName"

    def cmds = []
    sendEvent(name: "colorTemperature", value: value, displayed:false)
    sendEvent(name: "colorName", value: genericName)

    cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x0300 0x0a {${finalHex} 2000}"

    cmds
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

def setLevel(value) {
    state.levelValue = (value==null) ? 100 : value
    log.trace "setLevel($value)"
    def cmds = []

    if (value == 0) {
        sendEvent(name: "switch", value: "off")
        cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
    }
    else if (device.latestValue("switch") == "off") {
        sendEvent(name: "switch", value: "on")
    }

    sendEvent(name: "level", value: state.levelValue)
    def level = hex(state.levelValue * 254 / 100)
    cmds << "st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {${level} 0000}"

    //log.debug cmds
    cmds
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
private getGenericName(value){
    def genericName = "White"
    if(value < 3300){
        genericName = "Soft White"
    } else if(value < 4150){
        genericName = "Moonlight"
    } else if(value < 5000){
        genericName = "Cool White"
    } else if(value <= 6500){
        genericName = "Daylight"
    }

    genericName
}

private getEndpointId() {
    new BigInteger(device.endpointId, 16).toString()
}

private hex(value, width=2) {
    def s = new BigInteger(Math.round(value).toString()).toString(16)
    while (s.size() < width) {
        s = "0" + s
    }
    s
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

//Need to reverse array of size 2
private byte[] reverseArray(byte[] array) {
    byte tmp;
    tmp = array[1];
    array[1] = array[0];
    array[0] = tmp;
    return array
}
