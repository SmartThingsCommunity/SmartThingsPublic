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
	definition (name: "Eaton Accessory Dimmer", namespace: "smartthings", author: "Smartthings") {
		capability "Actuator"
		capability "Sensor"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"
		fingerprint mfr:"001A",prod:"4441", model:"0000", deviceJoinName: "RF9542-Z - RF Accessory Dimmer"
		// zw:L type:1202 mfr:001A prod:4441 model:0000 ver:3.17 zwv:3.67 lib:03 cc:26,27,75,86,70,71,85,77,2B,2C,72,73,87
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on",  label:'${name}', action:"switch.off", icon:"st.switches.switch.on",  backgroundColor: "#00a0dc"
				attributeState "off", label:'${name}', action:"switch.on",  icon:"st.switches.switch.off", backgroundColor:"#ffffff"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel", range: "1..100",  unit:"%"
			}
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 6, height: 2, backgroundColor: "#00a0dc") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main("switch")
		details(["switch","refresh"])
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
				description: "[1..255]", range: "1..255", required: false, displayDuringSetup: true
		)
	}
}

def installed() {
	log.debug "INSTALLED"
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
	log.debug "INITIALIZE"
	def delayedOff = settings.delayedOffTime ? settings.delayedOffTime : 10
	def dimmerRamp = settings.dimmerRampTime ? settings.dimmerRampTime : 5
	def powerUp = getPowerUpStateValue()

	def cmds = []
	// Update current state
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	//set delayed off parameter to value set in preferences (default value is 10s)
	cmds << zwave.configurationV1.configurationSet(configurationValue: [delayedOff], parameterNumber: 1, size: 1)
	//set powerUpState parameter to value set in preferences (default value is "lastState(=3)")
	cmds << zwave.configurationV1.configurationSet(configurationValue: [powerUp], parameterNumber: 5, size: 1)
	//set dimmer Ramp parameter to value set in preferences (default value is 5s)
	cmds << zwave.configurationV1.configurationSet(configurationValue: [dimmerRamp], parameterNumber: 7, size: 1)
	// Retrieve device information from manufacturer specific report
	if (!getDataValue("manufacturer")) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}
	sendHubCommand(cmds*.format(), 500)
}

def on() {
	handleOnOff(0xFF)
}

def off() {
	handleOnOff(0x00)
}

def setLevel(value) {
	def cmds = []
	//apply manufacturer's default limits
	def newLevel = Math.min(99, Math.max(4, value))
    cmds << zwave.switchMultilevelV1.switchMultilevelSet(value: newLevel)
	addDelayedCommandsAccordingToDimmerRampTime(cmds)
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
	delayBetween(cmds*.format(), refreshDelay)
}

def refresh() {
	def timeNow = now()
	if (!state.refreshTriggeredAt || (1 * 1000 < (timeNow - state.refreshTriggeredAt))) {
		state.refreshTriggeredAt = timeNow
		def cmds = []
		cmds << zwave.basicV1.basicGet()
		//cmds << zwave.switchMultilevelV1.switchMultilevelGet()
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 5)
		cmds << zwave.configurationV1.configurationGet(parameterNumber: 7)
		delayBetween(cmds*.format())
	} else {
		return null
	}
}

def parse(String description) {
	def result = []
	def cmd = zwave.parse(description)
	if (cmd) {
   		result += zwaveEvent(cmd)
	} else {
		log.debug "Non-parsed event: ${description}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	// creating on/off event is omitted, that state will be updated later (when level drops down to 0)
	def cmds = []
    return response(addDelayedCommandsAccordingToDimmerRampTime(cmds))
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelSet cmd) {
	createEvent(name: "level", value: cmd.value, unit: "%")
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelStartLevelChange cmd) {
	//Do nothing. Device will send SwitchMultilevelSet when level is set.
	return null
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	handleDimmerValue(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	handleDimmerValue(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd){
	def delayedOff = settings.delayedOffTime ? settings.delayedOffTime : 10
	def dimmerRamp = settings.dimmerRampTime ? settings.dimmerRampTime : 5
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

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
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

private handleDimmerValue(newValue){
	def result = []
	result << createEvent(name: "switch", value: newValue > 0 ? "on" : "off")
	result << createEvent(name: "level", value: newValue)
	return result
}

private handleOnOff(newValue){
	def cmds = []	
    cmds << zwave.basicV1.basicSet(value: newValue)
	cmds << zwave.basicV1.basicGet()   
	addDelayedCommandsAccordingToDimmerRampTime(cmds)
}

private addDelayedCommandsAccordingToDimmerRampTime(cmds){
	def rampTime = settings.dimmerRampTime ? settings.dimmerRampTime : 5
	// calculate delay between basicGet commands
	def totalDelay = (rampTime + 2)  * 1000
	
    def delay =  (totalDelay <= 10000) ? 1000 : totalDelay.intdiv(10)
	// add basicGet commands in order to refresh actual state in smart application
	// if dimmerRampTime is shorter than 10 second, basicGet commands will be called with 1 seconds delay
	// if dimmerRampTime is longer than 10 second, basicGet commands will be called with totalDelay/10 seconds delay
	while (totalDelay > 0){
        cmds << zwave.basicV1.basicGet()
		totalDelay -= delay
	}
	delayBetween(cmds*.format(),delay)
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