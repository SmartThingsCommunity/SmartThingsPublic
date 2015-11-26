/**
 *  Encored HTML Solution Module
 *
 *  Copyright 2015 Encored Technologies
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
    name: "Encored Energy Service",
    namespace: "Encored",
    author: "Encored Technologies",
    description: "An example showing several capabilities of HTML solution modules.",
    category: "SmartThings Labs",
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
	page(name: "authPage", title: "encoredTech", install:false)
}

cards {
    card(name: "Encored Energy Service", type: "html", action: "getHtml", whitelist: whitelist()) {}
}

def whitelist() {
	["code.jquery.com", "ajax.googleapis.com", "code.highcharts.com", "enertalk-card.encoredtech.com", "ui-hub.encoredtech.com"]
}

mappings {
	path("/auth") { action: [ GET: "auth" ] } /*get code value and make params for login.*/
	path("/receiveToken") { action: [ GET: "receiveToken"] } /*swap code and access Token*/
    path("/getHtml") { action: [GET: "getHtml"] }
    path("/consoleLog") {
        action: [POST: "consoleLog"]
    }
    
    path("/getInitialData") {
        action: [GET: "getInitialData"]
    }
}

def authPage(){
	log.debug "authPage()"
    log.debug "Start: ${atomicState}"
    
    if (!atomicState.authToken) { /*check if 3rd party's access token exist.*/
 
        if (!state.accessToken) { /*if smartThings' token does not available*/
        	log.debug "createAccessToken()"
            createAccessToken() /*request access token to smartThings*/
        }
        
        log.debug "creatAccessToken : ${state.accessToken}"

		def redirectUrl = buildRedirectUrl("auth")
        log.debug "${redirectUrl}"
        
        def des = "Click here to proceed..."
        return dynamicPage(name: "authPage", title: "EncoredTech", nextPage: null, uninstall: true, install:false) {
            section { href url:redirectUrl, style:"embedded", required:true, title:"Get Access Token", description: des }
        }
    } else { /* if it has been authorized by the user. */
        log.debug "in else"
        return dynamicPage(name:"authPage", title:"Discovery Started!", nextPage: null, install:true, uninstall: true) {
			section("Almost done! Please push done to finish setting and installing.") {
			}
		}        
    }
}

def auth(){
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
	log.debug "receiveToken()"
    def au = "Basic " + "${appSettings.clientId}:${appSettings.clientSecret}".bytes.encodeBase64()
    log.debug au
    def tokenParams = [
    	uri: "https://enertalk-auth.encoredtech.com/token",
        headers: [Authorization: au],
        contentType: "application/json",
        body : [
                grant_type: "authorization_code",
                code: params.code
                ]
    ]

	def jsonMap
	log.debug tokenParams
    httpPostJson(tokenParams) { resp ->
		jsonMap = resp.data
    }

	atomicState.refreshToken = jsonMap.refresh_token
	atomicState.authToken = jsonMap.access_token
    
    log.debug "refreshToken : ${atomicState.refreshToken}"
    
    //refreshAuthToken()
	
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
            width: 560px;
            padding: 40px;
            /*background: #eee;*/
            text-align: center;
        }
        img {
            vertical-align: middle;
        }
        img:nth-child(2) {
            margin: 0 30px;
        }
        p {
            font-size: 2.2em;
            font-family: 'Swiss 721 W01 Thin';
            text-align: center;
            color: #666666;
            padding: 0 40px;
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
            <img src="https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png" alt="encored icon" />
            <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
            <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
            <p>Your Encored Technologies Account is now connected to SmartThings!</p>
            <p>Click 'Done' to finish setup.</p>
            <p><a href="http://enertalk-card.encoredtech.com/link?url=enertalkhome://intro/main">Install device 2</a></p>
        </div>
    </body>
    </html>
    """

	render contentType: 'text/html', data: html
}

def getServerUrl() { return "https://graph.api.smartthings.com" }

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def buildRedirectUrl(pat) {
    //return "${serverUrl}/api/token/${state.accessToken}/smartapps/installations/${app.id}/${page}"
    return "${serverUrl}/api/token/${state.accessToken}/smartapps/installations/${app.id}/${pat}"
}


def installed() {
	log.debug "Installed with settings: ${settings}"
    log.debug "setting setting setting"
	   
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
            headers : [Authorization: "Bearer ${atomicState.authToken}", ContentType: "application/json"]
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
        eParams.headers = [Authorization: "Bearer ${atomicState.authToken}"]
        httpGet(eParams) { resp ->
            uuid = resp.data.uuid
        }
    }
    
    atomicState.uuid = uuid
    log.debug "get uuid: ${atomicState.uuid}"
   
    atomicState.dni = "encoredTest6"
    def d = getChildDevice(atomicState.dni)
    if(!d) {
        log.debug "device does not exists."
        
        d = addChildDevice("ttest", "updateTester", atomicState.dni, null, [name:"Poll test", label:name])
        //d.take()
        //d.sendEvent(name: "power", value: "${atomicState.watt}")
        //log.debug "created ${d.displayName} with id $dni"
    } else {
        log.debug "Device already created"
    }
    setSummary()    
    poll()
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
def poll() {
	//sendNotification("test notification - push", [method: "push"])
    log.debug "in parent poll"
	def d = getChildDevice(atomicState.dni)
    log.debug "making eParams: uuid: ${atomicState.uuid}"
    //atomicState.authToken = "55b866fb8fc0e7a7859ede774e21d7b667aec19dd06c7b77619efab85a32645c69635d1fc731a1d41a1af7b5c0f6670febae5a2f048eff91f66c3db45b24013a"
    def eParams = [
            uri: "http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/realtimeInfo",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.authToken}"]
		]
	log.debug "sending http get request."
    def meter
    try {
        httpGet(eParams) { resp ->
            log.debug "$resp"
            log.debug "$resp.status"
            if(resp.status == 200)
            {
                log.debug "poll"
                meter = resp	
            }
    	}
    } catch (e) {
    	log.debug "$e"
        log.debug "Refreshing your auth_token!"
        refreshAuthToken()
        eParams.headers = [Authorization: "Bearer ${atomicState.authToken}"]
        httpGet(eParams) { resp ->
        	meter = resp
        }
    }
    
    
    
     d?.sendEvent(name: "code", value : "${app.id}")
    d?.sendEvent(name: "power", value: "${meter.data.active_power}")
    
}

def getAccessToken(shouldRefresh) {
	if(shouldRefresh) {
    	refreshAuthToken()
    }
    	
	return atomicState.authToken
}

private refreshAuthToken() {
	log.debug "refreshing auth token"
	log.debug "atomicState $atomicState"
	if(!atomicState.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {
		def au = "Basic " + "${appSettings.clientId}:${appSettings.clientSecret}".bytes.encodeBase64()
		def refreshParams = [
				method: 'POST',
				uri   : "http://enertalk-auth.encoredtech.com",
                headers : [Authorization: au],
				path  : "/token",
				body : [grant_type: 'refresh_token', refresh_token: "${atomicState.refreshToken}"],
		]
        
        def token
        httpPost(refreshParams) { resp ->
            token = resp.data
        }
        atomicState.authToken = token.access_token
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
    renderHTML() {
        head {
        """
        <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width, height=device-height">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        <!-- test cards import directly -->
 		<script src="https://cdnjs.cloudflare.com/ajax/libs/webcomponentsjs/0.7.18/webcomponents-lite.min.js"></script>
        
        <script src="${buildResourceUrl('javascript/sdk.js')}"></script>
        <link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" href="${buildResourceUrl('css/app.css')}" type="text/css" media="screen"/>
        <script type="text/javascript" src="${buildResourceUrl('javascript/app.js')}"></script>
        """
        }
        body {
        """
         <div id="real-time">

    <!-- real-time card -->
    <div id="my-card"></div>

    <!-- this month section -->
    <div class="contents head" id="content1">
      <p class="key" id="korean-this">This Month</p>
      <span class="value-block">
        <p class="unit first">&#x20A9;</p>
        <p class="value" id="value-this">25,960</p>
        <p class="unit second">/13 days</p>
      </span> 
    </div>

    <!-- Billing Tier section -->
    <div class="contents tail" id="content2">
      <p class="key" id="korean-tier">Billing Tier</p>
      <span class="value-block">
        <p class="value" id="value-tier">3</p>
      </span>
    </div>  
    
    <!-- Plan section -->
    <div class="contents tail" id="content3">
      <p class="key" id="korean-plan">Plan</p>
      <span class="value-block">
        <p class="unit first">&#x20A9;</p>
        <p class="value" id="value-plan">5,960</p>
        <p class="unit second">left</p> 
      </span>
    </div>

    <!-- Last Month section -->
    <div class="contents tail" id="content4">
      <p class="key" id="korean-last">Last Month</p>
      <span class="value-block">
        <p class="unit first">&#x20A9;</p>
        <p class="value" id="value-last">54,120</p>
      </span>
    </div>

    <!-- Ranking section -->
    <div class="contents tail" id="content5">
      <p class="key" id="korean-ranking">Ranking</p>
      <span class="value-block">
      </span>
    </div> 
 
    <!-- Standby section -->
    <div class="contents tail" id="content6">
      <p class="key" id="korean-standby">Standby</p>
      <span class="value-block">
        <p class="value" id="value-standby">7.356</p>
        <p class="unit third">w<p>
      </span>
    </div>

    <!-- Device status section -->
    <div class="contents tail" id="content7">
      <p class="key">Energy Monitor Device</p>
      <span class="value-block">
        <div class="circle" ></div>
        <p class="value last">OK</p>
      </span>
    </div>

    
  </div>

  <!-- hidden section!! -->

  <div id="this-month">
    <div class="card-header">
      <p class="st-title" id="korean-title-this">This Month</p>
      <button class="st-show" id="show">X</button>
    </div>
    <div class="cards" id="my-card2"></div>
    <div class="cards" id="my-card3"></div>
  </div>
  
  <div id="last-month">
    <div class="card-header">
      <p class="st-title" id="korean-title-last">Last Month</p>
      <button class="st-show" id="show2">X</button>
    </div>
    <div class="cards" id="my-card4"></div>
  </div>
  
  <div id="progressive-step">
    <div class="card-header">
      <p class="st-title" id="korean-title-tier">Billing Tier</p>
      <button class="st-show" id="show3">X</button>
    </div>
    <div class="cards" id="my-card5"></div>
  </div>
  
  <div id="ranking">
    <div class="card-header">
      <p class="st-title" id="korean-title-ranking">Ranking</p>
      <button class="st-show" id="show4">X</button>
    </div>
    <div class="cards" id="my-card6"></div>
  </div>
    
  <div id="plan">
    <div class="card-header">
      <p class="st-title" id="korean-title-plan">Plan</p>
      <button class="st-show" id="show5">X</button>
    </div>
    <div class="cards" id="my-card7"></div>
  </div>
  
  <div id="standby">
    <div class="card-header">
      <p class="st-title" id="korean-title-standby">Standby</p>
      <button class="st-show" id="show6">X</button>
    </div>
    <div class="cards" id="my-card8"></div>
  </div>

        """
        }
    }
}

def getInitialData() {
	log.warn "stop this shit"
	log.debug "in getInitialData"
    if(0) {
    	log.debug "0 is not false"
    }
    
    def eParams = [
            uri: "http://enertalk-auth.encoredtech.com/verify",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.authToken}", ContentType: "application/json"]
		]
	log.debug "trying... http://enertalk-auth.encoredtech.com/verify"
    
    def token
    
    try {
        httpGet(eParams) { resp ->
            if(resp.status == 200)
            {	
            	log.debug resp.data
                log.debug "passed... http://enertalk-auth.encoredtech.com/verify"	
            }
    	}
    } catch (e) {
    	log.debug "not passed... http://enertalk-auth.encoredtech.com/verify"
        refreshAuthToken() //need to think if error has been occured.
    }
    
    //check the device status.
    eParams = [
            uri: "http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/status",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.authToken}", ContentType: "application/json"]
		]
	log.debug "trying... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/status"
    
    def deviceState = false
    
    try {
        httpGet(eParams) { resp ->
            if(resp.status == 200)
            {
                log.debug "passed... http:///api.encoredtech.com/1.2/devices/${atomicState.uuid}/status"	
            	if (resp.data.status == "NORMAL") {
                	deviceState = true
                }
            }
    	}
    } catch (e) {
    	log.debug "not passed... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/status"
    }
    
     eParams = [
            uri: "http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/standbyPower",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.authToken}", ContentType: "application/json"]
		]
	log.debug "trying... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/standbyPower"
    def standby = 0
    try {
        httpGet(eParams) { resp ->
            if(resp.status == 200)
            {
                log.debug "passed... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/standbyPower"	
            	standby = (resp.data.standbyPower / 1000)
            }
    	}
    } catch (e) {
    	log.debug "not passed... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/standbyPower"
    }
    
    
    eParams = [
            uri: "http://api.encoredtech.com/1.2/me",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.authToken}", ContentType: "application/json"]
		]
	log.debug "trying... http://api.encoredtech.com/1.2/me"
    def maxLimitUsageBill = 0
    try {
        httpGet(eParams) { resp ->
            if(resp.status == 200)
            {
                log.debug "passed... http://api.encoredtech.com/1.2/me"	
            	maxLimitUsageBill = resp.data.maxLimitUsageBill
            }
    	}
    } catch (e) {
    	log.debug "not passed... http://api.encoredtech.com/1.2/me"
    }
    
    eParams = [
            uri: "http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/meteringUsage",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.authToken}", ContentType: "application/json"]
		]
	log.debug "trying... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/meteringUsage"
    def meteringPeriodBill, start, end, metering, meteringUsage, plan
    try {
        httpGet(eParams) { resp ->
            if(resp.status == 200)
            {
                log.debug "passed... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/meteringUsage"	
                meteringPeriodBill = resp.data.meteringPeriodBill
                start = resp.data.meteringStart
                end = resp.data.meteringEnd
                metering = resp.data.meteringDay
                meteringUsage = resp.data.meteringPeriodUsage
            }
    	}
    } catch (e) {
    	log.debug "not passed... http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/meteringUsage"
    }
    
    eParams = [
            uri: "http://api.encoredtech.com/1.2/ranking/usages/${atomicState.uuid}?state=current&period=monthly",
            path: "",
            headers : [Authorization: "Bearer ${atomicState.authToken}", ContentType: "application/json"]
		]
	log.debug "trying... http://api.encoredtech.com/1.2/ranking/usages/${atomicState.uuid}?state=current&period=monthly"
    def ranking, population, percent
    try {
        httpGet(eParams) { resp ->
            if(resp.status == 200)
            {
                log.debug "passed... http://api.encoredtech.com/1.2/ranking/usages/${atomicState.uuid}?state=current&period=monthly"
                ranking = resp.data.user.ranking
                population = resp.data.user.population
                log.debug ranking
            }
    	}
    } catch (e) {
    	log.debug "not passed... http://api.encoredtech.com/1.2/ranking/usages/${atomicState.uuid}?state=current&period=monthly"
    }

 	 percent = ranking/population
     plan = maxLimitUsageBill - meteringPeriodBill
     def tier = ((int) (meteringUsage / 100000000) + 1)%10
    //ranking
    
    log.debug "timestamp"
    def now = new GregorianCalendar(2015, Calendar.JANUARY, 17, 15, 00, 00)
    def ss = new Date(start).toCalendar()
    def currentDate = new Date(start).toCalendar()
			def prevYear = currentDate.get(Calendar.YEAR)
            def prevMonth = currentDate.get(Calendar.MONTH)-1
           def  prevDay = meteringDay
            if (meteringDay > 27) {
	            prevDay = 27
            }
            def prevDate = new GregorianCalendar(prevYear, prevMonth, prevDay-1, 15, 00, 00).time.time

    log.debug now.time.month
    log.debug  now.get(Calendar.YEAR)
    log.debug now.get(Calendar.MONTH)
    log.debug  now.get(Calendar.DATE)
    log.debug new GregorianCalendar(ss.get(Calendar.YEAR), 
    								-1, 
                                    0,
                                    15, 00 ,00).time.time

    
   [
   auth           : "40a406bae883a2dafcddf39b3d1835d1ce3e75623b82183c55bde8d5d8b828787829a04c26dc8bc9b4374e838880560fc188f3ee3c981ee915743870a001524f", 
   deviceState    : deviceState, 
   standbyPower   : standby,
   plan           : plan,
   start          : start,
   end            : end,
   meteringDate   : metering,
   percent        : percent,
   tier           : tier,
   meteringPeriodBill      : meteringPeriodBill
   ]
}



// TODO: implement event handlers