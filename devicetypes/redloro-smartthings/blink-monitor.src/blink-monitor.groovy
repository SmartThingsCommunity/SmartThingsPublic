/**
 *  SmartThings Device Handler: Blink Monitor
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
  definition (name: "Blink Monitor", namespace: "redloro-smartthings", author: "redloro@gmail.com") {
    capability "Switch"
    capability "Refresh"
    capability "Polling"
    capability "Sensor"
    capability "Actuator"
    
    command "monitor"
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"state", type:"generic", width:6, height:4) {
      tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "off", label:'Disarmed', action:"switch.on", icon:"st.camera.dropcam", backgroundColor:"#ffffff"
        attributeState "on", label:'Armed', action:"switch.off", icon:"st.camera.dropcam", backgroundColor:"#79b821"
      }
    }

    standardTile("refresh", "device.status", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh", backgroundColor:"#ffffff"
    }

    // Defines which tile to show in the overview
    main "state"

    // Defines which tile(s) to show when user opens the detailed view
    details([
      "state",
      "refresh"
    ])
  }
}

/**************************************************************************
 * The following section simply maps the actions as defined in
 * the metadata into onAction() calls.
 *
 * This is preferred since some actions can be dealt with more
 * efficiently this way. Also keeps all user interaction code in
 * one place.
 *
 */
def on() {
  parent.setStatus("on")
}
def off() {
  parent.setStatus("off")
}
def refresh() {
  parent.getStatus()
}
def monitor(status) {
  // need to convert open to detected and closed to clear
  def descMap = [
    'on':"Was Armed",
    'off':"Was Disarmed"
  ]
  def desc = descMap."${status}"
  sendEvent(name: "switch", value: "${status}", descriptionText: "${desc}")
}
/**************************************************************************/

/**
 * Called every so often (every 5 minutes actually) to refresh the
 * tiles so the user gets the correct information.
 */
def poll() {
  refresh()
}