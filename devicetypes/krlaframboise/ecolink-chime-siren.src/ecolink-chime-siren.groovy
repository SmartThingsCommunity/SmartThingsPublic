/*
 *  Ecolink Chime+Siren v0.2
 *
 *  Changelog:
 *
 *    0.2 (05/04/2021)
 *      - New Firmware
 *
 *    0.1 (03/13/2021)
 *      - Beta Release
 *
 *
 *  Copyright 2021 Ecolink
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
*/

import groovy.transform.Field

@Field static Map commandClassVersions = [
	0x20: 1,	// Basic
	0x55: 1,	// Transport Service
	0x59: 1,	// AssociationGrpInfo
	0x5A: 1,	// DeviceResetLocally
	0x5E: 2,	// ZwaveplusInfo
	0x6C: 1,	// Supervision
	0x70: 1,	// Configuration
	0x71: 3,	// Notification v4
	0x72: 2,	// ManufacturerSpecific
	0x73: 1,	// Powerlevel
	0x79: 1,	// Sound Switch
	0x7A: 2,	// FirmwareUpdateMd
	0x80: 1,	// Battery
	0x85: 2,	// Association
	0x86: 1,	// Version (2)
	0x87: 3,	// Indicator
	0x8E: 2,	// Multi Channel Association
	0x9F: 1		// Security S2
]


@Field static List<Map> sounds = [
	[number:1, name:"1. One long beep"],
	[number:2, name:"2. Two beeps"],
	[number:3, name:"3. E1 Beep"],
	[number:4, name:"4. Tinker"],
	[number:5, name:"5. Droplet"],
	[number:6, name:"6. Rain"],
	[number:7, name:"7. Marimba"],
	[number:8, name:"8. Water dew"],
	[number:9, name:"9. Phone"],
	[number:10, name:"10. Pong"],
	[number:11, name:"11. Error Sound"],
	[number:12, name:"12. Chirp"],
	[number:13, name:"13. Alarm Siren", type:"siren"],
	[number:14, name:"14. Exit Delay", type:"siren"],
	[number:15, name:"15. Entry Delay", type:"siren"],
	[number:16, name:"16. Smoke Alarm", type:"siren"],
	[number:17, name:"17. CO Alarm", type:"siren"],
	[number:18, name:"18. Armed Away"],
	[number:19, name:"19. Armed Stay"],
	[number:20, name:"20. Disarmed"],
	[number:21, name:"21. Front Door"],
	[number:22, name:"22. Side Door"],
	[number:23, name:"23. Back Door"],
	[number:24, name:"24. Garage Door"],
	[number:25, name:"25. Alarm Siren 2", type:"siren"],
	[number:26, name:"26. Alarm Siren 3", type:"siren"],
	[number:27, name:"27. Traditional Marimba"],
	[number:28, name:"28. Westminster Piano"],
	[number:29, name:"29. Forest"],
	[number:30, name:"30. Garden Strings"],
	[number:126, name:"Entry/Exit Delay (15 Seconds)", indicatorID:0x16],
	[number:127, name:"Entry/Exit Delay (30 Seconds)", indicatorID:0x26],
	[number:128, name:"Entry/Exit Delay (45 Seconds)", indicatorID:0x36],
	[number:129, name:"Entry/Exit Delay (255 Seconds)", indicatorID:0xF6]
]


metadata {
	definition (
		name: "Ecolink Chime+Siren",
		namespace: "krlaframboise",
		author: "Kevin LaFramboise (@krlaframboise)",
		ocfDeviceType: "x.com.st.d.siren",
		mnmn: "SmartThingsCommunity",
		vid: "02a8f57f-6c7b-37f9-86c8-4705bf4faa6f"
	) {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "platemusic11009.soundVolume"
		capability "platemusic11009.ecoPlaySoundNumber"
		capability "platemusic11009.ecoSirenSound"
		capability "platemusic11009.sirenVolume"
		capability "Alarm"
		capability "platemusic11009.ecoChimeSound"
		capability "platemusic11009.chimeVolume"
		capability "Chime"
		capability "Power Source"
		capability "Battery"
		capability "Refresh"
		capability "Configuration"
		capability "Health Check"
		capability "platemusic11009.firmware"

		attribute "lastCheckIn", "string"

		fingerprint mfr:"014A", prod:"0007", model: "3975", deviceJoinName:"Ecolink Chime+Siren"
	}

	simulator { }

	preferences {
		[heartBeatParam, supervisionParam].each { param ->
			if (param.options) {
				input "configParam${param.num}", "enum",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					options: param.options
			}
			else if (param.range) {
				input "configParam${param.num}", "number",
					title: "${param.name}:",
					required: false,
					displayDuringSetup: false,
					defaultValue: param.defaultVal,
					range: param.range
			}
		}

		input "debugOutput", "enum",
			title: "Enable Debug Logging?",
			required: false,
			displayDuringSetup: false,
			defaultValue: 1,
			options: [0:"No", 1:"Yes [DEFAULT]"]
	}
}


def installed() {
	logDebug "installed()..."

	initialize()

	return []
}


def updated() {
	if (!isDuplicateCommand(state.lastUpdated, 2000)) {
		state.lastUpdated = new Date().time

		logDebug "updated()..."

		initialize()

		runIn(2, executeConfigureCmds)
	}
	return []
}

void initialize() {
	if (!device.currentValue("checkInterval")) {
		def checkInterval = ((60 * 60) + (5 * 60))
		sendEvent([name: "checkInterval", value: checkInterval, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"]])
	}

	sendInitEvent("activeSoundNumber", 0)
	sendInitEvent("soundVolume", 25, "%")	
	sendInitEvent("chimeSound", "1")
	sendInitEvent("chimeVolume", 50, "%")
	sendInitEvent("sirenSound", "13")
	sendInitEvent("sirenVolume", 100, "%")

	state.debugLoggingEnabled = (safeToInt(settings?.debugOutput, 1) != 0)
}

void sendInitEvent(String name, value, String unit="") {
	if (device.currentValue(name) == null) {
		sendEventIfNew(name, value, unit)
	}
}


def configure() {
	logDebug "configure()..."

	executeConfigureCmds()

	runIn(15, refresh)
}

void executeConfigureCmds() {
	List<String> cmds = []
	
	if (!device.currentValue("battery")) {
		cmds << batteryGetCmd()
	}
		
	if (!device.currentValue("switch")) {
		cmds << soundSwitchTonePlayGetCmd()
	}

	configParams.each { param ->
		Integer storedVal = getParamStoredValue(param.num)
		if ((storedVal != param.value) && (param.value != null)) {
			logDebug "Changing ${param.name}(#${param.num}) from ${storedVal} to ${param.value}"
			cmds << configSetCmd(param, param.value)
			cmds << configGetCmd(param)
		}
	}

	sendCommands(cmds)
}


def ping() {
	logDebug "ping()..."
	return [ batteryGetCmd() ]
}


def setChimeVolume(chimeVolume) {
	logTrace "setChimeVolume(${chimeVolume})..."
	sendEventIfNew("chimeVolume", chimeVolume, "%")
}


def setChimeSound(chimeSound) {
	logTrace "setChimeSound(${chimeSound})..."
	sendEventIfNew("chimeSound", chimeSound)
}


def chime() {
	logDebug "chime()..."

	int volume = safeToInt(device.currentValue("chimeVolume"), 50)
	int sound = safeToInt(device.currentValue("chimeSound"), 1)

	state.pendingAction = "chime"
	playSoundAtVolume(sound, volume)
}


def setSoundVolume(soundVolume) {
	logTrace "setSoundVolume(${soundVolume})..."
	sendEventIfNew("soundVolume", soundVolume, "%")
}


def playSound(soundNumber) {
	logDebug "playSound(${soundNumber})..."

	int volume = safeToInt(device.currentValue("soundVolume"), 25)
	int sound = safeToInt(soundNumber, 1)

	state.pendingAction = soundNumber
	playSoundAtVolume(sound, volume)
}


def setSirenVolume(sirenVolume) {
	logTrace "setSirenVolume(${sirenVolume})..."
	sendEventIfNew("sirenVolume", sirenVolume, "%")
}


def setSirenSound(sirenSound) {
	logTrace "setSirenSound(${sirenSound})..."
	sendEventIfNew("sirenSound", sirenSound)
}


def both() {
	siren()
}

def strobe() {
	siren()
}

def siren() {
	logDebug "siren()..."

	int volume = safeToInt(device.currentValue("sirenVolume"), 100)
	int sound = safeToInt(device.currentValue("sirenSound"), 13)

	state.pendingAction = "siren"
	playSoundAtVolume(sound, volume)
}


void playSoundAtVolume(soundNumber, volume) {
	logTrace "playSoundAtVolume(${soundNumber}, ${volume})..."

	Map sound = getSound(soundNumber)
	state.lastSound = sound
	
	logDebug "Playing '${sound.name}' at ${volume}%..."

	List<String> cmds = [
		soundSwitchConfigSetCmd(volume, 1)
	]

	if (sound.indicatorID) {
		cmds << indicatorSetCmd(sound.indicatorID)
	}
	else {
		cmds << soundSwitchTonePlaySetCmd(soundNumber)
	}

	cmds << soundSwitchTonePlayGetCmd()

	sendCommands(cmds, 100)
}


def on() {
	logTrace "on()..."

	chime()
}


def off() {
	logDebug "off()..."
	return delayBetween([
		soundSwitchTonePlaySetCmd(0),
		soundSwitchTonePlayGetCmd()
	], 100)
}


def refresh() {
	logDebug "refresh()..."

	sendCommands([
		batteryGetCmd(),
		versionGetCmd(),
		soundSwitchTonePlayGetCmd()
	])
}


void sendCommands(List<String> cmds, Integer delay=1000) {
	if (cmds) {
		def actions = []
		cmds.each {
			actions << new physicalgraph.device.HubAction(it)
		}
		sendHubCommand(actions, delay)
	}
}


String versionGetCmd() {
	return secureCmd(zwave.versionV1.versionGet())
}

String indicatorSetCmd(int value) {
	return secureCmd(zwave.indicatorV1.indicatorSet(value: value))
}

String batteryGetCmd() {
	return secureCmd(zwave.batteryV1.batteryGet())
}

String configSetCmd(Map param, int value) {
	return secureCmd(zwave.configurationV1.configurationSet(parameterNumber: param.num, size: param.size, scaledConfigurationValue: value))
}

String configGetCmd(Map param) {
	return secureCmd(zwave.configurationV1.configurationGet(parameterNumber: param.num))
}

String secureCmd(cmd) {
	try {
		if (isSecurityEnabled()) {
			return zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		}
		else {
			return cmd.format()
		}
	}
	catch (ex) {
		return cmd.format()
	}
}

String soundSwitchConfigSetCmd(int volume, int tone) {
	return soundSwitchCmd("05${intToHex(volume)}${intToHex(tone)}")
}

String soundSwitchConfigGetCmd() {
	return soundSwitchCmd("06")
}

String soundSwitchTonePlaySetCmd(int tone) {
	return soundSwitchCmd("08${intToHex(tone)}")
}

String soundSwitchTonePlayGetCmd() {
	return soundSwitchCmd("09")
}

String soundSwitchCmd(cmd) {
	cmd = "79${cmd}"
	if (isSecurityEnabled()) {
		return "988100${cmd}"
	}
	else {
		return cmd
	}
}

boolean isSecurityEnabled() {
	return (zwaveInfo?.zw?.contains("s") || ("0x98" in device?.rawDescription?.split(" ")))
}


def parse(String description) {
	if ("${description}".contains("command: 9881, payload: 00 79") || "${description}".contains("command: 79")) {
		// SOUND SWITCH NOT SUPPORTED BY SMARTTHINGS
		handleSoundSwitchEvent(description)
	}
	else if ("${description}".contains("command: 9881, payload: 00 80") || "${description}".contains("command: 80")) {
		// BATTERY V2 NOT SUPPORTED BY SMARTTHINGS
		handleBatteryReport(description)
	}
	else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			zwaveEvent(cmd)
		}
		else {
			log.warn "Unable to parse: $description"
		}
	}

	updateLastCheckIn()
	return []
}

void updateLastCheckIn() {
	if (!isDuplicateCommand(state.lastCheckInTime, 60000)) {
		state.lastCheckInTime = new Date().time
		sendEvent(name: "lastCheckIn", value: new Date().time, displayed: false)
	}
}


void zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCmd = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCmd) {
		zwaveEvent(encapsulatedCmd)
	}
	else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	logTrace "${cmd}"

	Map param = configParams.find { it.num == cmd.parameterNumber }
	if (param) {
		Integer val = cmd.scaledConfigurationValue
		logDebug "${param.name}(#${param.num}) = ${val}"

		setParamStoredValue(param.num, val)
	}
	else {
		logDebug "Parameter #${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
	}
}


void zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	logTrace "$cmd"
	sendEventIfNew("firmwareVersion", (cmd.applicationVersion + (cmd.applicationSubVersion / 100)))
}


void zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logTrace "${cmd}"
	sendEventIfNew("switch", (cmd.value ? "on" : "off"))
}


void zwaveEvent(physicalgraph.zwave.Command cmd) {
	logDebug "Unhandled zwaveEvent: $cmd"
}


void zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logTrace "${cmd}"

	if (cmd.notificationType == 0x08) {
		String powerSource = null
		switch (cmd.event) {
			case 0x02:
				logDebug "AC Mains Disconnected"
				powerSource = "battery"
				break
			case 0x03:
				logDebug "AC Mains Re-Connected"
				powerSource = "mains"
				break
			case 0x0C:
				// logDebug "Battery is charging"
				// powerSource = "mains"
				break
			case 0x0D:
				// logDebug "battery is fully charged"
				// powerSource = "mains"
				break
			case 0x0E:
				// logDebug "charge battery soon"
				// powerSource = "battery"
				break
			case 0x0F:
				// logDebug "charge battery now"
				// powerSource = "battery"
				break
			default:
				logDebug "Unknown Power Event: ${cmd}"
		}
		if (powerSource) {
			sendEventIfNew("powerSource", powerSource)
		}
	}
	else if (cmd.notificationType == 0x09) {
		if (cmd.event == 0x04) {
			// 0x5501 Watchdog Reset on the Z-Wave Module
			// 0x5502 Watchdog Reset on the STM32
		}
	}
	else {
		logDebug "Unknown notificationType: ${cmd}"
	}
}


void handleBatteryReport(String description) {
	// logTrace "handleBatteryReport(${description})..."

	// BATTERY V2 NOT SUPPORTED BY SMARTTHINGS

	// The handler can't rely on the Power Management Notification Reports to determine the power source because of a firmware issue that occurs when the device is plugged back in after the battery gets low.

	Map cmd = parseCommand(description)
	if (cmd?.payloadBytes?.size() >= 2) {

		int value = hexToInt(cmd.payloadBytes[0])

		sendEvent(getEventMap("battery", (value == 0xFF ? 1 : value), "%"))

		try {
			String powerSource = null

			int chargingStatus = Integer.parseInt(Integer.toBinaryString(hexToInt(cmd.payloadBytes[1])).padLeft(8, "0").substring(0, 2), 2)

			switch (chargingStatus) {
				case 0:
					// discharging
					powerSource = "battery"
					break

				case 1:
					// charging
					powerSource = "mains"
					break

				case 2:
					// maintaining
					powerSource = "mains"
					break
			}

			if (powerSource) {
				sendEventIfNew("powerSource", powerSource)
			}
		}
		catch (ex) {
			log.warn "Unable to parse battery charging status from ${description}"
		}
	}
}


void handleSoundSwitchEvent(String description) {
	// logTrace "handleSoundSwitchEvent(${description})..."

	// SOUND SWITCH CC NOT SUPPORTED BY SMARTTHINGS

	Map cmd = parseCommand(description)

	switch (cmd?.command) {
		case "7907":
			handleSoundSwitchConfigurationReport(cmd.payloadBytes)
			break
		case "790A":
			handleSoundSwitchTonePlayReport(cmd.payloadBytes)
			break
		default:
			logDebug "Unknown Sound Switch Command: ${description}"
	}
}


void handleSoundSwitchConfigurationReport(List<String> payloadBytes) {
	// logTrace "handleSoundSwitchConfigurationReport(${payloadBytes})..."

	if (payloadBytes?.size() == 2) {
		int volume = hexToInt(payloadBytes[0])
		int tone = hexToInt(payloadBytes[1])

		logDebug "Tone: ${tone} - Volume: ${volume}"
	}
	else {
		log.warn "Sound Switch Configuration Report: Unexpected Payload '${payloadBytes}'"
	}
}


void handleSoundSwitchTonePlayReport(List<String> payloadBytes) {
	// logTrace "handleSoundSwitchTonePlayReport(${payloadBytes})"

	if (payloadBytes?.size() == 1) {
		int soundNumber = hexToInt(payloadBytes[0])
		if (soundNumber) {

			Map sound = state.lastSound
			if ((sound?.number != soundNumber) && !sound?.indicatorID) {
				sound = getSound(soundNumber)
				state.lastSound = sound
			}
			else {
				logDebug "Active Sound Number: ${soundNumber}"
			}

			sendEventIfNew("switch", "on")

			switch (state.pendingAction) {
				case "siren":
					sendEventIfNew("alarm", "siren")
					break

				case "chime":
					sendEventIfNew("chime", "chime")
					break

				default:
					sendEvent(getEventMap("activeSoundNumber", soundNumber))
			}

			state.pendingAction = null
		}
		else {
			if ("${state.pendingAction}".isNumber()) {
				// Workaround for timeout error the mobile app throws when a user attempts to play an unsupported sound #.  This workaround wouldn't be necessary if the device followed the z-wave specs and played the default sound.
				log.warn "Sound #${state.pendingAction} Doesn't Exist"
				sendEvent(getEventMap("activeSoundNumber", safeToInt(state.pendingAction)))
				state.pendingAction = null
			}
			
			sendEventIfNew("switch", "off")
			sendEventIfNew("alarm", "off")
			sendEventIfNew("chime", "off")
			sendEventIfNew("activeSoundNumber", 0)
		}
	}
	else {
		log.warn "Sound Switch Tone Play Report: Unexpected Payload '${payloadBytes}'"
	}
}


Map parseCommand(String description) {
	Map cmd = description.split(", ").collectEntries { entry ->
		def pair = entry.split(": ")
		[(pair.first()): pair.last()]
	}

	List<String> payloadBytes = null
	if (cmd?.payload) {
		payloadBytes = cmd.payload.split(" ")
	}

	cmd.payloadBytes = payloadBytes
	return cmd
}


Map getSound(int soundNumber) {
	Map sound = sounds.find { it.number == soundNumber }
	if (!sound) {
		sound = [number: soundNumber, name:"${soundNumber}. Custom"]
	}
	return sound
}


Integer getParamStoredValue(Integer paramNum) {
	return safeToInt(state["configVal${paramNum}"] , null)
}

void setParamStoredValue(Integer paramNum, Integer value) {
	state["configVal${paramNum}"] = value
}


List<Map> getConfigParams() {
	return [
		// defaultSoundParam,
		heartBeatParam,
		supervisionParam,
		// soundVolumeParam,
		// soundsAvailableParam,
		emergencySoundVolumeParam
	]
}

Map getDefaultSoundParam() {
	return getParam(1, "Default Sound", 1, 5, null, "1..100")
}

Map getHeartBeatParam() {
	return getParam(2, "Heartbeat Notification Timing (seconds)", 4, 3600, null, "120..86400") // seconds
}

Map getSupervisionParam() {
	return getParam(3, "Supervision Encapsulation", 1, 1, [0:"Disabled", 1:"Enabled [DEFAULT]"])
}

Map getSoundVolumeParam() {
	return getParam(4, "Sound Volume", 1, 50, null, "0..100") //255 = restore last before mute
}

Map getSoundsAvailableParam() {
	return getParam(5, "Sounds Available", 1, null, null, "0..125")
}

Map getEmergencySoundVolumeParam() {
	return getParam(6, "Emergency Sound Volume Adjustable", 1, 1,  [0:"Disabled", 1:"Enabled [DEFAULT]"])
}

Map getParam(Integer num, String name, Integer size, Integer defaultVal, Map options, range=null) {
	Integer val = safeToInt((settings ? settings["configParam${num}"] : null), defaultVal)

	return [num: num, name: name, size: size, value: val, options: options, range: range, defaultVal: defaultVal]
}


void sendEventIfNew(String name, value, String unit="") {
	if (device.currentValue(name) != value) {
		sendEvent(getEventMap(name, value, unit))
	}
	else {
		logTrace("${name} is ${value}${unit}")
	}
}

Map getEventMap(String name, value, String unit="") {
	Map event = [
		name: name,
		value: value,
		displayed: true,
		isStateChange: true,
		descriptionText: "${name} is ${value}${unit}"
	]
	if (unit) {
		event.unit = unit
	}
	logDebug(event.descriptionText)
	return event
}


String intToHex(int value) {
	return Integer.toHexString(value).padLeft(2, "0").toUpperCase()
}

Integer hexToInt(String value) {
	return Integer.parseInt(value, 16)
}

Integer safeToInt(val, Integer defaultVal=0) {
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


boolean isDuplicateCommand(lastExecuted, allowedMil) {
	!lastExecuted ? false : (lastExecuted + allowedMil > new Date().time)
}


void logDebug(String msg) {
	if (state.debugLoggingEnabled != false) {
		log.debug "$msg"
	}
}

void logTrace(String msg) {
	// log.trace "$msg"
}