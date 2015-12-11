/**
 *  Copyright 2015 SmartThings
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
 *  Sunrise, Sunset
 *
 *  Author: SmartThings
 *
 *  Date: 2013-04-30
 */
definition(
    name: "Routine Automation",
    namespace: "fwump38/automations",
    author: "fwump38",
    description: "An app to configure time ranges for automating routines.",
    category: "Mode Magic",

    // the parent option allows you to specify the parent app in the form <namespace>/<app name>
    parent: "fwump38:Smart Routines",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    page name: "timePage", title: "Automate Routines", install: false, uninstall: true, nextPage: "actionPage"
    page name: "actionPage", title: "Automate Routines", install: false, uninstall: true, nextPage: "namePage"
    page name: "namePage", title: "Automate Routines", install: true, uninstall: true
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    initialize()
}

def initialize() {
    // if the user did not override the label, set the label to the default
    if (!overrideLabel) {
        app.updateLabel(defaultLabel())
    }
}

// page to select start and end for this automation time range
def timePage() {
    dynamicPage(name: "timePage") {
        section {
            timeInputs()
        }

    }
}

// main page to select lights, the action, and turn on/off times
def actionPage() {
    dynamicPage(name: "actionPage") {
        section {
            actionInputs()
        }

    }
}

// page for allowing the user to give the automation a custom name
def namePage() {
    if (!overrideLabel) {
        // if the user selects to not change the label, give a default label
        def l = defaultLabel()
        log.debug "will set default label of $l"
        app.updateLabel(l)
    }
    dynamicPage(name: "namePage") {
        if (overrideLabel) {
            section("Automation name") {
                label title: "Enter custom name", defaultValue: app.label, required: false
            }
        } else {
            section("Automation name") {
                paragraph app.label
            }
        }
        section {
            input "overrideLabel", "bool", title: "Edit automation name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

// inputs for selecting start and end time range
def timeInputs() {
    section("Time Range") {
        input "startingX", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
        if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false
        else {
            if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
            else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
        }
    }
            
    section("Time Range") {
        input "endingX", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
        if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false
        else {
            if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
            else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
        }
    }
    section(hideable: true, hidden: true, "Additional Options") {
        input "weekDays","enum",title: "Only on certain days of the week", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        input "modes", "mode", title: "Only when the mode is", multiple: true, required: false
        }
    }

// inputs for selecting which routines to run
def actionInputs() {
    // get the available actions
    def actions = location.helloHome?.getPhrases()*.label
    if (actions) {
        // sort them alphabetically
        actions.sort()
    }
    section("Automatically Run Home Routine(s)...") {
        input "homeChoice", "enum", title: "When...", options: ["At Start of Time Range", "At End of Time Range"], submitOnChange: true
        if(homeChoice == "At Start of Time Range") {
            input "routineStartHome", "enum", title: "Select which routine(s) should run", multiple: true, required: true, options: actions
        }
        if(homeChoice == "At End of Time Range") {
            input "routineEndHome", "enum", title: "Select which routine(s) should run", multiple: true, required: true, options: actions
        }
    }
    section("Only If One of These People Are Home") {
        input "peopleHome", "capability.presenceSensor", multiple: true
        input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false, defaultValue: 10
    }
    section(hideable: true, hidden: true, "Additional Options") {
        input "weekDays2","enum",title: "Only on certain days of the week", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        input "modes2", "mode", title: "Only when the mode is", multiple: true, required: false
    }
    section("Automatically Run Away Routine(s)...") {
        input "peopleAway", "capability.presenceSensor", multiple: true, title: "Only IF All of These People Are Away"
        input "falseAlarmThreshold2 ", "decimal", title: "Number of minutes", required: false, defaultValue: 10
    }
    section(hideable: true, hidden: true, "Additional Options") {
        input "weekDays3","enum",title: "Only on certain days of the week", required: false, multiple: true, submitOnChange: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        input "modes3", "mode", title: "Only when the mode is", multiple: true, required: false
        }

}


