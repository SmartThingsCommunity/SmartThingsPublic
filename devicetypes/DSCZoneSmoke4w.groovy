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

  tiles(scale: 2) {
    multiAttributeTile(name:"zone", type: "lighting", width: 6, height: 4){
      tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
        attributeState "clear",  label: 'clear',  icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff"
        attributeState "smoke",  label: 'SMOKE',  icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13"
        attributeState "tested", label: 'TESTED', icon: "st.alarm.smoke.test",  backgroundColor: "#e86d13"
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
    // Since this is a smoke device we need to convert the values to match the device capabilities
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
