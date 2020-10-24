/**
 *  Widom Smart DRY contact
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
    definition (name: "WiDom Smart Dry Contact", namespace: "WiDomsrl", author: "WiDom srl") {
		//capability "Actuator"
		capability "Switch"
		capability "Relay Switch"
        capability "Configuration"

        fingerprint mfr: "0149", prod: "1214", model: "0900"
    }
    
   	tiles{
        multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
			   attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			   attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
			   //attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			   //attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
			}
            tileAttribute("device.deviceID", key:"SECONDARY_CONTROL") {
                attributeState("deviceID", label:'(ID: ${currentValue})')
            }
	    }
        valueTile("multiStatus", "device.multiStatus", decoration: "flat", width: 6, height: 2) {
            state "multiStatus", label:'${currentValue}'
        }

        standardTile("main", "device.switch", decoration: "flat", canChangeIcon: true) {
            state "off", label: 'off', action: "switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
            state "on", label: 'on', action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
        }
        main "main"
		details(["switch", "multiStatus"])

	}

    preferences {
        input (
                title: "WiDom Smart Dry Contact manual",
                description: "Tap to view the manual.",
                image: "https://www.widom.it/wp-content/uploads/2019/03/widom-3d-smart-dry-contact.gif",
                url: "https://www.widom.it/wp-content/uploads/2020/04/Widom_Dry_Contact_IT_070420.pdf",
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
                    //description: "Default: $it.def" ,
                    type: it.type,
                    options: it.options,
                    //range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
                    defaultValue: it.def,
                    required: false
            )
        }
        input ( name: "logging", title: "Logging", type: "boolean", required: false )
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
                           "To delete previous configurations you must enter 0 "
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
    encap(zwave.basicV1.basicSet(value: 255))
}

def off() {
    encap(zwave.basicV1.basicSet(value: 0))
}


//Configuration and synchronization
def updated() {
    if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
    def cmds = []
    logging("Executing updated()","info")

    if (device.currentValue("numberOfButtons") != 6) { sendEvent(name: "numberOfButtons", value: 6) }

    state.lastUpdated = now()
    syncStart()
}

private syncStart() {
    boolean syncNeeded = false
    boolean syncNeededGroup = false
    Integer settingValue = null
    parameterMap().each {
        if(settings."$it.key" != null) {
            settingValue = settings."$it.key" as Integer
            if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
            if (state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
                state."$it.key".value = settingValue
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
        logging("sync needed.", "info")
        syncNext()
    }
    if ( syncNeededGroup ) {
        logging("${device.displayName} - starting sync.", "info")
        multiStatusEvent("Sync in progress.", true, true)
        syncNext()
    }
}

private syncNext() {
    logging("Executing syncNext()","info")
    def cmds = []
    for ( param in parameterMap() ) {
        if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
            multiStatusEvent("Sync in progress. (param: ${param.num})", true)
            state."$param.key"?.state = "inProgress"
            logging("Parameter number ${param.num}. Parameter Value: ${state."$param.key"?.value}","info")
            cmds << response(encap(zwave.configurationV1.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
            cmds << response(encap(zwave.configurationV1.configurationGet(parameterNumber: param.num)))
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
        sendHubCommand(cmds,1000)
    } else {
        runIn(1, "syncCheck")
    }
}

private syncCheck() {
    logging("Executing syncCheck()","info")
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

    parameterMap().each {
        if (state."$it.key"?.state == "incorrect" ) {
            incorrect << it
        } else if ( state."$it.key"?.state == "failed" ) {
            failed << it
        } else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
            notSynced << it
        }
    }

    if (failed) {
        multiStatusEvent("Sync failed! Verify parameter: ${failed[0].num}", true, true)
    } else if (incorrect) {
        multiStatusEvent("Sync mismatch! Verify parameter: ${incorrect[0].num}", true, true)
    } else if (notSynced) {
        multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
    } else {
        if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
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

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
    def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
    logging("Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
    state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
    syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
    logging("rejected request!","warn")
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
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    logging("SwitchBinaryReport received, value: ${cmd.value} ","info")
    sendEvent([name: "switch", value: (cmd.value == 0 ) ? "off": "on"])
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {
    def result = []
    def deviceId = [];
    logging("Parsing: ${description}")
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
            logging("Parsed: ${cmd}")
            zwaveEvent(cmd)
        }
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        logging("Unable to extract Secure command from $cmd","warn")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
    def version = cmdVersions()[cmd.commandClass as Integer]
    def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
    def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
    if (encapsulatedCommand) {
        logging("Parsed Crc16Encap into: ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand)
    } else {
        logging("Unable to extract CRC16 command from $cmd","warn")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
    if (encapsulatedCommand) {
        logging("Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
    } else {
        logging("Unable to extract MultiChannel command from $cmd","warn")
    }
}

private logging(text, type = "debug") {
    if (settings.logging == "true" || type == "warn") {
        log."$type" "${device.displayName} - $text"
    }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    logging("encapsulating command using Secure Encapsulation, command: $cmd","info")
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    logging("encapsulating command using CRC16 Encapsulation, command: $cmd","info")
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
    logging("encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
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
        logging("no encapsulation supported for command: $cmd","info")
        cmd.format()
    }
}

private encapSequence(cmds, Integer delay=250) {
    delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
    delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
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
    [0x5E: 2, 0x86: 2, 0x72: 2, 0x59: 2, 0x98: 1, 0x25: 1, 0x5A: 1, 0x85: 2, 0x70: 1, 0x8E: 2, 0x9F: 1, 0x6C: 1] 
}

private parameterMap() {[
		[key: "numClickLoad", num: 1, size: 1, type: "number", min: 0, max: 7, def: 7, title: "Numbers of clicks to controlthe loads",
         descr: "Define which sequences of clicks control the load (see device manual)."],
        [key: "valuesGroup2", num: 4, size: 1, type: "number", min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 2",
         descr: "Value  used  for  devices  belonging  to  Group  2  when  the  external switch receives 1 Click"],
        [key: "valuesGroup3", num: 5, size: 1, type: "number", min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 3",
         descr: "Value used for devices belonging to Group 3 whenthe external switch receives 2 Clicks"],
        [key: "valuesGroup4", num: 6, size: 1, type: "number", min: -1, max: 100, def: 100, title: "Value used for devices belonging to Group 4",
         descr: "Value used for devices belonging to Group 4 when external switch receives 3 Clicks"],      
		[key: "OffTimer", num: 10, size: 2, type: "number", def: 0, min: 0, max: 32000, title: " Timer to switch OFF the Relay",
         descr: "Defines the time after which the relay is switched OFF. Time unit is set by parameter 15(see device manual)"],
    	[key: "OnTimer", num: 11, size: 2, type: "number", def: 0, min: 0, max: 32000, title: " Timer to switch ON the Relay",
         descr: "Defines the time after which the relay is switched ON. Time unit is set by parameter 15(see device manual)"],
    	[key: "timerScale", num: 15, size: 1, type: "enum", options: [
                1: "Tenth of seconds",
                2: "Seconds",
        ], def: "1", title: "Timer scale", descr: "Defines the time unit used for parameters No.10 and No.11"],
    	[key: "oneClickScene", num: 20, size: 2, type: "number",min: 0, max: 255, def: 0, title: "One Click Scene ActivationSet",
         descr: "Defines the Scene Activation Set value sent to the Lifeline group with 1 Clickon the external switch"],
    	[key: "twoClickScene", num: 21, size: 2, type: "number",min: 0, max: 255, def: 0, title: "Two Clicks Scene ActivationSet",
         descr: "Defines the Scene Activation Set value sent to the Lifeline group with 2 Clickson the external switch"],
    	[key: "threeClickScene", num: 22, size: 2, type: "number",min: 0, max: 255, def: 0, title: "Three Clicks Scene ActivationSet",
         descr: "Defines the Scene Activation Set value sent to the Lifeline group with 1 Clicks on the external switch"],
        [key: "startUpStatus", num: 60, size: 1, type: "enum", options: [
                1: "ON",
                2: "OFF",
                3: "PREVIOUS STATUS"
        ], def: "3", title: "Start-up status",
         descr: "Defines the status of the device following a restart"],
        [key: "externaSwitchType", num: 62, size: 1, type: "enum", options: [
                0: "IGNORE",
                1: "BUTTON",
                2: "SWITCH"
        ], def: "1", title: " Type of external switches",
         descr: "Defines the type of external switch"],
]}

private groupMap() {[
        [key: "group1", id: 1, def: "", type: "text", title: "Group 1",
         descr: "lifeline"],
         [key: "group2", id: 2, def: "", type: "text", title: "Group 2",
         descr: "1 Click"],
         [key: "group3", id: 3, def: "", type: "text", title: "Group 3",
         descr: "2 Click"],
         [key: "group4", id: 4, def: "", type: "text", title: "Group 4",
         descr: "3 Click"],
]}