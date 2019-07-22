/**
 *  Quirky (Connect)
 *
 *  Author: todd@wackford.net
 *  Date: 2014-02-15
 *
 *  Update: 2014-02-22
 *			Added eggtray
 *			Added device specific methods called from poll (versus in poll)
 *
 *  Update2:2014-02-22
 *			Added nimbus
 *
 *  Update3:2014-02-26
 *			Improved eggtray integration
 *			Added notifications to hello home
 *			Introduced Quirky Eggtray specific icons (Thanks to Dane)
 *			Added an Egg Report that outputs to hello home.
 *			Switched to Dan Lieberman's client and secret
 *			Still not browser flow (next update?)
 *
 *  Update4:2014-03-08
 *			Added Browser Flow OAuth
 *
 *
 *  Update5:2014-03-14
 *			Added dynamic icon/tile updating to the nimbus. Changes the device icon from app.
 *
 *  Update6:2014-03-31
 *			Stubbed out creation and choice of nimbus, eggtray and porkfolio per request.
 *
 *  Update7:2014-04-01
 *          Renamed to 'Quirky (Connect)' and updated device names
 *
 *  Update8:2014-04-08 (dlieberman)
 *         Stubbed out Spotter
 *
 *  Update9:2014-04-08 (twackford)
 *         resubscribe to events on each poll
 */

import java.text.DecimalFormat

// Wink API
private apiUrl() 			{ "https://winkapi.quirky.com/" }
private getVendorName() 	{ "Quirky Wink" }
private getVendorAuthPath()	{ "https://winkapi.quirky.com/oauth2/authorize?" }
private getVendorTokenPath(){ "https://winkapi.quirky.com/oauth2/token?" }
private getVendorIcon()		{ "https://s3.amazonaws.com/smartthings-device-icons/custom/quirky/quirky-device@2x.png" }
private getClientId() 		{ appSettings.clientId }
private getClientSecret() 	{ appSettings.clientSecret }
private getServerUrl() 		{ appSettings.serverUrl }

definition(
    name: "Quirky (Connect)",
    namespace: "wackford",
    author: "SmartThings",
    description: "Connect your Quirky to SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/quirky.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/quirky@2x.png",
    singleInstance: true
) {
	appSetting "clientId"
	appSetting "clientSecret"
    appSetting "serverUrl"
}

preferences {
	page(name: "Credentials", title: "Fetch OAuth2 Credentials", content: "authPage", install: false)
	page(name: "listDevices", title: "Quirky Devices", content: "listDevices", install: false)
}

mappings {
	path("/receivedToken") 		{ action:[ POST: "receivedToken", 			GET: "receivedToken"] }
	path("/receiveToken") 		{ action:[ POST: "receiveToken", 			GET: "receiveToken"] }
	path("/powerstripCallback")	{ action:[ POST: "powerstripEventHandler",	GET: "subscriberIdentifyVerification"]}
	path("/sensor_podCallback") { action:[ POST: "sensor_podEventHandler",	GET: "subscriberIdentifyVerification"]}
	path("/piggy_bankCallback") { action:[ POST: "piggy_bankEventHandler",	GET: "subscriberIdentifyVerification"]}
	path("/eggtrayCallback") 	{ action:[ POST: "eggtrayEventHandler",		GET: "subscriberIdentifyVerification"]}
	path("/cloud_clockCallback"){ action:[ POST: "cloud_clockEventHandler", GET: "subscriberIdentifyVerification"]}
}

def authPage() {
	log.debug "In authPage"
	if(canInstallLabs()) {
		def description = null

		if (state.vendorAccessToken == null) {
			log.debug "About to create access token."

			createAccessToken()
			description = "Tap to enter Credentials."

			def redirectUrl = oauthInitUrl()


			return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage:"listDevices", uninstall: true, install:false) {
				section { href url:redirectUrl, style:"embedded", required:false, title:"Connect to ${getVendorName()}:", description:description }
			}
		} else {
			description = "Tap 'Next' to proceed"

			return dynamicPage(name: "Credentials", title: "Credentials Accepted!", nextPage:"listDevices", uninstall: true, install:false) {
				section { href url: buildRedirectUrl("receivedToken"), style:"embedded", required:false, title:"${getVendorName()} is now connected to SmartThings!", description:description }
			}
		}
	}
	else
	{
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""


		return dynamicPage(name:"Credentials", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section {
				paragraph "$upgradeNeeded"
			}
		}

	}
}

def oauthInitUrl() {
	log.debug "In oauthInitUrl"

	/* OAuth Step 1: Request access code with our client ID */

	state.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [ response_type: "code",
		client_id: getClientId(),
		state: state.oauthInitState,
		redirect_uri: buildRedirectUrl("receiveToken") ]

	return getVendorAuthPath() + toQueryString(oauthParams)
}

def buildRedirectUrl(endPoint) {
	log.debug "In buildRedirectUrl"

	return getServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/${endPoint}"
}

def receiveToken() {
	log.debug "In receiveToken"

	def oauthParams = [ client_secret: getClientSecret(),
		grant_type: "authorization_code",
		code: params.code ]

	def tokenUrl = getVendorTokenPath() + toQueryString(oauthParams)
	def params = [
		uri: tokenUrl,
	]

	/* OAuth Step 2: Request access token with our client Secret and OAuth "Code" */
	httpPost(params) { response ->

		def data = response.data.data

		state.vendorRefreshToken = data.refresh_token //these may need to be adjusted depending on depth of returned data
		state.vendorAccessToken = data.access_token
	}

	if ( !state.vendorAccessToken ) {  //We didn't get an access token, bail on install
		return
	}

	/* OAuth Step 3: Use the access token to call into the vendor API throughout your code using state.vendorAccessToken. */

	def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=50%,height=50%,  user-scalable = yes">
        <title>${getVendorName()} Connection</title>
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
                <img src=""" + getVendorIcon() + """ alt="Vendor icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>We have located your """ + getVendorName() + """ account.</p>
                <p>Tap 'Done' to process your credentials.</p>
			</div>
        </body>
        </html>
        """
	render contentType: 'text/html', data: html
}

def receivedToken() {
	log.debug "In receivedToken"

	def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=50%,height=50%,  user-scalable = yes">
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
                <img src=""" + getVendorIcon() + """ alt="Vendor icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>Tap 'Done' to continue to Devices.</p>
			</div>
        </body>
        </html>
        """
	render contentType: 'text/html', data: html
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}


def subscriberIdentifyVerification()
{
	log.debug "In subscriberIdentifyVerification"

	def challengeToken = params.hub.challenge

	render contentType: 'text/plain', data: challengeToken
}

def initialize()
{
	log.debug "Initialized with settings: ${settings}"

	//createAccessToken()

	//state.oauthInitState = UUID.randomUUID().toString()

	settings.devices.each {
		def deviceId = it

		state.deviceDataArr.each {
			if ( it.id == deviceId ) {
				switch(it.type) {

					case "powerstrip":
						log.debug "we have a Pivot Power Genius"
						createPowerstripChildren(it.data) //has sub-devices, so we call out to create kids
						createWinkSubscription( it.subsPath, it.subsSuff )
						break

					case "sensor_pod":
						log.debug "we have a Spotter"
						addChildDevice("wackford", "Quirky Wink Spotter", deviceId, null, [name: it.name, label: it.label, completedSetup: true])
						createWinkSubscription( it.subsPath, it.subsSuff )
						break

					case "piggy_bank":
						log.debug "we have a Piggy Bank"
						addChildDevice("wackford", "Quirky Wink Porkfolio", deviceId, null, [name: it.name, label: it.label, completedSetup: true])
						createWinkSubscription( it.subsPath, it.subsSuff )
						break

					case "eggtray":
						log.debug "we have a Egg Minder"
						addChildDevice("wackford", "Quirky Wink Eggtray", deviceId, null, [name: it.name, label: it.label, completedSetup: true])
						createWinkSubscription( it.subsPath, it.subsSuff )
						break

					case "cloud_clock":
						log.debug "we have a Nimbus"
						createNimbusChildren(it.data) //has sub-devices, so we call out to create kids
						createWinkSubscription( it.subsPath, it.subsSuff )
						break
				}
			}
		}
	}
}

def getDeviceList()
{
	log.debug "In getDeviceList"

	def deviceList = [:]
	state.deviceDataArr = []

	apiGet("/users/me/wink_devices") { response ->
		response.data.data.each() {
			if ( it.powerstrip_id ) {
				deviceList["${it.powerstrip_id}"] = it.name
				state.deviceDataArr.push(['name'    : it.name,
					'id'      : it.powerstrip_id,
					'type'    : "powerstrip",
					'serial'  : it.serial,
					'data'    : it,
					'subsSuff': "/powerstripCallback",
					'subsPath': "/powerstrips/${it.powerstrip_id}/subscriptions"
				])
			}

			/* stubbing out these out for later release
			if ( it.sensor_pod_id ) {
				deviceList["${it.sensor_pod_id}"] = it.name
				state.deviceDataArr.push(['name'   : it.name,
					'id'     : it.sensor_pod_id,
					'type'   : "sensor_pod",
					'serial' : it.serial,
					'data'   : it,
					'subsSuff': "/sensor_podCallback",
					'subsPath': "/sensor_pods/${it.sensor_pod_id}/subscriptions"

				])
			}

			if ( it.piggy_bank_id ) {
				deviceList["${it.piggy_bank_id}"] = it.name
				state.deviceDataArr.push(['name'   : it.name,
										  'id'     : it.piggy_bank_id,
										  'type'   : "piggy_bank",
										  'serial' : it.serial,
										  'data'   : it,
										  'subsSuff': "/piggy_bankCallback",
										  'subsPath': "/piggy_banks/${it.piggy_bank_id}/subscriptions"
				])
			}
			if ( it.cloud_clock_id ) {
				deviceList["${it.cloud_clock_id}"] = it.name
				state.deviceDataArr.push(['name'   : it.name,
										  'id'     : it.cloud_clock_id,
										  'type'   : "cloud_clock",
										  'serial' : it.serial,
										  'data'   : it,
										  'subsSuff': "/cloud_clockCallback",
										  'subsPath': "/cloud_clocks/${it.cloud_clock_id}/subscriptions"
				])
			}
			if ( it.eggtray_id ) {
				deviceList["${it.eggtray_id}"] = it.name
				state.deviceDataArr.push(['name'   : it.name,
										  'id'     : it.eggtray_id,
										  'type'   : "eggtray",
										  'serial' : it.serial,
										  'data'   : it,
										  'subsSuff': "/eggtrayCallback",
										  'subsPath': "/eggtrays/${it.eggtray_id}/subscriptions"
				])
			} */


		}
	}
	return deviceList
}

private removeChildDevices(delete)
{
	log.debug "In removeChildDevices"

	log.debug "deleting ${delete.size()} devices"

	delete.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def uninstalled()
{
	log.debug "In uninstalled"

	removeWinkSubscriptions()

	removeChildDevices(getChildDevices())
}

def updateWinkSubscriptions()
{	//since we don't know when wink subscription dies, we'll delete and recreate on every poll
	log.debug "In updateWinkSubscriptions"

	state.deviceDataArr.each() {
		if (it.subsPath) {
			def path = it.subsPath
			def suffix = it.subsSuff
			apiGet(it.subsPath) { response ->
				response.data.data.each {
					if ( it.subscription_id ) {
						deleteWinkSubscription(path + "/", it.subscription_id)
						createWinkSubscription(path, suffix)
					}
				}
			}
		}
	}
}

def createWinkSubscription(path, suffix)
{
	log.debug "In createWinkSubscription"

	def callbackUrl = buildCallbackUrl(suffix)

	httpPostJson([
		uri : apiUrl(),
		path: path,
		body: ['callback': callbackUrl],
		headers : ['Authorization' : 'Bearer ' + state.vendorAccessToken]
	],)
		{ 	response ->
			log.debug "Created subscription ID ${response.data.data.subscription_id}"
		}
}

def deleteWinkSubscription(path, subscriptionId)
{
	log.debug "Deleting the wink subscription ${subscriptionId}"

	httpDelete([
		uri : apiUrl(),
		path: path + subscriptionId,
		headers : [ 'Authorization' : 'Bearer ' + state.vendorAccessToken ]
	],)
		{	response ->
			log.debug "Subscription ${subscriptionId} deleted"
		}
}


def removeWinkSubscriptions()
{
	log.debug "In removeSubscriptions"

	try {
		state.deviceDataArr.each() {
			if (it.subsPath) {
				def path = it.subsPath
				apiGet(it.subsPath) { response ->
					response.data.data.each {
						if ( it.subscription_id ) {
							deleteWinkSubscription(path + "/", it.subscription_id)
						}
					}
				}
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.warn "Caught HttpResponseException: $e, with status: ${e.statusCode}"
	}
}

def buildCallbackUrl(suffix)
{
	log.debug "In buildRedirectUrl"

	def serverUrl = getServerUrl()
	return serverUrl + "/api/token/${state.accessToken}/smartapps/installations/${app.id}" + suffix
}

def createChildDevice(deviceFile, dni, name, label)
{
	log.debug "In createChildDevice"

	try {
		def existingDevice = getChildDevice(dni)
		if(!existingDevice) {
			log.debug "Creating child"
			def childDevice = addChildDevice("wackford", deviceFile, dni, null, [name: name, label: label, completedSetup: true])
		} else {
			log.debug "Device $dni already exists"
		}
	} catch (e) {
		log.error "Error creating device: ${e}"
	}

}

def listDevices()
{
	log.debug "In listDevices"

	//login()

	def devices = getDeviceList()
	log.debug "Device List = ${devices}"

	dynamicPage(name: "listDevices", title: "Choose devices", install: true) {
		section("Devices") {
			input "devices", "enum", title: "Select Device(s)", required: false, multiple: true, options: devices
		}
	}
}

def apiGet(String path, Closure callback)
{
	httpGet([
		uri : apiUrl(),
		path : path,
		headers : [ 'Authorization' : 'Bearer ' + state.vendorAccessToken ]
	],)
		{
			response ->
				callback.call(response)
		}
}

def apiPut(String path, cmd, Closure callback)
{
	httpPutJson([
		uri : apiUrl(),
		path: path,
		body: cmd,
		headers : [ 'Authorization' : 'Bearer ' + state.vendorAccessToken ]
	],)

		{
			response ->
				callback.call(response)
		}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	//initialize()
	listDevices()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	//unsubscribe()
	//unschedule()
	initialize()

	listDevices()
}


def poll(childDevice)
{
	log.debug "In poll"
	log.debug childDevice

	//login()

	def dni = childDevice.device.deviceNetworkId

	log.debug dni

	def deviceType = null

	state.deviceDataArr.each() {
		if (it.id == dni) {
			deviceType = it.type
		}
	}

	log.debug "device type is: ${deviceType}"

	switch(deviceType) {	//outlets are polled in unique method not here

		case "sensor_pod":
			log.debug "Polling sensor_pod"
			getSensorPodUpdate(childDevice)
			log.debug "sensor pod status updated"
			break

		case "piggy_bank":
			log.debug "Polling piggy_bank"
			getPiggyBankUpdate(childDevice)
			log.debug "piggy bank status updated"
			break

		case "eggtray":
			log.debug "Polling eggtray"
			getEggtrayUpdate(childDevice)
			log.debug "eggtray status updated"
			break

	}
	updateWinkSubscriptions()
}

def cToF(temp) {
	return temp * 1.8 + 32
}

def fToC(temp) {
	return (temp - 32) / 1.8
}

def dollarize(int money)
{
	def value = money.toString()

	if ( value.length() == 1 ) {
		value = "00" + value
	}

	if ( value.length() == 2 ) {
		value = "0" + value
	}

	def newval = value.substring(0, value.length() - 2) + "." + value.substring(value.length()-2, value.length())
	value = newval

	def pattern = "\$0.00"
	def moneyform = new DecimalFormat(pattern)
	String output = moneyform.format(value.toBigDecimal())

	return output
}

def debugEvent(message, displayEvent) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}

/////////////////////////////////////////////////////////////////////////
//	                 START NIMBUS SPECIFIC CODE HERE
/////////////////////////////////////////////////////////////////////////
def createNimbusChildren(deviceData)
{
	log.debug "In createNimbusChildren"

	def nimbusName = deviceData.name
	def deviceFile = "Quirky-Wink-Nimbus"
	def index = 1
	deviceData.dials.each {
		log.debug "creating dial device for ${it.dial_id}"
		def dialName = "Dial ${index}"
		def dialLabel = "${nimbusName} ${dialName}"
		createChildDevice( deviceFile, it.dial_id, dialName, dialLabel )
		index++
	}
}

def cloud_clockEventHandler()
{
	log.debug "In Nimbus Event Handler..."

	def json = request.JSON
	def dials = json.dials

	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html

	if ( dials ) {
		dials.each() {
			def childDevice = getChildDevice(it.dial_id)
			childDevice?.sendEvent( name : "dial", value : it.label , unit : "" )
			childDevice?.sendEvent( name : "info", value : it.name , unit : "" )
		}
	}
}

def pollNimbus(dni)
{

	log.debug "In pollNimbus using dni # ${dni}"

	//login()

	def dials = null

	apiGet("/users/me/wink_devices") { response ->

		response.data.data.each() {
			if (it.cloud_clock_id  ) {
				log.debug "Found Nimbus #" + it.cloud_clock_id
				dials   = it.dials
				//log.debug dials
			}
		}
	}

	if ( dials ) {
		dials.each() {
			def childDevice = getChildDevice(it.dial_id)

			childDevice?.sendEvent( name : "dial", value : it.label , unit : "" )
			childDevice?.sendEvent( name : "info", value : it.name , unit : "" )

			//Change the tile/icon to what info is being displayed
			switch(it.name) {
				case "Weather":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-weather")
					break
				case "Traffic":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-traffic")
					break
				case "Time":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-time")
					break
				case "Twitter":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-twitter")
					break
				case "Calendar":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-calendar")
					break
				case "Email":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-mail")
					break
				case "Facebook":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-facebook")
					break
				case "Instagram":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-instagram")
					break
				case "Fitbit":
					childDevice?.setIcon("dial", "dial",  "st.quirky.nimbus.quirky-nimbus-fitbit")
					break
				case "Egg Minder":
					childDevice?.setIcon("dial", "dial",  "st.quirky.egg-minder.quirky-egg-device")
					break
				case "Porkfolio":
					childDevice?.setIcon("dial", "dial",  "st.quirky.porkfolio.quirky-porkfolio-side")
					break
			}
			childDevice.save()
		}
	}
	return
}

/////////////////////////////////////////////////////////////////////////
//	                 START EGG TRAY SPECIFIC CODE HERE
/////////////////////////////////////////////////////////////////////////
def getEggtrayUpdate(childDevice)
{
	log.debug "In getEggtrayUpdate"

	apiGet("/eggtrays/" + childDevice.device.deviceNetworkId) { response ->

		def data = response.data.data
		def freshnessPeriod = data.freshness_period
		def trayName = data.name
		log.debug data

		int totalEggs = 0
		int oldEggs = 0

		def now = new Date()
		def nowUnixTime = now.getTime()/1000

		data.eggs.each() { it ->
			if (it != 0)
			{
				totalEggs++

				def eggArriveDate = it
				def eggStaleDate = eggArriveDate + freshnessPeriod
				if ( nowUnixTime > eggStaleDate ){
					oldEggs++
				}
			}
		}

		int freshEggs = totalEggs - oldEggs

		if ( oldEggs > 0 ) {
			childDevice?.sendEvent(name:"inventory",value:"haveBadEgg")
			def msg = "${trayName} says: "
			msg+= "Did you know that all it takes is one bad egg? "
			msg+= "And it looks like I found one.\n\n"
			msg+= "You should probably run an Egg Report before you use any eggs."
			sendNotificationEvent(msg)
		}
		if ( totalEggs == 0 ) {
			childDevice?.sendEvent(name:"inventory",value:"noEggs")
			sendNotificationEvent("${trayName} says:\n'Oh no, I'm out of eggs!'")
			sendNotificationEvent(msg)
		}
		if ( (freshEggs == totalEggs) && (totalEggs != 0) ) {
			childDevice?.sendEvent(name:"inventory",value:"goodEggs")
		}
		childDevice?.sendEvent( name : "totalEggs", value : totalEggs , unit : "" )
		childDevice?.sendEvent( name : "freshEggs", value : freshEggs , unit : "" )
		childDevice?.sendEvent( name : "oldEggs", value : oldEggs , unit : "" )
	}
}

def runEggReport(childDevice)
{
	apiGet("/eggtrays/" + childDevice.device.deviceNetworkId) { response ->

		def data = response.data.data
		def trayName = data.name
		def freshnessPeriod = data.freshness_period
		def now = new Date()
		def nowUnixTime = now.getTime()/1000

		def eggArray = []

		def i = 0

		data.eggs.each()  { it ->
			if (it != 0 ) {
				def eggArriveDate = it
				def eggStaleDate = eggArriveDate + freshnessPeriod
				if ( nowUnixTime > eggStaleDate ){
					eggArray.push("Bad  ")
				} else {
					eggArray.push("Good ")
				}
			} else {
				eggArray.push("Empty")
			}
			i++
		}

		def msg = " Egg Report for ${trayName}\n\n"
		msg+= "#7:${eggArray[6]}    #14:${eggArray[13]}\n"
		msg+= "#6:${eggArray[5]}    #13:${eggArray[12]}\n"
		msg+= "#5:${eggArray[4]}    #12:${eggArray[11]}\n"
		msg+= "#4:${eggArray[3]}    #11:${eggArray[10]}\n"
		msg+= "#3:${eggArray[2]}    #10:${eggArray[9]}\n"
		msg+= "#2:${eggArray[1]}      #9:${eggArray[8]}\n"
		msg+= "#1:${eggArray[0]}      #8:${eggArray[7]}\n"
		msg+= "                 +\n"
		msg+= "              ===\n"
		msg+= "              ==="

		sendNotificationEvent(msg)
	}
}

def eggtrayEventHandler()
{
	log.debug "In  eggtrayEventHandler..."

	def json = request.JSON
	def dni = getChildDevice(json.eggtray_id)

	log.debug "event received from ${dni}"

	poll(dni) //sometimes events are stale, poll for all latest states


	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}

/////////////////////////////////////////////////////////////////////////
//	                 START PIGGY BANK SPECIFIC CODE HERE
/////////////////////////////////////////////////////////////////////////
def getPiggyBankUpdate(childDevice)
{
	apiGet("/piggy_banks/" + childDevice.device.deviceNetworkId) { response ->
		def status = response.data.data
		def alertData = status.triggers

		if (( alertData.enabled ) && ( state.lastCheckTime )) {
			if ( alertData.triggered_at[0].toInteger() > state.lastCheckTime ) {
				childDevice?.sendEvent(name:"acceleration",value:"active",unit:"")
			} else {
				childDevice?.sendEvent(name:"acceleration",value:"inactive",unit:"")
			}
		}

		childDevice?.sendEvent(name:"goal",value:dollarize(status.savings_goal),unit:"")

		childDevice?.sendEvent(name:"balance",value:dollarize(status.balance),unit:"")

		def now = new Date()
		def longTime = now.getTime()/1000
		state.lastCheckTime = longTime.toInteger()
	}
}

def piggy_bankEventHandler()
{
	log.debug "In  piggy_bankEventHandler..."

	def json = request.JSON
	def dni = getChildDevice(json.piggy_bank_id)

	log.debug "event received from ${dni}"

	poll(dni) //sometimes events are stale, poll for all latest states


	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}

/////////////////////////////////////////////////////////////////////////
//	                 START SENSOR POD SPECIFIC CODE HERE
/////////////////////////////////////////////////////////////////////////
def getSensorPodUpdate(childDevice)
{
	apiGet("/sensor_pods/" + childDevice.device.deviceNetworkId) { response ->
		def status = response.data.data.last_reading

		status.loudness ? childDevice?.sendEvent(name:"sound",value:"active",unit:"") :
			childDevice?.sendEvent(name:"sound",value:"inactive",unit:"")

		status.brightness ? childDevice?.sendEvent(name:"light",value:"active",unit:"") :
			childDevice?.sendEvent(name:"light",value:"inactive",unit:"")

		status.vibration ? childDevice?.sendEvent(name:"acceleration",value:"active",unit:"") :
			childDevice?.sendEvent(name:"acceleration",value:"inactive",unit:"")

		status.external_power ? childDevice?.sendEvent(name:"powerSource",value:"powered",unit:"") :
			childDevice?.sendEvent(name:"powerSource",value:"battery",unit:"")

		childDevice?.sendEvent(name:"humidity",value:status.humidity,unit:"")

		childDevice?.sendEvent(name:"battery",value:(status.battery * 100).toInteger(),unit:"")

		childDevice?.sendEvent(name:"temperature",value:cToF(status.temperature),unit:"F")
	}
}

def sensor_podEventHandler()
{
	log.debug "In  sensor_podEventHandler..."

	def json = request.JSON
	//log.debug json
	def dni = getChildDevice(json.sensor_pod_id)

	log.debug "event received from ${dni}"

	poll(dni)   //sometimes events are stale, poll for all latest states


	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}

/////////////////////////////////////////////////////////////////////////
//	                 START POWERSTRIP SPECIFIC CODE HERE
/////////////////////////////////////////////////////////////////////////

def powerstripEventHandler()
{
	log.debug "In Powerstrip Event Handler..."

	def json = request.JSON
	def outlets = json.outlets

	outlets.each() {
		def dni = getChildDevice(it.outlet_id)
		pollOutlet(dni)   //sometimes events are stale, poll for all latest states
	}

	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}

def pollOutlet(childDevice)
{
	log.debug "In pollOutlet"

	//login()

	log.debug "Polling powerstrip"
	apiGet("/outlets/" + childDevice.device.deviceNetworkId) { response ->
		def data = response.data.data
		data.powered ? childDevice?.sendEvent(name:"switch",value:"on") :
			childDevice?.sendEvent(name:"switch",value:"off")
	}
}

def on(childDevice)
{
	//login()

	apiPut("/outlets/" + childDevice.device.deviceNetworkId, [powered : true]) { response ->
		def data = response.data.data
		log.debug "Sending 'on' to device"
	}
}

def off(childDevice)
{
	//login()

	apiPut("/outlets/" + childDevice.device.deviceNetworkId, [powered : false]) { response ->
		def data = response.data.data
		log.debug "Sending 'off' to device"
	}
}

def createPowerstripChildren(deviceData)
{
	log.debug "In createPowerstripChildren"

	def powerstripName = deviceData.name
	def deviceFile = "Quirky Wink Powerstrip"

	deviceData.outlets.each {
		createChildDevice( deviceFile, it.outlet_id, it.name, "$powerstripName ${it.name}" )
	}
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}

