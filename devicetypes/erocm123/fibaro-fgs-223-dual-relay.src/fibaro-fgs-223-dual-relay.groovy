/**
 *  Copyright 2016 Eric Maycock
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
 *  Date: 2016-10-25
 */
 
metadata {
definition (name: "Fibaro FGS-223 Dual Relay", namespace: "erocm123", author: "Eric Maycock") {
capability "Switch"
capability "Polling"
capability "Configuration"
capability "Refresh"
capability "Zw Multichannel"

attribute "switch1", "string"
attribute "switch2", "string"

command "on1"
command "off1"
command "on2"
command "off2"

fingerprint deviceId: "0x1001", inClusters:"0x86, 0x72, 0x85, 0x60, 0x8E, 0x25, 0x20, 0x70, 0x27"

}

simulator {
status "on": "command: 2003, payload: FF"
status "off": "command: 2003, payload: 00"

// reply messages
reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
reply "200100,delay 100,2502": "command: 2503, payload: 00"
}

tiles(scale: 2){

    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
	}
	standardTile("switch1", "device.switch1",canChangeIcon: true, width: 2, height: 2) {
		state "on", label: "switch1", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		state "off", label: "switch1", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    }
	standardTile("switch2", "device.switch2",canChangeIcon: true, width: 2, height: 2) {
		state "on", label: "switch2", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		state "off", label: "switch2", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    }
    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }

    valueTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state("NO" , label:'Synced', action:"configuration.configure", backgroundColor:"#8acb47")
            state("YES", label:'Pending', action:"configuration.configure", backgroundColor:"#f39c12")
    }

    main(["switch","switch1", "switch2"])
    details(["switch","switch1","switch2","refresh","configure"])
}
    preferences {
        
        input description: "Once you change values on this page, the \"Synced\" Status will become \"Pending\" status. When the parameters have been succesfully changed, the status will change back to \"Synced\"", displayDuringSetup: false, type: "paragraph", element: "paragraph"
        
		generate_preferences(configuration_model())
        
    }
}

def parse(String description) {
    def result = []
    def cmd = zwave.parse(description)
    if (cmd) {
        result += zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    log.debug "BasicReport $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    log.debug "BasicSet $cmd"
	sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2)
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2)
    response(secureSequence(result, 1000)) // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
    log.debug "SwitchBinaryReport $cmd"
    sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2)
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2)
    response(secureSequence(result, 1000)) // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
    log.debug "MeterReport $cmd"
    def result
    if (cmd.scale == 0) {
        result = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
    } else if (cmd.scale == 1) {
        result = createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
    } else {
        result = createEvent(name: "power", value: cmd.scaledMeterValue, unit: "W")
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) 
{
    log.debug "multichannelv3.MultiChannelCapabilityReport $cmd"
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
   log.debug "MultiChannelCmdEncap $cmd"
   def map = [ name: "switch$cmd.sourceEndPoint" ]
   if(cmd.parameter == [0] || cmd.parameter == [255]){
   switch(cmd.commandClass) {
      case 32:
         if (cmd.parameter == [0]) {
            map.value = "off"
         }
         if (cmd.parameter == [255]) {
            map.value = "on"
         }
         createEvent(map)
         break
      case 37:
         if (cmd.parameter == [0]) {
            map.value = "off"
         }
         if (cmd.parameter == [255]) {
            map.value = "on"
         }
         break
    }
    def events = [createEvent(map)]
    if (map.value == "on") {
            events += [createEvent([name: "switch", value: "on"])]
    } else {
         def allOff = true
         (1..2).each { n ->
             if (n != cmd.sourceEndPoint) {
                 if (device.currentState("switch${n}").value != "off") allOff = false
             }
         }
         if (allOff) {
             events += [createEvent([name: "switch", value: "off"])]
         }
    }
    events
    }
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	log.debug "AssociationReport $cmd"
    state."association${cmd.groupingIdentifier}" = cmd.nodeId[0]
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

def refresh() {
	def cmds = []
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2)
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2)
	secureSequence(cmds, 1000)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
    updateDataValue("MSR", msr)
}

def poll() {
	def cmds = []
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2)
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2)
	secureSequence(cmds, 1000)
}

def configure() {
	state.enableDebugging = settings.enableDebugging
    logging("Configuring Device For SmartThings Use")
    def cmds = []

    cmds = update_needed_settings()
    
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
    
    if (cmds != []) commands(cmds)
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
    logging("SceneActivationSet: $cmd")
    logging("sceneId: $cmd.sceneId")
    logging("dimmingDuration: $cmd.dimmingDuration")
    logging("Configuration for preference \"Switch Type\" is set to ${settings."20"}")
    
    if (settings."20" == "2") {
        logging("Switch configured as Roller blinds")
        switch (cmd.sceneId) {
            // Roller blinds S1
            case 10: // Turn On (1x click)
                buttonEvent(1, "pushed")
            break
            case 13: // Release
                buttonEvent(1, "held")
            break
            case 14: // 2x click
                buttonEvent(2, "pushed")
            break
            case 17: // Brightening
                buttonEvent(2, "held")
            break
            // Roller blinds S2
            case 11: // Turn Off
                buttonEvent(3, "pushed")
            break
            case 13: // Release
                buttonEvent(3, "held")
            break
            case 14: // 2x click
                buttonEvent(4, "pushed")
            break
            case 15: // 3x click
                buttonEvent(5, "pushed")
            break
            case 18: // Dimming
                buttonEvent(4, "held")
            break
            default:
                logging("Unhandled SceneActivationSet: ${cmd}")
            break
        }
    } else if (settings."20" == "1") {
        logging("Switch configured as Toggle")
        switch (cmd.sceneId) {
            // Toggle S1
            case 10: // Off to On
                buttonEvent(1, "pushed")
            break
            case 11: // On to Off
                buttonEvent(1, "held")
            break
            case 14: // 2x click
                buttonEvent(2, "pushed")
            break
            // Toggle S2
            case 20: // Off to On
                buttonEvent(3, "pushed")
            break
            case 21: // On to Off
                buttonEvent(3, "held")
            break
            case 24: // 2x click
                buttonEvent(4, "pushed")
            break
            case 25: // 3x click
                buttonEvent(5, "pushed")
            break
            default:
                logging("Unhandled SceneActivationSet: ${cmd}")
            break
        
        }
    } else {
        if (settings."20" == "0") logging("Switch configured as Momentary") else logging("Switch type not configured") 
        switch (cmd.sceneId) {
            // Momentary S1
            case 16: // 1x click
                buttonEvent(1, "pushed")
            break
            case 14: // 2x click
                buttonEvent(2, "pushed")
            break
            case 12: // held
                buttonEvent(1, "held")
            break
            case 13: // release
                buttonEvent(2, "held")
            break
            // Momentary S2
            case 26: // 1x click
                buttonEvent(3, "pushed")
            break
            case 24: // 2x click
                buttonEvent(4, "pushed")
            break
            case 25: // 3x click
                buttonEvent(5, "pushed")
            break
            case 22: // held
                buttonEvent(3, "held")
            break
            case 23: // release
                buttonEvent(4, "held")
            break
            default:
                logging("Unhandled SceneActivationSet: ${cmd}")
            break
        }
    }  
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
    log.debug convertParam(28, settings."${28}")
	state.enableDebugging = settings.enableDebugging
    logging("updated() is being called")
    
    def cmds = update_needed_settings()
    
    sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: true)
    
    if (cmds != []) response(commands(cmds))
}

def on() { 
   secureSequence([
        zwave.switchAllV1.switchAllOn(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2)
    ], 1000)
}
def off() {
   secureSequence([
        zwave.switchAllV1.switchAllOff(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2)
    ], 1000)
}

def on1() {
    secureSequence([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2)
    ], 1000)
}

def off1() {
    secureSequence([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2)
    ], 1000)
}

def on2() {
    secureSequence([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2)
    ], 1000)
}

def off2() {
    secureSequence([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2)
    ], 1000)
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x32: 1, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1]) // can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1500) {
	delayBetween(commands.collect{ command(it) }, delay)
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
        }  
    }
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    
    currentProperties."${cmd.parameterNumber}" = cmd.configurationValue

    if (settings."${cmd.parameterNumber}" != null)
    {
        if (settings."${cmd.parameterNumber}".toInteger() == convertParam("${cmd.parameterNumber}".toInteger(), cmd2Integer(cmd.configurationValue)))
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
    <Value type="list" byteSize="1" index="20" label="Switch type" min="0" max="2" value="0" setting_type="zwave" fw="">
    <Help>
Choose between momentary and toggle switch.
Range: 0~2
Default: 0 (Momentary)
    </Help>
    <Item label="Momentary" value="0" />
    <Item label="Toggle" value="1" />
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
    <Value type="boolean" index="enableDebugging" label="Enable Debug Logging?" value="true" setting_type="preference" fw="">
    <Help>
    </Help>
  </Value>
</configuration>
'''
}