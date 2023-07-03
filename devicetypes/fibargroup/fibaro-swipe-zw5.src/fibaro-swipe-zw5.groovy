/**
 *  Fibaro Swipe
 *
 */
def buttons = ["up","down","clockwise","left","right","counterClockwise"]

metadata {
    definition (name: "Fibaro Swipe ZW5", namespace: "FibarGroup", author: "Fibar Group") {
        capability "Actuator"
        capability "Battery"
        capability "Button"
        capability "Switch"
        capability "Configuration"
        capability "Power Source"
        capability "Power Meter"

        attribute "switch1", "string"
        attribute "switch2", "string"
        attribute "batteryStatus", "string"

        command "menuButton"

        buttons.each { button ->
            if ( button in ["clockwise","counterClockwise"] ) {
                command "${button}Start"
                command "${button}Stop"
            } else {
                command button
            }
        }

        fingerprint mfr: "010F", prod: "0D01"
        fingerprint deviceId: "0x1801", inClusters:"0x5E,0x59,0x80,0x56,0x7A,0x72,0x73,0x86,0x85,0x5B,0x70,0x5A,0x8E,0x84,0x98,"
        fingerprint deviceId: "0x1801", inClusters:"0x5E,0x59,0x80,0x56,0x7A,0x72,0x73,0x86,0x85,0x5B,0x70,0x5A,0x8E,0x84"
    }

    def url = "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/swipe/"

    tiles (scale: 2) {
        standardTile("menuButton", "device.button", inactiveLabel: false, width: 2, height: 2, decoration: "flat", canChangeIcon: true) {
            state "default", label:"SWIPE", action: "menuButton", backgroundColor: "#00A0DC", icon: "http://manuals.fibaro.com/wp-content/uploads/2017/02/swipe_icon.png"
            state "pushed", label:"SWIPE", action: "menuButton", backgroundColor: "#FFFFFF", icon: "http://manuals.fibaro.com/wp-content/uploads/2017/02/swipe_icon.png"
        }

        buttons.each { button ->
            if (button in ["clockwise","counterClockwise"]) {
                def imgUrl = getFileName(url, button)
                standardTile(button, "device.switch" + ((button == "clockwise") ? 1:2), inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
                    state "off", label:"", action: "${button}Start", backgroundColor: "#FFFFFF", icon: imgUrl
                    state "on", label:"", action: "${button}Stop", backgroundColor: "#00A0DC", icon: imgUrl
                }
            } else {
                def imgUrl = getFileName(url, button)
                standardTile(button, "device.button", inactiveLabel: false, width: 2, height:2, decoration: "flat") {
                    state "button", label:"", action: button, icon: imgUrl
                }
            }
        }

        valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "val", label: '${currentValue}'
        }

        standardTile("syncStatus", "device.syncStatus", decoration: "flat", width: 2, height: 2) {
            def syncIconUrl = "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/keyfob/sync_icon.png"
            state "synced", label:'OK', action:"forceSync", backgroundColor: "#00a0dc", icon: syncIconUrl
            state "pending", label:"Pending", action:"forceSync", backgroundColor: "#153591", icon: syncIconUrl
            state "inProgress", label:"Syncing", action:"forceSync", backgroundColor: "#44b621", icon: syncIconUrl
            state "incomplete", label:"Incomplete", action:"forceSync", backgroundColor: "#f1d801", icon: syncIconUrl
            state "failed", label:"Failed", action:"forceSync", backgroundColor: "#bc2323", icon: syncIconUrl
            state "force", label:"Force", action:"forceSync", backgroundColor: "#e86d13", icon: syncIconUrl
        }

        main "menuButton"
        details(buttons.plus(6,"batteryStatus").plus(7,"syncStatus"))
    }

    preferences {
        input (
                title: "Fibaro Swipe manual",
                description: "Tap to view the manual.",
                image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/swipe_icon.png",
                url: "http://manuals.fibaro.com/content/manuals/en/FGGC-001/FGGC-001-EN-T-v1.0.pdf",
                type: "href",
                element: "href"
        )

        parameterMap().findAll( {!it.key.contains('param')} ).each {
            getPrefsFor(it)
        }

        input (
                title: "Scenes sent to the controller",
                description: "Defines which actions result in sending scenes to the HUB.",
                type: "paragraph",
                element: "paragraph"
        )

        input ( name: "flickUp", title: "UP gesture" ,type: "boolean", defaultValue: true, required: false )
        input ( name: "flickDown", title: "DOWN gesture" ,type: "boolean", defaultValue: true, required: false )
        input ( name: "flickLeft", title: "LEFT gesture" ,type: "boolean", defaultValue: true, required: false )
        input ( name: "flickRight", title: "RIGHT gesture" ,type: "boolean", defaultValue: true, required: false )
        input ( name: "clockwise", title: "Clockwise gesture" ,type: "boolean", defaultValue: true, required: false )
        input ( name: "counterClockwise", title: "Counter-clockwise gesture" ,type: "boolean", defaultValue: true, required: false )
        input ( name: "menuButton", type: "enum", options: [
                1: "UP gesture",
                2: "DOWN gesture",
                3: "LEFT gesture",
                4: "RIGHT gesture"], title: "Gesture to be activated from main menu:", required: false, defaultValue: 1)
        input ( name: "logging", title: "Logging", type: "boolean", required: false )
    }
}

//UI Support functions
def getPrefsFor(parameter) {
    input (
            title: "${parameter.num}. ${parameter.title}",
            description: parameter.descr,
            type: "paragraph",
            element: "paragraph"
    )
    input (
            name: parameter.key,
            title: null,
            type: parameter.type,
            options: parameter.options,
            range: (parameter.min != null && parameter.max != null) ? "${parameter.min}..${parameter.max}" : null,
            defaultValue: parameter.def,
            required: false
    )
}

def menuButton() {
    if (!settings.menuButton) {
        buttonEvent(1)
    } else {
        buttonEvent(settings.menuButton as Integer)
    }
}
def up() { buttonEvent(1) }
def down() { buttonEvent(2) }
def left() { buttonEvent(3) }
def right() { buttonEvent(4) }
def clockwiseStart() { log.info "Start"; buttonEvent(5); switchEvent(1,"on"); }
def clockwiseStop() { switchEvent(1,"off") }
def counterClockwiseStart() { buttonEvent(6); switchEvent(2,"on"); }
def counterClockwiseStop() { switchEvent(2,"off") }

def buttonEvent(Integer number) {
    def descriptionList = [
            "UP gesture",
            "DOWN gesture",
            "LEFT gesture",
            "RIGHT gesture",
            "Clockwise gesture",
            "Counter-clockwise gesture"]
    logging("${device.displayName} - Sending buttonEvent $number","info")
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: number], descriptionText: "${descriptionList[number-1]} detected", isStateChange: true)
}

def switchEvent(Integer number, String value) {
    logging("${device.displayName} - Sending switchEvent $number $value","info")
    sendEvent(name: "switch$number", value: value, data: [switchNumber: number], isStateChange: true, displayed: false)
}

def updated() {
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    logging("${device.displayName} - Executing updated()","info")
    sendEvent(name: "numberOfButtons", value: 16)
    def syncRequired = 0
    def value
    parameterMap().each {
        if(settings."$it.key" != null || it.key in ["param10"] ) {
            if (it.key == "param10") {
                if (state.param10 == null) {value = 63}
                else {
                    value = calcParam10()}
            } else (value = settings."$it.key")

            if (state."$it.key" == null) {
                state."$it.key" = [value: null, state: "synced"]
            }
            if (state."$it.key".value != value as Integer) {
                syncRequired = 1
                state."$it.key".value = value as Integer
                state."$it.key".state = "notSynced"
            }
        }
    }

    if ( syncRequired !=0 ) { sendEvent(name: "syncStatus", value: "pending") }
    state.lastUpdated = now()
}

def calcParam10() {
    Integer result = 0
    result += ((settings.flickUp == "true")? 1:0)
    result += ((settings.flickDown == "true")? 2:0)
    result += ((settings.flickLeft == "true")? 4:0)
    result += ((settings.flickRight == "true")? 8:0)
    result += ((settings.clockwise == "true")? 16:0)
    result += ((settings.counterClockwise == "true")? 32:0)

    return result
}

def getFileName(url, button) {
    def fileName
    def result

    switch (button) {
        case "up": fileName = "swipe_white_up.png"; break;
        case "down": fileName = "swipe_white_down.png"; break;
        case "clockwise": fileName = "swipe_white_turnright.png"; break;
        case "left": fileName = "swipe_white_left.png"; break;
        case "right": fileName = "swipe_white_right.png"; break;
        case "counterClockwise": fileName = "swipe_white_turnleft.png"; break;
    }
    result = url + fileName
    return result
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    logging("${device.displayName} woke up", "info")
    def cmdsSet = []
    def cmdsGet = []
    def cmds = []
    def cmdCount = 0
    def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: true)]
    cmdsGet << zwave.batteryV1.batteryGet()
    cmdsGet << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
    if (device.currentValue("syncStatus") != "synced") {
        parameterMap().each {
            if (device.currentValue("syncStatus") == "force") { state."$it.key".state = "notSynced" }
            if (state."$it.key"?.value != null && state."$it.key"?.state == "notSynced") {
                logging "it.key:" + "$it.key" + "it.key.value:" + "$it.key.value"
                logging "it.num:" + it.num + "it.size:" + it.size
                cmdsSet << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num, size: it.size)
                cmdsGet << zwave.configurationV2.configurationGet(parameterNumber: it.num)
                cmdCount = cmdCount + 1
            }
        }

        logging("${device.displayName} - Not synced, syncing ${cmdCount} parameters", "info")
        sendEvent(name: "syncStatus", value: "inProgress")
        runIn((5+cmdCount*1.5), syncCheck)
    }
    if (cmdsSet) {
        cmds = encapSequence(cmdsSet,500)
        cmds << "delay 500"
    }
    cmds = cmds + encapSequence(cmdsGet,1000)
    cmds << "delay "+(5000+cmdCount*1500)
    cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
    results = results + response(cmds)

    return results
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey = parameterMap().find( {it.num == cmd.parameterNumber as Integer} ).key
    logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey"?.value, "info")

    if (state."$paramKey".value == cmd.scaledConfigurationValue) {
        state."$paramKey".state = "synced"
    }

}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    logging("${device.displayName} - rejected request!","warn")
    if (device.currentValue("syncStatus") == "inProgress") { sendEvent(name: "syncStatus", value:"failed") }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
    log.debug "interval! " + cmd
}

def syncCheck() {
    logging("${device.displayName} - Executing syncCheck()","info")
    def notSynced = []
    def count = 0
    if (device.currentValue("syncStatus") != "synced") {
        parameterMap().each {
            if (state."$it.key"?.state == "notSynced" ) {
                notSynced << it
                logging "Sync failed! Verify parameter: ${notSynced[0].num}"
                logging "Sync $it.key " + state."$it.key"
                sendEvent(name: "batteryStatus", value: "Sync incomplited! Check parameter nr. ${notSynced[0].num}")
                count = count + 1
            }
        }
    }
    if (count == 0) {
        logging("${device.displayName} - Sync Complete","info")
        sendEvent(name: "syncStatus", value: "synced")
    } else {
        logging("${device.displayName} Sync Incomplete","info")
        if (device.currentValue("syncStatus") != "failed") {
            sendEvent(name: "syncStatus", value: "incomplete")
        }
    }
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    if ((cmd.sceneNumber as Integer) in (1..4)) {
        buttonEvent((cmd.sceneNumber as Integer) + ((cmd.keyAttributes == 0)? 0:6))
    } else if ((cmd.sceneNumber as Integer) in (5..6)) {
        switchEvent((cmd.sceneNumber as Integer)-4, (cmd.keyAttributes == 2)? "on":"off")
        if (cmd.keyAttributes == 2) { buttonEvent((cmd.sceneNumber as Integer) ) }
    } else {
        buttonEvent((cmd.sceneNumber as Integer) + 4)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.info "$device.displayName battery is $cmd.batteryLevel ($cmd)"
    if (cmd.batteryLevel<100) {
        sendEvent(name: "batteryStatus", value: "Battery: $cmd.batteryLevel%")
    } else {
        sendEvent(name: "batteryStatus", value: "External supply")
    }
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
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

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    } else {
        log.warn "Could not extract MultiChannel command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecuritySchemeReport cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationGroupingsReport cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelReport cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
    logging cmd
}

def zwaveEvent(physicalgraph.zwave.commands.associationgrpinfov1.AssociationGroupCommandListReport cmd) {
    logging cmd
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
    logging("${device.displayName} - encapsulating command using Multi Channel Encapsulation, ep: $ep command: $cmd","info")
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
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

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
    encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
    encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
    encap(encapMap.cmd, encapMap.ep)
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
        logging "result": + result
        logging "value: " + value
        value = (value >> 8)
    }
    return result
}
/*
##########################
## Device Configuration ##
##########################
*/

def configure() {
    def cmds = []
    //1. configure wakeup interval
    cmds += zwave.wakeUpV1.wakeUpIntervalSet(seconds:21600, nodeid:zwaveHubNodeId)
    cmds += zwave.wakeUpV1.wakeUpNoMoreInformation()
    encapSequence(cmds, 500)

}
private Map cmdVersions() {
    [0x59: 1, 0x80: 1, 0x56: 1, 0x72: 2, 0x73: 1, 0x98: 1, 0x86: 1, 0x85: 2, 0x5B: 1, 0x70: 2, 0x5A: 1, 0x8E: 2, 0x84: 2] //Fibaro Swipe
}

private parameterMap() {[
        [key: "orientation", num: 1, size: 1, type: "enum", options: [
                0: "default orientation",
                1: "180° rotation",
                2: "90° clockwise rotation",
                3: "90° counter-clockwise rotation"],
         def: "0", title: "Device orientation",
         descr: "Determines orientation of the Swipe in relation to its default position."],
        [key: "buzzerSignaling", num: 2, size: 1, type: "enum", options: [0: "gestures detection is not signalled", 1: "gestures detection is signalled"], def: "1", title: "Buzzer - acoustic signal settings",
         descr: "Acoustic signalling of gestures detection."],
        [key: "buzzerMode", num: 4, size: 1, type: "enum", options: [
                1: "only successful recognition is signalled",
                2: "only failed recognition is signalled",
                3: "successful and failed recognition is signalled"],
         def: "3", title: "Buzzer - signalling result of gesture recognition",
         descr: "Acoustic signalling of gesture recognition result."],
        [key: "powerInterval", num: 5, size: 2, type: "number", def: 4, min: 0, max: 1080, title: "Powering mode - interval",
         descr: "How often the device checks if the USB power supply is connected.\n0 - powering mode is not updated\n1-1080 (in minutes) - time interval"],
        [key: "powerMode", num: 6, size: 1, type: "enum", options: [
                0: "Standby Mode",
                1: "Simple Mode",
                2: "the Swipe does not enter power saving mode"],
         def: "0", title: "Power saving mode (battery mode)",
         descr: "Determines operation of gesture detection when battery powered.\nStandby Mode - hold gesture must be performed to exit power saving mode.\nSimple Mode - gesture recognition is always active, but only slowly performed gestures will be recognized properly"],
        [key: "holdGesture", num: 7, size: 1, type: "enum", options: [
                0: "Hold gesture enabled",
                1: "Hold gesture disabled"],
         def: "0", title: "Hold gesture to enter the menu",
         descr: "This parameter allows to choose if the menu can be entered using the Hold gesture."],
        [key: "param10", num: 10, size: 1, type: "number", def: 63, min: 0, max: 63, title: null, descr: null],
]}