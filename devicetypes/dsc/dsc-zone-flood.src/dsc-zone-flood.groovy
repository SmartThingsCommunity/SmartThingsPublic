/*
 *  DSC Zone Flood Device
 *
 *  Author: Jordan <jordan@xeron.cc>
 *  Original Author: Matt Martz <matt.martz@gmail.com>
 *  Modified to be a motion device: Kent Holloway <drizit@gmail.com>
 *  Date: 2016-02-27
 */

// for the UI
metadata {
  definition (name: "DSC Zone Flood", author: "jordan@xeron.cc", namespace: 'dsc') {
    // Change or define capabilities here as needed
    capability "Water Sensor"
    capability "Sensor"
    capability "Momentary"

    // Add commands as needed
    command "zone"
    command "bypass"
  }

  simulator {
    // Nothing here, you could put some testing stuff here if you like
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"zone", type: "generic", width: 6, height: 4){
      tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
        attributeState "wet", label:'wet', icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
        attributeState "dry", label:'dry', icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
        attributeState "alarm", label:'ALARM', icon:"st.alarm.water.wet", backgroundColor:"#ff0000"
      }
    }
    standardTile ("trouble", "device.trouble", width: 3, height: 2, title: "Trouble") {
      state "restore", label: 'No\u00A0Trouble', icon: "st.security.alarm.clear"
      state "tamper", label: 'Tamper', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
      state "fault", label: 'Fault', icon: "st.security.alarm.alarm", backgroundColor: "#ff1e1e"
    }
    standardTile("bypass", "capability.momentary", width: 3, height: 2, title: "Bypass", decoration: "flat"){
      state "bypass", label: 'Bypass', action: "bypass", icon: "st.locks.lock.unlocked"
    }

    // This tile will be the tile that is displayed on the Hub page.
    main "zone"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details(["zone", "trouble", "bypass"])
  }
}

// handle commands
def bypass() {
  def zone = device.deviceNetworkId.minus('dsczone')
  parent.sendUrl("bypass?zone=${zone}")
}

def push() {
  bypass()
}

def zone(String state) {
  // state will be a valid state for a zone (open, closed)
  // zone will be a number for the zone
  log.debug "Zone: ${state}"

  def troubleList = ['fault','tamper','restore']

  if (troubleList.contains(state)) {
    // Send final event
    sendEvent (name: "trouble", value: "${state}")
  } else {
    // Since this is a water sensor device we need to convert open to active and closed to inactive
    // before sending the event
    def eventMap = [
     'open':"wet",
     'closed':"dry",
     'alarm':"alarm"
    ]
    def newState = eventMap."${state}"
    // Send final event
    sendEvent (name: "water", value: "${newState}")
  }
}
