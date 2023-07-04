/**
 *  Tado Connect
 *
 *  Copyright 2016 Stuart Buchanan
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
<<<<<<< HEAD
 * 07/02/2018 v2.7 Added some new try catch blocks around parse capability as there were exceptioons after v2.1 occuring for ait conditioners, this now works correctly
=======
 * 07/02/2018 v2.7 Added some new try catch blocks around parse capability as there were exceptioons after v2.1 occuring for air conditioners, this now works correctly
>>>>>>> origin/master
 * 06/02/2018 v2.6 Fixed Commands for those with Heat Cool that do not support Fan Modes
 * 08/06/2017 v2.5 Amended bug where Hot water type was set to WATER, Instead or HOT_WATER, with thanks to @invisiblemountain
 * 08/06/2017 v2.4 Added Device name to DNI, trying to avaid issue with multiple devices in a single Zone
 * 26/05/2017 v2.3 removed erronous jsonbody statements in the coolCommand Function.
 * 26/05/2017 v2.2 Corrected bug with parseCapability function as this was returning the map instead of the value, this would account for lots of strange behaviour.
 * 25/05/2017 v2.1 Added support for Air Condiioners which have a mandatory swing field for all Commands, corrected prevois bugs in v2.0, thanks again to @Jnick
 * 20/05/2017 v2.0 Added support for Air Condiioners which have a mandatory swing field in the heating & cool Commands, thanks again to @Jnick
 * 17/05/2017 v1.9 Corrected issue with the wrong temp unit being used on some thermostat functions when using Farenheit, many thanks again to @Jnick for getting the logs to help diagnose this.
 * 04/05/2017 v1.8 Corrected issue with scheduling which was introduced in v1.7 with merge of pull request. Many thanks to @Jnick for getting the logs to help diagnose this.
 * 17/04/2017 v1.7 General Bugfixes around Tado user presence with thanks to @sipuncher
 * 14/04/2017 v1.6 fixed defects in user presence device polling
 * 06/04/2017 v1.5 scheduled refresh of tado user status every minute (Thanks to @sipuncher for pointing out my mistake)
 * 03/04/2017 v1.4 Added ability to have your Tado Users created as Smarthings Virtual Presence Sensors for use in routines etc..
 * 03/01/2017 v1.3 Corrected Cooling Commands and Set Points issue with incorrect DNI statement with thanks to Richard Gregg
 * 03/12/2016 v1.2 Corrected Values for Heating and Hot Water set Points
 * 03/12/2016 v1.1 Updated to Support Multiple Hubs, and fixed bug in device discovery and creation, however all device types need updated also.
 * 26/11/2016 V1.0 initial release
 */

import java.text.DecimalFormat
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

private apiUrl() 			{ "https://my.tado.com" }
private getVendorName() 	{ "Tado" }
private getVendorIcon()		{ "https://dl.dropboxusercontent.com/s/fvjrqcy5xjxsr31/tado_128.png" }
private getClientId() 		{ appSettings.clientId }
private getClientSecret() 	{ appSettings.clientSecret }
private getServerUrl() 		{ if(!appSettings.serverUrl){return getApiServerUrl()} }

 // Automatically generated. Make future change here.
definition(
    name: "Tado (Connect)",
    namespace: "fuzzysb",
    author: "Stuart Buchanan",
    description: "Tado Integration, This SmartApp supports all Tado Products. (Heating Thermostats, Extension Kits, AC Cooling & Radiator Valves.)",
    category: "SmartThings Labs",
	iconUrl:   "https://dl.dropboxusercontent.com/s/fvjrqcy5xjxsr31/tado_128.png",
	iconX2Url: "https://dl.dropboxusercontent.com/s/jyad58wb28ibx2f/tado_256.png",
	oauth: true,
    singleInstance: false
) {
	appSetting "clientId"
	appSetting "clientSecret"
	appSetting "serverUrl"
}

preferences {
	page(name: "startPage", title: "Tado (Connect) Integration", content: "startPage", install: false)
	page(name: "Credentials", title: "Fetch Tado Credentials", content: "authPage", install: false)
	page(name: "mainPage", title: "Tado (Connect) Integration", content: "mainPage")
	page(name: "completePage", title: "${getVendorName()} is now connected to SmartThings!", content: "completePage")
	page(name: "listDevices", title: "Tado Devices", content: "listDevices", install: false)
    page(name: "listUsers", title: "Tado Users", content: "listUsers", install: false)
	page(name: "advancedOptions", title: "Tado Advanced Options", content: "advancedOptions", install: false)
	page(name: "badCredentials", title: "Invalid Credentials", content: "badAuthPage", install: false)
}
mappings {
	path("/receivedHomeId"){action: [POST: "receivedHomeId", GET: "receivedHomeId"]}
}

def startPage() {
    if (state.homeId) { return mainPage() }
    else { return authPage() }
}

def authPage() {
	log.debug "In authPage"
	def description = null
	log.debug "Prompting for Auth Details."
	description = "Tap to enter Credentials."
	return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage:mainPage, uninstall: false , install:false) {
	   section("Generate Username and Password") {
				input "username", "text", title: "Your Tado Username", required: true
				input "password", "password", title: "Your Tado Password", required: true
			}
	}
}

def mainPage() {
 	if (!state.accessToken){
  		createAccessToken()
  		getToken()
    }
  	getidCommand()
    getTempUnitCommand()
  	log.debug "Logging debug: ${state.homeId}"
	   if (state.homeId) {
       return completePage()
       } else {
         return badAuthPage()
       }
}

def completePage(){
	def description = "Tap 'Next' to proceed"
			return dynamicPage(name: "completePage", title: "Credentials Accepted!", uninstall:true, install:false,nextPage: listDevices) {
				section { href url: buildRedirectUrl("receivedHomeId"), style:"embedded", required:false, title:"${getVendorName()} is now connected to SmartThings!", description:description }
			}
}

def badAuthPage(){
	log.debug "In badAuthPage"
    log.error "login result false"
       		return dynamicPage(name: "badCredentials", title: "Bad Tado Credentials", install:false, uninstall:true, nextPage: Credentials) {
				section("") {
					paragraph "Please check your username and password"
           		}
            }
}

def advancedOptions() {
	log.debug "In Advanced Options"
	def options = getDeviceList()
	dynamicPage(name: "advancedOptions", title: "Select Advanced Options", install:true) {
    	section("Default temperatures for thermostat actions. Please enter desired temperatures") {
      	input("defHeatingTemp", "number", title: "Default Heating Temperature?", required: true)
      	input("defCoolingTemp", "number", title: "Default Cooling Temperature?", required: true)
		}
		section("Tado Override Method") {
      	input("manualmode", "enum", title: "Default Tado Manual Overide Method", options: ["TADO_MODE","MANUAL"], required: true)
    	}
        section(){
    		if (getHubID() == null){
        		input(
            		name		: "myHub"
            		,type		: "hub"
            		,title		: "Select your hub"
            		,multiple		: false
            		,required		: true
            		,submitOnChange	: true
        		)
     		} else {
        		paragraph("Tap done to finish the initial installation.")
     		}
		}
    }
}

def listDevices() {
	log.debug "In listDevices"
	def options = getDeviceList()
	dynamicPage(name: "listDevices", title: "Choose devices", install:false, uninstall:true, nextPage: listUsers) {
		section("Devices") {
			input "devices", "enum", title: "Select Device(s)", required: false, multiple: true, options: options, submitOnChange: true
		}
	}
}

def listUsers() {
	log.debug "In listUsers"
	def options = getUserList()
	dynamicPage(name: "listUsers", title: "Choose users you wish to create a Virtual Smarthings Presence Sensors for", install:false, uninstall:true, nextPage: advancedOptions) {
		section("Users") {
			input "users", "enum", title: "Select User(s)", required: false, multiple: true, options: options, submitOnChange: true
		}
	}
}

def getToken(){
  if (!state.accessToken) {
		try {
			getAccessToken()
			DEBUG("Creating new Access Token: $state.accessToken")
		} catch (ex) {
			DEBUG("Did you forget to enable OAuth in SmartApp IDE settings")
            DEBUG(ex)
		}
	}
}

def receivedHomeId() {
	log.debug "In receivedToken"

	def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${getVendorName()} Connection</title>
        <style type="text/css">
            * { box-sizing: border-box; }
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
                width: 560px;
                padding: 40px;
                /*background: #eee;*/
                text-align: center;
            }
            img {
                vertical-align: middle;
            }
            img:nth-child(2) {
                margin: 0 30px;
            }
            p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                margin-bottom: 0;
            }
        /*
            p:last-child {
                margin-top: 0px;
            }
        */
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
        </head>
        <body>
            <div class="container">
                <img src=""" + getVendorIcon() + """ alt="Vendor icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>Tap 'Done' to continue to Devices.</p>
			</div>
        </body>
        </html>
        """
	render contentType: 'text/html', data: html
}

def buildRedirectUrl(endPoint) {
	log.debug "In buildRedirectUrl"
	log.debug("returning: " + getServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/${endPoint}")
	return getServerUrl() + "/api/token/${state.accessToken}/smartapps/installations/${app.id}/${endPoint}"
}

def getDeviceList() {
  def TadoDevices = getZonesCommand()
  return TadoDevices.sort()
}

def getUserList() {
  def TadoUsers = getMobileDevicesCommand()
  return TadoUsers.sort()
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
  	unsubscribe()
	unschedule()
	initialize()
}

def uninstalled() {
  log.debug "Uninstalling Tado (Connect)"
  revokeAccessToken()
  removeChildDevices(getChildDevices())
  log.debug "Tado (Connect) Uninstalled"
}

def initialize() {
	log.debug "Initialized with settings: ${settings}"
	// Pull the latest device info into state
	getDeviceList();
    def children = getChildDevices()
    if(settings.devices) {
    	settings.devices.each { device ->
        log.debug("Devices Inspected ${device.inspect()}")
		def item = device.tokenize('|')
        def deviceType = item[0]
        def deviceId = item[1]
        def deviceName = item[2]
        def existingDevices = children.find{ d -> d.deviceNetworkId.contains(deviceId + "|" + deviceType) }
        log.debug("existingDevices Inspected ${existingDevices.inspect()}")
    	if(!existingDevices) {
          log.debug("Some Devices were not found....creating Child Device ${deviceName}")
          try {
            if (deviceType == "HOT_WATER")
            {
              log.debug("Creating Hot Water Device ${deviceName}")
              createChildDevice("Tado Hot Water Control", deviceId + "|" + deviceType + "|" + state.accessToken + "|" + devicename, "${deviceName}", deviceName)
            }
            if (deviceType == "HEATING")
            {
              log.debug("Creating Heating Device ${deviceName}")
              createChildDevice("Tado Heating Thermostat", deviceId + "|" + deviceType + "|" + state.accessToken + "|" + devicename, "${deviceName}", deviceName)
            }
            if (deviceType == "AIR_CONDITIONING")
            {
              log.debug("Creating Air Conditioning Device ${deviceName}")
              createChildDevice("Tado Cooling Thermostat", deviceId + "|" + deviceType + "|" + state.accessToken + "|" + devicename, "${deviceName}", deviceName)
            }
 			} catch (Exception e) 
            {
					log.error "Error creating device: ${e}"
			}
    		}
		}
    }
    
    getUserList();
       if(settings.users) {
    	settings.users.each { user ->
        log.debug("Devices Inspected ${user.inspect()}")
		def item = user.tokenize('|')
        def userId = item[0]
        def userName = item[1]
        def existingUsers = children.find{ d -> d.deviceNetworkId.contains(userId + "|" + userName) }
        log.debug("existingUsers Inspected ${existingUsers.inspect()}")
    	if(!existingUsers) {
          log.debug("Some Users were not found....creating Child presence Device ${userName}")
          try 
          	{
              createChildDevice("Tado User Presence", userId + "|" + userName + "|" + state.accessToken, "${userName}", userName)
 			} catch (Exception e) 
            {
					log.error "Error creating device: ${e}"
			}
    		}
		}
    }
    
    
	// Do the initial poll
    getInititialDeviceInfo()
	
	// Schedule it to run every 5 minutes
	runEvery5Minutes("poll")
    runEvery1Minute("userPoll")
}

def getInititialDeviceInfo(){
	log.debug "getInititialDeviceInfo"
	getDeviceList();
	def children = getChildDevices()
	if(settings.devices) {
    settings.devices.each { device ->
      log.debug("Devices Inspected ${device.inspect()}")
      def item = device.tokenize('|')
      def deviceType = item[0]
      def deviceId = item[1]
      def deviceName = item[2]
      def existingDevices = children.find{ d -> d.deviceNetworkId.contains(deviceId + "|" + deviceType) }
      if(existingDevices) {
        existingDevices.getInitialDeviceinfo()
      }
	   }
  }

}
def getHubID(){
	def hubID
    if (myHub){
        hubID = myHub.id
    } else {
        def hubs = location.hubs.findAll{ it.type == physicalgraph.device.HubType.PHYSICAL } 
        if (hubs.size() == 1) hubID = hubs[0].id 
    }
    return hubID
}

def poll() {
	log.debug "In Poll"
	getDeviceList();
  def children = getChildDevices()
  if(settings.devices) {
    settings.devices.each { device ->
      log.debug("Devices Inspected ${device.inspect()}")
      def item = device.tokenize('|')
      def deviceType = item[0]
      def deviceId = item[1]
      def deviceName = item[2]
      def existingDevices = children.find{ d -> d.deviceNetworkId.contains(deviceId + "|" + deviceType) }
      if(existingDevices) {
        existingDevices.poll()
      }
	}
  }
}

def userPoll() {
	log.debug "In UserPoll"
    def children = getChildDevices();
    if(settings.users) {
    	settings.users.each { user ->
    		log.debug("Devices Inspected ${user.inspect()}")
			def item = user.tokenize('|')
        	def userId = item[0]
        	def userName = item[1]
        	def existingUsers = children.find{ d -> d.deviceNetworkId.contains(userId + "|" + userName) }
        	log.debug("existingUsers Inspected ${existingUsers.inspect()}")
    		if(existingUsers) {
          		existingUsers.poll()
        	}
     	}
   }
}
        
def createChildDevice(deviceFile, dni, name, label) {
	log.debug "In createChildDevice"
    try{
		def childDevice = addChildDevice("fuzzysb", deviceFile, dni, getHubID(), [name: name, label: label, completedSetup: true])
	} catch (e) {
		log.error "Error creating device: ${e}"
	}
}

private sendCommand(method,childDevice,args = []) {
    def methods = [
		'getid': [
        			uri: apiUrl(),
                    path: "/api/v2/me",
                    requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password]
                    ],
    'gettempunit': [
        			uri: apiUrl(),
                    path: "/api/v2/homes/${state.homeId}",
                    requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password]
                    ],
    'getzones': [
             uri: apiUrl(),
                    path: "/api/v2/homes/" + state.homeId + "/zones",
                    requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password]
                    ],
	'getMobileDevices': [
             uri: apiUrl(),
                    path: "/api/v2/homes/" + state.homeId + "/mobileDevices",
                    requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password]
                    ],
    'getcapabilities': [
        			uri: apiUrl(),
                    path: "/api/v2/homes/" + state.homeId + "/zones/" + args[0] + "/capabilities",
                    requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password]
                    ],
    'status': [
        			uri: apiUrl(),
                    path: "/api/v2/homes/" + state.homeId + "/zones/" + args[0] + "/state",
                    requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password]
                    ],
	'userStatus': [
             		uri: apiUrl(),
                    path: "/api/v2/homes/" + state.homeId + "/mobileDevices",
                    requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password]
                    ],
	'temperature': [
        			uri: apiUrl(),
        			path: "/api/v2/homes/" + state.homeId + "/zones/" + args[0] + "/overlay",
        			requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password],
                  	body: args[1]
                   	],
	'weatherStatus': [
        			uri: apiUrl(),
        			path: "/api/v2/homes/" + state.homeId + "/weather",
        			requestContentType: "application/json",
    				query: [username:settings.username, password:settings.password]
                   	],
    'deleteEntry': [
        			uri: apiUrl(),
        			path: "/api/v2/homes/" + state.homeId + "/zones/" + args[0] + "/overlay",
        			requestContentType: "application/json",
                    query: [username:settings.username, password:settings.password],
                   	]
	]

	def request = methods.getAt(method)
      log.debug "Http Params ("+request+")"
      try{
        log.debug "Executing 'sendCommand'"
          if (method == "getid"){
            httpGet(request) { resp ->
                parseMeResponse(resp)
            }
          }else if (method == "gettempunit"){
            httpGet(request) { resp ->
                parseTempResponse(resp)
            }
          }else if (method == "getzones"){
            httpGet(request) { resp ->
                parseZonesResponse(resp)
            }
          }else if (method == "getMobileDevices"){
            httpGet(request) { resp ->
                parseMobileDevicesResponse(resp)
            }
       	  }else if (method == "getcapabilities"){
            httpGet(request) { resp ->
                parseCapabilitiesResponse(resp,childDevice)
            }
          }else if (method == "status"){
            httpGet(request) { resp ->
                parseResponse(resp,childDevice)
            }
          }else if (method == "userStatus"){
            httpGet(request) { resp ->
                parseUserResponse(resp,childDevice)
            }
		  }else if (method == "temperature"){
            httpPut(request) { resp ->
                parseputResponse(resp,childDevice)
            }
          }else if (method == "weatherStatus"){
            log.debug "calling weatherStatus Method"
            httpGet(request) { resp ->
                parseweatherResponse(resp,childDevice)
            }
          }else if (method == "deleteEntry"){
            httpDelete(request) { resp ->
                parsedeleteResponse(resp,childDevice)
            }
        }else{
            httpGet(request)
        }
    } catch(Exception e){
        log.debug("___exception: " + e)
    }
}

// Parse incoming device messages to generate events
private parseMeResponse(resp) {
    log.debug("Executing parseMeResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseMeResponse.successTrue")
        state.homeId = resp.data.homes[0].id
        log.debug("Got HomeID Value: " + state.homeId)

    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseputResponse(resp,childDevice) {
	log.debug("Executing parseputResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
}

private parsedeleteResponse(resp,childDevice) {
	log.debug("Executing parsedeleteResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
}

private parseUserResponse(resp,childDevice) {
  	def item = (childDevice.device.deviceNetworkId).tokenize('|')
  	def userId = item[0]
  	def userName = item[1]
    log.debug("Executing parseUserResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
      def restUsers = resp.data
      log.debug("Executing parseUserResponse.successTrue")
      log.debug("UserId is ${userId} and userName is ${userName}")
      for (TadoUser in restUsers) {
      	log.debug("TadoUserId is ${TadoUser.id}")
      	if ((TadoUser.id).toString() == (userId).toString())
        {
         log.debug("Entering presence Assesment for User Id: ${userId}")
         if (TadoUser.settings.geoTrackingEnabled == true)
         {
         	log.debug("GeoTracking is Enabled for User Id: ${userId}")
        	if (TadoUser.location.atHome == true)
            {	
            	log.debug("Send presence Home Event Fired")
               	childDevice?.sendEvent(name:"presence",value: "present")
            } else if (TadoUser.location.atHome == false)
            {
            	log.debug("Send presence Away Event Fired")
            	childDevice?.sendEvent(name:"presence",value: "not present")
            }
        }
            
        }
      }
    } else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseResponse(resp,childDevice) {
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  if (deviceType == "AIR_CONDITIONING")
  {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    def temperatureUnit = state.tempunit
    log.debug("Temperature Unit is ${temperatureUnit}")
    def humidityUnit = "%"
    def ACMode
    def ACFanSpeed
    def ACFanMode = "off"
    def thermostatSetpoint
    def tOperatingState
    if(resp.status == 200) {
        log.debug("Executing parseResponse.successTrue")
        def temperature
        if (temperatureUnit == "C") {
        	temperature = (Math.round(resp.data.sensorDataPoints.insideTemperature.celsius *10 ) / 10)
        }
        else if(temperatureUnit == "F"){
        	temperature = (Math.round(resp.data.sensorDataPoints.insideTemperature.fahrenheit * 10) / 10)
        }
        log.debug("Read temperature: " + temperature)
        childDevice?.sendEvent(name:"temperature",value:temperature,unit:temperatureUnit)
        log.debug("Send Temperature Event Fired")
        def autoOperation = "OFF"
        if(resp.data.overlayType == null){
        	autoOperation = resp.data.tadoMode
        }
        else if(resp.data.overlayType == "NO_FREEZE"){
        	autoOperation = "OFF"
        }else if(resp.data.overlayType == "MANUAL"){
        	autoOperation = "MANUAL"
        }
        log.debug("Read tadoMode: " + autoOperation)
        childDevice?.sendEvent(name:"tadoMode",value:autoOperation)
        log.debug("Send thermostatMode Event Fired")

        def humidity
        if (resp.data.sensorDataPoints.humidity.percentage != null){
        	humidity = resp.data.sensorDataPoints.humidity.percentage
        }else{
        	humidity = "--"
        }
        log.debug("Read humidity: " + humidity)
        childDevice?.sendEvent(name:"humidity",value:humidity,unit:humidityUnit)

    	if (resp.data.setting.power == "OFF"){
            tOperatingState = "idle"
            ACMode = "off"
            ACFanMode = "off"
            log.debug("Read thermostatMode: " + ACMode)
            ACFanSpeed = "OFF"
            log.debug("Read tadoFanSpeed: " + ACFanSpeed)
            thermostatSetpoint = "--"
            log.debug("Read thermostatSetpoint: " + thermostatSetpoint)
      }
      else if (resp.data.setting.power == "ON"){
        ACMode = (resp.data.setting.mode).toLowerCase()
        log.debug("thermostatMode: " + ACMode)
        ACFanSpeed = resp.data.setting.fanSpeed
        if (ACFanSpeed == null) {
          ACFanSpeed = "--"
        }
        if (resp.data.overlay != null){
          if (resp.data.overlay.termination.type == "TIMER"){
            if (resp.data.overlay.termination.durationInSeconds == "3600"){
              ACMode = "emergency heat"
              log.debug("thermostatMode is heat, however duration shows the state is: " + ACMode)
            }
          }
        }
            switch (ACMode) {
				case "off":
        			tOperatingState = "idle"
        		break
    			case "heat":
        			tOperatingState = "heating"
        		break
    			case "emergency heat":
        			tOperatingState = "heating"
        		break
        		case "cool":
        			tOperatingState = "cooling"
        		break
                case "dry":
        			tOperatingState = "drying"
        		break
                case "fan":
        			tOperatingState = "fan only"
        		break
                case "auto":
        			tOperatingState = "heating|cooling"
        		break
			}
            log.debug("Read thermostatOperatingState: " + tOperatingState)
        	log.debug("Read tadoFanSpeed: " + ACFanSpeed)

        if (ACMode == "dry" || ACMode == "auto" || ACMode == "fan"){
        	thermostatSetpoint = "--"
        }else if(ACMode == "fan") {
        	ACFanMode = "auto"
        }else{
       		if (temperatureUnit == "C") {
        		thermostatSetpoint = Math.round(resp.data.setting.temperature.celsius)
        	}
        	else if(temperatureUnit == "F"){
        		thermostatSetpoint = Math.round(resp.data.setting.temperature.fahrenheit)
        	}
        }
        log.debug("Read thermostatSetpoint: " + thermostatSetpoint)
      }
    }else{
        log.debug("Executing parseResponse.successFalse")
    }
    childDevice?.sendEvent(name:"thermostatOperatingState",value:tOperatingState)
    log.debug("Send thermostatOperatingState Event Fired")
    childDevice?.sendEvent(name:"tadoFanSpeed",value:ACFanSpeed)
    log.debug("Send tadoFanSpeed Event Fired")
    childDevice?.sendEvent(name:"thermostatFanMode",value:ACFanMode)
    log.debug("Send thermostatFanMode Event Fired")
    childDevice?.sendEvent(name:"thermostatMode",value:ACMode)
    log.debug("Send thermostatMode Event Fired")
    childDevice?.sendEvent(name:"thermostatSetpoint",value:thermostatSetpoint,unit:temperatureUnit)
    log.debug("Send thermostatSetpoint Event Fired")
    childDevice?.sendEvent(name:"heatingSetpoint",value:thermostatSetpoint,unit:temperatureUnit)
    log.debug("Send heatingSetpoint Event Fired")
    childDevice?.sendEvent(name:"coolingSetpoint",value:thermostatSetpoint,unit:temperatureUnit)
    log.debug("Send coolingSetpoint Event Fired")
  }
  if (deviceType == "HEATING")
  {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    def temperatureUnit = state.tempunit
    log.debug("Temperature Unit is ${temperatureUnit}")
    def humidityUnit = "%"
    def ACMode
    def ACFanSpeed
    def thermostatSetpoint
    def tOperatingState
    if(resp.status == 200) {
        log.debug("Executing parseResponse.successTrue")
        def temperature
        if (temperatureUnit == "C") {
        	temperature = (Math.round(resp.data.sensorDataPoints.insideTemperature.celsius * 10 ) / 10)
        }
        else if(temperatureUnit == "F"){
        	temperature = (Math.round(resp.data.sensorDataPoints.insideTemperature.fahrenheit * 10) / 10)
        }
        log.debug("Read temperature: " + temperature)
        childDevice?.sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
        log.debug("Send Temperature Event Fired")
        def autoOperation = "OFF"
        if(resp.data.overlayType == null){
        	autoOperation = resp.data.tadoMode
        }
        else if(resp.data.overlayType == "NO_FREEZE"){
        	autoOperation = "OFF"
        }else if(resp.data.overlayType == "MANUAL"){
        	autoOperation = "MANUAL"
        }
        log.debug("Read tadoMode: " + autoOperation)
        childDevice?.sendEvent(name: 'tadoMode', value: autoOperation)

		if (resp.data.setting.power == "ON"){
			childDevice?.sendEvent(name: 'thermostatMode', value: "heat")
			childDevice?.sendEvent(name: 'thermostatOperatingState', value: "heating")
			log.debug("Send thermostatMode Event Fired")
			if (temperatureUnit == "C") {
				thermostatSetpoint = resp.data.setting.temperature.celsius
			}
			else if(temperatureUnit == "F"){
				thermostatSetpoint = resp.data.setting.temperature.fahrenheit
			}
			log.debug("Read thermostatSetpoint: " + thermostatSetpoint)
		} else if(resp.data.setting.power == "OFF"){
			thermostatSetpoint = "--"
			childDevice?.sendEvent(name: 'thermostatMode', value: "off")
			childDevice?.sendEvent(name: 'thermostatOperatingState', value: "idle")
			log.debug("Send thermostatMode Event Fired")
		}

        def humidity
        if (resp.data.sensorDataPoints.humidity.percentage != null){
        	humidity = resp.data.sensorDataPoints.humidity.percentage
        }else{
        	humidity = "--"
        }
        log.debug("Read humidity: " + humidity)

        childDevice?.sendEvent(name: 'humidity', value: humidity,unit: humidityUnit)

	}

	else{
        log.debug("Executing parseResponse.successFalse")
    }

    childDevice?.sendEvent(name: 'thermostatSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
    log.debug("Send thermostatSetpoint Event Fired")
    childDevice?.sendEvent(name: 'heatingSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
    log.debug("Send heatingSetpoint Event Fired")
  }
  if (deviceType == "HOT_WATER")
  {
    log.debug("Executing parseResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    def temperatureUnit = state.tempunit
    log.debug("Temperature Unit is ${temperatureUnit}")
    def humidityUnit = "%"
    def ACMode
    def ACFanSpeed
    def thermostatSetpoint
    def tOperatingState
    if(resp.status == 200) {
    log.debug("Executing parseResponse.successTrue")
    def temperature
    if (state.supportsWaterTempControl == "true" && resp.data.tadoMode != null && resp.data.setting.power != "OFF"){
    if (temperatureUnit == "C") {
      temperature = (Math.round(resp.data.setting.temperature.celsius * 10 ) / 10)
    }
    else if(temperatureUnit == "F"){
      temperature = (Math.round(resp.data.setting.temperature.fahrenheit * 10) / 10)
    }
    log.debug("Read temperature: " + temperature)
    childDevice?.sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
    log.debug("Send Temperature Event Fired")
    } else {
      childDevice?.sendEvent(name: 'temperature', value: "--", unit: temperatureUnit)
      log.debug("Send Temperature Event Fired")
    }
    def autoOperation = "OFF"
    if(resp.data.overlayType == null){
      autoOperation = resp.data.tadoMode
    }
    else if(resp.data.overlayType == "NO_FREEZE"){
      autoOperation = "OFF"
    }else if(resp.data.overlayType == "MANUAL"){
      autoOperation = "MANUAL"
    }
    log.debug("Read tadoMode: " + autoOperation)
    childDevice?.sendEvent(name: 'tadoMode', value: autoOperation)

    if (resp.data.setting.power == "ON"){
      childDevice?.sendEvent(name: 'thermostatMode', value: "heat")
      childDevice?.sendEvent(name: 'thermostatOperatingState', value: "heating")
      log.debug("Send thermostatMode Event Fired")
      } else if(resp.data.setting.power == "OFF"){
        childDevice?.sendEvent(name: 'thermostatMode', value: "off")
        childDevice?.sendEvent(name: 'thermostatOperatingState', value: "idle")
        log.debug("Send thermostatMode Event Fired")
      }
      log.debug("Send thermostatMode Event Fired")
      if (state.supportsWaterTempControl == "true" && resp.data.tadoMode != null && resp.data.setting.power != "OFF"){
        if (temperatureUnit == "C") {
          thermostatSetpoint = resp.data.setting.temperature.celsius
        }
        else if(temperatureUnit == "F"){
          thermostatSetpoint = resp.data.setting.temperature.fahrenheit
        }
        log.debug("Read thermostatSetpoint: " + thermostatSetpoint)
        } else {
          thermostatSetpoint = "--"
        }
      }

      else{
        log.debug("Executing parseResponse.successFalse")
      }

      childDevice?.sendEvent(name: 'thermostatSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
      log.debug("Send thermostatSetpoint Event Fired")
      childDevice?.sendEvent(name: 'heatingSetpoint', value: thermostatSetpoint, unit: temperatureUnit)
      log.debug("Send heatingSetpoint Event Fired")
  }
}

private parseTempResponse(resp) {
    log.debug("Executing parseTempResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseTempResponse.successTrue")
        def tempunitname = resp.data.temperatureUnit
        if (tempunitname == "CELSIUS") {
        	log.debug("Setting Temp Unit to C")
        	state.tempunit = "C"
        }
        else if(tempunitname == "FAHRENHEIT"){
        	log.debug("Setting Temp Unit to F")
        	state.tempunit = "F"
        }
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseZonesResponse(resp) {
    log.debug("Executing parseZonesResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
      def restDevices = resp.data
      def TadoDevices = []
      log.debug("Executing parseZoneResponse.successTrue")
      restDevices.each { Tado -> TadoDevices << ["${Tado.type}|${Tado.id}|${Tado.name}":"${Tado.name}"] }
      log.debug(TadoDevices)
      return TadoDevices
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseMobileDevicesResponse(resp) {
    log.debug("Executing parseMobileDevicesResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
      def restUsers = resp.data
      def TadoUsers = []
      log.debug("Executing parseMobileDevicesResponse.successTrue")
      restUsers.each { TadoUser -> 
      	if (TadoUser.settings.geoTrackingEnabled == true)
        {
        	TadoUsers << ["${TadoUser.id}|${TadoUser.name}":"${TadoUser.name}"]
        }
      }
      log.debug(TadoUsers)
      return TadoUsers
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

private parseCapabilitiesResponse(resp,childDevice) {
    log.debug("Executing parseCapabilitiesResponse: "+resp.data)
    log.debug("Output status: " + resp.status)
    if(resp.status == 200) {
    	try
      {
    	log.debug("Executing parseResponse.successTrue")
       	childDevice?.setCapabilitytadoType(resp.data.type)
        log.debug("Tado Type is ${resp.data.type}")
        if(resp.data.type == "AIR_CONDITIONING")
        {
          try
          {
            if(resp.data.AUTO || (resp.data.AUTO).toString() == "[:]"){
              log.debug("settingautocapability state true")
              childDevice?.setCapabilitySupportsAuto("true")
            } else {
              log.debug("settingautocapability state false")
              childDevice?.setCapabilitySupportsAuto("false")
            }
            if(resp.data.AUTO.swings || (resp.data.AUTO.swings).toString() == "[:]")
            {
              log.debug("settingautoswingcapability state true")
            childDevice?.setCapabilitySupportsAutoSwing("true")
            }
            else
            {
              log.debug("settingautoswingcapability state false")
              childDevice?.setCapabilitySupportsAutoSwing("false")
            }
          }
          catch(Exception e)
          {
            log.debug("___exception parsing Auto Capabiity: " + e)
          }
          try
          {
              if(resp.data.COOL || (resp.data.COOL).toString() == "[:]"){
              log.debug("setting COOL capability state true")
              childDevice?.setCapabilitySupportsCool("true")
                def coolfanmodelist = resp.data.COOL.fanSpeeds
                if(resp.data.COOL.swings || (resp.data.COOL.swings).toString() == "[:]")
                {
                  log.debug("settingcoolswingcapability state true")
                  childDevice?.setCapabilitySupportsCoolSwing("true")
                }
                else
                {
                  log.debug("settingcoolswingcapability state false")
                  childDevice?.setCapabilitySupportsCoolSwing("false")
                }
                if(resp.data.COOL.fanSpeeds || (resp.data.COOL.fanSpeeds).toString() == "[:]")
                {
                  childDevice?.setCapabilitySupportsCoolFanSpeed("true")
                }
                else
                {
                  childDevice?.setCapabilitySupportsCoolFanSpeed("false")
                }
                if(coolfanmodelist.find { it == 'AUTO' }){
                  log.debug("setting COOL Auto Fan Speed capability state true")
                  childDevice?.setCapabilitySupportsCoolAutoFanSpeed("true")
                } else {
                  log.debug("setting COOL Auto Fan Speed capability state false")
                  childDevice?.setCapabilitySupportsCoolAutoFanSpeed("false")
                }
                if (state.tempunit == "C"){
                  childDevice?.setCapabilityMaxCoolTemp(resp.data.COOL.temperatures.celsius.max)
                  childDevice?.setCapabilityMinCoolTemp(resp.data.COOL.temperatures.celsius.min)
                } else if (state.tempunit == "F") {
                  childDevice?.setCapabilityMaxCoolTemp(resp.data.COOL.temperatures.fahrenheit.max)
                  childDevice?.setCapabilityMinCoolTemp(resp.data.COOL.temperatures.fahrenheit.min)
                }
            } else {
              log.debug("setting COOL capability state false")
              childDevice?.setCapabilitySupportsCool("false")
            }
          }
          catch(Exception e)
          {
            log.debug("___exception parsing Cool Capabiity: " + e)
          }
          try
          {
            if(resp.data.DRY || (resp.data.DRY).toString() == "[:]"){
              log.debug("setting DRY capability state true")
              childDevice?.setCapabilitySupportsDry("true")
            } else {
              log.debug("setting DRY capability state false")
              childDevice?.setCapabilitySupportsDry("false")
            }
            if(resp.data.DRY.swings || (resp.data.DRY.swings).toString() == "[:]")
            {
              log.debug("settingdryswingcapability state true")
            childDevice?.setCapabilitySupportsDrySwing("true")
            }
            else
            {
              log.debug("settingdryswingcapability state false")
              childDevice?.setCapabilitySupportsDrySwing("false")
            }
          }
          catch(Exception e)
          {
            log.debug("___exception parsing Dry Capabiity: " + e)
          }
          try
          {
            if(resp.data.FAN || (resp.data.FAN).toString() == "[:]"){
              log.debug("setting FAN capability state true")
              childDevice?.setCapabilitySupportsFan("true")
            } else {
              log.debug("setting FAN capability state false")
              childDevice?.setCapabilitySupportsFan("false")
            }
            if(resp.data.FAN.swings || (resp.data.FAN.swings).toString() == "[:]")
            {
              log.debug("settingfanswingcapability state true")
            childDevice?.setCapabilitySupportsFanSwing("true")
            }
            else
            {
              log.debug("settingfanswingcapability state false")
              childDevice?.setCapabilitySupportsFanSwing("false")
            }
          }
          catch(Exception e)
          {
            log.debug("___exception parsing Fan Capabiity: " + e)
          }
          try
          {
            if(resp.data.HEAT || (resp.data.HEAT).toString() == "[:]"){
              log.debug("setting HEAT capability state true")
              childDevice?.setCapabilitySupportsHeat("true")
                def heatfanmodelist = resp.data.HEAT.fanSpeeds
                if(resp.data.HEAT.swings || (resp.data.HEAT.swings).toString() == "[:]")
                {
                  log.debug("settingheatswingcapability state true")
                  childDevice?.setCapabilitySupportsHeatSwing("true")
                }
                else
                {
                  log.debug("settingheatswingcapability state false")
                  childDevice?.setCapabilitySupportsHeatSwing("false")
                }
                if(resp.data.HEAT.fanSpeeds || (resp.data.HEAT.fanSpeeds).toString() == "[:]")
                {
                  childDevice?.setCapabilitySupportsHeatFanSpeed("true")
                }
                else
                {
                  childDevice?.setCapabilitySupportsHeatFanSpeed("false")
                }
                if(heatfanmodelist.find { it == 'AUTO' }){
                  log.debug("setting HEAT Auto Fan Speed capability state true")
                  childDevice?.setCapabilitySupportsHeatAutoFanSpeed("true")
                } else {
                  log.debug("setting HEAT Auto Fan Speed capability state false")
                  childDevice?.setCapabilitySupportsHeatAutoFanSpeed("false")
                }
                if (state.tempunit == "C"){
                  childDevice?.setCapabilityMaxHeatTemp(resp.data.HEAT.temperatures.celsius.max)
                  childDevice?.setCapabilityMinHeatTemp(resp.data.HEAT.temperatures.celsius.min)
                } else if (state.tempunit == "F") {
                  childDevice?.setCapabilityMaxHeatTemp(resp.data.HEAT.temperatures.fahrenheit.max)
                  childDevice?.setCapabilityMinHeatTemp(resp.data.HEAT.temperatures.fahrenheit.min)
                }
            } else {
              log.debug("setting HEAT capability state false")
              childDevice?.setCapabilitySupportsHeat("false")
            }
          }catch(Exception e)
          {
            log.debug("___exception parsing Heat Capabiity: " + e)
          }
        }
        if(resp.data.type == "HEATING")
        {
          if(resp.data.type == "HEATING")
          {
          	log.debug("setting HEAT capability state true")
          	childDevice?.setCapabilitySupportsHeat("true")
            if (state.tempunit == "C")
            {
              childDevice?.setCapabilityMaxHeatTemp(resp.data.temperatures.celsius.max)
              childDevice?.setCapabilityMinHeatTemp(resp.data.temperatures.celsius.min)
            }
            else if (state.tempunit == "F")
            {
              childDevice?.setCapabilityMaxHeatTemp(resp.data.temperatures.fahrenheit.max)
              childDevice?.setCapabilityMinHeatTemp(resp.data.temperatures.fahrenheit.min)
            }
          }
          else
          {
          	log.debug("setting HEAT capability state false")
          	childDevice?.setCapabilitySupportsHeat("false")
          }
        }
        if(resp.data.type == "HOT_WATER")
        {
            if(resp.data.type == "HOT_WATER"){
              log.debug("setting WATER capability state true")
              dchildDevice?.setCapabilitySupportsWater("true")
              if (resp.data.canSetTemperature == true){
                childDevice?.setCapabilitySupportsWaterTempControl("true")
                if (state.tempunit == "C")
                {
                  childDevice?.setCapabilityMaxHeatTemp(resp.data.temperatures.celsius.max)
                  childDevice?.setCapabilityMinHeatTemp(resp.data.temperatures.celsius.min)
                }
                else if (state.tempunit == "F")
                {
                  childDevice?.setCapabilityMaxHeatTemp(resp.data.temperatures.fahrenheit.max)
                  childDevice?.setCapabilityMinHeatTemp(resp.data.temperatures.fahrenheit.min)
                }
              }
              else
              {
                childDevice?.setCapabilitySupportsWaterTempControl("false")
              }
            }
            else
            {
            log.debug("setting Water capability state false")
            childDevice?.setCapabilitySupportsWater("false")
            }
        }
      }
      catch(Exception e)
      {
        log.debug("___exception: " + e)
      }
    }
    else if(resp.status == 201)
    {
      log.debug("Something was created/updated")
    }
}

private parseweatherResponse(resp,childDevice) {
    log.debug("Executing parseweatherResponse: "+resp.data)
    log.debug("Output status: "+resp.status)
	def temperatureUnit = state.tempunit
    log.debug("Temperature Unit is ${temperatureUnit}")
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        def outsidetemperature
        if (temperatureUnit == "C") {
        	outsidetemperature = resp.data.outsideTemperature.celsius
        }
        else if(temperatureUnit == "F"){
        	outsidetemperature = resp.data.outsideTemperature.fahrenheit
        }
        log.debug("Read outside temperature: " + outsidetemperature)
        childDevice?.sendEvent(name: 'outsidetemperature', value: outsidetemperature, unit: temperatureUnit)
        log.debug("Send Outside Temperature Event Fired")
        return result

    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}

def getidCommand(){
	log.debug "Executing 'sendCommand.getidCommand'"
	sendCommand("getid",null,[])
}

def getTempUnitCommand(){
	log.debug "Executing 'sendCommand.getidCommand'"
	sendCommand("gettempunit",null,[])
}

def getZonesCommand(){
	log.debug "Executing 'sendCommand.getzones'"
	sendCommand("getzones",null,[])
}

def getMobileDevicesCommand(){
	log.debug "Executing 'sendCommand.getMobileDevices'"
	sendCommand("getMobileDevices",null,[])
}

def weatherStatusCommand(childDevice){
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
	log.debug "Executing 'sendCommand.weatherStatusCommand'"
	def result = sendCommand("weatherStatus",childDevice,[deviceId])
}

def getCapabilitiesCommand(childDevice, deviceDNI){
	log.debug("childDevice is: " + childDevice.inspect())
	log.debug("deviceDNI is: " + deviceDNI.inspect())
	def item = deviceDNI.tokenize('|')
	def deviceId = item[0]
	def deviceType = item[1]
	def deviceToken = item[2]
	log.debug "Executing 'sendCommand.getcapabilities'"
	sendCommand("getcapabilities",childDevice,[deviceId])
}

private removeChildDevices(delete) {
	try {
    	delete.each {
        	deleteChildDevice(it.deviceNetworkId)
            log.info "Successfully Removed Child Device: ${it.displayName} (${it.deviceNetworkId})"
    		}
   		}
    catch (e) { log.error "There was an error (${e}) when trying to delete the child device" }
}

def parseCapabilityData(Map results){
  log.debug "in parseCapabilityData"
  def result
  results.each { name, value ->
    
    if (name == "value")
    {
    log.debug "Map Name Returned, ${name} and Value is ${value}"
    result = value.toString()
    log.debug "Result is ${result}"
    //return result
    }
  }
  return result
}

//Device Commands Below Here
def autoCommand(childDevice){
  log.debug "Executing 'sendCommand.autoCommand' on device ${childDevice.device.name}"
  def terminationmode = settings.manualmode
  def traperror
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  if (deviceType == "AIR_CONDITIONING")
  {
    def capabilitySupportsAuto = parseCapabilityData(childDevice.getCapabilitySupportsAuto())
    def capabilitySupportsAutoSwing = parseCapabilityData(childDevice.getCapabilitySupportsAutoSwing())
    def capabilitysupported = capabilitySupportsAuto
    if (capabilitysupported == "true"){
    log.debug "Executing 'sendCommand.autoCommand' on device ${childDevice.device.name}"
    def jsonbody
   	if (capabilitySupportsAutoSwing == "true")
    {
        jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"AUTO", power:"ON", swing:"OFF", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    } 
    else
    {
      	jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"AUTO", power:"ON", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    }
    sendCommand("temperature",dchildDevice,[deviceId,jsonbody])
    statusCommand(device)
    } else {
      log.debug("Sorry Auto Capability not supported on device ${childDevice.device.name}")
    }
  }
  if(deviceType == "HEATING")
  {
    def initialsetpointtemp
    try {
      traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
    }
    catch (NumberFormatException e){
        traperror = 0
    }
    if(traperror == 0){
      initialsetpointtemp = settings.defHeatingTemp
    } else {
    	initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
    }
  	def jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HEATING"], termination:[type:terminationmode]])
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
    statusCommand(childDevice)
  }
  if (deviceType == "HOT_WATER")
  {
    log.debug "Executing 'sendCommand.autoCommand'"
    def initialsetpointtemp
    def jsonbody
    def capabilitySupportsWaterTempControl = parseCapabilityData(childDevice.getCapabilitySupportsWaterTempControl())
    if(capabilitySupportsWaterTempControl == "true"){
      try {
        traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
      }catch (NumberFormatException e){
        traperror = 0
      }
      if(traperror == 0){
        initialsetpointtemp = settings.defHeatingTemp
      } else {
        initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
      }
      jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HOT_WATER"], termination:[type:terminationmode]])
    } else {
      jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", type:"HOT_WATER"], termination:[type:terminationmode]])
    }
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
    statusCommand(childDevice)
  }
}

def dryCommand(childDevice){
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def capabilitySupportsDry = parseCapabilityData(childDevice.getCapabilitySupportsDry())
  def capabilitySupportsDrySwing = parseCapabilityData(childDevice.getCapabilitySupportsDrySwing())
  def capabilitysupported = capabilitySupportsDry
  if (capabilitysupported == "true"){
  	def terminationmode = settings.manualmode
  	log.debug "Executing 'sendCommand.dryCommand' on device ${childDevice.device.name}"
  	def jsonbody
      	if (capabilitySupportsDrySwing == "true")
        {
			jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"DRY", power:"ON", swing:"OFF", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        } 
        else
        {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"DRY", power:"ON", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
  	sendCommand("temperature",childDevice,[deviceId,jsonbody])
  	statusCommand(childDevice)
  } else {
    log.debug("Sorry Dry Capability not supported on device ${childDevice.device.name}")
  }
}

def fanAuto(childDevice){
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def capabilitySupportsFan = parseCapabilityData(childDevice.getCapabilitySupportsFan())
  def capabilitySupportsFanSwing = parseCapabilityData(childDevice.getCapabilitySupportsFanSwing())
  def capabilitysupported = capabilitySupportsFan
  if (capabilitysupported == "true"){
    def terminationmode = settings.manualmode
		log.debug "Executing 'sendCommand.fanAutoCommand' on device ${childDevice.device.name}"
      def jsonbody
      	if (capabilitySupportsFanSwing == "true")
        {
			jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"FAN", power:"ON", swing:"OFF", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        } 
        else
        {
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"FAN", power:"ON", type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
    
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
  	statusCommand(childDevice)
  } else {
    log.debug("Sorry Fan Capability not supported by your HVAC Device")
  }
}

def endManualControl(childDevice){
	log.debug "Executing 'sendCommand.endManualControl' on device ${childDevice.device.name}"
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
	sendCommand("deleteEntry",childDevice,[deviceId])
	statusCommand(childDevice)
}

def cmdFanSpeedAuto(childDevice){
  def supportedfanspeed
  def terminationmode = settings.manualmode
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def jsonbody
  def capabilitySupportsCool = parseCapabilityData(childDevice.getCapabilitySupportsCool())
  def capabilitysupported = capabilitySupportsCool
  def capabilitySupportsCoolAutoFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsCoolAutoFanSpeed())
  def fancapabilitysupported = capabilitySupportsCoolAutoFanSpeed
  if (fancapabilitysupported == "true"){
    supportedfanspeed = "AUTO"
    } else {
      supportedfanspeed = "HIGH"
    }
	def curSetTemp = (childDevice.device.currentValue("thermostatSetpoint"))
	def curMode = ((childDevice.device.currentValue("thermostatMode")).toUpperCase())
	if (curMode == "COOL" || curMode == "HEAT"){
    	if (capabilitySupportsCoolSwing == "true" || capabilitySupportsHeatSwing == "true")
        {
    		if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        } 
        else
        {
        	if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        }
		log.debug "Executing 'sendCommand.fanSpeedAuto' to ${supportedfanspeed}"
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
    statusCommand(childDevice)
	}
}

def cmdFanSpeedHigh(childDevice){
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def jsonbody
  def supportedfanspeed = "HIGH"
  def terminationmode = settings.manualmode
	def curSetTemp = (childDevice.device.currentValue("thermostatSetpoint"))
	def curMode = ((childDevice.device.currentValue("thermostatMode")).toUpperCase())
	if (curMode == "COOL" || curMode == "HEAT"){
    	if (capabilitySupportsCoolSwing == "true" || capabilitySupportsHeatSwing == "true")
        {
    		if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        } 
        else
        {
        	if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        }
		log.debug "Executing 'sendCommand.fanSpeedAuto' to ${supportedfanspeed}"
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
    statusCommand(childDevice)
	}
}

def cmdFanSpeedMid(childDevice){
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def supportedfanspeed = "MIDDLE"
  def terminationmode = settings.manualmode
  def jsonbody
	def curSetTemp = (childDevice.device.currentValue("thermostatSetpoint"))
	def curMode = ((childDevice.device.currentValue("thermostatMode")).toUpperCase())
	if (curMode == "COOL" || curMode == "HEAT"){
    	if (capabilitySupportsCoolSwing == "true" || capabilitySupportsHeatSwing == "true")
        {
    		if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        } 
        else
        {
        	if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        }
		log.debug "Executing 'sendCommand.fanSpeedMid' to ${supportedfanspeed}"
		sendCommand("temperature",childDevice,[deviceId,jsonbody])
    statusCommand(childDevice)
	}
}

def cmdFanSpeedLow(childDevice){
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def capabilitySupportsCoolSwing = parseCapabilityData(childDevice.getCapabilitySupportsCoolSwing())
  def capabilitySupportsHeatSwing = parseCapabilityData(childDevice.getCapabilitySupportsHeatSwing())
  def supportedfanspeed = "LOW"
  def terminationmode = settings.manualmode
  def jsonbody
	def curSetTemp = (childDevice.device.currentValue("thermostatSetpoint"))
	def curMode = ((childDevice.device.currentValue("thermostatMode")).toUpperCase())
	if (curMode == "COOL" || curMode == "HEAT"){
    	if (capabilitySupportsCoolSwing == "true" || capabilitySupportsHeatSwing == "true")
        {
    		if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", swing:"OFF", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        } 
        else
        {
        	if (state.tempunit == "C") {
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[celsius:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
    		else if(state.tempunit == "F"){
      			jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:curMode, power:"ON", temperature:[fahrenheit:curSetTemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    		}
        }
		log.debug "Executing 'sendCommand.fanSpeedLow' to ${supportedfanspeed}"
		sendCommand("temperature",childDevice,[deviceId,jsonbody])
    statusCommand(childDevice)
	}
}

def setCoolingTempCommand(childDevice,targetTemperature){
  def terminationmode = settings.manualmode
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def supportedfanspeed
  def capabilitySupportsCool = parseCapabilityData(childDevice.getCapabilitySupportsCool())
  def capabilitySupportsCoolSwing = parseCapabilityData(childDevice.getCapabilitySupportsCoolSwing())
  def capabilitysupported = capabilitySupportsCool
  def capabilitySupportsCoolFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsCoolFanSpeed())
  def capabilitySupportsCoolAutoFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsCoolAutoFanSpeed())
  def fancapabilitysupported = capabilitySupportsCoolAutoFanSpeed
  def jsonbody
    if (fancapabilitysupported == "true"){
    	supportedfanspeed = "AUTO"
    } else {
        supportedfanspeed = "HIGH"
    }
    if (capabilitySupportsCoolSwing == "true" && capabilitySupportsCoolFanSpeed == "true")
    {
 		if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", swing:"OFF", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", swing:"OFF", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else if(capabilitySupportsCoolSwing == "true" && capabilitySupportsCoolFanSpeed == "false"){
      if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", swing:"OFF", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", swing:"OFF", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else if(capabilitySupportsCoolSwing == "false" && capabilitySupportsCoolFanSpeed == "false"){
      if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else
    {
 		if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }

	log.debug "Executing 'sendCommand.setCoolingTempCommand' to ${targetTemperature} on device ${childDevice.device.name}"
	sendCommand("temperature",childDevice,[deviceId,jsonbody])
}

def setHeatingTempCommand(childDevice,targetTemperature){
  def terminationmode = settings.manualmode
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  if(deviceType == "AIR_CONDITIONING")
  {
    def capabilitySupportsHeat = parseCapabilityData(childDevice.getCapabilitySupportsHeat())
    def capabilitysupported = capabilitySupportsHeat
    def capabilitySupportsHeatSwing = parseCapabilityData(childDevice.getCapabilitySupportsHeatSwing())
    def capabilitySupportsHeatAutoFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsHeatAutoFanSpeed())
    def capabilitySupportsHeatFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsHeatFanSpeed())
    def fancapabilitysupported = capabilitySupportsHeatAutoFanSpeed
    def supportedfanspeed
    def jsonbody
    if (fancapabilitysupported == "true")
    {
      supportedfanspeed = "AUTO"
    }
    else
    {
      supportedfanspeed = "HIGH"
    }
    if (capabilitySupportsHeatSwing == "true" && capabilitySupportsHeatFanSpeed == "true")
    {
 		if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", swing:"OFF", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", swing:"OFF", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else if(capabilitySupportsHeatSwing == "true" && capabilitySupportsHeatFanSpeed == "false"){
      if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", swing:"OFF", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", swing:"OFF", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else if(capabilitySupportsHeatSwing == "false" && capabilitySupportsHeatFanSpeed == "false"){
      if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else
    {
 		if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[celsius:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[fahrenheit:targetTemperature], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
  	log.debug "Executing 'sendCommand.setHeatingTempCommand' to ${targetTemperature} on device ${childDevice.device.name}"
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
  }
  if(deviceType == "HEATING")
  {
    def jsonbody
    if (state.tempunit == "C") {
      jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:targetTemperature], type:"HEATING"], termination:[type:terminationmode]])
    }
    else if(state.tempunit == "F"){
      jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[fahrenheit:targetTemperature], type:"HEATING"], termination:[type:terminationmode]])
    }
    log.debug "Executing 'sendCommand.setHeatingTempCommand' to ${targetTemperature} on device ${childDevice.device.name}"
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
  }
  if(deviceType == "HOT_WATER")
  {
    def jsonbody
    def capabilitySupportsWaterTempControl = parseCapabilityData(childDevice.getCapabilitySupportsWaterTempControl())
    if(capabilitySupportsWaterTempControl == "true"){
      if (state.tempunit == "C") {
        jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:targetTemperature], type:"HOT_WATER"], termination:[type:terminationmode]])
			}
			else if(state.tempunit == "F"){
				jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[fahrenheit:targetTemperature], type:"HOT_WATER"], termination:[type:terminationmode]])
			}
		log.debug "Executing 'sendCommand.setHeatingTempCommand' to ${targetTemperature} on device ${childDevice.device.name}"
		sendCommand("temperature",[jsonbody])
	  } else {
		    log.debug "Hot Water Temperature Capability Not Supported on device ${childDevice.device.name}"
	  }
  }
}

def offCommand(childDevice){
	log.debug "Executing 'sendCommand.offCommand' on device ${childDevice.device.name}"
  def terminationmode = settings.manualmode
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  def jsonbody = new groovy.json.JsonOutput().toJson([setting:[type:deviceType, power:"OFF"], termination:[type:terminationmode]])
  sendCommand("temperature",childDevice,[deviceId,jsonbody])
}

def onCommand(childDevice){
  log.debug "Executing 'sendCommand.onCommand'"
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  if(deviceType == "AIR_CONDITIONING")
  {
    coolCommand(childDevice)
  }
  if(deviceType == "HEATING" || deviceType == "HOT_WATER")
  {
    heatCommand(childDevice)
  }
}

def coolCommand(childDevice){
	log.debug "Executing 'sendCommand.coolCommand'"
    def terminationmode = settings.manualmode
    def item = (childDevice.device.deviceNetworkId).tokenize('|')
    def deviceId = item[0]
    def deviceType = item[1]
    def deviceToken = item[2]
    def initialsetpointtemp
    def supportedfanspeed
    def capabilitySupportsCool = parseCapabilityData(childDevice.getCapabilitySupportsCool())
    def capabilitySupportsCoolSwing = parseCapabilityData(childDevice.getCapabilitySupportsCoolSwing())
    def capabilitysupported = capabilitySupportsCool
    def capabilitySupportsCoolAutoFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsCoolAutoFanSpeed())
    def capabilitySupportsCoolFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsCoolFanSpeed())
    def fancapabilitysupported = capabilitySupportsCoolAutoFanSpeed
    def traperror
    try {
        traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
    }catch (NumberFormatException e){
         traperror = 0
    }
    if (fancapabilitysupported == "true"){
    	supportedfanspeed = "AUTO"
        } else {
        supportedfanspeed = "HIGH"
        }
    if(traperror == 0){
    	initialsetpointtemp = settings.defCoolingTemp
    } else {
    	initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
    }
    def jsonbody
    if (capabilitySupportsCoolSwing == "true" && capabilitySupportsCoolFanSpeed == "true")
    {
    	if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", swing:"OFF", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    	}
    	else if (state.tempunit == "F"){
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", swing:"OFF", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    	}    
    }
    else if(capabilitySupportsCoolSwing == "true" && capabilitySupportsCoolFanSpeed == "false"){
      if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", swing:"OFF", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", swing:"OFF", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else if(capabilitySupportsCoolSwing == "false" && capabilitySupportsCoolFanSpeed == "false"){
      if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}
  		else if(state.tempunit == "F"){
            jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"COOL", power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
  		}    
    }
    else
    {
    	if (state.tempunit == "C") {
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    	}
    	else if (state.tempunit == "F"){
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"COOL", power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
    	}
    }
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
}

def heatCommand(childDevice){
  log.debug "Executing 'sendCommand.heatCommand' on device ${childDevice.device.name}"
  def terminationmode = settings.manualmode
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  if(deviceType == "AIR_CONDITIONING")
    {
      def initialsetpointtemp
      def supportedfanspeed
      def traperror
      def capabilitySupportsHeat = parseCapabilityData(childDevice.getCapabilitySupportsHeat())
      def capabilitySupportsHeatSwing = parseCapabilityData(childDevice.getCapabilitySupportsHeatSwing())
      def capabilitysupported = capabilitySupportsHeat
      def capabilitySupportsHeatAutoFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsHeatAutoFanSpeed())
      def capabilitySupportsHeatFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsHeatFanSpeed())
      def fancapabilitysupported = capabilitySupportsHeatAutoFanSpeed
      try
      {
        traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
      }
      catch (NumberFormatException e)
      {
        traperror = 0
      }
      if (fancapabilitysupported == "true")
      {
        supportedfanspeed = "AUTO"
      }
      else
      {
        supportedfanspeed = "HIGH"
      }
      if(traperror == 0)
      {
        initialsetpointtemp = settings.defHeatingTemp
      }
      else
      {
        initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
      }
      def jsonbody
      if (capabilitySupportsHeatSwing == "true" && capabilitySupportsHeatFanSpeed == "true")
      {
      	if (state.tempunit == "C") {
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", swing:"OFF", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
      	}
      	else if (state.tempunit == "F"){
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", swing:"OFF", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
      	}
      }
      else if(capabilitySupportsHeatSwing == "true" && capabilitySupportsHeatFanSpeed == "false"){
        if (state.tempunit == "C") {
          jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", swing:"OFF", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
              jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", swing:"OFF", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }    
      }
      else if(capabilitySupportsHeatSwing == "false" && capabilitySupportsHeatFanSpeed == "false"){
        if (state.tempunit == "C") {
          jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
              jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }    
      } 
      else
      {
      	if (state.tempunit == "C") {
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
      	}
      	else if (state.tempunit == "F"){
    		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
      	}      
      }
      
      sendCommand("temperature",childDevice,[deviceId,jsonbody])
    }
    if(deviceType == "HEATING")
    {
      def initialsetpointtemp
      def traperror
        try
        {
          traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
        }
        catch (NumberFormatException e)
        {
          traperror = 0
        }
        if(traperror == 0)
        {
          initialsetpointtemp = settings.defHeatingTemp
        }
        else
        {
          initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
        }
        def jsonbody
        if (state.tempunit == "C") {
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HEATING"], termination:[type:terminationmode]])
      	}
        else if (state.tempunit == "F"){
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"HEATING"], termination:[type:terminationmode]])
      	}
        sendCommand("temperature",childDevice,[deviceId,jsonbody])
    }
    if(deviceType == "HOT_WATER")
    {
      def jsonbody
      def initialsetpointtemp
      def traperror
      def capabilitySupportsWaterTempControl = parseCapabilityData(childDevice.getCapabilitySupportsWaterTempControl())
      if(capabilitySupportsWaterTempControl == "true"){
        try {
          traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
        }catch (NumberFormatException e){
          traperror = 0
        }
        if(traperror == 0){
          initialsetpointtemp = settings.defHeatingTemp
        } else {
          initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
        }
        if (state.tempunit == "C") {
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HOT_WATER"], termination:[type:terminationmode]])
      	}
        else if (state.tempunit == "F"){
        	jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"HOT_WATER"], termination:[type:terminationmode]])
      	}
      } else {
        jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", type:"HOT_WATER"], termination:[type:terminationmode]])
      }
      sendCommand("temperature",childDevice,[deviceId,jsonbody])
    }
}

def emergencyHeat(childDevice){
  log.debug "Executing 'sendCommand.heatCommand' on device ${childDevice.device.name}"
  def traperror
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
  if(deviceType == "AIR_CONDITIONING")
  {
    def capabilitySupportsHeat = parseCapabilityData(childDevice.getCapabilitySupportsHeat())
    def capabilitysupported = capabilitySupportsHeat
    def capabilitySupportsHeatSwing = parseCapabilityData(childDevice.getCapabilitySupportsHeatSwing())
    def capabilitySupportsHeatAutoFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsHeatAutoFanSpeed())
    def capabilitySupportsHeatFanSpeed = parseCapabilityData(childDevice.getCapabilitySupportsHeatFanSpeed())
    def fancapabilitysupported = capabilitySupportsHeatAutoFanSpeed
    try
    {
      traperror = Integer.parseInt(childDevice.device.currentValue("thermostatSetpoint"))
    }
    catch (NumberFormatException e)
    {
      traperror = 0
    }
    if (capabilitysupported == "true")
    {
      def initialsetpointtemp
      def supportedfanspeed
      if (fancapabilitysupported == "true")
      {
        supportedfanspeed = "AUTO"
      }
      else
      {
        supportedfanspeed = "HIGH"
      }
      if(traperror == 0)
      {
        initialsetpointtemp = settings.defHeatingTemp
      }
      else
      {
        initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
      }
      def jsonbody
	  if (capabilitySupportsHeatSwing == "true" && capabilitySupportsHeatFanSpeed == "true")
      {
      	if (state.tempunit == "C") {
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", swing:"OFF", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      	}
      	else if (state.tempunit == "F"){
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", swing:"OFF", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      	}
      }
      else if(capabilitySupportsHeatSwing == "true" && capabilitySupportsHeatFanSpeed == "false"){
        if (state.tempunit == "C") {
          jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", swing:"OFF", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
              jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", swing:"OFF", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }    
      }
      else if(capabilitySupportsHeatSwing == "false" && capabilitySupportsHeatFanSpeed == "false"){
        if (state.tempunit == "C") {
          jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }
        else if(state.tempunit == "F"){
              jsonbody = new groovy.json.JsonOutput().toJson([setting:[mode:"HEAT", power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[type:terminationmode]])
        }    
      } 
      else
      {
		if (state.tempunit == "C") {
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[celsius:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      	}
      	else if (state.tempunit == "F"){
      		jsonbody = new groovy.json.JsonOutput().toJson([setting:[fanSpeed:supportedfanspeed, mode:"HEAT", power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"AIR_CONDITIONING"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      	}
      }
      sendCommand("temperature",childDevice,[deviceId,jsonbody])
      statusCommand(device)
    }
    else
    {
      log.debug("Sorry Heat Capability not supported on device ${childDevice.device.name}")
    }
  }
  if(deviceType == "HEATING")
  {
      def initialsetpointtemp
      try
      {
        traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
      }
      catch (NumberFormatException e)
      {
        traperror = 0
      }
      if(traperror == 0)
      {
        initialsetpointtemp = settings.defHeatingTemp
      }
      else
      {
        initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
      }
      def jsonbody
      if (state.tempunit == "C") {
      	jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HEATING"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      }
      else if (state.tempunit == "F"){
      	jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"HEATING"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      }
      sendCommand("temperature",childDevice,[deviceId,jsonbody])
      statusCommand(childDevice)
  }
  (deviceType == "HOT_WATER")
  {
    def initialsetpointtemp
    def jsonbody
    def capabilitySupportsWaterTempControl = parseCapabilityData(childDevice.getCapabilitySupportsWaterTempControl())
    if(capabilitySupportsWaterTempControl == "true"){
      try
      {
        traperror = ((childDevice.device.currentValue("thermostatSetpoint")).intValue())
      }
      catch (NumberFormatException e)
      {
        traperror = 0
      }
      if(traperror == 0)
      {
        initialsetpointtemp = settings.defHeatingTemp
      }
      else
      {
        initialsetpointtemp = childDevice.device.currentValue("thermostatSetpoint")
      }
      if (state.tempunit == "C") {
      	jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[celsius:initialsetpointtemp], type:"HOT_WATER"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      }
      else if (state.tempunit == "F"){
      	jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", temperature:[fahrenheit:initialsetpointtemp], type:"HOT_WATER"], termination:[durationInSeconds:"3600", type:"TIMER"]])
      }
    }
    else
    {
      jsonbody = new groovy.json.JsonOutput().toJson([setting:[power:"ON", type:"HOT_WATER"], termination:[durationInSeconds:"3600", type:"TIMER"]])
    }
    sendCommand("temperature",childDevice,[deviceId,jsonbody])
    statusCommand(childDevice)
  }
}

def statusCommand(childDevice){
  def item = (childDevice.device.deviceNetworkId).tokenize('|')
  def deviceId = item[0]
  def deviceType = item[1]
  def deviceToken = item[2]
	log.debug "Executing 'sendCommand.statusCommand'"
	sendCommand("status",childDevice,[deviceId])
}

def userStatusCommand(childDevice){
	try{
		log.debug "Executing 'sendCommand.userStatusCommand'"
		sendCommand("userStatus",childDevice,[])
    	} catch(Exception e) { log.debug("Failed in setting userStatusCommand: " + e)
    }
}