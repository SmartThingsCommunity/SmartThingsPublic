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
  definition (name: "Konnected Siren/Strobe", namespace: "konnected-io", author: "konnected.io") {
    capability "Alarm"
    capability "Switch"
    capability "Actuator"
  }
  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
        attributeState ("off",  label: "Off",    icon:"st.security.alarm.clear", action:"alarm.both", backgroundColor:"#ffffff")
        attributeState ("both", label: "Alarm!", icon:"st.security.alarm.alarm", action:"alarm.off",  backgroundColor:"#e86d13")
      }
    }
    main "main"
    details "main"
  }
}

def off() { 
  sendEvent([name: "switch", value: "off", displayed: false])
  sendEvent([name: "alarm", value: "off"])
  parent.deviceUpdateDeviceState(device.deviceNetworkId, 0)
}

def on() {
  sendEvent([name: "switch", value: "on", displayed: false])
  sendEvent([name: "alarm", value: "both"])
  parent.deviceUpdateDeviceState(device.deviceNetworkId, 1)
}

def both() { on() }

def strobe() { on() }

def siren() { on() }