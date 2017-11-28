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
	definition (name: "Cooper Dimmer", namespace: "smartthings", author: "SmartThings") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"

		//zw:L type:1104 mfr:001A prod:4449 model:0101 ver:1.01 zwv:3.67 lib:03 cc:26,27,75,86,70,71,85,77,2B,2C,72,73,82,87
		fingerprint mfr: "001A ", prod: "4449", model: "0101", deviceJoinName: "Cooper Dimmer"
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
				width: 2, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "switch"
		details (["switch", "refresh"])
	}

	preferences {
		input "powerUpState", "enum", title: "Power Up State", description: "Off",
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
		//re-initialize with updated parameters
		initialize()
	}
}

def initialize() {
	def delayedOff = settings.delayedOffTime ? settings.delayedOffTime : 10
	def cmd = []
	cmd << hubAction(zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1,
			configurationValue: [delayedOff]))
	cmd << hubAction(zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1,
			configurationValue: [getPowerUpStateValue()]))
	//update current state
	cmd << hubAction(zwave.switchMultilevelV1.switchMultilevelGet())

	//Restore defaults where required:
	//we do not support fluid level change, so make dimming change z-wave commands instantaneous
	cmd << hubAction(zwave.configurationV1.configurationSet(parameterNumber: 7, size: 1,
			configurationValue: [0]))
	//disable kickstart feature - it alters dimming level over time
	cmd << hubAction(zwave.configurationV1.configurationSet(parameterNumber: 8, size: 1,
			configurationValue: [0]))
	//Device works best with factory default min/max levels set
	cmd << hubAction(zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1,
			configurationValue: [4]))
	cmd << hubAction(zwave.configurationV1.configurationSet(parameterNumber: 12, size: 1,
			configurationValue: [99]))
	if (!getDataValue("manufacturer")) {
		cmd << hubAction(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
	}
	sendHubCommand(cmd, 500)
}

def on() {
	def cmd = []
	cmd << hubAction(zwave.basicV1.basicSet(value: 255))
	cmd << hubAction(zwave.switchMultilevelV1.switchMultilevelGet())
	sendHubCommand(cmd, 500)
}

def off() {
	def cmd = []
	cmd << hubAction(zwave.basicV1.basicSet(value: 0))
	cmd << hubAction(zwave.switchMultilevelV1.switchMultilevelGet())
	sendHubCommand(cmd, 500)
}

def setLevel(value) {
	//apply manufacturer's default limits
	def newLevel = Math.min(99, Math.max(4, value))

	def cmd = []
	cmd << hubAction(zwave.switchMultilevelV1.switchMultilevelSet(value: newLevel))
	cmd << hubAction(zwave.switchMultilevelV1.switchMultilevelGet())
	sendHubCommand(cmd, 500)

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

	def cmd = []
	cmd << hubAction(zwave.switchMultilevelV2.switchMultilevelSet(value: newLevel,
			dimmingDuration: dimmingDuration))
	cmd << hubAction(zwave.switchMultilevelV1.switchMultilevelGet())
	sendHubCommand(cmd, refreshDelay)
}

def refresh() {
	def cmd = []
	cmd << hubAction(zwave.switchMultilevelV3.switchMultilevelGet())
	cmd << hubAction(zwave.configurationV1.configurationGet(parameterNumber: 1))
	cmd << hubAction(zwave.configurationV1.configurationGet(parameterNumber: 5))
	sendHubCommand(cmd)
}

def parse(String description) {
	//device sometimes sends configuration report with no configurationValue or size
	if (description.contains("command: 7006") && description.length() < 47) {
		//avoid null pointer exception in parse method
		log.debug "Bad configuration report: $description"
	} else {
		def cmd = zwave.parse(description, [0x70:1])
		if (cmd) {
			zwaveEvent(cmd)
		}
	}
	return []
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	handleLevelValue(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport  cmd) {
	handleLevelValue(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
	//device is switched on, so switch events are not necessary
	sendEvent(name: "level", value: cmd.value, unit: "%")
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.	SwitchMultilevelStartLevelChange cmd) {
	//Do nothing. Device will send SwitchMultilevelSet when level is set.
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
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport  cmd) {
	def delayedOff = settings.delayedOffTime ? settings.delayedOffTime : 10
	if(cmd.parameterNumber == 1 && delayedOff != cmd.configurationValue[0] ) {
		//Settings are read-only so inform user if device configuration does not match settings.
		sendEvent([descriptionText: "$device.displayName delayed off time settings are out of sync. Please save device preferences.",
					isStateChange: true])
	}
	if(cmd.parameterNumber == 5 && getPowerUpStateValue() != cmd.configurationValue[0] ) {
		//Settings are read-only so inform user if device configuration does not match settings.
		sendEvent([descriptionText: "$device.displayName power up state settings are out of sync. Please save device preferences.",
					isStateChange: true])
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

private handleLevelValue(value) {
	if (value) {
		sendEvent(name: "switch", value: "on")
		//1. switch can change level to 99 if switched off and button is held for 2 seconds
		//2. user can change dimming level while the device is off. This change will not be
		//   accessible remotely - no set command is called, and if we refresh
		//   SwitchMultilevelReport comes back with value == 0

		// Make sure level is limited in case we get BasicSet(value: 255)
		sendEvent(name: "level", value: Math.min(value, 99))
	} else {
		sendEvent(name: "switch", value: "off")
		// While "off", device dimmer state is remembered by device, but report will return 0
		// We set level to 0 to indicate that
		sendEvent(name: "level", value: 0)
		// Device will set current level when turned on,
		// and will turn on if user sets dimmer level remotely
	}
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

private hubAction(unformattedCommand) {
	new physicalgraph.device.HubAction(unformattedCommand.format())
}
