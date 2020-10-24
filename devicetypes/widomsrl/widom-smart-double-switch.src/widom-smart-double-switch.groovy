/**
 *  WiDomSmart Double Switch
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
    definition (name: "WiDom Smart Double Switch", namespace: "WiDomsrl", author: "WiDomsrl", vid: "generic-switch") {
        capability "Switch"
        //capability "Energy Meter"
        capability "Power Meter"
        //capability "Button"
        capability "Configuration"
        //capability "Health Check"

        command "reset"
        command "refresh"

        fingerprint mfr: "0149", prod: "1214", model: "0B00"
        //fingerprint type: "1001", cc: "5E,25,85,8E,59,55,86,72,5A,73,70,98,9F,6C,7A"
    	//fingerprint deviceId:"0x0B00"//, inClusters: "0x5E,0x25, 0x32, 0x60, 0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x70,0x98,0x9F,0x6C,0x7A"
        
    
	}


    tiles (scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: "off", action: "switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "on", label: "on", action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
            	state "turningOn", label:'Turning on', icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState: "turningOff"
            	state "turningOff", label:'Turning off', icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState: "turningOn"
            }
            
            tileAttribute("device.deviceID", key:"SECONDARY_CONTROL") {
                attributeState("deviceID", label:'(ID: ${currentValue}) - Channel 1')
            }
//            tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
//                attributeState("multiStatus", label:'${currentValue}')
//            }
        }
        valueTile("multiStatus", "device.multiStatus", decoration: "flat", width: 6, height: 2) {
            state "multiStatus", label:'${currentValue}'
        }

        valueTile("power", "device.power", decoration: "flat", width: 6, height: 2) {
            state "power", label:'${currentValue}\n W'
        }

        standardTile("main", "device.switch", decoration: "flat", canChangeIcon: true) {
            state "off", label: 'off', action: "switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            state "on", label: 'on', action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
        }
        main "main"
//        details(["switch","power","energy","reset"])
//        details(["switch","power",childDeviceTiles("all")])
		details(["switch","power", "multiStatus"])
    }

    preferences {
        input (
                title: "WiDom Smart Double Switch",
                description: "Tap to view the manual.",
                image: "https://www.widom.it/wp-content/uploads/2019/03/widom-3d-smart-double-switch.gif",
                url: "https://www.widom.it/wp-content/uploads/2020/03/Widom-Smart-Double-Switch_EN.pdf",
                type: "href",
                element: "href"
        )

        parameterMap().each {
            input (
                    title: "${it.num}. ${it.title}",
                    description: it.descr,
                    type: "paragraph",
                    element: "paragraph"
            )

            input (
                    name: it.key,
                    title: null,
                    //description: "Default: ${it.def}" ,
                    type: it.type,
                    options: it.options,
                    defaultValue: it.def,
                    required: false
            )
        }
        //input ( name: "logging", title: "Logging", type: "boolean", required: false )
        
		section("CONFIGURE AN ASSOCIATION GROUP") { // CONFIGURE AN ASSOCIATION GROUP:
        
           input (
                    type: "paragraph",
                    element: "paragraph",
                    title: "CONFIGURE ASSOCIATION GROUP:",
                    description: "Association Group Members:\n" +
                           "Enter a comma-delimited list of destinations (node IDs and/or endpoint IDs). " +
                           "All IDs must be in hexadecimal format. E.g.:\n" +
                           "Node destinations: '11, 0F'\n" +
                           "Endpoint destinations: '1C:1, 1C:2'\n" +
                           "Important: To delete previous configurations you must enter 0 "
                )
            groupMap().each {
                input (
                    name: it.key,
                    title: "${it.title} (${it.descr})",
                    type: it.type,
                    defaultValue: it.def,
                    required: false
                )
            }
		}
    }
}

def on() {
    encap(zwave.basicV1.basicSet(value: 255),1)
}

def off() {
    encap(zwave.basicV1.basicSet(value: 0),1)
}

def childOn() {
    sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 255),2)))
}

def childOff() {
    sendHubCommand(response(encap(zwave.basicV1.basicSet(value: 0),2)))
}


def setState(String key, Integer value) {
    state."$key".value = value
}

Integer getState(String key) {
    state."$key".value
}

//Configuration and synchronization

def updated() {
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    def cmds = []
    logging("${device.displayName} - Executing updated()","info")
    if (!childDevices) {
        createChildDevices()
    }
//	groupMap().each {
//    logging("Value ${settings."$it.key"}")
//    state."$it.key" = [value: null, state: "synced"]
//    	state."$it.key".value = settings."$it.key";
//        logging("Value ${state."$it.key".value}")
//    }
    runIn(3,"syncStart")
    state.lastUpdated = now()
    response(encapSequence(cmds,1000))
}

private syncStart() {
    boolean syncNeeded = false
    boolean syncNeededGroup = false
    parameterMap().each {
        if(settings."$it.key" != null) {
            if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
            if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state in ["notSynced","inProgress"]) {
                state."$it.key".value = settings."$it.key" as Integer
                state."$it.key".state = "notSynced"
                syncNeeded = true
            }
        }
    }
    groupMap().each {
        if(settings."$it.key" != null) {
            if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
            if (state."$it.key".value != settings."$it.key"|| state."$it.key".state in ["notSynced","inProgress"]) {
                state."$it.key".value = settings."$it.key"
                state."$it.key".state = "notSynced"
                syncNeededGroup = true
            }
        }
    }
    if ( syncNeeded ) {
        logging("${device.displayName} - starting sync.", "info")
        multiStatusEvent("Sync in progress.", true, true)
        syncNext()
    }
    if ( syncNeededGroup ) {
        logging("${device.displayName} - starting sync.", "info")
        multiStatusEvent("Sync in progress.", true, true)
        syncNext()
    }
}

private syncNext() {
    logging("${device.displayName} - Executing syncNext()","info")
    def cmds = []
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
            multiStatusEvent("Sync in progress. (param: ${param.num})", true)
            state."$param.key"?.state = "inProgress"
            cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
            cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
            break
        }
    }
    for(group in groupMap()){
       if (state."$group.key"?.value != null && state."$group.key"?.state in ["notSynced","inProgress"] ) {
            multiStatusEvent("Sync in progress. (group: ${group.id})", true)
                state.zwtAssocGroupTarget = [
                    id: group.id,
                    nodes: parseAssocGroupInput(settings."$group.key",8)
                ]
            logging("sync(): Syncing Association Group #${state.zwtAssocGroupTarget.id} using Multi-Channel Association commands. New Destinations: ${state.zwtAssocGroupTarget.nodes}","info")
            if(state.zwtAssocGroupTarget.nodes != []){
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: state.zwtAssocGroupTarget.id, nodeId: []) // Remove All
                logging("Node to insert: ${state.zwtAssocGroupTarget.nodes}","warn")
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: state.zwtAssocGroupTarget.id, nodeId: state.zwtAssocGroupTarget.nodes)
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
                //break
            } else{
            	logging("Resetting group configuration to default ","warn")
                //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
            	cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: state.zwtAssocGroupTarget.id, nodeId: []) // Remove All
                cmds << zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: state.zwtAssocGroupTarget.id)
            }
        } 
    }
    if (cmds) {
        runIn(10, "syncCheck")
        logging("cmds!")
        sendHubCommand(cmds,1000)
    } else {
        runIn(1, "syncCheck")
    }
}

private syncCheck() {
    logging("${device.displayName} - Executing syncCheck()","info")
    def failed = []
    def incorrect = []
    def notSynced = []
    parameterMap().each {
        if (state."$it.key"?.state == "incorrect" ) {
            incorrect << it
        } else if ( state."$it.key"?.state == "failed" ) {
            failed << it
        } else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
            notSynced << it
        }
    }
    groupMap().each {
        if (state."$it.key"?.state == "incorrect" ) {
            incorrect << it
        } else if ( state."$it.key"?.state == "failed" ) {
            failed << it
        } else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
            notSynced << it
        }
    }
    if (failed) {
        logging("${device.displayName} - Sync failed! Check parameter: ${failed[0].num}","info")
        sendEvent(name: "syncStatus", value: "failed")
        multiStatusEvent("Sync failed! Check parameter: ${failed[0].num}", true, true)
    } else if (incorrect) {
        logging("${device.displayName} - Sync mismatch! Check parameter: ${incorrect[0].num}","info")
        sendEvent(name: "syncStatus", value: "incomplete")
        multiStatusEvent("Sync mismatch! Check parameter: ${incorrect[0].num}", true, true)
    } else if (notSynced) {
        logging("${device.displayName} - Sync incomplete!","info")
        sendEvent(name: "syncStatus", value: "incomplete")
        multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
    } else {
        logging("${device.displayName} - Sync Complete","info")
        sendEvent(name: "syncStatus", value: "synced")
        multiStatusEvent("Sync OK.", true, true)
    }
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
    if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
        sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
    }
}

private deviceIdEvent(String value, boolean force = false, boolean display = false) {
        sendEvent(name: "deviceID", value: value, descriptionText: value, displayed: display)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
    logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
    state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
    syncNext()
}

private createChildDevices() {
    logging("${device.displayName} - executing createChildDevices()","info")
    addChildDevice(
            "Switch Child Device",
            "${device.deviceNetworkId}-2",
            null,
            [completedSetup: true, label: "${device.displayName} (CH2)", isComponent: false, componentName: "ch2", componentLabel: "Channel 2"]
    )
}

private physicalgraph.app.ChildDeviceWrapper getChild(Integer childNum) {
    return childDevices.find({ it.deviceNetworkId == "${device.deviceNetworkId}-${childNum}" })
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    logging("${device.displayName} - rejected request!","warn")
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.state == "inProgress" ) {
            state."$param.key"?.state = "failed"
            break
        }
    }
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    //ignore
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, ep=null) {
    logging("${device.displayName} - SwitchBinaryReport received, value: ${cmd.value} ep: $ep","info")
    switch (ep) {
        case 1:
            sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
            break
        case 2:
            getChild(2)?.sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
            break
    }
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep=null) {
    logging("${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale} ep: $ep","info")
    if (ep==1) {
        switch (cmd.scale) {
            case 0:
                sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
                break
            case 2:
                sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"])
                break
        }

    } else if (ep==2) {
        switch (cmd.scale) {
            case 0:
                getChild(2)?.sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
                break
            case 2:
                getChild(2)?.sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"])
                break
        }
    }
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {
    def result = []
    def deviceId = [];
    logging("${device.displayName} - Parsing: ${description}")
    if (description.startsWith("Err 106")) {
        result = createEvent(
                descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
                eventType: "ALERT",
                name: "secureInclusion",
                value: "failed",
                displayed: true,
        )
    } else if (description == "updated") {
        return null
    } else {
    	deviceId = description.split(", ")[0]
        deviceId = deviceId.split(":")[1]
        logging("deviceId- ${deviceId}")
        deviceIdEvent(deviceId, true, true)
        def cmd = zwave.parse(description, cmdVersions())
        if (cmd) {
            logging("${device.displayName} - Parsed: ${cmd}")
            zwaveEvent(cmd)
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract Secure command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def version = cmdVersions()[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        log.warn "Unable to extract CRC16 command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    } else {
        log.warn "Unable to extract MultiChannel command from $cmd"
    }
}

private logging(text, type = "debug") {
    if (settings.logging == "true") {
        log."$type" text
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
    logging("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
    zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
    encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
    encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
    encap(encapMap.cmd, encapMap.ep)
}

private encap(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo.zw.contains("s")) {
        secEncap(cmd)
    } else if (zwaveInfo.cc.contains("56")){
        crcEncap(cmd)
    } else {
        logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
        cmd.format()
    }
}

private encapSequence(cmds, Integer delay=250) {
    delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
    delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

private List intToParam(Integer value, Integer size = 1) {
    def result = []
    size.times {
        result = result.plus(0, (value & 0xFF) as Short)
        value = (value >> 8)
    }
    return result
}

/**
 *  zwaveEvent( COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2 (0x8E) : ASSOCIATION_REPORT_V2 (0x03) )
 *
 *  The Multi-channel Association Report command is used to advertise the current destinations of a given
 *  association group (nodes and endpoints).
 *
 *  Action: Store the destinations in the zwtAssocGroup cache, update syncPending, and log an info message.
 *
 *  Note: Ideally, we want to update the corresponding preference value shown on the Settings GUI, however this
 *  is not possible due to security restrictions in the SmartThings platform.
 *
 *  Example: MultiChannelAssociationReport(groupingIdentifier: 2, maxNodesSupported: 8, nodeId: [9,0,1,1,2,3],
 *            reportsToFollow: 0)
 **/
def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def groupKey = groupMap().find( {it.id == cmd.groupingIdentifier } ).key
    //logging("zwaveEvent(): Multi-Channel Association Report received: ${cmd}","trace")

    //logging("Association Group #${cmd.groupingIdentifier} contains destinations: ${cmd.nodeId}","info")
    logging("Association Group #${cmd.groupingIdentifier} Value in state  #${parseAssocGroupInput(state."$groupKey".value, 8)} destinations: ${cmd.nodeId}","info")
    state."$groupKey".state = (parseAssocGroupInput(state."$groupKey".value, 8) == cmd.nodeId) ? "synced" : "incorrect"
    syncNext()
}

/**
 *  parseAssocGroupInput(string, maxNodes)
 *
 *  Converts a comma-delimited string of destinations (nodes and endpoints) into an array suitable for passing to
 *  multiChannelAssociationSet(). All numbers are interpreted as hexadecimal. Anything that's not a valid node or
 *  endpoint is discarded (warn). If the list has more than maxNodes, the extras are discarded (warn).
 *
 *  Example input strings:
 *    "9,A1"      = Nodes: 9 & 161 (no multi-channel endpoints)            => Output: [9, 161]
 *    "7,8:1,8:2" = Nodes: 7, Endpoints: Node8:endpoint1 & node8:endpoint2 => Output: [7, 0, 8, 1, 8, 2]
 */
private parseAssocGroupInput(string, maxNodes) {
    logging("parseAssocGroupInput(): Parsing Association Group Nodes: ${string}","trace")

    // First split into nodes and endpoints. Count valid entries as we go.
    if (string) {
        def nodeList = string.split(',')
        def nodes = []
        def endpoints = []
        def count = 0

        nodeList = nodeList.each { node ->
            node = node.trim()
            if ( count >= maxNodes) {
                logging("parseAssocGroupInput(): Number of nodes and endpoints is greater than ${maxNodes}! The following node was discarded: ${node}","warn")
            }
            else if (node.matches("\\p{XDigit}+")) { // There's only hexadecimal digits = nodeId
                def nodeId = Integer.parseInt(node,16)  // Parse as hex
                if ( (nodeId > 0) & (nodeId < 256) ) { // It's a valid nodeId
                    nodes << nodeId
                    count++
                }
                else {
                    logging("parseAssocGroupInput(): Invalid nodeId: ${node}","warn")
                }
            }
            else if (node.matches("\\p{XDigit}+:\\p{XDigit}+")) { // endpoint e.g. "0A:2"
                def endpoint = node.split(":")
                def nodeId = Integer.parseInt(endpoint[0],16) // Parse as hex
                def endpointId = Integer.parseInt(endpoint[1],16) // Parse as hex
                if ( (nodeId > 0) & (nodeId < 256) & (endpointId > 0) & (endpointId < 256) ) { // It's a valid endpoint
                    endpoints.addAll([nodeId,endpointId])
                    count++
                }
                else {
                    logging("parseAssocGroupInput(): Invalid endpoint: ${node}","warn")
                }
            }
            else {
                logging("parseAssocGroupInput(): Invalid nodeId: ${node}","warn")
            }
        }

        return (endpoints) ? nodes + [0] + endpoints : nodes
    }
    else {
        return []
    }
}

/*
##########################
## Device Configuration ##
##########################
*/
private Map cmdVersions() {
	[0x5E: 1, 0x86: 1, 0x72: 1, 0x59: 1, 0x73: 1, 0x32: 3, 0x98: 1, 0x7A: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 2, 0x8E: 2, 0x60: 3] //widom
}
//0x5E,0x25, 0x32, 0x60, 0x85,0x8E,0x59,0x55,0x86,0x72,0x5A,0x73,0x70,0x98,0x9F,0x6C,0x7A"

private parameterMap() {[
        [key: "numClickLoad", num: 1, size: 1, type: "number", def: 7, min: 0, max: 7, def: 7, title: "Numbers of clicks to controlthe loads",
         descr: "Define which sequences of clickscontrol the load connected to both Channel 1and Channel 2, if parameter No. 2 is set toits default value (see device manual)."],
        [key: "numClickLoadCH2", num: 2, size: 1, type: "number", def: 8, min: 0, max: 8, def: 8, title: "Numbers of clicks to control Channel 2 load",
         descr: "Defineswhich sequences of click controlthe load connected to Channel 2"],
        [key: "valuesGroup2", num: 4, size: 1, type: "number", def: 100, min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 2",
         descr: "Value used for devices belonging to Group 2 when external switchI1 receives1 Click"],
        [key: "valuesGroup5", num: 5, size: 1, type: "number", def: 100, min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 5",
         descr: "Value used for devices belonging to Group 5 when external switch I2 receives  1 Click "],
        [key: "valuesGroup3", num: 6, size: 1, type: "number", def: 100, min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 3",
         descr: "Value used for devices belonging to Group 3 when external switch I1 receives 2 Clicks"],
        [key: "valuesGroup6", num: 7, size: 1, type: "number", def: 100, min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 6",
         descr: "Value used for devices belonging to Group 6 when external switch I2 receives 2 Clicks"],
        [key: "valuesGroup4", num: 8, size: 1, type: "number", def: 100, min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 4",
         descr: "Value used for devices belonging to Group 4 when external switch I1 receives 3 Clicks"],
        [key: "valuesGroup7", num: 9, size: 1, type: "number", def: 100, min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 7",
         descr: "Value used for devices belonging to Group 7 when external switch I2 receives 3 Clicks"],         
		[key: "ch1Offtimer", num: 10, size: 2, type: "number", def: 0, min: 0, max: 32000, title: " Timer to switch OFF the Channel 1 load(tenth of second)",
         descr: "Defines the time after which the Channel 1 load is switched OFF"],
    	[key: "ch2Offtimer", num: 11, size: 2, type: "number", def: 0, min: 0, max: 32000, title: " Timer to switch OFF the Channel 2 load(tenth of second)",
         descr: "Defines the time after which the Channel 2 load is switched OFF"],
    	[key: "ch1Ontimer", num: 12, size: 2, type: "number", def: 0, min: 0, max: 32000, title: "Timer to switch ON the Channel 1 load(tenth of second)",
         descr: "Defines the time after which the Channel 1 load is switched ON"],
    	[key: "ch2Ontimer", num: 13, size: 2, type: "number", def: 0, min: 0, max: 32000, title: "Timer to switch ON the Channel 2 load(tenth of second)",
         descr: "Defines the time after which the Channel 2 load is switched ON"],
        [key: "localScenario", num: 40, size: 1, type: "enum", options: [
                0: "INDIPENDENT_CHANNELS",
                1: "NEVER_BOTH_ON",
                2: "SEQUENCING_RELAY"
        ], def: "0", title: "Local Scenario",
         descr: "Defines the behavior of the device Channels when the I1/I2 external switches receive a valid number of clicks (see Parameters No. 1 and No. 2)"],
        [key: "startUpStatus", num: 60, size: 1, type: "enum", options: [
                0: "OFF_OFF",
                1: "OFF_ON",
                2: "ON_OFF",
                3: "ON_ON",
                4: "PREVIOUS STATUS"
        ], def: "4", title: "Start-up status",
         descr: "Defines the status of the device following a restart"],
        [key: "externaSwitchType", num: 62, size: 1, type: "enum", options: [
                0: "IGNORE",
                1: "BUTTON",
                2: "SWITCH"
        ], def: "1", title: " Type of external switches",
         descr: "Defines the type of external switch connected both to input 1 and input 2, if parameter No. 63 is set in its default value (see device manual)"],
        [key: "externaSwitchTypeCh2", num: 63, size: 1, type: "enum", options: [
                0: "IGNORE",
                1: "BUTTON",
                2: "SWITCH",
                3: "EQUAL TO SWITCH 1"
        ], def: "3", title: "Type of Channel 2 external switch",
         descr: "Defines the external switch type connected to the input 2"]         
]}

private groupMap() {[
        [key: "group1", id: 1, def: "", type: "text", title: "Group 1",
         descr: "lifeline"],
         [key: "group2", id: 2, def: "", type: "text", title: "Group 2",
         descr: "1 Click on S1"],
         [key: "group3", id: 3, def: "", type: "text", title: "Group 3",
         descr: "2 Click on S1"],
         [key: "group4", id: 4, def: "", type: "text", title: "Group 4",
         descr: "3 Click on S1"],
         [key: "group5", id: 5, def: "", type: "text", title: "Group 5",
         descr: "1 Click on S2"],
         [key: "group6", id: 6, def: "", type: "text", title: "Group 6",
         descr: "2 Click on S2"],
         [key: "group7", id: 7, def: "", type: "text", title: "Group 7",
         descr: "3 Click on S2"]
]}