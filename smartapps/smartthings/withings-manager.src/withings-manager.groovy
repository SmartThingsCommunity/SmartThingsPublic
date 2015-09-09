/**
 *  Title: Withings Service Manager
 * 	Description: Connect Your Withings Devices
 *
 *  Author: steve
 *  Date: 1/9/15
 *
 *
 *  Copyright 2015 steve
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
	name: "Withings Manager",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Connect With Withings",
	category: "",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/withings.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/withings%402x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Partner/withings%402x.png",
	oauth: true
) {
	appSetting "consumerKey"
	appSetting "consumerSecret"
}

// ========================================================
// PAGES
// ========================================================

preferences {
	page(name: "authPage")
}

def authPage() {

	def installOptions = false
	def description = "Required (tap to set)"
	def authState

	if (oauth_token()) {
		// TODO: Check if it's valid
		if (true) {
			description = "Saved (tap to change)"
			installOptions = true
			authState = "complete"
		} else {
			// Worth differentiating here? (no longer valid vs. non-existent state.externalAuthToken?)
			description = "Required (tap to set)"
		}
	}


	dynamicPage(name: "authPage", install: installOptions, uninstall: true) {
		section {

			if (installOptions) {
				input(name: "withingsLabel", type: "text", title: "Add a name", description: null, required: true)
			}

			href url: shortUrl("authenticate"), style: "embedded", required: false, title: "Authenticate with Withings", description: description, state: authState
		}
	}
}

// ========================================================
// MAPPINGS
// ========================================================

mappings {
	path("/authenticate") {
		action:
		[
			GET: "authenticate"
		]
	}
	path("/x") {
		action:
		[
			GET: "exchangeTokenFromWithings"
		]
	}
	path("/n") {
		action:
		[POST: "notificationReceived"]
	}

	path("/test/:action") {
		action:
		[GET: "test"]
	}
}

def test() {
	"${params.action}"()
}

def authenticate() {
	// do not hit userAuthorizationUrl when the page is executed. It will replace oauth_tokens
	// instead, redirect through here so we know for sure that the user wants to authenticate
	// plus, the short-lived tokens that are used during authentication are only valid for 2 minutes
	// so make sure we give the user as much of that 2 minutes as possible to enter their credentials and deal with network latency
	log.trace "starting Withings authentication flow"
	redirect location: userAuthorizationUrl()
}

def exchangeTokenFromWithings() {
	// Withings hits us here during the oAuth flow
//	log.trace "exchangeTokenFromWithings ${params}"
	atomicState.userid = params.userid // TODO: restructure this for multi-user access
	exchangeToken()
}

def notificationReceived() {
//	log.trace "notificationReceived params: ${params}"

	def notificationParams = [
		startdate: params.startdate,
		userid   : params.userid,
		enddate  : params.enddate,
	]

	def measures = wGetMeasures(notificationParams)
	sendMeasureEvents(measures)
	return [status: 0]
}

// ========================================================
// HANDLERS
// ========================================================


def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

//	wRevokeAllNotifications()

	unsubscribe()
	initialize()
}

def initialize() {
	if (!getChild()) { createChild() }
	app.updateLabel(withingsLabel)
	wCreateNotification()
	backfillMeasures()
}

// ========================================================
// CHILD DEVICE
// ========================================================

private getChild() {
	def children = childDevices
	children.size() ? children.first() : null
}

private void createChild() {
	def child = addChildDevice("smartthings", "Withings User", userid(), null, [name: app.label, label: withingsLabel])
	atomicState.child = [dni: child.deviceNetworkId]
}

// ========================================================
// URL HELPERS
// ========================================================

def stBaseUrl() {
	if (!atomicState.serverUrl) {
		stToken()
		atomicState.serverUrl = buildActionUrl("").split(/api\//).first()
	}
	return atomicState.serverUrl
}

def stToken() {
	atomicState.accessToken ?: createAccessToken()
}

def shortUrl(path = "", urlParams = [:]) {
	attachParams("${stBaseUrl()}api/t/${stToken()}/s/${app.id}/${path}", urlParams)
}

def noTokenUrl(path = "", urlParams = [:]) {
	attachParams("${stBaseUrl()}api/smartapps/installations/${app.id}/${path}", urlParams)
}

def attachParams(url, urlParams = [:]) {
	[url, toQueryString(urlParams)].findAll().join("?")
}

String toQueryString(Map m = [:]) {
//	log.trace "toQueryString. URLEncoder will be used on ${m}"
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

// ========================================================
// WITHINGS MEASURES
// ========================================================

def unixTime(date = new Date()) {
	def unixTime = date.time / 1000 as int
//	log.debug "converting ${date.time} to ${unixTime}"
	unixTime
}

def backfillMeasures() {
//	log.trace "backfillMeasures"
	def measureParams = [startdate: unixTime(new Date() - 10)]
	def measures = wGetMeasures(measureParams)
	sendMeasureEvents(measures)
}

// this is body measures. // TODO: get activity and others too
def wGetMeasures(measureParams = [:]) {
	def baseUrl = "https://wbsapi.withings.net/measure"
	def urlParams = [
		action     : "getmeas",
		userid     : userid(),
		startdate  : unixTime(new Date() - 5),
		enddate    : unixTime(),
		oauth_token: oauth_token()
	] + measureParams
	def measureData = fetchDataFromWithings(baseUrl, urlParams)
//	log.debug "measureData: ${measureData}"
	measureData.body.measuregrps.collect { parseMeasureGroup(it) }.flatten()
}
/*
[
	body:[
		measuregrps:[
			[
				category:1, // 1 for real measurements, 2 for user objectives.
				grpid:310040317,
				measures:[
					[
						unit:0, 	// Power of ten the "value" parameter should be multiplied to to get the real value. Eg : value = 20 and unit=-1 means the value really is 2.0
						value:60, // Value for the measure in S.I units (kilogram, meters, etc.). Value should be multiplied by 10 to the power of "unit" (see below) to get the real value.
						type:11   // 1 : Weight (kg), 4 : Height (meter), 5 : Fat Free Mass (kg), 6 : Fat Ratio (%), 8 : Fat Mass Weight (kg), 9 : Diastolic Blood Pressure (mmHg), 10 : Systolic Blood Pressure (mmHg), 11 : Heart Pulse (bpm), 54 : SP02(%)
					],
					[
						unit:-3,
						value:-1000,
						type:18
					]
				],
				date:1422750210,
				attrib:2
			]
		],
		updatetime:1422750227
	],
	status:0
]
*/

def sendMeasureEvents(measures) {
//	log.debug "measures: ${measures}"
	measures.each {
		if (it.name && it.value) {
			sendEvent(userid(), it)
		}
	}
}

def parseMeasureGroup(measureGroup) {
	long time = measureGroup.date // must be long. INT_MAX is too small
	time *= 1000
	measureGroup.measures.collect { parseMeasure(it) + [date: new Date(time)] }
}

def parseMeasure(measure) {
//	log.debug "parseMeasure($measure)"
	[
		name : measureAttribute(measure),
		value: measureValue(measure)
	]
}

def measureValue(measure) {
	def value = measure.value * 10.power(measure.unit)
	if (measure.type == 1) { // Weight (kg)
		value *= 2.20462262 // kg to lbs
	}
	value
}

String measureAttribute(measure) {
	def attribute = ""
	switch (measure.type) {
		case 1: attribute = "weight"; break;
		case 4: attribute = "height"; break;
		case 5: attribute = "leanMass"; break;
		case 6: attribute = "fatRatio"; break;
		case 8: attribute = "fatMass"; break;
		case 9: attribute = "diastolicPressure"; break;
		case 10: attribute = "systolicPressure"; break;
		case 11: attribute = "heartPulse"; break;
		case 54: attribute = "SP02"; break;
	}
	return attribute
}

String measureDescription(measure) {
	def description = ""
	switch (measure.type) {
		case 1: description = "Weight (kg)"; break;
		case 4: description = "Height (meter)"; break;
		case 5: description = "Fat Free Mass (kg)"; break;
		case 6: description = "Fat Ratio (%)"; break;
		case 8: description = "Fat Mass Weight (kg)"; break;
		case 9: description = "Diastolic Blood Pressure (mmHg)"; break;
		case 10: description = "Systolic Blood Pressure (mmHg)"; break;
		case 11: description = "Heart Pulse (bpm)"; break;
		case 54: description = "SP02(%)"; break;
	}
	return description
}

// ========================================================
// WITHINGS NOTIFICATIONS
// ========================================================

def wNotificationBaseUrl() { "https://wbsapi.withings.net/notify" }

def wNotificationCallbackUrl() { shortUrl("n") }

def wGetNotification() {
	def userId = userid()
	def url = wNotificationBaseUrl()
	def params = [
		action: "subscribe"
	]

}

// TODO: keep track of notification expiration
def wCreateNotification() {
	def baseUrl = wNotificationBaseUrl()
	def urlParams = [
		action     : "subscribe",
		userid     : userid(),
		callbackurl: wNotificationCallbackUrl(),
		oauth_token: oauth_token(),
		comment    : "hmm" // TODO: figure out what to do here. spaces seem to break the request
	]

	fetchDataFromWithings(baseUrl, urlParams)
}

def wRevokeAllNotifications() {
	def notifications = wListNotifications()
	notifications.each {
		wRevokeNotification([callbackurl: it.callbackurl]) // use the callbackurl Withings has on file
	}
}

def wRevokeNotification(notificationParams = [:]) {
	def baseUrl = wNotificationBaseUrl()
	def urlParams = [
		action     : "revoke",
		userid     : userid(),
		callbackurl: wNotificationCallbackUrl(),
		oauth_token: oauth_token()
	] + notificationParams

	fetchDataFromWithings(baseUrl, urlParams)
}

def wListNotifications() {

	/*
	{
		body: {
			profiles: [
				{
					appli: 1,
					expires: 2147483647,
					callbackurl: "https://graph.api.smartthings.com/api/t/72ab3e57-5839-4cca-9562-dcc818f83bc9/s/537757a0-c4c8-40ea-8cea-aa283915bbd9/n",
					comment: "hmm"
				}
			]
		},
		status: 0
	}*/

	def baseUrl = wNotificationBaseUrl()
	def urlParams = [
		action     : "list",
		userid     : userid(),
		callbackurl: wNotificationCallbackUrl(),
		oauth_token: oauth_token()
	]

	def notificationData = fetchDataFromWithings(baseUrl, urlParams)
	notificationData.body.profiles
}

def defaultOauthParams() {
	defaultParameterKeys().inject([:]) { keyMap, currentKey ->
		keyMap[currentKey] = "${currentKey}"()
		keyMap
	}
}

// ========================================================
// WITHINGS DATA FETCHING
// ========================================================

def fetchDataFromWithings(baseUrl, urlParams) {

//	log.debug "fetchDataFromWithings(${baseUrl}, ${urlParams})"

	def defaultParams = defaultOauthParams()
	def paramStrings = buildOauthParams(urlParams + defaultParams)
//	log.debug "paramStrings: $paramStrings"
	def url = buildOauthUrl(baseUrl, paramStrings, oauth_token_secret())
	def json
//	log.debug "about to make request to ${url}"
	httpGet(uri: url, headers: ["Content-Type": "application/json"]) { response ->
		json = new groovy.json.JsonSlurper().parse(response.data)
	}
	return json
}

// ========================================================
// WITHINGS OAUTH LOGGING
// ========================================================

def wLogEnabled() { false } // For troubleshooting Oauth flow

void wLog(message = "") {
	if (!wLogEnabled()) { return }
	def wLogMessage = atomicState.wLogMessage
	if (wLogMessage.length()) {
		wLogMessage += "\n|"
	}
	wLogMessage += message
	atomicState.wLogMessage = wLogMessage
}

void wLogNew(seedMessage = "") {
	if (!wLogEnabled()) { return }
	def olMessage = atomicState.wLogMessage
	if (oldMessage) {
		log.debug "purging old wLogMessage: ${olMessage}"
	}
	atomicState.wLogMessage = seedMessage
}

String wLogMessage() {
	if (!wLogEnabled()) { return }
	def wLogMessage = atomicState.wLogMessage
	atomicState.wLogMessage = ""
	wLogMessage
}

// ========================================================
// WITHINGS OAUTH DESCRIPTION
// >>>>>>	The user opens the authPage for this SmartApp
// STEP 1 get a token to be used in the url the user taps
// STEP 2 generate the url to be tapped by the user
// >>>>>>	The user taps the url and logs in to Withings
// STEP 3 generate a token to be used for accessing user data
// STEP 4 access user data
// ========================================================

// ========================================================
// WITHINGS OAUTH STEP 1: get an oAuth "request token"
// ========================================================

def requestTokenUrl() {
	wLogNew "WITHINGS OAUTH STEP 1: get an oAuth 'request token'"

	def keys = defaultParameterKeys() + "oauth_callback"
	def paramStrings = buildOauthParams(keys.sort())

	buildOauthUrl("https://oauth.withings.com/account/request_token", paramStrings, "")
}

// ========================================================
// WITHINGS OAUTH STEP 2: End-user authorization
// ========================================================

def userAuthorizationUrl() {

	// get url from Step 1
	def tokenUrl = requestTokenUrl()

	// collect token from Withings
	collectTokenFromWithings(tokenUrl)

	wLogNew "WITHINGS OAUTH STEP 2: End-user authorization"

	def keys = defaultParameterKeys() + "oauth_token"
	def paramStrings = buildOauthParams(keys.sort())

	buildOauthUrl("https://oauth.withings.com/account/authorize", paramStrings, oauth_token_secret())
}

// ========================================================
// WITHINGS OAUTH STEP 3: Generating access token
// ========================================================

def exchangeTokenUrl() {
	wLogNew "WITHINGS OAUTH STEP 3: Generating access token"

	def keys = defaultParameterKeys() + ["oauth_token", "userid"]
	def paramStrings = buildOauthParams(keys.sort())

	buildOauthUrl("https://oauth.withings.com/account/access_token", paramStrings, oauth_token_secret())
}

def exchangeToken() {

	def tokenUrl = exchangeTokenUrl()
//	log.debug "about to hit ${tokenUrl}"

	try {
		// replace old token with a long-lived token
		def token = collectTokenFromWithings(tokenUrl)
//		log.debug "collected token from Withings: ${token}"
		renderAction("authorized", "Withings Connection")
	}
	catch (Exception e) {
		log.error e
		renderAction("notAuthorized", "Withings Connection Failed")
	}
}

// ========================================================
// OAUTH 1.0
// ========================================================

def defaultParameterKeys() {
	[
		"oauth_consumer_key",
		"oauth_nonce",
		"oauth_signature_method",
		"oauth_timestamp",
		"oauth_version"
	]
}

def oauth_consumer_key() { consumerKey }

def oauth_nonce() { nonce() }

def nonce() { UUID.randomUUID().toString().replaceAll("-", "") }

def oauth_signature_method() { "HMAC-SHA1" }

def oauth_timestamp() { (int) (new Date().time / 1000) }

def oauth_version() { 1.0 }

def oauth_callback() { shortUrl("x") }

def oauth_token() { atomicState.wToken?.oauth_token }

def oauth_token_secret() { atomicState.wToken?.oauth_token_secret }

def userid() { atomicState.userid }

String hmac(String oAuthSignatureBaseString, String oAuthSecret) throws java.security.SignatureException {
	if (!oAuthSecret.contains("&")) { log.warn "Withings requires \"&\" to be included no matter what" }
	// get an hmac_sha1 key from the raw key bytes
	def signingKey = new javax.crypto.spec.SecretKeySpec(oAuthSecret.getBytes(), "HmacSHA1")
	// get an hmac_sha1 Mac instance and initialize with the signing key
	def mac = javax.crypto.Mac.getInstance("HmacSHA1")
	mac.init(signingKey)
	// compute the hmac on input data bytes
	byte[] rawHmac = mac.doFinal(oAuthSignatureBaseString.getBytes())
	return org.apache.commons.codec.binary.Base64.encodeBase64String(rawHmac)
}

Map parseResponseString(String responseString) {
//	log.debug "parseResponseString: ${responseString}"
	responseString.split("&").inject([:]) { c, it ->
		def parts = it.split('=')
		def k = parts[0]
		def v = parts[1]
		c[k] = v
		return c
	}
}

String applyParams(endpoint, oauthParams) { endpoint + "?" + oauthParams.sort().join("&") }

String buildSignature(endpoint, oAuthParams, oAuthSecret) {
	def oAuthSignatureBaseParts = ["GET", endpoint, oAuthParams.join("&")]
	def oAuthSignatureBaseString = oAuthSignatureBaseParts.collect { URLEncoder.encode(it) }.join("&")
	wLog "    ==> oAuth signature base string : \n${oAuthSignatureBaseString}"
	wLog "    .. applying hmac-sha1 to base string, with secret : ${oAuthSecret} (notice the \"&\")"
	wLog "    .. base64 encode then url-encode the hmac-sha1 hash"
	String hmacResult = hmac(oAuthSignatureBaseString, oAuthSecret)
	def signature = URLEncoder.encode(hmacResult)
	wLog "    ==> oauth_signature = ${signature}"
	return signature
}

List buildOauthParams(List parameterKeys) {
	wLog "    .. adding oAuth parameters : "
	def oauthParams = []
	parameterKeys.each { key ->
		def value = "${key}"()
		wLog "        ${key} = ${value}"
		oauthParams << "${key}=${URLEncoder.encode(value.toString())}"
	}

	wLog "    .. sorting all request parameters alphabetically "
	oauthParams.sort()
}

List buildOauthParams(Map parameters) {
	wLog "    .. adding oAuth parameters : "
	def oauthParams = []
	parameters.each { k, v ->
		wLog "        ${k} = ${v}"
		oauthParams << "${k}=${URLEncoder.encode(v.toString())}"
	}

	wLog "    .. sorting all request parameters alphabetically "
	oauthParams.sort()
}

String buildOauthUrl(String endpoint, List parameterStrings, String oAuthTokenSecret) {
	wLog "Api endpoint : ${endpoint}"

	wLog "Signing request :"
	def oAuthSecret = "${consumerSecret}&${oAuthTokenSecret}"
	def signature = buildSignature(endpoint, parameterStrings, oAuthSecret)

	parameterStrings << "oauth_signature=${signature}"

	def finalUrl = applyParams(endpoint, parameterStrings)
	wLog "Result: ${finalUrl}"
	if (wLogEnabled()) {
		log.debug wLogMessage()
	}
	return finalUrl
}

def collectTokenFromWithings(tokenUrl) {
	// get token from Withings using the url generated in Step 1
	def tokenString
	httpGet(uri: tokenUrl) { resp -> // oauth_token=<token_key>&oauth_token_secret=<token_secret>
		tokenString = resp.data.toString()
//		log.debug "collectTokenFromWithings: ${tokenString}"
	}
	def token = parseResponseString(tokenString)
	atomicState.wToken = token
	return token
}

// ========================================================
// APP SETTINGS
// ========================================================

def getConsumerKey() { appSettings.consumerKey }

def getConsumerSecret() { appSettings.consumerSecret }

// figure out how to put this in settings
def getUserId() { atomicState.wToken?.userid }

// ========================================================
// HTML rendering
// ========================================================

def renderAction(action, title = "") {
	log.debug "renderAction: $action"
	renderHTML(title) {
		head { "${action}HtmlHead"() }
		body { "${action}HtmlBody"() }
	}
}

def authorizedHtmlHead() {
	log.trace "authorizedHtmlHead"
	"""
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
				/*width: 560px;
				padding: 40px;*/
				/*background: #eee;*/
				text-align: center;
			}
			img {
				vertical-align: middle;
							max-width:20%;
			}
			img:nth-child(2) {
				margin: 0 30px;
			}
			p {
				/*font-size: 1.2em;*/
				font-family: 'Swiss 721 W01 Thin';
				text-align: center;
				color: #666666;
				padding: 0 10px;
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
		"""
}

def authorizedHtmlBody() {
	"""
		<div class="container">
			<img src="https://s3.amazonaws.com/smartapp-icons/Partner/withings@2x.png" alt="withings icon" />
			<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
			<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
			<p>Your Withings scale is now connected to SmartThings!</p>
			<p>Click 'Done' to finish setup.</p>
		</div>
		"""
}

def notAuthorizedHtmlHead() {
	log.trace "notAuthorizedHtmlHead"
	authorizedHtmlHead()
}

def notAuthorizedHtmlBody() {
	"""
		<div class="container">
			<p>There was an error connecting to SmartThings!</p>
			<p>Click 'Done' to try again.</p>
		</div>
		"""
}
