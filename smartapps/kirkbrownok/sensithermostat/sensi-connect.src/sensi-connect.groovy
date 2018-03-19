/**
 *  Copyright 2016 Kirk Brown
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
 *	Emerson Sensi Community Created Service Manager
 *
 *	Author: Kirk Brown
 *	Date: 2016-12-24
 *  Date: 2017-01-02  The Sensi Connect App is near fully functional
 *  Date: 2017-01-07  The Sensi Connect App has been changed to allow individual device polling. 
 *					  It also polls a thermostat immediately after sending a command
 *
 *  Date: 2017-05-02  Changed the frequency of subscription/unsubscribe. Any time a new poll or before command subscribe new.
 *
 *	Place the Sensi (Connect) code under the My SmartApps section. Be certain you publish the app for you.
 *  Place the Sensi Thermostat Device Type Handler under My Device Handlers section.
 *  Be careful that if you change the Name and Namespace you that additionally change it in the addChildDevice() function
 *
 *
 *  The Program Flow is as follows:
 *  1.	SmartApp gets user credentials in the install process.
 *  2.	The SmartApp gets the user’s thermostats and lists them for subscription in the SmartApp.
 *  	a.	The smartApp uses the user’s credentials to get authorized, get a connection token, and then list the thermostats
 *  3.	The User then selects the desired thermostats to add to SmartThings
 *  4.	The SmartApp schedules a refresh/poll of the thermostats every so often. Default is 5 minutes for now but can be changed in the install configurations. The interface is not official, so polling to often could get noticed.
 *  XXXXXX Not true any more -> 5.	If any thermostat device is refreshed, then they all get polled from the Sensi API. YOU SHOULD NOT add polling to devices ie don’t use pollster for more than 1 thermostat device -> if you do then all devices will get updated each time.
 *	6. The devices can be polled by a pollster type SmartApp now and update seperately. However, all of them are still polled at the interval chosen during setup.
 * There are a large number of debug statements that will turn on if you uncomment the statement inside the TRACE function at the bottom of the code
 */
 
definition(
		name: "Sensi (Connect)",
		namespace: "kirkbrownOK/SensiThermostat",
		author: "Kirk Brown",
		description: "Connect your Sensi thermostats to SmartThings.",
		category: "SmartThings Labs",
		iconUrl: "http://i.imgur.com/QVbsCpu.jpg",
		iconX2Url: "http://i.imgur.com/4BfQn6I.jpg",
		singleInstance: true
)

preferences {
	page(name: "auth", title: "Sensi", nextPage:"", content:"authPage", uninstall: true)
	page(name: "getDevicesPage", title: "Sensi Devices", nextPage:"", content:"getDevicesPage", uninstall: true, install: true)
}

def authPage() {

	def description
	def uninstallAllowed = false
	if(state.connectionToken) {
		description = "You are connected."
		uninstallAllowed = true
	} else {
		description = "Click to enter Sensi Credentials"
	}

		return dynamicPage(name: "auth", title: "Login", nextPage: "getDevicesPage", uninstall:uninstallAllowed) {
			section() {
				paragraph "Enter your Username and Password for Sensi Connect. Your username and password will be saved in SmartThings in whatever secure/insecure manner SmartThings saves them."
				input("userName", "string", title:"Sensi Email Address", required:true, displayDuringSetup: true)
    			input("userPassword", "password", title:"Sensi account password", required:true, displayDuringSetup:true)	
                input("pollInput", "number", title: "How often should ST poll Sensi Thermostat? (minutes)", required: false, displayDureingSetup: true)
			}
		}

}
def getDevicesPage() {
    getConnected()
         
    def stats = getSensiThermostats()
    return dynamicPage(name: "getDevicesPage", title: "Select Your Thermostats", uninstall: true) {
        section("") {
            paragraph "Tap below to see the list of sensi thermostats available in your sensi account and select the ones you want to connect to SmartThings."
            input(name: "thermostats", title:"", type: "enum", required:false, multiple:true, description: "Tap to choose", metadata:[values:stats])
            
        }
    }
}


def getThermostatDisplayName(stat) {
    if(stat?.DeviceName) {
        return stat.DeviceName.toString()
    }
    return "Unknown"
}

def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.info "Updated with settings: ${settings}"
	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {

    getAuthorized()
    getToken()
    
	def devices = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
        	TRACE( "addChildDevice($app.namespace, ${getChildName()}, $dni, null, [\"label\":\"${state.thermostats[dni]}\" : \"Sensi Thermostat\"])")
			d = addChildDevice(app.namespace, getChildName(), dni, null, ["label":"${state.thermostats[dni]}" ?: "Sensi Thermostat"])
			log.info "created ${d.displayName} with id $dni"
		} else {
			log.info "found ${d.displayName} with id $dni already exists"
		}
		return d
	}

	TRACE( "created ${devices.size()} thermostats.")

	def delete  // Delete any that are no longer in settings
	if(!thermostats) {
		log.info "delete thermostats ands sensors"
		delete = getAllChildDevices() //inherits from SmartApp (data-management)
	} else { //delete only thermostat
		log.info "delete individual thermostat"
		delete = getChildDevices().findAll { !thermostats.contains(it.deviceNetworkId) }		
	}
	log.warn "delete: ${delete}, deleting ${delete.size()} thermostats"
	delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

	//send activity feeds to tell that device is connected
	def notificationMessage = "is connected to SmartThings"
	sendActivityFeeds(notificationMessage)
	state.timeSendPush = null
	state.reAttempt = 0

	try{
		poll() //first time polling data data from thermostat
	} catch (e) {
    	log.warn "Error in first time polling. Could mean something is wrong."
    }
	//automatically update devices status every 5 mins
    def pollRate = pollInput == null ? 5 : pollInput
    if(pollRate > 59 || pollRate < 1) {
    	pollRate = 5
        log.warn "You picked an invalid pollRate: $pollInput minutes. Changed to 5 minutes."
    }    
	schedule("0 0/${pollRate} * * * ?","poll")
    

}

def getAuthorized() {
    def bodyParams = [ Password: "${userPassword}", UserName: "${userName}" ]
    state.RBCounter = 2
    state.sendCounter= 0
    state.GroupsToken = null
	def deviceListParams = [
		uri: getApiEndpoint(),
		path: "/api/authorize",
		headers: ["Content-Type": "application/json", "Accept": "application/json; version=1, */*; q=0.01", "X-Requested-With":"XMLHttpRequest"],
		body: [ Password: userPassword, UserName: userName ]
	]
	try {
		httpPostJson(deviceListParams) { resp ->
        	//log.debug "Resp Headers: ${resp.headers}"
        	
			if (resp.status == 200) {
				resp.headers.each {
            		//log.debug "${it.name} : ${it.value}"
                    if (it.name == "Set-Cookie") {
                    	//log.debug "Its SETCOOKIE ${it.value}"
                        //state.myCookie = it.value
                        def tempC = it.value.split(";")
                        tempC = tempC[0].trim()
                        if(tempC == state.myCookie) {
                        	//log.debug "Cookie didn't change"
                        } else {
                        	state.myCookie = tempC
                        	//log.debug "My Cookie: ${state.myCookie}"
                        }
                    }
        		}
			} else {
				TRACE( "http status: ${resp.status}")
			}
		}
	} catch (e) {
        log.warn "Exception trying to authenticate $e"
    }	

}

def getToken() {
	//log.debug "GetToken"
    def params = [
        uri: getApiEndpoint(),
    	path: '/realtime/negotiate',
        requestContentType: 'application/json',
        contentType: 'application/json',
        headers: [
        	'Cookie':state.myCookie,
            'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip'
            ]
	]
    try {
        httpGet(params) { resp ->
            state.connectionToken = resp.data.ConnectionToken
            state.connectionId = resp.data.ConnectionId
        }
    } catch (e) {
        log.error "Connection Token error $e"
    }

}
def getConnected() {
	getAuthorized()
    getToken()
	log.info "GetConnected"
    
    def params = [
    	
        uri: getApiEndpoint(),
    	path: '/realtime/connect',
        query: [
        	transport:'longPolling',
            connectionToken:state.connectionToken,
        	connectionData:"[{\"name\": \"thermostat-v1\"}]",
            connectionId:state.connectionId,
            tid:state.RBCounter,"_":now()
            ],
        contentType: 'application/json',
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip']
	]
    try {
        httpGet(params) { resp ->
            if(resp.data.C) {
            	state.messageId= resp.data.C
            	//log.debug "MessageID: ${state.messageId}"
            }    
            state.connected = true
            state.RBCounter = state.RBCounter + 1
            state.lastSubscribedDNI = null
        }
    } catch (e) {
        log.error "Get Connected went wrong: $e"
        state.connected = false
    }    
}
def getSensiThermostats() {
	TRACE("getting device list")
	state.sensiSensors = []
	def deviceListParams = [
		uri: apiEndpoint,
		path: "/api/thermostats",
        requestContentType: 'application/json',
        contentType: 'application/json',
		headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip']        
	]
	//log.debug "Get Stats: ${deviceListParams}"
	def stats = [:]
	try {
		httpGet(deviceListParams) { resp ->
        	
			if (resp.status == 200) {
            	TRACE ("resp.data.DeviceName: ${resp.data.DeviceName}")
				resp.data.each { stat ->
                	
					state.sensiSensors = state.sensiSensors == null ? stat.DeviceName : state.sensiSensors <<  stat.DeviceName
					def dni = stat.ICD
					stats[dni] = getThermostatDisplayName(stat)
				}
			} else {
				log.warn "Failed to get thermostat list in getSensiThermostats: ${resp.status}"
			}
		}
	} catch (e) {
        log.trace "Exception getting thermostats: " + e
        state.connected = false
    }
	state.thermostats = stats
    state.thermostatResponse = stats
    //log.debug "State Thermostats: ${state.thermostats}"
	return stats
}
def pollHandler() {
	//log.debug "pollHandler()"
	pollChildren(null) // Hit the sensi API for update on all thermostats

}

def pollChildren() {
	def devices = getChildDevices()
	devices.each { child ->
    	TRACE("pollChild($child.device.deviceNetworkId)")
        try{
            if(pollChild(child.device.deviceNetworkId)) {
                TRACE("pollChildren successful")

            } else {
                log.warn "pollChildren FAILED for $child.device.label"
                state.connected = false
                runIn(30, poll)
            }
        } catch (e) {
        	log.error "Error $e in pollChildren() for $child.device.label"
        }
    }
    return true
}
def getSubscribed(thermostatIdsString) {
	/*
	if(state.lastSubscribedDNI == thermostatIdsString) {
    	TRACE("Thermostat already subscribed")
        return true
    } else */
    if(state.lastSubscribedDNI != null) {
    	TRACE("Unsubscribing from: $state.lastSubscribedDNI")
    	getUnsubscribed(state.lastSubscribedDNI)
    }
	TRACE("Getting subscribed to $thermostatIdsString")
	if(!state.connected) { getConnected() }
    if( state.RBCounter > 50) {
    	state.RBCounter = 0
    }
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"Subscribe\",\"A\":[\"${thermostatIdsString}\"],\"I\":$state.RBCounter}"]
    state.RBCounter = state.RBCounter + 1


    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody
    ]

    try {

        httpPost(params) { resp ->
            TRACE( "Subscribe response: ${resp.data} Expected Response: [I:${state.RBCounter - 1}]")
            if(resp?.data.I?.toInteger() == (state.RBCounter - 1)) {
				state.lastSubscribedDNI = thermostatIdsString
                TRACE("Subscribe successfully")
            } else {
            	TRACE("Failed to subscribe")
                state.connected = false
            }
        }
    } catch (e) {
        log.error "getSubscribed failed: $e"
        state.connected = false
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite
    }
}

def getUnsubscribed(thermostatIdsString) {

    //Unsubscribe from this device
    def requestBody3 = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"Unsubscribe\",\"A\":[\"${thermostatIdsString}\"],\"I\":$state.RBCounter}"]
    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody3
    ]
	state.RBCounter = state.RBCounter + 1
    try {

        httpPost(params) { resp ->
            TRACE( "resp 3: ${resp.data}")
        }
    } 
    catch (e) {
        log.trace "Exception unsubscribing " + e
        state.connected = false
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite
    }
    state.lastSubscribedDNI = null
        
}	
def pollChildData(data) {
	def device = getChildDevice(data.value)
	log.info "Scheduled re-poll of $device.deviceLabel $data.value $device.label"
    pollChild(data.value)
}
// Poll Child is invoked from the Child Device itself as part of the Poll Capability
// If no dni is passed it will call pollChildren and poll all devices
def pollChild(dni = null) {
	
	if(dni == null) {
    	TRACE("dni in pollChild is $dni")
    	pollChildren()
        return
    }
    def thermostatIdsString = dni

    def params = []
    def result = false
    if(!state.connected || (state.messageId == null)) {
        getConnected()
    }    
    getSubscribed(thermostatIdsString)
    params = [
        uri: getApiEndpoint(),
        path: '/realtime/poll',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]"
                ,connectionId:state.connectionId,messageId:state.messageId,tid:state.RBCounter,'_':now()],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"]
    ]
    if(state.GroupsToken) {
        params.query = [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]"
                        ,connectionId:state.connectionId,messageId:state.messageId,GroupsToken:state.GroupsToken,tid:state.RBCounter,'_':now()]
    }

    try{
        httpGet(params) { resp ->
        	def httpResp =  resp.data.M[0].A[1] == null ? " " : resp.data.M[0].A[1]
            if(httpResp && (httpResp != true)) {            	
                state.thermostatResponse[thermostatIdsString] = httpResp
                TRACE("child.generateEvent=$httpResp")
                def myChild = getChildDevice(dni)
                myChild.generateEvent(httpResp)
				result = true
            } else {
            	httpResp = resp.data.M[0].M == null ? " " : resp.data.M[0].M
            	log.warn "Unexpected final resp in pollChild: ${resp.data} likely offline: $httpResp"
            }
            if(resp.data.C) {            	
                state.messageId = resp.data.C

            }
            if(resp.data.G) {
                state.GroupsToken = resp.data.G
            }
        }
        state.RBCounter = state.RBCounter + 1
    } catch (e) {
        log.error "Exception in pollChild: $e data: $resp.data"
        log.error "repoll in 30 seconds. Re-poll: $thermostatIdsString"
        state.connected = false //This will trigger new authentication next time the poll occurs   
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite
    }
            
	return result
}

void poll() {
	pollChildren()
}

def availableModes(child) {


	def modes = ["off", "heat", "cool", "aux", "auto"]

    return modes
}

def currentMode(child) {
	debugEvent ("state.Thermos = ${state.thermostats}")
	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = state.thermostatResponse[child.device.deviceNetworkId]?.EnvironmentControls

	//debugEvent("Data = ${tData}")

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
		return null
	}

	def mode = tData?.SystemMode
	return mode
}

/**
 * Executes the cmdString and cmdVal
 * @param deviceId - the ID of the device
 * @cmdString is passed directly to Sensi Web
 * @cmdVal is the value to send on.
 *
 * @retrun true if the command was successful, false otherwise.
 */

boolean setStringCmd(deviceId, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniStringCmd(deviceId,cmdString,cmdVal)
    TRACE( "Setstring ${result}")
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    getUnsubscribed(deviceId)
    return result
}
boolean setSettingsStringCmd(deviceId,cmdSettings, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniSettingsStringCmd(deviceId,cmdSettings,cmdString,cmdVal)
    TRACE( "Setstring ${result}")
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    getUnsubscribed(deviceId)
    return result
}
boolean setTempCmd(deviceId, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniValue(deviceId,cmdString,cmdVal)
    TRACE( "Setstring ${result}")
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    getUnsubscribed
    return result
}
boolean sendDniValue(thermostatIdsString,cmdString,cmdVal) {
	def result = false
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"$cmdString\",\"A\":[\"${thermostatIdsString}\",$cmdVal,\"$location.temperatureScale\"],\"I\":$state.RBCounter}"]
    
	TRACE( "sendDNIValue body: ${requestBody}")
    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody
    ]

    try {

        httpPost(params) { resp ->
            
            if (resp.data.I.toInteger() == state.RBCounter.toInteger()) {
            	result = true
            }
            state.RBCounter = state.RBCounter + 1
        }
    } catch (e) {
        log.warn "Send DNI Command went wrong: $e"
        state.connected = false
        state.RBCounter = state.RBCounter + 1

    }
    
    return result
}
boolean sendDniStringCmd(thermostatIdsString,cmdString,cmdVal) {
	def result = false
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"$cmdString\",\"A\":[\"${thermostatIdsString}\",\"$cmdVal\"],\"I\":$state.RBCounter}"]
    
    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody
    ]

    try {

        httpPost(params) { resp ->
            
            if (resp.data.I.toInteger() == state.RBCounter.toInteger()) {
            	result = true
            }
            state.RBCounter = state.RBCounter + 1
        }
    } catch (e) {
        log.warn "Send DNI String Command went wrong: $e"
        state.connected = false
        state.RBCounter = state.RBCounter + 1
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite

    }
    TRACE( "Send Function : $result")
    return result
}
/* {"H":"thermostat-v1","M":"ChangeSetting","A":["thermostatid is here","KeypadLockout","Off"],"I":8} */
boolean sendDniSettingsStringCmd(thermostatIdsString,cmdSettings,cmdString,cmdVal) {
	def result = false
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"$cmdSettings\",\"A\":[\"${thermostatIdsString}\",\"$cmdString\",\"$cmdVal\"],\"I\":$state.RBCounter}"]
    
    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody
    ]

    try {

        httpPost(params) { resp ->
            
            if (resp.data.I.toInteger() == state.RBCounter.toInteger()) {
            	result = true
            }
            state.RBCounter = state.RBCounter + 1
        }
    } catch (e) {
        log.warn "Send DNI Setting String Command went wrong: $e"
        state.connected = false
        state.RBCounter = state.RBCounter + 1
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite

    }
    TRACE( "Send Function : $result")
    return result
}

def getChildName()           { return "Sensi Thermostat" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getApiEndpoint()		 { return "https://bus-serv.sensicomfort.com" }

def debugEvent(message, displayEvent = false) {
	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)
}

def sendActivityFeeds(notificationMessage) {
	def devices = getChildDevices()
	devices.each { child ->
		child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
	}
}

private def TRACE(message) {
    //log.trace message
}