/**
 *  Aeotec Siren 6 v1.2.2
 *  (Model: ZW164-A)
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *	URL to Documentation:  https://community.smartthings.com/t/release-aeotec-siren-6/164654?u=krlaframboise
 *
 *
 *  Changelog:
 *
 *    1.2.2 (12/12/2020)
 *      - Added custom presentation to prevent offline errors.
 *
 *    1.2.1 (09/13/2020)
 *      - Removed vid which makes it fully supported in the new mobile app.
 *      - Made setLevel temporarily change the level to prevent network errors.
 *
 *    1.1.5 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.1.4 (09/14/2019)
 *      - Added fingerprints for EU and AU models.
 *
 *    1.1.3 (07/09/2019)
 *      - Fixed issue with volume passed into the playText command being ignored.
 *
 *    1.1.2 (06/05/2019)
 *      - Fixed issue with default settings not populating after first save.
 *
 *    1.1.1 (06/01/2019)
 *      - Fixed setting ranges for firmware 1.4.
 *      - Misc code cleanup
 *
 *    1.1 (06/01/2019)
 *      - Rewrote a lot of the handler because of changes in the 1.4 firmware.
 *
 *    1.0.1 (05/17/2019)
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
import groovy.transform.Field

@Field LinkedHashMap VOLUME = [mute:0]
@Field LinkedHashMap TONE = [longest:22, shortest:29, tamper:17]
@Field LinkedHashMap INTERVAL_BETWEEN = [notStopping:0]
@Field LinkedHashMap LIGHT_EFFECT = [off:1, on:2, flash:32, strobe:64]
@Field LinkedHashMap CONTINUOUS_PLAY_COUNT = [continuous:0]
@Field LinkedHashMap INTERCEPT_LENGTH = [actualToneLength:0]
@Field LinkedHashMap PLAY_ASSOC = [groupId:7, endpoint:6]
@Field LinkedHashMap TAMPER_ASSOC = [groupId:3, endpoint:2]

metadata {
	definition (
		name: "Aeotec Siren 6",
		namespace: "krlaframboise",
		author: "Kevin LaFramboise",
		ocfDeviceType: "x.com.st.d.siren",
		mnmn: "SmartThingsCommunity",
		vid: "af7ec41e-3877-3b86-a4be-c9a3fbfb8a4e"
	) {
		capability "Actuator"
		capability "Alarm"
		capability "Switch"
		capability "Audio Notification"
		capability "Music Player"
		capability "Speech Synthesis"
		capability "Switch Level"
		capability "Tamper Alert"
		capability "Tone"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"

		attribute "lastCheckIn", "string"
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "firmwareVersion", "string"

		// Music Player commands used by some apps
		command "playSoundAndTrack"
		command "playTrackAtVolume"
		command "playText"
		command "playSound"

		fingerprint mfr:"0371", prod:"0003", model:"00A4", deviceJoinName:"Aeotec Siren 6" // EU
		fingerprint mfr:"0371", prod:"0103", model:"00A4", deviceJoinName:"Aeotec Siren 6" // US
		fingerprint mfr:"0371", prod:"0203", model:"00A4", deviceJoinName:"Aeotec Siren 6" // AU
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"primaryStatus", type: "generic", width: 6, height: 4){
			tileAttribute ("device.primaryStatus", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'OFF', action: "switch.on", icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
				attributeState "alarm", label: 'ALARM', action: "alarm.off", icon: "st.alarm.alarm.alarm", backgroundColor: "#e86d13"
				attributeState "chime", label: 'CHIME', action: "switch.off", icon: "st.alarm.beep.beep", backgroundColor: "#00a0dc"
				attributeState "on", label: 'ON', action: "switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc"
			}
			tileAttribute ("device.secondaryStatus", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'${currentValue}'
			}
		}

		standardTile("sliderText", "generic", width: 2, height: 2) {
			state "default", label:'Play Sound #'
		}

		controlTile("slider", "device.level", "slider",	height: 2, width: 4) {
			state "level", action:"switch level.setLevel"
		}

		standardTile("off", "device.alarm", width: 2, height: 2) {
			state "default", label:'Off', action: "alarm.off"
		}

		standardTile("on", "device.switch", width: 2, height: 2) {
			state "default", label:'On', action: "switch.on"
		}

		standardTile("beep", "device.tone", width: 2, height: 2) {
			state "default", label:'Chime', action: "tone.beep"
		}

		standardTile("siren", "device.alarm", width: 2, height: 2) {
			state "default", label:'Siren', action: "alarm.siren"
		}

		standardTile("strobe", "device.alarm", width: 2, height: 2) {
			state "default", label:'Strobe', action: "alarm.strobe"
		}

		standardTile("both", "device.alarm", width: 2, height: 2) {
			state "default", label:'Both', action: "alarm.both"
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh.refresh", icon:"st.secondary.refresh-icon"
		}

		standardTile("configure", "device.configure", width: 2, height: 2) {
			state "default", label:'Sync', action: "configuration.configure", icon:"st.secondary.tools"
		}

		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}

		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}

		main "primaryStatus"
		details(["primaryStatus", "sliderText", "slider", "off", "on", "beep", "siren", "strobe", "both", "refresh", "syncStatus", "configure", "firmwareVersion"])
	}

	preferences {
		getParamInput(manualSilenceParam)

		getOptionsInput("tamperVolume", "Tamper Volume", defaultTamperVolume, setDefaultOption(volumeOptions, defaultTamperVolume))

		getOptionsInput("switchOnAction", "Switch On Action", 0, setDefaultOption(switchOnActionOptions, "0"))

		getOptionsInput("sirenTone", "Siren Sound", defaultSirenTone, setDefaultOption(toneOptions, defaultSirenTone))

		getOptionsInput("sirenVolume", "Siren Volume", defaultSirenVolume, setDefaultOption(volumeOptions, defaultSirenVolume))

		getOptionsInput("strobeLightEffect", "Strobe Light Effect", defaultStrobeLightEffect, setDefaultOption(lightEffectOptions, defaultStrobeLightEffect))

		getOptionsInput("sirenRepeat", "Siren Repeat", defaultSirenRepeat, setDefaultOption(getRepeatOptions(true), defaultSirenRepeat))

		getOptionsInput("sirenRepeatDelay", "Siren Repeat Delay", defaultSirenRepeatDelay, setDefaultOption(repeatDelayOptions, defaultSirenRepeatDelay))

		getOptionsInput("sirenToneIntercept", "Siren Tone Intercept Length", defaultSirenToneIntercept, setDefaultOption(toneInterceptOptions, defaultSirenToneIntercept))

		getOptionsInput("chimeTone", "Default Chime Sound", defaultChimeTone, setDefaultOption(toneOptions, defaultChimeTone))

		getOptionsInput("chimeVolume", "Default Chime Volume", defaultChimeVolume, setDefaultOption(volumeOptions, defaultChimeVolume))

		getOptionsInput("chimeLightEffect", "Chime Light Effect", defaultChimeLightEffect, setDefaultOption(lightEffectOptions, defaultChimeLightEffect))

		getOptionsInput("chimeRepeat", "Chime Repeat", defaultChimeRepeat, setDefaultOption(getRepeatOptions(false), defaultChimeRepeat))

		getOptionsInput("chimeRepeatDelay", "Chime Repeat Delay", defaultChimeRepeatDelay, setDefaultOption(repeatDelayOptions, defaultChimeRepeatDelay))

		getOptionsInput("chimeToneIntercept", "Chime Tone Intercept Length", defaultChimeToneIntercept, setDefaultOption(toneInterceptOptions, defaultChimeToneIntercept))

		input "debugOutput", "bool",
			title: "Enable Debug Logging?",
			defaultValue: true,
			required: false
	}
}

private getParamInput(param) {
	getOptionsInput("configParam${param.num}", param.name, param.value, param.options)
}

private getOptionsInput(name, title, defaultVal, options) {
	input "${name}", "enum",
		title: "${title}:",
		required: false,
		defaultValue: defaultVal,
		displayDuringSetup: true,
		options: options
}

private getTamperVolumeSetting() {
	return safeToInt(settings?.tamperVolume, defaultTamperVolume)
}

private getSirenVolumeSetting() {
	return safeToInt(settings?.sirenVolume, defaultSirenVolume)
}

private getSirenToneSetting() {
	return safeToInt(settings?.sirenTone, defaultSirenTone)
}

private getStrobeLightEffectSetting() {
	return safeToInt(settings?.strobeLightEffect, defaultStrobeLightEffect)
}

private getSirenRepeatSetting() {
	return safeToInt(settings?.sirenRepeat, defaultSirenRepeat)
}

private getSirenRepeatDelaySetting() {
	return safeToInt(settings?.sirenRepeatDelay, defaultSirenRepeatDelay)
}

private getSirenToneInterceptSetting() {
	return safeToInt(settings?.sirenToneIntercept, defaultSirenToneIntercept)
}

private getChimeVolumeSetting() {
	return safeToInt(settings?.chimeVolume, defaultChimeVolume)
}

private getChimeToneSetting() {
	return safeToInt(settings?.chimeTone, defaultChimeTone)
}

private getChimeLightEffectSetting() {
	return safeToInt(settings?.chimeLightEffect, defaultChimeLightEffect)
}

private getChimeRepeatSetting() {
	return safeToInt(settings?.chimeRepeat, defaultChimeRepeat)
}

private getChimeRepeatDelaySetting() {
	return safeToInt(settings?.chimeRepeatDelay, defaultChimeRepeatDelay)
}

private getChimeToneInterceptSetting() {
	return safeToInt(settings?.chimeToneIntercept, defaultChimeToneIntercept)
}


private getDefaultTamperVolume() { return 10 }
private getDefaultSirenTone() { return 10 }
private getDefaultSirenVolume() { return 50 }
private getDefaultStrobeLightEffect() { return LIGHT_EFFECT.strobe }
private getDefaultSirenRepeat() { return 0 }
private getDefaultSirenRepeatDelay() { return 0 }
private getDefaultSirenToneIntercept() { return 0 }
private getDefaultChimeTone() { return 1 }
private getDefaultChimeVolume() { return 10 }
private getDefaultChimeLightEffect() { return LIGHT_EFFECT.flash }
private getDefaultChimeRepeat() { return 1 }
private getDefaultChimeRepeatDelay() { return 0 }
private getDefaultChimeToneIntercept() { return 0 }


def installed() {
	state.syncAll = true
}

def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		runIn(2, updateSyncStatus)

		def cmds = getConfigureCmds()
		return cmds ? response(cmds) : []

	}
}


def configure() {
	logDebug "configure()..."
	runIn(5, updateSyncStatus)

	if (!device.currentValue("switch")) {
		sendEvent(getEventMap("tamper", "clear"))
		sendEvent(getEventMap("alarm", "off"))
		sendEvent(getEventMap("switch", "off"))
		sendEvent(getEventMap("level", 0))
	}

	if (!device.currentValue("checkInterval")) {
		initializeCheckin()
	}

	state.syncAll = true
	def cmds = getConfigureCmds()
	return cmds ? delayBetween(cmds, 250) : []
}

private initializeCheckin() {
	def checkInterval = (6 * 60 * 60) + (5 * 60)

	sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	startHealthPollSchedule()
}


def ping() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		logDebug "ping()"

		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()

		return [versionGetCmd()]
	}
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	runEvery3Hours(healthPoll)
}

def healthPoll() {
	logDebug "healthPoll()"
	sendHubCommand([new physicalgraph.device.HubAction(versionGetCmd())])
}

private getConfigureCmds() {
	def cmds = []

	if (state.syncAll || !state.playAssocSet) {
		cmds << associationSetCmd(PLAY_ASSOC.groupId)
		cmds << associationGetCmd(PLAY_ASSOC.groupId)
	}

	if (state.syncAll || !device.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
	}

	if (state.syncAll || tamperVolumeSetting != state.tamperVolume) {
		cmds << soundSwitchConfigSetCmd(tamperVolumeSetting, TONE.tamper, TAMPER_ASSOC.endpoint)
		cmds << soundSwitchConfigGetCmd(TAMPER_ASSOC.endpoint)
	}

	configParams.each {
		def storedVal = getParamStoredValue(it.num)
		if (state.syncAll || "${it.value}" != "${storedVal}") {
			logDebug "CHANGING ${it.name}(#${it.num}) from ${storedVal} to ${it.value}"
			cmds << configSetCmd(it, it.value)
			cmds << configGetCmd(it)
		}
	}

	state.syncAll = false

	return cmds
}


// Music Player Commands
def play() {
	return playSound(chimeToneSetting)
}

def pause() {
	return off()
}

def stop() {
	return off()
}


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

def playTrack(URI, volume=null) {
	def text = getTextFromTTSUrl(URI)
	playText(!text ? URI : text, volume)
}

private getTextFromTTSUrl(URI) {
	if (URI?.toString()?.contains("/")) {
		def startIndex = URI.lastIndexOf("/") + 1
		return URI.substring(startIndex, URI.size())?.toLowerCase()?.replace(".mp3","")
	}
	return null
}

def playText(message, volume=null) {
	playSound(message, volume)
}


def setLevel(level, duration=null) {
	sendEvent(name:"level", value:level)
	runIn(2, resetLevel)
	playSound(level)
}

def resetLevel() {
	sendEvent(name:"level", value:0)
}


def playSound(soundNumber, volume=null) {
	logDebug "playSound(${soundNumber}" + (volume ? ", ${volume}" : "") + ")"

	def cmds = []
	if (canPlaySound("playSound(${soundNumber})")) {

		def tone = validateTone(soundNumber)
		if (tone) {

			state.lastAction = "chime"

			def configVal = getGroupConfigVal(chimeLightEffectSetting, chimeToneInterceptSetting, chimeRepeatDelaySetting, chimeRepeatSetting)
			cmds << configSetCmd(siren1GroupParam, configVal)

			volume = (volume == null) ? chimeVolumeSetting : validateVolume(volume)
			cmds << soundSwitchConfigSetCmd(volume, tone, PLAY_ASSOC.endpoint)

			cmds << soundSwitchTonePlaySetCmd(tone, PLAY_ASSOC.endpoint)
		}
		else {
			log.warn "Ignoring playSound(${soundNumber}) because soundNumber must be between 1 and 30."
		}
	}
	return cmds ? delayBetween(cmds, 250) : []
}

private canPlaySound(cmd) {
	def alarm = device.currentValue("alarm")
	if (alarm == "off") {
		return true
	}
	else {
		log.warn "Ignoring ${cmd} because alarm is ${alarm}"
		return false
	}
}

private validateTone(val) {
	def tone = safeToInt(val)
	if (tone < 1 || tone > 30) tone = 0
	return tone
}

private validateVolume(volume) {
	def vol = safeToInt(volume, 50)
	if (vol > 100) vol = 100
	if (vol < 0) vol = 0
	return vol
}


def beep() {
	return playSound(chimeToneSetting)
}


def on() {
	logDebug "on()..."
	def cmds = []

	if (canPlaySound("on()")) {
		def switchAction
		switch (settings?.switchOnAction) {
			case "led":
				switchAction = "on"
				state.lastAction = "on"
				cmds = getSirenStrobeCmds(VOLUME.mute, LIGHT_EFFECT.on, TONE.longest, CONTINUOUS_PLAY_COUNT.continuous) // Using a tone that's over a minute long reduces the frequency of it flashing when the tone starts over.
				break
			case "chime":
				switchAction = "chime"
				cmds = playSound(chimeToneSetting)
				break
			case "siren":
				switchAction = "siren"
				cmds = siren()
				break
			case "strobe":
				switchAction = "siren"
				cmds = strobe()
				break
			case "both":
				switchAction = "siren"
				cmds = both()
				break
			default:
				def sound = safeToInt(settings?.switchOnAction, 0)
				if (sound) {
					switchAction = "chime"
					cmds = playSound(sound)
				}
				else {
					log.warn "Ignoring 'on' command because the Switch On Action setting is set to 'Do Nothing'"
				}
		}
		state.switchAction = switchAction
	}
	return cmds
}


def siren() {
	logDebug "siren()..."
	state.lastAction = "siren"
	return getSirenStrobeCmds(sirenVolumeSetting, LIGHT_EFFECT.off)
}

def strobe() {
	logDebug "strobe()..."
	state.lastAction = "strobe"
	return getSirenStrobeCmds(VOLUME.mute, strobeLightEffectSetting)
}

def both() {
	logDebug "both()..."
	state.lastAction = "both"
	return getSirenStrobeCmds(sirenVolumeSetting, strobeLightEffectSetting)
}

private getSirenStrobeCmds(volume, lightEffect, tone=sirenToneSetting, continuousPlayCount=sirenRepeatSetting) {
	def configVal = getGroupConfigVal(lightEffect, sirenToneInterceptSetting, sirenRepeatDelaySetting, continuousPlayCount)

	return delayBetween([
		configSetCmd(siren1GroupParam, configVal),
		soundSwitchConfigSetCmd(volume, tone, PLAY_ASSOC.endpoint),
		soundSwitchTonePlaySetCmd(tone, PLAY_ASSOC.endpoint)
	], 200)
}


def off() {
	logDebug "off()..."
	return [ soundSwitchTonePlaySetCmd(0, PLAY_ASSOC.endpoint) ]
}


def refresh() {
	logDebug "refresh()..."

	updateSyncStatus()

	resetTamper()

	return [ basicGetCmd() ]
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

private associationSetCmd(group) {
	return secureCmd(zwave.associationV2.associationSet(groupingIdentifier:group, nodeId:[zwaveHubNodeId]))
}

private associationGetCmd(group) {
	return secureCmd(zwave.associationV2.associationGet(groupingIdentifier:group))
}

private basicGetCmd(endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicGet(), endpoint)
}

private basicSetCmd(value, endpoint=null) {
	return multiChannelCmdEncapCmd(zwave.basicV1.basicSet(value: value), endpoint)
}

private soundSwitchConfigGetCmd(endpoint) {
	return soundSwitchCmd("06", endpoint)
}

private soundSwitchConfigSetCmd(volume, tone, endpoint) {
	return soundSwitchCmd("05${convertToHex(volume)}${convertToHex(tone)}", endpoint)
}

private soundSwitchTonePlayGetCmd(endpoint) {
	return soundSwitchCmd("09", endpoint)
}

private soundSwitchTonePlaySetCmd(tone, endpoint) {
	return soundSwitchCmd("08${convertToHex(tone)}", endpoint)
}

private soundSwitchCmd(cmd, endpoint=null) {
	cmd = "79${cmd}"

	if (endpoint) {
		cmd = "600D00${convertToHex(endpoint)}${cmd}"
	}
	return secureRawCmd(cmd)
}

private multiChannelCmdEncapCmd(cmd, endpoint) {
	if (endpoint) {
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd))
	}
	else {
		return secureCmd(cmd)
	}
}

private configSetCmd(param, value) {
	if ("${value}".isInteger()) {
		return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
	}
	else {
		return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, configurationValue: value))
	}
}

private configGetCmd(param) {
	return secureCmd(zwave.configurationV2.configurationGet(parameterNumber: param.num))
}

private secureCmd(cmd) {
	if (isSecurityEnabled()) {
		return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else {
		return cmd.format()
	}
}

private secureRawCmd(cmd) {
	if (isSecurityEnabled()) {
		return "988100${cmd}"
	}
	else {
		return cmd
	}
}

private isSecurityEnabled() {
	return zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))
}


private getCommandClassVersions() {
	[
		0x20: 1,	// Basic
		0x59: 1,	// AssociationGrpInfo
		0x55: 1,	// Transport Service (V2)
		0x5A: 1,	// DeviceResetLocally
		0x5E: 2,	// ZwaveplusInfo
		0x60: 3,	// MultiChannel (v)
		0x6C: 1,	// Supervision
		0x70: 1,	// Configuration
		0x71: 3,	// Notification (v4)
		0x72: 2,	// ManufacturerSpecific
		0x73: 1,	// Powerlevel
		0x79: 1,	// Sound Switch
		0x7A: 2,	// Firmware Update Md
		0x85: 2,	// Association
		0x86: 1,	// Version (2)
		0x8E: 2,	// Multi Channel Association
		0x98: 1,	// Security 0
		0x9F: 1		// Security 2
	]
}


// private getLightEffectConfigVal(brightenTime, dimTime, onTime, offTime) {
	// return [brightenTime, dimTime, onTime, offTime]
// }

private getGroupConfigVal(lightEffect, intercept, intervalBetween, continuousPlayCount) {
	def configVal = [lightEffect, intercept, intervalBetween, continuousPlayCount]

	logTrace "getGroupConfigVal(lightEffect:${lightEffect}, intercept:${intercept}, intervalBetween:${intervalBetween}, continuousPlayCount:${continuousPlayCount}) = ${configVal}"

	return configVal
}


def parse(String description) {
	def result = []
	try {
		if ("${description}".startsWith("Err 106")) {
			log.warn "secure inclusion failed"
		}
		else if ("${description}".contains("command: 9881, payload: 00 79") || "${description}".contains("command: 79")) {
			logTrace "soundSwitchEvent(${description})"
		}
		else {
			def cmd = zwave.parse(description, commandClassVersions)
			if (cmd) {
				result += zwaveEvent(cmd)
			}
			else {
				log.warn "Unable to parse: $description"
			}
		}

		if (!isDuplicateCommand(state.lastCheckInTime, 59000)) {
			state.lastCheckInTime = new Date().time
			sendEvent(getEventMap("lastCheckIn", convertToLocalTimeString(new Date())))
		}
	}
	catch (e) {
		log.error "${e}"
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


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 3])

	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
	}
	else if (cmd.commandClass == 121) {
		return soundSwitchEvent(cmd)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
		return []
	}
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "NotificationReport: $cmd"

	switch(cmd.notificationType) {
		case 7:
			handleTamperEvent(cmd.event == 9 ? "detected" : "clear")
			break
		case 8:
			// Ignore button battery notifications
			break
		case 0x0E:
			// Ignore siren notifications
			break
		default:
			logTrace "Unknown Notification Type: ${cmd}"
	}
	return []
}

private handleTamperEvent(value) {
	sendEventIfNew("tamper", value, true)
	sendEventIfNew("secondaryStatus", value == "detected" ? "TAMPERING" : "")
	if (value == "detected") {
		runIn(5, resetTamper)
	}
}

def resetTamper() {
	sendEventIfNew("tamper", "clear", true)
	sendEventIfNew("secondaryStatus", "")
}


def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	logTrace "AssociationReport: ${cmd}"

	if (cmd.groupingIdentifier == PLAY_ASSOC.groupId && zwaveHubNodeId in cmd.nodeId) {
		updateSyncStatus("Syncing...")
		runIn(5, updateSyncStatus)

		state.playAssocSet = true
	}
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"

	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"
	logDebug "Firmware: ${version}"
	sendEventIfNew("firmwareVersion", version)
	return []
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "ConfigurationReport: ${cmd}"

	updateSyncStatus("Syncing...")
	runIn(5, updateSyncStatus)

	def param = allConfigParams.find { it.num == cmd.parameterNumber }
	if (param) {
		def val = cmd.size == 1 ? cmd.configurationValue[0] : cmd.scaledConfigurationValue

		logDebug "${param.name}(#${param.num}) = ${val} ${cmd.size == 4 ? cmd.configurationValue : ''}"
		setParamStoredValue(param.num, val)
	}
	else {
		logTrace "Unknown Parameter #${cmd.parameterNumber} = ${val}"
	}
	return []
}


def updateSyncStatus(status=null) {
	if (status == null) {
		def changes = getPendingChanges()
		if (changes > 0) {
			status = "${changes} Pending Change" + ((changes > 1) ? "s" : "")
		}
		else {
			status = "Synced"
		}
	}
	if ("${syncStatus}" != "${status}") {
		sendEvent(getEventMap("syncStatus", status))
	}
}

private getSyncStatus() {
	return device.currentValue("syncStatus")
}

private getPendingChanges() {
	def pendingConfigParams = configParams.count { isConfigParamSynced(it) ? 0 : 1 }
	def pendingTamper = (tamperVolumeSetting != state.tamperVolume) ? 1 : 0
	def pendingPlayAssoc = !state.playAssocSet ? 1 : 0
	return (pendingConfigParams + pendingTamper + pendingPlayAssoc)
}

private isConfigParamSynced(param) {
	return ("${param.value}" == "${getParamStoredValue(param.num)}")
}

private getParamStoredValue(paramNum) {
	return state["configVal${paramNum}"]
}

private setParamStoredValue(paramNum, value) {
	state["configVal${paramNum}"] = value
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "BasicReport: ${cmd}"
	handleBasicEvent(cmd.value)
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, endpoint=null) {
	logTrace "BasicSet: ${cmd}"
	handleBasicEvent(cmd.value)
	return []
}

private handleBasicEvent(rawVal) {
	def lastAction = state.lastAction
	def statusVal = "off"
	def switchVal = "off"
	def alarmVal = "off"

	if (rawVal) {
		switch (lastAction) {
			case "on":
				statusVal = "on"
				switchVal = "on"
				break
			case "chime":
				statusVal = "chime"
				if (state.switchAction == "chime") {
					switchVal = "on"
				}
				break
			case { it in ["siren", "strobe", "both"] }:
				alarmVal = lastAction
				statusVal = "alarm"
				if (state.switchAction == "siren") {
					switchVal = "on"
				}
				break
		}
	}
	else {
		state.lastAction = null
		state.switchAction = null
	}

	sendEventIfNew("alarm", alarmVal, true)
	sendEventIfNew("switch", switchVal, true)
	sendEventIfNew("primaryStatus", statusVal, (lastAction == "chime"))
}


def soundSwitchEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	logDebug "soundSwitchEvent: ${cmd}"

	switch (cmd.command) {
		case 7:
			updateSyncStatus("Syncing...")
			runIn(5, updateSyncStatus)

			handleSoundSwitchConfigurationReport(cmd)
			break
		case 10:
			// handleSoundSwitchTonePlayReport(cmd)
			break
		default:
			logDebug "Unknown Sound Switch Command: ${cmd}"
	}
	return []
}

private handleSoundSwitchConfigurationReport(cmd) {
	if (cmd.parameter?.size() == 2) {
		def volume = cmd.parameter[0]
		def tone = cmd.parameter[1]
		if (cmd.sourceEndPoint == TAMPER_ASSOC.endpoint) {
			logDebug "Tamper - Volume:${volume}, Tone:${tone}"
			state.tamperVolume = volume
		}
	}
}


def zwaveEvent(physicalgraph.zwave.Command cmd, endpoint=null) {
	logTrace "Ignored zwaveEvent: ${cmd}" + (endpoint ? " (endpoint: ${endpoint})" : "")
	return []
}


private getConfigParams() {
	def params = [
		tamperGroupParam,
		manualSilenceParam
	]
	params += lightEffectParams
	return params
}

private getAllConfigParams() {
	def params = [
		tamperGroupParam,
		// button1GroupParam,
		// button2GroupParam,
		// button3GroupParam,
		siren1GroupParam,
		// siren2GroupParam,
		// siren3GroupParam,
		// basicSetGroup2Param,
		// basicSetGroup3Param,
		// basicSetGroup4Param,
		// basicSetGroup5Param,
		// basicSetGroup6Param,
		// basicSetGroup7Param,
		// basicSetGroup8Param,
		// basicSetGroup9Param,
		// triggerUnpairingModeParam,
		// triggerPairingModeParam
		activePairingButtonParam,
		pairingResultsParam,
		// button1InfoParam,
		// button2InfoParam,
		// button3InfoParam,
		manualSilenceParam
	]
	params += lightEffectParams
	return params
}

private getLightEffectParams() {
	return [
		lightEffect1Param,
		lightEffect2Param,
		lightEffect3Param,
		lightEffect4Param,
		lightEffect5Param,
		lightEffect6Param,
		lightEffect7Param
	]
}

private getTamperGroupParam() {
	return getParam(2, "Tamper Group", 4, 0x20030001)
}

// private getButton1GroupParam() {
	// return getParam(3, "Button 1 Group", 4, 0x02000001)
// }

// private getButton2GroupParam() {
	// return getParam(4, "Button 2 Group", 4, 0x02000001)
// }

// private getButton3GroupParam() {
	// return getParam(5, "Button 3 Group", 4, 0x02000001)
// }

private getSiren1GroupParam() {
	return getParam(6, "Siren 1 Group", 4, 0x04000000)
}

// private getSiren2GroupParam() {
	// return getParam(7, "Siren 2 Group", 4, 0x04000000)
// }

// private getSiren3GroupParam() {
	// return getParam(8, "Siren 3 Group", 4, 0x04000000)
// }


private getLightEffect1Param() {
	return getParam(16, "Off", 4, 0x0000000A)
	// brighten: 0-127 (unit=20ms)
	// dim: 0-127 (unit=20ms)
	// on: 0-255 (unit=100ms)
	// off: 0-255 (unit=100ms)
}

private getLightEffect2Param() {
	return getParam(17, "On", 4, 0x00007F00)
}

private getLightEffect3Param() {
	return getParam(18, "Slow Pulse", 4, 0x7F7F1414)
}

private getLightEffect4Param() {
	return getParam(19, "Pulse", 4, 0x64640808)
}

private getLightEffect5Param() {
	return getParam(20, "Fast Pulse", 4, 0x64640000)
}

private getLightEffect6Param() {
	return getParam(21, "Flash", 4, 0x00000A0A)
}

private getLightEffect7Param() {
	return getParam(22, "Strobe", 4, 0x00000101)
}

// private getBasicSetGroup2Param() {
	// return getParam(32, "Send Basic Set to Group 2", 1, 3)
	// // 0: start-nothing, stop-nothing
	// // 1: start-0xFF, stop-nothing
	// // 2: start-0x00, stop-nothing
	// // 3: start-0xFF, stop-0x00
	// // 4: start-0x00, stop-0xFF
// }

// private getBasicSetGroup3Param() {
	// return getParam(33, "Send Basic Set to Group 3", 1, 3)
// }

// private getBasicSetGroup4Param() {
	// return getParam(34, "Send Basic Set to Group 4", 1, 3)
// }

// private getBasicSetGroup5Param() {
	// return getParam(35, "Send Basic Set to Group 5", 1, 3)
// }

// private getBasicSetGroup6Param() {
	// return getParam(36, "Send Basic Set to Group 5", 1, 3)
// }

// private getBasicSetGroup7Param() {
	// return getParam(37, "Send Basic Set to Group 7", 1, 3)
// }

// private getBasicSetGroup8Param() {
	// return getParam(38, "Send Basic Set to Group 8", 1, 3)
// }

// private getBasicSetGroup9Param() {
	// return getParam(39, "Send Basic Set to Group 9", 1, 3)
// }

// private getTriggerUnpairingModeParam() {
	// return getParam(48, "Trigger Unpairing Mode", 1)  // write only
// }

// private getTriggerPairingModeParam() {
	// return getParam(49, "Trigger Pairing Mode", 1)  // write only
// }

private getActivePairingButtonParam() {
	return getParam(50, "Active Pairing Button", 1)  // read only (automatically sent)
}

private getPairingResultsParam() {
	return getParam(51, "Pairing Results", 1)  // read only (automatically sent)
}

// private getButton1InfoParam() {
	// return getParam(52, "Button 1 Information", 4) (read only)
// }

// private getButton2InfoParam() {
	// return getParam(53, "Button 2 Information", 4) (read only)
// }

// private getButton3InfoParam() {
	// return getParam(54, "Button 3 Information", 4) (read only)
// }

private getManualSilenceParam() {
	return getParam(96, "Silence Alarm with Action Button", 1, 0, [0:"Disabled", 1:"Enabled"])
}


private getParam(num, name, size, defaultVal=null, options=null) {
	def val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	def map = [num: num, name: name, size: size, value: val]
	if (options) {
		map.valueName = options?.find { k, v -> "${k}" == "${val}" }?.value
		map.options = setDefaultOption(options, defaultVal)
	}

	return map
}

private setDefaultOption(options, defaultVal) {
	return options?.collectEntries { k, v ->
		if ("${k}" == "${defaultVal}") {
			v = "${v} [DEFAULT]"
		}
		["$k": "$v"]
	}
}


private getVolumeOptions() {
	def options = ["0":"Mute", "1":"1%"]

	(1..20).each {
		options["${it * 5}"] = "${it * 5}%"
	}

	return options
}

private getToneOptions() {
	def options = [:]

	(1..30).each {
		options["${it}"] = "Tone #${it}"
	}

	return options
}

private getLightEffectOptions() {
	def options = [1:lightEffectParams[0].name]
	def val = 1
	(1..6).each {
		val = (val * 2)
		options["${val}"] = lightEffectParams[it].name
	}
	return options
}

private getRepeatOptions(includeUnlimited) {
	def options = [:]
	if (includeUnlimited) {
		options["0"] = "Unlimited"
	}
	(1..15).each {
		options["${it}"] = "${it}"
	}
	(4..50).each {
		options["${it * 5}"] = "${it * 5}"
	}
	return options
}

private getRepeatDelayOptions() {
	def options = [
		0:"No Delay"
	]
	options += durationOptions
	return options
}

private getToneInterceptOptions() {
	def options = [
		0:"Play Entire Tone"
	]
	options += durationOptions
	return options
}

private getDurationOptions() {
	def options = [
		1:"1 Second"
	]
	(2..15).each {
		options["${it}"] = "${it} Seconds"
	}
	(4..50).each {
		options["${it * 5}"] = "${it * 5} Seconds"
	}
	return options
}

private getSwitchOnActionOptions() {
	def options = [
		"0":"Do Nothing",
		"chime": "Play Default Chime",
		"led": "Turn On LED",
		"siren": "Turn On Siren",
		"strobe": "Turn On Strobe",
		"both": "Turn On Siren/Strobe"
	]

	(1..30).each {
		options["${it}"] = "Play Tone #${it}"
	}
	return options
}


private sendEventIfNew(attr, newValue, displayed=false, unit=null) {
	if (device.currentValue("${attr}") != newValue) {
		sendEvent(getEventMap("${attr}", newValue, displayed, unit))
	}
}

private getEventMap(name, value, displayed=false, unit=null) {
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true,
		descriptionText: "${device.displayName} - ${value}"
	]

	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText} ${unit}"
	}

	if (displayed) {
		logDebug "${name} is ${value}"
	}
	return eventMap
}


private convertToHex(num) {
	return Integer.toHexString(num).padLeft(2, "0").toUpperCase()
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
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