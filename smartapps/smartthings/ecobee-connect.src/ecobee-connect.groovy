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
    	atomicState.ecobeeDeviceList = [:]
		def stats = getEcobeeThermostats()
        def sensors = sensorsDiscovered() ?: []
        def switches = switchesDiscovered() ?: []
        def allDevices = (stats?:[]) + (sensors?:[]) + (switches?:[])
        allDevices = allDevices.sort { it.value.toLowerCase() }
    	def preselected = allDevices.collect{it.key}        
        
        def pageTitle
        def inputTitle = "Use these Ecobee Devices:"
        def p1

        if ( !selectedDevices ) {
        	pageTitle = "Connect Your Ecobee Devices"
            p1 = "If you wish to not add any of the devices listed below, tap on the list and then unselect that device. Once acceptable, tap 'Done'."
        } else {
        	pageTitle = "Connected Ecobee Devices"
            p1 = "If you wish to add or remove device(s), tap the list below and then choose which device(s). Once acceptable, tap 'Done'."
        }
              
		return dynamicPage(name: "auth", title: "Select Your Ecobee Devices", install: true, uninstall: true) {                 
       		section("") {
            	paragraph p1, image: vendorIcon
            }
            section("Devices") {
            	input(
                	name: "selectedDevices",
                	title: "Connect these Ecobee Devices:",
                	type: "enum",
                	required: false,
                	multiple: true,
                	description: "Tap to choose",
                	options: allDevices,
					defaultValue: preselected
            	)
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

	if (oauthState == atomicState.oauthInitState) {
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
		}
        
        if ( atomicState.authToken ) {
        	obtainJsonToken() //get jwt for switch+ devices
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

boolean obtainJsonToken() {
	boolean status = false
	def tokenParams = [
		grant_type: 	"refresh_token",
		refresh_token:	atomicState.refreshToken,
		client_id : 	smartThingsClientId,
		ecobee_type:	"jwt"
	]
    def tokenUrl = "https://api.ecobee.com/token?${toQueryString(tokenParams)}"
    try {
    	httpPost(uri: tokenUrl) { resp ->
			log.debug "jwt response: ${resp.data}"
			atomicState.refreshToken = resp.data.refresh_token
			atomicState.authToken = resp.data.access_token
            status = true
		}
    } catch (e) {
    	log.error "Unable to obtain Json Token. Response Code: ${e.response.data.status.code}"
    }
    status
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

def getEcobeeThermostats() {
	log.debug "getting device list"
	atomicState.remoteSensors = []

    def bodyParams = [
        selection: [
            selectionType: "registered",
            selectionMatch: "",
            includeRuntime: true,
            includeSensors: true
        ]
    ]
	def deviceListParams = [
		uri: apiEndpoint,
		path: "/1/thermostat",
		headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
        // TODO - the query string below is not consistent with the Ecobee docs:
        // https://www.ecobee.com/home/developer/api/documentation/v1/operations/get-thermostats.shtml
		query: [format: 'json', body: toJson(bodyParams)]
	]

	def stats = [:]
	try {
		httpGet(deviceListParams) { resp ->
			if (resp.status == 200) {
				resp.data.thermostatList.each { stat ->
                	//log.warn "stat: $stat"
					atomicState.remoteSensors = atomicState.remoteSensors == null ? stat.remoteSensors : atomicState.remoteSensors <<  stat.remoteSensors
					def dni = [app.id, stat.identifier].join('.')
					stats[dni] = getThermostatDisplayName(stat)
                    atomicState.ecobeeDeviceList[dni] = [ "deviceType": "thermostat", "name": getThermostatDisplayName(stat) ]                    
                    //log.warn "remote sensors: ${stat.remoteSensors}"
				}
			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
        log.trace "[SM] getEcobeeThermostats() Exception polling children: " + e.response.data.status
        if (e.response.data.status.code == 14) {
            atomicState.action = "getEcobeeThermostats"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
    }
	atomicState.thermostats = stats
	return stats
}

Map switchesDiscovered() {
	def map = [:]
	def switchListParams = [
		uri: apiEndpoint + "/ea/devices",
		headers: ["Content-Type": "application/json;charset=UTF-8", "Authorization": "Bearer ${atomicState.authToken}"],
	]
    def switches = [:]

	try {
		httpGet(switchListParams) { resp ->
            log.debug "http status: ${resp.status}"
			if (resp.status == 200) {
            	switches = resp.data.devices            
            	//log.debug "get device list response data:  ${resp.data.devices}"
			} else {
				log.warn "Unable to get switch device list!"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
        log.trace "Exception getting switches from account: " + e.getCause()
        if (e.response.data.status.code == 14) {
            atomicState.action = "switchesDiscovered"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
    }
	
    if ( switches ) {
    	atomicState.switchList= [:]
    	switches.each {
        	if ( it.type == "LIGHT_SWITCH" ) {
            	atomicState.switchList[it?.identifier] = it
				def value = "${it?.name}"
				def key = it?.identifier
				map["${key}"] = value  
                atomicState.ecobeeDeviceList["${key}"] = [ "deviceType": "switch", "name": value ]
            }
        }
    }
    //log.debug "atomicState.switchList: ${atomicState.switchList}"
	atomicState.switches = map
	return map
}

Map sensorsDiscovered() {
	def map = [:]
	//log.debug "list ${atomicState.remoteSensors}"
	atomicState.remoteSensors.each { sensors ->
		sensors.each {
			if (it.type != "thermostat") {
				def value = "${it?.name}"
				def key = "ecobee_sensor-"+ it?.id + "-" + it?.code
				map["${key}"] = value
                atomicState.ecobeeDeviceList["${key}"] = [ "deviceType": "sensor", "name": value ]
			}
		}
	}
	atomicState.sensors = map
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
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

private removeChildDevice(childDevice) {
	log.trace "[SM] Executing removeChildDevice()"
    def newDeviceList = settings['selectedDevices'] - childDevice.device.deviceNetworkId
    app.updateSetting("selectedDevices", newDeviceList)
}

private getDeviceFiles() {
	def files = [ 
    	"thermostat": "Ecobee Thermostat",
        "sensor": "Ecobee Sensor",
        "switch": "Ecobee Switch"
    ] 
}

def getDeviceFileName(deviceType) {
	//log.trace "[SM] Executing getDeviceFileName() with $deviceType"
	def found = deviceFiles.find { key, value -> key == deviceType }
    return found.value
}

def getSelectedDevices() {
	log.trace "[SM] Executing getSelectedDevices() with ${settings.selectedDevices}"    
	return settings.selectedDevices?.flatten()
}

def initialize() {
	log.trace "[SM] Executing initialize()"
    log.debug "[SM] initialize() - settings=${settings.selectedDevices}"

    selectedDevices.each() { dni ->		//'selectedDevices' comes from the user selection and is global
    	def existingDevice = getChildDevice(dni)
        if( !existingDevice ) {
        	def newDevice = atomicState.ecobeeDeviceList[dni]
            //def deviceType = newDevice.deviceType
        	def d = addChildDevice(app.namespace, getDeviceFileName(newDevice.deviceType), dni, null, ["label":"${newDevice.name}"])
            log.debug "[SM] initialize() - Created ${d.displayName} with dni: ${d.deviceNetworkId}"
        } else {
        	log.debug "[SM] initialize() - ${existingDevice.displayName} with dni: $dni already exists"
        }
    }
    
    def delete
    // Delete any that are no longer in settings
	if(!selectedDevices) {
		log.debug "[SM] initialize() - No selected devices, deleting all previously installed devices."
		delete = getAllChildDevices()
	} else {
		delete = getChildDevices().findAll { !selectedDevices.contains(it.deviceNetworkId) }
	}
	log.debug "[SM] initialize() - deleting ${delete.size()} devices"
	delete.each {
    	log.debug "[SM] initialize() - deleting ${delete.displayName}"
        deleteChildDevice(it.deviceNetworkId)
    }
    
    //if there is devices left, initiate healhPoll
    if (getAllChildDevices()) {
    	pollHandler() //first time polling data data from thermostat
		//automatically update devices status every 5 mins
        unschedule()
		runEvery5Minutes("poll")
    }
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
    def requestBody = [
        selection: [
            selectionType: "registered",
            selectionMatch: "", //thermostatIdsString,
            includeExtendedRuntime: true,
            includeSettings: true,
            includeRuntime: true,
            includeSensors: true
        ]
    ]

	def result = false

	def pollParams = [
        uri: apiEndpoint,
        path: "/1/thermostat",
        headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
        // TODO - the query string below is not consistent with the Ecobee docs:
        // https://www.ecobee.com/home/developer/api/documentation/v1/operations/get-thermostats.shtml
        query: [format: 'json', body: toJson(requestBody)]
    ]

	try{
		httpGet(pollParams) { resp ->
			if(resp.status == 200) {
                atomicState.remoteSensors = resp.data.thermostatList.remoteSensors
                updateSensorData()
                storeThermostatData(resp.data.thermostatList)
                result = true
                log.debug "updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
            }
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.trace "[SM] pollChildren() Exception polling children: " + e.response.data.status
        if (e.response.data.status.code == 14) {
            atomicState.action = "pollChildren"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        }
	}
	return result
}

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild() {

	def devices = getChildDevices()

	if (pollChildren()) {
		devices.each { child ->
			if (!child.device.deviceNetworkId.startsWith("ecobee_sensor")) {
				if(atomicState.thermostats[child.device.deviceNetworkId] != null) {
					def tData = atomicState.thermostats[child.device.deviceNetworkId]
					log.debug "pollChild(child)>> data for ${child.device.deviceNetworkId} : ${tData.data}"
					child.generateEvent(tData.data) //parse received message from parent
				} else if(atomicState.thermostats[child.device.deviceNetworkId] == null) {
                	if ( atomicState.switchList.find { it.key == child.device.deviceNetworkId } ) { //is a smartswitch
                    	return
                    }
					log.error "[SM] pollChild() ERROR: Device connection removed? no data for ${child.device.deviceNetworkId}"
					return null
				}
			}
		}
	} else {
		log.error "ERROR: pollChildren()"
		return null
	}

}

void poll() {
	pollChild()
    pollSwitches()
}

void controlSwitch( dni, desiredState ) {
	
	def d = getChildDevice(dni)
    log.trace "[SM] Executing 'on' controlSwitch for ${d.device.displayName}"
    def body = [ "on": desiredState ]
	def params = [
		uri: apiEndpoint + "/ea/devices/ls/$dni/state",
		headers: ["Content-Type": "application/json;charset=UTF-8", "Authorization": "Bearer ${atomicState.authToken}"],
        body: toJson(body)
	]
    
    try {
    	httpPut(params) { resp ->    
    		log.debug "RESPONSE CODE: ${resp.status}"
        }
    } catch (groovyx.net.http.HttpResponseException e) {
		//log.warn "Code=${e.getStatusCode()}"
        if (e.getStatusCode() == 401) {
        	if ( atomicState.tokenRefreshTries < 2 ) {
            	log.debug "Refreshing your auth_token!"
                atomicState.tokenRefreshTries++
                atomicState.action = "pollChildren"
            	refreshAuthToken()
                controlSwitch( dni, desiredState )
            } else {
            	log.error "Error Refreshing your auth_token! Unable to control your switch: ${d.device.displayName}"              
            }
        }
    	if ( e.getStatusCode() == 200 ) {
        	log.debug "Ecobee response to switch control = 'Success' for ${d.device.displayName}"
            def switchState = desiredState == true ? "on" : "off"
            d.sendEvent(name:"switch", value: switchState)
            atomicState.tokenRefreshTries = 0
        } else if ( e.getStatusCode() != 401 ) {
    		log.error "Exception from device control response: " + e.getCause()       
        	log.error "Exception from device control getMessage: " + e.getMessage()        
        }
    }
    runIn(10, pollSwitches, [overwrite: true])
}

def pollSwitches() {
	log.trace "[SM] Executing 'pollSwitches()'"
	switchesDiscovered()
	def switches = atomicState.switchList
    switches.each() {
    	def childSwitch = getChildDevice(it.key)
        def switchInfo = it.value
        def switchState = switchInfo.state.on == true ? "on" : "off"
        if ( childSwitch ) {
            log.debug "[SM] pollSwitches updating ${childSwitch.device.displayName} to $switchState"
        	childSwitch.sendEvent(name:"switch", value: switchState)
        } else {
        	log.info "[SM] pollSwitches received data for non-smarthings switch, ingoring"
        }
    }
    return null
}

def availableModes(child) {
	def tData = atomicState.thermostats[child.device.deviceNetworkId]

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

	def tData = atomicState.thermostats[child.device.deviceNetworkId]

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
		return null
	}

	def mode = tData.data.thermostatMode
	return mode
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
						if(it.value == "true") {
                            occupancy = "active"
                        } else {
							occupancy = "inactive"
                        }
					}
				}
				def dni = "ecobee_sensor-"+ it?.id + "-" + it?.code
				def d = getChildDevice(dni)
				if(d) {
					d.sendEvent(name:"temperature", value: temperature, unit: location.temperatureScale)
					d.sendEvent(name:"motion", value: occupancy)
				}
			}
		}
	}
}

def toJson(Map m) {
    return groovy.json.JsonOutput.toJson(m)
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

		def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
		//changed to httpPost
		try {
			def jsonMap
			httpPost(refreshParams) { resp ->
				if(resp.status == 200) {
					log.debug "Token refreshed...calling saved RestAction now!"
					saveTokenAndResumeAction(resp.data)
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

/**
 * Saves the refresh and auth token from the passed-in JSON object,
 * and invokes any previously executing action that did not complete due to
 * an expired token.
 *
 * @param json - an object representing the parsed JSON response from Ecobee
 */
private void saveTokenAndResumeAction(json) {
    log.debug "token response json: $json"
    if (json) {
        atomicState.refreshToken = json?.refresh_token
        atomicState.authToken = json?.access_token
        obtainJsonToken()
        if (atomicState.action) {
            log.debug "got refresh token, executing next action: ${atomicState.action}"
            "${atomicState.action}"()
        }
    } else {
        log.warn "did not get response body from refresh token response"
    }
    atomicState.action = ""
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
 * Makes a request to the Ecobee API to actuate the thermostat.
 * Used by command methods to send commands to Ecobee.
 *
 * @param bodyParams - a map of request parameters to send to Ecobee.
 *
 * @return true if the command was accepted by Ecobee without error, false otherwise.
 */
private boolean sendCommandToEcobee(Map bodyParams) {
	def isSuccess = false
	def cmdParams = [
		uri: apiEndpoint,
		path: "/1/thermostat",
		headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
		body: toJson(bodyParams)
	]

	try{
        httpPost(cmdParams) { resp ->
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
        log.trace "Exception Sending Json: " + e.response.data.status
        if (e.response.data.status.code == 14) {
            // TODO - figure out why we're setting the next action to be pollChildren
            // after refreshing auth token. Is it to keep UI in sync, or just copy/paste error?
            atomicState.action = "pollChildren"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        } else {
            log.error "Authentication error, invalid authentication method, lack of credentials, etc."
        }
    }

    return isSuccess
}

def getChildName()           { return "Ecobee Thermostat" }
def getSensorChildName()     { return "Ecobee Sensor" }
def getSwitchChildName()     { return "Ecobee Switch" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getShardUrl()            { return getApiServerUrl() }
def getCallbackUrl()         { return "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()    { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }
def getApiEndpoint()         { return "https://api.ecobee.com" }
def getSmartThingsClientId() { return appSettings.clientId }
private getVendorIcon() 	 { return "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png" }

//send both push notification and mobile activity feeds
def sendPushAndFeeds(notificationMessage) {
	log.warn "sendPushAndFeeds >> notificationMessage: ${notificationMessage}"
	log.warn "sendPushAndFeeds >> atomicState.timeSendPush: ${atomicState.timeSendPush}"
	if (atomicState.timeSendPush) {
		if (now() - atomicState.timeSendPush > 86400000) { // notification is sent to remind user once a day
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

/**
 * Stores data about the thermostats in atomicState.
 * @param thermostats - a list of thermostats as returned from the Ecobee API
 */
private void storeThermostatData(thermostats) {
    log.trace "Storing thermostat data: $thermostats"
    def data
    atomicState.thermostats = thermostats.inject([:]) { collector, stat ->
        def dni = [ app.id, stat.identifier ].join('.')

        data = [
            coolMode: (stat.settings.coolStages > 0),
            heatMode: (stat.settings.heatStages > 0),
            deviceTemperatureUnit: stat.settings.useCelsius,
            minHeatingSetpoint: (stat.settings.heatRangeLow / 10),
            maxHeatingSetpoint: (stat.settings.heatRangeHigh / 10),
            minCoolingSetpoint: (stat.settings.coolRangeLow / 10),
            maxCoolingSetpoint: (stat.settings.coolRangeHigh / 10),
            autoMode: stat.settings.autoHeatCoolFeatureEnabled,
            deviceAlive: stat.runtime.connected == true ? "true" : "false",
            auxHeatMode: (stat.settings.hasHeatPump) && (stat.settings.hasForcedAir || stat.settings.hasElectric || stat.settings.hasBoiler),
            temperature: (stat.runtime.actualTemperature / 10),
            heatingSetpoint: stat.runtime.desiredHeat / 10,
            coolingSetpoint: stat.runtime.desiredCool / 10,
            thermostatMode: stat.settings.hvacMode,
            humidity: stat.runtime.actualHumidity,
            thermostatFanMode: stat.runtime.desiredFanMode
        ]
        // Adjust autoMode in regards to coolMode and heatMode as thermostat may report autoMode:true despite only having heat or cool mode
        data["autoMode"] = data["autoMode"] && data.coolMode && data.heatMode
        data["deviceTemperatureUnit"] = (data?.deviceTemperatureUnit == false && location.temperatureScale == "F") ? "F" : "C"

        collector[dni] = [data:data]
        return collector
    }
    log.debug "updated ${atomicState.thermostats?.size()} thermostats: ${atomicState.thermostats}"
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
