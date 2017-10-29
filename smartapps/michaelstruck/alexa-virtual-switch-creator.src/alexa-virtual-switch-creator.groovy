/**
 *  Alexa Virtual Switch Creator
 *
 *  Copyright © 2017 Michael Struck
 *  Version 1.0.0 10/29/17
 * 
 *  Version 1.0.0 - Initial release
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
    name: "Alexa Virtual Switch Creator",
    singleInstance: true,
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Allows for creation of SmartThings virtual switches that can be tied to items controlled by Amazon Echo('Alexa').",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa@2x.png")
preferences {
    page name:"mainPage"
    page name:"pageAbout"
    page name:"pageAddSwitch"
}
def mainPage() {
    dynamicPage(name: "mainPage", uninstall: false, install: true) {
        section("New switch information"){
            input "addSwitchName", "text", title: "Switch Label", description: "Enter a unique label name for the virtual switch", required: false, submitOnChange:true
            input "addSwitchType", "enum", title: "Switch Type...", description: "Choose a switch type", options:["Alexa Switch","Momentary Button Tile"], required: false, submitOnChange:true	
            if (addSwitchType && addSwitchName) href "pageAddSwitch",title: "Add Switch", description: none, image: imgURL()+"add.png"
        }        
        def switchList = ""
        state.sw1Ver = ""
        state.sw2Ver = ""
        def noun = "${getChildDevices().size()} switches"
        if (getChildDevices().size() > 0) {
        	if (getChildDevices().size() == 1) noun = "One switch"
            def count = getChildDevices().size() 
            getChildDevices().each {
            	if (it.typeName=="Alexa Switch" && state.sw1Ver == "") state.sw1Ver = "Alexa Switch Version: ${it.versionNum()}"
                if (it.typeName=="Momentary Button Tile" && state.sw2Ver == "") state.sw2Ver = "Momentary Button Tile Version: ${it.versionNum()}"
                switchList += "${it.label} (${it.typeName})"
                count --
                if (count) switchList += "\n"
            }
		}
        if (getChildDevices().size>0){
        	section ("${noun} created within Alexa Virtual Switch Creator"){paragraph switchList}
    	}
        section (" "){	
            href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or to remove the application",
            	image: imgURL() + "info.png"
		}
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true) {
		section {
        	paragraph "${textAppName()}\n${textCopyright()}", image: "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/smartapps/michaelstruck/alexa-helper.src/Alexa@2x.png"
        }
        section ("SmartApp/Switch Versions") { paragraph "${textVersion()}" }    
        section ("Apache License") { paragraph "${textLicense()}" }
    	section("Instructions") { paragraph textHelp() }
        section("Tap below to remove all switches and application"){
        }
	}
}
// Show "pageAddSwitch" page
def pageAddSwitch() {
	dynamicPage(name: "pageAddSwitch", title: "Add Switch", install: false, uninstall: false) {
    	def repsonse
        if (getChildDevices().find{it.label == addSwitchName}){
            repsonse="There is already a switch labled '${addSwitchName}'.\n\nTap < to go back and change the switch label name."
        }
        else {
         	repsonse = !addSwitchName || !addSwitchType ? "Switch label name or type not specified.\n\nTap < to go back and enter the switch information" :  addChildSwitches()
        }
        section {paragraph repsonse}
    }
}
def installed() {
    initialize()
}
def updated() {
    initialize()
}
def uninstalled(){
	deleteChildSwitches()
}
def initialize() {}
//Common modules (for adding switches)
def addChildSwitches(){
    def deviceID = "AVSC_${app.id}_${getChildDevices().size()}"
    def nameSpace = "MichaelStruck"
    def result
    try {
		def childDevice = addChildDevice(nameSpace, addSwitchType, deviceID, null, [name: deviceID, label: addSwitchName, completedSetup: true])
		log.debug "Created Switch ${addSwitchName}: ${deviceID}"
        result ="The ${addSwitchType} named '${addSwitchName}' has been created.\n\nIf you are using this with Alexa,\nbe sure to 'discover' the switch in your Alexa app."
    } catch (e) {
		log.debug "Error creating switch: ${e}"
        result ="The ${addSwitchType} named '${addSwitchName}' could NOT be created.\n\nEnsure you have the correct device code installed and published within the IDE."
	}
	result + "\n\nTap < to return to the switches page."   
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
//Common modules
def getSwitchAbout(){ return "Created by Alexa Virtual Switch Creator" }
def imgURL() { return "https://raw.githubusercontent.com/MichaelStruck/SmartThingsPublic/master/img/" }
//Version/Copyright/Information/Help
private def textAppName() { return "Alexa Virtual Switch Creator" }	
private def textVersion() {
    def version = "Version: 1.0.0 (10/29/2017)", childVersion, deviceCount= getChildDevices().size()
    childVersion = state.sw1Ver && deviceCount ? "\n${state.sw1Ver}": ""
    childVersion += state.sw2Ver && deviceCount ? "\n${state.sw2Ver}": ""
    return "${version}${childVersion}"
}
private def versionInt(){ return 100 }
private def textCopyright() { return "Copyright © 2017 Michael Struck" }
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
		"Allows the creation of Virtual Momentary Button Tiles, or Alexa Switches (special virtual dimmers that allow for non-state updates)."+
        "You can then attach these switches to various SmartThings automations (WebCoRE, Amazon Alexa, etc)."
}