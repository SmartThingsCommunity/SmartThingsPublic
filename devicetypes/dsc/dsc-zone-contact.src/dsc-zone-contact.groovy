/*
 *  DSC Zone Contact Device
 *
 *  Author: Jordan <jordan@xeron.cc>
 *  Original Author: Matt Martz <matt.martz@gmail.com>
 *  Date: 2016-02-27
 */

// for the UI
metadata {
  definition (name: "DSC Zone Contact", author: "jordan@xeron.cc", namespace: 'dsc') {
    // Change or define capabilities here as needed
    capability "Contact Sensor"
    capability "Sensor"
    capability "Momentary"
    attribute "bypass", "string"
    attribute "trouble", "string"

    // Add commands as needed
    command "zone"
    command "bypass"
  }

  simulator {
    // Nothing here, you could put some testing stuff here if you like
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"zone", type: "generic", width: 6, height: 4) {
      tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
        attributeState "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ffa81e"
        attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
        attributeState "alarm", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#ff0000"
      }
      tileAttribute ("device.trouble", key: "SECONDARY_CONTROL") {
        attributeState "restore", label: 'No Trouble', icon: "st.security.alarm.clear"
        attributeState "tamper", label: 'Tamper', icon: "st.security.alarm.alarm"
        attributeState "fault", label: 'Fault', icon: "st.security.alarm.alarm"
      }
    }
    standardTile("bypass", "device.bypass", width: 3, height: 2, title: "Bypass Status", decoration:"flat"){
      state "off", label: 'Enabled', icon: "st.security.alarm.on"
      state "on", label: 'Bypassed', icon: "st.security.alarm.off"
    }
    standardTile("bypassbutton", "capability.momentary", width: 3, height: 2, title: "Bypass Button", decoration: "flat"){
      state "bypass", label: 'Bypass', action: "bypass", icon: "st.locks.lock.unlocked"
    }

    // This tile will be the tile that is displayed on the Hub page.
    main "zone"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details(["zone", "bypass", "bypassbutton"])
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
  def bypassList = ['on','off']

  if (troubleList.contains(state)) {
    // Send final event
    sendEvent (name: "trouble", value: "${state}")
  } else if (bypassList.contains(state)) {
    sendEvent (name: "bypass", value: "${state}")
  } else {
    sendEvent (name: "contact", value: "${state}")
  }
}
