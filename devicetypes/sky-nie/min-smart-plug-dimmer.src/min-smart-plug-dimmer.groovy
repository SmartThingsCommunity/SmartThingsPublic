/**
 *      Min Smart Plug Dimmer v1.1.3
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

metadata {
    definition (
            name: "Min Smart Plug Dimmer",
            namespace: "sky-nie",
            author: "winnie",
            ocfDeviceType: "oic.d.smartplug"
    ) {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Temperature Measurement"
        capability "Switch Level"
        capability "Configuration"
        capability "Refresh"
        capability "Health Check"

        attribute "firmwareVersion", "string"
        attribute "lastCheckIn", "string"
        attribute "syncStatus", "string"

        fingerprint mfr: "0312", prod: "FF00", model: "FF0D", deviceJoinName: "Minoston Dimmer Switch" //MP21ZD Minoston Mini Smart Plug Dimmer
        fingerprint mfr: "0312", prod: "FF07", model: "FF03", deviceJoinName: "Minoston Dimmer Switch" //MP22ZD Minoston Outdoor Smart Plug Dimmer
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light13", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light13", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
            }
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
        }

        multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState "temperature", label: '${currentValue}°',
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
        valueTile("icon", "device.icon", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: '', icon: "https://inovelli.com/wp-content/uploads/Device-Handler/Inovelli-Device-Handler-Logo.png"
        }
        main "switch", "temperature"
        details(["switch", "temperature", "refresh", "syncStatus", "sync", "firmwareVersion"])
    }

    preferences {
        configParams.each {
            if (it.name) {
                if (it.range) {
                    getNumberInput(it)
                }
                else {
                    getOptionsInput(it)
                }
            }
        }
    }
}

private getOptionsInput(param) {
    input "configParam${param.num}", "enum",
            title: "${param.name}:",
            required: false,
            defaultValue: "${param.value}",
            options: param.options
}

private getNumberInput(param) {
    input "configParam${param.num}", "number",
            title: "${param.name}:",
            required: false,
            defaultValue: "${param.value}",
            range: param.range
}

def installed() {
    logDebug "installed()..."
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

//	cmds << sensorBinaryGetCmd()
//	cmds << batteryGetCmd()

    configParams.each { param ->
        def storedVal = getParamStoredValue(param.num)
        def paramVal = param.value
        if (state.resyncAll || ("${storedVal}" != "${paramVal}")) {
            cmds << configSetCmd(param, paramVal)
            if (param.num == temperatureReportThresholdParam.num) {
                cmds << "delay 3000"
                cmds << sensorMultilevelGetCmd(tempSensorType)
            }else{
                cmds << configGetCmd(param)
            }
        }
    }

    state.resyncAll = false
    if (cmds) {
        sendCommands(delayBetween(cmds, 500))
    }
    return []
}

private sendCommands(cmds) {
    def actions = []
    cmds?.each {
        actions << new physicalgraph.device.HubAction(it)
    }
    sendHubCommand(actions, 100)
    return []
}


// Required for HealthCheck Capability, but doesn't actually do anything because this device sleeps.
def ping() {
}


// Forces the configuration to be resent to the device the next time it wakes up.
def refresh() {
    logForceWakeupMessage "The sensor data will be refreshed the next time the device wakes up."
    state.lastBattery = null
    if (!state.refreshSensors) {
        state.refreshSensors = true
    }
    else {
        state.refreshConfig = true
    }
    refreshSyncStatus()
    return []
}

private logForceWakeupMessage(msg) {
    logDebug "${msg}  You can force the device to wake up immediately by holding the z-button for 2 seconds."
}

def parse(String description) {
    def result = []
    try {
        def cmd = zwave.parse(description, commandClassVersions)
        if (cmd) {
            result += zwaveEvent(cmd)
        }
        else {
            logDebug "Unable to parse description: $description"
        }

        sendEvent(name: "lastCheckIn", value: convertToLocalTimeString(new Date()), displayed: false)
    }
    catch (e) {
        log.error "$e"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapCmd = cmd.encapsulatedCommand(commandClassVersions)

    def result = []
    if (encapCmd) {
        result += zwaveEvent(encapCmd)
    }
    else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    logTrace "SensorMultilevelReport: ${cmd}"

    if (cmd.sensorValue != [255, 255]) { // Bug in beta device
        switch (cmd.sensorType) {
            case tempSensorType:
                def unit = cmd.scale ? "F" : "C"
                def temp = convertTemperatureIfNeeded(cmd.scaledSensorValue, unit, cmd.precision)

                sendEvent(getEventMap("temperature", temp, true, null, getTemperatureScale()))
                break
            default:
                logDebug "Unknown Sensor Type: ${cmd.sensorType}"
        }
    }
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logTrace "ConfigurationReport ${cmd}"

    updateSyncingStatus()
    runIn(4, refreshSyncStatus)

    def param = configParams.find { it.num == cmd.parameterNumber }
    if (param) {
        def val = cmd.scaledConfigurationValue

        logDebug "${param.name}(#${param.num}) = ${val}"
        setParamStoredValue(param.num, val)
    }
    else {
        logDebug "Parameter #${cmd.parameterNumber} = ${cmd.configurationValue}"
    }
    return []
}
private updateSyncingStatus() {
    sendEventIfNew("syncStatus", "Syncing...", false)
}

def refreshSyncStatus() {
    def changes = pendingChanges
    sendEventIfNew("syncStatus", (changes ?  "${changes} Pending Changes" : "Synced"), false)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    logDebug "Ignored Command: $cmd"
    return []
}

private getEventMap(name, value, displayed=null, desc=null, unit=null) {
    def isStateChange = (device.currentValue(name) != value)
    displayed = (displayed == null ? isStateChange : displayed)
    def eventMap = [
            name: name,
            value: value,
            displayed: displayed,
            isStateChange: isStateChange,
            descriptionText: desc ?: "${device.displayName} ${name} is ${value}"
    ]

    if (unit) {
        eventMap.unit = unit
        eventMap.descriptionText = "${eventMap.descriptionText}${unit}"
    }
    if (displayed) {
        logDebug "${eventMap.descriptionText}"
    }
    return eventMap
}

private sensorMultilevelGetCmd(sensorType) {
    def scale = (sensorType == tempSensorType) ? 0 : 1
    return secureCmd(zwave.sensorMultilevelV5.sensorMultilevelGet(scale: scale, sensorType: sensorType))
}

private configGetCmd(param) {
    return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private configSetCmd(param, value) {
    return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
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

private static getCommandClassVersions() {
    [
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
            0x9F: 1	// Security S2
    ]
}

private getPendingChanges() {
    return configParams.count { "${it.value}" != "${getParamStoredValue(it.num)}" }
}

private getParamStoredValue(paramNum) {
    return safeToInt(state["configParam${paramNum}"] , null)
}

private setParamStoredValue(paramNum, value) {
    state["configParam${paramNum}"] = value
}

// Sensor Types
private static getTempSensorType() { return 1 }

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
            temperatureReportTimeParam,
            temperatureReportThresholdParam
    ]
}

private getLedModeParam() {
    return getParam(2, "LED Indicator Mode", 1, 0, ledModeOptions)
}

private getAutoOffIntervalParam() {
    return getParam(4, "Auto Turn-Off Timer(0,Disabled; 1--60480 minutes)", 4, 0, null, "0..60480")
}

private getAutoOnIntervalParam() {
    return getParam(6, "Auto Turn-On Timer(0,Disabled; 1--60480 minutes)", 4, 0, null, "0..60480")
}

private getNightLightParam() {
    return getParam(7, "Night Light Settings(1,10%;2,20%,...10,100%)", 1, 2, null, "1..10")
}

private getPowerFailureRecoveryParam() {
    return getParam(8, "Power Failure Recovery", 1, 2, powerFailureRecoveryOptions)
}

private getPushDimmingDurationParam() {
    return getParam(9, "Push Dimming Duration(0,Disabled; 1--10 Seconds)", 1, 2, null, "0..10")
}

private getHoldDimmingDurationParam() {
    return getParam(10, "Hold Dimming Duration(1--10 Seconds)", 1, 4, null, "1..10")
}

private getMinimumBrightnessParam() {
    return getParam(11, "Minimum Brightness(0,Disabled; 1--99:1%--99%)", 1, 10, null,"0..99")
}

private getMaximumBrightnessParam() {
    return getParam(12, "Maximum Brightness(0,Disabled; 1--99:1%--99%)", 1, 99, null,"0..99")
}

private getTemperatureReportTimeParam() {
    return getParam(13, "Temperature report time(1--60 minutes)", 1, 1, null, "1..60")
}

private getTemperatureReportThresholdParam() {
    return getParam(14, "Temperature report threshold", 1, 5, temperatureReportThresholdOptions)
}

private getParam(num, name, size, defaultVal, options=null, range=null) {
    def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

    def map = [num: num, name: name, size: size, value: val]
    if (options) {
        map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
        map.options = setDefaultOption(options, defaultVal)
    }
    if (range) map.range = range

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
            "0":"Off When On", "1":"On When On", "2":"Always Off", "3":"Always On"
    ]
}

private static getPowerFailureRecoveryOptions() {
    return [
            "0":"Turn Off", "1":"Turn On", "2":"Restore Last State"
    ]
}

private static getTemperatureReportThresholdOptions() {
    def options = [:]
    options["1"] = "1℃/1.8°F"
    def it2

    (2..10).each {
        it2 = it*1.8
        options["${it}"] = "${it}℃/${it2}°F"
    }
    return options
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

private static validateRange(val, defaultVal, lowVal, highVal) {
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

private static safeToInt(val, defaultVal=0) {
    return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}

private convertToLocalTimeString(dt) {
    def timeZoneId = location?.timeZone?.ID
    if (timeZoneId) {
        return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
    }
    else {
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

private sendSwitchEvents(rawVal, type) {
    def oldSwitch = device.currentValue("switch")
    def oldLevel = device.currentValue("level")

    def switchVal = rawVal ? "on" : "off"

    sendEventIfNew("switch", switchVal, true, type)

    if (rawVal) {
        sendEventIfNew("level", rawVal, true, type, "%")
    }
}