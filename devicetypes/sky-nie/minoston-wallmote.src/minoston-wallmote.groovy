/**
 *  Minoston Wallmote v1.0.3
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
 *
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

metadata {
    definition (name: "Minoston Wallmote", namespace: "sky-nie", author: "winnie", ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true) {
        capability "Actuator"
        capability "Button"
        capability "Battery"
        capability "Configuration"
        capability "Sensor"
        capability "Health Check"

        attribute "firmwareVersion", "string"
        attribute "lastCheckIn", "string"
        attribute "syncStatus", "string"

        fingerprint mfr: "0312", prod: "0924", model: "D001", deviceJoinName: "Minoston Remote Control", mnmn: "SmartThings", vid: "generic-4-button"//MR40Z
    }

    preferences {
        configParams.each {
            if (it.range) {
                input "configParam${it.num}", "number", title: "${it.name}:", required: false, defaultValue: "${it.value}", range: it.range
            } else {
                input "configParam${it.num}", "enum",  title: "${it.name}:", required: false, defaultValue: "${it.value}", options:it.options
            }
        }
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
        runIn(5, executeConfigureCmds, [overwrite: true])
    }
    return []
}

def configure() {
    sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "zwave", scheme:"untracked"].encodeAsJson(), displayed: false)

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

def parse(String description) {
    def result = []
    try {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result += zwaveEvent(cmd)
        } else {
            log.warn "Unable to parse: $description"
        }

        updateLastCheckIn()
    } catch (e) {
        log.error "${e}"
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
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
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
    results += response([
            secureCmd(zwave.batteryV1.batteryGet()),
            "delay 2000",
            secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())
    ])
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
            child = addChildDevice("Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
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
        log.error "Child device $childDni not found"
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
    return getParam(1, "Battery report threshold\n(1% - 20%)", 1, 10, null,"1..20")
}

private getLowBatteryAlarmReportParam() {
    return getParam(2, "Low battery alarm report\n(5% - 20%)", 1, 5, null, "5..20")
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

private static safeToInt(val, defaultVal=0) {
    return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

def executeConfigureCmds() {
    runIn(6, refreshSyncStatus)

    def cmds = []

    cmds << secureCmd(zwave.batteryV1.batteryGet())
    cmds << secureCmd(zwave.wakeUpV2.wakeUpNoMoreInformation())

    if (state.resyncAll || !device.currentValue("firmwareVersion")) {
        cmds << secureCmd(zwave.versionV1.versionGet())
    }

    configParams.each { param ->
        def storedVal = getParamStoredValue(param.num)
        if (state.resyncAll || ("${storedVal}" != "${param.value}")) {
            logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
            cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: param.value))
            cmds << secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
        }
    }

    state.resyncAll = false
    if (cmds) {
        sendCommands(delayBetween(cmds, 500))
    }
    return []
}

private sendCommands(cmds) {
    if (cmds) {
        def actions = []
        cmds.each {
            actions << new physicalgraph.device.HubAction(it)
        }
        sendHubCommand(actions)
    }
    return []
}

private secureCmd(cmd) {
    try {
        if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
            return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
        } else {
            return cmd.format()
        }
    }  catch (ex) {
        return cmd.format()
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
        return "$dt"
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
        state["configVal${param.num}"] = val
    } else {
        logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
    }
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    logTrace "VersionReport: ${cmd}"

    def subVersion = String.format("%02d", cmd.applicationSubVersion)
    def fullVersion = "${cmd.applicationVersion}.${subVersion}"

    sendEvent(name:  "firmwareVersion", value:  fullVersion)
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logTrace "${cmd}"
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
            0x9F: 1     // Security 2
    ]
}

private getPendingChanges() {
    return configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }
}

private getParamStoredValue(paramNum) {
    return safeToInt(state["configVal${paramNum}"] , null)
}

private static isDuplicateCommand(lastExecuted, allowedMil) {
    !lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

private logDebug(msg) {
    log.debug "$msg"
}

private logTrace(msg) {
    log.trace "$msg"
}