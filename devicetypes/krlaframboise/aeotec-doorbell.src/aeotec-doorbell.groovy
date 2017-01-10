/**
 *  Aeotec Doorbell v 1.11
 *      (Aeon Labs Doorbell - Model:ZW056-A)
 *
 *  (https://community.smartthings.com/t/release-aeon-labs-aeotec-doorbell/39166/16?u=krlaframboise)
 *
 *  Capabilities:
 *      Switch, Alarm, Tone, Audio Notification, 
 *      Polling, Battery, Configuration, Refresh
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *  1.11 (01/08/2017)
 *    - Made playText support messages like "track,repeat" which will play track at the specified number of times.
 *
 *  1.10.1 (10/08/2016)
 *    - Added speech synthesis capability so that the
 *      speak command can play a track number.
 *
 *  1.10 (10/04/2016)
 *    - Added volume slider to main tile.
 *    - Removed volume preference.
 *
 *  1.9.2 (09/10/2016)
 *    - Bug fix that uses default volume if the volume
 *      argument is 0.
 *
 *  1.9.1 (09/04/2016)
 *    - Bug fix for playing track at volume.
 *
 *  1.9 (08/31/2016)
 *    - !!!!!  BREAKING CHANGES !!!!!
 *    - The settings have been moved from the UI to the
 *      Settings screen.
 *    - Everything should continue to work after upgrading,
 *      but the first time you open the device settings, it
 *      will be blank so you'll have to re-enter the track
 *      numbers.
 *    - !!!!!  BREAKING CHANGES !!!!!
 *
 *  1.8.3 (08/23/2016)
 *    - Minor Bug fixes 
 *
 *  1.8.1 (08/17/2016)
 *    - Added polling capability.
 *
 *  1.8 (08/16/2016)
 *    - Fixed bug that caused switch.on to execute every time a track
 *      is played when the device isn't paired securely.
 *    - Implemented Audio Notification capability.
 *    - Implemented Button capability.
 *
 *  1.7.2 (07/30/2016)
 *    - Removed duplicate hub v1 fingerprint.
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
 *    -    UI Enhancements/Fixes, added Refresh capability.
 *
 *  1.2 (02/17/2016)
 *    -    Fixed bug causing error on install.
 *
 *  1.1 (02/15/2016)
 *    -    Consolidated code.
 *
 *  1.0 (02/14/2016)
 *    -    Initial Release
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
		capability "Button"
		capability "Alarm"
		capability "Tone"
		capability "Audio Notification"
		capability "Music Player"
		capability "Battery"
		capability "Refresh"
		capability "Polling"
		capability "Speech Synthesis"

		attribute "lastPoll", "number"
		
		attribute "status", "enum", ["off", "doorbell", "beep", "alarm", "play"]
		
		command "setVolume", ["number"]
		command "playRepeatTrack", ["number", "number"]
		command "playRepeatTrackAtVolume", ["number", "number", "number"]
		command "playSoundAndTrack"
		command "playTrackAtVolume"		

		fingerprint mfr: "0086", prod: "0104", model: "0038"

		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A", outClusters: "0x82"
	}

	simulator {
	}

	preferences {
		input "alarmTrack", "number",
			title: "Alarm Track: (1-100)",
			required: true,
			range: "1..100",
			displayDuringSetup: true
		input "beepTrack", "number",
			title: "Beep Track: (1-100)",
			required: true,
			range: "1..100",
			displayDuringSetup: true
		input "doorbellTrack", "number",
			title: "Doorbell Track: (1-100)",
			required: true,
			range: "1..100",
			displayDuringSetup: true		
		input "repeat", "number",
			title: "Repeat: (1-20)",
			required: true,
			range: "1..20",
			displayDuringSetup: true
		input "logging", "enum",
			title: "Types of messages to log:",
			multiple: true,
			required: true,
			defaultValue: ["debug", "info"],
			options: ["debug", "info", "trace"]		
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
		valueTile("volume", "device.level", decoration: "flat", height:1, width:2) {
			state "level", label: 'VOLUME ${currentValue}', defaultState: true
		}
		controlTile("volumeSlider", "device.level", "slider", height: 1, width: 4, range: "(0..10)") {
			state "level", action:"setVolume"
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
		valueTile("battery", "device.battery", height:2, width:2) {
			state "battery", label: 'Battery ${currentValue}%', backgroundColor: "#cccccc"
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'', action: "refresh", icon:"st.secondary.refresh"
		}		
		main "statusTile"
		details(["statusTile", "playDoorbell", "playBeep", "playAlarm", "volume", "volumeSlider", "refresh", "battery"])
	}
}

// Sends configuration to device
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {    
		state.lastUpdated = new Date().time
				
		if (device.currentValue("numberOfButtons") != 1) {
			sendEvent(name: "numberOfButtons", value: 1, displayed: false)
		}
				
		def cmds = []
		if (!state.isConfigured) {
			state.useSecureCmds = false
			cmds += configure()            
		}
		else {			
			logDebug "Secure Commands ${state.useSecureCmds ? 'Enabled' : 'Disabled'}"
			cmds += updateSettings()
			if (!cmds) {
				cmds += refresh()
			}
		}        
		return response(cmds)
	}
}

private updateSettings() {
	def result = []
	if (settings?.alarmTrack && settings?.alarmTrack != state?.alarmTrack) {
		result << setAlarmTrack(settings?.alarmTrack)
	}
	if (settings?.beepTrack && settings?.beepTrack != state?.beepTrack) {
		result << setBeepTrack(settings?.beepTrack)
	}
	if (settings?.doorbellTrack && settings?.doorbellTrack != state?.doorbellTrack) {
		result << setDoorbellTrack(settings?.doorbellTrack)
	}
	if (settings?.repeat && settings?.repeat != state?.repeat) {
		result << setRepeat(settings?.repeat)
	}	
	return result
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
	
	state.beepTrack = 3
	state.alarmTrack = 4
						
	if (!state.useSecureCmds) {
		cmds << supportedSecurityGetCmd()
	}
	
	return cmds
}
	
// Sets volume attribute and device setting
def setVolume(volume) {
	logTrace "Setting volume to $volume"
	return delayBetween([
		volumeSetCmd(volume),
		volumeGetCmd()
	], 100)
}

// Sets repeat attribute and device setting
def setRepeat(repeat) {
	logTrace "Setting repeat to $repeat"
	return delayBetween([
		repeatSetCmd(repeat),
		repeatGetCmd()
	], 100)
}

// Sets doorbellTrack attribute and setting
def setDoorbellTrack(track) {
	logTrace "Setting doorbellTrack to $track"
	delayBetween([
		doorbellSetCmd(track),
		doorbellGetCmd()
	], 100)
}

// Sets beepTrack attribute
void setBeepTrack(track) {	
	state.beepTrack = validateTrack(track)	
	logTrace "beepTrack changed to ${state.beepTrack}"
}

// Sets alarmTrack attribute
void setAlarmTrack(track) {
	state.alarmTrack = validateTrack(track)
	logTrace "alarmTrack changed to ${state.alarmTrack}"	
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
	logTrace "Executing off()"
	logDebug "Turning Off"
	return delayBetween([
		deviceNotifyTypeSetCmd(true),
		basicSetCmd(0x00)
	], 100)
}

// Plays doorbellTrack and raises switch.on event
def on() {
	logTrace "Executing on()"
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
	logTrace "Executing both()"
	logDebug "Sounding Alarm"
	return startTrack([track: getNumAttr("alarmTrack"), status: "alarm"])
}

// Plays beepTrack and raises status.beep event
def beep() {
	logTrace "Executing beep()"
	logDebug "Playing Beep Track"
	return startTrack([track: getNumAttr("beepTrack"), status: "beep"])
}

// Simulate doorbell button press.
def play() {
	return on()
}

// Turn off device.
def pause() {
	return off()
}

// Turn off device.
def stop() {
	return off()
}

// Display log message for unsupported Music Player commands.
def mute() {
	logUnsupportedCommand("mute()")
}
def unmute() {
	logUnsupportedCommand("unmute()")
}
def nextTrack() {
	logUnsupportedCommand("nextTrack()")
}
def previousTrack() {
	logUnsupportedCommand("previousTrack()")
}
private logUnsupportedCommand(cmdName) {
	logTrace "This device does not support the ${cmdName} command."
}
 
// Audio Notification Capability Commands
def playSoundAndTrack(URI, duration=null, track, volume=null) {	
	playTrack(URI, volume)
}
def playTrackAtVolume(URI, volume) {
	playTrack(URI, volume)
}

def playTrackAndResume(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	
def playTrackAndRestore(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	
def playTextAndResume(message, volume=null) {
	playText(message, volume)
}	
def playTextAndRestore(message, volume=null) {
	playText(message, volume)
}

def speak(message) {
	// Using playTrack in case url is passed in.
	playTrack("$message", null)
}

// Extracts the track number from the URI and passes it and
// the volume to the playText command.
def playTrack(URI, volume=null) {
	logTrace "Executing playTrack($URI, $volume)"
	def text = getTextFromTTSUrl(URI)
	playText(!text ? URI : text, volume)	
}

private getTextFromTTSUrl(URI) {
	def urlPrefix = "https://s3.amazonaws.com/smartapp-media/tts/"
	if (URI?.toString()?.toLowerCase()?.contains(urlPrefix)) {
		return URI.replace(urlPrefix,"").replace(".mp3","")
	}
	return null
}

//Plays the track specified as the message at the specified volume.
def playText(message, volume=null) {
	def items = "${message}".split(",")
	def track = validateTrack(items[0])
	def repeat = null
	
	if (items.size() > 1) {
		repeat = validateRange(items[1], 1, 1, 300, "Repeat")
	}
	
	if ("${volume}" == "0") {
		volume = null
	}
	
	logTrace "Executing playText($message, $volume)"
	return startTrack([track: track, repeat: repeat, volume: volume])
}

// Plays specified track for specified repeat
def playRepeatTrack(track, repeat) {
	logTrace "Executing playRepeatTrack($track, $repeat)"
	return startTrack([track: track, repeat: repeat])
}

// Plays specified track at specified volume and repeat.
def playRepeatTrackAtVolume(track, repeat, volume) {
	logTrace "Executing playRepeatTrackAtVolume($track, $repeat, $volume)"
	return startTrack([track: track, volume: volume, repeat: repeat])
}

private startTrack(Map data) {
	logTrace "startTrack($data)"
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
		result << "delay 2000"
	}
	
	result << deviceNotifyTypeSetCmd(false)
		
	if (!state.useSecureCmds) {            
		result << deviceNotifyTypeGetCmd()
		result << "delay 450"
	}
	
	result << playTrackSetCmd(data.track)
	result << "delay 450"
	result << deviceNotifyTypeSetCmd(true)

	if (!state.useSecureCmds) {
		result << deviceNotifyTypeGetCmd()
	}
		
	return delayBetween(result, 50)
}

def poll() {
	return batteryHealthGetCmd()
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
	def result = []

	if (state.useSecureCmds || cmd.value == 0 || state.notificationType == 2) {
		logTrace "BasicReport: $cmd"
		state.pendingStatus = null
		
		if (cmd.value == 0) {        
			result += handleDeviceTurningOff()
		} 
		else if (cmd.value == 255) {
			logDebug("Doorbell Ringing")
			result << createEvent(name: "switch", value: "on", descriptionText: "Doorbell Ringing", displayed: true, isStateChange: true)
			result << createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false, isStateChange: true)
			result << createEvent(name: "status", value: "doorbell", displayed: false, isStateChange: true)
		}
	}
	else {
		logTrace "Skipped BasicReport because Notification Type is Hail"
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

// Display the manufacturer info
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logDebug("$cmd")
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	logTrace "Hail: $cmd"
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
			name = "Volume"
			state.volume = cmd.configurationValue[0]
			if (state.volume != device.currentValue("level")) {
				result << createEvent(name: "level", value: state.volume, displayed: true)
			}
			if (device.currentValue("mute") != "unmuted") {
				result << createEvent(name: "mute", value: "unmuted", displayed: false)
			}
			break
		case 5:
			name = "Doorbell Track"
			state.doorbellTrack = cmd.configurationValue[0]
			break
		case 2:
			name = "Repeat"
			state.repeat = cmd.configurationValue[0]
			break            
		case 80:
			name = "Device Notification Type"
			state.notificationType = cmd.configurationValue[0]
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
		logDebug("${name}: ${cmd.configurationValue[0]}")
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
		displayed: (batteryLevel == "low")
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

private deviceNotifyTypeGetCmd() {
	return configGetCmd(80)
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

private getNumAttr(settingName) {
	def result = settings[settingName]
	if (result == null) {
		// Prior to version 1.9 these settings were stored 
		// as attributes so if the settings haven't been
		// saved it should use the attribute value.
		result = device.currentValue(settingName)		
	}
	if (result == null) {
		// It wasn't in settings or an attribute.
		result = 0
	}
	return result
}

private logDebug(msg) {
	//if (loggingTypeEnabled("debug")) {
		log.debug msg
	//}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logInfo(msg) {
	//if (loggingTypeEnabled("info")) {
		log.info msg
	//}
}

private loggingTypeEnabled(loggingType) {
	try {
		return (!settings?.logging || settings?.logging?.contains(loggingType))
	}
	catch (e) {
		return true
	}
}
 *    - Implemented Button capability.
 *
 *  1.7.2 (07/30/2016)
 *    - Removed duplicate hub v1 fingerprint.
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
 *    -    UI Enhancements/Fixes, added Refresh capability.
 *
 *  1.2 (02/17/2016)
 *    -    Fixed bug causing error on install.
 *
 *  1.1 (02/15/2016)
 *    -    Consolidated code.
 *
 *  1.0 (02/14/2016)
 *    -    Initial Release
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
		capability "Button"
		capability "Alarm"
		capability "Tone"
		capability "Audio Notification"
		capability "Music Player"
		capability "Battery"
		capability "Refresh"
		capability "Polling"
		capability "Speech Synthesis"

		attribute "lastPoll", "number"
		
		attribute "status", "enum", ["off", "doorbell", "beep", "alarm", "play"]
		
		command "setVolume", ["number"]
		command "playRepeatTrack", ["number", "number"]
		command "playRepeatTrackAtVolume", ["number", "number", "number"]
		command "playSoundAndTrack"
		command "playTrackAtVolume"		

		fingerprint mfr: "0086", prod: "0104", model: "0038"

		fingerprint deviceId: "0x1005", inClusters: "0x5E,0x98,0x25,0x70,0x72,0x59,0x85,0x73,0x7A,0x5A", outClusters: "0x82"
	}

	simulator {
	}

	preferences {
		input "alarmTrack", "number",
			title: "Alarm Track: (1-100)",
			required: true,
			range: "1..100",
			displayDuringSetup: true
		input "beepTrack", "number",
			title: "Beep Track: (1-100)",
			required: true,
			range: "1..100",
			displayDuringSetup: true
		input "doorbellTrack", "number",
			title: "Doorbell Track: (1-100)",
			required: true,
			range: "1..100",
			displayDuringSetup: true		
		input "repeat", "number",
			title: "Repeat: (1-20)",
			required: true,
			range: "1..20",
			displayDuringSetup: true
		input "logging", "enum",
			title: "Types of messages to log:",
			multiple: true,
			required: true,
			defaultValue: ["debug", "info"],
			options: ["debug", "info", "trace"]		
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
		valueTile("volume", "device.level", decoration: "flat", height:1, width:2) {
			state "level", label: 'VOLUME ${currentValue}', defaultState: true
		}
		controlTile("volumeSlider", "device.level", "slider", height: 1, width: 4, range: "(0..10)") {
			state "level", action:"setVolume"
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
		valueTile("battery", "device.battery", height:2, width:2) {
			state "battery", label: 'Battery ${currentValue}%', backgroundColor: "#cccccc"
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "refresh", label:'', action: "refresh", icon:"st.secondary.refresh"
		}		
		main "statusTile"
		details(["statusTile", "playDoorbell", "playBeep", "playAlarm", "volume", "volumeSlider", "refresh", "battery"])
	}
}

// Sends configuration to device
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 3000)) {    
		state.lastUpdated = new Date().time
				
		if (device.currentValue("numberOfButtons") != 1) {
			sendEvent(name: "numberOfButtons", value: 1, displayed: false)
		}
				
		def cmds = []
		if (!state.isConfigured) {
			state.useSecureCmds = false
			cmds += configure()            
		}
		else {			
			logDebug "Secure Commands ${state.useSecureCmds ? 'Enabled' : 'Disabled'}"
			cmds += updateSettings()
			if (!cmds) {
				cmds += refresh()
			}
		}        
		return response(cmds)
	}
}

private updateSettings() {
	def result = []
	if (settings?.alarmTrack && settings?.alarmTrack != state?.alarmTrack) {
		result << setAlarmTrack(settings?.alarmTrack)
	}
	if (settings?.beepTrack && settings?.beepTrack != state?.beepTrack) {
		result << setBeepTrack(settings?.beepTrack)
	}
	if (settings?.doorbellTrack && settings?.doorbellTrack != state?.doorbellTrack) {
		result << setDoorbellTrack(settings?.doorbellTrack)
	}
	if (settings?.repeat && settings?.repeat != state?.repeat) {
		result << setRepeat(settings?.repeat)
	}	
	return result
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
	
	state.beepTrack = 3
	state.alarmTrack = 4
						
	if (!state.useSecureCmds) {
		cmds << supportedSecurityGetCmd()
	}
	
	return cmds
}
	
// Sets volume attribute and device setting
def setVolume(volume) {
	logTrace "Setting volume to $volume"
	return delayBetween([
		volumeSetCmd(volume),
		volumeGetCmd()
	], 100)
}

// Sets repeat attribute and device setting
def setRepeat(repeat) {
	logTrace "Setting repeat to $repeat"
	return delayBetween([
		repeatSetCmd(repeat),
		repeatGetCmd()
	], 100)
}

// Sets doorbellTrack attribute and setting
def setDoorbellTrack(track) {
	logTrace "Setting doorbellTrack to $track"
	delayBetween([
		doorbellSetCmd(track),
		doorbellGetCmd()
	], 100)
}

// Sets beepTrack attribute
void setBeepTrack(track) {	
	state.beepTrack = validateTrack(track)	
	logTrace "beepTrack changed to ${state.beepTrack}"
}

// Sets alarmTrack attribute
void setAlarmTrack(track) {
	state.alarmTrack = validateTrack(track)
	logTrace "alarmTrack changed to ${state.alarmTrack}"	
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
	logTrace "Executing off()"
	logDebug "Turning Off"
	return delayBetween([
		deviceNotifyTypeSetCmd(true),
		basicSetCmd(0x00)
	], 100)
}

// Plays doorbellTrack and raises switch.on event
def on() {
	logTrace "Executing on()"
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
	logTrace "Executing both()"
	logDebug "Sounding Alarm"
	return startTrack([track: getNumAttr("alarmTrack"), status: "alarm"])
}

// Plays beepTrack and raises status.beep event
def beep() {
	logTrace "Executing beep()"
	logDebug "Playing Beep Track"
	return startTrack([track: getNumAttr("beepTrack"), status: "beep"])
}

// Simulate doorbell button press.
def play() {
	return on()
}

// Turn off device.
def pause() {
	return off()
}

// Turn off device.
def stop() {
	return off()
}

// Display log message for unsupported Music Player commands.
def mute() {
	logUnsupportedCommand("mute()")
}
def unmute() {
	logUnsupportedCommand("unmute()")
}
def nextTrack() {
	logUnsupportedCommand("nextTrack()")
}
def previousTrack() {
	logUnsupportedCommand("previousTrack()")
}
private logUnsupportedCommand(cmdName) {
	logTrace "This device does not support the ${cmdName} command."
}
 
// Audio Notification Capability Commands
def playSoundAndTrack(URI, duration=null, track, volume=null) {	
	playTrack(URI, volume)
}
def playTrackAtVolume(URI, volume) {
	playTrack(URI, volume)
}

def playTrackAndResume(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	
def playTrackAndRestore(URI, volume=null, otherVolume=null) {
	if (otherVolume) {
		// Fix for Speaker Notify w/ Sound not using command as documented.
		volume = otherVolume
	}
	playTrack(URI, volume)
}	
def playTextAndResume(message, volume=null) {
	playText(message, volume)
}	
def playTextAndRestore(message, volume=null) {
	playText(message, volume)
}

def speak(message) {
	// Using playTrack in case url is passed in.
	playTrack("$message", null)
}

// Extracts the track number from the URI and passes it and
// the volume to the playText command.
def playTrack(URI, volume=null) {
	logTrace "Executing playTrack($URI, $volume)"
	def text = getTextFromTTSUrl(URI)
	playText(!text ? URI : text, volume)	
}

private getTextFromTTSUrl(URI) {
	def urlPrefix = "https://s3.amazonaws.com/smartapp-media/tts/"
	if (URI?.toString()?.toLowerCase()?.contains(urlPrefix)) {
		return URI.replace(urlPrefix,"").replace(".mp3","")
	}
	return null
}

//Plays the track specified as the message at the specified volume.
def playText(message, volume=null) {
	if ("${volume}" == "0") {
		volume = null
	}
	logTrace "Executing playText($message, $volume)"
	return startTrack([track: message, volume: volume])
}

// Plays specified track for specified repeat
def playRepeatTrack(track, repeat) {
	logTrace "Executing playRepeatTrack($track, $repeat)"
	return startTrack([track: track, repeat: repeat])
}

// Plays specified track at specified volume and repeat.
def playRepeatTrackAtVolume(track, repeat, volume) {
	logTrace "Executing playRepeatTrackAtVolume($track, $repeat, $volume)"
	return startTrack([track: track, volume: volume, repeat: repeat])
}

private startTrack(Map data) {
	logTrace "startTrack($data)"
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
		result << "delay 2000"
	}
	
	result << deviceNotifyTypeSetCmd(false)
		
	if (!state.useSecureCmds) {            
		result << deviceNotifyTypeGetCmd()
		result << "delay 450"
	}
	
	result << playTrackSetCmd(data.track)
	result << "delay 450"
	result << deviceNotifyTypeSetCmd(true)

	if (!state.useSecureCmds) {
		result << deviceNotifyTypeGetCmd()
	}
		
	return delayBetween(result, 50)
}

def poll() {
	return batteryHealthGetCmd()
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
	def result = []

	if (state.useSecureCmds || cmd.value == 0 || state.notificationType == 2) {
		logTrace "BasicReport: $cmd"
		state.pendingStatus = null
		
		if (cmd.value == 0) {        
			result += handleDeviceTurningOff()
		} 
		else if (cmd.value == 255) {
			logDebug("Doorbell Ringing")
			result << createEvent(name: "switch", value: "on", descriptionText: "Doorbell Ringing", displayed: true, isStateChange: true)
			result << createEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false, isStateChange: true)
			result << createEvent(name: "status", value: "doorbell", displayed: false, isStateChange: true)
		}
	}
	else {
		logTrace "Skipped BasicReport because Notification Type is Hail"
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

// Display the manufacturer info
def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	logDebug("$cmd")
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	logTrace "Hail: $cmd"
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
			name = "Volume"
			state.volume = cmd.configurationValue[0]
			if (state.volume != device.currentValue("level")) {
				result << createEvent(name: "level", value: state.volume, displayed: true)
			}
			if (device.currentValue("mute") != "unmuted") {
				result << createEvent(name: "mute", value: "unmuted", displayed: false)
			}
			break
		case 5:
			name = "Doorbell Track"
			state.doorbellTrack = cmd.configurationValue[0]
			break
		case 2:
			name = "Repeat"
			state.repeat = cmd.configurationValue[0]
			break            
		case 80:
			name = "Device Notification Type"
			state.notificationType = cmd.configurationValue[0]
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
		logDebug("${name}: ${cmd.configurationValue[0]}")
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
		displayed: (batteryLevel == "low")
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

private deviceNotifyTypeGetCmd() {
	return configGetCmd(80)
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

private getNumAttr(settingName) {
	def result = settings[settingName]
	if (result == null) {
		// Prior to version 1.9 these settings were stored 
		// as attributes so if the settings haven't been
		// saved it should use the attribute value.
		result = device.currentValue(settingName)		
	}
	if (result == null) {
		// It wasn't in settings or an attribute.
		result = 0
	}
	return result
}

private logDebug(msg) {
	//if (loggingTypeEnabled("debug")) {
		log.debug msg
	//}
}

private logTrace(msg) {
	if (loggingTypeEnabled("trace")) {
		log.trace msg
	}
}

private logInfo(msg) {
	//if (loggingTypeEnabled("info")) {
		log.info msg
	//}
}

private loggingTypeEnabled(loggingType) {
	try {
		return (!settings?.logging || settings?.logging?.contains(loggingType))
	}
	catch (e) {
		return true
	}
}
