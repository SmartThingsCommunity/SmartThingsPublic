/**
 * Netatmo Connect
 */
import java.text.DecimalFormat
import groovy.json.JsonSlurper

private apiUrl() 			{ "https://api.netatmo.com" }
private getVendorName() 	{ "netatmo" }
private getVendorAuthPath()	{ "https://api.netatmo.com/oauth2/authorize?" }
private getVendorTokenPath(){ "https://api.netatmo.com/oauth2/token" }
private getVendorIcon()		{ "https://s3.amazonaws.com/smartapp-icons/Partner/netamo-icon-1%402x.png" }
private getClientId() 		{ appSettings.clientId }
private getClientSecret() 	{ appSettings.clientSecret }
private getServerUrl() 		{ "https://graph.api.smartthings.com" }

// Automatically generated. Make future change here.
definition(
    name: "Netatmo (Connect)",
    namespace: "dianoga",
    author: "Brian Steere",
    description: "Netatmo Integration",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/netamo-icon-1.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/netamo-icon-1%402x.png",
    oauth: true,
    singleInstance: true
){
	appSetting "clientId"
	appSetting "clientSecret"
}

preferences {
	page(name: "Credentials", title: "Fetch OAuth2 Credentials", content: "authPage", install: false)
	page(name: "listDevices", title: "Netatmo Devices", content: "listDevices", install: false)
}

mappings {
	path("/receivedToken"){action: [POST: "receivedToken", GET: "receivedToken"]}
	path("/receiveToken"){action: [POST: "receiveToken", GET: "receiveToken"]}
    path("/auth"){action: [GET: "auth"]}
}

def authPage() {
	log.debug "In authPage"
	if(canInstallLabs()) {
		def description = null

		if (state.vendorAccessToken == null) {
			log.debug "About to create access token."

			createAccessToken()
			description = "Tap to enter Credentials."

			return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage:"listDevices", uninstall: true, install:false) {
				section { href url:buildRedirectUrl("auth"), style:"embedded", required:false, title:"Connect to ${getVendorName()}:", description:description }
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

def auth() {
	redirect location: oauthInitUrl()
}

def oauthInitUrl() {
	log.debug "In oauthInitUrl"

	/* OAuth Step 1: Request access code with our client ID */

	state.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [ response_type: "code",
		client_id: getClientId(),
		state: state.oauthInitState,
		redirect_uri: buildRedirectUrl("receiveToken") ,
		scope: "read_station"
		]

	return getVendorAuthPath() + toQueryString(oauthParams)
}

def buildRedirectUrl(endPoint) {
	log.debug "In buildRedirectUrl"

	return getServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/${endPoint}"
}

def receiveToken() {
	log.debug "In receiveToken"

	def oauthParams = [
		client_secret: getClientSecret(),
		client_id: getClientId(),
		grant_type: "authorization_code",
		redirect_uri: buildRedirectUrl('receiveToken'),
		code: params.code,
		scope: "read_station"
		]

	def tokenUrl = getVendorTokenPath()
	def params = [
		uri: tokenUrl,
		contentType: 'application/x-www-form-urlencoded',
		body: oauthParams,
	]

    log.debug params

	/* OAuth Step 2: Request access token with our client Secret and OAuth "Code" */
	try {
		httpPost(params) { response ->
        	log.debug response.data
			def slurper = new JsonSlurper();

			response.data.each {key, value ->
				def data = slurper.parseText(key);
				log.debug "Data: $data"

				state.vendorRefreshToken = data.refresh_token
				state.vendorAccessToken = data.access_token
				state.vendorTokenExpires = now() + (data.expires_in * 1000)
				return
			}

		}
	} catch (Exception e) {
		log.debug "Error: $e"
	}

	log.debug "State: $state"

	if ( !state.vendorAccessToken ) {  //We didn't get an access token, bail on install
		return
	}

	/* OAuth Step 3: Use the access token to call into the vendor API throughout your code using state.vendorAccessToken. */

	def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${getVendorName()} Connection</title>
        <style type="text/css">
            * { box-sizing: border-box; }
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
                width: 100%;
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
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Withings Connection</title>
        <style type="text/css">
            * { box-sizing: border-box; }
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

// "

def refreshToken() {
	log.debug "In refreshToken"

	def oauthParams = [
		client_secret: getClientSecret(),
		client_id: getClientId(),
		grant_type: "refresh_token",
		refresh_token: state.vendorRefreshToken
		]

	def tokenUrl = getVendorTokenPath()
	def params = [
		uri: tokenUrl,
		contentType: 'application/x-www-form-urlencoded',
		body: oauthParams,
	]

	/* OAuth Step 2: Request access token with our client Secret and OAuth "Code" */
	try {
		httpPost(params) { response ->
			def slurper = new JsonSlurper();

			response.data.each {key, value ->
				def data = slurper.parseText(key);
				log.debug "Data: $data"

				state.vendorRefreshToken = data.refresh_token
				state.vendorAccessToken = data.access_token
				state.vendorTokenExpires = now() + (data.expires_in * 1000)
				return true
			}

		}
	} catch (Exception e) {
		log.debug "Error: $e"
	}

	log.debug "State: $state"

	if ( !state.vendorAccessToken ) {  //We didn't get an access token
		return false
	}
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
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
	log.debug "Initialized with settings: ${settings}"

	// Pull the latest device info into state
	getDeviceList();

	settings.devices.each {
		def deviceId = it
		def detail = state.deviceDetail[deviceId]

		try {
			switch(detail.type) {
				case 'NAMain':
					log.debug "Base station"
					createChildDevice("Netatmo Basestation", deviceId, "${detail.type}.${deviceId}", detail.module_name)
					break
				case 'NAModule1':
					log.debug "Outdoor module"
					createChildDevice("Netatmo Outdoor Module", deviceId, "${detail.type}.${deviceId}", detail.module_name)
					break
				case 'NAModule3':
					log.debug "Rain Gauge"
					createChildDevice("Netatmo Rain", deviceId, "${detail.type}.${deviceId}", detail.module_name)
					break
				case 'NAModule4':
					log.debug "Additional module"
					createChildDevice("Netatmo Additional Module", deviceId, "${detail.type}.${deviceId}", detail.module_name)
					break
			}
		} catch (Exception e) {
			log.error "Error creating device: ${e}"
		}
	}

	// Cleanup any other devices that need to go away
	def delete = getChildDevices().findAll { !settings.devices.contains(it.deviceNetworkId) }
	log.debug "Delete: $delete"
	delete.each { deleteChildDevice(it.deviceNetworkId) }

	// Do the initial poll
	poll()
	// Schedule it to run every 5 minutes
	runEvery5Minutes("poll")
}

def uninstalled() {
	log.debug "In uninstalled"

	removeChildDevices(getChildDevices())
}

def getDeviceList() {
	log.debug "In getDeviceList"

	def deviceList = [:]
	state.deviceDetail = [:]
	state.deviceState = [:]

	apiGet("/api/devicelist") { response ->
		response.data.body.devices.each { value ->
			def key = value._id
			deviceList[key] = "${value.station_name}: ${value.module_name}"
			state.deviceDetail[key] = value
            state.deviceState[key] = value.dashboard_data
		}
		response.data.body.modules.each { value ->
			def key = value._id
			deviceList[key] = "${state.deviceDetail[value.main_device].station_name}: ${value.module_name}"
			state.deviceDetail[key] = value
            state.deviceState[key] = value.dashboard_data
		}
	}

	return deviceList.sort() { it.value.toLowerCase() }
}

private removeChildDevices(delete) {
	log.debug "In removeChildDevices"

	log.debug "deleting ${delete.size()} devices"

	delete.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def createChildDevice(deviceFile, dni, name, label) {
	log.debug "In createChildDevice"

	try {
		def existingDevice = getChildDevice(dni)
		if(!existingDevice) {
			log.debug "Creating child"
			def childDevice = addChildDevice("dianoga", deviceFile, dni, null, [name: name, label: label, completedSetup: true])
		} else {
			log.debug "Device $dni already exists"
		}
	} catch (e) {
		log.error "Error creating device: ${e}"
	}
}

def listDevices() {
	log.debug "In listDevices"

	def devices = getDeviceList()

	dynamicPage(name: "listDevices", title: "Choose devices", install: true) {
		section("Devices") {
			input "devices", "enum", title: "Select Device(s)", required: false, multiple: true, options: devices
		}

        section("Preferences") {
        	input "rainUnits", "enum", title: "Rain Units", description: "Millimeters (mm) or Inches (in)", required: true, options: [mm:'Millimeters', in:'Inches']
        }
	}
}

def apiGet(String path, Map query, Closure callback) {
	if(now() >= state.vendorTokenExpires) {
		refreshToken();
	}

	query['access_token'] = state.vendorAccessToken
	def params = [
		uri: apiUrl(),
		path: path,
		'query': query
	]
	// log.debug "API Get: $params"

	try {
		httpGet(params)	{ response ->
			callback.call(response)
		}
	} catch (Exception e) {
		// This is most likely due to an invalid token. Try to refresh it and try again.
		log.debug "apiGet: Call failed $e"
		if(refreshToken()) {
			log.debug "apiGet: Trying again after refreshing token"
			httpGet(params)	{ response ->
				callback.call(response)
			}
		}
	}
}

def apiGet(String path, Closure callback) {
	apiGet(path, [:], callback);
}

def poll() {
	log.debug "In Poll"
	getDeviceList();
	def children = getChildDevices()
    log.debug "State: ${state.deviceState}"

	settings.devices.each { deviceId ->
		def detail = state.deviceDetail[deviceId]
		def data = state.deviceState[deviceId]
		def child = children.find { it.deviceNetworkId == deviceId }

		log.debug "Update: $child";
		switch(detail.type) {
			case 'NAMain':
				log.debug "Updating NAMain $data"
				child?.sendEvent(name: 'temperature', value: cToPref(data['Temperature']) as float, unit: getTemperatureScale())
				child?.sendEvent(name: 'carbonDioxide', value: data['CO2'])
				child?.sendEvent(name: 'humidity', value: data['Humidity'])
				child?.sendEvent(name: 'pressure', value: data['Pressure'])
				child?.sendEvent(name: 'noise', value: data['Noise'])
				break;
			case 'NAModule1':
				log.debug "Updating NAModule1 $data"
				child?.sendEvent(name: 'temperature', value: cToPref(data['Temperature']) as float, unit: getTemperatureScale())
				child?.sendEvent(name: 'humidity', value: data['Humidity'])
				break;
			case 'NAModule3':
				log.debug "Updating NAModule3 $data"
				child?.sendEvent(name: 'rain', value: rainToPref(data['Rain']) as float, unit: settings.rainUnits)
				child?.sendEvent(name: 'rainSumHour', value: rainToPref(data['sum_rain_1']) as float, unit: settings.rainUnits)
				child?.sendEvent(name: 'rainSumDay', value: rainToPref(data['sum_rain_24']) as float, unit: settings.rainUnits)
				child?.sendEvent(name: 'units', value: settings.rainUnits)
				break;
			case 'NAModule4':
				log.debug "Updating NAModule4 $data"
				child?.sendEvent(name: 'temperature', value: cToPref(data['Temperature']) as float, unit: getTemperatureScale())
				child?.sendEvent(name: 'carbonDioxide', value: data['CO2'])
				child?.sendEvent(name: 'humidity', value: data['Humidity'])
				break;
		}
	}
}

def cToPref(temp) {
	if(getTemperatureScale() == 'C') {
    	return temp
    } else {
		return temp * 1.8 + 32
    }
}

def rainToPref(rain) {
	if(settings.rainUnits == 'mm') {
    	return rain
    } else {
    	return rain * 0.039370
    }
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

private Boolean canInstallLabs() {
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware) {
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions() {
	return location.hubs*.firmwareVersionString.findAll { it }
}
