/**
 *  Eaton Accessory Dimmer
 *
 *  Copyright 2017 Smartthings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Eaton Dimmer", namespace: "smartthings", author: "Smartthings") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"

		attribute "protectionMode", "enum", ["unprotected", "sequence", "noControl"]

		command "toggleProtectionMode"

		// zw:L type:1202 mfr:001A prod:4441 model:0000 ver:3.17 zwv:3.67 lib:03 cc:26,27,75,86,70,71,85,77,2B,2C,72,73,87
		fingerprint mfr: "001A",prod: "4441", model: "0000", deviceJoinName: "RF9542-Z - RF Accessory Dimmer"

		//zw:L type:1104 mfr:001A prod:4449 model:0101 ver:1.01 zwv:3.67 lib:03 cc:26,27,75,86,70,71,85,77,2B,2C,72,73,82,87
		fingerprint mfr: "001A ", prod: "4449", model: "0101", deviceJoinName: "RF9540-N - RF Master Dimmer"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action: "switch.off", icon: "st.switches.switch.on",  backgroundColor: "#00a0dc"
				attributeState "off", label:'${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel", range: "1..100",  unit: "%"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 4, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		standardTile("protectionMode", "device.protectionMode", decoration: "flat", width: 2, height: 2) {
			state "unprotected", label: 'Unprotected', action: "toggleProtectionMode", icon: "st.security.alarm.off"
			state "sequence", label: 'Sequence', action: "toggleProtectionMode", icon: "st.security.alarm.partial"
			state "noControl", label: 'Protected', action: "toggleProtectionMode", icon: "st.security.alarm.on"
		}


		main "switch"
		details(["switch", "refresh", "protectionMode"])
	}

	preferences {
		input( "powerUpState", "enum", title: "Power Up State", description: "Power Up State",
				required: false, options:["on": "On", "off": "Off", "lastState": "Last State"]
		)
		input( "delayedOffTime", "number",
				title: "Time after which switch will turn off when using delayed off feature [seconds]",
				description: "[1..255]", range: "1..255", required: false, displayDuringSetup: true
		)
		input( "dimmerRampTime", "number",
				title: "Time of dimmed switching off [seconds]",
				description: "[0..255]", range: "0..255", required: false, displayDuringSetup: true
		)
	}
}

def installed() {
	log.debug "$device.displayName INSTALLED"
	runIn(4, "initialize", [overwrite: true])
}

def updated() {
	if (!getDataValue("manufacturer")) {
		runIn(4, "initialize", [overwrite: true])
	} else {
		//re-initialize with updated parameters
		initialize()
	}
}

def initialize() {
	log.debug "$device.displayName INITIALIZING"
	def delayedOff = settings.delayedOffTime ?: 10
	def dimmerRamp = settings.dimmerRampTime != null ? settings.dimmerRampTime : 5
	def powerUp = getPowerUpStateValue()
	def protectionValue = getProtectionValue()
	//this way we do not reset protection on initialized
	sendEvent(name: "protectionMode", value: protectionValue)

	def cmds = []
	// Update current state
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	//set protection state
	cmds << zwave.protectionV1.protectionSet(protectionState: protectionValue)
	//set delayed off parameter to value set in preferences (default value is 10s)
	cmds << zwave.configurationV1.configurationSet(configurationValue: [delayedOff], parameterNumber: 1, size: 1)
	//set powerUpState parameter to value set in preferences (default value is "lastState(=3)")
	cmds << zwave.configurationV1.configurationSet(configurationValue: [powerUp], parameterNumber: 5, size: 1)
	//set dimmer Ramp parameter to value set in preferences (default value is 5s)
	cmds << zwave.configurationV1.configurationSet(configurationValue: [dimmerRamp], parameterNumber: 7, size: 1)

	//ensure normal operation
	//basic value set (sets "on" value; default value is 0 for normal device operation)
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1)

	/* EATON MASTER DIMMER ONLY CONFIGURATION START */
	// Disable kickstart feature - it alters dimming level over time
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1,
			configurationValue: [0])
	// Device level works best with factory default min/max levels set
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1,
			configurationValue: [4])
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1,
			configurationValue: [99])
	/* EATON MASTER DIMMER ONLY CONFIGURATION END */


	// Retrieve device information from manufacturer specific report
	if (!getDataValue("manufacturer")) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}
	sendHubCommand(cmds*.format(), 500)
}

def on() {
	getOnOffCommands(0xFF)
}

def off() {
	getOnOffCommands(0x00)
}

def setLevel(value) {
	def currentRampTime = settings.dimmerRampTime != null ? settings.dimmerRampTime : 5
	//apply manufacturer's default limits
	def newLevel = Math.min(99, Math.max(4, value))

	def cmds = []
	cmds << zwave.switchMultilevelV1.switchMultilevelSet(value: newLevel)
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	delayBetween cmds*.format(), currentRampTime * 1000 + 500
}

def setLevel(value, duration) {
	//apply manufacturer's default limits
	def newLevel = Math.min(99, Math.max(4, value))
	def dimmingDuration
	def refreshDelay
	// duration - duration in [s].
	if (duration < 0x80) {
		// z-wave documentation: 0-127 is 1-127 seconds.
		dimmingDuration = duration
		// refresh delay in [ms]
		refreshDelay = duration * 1000 + 2000
	} else {
		// Values 0x80 - 0xFE indicate dimming duration 1-127 minutes.
		// we limit duration to 127 minutes so that
		// we do not hit value for factory default (0xFF)
		def durationInMinutes = Math.min(Math.round(duration / 60), 127)
		dimmingDuration = 0x80 + durationInMinutes
		// refresh delay in [ms]
		refreshDelay = durationInMinutes * 60 * 1000 + 2000
	}

	def cmds = []
	cmds << zwave.switchMultilevelV2.switchMultilevelSet(value: newLevel,
			dimmingDuration: dimmingDuration)
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	delayBetween cmds*.format(), refreshDelay
}

def refresh() {
	def timeNow = now()
	if (!state.refreshTriggeredAt || (1 * 1000 < (timeNow - state.refreshTriggeredAt))) {
		state.refreshTriggeredAt = timeNow
		def cmds = []
		cmds << zwave.basicV1.basicGet()
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 5)
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 7)
		cmds << zwave.protectionV1.protectionGet()
		delayBetween cmds*.format()
	} else {
		return null
	}
}

// handler for command "toggleProtectionMode"
def toggleProtectionMode() {
	def newProtection = (getProtectionValue() + 1) % 3 //current -> new : 0 -> 1; 1 -> 2; 2 -> 0
	def cmds = []
	cmds << zwave.protectionV1.protectionSet(protectionState: newProtection)
	cmds << zwave.protectionV1.protectionGet()
	delayBetween cmds*.format()
}

def parse(String description) {
	def result = []
	//Dimmer Master Switch device sometimes sends configuration report with no configurationValue or size
	if (description.contains("command: 7006") && description.length() < 47) {
		//avoid null pointer exception in parse method
		log.debug "Bad configuration report: $description"
	} else {
		def cmd = zwave.parse(description, [0x75: 1])
		if (cmd) {
			result += zwaveEvent(cmd)
		} else {
			log.debug "Event could not be parsed: ${description}"
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	createDimmerEvents(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	createDimmerEvents(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	createDimmerEvents(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
	//Do nothing. Device will send SwitchMultilevelSet when level is set.
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
	createEvent(name: "level", value: cmd.value, unit: "%")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd){
	def delayedOff = settings.delayedOffTime ?: 10
	def dimmerRamp = settings.dimmerRampTime != null ? settings.dimmerRampTime : 5
	//settings are read-only, so send event if settings are out of sync
	if(cmd.parameterNumber == 1 && cmd.configurationValue[0] != delayedOff) {
		createEvent([descriptionText: "$device.displayName delayed off time settings are out of sync. Please save device preferences.",
				isStateChange: true])
	} else if (cmd.parameterNumber == 5 && cmd.configurationValue[0] != getPowerUpStateValue()) {
		createEvent([descriptionText: "$device.displayName power up state settings are out of sync. Please save device preferences.",
				isStateChange: true])
	} else if (cmd.parameterNumber == 7 && cmd.configurationValue[0] != dimmerRamp){
		createEvent([descriptionText: "$device.displayName dimmer ramp time settings are out of sync. Please save device preferences.",
				isStateChange: true])
	}else {
		return null
	}
}

def zwaveEvent(physicalgraph.zwave.commands.protectionv1.ProtectionReport cmd) {
	def protectionModeStates = ["unprotected", "sequence", "noControl"]
	def protectionModeValue = protectionModeStates[cmd.protectionState]
	createEvent(name: "protectionMode", value: protectionModeValue)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "ManufacturerSpecificReport: ${cmd}"
	if (cmd.manufacturerName) {
		updateDataValue("manufacturer", cmd.manufacturerName)
	}
	if (cmd.productTypeId) {
		updateDataValue("productTypeId", cmd.productTypeId.toString())
	}
	if (cmd.productId != null) {
		updateDataValue("productId", cmd.productId.toString())
	}
	return null
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

private getOnOffCommands(value) {
	def currentRampTime = settings.dimmerRampTime != null ? settings.dimmerRampTime : 5
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: value)
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	delayBetween cmds*.format(), currentRampTime * 1000 + 500
}

private createDimmerEvents(newValue){
	def result = []
	result << createEvent(name: "switch", value: newValue > 0 ? "on" : "off")
	result << createEvent(name: "level", value: newValue)
	return result
}

private getPowerUpStateValue() {
	def value
	switch (settings.powerUpState) {
		case "off":
			value = 1
			break
		case "on":
			value = 2
			break
		case "lastState":
		default:
			value = 3
			break
	}
	value
}

private getProtectionValue() {
	def value
	//avoid MissingPropertyException
	switch (device.currentValue("protectionMode")) {
		case "sequence":
			value = 1
			break
		case "noControl":
			value = 2
			break
		case "unprotected":
		default:
			value = 0
			break
	}
	value
}

