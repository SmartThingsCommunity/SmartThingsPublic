/**
 *  Minoston Wallmote v1.0.4
 *
 *  	Models: Eva Logik (ZW924) / MINOSTON (MR40Z)
 *
 *  Author:
 *   winnie (sky-nie)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.0.4 (08/09/2021)
 *      - add function about association Group
 *
 *    1.0.3 (07/13/2021)
 *      - Syntax format compliance adjustment
 *      - delete dummy code
 *
 *    1.0.2 (07/13/2021)
 *      - complete the function about preferences
 *
 *    1.0.1 (07/07/2021)
 *      - Initial Release
 *
 * Reference：
 *  https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/devicetypes/smartthings/aeotec-wallmote.src/aeotec-wallmote.groovy
 *  https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/zooz/zooz-remote-switch-zen34.src/zooz-remote-switch-zen34.groovy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

private static getAssociationGroups() {[
        2:"associationGroupTwo",
        3:"associationGroupThree",
        4:"associationGroupFour",
        5:"associationGroupFive",
        6:"associationGroupSix",
        7:"associationGroupSeven",
        8:"associationGroupEight",
        9:"associationGroupNine"
]}

metadata {
    definition (
            name: "Minoston Wallmote",
            namespace: "sky-nie",
            author: "winnie",
            mcdSync: true,
            ocfDeviceType: "x.com.st.d.remotecontroller",
            mnmn: "SmartThingsCommunity",
            vid: "8dd23e05-62dd-3861-9c26-9c42fbabaf18"
    ) {
        capability "Actuator"
        capability "Battery"
        capability "Button"
        capability "Refresh"
        capability "Configuration"
        capability "Health Check"

        attribute "associationGroupTwo", "string"
        attribute "associationGroupThree", "string"
        attribute "associationGroupFour", "string"
        attribute "associationGroupFive", "string"
        attribute "associationGroupSix", "string"
        attribute "associationGroupSeven", "string"
        attribute "associationGroupEight", "string"
        attribute "associationGroupNine", "string"
        attribute "firmwareVersion", "string"
        attribute "lastCheckIn", "string"
        attribute "syncStatus", "string"

        fingerprint mfr: "0312", prod: "0924", model: "D001", deviceJoinName: "S2 Remote Control Switch"//MR40Z
    }

    preferences {
        configParams.each {
            if (it.range) {
                input "configParam${it.num}", "number", title: "${it.name}:", required: false, defaultValue: "${it.value}", range: it.range
            } else {
                input "configParam${it.num}", "enum",  title: "${it.name}:", required: false, defaultValue: "${it.value}", options:it.options
            }
        }
        input "debugOutput", "enum", title: "Debug Logging", required: false, defaultValue: "1", options: [0:"Disabled", 1:"Enabled [DEFAULT]"]

        input "assocInstructions", "paragraph",
                title: "Device Associations",
                description: "Associations are an advance feature that allow you to establish direct communication between Z-Wave devices.  To make this remote control another Z-Wave device, get that device's Device Network Id from the My Devices section of the IDE and enter the id in one of the settings below.  Group 2 and Group 3 supports up to 10 associations and you can use commas to separate the device network ids.",
                required: false

        input "assocDisclaimer", "paragraph",
                title: "WARNING",
                description: "If you add a device's Device Network ID to the list below and then remove that device from SmartThings, you MUST come back and remove it from the list below.  Failing to do this will substantially increase the number of z-wave messages being sent by this device and could affect the stability of your z-wave mesh.",
                required: false

        for (Integer id in 2..9) {
            getGroupInput(id)
        }
    }
}

private getGroupInput(id) {
    input "group${id}AssocDNIs",
            "string",
            title: getGroupAssocDNIsTitle(id),
            required: false
}

private static getGroupAssocDNIsTitle(id) {
    if(id == 4 || id == 5){
        return "Enter Device Network Ids for Group ${id} Dimming Association:"
    }else{
        return "Enter Device Network Ids for Group ${id} On/Off Association:"
    }
}

def getNumberOfButtons() {
    def modelToButtons = ["D001":4]
    return modelToButtons[zwaveInfo.model] ?: 1
}

def installed() {
    createChildDevices()
    sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
    sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJson(), displayed: false)
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
    state.resyncAll = true
    state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)
    for (Integer id in 2..9){
        def name = associationGroups[id]
        if (device.currentValue(name) == null) {
            sendEvent(name: name, value: "")
        }
    }
    return []
}

def updated() {
    createChildDevices()
    if (device.label != state.oldLabel) {
        childDevices.each {
            def segs = it.deviceNetworkId.split(":")
            def newLabel = "${device.displayName} button ${segs[-1]}"
            it.setLabel(newLabel)
        }
        state.oldLabel = device.label
    }

    if (!isDuplicateCommand(state.lastUpdated, 5000)) {
        state.lastUpdated = new Date().time
        runIn(3, refreshSyncStatus)
        state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)
        for (Integer id in 2..9){
            def name = associationGroups[id]
            if (device.currentValue(name) == null) {
                sendEvent(name: name, value: "")
            }
        }
        runIn(5, executeConfigureCmds, [overwrite: true])
    }
    return []
}

def configure() {
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "zwave", scheme:"untracked"].encodeAsJson(), displayed: false)
    runIn(3, refreshSyncStatus)
    if (state.resyncAll == null) {
        state.resyncAll = true
        runIn(8, executeConfigureCmds, [overwrite: true])
    } else {
        if (!pendingChanges) {
            state.resyncAll = true
        }
        executeConfigureCmds()
    }
    return []
}

def executeConfigureCmds() {
    sendHubCommand(getConfigureCmds(), 500)
}

def parse(String description) {
    def result = []
    try {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result += zwaveEvent(cmd)
        } else {
            logWarn "Unable to parse: $description"
        }

        updateLastCheckIn()
    } catch (e) {
        logErr "parse throw:${e}"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    def button = cmd.sceneNumber
    def value = buttonAttributesMap[(int)cmd.keyAttributes]
    if (value) {
        def child = getChildDevice(button)
        child?.sendEvent(name: "button", value: value, data: [buttonNumber: 1], descriptionText: "$child.displayName was $value", isStateChange: true)
        createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true, displayed: false)
    }

    if (state.lastSequenceNumber != cmd.sequenceNumber) {
        state.lastSequenceNumber = cmd.sequenceNumber

        String paddle = (cmd.sceneNumber == 1) ? "up" : "down"
        String btnVal
        switch (cmd.keyAttributes){
            case 0:
                btnVal = paddle
                break
            case 1:
                logDebug "${paddle}_released is not supported by SmartThings"
                btnVal = paddle + "_released"
                break
            case 2:
                btnVal = paddle + "_hold"
                break
            case { it >= 3 && it <= 7}:
                btnVal = paddle + "_${cmd.keyAttributes - 1}x"
                break
            default:
                logDebug "keyAttributes ${cmd.keyAttributes} not supported"
        }

        if (btnVal) {
            sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true, descriptionText: "${device.displayName} ${btnVal}")
        }

        if (!state.hasWokenUp) {
            // device hasn't been put to sleep after inclusion and is draining the battery so put it to sleep.
            state.hasWokenUp = true
            sendHubCommand([secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())])
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    } else {
        logWarn "Unable to extract encapsulated cmd from $cmd"
        createEvent(descriptionText: cmd.toString())
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    def linkText = device.label ?: device.name
    [linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    def results = []
    results += createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
    results += getConfigureCmds()
    runIn(3, refreshSyncStatus)
    state.hasWokenUp = true
    results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%", isStateChange: true ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "$device.displayName battery is low!"
    } else {
        map.value = cmd.batteryLevel
    }
    createEvent(map)
}

def createChildDevices() {
    if (!childDevices) {
        state.oldLabel = device.label
        def child
        for (i in 1..numberOfButtons) {
            child = addChildDevice("sky-nie", "Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
                    [completedSetup: true, label: "${device.displayName} button ${i}",
                     isComponent: true, componentName: "button$i", componentLabel: "Button $i"])
            child.sendEvent(name: "supportedButtonValues", value: supportedButtonValues.encodeAsJson(), displayed: false)
            child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$child.displayName was pushed", isStateChange: true, displayed: false)
        }
    }
}

def getChildDevice(button) {
    String childDni = "${device.deviceNetworkId}:${button}"
    def child = childDevices.find{it.deviceNetworkId == childDni}
    if (!child) {
        logErr "Child device $childDni not found"
    }
    return child
}

private static getSupportedButtonValues() {
    return [
        "pushed",
        "held",
        "double",
        "pushed_3x"
    ]
}

private static getButtonAttributesMap() {
    [
        0: "pushed",
        1: "held",
        3: "double",
        4: "pushed_3x"
    ]
}

// Configuration Parameters
private getConfigParams() {
    [
        batteryReportThresholdParam,
        lowBatteryAlarmReportParam,
        ledIndicator1ColorParam,
        ledIndicator2ColorParam,
        ledIndicator3ColorParam,
        ledIndicator4ColorParam,
        ledIndicatorBrightnessParam
    ]
}

private getBatteryReportThresholdParam() {
    return getParam(1, "Battery report threshold(1% - 20%)", 1, 10, null,"1..20")
}

private getLowBatteryAlarmReportParam() {
    return getParam(2, "Low battery alarm report(5% - 20%)", 1, 5, null, "5..20")
}

private getLedIndicator1ColorParam() {
    return getParam(3, "Led Indicator Color for the First Button", 1, 0, ledColorOptions)
}

private getLedIndicator2ColorParam() {
    return getParam(4, "Led Indicator Color for the Second Button", 1, 1, ledColorOptions)
}

private getLedIndicator3ColorParam() {
    return getParam(5, "Led Indicator Color for the Third Button", 1, 2, ledColorOptions)
}

private getLedIndicator4ColorParam() {
    return getParam(6, "Led Indicator Color for the Fourth Button", 1, 3, ledColorOptions)
}

private getLedIndicatorBrightnessParam() {
    return getParam(7, "Led Indicator Brightness", 1, 5, brightnessOptions)
}

private getParam(num, name, size, defaultVal, options=null, range=null) {
    def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

    def map = [num: num, name: name, size: size, value: val]
    if (options) {
        map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
        map.options = setDefaultOption(options, defaultVal)
    }
    if (range) {
        map.range = range
    }

    return map
}

private static setDefaultOption(options, defaultVal) {
    return options?.collectEntries { k, v ->
        if ("${k}" == "${defaultVal}") {
            v = "${v} [DEFAULT]"
        }
        ["$k": "$v"]
    }
}

// Setting Options
private static getLedColorOptions() {
    return [
        "0":"White",
        "1":"Purple",
        "2":"Orange",
        "3":"Cyan",
        "4":"Red",
        "5":"Green",
        "6":"Blue"
    ]
}

private static getBrightnessOptions() {
    def options = [:]
    options["0"] = "LED off"

    (1..10).each {
        options["${it}"] = "${it}% Brightness"
    }
    return options
}

private static Integer safeToInt(val, Integer defaultVal=0) {
    if ("${val}"?.isInteger()) {
        return "${val}".toInteger()
    } else if ("${val}".isDouble()) {
        return "${val}".toDouble()?.round()
    } else {
        return  defaultVal
    }
}

private List<String> getConfigureCmds() {
    List<String> cmds = []

    int changes = pendingChanges
    if (changes) {
        logInfo "Syncing ${changes} Change(s)"
    }

    if (state.resyncAll || !device.currentValue("firmwareVersion")) {
        cmds << secureCmd(zwave.versionV1.versionGet())
    }

    if (state.resyncAll || !device.currentValue("battery")) {
        cmds << secureCmd(zwave.batteryV1.batteryGet())
    }

    if (state.wakeUpInterval != 43200) {
        cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:43200, nodeid:zwaveHubNodeId))
        cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
    }

    configParams.each { param ->
        Integer storedVal = getParamStoredValue(param.num)
        if (state.resyncAll || storedVal != param.value) {
            logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
            cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
            cmds << secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
        }
    }
    cmds << secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
    cmds += getConfigureAssocsCmds()
    state.resyncAll = false
    return cmds
}

private secureCmd(cmd) {
    try {
        if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
            return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
        } else {
            return cmd.format()
        }
    }  catch (ex) {
        logErr "secureCmd throw  $ex"
    }
}

private updateLastCheckIn() {
    if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
        state.lastCheckInTime = new Date().time
        sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
    }
}

private convertToLocalTimeString(dt) {
    try {
        def timeZoneId = location?.timeZone?.ID
        if (timeZoneId) {
            return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
        } else {
            return "$dt"
        }
    }  catch (ex) {
        logErr "convertToLocalTimeString throw  $ex"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logTrace "${cmd}"
    sendEvent(name:  "syncStatus", value:  "Syncing...", displayed:  false)
    runIn(4, refreshSyncStatus)

    def param = configParams.find { it.num == cmd.parameterNumber }
    if (param) {
        def val = cmd.scaledConfigurationValue
        logDebug "${param.name}(#${param.num}) = ${val}"
        state["configParam${param.num}"] = val
    } else {
        logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
    }
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    logInfo "VersionReport: ${cmd}"
    def subVersion = String.format("%02d", cmd.applicationSubVersion)
    def fullVersion = "${cmd.applicationVersion}.${subVersion}"

    sendEvent(name:  "firmwareVersion", value:  fullVersion)
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logInfo "BasicReport:${cmd}"
    sendSwitchEvents(cmd.value, "physical")
    return []
}

def refreshSyncStatus() {
    def changes = pendingChanges
    sendEvent(name:  "syncStatus", value:  (changes ?  "${changes} Pending Changes" : "Synced"), displayed:  false)
}

private static getCommandClassVersions() {
    [
        0x20: 1,	// Basic
        0x26: 3,	// Switch Multilevel (4)
        0x55: 1,	// Transport Service
        0x59: 1,	// AssociationGrpInfo
        0x5A: 1,	// DeviceResetLocally
        0x5B: 1,	// CentralScene (3)
        0x5E: 2,	// ZwaveplusInfo
        0x6C: 1,	// Supervision
        0x70: 2,	// Configuration
        0x72: 2,	// ManufacturerSpecific
        0x73: 1,	// Powerlevel
        0x7A: 2,	// Firmware Update Md (3)
        0x80: 1,	// Battery
        0x84: 2,	// WakeUp
        0x85: 2,	// Association
        0x86: 1,	// Version (2)
        0x87: 1,	// Indicator
        0x8E: 2,	// MultiChannelAssociation (3)
        0x9F: 1		// Security 2
    ]
}

private int getPendingChanges() {
    int configChanges = safeToInt(configParams.count { it.value != getParamStoredValue(it.num) })
    int pendingWakeUpInterval = (state.wakeUpInterval != 43200 ? 1 : 0)
    int pendingAssocs = getConfigureAssocsCmds(true)?.size()
    return (configChanges + pendingWakeUpInterval + pendingAssocs)
}

private getParamStoredValue(paramNum) {
    return safeToInt(state["configParam${paramNum}"] , null)
}

private static isDuplicateCommand(lastExecuted, allowedMil) {
    !lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

private logErr(msg) {
    log.err "$msg"
}

private logWarn(msg) {
    if (state.debugLoggingEnabled) {
        log.warn "$msg"
    }
}

private logInfo(msg) {
    if (state.debugLoggingEnabled) {
        log.info "$msg"
    }
}

private logDebug(msg) {
    if (state.debugLoggingEnabled) {
        log.debug "$msg"
    }
}

private logTrace(msg) {
    if (state.debugLoggingEnabled) {
        log.trace "$msg"
    }
}

def refresh() {
    refreshSyncStatus()
    state.refreshAll = true
    return []
}

private getConfigureAssocsCmds(boolean countOnly=false) {
    List<String> cmds = []
    boolean failedS2 = (device?.getDataValue("networkSecurityLevel") == "ZWAVE_S2_FAILED")
    for (Integer id in 2..9){
        def name = associationGroups[id]
        boolean changes = false
        def stateNodeIds = state["${name}NodeIds"]
        def settingNodeIds = getAssocDNIsSettingNodeIds(id)
        def newNodeIds = settingNodeIds?.findAll { !(it in stateNodeIds) }
        if (newNodeIds) {
            if (!countOnly) {
                logDebug "Adding Nodes ${newNodeIds} to Association Group ${id}"
            }

            cmds << secureCmd(zwave.associationV2.associationSet(groupingIdentifier: id, nodeId: newNodeIds))
            changes = true
        }

        def oldNodeIds = stateNodeIds?.findAll { !(it in settingNodeIds) }
        if (oldNodeIds) {
            if (!countOnly) {
                logDebug "Removing Nodes ${oldNodeIds} from Association Group ${id}"
            }
            cmds << secureCmd(zwave.associationV2.associationRemove(groupingIdentifier: id, nodeId: oldNodeIds))
            changes = true
        }

        if (!countOnly && !failedS2 && (changes || state.refreshAll)) {
            cmds << secureCmd(zwave.associationV2.associationGet(groupingIdentifier: id))
        }
    }

    if (!countOnly && failedS2 && cmds) {
        // The handler doesn't get association reports for 700 series devices when not joined with S2 so requesting manufacturer report as a way to confirm the device is responding and if it responds then it assumes the association changes were successful.
        cmds << secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
    }

    return cmds
}

private List<Integer> getAssocDNIsSettingNodeIds(int id) {
    String assocSetting = settings["group${id}AssocDNIs"] ?: ""

    List<Integer> nodeIds = convertHexListToIntList(assocSetting?.split(","))

    if (assocSetting && !nodeIds) {
        logWarn "'${assocSetting}' is not a valid value for the 'Device Network Ids for Association Group ${id}' setting.  All z-wave devices have a 2 character Device Network Id and if you're entering more than 1, use commas to separate them."
    } else if (nodeIds?.size() >  10) {
        logDebug "The 'Device Network Ids for Association Group ${id}' setting contains more than 10 Ids so only the first 10 will be associated."
    }

    return nodeIds
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
    logTrace "$cmd"

    runIn(3, refreshSyncStatus)

    state.wakeUpInterval = cmd.seconds

    // Set the Health Check interval so that it can be skipped twice plus 5 minutes.
    def checkInterval = ((cmd.seconds * 2) + (5 * 60))

    sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    logTrace "$cmd"

    runIn(3, refreshSyncStatus)

    logDebug "Group ${cmd.groupingIdentifier} Association: ${cmd.nodeId}"
    saveGroupAssociations(cmd.groupingIdentifier, cmd.nodeId)
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logTrace "$cmd"

    runIn(3, refreshSyncStatus)

    // The handler doesn't get association reports for 700 series devices when not joined with S2 so this report was requested to confirm the device is responding and saved based on the assumption that they were applied.
    for (Integer id in 2..9){
        String assocSetting = settings["group${id}AssocDNIs"] ?: ""
        saveGroupAssociations(id, convertHexListToIntList(assocSetting?.split(",")))
    }
    return []
}

private void saveGroupAssociations(groupId, nodeIds) {
    Integer id  = safeToInt(groupId)
    def name = associationGroups[id]
    if (name) {
        state["${name}NodeIds"] = nodeIds

        def dnis = convertIntListToHexList(nodeIds)?.join(", ") ?: ""
        if (dnis) {
            dnis = "[${dnis}]" // wrapping it with brackets prevents ST from attempting to convert the value into a date.
        }
        sendEvent(name: name, value: dnis, descriptionText: "${device.displayName}: ${name} is ${dnis}", displayed: false)
    }
}

private static List<String> convertIntListToHexList(List<Integer> intList) {
    List<String> hexList = []
    intList?.each {
        hexList.add(Integer.toHexString(it).padLeft(2, "0").toUpperCase())
    }
    return hexList
}

private List<Integer> convertHexListToIntList(String[] hexList) {
    List<Integer> intList = []

    hexList?.each {
        try {
            it = it.trim()
            intList.add(Integer.parseInt(it, 16))
        } catch (e) {
            logErr "convertHexListToIntList throw :$e"
        }
    }
    return intList
}