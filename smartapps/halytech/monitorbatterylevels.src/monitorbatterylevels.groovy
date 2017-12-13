/**
 *  MonitorBatteryLevels
 *
 *  Copyright 2017 John McClure
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
    name: "MonitorBatteryLevels",
    namespace: "halytech",
    author: "John McClure",
    description: "Monitor Battery Levels on all devices and send notifications if their power level falls beneath a certain threshold",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances17-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances17-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances17-icn@3x.png")


preferences {
	// Create a page for preferences.  Could use this to make it dynamic later
	page name:"pageInfo"
}

def pageInfo() {
	return dynamicPage(name: "pageInfo", install: true, uninstall:true) {
        // About box
        section("About") {
        paragraph "Monitor Battery Levels, Monitors battery level on all devices currently on batteries and warns if they fall below a predefined level."
        paragraph "${textVersion()}\n${textCopyright()}"
        }
        
        // If there are already some devices selected for monitoring,
        // show them here and their current battery levels.
        def tempList = ""
        settings.batteries.each() {batt ->
            try {
                tempList += "${batt.displayName} battery is at ${batt.currentbattery}\n"
            } catch (e) {
                log.trace "Error checking battery levels for ${batt.displayName}."
                log.trace e
            }
        }
        
        if(tempList)
        {
            section("Monitored Batteries") {
                paragraph tempList.trim()
            }
        }
	
    	// Select the batteries to monitor and the level overall at which to warn
        // Might come back later and make this more of an array where the warning
        // level can be set per device.
		section("Select batteries to monitor") {
        	input("batteries", "capability.battery", title: "Select...", multiple:true, required:true, submitOnChange:true)
            input("lowestLevel", "number", title:"Battery Warning Level", defaultValue: "25", range: "0..100", required:true, submitOnChange:true)
            
        }
        
        // Lazy set of frequencies for monitoring.  Using the recommended
        // runEvery calls but really need to consider eiher more granularity
        // or some kind of initial warning and notification that the batteries
        // are low with less frequent push or sms notifications so you don't
        // get spammed if your devices are low but get a decent amount of
        // logging notifications.
        section("Frequency:") {
        	input("runEvery", "enum",title:"Select frequency of check",options: [
            "1min":"1 minute",
            "5min":"5 minutes",
            "10min":"10 minutes",
            "15min":"15 minutes",
            "30min":"30 minutes",
            "1hour":"1 hour",
            "3hour":"3 hours",
            "1day":"Once per day"])
            }
            
        // Notifications, either push, contacts, or specific SMS number (since contacts
        // I don't think is fully implemented yet)
        section("Notifications") {
        	input("sendPush", "bool", required:false, title:"Send Push when battery low?") 
        	input("recipients", "contact", title: "Send notifications to")
            {
            	input "phone", "phone", title: "Warn with text message (optional)", 
                		description: "Phone Number", required:false
                        
            }
        }
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

private def textVersion() {
    def text = "Version 1.1"
}

private def textCopyright() {
    def text = "Copyright © 2017 halytech"
}

def initialize() {
    
    log.info "Battery Monitor ${textVersion()} ${textCopyright()}"
    
    // Unschedule first.  Since this is only run on initialization it shouldn't beat things up
    // too badly (read that this was a costly function)
    unschedule()
    
    switch(settings.runEvery) {
    	case "1min":
        	runEvery1Minute(checkBatteries)
        	break
        case "5min":
        	runEvery5Minutes(checkBatteries)
            break
        case "10min":
        	runEvery10Minutes(checkBatteries)
            break
        case "15min":
        	runEvery15Minutes(checkBatteries)
            break
        case "30min":
        	runEvery30Minutes(checkBatteries)
            break
        case "1hour":
        	runEvery1Hour(checkBatteries)
            break
        case "3hour":
        	runEvery3Hours(checkBatteries)
            break
        case "1day": // In this case, it will run every day at the time the app starts this schedule.
        	schedule(new Date(),checkBatteries)
         	break
        default:
        	schedule(new Date(),checkBatteries)
        }
     
}


def checkBatteries() {
	log.trace "Checking batteries...\n"
    
    sendEvent(linkText:app.label, name:"checkBatteries", value:"Checking Device Batteries", descriptionText:"Checking Device Batteries", eventType:"SOLUTION_EVENT", displayed: true)
    
    def batteryLowList = ""
    
    settings.batteries.each() {batt ->
    	try {
        	log.trace "${batt.displayName} battery level is currently ${batt.currentbattery} percent"
            sendEvent(linkText:batt.displayName, name:"${batt.displayName}", value:"${batt.displayName} - ${batt.currentbattery} percent battery remains", descriptionText:"${batt.displayName} - ${batt.currentbattery} percent battery remains", eventType:"SOLUTION_EVENT", displayed: true)
		} catch (e) {
        	log.trace "Error checking battery"
			log.trace e
        }
		
        // If the battery level is below the set threshold percentage, 
        // add it to a list of batteries to add to the notifications
        // and pushes.
        if(settings.lowestLevel > batt.currentbattery) { 
        	batteryLowList += "${batt.displayName} battery level is low (${batt.currentbattery}%).  Please recharge or replace battery.\n"
            sendEvent(linkText:batt.displayName, name:"${batt.displayName}Low", value:"${batt.displayName} battery level is low (${batt.currentbattery}%).", descriptionText:"${batt.displayName} battery level is low (${batt.currentbattery}%).",eventType:"SOLUTION_EVENT", displayed: true)
		}
    }
    
    log.trace batteryLowList
    
    if(batteryLowList) sendNotifications(batteryLowList)
}

def sendNotifications(lowList) {
	def pushMethod = ""

	// Send out the notifications based upon the sendPush and phone number being
    // defined.  Need to revisit this when contact information is usable to 
    // parse through the list of recipients and send it to all of them instead
    // of a single phone number.
    
    if(	settings.sendPush && settings.phone) {
    	log.trace "Sending notification to push and phone ${settings.phone}"
    	sendNotification(lowList.trim(), [method: "both", phone: settings.phone])
    } else if (settings.sendPush) {
    	log.trace "Sending notification to push"
    	sendNotification(lowList.trim(), [method: "push"])
    } else if (settings.phone) {
    	log.trace "Sending notification to phone ${settings.phone}"
    	sendNotification(lowList.trim(), [method: "phone", phone: settings.phone])
    } else {
    	sendNotificationEvent(lowList.trim())
	}	

}