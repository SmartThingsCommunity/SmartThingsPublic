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
 *	Ecobee Service Manager
 *
 *	Author: scott
 *	Date: 2013-08-07
 *
 *  Last Modification:
 *      JLH - 01-23-2014 - Update for Correct SmartApp URL Format
 *      JLH - 02-15-2014 - Fuller use of ecobee API
 *      10-28-2015 DVCSMP-604 - accessory sensor, DVCSMP-1174, DVCSMP-1111 - not respond to routines
 *      StrykerSKS - 12-11-2015 - Make it work (better) with the Ecobee 3
 *
 *  See Changelog for change history
 *
 */  
def getVersionNum() { return "0.9.7" }
private def getVersionLabel() { return "ecobee (Connect) Version ${getVersionNum()}-RC8" }
private def getHelperSmartApps() {
	return [ 
    		[name: "ecobeeRoutinesChild", appName: "ecobee Routines",  
            	namespace: "smartthings", multiple: true, 
                title: "Create new Routines Handler..."]
             ]    
}
 
definition(
	name: "Ecobee (Connect)",
	namespace: "smartthings",
	author: "Sean Kendall Schneyer",
	description: "Connect your Ecobee thermostat to SmartThings.",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png",
	singleInstance: true
) {
	appSetting "clientId"
}

preferences {
	page(name: "mainPage")
    page(name: "removePage")
	page(name: "authPage")
	page(name: "thermsPage")
	page(name: "sensorsPage")
    page(name: "preferencesPage")    
    page(name: "helperSmartAppsPage")    
    // Part of debug Dashboard
    page(name: "debugDashboardPage")
    page(name: "pollChildrenPage")
    page(name: "updatedPage")
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}


// Begin Preference Pages
def mainPage() {
	
	def deviceHandlersInstalled 
    def readyToInstall 
    
    // Only create the dummy devices if we aren't initialized yet
    if (!state.initialized) {
    	deviceHandlersInstalled = testForDeviceHandlers()
    	readyToInstall = deviceHandlersInstalled
	}
    if (state.initialized) { readyToInstall = true }
    
	dynamicPage(name: "mainPage", title: "Welcome to ecobee (Connect)", install: readyToInstall, uninstall: false, submitOnChange: true) {
    	def ecoAuthDesc = (state.authToken != null) ? "[Connected]\n" :"[Not Connected]\n"        
		
        // If not device Handlers we cannot proceed
        if(!state.initialized && !deviceHandlersInstalled) {
			section() {
				paragraph "ERROR!\n\nYou MUST add the ${getChildThermostatName()} and ${getChildSensorName()} Device Handlers to the IDE BEFORE running the setup."				
			}		
        } else {
        	readyToInstall = true
        }
        
        if(state.initialized && !state.authToken) {
        	section() {
				paragraph "WARNING!\n\nYou are no longer connected to the ecobee API. Please re-Authorize below."				
			}
        }       

		if(state.authToken != null && state.initialized != true) {
        	section() {
            	paragraph "Please click 'Done' to save your credentials. Then re-open the SmartApp to continue the setup."
            }
        }

		// Need to save the initial login to setup the device without timeouts
		if(state.authToken != null && state.initialized) {
        	if (settings.thermostats?.size() > 0 && state.initialized) {
            	section("Helper SmartApps") {
                	href ("helperSmartAppsPage", title: "Helper SmartApps", description: "Tap to manage Helper SmartApps")
                }            
            }
			section("Devices") {
				def howManyThermsSel = settings.thermostats?.size() ?: 0
                def howManyTherms = state.numAvailTherms ?: "?"
                def howManySensors = state.numAvailSensors ?: "?"
                
                // Thermostats
				state.settingsCurrentTherms = settings.thermostats ?: []
    	    	href ("thermsPage", title: "Thermostats", description: "Tap to select Thermostats [${howManyThermsSel}/${howManyTherms}]")                
                
                // Sensors
            	if (settings.thermostats?.size() > 0) {
					state.settingsCurrentSensors = settings.ecobeesensors ?: []
                	def howManySensorsSel = settings.ecobeesensors?.size() ?: 0
                    if (howManySensorsSel > howManySensors) { howManySensorsSel = howManySensors } // This is due to the fact that you can remove alread selected hiden items
            		href ("sensorsPage", title: "Sensors", description: "Tap to select Sensors [${howManySensorsSel}/${howManySensors}]")
	            }
    	    }        
	        section("Preferences") {
    	    	href ("preferencesPage", title: "Preferences", description: "Tap to review SmartApp settings.")
                LOG("In Preferences page section after preferences line", 5, null, "trace")
        	}
            if ( debugLevel(5) ) {
	        	section ("Debug Dashboard") {
					href ("debugDashboardPage", description: "Tap to enter the Debug Dashboard", title: "Debug Dashboard")
    	    	}
			}
    	} // End if(state.authToken)
        
        // Setup our API Tokens       
		section("Ecobee Authentication") {
			href ("authPage", title: "ecobee Authorization", description: "${ecoAuthDesc}Tap for ecobee Credentials")
		}        
		section("Remove ecobee (Connect)") {
			href ("removePage", description: "Tap to remove ecobee (Connect) ", title: "Remove ecobee (Connect)")
		}            
     
		section (getVersionLabel())
	}
}


def removePage() {
	dynamicPage(name: "removePage", title: "Remove ecobee (Connect) and All Devices", install: false, uninstall: true) {
    	section ("WARNING!\n\nRemoving ecobee (Connect) also removes all Devices\n") {
        }
    }
}

// Setup OAuth between SmartThings and Ecobee clouds
def authPage() {
	LOG("=====> authPage() Entered", 5)

	if(!state.accessToken) { //this is an access token for the 3rd party to make a call to the connect app
		state.accessToken = createAccessToken()
	}

	def description = "Click to enter ecobee Credentials"
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(state.authToken) {
		description = "You are connected. Tap Done above."
		uninstallAllowed = true
		oauthTokenProvided = true
        apiRestored()
	} else {
		description = "Tap to enter ecobee Credentials"
	}

	def redirectUrl = buildRedirectUrl //"${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}"
    LOG("authPage() --> RedirectUrl = ${redirectUrl}")
    
	// get rid of next button until the user is actually auth'd
	if (!oauthTokenProvided) {
    	LOG("authPage() --> in !oauthTokenProvided")    	
		return dynamicPage(name: "authPage", title: "ecobee Setup", nextPage: "", uninstall: uninstallAllowed) {
			section() {
				paragraph "Tap below to log in to the ecobee service and authorize SmartThings access. Be sure to press the 'Allow' button on the 2nd page."
				href url:redirectUrl, style:"embedded", required:true, title: "ecobee Account Authorization", description:description 
			}
		}
	} else {    	
        LOG("authPage() --> in else for oauthTokenProvided - ${state.authToken}.")
        return dynamicPage(name: "authPage", title: "ecobee Setup", nextPage: "mainPage", uninstall: uninstallAllowed) {
        	section() {
            	paragraph "Return to main menu."
                href url:redirectUrl, style: "embedded", state: "complete", title: "ecobee Account Authorization", description: description
                }
        }           
	}
}

// Select which Thermostats are to be used
def thermsPage(params) {
	LOG("=====> thermsPage() entered", 5)
    state.thermsPageVisited = true
        
	def stats = getEcobeeThermostats()
    LOG("thermsPage() -> thermostat list: ${stats}")
    LOG("thermsPage() starting settings: ${settings}")
    LOG("thermsPage() params passed? ${params}", 4, null, "trace")

    dynamicPage(name: "thermsPage", title: "Select Thermostats", params: params, nextPage: "", content: "thermsPage", uninstall: false) {    
    	section("Units") {
        	paragraph "NOTE: The units type (F or C) is determined by your Hub Location settings automatically. Please update your Hub settings (under My Locations) to change the units used. Current value is ${getTemperatureScale()}."
        }
    	section("Select Thermostats") {
			LOG("thersPage(): state.settingsCurrentTherms=${state.settingsCurrentTherms}   settings.thermostats=${settings.thermostats}", 4, null, "trace")
			if (state.settingsCurrentTherms != settings.thermostats) {
				LOG("state.settingsCurrentTherms != settings.thermostats determined!!!", 4, null, "trace")			
			} else { LOG("state.settingsCurrentTherms == settings.thermostats: No changes detected!", 4, null, "trace") }
        	paragraph "Tap below to see the list of ecobee thermostats available in your ecobee account and select the ones you want to connect to SmartThings."
			input(name: "thermostats", title:"Select Thermostats", type: "enum", required:false, multiple:true, description: "Tap to choose", params: params, metadata:[values:stats], submitOnChange: true)        
        }      
    }      
}

def sensorsPage() {
	// Only show sensors that are part of the chosen thermostat(s)
    // Refactor to show the sensors under their corresponding Thermostats. Use Thermostat name as section header?
    LOG("=====> sensorsPage() entered. settings: ${settings}", 5)
    state.sensorsPageVisited = true

	def options = getEcobeeSensors() ?: []
	def numFound = options.size() ?: 0
    
    LOG("options = getEcobeeSensors == ${options}")

    dynamicPage(name: "sensorsPage", title: "Select Sensors", nextPage: "") {
		if (numFound > 0)  {
			section("Select Sensors"){
				LOG("sensorsPage(): state.settingsCurrentSensors=${state.settingsCurrentSensors}   settings.ecobeesensors=${settings.ecobeesensors}", 4, null, "trace")
				if (state.settingsCurrentSensors != settings.ecobeesensors) {
					LOG("state.settingsCurrentSensors != settings.ecobeesensors determined!!!", 4, null, "trace")					
				} else { LOG("state.settingsCurrentSensors == settings.ecobeesensors: No changes detected!", 4, null, "trace") }
				paragraph "Tap below to see the list of ecobee sensors available for the selected thermostat(s) and select the ones you want to connect to SmartThings."
                if (settings.showThermsAsSensor) { paragraph "NOTE: Also showing Thermostats as an available sensor to allow for actual temperature values to be used." }
				input(name: "ecobeesensors", title:"Select Ecobee Sensors (${numFound} found)", type: "enum", required:false, description: "Tap to choose", multiple:true, metadata:[values:options])
			}
		} else {
    		 // No sensors associated with this set of Thermostats was found
           LOG("sensorsPage(): No sensors found.", 4)
           section(""){
           		paragraph "No associated sensors were found. Click Done above."
           }
	    }        
	}
}

def preferencesPage() {
    LOG("=====> preferencesPage() entered. settings: ${settings}", 5)

    dynamicPage(name: "preferencesPage", title: "Update SmartApp Preferences", nextPage: "") {
		section("SmartApp Preferences") {
        	input(name: "holdType", title:"Select Hold Type", type: "enum", required:false, multiple:false, description: "Until I Change", metadata:[values:["Until I Change", "Until Next Program"]])
            paragraph "The 'Smart Auto Temperature Adjust' feature determines if you want to allow the thermostat setpoint to be changed using the arrow buttons in the Tile when the thermostat is in 'auto' mode."
            input(name: "smartAuto", title:"Use Smart Auto Temperature Adjust?", type: "bool", required:false, description: false)
            input(name: "pollingInterval", title:"Polling Interval (in Minutes)", type: "enum", required:false, multiple:false, description: "5", options:["5", "10", "15", "30"])
            input(name: "debugLevel", title:"Debugging Level (higher # for more information)", type: "enum", required:false, multiple:false, description: "3", metadata:[values:["5", "4", "3", "2", "1", "0"]])            
            paragraph "Showing a Thermostat as a Remote Sensor is useful if you need to access the actual temperature in the room where the Thermostat is located and not just the (average) temperature displayed on the Thermostat"
            input(name: "showThermsAsSensor", title:"Include Thermostats as a Remote Sensor?", type: "bool", required:false, description: false)
        }
	}
}

def debugDashboardPage() {
	LOG("=====> debugDashboardPage() entered.", 5)    
    
    dynamicPage(name: "debugDashboardPage", title: "") {
    	section (getVersionLabel())
		section("Commands") {
        	href(name: "pollChildrenPage", title: "", required: false, page: "pollChildrenPage", description: "Tap to execute command: pollChildren()")
            href(name: "updatedPage", title: "", required: false, page: "updatedPage", description: "Tap to execute command: updated()")
        }    	
        
    	section("Settings Information") {
        	paragraph "debugLevel: ${settings.debugLevel} (default=3 if null)"
            paragraph "holdType: ${settings.holdType} (default='Until I Change' if null)"
            paragraph "pollingInterval: ${settings.pollingInterval} (default=5 if null)"
            paragraph "showThermsAsSensor: ${settings.showThermsAsSensor} (default=false if null)"
            paragraph "smartAuto: ${settings.smartAuto} (default=false if null)"   
            paragraph "Selected Thermostats: ${settings.thermostats}"
        }
        section("Dump of Debug Variables") {
        	def debugParamList = getDebugDump()
            LOG("debugParamList: ${debugParamList}", 4, null, "debug")
            //if ( debugParamList?.size() > 0 ) {
			if ( debugParamList != null ) {
            	debugParamList.each { key, value ->  
                	LOG("Adding paragraph: key:${key}  value:${value}", 5, null, "trace")
                	paragraph "${key}: ${value}"
                }
            }
        }
    	section("Commands") {
        	href(name: "pollChildrenPage", title: "", required: false, page: "pollChildrenPage", description: "Tap to execute command: pollChildren()")
            href ("removePage", description: "Tap to remove ecobee (Connect) ", title: "")
        }
    }    
}

// pages that are part of Debug Dashboard
def pollChildrenPage() {
	LOG("=====> pollChildrenPage() entered.", 5)
    state.forcePoll = true // Reset to force the poll to happen
	pollChildren(null)
    
	dynamicPage(name: "pollChildrenPage", title: "") {
    	section() {
        	paragraph "pollChildren() was called"
        }
    }    
}

// pages that are part of Debug Dashboard
def updatedPage() {
	LOG("=====> updatedPage() entered.", 5)
    updated()
    
	dynamicPage(name: "updatedPage", title: "") {
    	section() {
        	paragraph "updated() was called"
        }
    }    
}

def helperSmartAppsPage() {
	LOG("helperSmartAppsPage() entered", 5)

	LOG("SmartApps available are ${getHelperSmartApps()}", 5, null, "info")
	
    //getHelperSmartApps() {
 	dynamicPage(name: "helperSmartAppsPage", title: "Helper Smart Apps", install: true, uninstall: false, submitOnChange: true) {    	
		getHelperSmartApps().each { oneApp ->
			LOG("Processing the app: ${oneApp}", 4, null, "trace")            
            def allowMultiple = oneApp.multiple.value
			section ("${oneApp.appName.value}") {
            	app(name:"${oneApp.name.value}", appName:"${oneApp.appName.value}", namespace:"${oneApp.namespace.value}", title:"${oneApp.title.value}", multiple: allowMultiple)            
                //app(name: "${oneApp.name.value}", appName: "ecobee Routines", namespace: "smartthings", title: "Create new ecobee Routine Handler...", multiple: true)
			}
		}
	}
}
// End Prefernce Pages


// Preference Pages Helpers
private def Boolean testForDeviceHandlers() {
	if (state.runTestOnce != null) { return state.runTestOnce }
    
    def DNIAdder = now().toString()
    def d1
    def d2
    def success = true
    
	try {    	
		d1 = addChildDevice(app.namespace, getChildThermostatName(), "dummyThermDNI-${DNIAdder}", null, ["label":"Ecobee Thermostat:TestingForInstall"])			
		d2 = addChildDevice(app.namespace, getChildSensorName(), "dummySensorDNI-${DNIAdder}", null, ["label":"Ecobee Sensor:TestingForInstall"])
	} catch (physicalgraph.app.exception.UnknownDeviceTypeException e) {
		LOG("You MUST add the ${getChildThermostatName()} and ${getChildSensorName()} Device Handlers to the IDE BEFORE running the setup.", 1, null, "error")
		success = false
	}
    
    state.runTestOnce = success
    
    if (d1) deleteChildDevice("dummyThermDNI-${DNIAdder}") 
    if (d2) deleteChildDevice("dummySensorDNI-${DNIAdder}") 
    
    return success
}

// End Preference Pages Helpers

// OAuth Init URL
def oauthInitUrl() {
	LOG("oauthInitUrl with callback: ${callbackUrl}", 5)
	state.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			client_id: smartThingsClientId,			
			scope: "smartRead,smartWrite",
			redirect_uri: callbackUrl, //"https://graph.api.smartthings.com/oauth/callback"
			state: state.oauthInitState			
	]

	LOG("oauthInitUrl - Before redirect: location: ${apiEndpoint}/authorize?${toQueryString(oauthParams)}", 4)
	redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
}

// OAuth Callback URL and helpers
def callback() {
	LOG("callback()>> params: $params, params.code ${params.code}, params.state ${params.state}, state.oauthInitState ${state.oauthInitState}", 4)
	def code = params.code
	def oauthState = params.state

	//verify oauthState == state.oauthInitState, so the callback corresponds to the authentication request
	if (oauthState == state.oauthInitState){
    	LOG("callback() --> States matched!", 4)
		def tokenParams = [
			grant_type: "authorization_code",
			code      : code,
			client_id : smartThingsClientId,
			state	  : oauthState,
			redirect_uri: callbackUrl //"https://graph.api.smartthings.com/oauth/callback"
		]

		def tokenUrl = "${apiEndpoint}/token?${toQueryString(tokenParams)}"
        LOG("callback()-->tokenURL: ${tokenUrl}", 2)

		httpPost(uri: tokenUrl) { resp ->
			state.refreshToken = resp.data.refresh_token
			state.authToken = resp.data.access_token
            
            LOG("Expires in ${resp?.data?.expires_in} seconds")
            state.authTokenExpires = now() + (resp.data.expires_in * 1000)
            LOG("swapped token: $resp.data; state.refreshToken: ${state.refreshToken}; state.authToken: ${state.authToken}", 2)
		}

		if (state.authToken) {        	
			success()            
		} else {
			fail()
		}

	} else {
    	LOG("callback() failed oauthState != state.oauthInitState", 1, null, "warn")
	}

}

def success() {
	def message = """
    <p>Your ecobee Account is now connected to SmartThings!</p>
    <p>Click 'Done' to finish setup.</p>
    """
	connectionStatus(message)
}

def fail() {
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
<meta name="viewport" content="width=640">
<title>Ecobee & SmartThings connection</title>
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
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/ecobee%402x.png" alt="ecobee icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                ${message}
        </div>
</body>
</html>
"""

	render contentType: 'text/html', data: html
}
// End OAuth Callback URL and helpers

// Get the list of Ecobee Thermostats for use in the settings pages
def getEcobeeThermostats() {	
	LOG("====> getEcobeeThermostats() entered", 5)    
 	def requestBody = '{"selection":{"selectionType":"registered","selectionMatch":"","includeRuntime":true,"includeSensors":true,"includeProgram":true}}'
	def deviceListParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${state.authToken}"],
			query: [format: 'json', body: requestBody]
	]

	def stats = [:]
    try {
        httpGet(deviceListParams) { resp ->
		LOG("httpGet() response: ${resp.data}")
        
        // Initialize the Thermostat Data. Will reuse for the Sensor List intialization
        state.thermostatData = resp.data        	
        
            if (resp.status == 200) {
            	LOG("httpGet() in 200 Response")
                state.numAvailTherms = resp.data.thermostatList?.size() ?: 0
                
            	resp.data.thermostatList.each { stat ->
					def dni = [app.id, stat.identifier].join('.')
					stats[dni] = getThermostatDisplayName(stat)
                }
            } else {                
                LOG("httpGet() - in else: http status: ${resp.status}")
                //refresh the auth token
                if (resp.status == 500 && resp.data.status.code == 14) {
                	LOG("Storing the failed action to try later")
                    state.action = "getEcobeeThermostats"
                    LOG("Refreshing your auth_token!", 1)
                    refreshAuthToken(true)
                } else {
                	LOG("Other error. Status: ${resp.status}  Response data: ${resp.data} ", 1)
                }
            }
        }
    } catch(Exception e) {
    	LOG("___exception getEcobeeThermostats(): ${e}", 1, null, "error")
        refreshAuthToken(true)
    }
	state.thermostatsWithNames = stats
    LOG("state.thermostatsWithNames == ${state.thermostatsWithNames}", 4)
	return stats
}

// Get the list of Ecobee Sensors for use in the settings pages (Only include the sensors that are tied to a thermostat that was selected)
// NOTE: getEcobeeThermostats() should be called prior to getEcobeeSensors to refresh the full data of all thermostats
Map getEcobeeSensors() {	
    LOG("====> getEcobeeSensors() entered. thermostats: ${thermostats}", 5)

	def sensorMap = [:]
    def foundThermo = null
	// TODO: Is this needed?
	state.remoteSensors = [:]    

	// Need to query to get full list of Thermostats (need to pull this here as we can call getEcobeeSensors out of sequence after initial setup
    // TODO: Check on possible race conditions. Leave to update and initialize procedures to call in sequence?
    // getEcobeeThermostats()
	
	state.thermostatData.thermostatList.each { singleStat ->
		LOG("thermostat loop: singleStat.identifier == ${singleStat.identifier} -- singleStat.remoteSensors == ${singleStat.remoteSensors} ", 4)
        
    	if (!settings.thermostats.findAll{ it.contains(singleStat.identifier) } ) {
        	// We can skip this thermostat as it was not selected by the user
            LOG("getEcobeeSensors() --> Skipping this thermostat: ${singleStat.identifier}", 5)
        } else {
        	LOG("getEcobeeSensors() --> Entering the else... we found a match. singleStat == ${singleStat.name}", 4)
                        
        	state.remoteSensors = state.remoteSensors ? (state.remoteSensors + singleStat.remoteSensors) : singleStat.remoteSensors
            LOG("After state.remoteSensors setup...", 5)	        
                        
            LOG("getEcobeeSensors() - singleStat.remoteSensors: ${singleStat.remoteSensors}", 4)
            LOG("getEcobeeSensors() - state.remoteSensors: ${state.remoteSensors}", 4)
		}
        
		// WORKAROUND: Iterate over remoteSensors list and add in the thermostat DNI
		// 		 This is needed to work around the dynamic enum "bug" which prevents proper deletion
        LOG("remoteSensors all before each loop: ${state.remoteSensors}", 5, null, "trace")
		state.remoteSensors.each {
        	LOG("Looping through each remoteSensor. Current remoteSensor: ${it}", 5, null, "trace")
			if (it.type == "ecobee3_remote_sensor") {
            	LOG("Adding an ecobee3_remote_sensor: ${it}", 4, null, "trace")
				def value = "${it?.name}"
				def key = "ecobee_sensor-"+ it?.id + "-" + it?.code
				sensorMap["${key}"] = value
			} else if ( (it.type == "thermostat") && (settings.showThermsAsSensor == true) ) {            	
				LOG("Adding a Thermostat as a Sensor: ${it}", 4, null, "trace")
                def value = "${it?.name}"
				def key = "ecobee_sensor_thermostat-"+ it?.id + "-" + it?.name
                LOG("Adding a Thermostat as a Sensor: ${it}, key: ${key}  value: ${value}", 4, null, "trace")
				sensorMap["${key}"] = value + " (Thermostat)"
            } else if ( it.type == "control_sensor" && it.capability[0]?.type == "temperature") {
            	// We can add this one as it supports temperature
                LOG("Adding a control_sensor: ${it}", 4, null, "trace")
				def value = "${it?.name}"
				def key = "control_sensor-"+ it?.id
				sensorMap["${key}"] = value
            
            } else {
            	LOG("Did NOT add: ${it}. settings.showThermsAsSensor=${settings.showThermsAsSensor}", 4, null, "trace")
            }
		}
	} // end thermostats.each loop
	
    LOG("getEcobeeSensors() - remote sensor list: ${sensorMap}", 4)
    state.eligibleSensors = sensorMap
    state.numAvailSensors = sensorMap.size() ?: 0
	return sensorMap
        
}
        
     
def getThermostatDisplayName(stat) {
	if(stat?.name)
		return stat.name.toString()
	return (getThermostatTypeName(stat) + " (${stat.identifier})").toString()
}

def getThermostatTypeName(stat) {
	return stat.modelNumber == "siSmart" ? "Smart Si" : "Smart"
}

def installed() {
	LOG("Installed with settings: ${settings}", 4)	
	initialize()
}

def updated() {	
    LOG("Updated with settings: ${settings}", 4)	
	if( readyForAuthRefresh() ) {    
    	LOG("In update() - readyForAuthRefresh() returned true. Need to refresh the tokens.", 2, null, "error")
        refreshAuthToken(true)
    }

    initialize()
}

def initialize() {	
    LOG("=====> initialize()", 4)    
    
    state.connected = "full"        
    state.reAttempt = 0
    
     try {
		unsubscribe()
    	unschedule() // reset all the schedules
	} catch (Exception e) {
    	LOG("updated() - Exception encountered trying to unschedule(). Exception: ${e}", 2, null, "error")
    }    
    
    def nowTime = now()
    def nowDate = getTimestamp()
    
    // Initialize several variables    
	state.lastScheduledPoll = nowTime
    state.lastScheduledPollDate = nowDate
    state.lastScheduledTokenRefresh = nowTime
    state.lastScheduledTokenRefreshDate = nowDate
    state.lastScheduledWatchdog = nowTime
    state.lastScheduledWatchdogDate = nowDate
	state.lastPoll = nowTime
    state.lastPollDate = nowDate
    state.timeOfDay = "night" 
    
    state.lastWatchdog = nowTime
    
    def sunriseAndSunset = getSunriseAndSunset()
    state.sunriseTime = sunriseAndSunset.sunrise.format("HHmm", location.timeZone).toDouble()
    state.sunsetTime = sunriseAndSunset.sunset.format("HHmm", location.timeZone).toDouble()
	    
    // Setup initial polling and determine polling intervals
	state.pollingInterval = getPollingInterval()
    state.tokenGrace = 18 // Anything more than this then we have a possible failed 
    state.watchdogInterval = 15
    state.reAttemptInterval = 15 // In seconds
	
    if (state.initialized) {		
    	// refresh Thermostats and Sensor full lists
    	getEcobeeThermostats()
    	getEcobeeSensors()
    } 
    
    // getEcobeeThermostats()
	// getEcobeeSensors()
    
    // Children
    def aOK = true
	if (settings.thermostats?.size() > 0) { aOK = aOK && createChildrenThermostats() }
	if (settings.ecobeesensors?.size() > 0) { aOK = aOK && createChildrenSensors() }    
    deleteUnusedChildren()
   
	// Initial poll()
	// if (settings.thermostats?.size() > 0) { if (canSchedule()) { runIn(10, pollInit) } else { pollInit() } }
    if (settings.thermostats?.size() > 0) { pollInit() }

    // Add subscriptions as little "daemons" that will check on our health
    subscribe(location, "routineExecuted", scheduleWatchdog)
    subscribe(location, "sunset", sunsetEvent)
    subscribe(location, "sunrise", sunriseEvent)
    
    // Schedule the various handlers
    if (settings.thermostats?.size() > 0) { spawnDaemon("poll") } 
    spawnDaemon("watchdog")
    spawnDaemon("auth")
    
    // TODO: Add ability to add additional physical (or virtual) items to subscribe to that have events generated that could heal our app
    
    //send activity feeds to tell that device is connected
    def notificationMessage = aOK ? "is connected to SmartThings" : "had an error during setup of devices"
    sendActivityFeeds(notificationMessage)
    state.timeSendPush = null
    if (!state.initialized) {
    	state.initialized = true
        state.initializedEpic = nowTime
        state.initializedDate = nowDate
	}
        
    return aOK
}

private def createChildrenThermostats() {
	LOG("createChildrenThermostats() entered: thermostats=${settings.thermostats}", 5)
    // Create the child Thermostat Devices
	def devices = settings.thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
        	// TODO: Place in a try block and check for this exception: physicalgraph.app.exception.UnknownDeviceTypeException
            try {
				d = addChildDevice(app.namespace, getChildThermostatName(), dni, null, ["label":"EcoTherm: ${state.thermostatsWithNames[dni]}"])			
			} catch (physicalgraph.app.exception.UnknownDeviceTypeException e) {
            	LOG("You MUST add the ${getChildSensorName()} Device Handler to the IDE BEFORE running the setup.", 1, null, "error")
                return false
            }
            LOG("created ${d.displayName} with id $dni", 4)
		} else {
			LOG("found ${d.displayName} with id $dni already exists", 4)            
		}
		return d
	}    
    
    LOG("Created/Updated ${devices.size()} thermostats")    
    return true
}

private def createChildrenSensors() {
	LOG("createChildrenSensors() entered: ecobeesensors=${settings.ecobeesensors}", 5)
    // Create the child Ecobee Sensor Devices
	def sensors = settings.ecobeesensors.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
        	// TODO: Place in a try block and check for this exception: physicalgraph.app.exception.UnknownDeviceTypeException
            try {
				d = addChildDevice(app.namespace, getChildSensorName(), dni, null, ["label":"EcoSensor: ${state.eligibleSensors[dni]}"])
			} catch (physicalgraph.app.exception.UnknownDeviceTypeException e) {
            	LOG("You MUST add the ${getChildSensorName()} Device Handler to the IDE BEFORE running the setup.", 1, null, "error")
                return false
            }
            LOG("created ${d.displayName} with id $dni", 4)
		} else {
        	LOG("found ${d.displayName} with id $dni already exists", 4)
		}
		return d
	}
    
	LOG("Created/Updated ${sensors.size()} sensors.")
    return true
}

// NOTE: For this to work effectively getEcobeeThermostats() and getEcobeeSensors() should be called prior
private def deleteUnusedChildren() {
	LOG("deleteUnusedChildren() entered", 5)    
    
    if (settings.thermostats?.size() == 0) {
    	// No thermostats, need to delete all children
        LOG("Deleting All My Children!", 2, null, "warn")
    	getAllChildDevices().each { deleteChildDevice(it.deviceNetworkId) }        
    } else {
    	// Only delete those that are no longer in the list
        // This should be a combination of any removed thermostats and any removed sensors
        def allMyChildren = getAllChildDevices()
        LOG("These are currently all of my childred: ${allMyChildren}", 5, null, "debug")
        
        // Update list of "eligibleSensors"       
        def childrenToKeep = (thermostats ?: []) + (state.eligibleSensors?.keySet() ?: [])
        LOG("These are the children to keep around: ${childrenToKeep}", 4, null, "trace")
        
    	def childrenToDelete = allMyChildren.findAll { !childrenToKeep.contains(it.deviceNetworkId) }        
        
        LOG("Ready to delete these devices. ${childrenToDelete}", 4, null, "trace")
		if (childrenToDelete.size() > 0) childrenToDelete?.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)    
    }    
}
	

def sunriseEvent(evt) {
	LOG("sunriseEvent() - with evt (${evt})", 4, null, "info")
	state.timeOfDay = "day"
    state.lastSunriseEvent = now()
    state.lastSunriseEventDate = getTimestamp()
    state.sunriseTime = new Date().format("HHmm", location.timeZone).toInteger()
    scheduleWatchdog(evt, false)
    
}

def sunsetEvent(evt) {
	LOG("sunsetEvent() - with evt (${evt})", 4, null, "info")
	state.timeOfDay = "night"
    state.lastSunsetEvent = now()
    state.lastSunsetEventDate = getTimestamp()
    state.sunsetTime = new Date().format("HHmm", location.timeZone).toInteger()
    scheduleWatchdog(evt, false)
}

def scheduleWatchdog(evt=null, local=false) {
	def results = true
    LOG("scheduleWhatdog() called with: evt (${evt}) & local (${local})", 4, null, "trace")
    // Only update the Scheduled timestamp if it is not a local action or from a subscription
    if ( (evt == null) && (local==false) ) {
    	state.lastScheduledWatchdog = now()
        state.lastScheduledWatchdogDate = getTimestamp()
	}
    
    // Check to see if we have called too soon
    def timeSinceLastWatchdog = (now() - state.lastWatchdog) / 1000 / 60
    if ( timeSinceLastWatchdog < 1 ) {
    	LOG("It has only been ${timeSinceLastWatchdog} since last scheduleWatchdog was called. Please come back later.", 2, null, "trace")
        return
    }
    
    state.lastWatchdog = now()
    state.lastWatchdogDate = getTimestamp()
    
    LOG("After watchdog tagging")
	if(apiConnected() == "lost") {
    	// Possibly a false alarm? Check if we can update the token with one last fleeting try...
        if( refreshAuthToken(true) ) { 
        	// We are back in business!
			LOG("scheduleWatchdog() - Was able to recover the lost connection. Please ignore any notifications received.", 1, null, "error")
        } else {        
			LOG("scheduleWatchdog() - Unable toschedule handlers do to loss of API Connection. Please ensure you are authorized.", 1, null, "error")
			return false
		}
	}
    
	def pollAlive = isDaemonAlive("poll")
    def authAlive = isDaemonAlive("auth")
    def watchdogAlive = isDaemonAlive("watchdog")
    
    LOG("scheduleWatchdog() --> pollAlive==${pollAlive}  authAlive==${authAlive}  watchdogAlive==${watchdogAlive}", 4, null, "debug")
    
    // Reschedule polling if it has been a while since the previous poll    
    if (!pollAlive) { spawnDaemon("poll") }
    if (!authAlive) { spawnDaemon("auth") }
    if (!watchdogAlive) { spawnDaemon("watchdog") }
    
    return 
}

// Watchdog Handler
private def Boolean isDaemonAlive(daemon="all") {
	// Daemon options: "poll", "auth", "watchdog", "all"    
    def daemonList = ["poll", "auth", "watchdog", "all"]

	daemon = daemon.toLowerCase()
    def result = true    
    		
    def timeSinceLastScheduledPoll = (state.lastScheduledPoll == 0 || state.lastScheduledPoll == null) ? 0 : ((now() - state.lastScheduledPoll) / 1000 / 60)  // TODO: Removed toDouble() will this impact?
    def timeSinceLastScheduledRefresh = (state.lastScheduledTokenRefresh == 0 || state.lastScheduledTokenRefresh == null) ? 0 : ((now() - state.lastScheduledTokenRefresh) / 1000 / 60)
    def timeSinceLastScheduledWatchdog = (state.lastScheduledWatchdog == 0 || state.lastScheduledWatchdog == null) ? 0 : ((now() - state.lastScheduledWatchdog) / 1000 / 60)
	def timeBeforeExpiry = state.authTokenExpires ? ((state.authTokenExpires - now()) / 1000 / 60) : 0
    
    LOG("isDaemonAlive() - now() == ${now()} for daemon (${daemon})", 5, null, "trace")
    LOG("isDaemonAlive() - Time since last poll? ${timeSinceLastScheduledPoll} -- state.lastScheduledPoll == ${state.lastScheduledPoll}", 4, null, "info")
    LOG("isDaemonAlive() - Time since last token refresh? ${timeSinceLastScheduledRefresh} -- state.lastScheduledTokenRefresh == ${state.lastScheduledTokenRefresh}", 4, null, "info")
    LOG("isDaemonAlive() - Time since watchdog activation? ${timeSinceLastScheduledWatchdog} -- state.lastScheduledWatchdog == ${state.lastScheduledWatchdog}", 4, null, "info")
    LOG("isDaemonAlive() - Time left (timeBeforeExpiry) until expiry (in min): ${timeBeforeExpiry}", 4, null, "info")
        
    if (daemon == "poll" || daemon == "all") {
    	LOG("isDaemonAlive() - Checking daemon (${daemon}) in 'poll'", 4, null, "trace")
        def maxInterval = state.pollingInterval + 3
		// if ( (timeSinceLastScheduledPoll == 0) || (timeSinceLastScheduledPoll >= maxInterval) || (state.initialized != true) ) { result = false }
        if ( timeSinceLastScheduledPoll >= maxInterval ) { result = false }
	}	
    
    if (daemon == "auth" || daemon == "all") {
    	LOG("isDaemonAlive() - Checking daemon (${daemon}) in 'auth'", 4, null, "trace")
    	// if ( (timeSinceLastScheduledRefresh == 0)  || (timeSinceLastScheduledRefresh >= state.tokenGrace) || (state.initialized != true) ) { result = false }
        if ( timeSinceLastScheduledRefresh >= state.tokenGrace ) { result = false }
    } 
    
    if (daemon == "watchdog" || daemon == "all") {
    	LOG("isDaemonAlive() - Checking daemon (${daemon}) in 'watchdog'", 4, null, "trace")
        def maxInterval = state.watchdogInterval + 6
        //if ( (timeSinceLastScheduledWatchdog == 0) || (timeSinceLastScheduledWatchdog >= (maxInterval)) || (state.initialized != true) ) { result = false }
        LOG("isDaemonAlive(watchdog) - timeSinceLastScheduledWatchdog=(${timeSinceLastScheduledWatchdog})  Timestamps: (${state.lastScheduledWatchdogDate}) (epic: ${state.lastScheduledWatchdog}) now-(${now()})", 4, null, "trace")
        if ( timeSinceLastScheduledWatchdog >= maxInterval ) { result = false }
    }
    
	if (!daemonList.contains(daemon) ) {
    	// Unkown option passed in, gotta punt
        LOG("isDaemonAlive() - Unknown daemon: ${daemon} received. Do not know how to check this daemon.", 1, null, "error")
        result = false
    }
    LOG("isDaemonAlive() - result is ${result}", 4, null, "trace")
    return result
}

private def Boolean spawnDaemon(daemon="all") {
	// Daemon options: "poll", "auth", "watchdog", "all"    
    def daemonList = ["poll", "auth", "watchdog", "all"]
    
    daemon = daemon.toLowerCase()
    def result = true
    
    if (daemon == "poll" || daemon == "all") {
    	LOG("spawnDaemon() - Performing seance for daemon (${daemon}) in 'poll'", 4, null, "trace")
        // Reschedule the daemon
        try {
			// result = result && unschedule("pollScheduled")
            unschedule("pollScheduled")
            if ( canSchedule() ) { 
        		"runEvery${state.pollingInterval}Minutes"("pollScheduled")
                // if ( canSchedule() ) { runIn(30, "pollScheduled") }  // This will wipe out the existing scheduler!
                // Web Services taking too long. Go ahead and only schedule here for now
                
            	result = result && pollScheduled()
			} else {
            	LOG("canSchedule() is NOT allowed or result already false! Unable to schedule daemon!", 1, null, "error")
        		result = false
        	}
        } catch (Exception e) {
        	LOG("spawnDaemon() - Exception when performing spawn for ${daemon}. Exception: ${e}", 1, null, "error")
            result = result && false
        }		
    }
    
    if (daemon == "auth" || daemon == "all") {
    	LOG("spawnDaemon() - Performing seance for daemon (${daemon}) in 'auth'", 4, null, "trace")
		// Reschedule the daemon
        try {
			//result = result && unschedule("refreshAuthTokenScheduled")
            unschedule("refreshAuthTokenScheduled")
            if ( canSchedule() && result ) { 
            	LOG("canSchedule() is true. About to perform runEvery15Minutes for 'refreshAuthTokenScheduled'", 4, null, "debug")
        		runEvery15Minutes("refreshAuthTokenScheduled")
                // if ( canSchedule() ) { runIn(30, "refreshAuthTokenScheduled") }  // Don't count this against the results
                // Web Services taking too long. Go ahead and only schedule here for now
                
            	result = result && refreshAuthTokenScheduled()
			} else {
            	LOG("canSchedule() is NOT allowed or result already false! Unable to schedule daemon!", 1, null, "error")
        		result = false
        	}
        } catch (Exception e) {
        	LOG("spawnDaemon() - Exception when performing spawn for ${daemon}. Exception: ${e}", 1, null, "error")
            result = result && false
        }		
    }
    
    if (daemon == "watchdog" || daemon == "all") {
    	LOG("spawnDaemon() - Performing seance for daemon (${daemon}) in 'watchdog'", 4, null, "trace")
        // Reschedule the daemon
        try {
			// result = result && unschedule("scheduleWatchdog")
            unschedule("scheduleWatchdog")
            if ( canSchedule() && result ) { 
        		runEvery15Minutes("scheduleWatchdog")
            	result = result && true
			} else {
            	LOG("canSchedule() is NOT allowed or result already false! Unable to schedule daemon!", 1, null, "error")
        		result = false
        	}
        } catch (Exception e) {
        	LOG("spawnDaemon() - Exception when performing spawn for ${daemon}. Exception: ${e}", 1, null, "error")
            result = result && false
        }		
    }
    
    if (!daemonList.contains(daemon) ) {
    	// Unkown option passed in, gotta punt
        LOG("isDaemonAlive() - Unknown daemon: ${daemon} received. Do not know how to check this daemon.", 1, null, "error")
        result = false
    }
    return result
}


def updateLastPoll(Boolean isScheduled=false) {
	if (isScheduled) {
    	state.lastScheduledPoll = now()
        state.lastScheduledPollDate =  getTimestamp()
    } else {
    	state.lastPoll = now()
        state.lastPollDate = getTimestamp()
    }
}

// Called by scheduled() event handler
def pollScheduled() {
	updateLastPoll(true)
	LOG("pollScheduled() - Running at ${state.lastScheduledPollDate} (epic: ${state.lastScheduledPoll})", 3, null, "trace")    
    return poll()
}


def updateLastTokenRefresh(isScheduled=false) {
	if (isScheduled) {    	
    	state.lastScheduledTokenRefresh = now()
        state.lastScheduledTokenRefreshDate =  getTimestamp()
        LOG("updateLastTokenRefresh(true) - Updated timestamps: state.lastScheduledTokenRefreshDate=(${state.lastScheduledTokenRefreshDate})  state.lastScheduledTokenRefresh=${state.lastScheduledTokenRefresh} ", 5, null, "trace")
    } else {
    	state.lastTokenRefresh = now()
        state.lastTokenRefreshDate = getTimestamp()      
    }
}

// Called by scheduled() event handler
def refreshAuthTokenScheduled() {
	updateLastTokenRefresh(true)
    LOG("refreshAuthTokenScheduled() - Running at ${state.lastScheduledTokenRefreshDate} (epic: ${state.lastScheduledTokenRefresh})", 3, null, "trace")    
    
    def result  = refreshAuthToken()
    return result    
}


// Called during initialization to get the inital poll
def pollInit() {
	LOG("pollInit()", 5)
    state.forcePoll = true // Initialize the variable and force a poll even if there was one recently    
	pollChildren(null) // Hit the ecobee API for update on all thermostats
}


def pollChildren(child = null) {
	def results = true   
    
	LOG("=====> pollChildren() - state.forcePoll(${state.forcePoll})  state.lastPoll(${state.lastPoll})  now(${now()})  state.lastPollDate(${state.lastPollDate})", 4, child, "trace")
    
	if(apiConnected() == "lost") {
    	// Possibly a false alarm? Check if we can update the token with one last fleeting try...
        LOG("apiConnected() == lost, try to do a recovery, else we are done...", 3, child, "debug")
        if( refreshAuthToken() ) { 
        	// We are back in business!
			LOG("pollChildren() - Was able to recover the lost connection. Please ignore any notifications received.", 1, child, "error")
        } else {        
			LOG("pollChildren() - Unable toschedule handlers do to loss of API Connection. Please ensure you are authorized.", 1, child, "error")
			return false
		}
	}

    // Run a watchdog checker here
    scheduleWatchdog(null, true)    
    
    if (settings.thermostats?.size() < 1) {
    	LOG("pollChildren() - Nothing to poll as there are no thermostats currently selected", 1, child, "warn")
		return true
    }    
    
    
   // Check to see if it is time to do an full poll to the Ecobee servers. If so, execute the API call and update ALL children
    def timeSinceLastPoll = (state.forcePoll == true) ? 0 : ((now() - state.lastPoll?.toDouble()) / 1000 / 60) 
    LOG("Time since last poll? ${timeSinceLastPoll} -- state.lastPoll == ${state.lastPoll}", 3, child, "info")
    
    if ( (state.forcePoll == true) || ( timeSinceLastPoll > getMinMinBtwPolls().toDouble() ) ) {
    	// It has been longer than the minimum delay
        LOG("Calling the Ecobee API to fetch the latest data...", 4, child)
    	pollEcobeeAPI(getChildThermostatDeviceIdsString())  // This will update the values saved in the state which can then be used to send the updates
	} else {
        LOG("pollChildren() - Not time to call the API yet. It has been ${timeSinceLastPoll} minutes since last full poll.", 4, child)
        generateEventLocalParams() // Update any local parameters and send
    }
	
	// Iterate over all the children
	def d = getChildDevices()
    d?.each() { oneChild ->
    	LOG("pollChildren() - Processing poll data for child: ${oneChild} has ${oneChild.capabilities}", 4)
        
    	if( oneChild.hasCapability("Thermostat") ) {
        	// We found a Thermostat, send all of its events
            LOG("pollChildren() - We found a Thermostat!", 5)
            oneChild.generateEvent(state.thermostats[oneChild.device.deviceNetworkId]?.data)
        } else {
        	// We must have a remote sensor
            LOG("pollChildren() - Updating sensor data for ${oneChild}: ${oneChild.device.deviceNetworkId} data: ${state.remoteSensorsData[oneChild.device.deviceNetworkId]?.data}", 4)
            oneChild.generateEvent(state.remoteSensorsData[oneChild.device.deviceNetworkId]?.data)
        } 
    }
    return results
}

private def generateEventLocalParams() {
	// Iterate over all the children
    LOG("Entered generateEventLocalParams() ", 5)
	def d = getChildDevices()
    d?.each() { oneChild ->
    	LOG("generateEventLocalParams() - Processing poll data for child: ${oneChild} has ${oneChild.capabilities}", 4)
        
    	if( oneChild.hasCapability("Thermostat") ) {
        	// We found a Thermostat, send local params as events
            LOG("generateEventLocalParams() - We found a Thermostat!")
            def data = [
            	apiConnected: apiConnected()
            ]
            
            state.thermostats[oneChild.device.deviceNetworkId].data?.apiConnected = apiConnected()            
            oneChild.generateEvent(data)
        } else {
        	// We must have a remote sensor
            LOG("generateEventLocalParams() - Updating sensor data: ${oneChild.device.deviceNetworkId}")
			// No local params to send            
        } 
    }

}

private def pollEcobeeAPI(thermostatIdsString = "") {
	LOG("=====> pollEcobeeAPI() entered - thermostatIdsString = ${thermostatIdsString}", 2, null, "info")
	state.forcePoll = false

	// TODO: Check on any running EVENTs on thermostat

	// def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeExtendedRuntime":"false","includeSettings":"true","includeRuntime":"true","includeEquipmentStatus":"true","includeSensors":true,"includeWeather":true,"includeProgram":true}}'
	def jsonRequestBody = buildBodyRequest("thermostatInfo", null, thermostatIdsString, null).toString()
    
    LOG("buildBodyRequest returned: ${jsonRequestBody}", 5)
    
    def result = false
	
	def pollParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${state.authToken}"],
			query: [format: 'json', body: jsonRequestBody]
	]

	try{
		httpGet(pollParams) { resp ->
			if(resp.status == 200) {
				LOG("poll results returned resp.data ${resp.data}", 2)
				state.remoteSensors = resp.data.thermostatList.remoteSensors
				state.thermostatData = resp.data
               
                // Create the list of sensors and related data
				updateSensorData()
                // Create the list of thermostats and related data
				updateThermostatData()                
				result = true
                
                if (apiConnected() != "full") {
					apiRestored()
                    generateEventLocalParams() // Update the connection status
                }
                updateLastPoll()
				LOG("httpGet: updated ${state.thermostats?.size()} stats: ${state.thermostats}")
			} else {
				LOG("pollEcobeeAPI() - polling children & got http status ${resp.status}", 1, null, "error")

				//refresh the auth token
				if (resp.status == 500 && resp.data.status.code == 14) {
					LOG("Resp.status: ${resp.status} Status Code: ${resp.data.status.code}. Unable to recover", 1, null, "error")
                    // Should not possible to recover from a code 14 but try anyway?
                    
                    apiLost("pollEcobeeAPI() - Resp.status: ${resp.status} Status Code: ${resp.data.status.code}. Unable to recover.")
				}
				else {
					LOG("pollEcobeeAPI() - Other responses received. Resp.status: ${resp.status} Status Code: ${resp.data.status.code}.", 1, null, "error")
				}
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {    
		LOG("pollEcobeeAPI() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}", 1, null, "error")
        result = false
        state.reAttemptPoll = state.reAttemptPoll + 1
        if (state.reAttemptPoll > 3) {        
        	apiLost("Too many retries (${state.reAttemptPoll - 1}) for polling.")
            return false
        } else {
        	LOG("Setting up retryPolling")
			def reAttemptPeriod = 15 // in sec
        	if ( canSchedule() ) {
            	runIn(state.reAttemptInterval, "retryPolling") 
			} else { 
            	LOG("Unable to schedule retryPolling, running directly")
            	retryPolling() 
            }
        }
        
        /*
		if ( (e.statusCode == 500 && e.getResponse()?.data.status.code == 14) ||  (e.statusCode == 401 && e.getResponse()?.data.status.code == 14) ) {
        	// Not possible to recover from status.code == 14
            LOG("In HttpResponseException: Received data.stat.code of 14", 1, null, "error")
            if ( refreshAuthToken(true) ) { 
            	LOG("We have recovered the token a from the code 14.", 2, null, "warn") 
                pollChildren()
			} else { 
            	LOT("Unable to recover from error even after refreshAuthToken called", 2, null, "warn")             
        		apiLost("pollEcobeeAPI() - In HttpResponseException: Received data.stat.code of 14") 
			}
		} else if (e.statusCode != 401) { //this issue might comes from exceed 20sec app execution, connectivity issue etc.
        	
            LOG("In HttpResponseException - statusCode != 401 (${e.statusCode})", 1, null, "warn")
            state.connected = "warn"
            generateEventLocalParams() // Update the connected state at the thermostat devices
            if ( refreshAuthToken(true) ) { pollChildren() }
		} else if (e.statusCode == 401) { // Status.code other than 14			
			LOG("statusCode == 401: will try to refreshAuthToken", 1, null, "warn")
			state.connected = "warn"
			generateEventLocalParams() // Update the connected state at the thermostat devices
			if ( refreshAuthToken(true) ) { pollChildren() }			
		} 
        */
    } catch (java.util.concurrent.TimeoutException e) {
		LOG("pollEcobeeAPI(), TimeoutException: ${e}.", 1, null, "warn")
        
	} catch (Exception e) {
    	LOG("pollEcobeeAPI(): General Exception: ${e}.", 1, null, "error")
    }
    LOG("<===== Leaving pollEcobeeAPI() results: ${result}", 5)
	return result
    
}

// Used after an HTTP Exception 
def retryPolling() {
	LOG("retryPolling() entered", 2, null, "trace")
    if ( refreshAuthToken(true) ) {
    	LOG("retryPolling() - refreshAuthToken() was successful. Will reattempt to poll children.", 2, null, "trace")
    	state.reAttempt = 0  // Refresh Auth success, reset retries
        state.forcePoll = true        
        
        if ( pollChildren() ) {
        	state.reAttemptPoll = 0
        	apiRestored()
        }
    }    
}

// poll() will be called on a regular interval using a runEveryX command
def poll() {	
    LOG("poll() - Running at ${state.lastPollDate} (epic: ${state.lastPoll})", 3, null, "trace")

    // Check to see if we are connected to the API or not
    if (apiConnected() == "lost") {
    	LOG("poll() - Attempting to recover poll() due to lost API Connection", 1, null, "warn")
        scheduleWatchdog(null, true)
        if ( refreshAuthToken() ) {
        	// We were able to recover
            apiRestored()
            LOG("poll() - we were able to recover the connection!", 1, null, "info")
        } else {
        	LOG("poll() - we were unable to recover the connection.", 2, null, "error")
            return false
        }
    }    
	
	LOG("poll() - Polling children with pollChildren(null)", 4)
	return pollChildren(null) // Poll ALL the children at the same time for efficiency    
}


def availableModes(child) {	
	def tData = state.thermostats[child.device.deviceNetworkId]
    LOG("state.thermostats = ${state.thermostats}", 3, child)
	LOG("Child DNI = ${child.device.deviceNetworkId}", 3, child)
	LOG("Data = ${tData}", 3, child)
	

	if(!tData)
	{
		LOG("ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling", 1, child, "error")

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}

	def modes = ["off"]

	if (tData.data.heatMode) modes.add("heat")
	if (tData.data.coolMode) modes.add("cool")
	if (tData.data.autoMode) modes.add("auto")
    // TODO: replace the use of auxHeatOnly with "emergency heat" to conform to the thermostatMode attributes allowed values
    // if (tData.data.auxHeatMode) modes.add("emergency heat")
	if (tData.data.auxHeatMode) modes.add("auxHeatOnly")

	modes

}

def currentMode(child) {
	def tData = state.thermostats[child.device.deviceNetworkId]
	LOG("state.thermostats = ${state.thermostats}", 3, child)
	LOG("Child DNI = ${child.device.deviceNetworkId}", 3, child)
	LOG("Data = ${tData}", 3, child)
    

	if(!tData) {
		LOG("ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling", 1, child, "error")

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}

	def mode = tData.data.thermostatMode

	mode
}

def updateSensorData() {
	LOG("Entered updateSensorData() ", 5)
 	def sensorCollector = [:]
                
	state.remoteSensors.each {
		it.each {
			if ( ( it.type == "ecobee3_remote_sensor" ) || (it.type == "control_sensor") || ((it.type == "thermostat") && (settings.showThermsAsSensor)) ) {
				// Add this sensor to the list
				def sensorDNI 
                if (it.type == "ecobee3_remote_sensor") { 
                	sensorDNI = "ecobee_sensor-" + it?.id + "-" + it?.code 
				} else if (it.type == "control_sensor") {
                	LOG("We have a Smart SI style control_sensor! it=${it}", 4, null, "trace")
                    sensorDNI = "control_sensor-" + it?.id 
                } else { 
                	LOG("We have a Thermostat based Sensor! it=${it}", 4, null, "trace")
                	sensorDNI = "ecobee_sensor_thermostat-"+ it?.id + "-" + it?.name
				}
				LOG("sensorDNI == ${sensorDNI}", 4)
            	                
				def temperature = ""
				def occupancy = ""
                            
				it.capability.each { cap ->
					if (cap.type == "temperature") {
                    	LOG("updateSensorData() - Sensor (DNI: ${sensorDNI}) temp is ${cap.value}", 4)
                        if ( cap.value.isNumber() ) { // Handles the case when the sensor is offline, which would return "unknown"
							temperature = cap.value as Double
							temperature = wantMetric() ? (temperature / 10).toDouble().round(1) : (temperature / 10).toDouble().round(0)
                        } else if (cap.value == "unknown") {
                        	// TODO: Do something here to mark the sensor as offline?
                            LOG("updateSensorData() - sensor (DNI: ${sensorDNI}) returned unknown temp value. Perhaps it is unreachable.", 1, null, "warn")
                            // Need to mark as offline somehow
                            temperature = "unknown"
                            
                        } else {
                        	 LOG("updateSensorData() - sensor (DNI: ${sensorDNI}) returned ${cap.value}.", 1, null, "error")
                        }
					} else if (cap.type == "occupancy") {
						if(cap.value == "true") {
							occupancy = "active"
            	        } else if (cap.value == "unknown") {
                        	// Need to mark as offline somehow
                            LOG("Setting sensor occupancy to unknown", 2, null, "warn")
                            occupancy = "unknown"
                        } else {
							occupancy = "inactive"
						}
                            
					}
				}
                                            				
				def sensorData = [
					temperature: ((temperature == "unknown") ? "unknown" : myConvertTemperatureIfNeeded(temperature, "F", 1))					
				]
                if (occupancy != "") {
                	sensorData << [ motion: occupancy ]
                }
				sensorCollector[sensorDNI] = [data:sensorData]
                LOG("sensorCollector being updated with sensorData: ${sensorData}", 4)
                
			} else if ( (it.type == "thermostat") && (settings.showThermsAsSensor) ) { 
            	// Also update the thermostat based Remote Sensor??
                // Don't think this is needed as we incorporated it directly in the if above
                
            
            } // end thermostat else if
		} // End it.each loop
	} // End remoteSensors.each loop
	state.remoteSensorsData = sensorCollector
	LOG("updateSensorData(): found these remoteSensors: ${sensorCollector}", 4)
                
}

def updateThermostatData() {
	state.timeOfDay = getTimeOfDay()
	
	// Create the list of thermostats and related data
	state.thermostats = state.thermostatData.thermostatList.inject([:]) { collector, stat ->
		def dni = [ app.id, stat.identifier ].join('.')

		LOG("Updating dni $dni, Got weather? ${stat.weather.forecasts[0].weatherSymbol.toString()}", 4)
        LOG("Climates available: ${stat.program?.climates}", 4)
        // Extract Climates
        def climateData = stat.program?.climates
        
        // TODO: Put a wrapper here based on the thermostat brand
        def thermSensor = stat.remoteSensors.find { it.type == "thermostat" }
        def occupancy = "not supported"
        if(!thermSensor) {
		LOG("This particular thermostat does not have a built in remote sensor", 4)
		state.hasInternalSensors = false
        } else {
        	state.hasInternalSensors = true
		LOG("updateThermostatData() - thermSensor == ${thermSensor}" )
        
		def occupancyCap = thermSensor?.capability.find { it.type == "occupancy" }
		LOG("updateThermostatData() - occupancyCap = ${occupancyCap} value = ${occupancyCap.value}")
        
		// Check to see if there is even a value, not all types have a sensor
		occupancy =  occupancyCap.value ?: "not supported"
        }
        LOG("Program data: ${stat.program}  Current climate (ref): ${stat.program?.currentClimateRef}", 4)
        
        // Determine if an Event is running, find the first running event
        def runningEvent = null
        
        if ( stat.events.size() > 0 ) {         
        	runningEvent = stat.events.find { 
            	LOG("Checking event: ${it}", 5) 
                it.running == true
            }        	
        }        
        
        def usingMetric = wantMetric() // cache the value to save the function calls
        def tempTemperature = myConvertTemperatureIfNeeded( (stat.runtime.actualTemperature.toDouble() / 10), "F", (usingMetric ? 1 : 0))
        def tempHeatingSetpoint = myConvertTemperatureIfNeeded( (stat.runtime.desiredHeat.toDouble() / 10), "F", (usingMetric ? 1 : 0))
        def tempCoolingSetpoint = myConvertTemperatureIfNeeded( (stat.runtime.desiredCool.toDouble() / 10), "F", (usingMetric ? 1 : 0))
        def tempWeatherTemperature = myConvertTemperatureIfNeeded( ((stat.weather.forecasts[0].temperature.toDouble() / 10)), "F", (usingMetric ? 1 : 0))
                
        def currentClimateName = ""
		def currentClimateId = ""
        def currentFanMode = ""
                 
        if (runningEvent) {         	
            LOG("Found a running Event: ${runningEvent}", 4) 
            def tempClimateRef = runningEvent.holdClimateRef ?: ""
        	if ( runningEvent.type == "hold" ) {
               	currentClimateName = "Hold" + (runningEvent.holdClimateRef ? ": ${runningEvent.holdClimateRef.capitalize()}" : "")
			} else if (runningEvent.type == "vacation" ) {
               	currentClimateName = "Vacation"
            } else if (runningEvent.type == "quickSave" ) {
               	currentClimateName = "Quick Save"                
            } else if (runningEvent.type == "autoAway" ) {
             	currentClimateName = "Auto Away"
            } else if (runningEvent.type == "autoHome" ) {
               	currentClimateName = "Auto Home"
            } else {                
               	currentClimateName = runningEvent.type
            }
            currentClimateId = runningEvent.holdClimateRef
		} else if (stat.program?.currentClimateRef) {
        	currentClimateName = (stat.program.climates.find { it.climateRef == stat.program.currentClimateRef }).name
        	currentClimateId = stat.program.currentClimateRef            
        } else {
        	LOG("updateThermostatData() - No climateRef or running Event was found", 1, null, "error")
            currentClimateName = ""
        	currentClimateId = ""        	
        }
        
        LOG("currentClimateName set = ${currentClimateName}  currentClimateId set = ${currentClimateId}")
        
        
        if (runningEvent) {
        	currentFanMode = circulateFanModeOn ? "circulate" : runningEvent.fan
        } else {
        	currentFanMode = stat.runtime.desiredFanMode
        }
     
        
	if (state.hasInternalSensors) { occupancy = (occupancy == "true") ? "active" : "inactive" }

	def data = [ 
		temperatureScale: getTemperatureScale(),
		apiConnected: apiConnected(),
		coolMode: (stat.settings.coolStages > 0),
		heatMode: (stat.settings.heatStages > 0),
		autoMode: stat.settings.autoHeatCoolFeatureEnabled,
		currentProgramName: currentClimateName,
		currentProgramId: currentClimateId,
		auxHeatMode: (stat.settings.hasHeatPump) && (stat.settings.hasForcedAir || stat.settings.hasElectric || stat.settings.hasBoiler),
		temperature: usingMetric ? tempTemperature : tempTemperature.toInteger(),
		heatingSetpoint: usingMetric ? tempHeatingSetpoint : tempHeatingSetpoint.toInteger(),
		coolingSetpoint: usingMetric ? tempCoolingSetpoint : tempCoolingSetpoint.toInteger(),
		thermostatMode: stat.settings.hvacMode,
		thermostatFanMode: currentFanMode,
		humidity: stat.runtime.actualHumidity,
		motion: occupancy,
		thermostatOperatingState: getThermostatOperatingState(stat),
        timeOfDay: state.timeOfDay,
		weatherSymbol: stat.weather.forecasts[0].weatherSymbol.toString(),
		weatherTemperature: usingMetric ? tempWeatherTemperature : tempWeatherTemperature.toInteger()
	]
       
		data["temperature"] = data["temperature"] ? ( wantMetric() ? data["temperature"].toDouble() : data["temperature"].toDouble().toInteger() ) : data["temperature"]
		data["heatingSetpoint"] = data["heatingSetpoint"] ? ( wantMetric() ? data["heatingSetpoint"].toDouble() : data["heatingSetpoint"].toDouble().toInteger() ) : data["heatingSetpoint"]
		data["coolingSetpoint"] = data["coolingSetpoint"] ? ( wantMetric() ? data["coolingSetpoint"].toDouble() : data["coolingSetpoint"].toDouble().toInteger() ) : data["coolingSetpoint"]
		data["weatherTemperature"] = data["weatherTemperature"] ? ( wantMetric() ? data["weatherTemperature"].toDouble() : data["weatherTemperature"].toDouble().toInteger() ) : data["weatherTemperature"]
        
		
		LOG("Event Data = ${data}", 4)

		collector[dni] = [data:data,climateData:climateData]
		return collector
	}
				
}


def getThermostatOperatingState(stat) {

	def equipStatus = (stat.equipmentStatus.size() > 0) ? stat.equipmentStatus : 'Idle'	
	equipStatus = equipStatus.trim().toUpperCase()
    
    LOG("getThermostatOperatingState() - equipStatus == ${equipStatus}", 4)
    
	def currentOpState = equipStatus.contains('HEAT')? 'heating' : (equipStatus.contains('COOL')? 'cooling' : 
    	equipStatus.contains('FAN')? 'fan only': 'idle')
	return currentOpState
}


def getChildThermostatDeviceIdsString(singleStat = null) {
	if(!singleStat) {
    	LOG("getChildThermostatDeviceIdsString() - !singleStat returning the list for all thermostats", 4, null, "info")
		return thermostats.collect { it.split(/\./).last() }.join(',')
	} else {
    	// Only return the single thermostat
        def ecobeeDevId = singleStat.device.deviceNetworkID.split(/\./).last()
        LOG("Received a single thermostat, returning the Ecobee Device ID as a String: ${ecobeeDevId}", 4, null, "info")
        return ecobeeDevId    	
    }
}

/* 
def toJson(Map m) {
	return new org.json.JSONObject(m).toString()
}
*/ // Pending delete if not used anywhere

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private def Boolean refreshAuthToken(force=false) {
	// Update the timestamp
    updateLastTokenRefresh()
    
	if(!state.refreshToken) {
    	LOG("refreshing auth token", 2)	
		LOG("refreshAuthToken() - There is no refreshToken stored! Unable to refresh OAuth token.", 1, null, "error")
    	apiLost("refreshAuthToken() - No refreshToken")
        return false
    } else if ( (force != true) && !readyForAuthRefresh() ) {
    	// Not ready to refresh yet
        LOG("refreshAuthToken() - Not time to refresh yet, there is still time left before expiration.")
    	return true
    } else {
		LOG("Performing a refreshAuthToken(${force})", 4)
        
        def refreshParams = [
                method: 'POST',
                uri   : apiEndpoint,
                path  : "/token",
                query : [grant_type: 'refresh_token', code: "${state.refreshToken}", client_id: smartThingsClientId],
        ]

        LOG("refreshParams = ${refreshParams}")

        try {
            def jsonMap
            httpPost(refreshParams) { resp ->
				LOG("Inside httpPost resp handling.", 3, null, "debug")
                if(resp.status == 200) {
                    LOG("refreshAuthToken() - 200 Response received - Extracting info." )
                    
                    jsonMap = resp.data // Needed to work around strange bug that wasn't updating state when accessing resp.data directly
                    LOG("resp.data = ${resp.data} -- jsonMap is? ${jsonMap}")

                    if(jsonMap) {
                        LOG("resp.data == ${resp.data}, jsonMap == ${jsonMap}")
						
                        state.refreshToken = jsonMap.refresh_token
                        
                        // TODO - Platform BUG: This was not updating the state values for some reason if we use resp.data directly??? 
                        // 		  Workaround using jsonMap for authToken                       
                        LOG("state.authToken before: ${state.authToken}")
                        def oldAuthToken = state.authToken
                        state.authToken = jsonMap?.access_token  
						LOG("state.authToken after: ${state.authToken}")
                        if (oldAuthToken == state.authToken) { 
                        	LOG("WARN: state.authToken did NOT update properly! This is likely a transient problem.", 1, null, "warn")
                            state.connected = "warn"
							generateEventLocalParams() // Update the connected state at the thermostat devices
						}

                        
                        // Save the expiry time to optimize the refresh
                        LOG("Expires in ${resp?.data?.expires_in} seconds")
                        state.authTokenExpires = (resp?.data?.expires_in * 1000) + now()
                        LOG("Updated state.authTokenExpires = ${state.authTokenExpires}", 4, null, "trace")

						LOG("Refresh Token = state =${state.refreshToken}  == in: ${resp?.data?.refresh_token}")
                        LOG("OAUTH Token = state ${state.authToken} == in: ${resp?.data?.access_token}")
                        

                        if(state.action && state.action != "") {
                            LOG("Token refreshed. Executing next action: ${state.action}")

                            "${state.action}"()

                            // Reset saved action
                            state.action = ""
                        }

                    } else {
                    	LOG("No jsonMap??? ${jsonMap}", 2)
                    }
                    state.action = ""
                    apiRestored()                    
                    generateEventLocalParams() // Update the connected state at the thermostat devices
                    return true
                } else {
                    LOG("Refresh failed ${resp.status} : ${resp.status.code}!", 1, null, "error")
					state.connected = "warn"
                    generateEventLocalParams() // Update the connected state at the thermostat devices
                    return false
                }
            }
        } catch (groovyx.net.http.HttpResponseException e) {
        	LOG("refreshAuthToken() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}", 1, null, "error")
           	def reAttemptPeriod = 300 // in sec
			if ( (e.statusCode == 500 && e.getResponse()?.data.status.code == 14) || (e.statusCode == 401 && e.getResponse()?.data.status.code == 14) ) {
            	LOG("refreshAuthToken() - Received data.status.code = 14", 1, null, "error")
            	apiLost("refreshAuthToken() - Received data.status.code = 14" )
                return false
            } else if (e.statusCode != 401) { //this issue might comes from exceed 20sec app execution, connectivity issue etc.
            	LOG("refreshAuthToken() - e.statusCode: ${e.statusCode}", 1, null, "warn")
            	state.connected = "warn"
            	generateEventLocalParams() // Update the connected state at the thermostat devices
				if(canSchedule()) { runIn(reAttemptPeriod, "refreshAuthToken") } else { refreshAuthTokens(true) }
                return false
			} else if (e.statusCode == 401) { // status.code other than 14
				state.reAttempt = state.reAttempt + 1
				LOG("reAttempt refreshAuthToken to try = ${state.reAttempt}", 1, null, "warn")
				if (state.reAttempt <= 3) {
                	state.connected = "warn"
            		generateEventLocalParams() // Update the connected state at the thermostat devices
					if(canSchedule()) { runIn(reAttemptPeriod, "refreshAuthToken") } else { refreshAuthToken(true) }
                    return false
				} else {
                	// More than 3 attempts, time to give up and notify the end user
                    LOG("More than 3 attempts to refresh tokens. Giving up", 1, null, "error")
                    debugEvent("More than 3 attempts to refresh tokens. Giving up")
                	apiLost("refreshAuthToken() - More than 3 attempts to refresh token. Have to give up")
                    return false
				}
            }
        } catch (java.util.concurrent.TimeoutException e) {
			LOG("refreshAuthToken(), TimeoutException: ${e}.", 1, null, "error")
			// Likely bad luck and network overload, move on and let it try again
            state.connected = "warn"
            generateEventLocalParams() // Update the connected state at the thermostat devices
			def reAttemptPeriod = 300 // in sec
			if(canSchedule()) { runIn(reAttemptPeriod, "refreshAuthToken") } else { refreshAuthTokens(true) }            
            return false
        } catch (groovy.lang.StringWriterIOException e) {
        	LOG("refreshAuthToken(), groovy.lang.StringWriterIOException encountered: ${e}", 1, null, "error")
          	apiLost("refreshAuthToken(), groovy.lang.StringWriterIOException encountered: ${e}")
            return false
        } catch (Exception e) {
        	LOG("refreshAuthToken(), General Exception: ${e}.", 1, null, "error")
            apiLost("refreshAuthToken(), General Exception: ${e}.")
            return false
        }
    }
}



def resumeProgram(child, deviceId) {
	LOG("Entered resumeProgram for deviceID: ${deviceID}", 5, child)

	def jsonRequestBody = buildBodyRequest('resumeProgram',null,deviceId,null,null).toString()
	LOG("jsonRequestBody = ${jsonRequestBody}", 4, child)
    
	def result = sendJson(jsonRequestBody)
    LOG("resumeProgram(child) with result ${result}", 3, child)    

    return result
}

def setHold(child, heating, cooling, deviceId, sendHoldType=null, fanMode="", extraParams=[]) {
	int h = (getTemperatureScale() == "C") ? (cToF(heating) * 10) : (heating * 10)
	int c = (getTemperatureScale() == "C") ? (cToF(cooling) * 10) : (cooling * 10)
    
	LOG("setHold(): setpoints____ - h: ${heating} - ${h}, c: ${cooling} - ${c}, setHoldType: ${sendHoldType}", 3, child)
    
    
	def tstatSettings = ((sendHoldType != null) && (sendHoldType != "")) ?
		[coolHoldTemp:"${c}", heatHoldTemp: "${h}", holdType:"${sendHoldType}"
		] :
		[coolHoldTemp:"${c}", heatHoldTemp: "${h}"
		]		

	if (fanMode != "") { 
		tstatSettings << [fan:"${fanMode}"] 
	}
        
    if (extraParams != []) {
    	tstatSettings << extraParams
    }                
    
	//def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"thermostat": {"settings":{"hvacMode":"'+"${mode}"+'"}}}'
	def jsonRequestBody = buildBodyRequest('setHold',null,deviceId,tstatSettings,null).toString()
	//LOG("Mode Request Body = ${jsonRequestBody}", 4, child)    
	
	//def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": '+sendHoldType+' } } ]}'
    LOG("about to sendJson with jsonRequestBody (${jsonRequestBody}", 4, child)

    
	def result = sendJson(child, jsonRequestBody)
    LOG("setHold: heating: ${h}, cooling: ${c} with result ${result}", 3, child)
    return result
}

def setMode(child, mode, deviceId) {
	LOG("setMode() to ${mode} with DeviceId: ${deviceId}", 5, child)
    
	def tstatSettings 
    tstatSettings = [hvacMode:"${mode}"]
        
	//def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"thermostat": {"settings":{"hvacMode":"'+"${mode}"+'"}}}'
	def jsonRequestBody = buildBodyRequest('setThermostatSettings',null,deviceId,null,tstatSettings).toString()
	LOG("Mode Request Body = ${jsonRequestBody}", 4, child)
    

	def result = sendJson(jsonRequestBody)
    LOG("setMode to ${mode} with result ${result}", 4, child)
	if (result) {
    	child.generateQuickEvent("thermostatMode", mode, 15)
    } else {
    	LOG("Unable to set new mode (${mode})", 1, child, "warn")
    }

	return result
}

def setFanMode(child, fanMode, deviceId, sendHoldType=null) {
	LOG("setFanMode() to ${fanMode} with DeviceID: ${deviceId}", 5, child)
    
    def extraParams = [isTemperatureRelative: "false", isTemperatureAbsolute: "false"]
    // TODO: Set the fan mode to circulate in the events data sent to the device
    if (fanMode == "circulate") {    	
    	fanMode = "auto"        
        LOG("fanMode == 'circulate'", 5, child, "trace")        
        // Add a minimum circulate time here
        // NOTE: This is not currently honored by the Ecobee
        extraParams << [fanMinOnTime:15]
		state.circulateFanModeOn = true
    } else if (fanMode == "off") {
    	state.circulateFanModeOn = false    
        fanMode = "auto"
        // NOTE: This is not currently honored by the Ecobee
        extraParams << [fanMinOnTime: "0"]
    } else {
		state.circulateFanModeOn = false    
    }

    def currentHeatingSetpoint = child.device.currentValue("heatingSetpoint")
    def currentCoolingSetpoint = child.device.currentValue("coolingSetpoint")
    def holdType = sendHoldType ?: whatHoldType()
    
    LOG("about to call setHold: ${currentHeatingSetpoint}, ${currentCoolingSetpoint}, ${deviceId}, ${holdType}, ${fanMode}, ${extraParams}", 5, child, "trace")
    return setHold(child, currentHeatingSetpoint, currentCoolingSetpoint, deviceId, holdType, fanMode, extraParams)
    
}

def setProgram(child, program, deviceId, sendHoldType=null) {
	LOG("setProgram() to ${program} with DeviceID: ${deviceId}", 5, child)
    program = program.toLowerCase()

	def tstatSettings 
    tstatSettings = ((sendHoldType != null) && (sendHoldType != "")) ?
				[holdClimateRef:"${program}", holdType:"${sendHoldType}"
				] :
				[holdClimateRef:"${program}"
				]
    
	def jsonRequestBody = buildBodyRequest('setHold',null,deviceId,tstatSettings,null).toString()
    LOG("about to sendJson with jsonRequestBody (${jsonRequestBody}", 4, child)    
	def result = sendJson(child, jsonRequestBody)	
    LOG("setProgram with result ${result}", 3, child)
    dirtyPollData()
    return result
}


// API Helper Functions
private def sendJson(child = null, String jsonBody) {
	// Reset the poll timer to allow for an immediate refresh
	dirtyPollData()
    
	def returnStatus = false
	def cmdParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${state.authToken}"],
			body: jsonBody
	]

	try{
    	def statusCode = true
        int j=0
        
        while ( (statusCode) && (j++ < 2) ) { // only retry once
			httpPost(cmdParams) { resp ->
    	    	statusCode = resp.data.status.code
	
				LOG("sendJson() resp.status ${resp.status}, resp.data: ${resp.data}, statusCode: ${statusCode}", 2, child)
				
            	// TODO: Perhaps add at least two tries incase the first one fails?
				if(resp.status == 200) {				
					LOG("Updated ${resp.data}", 4)
					returnStatus = resp.data.status.code
					if (resp.data.status.code == 0) {
						LOG("Successful call to ecobee API.", 2, child)
						apiRestored()
	                    generateEventLocalParams()
    	                statusCode=false
					} else {
						LOG("Error return code = ${resp.data.status.code}", 1, child, "error")
					}
				} else {
    	        	LOG("Sent Json & got http status ${resp.status} - ${resp.status.code}", 2, child, "warn")
	
					//refresh the auth token
					if (resp.status == 500 && resp.status.code == 14) {					
						LOG("Refreshing your auth_token!")					
						if(refreshAuthToken(true)) { 
                        	LOG("Successfully performed a refreshAuthToken() after a Code 14!", 2, child, "warn")                        
                        }
						return false // No way to recover from a status.code 14
					} else {
    	            	LOG("Possible Authentication error, invalid authentication method, lack of credentials, etc. Status: ${resp.status} - ${resp.status.code} ", 2, child, "error")
        	            state.connected = "warn"
            	        generateEventLocalParams()
                	    if (j == 2) { // Go ahead and refresh on the second pass through
                    		refreshAuthToken(true) 
	                        return false
    	                }						
					}
				} // resp.status if/else
			} // HttpPost
        } // While loop
	} catch (groovyx.net.http.HttpResponseException e) {
    	LOG("sendJson() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}", 1, child, "error")	
		state.connected = "warn"
        generateEventLocalParams()
        refreshAuthToken(true)
		return false
    } catch(Exception e) {
    	// Might need to further break down 
		LOG("sendJson() - Exception Sending Json: " + e, 1, child, "error")
        state.connected = "warn"
        generateEventLocalParams()
        if (j == 2) { // Go ahead and refresh on the second pass through
        	refreshAuthToken(true)
			return false
		}
	}

	if (returnStatus == 0)
		return true
	else
		return false
}

private def getChildThermostatName() { return "Ecobee Thermostat" }
private def getChildSensorName()     { return "Ecobee Sensor" }
private def getServerUrl()           { return "https://graph.api.smartthings.com" }
private def getShardUrl()            { return getApiServerUrl() }
private def getCallbackUrl()         { return "${serverUrl}/oauth/callback" }
private def getBuildRedirectUrl()    { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${shardUrl}" }
private def getApiEndpoint()         { return "https://api.ecobee.com" }

// This is the API Key from the Ecobee developer page. Can be provided by the app provider or use the appSettings
private def getSmartThingsClientId() { 
	if(!appSettings.clientId) {
		return "obvlTjUuuR2zKpHR6nZMxHWugoi5eVtS"		
	} else {
		return appSettings.clientId 
    }
}


private def LOG(message, level=3, child=null, logType="debug", event=false, displayEvent=true) {
	def prefix = ""
    def logTypes = ["error", "debug", "info", "trace"]
    
    if(!logTypes.contains(logType)) {
    	log.error "LOG() - Received logType ${logType} which is not in the list of allowed types."
        if (event && child) { debugEventFromParent(child, "LOG() - Received logType ${logType} which is not in the list of allowed types.") }
        logType = "debug"
    }
    
    if ( logType == "error" ) { 
    	state.lastLOGerror = "${message} @ ${getTimestamp()}"
        state.LastLOGerrorDate = getTimestamp()
	}
	if ( settings.debugLevel?.toInteger() == 5 ) { prefix = "LOG: " }
	if ( debugLevel(level) ) { 
    	log."${logType}" "${prefix}${message}"
        if (event) { debugEvent(message, displayEvent) }
        if (child) { debugEventFromParent(child, message) }
	}    
}


private def debugEvent(message, displayEvent = false) {
	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	if ( debugLevel(4) ) { log.debug "Generating AppDebug Event: ${results}" }
	sendEvent (results)
}

private def debugEventFromParent(child, message) {
	 def data = [
            	debugEventFromParent: message
            ]         
	if (child) { child.generateEvent(data) }
}

// TODO: Create a more generic push capability to send notifications
//send both push notification and mobile activity feeds
private def sendPushAndFeeds(notificationMessage) {
	LOG("sendPushAndFeeds >> notificationMessage: ${notificationMessage}", 1, null, "warn")
	LOG("sendPushAndFeeds >> state.timeSendPush: ${state.timeSendPush}", 1, null, "warn")
    
    if (state.timeSendPush) {
        if ( (now() - state.timeSendPush) >= (1000 * 60 * 60 * 1)){ // notification is sent to remind user no more than once per hour
            sendPush("Your Ecobee thermostat " + notificationMessage)
            sendActivityFeeds(notificationMessage)
            state.timeSendPush = now()
        }
    } else {
        sendPush("Your Ecobee thermostat " + notificationMessage)
        sendActivityFeeds(notificationMessage)
        state.timeSendPush = now()
    }
    // state.authToken = null
}

private def sendActivityFeeds(notificationMessage) {
    def devices = getChildDevices()
    devices.each { child ->
        child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
    }
}




// Helper Functions
// Creating my own as it seems that the built-in version only works for a device, NOT a SmartApp
def myConvertTemperatureIfNeeded(scaledSensorValue, cmdScale, precision) {
	if ( (cmdScale != "C") && (cmdScale != "F") && (cmdScale != "dC") && (cmdScale != "dF") ) {
    	// We do not have a valid Scale input, throw a debug error into the logs and just return the passed in value
        LOG("Invalid temp scale used: ${cmdScale}", 2, null, "error")
        return scaledSensorValue
    }

	def returnSensorValue 
    
	// Normalize the input
	if (cmdScale == "dF") { cmdScale = "F" }
    if (cmdScale == "dC") { cmdScale = "C" }

	LOG("About to convert/scale temp: ${scaledSensorValue}", 5, null, "trace", false)
	if (cmdScale == getTemperatureScale() ) {
    	// The platform scale is the same as the current value scale
        returnSensorValue = scaledSensorValue.round(precision)
    } else if (cmdScale == "F") {		    	
    	returnSensorValue = fToC(scaledSensorValue).round(precision)
    } else {
    	returnSensorValue = cToF(scaledSensorValue).round(precision)
    }
    LOG("returnSensorValue == ${returnSensorValue}", 5, null, "trace", false)
    return returnSensorValue
}

def wantMetric() {
	return (getTemperatureScale() == "C")
}

private def cToF(temp) {
	LOG("cToF entered with ${temp}", 5, null, "info")
	return (temp * 1.8 + 32) as Double
    // return celsiusToFahrenheit(temp)
}
private def fToC(temp) {	
	LOG("fToC entered with ${temp}", 5, null, "info")
	return (temp - 32) / 1.8 as Double
    // return fahrenheitToCelsius(temp)
}
private def milesToKm(distance) {
	return (distance * 1.609344) 
}

// Establish the minimum amount of time to wait to do another poll
private def  getMinMinBtwPolls() {
    // TODO: Make this configurable in the SmartApp
	return 1
}

private def getPollingInterval() {
	return (settings.pollingInterval?.toInteger() >= 5) ? settings.pollingInterval.toInteger() : 5
}

private def String getTimestamp() {
	return new Date().format("yyyy-MM-dd HH:mm:ss z", location.timeZone)
}

private def getTimeOfDay() {
	def nowTime = new Date().format("HHmm", location.timeZone).toDouble()
    LOG("getTimeOfDay() - nowTime = ${nowTime}", 4, null, "trace")
    if ( (nowTime < state.sunriseTime) || (nowTime > state.sunsetTime) ) {
    	return "night"
    } else {
    	return "day"
    }
}

// Are we connected with the Ecobee service?
private String apiConnected() {
	// values can be "full", "warn", "lost"
	if (state.connected == null) state.connected = "warn"
	return state.connected?.toString() ?: "lost"
}


private def apiRestored() {
	state.connected = "full"
	unschedule("notifyApiLost")
}


private def getDebugDump() {
	 def debugParams = [when:"${getTimestamp()}", whenEpic:"${now()}", 
				lastPollDate:"${state.lastPollDate}", lastScheduledPollDate:"${state.lastScheduledPollDate}", 
				lastScheduledTokenRefreshDate:"${state.lastScheduledTokenRefreshDate}", lastScheduledWatchdogDate:"${state.lastScheduledWatchdogDate}",
				lastTokenRefreshDate:"${state.lastTokenRefreshDate}", initializedEpic:"${state.initializedEpic}", initializedDate:"${state.initializedDate}",
                lastLOGerror:"${state.lastLOGerror}", authTokenExpires:"${state.authTokenExpires}"
			]    
	return debugParams
}

private def apiLost(where = "[where not specified]") {
    LOG("apiLost() - ${where}: Lost connection with APIs. unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again", 1, null, "error")
    state.apiLostDump = getDebugDump()
    if (apiConnected() == "lost") {
    	LOG("apiLost() - already in lost state. Nothing else to do. (where= ${where})", 5, null, "trace")
    }
        
    // Has our token really expired yet?
    /*
    if ( !readyForAuthRefresh() ) {
    	LOG("apiLost() - Still time left on expiry of Auth Token. Gonna wait until full expiry to actually declare the api as fully lost. Setting it to warn instead.", 1, null, "error")
        state.connected = "warn"
    	generateEventLocalParams()
        return
	} */   
    
    // provide cleanup steps when API Connection is lost
	def notificationMessage = "is disconnected from SmartThings/Ecobee, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
    state.connected = "lost"
    state.authToken = null
    
    sendPushAndFeeds(notificationMessage)
	generateEventLocalParams()

    LOG("Unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again", 0, null, "error")
    
    // Notify each child that we lost so it gets logged
    if ( debugLevel(3) ) {
    	def d = getChildDevices()
    	d?.each { oneChild ->
        	LOG("apiLost() - notifying each child: ${oneChild} of loss", 0, child, "error")
		}
    }
    
    unschedule("pollScheduled")
    unschedule("refreshAuthTokenScheduled")
    unschedule("scheduleWatchdog")
    runEvery3Hours("notifyApiLost")
}

def notifyApiLost() {
	def notificationMessage = "is disconnected from SmartThings/Ecobee, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
    if ( state.connected == "lost" ) {
    	generateEventLocalParams()
		sendPushAndFeeds(notificationMessage)
        LOG("notifyApiLost() - API Connection Previously Lost. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again", 0, null, "error")
	} else {
    	// Must have restored connection
        unschedule("notifyApiLost")
    }    
}

private String childType(child) {
	// Determine child type (Thermostat or Remote Sensor)
    if ( child.hasCapability("Thermostat") ) { return getChildThermostatName() }
    if ( child.name.contains( getChildSensorName() ) ) { return getChildSensorName() }
    return "Unknown"
    
}

private Boolean readyForAuthRefresh() {
	LOG("Entered readyForAuthRefresh() ", 5)
    def timeLeft 
    
    timeLeft = state.authTokenExpires ? ((state.authTokenExpires - now()) / 1000 / 60) : 0
    LOG("timeLeft until expiry (in min): ${timeLeft}", 3)
    
    def ready = timeLeft <= 29    
    LOG("Ready for authRefresh? ${ready}", 4)
    return ready
}

private def whatHoldType() {
	def sendHoldType = settings.holdType ? (settings.holdType=="Temporary" || settings.holdType=="Until Next Program")? "nextTransition" : (settings.holdType=="Permanent" || settings.holdType=="Until I Change")? "indefinite" : "indefinite" : "indefinite"
	LOG("Entered whatHoldType() with ${sendHoldType}  settings.holdType == ${settings.holdType}")
	 
    return sendHoldType
}

private debugLevel(level=3) {
	def debugLvlNum = settings.debugLevel?.toInteger() ?: 3
    def wantedLvl = level?.toInteger()
    
    return ( debugLvlNum >= wantedLvl )
}


// Mark the poll data as "dirty" to allow a new API call to take place
private def dirtyPollData() {
	LOG("dirtyPollData() called to reset poll state", 5)
	state.forcePoll = true
}


// Ecobee API Related Functions - from Yves code


// tstatType =managementSet or registered (no spaces).  
//		registered is for Smart, Smart-Si & Ecobee thermostats, 
//		managementSet is for EMS thermostat
//		may also be set to a specific locationSet (ex. /Toronto/Campus/BuildingA)
//		may be set to null if not relevant for the given method
// thermostatId may be a list of serial# separated by ",", no spaces (ex. '123456789012,123456789013') 
private def buildBodyRequest(method, tstatType="registered", thermostatId, tstatParams = [],
	tstatSettings = [], tstatEvents = []) {
   LOG("Entered buildBodyRequest()", 5)
    
	def selectionJson = null
	def selection = null  
	if (method == 'thermostatSummary') {
		if (tstatType.trim().toUpperCase() == 'REGISTERED') {
			selection = [selection: [selectionType: 'registered', selectionMatch: '',
							includeEquipmentStatus: 'true']
						]
		} else {
			// If tstatType is different than managementSet, it is assumed to be locationSet specific (ex./Toronto/Campus/BuildingA)
			selection = (tstatType.trim().toUpperCase() == 'MANAGEMENTSET') ? 
				// get all EMS thermostats from the root
				[selection: [selectionType: 'managementSet', selectionMatch: '/',
					includeEquipmentStatus: 'true']
				] : // Or Specific to a location
				[selection: [selectionType: 'managementSet', selectionMatch: tstatType.trim(),
					includeEquipmentStatus: 'true']
				]
		}
		selectionJson = new groovy.json.JsonBuilder(selection)
		return selectionJson
	} else if (method == 'thermostatInfo') {
		selection = [selection: [selectionType: 'thermostats',
			selectionMatch: thermostatId,
			includeSettings: 'true',
			includeRuntime: 'true',
			includeProgram: 'true',           
			includeWeather: 'true',            
			includeAlerts: 'true',
			includeEvents: 'true',
			includeEquipmentStatus: 'true',
			includeSensors: 'true'
			]
		]
		selectionJson = new groovy.json.JsonBuilder(selection)
		return selectionJson
	} else {
		selection = [selectionType: 'thermostats', selectionMatch: thermostatId]
	}
	selectionJson = new groovy.json.JsonBuilder(selection)
    
	if ((method != 'setThermostatSettings') && (tstatSettings != null) && (tstatSettings != [])) {
		def function_clause = ((tstatParams != null) && (tsatParams != [])) ? 
			[type:method, params: tstatParams] : 
			[type: method]
		def bodyWithSettings = [functions: [function_clause], selection: selection,
				thermostat: [settings: tstatSettings]
			]
		def bodyWithSettingsJson = new groovy.json.JsonBuilder(bodyWithSettings)
		return bodyWithSettingsJson
	} else if (method == 'setThermostatSettings') {
		def bodyWithSettings = [selection: selection,thermostat: [settings: tstatSettings]
			]
		def bodyWithSettingsJson = new groovy.json.JsonBuilder(bodyWithSettings)
		return bodyWithSettingsJson
	} else if ((tstatParams != null) && (tsatParams != [])) {
		def function_clause = [type: method, params: tstatParams]
		def simpleBody = [functions: [function_clause], selection: selection]
		def simpleBodyJson = new groovy.json.JsonBuilder(simpleBody)
		return simpleBodyJson
	} else {
		def function_clause = [type: method]
		def simpleBody = [functions: [function_clause], selection: selection]
		def simpleBodyJson = new groovy.json.JsonBuilder(simpleBody)
		return simpleBodyJson
    }    
}

