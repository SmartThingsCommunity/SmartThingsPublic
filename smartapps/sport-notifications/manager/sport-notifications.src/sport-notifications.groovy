/**
* Sport Notifications 
*
*  Copyright 2017 Eric Luttmann
*
*  Description:
*  Manages scheduling and notifications for multiple sport services.  This is the manager app that handles
*  scheduling and execution of each sport service app.
*
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

def handle() { return "Sport Notifications" }
def version() { return "0.9.0" }
def copyright() { return "Copyright © 2017" }

definition(
    name: "${handle()}",
    namespace: "sport-notifications/manager",
    author: "Eric Luttmann",
    description: "Manages scheduling and notifications for multiple sport services.",
    category: "My Apps",
    singleInstance: true,
    iconUrl: "https://cloud.githubusercontent.com/assets/2913371/22167524/3390bf68-df24-11e6-94c6-b099063842df.png",
    iconX2Url: "https://cloud.githubusercontent.com/assets/2913371/22167524/3390bf68-df24-11e6-94c6-b099063842df.png",
    iconX3Url: "https://cloud.githubusercontent.com/assets/2913371/22167524/3390bf68-df24-11e6-94c6-b099063842df.png")


preferences {
    page name:"pageMain"
    page name:"pageSchedule"
    page name:"pageAbout"
}

def pageMain() {
    dynamicPage(name: "pageMain", install: true, uninstall: false, submitOnChange: true) {
        def includeNHL = true  // detectChildApp("sport-notifications/nhlservice","NHL Notification Service")
        def includeNFL = false // detectChildApp("sport-notifications/nhlservice","NFL Notification Service")
        section () {
            if (includeNHL == true) {
                app(name: "nhlservice", appName: "NHL Notification Service", namespace: "sport-notifications/nhl", title: "New NHL Notification", multiple: true, submitOnChange: true)
            }
            if (includeNFL == true) {
                app(name: "nflservice", appName: "NFL Notification Service", namespace: "sport-notifications/nfl", title: "New NFL Notification", multiple: true, submitOnChange: true)
            }
        }

        section([title:"Options", mobileOnly:true]) {
            def apps = childApps.sort{ it.label }
            def appsList = []
            apps.each{appsList += it.label}

            input "createSwitches", "bool", title: "Create Virtual Switches", description: "Create virtual switches for each sporting service, allowing the ability to turn each specific service on and off",
            	defaultValue: "true", required: false, displayDuringSetup: true
            input "touchNotify", "enum", title: "Touch Notification", description: "Tap to select service to use for touch notification", 
                required: false, multiple: false, displayDuringSetup: true, options: appsList
            href "pageSchedule", title: "Service Scheduling", description: "Tap to change services scheduling"
            href "pageAbout", title: "About ${handle()}", description: "Tap to get application version, license, or to remove the application"
        }
    }
}

def pageSchedule() {
    dynamicPage(name: "pageSchedule") {
        section( "Sport Services Schedule" ) {
            input "serviceStartTime", "time", title: "Daily Game Check", defaultValue: "1:00", required: false, multiple: false, displayDuringSetup: true
            input "serviceHoursBeforeStart", "number", title: "Hours Before Game Start", description: "0-12 hours", required: false, multiple: false, displayDuringSetup: true, range: "0..12"
        }
    }
}

def pageAbout() {
    dynamicPage(name: "pageAbout", uninstall: true) {
        section {
            paragraph "${handle()}\n${copyright()}", image: "https://cloud.githubusercontent.com/assets/2913371/22167524/3390bf68-df24-11e6-94c6-b099063842df.png"
        }
        section {
            paragraph "${textAboutVersion()}"
        }
        section {
            href(name: "hrefDonate",
                 title: "Donate",
                 required: false,
                 style: "external",
                 image: "https://www.paypalobjects.com/webstatic/en_US/i/buttons/PP_logo_h_200x51.png",
                 url: "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VLKDKNJLQ55XU",
                 description: "If you like this app, please consider supporting its development by making a donation via PayPal")
        }
        section {
            paragraph "${textAboutLicense()}"
        }
        section("Tap button below to remove all notifications and applications"){
        }
    }
}

def textAboutVersion() {
    def version = "${handle()} version: ${version()}"
    def childCount = childApps.size()
    def childVersions = ""

    if (childCount) {
        childApps.each {child ->
            childVersions = childVersions + "\n" +
                "Service: ${child.label}\n" +
                "   ${child.name}\n" +
                "   version: ${child.childVersion()}\n"
        }
    } else {
        childVersions = "No notification services installed"  
    }

    return "${version}\n${childVersions}"
}

def textAboutLicense() {
    def text =
        "Licensed under the Apache License, Version 2.0 (the 'License'); "+
        "you may not use this file except in compliance with the License. "+
        "You may obtain a copy of the License at"+
        "\n\n"+
        "    http://www.apache.org/licenses/LICENSE-2.0"+
        "\n\n"+
        "Unless required by applicable law or agreed to in writing, software "+
        "distributed under the License is distributed on an 'AS IS' BASIS, "+
        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. "+
        "See the License for the specific language governing permissions and "+
        "limitations under the License." 
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    unsubscribe()

    initialize()
}

def initialize() {
    log.info "${handle()}: Version ${version()}"
    
    def doTouch = false
    if (childApps) {
        childApps.each {
            it.childForceInit()
            
    		// determine if touch notificaiton should be enabled
    		if (touchNotify) {
                if (it.label == touchNotify) {
                    doTouch = true
                }
            }
        }
    }

    // enable touch notificaiton 
    if (doTouch == true) {
        subscribe(app, appTouchNotify)
    }

    // schedule to run every day at specified time
    def start = getStartTime(settings.serviceStartTime)
    def startText = start.format('h:mm a',location.timeZone)
    log.debug "Scheduling game day check once per day at ${startText}"

    // setup schedule
    schedule(start, gameDayCheck)

    // start with initial gameday check
    gameDayCheck()
}

def appTouchNotify(evt) {
    childApps.each {
        if (it.label == touchNotify) {
            log.debug "${app.label}: found goal trigger = ${it.label}"
            it.childTouchTrigger()
            return
        }
    }
}

def getStartTime(startTime) {
    def start = startTime ? timeToday(startTime, location.timeZone) : timeToday("1:00AM", location.timeZone)
    return start
}

def gameDayCheck() {
    log.debug "Checking for game day"
    childApps.each {child ->
        if (child.childIsGameDay()) {
            def gameDayTime = child.childGameDate()
            log.debug "Start game routine for ${child.label}, game is at ${gameDayTime.format('h:mm:ss a',location.timeZone)}"
            child.childStartGame()
        } else {
            log.debug "Not gameday for ${child.label}"
        }
    }
}


def parentCreateSwitches() {
	return settings.createSwitches
}

def parentHourBeforeGame() {
	return settings.serviceHoursBeforeStart
}