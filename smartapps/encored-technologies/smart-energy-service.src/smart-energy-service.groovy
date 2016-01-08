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
    namespace: "Encored Technologies",
    author: "hyeon seok yang",
    description: "With visible realtime energy usage status, have good energy habits and enrich your life\r\n",
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
    card(name: "Encored Energy Service", type: "html", action: "getHtml", whitelist: whiteList()) {}
}

/* This list contains, that url need to be allowed in Smart Energy Service.*/
def whiteList() {
	[
    	"code.jquery.com", 
    	"ajax.googleapis.com", 
        "fonts.googleapis.com",
    	"code.highcharts.com", 
    	"enertalk-card.encoredtech.com", 
    	"s3-ap-northeast-1.amazonaws.com",
        "s3.amazonaws.com", 
    	"ui-hub.encoredtech.com",
        "enertalk-auth.encoredtech.com",
        "api.encoredtech.com",
        "cdnjs.cloudflare.com",
        "encoredtech.com",
        "itunes.apple.com"
    ]
}

/* url endpoints */
mappings {
	path("/requestCode") { action: [ GET: "requestCode" ] }
	path("/receiveToken") { action: [ GET: "receiveToken"] }
    path("/getHtml") { action: [GET: "getHtml"] }
    path("/consoleLog") { action: [POST: "consoleLog"]}
    path("/getInitialData") { action: [GET: "getInitialData"]}
    path("/getEncoredPush") { action: [POST: "getEncoredPush"]}
}


/* This method does two things depends on the existence of Encored access token. :
* 1. If Encored access token does not exits, it starts the process of getting access token.
* 2. If Encored access token does exist, it will show a list of configurations, that user need to define values. 
*/
def checkAccessToken() {
	log.debug "Staring the installation"

    /* Choose the level */    
    atomicState.env_mode ="prod"
   
    def lang = clientLocale?.language
   
   	/* getting language settings of user's device.  */
    if ("${lang}" == "ko") {
    	atomicState.language = "ko"
    } else {
    	atomicState.language = "en"
    }
    
    /* create tanslation for descriptive and informative strings that can be seen by users. */
    if (!state.languageString) {
    	createLocaleStrings() 
    }
    
	if (!atomicState.encoredAccessToken) { /*check if Encored access token does exist.*/
    	
        log.debug "Encored Access Token does not exist."
        
        if (!state.accessToken) { /*if smartThings' access token does not exitst*/
        	log.debug "SmartThings Access Token does not exist."
            
            createAccessToken() /*request and get access token from smartThings*/
            
            /* re-create strings to make sure it's been initialized. */
            //createLocaleStrings() 
        }

		def redirectUrl = buildRedirectUrl("requestCode") /* build a redirect url with endpoint "requestCode"*/
        
        /* These lines will start the OAuth process.\n*/
        log.debug "Start OAuth request."
        return dynamicPage(name: "checkAccessToken", nextPage:null, uninstall: true, install:false) {
            section{
            	paragraph state.languageString."${atomicState.language}".desc1
                href(title: state.languageString."${atomicState.language}".main,
                     description: state.languageString."${atomicState.language}".desc2,
                     required: true,
                     style:"embedded",
                     url: redirectUrl)
            }
        }
    } else {
    	/* This part will load the configuration for this application */
        return dynamicPage(name:"checkAccessToken",install:true, uninstall : true) {
        	section(title:state.languageString."${atomicState.language}".title6) {
            
            	/* A push alarm for this application */
            	input(
                    type: "boolean", 
                    name: "notification", 
                    title: state.languageString."${atomicState.language}".title1,
                    required: false,
                    default: true,
                    multiple: false
                )
                
                /* A plan that user need to decide */
                input(
                	type: "number", 
                    name: "energyPlan", 
                    title: state.languageString."${atomicState.language}".title2,
                    description : state.languageString."${atomicState.language}".subTitle1,
                    defaultValue: state.languageString.energyPlan,
                    range: "1130..*",
                    submitOnChange: true,
                    required: true, 
                    multiple: false
                )
                
                /* A displaying unit that user need to decide */
                input(
                	type: "enum", 
                    name: "displayUnit", 
                    title: state.languageString."${atomicState.language}".title3, 
                    defaultValue : state.languageString."${atomicState.language}".defaultValues.default1,
                    required: true, 
                    multiple: false, 
                    options: state.languageString."${atomicState.language}".displayUnits
                )
                
                /* A metering date that user should know */
            	input(
                	type: "enum", 
                    name: "meteringDate", 
                    title: state.languageString."${atomicState.language}".title4,
                    defaultValue: state.languageString."${atomicState.language}".defaultValues.default2,
                    required: true, 
                    multiple: false, 
                    options: state.languageString."${atomicState.language}".meteringDays
                )
                
                /* A contract type that user should know */
                input(
                	type: "enum", 
                	name: "contractType", 
                    title: state.languageString."${atomicState.language}".title5,
                    defaultValue: state.languageString."${atomicState.language}".defaultValues.default3,
                    required: true, 
                    multiple: false, 
                    options: state.languageString."${atomicState.language}".contractTypes)
            }
            
        }
    }
}

def requestCode(){
	log.debug "In state of sending a request to Encored for OAuth code.\n"
    
	/* Make a parameter to request Encored for a OAuth code. */
    def oauthParams = 
    [
		response_type: "code",
		scope: "remote",
		client_id: "${appSettings.clientId}",
        app_version: "web",
		redirect_uri: buildRedirectUrl("receiveToken")
	]

    /* Request Encored a code. */
	redirect location: "https://enertalk-auth.encoredtech.com/authorization?${toQueryString(oauthParams)}"
}

def receiveToken(){
	log.debug "Request Encored to swap code with Encored Aceess Token"
	
    /* Making a parameter to swap code with a token */
    def authorization = "Basic " + "${appSettings.clientId}:${appSettings.clientSecret}".bytes.encodeBase64()
    def uri = "https://enertalk-auth.encoredtech.com/token"
    def header = [Authorization: authorization, contentType: "application/json"]
    def body = [grant_type: "authorization_code", code: params.code]
	
    log.debug "Swap code with a token"
    def encoredTokenParams = makePostParams(uri, header, body)
    
    log.debug "API call to Encored to swap code with a token"
    def encoredTokens = getHttpPostJson(encoredTokenParams)
	
    /* make a page to show people if the REST was successful or not. */
    if (encoredTokens) {
    	log.debug "Got Encored OAuth token\n"
		atomicState.encoredRefreshToken = encoredTokens.refresh_token
		atomicState.encoredAccessToken = encoredTokens.access_token
        
        success()
	} else {
    	log.debug "Could not get Encored OAuth token\n"
    	fail()
    }
    
}


def installed() {
	log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	
    /* Make sure uuid is there. */
    getUUID()
    
   	/* Check uuid and if it does not exist then don't update.*/
    if (!atomicState.notPaired) {  
        def theDay = 1

        for(def i=1; i < 28; i++) {
        	
            /* set user choosen option to apropriate value. */
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

                if (settings.meteringDate == "매월 ${i}일") {

                    theDay = i
                    i = 28

                } else if ("말일" == settings.meteringDate) {
                    theDay = 27
                    i = 28
                }
            }

        }
		
        /* Set choosen contract to apropriate variable. */
        def contract = 1
        if (settings.contractType == "High voltage" || settings.contractType == "주택용 고압") {
            contract = 2
          	
            if (settings.energyPlan < 460) {
            	settings.energyPlan = 490
            }
        } else {
        	
        	if (settings.energyPlan < 1130) {
            	settings.energyPlan = 1130
            }
        }
		
        
        /* convert bill to milliwatts */
        def changeToUsageParam = makeGetParams("${state.domains."${atomicState.env_mode}"}/1.2/devices/${atomicState.uuid}/bill/expectedUsage?bill=${settings.energyPlan}",
                                      [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])

       def energyPlanUsage = getHttpGetJson(changeToUsageParam, 'CheckEnergyPlanUsage')
        def epUsage = 0
        if (energyPlanUsage) {
            epUsage = energyPlanUsage.usage
        } 
        
		/* update the the information depends on the option choosen */
        def configurationParam = makePostParams("${state.domains."${atomicState.env_mode}"}/1.2/me",
                                          [Authorization     : "Bearer ${atomicState.encoredAccessToken}"],
                                          [contractType      : contract, 
                                           meteringDay       : theDay,
                                           maxLimitUsage     : epUsage])
        getHttpPutJson(configurationParam)
    }
	
}

def initialize() {
	log.debug "Initializing Application"
    
    def EATValidation = checkEncoreAccessTokenValidation()
    
    /* if token exist get user's device id, uuid */
    if (EATValidation) {
    	getUUID()
        if (atomicState.uuid) {
        	
            def pushParams = makePostParams("${state.domains."${atomicState.env_mode}"}/1.2/devices/${atomicState.uuid}/events/push",
                                        [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"],
                                        [type: "REST", regId:"${state.accessToken}__${app.id}"])
            getHttpPostJson(pushParams)
		}
        
    } else {
    	log.warning "Ecored Access Token did not get refreshed!"
    }
        
    
    /* add device Type Handler */
    atomicState.dni = "EncoredDTH01"
    def d = getChildDevice(atomicState.dni)
    if(!d) {
        log.debug "Creating Device Type Handler."
        
        d = addChildDevice("Encored Technologies", "EnerTalk Energy Meter", atomicState.dni, null, [name:"EnerTalk Energy Meter", label:name])

    } else {
        log.debug "Device already created"
    }
    
    setSummary()    
}

def setSummary() {
    
    log.debug "in setSummary"
    def text = "Successfully installed."
    sendEvent(linkText:count.toString(), descriptionText: app.label,
              eventType:"SOLUTION_SUMMARY",
              name: "summary",
              value: text,
              data: [["icon":"indicator-dot-gray","iconColor":"#878787","value":text]],
              displayed: false)
}

// TODO: implement event handlers

/* 	Check the validation of Encored Access Token (EAT)
*	If it's not valid try refresh Access Token.
*	If the token gets refreshed, it will refresh the value of Encored Access Token
*	If it doesn't get refreshed, then it returns null
*/
private checkEncoreAccessTokenValidation() {
	/* make a parameter to check the validation of Encored access token */
    def verifyParam = makeGetParams("https://enertalk-auth.encoredtech.com/verify", 
    							[Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
    /* check the validation */
    def verified = getHttpGetJson(verifyParam, 'verifyToken')

    log.debug "verified : ${verified}"

    /* if Encored Access Token need to be renewed. */
    if (!verified) {
        try {
            refreshAuthToken()

            /* Recheck the renewed Encored access token. */
            verifyParam.headers = [Authorization: "Bearer ${atomicState.encoredAccessToken}"]
            verified = getHttpGetJson(verifyParam, 'CheckRefresh')

        } catch (groovyx.net.http.HttpResponseException e) {
            /* If refreshing token raises an error  */
            log.warn "Refresh Token Error :  ${e}"
        }
    }
    
    return verified
}

/* Get device UUID, if it does not exist, return false. true otherwise.*/
private getUUID() {
	atomicState.uuid = null
    atomicState.notPaired = true
    /* Make a parameter to get device id (uuid)*/
    def uuidParams = makeGetParams( "https://enertalk-auth.encoredtech.com/uuid",
                                   [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])

    def deviceUUID = getHttpGetJson(uuidParams, 'UUID')
    log.debug "device uuid is : ${deviceUUID}"
    if (!deviceUUID) {
    	return false
    }
    log.debug "got here even tho"
    atomicState.uuid = deviceUUID.uuid
    atomicState.notPaired = false
    return true
}

private createLocaleStrings() {
   state.domains = [
   		test : "http://api.encoredtech.com",
        prod : "https://api.encoredtech.com:8082/"
   ]
   state.languageString = 
   [
   		energyPlan : 30000,
    	en : [
                desc1 : "Tab below to sign in or sign up to Encored EnerTalk smart energy service and authorize SmartThings access.",
                desc2 : "Click to proceed authorization.",
                main : "EnerTalk",
                defaultValues : [
                                default1 : "kWh",
                                default2 : "1st day of the month",
                                default3 : "Low voltage"
                				],
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
                subTitle1 : "Setup your energy plan by won",
                title3 : "Display Unit",
                title4 : "Metering Date",
                title5 : "Contract Type",
                title6 : "User & Notifications",
                message1 : """ <p>Your Encored Account is now connected to SmartThings!</p> <p>Click 'Done' to finish setup.</p> """,
                message2 : """ <p>The connection could not be established!</p> <p>Click 'Done' to return to the menu.</p> """,
                message3 : [
                		header : "Device is not installed",
                        body1 : "You need to install EnerTalk device at first,",
                        body2 : "and proceed setup and register device.",
                        button1 : "Setup device",
                        button2 : "Not Installed"
                	   ],
            	message4 : [
                			header : "Device is not connected.",
                            body1 : "Please check the Wi-Fi network connection",
                            body2 : "and EnerTalk device status.",
                            body3 : "Select ‘Setup Device’ to reset the device."
                			]
            ],
        ko :[
                desc1 : "스마트 에너지 서비스를 이용하시려면 EnerTalk 서비스 가입과 SmartThings 접근 권한이 필요합니다.",
                desc2 : "아래 버튼을 누르면 인증을 시작합니다",
                main : "EnerTalk 인증",
                defaultValues : [
                                default1 : "kWh",
                                default2 : "매월 1일",
                                default3 : "주택용 저압"
                				],
                meteringDays : [
                            "매월 1일", 
                            "매월 2일", 
                            "매월 3일",
                            "매월 4일",
                            "매월 5일",
                            "매월 6일",
                            "매월 7일",
                            "매월 8일",
                            "매월 9일",
                            "매월 10일",
                            "매월 11일",
                            "매월 12일",
                            "매월 13일",
                            "매월 14일",
                            "매월 15일",
                            "매월 16일",
                            "매월 17일",
                            "매월 18일",
                            "매월 19일",
                            "매월 20일",
                            "매월 21일",
                            "매월 22일",
                            "매월 23일",
                            "매월 24일",
                            "매월 25일",
                            "매월 26일",
                            "말일"
                            ],
                displayUnits : ["원(₩)", "kWh"],
                contractTypes : ["주택용 저압", "주택용 고압"],
                title1 : "알람 설정",
                title2 : "사용 계획 (원)",
                subTitle1 : "월간 계획을 금액으로 입력하세요",
                title3 : "표시 단위",
                title4 : "정기검침일",
                title5 : "계약종별",
                title6 : "사용자 & 알람 설정",
                message1 : """ <p>EnerTalk 계정이 SmartThings와 연결 되었습니다!</p> <p>Done을 눌러 계속 진행해 주세요.</p> """,
                message2 : """ <p>계정 연결이 실패했습니다.</p> <p>Done 버튼을 눌러 다시 시도해주세요.</p> """,
                message3 : [
                		header : "기기 설치가 필요합니다.",
                        body1 : "가정 내 분전반에 EnerTalk 기기를 먼저 설치하고,",
                        body2 : "아래 버튼을 눌러 기기등록 및 연결을 진행하세요.",
                        button1 : "기기 설정",
                        button2 : "설치필요"
                	   ],
            	message4 : [
                			header : "Device is not connected.",
                            body1 : "Please check the Wi-Fi network connection",
                            body2 : "and EnerTalk device status.",
                            body3 : "Select ‘Setup Device’ to reset the device."
                			]
                
            ]
    ]

}

/* This method makes a redirect url with a given endpoint */
private buildRedirectUrl(mappingPath) {
	log.debug "Start : Starting to making a redirect URL with endpoint : /${mappingPath}"
    def url = "https://graph.api.smartthings.com/api/token/${state.accessToken}/smartapps/installations/${app.id}/${mappingPath}"
    log.debug "Done : Finished to make a URL : ${url}"
    url
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

/* make a success message. */
private success() {
	def lang = clientLocale?.language
   
    if ("${lang}" == "ko") {
   		log.debug "I was here at first."
    	atomicState.language = "ko"
    } else {
  
    	atomicState.language = "en"
    }
	log.debug atomicState.language
	def message = atomicState.languageString."${atomicState.language}".message1
	connectionStatus(message)
}

/* make a failure message. */
private fail() {
	def lang = clientLocale?.language
   
    if ("${lang}" == "ko") {
   		log.debug "I was here at first."
    	atomicState.language = "ko"
    } else {
  
    	atomicState.language = "en"
    }
    def message = atomicState.languageString."${atomicState.language}".message2
    connectionStatus(message)
}

private connectionStatus(message) {
    def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width height=device-height">
       
        <link href='https://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
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
                
            </div>
            
        </body>
        </html>
	"""
	render contentType: 'text/html', data: html
}

private refreshAuthToken() {
	/*Refreshing Encored Access Token*/
    
    log.debug "Refreshing Encored Access Token"
	if(!atomicState.encoredRefreshToken) {
		log.error "Encored Refresh Token does not exist!"
	} else {
    
    	def authorization = "Basic " + "${appSettings.clientId}:${appSettings.clientSecret}".bytes.encodeBase64()
    	def refreshParam = makePostParams("https://enertalk-auth.encoredtech.com/token",
        									[Authorization: authorization],
                                            [grant_type: 'refresh_token', refresh_token: "${atomicState.encoredRefreshToken}"])
        
        def newAccessToken = getHttpPostJson(refreshParam)
        
        if (newAccessToken) {
        	atomicState.encoredAccessToken = newAccessToken.access_token
            log.debug "Successfully got new Encored Access Token.\n"
        } else {
        	log.error "Was unable to renew Encored Access Token.\n"
        }
    }
}

private getHttpPutJson(param) {
	
    log.debug "Put URI : ${param.uri}"
	try {
       httpPut(param) { resp ->
 			log.debug "HTTP Put Success"
       }
    } catch(groovyx.net.http.HttpResponseException e) {
    	log.warn "HTTP Put Error : ${e}"
    }
}

private getHttpPostJson(param) {
	log.debug "Post URI : ${param.uri}"
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

private getHttpGetJson(param, testLog) {
	log.debug "Get URI : ${param.uri}"
   def jsonMap = null
   try {
       httpGet(param) { resp ->
           jsonMap = resp.data
       }
    } catch(groovyx.net.http.HttpResponseException e) {
    	log.warn "HTTP Get Error : ${e}"
    }
    
    return jsonMap

}

private makePostParams(uri, header, body=[]) {
	return [
    	uri : uri,
        headers : header,
        body : body
    ]
}

private makeGetParams(uri, headers, path="") {
	return [
    	uri : uri,
        path : path,
        headers : headers
    ]
}

def getInitialData() {
	def lang = clientLocale?.language
    if ("${lang}" == "ko") {
    	lang = "ko"
    } else {
        lang = "en"
    }
    atomicState.solutionModuleSettings.language = lang
	atomicState.solutionModuleSettings
}

def consoleLog() {
    log.debug "console log: ${request.JSON.str}"
}

def getHtml() {
	
    /* initializing variables */
	def deviceStatusData = "", standbyData = "", meData = "", meteringData = "", rankingData = "", lastMonth = "", deviceId = ""
	def standby = "", plan = "", start = "", end = "", meteringDay = "", meteringUsage = "", percent = "", tier = "", meteringPeriodBill = ""
    def maxLimitUsageBill, maxLimitUsage = 0
    def deviceStatus = false
    def displayUnit = "watt"
    
    def meteringPeriodBillShow = "", meteringPeriodBillFalse = "collecting data"
    def standbyShow = "", standbyFalse = "collecting data" 
    def rankingShow = "collecting data"
    def tierShow = "collecting data"
    def lastMonthShow = "", lastMonthFalse = "no records"
    def planShow = "", planFalse = "set up plan"
    
    def thisMonthUnitOne ="", thisMonthUnitTwo = "", planUnitOne = "", planUnitTwo = "", lastMonthUnit = "", standbyUnit = ""
    def thisMonthTitle = "This Month", tierTitle = "Billing Tier", planTitle = "Energy Goal", 
    lastMonthTitle = "Last Month", rankingTitle = "Ranking", standbyTitle = "Always on", energyMonitorDeviceTitle = "EnerTalk Device" , realtimeTitle = "Realtime"
    def onOff = "OFF", rankImage = "", tierImage = ""
    
    def htmlBody = ""
    
    /* Get the language setting on device. */
    def lang = clientLocale?.language
    if ("${lang}" == "ko") {
    	atomicState.language = "ko"
    } else {
        atomicState.language = "en"
    }
    
    if (atomicState.language == "ko") {
        rankingShow = "데이터 수집 중"
        meteringPeriodBillFalse = "데이터 수집 중" 
        lastMonthFalse = "정보가 없습니다" 
        standbyFalse = "데이터 수집 중"
        planFalse = "계획을 입력하세요"
        thisMonthTitle = "이번 달" 
        tierTitle = "누진단계" 
        planTitle = "사용 계획" 
        lastMonthTitle = "지난달" 
        rankingTitle = "랭킹" 
        standbyTitle = "대기전력" 
        energyMonitorDeviceTitle = "스마트미터 상태"
        realtimeTitle = "실시간"
    }
    
    /* check Encored Access Token */
    def EATValidation = checkEncoreAccessTokenValidation()
    log.debug EATValidation
    /* check if uuid already exist or not.*/
    if (EATValidation && atomicState.notPaired) {
    	getUUID()
    }
    
    /* If token has been verified or refreshed and if uuid exist, call other apis */
    log.debug atomicState.notPaired
    if (!atomicState.notPaired) {

        if(EATValidation) {
            /* make a parameter to get device status */
            def deviceStatusParam = makeGetParams( "${state.domains."${atomicState.env_mode}"}/1.2/devices/${atomicState.uuid}/status",
                                                    [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])

            /* get device status. */
            deviceStatusData = getHttpGetJson(deviceStatusParam, 'CheckDeviceStatus')
            

            /* make a parameter to get standby value.*/
            def standbyParam = makeGetParams( "${state.domains."${atomicState.env_mode}"}/1.2/devices/${atomicState.uuid}/standbyPower",
                                                [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
            
            /* get standby value */
            standbyData = getHttpGetJson(standbyParam, 'CheckStandbyPower')
            
            

            /* make a parameter to get user's info. */
            def meParam = makeGetParams( "${state.domains."${atomicState.env_mode}"}/1.2/me",
                                        [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
			
            /* Get user's info */
            meData = getHttpGetJson(meParam, 'CheckMe')
			
		
            /* make a parameter to get energy used since metering date */
            def meteringParam = makeGetParams( "${state.domains."${atomicState.env_mode}"}/1.2/devices/${atomicState.uuid}/meteringUsage",
                                                [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
            
            /* Get the value of energy used since metering date. */
            meteringData = getHttpGetJson(meteringParam, 'CheckMeteringUsage')
			
            
            /* make a parameter to get the energy usage ranking of a user. */
            def rankingParam = makeGetParams( "${state.domains."${atomicState.env_mode}"}/1.2/ranking/usages/${atomicState.uuid}?state=current&period=monthly",
                                                [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])
			
            /* Get user's energy usage rank */
            rankingData = getHttpGetJson(rankingParam, 'CheckingRanking')
			
            /* Parse the values from the returned value of api calls. Then use these values to inform user how much they have used or will use. */

            /* parse device status. */
            if (deviceStatusData) {
                if (deviceStatusData.status == "NORMAL") {
                    deviceStatus = true
                }
            }
            
            log.debug "deiceStatusData : ${deviceStatus} || ${deviceStatusData}"

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

            /* Parse the values which have been used since metering date.
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
                def lastMonthParam = makeGetParams( "${state.domains."${atomicState.env_mode}"}/1.2/devices/${atomicState.uuid}/meteringUsages?period=monthly&start=${start}&end=${end}",
                                                [Authorization: "Bearer ${atomicState.encoredAccessToken}", ContentType: "application/json"])

                lastMonth = getHttpGetJson(lastMonthParam, 'ChecklastMonth')

            }

            /* I decided to set values to device type handler, on loading solution module. 
            So, users may need to go back to solution module to update their device type handler. */
            def d = getChildDevice(atomicState.dni)
            def kWhMonth = Math.round(meteringUsage / 10000) / 100 /* milliwatt to kilowatt*/
            def planUsed = 0
            if ( maxLimitUsage > 0 ) {
                planUsed = Math.round((meteringUsage / maxLimitUsage) * 100) /* get the pecent of used amount against max usage */
            } else {
                planUsed = Math.round((meteringUsage/ 1000000) * 100) /* if max was not decided let the used value be percent. e.g. 1kWh = 100% */
            }

            /* get realtime usage of user's device.*/
            def realTimeParam = makeGetParams("${state.domains."${atomicState.env_mode}"}/1.2/devices/${atomicState.uuid}/realtimeUsage",
                                              [Authorization: "Bearer ${atomicState.encoredAccessToken}"])
            def realTimeInfo = getHttpGetJson(realTimeParam, 'CheckRealtimeinfo')

            if (!realTimeInfo) {
                realTimeInfo = 0
            } else {
                realTimeInfo = Math.round(realTimeInfo.activePower / 1000 )
            }
			
            
            
            /* inserting values to device type handler */
            
            d?.sendEvent(name: "view", value : "${kWhMonth}")
            if (deviceStatus) {

            	d?.sendEvent(name: "month", value : "${thisMonthTitle} \n ${kWhMonth} \n kWh")
            } else { 

            	d?.sendEvent(name: "month", value : "\n ${state.languageString."${atomicState.language}".message4.header} \n\n " + 
                									"${state.languageString."${atomicState.language}".message4.body1} \n " +
               										"${state.languageString."${atomicState.language}".message4.body2} \n " +
                                                    "${state.languageString."${atomicState.language}".message4.body3}")	
            }
            
            d?.sendEvent(name: "real", value : "${realTimeInfo}w \n\n ${realtimeTitle}")
            d?.sendEvent(name: "tier", value : "${tier} \n\n ${tierTitle}")
            d?.sendEvent(name: "plan", value : "${planUsed}% \n\n ${planTitle}")
          
            deviceId = d.id

        } else {
            /* If it finally couldn't get Encored access token. */
            log.error "Could not get Encored Access Token. Please try later."
        }

        /* change the display uinit to bill from kWh if user want. */
        if (settings.displayUnit == "WON(₩)" || settings.displayUnit == "원(₩)") {
            displayUnit = "bill"
        }

        if (meteringPeriodBill) {
            /* reform the value of the bill with the , separator */
            meteringPeriodBillShow = formatMoney("${meteringPeriodBill}")
            meteringPeriodBillFalse = ""
            thisMonthUnitOne = "&#x20A9;"

            def dayPassed = getDayPassed(start, end, meteringDay)
            if (atomicState.language == 'ko') {
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
            planShow = plan
            if (plan >= 1000) {planShow = formatMoney("${plan}") }
            planFalse = ""
            planUnitOne = "&#x20A9;"

            if (atomicState.language == 'ko') {
                planUnitTwo = "남음"
            } else {
                planUnitTwo = "left"
            }

        }

        /*set the showing units for html.*/
        log.debug lastMonth
        if (lastMonth.usages) {
            lastMonthShow = formatMoney("${lastMonth.usages[0].meteringPeriodBill}")
            lastMonthFalse = ""
            lastMonthUnit = "&#x20A9;"
       
        }

        if (standby) {
            standbyShow = standby
            standbyFalse = ""
            standbyUnit = "W"
        }

        if (percent) {
            rankImage = "<img id=\"image-rank\" src=\"https://s3-ap-northeast-1.amazonaws.com/smartthings-images/ranking_${percent}.svg\" />"
            rankingShow = ""
        }

        if (tier) {
            tierImage = "<img id=\"image-tier\" src=\"https://s3-ap-northeast-1.amazonaws.com/smartthings-images/tier_${tier}.svg\" />"
            tierShow = ""
        }

        if (deviceStatus) {
            onOff = "ON"
        }

        atomicState.solutionModuleSettings = [
           auth           : atomicState.encoredAccessToken, 
           deviceState    : deviceStatus, 
           percent        : percent,
           displayUnit    : displayUnit,
           language		  : atomicState.language,
           deviceId	      : deviceId,
           pairing		  : true
        ]
        
        htmlBody = """
         <div id="real-time">
         	
            <!-- real-time card -->
            <div id="my-card"></div>
            
            <!-- this month section -->
            <div class="contents head" id="content1">
              <p class="key" id="korean-this">${thisMonthTitle}</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-this">${thisMonthUnitOne}</p>
                <p class="value" id="value-this">${meteringPeriodBillShow}</p>
                <p class="value" id="value-fail">${meteringPeriodBillFalse}</p>
                <p class="unit second" id="unit-second-this">${thisMonthUnitTwo}</p>
              </span> 
            </div>
            
            <!-- Billing Tier section -->
            <div class="contents tail" id="content2">
              <p class="key" id="korean-tier">${tierTitle}</p>
              <span class="value-block">
              	<div id="value-block-tier">${tierImage}</div>
                <p class="value" id="value-fail">${tierShow}</p>
              </span>
            </div> 
            
            <!-- Plan section -->
            <div class="contents tail" id="content3">
              <p class="key" id="korean-plan">${planTitle}</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-plan">${planUnitOne}</p>
                <p class="value" id="value-plan">${planShow}</p>
                <p class="value" id="value-fail">${planFalse}</p>
                <p class="unit second" id="unit-second-plan"> ${planUnitTwo}</p> 
              </span>
            </div>
            
            <!-- Last Month section -->
            <div class="contents tail" id="content4">
              <p class="key" id="korean-last">${lastMonthTitle}</p>
              <span class="value-block">
                <p class="unit first" id="unit-first-last">${lastMonthUnit}</p>
                <p class="value" id="value-last">${lastMonthShow}</p>
                <p class="value" id="value-fail">${lastMonthFalse}</p>
              </span>
            </div>

            <!-- Ranking section -->
            <div class="contents tail" id="content5">
              <p class="key" id="korean-ranking">${rankingTitle}</p>
              <span class="value-block">
              <div id="value-block-rank">${rankImage}</div>
              <p class="value" id="value-fail">${rankingShow}</p>
              </span>
            </div> 
            
            <!-- Standby section -->
            <div class="contents tail" id="content6">
              <p class="key" id="korean-standby">${standbyTitle}</p>
              <span class="value-block">
                <p class="value" id="value-standby">${standbyShow}</p>
                <p class="value" id="value-fail">${standbyFalse}</p>
                <p class="unit third" id="unit-third-standby">${standbyUnit}<p>
              </span>
            </div>
            
            <!-- Device status section -->
            <div class="contents tail" id="content7">
              <p class="key" id="korean-device">${energyMonitorDeviceTitle}</p>
              <span class="value-block">
                <div class="circle"></div>
                <p class="value last" id="value-ON-OFF">${onOff}</p>
              </span>
            </div>
            
          </div>
          
          <!-- hidden section -->
          
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
          <script>
          	     \$("#this-month").slideUp();
                \$("#last-month").slideUp();
                \$("#progressive-step").slideUp();
                \$("#ranking").slideUp();
                \$("#plan").slideUp();
                \$("#standby").slideUp();

                var UI = new Encored.UI({
        
                });
                UI.renderCard({
            	   'cards': [{
                	'id': 'ui:h:strealtime:v1', 
                    'params': {
                                'lang': '${atomicState.language}', 
                                'useDemoLabel': 1, 
                                'displayUnit': '${displayUnit}'
					}
				    }], 
                    'accessToken': '${atomicState.encoredAccessToken}', 
                    'target': document.getElementById("my-card")
			     });
          </script>
        <script src="${buildResourceUrl('javascript/app.js')}"></script>
        
        """
    } else {
		log.debug "abotu to ask device connection"
    	def d = getChildDevice(atomicState.dni)
        /* inserting values to device type handler */ 

        d?.sendEvent(name: "month", value : "\n ${state.languageString."${atomicState.language}".message3.header} \n\n ${state.languageString."${atomicState.language}".message3.body1} \n ${state.languageString."${atomicState.language}".message3.body2}")
        deviceId = d.id
		
        if (state.language == "ko") {
        	energyMonitorDeviceTitle = "스마트미터 상태"	
        }
    	/* need device pairing */
        atomicState.solutionModuleSettings = [
        	dId		: deviceId,
            pairing	: false
        ]
  
        htmlBody = """
        
         	<div id="real-time">
        
            	<!-- real-time card -->
                <div id="st-pairing-card">
                	<p class="st-pairing-card-header" align="center">${state.languageString."${atomicState.language}".message3.header}</p>
                	<p class="st-pairing-card-body" align="center"> ${state.languageString."${atomicState.language}".message3.body1} <br/> ${state.languageString."${atomicState.language}".message3.body2}</p>
                	<div id="st-deep-link-container"></div>
                </div>



                <!-- Device status section -->
                <div class="contents tail" id="content7">
                  <p class="key">${energyMonitorDeviceTitle}</p>
                  <span class="value-block">
                    <p class="value last">${state.languageString."${atomicState.language}".message3.button2}</p>
                  </span>
                </div>
            
          	</div>
		  
          	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
            <script src="${buildResourceUrl('javascript/app.js')}"></script>
            <script>
                var ua = navigator.userAgent.toLowerCase();
                var isAndroid = ua.indexOf("android") > -1; //&& ua.indexOf("mobile");
                if(!isAndroid) { 
                	\$("#st-deep-link-container").html("<a id=\'st-deep-link\' href=\'https://itunes.apple.com/kr/app/enertalk-for-home/id1024660780?mt=8\'><p class=\'st-deep-text\'>${state.languageString."${state.language}".message3.button1}</p></a>");
				} else {
                	\$("#st-deep-link-container").html("<a id=\'st-deep-link\' href=\'market://details?id=com.ionicframework.enertalkhome874425\'><p class=\'st-deep-text\'>${state.languageString."${state.language}".message3.button1}</p></a>");
                };
            </script>
        """
    }

    renderHTML() {
        head {
        """
        	<meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no, width=device-width, height=device-height">
            <link href='https://fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
            <link rel="stylesheet" href="${buildResourceUrl('css/app.css')}" type="text/css">
            <script src="https://cdnjs.cloudflare.com/ajax/libs/webcomponentsjs/0.7.18/webcomponents-lite.min.js"></script>
            <script src="https://enertalk-card.encoredtech.com/sdk.js"></script>
            <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
        """
        }
        body {
        	htmlBody
        }
    }
}


/* put commas for money or if there are things that need to have a comma separator.*/
private formatMoney(money) {
	def i = money.length()-1
    def ret = ""
    def commas = ((int) Math.floor(i/3))

    def j = 0
    def counter = 0
 
    while (i >= 0) {

        if (counter > 0 && (counter % 3) == 0) {
        	ret = "${money[i]},${ret}"
            j++
        } else {
        	ret = "${money[i]}${ret}"
        }
        
        counter++
		i--
	}
    
    ret
}

/* Count how many days have been passed since metering day:
* 	if metering day < today, it returns today - metering day
*	else if metering day > today, it calcualtes how many days have been passed since meterin day and return calculated value.
*	else return 1 (today).
*/
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

/* Get Encored push and send the notification. */
def getEncoredPush() {

	byte[] decoded = "${params.msg}".decodeBase64()
	def decodedString = new String(decoded)

    if (settings.notification == "true") {
    	sendNotification("${decodedString}", [method: "push"])
    } else {
    	sendNotificationEvent("${decodedString}")
    }
    
}