/**
 *  Pollster
 *
 *  Copyright (c) 2014 Statusbits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed
 *  under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, either express or implied. See the License  for the
 *  specific language governing permissions and limitations under the License.
 *
 *  Version: 1.0.0
 *  Date: 2014-07-14
 */

definition(
    name: "Pollster",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Calls poll() function periodically for selected devices.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("About") {
        paragraph "Pollster is a polling daemon that calls poll() function periodically " +
            "for selected devices."
        paragraph "Version 1.0.0.\nCopyright (c) 2014 Statusbits.com"
    }
    section("Devices") {
        input "devices", "capability.polling", title:"Select devices to be polled", multiple:true, required:false
    }
    section("Polling Interval") {
        input "interval", "number", title:"Set polling interval (in minutes)", defaultValue:5
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def pollingTask() {
    TRACE("pollingTask()")

    if (!settings.devices?.size()) {
        TRACE("There's no devices to poll. Stopping daemon now.")
        unschedule()
        return
    }

    settings.devices*.poll()
}

private def initialize() {
    TRACE("initialize() with settings: ${settings}")

    def minutes = settings.interval.toInteger()
    if (minutes > 0) {
        // Schedule polling daemon to run every N minutes
        TRACE("Scheduling polling daemon to run every ${minutes} minutes.")
        schedule("0 0/${minutes} * * * ?", pollingTask)
    }
}

private def TRACE(message) {
    //log.debug message
    //log.debug "state: ${state}"
}
