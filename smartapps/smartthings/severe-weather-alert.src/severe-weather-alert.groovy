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
 *  Severe Weather Alert
 *
 *  Author: SmartThings
 *  Date: 2013-03-04
 */
definition(
    name: "Severe Weather Alert",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Get a push notification when severe weather is in your area.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather@2x.png",
    pausable: true
)

preferences {
    page name: "mainPage", install: true, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
        if (!(location.zipCode || ( location.latitude && location.longitude )) && location.channelName == 'samsungtv') {
            section { paragraph title: "Note:", "Location is required for this SmartApp. Go to 'Location Name' settings to setup your correct location." }
        }

        if (location.channelName != 'samsungtv') {
            section( "Set your location" ) { input "zipCode", "text", title: "Zip code" }
        }

        if (location.contactBookEnabled || phone1 || phone2 || phone3) {
            section("In addition to push notifications, send text alerts to...") {
                input("recipients", "contact", title: "Send notifications to") {
                    input "phone1", "phone", title: "Phone Number 1", required: false
                    input "phone2", "phone", title: "Phone Number 2", required: false
                    input "phone3", "phone", title: "Phone Number 3", required: false
                }
            }
        }

        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)"
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    scheduleJob()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unschedule()
    scheduleJob()
}

def scheduleJob() {
    def sec = Math.round(Math.floor(Math.random() * 60))
    def min = Math.round(Math.floor(Math.random() * 60))
    def cron = "$sec $min * * * ?"
    schedule(cron, "checkForSevereWeather")
}

def checkForSevereWeather() {
    def alerts
    if(locationIsDefined()) {
        if(!(zipcodeIsValid())) {
            log.warn "Severe Weather Alert: Invalid zipcode entered, defaulting to location's zipcode"
        }
        def zipToLocation = getTwcLocation("$zipCode").location
        alerts = getTwcAlerts("${zipToLocation.latitude},${zipToLocation.longitude}")
    } else {
        log.warn "Severe Weather Alert: Location is not defined"
    }

    if (alerts) {
        alerts.each {alert ->
            def msg = alert.headlineText
            if (alert.effectiveTimeLocal && !msg.contains(" from ")) {
                msg += " from ${parseAlertTime(alert.effectiveTimeLocal).format("E hh:mm a", TimeZone.getTimeZone(alert.effectiveTimeLocalTimeZone))}"
            }
            if (alert.expireTimeLocal && !msg.contains(" until ")) {
                msg += " until ${parseAlertTime(alert.expireTimeLocal).format("E hh:mm a", TimeZone.getTimeZone(alert.expireTimeLocalTimeZone))}"
            }
            send(msg)
        }
    } else {
        log.info "No current alerts"
    }
}

def descriptionFilter(String description) {
    def filterList = ["special", "statement", "test"]
    def passesFilter = true
    filterList.each() { word ->
        if(description.toLowerCase().contains(word)) { passesFilter = false }
    }
    passesFilter
}

def locationIsDefined() {
    zipcodeIsValid() || location.zipCode || ( location.latitude && location.longitude )
}

def zipcodeIsValid() {
    zipCode && zipCode.isNumber() && zipCode.size() == 5
}

private send(message) {
    sendPush message
    if (settings.phone1) {
        sendSms phone1, message
    }
    if (settings.phone2) {
        sendSms phone2, message
    }
    if (settings.phone3) {
        sendSms phone3, message
    }
}
