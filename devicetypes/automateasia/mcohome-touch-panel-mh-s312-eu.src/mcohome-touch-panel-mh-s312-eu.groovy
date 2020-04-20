/*
MCOHome Touch Panel MH-S312-EU Description

zw:L - Listening
type:1001
mfr:015F (Device manufacturer)
prod:3121 (Product Type ID)
model:5102 (Product ID)
ver:5.00 (Application firmware version)
zwv:4.62 (Z-Wave protocol stack)
lib:03 (Z-Wave library)
cc:5E,85,59,8E,60,55,86,72,5A,73,25,27,70,2C,2B,5B,20,7A (supported command classes)
ccOut:20,26,5B (control command classes)
sec: (supported command classes via security encapsulation)
secOut: (control command classes via security encapsulation)
role:05 (Z-Wave Plus Role Type)
ff:8700 (form factor. Has offset of 0x8000)
ui:8700 (Z-Wave Plus User Icon Type)
epc:2
ep:['1001 5E,85,59,8E,25,27']
*/

/*
List of command classes supported by this device
0x5E - ZWave Plus Info
0x85 - Association
0x59 - Association Group Info
0x8E - Multi Channel Association
0x60 - Multi Channel
0x55 - Transport Service
0x86 - Version
0x72 - Manufacturer Specific
0x5A - Device Reset Locally
0x73 - Powerlevel
0x25 - Binary Switch
0x27 - All Switch (Obsolete)
0x70 - Configuration
0x2C - Scene Actuator Configuration
0x2B - Scene Activation
0x5B - Central Scene
0x20 - Basic
0x7A - Firmware Update Meta Data
0x26 - Multilevel Switch
*/

metadata {
	definition (name: "MCOHome Touch Panel MH-S312-EU", namespace: "automateasia", author: "edwin") {
		capability "Switch"
		capability "Zw Multichannel"
		capability "Polling"
		capability "Configuration"
		capability "Refresh"
		attribute "Status", "enum", ["Connected", "Pinging"]
		command "childOn"
		command "childOff"
		fingerprint mfr: "015F", prod: "3121", model: "5102"
	}

	// TODO: To build simulator code
	simulator {
	}

	tiles(scale: 2) {
		standardTile("status", "device.status", inactiveLabel: true, decoration: "flat") {
            state("connected", label:'Connected', icon:"st.Home.home30", backgroundColor:"#00A0DC")
			state("pinging", label:'Pinging', icon:"st.Home.home30", backgroundColor:"#e86d13")
		}
		standardTile("refresh", "device.status", height: 2, width: 2, inactiveLabel: false, decoration: "flat") {
			state("connected", label:"Connected", action:"refresh.refresh", icon:"st.secondary.refresh")
            state("pinging", label:'Trying to connect', icon:"st.secondary.refresh")
		}
		childDeviceTile("channel-1", "channel-1", height: 3, width: 3, childTileName: "switch")
		childDeviceTile("channel-2", "channel-2", height: 3, width: 3, childTileName: "switch")
	}

	main("status")
	details(["channel-1", "channel-2", "refresh"])
}

def installed() {
	createChildDevices()
    // Device-Watch simply pings if no device events received for 60 secs (checkInterval)
    sendEvent(name: "checkInterval", value: 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    response(refresh() + configure())
}

def updated() {
	// Device-Watch simply pings if no device events received for 60 secs (checkInterval)
	sendEvent(name: "checkInterval", value: 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	response(refresh())
}

private void createChildDevices() {
	for (i in 1..2) {
		addChildDevice("MCOHome Touch Panel Switch","${device.deviceNetworkId}-${i}", null,[completedSetup: true, label: "Switch $i", isComponent: false, componentName: "channel-$i", componentLabel: "channel-$i"])
	}
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "[DNI $device.deviceNetworkId] ping() called"
	refresh()
}

def refresh() {
	sendEvent(name: "status", value: "Pinging")
	delayBetween([
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
		zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
		//TODO: To implement Associations
		//zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:1).format()
	])
}

def configure() {
    //TODO: To implement device configuration
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20:1, 0x60:3, 0x25:1, 0x70:1, 0x32:1, 0x72:1])
	if (cmd) {
		result = zwaveEvent(cmd)
        sendEvent(name: "status", value: "Connected")
		log.debug "[DNI $device.deviceNetworkId] Parsed ${description} to ${cmd} to ${result.inspect()}"
	} else {
		log.debug "[DNI $device.deviceNetworkId] Non-parsed event: ${description}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def thisChannel = cmd.sourceEndPoint
	def thisDevice
	def children = getChildDevices()
	for (i in 0..children.size()-1) {
		if (children.get(i).deviceNetworkId == "${device.deviceNetworkId}-$thisChannel") thisDevice = children.get(i)
	}
	switch(cmd.commandClass) {
		case 32:
			if (cmd.parameter == [0]) {
				thisDevice.sendEventOff()
			}
			if (cmd.parameter == [255]) {
				thisDevice.sendEventOn()
			}
		break
		case 37:
			if (cmd.parameter == [0]) {
				thisDevice.sendEventOff()
			}
			if (cmd.parameter == [255]) {
				thisDevice.sendEventOn()
			}
		break
	}
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
	log.debug("[DNI $device.deviceNetworkId] ManufacturerSpecificReport ${cmd.inspect()}")
}

//TODO: To implement Associations
/*
def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	log.debug("[DNI $device.deviceNetworkId] MultiChannelAssociationReport ${cmd.inspect()}")
	switch (cmd.groupingIdentifier) {
		case 1:
			log.debug("[DNI $device.deviceNetworkId] Group 1 ${cmd.nodeId.join(',')}")
		break
		case 2:
			log.debug("[DNI $device.deviceNetworkId] Group 2 ${cmd.nodeId.join(',')}")
		break
		case 3:
			log.debug("[DNI $device.deviceNetworkId] Group 3 ${cmd.nodeId.join(',')}")
		break
	}
}
*/

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug("[DNI $device.deviceNetworkId] Command ${cmd.inspect()}")
	[:]
}

def childOn(String dni) {
	if (dni=="${device.deviceNetworkId}-1") {
		delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
		])
	} else if (dni=="${device.deviceNetworkId}-2") {
    	delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
		])
	} else {
		//do nothing
    }
}

def childOff(String dni) {
	if (dni=="${device.deviceNetworkId}-1") {
		delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
		])
	} else if (dni=="${device.deviceNetworkId}-2") {
		delayBetween([
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format(),
			zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
		])
	} else {
		//do nothing
	}
}