/**
 *  Alexa Helper-Parent
 *
 *  Copyright © 2016 Michael Struck
 *  Version 4.5.2 4/21/16
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
 *  Version 4.3.0 - Added notification options, refined GUI
 *  Version 4.4.0a - Added option to add switches from the app instead of going to the IDE; GUI clean up
 *  Version 4.4.1 - Added routine for switch info feedback
 *  Version 4.4.2 - Minor GUI tweaks
 *  Version 4.4.3b - Added ability to poll device version numbers, showing in About screen
 *  Version 4.4.4 - Fixed bug in hub ID
 *  Version 4.4.5 - Added voice reporting in the help section
 *  Version 4.4.6 - Small syntax fixes
 *  Version 4.5.0 - Added icon to app about page
 *  Version 4.5.1b - Minor syntax changes, added main menu icons, new location for icons
 *  Version 4.5.2 - Minor GUI changes to accomodate new mobile app structure
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
definition(
    name: "Alexa Helper",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Allows for various SmartThings devices to be tied to switches controlled by Amazon Echo('Alexa').",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa@2x.png")
preferences {
    page name:"mainPage"
    page name:"pageAbout"
    page name:"pageSettings"
    page name:"pageSwitches"
    page name:"pageAddSwitch"
}
//Show main page
def mainPage() {
	dynamicPage(name: "mainPage", title: "Alexa Helper Scenarios", install: true, uninstall: false) {
		section {
        	def childCount = childApps.size()
        	if (childCount){
        		def childVersion = childApps[0].versionInt()
            	if (childVersion < 292){
            		paragraph "You are using a version of the child app that is older than the recommended version. Please upgrade "+
                    	"to the latest version to ensure you have the latest features and bug fixes."
            	}
            }
			app(name: "childScenarios", appName: "Alexa Helper-Scenario", namespace: "MichaelStruck", title: "Create New Alexa Scenario...", multiple: true)
		}
		section("Options") {
			href "pageSettings", title: "Configure Settings", description: none, 
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/settings.png"
            if (showAddSwitches) {
            	def titleTxt = getChildDevices().size() > 0 ? "Add/View Virtual Switches" : "Add Virtual Switches"
                def descTxt = getChildDevices().size() > 1 ? "${getChildDevices().size()} virtual switches created" : getChildDevices().size() == 1 ? "One virtual switch created" : ""
                href "pageSwitches", title: "${titleTxt}", description: "${descTxt}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/add.png"
			}
            href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application",
            	image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/info.png"
		}
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
		section {
        	paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa@2x.png"
        }
        section ("SmartApp/Switch Versions") {
    		paragraph "${textVersion()}"
        }    
        section ("Apache License") {
        	paragraph "${textLicense()}"
    	}
    	section("Instructions") {
        	paragraph textHelp()
    	}
        section("Tap below to remove all scenarios, switches and application"){
        }
	}
}
def pageSettings(){
    dynamicPage(name: "pageSettings", title: "Configure Settings", install: false, uninstall: false) {	
        section {
            input "speakerSonos", "bool", title: "Show Sonos options", defaultValue: false, submitOnChange:true
                if (speakerSonos){
                    input "memoryCount", "enum", title: "Maximum number of Sonos memory slots", options: [2:"2",3:"3",4:"4",5:"5",6:"6",7:"7",8:"8"], defaultValue: 2, required: false
                }
            input "tstatNest", "bool", title: "Show Nest options", defaultValue: false
            input "showRestrictions", "bool", title: "Show scenario restrictions", defaultValue: true
            input "showAddSwitches", "bool", title: "Allow in-app virtual switch creation", defaultValue: false
        	input "showNotifyFeed", "bool", title: "Post activity to notification feed" , defaultValue: false
        }
    }
}
def pageSwitches() {
    dynamicPage(name: "pageSwitches", title: "Add/View Virtual Switches", install: false, uninstall: false) {
    	section("New switch information"){
            input "addSwitchName", "text", title: "Switch Label", description: "Enter a unique label name for the virtual switch", required: false, submitOnChange:true
            input "addSwitchType", "enum", title: "Switch Type...", description: "Choose a switch type", options:["Alexa Switch","Momentary Button Tile"], required: false, submitOnChange:true	
            if (addSwitchType && addSwitchName) href "pageAddSwitch",title: "Add Switch", description: none
        }        
        def switchList = ""
        state.sw1Ver = ""
        state.sw2Ver = ""
        def noun = "${getChildDevices().size()} switches"
        if (getChildDevices().size() > 0) {
        	if (getChildDevices().size() == 1) noun = "One switch"
            getChildDevices().each {
            	if (it.typeName=="Alexa Switch" && state.sw1Ver == "") state.sw1Ver = "Alexa Switch Version: ${it.versionNum()}"
                if (it.typeName=="Momentary Button Tile" && state.sw2Ver == "") state.sw2Ver = "Momentary Button Tile Version: ${it.versionNum()}"
                switchList += "${it.label} (${it.typeName})\n"
            }
		}
        else switchList = "\n"
        section ("${noun} created within Alexa Helper"){paragraph switchList}
    }
}
// Show "pageAddSwitch" page
def pageAddSwitch() {
	dynamicPage(name: "pageAddSwitch", title: "Add Switch", install: false, uninstall: false) {
    	def repsonse
        if (getChildDevices().find{it.label == addSwitchName}){
            repsonse="There is already a switch labled '${addSwitchName}'.\n\nTap Done to go back and change the switch label name."
        }
        else {
         	repsonse = !addSwitchName || !addSwitchType ? "Switch label name or type not specified.\n\nTap Done to go back and enter the switch information" :  addChildSwitches()
        }
        section {paragraph repsonse}
    }
}
def installed() {
    initialize()
}
def updated() {
    initialize()
    unsubscribe()
}
def uninstalled(){
	deleteChildSwitches()
}
def initialize() {
    childApps.each {child ->log.info "Installed Scenario: ${child.label}"}
}
//Common modules (for adding switches)
def addChildSwitches(){
    def deviceID = "AH_${app.id}_${getChildDevices().size()}"
    def nameSpace = "MichaelStruck"
    def result
    try {
		def childDevice = addChildDevice(nameSpace, addSwitchType, deviceID, null, [name: deviceID, label: addSwitchName, completedSetup: true])
		log.debug "Created Switch ${addSwitchName}: ${deviceID}"
        result ="The ${addSwitchType} named '${addSwitchName}' has been created.\n\nBe sure to 'discover' the switch in your Alexa app."
    } catch (e) {
		log.debug "Error creating switch: ${e}"
        result ="The ${addSwitchType} named '${addSwitchName}' could NOT be created.\n\nEnsure you have the correct device code installed and published within the IDE."
	}
	result + "\n\nTap Done to return to the switches page."   
}
def deleteChildSwitches() {
    getChildDevices().each {
    	log.debug "Deleting switch ID: " + it.deviceNetworkId
        try {
            deleteChildDevice(it.deviceNetworkId)
        } catch (e) {
            log.debug "Fatal exception ${e}"
        }
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
def getNotifyFeed(){
	def result = showNotifyFeed
}
def getSwitchAbout(){
	def result = "Created by Alexa Helper SmartApp"
}
//Version/Copyright/Information/Help
private def textAppName() {
	def text = "Alexa Helper"
}	
private def textVersion() {
    def version = "Parent App Version: 4.5.2 (04/21/2016)"
    def childCount = childApps.size()
    def deviceCount= getChildDevices().size()
    def childVersion = childCount ? childApps[0].textVersion() : "No scenarios installed"
    childVersion += state.sw1Ver && deviceCount ? "\n${state.sw1Ver}": ""
    childVersion += state.sw2Ver && deviceCount ? "\n${state.sw2Ver}": ""
    return "${version}\n${childVersion}"
}
private def versionInt(){
	def text = 452
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
		"Ties various SmartThings functions to the on/off state of specifc switches. You may also control a thermostat, baseboard heaters, "+
        "the volume of a wireless speakers, define a panic command or report on the status of various devices using " +
        "either a dimmer control or momentary button tile. Perfect for use with the Amazon Echo ('Alexa').\n\n" +
		"To use, first create the required momentary button tiles or 'Alexa Switch' (custom switch/dimmer) from the SmartThings IDE or the SmartApp. "+
        "You may also use any physical switches already associated with SmartThings. Include these switches within the Echo/SmartThings app, then discover the "+ 
        "switches on the Echo. Then, create a new scenario that best fits your needs, associating the switches with the various controls within the scenario.\n\n" +
        "For more information, go to http://thingsthataresmart.wiki/index.php?title=Alexa_Helper"    
}