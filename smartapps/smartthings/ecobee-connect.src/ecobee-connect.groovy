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
import groovy.json.JsonSlurper
include 'localization'

definition(
		name: "Ecobee (Connect)",
		namespace: "smartthings",
		author: "SmartThings",
		description: "Connect your Ecobee thermostat to SmartThings.",
		category: "SmartThings Labs",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png",
		singleInstance: true,
		usesThirdPartyAuthentication: true,
		pausable: false
) {
	appSetting "clientId"
	appSetting "serverUrl" // See note below
	// NOTE regarding OAuth settings. On NA01 (i.e. graph.api) and NA01S the serverUrl app setting can be left
	// Blank. For other shards is should be set to the callback URL registered with Honeywell, which is:
	//
	// Production  -- https://graph.api.smartthings.com
	// Staging     -- https://graph-na01s-useast1.smartthingsgdev.com
}

preferences {
	page(name: "auth", title: "ecobee", nextPage:"", content:"authPage", uninstall: true, install:false)
	page(name: "deviceList", title: "ecobee", content:"ecobeeDeviceList", install:true)
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}

def authPage() {
	log.debug "authPage()"
	// Make sure poll/devices are not unscheduled/silenced when authPage is called when the app exits.
	// For some reason the first page is called when the app exits normally. 
	if (!state.initializeEndTime || (now() - state.initializeEndTime > 2000)) {
		// Make sure the poll is stopped to prevent state changes while user is configuring the app
		unschedule()
		// Schedule pollRestart in 15 minutes in case user exits the app abnormally. TODO: Is 15min short/long enough?
		runIn(15*60, "restartPoll")
		// TODO Make sure no child is calling any poll or command methods to prevent state changes
		//def childDevices = getChildDevices()
		//if (childDevices) {
		//	childDevices*.parentBusy(true)
		//}
	}

	if(!state.accessToken) { //this is to access token for 3rd party to make a call to connect app
		state.accessToken = createAccessToken()
	}

	def description
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(state.authToken) {
		if(!state.jwt) {
			state.jwt = true
			refreshAuthToken()
		}
		description = "You are connected."
		uninstallAllowed = true
		oauthTokenProvided = true
	} else {
		description = "Click to enter Ecobee Credentials"
	}

	def redirectUrl = buildRedirectUrl
	//log.debug "RedirectUrl = ${redirectUrl}"
	// get rid of next button until the user is actually auth'd
	if (!oauthTokenProvided) {
		return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall:uninstallAllowed) {
			section() {
				paragraph "Tap below to log in to the ecobee service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title:"ecobee", description:description
			}
		}
	} else {
		return dynamicPage(name: "auth", title: "Log In", nextPage:"deviceList", install: false, uninstall:uninstallAllowed) {
			section(){
				paragraph "Tap Next to continue to setup your ecobee thermostats."
				href url:redirectUrl, style:"embedded", state:"complete", title:"ecobee", description:description
			}
		}
	}
}

def restartPoll() {
	// This method should only be called in case the SA was terminated abnormally without
	// calling initialize which will unschedule this and start the poll as part of the normal flow
	// TODO Make sure child is calling any poll or command methods to prevent state changes
	//def childDevices = getChildDevices()
	//if (childDevices) {
	//	childDevices*.parentBusy(false)
	//}
	// Call poll
	unschedule()
	poll()
	runEvery5Minutes("poll")
}

def ecobeeDeviceList() {
	getEcobeeDevices()

	def thermostatList = thermostatsDiscovered()
	def numThermostats = thermostatList.size()
	def sensors = sensorsDiscovered()
	def numSensors = sensors.size()
	def switches = switchesDiscovered()
	def numSwitches = switches.size()

	if (!numThermostats && !numSensors && !numSwitches) {
		return dynamicPage(name: "deviceList", title: "No devices found", uninstall: true) {
			section ("") {
				paragraph "Could not find any devices avilable for SmartThings to control. Please check your ecobee account and retry later."
			}
		}
	}

	return dynamicPage(name: "deviceList", title: "Select Your ecobee Devices", uninstall: true) {
		if (numThermostats > 0)  {
			def preselectedThermostats = thermostatList.collect{it.key}
			section("") {
				paragraph "Tap below to add or remove thermostats available in your ecobee account. Selected thermostats will connect to SmartThings."
				input(name: "thermostats", title:"Select ecobee Thermostats ({{numThermostats}} found)", messageArgs: [numThermostats: numThermostats],
						type: "enum", required:false, multiple:true, 
						description: "Tap to choose", metadata:[values:thermostatList], defaultValue: preselectedThermostats)
			}
		}
		if (numSensors > 0)  {
			def preselectedSensors = sensors.collect{it.key}
			section("") {
				paragraph "Tap below to add or remove remote sensors available in your ecobee account. Selected sensors will connect to SmartThings."
				input(name: "ecobeesensors", title: "Select ecobee remote sensors ({{numSensors}} found)", messageArgs: [numSensors: numSensors],
						type: "enum", required:false, description: "Tap to choose", multiple:true, options:sensors, defaultValue: preselectedSensors)
			}
		}
		if (numSwitches > 0)  {
			def preselectedSwitches = switches.collect{it.key}
			section("") {
				paragraph "Tap below to add or remove switches available in your ecobee account. Selected switches will connect to SmartThings."
				input(name: "ecobeeswitches", title: "Select ecobee switches ({{numSwitches}} found)", messageArgs: [numSwitches: numSwitches],
						type: "enum", required:false, description: "Tap to choose", multiple:true, options:switches, defaultValue: preselectedSwitches)
			}
		}
	}
}

def oauthInitUrl() {
	log.debug "oauthInitUrl with callback: ${callbackUrl}"

	state.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			scope: "smartRead,smartWrite",
			client_id: smartThingsClientId,
			state: state.oauthInitState,
			redirect_uri: callbackUrl
	]

	redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
}

def callback() {
	log.debug "callback()>> params: $params, params.code ${params.code}"

	def code = params.code
	def oauthState = params.state

	if (oauthState == state.oauthInitState) {
		def tokenParams = [
			grant_type: "authorization_code",
			code      : code,
			client_id : smartThingsClientId,
			redirect_uri: callbackUrl
		]

		def tokenUrl = "https://www.ecobee.com/home/token?${toQueryString(tokenParams)}"

		httpPost(uri: tokenUrl) { resp ->
			state.refreshToken = resp.data.refresh_token
			state.authToken = resp.data.access_token
		}
		if ( state.authToken ) {
			// get jwt for switch+ devices
			state.jwt = true
			refreshAuthToken()
		}
		if (state.authToken) {
			success()
		} else {
			fail()
		}

	} else {
		log.error "callback() failed oauthState != state.oauthInitState"
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

def getEcobeeDevices() {
	log.debug "getting device list"
	state.remoteSensors = [] // reset depriciated application state, replaced by remoteSensors2

	def thermostatList = [:]
	def remoteSensors = [:]
	def switchList = [:]
	def isThermostatPolled = false
	def isSwitchesPolled = false
	def pollAttempt = 1
	// try obtain devices twice, in case authToken needs to be refreshed
	while (!(isThermostatPolled && isSwitchesPolled) && (pollAttempt < 3)) {
		try {
			// First get thermostats their remote sensors
			if (!isThermostatPolled) {
				def bodyParams = [
						selection: [
								selectionType:  "registered",
								selectionMatch: "",
								includeSettings:        true,
								includeRuntime:         true,
								includeSensors:         true
						]
				]
				def deviceListParams = [
						uri:     apiEndpoint,
						path:    "/1/thermostat",
						headers: ["Content-Type": "text/json", "Authorization": "Bearer ${state.authToken}"],
						// TODO - the query string below is not consistent with the Ecobee docs:
						// https://www.ecobee.com/home/developer/api/documentation/v1/operations/get-thermostats.shtml
						query: [format: 'json', body: toJson(bodyParams)]
				]
				httpGet(deviceListParams) { resp ->
					isThermostatPolled = true
					if (resp.status == 200) {
						resp.data.thermostatList.each { stat ->
							def dni = [ app.id, stat.identifier ].join('.')
							def data = getThermostatData(stat)
							thermostatList[dni] = thermostatList[dni] ? thermostatList[dni] << [data:data] : [data:data]
							thermostatList[dni].polled = true
							thermostatList[dni].pollAttempts = 0
							// compile all remote sensors conected to the thermostat
							stat.remoteSensors.each { sensor ->
								if (sensor.type != "thermostat") {
									def rsDni = "ecobee_sensor-"+ sensor?.id + "-" + sensor?.code
									remoteSensors[rsDni] = sensor
									remoteSensors[rsDni] << [thermostatId: dni]
								}
							}
						}
						state.remoteSensors2 = remoteSensors
						state.thermostats = thermostatList
					} else {
						log.debug "Failed to get thermostats and sensors, status:${resp.status}"
					}
				}
			}
			// Now get light swiches
			if (!isSwitchesPolled) {
				def switchListParams = [
						uri: apiEndpoint + "/ea/devices",
						headers: ["Content-Type": "application/json;charset=UTF-8", "Authorization": "Bearer ${state.authToken}"],
				]
				httpGet(switchListParams) { resp ->
					isSwitchesPolled = true
					if (resp.status == 200) {
						resp.data?.devices?.each {
							if (it.type == "LIGHT_SWITCH") {
								switchList[it?.identifier] = it
								switchList[it?.identifier] << [deviceAlive: (it?.connected ?: false)]
							}
						}
						state.switchList = switchList
					} else {
						log.warn "Unable to get switch device list, status:${resp.status}"
					}
				}
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "Exception getEcobeeDevices: ${e?.getStatusCode()}, e:${e}, data:${e.response?.data}"
			if (e.response?.data?.status?.code == 14) {
				pollAttempt++
				if (pollAttempt > 2 || !refreshAuthToken()) {
					pollAttempt = 3
					log.error "Ecobee failed getting devices despite refreshing authToken"
				}
			}
		} catch (Exception e) {
			log.error "Unhandled exception $e in getEcobeeDevices tried:${pollAttempt} times"
			// break the loop and exit
			pollAttempt = 3
		}
	}
}

Map thermostatsDiscovered() {
	def map = [:]
	def thermostatList = state.thermostats ?: [:]
	thermostatList.each { key, stat ->
		map[key] = stat.data.name
	}
	return map
}

Map sensorsDiscovered() {
	def map = [:]
	def remoteSensors = state.remoteSensors2 ?: [:]
	remoteSensors.each { key, sensors ->
		map[key] = sensors.name
	}
	return map
}

def switchesDiscovered() {
	def map = [:]
	def switches = state.switchList ?: [:]
	switches.each { key, ecobeeSwitch ->
		map[key] = ecobeeSwitch.name  
	}
	return map
}

def getThermostatDisplayName(stat) {
    if(stat?.name) {
        return stat.name.toString()
    }
    return (getThermostatTypeName(stat) + " (${stat.identifier})").toString()
}

def getThermostatTypeName(stat) {
	return stat.modelNumber == "siSmart" ? "Smart Si" : "Smart"
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	// initialize will be called by the updated method
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	def thermostatList = state.thermostats ?: [:]
	def remoteSensors = state.remoteSensors2 ?: [:]
	def switchList = state.switchList ?: [:]
	def childThermostats = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getChildName(), dni, null, ["label":"${thermostatList[dni].data.name}" ?: getChildName()])
			log.debug "created ${d.displayName} with id $dni"
			// initialize DTH with default data will be done using the first poll data
			// TODO: Move this to DTH install method
			d.generateEvent(thermostatList[dni].data)
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}
	def childSensors = ecobeesensors.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getSensorChildName(), dni, null, ["label":remoteSensors[dni].name ?: getSensorChildName()])
			log.debug "created ${d.displayName} with id $dni"
			// initialize DTH with default data - TODO: Move this to DTH install method
			d.sendEvent(name:"temperature", value: 0, unit: location.temperatureScale,
					descriptionText: "temperature is unknown", displayed: true)
			d.sendEvent(name:"motion", value: "inactive")
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}
	def childSwitches = ecobeeswitches.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getSwitchChildName(), dni, null, ["label":"${switchList[dni].name}" ?: getSwitchChildName()])
			log.debug "created ${d.displayName} with id $dni"
			// initialize DTH with default data - TODO: Move this to DTH install method
			d.sendEvent(name:"switch", value: "off")
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}

	log.debug "Now have ${childThermostats.size()} thermostats, ${childSensors.size()} sensors and ${childSwitches.size()} switches"

	def delete  // Delete any that are no longer in settings
	if(!thermostats && !ecobeesensors && !ecobeeswitches) {
		log.debug "delete thermostats ands sensors"
		delete = getAllChildDevices() //inherits from SmartApp (data-management)
	} else { //delete only thermostat
		log.debug "delete individual thermostat and sensor"
		delete = getChildDevices().findAll {
				!thermostats?.contains(it.deviceNetworkId) &&
				!ecobeesensors?.contains(it.deviceNetworkId) &&
				!ecobeeswitches?.contains(it.deviceNetworkId)
		}
	}
	log.warn "force deleting devices ${delete}"
	delete.each { deleteChildDevice(it.deviceNetworkId, true) } //inherits from SmartApp (data-management)

	// TODO Schedule purge of uninstalled device data as it takes some time before the child is gone
	runIn(10, "purgeUninstalledDeviceData", [overwrite: true])

	//send activity feeds to tell that device is connected
	def notificationMessage = "is connected to SmartThings"
	sendActivityFeeds(notificationMessage)
	state.timeSendPush = null
	state.reAttempt = 0

//	pollHandler() //first time polling data data from thermostat
	// clear depreciated data
	state.remoteSensors = []
	state.sensors = []
	//automatically update devices status every 5 mins
	runEvery5Minutes("poll")
	poll()
	state.initializeEndTime = now()
}

def purgeUninstalledDeviceData() {
	// purge state from devices that are not selected
	def thermostatList = state.thermostats ?: [:]
	def remoteSensors = state.remoteSensors2 ?: [:]
	def switchList = state.switchList ?: [:]

	// clean up device lists
	thermostatList.keySet().removeAll(thermostatList.keySet() - thermostats)
	remoteSensors.keySet().removeAll(remoteSensors.keySet() - ecobeesensors)
	switchList.keySet().removeAll(switchList.keySet() - ecobeeswitches)
	state.thermostats = thermostatList
	state.remoteSensors2 = remoteSensors
	state.switchList = switchList
}

def purgeChildDevice(childDevice) {
	def dni = childDevice.device.deviceNetworkId
	def thermostatList = state.thermostats ?: [:]
	def remoteSensors = state.remoteSensors2 ?: [:]
	def switchList = state.switchList ?: [:]
	if (thermostatList[dni]) {
		thermostatList.remove(dni)
		state.thermostats = thermostatList
		if (thermostats) {
			thermostats.remove(dni)
		}
		app.updateSetting("thermostats", thermostats ? thermostats : [])
	} else if (remoteSensors[dni]){
		remoteSensors.remove(dni)
		state.remoteSensors2 = remoteSensors
		if (ecobeesensors) {
			ecobeesensors.remove(dni)
		}
		app.updateSetting("ecobeesensors", ecobeesensors ? ecobeesensors : [])
	} else if(switchList[dni]) {
		switchList.remove(dni)
		state.switchList = switchList
		if (ecobeeswitches) {
			ecobeeswitches.remove(dni)
		}
		app.updateSetting("ecobeeswitches", ecobeeswitches ? ecobeeswitches : [])
	} else {
		log.error "Failed to purge data for childDevice dni:$dni"
	}
	if (getChildDevices().size <= 1) {
		log.info "No more thermostats to poll, unscheduling"
		unschedule()
		state.authToken = null
		runIn(1, "terminateMe")
	}
}

def terminateMe() {
	log.info "terminateMe"
	try {
		app.delete()
	} catch (Exception e) {
		log.error "Termination failed, I’m invincible!"
	}
}

def poll() {
	// No need to keep trying to poll if authToken is null
	if (!state.authToken) {
		log.info "poll failed due to authToken=null"
		def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
		sendPushAndFeeds(notificationMessage)
		markChildrenOffline(true)
		unschedule()
		unsubscribe()
		return
	}
	def isThermostatPolled = !(thermostats || ecobeesensors) // If no thermostats or sensors, mark them polled
	def isSwitchesPolled = !(ecobeeswitches)                 // If no switches, mark them polled
	def pollAttempt = 1

	// Mark all devices as offline for device health
	def remoteSensors = state.remoteSensors2 ?: [:]
	def thermostatList = state.thermostats ?: [:]
	def switchList = state.switchList ?: [:]
	remoteSensors.each { rdni, sensor ->
		sensor.deviceAlive = false
		sensor.polled = false
	}
	thermostatList.each { dni, stat ->
		stat.polled = false
		stat.data = stat.data ? stat.data << [deviceAlive:false] : [deviceAlive:false]
	}
	switchList.each { sdni, sw ->
		sw.deviceAlive = false
		sw.polled = false
	}
	state.remoteSensors2 = remoteSensors
	state.thermostats = thermostatList
	state.switchList = switchList

	while (!(isThermostatPolled && isSwitchesPolled) && (pollAttempt < 3)) {
		try{
			// First check if we need to poll thermostats or sensors
			if (!isThermostatPolled) {
				def requestBody = [
					selection: [
						selectionType:          "registered",
						selectionMatch:         "",
						includeExtendedRuntime: true,
						includeSettings:        true,
						includeRuntime:         true,
						includeSensors:         true
					]
				]
				def pollParams = [
					uri: apiEndpoint,
					path: "/1/thermostat",
					headers: ["Content-Type": "text/json", "Authorization": "Bearer ${state.authToken}"],
					// TODO - the query string below is not consistent with the Ecobee docs:
					// https://www.ecobee.com/home/developer/api/documentation/v1/operations/get-thermostats.shtml
					query: [format: 'json', body: toJson(requestBody)]
				]

				httpGet(pollParams) { resp ->
					isThermostatPolled = true
					if(resp.status == 200) {
						storeThermostatData(resp.data.thermostatList)
						if (ecobeesensors) {
							updateSensorData(resp.data.thermostatList.remoteSensors)
						}
					}
				}
			}
			// Check if we have switches that needs to be polled
			if (!isSwitchesPolled)  {
				def switchListParams = [
					uri: apiEndpoint + "/ea/devices",
					headers: ["Content-Type": "application/json;charset=UTF-8", "Authorization": "Bearer ${state.authToken}"],
				]

				httpGet(switchListParams) { resp ->
					isSwitchesPolled = true
					if (resp.status == 200) {
						updateSwitches(resp.data?.devices)
					} else {
						log.warn "Unable to get switch device list!"
					}
				}
			}

		} catch (groovyx.net.http.HttpResponseException e) {
			log.info "HttpResponseException ${e}, ${e?.getStatusCode()} polling ecobee pollAttempt:${pollAttempt}, " +
					"isThermostatPolled:${isThermostatPolled}, isSwitchesPolled:${isSwitchesPolled}, ${e?.response?.data}"
			if (e?.getStatusCode() == 401 || e?.response?.data?.status?.code == 14)  {
				pollAttempt++
				// Try refresh authToken and try poll one more time
				if (pollAttempt > 2 || !refreshAuthToken()) {
					// refresh of authToken failed, break the loop and exit
					pollAttempt = 3
					log.error "Ecobee poll failed despite refreshing authToken"
				}
			} else {
				log.error "Ecobee poll failed for other reason than expired authToken"
				// break the loop and exit
				pollAttempt = 3
			}
		} catch (Exception e) {
			log.error "Unhandled exception $e in ecobee polling pollAttempt:${pollAttempt}, " +
					"isThermostatPolled:${isThermostatPolled}, isSwitchesPolled:${isSwitchesPolled}"
			// break the loop and exit
			pollAttempt = 3
		}
	}
	markChildrenOffline()
	log.trace "poll exit pollAttempt:${pollAttempt}, isThermostatPolled:${isThermostatPolled}, " +
			"isSwitchesPolled:${isSwitchesPolled}"
}

def markChildrenOffline(boolean markAllOffline = false) {
	if (markAllOffline) {
		def childDevices = getChildDevices()
		childDevices.each{ child ->
			child.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
			child.sendEvent("name":"thermostat", "value":"Offline")
		}
	} else {
		def remoteSensors = state.remoteSensors2 ?: [:]
		def thermostatList = state.thermostats ?: [:]
		def switchList = state.switchList ?: [:] 
		// For devices offline that wasn't polled update pollAttemps, if this is 3rd pollAttempts, mark device offline
		def thermostatsOffline = thermostatList.findAll { dni, stat ->
			if ((stat.data.deviceAlive == false) && (stat.polled == false)) {
				stat.pollAttempts = stat.pollAttempts ? stat.pollAttempts + 1 : 1
				if (stat.pollAttempts > 2) {
					return dni
				}
			}
		}?.keySet()
		def remoteSensorsOffline = remoteSensors.findAll { rsdni, sensor ->
			if ((sensor.deviceAlive == false) && (sensor.polled == false)) {
				sensor.pollAttempts = sensor.pollAttempts ? sensor.pollAttempts + 1 : 1
				if (sensor.pollAttempts > 2) {
					return rsdni
				}
			}
		}?.keySet()
		def switchesOffline = switchList.findAll { sdni, sw ->
			if ((sw.deviceAlive == false) && (sw.polled == false)) {
				sw.pollAttempts = sw.pollAttempts ? sw.pollAttempts + 1 : 1
				if (sw.pollAttempts > 2) {
					return sdni
				}
			}
		}?.keySet()
		def devicesOffline = thermostatsOffline + remoteSensorsOffline + switchesOffline
		devicesOffline.each { dni ->
			def child = getChildDevice(dni)
			if (child) {
				child.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
			}
		}
		state.remoteSensors2 = remoteSensors
		state.thermostats = thermostatList
		state.switchList = switchList
	}
}

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild() {
	log.warn "Depreciated method pollChild is called"
}

void controlSwitch( dni, desiredState ) {
	// no need to try sending a command if authToken is null
	if (!state.authToken) {
		log.warn "controlSwitch failed due to authToken=null"
		return
	}

	def deviceAlive = state.switchList[dni].deviceAlive
	// Only send command to online switches
	if (deviceAlive == true) {
		def d = getChildDevice(dni)
		log.trace "[SM] Executing '${(desiredState ? "on" : "off")}' controlSwitch for ${d.device.displayName}"
		def body = [ "on": desiredState ]
		def params = [
			uri: apiEndpoint + "/ea/devices/ls/$dni/state",
			headers: ["Content-Type": "application/json;charset=UTF-8", "Authorization": "Bearer ${state.authToken}"],
			body: toJson(body)
		]
		def keepTrying = true
		def tokenRefreshTries = 0

		while (keepTrying) {
			try {
				httpPut(params) { resp ->
					keepTrying = false
					def rspDataString = "${resp?.data}".toString()
					log.info "RESPONSE CODE: ${resp.status}, data:${rspDataString}"
				}
			} catch (groovyx.net.http.HttpResponseException e) {
				//log.warn "Code=${e.getStatusCode()}"
				if (e.getStatusCode() == 401) {
					tokenRefreshTries++
					if (tokenRefreshTries > 1 || !refreshAuthToken()) {
						// refresh of authToken failed, break the loop and exit
						log.info "Error refreshing auth_token! Unable to control switch: ${d.device.displayName}"
						keepTrying = false
					} else {
						params.headers.Authorization = "Bearer ${state.authToken}"
					}
				} else if (e.getStatusCode() == 200) {
					// Due to ecobee API returning empty boddy on success we get HttpResponseException from platfrom
					// so handle sucess response here
					keepTrying = false
					log.debug "Ecobee response to switch control = 'Success' for ${d.device.displayName}"
					def switchState = desiredState == true ? "on" : "off"
					d.sendEvent(name:"switch", value: switchState)
				} else {
					keepTrying = false
					log.error "Exception from device control status:${e.getStatusCode()}, getMessage:${e.getMessage()}"
				}
			} catch (Exception e) {
				def rspDataString = "${e.response?.data}".toString()
				log.error "Unhandled exception ${e.getStatusCode()}, $e, response data:$rspDataString"
				keepTrying = false
			}
		}
	} else {
		log.debug "Can't send command to offline swich!"
	}
}

def availableModes(child) {
	def tData = state.thermostats[child.device.deviceNetworkId]

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
		return null
	}

	def modes = ["off"]

    if (tData.data.heatMode) {
        modes.add("heat")
    }
    if (tData.data.coolMode) {
        modes.add("cool")
    }
    if (tData.data.autoMode) {
        modes.add("auto")
    }
    if (tData.data.auxHeatMode) {
        modes.add("auxHeatOnly")
    }

    return modes
}

def currentMode(child) {

	def tData = state.thermostats[child.device.deviceNetworkId]

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
		return null
	}

	def mode = tData.data.thermostatMode
	return mode
}

def updateSwitches(switches) {
	if (switches) {
		def switchList = state.switchList ?: [:]
		switches.each {
			if ( it.type == "LIGHT_SWITCH" ) {
				def childSwitch = getChildDevice(it?.identifier)
				if (childSwitch) {
					switchList[it?.identifier] = it
					switchList[it?.identifier] << [deviceAlive: (it?.connected ?: false)]
					switchList[it?.identifier].polled = true
					switchList[it?.identifier].pollAttempts = 0
					if (it?.connected) {
						def switchState = it?.state?.on == true ? "on" : "off"
						childSwitch.sendEvent(name:"switch", value: switchState)
						childSwitch.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
					} else {
						childSwitch.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
					}
				} else {
					log.info "[SM] pollSwitches received data for non-smarthings switch, ingoring"
				}
			}
		}
		state.switchList = switchList
	}
}

def updateSensorData(sensorData) {
	def remoteSensors = state.remoteSensors2 ?: [:]
	sensorData.each {
		it.each {
			if (it.type != "thermostat") {
				def temperature = ""
				def occupancy = ""
				def dni = "ecobee_sensor-"+ it?.id + "-" + it?.code
				def child = getChildDevice(dni)
				if(child) {
					// If DeviceWatch hasn't be enrolled as untracked scheme, re-enroll
					if (!child.getDataValue("EnrolledUTDH")) {
						child.updated()
					} else if (it?.name && (it.name != child.displayName)) {
						// Only allowing name change after DeviceWatch has been enrolled, this to ensure the ST name
						// is preserved and not changed to name from ecobee cloud as this is the first name change is allowed
						child.setDisplayName(it.name)
					}
					if (!remoteSensors[dni] || remoteSensors[dni].deviceAlive) {
						it.capability.each {
							if (it.type == "temperature") {
								if (it.value == "unknown") {
									// setting to 0 as "--" is not a valid number depite 0 being a valid value
									temperature = 0
								} else {
									if (location.temperatureScale == "F") {
										temperature = Math.round(it.value.toDouble() / 10)
									} else {
										temperature = convertFtoC(it.value.toDouble() / 10)
									}
								}
							} else if (it.type == "occupancy") {
									occupancy = (it.value == "true") ? "active" : "inactive"
							}
						}
						remoteSensors[dni] << it
						child.sendEvent(name:"temperature", value: temperature, unit: location.temperatureScale,
								descriptionText: "temperature is " + (temperature ? "${temperature}°${location.temperatureScale}" : "unknown"), displayed: true)
						child.sendEvent(name:"motion", value: occupancy)
						child.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
					} else {
						remoteSensors[dni] << it
						child.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
					}
				}
			}
		}
	}
	state.remoteSensors2 = remoteSensors
}

def getChildDeviceIdsString() {
	return thermostats.collect { it.split(/\./).last() }.join(',')
}

def toJson(Map m) {
    return groovy.json.JsonOutput.toJson(m)
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

boolean refreshAuthToken() {
	log.debug "refreshing auth token"
	def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
	def isSuccess = false

	if(!state.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
		sendPushAndFeeds(notificationMessage)
	} else {
		def refreshParams = [
			method: 'POST',
			uri   : apiEndpoint,
			path  : "/token"
		]
		if (state.jwt) {
			refreshParams.query = [
				grant_type:    "refresh_token",
				refresh_token: state.refreshToken,
				client_id :    smartThingsClientId,
				ecobee_type:   "jwt"
			]
		} else {
			refreshParams.query = [
				grant_type: "refresh_token",
				code:       state.refreshToken,
				client_id:  smartThingsClientId
			]
		}
		try {
			httpPost(refreshParams) { resp ->
				if(resp.status == 200) {
					log.debug "Token refreshed, ${resp.data}"
					state.refreshToken = resp.data?.refresh_token
					state.authToken = resp.data?.access_token
					state.reAttempt = 0
					isSuccess = true
				}
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			def rspDataString = "${e.response?.data}".toString()
			log.error "Error refreshing auth_token:${e.statusCode}, refreshAttempt:${state.reAttempt}, response data:$rspDataString"
			if ((e.statusCode == 400) || (e.statusCode == 302)) {
				def slurper = new JsonSlurper()
				def rspData = slurper.parseText(rspDataString)
				if (rspData && (rspData.error != "invalid_request" || rspData.error != "not_supported")) {
					// either "invalid_grant", "unauthorized_client", "unsupported_grant_type",
					// or "invalid_scope", request user to re-enter credentials
					sendPushAndFeeds(notificationMessage)
				}
			} else if (e.statusCode == 401) { // unauthorized*/
				state.reAttempt = state.reAttempt ? state.reAttempt + 1 : 1
				log.warn "reAttempt refreshAuthToken ${state.reAttempt}"
				if (state.reAttempt > 3) {
					sendPushAndFeeds(notificationMessage)
					state.reAttempt = 0
				}
			}
		}
	}
	return isSuccess
}

/**
 * Executes the resume program command on the Ecobee thermostat
 * @param deviceId - the ID of the device
 *
 * @retrun true if the command was successful, false otherwise.
 */
boolean resumeProgram(deviceId) {
    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        functions: [
            [
                type: "resumeProgram"
            ]
        ]
    ]
    return sendCommandToEcobee(payload)
}

/**
 * Executes the set hold command on the Ecobee thermostat
 * @param heating - The heating temperature to set in fahrenheit
 * @param cooling - the cooling temperature to set in fahrenheit
 * @param deviceId - the ID of the device
 * @param sendHoldType - the hold type to execute
 *
 * @return true if the command was successful, false otherwise
 */
boolean setHold(heating, cooling, deviceId, sendHoldType) {
    // Ecobee requires that temp values be in fahrenheit multiplied by 10.
    int h = heating * 10
    int c = cooling * 10

    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        functions: [
            [
                type: "setHold",
                params: [
                    coolHoldTemp: c,
                    heatHoldTemp: h,
                    holdType: sendHoldType
                ]
            ]
        ]
    ]

    return sendCommandToEcobee(payload)
}

/**
 * Executes the set fan mode command on the Ecobee thermostat
 * @param heating - The heating temperature to set in fahrenheit
 * @param cooling - the cooling temperature to set in fahrenheit
 * @param deviceId - the ID of the device
 * @param sendHoldType - the hold type to execute
 * @param fanMode - the fan mode to set to
 *
 * @return true if the command was successful, false otherwise
 */
boolean setFanMode(heating, cooling, deviceId, sendHoldType, fanMode) {
    // Ecobee requires that temp values be in fahrenheit multiplied by 10.
    int h = heating * 10
    int c = cooling * 10

    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        functions: [
            [
                type: "setHold",
                params: [
                    coolHoldTemp: c,
                    heatHoldTemp: h,
                    holdType: sendHoldType,
                    fan: fanMode
                ]
            ]
        ]
    ]

	return sendCommandToEcobee(payload)
}

/**
 * Sets the mode of the Ecobee thermostat
 * @param mode - the mode to set to
 * @param deviceId - the ID of the device
 *
 * @return true if the command was successful, false otherwise
 */
boolean setMode(mode, deviceId) {
    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        thermostat: [
            settings: [
                hvacMode: mode
            ]
        ]
    ]
	return sendCommandToEcobee(payload)
}

/**
 * Sets the name of the Ecobee thermostat
 * @param name - the name to set to
 * @param deviceId - the ID of the device
 *
 * @return true if the command was successful, false otherwise
 */
def setName(name, deviceId) {
	def thermostatList = state.thermostats ?: [:]
	if (thermostatList[deviceId]?.data?.name != name) {
			def payload = [
			selection: [
				selectionType: "thermostats",
				selectionMatch: deviceId.split(/\./).last(),
				includeRuntime: true
			],
			thermostat: [
				name: name
			]
		]
		log.debug "setName: payload:$payload"
		sendCommandToEcobee(payload)
	}
}

/**
 * Sets the name of the Ecobee3 remote sensor
 * @param name - the name to set to
 * @param deviceId - the ID of the device
 *
 * @return true if the command was successful, false otherwise
 */
def setSensorName(name, deviceId) {
	def remoteSensors = state.remoteSensors2 ?: [:]
	if (remoteSensors[deviceId] && (remoteSensors[deviceId]?.name != name)) {
		def payload = [
			selection: [
				selectionType: "thermostats",
				selectionMatch: remoteSensors[deviceId].thermostatId?.split(/\./).last(),
				includeRuntime: true
			],
			functions: [
				[
					"type": "updateSensor",
					"params": [
						"deviceId": remoteSensors[deviceId].id,
						"sensorId": remoteSensors[deviceId].capability?.first()?.id,
						"name":     name
					]
				]
			]
		]
		log.debug "setSensorName: payload:$payload"
		sendCommandToEcobee(payload)
	}
}

/**
 * Makes a request to the Ecobee API to actuate the thermostat.
 * Used by command methods to send commands to Ecobee.
 *
 * @param bodyParams - a map of request parameters to send to Ecobee.
 *
 * @return true if the command was accepted by Ecobee without error, false otherwise.
 */
private boolean sendCommandToEcobee(Map bodyParams) {
	// no need to try sending a command if authToken is null
	if (!state.authToken) {
		log.warn "sendCommandToEcobee failed due to authToken=null"
		return false
	}
	def isSuccess = false
	def cmdParams = [
		uri: apiEndpoint,
		path: "/1/thermostat",
		headers: ["Content-Type": "application/json", "Authorization": "Bearer ${state.authToken}"],
		body: toJson(bodyParams)
	]
	def keepTrying = true
	def cmdAttempt = 1

	while (keepTrying) {
		try{
			httpPost(cmdParams) { resp ->
				keepTrying = false
				if(resp.status == 200) {
					log.debug "updated ${resp.data}"
					def returnStatus = resp.data.status.code
					if (returnStatus == 0) {
						log.debug "Successful call to ecobee API."
						isSuccess = true
					} else {
						log.debug "Error return code = ${returnStatus}"
					}
				}
			}
		} catch (groovyx.net.http.HttpResponseException e) {
			log.info "Exception sending command: $e, status:${e.getStatusCode()}, ${e?.response?.data}"
			if (e.response.data.status.code == 14) {
				cmdAttempt++
				if (cmdAttempt > 2 || !refreshAuthToken()) {
					// refresh authToken failed, break loop and exit
					log.error "Error refreshing auth_token! Unable to send command"
					keepTrying = false
				} else {
					cmdParams.headers.Authorization = "Bearer ${state.authToken}"
				}
			} else {
				log.error "Exception sending command: Authentication error, invalid authentication method, lack of credentials, etc."
				keepTrying = false
			}
		}
	}
	return isSuccess
}

def getChildName()           { return "Ecobee Thermostat" }
def getSensorChildName()     { return "Ecobee Sensor" }
def getSwitchChildName()     { return "Ecobee Switch" }
def getServerUrl()           { return appSettings.serverUrl ?: apiServerUrl }
def getCallbackUrl()         { return "${serverUrl}/oauth/callback" }
def getBuildRedirectUrl()    { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${apiServerUrl}" }
def getApiEndpoint()         { return "https://api.ecobee.com" }
def getSmartThingsClientId() { return appSettings.clientId }
private getVendorIcon() 	 { return "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png" }

//send both push notification and mobile activity feeds
def sendPushAndFeeds(notificationMessage) {
	def timeNow = now()
	log.warn "sendPushAndFeeds >> notificationMessage: ${notificationMessage}"
	log.warn "sendPushAndFeeds >> state.timeSendPush: ${state.timeSendPush}"
	// notification is sent to remind user once a day
	if (!state.timeSendPush || (24 * 60 * 60 * 1000 < (timeNow - state.timeSendPush))) {
		sendPush("Your Ecobee thermostat " + notificationMessage)
		sendActivityFeeds(notificationMessage)
		state.timeSendPush = now()
	}
	state.authToken = null
}

def getThermostatData(data) {

	return [
			name: getThermostatDisplayName(data),//stat.name ? stat.name : stat.identifier),
			coolMode: (data.settings.coolStages > 0),
			heatMode: (data.settings.heatStages > 0),
			deviceTemperatureUnit: (data.settings.useCelsius == false && location.temperatureScale == "F") ? "F" : "C",
			minHeatingSetpoint: (data.settings.heatRangeLow / 10),
			maxHeatingSetpoint: (data.settings.heatRangeHigh / 10),
			minCoolingSetpoint: (data.settings.coolRangeLow / 10),
			maxCoolingSetpoint: (data.settings.coolRangeHigh / 10),
			autoMode: data.settings.autoHeatCoolFeatureEnabled,
			deviceAlive: data.runtime.connected,
			auxHeatMode: (data.settings.hasHeatPump) && (data.settings.hasForcedAir || data.settings.hasElectric || data.settings.hasBoiler),
			temperature: (data.runtime.actualTemperature / 10),
			heatingSetpoint: (data.runtime.desiredHeat / 10),
			coolingSetpoint: (data.runtime.desiredCool / 10),
			thermostatMode: data.settings.hvacMode,
			humidity: data.runtime.actualHumidity,
			thermostatFanMode: data.runtime.desiredFanMode
	]
}

/**
 * Stores data about the thermostats in atomicState.
 * @param thermostats - a list of thermostats as returned from the Ecobee API
 */
private void storeThermostatData(thermostatData) {
	def data
	def remoteSensors = state.remoteSensors2 ?: [:]
	def thermostatList = state.thermostats ?: [:]
	thermostatData.each { stat ->
		def dni = [ app.id, stat.identifier ].join('.')
		def childDevice = getChildDevice(dni)
		data = getThermostatData(stat)
		if (childDevice) {
			// Adjust autoMode in regards to coolMode and heatMode as thermostat may report autoMode:true despite only having heat or cool mode
			data["autoMode"] = data["autoMode"] && data.coolMode && data.heatMode
			if (!childDevice.getDataValue("EnrolledUTDH")) {
				childDevice.updated()
			}
			if (childDevice.displayName != data.name) {
				childDevice.setDisplayName(data.name)
			}
			if (data["deviceAlive"]) {
				childDevice.generateEvent(data)
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false)
			} else {
				childDevice.sendEvent("name":"thermostat", "value":"Offline")
				childDevice.sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false)
			}
			thermostatList[dni] = thermostatList[dni] ? thermostatList[dni] << [data:data] : [data:data]
			thermostatList[dni].polled = true
			thermostatList[dni].pollAttempts = 0
		} else {
			log.info "Got poll data for ${data.name} with identifier ${stat.identifier} that doesn't have a DTH"
		}
		// Make sure any remote senors connected to the thermostat are marked offline too
		stat.remoteSensors.each { sensor ->
			if (sensor.type != "thermostat") {
				def rsDni = "ecobee_sensor-"+ sensor?.id + "-" + sensor?.code
				if (ecobeesensors?.contains(rsDni)) {
					remoteSensors[rsDni] = remoteSensors[rsDni] ?
							remoteSensors[rsDni] << [deviceAlive:data["deviceAlive"]] : [deviceAlive:data["deviceAlive"]]
					remoteSensors[rsDni] << [thermostatId: dni]
					remoteSensors[rsDni].polled = true
					remoteSensors[rsDni].pollAttempts = 0
				}
			}
		}
	}
	state.thermostats = thermostatList
	state.remoteSensors2 = remoteSensors
}

def sendActivityFeeds(notificationMessage) {
	def devices = getChildDevices()
	devices.each { child ->
		child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
	}
}

def convertFtoC (tempF) {
	return String.format("%.1f", (Math.round(((tempF - 32)*(5/9)) * 2))/2)
}
