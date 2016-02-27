/*
 *  DSC 4-Wire Smoke Device
 *
 *  Author: Jordan <jordan@xeron.cc>
 *  Originally by: Matt Martz <matt.martz@gmail.com>
 *  Modified by: Kent Holloway <drizit@gmail.com>
 *  Date: 2016-02-27
 */

// for the UI
metadata {
  definition (name: "DSC Zone Smoke 4w", author: "jordan@xeron.cc") {
    // Change or define capabilities here as needed
    capability "Smoke Detector"
    capability "Sensor"

    // Add commands as needed
    command "zone"
  }

  simulator {
    // Nothing here, you could put some testing stuff here if you like
  }

  tiles {
    // Main Row
    standardTile("zone", "device.smoke", width: 2, height: 2, canChangeBackground: true, canChangeIcon: true) {
      state "clear",  label: 'clear',  icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff"
      state "smoke",  label: 'SMOKE',  icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13"
      state "tested", label: 'TESTED', icon: "st.alarm.smoke.test",  backgroundColor: "#e86d13"
    }

    standardTile("tamper", "device.tamper", width: 2, height: 2, canChangeBackground: true, canChangeIcon: true) {
      state "clear",   label: 'No Tamper', icon: "st.contact.contact.closed",   backgroundColor: "#79b821"
      state "detected", label: 'Tamper', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
    }

    // This tile will be the tile that is displayed on the Hub page.
    main "zone"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details(["zone","tamper"])
  }
}

// handle commands
def zone(String state) {
  // state will be a valid state for a zone (open, closed)
  // zone will be a number for the zone
  log.debug "Zone: ${state}"

  def tamperMap = [
    'fault':"detected",
    'restore':"clear",
  ]

  if (tamperMap[state]) {
    def tamperState = tamperMap."${state}"
    // Send final event
    sendEvent (name: "tamper", value: "${tamperState}")
  } else {
    // Since this is a smoke device we need to convert open to test and closed to clear
    // before sending the event
    def eventMap = [
     'open':"tested",
     'closed':"clear",
     'alarm':"smoke"
    ]
    def newState = eventMap."${state}"
    // Send final event
    sendEvent (name: "smoke", value: "${newState}")
  }
}
