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
 *  Yoics Service Manager
 *
 *  Author: SmartThings
 *  Date: 2013-11-19
 */

definition(
    name: "Yoics (Connect)",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Connect and Control your Yoics Enabled Devices",
    category: "SmartThings Internal",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true,
    singleInstance: true
) {
  appSetting "serverUrl"
}

preferences {
	page(name: "auth", title: "Sign in", content: "authPage", uninstall:true)
	page(name: "page2", title: "Yoics Devices", install:true, content: "listAvailableCameras")
}


mappings {
	path("/foauth") {
		action: [
			GET: "foauth"
		]
	}
	path("/authorize") {
		action: [
			POST: "authorize"
		]
	}

}

def authPage()
{
	log.debug "authPage()"

	if(!state.accessToken)
	{
		log.debug "about to create access token"
		createAccessToken()
	}


	def description = "Required"

	if(getAuthHashValueIsValid())
	{
		// TODO: Check if it's valid
		if(true)
		{
			description = "Already saved"
		}
		else
		{
			description = "Required"
		}
	}

	def redirectUrl = buildUrl("", "foauth")

	return dynamicPage(name: "auth", title: "Yoics", nextPage:"page2") {
		section("Yoics Login"){
			href url:redirectUrl, style:"embedded", required:false, title:"Yoics", description:description
		}
	}

}

def buildUrl(String key, String endpoint="increment", Boolean absolute=true)
{
	if(key) {
		key = "/${key}"
	}

	def url = "/api/smartapps/installations/${app.id}/${endpoint}${key}?access_token=${state.accessToken}"

	if (q) {
		url += "q=${q}"
	}

	if(absolute)
	{
		url = serverUrl + url
	}

	return url
}

//Deprecated
def getServerName() {
	return getServerUrl()
}

def getServerUrl() {
  return appSettings.serverUrl
}

def listAvailableCameras() {

	//def loginResult = forceLogin()

	//if(loginResult.success)
	//{
		state.cameraNames = [:]

		def cameras = getDeviceList().inject([:]) { c, it ->
			def dni = [app.id, it.uuid].join('.')
			def cameraName = it.title ?: "Yoics"

			state.cameraNames[dni] = cameraName
			c[dni] = cameraName

			return c
		}

		return dynamicPage(name: "page2", title: "Yoics Devices", install:true) {
			section("Select which Yoics Devices to connect"){
				input(name: "cameras", title:"", type: "enum", required:false, multiple:true, metadata:[values:cameras])
			}
			section("Turn on which Lights when taking pictures")
			{
				input "switches", "capability.switch", multiple: true, required:false
			}
		}
	//}
	/*else
	{
		log.error "login result false"
		return [errorMessage:"There was an error logging in to Dropcam"]
	}*/

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
	removeChildDevices(getChildDevices())
}

def initialize() {

	if(!state.suppressDelete)
	{
		state.suppressDelete = [:]
	}

	log.debug "settings: $settings"

	def devices = cameras.collect { dni ->

		def name = state.cameraNames[dni] ?: "Yoics Device"

		def d = getChildDevice(dni)

		if(!d)
		{
			d = addChildDevice("smartthings", "Yoics Camera", dni, null, [name:"YoicsCamera", label:name])

			/* WE'LL GET PROXY ON TAKE REQUEST
			def setupProxyResult = setupProxy(dni)
			if(setupProxyResult.success)
			{
				log.debug "Setting up the proxy worked...taking image capture now?"

			}
			*/

			//Let's not take photos on add
			//d.take()

			log.debug "created ${d.displayName} with id $dni"
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists"
		}

		return d
	}

	log.debug "created ${devices.size()} dropcams"

	/* //Original Code seems to delete the dropcam that is being added */

	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !cameras?.contains(it.deviceNetworkId) }
	removeChildDevices(delete)
}

private removeChildDevices(delete)
{
	log.debug "deleting ${delete.size()} dropcams"
	delete.each {
		state.suppressDelete[it.deviceNetworkId] = true
		deleteChildDevice(it.deviceNetworkId)
		state.suppressDelete.remove(it.deviceNetworkId)
	}
}
private List getDeviceList()
{

	//https://apilb.yoics.net/web/api/getdevices.ashx?token=&filter=all&whose=me&state=%20all&type=xml

	def deviceListParams = [
		uri: "https://apilb.yoics.net",
		path: "/web/api/getdevices.ashx",
		headers: ['User-Agent': validUserAgent()],
		requestContentType: "application/json",
		query: [token: getLoginTokenValue(), filter: "all", whose: "me", state: "all", type:"json" ]
	]

	log.debug "cam list via: $deviceListParams"

	def multipleHtml
	def singleUrl
	def something
	def more

	def devices = []

	httpGet(deviceListParams) { resp ->

		log.debug "getting device list..."

		something = resp.status
		more = "headers: " + resp.headers.collect { "${it.name}:${it.value}" }

		if(resp.status == 200)
		{
			def jsonString = resp.data.str
			def body = new groovy.json.JsonSlurper().parseText(jsonString)

			//log.debug "get devices list response: ${jsonString}"
			//log.debug "get device list response: ${body}"

			body.NewDataSet.Table.each { d ->
				//log.debug "Addding ${d.devicealias} with address: ${d.deviceaddress}"
				devices << [title: d.devicealias, uuid: d.deviceaddress]	//uuid should be another name
			}

		}
		else
		{
			// ERROR
			log.error "camera list: unknown response"
		}

	}

	 log.debug "list: after getting cameras: " + [devices:devices, url:singleUrl, html:multipleHtml?.size(), something:something, more:more]

	// ERROR?
	return devices
}

def removeChildFromSettings(child)
{
	def device = child.device

	def dni = device.deviceNetworkId
	log.debug "removing child device $device with dni ${dni}"

	if(!state?.suppressDelete?.get(dni))
	{
		def newSettings = settings.cameras?.findAll { it != dni } ?: []
		app.updateSetting("cameras", newSettings)
	}
}

private forceLogin() {
	updateAuthHash(null)
	login()
}


private login() {

	if(getAuthHashValueIsValid())
	{
		return [success:true]
	}
	return doLogin()
}

/*private setupProxy(dni) {
	//https://apilb.yoics.net/web/api/connect.ashx?token=&deviceaddress=00:00:48:02:2A:A2:08:0E&type=xml

	def address = dni?.split(/\./)?.last()

	def loginParams = [
		uri: "https://apilb.yoics.net",
		path: "/web/api/connect.ashx",
		headers: ['User-Agent': validUserAgent()],
		requestContentType: "application/json",
		query: [token: getLoginTokenValue(), deviceaddress:address, type:"json" ]
	]

	def result = [success:false]

	httpGet(loginParams) { resp ->
		if (resp.status == 200) //&& resp.headers.'Content-Type'.contains("application/json")
		{
			log.debug "login 200 json headers: " + resp.headers.collect { "${it.name}:${it.value}" }
			def jsonString = resp.data.str
			def body = new groovy.json.JsonSlurper().parseText(jsonString)

			def proxy = body?.NewDataSet?.Table[0]?.proxy
			def requested = body?.NewDataSet?.Table[0]?.requested
			def expirationsec = body?.NewDataSet?.Table[0]?.expirationsec
			def url = body?.NewDataSet?.Table[0]?.url

			def proxyMap = [proxy:proxy, requested: requested, expirationsec:expirationsec, url: url]

			if (proxy) {
				//log.debug "setting ${dni} proxy to ${proxyMap}"
				//updateDeviceProxy(address, proxyMap)
				result.success = true
			}
			else
			{
				// ERROR: any more information we can give?
				result.reason = "Bad login"
			}
		}
		else
		{
			// ERROR: any more information we can give?
			result.reason = "Bad login"
		}


	}

	return result
}*/



private doLogin(user = "", pwd = "") { //change this name

	def loginParams = [
		uri: "https://apilb.yoics.net",
		path: "/web/api/login.ashx",
		headers: ['User-Agent': validUserAgent()],
		requestContentType: "application/json",
		query: [key: "SmartThingsApplication", usr: username, pwd: password, apilevel: 12, type:"json" ]
	]

	if (user) {
		loginParams.query = [key: "SmartThingsApplication", usr: user, pwd: pwd, apilevel: 12, type:"json" ]
	}

	def result = [success:false]

	httpGet(loginParams) { resp ->
		if (resp.status == 200) //&& resp.headers.'Content-Type'.contains("application/json")
		{
			log.debug "login 200 json headers: " + resp.headers.collect { "${it.name}:${it.value}" }
			def jsonString = resp.data.str
			def body = new groovy.json.JsonSlurper().parseText(jsonString)

			log.debug "login response: ${jsonString}"
			log.debug "login response: ${body}"

			def authhash = body?.NewDataSet?.Table[0]?.authhash //.token

			//this may return as well??
			def token = body?.NewDataSet?.Table[0]?.token ?: null

			if (authhash) {
				log.debug "login setting authhash to ${authhash}"
				updateAuthHash(authhash)
				if (token) {
					log.debug "login setting login token to ${token}"
					updateLoginToken(token)
					result.success = true
				} else {
					result.success = doLoginToken()
				}
			}
			else
			{
				// ERROR: any more information we can give?
				result.reason = "Bad login"
			}
		}
		else
		{
			// ERROR: any more information we can give?
			result.reason = "Bad login"
		}


	}

	return result
}

private doLoginToken() {

	def loginParams = [
		uri: "https://apilb.yoics.net",
		path: "/web/api/login.ashx",
		headers: ['User-Agent': validUserAgent()],
		requestContentType: "application/json",
		query: [key: "SmartThingsApplication", usr: getUserName(), auth: getAuthHashValue(), apilevel: 12, type:"json" ]
	]

	def result = [success:false]

	httpGet(loginParams) { resp ->
		if (resp.status == 200)
		{
			log.debug "login 200 json headers: " + resp.headers.collect { "${it.name}:${it.value}" }

			def jsonString = resp.data.str
			def body = new groovy.json.JsonSlurper().parseText(jsonString)

			def token = body?.NewDataSet?.Table[0]?.token

			if (token) {
				log.debug "login setting login to $token"
				updateLoginToken(token)
				result.success = true
			}
			else
			{
				// ERROR: any more information we can give?
				result.reason = "Bad login"
			}
		}
		else
		{
			// ERROR: any more information we can give?
			result.reason = "Bad login"
		}


	}

	return result
}

def takePicture(String dni, Integer imgWidth=null)
{

	//turn on any of the selected lights that are off
	def offLights = switches.findAll{(it.currentValue("switch") == "off")}
	log.debug offLights
	offLights.collect{it.on()}

	log.debug "parent.takePicture(${dni}, ${imgWidth})"

	def uuid = dni?.split(/\./)?.last()

	log.debug "taking picture for $uuid (${dni})"

	def imageBytes
	def loginRequired = false

	try
	{
		imageBytes = doTakePicture(uuid, imgWidth)
	}
	catch(Exception e)
	{
		log.error "Exception $e trying to take a picture, attempting to login again"
		loginRequired = true
	}

	if(loginRequired)
	{
		def loginResult = doLoginToken()
		if(loginResult.success)
		{
			// try once more
			imageBytes = doTakePicture(uuid, imgWidth)
		}
		else
		{
			log.error "tried to login to dropcam after failing to take a picture and failed"
		}
	}

	//turn previously off lights to their original state
	offLights.collect{it.off()}
	return imageBytes
}

private doTakePicture(String uuid, Integer imgWidth)
{
	imgWidth = imgWidth ?: 1280
	def loginRequired = false

	def proxyParams = getDeviceProxy(uuid)
	if(!proxyParams.success)
	{
		throw new Exception("Login Required")
	}

	def takeParams = [
		uri: "${proxyParams.uri}",
		path: "${proxyParams.path}",
		headers: ['User-Agent': validUserAgent()]
	]

	def imageBytes

	httpGet(takeParams) { resp ->

		if(resp.status == 403)
		{
			loginRequired = true
		}
		else if (resp.status == 200 && resp.headers.'Content-Type'.contains("image/jpeg"))
		{
			imageBytes = resp.data
		}
		else
		{
			log.error "unknown takePicture() response: ${resp.status} - ${resp.headers.'Content-Type'}"
		}
	}

	if(loginRequired)
	{
		throw new Exception("Login Required")
	}

	return imageBytes
}

/////////////////////////
private Boolean getLoginTokenValueIsValid()
{
	return getLoginTokenValue()
}

private updateLoginToken(String token) {
	state.loginToken = token
}

private getLoginTokenValue() {
	state.loginToken
}

private Boolean getAuthHashValueIsValid()
{
	return getAuthHashValue()
}

private updateAuthHash(String hash) {
	state.authHash = hash
}

private getAuthHashValue() {
	state.authHash
}

private updateUserName(String username) {
	state.username = username
}

private getUserName() {
	state.username
}

/*private getDeviceProxy(dni){
	//check if it exists or is not longer valid and create a new proxy here
	log.debug "returning proxy ${state.proxy[dni].proxy}"
	def proxy = [uri:state.proxy[dni].proxy, path:state.proxy[dni].url]
	log.debug "returning proxy ${proxy}"
	proxy
}*/

private updateDeviceProxy(dni, map){
	if (!state.proxy) { state.proxy = [:] }
	state.proxy[dni] = map
}

private getDeviceProxy(dni) {
	def address = dni?.split(/\./)?.last()

	def loginParams = [
		uri: "https://apilb.yoics.net",
		path: "/web/api/connect.ashx",
		headers: ['User-Agent': validUserAgent()],
		requestContentType: "application/json",
		query: [token: getLoginTokenValue(), deviceaddress:address, type:"json" ]
	]

	def result = [success:false]

	httpGet(loginParams) { resp ->
		if (resp.status == 200) //&& resp.headers.'Content-Type'.contains("application/json")
		{
			log.debug "login 200 json headers: " + resp.headers.collect { "${it.name}:${it.value}" }
			def jsonString = resp.data.str
			def body = new groovy.json.JsonSlurper().parseText(jsonString)

			if (body?.NewDataSet?.Table[0]?.error)
			{
				log.error "Attempt to get Yoics Proxy failed"
				// ERROR: any more information we can give?
				result.reason = body?.NewDataSet?.Table[0]?.message
			}
			else
			{
				result.uri = body?.NewDataSet?.Table[0]?.proxy
				result.path = body?.NewDataSet?.Table[0]?.url
				result.requested = body?.NewDataSet?.Table[0]?.requested
				result.expirationsec = body?.NewDataSet?.Table[0]?.expirationsec
				result.success = true
			}

		}
		else
		{
			// ERROR: any more information we can give?
			result.reason = "Bad login"
		}


	}

	return result

}

private validUserAgent() {
	"curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8x zlib/1.2.5"
}

def foauth() {
	def html = """<html>
<head>
	<title>$inputQuery results</title>
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link rel="shortcut icon" href="/static/sT2cZkBCCKJduBLfQ6NfUjZg1AiMhFK9ESNxUjjlvsk.ico" type="image/x-icon">
	<link rel="apple-touch-icon" href="/static/7UIUNICQhrzmPRYK3T7j5BhAsvUIbKE8OARNI702Dw9.png">
	<link rel="apple-touch-icon" sizes="114x114" href="/static/HkpqhLsUc5flOzvxrpaoyybhcCP1iRd0ogxhWFJ9vKo.png">

	<script src="/static/1vXORVkZK58St3QjdbzerXZDi9MfZQ8Q3wCyumiNiep.js" type="text/javascript" ></script>
	<link href="/static/ZLo6WmGLBQwvykZ4sFgJS1W8IKyGj3TKdKZXyHcBB9l.css" type="text/css" rel="stylesheet" media="screen, projection" />
	<link rel="stylesheet" href="/static/sd6ug4HGJyhdTwTONDZK6Yw8VsYbyDa4qUPgLokOkTn.css" type="text/css">

</head>
<body>

	<h1>
		Yoics Login
	</h1>

<form name="login" action="${buildUrl("", "authorize")}" method="post">
User:
<br>
<input type="text" name="user" style="height: 50px;">
<br>
<br>
Password:
<br>
<input type="password" name="password" style="height: 50px;">

<input type="submit" value="Submit">
</form>


</body>
</html>"""

	render status: 200, contentType: 'text/html', data: html
}

def authorize() {

	def loginResult = doLogin(params.user, params.password)

	def result
	if (loginResult.success) {
    	result = "Successful"

        //save username
		updateUserName(params.user)
    } else {
    	result = "Failed"
    }

	def html = """<html>
<head>
	<title>$inputQuery results</title>
	<meta name="apple-mobile-web-app-capable" content="yes" />

<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="shortcut icon" href="/static/sT2cZkBCCKJduBLfQ6NfUjZg1AiMhFK9ESNxUjjlvsk.ico" type="image/x-icon">
<link rel="apple-touch-icon" href="/static/7UIUNICQhrzmPRYK3T7j5BhAsvUIbKE8OARNI702Dw9.png">
<link rel="apple-touch-icon" sizes="114x114" href="/static/HkpqhLsUc5flOzvxrpaoyybhcCP1iRd0ogxhWFJ9vKo.png">



<script src="/static/1vXORVkZK58St3QjdbzerXZDi9MfZQ8Q3wCyumiNiep.js" type="text/javascript" ></script>
<link href="/static/ZLo6WmGLBQwvykZ4sFgJS1W8IKyGj3TKdKZXyHcBB9l.css" type="text/css" rel="stylesheet" media="screen, projection" />
<link rel="stylesheet" href="/static/sd6ug4HGJyhdTwTONDZK6Yw8VsYbyDa4qUPgLokOkTn.css" type="text/css">
<script>
function buildCmd(){

}
</script>
</head>
<body>

	<h1>
		Yoics Login ${result}!
	</h1>

</body>
</html>"""

	render status: 200, contentType: 'text/html', data: html
}
