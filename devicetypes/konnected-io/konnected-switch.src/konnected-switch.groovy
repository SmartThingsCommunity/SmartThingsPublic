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
  definition (name: "Konnected Switch", namespace: "konnected-io", author: "konnected.io", mnmn: "SmartThings", vid: "generic-switch") {
    capability "Switch"
    capability "Actuator"
  }

  preferences {
    input name: "invertTrigger", type: "bool", title: "Low Level Trigger",
          description: "Select if the attached relay uses a low-level trigger. Default is high-level trigger"
  }

  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState ("off",  label: '${name}',    icon:"st.switches.switch.off", action:"switch.on",   backgroundColor:"#ffffff", nextState: "turningOn")
        attributeState ("on",   label: '${name}',    icon:"st.switches.switch.on",  action:"switch.off",  backgroundColor:"#00A0DC", nextState: "turningOff")
        attributeState ("turningOn", label:'Turning on', icon:"st.switches.switch.on", action:"switch.off", backgroundColor:"#00a0dc", nextState: "turningOff")
        attributeState ("turningOff", label:'Turning off', icon:"st.switches.switch.off", action:"switch.on", backgroundColor:"#ffffff", nextState: "turningOn")
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
  def val
  if (state == 0) {
    val = invertTrigger ? "on" : "off"
  } else {
    val = invertTrigger ? "off" : "on"
  }
  log.debug "$device is $val"
  sendEvent(name: "switch", value: val)
}

def off() {
  def val = invertTrigger ? 1 : 0
  log.debug "Turning off $device.label (state = $val)"
  parent.deviceUpdateDeviceState(device.deviceNetworkId, val)
}

def on() {
  def val = invertTrigger ? 0 : 1
  log.debug "Turning on $device.label (state = $val)"
  parent.deviceUpdateDeviceState(device.deviceNetworkId, val)
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