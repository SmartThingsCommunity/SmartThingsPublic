/**
 *      Min Smart Plug v1.0.2
 *
 *  	Models: MINOSTON (MP21Z)
 *
 *  Author:
 *   winnie (sky-nie)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.0.2 (07/07/2021)
 *      - delete dummy code
 *
 *    1.0.1 (03/17/2021)
 *      - Simplify the code, delete dummy code
 *
 *    1.0.0 (03/11/2021)
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

import groovy.transform.Field

@Field static Map ledModeOptions = [0:"On When On", 1:"Off When On", 2:"Always Off"]//, 3:"Always On"

@Field static Map autoOnOffIntervalOptions = [0:"Disabled", 1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 420:"7 Hours", 480:"8 Hours", 540:"9 Hours", 600:"10 Hours", 720:"12 Hours", 1080:"18 Hours", 1440:"1 Day", 2880:"2 Days", 4320:"3 Days", 5760:"4 Days", 7200:"5 Days", 8640:"6 Days", 10080:"1 Week", 20160:"2 Weeks", 30240:"3 Weeks", 40320:"4 Weeks", 50400:"5 Weeks", 60480:"6 Weeks"]

@Field static Map powerFailureRecoveryOptions = [0:"Turn Off", 1:"Turn On", 2:"Restore Last State"]

metadata {
    definition (
            name: "Min Smart Plug",
            namespace: "sky-nie",
            author: "winnie",
            mnmn: "SmartThings",
            vid:"generic-switch",
            ocfDeviceType: "oic.d.smartplug"
    ) {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Light"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"

        attribute "firmwareVersion", "string"
        attribute "lastCheckIn", "string"
        attribute "syncStatus", "string"

        fingerprint mfr: "0312", prod: "C000", model: "C009", deviceJoinName: "Minoston Outlet" // old MP21Z
        fingerprint mfr: "0312", prod: "FF00", model: "FF0C", deviceJoinName: "Minoston Outlet" //MP21Z Minoston Mini Smart Plug
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
        }
        standardTile("refresh", "device.refresh", width: 2, height: 2) {
            state "refresh", label:'Refresh', action: "refresh"
        }
        valueTile("syncStatus", "device.syncStatus", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "syncStatus", label:'${currentValue}'
        }
        standardTile("sync", "device.configure", width: 2, height: 2) {
            state "default", label: 'Sync', action: "configure"
        }
        valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
            state "firmwareVersion", label:'Firmware ${currentValue}'
        }
        main "switch"
        details(["switch", "refresh", "syncStatus", "sync", "firmwareVersion"])
    }
    preferences {
        configParams.each {
            createEnumInput("configParam${it.num}", "${it.name}:", it.value, it.options)
        }
    }
}

private createEnumInput(name, title, defaultVal, options) {
    input name, "enum",
            title: title,
            required: false,
            defaultValue: defaultVal.toString(),
            options: options
}

def installed() {
    logDebug "installed()..."

}

def updated() {
    if (!isDuplicateCommand(state.lastUpdated, 5000)) {
        state.lastUpdated = new Date().time

        logDebug "updated()..."

        runIn(5, executeConfigureCmds, [overwrite: true])
    }
    return []
}

def configure() {
    logDebug "configure()..."

    if (state.resyncAll == null) {
        state.resyncAll = true
        runIn(8, executeConfigureCmds, [overwrite: true])
    }
    else {
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

    if (!device.currentValue("switch")) {
        cmds << switchBinaryGetCmd()
    }

    if (state.resyncAll || !device.currentValue("firmwareVersion")) {
        cmds << versionGetCmd()
    }

    configParams.each { param ->
        def storedVal = getParamStoredValue(param.num)
        def paramVal = param.value

        if (state.resyncAll || ("${storedVal}" != "${paramVal}")) {
            logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${paramVal}"
            cmds << configSetCmd(param, paramVal)
            cmds << configGetCmd(param)
        }
    }

    state.resyncAll = false
    if (cmds) {
        sendCommands(delayBetween(cmds, 500))
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

    sendCommands([switchBinaryGetCmd()])
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

private versionGetCmd() {
    return secureCmd(zwave.versionV1.versionGet())
}

private switchBinaryGetCmd() {
    return secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private switchBinarySetCmd(val) {
    return secureCmd(zwave.switchBinaryV1.switchBinarySet(switchValue: val))
}

private configSetCmd(param, value) {
    return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

private configGetCmd(param) {
    return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private secureCmd(cmd) {
    try {
        if (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" "))) {
            return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
        }
        else {
            return cmd.format()
        }
    }
    catch (ex) {
        return cmd.format()
    }
}

def parse(String description) {
    def result = []
    try {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result += zwaveEvent(cmd)
        }
        else {
            log.warn "Unable to parse: $description"
        }

        updateLastCheckIn()
    }
    catch (e) {
        log.error "${e}"
    }
    return result
}

private updateLastCheckIn() {
    if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
        state.lastCheckInTime = new Date().time

        def evt = [name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false]

        sendEvent(evt)
    }
}

private convertToLocalTimeString(dt) {
    try {
        def timeZoneId = location?.timeZone?.ID
        if (timeZoneId) {
            return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
        }
        else {
            return "$dt"
        }
    }
    catch (ex) {
        return "$dt"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)

    def result = []
    if (encapsulatedCmd) {
        result += zwaveEvent(encapsulatedCmd)
    }
    else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logTrace "${cmd}"

    updateSyncingStatus()
    runIn(4, refreshSyncStatus)

    def param = configParams.find { it.num == cmd.parameterNumber }
    if (param) {
        def val = cmd.scaledConfigurationValue
        logDebug "${param.name}(#${param.num}) = ${val}"
        setParamStoredValue(param.num, val)
    }
    else {
        logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
    }
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    logTrace "VersionReport: ${cmd}"

    def subVersion = String.format("%02d", cmd.applicationSubVersion)
    def fullVersion = "${cmd.applicationVersion}.${subVersion}"

    sendEventIfNew("firmwareVersion", fullVersion)
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logTrace "${cmd}"
    sendSwitchEvents(cmd.value, "physical")
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    logTrace "${cmd}"
    sendSwitchEvents(cmd.value, "digital")
    return []
}

private sendSwitchEvents(rawVal, type) {

    def switchVal = (rawVal == 0xFF) ? "on" : "off"

    sendEventIfNew("switch", switchVal, true, type)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logDebug "Unhandled zwaveEvent: $cmd"
    return []
}

private updateSyncingStatus() {
    sendEventIfNew("syncStatus", "Syncing...", false)
}

def refreshSyncStatus() {
    def changes = pendingChanges
    sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
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

private setParamStoredValue(paramNum, value) {
    state["configVal${paramNum}"] = value
}

private getConfigParams() {
    return [
            ledModeParam,
            autoOffIntervalParam,
            autoOnIntervalParam,
            powerFailureRecoveryParam
    ]
}


private getLedModeParam() {
    return getParam(1, "LED Indicator Mode", 1, 0, ledModeOptions)
}

private getAutoOffIntervalParam() {
    return getParam(2, "Auto Turn-Off Timer", 4, 0, autoOnOffIntervalOptions)
}

private getAutoOnIntervalParam() {
    return getParam(4, "Auto Turn-On Timer", 4, 0, autoOnOffIntervalOptions)
}

private getPowerFailureRecoveryParam() {
    return getParam(6, "Power Failure Recovery", 1, 0, powerFailureRecoveryOptions)
}

private getParam(num, name, size, defaultVal, options) {
    def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

    def map = [num: num, name: name, size: size, value: val]
    if (options) {
        map.options = setDefaultOption(options, defaultVal)
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

private sendEventIfNew(name, value, displayed=true, type=null, unit="") {
    def desc = "${name} is ${value}${unit}"
    if (device.currentValue(name) != value) {
        logDebug(desc)

        def evt = [name: name, value: value, descriptionText: "${device.displayName} ${desc}", displayed: displayed]

        if (type) {
            evt.type = type
        }
        if (unit) {
            evt.unit = unit
        }
        sendEvent(evt)
    }
    else {
        logTrace(desc)
    }
}

private static safeToInt(val, defaultVal=0) {
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