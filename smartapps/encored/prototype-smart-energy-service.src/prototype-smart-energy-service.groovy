/**
 *  ProtoType Smart Energy Service
 *
 *  Copyright 2015 hyeon seok yang
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
    name: "ProtoType Smart Energy Service",
    namespace: "Encored",
    author: "hyeon seok yang",
    description: "A prototype to see the proposed ux/ui procedure works without any shortages.\r\n",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)
    {
    	appSetting "clientId"
        appSetting "clientSecret"
        appSetting "callback"
    }


preferences {
	page(name: "checkAccessToken")
}

cards {
    card(name: "Encored Energy Service", type: "html", action: "getHtml", whitelist: whitelist()) {}
}

def whitelist() {
	["code.jquery.com", "ajax.googleapis.com", "code.highcharts.com", "enertalk-card.encoredtech.com"]
}

mappings {
	path("/requestCode") { action: [ GET: "requestCode" ] }
	path("/receiveToken") { action: [ GET: "receiveToken"] }
    path("/getHtml") { action: [GET: "getHtml"] }
    path("/consoleLog") {
        action: [POST: "consoleLog"]
    }
    
    path("/getInitialData") {
        action: [GET: "getInitialData"]
    }
}


def checkAccessToken() {
	log.debug "In checkAccessToken"
	log.debug "Encored Access Token : ${atomicState.encoredAccessToken}"
	if (!atomicState.encoredAccessToken) { /*check if Encored access token exist.*/
    	
        log.debug "Getting Encored Access Token"
 		log.debug "SmartThings Access Token : ${state.accessToken}"
        
        if (!state.accessToken) { /*if smartThings' access token does not exitst*/
        	log.debug "Getting SmartThings Access Token"
            createAccessToken() /*request and get access token from smartThings*/
        }

		def redirectUrl = buildRedirectUrl("requestCode") /* build a redirect url */
        
        log.debug "Application Redirect URL : ${redirectUrl}"
        
        /*Start the process of getting Encored Access Token.*/
        return dynamicPage(name: "checkAccessToken", title: "Encored EnerTalk", nextPage:null, uninstall: true, install:false) {
            section{
            	paragraph "Tab velow to sign in or sign up to Encored EnerTalk smart energy service and authorize SmartThings access."
                href(title: "EnerTalk",
                     description: "Click to proceed authorization",
                     required: true,
                     style:"embedded",
                     url: redirectUrl)
            }
        }
    } else {
    	//not implemented yet.
        def meteringDays =[
     		"1st day of the month", 
            "2nd day of the month", 
            "3rd day of the month",
            "4th day of the month",
            "5th day of the month",
            "6th day of the month",
            "7th day of the month",
            "8th day of the month",
            "9th day of the month",
            "10th day of the month",
            "11th day of the month",
            "12th day of the month",
            "13th day of the month",
            "14th day of the month",
            "15th day of the month",
            "16th day of the month",
            "17th day of the month",
            "18th day of the month",
            "19th day of the month",
            "20st day of the month",
            "21st day of the month",
            "22nd day of the month",
            "23rd day of the month",
            "24th day of the month",
            "25th day of the month",
            "26th day of the month",
            "Rest of the month"
            ],
       	displayUnits = ["WON(₩)", "kWh"],
        contractTypes = ["Low voltage", "High voltage"]
        return dynamicPage(name:"checkAccessToken",install:true, uninstall : true) {
        	section(title:"User & Notifications") {
            	input(type: "boolean", name: "notification", title: "Send push notification", required: false, multiple: false)
                input(type: "enum", name: "displayUnit", title: "Display Unit", required: true, multiple: false, options: displayUnits)
            	input(type: "enum", name: "meteringDate", title: "Metering Date", required: true, multiple: false, options: meteringDays)
                input(type: "enum", name: "contractType", title: "Contract Type", required: true, multiple: false, options: contractTypes)
            }
            
        }
    }
}

def requestCode(){
	
	/* Make a parameter to request Encored for a OAuth code. */
    def oauthParams = 
    [
		response_type: "code",
		scope: "remote",
		client_id: "${appSettings.clientId}",
        app_version: "web",
		redirect_uri: buildRedirectUrl("receiveToken")
	]
    
    log.debug "oauthParams : $oauthParams"
    log.debug "https://enertalk-auth.encoredtech.com/authorization?${toQueryString(oauthParams)}"

	redirect location: "https://enertalk-auth.encoredtech.com/authorization?${toQueryString(oauthParams)}"
}

def receiveToken(){
	log.debug "receiveCode()"
	
    def authorization = "Basic " + "${appSettings.clientId}:${appSettings.clientSecret}".bytes.encodeBase64()
    def uri = "https://enertalk-auth.encoredtech.com/token"
    def header = [Authorization: authorization, contentType: "application/json"]
    def body = [grant_type: "authorization_code", code: params.code]
	
    def encoredTokenParams = makeParams(uri, header, body)
	log.debug encoredTokenParams
    
    def encoredTokens = getHttpPostJson(encoredTokenParams)
    log.debug encoredTokens
	
    if (encoredTokens) {
    	log.debug "in success"
		atomicState.encoredRefreshToken = encoredTokens.refresh_token
		atomicState.encoredAccessToken = encoredTokens.access_token
    
    	log.debug "Encored Access Token : ${atomicState.encoredAccessToken}"
    	log.debug "Encored Refresh Token : ${atomicState.encoredRefreshToken}"
        success()
	} else {
    	log.debug "in fail"
    	fail()
    }
    
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
		log.debug "initialize initialize"
	// TODO: subscribe to attributes, devices, locations, etc.
    def eParams = [
            uri: "http://enertalk-auth.encoredtech.com/uuid",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"]
		]
	log.debug "sending http get request."
    
    def uuid
    try {
        httpGet(eParams) { resp ->
            log.debug "${resp.data}"
            log.debug "${resp.status}"
            if(resp.status == 200)
            {
                log.debug "poll"
                uuid = resp.data.uuid	
            }
    	}
    } catch (e) {
    	log.debug "$e"
        log.debug "Refreshing your auth_token!"
        refreshAuthToken()
        eParams.headers = [Authorization: "Bearer ${atomicState.encoredAccessToken}"]
        httpGet(eParams) { resp ->
            uuid = resp.data.uuid
        }
    }
    
    atomicState.uuid = uuid
    log.debug "get uuid: ${atomicState.uuid}"
   
    setSummary()    
}

def setSummary() {
    // Use sendEvent with a SOLUTION_SUMMARY eventType - this
    // is what drives the information on the solution module dashboard.
    // Since this summary just displays the switch configured, we need
    // to ensure it's called every time the app is installed or configured
    // so that the summary data is correct.
    log.debug "in setSummary"
    def text = "device detail is configured"
    sendEvent(linkText:count.toString(), descriptionText: app.label,
              eventType:"SOLUTION_SUMMARY",
              name: "summary",
              value: text,
              data: [["icon":"indicator-dot-gray","iconColor":"#878787","value":text]],
              displayed: false)
}

// TODO: implement event handlers

private getHttpPostJson(param) {
	def jsonMap = null
   try {
       httpPostJson(param) { resp ->
           jsonMap = resp.data
       }
    } catch(groovyx.net.http.HttpResponseException e) {
    	log.error "HTTP Post Error : ${e}"
    }
    
    return jsonMap
}

private makeParams(uri, header, body=[]) {
	return [
    	uri : uri,
        headers : header,
        body : body
    ]
}

private buildRedirectUrl(mappingPath) {
	log.debug "In buildRedirectUrl : /${mappingPath}"
    return "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/${mappingPath}"
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private success() {
	log.debug "in success2"
	def message = """
		<p>Your Encored Account is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	"""
	connectionStatus(message)
}

private fail() {
    def message = """
        <p>The connection could not be established!</p>
        <p>Click 'Done' to return to the menu.</p>
    """
    connectionStatus(message)
}

private connectionStatus(message) {
    def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width height=device-height">
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
            body {
            	margin: 0;
            	width : 100%;
            }
            .container {
                width: 100%;
            
                /*background: #eee;*/
                text-align: center;
            }
            img {
                vertical-align: middle;
                width: 30%;
            }
            p {
                font-size: 2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                
                margin-bottom: 0;
            }
        /*
            p:last-child {
                margin-top: 0px;
            }
        */
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
  
        </head>
        <body>
            <div class="container">
                <img src="https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" alt="Harmony icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                ${message}
                
            </div>
        </body>
        </html>
	"""
	render contentType: 'text/html', data: html
}

private refreshAuthToken() {
	log.debug "refreshing auth token"
	log.debug "atomicState $atomicState"
	if(!atomicState.encoredRefreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {
		def au = "Basic " + "${appSettings.clientId}:${appSettings.clientSecret}".bytes.encodeBase64()
		def refreshParams = [
				method: 'POST',
				uri   : "http://enertalk-auth.encoredtech.com",
                headers : [Authorization: au],
				path  : "/token",
				body : [grant_type: 'refresh_token', refresh_token: "${atomicState.encoredRefreshToken}"],
		]
        
        def token
        httpPost(refreshParams) { resp ->
            token = resp.data
        }
        atomicState.encoredAccessToken = token.access_token
    }
}

def consoleLog() {
    // If this endpoint were set up to work with a GET request,
    // we would get the params using the `params` object:
    // log.debug "console log: ${params.str}"

    // PUT/POST parameters come in on the request body as JSON.
    log.debug "console log: ${request.JSON.str}"
}

def getHtml() {
	log.debug "getHtml in here"
    renderHTML() {
        head {
        """
        <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width, height=device-height">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <script src="http://code.highcharts.com/highcharts.js"></script>
        <script src="http://enertalk-card.encoredtech.com/sdk.js"></script>
        
        <link rel="stylesheet" href="${buildResourceUrl('css/app.css')}" type="text/css" media="screen"/>
        <script type="text/javascript" src="${buildResourceUrl('javascript/app.js')}"></script>
        """
        }
        body {
        """
         <div id="real-time">
    <div id="my-card">
    </div>
    
    <div class="first-row">
      <div class="scope1">
        <div class="content" id="content1">
          <span class="words" align="center">
            <br/>
            This Month
            <br/><br/><br/>
            won 25,960
            <br/><br/>
            342kwh
          </span>
        </div>
      </div>

      <div class="scope1">
        <div class="content" id="content2">
          <span class="words" align="center">
            <br/>
            Last Month
            <br/><br/><br/>
            won 5,270
            <br/><br/>
            55.54kwh
          </span>
        </div>
      </div>  
    </div>

    <div class="second-row">
      <div class="scope2">
        <div class="content" id="content3">
          <span class="words" align="center">
            Rates
            <br /><br />
            Tier 3
          </span>
        </div>
      </div>

      <div class="scope2">
        <div class="content" id="content4">
          <span class="words" align="center">
            Ranking
            <br /><br />
            32nd out of 100 homes
          </span>
        </div>
      </div>

      <div class="scope2">
        <div class="content" id="content5">
          <span class="words" align="center">
            Plan
            <br /><br />
            won 40,000 <br />
            won 25,960 <br />
            22 days
          </span>
        </div>
      </div> 
    </div>
    
    <div class="third-row">
      <div class="scope2">
        <div class="content" id="content6">
          <span class="words" align="center">
            Standby
            <br /><br />
            13 % <br />
            45.2 W
          </span>
        </div>
      </div>
    </div>
    
  </div>

  <div id="this-month">
    <div class="card-header">
      <p class="title">This Month</p>
      <button class="show" id="show">X</button>
    </div>
    <div id="my-card2"></div>
    <div id="my-card3"></div>
  </div>
  
  <div id="last-month">
    <div class="card-header">
      <p class="title">Last Month</p>
      <button class="show" id="show2">X</button>
    </div>
    <div id="my-card4"></div>
  </div>
  
  <div id="progressive-step">
    <div class="card-header">
      <p class="title">Progressive step</p>
      <button class="show" id="show3">X</button>
    </div>
    <div id="my-card5"></div>
  </div>
  
  <div id="ranking">
    <div class="card-header">
      <p class="title">Ranking</p>
      <button class="show" id="show4">X</button>
    </div>
    <div id="my-card6"></div>
  </div>
    
  <div id="plan">
    <div class="card-header">
      <p class="title">Plan</p>
      <button class="show" id="show5">X</button>
    </div>
    <div id="my-card7"></div>
    <div id="my-card9"></div>
  </div>
  
  <div id="standby">
    <div class="card-header">
      <p class="title">Standby</p>
      <button class="show" id="show6">X</button>
    </div>
    <div id="my-card8"></div>
  </div>

        """
        }
    }
}

def getInitialData() {
	log.debug "in getInitialData"
    
    def eParams = [
            uri: "http://enertalk-auth.encoredtech.com/verify",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"]
		]
	log.debug "sending http get request."
    
    def token
    
    try {
        httpGet(eParams) { resp ->
            log.debug "${resp.data}"
            log.debug "${resp.status}"
            if(resp.status == 200)
            {
                log.debug "token still usable"	
            }
    	}
    } catch (e) {
    	log.debug "$e"
        log.debug "Refreshing your auth_token!"
        refreshAuthToken()
    }
   [auth : atomicState.encoredAccessToken]
}