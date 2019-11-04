/**
 *  Copyright 2018 SmartThings
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
	definition(name: "Z-Wave Motion Light", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.light", mmnm: "SmartThings", vid: "generic-motion-light") {
		capability "Switch"
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

		fingerprint mfr: "0060", prod: "0012", model: "0001", deviceJoinName: "Everspring Outdoor Floodlight"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 1, height: 1, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState("on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
				attributeState("off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
				attributeState("turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
				attributeState("turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
			}
		}
		valueTile("motion", "device.motion", decoration: "flat", width: 2, height: 2) {
			state("active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC")
			state("inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#CCCCCC")
		}
		valueTile("illuminance", "device.illuminance", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "illuminance", label: '${currentValue} lux', backgroundColors: [
					[value: 40, color: "#999900"],
					[value: 100, color: "#CCCC00"],
					[value: 300, color: "#FFFF00"],
					[value: 500, color: "#FFFF33"],
					[value: 1000, color: "#FFFF66"],
					[value: 2000, color: "#FFFF99"],
					[value: 10000, color: "#FFFFCC"]
			]
		}

		main "switch"
		details(["switch", "motion", "illuminance"])
	}
}

def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 24 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "motion", value: "inactive", displayed: false)
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def configure() {
	def cmds =  [
			secure(zwave.notificationV3.notificationGet(notificationType: 0x07)),
			secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03)),
			secure(zwave.switchBinaryV1.switchBinaryGet())
	]
	if (isEverspringFloodlight()) {
		cmds += secure(zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, scaledConfigurationValue: 10)) //enables illuminance report every 10 minutes
	}
	cmds
}

def ping() {
	response(secure(zwave.switchBinaryV1.switchBinaryGet()))
}

def parse(String description) {
	def result = []
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			result += zwaveEvent(cmd)
		}
	}
	log.debug "Parse returned: ${result}"
	result
}

def on() {
	[
			secure(zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)),
			"delay 500",
			secure(zwave.switchBinaryV1.switchBinaryGet())
	]
}

def off() {
	[
			secure(zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)),
			"delay 500",
			secure(zwave.switchBinaryV1.switchBinaryGet())
	]
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def map = [name: "switch"]
	map.value = cmd.value ? "on" : "off"
	map.descriptionText = "${device.displayName} light has been turned ${map.value}"
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	if (cmd.notificationType == 0x07) {
		if (cmd.event == 0x08) {                 // detected
			createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
		} else if (cmd.event == 0x00) {          // inactive
			createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	switch (cmd.sensorType) {
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			map.isStateChange = true
			break
		default:
			map.descriptionText = cmd.toString()
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Unhandled command: ${cmd}"
	[:]
}

private secure(cmd) {
	if(zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private isEverspringFloodlight() {
	zwaveInfo.mfr == "0060" && zwaveInfo.prod == "0012"
}