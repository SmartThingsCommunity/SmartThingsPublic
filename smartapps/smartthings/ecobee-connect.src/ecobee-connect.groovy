/**
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

	def redirectUrl = buildRedirectUrl //"${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}"
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
			redirect_uri: callbackUrl //"https://graph.api.smartthings.com/oauth/callback"
	]

	redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
}

def callback() {
	log.debug "callback()>> params: $params, params.code ${params.code}"

	def code = params.code
	def oauthState = params.state

	//verify oauthState == atomicState.oauthInitState, so the callback corresponds to the authentication request
	if (oauthState == atomicState.oauthInitState){

		def tokenParams = [
				grant_type: "authorization_code",
				code      : code,
				client_id : smartThingsClientId,
				redirect_uri: callbackUrl //"https://graph.api.smartthings.com/oauth/callback"
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
                    atomicState.remoteSensors = stat.remoteSensors
                    def dni = [app.id, stat.identifier].join('.')
                    stats[dni] = getThermostatDisplayName(stat)
                }
            } else {
                log.debug "http status: ${resp.status}"
                //refresh the auth token
                if (resp.status == 500 && resp.data.status.code == 14) {
                    log.debug "Storing the failed action to try later"
                    atomicState.action = "getEcobeeThermostats"
                    log.debug "Refreshing your auth_token!"
                    refreshAuthToken()
                } else {
                    log.error "Authentication error, invalid authentication method, lack of credentials, etc."
                }
            }
        }
    } catch(Exception e) {
        log.debug "___exception getEcobeeThermostats(): " + e
        refreshAuthToken()
    }
	atomicState.thermostats = stats
	return stats
}

Map sensorsDiscovered() {
	def map = [:]
	atomicState.remoteSensors.each {
		if (it.type != "thermostat") {
			def value = "${it?.name}"
			def key = "ecobee_sensor-"+ it?.id + "-" + it?.code
			map["${key}"] = value
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
			d = addChildDevice(app.namespace, getChildName(), dni, null, ["label":"Ecobee Thermostat:${atomicState.thermostats[dni]}"])
			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}

	def sensors = ecobeesensors.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getSensorChildName(), dni, null, ["label":"Ecobee Sensor:${atomicState.sensors[dni]}"])
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
    state.timeSendPush = null

	pollHandler() //first time polling data data from thermostat

	//automatically update devices status every 5 mins
	runEvery5Minutes("poll")

    //since access_token expires every 2 hours
    runEvery1Hour("refreshAuthToken")

	atomicState.reAttempt = 0

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

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeExtendedRuntime":"true","includeSettings":"true","includeRuntime":"true","includeSensors":true}}'
	def result = false
	// // TODO: test this:
	//
	// def jsonRequestBody = toJson([
	//	selection:[
	//		selectionType: "thermostats",
	//		   selectionMatch: getChildDeviceIdsString(),
	//		   includeRuntime: true
	//	   ]
	// ])

	def pollParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
			query: [format: 'json', body: jsonRequestBody]
	]

	try{
		httpGet(pollParams) { resp ->

//            if (resp.data) {
//                debugEventFromParent(child, "pollChildren(child) >> resp.status = ${resp.status}, resp.data = ${resp.data}")
//            }

			if(resp.status == 200) {
				log.debug "poll results returned resp.data ${resp.data}"
				atomicState.remoteSensors = resp.data.thermostatList.remoteSensors
				atomicState.thermostatData = resp.data
				updateSensorData()
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
					data["temperature"] = data["temperature"] ? data["temperature"].toDouble().toInteger() : data["temperature"]
					data["heatingSetpoint"] = data["heatingSetpoint"] ? data["heatingSetpoint"].toDouble().toInteger() : data["heatingSetpoint"]
					data["coolingSetpoint"] = data["coolingSetpoint"] ? data["coolingSetpoint"].toDouble().toInteger() : data["coolingSetpoint"]
//                    debugEventFromParent(child, "Event Data = ${data}")

					collector[dni] = [data:data]
					return collector
				}
				result = true
				log.debug "updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
			} else {
				log.error "polling children & got http status ${resp.status}"

				//refresh the auth token
				if (resp.status == 500 && resp.data.status.code == 14) {
					log.debug "Storing the failed action to try later"
					atomicState.action = "pollChildren";
					log.debug "Refreshing your auth_token!"
					refreshAuthToken()
				}
				else {
					log.error "Authentication error, invalid authentication method, lack of credentials, etc."
				}
			}
		}
	} catch(Exception e) {
		log.debug "___exception polling children: " + e
//        debugEventFromParent(child, "___exception polling children: " + e)
		refreshAuthToken()
	}
	return result
}

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild(child){

	if (pollChildren(child)){
		if (!child.device.deviceNetworkId.startsWith("ecobee_sensor")){
			if(atomicState.thermostats[child.device.deviceNetworkId] != null) {
				def tData = atomicState.thermostats[child.device.deviceNetworkId]
//                debugEventFromParent(child, "pollChild(child)>> data for ${child.device.deviceNetworkId} : ${tData.data}") //TODO comment
				log.info "pollChild(child)>> data for ${child.device.deviceNetworkId} : ${tData.data}"
				child.generateEvent(tData.data) //parse received message from parent
//            return tData.data
			} else if(atomicState.thermostats[child.device.deviceNetworkId] == null) {
//                debugEventFromParent(child, "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling") //TODO comment
				log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId}"
				return null
			}
		}
	} else {
//        debugEventFromParent(child, "ERROR: pollChildren(child) for ${child.device.deviceNetworkId} after polling") //TODO comment
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

def currentMode(child) {
	debugEvent ("atomicState.Thermos = ${atomicState.thermostats}")

	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = atomicState.thermostats[child.device.deviceNetworkId]

	debugEvent("Data = ${tData}")

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

		// TODO: flag device as in error state
		// child.errorState = true

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
						temperature = it.value as Double
						temperature = (temperature / 10).toInteger()
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
//                    debugEventFromParent(d, "temperature : ${temperature}, motion:${occupancy}")
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

                            //remove saved action
                            atomicState.action = ""
                        }

                    }
                    atomicState.action = ""
                } else {
                    log.debug "refresh failed ${resp.status} : ${resp.status.code}"
                }
            }
        } catch(Exception e) {
            log.error "refreshAuthToken() >> Error: e.statusCode ${e.statusCode}"
			def reAttemptPeriod = 300 // in sec
			if (e.statusCode != 401) { //this issue might comes from exceed 20sec app execution, connectivity issue etc.
				runIn(reAttemptPeriod, "refreshAuthToken")
			} else if (e.statusCode == 401) { //refresh token is expired
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

//    def thermostatIdsString = getChildDeviceIdsString()
//    log.debug "resumeProgram children: $thermostatIdsString"

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}]}'
	//, { "type": "sendMessage", "params": { "text": "Setpoint Updated" } }
	def result = sendJson(jsonRequestBody)
//    debugEventFromParent(child, "resumeProgram(child) with result ${result}")
	return result
}

def setHold(child, heating, cooling, deviceId, sendHoldType) {

	int h = heating * 10
	int c = cooling * 10

//    log.debug "setpoints____________ - h: $heating - $h, c: $cooling - $c"
//    def thermostatIdsString = getChildDeviceIdsString()

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": '+sendHoldType+' } } ]}'
//	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}, { "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": "indefinite" } } ]}'
	def result = sendJson(child, jsonRequestBody)
//    debugEventFromParent(child, "setHold: heating: ${h}, cooling: ${c} with result ${result}")
	return result
}

def setMode(child, mode, deviceId) {
//    def thermostatIdsString = getChildDeviceIdsString()
//    log.debug "setCoolingSetpoint children: $thermostatIdsString"

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"thermostat": {"settings":{"hvacMode":"'+"${mode}"+'"}}}'

//    log.debug "Mode Request Body = ${jsonRequestBody}"
//    debugEvent ("Mode Request Body = ${jsonRequestBody}")

	def result = sendJson(jsonRequestBody)
//    debugEventFromParent(child, "setMode to ${mode} with result ${result}")
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

//            debugEventFromParent(child, "sendJson >> resp.status ${resp.status}, resp.data: ${resp.data}")

			if(resp.status == 200) {

				log.debug "updated ${resp.data}"
				returnStatus = resp.data.status.code
				if (resp.data.status.code == 0)
					log.debug "Successful call to ecobee API."
				else {
					log.debug "Error return code = ${resp.data.status.code}"
					debugEvent("Error return code = ${resp.data.status.code}")
				}
			} else {
				log.error "sent Json & got http status ${resp.status} - ${resp.status.code}"
				debugEvent ("sent Json & got http status ${resp.status} - ${resp.status.code}")

				//refresh the auth token
				if (resp.status == 500 && resp.status.code == 14) {
					//log.debug "Storing the failed action to try later"
					log.debug "Refreshing your auth_token!"
					debugEvent ("Refreshing OAUTH Token")
					refreshAuthToken()
					return false
				} else {
					debugEvent ("Authentication error, invalid authentication method, lack of credentials, etc.")
					log.error "Authentication error, invalid authentication method, lack of credentials, etc."
					return false
				}
			}
		}
	} catch(Exception e) {
		log.debug "Exception Sending Json: " + e
		debugEvent ("Exception Sending JSON: " + e)
        refreshAuthToken()
		return false
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
