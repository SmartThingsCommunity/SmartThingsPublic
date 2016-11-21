/**
 *  SimpliSafe integration for SmartThings
 *
 *  Copyright 2015 Felix Gorodishter
 *	Modifications by Toby Harris - 5/10/2016
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

preferences {
	input(name: "username", type: "text", title: "Username", required: "true", description: "Your SimpliSafe username")
	input(name: "password", type: "password", title: "Password", required: "true", description: "Your SimpliSafe password")
}

metadata {
	// Automatically generated. Make future change here.
	definition (name: "SimpliSafe", namespace: "tobycth3", author: "Toby Harris") {
		capability "Alarm"
		capability "Polling"
		capability "Acceleration Sensor"
        capability "Contact Sensor"
		capability "Carbon Monoxide Detector"
        capability "Lock"
		capability "Presence Sensor"
		capability "Smoke Detector"
        capability "Temperature Measurement"
        capability "Water Sensor"
		command "home"
		command "away"
		command "off"
		command "update_state"
		command "update_temp"
		attribute "events", "string"
		attribute "recent_alarm", "string" 
		attribute "recent_fire", "string" 
		attribute "recent_co", "string" 
		attribute "recent_flood", "string" 
		attribute "warnings", "string"        
	}

	simulator {
		// TODO: define status and reply messages here
	}


tiles(scale: 2) {
    multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4){
        tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
            attributeState "off", label:'${name}', icon: "st.security.alarm.off", backgroundColor: "#505050"
            attributeState "home", label:'${name}', icon: "st.Home.home4", backgroundColor: "#00BEAC"
            attributeState "away", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#008CC1"
			attributeState "pending off", label:'${name}', icon: "st.security.alarm.off", backgroundColor: "#ffffff"
			attributeState "pending away", label:'${name}', icon: "st.Home.home4", backgroundColor: "#ffffff"
			attributeState "pending home", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#ffffff"
			attributeState "failed set", label:'error', icon: "st.secondary.refresh", backgroundColor: "#d44556"
        }
		
		tileAttribute("device.events", key: "SECONDARY_CONTROL", wordWrap: true) {
			attributeState("default", label:'${currentValue}')
		}
    }	
	
    standardTile("off", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#008CC1", nextState: "pending")
        state ("away", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "pending")
        state ("home", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "pending")
        state ("pending", label:"pending", icon: "st.security.alarm.off", backgroundColor: "#ffffff")
	}
	
    standardTile("away", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#505050", nextState: "pending") 
		state ("away", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#008CC1", nextState: "pending")
        state ("home", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#505050", nextState: "pending")
		state ("pending", label:"pending", icon: "st.security.alarm.on", backgroundColor: "#ffffff")
	}
	
    standardTile("home", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#505050", nextState: "pending")
        state ("away", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#505050", nextState: "pending")
		state ("home", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#008CC1", nextState: "pending")
		state ("pending", label:"pending", icon: "st.Home.home4", backgroundColor: "#ffffff")
	}
    
		standardTile("recent_alarm", "device.contact", inactiveLabel: false, width: 2, height: 2) {
			state "closed", label:'Alarm', icon: "st.security.alarm.clear", backgroundColor: "#50C65F"
			state "open", label:'ALARM', icon: "st.security.alarm.alarm", backgroundColor: "#d44556"
		}
		standardTile("freeze", "device.freeze_status", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state ("no alert", label:'Temp', action:"update_temp", icon: "st.alarm.temperature.normal", backgroundColor: "#50C65F", nextState: "updating")
			state ("alert", label:'TEMP', action:"update_temp", icon: "st.alarm.temperature.freeze", backgroundColor: "#d44556", nextState: "updating")
			state ("updating", label:"updating", icon: "st.alarm.temperature.normal", backgroundColor: "#ffffff")
		}
		standardTile("recent_fire", "device.smoke", inactiveLabel: false, width: 2, height: 2) {
			state "clear", label:'Fire', icon: "st.alarm.smoke.clear", backgroundColor: "#50C65F"
			state "detected", label:'FIRE', icon: "st.alarm.smoke.smoke", backgroundColor: "#d44556"
		}
		standardTile("recent_co", "device.carbonMonoxide", inactiveLabel: false, width: 2, height: 2) {
			state "clear", label:'CO2', icon: "st.alarm.carbon-monoxide.clear", backgroundColor: "#50C65F"
			state "detected", label:'CO2', icon: "st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor: "#d44556"
		}
		standardTile("recent_flood", "device.water", inactiveLabel: false, width: 2, height: 2) {
			state "dry", label:'Flood', icon: "st.alarm.water.dry", backgroundColor: "#50C65F"
			state "wet", label:'FLOOD', icon: "st.alarm.water.wet", backgroundColor: "#d44556"
		}
		standardTile("warnings", "device.acceleration", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state ("inactive", label:'Base', action:"update_state", icon: "st.Kids.kids15", backgroundColor: "#50C65F", nextState: "updating")
			state ("active", label:'BASE', action:"update_state", icon: "st.Kids.kids15", backgroundColor: "#d44556", nextState: "updating")
			state ("updating", label:"updating", icon: "st.Kids.kids15", backgroundColor: "#ffffff")
		}
		standardTile("refresh", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"polling.poll", icon:"st.secondary.refresh"
		}

		main(["alarm"])
		details(["alarm","off", "away", "home", "recent_alarm", "freeze", "recent_fire", "recent_co", "recent_flood", "warnings", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'alarm' attribute

}

// handle commands
def off() {
	log.info "Executing 'off'"
	api('set-state', [state: off, mobile: 1]) { response ->
	//	log.trace "Set-state response $response.status $response.data"
	}
	// refresh status
	poll()
}

def home() { 
	log.info "Executing 'home'"
	api('set-state', [state: home, mobile: 1]) { response ->
	//	log.trace "Set-state response $response.status $response.data"
	}
	// refresh status
	poll()	
}

def away() {
	log.info "Executing 'away'"
	api('set-state', [state: away, mobile: 1]) { response ->
	//	log.trace "Set-state response $response.status $response.data"
	}
	// refresh status
	poll()
}

def update_state() {
		log.info "Updating state from base station"
	api('get-state', []) { response ->
	//	log.trace "Get-state response $response.status $response.data"
	}
	// refresh status
	poll()   
  }
	
def update_temp() {
		log.info "Updating temperature from base station"
	api('update-freeze', []) { response ->
	//	log.trace "Update-freeze response $response.status $response.data"
	}
	// refresh status
	poll()
}

def setAlarmMode(mode) {
	// TODO
}

def strobe() {
	log.info "Executing 'strobe'"
	// TODO: handle 'strobe' command
}

def siren() {
	log.info "Executing 'siren'"
	// TODO: handle 'siren' command
}

def both() {
	log.info "Executing 'both'"
	// TODO: handle 'both' command
}

def poll() {
	log.info "Executing 'poll'"

	log.info "Executing 'status'"
	api('status', []) { response ->
	//	log.trace "Status response $response.status $response.data"

		if (response.data.return_code < 1) {
			return
		}

		def location = response.data.location

		def new_state = location.system.state
		def old_state = device.currentValue("alarm")
		def state_changed = new_state != old_state

        def new_recent_alarm = location.monitoring.recent_alarm.text
		def old_recent_alarm = device.currentValue("recent_alarm")
		def recent_alarm_changed = new_recent_alarm != old_recent_alarm

        def new_recent_fire = location.monitoring.recent_fire.text
		def old_recent_fire = device.currentValue("recent_fire")
		def recent_fire_changed = new_recent_fire != old_recent_fire

        def new_recent_co = location.monitoring.recent_co.text
		def old_recent_co = device.currentValue("recent_co")
		def recent_co_changed = new_recent_co != old_recent_co

		def new_recent_flood = location.monitoring.recent_flood.text
		def old_recent_flood = device.currentValue("recent_flood")
		def recent_flood_changed = new_recent_flood != old_recent_flood

        def new_freeze = location.monitoring.freeze.temp
		if (new_freeze != "?" || null) { 
        def old_freeze = device.currentValue("temperature")
        def freeze_changed = new_freeze != old_freeze }
		else { 
		log.debug "No freeze sensor data received - aborting" 
		new_freeze = "0"
		def old_freeze = "0"
		def freeze_changed = new_freeze != old_freeze } 

        def new_warnings = location.monitoring.warnings
        if (new_warnings == null) { new_warnings = "No Alert" }
		def old_warnings = device.currentValue("warnings")
		def warnings_changed = new_warnings != old_warnings


		def alarm_presence = ['off':'present', 'home':'present', 'away':'not present']
		def presence_state_changed = device.currentValue("presence") != alarm_presence.getAt(new_state)

		def alarm_armed = ['off':'unlocked', 'home':'locked', 'away':'locked']
		def armed_state_changed = device.currentValue("lock") != alarm_armed.getAt(new_state)

		log.debug "Alarm State: $new_state"
		log.debug "Alarm: $new_recent_alarm"
		log.debug "Fire: $new_recent_fire"
		log.debug "CO2: $new_recent_co"
		log.debug "Flood: $new_recent_flood"
		log.debug "Freeze: $new_freeze"
		log.debug "Warnings: $new_warnings"

		sendEvent(name: 'presence', value: alarm_presence.getAt(new_state), displayed: presence_state_changed, isStateChange: presence_state_changed)
		sendEvent(name: 'lock', value: alarm_armed.getAt(new_state), displayed: armed_state_changed, isStateChange: armed_state_changed)
		sendEvent(name: 'alarm', value: new_state, displayed: state_changed, isStateChange: state_changed)
		sendEvent(name: 'recent_alarm', value: new_recent_alarm, displayed: recent_alarm_changed, isStateChange: recent_alarm_changed)
		sendEvent(name: 'recent_fire', value: new_recent_fire, displayed: recent_fire_changed, isStateChange: recent_fire_changed)
		sendEvent(name: 'recent_co', value: new_recent_co, displayed: recent_co_changed, isStateChange: recent_co_changed)
		sendEvent(name: 'recent_flood', value: new_recent_flood, displayed: recent_flood_changed, isStateChange: recent_flood_changed)
		sendEvent(name: 'temperature', value: new_freeze, displayed: freeze_changed, isStateChange: freeze_changed)
		sendEvent(name: 'warnings', value: new_warnings, displayed: warnings_changed, isStateChange: warnings_changed)

	if (new_recent_alarm != "No Alert") { 
	sendEvent(name: 'contact', value: "open", displayed: recent_alarm_changed, isStateChange: recent_alarm_changed) }
		else {
	sendEvent(name: 'contact', value: "closed", displayed: recent_alarm_changed, isStateChange: recent_alarm_changed)
		}
	if (new_recent_fire != "No Alert") { 
	sendEvent(name: 'smoke', value: "detected", displayed: recent_fire_changed, isStateChange: recent_fire_changed) }
		else {
	sendEvent(name: 'smoke', value: "clear", displayed: recent_fire_changed, isStateChange: recent_fire_changed)
		}
	if (new_recent_co != "No Alert") { 
	sendEvent(name: 'carbonMonoxide', value: "detected", displayed: recent_co_changed, isStateChange: recent_co_changed) }
		else {
	sendEvent(name: 'carbonMonoxide', value: "clear", displayed: recent_co_changed, isStateChange: recent_co_changed)
		}
	if (new_recent_flood != "No Alert") { 
	sendEvent(name: 'water', value: "wet", displayed: recent_flood_changed, isStateChange: recent_flood_changed) }
		else {
	sendEvent(name: 'water', value: "dry", displayed: recent_flood_changed, isStateChange: recent_flood_changed)
		}
	if (new_freeze <= 41) { 
	sendEvent(name: 'freeze_status', value: "alert", displayed: freeze_changed, isStateChange: freeze_changed) }
		else {
	sendEvent(name: 'freeze_status', value: "no alert", displayed: freeze_changed, isStateChange: freeze_changed)
		}		
	if (new_warnings != "No Alert") { 
	sendEvent(name: 'acceleration', value: "active", displayed: warnings_changed, isStateChange: warnings_changed) }
		else {
	sendEvent(name: 'acceleration', value: "inactive", displayed: warnings_changed, isStateChange: warnings_changed)
		}
 }

	log.info "Executing 'events'" 
	api('events', []) { response ->
	//	log.trace "Events response $response.status $response.data"

		if (response.data.return_code < 1) {
			return
		} 
        
		def raw_event_desc = response.data.events.event_desc[0]
		if (raw_event_desc.find(/System Armed|System Disarmed/)) {
			def parsed_event_desc = raw_event_desc.findAll(/Armed|Disarmed|[(]\w*\s+\w+[)]/)
			parsed_event_desc = parsed_event_desc.join(' in ')
			state.parsed_event_desc = parsed_event_desc.replaceAll("[()]","") + " - "
		} else if (raw_event_desc.size() <=26 ) {
			state.parsed_event_desc = raw_event_desc + " - "
			} else {
			def parsed_event_desc = raw_event_desc.take(26) + "... "	
			state.parsed_event_desc = parsed_event_desc
		}
		
		def new_events = state.parsed_event_desc + response.data.events.event_time[0] + " " + response.data.events.event_date[0]
		def old_events = device.currentValue("events")
		def events_changed = new_events != old_events
      
		log.debug "Events: $new_events"
        
		sendEvent(name: 'events', value: new_events, displayed: events_changed, isStateChange: events_changed)       
 }        
 	// log out session	
	logout()
}

def api(method, args = [], success = {}) {
	log.info "Executing 'api'"

	if(!isLoggedIn()) {
		log.debug "Need to login"
		login(method, args, success)
		return
	}

	// SimpliSafe requires this funkiness
	def existing_args = args
	def required_payload = [
		no_persist: 0,
		XDEBUG_SESSION_START: 'session_name',
	]

	// append it to the args
	if (existing_args != [])
		{
		args = existing_args + required_payload
		} 
		else {
		args = required_payload
		}

	def methods = [
		'locations': [uri: "https://simplisafe.com/mobile/$state.auth.uid/locations", type: 'post'],
		'status': [uri: "https://simplisafe.com/mobile/$state.auth.uid/sid/$state.locationID/dashboard", type: 'post'],
		'events': [uri: "https://simplisafe.com/mobile/$state.auth.uid/sid/$state.locationID/events", type: 'post'],
		'get-state': [uri: "https://simplisafe.com/mobile/$state.auth.uid/sid/$state.locationID/get-state", type: 'post'],
		'set-state': [uri: "https://simplisafe.com/mobile/$state.auth.uid/sid/$state.locationID/set-state", type: 'post'],
		'update-freeze': [uri: "https://simplisafe.com/account2/$state.auth.uid/sid/$state.locationID/control-panel/utility/update-freeze", type: 'post'],
		'logout': [uri: "https://simplisafe.com/mobile/logout", type: 'post']
	]

	def request = methods.getAt(method)

	log.debug "Starting $method : $args"
	doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
	log.debug "Calling $type : $uri : $args"

	def params = [
		uri: uri,
		headers: [
			'Cookie': state.cookiess
		],
		body: args
	]

//	log.trace params

	try {
		if (type == 'post') {
			httpPost(params, success)
		} else if (type == 'get') {
			httpGet(params, success)
		}

	} catch (e) {
		log.debug "something went wrong: $e"
	}
}

def login(method = null, args = [], success = {}) { 
	log.info "Executing 'login'"
	def params = [
		uri: 'https://simplisafe.com/mobile/login',
		body: [
			name: settings.username, 
			pass: settings.password, 
			device_name: "SimpliSafe", 
			device_uuid: "SimpliSafe",
			version: 1200,
			no_persist: 1,
			XDEBUG_SESSION_START: 'session_name'
		]
	]

	state.cookiess = ''

	httpPost(params) {response ->
	//	log.trace "Login response, $response.status $response.data"
	//	log.trace response.headers

		state.auth = response.data

		// set the expiration to 10 minutes
		state.auth.expires_at = new Date().getTime() + 600000;

		response.getHeaders('Set-Cookie').each {
			String cookie = it.value.split(';|,')[0]
		//	log.trace "Adding cookie to collection: $cookie"
			state.cookiess = state.cookiess+cookie+';'
		}
	//	log.trace "cookies: $state.cookiess"

		// get location ID
		locations()

		api(method, args, success)

	}
}

def locations() {
	log.info "Executing 'locations'"

	api('locations', []) { response ->
	//	log.trace "Locations response $response.status $response.data"

	if (response.data.num_locations < 1) {
			return
		}

		def locations = response.data.locations
		state.locationID = locations.keySet()[0]
 }
}

def logout() { 
	log.info "Executing 'logout'"
	api('logout', []) { response ->
	//	log.trace "Logout response $response.status $response.data"
	}	
	state.auth = false		
}

def isLoggedIn() {
	if(!state.auth) {
		log.debug "No state.auth"
		return false
	}

//	log.trace state.auth.uid

	def now = new Date().getTime();
//	log.trace now
//	log.trace state.auth.expires_at
	return state.auth.expires_at > now
}