/**
 *  ja_Ubi (Connect)
 *
 *  Copyright 2015 Jody Albritton
 *  Based on code by Patrick Stuart 
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
 
 
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*


definition(
    name: "Ubi (Connect)",
    namespace: "jodyalbritton",
    author: "Jody Albritton",
    description: "Connect your Ubis to SmartThings as sensor and speech devices.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"){
	
    
    /*
    	These settings are required to connect to The Ubi Portal. For the purposes of this app
        you will need to obtain a client ID and Secret from https://portal.theubi.com
        
        
        After you have obtained the keys and installed the Service manager(this file) and the device type, 
        you need to access the settings on the service manager to install the keys. 
       
        
        stClientID can be found in your settings. 
        
        Once installed you should be able to authenticate to the ubi portal and add your ubis as sensor and speech devices. 

		ST Staff evaluating this app: Please contact jody.albritton@gmail.com and I will give you
        my credentials to test the app. For publication I am not certain if the app needs my credentials 
        or those that would be obtained by ST. 
        
        
    */
    appSetting "clientId"
	appSetting "clientSecret"
    appSetting "stClientId"
    
}


preferences {
	page(name: "auth", title: "ubi", nextPage:"deviceList", content:"authPage", uninstall: true)
	page(name: "deviceList", title: "ubi", content:"ubiDeviceList", install:true)
}

mappings {
	path("/swapToken") {
		action: [
			GET: "swapToken"
		]
	}
}

def authPage()
{
	log.debug "Version 0.0.10"
	log.debug "authPage()"

	if(!atomicState.accessToken)
	{
		log.debug "about to create access token"
		createAccessToken()
		atomicState.accessToken = state.accessToken
	}


	def description = "Required"
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(atomicState.authToken)
	{
		if(true)
		{
			description = "You are connected."
			uninstallAllowed = true
			oauthTokenProvided = true
		}
		else
		{
			description = "Required" 
			oauthTokenProvided = false
		}
	}

	def redirectUrl = oauthInitUrl()

	log.debug "RedirectUrl = ${redirectUrl}"

	// get rid of next button until the user is actually auth'd
	log.debug "_______AUTH______ ${atomicState.authToken}"
    log.debug oauthTokenProvided
    
	if (!oauthTokenProvided) {

		return dynamicPage(name: "auth", title: "Login", nextPage:null, uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap below to log in to the ubi service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title:"ubi", description:description
			}
		}

	} else {

		return dynamicPage(name: "auth", title: "Log In", nextPage:"deviceList", uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap Next to continue to setup your ubi."
				href url:redirectUrl, style:"embedded", state:"complete", title:"ubi", description:description
			}
		}

	}

}

def ubiDeviceList()
{
	log.debug "ubiDeviceList()"

	def ubis = getUbis()

	log.debug "device list: $ubis"

	def p = dynamicPage(name: "deviceList", title: "Select Your Ubis", uninstall: true) {
		section(""){
			paragraph "Tap below to see the list of Ubis available in your Ubi account and select the ones you want to connect to SmartThings."
			input(name: "ubis", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", options:ubis.collectEntries{[it.id, it.location]})
		}
	}

	log.debug "list p: $p"
	return p
}

def getUbis()
{
	log.debug "getting device list"

	def requestBody = '{"selection":{"selectionType":"registered","selectionMatch":"","includeRuntime":true}}'

	def deviceListParams = [
    uri: "https://portal.theubi.com",
    path: "/v2/ubi/list",
    headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"]
    ]

	log.debug "_______AUTH______ ${atomicState.authToken}"
	log.debug "device list params: $deviceListParams"
    
    

	def ubis = [:]
   
    	httpGet(deviceListParams) { resp ->
        log.debug "response:"
        log.debug resp

		if(resp.status == 200)
		{
        
		   ubis = resp.data.result.data
           state.ubis = ubis
           
	
           
		}
		else
		{
			log.debug "http status: ${resp.status}"

			//refresh the auth token
			if (resp.status == 500 && resp.data.status.code == 14)
			{
				log.debug "Storing the failed action to try later"
				data.action = "getUbis"
				log.debug "Refreshing your auth_token!"
				refreshAuthToken()
			}
			else
			{
				log.error "Authentication error, invalid authentication method, lack of credentials, etc."
			}
		}
	}

	log.debug "ubis: ${ubis}"

	return ubis
}


//get the latest data for the device

def pollChild(deviceId) {

	log.debug "polling child"
    
   
				
    def pollParams= [
    uri: "https://portal.theubi.com",
    path: "/v2/ubi/${deviceId}/sensors",
    headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"]
    ]

    httpGet(pollParams) { resp ->
        if(resp.status == 200) {

            def result = resp.data.result.data
            log.debug "Result: ${result}"
            return result
            log.debug "Token: ${atomicState.authToken}"




       }

    }
  
}


def speakChild(deviceId, message) {

	log.debug "pushing speech"
    
   
				
    def speakParams= [
    uri: "https://portal.theubi.com",
    path: "/v2/ubi/${deviceId}/speak",
    headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
    query: ["phrase":"${message}","conversation": false]
    ]

    httpGet(speakParams) { resp ->
        if(resp.status ==  200) {

            def result = resp.data.result.data
            log.debug "Result: ${result}"
           




       }

    }
  
}



def installed() {
 
 log.debug "In installed() method."
 
 settings.ubis.each{deviceId->
    	def device = state.ubis.find{it.id==deviceId}
        log.debug "Device: ${device}"
        
        if (device) {
        	def childDevice = addChildDevice("jodyalbritton", "Ubi", device.id,null,[name:device.location, completedSetup: true])
            	if (childDevice)
                {
                	childDevice.save()
                }
                
                
            
        }
    }
}
	

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
  
}

def initialize() {
		
	  log.debug "Settings: ${settings} "
      settings.ubis.each {deviceId ->
       
        try {
            def existingDevice = getChildDevice(deviceId)
            if(!existingDevice) {
                def device = state.ubis.find{it.id==deviceId}
                def childDevice = addChildDevice("jodyalbritton", "Ubi", device.id,null,[name:device.location, completedSetup: true])
            }
        } catch (e) {
            log.error "Error creating device: ${e}"
        }
    }
   
	
}


def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}


def oauthInitUrl()
{
	log.debug "oauthInitUrl"
    
    def stcid = getSmartThingsClientId();

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
		response_type: "code",
		scope: "",
		client_id: appSettings.clientId,
        client_secret: appSettings.clientSecret,
		state: atomicState.oauthInitState,
		redirect_uri: buildRedirectUrl()
	]
	log.debug oauthParams
	return "https://portal.theubi.com/oauth/authorize?" + toQueryString(oauthParams)
}

def buildRedirectUrl()
{
	log.debug "buildRedirectUrl ${serverUrl}"
	return serverUrl + "/api/token/${atomicState.accessToken}/smartapps/installations/${app.id}/swapToken"
}

private refreshAuthToken() {
	log.debug "refreshing auth token"
	//debugEvent("refreshing OAUTH token", true)

	def stcid = getSmartThingsClientId()

	def refreshParams = [
		method: 'POST',
		uri: "https://portal.theubi.com",
		path: "/oauth/token",
		query: [grant_type:'refresh_token', code:"${atomicState.refreshToken}", client_id:stcid],

		//data?.refreshToken
	]

	log.debug "UBI: refreshParams= $refreshParams"

	//changed to httpPost
	try{
		def jsonMap
		httpPost(refreshParams) { resp ->

			if(resp.status == 200)
			{
				log.debug "Token refreshed...calling saved RestAction now!"

				//debugEvent("Token refreshed ... calling saved RestAction now!", true)

				log.debug resp

				jsonMap = resp.data

				if (resp.data) {

					log.debug resp.data
					//debugEvent ("Response = ${resp.data}", true)

					atomicState.refreshToken = resp?.data?.refresh_token
					atomicState.authToken = resp?.data?.access_token

					//debugEvent ("Refresh Token = ${atomicState.refreshToken}", true)
					//debugEvent ("OAUTH Token = ${atomicState.authToken}", true)

					if (data?.action && data?.action != "") {
						log.debug data.action

						"{data.action}"()

						//remove saved action
						data.action = ""
					}

				}
				data.action = ""
			}
			else
			{
				log.debug "refresh failed ${resp.status} : ${resp.status.code}"
			}
		}
		log.debug "UBI: $jsonMap"
		atomicState.refreshToken = jsonMap.refresh_token
		atomicState.authToken = jsonMap.access_token
	}
	catch(Exception e)
	{
		log.debug "caught exception refreshing auth token: " + e
	}
    log.debug "_____AUTH_____ ${atomicState.authToken}"
}

def swapToken()
{
	log.debug "swapping token: $params"
    log.debug "_____AUTH_____ ${atomicState.authToken}"
	//debugEvent ("swapping token: $params", true)

	def code = params.code
	def oauthState = params.state

	// TODO: verify oauthState == atomicState.oauthInitState



	def stcid = getSmartThingsClientId()

	def tokenParams = [
		grant_type: "authorization_code",
		code: params.code,
		client_id: appSettings.clientId,
        client_secret: appSettings.clientSecret,
		redirect_uri: buildRedirectUrl()
	]
    

	def tokenUrl = "https://portal.theubi.com/oauth/token?" + toQueryString(tokenParams)

	log.debug "SCOTT: swapping token $params"

	def jsonMap
	httpPost(uri:tokenUrl) { resp ->
		jsonMap = resp.data
	}

	log.debug "SCOTT: swapped token for $jsonMap"
	//debugEvent ("swapped token for $jsonMap", true)

	atomicState.refreshToken = jsonMap.refresh_token
	atomicState.authToken = jsonMap.access_token
	log.debug atomicState
	def html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=640">
<title>Withings Connection</title>
<style type="text/css">
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
		padding: 0 40px;
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
		
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
		<p>Your ubi Account is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	</div>
</body>
</html>
"""

	render contentType: 'text/html', data: html
}



def toQueryString(Map m)
{
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def getServerUrl() { return "https://graph.api.smartthings.com" }
def getSmartThingsClientId() {appSettings.sTClientId }  
