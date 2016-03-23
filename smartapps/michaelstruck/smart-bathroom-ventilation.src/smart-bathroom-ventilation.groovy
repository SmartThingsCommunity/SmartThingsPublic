/**
 *  Smart Bathroom Ventilation-Parent
 *
 *  Version - 2.1.2 3/23/16
 * 
 *  Version 1.0.0 - Initial release
 *  Version 1.1.0 - Added restrictions for time that fan goes on to allow for future features along with logic fixes
 *  Version 1.2.0 - Added the option of starting the fans based on time (to eliminate the time of polling and for those without a humidity sensor)
 *  Version 2.0.0 - Modified to allow more scenarios via parent/child app structure
 *  Version 2.0.1 - Allow ability to see child app version in parent app and moved the remove button
 *  Version 2.1.0 - Added icon on about page
 *  Version 2.1.1 - Removed label from parent app
 *  Version 2.1.2 - Added icons to the main menu
 *
 * 
 *  Copyright 2016 Michael Struck - Uses code from Lighting Director by Tim Slagle & Michael Struck
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
    name: "Smart Bathroom Ventilation",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Control multiple ventilation scenarios based on humidity or certain lights being turned on.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Bathroom-Ventilation/BathVent.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Bathroom-Ventilation/BathVent@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Bathroom-Ventilation/BathVent@2x.png"
    )

preferences {
    page name:"mainPage"
    page name:"pageAbout"
}

def mainPage(){
    dynamicPage(name: "mainPage", title: "Ventilation Scenarios", install: true, uninstall: false, submitOnChange: true) {
		section {
        	app(name: "childScenarios", appName: "Smart Bathroom Ventilation-Scenario", namespace: "MichaelStruck", title: "Create New Scenario...", multiple: true)
		}
		section([title:"Options", mobileOnly:true]) {
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/img/info.png"
		}
	}
}

def pageAbout() {
	dynamicPage(name: "pageAbout" , uninstall: true) {
    	section {
        paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/Smart-Bathroom-Ventilation/BathVent@2x.png"
        }
        section ("SmartApp Versions") {
			paragraph textVersion()
        }    
        section ("Apache License") { 
        	paragraph textLicense()
    	}
		section("Instructions") {
            paragraph textHelp()
		}
        section("Tap button below to remove all scenarios and application"){
        }
	}
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    childApps.each {child ->
		log.info "Installed Scenario: ${child.label}"
    }    
}

//Version/Copyright/Information/Help

private def textAppName() {
	def text = "Smart Bathroom Ventilation"
}	

private def textVersion() {
    def version = "Parent App Version: 2.1.2 (03/23/2016)"
    def childCount = childApps.size()
    def childVersion = childCount ? childApps[0].textVersion() : "No scenarios installed"  
    return "${version}\n${childVersion}"
}

private def textCopyright() {
    def text = "Copyright © 2016 Michael Struck"
}

private def textLicense() {
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

private def textHelp() {
	def text =
    	"Within each scenario, select a light switch to monitor, a humidity sensor (optional), and fans to control. You can choose when " +
        "the ventilation fans comes on; either when the room humidity rises over a certain level or after a user definable time after the light switch is turned on. "+
        "The ventilation fans will turn off based on either a timer setting, humidity, or the light switch being turned off. " +
        "You can also choose to have the ventilation fans turn off automatically if they are turned on manually. "+
        "Each scenario can be restricted to specific modes, times of day or certain days of the week."
}