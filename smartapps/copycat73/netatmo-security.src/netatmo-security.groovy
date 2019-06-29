/*
 *  Netatmo Security
 *
 *  Copyright 2018 Nick Veenstra
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
 */

 
import java.text.DecimalFormat
import groovy.json.JsonSlurper

private getApiUrl()			{ "https://api.netatmo.com" }
private getVendorName()		{ "netatmo" }
private getVendorAuthPath()	{ "${apiUrl}/oauth2/authorize?" }
private getVendorTokenPath(){ "${apiUrl}/oauth2/token" }
private getVendorIcon()		{ "https://s3.amazonaws.com/smartapp-icons/Partner/netamo-icon-1%402x.png" }
private getScope()			{ "read_camera read_presence write_camera" }
private getClientId()		{ appSettings.clientId }
private getClientSecret()	{ appSettings.clientSecret }
private getServerUrl() 		{ appSettings.serverUrl }
private getShardUrl()		{ return getApiServerUrl() }
private getCallbackUrl()	{ "${serverUrl}/oauth/callback" }
private getBuildRedirectUrl() { "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${shardUrl}" }
private getWebhookUrl()		{ "${serverUrl}/api/smartapps/installations/${app.id}/webhook?access_token=${state.accessToken}".encodeAsURL() }
private getAddWebhookPath() { "${getApiUrl()}/api/addwebhook?access_token=${state.netatmoAccessToken.encodeAsURL()}&url=${getWebhookUrl()}&app_type=app_security" }
private getDropWebhookPath() { "${getApiUrl()}/api/dropwebhook?access_token=${state.netatmoAccessToken.encodeAsURL()}&app_type=app_security" }

// Automatically generated. Make future change here.
definition(
	name: "Netatmo Security",
	namespace: "CopyCat73",
	author: "Brian Steere,cscheiene, Nick Veenstra",
	description: "Netatmo Integration",
	category: "SmartThings Labs",
	iconUrl: "https://github.com/CopyCat73/SmartThings-Dev/raw/master/NetatmoSecurity.png",
	iconX2Url: "https://github.com/CopyCat73/SmartThings-Dev/raw/master/NetatmoSecurity@2x.png",
	oauth: true,
	singleInstance: true
){
	appSetting "clientId"
	appSetting "clientSecret"
	appSetting "serverUrl"
}

preferences {
	page(name: "Credentials", title: "Fetch OAuth2 Credentials", content: "authPage", install: false)
	page(name: "Settings", title: "Netatmo security settings", content: "Settings", install: false)
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
    path("/webhook") {action: [GET: "webhook", POST: "webhook"]}    
}

private showAlert(text,name,value) {
	if (state.initialSetup) {
    	return
    }
    sendEvent(
        descriptionText: text,
        eventType: "ALERT",
        name: name,
        value: value,
        displayed: true,
    )
    log.debug(text)
}

def authPage() {
	//log.debug "In authPage"

	def description
	def uninstallAllowed = false
	def oauthTokenProvided = false
    if (!state.initialSetup) {
    	state.initialSetup = true
    	state.webhookInstalled = false
    }

	if (!state.accessToken) {
		//log.debug "About to create access token."
		state.accessToken = createAccessToken()
	}

	if (canInstallLabs()) {

		def redirectUrl = getBuildRedirectUrl()
		// log.debug "Redirect url = ${redirectUrl}"

		if (state.authToken) {
			description = "Tap 'Next' to proceed"
			uninstallAllowed = true
			oauthTokenProvided = true
		} else {
			description = "Click to enter Credentials."
		}

		if (!oauthTokenProvided) {
			//log.debug "Showing the login page"
			return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage:"Settings", uninstall: uninstallAllowed, install:false) {
				section() {
					paragraph "Tap below to log in to Netatmo and authorize SmartThings access."
					href url:redirectUrl, style:"embedded", required:false, title:"Connect to ${getVendorName()}:", description:description
				}
			}
		} else {
			//log.debug "Showing the devices page"
			return dynamicPage(name: "Credentials", title: "Connected", nextPage:"Settings", uninstall: uninstallAllowed, install:false) {
				section() {
					input(name:"Devices", style:"embedded", required:false, title:"Netatmo is now connected to SmartThings!", description:description) 
				}
			}
		}
	} else {
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

	state.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
		response_type: "code",
		client_id: getClientId(),
		client_secret: getClientSecret(),
		state: state.oauthInitState,
		redirect_uri: getCallbackUrl(),
		scope: getScope()
	]

	// log.debug "REDIRECT URL: ${getVendorAuthPath() + toQueryString(oauthParams)}"

	redirect (location: getVendorAuthPath() + toQueryString(oauthParams))
}

def callback() {
	//log.debug "callback()>> params: $params, params.code ${params.code}"

	def code = params.code
	def oauthState = params.state

	if (oauthState == state.oauthInitState) {

		def tokenParams = [
			client_secret: getClientSecret(),
			client_id : getClientId(),
			grant_type: "authorization_code",
			redirect_uri: getCallbackUrl(),
			code: code,
			scope: getScope()
		]

		//log.debug "TOKEN URL: ${getVendorTokenPath() + toQueryString(tokenParams)}"

		def tokenUrl = getVendorTokenPath()
		def params = [
			uri: tokenUrl,
			contentType: 'application/x-www-form-urlencoded',
			body: tokenParams
		]

		// log.debug "PARAMS: ${params}"

		try {
            httpPost(params) { resp ->

                def slurper = new JsonSlurper()

                resp.data.each { key, value ->
                    def data = slurper.parseText(key)

                    state.refreshToken = data.refresh_token
                    state.authToken = data.access_token
                    state.tokenExpires = now() + (data.expires_in * 1000)
                    // log.debug "swapped token: $resp.data"
                }
            }
        }
        catch (e) {
        	log.debug "Error in token handling $e.response.data}"
        	//showAlert("Error retrieving token","token","token") 
		}
        //Error in token handling [{"error":"invalid_request"}:null]} dd72fcfc-eadb-433a-a77f-3f13820d6962  4:38:32 PM: debug TOKEN URL: https://api.netatmo.com/oauth2/tokenclient_id=5a4f56358c04c4157f8b483b&client_secret=Pc1NPnYxPbmr5OrSOMF6XuBZMDn6ExRZDx7YD7eqo4&code=ed4961cdbbf58d9668f4cd907c8e6106&grant_type=authorization_code&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2F%2Foauth%2Fcallback&scope=read_camera+read_presence
		// Handle success and failure here, and render stuff accordingly
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
	log.debug "OAuth flow succeeded"
	def message = """
	<p>We have located your """ + getVendorName() + """ account.</p>
	<p>Tap 'Done' to continue to Devices.</p>
	"""
	connectionStatus(message)
}

def fail() {
	log.debug "OAuth flow failed"
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
				${message}
			</div>
        </body>
        </html>
	"""
	render contentType: 'text/html', data: html
}

def refreshToken() {
	log.debug "In refreshToken"

	def oauthParams = [
		client_secret: getClientSecret(),
		client_id: getClientId(),
		grant_type: "refresh_token",
		refresh_token: state.refreshToken
	]

	def tokenUrl = getVendorTokenPath()
	def params = [
		uri: tokenUrl,
		contentType: 'application/x-www-form-urlencoded',
		body: oauthParams,
	]
    //log.debug "refresh token params: $params"
	//log.debug "TokenUrl: $tokenUrl"
	// OAuth Step 2: Request access token with our client Secret and OAuth "Code"
	try {
		httpPost(params) { response ->
			def slurper = new JsonSlurper();

			response.data.each {key, value ->
				def data = slurper.parseText(key);
				state.refreshToken = data.refresh_token
				state.netatmoAccessToken = data.access_token
				state.tokenExpires = now() + (data.expires_in * 1000)
				log.debug "refresh token success"
			}
		}
    } catch (Exception e) {
		log.debug ("refreshtoken Error",e)
        return false
	}

	// We didn't get an access token
	if ( !state.netatmoAccessToken ) {
		return false
	}

}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	state.initialSetup = false
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
   
    if (installWebhook && !state.webhookInstalled) {
		addWebhook()
        state.webhookInstalled = true
	}
    else if (!installWebhook && state.webhookInstalled) {
    	dropWebhook()
        state.webhookInstalled = false
    }

	initialize()
}

def parse () {
	log.debug "parse hier"
}

def initialize() {
	log.debug "Initialized with settings: ${settings}"

	// Pull the latest device info into state
	getDeviceAndPersonList()
 	
  	settings.devices.each {
		def deviceId = it
		def detail = state?.deviceDetail[deviceId]

		try {
			switch(detail?.type) {
				case 'NOC':
					log.debug "Creating Presence camera ${detail.name}"
					createChildDevice("Netatmo Presence", deviceId, deviceId, detail.name, detail.homeID, detail.homeName)
  					break
				case 'NACamera':
					log.debug "Creating Welcome camera ${detail.name}"
					createChildDevice("Netatmo Welcome", deviceId, deviceId, detail.name, detail.homeID, detail.homeName)
  					break
			}
		} catch (Exception e) {
			log.error "Error creating device: ${e}"
		}
	}
    
    settings.people.each {
		def personId = it
       	def detail = state?.personDetail[personId]
        
        try {
				log.debug "Creating Person ${detail.name}"
				createChildDevice("Netatmo Person", personId, personId, detail.name, detail.homeID, detail.homeName)
		} 
        catch (Exception e) {
			log.error "Error creating device: ${e}"
		}
	}
    
	// Cleanup any other devices that need to go away
    
    def allInstalled = [settings.devices, settings.people].findAll().join()
    if (allInstalled.size()>0) {
    	def delete = getChildDevices().findAll { !allInstalled.contains(it.deviceNetworkId) }
		log.debug "Delete: $delete"
		delete.each { deleteChildDevice(it.deviceNetworkId) }
	}
    else {
    	removeChildDevices(getChildDevices())
        log.debug "Removing all devices"
    }
	
    // Do the initial poll
	poll()
	// Schedule it to run every 5 minutes
	runEvery5Minutes("poll")

}

def uninstalled() {
	log.debug "In uninstalled"

	removeChildDevices(getChildDevices())
    if (state.webhookInstalled) {
    	dropWebhook()
    }
}

def getDeviceAndPersonList() {
	log.debug "Refreshing camera and person data"
    def deviceList = [:]
    def personList = [:]
    def combinedList = [:]
    state.deviceDetail = [:]
    state.deviceState = [:]
    
    state.personDetail = [:]
  
    apiGet("/api/gethomedata",[:]) { resp ->
    		if (resp) {
                state.response = resp.data.body
                resp.data.body.homes.each { home ->
                    //log.debug("home $home.id") 
                    home.cameras.each { camera ->
                        //log.debug("camera $camera")
                        def key = camera.id //.replace(':','').toUpperCase()
                        def cameraName = "${camera.name} (${home.name})"
                        deviceList[key] = "${camera.name} (${home.name})"
                        state.deviceDetail[key] =  ["name" : cameraName]
                        state.deviceDetail[key] << ["type" : camera.type]
                        state.deviceDetail[key] << ["homeID" : home.id]
                        state.deviceDetail[key] << ["homeName" : home.name]
                        state.deviceState[key] = ["sd_status": camera.sd_status]
                        state.deviceState[key] << ["status": camera.status]
                        state.deviceState[key] << ["alim_status": camera.alim_status]
                   	}
                    home.persons.each { person ->
                    	if (person.pseudo) {
                            def key = person.id //.replace(':','').toUpperCase()
                            def personName = person.pseudo
                            personList[key] = personName
                            state.personDetail[key] =  ["name" : personName]
							state.personDetail[key] << ["homeID" : home.id]
                            state.personDetail[key] << ["homeName" : home.name]
                            state.personDetail[key] <<  ["last_seen" : person.last_seen]
                            state.personDetail[key] << ["out_of_sight" : person.out_of_sight]
                        } 
                   	}                    
                }
           }
     }
	combinedList[0] = deviceList.sort() { it.value.toLowerCase() }
    combinedList[1] = personList.sort() { it.value.toLowerCase() }
    return combinedList

}

private removeChildDevices(delete) {
	log.debug "In removeChildDevices"

	log.debug "deleting ${delete.size()} devices"

	delete.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def createChildDevice(deviceFile, dni, name, label, homeID, homeName) {
	log.debug "In createChildDevice"
    
    def hub = location.hubs[0]

	try {
		def existingDevice = getChildDevice(dni)
		if(!existingDevice) {
			log.debug "Creating child"
			def childDevice = addChildDevice("copycat73", deviceFile, dni, hub.id, [name: name, label: label, completedSetup: true])
            childDevice.setHome(homeID,homeName)
		} else {
			log.debug "Device $dni already exists"
		}
	} catch (e) {
		log.error "Error creating device: ${e}"
	}
}

def Settings() {
	log.debug "Listing devices"
	
    def tmpList = getDeviceAndPersonList()
	def devices = tmpList[0]
    def persons = tmpList[1]

	dynamicPage(name: "Settings", title: "Settings", install: true) {
		section("Devices") {
			input "devices", "enum", title: "Select camera(s) to track", required: false, multiple: true, options: devices
		}
		section("People") {
			input "people", "enum", title: "Select person(s) to track (creates presence device)", required: false, multiple: true, options: persons 
    	}
        section("Preferences") {
        	input "installWebhook", "bool", title: "Activate webhook (enables motion detect and person tracking)", description: "", required: true
        }
	}
}

def webhook() {
	
    
    /*
    
    11:49:09 PM: debug [message:name seen by Achterkamer, event_type:person, app_type:app_camera, event_id:*****, camera_id:***mac, home_name:Thuis, user_id:****, persons:[[id:****, face_key:****, is_known:true, face_id:****]], home_id:****]
    11:49:04 PM: debug [message:name seen by Voorkamer, event_type:person, app_type:app_camera, event_id:*****, camera_id:70:***mac, home_name:Thuis, user_id:*****, persons:[[id:*****, face_key:****, is_known:true, face_id:****]], home_id:****]
   	11:48:25 PM: debug [message:Person seen by Achterdeur, snapshot_key:****, event_type:human, app_type:app_camera, event_id:*****, camera_id:***mac, home_name:Thuis, user_id:****, snapshot_id:***, home_id:****]
    11:48:13 PM: debug [message:Motion detected by Achterkamer, snapshot_key:*******, event_type:movement, app_type:app_camera, event_id:***, camera_id:***mac, home_name:Thuis, user_id:****, snapshot_id:****, home_id:****]
    message:Person seen by Voordeur, snapshot_key:d***, event_type:human, app_type:app_camera, event_id:**, camera_id:****, home_name:Thuis, user_id:**, 
    Car seen by Voordeur, snapshot_key:** event_type:vehicle, app_type:app_camera, event_id:***, camera_id:7***, home_name:Thuis, user_id:***, snapshot_id:***, home_id:***
    message:Voordeur: monitoring suspended, event_type:off, app_type:app_camera, event_id:***, camera_id:***, home_name:Thuis, user_id:**, home_id:**]
    */
    def jsonSlurper = new groovy.json.JsonSlurper()
	def messageJSON = request.JSON
    log.debug "$messageJSON"
    log.debug "Webhook message: ${messageJSON.message}"
    
    def children = getChildDevices()
    
    def cameraID = messageJSON.camera_id
    def child = children?.find { it.name == cameraID }
    if (!child) {
    	log.debug "Cant deliver event: no child device active for camera $cameraID"
    }
    else {
        switch (messageJSON.event_type) {
            case 'on':
                log.debug "Camera switched on"
                child?.on()
                break
            case 'off':
                log.debug "Camera switched off"
                child?.off()
                break                
			case 'person':
                log.debug "Person detected (welcome)"
                child?.seen()
                break
            case 'human':
                log.debug "Human detected (presence)"
                 child?.human()
                 break
            case 'vehicle':
                log.debug "Car detected (presence)"
                 child?.vehicle()
                 break
            case 'animal':
                log.debug "Animal detected (presence)"
                 child?.animal()
                 break
            case 'movement':
                log.debug "Movement detected sending event to $child"
                child?.motion()
                break
            default:
                log.debug "Unhandled message"
                break
        }
    }
 
    def resp = []
    resp << [name: "result" , value: "ok"]
 
    return resp
}

def setAway(homeID, personID) {
	log.debug "setting away ${homeID} ${personID}"
    
    def query = ["home_id": homeID] 
    if (personID) {
    	query <<["person_id": personID]
    }
  
    apiGet("/api/setpersonsaway",query) { resp ->
    		if (resp) {
            	if (resp.data.status == "ok") {
                	log.debug "Away command succesful"
            	}
            }
            
	}
}

def setAway(homeID) {
	log.debug "setting away ${homeID}"
    setAway(homeID, null)
}

def addWebhook() {
	
    if (!state.accessToken) {
    	createAccessToken() // create our own OAUTH access token to use in webhook url
    }

	def query = getAddWebhookPath()
  	log.debug "$query"

	try {
		httpGet(query)	{ response ->
        	if (response.data.status == "ok") {
				log.debug "Webhook added succesfully"
            }
		}
	}
    catch (groovyx.net.http.HttpResponseException e) {
    	def status = e.response.status
        log.debug "addwebhook error $status"
        showAlert("Add webhook error: $status\n" + e.response.data.error.message,"error","netatmo")
    }
}


def dropWebhook() {

	def query = getDropWebhookPath()
  	log.debug "$query"

	try {
		httpGet(query)	{ response ->
        	if (response.data.status == "ok") {
				log.debug "Webhook dropped succesfully"
            }
		}
	}
    catch (groovyx.net.http.HttpResponseException e) {
    	def status = e.response.status
        log.debug "dropwebhook error $status"
        showAlert("Drop webhook error: $status\n" + e.response.data.error.message,"error","netatmo")
    }
}



def apiGet(String path, Map query, Closure callback) {
	
    if (!state.netatmoAccessToken || now() >= state.tokenExpires) {
    	refreshToken()
    }
    query['access_token'] = state.netatmoAccessToken
	def params = [
		uri: getApiUrl(),
		path: path,
		'query': query
	]
	log.debug "API Get: $params"

	try {
		httpGet(params)	{ response ->
        	log.debug "apiGet response status $response.status"
			callback.call(response)
		}
	}
    catch (groovyx.net.http.HttpResponseException e) {
    	def status = e.response.status
        if (status == 403) {
        	// token has probably expired, trying refresh before reporting error
            if(refreshToken()) 
            {
            	try {
                    httpGet(params)	{ response ->
                        callback.call(response)
                    }
                }
                catch (groovyx.net.http.HttpResponseException ex) {
                	//still no succes..
                    callback.call(response)
                }
            }
            else {
        		log.debug "Token refresh failed"
                callback.call(response)
        	}
      	}
        showAlert("Netatmo api error: $status\n" + e.response.data.error.message,"error","netatmo")
        callback.call(response)
    	
	}

}

def apiGet(String path, Closure callback) {
	apiGet(path, [:], callback);
}

def poll() {
	log.debug "Polling"
	getDeviceAndPersonList()
 	def children = getChildDevices()
    
    if(location.timeZone == null) {
       log.warn "Location is not set! Go to your ST app and set your location"
    }
	
	settings.devices.each { deviceId ->
		def data = state?.deviceState[deviceId]
        if (data) {
            def child = children?.find { it.deviceNetworkId == deviceId }
            child?.sendEvent(name: 'switch', value: data['status'], unit: "")
        }
        else {
			log.debug "poll error for switch $deviceId"
		}
    }

 	settings.people.each { personId ->
		def detail = state?.personDetail[personId]
    	if (detail) {
            def child = children?.find { it.deviceNetworkId == personId }
            if (detail['out_of_sight']) {
                child?.away()
            }
            else {
                //log.debug "device $child"
                child?.seen()
            }
    	}
        else {
        	log.debug "poll error for person $personId"
        }
    }   
    
}

def lastUpdated(time) {
    if(settings.time == '24') {
    def updtTime = new Date(time*1000L).format("HH:mm", location.timeZone)
    state.lastUpdated = updtTime
    return updtTime
    } else {
    def updtTime = new Date(time*1000L).format("h:mm aa", location.timeZone)
    state.lastUpdated = updtTime
    return updtTime
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