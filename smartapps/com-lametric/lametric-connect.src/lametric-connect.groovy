/**
 *  LaMetric (Connect)
 *
 *  Copyright 2016 Smart Atoms Ltd.
 *  Author: Mykola Kirichuk
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
    name: "LaMetric (Connect)",
    namespace: "com.lametric",
    author: "Mykola Kirichuk",
    description: "Control your LaMetric Time smart display",
    category: "Family",
    iconUrl: "https://developer.lametric.com/assets/smart_things/smart_things_60.png",
    iconX2Url: "https://developer.lametric.com/assets/smart_things/smart_things_120.png",
    iconX3Url: "https://developer.lametric.com/assets/smart_things/smart_things_120.png",
    singleInstance: true)
{
    appSetting "clientId"
    appSetting "clientSecret"
}

preferences {
    page(name: "auth", title: "LaMetric", nextPage:"", content:"authPage", uninstall: true, install:true)
    page(name:"deviceDiscovery", title:"Device Setup", content:"deviceDiscovery", refreshTimeout:5);
}

mappings {
    path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
    path("/oauth/callback") {action: [GET: "callback"]}
}

import groovy.json.JsonOutput

def getEventNameListOfUserDeviceParsed(){ "EventListOfUserRemoteDevicesParsed" }
def getEventNameTokenRefreshed(){ "EventAuthTokenRefreshed" }

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    sendEvent(name:"Updated", value:true)
    unsubscribe()
    initialize()


}

def initialize() {
    // TODO: subscribe to attributes, devices, locations, etc.
    log.debug("initialize");
    state.subscribe  = false;
    if (selecteddevice) {
        addDevice()
        subscribeNetworkEvents(true)
        refreshDevices();
    }
}

/**
* Get the name of the new device to instantiate in the user's smartapps
* This must be an app owned by the namespace (see #getNameSpace).
*
* @return name
*/

def getDeviceName() {
    return "LaMetric"
}

/**
* Returns the namespace this app and siblings use
*
* @return namespace
*/
def getNameSpace() {
    return "com.lametric"
}


/**
* Returns all discovered devices or an empty array if none
*
* @return array of devices
*/
def getDevices() {
    state.remoteDevices = state.remoteDevices ?: [:]
}

/**
* Returns an array of devices which have been verified
*
* @return array of verified devices
*/
def getVerifiedDevices() {
    getDevices().findAll{ it?.value?.verified == true }
}

/**
* Generates a Map object which can be used with a preference page
* to represent a list of devices detected and verified.
*
* @return Map with zero or more devices
*/
Map getSelectableDevice() {
    def devices = getVerifiedDevices()
    def map = [:]
    devices.each {
        def value = "${it.value.name}"
        def key = it.value.id
        map["${key}"] = value
    }
    map
}

/**
* Starts the refresh loop, making sure to keep us up-to-date with changes
*
*/
private refreshDevices(){
    log.debug "refresh device list"
    listOfUserRemoteDevices()
    //every 30 min
    runIn(1800, "refreshDevices")
}

/**
* The deviceDiscovery page used by preferences. Will automatically
* make calls to the underlying discovery mechanisms as well as update
* whenever new devices are discovered AND verified.
*
* @return a dynamicPage() object
*/
/******************************************************************************************************************
DEVICE DISCOVERY AND VALIDATION
******************************************************************************************************************/
def deviceDiscovery()
{
    //    if(canInstallLabs())
    if (1)
    {
        //    	userDeviceList();
        log.debug("deviceDiscovery")
        def refreshInterval = 3 // Number of seconds between refresh
        int deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as int
            state.deviceRefreshCount = deviceRefreshCount + refreshInterval

        def devices = getSelectableDevice()
        def numFound = devices.size() ?: 0

        // Make sure we get location updates (contains LAN data such as SSDP results, etc)
        subscribeNetworkEvents()

        //device discovery request every 15s
        //        if((deviceRefreshCount % 15) == 0) {
        //            discoverLaMetrics()
        //        }

        // Verify request every 3 seconds except on discoveries
        if(((deviceRefreshCount % 5) == 0)) {
            verifyDevices()
        }

        log.trace "Discovered devices: ${devices}"

        return dynamicPage(name:"deviceDiscovery", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
            section("Please wait while we discover your ${getDeviceName()}. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
                input "selecteddevice", "enum", required:false, title:"Select ${getDeviceName()} (${numFound} found)", multiple:true, options:devices
            }
        }
    }
    else
    {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"deviceDiscovery", title:"Upgrade needed!", nextPage:"", install:true, uninstall: true) {
            section("Upgrade") {
                paragraph "$upgradeNeeded"
            }
        }
    }
}

/**

/**
* Starts a subscription for network events
*
* @param force If true, will unsubscribe and subscribe if necessary (Optional, default false)
*/
private subscribeNetworkEvents(force=false) {
    if (force) {
        unsubscribe()
        state.subscribe = false
    }
    if(!state.subscribe) {
        log.debug("subscribe on network events")
        subscribe(location, null, locationHandler, [filterEvents:false])
        //        subscribe(app, appHandler)
        state.subscribe = true
    }
}

private verifyDevices()
{
    log.debug "verify.devices"
    def devices = getDevices();
    for (it in devices) {
        log.trace ("verify device ${it.value}")
        def localIp = it?.value?.ipv4_internal;
        def apiKey = it?.value?.api_key;
        getAllInfoFromDevice(localIp, apiKey);
    }
}
def appHandler(evt)
{
    log.debug("application event handler ${evt.name}")
    if (evt.name == eventNameListOfUserDeviceParsed)
    {
        log.debug ("new account device list received ${evt.value}")
        def newRemoteDeviceList
        try {
            newRemoteDeviceList = parseJson(evt.value)
        } catch (e)
        {
            log.debug "Wrong value ${e}"
        }
        if (newRemoteDeviceList)
        {
            def remoteDevices = getDevices();
            newRemoteDeviceList.each{deviceInfo ->
                if (deviceInfo) {
                    def device = remoteDevices[deviceInfo.id]?:[:];
                        log.debug "before list ${device} ${deviceInfo} ${deviceInfo.id} ${remoteDevices[deviceInfo.id]}";
                    deviceInfo.each() {
                        device[it.key] = it.value;
                    }
                    remoteDevices[deviceInfo.id] = device;
                } else {
                    log.debug ("empty device info")
                }
            }
            verifyDevices();
        } else {
            log.debug "wrong value ${newRemoteDeviceList}"
        }
    } else if (evt.name == getEventNameTokenRefreshed())
    {
        log.debug "token refreshed"
        state.refreshToken = evt.refreshToken
        state.authToken = evt.access_token
    }
}

def locationHandler(evt)
{
    log.debug("network event handler ${evt.name}")
    if (evt.name == "ssdpTerm")
    {
        log.debug "ignore ssdp"
    } else {
        def lanEvent = parseLanMessage(evt.description, true)
        log.debug lanEvent.headers;
        if (lanEvent.body)
        {
            log.trace "lan event ${lanEvent}";
            def parsedJsonBody;
            try {
                parsedJsonBody = parseJson(lanEvent.body);
            } catch (e)
            {
                log.debug ("not json responce ignore $e");
            }
            if (parsedJsonBody)
            {
                log.trace (parsedJsonBody)
                log.debug("responce for device ${parsedJsonBody?.id}")
                //put or post response
                if (parsedJsonBody.success)
                {

                } else {
                    //poll response
                    log.debug "poll responce"
                    log.debug ("poll responce ${parsedJsonBody}")
                    def deviceId = parsedJsonBody?.id;
                    if (deviceId)
                    {
                        def devices = getDevices();
                        def device = devices."${deviceId}";

                        device.verified = true;
                        device.dni = [device.serial_number, device.id].join('.')
                        device.hub = evt?.hubId;
                        device.volume = parsedJsonBody?.audio?.volume;
                        log.debug "verified device ${deviceId}"
                        def childDevice = getChildDevice(device.dni)
                        //update device info
                        if (childDevice)
                        {
                            log.debug("send event to ${childDevice}")
                            childDevice.sendEvent(name:"currentIP",value:device?.ipv4_internal);
                            childDevice.sendEvent(name:"volume",value:device?.volume);
                            childDevice.setOnline();
                        }
                        log.trace device
                    }
                }
            }
        }
    }
}

/**
* Adds the child devices based on the user's selection
*
* Uses selecteddevice defined in the deviceDiscovery() page
*/
def addDevice() {
    def devices = getVerifiedDevices()
    def devlist
    log.trace "Adding childs"

    // If only one device is selected, we don't get a list (when using simulator)
    if (!(selecteddevice instanceof List)) {
        devlist = [selecteddevice]
    } else {
        devlist = selecteddevice
    }

    log.trace "These are being installed: ${devlist}"
    log.debug ("devlist" +  devlist)
    devlist.each { dni ->
        def newDevice = devices[dni];
        if (newDevice)
        {
            def d = getChildDevice(newDevice.dni)
            if(!d) {
                log.debug ("get child devices"  + getChildDevices())
                log.trace "concrete device ${newDevice}"
                def deviceName = newDevice.name
                d = addChildDevice(getNameSpace(), getDeviceName(), newDevice.dni, newDevice.hub, [label:"${deviceName}"])
                def childDevice = getChildDevice(d.deviceNetworkId)
                childDevice.sendEvent(name:"serialNumber", value:newDevice.serial_number)
                log.trace "Created ${d.displayName} with id $dni"
            } else {
                log.trace "${d.displayName} with id $dni already exists"
            }
        }
    }
}


//******************************************************************************************************************
//		 					 					 			OAUTH
//******************************************************************************************************************

def getServerUrl()           		{ "https://graph.api.smartthings.com" }
def getShardUrl()            		{ getApiServerUrl() }
def getCallbackUrl()        		{ "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()   		{ "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${shardUrl}" }
def getApiEndpoint()        		{ "https://developer.lametric.com" }
def getTokenUrl()					{ "${apiEndpoint}${apiTokenPath}" }
def getAuthScope() 					{ [ "basic", "devices_read" ] }
def getSmartThingsClientId() 		{ appSettings.clientId }
def getSmartThingsClientSecret() 	{ appSettings.clientSecret }
def getApiTokenPath()				{ "/api/v2/oauth2/token" }
def getApiUserMeDevicesList()		{ "/api/v2/users/me/devices" }

def toQueryString(Map m) {
    return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}
def composeScope(List scopes)
{
    def result = "";
    scopes.each(){ scope ->
        result += "${scope} "
    }
    if (result.length())
    return result.substring(0, result.length() - 1);
    return "";
}

def authPage() {
    log.debug "authPage()"

    if(!state.accessToken) { //this is to access token for 3rd party to make a call to connect app
        state.accessToken = createAccessToken()
    }

    def description
    def uninstallAllowed = false
    def oauthTokenProvided = false

    if(state.authToken) {
        description = "You are connected."
        uninstallAllowed = true
        oauthTokenProvided = true
    } else {
        description = "Click to enter LaMetric Credentials"
    }

    def redirectUrl = buildRedirectUrl
    log.debug "RedirectUrl = ${redirectUrl}"
    // get rid of next button until the user is actually auth'd
    if (!oauthTokenProvided) {
        return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall:uninstallAllowed) {
            section(){
                paragraph "Tap below to log in to the LaMatric service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
                href url:redirectUrl, style:"embedded", required:true, title:"LaMetric", description:description
            }
        }
    } else {
        subscribeNetworkEvents()
        listOfUserRemoteDevices()
        return deviceDiscovery();
    }
}


private refreshAuthToken() {
    log.debug "refreshing auth token"

    if(!state.refreshToken) {
        log.warn "Can not refresh OAuth token since there is no refreshToken stored"
    } else {
        def refreshParams = [
            method: 'POST',
            uri   : apiEndpoint,
            path  : apiTokenPath,
            body : [grant_type: 'refresh_token', 
                    refresh_token: "${state.refreshToken}", 
                    client_id : smartThingsClientId,
                    client_secret: smartThingsClientSecret,
                    redirect_uri: callbackUrl],
        ]

        log.debug refreshParams

        def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the LaMetric (Connect) SmartApp and re-enter your account login credentials."
        //changed to httpPost
        try {
            def jsonMap
            httpPost(refreshParams) { resp ->
                if(resp.status == 200) {
                    log.debug "Token refreshed...calling saved RestAction now! $resp.data"
                    jsonMap = resp.data
                    if(resp.data) {
                        state.refreshToken = resp?.data?.refresh_token
                        state.authToken = resp?.data?.access_token
                        if(state.action && state.action != "") {
                            log.debug "Executing next action: ${state.action}"

                            "${state.action}"()

                            state.action = ""
                        }

                    } else {
                        log.warn ("No data in refresh token!");
                    }
                    state.action = ""
                }
            }
        } catch (groovyx.net.http.HttpResponseException e) {
            log.error "refreshAuthToken() >> Error: e.statusCode ${e.statusCode}"
            log.debug e.response.data;
            def reAttemptPeriod = 300 // in sec
            if (e.statusCode != 401) { // this issue might comes from exceed 20sec app execution, connectivity issue etc.
                runIn(reAttemptPeriod, "refreshAuthToken")
            } else if (e.statusCode == 401) { // unauthorized
                state.reAttempt = state.reAttempt + 1
                log.warn "reAttempt refreshAuthToken to try = ${state.reAttempt}"
                if (state.reAttempt <= 3) {
                    runIn(reAttemptPeriod, "refreshAuthToken")
                } else {
                    sendPushAndFeeds(notificationMessage)
                    state.reAttempt = 0
                }
            }
        }
    }
}

def callback() {
    log.debug "callback()>> params: $params, params.code ${params.code}"

    def code = params.code
    def oauthState = params.state

    if (oauthState == state.oauthInitState){

        def tokenParams = [
            grant_type: "authorization_code",
            code      : code,
            client_id : smartThingsClientId,
            client_secret: smartThingsClientSecret,
            redirect_uri: callbackUrl
        ]
        log.trace tokenParams
        log.trace tokenUrl
        try {
            httpPost(uri: tokenUrl, body: tokenParams) { resp ->
                log.debug "swapped token: $resp.data"
                state.refreshToken = resp.data.refresh_token
                state.authToken = resp.data.access_token
            }
        } catch (e)
        {
            log.debug "fail ${e}";
        }
        if (state.authToken) {
            success()
        } else {
            fail()
        }
    } else {
        log.error "callback() failed oauthState != state.oauthInitState"
    }

}

def oauthInitUrl() {
    log.debug "oauthInitUrl with callback: ${callbackUrl}"

    state.oauthInitState = UUID.randomUUID().toString()

    def oauthParams = [
        response_type: "code",
        scope: composeScope(authScope),
        client_id: smartThingsClientId,
        state: state.oauthInitState,
        redirect_uri: callbackUrl
    ]
    log.debug oauthParams
    log.debug "${apiEndpoint}/api/v2/oauth2/authorize?${toQueryString(oauthParams)}"

    redirect(location: "${apiEndpoint}/api/v2/oauth2/authorize?${toQueryString(oauthParams)}")
}

def success() {
    def message = """
<p>Your LaMetric Account is now connected to SmartThings!</p>
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
<html lang="en"><head>
<meta charset="UTF-8">
<meta content="width=device-width" id="viewport" name="viewport">
<style>
@font-face {
font-family: 'latoRegular';
src: url("https://developer.lametric.com/assets/fonts/lato-regular-webfont.eot");
src: url("https://developer.lametric.com/assets/fonts/lato-regular-webfont.eot?#iefix") format("embedded-opentype"),
url("https://developer.lametric.com/assets/fonts/lato-regular-webfont.woff") format("woff"),
url("https://developer.lametric.com/assets/fonts/lato-regular-webfont.ttf") format("truetype"),
url("https://developer.lametric.com/assets/fonts/lato-regular-webfont.svg#latoRegular") format("svg");
font-style: normal;
font-weight: normal; }
.clearfix:after, .mobile .connect:after {
content: "";
clear: both;
display: table; }

.transition {
transition: all .3s ease 0s; }
html, body {
height: 100%;
}
body{
margin: 0;
padding: 0;
background: #f0f0f0;
color: #5c5c5c;
min-width: 1149px;
font-family: 'latoRegular', 'Lato';
}
.fixed-page #page {
min-height: 100%;
background: url(https://developer.lametric.com/assets/smart_things/page-bg.png) 50% 0 repeat-y;
}
.mobile {
min-width: 100%;
color: #757575; }
.mobile .wrap {
margin: 0 auto;
padding: 0;
max-width: 640px;
min-width: inherit; }
.mobile .connect {
width: 100%;
padding-top: 230px;
margin-bottom: 50px;
text-align: center; }
.mobile .connect img {
max-width: 100%;
height: auto;
vertical-align: middle;
display: inline-block;
margin-left: 2%;
border-radius: 15px; }
.mobile .connect img:first-child {
margin-left: 0; }
.mobile .info {
width: 100%;
margin: 0 auto;
margin-top: 50px;
margin-bottom: 50px; }
.mobile .info p {
max-width: 80%;
margin: 0 auto;
margin-top: 50px;
font-size: 28px;
line-height: 50px;
text-align: center; }

@media screen and (max-width: 639px) {
.mobile .connect{
padding-top: 100px; }
.mobile .wrap {
margin: 0 20px; }
.mobile .connect img {
width: 16%; }
.mobile .connect img:first-child, .mobile .connect img:last-child {
width: 40%; }
.mobile .info p{
font-size: 18px;
line-height: 24px;
margin-top: 20px; }
}
</style>
</head>
<body class="fixed-page mobile">

<div id="page">
<div class="wrap">

<div class="connect">
<img src="https://developer.lametric.com/assets/smart_things/product.png" width="190" height="190"><img src="https://developer.lametric.com/assets/smart_things/connected.png" width="87" height="19"><img src="https://developer.lametric.com/assets/smart_things/product-1.png" width="192" height="192">
</div>

<div class="info">
${message}
</div>
</div>
</div>
</body></html>
"""
    render contentType: 'text/html', data: html
}



//******************************************************************************************************************
//		 					 					 			LOCAL API
//******************************************************************************************************************

def getLocalApiDeviceInfoPath() 		{ "/api/v2/info" }
def getLocalApiSendNotificationPath()	{ "/api/v2/device/notifications" }
def getLocalApiIndexPath()				{ "/api/v2/device" }
def getLocalApiUser()					{ "dev" }


void requestDeviceInfo(localIp, apiKey)
{
    if (localIp && apiKey)
    {
        log.debug("request info ${localIp}");
        def command = new physicalgraph.device.HubAction([
            method: "GET",
            path: localApiDeviceInfoPath,
            headers: [
                HOST: "${localIp}:8080",
                Authorization: "Basic ${"${localApiUser}:${apiKey}".bytes.encodeBase64()}"
            ]])
        log.debug command
        sendHubCommand(command)
        command;
    } else {
        log.debug ("Unknown api key or ip address ${localIp} ${apiKey}")
    }
}

def sendNotificationMessageToDevice(dni, data)
{
    log.debug "send something"
    def device = resolveDNI2Device(dni);
    def localIp = device?.ipv4_internal;
    def apiKey = device?.api_key;
    if (localIp && apiKey)
    {
        log.debug "send notification message to device ${localIp}:8080 ${data}"
        sendHubCommand(new physicalgraph.device.HubAction([
            method: "POST",
            path: localApiSendNotificationPath,
            body: data,
            headers: [
                HOST: "${localIp}:8080",
                Authorization: "Basic ${"${localApiUser}:${apiKey}".bytes.encodeBase64()}",
                "Content-type":"application/json",
                "Accept":"application/json"
            ]]))
    }
}

def getAllInfoFromDevice(localIp, apiKey)
{
    log.debug "send something"
    if (localIp && apiKey)
    {
    	def hubCommand = new physicalgraph.device.HubAction([
            method: "GET",
            path: localApiIndexPath+"?fields=info,wifi,volume,bluetooth,id,name,mode,model,serial_number,os_version",
            headers: [
                HOST: "${localIp}:8080",
                Authorization: "Basic ${"${localApiUser}:${apiKey}".bytes.encodeBase64()}"
            ]])
        log.debug "sending request ${hubCommand}"
        sendHubCommand(hubCommand)
    }
}
//******************************************************************************************************************
//		 					 					 DEVICE HANDLER COMMANDs API
//******************************************************************************************************************

def resolveDNI2Device(dni)
{
    getDevices().find { it?.value?.dni == dni }?.value;
}

def requestRefreshDeviceInfo (dni)
{
    log.debug "device ${dni} request refresh";
    //	def devices = getDevices();
    //    def concreteDevice = devices[dni];
    //    requestDeviceInfo(conreteDevice);
}

private poll(dni) {
    def device = resolveDNI2Device(dni);
    def localIp = device?.ipv4_internal;
    def apiKey = device?.api_key;
    getAllInfoFromDevice(localIp, apiKey);
}

//******************************************************************************************************************
//		 					 					 			CLOUD METHODS
//******************************************************************************************************************


void listOfUserRemoteDevices()
{
    log.debug "get user device list"
    def deviceList = []
    if (state.accessToken)
    {
        def deviceListParams = [
            uri: apiEndpoint,
            path: apiUserMeDevicesList,
            headers: ["Content-Type": "text/json", "Authorization": "Bearer ${state.authToken}"]
        ]
        log.debug "making request ${deviceListParams}"
        def result;
        try {
            httpGet(deviceListParams){ resp ->
                if (resp.status == 200)
                {
                    deviceList = resp.data

                    def remoteDevices = getDevices();
                    for (deviceInfo in deviceList) {
                        if (deviceInfo)
                        {
                            def device = remoteDevices."${deviceInfo.id}"?:[:];
                                log.debug "before list ${device} ${deviceInfo} ${deviceInfo.id} ${remoteDevices[deviceInfo.id]}";
                            for (it in deviceInfo ) {
                                device."${it.key}" = it.value;
                            }
                            remoteDevices."${deviceInfo.id}" = device;
                        } else {
                            log.debug ("empty device info")
                        }
                    }
                    verifyDevices();
                } else {
                    log.debug "http status: ${resp.status}"
                }
            }
        } catch (groovyx.net.http.HttpResponseException e)
        {
            log.debug("failed to get device list ${e}")
            def status = e.response.status
            if (status == 401) {
                state.action = "refreshDevices"
                log.debug "Refreshing your auth_token!"
                refreshAuthToken()
            }
            return;
        }
    } else {
        log.debug ("no access token to fetch user device list");
        return;
    }
}