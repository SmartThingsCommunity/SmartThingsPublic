/*
    Osram Lightify Gardenspot Mini RGB

    Osram bulbs have a firmware issue causing it to forget its dimming level when turned off (via commands). Handling
    that issue by using state variables
*/

metadata {
    definition (name: "OSRAM LIGHTIFY Gardenspot mini RGB", namespace: "smartthings", author: "SmartThings") {

        capability "Color Temperature"
        capability "Actuator"
        capability "Switch"
        capability "Switch Level"
        capability "Configuration"
        capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "Color Control"

        attribute "colorName", "string"

        command "setAdjustedColor"

        fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "Gardenspot RGB"
		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0300,0B04,FC0F", outClusters: "0019", manufacturer: "OSRAM", model: "LIGHTIFY Gardenspot RGB"
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

        controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
            state "color", action:"setAdjustedColor"
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
        details(["switch", "refresh", "colorName", "levelSliderControl", "level", "rgbSelector"])
    }
}

// Parse incoming device messages to generate events
def parse(String description) {
    //log.info "description is $description"
    if (description?.startsWith("catchall:")) {
        if(description?.endsWith("0100") ||description?.endsWith("1001") || description?.matches("on/off\\s*:\\s*1"))
        {
            def result = createEvent(name: "switch", value: "on")
            log.debug "Parse returned ${result?.descriptionText}"
            return result
        }
        else if(description?.endsWith("0000") || description?.endsWith("1000") || description?.matches("on/off\\s*:\\s*0"))
        {
            if(!(description?.startsWith("catchall: 0104 0300"))){
                def result = createEvent(name: "switch", value: "off")
                log.debug "Parse returned ${result?.descriptionText}"
                return result
            }
        }
    }
    else if (description?.startsWith("read attr -")) {
        def descMap = parseDescriptionAsMap(description)
        log.trace "descMap : $descMap"

        if (descMap.cluster == "0300") {
            if(descMap.attrId == "0000"){  //Hue Attribute
                def hueValue = Math.round(convertHexToInt(descMap.value) / 255 * 360)
                log.debug "Hue value returned is $hueValue"
                sendEvent(name: "hue", value: hueValue, displayed:false)
            }
            else if(descMap.attrId == "0001"){ //Saturation Attribute
                def saturationValue = Math.round(convertHexToInt(descMap.value) / 255 * 100)
                log.debug "Saturation from refresh is $saturationValue"
                sendEvent(name: "saturation", value: saturationValue, displayed:false)
            }
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

def zigbeeOff() {
    "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 0 {}"
}

def off() {
    log.debug "off()"
    sendEvent(name: "switch", value: "off")
    zigbeeOff()
}

def refresh() {
    [
            "st rattr 0x${device.deviceNetworkId} ${endpointId} 6 0", "delay 500",
            "st rattr 0x${device.deviceNetworkId} ${endpointId} 8 0", "delay 500",
            "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 0", "delay 500",
            "st rattr 0x${device.deviceNetworkId} ${endpointId} 0x0300 1"
    ]

}

def configure() {
    state.levelValue = 100
    log.debug "Configuring Reporting and Bindings."
    def configCmds = [

            //Switch Reporting
            "zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
            "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1000",

            //Level Control Reporting
            "zcl global send-me-a-report 8 0 0x20 5 3600 {0010}", "delay 200",
            "send 0x${device.deviceNetworkId} ${endpointId} 1", "delay 1500",

            "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 6 {${device.zigbeeId}} {}", "delay 1000",
            "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 8 {${device.zigbeeId}} {}", "delay 500",
            "zdo bind 0x${device.deviceNetworkId} ${endpointId} 1 0x0300 {${device.zigbeeId}} {}", "delay 500"
    ]
    return configCmds + refresh() // send refresh cmds as part of config
}

def parseDescriptionAsMap(description) {
    (description - "read attr - ").split(",").inject([:]) { map, param ->
        def nameAndValue = param.split(":")
        map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
    }
}

def poll(){
    log.debug "Poll is calling refresh"
    refresh()
}

def zigbeeSetLevel(level) {
    "st cmd 0x${device.deviceNetworkId} ${endpointId} 8 4 {${level} 0000}"
}

def setLevel(value) {
	state.levelValue = (value==null) ? 100 : value
    log.trace "setLevel($value)"
    def cmds = []

    if (value == 0) {
        sendEvent(name: "switch", value: "off")
        cmds << zigbeeOff()
    }
    else if (device.latestValue("switch") == "off") {
        sendEvent(name: "switch", value: "on")
    }

    sendEvent(name: "level", value: state.levelValue)
    def level = hex(state.levelValue * 255 / 100)
    cmds << zigbeeSetLevel(level)

    //log.debug cmds
    cmds
}

//input Hue Integer values; returns color name for saturation 100%
private getColorName(hueValue){
    if(hueValue>360 || hueValue<0)
        return

    hueValue = Math.round(hueValue / 100 * 360)

    log.debug "hue value is $hueValue"

    def colorName = "Color Mode"
    if(hueValue>=0 && hueValue <= 4){
        colorName = "Red"
    }
    else if (hueValue>=5 && hueValue <=21 ){
        colorName = "Brick Red"
    }
    else if (hueValue>=22 && hueValue <=30 ){
        colorName = "Safety Orange"
    }
    else if (hueValue>=31 && hueValue <=40 ){
        colorName = "Dark Orange"
    }
    else if (hueValue>=41 && hueValue <=49 ){
        colorName = "Amber"
    }
    else if (hueValue>=50 && hueValue <=56 ){
        colorName = "Gold"
    }
    else if (hueValue>=57 && hueValue <=65 ){
        colorName = "Yellow"
    }
    else if (hueValue>=66 && hueValue <=83 ){
        colorName = "Electric Lime"
    }
    else if (hueValue>=84 && hueValue <=93 ){
        colorName = "Lawn Green"
    }
    else if (hueValue>=94 && hueValue <=112 ){
        colorName = "Bright Green"
    }
    else if (hueValue>=113 && hueValue <=135 ){
        colorName = "Lime"
    }
    else if (hueValue>=136 && hueValue <=166 ){
        colorName = "Spring Green"
    }
    else if (hueValue>=167 && hueValue <=171 ){
        colorName = "Turquoise"
    }
    else if (hueValue>=172 && hueValue <=187 ){
        colorName = "Aqua"
    }
    else if (hueValue>=188 && hueValue <=203 ){
        colorName = "Sky Blue"
    }
    else if (hueValue>=204 && hueValue <=217 ){
        colorName = "Dodger Blue"
    }
    else if (hueValue>=218 && hueValue <=223 ){
        colorName = "Navy Blue"
    }
    else if (hueValue>=224 && hueValue <=251 ){
        colorName = "Blue"
    }
    else if (hueValue>=252 && hueValue <=256 ){
        colorName = "Han Purple"
    }
    else if (hueValue>=257 && hueValue <=274 ){
        colorName = "Electric Indigo"
    }
    else if (hueValue>=275 && hueValue <=289 ){
        colorName = "Electric Purple"
    }
    else if (hueValue>=290 && hueValue <=300 ){
        colorName = "Orchid Purple"
    }
    else if (hueValue>=301 && hueValue <=315 ){
        colorName = "Magenta"
    }
    else if (hueValue>=316 && hueValue <=326 ){
        colorName = "Hot Pink"
    }
    else if (hueValue>=327 && hueValue <=335 ){
        colorName = "Deep Pink"
    }
    else if (hueValue>=336 && hueValue <=339 ){
        colorName = "Raspberry"
    }
    else if (hueValue>=340 && hueValue <=352 ){
        colorName = "Crimson"
    }
    else if (hueValue>=353 && hueValue <=360 ){
        colorName = "Red"
    }

    colorName
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

private evenHex(value){
    def s = new BigInteger(Math.round(value).toString()).toString(16)
    while (s.size() % 2 != 0) {
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

def setAdjustedColor(value) {
	log.debug "setAdjustedColor: ${value}"
	def adjusted = value + [:]
	adjusted.level = null // needed because color picker always sends 100
	setColor(adjusted)
}

def setColor(value){
    log.trace "setColor($value)"
    def max = 0xfe

    if (value.hex) { sendEvent(name: "color", value: value.hex, displayed:false)}

    def colorName = getColorName(value.hue)
    sendEvent(name: "colorName", value: colorName)

    log.debug "color name is : $colorName"
    sendEvent(name: "hue", value: value.hue, displayed:false)
    sendEvent(name: "saturation", value: value.saturation, displayed:false)
    def scaledHueValue = evenHex(Math.round(value.hue * max / 100.0))
    def scaledSatValue = evenHex(Math.round(value.saturation * max / 100.0))

    def cmd = []
    if (value.switch != "off" && device.latestValue("switch") == "off") {
        cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 6 1 {}"
        cmd << "delay 150"
    }

    cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x00 {${scaledHueValue} 00 0000}"
    cmd << "delay 150"
    cmd << "st cmd 0x${device.deviceNetworkId} ${endpointId} 0x300 0x03 {${scaledSatValue} 0000}"
    
    if (value.level) {
        state.levelValue = value.level
        sendEvent(name: "level", value: value.level)
        def level = hex(value.level * 255 / 100)
        cmd << zigbeeSetLevel(level)
    }
    
    if (value.switch == "off") {
        cmd << "delay 150"
        cmd << off()
    }

    cmd
}
