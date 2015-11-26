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
    name: "Smart Energy Service",
    namespace: "Encored",
    author: "hyeon seok yang",
    description: "A prototype to see the proposed ux/ui procedure works without any shortages.\r\n",
    category: "SmartThings Labs",
    iconUrl: "https://s3-ap-northeast-1.amazonaws.com/smartthings-images/appicon_enertalk%401.png",
    iconX2Url: "https://s3-ap-northeast-1.amazonaws.com/smartthings-images/appicon_enertalk%402x",
    iconX3Url: "https://s3-ap-northeast-1.amazonaws.com/smartthings-images/appicon_enertalk%403x",
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
	[
    	"code.jquery.com", 
    	"ajax.googleapis.com", 
    	"code.highcharts.com", 
    	"enertalk-card.encoredtech.com", 
    	"s3-ap-northeast-1.amazonaws.com", 
    	"ui-hub.encoredtech.com"
    ]
}

mappings {
	path("/requestCode") { action: [ GET: "requestCode" ] }
	path("/receiveToken") { action: [ GET: "receiveToken"] }
    path("/getHtml") { action: [GET: "getHtml"] }
    path("/consoleLog") { action: [POST: "consoleLog"]}
    path("/getInitialData") { action: [GET: "getInitialData"]}
    path("/getEncoredPush") { action: [POST: "getEncoredPush"]}
}


def checkAccessToken() {
	log.debug "In checkAccessToken"
    log.debug "state : ${state}"
	log.debug "Encored Access Token : ${atomicState.encoredAccessToken}"
    
    def lang = clientLocale?.language
    log.debug "lang lang lang lang lang"
    if ("${lang}" == "ko")
    	state.language = "ko"
    else {
    	state.language = "en"
    }
    
    if (!state.languageString) {
    	createLocaleStrings() 
    }
    log.debug state.language
    
	if (!atomicState.encoredAccessToken) { /*check if Encored access token exist.*/
    	
        log.debug "Getting Encored Access Token"
 		log.debug "SmartThings Access Token : ${state.accessToken}"
        
        if (!state.accessToken) { /*if smartThings' access token does not exitst*/
        	log.debug "Getting SmartThings Access Token"
            createAccessToken() /*request and get access token from smartThings*/
            
            createLocaleStrings() 
        }

		def redirectUrl = buildRedirectUrl("requestCode") /* build a redirect url */
        
        log.debug "Application Redirect URL : ${redirectUrl}"
        
        /*Start the process of getting Encored Access Token.*/
        return dynamicPage(name: "checkAccessToken", nextPage:null, uninstall: true, install:false) {
            section{
            	paragraph state.languageString."${state.language}".desc1
                href(title: "EnerTalk",
                     description: state.languageString."${state.language}".desc2,
                     required: true,
                     style:"embedded",
                     url: redirectUrl)
            }
        }
    } else {
    	//not implemented yet.
        return dynamicPage(name:"checkAccessToken",install:true, uninstall : true) {
        	section(title:state.languageString."${state.language}".title6) {
            	input(
                    type: "boolean", 
                    name: "notification", 
                    title: state.languageString."${state.language}".title1, 
                    required: false, 
                    multiple: false
                )
                
                input(
                	type: "number", 
                    name: "energyPlan", 
                    title: state.languageString."${state.language}".title2, 
                    required: true, 
                    multiple: false
                )
                    
                input(
                	type: "enum", 
                    name: "displayUnit", 
                    title: state.languageString."${state.language}".title3, 
                    required: true, 
                    multiple: false, 
                    options: state.languageString."${state.language}".displayUnits
                )
                
            	input(
                	type: "enum", 
                    name: "meteringDate", 
                    title: state.languageString."${state.language}".title4, 
                    required: true, 
                    multiple: false, 
                    options: state.languageString."${state.language}".meteringDays
                )
                
                input(
                	type: "enum", 
                	name: "contractType", 
                    title: state.languageString."${state.language}".title5, 
                    required: true, 
                    multiple: false, 
                    options: state.languageString."${state.language}".contractTypes)
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
    	if (atomicState.language == "en") {
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
        } else {
        	if ("매월${i}일" == settings.meteringDate) {

                theDay = i
                i = 28

            } else if ("말일" == settings.meteringDate) {
                theDay = 27
                i = 28
            }
        }
        
    }
    def changeToUsageParam = makeGetParams("http://api-staging.encoredtech.com/1.2/devices/${atomicState.uuid}/bill/expectedUsage?bill=${settings.energyPlan}",
                                  [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
    def energyPlanUsage = getHttpGetJson(changeToUsageParam)
    def epUsage = 0
    if (energyPlanUsage) {
    	epUsage = energyPlanUsage.usage
    } 
    log.debug epUsage
    
    def contract = 1
    if (settings.contractType == "High voltage" || settings.contractType == "주택용 고압") {
    	contract = 2
    }
    
    def configurationParam = makePostParams("http://api-staging.encoredtech.com/1.2/me",
                                      [Authorization     : "Bearer ${atomicState.encoredAccessToken}"],
                                      [contractType      : contract, 
                                       meteringDay       : theDay,
                                       maxLimitUsage     : epUsage])
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
   
   def pushParams = makePostParams(["http://api-staging.encoredtech.com/1.2/devices/${atomicState.uuid}/events/push",
   									[Authorization: "Bearer ${atomicState.encoredAccessToken}",
                                    ContentType: "application/json"],
                                    [type: "AND", 
                                    regId:"${state.accessToken}|${app.id}"]
   									])
    log.debug pushParams
    
    getHttpPostJson(pushParams)
    
    /* add device Type Handler */
    atomicState.dni = "EncoredDTH01"
    def d = getChildDevice(atomicState.dni)
    if(!d) {
        log.debug "device does not exists."
        
        d = addChildDevice("Encored", "EnerTalk Energy Meter", atomicState.dni, null, [name:"EnerTalk Energy Meter", label:name])

    } else {
        log.debug "Device already created"
    }
    
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

private createLocaleStrings() {
	
   state.languageString = 
   [
    	en : [
                desc1 : "Tab below to sign in or sign up to Encored EnerTalk smart energy service and authorize SmartThings access.",
                desc2 : "Click to proceed authorization.",
                meteringDays : [
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
                displayUnits : ["WON(₩)", "kWh"],
                contractTypes : ["Low voltage", "High voltage"],
                title1 : "Send push notification",
                title2 : "Energy Plan",
                title3 : "Display Unit",
                title4 : "Metering Date",
                title5 : "Contract Type",
                title6 : "User & Notifications",
                message1 : """ <p>Your Encored Account is now connected to SmartThings!</p> <p>Click 'Done' to finish setup.</p> """,
                message2 : """ <p>The connection could not be established!</p> <p>Click 'Done' to return to the menu.</p> """
            ],
        ko :[
                desc1 : "아래 버튼을 눌러 Encored EnerTalk smart energy service 에 가입 하거나 엑세스 권한을 부여해주세요.",
                desc2 : "계속 하시려면, 여기를 눌러주세요.",
                meteringDays : [
                            "매월1일", 
                            "매월2일", 
                            "매월3일",
                            "매월4일",
                            "매월5일",
                            "매월6일",
                            "매월7일",
                            "매월8일",
                            "매월9일",
                            "매월10일",
                            "매월11일",
                            "매월12일",
                            "매월13일",
                            "매월14일",
                            "매월15일",
                            "매월16일",
                            "매월17일",
                            "매월18일",
                            "매월19일",
                            "매월20일",
                            "매월21일",
                            "매월22일",
                            "매월23일",
                            "매월24일",
                            "매월25일",
                            "매월26일",
                            "말일"
                            ],
                displayUnits : ["원", "kWh"],
                contractTypes : ["주택용 저압", "주택용 고압"],
                title1 : "알람 설정",
                title2 : "사용량 계획",
                title3 : "단위",
                title4 : "정기 검침일",
                title5 : "계약종별",
                title6 : "사용자 & 알람 설정",
                message1 : """ <p>귀하의 Encored 계정이 SmartThings 에 연결되었습니다!</p> <p>셋업을 끝내기 위해서 "Done" 을 눌러주세요.</p> """,
                message2 : """ <p>귀하의 Encored 계정이 SmartThings 에 여결되지 못 했습니다!</p> <p>첫 화면으로 돌아가기 위해서 "Done" 을 눌러주세요.</p> """
            ]
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
    log.debug "state.languagesetting ${state.language}"
	def message = state.languageString."${state.language}".message1
	connectionStatus(message)
}

private fail() {
    def message = state.languageString."${state.language}".message2
    connectionStatus(message)
}

private connectionStatus(message) {
    def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width height=device-height">
       
        <link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
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
                margin-top:20.3125vw;
               
            }
            
            .encored{
            	width: 25vw;
                height: 25vw;
                margin-right : 8.75vw;
            }
            .chain {
            	width:6.25vw;
                height: 6.25vw;
            }
            .smartt {
            	width: 25vw;
                height: 25vw;
                margin-left: 8.75vw
            }
           	
            p {
                font-size: 21px;
                font-weight: 300;
                font-family: Roboto;
                text-align: center;
                color: #4c4c4e;
                
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
                <img class="encored" src="https://s3-ap-northeast-1.amazonaws.com/smartthings-images/appicon_enertalk.png" alt="Encored icon" />
                <img class="chain" src="https://s3-ap-northeast-1.amazonaws.com/smartthings-images/icon_link.svg" alt="connected device icon" />
                <img class="smartt" src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>${message}</p>
                <div id="mobile-app"></div>
            </div>
            
			<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
         	<script>
        
            	var ua = navigator.userAgent.toLowerCase();
            	var isAndroid = ua.indexOf("android") > -1; //&& ua.indexOf("mobile");
                console.log("asdhflkashflakjshflkjasdhflkjashflkjsadhfljkadshfjklashdfljkashfkljahsdkfljhalsjkdfhajksld");
            	if(!isAndroid) { 
                	\$(\"#mobile-app\").html(\"<a href=\'https://geo.itunes.apple.com/kr/app/enertalk-for-home/id1024660780?mt=8\' style=\'display:inline-block;overflow:hidden;background:url(http://linkmaker.itunes.apple.com/images/badges/en-us/badge_appstore-lrg.svg) no-repeat;width:165px;height:40px;\'></a>\"); 
				} else { 
                	\$(\"#mobile-app\").html(\"<a href=\'market://details?id=com.ionicframework.enertalkhome\'><img alt=\'Android app on Google Play\' src=\'https://developer.android.com/images/brand/en_app_rgb_wo_45.png\' /></a>\");
				}
         </script>
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
           log.debug resp.data
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
	state.solutionModuleSettings
}

def consoleLog() {
    log.debug "console log: ${request.JSON.str}"
}

def getHtml() {
	
    /* initializing variables */
	def deviceStatusData, standbyData, meData, meteringData, rankingData, lastMonth, deviceId = ""
	def standby, plan, start, end, meteringDay, meteringUsage, percent, tier, meteringPeriodBill = ""
    def maxLimitUsageBill, maxLimitUsage = 0
    def deviceStatus = false
    def displayUnit = "watt"
    def meteringPeriodBillShow, standbyShow, rankingShow = "collecting data"
    def lastMonthShow = "no records"
    def planShow = "set up plan"
    def thisMonthUnitOne, thisMonthUnitTwo, planUnitOne, planUnitTwo, lastMonthUnit, standbyUnit = ""
    def thisMonthTitle = "This Month", tierTitle = "Billing Tier", planTitle = "Plan", 
    lastMonthTitle = "Last Month", rankingTitle = "Ranking", standbyTitle = "Standby", energyMonitorDeviceTitle = "Energy Monitor Device"
    def onOff = "OFF"
    
    /* make a parameter to check the validation of Encored access token */
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
    
    /* If token has been verified or refreshed, call other apis */
    if (verified) {
    	
        /* make a parameter to get device status */
    	def deviceStatusParam = makeGetParams( "http://api-staging.encoredtech.com/1.2/devices/${atomicState.uuid}/status",
        										[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
        
        /* get device status. */
        deviceStatusData = getHttpGetJson(deviceStatusParam)
        
        /* make a parameter to get standby value.*/
        def standbyParam = makeGetParams( "http://api-staging.encoredtech.com/1.2/devices/${atomicState.uuid}/standbyPower",
        									[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
        
        /* get standby value */
        standbyData = getHttpGetJson(standbyParam)
        
        /* make a parameter to get user's info. */
        def meParam = makeGetParams( "http://api-staging.encoredtech.com/1.2/me",
        							[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
        
        /* Get user's info */
        meData = getHttpGetJson(meParam)
        
        /* make a parameter to get energy used since metering date */
        def meteringParam = makeGetParams( "http://api-staging.encoredtech.com/1.2/devices/${atomicState.uuid}/meteringUsage",
        									[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
         
        /* Get the value of energy used since metering date. */
        meteringData = getHttpGetJson(meteringParam)
        
        /* make a parameter to get the energy usage ranking of a user. */
        def rankingParam = makeGetParams( "http://api-staging.encoredtech.com/1.2/ranking/usages/${atomicState.uuid}?state=current&period=monthly",
        									[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
        
        /* Get user's energy usage rank */
        rankingData = getHttpGetJson(rankingParam)
        
        /* Parse the values from the returned value of api calls. Then use these values to inform user how much they have used or will use. */
        
        /* parse device status. */
        if (deviceStatusData) {
        	if (deviceStatusData.status == "NORMAL") {
            	deviceStatus = true
            }
        }
        
        /* Parse standby power. */
        if (standbyData) {
        	if (standbyData.standbyPower) {
            	standby = (standbyData.standbyPower / 1000)
            }
        }
        
        /* Parse max limit usage and it's bill from user's info. */
        if (meData) {
        	if (meData.maxLimitUsageBill) {
            	maxLimitUsageBill = meData.maxLimitUsageBill
                maxLimitUsage = meData.maxLimitUsage
            }
        }
        
        /* Parse the values that they have been used since metering date.
        * The list is :
        *	meteringPeriodBill : A bill for energy usage.
        *	plan  : The left amount of bill until it reaches limit.
        *	start : metering date in millisecond e.g. if the metering started on june and 1st, 2015,06,01
        * 	end	  : Today's date in millisecond
        *	meteringDay : The day of the metering date. e.g. if the metering date is June 1st, then it will return 1.
        *	meteringUSage : The amount of energy that user has used.
        * 	tier : the level of energy use, tier exits from 1 to 6.
        */	
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
		
        /* Get ranking data of a user and the percent */
        if (rankingData) {
        	if (rankingData.user.ranking) {
            	percent = ((int)((rankingData.user.ranking / rankingData.user.population) * 10))
                if (percent > 10) {
                	percent = 10
                }
            }
        }
        
        /* if the start value exist, get last month energy usage. */
        if (start) {
            def lastMonthParam = makeGetParams( "http://api-staging.encoredtech.com/1.2/devices/${atomicState.uuid}/meteringUsages?period=monthly&start=${start}&end=${end}",
        									[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
                                            
        	lastMonth = getHttpGetJson(lastMonthParam)
            
        }
        
        /* Get the language setting on device. */
        def lang = clientLocale?.language
        if ("${lang}" == "ko")
            state.language = "ko"
        else {
            state.language = "en"
        }
        
        /* I decided to set value to device type handler, on loading solution module. 
        So, uses may need to go to solution module to update their device type handler. */
        def d = getChildDevice(atomicState.dni)
        def kWhMonth = Math.round(meteringUsage / 1000000)
        def planUsed = Math.round((meteringUsage / maxLimitUsage) * 100)
        log.debug planUsed
        d?.sendEvent(name: "view", value : "${kWhMonth}")
     	d?.sendEvent(name: "month", value : "${kWhMonth}")
        d?.sendEvent(name: "real", value : "${100}")
        d?.sendEvent(name: "tier", value : "${tier}")
        d?.sendEvent(name: "plan", value :"${planUsed}")
        deviceId = d.id

    } else {
    	/* If it finally couldn't get Encored access token. */
    	log.error "Cannot get Encored Access Token. Please try later."
    }
    
    /* change the display uinit to bill from kwh if user want. */
   	if (settings.displayUnit == "WON(₩)" || settings.displayUnit == "원") {
    	displayUnit = "bill"
    }
    
    if (state.language == "ko") {
    	rankingShow = "데이터 수집 중"
    	meteringPeriodBillShow = "데이터 수집 중" 
        lastMonthShow = "정보가 없습니다." 
        standbyShow = "데이터 수집 중"
        planShow = "계획을 입력하세요"
        thisMonthTitle = "이번달" 
        tierTitle = "누진단계" 
        planTitle = "계획" 
    	lastMonthTitle = "지난달" 
        rankingTitle = "랭킹" 
        standbyTitle = "대기전력" 
        energyMonitorDeviceTitle = "스마트미터 상태"
    }
    
    if (meteringPeriodBill) {
    	/* reform the value of the bill with the , separator */
		meteringPeriodBillShow = formatMoney("${meteringPeriodBill}")
        thisMonthUnitOne = "&#x20A9;"
        
        def dayPassed = getDayPassed(start, end, meteringDay)
        if (state.language == 'ko') {
        	thisMonthUnitTwo = "/ ${dayPassed}일"
        } else {
        	if (dayPassed == 1) {
        		thisMonthUnitTwo = "/${dayPassed} day"
            } else {
            	thisMonthUnitTwo = "/${dayPassed} days"
            }
        }
    }
    
    if (plan) {
    	planShow = formatMoney("${plan}")
        planUnitOne = "&#x20A9;"
        
        if (state.language == 'ko') {
        	planUnitTwo = "남음"
        } else {
        	planUnitTwo = "left"
        }
        
    }
    
    if (lastMonth) {
    	lastMonthShow = formatMoney("${lastMonth.usages[0].meteringPeriodBill}")
        lastMonthUnit = "&#x20A9;"
    }
    
    if (standby) {
    	standbyShow = standby
        standbyUnit = "WH"
    }
    
    if (percent) {
    	rankingShow = "<img id=\"image-rank\" src=\"https://s3-ap-northeast-1.amazonaws.com/smartthings-images/ranking_${percent}.svg\" />"
    }
    
    if (deviceStatus) {
    	onOff = "ON"
    }
    
   
   	state.solutionModuleSettings = [
       auth           : atomicState.encoredAccessToken, 
       deviceState    : deviceStatus, 
       percent        : percent,
       displayUnit    : displayUnit,
       language		  : state.language,
       deviceId	      : deviceId
   	]

    renderHTML() {
        head {
        """
        	<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width, height=device-height">
        """
        }
        body {
        """
         <div id="real-time">
         	
            <!-- real-time card -->
            <div id="my-card"></div>

            <!-- this month section -->
            <div class="contents head" id="content1">
              <p class="key" id="korean-this">${thisMonthTitle}</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-this">${thisMonthUnitOne}</p>
                <p class="value" id="value-this">${meteringPeriodBillShow}</p>
                <p class="unit second" id ="unit-second-this">${thisMonthUnitTwo}</p>
              </span> 
            </div>

            <!-- Billing Tier section -->
            <div class="contents tail" id="content2">
              <p class="key" id="korean-tier">${tierTitle}</p>
              <span class="value-block">
              	<div id="value-block-tier"><img id=\"image-tier\" src=\"https://s3-ap-northeast-1.amazonaws.com/smartthings-images/tier_${tier}.svg\" /></div>
                <p class="value" id="value-tier">${tier}</p>
              </span>
            </div>  

            <!-- Plan section -->
            <div class="contents tail" id="content3">
              <p class="key" id="korean-plan">${planTitle}</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-plan">${planUnitOne}</p>
                <p class="value" id="value-plan">${planShow}</p>
                <p class="unit second" id="unit-second-plan"> ${planUnitTwo}</p> 
              </span>
            </div>

            <!-- Last Month section -->
            <div class="contents tail" id="content4">
              <p class="key" id="korean-last">${lastMonthTitle}</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-last">${lastMonthUnit}</p>
                <p class="value" id="value-last">${lastMonthShow}</p>
              </span>
            </div>

            <!-- Ranking section -->
            <div class="contents tail" id="content5">
              <p class="key" id="korean-ranking">${rankingTitle}</p>
              <span class="value-block">
              <div id="value-block-rank">${rankingShow}</div>
              </span>
            </div> 

            <!-- Standby section -->
            <div class="contents tail" id="content6">
              <p class="key" id="korean-standby">${standbyTitle}</p>
              <span class="value-block">
                <p class="value" id="value-standby">${standbyShow}</p>
                <p class="unit third" id="unit-third-standby">${standbyUnit}<p>
              </span>
            </div>

            <!-- Device status section -->
            <div class="contents tail" id="content7">
              <p class="key" id="korean-device" >${energyMonitorDeviceTitle}</p>
              <span class="value-block">
                <div class="circle" ></div>
                <p class="value last" id="value-ON-OFF">${onOff}</p>
              </span>
            </div>


          </div>

          <!-- hidden section!! -->

		  <div id="this-month">
            <div class="card-header">
              <p class="st-title" id="korean-title-this">${thisMonthTitle}</p>
              <button class="st-show" id="show">X</button>
            </div>
            <div class="cards" id="my-card2"></div>
            <div class="cards" id="my-card3"></div>
          </div>

          <div id="last-month">
            <div class="card-header">
              <p class="st-title" id="korean-title-last">${lastMonthTitle}</p>
              <button class="st-show" id="show2">X</button>
            </div>
            <div class="cards" id="my-card4"></div>
          </div>

          <div id="progressive-step">
            <div class="card-header">
              <p class="st-title" id="korean-title-tier">${tierTitle}</p>
              <button class="st-show" id="show3">X</button>
            </div>
            <div class="cards" id="my-card5"></div>
          </div>

          <div id="ranking">
            <div class="card-header">
              <p class="st-title" id="korean-title-ranking">${rankingTitle}</p>
              <button class="st-show" id="show4">X</button>
            </div>
            <div class="cards" id="my-card6"></div>
          </div>

          <div id="plan">
            <div class="card-header">
              <p class="st-title" id="korean-title-plan">${planTitle}</p>
              <button class="st-show" id="show5">X</button>
            </div>
            <div class="cards" id="my-card7"></div>
          </div>

          <div id="standby">
            <div class="card-header">
              <p class="st-title" id="korean-title-standby">${standbyTitle}</p>
              <button class="st-show" id="show6">X</button>
            </div>
            <div class="cards" id="my-card8"></div>
          </div>
		  
          <link href='http://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" href="${buildResourceUrl('css/app.css')}" type="text/css" media="screen"/>
          <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>

    	  <script src="https://cdnjs.cloudflare.com/ajax/libs/webcomponentsjs/0.7.18/webcomponents-lite.min.js"></script>
  		  <script src="http://ui-hub.encoredtech.com/sdk.js"></script>
          <script>
          	\$("#this-month").slideUp();
            \$("#last-month").slideUp();
            \$("#progressive-step").slideUp();
            \$("#ranking").slideUp();
            \$("#plan").slideUp();
            \$("#standby").slideUp();
           
            console.log("adjhsflakhflakdhflakhdflkahdflkahskjdfhaksf");
            
            var UI = new Encored.UI({env:'development'});
            UI.renderCard({
            	'cards': [{
                	'id': 'ui:h:strealtime:v1', 
                    'params': {
                                'lang': '${state.language}', 
                                'useDemoLabel': 1, 
                                'displayUnit': '${displayUnit}'
					}
				}], 
                'accessToken': '${atomicState.encoredAccessToken}', 
                'target': document.querySelector("#my-card")
			});
          </script>
        <script type="text/javascript" src="${buildResourceUrl('javascript/app.js')}"></script>

        """
        }
    }
}

private formatMoney(money) {
	def i = money.length() -1
    def ret = ""
	while (i >= 0 ) {
        if ((i % 3) == 0 && i != (money.length() -1)) {
        	ret = ",${ret}"
        }
        ret = "${money[i]}${ret}"
        i--
    }
    
    ret
}

private getDayPassed(start, end, meteringDay){
    
    def day = 1
    def today = new Date(end)
    def tzDifference = 9 * 60 + today.getTimezoneOffset()
	today = new Date(today.getTime() + tzDifference * 60 * 1000).getDate();
    
    if (today > meteringDay) {
        day += today - meteringDay;
       
    }
    if (today < meteringDay) {
        def startDate = new Date(start);
        def month = startDate.getMonth();
        def year = startDate.getYear();
        def lastDate = new Date(year, month, 31).getDate();
 
        if (lastDate == 1) {
            day += 30;
        } else {
            day += 31;
        }
        
        day = day - meteringDay + today;
    }
    
    day
}

def getEncoredPush() {

	byte[] decoded = "${params.msg}".decodeBase64()
	def decodedString = new String(decoded)

    if (settings.notification == "true") {
    	sendNotification("${decodedString}", [method: "push"])
    } else {
    	sendNotificationEvent("${decodedString}")
    }
    
}