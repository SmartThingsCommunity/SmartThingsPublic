/**
 *  PlantLink Monitor 2
 *
 *  Copyright 2015 Sidney Johnson
 *	Inspired by notoriousbdg's BatteryMonitor 
 *
 *  If you like this app, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
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
 *	Version: 2.0 - Initial Version
 *	Version: 2.1 - Better grammar and plant status detection
 */
definition(
    name: "PlantLink Monitor 2",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Monitors plantlink senors, and sends notifacations when your plants need water",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home10-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home10-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home10-icn@3x.png")

preferences {
    page name:"pageInfo"
}

def pageInfo() {
	return dynamicPage(name: "pageInfo", install: true, uninstall: true) {
		section("About") {
		paragraph "PlantLink Monitor 2, Monitors your Plantlinks and sends notifacations when your plants need water."
        paragraph "If you like this smartapp, please support the developer via PayPal and click on the Paypal link below " 
		href url: "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y",
		title:"Paypal donation..."
		paragraph "${textVersion()}\n${textCopyright()}"
		}
		def plantlist = ""
        def verb = ""
			settings.senors.each() {
			try {
				if (it.currentPlantStatus.contains("Needs")) {
					verb = " "
				}
				else {
					verb = " is "
				}
				plantlist += "$it.displayName$verb${it.currentPlantStatus.toLowerCase()}\n"
			} catch (e) {
                log.trace "Error checking status."
                log.trace e
            }
        }
		if (plantlist) {
			section("Your Plants...") {
				paragraph plantlist.trim()
			}
		}
        section("Select PlantLinks...") {
			input "senors", "device.Plantlink", title: "PlantLink", multiple: true, required: true, submitOnChange: true
		}
		section("Event Notifications..."){
			input "sendPush", "bool", title: "Send as Push?", required: false, defaultValue: true
			input "sendSMS", "phone", title: "Send as SMS?", required: false, defaultValue: null
			input "sendTooDry", "bool", title: "Notify when too dry?", required: false, defaultValue: true
            input "sendTooWet", "bool", title: "Notify when too wet?", required: false, defaultValue: false
		}
	}
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
    unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "PlantLink-Direct Monitor ${textVersion()} ${textCopyright()}"
    subscribe(app, onAppTouch)
	subscribe(location, "sunset", updated)
	subscribe(location, "sunrise", updated)
	schedule("22 32 6,19 1/1 * ?", sendStatus)
}

def onAppTouch(event) {
    log.info "onAppTouch(${event.value})"
    sendStatus()
}

def sendStatus() {
	log.trace "Checking Plants"
    def verb = ""
	sendEvent(linkText:app.label, name:"sendStatus", value:"Checking Plants", descriptionText:"Checking Plants", eventType:"SOLUTION_EVENT", displayed: true)
	settings.senors.each() {
		try {
			if (it.currentPlantStatus.contains("Needs")) {
				verb = " "
			}
			else {
				verb = " is "
			}
			log.trace "${it.displayName}$verb${it.currentPlantStatus.toLowerCase()}"
			sendEvent(linkText:app.label, name:"${it.displayName}", value:"${it.currentPlantStatus}", descriptionText:"${it.displayName}$verb${it.currentPlantStatus.toLowerCase()}", eventType:"SOLUTION_EVENT", displayed: true)
			if (it.currentPlantStatus == "Too Wet" && settings.sendTooWet == true) {
				send("${it.displayName}$verb${it.currentPlantStatus.toLowerCase()}")
			}
			if ((it.currentPlantStatus == "Too Dry" || it.currentPlantStatus == "Needs Water") && settings.sendTooDry == true) {
				send("${it.displayName}$verb${it.currentPlantStatus.toLowerCase()}")
			}
		}
		catch (e) {
			log.trace "Error checking status"
			log.trace e
		}
	}
}

def send(msg) {
    if (settings.sendPush == true) {
        sendPush(msg)
    }
    if (settings.sendSMS != null) {
        sendSms(phoneNumber, msg) 
    }
}

private def textVersion() {
    def text = "Version 2.1"
}

private def textCopyright() {
    def text = "Copyright © 2016 Sidjohn1"
}