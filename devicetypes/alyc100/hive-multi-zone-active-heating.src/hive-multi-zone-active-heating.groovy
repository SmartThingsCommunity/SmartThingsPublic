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
 *		   Thermostat
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
 *
 *	4. ANDROID USERS - You have to comment out the iOS details line at line 205 by adding "//" 
 * 	   and uncomment the Android details line by removing the preceding "//" at line 213 before publishing.
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
 *
 *	VERSION HISTORY
 *  19.11.2015
 *	v1.0 - Initial Release
 *	v1.1 - Added function buttons to set Hive Heating to Off, Manual or Schedule.
 *	v1.2 - Removed requirement to type in Receiver Nickname from Hive.
 *	v1.3 - Altered temperature colours to match Hive branding (I was bored).
 *	v1.4 - Enable options for sliders or buttons for temperature control.
 *
 *	20.11.2015
 *	v1.5 - Clean up UI and make user friendly status message appear on top panel
 *  v1.6 - Added icons to control buttons. Increased poll delay time to ensure UI is updated on control press.
 *
 *	21.11.2015
 *  v1.7 - Fixed issue where 'supportsHeatCoolModes' attribute does not exist.
 *	v1.8 - Changed behaviour when temperature is set when Hive Heating is in off mode to match Hive app behaviour.
 *	v1.9 - Added new Android tile layout option. Requires uncommenting/commenting out lines. Updated behaviour when Hive Heating is in off mode. Altered temperatue precision.
 *	v1.9.1 - Tweaks to Android layout.
 *  v1.9.2 - Tweaks to how set heating point temperature is reported in frost mode.
 *  v1.9.3 - Improvements to handling temperature setting when in 'off' mode.
 *  v1.10 - Added Boost button!! Reduced number of activity messages.
 *	v1.10.1 - Tweaks to temperature formatting.
 *	v1.10.2 - Added icons to thermostat mode states
 *	v1.10.3 - Tweaks to display on Things screen.
 *
 *	23.11.2015
 *	v1.10.4 - Set thermostatFanMode to 'off' to improve SmartTiles display
 *
 *	01.12.2015
 *	v1.10.5 - Handle event of thermostat being set to 'cool'. Changed API client name.
 *
 *	04.01.2016
 *	v1.11 - Support for multi zone systems with new thermostat name attribute. Changes to multi attribute tile to attempt to unify android UI with iOS.
 */
preferences {
	input("username", "text", title: "Username", description: "Your Hive username (usually an email address)")
	input("password", "password", title: "Password", description: "Your Hive password")
    input("thermostat", "thermostat", title: "Thermostat Name", description: "Multi Zone Hive Systems Only. The name of the thermostat controlling the zone (i.e, Ground Floor)")
} 
 
metadata {
	definition (name: "Hive Multi Zone Active Heating", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Temperature Measurement"
        capability "Thermostat"
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

		multiAttributeTile(name: "thermostat", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeBackground: true){
				attributeState "default", label: '${currentValue}°', unit:"C", backgroundColors: [
				// Celsius Color Range
				[value: 0, color: "#50b5dd"],
                [value: 10, color: "#43a575"],
                [value: 13, color: "#c5d11b"],
                [value: 17, color: "#f4961a"],
                [value: 20, color: "#e75928"],
                [value: 25, color: "#d9372b"],
                [value: 29, color: "#b9203b"]
			]}
            tileAttribute ("hiveHeating", key: "SECONDARY_CONTROL") {
				attributeState "hiveHeating", label:'${currentValue}'
			}
		}
        
        valueTile("thermostat_small", "device.temperature", width: 4, height: 4) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
                [value: 0, color: "#50b5dd"],
                [value: 10, color: "#43a575"],
                [value: 13, color: "#c5d11b"],
                [value: 17, color: "#f4961a"],
                [value: 20, color: "#e75928"],
                [value: 25, color: "#d9372b"],
                [value: 29, color: "#b9203b"]
            ]
		}
        
        standardTile("thermostat_main", "device.thermostatOperatingState", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state "idle", label:'${currentValue}', icon: "st.Weather.weather2"
			state "heating", label:'${currentValue}', icon: "st.Weather.weather2", backgroundColor:"#EC6E05"
		}
        
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 2, width: 4, inactiveLabel: false, range:"(5..32)") {
			state "setHeatingSetpoint", label:'Set temperature to', action:"setHeatingSetpoint"
		}
        
		standardTile("heatingSetpointUp", "device.heatingSetpoint", width: 2, height: 2, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
		}

		standardTile("heatingSetpointDown", "device.heatingSetpoint", width: 2, height: 2, canChangeIcon: false, inactiveLabel: false, decoration: "flat") {
			state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
		}

		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2) {
			state "default", label:'${currentValue}°', unit:"C",
            backgroundColors:[
                [value: 0, color: "#50b5dd"],
                [value: 10, color: "#43a575"],
                [value: 13, color: "#c5d11b"],
                [value: 17, color: "#f4961a"],
                [value: 20, color: "#e75928"],
                [value: 25, color: "#d9372b"],
                [value: 29, color: "#b9203b"]
            ]
		}
   
        standardTile("thermostatOperatingState", "device.thermostatOperatingState", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state "idle", action:"polling.poll", label:'${name}', icon: "st.sonos.pause-icon"
			state "heating", action:"polling.poll", label:'  ', icon: "st.thermostat.heating", backgroundColor:"#EC6E05"
		}
        
        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: true, decoration: "flat", width: 2, height: 2) {
			state("auto", label: "SCHEDULED", icon:"st.Office.office7")
			state("off", label: "OFF", icon:"st.thermostat.heating-cooling-off")
			state("heat", label: "MANUAL", icon:"st.Weather.weather2")
			state("emergency heat", label: "BOOST", icon:"st.Health & Wellness.health7")
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}
        
        valueTile("boost", "device.boostLabel", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'${currentValue}', action:"emergencyHeat")
		}
        
        standardTile("mode_auto", "device.mode_auto", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"auto", label:'Schedule', icon:"st.Office.office7"
    	}
        
        standardTile("mode_manual", "device.mode_manual", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"heat", label:'Manual', icon:"st.Weather.weather2"
   	 	}
        
        standardTile("mode_off", "device.mode_off", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"off", icon:"st.thermostat.heating-cooling-off"
   	 	}

		main(["thermostat_main"])

		// ============================================================
		// iOS TILES
		// To expose iOS optimised tiles, comment out the details line in Android Tiles section and uncomment details line below.
		
		details(["thermostat", "mode_auto", "mode_manual", "mode_off", "heatingSetpoint", "heatSliderControl", "boost", "refresh"])
		
		// ============================================================

		// ============================================================
		// ANDROID TILES
		// To expose Android optimised tiles, comment out the details line in iOS Tiles section and uncomment details line below.
		
		//details(["thermostat_small", "thermostatOperatingState", "thermostatMode", "mode_auto", "mode_manual", "mode_off", "heatingSetpoint", "heatSliderControl", "boost", "refresh"])
		
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
	log.debug "Executing 'setHeatingSetpoint with temp $temp'"
	def latestThermostatMode = device.latestState('thermostatMode')
    
    if (temp < 5) {
		temp = 5
	}
	if (temp > 32) {
		temp = 32
	}
    
	
    //if thermostat is off, set to manual    
   	if (latestThermostatMode.stringValue == 'off') {
    	def args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: true]]]]
            ]
		api('thermostat_mode',  args) {
		}    	
    }
    
    // {"nodes":[{"attributes":{"targetHeatTemperature":{"targetValue":11}}}]}    
    def args = [
        	nodes: [	[attributes: [targetHeatTemperature: [targetValue: temp]]]]
            ]               
	api('temperature', args) {        
        runIn(4, poll)   
	}	
}

def heatingSetpointUp(){
	log.debug "Executing 'heatingSetpointUp'"
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heat set point up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	log.debug "Executing 'heatingSetpointDown'"
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
	log.debug "Executing 'boost'"
	
    def latestThermostatMode = device.latestState('thermostatMode')
    
    //Don't do if already in BOOST mode.
	if (latestThermostatMode.stringValue != 'emergency heat') {
		setThermostatMode('emergency heat')
    }
    else {
    	log.debug "Already in boost mode."
    }

}

def auto() {
	setThermostatMode('auto')
}

def setThermostatMode(mode) {
	mode = mode == 'cool' ? 'heat' : mode
	log.debug "Executing 'setThermostatMode with mode $mode'"
    def args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], scheduleLockDuration: [targetValue: 0], activeScheduleLock: [targetValue: false]]]]
            ]
    if (mode == 'off') {
     	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "OFF"], scheduleLockDuration: [targetValue: 0], activeScheduleLock: [targetValue: true]]]]
            ]
    } else if (mode == 'heat') {
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"HEAT"},"activeScheduleLock":{"targetValue":true}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], scheduleLockDuration: [targetValue: 0], activeScheduleLock: [targetValue: true]]]]
            ]
    } else if (mode == 'emergency heat') {
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"BOOST"},"scheduleLockDuration":{"targetValue":30},"targetHeatTemperature":{"targetValue":22}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "BOOST"], scheduleLockDuration: [targetValue: 60], targetHeatTemperature: [targetValue: "21"]]]]
            ]
    }
    
	api('thermostat_mode',  args) {
		mode = mode == 'range' ? 'auto' : mode
        runIn(4, poll)
	}
}

def poll() {
	log.debug "Executing 'poll'"
	api('status', []) {
    	data.nodes = it.data.nodes
        
        //Construct status message
        def statusMsg = "Currently"
        
        //Boost button label
    	def boostLabel = "Start\nBoost"
        
        // get temperature status
        def temperature = data.nodes.attributes.temperature.reportedValue[0]
        def heatingSetpoint = data.nodes.attributes.targetHeatTemperature.reportedValue[0]
        temperature = String.format("%2.1f",temperature)
       	heatingSetpoint = convertTemperatureIfNeeded(heatingSetpoint, "C", 1)
        
        // convert temperature reading of 1 degree to 7 as Hive app does
        if (heatingSetpoint == "1.0") {
        	heatingSetpoint = "7.0"
        }
        
        sendEvent(name: 'temperature', value: temperature, unit: "C", state: "heat")
        sendEvent(name: 'heatingSetpoint', value: heatingSetpoint, unit: "C", state: "heat")
        sendEvent(name: 'thermostatSetpoint', value: heatingSetpoint, unit: "C", state: "heat", displayed: false)
        sendEvent(name: 'thermostatFanMode', value: "off", displayed: false)
        
        // determine hive operating mode
        def activeHeatCoolMode = data.nodes.attributes.activeHeatCoolMode.reportedValue[0]
        def activeScheduleLock = data.nodes.attributes.activeScheduleLock.targetValue[0]
        
        log.debug "activeHeatCoolMode: $activeHeatCoolMode"
        log.debug "activeScheduleLock: $activeScheduleLock"
        
        def mode = 'auto'
        
        if (activeHeatCoolMode == "OFF") {
        	mode = 'off'
            statusMsg = statusMsg + " set to OFF"
        }
        else if (activeHeatCoolMode == "BOOST") {
        	mode = 'emergency heat'
            statusMsg = statusMsg + " set to BOOST"
            def boostTime = data.nodes.attributes.scheduleLockDuration.reportedValue[0]
            boostLabel = "Boosting for \n" + boostTime + " mins"
            sendEvent("name":"boostTimeRemaining", "value": boostTime + " mins")
        }
        else if (activeHeatCoolMode == "HEAT" && activeScheduleLock) {
        	mode = 'heat'
            statusMsg = statusMsg + " set to MANUAL"
        }
        else {
        	statusMsg = statusMsg + " set to SCHEDULE"
        }
        sendEvent(name: 'thermostatMode', value: mode) 
        
        // determine if Hive heating relay is on
        def stateHeatingRelay = data.nodes.attributes.stateHeatingRelay.reportedValue[0]
        
        log.debug "stateHeatingRelay: $stateHeatingRelay"
        
        if (stateHeatingRelay == "ON") {
        	sendEvent(name: 'thermostatOperatingState', value: "heating")
            statusMsg = statusMsg + " and is HEATING"
        }       
        else {
        	sendEvent(name: 'thermostatOperatingState', value: "idle")
            statusMsg = statusMsg + " and is IDLE"
        }  
               
        sendEvent("name":"hiveHeating", "value": statusMsg, displayed: false)  
        sendEvent("name":"boostLabel", "value": boostLabel, displayed: false)
    }
}

def refresh() {
	log.debug "Executing 'refresh'"
	poll()
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
              'X-AlertMe-Client': 'Hive Web Dashboard',
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

def getThermostatId() {
	log.debug "Calling getThermostatId()"
	//get parent thermostat node id
    def params = [
		uri: 'https://api.prod.bgchprod.info:443/omnia/nodes',
        contentType: 'application/json',
        headers: [
        	  'Cookie': state.cookie,
              'Content-Type': 'application/vnd.alertme.zoo-6.2+json',
              'Accept': 'application/vnd.alertme.zoo-6.2+json',
              'Content-Type': 'application/*+json',
              'X-AlertMe-Client': 'Hive Web Dashboard',
              'X-Omnia-Access-Token': "${data.auth.sessions[0].id}"
        ]
    ]
    
    httpGet(params) {response ->
		log.debug "Request was successful, $response.status"
		log.debug response.headers
        
        response.data.nodes.each {
        	log.debug "node name $it.name"           
        	if (it.name == settings.thermostat)
            {   
            	state.parentNodeId = it.id
            }
        }
        
        if (state.parentNodeId == '')
        {
        	log.error "No thermostat found with name $settings.thermostat. Please check settings. Attempting to use default thermostat."
        }
        
		log.debug "parentNodeId: $state.parentNodeId"
    }
}

def getNodeId () {
	log.debug "Calling getNodeId()"
	//get node id
    log.debug "Using session id, $data.auth.sessions[0].id"
    def params = [
		uri: 'https://api.prod.bgchprod.info:443/omnia/nodes',
        contentType: 'application/json',
        headers: [
        	  'Cookie': state.cookie,
              'Content-Type': 'application/vnd.alertme.zoo-6.2+json',
              'Accept': 'application/vnd.alertme.zoo-6.2+json',
              'Content-Type': 'application/*+json',
              'X-AlertMe-Client': 'Hive Web Dashboard',
              'X-Omnia-Access-Token': "${data.auth.sessions[0].id}"
        ]
    ]
    
    state.nodeid = ''
	httpGet(params) {response ->
		log.debug "Request was successful, $response.status"
		log.debug response.headers
        
        response.data.nodes.each {
        	if (((state.parentNodeId == '') || (it.parentNodeId == state.parentNodeId)) && (it.attributes.supportsHotWater != null) && (it.attributes.supportsHotWater.reportedValue == false))
            {   
            	state.nodeid = it.id
            }
        }
        
        if (state.nodeid == '')
        {
        	log.error "No node found to create device type with. Please check settings."
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
              'X-AlertMe-Client': 'Hive Web Dashboard',
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
		data.auth.expires_at = new Date().getTime() + 10000;
        
        state.cookie = response?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
		log.debug "Adding cookie to collection: $cookie"
        log.debug "auth: $data.auth"
		log.debug "cookie: $state.cookie"
        log.debug "sessionid: $data.auth.sessions[0].id"
        
        state.parentNodeId = ''
        
        //Get thermostat id for multi zone systems.
        if (settings.thermostat != null && settings.thermostat != '')
        {
        	getThermostatId()
        }
        
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