/**
 *  Aeotec Doorbell v 1.6
 *  (https://community.smartthings.com/t/release-aeon-labs-aeotec-doorbell/39166/16?u=krlaframboise)
 *
 *  Capabilities:
 *	  Switch, Alarm, Tone, Battery, Configuration, Refresh
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *  1.6 (06/23/2016)
 *    - Bug fix for implicit int to string cast
 *
 *  1.5 (03/24/2016)
 *    - UI Enhancements			
 *
 *  1.4 (02/28/2016)
 *    - UI Enhancements and fixed fingerprint so that
 *      it doesn't conflict with the Aeon Labs
 *      Multifunction Siren.
 *
 *  1.3 (02/21/2016)
 *    -	UI Enhancements/Fixes, added Refresh capability.
 *
 *  1.2 (02/17/2016)
 *    -	Fixed bug causing error on install.
 *
 *  1.1 (02/15/2016)
 *    -	Consolidated code.
 *
 *  1.0 (02/14/2016)
 *    -	Initial Release
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
	definition (name: "Aeotec Doorbell", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Configuration"
		capability "Switch"
		capability "Alarm"
		capability "Tone"
		capability "Battery"
		capability "Refresh"

		attribute "alarmTrack", "number"
		attribute "beepTrack", "number"
		attribute "doorbellTrack", "number"
		attribute "repeat", "number"
		attribute "status", "enum", ["off", "doorbell", "beep", "alarm", "play"]
		attribute "volume", "number"

		command "playTrack", ["number"]
		command "alarmUp"
		command "setAlarmTrack", ["number"]
		command "alarmDown"
		command "beepUp"
		command "setBeepTrack", ["number"]
		command "beepDown"
		command "doorbellDown"		
		command "setDoorbellTrack", ["number"]
		command "doorbellUp"		
		command "setRepeat", ["number"]
		command "setVolume", ["number"]

		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A", outClusters: "0x82"
	}

	simulator {
	}

	preferences {
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: false, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "off", icon:"st.Home.home30", backgroundColor:"#ffffff"
				attributeState "doorbell", label:'Doorbell Ringing!', action: "off", icon:"st.Home.home30", backgroundColor:"#99c2ff"
				attributeState "alarm", label:'Alarm!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99FF99"
				attributeState "play", label:'Playing!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#694489"
			}
		}	
		standardTile("playDoorbell", "device.status", label: 'Doorbell', width: 2, height: 2) {
			state "default", 
				label:'Doorbell', 
				action:"on", 
				icon:"st.Home.home30", 
				backgroundColor: "#99c2ff"
			state "doorbell",
				label:'Ringing',
				action:"off",
				icon:"",
				background: "#ffffff"				
		}
		standardTile("playBeep", "device.status", label: 'Beep', width: 2, height: 2) {
			state "default", 
				label:'Beep', 
				action:"beep", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor: "#99FF99"
			state "beep", 
				label:'Beeping', 
				action:"off", 
				icon:"", 
				backgroundColor: "#ffffff"
		}		
		standardTile("playAlarm", "device.alarm", label: 'Alarm', width: 2, height: 2) {
			state "default", 
				label:'Alarm', 
				action: "both", 
				icon:"st.alarm.alarm.alarm", 
				backgroundColor: "#ff9999"
			state "both", 
				label:'Sounding', 
				action: "off", 
				icon:"", 
				backgroundColor: "#ffffff"
		}
		valueTile("doorbellTrack", "device.doorbellTrack", decoration: "flat", width:2, height: 2) {
			state "doorbellTrack", label: 'DOORBELL: ${currentValue}'
		}		
		controlTile("doorbellSlider", "device.doorbellTrack", "slider", width: 3, height: 2, range: "(1..100)") {
			state "doorbellTrack", action:"setDoorbellTrack"
		}
		valueTile("doorbellTrackUp", "device.doorbellTrack", width: 1, height: 1) {
			state "default", 
			label:'', 
			action:"doorbellUp",
			icon: "st.thermostat.thermostat-up"
		}		
		valueTile("doorbellTrackDown", "device.doorbellTrack", width: 1, height: 1) {
			state "default", 
			label:'', 
			action:"doorbellDown",
			icon: "st.thermostat.thermostat-down"
		}
		valueTile("beepTrack", "device.beepTrack", decoration: "flat", height:2, width:2) {
			state "beepTrack", label: 'BEEP: ${currentValue}'
		}
		controlTile("beepSlider", "device.beepTrack", "slider", width: 3, height: 2, range: "(1..100)") {
			state "beepTrack", action:"setBeepTrack"
		}
		valueTile("beepTrackUp", "device.beepTrack", width: 1, height: 1) {
			state "default", 
			label:'', 
			action:"beepUp",
			icon: "st.thermostat.thermostat-up"
		}		
		valueTile("beepTrackDown", "device.beepTrack", width: 1, height: 1) {
			state "default", 
			label:'', 
			action:"beepDown",
			icon: "st.thermostat.thermostat-down"
		}		
		valueTile("alarmTrack", "device.alarmTrack", decoration: "flat", height:2, width:2) {
			state "alarmTrack", label: 'ALARM: ${currentValue}'
		}
		controlTile("alarmSlider", "device.alarmTrack", "slider", width: 3, height: 2, range: "(1..100)") {
			state "alarmTrack", action:"setAlarmTrack"
		}
		valueTile("alarmTrackUp", "device.alarmTrack", width: 1, height: 1) {
			state "default", 
			label:'', 
			action:"alarmUp",
			icon: "st.thermostat.thermostat-up"
		}
		valueTile("alarmTrackDown", "device.alarmTrack", width: 1, height: 1) {
			state "default", 
			label:'', 
			action:"alarmDown",
			icon: "st.thermostat.thermostat-down"
		}		
		valueTile("volume", "device.volume", decoration: "flat", height:1, width:2) {
			state "volume", label: 'VOLUME: ${currentValue}'
		}
		controlTile("volumeSlider", "device.volume", "slider", height: 1, width: 4, range: "(1..10)") {
			state "volume", action:"setVolume"
		}
		valueTile("repeat", "device.repeat", decoration: "flat", height:1, width:2) {
			state "repeat", label: 'REPEAT: ${currentValue}'
		}
		controlTile("repeatSlider", "device.repeat", "slider", height: 1, width: 4, range: "(1..25)") {
			state "repeat", action:"setRepeat"
		}
		valueTile("battery", "device.battery", decoration: "flat", height:2, width:2) {
			state "default", label: 'Battery\n${currentValue}%'
		}
		standardTile("refresh", "device.refresh", label: 'Refresh', width: 2, height: 2) {
			state "default", label:'', action: "refresh", icon:"st.secondary.refresh"
		}		
		main "status"
		details(["status", "playDoorbell", "playBeep", "playAlarm", "doorbellTrack", "doorbellSlider", "doorbellTrackUp", "doorbellTrackDown", "beepTrack", "beepSlider", "beepTrackUp", "beepTrackDown", "alarmTrack", "alarmSlider", "alarmTrackUp", "alarmTrackDown", "volume", "volumeSlider", "repeat", "repeatSlider", "battery", "refresh"])
	}
}

// Stops playing track and raises events switch.off, alarm.off, status.off
def off() {
	secureDelayBetween([
		deviceNotifyTypeSetCmd(true),
		zwave.basicV1.basicSet(value: 0x00)])
}

// Plays doorbellTrack and raises switch.on event
def on() {
	sendDoorbellEvents()
	secureDelayBetween([
		deviceNotifyTypeSetCmd(false),
		zwave.basicV1.basicSet(value: 0xFF),
		deviceNotifyTypeSetCmd(true)])
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def strobe() {
	both()
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def siren() {
	both()
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def both() {
	playTrack(getNumAttr("alarmTrack"), "alarm", "Alarm Sounding!")
}

// Plays beepTrack and raises status.beep event
def beep() {
	playTrack(getNumAttr("beepTrack"), "beep", "Beeping!")
}

// Plays specified track and raises status.play event
def playTrack(track) {
	playTrack(track, "play", "Playing Track $track!")
}

// Plays specified track and raises specified status event
def playTrack(track, status, desc) {
	logDebug("$desc")
	def descText = "${device.displayName} $desc"

	if (status == "alarm") {
		sendEvent(name: "alarm", value: "both", descriptionText: descText)
	}

	sendEvent(name: "status", value: status, descriptionText: descText, isStateChange: true)

	track = validateTrack(track)
	secureDelayBetween([
		deviceNotifyTypeSetCmd(false),
		configSetCmd(6, track),
		deviceNotifyTypeSetCmd(true)])
}

// Re-loads attributes from device configuration.
def refresh() {
	def result = []
	result += configure()
	result += refreshDeviceAttr()
	return result
}

private refreshDeviceAttr() {
	secureDelayBetween([
		doorbellGetCmd(),
		repeatGetCmd(),
		volumeGetCmd(),			
		batteryHealthGetCmd()
	])
}

// Handles device reporting on and off
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	if (cmd.value == 0) {
		handleDeviceTurningOff()
	} 
	else if (cmd.value == 255) {
		sendDoorbellEvents()
	}
}

// Raises events switch.off, alarm.off, and status.off
def handleDeviceTurningOff() {
	[
		createEvent(name:"status", value: "off", isStateChange: true),
		createEvent(name:"alarm", value: "off", descriptionText: "$device.displayName alarm is off", isStateChange: true, displayed: false),
		createEvent(name:"switch", value: "off", descriptionText: "$device.displayName switch is off", isStateChange: true, displayed: false)
	]
}

private sendDoorbellEvents() {
	def desc = "Doorbell Ringing!"
	logDebug("$desc")
	sendEvent(name: "status", value: "doorbell", descriptionText: "${device.displayName} $desc", isStateChange: true)
	sendEvent(name: "switch", value: "on", displayed: false, isStateChange: true)
}

// Checks battery level if hasn't been checked recently
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	logDebug("WakeUpNotification: $cmd")

	def result = []
	result << createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)

	// Request every 24 hours
	if (!state.lastBatteryReport || (new Date().time) - state.lastBatteryReport > 24*60*60*1000) {
		result << response(batteryHealthGetCmd())
		result << response("delay 1200")
	}
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	result
}

// Raises battery event and writes level to Info Log
private batteryHealthReport(cmd) {
	state.lastBatteryReport = new Date().time
	def batteryValue = (cmd.configurationValue == [0]) ? 100 : 1
	def batteryLevel = (batteryValue == 100) ? "normal" : "low"

	sendEvent(name: "battery", value: batteryValue, unit: "%", descriptionText: "$batteryLevel", isStateChange: true)
	logInfo("Battery: $batteryValue")
}

// Writes parameter settings to Info Log
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	def name = null
	switch (cmd.parameterNumber) {
		case 8:
			sendAttrChangeEvent("volume", cmd.configurationValue[0])
			break
		case 5:
			sendAttrChangeEvent("doorbellTrack", cmd.configurationValue[0])
			break
		case 2:
			sendAttrChangeEvent("repeat", cmd.configurationValue[0])
			break			
		case 80:
			name = "Device Notification Type"
			break
		case 81:
			name = "Send Low Battery Notifications"
			break
		case 42:
			batteryHealthReport(cmd)
			break
		default:
			name = "Parameter #${cmd.parameterNumber}"
	}
	if (name) {
		logInfo("${name}: ${cmd.configurationValue}")
	} 
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug("Unhandled: $cmd")
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

// Parses incoming message warns if not paired securely
def parse(String description) {
	def result = null	
	if (description.startsWith("Err 106")) {
		state.useSecureCommands = false
		def msg = "This sensor failed to complete the network security key exchange. You may need to remove and re-add the device or disable Use Secure Commands in the settings"
		log.warn "$msg"
		result = createEvent( name: "secureInclusion", value: "failed", isStateChange: true, descriptionText: "$msg")
	}
	else if (description != null && description != "updated") {	
		def cmd = zwave.parse(description, [0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		} 
		else {
			logDebug("No Command: $cmd")
		}
	}
	result
}

// Unencapsulates the secure command.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	if (cmd != null) {
		def encapCmd = cmd.encapsulatedCommand([0x25: 1, 0x26: 1, 0x27: 1, 0x32: 3, 0x33: 3, 0x59: 1, 0x70: 1, 0x72: 2, 0x73: 1, 0x7A: 2, 0x82: 1, 0x85: 2, 0x86: 1])
		if (encapCmd) {
			zwaveEvent(encapCmd)

		} else {
			log.warn "Unable to extract encapsulated cmd from $cmd"
			createEvent(descriptionText: cmd.toString())
		}
	}
}

// Sends configuration to device
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 500)) {		
		state.lastUpdated = new Date().time
		state.debugOutput = validateBool(debugOutput, true)
		
		logDebug("Updating")
		
		setAlarmTrack(getNumAttr("alarmTrack"))
		setBeepTrack(getNumAttr("beepTrack"))
	
		response(refreshDeviceAttr())
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

// Sends secure configuration to device
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.useSecureCommands = true
	logDebug("Secure Commands Supported")
}

// Initializes variables and sends settings to device
def configure() {
	state.debugOutput = validateBool(debugOutput, true)
	state.useSecureCommands = null
	
	logDebug "Sending configuration to ${device.displayName}"
		
	secureDelayBetween([
		supportedSecurityGetCmd(),
		assocSetCmd(),
		deviceNotifyTypeSetCmd(true),		
		sendLowBatterySetCmd()
	])
}

// Sets volume attribute and device setting
def setVolume(volume) {
	volume = validateRange(volume, 5, 1, 10)
	secureDelayBetween([
		configSetCmd(8, volume),
		volumeGetCmd()
	])
}

// Sets repeat attribute and device setting
def setRepeat(repeat) {
	repeat = validateRange(repeat, 1, 1, 300)	
	secureDelayBetween([
		configSetCmd(2, repeat),
		repeatGetCmd()
	])
}

// Increments doorbellTrack.
def doorbellUp() {
	setDoorbellTrack(getNumAttr("doorbellTrack") + 1)
}

// Decrements doorbellTrack
def doorbellDown() {
	setDoorbellTrack(getNumAttr("doorbellTrack") - 1)
}

// Sets doorbellTrack attribute and setting
def setDoorbellTrack(track) {
	track = validateTrack(track)
	secureDelayBetween([
		configSetCmd(5, track),
		doorbellGetCmd()
	])
}

// Increments beepTrack
def beepUp(){
	setBeepTrack(getNumAttr("beepTrack") + 1)
}

// Decrements beepTrack
def beepDown(){
	setBeepTrack(getNumAttr("beepTrack") - 1)
}

// Sets beepTrack attribute
def setBeepTrack(track) {
	sendAttrChangeEvent("beepTrack", validateTrack(track))
}

// Decrements alarmTrack
def alarmDown() {
	setAlarmTrack(getNumAttr("alarmTrack") - 1)
}

// Increments alarmTrack
def alarmUp() {
	setAlarmTrack(getNumAttr("alarmTrack") + 1)
}

// Sets alarmTrack attribute
def setAlarmTrack(track) {
	sendAttrChangeEvent("alarmTrack", validateTrack(track))
}

private sendAttrChangeEvent(attrName, attrVal) {
	def desc = "$attrName set to $attrVal"
	logDebug(desc)
	sendEvent(name: attrName, value: attrVal, descriptionText: "${device.displayName} $desc", displayed: false)
}

int validateTrack(track) {
	validateRange(track, 1, 1, 100)
}

int validateRange(val, defaultVal, minVal, maxVal) {
	val = val instanceof String ? (val.isInteger() ? val.toInteger() : 0) : val
	def result = val
	
	if (!val) {
		result = defaultVal
	} else if (val > maxVal) {
		result = maxVal
	} else if (val < minVal) {
		result = minVal
	} 

	if (result != val) {
		logDebug("$val is invalid, defaulting to $result.")
	}
	result
}

private validateBool(val, defaulVal) {
	if (val == null) {
		defaultVal
	}
	else {
		(val == true || val == "true")
	}
}

private supportedSecurityGetCmd() {
	zwave.securityV1.securityCommandsSupportedGet()
}

private assocSetCmd() {
	zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
}

private batteryHealthGetCmd() {
	configGetCmd(42)
}

private deviceNotifyTypeSetCmd(notify) {
	// 0=nothing, 1=hail, 2=basic
	configSetCmd(80, (notify ? 2 : 0))
}

private sendLowBatterySetCmd() {
	configSetCmd(81, 1)
}

private repeatGetCmd() {
	configGetCmd(2)
}

private doorbellGetCmd() {
	configGetCmd(5)
}

private volumeGetCmd() {
	configGetCmd(8)
}

private configGetCmd(int paramNum) {
	zwave.configurationV1.configurationGet(parameterNumber: paramNum)
}

private configSetCmd(int paramNum, int val) {
	zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: 1, scaledConfigurationValue: val)
}

private getNumAttr(attrName) {
	def result = device.currentValue(attrName)
	if(!result) {
		result = 0
	}
	return result
}

private secureDelayBetween(commands, delay=100) {
	delayBetween(commands.collect{ secureCommand(it) }, delay)
}

private secureCommand(physicalgraph.zwave.Command cmd) {
	if (state.useSecureCommands == null || state.useSecureCommands) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		cmd.format()
	}
}

private logDebug(msg) {
	if (state.debugOutput || state.debugOutput == null) {
		log.debug "$msg"
	}
}

private logInfo(msg) {
	log.info "${device.displayName} $msg"
}
