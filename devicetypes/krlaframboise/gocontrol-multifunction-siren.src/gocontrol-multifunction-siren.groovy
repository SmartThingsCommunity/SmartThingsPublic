/**
 *  GoControl Multifunction Siren v 1.6.1
 *
 *  Devices:
 *    GoControl/Linear (Model#: WA105DBZ-1)
 *    Vision Home Security (Model#: ZM1601US-5)
 *  	LinearLinc Z-Wave Siren/Strobe (Model#: 1LIWA105DBZ-2)
 *
 *  Capabilities:
 *      Alarm, Tone, Audio Notification, Switch
 *      Battery, Polling
 *   
 *   ********************************************* 
 *   ** The Speech Synthesis and Music Player   **
 *   ** capabilities can ONLY be used to send   **
 *   ** specific commands to the device through **
 *   ** using any SmartApp.                     ** 
 *   *********************************************
 *
 *  Author: 
 *     Kevin LaFramboise (krlaframboise)
 *
 *  Url to Documentation:
 *      https://community.smartthings.com/t/release-gocontrol-linear-multifunction-siren/47024?u=krlaframboise
 *
 *  Changelog:
 *
 *    1.6 (08/28/2016)
 *      - Added support for turning on the alarm while a
 *        different alarm type is already playing.
 *
 *    1.6 (08/06/2016)
 *      - Added support for Audio Notification capability
 *
 *    1.5.2 (07/20/2016)
 *      - Added support for Vision version of siren because
 *        it's the same device, but the configuration uses
 *        different parameter numbers.
 *
 *    1.4 (07/14/2016)
 *      - Added secure command support.
 *
 *    1.3 (06/17/2016)
 *      - Fixed delayed alarm with strobe bug.
 *      - Numbered settings to make it easier to explain things.
 *      - Added "Allow Siren Only Light" setting that allows you
 *        to display a solid red light while the siren is on.
 *      - Added parameters delaySeconds and useStrobe to the
 *        custom beep command that allow you to delay a beep
 *        and display the strobe during that delay.
 *
 *    1.2.1, 1.2.2, 1.2.3 (06/12/2016)
 *      - Improved beep performance. 
 *      - Fixed UI issue caused by latest android update.
 *        (unable to completely fix, but the buttons no longer
 *         completely disapear)
 *      - Fixed icons and colors in activity log.
 *      - Added always set alarm type setting.
 *
 *    1.2 (06/11/2016)
 *      - *** BREAKING CHANGES ***
 *            - Removed strobe instead of beep option from settings.
 *            - Removed always use both/on option from settings.
 *            - Removed beep(length) command, but it still works 
 *              in the speakText or playText fields.
 *      - Added settings for default alarm delay time and
 *        default strobe during delay.
 *      - Added commands customAlarm, customSiren, customBoth
 *      - Made it check the current alarmtype and only change it
 *        as needed which prevents it from always flashing before
 *        turning on.
 *      - Misc bug fixes.
 *
 *    1.1 (05/21/2016)
 *      - Improved polling functionality.
 *
 *    1.0.3 (05/04/2016 - 05/12/2016)
 *      - Enhanced reporting of status, alarm, and switch state.
 *      - Enhanced activity feed messages.
 *      - Enhanced debug logging.
 *      - Improved beep reliability a little bit.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "GoControl Multifunction Siren", namespace: "krlaframboise", author: "Kevin LaFramboise") {
		capability "Actuator"
		capability "Alarm"
		capability "Battery"
		capability "Music Player"
		capability "Audio Notification"
		capability "Speech Synthesis"
		capability "Polling"		
		capability "Switch"
		capability "Tone"
		
		attribute "lastPoll", "number"

		command "customBeep" // beepLengthMS, delaySeconds, useStobe
		command "customBoth" // delaySeconds, autoOffSeconds, useStrobe
		command "customSiren" // delaySeconds, autoOffSeconds, useStrobe
		command "customStrobe" // delaySeconds, autoOffSeconds
		
		// Audio Notification commands that are in documentation,
		// but not in the capability code.
		command "playTrackAtVolume"
		command "playSoundAndTrack"
		
		fingerprint mfr: "0109", prod: "2005", model: "0508" //Vision
		fingerprint mfr: "014F", prod: "2005", model: "0503" //Linear/GoControl
		fingerprint deviceId: "0x1000", inClusters: "0x25,0x80,0x70,0x72,0x86"
	}

	simulator {
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"
	}

	preferences {
		input "autoOffTime", "enum", 
			title: "1. Automatically turn off after:\n(You should disable this feature while testing)", 
			defaultValue: "30 Seconds",
			displayDuringSetup: true, 
			required: false,
			options: ["30 Seconds", "60 Seconds", "120 Seconds", "Disable Auto Off"]		
		input "bothAlarmTypeOverride", "enum",
			title: "2. What should the 'both' and 'on' commands turn on?\n(Some SmartApps like Smart Home Monitor use the both command so you can't use just the siren or the strobe.  This setting allows you to override the default action of those commands.)",
			defaultValue: "Siren and Strobe",
			displayDuringSetup: true,
			required: false,
			options: ["Siren and Strobe", "Siren Only", "Strobe Only"]
		input "alwaysSetAlarmType", "bool", 
			title: "3. Always Set Alarm Type?\n(Enabling this option will make the device turn on quicker, but the strobe will always flash once before turning on.)", 
			defaultValue: false, 
			displayDuringSetup: false, 
			required: false
		input "allowSirenOnlyLight", "bool",
			title: "4. Allow Siren Only Light?\n(When this option is enabled, the strobe light will stay on solid when using the siren command.)",
			defaultValue: false,
			displayDuringSetup: false,
			required: false
		input "alarmDelayStrobe", "bool", 
			title: "5. Default Use Strobe During Alarm Delay?", 
			defaultValue: false, 
			displayDuringSetup: true, 
			required: false
		input "alarmDelaySeconds", "number", 
			title: "6. Default Alarm Delay (seconds):", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false		
		input "beepLength", "number", 
			title: "7. Default Length of Beep (milliseconds):", 
			defaultValue: 0, 
			displayDuringSetup: true, 
			required: false
		input "debugOutput", "bool", 
			title: "8. Enable debug logging?", 
			defaultValue: true, 
			displayDuringSetup: true, 
			required: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"status", type: "generic", width: 6, height: 3, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'Off', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "alarmPending", label:'Alarm Pending!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "siren", label:'Siren On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "strobe", label:'Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "both", label:'Siren/Strobe On!', action: "off", icon:"st.alarm.alarm.alarm", backgroundColor:"#ff9999"
				attributeState "beep", label:'Beeping!', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor:"#99ff99"							
			}
		}
		standardTile("turnOff", "device.alarm", width: 2, height: 2) {
			state "default", label:'Off', action: "off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm", defaultState: true
			state "off", label:'Off', action: "off", backgroundColor: "#ffffff", icon:"st.alarm.alarm.alarm"
			state "alarmPending", label:'Cancel', action: "off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm"
		}	
		standardTile("testBeep", "device.status", width: 2, height: 2) {
			state "default", label:' Beep ', action:"beep", icon:"st.Entertainment.entertainment2", backgroundColor: "#99ff99"
			state "beep", label:'Beeping', action: "off", icon:"st.Entertainment.entertainment2", backgroundColor: "#99c2ff"			
		}					
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'Battery ${currentValue}%', unit:"", defaultState: true
		}		
		standardTile("testSiren", "device.alarm", width: 2, height: 2) {
			state "default", label:'Siren ', action: "alarm.siren", backgroundColor: "#ff9999", icon:"st.alarm.alarm.alarm", defaultState: true			
			state "siren", label:' Siren', action: "alarm.off", icon:"st.alarm.alarm.alarm", backgroundColor: "#99c2ff"		
		}
		standardTile("testStrobe", "device.alarm", width: 2, height: 2) {
			state "default", label:'Strobe', action: "alarm.strobe", backgroundColor: "#ff9999", icon:"st.alarm.alarm.alarm", defaultState: true			
			state "strobe", label:'Strobe', action: "alarm.off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm"
		}
		standardTile("testBoth", "device.alarm", width: 2, height: 2) {
			state "default", label:' Both ', action: "alarm.both", backgroundColor: "#ff9999", icon:"st.alarm.alarm.alarm", defaultState: true
			state "both", label:' Both ', action: "alarm.off", backgroundColor: "#99c2ff", icon:"st.alarm.alarm.alarm"
		}		
				
		main "status"
		details(["status", "testSiren", "testStrobe", "testBoth", "turnOff", "testBeep", "battery"])
	}
}

// Stores preferences and displays device settings.
def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 5000)) {
		state.lastUpdated = new Date().time
		state.debugOutput = validateBoolean(settings.debugOutput, true)
		state.alarmDelaySeconds = validateRange(settings.alarmDelaySeconds, 0, 0, Integer.MAX_VALUE, "alarmDelaySeconds") 
		state.alarmDelayStrobe = validateBoolean(settings.alarmDelayStrobe, false)
		logDebug "Updating"

		def cmds = []		
		
		if (!state.useSecureCommands) {
			logDebug "Checking for Secure Command Support"
			state.useSecureCommands = true
			cmds << supportedSecurityGetCmd()
			state.useSecureCommands = false
		}
		
		cmds += configure()
		
		response(delayBetween(cmds, 200))
	}
}

private configure() {
	def cmds = []
	
	if (state.isVisionMfr == null) {
		cmds << manufacturerGetCmd()
		cmds << "delay 5000"
	}
	
	cmds << autoOffSetCmd(getAutoOffTimeValue())		
	cmds << batteryGetCmd()
	
	return cmds
}

private getAutoOffTimeValue() {
	def result	
	switch (settings.autoOffTime) {
		case "60 Seconds":
			state.autoOffSeconds = 60
			result = 1
			break
		case "120 Seconds":
			state.autoOffSeconds = 120
			result = 2
			break
		case "Disable Auto Off":
			state.autoOffSeconds = 0
			result = 3
			break
		default:
			state.autoOffSeconds = 30
			result = 0 // 30 Seconds
	}
	return result
}

private isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time) 
}

def poll() {
	def result = []
	def minimumPollMinutes = 30
	def lastPoll = device.currentValue("lastPoll")
	if ((new Date().time - lastPoll) > (minimumPollMinutes * 60 * 1000)) {
		logDebug "Poll: Refreshing because lastPoll was more than ${minimumPollMinutes} minutes ago."
		result << batteryGetCmd()
	}
	else {
		logDebug "Poll: Skipped because lastPoll was within ${minimumPollMinutes} minutes"		
	}
	return result
}

// Turns on siren and strobe
def on() {
	both()	
}

def beep() {
	customBeep(settings.beepLength, 0, false)
}

// Turns on and then off after specified milliseconds.
def customBeep(beepLengthMS, delaySeconds=0, useStrobe=false) {	
	beepLengthMS = validateRange(beepLengthMS, 0, 0, Integer.MAX_VALUE, "Beep Length")
	delaySeconds = validateRange(delaySeconds, 0, 0, 3600, "Beep Delay")
	useStrobe = validateBoolean(useStrobe, false)
	
	state.activeAlarm = null
	sendEvent(getStatusEventMap("beep"))
	
	def result = []	
	def delayMsg = ""
	if (delaySeconds > 0) {
		delayMsg = " with ${delaySeconds} Second${useStrobe ? ' Strobe ' : ' '}Delay"
		
		if (useStrobe) {
			result << alarmTypeSetCmd(getStrobeOnlyAlarmType())
			result << switchOnSetCmd()
			result << "delay ${delaySeconds * 1000}"
			result << switchOffSetCmd()
			result << alarmTypeSetCmd(getSirenOnlyAlarmType())
		}
		else {
			result << "delay ${delaySeconds * 1000}"
		}
	}	
	
	result << alarmTypeSetCmd(getSirenOnlyAlarmType())
	result << switchOnSetCmd()
	
	if (beepLengthMS > 0) {
		result << "delay $beepLengthMS"
	}
	result += switchOffSetCmds()
	
	logDebug "Executing ${beepLengthMS} Millisecond Beep$delayMsg"
	
	return result	
}

// Turns on siren and strobe using default autooff
def both() {	
	customBoth(state.alarmDelaySeconds, state.autoOffSeconds, state.alarmDelayStrobe)
}

// Turns on siren and strobe using specified auto off, delay and whether or not it should strobe during delay.
def customBoth(delaySeconds, autoOffSeconds, useStrobe) {
	turnOn(getSirenAndStrobeAlarmType(), delaySeconds, autoOffSeconds, useStrobe)	
}

// Turns on strobe using default autooff
def strobe() {
	customStrobe(state.alarmDelaySeconds, state.autoOffSeconds)	
}

// Turn on strobe using specified autooff.
def customStrobe(delaySeconds, autoOffSeconds) {	
	turnOn(getStrobeOnlyAlarmType(), delaySeconds, autoOffSeconds, false)
}

// Turns on siren using default auto off
def siren() {
	customSiren(state.alarmDelaySeconds, state.autoOffSeconds, state.alarmDelayStrobe)
}

// Turns on siren using specified auto off, delay and whether or not it should strobe during delay.
def customSiren(delaySeconds, autoOffSeconds, useStrobe) {
	turnOn(getSirenOnlyAlarmType(), delaySeconds, autoOffSeconds, useStrobe)	
}

// Stores a map with the alarm settings and requests the
// current alarmType from the device.
def turnOn(alarmType, delaySeconds, autoOffSeconds, useStrobe) {
	delaySeconds = validateRange(delaySeconds, 0, 0, Integer.MAX_VALUE, "delaySeconds")
	autoOffSeconds = validateRange(autoOffSeconds, 0, 0, Integer.MAX_VALUE, "autoOffSeconds")
	useStrobe = validateBoolean(useStrobe, false)
	
	state.activeAlarm = [
		alarmType: alarmType,
		autoOffSeconds: autoOffSeconds,
		delaySeconds: delaySeconds,
		useStrobe: useStrobe
	]
	
	def result = []
	if (delaySeconds > 0 && !useStrobe) {
		logDebug "Alarm delayed by ${delaySeconds} seconds"
		result += startDelayedAlarm(null)
	}
	else if (validateBoolean(settings.alwaysSetAlarmType, false) || validateBoolean(settings.allowSirenOnlyLight, false)) {
		// Not checking current alarm type so turn on with null to make sure the alarm type gets set.
		result += turnOn(null)
	}
	else {
		// Check the device's current alarm type and when it responds, the handler will turn the device on.
		result << alarmTypeGetCmd()
	}
	return result
}

def turnOn(currentAlarmType) {
	def result = []
	def activeAlarm = state.activeAlarm
	
	if (activeAlarm) {	
		if (activeAlarm.delaySeconds > 2 && activeAlarm.useStrobe) {
			logDebug "Alarm delayed with strobe for ${activeAlarm.delaySeconds} seconds."
			result += startDelayedAlarm(currentAlarmType)
		}
		else {
			state.activeAlarm.alarmPending = false			
			if (activeAlarm.alarmType != currentAlarmType) {			
				if (device.currentValue("alarm") != "off") {
					logDebug "Turning off alarm because it's already on."
					result << switchOffSetCmd()
					result << "delay 200"
				}			
				result += alarmTypeSetCmds(activeAlarm.alarmType)
			}

			result << switchOnSetCmd()
			result << switchGetCmd()

			if (activeAlarm.autoOffSeconds) {
				logDebug "Turning on Alarm for ${activeAlarm.autoOffSeconds} seconds."
				result << "delay ${activeAlarm.autoOffSeconds * 1000}"
				result += switchOffSetCmds()			
			}	
			else {
				logDebug "Turning on Alarm"
			}
		}
	}
	return delayBetween(result, 100)
}

private startDelayedAlarm(currentAlarmType) {
	def result = []	
	def useStrobe = state.activeAlarm?.useStrobe
	
	if (useStrobe) {
		if (getStrobeOnlyAlarmType() != currentAlarmType) {
			result << alarmTypeSetCmd(getStrobeOnlyAlarmType())
		}
		result << switchOnSetCmd()
	}
	result << "delay ${state.activeAlarm.delaySeconds * 1000}"
	
	result << switchOffSetCmd()
	
	if (useStrobe && state.activeAlarm.alarmType != getStrobeOnlyAlarmType()) {
		result << alarmTypeSetCmd(getSirenOnlyAlarmType())
	}
	
	result << alarmTypeGetCmd()
			
	sendEvent(getStatusEventMap("alarmPending"))
	
	state.activeAlarm.delaySeconds = null
	state.activeAlarm.useStrobe = null
	state.activeAlarm.alarmPending = true
	
	return result
}

// Turns off siren and strobe
def off() {
	logDebug "Executing off() command"
	turnOff()
}

private turnOff() {
	return switchOffSetCmds()
}

private getSirenAndStrobeAlarmType() {
	def overriding = true
	def result
	switch (settings.bothAlarmTypeOverride) {
		case "Siren Only":
			result = getSirenOnlyAlarmType()
			break
		case "Strobe Only":
			result = getStrobeOnlyAlarmType()
			break
		default:
			overriding = false
			result = 0
	}
	if (overriding) {
		logDebug "Overriding \"both\" command with \"${settings.bothAlarmTypeOverride}\""
	}	
	return result
}

private getSirenOnlyAlarmType() {
	return 1
}

private getStrobeOnlyAlarmType() {
	return 2
}

private alarmTypeSetCmds(alarmType) {
	def result = []
	
	result << alarmTypeSetCmd(alarmType)
	
	if (alarmType == 1  && state.activeAlarm && !validateBoolean(settings.allowSirenOnlyLight, false)) {
		// Prevents strobe light from staying on when setting to Siren Only.
		result << switchBinaryGetCmd()
	}	
	return result	
}

private alarmTypeSetCmd(alarmType) {
	alarmType = validateRange(alarmType, 0, 0, 2, "Alarm Type")
	configSetCmd(getAlarmTypeParamNumber(), 1, alarmType)	
}

private alarmTypeGetCmd() {
	configGetCmd(getAlarmTypeParamNumber())
}

private getAlarmTypeParamNumber() {
	return state.isVisionMfr ? 1 : 0
}

private autoOffSetCmd(autoOff) {
	configSetCmd(getAutoOffParamNumber(), 1, validateRange(autoOff, 0, 0, 3, "Auto Off"))
}

private getAutoOffParamNumber() {
	return state.isVisionMfr ? 2 : 1
}

private configSetCmd(paramNumber, paramSize, paramValue) {
	secureCmd(zwave.configurationV1.configurationSet(parameterNumber: paramNumber, size: paramSize, configurationValue: [paramValue]))
}

private configGetCmd(paramNumber) {
	secureCmd(zwave.configurationV1.configurationGet(parameterNumber: paramNumber))
}

private manufacturerGetCmd() {
	secureCmd(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
}


private switchOnSetCmd() {
	secureCmd(zwave.basicV1.basicSet(value: 0xFF))
}

private switchOffSetCmds() {
	return [
		switchOffSetCmd(),
		switchGetCmd()
	]
}

private switchOffSetCmd() {
	secureCmd(zwave.basicV1.basicSet(value: 0x00))
}

private switchGetCmd() {	
	secureCmd(zwave.basicV1.basicGet())
}

private switchBinaryGetCmd() {
	secureCmd(zwave.switchBinaryV1.switchBinaryGet())
}

private batteryGetCmd() {
	secureCmd(zwave.batteryV1.batteryGet())
}

private supportedSecurityGetCmd() {	
	secureCmd(zwave.securityV1.securityCommandsSupportedGet())
}

private secureCmd(physicalgraph.zwave.Command cmd) {
	if (state.useSecureCommands) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		cmd.format()
	}
}

// Parses incoming message
def parse(String description) {	
	def result = []
	if (description.startsWith("Err")) {
		log.error "Unknown Error: $description"		
	}
	else if (description != null && description != "updated") {
		def cmd = zwave.parse(description, [0x71: 3, 0x85: 2, 0x70: 1, 0x30: 2, 0x26: 1, 0x25: 1, 0x20: 1, 0x72: 2, 0x80: 1, 0x86: 1, 0x59: 1, 0x73: 1, 0x98: 1, 0x7A: 1, 0x5A: 1])		
		if (cmd) {
			result += zwaveEvent(cmd)
		}
		else {
			logDebug "Unable to parse: $cmd"
		}
	}
	result << createEvent(name:"lastPoll", value: new Date().time, displayed: false, isStateChange: true)
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand([0x71: 3, 0x85: 2, 0x70: 1, 0x30: 2, 0x26: 1, 0x25: 1, 0x20: 1, 0x72: 2, 0x80: 1, 0x86: 1, 0x59: 1, 0x73: 1, 0x98: 1, 0x7A: 1, 0x5A: 1])	
	if (encapsulatedCmd) {	
		logDebug "encapsulated: $encapsulatedCmd"
		zwaveEvent(encapsulatedCmd)
	}
}

private versionGetCmd() {
	secureCmd(zwave.versionV1.versionGet())
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	//logDebug "BinaryReport: $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	if (cmd?.parameterNumber == getAlarmTypeParamNumber()) {		
		def val = cmd.configurationValue[0]
		logDebug "Current Alarm Type: ${val}"
		return response(turnOn(val))
	}	
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	//The Linear/GoControl product uses different parameter numbers than the Vision product.
	state.isVisionMfr = (cmd.manufacturerId == 265)	
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	def switchValue = (cmd.value == 0) ? "off" : "on"
	def alarmValue = null
	
	if (cmd.value == 0) {
		if (device.currentValue("alarm") != "off") {
			logDebug "Alarm is off"
		}
		state.activeAlarm = null
		alarmValue = "off"
	}
	else {		
		alarmValue = getLastAlarmStateValue()			
	}	
	
	return getCommandEvents(alarmValue, switchValue, alarmValue)
}

private getCommandEvents(alarmValue, switchValue, statusValue) {
	[		
		createEvent(
			getStatusEventMap(statusValue)
		),
		createEvent(
			name:"alarm",
			description: "Alarm is $alarmValue",
			value: alarmValue, 
			isStateChange: true, 
			displayed: false
		),
		createEvent(
			name:"switch", 
			description: "Switch is $switchValue",
			value: switchValue, 
			isStateChange: true, 
			displayed: false
		)
	]	
}

private getStatusEventMap(statusValue) {
	return [
		name: "status",
		description: "Status is $statusValue",
		value: statusValue, 
		isStateChange: true, 
		displayed: true
	]
}

private getLastAlarmStateValue() {
	def result
	switch (state.activeAlarm?.alarmType) {
		case 0:
			result = "both"
			break
		case 1:
			result = "siren"
			break
		case 2:
			result = "strobe"
			break			
		default:
			result = "off"
	}		
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ 
		name: "battery", 
		unit: "%"
	]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "Battery is low"
		map.isStateChange = true
		map.displayed = true
	} else {
		map.value = cmd.batteryLevel
		map.displayed = false
	}	
	[createEvent(map)]
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	state.useSecureCommands = true
	logDebug("Secure Commands Supported")	
	response(delayBetween(configure(), 200))
}

// Writes unexpected commands to debug log
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unknown Command: $cmd"	
}

def speak(text) {
	playText(text)
}

// Unsuported Music Player commands
def unmute() { handleUnsupportedCommand("unmute") }
def nextTrack() { handleUnsupportedCommand("nextTrack") }
def setLevel(number) { handleUnsupportedCommand("setLevel") }
def previousTrack() { handleUnsupportedCommand("previousTrack") }


private handleUnsupportedCommand(cmd) {
	log.info "Command $cmd is not supported"
}

// Turns siren off
def pause() { off() }
def stop() { off() }
def mute() { off() }

// Turns siren on
def play() { both() }

// Audio Notification commands
def playTrackAtVolume(String URI, Number volume) {
	logTrace "playTrackAtVolume($URI, $volume)"
	playTrack(URI, volume)
}

def playSoundAndTrack(String URI, Number duration=null, String track, Number volume=null) {
	logTrace "playSoundAndTrack($URI, $duration, $track, $volume)"
	playTrack(URI, volume)
}

def playTrackAndRestore(String URI, Number volume=null) {
	logTrace "playTrackAndRestore($URI, $volume)"
	playTrack(URI, volume) 
}

def playTrackAndResume(String URI, Number volume=null) {
	logTrace "playTrackAndResume($URI, $volume)"
	playTrack(URI, volume)
}

def playTrack(String URI, Number volume=null) {
	logTrace "playTrack($URI, $volume)"
	playText(getTextFromTTSUrl(URI), volume)
}

def getTextFromTTSUrl(ttsUrl) {
	def urlPrefix = "https://s3.amazonaws.com/smartapp-media/tts/"
	if (ttsUrl?.toString()?.toLowerCase()?.contains(urlPrefix)) {
		return ttsUrl.replace(urlPrefix,"").replace(".mp3","")
	}
	return ttsUrl
}

def playTextAndResume(String message, Number volume=null) {
	logTrace "playTextAndResume($message, $volume)"
	playText(message, volume) 
}

def playTextAndRestore(String message, Number volume=null) {
	logTrace "playTextAndRestore($message, $volume)"
	playText(message, volume=null) 
}

def playText(String message, Number volume=null) {
	logTrace "playText($message, $volume)"
	logDebug "Executing playText($message) Command"
	message = cleanMessage(message)
	def cmds
	switch (message) {
		case ["on", "play"]:
			cmds = both()
			break
		case ["stop", "off", "pause", "mute"]:
			cmds = off()
			break
		default:
			if (message) {
				cmds = parseComplexCommand(message)
			}
	}
	if (!cmds) {
		logDebug "'$message' is not a valid command."
	}
	else {
		return cmds
	}
}

def cleanMessage(message) {
	return message?.
		toLowerCase()?.
		replace(",", "_")?.
		replace(" ", "")?.
		replace("(", "")?.
		replace(")", "")
}

def parseComplexCommand(message) {	
	def cmds = []
	
	def args = getComplexCmdArgs(message)
	if (message.contains("beep")) {	
		if (!args || args?.size() == 0) {
			cmds += beep()
		}
		else if (args?.size() == 3) {
			cmds += customBeep(args[0], args[1], args[2])
		}
		else {			
			cmds += customBeep(args[0], 0, false)
		}		
	}	
	else if (message.contains("strobe")) {	
		cmds += (args?.size() == 2) ? customStrobe(args[0], args[1]) : strobe()
	}
	else if (message.contains("both")) {
		cmds += (args?.size() == 3) ? customBoth(args[0], args[1], args[2]) : both()
	}
	else if (message.contains("siren")) {
		cmds += (args?.size() == 3) ? customSiren(args[0], args[1], args[2]) : siren()
	}
	return cmds
}

private getComplexCmdArgs(message) {
	def args = removeCmdPrefix(message).tokenize("_")
	if (args.every { node -> isNumeric(node) || node in ["true","false"]}) {
		return args
	}
	else {
		return null
	}	
}

private removeCmdPrefix(message) {
	for (prefix in ["custombeep","beep","customboth","both","customsiren","siren","customstrobe","strobe"]) {
		if (message.startsWith(prefix)) {
			return message.replace("${prefix}_", "").replace("$prefix", "")
		}		
	}
	return message
}

private isNumeric(val) {
	return val?.toString()?.isNumber()
}

private validateBoolean(val, defaulVal) {
	if (val == null) {
		defaultVal
	}
	else {
		(val == true || val == "true")
	}
}

private currentValue(attributeName) {
	def val = device.currentValue(attributeName)
	return val ? val : ""	
}

private int validateRange(val, defaultVal, minVal, maxVal, desc) {
	try {
		def result
		def errorType = null
		if (isNumeric(val)) {
			result = val.toInteger()
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
	catch (e) {
		log.error "$desc: Using $defaultVal because $val validation generated error.  ($e)"
		return defaultVal
	}
}

def describeCommands() {
	return [
		"customBeep": [ display: "Custom Beep", description: "{0} Beep Length in Milliseconds", parameters:["number", "number", "bool"]], // beepLengthMS, delaySeconds, useStrobe
		"customBoth": [ display: "Custom Strobe and Siren", description: "(delaySeconds: {0}, autoOffSeconds: {1}, useStrobe: {2})", parameters:["number", "number", "bool"]],
		"customSiren": [ display: "Custom Siren", description: "", parameters:["number", "number", "bool"]], // delaySeconds, autoOffSeconds, useStrobe
		"customStrobe": [ display: "Custom Strobe", description: "", parameters:["number", "number"]] // delaySeconds, autoOffSeconds
	]
}

private logDebug(msg) {
	if (state.debugOutput || state.debugOutput == null) {
		log.debug "${device.displayName}: $msg"
	}
}

private logTrace(msg) {
	//log.trace "$msg"
}