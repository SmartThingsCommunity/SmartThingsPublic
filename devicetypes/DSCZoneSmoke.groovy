/*
 *  DSC Smoke Device (wireless & 4-wire zone-attached types only)
 *
 *  Author: Jordan <jordan@xeron.cc>
 *  Originally by: Matt Martz <matt.martz@gmail.com>
 *  Modified by: Kent Holloway <drizit@gmail.com>
 *  Date: 2016-02-27
 */

// for the UI
metadata {
  definition (name: "DSC Zone Smoke", author: "jordan@xeron.cc", namespace: 'dsc') {
    // Change or define capabilities here as needed
    capability "Smoke Detector"
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
    standardTile ("zone", "device.smoke", width: 4, height: 4, title: "Zone") {
      state "clear",  label: 'clear',  icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff"
      state "detected",  label: 'SMOKE',  icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13"
      state "tested", label: 'TESTED', icon: "st.alarm.smoke.test",  backgroundColor: "#e86d13"
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
    // Since this is a smoke device we need to convert the values to match the device capabilities
    // before sending the event
    def eventMap = [
     'open':"tested",
     'closed':"clear",
     'alarm':"detected"
    ]
    def newState = eventMap."${state}"
    // Send final event
    sendEvent (name: "smoke", value: "${newState}")
  }
}
