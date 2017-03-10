/**
 *	My ecobee Init (Service Manager)
 *
 *	Authors: scott, Yracine
 *	Date: 2013-08-07
 *
 *  Last Modification: 
 *      JLH - 01-23-2014 - Update for Correct SmartApp URL Format
 *      JLH - 02-15-2014 - Fuller use of ecobee API
 *		Y.Racine Nov 2014 - Simplified the Service Manager as much as possible to reduce tight coupling with 
 *							its child device types (device handlers) for better performance and reliability.
 *		Y.Racine Dec. 2015 - Major Changes to use new initialize and callback endpoints (auth standards at ST).
 *                            Also, better auth tokens management to avoid any auth exceptions
 *      LinkedIn profile: ca.linkedin.com/pub/yves-racine-m-sc-a/0/406/4b/
 *
 *  Software Distribution is restricted and shall be done only with Developer's written approval.
 *
**/
definition(
    name: "MyEcobeeInit",
    namespace: "yracine",
    author: "Yves Racine",
    description: "Connect your Ecobee thermostat to SmartThings.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png"
)
preferences {
	page(name: "about", title: "About", nextPage: "auth")
	page(name: "auth", title: "ecobee", content:"authPage", nextPage:"deviceList")
	page(name: "deviceList", title: "ecobee", content:"ecobeeDeviceList",nextPage: "otherSettings")
	page(name: "otherSettings", title: "Other Settings", content:"otherSettings", install:true)
}

mappings {
    path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
    path("/oauth/callback") {action: [GET: "callback"]}
}

def about() {
 	dynamicPage(name: "about", install: false, uninstall: true) {
 		section("About") {	
			paragraph "My Ecobee Init, the smartapp that connects your Ecobee thermostat to SmartThings via cloud-to-cloud integration"
			paragraph "Version 2.8.9\n" 
			paragraph "If you like this smartapp, please support the developer via PayPal and click on the Paypal link below " 
				href url: "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=yracine%40yahoo%2ecom&lc=US&item_name=Maisons%20ecomatiq&no_note=0&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest",
					title:"Paypal donation..."
			paragraph "Copyright�2014 Yves Racine"
				href url:"http://github.com/yracine/device-type.myecobee", style:"embedded", required:false, title:"More information...", 
					description: "http://github.com/yracine/device-type.myecobee"
		}
	}        
}

def otherSettings() {
	dynamicPage(name: "otherSettings", title: "Other Settings", install: true, uninstall: false) {
		section("Polling at which interval in minutes (range=[5,10,15,30],default=20 min.)?") {
			input "givenInterval", "number", title:"Interval", required: false
		}
		section("Handle/Notify any exception proactively [default=false, you will not receive any exception notification]") {
			input "handleExceptionFlag", "bool", title: "Handle exceptions proactively?", required: false
		}
		section("What do I use as Master on/off switch to restart smartapp processing? [optional]") {
			input (name:"powerSwitch", type:"capability.switch", required: false, description: "Optional")
		}
		section("What do I use as temperature polling sensor to restart smartapp processing? [optional]") {
			input (name:"tempSensor", type:"capability.temperatureMeasurement", required: false, multiple:true, description: "Optional")
		}
		section("What do I use as energy polling sensor to restart smartapp processing? [optional]") {
			input (name:"energyMeter", type:"capability.powerMeter", required: false, multiple:true, description: "Optional")
		}
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required:
				false
			input "phoneNumber", "phone", title: "Send a text message?", required: false
		}
		section([mobileOnly:true]) {
			label title: "Assign a name for this SmartApp", required: false
		}
	}
}

def authPage() {
	log.debug "authPage(),atomicState.oauthTokenProvided=${atomicState?.oauthTokenProvided}"

	if (!atomicState.accessToken) {
		log.debug "about to create access token"
		createAccessToken()
		atomicState.accessToken = state.accessToken
	}

	def description = "Required"
	def uninstallAllowed = false

	if (atomicState.authToken) {

		// TODO: Check if it's valid
		if (true) {
			description = "You are connected."
			uninstallAllowed = true
			atomicState?.oauthTokenProvided=true            
		} else {
			description = "Required" // Worth differentiating here vs. not having atomicState.authToken? 
		}
	}

    def redirectUrl = "${get_ST_URI_ROOT()}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${getServerUrl()}"

	log.debug "authPage>atomicState.authToken=${atomicState.authToken},atomicState.oauthTokenProvided=${atomicState?.oauthTokenProvided}, RedirectUrl = ${redirectUrl}"


	// get rid of next button until the user is actually auth'd

	if (!atomicState?.oauthTokenProvided) {

		return dynamicPage(name: "auth", title: "Login", nextPage:null, uninstall:uninstallAllowed, submitOnChange: true) {
			section(){
				paragraph "Tap below to log in to the ecobee portal and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title:"ecobee", description:description
			}
		}

	} else {

		return dynamicPage(name: "auth", title: "Log In", nextPage:"deviceList", uninstall:uninstallAllowed,submitOnChange: true) {
			section(){
				paragraph "Tap Next to continue to setup your thermostats."
				href url:redirectUrl, style:"embedded", state:"complete", title:"ecobee", description:description
			}
		}

	}


}

def ecobeeDeviceList() {
	log.debug "ecobeeDeviceList>begin"

	def stats = getEcobeeThermostats()

	log.debug "ecobeeDeviceList>device list: $stats"

	def ems = getEcobeeThermostats("ems")

	log.debug "ecobeeDeviceList>device list: $ems"

	stats = stats + ems
	def p = dynamicPage(name: "deviceList", title: "Select Your Thermostats [3 max; if you have disconnect issue, create 1 instance of MyEcobeeInit per tstat].", uninstall: true) {
		section(""){
			paragraph "Tap below to see the list of ecobee thermostats available in your ecobee account"
			input(name: "thermostats", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:stats])
		}
	}

	log.debug "list p: $p"
	return p
}


def setParentAuthTokens(auth_data) {
	if (auth_data.authexptime > atomicState.authexptime) {
		if (handleException) {
/*
			For Debugging purposes, due to the fact that logging is not working when called (separate thread)
			send("MyEcobeeInit>setParentAuthTokens>begin auth_data: $auth_data")
*/
			log.debug("setParentAuthTokens>begin auth_data: $auth_data")
		}            
		atomicState.refreshToken = auth_data?.refresh_token
		atomicState.authToken = auth_data?.access_token
		atomicState.expiresIn=auth_data?.expires_in
		atomicState.tokenType = auth_data?.token_type
		atomicState.authexptime= auth_data?.authexptime
		refreshAllChildAuthTokens()
		if (handleException) {
/*
			For Debugging purposes, due to the fact that logging is not working when called (separate thread)
			send("MyEcobeeInit>setParentAuthTokens>atomicState =$atomicState")
*/
			log.debug("MyEcobeeInit>setParentAuthTokens>atomicState =$atomicState")
		}            
	}        

}

def refreshAllChildAuthTokens() {
/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	send("refreshAllChildAuthTokens>begin updating children with $atomicState")
*/

	def children= getChildDevices()
/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	send("refreshAllChildAuthtokens> refreshing ${children.size()} thermostats")
*/

	children.each { 
/*
		For Debugging purposes, due to the fact that logging is not working when called (separate thread)
		send("refreshAllChildAuthTokens>begin updating $it.deviceNetworkId with $atomicState")
*/
    	it.refreshChildTokens(atomicState) 
	}
}
def refreshThisChildAuthTokens(child) {
/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	send("refreshThisChildAuthTokens>begin child id: ${child.device.deviceNetworkId}, updating it with ${atomicState}")
*/
	child.refreshChildTokens(atomicState)

/*
	For Debugging purposes, due to the fact that logging is not working when called (separate thread)
	send("refreshThisChildAuthTokens>end")
*/
}

boolean refreshParentTokens() {

	if (isTokenExpired()) {
		if (refreshAuthToken()) {
			refreshAllChildAuthTokens()
			return true            
		}		        
	}
	return false    
    
}
def getEcobeeThermostats(String type="") {
	log.debug "getEcobeeThermostats>getting device list"
	def msg
    
	def requestBody

	if ((type == "") || (type == null)) {
    
	 	requestBody = '{"selection":{"selectionType":"registered","selectionMatch":""}}'
	} else {
		requestBody = '{"selection":{"selectionType":"managementSet","selectionMatch":"/"}}'
	}    
    
	def deviceListParams = [
		uri: "${get_URI_ROOT()}",
		path: "/1/thermostat",
		headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
		query: [format: 'json', body: requestBody]
	]

	log.debug "getEcobeeThermostats>device list params: $deviceListParams"

	def stats = [:]
	try {
		httpGet(deviceListParams) { resp ->

			if (resp.status == 200) {
				resp.data.thermostatList.each { stat ->
					def dni = [ app.id, stat.name, stat.identifier ].join('.')
					def tstatName = getThermostatDisplayName(stat)
					log.debug "getEcobeeThermostats>found ${tstatName}..."
					stats[dni] = tstatName
				}
				                
			} else {
				log.debug "getEcobeeThermostats>http status: ${resp.status}"

				//refresh the auth token
				if (resp.status == 500 || resp.data.status.code == 14) {
					if (handleException) {            
						send "MyEcobeeInit>http status=${resp.status}: need to refresh your auth_token, about to call refreshAuthToken() (resp status= ${resp.data.status.code})"
					}                        
					log.debug "getEcobeeThermostats>Need to refresh your auth_token!, about to call refreshAuthToken()"
					refreshAuthToken()
                    
				} else {
					if (handleException) {            
						send "MyEcobeeInit>http status=${resp.status}: authentication error, invalid authentication method, lack of credentials, (resp status= ${resp.data.status.code})"
					}                        
					log.error "getEcobeeThermostats>http status=${resp.status}: authentication error, invalid authentication method, lack of credentials,  (resp status= ${resp.data.status.code})"
				}
			}
    	}        
	} catch (java.net.UnknownHostException e) {
		msg ="Unknown host - check the URL " + deviceListParams.uri
		log.error msg   
		send "MyEcobeeInit>${msg}"        
	} catch (java.net.NoRouteToHostException t) {
		msg= "No route to host - check the URL " + deviceListParams.uri
		log.error msg        
		send "MyEcobeeInit>${msg}"        
	} catch (java.io.IOException e) {
		log.debug "getEcobeeThermostats>$e while getting list of thermostats, probable cause: not the right account for this type (${type}) of thermostat " +
			deviceListParams            
	} catch (e) {
		msg= "exception $e while getting list of thermostats" 
		if (handleException) {            
			send "MyEcobeeInit>http error status=${resp.status}: exception $e while getting list of thermostats)"
		}                        
		log.error msg        
		send "MyEcobeeInit>${msg}"        
    }

	log.debug "thermostats: $stats"

	return stats
}


def refreshAuthToken() {
	log.debug "refreshAuthToken>about to refresh auth token"
	boolean result=false
	def REFRESH_SUCCESS_CODE=200    
	def UNAUTHORIZED_CODE=401    
    
	def stcid = getSmartThingsClientId()

	def refreshParams = [
			method: 'POST',
			uri   : "${get_URI_ROOT()}",
			path  : "/token",
			query : [grant_type: 'refresh_token', code: "${atomicState.refreshToken}", client_id: stcid]
	]


	def jsonMap
    
	try {
    
		httpPost(refreshParams) { resp ->

			if (resp.status == REFRESH_SUCCESS_CODE) {
				log.debug "refreshAuthToken>Token refresh done resp = ${resp}"

				jsonMap = resp.data

				if (resp.data) {

					log.debug "refreshAuthToken>resp.data"
					atomicState.refreshToken = resp?.data?.refresh_token
					atomicState.authToken = resp?.data?.access_token
					atomicState.expiresIn=resp?.data?.expires_in
					atomicState.tokenType = resp?.data?.token_type
					def authexptime = new Date((now() + (resp?.data?.expires_in  * 1000))).getTime()
					atomicState.authexptime=authexptime 						                        
					log.debug("refreshAuthToken>new refreshToken = ${atomicState.refreshToken}")
					log.debug("refreshAuthToken>new authToken = ${atomicState.authToken}")
					if (handleException) {                        
						send("MyEcobeeInit>refreshAuthToken>,new authToken = ${atomicState.authToken}")
						send("refreshAuthToken>new authexptime = ${atomicState.authexptime}")
					}                            
					log.debug("refreshAuthToken>new authexptime = ${atomicState.authexptime}")
					result=true                    

				} /* end if resp.data */
			} else { 
				result=false                    
				log.debug "refreshAuthToken>refreshAuthToken failed ${resp.status} : ${resp.status.code}"
				if (handleException) {            
					send("MyEcobeeinit>refreshAuthToken failed ${resp.status} : ${resp.status.code}")
				} /* end handle expception */                        
			} /* end if resp.status==200 */
		} /* end http post */
	} catch (groovyx.net.http.HttpResponseException e) {
			log.error "refreshAuthToken> error: e.statusCode ${e.statusCode}"
			atomicState.exceptionCount=atomicState.exceptionCount+1             
			if (e.statusCode == UNAUTHORIZED_CODE) { //this issue might comes from exceed 20sec app execution, connectivity issue etc
				log.error "refreshAuthToken>exception $e"
				if (handleException) {            
					sendEvent name: "verboseTrace", value:
						"refreshAuthToken>exception $e"
				}            
			}            
	}    
    
    return result
}


def getThermostatDisplayName(stat) {		
	def tstatName 
	if (stat?.name) {
		tstatName= stat.name.toString()
	} else {
		tstatName="${getThermostatTypeName(stat)} (${stat.identifier})"
	}    
	log.debug "getThermostatDisplayName>${tstatName}"

	return tstatName
}

def getThermostatId(stat) {
	def id =stat?.idenfifier
	log.debug "getThermostatId>${id}"
	return id
}

def getThermostatTypeName(stat) {
	def model = stat.modelNumber == "siSmart" ? "Smart Si" : (stat.modelNumber=="idtSmart") ? "Smart" : (stat.modelNumber=="athenaSmart") ? "Ecobee3" : "Ems"
	log.debug "getThermostatTypeName>${model}"
	return model 
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	try {    
		unschedule()
	} catch (e) {
		log.debug "updated>exception $e, continue processing"    
	}    
	initialize()
}

def offHandler(evt) {
	log.debug "$evt.name: $evt.value"
}



def rescheduleHandler(evt) {
	log.debug "$evt.name: $evt.value"
	rescheduleIfNeeded()		
}


private def delete_child_devices() {
	def delete
    
	// Delete any that are no longer in settings

	if(!thermostats) {
		log.debug "delete_child_devices>delete all ecobee thermostats"
		delete = getAllChildDevices()
	} else {
		delete = getChildDevices().findAll { !thermostats.contains(it.deviceNetworkId) }
	}


	delete.each { 
		try {    
			deleteChildDevice(it.deviceNetworkId) 
		} catch (e) {
			log.error "delete_child_devices>exception $e while deleting ecobee thermostat ${it.deviceNetworkId}"
			send "MyEcobeeInit>exception $e while deleting ecobee thermostat ${it.deviceNetworkId}"
		}   
	}
	log.debug "delete_child_devices>deleted ${delete.size()} ecobee thermostats"


}

private def create_child_devices() {

   	int countNewChildDevices =0
	def devices = thermostats.collect { dni ->

		def d = getChildDevice(dni)
		log.debug "create_child_devices>looping thru thermostats, found id $dni"

		if(!d) {
			def tstat_info  = dni.tokenize('.')
			def thermostatId = tstat_info.last()
 			def name = tstat_info[1]
			def labelName = 'My ecobee ' + "${name}"
			log.debug "create_child_devices>about to create child device with id $dni, thermostatId = $thermostatId, name=  ${name}"
			d = addChildDevice(getChildNamespace(), getChildName(), dni, null,
				[label: "${labelName}"]) 
			d.initialSetup( getSmartThingsClientId(), atomicState, thermostatId ) 	// initial setup of the Child Device
			log.debug "create_child_devices>created ${d.displayName} with id $dni"
			countNewChildDevices++            
		} else {
			log.debug "create_child_devices>found ${d.displayName} with id $dni already exists"
			try {
				if (d.isTokenExpired()) {  // called refreshAllChildAuthTokens when updated
 					refreshAllChildAuthTokens()    
				}
			} catch (e) {
				log.debug "create_child_devices>exception $e while trying to refresh existing tokens in child $d"
            
			}            
		}

	}

	log.debug "create_child_devices>created $countNewChildDevices, total=${devices.size()} thermostats"
	    

}

def initialize() {
    
	log.debug "initialize"
	atomicState?.exceptionCount=0    
	def msg
	atomicState?.poll = [ last: 0, rescheduled: now() ]
    
	Integer delay = givenInterval ?: 20 // By default, do it every 20 min.
	delete_child_devices()	
	create_child_devices()
    
	//Subscribe to different events (ex. sunrise and sunset events) to trigger rescheduling if needed
	subscribe(location, "sunrise", rescheduleIfNeeded)
	subscribe(location, "sunset", rescheduleIfNeeded)
	subscribe(location, "mode", rescheduleIfNeeded)
	subscribe(location, "sunriseTime", rescheduleIfNeeded)
	subscribe(location, "sunsetTime", rescheduleIfNeeded)
	if (powerSwitch) {
		subscribe(powerSwitch, "switch.off", rescheduleHandler, [filterEvents: false])
		subscribe(powerSwitch, "switch.on", rescheduleHandler, [filterEvents: false])
	}
	if (tempSensor)	{
		subscribe(tempSensor,"temperature", rescheduleHandler,[filterEvents: false])
	}
	if (energyMeter)	{
		subscribe(energyMeter,"energy", rescheduleHandler,[filterEvents: false])
	}

	subscribe(app, appTouch)

	log.trace "initialize>polling delay= ${delay}..."
	rescheduleIfNeeded()   
}

def appTouch(evt) {
	rescheduleIfNeeded()
}

def rescheduleIfNeeded(evt) {
	if (evt) log.debug("rescheduleIfNeeded>$evt.name=$evt.value")
	Integer delay = givenInterval ?: 5 // By default, do it every 5 min.
	BigDecimal currentTime = now()    
	BigDecimal lastPollTime = (currentTime - (atomicState?.poll["last"]?:0))  
	if (lastPollTime != currentTime) {    
		Double lastPollTimeInMinutes = (lastPollTime/60000).toDouble().round(1)      
		log.info "rescheduleIfNeeded>last poll was  ${lastPollTimeInMinutes.toString()} minutes ago"
	}
	if (((atomicState?.poll["last"]?:0) + (delay * 60000) < currentTime) && canSchedule()) {
		log.info "rescheduleIfNeeded>scheduling takeAction in ${delay} minutes.."
		if ((delay >=5) && (delay <10)) {      
			runEvery5Minutes(takeAction)
		} else if ((delay >=10) && (delay <15)) {  
			runEvery10Minutes(takeAction)
		} else if ((delay >=15) && (delay <30)) {  
			runEvery15Minutes(takeAction)
		} else {  
			runEvery30Minutes(takeAction)
		}
		takeAction()
	}
    
    
	// Update rescheduled state
    
	if (!evt) atomicState.poll["rescheduled"] = now()
}



def takeAction() {
	log.trace "takeAction>begin"
	def msg, exceptionCheck    
	def MAX_EXCEPTION_COUNT=50
	boolean handleException = (handleExceptionFlag)?: false
    
	Integer delay = givenInterval ?: 5 // By default, do it every 5 min.
	atomicState?.poll["last"] = now()
		
	//schedule the rescheduleIfNeeded() function
    
	if (((atomicState?.poll["rescheduled"]?:0) + (delay * 60000)) < now()) {
		log.info "takeAction>scheduling rescheduleIfNeeded() in ${delay} minutes.."
		unschedule()        
		schedule("0 0/${delay} * * * ?", rescheduleIfNeeded)
		// Update rescheduled state
		atomicState?.poll["rescheduled"] = now()
	}
    
    

	def devices = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		log.debug "takeAction>Looping thru thermostats, found id $dni, about to poll"
		d.poll()
		exceptionCheck = d.currentVerboseTrace.toString()
		if (handleException) {            
			if ((exceptionCheck) && ((exceptionCheck.contains("exception") || (exceptionCheck.contains("error")) && 
				(!exceptionCheck.contains("Java.util.concurrent.TimeoutException")) && 
				(!exceptionCheck.contains("UndeclaredThrowableException"))))) {  
			// check if there is any exception or an error reported in the verboseTrace associated to the device (except the ones linked to rate limiting).
				atomicState.exceptionCount=atomicState.exceptionCount+1    
				log.error "found exception/error after polling, exceptionCount= ${atomicState?.exceptionCount}: $exceptionCheck" 
			} else {             
				// reset exception counter            
				atomicState?.exceptionCount=0      
			}                
		}   /* end if handleException */             
	}
	if (handleException) {    
		if ((exceptionCheck) && (exceptionCheck.contains("Unauthorized")) && (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT)) {
			// need to authenticate again    
			atomicState.authToken=null                    
			atomicState?.oauthTokenProvided=false
			msg="$exceptionCheck after ${atomicState?.exceptionCount} errors, press on 'ecobee' and re-login..." 
			send "MyEcobeeInit> ${msg}"
			log.error msg
		} else if (atomicState?.exceptionCount>=MAX_EXCEPTION_COUNT) {
			msg="too many exceptions/errors, $exceptionCheck (${atomicState?.exceptionCount} errors so far), you may need to press on 'ecobee' and re-login..." 
			send "MyEcobeeInit> ${msg}"
			log.error msg
		}
	} /* end if handleException */        
	log.trace "takeAction>end"

}


private send(msg) {
	if (sendPushMessage != "No") {
		log.debug("sending push message")
		sendPush(msg)

	}

	if (phoneNumber) {
		log.debug("sending text message")
		sendSms(phoneNumber, msg)
	}

	log.debug msg
}

def isTokenExpired() {
	def buffer_time_expiration=5  // set a 5 min. buffer time before token expiration to avoid auth_err 
	def time_check_for_exp = now() + (buffer_time_expiration * 60 * 1000);
	log.debug "isTokenExpired>expiresIn timestamp: ${atomicState.authexptime} > timestamp check for exp: ${time_check_for_exp}?"
	if (atomicState?.authexptime > time_check_for_exp) {
		log.debug "isTokenExpired>not expired"
//		send "isTokenExpired>not expired in MyEcobeeInit"
		return false
	}
	log.debug "isTokenExpired>expired"
//	send "isTokenExpired>expired in MyEcobeeInit"
	return true    
}


def oauthInitUrl() {
	log.debug "oauthInitUrl>begin"
	def stcid = getSmartThingsClientId();

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
		response_type: "code",
		scope: "ems,smartWrite",
		client_id: stcid,
		state: atomicState.oauthInitState,
		redirect_uri: "${get_ST_URI_ROOT()}/oauth/callback"
	]

	redirect(location: "${get_URI_ROOT()}/authorize?${toQueryString(oauthParams)}")
}


def callback() {
	log.debug "callback>swapping token: $params"
	debugEvent ("swapping token: $params", true)

	def code = params.code
    def oauthState = params.state

    // Validate the response from the 3rd party by making sure oauthState == atomicState.oauthInitState as expected
    if (oauthState == atomicState.oauthInitState){

		def stcid = getSmartThingsClientId()

		def tokenParams = [
			grant_type: "authorization_code",
			code: params.code,
			client_id: stcid,
            redirect_uri: "${get_ST_URI_ROOT()}/oauth/callback"            
		]
		def tokenUrl = "${get_URI_ROOT()}/token?" + toQueryString(tokenParams)

		log.debug "callback>Swapping token $params"

		def jsonMap
		httpPost(uri:tokenUrl) { resp ->
			jsonMap = resp.data
			atomicState.refreshToken = jsonMap.refresh_token
			atomicState.authToken = jsonMap.access_token
			atomicState.expiresIn=jsonMap.expires_in
			atomicState.tokenType = jsonMap.token_type
			def authexptime = new Date((now() + (jsonMap.expires_in * 1000))).getTime()
			atomicState.authexptime = authexptime
		}
		success()

    } else {
        log.error "callback() failed. Validation of state did not match. oauthState != state.oauthInitState"
    }

}

def success() {

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
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/ecobee%402x.png" width="216" height="216" alt="ecobee icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
		<p>Your ecobee Account is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	</div>
</body>
</html>
"""

	render contentType: 'text/html', data: html
}


def fail() {
	def message = """
		<p>There was an error connecting your ecobee account with SmartThings</p>
		<p>Please try again.</p>
	"""
	displayMessageAsHtml(message)
}

def displayMessageAsHtml(message) {
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
}

def getChildDeviceIdsString() {
	return thermostats.collect { it.split(/\./).last() }.join(',')
}

def toJson(Map m) {
	return new org.codehaus.groovy.grails.web.json.JSONObject(m).toString()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def getChildNamespace() { "yracine" }
def getChildName() { "My Ecobee Device" }

def getServerUrl() { return getApiServerUrl()  }

def getSmartThingsClientId() { "qqwy6qo0c2lhTZGytelkQ5o8vlHgRsrO" }


def debugEvent(message, displayEvent) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)
}
private def get_URI_ROOT() {
	return "https://api.ecobee.com"
}
private def get_ST_URI_ROOT() {
	return "https://graph.api.smartthings.com"
}