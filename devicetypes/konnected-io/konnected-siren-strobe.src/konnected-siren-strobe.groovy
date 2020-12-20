/**
 *  Konnected Siren/Strobe
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
  definition (name: "Konnected Siren/Strobe", namespace: "konnected-io", author: "konnected.io", mnmn: "SmartThings", vid: "generic-siren") {
    capability "Alarm"
    capability "Switch"
    capability "Actuator"
  }

  preferences {
  	input name: "invertTrigger", type: "bool", title: "Low Level Trigger",
  	      description: "Select if the attached relay uses a low-level trigger. Default is high-level trigger"
  }

  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
        attributeState ("off",  label: "Off",    icon:"st.security.alarm.clear", action:"alarm.both", backgroundColor:"#ffffff", nextState: "turningOn")
        attributeState ("both", label: "Alarm!", icon:"st.security.alarm.alarm", action:"alarm.off",  backgroundColor:"#e86d13", nextState: "turningOff")
        attributeState ("turningOn", label:'Activating', icon:"st.security.alarm.alarm", action:"alarm.off", backgroundColor:"#e86d13", nextState: "turningOff")
        attributeState ("turningOff", label:'Turning off', icon:"st.security.alarm.clear", action:"alarm.on", backgroundColor:"#ffffff", nextState: "turningOn")
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
  sendEvent(name: "switch", value: val, displayed: false)
  if (val == "on") { val = "both" }
  sendEvent(name: "alarm", value: val)
}

def off() {
  def val = invertTrigger ? 1 : 0
  parent.deviceUpdateDeviceState(device.deviceNetworkId, val)
}

def on() {
  def val = invertTrigger ? 0 : 1
  parent.deviceUpdateDeviceState(device.deviceNetworkId, val)
}

def both() { on() }

def strobe() { on() }

def siren() { on() }

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