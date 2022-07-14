/**
 *  In-Wall Smart Switch v1.0.0
 *
 *  	Models: MS10ZS/MS12ZS/ZW30/ZW30S/ZW30TS
 *
 *  Author:
 *   winnie (sky-nie)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.0.0 (12/22/2021)
 *      - Initial Release
 *
 * Reference：
 *  https://github.com/krlaframboise/SmartThings/blob/master/devicetypes/krlaframboise/eva-logik-in-wall-smart-switch.src/eva-logik-in-wall-smart-switch.groovy
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
    definition (name: "In-Wall Smart Switch", namespace: "sky-nie", author: "winnie", mnmn: "SmartThings", vid:"generic-switch") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"

        attribute "firmwareVersion", "string"
        attribute "syncStatus", "string"

        fingerprint mfr: "0312", prod: "EE00", model: "EE01", deviceJoinName: "Minoston Switch", ocfDeviceType: "oic.d.switch"    //MS10ZS Minoston Smart Switch
        fingerprint mfr: "0312", prod: "EE00", model: "EE03", deviceJoinName: "Minoston Switch", ocfDeviceType: "oic.d.switch"    //MS12ZS Minoston Smart on/off Toggle Switch
        fingerprint mfr: "0312", prod: "A000", model: "A005", deviceJoinName: "Evalogik Switch", ocfDeviceType: "oic.d.switch"    //ZW30
        fingerprint mfr: "0312", prod: "BB00", model: "BB01", deviceJoinName: "Evalogik Switch", ocfDeviceType: "oic.d.switch"    //ZW30S Evalogik Smart on/off Switch
        fingerprint mfr: "0312", prod: "BB00", model: "BB03", deviceJoinName: "Evalogik Switch", ocfDeviceType: "oic.d.switch"    //ZW30TS Evalogik Smart on/off Toggle Switch
    }

    preferences {
        configParams.each {
            if (it.range) {
                input "configParam${it.num}", "number", title: "${it.name}:", required: false, defaultValue: "${it.value}", range: it.range
            } else {
                input "configParam${it.num}", "enum", title: "${it.name}:", required: false, defaultValue: "${it.value}", options: it.options
            }
        }
    }
}

def installed() {
    logDebug "installed()..."
    sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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

def ping() {
    logDebug "ping()..."
    return [ switchBinaryGetCmd() ]
}

def on() {
    logDebug "on()..."
    return [ switchBinarySetCmd(0xFF) ]
}

def off() {
    logDebug "off()..."
    return [ switchBinarySetCmd(0x00) ]
}

def refresh() {
    logDebug "refresh()..."
    refreshSyncStatus()
    return [ switchBinaryGetCmd() ]
}

private switchBinaryGetCmd() {
    return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
    return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

private secureCmd(cmd) {
    try {
        if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
            return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
        } else {
            return cmd.format()
        }
    } catch (ex) {
        log.error("secureCmd exception", ex)
        return cmd.format()
    }
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
    } catch (e) {
        log.error "${e}"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    logTrace "SecurityMessageEncapsulation: ${cmd}"
    def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
    def result = []
    if (encapsulatedCmd) {
        result += zwaveEvent(encapsulatedCmd)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logTrace "ConfigurationReport: ${cmd}"
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
    logTrace "BasicReport: ${cmd}"
    sendSwitchEvents(cmd.value, "physical")
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    logTrace "SwitchBinaryReport: ${cmd}"
    sendSwitchEvents(cmd.value, "digital")
    return []
}

private sendSwitchEvents(rawVal, type) {
    def switchVal = (rawVal == 0xFF) ? "on" : "off"
    sendEvent(name:  "switch", value: switchVal, displayed:  true, type:  type)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logDebug "Unhandled zwaveEvent: $cmd"
    return []
}

def refreshSyncStatus() {
    def changes = pendingChanges
    sendEvent(name:  "syncStatus", value:  (changes ?  "${changes} Pending Changes" : "Synced"), displayed:  false)
}

private static getCommandClassVersions() {
    [
        0x20: 1,	// Basic
        0x25: 1,	// Switch Binary
        0x55: 1,	// Transport Service
        0x59: 1,	// AssociationGrpInfo
        0x5A: 1,	// DeviceResetLocally
        0x27: 1,	// Switch All
        0x5E: 2,	// ZwaveplusInfo
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
    return safeToInt(state["configVal${paramNum}"] , null)
}

private getConfigParams() {
    return [
        ledModeParam,
        autoOffIntervalParam,
        autoOnIntervalParam,
        powerFailureRecoveryParam,
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
    return getParam(2, "LED Indicator Mode", 1, 0,  alternativeLedOptions)
}

private getAutoOffIntervalParam() {
    return getParam(4, "Auto Turn-Off Timer(0, Disabled; 1 - 65535 minutes)", 4, 0, null, "0..65535")
}

private getAutoOnIntervalParam() {
    return getParam(6, "Auto Turn-On Timer(0, Disabled; 1 - 65535 minutes)", 4, 0, null, "0..65535")
}

private getPowerFailureRecoveryParam() {
    return getParam(8, "Power Failure Recovery", 1, 2, powerFailureRecoveryOptions)
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

private getAlternativeLedOptions() {
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

private static safeToInt(val, defaultVal = 0) {
    return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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