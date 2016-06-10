/**
 *  Change Nest Mode
 *
 *  Author: jorgecis@gmail.com
 *  Date: 06/06/15
 *
 *  Simply chage the nest to away for the selected modes, or present for any other mode.
 */

definition(
    name:        "Change Nest Mode",
    namespace:   "jorgeci",
    author:      "jorgecis@gmail.com",
    description: "Simply chage the nest to away for the selected modes, or present for any other mode.",
    category:    "Green Living",
    iconUrl:     "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url:   "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  section("Change away for this mode...") {
    input "newMode", "mode"
  }

  section("Change these thermostats modes...") {
    input "thermostats", "capability.thermostat", multiple: true
  }
}

def installed() {
  subscribe(location, changeMode)
}

def updated() {
  unsubscribe()
  subscribe(location, changeMode)
}

def changeMode(evt) {
  if(newMode == location.mode) {
    log.info("Marking Away")
    thermostats?.away()
  } else {
    log.info("Marking Present")
    thermostats?.present()
  }
}