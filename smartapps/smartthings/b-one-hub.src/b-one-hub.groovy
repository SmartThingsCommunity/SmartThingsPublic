/**
 *  B.One Hub(Connect)
 *
 *  Copyright 2018 Sridhar Ponugupati
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
 import groovy.json.JsonSlurper
include 'localization'
definition(
    name: "B.One Hub",
    namespace: "smartthings",
    author: "Sridhar Ponugupati",
    description: "Connect Your B.One Hub to SmartThings",
    category: "SmartThings Labs",
    iconUrl: "https://b1hub.github.io/images/b1logo.png",
    iconX2Url: "https://b1hub.github.io/images/b1logo@2x.png",
    iconX3Url: "https://b1hub.github.io/images/b1logo@2x.png",
    singleInstance: true,
    usesThirdPartyAuthentication: true,
    pausable: false
    
    ) {
    appSetting "clientId"
    appSetting "clientSecret"
    appSetting "serverUrl"
}

preferences {
    page(name: "auth", title: "B.One", nextPage:"", content:"authPage", uninstall: true, install:false)
	page(name: "deviceList", title: "B.One", content:"boneDeviceList", install:true)
}

mappings {

	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "oauthCallback"]}
}

def getServerUrl()               { return  appSettings.serverUrl ?: apiServerUrl }
def getCallbackUrl()             { return "${getServerUrl()}/oauth/callback" }
def apiURL(path = '/') 			 { return "https://smartthings.b1automation.com/smartthings/${path}" }
def getBuildRedirectUrl()        { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${apiServerUrl}" }
def getSecretKey()               { return appSettings.clientSecret }
def getClientId()                { return appSettings.clientId }
def getChildName()               { return "AC Remote" }
def getChildActionName()         { return "Actions" }
def getChildTvRemoteName()         { return "Television Remote" }

private getVendorName() { "B.One Mini" }

def authPage() {

log.debug "authPage()"

	if (!state.initializeEndTime || (now() - state.initializeEndTime > 2000)) {
		unschedule()
		runIn(15*60, "restartPoll")	
	}

	if(!state.accessToken) { //this is to access token for 3rd party to make a call to connect app
		state.accessToken = createAccessToken()
	}

	def description
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(state.boneAccessToken) {
		
		description = "You are connected."
		uninstallAllowed = true
		oauthTokenProvided = true
	} else {
		description = "Click to enter B.One Credentials"
	}

	def redirectUrl = buildRedirectUrl
	log.debug "RedirectUrl = ${redirectUrl}"
	// get rid of next button until the user is actually auth'd
	if (!oauthTokenProvided) {
		return dynamicPage(name: "auth", title: "B.One", nextPage: "", uninstall:uninstallAllowed) {
			section() {
				paragraph "By Connecting SmartThings to your B.One Hub, You can control the Infrared Remotes ( Air-conditioner, Television ) and Actions of your B.One Hub via SmartThings APP."
				href url:redirectUrl, style:"embedded", required:true, title:"B.One", description:description
			}
		}
	} else {
		return dynamicPage(name: "auth", title: "B.One", nextPage:"deviceList", install: false, uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap Next to continue to setup your B.One Devices."
				href url:redirectUrl, style:"embedded", state:"complete", title:"B.One", description:description
			}
		}
	}

}


def restartPoll() {
	// This method should only be called in case the SA was terminated abnormally without
	// calling initialize which will unschedule this and start the poll as part of the normal flow
	// TODO Make sure child is calling any poll or command methods to prevent state changes
	//def childDevices = getChildDevices()
	//if (childDevices) {
	//	childDevices*.parentBusy(false)
	//}
	// Call poll
	unschedule()
	poll()
	runEvery5Minutes("poll")
}



def boneDeviceList() {
	getboneDevices()

	def devices = thermostatsDiscovered()
    def actions =triggerDiscover()
    def tvremotes=tvremoteDiscovered()
	def numThermostats = devices.size()
    def numActionList =actions.size()
    def numTvRemoteList =tvremotes.size()
    
	if (!numThermostats && ! numActionList) {
		return dynamicPage(name: "deviceList", title: "No devices found", uninstall: true) {
			section ("") {
				paragraph "Could not find any devices avilable for SmartThings to control. Please check your B.One account and retry later."
			}
		}
	}

	return dynamicPage(name: "deviceList", title: "Select Your B.One Devices", uninstall: true) {
    
		
       
			
			section("") {
             paragraph "All your B.One connected AC & TV Remotes, Actions are selected to work with SmartThings by default.You may choose to un-select a few devices by tapping respective section below."
             if (numThermostats > 0)  {
             def preselectedThermostats = devices.collect{it.key}
				input(name: "thermostats", title:"Select AC Remotes ({{numThermostats}} found)", messageArgs: [numThermostats: numThermostats],
						type: "enum", required:false, multiple:true, 
						description: "Tap to choose", metadata:[values:devices], defaultValue: preselectedThermostats)
			}
            }
            if (numActionList > 0)  {
             def preselectedActions = actions.collect{it.key}
			section("") {
				input(name: "ruleaction", title:"Select  Actions ({{numActionList}} found)", messageArgs: [numActionList: numActionList],
						type: "enum", required:false, multiple:true, 
						description: "Tap to choose", metadata:[values:actions], defaultValue: preselectedActions)
			}
            
            }
            if (numTvRemoteList > 0)  {
             def preselectedTv = tvremotes.collect{it.key}
			section("") {
				input(name: "tvremotelist", title:"Select TV Remote ({{numTvRemoteList}} found)", messageArgs: [numTvRemoteList: numTvRemoteList],
						type: "enum", required:false, multiple:true, 
						description: "Tap to choose", metadata:[values:tvremotes], defaultValue: preselectedTv)
			}
            
            }
		
       
		
	}
}

def oauthInitUrl() {
    state.oauthInitState = UUID.randomUUID().toString()
    def oauthParams = [client_id: "${appSettings.clientId}",state: state.oauthInitState, scope: "remote", response_type: "code",redirect_uri: "${getCallbackUrl()}" ]
    redirect(location: "https://smartthings.b1automation.com/authserver/auth2/v1/authorize?${toQueryString(oauthParams)}")
}

def oauthCallback() {
    def redirectUrl = null
    if (params.authQueryString) {
        redirectUrl = URLDecoder.decode(params.authQueryString.replaceAll(".+&redirect_url=", ""))
    } else {
        log.warn "No authQueryString"
    }

    if (state.boneAccessToken) {
        log.debug "Access token already exists"
        success()
    } else {
        def code = params.code
        if (code) {
            if (code.size() > 6) {
              
                log.debug "Exchanging code for access token"
                oauthReceiveToken(redirectUrl)
            } else {
                oauthInit()
            }
        } else {
            log.debug "This code should be unreachable"
            success()
        }
    }
}

def oauthReceiveToken(redirectUrl = null) {
    // Not sure what redirectUrl is for
    log.debug "receiveToken - params: ${params}"
    def oauthParams = [ client_id: "${appSettings.clientId}", client_secret: "${appSettings.clientSecret}", grant_type: "authorization_code", code: params.code, scope: params.scope ] // how is params.code valid here?
    def params = [
            uri: "https://smartthings.b1automation.com/authserver/auth2/v1/token",
            body: oauthParams,
            headers: [
                    "User-Agent": "SmartThings Integration"
            ]
    ]
    httpPost(params) { response ->
        state.boneAccessToken = response.data.access_token
        state.boneuserid=response.data.user_id
        state.bonehubid=response.data.hub_id
    }

    if (state.boneAccessToken) {
        oauthSuccess()
    } else {
        oauthFailure()
    }
}

def oauthSuccess() {
    def message = """
        <p>Your B.One Account is now connected to SmartThings!</p>
        <p>Click 'Done' to finish setup.</p>
    """
    oauthConnectionStatus(message)
}

def oauthFailure() {
    def message = """
        <p>The connection could not be established!</p>
        <p>Click 'Done' to return to the menu.</p>
    """
    oauthConnectionStatus(message)
}

def oauthReceivedToken() {
    def message = """
        <p>Your B.One Account is already connected to SmartThings!</p>
        <p>Click 'Done' to finish setup.</p>
    """
    oauthConnectionStatus(message)
}


def webhookCallback() {
    log.debug "webhookCallback"
    def data = request.JSON
    log.debug data
    if (data) {
        //updateDevicesFromResponse(data)
        [status: "ok", source: "smartApp"]
    } else {
        [status: "operation not defined", source: "smartApp"]
    }
}


def oauthConnectionStatus(message, redirectUrl = null) {
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
        <meta name="viewport" content="width=device-width">
        <title>SmartThings Connection</title>
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
                width: 280;
                padding: 20px;
                text-align: center;
            }
            img {
                vertical-align: middle;
            }
            img:nth-child(2) {
                margin: 0 15px;
            }
            p {
                font-size: 1.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                padding: 0 20px;
                margin-bottom: 0;
            }
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
        ${redirectHtml}
        </head>
        <body>
            <div class="container">
                <img src='https://b1hub.github.io/images/b1logo@2x.png' alt='bone icon' width='100'/>
                <img src='https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png' alt='connected device icon' width="40"/>
                <img src='https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png' alt='SmartThings logo' width="100"/>
                <p>
                    ${message}
                </p>
            </div>
        </body>
        </html>
    """
    render contentType: 'text/html', data: html
}

String toQueryString(Map m) {
    return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

// App lifecycle hooks

def installed() {
    if (!state.accessToken) {
        createAccessToken()
    } else {
        initialize()
    }
}

// called after settings are changed
def updated() {
    if (!state.accessToken) {
        createAccessToken()
    } else {
        initialize()
    }
}

def uninstalled() {
   if (state.boneAccessToken) {
		try {
			state.boneAccessToken = ""
            state.boneuserid = ""
            state.bonehubid = ""
			log.debug "B.One - Success disconnecting B.One from SmartThings"
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "B.One - Error disconnecting B.One from SmartThings: ${e.statusCode}"
		}
	}
}

// called after Done is hit after selecting a Location
def initialize() {

updateDevicesFromResponse()

	runIn(10, "purgeUninstalledDeviceData", [overwrite: true])

	def notificationMessage = "is connected to SmartThings"

	state.timeSendPush = null
	state.reAttempt = 0
	runEvery5Minutes("poll")
	poll()
	state.initializeEndTime = now()

}

void updateDevicesFromResponse() {
  
    def changed = false
    
    def devices =state.tvremote ?:[:] 
	def actions = state.rulesdata ?:[:]
	def acremotes = state.thermostats ?: [:]
    def deviceIds = []
    def children = getChildDevices()
    tvremotelist.collect{ device ->
    def tvlist=devices[device].data
        deviceIds << tvlist.deviceID
        def childDevice = children.find {it.deviceNetworkId == tvlist.deviceID}
        if (!childDevice) {
            log.trace "adding child device $devices[device].device_name"
            childDevice=addChildDevice(app.namespace, getChildTvRemoteName(), tvlist.deviceID, null, ["label": tvlist.name, "completedSetup": true])
            childDevice.generateEvent(tvlist)
            changed = true
        }
    }
    ruleaction.collect{ device ->
    def tvlist=actions[device].data
        deviceIds << actions[device].data.deviceID
        def childDevice = children.find {it.deviceNetworkId == tvlist.deviceID}
        if (!childDevice) {
            log.trace "adding child device $devices[device].device_name"
            childDevice=addChildDevice(app.namespace, getChildActionName(), tvlist.deviceID, null, ["label": tvlist.name, "completedSetup": true])
            childDevice.generateEvent(tvlist)
            changed = true
        }
    }
    thermostats.collect{ device ->
    def tvlist=acremotes[device].data
        deviceIds << tvlist.deviceID
        def childDevice = children.find {it.deviceNetworkId == tvlist.deviceID}
        if (!childDevice) {
            log.trace "adding child device $devices[device].device_name"
            childDevice=addChildDevice(app.namespace, getChildName(), tvlist.deviceID, null, ["label": tvlist.name, "completedSetup": true])
            childDevice.generateEvent(tvlist)
            changed = true
        }
    }

    children.findAll { !deviceIds.contains(it.deviceNetworkId) }.each {
        log.trace "deleting child device $it.device_name"
        deleteChildDevice(it.deviceNetworkId)
        changed = true
    }


}




Map apiRequestHeaders() {
    return ["Authorization": "Bearer ${state.boneAccessToken}",
            "Accept": "application/json",
            "Content-Type": "application/json",
            "token":" ${state.boneAccessToken}",
            "User-Agent": "SmartThings Integration"
    ]
}


def getThermostatData(data) {
	def acstatus="Right Now : OFF"
    if(data.powerstatus=="on")
    {
     def val="Auto"
    if(data.mode=="cool")
    {
    val="Cool"
    }
    if(data.mode=="dry")
    {
     val="Dry"
    }
    if(data.mode=="fan")
    {
     val="Fan"
    }
    if(data.mode=="heat")
    {
     val="Heat"
    }
    
   
    val.toUpperCase();
    acstatus="Right Now : ${val}"
    }

	return [
			name: data.device_name,
            deviceID:data.device_id,
            deviceBID:data.device_b_one_id,
			coolMode: (0 > 0),
			heatMode: (0 > 0),
			deviceTemperatureUnit: "C",
			minHeatingSetpoint: 16,
			maxHeatingSetpoint: 31,
			autoMode: true,
			deviceAlive: true,
			temperature: data.temp,
			thermostatMode: data.mode,
            thermostatPowerMode:data.powerstatus,
			thermostatFanMode: data.fanMode,
            acstatusvalue:acstatus
	]
}


def getRulesListData(data) {

	return [
			name: data.device_name,//stat.name ? stat.name : stat.identifier),
            deviceID:data.device_id,
            deviceBID:data.device_b_one_id,
			cmdType:data.cmdType,
            deviceAlive: true,
            status:data.status
	]
}

def getTvRemoteListData(data) {
	return [
			name: data.device_name,//stat.name ? stat.name : stat.identifier),
            deviceID:data.device_id,
            deviceBID:data.device_b_one_id,
            deviceAlive: true,
	]
}



def getboneDevices() {
	log.debug "getting device list"
	state.remoteSensors = [] // reset depriciated application state, replaced by remoteSensors2

	def devices = [:]
	def remoteSensors = [:]
	def switchList = [:]
    def actions = [:]
    def tvremotes =[:]
	def isThermostatPolled = false
	def isSwitchesPolled = false
	def pollAttempt = 1
	// try obtain devices twice, in case authToken needs to be refreshed
	if(!(isThermostatPolled)) {
		try {
			// First get thermostats their remote sensors
			if (!isThermostatPolled) {
				
				def deviceListParams = [
						uri:     apiURL("device"),
						headers: apiRequestHeaders(),
				]
                //log.debug "getting device list:${deviceListParams}"
				httpPost(deviceListParams) { resp ->
                 //log.debug "getting device list1:${resp.data}"
					isThermostatPolled = true
					if (resp.status == 200) {
						resp.data.devices.each { stat ->
							def dni = stat.device_id
							def data = getThermostatData(stat)
							devices[dni] = devices[dni] ? devices[dni] << [data:data] : [data:data]
							devices[dni].polled = true
							devices[dni].pollAttempts = 0
							// compile all remote sensors conected to the thermostat
						}
                        resp.data.actions.each { stat ->
							def dni = stat.device_id
							def data = getRulesListData(stat)
                            //log.debug "Action Data Check:${data}"
							actions[dni] = actions[dni] ? actions[dni] << [data:data] : [data:data]
							actions[dni].polled = true
							actions[dni].pollAttempts = 0
							// compile all remote sensors conected to the thermostat
						}
                         resp.data.tvremote.each { stat ->
							def dni = stat.device_id
							def data = stat
                            //log.debug "TvData Data Check:${data}"
							tvremotes[dni] = tvremotes[dni] ? tvremotes[dni] << [data:data] : [data:data]
							tvremotes[dni].polled = true
							tvremotes[dni].pollAttempts = 0
							// compile all remote sensors conected to the thermostat
						}
						state.thermostats = devices
                        state.rulesdata=actions
                        state.tvremote = tvremotes
					} else {
						log.debug "Failed to get thermostats and sensors, status:${resp.status}"
					}
				}
			}
			
		} catch (groovyx.net.http.HttpResponseException e) {
			//log.error "Exception getBoneDevices: ${e?.getStatusCode()}, e:${e}, data:${e.response?.data}"
			if (e.response?.data?.status?.code == 14) {
				pollAttempt++
				if (pollAttempt > 2 || !refreshAuthToken()) {
					pollAttempt = 3
					log.error "BOne failed getting devices despite refreshing authToken"
				}
			}
		} catch (Exception e) {
			//log.error "Unhandled exception $e in getBOneDevices tried:${pollAttempt} times"
			// break the loop and exit
			pollAttempt = 3
		}
	}
}



Map thermostatsDiscovered() {
	def map = [:]
    //log.debug "list of data is:${state.thermostats}"
	def thermostatList = state.thermostats ?: [:]
	thermostatList.each { key, stat ->
		map[key] = stat.data.name
	}
	return map
}
Map triggerDiscover(){
def map = [:]
    //log.debug "list of data is:${state.rulesdata}"
	def RulesList = state.rulesdata ?: [:]
	RulesList.each { key, stat ->
		map[key] = stat.data.name
	}
	return map
}
Map tvremoteDiscovered(){
def map = [:]
    //log.debug "list of data is:${state.tvremote}"
	def TvRemoteList = state.tvremote ?: [:]
	TvRemoteList.each { key, stat ->
		map[key] = stat.data.name
	}
	return map
}

def poll() {
	// No need to keep trying to poll if authToken is null
	if (!state.boneAccessToken) {
		log.info "poll failed due to authToken=null"
		markChildrenOffline(true)
		unschedule()
		unsubscribe()
		return
	}
	def isThermostatPolled = false // If no thermostats or sensors, mark them polled                 // If no switches, mark them polled
	def pollAttempt = 1

	// Mark all devices as offline for device health
	def devices = state.thermostats ?: [:]
	devices.each { dni, stat ->
		stat.polled = false
		stat.data = stat.data ? stat.data << [deviceAlive:false] : [deviceAlive:false]
	}
	def actions = state.rulesdata ?: [:]
    actions.each { dni, stat ->
		stat.polled = false
		stat.data = stat.data ? stat.data << [deviceAlive:false] : [deviceAlive:false]
	}
    def tvremotes = state.tvremote ?: [:]
    tvremotes.each { dni, stat ->
		stat.polled = false
		stat.data = stat.data ? stat.data << [deviceAlive:false] : [deviceAlive:false]
	}

	state.thermostats = devices
    state.rulesdata = actions
    state.tvremote = tvremotes
	if (!isThermostatPolled ) {
		try{
			// First check if we need to poll thermostats or sensors
			if (!isThermostatPolled) {
				def pollParams = [
					uri: apiURL("device"),
					headers: apiRequestHeaders(),
				]

				httpPost(pollParams) { resp ->
					isThermostatPolled = true
					if(resp.status == 200) {
                    //log.info "poll success due to ${resp.data.devices}"
                    if(resp.data.status=="1")
                    {
                    storeThermostatData(resp.data.devices)	
                    storeActionData(resp.data.actions)
                    storeTvRemoteData(resp.data.tvremote)
                    }
						
					}
				}
			}
			

		} catch (groovyx.net.http.HttpResponseException e) {
			log.info "HttpResponseException ${e}, ${e?.getStatusCode()} polling BOne pollAttempt:${pollAttempt}, " +
					"isThermostatPolled:${isThermostatPolled}, isSwitchesPolled:${isSwitchesPolled}, ${e?.response?.data}"
			if (e?.getStatusCode() == 401 || e?.response?.data?.status?.code == 14)  {
				pollAttempt++
				// Try refresh authToken and try poll one more time
				if (pollAttempt > 2 || !refreshAuthToken()) {
					// refresh of authToken failed, break the loop and exit
					pollAttempt = 3
					log.error "B.One poll failed despite refreshing authToken"
				}
			} else {
				log.error "B.One poll failed for other reason than expired authToken"
				// break the loop and exit
				pollAttempt = 3
			}
		} catch (Exception e) {
			log.error "Unhandled exception $e in b.one polling pollAttempt:${pollAttempt}, " +
					"isThermostatPolled:${isThermostatPolled}, isSwitchesPolled:${isSwitchesPolled}"
			// break the loop and exit
			pollAttempt = 3
		}
	}
	//markChildrenOffline()
	log.trace "poll exit pollAttempt:${pollAttempt}, isThermostatPolled:${isThermostatPolled}, " 
}
def poll1(deviceId,status)
{
def payload = [
        "device_id":"${deviceId}",
        "status":"${status}"
    ]
	poll2(payload)
    return true

}
def poll2(Map bodyParam) {
              // If no switches, mark them polled
	 // If no thermostats or sensors, mark them polled   
     //log.trace "poll exit pollAttempt1 ${bodyParam.device_id}"
     // If no switches, mark them polled
    
    def isThermostatPolled = false // If no thermostats or sensors, mark them polled                 // If no switches, mark them polled
	def pollAttempt = 1

	// Mark all devices as offline for device health
	def devices = state.thermostats ?: [:]
	devices.each { dni, stat ->
		stat.polled = false
		stat.data = stat.data ? stat.data << [deviceAlive:false] : [deviceAlive:false]
	}
	def actions = state.rulesdata ?: [:]
    actions.each { dni, stat ->
		stat.polled = false
		stat.data = stat.data ? stat.data << [deviceAlive:false] : [deviceAlive:false]
	}

	state.thermostats = devices
    state.rulesdata = actions


	if (!isThermostatPolled ) {
	
		try{
			// First check if we need to poll thermostats or sensors
				def pollParams = [
					uri: apiURL("getLatestStatus"),
					headers: ["Content-Type": "application/json", "Authorization": "Bearer ${state.boneAccessToken}","token":"${state.boneAccessToken}","device_id":"${bodyParam.device_id}"],
				]
				//log.trace "poll exit pollAttempt2 ${pollParams}"
				httpPost(pollParams) { resp ->
                isThermostatPolled = true
					if(resp.status == 200) {
                    log.trace "poll exit pollAttempt2 ${resp.data}"
                    if(resp.data.status=="2")
                    {
                    purgeChildDevice(bodyParam.device_id)
                    }
                     else if(resp.data.status=="3")
                    {
                    try {
                    def msg="Your B.One is disconnected from SmartThings, because the access credential changed or was lost. Please go to the B.One Hub SmartApp and re-enter your account login credentials."
                    messagePush(msg)
			            state.boneAccessToken = ""
                        state.boneuserid = ""
                         state.bonehubid = ""
			             log.debug "B.One - Success disconnecting B.One from SmartThings"
		                } catch (groovyx.net.http.HttpResponseException e) {
			              log.error "B.One - Error disconnecting B.One from SmartThings: ${e.statusCode}"
		               }
                    }
                    else
                    {
                    //log.debug "B.One - Success disconnecting B.One from ${bodyParam.status}"
                     if(bodyParam.status=="0")
                    {
                    log.debug "B.One - Success disconnecting B.One from ${bodyParam.status}"
                     storeThermostatData(resp.data.devices)	
                    }
                     if(bodyParam.status=="1")
                    {
                     storeActionData(resp.data.devices)	
                    }
                    }
                    
                    
                   
					}
				}
			
			

		} catch (groovyx.net.http.HttpResponseException e) {
			log.info "HttpResponseException ${e}, ${e?.getStatusCode()} polling BOne pollAttempt:${e?.response?.data}"
			if (e?.getStatusCode() == 401 || e?.response?.data?.status?.code == 14)  {
				pollAttempt++
				// Try refresh authToken and try poll one more time
				if (pollAttempt > 2 || !refreshAuthToken()) {
					// refresh of authToken failed, break the loop and exit
					pollAttempt = 3
					log.error "bone poll failed despite refreshing authToken"
				}
			} else {
				log.error "bone poll failed for other reason than expired authToken"
				// break the loop and exit

			}
		} catch (Exception e) {
			log.error "Unhandled exception $e in b.one polling pollAttempt:"
			// break the loop and exit

		}
        }
	log.trace "poll exit pollAttempt " 
}


def toJson(Map m) {
    return groovy.json.JsonOutput.toJson(m)
}

private void storeThermostatData(thermostatData) {
	def data
	def devices = state.thermostats ?: [:]
	thermostatData.each { stat ->
		def dni =stat.device_id 
		def childDevice = getChildDevice(dni)
		data = getThermostatData(stat)
		if (childDevice) {
			if (!childDevice.getDataValue("EnrolledUTDH")) {
				childDevice.updated()
			}
			if (childDevice.displayName != data.name) {
				childDevice.setDisplayName(data.name)
			}
			if (data["deviceAlive"]) {
				childDevice.generateEvent(data)
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
			} else {
				childDevice.sendEvent("name":"thermostat", "value":"Offline")
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
			}
			devices[dni] = devices[dni] ? devices[dni] << [data:data] : [data:data]
			devices[dni].polled = true
			devices[dni].pollAttempts = 0
		} else {
			log.info "Got poll data for ${data.name} with identifier ${stat.identifier} that doesn't have a DTH"
		}
		// Make sure any remote senors connected to the thermostat are marked offline too
	}
	state.thermostats = devices
}

private void storeActionData(ActionData) {
	def data
	def actions = state.rulesdata ?: [:]
	ActionData.each { stat ->
		def dni = stat.device_id
		def childDevice = getChildDevice(dni)
		data = getRulesListData(stat)
		if (childDevice) {
			if (!childDevice.getDataValue("EnrolledUTDH")) {
				childDevice.updated()
			}
			if (childDevice.displayName != data.name) {
				childDevice.setDisplayName(data.name)
			}
			if (data["deviceAlive"]) {
				childDevice.generateEvent(data)
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
			} else {
				childDevice.sendEvent("name":"actons", "value":"Offline")
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
			}
			actions[dni] = actions[dni] ? actions[dni] << [data:data] : [data:data]
			actions[dni].polled = true
			actions[dni].pollAttempts = 0
		} else {
			log.info "Got poll data for ${data.name} with identifier ${stat.identifier} that doesn't have a DTH"
		}
		// Make sure any remote senors connected to the thermostat are marked offline too
	}
	state.rulesdata = actions
}

private void storeTvRemoteData(RemoteData) {
	def data
	def tvremotes = state.tvremote ?: [:]
	RemoteData.each { stat ->
		def dni =stat.device_id
		def childDevice = getChildDevice(dni)
		data = stat
		if (childDevice) {
			if (!childDevice.getDataValue("EnrolledUTDH")) {
				childDevice.updated()
			}
			if (childDevice.displayName != data.name) {
				childDevice.setDisplayName(data.name)
			}
			if (data["deviceAlive"]) {
				childDevice.generateEvent(data)
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
			} else {
				childDevice.sendEvent("name":"actons", "value":"Offline")
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
			}
			tvremotes[dni] = tvremotes[dni] ? tvremotes[dni] << [data:data] : [data:data]
			tvremotes[dni].polled = true
			tvremotes[dni].pollAttempts = 0
		} else {
			log.info "Got poll data for ${data.name} with identifier ${stat.identifier} that doesn't have a DTH"
		}
		// Make sure any remote senors connected to the thermostat are marked offline too
	}
	state.tvremote = tvremotes
}


def purgeUninstalledDeviceData() {
	// purge state from devices that are not selected
	def devices = state.thermostats ?: [:]
	def actions = state.rulesdata ?: [:]
    def tvremot = state.tvremote ?:[:]

	devices.keySet().removeAll(devices.keySet() - thermostats)
	actions.keySet().removeAll(actions.keySet() - ruleaction)
	tvremot.keySet().removeAll(tvremot.keySet() - tvremotelist)
	state.thermostats = devices
	state.rulesdata = actions
	state.tvremote = tvremot
}

def purgeChildDevice(childDevice) {
log.debug("device remove success")
	def dni = childDevice
	def devices = state.thermostats ?: [:]
	def actions = state.rulesdata ?: [:]
    def tvremot = state.tvremote ?:[:]
    
     try {
     log.debug("device remove success")
            deleteChildDevice(childDevice)
        } catch (Exception e) {
            log.debug("Can't remove this device because it's being used by an SmartApp")
        }
	if (devices[dni]) {
		devices.remove(dni)
		state.thermostats = devices
		if (thermostats) {
			thermostats.remove(dni)
		}
		app.updateSetting("thermostats", thermostats ? thermostats : [])
	} else if (actions[dni]){
		actions.remove(dni)
		state.rulesdata = actions
		if (ruleaction) {
			ruleaction.remove(dni)
		}
		app.updateSetting("ruleaction", ruleaction ? ruleaction : [])
	}else if (tvremot[dni]){
		tvremot.remove(dni)
		state.tvremote = tvremot
		if (tvremotelist) {
			tvremotelist.remove(dni)
		}
		app.updateSetting("ruleaction", tvremotelist ? tvremotelist : [])
	} else {
		log.error "Failed to purge data for childDevice dni:$dni"
	}
	if (getChildDevices().size <= 1) {
		log.info "No more thermostats to poll, unscheduling"
		unschedule()
		state.authToken = null
		runIn(1, "terminateMe")
	}
}

private boolean sendCommandToBOne(Map bodyParams) {
	// no need to try sending a command if authToken is null
	if (!state.boneAccessToken) {
		log.warn "sendCommandToBOne failed due to authToken=null"
		return false
	}
	def isSuccess = false
	def cmdParams = [
		uri: apiURL("sendevent"),
		headers: ["Content-Type": "application/json", "Authorization": "Bearer ${state.boneAccessToken}","token":"${state.boneAccessToken}"],
		body: toJson(bodyParams)
	]
	def keepTrying = true
	def cmdAttempt = 1

	while (keepTrying) {
		try{
			httpPost(cmdParams) { resp ->
				keepTrying = false
				if(resp.status == 200) {
					log.debug "updated ${resp.data}"
					def returnStatus = resp.data.status
					if (returnStatus == "1") {
						log.debug "Successful call to bone API."
						isSuccess = true
                        if(bodyParams.status)
                        {
                        poll1(bodyParams.device_mid,"1")
                        }
                        else if(bodyParams.keynum){
                        isSuccess = true
                        }
                        else
                        {
                        poll1(bodyParams.device_mid,"0")
                        }
                        //poll1(bodyParams.device_id,"0")
					} else {
                    isSuccess = true
						log.debug "Error return code = ${returnStatus}"
					}
				}
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			log.info "Exception sending command: $e, status:${e.getStatusCode()}, ${e?.response?.data}"
			if (e.response.data.status.code == 14) {
				cmdAttempt++
				if (cmdAttempt > 2 || !refreshAuthToken()) {
					// refresh authToken failed, break loop and exit
					log.error "Error refreshing auth_token! Unable to send command"
					keepTrying = false
				} else {
					cmdParams.headers.Authorization = "Bearer ${state.authToken}"
				}
			} else {
				log.error "Exception sending command: Authentication error, invalid authentication method, lack of credentials, etc."
				keepTrying = false
			}
		}
	}
	return isSuccess
}

boolean setMode(mode,deviceId,devicemID) {

log.info "sending command:${mode}, ${deviceId}"
    def payload = [
        device_id:deviceId,
        mode:mode,
        device_mid:devicemID
    ]
	return sendCommandToBOne(payload)
}


boolean sendIrData(keynum, deviceId) {

log.info "sending command:${keynum}, ${deviceId}"
    def payload = [
        device_id:deviceId,
        keynum:keynum
    ]
	return sendCommandToBOne(payload)
}
boolean setPowerMode(mode, deviceId,devicemID) {

log.info "sending command:${mode}, ${deviceId}"
    def payload = [
        device_id:deviceId,
        power:mode,
        device_mid:devicemID
    ]
	return sendCommandToBOne(payload)
}
boolean setFanMode(mode, deviceId,devicemID) {
log.info "sending command:${mode}, ${deviceId}"
    def payload = [
        device_id:deviceId,
        fan:mode,
        device_mid:devicemID
    ]
	return sendCommandToBOne(payload)
}
boolean setTemperatueAc(value, deviceId,devicemID) {
log.info "sending command:${value}, ${deviceId}"
    def payload = [
        device_id:deviceId,
        temp:value,
        device_mid:devicemID
    ]
	return sendCommandToBOne(payload)
}


boolean setSwitchOnOff(status,deviceId,devicemID)
{
log.info "sending command:${status}, ${deviceId},${devicemID}"
def payload = [
        device_id:deviceId,
        status:status,
        device_mid:devicemID
    ]
	return sendCommandToBOne(payload)

}

boolean setActionsAC(mode,temp,deviceId,devicemID)
{
log.info "sending command:${mode},${temp},${deviceId},${devicemID}"
def payload = [
        device_id:deviceId,
        mode:mode,
        temp:temp,
        device_mid:devicemID
    ]
	return sendCommandToBOneAction(payload)

}
private boolean sendCommandToBOneAction(Map bodyParams) {
	// no need to try sending a command if authToken is null
	if (!state.boneAccessToken) {
		log.warn "sendCommandToBOne failed due to authToken=null"
		return false
	}
	def isSuccess = false
	def cmdParams = [
		uri: apiURL("sendeventaction"),
		headers: ["Content-Type": "application/json", "Authorization": "Bearer ${state.boneAccessToken}","token":"${state.boneAccessToken}"],
		body: toJson(bodyParams)
	]
	def keepTrying = true
	def cmdAttempt = 1

	while (keepTrying) {
		try{
			httpPost(cmdParams) { resp ->
				keepTrying = false
				if(resp.status == 200) {
					log.debug "updated ${resp.data}"
					def returnStatus = resp.data.status
					if (returnStatus == "1") {
						log.debug "Successful call to bone API."
						isSuccess = true
                        if(bodyParams.status)
                        {
                        poll1(bodyParams.device_mid,"1")
                        }
                        else if(bodyParams.keynum){
                        isSuccess = true
                        }
                        else
                        {
                        poll1(bodyParams.device_mid,"0")
                        }
                        //poll1(bodyParams.device_id,"0")
					} else {
                    isSuccess = true
						log.debug "Error return code = ${returnStatus}"
					}
				}
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			log.info "Exception sending command: $e, status:${e.getStatusCode()}, ${e?.response?.data}"
			if (e.response.data.status.code == 14) {
				cmdAttempt++
				if (cmdAttempt > 2 || !refreshAuthToken()) {
					// refresh authToken failed, break loop and exit
					log.error "Error refreshing auth_token! Unable to send command"
					keepTrying = false
				} else {
					cmdParams.headers.Authorization = "Bearer ${state.authToken}"
				}
			} else {
				log.error "Exception sending command: Authentication error, invalid authentication method, lack of credentials, etc."
				keepTrying = false
			}
		}
	}
	return isSuccess
}

def polingtest()
{
log.debug "data test in values of the handular"
}
def messagePush(msg){
log.debug "message ${msg}"
sendPush(msg)
}