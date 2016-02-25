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
 *
 */
definition(
		name: "Hive (Connect)",
		namespace: "alyc100",
		author: "Alex Lee Yuk Cheung",
		description: "Connect your Hive devices to SmartThings.",
		iconUrl: "https://www.hivehome.com/attachment/46/download/hive_logo_low_res.png",
		iconX2Url: "https://www.hivehome.com/attachment/46/download/hive_logo_low_res.png",
        singleInstance: true
) 

preferences {
	page(name:"firstPage", title:"Hive Device Setup", content:"firstPage", install: true)
}

def apiURL(path = '/') 			 { return "https://api.prod.bgchprod.info:443/omnia${path}" }

def firstPage() {
	log.debug "firstPage"
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			input("username", "text", title: "Username", description: "Your Hive username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Hive password", required: true, submitOnChange: true)
  			}
    	}
    }
    else
    {
    	log.debug "next phase"
    	getHiveAccessToken()
        updateDevices()
        return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			input("username", "text", title: "Username", description: "Your Hive username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Hive password", required: true, submitOnChange: true)
            }
            if (state.hiveAccessToken != null && state.hiveAccessToken != '') {
            	section {
                	paragraph "You have successfully connected to Hive. Your devices should be discovered and selectable below."
  				}
                section("Select a device...") {
					input "selectedHeating", "enum", required:false, title:"Select Hive Heating Devices \n(${state.hiveHeatingDevices.size() ?: 0} found)", multiple:true, options:state.hiveHeatingDevices
					input "selectedHotWater", "enum", required:false, title:"Select Hive Hot Water Devices \n(${state.hiveHotWaterDevices.size() ?: 0} found)", multiple:true, options:state.hiveHotWaterDevices
					
				}
            } else {
            	section {
            		paragraph "There was a problem connecting to Hive. Check your user credential and error logs in SmartThings web console."
           		}
           }
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
    	if (device.attributes.capabilities != null && device.attributes.capabilities.reportedValue[0] == "THERMOSTAT") {
        	def parentNode = devices.find { d -> d.id == device.parentNodeId }
            if ((device.attributes.supportsHotWater != null) && (device.attributes.supportsHotWater.reportedValue == false) && (device.attributes.temperature != null)) {
            	def value = "${parentNode.name} Hive Heating"
				def key = device.id
				state.hiveHeatingDevices["${key}"] = value
                
            }
            else if ((device.attributes.supportsHotWater != null) && (device.attributes.supportsHotWater.reportedValue == true)) {
            	def value = "${parentNode.name} Hive Hot Water"
				def key = device.id
				state.hiveHotWaterDevices["${key}"] = value
                
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
			state.hiveAccessToken = response.data
      
        	state.cookie = response?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
			log.debug "Adding cookie to collection: $cookie"
        	log.debug "auth: $response.data"
			log.debug "cookie: $state.cookie"
        	log.debug "sessionid: ${response.data.sessions[0].id}"
        
        	state.hiveAccessToken = response.data.sessions[0].id
        	// set the expiration to 5 minutes
			state.hiveAccessToken_expires_at = new Date().getTime() + 300000;
		}
    } catch (groovyx.net.http.HttpResponseException e) {
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
