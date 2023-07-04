/**
 *  Aeotec Doorbell 6 v1.3.1
 *  (Model: ZW162-A)
 *
 *  Author: 
 *    Kevin LaFramboise (krlaframboise)
 *
 *	URL to Documentation: https://community.smartthings.com/t/release-aeotec-doorbell-6/165030
 *    
 *
 *  Changelog:
 *
 *    1.3.1 (09/13/2020)
 *      - Removed vid which makes it fully supported in the new mobile app.
 *      - Made setLevel temporarily change the level to prevent network errors.
 *
 *    1.2 (05/09/2020)
 *      - *** POSSIBLE BREAKING CHANGES - TEST AFTER UPDATING ***
 *      - Switched to notification reports and sound switch events with endpoints to eliminate the need to use association groups because SmartThings stopped supporting them.
 *      - Implemented Chime capability.
 *
 *    1.1.4 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.1.3 (09/14/2019)
 *      - Added fingerprints for EU and AU models.
 *
 *    1.1.2 (07/09/2019)
 *      - Fixed issue with volume passed into the playText command being ignored.
 *
 *    1.1.1 (06/05/2019)
 *      - Fixed issue with default settings not populating after first save.
 *
 *    1.1 (06/01/2019)
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
@Field LinkedHashMap BUTTON_MODE = [normal:0, pairing:1, unpairing:2]
@Field LinkedHashMap BUTTON1 = [num:1, infoParamNum:52, groupParamNum:3, endpoint:3, assocGroupId:4, pairingMode:1]
@Field LinkedHashMap BUTTON2 = [num:2, infoParamNum:53, groupParamNum:4, endpoint:4, assocGroupId:5, pairingMode:2]
@Field LinkedHashMap BUTTON3 = [num:3, infoParamNum:54, groupParamNum:5, endpoint:5, assocGroupId:6, pairingMode:4]
 
metadata {
	definition (
		name: "Aeotec Doorbell 6", 
		namespace: "krlaframboise", 
		author: "Kevin LaFramboise",
		ocfDeviceType: "x.com.st.d.siren"
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
		capability "Chime"
		// capability "Configuration"
		capability "Refresh"
		capability "Health Check"		
				
		attribute "lastCheckIn", "string"
		attribute "primaryStatus", "string"
		attribute "secondaryStatus", "string"
		attribute "firmwareVersion", "string"
		
		(1..3).each {
			attribute "btn${it}Name", "string"
			attribute "btn${it}Switch", "string"
			attribute "btn${it}Action", "string"
			command "pairRemoveButton${it}"
			command "toggleButton${it}"
		}
		
		command "configure"
		
		// Music Player commands used by some apps
		command "playSoundAndTrack"
		command "playTrackAtVolume"
		command "playText"
		command "playSound"
		
		fingerprint mfr:"0371", prod:"0003", model:"00A2", deviceJoinName:"Aeotec Doorbell 6" // EU
		fingerprint mfr:"0371", prod:"0103", model:"00A2", deviceJoinName:"Aeotec Doorbell 6" // US		
		fingerprint mfr:"0371", prod:"0203", model:"00A2", deviceJoinName:"Aeotec Doorbell 6" // AU		
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
		
		standardTile("configure", "device.generic", width: 2, height: 2) {
			state "default", label:'Sync', action: "configure", icon:"st.secondary.tools"
		}
		
		valueTile("syncStatus", "device.syncStatus", decoration:"flat", width:2, height: 2) {
			state "syncStatus", label:'${currentValue}'
		}
		
		standardTile("btn1Label", "device.generic", width: 2, height: 1, decoration:"flat") {
			state "default", label:'Button 1:'
		}

		standardTile("btn1Name", "device.btn1Name", width: 2, height: 1, decoration:"flat") {
			state "default", label:'${currentValue}'
		}		
		standardTile("btn1Switch", "device.btn1Switch", width: 2, height: 2) {
			state "empty", label:'', backgroundColor: "#ffffff"			
			state "on", label:'ON', action: "toggleButton1", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action: "toggleButton1", backgroundColor: "#ffffff"
		}		
		standardTile("btn1Action", "device.btn1Action", width: 2, height: 2) {
			state "pair", label:'Pair', action: "pairRemoveButton1"			
			state "pairing", label:'Pairing...', backgroundColor: "#CCCC00"
			state "confirm", label:'Confirm Remove', action: "pairRemoveButton1", backgroundColor:"#ff0000"
			state "remove", label:'Remove', action: "pairRemoveButton1"
		}
		
		standardTile("btn2Label", "device.generic", width: 2, height: 1, decoration:"flat") {
			state "default", label:'Button 2:'
		}
		standardTile("btn2Name", "device.btn2Name", width: 2, height: 1, decoration:"flat") {
			state "default", label:'${currentValue}'
		}		
		standardTile("btn2Switch", "device.btn2Switch", width: 2, height: 2) {
			state "empty", label:'', backgroundColor: "#ffffff"
			state "on", label:'ON', action: "toggleButton2", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action: "toggleButton2", backgroundColor: "#ffffff"
		}		
		standardTile("btn2Action", "device.btn2Action", width: 2, height: 2) {
			state "pair", label:'Pair', action: "pairRemoveButton2"			
			state "pairing", label:'Pairing...', backgroundColor: "#CCCC00"
			state "confirm", label:'Confirm Remove', action: "pairRemoveButton2", backgroundColor:"#ff0000"
			state "remove", label:'Remove', action: "pairRemoveButton2"
		}
		
		standardTile("btn3Label", "device.generic", width: 2, height: 1, decoration:"flat") {
			state "default", label:'Button 3:'
		}
		standardTile("btn3Name", "device.btn3Name", width: 2, height: 1, decoration:"flat") {
			state "default", label:'${currentValue}'
		}		
		standardTile("btn3Switch", "device.btn3Switch", width: 2, height: 2) {
			state "empty", label:'', backgroundColor: "#ffffff"
			state "on", label:'ON', action: "toggleButton3", backgroundColor: "#00a0dc"
			state "off", label:'OFF', action: "toggleButton3", backgroundColor: "#ffffff"
		}		
		standardTile("btn3Action", "device.btn3Action", width: 2, height: 2) {
			state "pair", label:'Pair', action: "pairRemoveButton3"			
			state "pairing", label:'Pairing...', backgroundColor: "#CCCC00"
			state "confirm", label:'Confirm Remove', action: "pairRemoveButton3", backgroundColor:"#ff0000"
			state "remove", label:'Remove', action: "pairRemoveButton3"
		}
		
		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}
		
		main "primaryStatus"
		details(["primaryStatus", "sliderText", "slider", "off", "on", "beep", "siren", "strobe", "both", "refresh", "syncStatus", "configure", "btn1Label", "btn1Switch", "btn1Action","btn1Name","btn2Label","btn2Switch","btn2Action","btn2Name","btn3Label","btn3Switch","btn3Action","btn3Name","firmwareVersion"])
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
	

// Setting Defaults
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
	logDebug "installed()..."
	state.syncAll = true
}

def updated() {	
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time
		
		logDebug "updated()..."
		
		initialize()
	
		runIn(5, updateSyncStatus)
		
		def cmds = getConfigureCmds()	
		return cmds ? response(delayBetween(cmds, 250)) : []		
	}
}


def configure() {	
	logDebug "configure()..."
	
	initialize()
	
	runIn(5, updateSyncStatus)
	
	state.syncAll = true	
	def cmds = getConfigureCmds()	
	return cmds ? delayBetween(cmds, 250) : []
}

private initialize() {
	if (!device?.currentValue("btn1Name")) {
		buttons.each {
			resetButton(it)			
		}		
	}

	if (!device?.currentValue("switch")) {
		resetTamper()		
		sendEvent(getEventMap("alarm", "off"))
		sendEvent(getEventMap("switch", "off"))
		sendEvent(getEventMap("level", 0))
	}	
	
	if (!device?.currentValue("chime")) {
		sendEvent(getEventMap("chime", "off"))
	}
	
	if (!device?.currentValue("checkInterval")) {
		def checkInterval = (6 * 60 * 60) + (5 * 60)

		sendEvent(name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

		startHealthPollSchedule()	
	}
}


def ping() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		logDebug "ping()"
	
		// Restart the polling schedule in case that's the reason why it's gone too long without checking in.
		startHealthPollSchedule()

		healthPoll()
	}	
}

private startHealthPollSchedule() {
	unschedule(healthPoll)
	runEvery3Hours(healthPoll)
}

def healthPoll() {
	logDebug "healthPoll()"	
	def cmds = []
	
	buttons.each { btn ->
		if (canRequestButtonBattery(btn)) {
			cmds << configGetCmd(getButtonInfoParam(btn))
		}
	}
	
	if (!cmds) {
		cmds << versionGetCmd()
	}
	sendCmds(delayBetween(cmds, 200))
}

private canRequestButtonBattery(btn) {
	// Only request battery level of buttons that are paired and haven't reported it within the last 24 hours.
	return (buttonIsPaired(btn) && !isDuplicateCommand(state["btn${btn.num}LastBattery"], (23 * 60 * 60 * 1000)))
}

private getConfigureCmds() {
	def cmds = []
	
	if (state.syncAll || tamperVolumeSetting != state.tamperVolume) {
		cmds << soundSwitchConfigSetCmd(tamperVolumeSetting, TONE.tamper, TAMPER_ASSOC.endpoint)
		cmds << soundSwitchConfigGetCmd(TAMPER_ASSOC.endpoint)		
	}
	
	if (state.syncAll) {
		cmds += getRefreshBtnsCmds()
	}

	if (state.syncAll || !device?.currentValue("firmwareVersion")) {
		cmds << versionGetCmd()
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
	logDebug "This device does not support the ${cmdName} command."
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
	return cmds ? delayBetween(cmds, 300) : []
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


def chime() {
	return playSound(chimeToneSetting)
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
	], 250)
}


def off() {
	logDebug "off()..."	
	def cmds = [
		basicSetCmd(0, PLAY_ASSOC.endpoint)
	]
	buttons.each {
		if (device.currentValue("btn${it.num}Switch") != "empty") {
			cmds << basicSetCmd(0, it.endpoint)
		}
	}	
	return delayBetween(cmds, 200)
}


def executeRefresh() {
	sendCmds(refresh())
}

def refresh() {
	logDebug "refresh()..."
	
	updateSyncStatus()
	
	resetTamper()
		
	def cmds = [
		basicGetCmd(PLAY_ASSOC.endpoint)
	]
	
	cmds += getRefreshBtnsCmds()
	
	return delayBetween(cmds, 500)	
}

private getRefreshBtnsCmds() {
	def cmds = []
	buttons.each { btn ->
		cmds << configGetCmd(getButtonInfoParam(btn))
	}	
	return cmds
}

private buttonIsPaired(btn) {
	return (device.currentValue("btn${btn.num}Switch") != "empty")
}


def pairRemoveButton1() { pairRemoveButton(BUTTON1) }
def pairRemoveButton2() { pairRemoveButton(BUTTON2) }
def pairRemoveButton3() { pairRemoveButton(BUTTON3) }

private pairRemoveButton(btn) {
	logTrace "pairRemoveButton(${btn})"
	def cmds = []
	switch (device.currentValue("btn${btn.num}Action")) {
		case "remove":
			runIn(3, resetButtonAction, [data:[btnNum: btn.num]])
			log.warn "Tap 'Confirm Remove' to remove the button and child device."
			sendButtonActionEvent(btn.num, "confirm")
			break
		case "confirm":
			cmds += removeButton(btn)
			break
		default:
			cmds += pairButton(btn)
	}	
	return cmds
}

private removeButton(btn) {
	logDebug "removeButton(${btn.num})"
	def cmds = []
	def child = findChild(btn)
	if (child) {
		log.warn "Removing child device '${child.displayName}' because Button #${btn.num} was removed"
		deleteChildDevice(child.deviceNetworkId)
		
		resetButton(btn)
		
		state.buttonMode = BUTTON_MODE.unpairing
		
		cmds << configSetCmd(triggerUnpairingModeParam, btn.pairingMode)
	}
	return cmds
}

private resetButton(btn) {
	sendEvent(getEventMap("btn${btn.num}Name", "(NOT PAIRED)"))
	sendEvent(getEventMap("btn${btn.num}Switch", "empty"))
	sendEvent(getEventMap("btn${btn.num}Action", "pair"))
}

private pairButton(btn) {
	log.warn "Pairing initiated for Button #${btn.num} so triple-click that button to complete the process."
	
	sendButtonActionEvent(btn.num, "pairing")
	runIn(10, resetButtonAction, [data:[btnNum: btn.num]])
	
	state.buttonMode = BUTTON_MODE.pairing
	return [ configSetCmd(triggerPairingModeParam, btn.pairingMode) ]	
}


def resetButtonAction(data) {
	def action = device.currentValue("btn${data?.btnNum}Action")
	if (action == "confirm") {
		sendButtonActionEvent(data?.btnNum, "remove")
	}
	else if (action == "pairing") {
		sendButtonActionEvent(data?.btnNum, "pair")
	}
	if (data?.btn) {
		sendCmds([ configGetCmd(getButtonInfoParam(data?.btn)) ])
	}
}

private sendButtonActionEvent(btnNum, value) {
	sendEventIfNew("btn${btnNum}Action", value)
}


def childUpdated(buttonNumber, groupSettings) {
	def btn = getButton(buttonNumber)
	if (btn && groupSettings) {
		
		sendEventIfNew("btn${btn.num}Name", groupSettings.childName)
		
		def configVal = getGroupConfigVal(groupSettings.lightEffect, groupSettings.toneIntercept, groupSettings.repeatDelay, groupSettings.repeat)
		
		def param = getButtonGroupParam(btn)
		logDebug "CHANGING ${param.name}(#${param.num}) to ${configVal}"
		logDebug "CHANGING Button ${btn.num} Tone/Volume to ${groupSettings.tone}/${groupSettings.volume}"
		
		sendCmds(delayBetween([ 
			configSetCmd(param, configVal),
			configGetCmd(param),
			soundSwitchConfigSetCmd(groupSettings.volume, groupSettings.tone, btn.endpoint),
			soundSwitchConfigGetCmd(btn.endpoint)
		], 500))
	}
}


def childRefresh(buttonNumber) {
	def btn = getButton(buttonNumber)
	if (btn) {
		sendCmds(delayBetween([ 
			basicGetCmd(btn.endpoint),
			configGetCmd(getButtonInfoParam(btn)) 
		], 200))
	}	
}


def toggleButton1() { toggleButton(BUTTON1) }
def toggleButton2() { toggleButton(BUTTON2) }
def toggleButton3() { toggleButton(BUTTON3) }

private toggleButton(btn) {
	logDebug "toggleButton(${btn?.num})"
	
	if (device.currentValue("btn${btn.num}Switch") == "off") {
		childOn(btn.num)
	}
	else {
		childOff(btn.num)
	}
	return []
}


def childOn(buttonNumber) {
	def btn = getButton(buttonNumber)
	if (btn) {
		sendCmds([ basicSetCmd(0xFF, btn.endpoint) ])
	}
}


def childOff(buttonNumber) {
	def btn = getButton(buttonNumber)
	if (btn) {
		sendCmds([ basicSetCmd(0x00, btn.endpoint) ])
	}
}


private sendCmds(cmds) {
	def actions = []
	cmds?.each {
		actions << new physicalgraph.device.HubAction(it)
	}
	if (actions) {
		sendHubCommand(actions)
	}
	return []
}


private versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
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
	def cmd = "08"
	if (tone != null) {
		cmd = "${cmd}${convertToHex(tone)}"
	}
	return soundSwitchCmd(cmd, endpoint)
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
		return secureCmd(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:safeToInt(endpoint)).encapsulate(cmd))
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
	try {
		return zwaveInfo?.zw?.contains("s") || ("0x98" in device.rawDescription?.split(" "))
	}
	catch (e) {
		return false
	}
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
		return soundSwitchEvent(cmd, cmd.sourceEndPoint)
	}
	else {
		logDebug "Unable to get encapsulated command: $cmd"
		return []
	}
}


def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, endpoint=null) {
	logTrace "NotificationReport: $cmd - endpoint: ${endpoint}"
	
	def btn 
	if (endpoint) {
		btn = buttons.find { it.endpoint == endpoint }
	}
	
	switch(cmd.notificationType) {
		case 7:
			handleTamperEvent(cmd.event == 9 ? "detected" : "clear")
			break
		case 8:
			// low/normal battery change.
			if (btn) {
				sendCmds([configGetCmd(getButtonInfoParam(btn))])
			}
			else {
				sendCmds(delayBetween(getRefreshBtnsCmds(), 200))
			}
			break
		case 14:
			if (btn) {
				handleButtonEvent(btn, cmd.event)
			}
			// else if (endpoint == 6) {
				// handleAlarmChimeEvent(cmd.event)	
			// }
			break
		default:
			logDebug "Unknown Notification Type: ${cmd}"		
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


def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "VersionReport: ${cmd}"
	
	def version = "${cmd.applicationVersion}.${cmd.applicationSubVersion}"	
	logDebug "Firmware: ${version}"
	sendEventIfNew("firmwareVersion", version)
	return []	
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {	
	// logTrace "ConfigurationReport: ${cmd}"
	
	updateSyncStatus("Syncing...")
	runIn(5, updateSyncStatus)
	
	def param = allConfigParams.find { it.num == cmd.parameterNumber }
	if (param) {	
		def val = cmd.size == 1 ? cmd.configurationValue[0] : cmd.scaledConfigurationValue		
		switch (param.num) {
			case activePairingButtonParam.num:
				handleActivePairingButtonReport(val)
				break
			case pairingResultsParam.num:
				handleButtonPairingResultsReport(val)
				break
			case { it in buttons.collect { it.groupParamNum } }:
				logDebug "${param.name}(#${param.num}) = ${val} ${cmd.size == 4 ? cmd.configurationValue : ''}"
				setParamStoredValue(param.num, "${cmd.configurationValue}")	
				break
			case { it in buttons.collect { it.infoParamNum } }:
				handleButtonInfoReport(param.num, cmd.configurationValue)
				break
			default:
				logDebug "${param.name}(#${param.num}) = ${val} ${cmd.size == 4 ? cmd.configurationValue : ''}"
				setParamStoredValue(param.num, val)
		}
	}
	else {
		logTrace "Unknown Parameter #${cmd.parameterNumber} = ${val}"
	}		
	return []
}

private handleActivePairingButtonReport(val) {
	def btn = buttons.find { it.pairingMode == val }
	if (btn) {
		logDebug "Pairing Mode Started for Button #${btn?.num}"
	}
	else {
		logDebug "Pairing Mode Stopped"
	}
}

private handleButtonPairingResultsReport(val) {
	if (state.buttonMode == BUTTON_MODE.pairing) {
		def btn = buttons.find { it.pairingMode == val }
		if (val == 0) {
			logDebug "No buttons are paired"
		}
		else if (btn) {
			logDebug "Button #${btn.num} is paired"
			sendCmds([ configGetCmd(getButtonInfoParam(btn)) ])
		}
		else {
			logDebug "Multiple buttons are paired"
			sendCmds(delayBetween(getRefreshBtnsCmds(), 500))
		}
	}
	else {
		logDebug "A button was removed"
	}
	state.buttonMode = BUTTON_MODE.normal
}

private handleButtonInfoReport(paramNum, btnInfo) {
	def btn = buttons.find { it.infoParamNum == paramNum }
	if (btn && btnInfo) {
		
		logTrace "Button ${btn.num}: handleButtonInfoReport(${paramNum}, ${btnInfo})"
		
		def child = findChild(btn)
		if (btnInfo[2] != 0) {
			if (!child) {
				logDebug "Creating child device for Button ${btn.num}"
				child = addChildButton(btn)
				if (child) {
					sendEvent(getEventMap("btn${btn.num}Name", child.displayName))
				}
			}
				
			if (child) {
				state["btn${btn.num}LastBattery"] = new Date().time
				child.sendEvent(child.getEventMap("battery", parseButtonInfoBattery(btnInfo), true, "%"))
				
				sendButtonActionEvent(btn.num, "remove")
				sendEventIfNew("btn${btn.num}Name", child.displayName)
				sendEventIfNew("btn${btn.num}Switch", child.currentValue("switch"))
				
				def firmwareVersion = parseButtonInfoFirmwareVersion(btnInfo)
				if (child.currentValue("firmwareVersion") != firmwareVersion) {
					child.sendEvent(child.getEventMap("firmwareVersion", firmwareVersion))
				}
			}
		}
	}
}

private addChildButton(btn) {
	addChildDevice(
		"krlaframboise", 
		"Aeotec Doorbell 6 Button", 
		"${device.deviceNetworkId}-${btn.num}", 
		device.getHub().getId(), 
		[
			completedSetup: true,
			isComponent: false,
			label: "${device.displayName} - Button${btn.num}",
			data: [
				buttonNumber: "${btn.num}"
			]
		]
	)
}

private parseButtonInfoBattery(btnInfo) {
	def rawBattery = btnInfo[1] + (btnInfo[0] * 0x100)	
	def batteryVolts = (rawBattery / 1000)
	def battery = 0
	
	if (batteryVolts > 0) {
		if (batteryVolts > 2.8) {
			battery = Math.round((1 - ((3 - batteryVolts) / 0.2)) * 100)
		}
		else {
			battery = 1
		}
	}
	if (battery > 100) battery = 100
	if (battery < 0) battery = 1
	
	return battery
}

private parseButtonInfoFirmwareVersion(btnInfo) {
	return "${safeToInt(btnInfo[2])}.${safeToInt(btnInfo[3])}"
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
	
	return (pendingConfigParams + pendingTamper)
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


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=null) {
	logTrace "BasicReport: ${cmd}" + (endpoint ? " (endpoint:${endpoint})" : "")
	handleBasicEvent(cmd.value, endpoint)
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, endpoint=null) {
	logTrace "BasicSet: ${cmd}" + (endpoint ? " (endpoint:${endpoint})" : "")	
	handleBasicEvent(cmd.value, endpoint)	
	return []
}

private handleBasicEvent(rawVal, endpoint) {
	def btn 
	if (endpoint) {
		btn = buttons.find { it.endpoint == endpoint }
	}
	
	if (btn) {
		handleButtonEvent(btn, rawVal)
	}
	else {
		handleAlarmChimeEvent(rawVal)
		
		if (!rawVal) {
			// The basic set off commands for the endpoints sometimes get lost so this ensures that the buttons will always get set back to off.
			buttons.each {
				if (findChild(it)?.currentValue("switch") == "on") {
					handleButtonEvent(it, rawVal)
				}
			}
		}
	}
}

private handleAlarmChimeEvent(rawVal) {
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
	sendEventIfNew("primaryStatus", statusVal)
	
	if (lastAction == "chime" || (statusVal == "off")) {
		sendEventIfNew("chime", statusVal, true)
	}
}

private handleButtonEvent(btn, rawVal) {
	def child = findChild(btn)
	
	if (child) {
		if (rawVal) {
			def evt = child.getEventMap("button", "pushed")
			evt.data = [buttonNumber: btn.num]
			child.sendEvent(evt)	
			
			child.sendEvent(child.getEventMap("lastPushed", convertToLocalTimeString(new Date())))
		}
		
		def switchVal = rawVal ? "on" : "off"
		if (child.currentValue("switch") != switchVal) {
			child.sendEvent(child.getEventMap("switch", switchVal, true))
			sendEvent(getEventMap("btn${btn.num}Switch", switchVal))
		}
	}
}


def soundSwitchEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, endpoint=null) {
	logTrace "soundSwitchEvent: ${cmd} - endpoint: ${endpoint}"

	def btn 
	if (endpoint) {
		btn = buttons.find { it.endpoint == endpoint }
	}
			
	switch (cmd.command) {
		case 7:
			updateSyncStatus("Syncing...")
			runIn(5, updateSyncStatus)			
			handleSoundSwitchConfigurationReport(cmd)
			break		
		case 10:			
			// Sound Switch Tone Play Report
			def rawVal = (cmd.parameter ? cmd.parameter[0] : 0)			
			if (btn && !rawVal && (findChild(btn)?.currentValue("switch") == "on")) {
				// The notification report for off is often lost for short sounds so this should ensure that the button's switch state goes back to off.
				handleButtonEvent(btn, rawVal)
			}
			else if (endpoint == 6) {
				handleAlarmChimeEvent(rawVal)
			}
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
			logDebug "Tamper Tone/Volume = ${tone}/${volume}"
			state.tamperVolume = volume			
		}
		else {
			def btn = buttons.find { it.endpoint == cmd.sourceEndPoint }
			if (btn) {
				logDebug "Button ${btn.num} Tone/Volume = ${tone}/${volume}"
			}
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

	buttons.each { btn ->
		params << getButtonGroupParam(btn)
		params << getButtonInfoParam(btn)
	}
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

private getButtonGroupParam(btn) {
	return getParam(btn.groupParamNum, "Button ${btn.num} Group", 4)
	//DEFAULTS: btn1:0x31070914, btn2:0x39070914, btn3:0x41070914
}

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

private getTriggerUnpairingModeParam() {
	return getParam(48, "Trigger Unpairing Mode", 1)  // write only
}

private getTriggerPairingModeParam() {
	return getParam(49, "Trigger Pairing Mode", 1)  // write only
}

private getActivePairingButtonParam() {
	return getParam(50, "Active Pairing Button", 1)  // read only (automatically sent)
}

private getPairingResultsParam() {
	return getParam(51, "Pairing Results", 1)  // read only (automatically sent)
}

private getButtonInfoParam(btn) {
	return getParam(btn.infoParamNum, "Button ${btn.num} Information", 4)
}

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


private findChild(btn) {
	return childDevices?.find { it.getDataValue("buttonNumber") == "${btn.num}" }
}

private getButton(buttonNumber) {
	return buttons.find { buttonNumber == it.num }	
}

private getButtons() {
	return [BUTTON1,BUTTON2,BUTTON3]
}


private sendEventIfNew(attr, newValue, displayed=false, unit=null) {
	if (device?.currentValue("${attr}") != newValue) {
		sendEvent(getEventMap("${attr}", newValue, displayed, unit))
	}
}

private getEventMap(name, value, displayed=false, unit=null) {	
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true,
		descriptionText: "${device?.displayName} - ${value}"
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