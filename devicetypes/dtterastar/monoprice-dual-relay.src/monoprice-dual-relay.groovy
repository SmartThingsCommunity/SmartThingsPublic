/**
 *  ZWN-RSM2 Enerwave Dual Load ZWN-RSM2
 *
 *  Author Matt Frank based on the work of chrisb for AEON Power Strip
 *
 *  Date Created:  6/26/2014
 *  Last Modified: 1/11/2015
 *
 */
 // for the UI
metadata {
  definition (name: "MonoPrice Dual Relay", namespace: "dtterastar", author: "Darrell Turner") {
    capability "Switch"
    capability "Polling"
    capability "Configuration"
    capability "Refresh"
    capability "Zw Multichannel"

    attribute "switch", "string"
    attribute "switch2", "string"

    command "on"
    command "off"
    command "on2"
    command "off2"

    fingerprint deviceId: "0x1001", inClusters:"0x25, 0x27, 0x60, 0x70, 0x72, 0x86"
  }

  simulator {
    	status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		reply "8E010101,delay 800,6007": "command: 6008, payload: 4004"
		reply "8505": "command: 8506, payload: 02"
		reply "59034002": "command: 5904, payload: 8102003101000000"
		reply "6007":  "command: 6008, payload: 0002"
		reply "600901": "command: 600A, payload: 10002532"
		reply "600902": "command: 600A, payload: 210031"
  }

  tiles {
  		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
        standardTile("switch2", "device.switch2",canChangeIcon: true) {
                        state "on", label: "switch2", action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821"
                        state "off", label: "switch2", action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
                }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
                        state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
                }

        standardTile("configure", "device.switch", inactiveLabel: false, decoration: "flat") {
                state "default", label:"", action:"configure", icon:"st.secondary.configure"
                }

        main("switch")
        details(["switch","switch2","refresh","configure"])
  }
}

// 0x25 0x32 0x27 0x70 0x85 0x72 0x86 0x60 0xEF 0x82

// 0x25: switch binary
// 0x32: meter
// 0x27: switch all
// 0x70: configuration
// 0x85: association
// 0x86: version
// 0x60: multi-channel
// 0xEF: mark
// 0x82: hail

def parse(String description) {
    def result = null
    def cmd = zwave.parse(description, [0x60:3, 0x25:1, 0x70:1, 0x32:1, 0x72:1])
    if (cmd) {
        result = zwaveEvent(cmd)
        log.debug "Parsed ${description} to ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}


//Reports

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
  log.debug "SwitchBinaryReport $cmd"
  def result = []
  result << response(
  	delayBetween([
    	zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
    	zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
  	])
  )
  result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	log.debug "MultiChannelCmdEncap $cmd"
	def name = "switch$cmd.sourceEndPoint"
    if (cmd.sourceEndPoint == 1) name = "switch"
    def map = [ name: name ]
    if (cmd.commandClass == 37) {
    	if (cmd.parameter == [0]) {
        	map.value = "off"
        }
        if (cmd.parameter == [255]) {
            map.value = "on"
        }
        return createEvent(map)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
	log.debug "MultiChannelCapabilityReport $cmd"
}

def refresh() {
  log.debug "refresh"
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
  ])
}


def poll() {
  log.debug "poll"
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
  ])
}

def configure() {
  log.debug "configure"
    delayBetween([
        zwave.configurationV1.configurationSet(parameterNumber:4, configurationValue: [0]).format()				// Report reguarly
    ])
}

def on() {
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[255]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
  ])
}

def off() {
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:1, parameter:[0]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1, commandClass:37, command:2).format()
    ])
}

def on2() {
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
    ])
}

def off2() {
  delayBetween([
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
    zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:2, destinationEndPoint:2, commandClass:37, command:2).format()
  ])
}
