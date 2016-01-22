/**
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
 *	Nexia Thermostat Service Manager
 *
 *	Author: Trent Foley
 *	Date: 2016-01-19
 */
definition(
    name: "Nexia Thermostat Manager",
    namespace: "trentfoley",
    author: "Trent Foley",
    description: "Connect your Nexia thermostat to SmartThings.",
    category: "Convenience",
    iconUrl: "http://lh4.ggpht.com/oMx3-nlICwLmUxpDhTXWsZ6Ocuzu9P2yfz9jpXBx1rhrW_Vcj94kPl2M9ooApckK6TM1=w60",
    iconX2Url: "https://www.trane.com/content/dam/Trane/residential/products/nexia/medium/TR_Nexia%20-%20Medium.jpg",
    iconX3Url: "https://www.trane.com/content/dam/Trane/residential/products/nexia/medium/TR_Nexia%20-%20Medium.jpg",
    singleInstance: true
) { }

preferences {
	section("Nexia Auth") {
        input "username", "text", title: "Username"
        input "password", "password", title: "Password"
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
    // Ensure authenticated
    refreshAuthToken()
    
    // Get list of thermostats and ensure child devices
    def homeParams = [
        method: 'GET',
        uri: serverUrl,
        headers: defaultHeaders
    ]

    log.debug "Home Parameters: ${homeParams}"

	try {
        httpGet(homeParams) { homeResp ->
            // html / body / div id=footer-wrapper / div id=content / div id=content_sidebar / nav / ul / li / a id=climate_link
            def climatePath = homeResp.data[0].children[1].children[0].children[3].children[1].children[2].children[0].children[3].children[0].attributes()["href"]
            state.thermostatsPath = climatePath.replace("climate/index", "xxl_thermostats")
        }
    }
    catch(Exception e) {
        log.debug "Caught exception determining thermostats path: ${e}"
    }
    
    // Get list of thermostats and ensure child devices
    requestThermostats { thermostatsResp ->
        
        def devices = thermostatsResp.data.collect { stat ->
			def deviceId = stat.id
            log.debug "Found Device with ID: ${deviceId}"
            
            def dni = [ app.id, deviceId ].join('.')
			def d = getChildDevice(dni)
            if(!d) {
                d = addChildDevice(childNamespace, childName, dni, null, [ label: "${childName} (${stat.name})" ])
                log.debug "Created ${d.displayName} with device network id: ${dni}"
            } else {
                log.debug "Found already existing ${d.displayName} with device network id: ${dni}"
            }

            return d
        }

		log.debug "Discovered ${devices.size()} thermostats"
        
		// pollHandler()
    }
}

private updateCookies(groovyx.net.http.HttpResponseDecorator response) {
	response.getHeaders('Set-Cookie').each {
        def cookieValue = it.value.split(';')[0]
        def cookieName = cookieValue.split('=')[0]
        // if (!state.cookies) { state.cookies = [:] }
        
        log.debug "Updating cookie: ${cookieValue}"
        
        state.cookies[(cookieName)] = cookieValue
    }
}

def getDefaultHeaders() {
	def headers = [
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Encoding': 'gzip, deflate',
        'Accept-Language': 'en-US,en,q=0.8',
        'Cache-Control': 'max-age=0',
        'Connection': 'keep-alive',
        'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36'
    ]

    def cookieString = state.cookies?.collect { entry -> entry.value }?.join('; ');
    if (cookieString) { headers.Cookie = cookieString }
    return headers
}

private refreshAuthToken() {
    debugEvent("refreshing auth token")

	// Initialize / clear any existing cookies
	state.cookies = [:]

	def loginParams = [
        method: 'GET',
        uri: serverUrl,
        path: "/login",
        headers: defaultHeaders
    ]

    log.debug "Login Params: ${loginParams}"
    
    try {
        httpGet(loginParams) { loginResp ->
            updateCookies(loginResp)
            
            // html / body   / div id=content / div id=external-wrapper / div id=external-content / div id=login-form / form / div / input name=authenticity_token
            def authenticityToken = loginResp.data[0].children[1].children[1].children[0].children[0].children[1].children[2].children[0].children[1].attributes()["value"]
            log.debug "Authenticity Token: ${authenticityToken}"
            
            def sessionParams = [
                method: 'POST',
                uri: serverUrl,
                path: '/session',
                requestContentType: 'application/x-www-form-urlencoded',
                headers: defaultHeaders,
                body: [
                	'utf8': '✓',
                    'authenticity_token': authenticityToken,
                    'login': settings.username,
                    'password': settings.password
                ]
            ]

            log.debug "Session Parameters: ${sessionParams}"

            httpPost(sessionParams) { sessionResp ->
                if (sessionResp.status != 302) { throw new Exception("Did not receive expected response status code.  Expected 302, actual ${sessionResp.status}") }
            	updateCookies(sessionResp)
            }
        }
    }
    catch(Exception e) {
        log.debug "Caught exception refreshing auth token: ${e}"
    }
}

private def requestThermostats(Closure closure) {
	log.debug "Requesting thermostats at ${state.thermostatsPath}"
    
    def thermostatsParams = [
        method: 'GET',
        uri: serverUrl,
        path: state.thermostatsPath,
        headers: defaultHeaders
    ]

	log.debug "Thermostat Params: ${thermostatsParams}"
    
    try {
        httpGet(thermostatsParams, closure)
    }
    catch(Exception e) {
        log.debug "Caught exception requesting thermostats: ${e}"
    }
}

def getChildNamespace() { "trentfoley" }
def getChildName() { "Nexia Thermostat" }

def getServerUrl() { "https://www.mynexia.com" }

def debugEvent(message, displayEvent = false) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}