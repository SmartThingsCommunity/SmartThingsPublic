/**
 *  Konnected Water Leak Sensor
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
  definition (name: "Konnected Water Sensor", namespace: "konnected-io", author: "konnected.io") {
    capability "Water Sensor"
    capability "Sensor"
  }
  preferences {
    input name: "normalState", type: "enum", title: "Normal State",
      options: ["Normally Closed", "Normally Open"],
      defaultValue: "Normally Open",
      description: "Most leak sensors indicate water when the circuit is closed (NO). Select Normally Closed (NC) to reverse this logic."
  }
  tiles {
    multiAttributeTile(name:"main", type: "generic", width: 6, height: 4, canChangeIcon: true) {
      tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
      	attributeState ("dry", label: "Dry", icon: "st.alarm.water.dry", backgroundColor: "#ffffff")
		    attributeState ("wet", label: "Wet", icon: "st.alarm.water.wet", backgroundColor: "#00A0DC")
      }
    }
    main "main"
    details "main"
  }
}

def isClosed() {
  normalState == "Normally Closed" ? "dry" : "wet"
}

def isOpen() {
  normalState == "Normally Closed" ? "wet" : "dry"
}

//Update state sent from parent app
def setStatus(state) {
  def stateValue = state == "1" ? isOpen() : isClosed()
  sendEvent(name: "water", value: stateValue)
  log.debug "$device.label is $stateValue"
}