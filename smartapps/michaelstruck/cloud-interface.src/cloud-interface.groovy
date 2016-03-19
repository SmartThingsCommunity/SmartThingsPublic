/**
 *  Cloud Interface
 *
 *  Version 1.3.0 - 3/19/16 Copyright © 2016 Michael Struck
 *  
 *  Version 1.0.0 - Initial release
 *  Version 1.0.1 - Fixed code syntax
 *  Version 1.0.2 - Fixed additional syntax items and moved the remove button to the help screen
 *  Version 1.0.3 - Fixed OAuth reset/code optimization
 *  Version 1.0.4 - Changed name to allow it to be used with other SmartApps instead of associating it with Alexa Helper
 *  Version 1.1.0 - Code optimization and URL page improvements
 *  Version 1.1.1 - GUI clean up
 *  Version 1.2.0 - Address URL accesses via API instead of hard coding it
 *  Version 1.3.0 - Added icon to app about page
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
    name: "Cloud Interface",
    namespace: "MichaelStruck",
    author: "Michael Struck",
    description: "Allows for URL cloud interfacing to control SmartThings devices.",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/CloudInterface.png",
    iconX2Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/CloudInterface@2x.png",
    iconX3Url: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/CloudInterface@2x.png",
  	oauth: true)
preferences {
    page name:"mainPage"
    page name:"showURLs"
    page name:"pageReset"
    page name:"pageAbout"
}
//Show main page
def mainPage() {
    dynamicPage(name: "mainPage", title:"", install: true, uninstall: false) {
		section("External control") {
        	input "switches", "capability.switch", title: "Choose Switches", multiple: true, required: false, submitOnChange:true
			if (switches){
            	if (!state.accessToken) {
					OAuthToken()
				}
                if (state.accessToken != null){
                    href url:"${getApiServerUrl()}/api/smartapps/installations/${app.id}/l?access_token=${state.accessToken}", style:"embedded", required:false, title:"Show URLs", description: none
                }
                else {
                	paragraph "URLs cannot be created. Access Token not defined. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
               }
			}
        }
        section([title:"Options", mobileOnly:true]) {
			href "pageSettings", title: "Settings", description: none
			href "pageAbout", title: "About ${textAppName()}", description: "Tap to get application version, license, instructions or remove the application"
        }
	}
}
def pageAbout(){
	dynamicPage(name: "pageAbout", uninstall: true ) {
        section {
        	paragraph "${textAppName()}\n${textCopyright()}",image: "https://raw.githubusercontent.com/MichaelStruck/SmartThings/master/Other-SmartApps/AlexaHelper/CloudInterface@2x.png"
        }
        section ("SmartApp Version/Access Token"){
            if (!state.accessToken) {
				OAuthToken()
			}
            def msg = state.accessToken != null ? state.accessToken : "Could not create Access Token. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
            paragraph "${textVersion()}\n\nAccess Token:\n${msg}"
    	}
        section ("Apache License"){
        	paragraph "${textLicense()}"
        }
    	section("Instructions") {
        	paragraph textHelp()
    	}
        section("Tap button below to remove the application"){
        }
	}
}
page(name: "pageSettings", title: "Settings"){
	section("URL Settings"){
    	input "urlOnOff", "bool", title: "Show both ON/OFF links on 'Show URLs' page (default=show ON only)", defaultValue: Off
    }
    section("Security Settings"){
    	href "pageReset", title: "Reset Access Token", description: "Tap to revoke access token. All current URLs in use will need to be re-generated"
	}
}
def pageReset(){
	dynamicPage(name: "pageReset", title: "Access Token Reset"){
        section{
			state.accessToken = null
            OAuthToken()
            def msg = state.accessToken != null ? "New access token:\n${state.accessToken}\n\nClick 'Done' above to return to the previous menu." : "Could not reset Access Token. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
	    	paragraph "${msg}"
		}
	}
}
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}
def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}
def initialize() {
	if (!state.accessToken) {
		log.error "Access token not defined. Ensure OAuth is enabled in the SmartThings IDE and generate the Access Token in the help or URL pages within the app."
	}
}
mappings {
      path("/w") {action: [GET: "writeData"]}
      path("/l") {action: [GET: "listURLs"]}
}
def writeData() {
    log.debug "Command received with params $params"
	def command = params.c  	//The action you want to take i.e. on/off 
	def label = params.l		//The name given to the device by you
	if (switches){
		def device = switches?.find{it.label == label}
       	device."$command"()
	}
}
def listURLs() {
	render contentType: "text/html", data: """<!DOCTYPE html><html><head><meta charset="UTF-8" /></head><body style="margin: 0;">${displayURLS()}</body></html>"""
}
//Common Code
def OAuthToken(){
	try {
		createAccessToken()
		log.debug "Creating new Access Token"
	} catch (e) {
		log.error "Access Token not defined. OAuth may not be enabled. Go to the SmartApp IDE settings to enable OAuth."
	}
}
def displayURLS(){
	def display = "<div style='padding:10px'>Copy the URLs of the switches you want to control.<br>Paste them to your control applications.</div><div style='padding:10px'>Click DONE to return to the Cloud Interface SmartApp.</div>"
	switches.each {
    	display += "<div style='padding:10px'>${it.label} ON:</div>"
		display += "<textarea rows='5' style='font-size:10px; width: 100%'>${getApiServerUrl()}/api/smartapps/installations/${app.id}/w?l=${it.label}&c=on&access_token=${state.accessToken}</textarea>"
		if (urlOnOff){
        	display += "<div style='padding:10px'>${it.label} OFF:</div>"
        	display += "<textarea rows='5' style='font-size:10px; width: 100%'>${getApiServerUrl()}/api/smartapps/installations/${app.id}/w?l=${it.label}&c=off&access_token=${state.accessToken}</textarea>"
		}
		display += "<hr>"
    }
    display
}
//Version/Copyright/Information/Help
private def textAppName() {
	def text = "Cloud Interface"
}	
private def textVersion() {
    def text = "Version 1.3.0 (03/19/2016)"
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
		"This app allows you to define switches (typically virtual) to be controlled via a single URL REST point. "+
		"This is useful when controlling devices at two different SmartThings locations/accounts. "
}