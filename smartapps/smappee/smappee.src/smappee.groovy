/**
 *  Smappee
 *
 *  Copyright 2015 Chuck J
 *
 */
definition(
        name: "Smappee",
        namespace: "smappee",
        author: "Chuck J",
        description: "Smappee Power Meter",
        category: "My Apps",
        iconUrl: "https://static.smappee.net/img/AppIcon.png",
        iconX2Url: "https://static.smappee.net/img/AppIconx2.png",
        iconX3Url: ""
){
        appSetting "clientId"
        appSetting "clientSecret"
}

preferences {
    page(name: "Credentials", title: "Smappee Authentication", content: "authPage", nextPage: "deviceList", uninstall: true)
    page(name: "deviceList", title: "Smappee", content:"smappeeDeviceList", install:true)
}

mappings {
    path("/oauth/initialize") {action: [GET: "init"]}
    path("/oauth/callback") {action: [ GET: "callback" ]}
    path("/receiveToken")   {action: [POST: "receiveToken", GET: "receiveToken"]}
}

//Section2: page-related methods ---------------------------------------------------------------------------------------
def authPage() {
    log.debug "authPage()"

    if(!state.sampleAccessToken) { //this is used by ST dynamic callback uri
        state.sampleAccessToken = createAccessToken()
        log.debug "created access token: ${state.sampleAccessToken}"
        log.debug "app.id: $app.id"
    }

    def description = null
    def uninstallAllowed = false
    def oauthTokenProvided = false

    if(state.authToken) { //obtain authToken from smappee
        description = "You are connected."
        uninstallAllowed = true
        oauthTokenProvided = true
    } else {
        description = "Click to enter your Smappee credentials"
    }

    def redirectUrl = "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}"
    log.debug "RedirectUrl = ${redirectUrl}"

    if (!oauthTokenProvided) {

        return dynamicPage(name: "Credentials", title: "Login", nextPage:null, uninstall:uninstallAllowed) {
            section(){
                paragraph "Tap below to log in to the Smappee service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
                href url:redirectUrl, style:"embedded", required:true, title:"Smappee", description:description
            }
        }

    } else {
        return dynamicPage(name: "Credentials", title: "Log In", nextPage:"deviceList") {
            section(){
                paragraph "Tap Next to continue to setup your Smappeee devices."
                href url:redirectUrl, style:"embedded", state:"complete", title:"Smappee", description:description
            }
        }
    }
}

//1. redirect SmartApp to prompt user to input his/her credentials on 3rd party cloud service
def init() {
    log.debug "init()"
    state.oauthInitState = UUID.randomUUID().toString()

    def oauthParams = [
            response_type: "code",
            client_id: clientId,
            state: state.oauthInitState,
            redirect_uri: "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/receiveToken"
    ]

    log.debug "https://farm1pub.smappee.net/dev/v1/oauth2/authorize?${toQueryString(oauthParams)}"
    redirect(location: "https://farm1pub.smappee.net/dev/v1/oauth2/authorize?${toQueryString(oauthParams)}")
}

/*2. Obtain authorization_code, access_token, refresh_token to be used with API calls
    2.1 get authorization_code from 3rd party cloud service
    2.2 use authorization_code to get access_token, refresh_token, and expire from 3rd party cloud service
*/
def receiveToken() {
    log.debug "receiveToken()>> params: $params, params.code ${params.code}"

    state.sampleAccessCode = params.code

    def tokenParams = [
            grant_type  : "authorization_code",
            code        : state.sampleAccessCode,
            client_id   : clientId,
            client_secret: clientSecret,
            redirect_uri: "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/receiveToken"
    ]

    def tokenUrl = "https://farm1pub.smappee.net/dev/v1/oauth2/token?${toQueryString(tokenParams)}"
    log.debug "url to get tokens: $tokenUrl"
    try {
        httpGet(uri: tokenUrl) { resp ->
            state.authToken = resp.data.access_token.toString()
            state.refreshToken = resp.data.refresh_token.toString()
            state.authTokenExpireIn = resp.data.expires_in.toString()
            log.debug "Response: ${resp.data}"
            log.debug "access_token: ${state.authToken}"
            log.debug "refresh_token: ${state.refreshToken}"
            log.debug "expires_in: ${state.authTokenExpireIn}"
        }
    } catch (groovyx.net.http.HttpResponseException e) {
        log.error "Error: ${e.statusCode}"
    }

    if (state.authToken) {
        success()
    } else {
        fail()
    }
}

def success() {
    def message = """
		<p>Your Smappee Account is now connected to SmartThings!</p>
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
        ${redirectHtml}
        </head>
        <body>
                <div class="container">
                        <img src="https://static.smappee.net/img/AppIcon.png" alt="Smappee icon" />
                        <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                        <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                        ${message}
                </div>
        </body>
        </html>
        """
    render contentType: 'text/html', data: html
}

/*
This method is called after "auth" page is done with Oauth2 authorization, then page "deviceList" with content of
smappeeDeviceList() will prompt the user to select which device in his/her account to be used with ST.
Return -> (Map)devices object from page input (by user)
 */
def smappeeDeviceList() {
    log.debug "Page : smappeeDeviceList()"
    //step1: get (list) of available devices associated with the login account.
    def devices = getSmappeeDevices()
    log.debug "Page smappeeDeviceList() device list: $devices"

    //step2: render the page for user to select which device
    def p = dynamicPage(name: "deviceList", title: "Select Your Devices", uninstall: true) {
        section(){
            paragraph "Tap below to see the list of smappee devices available in your account and select the ones you want to connect to SmartThings."
            //input to store all selected devices to be shown as (thermostat) things
            input(name: "smappees", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", options:devices)
        }
    }
}

/*this method make HTTP GET to get locations, then use those locations to make another HTTP GET to obtain devices
  associated with a particular locations. Return-> Map([dni(mac addr):deviceDisplayName, ...])
*/
def getSmappeeDevices() {
    /*
    required: 1. GET locations (headers: Content-Type, Authorization, params: apikey)
              2. GET devices (headers: Content-Type, Authorization, params: apikey, locationId)
     */
    log.debug "Page : smappeeDeviceList() called getSmappeeDevices()"

    //Step1: GET locations - return state.locationId, state.locationName,  state.locationZipcode
    def deviceLocationsParams = [
            uri: "https://farm1pub.smappee.net",
            path: "/dev/v1/servicelocation",
            headers: ["Authorization": "Bearer ${state.authToken}"]
    ]

    httpGet(deviceLocationsParams) { resp ->

        log.debug "resp.data : ${resp.data}"

        if(resp.status == 200 && resp.data) {
            state.serviceLocationId = resp.data.serviceLocations.first().serviceLocationId.toString()
            state.serviceLocationName = resp.data.serviceLocations.first().name
        } else {
            log.debug "http status: ${resp.status}"

            //refresh the auth token
            if (resp.status == 500 && resp.data.status.code == 14) {
                log.debug "Storing the failed action to try later"
                data.action = "getHoneywellThermostats"
                log.debug "Refreshing your auth_token!"
                refreshAuthToken()
            } else {
                log.error "Authentication error, invalid authentication method, lack of credentials, etc."
            }
        }
    }

    //Step2: GET devices (headers: Content-Type, Authorization, params: apikey, locationId) - need locationId from previous call
    def devices = [:]  //Map object to store number of found devices [dni:deviceDisplayName]
    def dni

    def deviceListParams = [
            uri: "https://farm1pub.smappee.net",
            path: "/dev/v1/servicelocation/${state.serviceLocationId}/info",
            headers: ["Authorization": "Bearer ${state.authToken}"],
    ]

    httpGet(deviceListParams) { resp ->

        log.debug "device list ${resp.data}"
        log.warn "actuators ${resp.data.actuators} : ${resp.data.actuators.size()}"   //if plugs exist add Smappee Plug
        log.warn "electricityCost ${resp.data.electricityCost}"

        if(resp.status == 200 && resp.data) {

            dni = "${getNamePrefix()}:${resp.data.serviceLocationId.toString()}"
            devices[dni] = getSmappeeMeterDisplayName(resp.data.name)
            log.info "Found Smappee Meter with dni: $dni and displayname: ${devices[dni]}"
 
            resp.data.actuators.each { returnObject ->
                dni = "Smappee Plug:${returnObject.id}"
                devices[dni] = getSmappeePlugDisplayName("Smappee Plug:${returnObject.name}")
                log.info "Found Smappee Plug with dni: $dni and displayname: ${devices[dni]}"
            }
        }
    }

    log.info "getSmappeeDevices()>> smappees: $devices"

    state.devices = devices
    return devices
}

//return String displayName of a device
def getSmappeeMeterDisplayName(returnObject) {
    if(returnObject) {
        return "${getNamePrefix()}:${returnObject}"
    } else {
        return "${getNamePrefix()}"
    }
}

def getNamePrefix() {
	return "Smappee Meter"
}

//return String displayName of a device
def getSmappeePlugDisplayName(returnObject) {
    if(returnObject) {
        return "${returnObject}"
    } else {
        return "Smappee Plug"
    }
}

//Section3: installed, updated, initialize methods ---------------------------------------------------------------------
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

    def devices = smappees.collect{ dni ->
        log.debug "initialized() dni:$dni"
        //Check if the discovered devicess are already initiated with corresponding device types.
        def d = getChildDevice(dni)  //this method inherits from SmartApp (data-management)
        if (!d){
            //addChildDevice() looks for correspondings device type specified by "childName" in "App Settings"
            if (dni.startsWith(getNamePrefix())) {
                d = addChildDevice(app.namespace, getChildNameMeter(), dni, null, ["label": state.devices[dni]])
                log.debug "created ${d.displayName} with dni $dni"
            } else if ("Smappee Plug"){
                d = addChildDevice(app.namespace, getChildNamePlug(), dni, null, ["label": state.devices[dni]])
                log.debug "created ${d.displayName} with dni $dni"
            } else {
                log.debug "cannot addChildDevice with dni: $dni"
            }
        }else{
            log.debug "found ${d.displayName} with dni $dni already exists"
        }
        return d
    }

    log.debug "created ${devices.size()} devices"

    def delete  // Delete any that are no longer in settings
    if(!devices) {
        log.debug "delete devices"
        delete = getAllChildDevices() //inherits from SmartApp (data-management)
    } else {
        delete = getChildDevices().findAll { !smappees.contains(it.deviceNetworkId) } //inherits from SmartApp (data-management)
    }
    log.debug "deleting ${delete.size()} devices"
    delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

    state.devices = [:] //reset Map to store thermostat data

    log.info "pollHandler()"
    pollHandler(null)

    //automatically update devices status every 30 mins
//    runEvery30Minutes("poll")
    runEvery5Minutes("poll")
}

def uninstalled() {
    log.info("Uninstalling, removing child devices...")
    removeChildDevices(getChildDevices())
}

private removeChildDevices(devices) {
    devices.each {
        deleteChildDevice(it.deviceNetworkId) // 'it' is default
    }
}

def pollHandler(child=null) {
    log.debug "pollHandler() is called at ${now()}"
    //poll data from 3rd party cloud
    if (pollChildren(child)){ //return state.thermostats >> e.g. [18902.10984.31990.31989:[data:[temperature:24.5, heatingSetpoint:5.0000, thermostatMode:off]]]

        //generate event for each (child) device type identified by different dni
        state.devices.each { stat ->

            def dni = stat.key
            def d = getChildDevice(dni)

            log.debug "pollHandler() found childDevice $d, will updating its status"

            if(d) {
                d.generateEvent(state.devices[dni].data)
            }
        }
    }
}

def poll() {
    def devices = getChildDevices()
    devices.each {pollHandler(it)}
}

def pollChildren(child=null)  {
    log.debug "pollChildren() is called at ${now()}"
    def result = false
    def deviceListParams = [
            uri: "https://farm1pub.smappee.net",
            path: "/dev/v1/servicelocation/${state.serviceLocationId}/recent",
            headers: ["Authorization": "Bearer ${state.authToken}"],
    ]

    try {
        httpGet(deviceListParams) { resp ->

            log.info "pollChildren(child) >> resp.status = ${resp.status}, resp.data = ${resp.data}"

            log.info "state.devices ${state.devices}"

            //TODO need to get update from Smappee Plug
            if(resp.status == 200 && resp.data) {
                def dni = "${getNamePrefix()}:${resp.data.serviceLocationId.toString()}"
                log.debug "dni: $dni"
                log.debug "resp.data.consumptions: ${resp.data.consumptions}"
                state.devices = resp.data.consumptions.inject([:]) { collector, meter ->
                    log.debug "collector $collector"
                    log.debug "meter $meter"
                    def data = [
                            power:meter['consumption'],
                            alwaysOn:meter['alwaysOn'],
                            solar:meter['solar']
                    ]
                    log.debug "Updating dni $dni with data = ${data}"

                    collector[dni] = [data:data]
                    return collector
                }

                /* update state.devices to associate current data with dni
                    state.devices [Smappee Meter:1015:[data:[power:1.065, alwaysOn:12.0, solar:0.0]]]
                 */
                log.info "state.devices ${state.devices}"
                result = true
            } else if (resp.status == 500 && resp.data.status.code == 14) {
                    log.debug "Refreshing your auth_token!"
                    refreshAuthToken()
            } else {
                    log.error "Authentication error, invalid authentication method, lack of credentials, etc."
            }
        }
    } catch (groovyx.net.http.HttpResponseException e) {
        log.error e
        refreshToken()
        return;
    }
}

def refreshToken() {

    def tokenParams = [
            grant_type: "refresh_token",
            code: state.sampleAccessCode,
            refresh_token: state.refreshToken,
            client_id: clientId,
            client_secret: clientSecret,
            redirect_uri: "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/receiveToken"
    ]

    def tokenUrl = "https://farm1pub.smappee.net/dev/v1/oauth2/token?${toQueryString(tokenParams)}"
    log.debug "url to get tokens: $tokenUrl"

    try {
        httpGet(uri:tokenUrl) { resp ->
            state.authToken = resp.data.access_token.toString()
            state.refreshToken = resp.data.refresh_token.toString()
            state.authTokenExpireIn = resp.data.expires_in.toString()
            log.debug "Response: ${resp.data}"
            log.debug "access_token: ${state.authToken }"
            log.debug "refresh_token: ${state.refreshToken}"
            log.debug "expires_in: ${state.authTokenExpireIn}"
        }
    } catch (groovyx.net.http.HttpResponseException e) {
        log.error "Error: ${e.statusCode}"
    }

    pollHandler(null)
}

def turnOn(plug) {
    log.debug "plug.deviceNetworkId ${plug.deviceNetworkId}"

    def eventParams = [
            uri: "https://farm1pub.smappee.net",
            path: "/dev/v1/servicelocation/${state.serviceLocationId}/actuator/${plug.deviceNetworkId-"Smappee Plug:"}/on",
            headers: ["Authorization": "Bearer ${state.authToken}"],
            body: "{}"
    ]

    log.debug eventParams

    httpPost(eventParams) { resp ->
        log.debug "resp: $resp"
        log.debug "plug ${plug.name} turned on"
    }

}

def turnOff(plug) {
    log.debug "plug.deviceNetworkId ${plug.deviceNetworkId}"

    def eventParams = [
            uri: "https://farm1pub.smappee.net",
            path: "/dev/v1/servicelocation/${state.serviceLocationId}/actuator/${plug.deviceNetworkId-"Smappee Plug:"}/off",
            headers: ["Authorization": "Bearer ${state.authToken}"],
            body: "{}"
    ]

    log.debug eventParams

    httpPost(eventParams) { resp ->
        log.debug "resp: $resp"
        log.debug "plug ${plug.name} turned off"
    }

}

def toQueryString(Map m) { return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&") }

def getChildNameMeter()               { return "Smappee Meter" }
def getChildNamePlug()               { return "Smappee Plug" }
def getServerUrl()               { return getApiServerUrl() }
def getClientSecret()               { return appSettings.clientSecret }
def getClientId()                { return appSettings.clientId }
