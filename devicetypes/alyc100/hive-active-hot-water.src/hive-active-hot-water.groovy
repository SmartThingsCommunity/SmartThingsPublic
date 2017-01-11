/**
 *  Hive Active Hot Water
 *
 *  Copyright 2015 Alex Lee Yuk Cheung
 *
 * 	1. Create a new device type (https://graph.api.smartthings.com/ide/devices)
 *     Name: Hive Active Heating
 *     Author: alyc100
 *     Capabilities:
 *         Polling
 *         Refresh
 *		   Thermostat
 *         Thermostat Mode
 *
 *     Custom Commands:
 *         setThermostatMode
 *
 * 	2. Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: Hive Active Hot Water (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * 	3. Update device preferences
 *     Click on the new device to see the details.
 *     Click the edit button next to Preferences
 *     Fill in your your Hive user name, Hive password.
 *
 *	4. ANDROID USERS - You have to comment out the iOS details line at line 130 by adding "//" 
 * 	   and uncomment the Android details line by removing the preceding "//" at line 138 before publishing.
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
 *	VERSION HISTORY
 *  20.11.2015
 *	v1.0 - Initial Release - There seems to be an issue on the Hive side where the Hot Water Relay status is being reported back incorrectly sometimes.
 *	v1.0.1 - Minor tweaks to improve support for thermostat capability
 *	
 *	22.11.2015
 *	v1.1 - Implemented Boost functionality! Added optimised Android tile layout.
 *	v1.1.1 - Improvements to display of Things screen.
 *	v1.1.2 - Change wording from 'Manual' to 'On' to match Hive app.
 *			 Fixes to 'On' mode. 
 *            Changes to display on Things to match Hive Heating.
 *
 *	01.12.2015
 *	v1.1.3 - Handle 'cool' mode. Change API client id.
 *
 *	05.01.2016
 *	v1.1.4 - Removed the need for Pollster. Unify Android tile UI to match iOS.
 *	v1.1.5 - Improved scheduler reliability without Pollster.
 *	v1.2 - Added option to adjust boost length.
 */
preferences {
	input("username", "text", title: "Username", description: "Your Hive username (usually an email address)")
	input("password", "password", title: "Password", description: "Your Hive password")
}

metadata {
	definition (name: "Hive Active Hot Water", namespace: "alyc100", author: "Alex Lee Yuk Cheung") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
        capability "Thermostat"
		capability "Thermostat Mode"
        
        command "setThermostatMode"
        command "setBoostLength"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {

		multiAttributeTile(name: "hotWaterRelay", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.thermostatOperatingState", key:"PRIMARY_CONTROL"){
				attributeState "heating", icon: "st.thermostat.heat", backgroundColor: "#EC6E05"
  				attributeState "idle", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#ffffff"
            }
            tileAttribute ("hiveHotWater", key: "SECONDARY_CONTROL") {
				attributeState "hiveHotWater", label:'${currentValue}'
			}
		}
        
        standardTile("hotWaterRelay_main", "device.thermostatOperatingState", inactiveLabel: true, width: 3, height: 3) {
			state( "heating", label:'${currentValue}', icon: "st.Bath.bath6", backgroundColor: "#EC6E05")
  			state( "idle", label:'${currentValue}', icon: "st.Bath.bath6", backgroundColor: "#ffffff")
		}
        
        standardTile("hotWaterRelay_small", "device.thermostatOperatingState", inactiveLabel: true, width: 3, height: 3) {
			state( "heating", icon: "st.thermostat.heat", backgroundColor: "#EC6E05")
  			state( "idle", icon: "st.thermostat.heating-cooling-off", backgroundColor: "#ffffff")
		}

        standardTile("thermostatMode", "device.thermostatMode", inactiveLabel: false, decoration: "flat", width: 3, height: 3) {
			state("auto", label: "SCHEDULED", action:"heat", icon:"st.Bath.bath6")
			state("off", label: "OFF", action:"auto", icon:"st.Bath.bath6")
			state("heat", label: "ON", action:"off", icon:"st.Bath.bath6")
			state("emergency heat", label: "BOOST", action:"auto", icon:"st.Bath.bath6")
		}
        

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'refresh', action:"polling.poll", icon:"st.secondary.refresh-icon")
		}
        
        valueTile("boost", "device.boostLabel", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state("default", label:'${currentValue}', action:"emergencyHeat")
		}
        
        controlTile("boostSliderControl", "device.boostLength", "slider", height: 2, width: 4, inactiveLabel: false, range:"(10..240)") {
			state "setBoostLength", label:'Set boost length to', action:"setBoostLength"
		}
        
        standardTile("mode_auto", "device.mode_auto", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"auto", label:'Schedule', icon:"st.Office.office7"
    	}
        
        standardTile("mode_manual", "device.mode_manual", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"heat", label:'On', icon:"st.Weather.weather2"
   	 	}
        
        standardTile("mode_off", "device.mode_off", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
        	state "default", action:"off", icon:"st.thermostat.heating-cooling-off"
   	 	}

		main(["hotWaterRelay_main"])	
		details(["hotWaterRelay", "mode_auto", "mode_manual", "mode_off", "boost", "boostSliderControl", "refresh"])

	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute
	// TODO: handle 'thermostatMode' attribute

}

def installed() {
	log.debug "Executing 'installed'"
    state.boostLength = 60
	// execute handlerMethod every 10 minutes.
    schedule("0 0/10 * * * ?", poll)
}

def updated() {
	log.debug "Executing 'updated'"
	// execute handlerMethod every 10 minutes.
    unschedule(poll)
    schedule("0 0/10 * * * ?", poll)
}

def uninstalled() {
	log.debug "Executing 'unsinstalled'"
	unschedule(poll)
}

// handle commands
def setHeatingSetpoint(temp) {
	//Not implemented	
}

def setBoostLength(minutes) {
	log.debug "Executing 'setBoostLength with length $minutes minutes'"
    if (minutes < 10) {
		minutes = 10
	}
	if (minutes > 240) {
		minutes = 240
	}
    state.boostLength = minutes
    sendEvent("name":"boostLength", "value": state.boostLength, displayed: true)
    
    def latestThermostatMode = device.latestState('thermostatMode')
    
    //If already in BOOST mode, send updated boost length to Hive.
	if (latestThermostatMode.stringValue == 'emergency heat') {
		setThermostatMode('emergency heat')
    }
    else {
    	refresh()
    }
}

def heatingSetpointUp(){
	//Not implemented
}

def heatingSetpointDown(){
	//Not implemented
}

def on() {
	log.debug "Executing 'on'"
	setThermostatMode('heat')
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
    def args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: false]]]]
            ]
    if (mode == 'off') {
     	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "OFF"]]]]
            ]
    } else if (mode == 'heat') {
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"HEAT"},"activeScheduleLock":{"targetValue":true},"targetHeatTemperature":{"targetValue":99}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "HEAT"], activeScheduleLock: [targetValue: true], targetHeatTemperature: [targetValue: "99"]]]]
            ]
    } else if (mode == 'emergency heat') {
    	if (state.boostLength == null || state.boostLength == '')
        {
        	state.boostLength = 60
            sendEvent("name":"boostLength", "value": 60, displayed: true)
        }
    	//{"nodes":[{"attributes":{"activeHeatCoolMode":{"targetValue":"BOOST"},"scheduleLockDuration":{"targetValue":30},"targetHeatTemperature":{"targetValue":99}}}]}
    	args = [
        	nodes: [	[attributes: [activeHeatCoolMode: [targetValue: "BOOST"], scheduleLockDuration: [targetValue: state.boostLength], targetHeatTemperature: [targetValue: "99"]]]]
            ]
    }
    
	api('thermostat_mode',  args) {
		mode = mode == 'range' ? 'auto' : mode
        runIn(3, refresh)
	}
}

def poll() {
log.debug "Executing 'poll'"
	api('status', []) {
    	data.nodes = it.data.nodes
        
        //Construct status message
        def statusMsg = "Currently"
        
        //Boost button label
        if (state.boostLength == null || state.boostLength == '')
        {
        	state.boostLength = 60
            sendEvent("name":"boostLength", "value": 60, displayed: true)
        }
    	def boostLabel = "Start\n$state.boostLength Min Boost"
        
        // determine hive hot water operating mode
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
            statusMsg = statusMsg + " set to ON"
        }
        else {
        	statusMsg = statusMsg + " set to SCHEDULE"
        }
        
        sendEvent(name: 'thermostatMode', value: mode) 
        
        // determine if Hive hot water relay is on
        def stateHotWaterRelay = data.nodes.attributes.stateHotWaterRelay.reportedValue[0]
        
        log.debug "stateHotWaterRelay: $stateHotWaterRelay"
        
        if (stateHotWaterRelay == "ON") {
        	sendEvent(name: 'temperature', value: 99, unit: "C", state: "heat", displayed: false)
        	sendEvent(name: 'heatingSetpoint', value: 99, unit: "C", state: "heat", displayed: false)
            sendEvent(name: 'thermostatOperatingState', value: "heating")
            statusMsg = statusMsg + " and is HEATING"
        }       
        else {
        	sendEvent(name: 'temperature', value: 0, unit: "C", state: "heat", displayed: false)
       	 	sendEvent(name: 'heatingSetpoint', value: 0, unit: "C", state: "heat", displayed: false)
            sendEvent(name: 'thermostatOperatingState', value: "idle")
            statusMsg = statusMsg + " and is IDLE"
        }
        sendEvent("name":"hiveHotWater", "value":statusMsg, displayed: false)
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
              'X-AlertMe-Client': 'Hive Web Dashboard',
              'X-Omnia-Access-Token': "${data.auth.sessions[0].id}"
        ]
    ]
    
    state.nodeid = ''
	httpGet(params) {response ->
		log.debug "Request was successful, $response.status"
		log.debug response.headers
        
        response.data.nodes.each {
        	log.debug "node name $it.name"           
        	if ((it.attributes.supportsHotWater != null) && (it.attributes.supportsHotWater.reportedValue == true))
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

