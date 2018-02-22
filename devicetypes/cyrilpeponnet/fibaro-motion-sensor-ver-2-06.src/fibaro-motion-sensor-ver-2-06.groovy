/**
 *  Device Type Definition File
 *
 *  Device Type:        Fibaro Motion Sensor
 *  File Name:          FibarMotionSensor.groovy
 *  Initial Release:    2015-06-23
 *  Author:             Cyril Peponnet
 *  Email:              cyril@peponnet.fr
 *
 *  Copyright 2015 Cyril Peponnet
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
 ***************************************************************************************
*/

/**
 * Sets up metadata, preferences simulator info and tile definition.
 *
 */

metadata {
    definition (name: "Fibaro Motion Sensor ver 2.06", namespace: "cyrilpeponnet", author: "Cyril Peponnet") {

        attribute   "needUpdate", "string"

        capability  "Acceleration Sensor"
        capability  "Battery"
        capability  "Configuration"
        capability  "Illuminance Measurement"
        capability  "Motion Sensor"
        capability  "Sensor"
        capability  "Temperature Measurement"

        fingerprint deviceId: "0x2001", inClusters: "0x30,0x84,0x85,0x80,0x8F,0x56,0x72,0x86,0x70,0x8E,0x31,0x9C,0xEF,0x30,0x31,0x9C"
    }

    preferences {
        input description: "Once you change values on this page, the `Synced` Status will become `pending` status.\
                            You can then force the sync by triple click the b-button on the device or wait for the\
                            next WakeUp (every 2 hours).",

              displayDuringSetup: false, type: "paragraph", element: "paragraph"

        generate_preferences(configuration_model())
    }

    simulator {

        status "motion (basic)"     : zwave.basicV1.basicSet(value:0xFF).incomingMessage()
        status "no motion (basic)"  : zwave.basicV1.basicSet(value:0).incomingMessage()
        status "motion (binary)"    : zwave.sensorBinaryV2.sensorBinaryReport(sensorType:12, sensorValue:0xFF).incomingMessage()
        status "no motion (binary)" : zwave.sensorBinaryV2.sensorBinaryReport(sensorType:12, sensorValue:0).incomingMessage()
        status "wake up"            : "command: 8407, payload: "

        for (int i = 0; i <= 100; i += 20) {
            status "Vibration ${i}%": zwave.sensorAlarmV1.sensorAlarmReport(
                seconds: 30, sensorType: 0x00 /*general purpose */, sensorState:i, sourceNodeId: 2).incomingMessage()
        }

        for (int i = 0; i <= 100; i += 20) {
            status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
        }

        for (int i = 200; i <= 1000; i += 200) {
            status "luminance ${i} lux": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
                scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
        }

        for (int i = 0; i <= 100; i += 20) {
            status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
                batteryLevel: i).incomingMessage()
        }
    }

    tiles {
        standardTile("motion", "device.motion", width: 2, height: 2) {
            state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
            state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false) {
            state "temperature", label:'${currentValue}°',
            backgroundColors:
            [
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("illuminance", "device.illuminance", inactiveLabel: false) {
            state "luminosity", label:'${currentValue} lux', unit:"lux"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
            state "battery", label:'${currentValue}% battery', unit:""
        }
        standardTile("acceleration", "device.acceleration") {
            state("vibration", label:'${currentValue}', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
            state("still", label:'${currentValue}', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
            state("moving", label:'vibration', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
            state("active", label:'vibration', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
            state("inactive", label:'still', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
        }
        standardTile("configure", "device.needUpdate", inactiveLabel: false) {
            state "NO" , label:'Synced', action:"configuration.configure", icon:"st.secondary.refresh-icon", backgroundColor:"#99CC33"
            state "YES", label:'Pending', action:"configuration.configure", icon:"st.secondary.refresh-icon", backgroundColor:"#CCCC33"
        }
        main(["motion", "temperature", "illuminance", "acceleration"])
        details(["motion", "temperature", "illuminance", "acceleration", "configure", "battery"])
    }
}

/**
* This function generate the preferences menu from the XML file
* each input will be accessible from settings map object.
*/
def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
    configuration.Value.each
    {
        switch(it.@type)
        {
            case ["byte","short"]:
                input "${it.@index}", "number",
                    title:"${it.@index} - ${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"${it.@index} - ${it.@label}\n" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    options: items
            break
        }
    }
}

/**
* Parse incoming device messages to generate events
*/
def parse(String description)
{
    log.debug "==> New Zwave Event: ${description}, Battery: ${state.lastBatteryReport}"

    def result = []

    switch(description) {
        case ~/Err.*/:
            log.error "Error: $description"
        break
        // updated is hit when the device is paired.
        case "updated":
            result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 7200, nodeid:zwaveHubNodeId).format())
            result << response(zwave.batteryV1.batteryGet().format())
            result << response(zwave.versionV1.versionGet().format())
            result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
            result << response(zwave.firmwareUpdateMdV2.firmwareMdGet().format())
            result << response(configure())
        break
        default:
            def cmd = zwave.parse(description, [0x72: 2, 0x31: 2, 0x30: 1, 0x84: 1, 0x9C: 1, 0x70: 2, 0x80: 1, 0x86: 1, 0x7A: 1, 0x56: 1])
            if (cmd) {
                result += zwaveEvent(cmd)
            }
        break
    }

    log.debug "=== Parsed '${description}' to ${result.inspect()}"
    if ( result[0] != null ) { result }
}

/**
* Handle and decode encapsulated cmds
*/
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
    def versions = [0x31: 2, 0x30: 1, 0x84: 1, 0x9C: 1, 0x70: 2]
    // def encapsulatedCommand = cmd.encapsulatedCommand(versions)
    def version = versions[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (!encapsulatedCommand) {
        log.debug "Could not extract command from $cmd"
    } else {
        //log.debug "Encapsulated Command $cmd -> $encapsulatedCommand"
        zwaveEvent(encapsulatedCommand)
    }
}

/**
* Sensors Events
*/
def motionValueEvent(Short value, String message)
{
    def description = value ? "$device.displayName detected $message " : "$device.displayName $message has stopped"
    def returnValue = value ? "active" : "inactive"
    createEvent([name: "motion", value: returnValue, descriptionText: description ])
}

/**
* Zwave events overload
*/
def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
    log.debug "%%%% Sensor Alarm Report"
    def returnValue = ""
    def message = ""
    switch(cmd.sensorState) {
        case 0:
            returnValue = "still"
            message     = "$device.displayName is now still"
        break
        case 255:
            returnValue = "moving"
            message     = "$device.displayName is moving"
        break
        default:
            returnValue = cmd.sensorState
            message     = "$device.displayName detected some vibrations"
        break
    }
    createEvent([ name: "acceleration", value: returnValue , descriptionText: message ])
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
    log.debug "%%%% Sensor Binary Report"
    motionValueEvent(cmd.sensorValue, "motion")
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
    log.debug "%%%% Basic Set Cmd"
    motionValueEvent(cmd.value, "motion")
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
    log.debug "%%%% Multi Sensor Report"
    def map = [ displayed: true ]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = Math.round(convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale,  cmd.precision).toFloat()).toString()
			map.unit  = getTemperatureScale()
            break;
        case 3:
            map.name = "illuminance"
            map.value = cmd.scaledSensorValue.toInteger().toString()
            map.unit = "lux"
            break;
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
    def map = [ name: "battery", unit: "%" ]
    if (cmd.batteryLevel == 0xFF)
    {
        map.value = 1
        map.descriptionText = "${device.displayName} battery is low"
        map.isStateChange = true
    }
    else
    {
        map.value = cmd.batteryLevel
    }
    state.lastBatteryReport = now()
    createEvent(map)
}

/**
* This is called each time your device will wake up.
*/
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    log.debug "%%%% Device ${device.displayName} woke up"
    def commands = sync_properties()
    sendEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
    // check if we need to request battery level (every 48h)
    if (!state.lastBatteryReport || (now() - state.lastBatteryReport)/60000 >= 60 * 48)
    {
        commands << zwave.batteryV1.batteryGet().format()
    }
    // Adding No More infomration needed at the end
    commands << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
    response(delayBetween(commands, 1500))
}


/**
* This will be called each time we update a paramter. Use it to fill our currents parameters as a callback
*/
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    update_current_properties(cmd)
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

/**
* This is called the first time upon association,
*/

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {

    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    def result = []

    log.debug "msr: $msr"
    updateDataValue("MSR", msr)
    result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

    if ( msr == "010F-0800-2001" )
    {
        log.debug "Dealing with a Fibaro Motion Sensor."
        result << response(configure())
    }

    result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "Catchall reached for cmd: ${cmd.toString()}}"
    createEvent(displayed: false, descriptionText: "$device.displayName: $cmd")
}

/**
* Only for information purpose upon association
*/
def createEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, Map item1) {
    log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def createEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd, Map item1) {
    updateDataValue("applicationVersion", "${cmd.applicationVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def createEvent(physicalgraph.zwave.commands.firmwareupdatemdv1.FirmwareMdReport cmd, Map item1) {
    log.debug "checksum:       ${cmd.checksum}"
    log.debug "firmwareId:     ${cmd.firmwareId}"
    log.debug "manufacturerId: ${cmd.manufacturerId}"
}

/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
    // Only used to toggle the status if update is needed
    update_needed_settings()
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
}

/**
* Update current cache properties
*/
def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]

    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (settings."${cmd.parameterNumber}".toInteger() == cmd2Integer(cmd.configurationValue))
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }

    state.currentProperties = currentProperties
}

/**
* Update needed settings
*/
def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    configuration.Value.each
    {
        if (currentProperties."${it.@index}" == null)
        {
            log.debug "Current value of parameter ${it.@index} is unknown"
            isUpdateNeeded = "YES"
        }
        else if (settings."${it.@index}" != null && cmd2Integer(currentProperties."${it.@index}") != settings."${it.@index}".toInteger())
        {
            log.debug "Parameter ${it.@index} will be updated to " + settings."${it.@index}"
            isUpdateNeeded = "YES"
            switch(it.@type)
            {
                case ["byte", "list"]:
                    cmds << zwave.configurationV1.configurationSet(configurationValue: [(settings."${it.@index}").toInteger()], parameterNumber: it.@index.toInteger(), size: 1).format()
                break
                case "short":
                    def short valueLow   = settings."${it.@index}" & 0xFF
                    def short valueHigh = (settings."${it.@index}" >> 8) & 0xFF
                    def value = [valueHigh, valueLow]
                    cmds << zwave.configurationV1.configurationSet(configurationValue: value, parameterNumber: it.@index.toInteger(), size: 2).format()
                break
            }
            cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger()).format()
        }
    }
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)

    return cmds
}

/**
* Try to sync properties with the device
*/
def sync_properties()
{
    def currentProperties = state.currentProperties ?: [:]
    def configuration = parseXml(configuration_model())

    def cmds = []
    configuration.Value.each
    {
        if (! currentProperties."${it.@index}" || currentProperties."${it.@index}" == null)
        {
            log.debug "Looking for current value of parameter ${it.@index}"
            cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger()).format()
        }
    }

    if (device.currentValue("needUpdate") == "YES") { cmds += update_needed_settings() }
    return cmds
}

/**
* Configures the device to settings needed by SmarthThings at device discovery time.
* Need a triple click on B-button to zwave commands to pass
*/
def configure() {
    log.debug "Configuring Device For SmartThings Use"
    def cmds = []

    // Associate Group 3 Device Status (Group 1 is for Basic direct action -switches-, Group 2 for Tamper Alerts System -alarm-)
    // Hub need to be Associate to group 3
    cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
    cmds += sync_properties()
    delayBetween(cmds , 1500)
}

/**
* Convert 1 and 2 bytes values to integer
*/
def cmd2Integer(array) { array.size() == 1 ? array[0] : ((array[0] & 0xFF) << 8) | (array[1] & 0xFF) }

/**
* Define the Fibaro motion senssor model used to generate preference pane.
*/
def configuration_model()
{
'''
<configuration>
  <Value type="byte" index="1" label="Motion sensor sensitivity" min="0" max="255" value="10">
    <Help>
The lower the value is , the more sensitive the PIR sensor will be.
Available settings: 8 - 255
Default setting: 10
    </Help>
  </Value>
  <Value type="byte" index="2" label="Motion sensor blind time (insensitivity)" min="0" max="15" value="15">
    <Help>
Period of time through which the PIR sensor is "blind" (insensitive) to motion.
After this time period the PIR sensor will be again able to detect motion.
The longer the insensitivity period, the longer the battery life.
If the sensor is required to detect motion quickly, the time period may be shortened.
The time of insensitivity should be shorter than the time period set in parameter 6.
Available settings: 0 - 15
Formula to calculate the time: time [s] = 0.5 x (value + 1)
Default setting: 15 (8 seconds)
    </Help>
  </Value>
  <Value type="list" index="3" label="PIR sensor pulse counter" min="0" max="3" value="1" size="1">
    <Help>
Sets the number of moves required for the PIR sensor to report motion.
The lower the value, the less sensitive the PIR sensor.
It\'s not recommended to modify this parameter setting.
Available settings: 0 - 3
Formula to calculate the number of pulses: pulses = (value + 1)
Default setting: 1 (2 pulses)
    </Help>
        <Item label="1 pulse" value="0" />
        <Item label="2 pulses" value="1" />
        <Item label="3 pulses" value="2" />
        <Item label="4 pulses" value="3" />
  </Value>
  <Value type="list" index="4" label="PIR sensor window time" min="0" max="3" value="2" size="1">
    <Help>
Period of time during which the number of moves set in parameter 3 must be detected in order for the PIR sensor to report motion.
The higher the value, the more sensitive the PIR sensor.
It\'s not recommended to modify this parameter setting.
Available settings: 0 - 3
Formula to calculate the time: time [s] = 4 x (value + 1)
Default setting: 2 (12 seconds)
    </Help>
        <Item label="4 seconds" value="0" />
        <Item label="8 seconds" value="1" />
        <Item label="12 seconds" value="2" />
        <Item label="16 seconds" value="3" />
  </Value>
<Value type="short" index="6" label="Motion alarm cancellation delay" min="0" max="65535" value="30">
<Help>
Motion alarm will be cancelled in the main controller and the associated devices after the period of time set in this parameter.
Any motion detected during the cancellation delay time countdown will result in the countdown being restarted.
In case of small values, below 10 seconds, the value of parameter 2 must be modified (PIR sensor blind time).
Available settings: 1 - 65535
Default setting: 30 (30 seconds)
</Help>
    </Value>
<Value type="list" index="8" label="PIR sensor operating mode" min="0" max="2" value="0" size="1">
    <Help>
The parameter determines the part of day in which the PIR sensor will be active.
This parameter influences only the motion reports and associations.
Tamper, light intensity and temperature measurements will be still active, regardless of this parameter settings.
Default setting: 0 (always active)
    </Help>
    <Item label="PIR sensor always active" value="0" />
    <Item label="PIR sensor active during the day only" value="1" />
    <Item label="PIR sensor active during the night only" value="2" />
</Value>
    <Value type="short" index="9" label="Night / day" min="0" max="65535" value="200">
<Help>
The parameter defines the difference between night and day, in terms of light intensity, used in parameter 8.
Available settings: 1 - 65535
Default setting: 200 (200 lux)
</Help>
    </Value>
    <Value type="list" index="12" label="Basic command class frames configuration" min="0" max="2" value="0" size="1">
<Help>
The parameter determines the command frames sent in 1-st association group, assigned to PIR sensor.
Values of BASIC ON and BASIC OFF command frames may be modified by dedicated parameters.
Default setting: 0 (ON and OFF)
</Help>
<Item label="BASIC ON and BASIC OFF command frames sent in Basic Command Class." value="0" />
<Item label="only the BASIC ON command frame sent in Basic Command Class." value="1" />
<Item label="only the BASIC OFF command frame sent in Basic Command Class." value="2" />
    </Value>
    <Value type="byte" index="14" label="BASIC ON command frame value" min="0" max="255" value="255">
<Help>
The value of 255 allows to turn ON a device.
In case of the Dimmer, the value of 255 means turning ON at the last memorized state, e.g. the Dimmer turned ON at 30% and turned OFF using the value of 255, and then turned OFF, will be turned ON at 30%, i.e. the last memorized state.
Available settings: 0 - 255
Default setting: 255
</Help>
    </Value>
    <Value type="byte" index="16" label="BASIC OFF command frame value" min="0" max="255" value="0">
<Help>
The command frame sent at the moment of motion alarm cancellation, after the cancellation delay time, specified in parameter 6, has passed.
The value of 0 allows to turn a device OFF while the value of 255 allows to turn ON a device. In case of the Dimmer, the value of 255 means turning ON at the last memorized state, e.g. the Dimmer turned ON at 30% and turned OFF using the value of 255, and then turned OFF, will be turned ON at 30%, i.e. the last memorized state.
Available settings: 0 - 255
Default setting: 0
</Help>
    </Value>
    <Value type="byte" index="20" label="Tamper sensitivity" min="0" max="122" value="15">
<Help>
The parameter determines the changes in forces acting on the Fibaro Motion Sensor resulting in tamper alarm being reported - g-force acceleration.
Available settings: 0 - 122 (0.08 - 2g; multiply by 0.016g; 0 = tamper inactive)
Default setting: 15 (0.224g)
</Help>
    </Value>
    <Value type="short" index="22" label="Tamper alarm cancellation delay" min="0" max="65535" value="30">
<Help>
Time period after which a tamper alarm will be cancelled.
Another tampering detected during the countdown to cancellation will not extend the delay.
Available settings: 1 - 65535
Default setting: 30 (seconds)
</Help>
    </Value>
    <Value type="list" index="24" label="Tamper operating modes" min="0" max="4" value="0" size="1">
<Help>
The parameter determines the behaviour of tamper and how it reports.
    Tamper: Tamper alarm is reported in Sensor Alarm command class.
    Cancellation: Cancellation is reported in Sensor Alarm command class after the time period set in parameter 22 (Tamper Alarm Cancellation Delay).
    Orientation: Sensor\'s orientation in space is reported in Fibaro Command Class after the time period set in parameter 22.
    Vibration: The maximum level of vibrations recorded in the time period set in parameter 22 is reported. Reports stop being sent when the vibrations cease. The reports are sent in Sensor Alarm command class. Value displayed in the "value" field (0 - 100) depends on the vibrations force. Reports to the association groups are sent using Sensor Alarm command class.
Default setting: 0 (Tamper)
</Help>
<Item label="Tamper" value="0" />
<Item label="Tamper + Cancellation" value="1" />
<Item label="Tamper + Orientation" value="2" />
<Item label="Tamper + Cancellation + Orientation" value="3" />
<Item label="Vibration" value="4" />
    </Value>
    <Value type="byte" index="26" label="Tamper alarm broadcast mode" min="0" max="1" value="0">
<Help>
The parameter determines whether the tamper alarm frame will or will not be sent in broadcast mode. Alarm frames sent in broadcast mode may be received by all of the devices within communication range (if they accept such frames).
Default setting: 0
</Help>
<Item label="Tamper alarm is not sent in broadcast mode." value="0" />
<Item label="Tamper alarm sent in broadcast mode." value="1" />
    </Value>
    <Value type="short" index="40" label="Illumination report threshold" min="0" max="65535" value="200">
<Help>
The parameter determines the change in light intensity level resulting in illumination report being sent to the main controller.
Available settings: 0 - 65535 (1 - 65535 lux; 0 = reports are notsent)
Default setting: 200 (200 lux)
</Help>
    </Value>
    <Value type="short" index="42" label="Illumination reports interval" min="0" max="65535" value="0">
<Help>
Time interval between consecutive illumination reports.
The reports are sent even if there are no changes in the light intensity.
Available settings: 0 - 65535 (1 - 65535 seconds; 0 = reports arenot sent)
Default setting: 0 (no reports)
NOTE :
    1/Frequent reports will shorten the battery life.
    2/Parameter value under 5 may result in blocking the temperature reports.
</Help>
    </Value>
    <Value type="byte" index="60" label="Temperature report threshold" min="0" max="255" value="10">
<Help>
The parameter determines the change in level of temperature resulting in temperature report being sent to the main controller.
Available settings: 0 - 255 (0.1 - 25.5C; 0 = reports are not sent)
Default setting: 10 (1C)
</Help>
    </Value>
    <Value type="short" index="62" label="Interval of temperature measuring" min="0" max="65535" value="900">
<Help>
The parameter determines how often the temperature will be measured.
The shorter the time, the more frequently the temperature will be measured, but the battery life will shorten.
Available settings: 0 - 65535 (1 - 65535 seconds; 0 = temperature will not be measured)
Default setting: 900 (900 seconds)
NOTE :
 1/Frequent reports will shorten the battery life.
 2/Parameter value under 5 may result in blocking the illumination reports.
</Help>
    </Value>
    <Value type="short" index="64" label="Temperature reports interval" min="0" max="65535" value="0">
<Help>
The parameter determines how often the temperature reports will be sent to the main controller.
Available settings: 0 - 65535 (1 - 65535 seconds; 0 = reports are not sent)
Default setting: 0
</Help>
    </Value>
    <Value type="short" index="66" label="Temperature offset" min="0" max="65535" value="0">
<Help>
The value to be added to the actual temperature, measured by the sensor (temperature compensation).
Available settings: 0 - 100 (0 to 100C) or 64536 - 65535 (-100 to -0.10C)
Default setting: 0
</Help>
    </Value>
    <Value type="list" index="80" label="LED signaling mode" min="0" max="26" value="10" size="1">
<Help>
The parameter determines the way in which LED behaves after motion has been detected.
 Values 1 and from 3 to 9 = single long blink at the moment of reporting motion. No other motion will be indicated until alarm is cancelled.
 Values from 10 to 18 = single long blink at the moment of reporting motion and one short blink each time the motion is detected again.
 Values from 19 to 26 = single long blink at the moment of reporting motion and two short blinks each time the motion is detected again.
Default setting: 10 (flashlight)
</Help>
<Item label="LED inactive." value="0" />
<Item label="1 long blink, LED colour depends on the temperature. Set by parameters 86 and 87." value="1" />
<Item label="Flashlight mode - LED glows in white for 10 seconds." value="2" />
<Item label="Long blink White." value="3" />
<Item label="Long blink Red." value="4" />
<Item label="Long blink Green." value="5" />
<Item label="Long blink Blue." value="6" />
<Item label="Long blink Yellow." value="7" />
<Item label="Long blink Cyan." value="8" />
<Item label="Long blink Magenta." value="9" />
<Item label="Long blink, then short blink, LED colour depends on the temperature. Set by parameters 86 and 87." value="10" />
<Item label="Flashlight mode - LED glows in white through 10 seconds.  Each next detected motion extends the glowing by next 10 seconds." value="11" />
<Item label="Long blink, then short blinks White." value="12" />
<Item label="Long blink, then short blinks Red." value="13" />
<Item label="Long blink, then short blinks Green." value="14" />
<Item label="Long blink, then short blinks Blue." value="15" />
<Item label="Long blink, then short blinks Yellow." value="16" />
<Item label="Long blink, then short blinks Cyan" value="17" />
<Item label="Long blink, then short blinks Magenta" value="18" />
<Item label="Long blink, then 2 short blinks, LED colour depends on the temperature. Set by parameters 86 and 87." value="19" />
<Item label="Long blink, then 2 short blinks White" value="20" />
<Item label="Long blink, then 2 short blinks Red" value="21" />
<Item label="Long blink, then 2 short blinks Green" value="22" />
<Item label="Long blink, then 2 short blinks Blue" value="23" />
<Item label="Long blink, then 2 short blinks Yellow" value="24" />
<Item label="Long blink, then 2 short blinks Cyan" value="25" />
<Item label="Long blink, then 2 short blinks Magenta" value="26" />
    </Value>
    <Value type="byte" index="81" label="LED brightness" min="0" max="100" value="50">
<Help>
The parameter determines the brightness of LED when indicating motion.
Available settings: 0 - 100 (1 - 100%; 0 = brightness determined by the ambient lighting - see parameters 82 and 83)
Default setting: 50
</Help>
    </Value>
    <Value type="short" index="82" label="Ambient illumination level below which LED brightness is set to 1%" min="0" max="65535" value="100">
<Help>
The parameter is relevant only when the parameter 81 is set to 0.
Available settings: 0 to parameter 83 value
Default setting: 100 (100 lux)
</Help>
    </Value>
    <Value type="short" index="83" label="Ambient illumination level above which LED brightness is set to 100%" min="0" max="65535" value="1000">
<Help>
The parameter is relevant only when the parameter 81 is set to 0.
Available settings: parameter 82 value to 65535
Default setting: 1000 (1000 lux)
NOTE: The value of the parameter 83 must be higher than the value of the parameter 82.
</Help>
    </Value>
    <Value type="byte" index="86" label="Minimum temperature resulting in blue LED illumination" min="0" max="255" value="18">
<Help>
This parameter is relevant only when parameter 80 has been properly configured.
Available settings: 0 to parameter 87 value (degrees Celsius)
Default setting: 18 (18C)
</Help>
    </Value>
    <Value type="byte" index="87" label="Maximum temperature resulting in red LED illumination" min="0" max="255" value="28">
<Help>
This parameter is relevant only when parameter 80 has beenproperly configured.
Available settings: parameter 86 value to 255 (degrees Celsius)
Default setting: 28 (28C)
</Help>
    </Value>
    <Value type="list" index="89" label="LED indicating tamper alarm" min="0" max="1" value="1" size="1">
<Help>
Indicating mode resembles a police car (white, red and blue).
Default setting: 1 (on)
</Help>
<Item label="LED does not indicate tamper alarm." value="0" />
<Item label="LED indicates tamper alarm." value="1" />
    </Value>
</configuration>
'''
}