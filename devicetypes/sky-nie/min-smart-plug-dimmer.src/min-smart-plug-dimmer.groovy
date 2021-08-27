/**
 *      Min Smart Plug Dimmer v2.0.1
 *
 *  	Models: MINOSTON (MP21ZD MP22ZD/ZW39S ZW96SD)
 *
 *  Author:
 *   winnie (sky-nie)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    2.0.1 (08/27/2021)
 *      - Syntax format compliance adjustment
 *      - fix some bugs
 *
 *    2.0.0  (07/30/2021)
 *      - add some fingerprint for new devices
 *
 *    1.1.9 (07/29/2021)
 *      - add a fingerprint for a new device
 *
 *    1.1.8 (07/22/2021)
 *      - remove code about "Temperature Measurement" as beta product.
 *      - change "auto off interval"  and "auto on interval" 's range
 *
 *    1.1.7 (07/22/2021)
 *      - fix a bug about temperature report threshold sync.
 *
 *    1.1.6 (07/13/2021)
 *      - Syntax format compliance adjustment
 *      - Adjust the preferences interface prompts for SmartTings App
 *      - Simplify the process of calling sendHubCommand
 *
 *    1.1.5 (07/13/2021)
 *      - Syntax format compliance adjustment
 *      - delete dummy code
 *
 *    1.1.4 (07/12/2021)
 *    1.1.3 (07/07/2021)
 *      - delete dummy code
 *
 *    1.1.2 (06/30/2021)
 *      - Add new product supported
 *
 *    1.1.1 (05/06/2021)
 *      - 1.Solve the problem that the temperature cannot be displayed normally
 *      - 2.Synchronize some of the latest processing methods, refer to Minoston Door/Window Sensor
 *
 *    1.0.1 (03/17/2021)
 *      - Simplify the code, delete dummy code
 *
 *    1.0.0 (03/11/2021)
 *      - Initial Release
 *
 * Reference：
 *    https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/eva-logik-in-wall-smart-dimmer.src/eva-logik-in-wall-smart-dimmer.groovy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
import groovy.json.JsonOutput

metadata {
    definition (name: "Min Smart Plug Dimmer", namespace: "sky-nie", author: "winnie", ocfDeviceType: "oic.d.smartplug") {
        capability "Actuator"
        capability "Switch"
        capability "Switch Level"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"

        attribute "firmwareVersion", "string"
        attribute "lastCheckIn", "string"
        attribute "syncStatus", "string"

        fingerprint mfr: "0312", prod: "FF00", model: "FF0D", deviceJoinName: "Minoston Dimmer Switch" //MP21ZD
        fingerprint mfr: "0312", prod: "FF07", model: "FF03", deviceJoinName: "Minoston Dimmer Switch" //MP22ZD
        fingerprint mfr: "0312", prod: "AC01", model: "4002", deviceJoinName: "New One Dimmer Switch" //N4002
        fingerprint mfr: "0312", prod: "0004", model: "EE02", deviceJoinName: "Minoston Dimmer Switch", mnmn: "SmartThings", vid:"generic-dimmer" //MS11ZS Minoston Smart Dimmer Switch   
        fingerprint mfr: "0312", prod: "EE00", model: "EE04", deviceJoinName: "Minoston Dimmer Switch", mnmn: "SmartThings", vid:"generic-dimmer" //MS13ZS Minoston Smart Toggle Dimmer Switch  
        fingerprint mfr: "0312", prod: "BB00", model: "BB02", deviceJoinName: "Evalogik Dimmer Switch", mnmn: "SmartThings", vid:"generic-dimmer" //ZW31S Evalogik Smart Dimmer Switch
        fingerprint mfr: "0312", prod: "BB00", model: "BB04", deviceJoinName: "Evalogik Dimmer Switch", mnmn: "SmartThings", vid:"generic-dimmer" //ZW31TS Evalogik Smart Toggle Dimmer Switch
    }

    preferences {
        getConfigParamInput(ledModeParam)
        getConfigParamInput(autoOffIntervalParam)
        getConfigParamInput(autoOnIntervalParam)
        getConfigParamInput(powerFailureRecoveryParam)
        getConfigParamInput(pushDimmingDurationParam)
        getConfigParamInput(holdDimmingDurationParam)
        getConfigParamInput(minimumBrightnessParam)
        input "disclaimer", "paragraph",
                title: "WARNING",
                description: "Configuring for 'Night Light Settings' is only valid for the devices with product number of MP21ZD、MP22ZD、N4002(one of them)",
                required: false
        getConfigParamInput(nightLightParam)
        input "disclaimer", "paragraph",
                title: "WARNING",
                description: "Configuring for 'createButton'、'Maximum Brightness' and 'Paddle Control' are only valid for the devices with product number of MS11ZS、MS13ZS、ZW31S、ZW31TS(one of them)",
                required: false
        getConfigParamInput(maximumBrightnessParam)
        getConfigParamInput(paddleControlParam)
        input(type: "enum", name: "createButton", required: false, title: "Create Button for Paddles?", options: ["No", "Yes"], defaultValue:"Yes")
    }
}

private getConfigParamInput(param) {
    if (param.range) {
        input "configParam${param.num}", "number", title: "${param.name}:", required: false, defaultValue: "${param.value}", range: param.range
    } else {
        input "configParam${param.num}", "enum", title: "${param.name}:", required: false, defaultValue: "${param.value}", options: param.options
    }
}

private initialize() {
    if (state.createButtonEnabled && !childDevices) {
        try {
            def child = addChildButton()
            child?.sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
        } catch (ex) {
            log.error("Unable to create button device because the 'Child Button' DTH is not installed",ex)
        }
    } else if (!state.createButtonEnabled && childDevices) {
        removeChildButton(childDevices[0])
    }
}

private addChildButton() {
    log.warn "Creating Button Device"
    def child = addChildDevice(
            "smartthings",
            "Child Button",
            "${device.deviceNetworkId}-2",
            device.getHub().getId(),
            [
                completedSetup: true,
                isComponent: false,
                label: "plugButton",
                componentLabel: "${device.displayName[0..-8]} Button"
            ]
    )
    child?.sendEvent(name:"supportedButtonValues", value:JsonOutput.toJson(["pushed", "down", "down_2x", "up", "up_2x"]), displayed:false)
    child?.sendEvent(name:"numberOfButtons", value:1, displayed:false)
    sendButtonEvent("pushed")
    return child
}

private removeChildButton(child) {
    try {
        log.warn "Removing ${child.displayName}} "
        deleteChildDevice(child.deviceNetworkId)
    } catch (ex) {
        log.error("Unable to remove ${child.displayName}!  Make sure that the device is not being used by any SmartApps.",ex)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd){
    if (state.lastSequenceNumber != cmd.sequenceNumber) {
        state.lastSequenceNumber = cmd.sequenceNumber
        logTrace "${cmd}"
        def paddle = (cmd.sceneNumber == 1) ? "down" : "up"
        def btnVal
        switch (cmd.keyAttributes){
            case 0:
                btnVal = paddle
                break
            case 1:
                logDebug "Button released not supported"
                break
            case 2:
                logDebug "Button held not supported"
                break
            case 3:
                btnVal = paddle + "_2x"
                break
        }

        if (btnVal) {
            sendButtonEvent(btnVal)
        }
    }
    return []
}

private sendButtonEvent(value) {
    if (childDevices) {
        childDevices[0].sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
    }
}

def ping() {
    logDebug "ping()..."
    return [ switchMultilevelGetCmd() ]
}

def refresh() {
    logDebug "refresh()..."
    refreshSyncStatus()
    return [ switchMultilevelGetCmd() ]
}

private switchMultilevelGetCmd() {
    return secureCmd(zwave.switchMultilevelV3.switchMultilevelGet())
}

def installed() {
    logDebug "installed()..."
    if (isButtonAvailable()) {
        state.createButtonEnabled = true
    }
    sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    state.refreshConfig = true
}

private static def getCheckInterval() {
    // These are battery-powered devices, and it's not very critical
    // to know whether they're online or not – 12 hrs
    return (60 * 60 * 3) + (5 * 60)
}

def updated() {
    if (!isDuplicateCommand(state.lastUpdated, 5000)) {
        state.lastUpdated = new Date().time

        logDebug "updated()..."
        if (device.latestValue("checkInterval") != checkInterval) {
            sendEvent(name: "checkInterval", value: checkInterval, displayed: false)
        }

        if (isButtonAvailable()) {
            state.createButtonEnabled = (safeToInt(settings?.createButton) != 0)
            initialize()
        }
        runIn(5, executeConfigureCmds, [overwrite: true])
    }

    return []
}

def configure() {
    logDebug "configure()..."

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
    runIn(6, refreshSyncStatus)

    def cmds = []

    configParams.each { param ->
        def storedVal = getParamStoredValue(param.num)
        def paramVal = param.value
        if (isButtonAvailable()) {
            if ((param == paddleControlParam) && state.createButtonEnabled && (param.value == 2)) {
                log.warn "Only 'pushed', 'up_2x', and 'down_2x' button events are supported when Paddle Control is set to Toggle."
            }
        }
        if (state.resyncAll || ("${storedVal}" != "${paramVal}")) {
            cmds << secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: paramVal))
            cmds << secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
        }
    }

    state.resyncAll = false
    if (cmds) {
        sendHubCommand(cmds, 500)
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
            logDebug "Unable to parse description: $description"
        }

        sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
    } catch (e) {
        log.error "$e"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapCmd = cmd.encapsulatedCommand(commandClassVersions)

    def result = []
    if (encapCmd) {
        result += zwaveEvent(encapCmd)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logTrace "ConfigurationReport ${cmd}"

    sendEvent(name:  "syncStatus", value:  "Syncing...", displayed:  false)
    runIn(4, refreshSyncStatus)

    def param = configParams.find { it.num == cmd.parameterNumber }
    if (param) {
        def val = cmd.scaledConfigurationValue
        logDebug "${param.name}(#${param.num}) = ${val}"
        state["configParam${param.num}"] = val
    } else {
        logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
    }
    return []
}

def refreshSyncStatus() {
    def changes = pendingChanges
    sendEvent(name: "syncStatus", value:  (changes ?  "${changes} Pending Changes" : "Synced"), displayed:  false)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logDebug "Ignored Command: $cmd"
    return []
}

private secureCmd(cmd) {
    try {
        if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
            return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
        } else {
            return cmd.format()
        }
    } catch (ex) {
        log.error("caught exception", ex)
    }
}

private static getCommandClassVersions() {
    [
        0x20: 1,	// Basic
        0x26: 3,	// Switch Multilevel
        0x5B: 1,	// CentralScene (3)
        0x55: 1,	// Transport Service
        0x59: 1,	// AssociationGrpInfo
        0x5A: 1,	// DeviceResetLocally
        0x5E: 2,	// ZwaveplusInfo
        0x71: 3,	// Notification
        0x6C: 1,	// Supervision
        0x70: 1,	// Configuration
        0x7A: 2,	// FirmwareUpdateMd
        0x72: 2,	// ManufacturerSpecific
        0x73: 1,	// Powerlevel
        0x85: 2,	// Association
        0x86: 1,	// Version (2)
        0x8E: 2,	// Multi Channel Association
        0x98: 1,	// Security S0
        0x9F: 1		// Security S2
    ]
}

private getPendingChanges() {
    return configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }
}

private getParamStoredValue(paramNum) {
    return safeToInt(state["configParam${paramNum}"] , null)
}

// Configuration Parameters
private getConfigParams() {
    [
        ledModeParam,
        autoOffIntervalParam,
        autoOnIntervalParam,
        nightLightParam,
        powerFailureRecoveryParam,
        pushDimmingDurationParam,
        holdDimmingDurationParam,
        minimumBrightnessParam,
        maximumBrightnessParam,
        paddleControlParam
    ]
}

private static getPaddleControlOptions() {
    return [
        "0":"Normal",
        "1":"Reverse",
        "2":"Toggle"
    ]
}

private getPaddleControlParam() {
    return getParam(1, "Paddle Control", 1, 0, paddleControlOptions)
}

private getLedModeParam() {
    return getParam(2, "LED Indicator Mode", 1, 0, ledModeOptions)
}

private getAutoOffIntervalParam() {
    return getParam(4, "Auto Turn-Off Timer(0, Disabled; 1 - 65535 minutes)", 4, 0, null, "0..65535")
}

private getAutoOnIntervalParam() {
    return getParam(6, "Auto Turn-On Timer(0, Disabled; 1 - 65535 minutes)", 4, 0, null, "0..65535")
}

private getNightLightParam() {
    return getParam(7, "Night Light Settings(1 - 10:10% - 100%)", 1, 2, null, "1..10")
}

private getPowerFailureRecoveryParam() {
    def defaultVal = isButtonAvailable()? 0:2
    return getParam(8, "Power Failure Recovery", 1, defaultVal, powerFailureRecoveryOptions)
}

private getPushDimmingDurationParam() {
    def defaultVal = isButtonAvailable()? 1:2
    return getParam(9, "Push Dimming Duration(0, Disabled; 1 - 10 Seconds)", 1, defaultVal, null, "0..10")
}

private getHoldDimmingDurationParam() {
    return getParam(10, "Hold Dimming Duration(1 - 10 Seconds)", 1, 4, null, "1..10")
}

private getMinimumBrightnessParam() {
    return getParam(11, "Minimum Brightness(0, Disabled; 1 - 99:1% - 99%)", 1, 10, null,"0..99")
}

private getMaximumBrightnessParam() {
    return getParam(12, "Maximum Brightness(0, Disabled; 1 - 99:1% - 99%)", 1, 99, null,"0..99")
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

private static getLedModeOptions() {
    return [
        "0":"Off When On",
        "1":"On When On",
        "2":"Always Off",
        "3":"Always On"
    ]
}

private static getPowerFailureRecoveryOptions() {
    return [
        "0":"Turn Off",
        "1":"Turn On",
        "2":"Restore Last State"
    ]
}

private static validateRange(val, defaultVal, lowVal, highVal) {
    val = safeToInt(val, defaultVal)
    if (val > highVal) {
        return highVal
    } else if (val < lowVal) {
        return lowVal
    } else {
        return val
    }
}

private static safeToInt(val, defaultVal=0) {
    return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private convertToLocalTimeString(dt) {
    def timeZoneId = location?.timeZone?.ID
    if (timeZoneId) {
        return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
    } else {
        return "$dt"
    }
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

def on() {
    logDebug "on()..."
    return [ basicSetCmd(0xFF) ]
}

def off() {
    logDebug "off()..."
    return [ basicSetCmd(0x00) ]
}

def setLevel(level) {
    logDebug "setLevel($level)..."
    return setLevel(level, 1)
}

def setLevel(level, duration) {
    logDebug "setLevel($level, $duration)..."
    if (duration > 30) {
        duration = 30
    }
    return [ switchMultilevelSetCmd(level, duration) ]
}

private basicSetCmd(val) {
    return secureCmd(zwave.basicV1.basicSet(value: val))
}

private switchMultilevelSetCmd(level, duration) {
    def levelVal = validateRange(level, 99, 0, 99)
    def durationVal = validateRange(duration, 1, 0, 100)
    return secureCmd(zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: durationVal, value: levelVal))
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    logTrace "VersionReport: ${cmd}"
    def subVersion = String.format("%02d", cmd.applicationSubVersion)
    def fullVersion = "${cmd.applicationVersion}.${subVersion}"
    sendEvent(name: "firmwareVersion",  value:fullVersion, displayed: true, type:  null)
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logTrace "${cmd}"
    sendSwitchEvents(cmd.value, "physical")
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    logTrace "${cmd}"
    sendSwitchEvents(cmd.value, "digital")
    return []
}

private sendSwitchEvents(rawVal, type) {
    def switchVal = rawVal ? "on" : "off"
    sendEvent(name: "switch",  value:switchVal, displayed: true, type: type)
    if (rawVal) {
        sendEvent(name: "level",  value:rawVal, displayed: true, type: type, unit:"%")
    }
    if(isButtonAvailable()) {
        def paddlesReversed = (paddleControlParam.value == 1)
        if (state.createButtonEnabled && (type == "physical") && childDevices) {
            if (paddleControlParam.value == 2) {
                sendButtonEvent("pushed")
            } else {
                def btnVal = ((rawVal && !paddlesReversed) || (!rawVal && paddlesReversed)) ? "up" : "down"
                def oldSwitch = device.currentValue("switch")
                def oldLevel = device.currentValue("level")
                if ((oldSwitch == "on") && (btnVal == "up") && (oldLevel > rawVal)) {
                    btnVal = "down"
                }
                sendButtonEvent(btnVal)
            }
        }
    }
}

private isButtonAvailable() {
    if(device == null){
        log.error "isButtonAvailable device = null"
        return true
    }else{
        log.debug "isButtonAvailable device.rawDescription = ${device.rawDescription}"
        def v20 = "${device.rawDescription}".contains("model:EE02")
        def v21 = "${device.rawDescription}".contains("model:EE04")
        def v22 = "${device.rawDescription}".contains("model:BB02")
        def v23 = "${device.rawDescription}".contains("model:BB04")
        def v2 = v20||v21||v22||v23
        return v2
    }
}