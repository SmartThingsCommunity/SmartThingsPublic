/**
 *  Lights Off with No Motion and Presence
 *
 *  Author: Bruce Adelsman
 */

definition(
    name: "Lights Off with No Motion and Presence",
    namespace: "naissan",
    author: "Bruce Adelsman",
    description: "Turn lights off when no motion and presence is detected for a set period of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
  section("Light switches to turn off") {
    input "switches", "capability.switch", title: "Choose light switches", multiple: true
  }
  section("Turn off when there is no motion and presence") {
    input "motionSensor", "capability.motionSensor", title: "Choose motion sensor"
    input "presenceSensors", "capability.presenceSensor", title: "Choose presence sensors", multiple: true
  }
  section("Delay before turning off") {
    input "delayMins", "number", title: "Minutes of inactivity?"
  }
}

def installed() {
  subscribe(motionSensor, "motion", motionHandler)
  subscribe(presenceSensors, "presence", presenceHandler)
}

def updated() {
  unsubscribe()
  subscribe(motionSensor, "motion", motionHandler)
  subscribe(presenceSensors, "presence", presenceHandler)
}

def motionHandler(evt) {
  log.debug "handler $evt.name: $evt.value"
  if (evt.value == "inactive") {
    runIn(delayMins * 60, scheduleCheck, [overwrite: true])
  }
}

def presenceHandler(evt) {
  log.debug "handler $evt.name: $evt.value"
  if (evt.value == "not present") {
    runIn(delayMins * 60, scheduleCheck, [overwrite: true])
  }
}

def isActivePresence() {
  // check all the presence sensors, make sure none are present
  def noPresence = presenceSensors.find{it.currentPresence == "present"} == null
  !noPresence
}

def scheduleCheck() {
  log.debug "scheduled check"
  def motionState = motionSensor.currentState("motion")
    if (motionState.value == "inactive") {
      def elapsed = now() - motionState.rawDateCreated.time
      def threshold = 1000 * 60 * delayMins - 1000
      if (elapsed >= threshold) {
        if (!isActivePresence()) {
          log.debug "Motion has stayed inactive since last check ($elapsed ms) and no presence:  turning lights off"
          switches.off()
        } else {
          log.debug "Presence is active: do nothing"
        }
      } else {
        log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms): do nothing"
      }
    } else {
      log.debug "Motion is active: do nothing"
    }
}
