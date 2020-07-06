/**
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

    category: "Green Living",

    iconUrl: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX2Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX3Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    singleInstance: true,
    usesThirdPartyAuthentication: true,
    pausable: false
) {
    appSetting "clientId"
    appSetting "clientSecret"
    appSetting "serverUrl"
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

def getCurbAuthUrl() { return "https://energycurb.auth0.com" }

def getCurbLoginUrl() { return "${curbAuthUrl}/authorize" }

def getCurbTokenUrl() { return "${curbAuthUrl}/oauth/token" }

def getServerUrl() { return  appSettings.serverUrl ?: apiServerUrl }

def getCallbackUrl() { return "${serverUrl}/oauth/callback" }

def getBuildRedirectUrl() {
  return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${serverUrl}"
}

def installed() {
    log.debug "Installed with settings: ${settings}"
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    removeChildDevices(getChildDevices())

    initialize()
}

def initialize() {
    log.debug "Initializing"
    unschedule()

    def curbCircuits = getCurbCircuits()
    log.debug "Found devices: ${curbCircuits}"
    log.debug settings
    runEvery1Minute(getPowerData)
    if (settings.energyInterval=="Hour" || settings.energyInterval == "Half Hour" || settings.energyInterval == "Fifteen Minutes")
    {
      runEvery1Minute(getKwhData)
    } else {
      runEvery1Hour(getKwhData)
    }
}

def uninstalled() {
    log.debug "Uninstalling"
    removeChildDevices(getChildDevices())
}

def authPage() {

    if (!state.accessToken) {
        state.accessToken = createAccessToken()
    }

    if (state.authToken) {
    	getCurbLocations()
        return dynamicPage(name: "auth", title: "Login Successful", nextPage: "", install: true, uninstall: true) {
            section() {
                paragraph("Select your CURB Location")
                input(
                    name: "curbLocation",
                    type: "enum",
                    title: "CURB Location",
                    options: state.locations

                )
                input(
                  name: "energyInterval",
                  type: "enum",
                  title: "Energy Interval",
                  options: ["Billing Period", "Day", "Hour", "Half Hour", "Fifteen Minutes"],
                  defaultValue: "Hour"
                  )
            }
        }
    } else {
        return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall: false) {
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

        asynchttp_v1.post(handleTokenResponse, [uri: curbTokenUrl, body: tokenParams])
        success()
    } else {
        log.error "callback() failed oauthState != state.oauthInitState"
    }
}

def handleTokenResponse(resp, data){
	state.refreshToken = resp.json.refresh_token
    state.authToken = resp.json.access_token
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def updateChildDevice(dni, value) {
    try {
        def existingDevice = getChildDevice(dni)
        existingDevice?.handlePower(value)
    } catch (e) {
        log.error "Error updating device: ${e}"
    }
}

def createChildDevice(dni, label) {
    log.debug "Creating child device with DNI ${dni} and name ${label}"
    return addChildDevice("curb", "CURB Power Meter", dni, null, [name: "${dni}", label: "${label}"])
}

def getCurbCircuits() {
    getPowerData(true)
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
            resp.data.each {
                log.debug "Found location: ${it}"
                allLocations[it.id] = it.label
            }
            state.locations = allLocations
        }
    } catch (e) {
        log.error "something went wrong: ${e}"
    }
}

def getPowerData(create=false) {
  log.debug "Getting data at ${settings.curbLocation} with token: ${state.authToken}"
    def params = [
        uri: "https://app.energycurb.com",
        path: "/api/aggregate/${settings.curbLocation}/2m/s",
        headers: ["Authorization": "Bearer ${state.authToken}"],
        requestContentType: 'application/json'
    ]
    try {
    	httpGet(params) { resp ->
            processData(resp, null, create, false)
            return resp.data.circuits
        }
    } catch (e) {
    	refreshAuthToken()
        log.error "something went wrong: ${e}"
    }
}

def getKwhData() {
  log.debug "Getting kwh data at ${settings.curbLocation} with token: ${state.authToken}"
  def url = "/api/aggregate/${settings.curbLocation}/"

  if (settings.energyInterval == "Hour"){ url = url + "1h/m"}
  if (settings.energyInterval == "Billing Period"){ url = url + "billing/h"}
  if (settings.energyInterval == "Half Hour"){ url = url + "30m/m"}
  if (settings.energyInterval == "Day"){ url = url + "24h/h"}
  if (settings.energyInterval == "Fifteen Minutes"){ url = url + "15m/m"}
	log.debug "KWH FOR: ${url}"
    def params = [
        uri: "https://app.energycurb.com",
        path: url,
        headers: ["Authorization": "Bearer ${state.authToken}"],
        requestContentType: 'application/json'
    ]
    try {
    	httpGet(params) { resp ->
            processData(resp, null, false, true)
            return
        }
    } catch (e) {
    	refreshAuthToken()
        log.error "something went wrong: ${e}"
    }
}

def processData(resp, data, create=false, energy=false)
{
    log.debug "Processing usage data: ${resp.data}"
    if (!isOK(resp)) {

        refreshAuthToken()
        log.error "Usage Response Error: ${resp.getErrorMessage()}"
        return
    }
    def main = 0.0
    def production = 0.0
    def all = 0.0
    def hasProduction = false
    def hasMains = false
    if (resp.data) {
        resp.data.each {
        	def numValue = 0.0
        	if(energy){
            	numValue=it.kwhr.floatValue()
            } else {
            	numValue=it.avg
            }
        	all += numValue
            if (!it.main && !it.production && it.label != null && it.label != "") {
            	if (create) { createChildDevice("${it.id}", "${it.label}") }
                energy ?  getChildDevice("${it.id}")?.handleKwhBilling(numValue.floatValue()) : updateChildDevice("${it.id}", numValue)
            }
            if (it.grid) {
              hasMains = true
              main += numValue
            }
            if (it.production) {
              hasProduction = true
              production += numValue
            }
        }

        if (create) { createChildDevice("__NET__", "Net Grid Impact") }

        if (!hasMains) {
        	main = all
        }

        energy ? getChildDevice("__NET__")?.handleKwhBilling(main) : updateChildDevice("__NET__", main)
        if (hasProduction) {
          if (create) { createChildDevice("__PRODUCTION__", "Production") }
          if (create) { createChildDevice("__CONSUMPTION__", "Consumption") }
          energy ? getChildDevice("__PRODUCTION__")?.handleKwhBilling(production) : updateChildDevice("__PRODUCTION__", production)
          energy ? getChildDevice("__CONSUMPTION__")?.handleKwhBilling(main-production) : updateChildDevice("__CONSUMPTION__", main-production)
        }
    }
    if ( create && !energy){
    	getKwhData()
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
