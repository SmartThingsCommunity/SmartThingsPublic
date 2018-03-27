/**
 *  Harmony (Connect) - https://developer.Harmony.com/documentation
 *
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
 *  Author: SmartThings
 *
 *  For complete set of capabilities, attributes, and commands see:
 *
 *  https://graph.api.smartthings.com/ide/doc/capabilities
 *
 *  ---------------------+-------------------+-----------------------------+------------------------------------
 *  Device Type          | Attribute Name    | Commands                    | Attribute Values
 *  ---------------------+-------------------+-----------------------------+------------------------------------
 *  switches             | switch            | on, off                     | on, off
 *  motionSensors        | motion            |                             | active, inactive
 *  contactSensors       | contact           |                             | open, closed
 *  thermostat           | thermostat        | setHeatingSetpoint,         | temperature, heatingSetpoint
 *                       |                   | setCoolingSetpoint(number)  | coolingSetpoint, thermostatSetpoint
 *                       |                   | off, heat, emergencyHeat    | thermostatMode — ["emergency heat", "auto", "cool", "off", "heat"]
 *                       |                   | cool, setThermostatMode     | thermostatFanMode — ["auto", "on", "circulate"]
 *                       |                   | fanOn, fanAuto, fanCirculate| thermostatOperatingState — ["cooling", "heating", "pending heat",
 *                       |                   | setThermostatFanMode, auto  | "fan only", "vent economizer", "pending cool", "idle"]
 *  presenceSensors      | presence          |                             | present, 'not present'
 *  temperatureSensors   | temperature       |                             | <numeric, F or C according to unit>
 *  accelerationSensors  | acceleration      |                             | active, inactive
 *  waterSensors         | water             |                             | wet, dry
 *  lightSensors         | illuminance       |                             | <numeric, lux>
 *  humiditySensors      | humidity          |                             | <numeric, percent>
 *  alarms               | alarm             | strobe, siren, both, off    | strobe, siren, both, off
 *  locks                | lock              | lock, unlock                | locked, unlocked
 *  ---------------------+-------------------+-----------------------------+------------------------------------
 */
include 'asynchttp_v1'

definition(
    name: "Logitech Harmony (Connect)",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Allows you to integrate your Logitech Harmony account with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/harmony.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/harmony%402x.png",
    oauth: [displayName: "Logitech Harmony", displayLink: "http://www.logitech.com/en-us/harmony-remotes"],
    singleInstance: true
){
	appSetting "clientId"
	appSetting "clientSecret"
}

preferences(oauthPage: "deviceAuthorization") {
 	page(name: "Credentials", title: "Connect to your Logitech Harmony device", content: "authPage", install: false, nextPage: "deviceAuthorization")
	page(name: "deviceAuthorization", title: "Logitech Harmony device authorization", install: true) {
		section("Allow Logitech Harmony to control these things...") {
			input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
			input "motionSensors", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
			input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
			input "thermostats", "capability.thermostat", title: "Which Thermostats?", multiple: true, required: false
			input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
			input "temperatureSensors", "capability.temperatureMeasurement", title: "Which Temperature Sensors?", multiple: true, required: false
			input "accelerationSensors", "capability.accelerationSensor", title: "Which Vibration Sensors?", multiple: true, required: false
			input "waterSensors", "capability.waterSensor", title: "Which Water Sensors?", multiple: true, required: false
			input "lightSensors", "capability.illuminanceMeasurement", title: "Which Light Sensors?", multiple: true, required: false
			input "humiditySensors", "capability.relativeHumidityMeasurement", title: "Which Relative Humidity Sensors?", multiple: true, required: false
			input "alarms", "capability.alarm", title: "Which Sirens?", multiple: true, required: false
			input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
		}
	}
    page(name: "revokeToken", title: "You have succesfully logged out of the account.", install: false, nextPage: null)
}

mappings {
	path("/devices") { action: [ GET: "listDevices"] }
	path("/devices/:id") { action: [ GET: "getDevice", PUT: "updateDevice"] }
	path("/subscriptions") { action: [ GET: "listSubscriptions", POST: "addSubscription"] }
	path("/subscriptions/:id") { action: [ DELETE: "removeSubscription"] }
	path("/phrases") { action: [ GET: "listPhrases"] }
	path("/phrases/:id") { action: [ PUT: "executePhrase"] }
	path("/hubs") { action: [ GET: "listHubs" ] }
	path("/hubs/:id") { action: [ GET: "getHub" ] }
	path("/activityCallback/:dni") { action: [ POST: "activityCallback" ] }
	path("/harmony") { action: [ GET: "getHarmony", POST: "harmony" ] }
	path("/harmony/:mac") { action: [ DELETE: "deleteHarmony" ] }
	path("/receivedToken") { action: [ POST: "receivedToken", GET: "receivedToken"] }
	path("/receiveToken") { action: [ POST: "receiveToken", GET: "receiveToken"] }
	path("/hookCallback") { action: [ POST: "hookEventHandler", GET: "hookEventHandler"] }
	path("/oauth/callback") { action: [ GET: "callback" ] }
	path("/oauth/initialize") { action: [ GET: "init"] }
}

def getServerUrl() { return "https://graph.api.smartthings.com" }
def getServercallbackUrl() { "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl() { "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${apiServerUrl}" }

def authPage() {
	def description = null
	if (!state.HarmonyAccessToken) {
		if (!state.accessToken) {
			log.debug "Harmony - About to create access token"
			createAccessToken()
		}
		description = "Click to enter Harmony Credentials"
		def redirectUrl = buildRedirectUrl
        return dynamicPage(name: "Credentials", title: "Harmony", nextPage: null, uninstall: true, install:false) {
            section { paragraph title: "Note:", "This device has not been officially tested and certified to “Work with SmartThings”. You can connect it to your SmartThings home but performance may vary and we will not be able to provide support or assistance." }
            section { href url:redirectUrl, style:"embedded", required:true, title:"Harmony", description:description }
        }
    } else {
		//device discovery request every 5 //25 seconds
		int deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as int
		state.deviceRefreshCount = deviceRefreshCount + 1
		def refreshInterval = 5

		def huboptions = state.HarmonyHubs ?: []
		def actoptions = state.HarmonyActivities ?: []

		def numFoundHub = huboptions.size() ?: 0
		def numFoundAct = actoptions.size() ?: 0

		if((deviceRefreshCount % 5) == 0) {
			discoverDevices()
		}

		return dynamicPage(name:"Credentials", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
			section("Please wait while we discover your Harmony Hubs and Activities. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
				input "selectedhubs", "enum", required:false, title:"Select Harmony Hubs (${numFoundHub} found)", multiple:true, submitOnChange: true, options:huboptions
			}
      	// Virtual activity flag
      	if (numFoundHub > 0 && numFoundAct > 0 && true)
			section("You can also add activities as virtual switches for other convenient integrations") {
				input "selectedactivities", "enum", required:false, title:"Select Harmony Activities (${numFoundAct} found)", multiple:true, submitOnChange: true, options:actoptions
			}
            section("") {
                paragraph "If you have added another hub to your Logitech Harmony account you need to log out and reconnect to authorize access."
                href "revokeToken", title: "Log out from account", description: "", state: "incomplete"
            }
    	if (state.resethub)
			section("Connection to the hub timed out. Please restart the hub and try again.") {}
		}
    }
}

def revokeToken() {
	 return dynamicPage(name: "revokeToken", title: "You have succesfully logged out of the account.") {
     	deleteToken()
     }
}

def callback() {
	def redirectUrl = null
	if (params.authQueryString) {
		redirectUrl = URLDecoder.decode(params.authQueryString.replaceAll(".+&redirect_url=", ""))
		log.debug "Harmony - redirectUrl: ${redirectUrl}"
	} else {
		log.warn "Harmony - No authQueryString"
	}

	if (state.HarmonyAccessToken) {
		log.debug "Harmony - Access token already exists"
		discovery()
		success()
	} else {
		def code = params.code
		if (code) {
			if (code.size() > 6) {
				// Harmony code
				log.debug "Harmony - Exchanging code for access token"
				receiveToken(redirectUrl)
			} else {
				// Initiate the Harmony OAuth flow.
				init()
			}
		} else {
			log.debug "Harmony - This code should be unreachable"
			success()
		}
	}
}

def deleteToken() {
	if (state?.HarmonyAccessToken) {
		state.HarmonyAccessToken = null;
    }
}

def init() {
	log.debug "Harmony - Requesting Code"
	def oauthParams = [client_id: "${appSettings.clientId}", scope: "remote", response_type: "code", redirect_uri: "${servercallbackUrl}" ]
	redirect(location: "https://home.myharmony.com/oauth2/authorize?${toQueryString(oauthParams)}")
}

def receiveToken(redirectUrl = null) {
	log.debug "Harmony - receiveToken"
    def oauthParams = [ client_id: "${appSettings.clientId}", client_secret: "${appSettings.clientSecret}", grant_type: "authorization_code", code: params.code ]
    def params = [
      uri: "https://home.myharmony.com/oauth2/token?${toQueryString(oauthParams)}",
    ]
	try {
        httpPost(params) { response ->
            state.HarmonyAccessToken = response.data.access_token
        }
	} catch (java.util.concurrent.TimeoutException e) {
    	fail(e)
		log.warn "Harmony - Connection timed out, please try again later."
	}
    discovery()
	if (state.HarmonyAccessToken) {
		success()
	} else {
		fail("")
	}
}

def success() {
	def message = """
		<p>Your Harmony Account is now connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
	"""
	connectionStatus(message)
}

def fail(msg) {
    def message = """
        <p>The connection could not be established!</p>
        <p>$msg</p>
        <p>Click 'Done' to return to the menu.</p>
    """
    connectionStatus(message)
}

def receivedToken() {
	def message = """
		<p>Your Harmony Account is already connected to SmartThings!</p>
		<p>Click 'Done' to finish setup.</p>
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
        <title>SmartThings Connection</title>
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
		${redirectHtml}
        </head>
        <body>
            <div class="container">
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/harmony@2x.png" alt="Harmony icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                ${message}
            </div>
        </body>
        </html>
	"""
	render contentType: 'text/html', data: html
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def buildRedirectUrl(page) {
    return "${serverUrl}/api/token/${state.accessToken}/smartapps/installations/${app.id}/${page}"
}

def installed() {
	if (!state.accessToken) {
		log.debug "Harmony - About to create access token"
		createAccessToken()
	} else {
		initialize()
	}
}

def updated() {
	if (!state.accessToken) {
		log.debug "Harmony - About to create access token"
		createAccessToken()
	} else {
		initialize()
	}
}

def uninstalled() {
	if (state.HarmonyAccessToken) {
		try {
        	state.HarmonyAccessToken = ""
        	log.debug "Harmony - Success disconnecting Harmony from SmartThings"
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "Harmony - Error disconnecting Harmony from SmartThings: ${e.statusCode}"
		}
	}
}

def initialize() {
	state.aux = 0
	if (selectedhubs || selectedactivities) {
		addDevice()
	    runEvery5Minutes("poll")
	    log.trace getActivityList()
	}
}

def getHarmonydevices() {
	state.Harmonydevices ?: []
}

Map discoverDevices() {
    log.trace "Harmony - Discovering devices..."
    discovery()
    if (getHarmonydevices() != []) {
        def devices = state.Harmonydevices.hubs
        log.trace devices.toString()
        def activities = [:]
        def hubs = [:]
        devices.each {
			if (it.value.response){
				def hubkey = it.key
				def hubname = getHubName(it.key)
				def hubvalue = "${hubname}"
				hubs["harmony-${hubkey}"] = hubvalue
				it.value.response.data?.activities?.each {
					def value = "${it.value.name}"
					def key = "harmony-${hubkey}-${it.key}"
					activities["${key}"] = value
				}
			} else {
				log.trace "Harmony - Device $it.key is no longer available"
			}
        }
        state.HarmonyHubs = hubs
        state.HarmonyActivities = activities
    }
}

//CHILD DEVICE METHODS
def discovery() {
	def tokenParam = [auth: state.HarmonyAccessToken]
	def url = "https://home.myharmony.com/cloudapi/activity/all?${toQueryString(tokenParam)}"
	def params = [
		uri: url,
		contentType: 'application/json'
	]
	asynchttp_v1.get('discoveryResponse', params)
	log.trace "Harmony - Discovery Command Sent"
}

def discoveryResponse(response, data) {
	if (response.hasError()) {
		log.error "Harmony - response has error: $response.errorMessage"
		if (response.status == 401) { // token is expired
			state.remove("HarmonyAccessToken")
			log.warn "Harmony - Access token has expired"
		} else {
			log.warn "Harmony - Connection to the hub timed out. Please restart the hub and try again."
			state.resethub = true
		}
	} else {
		if (response.status == 200) {
			log.debug "Harmony - valid Token"
			state.Harmonydevices = response.json
			state.resethub = false
	    } else {
			log.warn "Harmony - Error, response status: $response.status"
	    }
	}
}

def addDevice() {
    log.trace "Harmony - Adding Hubs"
    selectedhubs.each { dni ->
        def d = getChildDevice(dni)
        if(!d) {
            def newAction = state.HarmonyHubs.find { it.key == dni }
            d = addChildDevice("smartthings", "Logitech Harmony Hub C2C", dni, null, [label:"${newAction.value}"])
            log.trace "Harmony - Created ${d.displayName} with id $dni"
            poll()
        } else {
            log.trace "Harmony - Found ${d.displayName} with id $dni already exists"
        }
    }
    log.trace "Harmony - Adding Activities"
    selectedactivities.each { dni ->
        def d = getChildDevice(dni)
        if(!d) {
            def newAction = state.HarmonyActivities.find { it.key == dni }
            if (newAction) {
	            d = addChildDevice("smartthings", "Harmony Activity", dni, null, [label:"${newAction.value} [Harmony Activity]"])
	            log.trace "Harmony - Created ${d.displayName} with id $dni"
	            poll()
            }
        } else {
            log.trace "Harmony - Found ${d.displayName} with id $dni already exists"
        }
    }
}

def activity(dni,mode) {
    def tokenParam = [auth: state.HarmonyAccessToken]
    def url
    if (dni == "all") {
        url = "https://home.myharmony.com/cloudapi/activity/off?${toQueryString(tokenParam)}"
    } else {
        def aux = dni.split('-')
        def hubId = aux[1]
        if (mode == "hub" || (aux.size() <= 2) || (aux[2] == "off")){
        	url = "https://home.myharmony.com/cloudapi/hub/${hubId}/activity/off?${toQueryString(tokenParam)}"
        } else {
          def activityId = aux[2]
        	url = "https://home.myharmony.com/cloudapi/hub/${hubId}/activity/${activityId}/${mode}?${toQueryString(tokenParam)}"
        }
	}
	def params = [
		uri: url,
		contentType: 'application/json'
	]
	asynchttp_v1.post('activityResponse', params)
	log.trace "Harmony - Command Sent"
}

def activityResponse(response, data) {
	if (response.hasError()) {
		log.error "Harmony - response has error: $response.errorMessage"
		if (response.status == 401) { // token is expired
			state.remove("HarmonyAccessToken")
			log.warn "Harmony - Access token has expired"
		}
	} else {
		if (response.status == 200) {
			log.trace "Harmony - Command sent succesfully"
			poll()
		} else {
			log.trace "Harmony - Command failed. Error: $response.status"
		}
	}
}

def poll() {
	// GET THE LIST OF ACTIVITIES
    if (state.HarmonyAccessToken) {
        def tokenParam = [auth: state.HarmonyAccessToken]
        def params = [
            uri: "https://home.myharmony.com/cloudapi/state?${toQueryString(tokenParam)}",
            headers: ["Accept": "application/json"],
            contentType: 'application/json'
        ]
        asynchttp_v1.get('pollResponse', params)
      } else {
        log.warn "Harmony - Access token has expired"
      }
}

def pollResponse(response, data) {
	if (response.hasError()) {
	    log.error "Harmony - response has error: $response.errorMessage"
	    if (response.status == 401) { // token is expired
			state.remove("HarmonyAccessToken")
			log.warn "Harmony - Access token has expired"
	    }
	} else {
		def ResponseValues
		try {
			// json response already parsed into JSONElement object
			ResponseValues = response.json
		} catch (e) {
			log.error "Harmony - error parsing json from response: $e"
		}
		if (ResponseValues) {
	        def map = [:]
	        ResponseValues.hubs.each {
		        // Device-Watch relies on the Logitech Harmony Cloud to get the Device state.
		        def isAlive = it.value.status
		        def d = getChildDevice("harmony-${it.key}")
		        d?.sendEvent(name: "DeviceWatch-DeviceStatus", value: isAlive!=504? "online":"offline", displayed: false, isStateChange: true)
		        if (it.value.message == "OK") {
					map["${it.key}"] = "${it.value.response.data.currentAvActivity},${it.value.response.data.activityStatus}"
					def hub = getChildDevice("harmony-${it.key}")
					if (hub) {
						if (it.value.response.data.currentAvActivity == "-1") {
							hub.sendEvent(name: "currentActivity", value: "--", descriptionText: "There isn't any activity running", displayed: false)
						} else {
							def currentActivity
							def activityDTH = getChildDevice("harmony-${it.key}-${it.value.response.data.currentAvActivity}")
							if (activityDTH)
								currentActivity = activityDTH.device.displayName
							else
								currentActivity = getActivityName(it.value.response.data.currentAvActivity,it.key)
							hub.sendEvent(name: "currentActivity", value: currentActivity, descriptionText: "Current activity is ${currentActivity}", displayed: false)
						}
					}
	          	} else {
	            	log.trace "Harmony - error response: $it.value.message"
	          	}
        	}
	        def activities = getChildDevices()
	        def activitynotrunning = true
	        activities.each { activity ->
	            def act = activity.deviceNetworkId.split('-')
	            if (act.size() > 2) {
	                def aux = map.find { it.key == act[1] }
	                if (aux) {
	                    def aux2 = aux.value.split(',')
	                    def childDevice = getChildDevice(activity.deviceNetworkId)
	                    if ((act[2] == aux2[0]) && (aux2[1] == "1" || aux2[1] == "2")) {
	                        childDevice?.sendEvent(name: "switch", value: "on")
	                        if (aux2[1] == "1")
	                            runIn(5, "poll", [overwrite: true])
	                    } else {
	                        childDevice?.sendEvent(name: "switch", value: "off")
	                        if (aux2[1] == "3")
	                            runIn(5, "poll", [overwrite: true])
	                    }
	                }
	            }
	        }
		} else {
			log.debug "Harmony - did not get json results from response body: $response.data"
		}
	}
}

def getActivityList() {
    if (state.HarmonyAccessToken) {
		def tokenParam = [auth: state.HarmonyAccessToken]
		def url = "https://home.myharmony.com/cloudapi/activity/all?${toQueryString(tokenParam)}"
		def params = [
			uri: url,
			contentType: 'application/json'
		]
		asynchttp_v1.get('getActivityListResponse', params)
		log.trace "Harmony - Activity List Request Sent"
	}
}

def getActivityListResponse(response, data) {
	if (response.hasError()) {
		log.error "Harmony - response has error: $response.errorMessage"
		if (response.status == 401) { // token is expired
			state.remove("HarmonyAccessToken")
			log.warn "Harmony - Access token has expired"
		}
	} else {
		log.trace "Harmony - Parsing Activity List Response"
		response.json.hubs.each {
			def hub = getChildDevice("harmony-${it.key}")
			if (hub) {
				def hubname = getHubName("${it.key}")
				def activities = []
				def aux = it.value.response.data.activities.size()
				if (aux >= 1) {
					activities = it.value.response.data.activities.collect {
						[id: it.key, name: it.value['name'], type: it.value['type']]
					}
					activities += [id: "off", name: "Activity OFF", type: "0"]
				}
				hub.sendEvent(name: "activities", value: new groovy.json.JsonBuilder(activities).toString(), descriptionText: "Activities are ${activities.collect { it.name }?.join(', ')}", displayed: false)
			}
		}
	}
}

def getActivityName(activity,hubId) {
	// GET ACTIVITY'S NAME
    def actname = activity
    if (state.HarmonyAccessToken) {
        def Params = [auth: state.HarmonyAccessToken]
        def url = "https://home.myharmony.com/cloudapi/hub/${hubId}/activity/all?${toQueryString(Params)}"
        try {
            httpGet(uri: url, headers: ["Accept": "application/json"]) {response ->
                actname = response.data.data.activities[activity].name
            }
		} catch(Exception e) {
        	log.trace e
		}
    }
	return actname
}

def getActivityId(activity,hubId) {
	// GET ACTIVITY'S NAME
    def actid = activity
    if (state.HarmonyAccessToken) {
        def Params = [auth: state.HarmonyAccessToken]
        def url = "https://home.myharmony.com/cloudapi/hub/${hubId}/activity/all?${toQueryString(Params)}"
        try {
            httpGet(uri: url, headers: ["Accept": "application/json"]) {response ->
            	response.data.data.activities.each {
                	if (it.value.name == activity)
                		actid = it.key
                }
            }
		} catch(Exception e) {
        	log.trace "Harmony - getActivityId() response $e"
		}
    }
	return actid
}

def getHubName(hubId) {
	// GET HUB'S NAME
    def hubname = hubId
    if (state.HarmonyAccessToken) {
        def Params = [auth: state.HarmonyAccessToken]
        def url = "https://home.myharmony.com/cloudapi/hub/${hubId}/discover?${toQueryString(Params)}"
        try {
            httpGet(uri: url, headers: ["Accept": "application/json"]) {response ->
                hubname = response.data.data.name
            }
		} catch(Exception e) {
        	log.trace "Harmony - getHubName() response $e"
		}
    }
	return hubname
}

def sendNotification(msg) {
	sendNotification(msg)
}

def hookEventHandler() {
    // log.debug "In hookEventHandler method."
    log.debug "Harmony - request = ${request}"

    def json = request.JSON

	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}

def listDevices() {
	log.debug "Harmony - getDevices(), params: ${params}"
	allDevices.collect {
		deviceItem(it)
	}
}

def getDevice() {
	log.debug "Harmony - getDevice(), params: ${params}"
	def device = allDevices.find { it.id == params.id }
	if (!device) {
		render status: 404, data: '{"msg": "Device not found"}'
	} else {
		deviceItem(device)
	}
}

def updateDevice() {
	def data = request.JSON
	def command = data.command
	def arguments = data.arguments
	log.debug "Harmony - updateDevice(), params: ${params}, request: ${data}"
	if (!command) {
		render status: 400, data: '{"msg": "command is required"}'
	} else {
		def device = allDevices.find { it.id == params.id }
    if (device) {
        if (validateCommand(device, command)) {
            if (arguments) {
                device."$command"(*arguments)
            } else {
                device."$command"()
            }
            render status: 204, data: "{}"
        } else {
          render status: 403, data: '{"msg": "Access denied. This command is not supported by current capability."}'
        }
    } else {
      render status: 404, data: '{"msg": "Device not found"}'
    }
  }
}

/**
 * Validating the command passed by the user based on capability.
 * @return boolean
 */
def validateCommand(device, command) {
	def capabilityCommands = getDeviceCapabilityCommands(device.capabilities)
	def currentDeviceCapability = getCapabilityName(device)
	if (currentDeviceCapability != "" && capabilityCommands[currentDeviceCapability]) {
    return (command in capabilityCommands[currentDeviceCapability] || (currentDeviceCapability == "Switch" && command == "setLevel" && device.hasCommand("setLevel"))) ? true : false
	} else {
		// Handling other device types here, which don't accept commands
		httpError(400, "Bad request.")
	}
}

/**
 * Need to get the attribute name to do the lookup. Only
 * doing it for the device types which accept commands
 * @return attribute name of the device type
 */
def getCapabilityName(device) {
    def capName = ""
    if (switches.find{it.id == device.id})
			capName = "Switch"
		else if (alarms.find{it.id == device.id})
			capName = "Alarm"
		else if (locks.find{it.id == device.id})
			capName = "Lock"
    log.trace "Device: $device - Capability Name: $capName"
		return capName
}

/**
 * Constructing the map over here of
 * supported commands by device capability
 * @return a map of device capability -> supported commands
 */
def getDeviceCapabilityCommands(deviceCapabilities) {
	def map = [:]
	deviceCapabilities.collect {
		map[it.name] = it.commands.collect{ it.name.toString() }
	}
	return map
}

def listSubscriptions() {
	log.debug "Harmony - listSubscriptions()"
	app.subscriptions?.findAll { it.device?.device && it.device.id }?.collect {
		def deviceInfo = state[it.device.id]
		def response = [
			id: it.id,
			deviceId: it.device.id,
			attributeName: it.data,
			handler: it.handler
		]
		if (!state.harmonyHubs) {
			response.callbackUrl = deviceInfo?.callbackUrl
		}
		response
	} ?: []
}

def addSubscription() {
	def data = request.JSON
	def attribute = data.attributeName
	def callbackUrl = data.callbackUrl

	log.debug "Harmony - addSubscription, params: ${params}, request: ${data}"
	if (!attribute) {
		render status: 400, data: '{"msg": "attributeName is required"}'
	} else {
		def device = allDevices.find { it.id == data.deviceId }
		if (device) {
			if (!state.harmonyHubs) {
				log.debug "Harmony - Adding callbackUrl: $callbackUrl"
				state[device.id] = [callbackUrl: callbackUrl]
			}
			log.debug "Harmony - Adding subscription"
			def subscription = subscribe(device, attribute, deviceHandler)
			if (!subscription || !subscription.eventSubscription) {
				subscription = app.subscriptions?.find { it.device?.device && it.device.id == data.deviceId && it.data == attribute && it.handler == 'deviceHandler' }
			}

			def response = [
				id: subscription.id,
				deviceId: subscription.device.id,
				attributeName: subscription.data,
				handler: subscription.handler
			]
			if (!state.harmonyHubs) {
				response.callbackUrl = callbackUrl
			}
			response
		} else {
			render status: 400, data: '{"msg": "Device not found"}'
		}
	}
}

def removeSubscription() {
	def subscription = app.subscriptions?.find { it.id == params.id }
	def device = subscription?.device

	log.debug "removeSubscription, params: ${params}, subscription: ${subscription}, device: ${device}"
	if (device) {
		log.debug "Harmony - Removing subscription for device: ${device.id}"
		state.remove(device.id)
		unsubscribe(device)
	}
	render status: 204, data: "{}"
}

def listPhrases() {
	location.helloHome.getPhrases()?.collect {[
		id: it.id,
		label: it.label
	]}
}

def executePhrase() {
	log.debug "executedPhrase, params: ${params}"
	location.helloHome.execute(params.id)
	render status: 204, data: "{}"
}

def deviceHandler(evt) {
	def deviceInfo = state[evt.deviceId]
	if (state.harmonyHubs) {
		state.harmonyHubs.each { harmonyHub ->
      log.trace "Harmony - Sending data to $harmonyHub.name"
			sendToHarmony(evt, harmonyHub.callbackUrl)
		}
	} else if (deviceInfo) {
		if (deviceInfo.callbackUrl) {
			sendToHarmony(evt, deviceInfo.callbackUrl)
		} else {
			log.warn "Harmony - No callbackUrl set for device: ${evt.deviceId}"
		}
	} else {
		log.warn "Harmony - No subscribed device found for device: ${evt.deviceId}"
	}
}

def sendToHarmony(evt, String callbackUrl) {
  def callback = new URI(callbackUrl)
  if (callback.port != -1) {
  	def host = callback.port != -1 ? "${callback.host}:${callback.port}" : callback.host
  	def path = callback.query ? "${callback.path}?${callback.query}".toString() : callback.path
  	sendHubCommand(new physicalgraph.device.HubAction(
  		method: "POST",
  		path: path,
  		headers: [
  			"Host": host,
  			"Content-Type": "application/json"
  		],
  		body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value]]
  	))
  } else {
    def params = [
      uri: callbackUrl,
      body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value]]
    ]
    try {
        log.debug "Harmony - Sending data to Harmony Cloud: $params"
        httpPostJson(params) { resp ->
            log.debug "Harmony - Cloud Response: ${resp.status}"
        }
    } catch (e) {
        log.error "Harmony - Cloud Something went wrong: $e"
    }
  }
}

def listHubs() {
	location.hubs?.findAll { it.type.toString() == "PHYSICAL" }?.collect { hubItem(it) }
}

def getHub() {
	def hub = location.hubs?.findAll { it.type.toString() == "PHYSICAL" }?.find { it.id == params.id }
	if (!hub) {
		render status: 404, data: '{"msg": "Hub not found"}'
	} else {
		hubItem(hub)
	}
}

def activityCallback() {
	def data = request.JSON
	def device = getChildDevice(params.dni)
	if (device) {
		if (data.errorCode == "200") {
			device.setCurrentActivity(data.currentActivityId)
		} else {
			log.warn "Harmony - Activity callback error: ${data}"
		}
	} else {
		log.warn "Harmony - Activity callback sent to non-existant dni: ${params.dni}"
	}
	render status: 200, data: '{"msg": "Successfully received callbackUrl"}'
}

def getHarmony() {
	state.harmonyHubs ?: []
}

def harmony() {
	def data = request.JSON
	if (data.mac && data.callbackUrl && data.name) {
		if (!state.harmonyHubs) { state.harmonyHubs = [] }
		def harmonyHub = state.harmonyHubs.find { it.mac == data.mac }
		if (harmonyHub) {
			harmonyHub.mac = data.mac
			harmonyHub.callbackUrl = data.callbackUrl
			harmonyHub.name = data.name
		} else {
			state.harmonyHubs << [mac: data.mac, callbackUrl: data.callbackUrl, name: data.name]
		}
		render status: 200, data: '{"msg": "Successfully received Harmony data"}'
	} else {
		if (!data.mac) {
			render status: 400, data: '{"msg": "mac is required"}'
		} else if (!data.callbackUrl) {
			render status: 400, data: '{"msg": "callbackUrl is required"}'
		} else if (!data.name) {
			render status: 400, data: '{"msg": "name is required"}'
		}
	}
}

def deleteHarmony() {
	log.debug "Harmony - Trying to delete Harmony hub with mac: ${params.mac}"
	def harmonyHub = state.harmonyHubs?.find { it.mac == params.mac }
	if (harmonyHub) {
		log.debug "Harmony - Deleting Harmony hub with mac: ${params.mac}"
		state.harmonyHubs.remove(harmonyHub)
	} else {
		log.debug "Harmony - Couldn't find Harmony hub with mac: ${params.mac}"
	}
	render status: 204, data: "{}"
}

private getAllDevices() {
	([] + switches + motionSensors + contactSensors + thermostats + presenceSensors + temperatureSensors + accelerationSensors + waterSensors + lightSensors + humiditySensors + alarms + locks)?.findAll()?.unique { it.id }
}

private deviceItem(device) {
	[
		id: device.id,
		label: device.displayName,
		currentStates: device.currentStates,
		capabilities: device.capabilities?.collect {[
			name: it.name
		]},
		attributes: device.supportedAttributes?.collect {[
			name: it.name,
			dataType: it.dataType,
			values: it.values
		]},
		commands: device.supportedCommands?.collect {[
			name: it.name,
			arguments: it.arguments
		]},
		type: [
			name: device.typeName,
			author: device.typeAuthor
		]
	]
}

private hubItem(hub) {
	[
		id: hub.id,
		name: hub.name,
		ip: hub.localIP,
		port: hub.localSrvPortTCP
	]
}
