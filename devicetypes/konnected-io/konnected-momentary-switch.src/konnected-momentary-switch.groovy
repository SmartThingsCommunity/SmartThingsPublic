/**
 *  Konnected Switch
 *
 *  Copyright 2017 konnected.io
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
  definition (name: "Konnected Momentary Switch", namespace: "konnected-io", author: "konnected.io", mnmn: "SmartThings", vid: "generic-switch") {
    capability "Switch"
    capability "Actuator"
    capability "Momentary"
  }

  preferences {
    input name: "invertTrigger", type: "bool", title: "Low Level Trigger",
          description: "Select if the attached relay uses a low-level trigger. Default is high-level trigger"
    input name: "momentaryDelay", type: "number", title: "Momentary Delay",
          description: "Off delay (in milliseconds)"
  }

  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "pushed"
        attributeState "on", label: 'Push', action: "switch.off", backgroundColor: "#00a0dc"
        attributeState "pushed", label:'pushed', action: "switch.off", backgroundColor:"#00a0dc", nextState: "off"
      }
    }
    main "main"
    details "main"
  }
}

def updated() {
  parent.updateSettingsOnDevice()
}

def updatePinState(Integer state) {
  def off = invertTrigger ? 1 : 0
  if (state == off) {
    sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
  } else {
    sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    def delaySeconds = (momentaryDelay ?: 1000) / 1000 as Integer
    runIn(Math.max(delaySeconds, 1), switchOff)
  }
}

def switchOff() {
  sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
}

def off() {
  def val = invertTrigger ? 1 : 0
  parent.deviceUpdateDeviceState(device.deviceNetworkId, val)
}

def on() {
  push()
}

def push() {
  def val = invertTrigger ? 0 : 1
  parent.deviceUpdateDeviceState(device.deviceNetworkId, val, [
    momentary : momentaryDelay ?: 500
  ])
}

def triggerLevel() {
  return invertTrigger ? 0 : 1
}

def currentBinaryValue() {
  if (device.currentValue('switch') == 'on') {
    invertTrigger ? 0 : 1
  } else {
    invertTrigger ? 1 : 0
  }
}