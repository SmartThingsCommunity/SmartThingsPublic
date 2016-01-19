/**
 *  Cloud Nine Apps Temperature Monitor
 *
 *  Copyright 2016 Cloud Nine Apps, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * Version History
 * ---------------
 * - 1.0 (First release)
 *   - Features
 *     - Ability to send push notification and/or text message for one or more sensors when temperature either
 *       falls below a configured value or rises above a configured value.
 *     - Ability to set low temperature and high temperature values via preferences.
 *     - Ability to send custom text message for the above events. 
 */
definition(
    name: "Cloud Nine Apps Temperature Monitor",
    namespace: "CloudNineApps",
    author: "Cloud Nine Apps, LLC",
    description: "A SmartApp to notify when temperature of one or more sensors rises above or falls below configured values.",
    category: "Safety & Security",
    iconUrl: "http://cloudnineapps.com/images/products/SmartApps/TemperatureMonitor/TemperatureMonitor.png",
    iconX2Url: "http://cloudnineapps.com/images/products/SmartApps/TemperatureMonitor/TemperatureMonitor@2x.png",
    iconX3Url: "http://cloudnineapps.com/images/products/SmartApps/TemperatureMonitor/TemperatureMonitor@2x.png")


preferences {
  section("Enable/disable the SmartApp"){
    input "isEnabled", "bool", title: "Is enabled?", required: false, defaultValue: true
  }
  section("Choose one or more, when ...") {
    input "temperatureSensor", "capability.temperatureMeasurement", title: "Temperature Sensor", required: true, multiple: true
  }
  section("Set the low temperature mark"){
    input "lowTemperatureValue", "decimal", title: "Low Temperature", required: false
  }
  section("Set the high temperature mark"){
    input "highTemperatureValue", "decimal", title: "High Temperature", required: false
  }
  section("Send this message on low temperature (optional, sends standard status message if not specified)"){
    input "lowMsg", "text", title: "Message Text", required: false
  }
  section("Send this message on high temperature (optional, sends standard status message if not specified)"){
	input "highMsg", "text", title: "Message Text", required: false
  }
  section("Via a push notification and/or an SMS message"){
    input("recipients", "contact", title: "Send notifications to") {
      input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
      input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes", "No"]
    }
  }
  section("Minimum time between messages (optional, defaults to every message)") {
    input "frequency", "decimal", title: "Minutes", required: false
  }
}

def installed() {
  log.debug "installed(): settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "updated(): settings: ${settings}"
  unsubscribe()
  initialize()
}

def initialize() {
  subscribeToEvents();
}

/** Subscribes to the events. */
def subscribeToEvents() {
  subscribe(temperatureSensor, "temperature", eventHandler)
}

/** Handles the event. */
def eventHandler(event) {
  if (isEnabled) {
    log.debug("eventHandler(): eventNme: ${event.name}, value: ${event.value}, linkText: ${event.linkText}, pushAndPhone: ${pushAndPhone}")
    if (goNogo(event)) {
      def msg = ''
      if (event.doubleValue <= lowTemperatureValue) {
        msg = prepareMessage("lowTemperatureEvent", event)
      }
      else if (event.doubleValue >= highTemperatureValue) {
        msg = prepareMessage("highTemperatureEvent", event)
      }
      if (msg) {
        log.debug("eventHandler(): msg: ${msg}")
        sendMessage(msg)
      }
      if (frequency) {
    	// Event handling frequency specified, track occurrence
    	state[event.deviceId] = now()
      }
    }
  }
}

/** Returns true if the specified event should be handled or not. */
private goNogo(event) {
  def retval = true 
  if (frequency) {
    def lastTime = state[event.deviceId]
    retval = (!lastTime || (now() - lastTime) >= frequency * 60000)
  }
  return retval
}

/** Prepares the message based on the event type and event data (and user specified preferences). */
private prepareMessage(type, event) {
  def msg = (type == 'lowTemperatureEvent') ? lowMsg : highMsg;
  if (!msg) {
	if (type == 'lowTemperatureEvent') {
	  msg = "Temperature of ${event.linkText} has fallen to ${event.doubleValue} degree"
	}
	else {
	  msg = "Temperature of ${event.linkText} has risen to ${event.doubleValue} degree"
	}
  }
  return msg
}

/** Sends the message. */
private sendMessage(msg) {
  // Send message now
  if (location.contactBookEnabled) {
    sendNotification(msg, recipients)
  }
  else {
	if (!phone || pushPhone != "No") {
	  log.debug("sendMessage(): Sending push msg: ${msg}")
	  sendPush(msg)
	}
	if (phone) {
	  log.debug("sendMessage(): Sending SMS msg: ${msg}")
	  sendSMS(phone, msg)
	}
  }
}
