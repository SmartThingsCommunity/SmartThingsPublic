/**
 *  Fibaro Button
 *
 */
metadata {
    definition (name: "Fibaro Button", namespace: "FibarGroup", author: "Fibar Group") {
        capability "Actuator"
        capability "Button"
        capability "Switch"
        capability "Configuration"
        capability "Battery"

        command "push1"
        command "push2"
        command "push3"
        command "push4"
        command "push5"

        fingerprint mfr: "010F", prod: "0F01"
        fingerprint deviceId: "0x1801", inClusters: "0x5E,0x59,0x80,0x73,0x56,0x98,0x7A,0x5B,0x85,0x84,0x5A,0x86,0x72,0x71,0x70,0x8E,0x9C"
        fingerprint deviceId: "0x1801", inClusters: "0x5E,0x59,0x80,0x73,0x56,0x7A,0x5B,0x85,0x84,0x5A,0x86,0x72,0x71,0x70,0x8E,0x9C"
    }

    tiles (scale: 2) {
        multiAttributeTile(name:"FGB", type:"lighting", width:6, height:4) {
            tileAttribute("device.button", key:"PRIMARY_CONTROL") {
                attributeState("default", label:'', action: 'push1', backgroundColor:"#cd1b11", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_x1.png")

            }
            tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
                attributeState("multiStatus", label:'${currentValue}')
            }
        }
        standardTile("push1", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2, canChangeIcon: true) {
            state "default", label:"PUSH", action:"push1", backgroundColor:"#00a0dc", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_std.png"
            state "pushed", label:"PUSH", action:"push1", backgroundColor:"#ffffff",  icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_std.png"

        }
        standardTile("push2", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"push2", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_x2.png"
        }
        standardTile("push3", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"push3", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_x3.png"
        }
        standardTile("push4", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"push4", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_x4.png"
        }
        standardTile("push5", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"push5", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_x5.png"
        }
        standardTile("switch", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "off", label:"", action:"on", backgroundColor:"#ffffff", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_hold_std.png"
            state "on", label:"", action:"off", backgroundColor:"#cd1b11", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/button/pb_red_hold.png"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: false) {
            state "battery", label:'${currentValue}%\nbattery', unit:"%"
        }
        main "push1"
        details(["FGB","push2","push3","push4","push5","switch","battery"])
    }

    preferences {

        input (
                title: "Fibaro Button",
                description: "Tap to view the manual.",
                image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/button_icon.png",
                url: "http://manuals.fibaro.com/content/manuals/en/FGPB-101/FGPB-101-EN-T-v1.2.pdf",
                type: "href",
                element: "href"
        )

        input ( name: "logging", title: "Logging", type: "boolean", required: false )
    }
}

def push1() { buttonEvent(1, "pushed"); statusEvent("Pushed once (virtual)"); }
def push2() { buttonEvent(2, "pushed"); statusEvent("Pushed twice (virtual)"); }
def push3() { buttonEvent(3, "pushed"); statusEvent("Pushed 3 times (virtual)") }
def push4() { buttonEvent(4, "pushed"); statusEvent("Pushed 4 times (virtual)") }
def push5() { buttonEvent(5, "pushed"); statusEvent("Pushed 5 times (virtual)") }
def on() { switchEvent("on"); buttonEvent(1, "held"); statusEvent("Held (virtual)") }
def off() { switchEvent("off"); statusEvent("Released (virtual)") }

def updated() {
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    sendEvent(name: "numberOfButtons", value: 5)
    state.lastUpdated = now()
}

def configure() {
    def cmds = []
    encap(zwave.batteryV1.batteryGet())
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    logging("${device.displayName} woke up", "info")
    [response(encap(zwave.batteryV1.batteryGet()))]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    logging("${device.displayName} - $cmd", "info")
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    logging("${device.displayName} - rejected request!","warn")
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel}", "info")
    sendEvent(name: "battery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    //log.debug "Scene: ${cmd.keyAttributes} ${calcParamValue()}"
    switch (cmd.keyAttributes as Integer) {
        case 0: buttonEvent(1, "pushed"); statusEvent("Pushed once (physical)"); break;
        case 1: buttonEvent(1, "released"); switchEvent("off"); statusEvent("Released (physical)"); break;
        case 2: buttonEvent(1, "held"); switchEvent("on"); statusEvent("Held (physical)"); break;
        case 3: buttonEvent(2, "pushed"); statusEvent("Pushed twice (physical)"); break;
        case 4: buttonEvent(3, "pushed"); statusEvent("Pushed 3 times (physical)"); break;
        case 5: buttonEvent(4, "pushed"); statusEvent("Pushed 4 times (physical)"); break;
        case 6: buttonEvent(5, "pushed"); statusEvent("Pushed 5 times (physical)"); break;
    }
}

def buttonEvent(Integer button, String action) {
    sendEvent(name: "button", value: action, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $action", isStateChange: true)
}

def statusEvent(String text) {
    def lastEventTime = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    sendEvent(name: "multiStatus", value: "${text} - ${lastEventTime}" , displayed: false)
}

def switchEvent(String value) {
    sendEvent(name: "switch", value: value, isStateChange: true, displayed: false)
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
        log.warn "Unable to extract secure cmd from $cmd"
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
        log.warn "Could not extract crc16 command from $cmd"
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

private List intToParam(Long value, Integer size = 1) {
    def result = []
    size.times {
        result = result.plus(0, (value & 0xFF) as Short)
        value = (value >> 8)
    }
    return result
}

private Map cmdVersions() {
    [0x5E: 2, 0x59: 1, 0x80: 1, 0x73: 1, 0x56: 1, 0x98:1, 0x7A: 3, 0x5B: 1, 0x85: 2, 0x84: 2, 0x5A: 1, 0x86: 2, 0x72: 2, 0x71: 1, 0x70: 2, 0x8E: 2, 0x9C: 1]
}