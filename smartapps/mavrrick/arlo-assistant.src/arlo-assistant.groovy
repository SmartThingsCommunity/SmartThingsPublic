/**
 *  Arlo Assistant
 *
 *  Copyright 2018 CRAIG KING
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
    name: "Arlo Assistant",
    namespace: "Mavrrick",
    author: "CRAIG KING",
    description: "Help Manage Arlo Cameras from Smartthings",
    category: "Safety & Security",
	iconUrl: "https://farm9.staticflickr.com/8632/16461422990_e5121d68ee_o.jpg",
	iconX2Url: "https://farm9.staticflickr.com/8632/16461422990_e5121d68ee_o.jpg",
	iconX3Url: "https://farm9.staticflickr.com/8632/16461422990_e5121d68ee_o.jpg",
    singleInstance: true)

/* 
*
* Prerelease v.0.1
* This is a prerelease of ArloAssistant to provide programs already created and ready to use.
*
*/


preferences {
		page (name: "mainPage", title: "ADT Tools")
        page (name: "arloModeSetup", title: "Arlo Mode Setup")
        page (name: "arloCameraMan", title: "Arlo Camera Management")
        page (name: "about", title: "About ADT Tools")
}

def mainPage()
{
	dynamicPage(name: "mainPage", title: "Arlo Assistant", uninstall: true, install: true)
	{
// The parent app preferences are pretty simple: just use the app input for the child app.
    	section ("Current state"){
        paragraph "This is a early release version. More features will be coming"
         }
		section ("Arlo Assistant Management"){
		href "arloModeSetup", title: "Arlo Asistant Mode Management", description: "Create Custom Smartthings Arlo integration Modes."
        href "arloCameraMan", title: "Arlo Camera Management", description: "Create rules around camera management."
//			app(name: "arloTriggerRecord", appName: "Arlo Triggered Record", namespace: "Mavrrick", title: "Create a triggered event that will create recording", multiple: true)
//            app(name: "arloTriggerRecordRepeat", appName: "Arlo Triggered Repeat Recorder", namespace: "Mavrrick", title: "Create a triggered event that will record until the event is over", multiple: true)            
            }
/*        section ("Arlo Camera Management"){
            app(name: "arloImageRefresh", appName: "Arlo Image/Clip Refresh", namespace: "Mavrrick", title: "Generate a clip to refresh the image tile on the camera", multiple: true)
		} */
        section ("Arlo Assistant About"){
        href "about", title: "Arlo Asistant About", description: "Arlo Asistant About"
        }
    }
}

def arloModeSetup()
{
	dynamicPage(name: "arloModeSetup", title: "Arlo Assistant mode Management", uninstall: false, install: true)
	{
// The parent app preferences are pretty simple: just use the app input for the child app.
        section ("Arlo Smartthings Mode management"){
			app(name: "arloSmartthingsMode", appName: "Arlo Smartthings Mode", namespace: "Mavrrick", title: "Create Arlo Smarthings integration Mode", multiple: true)
            }
    }
}

def arloCameraMan()
{
	dynamicPage(name: "arloCameraMan", title: "Arlo Assistant Camera Management", uninstall: false, install: true)
	{
// The parent app preferences are pretty simple: just use the app input for the child app.
        section ("Arlo Smartthings Mode management"){
			app(name: "arloCameraReadyCheck", appName: "Arlo Camera Ready Check", namespace: "Mavrrick", title: "Create Arlo Camera Health Check", multiple: true)
            }
    }
}

def about()
{
	dynamicPage(name: "about", title: "Arlo Assistant About", uninstall: false, install: false)
	{
		section()
		{
			paragraph image: "https://farm9.staticflickr.com/8632/16461422990_e5121d68ee_o.jpg", "Arlo Assistant"
		}
        section("Support locations")
		{
			href (name: "thingsAreSmart", style:"embedded", title: "Things That Are Smart Support Page", url: "http://thingsthataresmart.wiki/index.php?title=Arlo_Assistant")
			href (name: "smtReleaseThd", style:"embedded", title: "Smartthings Community Support Thread", url: "https://community.smartthings.com/t/beta-arlo-assitant-arlo-enhanced-management-from-smartthings/163612")
		}
        section("Support the Project")
		{
			paragraph "Arlo Assistant is provided free for personal and non-commercial use.  I have worked on this app in my free time to fill the needs I have found for myself and others like you.  I will continue to make improvements where I can. If you would like you can donate to continue to help with development please use the link below."
			href (name: "donate", style:"embedded", title: "Consider making a \$5 or \$10 donation today.", url: "https://www.paypal.me/mavrrick58")
		}
        section ("Return to Arlo Assistant Main page"){
            href "mainPage", title: "Arlo Assistant Main Menu", description: "Return to main Arlo Assistant Main Menu"            
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

def initialize() {
    // nothing needed here, since the child apps will handle preferences/subscriptions
    // this just logs some messages for demo/information purposes
    log.debug "there are ${childApps.size()} child smartapps"
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}