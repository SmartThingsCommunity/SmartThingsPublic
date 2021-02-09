/**
 *  Konnected Panic Button
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
  definition (name: "Konnected Panic Button", namespace: "konnected-io", author: "konnected.io") {
    capability "Switch"
    capability "Sensor"
  }
  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState ("off", label: "Off",    icon:"st.illuminance.illuminance.dark",  backgroundColor:"#ffffff")
        attributeState ("on",  label: "Panic!", icon:"st.illuminance.illuminance.light", backgroundColor:"#e86d13")
      }
    }
    main "main"
    details "main"
  }
}

//Update state sent from parent app
def setStatus(state) { 
  switch(state) {
    case "0" :
      sendEvent(name: "switch", value: "off") 
      break
    case "1" :
      sendEvent(name: "switch", value: "on") 
      break
    default:
      sendEvent(name: "switch", value: "on") 
      break
  }
}