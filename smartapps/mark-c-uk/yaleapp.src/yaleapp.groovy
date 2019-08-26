/**
 *  YaleApp
 *
 *  Copyright 2019 Mark Cockcroft (and thanks to the support of DAVE GUTHEINZ)
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
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LetMyFriendsIn.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LetMyFriendsIn@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LetMyFriendsIn@2x.png",
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
				"userName", "email", 
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
            input("push", "bool", title: "Send push messages", required: true, displayDuringSetup: true, )
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
    log.debug "select devices, ${devices}"
	devices.each {
    	//log.debug "select devices each ${it.value.deviceId} - ${it.value.alias} - model ${it.value.deviceModel}"
		def isChild = getChildDevice(it.value.deviceId) // deviceId changed to dni so dont add twice
		if (!isChild) {
        	//log.debug "select devices, each !ischild ${it.value.alias} - ${it.value.deviceid}" //value.
			newDevices["${it.value.deviceId}"] = "${it.value.alias} \n model ${it.value.deviceModel}"
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
    log.info "get devices - ${currentDevices}"
	state.devices = [:]
	def devices = state.devices
	currentDevices.each { //.data?.data
		def device = [:]
		device["alias"] = it.name
		device["deviceModel"] = it.type
		device["deviceId"] = it.device_id
        devices << ["${it.device_id}": device]	
		log.info "GET Device ${it.name} - ${it.device_id}"
	}
    def deviceP = [:] //make up device for pannel and add to array
	deviceP["alias"] = "Yale Alarm"
	deviceP["deviceModel"] = "YaleAlarmPannel"
	deviceP["deviceId"] = "RF:YalePan1"  
	devices << ["RF:YalePan1": deviceP]
    
    log.debug "arry $devices"
}

def addDevices() {
	//log.debug "ADD Devices "// ${state?.devices}
	def Model = [:]
	Model << ["YaleAlarmPannel" : "Yale Alarm pannel"]			
	Model << ["device_type.keypad" : "Yale Alarm Open Close Sensor"]
    Model << ["device_type.remote_controller" : "Yale Alarm Open Close Sensor"]
    Model << ["device_type.pir" : "Yale Alarm Open Close Sensor"]
    Model << ["device_type.door_contact" : "Yale Alarm Open Close Sensor"]

	def hub = location.hubs[0]
	def hubId = hub.id
	selectedDevices.each { deviceId -> 
    	log.debug "Add Devices each -${device?.value?.alias} - $deviceId"
		def isChild = getChildDevice(deviceId)
		if (!isChild) {
			def device = state.devices.find { it.value.deviceId == deviceId }
			def deviceModel = device.value.deviceModel 
            log.debug "Add Devices, not child $device - $deviceModel"
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
	//def toke = createAccessToken()
	//log.debug "Attempting to login for new token tringin this $toke"
	def paramsLogin = [
			uri: "https://mob.yalehomesystem.co.uk/yapi/o/token/",
			body: [grant_type: "password", username: "${userName}" , password: "${userPassword}"],
			headers: ['Authorization' : "Basic ${yaleAuthToken()}"],
            //headers: ['Authorization' : "Basic $toke"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
    try{
	httpPost(paramsLogin) { responseLogin ->
		log.debug "Login response is $responseLogin.data"
		def respstatus = responseLogin?.status
        if (respstatus == 200){
        	state.Token = responseLogin?.data?.access_token
    		log.info "$respstatus - Token updated to ${state.Token}"
    		sendEvent(name: "TokenUpdate", value: "tokenUpdate Successful.")
            if (state.currentError != null) {
				state.currentError = null
			}
    	}
    	else {
			state.currentError = "token error $respstatus, ${responseLogin?.data}" //responseLogin.message
			log.error "Error in getToken: ${state.currentError}, ${responseLogin?.data}"
			sendEvent(name: "TokenUpdate", value: state.currentError)
            errorhand("Error Token NOT 200")
		}
	}
    }
    catch (e){
    	log.error "Error token: ${e}, ${e?.message}"
        state.currentError = e?.message
        sendEvent(name: "TokenUpdate", value: state.currentError)
        errorhand("Error Token catch")
    }
	log.debug "token end ${state.currentError}"
}
//	----- GET DEVICE DATA FROM THE CLOUD -----
def getDeviceData() { // get details for adding
	//log.debug "getDeviceData"
	def currentDevices = ''
    def getDeviceStatus = [
			uri: "https://mob.yalehomesystem.co.uk/yapi/api/panel/device_status/",
			headers: ['Authorization' : "Bearer ${state.Token}"]
	]
    try {
    httpGet(getDeviceStatus) { response ->
    	def respstatus = response?.status
        def respdata = response?.data
		//log.debug "get device data devices ${response.status} , ${response?.data}"
        if (respstatus == 200){
        	if (state.errorCount != 0) {
				state.errorCount = 0
			}
        	if (state.currentError != null) {
				state.currentError = null
			}
        	currentDevices = response?.data?.data
	        currentDevices.each {
        		//log.debug "it ${it?.name} - ${it?.device_id}"
				def isChild = getChildDevice(it?.device_id)
            	if (isChild) {
                	log.info "Sending status of '${it?.status_open[0]}' to child '${it?.name}'" 
                	isChild.datain(it)
                }
        	}
        }
		else {
			state.currentError = "error get devices $respstatus - $respdata"
			log.error "Get device data NOT 200 - ${state.currentError}"
            errorhand("Get device data NOT 200")
		}
	}
    }
    
    catch (ed){
    	log.error "Error device data: ${ed}, ${ed?.message}"
        state.currentError = ed?.message
        errorhand("Get device data catch")
    }
	def getPanelStatus = [
			uri: "https://mob.yalehomesystem.co.uk/yapi/api/panel/mode/", //	api/panel/mode/",
			headers: ['Authorization' : "Bearer ${state.Token}"]
	]
    try {
    httpGet(getPanelStatus) { response ->
    	def respstatus = response?.status
        def respdata = response?.data
		//log.debug "get device data - pannel ${response.status}"
        if (respstatus == 200){
        	def respmsg = response?.data?.message
        	if (state.currentError != null) {
				state.currentError = null
			}
        	//log.debug "Pannel request good - ${response.data.data.getAt(0)} , ${response.data.message}" //${response.data}
        	def isChild = getChildDevice('RF:YalePan1')
            	if (isChild) {
                	log.info "Sending status of '${response.data.data}', '$respmsg' to AlarmPannel" 
                	isChild.datain(respdata)
                	if (respmsg != 'OK!'){
                    	send("Alarm updated with message $respmsg")   
                    }
                }
        }
        else {
			state.currentError = "error get pannel $respstatus - $respdata"
			log.error "Get pannel data NOT 200 - ${state.currentError}"
            errorhand("Get pannel data NOT 200")
		}
	}
    }
    catch (ep){
		state.currentError = ep?.message
    	log.error "Get pannel data catch ${state.currentError}"
        errorhand("Get pannel data catch")
    }
    log.info "get device data current devices ${currentDevices.name}"
    return currentDevices
}

private send(msg) {
    if ( push == true ) {
        log.debug "sending push message - $msg" 
        sendPush(msg)
    }
}
//	----- ARM DISARM REFRESH -----
def ArmDisRef(mode){
	//log.debug "Incoming Mode CMD $mode "
	def paramsMode = [
			uri: "https://mob.yalehomesystem.co.uk/yapi/api/panel/mode/",
			body: [area: 1, mode: "${mode.value}"],
			headers: ['Authorization' : "Bearer ${state.Token}"],
			requestContentType: "application/x-www-form-urlencoded",
			contentType: "application/json"
	]
    try{
	httpPost(paramsMode) {	response ->
    	def respstatus = response?.status
        def respdata = response?.data
		if (respstatus == 200){
            def respmsg = response?.data?.message
            if (state.errorCount != 0) {
				state.errorCount = 0
			}
            if (state.currentError != null) {
				state.currentError = null
            }
            log.info "Mode $mode - '$respstatus' - '$respmsg' " 
            if (respmsg != 'OK!'){
               	send("Alarm mode change to '$mode' issue, message $respmsg") //if door left open
            }
		}
		else { //response status not 200
        	log.error "Error in MODE '$respstatus' to $mode, ${state.currentError} - $respdata"
			state.currentError = "error mode pannel $respstatus - $respdata"
			respdata = 'error'
            errorhand("Error Arm/Dis/Ref NOT 200")
		}
		return respdata
	}
    }
    catch (e){
    	log.error "Error arm/dis/Ref: ${e}, ${e?.message}"
        state.currentError = e?.message
        errorhand("Error Arm/Dis/Ref Catch")
    }
}

def errorhand (msg){
	log.warn "Error Handler $msg"
    sendEvent(name: "currentError", value: "Yale - $msg ${state.currentError}")
    send("Yale - $msg - ${state.currentError}")

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
	log.debug "initialise"
	unsubscribe()
	unschedule()
	runEvery5Minutes(checkError)
    runEvery10Minutes(getDeviceData)
    schedule(now() + 604800000, getToken) // once a week
	if (selectedDevices) {
		addDevices()
	}
}
//	----- PERIODIC CLOUD MX TASKS -----
def checkError() {
	if (state.currentError == null || state.currentError == "none") {
		log.info "Connect did not have any set errors - ${state?.currentError}"
		return
	}
	def errMsg = state.currentError
	log.info "Attempting to solve error: ${errMsg}"
	state.errorCount = state.errorCount +1
    send("error ${errMsg}, count is ${state.errorCount}")
	if (state.errorCount < 6) {
		sendEvent (name: "ErrHandling", value: "Handle comms error attempt ${state.errorCount} - $errMsg")
		getDevices()
		if (state.currentError == null) {
			log.info "getDevices successful. token is good."
			return
		}
		log.error "${errMsg} error while attempting getDevices.  Will attempt getToken"
		getToken()
		if (state.currentError == null) {
			log.info "getToken successful.  Token has been updated."
			getDevices()
			return
		}
	}
    else {
		log.error "checkError:  No auto-correctable errors or exceeded Token request count."
        send("error ${errMsg}, count is ${state.errorCount} couldnt fix it")
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