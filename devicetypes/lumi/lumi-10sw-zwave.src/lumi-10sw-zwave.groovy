
metadata {
    definition (name: " Lumi 10SW Zwave", namespace: "lumi", author: "phuclm") {
    capability "Switch"
    capability "Polling"
    capability "Configuration"
    capability "Refresh"
    capability "Zw Multichannel"
          
    attribute "switch1", "string"
    attribute "switch2", "string"
    attribute "switch3", "string"
    attribute "switch4", "string"
    attribute "switch5", "string"
    attribute "switch6", "string"
    attribute "switch7", "string"
    attribute "switch8", "string"
    attribute "switch9", "string"
    attribute "switch10", "string"

    attribute "allSwitch", "string"  
    
    command "on1"
    command "off1"
    command "on2"
    command "off2"
    command "on3"
    command "off3"
    command "on4"
    command "off4"
    command "on5"
    command "off5"
    command "on6"
    command "off6"
    command "on7"
    command "off7"
    command "on8"
    command "off8"
    command "on9"
    command "off9"
    command "on10"
    command "off10"

    command "onAll"
    command "offAll"

    fingerprint mfr: "0293", prod: "0003", model: "001A"
}

    simulator {
        // TODO: define status and reply messages here
    }

    tiles {
        standardTile("switch1", "device.switch1", canChangeIcon: true) {
            state "on", label: "switch1", action: "off1", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch1", action: "on1", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch2", "device.switch2", canChangeIcon: true) {
            state "on", label: "switch2", action: "off2", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch2", action: "on2", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch3", "device.switch3", canChangeIcon: true) {
            state "on", label: "switch3", action: "off3", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch3", action: "on3", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch4", "device.switch4", canChangeIcon: true) {
            state "on", label: "switch4", action: "off4", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch4", action: "on4", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch5", "device.switch5", canChangeIcon: true) {
            state "on", label: "switch5", action: "off5", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch5", action: "on5", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch6", "device.switch6", canChangeIcon: true) {
            state "on", label: "switch6", action: "off6", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch6", action: "on6", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch7", "device.switch1", canChangeIcon: true) {
            state "on", label: "switch7", action: "off7", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch7", action: "on7", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch8", "device.switch8", canChangeIcon: true) {
            state "on", label: "switch8", action: "off8", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch8", action: "on8", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch9", "device.switch9", canChangeIcon: true) {
            state "on", label: "switch9", action: "off9", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch9", action: "on9", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
        standardTile("switch10", "device.switch10", canChangeIcon: true) {
            state "on", label: "switch10", action: "off10", icon: "st.switches.light.on", backgroundColor: "#79b821"
            state "off", label: "switch10", action: "on10", icon: "st.switches.light.off", backgroundColor: "#ffffff"
        }
                
        
        
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
            state "default", label:"", action:"configure", icon:"st.secondary.configure"
        }

        standardTile("onall", "device.allSwitch", canChangeIcon: true) {
            state "on", label: "on all", action: "onAll", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#ffffff"
        }
        standardTile("offall", "device.allSwitch", canChangeIcon: true) {
            state "off", label: "off all", action: "offAll", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff"
        }
        standardTile("allSwitch", "device.allSwitch", canChangeIcon: true) {
            state "on", label: "all", action: "offAll", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821"
            state "off", label: "all", action: "onAll", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#ffffff"
        }

        main(["allSwitch"])
        details(["switch1","switch2","switch3","switch4","switch5",
                "switch6","switch7","switch8","switch9","switch10",
                "onall","offall","allSwitch","refresh","configure"]
        )
    }
}

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, [0x60:3, 0x25:1, 0x70:1, 0x72:1])
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "Parsed ${description} to ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}

//Reports
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    createEvent([name: "allSwitch", value: cmd.value ? "on" : "off"])
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) 
{
    def map = [name: "switch$cmd.sourceEndPoint"]
   
    def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x20: 1])
   
    if (encapsulatedCommand && cmd.commandClass == 50) 
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
             createEvent(map)
             break
          case 37: // "0x25" - Switch Binary
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
    
    // check state to update allSwitch icon
    /* def milestone = state.sw1
    def isSame = true;
    for (i = 2; i < 11; i++)
    {
        if (state?.sw$i != milestone)
            isSame = false;
    }
    if (isSame == true)
        sendEvent([name:"allSwitch", value:milestone]) */
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
    log.debug("ManufacturerSpecificReport ${cmd.inspect()}")
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // Handles all Z-Wave commands we aren't interested in
    [:]
}

/*****************************************************************************************************/
// handle commands

def refresh() {
    delayBetween([
        log.debug("refreshing s1"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:2).format(),
        log.debug("refreshing s2"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:2, commandClass:37, command:2).format(),
        log.debug("refreshing s3"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:3, commandClass:37, command:2).format(),
        log.debug("refreshing s4"),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:4, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:5, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:6, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:7, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:8, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:9, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:10, commandClass:37, command:2).format()
    ])  
}

def poll() {
    log.debug "Executing 'poll'"
    delayBetween([
        //zwave.switchBinaryV1.switchBinaryGet().format(),
        // zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
    ])
}

def enableEpEvents(enabledEndpoints) {
    log.debug "Lumi4-enabledEndpoints"
    state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
    null
}

def configure() {
    log.debug "configure"
    
    // currently hard-coded to four button switch
    enableEpEvents("1,2,3,4,5,6,7,8,9,10")

    // not sure the association is needed
    // documentation says last group is automatically associated
    // with controller node id by default, will test
    //commands([
    //    //zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:3),
    //    zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId)
    //], 800)
}

def onAll() {
    log.debug("all on")
    zwave.basicV1.basicSet(value: 0xFF).format()
}

def offAll() {
    log.debug("all off")
    zwave.basicV1.basicSet(value: 0x00).format()
}

def on1() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format()
}
def off1() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format()
}

def on2() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format()
}
def off2() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format()
}

def on3() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:3, commandClass:37, command:1, parameter:[255]).format()
}
def off3() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:3, commandClass:37, command:1, parameter:[0]).format()
}

def on4() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:4, commandClass:37, command:1, parameter:[255]).format()
}
def off4() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:4, commandClass:37, command:1, parameter:[0]).format()
}

def on5() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:5, commandClass:37, command:1, parameter:[255]).format()
}
def off5() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:5, commandClass:37, command:1, parameter:[0]).format()
}

def on6() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:6, commandClass:37, command:1, parameter:[255]).format()
}
def off6() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:6, commandClass:37, command:1, parameter:[0]).format()
}

def on7() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:7, commandClass:37, command:1, parameter:[255]).format()
}
def off7() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:7, commandClass:37, command:1, parameter:[0]).format()
}

def on8() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:8, commandClass:37, command:1, parameter:[255]).format()
}
def off8() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:8, commandClass:37, command:1, parameter:[0]).format()
}

def on9() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:9, commandClass:37, command:1, parameter:[255]).format()
}
def off9() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:9, commandClass:37, command:1, parameter:[0]).format()
}

def on10() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:10, commandClass:37, command:1, parameter:[255]).format()
}
def off10() {
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:0, destinationEndPoint:11, commandClass:37, command:1, parameter:[0]).format()
}