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
    name: "Curb Bridge",
    namespace: "curb",
    author: "Curb",
    description: "App to get usage data from a Curb home energy monitor",
    category: "",
    iconUrl: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX2Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    iconX3Url: "http://energycurb.com/wp-content/uploads/2015/12/curb-web-logo.png",
    singleInstance: true
) {
    appSetting "clientId"
    appSetting "clientSecret"
}

preferences {
    page(
        name: "auth",
        title: "Curb",
        nextPage: "",
        content: "authPage",
        uninstall: true)
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
    removeChildDevices(getChildDevices())
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "Initializing"
    unschedule()
    refreshAuthToken()
    updateSelectedLocationId()
    getDevices()
    runEvery1Minute(getAllData)
}

def uninstalled() {
    log.debug "Uninstalling"
    removeChildDevices(getChildDevices())
}

def authPage() {
    if (!atomicState.accessToken) {
        atomicState.accessToken = createAccessToken()
    }

    if (atomicState.authToken) {
        return dynamicPage(name: "auth", title: "SuccessfullyConnected", nextPage: "", install: true, uninstall: true) {
            section() {
                paragraph("Select your Curb Location")
                input(
                    name: "curbLocation",
                    type: "enum",
                    title: "Curb Location",
                    options: atomicState.locationNames
                )
                input(
                    name: "energyInterval",
                    type: "enum",
                    title: "Energy Interval",
                    options: [
                    	"1m",
                        "15m",
                        "30m",
                        "1h",
                        "1d",
                        "1mo",
                        "billing"
                    ],
                    default: "30m"
                )
            }
        }
    } else {
        return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall: false) {
            section() {
                paragraph("Tap below to log in to the Curb service and authorize SmartThings access")
                href url: buildRedirectUrl, style: "embedded", required: true, title: "Curb", description: "Click to enter Curb Credentials"
            }
        }
    }
}

def oauthInitUrl() {
    atomicState.oauthInitState = UUID.randomUUID().toString()
    def oauthParams = [
        response_type: "code",
        scope: "offline_access",
        audience: "app.energycurb.com/api",
        client_id: appSettings.clientId,
        connection: "Users",
        state: atomicState.oauthInitState,
        redirect_uri: callbackUrl
    ]
    redirect(location: "${curbLoginUrl}?${toQueryString(oauthParams)}")
}

def callback() {
    //log.debug "callback()>> params: $params, params.code ${params.code}"
    def code = params.code
    def oauthState = params.state
    if (oauthState == atomicState.oauthInitState) {
        def tokenParams = [
            grant_type: "authorization_code",
            code: code,
            client_id: appSettings.clientId,
            client_secret: appSettings.clientSecret,
            redirect_uri: callbackUrl
        ]
        httpPostJson([uri: curbTokenUrl, body: tokenParams]) {
            resp ->
                //log.debug "response contentType: ${resp.contentType}"
                //log.debug("Got POST response: ${resp.data}")

                atomicState.refreshToken = resp.data.refresh_token
            atomicState.authToken = resp.data.access_token

            getCurbLocations()
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

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def getCurbLocations() {
    //log.debug("Requesting Curb location info");

    def params = [
        uri: "http://app.energycurb.com",
        path: "/api/locations",
        headers: ["Authorization": "Bearer ${atomicState.authToken}"]
    ]

    try {
        httpGet(params) {
            resp ->
                def locationNameList = []
            def locationLookup = []
            //log.debug(resp.data)
            resp.data.each {
                locationNameList.push(it.name)
                locationLookup.push(it)
            }
            atomicState.locationNames = locationNameList
            atomicState.locationLookup = locationLookup

            //log.debug("Location ID: ${atomicState.location}")
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

def updateSelectedLocationId() {
    atomicState.locationLookup.each {
        // log.debug "location match check: ${it.name} vs selected: ${settings.curbLocation}"
        if (it.name == settings.curbLocation) {
            atomicState.location = it.id
            return it.id
        }
    }
}

def updateChildDevice(dni, label, values) {
    try {
        def existingDevice = getChildDevice(dni)
        existingDevice.handleMeasurements(values)
    } catch (e) {
        log.error "Error creating or updating device: ${e}"
    }
}

def createChildDevice(dni, label) {
    return addChildDevice("curb", "Curb Power Meter", dni, null, [name: "${dni}", label: "${label}"])
}

def getDevices() {
    def params = [
        uri: "https://app.energycurb.com",
        path: "/api/latest/${atomicState.location}",
        headers: ["Authorization": "Bearer ${atomicState.authToken}"],
        requestContentType: 'application/json'
    ]
    asynchttp_v1.get(processDevices, params)
}

def getAllData() {

    def aggregateResolution = "h"
    if (settings.energyInterval == "30m" || settings.energyInterval == "15m" || settings.energyInterval == "1m") {
        aggregateResolution = "s"
    }
    if (settings.energyInterval == "1h") {
        aggregateResolution = "m"
    }

    def kwhrparams = [
        uri: "https://app.energycurb.com",
        path: "/api/aggregate/${atomicState.location}/${settings.energyInterval}/${aggregateResolution}",
        headers: ["Authorization": "Bearer ${atomicState.authToken}"],
        requestContentType: 'application/json'
    ]
    log.debug kwhrparams.path

    asynchttp_v1.get(processKwhr, kwhrparams)

    def latestparams = [
        uri: "https://app.energycurb.com",
        path: "/api/latest/${atomicState.location}",
        headers: ["Authorization": "Bearer ${atomicState.authToken}"],
        requestContentType: 'application/json'
    ]

    asynchttp_v1.get(processUsage, latestparams)

    def historicalparams = [
      uri: "https://app.energycurb.com",
      path: "/api/historical/${atomicState.location}/24h/5m",
        headers: ["Authorization": "Bearer ${atomicState.authToken}"],
        requestContentType: 'application/json'
      ]
    asynchttp_v1.get(processHistorical, historicalparams)

}

def processHistorical(resp, data)
{
    if (resp.hasError())
    {
        log.error "Historical Response Error: ${resp.getErrorMessage()}"
        return
    }

  def json = resp.json

    if(json)
    {
        //log.debug "Got Historical Data: ${json}"
        def total = null
        def prod = null

        json.each
        {
        	if(!it.main && !it.production){

            	updateChildDevice("${it.id}", it.label, it.values)

            }

            if(it.main)
            {
                it.values.sort{a,b -> a.t <=> b.t}
                if(!total)
                {
                    total = it
                }
                else
                {
                    if(it.values.size() != total.values.size())
                    {
                        log.debug("Size mismatch")
                    }
                    else
                    {
                        for(int i = 0; i < total.values.size(); ++i)
                        {
                            if(total.values[i].t != it.values[i].t)
                            {
                                log.debug("Time mismatch")
                            }
                            else
                            {
                                total.values[i].w = (total.values[i].w) + (it.values[i].w)
                            }
                        }
                    }
                }
            }
            if(it.production)
            {
                it.values.sort{a,b -> a.t <=> b.t}
                if(prod == null)
                {
                    prod = it
                }
                else
                {
                    if(it.values.size() != total.values.size())
                    {
                        log.debug("Size mismatch")
                    }
                    else
                    {
                        for(int i = 0; i < total.values.size(); ++i)
                        {
                            if(prod.values[i].t != it.values[i].t)
                            {
                                log.debug("Time mismatch")
                            }
                            else
                            {
                                prod.values[i].w = (prod.values[i].w) + (it.values[i].w)
                            }
                        }
                    }
                }
            }
        }

        updateChildDevice("__NET__", "Main", total.values)
        if(prod){
        	updateChildDevice("__PRODUCTION__", "Solar", prod.values)
        }
    }
}

def processUsage(resp, data) {
    if (resp.hasError()) {
        refreshAuthToken()
        log.error "Usage Response Error: ${resp.getErrorMessage()}"
        return
    }
    def json = resp.json
    if (json) {
        def hasProduction = false
        json.circuits.each {
            if (!it.main && !it.production) {
                updateChildDevice("${it.id}", it.label, it.w)
            }
            if (it.production) {
                hasProduction = true
            }
        }
        updateChildDevice("__NET__", "Main", json.net)
        if (hasProduction) {
            updateChildDevice("__PRODUCTION__", "Solar", json.production)
            updateChildDevice("__CONSUMPTION__", "Usage", json.consumption)
        }
    }
}

def processDevices(resp, data) {
    if (resp.hasError()) {
        log.error "Error setting up devices: ${resp.getErrorMessage()}"
        return
    }
    def json = resp.json
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

def processKwhr(resp, data) {
    if (resp.hasError()) {
        log.error "Kwhr Response Error: ${resp.getErrorMessage()}"
        return
    }

    def json = resp.json
    def mainkwh = 0.0
    def solarkwh = 0.0
    def usagekwh = 0.0
    if (json) {
        json.each {
            if (it.main) {
                mainkwh = mainkwh + it.kwhr
            }
            try {
                def existingDevice = getChildDevice(it.id)
                if (existingDevice) {
                    existingDevice.handleKwhr(it.kwhr)
                    //existingDevice.setAggregate(it)
                }
            } catch (e) {
                log.error "Error creating or updating device: ${e}"
            }
        }
        def existingDevice = getChildDevice("__NET__")
        if (existingDevice) {
            existingDevice.handleKwhr(mainkwh)
        }
    }
}

def toQueryString(Map m) {
    return m.collect {
        k, v -> "${k}=${URLEncoder.encode(v.toString())}"
    }.sort().join("&")
}

def refreshAuthToken() {
    if (!atomicState.refreshToken) {
        log.warn "Can not refresh OAuth token since there is no refreshToken stored"
    } else {
        def tokenParams = [
            grant_type: "refresh_token",
            client_id: appSettings.clientId,
            client_secret: appSettings.clientSecret,
            refresh_token: atomicState.refreshToken
        ]

        httpPostJson([uri: curbTokenUrl, body: tokenParams]) {
            resp ->
                atomicState.authToken = resp.data.access_token
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
            return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}"
        }
        def getApiEndpoint() {
            return "https://api.energycurb.com"
        }
