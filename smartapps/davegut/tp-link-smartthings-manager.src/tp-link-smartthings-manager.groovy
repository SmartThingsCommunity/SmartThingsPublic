/*
TP-Link SmartThings Manager and TP-Link Cloud Connect, 2018 Version 4

	Copyright 2019 Dave Gutheinz, Anthony Ramirez
    
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
obtain a copy of the License at:

	http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.

Discalimer: This Service Manager and the associated Device Handlers are in no way sanctioned or supported by TP-Link. All
development is based upon open-source data on the TP-Link Kasa Devices; primarily various users on GitHub.com.
	====== Application History ================================
02.22.19	4.0.01.	Update to version 4 of Service Manager.  Following are the goals:
			a.	Reduced user options at each level.
			b.	More intuitive interface with appropriate in-line annotation giving.
			c.	Removal of external icons to preclude crashing of app in the new phone app.
02.24.19	4.0.02.	Fix code to eliminate periodic error on initial installation.  Genericised text to accommodate
			differnces in IOS and Android interface.
	====== Application Information ==========================*/
    def traceLogging() { return true }
//	def traceLogging() { return false }
	def appVersion() { return "4.0.01" }
	def driverVersion() { return "4.0" }
    def hubVersion() { return "4.0" }
//	===========================================================

definition (
	name: "TP-Link SmartThings Manager", 
	namespace: "davegut", 
	author: "Dave Gutheinz, Anthony Ramirez", 
	description: "SmartThings TP-Link/Kasa Service Manager.", 
	category: "Convenience", 
	iconUrl: "http://ecx.images-amazon.com/images/I/51S8gO0bvZL._SL210_QL95_.png",
	iconX2Url: "http://ecx.images-amazon.com/images/I/51S8gO0bvZL._SL210_QL95_.png",
	iconX3Url: "http://ecx.images-amazon.com/images/I/51S8gO0bvZL._SL210_QL95_.png",
	singleInstance: true
)

preferences {
	page(name: "startPage")
	page(name: "welcomePage")
	page(name: "hubEnterIpPage")
	page(name: "kasaAuthenticationPage")
    page(name: "addDevicesPage")
	page(name: "devicePreferencesPage")
	page(name: "updateInstallDataPage")
	page(name: "flowDirector")
}

def setInitialStates() {
	if (!state.TpLinkToken) { state.TpLinkToken = null }
	if (!state.devices) { state.devices = [:] }
	if (!state.currentError) { state.currentError = null }
	if (!state.errorCount) { state.errorCount = 0 }
    if (!state.hubVersion) { state.hubVersion = null }
	state.flowType = "default"
}

//	===== Pages =====================
def startPage() {
	traceLog("startPage: installType = ${installType}")
	setInitialStates()
	if (installType) {
		if (installType == "Kasa Account" && !userName) {
			return kasaAuthenticationPage()       	
		} else if (installType == "Node Applet" && !bridgeIp) {
			return hubEnterIpPage()
        } else {
        	return welcomePage()
        }
	}

    def page1Text = ""
    page1Text += "This Service Manager supports either a Kasa Account or Node Applet installation:"
    page1Text += "\na.  Kasa Account: Kasa cloud based integration."
    page1Text += "\nb.  Node Applet: node.js applet based integration."
    page1Text += "\nAfter selecting the installation type, the SM will transition to either the Kasa Login "
    page1Text += "or the enter hub IP page."

	return dynamicPage (name: "startPage", title: "Select Installation Type", uninstall: true) {
		errorSection()
 		section("Instructions - select  '+'  to expand", hideable: true, hidden: true) {
            paragraph page1Text
		}
		section("") {
			input ("installType", "enum", 
            	title: "Select Installation Type", 
                required: true, 
                multiple: false,
                submitOnChange: true,
				options: ["Kasa Account", "Node Applet"])
            if (installType == "Kasa Account") {
            	return kasaAuthenticationPage()
            } else if (installType == "Node Applet") {
            	return hubEnterIpPage()
            }
			paragraph "Select  '<'  at upper left corner to exit."
            paragraph "Select 'Remove' to remove application."
 		}
	}
}

def welcomePage() {
	traceLog("welcomePage: installType = ${installType}")
	if (installType == "Kasa Account") {
    	kasaGetDevices()
    } else {
	    hubSendCommand("hubCheck")
    }
	app.deleteSetting("selectedAddDevices")
	app.deleteSetting("selectedDeleteDevices")
	app.deleteSetting("selectedUpdateDevices")
	app.deleteSetting("userLightTransTime")
	app.deleteSetting("userRefreshRate")
    
    def page1Text = ""
    page1Text += "Various options are available:"
    page1Text += "\na. ADD DEVICES. Select devices to add and install into ST."
    page1Text += "\n\nb. SET DEVICE PREFERENCES.  Set refresh rate or light transition time for devices."
    if (installType == "Kasa Account") {
	    page1Text += "\n\nc. UPDATE KASA ACCOUNT CREDENTIALS. Maintenance function to try if you are having "
    	page1Text += "communications issues."
    } else {
	    page1Text += "\n\nc. UPDATE NODE.JS HUB IP. Maintenance function to try if you are having "
    	page1Text += "communications issues."
    }
    page1Text += "\n\nd. UPDATE INSTALLATION DATA.  For upgrade installations, updates the legacy device "
    page1Text += "data tothe new data paradigm, insuring the device will work properly."

	return dynamicPage (name: "welcomePage", title: "Kasa Device Management Page", uninstall: true) {
		errorSection()
 		section("Instructions - select  '+'  to expand", hideable: true, hidden: true) {
            paragraph page1Text
		}
		section("Add Devices", hideable: true, hidden: true) {
        	href "addDevicesPage", title: "Install Kasa Devices", description: "Go to Install Devices"
        }
 		section("Set Device Preferences", hideable: true, hidden: true) {
			href "devicePreferencesPage", title: "Update Device Preferences", description: "Go to Set Device Preferences"
		}
        if (installType == "Kasa Account") {
			section("Update Kasa Account Credentials", hideable: true, hidden: true) {
				href "kasaAuthenticationPage", title: "Kasa Login and Token Update", description: "Go to Kasa Login Update"
            }
        } else {
			section("Update Node.js Hub IP", hideable: true, hidden: true) {
				href "hubEnterIpPage", title: "Node.js Hub IP = ${bridgeIp}", description: "Update Node.js Hub IP"
			}
        }
 		section("Update Installation Data", hideable: true, hidden: true) {
            href "updateInstallDataPage", title: "Update Installation Data", description: "Update data during upgrade process"
		}
        section() {
			paragraph "Select  '<'  at upper left corner to exit.\nSelect 'Remove' to remove application and all Kasa devices."
        }
	}
}

def kasaAuthenticationPage() {
    def page1Text = ""
    page1Text += "Enter you Kasa Account username and password below.  Select 'Save' at upper right hand corner.  "
    page1Text += "\nThe program will log into you Kasa account, capture the token for further interaction, and "
    page1Text += "install itself into SmartThings.  It will then exit.  \n\nTo continue adding devices or setting "
    page1Text += "prefences, open the app again."

	return dynamicPage (name: "kasaAuthenticationPage", title: "Initial Kasa Login Page", install: true) {
        errorSection()
 		section("Instructions - select  '+'  to expand", hideable: true, hidden: true) {
            paragraph page1Text
		}
		state.flowType = "updateKasaToken"
		section("Enter Kasa Account Credentials: ") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: true)
			input ("userPassword", "text", title: "Kasa Account Password", required: true, submitOnChange: true)
			paragraph "Select the text at upper right corner to update your Kasa credentials."
			paragraph "Select  '<'  at upper left corner to exit."
		}
	}
}

def hubEnterIpPage() {
    def page1Text = ""
    page1Text += "Enter the IP address of your node.js Hub.  IP format is NNN.NNN.NNN.NNN. Select 'Save' at upper "
    page1Text += "right hand corner.\nThe program set the Hup IP, capture device data and install itself into "
    page1Text += "SmartThings. It will then exit.  \n\nTo continue adding devices or setting prefences, open the app again."
	return dynamicPage (name: "hubEnterIpPage", title: "Set/Update Node IP", install: true) {
        errorSection()
 		section("Instructions - select  '+'  to expand", hideable: true, hidden: true) {
            paragraph page1Text
		}
		state.flowType = "updateNodeIp"
		section("") {
			input ("bridgeIp", "text", title: "Enter the Node.js Hub IP", required: true, multiple: false, submitOnChange: true)
            paragraph "Select the text at the upper right corner to update the IP."
            paragraph "Select  '<'  at upper left corner to exit."
		}
	}
}

def addDevicesPage() {
	def devices = state.devices
	def errorMsgDev = null
	def newDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceNetworkId)
		if (!isChild) {
			newDevices["${it.value.deviceNetworkId}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	traceLog("addDevicesPage: newDevices = ${newDevices}")
	if (devices == [:]) {
		errorMsgDev = "Looking for devices.  If this message persists, we have been unable to find " +
        "TP-Link devices on your wifi.  Check: 1) Hubitat Environment logs, 2) node.js logfile."
	} else if (newDevices == [:]) {
		errorMsgDev = "No new devices to add. Are you sure they are in Remote Control Mode?"
	}

    def page1Text = ""
    page1Text += "The manager has scanned your system for Kasa devices and removed already installed devices."
    page1Text += "The not installed devices are linked to the field below.  If there are none, then none were found."
    page1Text += "\na. Select the field below."
    page1Text += "\nb. Select the devices to add from the list."
    page1Text += "\nc. Select 'Done' in upper right."
    page1Text += "\nd. Select 'Save' in upper right to install the devices"

	return dynamicPage (name: "addDevicesPage", title: "Add Kasa Devices", install: true) {
        errorSection()
 		section("Instructions - select  '+'  to expand", hideable: true, hidden: true) {
            paragraph page1Text
		}
  		section("Select Devices to Add (${newDevices.size() ?: 0} found)", hideable: true) {
			input ("selectedAddDevices", "enum", required: true, multiple: true, submitOnChange: true, title: null, options: newDevices)
 			paragraph "To add devices, select the text at upper right corner."
			paragraph "Select  '<'  at upper left corner to exit."
        }
	}
}

def devicePreferencesPage() {
	def devices = state.devices
	def oldDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceNetworkId)
		if (isChild) {
			oldDevices["${it.value.deviceNetworkId}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	traceLog("devicePreferencePage: devices = ${oldDevices}")

    def page1Text = ""
    page1Text += "Select Devices to Update.  Selecting this field will cause a window with the"
    page1Text += "installed devices.  Select the devices for prefrence update and select 'Done'."
    page1Text += "\nYou must select at least one option to update."
    page1Text += "\na.  Lighting Transition Time.  Select the value from the pull-down.  This is "
    page1Text += "for BULBS and will be ignored for plugs and switches."
    page1Text += "\nb.  Device Refresh Rate.  Actually the minutes between refresh cycles.  It is "
    page1Text += "not recommended to change the default here unless you need to."
    page1Text += "\nc.  When done selecting and setting parameters, select 'Save'."

	return dynamicPage (name: "devicePreferencesPage", title: "Device Preferences Page", install: true) {
        errorSection()
 		section("Instructions - select  '+'  to expand", hideable: true, hidden: true) {
            paragraph page1Text
		}
		section("Device Configuration: ") {
			input ("selectedUpdateDevices", "enum",
            	required: true,
                multiple: true,
                submitOnChange: true,
                title: "Select Devices to Update (${oldDevices.size() ?: 0} found)",
	            options: oldDevices)
			input ("userLightTransTime", "number", 
            	required: false, 
                multiple: false, 
	            submitOnChange: false,
                title: "Lighting Transition Time in Seconds (Bulbs Only)")
			input ("userRefreshRate", "enum", 
            	required: false, 
                multiple: false,
                submitOnChange: false,
                title: "Device Refresh Rate",
                metadata: [values:["1" : "Refresh every minute", 
               					   "5" : "Refresh every 5 minutes", 
                                   "10" : "Refresh every 10 minutes", 
                                   "15" : "Refresh every 15 minutes",
                                   "30" : "Refresh every 30 minutes"]])
			paragraph "To update preferences, select the text at upper right corner."
			paragraph "Select  '<'  at upper left corner to exit."
		}
	}
}

def updateInstallDataPage() {
    def page1Text = ""
    page1Text += "This page is used to update the installation data.  NO input is required.  Then intention is "
	page1Text += "for this update to occur on initial installation only for upgrades."
    page1Text += "\n\nTo update the Installation Data, select 'Save' in the upper right."
     
	state.flowType = "updateInstallData"
	return dynamicPage (name: "updateInstallDataPage", title: "Initial Install Update Device Data", install: true) {
        errorSection()
 		section("Instructions", hideable: true, hidden: false) {
            paragraph page1Text
		}
		section("") {
            paragraph "Select the text at the upper right corner to update the Installation Data."
            paragraph "Select  '<'  at upper left corner to exit."
		}
	}
}

def flowDirector() {
	traceLog("flowDirector ${state.flowType}")
	switch(state.flowType) {
		case "updateNodeIp":
		    hubSendCommand("pollForDevices")
			break
		case "updateKasaToken":
	        getToken()
			break
        case "updateInstallData":
        	updateInstallData()
            break
        default:
        	break
    }
	state.flowType = "default"
}

def errorSection() {
	section("") {
		if (state.currentError != null) {
			paragraph "ERROR:  ${state.currentError}! Correct before continuing."
		} else if (errorMsgDev != null) {
			paragraph "ERROR:  ${errorMSgDev}"
		}
	}
}

//	===== Page Support Methods =====
def updateDevices(deviceNetworkId, alias, deviceModel, plugId, deviceId, appServerUrl, deviceIP) {
	def devices = state.devices
	def device = [:]
	device["deviceNetworkId"] = deviceNetworkId
	device["alias"] = alias
	device["deviceModel"] = deviceModel
	device["plugId"] = plugId
	device["deviceId"] = deviceId
	device["appServerUrl"] = appServerUrl
	device["deviceIP"] = deviceIP
	devices << ["${deviceNetworkId}" : device]
	def child = getChildDevice(deviceNetworkId)
	if (child) {
		def devVer = child.devVer()
        child.updateDataValue("deviceVersion", devVer)
		child.updateDataValue("appVersion", appVersion())
    	if (installType == "Kasa Account") {
			child.updateDataValue("appServerUrl", appServerUrl)
        } else {
            child.updateDataValue("hubVersion", state.hubVersion)
	        child.updateDataValue("deviceIP", deviceIP)
			child.updateDataValue("gatewayIP", bridgeIp)
		}
    }
	log.info "Device ${alias} added to devices array"
}

def addDevices() {
	traceLog("addDevices ${selectedAddDevices}")
	def tpLinkModel = [:]
	//	Plug-Switch Devices (no energy monitor capability)
	tpLinkModel << ["HS100" : "TP-Link Smart Plug"]
	tpLinkModel << ["HS103" : "TP-Link Smart Plug"]
	tpLinkModel << ["HS105" : "TP-Link Smart Plug"]
	tpLinkModel << ["HS200" : "TP-Link Smart Switch"]
	tpLinkModel << ["HS210" : "TP-Link Smart Switch"]
	tpLinkModel << ["KP100" : "TP-Link Smart Plug"]
	//	WiFi Range Extender with smart plug.
	tpLinkModel << ["RE270" : "TP-Link Smart RE Plug"]
	tpLinkModel << ["RE370" : "TP-Link Smart RE Plug"]
	//	Miltiple Outlet Plug
	tpLinkModel << ["HS107" : "TP-Link Smart Multi-Plug"]
	tpLinkModel << ["HS300" : "TP-Link Smart Multi-Plug"]
	tpLinkModel << ["KP200" : "TP-Link Smart Multi-Plug"]
	tpLinkModel << ["KP400" : "TP-Link Smart Multi-Plug"]
	//	Dimming Switch Devices
	tpLinkModel << ["HS220" : "TP-Link Smart Dimming Switch"]
	//	Energy Monitor Plugs
	tpLinkModel << ["HS110" : "TP-Link Smart Energy Monitor Plug"]
	//	Soft White Bulbs
	tpLinkModel << ["KB100" : "TP-Link Smart Soft White Bulb"]
	tpLinkModel << ["LB100" : "TP-Link Smart Soft White Bulb"]
	tpLinkModel << ["LB110" : "TP-Link Smart Soft White Bulb"]
	tpLinkModel << ["KL110" : "TP-Link Smart Soft White Bulb"]
	tpLinkModel << ["LB200" : "TP-Link Smart Soft White Bulb"]
	//	Tunable White Bulbs
	tpLinkModel << ["LB120" : "TP-Link Smart Tunable White Bulb"]
	tpLinkModel << ["KL120" : "TP-Link Smart Tunable White Bulb"]
	//	Color Bulbs
	tpLinkModel << ["KB130" : "TP-Link Smart Color Bulb"]
	tpLinkModel << ["LB130" : "TP-Link Smart Color Bulb"]
	tpLinkModel << ["KL130" : "TP-Link Smart Color Bulb"]
	tpLinkModel << ["LB230" : "TP-Link Smart Color Bulb"]

		def hub = location.hubs[0]
		def hubId = hub.id
	selectedAddDevices.each { dni ->
		try {
			def isChild = getChildDevice(dni)
			if (!isChild) {
				def device = state.devices.find { it.value.deviceNetworkId == dni }
				def deviceModel = device.value.deviceModel
			def deviceData
			if (installType == "Kasa Account") {
				deviceData = [
					"installType" : "Kasa Account",
					"deviceId" : device.value.deviceId,
					"plugId" : device.value.plugId,
					"appServerUrl" : device.value.appServerUrl,
					"appVersion" : appVersion()
				]
			} else {
				deviceData = [
					"installType" : "Node Applet",
					"deviceId" : device.value.deviceId,
					"plugId" : device.value.plugId,
					"deviceIP" : device.value.deviceIP,
					"gatewayIP" : bridgeIp,
                    "hubVersion": state.hubVersion,
					"appVersion" : appVersion()
				]
			}

				addChildDevice(
                	"davegut", 
                	tpLinkModel["${deviceModel}"],
                    device.value.deviceNetworkId,
                    hubId, [
                    	"label" : device.value.alias,
                    	"name" : deviceModel,
						"data" : deviceData
					]
                )
				log.info "Installed TP-Link ${deviceModel} ${device.value.alias}"
			}
		} catch (e) {
			log.debug "Error Adding ${deviceModel} ${device.value.alias}: ${e}"
		}
	}
}

def updatePreferences() {
	traceLog("updatePreferences ${selectedUpdateDevices}, ${userLightTransTime}, ${userRefreshRate}")
	selectedUpdateDevices.each {
		def child = getChildDevice(it)
        if (userLightTransTime) {
        	def deviceType = child.getTypeName()
			switch (deviceType) {
		    	case "TP-Link Smart Soft White Bulb":
		        case "TP-Link Smart Tunable White Bulb":
		        case "TP-Link Smart Color Bulb":
					def transTime = 1000*userLightTransTime
					child.updateDataValue("transTime", "${transTime}")
					break
		        default:
                	break
			}
        }
        if (userRefreshRate) { child.setRefreshRate(userRefreshRate) }
		log.info "Kasa device ${child} preferences updated"
	}
}

def updateInstallData() {
	traceLog("updateInstallData")
	state.flowType = "default"
	def devices = state.devices
	devices.each {
		def child = getChildDevice(it.value.deviceNetworkId)
        if (child) {
			def devVer = child.devVer()
			child.updateDataValue("appVersion", appVersion())
			def installType = child.getDataValue("installType")
			if (installType == "Node Applet") {
				child.updateDataValue("deviceIP", it.value.deviceIP)
	            child.updateDataValue("gatewayIP", bridgeIp)
	            child.updateDataValue("hubVersion",state.hubVersion)
            } else {
				child.updateDataValue("installType", "Kasa Account")
            }
            child.installed()
		}
    }
}

//	===== Kasa Account Methods ===========
def getToken() {
	traceLog("getToken ${userName}, ${userPassword}")
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

def kasaGetDevices() {
	traceLog("kasaGetDevices ${state.TpLinkToken}")
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
				state.currentError = null
		} else if (resp.status != 200) {
			state.currentError = resp.statusLine
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getDeviceData: ${state.currentError}"
            return
		} else if (resp.data.error_code != 0) {
			state.currentError = resp.data
			sendEvent(name: "currentError", value: resp.data)
			log.error "Error in getDeviceData: ${state.currentError}"
            return
		}
	}
	state.devices = [:]
	currentDevices.each {
		def deviceModel = it.deviceModel.substring(0,5)
        def plugId = ""
        def deviceIP = ""
		if (deviceModel == "HS107" || deviceModel == "HS300") {
			def totalPlugs = 2
			if (deviceModel == "HS300") {
				totalPlugs = 6
			}
			for (int i = 0; i < totalPlugs; i++) {
				def deviceNetworkId = "${it.deviceMac}_0${i}"
				plugId = "${it.deviceId}0${i}"
				def sysinfo = sendDeviceCmd(it.appServerUrl, it.deviceId, '{"system" :{"get_sysinfo" :{}}}')
				def children = sysinfo.system.get_sysinfo.children
				def alias
				children.each {
					if (it.id == plugId) {
						alias = it.alias
					}
				}
                updateDevices(deviceNetworkId, alias, deviceModel, plugId, it.deviceId, it.appServerUrl, deviceIP)
			}
		} else {
            updateDevices(it.deviceMac, it.alias, deviceModel, plugId, it.deviceId, it.appServerUrl, deviceIP)
		}
	}
}

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
				log.debug "state.errorCount = ${state.errorCount}"
			}
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

//	===== Node Applet Methods ============
def hubGetDevices() {
    hubSendCommand("pollForDevices")
}

def hubSendCommand(action) {
	traceLog("hubSendCommand ${bridgeIp}, ${action}")
	state.currentError = null
	runIn(10, createBridgeError)
	def headers = [:]
	headers.put("HOST", "${bridgeIp}:8082")	//	Same as on Hub.
	headers.put("command", action)
	sendHubCommand(new physicalgraph.device.HubAction([headers: headers], null, [callback: hubExtractData]))
}

def hubExtractData(response) {
	traceLog("hubExtractData")
    unschedule(createBridgeError)
	def action = response.headers["action"]
    if (action == "hubCheck") {
	    state.hubVersion = response.headers["cmd-response"]
	    hubSendCommand("pollForDevices")
    } else {
		def currentDevices =  parseJson(response.headers["cmd-response"])
	    if (currentDevices == []) {
	    	return 
	    } else if (currentDevices == "TcpTimeout") {
			log.error "Communications Error"
			sendEvent(name: "currentError", value: "TCP Timeout in Hub")
	        return
		}
		state.devices = [:]
		currentDevices.each {
		    def appServerUrl = ""
	        updateDevices(it.deviceMac, it.alias, it.deviceModel, it.plugId, it.deviceId, appServerUrl, it.deviceIP)
		}
    }
    log.info "Node Applet Hub Status: OK"
	state.currentError = null
	sendEvent(name: "currentError", value: null)
}

def createBridgeError() {
    log.error "Node Applet Bridge Status: Not Accessible"
	state.currentError = "Node Applet not acessible"
	sendEvent(name: "currentError", value: "Node Applet Not Accessible")
}

//	===== Utilities ============
def checkError() {
	if (state.currentError == null || state.currentError == "none") {
		log.info "No errors detected."
		if (state.currentError == "none") {
			state.currentError = null
		}
		return
	}
	def errMsg = state.currentError.msg
	log.info "Attempting to solve error: ${errMsg}"
	state.errorCount = state.errorCount +1
	if (errMsg == "Token expired" && state.errorCount < 6) {
		sendEvent (name: "ErrHandling", value: "Handle comms error attempt ${state.errorCount}")
		getDevices()
		if (state.currentError == null) {
			log.info "getDevices successful. apiServerUrl updated and token is good."
			return
		}
		log.error "${errMsg} error while attempting getDevices. Will attempt getToken"
		getToken()
		if (state.currentError == null) {
			log.info "getToken successful. Token has been updated."
			getDevices()
			return
		}
	} else {
		log.error "checkError: No auto-correctable errors or exceeded Token request count."
	}
	log.error "checkError residual: ${state.currentError}"
}

def installed() { initialize() }

def updated() { initialize() }

def initialize() {
	traceLog("initialize ${state.flowType}")
	unsubscribe()
	unschedule()
	if (installType == "Kasa Account"){
		schedule("0 30 2 ? * WED", getToken)
		runEvery5Minutes(checkError)
    } else if (installType == "Node Applet") { runEvery15Minutes(hubGetDevices) }
	if (selectedAddDevices) {
    	addDevices()
    } else if (selectedDeleteDevices) {
    	removeDevices()
    } else if (selectedUpdateDevices) {
    	updatePreferences()
    } else {
    	flowDirector()
    }
}

def uninstalled() { }

def traceLog(logMsg) {
	if (traceLogging() == true) { log.trace "${appVersion()} ${logMsg}" }
}

//end-of-file