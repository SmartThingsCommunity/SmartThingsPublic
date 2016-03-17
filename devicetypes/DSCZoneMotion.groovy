/*
 *  DSC Zone Motion Device
 *
 *  Author: Jordan <jordan@xeron.cc>
 *  Original Author: Matt Martz <matt.martz@gmail.com>
 *  Modified to be a motion device: Kent Holloway <drizit@gmail.com>
 *  Date: 2016-02-27
 */

// for the UI
metadata {
  definition (name: "DSC Zone Motion", author: "jordan@xeron.cc", namespace: 'dsc') {
    // Change or define capabilities here as needed
    capability "Motion Sensor"
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
    standardTile ("zone", "device.motion", width: 4, height: 4, title: "Zone") {
      state "active",   label:'motion',    icon:"st.motion.motion.active",   backgroundColor:"#53a7c0"
      state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
      state "alarm",    label:'ALARM',     icon:"st.motion.motion.active",   backgroundColor:"#ff0000"
    }
    standardTile ("trouble", "device.trouble", width: 2, height: 2, title: "Trouble") {
      state "restore", label: 'No\u00A0Trouble', icon: "st.security.alarm.clear", backgroundColor: "#79b821"
      state "tamper", label: 'Tamper', icon: "st.security.alarm.alarm", backgroundColor: "#ffa81e"
      state "fault", label: 'Fault', icon: "st.security.alarm.alarm", backgroundColor: "#ff1e1e"
    }
    standardTile("bypass", "capability.momentary", width: 2, height: 2, title: "Bypass"){
      state "bypass", label: 'Bypass', action: "bypass", icon: "st.locks.lock.unlocked", backgroundColor: "#FFFF00"
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
    // Since this is a motion sensor device we need to convert open to active and closed to inactive
    // before sending the event
    def eventMap = [
     'open':"active",
     'closed':"inactive",
     'alarm':"alarm"
    ]
    def newState = eventMap."${state}"
    // Send final event
    sendEvent (name: "motion", value: "${newState}")
  }
}
