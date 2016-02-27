/*
 *  DSC Zone Contact Device
 *
 *  Author: Jordan <jordan@xeron.cc>
 *  Original Author: Matt Martz <matt.martz@gmail.com>
 *  Date: 2016-02-27
 */

// for the UI
metadata {
  definition (name: "DSC Zone Contact", author: "jordan@xeron.cc") {
    // Change or define capabilities here as needed
    capability "Contact Sensor"
    capability "Sensor"

    // Add commands as needed
    command "zone"
  }

  simulator {
    // Nothing here, you could put some testing stuff here if you like
  }

  tiles {
    // Main Row
    standardTile("zone", "device.contact", width: 2, height: 2, canChangeBackground: true, canChangeIcon: true) {
      state "open",   label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#ffa81e"
      state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
      state "alarm",  label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#ff0000"
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
    sendEvent (name: "contact", value: "${state}")
  }
}
