/*	TP Link Plugs and Switches Device Handler, 2019 Version 4

	Copyright 2018, 2019 Dave Gutheinz and Anthony Ramirez

Licensed under the Apache License, Version 2.0(the "License"); you may not use this  file except in compliance with the
License. You may obtain a copy of the License at:

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific 
language governing permissions and limitations under the License.

Discalimer:  This Service Manager and the associated Device Handlers are in no way sanctioned or supported by TP-Link.  
All  development is based upon open-source data on the TP-Link devices; primarily various users on GitHub.com.

===== History ================================================
02.28.19	4.0.01	Update to production version - single file per device type.
					Updated Service Manager to Device communications.
======== DO NOT EDIT LINES BELOW ===========================*/
	def devVer()	{ return "4.0.01" }
metadata {
	definition (name: "TP-Link Smart Switch", 
    			namespace: "davegut", 
                author: "Dave Gutheinz, Anthony Ramirez", 
                ocfDeviceType: "oic.d.switch", 
                mnmn: "SmartThings", 
                vid: "generic-switch") {
		capability "Switch"
		capability "refresh"
		capability "Health Check"
	}
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action: "switch.off", icon: "st.Home.home30", backgroundColor: "#00a0dc",
				nextState: "waiting"
				attributeState "off", label:'${name}', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#ffffff",
				nextState: "waiting"
				attributeState "waiting", label:'${name}', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#15EE10",
				nextState: "waiting"
				attributeState "Unavailable", label:'Unavailable', action: "switch.on", icon: "st.Home.home30", backgroundColor: "#e86d13",
				nextState: "waiting"
			}
			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 1, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh.refresh"
		}
		main("switch")
		details("switch", "refresh")
	}
    
    def refreshRate = [:]
    refreshRate << ["1" : "Refresh every minute"]
    refreshRate << ["5" : "Refresh every 5 minutes"]
	refreshRate << ["10" : "Refresh every 10 minutes"]
    refreshRate << ["15" : "Refresh every 15 minutes"]
    refreshRate << ["30" : "Refresh every 30 minutes"]

	preferences {
		input ("refresh_Rate", "enum", title: "Device Refresh Rate", options: refreshRate)
		input ("device_IP", "text", title: "Device IP (Hub Only, NNN.NNN.N.NNN)")
		input ("gateway_IP", "text", title: "Hub IP (Hub Only, NNN.NNN.N.NNN)")
	}
}

//	===== Update when installed or setting changed =====
def installed() {
	log.info "Installing ${device.label}..."
    updateDataValue("refreshRate", "30")
	if(getDataValue("installType") == null) { updateDataValue("installType", "Manual") }
	device.updateSetting("refreshRate",[type:"text", value:""])
    update()
}

def update() {
    runIn(2, updated)
}

def updated() {
	log.info "Updating ${device.label}..."
	unschedule()

    //	Capture legacy refresh rate data
	if (refresh_Rate) { 
    	setRefreshRate(refresh_Rate)
    } else {
    	setRefreshRate(getDataValue("refreshRate"))
    }

    if (device_IP) { updateDataValue("deviceIP", device_IP) }
    if (gateway_IP) { updateDataValue("gatewayIP", gateway_IP) }
	sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
    if (getDataValue("installType") == "Manual") { updateDataValue("deviceDriverVersion", devVer())  }
	runIn(2, refresh)
}

//	===== Basic Plug Control/Status =====
def on() {
	sendCmdtoServer("""{"system" :{"set_relay_state" :{"state" : 1}}}""", "deviceCommand", "commandResponse")
}

def off() {
	sendCmdtoServer("""{"system" :{"set_relay_state" :{"state" : 0}}}""", "deviceCommand", "commandResponse")
}

def refresh(){
	sendCmdtoServer('{"system" :{"get_sysinfo" :{}}}', "deviceCommand", "refreshResponse")
}

def refreshResponse(cmdResponse){
	def onOffState = cmdResponse.system.get_sysinfo.relay_state
	if (onOffState == 1) {
		sendEvent(name: "switch", value: "on")
		log.info "${device.label}: Power: on"
	} else {
		sendEvent(name: "switch", value: "off")
		log.info "${device.label}: Power: off"
	}
}

//	===== Send the Command =====
private sendCmdtoServer(command, hubCommand, action) {
	try {
		if (getDataValue("installType") == "Kasa Account") {
			sendCmdtoCloud(command, hubCommand, action)
		} else {
			sendCmdtoHub(command, hubCommand, action)
		}
	} catch (ex) {
		log.error "${device.label}: Sending Command Exception: ${ex}.  Communications error with device."
	}
}

private sendCmdtoCloud(command, hubCommand, action){
	def appServerUrl = getDataValue("appServerUrl")
	def deviceId = getDataValue("deviceId")
	def cmdResponse = parent.sendDeviceCmd(appServerUrl, deviceId, command)
	String cmdResp = cmdResponse.toString()
	if (cmdResp.substring(0,5) == "ERROR"){
		def errMsg = cmdResp.substring(7,cmdResp.length())
		log.error "${device.label}: ${errMsg}"
		sendEvent(name: "switch", value: "unavailable", descriptionText: errMsg)
		sendEvent(name: "deviceError", value: errMsg)
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
		action = ""
	} else {
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
		sendEvent(name: "deviceError", value: "OK")
	}
	actionDirector(action, cmdResponse)
}

private sendCmdtoHub(command, hubCommand, action){
	def gatewayIP = getDataValue("gatewayIP")
    def deviceIP = getDataValue("deviceIP")
	if (deviceIP =~ null && gatewayIP =~ null) {
		sendEvent(name: "switch", value: "unavailable", descriptionText: "Please input Device IP / Gateway IP")
		sendEvent(name: "deviceError", value: "No Hub Address Data")
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
		log.error "${device.label}: Invalid IP.  Please check and update."
	}
	def headers = [:]
	headers.put("HOST", "$gatewayIP:8082")
	headers.put("tplink-iot-ip", deviceIP)
	headers.put("tplink-command", command)
	headers.put("action", action)
	headers.put("command", hubCommand)
	sendHubCommand(new physicalgraph.device.HubAction([headers: headers], device.deviceNetworkId, [callback: hubResponseParse]))
}

def hubResponseParse(response) {
	def action = response.headers["action"]
	def cmdResponse = parseJson(response.headers["cmd-response"])
	if (cmdResponse == "TcpTimeout") {
		log.error "${device.label}: Communications Error"
		sendEvent(name: "switch", value: "offline", descriptionText: "ERROR - OffLine in hubResponseParse")
		sendEvent(name: "deviceError", value: "TCP Timeout in Hub")
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
	} else {
		sendEvent(name: "deviceError", value: "OK")
		sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
		actionDirector(action, cmdResponse)
	}
}

def actionDirector(action, cmdResponse) {
	switch(action) {
		case "commandResponse":
			refresh()
			break
		case "refreshResponse":
			refreshResponse(cmdResponse)
			break
		default:
			log.info "${device.label}: Interface Error.	See SmartApp and Device error message."
	}
}

//	===== Child / Parent Interchange =====
def setRefreshRate(refreshRate) {
	updateDataValue("refreshRate", refreshRate)
	switch(refreshRate) {
		case "1" :
			runEvery1Minute(refresh)
			log.info "${device.label}: Refresh Scheduled for every minute."
			break
		case "5" :
			runEvery5Minutes(refresh)
			log.info "${device.label}: Refresh Scheduled for every 5 minutes."
			break
		case "10" :
			runEvery10Minutes(refresh)
			log.info "${device.label}: Refresh Scheduled for every 10 minutes."
			break
		case "15" :
			runEvery15Minutes(refresh)
			log.info "${device.label}: Refresh Scheduled for every 15 minutes."
			break
		default:
			runEvery30Minutes(refresh)
			log.info "${device.label}: Refresh Scheduled for every 30 minutes."
	}
}

//end-of-file