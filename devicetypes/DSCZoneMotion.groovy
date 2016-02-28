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
  definition (name: "DSC Zone Motion", author: "jordan@xeron.cc") {
    // Change or define capabilities here as needed
    capability "Motion Sensor"
    capability "Sensor"

    // Add commands as needed
    command "zone"
  }

  simulator {
    // Nothing here, you could put some testing stuff here if you like
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"zone", type: "lighting", width: 6, height: 4){
      tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
        attributeState "active",   label:'motion',    icon:"st.motion.motion.active",   backgroundColor:"#53a7c0"
        attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        attributeState "alarm",    label:'ALARM',     icon:"st.motion.motion.active",   backgroundColor:"#ff0000"
       }
       tileAttribute("device.trouble", key: "SECONDARY_CONTROL") {
         attributeState("restore", label:'No Trouble')
         attributeState("tamper", label:'Tamper')
         attributeState("fault", label:'Fault')
       }
    }

    // This tile will be the tile that is displayed on the Hub page.
    main "zone"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details "zone"
  }
}

// handle commands
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
