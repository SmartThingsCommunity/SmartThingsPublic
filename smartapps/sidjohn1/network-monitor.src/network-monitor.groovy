/**
 *  Network Monitor
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
 *	Version: 1.0 - Initial Version
 */
definition(
    name: "Network Monitor",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Monitors rssi status",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@3x.png")

preferences {
    page name:"pageInfo"
}

def pageInfo() {
	return dynamicPage(name: "pageInfo", install: true, uninstall: true) {
		section("About") {
		paragraph "Network Monitor, Monitors your rssi status and puts all the data in one place."
		paragraph "${textVersion()}\n${textCopyright()}"
		}
		def templist = ""
			settings.senors.each() {
			try {
				templist += "$it.displayName is $it.currentRssi RSSI and $it.currentLqi% Link quality\n"
			} catch (e) {
                log.trace "Error checking status."
                log.trace e
            }
        }
		if (templist) {
			section("Your Monitored Sensors...") {
				paragraph templist.trim()
			}
		}
        section("Select Sensors to monitor...") {
			input "senors", "capability.signalStrength", title: "Select...", multiple: true, required: true, submitOnChange: true
		}
	}
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "Network Monitor ${textVersion()} ${textCopyright()}"
	schedule("22 32 6,19 1/1 * ?", sendStatus)
}

def sendStatus() {
	log.trace "Checking Network"
	sendEvent(linkText:app.label, name:"sendStatus", value:"Checking Network", descriptionText:"Checking Network", eventType:"SOLUTION_EVENT", displayed: true)
	settings.senors.each() {
		try {
			log.trace "$it.displayName is $it.currentRssi RSSI and $it.currentLqi% Link quality"
			sendEvent(linkText:app.label, name:"${it.displayName}", value:"${it.currentRssi} RSSI and ${it.currentLqi}% Link quality", descriptionText:"${it.displayName} is ${it.currentRssi} RSSI and ${it.currentLqi}% Link quality", eventType:"SOLUTION_EVENT", displayed: true)
		}
		catch (e) {
			log.trace "Error checking status"
			log.trace e
		}
	}
}

private def textVersion() {
    def text = "Version 1.0"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1"
}