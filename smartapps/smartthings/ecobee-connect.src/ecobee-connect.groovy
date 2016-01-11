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
 */
 
definition(
	name: "Ecobee (Connect)",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Connect your Ecobee thermostat to SmartThings.",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee@2x.png",
	singleInstance: true
) {
	appSetting "clientId"
}

preferences {
	page(name: "auth", title: "ecobee3 Auth", nextPage: "therms", content: "authPage", uninstall: true)
	page(name: "therms", title: "Select Thermostats", nextPage: "sensors", content: "thermsPage")
	page(name: "sensors", title: "Select Sensors", nextPage: "", content: "sensorsPage", install:true)
}

mappings {
	path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
	path("/oauth/callback") {action: [GET: "callback"]}
}


// Begin Preference Pages
def authPage() {
	log.debug "=====> authPage() Entered"

	if(!atomicState.accessToken) { //this is an access token for the 3rd party to make a call to the connect app
		atomicState.accessToken = createAccessToken()
	}

	def description = "Click to enter Ecobee Credentials"
	def uninstallAllowed = false
	def oauthTokenProvided = false

	if(atomicState.authToken) {
		description = "You are connected. Click Next above."
		uninstallAllowed = true
		oauthTokenProvided = true
	} else {
		description = "Click to enter Ecobee Credentials"
	}

	def redirectUrl = buildRedirectUrl //"${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}"
	log.debug "authPage() --> RedirectUrl = ${redirectUrl}"
	// get rid of next button until the user is actually auth'd
	if (!oauthTokenProvided) {
    	log.debug "authPage() --> in !oauthTokenProvided"
		return dynamicPage(name: "auth", title: "ecobee Setup", nextPage: "", uninstall: uninstallAllowed) {
			section() {
				paragraph "Tap below to log in to the ecobee service and authorize SmartThings access. Be sure to scroll down on page 2 and press the 'Allow' button."
				href url:redirectUrl, style:"embedded", required:true, title: "ecobee Account Login", description:description 
			}
		}
	} else {
    	log.debug "authPage() --> in else for oauthTokenProvided - ${atomicState.authToken}."
        return dynamicPage(name: "auth", title: "ecobee Setup", nextPage: "therms", uninstall: uninstallAllowed) {
        	section() {
            	paragraph "Continue on to select thermostats."
                href url:redirectUrl, style: "embedded", state: "complete", title: "ecobee Account Login", description: description
                }
        }           
	}
}

def thermsPage() {
	log.debug "=====> thermsPage() entered"
        
	def stats = getEcobeeThermostats()
	log.debug "thermsPage() -> thermostat list: $stats"
    log.debug "thermsPage() starting settings: ${settings}"
    
    dynamicPage(name: "therms", title: "Select Thermostats", nextPage: "sensors", content: "thermsPage", uninstall: true) {    
    	section("Units") {
        	paragraph "NOTE: The units type (F or C) is determined by your Hub Location settings automatically. Please update your Hub settings (under My Locations) to change the units used. Current value is ${getTemperatureScale()}."
        }
    	section("Select Thermostats") {
        	paragraph "Tap below to see the list of ecobee thermostats available in your ecobee account and select the ones you want to connect to SmartThings."
			input(name: "thermostats", title:"Select Thermostats", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:stats])        
        }
        section("Optional Settings") {
        	input(name: "holdType", title:"Select Hold Type", type: "enum", required:false, multiple:false, description: "Permanent", metadata:[values:["Permanent", "Temporary"]])
            input(name: "smartAuto", title:"Use Smart Auto Temperature Adjust?", type: "bool", required:false, description: false)
            input(name: "pollingInterval", title:"Polling Interval (in Minutes)", type: "enum", required:false, multiple:false, description: "5", options:["5", "10", "15", "30"])
        }
    } 
}

def sensorsPage() {
	// Only show sensors that are part of the chosen thermostat(s)
	log.debug "=====> sensorsPage() entered. settings: ${settings}"

	def options = getEcobeeSensors() ?: []
	def numFound = options.size() ?: 0
      
	log.debug "options = getEcobeeSensors == ${options}"

    dynamicPage(name: "sensors", title: "Select Sensors", nextPage: "") {
		if (numFound > 0)  {
			section(""){
				paragraph "Tap below to see the list of ecobee sensors available for the selected thermostat(s) and select the ones you want to connect to SmartThings."
				input(name: "ecobeesensors", title:"Select Ecobee Sensors (${numFound} found)", type: "enum", required:false, description: "Tap to choose", multiple:true, metadata:[values:options])
			}
		} else {
    		 // Must not have any sensors associated with this Thermostat
 		   log.debug "sensorsPage(): No sensors found."
           section(""){
           		paragraph "No associated sensors were found. Click Done above."
           }
	    }
	}
}
// End Prefernce Pages

// OAuth Init URL
def oauthInitUrl() {
	log.debug "oauthInitUrl with callback: ${callbackUrl}"

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			client_id: smartThingsClientId,			
			scope: "smartRead,smartWrite",
			redirect_uri: callbackUrl, //"https://graph.api.smartthings.com/oauth/callback"
			state: atomicState.oauthInitState			
	]

	log.debug "oauthInitUrl - Before redirect: location: ${apiEndpoint}/authorize?${toQueryString(oauthParams)}"
	redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
}

// OAuth Callback URL and helpers
def callback() {
	log.debug "callback()>> params: $params, params.code ${params.code}, params.state ${params.state}, atomicState.oauthInitState ${atomicState.oauthInitState}"

	def code = params.code
	def oauthState = params.state

	//verify oauthState == atomicState.oauthInitState, so the callback corresponds to the authentication request
	if (oauthState == atomicState.oauthInitState){
		log.debug "callback() --> States matched!"
		def tokenParams = [
			grant_type: "authorization_code",
			code      : code,
			client_id : smartThingsClientId,
			state	  : oauthState,
			redirect_uri: callbackUrl //"https://graph.api.smartthings.com/oauth/callback"
		]

		def tokenUrl = "${apiEndpoint}/token?${toQueryString(tokenParams)}"
		log.debug "callback()-->tokenURL: ${tokenUrl}"

		httpPost(uri: tokenUrl) { resp ->
			atomicState.refreshToken = resp.data.refresh_token
			atomicState.authToken = resp.data.access_token
            
			log.debug "Expires in ${resp?.data?.expires_in} seconds"
            atomicState.authTokenExpires = now() + (resp.data.expires_in * 1000)
			log.debug "swapped token: $resp.data"
			log.debug "atomicState.refreshToken: ${atomicState.refreshToken}"
			log.debug "atomicState.authToken: ${atomicState.authToken}"
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
	log.debug "====> getEcobeeThermostats() entered"

 	def requestBody = '{"selection":{"selectionType":"registered","selectionMatch":"","includeRuntime":true,"includeSensors":true}}'

	def deviceListParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
			query: [format: 'json', body: requestBody]
	]

	def stats = [:]
    try {
        httpGet(deviceListParams) { resp ->

		log.debug "httpGet() response: ${resp.data}"
        
        // Initialize the Thermostat Data. Will reuse for the Sensor List intialization
        atomicState.thermostatData = resp.data
        	
        
            if (resp.status == 200) {
            	log.debug "httpGet() in 200 Response"
            	resp.data.thermostatList.each { stat ->
					def dni = [app.id, stat.identifier].join('.')
					stats[dni] = getThermostatDisplayName(stat)
                }
            } else {
                log.debug "httpGet() - in else: http status: ${resp.status}"
                //refresh the auth token
                if (resp.status == 500 && resp.data.status.code == 14) {
                    log.debug "Storing the failed action to try later"
                    atomicState.action = "getEcobeeThermostats"
                    log.debug "Refreshing your auth_token!"
                    refreshAuthToken()
                } else {
                    log.error "Authentication error, invalid authentication method, lack of credentials, etc."
                }
            }
        }
    } catch(Exception e) {
        log.debug "___exception getEcobeeThermostats(): " + e
        refreshAuthToken()
    }
	atomicState.thermostatsWithNames = stats
    log.debug "atomicState.thermostatsWithNames == ${atomicState.thermostatsWithNames}"
	return stats
}

// Get the list of Ecobee Sensors for use in the settings pages (Only include the sensors that are tied to a thermostat that was selected)
Map getEcobeeSensors() {
	log.debug "====> getEcobeeSensors() entered. thermostats: ${thermostats}"

	def sensorMap = [:]
    def foundThermo = null
	// TODO: Is this needed?
	atomicState.remoteSensors = [:]    

	atomicState.thermostatData.thermostatList.each { singleStat ->
		log.debug "thermostat loop: singleStat == ${singleStat} singleStat.identifier == ${singleStat.identifier}"
        
    	if (!settings.thermostats.findAll{ it.contains(singleStat.identifier) } ) {
        	// We can skip this thermostat as it was not selected by the user
            log.info "getEcobeeSensors() --> Skipping this thermostat: ${singleStat.identifier}"
        } else {
			log.info "getEcobeeSensors() --> Entering the else... we found a match."
			
			log.debug "singleStat == ${singleStat.name}"
                        
        	atomicState.remoteSensors = atomicState.remoteSensors ? (atomicState.remoteSensors + singleStat.remoteSensors) : singleStat.remoteSensors
	        log.debug "After atomicState.remoteSensors setup..."
                        
    	    // WORKAROUND: Iterate over remoteSensors list and add in the thermostat DNI
        	// 		 This is needed to work around the dynamic enum "bug" which prevents proper deletion
            // TODO: Check to see if this is still needed. Seem to use elibleSensors now instead
        	// singleStat.remoteSensors.each { tempSensor ->
        	//	tempSensor.thermDNI = "${thermostat}"
            //	atomicState.remoteSensors = atomicState.remoteSensors + tempSensor
			//}

			log.debug "getEcobeeSensors() - singleStat.remoteSensors: ${singleStat.remoteSensors}"
        	log.debug "getEcobeeSensors() - atomicState.remoteSensors: ${atomicState.remoteSensors}"
		}
        
		atomicState.remoteSensors.each {
			if (it.type != "thermostat") {
				def value = "${it?.name}"
				def key = "ecobee_sensor-"+ it?.id + "-" + it?.code
				sensorMap["${key}"] = value
			}
		}
	} // end thermostats.each loop

	log.debug "getEcobeeSensors() - remote sensor list: ${sensorMap}"
    atomicState.eligibleSensors = sensorMap
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
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	log.debug "=====> initialize()"
    
    atomicState.connected = "full"
    
    // Create the child Thermostat Devices
	def devices = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getChildThermostatName(), dni, null, ["label":"Ecobee Thermostat:${atomicState.thermostatsWithNames[dni]}"])
			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}

    // Create the child Ecobee Sensor Devices
	def sensors = settings.ecobeesensors.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getChildSensorName(), dni, null, ["label":"Ecobee Sensor:${atomicState.eligibleSensors[dni]}"])
			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}
    
	log.debug "created ${devices.size()} thermostats and ${sensors.size()} sensors."


	// WORKAROUND: settings.ecobeesensors may contain leftover sensors in the dynamic enum bug scenario, use info in atomicState.eligibleSensors instead
    // TODO: Need to deal with individual sensors from remaining thermostats that might be excluded...
    // TODO: Cleanup this code now that it is working!
    def sensorList = atomicState.eligibleSensors.keySet()
    
    // atomicState.eligibleSensorsAsList = sensorList
    
    def reducedSensorList = settings.ecobeesensors.findAll { sensorList.contains(it) }
    log.info "**** reducedSensorList = ${reducedSensorList} *****"
    atomicState.activeSensors = reducedSensorList
        
    log.debug "sensorList based on keys: ${sensorList} from atomicState.sensors: ${atomicState.eligibleSensors}"
    
	def combined = settings.thermostats + atomicState.activeSensors  
	log.debug "Combined devices == ${combined}"
    
    // Delete any that are no longer in settings
	def delete  
    
    if (combined) {
    	delete = getChildDevices().findAll { !combined.contains(it.deviceNetworkId) }        
    } else {
    	delete = getAllChildDevices() // inherits from SmartApp (data-management)
    }
    
	log.warn "delete: ${delete}, deleting ${delete.size()} thermostat(s) and/or sensor(s)"
	delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

	atomicState.thermostatData = [:] //reset Map to store thermostat data

    //send activity feeds to tell that device is connected
    def notificationMessage = "is connected to SmartThings"
    sendActivityFeeds(notificationMessage)
    atomicState.timeSendPush = null

	pollHandler() //first time polling data from thermostat

	//automatically update devices status every 5 mins
    def interval = (settings.pollingInterval?.toInteger() >= 5) ? settings.pollingInterval.toInteger() : 5
	"runEvery${interval}Minutes"("poll")

    // Auth Token expires every hour 
    // Run as part of the poll() procedure since it runs every 5 minutes. Only runs if the time is close enough though to avoid API calls
	runEvery15Minutes("refreshAuthToken")

	atomicState.reAttempt = 0

}


// Called during initialization to get the inital poll
def pollHandler() {
	log.debug "pollHandler()"
    atomicState.lastPoll = 0 // Initialize the variable and force a poll even if there was one recently
	pollChildren(null) // Hit the ecobee API for update on all thermostats

/*
	atomicState.thermostats.each {stat ->
		def dni = stat.key
		log.debug ("DNI = ${dni}")
		def d = getChildDevice(dni)
		if(d) {
			log.debug ("pollHandler(): Found Child Device.")
			d.generateEvent(atomicState.thermostats[dni].data)
		}
	}
    */
}


def pollChildren(child = null) {
	log.debug "=====> pollChildren()"
    
    if (apiConnected() == "lost") {
    	log.warn "pollChildren() - Unable to pollChildren() due to API not being connected"
    	return
    }
    
    // TODO: Fix the SmartThings docs: now() is in seconds, NOT milliseconds
    // Check to see if it is time to do an full poll to the Ecobee servers. If so, execute the API call and update ALL children
    def timeSinceLastPoll = ((now() - atomicState.lastPoll?.toDouble()) / 1000 / 60)
    log.info "Time since last poll? ${timeSinceLastPoll} -- atomicState.lastPoll == ${atomicState.lastPoll}"
    
    if ( (atomicState.lastPoll == 0) || ( timeSinceLastPoll > getMinMinBtwPolls() ) ) {
    	// It has been longer than the minimum delay
        log.debug "Calling the Ecobee API to fetch the latest data..."
    	pollEcobeeAPI(getChildThermostatDeviceIdsString())  // This will update the values saved in the atomicState which can then be used to send the updates
	} else {
    	log.debug "pollChildren() - Not time to call the API yet. It has been ${timeSinceLastPoll} minutes since last full poll."
        generateEventLocalParams() // Update any local parameters and send
    }

	
	// Iterate over all the children
	def d = getChildDevices()
    d?.each() { oneChild ->
    	log.debug "pollChildren() - Processing poll data for child: ${oneChild} has ${oneChild.capabilities}"        
        
    	if( oneChild.hasCapability("Thermostat") ) {
        	// We found a Thermostat, send all of its events
            log.debug "pollChildren() - We found a Thermostat!"
            oneChild.generateEvent(atomicState.thermostats[oneChild.device.deviceNetworkId].data)
        } else {
        	// We must have a remote sensor
            log.debug "pollChildren() - Updating sensor data: ${oneChild.device.deviceNetworkId} data: ${atomicState.remoteSensorsData[oneChild.device.deviceNetworkId].data}"
            oneChild.generateEvent(atomicState.remoteSensorsData[oneChild.device.deviceNetworkId].data)
        } 
    }

/*
 * Possibly more flexibility later and only handle individual children requests, but since a single API call is used it is perhaps not really necessary)
	// Determine which type of child we have 
    if (!child) {
    	// Resend them their cached data (if we got here we can't poll the API yet)
        
    else if ( childType(child) ==  getChildThermostatName() ) {
    	// We have a child Thermostat
    	child.generateEvent(atomicState.thermostats[child.device.deviceNetworkId].data)
        
    } else if ( childType(child) == getChildSensorName() ) {
    	// We have a Remote Sensor
        
    } else {
    	// We have an unkown child type!
        log.error "pollChildren() --> Unkown child type trying to poll. Child = ${child}"
    }

*/
}

private def generateEventLocalParams() {
	// Iterate over all the children
    log.debug "entered generateEventLocalParams() "
	def d = getChildDevices()
    d?.each() { oneChild ->
    	log.debug "generateEventLocalParams() - Processing poll data for child: ${oneChild} has ${oneChild.capabilities}"        
        
    	if( oneChild.hasCapability("Thermostat") ) {
        	// We found a Thermostat, send local params as events
            log.debug "generateEventLocalParams() - We found a Thermostat!"
            def data = [
            	apiConnected: apiConnected()
            ]
            
            atomicState.thermostats[oneChild.device.deviceNetworkId].data.apiConnected = apiConnected()            
            oneChild.generateEvent(data)
        } else {
        	// We must have a remote sensor
            log.debug "generateEventLocalParams() - Updating sensor data: ${oneChild.device.deviceNetworkId}"
			// No local params to send            
        } 
    }

}

private def pollEcobeeAPI(thermostatIdsString = "") {
	log.info "=====> pollEcobeeAPI() entered - thermostatIdsString = ${thermostatIdsString}"


	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeExtendedRuntime":"false","includeSettings":"true","includeRuntime":"true","includeEquipmentStatus":"true","includeSensors":true,"includeWeather":true}}'
	def result = false
	
	def pollParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "text/json", "Authorization": "Bearer ${atomicState.authToken}"],
			query: [format: 'json', body: jsonRequestBody]
	]

	try{
		httpGet(pollParams) { resp ->

//            if (resp.data) {
//                debugEventFromParent(child, "pollChildren(child) >> resp.status = ${resp.status}, resp.data = ${resp.data}")
//            }

			if(resp.status == 200) {
				log.debug "poll results returned resp.data ${resp.data}"
				atomicState.remoteSensors = resp.data.thermostatList.remoteSensors
				atomicState.thermostatData = resp.data
               
                // Create the list of sensors and related data
				updateSensorData()
                // Create the list of thermostats and related data
				updateThermostatData()
                
				result = true
                
                if (atomicState.connected != "full") {
					atomicState.connected = "full"
                    generateEventLocalParams() // Update the connection status
                }
                atomicState.lastPoll = now();
				log.debug "updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
			} else {
				log.error "pollEcobeeAPI() - polling children & got http status ${resp.status}"

				//refresh the auth token
				if (resp.status == 500 && resp.data.status.code == 14) {
					log.debug "Resp.status: ${resp.status} Status Code: ${resp.data.status.code}. Unable to recover"
                    // Not possible to recover from a code 14
                    apiLost()					
				}
				else {
					log.error "pollEcobeeAPI() - Authentication error, invalid authentication method, lack of credentials, etc."
				}
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
    
		log.error "pollEcobeeAPI() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}"

		if ( (e.statusCode == 500 && e.getResponse()?.data.status.code == 14) ||  (e.statusCode == 401 && e.getResponse()?.data.status.code == 14) ) {
        	// Not possible to recover from status.code == 14
        	apiLost()
		} else if (e.statusCode != 401) { //this issue might comes from exceed 20sec app execution, connectivity issue etc.
            atomicState.connected = "warn"
            generateEventLocalParams() // Update the connected state at the thermostat devices
			runIn(reAttemptPeriod, "refreshAuthToken")
		} else if (e.statusCode == 401) { // Status.code other than 14
			atomicState.reAttempt = atomicState.reAttempt + 1
			log.warn "reAttempt refreshAuthToken to try = ${atomicState.reAttempt}"
			if (atomicState.reAttempt <= 3) {
               	atomicState.connected = "warn"
           		generateEventLocalParams() // Update the connected state at the thermostat devices
				runIn(reAttemptPeriod, "refreshAuthToken")
			} else {
               	apiLost()
			}
		}    
    } catch (java.util.concurrent.TimeoutException e) {
		log.error "pollEcobeeAPI(), TimeoutException: ${e}."
//        debugEventFromParent(child, "___exception polling children: " + e)
		// Likely bad luck and network overload, move on and let it try again
        
	} catch (Exception e) {
    	log.error "pollEcobeeAPI(): General Exception: ${e}."
    }
    log.debug "<===== Leaving pollEcobeeAPI() results: ${result}"
	return result
    
}


// poll() will be called on a regular interval using a runEveryX command
void poll() {
	// def devices = getChildDevices()
	// devices.each {pollChild(it)}
   //  TODO: if ( readyForAuthRefresh() ) { refreshAuthToken() } // Use runIn to make this feasible?
	
    // Check to see if we are connected to the API or not
    if (apiConnected() == "lost") {
    	log.warn "poll() - Skipping poll() due to lost API Connection"
    } else {
    	log.debug "poll() - Polling children with pollChildren(null)"
    	pollChildren(null) // Poll ALL the children at the same time for efficiency
    }
}


def availableModes(child) {

	debugEvent ("atomicState.thermostats = ${atomicState.thermostats}")
	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = atomicState.thermostats[child.device.deviceNetworkId]
	debugEvent("Data = ${tData}")

	if(!tData)
	{
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}

	def modes = ["off"]

	if (tData.data.heatMode) modes.add("heat")
	if (tData.data.coolMode) modes.add("cool")
	if (tData.data.autoMode) modes.add("auto")
	if (tData.data.auxHeatMode) modes.add("auxHeatOnly")

	modes

}

def currentMode(child) {
	debugEvent ("atomicState.thermostats = ${atomicState.thermostats}")
	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = atomicState.thermostats[child.device.deviceNetworkId]
	debugEvent("Data = ${tData}")

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}

	def mode = tData.data.thermostatMode

	mode
}

def updateSensorData() {
	log.debug "Entered updateSensorData() "
 	def sensorCollector = [:]
                
	atomicState.remoteSensors.each {
		it.each {
			if ( it.type == "ecobee3_remote_sensor" ) {
				// Add this sensor to the list
				def sensorDNI = "ecobee_sensor-" + it?.id + "-" + it?.code 
				log.info "sensorDNI == ${sensorDNI}"
            	                
				def temperature = ""
				def occupancy = ""
                            
				it.capability.each { cap ->
					if (cap.type == "temperature") {
                    	log.debug "updateSensorData() - Sensor (DNI: ${sensorDNI}) temp is ${cap.value}"
                        if ( cap.value.isNumber() ) { // Handles the case when the sensor is offline, which would return "unkown"
							temperature = cap.value as Double
							temperature = wantMetric() ? (temperature / 10).toDouble().round(1) : (temperature / 10).toDouble().round(0)
                        } else if (temperature == "unknown") {
                        	// TODO: Do something here to mark the sensor as offline?
                            log.error "updateSensorData() - sensor (DNI: ${sensorDNI}) returned unknown temp value. Perhaps it is unreachable."
                            
                        } else {
                        	 log.error "updateSensorData() - sensor (DNI: ${sensorDNI}) returned ${cap.value}."
                        }
					} else if (cap.type == "occupancy") {
						if(cap.value == "true") {
							occupancy = "active"
            	        } else {
							occupancy = "inactive"
						}
                            
					}
				}
                                            
				// TODO: Test the "unknown" populated data
				def sensorData = [
					temperature: ((temperature == "unknown") ? "--" : myConvertTemperatureIfNeeded(temperature, "F", 1)),
					motion: occupancy
				]
				sensorCollector[sensorDNI] = [data:sensorData]
			}
		} // End it.each loop
	} // End remoteSensors.each loop
	atomicState.remoteSensorsData = sensorCollector
	log.debug "updateSensorData(): found these remoteSensors: ${sensorCollector}"
                
}

def updateThermostatData() {
	// Create the list of thermostats and related data
	atomicState.thermostats = atomicState.thermostatData.thermostatList.inject([:]) { collector, stat ->
		def dni = [ app.id, stat.identifier ].join('.')

		log.debug "Updating dni $dni, Got weather? ${stat.weather.forecasts[0].weatherSymbol.toString()}"

		def data = [ 
			temperatureScale: getTemperatureScale(),
			apiConnected: apiConnected(),
			coolMode: (stat.settings.coolStages > 0),
			heatMode: (stat.settings.heatStages > 0),
			autoMode: stat.settings.autoHeatCoolFeatureEnabled,
			auxHeatMode: (stat.settings.hasHeatPump) && (stat.settings.hasForcedAir || stat.settings.hasElectric || stat.settings.hasBoiler),
			temperature: myConvertTemperatureIfNeeded( (stat.runtime.actualTemperature / 10), "F", (wantMetric() ? 1 : 0)),
			heatingSetpoint: myConvertTemperatureIfNeeded( (stat.runtime.desiredHeat / 10), "F", (wantMetric() ? 1 : 0)),
			coolingSetpoint: myConvertTemperatureIfNeeded( (stat.runtime.desiredCool / 10), "F", (wantMetric() ? 1 : 0)),
			thermostatMode: stat.settings.hvacMode,                            
			humidity: stat.runtime.actualHumidity,
			thermostatOperatingState: getThermostatOperatingState(stat),
			weatherSymbol: stat.weather.forecasts[0].weatherSymbol.toString(),
			weatherTemperature: myConvertTemperatureIfNeeded( ((stat.weather.forecasts[0].temperature / 10)), "F", (wantMetric() ? 1 : 0))
			// weatherStation:stat.weather.weatherStation,
			// weatherSymbol:stat.weather.forecasts[0].weatherSymbol.toString(),
			// weatherTemperature:stat.weather.forecasts[0].temperature,
			// weatherTemperatureDisplay:stat.weather.forecasts[0].temperature,
			// weatherDateTime:"Weather as of\n ${stat.weather.forecasts[0].dateTime.substring(0,16)}",
			// weatherCondition:stat.weather.forecasts[0].condition,
			// weatherTemp: stat.weather.forecasts[0].temperature,
			// weatherTempDisplay: stat.weather.forecasts[0].temperature,
			// weatherTempHigh: stat.weather.forecasts[0].tempHigh, 
			// weatherTempLow: stat.weather.forecasts[0].tempLow,
			// weatherTempHighDisplay: stat.weather.forecasts[0].tempHigh, 
			// weatherTempLowDisplay: stat.weather.forecasts[0].tempLow,
			// weatherWindSpeed: (stat.weather.forecasts[0].windSpeed/1000),		// divided by 1000 for display  TODO: Verify units on windSpeed
			// weatherPressure:stat.weather.forecasts[0].pressure.toString(),
			// weatherRelativeHumidity:stat.weather.forecasts[0].relativeHumidity,
			// weatherWindDirection:stat.weather.forecasts[0].windDirection + " Winds",
			// weatherPop:stat.weather.forecasts[0].pop.toString()
		]
        // TODO: Fix F to C conversion here as well
		data["temperature"] = data["temperature"] ? ( wantMetric() ? data["temperature"].toDouble() : data["temperature"].toDouble().toInteger() ) : data["temperature"]
		data["heatingSetpoint"] = data["heatingSetpoint"] ? ( wantMetric() ? data["heatingSetpoint"].toDouble() : data["heatingSetpoint"].toDouble().toInteger() ) : data["heatingSetpoint"]
		data["coolingSetpoint"] = data["coolingSetpoint"] ? ( wantMetric() ? data["coolingSetpoint"].toDouble() : data["coolingSetpoint"].toDouble().toInteger() ) : data["coolingSetpoint"]
        data["weatherTemperature"] = data["weatherTemperature"] ? ( wantMetric() ? data["weatherTemperature"].toDouble() : data["weatherTemperature"].toDouble().toInteger() ) : data["weatherTemperature"]
        
		
		debugEventFromParent(child, "Event Data = ${data}")

		collector[dni] = [data:data]
		return collector
	}
				
}

def getThermostatOperatingState(stat) {

	def equipStatus = (stat.equipmentStatus.size() > 0) ? stat.equipmentStatus : 'Idle'	
	equipStatus = equipStatus.trim().toUpperCase()
    
    log.debug "getThermostatOperatingState() - equipStatus == ${equipStatus}"
    
	def currentOpState = equipStatus.contains('HEAT')? 'heating' : (equipStatus.contains('COOL')? 'cooling' : 
    	equipStatus.contains('FAN')? 'fan only': 'idle')
	return currentOpState
}


def getChildThermostatDeviceIdsString(singleStat = null) {
	if(!singleStat) {
    	log.debug "getChildThermostatDeviceIdsString() - !singleStat"
		return thermostats.collect { it.split(/\./).last() }.join(',')
	} else {
    	// Only return the single thermostat
        def ecobeeDevId = singleStat.device.deviceNetworkID.split(/\./).last()
        log.debug "Received a single thermostat, returning the Ecobee Device ID as a String: ${ecobeeDevId}"
        return ecobeeDevId    	
    }
}

def toJson(Map m) {
	return new org.json.JSONObject(m).toString()
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private refreshAuthToken() {
    log.debug "refreshing auth token"
    debugEvent("Entered refreshAuthToken() ...")

	if(!atomicState.refreshToken) {
        log.error "refreshAuthToken() - There is no refreshToken stored! Unable to refresh OAuth token."
    	apiLost()
        return
    } else if ( !readyForAuthRefresh() ) {
    	// Not ready to refresh yet
        log.debug "refreshAuthToken() - Not time to refresh yet, there is still time left before expiration."
		debugEvent("Entered refreshAuthToken(): There is still time left before expiration.")        
    	return
    } else {

        def refreshParams = [
                method: 'POST',
                uri   : apiEndpoint,
                path  : "/token",
                query : [grant_type: 'refresh_token', code: "${atomicState.refreshToken}", client_id: smartThingsClientId],
        ]

        log.debug "refreshParams = ${refreshParams}"

        try {
            def jsonMap
            httpPost(refreshParams) { resp ->

                if(resp.status == 200) {
                    log.debug "refreshAuthToken() - 200 Response received - Extracting info."
                    debugEvent("refreshAuthToken() - 200 Response received - Extracting info")
                    
                    jsonMap = resp.data // Needed to work around strange bug that wasn't updating atomicState when accessing resp.data directly
                    log.debug "resp.data = ${resp.data} -- jsonMap is? ${jsonMap}"

                    if(jsonMap) {
                        log.debug "resp.data == ${resp.data}, jsonMap == ${jsonMap}"
                        debugEvent("Response = ${resp.data}, jsonMap == ${jsonMap}")
						
                        atomicState.refreshToken = jsonMap.refresh_token
                        
                        // TODO - Platform BUG: This was not updating the atomicState values for some reason if we use resp.data directly??? 
                        // 		  Workaround using jsonMap for authToken                       
                        log.debug "atomicState.authToken before: ${atomicState.authToken}"
                        def oldAuthToken = atomicState.authToken
                        atomicState.authToken = jsonMap?.access_token  
						log.debug "atomicState.authToken after: ${atomicState.authToken}"
                        if (oldAuthToken == atomicState.authToken) { 
                        	log.warn "WARN: atomicState.authToken did NOT update properly! This is likely a transient problem." 
                            atomicState.connected = "warn"
							generateEventLocalParams() // Update the connected state at the thermostat devices
						}

                        
                        // Save the expiry time to optimize the refresh
                        log.debug "Expires in ${resp?.data?.expires_in} seconds"
                        atomicState.authTokenExpires = (resp?.data?.expires_in * 1000) + now()

                        debugEvent("Refresh Token = atomicState =${atomicState.refreshToken}  == in: ${resp?.data?.refresh_token}")
                        debugEvent("OAUTH Token = atomicState ${atomicState.authToken} == in: ${resp?.data?.access_token}")

                        if(atomicState.action && atomicState.action != "") {
                            log.debug "Token refreshed. Executing next action: ${atomicState.action}"
                    		debugEvent("Token refreshed. Executing next action: ${atomicState.action}")

                            "${atomicState.action}"()

                            // Reset saved action
                            atomicState.action = ""
                        }

                    } else {
                    	debugEvent("No jsonMap??? ${jsonMap}")
                    }
                    atomicState.action = ""
                    atomicState.connected = "full"
                    generateEventLocalParams() // Update the connected state at the thermostat devices
                    
                } else {
                    log.error "Refresh failed ${resp.status} : ${resp.status.code}!"
					atomicState.connected = "warn"
                    generateEventLocalParams() // Update the connected state at the thermostat devices
                }
            }
        } catch (groovyx.net.http.HttpResponseException e) {
        	log.error "refreshAuthToken() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}"
            // log.error "refreshAuthToken() >> Error: e.statusCode ${e.statusCode}. full exception: ${e} response? data: ${e.getResponse()?.getData()} headers ${e.getResponse()?.getHeaders()}}"
           	def reAttemptPeriod = 300 // in sec
			if ( (e.statusCode == 500 && e.getResponse()?.data.status.code == 14) || (e.statusCode == 401 && e.getResponse()?.data.status.code == 14) ) {
            	apiLost()
            } else if (e.statusCode != 401) { //this issue might comes from exceed 20sec app execution, connectivity issue etc.
            	atomicState.connected = "warn"
            	generateEventLocalParams() // Update the connected state at the thermostat devices
				runIn(reAttemptPeriod, "refreshAuthToken")
			} else if (e.statusCode == 401) { // status.code other than 14
				atomicState.reAttempt = atomicState.reAttempt + 1
				log.warn "reAttempt refreshAuthToken to try = ${atomicState.reAttempt}"
				if (atomicState.reAttempt <= 3) {
                	atomicState.connected = "warn"
            		generateEventLocalParams() // Update the connected state at the thermostat devices
					runIn(reAttemptPeriod, "refreshAuthToken")
				} else {
                	// More than 3 attempts, time to give up and notify the end user
                	apiLost()
				}
            }
        } catch (java.util.concurrent.TimeoutException e) {
			log.error "refreshAuthToken(), TimeoutException: ${e}."
			// Likely bad luck and network overload, move on and let it try again
            runIn(300, "refreshAuthToken")
        } catch (Exception e) {
        	log.error "refreshAuthToken(), General Exception: ${e}."
        }
    }
}



def resumeProgram(child, deviceId) {

//    def thermostatIdsString = getChildDeviceIdsString()
//    log.debug "resumeProgram children: $thermostatIdsString"

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}]}'
	//, { "type": "sendMessage", "params": { "text": "Setpoint Updated" } }
	def result = sendJson(jsonRequestBody)
//    debugEventFromParent(child, "resumeProgram(child) with result ${result}")
	// Update the data after giving the thermostat a chance to consume the command and update the values
	runIn(20, "pollChildren")
	return result
}

def setHold(child, heating, cooling, deviceId, sendHoldType) {
	int h = (getTemperatureScale() == "C") ? (cToF(heating) * 10) : (heating * 10)
	int c = (getTemperatureScale() == "C") ? (cToF(cooling) * 10) : (cooling * 10)
    

    log.debug "setHold(): setpoints____________ - h: ${heating} - ${h}, c: ${cooling} - ${c}, setHoldType: ${sendHoldType}"
    debugEventFromParent(child, "setHold(): setpoints____________ - h: ${heating} - ${h}, c: ${cooling} - ${c}, setHoldType: ${sendHoldType}")
//    def thermostatIdsString = getChildDeviceIdsString()

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": '+sendHoldType+' } } ]}'
//	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}, { "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": "indefinite" } } ]}'
	def result = sendJson(child, jsonRequestBody)
    debugEventFromParent(child, "setHold: heating: ${h}, cooling: ${c} with result ${result}")
	return result
}

def setMode(child, mode, deviceId) {
//    def thermostatIdsString = getChildDeviceIdsString()
//    log.debug "setCoolingSetpoint children: $thermostatIdsString"

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"thermostat": {"settings":{"hvacMode":"'+"${mode}"+'"}}}'

//    log.debug "Mode Request Body = ${jsonRequestBody}"
//    debugEvent ("Mode Request Body = ${jsonRequestBody}")

	def result = sendJson(jsonRequestBody)
//    debugEventFromParent(child, "setMode to ${mode} with result ${result}")
	return result
}

def sendJson(child = null, String jsonBody) {

	def returnStatus = false
	def cmdParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
			body: jsonBody
	]

	try{
		httpPost(cmdParams) { resp ->

//            debugEventFromParent(child, "sendJson >> resp.status ${resp.status}, resp.data: ${resp.data}")

			if(resp.status == 200) {

				log.debug "updated ${resp.data}"
				returnStatus = resp.data.status.code
				if (resp.data.status.code == 0)
					log.debug "Successful call to ecobee API."
				else {
					log.debug "Error return code = ${resp.data.status.code}"
					debugEvent("Error return code = ${resp.data.status.code}")
				}
			} else {
				log.error "sent Json & got http status ${resp.status} - ${resp.status.code}"
				debugEvent ("sent Json & got http status ${resp.status} - ${resp.status.code}")

				//refresh the auth token
				if (resp.status == 500 && resp.status.code == 14) {
					//log.debug "Storing the failed action to try later"
					log.debug "Refreshing your auth_token!"
					debugEvent ("sendJson() - Refreshing OAUTH Token")
					refreshAuthToken()
					return false
				} else {
					debugEvent ("Possible Authentication error, invalid authentication method, lack of credentials, etc. Status: ${resp.status} - ${resp.status.code} ")
					log.error "Possible Authentication error, invalid authentication method, lack of credentials, etc. Status: ${resp.status} - ${resp.status.code} "
                    atomicState.connected = "warn"
                    generateEventLocalParams()
                    refreshAuthToken()
					return false
				}
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
    	log.error "sendJson() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}"
        debugEvent( "sendJson() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}" )
		atomicState.connected = "warn"
        generateEventLocalParams()
        refreshAuthToken()
		return false
    } catch(Exception e) {
    	// Might need to further break down 
		log.error "sendJson() - Exception Sending Json: " + e
		debugEvent ("Exception Sending JSON: " + e)
        atomicState.connected = "warn"
        generateEventLocalParams()
        refreshAuthToken()
		return false
	}

	if (returnStatus == 0)
		return true
	else
		return false
}

def getChildThermostatName() { return "Ecobee Thermostat" }
def getChildSensorName()     { return "Ecobee Sensor" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getShardUrl()            { return getApiServerUrl() }
def getCallbackUrl()         { return "${serverUrl}/oauth/callback" }
def getBuildRedirectUrl()    { return "${serverUrl}/oauth/initialize?appId=${app.id}&access_token=${atomicState.accessToken}&apiServerUrl=${shardUrl}" }
def getApiEndpoint()         { return "https://api.ecobee.com" }

// This is the API Key from the Ecobee developer page. Can be provided by the app provider or use the appSettings
def getSmartThingsClientId() { 
	if(!appSettings.clientId) {
		return "obvlTjUuuR2zKpHR6nZMxHWugoi5eVtS"		
	} else {
		return appSettings.clientId 
    }
}

def debugEvent(message, displayEvent = false) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}

def debugEventFromParent(child, message) {
	if (child != null) { child.sendEvent("name":"debugEventFromParent", "value":message, "description":message, displayed: true, isStateChange: true)}
}

//send both push notification and mobile activity feeds
def sendPushAndFeeds(notificationMessage){
    log.warn "sendPushAndFeeds >> notificationMessage: ${notificationMessage}"
    log.warn "sendPushAndFeeds >> atomicState.timeSendPush: ${atomicState.timeSendPush}"
    if (atomicState.timeSendPush){
        if (now() - atomicState.timeSendPush > 86400000){ // notification is sent to remind user once a day
            sendPush("Your Ecobee thermostat " + notificationMessage)
            sendActivityFeeds(notificationMessage)
            atomicState.timeSendPush = now()
        }
    } else {
        sendPush("Your Ecobee thermostat " + notificationMessage)
        sendActivityFeeds(notificationMessage)
        atomicState.timeSendPush = now()
    }
    atomicState.authToken = null
}

def sendActivityFeeds(notificationMessage) {
    def devices = getChildDevices()
    devices.each { child ->
        child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
    }
}

// Helper Apps

// Built in functions from SmartThings for temperature unit handling?
// getTemperatureScale()
// fahrenheitToCelsius()
// celsiusToFahrenheit()
// convertTemperatureIfNeeded()

// Creating my own as it seems that the built-in version only works for a device, NOT a SmartApp
def myConvertTemperatureIfNeeded(scaledSensorValue, cmdScale, precision) {
	if ( (cmdScale != "C") && (cmdScale != "F") && (cmdScale != "dC") && (cmdScale != "dF") ) {
    	// We do not have a valid Scale input, throw a debug error into the logs and just return the passed in value
        return scaledSensorValue
    }

	// Normalize the input
	if (cmdScale == "dF") { cmdScale = "F" }
    if (cmdScale == "dC") { cmdScale = "C" }

	if (cmdScale == getTemperatureScale() ) {
    	// The platform scale is the same as the current value scale
        return scaledSensorValue
    } else if (cmdScale == "F") {
    	return fToC(scaledSensorValue).round(precision)
    } else {
    	return cToF(scaledSensorValue).round(precision)
    }
    
}

def wantMetric() {
	return (getTemperatureScale() == "C")
}

private def cToF(temp) {
	log.info "cToF entered with ${temp}"
	return (temp * 1.8 + 32) as Double
    // return celsiusToFahrenheit(temp)
}
private def fToC(temp) {	
	log.info "fToC entered with ${temp}"
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


// Are we connected with the Ecobee service?
private String apiConnected() {
	// values can be "full", "warn", "lost"
	if (atomicState.connected == null) atomicState.connected = "lost"
	return atomicState.connected?.toString() ?: "lost"
}

private def apiLost() {
    log.error "Lost connection with APIs. unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again"
	
    // provide cleanup steps when API Connection is lost
	def notificationMessage = "is disconnected from SmartThings/Ecobee, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
    atomicState.connected = "lost"
    sendPushAndFeeds(notificationMessage)
	generateEventLocalParams()

    debugEvent("unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again")
    unschedule("poll")
    unschedule("refreshAuthToken")
}

private String childType(child) {
	// Determine child type (Thermostat or Remote Sensor)
    if ( child.hasCapability("Thermostat") ) { return getChildThermostatName() }
    if ( child.name.contains( getChildSensorName() ) ) { return getChildSensorName() }
    return "Unknown"
    
}

private Boolean readyForAuthRefresh () {
	log.debug "Entered readyForAuthRefresh() "
    def timeLeft 
    
    timeLeft = atomicState.authTokenExpires ? ((atomicState.authTokenExpires - now()) / 1000 / 60) : 0
    log.debug "timeLeft until expiry (in min): ${timeLeft}"
    
    // Since this runs as part of poll() we can be a bit more conservative on the time before renewing the token
    // def pollInterval = settings.pollingInterval ?: 5
    // def ready = timeLeft <= ((pollInterval * 3) + 2)
    def ready = timeLeft <= 29
    log.debug "Ready for authRefresh? ${ready}"
    return ready
}
