/**
 *  Safe Watch
 *
 *  Author: brian@bevey.org
 *  Date: 2013-11-17
 *
 *  Watch a series of sensors for any anomalies for securing a safe or room.
 */

definition(
  name: "Safe Watch",
  namespace: "imbrianj",
  author: "brian@bevey.org",
  description: "Watch a series of sensors for any anomalies for securing a safe.",
  category: "Safety & Security",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  section("Things to secure?") {
    input "contact", "capability.contactSensor",      title: "Contact Sensor",    required: false
    input "motion",  "capability.motionSensor",       title: "Motion Sensor",     required: false
    input "knock",   "capability.accelerationSensor", title: "Knock Sensor",      required: false
    input "axis",    "capability.threeAxis",          title: "Three-Axis Sensor", required: false
  }

  section("Temperature monitor?") {
    input "temp",    "capability.temperatureMeasurement", title: "Temperature Sensor", required: false
    input "maxTemp", "number",                            title: "Max Temperature (°${location.temperatureScale})",   required: false
    input "minTemp", "number",                            title: "Min Temperature (°${location.temperatureScale})",   required: false
  }

  section("When which people are away?") {
    input "people", "capability.presenceSensor", multiple: true
  }

  section("Notifications?") {
    input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required: false
    input "phone", "phone", title: "Send a Text Message?", required: false
  }

  section("Message interval?") {
    input name: "messageDelay", type: "number", title: "Minutes (default to every message)", required: false
  }
}

def installed() {
  init()
}

def updated() {
  unsubscribe()
  init()
}

def init() {
  subscribe(contact, "contact.open",         triggerContact)
  subscribe(motion,  "motion.active",        triggerMotion)
  subscribe(knock,   "acceleration.active",  triggerKnock)
  subscribe(temp,    "temperature",          triggerTemp)
  subscribe(axis,    "threeAxis",            triggerAxis)
}

def triggerContact(evt) {
  if(everyoneIsAway()) {
    send("Safe Watch: ${contact.label ?: contact.name} was opened!")
  }
}

def triggerMotion(evt) {
  if(everyoneIsAway()) {
    send("Safe Watch: ${motion.label ?: motion.name} sensed motion!")
  }
}

def triggerKnock(evt) {
  if(everyoneIsAway()) {
    send("Safe Watch: ${knock.label ?: knock.name} was knocked!")
  }
}

def triggerTemp(evt) {
  def temperature = evt.doubleValue

  if((maxTemp && maxTemp < temperature) ||
     (minTemp && minTemp > temperature)) {
    send("Safe Watch: ${temp.label ?: temp.name} is ${temperature}")
  }
}

def triggerAxis(evt) {
  if(everyoneIsAway()) {
    send("Safe Watch: ${axis.label ?: axis.name} was tilted!")
  }
}

private everyoneIsAway() {
  def result = true

  if(people.findAll { it?.currentPresence == "present" }) {
    result = false
  }

  log.debug("everyoneIsAway: ${result}")

  return result
}

private send(msg) {
  def delay = (messageDelay != null && messageDelay != "") ? messageDelay * 60 * 1000 : 0

  if(now() - delay > state.lastMessage) {
    state.lastMessage = now()
    if(sendPushMessage == "Yes") {
      log.debug("Sending push message.")
      sendPush(msg)
    }

    if(phone) {
      log.debug("Sending text message.")
      sendSms(phone, msg)
    }

    log.debug(msg)
  }

  else {
    log.info("Have a message to send, but user requested to not get it.")
  }
}
