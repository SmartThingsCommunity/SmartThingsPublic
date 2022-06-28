/**
 *  Minoston Wallmote v1.0.4
 *
 *  	Models: EvaLogik (ZW924) / MINOSTON (MR40Z)
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
 * Reference:
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
import groovy.transform.Field
private static Map getCommandClassVersions() {
    [
            0x20: 1,	// Basic                      //BasicReport
            0x26: 3,	// Switch Multilevel (4)      //SwitchMultilevelReport
            0x55: 1,	// Transport Service
            0x59: 1,	// AssociationGrpInfo         //AssociationGroupInfoReport
            0x5A: 1,	// DeviceResetLocally         //DeviceResetLocallyNotification
            0x5B: 1,	// CentralScene (3)           //CentralSceneNotification
            0x5E: 2,	// ZwaveplusInfo
            0x6C: 1,	// Supervision                //SupervisionGet
            0x70: 2,	// Configuration              //ConfigurationReport
            0x72: 2,	// ManufacturerSpecific       //ManufacturerSpecificReport
            0x73: 1,	// Powerlevel
            0x7A: 2,	// Firmware Update Md (3)     //FirmwareMdReport
            0x80: 1,	// Battery                    //BatteryReport
            0x84: 2,	// WakeUp                     //WakeUpIntervalReport
            0x85: 2,	// Association                //AssociationReport
            0x86: 1,	// Version (2)                //VersionReport
            0x87: 1,	// Indicator                  //IndicatorReport
            0x8E: 2,	// MultiChannelAssociation (3)//MultiChannelAssociationReport
            0x9F: 1 	// Security 2
    ]
}

@Field static int wakeUpInterval = 43200

private static getAssociationGroups() {
    [
        2:"associationGroupTwo",
        3:"associationGroupThree",
        4:"associationGroupFour",
        5:"associationGroupFive",
        6:"associationGroupSix",
        7:"associationGroupSeven",
        8:"associationGroupEight",
        9:"associationGroupNine"
    ]
}

metadata {
    definition (
            name:"Minoston Wallmote(MR40Z)",
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

        associationGroups.each { group, name ->
            attribute name, "string"
        }
        attribute "syncStatus", "string"
        attribute "firmwareVersion", "string"
        attribute "lastCheckIn", "string"

        fingerprint model: "D001", mfr: "0312", prod: "0924", deviceJoinName: "Minoston Remote Control Switch", inClusters:"0x5E,0x55,0x9F,0x6C"
        fingerprint model: "D001", mfr: "0312", prod: "0924", deviceJoinName: "Minoston Remote Control Switch", inClusters:"0x5E,0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x80,0x5B,0x26,0x9F,0x70,0x84,0x6C,0x7A"
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
        associationGroups.each { group, name ->
            if(group == 4 || group == 5){
                input "group${group}AssocDNIs", "string", title: "Enter Device Network Ids for Group ${group} Dimming Association:", required: false
            }else{
                input "group${group}AssocDNIs", "string", title: "Enter Device Network Ids for Group ${group} On/Off Association:", required: false
            }
        }
    }
}

void installed() {
    logDebug "installed()..."

    state.refreshAll = true

    initialize()
}

void updated() {
    if (!isDuplicateCommand(state.lastUpdated, 2000)) {
        state.lastUpdated = new Date().time

        logDebug "updated()..."

        initialize()

        if (pendingChanges) {
            logForceWakeupMessage "The setting changes will be sent to the device the next time it wakes up."
        }

        refreshSyncStatus()
    }
}

void configure() {
    logDebug "configure()..."

    sendCommands(getConfigureCmds())
}

List<String> getConfigureCmds() {
    List<String> cmds = []

    int changes = pendingChanges
    if (changes) {
        log.warn "Syncing ${changes} Change(s)"
    }

    if (state.refreshAll || !device.currentValue("firmwareVersion")) {
        logDebug "Requesting Version Report"
        cmds << secureCmd(zwave.versionV1.versionGet())
    }

    if (state.refreshAll || !device.currentValue("battery")) {
        logDebug "Requesting Battery Report"
        cmds << secureCmd(zwave.batteryV1.batteryGet())
    }

    if (state.wakeUpInterval != wakeUpInterval) {
        logDebug "Setting Wake Up Interval to ${wakeUpInterval} Seconds"
        cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalSet(seconds:wakeUpInterval, nodeid:zwaveHubNodeId))
        cmds << secureCmd(zwave.wakeUpV2.wakeUpIntervalGet())
    }

    configParams.each { param ->
        Integer storedVal = getParamStoredValue(param.num)
        if (state.refreshAll || storedVal != param.value) {
            logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
            cmds << secureCmd(zwave.configurationV2.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
            cmds << secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
        }
    }

    cmds += getConfigureAssocsCmds()

    state.refreshAll = false

    return cmds
}

private getConfigureAssocsCmds(boolean countOnly=false) {
    List<String> cmds = []

    boolean failedS2 = device?.getDataValue("networkSecurityLevel") == "ZWAVE_S2_FAILED"
    associationGroups.each { group, name ->
        boolean changes = false

        def stateNodeIds = state["${name}NodeIds"]
        def settingNodeIds = getAssocDNIsSettingNodeIds(group)

        def newNodeIds = settingNodeIds?.findAll { !(it in stateNodeIds) }
        if (newNodeIds) {
            if (!countOnly) {
                logDebug "Adding Nodes ${newNodeIds} to Association Group ${group}"
            }

            cmds << secureCmd(zwave.associationV2.associationSet(groupingIdentifier: group, nodeId: newNodeIds))
            changes = true
        }

        def oldNodeIds = stateNodeIds?.findAll { !(it in settingNodeIds) }
        if (oldNodeIds) {
            if (!countOnly) {
                logDebug "Removing Nodes ${oldNodeIds} from Association Group ${group}"
            }
            cmds << secureCmd(zwave.associationV2.associationRemove(groupingIdentifier: group, nodeId: oldNodeIds))
            changes = true
        }

        if (!countOnly && !failedS2 && (changes || state.refreshAll)) {
            cmds <<secureCmd(zwave.associationV2.associationGet(groupingIdentifier: group))
        }
    }

    if (!countOnly && failedS2 && cmds) {
        // The handler doesn't get association reports for 700 series devices when not joined with S2 so requesting manufacturer report as a way to confirm the device is responding and if it responds then it assumes the association changes were successful.
        cmds << secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
    }
    return cmds
}

List<Integer> getAssocDNIsSettingNodeIds(int group) {
    String assocSetting = settings["group${group}AssocDNIs"]
    List<Integer> nodeIds = []
    if(assocSetting){
        nodeIds = convertHexListToIntList(assocSetting?.split(","))
    }
    return nodeIds
}

void refresh() {
    logDebug "refresh()..."

    refreshSyncStatus()
    state.refreshAll = true

    logForceWakeupMessage "The next time the device wakes up, the sensor data will be requested."
}

String wakeUpNoMoreInfoCmd() {
    return secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
}

void parse(String description) {
    def cmd = zwave.parse(description, commandClassVersions)
    if (cmd) {
        zwaveEvent(cmd)
    } else {
        log.error "Unable to parse: $description"
    }

    updateLastCheckIn()
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
    log.info "WakeUpIntervalReport:${cmd}"
    runIn(3, refreshSyncStatus)
    state.wakeUpInterval = cmd.seconds

    // Set the Health Check interval so that it can be skipped twice plus 5 minutes.
    def checkInterval = ((cmd.seconds * 2) + (5 * 60))

    sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

void zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    log.info "WakeUpNotification:${cmd}"
    runIn(3, refreshSyncStatus)
    state.hasWokenUp = true
    List<String> cmds = getConfigureCmds()
    cmds << wakeUpNoMoreInfoCmd()
    sendCommands(cmds)
}

void zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.info "ConfigurationReport:${cmd}"
    sendEvent(name:  "syncStatus", value:  "Syncing...", displayed:  false)
    runIn(3, refreshSyncStatus)
    Map param = configParams.find { it.num == cmd.parameterNumber }
    if (param) {
        state["configVal${param.num}"] = cmd.scaledConfigurationValue
    } else {
        logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
    }
}

void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    log.info "AssociationReport:${cmd}"

    runIn(3, refreshSyncStatus)

    logDebug "Group ${cmd.groupingIdentifier} Association: ${cmd.nodeId}"

    if( cmd.nodeId &&  cmd.nodeId != []){
        saveGroupAssociations(cmd.groupingIdentifier, cmd.nodeId)
    }
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.info "VersionReport:${cmd}"
    String subVersion = String.format("%02d", cmd.applicationSubVersion)
    String fullVersion = "${cmd.applicationVersion}.${subVersion}"

    logDebug "Firmware Version: ${fullVersion}"

    sendEventIfNew("firmwareVersion", fullVersion.toBigDecimal())
}

void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    log.info "BasicReport:${cmd}"
    sendSwitchEvents(cmd.value, "physical")
}

void zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    log.info "SwitchMultilevelReport:${cmd}"
    sendSwitchEvents(cmd.value, "digital")
}

private sendSwitchEvents(rawVal, type) {
    def switchVal = (rawVal == 0xFF) ? "on" : "off"
    sendEvent(name:  "switch", value: switchVal, displayed:  true, type:  type)
}

void zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    log.info "BatteryReport:${cmd}"
    int val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)

    if (val > 100) {
        val = 100
    }

    logDebug "Battery is ${val}%"
    sendEvent(name:"battery", value:val, unit:"%", isStateChange: true)
}

void zwaveEvent(physicalgraph.zwave.Command cmd) {
    def linkText = device.label ?: device.name
    [linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

List<Map> getConfigParams() {
    return [
            batteryReportThresholdParam,
            lowBatteryAlarmReportParam,
            ledIndicator1ColorParam,
            ledIndicator2ColorParam,
            ledIndicator3ColorParam,
            ledIndicator4ColorParam,
            ledIndicatorBrightnessParam
    ]
}

Map getBatteryReportThresholdParam() {
    return getParam(1, "Battery report threshold(1% - 20%)", 1, 10, null,"1..20")
}

Map getLowBatteryAlarmReportParam() {
    return getParam(2, "Low battery alarm report(5% - 20%)", 1, 5, null, "5..20")
}

Map getLedIndicator1ColorParam() {
    return getParam(3, "Led Indicator Color for the First Button", 1, 0, ledColorOptions)
}

Map getLedIndicator2ColorParam() {
    return getParam(4, "Led Indicator Color for the Second Button", 1, 1, ledColorOptions)
}

Map getLedIndicator3ColorParam() {
    return getParam(5, "Led Indicator Color for the Third Button", 1, 2, ledColorOptions)
}

Map getLedIndicator4ColorParam() {
    return getParam(6, "Led Indicator Color for the Fourth Button", 1, 3, ledColorOptions)
}

Map getLedIndicatorBrightnessParam() {
    return getParam(7, "Led Indicator Brightness", 1, 5, brightnessOptions)
}

private getParam(num, name, size, defaultVal, options = null, range = null) {
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
    def it2
    (1..10).each {
        it2 = 10*it
        options["${it}"] = "${it2}% Brightness"
    }
    return options
}

private static getButtonAttributesMap() {
    [
            0: "pushed",
            1: "released",// Although "released" will be ignored by SmartThings, in order to maintain code consistency, the relevant code is retained here
            2: "held",
            3: "double",
            4: "pushed_3x"
    ]
}

Integer getParamStoredValue(Integer paramNum, Integer defaultVal=null) {
    return safeToInt(state["configVal${paramNum}"] , defaultVal)
}

void refreshSyncStatus() {
    int changes = pendingChanges
    sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Change(s)<br>Tap Up x 7" : "Synced"), false)
}

int getPendingChanges() {
    int configChanges = safeToInt(configParams.count { it.value != getParamStoredValue(it.num) })
    int pendingWakeUpInterval = (state.wakeUpInterval != wakeUpInterval ? 1 : 0)
    int pendingAssocs = getConfigureAssocsCmds(true)?.size()

    return (configChanges + pendingWakeUpInterval + pendingAssocs)
}

void logForceWakeupMessage(String msg) {
    log.warn "${msg}  You can force the device to wake up immediately by tapping the thiid paddle 5x."
}

void sendEventIfNew(String name, value, boolean displayed=true) {
    String desc = "${device.displayName}: ${name} is ${value}"
    if (device.currentValue(name) != value) {
        logDebug(desc)
        sendEvent(name: name, value: value, descriptionText: desc, displayed: displayed)
    }
}

static List<String> convertIntListToHexList(List<Integer> intList) {
    List<String> hexList = []
    intList?.each {
        hexList.add(Integer.toHexString(it).padLeft(2, "0").toUpperCase())
    }
    return hexList
}

static List<Integer> convertHexListToIntList(String[] hexList) {
    List<Integer> intList = []

    hexList?.each {
        try {
            it = it.trim()
            intList.add(Integer.parseInt(it, 16))
        }  catch (ex) {
            log.error "convertToLocalTimeString throw  $ex"
        }
    }
    return intList
}

static Integer safeToInt(val, Integer defaultVal=0) {
    if ("${val}"?.isInteger()) {
        return "${val}".toInteger()
    } else if ("${val}".isDouble()) {
        return "${val}".toDouble()?.round()
    } else {
        return  defaultVal
    }
}

void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    log.info "SecurityMessageEncapsulation:${cmd}"
    def encapCmd = cmd.encapsulatedCommand(commandClassVersions)
    if (encapCmd) {
        zwaveEvent(encapCmd)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
        createEvent(descriptionText: cmd.toString())
    }
}

void zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.info "ManufacturerSpecificReport:${cmd}"
    runIn(3, refreshSyncStatus)
    associationGroups.each { group, name ->
        def settingNodeIds = getAssocDNIsSettingNodeIds(group)
        if(settingNodeIds && settingNodeIds != []){
            saveGroupAssociations(group, settingNodeIds)
        }
    }
}

void saveGroupAssociations(groupId, nodeIds) {
    logTrace "saveGroupAssociations(${groupId}, ${nodeIds})"
    runIn(3, refreshSyncStatus)

    String name = associationGroups.get(safeToInt(groupId))
    if (name) {
        state["${name}NodeIds"] = nodeIds

        def dnis = convertIntListToHexList(nodeIds)?.join(", ") ?: ""
        if (dnis) {
            dnis = "[${dnis}]" // wrapping it with brackets prevents ST from attempting to convert the value into a date.
        }
        sendEventIfNew(name, dnis, false)
    }
}

void updateLastCheckIn() {
    if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
        state.lastCheckInTime = new Date().time

        sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
    }
}

String convertToLocalTimeString(dt) {
    try {
        def timeZoneId = location?.timeZone?.ID
        if (timeZoneId) {
            return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
        } else {
            return "$dt"
        }
    } catch (ex) {
        return "$dt"
    }
}

static boolean isDuplicateCommand(lastExecuted, allowedMil) {
    !lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

void logDebug(String msg) {
    if (state.debugLoggingEnabled) {
        log.debug(msg)
    }
}

void logTrace(String msg) {
    log.trace(msg)
}

void initialize() {
    if (!childDevices) {
        state.oldLabel = device.label
        for (i in 1..4) {
            def child = addChildDevice("smartthings", "Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
                    [completedSetup: true, label: "${device.displayName} button ${i}",
                     isComponent: true, componentName: "button$i", componentLabel: "Button $i"])
            child.sendEvent(name: "supportedButtonValues", value:  ["pushed", "released", "held", "double", "pushed_3x"].encodeAsJson(), displayed: false)
            child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$child.displayName was pushed", isStateChange: true, displayed: false)
        }
    } else if (device.label != state.oldLabel) {
        childDevices.each {
            def segs = it.deviceNetworkId.split(":")
            def newLabel = "${device.displayName} button ${segs[-1]}"
            it.setLabel(newLabel)
        }
        state.oldLabel = device.label
    }
    state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)
    if (!device.currentValue("numberOfButtons")) {
        sendEvent(name: "numberOfButtons", value: 4, displayed: false)
    }

    if (!device.currentValue("supportedButtonValues")) {
        sendEvent(name: "supportedButtonValues", value: ["pushed", "released", "held", "double", "pushed_3x"].encodeAsJson(), displayed: false)
    }

    if (!device.currentValue("button")) {
        sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
    }

    associationGroups.each { group, name ->
        if (device.currentValue(name) == null) {
            sendEvent(name: name, value: "")
        }
    }
}

void sendCommands(List<String> cmds, Integer delay=500) {
    if (cmds) {
        sendHubCommand(cmds, delay)
    }
}

String secureCmd(cmd) {
    try {
        if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
            return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
        } else {
            return cmd.format()
        }
    } catch (ex) {
        log.error "secureCmd throw  $ex"
    }
}

void zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
    def button = cmd.sceneNumber
    def value = buttonAttributesMap[(int)cmd.keyAttributes]
    if (value) {
        String childDni = "${device.deviceNetworkId}:${button}"
        def child = childDevices.find{it.deviceNetworkId == childDni}
        if (!child) {
            log.error "Child device $childDni not found"
        }
        child?.sendEvent(name: "button", value: value, data: [buttonNumber: 1], descriptionText: "$child.displayName was $value", isStateChange: true)
        createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true, displayed: false)
    }
}

void zwaveEvent(physicalgraph.zwave.commands.associationgrpinfov1.AssociationGroupInfoReport cmd) {
    log.info "AssociationGroupInfoReport:${cmd}"
    def cmds = []
    for (def i = 2; i <= state.groups; i++) {
        cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:i, nodeId:zwaveHubNodeId))
    }
    sendCommands(cmds)
}

void zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    log.info "DeviceResetLocallyNotification:${device.displayName}: received command: $cmd - device has reset itself"
}

void zwaveEvent(physicalgraph.zwave.commands.indicatorv1.IndicatorReport cmd) {
    log.info "IndicatorReport:${cmd}"
}

void zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) {
    log.info "FirmwareMdReport:${cmd}"
}

void zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
    log.info "MultiChannelAssociationReport:${cmd}"
    def cmds = []
    if (cmd.groupingIdentifier == 1) {
        if (cmd.nodeId != [0, zwaveHubNodeId, 1]) {
            log.debug "${device.displayName} - incorrect MultiChannel Association for Group 1! nodeId: ${cmd.nodeId} will be changed to [0, ${zwaveHubNodeId}, 1]"
            cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1)
            cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1])
        } else {
            logging("${device.displayName} - MultiChannel Association for Group 1 correct.","info")
        }
    }
    sendCommands(cmds, 1000)
}