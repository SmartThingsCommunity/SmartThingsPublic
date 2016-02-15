/**
 *  ZWN-RSM2 Enerwave Dual Load Binder Application
 *
 *  Author Matt Frank based on the work of chrisb for AEON Power Strip
 *
 *  Date Created:  6/26/2014
 *  Last Modified: 1/11/2015
 *
 */

// Automatically generated. Make future change here.
definition(
    name: "ZWN-RSM2 Enerwave Dual Load Binder",
    namespace: "mattjfrank",
    author: "Matt Frank",
    description: "ZWN-RSM2 Enerwave Dual Load Binder",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
  section("Which Enerwave Dual Load ZWN-RSM2"){
    input "strip", "capability.switch", multiple: true, icon: "st.switches.switch.on"
  }
  section("Select a Virtual Switch to bind to Outlet 1"){
    input "switch1", "capability.switch"
  }
    section("Select a Virtual Switch to bind to Outlet 2"){
    input "switch2", "capability.switch"
  }

}

def installed() {
  log.debug "Installed with settings: ${settings}"
  subscribe(switch1, "switch.on", switchOnOneHandler)
    subscribe(switch2, "switch.on", switchOnTwoHandler)

    subscribe(switch1, "switch.off", switchOffOneHandler)
    subscribe(switch2, "switch.off", switchOffTwoHandler)

}

def updated(settings) {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  subscribe(switch1, "switch.on", switchOnOneHandler)
    subscribe(switch2, "switch.on", switchOnTwoHandler)

    subscribe(switch1, "switch.off", switchOffOneHandler)
    subscribe(switch2, "switch.off", switchOffTwoHandler)
}

def switchOnOneHandler(evt) {
  log.debug "switch on1"
  strip.on1()
}

def switchOnTwoHandler(evt) {
  log.debug "switch on2"
  strip.on2()
}


def switchOffOneHandler(evt) {
  log.debug "switch off1"
  strip.off1()
}

def switchOffTwoHandler(evt) {
  log.debug "switch off2"
  strip.off2()
}