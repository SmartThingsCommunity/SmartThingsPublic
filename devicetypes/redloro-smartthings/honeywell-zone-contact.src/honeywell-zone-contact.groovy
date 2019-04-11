/**
 *  SmartThings Device Handler: Honeywell Zone Contact
 *
 *  Author: redloro@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
  definition (name: "Honeywell Zone Contact", namespace: "redloro-smartthings", author: "redloro@gmail.com") {
    capability "Contact Sensor"
    capability "Sensor"

    command "zone"
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"zone", type: "generic", width: 6, height: 4){
      tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
        attributeState "closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821"
        attributeState "open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e"
        attributeState "alarm", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ff0000"
      }
    }

    main "zone"

    details(["zone"])
  }
}

def zone(String state) {
  def descMap = [
    'closed':"Was Closed",
    'open':"Was Opened",
    'alarm':"Alarm Triggered"
  ]
  def desc = descMap."${state}"

  sendEvent (name: "contact", value: "${state}", descriptionText: "${desc}")
}