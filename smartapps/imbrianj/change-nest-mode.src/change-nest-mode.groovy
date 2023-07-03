/**
 *  Change Nest Mode
 *
 *  Author: brian@bevey.org
 *  Date: 5/5/14
 *
 *  Simply marks any thermostat "away" if able (primarily focused on the Nest
 *  thermostat).  This is intended to be used with an "Away" or "Home" mode.
 */

definition(
    name:        "Change Nest Mode",
    namespace:   "imbrianj",
    author:      "brian@bevey.org",
    description: "Simply marks any thermostat 'away' if able (primarily focused on the Nest thermostat).  This is intended to be used with an 'Away' or 'Home' mode.",
    category:    "Green Living",
    iconUrl:     "https://s3.amazonaws.com/smartapp-icons/Partner/nest.png",
    iconX2Url:   "https://s3.amazonaws.com/smartapp-icons/Partner/nest@2x.png"
)

preferences {
  section("Change to this mode to...") {
    input "newMode", "mode", metadata:[values:["Away", "Home"]]
  }

  section("Change these thermostats modes...") {
    input "thermostats", "capability.thermostat", multiple: true
  }
}

def installed() {
  subscribe(location, changeMode)
  subscribe(app, changeMode)
}

def updated() {
  unsubscribe()
  subscribe(location, changeMode)
  subscribe(app, changeMode)
}

def changeMode(evt) {
  if(newMode == "Away") {
    log.info("Marking Away")
    thermostats?.away()
  }

  else {
    log.info("Marking Present")
    thermostats?.present()
  }
}