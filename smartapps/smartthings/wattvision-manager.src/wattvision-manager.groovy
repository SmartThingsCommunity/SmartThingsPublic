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
 *  Wattvision Manager
 *
 *  Author: steve
 *  Date: 2014-02-13
 */

// Automatically generated. Make future change here.
definition(
	name: "Wattvision Manager",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Monitor your whole-house energy use by connecting to your Wattvision account",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision%402x.png",
	oauth: [displayName: "Wattvision", displayLink: "https://www.wattvision.com/"]
)

preferences {
	page(name: "rootPage")
}

def rootPage() {
	def sensors = state.sensors
	def hrefState = sensors ? "complete" : ""
	def hrefDescription = ""
	sensors.each { sensorId, sensorName ->
		hrefDescription += "${sensorName}\n"
	}

	dynamicPage(name: "rootPage", install: sensors ? true : false, uninstall: true) {
		section {
			href(url: loginURL(), title: "Connect Wattvision Sensors", style: "embedded", description: hrefDescription, state: hrefState)
		}
		section {
			href(url: "https://www.wattvision.com", title: "Learn More About Wattvision", style: "external", description: null)
		}
	}
}

mappings {
	path("/access") {
		actions:
		[
			POST  : "setApiAccess",
			DELETE: "revokeApiAccess"
		]
	}
	path("/devices") {
		actions:
		[
			GET: "listDevices"
		]
	}
	path("/device/:sensorId") {
		actions:
		[
			GET   : "getDevice",
			PUT   : "updateDevice",
			POST  : "createDevice",
			DELETE: "deleteDevice"
		]
	}
	path("/${loginCallbackPath()}") {
		actions:
		[
			GET: "loginCallback"
		]
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	getDataFromWattvision()
	scheduleDataCollection()
}

def getDataFromWattvision() {

	log.trace "Getting data from Wattvision"

	def children = getChildDevices()
	if (!children) {
		log.warn "No children. Not collecting data from Wattviwion"
		// currently only support one child
		return
	}

	def endDate = new Date()
	def startDate

	if (!state.lastUpdated) {
//		log.debug "no state.lastUpdated"
		startDate = new Date(hours: endDate.hours - 3)
	} else {
//		log.debug "parsing state.lastUpdated"
		startDate = new Date().parse(smartThingsDateFormat(), state.lastUpdated)
	}

	state.lastUpdated = endDate.format(smartThingsDateFormat())

	children.each { child ->
		getDataForChild(child, startDate, endDate)
	}

}

def getDataForChild(child, startDate, endDate) {
	if (!child) {
		return
	}

	def wattvisionURL = wattvisionURL(child.deviceNetworkId, startDate, endDate)
	if (wattvisionURL) {
		try {
			httpGet(uri: wattvisionURL) { response ->
				def json = new org.json.JSONObject(response.data.toString())
				child.addWattvisionData(json)
				return "success"
			}
		} catch (groovyx.net.http.HttpResponseException httpE) {
			log.error "Wattvision getDataForChild HttpResponseException: ${httpE} -> ${httpE.response.data}"
			//log.debug "wattvisionURL = ${wattvisionURL}"
			return "fail"
		} catch (e) {
			log.error "Wattvision getDataForChild General Exception: ${e}"
			//log.debug "wattvisionURL = ${wattvisionURL}"
			return "fail"
		}
	}
}

def wattvisionURL(senorId, startDate, endDate) {

	log.trace "getting wattvisionURL"

	def wattvisionApiAccess = state.wattvisionApiAccess
	if (!wattvisionApiAccess.id || !wattvisionApiAccess.key) {
		return null
	}

	if (!endDate) {
		endDate = new Date()
	}
	if (!startDate) {
		startDate = new Date(hours: endDate.hours - 3)
	}

	def diff = endDate.getTime() - startDate.getTime()
	if (diff > 10800000) { // 3 hours in milliseconds
		// Wattvision only allows pulling 3 hours of data at a time
		startDate = new Date(hours: endDate.hours - 3)
	} else if (diff < 10000) { // 10 seconds in milliseconds
		// Wattvision throws errors when the difference between start_time and end_time is 5 seconds or less
		// So we are going to make sure that we have a few more seconds of breathing room
		use (groovy.time.TimeCategory) {
			startDate = endDate - 10.seconds
		}
	}

	def params = [
		"sensor_id" : senorId,
		"api_id"    : wattvisionApiAccess.id,
		"api_key"   : wattvisionApiAccess.key,
		"type"      : wattvisionDataType ?: "rate",
		"start_time": startDate.format(wattvisionDateFormat()),
		"end_time"  : endDate.format(wattvisionDateFormat())
	]

	def parameterString = params.collect { key, value -> "${key.encodeAsURL()}=${value.encodeAsURL()}" }.join("&")
	def accessURL = wattvisionApiAccess.url ?: "https://www.wattvision.com/api/v0.2/elec"
	def url = "${accessURL}?${parameterString}"

//	log.debug "wattvisionURL: ${url}"
	return url
}

def getData() {
	state.lastUpdated = new Date().format(smartThingsDateFormat())
}

public smartThingsDateFormat() { "yyyy-MM-dd'T'HH:mm:ss.SSSZ" }

public wattvisionDateFormat() { "yyyy-MM-dd'T'HH:mm:ss" }

def childMarshaller(child) {
	return [
		name     : child.name,
		label    : child.label,
		sensor_id: child.deviceNetworkId,
		location : child.location.name
	]
}

// ========================================================
// ENDPOINTS
// ========================================================

def listDevices() {
	getChildDevices().collect { childMarshaller(it) }
}

def getDevice() {

	log.trace "Getting device"

	def child = getChildDevice(params.sensorId)

	if (!child) {
		httpError(404, "Device not found")
	}

	return childMarshaller(child)
}

def updateDevice() {

	log.trace "Updating Device with data from Wattvision"

	def body = request.JSON

	def child = getChildDevice(params.sensorId)

	if (!child) {
		httpError(404, "Device not found")
	}

	child.addWattvisionData(body)

	render([status: 204, data: " "])
}

def createDevice() {

	log.trace "Creating Wattvision device"

	if (getChildDevice(params.sensorId)) {
		httpError(403, "Device already exists")
	}

	def child = addChildDevice("smartthings", "Wattvision", params.sensorId, null, [name: "Wattvision", label: request.JSON.label])

	child.setGraphUrl(getGraphUrl(params.sensorId));

	getDataForChild(child, null, null)

	return childMarshaller(child)
}

def deleteDevice() {

	log.trace "Deleting Wattvision device"

	deleteChildDevice(params.sensorId)
	render([status: 204, data: " "])
}

def setApiAccess() {

	log.trace "Granting access to Wattvision API"

	def body = request.JSON

	state.wattvisionApiAccess = [
		url: body.url,
		id : body.id,
		key: body.key
	]

	scheduleDataCollection()

	render([status: 204, data: " "])
}

def scheduleDataCollection() {
	schedule("* /1 * * * ?", "getDataFromWattvision") // every 1 minute
}

def revokeApiAccess() {

	log.trace "Revoking access to Wattvision API"

	state.wattvisionApiAccess = [:]
	render([status: 204, data: " "])
}

public getGraphUrl(sensorId) {

	log.trace "Collecting URL for Wattvision graph"

	def apiId = state.wattvisionApiAccess.id
	def apiKey = state.wattvisionApiAccess.key

	// TODO: allow the changing of type?
	"http://www.wattvision.com/partners/smartthings/charts?s=${sensorId}&api_id=${apiId}&api_key=${apiKey}&type=w"
}

// ========================================================
// SmartThings initiated setup
// ========================================================

/* Debug info for Steve / Andrew

this page: /partners/smartthings/whatswv
	- linked from within smartthings, will tell you how to get a wattvision sensor, etc.
	- pass the debug flag (?debug=1) to show this text.

login page: /partners/smartthings/login?callback_url=CALLBACKURL
	- open this page, which will require login.
	- once login is complete, we call you back at callback_url with:
		<callback_url>?id=<wattvision_api_id>&key=<wattvision_api_key>
			question: will you know which user this is on your end?

sensor json: /partners/smartthings/sensor_list?api_id=...&api_key=...
	- returns a list of sensors and their associated house names, as a json object
	- example return value with one sensor id 2, associated with house 'Test's House'
		- content type is application/json
		- {"2": "Test's House"}

*/

def loginCallback() {
	log.trace "loginCallback"

	state.wattvisionApiAccess = [
		id : params.id,
		key: params.key
	]

	getSensorJSON(params.id, params.key)

	connectionSuccessful("Wattvision", "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision@2x.png")
}

private getSensorJSON(id, key) {
	log.trace "getSensorJSON"

	def sensorUrl = "${wattvisionBaseURL()}/partners/smartthings/sensor_list?api_id=${id}&api_key=${key}"

    httpGet(uri: sensorUrl) { response ->

		def sensors = [:]

        response.data.each { sensorId, sensorName ->
        	sensors[sensorId] = sensorName
			createChild(sensorId, sensorName)
        }
        
        state.sensors = sensors

		return "success"
	}
    
}

def createChild(sensorId, sensorName) {
	log.trace "creating Wattvision Child"

	def child = getChildDevice(sensorId)

	if (child) {
		log.warn "Device already exists"
	} else {
		child = addChildDevice("smartthings", "Wattvision", sensorId, null, [name: "Wattvision", label: sensorName])
	}

	child.setGraphUrl(getGraphUrl(sensorId));

	getDataForChild(child, null, null)

	scheduleDataCollection()

	return childMarshaller(child)
}

// ========================================================
// URL HELPERS
// ========================================================

private loginURL() { "${wattvisionBaseURL()}${loginPath()}" }

private wattvisionBaseURL() { "https://www.wattvision.com" }

private loginPath() { "/partners/smartthings/login?callback_url=${loginCallbackURL().encodeAsURL()}" }

private loginCallbackURL() {
	if (!atomicState.accessToken) { createAccessToken() }
	buildActionUrl(loginCallbackPath())
}
private loginCallbackPath() { "login/callback" }

// ========================================================
// Access Token
// ========================================================

private getMyAccessToken() { return atomicState.accessToken ?: createAccessToken() }

// ========================================================
// CONNECTED HTML
// ========================================================

def connectionSuccessful(deviceName, iconSrc) {
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
		<img src="${iconSrc}" alt="${deviceName} icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
		<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
		<p>Your ${deviceName} is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	</div>
</body>
</html>
"""

	render contentType: 'text/html', data: html
}
