/*
 *  Copyright 2020 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

// Modification from template to enable IKEA Styrbar 4 buttons remote
// 2021-08-28 		0.01  minimum functionnlity release. Up/down push and hold, left/right push only
// 2021-08-30 		0.02  some house keeping and logging changes
// 2021-09-05 		0.03  apparently not working as it should, some additionnal logging updates
//
//

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType

metadata {
  definition(name: "IKEA Styrbar remote", namespace: "scubaandre", author: "André Parent", ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true, runLocally: false, executeCommandsLocally: false, mnmn: "SmartThings", vid: "generic-4-button") {
    capability "Actuator"
    capability "Battery"
    capability "Button"
    capability "Holdable Button"
    capability "Configuration"
    capability "Sensor"
    capability "Health Check"

    fingerprint inClusters: "0000, 0001, 0003, 0020, 1000, FC57", outClusters: "0003, 0006, 0008, 0019, 1000", manufacturer: "IKEA of Sweden", model: "Remote Control N2", deviceJoinName: "IKEA Styrbar remote" //IKEA Styrbar 4-button remote
  }

  simulator {}

  tiles {
    standardTile("button", "device.button", width: 2, height: 2) {
      state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
      state "button 1 pushed", label: "pushed #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#00A0DC"
    }

    valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
      state "battery", label: '${currentValue}% battery', unit: ""
    }

    main(["button"])
    details(["button", "battery"])
  }
}

//private getCLUSTER_GROUPS() {
//  0x0004
//}

private channelNumber(String dni) {
  dni.split(":")[-1] as Integer
}

private getButtonName(buttonNum) {
  return "${device.displayName} " + "Button ${buttonNum}"
}

def updated() {
  log.debug "updated() called"
  if (childDevices && device.label != state.oldLabel) {
    childDevices.each {
      def newLabel = getButtonName(channelNumber(it.deviceNetworkId))
      it.setLabel(newLabel)
    }
    state.oldLabel = device.label
  }
}

def configure() {
  log.debug "configure() called"
  log.debug "Configuring device ${device.getDataValue("model ")}"

  def cmds = zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x21, DataType.UINT8, 30, 21600, 0x01) +
    zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x21) +
    zigbee.addBinding(zigbee.ONOFF_CLUSTER) //+

  cmds
}

def installed() {
  log.debug "installed() called"
  // forcing 4 buttons 
  def numberOfButtons = 4
  createChildButtonDevices(numberOfButtons)
  sendEvent(name: "supportedButtonValues", value: ["pushed", "held"].encodeAsJSON(), displayed: false)
  sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
  numberOfButtons.times {
    sendEvent(name: "button", value: "pushed", data: [buttonNumber: it + 1], displayed: false)
  }

  // These devices don't report regularly so they should only go OFFLINE when Hub is OFFLINE
  sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme: "untracked"]), displayed: false)
  sendEvent(name: "lastButtonState", value: "released", displayed: false)
}

private void createChildButtonDevices(numberOfButtons) {
  log.debug "createChildButtonDevices() called"
  state.oldLabel = device.label
  def existingChildren = getChildDevices()

  log.debug "Creating ${numberOfButtons} children"

  for (i in 1..numberOfButtons) {
    def newChildNetworkId = "${device.deviceNetworkId}:${i}"
    def childExists = (existingChildren.find {child -> child.getDeviceNetworkId() == newChildNetworkId} != NULL)

    if (!childExists) {
      log.debug "Creating child $i"
      def child = addChildDevice("smartthings", "Child Button", newChildNetworkId, device.hubId,
        [completedSetup: true, label: getButtonName(i),
          isComponent: true, componentName: "button$i", componentLabel: "Button ${i}"
        ])
      // left and right button only support one hold event, same for both
      if (i == 3 || i == 4) {
        child.sendEvent(name: "supportedButtonValues", value: ["pushed"].encodeAsJSON(), displayed: false)
      } else {
        child.sendEvent(name: "supportedButtonValues", value: ["pushed", "held"].encodeAsJSON(), displayed: false)
      }
      child.sendEvent(name: "numberOfButtons", value: 1, displayed: false)
      child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
    } else {
      log.debug "Child $i already exists, not creating"
    }
  }
}


def parse(String description) {
  log.debug "${device.displayName} parsing: $description"
  def event = zigbee.getEvent(description) 
  
  if (event) {
    log.debug "Creating event: ${event}"
    sendEvent(event)
  } else {
    if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
      def descMap = zigbee.parseDescriptionAsMap(description)
      if (descMap.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.attrInt == 0x0021) {
        event = getBatteryEvent(zigbee.convertHexToInt(descMap.value))
      } else if (descMap.clusterInt == zigbee.ONOFF_CLUSTER ||
        descMap.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER ||
        descMap.clusterInt == 0x05) { // Scene
        event = getButtonEvent(descMap)
      }
    }

    def result = []
    if (event) {
      log.debug "Creating event: ${event}"
      result = createEvent(event)
    }

    return result
  }
}

private Map getBatteryEvent(value) {
  def result = [: ]
  result.value = value / 2
  result.name = 'battery'
  result.descriptionText = "${device.displayName} battery was ${result.value}%"
  return result
}

private sendButtonEvent(buttonNumber, buttonState) {
  def child = childDevices?.find {channelNumber(it.deviceNetworkId) == buttonNumber}

  if (child) {
    def descriptionText = "$child.displayName was $buttonState" // TODO: Verify if this is needed, and if capability template already has it handled
	child?.sendEvent([name: "button", value: buttonState, data: [buttonNumber: 1], descriptionText: descriptionText, isStateChange: true])
  } else {
    log.debug "Child device $buttonNumber not found!"
  }
}

private Map getButtonEvent(Map descMap) {
  def buttonState = ""
  def buttonNumber = 0
  Map result = [: ]
  //  log.debug "cluster $descMap.clusterInt" + "command $descMap.commandInt" + "data $descMap.data"

  // Button 1 up
  if (descMap.clusterInt == zigbee.ONOFF_CLUSTER && descMap.commandInt == 0x01) { //on
    buttonNumber = 1
    buttonState = "pushed"
  }
  // Button 2 down
  else if (descMap.clusterInt == zigbee.ONOFF_CLUSTER && descMap.commandInt == 0x00) { //off
    buttonNumber = 2
    buttonState = "pushed"
  }
  // Button 3 left
  else if (descMap.clusterInt == 0x0005 && descMap.commandInt == 0x07 && descMap.data[0] == "01") {
    buttonNumber = 3
    buttonState = "pushed"
  }
  // Button 4 right
  else if (descMap.clusterInt == 0x0005 && descMap.commandInt == 0x07 && descMap.data[0] == "00") {
    buttonNumber = 4
    buttonState = "pushed"
  }

  // Button Hold
  else if (descMap.clusterInt == 0X0008 && (descMap.commandInt == 0x05 || descMap.commandInt == 0x01)) {
    if (descMap.data[0] == "00") {
      buttonNumber = 1
      buttonState = "held"
    } else if (descMap.data[0] == "01") {
      buttonNumber = 2
      buttonState = "held"
    }

  }
    // Button release
  else if (descMap.clusterInt == 0X0008 && (descMap.commandInt == 0x07)) {
    log.info "Button released"
    
  }

  if (buttonNumber != 0) {
    // Create and send component event
    sendButtonEvent(buttonNumber, buttonState)
    log.info "Button ${buttonNumber} ${buttonState}"
  }
  result
}
