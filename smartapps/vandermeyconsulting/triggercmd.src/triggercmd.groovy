/**
 *  TRIGGERcmd V2
 *
 *  Copyright 2017 VanderMey Consulting, LLC
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

import java.security.MessageDigest;

private apiUrl() { "https://www.triggercmd.com" }

definition(
	name: "TRIGGERcmd",
	namespace: "vandermeyconsulting",
	author: "VanderMey Consulting, LLC",
	description: "Run commands on your computers. You must create a login acct at TRIGGERcmd.com.",
	category: "SmartThings Labs",
	iconUrl: "http://s3.amazonaws.com/triggercmdagents/icon60.jpg",
	iconX2Url: "http://s3.amazonaws.com/triggercmdagents/icon120.jpg",
    iconX3Url: "http://s3.amazonaws.com/triggercmdagents/icon550.jpg",
	singleInstance: true    
)


preferences {
	def msg = """Tap 'Next' after you have entered your TRIGGERcmd credentials.

Once your credentials are accepted, SmartThings will scan your TRIGGERcmd account for commands."""

	page(name: "selectDevices", title: "Connect your TRIGGERcmd commands to SmartThings", install: false, uninstall: true, nextPage: "chooseTriggers") {
		section("TRIGGERcmd credentials") {
			input "username", "text", title: "Enter TRIGGERcmd Email/UserName", required: true
			input "password", "password", title: "Enter TRIGGERcmd Password", required: true
			paragraph msg
		}
	}

	page(name: "chooseTriggers", title: "Choose commands to control with SmartThings", content: "initialize")
}

def installed() {
	debugOut "Installed with settings: ${settings}"

	unschedule()
	unsubscribe()

	setupBulbs()       
}

def updated() {
	debugOut "Updated with settings: ${settings}"

	unschedule()

	setupBulbs()
}

def uninstalled()
{
	unschedule() //in case we have hanging runIn()'s
}

private removeChildDevices(delete)
{
	debugOut "deleting ${delete.size()} bulbs"
	debugOut "deleting ${delete}"
	delete.each {
		deleteChildDevice(it.device.deviceNetworkId)
	}
}

def uninstallFromChildDevice(childDevice)
{
	def errorMsg = "uninstallFromChildDevice was called and "
	if (!settings.selectedBulbs) {
		debugOut errorMsg += "had empty list passed in"
		return
	}

	def dni = childDevice.device.deviceNetworkId

	if ( !dni ) {
		debugOut errorMsg += "could not find dni of device"
		return
	}

	def newDeviceList = settings.selectedBulbs - dni
	app.updateSetting("selectedBulbs", newDeviceList)

	debugOut errorMsg += "completed succesfully"
}


def setupBulbs() {
	debugOut "In setupBulbs"

	def bulbs = state.devices
    
    debugOut "bulbs: ${bulbs}"
    
	def deviceFile = "TRIGGERcmd Switch"

	if(selectedBulbs instanceof String) {
    	debugOut "selectedBulbs is a string because only a single command was selected"        
        def did = selectedBulbs
        
        //see if this is a selected bulb and install it if not already
		def d = getChildDevice(did)

		if(!d) {
			def newBulb = bulbs.find { (it.did) == did }
			d = addChildDevice("vandermeyconsulting", deviceFile, did, null, [name: "${newBulb?.name}", label: "${newBulb?.name}", completedSetup: true])

			debugOut "Added device d: ${d} did: ${did}"
		} else {
			debugOut "We already added this device d: ${d} did: ${did}"
		}        
    } else {
    	debugOut "selectedBulbs is not a string"
        
        selectedBulbs.each { did ->
			//see if this is a selected bulb and install it if not already
			def d = getChildDevice(did)

			if(!d) {
				def newBulb = bulbs.find { (it.did) == did }
				d = addChildDevice("vandermeyconsulting", deviceFile, did, null, [name: "${newBulb?.name}", label: "${newBulb?.name}", completedSetup: true])
				debugOut "Added device d: ${d} did: ${did}"
			} else {
				debugOut "We already added this device d: ${d} did: ${did}"
			}
    	}
  	}

	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !selectedBulbs?.contains(it.deviceNetworkId) }
	removeChildDevices(delete)

    unschedule()
    def exp = "* 0 * * * ?"    
	schedule(exp, cleanupTriggers)    
}

def initialize() {

	atomicState.token = ""

	getToken()

	if ( atomicState.token == "error" ) {
		return dynamicPage(name:"chooseBulbs", title:"TCP Login Failed!\r\nTap 'Done' to try again", nextPage:"", install:false, uninstall: false) {
			section("") {}
		}
	} else {
		"we're good to go"
		debugOut "We have Token."
	}

	//getGatewayData() //we really don't need anything from the gateway

	deviceDiscovery()

	def options = devicesDiscovered() ?: []

	def msg = """Tap 'Done' after you have selected the desired commands."""

	return dynamicPage(name:"chooseTriggers", title:"TRIGGERcmd and SmartThings Connected!", nextPage:"", install:true, uninstall: true) {
		section("Tap below to view command list") {
			input "selectedBulbs", "enum", required:false, title:"Select commands", multiple:true, options:options
			paragraph msg
		}
	}

}

def cleanupTriggers() {
	debugOut "Running cleanupTriggers."
    deviceDiscovery()    
    
    def devices =  state.devices
   
    // debugOut "devices: ${devices}"
    // debugOut "selectedBulbs: ${selectedBulbs}"
    
    def founddevice = ""
    def founddevicename = ""
    def selecteddevice = ""    
    def selectedwasfound = ""
    
    selectedBulbs.each { did ->
      selectedwasfound = "false"
      selecteddevice = did     
      // debugOut "Selecteddevice: ${selecteddevice}"
      devices.each({
        founddevice = it?.did        
        if ( founddevice == selecteddevice ) {
        	// debugOut "selecteddevice: ${selecteddevice} founddevice: ${founddevice}"
      		selectedwasfound = "true"
        }        
      })
      if ( selectedwasfound == "false" ) {
         debugOut "Deleting: ${selecteddevice}"
         deleteChildDevice(selecteddevice)
      }
	}    
}


def deviceDiscovery() {
	def Params = [
		token: "${atomicState.token}",
        uri: "/api/smartthings/commandlist"		
	]

	def triggers = ""

	apiPost(Params) { response ->
        triggers = response.data
	}

	debugOut "trigger data = ${triggers}"

	def devices = []
	def bulbIndex = 1
	def lastRoomName = null
	def deviceList = []
	
    def roomId = 1
    def roomName = ""
	devices = triggers
	
	if ( devices[1] != null ) {		
		debugOut "Room Device Data: did:${roomId} roomName:${roomName}"		
		devices.each({
			// debugOut "Bulb Device Data: did:${it?.did} room:${roomName} BulbName:${it?.name}"
			deviceList += ["name" : "${roomName} ${it?.name}", "did" : "${it?.did}", "type" : "bulb"]
		})
	} else {
		debugOut "Bulb Device Data: did:${devices?.did} room:${roomName} BulbName:${devices?.name}"
		// deviceList += ["name" : "${roomName} ${devices?.name}", "did" : "${devices?.did}", "type" : "bulb"]  <- this logic doesn't work when there's only 1 command
		// Thanks to mthiel for finding this fix. 
		deviceList += ["name" : "${roomName} ${devices[0].name}", "did" : "${devices[0].did}", "type" : "bulb"]
		
	}

	devices = ["devices" : deviceList]
	state.devices = devices.devices
    debugOut "state.devices: ${state.devices}"
}

Map devicesDiscovered() {
	def devices =  state.devices
	def map = [:]
	if (devices instanceof java.util.Map) {
		devices.each {
			def value = "${it?.name}"
			def key = it?.did
			map["${key}"] = value
		}
	} else { //backwards compatable
		devices.each {
			def value = "${it?.name}"
			def key = it?.did
			map["${key}"] = value
		}
	}
	map
}

def getToken() {

	atomicState.token = ""

	if (password) {

		def qParams = [			
			email: "${username}",
            password: "${password}"
		]

		apiLogin(qParams) { response ->
			def status = response.status.toString()
			// log.debug "response status: ${status}"  // russ
			sendNotificationEvent("Get token status ${status}")

			if (status != "200") {//success code = 200
				def errorText = response.data
                log.debug "error text: ${errorText}"  // russ
				debugOut "Error logging into TRIGGERcmd. Error = ${errorText}"
				atomicState.token = "error"
			} else {
				atomicState.token = response.data.token                
                log.debug "response token: ${response.data.token}" // russ
			}
		}
	} else {
		log.warn "Unable to log into TRIGGERcmd. Error = Password is null"
		atomicState.token = "error"
	}
}

def apiLogin(creds, Closure callback) {
	debugOut "In apiLogin with creds: ${creds}"
	def params = [
		uri: apiUrl() + "/api/auth/authenticate",
		body: creds
	]

	httpPost(params) {
		response ->
        	// log.debug "response data: ${response.status}"
        	def rc = response.status.toString()

			if ( rc == "200" ) {
				debugOut ("Return Code = ${rc} = Command Succeeded.")
				callback.call(response)

			} else if ( rc == "401" ) {
				debugOut "Return Code = ${rc} = Error: User not logged in!" //Error code from gateway
				log.debug "Refreshing Token"
				getToken()
				//callback.call(response) //stubbed out so getToken works (we had race issue)

			} else {
				log.error "APILogin Return Code = ${rc} = Error!" //Error code from gateway
				sendNotificationEvent("TRIGGERcmd is having Communication Errors. Error code = ${rc}.")
				callback.call(response)
			}
	}
}

def apiPost(data, Closure callback) {
	debugOut "In apiPost with data: ${data}"
	def params = [
		uri: apiUrl() + data.uri,
		body: data.body,
        headers: [
           'Authorization': "Bearer ${data.token}"
        ],
	]

	httpPost(params) {
		response ->
			def rc = response.status.toString()

			if ( rc == "200" ) {
				debugOut ("Return Code = ${rc} = Command Succeeded.")
				callback.call(response)

			} else if ( rc == "401" ) {
				debugOut "Return Code = ${rc} = Error: User not logged in!" //Error code from TRIGGERcmd
				log.debug "Refreshing Token"
				getToken()
				//callback.call(response) //stubbed out so getToken works (had race issue)

			} else {
				log.error "Return Code = ${rc} = Error!" //Error code from gateway
				sendNotificationEvent("TRIGGERcmd is having communication errors. Error code = ${rc}.")
				callback.call(response)
			}
	}
}


def generateSha256(String s) {

	MessageDigest digest = MessageDigest.getInstance("SHA-256")
	digest.update(s.bytes)
	new BigInteger(1, digest.digest()).toString(16).padLeft(40, '0')
}

def generateMD5(String s) {
	MessageDigest digest = MessageDigest.getInstance("MD5")
	digest.update(s.bytes);
	new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
}

String toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

def syncronizeDevices() {
	debugOut "In syncronizeDevices"

	def update = getChildDevices().findAll { selectedBulbs?.contains(it.deviceNetworkId) }

	update.each {
		def dni = getChildDevice( it.deviceNetworkId )
		debugOut "dni = ${dni}"

		if (isRoom(dni)) {
			pollRoom(dni)
		} else {
			poll(dni)
		}
	}
}

boolean isRoom(dni) {
	def device = state.devices.find() {(( it.type == 'room') && (it.did == "${dni}"))}
}

boolean isBulb(dni) {
	def device = state.devices.find() {(( it.type == 'bulb') && (it.did == "${dni}"))}
}

def debugEvent(message, displayEvent) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}

def debugOut(msg) {
	log.debug msg
	sendNotificationEvent(msg) //Uncomment this for troubleshooting only
}


/**************************************************************************
 Child Device Call In Methods
 **************************************************************************/
def on(childDevice) {
	debugOut "On request from child device ${childDevice}"

	def dni = childDevice.device.deviceNetworkId

	//Russ
	def Params = [
		token: "${atomicState.token}",
        uri: "/api/smartthings/triggerBase64",
        body: "trigger=${dni}&params=on"
	]

	apiPost(Params) { response ->
		debugOut "ON result: ${response.data}"
	}

}

def off(childDevice) {
	debugOut "Off request from child device"

    def dni = childDevice.device.deviceNetworkId

	//Russ
	def Params = [
		token: "${atomicState.token}",
        uri: "/api/smartthings/triggerBase64",
        body: "trigger=${dni}&params=off"
	]

	apiPost(Params) { response ->
		debugOut "OFF result: ${response.data}"
	}
}

