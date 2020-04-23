/**
 *  Z-Wave Light Switch Multichannel - Device Handler (parent)
 *
 *  Copyright 2017 HongTat Tan
 *
 *
 *  Requires ** Z-Wave Light Switch Multichannel Child Device **
 *
 *  Tested on the following Z-Wave light switches:
 *      NEO Coolcam Z-Wave Light Switch (EU-1/2 Gang)
 *      MCOHome Z-Wave Light Switch (MH-312-EU)
 *
 *
 *  Version history:
 *      1.0.3 (7/10/2017) - Add 2-way status reporting
 *      1.0.2 (25/09/2017) - Add Health Check & Configure for child device
 *      1.0.1 (23/09/2017) - Bug fix
 *      1.0 (23/09/2017) - Initial Release
 *
 *  Incorporates code from:
 *      Peter Major  - https://github.com/petermajor/
 *      Omar Shahbal - https://github.com/omarshahbal/
 *      Eric Maycock - https://github.com/erocm123/
 *      David Lomas - https://github.com/codersaur/SmartThings/tree/master/devices/zwave-tweaker
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
 */

metadata {
    definition (name: "COOLTOUCH Z-Wave Light Switch", namespace: "hongtat", author: "HongTat Tan") {
    capability "Actuator"
    capability "Switch"
    capability "Refresh"
    capability "Health Check"
    capability "Polling"
    capability "Configuration"
    capability "Sensor"
    capability "Zw Multichannel"

    attribute "lastCheckin", "String"

    preferences {
        section(title: "Check-in Interval") {
            paragraph "Run a Check-in procedure every so often."
            input "checkin", "enum", title: "Run Check-in procedure", options: ["Every 1 minute", "Every 5 minutes", "Every 10 minutes", "Every 15 minutes", "Every 30 minutes", "Every 1 hour"], description: "Allows check-in procedure to run every so often", defaultValue: "Every 1 minute", required: true
        }
        section(title: "Check-in Info") {
            paragraph "Display check-in info"
            input "checkinInfo", "enum", title: "Show last Check-in info", options: ["Hide", "MM/dd/yyyy h:mm", "MM-dd-yyyy h:mm", "dd/MM/yyyy h:mm", "dd-MM-yyyy h:mm"], description: "Show last check-in info.", defaultValue: "dd/MM/yyyy h:mm", required: true
        }
    }

    fingerprint cc: "20,25,85,8E,59,71,55,86,98,60,5A", deviceJoinName: "COOLTOUCH Z-Wave Light Switch"
    ///fingerprint inClusters: "0x60"
	//fingerprint inClusters: "0x60, 0x25"
	//fingerprint inClusters: "0x60, 0x26"
	//fingerprint inClusters: "0x5E, 0x59, 0x60, 0x8E"
    // mfr:0505 prod:0000 model:0003
    //fingerprint mfr:"0258", prod:"0003", model:"108C", deviceJoinName: "NEO Coolcam Light Switch (1-CH)"
    //fingerprint mfr:"0258", prod:"0003", model:"108B", deviceJoinName: "NEO Coolcam Light Switch (2-CH)"
    //fingerprint mfr:"015F", prod:"3102", model:"0201", deviceJoinName: "MCOHome Light Switch S311 (1-CH)"
    //fingerprint mfr:"015F", prod:"3102", model:"0202", deviceJoinName: "MCOHome Light Switch S312 (2-CH)"
    //fingerprint mfr:"015F", prod:"3102", model:"0204", deviceJoinName: "MCOHome Light Switch S314 (4-CH)"
    //fingerprint mfr:"015F", prod:"4102", model:"0201", deviceJoinName: "MCOHome Light Switch S411 (1-CH)"
    //fingerprint mfr:"015F", prod:"4102", model:"0202", deviceJoinName: "MCOHome Light Switch S412 (2-CH)"
    }
    simulator {
        // TODO: define status and reply messages here
    }
    tiles(scale: 2) {
        multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
                attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff"
                attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
            }
            tileAttribute("device.lastCheckin", key: "SECONDARY_CONTROL") {
                attributeState("default", label:'${currentValue}')
            }
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
        }
        standardTile("configure", "device.default", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"configure", icon:"st.secondary.configure"
        }
        main(["switch"])
        details(["switch", "refresh", "configure"])
    }
}

private getCommandClassVersions() {
    [
    0x20: 1,  // Basic (v1)
    0x25: 1,  // Switch Binary (v1)
    0x27: 1,  // Switch All (v1)
    0x59: 1,  // Association Grp Info (v1)
    0x5A: 1,  // Device Reset Locally (v1)
    0x5E: 1,  // ZwaveplusInfo
    0x60: 3,  // Multi Channel (v3)
    0x70: 2,  // Configuration (v1,2)
    0x72: 2,  // Manufacturer Specific (v1,2)
    0x73: 1,  // Powerlevel (v1)
    0x85: 2,  // Association (v1,2)
    0x86: 1,  // Version (v1)
    0x8E: 2   // Multi Channel Association (v2)
    ]
}
def parse(String description) {
    def checkinInfoFormat = (settings.checkinInfo ?: 'dd/MM/yyyy h:mm')
    def now = ''
    if (checkinInfoFormat != 'Hide') {
        try {
            now = 'Last Check-in: ' + new Date().format("${checkinInfoFormat}a", location.timeZone)
        } catch (all) { }
    }
    sendEvent(name: "lastCheckin", value: now, displayed: false)
    def channels = getDataValue("endpoints")?.toInteger()
    if (channels > 0) {
        def epevents = "1"
        if (channels > 1) {
            for (i in 2..channels) {
                def childDevice = childDevices.find{ it.deviceNetworkId == "$device.deviceNetworkId-ep$channels" }
                if (childDevice) {
                    childDevice.sendEvent(name: "lastCheckin", value: now, displayed: false)
                }
            }
        }
    }
    log.debug "Event: ${description}"
    def result = null
    def cmd = zwave.parse(description, getCommandClassVersions())
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "Parsed ${description} to ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    result
}
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    log.debug "BasicReport() called - ${cmd.inspect()}"
    createEvent(name:"switch", value: cmd.value ? "on" : "off")
}
def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    log.debug "SwitchBinaryReport() called - ${cmd.inspect()}"
    createEvent(name:"switch", value: cmd.value ? "on" : "off")
}
def zwaveEvent(physicalgraph.zwave.commands.powerlevelv1.PowerlevelReport cmd) {
    log.debug "PowerlevelReport() called - ${cmd.inspect()}"
}
def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    log.debug "AssociationReport() called - Group #${cmd.groupingIdentifier} contains nodes: ${toHexString(cmd.nodeId)} (hexadecimal format)"
}
def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
    log.debug "MultiChannelAssociationReport() called - Group #${cmd.groupingIdentifier} contains destinations: ${toHexString(cmd.nodeId)} (hexadecimal format)"
}
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
    def result = []
    def cmds = []
    if(!state.endpointInfo) state.endpointInfo = []
    state.endpointInfo[cmd.endPoint - 1] = cmd.format()[6..-1]
    if (cmd.endPoint < getDataValue("endpoints").toInteger()) {
        cmds = zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: cmd.endPoint + 1).format()
    } else {
        log.debug "endpointInfo: ${state.endpointInfo.inspect()}"
    }
    result << createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:"")
    if(cmds) result << response(cmds)
    result
}
private List loadEndpointInfo() {
    if (state.endpointInfo) {
        state.endpointInfo
    } else if (device.currentValue("epInfo")) {
        fromJson(device.currentValue("epInfo"))
    } else {
        []
    }
}
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
    updateDataValue("endpoints", cmd.endPoints.toString())
    if (!state.endpointInfo) {
        state.endpointInfo = loadEndpointInfo()
    }
    if (state.endpointInfo.size() > cmd.endPoints) {
        cmd.endpointInfo
    }
    state.endpointInfo = [null] * cmd.endPoints
    [ createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:""),
      response(zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: 1)) ]
}
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
    log.debug("ManufacturerSpecificReport ${cmd.inspect()}")
}
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {

    def map = [ name: "switch${cmd.sourceEndPoint}" ]

    def encapsulatedCommand = cmd.encapsulatedCommand([0x32: 3, 0x25: 1, 0x20: 1])
    log.debug "MultiChannelCmdEncap called - ${cmd.inspect()}"
    if (encapsulatedCommand && cmd.commandClass == 50) {
        zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
    } else {
        switch(cmd.commandClass) {
            case 32:
            case 37:
                if (cmd.parameter == [0]) {
                    map.value = "off"
                }
                if (cmd.parameter == [255]) {
                    map.value = "on"
                }
                log.debug "MultiChannelCmdEncap mapped - ${map}"

                if (cmd.sourceEndPoint == 1) {
                    sendEvent(name: "switch", value: map.value)
                } else {
                    def childDevice = childDevices.find{ it.deviceNetworkId == "$device.deviceNetworkId-ep$cmd.sourceEndPoint" }
                    if (childDevice) {
                        childDevice.sendEvent(name: "switch", value: map.value)
                    }
                }
            break
        }
    }
}
def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug("Command () called - ${device.displayName} ${cmd}")
    // Handles all Z-Wave commands we aren't interested in
    [:]
}
// Devices that support the Security command class can send messages in an
// encrypted form; they arrive wrapped in a SecurityMessageEncapsulation
// command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1])

    // can specify command class versions here like in zwave.parse
    if (encapsulatedCommand) {
        return zwaveEvent(encapsulatedCommand)
    }
}
def on() {
    onOffCmd(device.deviceNetworkId, 0xFF)
}
def off() {
    onOffCmd(device.deviceNetworkId, 0)
}
void onOffCmd(dni, value) {
    def channels = getDataValue("endpoints").toInteger()
    if (channels > 0) {
        log.debug("onOffCmd called - ${channelNumber(dni)} ${value}")
        def cmds = []
        cmds << new physicalgraph.device.HubAction(zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:channelNumber(dni), destinationEndPoint:channelNumber(dni), commandClass:37, command:1, parameter:[value]).format())
        cmds << new physicalgraph.device.HubAction(zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:channelNumber(dni), destinationEndPoint:channelNumber(dni), commandClass:37, command:2).format())
        sendHubCommand(cmds, 1000)
    } else {
        def cmds = []
        cmds << new physicalgraph.device.HubAction(zwave.basicV1.basicSet(value: value).format())
        cmds << new physicalgraph.device.HubAction(zwave.switchBinaryV1.switchBinaryGet().format())
        sendHubCommand(cmds, 500)
    }
}

def installed() {
    log.debug "installed() called"
    initialize()
}
def uninstalled() {
    log.debug "uninstalled() called"
    sendEvent(name: "epEvent", value: "delete all", isStateChange: true, displayed: false, descriptionText: "Delete endpoint devices")
}
def updated() {
    log.debug "updated() called"
    initialize()
    if (childDevices) {
        if (device.label != state.oldLabel) {
            childDevices.each {
                if (it.label == "${state.oldLabel} (CH${channelNumber(it.deviceNetworkId)})") {
                    def newLabel = "${device.displayName} (CH${channelNumber(it.deviceNetworkId)})"
                    it.setLabel(newLabel)
                }
            }
            state.oldLabel = device.label
    	}
    }
}
def refresh() {
    def lastRefreshed = state.lastRefreshed
    if (lastRefreshed && (now() - lastRefreshed < 5000)) return
    state.lastRefreshed = now()

    log.debug "refresh() called"
    def channels = getDataValue("endpoints").toInteger()
    if (channels > 0) {
        def cmds = (1..channels).collect { endpoint ->
            new physicalgraph.device.HubAction(zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:endpoint, destinationEndPoint:endpoint, commandClass:37, command:2).format())
        }
        sendHubCommand(cmds, 500)
    } else {
        sendHubCommand([
            new physicalgraph.device.HubAction(zwave.basicV1.basicGet().format()),
            new physicalgraph.device.HubAction(zwave.switchBinaryV1.switchBinaryGet().format())
        ], 500)
    }
}
// If you add the Configuration capability to your device type, this
// command will be called right after the device joins to set
// device-specific configuration commands.
def configure() {
    log.debug "configure() called"
    initialize()
}
// PING is used by Device-Watch in attempt to reach the Device
def ping() {
    log.debug "ping() called"
    refresh()
}
// If you add the Polling capability to your device type, this command
// will be called approximately every 5 minutes to check the device's state
def poll() {
    log.debug "poll() called"
    refresh()
}
def initialize() {
    def lastInitialized = state.lastInitialized
    if (lastInitialized && (now() - lastInitialized < 2000)) return
    state.lastInitialized = now()
    log.debug "initialize() called"

    def checkinMethod = (settings.checkin ?: 'Every 1 minute').replace('Every ', 'Every').replace(' minute', 'Minute').replace(' hour', 'Hour')
    try {
        "run$checkinMethod"(refresh)
    } catch (all) { }

    device?.updateSetting("refreshinterval", null)

    // Parse fingerprint for supported command classes:
    def ccIds = []
    if (getZwaveInfo()?.cc) {
        ccIds = getZwaveInfo()?.cc + getZwaveInfo()?.sec
    }
    else {
        ccIds = device.rawDescription.findAll(/0x\p{XDigit}+/)
        if (ccIds.size() > 0) { ccIds.remove(0) }
    }
    ccIds.removeAll([null])
    ccIds = ccIds.sort().collect { Integer.parseInt(it.replace("0x",""),16) }
    if (!ccIds.find( {it == 0x60 }) ) {
        log.debug "initialize() - Device does not support MULTI_CHANNEL command classes"
        updateDataValue("endpoints", "0")
        if (ccIds.find( {it == 0x27 }) ) {
        }
    } else {
        sendHubCommand([new physicalgraph.device.HubAction(zwave.multiChannelV3.multiChannelEndPointGet().format())], 500)
    }
    // Powerlevel
    /*
    if (ccIds.find( {it == 0x73 }) ) {
        updateDataValue("powerlevel", "1")
    } else {
        updateDataValue("powerlevel", "0")
    }
    */
    def channels = getDataValue("endpoints")?.toInteger()
    if (channels > 0) {
        def epevents = "1"
        if ("${channels}" > 1) {
            for (i in 2..channels) {
                epevents = "${epevents},${i}"
            }
        }
        enableEpEvents(epevents)
    }
    createChildDevices()
    sendEvent(name: "checkInterval", value: (30 * 60), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}
def enableEpEvents(enabledEndpoints) {
    log.debug "enableEpEvents() called - ${enabledEndpoints}"
    state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
    null
}
private void createChildDevices() {
    state.oldLabel = device.label
    def channels = getDataValue("endpoints")?.toInteger()
    log.debug "createChildDevices() called - Channels: ${channels}"
    if (channels > 0) {
        // Multi-channel Association
        def cmds = []
        cmds << new physicalgraph.device.HubAction(zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1, nodeId: []).format())
        cmds << new physicalgraph.device.HubAction(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]).format())
        cmds << new physicalgraph.device.HubAction(zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1).format())
        if (channels > 1) {
            try {
                for (i in 2..channels) {
                    cmds << new physicalgraph.device.HubAction(zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: i, nodeId: []).format())
                    cmds << new physicalgraph.device.HubAction(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: i, nodeId: [zwaveHubNodeId]).format())
                    cmds << new physicalgraph.device.HubAction(zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: i).format())
                    def childDevice = childDevices.find{ it.deviceNetworkId == "$device.deviceNetworkId-ep$i" }
                    if (!childDevice) {
                        addChildDevice("COOLTOUCH Z-Wave Multichannel Child Device", "${device.deviceNetworkId}-ep${i}", null, [completedSetup: true, label: "${device.displayName} (CH${i})",
                            isComponent: false, componentName: "ep$i", componentLabel: "Channel $i"
                        ])
                    } else {
                        childDevice.sendEvent(name: "checkInterval", value: (30 * 60), displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
                    }
                }
            } catch (e) {
                runIn(2, "sendAlert")
            }
        }
        sendHubCommand(cmds, 500)
    } else {
        // Single-channel Association
        sendHubCommand([
            new physicalgraph.device.HubAction(zwave.associationV2.associationRemove(groupingIdentifier: 1, nodeId: []).format()),
            new physicalgraph.device.HubAction(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]).format()),
            new physicalgraph.device.HubAction(zwave.associationV2.associationGet(groupingIdentifier: 1).format()),
            new physicalgraph.device.HubAction(zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: []).format()),
            new physicalgraph.device.HubAction(zwave.associationV2.associationSet(groupingIdentifier: 2, nodeId: [zwaveHubNodeId]).format()),
            new physicalgraph.device.HubAction(zwave.associationV2.associationGet(groupingIdentifier: 2).format())
        ], 500)
    }
}
private channelNumber(String dni) {
    if (dni.indexOf("-ep") >= 0) {
        dni.split("-ep")[-1] as Integer
    } else {
        "1" as Integer
    }
}
/**
 *  toHexString()
 *
 *  Convert a list of integers to a list of hex strings.
 **/
private toHexString(input, size = 2, usePrefix = false) {
    def pattern = (usePrefix) ? "0x%0${size}X" : "%0${size}X"

    if (input instanceof Collection) {
        def hex  = []
        input.each { hex.add(String.format(pattern, it)) }
        return hex.toString()
    }
    else {
        return String.format(pattern, input)
    }
}
private sendAlert() {
    sendEvent(descriptionText: "Child device creation failed. Please make sure that the \"COOLTOUCH Z-Wave Multichannel Child Device\" is installed and published.", eventType: "ALERT", name: "childDeviceCreation", value: "failed", displayed: true, )
}