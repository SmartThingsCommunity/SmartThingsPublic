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
def getCallbackUrl() { "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl() { "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${apiServerUrl}" }

def authPage() {
    def description = null
    if (!state.HarmonyAccessToken) {
		if (!state.accessToken) {
			log.debug "About to create access token"
			createAccessToken()
		}
        description = "Click to enter Harmony Credentials"
        def redirectUrl = buildRedirectUrl
        return dynamicPage(name: "Credentials", title: "Harmony", nextPage: null, uninstall: true, install:false) {
               section { href url:redirectUrl, style:"embedded", required:true, title:"Harmony", description:description }
        }
    } else {
		//device discovery request every 5 //25 seconds
		int deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as int
		state.deviceRefreshCount = deviceRefreshCount + 1
		def refreshInterval = 3

		def huboptions = state.HarmonyHubs ?: []
		def actoptions = state.HarmonyActivities ?: []

		def numFoundHub = huboptions.size() ?: 0
        def numFoundAct = actoptions.size() ?: 0
		if((deviceRefreshCount % 5) == 0) {
			discoverDevices()
		}
		return dynamicPage(name:"Credentials", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
			section("Please wait while we discover your Harmony Hubs and Activities. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
				input "selectedhubs", "enum", required:false, title:"Select Harmony Hubs (${numFoundHub} found)", multiple:true, options:huboptions
			}
            // Virtual activity flag
            if (numFoundHub > 0 && numFoundAct > 0 && true)
			section("You can also add activities as virtual switches for other convenient integrations") {
				input "selectedactivities", "enum", required:false, title:"Select Harmony Activities (${numFoundAct} found)", multiple:true, options:actoptions
			}
            if (state.resethub)
			section("Connection to the hub timed out. Please restart the hub and try again.") {}
		}
    }
}

def callback() {
	def redirectUrl = null
	if (params.authQueryString) {
		redirectUrl = URLDecoder.decode(params.authQueryString.replaceAll(".+&redirect_url=", ""))
		log.debug "redirectUrl: ${redirectUrl}"
	} else {
		log.warn "No authQueryString"
	}

	if (state.HarmonyAccessToken) {
		log.debug "Access token already exists"
		discovery()
		success()
	} else {
		def code = params.code
		if (code) {
			if (code.size() > 6) {
				// Harmony code
				log.debug "Exchanging code for access token"
				receiveToken(redirectUrl)
			} else {
				// Initiate the Harmony OAuth flow.
				init()
			}
		} else {
			log.debug "This code should be unreachable"
			success()
		}
	}
}

def init() {
	log.debug "Requesting Code"
	def oauthParams = [client_id: "${appSettings.clientId}", scope: "remote", response_type: "code", redirect_uri: "${callbackUrl}" ]
	redirect(location: "https://home.myharmony.com/oauth2/authorize?${toQueryString(oauthParams)}")
}

def receiveToken(redirectUrl = null) {
	log.debug "receiveToken"
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
		log.warn "Connection timed out, please try again later."
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
		log.debug "About to create access token"
		createAccessToken()
	} else {
		initialize()
	}
}

def updated() {
	unsubscribe()
  unschedule()
	if (!state.accessToken) {
		log.debug "About to create access token"
		createAccessToken()
	} else {
		initialize()
	}
}

def uninstalled() {
	if (state.HarmonyAccessToken) {
		try {
        	state.HarmonyAccessToken = ""
        	log.debug "Success disconnecting Harmony from SmartThings"
		} catch (groovyx.net.http.HttpResponseException e) {
			log.error "Error disconnecting Harmony from SmartThings: ${e.statusCode}"
		}
	}
}

def initialize() {
	state.aux = 0
	if (selectedhubs || selectedactivities) {
		addDevice()
        runEvery5Minutes("discovery")
	}
}

def getHarmonydevices() {
	state.Harmonydevices ?: []
}

Map discoverDevices() {
    log.trace "Discovering devices..."
    discovery()
    if (getHarmonydevices() != []) {
        def devices = state.Harmonydevices.hubs
        log.trace devices.toString()
        def activities = [:]
        def hubs = [:]
        devices.each {
        	def hubkey = it.key
            def hubname = getHubName(it.key)
            def hubvalue = "${hubname}"
            hubs["harmony-${hubkey}"] = hubvalue
        	it.value.response.data.activities.each {
                def value = "${it.value.name}"
                def key = "harmony-${hubkey}-${it.key}"
                activities["${key}"] = value
           }
        }
        state.HarmonyHubs = hubs
        state.HarmonyActivities = activities
    }
}

//CHILD DEVICE METHODS
def discovery() {
    def Params = [auth: state.HarmonyAccessToken]
    def url = "https://home.myharmony.com/cloudapi/activity/all?${toQueryString(Params)}"
	try {
		httpGet(uri: url, headers: ["Accept": "application/json"]) {response ->
	    	if (response.status == 200) {
            	log.debug "valid Token"
                state.Harmonydevices = response.data
                state.resethub = false
                getActivityList()
                poll()
	        } else {
            	log.debug "Error: $response.status"
            }
	    }
	} catch (groovyx.net.http.HttpResponseException e) {
        if (e.statusCode == 401) { // token is expired
            state.remove("HarmonyAccessToken")
            log.warn "Harmony Access token has expired"
        }
	} catch (java.net.SocketTimeoutException e) {
		log.warn "Connection to the hub timed out. Please restart the hub and try again."
        state.resethub = true
 	} catch (e) {
		log.warn "Hostname in certificate didn't match. Please try again later."
	}
    return null
}

def addDevice() {
    log.trace "Adding Hubs"
    selectedhubs.each { dni ->
        def d = getChildDevice(dni)
        if(!d) {
            def newAction = state.HarmonyHubs.find { it.key == dni }
            d = addChildDevice("smartthings", "Logitech Harmony Hub C2C", dni, null, [label:"${newAction.value}"])
            log.trace "created ${d.displayName} with id $dni"
            poll()
        } else {
            log.trace "found ${d.displayName} with id $dni already exists"
        }
    }
    log.trace "Adding Activities"
    selectedactivities.each { dni ->
        def d = getChildDevice(dni)
        if(!d) {
            def newAction = state.HarmonyActivities.find { it.key == dni }
            d = addChildDevice("smartthings", "Harmony Activity", dni, null, [label:"${newAction.value} [Harmony Activity]"])
            log.trace "created ${d.displayName} with id $dni"
            poll()
        } else {
            log.trace "found ${d.displayName} with id $dni already exists"
        }
    }
}

def activity(dni,mode) {
    def Params = [auth: state.HarmonyAccessToken]
    def msg = "Command failed"
    def url = ''
    if (dni == "all") {
        url = "https://home.myharmony.com/cloudapi/activity/off?${toQueryString(Params)}"
    } else {
        def aux = dni.split('-')
        def hubId = aux[1]
        if (mode == "hub" || (aux.size() <= 2) || (aux[2] == "off")){
        	url = "https://home.myharmony.com/cloudapi/hub/${hubId}/activity/off?${toQueryString(Params)}"
        } else {
            def activityId = aux[2]
        	url = "https://home.myharmony.com/cloudapi/hub/${hubId}/activity/${activityId}/${mode}?${toQueryString(Params)}"
        }
	}
	try {
    	httpPostJson(uri: url) { response ->
        	if (response.data.code == 200 || dni == "all") {
            	msg = "Command sent succesfully"
            	state.aux = 0
            } else {
            	msg = "Command failed. Error: $response.data.code"
            }
		}
    } catch (groovyx.net.http.HttpResponseException ex) {
        log.error ex
        if (state.aux == 0) {
        	state.aux = 1
        	activity(dni,mode)
        } else {
        	msg = ex
            state.aux = 0
        }
    } catch(Exception ex) {
    	msg = ex
    }
    runIn(10, "poll", [overwrite: true])
    return msg
}

def poll() {
	// GET THE LIST OF ACTIVITIES
    if (state.HarmonyAccessToken) {
    	getActivityList()
        def Params = [auth: state.HarmonyAccessToken]
        def url = "https://home.myharmony.com/cloudapi/state?${toQueryString(Params)}"
        try {
            httpGet(uri: url, headers: ["Accept": "application/json"]) {response ->
                def map = [:]
                response.data.hubs.each {
		            if (it.value.message == "OK") {
	                    map["${it.key}"] = "${it.value.response.data.currentAvActivity},${it.value.response.data.activityStatus}"
	                    def hub = getChildDevice("harmony-${it.key}")
	                    if (hub) {
	                        if (it.value.response.data.currentAvActivity == "-1") {
	                            hub.sendEvent(name: "currentActivity", value: "--", descriptionText: "There isn't any activity running", display: false)
	                        } else {
	                            def currentActivity = getActivityName(it.value.response.data.currentAvActivity,it.key)
	                            hub.sendEvent(name: "currentActivity", value: currentActivity, descriptionText: "Current activity is ${currentActivity}", display: false)
	                        }
	                    }
	                } else {
		                log.trace it.value.message
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
                return "Poll completed $map - $state.hubs"
            }
        } catch (groovyx.net.http.HttpResponseException e) {
            if (e.statusCode == 401) { // token is expired
                state.remove("HarmonyAccessToken")
                return "Harmony Access token has expired"
            }
		} catch(Exception e) {
        	log.trace e
		}
	}
}


def getActivityList() {
	// GET ACTIVITY'S NAME
    if (state.HarmonyAccessToken) {
        def Params = [auth: state.HarmonyAccessToken]
        def url = "https://home.myharmony.com/cloudapi/activity/all?${toQueryString(Params)}"
        try {
            httpGet(uri: url, headers: ["Accept": "application/json"]) {response ->
                response.data.hubs.each {
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
                            log.trace activities
                        }
                        hub.sendEvent(name: "activities", value: new groovy.json.JsonBuilder(activities).toString(), descriptionText: "Activities are ${activities.collect { it.name }?.join(', ')}", display: false)
					}
                }
            }
        } catch (groovyx.net.http.HttpResponseException e) {
        	log.trace e
        } catch (java.net.SocketTimeoutException e) {
        	log.trace e
		} catch(Exception e) {
        	log.trace e
		}
    }
	return activity
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
        	log.trace e
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
        	log.trace e
		}
    }
	return hubname
}

def sendNotification(msg) {
	sendNotification(msg)
}

def hookEventHandler() {
    // log.debug "In hookEventHandler method."
    log.debug "request = ${request}"

    def json = request.JSON

	def html = """{"code":200,"message":"OK"}"""
	render contentType: 'application/json', data: html
}

def listDevices() {
	log.debug "getDevices, params: ${params}"
	allDevices.collect {
		deviceItem(it)
	}
}

def getDevice() {
	log.debug "getDevice, params: ${params}"
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

	log.debug "updateDevice, params: ${params}, request: ${data}"
	if (!command) {
		render status: 400, data: '{"msg": "command is required"}'
	} else {
		def device = allDevices.find { it.id == params.id }
		if (device) {
        	if (device.hasCommand("$command")) {
                if (arguments) {
                    device."$command"(*arguments)
                } else {
                    device."$command"()
                }
                render status: 204, data: "{}"
           	} else {
				render status: 404, data: '{"msg": "Command not supported by this Device"}'
			}
		} else {
			render status: 404, data: '{"msg": "Device not found"}'
		}
	}
}

def listSubscriptions() {
	log.debug "listSubscriptions()"
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

	log.debug "addSubscription, params: ${params}, request: ${data}"
	if (!attribute) {
		render status: 400, data: '{"msg": "attributeName is required"}'
	} else {
		def device = allDevices.find { it.id == data.deviceId }
		if (device) {
			if (!state.harmonyHubs) {
				log.debug "Adding callbackUrl: $callbackUrl"
				state[device.id] = [callbackUrl: callbackUrl]
			}
			log.debug "Adding subscription"
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
		log.debug "Removing subscription for device: ${device.id}"
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
			sendToHarmony(evt, harmonyHub.callbackUrl)
		}
	} else if (deviceInfo) {
		if (deviceInfo.callbackUrl) {
			sendToHarmony(evt, deviceInfo.callbackUrl)
		} else {
			log.warn "No callbackUrl set for device: ${evt.deviceId}"
		}
	} else {
		log.warn "No subscribed device found for device: ${evt.deviceId}"
	}
}

def sendToHarmony(evt, String callbackUrl) {
	def callback = new URI(callbackUrl)
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
			log.warn "Activity callback error: ${data}"
		}
	} else {
		log.warn "Activity callback sent to non-existant dni: ${params.dni}"
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
	log.debug "Trying to delete Harmony hub with mac: ${params.mac}"
	def harmonyHub = state.harmonyHubs?.find { it.mac == params.mac }
	if (harmonyHub) {
		log.debug "Deleting Harmony hub with mac: ${params.mac}"
		state.harmonyHubs.remove(harmonyHub)
	} else {
		log.debug "Couldn't find Harmony hub with mac: ${params.mac}"
	}
	render status: 204, data: "{}"
}

private getAllDevices() {
	([] + switches + motionSensors + contactSensors + presenceSensors + temperatureSensors + accelerationSensors + waterSensors + lightSensors + humiditySensors + alarms + locks)?.findAll()?.unique { it.id }
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
