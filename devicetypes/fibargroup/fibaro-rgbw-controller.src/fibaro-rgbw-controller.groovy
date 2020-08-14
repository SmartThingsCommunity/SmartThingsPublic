/**
 *  FIBARO RGBW Controller
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition (name: "Fibaro RGBW Controller", namespace: "FibarGroup", author: "Fibar Group") {
        capability "Switch"
        capability "Power Meter"
        capability "Configuration"
        capability "Health Check"
        capability "Color Control"
        capability "Switch Level"

        attribute "whiteLevel", "number"
        attribute "redLevel", "number"
        attribute "greenLevel", "number"
        attribute "blueLevel", "number"

        command "setWhiteLevel"
        command "setRedLevel"
        command "setGreenLevel"
        command "setBlueLevel"
        command "refresh"

        command "fireplace"
        command "storm"
        command "deepFade"
        command "liteFade"
        command "police"
        command "stop"

        fingerprint mfr: "010F", prod: "0900"
        fingerprint deviceId: "0x1101 ", inClusters:"0x27,0x72,0x86,0x26,0x60,0x70,0x32,0x31,0x85,0x33"
    }

    tiles (scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: 'Off', action: "switch.on", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/rgbw-controller/rgbw_white.png", backgroundColor: "#ffffff", nextState:"turningOn"
                attributeState "on", label: 'On', action: "switch.off", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/rgbw-controller/rgbw_white.png", backgroundColor: "#00a0dc", nextState:"turningOff"
                attributeState "turningOn", label:'Turning On', action:"switch.off", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/rgbw-controller/rgbw_white.png", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'Turning Off', action:"switch.on", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/rgbw-controller/rgbw_white.png", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
                attributeState("multiStatus", label:'${currentValue}')
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setColor"
            }
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "power", label:'${currentValue}\nW', action: "refresh"
        }
        controlTile("redLevel", "device.redLevel", "slider", range:"(0..100)", height: 1, width: 2) {
            state "redLevel", action:"setRedLevel", backgroundColor:"#ff0000"
        }
        controlTile("greenLevel", "device.greenLevel", "slider", range:"(0..100)", height: 1, width: 2) {
            state "greenLevel", action:"setGreenLevel", backgroundColor:"#00ff00"
        }
        controlTile("blueLevel", "device.blueLevel", "slider", range:"(0..100)", height: 1, width: 2) {
            state "blueLevel", action:"setBlueLevel", backgroundColor:"#0000ff"
        }
        controlTile("whiteLevel", "device.whiteLevel", "slider", range:"(0..100)", height: 1, width: 2) {
            state "whiteLevel", action:"setWhiteLevel", backgroundColor:"#000000"
        }
        valueTile("fireplace", "device.fireplace", decoration: "flat", width: 2, height: 1) {
            state "fireplace", label:'Fireplace', action:"fireplace"
        }
        valueTile("storm", "device.storm", decoration: "flat", width: 2, height: 1) {
            state "storm", label:'Storm', action:"storm"
        }
        valueTile("deepFade", "device.deepFade", decoration: "flat", width: 2, height: 1) {
            state "deepFade", label:'Deep Fade', action:"deepFade"
        }
        valueTile("liteFade", "device.liteFade", decoration: "flat", width: 2, height: 1) {
            state "liteFade", label:'Lite Fade', action:"liteFade"
        }
        valueTile("police", "device.police", decoration: "flat", width: 2, height: 1) {
            state "police", label:'Police', action:"police"
        }
        valueTile("stop", "device.stop", decoration: "stop", width: 2, height: 1) {
            state "Stop", label:'Stop', action:"stop"
        }
    }

    preferences {
        input (
                title: "Fibaro RGBW Controller ZW5 manual",
                description: "Tap to view the manual.",
                image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/rgbw_icon.png",
                url: "http://manuals.fibaro.com/content/manuals/en/FGRGBWM-441/FGRGBWM-441-EN-A-v1.1.pdf",
                type: "href",
                element: "href"
        )

        parameterMap().each {
            input (
                    title: "${it.num}. ${it.title}",
                    description: it.descr,
                    type: "paragraph",
                    element: "paragraph"
            )

            input (
                    name: it.key,
                    title: null,
                    description: "Default: $it.def" ,
                    type: it.type,
                    options: it.options,
                    range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
                    defaultValue: it.def,
                    required: false
            )
        }

        input ( name: "logging", title: "Logging", type: "boolean", required: false )
    }
}

def on() { encap(zwave.switchMultilevelV3.switchMultilevelSet(value: 255)) }

def off() { encap(zwave.switchMultilevelV3.switchMultilevelSet(value: 0)) }

def setLevel(level) { encap(zwave.switchMultilevelV3.switchMultilevelSet(value: (level > 0) ? level-1 : 0), 1) }

def setRedLevel(level) { encap(zwave.switchMultilevelV3.switchMultilevelSet(value: (level > 0) ? level-1 : 0), 2) }

def setGreenLevel(level) { encap(zwave.switchMultilevelV3.switchMultilevelSet(value: (level > 0) ? level-1 : 0), 3) }

def setBlueLevel(level) { encap(zwave.switchMultilevelV3.switchMultilevelSet(value: (level > 0) ? level-1 : 0), 4) }

def setWhiteLevel(level) { encap(zwave.switchMultilevelV3.switchMultilevelSet(value: (level > 0) ? level-1 : 0), 5) }

def fireplace() { encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(6, 1), parameterNumber: 72, size: 1)) }

def storm() { encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(7, 1), parameterNumber: 72, size: 1)) }

def deepFade() { encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(8, 1), parameterNumber: 72, size: 1)) }

def liteFade() { encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(9, 1), parameterNumber: 72, size: 1)) }

def police() { encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(10, 1), parameterNumber: 72, size: 1)) }

def stop() {
    def cmds = []
    cmds << [zwave.switchMultilevelV3.switchMultilevelSet(value: (device.currentValue("redLevel") > 0) ? device.currentValue("redLevel")-1 : 0), 2]
    cmds << [zwave.switchMultilevelV3.switchMultilevelSet(value: (device.currentValue("greenLevel") > 0) ? device.currentValue("greenLevel")-1 : 0), 3]
    cmds << [zwave.switchMultilevelV3.switchMultilevelSet(value: (device.currentValue("blueLevel") > 0) ? device.currentValue("blueLevel")-1 : 0), 4]
    cmds << [zwave.switchMultilevelV3.switchMultilevelSet(value: (device.currentValue("whiteLevel") > 0) ? device.currentValue("whiteLevel")-1 : 0), 5]
    encapSequence(cmds)
}

def refresh() {
    logging("${device.displayName} - Executing refresh()","info")
    encap(zwave.sensorMultilevelV5.sensorMultilevelGet())
}

def setColor(value) {
    logging("${device.displayName} - Executing setColor($value)","info")
    def cmds = []
    def rgb = [r: 0, g: 0, b: 0]
    def level = (device.currentValue("level") > 0) ? ((device.currentValue("level") -1)/99*100) : 100

    if ( value.hex ) {
        rgb = [r: Integer.parseInt(value.hex.substring(1,3),16), g: Integer.parseInt(value.hex.substring(3,5),16), b: Integer.parseInt(value.hex.substring(5,7),16)]
    } else if ( value.red || value.green || value.blue ) {
        rgb.r = (value.red) ?: 0
        rgb.g = (value.green) ?: 0
        rgb.b = (value.blue) ?: 0
    } else {
        rgb = hueToRGB(value.hue as Float, value.saturation as Float)
    }

    if(value.hue) { sendEvent(name: "hue", value: value.hue) }
    if(value.hex) { sendEvent(name: "color", value: value.hex) }
    if(value.saturation) { sendEvent(name: "saturation", value: value.saturation) }

    encap(zwave.switchColorV3.switchColorSet(red: rgb.r, green: rgb.g, blue: rgb.b, warmWhite: 0))
}

def hueToRGB(Float hue, Float sat) {
    hue = hue % 100 as Float
    sat = sat / 100
    Integer h = Math.floor(hue / 100 * 6)
    Float f = hue / 100 * 6 - h
    Integer p = Math.round((1 - sat) * 255)
    Integer q = Math.round((1 - sat * f) * 255)
    Integer t = Math.round((1 - sat * (1 - f)) * 255)
    switch (h) {
        case 0: return [r: 255, g: t, b: p]
        case 1: return [r: q, g: 255, b: p]
        case 2: return [r: p, g: 255, b: t]
        case 3: return [r: p, g: q, b: 255]
        case 4: return [r: t, g: p, b: 255]
        case 5: return [r: 255, g: p, b: q]
    }
}

def updated() {
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    def cmds = []
    logging("${device.displayName} - Executing updated()","info")

    runIn(3,"syncStart")
    state.lastUpdated = now()
}

def configure() {
    encap(zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 8, size: 1))
}

private syncStart() {
    boolean syncNeeded = false
    parameterMap().each {
        if(settings."$it.key" != null) {
            if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
            if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state in ["notSynced","inProgress"]) {
                state."$it.key".value = settings."$it.key" as Integer
                state."$it.key".state = "notSynced"
                syncNeeded = true
            }
        }
    }
    if ( syncNeeded ) {
        logging("${device.displayName} - starting sync.", "info")
        multiStatusEvent("Sync in progress.", true, true)
        syncNext()
    }
}

private syncNext() {
    logging("${device.displayName} - Executing syncNext()","info")
    def cmds = []
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
            multiStatusEvent("Sync in progress. (param: ${param.num})", true)
            state."$param.key"?.state = "inProgress"
            cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
            cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
            break
        }
    }
    if (cmds) {
        runIn(10, "syncCheck")
        sendHubCommand(cmds,1000)
    } else {
        runIn(1, "syncCheck")
    }
}

private syncCheck() {
    logging("${device.displayName} - Executing syncCheck()","info")
    def failed = []
    def incorrect = []
    def notSynced = []
    parameterMap().each {
        if (state."$it.key"?.state == "incorrect" ) {
            incorrect << it
        } else if ( state."$it.key"?.state == "failed" ) {
            failed << it
        } else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
            notSynced << it
        }
    }
    if (failed) {
        logging("${device.displayName} - Sync failed! Check parameter: ${failed[0].num}","info")
        sendEvent(name: "syncStatus", value: "failed")
        multiStatusEvent("Sync failed! Check parameter: ${failed[0].num}", true, true)
    } else if (incorrect) {
        logging("${device.displayName} - Sync mismatch! Check parameter: ${incorrect[0].num}","info")
        sendEvent(name: "syncStatus", value: "incomplete")
        multiStatusEvent("Sync mismatch! Check parameter: ${incorrect[0].num}", true, true)
    } else if (notSynced) {
        logging("${device.displayName} - Sync incomplete!","info")
        sendEvent(name: "syncStatus", value: "incomplete")
        multiStatusEvent("Open settings and tap Done.", true, true)
    } else {
        logging("${device.displayName} - Sync Complete","info")
        sendEvent(name: "syncStatus", value: "synced")
        multiStatusEvent("Sync OK.", true, true)
    }
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
    if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
        sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
    logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
    state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
    syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    logging("${device.displayName} - rejected request!","warn")
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.state == "inProgress" ) {
            state."$param.key"?.state = "failed"
            break
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    sendEvent(name: "switch", value: (cmd.value > 0) ? "on" : "off")
    sendEvent(name: "level", value: (cmd.value > 0) ? cmd.value+1 : 0)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd, ep=null) {
    logging("${device.displayName} - SwitchMultilevelReport received, $cmd","info")
    def list = ["level","redLevel","greenLevel","blueLevel","whiteLevel"]
    if (ep > 1) {
        sendEvent(name: list[ep-1], value: (cmd.value > 0) ? cmd.value+1 : 0)

    }
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    logging("${device.displayName} - SensorMultilevelReport received, $cmd","info")
    if ( cmd.sensorType == 4 ) {
        sendEvent(name: "power", value: cmd.scaledSensorValue, unit: "W")
        multiStatusEvent("$cmd.scaledSensorValue W")
    }
}

def parse(String description) {
    def result = []
    logging("${device.displayName} - Parsing: ${description}")
    if (description.startsWith("Err 106")) {
        result = createEvent(
                descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
                eventType: "ALERT",
                name: "secureInclusion",
                value: "failed",
                displayed: true,
        )
    } else if (description == "updated") {
        return null
    } else {
        def cmd = zwave.parse(description, cmdVersions())
        if (cmd) {
            logging("${device.displayName} - Parsed: ${cmd}")
            zwaveEvent(cmd)
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract Secure command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def version = cmdVersions()[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract CRC16 command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    } else {
        log.warn "Unable to extract MultiChannel command from $cmd"
    }
}

private logging(text, type = "debug") {
    if (settings.logging == "true") {
        log."$type" text
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
    logging("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
    encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
    encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
    encap(encapMap.cmd, encapMap.ep)
}

private encap(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo.zw.contains("s")) {
        secEncap(cmd)
    } else if (zwaveInfo.cc.contains("56")){
        crcEncap(cmd)
    } else {
        logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
        cmd.format()
    }
}

private encapSequence(cmds, Integer delay=250) {
    delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
    delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
    def result = []
    size.times {
        result = result.plus(0, (value & 0xFF) as Short)
        value = (value >> 8)
    }
    return result
}

private Map cmdVersions() {
    [0x27: 1, 0x72: 1, 0x86: 1, 0x26: 3, 0x60: 3, 0x70: 2, 0x32: 3, 0x31: 5, 0x85: 2, 0x33: 1] //Fibaro RGBW Controller
}

private parameterMap() {[
        [key: "stepTime", num: 10, size: 2, type: "number", def: 10, min: 0, max: 60000 , title: "Time between steps", descr: "(1-60000 ms)"]
]}