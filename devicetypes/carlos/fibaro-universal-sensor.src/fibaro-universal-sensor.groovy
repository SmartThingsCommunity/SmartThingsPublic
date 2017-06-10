metadata {
  definition (name: "Fibaro Universal Sensor", namespace: "Carlos", author: "Carlos") {
    capability "Sensor"
    command "report"
    command "singleSet"
    command "singleRemove"
    command "multiSet"
    command "multiRemove"
    fingerprint deviceId: "0x2001", inClusters: "0x30 0x60 0x85 0x8E 0x72 0x70 0x86 0x7A 0xEF 0x2B"
  }

  simulator {
           // These show up in the IDE simulator "messages" drop-down to test
           // sending event messages to your device handler
		    status "open"  : "zw device: 02, command: 2001, payload: 00"
    		status "closed": "zw device: 02, command: 2001, payload: FF"
			status "basic report on":
                   zwave.basicV1.basicReport(value:0xFF).incomingMessage()
            status "basic report off":
                   zwave.basicV1.basicReport(value:0).incomingMessage()
            status "dimmer switch on at 70%":
                   zwave.switchMultilevelV1.switchMultilevelReport(value:70).incomingMessage()
            status "basic set on":
                   zwave.basicV1.basicSet(value:0xFF).incomingMessage()
            status "temperature report 70°F":
                   zwave.sensorMultilevelV2.sensorMultilevelReport(scaledSensorValue: 70.0, precision: 1, sensorType: 1, scale: 1).incomingMessage()
            status "low battery alert":
                   zwave.batteryV1.batteryReport(batteryLevel:0xFF).incomingMessage()
            status "multichannel sensor":
                   zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:1).encapsulate(zwave.sensorBinaryV1.sensorBinaryReport(sensorValue:0)).incomingMessage()
            // simulate turn on
            reply "2001FF,delay 5000,2002": "command: 2503, payload: FF"

            // simulate turn off
            reply "200100,delay 5000,2002": "command: 2503, payload: 00"
  }

  tiles {
    standardTile("Gate", "device.Gate", width: 2, height: 2) {
      state "open",   label: "Gate Open", icon: "st.doors.garage.garage-open",   backgroundColor: "#ff5050"
      state "closed", label: "Gate Closed", icon: "st.doors.garage.garage-closed", backgroundColor: "#79b821"
      state "unknonw", label: '${name}', icon: "st.transportation.transportation13", backgroundColor: "#6495ed"
    }
    standardTile("Switch1", "device.Switch1", width: 1, height: 1) {
      state "open",   label: "Not Open", icon: "st.contact.contact.open",   backgroundColor: "#ffa81e"
      state "closed", label: "Open", icon: "st.contact.contact.closed", backgroundColor: "#79b821"
    }
    standardTile("Switch2", "device.Switch2", width: 1, height: 1) {
      state "open",   label: "Not Closed", icon: "st.contact.contact.open",   backgroundColor: "#ffa81e"
      state "closed", label: "Closed", icon: "st.contact.contact.closed", backgroundColor: "#79b821"
    }
    
    main(["Gate","Switch1","Switch2"])
    details(["Gate","Switch1","Switch2"])
  }
}

def parse(String description)
{
  def result = null
  def cmd = zwave.parse(description, [ 0x60: 3])
  if (cmd) {
  	result = zwaveEvent(cmd)
  }
  log.debug "parsed '$description' to result: ${result}"
  result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv1.ManufacturerSpecificReport cmd) {
  log.debug("ManufacturerSpecificReport ${cmd.inspect()}")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
  log.debug("ConfigurationReport ${cmd.inspect()}")
}

def report() {
  // zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
  //
  delayBetween([
    zwave.configurationV1.configurationGet(parameterNumber: 5).format(),
    zwave.configurationV1.configurationGet(parameterNumber: 6).format()
  ])
}

def configTest() {
  log.debug "configTest"
  zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
  //zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
  zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
}

def singleSet() {
  def cmds = []
  //cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
  //cmds << zwave.associationV2.associationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
  cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
  delayBetween(cmds, 500)
}

def singleRemove() {
  def cmds = []
  //cmds << zwave.associationV2.associationRemove(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
  //cmds << zwave.associationV2.associationRemove(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
  cmds << zwave.associationV2.associationRemove(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
  delayBetween(cmds, 500)
}

def multiSet() {
  def cmds = []
  //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
  //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
  cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
  delayBetween(cmds, 500)
}

def multiRemove() {
  def cmds = []
  //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:1, nodeId:[zwaveHubNodeId]).format()
  //cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:2, nodeId:[zwaveHubNodeId]).format()
  cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
  delayBetween(cmds, 500)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
  log.debug "BasicSet V1 ${cmd.inspect()}"
  if (cmd.value) {
    createEvent(name: "Switch1", value: "open", descriptionText: "$device.displayName is open")
  } else {
    createEvent(name: "Switch1", value: "closed", descriptionText: "$device.displayName is closed")
  }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	log.debug "ZWaveEvent V3 ${cmd.inspect()}"
	def evt1
	def evt2
    if (cmd.commandClass == 32) {
       if (cmd.parameter == [0]) {
          if (cmd.sourceEndPoint == 1) {
              evt1 = createEvent(name: "Switch1", value: "closed", descriptionText: "$device.displayName is closed")
              evt2 = createEvent(name: "Gate", value: "open", descriptionText: "$device.displayName is closed")
              log.debug "Switch 1 closed"
         }
         else
         if (cmd.sourceEndPoint == 2) {
              evt1 = createEvent(name: "Switch2", value: "closed", descriptionText: "$device.displayName is closed")
              evt2 = createEvent(name: "Gate", value: "closed", descriptionText: "$device.displayName is closed")
              log.debug "Switch 2 closed"
         }
       }
       if (cmd.parameter == [255]) {
          if (cmd.sourceEndPoint == 1) {
              evt1 = createEvent(name: "Switch1", value: "open", descriptionText: "$device.displayName is open")
              evt2 = createEvent(name: "Gate", value: "unknonw", descriptionText: "$device.displayName is closed")
              log.debug "Switch 1 open"
          }
       else
          if (cmd.sourceEndPoint == 2) {
              evt1 = createEvent(name: "Switch2", value: "open", descriptionText: "$device.displayName is open")
              evt2 = createEvent(name: "Gate", value: "unknonw", descriptionText: "$device.displayName is closed")
              log.debug "Switch 2 open"
          }
       }
    }
    return [evt1, evt2]

}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}
