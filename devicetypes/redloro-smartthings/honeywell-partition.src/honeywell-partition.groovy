/**
 *  SmartThings Device Handler: Honeywell Partition
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
  definition (name: "Honeywell Partition", namespace: "redloro-smartthings", author: "redloro@gmail.com") {
    capability "Button"
    capability "Alarm"

    command "partition"
    command "disarm"
    command "armStay"
    command "armAway"
    command "armInstant"
    command "chime"
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"partition", type: "generic", width: 6, height: 4) {
      tileAttribute ("device.dscpartition", key: "PRIMARY_CONTROL") {
        attributeState "ready", label: 'Ready', icon:"st.Home.home2"
        attributeState "notready", label: 'Not Ready', backgroundColor: "#ffcc00", icon:"st.Home.home2"
        attributeState "arming", label: 'Arming', backgroundColor: "#ffcc00", icon:"st.Home.home3"
        attributeState "armedstay", label: 'Armed Stay', backgroundColor: "#79b821", icon:"st.Home.home3"
        attributeState "armedaway", label: 'Armed Away', backgroundColor: "#79b821", icon:"st.Home.home3"
        attributeState "armedinstant", label: 'Armed Instant Stay', backgroundColor: "#79b821", icon:"st.Home.home3"
        attributeState "armedmax", label: 'Armed Instant Away', backgroundColor: "#79b821", icon:"st.Home.home3"
        attributeState "alarmcleared", label: 'Alarm in Memory', backgroundColor: "#ffcc00", icon:"st.Home.home2"
        attributeState "alarm", label: 'Alarm', backgroundColor: "#ff0000", icon:"st.Home.home3"
      }
      tileAttribute ("panelStatus", key: "SECONDARY_CONTROL") {
        attributeState "panelStatus", label:'${currentValue}'
      }
    }

    standardTile("armAwayButton","device.button", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
      state "default", label: 'Away', action: "armAway", icon: "st.Home.home3", backgroundColor: "#79b821"
    }

    standardTile("armStayButton","device.button", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
      state "default", label: 'Stay', action: "armStay", icon: "st.Home.home3", backgroundColor: "#79b821"
    }

    standardTile("disarmButton","device.button", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
      state "default", label: 'Disarm', action: "disarm", icon: "st.Home.home2", backgroundColor: "#C0C0C0"
    }

    standardTile("armInstantButton","device.button", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
      state "default", label: 'Instant', action: "armInstant", icon: "st.Home.home3", backgroundColor: "#79b821"
    }

    standardTile("chimeButton","device.button", width: 2, height: 2, canChangeIcon: true, decoration: "flat") {
      state "default", label: 'Chime', action: "chime", icon: "st.custom.sonos.unmuted", backgroundColor: "#79b821"
    }

    main "partition"

    details(["partition",
             "armAwayButton", "armStayButton", "disarmButton",
             "armInstantButton", "chimeButton"])
  }
}

def partition(String state, String alpha) {
  sendEvent (name: "dscpartition", value: "${state}", descriptionText: "${alpha}")
  sendEvent (name: "panelStatus", value: "${alpha}", displayed: false)
}

def disarm() {
    parent.sendCommand('/plugins/envisalink/disarm');
}

def armAway() {
    parent.sendCommand('/plugins/envisalink/armAway');
}

def armStay() {
    parent.sendCommand('/plugins/envisalink/armStay');
}

def armInstant() {
    parent.sendCommand('/plugins/envisalink/armInstant');
}

def chime() {
    parent.sendCommand('/plugins/envisalink/chime');
}
