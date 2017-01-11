/**
 *  Eight Sleep (Connect)
 *
 *  Copyright 2017 Alex Lee Yuk Cheung
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
 *
 *	11.01.2017: 1.0 BETA Release 1 - Initial Release
 */
definition(
    name: "Eight Sleep (Connect)",
    namespace: "alyc100",
    author: "Alex Lee Yuk Cheung",
    description: "Connect your Eight Sleep device to SmartThings",
    iconUrl: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/8slp-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/8slp-icon.png",
    iconX3Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/8slp-icon.png",
    singleInstance: true
)

preferences {
	page(name:"firstPage", title:"Eight Sleep Device Setup", content:"firstPage", install: true)
    page(name: "loginPAGE")
    page(name: "selectDevicePAGE")
}

def apiURL(path = '/') 			 { return "https://client-api.8slp.net/v1${path}" }

def firstPage() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter Eight Sleep account crednentials", state: authenticated())
  			}
    	}
    }
    else
    {
        return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
            	headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter Eight Sleep account crednentials", state: authenticated())
            }
            if (stateTokenPresent()) {           	
                section ("Choose your Eight Sleep devices:") {
					href("selectDevicePAGE", title: null, description: devicesSelected() ? getDevicesSelectedString() : "Tap to select Eight Sleep devices", state: devicesSelected())
        		}
            } else {
            	section {
            		paragraph "There was a problem connecting to Eight Sleep. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
           }
    	}
    }
}

def loginPAGE() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your Eight Sleep account credentials below to enable SmartThings and Eight Sleep integration." }
    		section {
    			input("username", "text", title: "Username", description: "Your Eight Sleep username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Eight Sleep password", required: true, submitOnChange: true)
  			}   	
    	}
    }
    else {
    	getEightSleepAccessToken()
        dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your Eight Sleep account credentials below to enable SmartThings and Eight Sleep integration." }
    		section("Eight Sleep Credentials:") {
				input("username", "text", title: "Username", description: "Your Eight Sleep username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Eight Sleep password", required: true, submitOnChange: true)	
			}    	
    	
    		if (stateTokenPresent()) {
        		section {
                	paragraph "You have successfully connected to Eight Sleep. Click 'Done' to select your Eight Sleep devices."
  				}
        	}
        	else {
        		section {
            		paragraph "There was a problem connecting to Eight Sleep. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
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
			input "selectedEightSleep", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/eightsleep-device.png", required:false, title:"Select Eight Sleep Device \n(${state.eightSleepDevices.size() ?: 0} found)", multiple:true, options:state.eightSleepDevices
	}
  }
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/8slp-icon.png",
                  "${textVersion()}")
} 

def stateTokenPresent() {
	return state.eightSleepAccessToken != null && state.eightSleepAccessToken != ''
}

def authenticated() {
	return (state.eightSleepAccessToken != null && state.eightSleepAccessToken != '') ? "complete" : null
}

def devicesSelected() {
	return (selectedEightSleep) ? "complete" : null
}

def getDevicesSelectedString() {
	if (state.eightSleepDevices == null) {
    	updateDevices()
  	}
    
	def listString = ""
	selectedEightSleep.each { childDevice ->
    	if (state.eightSleepDevices[childDevice] != null) listString += state.eightSleepDevices[childDevice] + "\n"
  	}
  	return listString
}

// App lifecycle hooks

def installed() {
	log.debug "installed"
	initialize()
	// Check for new devices and remove old ones every 3 hours
	runEvery3Hours('updateDevices')
    // execute refresh method every minute
    schedule("0 0/5 * * * ?", refreshDevices)
}

// called after settings are changed
def updated() {
	log.debug "updated"
	initialize()
    unschedule('refreshDevices')
    schedule("0 0/5 * * * ?", refreshDevices)
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
	if (selectedEightSleep) {
		addEightSleep()
	}
    
    def devices = getChildDevices()
	devices.each {
    	log.debug "Refreshing device $it.name"
        it.refresh()
	}
}

def updateDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	def devices = devicesList()
  	state.eightSleepDevices = [:]

    def selectors = []
    
	devices.each { device ->
        log.debug "Identified: device ${device}"
        def value = "Eight Sleep ${device.reverse().take(4).reverse()}"
		def key = device
		state.eightSleepDevices["${key}"] = value
        def resp = apiGET("/devices/${device}?filter=ownerId,leftUserId,rightUserId")
        if (resp.status == 200) {
        	def leftUserId = resp.data.result.leftUserId
        	def rightUserId = resp.data.result.rightUserId
        	selectors.add("${device}/${leftUserId}")
        	selectors.add("${device}/${rightUserId}")
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
    }
   	log.debug selectors
    
    //Remove devices if does not exist on the Eight Sleep platform
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

def addEightSleep() {
	updateDevices()

	selectedEightSleep.each { device ->
    	def resp = apiGET("/devices/${device}?filter=ownerId,leftUserId,rightUserId")
        if (resp.status == 200) {
        	//Add left side of mattress as device
        	def leftUserId = resp.data.result.leftUserId
			def childDevice = getChildDevice("${device}/${leftUserId}")
        	if (!childDevice && state.eightSleepDevices[device] != null) {
    			log.info("Adding device ${device}/${leftUserId}: ${state.eightSleepDevices[device]} [Left]")
				def data = [
            		name: "${state.eightSleepDevices[device]} [Left]",
					label: "${state.eightSleepDevices[device]} [Left]"
				]
            	childDevice = addChildDevice(app.namespace, "Eight Sleep Mattress", "${device}/${leftUserId}", null, data)
				log.debug "Created ${state.eightSleepDevices[device]} [Left] with id: ${device}/${leftUserId}"
			} else {
				log.debug "found ${state.eightSleepDevices[device]} [Left] with id ${device}/${leftUserId} already exists"
			}
        	
            //Add right side of mattress as device
        	def rightUserId = resp.data.result.rightUserId
            childDevice = getChildDevice("${device}/${rightUserId}")
        	if (!childDevice && state.eightSleepDevices[device] != null) {
    			log.info("Adding device ${device}/${rightUserId}: ${state.eightSleepDevices[device]} [Right]")
				def data = [
            		name: "${state.eightSleepDevices[device]} [Right]",
					label: "${state.eightSleepDevices[device]} [Right]"
				]
            	childDevice = addChildDevice(app.namespace, "Eight Sleep Mattress", "${device}/${rightUserId}", null, data)
				log.debug "Created ${state.eightSleepDevices[device]} [Right] with id: ${device}/${rightUserId}"
			} else {
				log.debug "found ${state.eightSleepDevices[device]} [Right] with id ${device}/${rightUserId} already exists"
			}
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def refreshDevices() {
	log.info("Executing refreshDevices...")
	getChildDevices().each { device ->
    	log.info("Refreshing device ${device.name} ...")
    	device.refresh()
    }
}

def devicesList() {
	logErrors([]) {
    	def resp = apiGET("/users/me")
		if (resp.status == 200) {
			return resp.data.user.devices
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def getEightSleepAccessToken() {  
	def body = [ 
        	"email": "${username}",
        	"password" : "${password}"
        ]
	def resp = apiPOST("/login", body)
    if (resp.status == 200) {
		state.eightSleepAccessToken = resp.data.session.token
        state.userId = resp.data.session.userId
        state.expirationDate = resp.data.session.expirationDate
        log.debug "eightSleepAccessToken: $resp.data.session.token"  
        log.debug "eightSleepUserId: $resp.data.session.userId"  
        log.debug "eightSleepTokenExpirationDate: $resp.data.session.expirationDate" 
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
		httpPost(uri: apiURL(path), body: new groovy.json.JsonBuilder(body).toString(), headers: apiRequestHeaders() ) {response ->
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
		log.debug("Beginning API POST: ${path}, ${body}")
		httpPut(uri: apiURL(path), body: new groovy.json.JsonBuilder(body).toString(), headers: apiRequestHeaders() ) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

Map apiRequestHeaders() {
   //Check token expiry
   if (state.eightSleepAccessToken) {
   		def now = new Date().getTime()
   		def sessionExpiryTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", state.expirationDate).getTime()
   		if (now > sessionExpiryTime) {
   			log.debug "Renewing Access Token"
        	getEightSleepAccessToken()
   		}
   }
   
   return [ "Host": "client-api.8slp.net",
   			"Content-Type": "application/json",
            "API-Key": "api-key",
            "Application-Id": "morphy-app-id",
    		"Connection": "keep-alive",
            "User-Agent" : "Eight%20AppStore/11 CFNetwork/808.2.16 Darwin/16.3.0",
            "Accept-Language": "en-gb",
			"Accept-Encoding": "gzip, deflate",
			"Accept": "*/*",
			"app-Version": "1.10.0",
            "Session-Token": "${state.eightSleepAccessToken}"

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
		options.logObject.error("got error: ${e}, body: ${e.getResponse().getData()}")
		if (e.statusCode == 401) { // token is expired
			state.remove("eightSleepAccessToken")
			options.logObject.warn "Access token is not valid"
		}
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		options.logObject.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}

private def textVersion() {
    def text = "Eight Sleep (Connect)\nVersion: 1.0 BETA Release 1\nDate: 11012017(0040)"
}

private def textCopyright() {
    def text = "Copyright © 2017 Alex Lee Yuk Cheung"
}