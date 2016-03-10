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
 *      10-28-2015 DVCSMP-604 - accessory sensor, DVCSMP-1174, DVCSMP-1111 - not respond to routines
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
}

preferences {
	page(name: "auth", title: "ecobee", nextPage:"", content:"authPage", uninstall: true, install:true)
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}

def authPage() {
	log.debug "authPage()"

	if(!atomicState.accessToken) { //this is to access token for 3rd party to make a call to connect app
		atomicState.accessToken = createAccessToken()
	}

	def description
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(atomicState.authToken) {
		description = "You are connected."
		uninstallAllowed = true
		oauthTokenProvided = true
	} else {
		description = "Click to enter Ecobee Credentials"
	}

	def redirectUrl = buildRedirectUrl
	log.debug "RedirectUrl = ${redirectUrl}"
	// get rid of next button until the user is actually auth'd
	if (!oauthTokenProvided) {
		return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap below to log in to the ecobee service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title:"ecobee", description:description
			}
		}
	} else {
		def stats = getEcobeeThermostats()
		log.debug "thermostat list: $stats"
		log.debug "sensor list: ${sensorsDiscovered()}"
		return dynamicPage(name: "auth", title: "Select Your Thermostats", uninstall: true) {
			section(""){
				paragraph "Tap below to see the list of ecobee thermostats available in your ecobee account and select the ones you want to connect to SmartThings."
				input(name: "thermostats", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:stats])
			}

			def options = sensorsDiscovered() ?: []
			def numFound = options.size() ?: 0
			if (numFound > 0)  {
				section(""){
					paragraph "Tap below to see the list of ecobee sensors available in your ecobee account and select the ones you want to connect to SmartThings."
					input(name: "ecobeesensors", title:"Select Ecobee Sensors (${numFound} found)", type: "enum", required:false, description: "Tap to choose", multiple:true, options:options)
				}
			}
		}
	}
}

def oauthInitUrl() {
	log.debug "oauthInitUrl with callback: ${callbackUrl}"

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			scope: "smartRead,smartWrite",
			client_id: smartThingsClientId,
			state: atomicState.oauthInitState,
			redirect_uri: callbackUrl
	]

	redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
}

def callback() {
	log.debug "callback()>> params: $params, params.code ${params.code}"

	def code = params.code
	def oauthState = params.state

	if (oauthState == atomicState.oauthInitState){

		def tokenParams = [
				grant_type: "authorization_code",
				code      : code,
				client_id : smartThingsClientId,
				redirect_uri: callbackUrl
		]

		def tokenUrl = "https://www.ecobee.com/home/token?${toQueryString(tokenParams)}"

		httpPost(uri: tokenUrl) { resp ->
			atomicState.refreshToken = resp.data.refresh_token
			atomicState.authToken = resp.data.access_token
			log.debug "swapped token: $resp.data"
			log.debug "atomicState.refreshToken: ${atomicState.refreshToken}"
			log.debug "atomicState.authToken: ${atomicState.authToken}"
		}

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
	def message = """
    <p>Your ecobee Account is now connected to SmartThings!</p>
    <p>Click 'Done' to finish setup.</p>
    """
	connectionStatus(message)
}

def fail() {
	def message = """
        <p>The connection could not be established!</p>
        <p>Click 'Done' to return to the menu.</p>
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
<meta name="viewport" content="width=640">
<title>Ecobee & SmartThings connection</title>
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
                width: 90%;
                padding: 4%;
                /*background: #eee;*/
                text-align: center;
        }
        img {
                vertical-align: middle;
        }
        p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                padding: 0 40px;
                margin-bottom: 0;
        }
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
                ${message}
        </div>
</body>
</html>
"""

	render contentType: 'text/html', data: html
}

def getEcobeeThermostats() {
	log.debug "getting device list"
	atomicState.remoteSensors = []

	def requestBody = '{"selection":{"selectionType":"registered","selectionMatch":"","includeRuntime":true,"includeSensors":true}}'

	def deviceListParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
			query: [format: 'json', body: requestBody]
	]

	def stats = [:]
	try {
		httpGet(deviceListParams) { resp ->

			if (resp.status == 200) {
				resp.data.thermostatList.each { stat ->
					atomicState.remoteSensors = atomicState.remoteSensors == null ? stat.remoteSensors : atomicState.remoteSensors <<  stat.remoteSensors
					def dni = [app.id, stat.identifier].join('.')
					stats[dni] = getThermostatDisplayName(stat)
				}
			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
        log.trace "Exception polling children: " + e.response.data.status
        if (e.response.data.status.code == 14) {
            atomicState.action = "getEcobeeThermostats"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
    }
	atomicState.thermostats = stats
	return stats
}

Map sensorsDiscovered() {
	def map = [:]
	log.info "list ${atomicState.remoteSensors}"
	atomicState.remoteSensors.each { sensors ->
		sensors.each {
			if (it.type != "thermostat") {
				def value = "${it?.name}"
				def key = "ecobee_sensor-"+ it?.id + "-" + it?.code
				map["${key}"] = value
			}
		}
	}
	atomicState.sensors = map
	return map
}

def getThermostatDisplayName(stat) {
	if(stat?.name)
		return stat.name.toString()
	return (getThermostatTypeName(stat) + " (${stat.identifier})").toString()
}

def getThermostatTypeName(stat) {
	return stat.modelNumber == "siSmart" ? "Smart Si" : "Smart"
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

	log.debug "initialize"
	def devices = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getChildName(), dni, null, ["label":"${atomicState.thermostats[dni]}" ?: "Ecobee Thermostat"])
			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}

	def sensors = ecobeesensors.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getSensorChildName(), dni, null, ["label":"${atomicState.sensors[dni]}" ?:"Ecobee Sensor"])
			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}
	log.debug "created ${devices.size()} thermostats and ${sensors.size()} sensors."

	def delete  // Delete any that are no longer in settings
	if(!thermostats && !ecobeesensors) {
		log.debug "delete thermostats ands sensors"
		delete = getAllChildDevices() //inherits from SmartApp (data-management)
	} else { //delete only thermostat
		log.debug "delete individual thermostat and sensor"
		if (!ecobeesensors) {
			delete = getChildDevices().findAll { !thermostats.contains(it.deviceNetworkId) }
		} else {
			delete = getChildDevices().findAll { !thermostats.contains(it.deviceNetworkId) && !ecobeesensors.contains(it.deviceNetworkId)}
		}
	}
	log.warn "delete: ${delete}, deleting ${delete.size()} thermostats"
	delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

	atomicState.thermostatData = [:] //reset Map to store thermostat data

	//send activity feeds to tell that device is connected
	def notificationMessage = "is connected to SmartThings"
	sendActivityFeeds(notificationMessage)
	atomicState.timeSendPush = null
	atomicState.reAttempt = 0

	pollHandler() //first time polling data data from thermostat

	//automatically update devices status every 5 mins
	runEvery5Minutes("poll")

}

def pollHandler() {
	log.debug "pollHandler()"
	pollChildren(null) // Hit the ecobee API for update on all thermostats

	atomicState.thermostats.each {stat ->
		def dni = stat.key
		log.debug ("DNI = ${dni}")
		def d = getChildDevice(dni)
		if(d) {
			log.debug ("Found Child Device.")
			d.generateEvent(atomicState.thermostats[dni].data)
		}
	}
}

def pollChildren(child = null) {
	def thermostatIdsString = getChildDeviceIdsString()
	log.debug "polling children: $thermostatIdsString"
	def data = ""

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeExtendedRuntime":"true","includeSettings":"true","includeRuntime":"true","includeSensors":true}}'
	def result = false

	def pollParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
			query: [format: 'json', body: jsonRequestBody]
	]

	try{
		httpGet(pollParams) { resp ->
			if(resp.status == 200) {
				log.debug "poll results returned resp.data ${resp.data}"
				atomicState.remoteSensors = resp.data.thermostatList.remoteSensors
				atomicState.thermostatData = resp.data
				updateSensorData()
				atomicState.thermostats = resp.data.thermostatList.inject([:]) { collector, stat ->
					def dni = [ app.id, stat.identifier ].join('.')

					log.debug "updating dni $dni"

					data = [
							coolMode: (stat.settings.coolStages > 0),
							heatMode: (stat.settings.heatStages > 0),
							deviceTemperatureUnit: stat.settings.useCelsius,
							minHeatingSetpoint: (stat.settings.heatRangeLow / 10),
							maxHeatingSetpoint: (stat.settings.heatRangeHigh / 10),
							minCoolingSetpoint: (stat.settings.coolRangeLow / 10),
							maxCoolingSetpoint: (stat.settings.coolRangeHigh / 10),
							autoMode: stat.settings.autoHeatCoolFeatureEnabled,
							auxHeatMode: (stat.settings.hasHeatPump) && (stat.settings.hasForcedAir || stat.settings.hasElectric || stat.settings.hasBoiler),
							temperature: (stat.runtime.actualTemperature / 10),
							heatingSetpoint: stat.runtime.desiredHeat / 10,
							coolingSetpoint: stat.runtime.desiredCool / 10,
							thermostatMode: stat.settings.hvacMode,
							humidity: stat.runtime.actualHumidity,
							thermostatFanMode: stat.runtime.desiredFanMode
					]

					if (location.temperatureScale == "F")
					{
						data["temperature"] = data["temperature"] ? Math.round(data["temperature"].toDouble()) : data["temperature"]
						data["heatingSetpoint"] = data["heatingSetpoint"] ? Math.round(data["heatingSetpoint"].toDouble()) : data["heatingSetpoint"]
						data["coolingSetpoint"] = data["coolingSetpoint"] ? Math.round(data["coolingSetpoint"].toDouble()) : data["coolingSetpoint"]
						data["minHeatingSetpoint"] = data["minHeatingSetpoint"] ? Math.round(data["minHeatingSetpoint"].toDouble()) : data["minHeatingSetpoint"]
						data["maxHeatingSetpoint"] = data["maxHeatingSetpoint"] ? Math.round(data["maxHeatingSetpoint"].toDouble()) : data["maxHeatingSetpoint"]
						data["minCoolingSetpoint"] = data["minCoolingSetpoint"] ? Math.round(data["minCoolingSetpoint"].toDouble()) : data["minCoolingSetpoint"]
						data["maxCoolingSetpoint"] = data["maxCoolingSetpoint"] ? Math.round(data["maxCoolingSetpoint"].toDouble()) : data["maxCoolingSetpoint"]

					}

					if (data?.deviceTemperatureUnit == false && location.temperatureScale == "F") {
						data["deviceTemperatureUnit"] = "F"

					} else {
						data["deviceTemperatureUnit"] = "C"
					}

					collector[dni] = [data:data]
					return collector
				}
				result = true
				log.debug "updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.trace "Exception polling children: " + e.response.data.status
        if (e.response.data.status.code == 14) {
            atomicState.action = "pollChildren"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
	}
	return result
}

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild(child){

	if (pollChildren(child)){
		if (!child.device.deviceNetworkId.startsWith("ecobee_sensor")){
			if(atomicState.thermostats[child.device.deviceNetworkId] != null) {
				def tData = atomicState.thermostats[child.device.deviceNetworkId]
				log.info "pollChild(child)>> data for ${child.device.deviceNetworkId} : ${tData.data}"
				child.generateEvent(tData.data) //parse received message from parent
			} else if(atomicState.thermostats[child.device.deviceNetworkId] == null) {
				log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId}"
				return null
			}
		}
	} else {
		log.info "ERROR: pollChildren(child) for ${child.device.deviceNetworkId} after polling"
		return null
	}

}

void poll() {
	def devices = getChildDevices()
	devices.each {pollChild(it)}
}

def availableModes(child) {

	debugEvent ("atomicState.thermostats = ${atomicState.thermostats}")

	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = atomicState.thermostats[child.device.deviceNetworkId]

	debugEvent("Data = ${tData}")

	if(!tData)
	{
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

		return null
	}

	def modes = ["off"]

	if (tData.data.heatMode) modes.add("heat")
	if (tData.data.coolMode) modes.add("cool")
	if (tData.data.autoMode) modes.add("auto")
	if (tData.data.auxHeatMode) modes.add("auxHeatOnly")

	modes

}

def currentMode(child) {
	debugEvent ("atomicState.Thermos = ${atomicState.thermostats}")

	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = atomicState.thermostats[child.device.deviceNetworkId]

	debugEvent("Data = ${tData}")

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"


		return null
	}

	def mode = tData.data.thermostatMode

	mode
}

def updateSensorData() {
	atomicState.remoteSensors.each {
		it.each {
			if (it.type != "thermostat") {
				def temperature = ""
				def occupancy = ""
				it.capability.each {
					if (it.type == "temperature") {
						if (it.value == "unknown") {
							temperature = "--"
						} else {
							if (location.temperatureScale == "F") {
								temperature = Math.round(it.value.toDouble() / 10)
							} else {
								temperature = convertFtoC(it.value.toDouble() / 10)
							}

						}

					} else if (it.type == "occupancy") {
						if(it.value == "true")
							occupancy = "active"
						else
							occupancy = "inactive"
					}
				}
				def dni = "ecobee_sensor-"+ it?.id + "-" + it?.code
				def d = getChildDevice(dni)
				if(d) {
					d.sendEvent(name:"temperature", value: temperature)
					d.sendEvent(name:"motion", value: occupancy)
				}
			}
		}
	}
}

def getChildDeviceIdsString() {
	return thermostats.collect { it.split(/\./).last() }.join(',')
}

def toJson(Map m) {
	return new org.json.JSONObject(m).toString()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private refreshAuthToken() {
	log.debug "refreshing auth token"

	if(!atomicState.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {

		def refreshParams = [
				method: 'POST',
				uri   : apiEndpoint,
				path  : "/token",
				query : [grant_type: 'refresh_token', code: "${atomicState.refreshToken}", client_id: smartThingsClientId],
		]

		log.debug refreshParams

		def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
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

							"${atomicState.action}"()

							atomicState.action = ""
						}

					}
					atomicState.action = ""
				}
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "refreshAuthToken() >> Error: e.statusCode ${e.statusCode}"
			def reAttemptPeriod = 300 // in sec
			if (e.statusCode != 401) { // this issue might comes from exceed 20sec app execution, connectivity issue etc.
				runIn(reAttemptPeriod, "refreshAuthToken")
			} else if (e.statusCode == 401) { // unauthorized
				atomicState.reAttempt = atomicState.reAttempt + 1
				log.warn "reAttempt refreshAuthToken to try = ${atomicState.reAttempt}"
				if (atomicState.reAttempt <= 3) {
					runIn(reAttemptPeriod, "refreshAuthToken")
				} else {
					sendPushAndFeeds(notificationMessage)
					atomicState.reAttempt = 0
				}
			}
		}
	}
}

def resumeProgram(child, deviceId) {


	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}]}'
	def result = sendJson(jsonRequestBody)
	return result
}

def setHold(child, heating, cooling, deviceId, sendHoldType) {

	int h = heating * 10
	int c = cooling * 10
	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": '+sendHoldType+' } } ]}'

	def result = sendJson(child, jsonRequestBody)
	return result
}

def setFanMode(child, heating, cooling, deviceId, sendHoldType, fanMode) {

	int h = heating * 10
	int c = cooling * 10


	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": '+sendHoldType+', "fan": '+fanMode+' } } ]}'
	def result = sendJson(child, jsonRequestBody)
	return result
}

def setMode(child, mode, deviceId) {
	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"thermostat": {"settings":{"hvacMode":"'+"${mode}"+'"}}}'

	def result = sendJson(jsonRequestBody)
	return result
}

def sendJson(child = null, String jsonBody) {

	def returnStatus = false
	def cmdParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
			body: jsonBody
	]

	try{
		httpPost(cmdParams) { resp ->

			if(resp.status == 200) {

				log.debug "updated ${resp.data}"
				returnStatus = resp.data.status.code
				if (resp.data.status.code == 0)
					log.debug "Successful call to ecobee API."
				else {
					log.debug "Error return code = ${resp.data.status.code}"
					debugEvent("Error return code = ${resp.data.status.code}")
				}
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
        log.trace "Exception Sending Json: " + e.response.data.status
        debugEvent ("sent Json & got http status ${e.statusCode} - ${e.response.data.status.code}")
        if (e.response.data.status.code == 14) {
            atomicState.action = "pollChildren"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
        else {
            debugEvent("Authentication error, invalid authentication method, lack of credentials, etc.")
            log.error "Authentication error, invalid authentication method, lack of credentials, etc."
        }
    }

	if (returnStatus == 0)
		return true
	else
		return false
}

def getChildName()           { "Ecobee Thermostat" }
def getSensorChildName()     { "Ecobee Sensor" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getShardUrl()            { return getApiServerUrl() }
def getCallbackUrl()        { "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()   { "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }
def getApiEndpoint()        { "https://api.ecobee.com" }
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

def debugEventFromParent(child, message) {
	if (child != null) { child.sendEvent("name":"debugEventFromParent", "value":message, "description":message, displayed: true, isStateChange: true)}
}

//send both push notification and mobile activity feeds
def sendPushAndFeeds(notificationMessage){
	log.warn "sendPushAndFeeds >> notificationMessage: ${notificationMessage}"
	log.warn "sendPushAndFeeds >> atomicState.timeSendPush: ${atomicState.timeSendPush}"
	if (atomicState.timeSendPush){
		if (now() - atomicState.timeSendPush > 86400000){ // notification is sent to remind user once a day
			sendPush("Your Ecobee thermostat " + notificationMessage)
			sendActivityFeeds(notificationMessage)
			atomicState.timeSendPush = now()
		}
	} else {
		sendPush("Your Ecobee thermostat " + notificationMessage)
		sendActivityFeeds(notificationMessage)
		atomicState.timeSendPush = now()
	}
	atomicState.authToken = null
}

def sendActivityFeeds(notificationMessage) {
	def devices = getChildDevices()
	devices.each { child ->
		child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
	}
}

def roundC (tempC) {
	return String.format("%.1f", (Math.round(tempC * 2))/2)
}

def convertFtoC (tempF) {
	return String.format("%.1f", (Math.round(((tempF - 32)*(5/9)) * 2))/2)
}

def convertCtoF (tempC) {
	return (Math.round(tempC * (9/5)) + 32).toInteger()
}
