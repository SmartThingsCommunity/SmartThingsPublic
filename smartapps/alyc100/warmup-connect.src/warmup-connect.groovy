/**
 *  Warmup (Connect)
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
 */
definition(
    name: "Warmup (Connect)",
    namespace: "alyc100",
    author: "Alex Lee Yuk Cheung",
    description: "Connect your Warmup devices to SmartThings.",
    category: "",
    iconUrl: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/warmup-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/warmup-icon.png",
    iconX3Url: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/warmup-icon.png")
    singleInstance: true


preferences {
	page(name:"firstPage", title:"Warmup Device Setup", content:"firstPage", install: true)
    page(name: "loginPAGE")
    page(name: "selectDevicePAGE")
}

def apiURL() 			 { return "https://api.warmup.com/apps/app/v1" }

def firstPage() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter Warmup account credentials", state: authenticated())
  			}
    	}
    }
    else
    {
        return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
            	headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter Warmup account credentials", state: authenticated())
            }
            if (stateTokenPresent()) {           	
                section ("Choose your Warmup devices:") {
					href("selectDevicePAGE", title: null, description: devicesSelected() ? getDevicesSelectedString() : "Tap to select Warmup devices", state: devicesSelected())
        		}
            } else {
            	section {
            		paragraph "There was a problem connecting to Warmup. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
           }
    	}
    }
}

def loginPAGE() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your Warmup account credentials below to enable SmartThings and Warmup integration." }
    		section {
    			input("username", "text", title: "Username", description: "Your Warmup username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Warmup password", required: true, submitOnChange: true)
  			}   	
    	}
    }
    else {
    	getWarmupAccessToken()
        dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your Warmup account credentials below to enable SmartThings and Warmup integration." }
    		section("Warmup Credentials:") {
				input("username", "text", title: "Username", description: "Your Warmup username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your Warmup password", required: true, submitOnChange: true)	
			}    	
    	
    		if (stateTokenPresent()) {
        		section {
                	paragraph "You have successfully connected to Warmup. Click 'Done' to select your Warmup devices."
  				}
        	}
        	else {
        		section {
            		paragraph "There was a problem connecting to Warmup. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
        	}
        }
    }
}

def selectDevicePAGE() {
	updateLocations()
	dynamicPage(name: "selectDevicePAGE", title: "Devices", uninstall: false, install: false) {
  	section { headerSECTION() }
    if (devicesSelected() == null) {
    	section("Select your Location:") {
			input "selectedLocation", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/warmup-location.png", required:false, title:"Select a Location \n(${state.warmupLocations.size() ?: 0} found)", multiple:false, options:state.warmupLocations, submitOnChange: true
		}
    }
    else {
    	section("Your location:") {
        	paragraph (image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/warmup-location.png",
                  "Location: ${state.warmupLocations[selectedLocation]}\n(Remove all devices to change)")
        }
    }
    if (selectedLocation) {
    	updateDevices()
        
    	section("Select your devices:") {
			input "selectedWarmup4IEs", "enum", image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/warmup-4ie.png", required:false, title:"Select Warmup 4IE Devices \n(${state.warmup4IEDevices.size() ?: 0} found)", multiple:true, options:state.warmup4IEDevices
		}
    }
  }
}

def headerSECTION() {
	return paragraph (image: "https://raw.githubusercontent.com/alyc100/SmartThingsPublic/master/smartapps/alyc100/warmup-icon.png",
                  "${textVersion()}")
}

def stateTokenPresent() {
	return state.warmupAccessToken != null && state.warmupAccessToken != ''
}

def authenticated() {
	return (state.warmupAccessToken != null && state.warmupAccessToken != '') ? "complete" : null
}

def devicesSelected() {
	return (selectedWarmup4IEs) ? "complete" : null
}

def getDevicesSelectedString() {
	if (state.warmup4IEDevices == null) {
    	updateDevices()
  	}
    
	def listString = ""
	selectedWarmup4IEs.each { childDevice ->
    	if (state.warmup4IEDevices[childDevice] != null) listString += state.warmup4IEDevices[childDevice] + "\n"
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
    schedule("0 0/1 * * * ?", refreshDevices)
}

// called after settings are changed
def updated() {
	log.debug "updated"
	initialize()
    unschedule('refreshDevices')
    schedule("0 0/10 * * * ?", refreshDevices)
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
	if (selectedWarmup4IEs) {
		addWarmup4IE()
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
  	state.warmup4IEDevices = [:]

    def selectors = []
	devices.each { device ->
        log.debug "Identified: device ${device.roomId}: ${device.roomName}: ${device.targetTemp}: ${device.currentTemp}: ${device.energy}"
        selectors.add("${device.roomId}")
            def value = "${device.roomName} Warmup"
			def key = device.roomId
			state.warmup4IEDevices["${key}"] = value
            
            //Update names of devices with MiHome
     		def childDevice = getChildDevice("${device.roomId}")
     		if (childDevice) {
     			//Update name of device if different.
     			if(childDevice.name != device.roomName + " Warmup") {
					childDevice.name = device.roomName + " Warmup"
					log.debug "Device's name has changed."
				}
     		}
    }
   	log.debug selectors
    //Remove devices if does not exist on the Warmup platform
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

def updateLocations() {
	def locations = locationsList()
	state.warmupLocations = [:]
    
    def selectors = []
	locations.each { location ->
        log.debug "Identified: location ${location.id}: ${location.name}"
            selectors.add("${location.id}")
            def value = "${location.name}"
			def key = location.id
			state.warmupLocations["${key}"] = value
    }
   	log.debug selectors
}

def addWarmup4IE() {
	updateDevices()

	selectedWarmup4IEs.each { device ->

        def childDevice = getChildDevice("${device}")
        if (!childDevice && state.warmup4IEDevices[device] != null) {
    		log.info("Adding device ${device}: ${state.warmup4IEDevices[device]}")

        	def data = [
                	name: state.warmup4IEDevices[device],
					label: state.warmup4IEDevices[device]
				]
            childDevice = addChildDevice(app.namespace, "Warmup 4IE", "$device", null, data)

			log.debug "Created ${state.warmup4IEDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.warmup4IEDevices[device]} with id ${device} already exists"
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
    	def body = [ 
    		account: [
            	"email": "${username}",
                "token" : "${state.warmupAccessToken}"
            ],
    		request: [
            	"method" : "getRooms",
                "locId" : "${selectedLocation}"
        	]
        ]
		def resp = apiPOST(body)
		if (resp.status == 200 && resp.data.status.result == "success") {
			return resp.data.response.rooms
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def getStatus(roomId) {
	logErrors([]) {
    	def body = [ 
    		account: [
            	"email": "${username}",
                "token" : "${state.warmupAccessToken}"
            ],
    		request: [
            	"method" : "getRooms",
                "locId" : "${selectedLocation}"
        	]
        ]
		def resp = apiPOST(body)
		if (resp.status == 200 && resp.data.status.result == "success") {
			resp.data.response.rooms.each { room ->
            	if (room.roomId == roomId) return room
            }
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def locationsList() {
	logErrors([]) {
    	def body = [ 
        	account: [
            	"email": "${username}",
                "token" : "${state.warmupAccessToken}"
            ],
    		request: [
            	"method" : "getLocations"
        	]
        ]
		def resp = apiPOST(body)
		if (resp.status == 200 && resp.data.status.result == "success") {
			return resp.data.response.locations
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def getWarmupAccessToken() {  
	def body = [ 
    		request: [
            	"email": "${username}",
                "password" : "${password}",
            	"method" : "userLogin",
                "appId" : "${app.id.toUpperCase()}"
        	]
        ]
	def resp = apiPOST(body)
    if (resp.status == 200) {
    	if (resp.data.status.result == "success" && resp.data.status.result == "success") {
			state.warmupAccessToken = resp.data.response.token
    		log.debug "warmupAccessToken: $resp.data.response.token"  
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
        }
	}
}

def apiPOST(body = [:]) {
	def bodyString = new groovy.json.JsonBuilder(body).toString()
	log.debug("Beginning API POST: ${apiURL()}, ${bodyString}")
    try {
    	httpPost(uri: apiURL(), body: bodyString, headers: apiRequestHeaders() ) {
    		response ->
			logResponse(response)
			return response
        }
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

def apiPOSTByChild(args = [:]) {
	def body = [ 
        	account: [
            	"email": "${username}",
                "token" : "${state.warmupAccessToken}"
            ],
    		request: args
        ]
    return apiPOST(body)
}

def setLocationToFrost() {
	def body = [
    	account: [
            	"email": "${username}",
                "token" : "${state.warmupAccessToken}"
            ],
    	request: [
        		method: "setModes", values: [holEnd:"-", fixedTemp: "",holStart:"-",geoMode:"0",holTemp:"-",locId:"${selectedLocation}",locMode:"frost"]
        	]
    ]
    return apiPOST(body)
}


Map apiRequestHeaders() {
   return [ "Host": "api.warmup.com",
   			"Content-Type": "application/json",
            "APP-Token": "M=;He<Xtg\"\$}4N%5k{\$:PD+WA\"]D<;#PriteY|VTuA>_iyhs+vA\"4lic{6-LqNM:",
    		"Connection": "keep-alive",
            "User-Agent" : "WARMUP_APP",
            "Accept-Language": "en-gb",
			"Accept-Encoding": "gzip, deflate",
			"Accept": "*/*",
			"APP-Version": "1.4.2",

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
			state.remove("warmupAccessToken")
			options.logObject.warn "Access token is not valid"
		}
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		options.logObject.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}



private def textVersion() {
    def text = "Warmup (Connect)\nVersion: 1.0 BETA\nDate: 14122016(1500)"
}

private def textCopyright() {
    def text = "Copyright © 2016 Alex Lee Yuk Cheung"
}