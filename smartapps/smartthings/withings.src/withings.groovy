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
 *	Withings Service Manager
 *
 *	Author: SmartThings
 *	Date: 2013-09-26
 */

definition(
    name: "Withings",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Connect your Withings scale to SmartThings.",
    category: "Connections",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/withings.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/withings%402x.png",
    oauth: true,
    singleInstance: true
) {
	appSetting "clientId"
	appSetting "clientSecret"
    appSetting "serverUrl"
}

preferences {
	page(name: "auth", title: "Withings", content:"authPage")
}

mappings {
	path("/exchange") {
		action: [
			GET: "exchangeToken"
		]
	}
	path("/load") {
		action: [
			GET: "load"
		]
	}
}

def authPage() {
	log.debug "authPage()"
	dynamicPage(name: "auth", title: "Withings", install:false, uninstall:true) {
		section {
			paragraph "This version is no longer supported. Please uninstall it."
		}
	}
}

def oauthInitUrl() {
	def token = getToken()
	log.debug "initiateOauth got token: $token"

	// store these for validate after the user takes the oauth journey
	state.oauth_request_token = token.oauth_token
	state.oauth_request_token_secret = token.oauth_token_secret

	return buildOauthUrlWithToken(token.oauth_token, token.oauth_token_secret)
}

def getToken() {
	def callback = getServerUrl() + "/api/smartapps/installations/${app.id}/exchange?access_token=${state.accessToken}"
	def params = [
		oauth_callback:URLEncoder.encode(callback),
	]
	def requestTokenBaseUrl = "https://oauth.withings.com/account/request_token"
	def url = buildSignedUrl(requestTokenBaseUrl, params)
	log.debug "getToken - url: $url"

	return getJsonFromUrl(url)
}

def buildOauthUrlWithToken(String token, String tokenSecret) {
	def callback = getServerUrl() + "/api/smartapps/installations/${app.id}/exchange?access_token=${state.accessToken}"
	def params = [
		oauth_callback:URLEncoder.encode(callback),
		oauth_token:token
	]
	def authorizeBaseUrl = "https://oauth.withings.com/account/authorize"

	return buildSignedUrl(authorizeBaseUrl, params, tokenSecret)
}

/////////////////////////////////////////
/////////////////////////////////////////
// vvv vvv		OAuth 1.0	   vvv vvv //
/////////////////////////////////////////
/////////////////////////////////////////
String buildSignedUrl(String baseUrl, Map urlParams, String tokenSecret="") {
	def params = [
		oauth_consumer_key: smartThingsConsumerKey,
		oauth_nonce: nonce(),
		oauth_signature_method: "HMAC-SHA1",
		oauth_timestamp: timestampInSeconds(),
		oauth_version: 1.0
	] + urlParams
	def signatureBaseString = ["GET", baseUrl, toQueryString(params)].collect { URLEncoder.encode(it) }.join("&")

	params.oauth_signature = hmac(signatureBaseString, getSmartThingsConsumerSecret(), tokenSecret)

	// query string is different from what is used in generating the signature above b/c it includes "oauth_signature"
	def url = [baseUrl, toQueryString(params)].join('?')
	return url
}

String nonce() {
	return UUID.randomUUID().toString().replaceAll("-", "")
}

Integer timestampInSeconds() {
	return (int)(new Date().time/1000)
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

String hmac(String dataString, String consumerSecret, String tokenSecret="") throws java.security.SignatureException {
	String result

	def key = [consumerSecret, tokenSecret].join('&')
	
	// get an hmac_sha1 key from the raw key bytes
	def signingKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA1")
	
	// get an hmac_sha1 Mac instance and initialize with the signing key
	def mac = javax.crypto.Mac.getInstance("HmacSHA1")
	mac.init(signingKey)

	// compute the hmac on input data bytes
	byte[] rawHmac = mac.doFinal(dataString.getBytes())

	result = org.apache.commons.codec.binary.Base64.encodeBase64String(rawHmac)

	return result
}
/////////////////////////////////////////
/////////////////////////////////////////
// ^^^ ^^^		OAuth 1.0	   ^^^ ^^^ //
/////////////////////////////////////////
/////////////////////////////////////////

/////////////////////////////////////////
/////////////////////////////////////////
// vvv vvv		 rest		   vvv vvv //
/////////////////////////////////////////
/////////////////////////////////////////

protected rest(Map params) {
	new physicalgraph.device.RestAction(params)
}

/////////////////////////////////////////
/////////////////////////////////////////
// ^^^ ^^^		 rest		   ^^^ ^^^ //
/////////////////////////////////////////
/////////////////////////////////////////

def exchangeToken() {
	//	 oauth_token=abcd
	//	 &userid=123

	def newToken = params.oauth_token
	def userid = params.userid
	def tokenSecret = state.oauth_request_token_secret

	def params = [
		oauth_token: newToken,
		userid: userid
	]

	def requestTokenBaseUrl = "https://oauth.withings.com/account/access_token"
	def url = buildSignedUrl(requestTokenBaseUrl, params, tokenSecret)
	log.debug "signed url: $url with secret $tokenSecret"

	def token = getJsonFromUrl(url)

	state.userid = userid
	state.oauth_token = token.oauth_token
	state.oauth_token_secret = token.oauth_token_secret

	log.debug "swapped token"

	def location = getServerUrl() + "/api/smartapps/installations/${app.id}/load?access_token=${state.accessToken}"
	redirect(location:location)
}

def load() {
	def json = get(getMeasurement(new Date() - 30))

	log.debug "swapped, then received: $json"
	parse(data:json)

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
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/withings@2x.png" alt="withings icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
		<p>Your Withings scale is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	</div>
</body>
</html>
"""

	render contentType: 'text/html', data: html
}

Map getJsonFromUrl(String url) {
	return [:] // stop making requests to Withings API. This entire SmartApp will be replaced with a fix

	def jsonString
	httpGet(uri: url) { resp ->
		jsonString = resp.data.toString()
	}

	return getJsonFromText(jsonString)
}

Map getJsonFromText(String jsonString) {
	def jsonMap = jsonString.split("&").inject([:]) { c, it ->
		def parts = it.split('=')
		def k = parts[0]
		def v = parts[1]
		c[k] = v
		return c
	}

	return jsonMap
}

def getMeasurement(Date since=null) {
	return null // stop making requests to Withings API. This entire SmartApp will be replaced with a fix

	// TODO: add startdate and enddate ... esp. when in response to notify
	def params = [
		action:"getmeas",
		oauth_consumer_key:getSmartThingsConsumerKey(),
		oauth_nonce:nonce(),
		oauth_signature_method:"HMAC-SHA1",
		oauth_timestamp:timestampInSeconds(),
		oauth_token:state.oauth_token,
		oauth_version:1.0,
		userid: state.userid
	]

	if(since)
	{
		params.startdate = dateToSeconds(since)
	}

	def requestTokenBaseUrl = "http://wbsapi.withings.net/measure"
	def signatureBaseString = ["GET", requestTokenBaseUrl, toQueryString(params)].collect { URLEncoder.encode(it) }.join("&")

	params.oauth_signature = hmac(signatureBaseString, getSmartThingsConsumerSecret(), state.oauth_token_secret)

	return rest(
		method: 'GET',
		endpoint: "http://wbsapi.withings.net",
		path: "/measure",
		query: params,
		synchronous: true
	)

}

String get(measurementRestAction) {
	return "" // stop making requests to Withings API. This entire SmartApp will be replaced with a fix

	def httpGetParams = [
		uri: measurementRestAction.endpoint,
		path: measurementRestAction.path,
		query: measurementRestAction.query
	]

	String json
	httpGet(httpGetParams) {resp ->
		json = resp.data.text.toString()
	}

	return json
}

def parse(Map response) {
	def json = new org.codehaus.groovy.grails.web.json.JSONObject(response.data)
	parseJson(json)
}

def parseJson(json) {
	log.debug "parseJson: $json"

	def lastDataPointMillis = (state.lastDataPointMillis ?: 0).toLong()
	def i = 0

	if(json.status == 0)
	{
		log.debug "parseJson measure group size: ${json.body.measuregrps.size()}"

		state.errorCount = 0

		def childDni = getWithingsDevice(json.body.measuregrps).deviceNetworkId

		def latestMillis = lastDataPointMillis
		json.body.measuregrps.sort { it.date }.each { group ->

			def measurementDateSeconds = group.date
			def dataPointMillis = measurementDateSeconds * 1000L

			if(dataPointMillis > lastDataPointMillis)
			{
				group.measures.each { measure ->
					i++
					saveMeasurement(childDni, measure, measurementDateSeconds)
				}
			}

			if(dataPointMillis > latestMillis)
			{
				latestMillis = dataPointMillis
			}

		}

		if(latestMillis > lastDataPointMillis)
		{
			state.lastDataPointMillis = latestMillis
		}

		def weightData = state.findAll { it.key.startsWith("measure.") }

		// remove old data
		def old = "measure." + (new Date() - 30).format('yyyy-MM-dd')
		state.findAll { it.key.startsWith("measure.") && it.key < old }.collect { it.key }.each { state.remove(it) }
	}
	else
	{
		def errorCount = (state.errorCount ?: 0).toInteger()
		state.errorCount = errorCount + 1

		// TODO: If we poll, consider waiting for a couple failures before showing an error
		//			But if we are only notified, then we need to raise the error right away
		measurementError(json.status)
	}

	log.debug "Done adding $i measurements"
	return
}

def measurementError(status) {
	log.error "received api response status ${status}"
	sendEvent(state.childDni, [name: "connection", value:"Connection error: ${status}", isStateChange:true, displayed:true])
}

def saveMeasurement(childDni, measure, measurementDateSeconds) {
	def dateString = secondsToDate(measurementDateSeconds).format('yyyy-MM-dd')

	def measurement = withingsEvent(measure)
	sendEvent(state.childDni, measurement + [date:dateString], [dateCreated:secondsToDate(measurementDateSeconds)])

	log.debug "sm: ${measure.type} (${measure.type == 1})"

	if(measure.type == 6)
	{
		sendEvent(state.childDni, [name: "leanRatio", value:(100-measurement.value), date:dateString, isStateChange:true, display:true], [dateCreated:secondsToDate(measurementDateSeconds)])
	}
	else if(measure.type == 1)
	{
		state["measure." + dateString] = measurement.value
	}
}

def eventValue(measure, roundDigits=1) {
	def value = measure.value * 10.power(measure.unit)

	if(roundDigits != null)
	{
		def significantDigits = 10.power(roundDigits)
		value = (value * significantDigits).toInteger() / significantDigits
	}

	return value
}

def withingsEvent(measure) {
	def withingsTypes = [
		(1):"weight",
		(4):"height",
		(5):"leanMass",
		(6):"fatRatio",
		(8):"fatMass",
		(11):"pulse"
	]

	def value = eventValue(measure, (measure.type == 4 ? null : 1))

	if(measure.type == 1) {
		value *= 2.20462
	} else if(measure.type == 4) {
		value *= 39.3701
	}

	log.debug "m:${measure.type}, v:${value}"

	return [
		name: withingsTypes[measure.type],
		value: value
	]
}

Integer dateToSeconds(Date d) {
	return d.time / 1000
}

Date secondsToDate(Number seconds) {
	return new Date(seconds * 1000L)
}

def getWithingsDevice(measuregrps=null) {
	// unfortunately, Withings doesn't seem to give us enough information to know which device(s) they have,
	// ... so we have to guess and create a single device

	if(state.childDni)
	{
		return getChildDevice(state.childDni)
	}
	else
	{
		def children = getChildDevices()
		if(children.size() > 0)
		{
			return children[0]
		}
		else
		{
			// no child yet, create one
			def dni = [app.id, UUID.randomUUID().toString()].join('.')
			state.childDni = dni

			def childDeviceType = getBodyAnalyzerChildName()

			if(measuregrps)
			{
				def hasNoHeartRate = measuregrps.find { grp -> grp.measures.find { it.type == 11 } } == null
				if(hasNoHeartRate)
				{
					childDeviceType = getScaleChildName()
				}
			}

			def child = addChildDevice(getChildNamespace(), childDeviceType, dni, null, [label:"Withings"])
			state.childId = child.id
			return child
		}
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
	// TODO: subscribe to attributes, devices, locations, etc.
}

def poll() {
	if(shouldPoll())
	{
		return getMeasurement()
	}

	return null
}

def shouldPoll() {
	def lastPollString = state.lastPollMillisString
	def lastPoll = lastPollString?.isNumber() ? lastPollString.toLong() : 0
	def ONE_HOUR = 60 * 60 * 1000

	def time = new Date().time

	if(time > (lastPoll + ONE_HOUR))
	{
		log.debug "Executing poll b/c (now > last + 1hr): ${time} > ${lastPoll + ONE_HOUR} (last: ${lastPollString})"
		state.lastPollMillisString = time

		return true
	}

	log.debug "skipping poll b/c !(now > last + 1hr): ${time} > ${lastPoll + ONE_HOUR} (last: ${lastPollString})"
	return false
}

def refresh() {
	log.debug "Executing 'refresh'"
	return getMeasurement()
}

def getChildNamespace() { "smartthings" }
def getScaleChildName() { "Wireless Scale" }
def getBodyAnalyzerChildName() { "Smart Body Analyzer" }

def getServerUrl() { appSettings.serverUrl }
def getSmartThingsConsumerKey() { appSettings.clientId }
def getSmartThingsConsumerSecret() { appSettings.clientSecret }
