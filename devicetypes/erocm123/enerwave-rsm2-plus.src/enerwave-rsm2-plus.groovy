/**
 *
 *  Enerwave RSM2-Plus
 *   
 *	github: Eric Maycock (erocm123)
 *	Date: 2017-06-16
 *	Copyright Eric Maycock
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
 *  2017-06-16: Added support for firmware 5.11 in this handler.
 */
 
metadata {
   definition (name: "Enerwave RSM2-Plus", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch") {
      capability "Actuator"
      capability "Sensor"
      capability "Switch"
      capability "Polling"
      capability "Configuration"
      capability "Refresh"
      capability "Health Check"
      
      fingerprint mfr: "011A", prod: "0111", model: "0605", deviceJoinName: "Enerwave RSM2-Plus"

      fingerprint deviceId: "0x1001", inClusters:"0x5E,0x86,0x72,0x5A,0x73,0x20,0x27,0x25,0x32,0x60,0x85,0x8E,0x59,0x70", outClusters:"0x20"
      fingerprint deviceId: "0x1001", inClusters:"0x5E,0x86,0x72,0x5A,0x73,0x25,0x60,0x8E,0x85,0x59,0x27"
   }

   simulator {
   }
   
   preferences {
        input description: "Once you change values on this page, the corner of the \"configuration\" icon will change orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph"
		generate_preferences(configuration_model())  
   }

   tiles{
    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
               attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			   attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
			   attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			   attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"  
			}
	}
    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }
    standardTile("configure", "device.needUpdate", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "NO" , label:'', action:"configuration.configure", icon:"st.secondary.configure"
            state "YES", label:'', action:"configuration.configure", icon:"https://github.com/erocm123/SmartThingsPublic/raw/master/devicetypes/erocm123/qubino-flush-1d-relay.src/configure@2x.png"
    }

    main(["switch"])
    details(["switch",
             childDeviceTiles("all"),
             "refresh","configure","reset"
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

    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep=null)
{
    logging("BasicReport ${cmd} - ep ${ep}", 2)
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    logging("BasicSet ${cmd}", 2)
	def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def cmds = []
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
    cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
    return [result, response(commands(cmds))] // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null)
{
    logging("SwitchBinaryReport ${cmd} - ep ${ep}", 2)
    if (ep) {
        def event
        def childDevice = childDevices.find{it.deviceNetworkId == "$device.deviceNetworkId-ep$ep"}
        if (childDevice)
            childDevice.sendEvent(name: "switch", value: cmd.value ? "on" : "off")
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
        def result = createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
        def cmds = []
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 1)
        cmds << encap(zwave.switchBinaryV1.switchBinaryGet(), 2)
        return [result, response(commands(cmds))] // returns the result of reponse()
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   logging("MultiChannelCmdEncap ${cmd}", 2)
   def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
   if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
   }
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
   logging("ManufacturerSpecificReport ${cmd}", 2)
   def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
   logging("msr: $msr", 2)
   updateDataValue("MSR", msr)
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	def fw = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	updateDataValue("fw", fw)
	if (state.MSR == "003B-6341-5044") {
		updateDataValue("ver", "${cmd.applicationVersion >> 4}.${cmd.applicationVersion & 0xF}")
	}
	def text = "$device.displayName: firmware version: $fw, Z-Wave version: ${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}"
	createEvent(descriptionText: text, isStateChange: false)
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
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}

void childOff(String dni) {
    logging("childOff($dni)", 1)
	def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
}

void childRefresh(String dni) {
    logging("childRefresh($dni)", 1)
	def cmds = []
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.switchBinaryV1.switchBinaryGet(), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 0), channelNumber(dni))))
    cmds << new physicalgraph.device.HubAction(command(encap(zwave.meterV2.meterGet(scale: 2), channelNumber(dni))))
	sendHubCommand(cmds, 1000)
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

def updated()
{
    logging("updated()", 1)
    if (!childDevices) {
		createChildDevices()
	}
	else if (device.label != state.oldLabel) {
		childDevices.each {
            if (it.label == "${state.oldLabel} (R${channelNumber(it.deviceNetworkId)})") {
			    def newLabel = "${device.displayName} (R${channelNumber(it.deviceNetworkId)})"
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

def generate_preferences(configuration_model)
{
    def configuration = parseXml(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
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
    
    cmds << zwave.versionV1.versionGet()
    
    if (state.fw == "5.11") {
       if(state.association2){
           logging("Setting association group 2", 1)
           cmds << zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: [])
           cmds << zwave.associationV2.associationGet(groupingIdentifier:2)
        }
        if(state.association3){
           logging("Setting association group 3", 1)
           cmds << zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: [])
           cmds << zwave.associationV2.associationGet(groupingIdentifier:3)
        }
        if(!state.associationMC1) {
           logging("Adding MultiChannel association group 1", 1)
           cmds << zwave.associationV2.associationRemove(groupingIdentifier: 1, nodeId: [])
           cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1])
           cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1)
        }
    } else {           
       if(!state.association2){
          logging("Setting association group 2", 1)
          cmds << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId)
          cmds << zwave.associationV2.associationGet(groupingIdentifier:2)
       }
       if(!state.association3){
          logging("Setting association group 3", 1)
          cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId)
          cmds << zwave.associationV2.associationGet(groupingIdentifier:3)
       }
    }
    
    configuration.Value.each
    {     
        if ("${it.@setting_type}" == "zwave" && it.@disabled != "true"){
            if (currentProperties."${it.@index}" == null)
            {
               if (it.@setonly == "true"){
                  logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"), 2)
                  def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
                  cmds << zwave.configurationV1.configurationSet(configurationValue: integer2Cmd(convertedConfigurationValue, it.@byteSize.toInteger()), parameterNumber: it.@index.toInteger(), size: it.@byteSize.toInteger())
               } else {
                  isUpdateNeeded = "YES"
                  logging("Current value of parameter ${it.@index} is unknown", 2)
                  cmds << zwave.configurationV1.configurationGet(parameterNumber: it.@index.toInteger())
               }
            }
            else if ((settings."${it.@index}" != null || "${it.@type}" == "hidden") && cmd2Integer(currentProperties."${it.@index}") != convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"))
            { 
                isUpdateNeeded = "YES"
                logging("Parameter ${it.@index} will be updated to " + convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}"), 2)
                def convertedConfigurationValue = convertParam(it.@index.toInteger(), settings."${it.@index}"? settings."${it.@index}" : "${it.@value}")
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
    if (logLevel != "0"){
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
	       addChildDevice("Switch Child Device", "${device.deviceNetworkId}-ep${i}", null,
		      [completedSetup: true, label: "${device.displayName} (R${i})",
		      isComponent: false, componentName: "ep$i", componentLabel: "Relay $i"])
        }
    } catch (e) {
	    runIn(2, "sendAlert")
    }
}

private sendAlert() {
   sendEvent(
      descriptionText: "Child device creation failed. Please make sure that the \"Switch Child Device\" is installed and published.",
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
