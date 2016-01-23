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
            input(name: "debugLevel", title:"Debugging Level (higher # is more data reported)", type: "enum", required:false, multiple:false, description: "3", metadata:[values:["5", "4", "3", "2", "1", "0"]])
        }
    } 
}

def sensorsPage() {
	// Only show sensors that are part of the chosen thermostat(s)
	if ( debugLevel(4) ) { log.debug "=====> sensorsPage() entered. settings: ${settings}" }

	def options = getEcobeeSensors() ?: []
	def numFound = options.size() ?: 0
      
	if ( debugLevel(3) ) { log.debug "options = getEcobeeSensors == ${options}" }

    dynamicPage(name: "sensors", title: "Select Sensors", nextPage: "") {
		if (numFound > 0)  {
			section(""){
				paragraph "Tap below to see the list of ecobee sensors available for the selected thermostat(s) and select the ones you want to connect to SmartThings."
				input(name: "ecobeesensors", title:"Select Ecobee Sensors (${numFound} found)", type: "enum", required:false, description: "Tap to choose", multiple:true, metadata:[values:options])
			}
		} else {
    		 // Must not have any sensors associated with this Thermostat
 		   if ( debugLevel(4) ) { log.debug "sensorsPage(): No sensors found." }
           section(""){
           		paragraph "No associated sensors were found. Click Done above."
           }
	    }
	}
}
// End Prefernce Pages

// OAuth Init URL
def oauthInitUrl() {
	if ( debugLevel(4) ) { log.debug "oauthInitUrl with callback: ${callbackUrl}" }

	atomicState.oauthInitState = UUID.randomUUID().toString()

	def oauthParams = [
			response_type: "code",
			client_id: smartThingsClientId,			
			scope: "smartRead,smartWrite",
			redirect_uri: callbackUrl, //"https://graph.api.smartthings.com/oauth/callback"
			state: atomicState.oauthInitState			
	]

	if ( debugLevel(4) ) { log.debug "oauthInitUrl - Before redirect: location: ${apiEndpoint}/authorize?${toQueryString(oauthParams)}" }
	redirect(location: "${apiEndpoint}/authorize?${toQueryString(oauthParams)}")
}

// OAuth Callback URL and helpers
def callback() {
	if ( debugLevel(4) )  { log.debug "callback()>> params: $params, params.code ${params.code}, params.state ${params.state}, atomicState.oauthInitState ${atomicState.oauthInitState}" }

	def code = params.code
	def oauthState = params.state

	//verify oauthState == atomicState.oauthInitState, so the callback corresponds to the authentication request
	if (oauthState == atomicState.oauthInitState){
		if ( debugLevel(4) ) { log.debug "callback() --> States matched!" }
		def tokenParams = [
			grant_type: "authorization_code",
			code      : code,
			client_id : smartThingsClientId,
			state	  : oauthState,
			redirect_uri: callbackUrl //"https://graph.api.smartthings.com/oauth/callback"
		]

		def tokenUrl = "${apiEndpoint}/token?${toQueryString(tokenParams)}"
		if ( debugLevel(2) ) { log.debug "callback()-->tokenURL: ${tokenUrl}" }

		httpPost(uri: tokenUrl) { resp ->
			atomicState.refreshToken = resp.data.refresh_token
			atomicState.authToken = resp.data.access_token
            
			if ( debugLevel(3) ) { log.debug "Expires in ${resp?.data?.expires_in} seconds" }
            atomicState.authTokenExpires = now() + (resp.data.expires_in * 1000)
			if ( debugLevel(2) ) { log.debug "swapped token: $resp.data; atomicState.refreshToken: ${atomicState.refreshToken}; atomicState.authToken: ${atomicState.authToken}" }
		}

		if (atomicState.authToken) {
			success()
		} else {
			fail()
		}

	} else {
		if ( debugLevel(1) ) { log.error "callback() failed oauthState != atomicState.oauthInitState" }
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
	if ( debugLevel(5) ) { log.debug "====> getEcobeeThermostats() entered" }

 	def requestBody = '{"selection":{"selectionType":"registered","selectionMatch":"","includeRuntime":true,"includeSensors":true,"includeProgram":true}}'

	def deviceListParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
			query: [format: 'json', body: requestBody]
	]

	def stats = [:]
    try {
        httpGet(deviceListParams) { resp ->

		if ( debugLevel(3) ) { log.debug "httpGet() response: ${resp.data}" }
        
        // Initialize the Thermostat Data. Will reuse for the Sensor List intialization
        atomicState.thermostatData = resp.data
        	
        
            if (resp.status == 200) {
            	if ( debugLevel(3) ) { log.debug "httpGet() in 200 Response" }
            	resp.data.thermostatList.each { stat ->
					def dni = [app.id, stat.identifier].join('.')
					stats[dni] = getThermostatDisplayName(stat)
                }
            } else {
                if ( debugLevel(3) ) { log.debug "httpGet() - in else: http status: ${resp.status}" }
                //refresh the auth token
                if (resp.status == 500 && resp.data.status.code == 14) {
                    log.debug "Storing the failed action to try later"
                    atomicState.action = "getEcobeeThermostats"
                    log.debug "Refreshing your auth_token!"
                    refreshAuthToken()
                } else {
                    if ( debugLevel(1) ) { log.error "Other error. Status: ${resp.status}  Response data: ${resp.data} " }
                }
            }
        }
    } catch(Exception e) {
        log.debug "___exception getEcobeeThermostats(): " + e
        refreshAuthToken()
    }
	atomicState.thermostatsWithNames = stats
    if ( debugLevel(4) ) { log.debug "atomicState.thermostatsWithNames == ${atomicState.thermostatsWithNames}" }
	return stats
}

// Get the list of Ecobee Sensors for use in the settings pages (Only include the sensors that are tied to a thermostat that was selected)
Map getEcobeeSensors() {
	if ( debugLevel(5) ) { log.debug "====> getEcobeeSensors() entered. thermostats: ${thermostats}" }

	def sensorMap = [:]
    def foundThermo = null
	// TODO: Is this needed?
	atomicState.remoteSensors = [:]    

	atomicState.thermostatData.thermostatList.each { singleStat ->
		if ( debugLevel(4) ) { log.debug "thermostat loop: singleStat == ${singleStat} singleStat.identifier == ${singleStat.identifier}" }
        
    	if (!settings.thermostats.findAll{ it.contains(singleStat.identifier) } ) {
        	// We can skip this thermostat as it was not selected by the user
            if ( debugLevel(5) ) { log.info "getEcobeeSensors() --> Skipping this thermostat: ${singleStat.identifier}" }
        } else {
			if ( debugLevel(4) ) { log.info "getEcobeeSensors() --> Entering the else... we found a match. singleStat == ${singleStat.name}" }
                        
        	atomicState.remoteSensors = atomicState.remoteSensors ? (atomicState.remoteSensors + singleStat.remoteSensors) : singleStat.remoteSensors
	        if ( debugLevel(4) ) { log.debug "After atomicState.remoteSensors setup..." }
                        
    	    // WORKAROUND: Iterate over remoteSensors list and add in the thermostat DNI
        	// 		 This is needed to work around the dynamic enum "bug" which prevents proper deletion
            // TODO: Check to see if this is still needed. Seem to use elibleSensors now instead
        	// singleStat.remoteSensors.each { tempSensor ->
        	//	tempSensor.thermDNI = "${thermostat}"
            //	atomicState.remoteSensors = atomicState.remoteSensors + tempSensor
			//}
			if ( debugLevel(4) ) {
				log.debug "getEcobeeSensors() - singleStat.remoteSensors: ${singleStat.remoteSensors}"
        		log.debug "getEcobeeSensors() - atomicState.remoteSensors: ${atomicState.remoteSensors}"
			}
		}
        
		atomicState.remoteSensors.each {
			if (it.type != "thermostat") {
				def value = "${it?.name}"
				def key = "ecobee_sensor-"+ it?.id + "-" + it?.code
				sensorMap["${key}"] = value
			}
		}
	} // end thermostats.each loop

	if ( debugLevel(4) ) { log.debug "getEcobeeSensors() - remote sensor list: ${sensorMap}" }
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
	if ( debugLevel(4) ) { log.debug "Updated with settings: ${settings}" }
	unsubscribe()
	initialize()
}

def initialize() {
	if ( debugLevel(4) ) { log.debug "=====> initialize()" }
    
    atomicState.connected = "full"
    unschedule()
    atomicState.reAttempt = 0
    
    // Create the child Thermostat Devices
	def devices = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getChildThermostatName(), dni, null, ["label":"Ecobee Thermostat:${atomicState.thermostatsWithNames[dni]}"])
			if ( debugLevel(4) ) { log.debug "created ${d.displayName} with id $dni" }
		} else {
			if ( debugLevel(4) ) { log.debug "found ${d.displayName} with id $dni already exists" }
		}
		return d
	}

    // Create the child Ecobee Sensor Devices
	def sensors = settings.ecobeesensors.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			d = addChildDevice(app.namespace, getChildSensorName(), dni, null, ["label":"Ecobee Sensor:${atomicState.eligibleSensors[dni]}"])
			if ( debugLevel(4) ) { log.debug "created ${d.displayName} with id $dni" }
		} else {
			if ( debugLevel(4) ) { log.debug "found ${d.displayName} with id $dni already exists" }
		}
		return d
	}
    
	if ( debugLevel(3) ) { log.debug "created ${devices.size()} thermostats and ${sensors.size()} sensors." }


	// WORKAROUND: settings.ecobeesensors may contain leftover sensors in the dynamic enum bug scenario, use info in atomicState.eligibleSensors instead
    // TODO: Need to deal with individual sensors from remaining thermostats that might be excluded...
    // TODO: Cleanup this code now that it is working!
    def sensorList = atomicState.eligibleSensors.keySet()
    
    // atomicState.eligibleSensorsAsList = sensorList
    
    def reducedSensorList = settings.ecobeesensors.findAll { sensorList.contains(it) }
    if ( debugLevel(5) ) { log.info "**** reducedSensorList = ${reducedSensorList} *****" }
    atomicState.activeSensors = reducedSensorList
        
    if ( debugLevel(4) ) { log.debug "sensorList based on keys: ${sensorList} from atomicState.sensors: ${atomicState.eligibleSensors}" }
    
	def combined = settings.thermostats + atomicState.activeSensors  
	if ( debugLevel(4) ) { log.debug "Combined devices == ${combined}" }
    
    // Delete any that are no longer in settings
	def delete  
    
    if (combined) {
    	delete = getChildDevices().findAll { !combined.contains(it.deviceNetworkId) }        
    } else {
    	delete = getAllChildDevices() // inherits from SmartApp (data-management)
    }
    
	if (debugLevel > 2) { log.warn "delete: ${delete}, deleting ${delete.size()} thermostat(s) and/or sensor(s)" }
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
    // runEvery5Minutes("poll")

    // Auth Token expires every hour 
    // Run as part of the poll() procedure since it runs every 5 minutes. Only runs if the time is close enough though to avoid API calls
	runEvery15Minutes("refreshAuthToken")
}


// Called during initialization to get the inital poll
def pollHandler() {
	if ( debugLevel(4) ) { log.debug "pollHandler()" }
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
	if ( debugLevel(4) ) { log.debug "=====> pollChildren()" }
    
    if (apiConnected() == "lost") {
    	log.warn "pollChildren() - Unable to pollChildren() due to API not being connected"
        generateEventLocalParams() // Ensure that the client is notified that the API is disconnected
    	return
    }
    
    // TODO: Fix the SmartThings docs: now() is in seconds, NOT milliseconds
    // Check to see if it is time to do an full poll to the Ecobee servers. If so, execute the API call and update ALL children
    def timeSinceLastPoll = ((now() - atomicState.lastPoll?.toDouble()) / 1000 / 60)
    if ( debugLevel(3) ) { 
    	log.info "Time since last poll? ${timeSinceLastPoll} -- atomicState.lastPoll == ${atomicState.lastPoll}" 
        if (child) debugEventFromParent(child, "Time since last poll? ${timeSinceLastPoll} -- atomicState.lastPoll == ${atomicState.lastPoll}")
	}
    
    // Reschedule polling if it has been a while since the previous poll
    def interval = (settings.pollingInterval?.toInteger() >= 5) ? settings.pollingInterval.toInteger() : 5
    if ( timeSinceLastPoll >= (interval * 2) ) {
    	// automatically update devices status every ${interval} mins    
        // re-establish polling
		"runEvery${interval}Minutes"("poll")
    }
    
    if ( (atomicState.lastPoll == 0) || ( timeSinceLastPoll > getMinMinBtwPolls().toDouble() ) ) {
    	// It has been longer than the minimum delay
        if ( debugLevel(4) ) { 
        	log.debug "Calling the Ecobee API to fetch the latest data..." 
        	if (child) debugEventFromParent(child, "Calling the Ecobee API to fetch the latest data..." )
        }
    	pollEcobeeAPI(getChildThermostatDeviceIdsString())  // This will update the values saved in the atomicState which can then be used to send the updates
	} else {
    	if ( debugLevel(4) ) { log.debug "pollChildren() - Not time to call the API yet. It has been ${timeSinceLastPoll} minutes since last full poll." }
        generateEventLocalParams() // Update any local parameters and send
    }

	
	// Iterate over all the children
	def d = getChildDevices()
    d?.each() { oneChild ->
    	if ( debugLevel(4) ) { log.debug "pollChildren() - Processing poll data for child: ${oneChild} has ${oneChild.capabilities}" }
        
    	if( oneChild.hasCapability("Thermostat") ) {
        	// We found a Thermostat, send all of its events
            if ( debugLevel(4) ) { log.debug "pollChildren() - We found a Thermostat!" }
            oneChild.generateEvent(atomicState.thermostats[oneChild.device.deviceNetworkId].data)
        } else {
        	// We must have a remote sensor
            if ( debugLevel(4) ) { log.debug "pollChildren() - Updating sensor data: ${oneChild.device.deviceNetworkId} data: ${atomicState.remoteSensorsData[oneChild.device.deviceNetworkId].data}" }
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
    	// We have an unknown child type!
        log.error "pollChildren() --> Unknown child type trying to poll. Child = ${child}"
    }

*/
}

private def generateEventLocalParams() {
	// Iterate over all the children
    if ( debugLevel(4) ) { log.debug "entered generateEventLocalParams() " }
	def d = getChildDevices()
    d?.each() { oneChild ->
    	if ( debugLevel(4) ) { log.debug "generateEventLocalParams() - Processing poll data for child: ${oneChild} has ${oneChild.capabilities}" }
        
    	if( oneChild.hasCapability("Thermostat") ) {
        	// We found a Thermostat, send local params as events
            if ( debugLevel(3) ) { log.debug "generateEventLocalParams() - We found a Thermostat!" }
            def data = [
            	apiConnected: apiConnected()
            ]
            
            atomicState.thermostats[oneChild.device.deviceNetworkId].data.apiConnected = apiConnected()            
            oneChild.generateEvent(data)
        } else {
        	// We must have a remote sensor
            if ( debugLevel(3) ) { log.debug "generateEventLocalParams() - Updating sensor data: ${oneChild.device.deviceNetworkId}" }
			// No local params to send            
        } 
    }

}

private def pollEcobeeAPI(thermostatIdsString = "") {
	if ( debugLevel(2) ) { log.info "=====> pollEcobeeAPI() entered - thermostatIdsString = ${thermostatIdsString}" }

	// TODO: Check on any running EVENTs on thermostat

	// def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeExtendedRuntime":"false","includeSettings":"true","includeRuntime":"true","includeEquipmentStatus":"true","includeSensors":true,"includeWeather":true,"includeProgram":true}}'
	def jsonRequestBody = build_body_request("thermostatInfo", null, thermostatIdsString, null).toString()
    
    if ( debugLevel(5) ) { log.info "build_body_request returned: ${jsonRequestBody}" }
    

    
    def result = false
	
	def pollParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
			query: [format: 'json', body: jsonRequestBody]
	]

	try{
		httpGet(pollParams) { resp ->

//            if (resp.data) {
//                debugEventFromParent(child, "pollChildren(child) >> resp.status = ${resp.status}, resp.data = ${resp.data}")
//            }

			if(resp.status == 200) {
				if ( debugLevel(2) ) { log.debug "poll results returned resp.data ${resp.data}" }
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
				log.debug "httpGet: updated ${atomicState.thermostats?.size()} stats: ${atomicState.thermostats}"
			} else {
				if ( debugLevel(1) ) { log.error "pollEcobeeAPI() - polling children & got http status ${resp.status}" }

				//refresh the auth token
				if (resp.status == 500 && resp.data.status.code == 14) {
					if ( debugLevel(1) ) {  
                    	log.error "Resp.status: ${resp.status} Status Code: ${resp.data.status.code}. Unable to recover" 
                    	debugEvent("Resp.status: ${resp.status} Status Code: ${resp.data.status.code}. Unable to recover") 
                    }
                    // Not possible to recover from a code 14
                    apiLost("pollEcobeeAPI() - Resp.status: ${resp.status} Status Code: ${resp.data.status.code}. Unable to recover.")
				}
				else {
					if ( debugLevel(1) ) { 
                    	log.error "pollEcobeeAPI() - Other responses received. Resp.status: ${resp.status} Status Code: ${resp.data.status.code}." 
                    	debugEvent("pollEcobeeAPI() - Other responses received. Resp.status: ${resp.status} Status Code: ${resp.data.status.code}.")
                    }
				}
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
    
		if ( debugLevel(1) ) { log.error "pollEcobeeAPI() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}" }
        
		def reAttemptPeriod = 45 // in sec
		if ( (e.statusCode == 500 && e.getResponse()?.data.status.code == 14) ||  (e.statusCode == 401 && e.getResponse()?.data.status.code == 14) ) {
        	// Not possible to recover from status.code == 14
            if ( debugLevel(1) ) { 
            	log.error "In HttpResponseException: Received data.stat.code of 14"
            	debugEvent("In HttpResponseException: Received data.stat.code of 14") 
            }
        	apiLost("pollEcobeeAPI() - In HttpResponseException: Received data.stat.code of 14")
		} else if (e.statusCode != 401) { //this issue might comes from exceed 20sec app execution, connectivity issue etc.
            atomicState.connected = "warn"
            generateEventLocalParams() // Update the connected state at the thermostat devices
			runIn(reAttemptPeriod, "pollEcobeeAPI") // retry to poll
		} else if (e.statusCode == 401) { // Status.code other than 14
			atomicState.reAttemptPoll = atomicState.reAttemptPoll + 1
			if ( debugLevel(1) ) { log.warn "reAttempt refreshAuthToken to try = ${atomicState.reAttemptPoll}" }
			if (atomicState.reAttemptPoll <= 3) {
               	atomicState.connected = "warn"
           		generateEventLocalParams() // Update the connected state at the thermostat devices
				runIn(reAttemptPeriod, "pollEcobeeAPI")
			} else {
               	if ( debugLevel(1) ) { log.error "Unable to poll EcobeeAPI after three attempts. Will try to refresh authtoken." }
                debugEvent( "Unable to poll EcobeeAPI after three attempts. Will try to refresh authtoken." )
                refreshAuthToken()
			}
		}    
    } catch (java.util.concurrent.TimeoutException e) {
		if ( debugLevel(2) ) { log.warn "pollEcobeeAPI(), TimeoutException: ${e}." }
//        debugEventFromParent(child, "___exception polling children: " + e)
		// Likely bad luck and network overload, move on and let it try again
        
	} catch (Exception e) {
    	if ( debugLevel(1) ) { log.error "pollEcobeeAPI(): General Exception: ${e}." }
    }
    if ( debugLevel(4) ) { log.debug "<===== Leaving pollEcobeeAPI() results: ${result}" }
	return result
    
}


// poll() will be called on a regular interval using a runEveryX command
void poll() {
	// def devices = getChildDevices()
	// devices.each {pollChild(it)}
   //  TODO: if ( readyForAuthRefresh() ) { refreshAuthToken() } // Use runIn to make this feasible?
	
    // Check to see if we are connected to the API or not
    if (apiConnected() == "lost") {
    	if ( debugLevel(1) ) { log.warn "poll() - Skipping poll() due to lost API Connection" }
    } else {
    	if ( debugLevel(4) ) { log.debug "poll() - Polling children with pollChildren(null)" }
    	pollChildren(null) // Poll ALL the children at the same time for efficiency
    }
}


def availableModes(child) {
	
	def tData = atomicState.thermostats[child.device.deviceNetworkId]
    if ( debugLevel(3) ) { 
		debugEvent ("atomicState.thermostats = ${atomicState.thermostats}")
		debugEvent ("Child DNI = ${child.device.deviceNetworkId}")
		debugEvent("Data = ${tData}")
	}

	if(!tData)
	{
		if ( debugLevel(1) ) { log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling" }

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
	def tData = atomicState.thermostats[child.device.deviceNetworkId]
	if ( debugLevel(3) ) { 
    	debugEvent ("atomicState.thermostats = ${atomicState.thermostats}")
		debugEvent ("Child DNI = ${child.device.deviceNetworkId}")	
		debugEvent("Data = ${tData}")
    }

	if(!tData) {
		if ( debugLevel(1) ) { log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling" }

		// TODO: flag device as in error state
		// child.errorState = true

		return null
	}

	def mode = tData.data.thermostatMode

	mode
}

def updateSensorData() {
	if ( debugLevel(4) ) { log.debug "Entered updateSensorData() " }
 	def sensorCollector = [:]
                
	atomicState.remoteSensors.each {
		it.each {
			if ( it.type == "ecobee3_remote_sensor" ) {
				// Add this sensor to the list
				def sensorDNI = "ecobee_sensor-" + it?.id + "-" + it?.code 
				if ( debugLevel(5) ) { log.info "sensorDNI == ${sensorDNI}" }
            	                
				def temperature = ""
				def occupancy = ""
                            
				it.capability.each { cap ->
					if (cap.type == "temperature") {
                    	if ( debugLevel(4) ) { log.debug "updateSensorData() - Sensor (DNI: ${sensorDNI}) temp is ${cap.value}" }
                        if ( cap.value.isNumber() ) { // Handles the case when the sensor is offline, which would return "unknown"
							temperature = cap.value as Double
							temperature = wantMetric() ? (temperature / 10).toDouble().round(1) : (temperature / 10).toDouble().round(0)
                        } else if (cap.value == "unknown") {
                        	// TODO: Do something here to mark the sensor as offline?
                            if ( debugLevel(1) ) { log.warn "updateSensorData() - sensor (DNI: ${sensorDNI}) returned unknown temp value. Perhaps it is unreachable." }
                            // Need to mark as offline somehow
                            temperature = "unknown"
                            
                        } else {
                        	 if ( debugLevel(1) ) { log.debug "updateSensorData() - sensor (DNI: ${sensorDNI}) returned ${cap.value}." }
                        }
					} else if (cap.type == "occupancy") {
						if(cap.value == "true") {
							occupancy = "active"
            	        } else if (cap.value == "unknown") {
                        	// Need to mark as offline somehow
                            if ( debugLevel(2) ) { log.warn "Setting sensor occupancy to unknown" }
                            occupancy = "unknown"
                        } else {
							occupancy = "inactive"
						}
                            
					}
				}
                                            
				// TODO: Test the "unknown" populated data
				def sensorData = [
					temperature: ((temperature == "unknown") ? "unknown" : myConvertTemperatureIfNeeded(temperature, "F", 1)),
					motion: occupancy
				]
				sensorCollector[sensorDNI] = [data:sensorData]
                if ( debugLevel(4) ) { log.debug "sensorCollector being updated with sensorData: ${sensorData}" }
                
			} else if (it.type == "thermostat") { 
            	// Extract the occupancy status
            
            } // end thermostat else if
		} // End it.each loop
	} // End remoteSensors.each loop
	atomicState.remoteSensorsData = sensorCollector
	if ( debugLevel(4) ) { log.debug "updateSensorData(): found these remoteSensors: ${sensorCollector}" }
                
}

def updateThermostatData() {
	// Create the list of thermostats and related data
	atomicState.thermostats = atomicState.thermostatData.thermostatList.inject([:]) { collector, stat ->
		def dni = [ app.id, stat.identifier ].join('.')

		if ( debugLevel(3) ) { log.debug "Updating dni $dni, Got weather? ${stat.weather.forecasts[0].weatherSymbol.toString()}" }
        
        def thermSensor = stat.remoteSensors.find { it.type == "thermostat" }
        if ( debugLevel(3) ) { 
        	log.debug "updateThermostatData() - thermSensor == ${thermSensor}" 
        	debugEvent( "updateThermostatData() - thermSensor == ${thermSensor}" )
        }
        
        def occupancyCap = thermSensor.capability.find { it.type == "occupancy" }
        if ( debugLevel(3) ) { 
        	log.debug "updateThermostatData() - occupancyCap = ${occupancyCap} value = ${occupancyCap.value}"
        	debugEvent( "updateThermostatData() - occupancyCap = ${occupancyCap}  value = ${occupancyCap.value}" )
        }
        
        def occupancy =  occupancyCap.value
        
        
        if ( debugLevel(3) ) { 
        	log.debug "Program data: ${stat.program}  "
            log.debug "Current climate (ref): ${stat.program?.currentClimateRef}" 
        }
        
        def currentClimateName = ""
		def currentClimateId = ""
        
        if (stat.program?.currentClimateRef) {
        	currentClimateName = (stat.program.climates.find { it.climateRef == stat.program.currentClimateRef }).name
        	currentClimateId = stat.program.currentClimateRef
        	if ( debugLevel(3) ) { log.debug "currentClimateName = ${currentClimateName}" }
        } else {
        	if ( debugLevel(1) ) { log.error "updateThermostatData() - No climateRef was found" }
            currentClimateName = ""
        	currentClimateId = ""        	
        }
        
        
        
        def usingMetric = wantMetric() // cache the value to save the function calls
        def tempTemperature = myConvertTemperatureIfNeeded( (stat.runtime.actualTemperature / 10), "F", (usingMetric ? 1 : 0))
        def tempHeatingSetpoint = myConvertTemperatureIfNeeded( (stat.runtime.desiredHeat / 10), "F", (usingMetric ? 1 : 0))
        def tempCoolingSetpoint = myConvertTemperatureIfNeeded( (stat.runtime.desiredCool / 10), "F", (usingMetric ? 1 : 0))
        def tempWeatherTemperature = myConvertTemperatureIfNeeded( ((stat.weather.forecasts[0].temperature / 10)), "F", (usingMetric ? 1 : 0))
        

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
			humidity: stat.runtime.actualHumidity,
            motion: (occupancy == "true") ? "active" : "inactive",
			thermostatOperatingState: getThermostatOperatingState(stat),
			weatherSymbol: stat.weather.forecasts[0].weatherSymbol.toString(),
			weatherTemperature: usingMetric ? tempWeatherTemperature : tempWeatherTemperature.toInteger()
		]
        
		data["temperature"] = data["temperature"] ? ( wantMetric() ? data["temperature"].toDouble() : data["temperature"].toDouble().toInteger() ) : data["temperature"]
		data["heatingSetpoint"] = data["heatingSetpoint"] ? ( wantMetric() ? data["heatingSetpoint"].toDouble() : data["heatingSetpoint"].toDouble().toInteger() ) : data["heatingSetpoint"]
		data["coolingSetpoint"] = data["coolingSetpoint"] ? ( wantMetric() ? data["coolingSetpoint"].toDouble() : data["coolingSetpoint"].toDouble().toInteger() ) : data["coolingSetpoint"]
        data["weatherTemperature"] = data["weatherTemperature"] ? ( wantMetric() ? data["weatherTemperature"].toDouble() : data["weatherTemperature"].toDouble().toInteger() ) : data["weatherTemperature"]
        
		
		if ( debugLevel(4) ) { 
        	log.debug "Event Data = ${data}"
        	debugEvent( "Event Data = ${data}" ) 
		}

		collector[dni] = [data:data]
		return collector
	}
				
}

def getThermostatOperatingState(stat) {

	def equipStatus = (stat.equipmentStatus.size() > 0) ? stat.equipmentStatus : 'Idle'	
	equipStatus = equipStatus.trim().toUpperCase()
    
    if ( debugLevel(4) ) { log.debug "getThermostatOperatingState() - equipStatus == ${equipStatus}" }
    
	def currentOpState = equipStatus.contains('HEAT')? 'heating' : (equipStatus.contains('COOL')? 'cooling' : 
    	equipStatus.contains('FAN')? 'fan only': 'idle')
	return currentOpState
}


def getChildThermostatDeviceIdsString(singleStat = null) {
	if(!singleStat) {
    	if ( debugLevel(2) ) { log.debug "getChildThermostatDeviceIdsString() - !singleStat" }
		return thermostats.collect { it.split(/\./).last() }.join(',')
	} else {
    	// Only return the single thermostat
        def ecobeeDevId = singleStat.device.deviceNetworkID.split(/\./).last()
        if ( debugLevel(4) ) { log.debug "Received a single thermostat, returning the Ecobee Device ID as a String: ${ecobeeDevId}" }
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
    if ( debugLevel(2) ) { log.debug "refreshing auth token" }
    debugEvent("Entered refreshAuthToken() ...")

	if(!atomicState.refreshToken) {
        if ( debugLevel(1) ) { 
        	log.error "refreshAuthToken() - There is no refreshToken stored! Unable to refresh OAuth token."
        	debugEvent("refreshAuthToken() - There is no refreshToken stored! Unable to refresh OAuth token.")
        }
    	apiLost("refreshAuthToken() - No refreshToken")
        return
    } else if ( !readyForAuthRefresh() ) {
    	// Not ready to refresh yet
        if ( debugLevel(3) ) { 
        	log.debug "refreshAuthToken() - Not time to refresh yet, there is still time left before expiration."
			debugEvent("Entered refreshAuthToken(): There is still time left before expiration.")        
        }
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
                    if ( debugLevel(3) ) { 
                    	log.debug "refreshAuthToken() - 200 Response received - Extracting info." 
                    	debugEvent("refreshAuthToken() - 200 Response received - Extracting info") 
                    }
                    
                    jsonMap = resp.data // Needed to work around strange bug that wasn't updating atomicState when accessing resp.data directly
                    if ( debugLevel(3) ) { log.debug "resp.data = ${resp.data} -- jsonMap is? ${jsonMap}" }

                    if(jsonMap) {
                        if ( debugLevel(4) ) { log.debug "resp.data == ${resp.data}, jsonMap == ${jsonMap}" }
                        debugEvent("Response = ${resp.data}, jsonMap == ${jsonMap}")
						
                        atomicState.refreshToken = jsonMap.refresh_token
                        
                        // TODO - Platform BUG: This was not updating the atomicState values for some reason if we use resp.data directly??? 
                        // 		  Workaround using jsonMap for authToken                       
                        if ( debugLevel(2) ) { log.debug "atomicState.authToken before: ${atomicState.authToken}" }
                        def oldAuthToken = atomicState.authToken
                        atomicState.authToken = jsonMap?.access_token  
						if ( debugLevel(2) ) { log.debug "atomicState.authToken after: ${atomicState.authToken}" }
                        if (oldAuthToken == atomicState.authToken) { 
                        	if ( debugLevel(1) ) { log.warn "WARN: atomicState.authToken did NOT update properly! This is likely a transient problem." }
                            atomicState.connected = "warn"
							generateEventLocalParams() // Update the connected state at the thermostat devices
						}

                        
                        // Save the expiry time to optimize the refresh
                        if ( debugLevel(3) ) { log.debug "Expires in ${resp?.data?.expires_in} seconds" }
                        atomicState.authTokenExpires = (resp?.data?.expires_in * 1000) + now()

						if ( debugLevel(3) ) { 
                        	debugEvent("Refresh Token = atomicState =${atomicState.refreshToken}  == in: ${resp?.data?.refresh_token}")
                        	debugEvent("OAUTH Token = atomicState ${atomicState.authToken} == in: ${resp?.data?.access_token}")
                        }

                        if(atomicState.action && atomicState.action != "") {
                            if ( debugLevel(4) ) { 
                            	log.debug "Token refreshed. Executing next action: ${atomicState.action}" 
                    			debugEvent("Token refreshed. Executing next action: ${atomicState.action}")
                            }

                            "${atomicState.action}"()

                            // Reset saved action
                            atomicState.action = ""
                        }

                    } else {
                    	if ( debugLevel(2) ) { debugEvent("No jsonMap??? ${jsonMap}") }
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
        	if ( debugLevel(1) ) { log.error "refreshAuthToken() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}" }
            debugEvent("refreshAuthToken() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}")
            // log.error "refreshAuthToken() >> Error: e.statusCode ${e.statusCode}. full exception: ${e} response? data: ${e.getResponse()?.getData()} headers ${e.getResponse()?.getHeaders()}}"
           	def reAttemptPeriod = 300 // in sec
			if ( (e.statusCode == 500 && e.getResponse()?.data.status.code == 14) || (e.statusCode == 401 && e.getResponse()?.data.status.code == 14) ) {
            	if ( debugLevel(1) ) { 
                	log.error "refreshAuthToken() - Received data.status.code = 14" 
                	debugEvent("refreshAuthToken() - Received data.status.code = 14") 
                }
            	apiLost("refreshAuthToken() - Received data.status.code = 14" )
            } else if (e.statusCode != 401) { //this issue might comes from exceed 20sec app execution, connectivity issue etc.
            	if ( debugLevel(1) ) { 
                	log.error "refreshAuthToken() - e.statusCode: ${e.statusCode}" 
                	debugEvent("refreshAuthToken() - Received data.status.code = 14") 
                }
            	atomicState.connected = "warn"
            	generateEventLocalParams() // Update the connected state at the thermostat devices
				runIn(reAttemptPeriod, "refreshAuthToken")
			} else if (e.statusCode == 401) { // status.code other than 14
				atomicState.reAttempt = atomicState.reAttempt + 1
				if ( debugLevel(1) ) { log.warn "reAttempt refreshAuthToken to try = ${atomicState.reAttempt}" }
				if (atomicState.reAttempt <= 3) {
                	atomicState.connected = "warn"
            		generateEventLocalParams() // Update the connected state at the thermostat devices
					runIn(reAttemptPeriod, "refreshAuthToken")
				} else {
                	// More than 3 attempts, time to give up and notify the end user
                    if ( debugLevel(1) ) { log.error "More than 3 attempts to refresh tokens. Giving up" }
                    debugEvent("More than 3 attempts to refresh tokens. Giving up")
                	apiLost("refreshAuthToken() - More than 3 attempts to refresh token. Have to give up")
				}
            }
        } catch (java.util.concurrent.TimeoutException e) {
			if ( debugLevel(1) ) { log.error "refreshAuthToken(), TimeoutException: ${e}." }
			// Likely bad luck and network overload, move on and let it try again
            runIn(300, "refreshAuthToken")
        } catch (Exception e) {
        	if ( debugLevel(1) ) { log.error "refreshAuthToken(), General Exception: ${e}." }
        }
    }
}



def resumeProgram(child, deviceId) {
	if ( debugLevel(5) ) { debugEventFromParent(child, "Entered resumeProgram for deviceID: ${deviceID}") }

//    def thermostatIdsString = getChildDeviceIdsString()
//    log.debug "resumeProgram children: $thermostatIdsString"

	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}]}'
	//, { "type": "sendMessage", "params": { "text": "Setpoint Updated" } }
	def result = sendJson(jsonRequestBody)
	if ( debugLevel(3) ) { debugEventFromParent(child, "resumeProgram(child) with result ${result}") }
    
	// Update the data after giving the thermostat a chance to consume the command and update the values
	return result
}

def setHold(child, heating, cooling, deviceId, sendHoldType, fanMode="") {
	int h = (getTemperatureScale() == "C") ? (cToF(heating) * 10) : (heating * 10)
	int c = (getTemperatureScale() == "C") ? (cToF(cooling) * 10) : (cooling * 10)
    

    if ( debugLevel(3) ) { 
    	log.debug "setHold(): setpoints____ - h: ${heating} - ${h}, c: ${cooling} - ${c}, setHoldType: ${sendHoldType}" 
    	debugEventFromParent(child, "setHold(): setpoints____ - h: ${heating} - ${h}, c: ${cooling} - ${c}, setHoldType: ${sendHoldType}")
    }
//    def thermostatIdsString = getChildDeviceIdsString()
	
	
	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": '+sendHoldType+' } } ]}'
    if ( debugLevel(4) ) { debugEventFromParent(child, "about to sendJson with jsonRequestBody (${jsonRequestBody}") }
    
//	def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + thermostatIdsString + '","includeRuntime":true},"functions": [{"type": "resumeProgram"}, { "type": "setHold", "params": { "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": "indefinite" } } ]}'
	def result = sendJson(child, jsonRequestBody)
    if ( debugLevel(3) ) { debugEventFromParent(child, "setHold: heating: ${h}, cooling: ${c} with result ${result}") }
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

def setProgram(child, program, deviceId, sendHoldType) {

	def tstatSettings 
    tstatSettings = ((sendHoldType != null) && (sendHoldType != "")) ?
				[holdClimateRef:"${program}", holdType:"${sendHoldType}"
				] :
				[holdClimateRef:"${program}"
				]
    
	def jsonRequestBody = build_body_request('setHold',null,deviceId,tstatSettings,null).toString()
	//def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "holdClimateRef": '+ program + '", "coolHoldTemp": '+c+',"heatHoldTemp": '+h+', "holdType": '+sendHoldType+' } } ]}'
	// def jsonRequestBody = '{"selection":{"selectionType":"thermostats","selectionMatch":"' + deviceId + '","includeRuntime":true},"functions": [{ "type": "setHold", "params": { "holdClimateRef": "'+program+'", "holdType": '+sendHoldType+' } } ]}'
    
    if ( debugLevel(4) ) { 
    	debugEventFromParent(child, "about to sendJson with jsonRequestBody (${jsonRequestBody}")     	
    }
    
    
	def result = sendJson(child, jsonRequestBody)
	if ( debugLevel(3) ) { debugEventFromParent(child, "setProgram with result ${result}") }    
    return result
    
 //{"functions":[{"type":"setHold","params":{"holdClimateRef":"home","holdType":"Temporary"}}],"selection":{"selectionType":"thermostats","selectionMatch":"312989153500"}}

}

/*

// Only handles one Thermostat at this time
def setProgram(child, program, thermostatId) {
	// Currently only handles "home", "away", & "sleep" which are the baseline modes available on Ecobee 3
    if ( debugLevel(4) ) { 
    	log.debug "In setProgram()" 
        debugEvent ( "In setProgram()" )
        debugEventFromParent(child, "In setProgram()")
	}
    
    def okResults = false
	def currentProgramName = atomicState.thermostats[child.device.deviceNetworkId].data.currentProgramName    
	def currentProgramType = atomicState.thermostats[child.device.deviceNetworkId].data.currentProgramId
    
    if ( debugLevel(3) ) { 
    	log.debug "In setProgram. New program: ${program}, ThermId: ${thermostatId}, currentProgramName: ${currentProgramName}, currentProgramType: ${currentProgramType}"
        debugEventFromParent (child, "In setProgram. New program: ${program}, ThermId: ${thermostatId}, currentProgramName: ${currentProgramName}, currentProgramType: ${currentProgramType}" )
    }
    
    
	if (currentProgramType == 'VACATION') {
		if ( debugLevel(1) ) {
			log.warn "setThisTstatClimate>thermostatId = ${thermostatId}, cannot do the prog switch due to vacation settings"
			debugEventFromParent(child, "setThisTstatClimate>thermostatId = ${thermostatId}, cannot do the prog switch due to vacation settings" )
		}
		okResults = false
	}
    
    def newProgramType = program.toLowerCase() // (program.toLowerCase() == "away") ? "away" : ( (program.toLowerCase() == "sleep") ? "sleep" : "home" )
    if ( debugLevel(3) ) { 
    	log.debug "newProgramType = ${newProgramType}" 
        debugEventFromParent(child, "newProgramType = ${newProgramType}" )        
        }
    
	// If climate is different from current one, then change it to the given climate
	if (currentProgramType != newProgramType) {    
		
        if ( debugLevel(4) ) {
        	debugEventFromParent(child, "newProgramType (${newProgramType}) != currentProgramType (${currentProgramType}) - about to call resumeProgram() - thermostatId: ${thermostatId}")
        }
		resumeProgram(child, thermostatId)
        
		def tstatParams = ((settings.holdType != null) && (settings.holdType.trim() != "")) ?
				[holdClimateRef:"${newProgramType}", holdType:"${settings.holdType.trim()}"
				] :
				[holdClimateRef:"${newProgramType}"
				]
           	
		// def bodyReq = build_body_request('setHold',null, data.thermostatList[i].identifier,tstatParams)	
        def bodyReq = build_body_request('setHold',null, thermostatId,tstatParams)	
		def statusCode=true
		int j=0        
		while ((statusCode) && (j++ <2)) { // retries once if api call fails
            
            if ( debugLevel(5) ) { debugEventFromParent(child, "about to call api() setHold with bodyReq (${bodyReq})") }
            def success = {}
			apiHelper(child, 'setHold', bodyReq) { resp ->
            	if ( debugLevel(3) ) { 
                	debugEventFromParent(child, "results from API: resp ${resp}, statusCode: ${resp.data.status.code}  message: ${resp.data.status.message}")
                }
                
				statusCode = resp.data.status.code
				def message = resp.data.status.message
				if (!statusCode) {
					// Post the setClimate value     
					// sendEvent(name: 'setClimate', value: climateName)
                    atomicState.thermostats[child.device.deviceNetworkId].data.currentProgramName = newProgramType.capitalize()
					atomicState.thermostats[child.device.deviceNetworkId].data.currentProgramId = newProgramType
					
                    if ( debugLevel(3) ) {
                    	log.debug "setProgram() - Success in setting climate: newProgram: ${newProgramName}"
                        debugEventFromParent(child, "setProgram() - Success in setting climate: newProgram: ${newProgramName}" )
					}
                    okResults = true
				} else {
                	okResults = false
					if ( debugLevel(1) ) {
						log.error "setProgram() ERROR ${statusCode.toString()},message=${message} while setting climate ${climateName} for thermostatId =${data.thermostatList[i].identifier}"
                        debugEventFromParent(child, "setProgram() ERROR ${statusCode.toString()},message=${message} while setting climate ${climateName} for thermostatId =${data.thermostatList[i].identifier}" )
					}
                        
					// introduce a 1 second delay before re-attempting any other command                    
					def cmd= []           
					cmd << "delay 1000"                    
					cmd            
				} // end if statusCode
			} // end api call
		} // end while
	} else { // end if currentProgram != newProgram
    	okResults = true // Nothing to change, but still okay
        if ( debugLevel(4) ) {
        	debugEventFromParent(child, "currentProgramType == newProgramType? currentProgramType = ${currentProgramType}   newProgramType = ${newProgramType}")
        }
    }
    return okResults
}

*/



def sendJson(child = null, String jsonBody) {

	def returnStatus = false
	def cmdParams = [
			uri: apiEndpoint,
			path: "/1/thermostat",
			headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
			body: jsonBody
	]

	try{
    	def statusCode = true
        int j=0
        
        while ( (statusCode) && (j++ < 2) ) { // only retry once
		httpPost(cmdParams) { resp ->
        	statusCode = resp.data.status.code

			if ( debugLevel(4) && (child) ) { debugEventFromParent(child, "sendJson() resp.status ${resp.status}, resp.data: ${resp.data}, statusCode: ${statusCode}") }
			
            // TODO: Perhaps add at least two tries incase the first one fails?
			if(resp.status == 200) {				
				if ( debugLevel(4) ) { log.debug "updated ${resp.data}" }
				returnStatus = resp.data.status.code
				if (resp.data.status.code == 0) {
					if ( debugLevel(3) ) { log.debug "Successful call to ecobee API." }
						atomicState.connected = "full"
                    	generateEventLocalParams()
                        statusCode=false
				} else {
					if ( debugLevel(1) ) { 
                    	log.debug "Error return code = ${resp.data.status.code}"
						debugEvent("Error return code = ${resp.data.status.code}")
                    }
				}
			} else {
				if ( debugLevel(1) ) { 
                	log.error "sent Json & got http status ${resp.status} - ${resp.status.code}"
					debugEvent ("sent Json & got http status ${resp.status} - ${resp.status.code}") 
                }

				//refresh the auth token
				if (resp.status == 500 && resp.status.code == 14) {
					//log.debug "Storing the failed action to try later"
					if ( debugLevel(2) ) { 
                    	log.debug "Refreshing your auth_token!" 
						debugEvent ("sendJson() - Refreshing OAUTH Token")
                    }
					refreshAuthToken()
					return false // No way to recover from a status.code 14
				} else {
					if ( debugLevel(2) ) { 
                    	debugEvent ("Possible Authentication error, invalid authentication method, lack of credentials, etc. Status: ${resp.status} - ${resp.status.code} ")
						log.error "Possible Authentication error, invalid authentication method, lack of credentials, etc. Status: ${resp.status} - ${resp.status.code} "
                    }
                    atomicState.connected = "warn"
                    generateEventLocalParams()
                    if (j == 2) { // Go ahead and refresh on the second pass through
                    	refreshAuthToken() 
                        return false
                    } 
					
				}
			} // resp.status if/else
		} // HttpPost
        } // While loop
	} catch (groovyx.net.http.HttpResponseException e) {
    	if ( debugLevel(1) ) { 
        	log.error "sendJson() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}"
        	debugEvent( "sendJson() >> HttpResponseException occured. Exception info: ${e} StatusCode: ${e.statusCode}  response? data: ${e.getResponse()?.getData()}" )
        }
		atomicState.connected = "warn"
        generateEventLocalParams()
        refreshAuthToken()
		return false
    } catch(Exception e) {
    	// Might need to further break down 
		if ( debugLevel(1) ) { 
        	log.error "sendJson() - Exception Sending Json: " + e
			debugEvent ("Exception Sending JSON: " + e)
        }
        atomicState.connected = "warn"
        generateEventLocalParams()
        if (j == 2) { // Go ahead and refresh on the second pass through
        	refreshAuthToken()
			return false
		}
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
	if ( debugLevel(3) ) { log.debug "Generating AppDebug Event: ${results}" }
	sendEvent (results)

}

def debugEventFromParent(child, message) {

	 def data = [
            	debugEventFromParent: message
            ]         
	if (child) { child.generateEvent(data) }
	/*
	if (child != null) {
    	child.sendEvent("name":"debugEventFromParent", "value":message, "description":message, displayed: true, isStateChange: true)        
    }
    */
}

//send both push notification and mobile activity feeds
def sendPushAndFeeds(notificationMessage) {
    if ( debugLevel(1) ) { 
    	log.warn "sendPushAndFeeds >> notificationMessage: ${notificationMessage}"
    	log.warn "sendPushAndFeeds >> atomicState.timeSendPush: ${atomicState.timeSendPush}"
    }
    if (atomicState.timeSendPush) {
        if ( (now() - atomicState.timeSendPush) >= (1000 * 60 * 60 * 1)){ // notification is sent to remind user no more than once per hour
            sendPush("Your Ecobee thermostat " + notificationMessage)
            sendActivityFeeds(notificationMessage)
            atomicState.timeSendPush = now()
        }
    } else {
        sendPush("Your Ecobee thermostat " + notificationMessage)
        sendActivityFeeds(notificationMessage)
        atomicState.timeSendPush = now()
    }
    // atomicState.authToken = null
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
	if ( debugLevel(5) ) { log.info "cToF entered with ${temp}" }
	return (temp * 1.8 + 32) as Double
    // return celsiusToFahrenheit(temp)
}
private def fToC(temp) {	
	if ( debugLevel(5) ) { log.info "fToC entered with ${temp}" }
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

private def apiLost(where = "not specified") {
    if ( debugLevel(1) ) { 
    	log.error "apiLost() - ${where}: Lost connection with APIs. unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again"
    	debugEvent("apiLost() - ${where}: Lost connection with APIs. unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again")
    }
	
    // provide cleanup steps when API Connection is lost
	def notificationMessage = "is disconnected from SmartThings/Ecobee, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
    atomicState.connected = "lost"
    // atomicState.authToken = null
    
    sendPushAndFeeds(notificationMessage)
	generateEventLocalParams()

    debugEvent("unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again")
    
    // Notify each child that we lost so it gets logged
    if ( debugLevel(3) ) {
    	def d = getChildDevices()
    	d?.each { oneChild ->
    		if ( debugLevel(4) ) { log.debug "apiLost() - notifying each child: ${oneChild} of loss" }
        	debugEventFromParent(oneChild, "apiLost() - ${where}: Lost connection with APIs. unscheduling Polling and refreshAuthToken. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again")     	   
		}
    }
    
    unschedule("poll")
    unschedule("refreshAuthToken")
    runEvery3Hours("notifyApiLost")
}

def notifyApiLost() {
	def notificationMessage = "is disconnected from SmartThings/Ecobee, because the access credential changed or was lost. Please go to the Ecobee (Connect) SmartApp and re-enter your account login credentials."
    if ( atomicState.connected == "lost" ) {
    	generateEventLocalParams()
		sendPushAndFeeds(notificationMessage)
        debugEvent("notifyApiLost() - API Connection Previously Lost. User MUST reintialize the connection with Ecobee by running the SmartApp and logging in again")
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
	if ( debugLevel(4) ) { log.debug "Entered readyForAuthRefresh() " }
    def timeLeft 
    
    timeLeft = atomicState.authTokenExpires ? ((atomicState.authTokenExpires - now()) / 1000 / 60) : 0
    log.debug "timeLeft until expiry (in min): ${timeLeft}"
    
    // Since this runs as part of poll() we can be a bit more conservative on the time before renewing the token
    // def pollInterval = settings.pollingInterval ?: 5
    // def ready = timeLeft <= ((pollInterval * 3) + 2)
    def ready = timeLeft <= 29
    if ( debugLevel(4) ) { log.debug "Ready for authRefresh? ${ready}" }
    return ready
}


private debugLevel(level=3) {
	def debugLvlNum = settings.debugLevel?.toInteger() ?: 3
    def wantedLvl = level.toInteger()
    
    return ( debugLvlNum >= wantedLvl )
}




// Ecobee API Related Functions - from Yves code
private void apiHelper(child=null, method, args, success = {}) {
	String URI_ROOT = "${getApiEndpoint()}/1"
    
    if ( debugLevel(5) ) {
    	log.error "apiHelper(): Entered. URI_ROOT = ${URI_ROOT}"
		debugEvent ("apiHelper(): Entered. URI_ROOT = ${URI_ROOT}")
		if(child) { debugEventFromParent(child, "api(): Entered. URI_ROOT = ${URI_ROOT}") }
    }
    
	 if (apiConnected() == "lost") {
     	// Unable to perform action since the API is not connected
        if ( debugLevel(1) ) {
        	log.error "apiHelper(): Unable to execute ${method} because we are not currently connected to the Ecobee API!"
            debugEvent ("apiHelper(): Unable to execute ${method} because we are not currently connected to the Ecobee API!")
            if(child) { debugEventFromParent(child, "ERROR: api(): Unable to execute ${method} because we are not currently connected to the Ecobee API!") }
        }
     	return
     }

	def args_encoded = java.net.URLEncoder.encode(args.toString(), "UTF-8")
    if ( debugLevel(4) ) {
    	debugEvent ("apiHelper() args: ${args.toString()}  args_encoded: ${args_encoded}")
    	if(child) { debugEventFromParent(child, "apiHelper() args: ${args.toString()}  args_encoded: ${args_encoded}") }
    }
	def methods = [
		'thermostatSummary': 
			[uri:"${URI_ROOT}/thermostatSummary?format=json&body=${args_encoded}", 
      			type:'get'],
		'thermostatInfo': 
			[uri:"${URI_ROOT}/thermostat?format=json&body=${args_encoded}", 
          		type: 'get'],
		'setThermostatSettings':
			[uri: "${URI_ROOT}/thermostat?format=json", type: 'post'],
		'setHold': 
			[uri: "${URI_ROOT}/thermostat?format=json", type: 'post'],
		'resumeProgram': 
			[uri: "${URI_ROOT}/thermostat?format=json", type: 'post'],
		'createVacation': 
			[uri: "${URI_ROOT}/thermostat?format=json", type: 'post'],
		'deleteVacation': 
			[uri: "${URI_ROOT}/thermostat?format=json", type: 'post'],
		'getGroups': 
			[uri: "${URI_ROOT}/group?format=json&body=${args_encoded}",
			type: 'get'],
		'updateGroup': 
			[uri: "${URI_ROOT}/group?format=json", type: 'post'],
		'updateClimate': 
			[uri: "${URI_ROOT}/thermostat?format=json", type: 'post'],
		'controlPlug': 
			[uri: "${URI_ROOT}/thermostat?format=json", type: 'post'],
		'runtimeReport': 
			[uri:"${URI_ROOT}/runtimeReport?format=json&body=${args_encoded}", 
          		type: 'get'],
		]
        
        
	def request = methods.getAt(method)
    
	if ( debugLevel(3) ) {
		log.debug "apiHelper() about to call doRequest with (unencoded) args = ${args}"
		debugEvent( "apiHelper() about to call doRequest with (unencoded) args = ${args}" )
        if(child) { debugEventFromParent(child, "apiHelper() about to call doRequest with (unencoded) args = ${args}") }
	}
    
	doRequest(child, request.uri, args_encoded, request.type, success)
    
	/*
    if ( debugLevel(4) ) {
		log.debug "apiHelper() after doRequest: ${success} "
		debugEvent( "apiHelper() after doRequest: ${success} " )
        if(child) { debugEventFromParent(child, "apiHelper() after doRequest: ${success}") }
	}
	*/
}

// Need to be authenticated in before this is called. So don't call this. Call api.
private void doRequest(child=null, uri, args, type, success) {
	def params = [
		uri: uri,
		headers: [
			'Authorization': "Bearer ${atomicState.authToken}",
			'Content-Type': "application/json",
			'charset': "UTF-8",
			'Accept': "application/json"
		],
		body: args
	]
	try {
		if ( debugLevel(4) ) {
//			sendEvent name: "verboseTrace", value: "doRequest>token= ${data.auth.access_token}"
			log.debug "doRequest - about to ${type} with uri ${params.uri}, (encoded)args= ${args}"
			debugEvent ( "doRequest -  ${type}> uri ${params.uri}, args= ${args}" )
            if(child) { debugEventFromParent(child, "doRequest - about to ${type} with uri ${params.uri}, (encoded)args= ${args}") }
		}
		if (type == 'post') {
        	if ( debugLevel(4) ) { debugeEventFromParent(child, "about to perform httpPostJson with params: ${params}" ) }
			httpPostJson(params, success)

		} else if (type == 'get') {
			params.body = null // parameters already in the URL request
			httpGet(params, success)
		}
		atomicState.apiConnected = "full"
		generateEventLocalParams()

	} catch (java.net.UnknownHostException e) {
    	if ( debugLevel(1) ) {
			log.error "doRequest() Unknown host - check the URL " + params.uri
            debugEvent( "doRequest() Unknown host - check the URL " + params.uri )	
            if(child) { debugEventFromParent(child, "ERROR: doRequest() Unknown host - check the URL " + params.uri) }
		}            
		atomicState.apiConnected = "warn"
		generateEventLocalParams()
	} catch (java.net.NoRouteToHostException e) {
    	if (debugLevel(1) ) {
			log.error "doRequest() No route to host - check the URL " + params.uri
			debugEvent( "doRequest() No route to host - check the URL " + params.uri )		
        }
        atomicState.apiConnected = "warn"
		generateEventLocalParams()
	} catch (e) {
    	if ( debugLevel(1) ) {
			log.debug "doRequest() General exception $e for " + params.body
            debugEvent( "doRequest() General exception $e for " + params.body )		
        }
		atomicState.apiConnected = "warn"
		generateEventLocalParams()

	}
}

// tstatType =managementSet or registered (no spaces).  
//		registered is for Smart, Smart-Si & Ecobee thermostats, 
//		managementSet is for EMS thermostat
//		may also be set to a specific locationSet (ex. /Toronto/Campus/BuildingA)
//		may be set to null if not relevant for the given method
// thermostatId may be a list of serial# separated by ",", no spaces (ex. '123456789012,123456789013') 
private def build_body_request(method, tstatType="registered", thermostatId, tstatParams = [],
	tstatSettings = []) {
    if ( debugLevel(5) ) { log.debug "Entered build_body_request()" }
    
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


// iterateSetThermostatSettings: iterate thru all the thermostats under a specific account and set the desired settings
// tstatType =managementSet or registered (no spaces).  May also be set to a specific locationSet (ex./Toronto/Campus/BuildingA)
// settings can be anything supported by ecobee 
//		at https://www.ecobee.com/home/developer/api/documentation/v1/objects/Settings.shtml
void iterateSetThermostatSettings(tstatType, tstatSettings = []) {
	Integer MAX_TSTAT_BATCH = get_MAX_TSTAT_BATCH()
	def tstatlist = null
	Integer nTstats = 0

	def ecobeeType = determine_ecobee_type_or_location(tstatType)
	getThermostatSummary(ecobeeType)
	if (settings.trace) {
		log.debug
			"iterateSetThermostatSettings>ecobeeType=${ecobeeType},about to loop ${data.thermostatCount} thermostat(s)"
		sendEvent name: "verboseTrace", value:
			"iterateSetThermostatSettings>ecobeeType=${ecobeeType},about to loop ${data.thermostatCount} thermostat(s)"
	}
	for (i in 0..data.thermostatCount - 1) {
		def thermostatDetails = data.revisionList[i].split(':')
		def Id = thermostatDetails[0]
		def thermostatName = thermostatDetails[1]
		def connected = thermostatDetails[2]
		if (connected == 'true') {
			if (nTstats == 0) {
				tstatlist = Id
				nTstats = 1
			}
			if ((nTstats > MAX_TSTAT_BATCH) || (i == (data.thermostatCount - 1))) { 
				// process a batch of maximum 25 thermostats according to API doc
				if (settings.trace) {
					sendEvent name: "verboseTrace", value:
						"iterateSetThermostatSettings>about to call setThermostatSettings for ${tstatlist}"
					log.debug "iterateSetThermostatSettings> about to call setThermostatSettings for ${tstatlist}"
				}
				setThermostatSettings("${tstatlist}",tstatSettings)
				tstatlist = Id
				nTstats = 1
			} else {
				tstatlist = tstatlist + "," + Id
				nTstats++ 
			}
		}
	}
}

// thermostatId may be a list of serial# separated by ",", no spaces (ex. '123456789012,123456789013') 
//	if no thermostatId is provided, it is defaulted to the current thermostatId 
// settings can be anything supported by ecobee at https://www.ecobee.com/home/developer/api/documentation/v1/objects/Settings.shtml
void setThermostatSettings(thermostatId,tstatSettings = []) {
   	thermostatId= determine_tstat_id(thermostatId) 	    
	if ( debugLevel(5) ) {
		log.debug "setThermostatSettings>called with values ${tstatSettings} for ${thermostatId}"		
	}
    
	def bodyReq = build_body_request('setThermostatSettings',null,thermostatId,null,tstatSettings)
	def statusCode=true
	int j=0        
	while ((statusCode) && (j++ <2)) { // retries once if api call fails
		apiHelper('setThermostatSettings', bodyReq) {resp ->
			statusCode = resp.data.status.code
			def message = resp.data.status.message
			if (!statusCode) {
				if ( debugLevel(3) ) { log.debug "setThermostatSettings() successful for ${thermostatId} with settings ${tstatSettings}" }            	

			} else {
            	if ( debugLevel(1) ) {
					log.error "setThermostatSettings() error=${statusCode.toString()},message=${message} for ${thermostatId}"
					debugEvent( "setThermostatSettings() error=${statusCode.toString()},message=${message} for ${thermostatId}" )
				} 

				// introduce a 1 second delay before re-attempting any other command                    
				def cmd= []           
				cmd << "delay 1000"                    
				cmd            
			} /* end if statusCode */
		} /* end api call */                
	} /* end for */
}


