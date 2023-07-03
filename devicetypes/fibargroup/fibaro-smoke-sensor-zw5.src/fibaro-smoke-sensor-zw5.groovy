/**
 * Fibaro Smoke Sensor DEV ZW5
 */
metadata {
    definition (name: "Fibaro Smoke Sensor ZW5", namespace: "FibarGroup", author: "Fibar Group") {
        capability "Battery"
        capability "Configuration"
        capability "Sensor"
        capability "Smoke Detector"
        capability "Temperature Measurement"
        attribute "heatAlarm", "enum", ["overheat", "inactive"]
        attribute "tamperStatus", "string"

        fingerprint mfr:"010F", prod:"0C02"
        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x9C, 0x98, 0x7A", outClusters: "0x20, 0x8B"
        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x9C, 0x98, 0x7A"
    }
    simulator {
        //battery
        for (int i in [0, 5, 10, 15, 50, 99, 100]) {
            status "battery ${i}%":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
                    new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i)
            ).incomingMessage()
        }
        status "battery 100%": "command: 8003, payload: 64"
        status "battery 5%": "command: 8003, payload: 05"
        //smoke
        status "smoke detected": "command: 7105, payload: 01 01"
        status "smoke clear": "command: 7105, payload: 01 00"
        status "smoke tested": "command: 7105, payload: 01 03"
        //temperature
        for (int i = 0; i <= 100; i += 20) {
            status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
                    new physicalgraph.zwave.Zwave().sensorMultilevelV5.sensorMultilevelReport(scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1)
            ).incomingMessage()
        }
    }
    preferences {
        input(
                title: "Fibaro Smoke Sensor manual",
                description: "Tap to view the manual.",
                image: "http://manuals.fibaro.com/wp-content/uploads/2017/07/sd_icon.png",
                url: "http://manuals.fibaro.com/content/manuals/en/FGSD-002/FGSD-002-EN-A-v1.1.pdf",
                type: "href",
                element: "href"
        )

        parameterMap().each {
            getPrefsFor(it)
        }

    }
    tiles (scale: 2){
        multiAttributeTile(name:"FGSD", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
                def url = "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/smoke/"
                attributeState("clear", label:"CLEAR", icon: url + "smoke-sensor0.png", backgroundColor:"#ffffff")
                attributeState("detected", label:"SMOKE", icon: url + "smoke-sensor1.png", backgroundColor:"#e86d13")
                attributeState("tested", label:"TEST", icon: url + "smoke-sensor1.png", backgroundColor:"#e86d13")
                attributeState("unknown", label:"UNKNOWN", icon:url + "smoke-sensor1.png", backgroundColor:"#ffffff")
            }

            tileAttribute("device.multiStatus", key: "SECONDARY_CONTROL") {
                attributeState("multiStatus", label: '${currentValue}')
            }
        }
        valueTile("tamperStatus", "device.tamperStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state("tamperStatus", label:'${currentValue}')

        }

        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:"%"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
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
        valueTile("heatAlarm", "device.heatAlarm", inactiveLabel: false, width: 4, height: 2) {
            state "inactive", label:'No temperature alarm', backgroundColor:"#ffffff"
            state "overheat", label:'Temperature alarm', backgroundColor:"#bc2323"
        }

        standardTile("syncStatus", "device.syncStatus", decoration: "flat", width: 2, height: 2) {
            def syncIconUrl = "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/keyfob/sync_icon.png"
            state "synced", label: 'OK', action: "forceSync", backgroundColor: "#00a0dc", icon: syncIconUrl
            state "pending", label: "Pending", action: "forceSync", backgroundColor: "#153591", icon: syncIconUrl
            state "inProgress", label: "Syncing", action: "forceSync", backgroundColor: "#44b621", icon: syncIconUrl
            state "incomplete", label: "Incomplete", action: "forceSync", backgroundColor: "#f1d801", icon: syncIconUrl
            state "failed", label: "Failed", action: "forceSync", backgroundColor: "#bc2323", icon: syncIconUrl
            state "force", label: "Force", action: "forceSync", backgroundColor: "#e86d13", icon: syncIconUrl
        }

        main "FGSD"
        details(["FGSD","temperature", "battery", "heatAlarm", "tamperStatus", "syncStatus"])
    }
}

//UI Support functions
def getPrefsFor(parameter) {
    input(
            title: "${parameter.num}. ${parameter.title}",
            description: parameter.descr,
            type: "paragraph",
            element: "paragraph"
    )
    input(
            name: parameter.key,
            title: null,
            type: parameter.type,
            options: parameter.options,
            range: (parameter.min != null && parameter.max != null) ? "${parameter.min}..${parameter.max}" : null,
            defaultValue: parameter.def,
            required: false
    )
}

def updated() {
    log.debug "Updated"
    if (state.lastUpdated && (now() - state.lastUpdated) < 2000) return
    def cmds = []
    def cmdCount = 0


    parameterMap().each {
        if (settings."$it.key" == null || state."$it.key" == null) {
            state."$it.key" = [value: it.def as Integer, state: "notSynced"]
        }

        if (settings."$it.key" != null) {
            if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state == "notSynced") {
                state."$it.key".value = settings."$it.key" as Integer
                state."$it.key".state = "notSynced"
                cmdCount = cmdCount + 1
            }
        } else {
            if (state."$it.key".state == "notSynced") {
                cmdCount = cmdCount + 1
            }
        }
    }

    if (cmdCount > 0) {
        logging("${device.displayName} - sending config.", "info")
        sendEvent(name: "syncStatus", value: "pending")
    }

    state.lastUpdated = now()
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.info "Executing zwaveEvent 86 (VersionV1): 12 (VersionReport) with cmd: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} battery is low"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    state.lastbatt = now()
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
    def msg = cmd.status == 0 ? "try again later" :
            cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
                    cmd.status == 2 ? "request queued" : "sorry"
    createEvent(displayed: true, descriptionText: "$device.displayName is busy, $msg")
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    log.info "Executing zwaveEvent 71 (NotificationV3): 05 (NotificationReport) with cmd: $cmd"
    def result = []
    if (cmd.notificationType == 7) {
        switch (cmd.event) {
            case 0:
                //result << createEvent(name: "tamper", value: "Tamper is inactive", displayed: false)
                sendEvent(name: "tamperStatus", value: "Tamper inactive")
                break
            case 3:
                //result << createEvent(name: "tamper", value: "Tamper is active", descriptionText: "$device.displayName casing was opened")
                sendEvent(name: "tamperStatus", value: "Tamper active")
                break
                break
                break
        }
    } else if (cmd.notificationType == 1) { //Smoke Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Smoke Alarm (V2)"
        result << smokeAlarmEvent(cmd.event)
    } else if (cmd.notificationType == 4) { // Heat Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Heat Alarm (V2)"
        result << heatAlarmEvent(cmd.event)
    } else if (cmd.notificationType == 8) {
        if (cmd.event == 0x0A) {
            def map = [:]
            map.name = "battery"
            map.value = 1
            map.unit = "%"
            map.displayed = true
            result << createEvent(map)
        }
    } else if (cmd.notificationType == 9) {
        if (cmd.event == 0x01) {
            result << createEvent(descriptionText: "Warning: $device.displayName system hardware failure", isStateChange: true)
        }
    } else {
        log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
        result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
    }
    result
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
    sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
}

def smokeAlarmEvent(value) {
    def alarmInfo = "Last alarm detection: "
    def alarmTestInfo = "Last alarm detection - TEST: "
    log.debug "smokeAlarmEvent(value): $value"
    def map = [name: "smoke"]
    if (value == 2) {
        map.value = "detected"
        map.descriptionText = "$device.displayName detected smoke"
        state.lastAlarmDate = "\n"+new Date().format("yyyy MMM dd EEE HH:mm:ss", location.timeZone)
        multiStatusEvent(alarmInfo + state.lastAlarmDate)
    } else if (value == 0) {
        map.value = "clear"
        map.descriptionText = "$device.displayName is clear (no smoke)"
    } else if (value == 3) {
        map.value = "tested"
        map.descriptionText = "$device.displayName smoke alarm test"
        map.isStateChange = true
        state.lastAlarmDate = "\n"+new Date().format("yyyy MMM dd EEE HH:mm:ss", location.timeZone)
        multiStatusEvent(alarmTestInfo + state.lastAlarmDate)
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

def heatAlarmEvent(value) {
    log.debug "heatAlarmEvent(value): $value"
    def map = [name: "heatAlarm"]
    if (value == 2) {
        map.value = "overheat"
        map.descriptionText = "$device.displayName overheat detected"
    } else if (value == 0) {
        map.value = "inactive"
        map.descriptionText = "$device.displayName heat alarm cleared (no overheat)"
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.info "Executing zwaveEvent 84 (WakeUpV1): 07 (WakeUpNotification) with cmd: $cmd"
    log.debug "WakeUpNotification"
    def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
    def cmds = []
    def cmdsSet = []
    def cmdsGet = []
    def cmdCount = 0
    def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: true)]

    cmdsGet << zwave.batteryV1.batteryGet()
    cmdsGet << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)

    if (device.currentValue("syncStatus") != "synced") {

        parameterMap().each {
            if (device.currentValue("syncStatus") == "force") {
                state."$it.key".state = "notSynced"
            }

            if (state."$it.key"?.value != null && state."$it.key"?.state == "notSynced") {
                cmdsSet << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num, size: it.size)
                cmdsGet << zwave.configurationV2.configurationGet(parameterNumber: it.num)
                cmdCount = cmdCount + 1
            }
        }

        log.debug "Not synced, syncing ${cmdCount} parameters"
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

def syncCheck(){
    logging("${device.displayName} - Executing syncCheck()", "info")
    log.warn "CHECK"
    def notSynced = []
    def count = 0

    if (device.currentValue("syncStatus") != "synced") {
        parameterMap().each {
            if (state."$it.key"?.state == "notSynced") {
                notSynced << it
                log.warn "Sync failed! Verify parameter: ${notSynced[0].num}"
                log.warn "Sync $it.key " + state."$it.key"
                sendEvent(name: "batteryStatus", value: "Sync incomplited! Check parameter nr. ${notSynced[0].num}")
                count = count + 1
            }
        }
    }
    if (count == 0) {
        logging("${device.displayName} - Sync Complete", "info")
        sendEvent(name: "syncStatus", value: "synced")
    } else {
        logging("${device.displayName} Sync Incomplete", "info")
        if (device.currentValue("syncStatus") != "failed") {
            sendEvent(name: "syncStatus", value: "incomplete")
        }
    }

}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    log.info "Executing zwaveEvent 31 (SensorMultilevelV5): 05 (SensorMultilevelReport) with cmd: $cmd"
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.unit = getTemperatureScale()
            break
        default:
            map.descriptionText = cmd.toString()
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    log.info "Executing zwaveEvent 5A (DeviceResetLocallyV1) : 01 (DeviceResetLocallyNotification) with cmd: $cmd"
    createEvent(descriptionText: cmd.toString(), isStateChange: true, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.info "Executing zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport) with cmd: $cmd"
    log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
    log.debug "deviceIdData:                ${cmd.deviceIdData}"
    log.debug "deviceIdDataFormat:          ${cmd.deviceIdDataFormat}"
    log.debug "deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}"
    log.debug "deviceIdType:                ${cmd.deviceIdType}"

}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    def result = []
    if (cmd.nodeId.any { it == zwaveHubNodeId }) {
        result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
    } else if (cmd.groupingIdentifier == 1) {
        result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
        result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd) {
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "smoke"
            map.value = cmd.sensorState == 0xFF ? "detected" : "clear"
            map.descriptionText = cmd.sensorState == 0xFF ? "$device.displayName detected smoke" : "$device.displayName is clear (no smoke)"
            break
        case 4:
            map.name = "heatAlarm"
            map.value = cmd.sensorState == 0xFF ? "overheat" : "inactive"
            map.descriptionText = cmd.sensorState == 0xFF ? "$device.displayName overheat detected" : "$device.displayName heat alarm cleared (no overheat)"
            break
    }

    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.timeparametersv1.TimeParametersGet cmd) {
    log.info "Executing zwaveEvent 8B (TimeParametersV1) : 02 (TimeParametersGet) with cmd: $cmd"
    def nowCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    //Time Parameters are requested by an un-encapsulated frame
    response(zwave.timeParametersV1.timeParametersReport(year: nowCal.get(Calendar.YEAR), month: (nowCal.get(Calendar.MONTH) + 1), day: nowCal.get(Calendar.DAY_OF_MONTH),
            hourUtc: nowCal.get(Calendar.HOUR_OF_DAY), minuteUtc: nowCal.get(Calendar.MINUTE), secondUtc: nowCal.get(Calendar.SECOND)).format())
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.warn "General zwaveEvent cmd: ${cmd}"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

private command(physicalgraph.zwave.Command cmd) {
    def secureClasses = [0x20, 0x5A, 0x70, 0x71, 0x84, 0x85, 0x8E, 0x9C]

    if (isSecured() && secureClasses.find{ it == cmd.commandClassId }) {
        log.info "Sending secured command: ${cmd}"
        secure(cmd)
    } else {
        log.info "Sending unsecured command: ${cmd}"
        crc16(cmd)
    }
}

private secure(physicalgraph.zwave.Command cmd) {
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
    //zwave.crc16encapV1.crc16Encap().encapsulate(cmd).format()
    "5601${cmd.format()}0000"
}

private commands(commands, delay=200) {
    log.info "inside commands: ${commands}"
    delayBetween(commands.collect{ command(it) }, delay)
}

private setSecured() {
    updateDataValue("secured", "true")
}
private isSecured() {
    getDataValue("secured") == "true"
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

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey = parameterMap().find({ it.num == cmd.parameterNumber }).key
    logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
    if (state."$paramKey".value == cmd.scaledConfigurationValue) {
        state."$paramKey".state = "synced"
        log.debug state."$paramKey"
    }
}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    setSecured()
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        log.debug "command: 98 (Security) 81(SecurityMessageEncapsulation) encapsulatedCommand:  $encapsulatedCommand"
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
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
    logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd", "info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd", "info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
    logging("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd", "info")
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: ep).encapsulate(cmd)
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
    } else if (zwaveInfo.cc.contains("56")) {
        crcEncap(cmd)
    } else {
        logging("${device.displayName} - no encapsulation supported for command: $cmd", "info")
        cmd.format()
    }
}

private encapSequence(cmds, Integer delay = 250) {
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

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    log.warn "Flood Sensor rejected configuration!"
    sendEvent(name: "syncStatus", value: "failed")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
    log.info "Executing zwaveEvent 98 (SecurityV1): 07 (NetworkKeyVerify) with cmd: $cmd (node is securely included)"
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecuritySchemeReport cmd) {
    log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    log.info "Executing zwaveEvent 98 (SecurityV1): 03 (SecurityCommandsSupportedReport) with cmd: $cmd"

}

def configure() {
    state.lastAlarmDate = "-"
    def cmds = []
    //1. configure wakeup interval
    cmds += zwave.wakeUpV1.wakeUpIntervalSet(seconds:4200, nodeid:zwaveHubNodeId)
    cmds += zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: 2)
    cmds += zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: 0)
    cmds += zwave.configurationV1.configurationSet(parameterNumber: 20, size: 2, scaledConfigurationValue: 1)
    cmds += zwave.configurationV1.configurationSet(parameterNumber: 21, size: 1, scaledConfigurationValue: 11)
    cmds += zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: 55)
    cmds += zwave.wakeUpV1.wakeUpNoMoreInformation()
    encapSequence(cmds, 500)

}

private Map cmdVersions() {
    [0x86: 1, 0x72: 2, 0x59: 1, 0x73: 1, 0x80: 1, 0x56: 1, 0x31: 5, 0x22: 1, 0x98: 1, 0x7A: 2, 0x84: 1, 0x84: 2, 0x71: 3, 0x70: 1, 0x70: 2, 0x8E: 1, 0x8E: 2, 0x9C: 1, 0x5A: 1, 0x85: 1, 0x85: 2, 0x20: 1, 0x8B: 1]
}

private parameterMap() {
    [
            [key: "smokeSensorSensitivity", num: 1, size: 1, type: "enum", options: [
                    1: "High",
                    2: "Medium",
                    3: "Low"],
             def: 2, title: "Fibaro Smoke Sensor sensitivity", descr: ""],
            [key: "statusNotification", num: 2, size: 1, type: "enum", options: [
                    0: "all notifications disabled",
                    1: "casing opening notification enabled",
                    2: "exceeding temperature threshold notification enabled",
                    3: "casing opening and exceeding temperature threshold enabled"],
             def: 0, title: "Status notification:", descr: "Notification sends in case of excess temperature and/or case opening"],
            [key: "intervalOfTemperatureReports", num: 20, size: 2, type: "enum", options: [
                    1: "10s",
                    3: "30s",
                    6: "60s",
                    30: "300s",
                    60: "600s",
                    360: "3600s",
                    864: "8640s"],
             def: 1, title: "Interval of temperature measurements", descr: "Time interval between temperature measurements. Report is sent when the measured value is different than previous one - specified in par. 21"],
            [key: "temperatureReportsThreshold", num: 21, size: 1, type: "enum", options: [
                    3: "0.5°F / 0.3°C",
                    6: "1°F / 0.6°C",
                    11: "2°F / 1.1°C",
                    17: "3°F / 1.7°C",
                    22: "4°F / 2.2°C",
                    28: "5°F / 2.8°C"],
             def: 11, title: "Temperature reports threshold ", descr: "Change of temperature resulting in temperature report being sent to the HUB."],
            [key: "temperatureThreshold", num: 30, size: 1, type: "enum", options: [
                    55: "130°F / 55°C",
                    60: "140°F / 60°C",
                    65: "150°F / 65°C",
                    71: "160°F / 71°C",
                    77: "170°F / 77°C",
                    82: "180°F / 82°C",
                    93: "200°F / 93°C"],
             def: 55, title: "Temperature threshold", descr: "Temperature above which the excess temperature notification is sent to the HUB (with visual and sound indication)"]
    ]
}