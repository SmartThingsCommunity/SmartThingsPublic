/*
TP-Link SmartThings Manager and TP-Link Cloud Connect, 2018 Version 4

Lite version with less information and no gitHub Icons.

	Copyright 2018 Dave Gutheinz, Anthony Ramirez
    
Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at:

	http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.
Discalimer: This Service Manager and the associated Device
Handlers are in no way sanctioned or supported by TP-Link. All
development is based upon open-source data on the TP-Link Kasa Devices;
primarily various users on GitHub.com.
	====== Application History ================================
11-14-2015	Finalized version 3.5 for public release.  Changes
			a.  Added support for hub-based device installation and
            	management.
            b.	Added capability to set device preferences (not
            	available in new phone app).
            c.	Added capability to remove devices.
            d.	Reworked HMI and improved information.
12-0302018	Updated to finalize multi-plug integration and
			(attempt) to ease login issues.
	====== Application Information ==========================*/
	def textCopyright()	{ return "Copyright� 2018 - Dave Gutheinz, Anthony Ramirez" }
	def appNamespace() { return "davegut" }
	def appLabel() { return "TP-Link SmartThings Manager (lite)" }
	def appVersion() { return "3.5.02" }
	def appVerDate() { return "11-02-2018" }
//	===========================================================

definition (
	name: appLabel(), 
	namespace: appNamespace(), 
	author: "Dave Gutheinz, Anthony Ramirez", 
	description: "TP-Link/Kasa Service Manager for both cloud and hub connected devices.", 
	category: "Convenience", 
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	singleInstance: true
)

preferences {
	page(name: "startPage")
	page(name: "welcomePage")
	page(name: "hubEnterIpPage")
	page(name: "kasaAuthenticationPage")
	page(name: "kasaAddDevicesPage")
	page(name: "hubAddDevicesPage")
	page(name: "removeDevicesPage")
	page(name: "uninstallPage")
	page(name: "devicePreferencesPage")
}

def setInitialStates() {
	if (!state.TpLinkToken) { state.TpLinkToken = null }
	if (!state.devices) {state.devices = [:]}
	if (!state.currentError) {state.currentError = null}
	if (!state.errorCount) {state.errorCount = 0}
	settingRemove("userSelectedDevicesRemove")
	settingRemove("userSelectedDevicesAdd")
	settingRemove("userSelectedDevicesToUpdate")
	if ("${userName}" =~ null) {settingRemove("userName")}
	if ("${userPassword}" =~ null) {settingRemove("userPassword")}
}

def startPage() {
	setInitialStates()
    if (installType) {
		if (installType == "Kasa Account" && userName == null && password == null) {
			return kasaAuthenticationPage()       	
		} else if (installType == "Node Applet" && bridgeIp == null) {
			return hubEnterIpPage()
		} else {
        	return welcomePage()
        }
    }
    def page1Text = ""
	def page2Text = ""
    def page3Text = ""
    def page4Text = ""
    def page5Text = ""
    def page6Text = ""
	page1Text = "Before the installation, the installation type must be entered.  There are two options:"
    page2Text += "Kasa Account:  This is the cloud based entry.  It requires that the user provide "
    page2Text += "enter their Kasa Account login and password to install the devices."
    page3Text += "Node Applet:  This installation requires several items. (1) An always on server (PC, Android, "
    page3Text += "or other device).  (2) The provided node.js applet up and running on the Server.  (3) Static "
    page3Text += "IP addresses for the server (bridge/hub).  It does not require login credentials."
    page4Text += "After selecting the mode, you will be sent a page to enter your Kasa Login Information "
    page4Text += "or enter the IP address of your Hub."
    page5Text += "Once you enter the connection information, the program will direct you to add devices. "
    page5Text += "The application will poll for your devices, allow you to select found devices, and "
    page5Text += "finally install the devices and application to SmartThings."
    page6Text += "Once the application is already installed, you not see this page.  Instead you will be"
    page6Text += "directed to a page where you can select add devices, remove devices, and set device preferences."
	return dynamicPage (name: "startPage", title: "Select Installation Type", install: false, uninstall: false) {
		section("") {
			paragraph appLabel(), image: appLogo()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: error()
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
            paragraph page2Text
            paragraph page3Text
            paragraph page4Text
            paragraph page5Text
            paragraph page6Text
		}
        
		section("") {
			input ("installType", "enum", title: "Select Installation Type", required: true, multiple: false, 
            	   submitOnChange: true, metadata: [values:["Kasa Account", "Node Applet"]])
 		}
        
		section("${textCopyright()}")
	}
}

//	----- Main first (landing) Pages -----
def kasaAuthenticationPage() {
	def page1Text = ""
	def page2Text = ""
	def page3Text = ""
	page1Text += "If possible, open the IDE and select Live Logging. Then, "
	page1Text += "enter your Username and Password for the Kasa Application."
	page2Text += "After entering all credentials, select 'Install Devices to Continue'.  "
	page2Text += "This will call the Add Devices page."
	page3Text += "You must select and add a device to install the application!"
	return dynamicPage (name: "kasaAuthenticationPage", 
    		title: "Initial Kasa Login Page", 
            nextPage: "kasaAddDevicesPage", 
            install: false, 
            uninstall: false) {
		section("") {
			paragraph appLabel(), image: appLogo()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: error()
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
            paragraph page2Text
            paragraph page3Text
		}
            
		section("Enter Kasa Account Credentials: ") {
			input ("userName", "email", title: "TP-Link Kasa Email Address", required: true, submitOnChange: false)
			input ("userPassword", "text", title: "Kasa Account Password", required: true, submitOnChange: true)
		}
        
		section("") {
			if (state.currentError != null) {
				paragraph "Error! Exit program and try again after resolving problem. ${state.currentError}!", image: error()
			} else if (userName != null && userPassword != null) {
 				href "kasaAddDevicesPage", title: "Install Devices to Continue!", description: "Go to Install Devices"
			}
		}
        
		section("${textCopyright()}")
	}
}

def hubEnterIpPage() {
	def page1Text = "If possible, open the IDE and select Live Logging. Then, " +
		"enter the static IP address for your gateway that runs the Node.js applet.  Assure "+
		"that the node.js applet is running and logging to the display."
    
	return dynamicPage (name: "hubEnterIpPage", title: "Set Gateway IP", nextPage: "",install: false, uninstall: false) {
		section("") {
			paragraph appLabel(), image: appLogo()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: error()
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
		}
            
		section("") {
			input ("bridgeIp", 
				"text", 
                title: "Enter the Gateway IP", 
                required: true, 
                multiple: false, 
                submitOnChange: true,
                image: network()
            )
            if (bridgeIp) {
				href "hubAddDevicesPage", title: "Install Devices to Continue!", description: "Go to Install Devices"
			}
		}
        
		section("${textCopyright()}")
	}
}

def welcomePage() {
	def page1Text = ""
	def page2Text = ""
	def page3Text = ""
	def page4Text = ""
	def page5Text = ""
	page1Text += "Welcome to the new SmartThings application for TP-Link Kasa Devices. "
	page1Text += "Options for this page:"
    page2Text += "Install Devices:  Detects devices on your network that are not installed "
    page2Text += "and then installs them.  For Cloud-based installation, the devices must "
    page2Text += "be in remote control mode via the Kasa Application"
    page3Text += "Remove Devices:  Allow selection of installed devices for removal from SmartThings."
    page4Text += "Set Device Preferences.  Allows selection of installed devices and setting "
    page4Text += "the refresh rate (all devices) and the light transition time (bulbs).  "
    page4Text += "If not set here, the refresh rate is 30 minutes and the transition time is 1 second."
    page5Text += "Maintenance Functions.  Provides for options to (1) Update the Kasa Token "
    page5Text += "(cloud mode), (2) change the Hub IP address (hub version), or (3) uninstall "
    page5Text += "all devices and the application"
	return dynamicPage (name: "welcomePage", title: "Program Options", install: false, uninstall: false) {
		section("") {
			paragraph appLabel(), image: appLogo()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: error()
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
            paragraph page2Text
            paragraph page3Text
            paragraph page4Text
            paragraph page5Text
		}
            
		section("Device Functions") {
        	if (installType == "Kasa Account") {
				href "kasaAddDevicesPage", title: "Install Kasa Devices", description: "Go to Install Devices"
            } else {
				href "hubAddDevicesPage", title: "Install Hub Devices", description: "Go to Install Devices"
            }
			href "removeDevicesPage", title: "Remove Devices", description: "Go to Remove Devices"
			href "devicePreferencesPage", title: "Set Device Preferences", description: "Go to Set Device Preferences"
		}
        
		section("Maintenance Actions") {
            if (installType == "Kasa Account") {
				href "kasaAuthenticationPage", title: "Kasa Login and Token Update", description: "Go to Kasa Login Update"
            }	else {
				href "hubEnterIpPage", title: "Update Gateway IP", description: "Update Gateway IP"
			}
			href "uninstallPage", title: "Uninstall Page", description: "Uninstall All"
		}
        
		section("${textCopyright()}")
	}
}

//	----- Device Control Pages -----
def kasaAddDevicesPage() {
	getToken()
	kasaGetDevices()
	def devices = state.devices
	def errorMsgDev
	def newDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceNetworkId)
		if (!isChild) {
			newDevices["${it.value.deviceNetworkId}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (devices == [:]) {
		errorMsgDev = "We were unable to find any TP-Link Kasa devices on your account. This usually means "+
		"that all devices are in 'Local Control Only'. Correct them then " + "rerun the application."
	}
	if (newDevices == [:]) {
		errorMsgDev = "No new devices to add. Are you sure they are in Remote " + "Control Mode?"
	}
	def page1Text = "Devices that have not been previously installed and are not in 'Local " +
		"WiFi control only' will appear below. Tap below to see the list of " +
		"TP-Link Kasa Devices available select the ones you want to connect to " +
		"SmartThings.\n" + "Press Done when you have selected the devices you " +
		"wish to add, then press Save to add the devices to your SmartThings account."
	return dynamicPage (name: "kasaAddDevicesPage", title: "Kasa Device Installer Page", install: true, uninstall: false) {
		section("") {
			paragraph appLabel(), image: appLogo()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: error()
            } else if (errorMsgDev != null) {
				paragraph "ERROR:  ${errorMSgDev}", image: error()
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
		}
            
 		section("") {
			input ("userSelectedDevicesAdd", 
            	   "enum", 
                   required: true, 
                   multiple: true, 
                   submitOnChange: true,
                   title: "Select Devices to Add (${newDevices.size() ?: 0} found)", 
                   metadata: [values:newDevices])
		}
        
		section("${textCopyright()}")
	}
}

def hubAddDevicesPage() {
	hubGetDevices()
	def devices = state.devices
	def errorMsgDev
	def newDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceNetworkId)
		if (!isChild) {
			newDevices["${it.value.deviceNetworkId}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (devices == [:]) {
		errorMsgDev = "Looking for devices.  If this message persists, we have been unable to find " +
        "TP-Link devices on your wifi.  Check: 1) SmartThings logs, 2) node.js logfile for "
	} else if (newDevices == [:]) {
		errorMsgDev = "No new devices to add. Check: 1) Device installed to Kasa properly, " +
        "2) The SmartThings MyDevices (in case already installed)."
	}
    def page1Text = ""
    def page2Text = ""
    def page3Text = ""
    def page4Text = ""
    def page5Text = ""
    def page6Text = ""
	page1Text = "This page installs the devices through the running node applet. "
    page1Text += "On initial installation, an error will be displayed until the "
    page1Text += "node applet returns the device data."
    page2Text = "1.  Assure that the node applet is running."
    page3Text = "2.  Wait for a device count equal to your devices to appear in "
    page3Text += "'Selet Devices to Add' string."
    page4Text = "3.  Select the devices you want to install."
    page5Text = "4.  Select 'DONE' in upper right corner to install the devices and "
    page5Text += "install the Application."
    page6Text = "5.  To cancel, select '<' in the upper left corner."
	return dynamicPage(name:"hubAddDevicesPage",
		title:"Node Applet: Add TP-Link Devices",
		nextPage:"",
		refreshInterval: 10,
        multiple: true,
		install:true,
		uninstall:false) {
		section("") {
			paragraph appLabel(), image: appLogo()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: error()
            } else if (errorMsgDev != null) {
				paragraph "ERROR:  ${errorMSgDev}", image: error()
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
            paragraph page2Text
            paragraph page3Text
            paragraph page4Text
            paragraph page5Text
            paragraph page6Text
		}
        
 		section("") {
			input ("userSelectedDevicesAdd", 
            	   "enum", 
                   required: true, 
                   multiple: true, 
		  		   refreshInterval: 10,
                   submitOnChange: true,
                   title: "Select Devices to Add (${newDevices.size() ?: 0} found)", 
                   metadata: [values:newDevices])
		}
    
		section("${textCopyright()}")
	}
}

def removeDevicesPage() {
	def devices = state.devices
	def errorMsgDev
	def oldDevices = [:]
	devices.each {
		def isChild = getChildDevice(it.value.deviceNetworkId)
		if (isChild) {
			oldDevices["${it.value.deviceNetworkId}"] = "${it.value.alias} model ${it.value.deviceModel}"
		}
	}
	if (devices == [:]) {
		errorMsgDev = "Devices database was cleared in-error.  Run Device Installer Page to correct " +
        "then try again.  You can also remove devices using the SmartThings IDE or either version " +
        "of the phone app."
	}
	if (oldDevices == [:]) {
		errorMsgDev = "There are no devices to remove from the SmartThings app at this time.  This " +
        "implies no devices are installed."
	}
	def page1Text = "Devices that have been installed " +
		"will appear below. Tap below to see the list of " +
		"TP-Link Kasa Devices available select the ones you want to connect to " +
		"SmartThings.\n" + "Press Done when you have selected the devices you " +
		"wish to remove, then Press Save to remove the devices to your SmartThings account."
	return dynamicPage (name: "removeDevicesPage", title: "Device Uninstaller Page", install: true, uninstall: false) {
		section("") {
			paragraph appLabel(), image: appLogo()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: error()
            } else if (errorMsgDev != null) {
				paragraph "ERROR:  ${errorMSgDev}", image: error()
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
		}
        
        
		section("") {
			input ("userSelectedDevicesRemove", "enum", required: true, multiple: true, 
            	submitOnChange: false, title: "Select Devices to Remove (${oldDevices.size() ?: 0} found)", 
                metadata: [values:oldDevices])
		}
        
		section("${textCopyright()}")
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

	def page1Text = ""
    def page2Text = ""
    def page3Text = ""
    page1Text += "Transition Time.  The light transition time that allows the light to fade on and off. "
    page1Text += "Applies to bulbs only.  If filled-in, is ignored for plugs and switches."
    page2Text += "Refresh Rate.  Allows selecting a refresh rate other than the default of every 30 minutes."
    page3Text += "Saving or Exiting.  This page is saved by selecting the 'SAVE' in the upper righht hand "
    page3Text += "top of the page.  To exit w/o saving, use the '<' in the upper left hand corner."
    
	return dynamicPage (name: "devicePreferencesPage", title: "Device Preferences Page", install: true, uninstall: false) {
		section("") {
			paragraph "${appLabel()}", image: appLabel()
			if (state.currentError != null) {
				paragraph "ERROR:  ${state.currentError}! Correct before continuing.", image: getAppImg("error.png")
			} else {
				paragraph "No detected program errors!"
            }
		}
        
		section("Information and Instructions: ", hideable: true, hidden: true) {
            paragraph page1Text
            paragraph page2Text
            paragraph page3Text
		}
        
		section("Device Configuration: ") {
			input ("userSelectedDevicesToUpdate", "enum", required: true, multiple: true, 
	            submitOnChange: false, title: "Select Devices to Update (${oldDevices.size() ?: 0} found)", 
	            metadata: [values: oldDevices])
			input ("userLightTransTime", "enum", required: true, multiple: false, 
	            submitOnChange: false, title: "Lighting Transition Time", 
            metadata: [values:["500" : "0.5 second", "1000" : "1 second", "1500" : "1.5 second", "2000" : "2 seconds", "2500" : "2.5 seconds", "5000" : "5 seconds", "10000" : "10 seconds"]])
				input ("userRefreshRate", "enum", required: true, multiple: false, 
 	           submitOnChange: false, title: "Device Refresh Rate", metadata: [values:["1" : "Refresh every minute", "5" : "Refresh every 5 minutes", "10" : "Refresh every 10 minutes", "15" : "Refresh every 15 minutes"]])
            paragraph "Save options: select 'NEXT' (upper right).  Return: select '<' (upper left)."
		}

		section("${textCopyright()}")
	}
}

//	----- Maintenance Pages -----
def uninstallPage() {
	def page1Text = "This will uninstall the All Child Devices including this Application with all it's user data. \nPlease make sure that any devices created by this app are removed from any routines/rules/smartapps before tapping Remove."
	dynamicPage (name: "uninstallPage", title: "Uninstall Page", install: false, uninstall: true) {
		section("") {
			paragraph appLabel(), image: appLogo()
		}
        
		section("Instructions", hideable: true, hidden: true) {
            paragraph page1Text
		}
            
		section("${textCopyright()}")
        
		remove("Uninstall this application", "Warning!!!", "Last Chance to Stop! \nThis action is not reversible \n\nThis will remove All Devices including this Application with all it's user data")
	}
}

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

//	----- Get Device Data -----
def kasaGetDevices() {
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

def hubGetDevices() {
	runIn(10, createBridgeError)
	def headers = [:]
	headers.put("HOST", "${bridgeIp}:8082")	//	Same as on Hub.
	headers.put("command", "pollForDevices")
	sendHubCommand(new physicalgraph.device.HubAction([headers: headers], null, [callback: hubExtractDeviceData]))
}

def hubExtractDeviceData(response) {
	def currentDevices =  parseJson(response.headers["cmd-response"])
    if (currentDevices == []) {
    	return
    }
	state.devices = [:]
	currentDevices.each {
	    def appServerUrl = ""
        updateDevices(it.deviceMac, it.alias, it.deviceModel, it.plugId, it.deviceId, appServerUrl, it.deviceIP)
	}
    unschedule(createBridgeError)
	state.currentError = null
	sendEvent(name: "currentError", value: null)
}

def createBridgeError() {
    log.info "Node Applet Bridge Status: Not Accessible"
	state.currentError = "Node Applet not acessible"
	sendEvent(name: "currentError", value: "Node Applet Not Accessible")
}

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
	def isChild = getChildDevice(deviceNetworkId)
	if (isChild) {
    	if (installType == "Kasa Account") {
			isChild.setAppServerUrl(appServerUrl)
        } else {
			isChild.setDeviceIP(deviceIP)
			isChild.setGatewayIP(bridgeIp)
		}
    }
	log.info "Device ${alias} added to devices array"
}

//	----- ACTION PAGES. Add, Delete, Update Devices.  Remove App -----
def addDevices() {
	def tpLinkModel = [:]
	//	Plug-Switch Devices (no energy monitor capability)
	tpLinkModel << ["HS100" : "TP-Link Smart Plug"]
	tpLinkModel << ["HS103" : "TP-Link Smart Plug"]
	tpLinkModel << ["HS105" : "TP-Link Smart Plug"]
	tpLinkModel << ["HS200" : "TP-Link Smart Switch"]
	tpLinkModel << ["HS210" : "TP-Link Smart Switch"]
	tpLinkModel << ["KP100" : "TP-Link Smart Plug"]
	//	Miltiple Outlet Plug
	tpLinkModel << ["HS107" : "TP-Link Smart Multi-Plug"]
	tpLinkModel << ["HS300" : "TP-Link Smart Multi-Plug"]
	tpLinkModel << ["KP200" : "TP-Link Smart Multi-Plug"]
	tpLinkModel << ["KP400" : "TP-Link Smart Multi-Plug"]
	//	Dimming Switch Devices
	tpLinkModel << ["HS220" : "TP-Link Smart Dimming Switch"]
	//	Energy Monitor Plugs
	tpLinkModel << ["HS110" : "TP-Link Smart Energy Monitor Plug"]
	tpLinkModel << ["HS115" : "TP-Link Smart Energy Monitor Plug"]
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
    def devices = state.devices
	userSelectedDevicesAdd.each { dni ->
		try {
			def isChild = getChildDevice(dni)
			if (!isChild) {
				def device = devices.find { it.value.deviceNetworkId == dni }
				def deviceModel = device.value.deviceModel.substring(0,5)
				addChildDevice(
                	"${appNamespace()}", 
                	tpLinkModel["${deviceModel}"],
                    device.value.deviceNetworkId,
                    hubId, [
                    	"label" : device.value.alias,
                    	"name" : deviceModel,
                    	"data" : [
                        	"deviceId" : device.value.deviceId, 
                            "appServerUrl" : device.value.appServerUrl,
                            "installType" : installType,
                            "deviceIP" : device.value.deviceIP,
                            "gatewayIP" : bridgeIp,
                            "plugId" : device.value.plugId
                        ]
                    ]
                )
				log.info "Installed TP-Link $deviceModel with alias ${device.value.alias}"
                
				if (userSelectedNotification) {
					sendPush("Successfully installed TP-Link $deviceModel with alias ${device.value.alias}")
				}
			}
		} catch (e) {
			log.debug "Error Adding ${deviceModel}: ${e}"
		}
	}
}

def removeDevices() {
	userSelectedDevicesRemove.each { dni ->
		try{
			def isChild = getChildDevice(dni)
			if (isChild) {
				def delete = isChild
				delete.each { deleteChildDevice(it.deviceNetworkId, true) }
			}
			if (userSelectedNotification) {
				sendPush("Successfully uninstalled TP-Link $deviceModel with alias ${device.value.alias}")
			}
		} catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
		}
	}
}

def updatePreferences() {
	userSelectedDevicesToUpdate.each {
		def child = getChildDevice(it)
		child.setLightTransTime(userLightTransTime)
		child.setRefreshRate(userRefreshRate)
		log.info "Kasa device ${child} preferences updated"
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
				log.debug "state.errorCount = ${state.errorCount}	//	state.currentError = ${state.currentError}"
			}
		//log.debug "state.errorCount = ${state.errorCount}		//	state.currentError = ${state.currentError}"
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

//	----- INSTALL, UPDATE, INITIALIZE, UNINSTALLED -----
def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	unsubscribe()
	unschedule()
	if (installType == "Kasa Account"){
		schedule("0 30 2 ? * WED", getToken)
		runEvery5Minutes(checkError)
    }
    if (installType == "Node Applet") { runEvery5Minutes(hubGetDevices) }
	if (userSelectedDevicesAdd) { addDevices() }
	if (userSelectedDevicesRemove) { removeDevices() }
	if (userSelectedDevicesToUpdate) { updatePreferences() }
}

def uninstalled() {
}

//	----- PERIODIC CLOUD and HUB TASKS -----
def checkError() {
	if (state.currentError == null || state.currentError == "none") {
		log.info "${appLabel()} did not find any errors."
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

def removeChildDevice(alias, deviceNetworkId) {
	try {
		deleteChildDevice(it.deviceNetworkId)
		sendEvent(name: "DeviceDelete", value: "${alias} deleted")
	} catch (Exception e) {
		sendEvent(name: "DeviceDelete", value: "Failed to delete ${alias}")
	}
}

void settingUpdate(name, value, type=null) {
	log.trace "settingUpdate($name, $value, $type)..."
	if(name) {
		if(value == "" || value == null || value == []) {
			settingRemove(name)
			return
		}
	}
	if(name && type) {
		app?.updateSetting("$name", [type: "$type", value: value])
	}
	else if (name && type == null) { app?.updateSetting(name.toString(), value) }
}

void settingRemove(name) {
	log.trace "settingRemove($name)..."
	if(name) { app?.deleteSetting("$name") }
}

def network() {return "https://s3.amazonaws.com/smartapp-icons/Internal/network-scanner.png"}

def error() {return "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-Panic.png"}

def appLogo() {return "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png"}

def appInfoDesc() {return "� ${textVersion()}  \n\r� ${textModified()}"}
//end-of-file