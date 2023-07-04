/**
 *  Note: This handler requires the "Metering Switch Child Device" to be installed.
 *
 *  Copyright 2018 Eric Maycock
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
 *  Fibaro FGS-223 Dual Relay
 *
 *  Author: Eric Maycock (erocm123)
 *
 *  08/24/2018 - Modify handler to work with secure and non-secure inclusion.
 *  04/25/2017 - Fix for combined energy & power reports & switch endpoints showing correct info.
 *  04/18/2017 - This handler requires the Metering Switch Child device to create the multiple switch endpoints.
 */
 
metadata {
definition (name: "Fibaro Double Switch 2 FGS-223", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch-power-energy") {
    capability "Sensor"
    capability "Actuator"
    capability "Switch"
    capability "Polling"
    capability "Configuration"
    capability "Refresh"
    capability "Zw Multichannel"
    capability "Energy Meter"
    capability "Power Meter"
    capability "Health Check"
    capability "Button"
    capability "Holdable Button"

    command "reset"

    fingerprint mfr: "010F", prod: "0203", model: "2000", deviceJoinName: "Fibaro Double Switch 2"
    fingerprint mfr: "010F", prod: "0203", model: "1000", deviceJoinName: "Fibaro Double Switch 2"

    fingerprint deviceId: "0x1001", inClusters:"0x5E,0x86,0x72,0x59,0x73,0x22,0x56,0x32,0x71,0x98,0x7A,0x25,0x5A,0x85,0x70,0x8E,0x60,0x75,0x5B"
}

simulator {
}

tiles(scale: 2){

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
	valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
	}
    valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
	}
    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }
    standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
    }
    standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:'reset kWh', action:"reset"
	}
    
    main(["switch"])
    details(["switch", childDeviceTiles("all"),
             "refresh","reset","configure"])

}
    preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())
    }
}

def parse(String description) {
    def result = []
    if (description.startsWith("Err 106")) {
        state.sec = 0
        result = createEvent(descriptionText: description, isStateChange: true)
    } else {
        def cmd = zwave.parse(description)
        if (cmd) {
            result += zwaveEvent(cmd)
            //log.debug "Parsed ${cmd} to ${result.inspect()}"
        } else {
            log.debug "Non-parsed event: ${description}"
        }
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    log.debug "BasicReport $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, ep=null) {
    logging("BasicSet: $cmd : Endpoint: $ep")
    if (ep) {
    def event
        childDevices.each { childDevice ->
            if (childDevice.deviceNetworkId == "$device.deviceNetworkId-ep$ep") {
                childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
            }
        }
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each { n ->
               if (n.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
               event = [createEvent([name: "switch", value: "off"])]
            } else {
               event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    }
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null)
{
    logging("SwitchBinaryReport: $cmd : Endpoint: $ep")
    if (ep) {
        def event
        childDevices.each { childDevice ->
            if (childDevice.deviceNetworkId == "$device.deviceNetworkId-ep$ep") {
                childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
            }
        }
        if (cmd.value) {
            event = [createEvent([name: "switch", value: "on"])]
        } else {
            def allOff = true
            childDevices.each { n ->
               if (n.currentState("switch").value != "off") allOff = false
            }
            if (allOff) {
               event = [createEvent([name: "switch", value: "off"])]
            } else {
               event = [createEvent([name: "switch", value: "on"])]
            }
        }
        return event
    } else {
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        return response(commands(cmds)) // returns the result of reponse()
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
    logging("MeterReport: $cmd : Endpoint: $ep")
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
       def combinedValue = 0.00
       childDevices.each {
           if(it.currentValue(result.name)) combinedValue += it.currentValue(result.name)
       }
       return createEvent([name: result.name, value: combinedValue])
    } else {
       (1..2).each { endpoint ->
			cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
            cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
	   }
       return response(commands(cmds))
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) 
{
    //log.debug "multichannelv3.MultiChannelCapabilityReport $cmd"
    if (cmd.endPoint == 2 ) {
        def currstate = device.currentState("switch2").getValue()
        if (currstate == "on")
        	sendEvent(name: "switch2", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        	sendEvent(name: "switch2", value: "on", isStateChange: true, display: false)
    }
    else if (cmd.endPoint == 1 ) {
        def currstate = device.currentState("switch1").getValue()
        if (currstate == "on")
        sendEvent(name: "switch1", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        sendEvent(name: "switch1", value: "on", isStateChange: true, display: false)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   //logging("MultiChannelCmdEncap ${cmd}")
   def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
   if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
   }
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	log.debug "AssociationReport $cmd"
    if (zwaveHubNodeId in cmd.nodeId) state."association${cmd.groupingIdentifier}" = true
    else state."association${cmd.groupingIdentifier}" = false
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	log.debug "MultiChannelAssociationReport $cmd"
    if (cmd.groupingIdentifier == 1) {
        if ([0,zwaveHubNodeId,1] == cmd.nodeId) state."associationMC${cmd.groupingIdentifier}" = true
        else state."associationMC${cmd.groupingIdentifier}" = false
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "Unhandled event $cmd"
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.switchallv1.SwitchAllReport cmd) {
   log.debug "SwitchAllReport $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
     update_current_properties(cmd)
     logging("${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'")
}

def refresh() {
	def cmds = []
    (1..2).each { endpoint ->
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), endpoint)
		cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
        cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
	}
	commands(cmds, 1000)
}

def reset() {
    logging("reset()")
	def cmds = []
    (1..2).each { endpoint ->
        cmds << encap(zwave.meterV2.meterReset(), endpoint)
        cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
        cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
    }
	commands(cmds, 1000)
}

def ping() {
	def cmds = []
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
	commands(cmds, 1000)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
    updateDataValue("MSR", msr)
}

def poll() {
	def cmds = []
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
	commands(cmds, 1000)
}

def configure() {
	state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
    if (cmds != []) commands(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    logging("CentralSceneNotification: $cmd")
    logging("sceneNumber: $cmd.sceneNumber")
    logging("sequenceNumber: $cmd.sequenceNumber")
    logging("keyAttributes: $cmd.keyAttributes")
    
    buttonEvent(cmd.keyAttributes + 1, (cmd.sceneNumber == 1? "pushed" : "held"))

}

def buttonEvent(button, value) {
    logging("buttonEvent() Button:$button, Value:$value")
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
	state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    if (!childDevices) {
		createChildDevices()
	}
	else if (device.label != state.oldLabel) {
		childDevices.each {
            if (it.label == "${state.oldLabel} (S${channelNumber(it.deviceNetworkId)})") {
			    def newLabel = "${device.displayName} (S${channelNumber(it.deviceNetworkId)})"
			    it.setLabel(newLabel)
            }
		}
		state.oldLabel = device.label
	}
    sendEvent(name: "checkInterval", value: 2 * 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    def cmds = update_needed_settings()
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    if (cmds != []) response(commands(cmds))
}

def on() { 
   commands([
        encap(zwave.basicV1.basicSet(value: 0xFF), 1),
        encap(zwave.basicV1.basicSet(value: 0xFF), 2)
    ])
}
def off() {
   commands([
        encap(zwave.basicV1.basicSet(value: 0x00), 1),
        encap(zwave.basicV1.basicSet(value: 0x00), 2)
    ])
}

void childOn(String dni) {
    logging("childOn($dni)")
    def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0xFF), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}

void childOff(String dni) {
    logging("childOff($dni)")
	def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.basicV1.basicSet(value: 0x00), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}

void childRefresh(String dni) {
    logging("childRefresh($dni)")
	def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 0), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 2), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}

void childReset(String dni) {
    logging("childReset($dni)")
	def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterReset(), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 0), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 2), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}

private encap(cmd, endpoint) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
	} else {
		cmd
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    state.sec = 1
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x32: 3, 0x25: 1, 0x98: 1, 0x70: 2, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x2B: 1]) // can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        switch(it.@type)
        {   
            case ["byte","short","four"]:
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
                    title: it.@label != "" ? "${it.@label}\n" + "${it.Help}" : "" + "${it.Help}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "paragraph":
               input title: "${it.@label}",
                    description: "${it.Help}",
                    type: "paragraph",
                    element: "paragraph"
            break
        }  
    }
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (convertParam(cmd.parameterNumber, settings."${cmd.parameterNumber}") == cmd2Integer(cmd.configurationValue))
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

def update_needed_settings()
{
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
     
    def configuration = parseXml(configuration_model())
    def isUpdateNeeded = "NO"
    
    sendEvent(name:"numberOfButtons", value:"5")
    
    if(!state.associationMC1) {
       logging("Adding MultiChannel association group 1")
       cmds << zwave.associationV2.associationRemove(groupingIdentifier: 1, nodeId: [])
       cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1])
       cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1)
    }
    if(state.association2){
       logging("Removing association group 2")
       cmds << zwave.associationV2.associationRemove(groupingIdentifier:2, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:2)
    }
    if(state.association4){
       logging("Removing association group 4")
       cmds << zwave.associationV2.associationRemove(groupingIdentifier:4, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:4)
    }
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave"){
            if (currentProperties."${it.@index}" == null)
            {
                isUpdateNeeded = "YES"
                logging("Current value of parameter ${it.@index} is unknown")
                cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
            }
            else if (settings."${it.@index}" != null && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), settings."${it.@index}"))
            { 
                isUpdateNeeded = "YES"

                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"))
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}")
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
	switch (number){
    	case 28:
            parValue = (value == "true" ? 1 : 0)
            parValue += (settings."fc_2" == "true" ? 2 : 0)
            parValue += (settings."fc_3" == "true" ? 4 : 0)
            parValue += (settings."fc_4" == "true" ? 8 : 0)
        break
        case 29:
            parValue = (value == "true" ? 1 : 0)
            parValue += (settings."sc_2" == "true" ? 2 : 0)
            parValue += (settings."sc_3" == "true" ? 4 : 0)
            parValue += (settings."sc_4" == "true" ? 8 : 0)
        break
        default:
        	parValue = value
        break
    }
    return parValue.toInteger()
}

private def logging(message) {
    if (state.enableDebugging == null || state.enableDebugging == "true") log.debug "$message"
}

/**
* Convert 1 and 2 bytes values to integer
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
    	def short value1   = value & 0xFF
        def short value2 = (value >> 8) & 0xFF
        [value2, value1]
    break
    case 3:
    	def short value1   = value & 0xFF
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

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = [0x31: 5, 0x30: 1, 0x9C: 1, 0x70: 2, 0x85: 2]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

private channelNumber(String dni) {
	dni.split("-ep")[-1] as Integer
}

private void createChildDevices() {
	state.oldLabel = device.label
     try {
        for (i in 1..2) {
	       addChildDevice("Metering Switch Child Device", "${device.deviceNetworkId}-ep${i}", null,
		      [completedSetup: true, label: "${device.displayName} (S${i})",
		      isComponent: false, componentName: "ep$i", componentLabel: "Switch $i"])
        }
    } catch (e) {
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

def configuration_model()
{
'''
<configuration>
    <Value type="list" byteSize="1" index="9" label="State of the device after a power failure" min="0" max="1" value="1" setting_type="zwave" fw="">
    <Help>
The device will return to the last state before power failure.
0 - the Dimmer 2 does not save the state before a power failure, it returns to the "off" position
1 - the Dimmer 2 restores its state before power failure
Range: 0~1
Default: 1 (Previous State)
    </Help>
        <Item label="Off" value="0" />
        <Item label="Previous State" value="1" />
  </Value>
      <Value type="list" byteSize="1" index="10" label="First Channel - Operating Mode" min="0" max="5" value="0" setting_type="zwave" fw="">
    <Help>
This parameter allows you to choose the operating mode for the 1st channel controlled by the S1 switch.
Range: 0~5
Default: 0 (Standard)
    </Help>
        <Item label="Standard" value="0" />
        <Item label="Delay On" value="1" />
        <Item label="Delay Off" value="2" />
        <Item label="Auto On" value="3" />
        <Item label="Auto Off" value="4" />
        <Item label="Flashing" value="5" />
  </Value>
        <Value type="list" byteSize="1" index="11" label="First Channel - Reaction For Delay/Auto" min="0" max="2" value="0" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device in timed mode reacts to pushing the switch connected to the S1 terminal.
Range: 0~2
Default: 0 (Cancel)
    </Help>
        <Item label="Cancel" value="0" />
        <Item label="No Reaction" value="1" />
        <Item label="Reset" value="2" />
  </Value>
      <Value type="byte" byteSize="2" index="12" label="First Channel - Time Parameter for Delay/Auto" min="0" max="32000" value="50" setting_type="zwave" fw="">
    <Help>
This parameter allows to set time parameter used in timed modes. 
Range: 0~32000 (0.1s, 1-32000s)
Default: 50
    </Help>
  </Value>
      <Value type="byte" byteSize="2" index="13" label="First Channel - Pulse Time For Flashing" min="1" max="32000" value="5" setting_type="zwave" fw="">
    <Help>
This parameter allows to set time of switching to opposite state in flashing mode.
Range: 1~32000 (0.1s-3200.0s)
Default: 5 (0.5s)
    </Help>
  </Value>
        <Value type="list" byteSize="1" index="15" label="Second Channel - Operating Mode" min="0" max="5" value="0" setting_type="zwave" fw="">
    <Help>
This parameter allows you to choose the operating mode for the 2nd channel controlled by the S2 switch.
Range: 0~5
Default: 0 (Standard)
    </Help>
        <Item label="Standard" value="0" />
        <Item label="Delay On" value="1" />
        <Item label="Delay Off" value="2" />
        <Item label="Auto On" value="3" />
        <Item label="Auto Off" value="4" />
        <Item label="Flashing" value="5" />
  </Value>
        <Value type="list" byteSize="1" index="16" label="Second Channel - Reaction For Delay/Auto" min="0" max="2" value="0" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device in timed mode reacts to pushing the switch connected to the S2 terminal.
Range: 0~2
Default: 0 (Cancel)
    </Help>
        <Item label="Cancel" value="0" />
        <Item label="No Reaction" value="1" />
        <Item label="Reset" value="2" />
  </Value>
      <Value type="byte" byteSize="2" index="17" label="Second Channel - Time Parameter for Delay/Auto" min="0" max="32000" value="50" setting_type="zwave" fw="">
    <Help>
This parameter allows to set time parameter used in timed modes. 
Range: 0~32000 (0.1s, 1-32000s)
Default: 50
    </Help>
  </Value>
      <Value type="byte" byteSize="2" index="18" label="Second Channel - Pulse Time For Flashing" min="1" max="32000" value="5" setting_type="zwave" fw="">
    <Help>
This parameter allows to set time of switching to opposite state in flashing mode.
Range: 1~32000 (0.1s-3200.0s)
Default: 5 (0.5s)
    </Help>
  </Value>
    <Value type="list" byteSize="1" index="20" label="Switch type" min="0" max="2" value="2" setting_type="zwave" fw="">
    <Help>
Choose between momentary and toggle switch.
Range: 0~2
Default: 2 (Toggle)
    </Help>
    <Item label="Momentary" value="0" />
    <Item label="Toggle (Open=On, Closed=Off)" value="1" />
    <Item label="Toggle (On Switch Change)" value="2" />
  </Value>
      <Value type="list" byteSize="1" index="40" label="Reaction to General Alarm" min="0" max="3" value="3" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to General Alarm frame.
Range: 0~3
Default: 3 (Flash)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
      <Value type="list" byteSize="1" index="41" label="Reaction to Flood Alarm" min="0" max="3" value="2" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to Flood Alarm frame.
Range: 0~3
Default: 2 (OFF)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
      <Value type="list" byteSize="1" index="42" label="Reaction to CO/CO2/Smoke Alarm" min="0" max="3" value="3" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to CO, CO2 or Smoke frame. 
Range: 0~3
Default: 3 (Flash)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
      <Value type="list" byteSize="1" index="43" label="Reaction to Heat Alarm" min="0" max="3" value="1" setting_type="zwave" fw="">
    <Help>
This parameter determines how the device will react to Heat Alarm frame.
Range: 0~3
Default: 1 (ON)
    </Help>
    <Item label="Alarm frame is ignored" value="0" />
    <Item label="Turn ON after receiving the alarm frame" value="1" />
    <Item label="Turn OFF after receiving the alarm frame" value="2" />
	<Item label="Flash after receiving the alarm frame" value="3" />
  </Value>
        <Value type="byte" byteSize="2" index="44" label="Flashing alarm duration" min="1" max="32000" value="600" setting_type="zwave" fw="">
    <Help>
This parameter allows to set duration of flashing alarm mode. 
Range: 1~32000 (1s-32000s)
Default: 600 (10 min)
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="50" label="First Channel - Active power reports" min="0" max="100" value="10" setting_type="zwave" fw="">
    <Help>
The parameter defines the power level change that will result in a new power report being sent. The value is a percentage of the previous report.
Range: 0~100 (1-100%)
Default: 10
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="51" label="First Channel - Periodic active power and energy reports" min="0" max="120" value="10" setting_type="zwave" fw="">
    <Help>
Parameter 51 defines a time period between consecutive reports. Timer is reset and counted from zero after each report. 
Range: 0~120 (1-120s)
Default: 10
    </Help>
  </Value>
    <Value type="short" byteSize="2" index="53" label="First Channel - Energy reports" min="0" max="32000" value="100" setting_type="zwave" fw="">
    <Help>
Energy level change which will result in sending a new energy report.
Range: 0~32000 (0.01-320 kWh)
Default: 100
    </Help>
  </Value>
      <Value type="byte" byteSize="1" index="54" label="Second Channel - Active power reports" min="0" max="100" value="10" setting_type="zwave" fw="">
    <Help>
The parameter defines the power level change that will result in a new power report being sent. The value is a percentage of the previous report.
Range: 0~100 (1-100%)
Default: 10
    </Help>
  </Value>
    <Value type="byte" byteSize="1" index="55" label="Second Channel - Periodic active power and energy reports" min="0" max="120" value="10" setting_type="zwave" fw="">
    <Help>
Parameter 55 defines a time period between consecutive reports. Timer is reset and counted from zero after each report. 
Range: 0~120 (1-120s)
Default: 10
    </Help>
  </Value>
    <Value type="short" byteSize="2" index="57" label="Second Channel - Energy reports" min="0" max="32000" value="100" setting_type="zwave" fw="">
    <Help>
Energy level change which will result in sending a new energy report.
Range: 0~32000 (0.01-320 kWh)
Default: 100
    </Help>
  </Value>
    <Value type="byte" byteSize="2" index="58" label="Periodic power reports" min="0" max="32000" value="3600" setting_type="zwave" fw="">
    <Help>
This parameter determines in what time interval the periodic power reports are sent to the main controller.
Range: 0~32000 (1-32000s)
Default: 3600
    </Help>
  </Value>
     <Value type="byte" byteSize="2" index="59" label="Periodic energy reports" min="0" max="32000" value="3600" setting_type="zwave" fw="">
    <Help>
This parameter determines in what time interval the periodic energy reports are sent to the main controller.
Range: 0~32000 (1-32000s)
Default: 3600
    </Help>
  </Value>
  <Value type="boolean" byteSize="1" index="28" label="First Channel" value="false" setting_type="zwave" fw="">
    <Help>
Send scene ID on single press 
    </Help>
  </Value>
  <Value type="boolean" byteSize="1" index="fc_2" label="" value="false" setting_type="" fw="">
    <Help>
Send scene ID on double press 
    </Help>
  </Value>
    <Value type="boolean" byteSize="1" index="fc_3" label="" value="false" setting_type="" fw="">
    <Help>
Send scene ID on tripple press 
    </Help>
  </Value>
    <Value type="boolean" byteSize="1" index="fc_4" label="" value="false" setting_type="" fw="">
    <Help>
Send scene ID on hold and release 
    </Help>
  </Value>
  <Value type="boolean" byteSize="1" index="29" label="Second Channel" value="false" setting_type="zwave" fw="">
    <Help>
Send scene ID on single press 
    </Help>
  </Value>
  <Value type="boolean" byteSize="1" index="sc_2" label="" value="false" setting_type="" fw="">
    <Help>
Send scene ID on double press 
    </Help>
  </Value>
    <Value type="boolean" byteSize="1" index="sc_3" label="" value="false" setting_type="" fw="">
    <Help>
Send scene ID on tripple press 
    </Help>
  </Value>
    <Value type="boolean" byteSize="1" index="sc_4" label="" value="false" setting_type="" fw="">
    <Help>
Send scene ID on hold and release 
    </Help>
  </Value>
    <Value type="paragraph" byteSize="1" index="mappings" label="Button Mappings" value="false" setting_type="" fw="">
    <Help>
Toggle Mode
1 pushed - S1 1x toggle
4 pushed - S1 2x toggle
5 pushed - S1 3x toggle

1 held - S2 1x toggle
4 held - S2 2x toggle
5 held - S2 3x toggle

Momentary Mode
1 pushed - S1 1x click
2 pushed - S1 release
3 pushed - S1 hold
4 pushed - S1 2x click
5 pushed - S1 3x click

1 held - S2 1x click
2 held - S2 release
3 held - S2 hold
4 held - S2 2x click
5 held - S2 3x click
    </Help>
  </Value>
    <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="">
    <Help>
    </Help>
  </Value>
</configuration>
'''
}