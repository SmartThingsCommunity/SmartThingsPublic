/**
 *  Fibaro Single Switch 2
 *
 */
metadata {
    definition (name: "Fibaro Single Switch 2 ZW5", namespace: "FibarGroup", author: "Fibar Group", mnmn: "SmartThings", vid:"generic-switch-power-energy") {
        capability "Switch"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Button"
        capability "Configuration"
        capability "Health Check"
        capability "Refresh"

        command "reset"

        fingerprint mfr: "010F", prod: "0403", model: "2000"
        fingerprint mfr: "010F", prod: "0403", model: "1000"
     }

    tiles (scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_2.png", backgroundColor: "#ffffff", nextState:"turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/switch/switch_1.png", backgroundColor: "#00a0dc", nextState:"turningOff"
            }
            tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
                attributeState("multiStatus", label:'${currentValue}')
            }
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "power", label:'${currentValue}\nW', action:"refresh"
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "energy", label:'${currentValue}\nkWh', action:"refresh"
        }
        valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "reset", label:'reset\nkWh', action:"reset"
        }


        main(["switch","power","energy"])
        details(["switch","power","energy","reset"])
    }

    preferences {
        input (
                title: "Fibaro Single Switch 2 ZW5 manual",
                description: "Tap to view the manual.",
                image: "http://manuals.fibaro.com/wp-content/uploads/2016/08/switch2_icon.jpg",
                url: "http://manuals.fibaro.com/content/manuals/en/FGS-2x3/FGS-2x3-EN-T-v1.2.pdf",
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

//UI and tile functions
private getPrefsFor(String name) {
    parameterMap().findAll( {it.key.contains(name)} ).each {
        input (
                name: it.key,
                title: "${it.num}. ${it.title}",
                description: it.descr,
                type: it.type,
                options: it.options,
                range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
                defaultValue: it.def,
                required: false
        )
    }
}

def on() {
    encap(zwave.basicV1.basicSet(value: 255))
}

def off() {
    encap(zwave.basicV1.basicSet(value: 0))
}

def reset() {
    def cmds = []
    cmds << zwave.meterV3.meterReset()
    cmds << zwave.meterV3.meterGet(scale: 0)
    cmds << zwave.meterV3.meterGet(scale: 2)
    encapSequence(cmds,1000)
}

def refresh() {
    def cmds = []
    cmds << zwave.meterV3.meterGet(scale: 0)
    cmds << zwave.meterV3.meterGet(scale: 2)
    encapSequence(cmds,1000)
}

def ping() {
    log.debug "ping()"
    refresh()
}

def installed(){
    log.debug "installed()"
    sendEvent(name: "checkInterval", value: 1920, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

//Configuration and synchronization
def updated() {
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    logging("Executing updated()","info")

    state.lastUpdated = now()
    syncStart()
}

private syncStart() {
    boolean syncNeeded = false
    Integer settingValue = null
    parameterMap().each {
        if(settings."$it.key" != null) {
            settingValue = settings."$it.key" as Integer
            if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
            if (state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
                state."$it.key".value = settingValue
                state."$it.key".state = "notSynced"
                syncNeeded = true
            }
        }
    }
    if ( syncNeeded ) {
        logging("sync needed.", "info")
        syncNext()
    }
}

private syncNext() {
    logging("Executing syncNext()","info")
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

def syncCheck() {
    logging("Executing syncCheck()","info")
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
        multiStatusEvent("Sync failed! Verify parameter: ${failed[0].num}", true, true)
    } else if (incorrect) {
        multiStatusEvent("Sync mismatch! Verify parameter: ${incorrect[0].num}", true, true)
    } else if (notSynced) {
        multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
    } else {
        if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
    }
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
    if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
        sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
    }
}

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
    logging("Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
    state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
    syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    logging("rejected request!","warn")
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.state == "inProgress" ) {
            state."$param.key"?.state = "failed"
            break
        }
    }
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    //ignore
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    logging("SwitchBinaryReport received, value: ${cmd.value} ","info")
    sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    logging("MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale} ","info")
    switch (cmd.scale) {
        case 0: sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]); break;
        case 2: sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"]); break;
    }
    multiStatusEvent("${(device.currentValue("power") ?: "0.0")} W | ${(device.currentValue("energy") ?: "0.00")} kWh")
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    logging("CentralSceneNotification received, sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}","info")
    def String action
    def Integer button
    switch (cmd.keyAttributes as Integer) {
        case 0: action = "pushed"; button = cmd.sceneNumber; break
        case 1: action = "released"; button = cmd.sceneNumber; break
        case 2: action = "held"; button = cmd.sceneNumber; break
        case 3: action = "pushed"; button = 2+(cmd.sceneNumber as Integer); break
        case 4: action = "pushed"; button = 4+(cmd.sceneNumber as Integer); break
    }
    sendEvent(name: "button", value: action, data: [buttonNumber: button], isStateChange: true)
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {
    def result = []
    logging("Parsing: ${description}")
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
            logging("Parsed: ${cmd}")
            zwaveEvent(cmd)
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        logging("Unable to extract Secure command from $cmd","warn")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def version = cmdVersions()[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        logging("Parsed Crc16Encap into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        logging("Unable to extract CRC16 command from $cmd","warn")
    }
}


private logging(text, type = "debug") {
    if (settings.logging == "true" || type == "warn") {
        log."$type" "${device.displayName} - $text"
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    logging("encapsulating command using Secure Encapsulation, command: $cmd","info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("encapsulating command using CRC16 Encapsulation, command: $cmd","info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
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
        logging("no encapsulation supported for command: $cmd","info")
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
/*
##########################
## Device Configuration ##
##########################
*/
private Map cmdVersions() {
    [0x5E: 1, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x22: 1, 0x56: 1, 0x32: 3, 0x71: 1, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 2, 0x60: 3, 0x75: 1, 0x5B: 1] //Fibaro Single Switch 2
}

private parameterMap() {[
        [key: "restoreState", num: 9, size: 1, type: "enum", options: [
                0: "power off after power failure",
                1: "restore state"
        ], def: "1", title: "Restore state after power failure",
         descr: "This parameter determines if the device will return to state prior to the power failure after power is restored"],
        [key: "ch1operatingMode", num: 10, size: 1, type: "enum", options: [
                0: "standard operation",
                1: "delay ON",
                2: "delay OFF",
                3: "auto ON",
                4: "auto OFF",
                5: "flashing mode"
        ], def: "0", title: "Operating mode",
         descr: "This parameter allows to choose operating for the 1st channel controlled by the S1 switch."],
        [key: "ch1reactionToSwitch", num: 11, size: 1, type: "enum", options: [
                0: "cancel and set target state",
                1: "no reaction",
                2: "reset timer"
        ], def: "0", title: "Reaction to switch for delay/auto ON/OFF modes",
         descr: "This parameter determines how the device in timed mode reacts to pushing the switch connected to the S1 terminal."],
        [key: "ch1timeParameter", num: 12, size: 2, type: "number", def: 50, min: 0, max: 32000, title: "Time parameter for delay/auto ON/OFF modes",
         descr: "This parameter allows to set time parameter used in timed modes. (1-32000s)"],
        [key: "ch1pulseTime", num: 13, size: 2, type: "enum", options: [
                1: "0.1 s",
                5: "0.5 s",
                10: "1 s",
                20: "2 s",
                30: "3 s",
                40: "4 s",
                50: "5 s",
                60: "6 s",
                70: "7 s",
                80: "8 s",
                90: "9 s",
                100: "10 s",
                300: "30 s",
                600: "60 s",
                6000: "600 s"
        ], def: 5, min: 1, max: 32000, title: "First channel - Pulse time for flashing mode",
         descr: "This parameter allows to set time of switching to opposite state in flashing mode."],
        [key: "switchType", num: 20, size: 1, type: "enum", options: [
                0: "momentary switch",
                1: "toggle switch (contact closed - ON, contact opened - OFF)",
                2: "toggle switch (device changes status when switch changes status)"
        ], def: "2", title: "Switch type",
         descr: "Parameter defines as what type the device should treat the switch connected to the S1 and S2 terminals"],
        [key: "flashingReports", num: 21, size: 1, type: "enum", options: [
                0: "do not send reports",
                1: "sends reports"
        ], def: "0", title: "Flashing mode - reports",
         descr: "This parameter allows to define if the device sends reports during the flashing mode."],
        [key: "s1scenesSent", num: 28, size: 1, type: "enum", options: [
                0: "do not send scenes",
                1: "key pressed 1 time",
                2: "key pressed 2 times",
                3: "key pressed 1 & 2 times",
                4: "key pressed 3 times",
                5: "key pressed 1 & 3 times",
                6: "key pressed 2 & 3 times",
                7: "key pressed 1, 2 & 3 times",
                8: "key held & released",
                9: "key Pressed 1 time & held",
                10: "key pressed 2 times & held",
                11: "key pressed 1, 2 times & held",
                12: "key pressed 3 times & held",
                13: "key pressed 1, 3 times & held",
                14: "key pressed 2, 3 times & held",
                15: "key pressed 1, 2, 3 times & held"
        ], def: "0", title: "Switch 1 - scenes sent",
         descr: "This parameter determines which actions result in sending scene IDs assigned to them."],
        [key: "s2scenesSent", num: 29, size: 1, type: "enum", options: [
                0: "do not send scenes",
                1: "key pressed 1 time",
                2: "key pressed 2 times",
                3: "key pressed 1 & 2 times",
                4: "key pressed 3 times",
                5: "key pressed 1 & 3 times",
                6: "key pressed 2 & 3 times",
                7: "key pressed 1, 2 & 3 times",
                8: "key held & released",
                9: "key Pressed 1 time & held",
                10: "key pressed 2 times & held",
                11: "key pressed 1, 2 times & held",
                12: "key pressed 3 times & held",
                13: "key pressed 1, 3 times & held",
                14: "key pressed 2, 3 times & held",
                15: "key pressed 1, 2, 3 times & held"
        ], def: "0", title: "Switch 2 - scenes sent",
         descr: "This parameter determines which actions result in sending scene IDs assigned to them."],
        [key: "ch1energyReports", num: 53, size: 2, type: "enum", options: [
                1: "0.01 kWh",
                10: "0.1 kWh",
                50: "0.5 kWh",
                100: "1 kWh",
                500: "5 kWh",
                1000: "10 kWh"
        ], def: 100, min: 0, max: 32000, title: "Energy reports",
         descr: "This parameter determines the min. change in consumed power that will result in sending power report"],
        [key: "periodicPowerReports", num: 58, size: 2, type: "enum", options: [
                1: "1 s",
                5: "5 s",
                10: "10 s",
                600: "600 s",
                3600: "3600 s",
                32000: "32000 s"
        ], def: 3600, min: 0, max: 32000, title: "Periodic power reports",
         descr: "This parameter defines in what time interval the periodic power reports are sent"],
        [key: "periodicEnergyReports", num: 59, size: 2, type: "enum", options: [
                1: "1 s",
                5: "5 s",
                10: "10 s",
                600: "600 s",
                3600: "3600 s",
                32000: "32000 s"
        ], def: 3600, min: 0, max: 32000, title: "Periodic energy reports",
         descr: "This parameter determines in what time interval the periodic Energy reports are sent"]
]}
