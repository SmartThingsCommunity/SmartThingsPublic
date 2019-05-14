/**
 *  YaleApp
 *
 *  Copyright 2019 Mark Cockcroft
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
definition(
    name: "YaleApp",
    namespace: "Mark-C-uk",
    author: "Mark Cockcroft",
    description: "manage yale conncetion",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	singleInstance: true
)

preferences {
	page(name: "cloudLogin", title: "Cloud Login", nextPage:"", content:"cloudLogin", uninstall: true)
	page(name: "selectDevices", title: "Select Devices", nextPage:"", content:"selectDevices", uninstall: true, install: true)
}

def setInitialStates() {
	if (!state.Token) {state.Token = null}
	if (!state.devices) {state.devices = [:]}
	if (!state.currentError) {state.currentError = null}
	if (!state.errorCount) {state.errorCount = 0}
}

//	----- LOGIN PAGE -----
def cloudLogin() {
	setInitialStates()
	def cloudLoginText = "If possible, open the IDE and select Live Logging.  THEN, " +
		"enter your Username and Password for YALE and the "+
		"action you want to complete.  Your current token:\n\r\n\r${state.Token}" +
		"\n\r\n\rAvailable actions:\n\r" +
		"	Initial Install: Obtains token and adds devices.\n\r" +
		"	Add Devices: Only add devices.\n\r" +
		"	Update Token:  Updates the token.\n\r"
	def errorMsg = ""
	if (state.currentError != null){
		errorMsg = "Error communicating with cloud:\n\r\n\r${state.currentError}" +
			"\n\r\n\rPlease resolve the error and try again.\n\r\n\r"
		}
	return dynamicPage(
		name: "cloudLogin", 
		title: "Device Service Manager", 
		nextPage: "selectDevices", 
		uninstall: true) {
		section(errorMsg)
		section(cloudLoginText) {
			input( 
				"userName", "string", 
				title:"Your YALE Email Address", 
				required:true, 
				displayDuringSetup: true
			)
			input(
				"userPassword", "password", 
				title:"account password", 
				required: true, 
				displayDuringSetup: true
			)
			input(
				"updateToken", "enum",
				title: "What do you want to do?",
				required: true, 
				multiple: false,
				options: ["Initial Install", "Add Devices", "Update Token"]
			)
		}
	}
}
//====================================== loginPage end

//	----- SELECT DEVICES PAGE -----
def selectDevices() {
	if (updateToken != "Add Devices") {
		getToken()
	}
	if (state.currentError != null || updateToken == "Update Token") {
		return cloudLogin()
	}
	getDevices()
	def devices = state.devices
	if (state.currentError != null) {
		return cloudLogin()
	}
	def errorMsg = ""
	if (devices == [:]) {
		errorMsg = "There were no devices from YALE"
	}
	def newDevices = [:]
    //log.debug "select devices, ${devices}"
	devices.each {
    	//log.debug "select devices each ${it.value.deviceId} - ${it.value.alias} - model ${it.value.deviceModel}"
		def isChild = getChildDevice(it.value.deviceId) // deviceId changed to dni so dont add twice
		if (!isChild) {
        	//log.debug "select devices, each !ischild ${it.value.alias} - ${it.value.deviceid}" //value.
			newDevices["${it.value.deviceId}"] = "${it.value.alias} model ${it.value.deviceModel}"
            //log.debug "select devices, each !ischild $newDevices"
		}
	}
	if (newDevices == [:]) {
		errorMsg = "No new devices to add."
		}
	settings.selectedDevices = null
	def DevicesMsg = "Token is ${state.Token}\n\r" +
		"TAP below to see the list of devices available select the ones you want to connect to " +
		"SmartThings.\n\r\n\rPress DONE when you have selected the devices you " +
		"wish to add, thenpress DONE again to install the devices.  Press	<	" +
		"to return to the previous page."
	return dynamicPage(
		name: "selectDevices", 
		title: "Select Your Devices", 
		install: true,
		uninstall: true) {
		section(errorMsg)
		section(DevicesMsg) {
			input "selectedDevices", "enum",
			required:false, 
			multiple:true, 
			title: "Select Devices (${newDevices.size() ?: 0} found)",
			options: newDevices
		}
	}
}
def getDevices() {
	def currentDevices = ''
	currentDevices = getDeviceData()
    //log.debug "get devices - ${currentDevices?.data?.data}"
	state.devices = [:]
	def devices = state.devices
    
/*    def map = [ result: "true", //need to add this map to other
            			code: "000",
                        message: "OK!",
                        token: "NA",
                        data:[[mac:"mademac",
                        		name:"Alarm",
                                type:"Alarm",
                                device_id:"madeid"
                         ],
                         [mac:"mademac1",
                        		name:"Alarm1",
                                type:"Alarm1",
                                device_id:"madeid1"
                         ]
            	]]
*/    
	currentDevices.data?.data.each {
    def length = it.device_id.length()-1
		def device = [:]
		//device["deviceMac"] = it.mac		//	data.data[0]
		device["alias"] = it.name
		device["deviceModel"] = it.type
		device["deviceId"] = it.device_id
        device["dni"] = it.device_id.substring(3,length)
        
		devices << ["${it.device_id}": device]	
		//log.info "GET Device ${it.name} - ${it.device_id}"
	}
    ///devices << ["${it.device_id}": device] //add pannel device?
    log.debug "arry $devices"
}

def addDevices() {
	log.debug "ADD Devices "// ${state?.devices}
	def Model = [:]
	Model << ["YaleAlarm" : "Yale Alarm pannel"]			
	Model << ["device_type.keypad" : "Yale Alarm Open Close Sensor"]
    Model << ["device_type.remote_controller" : "Yale Alarm Open Close Sensor"]
    Model << ["device_type.pir" : "Yale Alarm Open Close Sensor"]
    Model << ["device_type.door_contact" : "Yale Alarm Open Close Sensor"]


	def hub = location.hubs[0]
	def hubId = hub.id
	selectedDevices.each { deviceId -> 
    	log.debug "addDevice each --- $deviceId"
		def isChild = getChildDevice(deviceId)
		if (!isChild) {
			def device = state.devices.find { it.value.deviceId == deviceId }
			def deviceModel = device.value.deviceModel 
            log.debug " add it not child $device - $device -and- $deviceModel"
			addChildDevice(
				"smartthings",
				Model["${deviceModel}"], 
				device.value.deviceId,
				hubId, [
					"label": "${device.value.alias} Yale",
						"name": device.value.deviceModel,
					"data": [
						"deviceId" : device.value.deviceId,
					]
				]
			)
			log.info "Installed  $deviceModel with alias ${device.value.alias}"
		}
	}
}

def yaleAuthToken () {
	return "VnVWWDZYVjlXSUNzVHJhcUVpdVNCUHBwZ3ZPakxUeXNsRU1LUHBjdTpkd3RPbE15WEtENUJ5ZW1GWHV0am55eGhrc0U3V0ZFY2p0dFcyOXRaSWNuWHlSWHFsWVBEZ1BSZE1xczF4R3VwVTlxa1o4UE5ubGlQanY5Z2hBZFFtMHpsM0h4V3dlS0ZBcGZzakpMcW1GMm1HR1lXRlpad01MRkw3MGR0bmNndQ=="
}
def getToken() {
	log.debug "Attempting to login for new token"
	def paramsLogin = [
			uri: "https://mob.yalehomesystem.co.uk/yapi/o/token/",
			body: [grant_type: "password", username: "${userName}" , password: "${userPassword}"],
			headers: ['Authorization' : "Basic ${yaleAuthToken()}"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
    //log.debug "login in params = $paramsLogin"
	httpPost(paramsLogin) { responseLogin ->
		//log.debug "Login response is $responseLogin.data"
		
        if (responseLogin.status == 200){
        	state.Token = responseLogin.data?.access_token
    		//log.info "Token updated to ${state.Token}"
    		sendEvent(name: "TokenUpdate", value: "tokenUpdate Successful.")
    	}
    	else if (responseLogin.status != 200) {
			state.currentError = responseLogin.message
			sendEvent(name: "currentError", value: responseLogin.data)
			log.error "Error in getToken: ${state.currentError}"
			sendEvent(name: "TokenUpdate", value: state.currentError)
		} 
	}
}
//	----- GET DEVICE DATA FROM THE CLOUD -----
def getDeviceData() { // get details for adding
log.debug "getDeviceData"
/*	data()
    def currentDevices = state.data //''
    //currentDevices = state.data
	
    return currentDevices
*/	
	def currentDevices = ''
    def getPanelMetaDataAndFullStatus = [
			uri: "https://mob.yalehomesystem.co.uk/yapi/api/panel/device_status/", //	api/panel/mode/",
			headers: ['Authorization' : "Bearer ${state.Token}"]
	]
    httpGet(getPanelMetaDataAndFullStatus) { response ->
		//log.debug "get device data - response = ${response.status} ===== ${response.data}"
        if (response.status == 200){
        	currentDevices = response
            state.devdata = response.data.data //
   			log.debug "retun data from getdevicedata - done " //${currentDevices.data} 
			return currentDevices
        }
		else (response.status != 200) {
			state.currentError = response.status
			sendEvent(name: "currentError", value: response.data)
			log.error "Error in getDeviceData: ${state.currentError}"
		}
	}
	def getPanelStatus = [
			uri: "https://mob.yalehomesystem.co.uk/yapi/api/panel/mode/", //	api/panel/mode/",
			headers: ['Authorization' : "Bearer ${state.Token}"]
	]
    httpGet(getPanelStatus) { response ->
		//log.debug "get device data - response = ${response.status} ===== ${response.data}"
        if (response.status == 200){
        	log.debug "Pannel request good - ${response.data.data.getAt(0)}"
        }
	}
}
//	----- INSTALL, UPDATE, INITIALIZE -----
def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	unsubscribe()
	unschedule()
	runEvery5Minutes(checkError)
    runEvery5Minutes(getDeviceData)
	schedule("0 30 2 ? * WED", getToken)
	if (selectedDevices) {
		addDevices()
	}
}
//	----- PERIODIC CLOUD MX TASKS -----
def checkError() {
	if (state.currentError == null || state.currentError == "none") {
		log.info "Connect did not have any set errors."
		return
	}
	def errMsg = state.currentError.msg
	log.info "Attempting to solve error: ${errMsg}"
	state.errorCount = state.errorCount +1
	if (errMsg == "Token expired" && state.errorCount < 6) {
		sendEvent (name: "ErrHandling", value: "Handle comms error attempt ${state.errorCount}")
		getDevices()
		if (state.currentError == null) {
			log.info "getDevices successful.  apiServerUrl updated and token is good."
			return
		}
		log.error "${errMsg} error while attempting getDevices.  Will attempt getToken"
		getToken()
		if (state.currentError == null) {
			log.info "getToken successful.  Token has been updated."
			getDevices()
			return
		}
	} else {
		log.error "checkError:  No auto-correctable errors or exceeded Token request count."
	}
	log.error "checkError residual:  ${state.currentError}"
}
//	----- CHILD CALLED TASKS -----
def removeChildDevice(alias, deviceNetworkId) {
	try {
		deleteChildDevice(it.deviceNetworkId)
		sendEvent(name: "DeviceDelete", value: "${alias} deleted")
	} catch (Exception e) {
		sendEvent(name: "DeviceDelete", value: "Failed to delete ${alias}")
	}
}