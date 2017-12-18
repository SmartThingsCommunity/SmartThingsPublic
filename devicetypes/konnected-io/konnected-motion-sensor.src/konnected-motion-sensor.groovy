/**
 *  Konnected Motion Sensor
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
  definition (name: "Konnected Motion Sensor", namespace: "konnected-io", author: "konnected.io") {
    capability "Motion Sensor"
    capability "Sensor"
  }
  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
        attributeState ("inactive", label: "No Motion", icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
        attributeState ("active",   label: "Motion",    icon:"st.motion.motion.active",   backgroundColor:"#00a0dc")
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
      sendEvent(name: "motion", value: "inactive")
      log.debug "$device.label motion inactive"
      break
    case "1" :
      sendEvent(name: "motion", value: "active") 
      log.debug "$device.label motion detected"
      break
    default:
      sendEvent(name: "motion", value: "inactive") 
      break
  }
}