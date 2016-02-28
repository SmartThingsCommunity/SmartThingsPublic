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

  tiles(scale: 2) {
    multiAttributeTile(name:"zone", type: "lighting", width: 6, height: 4){
      tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
        attributeState "open",   label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#ffa81e"
        attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#79b821"
        attributeState "alarm",  label: '${name}', icon: "st.contact.contact.open",   backgroundColor: "#ff0000"
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
    sendEvent (name: "contact", value: "${state}")
  }
}
