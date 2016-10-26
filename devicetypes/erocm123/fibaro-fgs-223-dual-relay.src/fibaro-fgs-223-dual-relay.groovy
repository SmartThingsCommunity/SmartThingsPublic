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

    standardTile("configure", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"configure", icon:"st.secondary.configure"
    }

    main(["switch","switch1", "switch2"])
    details(["switch","switch1","switch2","refresh","configure"])
}
    /* Waiting to hear back from manufacturer regarding what preferences can be set for this device. 
	preferences {
        input "switchType", "enum", title: "Switch Type", defaultValue: "1", displayDuringSetup: true, required: false, options: [
                "1":"Toggle",
                "2":"Momentary"]
  }*/
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
	log.debug "configure() called"
    def cmds = []
    if(!state.association4 || state.association4 == "" || state.association4 != "1"){
       log.debug "Setting association group 4"
       cmds << zwave.associationV2.associationSet(groupingIdentifier:4, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:4)
    }
    if(!state.association5 || state.association5 == "" || state.association5 != "1"){
       log.debug "Setting association group 5"
       cmds << zwave.associationV2.associationSet(groupingIdentifier:5, nodeId:zwaveHubNodeId)
       cmds << zwave.associationV2.associationGet(groupingIdentifier:5)
    }
    if (switchType != null && switchType.value != null) {
    switch (switchType.value as String) {
       case "1":
       log.debug "Configuring device as a Toggle switch"
       //cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [3]).format()	
       //cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
       break
       case "2":
       log.debug "Configuring device as a Momentary switch"
       //cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, configurationValue: [1]).format()	
       //cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
       break
       default:
       log.debug "No valid device type chosen"
       break
    }
    }
    
    if ( cmds != [] && cmds != null ) return secureSequence(cmds, 2000) else return
}
/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
	log.debug "Preferences have been changed. Attempting configure()"
    def cmds = configure()
    response(cmds)
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