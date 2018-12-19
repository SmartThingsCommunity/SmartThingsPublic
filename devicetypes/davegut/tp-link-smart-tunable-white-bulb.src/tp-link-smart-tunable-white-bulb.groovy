/*	TP Link Bulbs Device Handler, 2018 Version 3

	Copyright 2018 Dave Gutheinz and Anthony Ramirez

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

===== History ================================================
2018-10-23	Update to Version 3.5:
			a.	Compatibility with new SmartThings app.
            b.	Update capabilities per latest ST definitions
            	1.	deleted capability polling (depreciated)
                2.	deleted capability sensor (depreciated)
				3.	update program to accommodate other items
			c.	Various changes for updated Service Manager
           	With great appreciation to Anthony Ramirez for
            his assistance as well as leading the development
            of the new Service Manager.
12.07.18	3.5.03.  Corrected error in Refresh rate.
======== DO NOT EDIT LINES BELOW ===*/
//	===== Device Type Identifier =====
//	def deviceType()	{ return "Soft White Bulb" }
	def deviceType()	{ return "Tunable White Bulb" }
//	def deviceType()	{ return "Color Bulb" }
//	====== Device Namespace =====
	def devNamespace()	{ return "davegut" }
//	def devNamespace()	{ return "ramiran2" }
//	======== Other System Values =============================================================
	def devVer()	{ return "3.5.03" }
	def ocfValue()	{ return "oic.d.light" }
	def vidValue()	{ return "generic-rgbw-color-bulb" }
//	==========================================================================================

metadata {
	definition (name: "TP-Link Smart ${deviceType()}", 
    			namespace: "${devNamespace()}", 
                author: "Dave Gutheinz, Anthony Ramirez", 
                ocfDeviceType: "${ocfValue()}", 
                mnmn: "SmartThings", 
                vid: "${vidValue()}") {
		capability "Switch"
		capability "Switch Level"
		capability "refresh"
		capability "Health Check"
		if (deviceType() != "Soft White Bulb") {
			capability "Color Temperature"
			command "setModeNormal"
			command "setModeCircadian"
			attribute "circadianMode", "string"
		}
		if (deviceType() == "Color Bulb") {
			capability "Color Control"
			capability "Color Mode"
		}
		attribute "devVer", "string"
		attribute "devType", "string"
	}
	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action: "switch.off", icon: "st.Lighting.light13", backgroundColor: "#00a0dc",
				nextState: "waiting"
				attributeState "off", label:'${name}', action: "switch.on", icon: "st.Lighting.light13", backgroundColor: "#ffffff",
				nextState: "waiting"
				attributeState "waiting", label:'${name}', action: "switch.on", icon: "st.Lighting.light13", backgroundColor: "#15EE10",
				nextState: "waiting"
				attributeState "unavailable", label: 'unavailable', action: "switch.on", icon: "st.Lighting.light13", backgroundColor: "#e86d13",
				nextState: "waiting"
			}
			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", label: "Brightness: ${currentValue}", action: "switch level.setLevel"
			}
			if (deviceType() == "Color Bulb") {
				tileAttribute ("device.color", key: "COLOR_CONTROL") {
					attributeState "color", action: "setColor"
				}
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 1, decoration: "flat") {
			state "default", label: "Refresh", action: "refresh.refresh"
		}
		if (deviceType() == "Tunable White Bulb") {
			controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 1, inactiveLabel: false,
			range: "(2700..6500)") {
				state "colorTemperature", action: "color temperature.setColorTemperature"
			}
		} else if (deviceType() == "Color Bulb") {
			controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 2, height: 1, inactiveLabel: false,
			range: "(2500..9000)") {
				state "colorTemperature", action: "color temperature.setColorTemperature"
			}
		}
		valueTile("colorTemp", "default", inactiveLabel: false, decoration: "flat", height: 1, width: 2) {
			state "default", label: 'Color\n\rTemperature'
		}
		standardTile("circadianMode", "circadianMode", width: 2, height: 1, decoration: "flat") {
			state "normal", label:'Circadian\n\rOFF', action: "setModeCircadian", nextState: "circadian"
			state "circadian", label:'Circadian\n\rOn', action: "setModeNormal", nextState: "normal"
		}
		main("switch")
		if (deviceType() == "Soft White Bulb") {
			details("switch", "refresh")
		} else {
			details("switch", "colorTemp", "circadianMode", "refresh", "colorTempSliderControl")
		}
	}
    
    def transTime = [:]
    transTime << ["500" : "0.5 second"]
    transTime << ["1000" : "1 second"]
    transTime << ["1500" : "1.5 second"]
    transTime << ["2000" : "2 seconds"]
    transTime << ["5000" : "5 seconds"]
    transTime << ["10000" : "10 seconds"]
    
    def refreshRate = [:]
    refreshRate << ["1" : "Refresh every minute"]
    refreshRate << ["5" : "Refresh every 5 minutes"]
	refreshRate << ["10" : "Refresh every 10 minutes"]
    refreshRate << ["15" : "Refresh every 15 minutes"]

	preferences {
		input ("transition_Time", "enum", title: "Lighting Transition Time", options: transTime)
		input ("refresh_Rate", "enum", title: "Device Refresh Rate", options: refreshRate)
		input ("device_IP", "text", title: "Device IP (Hub Only, NNN.NNN.N.NNN)")
		input ("gateway_IP", "text", title: "Gateway IP (Hub Only, NNN.NNN.N.NNN)")
	}
}

//	===== Update when installed or setting changed =====
def installed() {
	log.info "Installing ${device.label}..."
    setRefreshRate(10)
    setLightTransTime("1000")
	if(getDataValue("installType") == null) {
		setInstallType("Node Applet")
	}
    update()
}

def ping() {
	refresh()
}

def update() {
    runIn(2, updated)
}

def updated() {
	log.info "Updating ${device.label}..."
	unschedule()
    if (getDataValue("installType") == null) { setInstallType("Node Applet") }
	if (refresh_Rate) { setRefreshRate(refresh_Rate) }
    if (transition_Time) { setLightTransTime(transitionTime) }
    if (device_IP) { setDeviceIP(device_IP) }
    if (gateway_IP) { setGatewayIP(gateway_IP) }
	sendEvent(name: "DeviceWatch-Enroll", value: groovy.json.JsonOutput.toJson(["protocol":"cloud", "scheme":"untracked"]), displayed: false)
	sendEvent(name: "devVer", value: devVer(), displayed: false)
	sendEvent(name: "devTyp", value: deviceType(), displayed: false)
	runIn(2, refresh)
}

void uninstalled() {
	try {
		def alias = device.label
		log.info "Removing device ${alias} with DNI = ${device.deviceNetworkId}"
		parent.removeChildDevice(alias, device.deviceNetworkId)
	} catch (ex) {
		log.info "${device.name} ${device.label}: No Parent Application.  Either the device was manually installed or there was an error"
	}
}

//	===== Basic Bulb Control/Status =====
def on() {
	def transTime = getDataValue("transTime")
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":1,"transition_period":${transTime}}}}""", "deviceCommand", "commandResponse")
}

def off() {
	def transTime = getDataValue("transTime")
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"on_off":0,"transition_period":${transTime}}}}""", "deviceCommand", "commandResponse")
}

def setLevel(percentage) {
	def transTime = getDataValue("transTime")
	setLevel(percentage, state.transTime)
}

def setLevel(percentage, rate) {
	if (percentage < 0 || percentage > 100) {
		log.error "$device.name $device.label: Entered brightness is not from 0...100"
		percentage = 50
	}
	percentage = percentage as int
	rate = rate.toInteger()
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"brightness":${percentage},"transition_period":${rate}}}}""", "deviceCommand", "commandResponse")
}

def setColorTemperature(kelvin) {
	if (kelvin == null) kelvin = state.lastColorTemp
	switch(deviceType()) {
		case "Tunable White Bulb" :
			if (kelvin < 2700) kelvin = 2700
			if (kelvin > 6500) kelvin = 6500
			break
		defalut:
			if (kelvin < 2500) kelvin = 2500
			if (kelvin > 9000) kelvin = 9000
	}
	kelvin = kelvin as int
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"color_temp": ${kelvin},"hue":0,"saturation":0}}}""", "deviceCommand", "commandResponse")
}

def setModeNormal() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"normal"}}}""", "deviceCommand", "commandResponse")
}

def setModeCircadian() {
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"mode":"circadian"}}}""", "deviceCommand", "commandResponse")
}

def setHue(hue) {
	if (hue == null) hue = state.lastHue
	saturation = state.lastSaturation
	setColor([hue: hue, saturation: saturation])
}

def setSaturation(saturation) {
	if (saturation == null) saturation = state.lastSaturation
	hue = state.lastHue
	setColor([hue: hue, saturation: saturation])
}

def setColor(Map color) {
	if (color == null) {
		setColor([hue: state.lastHue, saturation: state.lastSaturation])
		return
	}
	def hue = color.hue * 3.6 as int
	def saturation = color.saturation as int
	sendCmdtoServer("""{"smartlife.iot.smartbulb.lightingservice":{"transition_light_state":{"ignore_default":1,"on_off":1,"color_temp":0,"hue":${hue},"saturation":${saturation}}}}""", "deviceCommand", "commandResponse")
}

def refresh(){
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "deviceCommand", "refreshResponse")
}

def refreshResponse(cmdResponse){
	def status = cmdResponse.system.get_sysinfo.light_state
	def onOff = status.on_off
	if (onOff == 1) {
		onOff = "on"
	} else {
		onOff = "off"
		status = status.dft_on_state
	}
	sendEvent(name: "switch", value: onOff)
	def level = status.brightness
	sendEvent(name: "level", value: level)
	switch(deviceType()) {
		case "Soft White Bulb" :
			log.info "$device.name $device.label: Power: ${onOff} / Brightness: ${level}%"
			break
		case "Tunable White Bulb" :
			def circadianMode = status.mode
			def color_temp = status.color_temp
			sendEvent(name: "circadianMode", value: circadianMode)
			sendEvent(name: "colorTemperature", value: color_temp)
			state.lastColorTemp = color_temp
			log.info "$device.name $device.label: Power: ${onOff} / Brightness: ${level}% / Circadian Mode: ${circadianMode} / Color Temp: ${color_temp}K"
			break
		default:	//	Color Bulb
			def circadianMode = status.mode
			def color_temp = status.color_temp
			def hue = status.hue as int
			def saturation = status.saturation
			def color = [:]
			color << ["hue" : hue]
			color << ["saturation" : saturation]
			sendEvent(name: "circadianMode", value: circadianMode)
			sendEvent(name: "colorTemperature", value: color_temp)
			sendEvent(name: "hue", value: hue)
			sendEvent(name: "saturation", value: saturation)
			sendEvent(name: "color", value: color)
			if (color_temp.toInteger() == 0) {
				state.lastHue = scaledHue
				state.lastSaturation = saturation
				sendEvent(name: "colorMode", value: "color" ,descriptionText: descriptionText)
			} else {
				state.lastColorTemp = color_temp
				sendEvent(name: "colorMode", value: "colorTemperature" ,descriptionText: descriptionText)
			}
			log.info "$device.name $device.label: Power: ${onOff} / Brightness: ${level}% / Circadian Mode: ${circadianMode} / Color Temp: ${color_temp}K / Color: ${color}"
	}
}

//	===== Send the Command =====
private sendCmdtoServer(command, hubCommand, action) {
	try {
    	def installType = getDataValue("installType")
		if (installType == "Kasa Account") {
			sendCmdtoCloud(command, hubCommand, action)
		} else {
	    	def deviceIP = getDataValue("deviceIP")
	        def gatewayIP = getDataValue("gatewayIP")
			if (deviceIP =~ null && gatewayIP =~ null) {
				sendEvent(name: "switch", value: "unavailable", descriptionText: "Please input Device IP / Gateway IP")
				sendEvent(name: "deviceError", value: "No Hub Address Data")
				sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
			} else {
				sendCmdtoHub(command, hubCommand, action)
			}
		}
	} catch (ex) {
		log.error "Sending Command Exception: ${ex}.  Communications error with device."
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
	def headers = [:]
	headers.put("HOST", "$gatewayIP:8082")	//	Same as on Hub.
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
		log.error "$device.name $device.label: Communications Error"
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
		case "commandResponse" :
			refresh()
			break
		case "refreshResponse" :
			refreshResponse(cmdResponse)
			break
		default:
			log.info "Interface Error. See SmartApp and Device error message."
	}
}

//	===== Child / Parent Interchange =====
def setAppServerUrl(newAppServerUrl) {
	updateDataValue("appServerUrl", newAppServerUrl)
	log.info "Updated appServerUrl for ${device.name} ${device.label}"
}

def setLightTransTime(newTransTime) {
	updateDataValue("transTime", newTransTime)
	log.info "Light Transition Time for ${device.name} ${device.label} set to ${newTransTime} miliseconds"
}

def setRefreshRate(refreshRate) {
	switch(refreshRate) {
		case "1" :
			runEvery1Minute(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every minute"
			break
		case "5" :
			runEvery5Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 5 minutes"
			break
		case "10" :
			runEvery10Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 10 minutes"
			break
		case "15" :
			runEvery15Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 15 minutes"
			break
		default:
			runEvery10Minutes(refresh)
			log.info "${device.name} ${device.label} Refresh Scheduled for every 10 minutes"
	}
}

def setDeviceIP(deviceIP) { 
	updateDataValue("deviceIP", deviceIP) 	//	gatewayIP must be in form NNN.NNN.N.NNN
	log.info "${device.name} ${device.label} device IP set to ${deviceIP}"
}

def setGatewayIP(gatewayIP) { 
	updateDataValue("gatewayIP", gatewayIP) 	//	gatewayIP must be in form NNN.NNN.N.NNN
	log.info "${device.name} ${device.label} gateway IP set to ${gatewayIP}"
}

def setInstallType(installType) {
	updateDataValue("installType", installType)
	log.info "${device.name} ${device.label} Installation Type set to ${installType}"
}

//	===== GitHub Values =====
	def getDevImg(imgName)	{
    	return "https://raw.githubusercontent.com/${devNamespace()}/TP-Link-SmartThing/master/images/$imgName" 
    }
//end-of-file