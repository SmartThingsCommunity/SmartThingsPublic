/**
 *  Guardzilla
 *
 *  Copyright 2016 Robert Hamm
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
    name: "Guardzilla",
    namespace: "Guardzilla",
    author: "Robert Hamm",
    description: "Arm and Disarm your Guardzillas using Smart Things Home and Away.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    singleInstance: true)
    
mappings {
  path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
  path("/oauth/callback") {action: [GET: "callback"]}
}

preferences {
  page(name: "auth", title: "Authentication", content: "authPage", uninstall: true, install: true)
  page(name: "loggedIn", title: "Guardzilla", content: "loggedInContent", uninstall: true)
}

def authPage() {
    // Check to see if SmartApp has its own access token and create one if not.
    if(!state.accessToken) {
        // the createAccessToken() method will store the access token in state.accessToken
        createAccessToken()
    }

    def redirectUrl = "https://graph.api.smartthings.com/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${getApiServerUrl()}"
    // Check to see if SmartThings already has an access token from the third-party service.
    if(!state.authToken) {
        return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall: false) {
            section() {
                paragraph "Tap below to log in to Guardzilla and authorize SmartThings access."
                href url: redirectUrl, style: "embedded", required: true, title: "Guardzilla", description: "Click to login"
            }
        }
    } else {
        // SmartThings has the token show the logged in screen
        log.debug("logged in")
        loggedInContent()
    }
}
def loggedInContent(){
	 return dynamicPage(name: "loggedIn", title: "Guardzilla", nextPage: "", uninstall: true) {
            section() {
                paragraph "Your Guardzillas will automatically Arm and Disarm when you are Home or Away."                
            }
        }
}
def oauthInitUrl() {

    // Generate a random ID to use as a our state value. This value will be used to verify the response we get back from the third-party service.
    state.oauthInitState = UUID.randomUUID().toString()

    def oauthParams = [
        response_type: "code",
        scope: "smartRead,smartWrite",
        client_id: appSettings.clientId,
        client_secret: appSettings.clientSecret,
        state: state.oauthInitState,
        redirect_uri: "https://graph.api.smartthings.com/oauth/callback"
    ]
	//redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
    redirect(location: "https://oauth.guardzilla.com/Authorize?${toQueryString(oauthParams)}&AppType=3")
}

// The toQueryString implementation simply gathers everything in the passed in map and converts them to a string joined with the "&" character.
String toQueryString(Map m) {
        return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def callback() {
    log.debug "callback()>> params: $params, params.code ${params.code}"

    def code = params.code
    def oauthState = params.state

    // Validate the response from the third party by making sure oauthState == state.oauthInitState as expected
    if (oauthState == state.oauthInitState){
        def tokenParams = [
            grant_type: "authorization_code",
            code      : code,
            client_id : appSettings.clientId,
            client_secret: appSettings.clientSecret,
            redirect_uri: "https://graph.api.smartthings.com/oauth/callback"
        ]

        // This URL will be defined by the third party in their API documentation
        def tokenUrl = "https://oauth.guardzilla.com/api/token/token?${toQueryString(tokenParams)}"

        httpPost(uri: tokenUrl) { resp ->
        	log.debug "It Worked! Access Token is = " + resp.data.access_token
            //state.refreshToken = resp.data.refresh_token
            state.authToken = resp.data.access_token
        }

        if (state.authToken) {
            // call some method that will render the successfully connected message
            success()
        } else {
            // gracefully handle failures
            fail()
        }

    } else {
        log.error "callback() failed. Validation of state did not match. oauthState != state.oauthInitState"
    }
}

// Example success method
def success() {
        def message = """
                <p>Your account is now connected to SmartThings!</p>
                <p>Click 'Done' to finish setup.</p>
        """
        displayMessageAsHtml(message)
}

// Example fail method
def fail() {
    def message = """
        <p>There was an error connecting your account with SmartThings</p>
        <p>Please try again.</p>
    """
    displayMessageAsHtml(message)
}

def displayMessageAsHtml(message) {
    try{
        def html = """
            <!DOCTYPE html>
            <html>
                <head>
                </head>
                <body>
                    <div>
                        ${message}
                    </div>
                </body>
            </html>
        """
        render contentType: 'text/html', data: html
    } catch (e) {
        log.error "something went wrong: $e"
    }
}

def installed() {
	log.debug "Installed!"

	initialize()
}

def updated() {
	log.debug "Updated!"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    log.debug "Subscribing to mode changes..."
    subscribe(location, "mode", modeChangeHandler)
    //subscribe(people, "presence", presence)    
    //subscribe(switches, "switch.on", switchOnHandler)
    //subscribe(switches, "switch.off", switchOffHandler)
}

def modeChangeHandler(evt) {
    if (evt.value == "Home")
    {
    // Do stuff you want when mode changes to "Home"
    log.debug "modeChangeHandler called: Home - authToken is " + state.authToken
        def params = [
            //uri: "http://33b257e5.ngrok.io",
            uri: "https://iot.guardzilla.com",
            path: "/SmartThings/Arm",
            body: [
                    accessToken: state.authToken
                ]
        ]

        try {
            httpPostJson(params) { resp ->
                resp.headers.each {
                   log.debug "${it.name} : ${it.value}"
                }
                log.debug "response contentType: ${resp.contentType}"
                log.debug "response data: ${resp.data}"
            }
        } catch (e) {
            log.error "something went wrong: $e"
        }
    }
    if (evt.value == "Away")
    {
    //do stuff
    // Do stuff you want when mode changes to "Home"
    log.debug "modeChangeHandler called: Away - authToken is " + state.authToken
        def params = [
            //uri: "http://33b257e5.ngrok.io",
            uri: "https://iot.guardzilla.com",
            path: "/SmartThings/Disarm",
            body: [
                    accessToken: state.authToken
                ]
        ]

        try {
            httpPostJson(params) { resp ->
                resp.headers.each {
                   log.debug "${it.name} : ${it.value}"
                }
                log.debug "response contentType: ${resp.contentType}"
                log.debug "response data: ${resp.data}"
            }
        } catch (e) {
            log.error "something went wrong: $e"
        }
    }
}
