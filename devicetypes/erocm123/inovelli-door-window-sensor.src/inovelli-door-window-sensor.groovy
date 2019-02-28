 /**
 *  Inovelli Door/Window Sensor
 *  Author: Eric Maycock (erocm123)
 *  Date: 2017-08-17
 *
 *  Copyright 2017 Eric Maycock
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
    definition (name: "Inovelli Door/Window Sensor", namespace: "erocm123", author: "Eric Maycock", ocfDeviceType: "x.com.st.d.sensor.contact", vid:"generic-contact") {
        capability "Contact Sensor"
        capability "Sensor"
        capability "Battery"
        capability "Configuration"
        capability "Health Check"
        capability "Temperature Measurement"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"

        fingerprint mfr:"015D", prod:"2003", model:"B41C"
    }

    simulator {
    }
    
    preferences {
        input "tempReportInterval", "enum", title: "Temperature Report Interval\n\nHow often you would like temperature reports to be sent from the sensor. More frequent reports will have a negative impact on battery life.\nRange: 1 to 3600", description: "", required: false, options:[10: "10 Minutes", 30: "30 Minutes", 60: "1 Hour", 120: "2 Hours", 180: "3 Hours", 240: "4 Hours", 300: "5 Hours", 360: "6 Hours", 720: "12 Hours", 1440: "24 Hours"], defaultValue: 180
        input "tempOffset", "number", title: "Temperature Offset\n\nCalibrate reported temperature by applying a negative or positive offset\nRange: -10 to 10", description: "", required: false, range: "-10..10"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"contact", type: "generic", width: 6, height: 4){
            tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
                attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13"
                attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00a0dc"
            }
            tileAttribute("device.temperature", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}°',icon: "")
            }
        }
        
        valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 1) {
            state "battery", label:'${currentValue}% battery', unit:""
        }

        valueTile("lastActivity", "device.lastActivity", inactiveLabel: false, decoration: "flat", width: 4, height: 1) {
            state "default", label: 'Last Activity: ${currentValue}',icon: "st.Health & Wellness.health9"
        }
        
        valueTile("info", "device.info", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: 'After adjusting the Temperature Report Interval, open the sensor and press the small white button'
        }
        
        valueTile("icon", "device.icon", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
            state "default", label: '', icon: "https://inovelli.com/wp-content/uploads/Device-Handler/Inovelli-Device-Handler-Logo.png"
        }
    }
}

def parse(String description) {
    def result = null
    if (description.startsWith("Err 106")) {
        if (state.sec) {
            log.debug description
        } else {
            result = createEvent(
                descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
                eventType: "ALERT",
                name: "secureInclusion",
                value: "failed",
                isStateChange: true,
            )
        }
    } else if (description != "updated") {
        def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1])
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    def now
    if(location.timeZone)
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone)
    else
    now = new Date().format("yyyy MMM dd EEE h:mm:ss a")
    sendEvent(name: "lastActivity", value: now, displayed:false)
    return result
}

def installed() {
    // Device-Watch simply pings if no device events received for 482min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
}

def updated() {
    // Device-Watch simply pings if no device events received for 482min(checkInterval)
    sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    def cmds = []
    if (!state.MSR) {
        cmds = [
            command(zwave.manufacturerSpecificV2.manufacturerSpecificGet()),
            "delay 1200",
            zwave.wakeUpV1.wakeUpNoMoreInformation().format()
        ]
    } else if (!state.lastbat) {
        cmds = []
    } else {
        cmds = [zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
    }
    response(cmds)
}

private getAdjustedTemp(value) {
    value = Math.round((value as Double) * 100) / 100
    if (tempOffset) {
       return value =  value + Math.round(tempOffset * 100) /100
    } else {
       return value
    }
}

def configure() {
    commands([
        zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_DOOR_WINDOW),
        zwave.manufacturerSpecificV2.manufacturerSpecificGet()
    ], 1000)
}

def sensorValueEvent(value) {
    if (value) {
        createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
    } else {
        createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
    sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
    sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
    log.debug "WakeUpIntervalReport ${cmd.toString()}"
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
    log.debug cmd
    def result = []
    if (cmd.notificationType == 0x06 && cmd.event == 0x16) {
        result << sensorValueEvent(1)
    } else if (cmd.notificationType == 0x06 && cmd.event == 0x17) {
        result << sensorValueEvent(0)
    } else if (cmd.notificationType == 0x07) {
        if (cmd.event == 0x00) {
            result << createEvent(descriptionText: "$device.displayName covering was restored", isStateChange: true)
            result << response(command(zwave.batteryV1.batteryGet()))
        } else if (cmd.event == 0x01 || cmd.event == 0x02) {
            result << sensorValueEvent(1)
        } else if (cmd.event == 0x03) {
            result << createEvent(descriptionText: "$device.displayName covering was removed", isStateChange: true)
            if(!state.MSR) result << response(command(zwave.manufacturerSpecificV2.manufacturerSpecificGet()))
        }
    } else if (cmd.notificationType) {
        def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
        result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
    } else {
        def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
        result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
    def cmds = []
    if (!state.MSR) {
        cmds << command(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
        cmds << "delay 1200"
    }
    
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1).format()
    
    if(state.wakeInterval == null || state.wakeInterval != (tempReportInterval? tempReportInterval.toInteger()*60:10800)){
        log.debug "Setting Wake Interval to ${tempReportInterval? tempReportInterval.toInteger()*60:10800}"
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: tempReportInterval? tempReportInterval.toInteger()*60:10800, nodeid:zwaveHubNodeId).format()
        cmds << zwave.wakeUpV1.wakeUpIntervalGet().format()
    }

    if (!state.lastbat || now() - state.lastbat > 53*60*60*1000) {
        cmds << command(zwave.batteryV1.batteryGet())
    } else { // If we check the battery state we will send NoMoreInfo in the handler for BatteryReport so that we definitely get the report
        cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
    }

    [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
    log.debug "SensorMultilevelReport: $cmd"
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            state.realTemperature = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.value = getAdjustedTemp(state.realTemperature)
            map.unit = getTemperatureScale()
            log.debug "Temperature Report: $map.value"
            break;
        default:
            map.descriptionText = cmd.toString()
    }
    return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF) {
        map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
        map.isStateChange = true
    } else {
        map.value = cmd.batteryLevel
    }
    state.lastbat = now()
    [createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    def result = []

    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    log.debug "msr: $msr"
    updateDataValue("MSR", msr)

    result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
    result << response(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
    if (!device.currentState("battery")) {
        result << response(zwave.securityV1.securityMessageEncapsulation().encapsulate(zwave.batteryV1.batteryGet()).format())
    } else {
        result << response(command(zwave.batteryV1.batteryGet()))
    }

    result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1])
    if (encapsulatedCommand) {
        state.sec = 1
        zwaveEvent(encapsulatedCommand)
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

private command(physicalgraph.zwave.Command cmd) {
    if (state.sec == 1) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=200) {
    delayBetween(commands.collect{ command(it) }, delay)
}
