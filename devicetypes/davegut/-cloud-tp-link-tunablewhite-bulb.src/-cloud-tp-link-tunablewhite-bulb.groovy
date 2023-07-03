/*	TP Link Bulbs Device Handler, 2018 Version 2
	Copyright 2018 Dave Gutheinz
Licensed under the Apache License, Version 2.0(the "License");
you may not use this  file except in compliance with the
License. You may obtain a copy of the License at:
	http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, 
software distributed under the License is distributed on an 
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific 
language governing permissions and limitations under the 
License.
Discalimer:  This Service Manager and the associated Device 
Handlers are in no way sanctioned or supported by TP-Link.  
All  development is based upon open-source data on the 
TP-Link devices; primarily various users on GitHub.com.
	===== History ============================================
2018-01-31	Update to Version 2
		a.	Common file content for all bulb implementations,
			using separate files by model only.
		b.	User file-internal selection of Energy Monitor
			function enabling.
	===== Bulb Identifier.  DO NOT EDIT ====================*/
	//def deviceType = "SoftWhite Bulb"	//	Soft White
	def deviceType = "TunableWhite Bulb"	//	ColorTemp
	//def deviceType = "Color Bulb"			//	Color
//	===== Hub or Cloud Installation ==========================
	def installType = "Cloud"
	//def installType = "Hub"
//	==========================================================

metadata {
	definition (name: "(${installType}) TP-Link ${deviceType}",
				namespace: "davegut",
				author: "Dave Gutheinz",
				deviceType: "${deviceType}",
				energyMonitor: "Standard",
				installType: "${installType}") {
		capability "Switch"
		capability "Switch Level"
		capability "refresh"
		capability "polling"
		capability "Sensor"
		capability "Actuator"
		if (deviceType == "TunableWhite Bulb" || "Color Bulb") {
			capability "Color Temperature"
			command "setModeNormal"
			command "setModeCircadian"
			attribute "bulbMode", "string"
		}
		if (deviceType == "Color Bulb") {
			capability "Color Control"
		}
	}
	tiles(scale:2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc",
				nextState:"waiting"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff",
				nextState:"waiting"
				attributeState "waiting", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#15EE10",
				nextState:"waiting"
				attributeState "commsError", label: 'Comms Error', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#e86d13",
				nextState:"waiting"
			}
			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label: "Brightness: ${currentValue}", action:"switch level.setLevel"
			}
			if (deviceType == "Color Bulb") {
				tileAttribute ("device.color", key: "COLOR_CONTROL") {
					attributeState "color", action:"setColor"
				}
			}
		}
		
		standardTile("refresh", "capability.refresh", width: 2, height: 1,  decoration: "flat") {
			state "default", label:"Refresh", action:"refresh.refresh"
		}
		
		if (deviceType == "TunableWhite Bulb") {
			controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 1, inactiveLabel: false,
			range:"(2500..6500)") {
				state "colorTemperature", action:"color temperature.setColorTemperature"
			}
		} else if (deviceType == "Color Bulb") {
			controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 1, inactiveLabel: false,
			range:"(2500..9000)") {
				state "colorTemperature", action:"color temperature.setColorTemperature"
			}
		}
		
		valueTile("colorTemp", "default", inactiveLabel: false, decoration: "flat", height: 1, width: 2) {
			state "default", label: 'Color\n\rTemperature'
		}
		
		standardTile("bulbMode", "bulbMode", width: 2, height: 1, decoration: "flat") {
			state "normal", label:'Circadian\n\rOFF', action:"setModeCircadian", nextState: "circadian"
			state "circadian", label:'Circadian\n\rOn', action:"setModeNormal", nextState: "normal"
		}

		main("switch")
		if (deviceType == "SoftWhite Bulb") {
			details("switch", "refresh")
		} else {
				details("switch", "colorTemp", "bulbMode", "refresh", 
						"colorTempSliderControl")
		}
	}

	def rates = [:]
	rates << ["5" : "Refresh every 5 minutes"]
	rates << ["10" : "Refresh every 10 minutes"]
	rates << ["15" : "Refresh every 15 minutes"]
	rates << ["30" : "Refresh every 30 minutes"]

	preferences {
		if (installType == "Hub") {
			input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
			input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
		}
		input name: "lightTransTime", type: "number", title: "Lighting Transition Time (seconds)", options: rates, description: "0 to 60 seconds", required: false
		input name: "refreshRate", type: "enum", title: "Refresh Rate", options: rates, description: "Select Refresh Rate", required: false
	}
}

//	===== Update when installed or setting changed =====
def installed() {
	update()
}

def updated() {
	runIn(2, update)
}

def update() {
	state.deviceType = metadata.definition.deviceType
	state.installType = metadata.definition.installType
	unschedule()
	switch(refreshRate) {
		case "5":
			runEvery5Minutes(refresh)
			log.info "Refresh Scheduled for every 5 minutes"
			break
		case "10":
			runEvery10Minutes(refresh)
			log.info "Refresh Scheduled for every 10 minutes"
			break
		case "15":
			runEvery15Minutes(refresh)
			log.info "Refresh Scheduled for every 15 minutes"
			break
		default:
			runEvery30Minutes(refresh)
			log.info "Refresh Scheduled for every 30 minutes"
	}
	if (lightTransTime >= 0 && lightTransTime <= 60) {
		state.transTime = 1000 * lightTransTime
	} else {
		state.transTime = 5000
	}
	runIn(2, refresh)
}

void uninstalled() {
	if (state.installType == "Cloud") {
		def alias = device.label
		log.debug "Removing device ${alias} with DNI = ${device.deviceNetworkId}"
		parent.removeChildDevice(alias, device.deviceNetworkId)
	}
}

//	===== Basic Bulb Control/Status =====
def on() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":1,"transition_period":${state.transTime}}}}""", "deviceCommand", "commandResponse")
}

def off() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":0,"transition_period":${state.transTime}}}}""", "deviceCommand", "commandResponse")
}

def setLevel(percentage) {
	percentage = percentage as int
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"brightness":${percentage},"transition_period":${state.transTime}}}}""", "deviceCommand", "commandResponse")
}

def setColorTemperature(kelvin) {
	kelvin = kelvin as int
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"color_temp": ${kelvin},"hue":0,"saturation":0}}}""", "deviceCommand", "commandResponse")
}

def setModeNormal() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"normal"}}}""", "deviceCommand", "commandResponse")
}

def setModeCircadian() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"circadian"}}}""", "deviceCommand", "commandResponse")
}

def setColor(Map color) {
	def hue = color.hue * 3.6 as int
	def saturation = color.saturation as int
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"color_temp":0,"hue":${hue},"saturation":${saturation}}}}""", "deviceCommand", "commandResponse")
}

def poll() {
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "deviceCommand", "commandResponse")
}

def refresh(){
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "deviceCommand", "commandResponse")
}

def commandResponse(cmdResponse){
	def status
	def respType = cmdResponse.toString().substring(1,10)
	if (respType == "smartlife") {
		status =  cmdResponse["smartlife.iot.smartbulb.lightingservice"]["transition_light_state"]
	} else {
		status = cmdResponse.system.get_sysinfo.light_state
	}
	def onOff = status.on_off
	if (onOff == 1) {
		onOff = "on"
	} else {
		onOff = "off"
		status = status.dft_on_state
	}
	def level = status.brightness
	def mode = status.mode
	def color_temp = status.color_temp
	def hue = status.hue
	def saturation = status.saturation
	log.info "$device.name $device.label: Power: ${onOff} / Brightness: ${level}% / Mode: ${mode} / Color Temp: ${color_temp}K / Hue: ${hue} / Saturation: ${saturation}"
	sendEvent(name: "switch", value: onOff)
 	sendEvent(name: "level", value: level)
	if (state.deviceType == "TunableWhite Bulb" || "Color Bulb") {
		sendEvent(name: "bulbMode", value: mode)
		sendEvent(name: "colorTemperature", value: color_temp)
	}
	if (state.deviceType == "Color Bulb") {
		sendEvent(name: "hue", value: hue)
		sendEvent(name: "saturation", value: saturation)
		sendEvent(name: "color", value: colorUtil.hslToHex(hue/3.6 as int, saturation as int))
	}
}

//	===== Send the Command =====
private sendCmdtoServer(command, hubCommand, action) {
	if (state.installType == "Cloud") {
		sendCmdtoCloud(command, hubCommand, action)
	} else {
		sendCmdtoHub(command, hubCommand, action)
	}
}

private sendCmdtoCloud(command, hubCommand, action){
	def appServerUrl = getDataValue("appServerUrl")
	def deviceId = getDataValue("deviceId")
	def cmdResponse = parent.sendDeviceCmd(appServerUrl, deviceId, command)
	String cmdResp = cmdResponse.toString()
	if (cmdResp.substring(0,5) == "ERROR"){
		def errMsg = cmdResp.substring(7,cmdResp.length())
		log.error "${device.name} ${device.label}: ${errMsg}"
		sendEvent(name: "switch", value: "commsError", descriptionText: errMsg)
		sendEvent(name: "deviceError", value: errMsg)
		action = ""
	} else {
		sendEvent(name: "deviceError", value: "OK")
	}
	actionDirector(action, cmdResponse)
}

private sendCmdtoHub(command, hubCommand, action){
	def headers = [:] 
	headers.put("HOST", "$gatewayIP:8082")	//	Same as on Hub.
	headers.put("tplink-iot-ip", deviceIP)
	headers.put("tplink-command", command)
	headers.put("action", action)
	headers.put("command", hubCommand)
	sendHubCommand(new physicalgraph.device.HubAction([
		headers: headers],
		device.deviceNetworkId,
		[callback: hubResponseParse]
	))
}

def hubResponseParse(response) {
	def action = response.headers["action"]
	def cmdResponse = parseJson(response.headers["cmd-response"])
	if (cmdResponse == "TcpTimeout") {
		log.error "$device.name $device.label: Communications Error"
		sendEvent(name: "switch", value: "offline", descriptionText: "ERROR - OffLine in hubResponseParse")
		sendEvent(name: "deviceError", value: "TCP Timeout in Hub")
	} else {
		actionDirector(action, cmdResponse)
		sendEvent(name: "deviceError", value: "OK")
	}
}

def actionDirector(action, cmdResponse) {
	switch(action) {
		case "commandResponse":
			commandResponse(cmdResponse)
			break
		default:
			log.info "Interface Error.  See SmartApp and Device error message."
	}
}

//	===== Child / Parent Interchange =====
def syncAppServerUrl(newAppServerUrl) {
	updateDataValue("appServerUrl", newAppServerUrl)
		log.info "Updated appServerUrl for ${device.name} ${device.label}"
}