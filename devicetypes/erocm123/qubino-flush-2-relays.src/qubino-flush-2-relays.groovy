/**
 *
 *  Qubino Flush 2 Relays
 *
 *  github: Eric Maycock (erocm123)
 *  Date: 2017-02-22
 *  Copyright Eric Maycock
 *
 *  Includes all configuration parameters and ease of advanced configuration.
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
    definition (name: "Qubino Flush 2 Relays", namespace: "erocm123", author: "Eric Maycock") {
        capability "Actuator"
        capability "Sensor"
        capability "Switch"
        capability "Polling"
        capability "Configuration"
        capability "Refresh"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Temperature Measurement"
        capability "Health Check"

        fingerprint mfr: "0159", prod: "0002", model: "0051"
        fingerprint deviceId: "0x1001", inClusters:"0x5E,0x86,0x72,0x5A,0x73,0x20,0x27,0x25,0x32,0x60,0x85,0x8E,0x59,0x70", outClusters:"0x20"
    }

    simulator {
    }

    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        generate_preferences(configuration_model())
    }

    tiles {
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
            }
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
                attributeState "statusText", label:'${currentValue}'
            }
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
        }
        valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue} kWh'
        }
        valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue} W'
        }

        standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'reset kWh', action:"reset"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
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

        main(["switch","switch1", "switch2"])
        details(["switch",
                childDeviceTiles("all"),
                "temperature","refresh","configure",
                "reset"
                ])
   }
}

def parse(String description) {
    def result = []
    def cmd = zwave.parse(description)
    if (cmd) {
        result += zwaveEvent(cmd)
        logging("Parsed ${cmd} to ${result.inspect()}", 1)
    } else {
        logging("Non-parsed event: ${description}", 2)
    }

    def statusTextmsg = ""

    result.each {
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "power") {
            statusTextmsg = "${it.value} W ${device.currentValue('energy')? device.currentValue('energy') : "0"} kWh"
        }
        if ((it instanceof Map) == true && it.find{ it.key == "name" }?.value == "energy") {
            statusTextmsg = "${device.currentValue('power')? device.currentValue('power') : "0"} W ${it.value} kWh"
        }
    }
    if (statusTextmsg != "") sendEvent(name:"statusText", value:statusTextmsg, displayed:false)

    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    logging("BasicReport ${cmd}", 2)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    logging("BasicSet ${cmd}", 2)
    def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def cmds = []
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    return [result, response(commands(cmds))] // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null) {
    logging("SwitchBinaryReport ${cmd} , ${ep}", 2)
    if (ep) {
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"}
        if (childDevice)
            childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
    } else {
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        return [result, response(commands(cmds))] // returns the result of reponse()
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
    logging("MeterReport $cmd : Endpoint: $ep", 2)
    def result
    def cmds = []
    if (cmd.scale == 0) {
        result = [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
    } else if (cmd.scale == 1) {
        result = [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
    } else {
        result = [name: "power", value: cmd.scaledMeterValue, unit: "W"]
    }
    if (ep) {
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"}
        if (childDevice)
            childDevice.sendEvent(result)
    } else {
       (1..2).each { endpoint ->
            cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
            cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
       }
       return [createEvent(result), response(commands(cmds))]
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   logging("MultiChannelCmdEncap ${cmd}", 2)
   def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
   if (encapsulatedCommand) {
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
   }
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    logging("SensorMultilevelReport: $cmd", 2)
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.unit = getTemperatureScale()
            logging("Temperature Report: $map.value", 2)
            break;
        default:
            map.descriptionText = cmd.toString()
    }

    return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logging("ManufacturerSpecificReport ${cmd}", 2)
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    logging("msr: $msr", 2)
    updateDataValue("MSR", msr)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    logging("Unhandled Event: ${cmd}", 2)
}

def on() {
    logging("on()", 1)
    commands([
        zwave.switchAllV1.switchAllOn(),
        encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
        encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    ])
}

def off() {
    logging("off()", 1)
    commands([
        zwave.switchAllV1.switchAllOff(),
        encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
        encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    ])
}

void childOn(String dni) {
    logging("childOn($dni)", 1)
    def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    sendHubCommand(cmds)
}

void childOff(String dni) {
    logging("childOff($dni)", 1)
    def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    sendHubCommand(cmds)
}

void childRefresh(String dni) {
    logging("childRefresh($dni)", 1)
    def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 0), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 2), channelNumber(dni))))
    sendHubCommand(cmds)
}

def poll() {
    logging("poll()", 1)
    commands([
       encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
       encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
    ])
}

def refresh() {
    logging("refresh()", 1)
    commands([
        encap(zwave.switchBinaryV1.switchBinaryGet(), 1),
        encap(zwave.switchBinaryV1.switchBinaryGet(), 2),
        zwave.meterV2.meterGet(scale: 0),
        zwave.meterV2.meterGet(scale: 2),
        zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1)
    ])
}

def reset() {
    logging("reset()", 1)
    commands([
        zwave.meterV2.meterReset(),
        zwave.meterV2.meterGet()
    ])
}

def ping() {
    logging("ping()", 1)
    refresh()
}

def installed() {
    logging("installed()", 1)
    command(zwave.manufacturerSpecificV1.manufacturerSpecificGet())
    createChildDevices()
}

def configure() {
    logging("configure()", 1)
    def cmds = []
    cmds = update_needed_settings()
    if (cmds != []) commands(cmds)
}

def updated() {
    logging("updated()", 1)
    if (!childDevices) {
        createChildDevices()
    } else if (device.label != state.oldLabel) {
        childDevices.each {
            if (it.label == "${state.oldLabel} (Q${channelNumber(it.deviceNetworkId)})") {
                def newLabel = "${device.displayName} (Q${channelNumber(it.deviceNetworkId)})"
                it.setLabel(newLabel)
            }
        }
        state.oldLabel = device.label
    }
    def cmds = []
    cmds = update_needed_settings()
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    if (cmds != []) response(commands(cmds))
}

def generate_preferences(configuration_model) {
    def configuration = parseXml(configuration_model)

    configuration.Value.each {
        if(it.@hidden != "true" && it.@disabled != "true") {
            switch(it.@type) {
                case ["number"]:
                    input "${it.@index}", "number",
                        title:"${it.@label}\n" + "${it.Help}",
                        range: "${it.@min}..${it.@max}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}"
                    break
                case "list":
                    def items = []
                    it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                    input "${it.@index}", "enum",
                        title:"${it.@label}\n" + "${it.Help}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}",
                        options: items
                    break
                case "decimal":
                    input "${it.@index}", "decimal",
                        title:"${it.@label}\n" + "${it.Help}",
                        range: "${it.@min}..${it.@max}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}"
                    break
                case "boolean":
                    input "${it.@index}", "boolean",
                        title:"${it.@label}\n" + "${it.Help}",
                        defaultValue: "${it.@value}",
                        displayDuringSetup: "${it.@displayDuringSetup}"
                    break
            }
        }
    }
}

 /*  Code has elements from other community source @CyrilPeponnet (Z-Wave Parameter Sync). */

def update_current_properties(cmd) {
    def currentProperties = state.currentProperties ?: [:]

    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue
    
    def parameterSettings = parseXml(configuration_model()).Value.find{it.@index == "${cmd.parameterNumber}"}

    if (settings."${cmd.parameterNumber}" != null || parameterSettings.@hidden == "true") {
        if (convertParam(cmd.parameterNumber, parameterSettings.@hidden != "true"? settings."${cmd.parameterNumber}" : parameterSettings.@value) == cmd2Integer(cmd.configurationValue)) {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        } else {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }

    state.currentProperties = currentProperties
}

def update_needed_settings() {
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]

    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1])
    //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1)
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
    cmds << zwave.associationV2.associationGet(groupingIdentifier: 1)
    
    configuration.Value.each {
        if ("${it.@setting_type}" == "zwave" && it.@disabled != "true") {
            if (currentProperties."${it.@index}" == null) {
                if (it.@setonly == "true") {
                    logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"), 2)
                    def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                    cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                } else {
                    isUpdateNeeded = "YES"
                    logging("Current value of parameter ${it.@index} is unknown", 2)
                    cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
                }
            } else if ((settings."${it.@index}" != null || "${it.@hidden}" == "true") && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}")) {
                isUpdateNeeded = "YES"
                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}"), 2)
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), "${it.@hidden}" != "true"? settings."${it.@index}" : "${it.@value}")
                cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            }
        }
    }

    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: true)
    return cmds
}

def convertParam(number, value) {
    def parValue
    switch (number) {
        case 110:
            if (value < 0)
                parValue = value * -1 + 1000
            else
                parValue = value
            break
        default:
            parValue = value
            break
    }
    return parValue.toInteger()
}

private def logging(message, level) {
    if (logLevel != "0") {
        switch (logLevel) {
            case "1":
                if (level > 1)
                    log.debug "$message"
                break
            case "99":
                log.debug "$message"
                break
        }
    }
}

/**
* Convert byte values to integer
*/
def cmd2Integer(array) {
    switch(array.size()) {
        case 1:
            array[0]
            break
        case 2:
            ((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
            break
        case 3:
            ((array[0] & 0xFF) << 16) | ((array[1] & 0xFF) << 8) | (array[2] & 0xFF)
            break
        case 4:
            ((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
            break
    }
}

def integer2Cmd(value, size) {
    switch(size) {
        case 1:
            [value]
            break
        case 2:
            def short value1 = value & 0xFF
            def short value2 = (value >> 8) & 0xFF
            [value2, value1]
            break
        case 3:
            def short value1 = value & 0xFF
            def short value2 = (value >> 8) & 0xFF
            def short value3 = (value >> 16) & 0xFF
            [value3, value2, value1]
            break
        case 4:
            def short value1 = value & 0xFF
            def short value2 = (value >> 8) & 0xFF
            def short value3 = (value >> 16) & 0xFF
            def short value4 = (value >> 24) & 0xFF
            [value4, value3, value2, value1]
            break
    }
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    update_current_properties(cmd)
    logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'", 2)
}

private encap(cmd, endpoint) {
    if (endpoint) {
        zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
    } else {
        cmd
    }
}

private command(physicalgraph.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=1000) {
    delayBetween(commands.collect{ command(it) }, delay)
}

private channelNumber(String dni) {
    dni.split("-ep")[-1] as Integer
}

private void createChildDevices() {
    state.oldLabel = device.label
    try {
        for (i in 1..2) {
            addChildDevice("Metering Switch Child Device", "${device.deviceNetworkId}-ep${i}", null,
                [completedSetup: true, label: "${device.displayName} (Q${i})",
                isComponent: false, componentName: "ep$i", componentLabel: "Output $i"])
        }
    } catch (e) {
        log.debug e
        runIn(2, "sendAlert")
    }
}

private sendAlert() {
    sendEvent(
        descriptionText: "Child device creation failed. Please make sure that the \"Metering Switch Child Device\" is installed and published.",
        eventType: "ALERT",
        name: "childDeviceCreation",
        value: "failed",
        displayed: true,
    )
}

def configuration_model() {
'''
<configuration>
<Value type="list" byteSize="1" index="1" label="Input 1 switch type" min="0" max="1" value="1" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: Bi-stable switch type (Toggle)
</Help>
        <Item label="Mono-stable switch type (Push button)" value="0" />
        <Item label="Bi-stable switch type (Toggle)" value="1" />
</Value>
<Value type="list" byteSize="1" index="2" label="Input 2 switch type" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: Bi-stable switch type (Toggle)
</Help>
        <Item label="Mono-stable switch type (Push button)" value="0" />
        <Item label="Bi-stable switch type (Toggle)" value="1" />
</Value>
<Value type="list" byteSize="2" index="10" label=" Activate / deactivate functions ALL ON/ALL OFF" min="0" max="255" value="255" setting_type="zwave" fw="">
 <Help>
ALL ON active
Range: 0, 1, 2, 255
Default: ALL ON active, ALL OFF active
</Help>
        <Item label="ALL ON active, ALL OFF active" value="255" />
        <Item label="ALL ON is not active, ALL OFF is not active" value="0" />
        <Item label="ALL ON is not active, ALL OFF active" value="1" />
        <Item label="ALL ON active, ALL OFF is not active" value="2" />
</Value>
<Value type="list" byteSize="1" index="15" label="Automatic turning off / on seconds or milliseconds selection" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (Seconds)
</Help>
        <Item label="Seconds" value="0" />
        <Item label="Milliseconds" value="1" />
</Value>
<Value type="number" byteSize="2" index="11" label="Automatic turning off output Q1 after set time " min="0" max="32535" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32535
Default: 0 (Disabled)
</Help>
</Value>
<Value type="number" byteSize="2" index="12" label="Automatic turning on output Q1 after set time " min="0" max="32535" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32535
Default: 0 (Disabled)
</Help>
</Value>
<Value type="number" byteSize="2" index="13" label="Automatic turning off output Q2 after set time " min="0" max="32535" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32535
Default: 0 (Disabled)
</Help>
</Value>
<Value type="number" byteSize="2" index="14" label="Automatic turning on output Q2 after set time " min="0" max="32535" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32535
Default: 0 (Disabled)
</Help>
</Value>
<Value type="list" byteSize="1" index="30" label="State of the relay after a power failure" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (Previous State)
</Help>
        <Item label="Previous" value="0" />
        <Item label="Off" value="1" />
</Value>
<Value type="number" byteSize="1" index="40" label="Power reporting in Watts on power change % for Q1" min="0" max="100" value="5" setting_type="zwave" fw="">
 <Help>
Range: 0 (Disabled) to 100
Default: 5 (%)
</Help>
</Value>
<Value type="number" byteSize="2" index="42" label="Power reporting in Watts by time interval for Q1" min="0" max="32767" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32767
Default: 0 (Disabled)
</Help>
</Value>
<Value type="number" byteSize="1" index="41" label="Power reporting in Watts on power change % for Q2" min="0" max="100" value="5" setting_type="zwave" fw="">
 <Help>
Range: 0 (Disabled) to 100
Default: 5 (%)
</Help>
</Value>
<Value type="number" byteSize="2" index="43" label="Power reporting in Watts by time interval for Q2" min="0" max="32767" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 32767
Default: 0 (Disabled)
</Help>
</Value>
<Value type="list" byteSize="1" index="63" label="Output Q1 Switch selection" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (NC - Normally Closed)
</Help>
        <Item label="NC (normally closed)" value="0" />
        <Item label="NO (normally open)" value="1" />
</Value>
<Value type="list" byteSize="1" index="64" label="Output Q2 Switch selection" min="0" max="1" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 1
Default: 0 (NC - Normally Closed)
</Help>
        <Item label="NC (normally closed)" value="0" />
        <Item label="NO (normally open)" value="1" />
</Value>
<Value type="number" byteSize="2" index="110" label="Temperature sensor offset settings" min="1" max="32536" value="32536" setting_type="zwave" fw="">
 <Help>
1 to 100 value from 0.1C to 10.0C is added to actual measured temperature.
1001 to 1100 value from -0.1C to -10.0C is subtracted to actual measured temperature.
Range: 1 to 32536
Default: 32536 (0.0)
</Help>
</Value>
<Value type="number" byteSize="1" index="120" label="Digital temperature sensor reporting" min="0" max="127" value="0" setting_type="zwave" fw="">
 <Help>
Range: 0 to 127
Default: 0 (Disabled)
</Help>
</Value>
  <Value type="list" index="logLevel" label="Debug Logging Level?" value="0" setting_type="preference" fw="">
    <Help>
    </Help>
        <Item label="None" value="0" />
        <Item label="Reports" value="1" />
        <Item label="All" value="99" />
  </Value>
</configuration>
'''
}