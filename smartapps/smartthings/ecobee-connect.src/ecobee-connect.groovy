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
 *	Ecobee Service Manager
 *
 *	Author: scott
 *	Date: 2013-08-07
 *
 *  Last Modification:
 *      JLH - 01-23-2014 - Update for Correct SmartApp URL Format
 *      JLH - 02-15-2014 - Fuller use of ecobee API
 */
definition(
    name: "Ecobee (Connect)",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Connect your Ecobee thermostat to SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png",
    singleInstance: true
) {
	appSetting "clientId"
	appSetting "serverUrl"
}

preferences {
	page(name: "auth", title: "ecobee", nextPage:"deviceList", content:"authPage", uninstall: true)
	page(name: "deviceList", title: "ecobee", content:"ecobeeDeviceList", install:true)
}

mappings {
	path("/auth") {
		action: [
		  GET: "auth"
		]
	}
	path("/swapToken") {
		action: [
			GET: "swapToken"
		]
	}
}

def auth() {
	redirect location: oauthInitUrl()
}

def authPage()
{
	log.debug "authPage()"

	if(!atomicState.accessToken)
	{
		log.debug "about to create access token"
		createAccessToken()
		atomicState.accessToken = state.accessToken
	}


	def description = "Required"
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(atomicState.authToken)
	{
		// TODO: Check if it's valid
		if(true)
		{
			description = "You are connected."
			uninstallAllowed = true
			oauthTokenProvided = true
		}
		else
		{
			description = "Required" // Worth differentiating here vs. not having atomicState.authToken?
			oauthTokenProvided = false
		}
	}

	def redirectUrl = buildRedirectUrl("auth")

	log.debug "RedirectUrl = ${redirectUrl}"

	// get rid of next button until the user is actually auth'd

	if (!oauthTokenProvided) {

		return dynamicPage(name: "auth", title: "Login", nextPage:null, uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap below to log in to the ecobee service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title:"ecobee", description:description
			}
		}

	} else {

		return dynamicPage(name: "auth", title: "Log In", nextPage:"deviceList", uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap Next to continue to setup your thermostats."
				href url:redirectUrl, style:"embedded", state:"complete", title:"ecobee", description:description
			}
		}

	}

}

def ecobeeDeviceList()
{
	log.debug "ecobeeDeviceList()"

	def stats = getEcobeeThermostats()

	log.debug "device list: $stats"

	def p = dynamicPage(name: "deviceList", title: "Select Your Thermostats", uninstall: true) {
		section(""){
			paragraph "Tap below to see the list of ecobee thermostats available in your ecobee account and select the ones you want to connect to SmartThings."
			input(name: "thermostats", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:stats])
		}
	}

	log.debug "list p: $p"
	return p
}

def getEcobeeThermostats()
{
	log.debug "getting device list"

	def requestBody = '{"selection":{"selectionType":"registered","selectionMatch":"","includeRuntime":true}}'

	def deviceListParams = [
		uri: "https://api.ecobee.com",
		path: "/1/thermostat",
		headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
		query: [format: 'json', body: requestBody]
	]

	log.debug "_______AUTH______ ${atomicState.authToken}"
	log.debug "device list params: $deviceListParams"

	def stats = [:]
	httpGet(deviceListParams) { resp ->

		if(resp.status == 200)
		{
			resp.data.thermostatList.each { stat ->
				def dni = [ app.id, stat.identifier ].join('.')
				stats[dni] = getThermostatDisplayName(stat)
			}
		}
		else
		{
			log.debug "http status: ${resp.status}"

			//refresh the auth token
			if (resp.status == 500 && resp.data.status.code == 14)
			{
				log.debug "Storing the failed action to try later"
				atomicState.action = "getEcobeeThermostats"
				log.debug "Refreshing your auth_token!"
				refreshAuthToken()
			}
			else
			{
				log.error "Authentication error, invalid authentication method, lack of credentials, etc."
			}
		}
	}

	log.debug "thermostats: $stats"

	return stats
}

def getThermostatDisplayName(stat)
{
	log.debug "getThermostatDisplayName"
	if(stat?.name)
	{
		return stat.name.toString()
	}

	return (getThermostatTypeName(stat) + " (${stat.identifier})").toString()
}

def getThermostatTypeName(stat)
{
	log.debug "getThermostatTypeName"
	return stat.modelNumber == "siSmart" ? "Smart Si" : "Smart"
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	// createAccessToken()


	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
	log.debug "initialize"
	def devices = thermostats.collect { dni ->

		def d = getChildDevice(dni)

		if(!d)
		{
			d = addChildDevice(getChildNamespace(), getChildName(), dni)
			log.debug "created ${d.displayName} with id $dni"
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists"
		}

		return d
	}

	log.debug "created ${devices.size()} thermostats"

	def delete
	// Delete any that are no longer in settings
	if(!thermostats)
	{
		log.debug "delete thermostats"
		delete = getAllChildDevices()
	}
	else
	{
		delete = getChildDevices().findAll { !thermostats.contains(it.deviceNetworkId) }
	}

	log.debug "deleting ${delete.size()} thermostats"
	delete.each { deleteChildDevice(it.deviceNetworkId) }

	atomicState.thermostatData = [:]

	pollHandler()

	// schedule ("0 0/15 * 1/1 * ? *", pollHandler)
}


def oauthInitUrl()
{
	log.debug "oauthInitUrl"
	// def oauth_url = "https://api.ecobee.com/authorize?response_type=code&client_id=qqwy6qo0c2lhTZGytelkQ5o8vlHgRsrO&redirect_uri=http://localhost/&scope=smartRead,smartWrite&state=abc123"
	def stcid = getSmartThingsClientId();

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
		response_type: "code",
		scope: "smartRead,smartWrite",
		client_id: stcid,
		state: atomicState.oauthInitState,
		redirect_uri: buildRedirectUrl()
	]

	return "https://api.ecobee.com/authorize?" + toQueryString(oauthParams)
}

def buildRedirectUrl(action = "swapToken")
{
	log.debug "buildRedirectUrl"
	// return serverUrl + "/api/smartapps/installations/${app.id}/token/${atomicState.accessToken}"
	return serverUrl + "/api/token/${atomicState.accessToken}/smartapps/installations/${app.id}/${action}"
}

def swapToken()
{
	log.debug "swapping token: $params"
	debugEvent ("swapping token: $params")

	def code = params.code
	def oauthState = params.state

	// TODO: verify oauthState == atomicState.oauthInitState



	// https://www.ecobee.com/home/token?grant_type=authorization_code&code=aliOpagDm3BqbRplugcs1AwdJE0ohxdB&client_id=qqwy6qo0c2lhTZGytelkQ5o8vlHgRsrO&redirect_uri=https://graph.api.smartthings.com/
	def stcid = getSmartThingsClientId()

	def tokenParams = [
		grant_type: "authorization_code",
		code: params.code,
		client_id: stcid,
		redirect_uri: buildRedirectUrl()
	]

	def tokenUrl = "https://www.ecobee.com/home/token?" + toQueryString(tokenParams)

	log.debug "SCOTT: swapping token $params"

	def jsonMap
	httpPost(uri:tokenUrl) { resp ->
		jsonMap = resp.data
	}

	log.debug "SCOTT: swapped token for $jsonMap"
	debugEvent ("swapped token for $jsonMap")

	atomicState.refreshToken = jsonMap.refresh_token
	atomicState.authToken = jsonMap.access_token

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
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/ecobee%402x.png" alt="ecobee icon" />
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

def getPollRateMillis() { return 15 * 60 * 1000 }

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild( child )
{
	log.debug "poll child"
	debugEvent ("poll child")
	def now = new Date().time

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

def availableModes(child)
{

	debugEvent ("atomicState.Thermos = ${atomicState.thermostats}")

	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = atomicState.thermostats[child.device.deviceNetworkId]

	debugEvent("Data = ${tData}")

	if(!tData)
	{
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}

	def modes = ["off"]

	if (tData.data.heatMode) modes.add("heat")
	if (tData.data.coolMode) modes.add("cool")
	if (tData.data.autoMode) modes.add("auto")
	if (tData.data.auxHeatMode) modes.add("auxHeatOnly")

	modes

}


def currentMode(child)
{

	debugEvent ("atomicState.Thermos = ${atomicState.thermostats}")

	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = atomicState.thermostats[child.device.deviceNetworkId]

	debugEvent("Data = ${tData}")

	if(!tData)
	{
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}

	def mode = tData.data.thermostatMode

	mode

}



def pollChildren()
{
	log.debug "polling children"
	def thermostatIdsString = getChildDeviceIdsString()

	log.debug "polling children: $thermostatIdsString"


	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeExtendedRuntime":"true","includeSettings":"true","includeRuntime":"true"}}'

	// // TODO: test this:
	//
	// def jsonRequestBody = toJson([
	//	selection:[
	//		selectionType: "thermostats",
	//		   selectionMatch: getChildDeviceIdsString(),
	//		   includeRuntime: true
	//	   ]
	// ])
	log.debug "json Request: " + jsonRequestBody

	log.debug "State AuthToken: ${atomicState.authToken}"
	debugEvent "State AuthToken: ${atomicState.authToken}"


	def pollParams = [
		uri: "https://api.ecobee.com",
		path: "/1/thermostat",
		headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
		query: [format: 'json', body: jsonRequestBody]
	]

	debugEvent ("Before HTTPGET to ecobee.")

	try{
		httpGet(pollParams) { resp ->

			if (resp.data) {
				debugEvent ("Response from ecobee GET = ${resp.data}")
				debugEvent ("Response Status = ${resp.status}")
			}

			if(resp.status == 200) {
				log.debug "poll results returned"

				atomicState.thermostats = resp.data.thermostatList.inject([:]) { collector, stat ->

					def dni = [ app.id, stat.identifier ].join('.')

					log.debug "updating dni $dni"

					def data = [
						coolMode: (stat.settings.coolStages > 0),
						heatMode: (stat.settings.heatStages > 0),
						autoMode: stat.settings.autoHeatCoolFeatureEnabled,
						auxHeatMode: (stat.settings.hasHeatPump) && (stat.settings.hasForcedAir || stat.settings.hasElectric || stat.settings.hasBoiler),
						temperature: stat.runtime.actualTemperature / 10,
						heatingSetpoint: stat.runtime.desiredHeat / 10,
						coolingSetpoint: stat.runtime.desiredCool / 10,
						thermostatMode: stat.settings.hvacMode
					]

					debugEvent ("Event Data = ${data}")

					collector[dni] = [data:data]
					return collector
				}

				log.debug "updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
			}
			else
			{
				log.error "polling children & got http status ${resp.status}"

				//refresh the auth token
				if (resp.status == 500 && resp.data.status.code == 14)
				{
					log.debug "Storing the failed action to try later"
					atomicState.action = "pollChildren";
					log.debug "Refreshing your auth_token!"
					refreshAuthToken()
				}
				else
				{
					log.error "Authentication error, invalid authentication method, lack of credentials, etc."
				}
			}
		}
	}
	catch(Exception e)
	{
		log.debug "___exception polling children: " + e
		debugEvent ("${e}")

		refreshAuthToken()
	}

}

def pollHandler() {

	debugEvent ("in Poll() method.")
	pollChildren() // Hit the ecobee API for update on all thermostats

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

def getChildDeviceIdsString()
{
	return thermostats.collect { it.split(/\./).last() }.join(',')
}

def toJson(Map m)
{
	return new org.json.JSONObject(m).toString()
}

def toQueryString(Map m)
{
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private refreshAuthToken() {
	log.debug "refreshing auth token"
	debugEvent("refreshing OAUTH token")

	if(!atomicState.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {
		def stcid = getSmartThingsClientId()

		def refreshParams = [
				method: 'POST',
				uri   : "https://api.ecobee.com",
				path  : "/token",
				query : [grant_type: 'refresh_token', code: "${atomicState.refreshToken}", client_id: stcid],

				//data?.refreshToken
		]

		log.debug refreshParams

		//changed to httpPost
		try {
			def jsonMap
			httpPost(refreshParams) { resp ->

				if(resp.status == 200) {
					log.debug "Token refreshed...calling saved RestAction now!"

					debugEvent("Token refreshed ... calling saved RestAction now!")

					log.debug resp

					jsonMap = resp.data

					if(resp.data) {

						log.debug resp.data
						debugEvent("Response = ${resp.data}")

						atomicState.refreshToken = resp?.data?.refresh_token
						atomicState.authToken = resp?.data?.access_token

						debugEvent("Refresh Token = ${atomicState.refreshToken}")
						debugEvent("OAUTH Token = ${atomicState.authToken}")

						if(atomicState.action && atomicState.action != "") {
							log.debug "Executing next action: ${atomicState.action}"

							"{atomicState.action}"()

							//remove saved action
							atomicState.action = ""
						}

					}
					atomicState.action = ""
				} else {
					log.debug "refresh failed ${resp.status} : ${resp.status.code}"
				}
			}

			// atomicState.refreshToken = jsonMap.refresh_token
			// atomicState.authToken = jsonMap.access_token
		}
		catch(Exception e) {
			log.debug "caught exception refreshing auth token: " + e
		}
	}
}

def resumeProgram(child)
{

	def thermostatIdsString = getChildDeviceIdsString()
	log.debug "resumeProgram children: $thermostatIdsString"

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}]}'
	//, { "type": "sendMessage", "params": { "text": "Setpoint Updated" } }
	sendJson(jsonRequestBody)
}

def setHold(child, heating, cooling)
{

	int h = heating * 10
	int c = cooling * 10

	log.debug "setpoints____________ - h: $heating - $h, c: $cooling - $c"
	def thermostatIdsString = getChildDeviceIdsString()
	log.debug "setCoolingSetpoint children: $thermostatIdsString"



	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": "indefinite" } } ]}'

//	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}, { "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": "indefinite" } } ]}'

	sendJson(jsonRequestBody)
}

def setMode(child, mode)
{
	log.debug "requested mode = ${mode}"
	def thermostatIdsString = getChildDeviceIdsString()
	log.debug "setCoolingSetpoint children: $thermostatIdsString"


	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeRuntime":true},"thermostat": {"settings":{"hvacMode":"'+"${mode}"+'"}}}'

	log.debug "Mode Request Body = ${jsonRequestBody}"
	debugEvent ("Mode Request Body = ${jsonRequestBody}")

	def result = sendJson(jsonRequestBody)

	if (result) {
		def tData = atomicState.thermostats[child.device.deviceNetworkId]
		tData.data.thermostatMode = mode
	}

	return(result)
}

def changeSetpoint (child, amount)
{
	def tData = atomicState.thermostats[child.device.deviceNetworkId]

	log.debug "In changeSetpoint."
	debugEvent ("In changeSetpoint.")

	if (tData) {

		def thermostat = tData.data

		log.debug "Thermostat=${thermostat}"
		debugEvent ("Thermostat=${thermostat}")

		if (thermostat.thermostatMode == "heat") {
			thermostat.heatingSetpoint = thermostat.heatingSetpoint + amount
			child.setHeatingSetpoint (thermostat.heatingSetpoint)

			log.debug "New Heating Setpoint = ${thermostat.heatingSetpoint}"
			debugEvent ("New Heating Setpoint = ${thermostat.heatingSetpoint}")

		}
		else if (thermostat.thermostatMode == "cool") {
			thermostat.coolingSetpoint = thermostat.coolingSetpoint + amount
			child.setCoolingSetpoint (thermostat.coolingSetpoint)

			log.debug "New Cooling Setpoint = ${thermostat.coolingSetpoint}"
			debugEvent ("New Cooling Setpoint = ${thermostat.coolingSetpoint}")
		}
	}
}


def sendJson(String jsonBody)
{

	//log.debug "_____AUTH_____ ${atomicState.authToken}"

	def cmdParams = [
		uri: "https://api.ecobee.com",

		path: "/1/thermostat",
		headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
		body: jsonBody
	]

	def returnStatus = -1

	try{
		httpPost(cmdParams) { resp ->

			if(resp.status == 200) {

				log.debug "updated ${resp.data}"
				debugEvent("updated ${resp.data}")
				returnStatus = resp.data.status.code
				if (resp.data.status.code == 0)
					log.debug "Successful call to ecobee API."
				else {
					log.debug "Error return code = ${resp.data.status.code}"
					debugEvent("Error return code = ${resp.data.status.code}")
				}
			}
			else
			{
				log.error "sent Json & got http status ${resp.status} - ${resp.status.code}"
				debugEvent ("sent Json & got http status ${resp.status} - ${resp.status.code}")

				//refresh the auth token
				if (resp.status == 500 && resp.status.code == 14)
				{
					//log.debug "Storing the failed action to try later"
					log.debug "Refreshing your auth_token!"
					debugEvent ("Refreshing OAUTH Token")
					refreshAuthToken()
					return false
				}
				else
				{
					debugEvent ("Authentication error, invalid authentication method, lack of credentials, etc.")
					log.error "Authentication error, invalid authentication method, lack of credentials, etc."
					return false
				}
			}
		}
	}
	catch(Exception e)
	{
		log.debug "Exception Sending Json: " + e
		debugEvent ("Exception Sending JSON: " + e)
		return false
	}

	if (returnStatus == 0)
		return true
	else
		return false
}


def getChildNamespace() { "smartthings" }
def getChildName() { "Ecobee Thermostat" }

def getServerUrl() { return appSettings.serverUrl }
def getSmartThingsClientId() { appSettings.clientId }

def debugEvent(message, displayEvent = false) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}
