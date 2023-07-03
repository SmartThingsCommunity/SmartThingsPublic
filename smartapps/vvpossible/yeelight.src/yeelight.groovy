/**
 *  Yeelight
 *
 *  Copyright 2017 WEI WEI
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
    name: "Yeelight",
    namespace: "vvpossible",
    author: "YEELIGHT",
    description: "Allows you to connect your Yeelight smart lights (Singapore only currently) with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/yeelight-images/yeelightlogo.png",
    iconX2Url: "https://s3.amazonaws.com/yeelight-images/yeelightlogo%402x.png",
    iconX3Url: "https://s3.amazonaws.com/yeelight-images/yeelightlogo%402x.png",
    singleInstance: true) {
    appSetting "AppSecret"
    appSetting "AppID"
}


preferences {
	page(name: "auth", title: "Yeelight", nextPage:"", content:"authPage", uninstall: true, install:true)
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}

def getChildName()           { return "Yeelight Smart LED" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getShardUrl()            { return getApiServerUrl() }
def getCallbackUrl()         { return "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()    { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }
def getSmartThingsClientId() { return appSettings.AppID }
def getApiEndpoint() { 
    return "https://${region}.openapp.io.mi.com/openapp/"
}

def authPage() {
	if(!atomicState.accessToken) { //this is to access token for 3rd party to make a call to connect app
		atomicState.accessToken = createAccessToken()
	}

	def description = "Connect Yeelight with SmartThings"
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(atomicState.authToken) {
		description = "You are connected."
		uninstallAllowed = true
		oauthTokenProvided = true
	} else {
		description = "Click to enter Yeelight Credentials"
	}

	def redirectUrl = buildRedirectUrl
    
    log.debug redirectUrl
       
	if (!oauthTokenProvided) {
		return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall:uninstallAllowed) {
			section() {
                paragraph "Tap below to select region first. Default: Singapore."
				input(name: "region", title:"Region", type: "enum", 
                	  required:true, multiple:false, defaultValue: "sg",
                      description: "Tap to choose", options:["sg":"Singapore", "us":"North America"])
                      
				paragraph "Tap below to log in to the Yeelight service."
				href url:redirectUrl, style:"embedded", required:true, title:"Yeelight", description:description
			}
		}
	} else {
		getYeelightDevices()      
        
        def options = lightsDiscovered() ?: []

		return dynamicPage(name: "auth", title: "Select Your Lights", uninstall: true) {
			section("") {
				input(name: "selectedLights", title:"Select Your Light", type: "enum", 
                	  required:true, multiple:true,
                      description: "Tap to choose", options:options)
                paragraph "Tap 'Done' after you have selected the desired devices."
			}
		}
	}
}

def oauthInitUrl() {
	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			scope: "1 3 6000",
			client_id: smartThingsClientId,
			state: atomicState.oauthInitState,
			redirect_uri: callbackUrl
	]

	redirect(location: "https://account.xiaomi.com/oauth2/authorize?${toQueryString(oauthParams)}")
}

def callback() {
	def code = params.code
	def oauthState = params.state

	if (oauthState == atomicState.oauthInitState) {   
    	def tokenParams = [
        	client_id: smartThingsClientId, grant_type: "authorization_code", 
            code: code, redirect_uri: callbackUrl, 
            client_secret : appSettings.AppSecret, 
            token_type: "bearer"
		]
        def tokenUrl = "https://account.xiaomi.com/oauth2/token?${toQueryString(tokenParams)}"    
        try {
			httpPost(uri: tokenUrl) { resp ->
                if (resp.status == 200) {
					atomicState.refreshToken = resp.data.refresh_token
					atomicState.authToken = resp.data.access_token
                }
			}
			if (atomicState.authToken) {
				success()
			} else {
				fail()
			}
        } catch (e) {
        	log.error "failed to get token ${e}"
            fail()
        }
	} else {
		log.error "callback() failed oauthState != atomicState.oauthInitState"
	}

}

def success() {
	def message = """
        <p>Your Yeelight Account is now connected to SmartThings!</p>
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
                <title>Yeelight & SmartThings connection</title>
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
                <img src="https://s3.amazonaws.com/yeelight-images/st-logo.png" alt="Yeelight icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                ${message}
            </div>
        </body>
    </html>
    """

	render contentType: 'text/html', data: html
}

def installed() {
    unschedule()
	unsubscribe()
	configLights()
    //sync online/offline status every minute
	runEvery1Minute(syncOnlineState)
}

def updated() {
    unschedule()
	configLights()
    //sync online/offline status every minute
	runEvery1Minute(syncOnlineState)
}

def uninstalled()
{
	unschedule()
    removeChildDevices(getChildDevices())
}

def get_dev(final it) { ["name" : it.name, "did" : it.did, "type" : it.model] }

def getYeelightDevices() {   
	atomicState.lights = []

	def deviceListParams = [
		uri: apiEndpoint,
		path: "user/device_list",
        contentType : "application/json",
        query: [clientId: smartThingsClientId, accessToken: atomicState.authToken]
	]

	try {
		httpGet(deviceListParams) { resp ->
			if (resp.status == 200) {
                atomicState.lights = resp.data.result.list.findResults { (it.did.contains('virtual') || it.did.contains('.')) ? null : get_dev(it)} //don't show Yeelight virtual devices
			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (e) {
        log.trace "Exception getting device list: $e"
    }
}

Map lightsDiscovered() {
	def lights = atomicState.lights
	def map = [:]
	lights.each {
		def value = it?.name
		def key = it?.did
		map["${key}"] = value
	}
	return map
}


def get_dev_status(final it) { ["did" : it.did, "online" : it.isOnline, "power": it.prop.power, "bright": it.prop.bright] }

def syncOnlineState() {  
    def devices = getChildDevices()
    def lights = []
    
	def deviceListParams = [
		uri: apiEndpoint,
		path: "user/device_list",
        contentType : "application/json",
        query: [clientId: smartThingsClientId, accessToken: atomicState.authToken]
	]

	try {
		httpGet(deviceListParams) { resp ->
			if (resp.status == 200) {
                lights = resp.data.result.list.findResults { (it.did.contains('virtual') || it.did.contains('.')) ? null : get_dev_status(it)}
			} else {
				log.debug "http status: ${resp.status}"
                return
			}
		}
	} catch (e) {
        log.trace "Exception getting device list: $e"
        return
    }  
    
    devices.each {
        def did = it.deviceNetworkId
        def dev = lights.find {(it.did) == did}
        if (dev) {
            it.update(dev)
        } else {
            it.update(["online": false]) //if user has deleted the device through Yeelight app, show offline
        }
   }
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def configLights() {
	def lights = atomicState.lights

    if (selectedLights instanceof String) {
        def did = selectedLights
    	def d = getChildDevice(did)
        if(!d) {
            def newLight = lights.find { (it.did) == did }
            try {
            	d = addChildDevice("com.yeelight.www", newLight.type, did, null, [name: "${newLight?.name}", label: "${newLight?.name}", completedSetup: true])
            } catch (e) {
            	log.debug "unsupported device type"
            }
        }
		// Delete any that are no longer in settings
		def delete = getChildDevices().findAll { (it.deviceNetworkId) != did }
        
		removeChildDevices(delete)
     } else {
		selectedLights.each { did ->
			//see if this is a selected light and install it if not already
			def d = getChildDevice(did)
        
			if(!d) {
			   def newLight = lights.find { (it.did) == did }
			   d = addChildDevice("com.yeelight.www", newLight.type, did, null, [name: "${newLight?.name}", label: "${newLight?.name}", completedSetup: true])
			} else {
				log.debug "We already added this light"
			}
		}

		// Delete any that are no longer in settings
		def delete = getChildDevices().findAll { !selectedLights?.contains(it.deviceNetworkId) }
		removeChildDevices(delete)
    }
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private refreshAuthToken() {
	if(!atomicState.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {
		def refreshParams = [
			grant_type: 'refresh_token', refresh_token: "${atomicState.refreshToken}", 
            client_id: smartThingsClientId, token_type: "bearer", 
            client_secret: appSettings.AppSecret, redirect_uri: callbackUrl
		]

        def tokenUrl = "https://account.xiaomi.com/oauth2/token?${toQueryString(refreshParams)}"    
        
		def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the Yeelight (Connect) SmartApp and re-enter your account login credentials."

		try {
			httpPost(uri: tokenUrl) { resp ->
				if (resp.status == 200) {
					atomicState.refreshToken = resp.data.refresh_token
					atomicState.authToken = resp.data.access_token
			    }
            }
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "refreshAuthToken() >> Error: e.statusCode ${e.statusCode}"
			def reAttemptPeriod = 300 // in sec
			if (e.statusCode != 401) { 
				runIn(reAttemptPeriod, "refreshAuthToken")
			} else if (e.statusCode == 401) { // unauthorized
				atomicState.reAttempt = atomicState.reAttempt + 1
				log.warn "reAttempt refreshAuthToken to try = ${atomicState.reAttempt}"
				if (atomicState.reAttempt <= 3) {
					runIn(reAttemptPeriod, "refreshAuthToken")
				} else {
					log.error "failed to get refresh token"
					atomicState.reAttempt = 0
				}
			}
		}
	}
}

///////// Cloud API Method //////////
def callAPI(did, method, params, ret_raw) {
	def ctrlParams = [
		uri: apiEndpoint,
		path: "device/rpc/${did}",
        contentType : "application/json",
        query: [clientId: smartThingsClientId, accessToken: atomicState.authToken, data: "{\"method\":\"${method}\", \"params\":${params}}"]
	]
    
	try {
		httpGet(ctrlParams) { resp ->
			if (resp.status == 200 && resp.data.code == 0) {
            	if (ret_raw)
                    return resp.data.result 
                else 
                    return 0
			} else {
                if (ret_raw)
                    return null
                else {
                    return -1
                }
			}
		}
	} catch (e) {
        log.trace "Exception getting device list: $e"
        if (e.response.data.status == 401) {
            atomicState.action = "callYeelightAPI"
            refreshAuthToken()
        }
    }
}

def setColor(did, param) {
    if (!param.bright) param.bright = 100
    return callAPI(did, "set_scene", "[\"color\", ${param.color}, ${param.bright}]", 0)
}

def setBright(did, param) {
    return callAPI(did, "set_scene", "[\"bright\", ${param.level}, 500]", 0)
}

def setCT(did, param) {
	if (!param.bright) param.bright = 100
    return callAPI(did, "set_scene", "[\"ct\", ${param.ct}, ${param.bright}]", 0) 
}

def setPower(did, param) {
    return callAPI(did, "set_power", "[\"${param.power}\"]", 0)
}

def getProp(did, param) {
    def resp = callAPI(did, "get_prop", "[\"power\", \"bright\", \"ct\"]", 1)
    if (resp)
        return [status: 0, power: resp[0], bright: resp[1] ,ct: resp[2]]
    else
        return [status: -1]
}