/**
 *      Minoston Min Dim Plug v1.0.2
 *
 *  	Models: MINOSTON (MP21ZD)
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *	Documentation:
 *
 *  Changelog:
 *
 *    1.0.2 (06/10/2020)
 *      - Changed dimming duration options to 1-10 seconds.
 *
 *    1.0.1 (06/05/2020)
 *      - Swapped first 2 options of LED Control preference
 *
 *    1.0 (04/20/2020)
 *      - Initial Release
 *
 *
 *  Copyright 2020 Kevin LaFramboise
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
import groovy.transform.Field
def cmd = zwave.parse(description, [0x25: 1, 0x30: 1,  0x80: 1, 0x84: 1, 0x9C: 1])
@Field static Map commandClassVersions = [
        0x20: 1,	// Basic
        0x26: 3,	// Switch Multilevel
        0x55: 1,	// Transport Service
        0x59: 1,	// AssociationGrpInfo
        0x5A: 1,	// DeviceResetLocally
        0x31: 5,    // SENSOR_MULTILEVEL_V11
        0x71: 3,	// NOTIFICATION_V8
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

@Field static Map buttons = [0:"upper", 1:"lower"]

@Field static Map paddleControlOptions = [0:"Normal", 1:"Reverse", 2:"Toggle"]
@Field static Integer reversePaddle = 1
@Field static Integer togglePaddle = 2

@Field static Map ledModeOptions = [0:"Off When On", 1:"On When On", 2:"Always Off", 3:"Always On"]

@Field static Map nightLightOptions = [1:"10%", 2:"20%", 3:"30%", 4:"40%", 5:"50%", 6:"60%", 7:"70%", 8:"80%", 9:"90%", 10:"100%"]

@Field static Map autoOnOffIntervalOptions = [0:"Disabled", 1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 45:"45 Minutes", 60:"1 Hour", 120:"2 Hours", 180:"3 Hours", 240:"4 Hours", 300:"5 Hours", 360:"6 Hours", 420:"7 Hours", 480:"8 Hours", 540:"9 Hours", 600:"10 Hours", 720:"12 Hours", 1080:"18 Hours", 1440:"1 Day", 2880:"2 Days", 4320:"3 Days", 5760:"4 Days", 7200:"5 Days", 8640:"6 Days", 10080:"1 Week", 20160:"2 Weeks", 30240:"3 Weeks", 40320:"4 Weeks", 50400:"5 Weeks", 60480:"6 Weeks"]

@Field static Map powerFailureRecoveryOptions = [0:"Turn Off", 1:"Turn On", 2:"Restore Last State"]

@Field static Map noYesOptions = [0:"No", 1:"Yes"]

@Field static Map dimmingDurationOptions0 = [0:"Disabled", 1:"1 Second", 2:"2 Seconds", 3:"3 Seconds", 4:"4 Seconds", 5:"5 Seconds", 6:"6 Seconds", 7:"7 Seconds", 8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds"]

@Field static Map dimmingDurationOptions1 = [1:"1 Second", 2:"2 Seconds", 3:"3 Seconds", 4:"4 Seconds", 5:"5 Seconds", 6:"6 Seconds", 7:"7 Seconds", 8:"8 Seconds", 9:"9 Seconds", 10:"10 Seconds"]

@Field static Map brightnessOptions = [0:"Disabled", 1:"1%", 5:"5%", 10:"10%", 15:"15%", 20:"20%", 25:"25%", 30:"30%", 35:"35%", 40:"40%", 45:"45%", 50:"50%", 55:"55%",60:"60%", 65:"65%", 70:"70%", 75:"75%", 80:"80%", 85:"85%", 90:"90%", 95:"95%", 99:"99%"]

@Field static Map temperatureReportTimeOptions = [1:"1 Minute", 2:"2 Minutes", 3:"3 Minutes", 4:"4 Minutes", 5:"5 Minutes", 6:"6 Minutes", 7:"7 Minutes", 8:"8 Minutes", 9:"9 Minutes", 10:"10 Minutes", 15:"15 Minutes", 20:"20 Minutes", 25:"25 Minutes", 30:"30 Minutes", 35:"30 Minutes", 40:"30 Minutes", 45:"45 Minutes", 50:"30 Minutes", 55:"45 Minutes",60:"60 Minutes"]

@Field static Map temperatureReportThresholdOptions = [1:"1℃/1.8°F", 2:"2℃/3.6°F", 3:"3℃/5.4°F", 4:"4℃/7.2°F", 5:"5℃/9.0°F", 6:"6℃/10.8°F", 7:"7℃/12.6°F", 8:"8℃/14.4°F", 9:"8℃/16.2°F", 10:"10℃/18°F"]

metadata {
    definition (
            name: "Minoston Min Dim Plug",
            namespace: "krlaframboise",
            author: "Kevin LaFramboise",
            vid:"generic-dimmer",
            ocfDeviceType: "oic.d.switch"
    ) {
            capability "Actuator"
            capability "Sensor"
            capability "Switch"
            capability "Switch Level"
            capability "Light"
            capability "Configuration"
            capability "Refresh"
            capability "Health Check"
            capability "Temperature Measurement"

            attribute "firmwareVersion", "string"
            attribute "lastCheckIn", "string"
            attribute "syncStatus", "string"
            fingerprint mfr: "0312", prod: "FF00", model: "FF0D", deviceJoinName: "Minoston Dimmer Switch", ocfDeviceType: "oic.d.smartplug" //MP21ZD Minoston Mini Smart Plug Dimmer
	}

    simulator { }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute("device.temperature", key: "SECONDARY_CONTROL") {
                attributeState("temperature", label: '${currentValue}°', icon: "st.alarm.temperature.normal",
                        backgroundColors: [
                                // Celsius
                                [value: 0, color: "#153591"],
                                [value: 7, color: "#1e9cbb"],
                                [value: 15, color: "#90d2a7"],
                                [value: 23, color: "#44b621"],
                                [value: 28, color: "#f1d801"],
                                [value: 35, color: "#d04e00"],
                                [value: 37, color: "#bc2323"],
                                // Fahrenheit
                                [value: 40, color: "#153591"],
                                [value: 44, color: "#1e9cbb"],
                                [value: 59, color: "#90d2a7"],
                                [value: 74, color: "#44b621"],
                                [value: 84, color: "#f1d801"],
                                [value: 95, color: "#d04e00"],
                                [value: 96, color: "#bc2323"]
                        ]
                )
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
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

        //createEnumInput("createButton", "Create Button for Paddles?", 1, setDefaultOption(noYesOptions, 1))

        //createEnumInput("debugOutput", "Enable Debug Logging?", 1, setDefaultOption(noYesOptions, 1))
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

    if (state.debugLoggingEnabled == null) {
        state.debugLoggingEnabled = true
        state.createButtonEnabled = true
    }
    def cmds = [ zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01).format()]
    response(cmds)
}

def updated() {
    if (!isDuplicateCommand(state.lastUpdated, 5000)) {
        state.lastUpdated = new Date().time

        logDebug "updated()..."

        state.debugLoggingEnabled = (safeToInt(settings?.debugOutput) != 0)
        state.createButtonEnabled = (safeToInt(settings?.createButton) != 0)

        initialize()

        runIn(5, executeConfigureCmds, [overwrite: true])
    }
    return []
}

private initialize() {
    def checkInterval = ((60 * 60 * 3) + (5 * 60))

    def checkIntervalEvt = [name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"]]

    if (!device.currentValue("checkInterval")) {
        sendEvent(checkIntervalEvt)
    }

    if (state.createButtonEnabled && !childDevices) {
        try {
            def child = addChildButton()
            child?.sendEvent(checkIntervalEvt)
        }
        catch (ex) {
            log.warn "Unable to create button device because the 'Component Button' DTH is not installed"
        }
    }
    else if (!state.createButtonEnabled && childDevices) {
        removeChildButton(childDevices[0])
    }
}

private addChildButton() {
    log.warn "Creating Button Device"

    def child = addChildDevice(
            "krlaframboise",
            "Component Button",
            "${device.deviceNetworkId}-BUTTON",
            device.getHub().getId(),
            [
                    completedSetup: true,
                    isComponent: false,
                    label: "${device.displayName}-Button",
                    componentLabel: "${device.displayName}-Button"
            ]
    )

    child?.sendEvent(name:"supportedButtonValues", value:JsonOutput.toJson(["pushed", "down","down_2x","up","up_2x"]), displayed:false)

    child?.sendEvent(name:"numberOfButtons", value:1, displayed:false)

    sendButtonEvent("pushed")

    return child
}

private removeChildButton(child) {
    try {
        log.warn "Removing ${child.displayName}} "
        deleteChildDevice(child.deviceNetworkId)
    }
    catch (e) {
        log.error "Unable to remove ${child.displayName}!  Make sure that the device is not being used by any SmartApps."
    }
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
        cmds << switchMultilevelGetCmd()
    }

    if (state.resyncAll || !device.currentValue("firmwareVersion")) {
        cmds << versionGetCmd()
    }

    configParams.each { param ->
        def storedVal = getParamStoredValue(param.num)
        def paramVal = param.value

        if ((param == paddleControlParam) && state.createButtonEnabled && (param.value == togglePaddle)) {
            log.warn "Only 'pushed', 'up_2x', and 'down_2x' button events are supported when Paddle Control is set to Toggle."
        }

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

    return [ switchMultilevelGetCmd() ]
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

def refresh() {
    logDebug "refresh()..."

    refreshSyncStatus()

    sendCommands([switchMultilevelGetCmd()])
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

private basicSetCmd(val) {
    return secureCmd(zwave.basicV1.basicSet(value: val))
}

private switchMultilevelSetCmd(level, duration) {
    def levelVal = validateRange(level, 99, 0, 99)

    def durationVal = validateRange(duration, 1, 0, 100)

    return secureCmd(zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: durationVal, value: levelVal))
}

private switchMultilevelGetCmd() {
    return secureCmd(zwave.switchMultilevelV3.switchMultilevelGet())
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

        if (childDevices) {
            childDevices*.sendEvent(evt)
        }
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

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    logTrace "${cmd}"
    sendSwitchEvents(cmd.value, "digital")
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    log.debug "SensorMultilevelReport: $cmd"
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            def realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.value = getAdjustedTemp(realTemperature)
            map.unit = getTemperatureScale()
            log.debug "Temperature Report: $map.value"
            break
        default:
            map.descriptionText = cmd.toString()
            break
    }

    [createEvent(map)]
}

private sendSwitchEvents(rawVal, type) {
    def oldSwitch = device.currentValue("switch")
    def oldLevel = device.currentValue("level")

    def switchVal = rawVal ? "on" : "off"

    sendEventIfNew("switch", switchVal, true, type)

    if (rawVal) {
        sendEventIfNew("level", rawVal, true, type, "%")
    }

    def paddlesReversed = (paddleControlParam.value == reversePaddle)

    if (state.createButtonEnabled && (type == "physical") && childDevices) {
        if (paddleControlParam.value == togglePaddle) {
            sendButtonEvent("pushed")
        }
        else {
            def btnVal = ((rawVal && !paddlesReversed) || (!rawVal && paddlesReversed)) ? "up" : "down"

            if ((oldSwitch == "on") && (btnVal == "up") && (oldLevel > rawVal)) {
                btnVal = "down"
            }

            sendButtonEvent(btnVal)
        }
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
    def child = childDevices?.first()
    if (child) {
        logDebug "${child.displayName} Button ${value}"
        childDevices[0].sendEvent(name: "button", value: value, data:[buttonNumber: 1], isStateChange: true)
    }
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
            // paddleControlParam,
            ledModeParam,
            autoOffIntervalParam,
            autoOnIntervalParam,
            nightLightParam,
            powerFailureRecoveryParam,
            pushDimmingDurationParam,
            holdDimmingDurationParam,
            minimumBrightnessParam,
            maximumBrightnessParam,
            temperatureReportTimeParam,
            temperatureReportThresholdParam
    ]
}

//private getPaddleControlParam() {
//	return getParam(1, "Paddle Control", 1, 0, paddleControlOptions)
//}

private getLedModeParam() {
	return getParam(2, "LED Indicator Mode", 1, 0, ledModeOptions)
}

private getAutoOffIntervalParam() {
	return getParam(4, "Auto Turn-Off Timer", 4, 0, autoOnOffIntervalOptions)
}

private getAutoOnIntervalParam() {
	return getParam(6, "Auto Turn-On Timer", 4, 0, autoOnOffIntervalOptions)
}

private getNightLightParam() {
	return getParam(7, "Night Light Settings", 1, 2, nightLightOptions)
}

private getPowerFailureRecoveryParam() {
	return getParam(8, "Power Failure Recovery", 1, 2, powerFailureRecoveryOptions)
}

private getPushDimmingDurationParam() {
    return getParam(9, "Push Dimming Duration", 1, 2, dimmingDurationOptions0)
}

private getHoldDimmingDurationParam() {
    return getParam(10, "Hold Dimming Duration", 1, 4, dimmingDurationOptions1)
}

private getMinimumBrightnessParam() {
    return getParam(11, "Minimum Brightness", 1, 10, brightnessOptions)
}

private getMaximumBrightnessParam() {
    return getParam(12, "Maximum Brightness", 1, 99, brightnessOptions)
}

private getTemperatureReportTimeParam() {
    return getParam(13, "Temperature report time", 1, 1, temperatureReportTimeOptions)
}


private getTemperatureReportThresholdParam() {
    return getParam(14, "Temperature report threshold", 1, 5, temperatureReportThresholdOptions)
}

private getParam(num, name, size, defaultVal, options) {
    def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

    def map = [num: num, name: name, size: size, value: val]
    if (options) {
        map.options = setDefaultOption(options, defaultVal)
    }

    return map
}

private setDefaultOption(options, defaultVal) {
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

private validateRange(val, defaultVal, lowVal, highVal) {
    val = safeToInt(val, defaultVal)
    if (val > highVal) {
        return highVal
    }
    else if (val < lowVal) {
        return lowVal
    }
    else {
        return val
    }
}

private safeToInt(val, defaultVal=0) {
    return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private isDuplicateCommand(lastExecuted, allowedMil) {
    !lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}

private logDebug(msg) {
    if (state.debugLoggingEnabled) {
        log.debug "$msg"
    }
}

private logTrace(msg) {
    log.trace "$msg"
}
