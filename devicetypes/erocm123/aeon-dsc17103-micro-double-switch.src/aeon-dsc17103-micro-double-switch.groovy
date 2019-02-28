/**
 *  
 *	Aeon DSC17103 Micro Double Switch
 *  
 *	Author: Eric Maycock (erocm123)
 *	email: erocmail@gmail.com
 *	Date: 2016-07-06
 *  Copyright Eric Maycock
 * 	 
 *	Device Type supports all the feautres of the DSC17103 device including both switches, 
 *	current energy consumption in W and cumulative energy consumption in kWh.
 */
 
metadata {
definition (name: "Aeon DSC17103 Micro Double Switch", namespace: "erocm123", author: "Eric Maycock", vid:"generic-switch-power-energy") {
capability "Switch"
capability "Polling"
capability "Configuration"
capability "Refresh"
capability "Energy Meter"
capability "Power Meter"
capability "Health Check"

attribute "switch1", "string"
attribute "switch2", "string"
attribute "power1", "number"
attribute "energy1", "number"
attribute "power2", "number"
attribute "energy2", "number"

command "on1"
command "off1"
command "on2"
command "off2"
command "reset"

fingerprint mfr: "0086", prod: "0003", model: "0011"
fingerprint deviceId: "0x1001", inClusters:"0x5E, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x25, 0x20, 0x27, 0x71, 0x2B, 0x2C, 0x75, 0x7A, 0x60, 0x32, 0x70"
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
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
            tileAttribute ("statusText", key: "SECONDARY_CONTROL") {
           		attributeState "statusText", label:'${currentValue}'       		
            }
	}
	standardTile("switch1", "device.switch1",canChangeIcon: true, width: 2, height: 2) {
		state "on", label: "switch1", action: "off1", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		state "off", label: "switch1", action: "on1", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    }
	standardTile("switch2", "device.switch2",canChangeIcon: true, width: 2, height: 2) {
		state "on", label: "switch2", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		state "off", label: "switch2", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
    }
    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }

    standardTile("configure", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"configure", icon:"st.secondary.configure"
    }
    valueTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:'reset kWh', action:"reset"
	}
    valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
	}
    valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
	}
    valueTile("energy1", "device.energy1", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
	}
    valueTile("power1", "device.power1", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
	}
    valueTile("energy2", "device.energy2", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
	}
    valueTile("power2", "device.power2", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
	}

    main(["switch","switch1", "switch2"])
    details(["switch", 
             "switch1", "switch2", "refresh",
             "power", "energy", "reset",
             "configure"])
}
	preferences {
        input name: "parameter120", type: "enum", title: "Switch Type", defaultValue: "3", displayDuringSetup: true, required: false, options: [
                "0":"Momentary",
                "1":"Toggle"]
        input name: "parameter111", type: "number", title: "Power Meter Report (in seconds)", displayDuringSetup: false, required: false
        input name: "parameter112", type: "number", title: "Energy Meter Report (in seconds)", displayDuringSetup: false, required: false
        input name: "parameter90", type: "boolean", title: "Enable Selective Reporting?", description: "Only send energy report if below conditions are true.", displayDuringSetup: false, required: false
        input name: "parameter91", type: "number", title: "Minimum W change in Wattage for report to be sent", displayDuringSetup: false, required: false
        input name: "parameter92", type: "number", title: "Minimum % Change in Wattage for report to be sent", displayDuringSetup: false, required: false
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
    
    def statusTextmsg = ""
    if (device.currentState('power') && device.currentState('energy')) statusTextmsg = "${device.currentState('power').value} W ${device.currentState('energy').value} kWh"
    sendEvent(name:"statusText", value:statusTextmsg, displayed:false)
    
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
   log.debug "BasicReport ${cmd}"
   /*def map = [ name: "switch" ]

   switch(cmd.value) {
      case 0:
         map.value = "off"
         createEvent(map)
         break
      case 255:
         map.value = "on"
         createEvent(map)
         break
    }*/
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    //result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    response(delayBetween(result, 1000)) // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
    sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    //result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    response(delayBetween(result, 1000)) // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
    def result
    def eName
    def pName
    def cmds = []
    if (ep) {
       eName = "energy${ep}"
       pName = "power${ep}"
    } else {
       eName = "energy"
       pName = "power"
       (1..2).each { endpoint ->
			cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
            cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
	   }
    }
    if (cmd.scale == 0) {
        result = createEvent(name: eName, value: cmd.scaledMeterValue, unit: "kWh")
    } else if (cmd.scale == 1) {
        result = createEvent(name: eName, value: cmd.scaledMeterValue, unit: "kVAh")
    } else {
        result = createEvent(name: pName, value: cmd.scaledMeterValue, unit: "W")
    }
    cmds ? [result, response(delayBetween(cmds, 1000))] : result
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

/*def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   def map = [ name: "switch$cmd.sourceEndPoint" ]
   
   def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
   if (encapsulatedCommand && cmd.commandClass == 50) {
      zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
   } else {
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
         createEvent(map)
         break
    }
    }
}*/

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   def map = [ name: "switch$cmd.sourceEndPoint" ]
    
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


def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd2Integer(cmd.configurationValue)}'"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def refresh() {
	def cmds = []
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    (1..2).each { endpoint ->
			cmds << encap(zwave.meterV2.meterGet(scale: 0), endpoint)
            cmds << encap(zwave.meterV2.meterGet(scale: 2), endpoint)
	}
	delayBetween(cmds, 1000)
}

def ping() {
    log.debug "ping()"
	refresh()
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
    updateDataValue("MSR", msr)
}

def poll() {
	def cmds = []
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
	delayBetween(cmds, 1000)
}

def reset() {
    delayBetween([
        zwave.meterV2.meterReset().format(),
        zwave.meterV2.meterGet().format()
    ], 1000)
}

def configure() {
	log.debug "configure() called"
    sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4).format()
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8).format()
    cmds << zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: 2).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 101).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 102).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 80).format()
    if (settings."parameter90" != null && settings."parameter90" == "true") {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 90, scaledConfigurationValue: 1).format()
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 90).format()
        if (settings."parameter91" != null)
            cmds << zwave.configurationV1.configurationSet(parameterNumber: 91, size: 2, scaledConfigurationValue: settings."parameter91".toInteger()).format()
            cmds << zwave.configurationV1.configurationGet(parameterNumber: 91).format()
        if (settings."parameter92" != null)
            cmds << zwave.configurationV1.configurationSet(parameterNumber: 92, size: 1, scaledConfigurationValue: settings."parameter92".toInteger()).format()
            cmds << zwave.configurationV1.configurationGet(parameterNumber: 92).format()
    } else {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 90, scaledConfigurationValue: 0).format()
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 90).format()
    }
    
    if ( settings."parameter111" != null ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: (settings."parameter111".toInteger())).format()	// Set switch to report values for both Relay1 and Relay2
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 111).format()
    }
    if ( settings."parameter112" != null ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: (settings."parameter112".toInteger())).format()	// Set switch to report values for both Relay1 and Relay2
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 112).format()
    }
    if ( settings."parameter120" != null ) {
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 120, size: 1, scaledConfigurationValue: (settings."parameter120".toInteger())).format()	// Set switch to report values for both Relay1 and Relay2
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 120).format()
    }
    
    
    if ( cmds != [] && cmds != null ) return delayBetween(cmds, 1000) else return
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
   delayBetween([
        zwave.switchAllV1.switchAllOn().format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 1000)
}
def off() {
   delayBetween([
        zwave.switchAllV1.switchAllOff().format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 1000)
}

def on1() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format(),
        //zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ], 1000)
}

def off1() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format(),
        //zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ], 1000)
}

def on2() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
        //zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 1000)
}

def off2() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
        //zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 1000)
}

private encap(cmd, endpoint) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private valueCheck(number, value) {
    switch (number) {
       case 1:
          return value / 5
       break
       case 2:
          return value / 10
       break
       case 4:
          return value
       break
       default:
          return value
       break
    }
}

def cmd2Integer(array) { 
switch(array.size()) {
	case 1:
		array[0]
    break
	case 2:
    	((array[0] & 0xFF) << 8) | (array[1] & 0xFF)
    break
	case 4:
    	((array[0] & 0xFF) << 24) | ((array[1] & 0xFF) << 16) | ((array[2] & 0xFF) << 8) | (array[3] & 0xFF)
	break
}
}
