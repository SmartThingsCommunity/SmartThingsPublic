def smartAppNameFull() {
    return  "BatteryMonitor SmartApp for SmartThings"
}

def smartAppNameShort() {
    return  "BatteryMonitor"
}

def smartAppVersion() {
    return  "Version 0.0.5"
}

def smartAppAuthor() {
    return  "Author Brandon Gordon"
}

def smartAppCopyright() {
    return  "Copyright (c) 2014 Brandon Gordon"
}

def smartAppSource() {
    return  "https://github.com/notoriousbdg/SmartThings.BatteryMonitor"
}

def smartAppDescription() {
    return  "This SmartApp helps you monitor the status of your SmartThings devices with batteries."
}

def smartAppRevision () {
    return  '2014-11-14  v0.0.1\n' +
            ' * Initial release\n\n' +
            '2014-11-15  v0.0.2\n' +
            ' * Moved status to main page\n' +
            ' * Removed status page\n' +
            ' * Improved formatting of status page\n' +
            ' * Added low, medium, high thresholds\n' +
            ' * Handle battery status strings of OK and Low\n\n' +
            '2014-11-15  v0.0.3\n' +
            ' * Added push notifications\n\n' +
            '2014-11-20  v0.0.4\n' +
            ' * Added error handling for batteries that return strings\n\n' +
            '2014-12-26  v0.0.5\n' +
            ' * Move app metadata to a new about page\n' +
            ' * Changed notifications to only send at specified time daily\n' +
            ' * modified by lgkahn to send complete message whenever the schedule fires, showing the status of all devices.\n'
}

def smartAppLicense() {
    return  'Licensed under the Apache License, Version 2.0 (the "License"); you ' +
            'may not use this file except in compliance with the License. You ' +
            'may obtain a copy of the License at:' +
            '\n\n' +
            'http://www.apache.org/licenses/LICENSE-2.0' +
            '\n\n' +
            'Unless required by applicable law or agreed to in writing, software ' +
            'distributed under the License is distributed on an "AS IS" BASIS, ' +
            'WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or ' +
            'implied. See the License for the specific language governing ' +
            'permissions and limitations under the License.'
}

definition(
    name: "BatteryMonitor",
    namespace: "notoriousbdg",
    author: "Brandon Gordon",
    description: "SmartApp to monitor battery levels.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page name:"pageStatus"
    page name:"pageConfigure"
    page name:"pageAbout"
}

// Show About Page
def pageAbout() {
    def pageProperties = [
        name:           "pageAbout",
        title:          smartAppNameFull(),
        nextPage:       "pageConfigure",
        uninstall:      true
    ]

    return dynamicPage(pageProperties) {
        section() {
            paragraph smartAppVersion() + "\n" +
                      smartAppAuthor() + "\n" +
                      smartAppCopyright()
        }
        
        section() {
            paragraph smartAppDescription()
        }
        
        section() {
            href(
                name: "sourceCode",
                title: "Source Code (Tap to view)",
                required: false,
                external: true,
                style: "external",
                url: smartAppSource(),
                description: smartAppSource()
            )
        }

        section() {
            paragraph title: "Revision History",
                      smartAppRevision()
        }
        
        section() {
            paragraph title: "License",
                      smartAppLicense()
        }  
    }
}

// Show Status page
def pageStatus() {
    def pageProperties = [
        name:       "pageStatus",
        title:      smartAppNameShort() + " Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    if (settings.devices == null) {
        return pageAbout()
    }
    
    def listLevel0 = ""
    def listLevel1 = ""
    def listLevel2 = ""
    def listLevel3 = ""
    def listLevel4 = ""

    if (settings.level1 == null) { settings.level1 = 33 }
    if (settings.level3 == null) { settings.level3 = 67 }
    if (settings.pushMessage) { settings.pushMessage = true }
    
    return dynamicPage(pageProperties) {
        settings.devices.each() {
            try {
                if (it.currentBattery == null) {
                    listLevel0 += "$it.displayName\n"
                } else if (it.currentBattery >= 0 && it.currentBattery <  settings.level1.toInteger()) {
                    listLevel1 += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery >= settings.level1.toInteger() && it.currentBattery <= settings.level3.toInteger()) {
                    listLevel2 += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery >  settings.level3.toInteger() && it.currentBattery < 100) {
                    listLevel3 += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery == 100) {
                    listLevel4 += "$it.displayName\n"
                } else {
                    listLevel0 += "$it.currentBattery  $it.displayName\n"
                }
            } catch (e) {
                log.trace "Caught error checking battery status."
                log.trace e
                listLevel0 += "$it.displayName\n"
            }
        }

        if (listLevel0) {
            section("Batteries with errors or no status") {
                paragraph listLevel0.trim()
            }
        }
        
        if (listLevel1) {
            section("Batteries with low charge (less than $settings.level1)") {
                paragraph listLevel1.trim()
            }
        }

        if (listLevel2) {
            section("Batteries with medium charge (between $settings.level1 and $settings.level3)") {
                paragraph listLevel2.trim()
            }
        }

        if (listLevel3) {
            section("Batteries with high charge (more than $settings.level3)") {
                paragraph listLevel3.trim()
            }
        }

        if (listLevel4) {
            section("Batteries with full charge") {
                paragraph listLevel4.trim()
            }
        }

        section("Menu") {
            href "pageStatus", title:"Refresh", description:""
            href "pageConfigure", title:"Configure", description:""
            href "pageAbout", title:"About", description: ""
        }
    }
}

// Show Configure Page
def pageConfigure() {
    def helpPage =
        "Select devices with batteries that you wish to monitor."

    def inputBattery   = [
        name:           "devices",
        type:           "capability.battery",
        title:          "Which devices with batteries?",
        multiple:       true,
        required:       true
    ]

    def inputLevel1    = [
        name:           "level1",
        type:           "number",
        title:          "Low battery threshold?",
        defaultValue:   "20",
        required:       true
    ]

    def inputLevel3    = [
        name:           "level3",
        type:           "number",
        title:          "Medium battery threshold?",
        defaultValue:   "70",
        required:       true
    ]

    def inputTime      = [
        name:           "time",
        type:           "time",
        title:          "Notify at what time daily?",
        required:       true
    ]

      def nightlyStatusMsg = [
        name:           "nightlyStatus",
        type:           "bool",
        title:          "Send scheduled status message for all devices?",
        defaultValue:   true
    ]
    
    def inputPush      = [
        name:           "pushMessage",
        type:           "bool",
        title:          "Send push notifications?",
        defaultValue:   true
    ]

    def inputSMS       = [
        name:           "phoneNumber",
        type:           "phone",
        title:          "Send SMS notifications to?",
        required:       false
    ]
    

    def pageProperties = [
        name:           "pageConfigure",
        title:          smartAppNameShort() + " Configuration",
        nextPage:       "pageStatus",
        uninstall:      true
    ]

    return dynamicPage(pageProperties) {
        section("About") {
            paragraph helpPage
        }

        section("Devices") {
            input inputBattery
        }
        
        section("Settings") {
            input inputLevel1
            input inputLevel3
        }
        
        section("Notification") {
            input inputTime
            input inputPush
            input nightlyStatusMsg
            input inputSMS           
        }

        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

def installed() {
    log.debug "Initialized with settings: ${settings}"

    initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
    //nightlyStatus() for testing fx
}

def initialize() {
    schedule(settings.time, updateStatus)
}

def send(msg) {
    log.debug msg

    if (settings.pushMessage) {
        sendPush(msg)
    } else {
        sendNotificationEvent(msg)
    }

    if (settings.phoneNumber != null) {
        sendSms(phoneNumber, msg) 
    }
}

// lgk now fx to prepare nightly message and send to get complete status not just outstanding devices.
def nightlyStatus()

{
    def listLevel0 = ""
    def listLevel1 = ""
    def listLevel2 = ""
    def listLevel3 = ""
    def listLevel4 = ""
    def myhub =  location.hubs[0].name
    def outgoingMsg = "For Hub: $myhub\n"
    
    if (settings.level1 == null) { settings.level1 = 33 }
    if (settings.level3 == null) { settings.level3 = 67 }
    
    if (settings.nightlyStatus == true)
     {
        settings.devices.each() {
            try {
                if (it.currentBattery == null) {
                    listLevel0 += "$it.displayName\n"
                } else if (it.currentBattery >= 0 && it.currentBattery <  settings.level1.toInteger()) {
                    listLevel1 += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery >= settings.level1.toInteger() && it.currentBattery <= settings.level3.toInteger()) {
                    listLevel2 += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery >  settings.level3.toInteger() && it.currentBattery < 100) {
                    listLevel3 += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery == 100) {
                    listLevel4 += "$it.displayName\n"
                } else {
                    listLevel0 += "$it.currentBattery  $it.displayName\n"
                }
            } catch (e) {
                log.trace "Caught error checking battery status."
                log.trace e
                listLevel0 += "$it.displayName\n"
            }
        }

        if (listLevel0) {
      	 	    outgoingMsg = outgoingMsg + "Batteries with errors or no status\n\n" +
                listLevel0.trim()
            }
       
      // prepare message
      
        if (listLevel1) {
     	  	    outgoingMsg = outgoingMsg + "\n\nBatteries with low charge (less than $settings.level1)\n\n" +
         		listLevel1.trim()
            }
    
        if (listLevel2) {
       			 outgoingMsg = outgoingMsg + "\n\nBatteries with medium charge (between $settings.level1 and $settings.level3)\n\n" +
                 listLevel2.trim()
        }

        if (listLevel3) {
                outgoingMsg = outgoingMsg + "\n\nBatteries with high charge (more than $settings.level3)\n\n" +
                listLevel3.trim()
            }


        if (listLevel4) {
              outgoingMsg = outgoingMsg + "\n\nBatteries with full charge\n\n" +
                 listLevel4.trim()
            }
      
  // lgk now send message
  //log.debug "nightly message is $outgoingMsg"
  
 send(outgoingMsg)
 }
}


def updateStatus() {
    settings.devices.each() {
        try {
            if (it.currentBattery == null) {
                send("${it.displayName} battery is not reporting.")
            } else if (it.currentBattery > 100) {
                send("${it.displayName} battery is ${it.currentBattery}, which is over 100.")
            } else if (it.currentBattery < settings.level1) {
                send("${it.displayName} battery is ${it.currentBattery} (threshold ${settings.level1}.)")
            }
        } catch (e) {
            log.trace "Caught error checking battery status."
            log.trace e
            send("${it.displayName} battery reported a non-integer level.")
        }
    }
   nightlyStatus()
}