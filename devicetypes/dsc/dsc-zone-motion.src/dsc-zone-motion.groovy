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
      tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
        attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
        attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        attributeState "alarm", label:'ALARM', icon:"st.motion.motion.active", backgroundColor:"#ff0000"
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
