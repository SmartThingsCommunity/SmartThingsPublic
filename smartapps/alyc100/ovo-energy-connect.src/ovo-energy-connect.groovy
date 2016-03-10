/**
 *  OVO Energy (Connect)
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
 *  09.03.2016
 *  v2.0 - New OVO Connect App
 *
 */
definition(
		name: "OVO Energy (Connect)",
		namespace: "alyc100",
		author: "Alex Lee Yuk Cheung",
		description: "Connect your OVO Energy Account to SmartThings. (Requires OVO Smart Gateway)",
		iconUrl: "http://a1.mzstatic.com/eu/r30/Purple69/v4/a8/75/b1/a875b13e-a6f1-fe8d-8063-6f36517fc272/icon175x175.jpeg",
		iconX2Url: "http://a1.mzstatic.com/eu/r30/Purple69/v4/a8/75/b1/a875b13e-a6f1-fe8d-8063-6f36517fc272/icon175x175.jpeg",
        singleInstance: true
) 

preferences {
	page(name:"firstPage", title:"OVO Account Setup", content:"firstPage", install: true)
    page(name: "loginPAGE")
    page(name: "selectDevicePAGE")
	page(name: "accountDetailsPAGE")
}

def firstPage() {
	log.debug "firstPage"
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
    			headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter OVO Energy account crednentials", state: authenticated())
  			}
    	}
    }
    else
    {
    	log.debug "next phase"
        return dynamicPage(name: "firstPage", title: "", install: true, uninstall: true) {
			section {
            	headerSECTION()
                href("loginPAGE", title: null, description: authenticated() ? "Authenticated as " +username : "Tap to enter OVO Energy account crednentials", state: authenticated())
            }
            if (stateTokenPresent()) {           	
                section ("Choose your Smart Meters:") {
					href("selectDevicePAGE", title: null, description: devicesSelected() ? "Devices: " + getDevicesSelectedString() : "Tap to select smart meters", state: devicesSelected())
        		}
                section ("Account Details:") {
					href("accountDetailsPAGE", title: null, description: "Tap to view OVO Energy Account Details")
        		}
            } else {
            	section {
            		paragraph "There was a problem connecting to OVO Energy. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
           		}
           }
    	}
    }
}

def headerSECTION() {
	return paragraph (image: "http://a1.mzstatic.com/eu/r30/Purple69/v4/a8/75/b1/a875b13e-a6f1-fe8d-8063-6f36517fc272/icon175x175.jpeg",
                  "OVO Energy (Connect)\nVersion: 2.0\nBuild: 103009032016")
}

def stateTokenPresent() {
	return state.ovoAccessToken != null && state.ovoAccessToken != ''
}

def authenticated() {
	return (state.ovoAccessToken != null && state.ovoAccessToken != '') ? "complete" : null
}

def devicesSelected() {
	return (selectedMeters) ? "complete" : null
}

def getDevicesSelectedString() {
	def listString = ""
	selectedMeters.each { childDevice -> 
    	if (listString == "") {
        	listString += state.smartMeterDevices[childDevice]
        }
        else {
        	listString += "\n" + state.smartMeterDevices[childDevice]
        }
    }
    return listString
}

def loginPAGE() {
	if (username == null || username == '' || password == null || password == '') {
		return dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your OVO Energy account credentials below to enable SmartThings and OVO Energy integration." }
    		section("OVO Energy Credentials:") {
				input("username", "text", title: "Username", description: "Your OVO username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your OVO password", required: true, submitOnChange: true)		
			}    	
    	}
    }
    else {
    	getOVOAccessToken()
        dynamicPage(name: "loginPAGE", title: "Login", uninstall: false, install: false) {
    		section { headerSECTION() }
        	section { paragraph "Enter your OVO Energy account credentials below to enable SmartThings and OVO Energy integration." }
    		section("OVO Energy Credentials:") {
				input("username", "text", title: "Username", description: "Your OVO Energy username (usually an email address)", required: true)
				input("password", "password", title: "Password", description: "Your OVO Energy password", required: true, submitOnChange: true)		
			}    	
    	
    		if (stateTokenPresent()) {
        		section {
                	paragraph "You have successfully connected to OVO Energy. Click 'Done' to select your OVO Smart Meter devices."
  				}
        	}
        	else {
        		section {
            		paragraph "There was a problem connecting to OVO Energy. Check your user credentials and error logs in SmartThings web console.\n\n${state.loginerrors}"
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
			input "selectedMeters", "enum", image: "https://www.ovoenergy.com/binaries/content/gallery/ovowebsitessuite/images/ovo-answers/ihd-screens-15.png/ihd-screens-15.png/ovowebsitessuite%3Acarousel", required:false, title:"Select Smart Meter Devices \n(${state.smartMeterDevices.size() ?: 0} found)", multiple:true, options:state.smartMeterDevices					
		}
    }
}

def accountDetailsPAGE() {
	def accountData = updateAccountDetails()
	dynamicPage(name: "accountDetailsPAGE", title: "OVO Energy Account Details", uninstall: false, install: false) {
    	section("Account Holder") {
    		paragraph "Account ID:\n${accountData.id}"
            paragraph "Name:\n${accountData.accountHolder}"
            paragraph "Address:\n${accountData.homeAddress.line1}\n${accountData.homeAddress.line2}\n${accountData.homeAddress.town}\n${accountData.homeAddress.county}\n${accountData.homeAddress.postcode}"
        }
        section("Balance") {
        	paragraph "${String.format("%1.2f", accountData.balance.amount as BigDecimal)} ${accountData.balance.currency}"
        }
        section("Direct Debit") {
        	paragraph "${String.format("%1.2f", accountData.directDebit.payment.amount as BigDecimal)} ${accountData.directDebit.payment.currency}"
            paragraph "Next Payment Date: ${accountData.directDebit.nextPaymentDate}"
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
    schedule("0 0/1 * * * ?", refreshDevices)
}

// called after settings are changed
def updated() {
	log.debug "updated"
    unsubscribe()
	initialize()
    unschedule('refreshDevices')
    schedule("0 0/1 * * * ?", refreshDevices)
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
    if (selectedMeters)
		addMeters()

    runIn(10, 'refreshDevices') // Asynchronously refresh devices so we don't block
  
}

def updateDevices() {
	log.debug "Executing 'updateDevices'"
	if (!state.devices) {
		state.devices = [:]
	}
	def devices = devicesList()
    state.smartMeterDevices = [:]
    def selectors = []
	devices.each { device -> 
    	if (device.mpan != null) {
        	selectors.add("${device.mpan}")
            def value
        	value = (device.utilityType == "GAS") ? "OVO Gas Smart Meter" : "OVO Electricity Smart Meter"
			def key = device.mpan
			state.smartMeterDevices["${key}"] = value
      	}
	}    
    log.debug selectors
    //Remove devices if does not exist on the OVO platform
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

def addMeters() {
	updateDevices()

	selectedMeters.each { device ->
    	
        def childDevice = getChildDevice("${device}")
        
        if (!childDevice) { 
    		log.info("Adding Smart Meter device ${device}: ${state.smartMeterDevices[device]}")
            
        	def data = [
                name: state.smartMeterDevices[device],
				label: state.smartMeterDevices[device],
			]
            childDevice = addChildDevice(app.namespace, "OVO Energy Meter V2.0", "$device", null, data)
            childDevice.refresh()
           
			log.debug "Created ${state.smartMeterDevices[device]} with id: ${device}"
		} else {
			log.debug "found ${state.smartMeterDevices[device]} with id ${device} already exists"
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
		def resp = apiGET("https://paym.ovoenergy.com/api/paym/accounts")
		if (resp.status == 200) {
			return resp.data.consumers[0]
		} else {
			log.error("Non-200 from device list call. ${resp.status} ${resp.data}")
			return []
		}
	}
}

def updateAccountDetails() {
	logErrors([]) {
		def resp = apiGET("https://paym.ovoenergy.com/api/paym/accounts")
		if (resp.status == 200) {
			return resp.data[0]
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
			getOVOAccessToken()
		}
        log.debug("Beginning API GET: ${path}, ${apiRequestHeaders()}")
        
        httpGet(uri: path, contentType: 'application/json', headers: apiRequestHeaders()) {response ->
			logResponse(response)
			return response
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		logResponse(e.response)
		return e.response
	}
}

def getOVOAccessToken() {   
	try {
    	def params = [
			uri: 'https://my.ovoenergy.com/api/auth/login',
        	contentType: 'application/json;charset=UTF-8',
        	headers: [
              'Accept': 'application/json, text/plain, */*',
              'Content-Type': 'application/json;charset=UTF-8',
              'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36',
              'Origin': 'https://my.ovoenergy.com'
        	],
        	body: [
        		username: settings.username,
                password: settings.password,           
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
        	log.debug "sessionid: ${response.data.token}"
            
            state.ovoAccessToken = response.data.token
        	// set the expiration to 5 minutes
			state.ovoAccessToken_expires_at = new Date().getTime() + 600000
            state.loginerrors = null
		}
		
    } catch (groovyx.net.http.HttpResponseException e) {
    	state.ovoAccessToken = null
        state.ovoAccessToken_expires_at = null
   		state.loginerrors = "Error: ${e.response.status}: ${e.response.data}"
    	logResponse(e.response)
		return e.response
    }
}

Map apiRequestHeaders() {        
	return [
    	'Cookie': "${state.cookie}",
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json;charset=UTF-8',
        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36',
        'Origin': 'https://my.ovoenergy.com',
        'Authorization': "${state.ovoAccessToken}"
    ]
}

def isLoggedIn() {
	log.debug "Calling isLoggedIn()"
	log.debug "isLoggedIn state $state.ovoAccessToken"
	if(!state.ovoAccessToken) {
		log.debug "No state.ovoAccessToken"
		return false
	}

	def now = new Date().getTime();
    return state.ovoAccessToken_expires_at > now
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
			state.remove("ovoAccessToken")
			options.logObject.warn "Access token is not valid"
		}
		return options.errorReturn
	} catch (java.net.SocketTimeoutException e) {
		options.logObject.warn "Connection timed out, not much we can do here"
		return options.errorReturn
	}
}