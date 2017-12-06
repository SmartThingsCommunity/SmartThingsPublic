/**
 *  Copyright 2017 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "Eaton Dimmer", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"

		//zw:L type:1104 mfr:001A prod:4449 model:0101 ver:1.01 zwv:3.67 lib:03 cc:26,27,75,86,70,71,85,77,2B,2C,72,73,82,87
		fingerprint mfr: "001A ", prod: "4449", model: "0101", deviceJoinName: "Eaton Dimmer"
	}

	tiles {
		multiAttributeTile(name: "switch",type: "lighting", width: 6, height:4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off",
						icon: "st.unknown.zwave.device", backgroundColor: "#00a0dc"
				attributeState "off", label: '${name}', action: "switch.on",
						icon: "st.unknown.zwave.device", backgroundColor: "#ffffff"
			}
			tileAttribute("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action: "switch level.setLevel", range: "(4..99)",  unit:"%"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat",
				width: 6, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "switch"
		details (["switch", "refresh"])
	}

	preferences {
		input "powerUpState", "enum", title: "Power Up State", description: "Last State",
				required: false, options:["on": "On", "off": "Off", "lastState": "Last State"]
		input "delayedOffTime", "number",
				title: "Time after which switch will turn off when using delayed off feature (1 - 255 [s])",
				description: "10", range: "1..255"
	}
}

def installed() {
	runIn(4, "initialize", [overwrite: true])
}

def updated() {
	if (!getDataValue("manufacturer")) {
		runIn(4, "initialize", [overwrite: true])
	} else {
		// Re-initialize with updated parameters.
		initialize()
	}
}

def initialize() {
	def delayedOffValue = settings.delayedOffTime ? settings.delayedOffTime : 10
	// Apply current preferences
	def cmds = []
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1,
			configurationValue: [delayedOffValue])
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1,
			configurationValue: [getPowerUpStateValue()])

	// Set/Restore defaults where required:
	// Make level change instantaneous by default
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1,
			configurationValue: [0])
	// Disable kickstart feature - it alters dimming level over time
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1,
			configurationValue: [0])
	// Device level works best with factory default min/max levels set
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1,
			configurationValue: [4])
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1,
			configurationValue: [99])

	// Update current state
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	// Retrieve device information from manufacturer specific report
	if (!getDataValue("manufacturer")) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}
	sendHubCommand cmds*.format(), 500
}

def on() {
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 255)
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	delayBetween cmds*.format(), 500
}

def off() {
	def cmds = []
	cmds << zwave.basicV1.basicSet(value: 0)
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	delayBetween cmds*.format(), 500
}

def setLevel(value) {
	//apply manufacturer's default limits
	def newLevel = Math.min(99, Math.max(4, value))

	def cmds = []
	cmds << zwave.switchMultilevelV1.switchMultilevelSet(value: newLevel)
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	delayBetween cmds*.format(), 500
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
	def cmds = []
	cmds << zwave.switchMultilevelV3.switchMultilevelGet()
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
	cmds << zwave.configurationV1.configurationGet(parameterNumber: 5)
	delayBetween cmds*.format()
}

def parse(String description) {
	def result = []
	//device sometimes sends configuration report with no configurationValue or size
	if (description.contains("command: 7006") && description.length() < 47) {
		//avoid null pointer exception in parse method
		log.debug "Bad configuration report: $description"
	} else {
		def cmd = zwave.parse(description, [0x70:1])
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	handleLevelValue(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport  cmd) {
	handleLevelValue(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
	//device is switched on, so switch events are not necessary
	createEvent(name: "level", value: cmd.value, unit: "%")
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.	SwitchMultilevelStartLevelChange cmd) {
	//Do nothing. Device will send SwitchMultilevelSet when level is set.
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	//update manufacturer info
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

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport  cmd) {
	def delayedOff = settings.delayedOffTime ? settings.delayedOffTime : 10
	//settings are read-only, so send event if settings are out of sync
	if(cmd.parameterNumber == 1 && cmd.configurationValue[0] != delayedOff) {
		createEvent([descriptionText: "$device.displayName delayed off time settings are out of sync. Please save device preferences.",
				isStateChange: true])
	} else if (cmd.parameterNumber == 5 && cmd.configurationValue[0] != getPowerUpStateValue()) {
		createEvent([descriptionText: "$device.displayName power up state settings are out of sync. Please save device preferences.",
				isStateChange: true])
	} else {
		return null
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

private handleLevelValue(value) {
	def events = []
	if (value) {
		events << createEvent(name: "switch", value: "on")
		//1. switch can change level to 99 if switched off and button is held for 2 seconds
		//2. user can change dimming level while the device is off. This change will not be
		//   accessible remotely - no set command is called, and if we refresh
		//   SwitchMultilevelReport comes back with value == 0

		// Make sure level is limited in case we get BasicSet(value: 255)
		events << createEvent(name: "level", value: Math.min(value, 99), unit: "%")
	} else {
		events << createEvent(name: "switch", value: "off")
		// While "off", device dimmer state is remembered by device, but report will return 0
		// We set level to 0 to indicate that
		events << createEvent(name: "level", value: 0, unit: "%")
		// Device will set current level when turned on,
		// and will turn on if user sets dimmer level remotely
	}
	events
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
