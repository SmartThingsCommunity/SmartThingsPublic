/**
 *  Copyright 2015 SmartThings
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
 *	Navien Service Manager
 *
 *	Author: sangju
 *	Date: 2015-11-01
 *
 */
definition(
    name: "나비엔 스마트톡 연동",
    namespace: "smartthings",
    author: "나비엔 스마트톡",
    description: "SmartThings에서 나비엔 스마트톡의 온도조절기를 연결합니다.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Navien/navien.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Navien/navien@2x.png",
    singleInstance: true
) {
	appSetting "clientId"
	appSetting "serverUrl"
    appSetting "bcd"
}

preferences {
	page(name: "loginPage",        title: "나비엔 스마트톡 등록")
	page(name: "navienAuth",       title: "나비엔 스마트톡 등록")
    page(name: "navienDeviceList", title: "나비엔 스마트톡 등록", install: true )
}

def refreshToken
def authToken
def userName

def loginPage(){
	log.debug "authPage()"
	
    def showUninstall = username != null && password != null
    return dynamicPage(name: "loginPage", title: "경동나비엔", nextPage:"navienAuth", uninstall:false) {
        section("나비엔 스마트톡 등록"){
            input "username", "text",     title: "나비엔 스마트톡 아이디",   required: true, autoCorrect:false
            input "password", "password", title: "나비엔 스마트톡 패스워드", required: true, autoCorrect:false
        }
        //section("To use Navien, SmartThings encrypts and securely stores your Navien credentials.") {}
    }
}

def navienAuth(){
	log.debug "navienAuth()"
    
    def loginResult = forceLogin()
    if(loginResult.success)
    {
    	return dynamicPage(name: "navienAuth", title: "나비엔 스마트톡 인증", nextPage:"navienDeviceList", uninstall:false) { 
            section(){
        		paragraph "나비엔 스마트톡 인증 성공"
        	}
        }
    }
    else
    {
    	return dynamicPage(name: "navienAuth", title: "나비엔 스마트톡 인증", nextPage:null, uninstall:false) { 
        	section("Login failed"){
        		paragraph "나비엔 스마트톡 인증 실패"
        	}
        }
    }
}

def navienDeviceList(){
	log.debug "navienDeviceList()"
    
    def connectResult = navienConnecting()
    def p
    if(connectResult.success)
    {
    	statusSetting(state.status)
        
        def stats = getNavienThermostats()
        log.debug "device list: $stats"
                
        p = dynamicPage(name: "navienDeviceList", title: "나비엔 스마트톡 선택", install:true) {
            section(""){
                paragraph "나비엔 스마트톡 계정에서 사용할 수 있는 온도 조절 장치의 목록을 확인하고 SmartTings에 연결하려는 목록을 선택하려면 아래에서 선택해 주세요."
                input(name: "thermostats", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:stats])
            }
		}
    }
    else
    {
    	p =  dynamicPage(name: "navienDeviceList", title: "나비엔 스마트톡 선택", nextPage:null, uninstall:false) { 
        	section("나비엔 스마트톡 온도 조절 장치의 연결 상태를 확인하시기 바랍니다."){
        		paragraph ""
        	}
        }
    }
    log.debug "list p: $p"
	return p
}

def getNavienThermostats() {
    def stats = [:]
    def dni = [getChildName()].join('.')
    
    if(state.boilerType == "01") stats[dni] = "스마트톡"
    else if(state.boilerType == "02") stats[dni] = "콘덴싱톡"
    else stats[dni] = "----"
    
    return stats
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

def uninstalled() {
	def devices = getChildDevices()
	if(devices != null) log.trace "deleting ${devices.size()} device"
}

def initialize() {
	log.debug "initialize"
    
    def d = getChildDevice(getChildName())

    if(!d)
    {
        d = addChildDevice(getChildNamespace(), getChildName(), getChildName())
        log.debug "created ${d.displayName} with id $dni"
    }
    else
    {
        log.debug "found ${d.displayName} with id $dni already exists"
    }

    def devices = d
    
    log.debug "created ${devices.size()} thermostats"
	
    def delete
	// Delete any that are no longer in settings   
    
	if(!thermostats)
	{
		log.debug "If delete thermostats"
		delete = getAllChildDevices()
	}
	else
	{
    	log.debug "Else delete thermostats"
    	if(it != null) delete = getChildDevices().findAll { !thermostats.contains(it.deviceNetworkId) }
	}

	if(delete != null) log.debug "deleting ${delete.size()} thermostats"
	if(it != null) delete.each { deleteChildDevice(it.deviceNetworkId) }

	atomicState.thermostatData = [:]
    
    pollHandler()    
}

def pollHandler() {
	log.debug ("pollHandler.")
	pollChildren()    
    
    atomicState.thermostats.each {stat ->
		def dni = stat.key

		log.debug ("DNI = ${dni}")
        debugEvent ("DNI = ${dni}")
        
        def d = getChildDevice(dni)
        if(d)
		{
			log.debug ("Found Child Device.")
			debugEvent ("Found Child Device.")
			debugEvent("Event Data before generate event call = ${stat}")

			d.generateEvent(atomicState.thermostats[dni].data)
		}
	}
}

def pollChildren()
{
	log.trace "polling children"
        
    def pollParams = [
        uri: getServerUrl()+"/api/SmartTokApi?bcd="+getBCD()+"&mid=${state.mid}&did=36&subid=1&cmd=01&data=0",
		headers: ["Authorization": "Bearer ${state.authToken}"]
	]
    
    log.trace "Before HTTPGET to navien."
    def jsonData
    try{
        httpGet(pollParams) { resp ->            
            if (resp.status == 200)
         	{
            	log.debug "poll results returned"
                //atomicState.thermostats = resp.data.Status.inject([:]) { collector, stat ->
				atomicState.thermostats = "1".inject([:]) { collector, stat ->
					def dni = [getChildName()].join('.')

					log.debug "updating dni $dni"

					def data = statusSetting(resp.data.Status)

					log.debug ("Event Data = ${data}")

					collector[dni] = [data:data]
					return collector
                }
                
                log.debug "updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
            }
            else
			{
				log.error "polling children & got http status ${resp.status}"

				//refresh the auth token
				if(resp.status == 400)
                {
                	log.debug "Bad Request Description"
                }
                else if(resp.status == 401)
                {
                	log.debug "Unauthorized Description"
                }
                else if(resp.status == 500)
                {
                	log.debug "InternalServerError Description"
                    atomicState.action = "pollChildren";
                    refreshAuthToken()
                }
                else
				{
					log.error "Authentication error, invalid authentication method, lack of credentials, etc."
				}
			}
        }       
    }
	catch(all)
	{
		log.debug "___exception polling children: "
		//refreshAuthToken()
	}
}

def getPollRateMillis() { return 2 * 60 * 1000 }

def pollChild( child )
{
	log.debug "poll child"
	debugEvent ("poll child")
	def now = new Date().time
    log.debug "now ====> ${now}"

	debugEvent ("Last Poll Millis = ${atomicState.lastPollMillis}")
	def last = atomicState.lastPollMillis ?: 0
	def next = last + pollRateMillis

	log.debug "pollChild( ${child.device.deviceNetworkId} ): $now > $next ?? w/ current state: ${atomicState.thermostats}"
	debugEvent ("pollChild( ${child.device.deviceNetworkId} ): $now > $next ?? w/ current state: ${atomicState.thermostats}")

	// if( now > next )
	if( true ) // for now let's always poll/refresh
	{
		log.debug "polling children because $now > $next"
		debugEvent("polling children because $now > $next")

		pollChildren()

		log.debug "polled children and looking for ${child.device.deviceNetworkId} from ${atomicState.thermostats}"
		debugEvent ("polled children and looking for ${child.device.deviceNetworkId} from ${atomicState.thermostats}")

		def currentTime = new Date().time
		debugEvent ("Current Time = ${currentTime}")
		atomicState.lastPollMillis = currentTime

		def tData = atomicState.thermostats[child.device.deviceNetworkId]

		if(!tData)
		{
			log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

			// TODO: flag device as in error state
			// child.errorState = true

			return null
		}

		tData.updated = currentTime

		return tData.data
	}
	else if(atomicState.thermostats[child.device.deviceNetworkId] != null)
	{
		log.debug "not polling children, found child ${child.device.deviceNetworkId} "

		def tData = atomicState.thermostats[child.device.deviceNetworkId]
		if(!tData.updated)
		{
			// we have pulled new data for this thermostat, but it has not asked us for it
			// track it and return the data
			tData.updated = new Date().time
			return tData.data
		}
		return null
	}
	else if(atomicState.thermostats[child.device.deviceNetworkId] == null)
	{
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId}"

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}
	else
	{
		// it's not time to poll again and this thermostat already has its latest values
	}

	return null
}

def childRequest( child, subid, cmd, data )
{
	getControlSend(subid, cmd, data)
    def tData = atomicState.thermostats[child.device.deviceNetworkId]

    if(!tData)
    {
        log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
        return null
    }
    tData.updated = currentTime

    return tData.data
}

def getControlSend(subid, cmd, data)
{
	log.trace "getParams"
        
    def pollParams = [
        uri: getServerUrl()+"/api/SmartTokApi?bcd="+getBCD()+"&mid=${state.mid}&did=36&subid=${subid}&cmd=${cmd}&data=${data}",
		headers: ["Authorization": "Bearer ${state.authToken}"]
	]
    
    log.trace "Before Control HTTPGET to navien."
    def jsonData
    try{
        httpGet(pollParams) { resp ->
        	debugEvent ("Response (resp.data.Staus) : = ${resp.data.Staus}", true)
            if (resp.status == 200)
         	{
            	log.debug "poll results returned"
                //atomicState.thermostats = resp.data.Status.inject([:]) { collector, stat ->
				atomicState.thermostats = "1".inject([:]) { collector, stat ->
					def dni = [getChildName()].join('.')

					log.debug "updating dni $dni"

					def response = statusSetting(resp.data.Status)

					log.debug ("Event Data = ${response}")

					collector[dni] = [data:response]
					return collector
                }
                
                log.debug "updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
            }
            else
			{
				log.error "polling children & got http status ${resp.status}"

				//refresh the auth token
				if(resp.status == 400)
                {
                	log.debug "Bad Request Description"
                }
                else if(resp.status == 401)
                {
                	log.debug "Unauthorized Description"
                }
                else if(resp.status == 500)
                {
                	log.debug "InternalServerError Description"
                    atomicState.action = "pollChildren";
                    refreshAuthToken()
                }
                else
				{
					log.error "Authentication error, invalid authentication method, lack of credentials, etc."
				}
			}
        }       
    }
	catch(all)
	{
		log.debug "___exception polling children: "
		//refreshAuthToken()
	}
}

private refreshAuthToken() {
	log.trace "refreshing auth token"

	if(!atomicState.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {      
        def refreshParams = [
            uri: getServerUrl(),
            path: "/Token",
            headers: ['Content-Type': "application/x-www-form-urlencoded"],
            body: [grant_type: "refresh_token", refresh_token: "${state.refreshToken}"]
		]

		log.debug refreshParams

		try {
			def jsonMap
			httpPost(refreshParams) { resp ->

				if(resp.status == 200) {
					log.debug "Token refreshed...calling saved RestAction now! ${resp}"

					if(resp.data) 
                    {
                    	jsonMap = resp.data
                    	state.refreshToken = jsonMap.refresh_token
                    	state.authToken = jsonMap.access_token

						if(atomicState.action && atomicState.action != "") {
							log.debug "Executing next action: ${atomicState.action}"
							"{atomicState.action}"()

							//remove saved action
							atomicState.action = ""
						}
					}
					atomicState.action = ""
				} 
                else 
                {
					log.debug "refresh failed ${resp.status} : ${resp.status.code}"
				}
			}
		}
		catch(Exception e) {
			log.debug "caught exception refreshing auth token: " + e.getStackTrace()
		}
	}
}

def statusSetting(status){
	/*
	log.debug "state.status ====> ${state.status}"
    log.debug "제품아이디 ====> ${state.status.substring(0, 16)}"       // 제품아이디
    log.debug "보일러모델타입 ====> ${state.status.substring(26, 28)}"   // 보일러모델타입
    log.debug "에러코드 ====> ${state.status.substring(32, 36)}"        // 에러코드
    log.debug "온수설정온도 ====> ${state.status.substring(36, 38)}"     // 온수설정온도
    log.debug "state.status ====> ${state.status.substring(38, 40)}"   // 난방세기
    log.debug "state.status ====> ${state.status.substring(40, 42)}"   // 옵션기능
    log.debug "작동모드 ====> ${state.status.substring(42, 44)}"        // 작동모드
    log.debug "현재실내온도 ====> ${state.status.substring(44, 46)}"     // 현재실내온도
    log.debug "실내난방설정온도 ====> ${state.status.substring(46, 48)}"  // 실내난방설정온도
    log.debug "온돌난방설정온도 ====> ${state.status.substring(48, 50)}"  // 온돌난방설정온도
	*/        
    state.mid              = status.substring(0, 16)
    state.boilerType       = status.substring(26, 28)				
    state.errorCode        = status.substring(32, 36)				
    state.hotWater         = convertHexToInt(status.substring(36, 38))
    
    def s = status.substring(42, 44)
    if(s == "01") state.thermostatStatus = "전원 OFF"
    else if(s == "02") state.thermostatStatus = "외출 ON"
    else if(s == "03") state.thermostatStatus = "실내난방"
    else if(s == "04") state.thermostatStatus = "온돌난방"
    else if(s == "05") state.thermostatStatus = "반복예약난방"
    else if(s == "06") state.thermostatStatus = "24시간예약난방"
    else if(s == "07") state.thermostatStatus = "간편예약난방"
    else if(s == "08") state.thermostatStatus = "온수전용"
    else if(s == "09") state.thermostatStatus = "빠른온수"
    else state.thermostatStatus = "---"
    
    state.temperature      = convertHexToInt(status.substring(44, 46))
    state.roomTemp         = convertHexToInt(status.substring(46, 48))
    state.ondolTemp        = convertHexToInt(status.substring(48, 50))
    
    def data = [
    	mid: state.mid,
        boilerType: state.boilerType,
        errorCode: state.errorCode,
        hotWater: state.hotWater,
        thermostatStatus: state.thermostatStatus,
        temperature: state.temperature,
        roomTemp: state.roomTemp,
        ondolTemp: state.ondolTemp
    ]
	return data
}

def navienConnecting(){
	log.debug "navienConnecting()"
    
    def connectParams = [
        uri: getServerUrl()+"/api/SmartTokApi?bcd="+getBCD()+"&uid=${state.userName}&scd=2",
		headers: ["Authorization": "Bearer ${state.authToken}"]
	]
    
    def result = [success:false]
    def jsonData
    
    httpGet(connectParams) { resp ->
        if (resp.status == 200)
		{
            jsonData = resp.data
            result.success = true
            state.status = jsonData.Status
        }
        else if(resp.status == 400)
        {
        	result.reason = "Bad Request"
        }
        else if(resp.status == 401)
        {
        	result.reason = "Unauthorized"
        }
        else if(resp.status == 500)
        {
        	result.reason = "Internal ServerError"
        }
        else
        {
        	result.reason = "Bad Connect"
        }
    }
    
    return result
}

private forceLogin(){
    log.debug "forceLogin()"
    
	updateCookie(null)
    login()
}

private updateCookie(String cookie){
	atomicState.cookie = cookie
    state.cookie = cookie
}

private login(){
	if(getCookieValueIsValid())
	{
		return [success:true]
	}
	return doLogin()
}

private doLogin() {
    log.debug "doLogin()"
    
	def loginParams = [
		uri: getServerUrl(),
		path: "/Token",
		headers: ['Content-Type': "application/x-www-form-urlencoded"],
		body: [grant_type: "password", username: username, password: password]
	]

	def result = [success:false]
	def jsonMap
    
    try
    {
        httpPost(loginParams) { resp ->
            if (resp.status == 200 && resp.headers.'Content-Type'.contains("application/json"))
            {
                log.debug "login 200 json headers: " + resp.headers.collect { "${it.name}:${it.value}" }
                def cookie = resp?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
                if (cookie) {
                    log.debug "login setting cookie to $cookie"
                    updateCookie(cookie)
                    result.success = true

                    jsonMap = resp.data
                    state.refreshToken = jsonMap.refresh_token
                    state.authToken = jsonMap.access_token
                    state.userName = jsonMap.userName
                }
                else
                {
                    // ERROR: any more information we can give?
                    result.reason = "Bad login"
                }
            }
            else
            {
                result.reason = "Bad login"
            }
        }
    }
    catch(groovyx.net.http.HttpResponseException hre)
    {
    	result.reason = "Exception"
    }

	return result
}

private Boolean getCookieValueIsValid()
{
	// TODO: make a call with the cookie to verify that it works
	return getCookieValue()
}

private getCookieValue(){
	state.cookie
}

def getChildNamespace() { "smartthings" }
def getChildName() { "Navien Room Controller" }

def getChildDeviceIdsString()
{
	log.debug "thermostats ====> ${thermostats}"
	return thermostats.collect { it.split(/\./).last() }.join(',')
}

def getServerUrl()           { return appSettings.serverUrl }
def getSmartThingsClientId() { return appSettings.clientId }
def getBCD()                 { return appSettings.bcd }

def debugEvent(message, displayEvent = true) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)
}

private convertHexToInt(hex) {
	return (Integer.parseInt(hex,16) / 2)
}