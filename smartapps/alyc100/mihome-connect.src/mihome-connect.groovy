/**
 *	MiHome (Connect)
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
 *	VERSION HISTORY
 *  09.01.2016
 *	v1.0 - Initial Release
 *
 *	10.01.2016
 *	v1.0.1 - Improve messaging for connection process.
 *
 *	17.01.2016
 *	v1.0.2 - Bug fix when device has been manually deleted.
 *
 *	31.01.2016
 *	v1.0.3 - Bug fix to refresh schedule job.
 *
 *	v1.0.3b - Added icons to MiHome device list.
 */
definition(
		name: "MiHome (Connect)",
		namespace: "alyc100",
		author: "Alex Lee Yuk Cheung",
		description: "Connect your MiHome devices to SmartThings.",
		iconUrl: "https://mihome4u.co.uk/assets/homepage/mihome-icon-89db7a9bfb5c8b066ffb4e50c8d68235.png",
		iconX2Url: "https://mihome4u.co.uk/assets/homepage/mihome-icon-89db7a9bfb5c8b066ffb4e50c8d68235.png",
        singleInstance: true
) 

preferences {
	page(name:"firstPage", title:"MiHome Device Setup", content:"firstPage", install: true)
}

def apiURL(path = '/') 			 { return "https://mihome4u.co.uk/api/v1${path}" }

def firstPage() {
	log.debug "firstPage"
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			input("username", "text", title: "Username", description: "Your MiHome username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your MiHome password", required: true, submitOnChange: true)
  			}
    	}
    }
    else
    {
    	log.debug "next phase"
    	getMiHomeAccessToken()
        updateDevices()
        return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			input("username", "text", title: "Username", description: "Your MiHome username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your MiHome password", required: true, submitOnChange: true)
            }
            if (state.miHomeAccessToken != null && state.miHomeAccessToken != '') {
            	section {
                	paragraph "You have successfully connected to MiHome. Press 'Done' and your devices should have been added automatically."
  				}
                section("Devices Discovered And Automatically Added...") {
					state.devices.each {devices ->
                    	paragraph image: "https://mihome4u.co.uk/assets/homepage/mihome4-01bc8a0e478b385df3248b55cc2df7ca.png", devices.trim()
                    }
					
				}
            } else {
            	paragraph "There was a problem connecting to MiHome. Check your user credential and error logs in SmartThings web console."
            }
    	}
    }
}

// App lifecycle hooks

def installed() {
	log.debug "installed"
	initialize()
	// Check for new devices and remove old ones every 3 hours
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
	unschedule('updateDevices')
    unschedule('refreshDevices')
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
	updateDevices()
}


def updateDevices() {
	state.devices = []
	def devices = devicesList()
    def selectors = []
	devices.each { device ->
    	def childDevice = getChildDevice("${device.id}")
        state.devices.add("${device.label} eTRV")
    	selectors.add("${device.id}")
        if (!childDevice) { 
    		log.info("Adding device ${device.id}: ${device.device_type}: ${device.label}: ${device.target_temperature}: ${device.last_temperature}: ${device.voltage}")
    		if (device.device_type == 'etrv')
        	{
        		def data = [
                	name: device.label + " eTRV",
					label: device.label + " eTRV",
					temperature: device.last_temperature,
					heatingSetpoint: device.target_temperature,
					switch: device.target_temperature == 12 ? "off" : "on"
				]
            	childDevice = addChildDevice(app.namespace, "MiHome eTRV", "$device.id", null, data)
        	}
            //Support for further miHome device types can be added here.
        }
    }
    getChildDevices().findAll { !selectors.contains("${it.deviceNetworkId}") }.each {
		log.info("Deleting ${it.deviceNetworkId}")
        try {
			deleteChildDevice(it.deviceNetworkId)
        } catch (physicalgraph.exception.NotFoundException e) {
        	log.info("Could not find ${it.deviceNetworkId}. Assuming manually deleted.")
        }
	}
	//runIn(1, 'refreshDevices') // Asynchronously refresh devices so we don't block
}

def refreshDevices() {
	log.info("Refreshing all devices...")
	getChildDevices().each { device ->
		device.refresh()
	}
}

def devicesList() {
	logErrors([]) {
		def resp = apiGET("/subdevices/list")
		if (resp.status == 200) {
			return resp.data.data
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def getMiHomeAccessToken() {   
	def resp = apiGET("/users/profile")
    if (resp.status == 200) {
			state.miHomeAccessToken = resp.data.data.api_key
    		log.debug "miHomeAccessToken: $resp.data.data.api_key"  
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
}

def apiGET(path) {
	try {   			
        httpGet(uri: apiURL(path), headers: apiRequestHeaders()) {response ->
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
		log.debug("Beginning API POST: ${path}, ${body}")
		httpGet(uri: apiURL(path), body: new groovy.json.JsonBuilder(body).toString(), headers: apiRequestHeaders() ) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

Map apiRequestHeaders() {
	def userpassascii = "${username}:${password}"
    if (state.miHomeAccessToken != null && state.miHomeAccessToken != '') {
    	userpassascii = "${username}:${state.miHomeAccessToken}"
    }
  	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    log.debug userpassascii
        
	return ["User-Agent": "SmartThings Integration",
            "Authorization": "$userpass"
	]
}

def logResponse(response) {
	log.info("Status: ${response.status}")
	//log.info("Body: ${response.data}")
}

def logErrors(options = [errorReturn: null, logObject: log], Closure c) {
	try {
		return c()
	} catch (groovyx.net.http.HttpResponseException e) {
		options.logObject.error("got error: ${e}, body: ${e.getResponse().getData()}")
		if (e.statusCode == 401) { // token is expired
			state.remove("miHomeAccessToken")
			options.logObject.warn "Access token is not valid"
		}
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		options.logObject.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}