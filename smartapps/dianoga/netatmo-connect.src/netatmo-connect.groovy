/**
 * Netatmo Connect
 */
import java.text.DecimalFormat
import groovy.json.JsonSlurper

private getApiUrl()			{ "https://api.netatmo.com" }
private getVendorAuthPath()	{ "${apiUrl}/oauth2/authorize?" }
private getVendorTokenPath(){ "${apiUrl}/oauth2/token" }
private getVendorIcon()		{ "https://s3.amazonaws.com/smartapp-icons/Partner/netamo-icon-1%402x.png" }
private getClientId()		{ appSettings.clientId }
private getClientSecret()	{ appSettings.clientSecret }
private getServerUrl() 		{ appSettings.serverUrl }
private getShardUrl()		{ return getApiServerUrl() }
private getCallbackUrl()	{ "${serverUrl}/oauth/callback" }
private getBuildRedirectUrl() { "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }

definition(
	name: "Netatmo (Connect)",
	namespace: "dianoga",
	author: "Brian Steere",
	description: "Integrate your Netatmo devices with SmartThings",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/netamo-icon-1.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/netamo-icon-1%402x.png",
	oauth: true,
	singleInstance: true
){
	appSetting "clientId"
	appSetting "clientSecret"
	appSetting "serverUrl"
}

preferences {
	page(name: "Credentials", title: "Fetch OAuth2 Credentials", content: "authPage", install: false)
	page(name: "listDevices", title: "Netatmo Devices", content: "listDevices", install: false)
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}

def authPage() {
	// log.debug "running authPage()"

	def description
	def uninstallAllowed = false
	def oauthTokenProvided = false

	// If an access token doesn't exist, create one
	if (!atomicState.accessToken) {
		atomicState.accessToken = createAccessToken()
        log.debug "Created access token"
	}

	if (canInstallLabs()) {

		def redirectUrl = getBuildRedirectUrl()
		// log.debug "Redirect url = ${redirectUrl}"

		if (atomicState.authToken) {
			description = "Tap 'Next' to select devices"
			uninstallAllowed = true
			oauthTokenProvided = true
		} else {
			description = "Tap to enter credentials"
		}

		if (!oauthTokenProvided) {
			log.debug "Showing the login page"
			return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage:"listDevices", uninstall: uninstallAllowed, install:false) {
				section() {
					paragraph "Tap below to login to Netatmo and authorize SmartThings access"
					href url:redirectUrl, style:"embedded", required:false, title:"Connect to Netatmo", description:description
				}
			}
		} else {
			log.debug "Showing the devices page"
			return dynamicPage(name: "Credentials", title: "Connected", nextPage:"listDevices", uninstall: uninstallAllowed, install:false) {
				section() {
					input(name:"Devices", style:"embedded", required:false, title:"Netatmo is connected to SmartThings", description:description) 
				}
			}
		}
	} else {
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date. To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""
		return dynamicPage(name:"Credentials", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section {
				paragraph "$upgradeNeeded"
			}
		}

	}
}

def oauthInitUrl() {
	// log.debug "runing oauthInitUrl()"

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
		response_type: "code",
		client_id: getClientId(),
		client_secret: getClientSecret(),
		state: atomicState.oauthInitState,
		redirect_uri: getCallbackUrl(),
		scope: "read_station"
	]

	// log.debug "REDIRECT URL: ${getVendorAuthPath() + toQueryString(oauthParams)}"

	redirect (location: getVendorAuthPath() + toQueryString(oauthParams))
}

def callback() {
	// log.debug "running callback()"

	def code = params.code
	def oauthState = params.state

	if (oauthState == atomicState.oauthInitState) {

		def tokenParams = [
        	grant_type: "authorization_code",
			client_secret: getClientSecret(),
			client_id : getClientId(),
			code: code,
			scope: "read_station",
            redirect_uri: getCallbackUrl()
		]

		// log.debug "TOKEN URL: ${getVendorTokenPath() + toQueryString(tokenParams)}"

		def tokenUrl = getVendorTokenPath()
		def requestTokenParams = [
			uri: tokenUrl,
			requestContentType: 'application/x-www-form-urlencoded',
			body: tokenParams
		]
    
		// log.debug "PARAMS: ${requestTokenParams}"

		try {
            httpPost(requestTokenParams) { resp ->
                //log.debug "Data: ${resp.data}"
                atomicState.refreshToken = resp.data.refresh_token
                atomicState.authToken = resp.data.access_token
                // resp.data.expires_in is in milliseconds so we need to convert it to seconds
                atomicState.tokenExpires = now() + (resp.data.expires_in * 1000)
            }
        } catch (e) {
			      log.debug "callback() failed: $e"
        }

		// If we successfully got an authToken run sucess(), else fail()
		if (atomicState.authToken) {
			success()
		} else {
			fail()
		}

	} else {
		log.error "callback() failed oauthState != atomicState.oauthInitState"
	}
}

def success() {
	log.debug "OAuth flow succeeded"
	def message = """
	<p>Success!</p>
	<p>Tap 'Done' to continue</p>
	"""
	connectionStatus(message)
}

def fail() {
	log.debug "OAuth flow failed"
    atomicState.authToken = null
	def message = """
	<p>Error</p>
	<p>Tap 'Done' to return</p>
	"""
	connectionStatus(message)
}

def connectionStatus(message, redirectUrl = null) {
	def redirectHtml = ""
	if (redirectUrl) {
		redirectHtml = """
			<meta http-equiv="refresh" content="3; url=${redirectUrl}" />
		"""
	}
	def html = """
		<!DOCTYPE html>
		<html>
		<head>
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<title>Netatmo Connection</title>
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
				${message}
			</div>
        </body>
        </html>
	"""
	render contentType: 'text/html', data: html
}

def refreshToken() {
	// Check if atomicState has a refresh token
	if (atomicState.refreshToken) {
        log.debug "running refreshToken()"

        def oauthParams = [
            grant_type: "refresh_token",
            refresh_token: atomicState.refreshToken,
            client_secret: getClientSecret(),
            client_id: getClientId(),
        ]

        def tokenUrl = getVendorTokenPath()
        
        def requestOauthParams = [
            uri: tokenUrl,
            requestContentType: 'application/x-www-form-urlencoded',
            body: oauthParams
        ]
        
        // log.debug "PARAMS: ${requestOauthParams}"

        try {
            httpPost(requestOauthParams) { resp ->
            	//log.debug "Data: ${resp.data}"
                atomicState.refreshToken = resp.data.refresh_token
                atomicState.authToken = resp.data.access_token
                // resp.data.expires_in is in milliseconds so we need to convert it to seconds
                atomicState.tokenExpires = now() + (resp.data.expires_in * 1000)
                return true
            }
        } catch (e) {
            log.debug "refreshToken() failed: $e"
        }

        // If we didn't get an authToken
        if (!atomicState.authToken) {
            return false
        }
	} else {
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
	getDeviceList()

	settings.devices.each {
		def deviceId = it
		def detail = state?.deviceDetail[deviceId]

		try {
			switch(detail?.type) {
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
    
	// Run initial poll and schedule future polls
	poll()
	runEvery5Minutes("poll")
}

def uninstalled() {
	log.debug "Uninstalling"
	removeChildDevices(getChildDevices())
}

def getDeviceList() {
	if (atomicState.authToken) {
    
        log.debug "Getting stations data"

        def deviceList = [:]
        state.deviceDetail = [:]
        state.deviceState = [:]

        apiGet("/api/getstationsdata") { resp ->
            resp.data.body.devices.each { value ->
                def key = value._id
                deviceList[key] = "${value.station_name}: ${value.module_name}"
                state.deviceDetail[key] = value
                state.deviceState[key] = value.dashboard_data
                value.modules.each { value2 ->            
                    def key2 = value2._id
                    deviceList[key2] = "${value.station_name}: ${value2.module_name}"
                    state.deviceDetail[key2] = value2
                    state.deviceState[key2] = value2.dashboard_data            
                }
            }
        }
        
        return deviceList.sort() { it.value.toLowerCase() }
        
	} else {
    	return null
  }
}

private removeChildDevices(delete) {
	log.debug "Removing ${delete.size()} devices"
	delete.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def createChildDevice(deviceFile, dni, name, label) {
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
	log.debug "Listing devices"

	def devices = getDeviceList()

	dynamicPage(name: "listDevices", title: "Choose Devices", install: true) {
		section("Devices") {
			input "devices", "enum", title: "Select Devices", required: false, multiple: true, options: devices
		}

        section("Preferences") {
        	input "rainUnits", "enum", title: "Rain Units", description: "Millimeters (mm) or Inches (in)", required: true, options: [mm:'Millimeters', in:'Inches']
        }
	}
}

def apiGet(String path, Map query, Closure callback) {
	log.debug "running apiGet()"
    
    // If the current time is over the expiration time, request a new token
	if(now() >= atomicState.tokenExpires) {
    	atomicState.authToken = null
		refreshToken()
	}

	def queryParam = [
    	access_token: atomicState.authToken
    ]
    
	def apiGetParams = [
		uri: getApiUrl(),
		path: path,
		query: queryParam
	]
    
	// log.debug "apiGet(): $apiGetParams"

	try {
		httpGet(apiGetParams) { resp ->
			callback.call(resp)
		}
	} catch (e) {
		log.debug "apiGet() failed: $e"
        // Netatmo API has rate limits so a failure here doesn't necessarily mean our token has expired, but we will check anyways
        if(now() >= atomicState.tokenExpires) {
    		atomicState.authToken = null
			refreshToken()
		}
	}
}

def apiGet(String path, Closure callback) {
	apiGet(path, [:], callback);
}

def poll() {
	log.debug "Polling..."
    
	getDeviceList()
    
	def children = getChildDevices()
    //log.debug "State: ${state.deviceState}"

	settings.devices.each { deviceId ->
		def detail = state?.deviceDetail[deviceId]
		def data = state?.deviceState[deviceId]
		def child = children?.find { it.deviceNetworkId == deviceId }

		log.debug "Update: $child";
		switch(detail?.type) {
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
	sendEvent(results)
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