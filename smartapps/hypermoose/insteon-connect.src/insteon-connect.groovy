/**
 *  Copyright 2015 SmartThings
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
 *	Insteon Service Manager
 *
 *	Author: gary
 *	Date: 2016-06-10
 * 
 *  Updated by kuestess
 *  Date: 05/19/2017
 */
definition(
		name: "Insteon (Connect)",
		namespace: "hypermoose",
		author: "hypermoose",
		description: "Connect your Insteon Hub to SmartThings.",
		category: "",
		iconUrl: "https://hypermoose-icons.s3.amazonaws.com/insteon.png",
		iconX2Url: "https://hypermoose-icons.s3.amazonaws.com/insteon@2x.png",
		singleInstance: true
) {
	appSetting "clientId"
    appSetting "clientSecret"
}

preferences {
	page(name: "auth", title: "insteon", nextPage:"", content:"authPage", uninstall: true, install:true)
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}

def authPage() {
	log.debug "authPage()"

	if(!atomicState.accessToken) { //this is to access token for 3rd party to make a call to connect app
		atomicState.accessToken = createAccessToken()
	}

	def description
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(atomicState.authToken) {
		description = "You are connected."
		uninstallAllowed = true
		oauthTokenProvided = true
	} else {
		description = "Click to enter Insteon Credentials"
	}

	def redirectUrl = buildRedirectUrl
	log.debug "RedirectUrl = ${redirectUrl}"
    
	// get rid of next button until the user is actually auth'd
	if (!oauthTokenProvided) {
		return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap below to log in to the insteon service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title:"insteon", description:description
			}
		}
	} else {
        
		def devices = getInsteonDevices()
		log.debug "Device list: $devices"
		return dynamicPage(name: "auth", title: "Devices", uninstall: true) {
			section("Switches"){
				input(name: "switches", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:devices])
			}
            section("IOLinc"){
				input(name: "iolinc", title:"", type: "enum", required:false, multiple:true, description: "Tap to choose", metadata:[values:devices])
			}
		}
	}
}

def oauthInitUrl() {
	log.debug "oauthInitUrl with callback: ${callbackUrl}"

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			client_id: smartThingsClientId,
			state: atomicState.oauthInitState,
			redirect_uri: callbackUrl,
	]

	redirect(location: "${apiEndpoint}/api/v2/oauth2/auth?${toQueryString(oauthParams)}")
}

def callback() {
	log.debug "callback called with code ${params.code}"
    
    def code = params.code
	def oauthState = params.state

	if (oauthState == atomicState.oauthInitState) {

		def tokenParams = [
				grant_type: "authorization_code",
				code      : code,
				client_id : smartThingsClientId,
                client_secret: smartThingsClientSecret,
				redirect_uri: callbackUrl
		]

		def tokenUrl = "${apiEndpoint}/api/v2/oauth2/token"

		try {
            httpPost(uri: tokenUrl,
            		body: tokenParams
            ) { resp ->
                atomicState.refreshToken = resp.data.refresh_token
                atomicState.authToken = resp.data.access_token
                log.debug "atomicState.refreshToken: ${atomicState.refreshToken}"
                log.debug "atomicState.authToken: ${atomicState.authToken}"
            }
        } catch (e) {
        	log.error "httpPost failed with exception: ${e}"
            fail()
        }

		if (atomicState.authToken) {
			success()
		} else {
			fail()
		}

	} else {
		log.error "callback() failed oauthState != atomicState.oauthInitState"
	}
}

def success() {
	def message = """
    <p>Your Insteon Account is now connected to SmartThings!</p>
    <p>Click 'Done' to finish setup.</p>
    """
	connectionStatus(message)
}

def fail() {
	def message = """
        <p>The connection could not be established!</p>
        <p>Click 'Done' to return to the menu.</p>
    """
	connectionStatus(message)
}

def connectionStatus(message, redirectUrl = null) {
	def redirectHtml = ""
	if (redirectUrl) {
		redirectHtml = """
			<meta http-equiv="refresh" content="3; url=${redirectUrl}" />
		"""
	}

	def html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=640">
<title>Insteon & SmartThings connection</title>
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
                width: 90%;
                padding: 4%;
                /*background: #eee;*/
                text-align: center;
        }
        img {
                vertical-align: middle;
        }
        p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                padding: 0 40px;
                margin-bottom: 0;
        }
        span {
                font-family: 'Swiss 721 W01 Light';
        }
</style>
</head>
<body>
        <div class="container">
                <img src="https://hypermoose-icons.s3.amazonaws.com/insteon%402x.png" alt="insteon icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                ${message}
        </div>
</body>
</html>
"""

	render contentType: 'text/html', data: html
}

def getInsteonDevices() {
	log.debug "getting device list"

	def deviceListParams = [
			uri: apiEndpoint,
			path: "/api/v2/devices",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}", "Authentication": "APIKey ${smartThingsClientId}"],
            query: ["properties": "all"]
	]

	def devices = [:]
	try {
		httpGet(deviceListParams) { resp ->

			if (resp.status == 200) {
           
				resp.data.DeviceList.each { device ->
                	def devCat = device.DevCat
                    
                    // Pick switches
                    if (devCat == 1 || devCat == 2) {
                        def value = "${device.DeviceName}"
                        def key = "insteon_switch." + "${device.DeviceID}"
                        devices["${key}"] = value
                    }
                    // Pick IOLincs
                    //if (devCat == 7) {
                    //    def value = "${device.DeviceName}"
                    //    def key = "insteon_iolinc." + "${device.DeviceID}"
                    //    devices["${key}"] = value
                    //}
				}

			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
        log.trace "Exception getting devices: " + e.response.data
        if (e.response.data.code == 4014 || e.response.data.code == 4012) {
            atomicState.action = "getInsteonDevices"
            def retry = true
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
    }
                
    atomicState.devices = devices
	return devices
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

	log.debug "initialize"

	def devices = settings.switches.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getChildName(), dni, null, ["label":"${atomicState.devices[dni]}" ?: "Insteon Switch"])
			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}
    
	log.debug "created ${devices.size()} devices."

	def delete  // Delete any that are no longer in settings
	if (!devices) {
		log.debug "delete all switches"
		delete = getAllChildDevices() //inherits from SmartApp (data-management)
	} else { //delete only switches
		log.debug "delete individual switches"
        
		delete = getChildDevices().findAll { !settings.switches.contains(it.deviceNetworkId) }
	}
	log.warn "delete: ${delete}, deleting ${delete.size()} switches"
	delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

	//send activity feeds to tell that device is connected
	def notificationMessage = "is connected to SmartThings"
	sendActivityFeeds(notificationMessage)
	atomicState.timeSendPush = null
	atomicState.reAttempt = 0
    
    // reset the poll data and deviceStatus
    atomicState.pollData = [:]
    atomicState.deviceStatus = [:]

	pollHandler() //first time polling

	//automatically update devices status every 5 mins
	runEvery5Minutes("poll")
}

def pollHandler() {
	log.debug "pollHandler()"
	pollChildren(null) // Hit the Insteon API for update on all switches
}

def pollChildren(child = null) {
	def result = false;
    def hasPending = false;
    
  	def pollData = atomicState.pollData
    def deviceStatus = atomicState.deviceStatus
   
    settings.switches.collect { dni ->
    	def deviceId = dni.split(/\./).last()        
           
        try {
            //if (pollData[dni] == null) {
            
            	log.debug "polling child: $deviceId"
            
                def cmdParams = [
                    uri: apiEndpoint,
                    path: "/api/v2/commands",
                    headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}", "Authentication": "APIKey ${smartThingsClientId}"],
                    body: '{ "command": "get_status", "device_id": ' + "${deviceId}" + ' }'
                ]
                httpPost(cmdParams) { resp ->
                    if(resp.status == 200 || resp.status == 202) {
                        if (resp.data.status == "pending") {
                            log.debug "command still pending for ${dni}"
                            pollData[dni] = resp.data.link
                            hasPending = true
                        } else {
                        	log.error "Unexpected result: ${resp.data}"
                        }
                    }
                }
        	//}
        } catch (groovyx.net.http.HttpResponseException e) {
            log.trace "Exception Sending Json: " + e.response.data
            debugEvent ("sent Json & got http status ${e.statusCode}")
            if (e.response.data.code == 4014 || e.response.data.code == 4012) {
                atomicState.action = "pollChildren"
                log.debug "Refreshing your auth_token!"
                refreshAuthToken()
                return true
            }
            else {
                debugEvent("Authentication error, invalid authentication method, lack of credentials, etc.")
                log.error "Authentication error, invalid authentication method, lack of credentials, etc."
            }
        }
    }
    
    atomicState.pollData = pollData
    atomicState.deviceStatus = deviceStatus
    log.debug "pollChildren updated pollData = ${pollData}"
    log.debug "pollChildren updated deviceStatus = ${deviceStatus}"
    log.debug "Has pending: ${hasPending}"
    
    if (hasPending) {
   		log.debug "Scheduling checkPendingRequests"
    	runIn(60, "checkPendingRequests")
    }
    
	return true
}

def checkPendingRequests() {
	def stillPending = false;
    
    log.debug "checkPendingRequests called"
    
  	def pollData = atomicState.pollData
    def deviceStatus = atomicState.deviceStatus
   
    settings.switches.collect { dni ->
    	def deviceId = dni.split(/\./).last()        
           
        try {
            if (pollData[dni] != null) {
            
                log.debug "Checking pending request for ${dni}"         
             
            	def getParams = [
                    uri: apiEndpoint,
                    path: pollData[dni],
                    headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}", "Authentication": "APIKey ${smartThingsClientId}"],
                ]
                httpGet(getParams) { resp ->
                    log.debug "Response status: ${resp.status}"
                    if(resp.status == 200) {

                        log.debug "updated ${resp.data}"

                        if (resp.data.status == "succeeded") {
                            pollData[dni] = null  // Clear the poll data
                            deviceStatus[dni] = resp.data.response  // Save the response
                        } else if (resp.data.status != "pending") {
                        	log.error "Unexpected result: ${resp.data}"
                            pollData[dni] = null  // Clear the poll data
                        } else {
                        	stillPending = true;
                        }
                    }
                }
            }
        } catch (groovyx.net.http.HttpResponseException e) {
            log.trace "Exception Sending Json: " + e.response.data
            debugEvent ("sent Json & got http status ${e.statusCode}")
            if (e.response.data.code == 4014 || e.response.data.code == 4012) {
                atomicState.action = "checkPendingRequests"
                log.debug "Refreshing your auth_token!"
                refreshAuthToken()
                return true
            }
            else {
                debugEvent("Authentication error, invalid authentication method, lack of credentials, etc.")
                log.error "Authentication error, invalid authentication method, lack of credentials, etc."
            }
        }
    }
    
    atomicState.pollData = pollData
    atomicState.deviceStatus = deviceStatus
    log.debug "checkPendingRequests updated pollData = ${pollData}"
    log.debug "checkPendingRequests updated deviceStatus = ${deviceStatus}"
    
    if (stillPending) {
    	log.debug "Rescheduling checkPendingRequests"
        runIn(60, checkPendingRequests);
    }
    
    // Generate events for changes
    devices.each { child ->
        if(atomicState.deviceStatus[child.device.deviceNetworkId] != null) {
            def tData = atomicState.deviceStatus[child.device.deviceNetworkId]
            log.info "pollChild(child)>> data for ${child.device.deviceNetworkId} : ${tData}"
            child.generateEvent(tData) //parse received message from parent
       }
    }
    
	return true
}


// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild(){

	def devices = getChildDevices()

	if (pollChildren()){
		devices.each { child ->
            if(atomicState.deviceStatus[child.device.deviceNetworkId] != null) {
                def tData = atomicState.deviceStatus[child.device.deviceNetworkId]
                log.info "pollChild(child)>> data for ${child.device.deviceNetworkId} : ${tData}"
                child.generateEvent(tData) //parse received message from parent
           }
		}
	} else {
		log.info "ERROR: pollChildren()"
		return null
	}
}

void poll() {
	log.debug "poll() called"
	pollChild()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private refreshAuthToken() {
	log.debug "refreshing auth token"

	if(!atomicState.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {

		def refreshParams = [
				method: 'POST',
				uri   : apiEndpoint,
				path  : "/api/v2/oauth2/token",
				body : [grant_type: 'refresh_token', refresh_token: "${atomicState.refreshToken}", client_id: smartThingsClientId, client_secret: smartThingsClientSecret],
		]

		log.debug refreshParams

		def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the Insteon (Connect) SmartApp and re-enter your account login credentials."
		//changed to httpPost
		try {
			def jsonMap
			httpPost(refreshParams) { resp ->

				if(resp.status == 200) {
					log.debug "Token refreshed...calling saved RestAction now!"

					debugEvent("Token refreshed ... calling saved RestAction now!")

					log.debug resp

					jsonMap = resp.data

					if(resp.data) {

						log.debug resp.data
						debugEvent("Response = ${resp.data}")

						atomicState.refreshToken = resp?.data?.refresh_token
						atomicState.authToken = resp?.data?.access_token

						debugEvent("Refresh Token = ${atomicState.refreshToken}")
						debugEvent("OAUTH Token = ${atomicState.authToken}")

						if(atomicState.action && atomicState.action != "") {
							log.debug "Executing next action: ${atomicState.action}"

							"${atomicState.action}"()

							atomicState.action = ""
						}

					}
					atomicState.action = ""
				}
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "refreshAuthToken() >> Error: e.statusCode ${e.statusCode}"
			def reAttemptPeriod = 300 // in sec
			if (e.statusCode != 401) { // this issue might comes from exceed 20sec app execution, connectivity issue etc.
				runIn(reAttemptPeriod, "refreshAuthToken")
			} else if (e.statusCode == 401) { // unauthorized
				atomicState.reAttempt = atomicState.reAttempt + 1
				log.warn "reAttempt refreshAuthToken to try = ${atomicState.reAttempt}"
				if (atomicState.reAttempt <= 3) {
					runIn(reAttemptPeriod, "refreshAuthToken")
				} else {
					sendPushAndFeeds(notificationMessage)
					atomicState.reAttempt = 0
				}
			}
		}
	}
}

def switchOn(child, deviceNetworkId) {

	def deviceId = deviceNetworkId.split(/\./).last()
    
    def deviceStatus = atomicState.deviceStatus;

	def jsonRequestBody = '{ "command": "on", "device_id": ' + "${deviceId}" + ' }'
	def result = sendJson(jsonRequestBody)
    
    if (result) {
    	deviceStatus[deviceNetworkId] = '{level: 100}'
        atomicState.deviceStatus = deviceStatus
    }
	return result
}

def switchOff(child, deviceNetworkId) {

	def deviceId = deviceNetworkId.split(/\./).last()
    
    def deviceStatus = atomicState.deviceStatus;

	def jsonRequestBody = '{ "command": "off", "device_id": ' + "${deviceId}" + ' }'
	def result = sendJson(jsonRequestBody)
    if (result) {
    	deviceStatus[deviceNetworkId] = '{level: 0}'
        atomicState.deviceStatus = deviceStatus
    }
	return result
}

def switchLevel(child, deviceNetworkId, level) {
	log.debug "Calling switchLevel"
	def deviceId = deviceNetworkId.split(/\./).last()
    
    def deviceStatus = atomicState.deviceStatus;
	log.debug "FirstDeviceStatus ${deviceStatus}"
	def jsonRequestBody = '{ "command": "on", "device_id": ' + "${deviceId}" + ', "level":' + "${level}" + ' }'
	log.debug "JSONRequestBody ${jsonRequestBody}"
    def result = sendJson(jsonRequestBody)
    //log.debug "DimResult ${result}"
    
    if (result) {
        deviceStatus[deviceNetworkId] = '{level: ' + "${level}" + '}'
        atomicState.deviceStatus = deviceStatus
        log.debug "DeviceStatus ${atomicState.deviceStatus}"
    }
	return result
}

def sendJson(child = null, String jsonBody) {
	log.debug "JSONbody: ${jsonBody}"
	def returnStatus = "not sent"
	def cmdParams = [
			uri: apiEndpoint,
			path: "/api/v2/commands",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}", "Authentication": "APIKey ${smartThingsClientId}"],
			body: jsonBody
	]

	try{
		httpPost(cmdParams) { resp ->
        
			if(resp.status == 200) {

				log.debug "updated ${resp.data}"
				returnStatus = resp.data.status
				if (resp.data.status != "failed")
					log.debug "Successful call to insteaon API."
				else {
					log.debug "Error return code = ${resp.data.status.code}"
					debugEvent("Error return code = ${resp.data.status.code}")
				}
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
        log.trace "Exception Sending Json: " + e.response.data.status
        debugEvent ("sent Json & got http status ${e.statusCode} - ${e.response.data.status.code}")
         if (e.response.data.code == 4014 || e.response.data.code == 4012) {
            atomicState.action = "pollChildren"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
        else {
            debugEvent("Authentication error, invalid authentication method, lack of credentials, etc.")
            log.error "Authentication error, invalid authentication method, lack of credentials, etc."
        }
    }

	if (returnStatus != "failed")
		return true
	else
		return false
}

def getChildName()           { "Insteon Switch" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getShardUrl()            { return getApiServerUrl() }
def getCallbackUrl()        { "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()   { "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }
def getApiEndpoint()        { "https://connect.insteon.com" }
def getSmartThingsClientId() { appSettings.clientId }
def getSmartThingsClientSecret() { appSettings.clientSecret }

def debugEvent(message, displayEvent = false) {

	def results = [
			name: "appdebug",
			descriptionText: message,
			displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}

//send both push notification and mobile activity feeds
def sendPushAndFeeds(notificationMessage){
	log.warn "sendPushAndFeeds >> notificationMessage: ${notificationMessage}"
	log.warn "sendPushAndFeeds >> atomicState.timeSendPush: ${atomicState.timeSendPush}"
	if (atomicState.timeSendPush){
		if (now() - atomicState.timeSendPush > 86400000){ // notification is sent to remind user once a day
			sendPush("Your Insteon switch " + notificationMessage)
			sendActivityFeeds(notificationMessage)
			atomicState.timeSendPush = now()
		}
	} else {
		sendPush("Your Insteon switch " + notificationMessage)
		sendActivityFeeds(notificationMessage)
		atomicState.timeSendPush = now()
	}
	atomicState.authToken = null
}

def sendActivityFeeds(notificationMessage) {
	def devices = getChildDevices()
	devices.each { child ->
		child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
	}
}