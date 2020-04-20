/**
 *  Modern Forms
 *
 *  Copyright 2018 Eric Stef
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
    name: "Modern Forms",
    namespace: "modernforms",
    author: "Eric Stef",
    description: "Modern Forms SmartThings integration ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/windermier-icons/Fan_app_icon_IOS_108.png",
    iconX2Url: "https://s3.amazonaws.com/windermier-icons/Fan_app_icon_IOS_512.png",
    iconX3Url: "https://s3.amazonaws.com/windermier-icons/Fan_app_icon_IOS_512.png")


preferences {
    page(name: "auth", title: "modern forms", nextPage:"", content:"authPage", uninstall: true, install:true)
}

mappings {
    path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
    path("/oauth/callback") {action: [GET: "callback"]}
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    initialize()
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
        description = "Click to enter Modern Forms credentials"
    }

    def redirectUrl = buildRedirectUrl
    log.debug "RedirectUrl = ${redirectUrl}"
    // get rid of next button until the user is actually auth'd
    if (!oauthTokenProvided) {
        return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall:uninstallAllowed) {
            section() {
                paragraph "Tap below to log in to the Modern Forms service and authorize SmartThings access"
                href url:redirectUrl, style:"embedded", required:true, title:"Modern Forms", description:description
            }
        }
    } else {
        def devices = getMyDevices()
        
        def options = [:]
        devices.each { key, value ->
            options[key] = value.deviceName
        }
        
        return dynamicPage(name: "auth", title: "Select Your Fans", uninstall: true) {
            section("") {
                paragraph "Tap below to see the list of fans available in your Modern Forms account and select the ones you want to connect to SmartThings."
                input(name: "selectedDevices", title:"Select Your Fans", type: "enum", required:false, multiple:true, description: "Tap to choose", options: options)
            }
        }
    }
}

def oauthInitUrl() {
    //log.debug "oauthInitUrl with callback: ${callbackUrl}"

    atomicState.oauthInitState = UUID.randomUUID().toString()

    def oauthParams = [
            response_type: "code",
            client_id: "3ad8vh2jtafpjhccoatce0rs7e",
            state: atomicState.oauthInitState,
            redirect_uri: callbackUrl
    ]

    redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
}

def callback() {
    def code = params.code
    def oauthState = params.state

    if (oauthState == atomicState.oauthInitState) {
        def tokenParams = [
            grant_type: "authorization_code",
            code: code,
            client_id: "3ad8vh2jtafpjhccoatce0rs7e",
            redirect_uri: callbackUrl
        ]
        
        def postBody =  "grant_type=authorization_code&" +
            "code=${code}&"+
            "clientId=3ad8vh2jtafpjhccoatce0rs7e&" +
            "redirect_uri=${callbackUrl}"
                
        def basicCredentials = "3ad8vh2jtafpjhccoatce0rs7e:h8086df68uhgq3o6remt81nopemmkqopg9h8utb4dhqdrjlh7b0"
        def encodedCredentials = basicCredentials.encodeAsBase64().toString()

        def tokenUrl = "https://modernforms.auth.us-east-1.amazoncognito.com/oauth2/token"

        httpPost(uri: tokenUrl, body: postBody, headers: ["Content-Type": "application/x-www-form-urlencoded", "Authorization": "Basic ${encodedCredentials}" ]) { resp ->
            atomicState.refreshToken = resp.data.refresh_token
            atomicState.authToken = resp.data.access_token
            atomicState.idToken = resp.data.id_token
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
        <p>Your Modern Forms account is now connected to SmartThings!</p>
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
                <title>Modern Forms & SmartThings connection</title>
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
                <img src="https://s3.amazonaws.com/windermier-icons/Fan_app_icon_IOS_108.png" alt="ecobee icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                ${message}
            </div>
        </body>
    </html>
    """

    render contentType: 'text/html', data: html
}

def initialize() {
    // TODO: subscribe to attributes, devices, locations, etc.
    log.debug "initialize"  
        
    def devices = state.devices;
    //def mapMyDevices = devices.collectEntries{[it.clientId, it.deviceName]}
    
    settings.selectedDevices.each {clientId ->
        def deviceName = devices[clientId].deviceName
        def existingDevice = getChildDevice(clientId)
        if(!existingDevice) {
            def childDevice = addChildDevice("modern-forms-fan", "Modern Forms Fan", clientId, null, [completedSetup: true, name: clientId, label: deviceName]);
        }
    }
    
    log.debug '----delete----'
    def delete = getChildDevices().findAll { !settings.selectedDevices.contains(it.deviceNetworkId) }
    removeChildDevices(delete)
    log.debug '----delete----'
    
    pollHandler() //first time polling data data from thermostat

    //automatically update devices status every 5 mins
    runEvery5Minutes("poll")
}

def getMyDevices() {
    log.debug 'getMyDevices()'

    def bodyParams = [
        id_token: atomicState.idToken
    ]

    def deviceListParams = [
        uri: "https://7wekx875ki.execute-api.us-east-1.amazonaws.com",
        path: "/prod/smart-things/devices",
        headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
        query: [format: 'json', body: bodyParams]
    ]
    
    log.debug '----getMyDevices----'
    def stats = [:]
    try {
        httpGet(deviceListParams) { resp ->
            if (resp.status == 200 || resp.statusCode == 200) {
                resp.getData().each { stat -> 
                    def dni = stat.clientId
                    log.debug dni
                    stats[dni] = stat
                }
            }
        }
    } catch(groovyx.net.http.HttpResponseException e) {
        log.error "error: " + e
    }
    
    log.debug '----getMyDevices----'
        
    state.devices = stats
    return stats
}

def pollHandler() {
    log.debug "pollHandler()"
    pollChildren(null)
    
    //generate event for each child
}

def pollChildren(child = null) {
    log.debug 'pollChildren()'
    poll();
}

def pollBecause(child) {
    def device = getChildDevice(child.device.deviceNetworkId)
    pollChild(device)
}

def pollChild(child) {
    log.debug 'pollChild()'
    
    def bodyParams = [
        clientId: child.device.deviceNetworkId
    ]

    def deviceListParams = [
        uri: "https://7wekx875ki.execute-api.us-east-1.amazonaws.com",
        path: "/prod/smart-things/shadow",
        headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
        query: [format: 'json', body: bodyParams]
    ]
    
    log.debug '----getDeviceShadow----'
    try {
        httpGet(deviceListParams) { resp ->
            if (resp.status == 200 || resp.statusCode == 200) {
                log.debug resp.getData();
                child.generateEvent(resp.getData());
            }
        }
    } catch(groovyx.net.http.HttpResponseException e) {
        log.error "error: " + e
    }
    log.debug '----getDeviceShadow----'
}

void poll() {
    getChildDevices().each { child -> 
        pollChild(child)
    }
}

def lambda(clientId, command, value) {
    def params = [
        uri: "https://7wekx875ki.execute-api.us-east-1.amazonaws.com/prod/smart-things",
        body: [
            clientId: clientId,
            command: command,
            value: value
        ]
    ]
    
    httpPostJson(params) { resp ->
        log.debug resp.getData()
    }
}

private removeChildDevices(delete) {
    log.debug "deleting ${delete.size()} bulbs"
    log.debug "deleting ${delete}"
    delete.each {
        deleteChildDevice(it.device.deviceNetworkId)
    }
}

def toJson(Map m) {
    return groovy.json.JsonOutput.toJson(m)
}

def toQueryString(Map m) {
    return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getCallbackUrl()         { return "https://graph.api.smartthings.com/oauth/callback" }
def getShardUrl()            { return getApiServerUrl() }
def getBuildRedirectUrl()    { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }
def getApiEndpoint()         { return "https://modernforms.auth.us-east-1.amazoncognito.com/oauth2" }