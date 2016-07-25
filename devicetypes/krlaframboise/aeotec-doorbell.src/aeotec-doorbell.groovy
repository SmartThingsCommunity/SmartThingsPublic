/**
 *  Aeotec Doorbell v 1.7.1
 *      (Aeon Labs Doorbell - Model:ZW056-A)
 *
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
 *  1.7.1 (07/24/2016)
 *    - Added commands playRepeatTrack, playTrackAtVolume,
 *      playRepeatTrackAtVolume, volumeUp, volumeDown,
 *      repeatUp, and repeatDown
 *    - Made DTH only raise device specific events from 
 *      parse.
 *    - Made device work with or without secure inclusion.
 *    - Added fingerprints for hub v2 and non-secure
 *    - Volume 0 allows you to mute the doorbell, but
 *      still receive the notifications.
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

		attribute "lastPoll", "number"
		attribute "alarmTrack", "number"
		attribute "beepTrack", "number"
		attribute "doorbellTrack", "number"
		attribute "repeat", "number"
		attribute "status", "enum", ["off", "doorbell", "beep", "alarm", "play"]
		attribute "volume", "number"

		command "playTrack", ["number"]
		command "playRepeatTrack", ["number", "number"]
		command "playTrackAtVolume", ["number", "number"]
		command "playRepeatTrackAtVolume", ["number", "number", "number"]		
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
		command "repeatUp"
		command "repeatDown"
		command "setVolume", ["number"]
		command "volumeUp"
		command "volumeDown"

		fingerprint mfr: "0086", prod: "0104", model: "0038"		

		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A", outClusters: "0x82"

		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A", outClusters: "0x82"
	}

	simulator {
	}

	preferences {
		input "debugOutput", "bool", 
			title: "Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"statusTile", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action: "off", icon:"st.Home.home30", backgroundColor:"#ffffff"
				attributeState "doorbell", label:'Doorbell Ringing!', action: "off", icon:"st.Home.home30", backgroundColor:"#99c2ff"
				attributeState "alarm", label:'Alarm Sounding!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99FF99"
				attributeState "play", label:'Playing!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#694489"
			}
		}	
		standardTile("playDoorbell", "device.switch", width: 2, height: 2) {
			state "off", 
				label:'Doorbell', 
				action:"on",
				icon:"st.Home.home30",
				defaultState: true
			state "on",
				label:'Ringing',
				action:"off",
				icon:"st.Home.home30",
				nextState: "off", 
				backgroundColor: "#99c2ff"
		}
		standardTile("playBeep", "device.status", width: 2, height: 2) {
			state "off", 
				label:'Beep', 
				action:"beep", 
				icon:"st.Entertainment.entertainment2",
				defaultState: true
			state "beep", 
				label:'Beeping', 
				action:"off", 
				icon:"st.Entertainment.entertainment2", 
				backgroundColor: "#99FF99"
		}		
		standardTile("playAlarm", "device.alarm", width: 2, height: 2) {
			state "off", 
				label:'Alarm', 
				action: "both", 
				icon:"st.alarm.alarm.alarm",
				defaultState: true
			state "both", 
				label:'Sounding', 
				action: "off", 
				icon:"st.alarm.alarm.alarm", 
				backgroundColor: "#ff9999"
		}
		valueTile("doorbellTrack", "device.doorbellTrack", decoration: "flat", width:2, height: 2) {
			state "doorbellTrack", label: 'Doorbell ${currentValue}', defaultState: true
		}		
		controlTile("doorbellSlider", "device.doorbellTrack", "slider", width: 4, height: 1, range: "(1..100)") {
			state "doorbellTrack", action:"setDoorbellTrack"
		}
		standardTile("doorbellTrackUp", "device.doorbellTrack", width: 2, height: 1, decoration: "flat") {
			state "doorbellTrack", label:'>', action:"doorbellUp"
		}		
		standardTile("doorbellTrackDown", "device.doorbellTrack", width: 2, height: 1, decoration: "flat") {
			state "doorbellTrack", label:'<', action:"doorbellDown"
		}
		valueTile("beepTrack", "device.beepTrack", decoration: "flat", height:2, width:2) {
			state "beepTrack", label: 'BEEP ${currentValue}'
		}
		controlTile("beepSlider", "device.beepTrack", "slider", width: 4, height: 1, range: "(1..100)") {
			state "beepTrack", action:"setBeepTrack"
		}
		standardTile("beepTrackUp", "device.beepTrack", width: 2, height: 1, decoration: "flat") {
			state "beepTrack", label:'>', action:"beepUp", icon: ""
		}		
		standardTile("beepTrackDown", "device.beepTrack", width: 2, height: 1, decoration: "flat") {
			state "beepTrack", label:'<', action:"beepDown", icon: ""
		}		
		valueTile("alarmTrack", "device.alarmTrack", decoration: "flat", height:2, width:2) {
			state "alarmTrack", label: 'ALARM ${currentValue}', defaultStatus: true
		}
		controlTile("alarmSlider", "device.alarmTrack", "slider", width: 4, height: 1, range: "(1..100)") {
			state "alarmTrack", action:"setAlarmTrack"
		}
		standardTile("alarmTrackUp", "device.alarmTrack", width: 2, height: 1, decoration: "flat") {
			state "alarmTrack", label:'>', action:"alarmUp", icon: ""
		}
		standardTile("alarmTrackDown", "device.alarmTrack", width: 2, height: 1, decoration: "flat") {
			state "alarmTrack", label:'<', action:"alarmDown", icon: ""
		}		
		valueTile("volume", "device.volume", decoration: "flat", height:2, width:2) {
			state "volume", label: 'VOLUME ${currentValue}', defaultState: true
		}
		controlTile("volumeSlider", "device.volume", "slider", height: 1, width: 4, range: "(0..10)") {
			state "volume", action:"setVolume"
		}
		standardTile("volumeUp", "device.volume", width: 2, height: 1, decoration: "flat") {
			state "volume", label:'>', action:"volumeUp"
		}		
		standardTile("volumeDown", "device.volume", width: 2, height: 1, decoration: "flat") {
			state "volume", label:'<', action:"volumeDown"
		}		
		valueTile("repeat", "device.repeat", decoration: "flat", height:2, width:2) {
			state "repeat", label: 'REPEAT ${currentValue}', defaultState: true
		}
		controlTile("repeatSlider", "device.repeat", "slider", height: 1, width: 4, range: "(1..25)") {
			state "repeat", action:"setRepeat"
		}
		standardTile("repeatUp", "device.repeat", width: 2, height: 1, decoration: "flat") {
			state "repeat", label:'>', action:"repeatUp"
		}		
		standardTile("repeatDown", "device.repeat", width: 2, height: 1, decoration: "flat") {
			state "repeat", label:'<', action:"repeatDown"
		}
		valueTile("battery", "device.battery", decoration: "flat", height:2, width:2) {
			state "battery", label: 'Battery ${currentValue}%'
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'', action: "refresh", icon:"st.secondary.refresh"
		}		
		main "statusTile"
		details(["statusTile", "playDoorbell", "playBeep", "playAlarm", "doorbellTrack", "doorbellSlider", "doorbellTrackDown", "doorbellTrackUp", "beepTrack", "beepSlider", "beepTrackDown", "beepTrackUp", "alarmTrack", "alarmSlider", "alarmTrackDown", "alarmTrackUp", "volume", "volumeSlider", "volumeDown", "volumeUp", "repeat", "repeatSlider", "repeatDown", "repeatUp", "battery", "refresh"])
	}
}

// Sends configuration to device
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {	
		state.lastUpdated = new Date().time
			
		def cmds = []
		if (!state.isConfigured) {
			state.useSecureCmds = false
			cmds += configure()			
		}
		else {
			logDebug "Secure Commands ${state.useSecureCmds ? 'Enabled' : 'Disabled'}"
			cmds += refresh()
		}		
		return response(cmds)
	}
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

// Initializes variables and sends settings to device
def configure() {
	def cmds = []
	
	logDebug "Configuring ${state.useSecureCmds ? 'Secure' : 'Non-Secure'} Device"
	
	cmds += delayBetween([
		assocSetCmd(),
		deviceNotifyTypeSetCmd(true),
		sendLowBatterySetCmd(),
		volumeSetCmd(10),
		volumeGetCmd(),
		repeatSetCmd(1),
		repeatGetCmd(),
		doorbellSetCmd(2),
		doorbellGetCmd(),
		batteryHealthGetCmd(),
		manufacturerGetCmd()
	], 200)
	
	sendEvent(name: "beepTrack", value: 3, displayed: false)
	sendEvent(name: "alarmTrack", value: 4, displayed: false)
					
	if (!state.useSecureCmds) {
		cmds << supportedSecurityGetCmd()
	}
	
	return cmds
}
	
// Decrements volume
def volumeDown() {
	setVolume(getNumAttr("volume") - 1)
}

// Increments volume
def volumeUp() {
	setVolume(getNumAttr("volume") + 1)
}

// Sets volume attribute and device setting
def setVolume(volume) {	
	return delayBetween([
		volumeSetCmd(volume),
		volumeGetCmd()
	], 100)
}

// Decrements repeat
def repeatDown() {
	setRepeat(getNumAttr("repeat") - 1)
}

// Increments repeat
def repeatUp() {
	setRepeat(getNumAttr("repeat") + 1)
}

// Sets repeat attribute and device setting
def setRepeat(repeat) {	
	return delayBetween([
		repeatSetCmd(repeat),
		repeatGetCmd()
	], 100)
}

// Increments doorbellTrack.
def doorbellUp() {
	return setDoorbellTrack(getNumAttr("doorbellTrack") + 1)
}

// Decrements doorbellTrack
def doorbellDown() {
	return setDoorbellTrack(getNumAttr("doorbellTrack") - 1)
}

// Sets doorbellTrack attribute and setting
def setDoorbellTrack(track) {	
	delayBetween([
		doorbellSetCmd(track),
		doorbellGetCmd()
	], 100)
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
void setBeepTrack(track) {
	sendEvent(getEventMap("beepTrack", validateTrack(track)))	
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
void setAlarmTrack(track) {
	sendEvent(getEventMap("alarmTrack", validateTrack(track)))
}

private getEventMap(name, val) {
	logDebug "$name is $val"
	return [
		name: name, 
		value: val, 
		displayed: false,
		isStateChange: true
	]
}

// Stops playing track and raises events switch.off, alarm.off, status.off
def off() {
	logDebug "Turning Off"
	return delayBetween([
		deviceNotifyTypeSetCmd(true),
		basicSetCmd(0x00)
	], 100)
}

// Plays doorbellTrack and raises switch.on event
def on() {
	logDebug "Ringing Doorbell"
	return delayBetween([
		deviceNotifyTypeSetCmd(true),
		basicSetCmd(0xFF)
	], 100)
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def strobe() {
	return both()
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def siren() {
	return both()
}

// Plays alarmTrack and raises alarm.both and status.alarm events
def both() {
	logDebug "Sounding Alarm"
	return playTrack(getNumAttr("alarmTrack"), "alarm")
}

// Plays beepTrack and raises status.beep event
def beep() {
	logDebug "Playing Beep Track"
	return playTrack(getNumAttr("beepTrack"), "beep")		
}

// Plays specified track and raises status.play event
def playTrack(track) {
	return play([track: track])
}

// Plays specified track using default volume/repeat and raises specified status event
def playTrack(track, status) {
	return play([track:track, status: status])
}

// Plays specified track at specified volume.
def playTrackAtVolume(track, volume) {
	return play([track: track, volume: volume])
}

// Plays specified track for specified repeat
def playRepeatTrack(track, repeat) {
	return play([track: track, repeat: repeat])
}

// Plays specified track at specified volume and repeat.
def playRepeatTrackAtVolume(track, repeat, volume) {
	return play([track: track, volume: volume, repeat: repeat])
}

private play(Map data) {
	def changingVolumeOrRepeat = false
	data = data ?: [:]
	data.status = data.status ?: "play"		
	
	state.pendingStatus = data.status
	
	if (data.status == "play") {
		logDebug "Playing Track ${data.track}"
	}
			
	def result = []
		
	if (data.volume != null && data.volume != getNumAttr("volume")) {
		logDebug "Temporarily changing volume to ${data.volume}"
		if (!state.pendingVolume) {
			state.pendingVolume = getNumAttr("volume")
		}
		result << volumeSetCmd(data.volume)
		changingVolumeOrRepeat = true
	}
			
	if (data.repeat != null && data.repeat != getNumAttr("repeat")) {
		logDebug "Temporarily changing repeat to ${data.repeat}"
		if (!state.pendingRepeat) {
			state.pendingRepeat = getNumAttr("repeat")
		}
		result << repeatSetCmd(data.repeat)
		changingVolumeOrRepeat = true
	}
	
	if (changingVolumeOrRepeat) {
		result << "delay 300"
	}
	
	result << deviceNotifyTypeSetCmd(false)
	result << playTrackSetCmd(data.track)	
	result << deviceNotifyTypeSetCmd(true)
		
	return delayBetween(result, 100)		
}

// Re-loads attributes from device configuration.
def refresh() {
	logDebug "Executing refresh()"
	logDebug "beepTrack is ${getNumAttr('beepTrack')}"
	logDebug "alarmTrack is ${getNumAttr('alarmTrack')}"
	
	return delayBetween([
		doorbellGetCmd(),
		repeatGetCmd(),
		volumeGetCmd(),			
		batteryHealthGetCmd()
	], 100)
}


// Parses incoming message
def parse(String description) {
	def result = null	
	if (description != null && description != "updated") {	
		def cmd = zwave.parse(description, [0x20:1,0x25:1,0x59:1,0x70:1,0x72:2,0x82:1,0x85:2,0x86:1,0x98:1])
		if (cmd) {
			result = zwaveEvent(cmd)
			
			result << createEvent(name: "lastPoll", value: new Date().time, displayed: false, isStateChange: true)			
		} 
		else {
			logDebug("No Command: $cmd")
		}
	}
	return result
}

// Unencapsulates the secure command.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def result = []
	if (cmd) {
		def encapCmd = cmd.encapsulatedCommand([0x20:1,0x25:1,0x59:1,0x70:1,0x72:2,0x82:1,0x85:2,0x86:1,0x98:1])
		if (encapCmd) {
			result = zwaveEvent(encapCmd)
		}
		else {
			log.warn "Unable to extract encapsulated cmd from $cmd"
		}
	}
	return result
}

// Sends secure configuration to the device.
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.useSecureCmds = true
	logDebug "Secure Inclusion Detected"
	def result = []
	result += response(configure())
	return result	
}

// Handles device reporting on and off
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	state.pendingStatus = null
	def result = []
	if (cmd.value == 0) {
		result += handleDeviceTurningOff()
	} 
	else if (cmd.value == 255) {
		logDebug("Doorbell Ringing")

		result << createEvent(name: "switch", value: "on", descriptionText: "Doorbell Ringing", displayed: true, isStateChange: true)
		
		result << createEvent(name: "status", value: "doorbell", displayed: false, isStateChange: true)
	}
	return result
}

// Raises events switch.off, alarm.off, and status.off
def handleDeviceTurningOff() {
	def result = []
	def desc = "${device.displayName} is off"
	def displayStatus = true
	
	logDebug desc
	
	if (device.currentValue("alarm") != "off") {
		result << createEvent(name:"alarm", value: "off", descriptionText: desc, isStateChange: true, displayed: true)
		displayStatus = false
	}
	
	if (device.currentValue("switch") != "off") {
		result << createEvent(name:"switch", value: "off", descriptionText: desc, isStateChange: true, displayed: true)
		displayStatus = false
	}
	
	if (device.currentValue("status") != "off") {
		result << createEvent(name:"status", value: "off", descriptionText: desc, isStateChange: true, displayed: displayStatus)
	}
	
	if (state.pendingVolume) {
		// Last play changed volume so restore default
		logDebug "Restoring volume to ${state.pendingVolume}"
		result << response(volumeSetCmd(state.pendingVolume))		
		state.pendingVolume = null
	}
	
	if (state.pendingRepeat) {
		// Last play changed repeat so change back to default.
		logDebug "Restoring repeat to ${state.pendingRepeat}"
		result << response(repeatSetCmd(state.pendingRepeat))
		state.pendingRepeat = null
	}
	
	return result
}

// def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	// def result = []
	// def displayStatus = true
	// if (cmd.value == 0xFF && state.pendingStatus) {
		
		// if (state.pendingStatus == "alarm") {
			// result << createEvent(name: "alarm", value: "both", displayed: true, isStateChange: true)
			// displayStatus = false
		// }
		
		// result << createEvent(name: "status", value: state.pendingStatus, displayed: displayStatus, isStateChange: true)
	// }	
	// state.pendingStatus = null
	// return result
// }

// Display the manufacturer info
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logDebug("$cmd")
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	def result = []
	def displayStatus = true
	if (state.pendingStatus) {
		
		if (state.pendingStatus == "alarm") {
			result << createEvent(name: "alarm", value: "both", displayed: true, isStateChange: true)
			displayStatus = false
		}
		
		result << createEvent(name: "status", value: state.pendingStatus, displayed: displayStatus, isStateChange: true)
	}	
	state.pendingStatus = null
	return result
}

// Writes parameter settings to Info Log
def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	 
	def result = []
	def name = null
	state.isConfigured = true
	switch (cmd.parameterNumber) {
		case 8:
			result << createEvent(getEventMap("volume", cmd.configurationValue[0]))
			break
		case 5:
			result << createEvent(getEventMap("doorbellTrack", cmd.configurationValue[0]))
			break
		case 2:
			result << createEvent(getEventMap("repeat", cmd.configurationValue[0]))
			break			
		case 80:
			name = "Device Notification Type"
			break
		case 81:
			name = "Send Low Battery Notifications"
			break
		case 42:
			result << createEvent(getBatteryEventMap(cmd.configurationValue[0]))
			break
		default:
			name = "Parameter #${cmd.parameterNumber}"
	}
	if (name) {
		logDebug("${name}: ${cmd.configurationValue}")
	}
	return result	
}

private getBatteryEventMap(val) {
	def batteryVal = (val == 0) ? 100 : 1
	def batteryLevel = (val == 0) ? "normal" : "low"
	
	logDebug("Battery is $batteryLevel")

	return [
		name: "battery", 
		value: batteryVal,
		unit: "%", 
		descriptionText: "$batteryLevel", 
		isStateChange: true,
		displayed: true
	]
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug("Unhandled: $cmd")
	return []
}


private assocSetCmd() {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
}

private manufacturerGetCmd() {
	return secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
}

private basicSetCmd(val) {
	return secureCmd(zwave.basicV1.basicSet(value: val))
}

private batteryHealthGetCmd() {
	return configGetCmd(42)
}

private deviceNotifyTypeSetCmd(useBasicReport) {
	// 0=nothing, 1=hail, 2=basic
	return configSetCmd(80, (useBasicReport ? 2 : 1))
}

private sendLowBatterySetCmd() {
	return configSetCmd(81, 1)
}

private repeatGetCmd() {
	return configGetCmd(2)
}

private repeatSetCmd(repeat) {
	repeat = validateRange(repeat, 1, 1, 300, "Repeat")	
	return configSetCmd(2, repeat)
}

private doorbellGetCmd() {
	return configGetCmd(5)
}

private doorbellSetCmd(track) {
	track = validateTrack(track)	
	return configSetCmd(5, track)
}

private volumeGetCmd() {
	return configGetCmd(8)
}

private volumeSetCmd(volume) {
	volume = validateRange(volume, 5, 0, 10, "Volume")
	return configSetCmd(8, volume)
}

private playTrackSetCmd(track) {
	track = validateTrack(track)
	return configSetCmd(6, track)
}

private configGetCmd(int paramNum) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNum))
}

private configSetCmd(int paramNum, int val) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: 1, scaledConfigurationValue: val))
}

private supportedSecurityGetCmd() {
	logDebug "Checking for Secure Command Support"	
	state.useSecureCmds = true // force secure cmd	
	def cmd = secureCmd(zwave.securityV1.securityCommandsSupportedGet())	
	state.useSecureCmds = false // reset secure cmd	
	return cmd
}

private secureCmd(physicalgraph.zwave.Command cmd) {
	if (state.useSecureCmds) {		
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} 
	else {		
		return cmd.format()
	}
}

private int validateTrack(track) {
	validateRange(track, 1, 1, 100, "Track")
}

private int validateRange(val, defaultVal, minVal, maxVal, desc) {
	def result
	def errorType = null
	if (isInt(val)) {
		result = val.toString().toInteger()
	}
	else {
		errorType = "invalid"
		result = defaultVal
	}
	
	if (result > maxVal) {
		errorType = "too high"
		result = maxVal
	} else if (result < minVal) {
		errorType = "too low"
		result = minVal
	} 

	if (errorType) {
		logDebug("$desc: $val is $errorType, using $result instead.")
	}
	return result
}

private isInt(val) {
	return val?.toString()?.isInteger() ? true : false
}

private getNumAttr(attrName) {
	def result = device.currentValue(attrName)
	if (!result) {
		result = 0
	}
	return result
}

private logDebug(msg) {
	if (settings?.debugOutput == null || settings?.debugOutput) {
		log.debug "$msg"
	}
}