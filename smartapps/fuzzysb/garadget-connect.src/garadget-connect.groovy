/**
 *  Garadget Connect
 *
 *  Copyright 2016 Stuart Buchanan
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
 * 12/12/2017 V1.5 fixed bug introduced in v1.4 on initialize function.
 * 12/12/2017 V1.4 debug logging changes. - btrenbeath
 * 21/04/2017 V1.3 added url encoding to username and password for when special characters are used, with thanks to pastygangster
 * 20/03/2017 V1.2 updated to refresh the garadget devices every 1 minute which is the minimum schedule allowed in ST
 * 13/02/2016 V1.1 added the correct call for API url for EU/US servers, left to do: cleanup child devices when removed from setup
 * 12/02/2016 V1.0 initial release, left to do: cleanup child devices when removed from setup
 */
 import java.net.URLEncoder
 import java.text.DecimalFormat
 import groovy.json.JsonSlurper
 import groovy.json.JsonOutput

private apiUrl() 			{ "https://api.particle.io" }
private getVendorName() 	{ "Garadget" }
private getVendorTokenPath(){ "https://api.particle.io/oauth/token" }
private getVendorIcon()		{ "https://dl.dropboxusercontent.com/s/lkrub180btbltm8/garadget_128.png" }
private getClientId() 		{ appSettings.clientId }
private getClientSecret() 	{ appSettings.clientSecret }
private getServerUrl() 		{ if(!appSettings.serverUrl){return getApiServerUrl()} }


 // Automatically generated. Make future change here.
definition(
    name: "Garadget (Connect)",
    namespace: "fuzzysb",
    author: "Stuart Buchanan",
    description: "Garadget Integration",
    category: "SmartThings Labs",
	iconUrl:   "https://dl.dropboxusercontent.com/s/lkrub180btbltm8/garadget_128.png",
	iconX2Url: "https://dl.dropboxusercontent.com/s/w8tvaedewwq56kr/garadget_256.png",
	iconX3Url: "https://dl.dropboxusercontent.com/s/5hiec37e0y5py06/garadget_512.png",
    oauth: true,
    singleInstance: true
) {
    appSetting "serverUrl"
}

preferences {
	page(name: "startPage", title: "Garadget Integration", content: "startPage", install: false)
	page(name: "Credentials", title: "Fetch OAuth2 Credentials", content: "authPage", install: false)
  page(name: "mainPage", title: "Garadget Integration", content: "mainPage")
  page(name: "completePage", title: "${getVendorName()} is now connected to SmartThings!", content: "completePage")
	page(name: "listDevices", title: "Garadget Devices", content: "listDevices", install: false)
  page(name: "badCredentials", title: "Invalid Credentials", content: "badAuthPage", install: false)
}

mappings {
	path("/receivedToken"){action: [POST: "receivedToken", GET: "receivedToken"]}
}

def startPage() {
    if (state.garadgetAccessToken) { return mainPage() }
    else { return authPage() }
}

def mainPage(){

	def result = [success:false]


	if (!state.garadgetAccessToken) {
    	createAccessToken()
       	log.debug "About to create Smarthings Garadget access token."
        getToken(garadgetUsername, garadgetPassword)
    }
    if (state.garadgetAccessToken){
    	result.success = true
    }


    if(result.success == true) {
           		return completePage()
	} else {
    			return badAuthPage()
	}
}



def completePage(){
	def description = "Tap 'Next' to proceed"
			return dynamicPage(name: "completePage", title: "Credentials Accepted!", nextPage: listDevices , uninstall: true, install:false) {
				section { href url: buildRedirectUrl("receivedToken"), style:"embedded", required:false, title:"${getVendorName()} is now connected to SmartThings!", description:description }
			}
}

def badAuthPage(){
	log.debug "In badAuthPage"
    log.error "login result false"
       		return dynamicPage(name: "badCredentials", title: "Garadget", install:false, uninstall:true, nextPage: Credentials) {
				section("") {
					paragraph "Please check your username and password"
           		}
            }
}

def authPage() {
	log.debug "In authPage"
	if(canInstallLabs()) {
		def description = null


			log.debug "Prompting for Auth Details."

			description = "Tap to enter Credentials."

			return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage: mainPage, uninstall: false , install:false) {
			section("Generate Username and Password") {
				input "garadgetUsername", "text", title: "Your Garadget Username", required: true
				input "garadgetPassword", "password", title: "Your Garadget Password", required: true
				}
			}
	}
	else
	{
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""


		return dynamicPage(name:"Credentials", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section {
				paragraph "$upgradeNeeded"
			}
		}

	}
}

def createChildDevice(deviceFile, dni, name, label) {
	log.debug "In createChildDevice"
    try{
		def childDevice = addChildDevice("fuzzysb", deviceFile, dni, null, [name: name, label: label, completedSetup: true])
	} catch (e) {
		log.error "Error creating device: ${e}"
	}
}

def listDevices() {
	log.debug "In listDevices"

	def options = getDeviceList()

	dynamicPage(name: "listDevices", title: "Choose devices", install: true) {
		section("Devices") {
			input "devices", "enum", title: "Select Device(s)", required: false, multiple: true, options: options, submitOnChange: true
		}
	}
}

def buildRedirectUrl(endPoint) {
	log.debug "In buildRedirectUrl"
	log.debug("returning: " + getServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/${endPoint}")
	return getServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/${endPoint}"
}

def receivedToken() {
	log.debug "In receivedToken"

	def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${getVendorName()} Connection</title>
        <style type="text/css">
            * { box-sizing: border-box; }
            @font-face {
                font-family: 'Swiss 721 W01 Thin';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            @font-face {
                font-family: 'Swiss 721 W01 Light';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            .container {
                width: 560px;
                padding: 40px;
                /*background: #eee;*/
                text-align: center;
            }
            img {
                vertical-align: middle;
            }
            img:nth-child(2) {
                margin: 0 30px;
            }
            p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                margin-bottom: 0;
            }
        /*
            p:last-child {
                margin-top: 0px;
            }
        */
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
        </head>
        <body>
            <div class="container">
                <img src=""" + getVendorIcon() + """ alt="Vendor icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>Tap 'Done' to continue to Devices.</p>
			</div>
        </body>
        </html>
        """
	render contentType: 'text/html', data: html
}

def getDeviceList() {
	def garadgetDevices = []

    httpGet( apiUrl() + "/v1/devices?access_token=${state.garadgetAccessToken}"){ resp ->
    	def restDevices = resp.data
		restDevices.each { garadget ->
        	if (garadget.connected == true)
                garadgetDevices << ["${garadget.id}|${garadget.name}":"${garadget.name}"]
            }

	}
	return garadgetDevices.sort()

}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	initialize()
}

def uninstalled() {
  log.debug "Uninstalling Garadget (Connect)"
  deleteToken()
  removeChildDevices(getChildDevices())
  log.debug "Garadget (Connect) Uninstalled"

}

def initialize() {
	log.debug "Initialized with settings: ${settings}"
	// Pull the latest device info into state
	getDeviceList();
    def children = getChildDevices()
    if(settings.devices) {
    	settings.devices.each { device ->
    	def item = device.tokenize('|')
        def deviceId = item[0]
        def deviceName = item[1]
        def existingDevices = children.find{ d -> d.deviceNetworkId.contains(deviceId) }
    		if(!existingDevices) {
				try {
					createChildDevice("Garadget", deviceId + ":" + state.garadgetAccessToken, "${deviceName}", deviceName)
				} catch (Exception e) {
					log.error "Error creating device: ${e}"
				}
    		}
		}
    }


	// Do the initial poll
	poll()
	// Schedule it to run every 1 minutes
	runEvery1Minute("poll")
}

def getToken(garadgetUsername, garadgetPassword){
	log.debug "Executing 'sendCommand.setState'"
    def encodedUsername = URLEncoder.encode(garadgetUsername, "UTF-8")
 	def encodedPassword = URLEncoder.encode(garadgetPassword, "UTF-8")
    def body = ("grant_type=password&username=${encodedUsername}&password=${encodedPassword}&expires_in=0")
	sendCommand("createToken","particle","particle", body)
}

private sendCommand(method, user, pass, command) {
    def userpassascii = "${user}:${pass}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    def headers = [:]
    headers.put("Authorization", userpass)
	def methods = [
			'createToken': [
        				uri: getVendorTokenPath(),
            			requestContentType: "application/x-www-form-urlencoded",
                        headers: headers,
                	    body: command
                    	],
            'deleteToken': [
        				uri: apiUrl() + "/v1/access_tokens/${state.garadgetAccessToken}",
            			requestContentType: "application/x-www-form-urlencoded",
                        headers: headers,
                    	]
                   ]
	def request = methods.getAt(method)
	log.debug "Http Params ("+request+")"

    try{
        if (method == "createToken"){
        	log.debug "Executing createToken 'sendCommand'"
            httpPost(request) { resp ->
                parseResponse(resp)
            }
        }else if (method == "deleteToken"){
        	log.debug "Executing deleteToken 'sendCommand'"
            httpDelete(request) { resp ->
                parseResponse(resp)
            }
        }else{
        log.debug "Executing default HttpGet 'sendCommand'"
            httpGet(request) { resp ->
                parseResponse(resp)
        }
        }
    } catch(Exception e){
        log.debug("___exception: " + e)
    }
}


private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        state.garadgetAccessToken = resp.data.access_token
        log.debug("Access Token: "+ state.garadgetAccessToken)
		state.garadgetRefreshToken = resp.data.refresh_token
        log.debug("Refresh Token: "+ state.garadgetRefreshToken)
		state.garadgetTokenExpires = resp.data.expires_in
        log.debug("Token Expires: "+ state.garadgetTokenExpires)
        log.debug "Created new Garadget token"
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

def poll() {
	log.debug "Executing - Service Manager - poll() - "
	getDeviceList();
	getAllChildDevices().each {
        it.statusCommand()
	}
}

private Boolean canInstallLabs() {
	return hasAllHubsOver("000.011.00603")
}

private List getRealHubFirmwareVersions() {
	return location.hubs*.firmwareVersionString.findAll { it }
}

private Boolean hasAllHubsOver(String desiredFirmware) {
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

void deleteToken() {
try{
    sendCommand("deleteToken","${garadgetUsername}","${garadgetPassword}",[])
	log.debug "Deleted the existing Garadget Access Token"
 } catch (e) {log.debug "Couldn't delete Garadget Token, There was an error (${e}), moving on"}
}

private removeChildDevices(delete) {
	try {
    	delete.each {
        	deleteChildDevice(it.deviceNetworkId)
            log.info "Successfully Removed Child Device: ${it.displayName} (${it.deviceNetworkId})"
    		}
   		}
    catch (e) { log.error "There was an error (${e}) when trying to delete the child device" }
}