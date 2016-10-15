/**
 *  Neato (Connect)
 *
 *  Copyright 2016 Alex Lee Yuk Cheung
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
 *  VERSION HISTORY
 *	15-10-2016: 1.0c - Fix to auto SHM mode not triggering
 *	14-10-2016: 1.0b - Minor fix to preference list
 *	14-10-2016: 1.0 - Initial Version
 */
definition(
    name: "Neato (Connect)",
    namespace: "alyc100",
    author: "Alex Lee Yuk Cheung",
    description: "Integration to Neato Robotics Connected Series robot vacuums",
    category: "",
    iconUrl: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/neato_icon.png",
    iconX2Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/neato_icon.png",
    iconX3Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/neato_icon.png",
    oauth: true)

{
	appSetting "clientId"
	appSetting "clientSecret"
}


preferences {
	page(name: "auth", title: "Neato", nextPage:"", content:"authPage", uninstall: true, install:true)
    page(name: "selectDevicePAGE")
    page(name: "preferencesPAGE")
    page(name: "notificationsPAGE")
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
		description = "Click to enter Neato Credentials"
	}

	def redirectUrl = buildRedirectUrl
	log.debug "RedirectUrl = ${redirectUrl}"
	// get rid of next button until the user is actually auth'd
	if (!oauthTokenProvided) {
		return dynamicPage(name: "auth", title: "Login", nextPage: "", uninstall:uninstallAllowed) {
        	section { headerSECTION() }
			section() {
				paragraph "Tap below to log in to the Neato service and authorize SmartThings access."
				href url:redirectUrl, style:"embedded", required:true, title:"Neato", description:description
			}
		}
    } else {
		updateDevices()
        dynamicPage(name: "auth", uninstall: false, install: false) {
        	section { headerSECTION() }
			section ("Choose your Neato Botvacs:") {
					href("selectDevicePAGE", title: null, description: devicesSelected() ? "Devices:\n " + getDevicesSelectedString() : "Tap to select your Neato Botvacs", state: devicesSelected())
        		}
                section ("Preferences:") {
					href("preferencesPAGE", title: null, description: preferencesSelected() ? getPreferencesString() : "Tap to configure preferences", state: preferencesSelected())
        		}
                section ("Notifications:") {
					href("notificationsPAGE", title: null, description: notificationsSelected() ? getNotificationsString() : "Tap to configure notifications", state: notificationsSelected())
        		}
        	def botvacList = ""
    		selectedBotvacs.each() {
            	def childDevice = getChildDevice("${it}")
				try {
					botvacList += "${childDevice.displayName} is ${childDevice.currentStatus}. Battery is ${childDevice.currentBattery}%\n"
				}
        		catch (e) {
           			log.trace "Error checking status."
            		log.trace e
        		}
				if (botvacList) {
					section("Botvac Status:") {
						paragraph image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_botvac_image.png", botvacList.trim()
					}
				}  
        	}
        }
	}
}

def selectDevicePAGE() {
	updateDevices()
	dynamicPage(name: "selectDevicePAGE", title: "Devices", uninstall: false, install: false) {
    	section { headerSECTION() }
    	section() {
			paragraph "Tap below to see the list of Neato Botvacs available in your Neato account and select the ones you want to connect to SmartThings."
    		input "selectedBotvacs", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/devicetypes/alyc100/neato_botvac_image.png", required:false, title:"Select Neato Devices \n(${state.botvacDevices.size() ?: 0} found)", multiple:true, options:state.botvacDevices
        }
    }
}

def notificationsPAGE() {
	return dynamicPage(name: "notificationsPAGE", title: "Notifications", install: false, uninstall: false) {   
		section(""){
        	input("recipients", "contact", title: "Send notifications to", required: false) {
				input "sendPush", "bool", title: "Send as Push?", required: false
            }
			input "sendSMS", "phone", title: "Send as SMS?", required: false, defaultValue: null
			input "sendBotvacOn", "bool", title: "Notify when on?", required: false, defaultValue: false
			input "sendBotvacOff", "bool", title: "Notify when off?", required: false, defaultValue: false
			input "sendBotvacError", "bool", title: "Notify on error?", required: false, defaultValue: true
			input "sendBotvacBin", "bool", title: "Notify on full bin?", required: false, defaultValue: true
		}
    }
}

def preferencesPAGE() {
	return dynamicPage(name: "preferencesPAGE", title: "Preferences", install: false, uninstall: false) {   
		
        section("Force Clean"){
        	paragraph "If Botvac has been inactive for a number of days specified, then force a clean."
        	input "forceClean", "bool", title: "Force clean after elapsed time?", required: false, defaultValue: false, submitOnChange: true
            if (forceClean) {
        		input ("forceCleanDelay", "number", title: "Number of days before force clean (in days)", required: false, defaultValue: 7)
            }
        }
        section("Auto Dock") {
        	paragraph "When Botvac is paused, automatically send to base after a specified number of seconds."
			input "autoDock", "bool", title: "Auto dock Botvac after pause?", required: false, defaultValue: true, submitOnChange: true
            if (autoDock) {
            	input ("autoDockDelay", "number", title: "Auto dock delay after pause (in seconds)", required: false, defaultValue: 60)
            }
		}
		section("Auto Smart Home Monitor..."){
       	 	paragraph "If Smart Home Monitor is set to Arm(Away), auto Set Smart Home Monitor to Arm(Stay) when cleaning and reset when done. If Smart Home Monitor is Disarmed during cleaning, then this will not reactivate SHM."
			input "autoSHM", "bool", title: "Auto Set Smart Home Monitor?", required: false, defaultValue: false, submitOnChange: true
			
		}
    }
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/neato_icon.png",
                  "${textVersion()}")
} 

def oauthInitUrl() {
	log.debug "oauthInitUrl with callback: ${callbackUrl}"

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			scope: "public_profile control_robots",
			client_id: clientId(),
			state: atomicState.oauthInitState,
			redirect_uri: callbackUrl
	]

	redirect(location: "${apiEndpoint}/oauth2/authorize?${toQueryString(oauthParams)}")
}

// The toQueryString implementation simply gathers everything in the passed in map and converts them to a string joined with the "&" character.
String toQueryString(Map m) {
        return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def callback() {
	log.debug "callback()>> params: $params, params.code ${params.code}"

	def code = params.code
	def oauthState = params.state

	if (oauthState == atomicState.oauthInitState) {
		def tokenParams = [
			grant_type: "authorization_code",
			code      : code,
			client_id : clientId(),
            client_secret: clientSecret(),
			redirect_uri: callbackUrl
		]

		def tokenUrl = "https://beehive.neatocloud.com/oauth2/token?${toQueryString(tokenParams)}"

		httpPost(uri: tokenUrl) { resp ->
			atomicState.refreshToken = resp.data.refresh_token
			atomicState.authToken = resp.data.access_token
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

// Example success method
def success() {
	def message = """
        <p>Your Neato Account is now connected to SmartThings!</p>
        <p>Click 'Done' to finish setup.</p>
    """
	displayMessageAsHtml(message)
}

def fail() {
	def message = """
        <p>The connection could not be established!</p>
        <p>Click 'Done' to return to the menu.</p>
    """
	displayMessageAsHtml(message)
}

def displayMessageAsHtml(message) {
    def redirectHtml = ""
	if (redirectUrl) { redirectHtml = """<meta http-equiv="refresh" content="3; url=${redirectUrl}" />""" }

	def html = """
		<!DOCTYPE html>
		<html>
		<head>
		<meta name="viewport" content="width=640">
		<title>SmartThings & Neato connection</title>
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
						<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
						<img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
						<img src="https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/neato_icon.png" alt="neato icon" width="205" />
						${message}
				</div>
		</body>
		</html>
		"""
	render contentType: 'text/html', data: html
}

private refreshAuthToken() {
	log.debug "refreshing auth token"

	if(!atomicState.refreshToken) {
		log.warn "Can not refresh OAuth token since there is no refreshToken stored"
	} else {
		def refreshParams = [
			method: 'POST',
			uri   : "https://beehive.neatocloud.com",
			path  : "/oauth2/token",
			query : [grant_type: 'refresh_token', refresh_token: "${atomicState.refreshToken}"],
		]

		def notificationMessage = "is disconnected from SmartThings, because the access credential changed or was lost. Please go to the Neato (Connect) SmartApp and re-enter your account login credentials."
		//changed to httpPost
		try {
			def jsonMap
			httpPost(refreshParams) { resp ->
				if(resp.status == 200) {
					log.debug "Token refreshed...calling saved RestAction now!"
					debugEvent("Token refreshed ... calling saved RestAction now!")
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
	// TODO: subscribe to attributes, devices, locations, etc.
    if (selectedBotvacs)
		addBotvacs()
    unschedule()
    runEvery5Minutes('pollOn') // Asynchronously refresh devices so we don't block
    
    //subscribe to events for notifications if activated
  	if (preferencesSelected() == "complete" || notificationsSelected() == "complete") {
  		getChildDevices().each { childDevice ->
  			subscribe(childDevice, "status.cleaning", eventHandler, [filterEvents: false])
            subscribe(childDevice, "status.ready", eventHandler, [filterEvents: false])
            subscribe(childDevice, "status.error", eventHandler, [filterEvents: false])
            subscribe(childDevice, "status.paused", eventHandler, [filterEvents: false])
            subscribe(childDevice, "bin.full", eventHandler, [filterEvents: false])
  		}
  	}
}

def uninstalled() {
	log.info("Uninstalling, removing child devices...")
	unschedule()
	removeChildDevices(getChildDevices())
}


def updateDevices() {
	log.debug "Executing 'updateDevices'"
	if (!state.devices) {
		state.devices = [:]
	}
	def devices = devicesList()
    state.botvacDevices = [:]
    def selectors = []
	devices.each { device -> 
    	if (device.serial != null) {
        	selectors.add("${device.serial}|${device.secret_key}")
            def value
        	value = "Neato Botvac - " + device.name
			def key = device.serial + "|" + device.secret_key
			state.botvacDevices["${key}"] = value
      	}
	}    
    log.debug selectors
    //Remove devices if does not exist on the Neato platform
    getChildDevices().findAll { !selectors.contains("${it.deviceNetworkId}") }.each {
		log.info("Deleting ${it.deviceNetworkId}")
        try {
			deleteChildDevice(it.deviceNetworkId)
        } catch (physicalgraph.exception.NotFoundException e) {
        	log.info("Could not find ${it.deviceNetworkId}. Assuming manually deleted.")
        } catch (physicalgraph.exception.ConflictException ce) {
        	log.info("Device ${it.deviceNetworkId} in use. Please manually delete.")
        }
	}  
}

def addBotvacs() {
	log.debug "Executing 'addBotvacs'"
	updateDevices()

	selectedBotvacs.each { device ->
    	
        def childDevice = getChildDevice("${device}")
        
        if (!childDevice) { 
    		log.info("Adding Neato Botvac device ${device}: ${state.botvacDevices[device]}")
            
        	def data = [
                name: state.botvacDevices[device],
				label: state.botvacDevices[device],
			]
            childDevice = addChildDevice(app.namespace, "Neato Botvac Connected Series", "$device", null, data)
            childDevice.refresh()
           
			log.debug "Created ${state.botvacDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.botvacDevices[device]} with id ${device} already exists"
		}
		
	}
}

private removeChildDevices(devices) {
	devices.each {
		deleteChildDevice(it.deviceNetworkId) // 'it' is default
	}
}

def devicesList() {
	logErrors([]) {
		def resp = beehiveGET("/users/me/robots")
		if (resp.status == 200) {
			return resp.data
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def devicesSelected() {
	return (selectedBotvacs) ? "complete" : null
}

def getDevicesSelectedString() {
	if (state.botvacDevices == null) {
    	updateDevices()
    }
	def listString = ""
	selectedBotvacs.each { childDevice -> 
    	if (listString == "") {
        	if (null != state.botvacDevices) {
        		listString += "• " + state.botvacDevices[childDevice]
            }
        }
        else {
        	if (null != state.botvacDevices) {
        		listString += "\n• " + state.botvacDevices[childDevice]
            }
        }
    }
    return listString
}

def preferencesSelected() {
	return (forceClean || autoDock || autoSHM) ? "complete" : null
}

def getPreferencesString() {
	def listString = ""
	if (forceClean) listString += "• Force clean after ${forceCleanDelay} days\n"
  	if (autoDock) listString += "• Auto Dock after ${autoDockDelay} seconds\n"
    if (autoSHM) listString += "• Automatically set Smart Home Monitor\n"
  	
  	if (listString != "") listString = listString.substring(0, listString.length() - 1)
    return listString
}

def notificationsSelected() {
    return ((location.contactBookEnabled && recipients) || sendPush || sendSMS != null) && (sendBotvacOn || sendBotvacOff || sendBotvacError || sendBotvacBin) ? "complete" : null
}

def getNotificationsString() {
	def listString = ""
    if (location.contactBookEnabled && recipients) listString += "• Send via Contact Book\n"
    if (sendPush) listString += "• Send Push\n"
  	if (sendSMS != null) listString += "• Send SMS to ${sendSMS}\n"
  	if (sendBotvacOn) listString += "• Botvac On Notification\n"
  	if (sendBotvacOff) listString += "• Botvac Off Notification\n"
  	if (sendBotvacError) listString += "• Botvac Error Notification\n"
  	if (sendBotvacBin) listString += "• Bin Full Notification\n"
    if (listString != "") listString = listString.substring(0, listString.length() - 1)
    return listString
}

//Beehive API Access
def beehiveGET(path, body = [:]) {
	try {
        log.debug("Beginning API GET: ${beehiveURL(path)}, ${beehiveRequestHeaders()}")

        httpGet(uri: beehiveURL(path), contentType: 'application/json', headers: beehiveRequestHeaders()) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

Map beehiveRequestHeaders() {
	return [
        'Accept': 'application/vnd.neato.nucleo.v1',
        'Content-Type': 'application/*+json',
        'X-Agent': '0.11.3-142',
        'Authorization': "Bearer ${atomicState.authToken}"
    ]
}

def logResponse(response) {
	log.info("Status: ${response.status}")
	log.info("Body: ${response.data}")
}

def logErrors(options = [errorReturn: null, logObject: log], Closure c) {
	try {
		return c()
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error("got error: ${e}, body: ${e.getResponse().getData()}")
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		log.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}

// Implement event handlers
def eventHandler(evt) {
	log.debug "Executing 'eventHandler' for ${evt.displayName}"
	def msg
    if (evt.value == "paused") {
    log.trace "Setting auto dock for ${evt.displayName}"
    	//If configured, set to dock automatically after one minute.
        if (autoDock == true) {
        	runIn(autoDockDelay, scheduleAutoDock)
        }
    }
	else if (evt.value == "error") {
    	unschedule()
        runEvery5Minutes('pollOn')
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"error",descriptionText:"${evt.displayName} has an error", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} has an error"
		msg = "${evt.displayName} has an error"
		if (sendBotvacError == true) {
        	messageHandler(msg, false)
		}
     }
	 else if (evt.value == "cleaning") {
     	unschedule()
        //Increase poll interval during cleaning
        schedule("0 0/1 * * * ?", pollOn)
        if (state.lastClean == null) {
        	state.lastClean = [:]
        }
        //Record last cleaning time for device
        state.lastClean[evt.displayName] = now()
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"on",descriptionText:"${evt.displayName} is on", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} is on"
		msg = "${evt.displayName} is on"
		if (sendBotvacOn == true) {
			messageHandler(msg, false)
		}
        if (autoSHM) {
        	if (location.currentState("alarmSystemStatus")?.value == "away") {
				sendEvent(linkText:app.label, name:"Smart Home Monitor", value:"stay",descriptionText:"Smart Home Monitor was set to stay", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "Smart Home Monitor is set to stay"
				sendLocationEvent(name: "alarmSystemStatus", value: "stay")
				state.autoSHMchange = "y"
                messageHandler("Smart Home Monitor is set to stay as ${evt.displayName} is on", true)
            }
        }
     }
	 else if (evt.value == "full") {
     	unschedule()
        runEvery5Minutes('pollOn')
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"bin full",descriptionText:"${evt.displayName} bin is full", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} bin is full"
		msg = "${evt.displayName} bin is full"
		if (sendBotvacBin == true) {
			messageHandler(msg, false)
		}
	 }
     else if (evt.value == "ready") {
     	unschedule()
        runEvery5Minutes('pollOn')
		sendEvent(linkText:app.label, name:"${evt.displayName}", value:"off",descriptionText:"${evt.displayName} is off", eventType:"SOLUTION_EVENT", displayed: true)
		log.trace "${evt.displayName} is off"
		msg = "${evt.displayName} is off"
		if (sendBotvacOff == true) {
			messageHandler(msg, false)
		}
	}
}

def scheduleAutoDock() {
	getChildDevices().each { childDevice ->
		if (childDevice.latestState('status').stringValue == 'paused') {
			childDevice.dock()
		}
	}
}

def pollOn() {
	log.debug "Executing 'pollOn'"
    
    def activeCleaners = false
    
	getChildDevices().each { childDevice ->
    	state.pollState = now()
		childDevice.poll()
        //Force on if last clean was a long time ago
        if (childDevice.currentSwitch == "off" && forceClean && state.lastClean != null && state.lastClean[childDevice.displayName] != null) {
        	def t = now() - state.lastClean[childDevice.displayName]
            log.debug "$childDevice.displayName last cleaned at " + state.lastClean[childDevice.displayName] + ". $t milliseconds has elapsed since."
			if (t > (forceCleanDelay * 86400000)) {
            	log.debug "Force clean activated as $t milliseconds has elapsed"
				messageHandler(childDevice.displayName + " has not cleaned for " + forceCleanDelay + " days. Forcing a clean.", true)
                childDevice.on()
        	}
        }
        //Search for active cleaners
        if (childDevice.latestState('status').stringValue == 'cleaning') {
        	activeCleaners = true
        }
	}
    
	if (!activeCleaners) {
		if (autoSHM) {
			if (location.currentState("alarmSystemStatus")?.value == "stay" && state.autoSHMchange == "y"){
				sendEvent(linkText:app.label, name:"Smart Home Monitor", value:"away",descriptionText:"Smart Home Monitor was set back to away", eventType:"SOLUTION_EVENT", displayed: true)
				log.trace "Smart Home Monitor is set back to away"
				sendLocationEvent(name: "alarmSystemStatus", value: "away")
				state.autoSHMchange = "n"
                messageHandler("Smart Home Monitor is set to away as all cleaners are off", true)
			}
		}
	}
    
    //If SHM is disarmed because of external event, then disable auto SHM mode
    if (location.currentState("alarmSystemStatus")?.value == "off") {
    	state.autoSHMchange = "n"
    }
}

def messageHandler(msg, forceFlag) {
	if (settings.sendSMS != null && !forceFlag) {
		sendSms(sendSMS, msg) 
	}
    if (location.contactBookEnabled && recipients) {
    	sendNotificationToContacts(msg, recipients)
    } else if (settings.sendPush == true || forceFlag) {
		sendPush(msg)
	}
    
}


def getChildName()           { return "Neato BotVac" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getShardUrl()            { return getApiServerUrl() }
def getCallbackUrl()         { return "https://graph.api.smartthings.com/oauth/callback" }
def getBuildRedirectUrl()    { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }
def getApiEndpoint()         { return "https://apps.neatorobotics.com" }
def getSmartThingsClientId() { return appSettings.clientId }
def beehiveURL(path = '/') 			 { return "https://beehive.neatocloud.com${path}" }
private def textVersion() {
    def text = "Neato (Connect)\nVersion: 1.0c\nDate: 15102016(1330)"
}

private def textCopyright() {
    def text = "Copyright © 2016 Alex Lee Yuk Cheung"
}

def clientId() {
	if(!appSettings.clientId) {
		return "3ba64237d07f43e2e6ecff97de60916b73c4b06df71e9ad35ec02d7b3b513881"
	} else {
		return appSettings.clientId
	}
}

def clientSecret() {
	if(!appSettings.clientSecret) {
		return "e7fd560dab04efdd38488f918a2a8b0c097157d765e19003360fc458f5119bde"
	} else {
		return appSettings.clientSecret
	}
}