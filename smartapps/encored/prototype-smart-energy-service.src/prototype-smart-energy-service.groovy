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
	["code.jquery.com", "ajax.googleapis.com", "code.highcharts.com", "enertalk-card.encoredtech.com", "s3-ap-northeast-1.amazonaws.com", "ui-hub.encoredtech.com"]
}

mappings {
	path("/requestCode") { action: [ GET: "requestCode" ] }
	path("/receiveToken") { action: [ GET: "receiveToken"] }
    path("/getHtml") { action: [GET: "getHtml"] }
    path("/consoleLog") { action: [POST: "consoleLog"]}
    path("/getInitialData") { action: [GET: "getInitialData"]}
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
	
    def encoredTokenParams = makePostParams(uri, header, body)
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
	if(settings.contractType == "Low voltage"){
    	settings.contractType = 1
    } else {
    	settings.contractType = 2
    }
    
    def theDay = 1
    for(def i=1; i < 28; i++) {
    	if ("${i}st day of the month" == settings.meteringDate || 
        	"${i}nd day of the month" == settings.meteringDate || 
        	"${i}rd day of the month" == settings.meteringDate || 
        	"${i}th day of the month" == settings.meteringDate) {
            
        	theDay = i
            i = 28
            
        } else if ("Rest of the month" == settings.meteringDate) {
        	theDay = 27
            i = 28
        }
        
    }
    
    def configurationParam = makePostParams("http://api.encoredtech.com/1.2/me",
                                      [Authorization: "Bearer ${atomicState.encoredAccessToken}"],
                                      [contractType: settings.contractType, 
                                       meteringDay: theDay ])
    getHttpPutJson(configurationParam)
                                            
    
	//initialize()
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

	if(!atomicState.encoredRefreshToken) {
		log.error "Can not refresh OAuth token since there is no refreshToken stored"
	} else {
    
    	def authorization = "Basic " + "${appSettings.clientId}:${appSettings.clientSecret}".bytes.encodeBase64()
    	def refreshParam = makePostParams("http://enertalk-auth.encoredtech.com/token",
        									[Authorization: authorization],
                                            [grant_type: 'refresh_token', refresh_token: "${atomicState.encoredRefreshToken}"])
        
        def newAccessToken = getHttpPostJson(refreshParam)
        
        if (newAccessToken) {
        	atomicState.encoredAccessToken = newAccessToken.access_token
        } else {
        	log.error "Was unable to renew access token. Please check your refresh token."
        }
    }
}

private makePostParams(uri, header, body=[]) {
	return [
    	uri : uri,
        headers : header,
        body : body
    ]
}

private getHttpPutJson(param) {

	try {
       httpPut(param) { resp ->
 			log.debug "HTTP Put Success"
       }
    } catch(groovyx.net.http.HttpResponseException e) {
    	log.warn "HTTP Put Error : ${e}"
    }
}

private getHttpPostJson(param) {
   def jsonMap = null
   try {
       httpPost(param) { resp ->
           jsonMap = resp.data
       }
    } catch(groovyx.net.http.HttpResponseException e) {
    	log.warn "HTTP Post Error : ${e}"
    }
    
    return jsonMap
}

private makeGetParams(uri, headers, path="") {
	return [
    	uri : uri,
        path : path,
        headers : headers
    ]
}

private getHttpGetJson(param) {
	
   def jsonMap = null
   try {
       httpGet(param) { resp ->
           jsonMap = resp.data
       }
    } catch(groovyx.net.http.HttpResponseException e) {
    	log.debug param
    	log.warn "HTTP Get Error : ${e}"
    }
    
    return jsonMap

}

def getInitialData() {
	def deviceStatusData, standbyData, meData, meteringData, rankingData
	def deviceStatus, standby, plan, start, end, meteringDay, meteringUsage, percent, tier, meteringPeriodBill = false
    def maxLimitUsageBill = 0
    def displayUnit = "watt"
    /* make a parameter to validate the Encored access token */
    def verifyParam = makeGetParams("http://enertalk-auth.encoredtech.com/verify", 
    							[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
    /* check the validation */
    def verified = getHttpGetJson(verifyParam)
    
    log.debug "verified : ${verified}"
    
    /* if Encored Access Token need to be renewed. */
    if (!verified) {
    	try {
        	refreshAuthToken()
            
            /* Recheck the renewed Encored access token. */
    		verifyParam.headers = [Authorization: "Bearer ${atomicState.encoredAccessToken}"]
    		verified = getHttpGetJson(verifyParam)
            
        } catch (groovyx.net.http.HttpResponseException e) {
        	/* If refreshing token raises an error  */
        	log.warn "Refresh Token Error :  ${e}"
        }
    }
    
    /* call other apis */
    if (verified) {
    	def deviceStatusParam = makeGetParams( "http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/status",
        										[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
        
        deviceStatusData = getHttpGetJson(deviceStatusParam)
        
        def standbyParam = makeGetParams( "http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/standbyPower",
        									[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
                                            
        standbyData = getHttpGetJson(standbyParam)
        
        def meParam = makeGetParams( "http://api.encoredtech.com/1.2/me",
        							[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
        
        meData = getHttpGetJson(meParam)
        
        def meteringParam = makeGetParams( "http://api.encoredtech.com/1.2/devices/${atomicState.uuid}/meteringUsage",
        									[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
                                            
        meteringData = getHttpGetJson(meteringParam)
        
        def rankingParam = makeGetParams( "http://api.encoredtech.com/1.2/ranking/usages/${atomicState.uuid}?state=current&period=monthly",
        									[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
                                            
        rankingData = getHttpGetJson(rankingParam)
        
        /* compute the values that will be used in web view. */
        
        if (deviceStatusData) {
        	if (deviceStatusData.status == "NORMAL") {
            	deviceStatus = true
            }
        }
        
        if (standbyData) {
        	if (standbyData.standbyPower) {
            	standby = (standbyData.standbyPower / 1000)
            }
        }
        
        if (meData) {
        	if (meData.maxLimitUsageBill) {
            	maxLimitUsageBill = meData.maxLimitUsageBill
            }
        }
        
        if (meteringData) {
        	if (meteringData.meteringPeriodBill) {
            	meteringPeriodBill = meteringData.meteringPeriodBill
            	plan = maxLimitUsageBill - meteringData.meteringPeriodBill
                start = meteringData.meteringStart
                end = meteringData.meteringEnd
                meteringDay = meteringData.meteringDay
                meteringUsage = meteringData.meteringPeriodUsage
                tier = ((int) (meteringData.meteringPeriodUsage / 100000000) + 1)
                if(tier > 6) {
                	tier = 6
                }
                
            } 
        }

        if (rankingData) {
        	if (rankingData.user.ranking) {
            	percent = ((int)((rankingData.user.ranking / rankingData.user.population) * 10))
                if (percent > 10) {
                	percent = 10
                }
            }
        }
        
    } else {
    	/* If it finally, couldn't get Encored access token. */
    	log.error "Cannot get Encored Access Token. Please try later."
    }
    
   	if (settings.displayUnit == "WON(₩)") {
    	displayUnit = "bill"
    }
   	[
   auth           : "f247a7005e3f152d23317abaf1e9351d1ceb034039065e194cf5bfc71ade965060919f64eb02a8ff2e5784aa66c8768fac902f47c96d454b2c13b74c428ac1c4", 
   deviceState    : deviceStatus, 
   standbyPower   : standby,
   plan           : plan,
   start          : start,
   end            : end,
   meteringDate   : meteringDay,
   percent        : percent,
   tier           : tier,
   meteringPeriodBill      : meteringPeriodBill,
   displayUnit    : displayUnit
   ]
}

def consoleLog() {
    log.debug "console log: ${request.JSON.str}"
}

def getHtml() {

    renderHTML() {
        head {
        """
        <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width, height=device-height">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        
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
                <p class="unit first" id="unit-first-this"></p>
                <p class="value" id="value-this">analyzing...</p>
                <p class="unit second" id ="unit-second-this"></p>
              </span> 
            </div>

            <!-- Billing Tier section -->
            <div class="contents tail" id="content2">
              <p class="key" id="korean-tier">Billing Tier</p>
              <span class="value-block">
              	<div id="value-block-tier"></div>
                <p class="value" id="value-tier">analyzing...</p>
              </span>
            </div>  

            <!-- Plan section -->
            <div class="contents tail" id="content3">
              <p class="key" id="korean-plan">Plan</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-plan"></p>
                <p class="value" id="value-plan">set up plan</p>
                <p class="unit second" id="unit-second-plan"></p> 
              </span>
            </div>

            <!-- Last Month section -->
            <div class="contents tail" id="content4">
              <p class="key" id="korean-last">Last Month</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-last"></p>
                <p class="value" id="value-last">no records</p>
              </span>
            </div>

            <!-- Ranking section -->
            <div class="contents tail" id="content5">
              <p class="key" id="korean-ranking">Ranking</p>
              <span class="value-block">
              <div id="value-block-rank"></div>
              <p class="value" id="value-last">analyzing...</p>
              </span>
            </div> 

            <!-- Standby section -->
            <div class="contents tail" id="content6">
              <p class="key" id="korean-standby">Standby</p>
              <span class="value-block">
                <p class="value" id="value-standby">analyzing...</p>
                <p class="unit third" id="unit-third-standby"><p>
              </span>
            </div>

            <!-- Device status section -->
            <div class="contents tail" id="content7">
              <p class="key">Energy Monitor Device</p>
              <span class="value-block">
                <div class="circle" ></div>
                <p class="value last" id="value-ON-OFF">ON</p>
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