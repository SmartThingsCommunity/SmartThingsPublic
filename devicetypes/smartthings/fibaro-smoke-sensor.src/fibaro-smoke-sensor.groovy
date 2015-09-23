/**
 *  Copyright 2015 SmartThings
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
    definition (name: "Fibaro Smoke Sensor", namespace: "smartthings", author: "SmartThings") {
        capability "Battery" //attributes: battery
        capability "Carbon Monoxide Detector" //attributes: carbonMonoxide ("detected","clear","tested")
        capability "Configuration"  //commands: configure()
        capability "Sensor"
        capability "Smoke Detector" //attributes: smoke ("detected","clear","tested")
        capability "Temperature Measurement" //attributes: temperature

        attribute "tamper", "enum", ["detected", "clear"]
        attribute "heatAlarm", "enum", ["overheat detected", "clear", "rapid temperature rise", "underheat detected"]

        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x31, 0x8E, 0x22, 0x9C, 0x98, 0x7A", outClusters: "0x20, 0x8B"
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

        //carbonMonoxide
        status "carbonMonoxide detected": "command: 7105, payload: 02 01"
        status "carbonMonoxide clear": "command: 7105, payload: 02 00"
        status "carbonMonoxide tested": "command: 7105, payload: 02 03"

        //smoke
        status "smoke detected": "command: 7105, payload: 01 01"
        status "smoke clear": "command: 7105, payload: 01 00"
        status "smoke tested": "command: 7105, payload: 01 03"

        //temperature
        for (int i = 0; i <= 100; i += 20) {
            status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
                    new physicalgraph.zwave.Zwave().sensorMultilevelV5.sensorMultilevelReport(
                            scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1)
            ).incomingMessage()
        }

    }

    preferences {
        input description: "After successful installation, please click B-button at the Fibaro Smoke Sensor to update device status and configuration",
                title: "Instruction", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input description: "Please consult Fibaro Smoke Sensor operating manual for advanced setting options. You can skip this configuration to use default settings",
                title: "Advanced Configuration", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input "smokeSensorSensitivity", "enum", title: "Smoke Sensor Sensitivity", options: ["High","Medium","Low"], defaultValue: "${smokeSensorSensitivity}", displayDuringSetup: true
        input "zwaveNotificationStatus", "enum", title: "Notifications Status", options: ["disabled","casing opening","exceeding temperature threshold", "lack of Z-Wave range", "all notifications"],
                defaultValue: "${zwaveNotificationStatus}", displayDuringSetup: true
        input "visualIndicatorNotificationStatus", "enum", title: "Visual Indicator Notifications Status",
                options: ["disabled","casing opening","exceeding temperature threshold", "lack of Z-Wave range", "all notifications"],
                defaultValue: "${visualIndicatorNotificationStatus}", displayDuringSetup: true
        input "soundNotificationStatus", "enum", title: "Sound Notifications Status",
                options: ["disabled","casing opening","exceeding temperature threshold", "lack of Z-Wave range", "all notifications"],
                defaultValue: "${soundNotificationStatus}", displayDuringSetup: true
        input "temperatureReportInterval", "enum", title: "Temperature Report Interval",
                options: ["reports inactive","10 seconds","30 seconds", "1 minute", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"],
                defaultValue: "${temperatureReportInterval}", displayDuringSetup: true
        input "temperatureReportHysteresis", "number", title: "Temperature Report Hysteresis", description: "Available settings: 1-100", range: "1..100", displayDuringSetup: true
        input "temperatureThreshold", "number", title: "Temperature Threshold", description: "Available settings: 1-100", range: "1..100", displayDuringSetup: true
        input "excessTemperatureSignalingInterval", "enum", title: "Excess Temperature Signaling Interval",
                options: ["10 seconds","30 seconds", "1 minute", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"],
                defaultValue: "${excessTemperatureSignalingInterval}", displayDuringSetup: true
        input "lackOfZwaveRangeIndicationInterval", "enum", title: "Lack of Z-Wave Range Indication Interval",
                options: ["10 seconds","30 seconds", "1 minute", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"],
                defaultValue: "${excessTemperatureSignalingInterval}", displayDuringSetup: true
    }

    tiles (scale: 2){
        multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
                attributeState("clear", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
                attributeState("detected", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
                attributeState("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
                attributeState("replacement required", label:"REPLACE", icon:"st.alarm.smoke.test", backgroundColor:"#FFFF66")
                attributeState("unknown", label:"UNKNOWN", icon:"st.alarm.smoke.test", backgroundColor:"#ffffff")
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label:'Battery: ${currentValue}%', unit:"%"
            }
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:"%"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "temperature", label:'${currentValue}°', unit:"C"
        }
        valueTile("heatAlarm", "device.heatAlarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "clear", label:'TEMPERATURE OK', backgroundColor:"#ffffff"
            state "overheat detected", label:'OVERHEAT DETECTED', backgroundColor:"#ffffff"
            state "rapid temperature rise", label:'RAPID TEMP RISE', backgroundColor:"#ffffff"
            state "underheat detected", label:'UNDERHEAT DETECTED', backgroundColor:"#ffffff"
        }
        valueTile("tamper", "device.tamper", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "clear", label:'NO TAMPER', backgroundColor:"#ffffff"
            state "detected", label:'TAMPER DETECTED', backgroundColor:"#ffffff"
        }
        standardTile("refresh", "device.temperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "smoke"
        details(["smoke","temperature", "heatAlarm", "tamper"])
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    setConfigured("false") //wait until the next time device wakeup to send configure command
}

def configure() {

    // This sensor joins as a secure device if you tripple-click the button to include it
    //    if (device.device.rawDescription =~ /98/ && !isSecured()) {
    log.debug "configure() >> isSecured() : ${isSecured()}"
    if (!isSecured()) {
        log.debug "Fibaro smoke sensor not sending configure until secure"
        return []
    } else {
        log.info "${device.displayName} is configuring its settings"

        def request = []

        //1. configure wakeup interval, available: 0, 4200s-65535s, device default 21600s(6hr)
        request += zwave.wakeUpV1.wakeUpIntervalSet(seconds:4*3600, nodeid:zwaveHubNodeId)

        //2. Smoke Sensitivity 3 levels: 1-HIGH , 2-MEDIUM (default), 3-LOW
        if (smokeSensorSensitivity != "null") {
            request += zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1,
                    scaledConfigurationValue: smokeSensorSensitivity == "High" ? 1 : smokeSensorSensitivity == "Medium" ? 2 : smokeSensorSensitivity == "Low" ? 3 : 2)
        }

        //3. Z-Wave notification status: 0-all disabled (default), 1-casing open enabled, 2-exceeding temp enable
        if (zwaveNotificationStatus != "null"){
            request += zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1,
                    scaledConfigurationValue: zwaveNotificationStatus == "disabled" ? 0 : zwaveNotificationStatus == "casing opening" ? 1 :
                            zwaveNotificationStatus == "exceeding temperature threshold" ? 2 : zwaveNotificationStatus == "all notifications" ? 3 : 0)
        }

        //4. Visual indicator notification status: 0-all disabled (default), 1-casing open enabled, 2-exceeding temp enable, 4-lack of range notification
        if (visualIndicatorNotificationStatus != "null") {
            request += zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1,
                    scaledConfigurationValue: visualIndicatorNotificationStatus == "disabled" ? 0 : visualIndicatorNotificationStatus == "casing opening" ? 1 :
                    visualIndicatorNotificationStatus == "exceeding temperature threshold" ? 2 : visualIndicatorNotificationStatus == "lack of Z-Wave range" ? 4:
                            visualIndicatorNotificationStatus == "all notifications" ? 7 : 0)
        }

        //5. Sound notification status: 0-all disabled (default), 1-casing open enabled, 2-exceeding temp enable, 4-lack of range notification
        if (soundNotificationStatus != "null") {
            request += zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1,
                    scaledConfigurationValue: soundNotificationStatus == "disabled" ? 0 : soundNotificationStatus == "casing opening" ? 1 :
                            soundNotificationStatus == "exceeding temperature threshold" ? 2 : soundNotificationStatus == "lack of Z-Wave range" ? 4:
                                    soundNotificationStatus == "all notifications" ? 7 : 0)
        }


        //6. Temperature report interval: 0-report inactive, 1-8640 (multiply by 10 secs) [10s-24hr], default 1 (10 secs)
        if (temperatureReportInterval != "null") {
            request += zwave.configurationV1.configurationSet(parameterNumber: 20, size: 2,
                    scaledConfigurationValue: temperatureReportInterval == "reports inactive" ? 0 : temperatureReportInterval == "10 seconds" ? 1 :
                            temperatureReportInterval == "30 seconds" ? 3 : temperatureReportInterval == "1 minute" ? 6:
                            temperatureReportInterval == "30 minutes" ? 180 : temperatureReportInterval == "1 hour" ? 360 :
                            temperatureReportInterval == "6 hours" ? 2160 : temperatureReportInterval == "12 hours" ? 4320:
                            temperatureReportInterval == "18 hours" ? 6480 : temperatureReportInterval == "24 hours" ? 8640: 1)
        }

        //7. Temperature report hysteresis: 1-100 (in 0.1C step) [0.1C - 10C], default 10 (1 C)
        if (temperatureReportHysteresis != null) {
            request += zwave.configurationV1.configurationSet(parameterNumber: 21, size: 1, scaledConfigurationValue: temperatureReportHysteresis < 1 ? 1 : temperatureReportHysteresis > 100 ? 100 : temperatureReportHysteresis)
        }

        //8. Temperature threshold: 1-100 (C), default 55 (C)
        if (temperatureThreshold != null) {
            request += zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: temperatureThreshold < 1 ? 1 : temperatureThreshold > 100 ? 100 : temperatureThreshold)
        }

        //9. Excess temperature signaling interval: 1-8640 (multiply by 10 secs) [10s-24hr], default 1 (10 secs)
        if (excessTemperatureSignalingInterval != "null") {
            request += zwave.configurationV1.configurationSet(parameterNumber: 31, size: 2,
                    scaledConfigurationValue: excessTemperatureSignalingInterval == "10 seconds" ? 1 : excessTemperatureSignalingInterval == "30 seconds" ? 3 : excessTemperatureSignalingInterval == "1 minute" ? 6:
                    excessTemperatureSignalingInterval == "30 minutes" ? 180 : excessTemperatureSignalingInterval == "1 hour" ? 360 :
                    excessTemperatureSignalingInterval == "6 hours" ? 2160 : excessTemperatureSignalingInterval == "12 hours" ? 4320:
                    excessTemperatureSignalingInterval == "18 hours" ? 6480 : excessTemperatureSignalingInterval == "24 hours" ? 8640: 1)
        }

        //10. Lack of Z-Wave range indication interval: 1-8640 (multiply by 10 secs) [10s-24hr], default 180 (30 mins)
        if (lackOfZwaveRangeIndicationInterval != "null") {
            request += zwave.configurationV1.configurationSet(parameterNumber: 32, size: 2,
            scaledConfigurationValue: lackOfZwaveRangeIndicationInterval == "10 seconds" ? 1 : lackOfZwaveRangeIndicationInterval == "30 seconds" ? 3 :
            lackOfZwaveRangeIndicationInterval == "1 minute" ? 6: lackOfZwaveRangeIndicationInterval == "30 minutes" ? 180 : lackOfZwaveRangeIndicationInterval == "1 hour" ? 360 :
            lackOfZwaveRangeIndicationInterval == "6 hours" ? 2160 : lackOfZwaveRangeIndicationInterval == "12 hours" ? 4320: lackOfZwaveRangeIndicationInterval == "18 hours" ? 6480 :
            lackOfZwaveRangeIndicationInterval == "24 hours" ? 8640: 180)
        }

        //11. get battery level when device is paired
        request += zwave.batteryV1.batteryGet()

        //12. get temperature reading from device
        request += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)

        commands(request) + ["delay 10000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]

    }

}

private command(physicalgraph.zwave.Command cmd) {
    if (isSecured()) {
        log.info "Sending secured command: ${cmd}"
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        log.info "Sending unsecured command: ${cmd}"
        cmd.format()
    }
}

private commands(commands, delay=200) {
    log.info "inside commands: ${commands}"
    delayBetween(commands.collect{ command(it) }, delay)
}

def parse(String description) {
    log.debug "parse() >> description: $description"
    def result = null
    if (description.startsWith("Err 106")) {
        log.debug "parse() >> Err 106"
        result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true,
                descriptionText: "This sensor failed to complete the network security key exchange. " +
                        "If you are unable to control it via SmartThings, you must remove it from your network and add it again.")
    } else if (description != "updated") {
        log.debug "parse() >> zwave.parse(description)"
        def cmd = zwave.parse(description, [0x31: 5, 0x71: 3, 0x84: 1])
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    log.debug "After zwaveEvent(cmd) >> Parsed '${description}' to ${result.inspect()}"
    return result
}

//1. zwaveEvent 5E (Z-Wave Plus Info)

//2. zwaveEvent 86 (Version)
def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.info "Executing zwaveEvent 86 (VersionV1): 12 (VersionReport) with cmd: $cmd"
    def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
    updateDataValue("fw", fw)
    def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
    createEvent(descriptionText: text, isStateChange: false)
}

//3. zwaveEvent 80 (Battery) : 03 (BatteryReport)
def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} battery is low"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    setConfigured("true")  //when battery is reported back meaning configuration is
    //Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
    state.lastbatt = now()
    createEvent(map)
}

//4. zwaveEvent 22 (Application Status) : 01 (ApplicationBusy), 02 (ApplicationRejectedRequest)
def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationBusy cmd) {
    def msg = cmd.status == 0 ? "try again later" :
              cmd.status == 1 ? "try again in $cmd.waitTime seconds" :
                      cmd.status == 2 ? "request queued" : "sorry"
    createEvent(displayed: true, descriptionText: "$device.displayName is busy, $msg")
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    createEvent(displayed: true, descriptionText: "$device.displayName rejected the last request")
}

//5. zwaveEvent 98 (Security) : 81 (SecurityMessageEncapsulation), 03 (SecurityCommandsSupportedReport), 07 (NetworkKeyVerify), 05 (SecuritySchemeReport)
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    setSecured()
    def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 5, 0x71: 3, 0x84: 1])
    if (encapsulatedCommand) {
        log.debug "command: 98 (Security) 81(SecurityMessageEncapsulation) encapsulatedCommand:  $encapsulatedCommand"
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract encapsulated cmd from $cmd"
        createEvent(descriptionText: cmd.toString())
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
    log.info "Executing zwaveEvent 98 (SecurityV1): 03 (SecurityCommandsSupportedReport) with cmd: $cmd"
    setSecured()
    response(configure()) //configure device using SmartThings default settings
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
    log.info "Executing zwaveEvent 98 (SecurityV1): 07 (NetworkKeyVerify) with cmd: $cmd (node is securely included)"
    createEvent(name:"secureInclusion", value:"success", descriptionText:"Secure inclusion was successful", isStateChange: true, displayed: true)
    //after device securely joined the network, call configure() to config device
    setSecured()
    response(configure()) //configure device using SmartThings default settings
}

//6. zwaveEvent 71 (NotificationV3): 05 (NotificationReport)
def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    log.info "Executing zwaveEvent 71 (NotificationV3): 05 (NotificationReport) with cmd: $cmd"
    def result = []
    if (cmd.notificationType == 7) {
        switch (cmd.event) {
            case 0:
                result << createEvent(name: "tamper", value: "clear", displayed: false)
                break
            case 3:
                result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName casing was opened")
                break
        }
    } else if (cmd.notificationType == 1) { //Smoke Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Smoke Alarm (V2)"
        result << smokeAlarmEvent(cmd.event)
    } else if (cmd.notificationType == 2) { //CO Alarm (V2)
        log.debug "notificationv3.NotificationReport: for CO Alarm (V2)"
        result << coAlarmEvent(cmd.event)
    } else if (cmd.notificationType == 4) { // Heat Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Heat Alarm (V2)"
        result << heatAlarmEvent(cmd.event)
    } else {
        log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
        result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
    }

    result
}

def smokeAlarmEvent(value) {
    log.debug "smokeAlarmEvent(value): $value"
    def map = [name: "smoke"]
    if (value == 1 || value == 2) {
        map.value = "detected"
        map.descriptionText = "$device.displayName detected smoke"
    } else if (value == 0) {
        map.value = "clear"
        map.descriptionText = "$device.displayName is clear (no smoke)"
    } else if (value == 3) {
        map.value = "tested"
        map.descriptionText = "$device.displayName smoke alarm test"
    } else if (value == 4) {
        map.value = "replacement required"
        map.descriptionText = "$device.displayName replacement required"
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

def coAlarmEvent(value) {
    log.debug "coAlarmEvent(value): $value"
    def map = [name: "carbonMonoxide"]
    if (value == 1 || value == 2) {
        map.value = "detected"
        map.descriptionText = "$device.displayName detected carbon monoxide"
    } else if (value == 0) {
        map.value = "clear"
        map.descriptionText = "$device.displayName is clear (no carbon monoxide)"
    } else if (value == 3) {
        map.value = "tested"
        map.descriptionText = "$device.displayName carbon monoxide alarm test"
    } else if (value == 4) {
        map.value = "replacement required"
        map.descriptionText = "$device.displayName replacement required"
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

def heatAlarmEvent(value) {
    log.debug "heatAlarmEvent(value): $value"
    def map = [name: "heatAlarm"]
    if (value == 1 || value == 2) {
        map.value = "overheat detected"
        map.descriptionText = "$device.displayName overheat detected"
    } else if (value == 0) {
        map.value = "clear"
        map.descriptionText = "$device.displayName is clear (no overheat)"
    } else if (value == 3 || value == 4) {
        map.value = "rapid temperature rise"
        map.descriptionText = "$device.displayName rapid temperature rise"
    } else if (value == 5 || value == 6) {
        map.value = "underheat detected"
        map.descriptionText = "$device.displayName underheat detected"
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

//7. zwaveEvent 84 WakeUp: 07 (WakeUpNotification)
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
    log.info "Executing zwaveEvent 84 (WakeUpV1): 07 (WakeUpNotification) with cmd: $cmd"
    def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

    if (!isConfigured()) {
        log.debug("late configure")
        result += response(configure()) // configuring a newly joined device or joined device with preference update
    } else {
        //Only ask for battery if we havn't had a BatteryReport in a while
        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
            log.debug("Device has been configured sending >> batteryGet()")
            result += response(zwave.batteryV1.batteryGet())
            result += response("delay 1200")  // leave time for device to respond to batteryGet
        }
        log.debug("Device has been configured sending >> wakeUpNoMoreInformation()")
        result += response(zwave.wakeUpV1.wakeUpNoMoreInformation()) //tell device back to "sleeping"
    }
    result
}

//8. zwaveEvent 31 (SensorMultilevelV5) : 05 (SensorMultilevelReport)
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

//9. zwaveEvent 5A (DeviceResetLocallyV1) : 01 (DeviceResetLocallyNotification)
def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
    log.info "Executing zwaveEvent 5A (DeviceResetLocallyV1) : 01 (DeviceResetLocallyNotification) with cmd: $cmd"
    createEvent(descriptionText: cmd.toString(), isStateChange: true, displayed: true)
}

//10. zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport)
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.info "Executing zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport) with cmd: $cmd"
    log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"

    def result = []

    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)

    result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
    result
}

//11. zwaveEvent 85 (Association) : 03 (AssociationReport)
def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    def result = []
    if (cmd.nodeId.any { it == zwaveHubNodeId }) {
        result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
    } else if (cmd.groupingIdentifier == 1) {
        // We're not associated properly to group 1, set association
        result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
        result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
    }
    result
}

//This is to capture zwave command that needs to be handle
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.warn "General zwaveEvent cmd: ${cmd}"
    createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

//helper method --------------------------------------------
private setConfigured(configure) {
    updateDataValue("configured", configure)
}

private isConfigured() {
    getDataValue("configured") == "true"
}

private setSecured() {
    updateDataValue("secured", "true")
}

private isSecured() {
    getDataValue("secured") == "true"
}
