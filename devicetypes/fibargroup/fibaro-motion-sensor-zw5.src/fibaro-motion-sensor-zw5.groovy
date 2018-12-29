/**
 *  FIBARO Motion Sensor ZW5
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition (name: "Fibaro Motion Sensor ZW5", namespace: "FibarGroup", author: "Fibar Group") {
        capability "Battery"
        capability "Configuration"
        capability "Illuminance Measurement"
        capability "Motion Sensor"
        capability "Sensor"
        capability "Tamper Alert"
        capability "Temperature Measurement"
        capability "Health Check"
        capability "Three Axis"

        command "resetMotionTile"
        command "forceSync"

        fingerprint mfr: "010F", prod: "0801"
        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x30, 0x9C, 0x98, 0x7A"
        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x30, 0x9C, 0x7A"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"FGMS", type:"lighting", width:6, height:4) {
            tileAttribute("device.motion", key:"PRIMARY_CONTROL") {
                attributeState("inactive", label:"no motion", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/motionSensor/motion0.png", backgroundColor:"#ffffff")
                attributeState("active", label:"motion", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/motionSensor/motion1.png", backgroundColor:"#00a0dc")
            }
            tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
                attributeState("val", label:'${currentValue}')
            }
        }
        standardTile("tamper", "device.tamper", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "clear", label:'', icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/motionSensor/tamper_detector0.png"
            state "detected", label:'', icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/motionSensor/tamper_detector100.png"
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
        valueTile("illuminance", "device.illuminance", inactiveLabel: false, width: 2, height: 2) {
            state "luminosity", label:'${currentValue}\nLux', unit:"lux"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "battery", label:'${currentValue}%\nbattery', unit:"%"
        }
        valueTile("motionTile", "device.motionText", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "motionText", label:'${currentValue}', action:"resetMotionTile"
        }
        main "FGMS"
        details(["FGMS","tamper","temperature","illuminance","motionTile","battery"])
    }

    preferences {

        input (
                title: "Fibaro Motion Sensor ZW5 manual",
                description: "Tap to view the manual.",
                image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/ms_icon.png",
                url: "http://manuals.fibaro.com/content/manuals/en/FGMS-001/FGMS-001-EN-T-v2.1.pdf",
                type: "href",
                element: "href"
        )

        parameterMap().findAll{(it.num as Integer) != 54}.each {
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

// UI - supporting functions
def resetMotionTile() {
    logging("${device.displayName} - Executing resetMotionTile()", "info")
    sendEvent(name: "motionText", value: "--", displayed: false)
}

// Event handlers and supporting functions
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    logging("${device.displayName} - NotificationReport received for ${cmd.event}, parameter value: ${cmd.eventParameter[0]}", "info")
    def lastTime = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    if (cmd.notificationType == 7) {
        if (cmd.event == 0) {
            sendEvent(name: (cmd.eventParameter[0] == 3) ? "tamper" : "motion", value: (cmd.eventParameter[0] == 3) ? "clear" :"inactive")
        } else {
            sendEvent(name: (cmd.event == 3) ? "tamper" : "motion", value: (cmd.event == 3) ? "detected" : "active")
            multiStatusEvent( (cmd.event == 3) ? "Tamper - $lastTime" : "Motion - $lastTime" )
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel}", "info")
    sendEvent(name: "battery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true, isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    logging("${device.displayName} - SensorMultilevelReport received, sensor: ${cmd.sensorType}, scaledValue: ${cmd.scaledSensorValue}", "info")
    switch (cmd.sensorType as Integer) {
        case 1:
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            sendEvent(name: "temperature", unit: getTemperatureScale(), value: convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), displayed: true)
            break
        case 3:
            sendEvent(name: "illuminance", value: cmd.scaledSensorValue.toInteger().toString(), unit:"lux", displayed: true)
            break
        case [25,52,53,54]:
            motionEvent(cmd.sensorType, cmd.scaledSensorValue )
            break
    }
}

private motionEvent(Integer sensorType, value) {
    logging("${device.displayName} - Executing motionEvent() with parameters: ${sensorType}, ${value}", "info")
    def axisMap = [52: "yAxis", 53: "zAxis", 54: "xAxis"]
    switch (sensorType) {
        case 25:
            sendEvent(name: "motionText", value: "Vibration:\n${value} MMI", displayed: false)
            break
        case 52..54:
            sendEvent(name: axisMap[sensorType], value: value , displayed: false)
            runIn(2,"axisEvent")
            break
    }
}

private axisEvent() {
    logging("${device.displayName} - Executing axisEvent() values are: ${device.currentValue("xAxis")}, ${device.currentValue("yAxis")}, ${device.currentValue("zAxis")}", "info")
    def xAxis = Math.round((device.currentValue("xAxis") as Float) * 100)
    def yAxis = Math.round((device.currentValue("yAxis") as Float) * 100) // * 100 becaouse from what I can tell apps expect data in cm/s2
    def zAxis = Math.round((device.currentValue("zAxis") as Float) * 100)
    sendEvent(name: "motionText", value: "X: ${device.currentValue("xAxis")}\nY: ${device.currentValue("yAxis")}\nZ: ${device.currentValue("zAxis")}", displayed: false)
    sendEvent(name: "threeAxis", value: "${xAxis},${yAxis},${zAxis}", isStateChange: true, displayed: false)
}

// Parameter configuration, synchronization and verification
def updated() {
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    logging("${device.displayName} - Executing updated()","info")

    if ( settings.tamperOperatingMode as Integer == 0 ) {
        resetMotionTile()
    }

    syncStart()
    state.lastUpdated = now()
}

def configure() {
    def cmds = []
    cmds << zwave.batteryV1.batteryGet()
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
    encapSequence(cmds,1000)
}

private syncStart() {
    boolean syncNeeded = false
    Integer settingValue = null
    parameterMap().each {
        if(settings."$it.key" != null || it.num == 54) {
            if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
            if ( (it.num as Integer) == 54 ) {
                settingValue = (((settings."temperatureHigh" as Integer) == 0) ? 0 : 1) + (((settings."temperatureLow" as Integer) == 0) ? 0 : 2)
            } else if ( (it.num as Integer) in [55,56] ) {
                settingValue = (((settings."$it.key" as Integer) == 0) ? state."$it.key".value : settings."$it.key") as Integer
            } else {
                settingValue = settings."$it.key" as Integer
            }
            if (state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
                state."$it.key".value = settingValue
                state."$it.key".state = "notSynced"
                syncNeeded = true
            }
        }
    }

    if ( syncNeeded ) {
        logging("${device.displayName} - sync needed.", "info")
        multiStatusEvent("Sync pending. Please wake up the device by pressing the B button.", true)
    }
}

private syncNext() {
    logging("${device.displayName} - Executing syncNext()","info")
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

private syncCheck() {
    logging("${device.displayName} - Executing syncCheck()","info")
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
        multiStatusEvent("Sync incomplete! Wake up the device again by pressing the tamper button.", true, true)
    } else {
        sendHubCommand(response(encap(zwave.wakeUpV1.wakeUpNoMoreInformation())))
        if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
    }
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
    if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
        sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    logging("${device.displayName} woke up", "info")
    def cmds = []
    sendEvent(descriptionText: "$device.displayName woke up", isStateChange: true)
    if ( state.wakeUpInterval?.state == "notSynced" && state.wakeUpInterval?.value != null ) {
        cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: state.wakeUpInterval.value as Integer, nodeid: zwaveHubNodeId)
        state.wakeUpInterval.state = "synced"
    }
    cmds << zwave.batteryV1.batteryGet()
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
    runIn(1,"syncNext")
    [response(encapSequence(cmds,1000))]
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
    logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
    state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
    syncNext()
}


def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    logging("${device.displayName} - rejected request!","warn")
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.state == "inProgress" ) {
            state."$param.key"?.state = "failed"
            break
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    log.warn "${device.displayName} - received command: $cmd - device has reset itself"
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

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract secure cmd from $cmd"
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
        log.warn "Could not extract crc16 command from $cmd"
    }
}

private logging(text, type = "debug") {
    if (settings.logging == "true") {
        log."$type" text
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format() // doesn't work righ now because SmartThings...
    //"5601${cmd.format()}0000"
}

private encap(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo.zw.contains("s")) {
        secEncap(cmd)
    } else if (zwaveInfo.cc.contains("56")){
        crcEncap(cmd)
    } else {
        logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
        cmd.format()
    }
}

private encapSequence(cmds, delay=250) {
    delayBetween(cmds.collect{ encap(it) }, delay)
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
    [0x5E: 1, 0x86: 1, 0x72: 2, 0x59: 1, 0x80: 1, 0x73: 1, 0x56: 1, 0x22: 1, 0x31: 5, 0x98: 1, 0x7A: 3, 0x20: 1, 0x5A: 1, 0x85: 2, 0x84: 2, 0x71: 3, 0x8E: 1, 0x70: 2, 0x30: 1, 0x9C: 1] //Fibaro Motion Sensor ZW5
}

private parameterMap() {[
        [key: "motionSensitivity", num: 1, size: 2, type: "enum", options: [
                15: "High sensitivity",
                100: "Medium sensitivity",
                200: "Low sensitivity"
        ], def: 15, min: 8, max: 255, title: "Motion detection - sensitivity", descr: ""],
        [key: "motionBlindTime", num: 2, size: 1, type: "enum", options: [
                1: "1 s",
                3: "2 s",
                5: "3 s",
                7: "4 s",
                9: "5 s",
                11: "6 s",
                13: "7 s",
                15: "8 s"
        ], def: 15, title: "Motion detection - blind time",
         descr: "PIR sensor is “blind” (insensitive) to motion after last detection for the amount of time specified in this parameter. (1-8 in sec.)"],
        [key: "motionCancelationDelay", num: 6, size: 2, type: "number", def: 30, min: 1, max: 32767, title: "Motion detection - alarm cancellation delay",
         descr: "Time period after which the motion alarm will be cancelled in the main controller. (1-32767 sec.)"],
        [key: "motionOperatingMode", num: 8, size: 1, type: "enum", options: [0: "Always Active (default)", 1: "Active During Day", 2: "Active During Night"], def: "0", title: "Motion detection - operating mode",
         descr: "This parameter determines in which part of day the PIR sensor will be active."],
        [key: "motionNightDay", num: 9, size: 2, type: "number", def: 200, min: 1, max: 32767, title: "Motion detection - night/day",
         descr: "This parameter defines the difference between night and day in terms of light intensity, used in parameter 8. (1-32767 lux)"],
        [key: "tamperCancelationDelay", num: 22, size: 2, type: "number", def: 30, min: 1, max: 32767, title: "Tamper - alarm cancellation delay",
         descr: "Time period after which a tamper alarm will be cancelled in the main controller. (1-32767 in sec.)"],
        [key: "tamperOperatingMode", num: 24, size: 1, type: "enum", options: [0: "tamper only (default)", 1: "tamper and earthquake detector", 2: "tamper and orientation"], def: "0", title: "Tamper - operating modes",
         descr: "This parameter determines function of the tamper and sent reports. It is an advanced feature serving much more functions than just detection of tampering."],
        [key: "illuminanceThreshold", num: 40, size: 2, type: "number", def: 200, min: 0, max: 32767, title: "Illuminance report - threshold",
         descr: "This parameter determines the change in light intensity level (in lux) resulting in illuminance report being sent to the main controller. (1-32767 in lux)"],
        [key: "illuminanceInterval", num: 42, size: 2, type: "number", def: 3600, min: 0, max: 32767, title: "Illuminance report - interval",
         descr: "Time interval between consecutive illuminance reports. The reports are sent even if there is no change in the light intensity. (1-3276 in sec)"],
        [key: "temperatureThreshold", num: 60, size: 2, type: "enum", options: [
                3: "0.5°F/0.3°C",
                6: "1°F/0.6°C",
                10: "2°F/1 °C",
                17: "3°F/1.7°C",
                22: "4°F/2.2°C",
                28: "5°F/2.8°C"
        ], def: 10, min: 0, max: 255, title: "Temperature report - threshold", descr: "This parameter determines the change in measured temperature that will result in new temperature report being sent to the main controller."],
        [key: "ledMode", num: 80, size: 1, type: "enum", options: [
                0: "LED inactive",
                1: "Temp Dependent (1 long blink)",
                2: "Flashlight Mode (1 long blink)",
                3: "White (1 long blink)",
                4: "Red (1 long blink)",
                5: "Green (1 long blink)",
                6: "Blue (1 long blink)",
                7: "Yellow (1 long blink)",
                8: "Cyan (1 long blink)",
                9: "Magenta (1 long blink)",
                10: "Temp dependent (1 long 1 short blink) (default)",
                11: "Flashlight Mode (1 long 1 short blink)",
                12: "White (1 long 1 short blink)",
                13: "Red (1 long 1 short blink)",
                14: "Green (1 long 1 short blink)",
                15: "Blue (1 long 1 short blink)",
                16: "Yellow (1 long 1 short blink)",
                17: "Cyan (1 long 1 short blink)",
                18: "Magenta (1 long 1 short blink)",
                19: "Temp Dependent (1 long 2 short blink)",
                20: "White (1 long 2 short blinks)",
                21: "Red (1 long 2 short blinks)",
                22: "Green (1 long 2 short blinks)",
                23: "Blue (1 long 2 short blinks)",
                24: "Yellow (1 long 2 short blinks)",
                25: "Cyan (1 long 2 short blinks)",
                26: "Magenta (1 long 2 short blinks)"
        ], def: "10", title: "Visual LED indicator - signalling mode", descr: "This parameter determines the way in which visual indicator behaves after motion has been detected."],
        [key: "ledBrightness", num: 81, size: 1, type: "number", def: 50, min: 0, max: 100, title: "Visual LED indicator - brightness",
         descr: "This parameter determines the brightness of the visual LED indicator when indicating motion. (1-100%)"],
        [key: "ledLowBrightness", num: 82, size: 2, type: "number", def: 100, min: 0, max: 32767, title: "Visual LED indicator - illuminance for low indicator brightness",
         descr: "Light intensity level below which brightness of visual indicator is set to 1% (1-32767 lux)"],
        [key: "ledHighBrightness", num: 83, size: 2, type: "number", def: 1000, min: 0, max: 32767, title: "Visual LED indicator - illuminance for high indicator brightness",
         descr: "Light intensity level above which brightness of visual indicator is set to 100%. (value of parameter 82 to 32767 in lux)"]
]}