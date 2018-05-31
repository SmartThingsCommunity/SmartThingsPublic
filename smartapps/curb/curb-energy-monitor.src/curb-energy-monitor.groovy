/**
 *  Curb (Connect)
 *
 *  Copyright 2017 Curb
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

include 'asynchttp_v1'

definition(
    name: "CURB Energy Monitor",
    namespace: "curb",
    author: "Curb",
    description: "Gain insight into energy usage throughout your home.",
    category: "",
    iconUrl: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX2Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX3Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    singleInstance: true,
    usesThirdPartyAuthentication: true
) {
    appSetting "clientId"
    appSetting "clientSecret"
}

preferences {
    page(name: "auth", title: "Authorize with Curb", content: "authPage", uninstall: true)
}

mappings {
    path("/oauth/initialize") {
        action: [GET: "oauthInitUrl"]
    }
    path("/oauth/callback") {
        action: [GET: "callback"]
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "Initializing with token: ${state.authToken}"
    unschedule()
    def curbCircuits = getCurbCircuits()
    log.debug "Found devices: ${curbCircuits}"
    runEvery1Minute(getAllData)
}

def uninstalled() {
    log.debug "Uninstalling"
    removeChildDevices(getChildDevices())
}

def authPage() {
    if (!state.accessToken) {
        state.accessToken = createAccessToken()
        log.debug "Created access token"
    }

    if (state.authToken) {
        log.debug "Logged in, locations:${state.location}"
        // def curbLocations = getCurbLocations()
        return dynamicPage(name: "auth", title: "Login Successful", install: true, uninstall: true) {
            section() {
                paragraph("Select your CURB location")
                input(name: "curbLocation", type: "enum", title: "CURB Location", options: curbLocations)
            }
        }
    } else {
        return dynamicPage(name: "auth", title: "Login", uninstall: false) {
            section() {
                paragraph("Tap below to log in to the CURB service and authorize SmartThings access")
                href url: buildRedirectUrl, style: "embedded", required: true, title: "CURB", description: "Click to enter CURB Credentials"
            }
        }
    }
}

def oauthInitUrl() {
    log.debug "Initializing oauth"
    state.oauthInitState = UUID.randomUUID().toString()
    def oauthParams = [
        response_type: "code",
        scope: "offline_access",
        audience: "app.energycurb.com/api",
        client_id: appSettings.clientId,
        connection: "Users",
        state: state.oauthInitState,
        redirect_uri: callbackUrl
    ]
    redirect(location: "${curbLoginUrl}?${toQueryString(oauthParams)}")
}

def callback() {
    log.debug "Oauth callback: ${params}"
    def code = params.code
    def oauthState = params.state
    if (oauthState == state.oauthInitState) {
        def tokenParams = [
            grant_type: "authorization_code",
            code: code,
            client_id: appSettings.clientId,
            client_secret: appSettings.clientSecret,
            redirect_uri: callbackUrl
        ]
        httpPostJson([uri: curbTokenUrl, body: tokenParams]) {
            resp ->
                state.refreshToken = resp.data.refresh_token
              state.authToken = resp.data.access_token
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

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def getCurbLocations() {
    log.debug "Getting curb locations"
    def params = [
        uri: "http://app.energycurb.com",
        path: "/api/locations",
        headers: ["Authorization": "Bearer ${state.authToken}"]
    ]
    def allLocations = [:]
    try {
        httpGet(params) {
            resp ->
                def locationNameList = []
            def locationLookup = []
            resp.data.each {
                log.debug "Found location: ${it}"
                allLocations[it.id] = it.label
                locationNameList.push(it.name)
                locationLookup.push(it)
            }
            state.locationNames = locationNameList
            state.locationLookup = locationLookup
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
    log.debug "Found locations: ${allLocations}"
    allLocations
}

def getCurbCircuits() {
	updateSelectedLocationId()
	log.debug "Getting curb circuits at ${state.location} with token: ${state.authToken}"
    def params = [
        uri: "https://app.energycurb.com",
        path: "/api/latest/${state.location}",
        headers: ["Authorization": "Bearer ${state.authToken}"],
        requestContentType: 'application/json'
    ]
    try {
    	httpGet(params) { resp ->
            processDevices(resp, null) //processDevices is usually called by asynchttp, hence the extra null
            return resp.data.circuits
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}
def updateSelectedLocationId() {
    state.location = curbLocations.keySet().collect()[0]
}

def updateChildDevice(dni, label, value) {
    try {
        def existingDevice = getChildDevice(dni)
        log.info "Found device: ${existingDevice}"
        existingDevice?.handlePower(value)
    } catch (e) {
        log.error "Error creating or updating device: ${e}"
    }
}

def createChildDevice(dni, label) {
    log.debug "Creating child device with DNI ${dni} and name ${label}"
    return addChildDevice("curb", "CURB Power Meter", dni, null, [name: "${dni}", label: "${label}"])
}

def getDevices() {
    def params = [
        uri: "https://app.energycurb.com",
        path: "/api/latest/${state.location}",
        headers: ["Authorization": "Bearer ${state.authToken}"],
        requestContentType: 'application/json'
    ]
    asynchttp_v1.get(processDevices, params)
}

def getAllData() {
    def billingParams = [
        uri: "https://app.energycurb.com",
        path: "/api/aggregate/${state.location}/billing/h",
        headers: ["Authorization": "Bearer ${state.authToken}"],
        requestContentType: 'application/json'
    ]
    asynchttp_v1.get(processKwh, billingParams)
}

def processUsage(resp, data) {
    log.debug "Processing usage data: ${resp.data}"
    if (!isOK(resp)) {
        refreshAuthToken()
        log.error "Usage Response Error: ${resp.getErrorMessage()}"
        return
    }
    def json = new groovy.json.JsonSlurper().parseText(resp.data)
    def main = 0.0
    def production = 0.0
    if (json) {
        def hasProduction = false
        json.each {
            log.debug "Processing usage for: ${it}"
            if (!it.main && !it.production) {
                updateChildDevice("${it.id}", it.label, it.avg)
            }
            if (it.production) {
                hasProduction = true
            }
            if (it.main) {
              main += it.avg
            }
            if (it.production) {
              production += it.avg
            }
        }
        updateChildDevice("__NET__", "Main", main)
        if (hasProduction) {
            updateChildDevice("__PRODUCTION__", "Solar", production)
            updateChildDevice("__CONSUMPTION__", "Usage", main-production)
        }
    }
}

def processDevices(resp, data) {
    if (!isOK(resp)) {
        log.error "Error setting up devices: ${resp.getErrorMessage()}"
        return
    }
    def json = resp.data
    if (json) {
        def hasProduction = false
        json.circuits.each {
            if (!it.main && !it.production) {
                device = createChildDevice("${it.id}", "${it.label}")
            }
            if (it.production) {
                hasProduction = true
            }
        }
        createChildDevice("__NET__", "Main")
        if (hasProduction) {
            createChildDevice("__PRODUCTION__", "Solar")
            createChildDevice("__CONSUMPTION__", "Usage")
        }
    }
}

def processKwh(resp, data) {
	log.debug "Processing billing data: ${resp.data}"
    if (!isOK(resp)) {
        refreshAuthToken()
        log.error "Usage Response Error: ${resp.getErrorMessage()}"
        return
    }
    def json = resp.json
    def main = 0.0
    def production = 0.0
    def existingDevice = null
    if (json) {
        def hasProduction = false
        json.each {
            log.debug "Updating billing for: ${it}"
            if (!it.main && !it.production) {
            	getChildDevice("${it.id}").handlePower(it.avg)
                getChildDevice("${it.id}").handleKwhBilling(it.kwhr)
            }
            if (it.production) {
                hasProduction = true
            }
            if (it.main) {
              main += it.kwhr
            }
            if (it.production) {
              production += it.kwhr
            }
        }
        getChildDevice("__NET__").handleKwhBilling(main)
        if (hasProduction) {
            getChildDevice("__SOLAR__").handleKwhBilling(production)
            getChildDevice("__USAGE__").handleKwhBilling(main-production)
        }
    }
}

def toQueryString(Map m) {
    return m.collect {
        k, v -> "${k}=${URLEncoder.encode(v.toString())}"
    }.sort().join("&")
}

def refreshAuthToken() {
    log.debug "Refreshing auth token"
    if (!state.refreshToken) {
        log.warn "Can not refresh OAuth token since there is no refreshToken stored"
    } else {
        def tokenParams = [
            grant_type: "refresh_token",
            client_id: appSettings.clientId,
            client_secret: appSettings.clientSecret,
            refresh_token: state.refreshToken
        ]

        httpPostJson([uri: curbTokenUrl, body: tokenParams]) {
            resp ->
                state.authToken = resp.data.access_token
                log.debug "Got authToken: ${state.authToken}"
        }
    }
}

//THIS DEFINES THE SCREEN AFTER AUTHORIZATION:

def success() {
  def message = """
        <p>Your Curb account is now connected to SmartThings!</p>
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
      <title>Curb & SmartThings connection</title>
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
          <img src="http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png" alt="curb icon" />
          <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
          <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" /> ${message}
      </div>
  </body>

  </html>
    """

  render contentType: 'text/html', data: html
}

def isOK(response) {
	response.status in [200, 201]
}

def getCurbAuthUrl() {
    return "https://energycurb.auth0.com"
}
def getCurbLoginUrl() {
    return "${curbAuthUrl}/authorize"
}
def getCurbTokenUrl() {
    return "${curbAuthUrl}/oauth/token"
}
def getServerUrl() {
    return "https://graph.api.smartthings.com"
}
def getShardUrl() {
    return getApiServerUrl()
}
def getCallbackUrl() {
    return "https://graph.api.smartthings.com/oauth/callback"
}
def getBuildRedirectUrl() {
    return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${shardUrl}"
}
def getApiEndpoint() {
    return "https://api.energycurb.com"
}
