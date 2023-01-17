/**
 *  Copyright 2016 David Lomas (codersaur)
 *
 *  Name: Evohome (Connect)
 *
 *  Author: David Lomas (codersaur)
 *
 *  Date: 2016-04-05
 *
 *  Version: 0.08
 *
 *  Description:
 *   - Connect your Honeywell Evohome System to SmartThings.
 *   - Requires the Evohome Heating Zone device handler.
 *   - For latest documentation see: https://github.com/codersaur/SmartThings
 *
 *  Version History:
 * 
 *   2016-04-05: v0.08
 *    - New 'Update Refresh Time' setting to control polling after making an update.
 *    - poll() - If onlyZoneId is 0, this will force a status update for all zones.
 * 
 *   2016-04-04: v0.07
 *    - Additional info log messages.
 * 
 *   2016-04-03: v0.06
 *    - Initial Beta Release
 * 
 *  To Do:
 *   - Add support for hot water zones (new device handler).
 *   - Tidy up settings: See: http://docs.smartthings.com/en/latest/smartapp-developers-guide/preferences-and-settings.html
 *   - Allow Evohome zones to be (de)selected as part of the setup process.
 *   - Enable notifications if connection to Evohome cloud fails.
 *   - Expose whether thremoStatMode is permanent or temporary, and if temporary for how long. Get from 'tcs.systemModeStatus.*'. i.e thermostatModeUntil
 *   - Investigate if Evohome supports registing a callback so changes in Evohome are pushed to SmartThings instantly (instead of relying on polling).
 *
 *  License:
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *   for the specific language governing permissions and limitations under the License.
 *
 */
definition(
	name: "Evohome (Connect)",
	namespace: "codersaur",
	author: "David Lomas (codersaur)",
	description: "Connect your Honeywell Evohome System to SmartThings.",
	category: "My Apps",
	iconUrl: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
	iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
	iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
	singleInstance: true
)

preferences {

	section ("Evohome:") {
		input "prefEvohomeUsername", "text", title: "Username", required: true, displayDuringSetup: true
		input "prefEvohomePassword", "password", title: "Password", required: true, displayDuringSetup: true
		input title: "Advanced Settings:", displayDuringSetup: true, type: "paragraph", element: "paragraph", description: "Change these only if needed"
		input "prefEvohomeStatusPollInterval", "number", title: "Polling Interval (minutes)", range: "1..60", defaultValue: 5, required: true, displayDuringSetup: true, description: "Poll Evohome every n minutes"
		input "prefEvohomeUpdateRefreshTime", "number", title: "Update Refresh Time (seconds)", range: "2..60", defaultValue: 3, required: true, displayDuringSetup: true, description: "Wait n seconds after an update before polling"
		input "prefEvohomeWindowFuncTemp", "decimal", title: "Window Function Temperature", range: "0..100", defaultValue: 5.0, required: true, displayDuringSetup: true, description: "Must match Evohome controller setting"
		input title: "Thermostat Modes", description: "Configure how long thermostat modes are applied for by default. Set to zero to apply modes permanently.", displayDuringSetup: true, type: "paragraph", element: "paragraph"
		input 'prefThermostatModeDuration', 'number', title: 'Away/Custom/DayOff Mode (days):', range: "0..99", defaultValue: 0, required: true, displayDuringSetup: true, description: 'Apply thermostat modes for this many days'
		input 'prefThermostatEconomyDuration', 'number', title: 'Economy Mode (hours):', range: "0..24", defaultValue: 0, required: true, displayDuringSetup: true, description: 'Apply economy mode for this many hours'
	}

	section("General:") {
		input "prefDebugMode", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: true
	}
	
}

/**********************************************************************
 *  Setup and Configuration Commands:
 **********************************************************************/

/**
 *  installed()
 *
 *  Runs when the app is first installed.
 *
 **/
def installed() {

	atomicState.installedAt = now()
	log.debug "${app.label}: Installed with settings: ${settings}"

}


/**
 *  uninstalled()
 *
 *  Runs when the app is uninstalled.
 *
 **/
def uninstalled() {
	if(getChildDevices()) {
		removeChildDevices(getChildDevices())
	}
}


/**
 *  updated()
 * 
 *  Runs when app settings are changed.
 *
 **/
void updated() {

	if (atomicState.debug) log.debug "${app.label}: Updating with settings: ${settings}"

	// General:
	atomicState.debug = settings.prefDebugMode
	
	// Evohome:
	atomicState.evohomeEndpoint = 'https://tccna.honeywell.com'
	atomicState.evohomeAuth = [tokenLifetimePercentThreshold : 50] // Auth Token will be refreshed when down to 50% of its lifetime.
	atomicState.evohomeStatusPollInterval = settings.prefEvohomeStatusPollInterval // Poll interval for status updates (minutes).
	atomicState.evohomeSchedulePollInterval = 60 // Hardcoded to 1hr (minutes).
	atomicState.evohomeUpdateRefreshTime = settings.prefEvohomeUpdateRefreshTime // Wait this many seconds after an update before polling.
	

	// Thermostat Mode Durations:
	atomicState.thermostatModeDuration = settings.prefThermostatModeDuration
	atomicState.thermostatEconomyDuration = settings.prefThermostatEconomyDuration
	
	// Force Authentication:
	authenticate()

	// Refresh Subscriptions and Schedules:
	manageSubscriptions()
	manageSchedules()
	
	// Refresh child device configuration:
	getEvohomeConfig()
	updateChildDeviceConfig()

	// Run a poll, but defer it so that updated() returns sooner:
	runIn(5, "poll")

}


/**********************************************************************
 *  Management Commands:
 **********************************************************************/

/**
 *  manageSchedules()
 * 
 *  Check scheduled tasks have not stalled, and re-schedule if necessary.
 *  Generates a random offset (seconds) for each scheduled task.
 *  
 *  Schedules:
 *   - manageAuth() - every 5 mins.
 *   - poll() - every minute. 
 *  
 **/
void manageSchedules() {

	if (atomicState.debug) log.debug "${app.label}: manageSchedules()"

	// Generate a random offset (1-60):
	Random rand = new Random(now())
	def randomOffset = 0
	
	// manageAuth (every 5 mins):
	if (1==1) { // To Do: Test if schedule has actually stalled.
		if (atomicState.debug) log.debug "${app.label}: manageSchedules(): Re-scheduling manageAuth()"
		try {
			unschedule(manageAuth)
		}
		catch(e) {
			//if (atomicState.debug) log.debug "${app.label}: manageSchedules(): Unschedule failed"
		}
		randomOffset = rand.nextInt(60)
		schedule("${randomOffset} 0/5 * * * ?", "manageAuth")
	}

	// poll():
	if (1==1) { // To Do: Test if schedule has actually stalled.
		if (atomicState.debug) log.debug "${app.label}: manageSchedules(): Re-scheduling poll()"
		try {
			unschedule(poll)
		}
		catch(e) {
			//if (atomicState.debug) log.debug "${app.label}: manageSchedules(): Unschedule failed"
		}
		randomOffset = rand.nextInt(60)
		schedule("${randomOffset} 0/1 * * * ?", "poll")
	}

}


/**
 *  manageSubscriptions()
 * 
 *  Unsubscribe/Subscribe.
 **/
void manageSubscriptions() {

	if (atomicState.debug) log.debug "${app.label}: manageSubscriptions()"

	// Unsubscribe:
	unsubscribe()
	
	// Subscribe to App Touch events:
	subscribe(app,handleAppTouch)
	
}


/**
 *  manageAuth()
 * 
 *  Ensures authenication token is valid. 
 *   Refreshes Auth Token if lifetime has exceeded evohomeAuthTokenLifetimePercentThreshold.
 *   Re-authenticates if Auth Token has expired completely.
 *   Otherwise, done nothing.
 *
 *  Should be scheduled to run every 1-5 minutes.
 **/
void manageAuth() {

	if (atomicState.debug) log.debug "${app.label}: manageAuth()"

	// Check if Auth Token is valid, if not authenticate:
	if (!atomicState.evohomeAuth.authToken) {
	
		log.info "${app.label}: manageAuth(): No Auth Token. Authenticating..."
		authenticate()
	}
	else if (atomicState.evohomeAuthFailed) {
	
		log.info "${app.label}: manageAuth(): Auth has failed. Authenticating..."
		authenticate()
	}
	else if (!atomicState.evohomeAuth.expiresAt.isNumber() || now() >= atomicState.evohomeAuth.expiresAt) {
	
		log.info "${app.label}: manageAuth(): Auth Token has expired. Authenticating..."
		authenticate()
	}
	else {		
		// Check if Auth Token should be refreshed:
		def refreshAt = atomicState.evohomeAuth.expiresAt - ( 1000 * (atomicState.evohomeAuth.tokenLifetime * atomicState.evohomeAuth.tokenLifetimePercentThreshold / 100))
		
		if (now() >= refreshAt) {
			log.info "${app.label}: manageAuth(): Auth Token needs to be refreshed before it expires."
			refreshAuthToken()
		}
		else {
			log.info "${app.label}: manageAuth(): Auth Token is okay."		
		}
	}

}


/**
 *  poll(onlyZoneId=-1)
 * 
 *  This is the main command that co-ordinates retrieval of information from the Evohome API
 *  and its dissemination to child devices. It should be scheduled to run every minute.
 *
 *  Different types of information are collected on different schedules:
 *   - Zone status information is polled according to ${evohomeStatusPollInterval}.
 *   - Zone schedules are polled according to ${evohomeSchedulePollInterval}.
 *
 *  poll() can be called by a child device when an update has been made, in which case
 *  onlyZoneId will be specified, and only that zone will be updated.
 * 
 *  If onlyZoneId is 0, this will force a status update for all zones, igonoring the poll 
 *  interval. This should only be used after setThremostatMode() call.
 *
 *  If onlyZoneId is not specified all zones are updated, but only if the relevent poll
 *  interval has been exceeded.
 *
 **/
void poll(onlyZoneId=-1) {

	if (atomicState.debug) log.debug "${app.label}: poll(${onlyZoneId})"
	
	// Check if there's been an authentication failure:
	if (atomicState.evohomeAuthFailed) {
		manageAuth()
	}
	
	if (onlyZoneId == 0) { // Force a status update for all zones (used after a thermostatMode update):
		getEvohomeStatus()
		updateChildDevice()
	}
	else if (onlyZoneId != -1) { // A zoneId has been specified, so just get the status and update the relevent device:
		getEvohomeStatus(onlyZoneId)
		updateChildDevice(onlyZoneId)
	}
	else { // Get status and schedule for all zones, but only if the relevent poll interval has been exceeded: 
	
		// Adjust intervals to allow for poll() execution time:
		def evohomeStatusPollThresh = (atomicState.evohomeStatusPollInterval * 60) - 30
		def evohomeSchedulePollThresh = (atomicState.evohomeSchedulePollInterval * 60) - 30

		// Get zone status:
		if (!atomicState.evohomeStatusUpdatedAt || atomicState.evohomeStatusUpdatedAt + (1000 * evohomeStatusPollThresh) < now()) {
			getEvohomeStatus()
		} 

		// Get zone schedules:
		if (!atomicState.evohomeSchedulesUpdatedAt || atomicState.evohomeSchedulesUpdatedAt + (1000 * evohomeSchedulePollThresh) < now()) {
			getEvohomeSchedules()
		}
		
		// Update all child devices:
		updateChildDevice()
	}

}


/**********************************************************************
 *  Event Handlers:
 **********************************************************************/


/**
 *  handleAppTouch(evt)
 * 
 *  App touch event handler.
 *   Used for testing and debugging.
 *
 **/
void handleAppTouch(evt) {

	if (atomicState.debug) log.debug "${app.label}: handleAppTouch()"

	//manageAuth()
	//manageSchedules()
	
	//getEvohomeConfig()
	//updateChildDeviceConfig()
	
	poll()

}


/**********************************************************************
 *  SmartApp-Child Interface Commands:
 **********************************************************************/

/**
 *  updateChildDeviceConfig()
 * 
 *  Add/Remove/Update Child Devices based on atomicState.evohomeConfig
 *  and update their internal state.
 *
 **/
void updateChildDeviceConfig() {

	if (atomicState.debug) log.debug "${app.label}: updateChildDeviceConfig()"
	
	// Build list of active DNIs, any existing children with DNIs not in here will be deleted.
	def activeDnis = []
	
	// Iterate through evohomeConfig, adding new Evohome Heating Zone devices where necessary.
	atomicState.evohomeConfig.each { loc ->
		loc.gateways.each { gateway ->
			gateway.temperatureControlSystems.each { tcs ->
				tcs.zones.each { zone ->
					
					def dni = generateDni(loc.locationInfo.locationId, gateway.gatewayInfo.gatewayId, tcs.systemId, zone.zoneId )
					activeDnis << dni
					
					def values = [
						'debug': atomicState.debug,
						'updateRefreshTime': atomicState.evohomeUpdateRefreshTime,
						'minHeatingSetpoint': formatTemperature(zone?.heatSetpointCapabilities?.minHeatSetpoint),
						'maxHeatingSetpoint': formatTemperature(zone?.heatSetpointCapabilities?.maxHeatSetpoint),
						'temperatureResolution': zone?.heatSetpointCapabilities?.valueResolution,
						'windowFunctionTemperature': formatTemperature(settings.prefEvohomeWindowFuncTemp),
						'zoneType': zone?.zoneType,
						'locationId': loc.locationInfo.locationId,
						'gatewayId': gateway.gatewayInfo.gatewayId,
						'systemId': tcs.systemId,
						'zoneId': zone.zoneId
					]
					
					def d = getChildDevice(dni)
					if(!d) {
						try {
							values.put('label', "${zone.name} Heating Zone (Evohome)")
							log.info "${app.label}: updateChildDeviceConfig(): Creating device: Name: ${values.label},  DNI: ${dni}"
		                   	d = addChildDevice(app.namespace, "Evohome Heating Zone", dni, null, values)
						} catch (e) {
							log.error "${app.label}: updateChildDeviceConfig(): Error creating device: Name: ${values.label}, DNI: ${dni}, Error: ${e}"
						}
					} 
					
					if(d) {
						d.generateEvent(values)
					}
				}
			}
		}
	}
	
	if (atomicState.debug) log.debug "${app.label}: updateChildDeviceConfig(): Active DNIs: ${activeDnis}"
	
	// Delete Devices:
	def delete = getChildDevices().findAll { !activeDnis.contains(it.deviceNetworkId) }
	
	if (atomicState.debug) log.debug "${app.label}: updateChildDeviceConfig(): Found ${delete.size} devices to delete."

	delete.each {
		log.info "${app.label}: updateChildDeviceConfig(): Deleting device with DNI: ${it.deviceNetworkId}"
		try {
			deleteChildDevice(it.deviceNetworkId)
		}
		catch(e) {
			log.error "${app.label}: updateChildDeviceConfig(): Error deleting device with DNI: ${it.deviceNetworkId}. Error: ${e}"
		}
	}
}



/**
 *  updateChildDevice(onlyZoneId=-1)
 * 
 *  Update the attributes of a child device from atomicState.evohomeStatus
 *  and atomicState.evohomeSchedules.
 *  
 *  If onlyZoneId is not specified, then all zones are updated.
 *
 *  Recalculates scheduledSetpoint, nextScheduledSetpoint, and nextScheduledTime.
 *
 **/
void updateChildDevice(onlyZoneId=-1) {

	if (atomicState.debug) log.debug "${app.label}: updateChildDevice(${onlyZoneId})"
	
	atomicState.evohomeStatus.each { loc ->
		loc.gateways.each { gateway ->
			gateway.temperatureControlSystems.each { tcs ->
				tcs.zones.each { zone ->
					if (onlyZoneId == -1 || onlyZoneId == zone.zoneId) { // Filter on zoneId if one has been specified.
					
						def dni = generateDni(loc.locationId, gateway.gatewayId, tcs.systemId, zone.zoneId)
						def d = getChildDevice(dni)
						if(d) {
							def schedule = atomicState.evohomeSchedules.find { it.dni == dni}
							def currSw = getCurrentSwitchpoint(schedule.schedule)
							def nextSw = getNextSwitchpoint(schedule.schedule)

							def values = [
								'temperature': formatTemperature(zone?.temperatureStatus?.temperature),
								//'isTemperatureAvailable': zone?.temperatureStatus?.isAvailable,
								'heatingSetpoint': formatTemperature(zone?.heatSetpointStatus?.targetTemperature),
								'thermostatSetpoint': formatTemperature(zone?.heatSetpointStatus?.targetTemperature),
								'thermostatSetpointMode': formatSetpointMode(zone?.heatSetpointStatus?.setpointMode),
								'thermostatSetpointUntil': zone?.heatSetpointStatus?.until,
								'thermostatMode': formatThermostatMode(tcs?.systemModeStatus?.mode),
								'scheduledSetpoint': formatTemperature(currSw.temperature),
								'nextScheduledSetpoint': formatTemperature(nextSw.temperature),
								'nextScheduledTime': nextSw.time
							]
							if (atomicState.debug) log.debug "${app.label}: updateChildDevice(): Updating Device with DNI: ${dni} with data: ${values}"
							d.generateEvent(values)
						} else {
							if (atomicState.debug) log.debug "${app.label}: updateChildDevice(): Device with DNI: ${dni} does not exist, so skipping status update."
						}
					}
				}
			}
		}
	}
}


/**********************************************************************
 *  Evohome API Commands:
 **********************************************************************/

/**
 *  authenticate()
 * 
 *  Authenticate to Evohome.
 *
 **/
private authenticate() {

	if (atomicState.debug) log.debug "${app.label}: authenticate()"
	
	def requestParams = [
		method: 'POST',
		uri: 'https://tccna.honeywell.com',
		path: '/Auth/OAuth/Token',
		headers: [
			'Authorization': 'Basic YjAxM2FhMjYtOTcyNC00ZGJkLTg4OTctMDQ4YjlhYWRhMjQ5OnRlc3Q=',
			'Accept': 'application/json, application/xml, text/json, text/x-json, text/javascript, text/xml',
			'Content-Type':	'application/x-www-form-urlencoded; charset=utf-8'
		],
		body: [
			'grant_type':	'password',
			'scope':	'EMEA-V1-Basic EMEA-V1-Anonymous EMEA-V1-Get-Current-User-Account',
			'Username':	settings.prefEvohomeUsername,
			'Password':	settings.prefEvohomePassword
		]
	]

	try {
		httpPost(requestParams) { resp ->
			if(resp.status == 200 && resp.data) {
				// Update evohomeAuth:
				// We can't just '.put' or '<<' with atomicState, we have to make a temp copy, edit, and then re-assign.
				def tmpAuth = atomicState.evohomeAuth ?: [:]
			    	tmpAuth.put('lastUpdated' , now())
					tmpAuth.put('authToken' , resp?.data?.access_token)
					tmpAuth.put('tokenLifetime' , resp?.data?.expires_in.toInteger() ?: 0)
					tmpAuth.put('expiresAt' , now() + (tmpAuth.tokenLifetime * 1000))
					tmpAuth.put('refreshToken' , resp?.data?.refresh_token)
				atomicState.evohomeAuth = tmpAuth
				atomicState.evohomeAuthFailed = false
				
				if (atomicState.debug) log.debug "${app.label}: authenticate(): New evohomeAuth: ${atomicState.evohomeAuth}"
				def exp = new Date(tmpAuth.expiresAt)
				log.info "${app.label}: authenticate(): New Auth Token Expires At: ${exp}"

				// Update evohomeHeaders:
				def tmpHeaders = atomicState.evohomeHeaders ?: [:]
					tmpHeaders.put('Authorization',"bearer ${atomicState.evohomeAuth.authToken}")
					tmpHeaders.put('applicationId', 'b013aa26-9724-4dbd-8897-048b9aada249')
					tmpHeaders.put('Accept', 'application/json, application/xml, text/json, text/x-json, text/javascript, text/xml')
				atomicState.evohomeHeaders = tmpHeaders
				
				if (atomicState.debug) log.debug "${app.label}: authenticate(): New evohomeHeaders: ${atomicState.evohomeHeaders}"
				
				// Now get User Account info:
				getEvohomeUserAccount()
			}
			else {
				log.error "${app.label}: authenticate(): No Data. Response Status: ${resp.status}"
				atomicState.evohomeAuthFailed = true
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: authenticate(): Error: e.statusCode ${e.statusCode}"
		atomicState.evohomeAuthFailed = true
	}
	
}


/**
 *  refreshAuthToken()
 * 
 *  Refresh Auth Token.
 *  If token refresh fails, then authenticate() is called.
 *  Request is simlar to authenticate, but with grant_type = 'refresh_token' and 'refresh_token'.
 *
 **/
private refreshAuthToken() {

	if (atomicState.debug) log.debug "${app.label}: refreshAuthToken()"

	def requestParams = [
		method: 'POST',
		uri: 'https://tccna.honeywell.com',
		path: '/Auth/OAuth/Token',
		headers: [
			'Authorization': 'Basic YjAxM2FhMjYtOTcyNC00ZGJkLTg4OTctMDQ4YjlhYWRhMjQ5OnRlc3Q=',
			'Accept': 'application/json, application/xml, text/json, text/x-json, text/javascript, text/xml',
			'Content-Type':	'application/x-www-form-urlencoded; charset=utf-8'
		],
		body: [
			'grant_type':	'refresh_token',
			'scope':	'EMEA-V1-Basic EMEA-V1-Anonymous EMEA-V1-Get-Current-User-Account',
			'refresh_token':	atomicState.evohomeAuth.refreshToken
		]
	]

	try {
		httpPost(requestParams) { resp ->
			if(resp.status == 200 && resp.data) {
				// Update evohomeAuth:
				// We can't just '.put' or '<<' with atomicState, we have to make a temp copy, edit, and then re-assign.
				def tmpAuth = atomicState.evohomeAuth ?: [:]
			    	tmpAuth.put('lastUpdated' , now())
					tmpAuth.put('authToken' , resp?.data?.access_token)
					tmpAuth.put('tokenLifetime' , resp?.data?.expires_in.toInteger() ?: 0)
					tmpAuth.put('expiresAt' , now() + (tmpAuth.tokenLifetime * 1000))
					tmpAuth.put('refreshToken' , resp?.data?.refresh_token)
				atomicState.evohomeAuth = tmpAuth
				atomicState.evohomeAuthFailed = false
				
				if (atomicState.debug) log.debug "${app.label}: refreshAuthToken(): New evohomeAuth: ${atomicState.evohomeAuth}"
				def exp = new Date(tmpAuth.expiresAt)
				log.info "${app.label}: refreshAuthToken(): New Auth Token Expires At: ${exp}"

				// Update evohomeHeaders:
				def tmpHeaders = atomicState.evohomeHeaders ?: [:]
					tmpHeaders.put('Authorization',"bearer ${atomicState.evohomeAuth.authToken}")
					tmpHeaders.put('applicationId', 'b013aa26-9724-4dbd-8897-048b9aada249')
					tmpHeaders.put('Accept', 'application/json, application/xml, text/json, text/x-json, text/javascript, text/xml')
				atomicState.evohomeHeaders = tmpHeaders
				
				if (atomicState.debug) log.debug "${app.label}: refreshAuthToken(): New evohomeHeaders: ${atomicState.evohomeHeaders}"
				
				// Now get User Account info:
				getEvohomeUserAccount()
			}
			else {
				log.error "${app.label}: refreshAuthToken(): No Data. Response Status: ${resp.status}"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: refreshAuthToken(): Error: e.statusCode ${e.statusCode}"
		// If Unauthorized (401) then re-authenticate:
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
			authenticate()
		}
	}
	
}


/**
 *  getEvohomeUserAccount()
 * 
 *  Gets user account info and stores in atomicState.evohomeUserAccount.
 *
 **/
private getEvohomeUserAccount() {

	log.info "${app.label}: getEvohomeUserAccount(): Getting user account information."
	
	def requestParams = [
		method: 'GET',
		uri: atomicState.evohomeEndpoint,
		path: '/WebAPI/emea/api/v1/userAccount',
		headers: atomicState.evohomeHeaders
	]

	try {
		httpGet(requestParams) { resp ->
			if (resp.status == 200 && resp.data) {
				atomicState.evohomeUserAccount = resp.data
				if (atomicState.debug) log.debug "${app.label}: getEvohomeUserAccount(): Data: ${atomicState.evohomeUserAccount}"
			}
			else {
				log.error "${app.label}: getEvohomeUserAccount(): No Data. Response Status: ${resp.status}"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: getEvohomeUserAccount(): Error: e.statusCode ${e.statusCode}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
	}
}



/**
 *  getEvohomeConfig()
 * 
 *  Gets Evohome configuration for all locations and stores in atomicState.evohomeConfig.
 *
 **/
private getEvohomeConfig() {

	log.info "${app.label}: getEvohomeConfig(): Getting configuration for all locations."

	def requestParams = [
		method: 'GET',
		uri: atomicState.evohomeEndpoint,
		path: '/WebAPI/emea/api/v1/location/installationInfo',
		query: [
			'userId': atomicState.evohomeUserAccount.userId,
			'includeTemperatureControlSystems': 'True'
		],
		headers: atomicState.evohomeHeaders
	]

	try {
		httpGet(requestParams) { resp ->
			if (resp.status == 200 && resp.data) {
				if (atomicState.debug) log.debug "${app.label}: getEvohomeConfig(): Data: ${resp.data}"
				atomicState.evohomeConfig = resp.data
				atomicState.evohomeConfigUpdatedAt = now()
				return null
			}
			else {
				log.error "${app.label}: getEvohomeConfig(): No Data. Response Status: ${resp.status}"
				return 'error'
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: getEvohomeConfig(): Error: e.statusCode ${e.statusCode}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
		return e
	}
}


/**
 *  getEvohomeStatus(onlyZoneId=-1)
 * 
 *  Gets Evohome Status for specified zone and stores in atomicState.evohomeStatus.
 *  If onlyZoneId is not specified, all zones are updated.
 *
 **/
private getEvohomeStatus(onlyZoneId=-1) {

	if (atomicState.debug) log.debug "${app.label}: getEvohomeStatus(${onlyZoneId})"
	
	def newEvohomeStatus = []
	
	if (onlyZoneId == -1) { // Update all zones (which can be obtained en-masse for each location):
		
		log.info "${app.label}: getEvohomeStatus(): Getting status for all zones."
		
		atomicState.evohomeConfig.each { loc ->
			def locStatus = getEvohomeLocationStatus(loc.locationInfo.locationId)
			if (locStatus) {
				newEvohomeStatus << locStatus
			}
		}

		if (newEvohomeStatus) {
			// Write out newEvohomeStatus back to atomicState:
			atomicState.evohomeStatus = newEvohomeStatus
			atomicState.evohomeStatusUpdatedAt = now()
		}
	}
	else { // Only update the specified zone:
		
		log.info "${app.label}: getEvohomeStatus(): Getting status for zone ID: ${onlyZoneId}"
		
		def newZoneStatus = getEvohomeZoneStatus(onlyZoneId)
		if (newZoneStatus) {
			// Get existing evohomeStatus and update only the specified zone, preserving data for other zones:
			// Have to do this as atomicState.evohomeStatus can only be written in its entirety (as using atomicstate).
			// If mutiple zones are requesting updates at the same time this could cause loss of new data, but
			// the worse case is having out-of-date data for a few minutes...
			newEvohomeStatus = atomicState.evohomeStatus
			newEvohomeStatus.each { loc ->
				loc.gateways.each { gateway ->
					gateway.temperatureControlSystems.each { tcs ->
						tcs.zones.each { zone ->
							if (onlyZoneId == zone.zoneId) { // This is the zone that must be updated:
								zone.activeFaults = newZoneStatus.activeFaults
								zone.heatSetpointStatus = newZoneStatus.heatSetpointStatus
								zone.temperatureStatus = newZoneStatus.temperatureStatus
							}
						}
					}
				}
			}
			// Write out newEvohomeStatus back to atomicState:
			atomicState.evohomeStatus = newEvohomeStatus
			// Note: atomicState.evohomeStatusUpdatedAt is NOT updated.
		} 
	}
}


/**
 *  getEvohomeLocationStatus(locationId)
 * 
 *  Gets the status for a specific location and returns data as a map.
 *
 *  Called by getEvohomeStatus().
 **/
private getEvohomeLocationStatus(locationId) {

	if (atomicState.debug) log.debug "${app.label}: getEvohomeLocationStatus: Location ID: ${locationId}"
	
	def requestParams = [
		'method': 'GET',
		'uri': atomicState.evohomeEndpoint,
		'path': "/WebAPI/emea/api/v1/location/${locationId}/status", 
		'query': [ 'includeTemperatureControlSystems': 'True'],
		'headers': atomicState.evohomeHeaders
	]

	try {
		httpGet(requestParams) { resp ->
			if(resp.status == 200 && resp.data) {
				if (atomicState.debug) log.debug "${app.label}: getEvohomeLocationStatus: Data: ${resp.data}"
				return resp.data
			}
			else {
				log.error "${app.label}: getEvohomeLocationStatus:  No Data. Response Status: ${resp.status}"
				return false
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: getEvohomeLocationStatus: Error: e.statusCode ${e.statusCode}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
		return false
	}
}


/**
 *  getEvohomeZoneStatus(zoneId)
 * 
 *  Gets the status for a specific zone and returns data as a map.
 *
 **/
private getEvohomeZoneStatus(zoneId) {

	if (atomicState.debug) log.debug "${app.label}: getEvohomeZoneStatus(${zoneId})"
	
	def requestParams = [
		'method': 'GET',
		'uri': atomicState.evohomeEndpoint,
		'path': "/WebAPI/emea/api/v1/temperatureZone/${zoneId}/status",
		'headers': atomicState.evohomeHeaders
	]

	try {
		httpGet(requestParams) { resp ->
			if(resp.status == 200 && resp.data) {
				if (atomicState.debug) log.debug "${app.label}: getEvohomeZoneStatus: Data: ${resp.data}"
				return resp.data
			}
			else {
				log.error "${app.label}: getEvohomeZoneStatus:  No Data. Response Status: ${resp.status}"
				return false
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: getEvohomeZoneStatus: Error: e.statusCode ${e.statusCode}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
		return false
	}
}


/**
 *  getEvohomeSchedules()
 * 
 *  Gets Evohome Schedule for each zone and stores in atomicState.evohomeSchedules.
 *
 **/
private getEvohomeSchedules() {

	log.info "${app.label}: getEvohomeSchedules(): Getting schedules for all zones."
			
	def evohomeSchedules = []
		
	atomicState.evohomeConfig.each { loc ->
		loc.gateways.each { gateway ->
			gateway.temperatureControlSystems.each { tcs ->
				tcs.zones.each { zone ->
					def dni = generateDni(loc.locationInfo.locationId, gateway.gatewayInfo.gatewayId, tcs.systemId, zone.zoneId )
					def schedule = getEvohomeZoneSchedule(zone.zoneId)
					if (schedule) {
						evohomeSchedules << ['zoneId': zone.zoneId, 'dni': dni, 'schedule': schedule]
					}
				}
			}
		}
	}

	if (evohomeSchedules) {
		// Write out complete schedules to state:
		atomicState.evohomeSchedules = evohomeSchedules
		atomicState.evohomeSchedulesUpdatedAt = now()
	}

	return evohomeSchedules
}


/**
 *  getEvohomeZoneSchedule(zoneId)
 * 
 *  Gets the schedule for a specific zone and returns data as a map.
 *
 **/
private getEvohomeZoneSchedule(zoneId) {
	if (atomicState.debug) log.debug "${app.label}: getEvohomeZoneSchedule(${zoneId})"
	
	def requestParams = [
		'method': 'GET',
		'uri': atomicState.evohomeEndpoint,
		'path': "/WebAPI/emea/api/v1/temperatureZone/${zoneId}/schedule",
		'headers': atomicState.evohomeHeaders
	]

	try {
		httpGet(requestParams) { resp ->
			if(resp.status == 200 && resp.data) {
				if (atomicState.debug) log.debug "${app.label}: getEvohomeZoneSchedule: Data: ${resp.data}"
				return resp.data
			}
			else {
				log.error "${app.label}: getEvohomeZoneSchedule:  No Data. Response Status: ${resp.status}"
				return false
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: getEvohomeZoneSchedule: Error: e.statusCode ${e.statusCode}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
		return false
	}
}


/**
 *  setThermostatMode(systemId, mode, until)
 * 
 *  Set thermostat mode for specified controller, until specified time.
 *
 *   systemId:   SystemId of temperatureControlSystem. E.g.: 123456
 *
 *   mode:       String. Either: "auto", "off", "economy", "away", "dayOff", "custom".
 *
 *   until:      (Optional) Time to apply mode until, can be either:
 *                - Date: date object representing when override should end.
 *                - ISO-8601 date string, in format "yyyy-MM-dd'T'HH:mm:ssXX", e.g.: "2016-04-01T00:00:00Z".
 *                - String: 'permanent'.
 *                - Number: Duration in hours if mode is 'economy', or in days if mode is 'away'/'dayOff'/'custom'.
 *                          Duration will be rounded down to align with Midnight in the local timezone
 *                          (e.g. a duration of 1 day will end at midnight tonight). If 0, mode is permanent.
 *                If 'until' is not specified, a default value is used from the SmartApp settings.
 *
 *   Notes:      'Auto' and 'Off' modes are always permanent.
 *               Thermostat mode is a property of the temperatureControlSystem (i.e. Evohome controller).
 *               Therefore changing the thermostatMode will affect all zones associated with the same controller.
 * 
 * 
 *  Example usage:
 *   setThermostatMode(123456, 'auto') // Set auto mode permanently, for controller 123456.
 *   setThermostatMode(123456, 'away','2016-04-01T00:00:00Z') // Set away mode until 1st April, for controller 123456.
 *   setThermostatMode(123456, 'dayOff','permanent') // Set dayOff mode permanently, for controller 123456.
 *   setThermostatMode(123456, 'dayOff', 2) // Set dayOff mode for 2 days (ends tomorrow night), for controller 123456.
 *   setThermostatMode(123456, 'economy', 2) // Set economy mode for 2 hours, for controller 123456.
 *
 **/
def setThermostatMode(systemId, mode, until=-1) {

	if (atomicState.debug) log.debug "${app.label}: setThermostatMode(): SystemID: ${systemId}, Mode: ${mode}, Until: ${until}"
	
	// Clean mode (translate to index):
	mode = mode.toLowerCase()
	int modeIndex
	switch (mode) {
		case 'auto':
			modeIndex = 0
			break
		case 'off':
			modeIndex = 1
			break
		case 'economy':
			modeIndex = 2
			break
		case 'away':
			modeIndex = 3
			break
		case 'dayoff':
			modeIndex = 4
			break
		case 'custom':
			modeIndex = 6
			break
		default:
			log.error "${app.label}: setThermostatMode(): Mode: ${mode} is not supported!"
			modeIndex = 999
			break
	}
	
	// Clean until:
	def untilRes
	
	// until has not been specified, so determine behaviour from settings:
	if (-1 == until && 'economy' == mode) { 
		until = atomicState.thermostatEconomyDuration ?: 0 // Use Default duration for economy mode (hours):
	}
	else if (-1 == until && ( 'away' == mode ||'dayoff' == mode ||'custom' == mode )) {
		until = atomicState.thermostatModeDuration ?: 0 // Use Default duration for other modes (days):
	}
	
	// Convert to date (or 0):    
	if ('permanent' == until || 0 == until || -1 == until) {
		untilRes = 0
	}
	else if (until instanceof Date) {
		untilRes = until.format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC')) // Round to nearest minute.
	}
	else if (until ==~ /\d+.*T.*/) { // until is a ISO-8601 date string already, but we'll re-format it anyway to ensure it's in UTC:
		untilRes = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXX", until).format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC')) // Round to nearest minute.
	}
	else if (until.isNumber() && 'economy' == mode) { // until is a duration in hours:
		untilRes = new Date( now() + (Math.round(until) * 3600000) ).format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC')) // Round to nearest minute.
	}
	else if (until.isNumber() && ('away' == mode ||'dayoff' == mode ||'custom' == mode )) { // until is a duration in days:
		untilRes = new Date( now() + (Math.round(until) * 86400000) ).format("yyyy-MM-dd'T'00:00:00XX", location.timeZone) // Round down to midnight in the LOCAL timezone.
	}
	else {
		log.warn "${device.label}: setThermostatMode(): until value could not be parsed. Mode will be applied permanently."
		untilRes = 0
	}
	
	// If mode is away/dayOff/custom the date needs to be rounded down to midnight in the local timezone, then converted back to string again:
	if (0 != untilRes && ('away' == mode ||'dayoff' == mode ||'custom' == mode )) { 
		untilRes = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXX", new Date().parse("yyyy-MM-dd'T'HH:mm:ssXX", untilRes).format("yyyy-MM-dd'T'00:00:00XX", location.timeZone) ).format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC'))
	}

	// Build request:
	def body
	if (0 == untilRes || 'off' == mode || 'auto' == mode) { // Mode is permanent:
		body = ['SystemMode': modeIndex, 'TimeUntil': null, 'Permanent': 'True']
		log.info "${app.label}: setThermostatMode(): System ID: ${systemId}, Mode: ${mode}, Permanent: True"
	}
	else { // Mode is temporary:
		body = ['SystemMode': modeIndex, 'TimeUntil': untilRes, 'Permanent': 'False']
		log.info "${app.label}: setThermostatMode(): System ID: ${systemId}, Mode: ${mode}, Permanent: False, Until: ${untilRes}"
	}
	
	def requestParams = [
		'uri': atomicState.evohomeEndpoint,
		'path': "/WebAPI/emea/api/v1/temperatureControlSystem/${systemId}/mode", 
		'body': body,
		'headers': atomicState.evohomeHeaders
	]
	
	// Make request:
	try {
		httpPutJson(requestParams) { resp ->
			if(resp.status == 201 && resp.data) {
				if (atomicState.debug) log.debug "${app.label}: setThermostatMode(): Response: ${resp.data}"
				return null
			}
			else {
				log.error "${app.label}: setThermostatMode():  No Data. Response Status: ${resp.status}"
				return 'error'
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: setThermostatMode(): Error: ${e}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
		return e
	}
}


 /**
 *  setHeatingSetpoint(zoneId, setpoint, until=-1)
 * 
 *  Set heatingSetpoint for specified zoneId, until specified time.
 *
 *   zoneId:     Zone ID of zone, e.g.: "123456"
 *
 *   setpoint:   Setpoint temperature, e.g.: "21.5". Can be a number or string.
 *
 *   until:      (Optional) Time to apply setpoint until, can be either:
 *                - Date: date object representing when override should end.
 *                - ISO-8601 date string, in format "yyyy-MM-dd'T'HH:mm:ssXX", e.g.: "2016-04-01T00:00:00Z".
 *                - String: 'permanent'.
 *               If not specified, setpoint will be applied permanently.
 *
 *  Example usage:
 *   setHeatingSetpoint(123456, 21.0) // Set temp of 21.0 permanently, for zone 123456.
 *   setHeatingSetpoint(123456, 21.0, 'permanent') // Set temp of 21.0 permanently, for zone 123456.
 *   setHeatingSetpoint(123456, 21.0, '2016-04-01T00:00:00Z') // Set until specific time, for zone 123456.
 *
 **/
def setHeatingSetpoint(zoneId, setpoint, until=-1) {

	if (atomicState.debug) log.debug "${app.label}: setHeatingSetpoint(): Zone ID: ${zoneId}, Setpoint: ${setpoint}, Until: ${until}"
	
	// Clean setpoint:
	setpoint = formatTemperature(setpoint)
	
	// Clean until:
	def untilRes
	if ('permanent' == until || 0 == until || -1 == until) {
		untilRes = 0
	}
	else if (until instanceof Date) {
		untilRes = until.format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC')) // Round to nearest minute.
	}
	else if (until ==~ /\d+.*T.*/) { // until is a ISO-8601 date string already, but we'll re-format it anyway to ensure it's in UTC:
		untilRes = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXX", until).format("yyyy-MM-dd'T'HH:mm:00XX", TimeZone.getTimeZone('UTC')) // Round to nearest minute.
	}
	else {
		log.warn "${device.label}: setHeatingSetpoint(): until value could not be parsed. Setpoint will be applied permanently."
		untilRes = 0
	}
	
	// Build request:
	def body
	if (0 == untilRes) { // Permanent:
		body = ['HeatSetpointValue': setpoint, 'SetpointMode': 1, 'TimeUntil': null]
		log.info "${app.label}: setHeatingSetpoint(): Zone ID: ${zoneId}, Setpoint: ${setpoint}, Until: Permanent"
	}
	else { // Temporary:
		body = ['HeatSetpointValue': setpoint, 'SetpointMode': 2, 'TimeUntil': untilRes]
		log.info "${app.label}: setHeatingSetpoint(): Zone ID: ${zoneId}, Setpoint: ${setpoint}, Until: ${untilRes}"
	}
	
	def requestParams = [
		'uri': atomicState.evohomeEndpoint,
		'path': "/WebAPI/emea/api/v1/temperatureZone/${zoneId}/heatSetpoint", 
		'body': body,
		'headers': atomicState.evohomeHeaders
	]
	
	// Make request:
	try {
		httpPutJson(requestParams) { resp ->
			if(resp.status == 201 && resp.data) {
				if (atomicState.debug) log.debug "${app.label}: setHeatingSetpoint(): Response: ${resp.data}"
				return null
			}
			else {
				log.error "${app.label}: setHeatingSetpoint():  No Data. Response Status: ${resp.status}"
				return 'error'
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: setHeatingSetpoint(): Error: ${e}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
		return e
	}
}


/**
 *  clearHeatingSetpoint(zoneId)
 * 
 *  Clear the heatingSetpoint for specified zoneId.
 *   zoneId:     Zone ID of zone, e.g.: "123456"
 **/
def clearHeatingSetpoint(zoneId) {

	log.info "${app.label}: clearHeatingSetpoint(): Zone ID: ${zoneId}"
	
	// Build request:
	def requestParams = [
		'uri': atomicState.evohomeEndpoint,
		'path': "/WebAPI/emea/api/v1/temperatureZone/${zoneId}/heatSetpoint", 
		'body': ['HeatSetpointValue': 0.0, 'SetpointMode': 0, 'TimeUntil': null],
		'headers': atomicState.evohomeHeaders
	]
	
	// Make request:
	try {
		httpPutJson(requestParams) { resp ->
			if(resp.status == 201 && resp.data) {
				if (atomicState.debug) log.debug "${app.label}: clearHeatingSetpoint(): Response: ${resp.data}"
				return null
			}
			else {
				log.error "${app.label}: clearHeatingSetpoint():  No Data. Response Status: ${resp.status}"
				return 'error'
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "${app.label}: clearHeatingSetpoint(): Error: ${e}"
		if (e.statusCode == 401) {
			atomicState.evohomeAuthFailed = true
		}
		return e
	}
}


/**********************************************************************
 *  Helper Commands:
 **********************************************************************/
 
 /**
 *  generateDni(locId,gatewayId,systemId,deviceId)
 * 
 *  Generate a device Network ID.
 *  Uses the same format as the official Evohome App, but with a prefix of "Evohome."
 **/
private generateDni(locId,gatewayId,systemId,deviceId) {
	return 'Evohome.' + [ locId, gatewayId, systemId, deviceId ].join('.')
}
 
 
/**
 *  formatTemperature(t)
 * 
 *  Format temperature value to one decimal place.
 *  t:   can be string, float, bigdecimal...
 *  Returns as string.
 **/
private formatTemperature(t) {
	return Float.parseFloat("${t}").round(1).toString()
}
 
 
 /**
 *  formatSetpointMode(mode)
 * 
 *  Format Evohome setpointMode values to SmartThings values:
 *   
 **/
private formatSetpointMode(mode) {
 
	switch (mode) {
		case 'FollowSchedule':
			mode = 'followSchedule'
			break
		case 'PermanentOverride':
			mode = 'permanentOverride'
			break
		case 'TemporaryOverride':
			mode = 'temporaryOverride'
			break
		default:
			log.error "${app.label}: formatSetpointMode(): Mode: ${mode} unknown!"
			mode = mode.toLowerCase()
			break
	}

	return mode
}
 
 
/**
 *  formatThermostatMode(mode)
 * 
 *  Translate Evohome thermostatMode values to SmartThings values.
 *   
 **/
private formatThermostatMode(mode) {
 
	switch (mode) {
		case 'Auto':
			mode = 'auto'
			break
		case 'AutoWithEco':
			mode = 'economy'
			break
		case 'Away':
			mode = 'away'
			break
		case 'Custom':
			mode = 'custom'
			break
		case 'DayOff':
			mode = 'dayOff'
			break
		case 'HeatingOff':
			mode = 'off'
			break
		default:
			log.error "${app.label}: formatThermostatMode(): Mode: ${mode} unknown!"
			mode = mode.toLowerCase()
			break
	}

	return mode
}
  

/**
 *  getCurrentSwitchpoint(schedule)
 * 
 *  Returns the current active switchpoint in the given schedule.
 *  e.g. [timeOfDay:"23:00:00", temperature:"15.0000"]
 *   
 **/
private getCurrentSwitchpoint(schedule) {

	if (atomicState.debug) log.debug "${app.label}: getCurrentSwitchpoint()"
	
	Calendar c = new GregorianCalendar()
	def ScheduleToday = schedule.dailySchedules.find { it.dayOfWeek = c.getTime().format("EEEE", location.timeZone) }
	
	// Sort and find next switchpoint:
	ScheduleToday.switchpoints.sort {it.timeOfDay}
	ScheduleToday.switchpoints.reverse(true)
	def currentSwitchPoint = ScheduleToday.switchpoints.find {it.timeOfDay < c.getTime().format("HH:mm:ss", location.timeZone)}
	
	if (!currentSwitchPoint) {
		// There are no current switchpoints today, so we must look for the last Switchpoint yesterday.
		if (atomicState.debug) log.debug "${app.label}: getCurrentSwitchpoint(): No current switchpoints today, so must look to yesterday's schedule."
		c.add(Calendar.DATE, -1 ) // Subtract one DAY.
		def ScheduleYesterday = schedule.dailySchedules.find { it.dayOfWeek = c.getTime().format("EEEE", location.timeZone) }
		ScheduleYesterday.switchpoints.sort {it.timeOfDay}
		ScheduleYesterday.switchpoints.reverse(true)
		currentSwitchPoint = ScheduleYesterday.switchpoints[0] // There will always be one.
	}
	
	// Now construct the switchpoint time as a full ISO-8601 format date string in UTC:
	def localDateStr = c.getTime().format("yyyy-MM-dd'T'", location.timeZone) + currentSwitchPoint.timeOfDay + c.getTime().format("XX", location.timeZone) // Switchpoint in local timezone.
	def isoDateStr = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXX", localDateStr).format("yyyy-MM-dd'T'HH:mm:ssXX", TimeZone.getTimeZone('UTC')) // Parse and re-format to UTC timezone.    
	currentSwitchPoint << [ 'time': isoDateStr ]
	if (atomicState.debug) log.debug "${app.label}: getCurrentSwitchpoint(): Current Switchpoint: ${currentSwitchPoint}"
	
	return currentSwitchPoint
}
 

/**
 *  getNextSwitchpoint(schedule)
 * 
 *  Returns the next switchpoint in the given schedule.
 *  e.g. [timeOfDay:"23:00:00", temperature:"15.0000"]
 *   
 **/
private getNextSwitchpoint(schedule) {

	if (atomicState.debug) log.debug "${app.label}: getNextSwitchpoint()"
	
	Calendar c = new GregorianCalendar()
	def ScheduleToday = schedule.dailySchedules.find { it.dayOfWeek = c.getTime().format("EEEE", location.timeZone) }
	
	// Sort and find next switchpoint:
	ScheduleToday.switchpoints.sort {it.timeOfDay}
	def nextSwitchPoint = ScheduleToday.switchpoints.find {it.timeOfDay > c.getTime().format("HH:mm:ss", location.timeZone)}
	
	if (!nextSwitchPoint) {
		// There are no switchpoints left today, so we must look for the first Switchpoint tomorrow.
		if (atomicState.debug) log.debug "${app.label}: getNextSwitchpoint(): No more switchpoints today, so must look to tomorrow's schedule."
		c.add(Calendar.DATE, 1 ) // Add one DAY.
		def ScheduleTmrw = schedule.dailySchedules.find { it.dayOfWeek = c.getTime().format("EEEE", location.timeZone) }
		ScheduleTmrw.switchpoints.sort {it.timeOfDay}
		nextSwitchPoint = ScheduleTmrw.switchpoints[0] // There will always be one.
	}

	// Now construct the switchpoint time as a full ISO-8601 format date string in UTC:
	def localDateStr = c.getTime().format("yyyy-MM-dd'T'", location.timeZone) + nextSwitchPoint.timeOfDay + c.getTime().format("XX", location.timeZone) // Switchpoint in local timezone.
	def isoDateStr = new Date().parse("yyyy-MM-dd'T'HH:mm:ssXX", localDateStr).format("yyyy-MM-dd'T'HH:mm:ssXX", TimeZone.getTimeZone('UTC')) // Parse and re-format to UTC timezone.    
	nextSwitchPoint << [ 'time': isoDateStr ]
	if (atomicState.debug) log.debug "${app.label}: getNextSwitchpoint(): Next Switchpoint: ${nextSwitchPoint}"
	
	return nextSwitchPoint
}