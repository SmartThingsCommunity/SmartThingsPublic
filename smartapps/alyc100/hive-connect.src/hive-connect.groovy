/**
 *  Hive (Connect)
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
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
 *  24.02.2016
 *  v2.0 BETA - New Hive Connect App
 *  v2.0.1 BETA - Fix bug for accounts that do not have capabilities attribute against thermostat nodes.
 *	v2.1 - Improved authentication process and overhaul to UI. Added notification capability.
 *  v2.1.1 - Bug fix when initially selecting devices for the first time.
 *	v2.1.2 - Move external icon references into Github
 *
 */
definition(
		name: "Hive (Connect)",
		namespace: "alyc100",
		author: "Alex Lee Yuk Cheung",
		description: "Connect your Hive devices to SmartThings.",
		iconUrl: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/10457773_334250273417145_3395772416845089626_n.png",
		iconX2Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/10457773_334250273417145_3395772416845089626_n.png",
        singleInstance: true
) 

preferences {
	page(name:"firstPage", title:"Hive Device Setup", content:"firstPage", install: true)
    page(name: "loginPAGE")
    page(name: "selectDevicePAGE")
	page(name: "preferencesPAGE")
}

def apiURL(path = '/') 			 { return "https://api.prod.bgchprod.info:443/omnia${path}" }

def firstPage() {
	log.debug "firstPage"
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter Hive crednentials", state: authenticated())
  			}
    	}
    }
    else
    {
    	log.debug "next phase"
        return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
            	headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter Hive credentials", state: authenticated())
            }
            if (stateTokenPresent()) {           	
                section ("Choose your devices:") {
					href("selectDevicePAGE", title: null, description: devicesSelected() ? "Devices: " + getDevicesSelectedString() : "Tap to select devices", state: devicesSelected())
        		}
                section ("Notifications:") {
					href("preferencesPAGE", title: null, description: preferencesSelected() ? getPreferencesString() : "Tap to configure notifications", state: preferencesSelected())
        		}
            } else {
            	section {
            		paragraph "There was a problem connecting to Hive. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
           }
    	}
    }
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/10457773_334250273417145_3395772416845089626_n.png",
                  "Hive (Connect)\nVersion: 2.1.2\nDate: 16032016(1130)")
}

def stateTokenPresent() {
	return state.hiveAccessToken != null && state.hiveAccessToken != ''
}

def authenticated() {
	return (state.hiveAccessToken != null && state.hiveAccessToken != '') ? "complete" : null
}

def devicesSelected() {
	return (selectedHeating || selectedHotWater) ? "complete" : null
}

def preferencesSelected() {
	return (sendPush || sendSMS != null) && (maxtemp != null || mintemp != null || sendBoost || sendOff || sendManual || sendSchedule) ? "complete" : null
}

def getDevicesSelectedString() {
	def listString = ""
	selectedHeating.each { childDevice -> 
    	if (listString == "") {
        	listString += state.hiveHeatingDevices[childDevice]
        }
        else {
        	listString += "\n" + state.hiveHeatingDevices[childDevice]
        }
    }
    selectedHotWater.each { childDevice -> 
    	if (listString == "") {
        	listString += state.hiveHotWaterDevices[childDevice]
        }
        else {
        	listString += "\n" + state.hiveHotWaterDevices[childDevice]
        }
    }
    return listString
}

def getPreferencesString() {
	def listString = ""
    if (sendPush) listString += "Send Push, "
    if (sendSMS != null) listString += "Send SMS, "
    if (maxtemp != null) listString += "Max Temp: ${maxtemp}, "
    if (mintemp != null) listString += "Min Temp: ${mintemp}, "
    if (sendBoost) listString += "Boost, "
    if (sendOff) listString += "Off, "
    if (sendManual) listString += "Manual, "
    if (sendSchedule) listString += "Schedule, "
    if (listString != "") listString = listString.substring(0, listString.length() - 2)
    return listString
   
    
}

def loginPAGE() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your Hive credentials below to enable SmartThings and Hive integration." }
    		section("Hive Credentials:") {
				input("username", "text", title: "Username", description: "Your Hive username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Hive password", required: true, submitOnChange: true)		
			}    	
    	}
    }
    else {
    	getHiveAccessToken()
        dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your Hive credentials below to enable SmartThings and Hive integration." }
    		section("Hive Credentials:") {
				input("username", "text", title: "Username", description: "Your Hive username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Hive password", required: true, submitOnChange: true)		
			}    	
    	
    		if (stateTokenPresent()) {
        		section {
                	paragraph "You have successfully connected to Hive. Click 'Done' to select your Hive devices."
  				}
        	}
        	else {
        		section {
            		paragraph "There was a problem connecting to Hive. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
        	}
        }
    }
}

def selectDevicePAGE() {
	updateDevices()
	dynamicPage(name: "selectDevicePAGE", title: "Devices", uninstall: false, install: false) {
    	section { headerSECTION() }
    	section("Select your devices:") {
			input "selectedHeating", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/thermostat-frame-6c75d5394d102f52cb8cf73704855446.png", required:false, title:"Select Hive Heating Devices \n(${state.hiveHeatingDevices.size() ?: 0} found)", multiple:true, options:state.hiveHeatingDevices
			input "selectedHotWater", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/thermostat-frame-6c75d5394d102f52cb8cf73704855446.png", required:false, title:"Select Hive Hot Water Devices \n(${state.hiveHotWaterDevices.size() ?: 0} found)", multiple:true, options:state.hiveHotWaterDevices
					
		}
    }
}

def preferencesPAGE() {
	dynamicPage(name: "preferencesPAGE", title: "Preferences", uninstall: false, install: false) {
    	section {
    		input "sendPush", "bool", title: "Send as Push?", required: false, defaultValue: false
			input "sendSMS", "phone", title: "Send as SMS?", required: false, defaultValue: null	
        }
    	section("Thermostat Notifications:") {
			
			input "sendBoost", "bool", title: "Notify when mode is Boosting?", required: false, defaultValue: false
			input "sendOff", "bool", title: "Notify when mode is Off?", required: false, defaultValue: false
			input "sendManual", "bool", title: "Notify when mode is Manual?", required: false, defaultValue: false     
            input "sendSchedule", "bool", title: "Notify when mode is Schedule?", required: false, defaultValue: false  
		}
        section("Thermostat Max Temperature") {
        	input ("maxtemp", "number", title: "Alert when temperature is above this value", required: false, defaultValue: 25)
        }
        section("Thermostat Min Temperature") {
        	input ("mintemp", "number", title: "Alert when temperature is below this value", required: false, defaultValue: 10)
        }
        
    }
}



// App lifecycle hooks

def installed() {
	log.debug "installed"
	initialize()
	// Check for new devices every 3 hours
	runEvery3Hours('updateDevices')
    // execute handlerMethod every 10 minutes.
    runEvery10Minutes('refreshDevices')
}

// called after settings are changed
def updated() {
	log.debug "updated"
    unsubscribe()
	initialize()
    unschedule('refreshDevices')
    runEvery10Minutes('refreshDevices')
}

def uninstalled() {
	log.info("Uninstalling, removing child devices...")
	unschedule()
	removeChildDevices(getChildDevices())
}

private removeChildDevices(devices) {
	devices.each {
		deleteChildDevice(it.deviceNetworkId) // 'it' is default
	}
}

// called after Done is hit after selecting a Location
def initialize() {
	log.debug "initialize"
    if (selectedHeating)
		addHeating()

	if (selectedHotWater)
		addHotWater()

    runIn(10, 'refreshDevices') // Asynchronously refresh devices so we don't block
    
    //subscribe to events for notifications if activated
    if (preferencesSelected() == "complete") {
    	getChildDevices().each { childDevice -> 
    		if (childDevice.typeName == "Hive Heating V2.0" || childDevice.typeName == "Hive Hot Water V2.0") {
    			subscribe(childDevice, "thermostatMode", modeHandler)
        	}
        	if (childDevice.typeName == "Hive Heating V2.0") {
        		subscribe(childDevice, "temperature", tempHandler)
        	}
    	}
    }
    state.maxNotificationSent = false
    state.minNotificationSent = false
    
}

def tempHandler(evt) {
	def msg
    log.trace "temperature: $evt.value, $evt"
    
    if (settings.maxtemp != null) {
    	def maxTemp = settings.maxtemp
        if (evt.doubleValue >= maxTemp) {
        	msg = "${evt.displayName} temperature reading is very hot."
            if (state.maxNotificationSent == null || state.maxNotificationSent == false) {
            	generateNotification(msg)
                //Avoid constant messages
            	state.maxNotificationSent = true
            }
        }
        else {
        	//Reset if temperature falls back to normal levels
            state.maxNotificationSent = false
        }
    }
    else if (settings.mintemp != null) {
    	def minTemp = settings.mintemp
        if (evt.doubleValue <= minTemp) {
        	msg = "${evt.displayName} temperature reading is very cold."
            if (state.minNotificationSent == null || state.minNotificationSent == false) {
            	generateNotification(msg)
                //Avoid constant messages
            	state.minNotificationSent = true
            }
        }
        else {
        	//Reset if temperature falls back to normal levels
        	state.minNotificationSent = false
        }
    }
}

def modeHandler(evt) {
	def msg
    if (evt.value == "heat") { 
    	msg = "${evt.displayName} is set to Manual"
        if (settings.sendSchedule) generateNotification(msg)
    }
	else if (evt.value == "off") {
    	msg = "${evt.displayName} is turned Off"
        if (settings.sendOff) generateNotification(msg)
    }
    else if (evt.value == "auto") {
    	msg = "${evt.displayName} is set to Schedule"
        if (settings.sendManual) generateNotification(msg)
    }
    else if (evt.value == "emergency heat") {
    	msg = "${evt.displayName} is in Boost mode" 
        if (settings.sendBoost) generateNotification(msg)
    }   
}

def generateNotification(msg) {
	if (settings.sendSMS != null) {
		sendSms(sendSMS, msg) 
	}	
	if (settings.sendPush == true) {
		sendPush(msg)
	}
}

def updateDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	def devices = devicesList()
    state.hiveHeatingDevices = [:]
    state.hiveHotWaterDevices = [:]
    def selectors = []
	devices.each { device ->  	
    	selectors.add("${device.id}")
    	if (device.attributes.activeHeatCoolMode != null) {
        	def parentNode = devices.find { d -> d.id == device.parentNodeId }
            if ((device.attributes.supportsHotWater != null) && (device.attributes.supportsHotWater.reportedValue == false) && (device.attributes.temperature != null)) {
            	def value = "${parentNode.name} Hive Heating"
				def key = device.id
				state.hiveHeatingDevices["${key}"] = value
                
                //Update names of devices with Hive
         		def childDevice = getChildDevice("${device.id}")
         		if (childDevice) { 
         			//Update name of device if different.
         			if(childDevice.name != parentNode.name + " Hive Heating") {
 						childDevice.name = parentNode.name + " Hive Heating"
 						log.debug "Device's name has changed."
 					}
         		}
                
            }
            else if ((device.attributes.supportsHotWater != null) && (device.attributes.supportsHotWater.reportedValue == true)) {
            	def value = "${parentNode.name} Hive Hot Water"
				def key = device.id
				state.hiveHotWaterDevices["${key}"] = value
                
                //Update names of devices
         		def childDevice = getChildDevice("${device.id}")
         		if (childDevice) { 
         			//Update name of device if different.
         			if(childDevice.name != parentNode.name + " Hive Hot Water") {
 						childDevice.name = parentNode.name + " Hive Hot Water"
 						log.debug "Device's name has changed."
 					}
         		}
                
            }
            // Support for more Hive Device Types can be added here in the future.
        }
    }    
    
    //Remove devices if does not exist on the Hive platform
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

def addHeating() {
	updateDevices()

	selectedHeating.each { device ->
    	
        def childDevice = getChildDevice("${device}")
        
        if (!childDevice) { 
    		log.info("Adding Hive Heating device ${device}: ${state.hiveHeatingDevices[device]}")
            
        	def data = [
                name: state.hiveHeatingDevices[device],
				label: state.hiveHeatingDevices[device],
			]
            childDevice = addChildDevice(app.namespace, "Hive Heating V2.0", "$device", null, data)
            childDevice.refresh()
           
			log.debug "Created ${state.hiveHeatingDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.hiveHeatingDevices[device]} with id ${device} already exists"
		}
		
	}
}

def addHotWater() {
	updateDevices()

	selectedHotWater.each { device ->
    	
        def childDevice = getChildDevice("${device}")
        
        if (!childDevice) { 
    		log.info("Adding Hive Hot Water device ${device}: ${state.hiveHotWaterDevices[device]}")
            
        	def data = [
                name: state.hiveHotWaterDevices[device],
				label: state.hiveHotWaterDevices[device],
			]
            childDevice = addChildDevice(app.namespace, "Hive Hot Water V2.0", "$device", null, data)
            childDevice.refresh()
			log.debug "Created ${state.hiveHotWaterDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.hiveHotWaterDevices[device]} with id ${device} already exists"
		}
		
	}
}

def refreshDevices() {
	log.info("Refreshing all devices...")
	getChildDevices().each { device ->
		device.refresh()
	}
}

def devicesList() {
	logErrors([]) {
		def resp = apiGET("/nodes")
		if (resp.status == 200) {
			return resp.data.nodes
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def apiGET(path, body = [:]) {
	try { 
    	if(!isLoggedIn()) {
			log.debug "Need to login"
			getHiveAccessToken()
		}
        log.debug("Beginning API GET: ${apiURL(path)}, ${apiRequestHeaders()}")
        
        httpGet(uri: apiURL(path), contentType: 'application/json', headers: apiRequestHeaders()) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

def apiPOST(path, body = [:]) {
	try {
    	if(!isLoggedIn()) {
			log.debug "Need to login"
			getHiveAccessToken()
		}
		log.debug("Beginning API POST: ${path}, ${body}")
        
		httpPostJson(uri: apiURL(path), body: body, headers: apiRequestHeaders() ) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

def apiPUT(path, body = [:]) {
	try {
    	if(!isLoggedIn()) {
			log.debug "Need to login"
			getHiveAccessToken()
		}
		log.debug("Beginning API POST: ${path}, ${body}")
        
		httpPutJson(uri: apiURL(path), body: body, headers: apiRequestHeaders() ) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

def getHiveAccessToken() {   
	try {
    	def params = [
			uri: apiURL('/auth/sessions'),
        	contentType: 'application/json',
        	headers: [
              'Content-Type': 'application/vnd.alertme.zoo-6.1+json',
              'Accept': 'application/vnd.alertme.zoo-6.2+json',
              'Content-Type': 'application/*+json',
              'X-AlertMe-Client': 'Hive Web Dashboard',
        	],
        	body: [
        		sessions: [	[username: settings.username,
                 		password: settings.password,
                 		caller: 'Hive Web Dashboard']]
        	]
    	]

		state.cookie = ''
	
		httpPostJson(params) {response ->
			log.debug "Request was successful, $response.status"
			log.debug response.headers
      
        	state.cookie = response?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
			log.debug "Adding cookie to collection: $cookie"
        	log.debug "auth: $response.data"
			log.debug "cookie: $state.cookie"
        	log.debug "sessionid: ${response.data.sessions[0].id}"
        
        	state.hiveAccessToken = response.data.sessions[0].id
        	// set the expiration to 5 minutes
			state.hiveAccessToken_expires_at = new Date().getTime() + 300000
            state.loginerrors = null
		}
    } catch (groovyx.net.http.HttpResponseException e) {
    	state.hiveAccessToken = null
        state.hiveAccessToken_expires_at = null
   		state.loginerrors = "Error: ${e.response.status}: ${e.response.data}"
    	logResponse(e.response)
		return e.response
    }
}

Map apiRequestHeaders() {        
	return [
    	'Cookie': state.cookie,
        'Content-Type': 'application/vnd.alertme.zoo-6.2+json',
        'Accept': 'application/vnd.alertme.zoo-6.2+json',
        'Content-Type': 'application/*+json',
        'X-AlertMe-Client': 'Hive Web Dashboard',
        'X-Omnia-Access-Token': "${state.hiveAccessToken}"
    ]
}

def isLoggedIn() {
	log.debug "Calling isLoggedIn()"
	log.debug "isLoggedIn state $state.hiveAccessToken"
	if(!state.hiveAccessToken) {
		log.debug "No state.hiveAccessToken"
		return false
	}

	def now = new Date().getTime();
    return state.hiveAccessToken_expires_at > now
}

def logResponse(response) {
	log.info("Status: ${response.status}")
	log.info("Body: ${response.data}")
}

def logErrors(options = [errorReturn: null, logObject: log], Closure c) {
	try {
		return c()
	} catch (groovyx.net.http.HttpResponseException e) {
		options.logObject.error("got error: ${e}, body: ${e.getResponse().getData()}")
		if (e.statusCode == 401) { // token is expired
			state.remove("hiveAccessToken")
			options.logObject.warn "Access token is not valid"
		}
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		options.logObject.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}