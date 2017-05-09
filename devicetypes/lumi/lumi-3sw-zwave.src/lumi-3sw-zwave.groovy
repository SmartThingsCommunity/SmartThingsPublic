metadata {
    definition (name: " Lumi 3SW Zwave", namespace: "lumi", author: "phuclm") {
    capability "Switch"
    capability "Polling"
    capability "Configuration"
    capability "Refresh"
    capability "Zw Multichannel"
          
    attribute "switch1", "string"
    attribute "switch2", "string"
    attribute "switch3", "string"

    attribute "allSwitch", "string"  
    
    command "on1"
    command "off1"
    command "on2"
    command "off2"
    command "on3"
    command "off3"

    command "onAll"
    command "offAll"

    fingerprint mfr: "0293", prod: "0003", model: "0013"
}

    tiles (scale:2) {
        standardTile("switch1", "device.switch1", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: "SW1", action: "off1", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "SW1", action: "on1", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch2", "device.switch2", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: "SW2", action: "off2", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "SW2", action: "on2", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch3", "device.switch3", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: "SW3", action: "off3", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "SW3", action: "on3", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        standardTile("onall", "device.allSwitch", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
            state "on", label: "On All", action: "onAll", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#ffffff"
        }
        standardTile("offall", "device.allSwitch", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
            state "off", label: "Off All", action: "offAll", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff"
        }
        standardTile("allSwitch", "device.allSwitch", width: 2, height: 2, canChangeIcon: true) {
            state "hasOn", label: "Has On", action: "offAll", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState: "allOff"
            state "allIsOff", label: "All Off", action: "onAll", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff", nextstate: "hasOn"
        }

        main(["allSwitch"])
        details(["switch1","switch2","switch3","onall","offall", "allSwitch", "refresh"]
        )
    }
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, [0x60:3, 0x25:1, 0x20:1, 0x70:1, 0x72:1])
    if (cmd) {
        result = zwaveEvent(cmd)
        //log.debug "Parsed ${description} to ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}

//Reports
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
   log.debug "zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)"
   // createEvent([name: "allSwitch", value: cmd.value ? "on" : "off"])
}

def allSwitchIsOff()
{
	log.debug "Checking switchs state: ${state}"
    if (state.switchsState.findAll{key, value->value.contains('bat')})
    	return false
    return true
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) 
{
    def map = [name: "switch$cmd.sourceEndPoint"]
   
    def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x20: 1])
   
    if (encapsulatedCommand && cmd.commandClass == 0x32) 
    {
        log.debug "Commandclass is Meter!"  
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
    } 
    else
    {
       switch(cmd.commandClass) {
          case 32: // "0x20" - Basic
             if (cmd.parameter == [0]) {
                map.value = "off"
             }
             if (cmd.parameter == [255]) {
                map.value = "on"
             }
             break
          case 37: // "0x25" - Switch Binary
             if (cmd.parameter == [0]) {
                map.value = "off"
             }
             if (cmd.parameter == [255]) {
                map.value = "on"
             }
             break
        }
    }
    
    log.info "Current switchs state: ${map}"
    
    // store switchs state
    state.switchsState["sw$cmd.sourceEndPoint"] = map.value == "on" ? "bat" : "tat" 
    def evt1 = createEvent(map)
    
    def evt2
   	if (allSwitchIsOff()) {
    	evt2 = createEvent([name: "allSwitch", value:"allIsOff"])
    }
    else {
    	evt2 = createEvent([name: "allSwitch", value:"hasOn"])
    }
    log.info "evt2: ${evt2}"
    
    return [evt1, evt2]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
    log.debug("ManufacturerSpecificReport ${cmd.inspect()}")
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "zwaveEvent(physicalgraph.zwave.Command cmd)"
    // Handles all Z-Wave commands we aren't interested in
    [:]
}

/*****************************************************************************************************/
// handle commands
def refresh() {
	// Global map use for save state of switchs
    state.switchsState = [sw1:"bat", sw2:"bat", sw3:"bat"]
    
    delayBetween([
        log.debug("refreshing s1"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:2).format(),
        log.debug("refreshing s2"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:2, commandClass:37, command:2).format(),
        log.debug("refreshing s3"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:3, commandClass:37, command:2).format()
    ])  
}

def poll() {
    log.debug "Executing 'poll'"
    delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    ])
}

def enableEpEvents(enabledEndpoints) {
    log.debug "Lumi4-enabledEndpoints"
    state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
    null
}

def configure() {
	// Global map use for save state of switchs
    state.switchsState = [sw1:"bat", sw2:"bat", sw3:"bat"]
    
    log.debug "configure"
    
    // currently hard-coded to four button switch
    enableEpEvents("1,2,3")
}

def onAll() {
    log.debug "On All"
    zwave.basicV1.basicSet(value: 0xFF).format()
}

def offAll() {
    log.debug "Off All"
    zwave.basicV1.basicSet(value: 0x00).format()
}

def on1() {
	log.debug "on1"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format()
}
def off1() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format()
}

def on2() {
	log.debug "on2"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format()
}
def off2() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format()
}

def on3() {
	log.debug "on3"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:3, commandClass:37, command:1, parameter:[255]).format()
}
def off3() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:3, commandClass:37, command:1, parameter:[0]).format()
}