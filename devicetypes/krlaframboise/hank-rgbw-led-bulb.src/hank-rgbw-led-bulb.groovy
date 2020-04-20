/**
 *  Hank RGBW LED Bulb v1.0.1
 *  (Model: HKZW-RGB01)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *    
 *
 *  Changelog:
 *
 *    1.0.1 (06/05/2018)
 *      - Fixed Power Recovery setting and made other bug fixes.
 *
 *    1.0 (05/31/2018)
 *      - Initial Release
 *
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

metadata {
	definition (name: "Hank RGBW LED Bulb", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch Level"
		capability "Switch"
		capability "Color Control"
		capability "Color Temperature"
		capability "Refresh"		
		capability "Configuration"

		command "reset"
		
		fingerprint mfr:"0208", prod:"0101", model:"0004", deviceJoinName:"Hank RGBW LED Bulb"
	}

	simulator {	}
	
	tiles(scale:2) {
			multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'TURNING ON', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'TURNING OFF', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"setColor"
			}
     }
	
		standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat", height: 2, width:2) {
			state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", height:2, width:2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
		standardTile("colorTempLabel", "device.generic", inactiveLabel: false, decoration: "flat", height:1, width:2) {
			state "default", label:"Temperature %"
		}
				
		controlTile("colorTempControl", "device.colorTemperature", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "colorTemperature", label: "Color Temp", action:"setColorTemperature"
		}
		
		controlTile("rgbSelector", "device.color", "color", height: 6, width: 6, inactiveLabel: false) {
			state "color", action:"setColor"
		}

		main(["switch"])
		details(["switch", "reset", "colorTempLabel", "refresh", "colorTempControl", "rgbSelector"])
	}
	
	preferences {		
		getConfigParamInput(powerRecoveryParam)
					
		input "toggleDuration", "enum",
			title: "Transition Speed:",
			defaultValue: "1",
			required: false,
			options: [
				["0": "Instant"],
				["1": "Fast [DEFAULT]"],
				["2": "Medium"],
				["3": "Slow"]
			]
		
		input "debugOutput", "bool", 
			title: "Enable Debug Logging?", 
			defaultValue: true, 
			required: false
	}
}

private getConfigParamInput(param) {
	input "configParam${param.num}", "enum",
		title: "${param.name}:",
		required: false,
		defaultValue: "${param.defaultValue}",
		options: param.options
}

private getToggleDurationSetting() {
	return safeToInt(settings?.toggleDuration, 1)
}


def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		logTrace "updated()..."
		state.lastUpdated = new Date().time
	
		initializeCheckin()
	
		if (hasSettingChanges()) { 
			def cmds = configure()
			return cmds ? response(cmds) : []
		}	
	}
}

private hasSettingChanges() {
	return configParams.find {  (it.value != state["configVal${it.num}"]) } ? true : false
}

private initializeCheckin() {
	def checkInterval = (6 * 60 * 60) + (5 * 60)
	
	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	
	startHealthPollSchedule()
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	runEvery3Hours(healthPoll)
}

def healthPoll() {
	logTrace "healthPoll()"	
	sendHubCommand([new physicalgraph.device.HubAction(versionGetCmd())])
}


def ping() {
	logTrace "ping()"
	// Don't allow it to ping the device more than once per minute.
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		logDebug "Attempting to ping device."
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()
		
		return [versionGetCmd()]
	}	
}


def configure() {
	logDebug "configure()..."
	def cmds = []	
	configParams.each {
		cmds << configSetCmd(it)
		cmds << configGetCmd(it)
	}		
	return delayBetween(cmds, 1000)
}


def on() {
	logDebug "on()..."	
	def level = (device.currentValue("level") ?: 99)
	return [switchMultilevelSetCmd(level, toggleDurationSetting)]
}

def off() {
	logDebug "off()..."
	return [switchMultilevelSetCmd(0, toggleDurationSetting)]
}


def setLevel(level) {
	logDebug "setLevel($level)..."
	return setLevel(level, toggleDurationSetting)
}

def setLevel(level, duration) {
	logDebug "setLevel($level, $duration)..."
	if (duration > 3) {
		duration = 3
	}
	return [switchMultilevelSetCmd(level, duration)]
}


def refresh() {
	logDebug "refresh()..."
	return [basicGetCmd()]
} 
  
def reset() {
	logDebug "reset()..."
	return setColorTemperature(device.currentValue("colorTemperature") ?: 3000)
}

def setSaturation(percent) {
	logDebug "setSaturation($percent)..."
	return setColor([saturation: percent])
}

def setHue(value) {
	logDebug "setHue($value)..."
	return setColor([hue: value])	
}


def setColor(value) {
	logDebug "setColor($value)..."
  def result = []
	def rgb
	def hue
	def saturation
	if (value.hex) {
		rgb = "${value.hex}".findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) }
		
		def hsv = rgbToHSV(rgb[0], rgb[1], rgb[2])
		if (hsv) {
			hue = hsv.hue
			saturation = hsv.saturation
		}		
	}
	else {
		hue = value.hue != null ? value.hue : device.currentValue("hue")
    saturation = value.saturation != null ? value.saturation : device.currentValue("saturation")
    
		if (hue == null) hue = 13
    if (saturation == null) saturation = 13
        
		rgb = huesatToRGB(hue, saturation)
	}
	
	if (hue != null) {
		sendEvent(name: "hue", value: hue)
	}
	
	if (saturation != null) {
		sendEvent(name: "saturation", value: saturation)
	}
	
	if (value.hex) {
		sendEvent(name: "color", value: value.hex)
	}
	    	
	return [switchColorSetCmd(rgb[0], rgb[1], rgb[2], 0, 0)]
}

def setColorTemperature(temperature) {
	logDebug "setColorTemperature($temperature)..."
	temperature = safeToInt(temperature,50)
	if (temperature > 100) {
		// Temperature should be percentage, but some integrations use kelvin so convert kelvin into percentage.
		if (temperature > 7000) temperature = 7000
		if (temperature < 2000) temperature = 2000

		temperature = safeToInt((1 - ((7000 - temperature) / 5000)) * 100)
		
		logDebug "Kelvin temperature converted to ${temperature}%"
	}
	
	if (temperature < 1) temperature = 1
	
	def val = safeToInt(255 * (temperature / 100))
	def warmVal = 255 - val
	def coolVal = 0 + val
	
	sendEvent(name: "colorTemperature", value: temperature)

	return [switchColorSetCmd(0, 0, 0, warmVal, coolVal)]
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private basicSetCmd(val) {
	return secureCmd(zwave.basicV1.basicSet(value: val))
}

private basicGetCmd() {
	return secureCmd(zwave.basicV1.basicGet())
}

private switchMultilevelSetCmd(level, duration) {
	def levelVal = validateRange(level, 99, 0, 99)
	
	def durationVal = validateRange(duration, defaultDimmingDurationSetting, 0, 100)
			
	return secureCmd(zwave.switchMultilevelV3.switchMultilevelSet(dimmingDuration: durationVal, value: levelVal))
}

private switchColorSetCmd(r, g, b, warmWhite, coldWhite) {
	return secureCmd(zwave.switchColorV3.switchColorSet(red: r, green: g, blue: b, warmWhite: warmWhite, coldWhite: coldWhite)) 
}

private switchColorGetCmd() {
	return secureCmd(zwave.switchColorV3.switchColorGet())
}

private configSetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size,scaledConfigurationValue: param.value))
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

private secureCmd(cmd) {
	if (zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}	
}

private getConfigParams() {
	return [
		basicReportParam,
		powerRecoveryParam
	]
}

private getBasicReportParam() {
	return getParam(24, "Load Status Change Notification", 1, 1, [
			["0": "Disabled"],
			["1": "Send Basic Report"],
			["2": "Send Basic Report for Phyiscal Reports"]
		])
}

private getPowerRecoveryParam() {
	return getParam(21, "Power Failure Recovery", 1, 1, [
			["0": "Remember Last State"],
			["1": "On"],
			["2": "Off"]
		])
}

private getParam(num, name, size, defaultVal, options) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal) 
	
	def map = [
		num: num, 
		name: name, 
		size: size, 
		defaultValue: defaultVal, 
		value: val
	]
	
	map.options = options?.collect {
		it.collect { k, v ->
			if ("${k}" == "${defaultVal}") {
				v = "${v} [DEFAULT]"		
			}
			["$k": "$v"]
		}
	}.flatten()	
	
	return map
}


def parse(description) {	
	def result = null
	
	if (description != "updated") {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)			
		} 
		else {
			logDebug("Couldn't zwave.parse '$description'")
		}
	}
	
	if (!isDuplicateCommand(state.lastCheckinTime, 60000)) {
		sendLastCheckinEvent()
	}
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)	
	
	def result = []
	if (encapsulatedCmd) {
		result += zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
	return result
}

private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x26: 3,	// Switch Multilevel
		0x27: 1,	// All Switch
		0x2B: 1,	// Scene Activation
		0x2C: 1,	// Scene Actuator Configuration
		0x33: 1,	// Color Control
		0x59: 1,	// AssociationGrpInfo
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x70: 1,	// Configuration
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x7A: 2,	// Firmware Update Md
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x98: 1		// Security
	]
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport $cmd"
	
	def val = cmd.configurationValue[0]
	state["configVal${cmd.parameterNumber}"] = val
	logDebug "Parameter #${cmd.parameterNumber} = ${val}"
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: $cmd"	
	// Using this event for health monitoring to update lastCheckin
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: $cmd"
	def value = cmd.value ? "on" : "off"
	
	if (device.currentValue("switch") != value) {
		sendEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned ${value}")
	}
	
	if (cmd.value && device.currentValue("level") != cmd.value) {
		sendEvent(name: "level", value: cmd.value, unit: "%", descriptionText: "$device.displayName set to ${cmd.value}%")
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logTrace "unhandled: $cmd"
	return []
}



private rgbToHSV(red, green, blue) {
    float r = red / 255f
    float g = green / 255f
    float b = blue / 255f
    float max = [r, g, b].max()
    float delta = max - [r, g, b].min()
    def hue = 13
    def saturation = 0
    if (max && delta) {
        saturation = 100 * delta / max
        if (r == max) {
            hue = ((g - b) / delta) * 100 / 6
        } else if (g == max) {
            hue = (2 + (b - r) / delta) * 100 / 6
        } else {
            hue = (4 + (r - g) / delta) * 100 / 6
        }
    }
    [hue: hue, saturation: saturation, value: max * 100]
}

private huesatToRGB(float hue, float sat) {
    while(hue >= 100) hue -= 100
    int h = (int)(hue / 100 * 6)
    float f = hue / 100 * 6 - h
    int p = Math.round(255 * (1 - (sat / 100)))
    int q = Math.round(255 * (1 - (sat / 100) * f))
    int t = Math.round(255 * (1 - (sat / 100) * (1 - f)))
    switch (h) {
        case 0: return [255, t, p]
        case 1: return [q, 255, p]
        case 2: return [p, 255, t]
        case 3: return [p, q, 255]
        case 4: return [t, p, 255]
        case 5: return [255, p, q]
    }
}

private validateRange(val, defaultVal, lowVal, highVal) {
	val = safeToInt(val, defaultVal)
	if (val > highVal) {
			return highVal
	}
	else if (val < lowVal) {
		return lowVal
	}
	else {
		return val
	}
}

private safeToInt(val, defaultVal=0) {
	if ("${val}"?.isInteger()) {
		return "${val}".toInteger()
	}
	else if ("${val}".isDouble()) {
		return "${val}".toDouble()?.round()
	}
	else {
		return  defaultVal
	}
}

private sendLastCheckinEvent() {
	state.lastCheckinTime = new Date().time
	logDebug "Device Checked In"	
	sendEvent(name: "lastCheckin", value: convertToLocalTimeString(new Date()), displayed: false)
}

private convertToLocalTimeString(dt) {
	def timeZoneId = location?.timeZone?.ID
	if (timeZoneId) {
		return dt.format("MM/dd/yyyy hh:mm:ss a", TimeZone.getTimeZone(timeZoneId))
	}
	else {
		return "$dt"
	}	
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

private logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}

private logTrace(msg) {
	// log.trace "$msg"
}