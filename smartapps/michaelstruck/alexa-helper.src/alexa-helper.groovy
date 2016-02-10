/**
 *  Alexa Helper-Parent
 *
 *  Copyright © 2016 Michael Struck
 *  Version 4.2.0a 2/10/16
 * 
 *  Version 1.0.0 - Initial release
 *  Version 2.0.0 - Added 6 slots to allow for one app to control multiple on/off actions
 *  Version 2.0.1 - Changed syntax to reflect SmartThings Routines (instead of Hello, Home Phrases)
 *  Version 2.1.0 - Added timers to the first 4 slots to allow for delayed triggering of routines or modes
 *  Version 2.2.1 - Allow for on/off control of switches and changed the UI slightly to allow for other controls in the future
 *  Version 2.2.2 - Fixed an issue with slot 4
 *  Version 3.0.0 - Allow for parent/child 'slots'
 *  Version 3.1.0 - Added ability to control a thermostat
 *  Version 3.1.1 - Refined thermostat controls and GUI (thanks to @SDBOBRESCU "Bobby")
 *  Version 3.2.0 - Added ability to a connected speaker
 *  Version 3.3.0 - Added ability to change modes on a thermostat
 *  Version 3.3.1 - Fixed a small GUI misspelling
 *  Version 3.3.2 - Added option for triggering URLs when Alexa switch trips
 *  Version 3.3.3 - Added version number for child apps within main parent app
 *  Version 3.3.4 - Updated instructions, moved the remove button, fixed code variables and GUI options
 *  Version 4.0.0 - Moved the speaker and thermostat controls to the scenario child app and optimized code
 *  Version 4.1.0 - Updated the instructions to reflect new functionality within the scenarios and new options page
 *  Version 4.1.1 - Minor syntax clean up
 *  Version 4.2.0a - Added dropdown for number of Sonos memory slots
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
    name: "Alexa Helper",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Allows for various SmartThings devices to be tied to switches controlled by Amazon Echo('Alexa').",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/Alexa@2x.png")
preferences {
    page name:"mainPage"
    page name:"pageAbout"
    page name:"pageSettings"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title: "Alexa Helper Scenarios", install: true, uninstall: false) {
		section {
        	def childCount = childApps.size()
        	if (childCount){
        		def childVersion = childApps[0].versionInt()
            	if (childVersion < 240){
            		paragraph "You are using a version of the child app that is older than the recommended version. Please upgrade "+
                    	"to the latest version to ensure you have the latest features and bug fixes."
            	}
            }
			app(name: "childScenarios", appName: "Alexa Helper-Scenario", namespace: "MichaelStruck", title: "Create New Alexa Scenario...", multiple: true)
		}
		section([title:"Options", mobileOnly:true]) {
			href "pageSettings", title: "Settings", description: "Tap to edit Alexa Helper settings"
            label title:"Assign a name", required:false
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application"
		}
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", title: "About ${textAppName()}", uninstall: true) {
		section {
    		paragraph "${textVersion()}\n${textCopyright()}\n\n${textLicense()}\n"
    	}
    	section("Instructions") {
        	paragraph textHelp()
    	}
        section("Tap button below to remove all scenarios and application"){
        }
	}
}
def pageSettings(){
    dynamicPage(name: "pageSettings", title: "Settings", install: false, uninstall: false) {	
        section {
            input "speakerSonos", "bool", title: "Allow Sonos special options", defaultValue: false, submitOnChange:true
                if (speakerSonos){
                    input "memoryCount", "enum", title: "Maximum number of Sonos memory slots", options: [2:"2",3:"3",4:"4",5:"5",6:"6",7:"7",8:"8"], defaultValue: 2, required: false
                }
            input "tstatNest", "bool", title: "Allow Nest special options", defaultValue: false
            input "showRestrictions", "bool", title: "Show scenario restrictions", defaultValue: true
        }
    }
}
def installed() {
    initialize()
}
def updated() {
    initialize()
    unsubscribe()
}
def initialize() {
    childApps.each {child ->
		log.info "Installed Scenario: ${child.label}"
    }
}
//Common modules (for child app)
def getSonos(){
	def result = speakerSonos
}
def getRestrictions(){
	def result = showRestrictions
}
def getNest(){
	def result = tstatNest
}
def getMemCount(){
	def result = memoryCount ? memoryCount : 2
}
//Version/Copyright/Information/Help
private def textAppName() {
	def text = "Alexa Helper"
}	
private def textVersion() {
    def version = "Parent App Version: 4.2.0a (02/10/2016)"
    def childCount = childApps.size()
    def childVersion = childCount ? childApps[0].textVersion() : "No scenarios installed"
    return "${version}\n${childVersion}"
}
private def versionInt(){
	def text = 420
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
		"Ties various SmartThings functions to the on/off state of specifc switches. You may also control a thermostat or the volume of a wireless speaker using a dimmer control. "+
		"Perfect for use with the Amazon Echo ('Alexa').\n\nTo use, first create the required momentary button tiles or 'Alexa Switch' (custom switch/dimmer) from the SmartThings IDE. "+
		"You may also use any physical switches already associated with SmartThings. Include these switches within the Echo/SmartThings app, then discover the switches on the Echo. "+
		"For control over SmartThings aspects such as modes and routines, add a new scenario choosing Modes/Routines/Devices/HTTP/SHM scenario type. "+
		"Within scenario settings, choose the Alexa discovered switch to be monitored and tie the on/off state of that switch to a specific routine, mode, URL, Smart Home Monitor "+
		"security state or the state of other SmartThings devices. The chosen functions or devices will fire when the main switch changes, except in cases where you have a delay "+ 
		"specified. This time delay is optional. "+
		"\n\nPlease note that if you are using a momentary switch you should only define either an 'on' action or an 'off' action within each scenario, but not both.\n\n" +
		"To control a thermostat, add a new scenario choosing the 'thermostat' scenario type, then under the settings choose a dimmer switch (usually an Alexa Switch) and " +
		"the thermostat you wish to control. You can also control the on/off of the thermostat with the state of the dimmer switch, limit the range the thermostat will reach "+
		"(for example, even if you accidently set the Alexa Switch to 100, the value sent to the thermostat could be limited to 72) or set the initial value of the thermostat when "+
		"you change modes. Momentary switches can be used to activate the thermostat's heating, cooling, or auto modes."+
		"\n\nTo control a connected speaker, add a new scenario choosing the 'speaker' scenario type, then under the settings choose a dimmer switch (usually an Alexa Switch) "+
		"and speaker you wish to control. You can set the initial volume upon turning on the speaker, along with volume limits. Finally, you can utilize other virtual momentary "+
		"button tiles to choose next/previous tracks or (if you have a Sonos speaker) specific memory slots for songs/internet stations." +
		"\n\nYou can also sent up panic commands that will turn on a connected strobe/alarm device or send an SMS message to someone when activated."
}