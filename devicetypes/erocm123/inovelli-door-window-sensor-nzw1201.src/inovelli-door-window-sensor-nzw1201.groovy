/**
 *  Inovelli Door/Window Sensor NZW1201
 *  Author: Eric Maycock (erocm123)
 *  Date: 2018-02-26
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
 *  2018-02-26: Added support for Z-Wave Association Tool SmartApp.
 *              https://github.com/erocm123/SmartThingsPublic/tree/master/smartapps/erocm123/z-waveat
 *
 */

metadata {
    definition (name: "Inovelli Door/Window Sensor NZW1201", namespace: "erocm123", author: "Eric Maycock", ocfDeviceType: "x.com.st.d.sensor.contact", vid: "generic-motion") {
        capability "Contact Sensor"
        capability "Sensor"
        capability "Battery"
        capability "Configuration"
        //capability "Health Check"
        capability "Temperature Measurement"
        
        attribute "lastActivity", "String"
        attribute "lastEvent", "String"
        attribute "firmware", "String"
        
        command "setAssociationGroup", ["number", "enum", "number", "number"] // group number, nodes, action (0 - remove, 1 - add), multi-channel endpoint (optional)

        fingerprint mfr:"015D", prod:"2003", model:"B41C", deviceJoinName: "Inovelli Door/Window Sensor"
        fingerprint mfr:"0312", prod:"2003", model:"C11C", deviceJoinName: "Inovelli Door/Window Sensor"
        fingerprint mfr:"015D", prod:"2003", model:"C11C", deviceJoinName: "Inovelli Door/Window Sensor"
        fingerprint mfr:"015D", prod:"C100", model:"C100", deviceJoinName: "Inovelli Door/Window Sensor"
        fingerprint mfr:"0312", prod:"C100", model:"C100", deviceJoinName: "Inovelli Door/Window Sensor"
        fingerprint deviceId: "0x0701", inClusters:"0x5E,0x86,0x72,0x5A,0x73,0x80,0x85,0x59,0x71,0x30,0x31,0x70,0x84"
    }

    simulator {
    }
    
    preferences {
        input "tempReportInterval", "enum", title: "Temperature Report Interval\n\nHow often you would like temperature reports to be sent from the sensor. More frequent reports will have a negative impact on battery life.\n", description: "Tap to set", required: false, options:[["10": "10 Minutes"], ["30": "30 Minutes"], ["60": "1 Hour"], ["120": "2 Hours"], ["180": "3 Hours"], ["240": "4 Hours"], ["300": "5 Hours"], ["360": "6 Hours"], ["720": "12 Hours"], ["1440": "24 Hours"]], defaultValue: "180"
        input "tempOffset", "number", title: "Temperature Offset\n\nCalibrate reported temperature by applying a negative or positive offset\nRange: -10 to 10", description: "Tap to set", required: false, range: "-10..10"
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
    def result = []
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
            result += zwaveEvent(cmd)
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
    log.debug "installed()"
    def cmds = [zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 10)]
    commands(cmds)
}

def configure() {
    log.debug "configure()"
    def cmds = initialize()
    commands(cmds)
}

def updated() {
    if (!state.lastRan || now() >= state.lastRan + 2000) {
        log.debug "updated()"
        state.lastRan = now()
        state.needfwUpdate = ""
        def cmds = initialize()
        response(commands(cmds))
    } else {
        log.debug "updated() ran within the last 2 seconds. Skipping execution."
    }
}

def initialize() {
    sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "0"])
    def cmds = processAssociations()
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    if (state.realTemperature != null) sendEvent(name:"temperature", value: getAdjustedTemp(state.realTemperature))
    if(!state.needfwUpdate || state.needfwUpdate == "") {
       log.debug "Requesting device firmware version"
       cmds << zwave.versionV1.versionGet()
    }
    if (!state.lastbat || now() - state.lastbat > 24*60*60*1000) {
        log.debug "Battery report not received in 24 hours. Requesting one now."
        cmds << zwave.batteryV1.batteryGet()
    } 
    cmds << zwave.wakeUpV1.wakeUpNoMoreInformation()
	return cmds
}

private getAdjustedTemp(value) {
    value = Math.round((value as Double) * 100) / 100
    if (tempOffset) {
       return value =  value + Math.round(tempOffset * 100) /100
    } else {
       return value
    }
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

void zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
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
    log.debug "${device.displayName} woke up"
    def cmds = processAssociations()
    
    cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    
    if(!state.wakeInterval || state.wakeInterval != (tempReportInterval? tempReportInterval.toInteger()*60:10800)){
        log.debug "Setting Wake Interval to ${tempReportInterval? tempReportInterval.toInteger()*60:10800}"
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: tempReportInterval? tempReportInterval.toInteger()*60:10800, nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
    if (!state.lastbat || now() - state.lastbat > 24*60*60*1000) {
        log.debug "Battery report not received in 24 hours. Requesting one now."
        cmds << zwave.batteryV1.batteryGet()
    } 
    if(!state.needfwUpdate || state.needfwUpdate == "") {
       log.debug "Requesting device firmware version"
       cmds << zwave.versionV1.versionGet()
    }
    
    cmds << zwave.wakeUpV1.wakeUpNoMoreInformation()
    
    response(commands(cmds))
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
    createEvent(map)
}

void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
    log.debug cmd
    if(cmd.applicationVersion && cmd.applicationSubVersion) {
	    def firmware = "${cmd.applicationVersion}.${cmd.applicationSubVersion.toString().padLeft(2,'0')}"
        state.needfwUpdate = "false"
        updateDataValue("firmware", firmware)
    }
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
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=500) {
    delayBetween(commands.collect{ command(it) }, delay)
}

def setDefaultAssociations() {
    state.associationGroups = 3
    def smartThingsHubID = zwaveHubNodeId.toString().format( '%02x', zwaveHubNodeId )
    state.defaultG1 = [smartThingsHubID]
    state.defaultG2 = []
    state.defaultG3 = []
}

def setAssociationGroup(group, nodes, action, endpoint = null){
    if (!state."desiredAssociation${group}") {
        state."desiredAssociation${group}" = nodes
    } else {
        switch (action) {
            case 0:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" - nodes
            break
            case 1:
                state."desiredAssociation${group}" = state."desiredAssociation${group}" + nodes
            break
        }
    }
}

def processAssociations(){
   def cmds = []
   setDefaultAssociations()
   def supportedGroupings = 5
   if (state.supportedGroupings) {
       supportedGroupings = state.supportedGroupings
   } else {
       log.debug "Getting supported association groups from device"
       cmds <<  zwave.associationV2.associationGroupingsGet()
   }
   for (int i = 1; i <= supportedGroupings; i++){
      if(state."actualAssociation${i}" != null){
         if(state."desiredAssociation${i}" != null || state."defaultG${i}") {
            def refreshGroup = false
            ((state."desiredAssociation${i}"? state."desiredAssociation${i}" : [] + state."defaultG${i}") - state."actualAssociation${i}").each {
                log.debug "Adding node $it to group $i"
                cmds << zwave.associationV2.associationSet(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                refreshGroup = true
            }
            ((state."actualAssociation${i}" - state."defaultG${i}") - state."desiredAssociation${i}").each {
                log.debug "Removing node $it from group $i"
                cmds << zwave.associationV2.associationRemove(groupingIdentifier:i, nodeId:Integer.parseInt(it,16))
                refreshGroup = true
            }
            if (refreshGroup == true) cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
            else log.debug "There are no association actions to complete for group $i"
         }
      } else {
         log.debug "Association info not known for group $i. Requesting info from device."
         cmds << zwave.associationV2.associationGet(groupingIdentifier:i)
      }
   }
   return cmds
}

void zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    def temp = []
    if (cmd.nodeId != []) {
       cmd.nodeId.each {
          temp += it.toString().format( '%02x', it.toInteger() ).toUpperCase()
       }
    } 
    state."actualAssociation${cmd.groupingIdentifier}" = temp
    log.debug "Associations for Group ${cmd.groupingIdentifier}: ${temp}"
    updateDataValue("associationGroup${cmd.groupingIdentifier}", "$temp")
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    log.debug "Supported association groups: ${cmd.supportedGroupings}"
    state.supportedGroupings = cmd.supportedGroupings
    return createEvent(name: "groups", value: cmd.supportedGroupings)
}