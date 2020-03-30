/**
 *  Working From Home
 *
 *  Copyright 2014 George Sudarkoff
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
    name: "Working From Home",
    namespace: "com.sudarkoff",
    author: "George Sudarkoff",
    description: "If after a particular time of day a certain person is still at home, trigger a 'Working From Home' action.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/Cat-ModeMagic@2x.png",
    pausable: true
)

preferences {
    page (name:"configActions")
}

def configActions() {
    dynamicPage(name: "configActions", title: "Configure Actions", uninstall: true, install: true) {
        section ("When this person") {
            input "person", "capability.presenceSensor", title: "Who?", multiple: false, required: true
        }
        section ("Still at home past") {
            input "timeOfDay", "time", title: "What time?", required: true
        }

        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases) {
            phrases.sort()
            section("Perform this action") {
                input "wfhPhrase", "enum", title: "\"Hello, Home\" action", required: true, options: phrases
            }
        }

        section (title: "More options", hidden: hideOptions(), hideable: true) {
            input "sendPushMessage", "bool", title: "Send a push notification?"
            input "phone", "phone", title: "Send a Text Message?", required: false
            input "days", "enum", title: "Set for specific day(s) of the week", multiple: true, required: false,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }

        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def initialize() {
    schedule(timeToday(timeOfDay, location?.timeZone), "checkPresence")
    if (customName) {
      app.setTitle(customName)
    }
}

def checkPresence() {
    if (daysOk && modeOk) {
        if (person.latestValue("presence") == "present") {
            log.debug "${person} is present, triggering WFH action."
            location.helloHome.execute(settings.wfhPhrase)
            def message = "${location.name} executed '${settings.wfhPhrase}' because ${person} is home."
            send(message)
        }
    }
}

private send(msg) {
    if (sendPushMessage != "No") {
        sendPush(msg)
    }

    if (phone) {
        sendSms(phone, msg)
    }

    log.debug msg
}

private getModeOk() {
    def result = !modes || modes.contains(location.mode)
    result
}

private getDaysOk() {
    def result = true
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))
        }
        def day = df.format(new Date())
        result = days.contains(day)
    }
    result
}

private hideOptions() {
    (days || modes)? false: true
}

