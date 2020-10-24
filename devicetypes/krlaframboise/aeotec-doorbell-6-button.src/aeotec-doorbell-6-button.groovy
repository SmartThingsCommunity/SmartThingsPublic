/**
 *  Aeotec Doorbell 6 Button (CHILD DEVICE) v1.3
 *
 *  Author:
 *    Kevin LaFramboise (krlaframboise)
 *
 *  Changelog:
 *
 *    1.3 (09/13/2020)
 *      - Changed ocfDeviceType to remotecontroller which adds the button action to the device details screen and lets you choose the button pressed event from the Automattions action list.
 *
 *    1.2.2 (05/09/2020)
 *      - Implemented button capability for new mobile app.
 *
 *    1.2.1 (03/14/2020)
 *      - Fixed bug with enum settings that was caused by a change ST made in the new mobile app.
 *
 *    1.2 (08/10/2019)
 *      - Added setVolume command, but using that command to change the volume won't change it in the settings.
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
metadata {
	definition (
		name: "Aeotec Doorbell 6 Button",
		namespace: "krlaframboise",
		author: "Kevin LaFramboise",		
		ocfDeviceType: "x.com.st.d.remotecontroller"

	) {
		capability "Actuator"
		capability "Sensor"
		capability "Button"
		capability "Battery"
		capability "Switch"
		capability "Refresh"

		attribute "firmwareVersion", "string"
		attribute "lastPushed", "string"

		command "setVolume"
	}

	simulator { }

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: false){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			}
			tileAttribute ("device.lastPushed", key: "SECONDARY_CONTROL") {
				attributeState "lastPushed", label:'Last Pushed: ${currentValue}'
			}
		}

		standardTile("on", "device.switch", width: 2, height: 2) {
			state "default", label:'On', action: "switch.on"
		}

		standardTile("off", "device.switch", width: 2, height: 2) {
			state "default", label:'Off', action: "switch.off"
		}

		standardTile("refresh", "device.refresh", width: 2, height: 2) {
			state "default", label:'Refresh', action: "refresh", icon:"st.secondary.refresh-icon"
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 3, height: 1) {
			state "battery", label:'${currentValue}% Battery', unit:"%"
		}

		valueTile("firmwareVersion", "device.firmwareVersion", decoration:"flat", width:3, height: 1) {
			state "firmwareVersion", label:'Firmware ${currentValue}'
		}

		main("switch")
		details(["switch", "on", "off", "refresh", "battery", "firmwareVersion"])
	}

	preferences {
		getOptionsInput("tone", "Sound", defaultTone, setDefaultOption(toneOptions, defaultTone))

		getOptionsInput("volume", "Volume", defaultVolume, setDefaultOption(volumeOptions, defaultVolume))

		getOptionsInput("lightEffect", "Light Effect", defaultLightEffect, setDefaultOption(lightEffectOptions, defaultLightEffect))

		getOptionsInput("repeat", "Repeat", defaultRepeat, setDefaultOption(getRepeatOptions(false), defaultRepeat))

		getOptionsInput("repeatDelay", "Repeat Delay", defaultRepeatDelay, setDefaultOption(repeatDelayOptions, defaultRepeatDelay))

		getOptionsInput("toneIntercept", "Tone Intercept Length", defaultToneIntercept, setDefaultOption(toneInterceptOptions, defaultToneIntercept))

		input "debugOutput", "bool",
			title: "Enable Debug Logging?",
			defaultValue: true,
			required: false
	}
}

private getOptionsInput(name, title, defaultVal, options) {
	input "${name}", "enum",
		title: "${title}:",
		required: false,
		defaultValue: defaultValue,
		displayDuringSetup: true,
		options: options
}


private getVolumeSetting() {
	return safeToInt(settings?.volume, defaultVolume)
}

private getToneSetting() {
	return safeToInt(settings?.tone, defaultTone)
}

private getLightEffectSetting() {
	return safeToInt(settings?.lightEffect, defaultLightEffect)
}

private getRepeatSetting() {
	return safeToInt(settings?.repeat, defaultRepeat)
}

private getRepeatDelaySetting() {
	return safeToInt(settings?.repeatDelay, defaultRepeatDelay)
}

private getToneInterceptSetting() {
	return safeToInt(settings?.toneIntercept, defaultToneIntercept)
}

private getDefaultVolume() { return 30 }
private getDefaultTone() { return 1 }
private getDefaultLightEffect() { return 4 }
private getDefaultRepeat() { return 1 }
private getDefaultRepeatDelay() { return 0 }
private getDefaultToneIntercept() { return 0 }


def installed() {
	logDebug "installed()..."

	initialize()
}


def updated() {
	logDebug "updated()..."

	initialize()

	parent?.childUpdated(buttonNumber, getGroupSettings(volumeSetting))
}

private initialize() {
	if (!device.currentValue("numberOfButtons")) {
		sendEvent(getEventMap("numberOfButtons", 1))
	}

	if (!device.currentValue("supportedButtonValues")) {
		sendEvent(getEventMap("supportedButtonValues", ["pushed"].encodeAsJSON()))
	}

	if (!device.currentValue("button")) {
		def evt = getEventMap("button", "pushed")
		evt.data = [buttonNumber: buttonNumber]
		sendEvent(evt)
	}

	if (!device.currentValue("switch")) {
		sendEvent(getEventMap("switch", "off"))
	}
}

def setVolume(volume) {
	logDebug "setVolume(${volume})..."

	parent?.childUpdated(buttonNumber, getGroupSettings(validateVolume(volume)))
}

private getGroupSettings(volume) {
	[
		"tone": toneSetting,
		"volume": volume,
		"lightEffect": lightEffectSetting,
		"repeat": repeatSetting,
		"repeatDelay": repeatDelaySetting,
		"toneIntercept": toneInterceptSetting,
		"childName": device.displayName
	]
}

private validateVolume(value) {
	def volume = safeToInt(value, 50)
	if (volume > 100) volume = 100
	if (volume < 0) volume = 0
	return volume
}


def refresh() {
	logDebug "refresh()..."
	parent?.childRefresh(buttonNumber)
}


def on() {
	logDebug "on()..."
	parent?.childOn(buttonNumber)
}


def off() {
	logDebug "off()..."
	parent?.childOff(buttonNumber)
}


def getEventMap(name, value, displayed=false, unit=null) {
	def eventMap = [
		name: name,
		value: value,
		displayed: displayed,
		isStateChange: true,
		descriptionText: "${device.displayName} - ${name} ${value}"
	]

	if (unit) {
		eventMap.unit = unit
		eventMap.descriptionText = "${eventMap.descriptionText}${unit}"
	}

	if (displayed) {
		logDebug "${eventMap.descriptionText}"
	}
	return eventMap
}


def setDefaultOption(options, defaultVal) {
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

def getToneOptions() {
	def options = [:]

	(1..30).each {
		options["${it}"] = "Tone #${it}"
	}

	return options
}

def getLightEffectOptions() {
	[
		1:"Off",
		2:"On",
		4:"Slow Pulse",
		8:"Pulse",
		16:"Fast Pulse",
		32:"Flash",
		64:"Strobe"
	]
}

def getRepeatOptions(includeUnlimited) {
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


private getButtonNumber() {
	return safeToInt(getDataValue("buttonNumber"))
}

private safeToInt(val, defaultVal=0) {
	return "${val}"?.isInteger() ? "${val}".toInteger() : defaultVal
}


def logDebug(msg) {
	if (settings?.debugOutput != false) {
		log.debug "$msg"
	}
}