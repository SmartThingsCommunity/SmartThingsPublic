/**
 *  Fibaro KeyFob
 */
metadata {
    definition(name: "Fibaro KeyFob ZW5", namespace: "FibarGroup", author: "Fibar Group") {
        capability "Actuator"
        capability "Battery"
        capability "Button"
        capability "Switch"
        capability "Configuration"

        (1..30).each { n ->
            if (n in (1..6)) {
                attribute "switch$n", "string"
                command "on$n"
                command "off$n"
            }
            command "button$n"
        }
        attribute "syncStatus", "string"
        attribute "batteryStatus", "string"
        command "forceSync"
        command "menuButton"

        fingerprint mfr: "010F", prod: "0501"
        fingerprint deviceId: "0x1801", inClusters: "0x5E,0x59,0x80,0x7A,0x73,0x22,0x85,0x5B,0x70,0x5A,0x72,0x8E,0x86,0x84,0x75,0x56,0x98"
        fingerprint deviceId: "0x1801", inClusters: "0x5E,0x59,0x80,0x7A,0x73,0x22,0x85,0x5B,0x70,0x5A,0x72,0x8E,0x86,0x84,0x75,0x56"
    }

    tiles(scale: 2) {
        def detailList = []
        def url = "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/keyfob/"

        standardTile("menuButton", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2, canChangeIcon: true) {
            state "default", label: "PUSH", action: "menuButton", backgroundColor: "#00A0DC", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/keyfob/kefob_std.png"
            state "pushed", label: "PUSH", action: "menuButton", backgroundColor: "#FFFFFF", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/keyfob/kefob_std.png"
        }

        standardTile("infoLabel", "device.button", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: "--"
        }

        standardTile("infoLabel1", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 1) {
            state "default", label: "Recently:"
        }

        [1, 2, 3, 7, 19, 8, 20, 9, 21, 4, 5, 6, 10, 22, 11, 23, 12, 24].each { n ->
            if (n in (1..6)) { //main large tiles
                def imgUrl = getFileName(url, n)
                detailList << "button$n"
                standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
                    state "default", label: "", action: "button$n", icon: imgUrl
                }
            } else if (n in (7..12)) { //x2 tiles
                def imgUrl = getFileName(url, n)
                detailList << "button$n"
                standardTile("button$n", "device.button", inactiveLabel: false, decoration: "flat", width: 1, height: 1) {
                    state "default", label: "", action: "button$n", icon: imgUrl
                }
            } else if (n in (19..24)) { //hold tiles
                def i = n - 18
                def imgUrl = getFileName(url, n)
                def stateONsufix = ".png"
                def stateOFFsufix = "_hold.png"
                detailList << "switch$i"
                standardTile("switch$i", "device.switch$i", canChangeIcon: false, width: 1, height: 1, decoration: "flat") {
                    state "off", label: "", action: "on$i", backgroundColor: "#ffffff", icon: imgUrl + stateOFFsufix
                    state "on", label: "", action: "off$i", backgroundColor: "#FF0000", icon: imgUrl + stateONsufix
                }
            }
        }
        valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
            state "val", label: '${currentValue}'
        }

        standardTile("syncStatus", "device.syncStatus", decoration: "flat", width: 2, height: 2) {
            def syncIconUrl = "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/keyfob/sync_icon.png"
            state "synced", label: 'OK', action: "forceSync", backgroundColor: "#00a0dc", icon: syncIconUrl
            state "pending", label: "Pending", action: "forceSync", backgroundColor: "#153591", icon: syncIconUrl
            state "inProgress", label: "Syncing", action: "forceSync", backgroundColor: "#44b621", icon: syncIconUrl
            state "incomplete", label: "Incomplete, do it again", action: "forceSync", backgroundColor: "#f1d801", icon: syncIconUrl
            state "failed", label: "Failed", action: "forceSync", backgroundColor: "#bc2323", icon: syncIconUrl
            state "force", label: "Force", action: "forceSync", backgroundColor: "#e86d13", icon: syncIconUrl
        }

        detailList << "batteryStatus"
        detailList << "syncStatus"

        main "menuButton"
        details(detailList)
    }

    preferences {
        input (
                title: "Fibaro KeyFob ZW5 manual",
                description: "Tap to view the manual.",
                image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/kf_icon.png",
                url: "http://manuals.fibaro.com/content/manuals/en/FGKF-601/FGKF-601-EN-T-v1.1.pdf",
                type: "href",
                element: "href"
        )
        input(
                type: "paragraph",
                element: "paragraph",
                title: "Lock Mode:",
                description: "The KeyFob can be protected with a sequence of 2 to 5 button clicks. When unlocking sequence is set, the device will lock itself after specified time (in sec) of being inactive, or by pressing and hold specified button."
        )

        input name: "protection", title: "Protection State", type: "enum", options: [0: "Unprotected", 1: "Protection by sequence"], required: false
        input name: "unlockSeq", type: "number", title: "1. Unlocking sequence (2 to 5 buttons)", range: "2..66666", required: false
        input name: "lockTim", type: "number", title: "2. Time to lock  (in sec 5-1791)", range: "5..1791", required: false
        input name: "lockBtn", type: "number", title: "2.a) Locking button: KeyFob locked by holding one of the 1-6 button", range: "1..6", required: false

        input(
                type: "paragraph",
                element: "paragraph",
                title: "Button Modes:",
                description: "Select button modes.\nActivating a double click will introduce delay to a single click reaction."
        )
        parameterMap().findAll({ it.key.contains('btn') }).each {
            input(
                    name: it.key,
                    title: it.descr,
                    description: "1 Click & Hold (Default)",
                    type: it.type,
                    options: [
                            1 : "1 Click",
                            2 : "2 Clicks",
                            3 : "1 & 2 Clicks",
                            8 : "Hold and Release",
                            9 : "1 Click & Hold (Default)",
                            10: "2 Clicks & Hold",
                            11: "1, 2 Clicks & Hold"
                    ],
                    required: false
            )
        }
        input name: "menuButton", type: "number", title: "Button number to be activated from main menu:", required: false
        input name: "logging", title: "Logging", type: "boolean", required: false
    }
}

def installed() {
    initialize()
}

def initialize() {
    sendEvent(name: "numberOfButtons", value: 30)
    state.lastUpdated = now()
    parameterMap().each {
        state."$it.key" = [value: null, state: "synced"]
    }
    state.protection = [value: null, state: "synced"]

}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.info "$device.displayName woke up"
    def cmdsSet = []
    def cmdsGet = []
    def cmds = []
    def cmdCount = 0
    def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: true)]
    cmdsGet << zwave.batteryV1.batteryGet()
    if (device.currentValue("syncStatus") != "synced") {
        parameterMap().each {
            if (device.currentValue("syncStatus") == "force") {
                state."$it.key".state = "notSynced"
            }
            if (state."$it.key".value != null && state."$it.key".state == "notSynced") {
                cmdsSet << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num, size: it.size)
                cmdsGet << zwave.configurationV2.configurationGet(parameterNumber: it.num)
                cmdCount = cmdCount + 1
            }
        }
        if (device.currentValue("syncStatus") == "force") {
            state.protection.state = "notSynced"
        }
        if (state.protection.value != null && state.protection.state == "notSynced") {
            cmdsSet << zwave.protectionV2.protectionSet(localProtectionState: state.protection.value)
            cmdsGet << zwave.protectionV2.protectionGet()
            cmdCount = cmdCount + 1
        }
        sendEvent(name: "syncStatus", value: "inProgress")
        runIn((5 + cmdCount * 1.5), syncCheck)
    }
    if (cmdsSet) {
        cmds = encapSequence(cmdsSet, 500)
        cmds << "delay 500"
    }
    cmds = cmds + encapSequence(cmdsGet, 1000)
    cmds << "delay " + (5000 + cmdCount * 1500)
    cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
    results = results + response(cmds)

    return results
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey
    paramKey = parameterMap().find({ it.num == cmd.parameterNumber }).key
    if (state."$paramKey".value == cmd.scaledConfigurationValue) {
        state."$paramKey".state = "synced"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv2.ProtectionReport cmd) {
    if (state.protection.value == cmd.localProtectionState) {
        state.protection.state = "synced"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.info "$device.displayName battery is $cmd.batteryLevel ($cmd)"
    def timeDate = new Date().format("yyyy MMM dd EEE HH:mm:ss", location.timeZone)
    sendEvent(name: "batteryStatus", value: "Battery: $cmd.batteryLevel%\n($timeDate)")
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    log.warn "KeyFob rejected configuration!"
    sendEvent(name: "syncStatus", value: "failed")
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv2.ProtectionSupportedReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.associationgrpinfov1.AssociationGroupCommandListReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationGroupingsReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecuritySchemeReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    def realButton = cmd.sceneNumber as Integer //1-6 physical, 7-12 sequences
    def keyAttribute = cmd.keyAttributes as Integer
    def mappedButton
    def action
    /* buttons:
        1-6		Single Presses
        7-12	Double Presses
        19-24	Held Buttons
    */
    if (keyAttribute in [1, 2]) {
        mappedButton = 18 + realButton
        if (keyAttribute == 1) {
            action = "off"
        } else {
            action = "on"
        }
    } else {
        if (keyAttribute == 0) {
            if (realButton > 6) {
                mappedButton = 18 + realButton
            } else {
                mappedButton = realButton
            }
        } else {
            mappedButton = 6 * (keyAttribute - 2) + realButton
        }
        action = "pushed"
    }
    buttonEvent(mappedButton, action)
}

def updated() {
    if (state.lastUpdated && (now() - state.lastUpdated) < 500) return
    sendEvent(name: "numberOfButtons", value: 30)
    def tempValue
    def syncRequired = 0
    parameterMap().each {
        if (settings."$it.key" != null || it.key == "lockBtnTim") {
            switch (it.type) {
                case "buttonTime": tempValue = btnTimToValue(); break
                case "sequence": tempValue = seqToValue(settings."$it.key"); break
                case "mode": tempValue = settings."$it.key" as Integer; break
                case "number": tempValue = settings."$it.key"; break
            }
            if (state."$it.key" == null) {
                state."$it.key" = [value: null, state: "synced"]
            }
            if (state."$it.key".value != tempValue) {
                syncRequired = 1
                state."$it.key".value = tempValue
                state."$it.key".state = "notSynced"
            }
        }
    }
    if (state.protection == null) {
        state.protection = [value: null, state: "synced"]
    }
    if (state.protection != null) {
        tempValue = settings.protection as Integer
        if (state.protection.value != tempValue) {
            syncRequired = 1
            state.protection.value = tempValue
            state.protection.state = "notSynced"
        }
    }
    if (syncRequired != 0) {
        sendEvent(name: "syncStatus", value: "pending")
    }
    state.lastUpdated = now()
}

def on1() { buttonEvent(19, "on") }

def on2() { buttonEvent(20, "on") }

def on3() { buttonEvent(21, "on") }

def on4() { buttonEvent(22, "on") }

def on5() { buttonEvent(23, "on") }

def on6() { buttonEvent(24, "on") }

def off1() { buttonEvent(19, "off") }

def off2() { buttonEvent(20, "off") }

def off3() { buttonEvent(21, "off") }

def off4() { buttonEvent(22, "off") }

def off5() { buttonEvent(23, "off") }

def off6() { buttonEvent(24, "off") }

def button1() { buttonEvent(1, "pushed") }

def button2() { buttonEvent(2, "pushed") }

def button3() { buttonEvent(3, "pushed") }

def button4() { buttonEvent(4, "pushed") }

def button5() { buttonEvent(5, "pushed") }

def button6() { buttonEvent(6, "pushed") }

def button7() { buttonEvent(7, "pushed") }

def button8() { buttonEvent(8, "pushed") }

def button9() { buttonEvent(9, "pushed") }

def button10() { buttonEvent(10, "pushed") }

def button11() { buttonEvent(11, "pushed") }

def button12() { buttonEvent(12, "pushed") }


def menuButton() {
    if (settings.menuButton == null) {
        buttonEvent(1, "pushed")
    } else {
        buttonEvent(settings.menuButton as Integer, "pushed")
    }
}

def buttonEvent(button, action) {
    button = button as Integer
    def switchNr = button - 18 as Integer
    def swichIsHold = swichHoldKeyCheck()
    if (action == "pushed"  && swichIsHold == false) {
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
    } else if (action == "on" && swichIsHold == false) {
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
        sendEvent(name: "switch$switchNr", value: "on", data: [switchNumber: switchNr], descriptionText: "$device.displayName switch $switchNr was turned on", isStateChange: true)
        runIn((3), swichOff,[data: [num:"$switchNr"]])
    }
}

def swichOff(data){
    def switchNr = data.num
    sendEvent(name: "switch$switchNr", value: "off", data: [switchNumber: switchNr], descriptionText: "$device.displayName switch $switchNr was turned off", isStateChange: true)
}

def swichHoldKeyCheck() {
    def isHold = false
    (1..6).each { n ->
        if (device.currentValue("switch$n") == "on") { isHold=true; }
    }
    return isHold
}

def syncCheck() {
    def count = 0
    def notSynced = []
    if (device.currentValue("syncStatus") != "synced") {
        parameterMap().each {
            if (state."$it.key".state == "notSynced") {
                notSynced << it
                log.warn "Sync failed! Verify parameter: ${notSynced[0].num}"
                sendEvent(name: "batteryStatus", value: "Sync incomplited! Verify parameter: ${notSynced[0].num}")
                count = count + 1
            }
        }
    }
    if (state.protection.state != "synced") {
        count = count + 1
    }
    if (count == 0) {
        sendEvent(name: "syncStatus", value: "synced")
    } else {
        if (device.currentValue("syncStatus") != "failed") {
            sendEvent(name: "syncStatus", value: "incomplete")
        }
    }
}

def forceSync() {
    if (device.currentValue("syncStatus") != "force") {
        state.prevSyncState = device.currentValue("syncStatus")
        sendEvent(name: "syncStatus", value: "force")
    } else {
        if (state.prevSyncState != null) {
            sendEvent(name: "syncStatus", value: state.prevSyncState)
        } else {
            sendEvent(name: "syncStatus", value: "synced")
        }
    }
}

def seqToValue(sequence) {
    sequence = sequence as String
    def size = sequence.length()
    def result = 0
    if (size > 5) {
        size = 5; log.info "Sequence too long, will be trimmed."
    }
    (0..size - 1).each { n ->
        result = result + ((sequence[n] as Integer) * (8**n))
    }
    return result
}

def btnTimToValue() {
    def buttonVal
    def timeVal
    if (lockBtn) {
        buttonVal = (lockBtn as Integer) * 256
    } else {
        buttonVal = 0
    }
    if (lockTim) {
        timeVal = lockTim
    } else {
        timeVal = 0
    }
    if (timeVal > 255) {
        timeVal = 255
    }
    if (timeVal < 5 && timeVal != 0) {
        timeVal = 5
    }
    if (buttonVal > 1536) {
        buttonVal = 1536
    }
    return buttonVal + timeVal
}

def getFileName(url, n) {
    def fileName
    def result
    switch (n) {
        case "1": fileName = "kefob_btn_1_x1.png"; break;
        case "2": fileName = "kefob_btn_2_x1.png"; break;
        case "3": fileName = "kefob_btn_3_x1.png"; break;
        case "4": fileName = "kefob_btn_4_x1.png"; break;
        case "5": fileName = "kefob_btn_5_x1.png"; break;
        case "6": fileName = "kefob_btn_6_x1.png"; break;
        case "7": fileName = "kefob_btn_1_x2.png"; break;
        case "8": fileName = "kefob_btn_2_x2.png"; break;
        case "9": fileName = "kefob_btn_3_x2.png"; break;
        case "10": fileName = "kefob_btn_4_x2.png"; break;
        case "11": fileName = "kefob_btn_5_x2.png"; break;
        case "12": fileName = "kefob_btn_6_x2.png"; break;
        case "19": fileName = "kefob_btn_1"; break;
        case "20": fileName = "kefob_btn_2"; break;
        case "21": fileName = "kefob_btn_3"; break;
        case "22": fileName = "kefob_btn_4"; break;
        case "23": fileName = "kefob_btn_5"; break;
        case "24": fileName = "kefob_btn_6"; break;
    }
    result = url + fileName
    return result
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
                displayed: true
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
        log.warn "Could not extract multi channel command from $cmd"
    }
}

private logging(text, type = "debug") {
    if (settings.logging == "true") {
        log."$type" text
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd", "info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private encap(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo.zw.contains("s") && zwaveInfo.sec.contains(Integer.toHexString(cmd.commandClassId).toUpperCase())) {
        // if device is included securly and the command is on list of commands dupported with secure encapsulation
        secEncap(cmd)
    } else if (zwaveInfo.cc.contains("56")) {
        // if device supports crc16
        crcEncap(cmd)
    } else { // if all else fails send plain command
        logging("${device.displayName} - no encapsulation supported for command: $cmd", "info")
        cmd.format()
    }
}

private encapSequence(cmds, delay = 250) {
    delayBetween(cmds.collect { encap(it) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
    def result = []
    size.times {
        result = result.plus(0, (value & 0xFF) as Short)
        value = (value >> 8)
    }
    return result
}
/*
##########################
## Device Configuration ##
##########################
*/

private Map cmdVersions() {
    [0x59: 1, 0x80: 1, 0x56: 1, 0x7A: 3, 0x73: 1, 0x98: 1, 0x22: 1, 0x85: 2, 0x5B: 1, 0x70: 2, 0x8E: 2, 0x84: 2, 0x75: 2, 0x72: 2]
}

def parameterMap() {
    return [
            [key: "unlockSeq", num: 1, size: 2, descr: "1. Unlocking sequence (2 to 5 buttons)", type: "sequence"],
            [key: "lockBtnTim", num: 2, size: 2, descr: "2. Lock Time and Button", type: "buttonTime"],
            [key: "btn1mode", num: 21, size: 1, descr: "Square (1) Button Mode", type: "enum"],
            [key: "btn2mode", num: 22, size: 1, descr: "Circle (2) Button Mode", type: "enum"],
            [key: "btn3mode", num: 23, size: 1, descr: "Cross (3) Button Mode", type: "enum"],
            [key: "btn4mode", num: 24, size: 1, descr: "Triangle (4) Button Mode", type: "enum"],
            [key: "btn5mode", num: 25, size: 1, descr: "Minus (5) Button Mode", type: "enum"],
            [key: "btn6mode", num: 26, size: 1, descr: "Plus (6) Button Mode", type: "enum"]
    ]
}