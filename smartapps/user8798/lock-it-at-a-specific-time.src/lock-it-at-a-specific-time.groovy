/**
 *  Lock it at a specific time
 *
 *  Copyright 2014 Erik Thayer
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
 */
definition(
    name: "Lock it at a specific time",
    namespace: "user8798",
    author: "Erik Thayer",
    description: "Make sure a door is locked at a specific time.  Option to add door contact sensor to only lock if closed.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
  section("At this time every day") {
    input "time", "time", title: "Time of Day"
  }
  section("Make sure this is locked") {
    input "lock","capability.lock"
  }
  section("Make sure it's closed first..."){
    input "contact", "capability.contactSensor", title: "Which contact sensor?", required: false
  }
  section( "Notifications" ) {
    input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false
    input "phone", "phone", title: "Send a text message?", required: false
  }
}
def installed() {
  schedule(time, "setTimeCallback")

}

def updated(settings) {
  unschedule()
  schedule(time, "setTimeCallback")
}

def setTimeCallback() {
  if (contact) {
    doorOpenCheck()
  } else {
    lockMessage()
    lock.lock()
  }
}
def doorOpenCheck() {
  def currentState = contact.contactState
  if (currentState?.value == "open") {
    def msg = "${contact.displayName} is open.  Scheduled lock failed."
    log.debug msg
    if (sendPushMessage) {
      sendPush msg
    }
    if (phone) {
      sendSms phone, msg
    }
  } else {
    lockMessage()
    lock.lock()
  }
}

def lockMessage() {
  def msg = "Locking ${lock.displayName} due to scheduled lock."
  log.debug msg
  if (sendPushMessage) {
    sendPush msg
  }
  if (phone) {
    sendSms phone, msg
  }
}
