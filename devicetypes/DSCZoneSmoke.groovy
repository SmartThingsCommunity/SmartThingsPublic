/*
 *  DSC Zone Smoke
 *
 *  Original Device Author: Matt Martz <matt.martz@gmail.com>
 *  Smoke Alarm Additions Author: Dan S <coke12oz@hotmail.com>
 *  Modified by Jordan <jordan@xeron.cc>
 *  Date: 2016-02-04
 */

// for the UI
metadata {
  definition (name: "DSC Zone Smoke", author: "matt.martz@gmail.com") {
    // Change or define capabilities here as needed
    capability "Smoke Detector"
    capability "Sensor"

    attribute "alarmState", "string"
    // Add commands as needed
    command "zone"
  }

  simulator {
    // Nothing here, you could put some testing stuff here if you like
  }

  tiles {
    // Main Row
    standardTile("zone", "device.alarmState", width: 2, height: 2, canChangeBackground: true, canChangeIcon: true) {
      state "clear",  label: 'clear',  icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff"
      state "smoke",  label: 'SMOKE',  icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13"
      state "tested", label: 'TESTED', icon: "st.alarm.smoke.test",  backgroundColor: "#e86d13"
    }

    // This tile will be the tile that is displayed on the Hub page.
    main "zone"

    // These tiles will be displayed when clicked on the device, in the order listed here.
    details(["zone"])
  }
}

// handle commands
def zone(String state) {
  def text = null
  def results = []
  // state will be a valid state for a zone (open, closed)
  // zone will be a number for the zone
  log.debug "Zone: ${state}"

  if (state == "smoke") {
        text = "$device.displayName smoke was detected!"
        log.debug "$text"
        // these are displayed:false because the composite event is the one we want to see in the app
        sendEvent(name: "smoke", value: "detected", descriptionText: text, displayed: false)

  } else if (state == "clear") {
        text = "$device.displayName smoke is clear"
        log.debug "$text"
        sendEvent(name: "smoke", value: "clear", descriptionText: text, displayed: false)
  }

  // Send final event?
  sendEvent (name: "alarmState", value: "${newState}")
}
