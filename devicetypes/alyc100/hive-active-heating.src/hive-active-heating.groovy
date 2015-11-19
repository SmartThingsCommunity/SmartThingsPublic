/**
 *  Hive Active Heating
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
 *
 * 	1. Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *     Name: Hive Active Heating
 *     Author: alyc100
 *     Capabilities:
 *         Polling
 *         Refresh
 *         Temperature Measurement
 *         Thermostat Mode
 *         Thermostat Operating State
 *		   Thermostat Heating Setpoint
 *
 *     Custom Commands:
 *         setThermostatMode
 *         setHeatingSetpoint
 *         heatingSetpointUp
 *         heatingSetpointDown
 *
 * 	2. Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: Hive Active Heating (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 	3. Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your your Hive user name, Hive password.
 *     To find your Receiver nickname, login to http://www.hivehome.com. Click on 'Manage Devices'.
 *     You should see 3 devices including the Hub, Thermostat and Receiver. It is the Receiver nickname you need.
 *
 * 	4. It should be done.
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
preferences {
	input("username", "text", title: "Username", description: "Your Hive username (usually an email address)")
	input("password", "password", title: "Password", description: "Your Hive password")
    input("receiver", "text", title: "Receiver", description: "Your receiver nickname")
} 
 
metadata {
	definition (name: "Hive Active Heating", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
		capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
        
        command "heatingSetpointUp"
		command "heatingSetpointDown"
        command "setThermostatMode"
        command "setHeatingSetpoint"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {

		multiAttributeTile(name: "Thermostat", width: 6, height: 4, type:"thermostat") {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL"){
				attributeState "default", label: '${currentValue}°C', backgroundColors: [
				// Celsius Color Range
				[value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 29, color: "#f1d801"],
                [value: 33, color: "#d04e00"],
                [value: 36, color: "#bc2323"]
			]}

			main "Thermostat"
			details "Thermostat"
		}

		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(5..32)") {
			state "setHeatingSetpoint", label:'Set temperature to', action:"setHeatingSetpoint"
		}
		//standardTile("heatingSetpointUp", "device.heatingSetpoint", width: 2, height: 2, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
		//	state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
		//}

		//standardTile("heatingSetpointDown", "device.heatingSetpoint", width: 2, height: 2, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
		//	state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
		//}

		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state "default", label:'${currentValue}°C', unit:"Heat", 
            backgroundColors:[
                [value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 29, color: "#f1d801"],
                [value: 33, color: "#d04e00"],
                [value: 36, color: "#bc2323"]
            ]
		}
   
		standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "idle", action:"polling.poll", label:'${name}', icon: "st.Home.home18"
			state "heating", action:"polling.poll", label:'${name}', icon: "st.Home.home1"
		}
        
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: false, decoration: "flat") {
			state "idle", action:"polling.poll", label:'${name}', icon: "st.sonos.pause-icon"
			state "heating", action:"polling.poll", label:'  ', icon: "st.thermostat.heating", backgroundColor:"#bc2323"
		}
        
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state("auto", action:"thermostat.off", icon: "st.thermostat.auto")
			state("off", action:"thermostat.cool", icon: "st.thermostat.heating-cooling-off")
			state("cool", action:"thermostat.heat", icon: "st.thermostat.cool")
			state("heat", action:"thermostat.auto", icon: "st.thermostat.heat")
		}

		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}

		main(["temperature", "thermostatOperatingState"])

		// ============================================================
		// Slider or Buttons...
		// To expose buttons, comment out the first detials line below and uncomment the second details line below.
		// To expose sliders, uncomment the first details line below and comment out the second details line below.

		//details(["heatingSetpointDown", "heatingSetpoint", "heatingSetpointUp", "thermostatMode", "thermostatOperatingState", "refresh"])
        details(["heatingSetpoint", "heatSliderControl", "thermostatMode", "thermostatOperatingState", "refresh"])
		
		// ============================================================

	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'temperature' attribute
	// TODO: handle 'heatingSetpoint' attribute
	// TODO: handle 'thermostatSetpoint' attribute
	// TODO: handle 'thermostatMode' attribute
	// TODO: handle 'thermostatOperatingState' attribute
}

// handle commands
def setHeatingSetpoint(temp) {
	def latestThermostatMode = device.latestState('thermostatMode')
    if (temp < 5) {
		temp = 5
	}
	if (temp > 32) {
		temp = 32
	}
   	// {"nodes":[{"attributes":{"targetHeatTemperature":{"targetValue":11}}}]}
    def args = [
        	nodes: [	[attributes: [targetHeatTemperature: [targetValue: temp]]]]
            ]
    
	api('temperature', args) {
        runIn(2, poll)
	}
	
}

def heatingSetpointUp(){
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heat set point down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def off() {
	setThermostatMode('off')
}

def heat() {
	setThermostatMode('heat')
}

def emergencyHeat() {
	setThermostatMode('heat')
}

def auto() {
	setThermostatMode('auto')
}

def setThermostatMode(mode) {
	mode = mode == 'emergency heat'? 'heat' : mode  
    def args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: false]]]]
            ]
    if (mode == 'off') {
     	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "OFF"]]]]
            ]
    } else if (mode == 'heat') {
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"HEAT"},"activeScheduleLock":{"targetValue":true}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: true]]]]
            ]
    } 
    
	api('thermostat_mode',  args) {
		mode = mode == 'range' ? 'auto' : mode
        runIn(2, poll)
	}
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	api('status', []) {
    	data.nodes = it.data.nodes
        
        // get temperature status
        def temperature = data.nodes.attributes.temperature.reportedValue[0]
        def heatingSetpoint = data.nodes.attributes.targetHeatTemperature.reportedValue[0]        
        
        sendEvent(name: 'temperature', value: temperature, unit: "C", state: "heat")
        sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: "C", state: "heat")
        
        // determine hive operating mode
        def activeHeatCoolMode = data.nodes.attributes.activeHeatCoolMode.reportedValue[0]
        def activeScheduleLock = data.nodes.attributes.activeScheduleLock.targetValue[0]
        
        log.debug "activeHeatCoolMode: $activeHeatCoolMode"
        log.debug "activeScheduleLock: $activeScheduleLock"
        
        def mode = 'auto'
        
        if (activeHeatCoolMode == "OFF") {
        	mode = 'off'
        }
        else if (activeHeatCoolMode == "HEAT" && activeScheduleLock) {
        	mode = 'heat'
        }
        sendEvent(name: 'thermostatMode', value: mode) 
        
        // determine if Hive heating relay is on
        def stateHeatingRelay = data.nodes.attributes.stateHeatingRelay.reportedValue[0]
        
        log.debug "stateHeatingRelay: $stateHeatingRelay"
        
        if (stateHeatingRelay == "ON") {
        	sendEvent(name: 'thermostatOperatingState', value: "heating")
        }       
        else {
        	sendEvent(name: 'thermostatOperatingState', value: "idle")
        }
    }
}

def refresh() {
	log.debug "Executing 'refresh'"
	// TODO: handle 'refresh' command
}

def api(method, args = [], success = {}) {
	log.debug "Executing 'api'"
	
	if(!isLoggedIn()) {
		log.debug "Need to login"
		login(method, args, success)
		return
	}
	log.debug "Using node id: $state.nodeid"
	def methods = [
		'status': [uri: "https://api.prod.bgchprod.info:443/omnia/nodes/${state.nodeid}", type: 'get'],
		'temperature': [uri: "https://api.prod.bgchprod.info:443/omnia/nodes/${state.nodeid}", type: 'put'],
        'thermostat_mode': [uri: "https://api.prod.bgchprod.info:443/omnia/nodes/${state.nodeid}", type: 'put']
	]
	
	def request = methods.getAt(method)
	
	log.debug "Starting $method : $args"
	doRequest(request.uri, args, request.type, success)
}

// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
	log.debug "Calling doRequest()"
	log.debug "Calling $type : $uri : $args"
	
	def params = [
		uri: uri,
        contentType: 'application/json',
		headers: [
        	  'Cookie': state.cookie,
              'Content-Type': 'application/vnd.alertme.zoo-6.2+json',
              'Accept': 'application/vnd.alertme.zoo-6.2+json',
              'Content-Type': 'application/*+json',
              'X-AlertMe-Client': 'smartthings',
              'X-Omnia-Access-Token': "${data.auth.sessions[0].id}"
        ],
		body: args
	]
	
	log.debug params
	
	def postRequest = { response ->
		success.call(response)
	}

	
		if (type == 'post') {
			httpPostJson(params, postRequest)
        } else if (type == 'put') {
        	httpPutJson(params, postRequest)
		} else if (type == 'get') {
			httpGet(params, postRequest)
		}
	
}

def getNodeId () {
	log.debug "Calling getNodeId()"
	//get thermostat node id
    log.debug "Using session id, $data.auth.sessions[0].id"
    def params = [
		uri: 'https://api.prod.bgchprod.info:443/omnia/nodes',
        contentType: 'application/json',
        headers: [
        	  'Cookie': state.cookie,
              'Content-Type': 'application/vnd.alertme.zoo-6.2+json',
              'Accept': 'application/vnd.alertme.zoo-6.2+json',
              'Content-Type': 'application/*+json',
              'X-AlertMe-Client': 'smartthings',
              'X-Omnia-Access-Token': "${data.auth.sessions[0].id}"
        ]
    ]
    
    state.nodeid = ''
	httpGet(params) {response ->
		log.debug "Request was successful, $response.status"
		log.debug response.headers
        
        response.data.nodes.each {
        	log.debug "node name $it.name"
        	if (it.name == settings.receiver)
            {            	
            	state.nodeid = it.id
            }
        }
        
		log.debug "nodeid: $state.nodeid"
    }
}

def login(method = null, args = [], success = {}) {
	log.debug "Calling login()"
	def params = [
		uri: 'https://api.prod.bgchprod.info:443/omnia/auth/sessions',
        contentType: 'application/json',
        headers: [
              'Content-Type': 'application/vnd.alertme.zoo-6.1+json',
              'Accept': 'application/vnd.alertme.zoo-6.2+json',
              'Content-Type': 'application/*+json',
              'X-AlertMe-Client': 'Smartthings Hive Device Type',
        ],
        body: [
        	sessions: [	[username: settings.username,
                 		password: settings.password,
                 		caller: 'smartthings']]
        ]
    ]

	state.cookie = ''
	
	httpPostJson(params) {response ->
		log.debug "Request was successful, $response.status"
		log.debug response.headers
		data.auth = response.data
		
		// set the expiration to 5 minutes
		data.auth.expires_at = new Date().getTime() + 300000;
		
        state.cookie = response?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
		log.debug "Adding cookie to collection: $cookie"
        log.debug "auth: $data.auth"
		log.debug "cookie: $state.cookie"
        log.debug "sessionid: $data.auth.sessions[0].id"
        
        getNodeId()
		
		api(method, args, success)

	}
}

def isLoggedIn() {
	log.debug "Calling isLoggedIn()"
	log.debug "isLoggedIn state $data.auth"
	if(!data.auth) {
		log.debug "No data.auth"
		return false
	}

	def now = new Date().getTime();
    return data.auth.expires_at > now
}