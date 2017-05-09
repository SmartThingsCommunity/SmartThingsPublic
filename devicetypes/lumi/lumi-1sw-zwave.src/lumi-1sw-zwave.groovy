metadata {
    definition (name: " Lumi 1SW Zwave", namespace: "lumi", author: "phuclm") {
    capability "Switch"
    capability "Polling"
    capability "Configuration"
    capability "Refresh"
    capability "Zw Multichannel"

    fingerprint mfr: "0293", prod: "0003", model: "0011"
}

    tiles (scale:2) {
        standardTile("switch", "device.switch", width: 3, height: 3, canChangeIcon: true) {
            state "on", label: "SWITCH", action: "off", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "SW", action: "on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("on", "device.switch", width: 3, height: 3, canChangeIcon: true, decoration: "flat") {
            state "on", label: "ON", action: "on", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("off", "device.switch", width: 3, height: 3, canChangeIcon: true, decoration: "flat") {
            state "off", label: "OFF", action: "off", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 3, height: 3, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        main(["switch"])
        details(["switch","on","refresh","off"]
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
    
    createEvent(map)
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
    delayBetween(
        log.debug("refreshing s1"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:2).format()
    )
}

def poll() {
    log.debug "Executing 'poll'"
    delayBetween([
        zwave.switchBinaryV1.switchBinaryGet().format(),
        zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    ])
}

def enableEpEvents(enabledEndpoints) {
    state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
    null
}

def configure() {
    // currently hard-coded to four button switch
    enableEpEvents("1")
}

def on() {
	log.debug "on"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format()
}
def off() {
	log.debug "off"
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format()
}