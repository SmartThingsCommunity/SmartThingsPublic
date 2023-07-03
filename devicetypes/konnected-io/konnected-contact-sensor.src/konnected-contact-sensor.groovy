/**
 *  Konnected Contact Sensor
 *
 *  Copyright 2018 Konnected Inc (https://konnected.io)
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
  definition (name: "Konnected Contact Sensor", namespace: "konnected-io", author: "konnected.io") {
    capability "Contact Sensor"
    capability "Sensor"
  }

  preferences {
    input name: "normalState", type: "enum", title: "Normal State",
      options: ["Normally Closed", "Normally Open"],
      defaultValue: "Normally Open",
      description: "Most door & window sensors are Normally Open (NO), meaning that the circuit closes when the door/window is closed. To reverse this logic, select Normally Closed (NC)."
  }

  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
        attributeState ("closed", label: "Closed", icon:"st.contact.contact.closed", backgroundColor:"#00a0dc")
        attributeState ("open",   label: "Open",   icon:"st.contact.contact.open",   backgroundColor:"#e86d13")
      }
    }
    main "main"
    details "main"
  }
}

def isClosed() {
  normalState == "Normally Closed" ? "open" : "closed"
}

def isOpen() {
  normalState == "Normally Closed" ? "closed" : "open"
}

// Update state sent from parent app
def setStatus(state) {
  def stateValue = state == "1" ? isOpen() : isClosed()
  sendEvent(name: "contact", value: stateValue)
  log.debug "$device.label is $stateValue"
}