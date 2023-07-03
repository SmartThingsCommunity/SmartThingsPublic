/*
TP-Link Connect Service Manager, 2018 Version 2
Copyright 2018 Dave Gutheinz
Licensed under the Apache License, Version 2.0 (the "License"); you 
may not use this file except in compliance with the License. You may 
obtain a copy of the License at:
	http://www.apache.org/licenses/LICENSE-2.0
		
Unless required by applicable law or agreed to in writing, software 
distributed under  the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
implied. See the License for the specific language governing 
permissions and limitations under the License.
##### Discalimer:  This Service Manager and the associated Device 
Handlers are in no way sanctioned or supported by TP-Link.  All  
development is based upon open-source data on the TP-Link devices; 
primarily various users on GitHub.com.
##### Notes #####
1.	This Service Manager is designed to install and manage TP-Link 
	bulbs, plugs, and switches using their respective device handlers.
2.	Please direct comments to the SmartThings community thread 
	'Cloud TP-Link Device SmartThings Integration'.
##### History #####
2018-01-31	Updated for new release of Device Handlers
*/

definition(
	name: "TP-Link Cloud Connect",
	namespace: "davegut",
	author: "Dave Gutheinz",
	description: "A Service Manager for the TP-Link devices connecting through the TP-Link Cloud",
	category: "SmartThings Labs",
	iconUrl: "http://ecx.images-amazon.com/images/I/51S8gO0bvZL._SL210_QL95_.png",
	iconX2Url: "http://ecx.images-amazon.com/images/I/51S8gO0bvZL._SL210_QL95_.png",
	iconX3Url: "http://ecx.images-amazon.com/images/I/51S8gO0bvZL._SL210_QL95_.png",
	singleInstance: true
)

preferences {
	page(name: "cloudLogin", title: "TP-Link Cloud Login", nextPage:"", content:"cloudLogin", uninstall: true)
	page(name: "selectDevices", title: "Select TP-Link Devices", nextPage:"", content:"selectDevices", uninstall: true, install: true)
}

def setInitialStates() {
	if (!state.TpLinkToken) {state.TpLinkToken = null}
	if (!state.devices) {state.devices = [:]}
	if (!state.currentError) {state.currentError = null}
	if (!state.errorCount) {state.errorCount = 0}
}

//	----- LOGIN PAGE -----
def cloudLogin() {
	setInitialStates()
	def cloudLoginText = "If possible, open the IDE and select Live Logging.  THEN, " +
		"enter your Username and Password for TP-Link (same as Kasa app) and the "+
		"action you want to complete.  Your current token:\n\r\n\r${state.TpLinkToken}" +
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
		title: "TP-Link Device Service Manager", 
		nextPage: "selectDevices", 
		uninstall: true) {
		section(errorMsg)
		section(cloudLoginText) {
			input( 
				"userName", "string", 
				title:"Your TP-Link Email Address", 
				required:true, 
				displayDuringSetup: true
			)
			input(
				"userPassword", "password", 
				title:"TP-Link account password", 
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
		errorMsg = "There were no devices from TP-Link.  This usually means "+
			"that all devices are in 'Local Control Only'.  Correct then " +
			"rerun.\n\r\n\r"
	}
	def newDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceMac)
		if (!isChild) {
			newDevices["${it.value.deviceMac}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (newDevices == [:]) {
		errorMsg = "No new devices to add.  Are you sure they are in Remote " +
			"Control Mode?\n\r\n\r"
		}
	settings.selectedDevices = null
	def TPLinkDevicesMsg = "TP-Link Token is ${state.TpLinkToken}\n\r" +
		"Devices that have not been previously installed and are not in 'Local " +
		"WiFi control only' will appear below.  TAP below to see the list of " +
		"TP-Link devices available select the ones you want to connect to " +
		"SmartThings.\n\r\n\rPress DONE when you have selected the devices you " +
		"wish to add, thenpress DONE again to install the devices.  Press	<	" +
		"to return to the previous page."
	return dynamicPage(
		name: "selectDevices", 
		title: "Select Your TP-Link Devices", 
		install: true,
		uninstall: true) {
		section(errorMsg)
		section(TPLinkDevicesMsg) {
			input "selectedDevices", "enum",
			required:false, 
			multiple:true, 
			title: "Select Devices (${newDevices.size() ?: 0} found)",
			options: newDevices
		}
	}
}

def getDevices() {
	def currentDevices = getDeviceData()
	state.devices = [:]
	def devices = state.devices
	currentDevices.each {
		def device = [:]
		device["deviceMac"] = it.deviceMac
		device["alias"] = it.alias
		device["deviceModel"] = it.deviceModel
		device["deviceId"] = it.deviceId
		device["appServerUrl"] = it.appServerUrl
		devices << ["${it.deviceMac}": device]
		def isChild = getChildDevice(it.deviceMac)
		if (isChild) {
			isChild.syncAppServerUrl(it.appServerUrl)
		}
		log.info "Device ${it.alias} added to devices array"
	}
}

def addDevices() {
	def tpLinkModel = [:]
	//	Plug-Switch Devices (no energy monitor capability)
	tpLinkModel << ["HS100" : "(Cloud) TP-Link Plug-Switch"]			//	HS100
	tpLinkModel << ["HS103" : "(Cloud) TP-Link Plug-Switch"]			//	HS103
	tpLinkModel << ["HS105" : "(Cloud) TP-Link Plug-Switch"]			//	HS105
	tpLinkModel << ["HS200" : "(Cloud) TP-Link Plug-Switch"]			//	HS200
	tpLinkModel << ["HS210" : "(Cloud) TP-Link Plug-Switch"]			//	HS210
	tpLinkModel << ["KP100" : "(Cloud) TP-Link Plug-Switch"]			//	KP100
	//	Dimming Plug Devices
	tpLinkModel << ["HS220" : "(Cloud) TP-Link Dimming Switch"]			//	HS220
	//	Energy Monitor Plugs
	tpLinkModel << ["HS110" : "(Cloud) TP-Link EnergyMonitor Plug"]		//	HS110
	tpLinkModel << ["HS115" : "(Cloud) TP-Link EnergyMonitor Plug"]		//	HS110
	//	Soft White Bulbs
	tpLinkModel << ["KB100" : "(Cloud) TP-Link SoftWhite Bulb"]			//	KB100
	tpLinkModel << ["LB100" : "(Cloud) TP-Link SoftWhite Bulb"]			//	LB100
	tpLinkModel << ["LB110" : "(Cloud) TP-Link SoftWhite Bulb"]			//	LB110
	tpLinkModel << ["KL110" : "(Cloud) TP-Link SoftWhite Bulb"]			//	KL110
	tpLinkModel << ["LB200" : "(Cloud) TP-Link SoftWhite Bulb"]			//	LB200
	//	Tunable White Bulbs
	tpLinkModel << ["LB120" : "(Cloud) TP-Link TunableWhite Bulb"]		//	LB120
	tpLinkModel << ["KL120" : "(Cloud) TP-Link TunableWhite Bulb"]		//	KL120
	//	Color Bulbs
	tpLinkModel << ["KB130" : "(Cloud) TP-Link Color Bulb"]				//	KB130
	tpLinkModel << ["LB130" : "(Cloud) TP-Link Color Bulb"]				//	LB130
	tpLinkModel << ["KL130" : "(Cloud) TP-Link Color Bulb"]				//	KL130
	tpLinkModel << ["LB230" : "(Cloud) TP-Link Color Bulb"]				//	LB230

	def hub = location.hubs[0]
	def hubId = hub.id
	selectedDevices.each { dni ->
		def isChild = getChildDevice(dni)
		if (!isChild) {
			def device = state.devices.find { it.value.deviceMac == dni }
			def deviceModel = device.value.deviceModel.substring(0,5)
			addChildDevice(
				"davegut",
				tpLinkModel["${deviceModel}"], 
				device.value.deviceMac,
				hubId, [
					"label": device.value.alias,
						"name": device.value.deviceModel, 
					"data": [
						"deviceId" : device.value.deviceId,
						"appServerUrl": device.value.appServerUrl,
					]
				]
			)
			log.info "Installed TP-Link $deviceModel with alias ${device.value.alias}"
		}
	}
}

//	----- GET A NEW TOKEN FROM CLOUD -----
def getToken() {
	def hub = location.hubs[0]
	def cmdBody = [
		method: "login",
		params: [
			appType: "Kasa_Android",
			cloudUserName: "${userName}",
			cloudPassword: "${userPassword}",
			terminalUUID: "${hub.id}"
		]
	]
	def getTokenParams = [
		uri: "https://wap.tplinkcloud.com",
		requestContentType: 'application/json',
		contentType: 'application/json',
		headers: ['Accept':'application/json; version=1, */*; q=0.01'],
		body : new groovy.json.JsonBuilder(cmdBody).toString()
	]
	httpPostJson(getTokenParams) {resp ->
		if (resp.status == 200 && resp.data.error_code == 0) {
			state.TpLinkToken = resp.data.result.token
			log.info "TpLinkToken updated to ${state.TpLinkToken}"
			sendEvent(name: "TokenUpdate", value: "tokenUpdate Successful.")
			if (state.currentError != null) {
				state.currentError = null
			}
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getToken: ${state.currentError}"
			sendEvent(name: "TokenUpdate", value: state.currentError)
		} else if (resp.data.error_code != 0) {
			state.currentError = resp.data
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getToken: ${state.currentError}"
			sendEvent(name: "TokenUpdate", value: state.currentError)
		}
	}
}

//	----- GET DEVICE DATA FROM THE CLOUD -----
def getDeviceData() {
	def currentDevices = ""
	def cmdBody = [method: "getDeviceList"]
	def getDevicesParams = [
		uri: "https://wap.tplinkcloud.com?token=${state.TpLinkToken}",
		requestContentType: 'application/json',
		contentType: 'application/json',
		headers: ['Accept':'application/json; version=1, */*; q=0.01'],
		body : new groovy.json.JsonBuilder(cmdBody).toString()
	]
	httpPostJson(getDevicesParams) {resp ->
		if (resp.status == 200 && resp.data.error_code == 0) {
			currentDevices = resp.data.result.deviceList
			if (state.currentError != null) {
				state.currentError = null
			}
			return currentDevices
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getDeviceData: ${state.currentError}"
		} else if (resp.data.error_code != 0) {
			state.currentError = resp.data
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getDeviceData: ${state.currentError}"
		}
	}
}

//	----- SEND DEVICE COMMAND TO CLOUD FOR DH -----
def sendDeviceCmd(appServerUrl, deviceId, command) {
	def cmdResponse = ""
	def cmdBody = [
		method: "passthrough",
		params: [
			deviceId: deviceId, 
			requestData: "${command}"
		]
	]
	def sendCmdParams = [
		uri: "${appServerUrl}/?token=${state.TpLinkToken}",
		requestContentType: 'application/json',
		contentType: 'application/json',
		headers: ['Accept':'application/json; version=1, */*; q=0.01'],
		body : new groovy.json.JsonBuilder(cmdBody).toString()
	]
	httpPostJson(sendCmdParams) {resp ->
		if (resp.status == 200 && resp.data.error_code == 0) {
			def jsonSlurper = new groovy.json.JsonSlurper()
			cmdResponse = jsonSlurper.parseText(resp.data.result.responseData)
			if (state.errorCount != 0) {
				state.errorCount = 0
			}
			if (state.currentError != null) {
				state.currentError = null
				sendEvent(name: "currentError", value: null)
			log.debug "state.errorCount = ${state.errorCount} //	state.currentError = ${state.currentError}"
			}
//			log.debug "state.errorCount = ${state.errorCount} //	state.currentError = ${state.currentError}"
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			cmdResponse = "ERROR: ${resp.statusLine}"
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in sendDeviceCmd: ${state.currentError}"
		} else if (resp.data.error_code != 0) {
			state.currentError = resp.data
			cmdResponse = "ERROR: ${resp.data.msg}"
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in sendDeviceCmd: ${state.currentError}"
		}
	}
	return cmdResponse
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
	schedule("0 30 2 ? * WED", getToken)
	if (selectedDevices) {
		addDevices()
	}
}

//	----- PERIODIC CLOUD MX TASKS -----
def checkError() {
	if (state.currentError == null || state.currentError == "none") {
		log.info "TP-Link Connect did not have any set errors."
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