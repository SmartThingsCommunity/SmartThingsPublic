/**
 *  Copyright 2019 SmartThings
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
	definition(name: "Z-Wave Mouse Trap", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-pestcontrol-1", runLocally: false, executeCommandsLocally: false) {
		capability "Sensor"
		capability "Battery"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Pest Control"
		//capability "pestControl", enum: idle, trapArmed, trapRearmRequired, pestDetected, pestExterminated

		//zw:S type:0701 mfr:021F prod:0003 model:0104 ver:3.49 zwv:4.38 lib:06 cc:5E,86,72,5A,73,80,71,30,85,59,84,70 role:06 ff:8C13 ui:8C13
		fingerprint mfr: "021F", prod: "0003", model: "0104", deviceJoinName: "Dome Mouser"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "pestControl", type: "generic", width: 6, height: 4) {
			tileAttribute("device.pestControl", key: "PRIMARY_CONTROL") {
				attributeState("idle", label: 'IDLE', icon: "st.contact.contact.open", backgroundColor: "#00FF00")
				attributeState("trapRearmRequired", label: 'TRAP RE-ARM REQUIRED', icon: "st.contact.contact.open", backgroundColor: "#00A0DC")
				attributeState("trapArmed", label: 'TRAP ARMED', icon: "st.contact.contact.open", backgroundColor: "#FF6600")
				attributeState("pestDetected", label: 'PEST DETECTED', icon: "st.contact.contact.open", backgroundColor: "#FF6600")
				attributeState("pestExterminated", label: 'PEST EXTERMINATED', icon: "st.contact.contact.closed", backgroundColor: "#FF0000")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label: '', action: "configuration.configure", icon: "st.secondary.configure"
		}
		main "pestControl"
		details(["pestControl", "battery", "refresh", "configure"])
	}
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping() called"
	refresh()
}

def refresh() {
	log.debug "sending battery refresh command"
	def cmds = []
	cmds << zwave.batteryV1.batteryGet().format()
	cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	cmds << zwave.notificationV3.notificationGet(notificationType: 0x13).format()
	cmds << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_CO).format()
	return delayBetween(cmds, 2000)
}

def parse(String description) {
	def result = []
	log.debug "desc: $description"
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "parsed '$description' to $result"
	return result
}

def installed() {
	log.debug "installed()"
	// Device-Watch simply pings if no device events received for 8h 6min(checkInterval)
	sendEvent(name: "checkInterval", value: 24 * 60 * 60 + 6 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	initialize()
}

def updated() {
	log.debug "updated()"
	// Device-Watch simply pings if no device events received for 8h 6min(checkInterval)
	sendEvent(name: "checkInterval", value: 24 * 60 * 60 + 6 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	runIn(12, "initialize", [overwrite: true, forceForLocallyExecuting: true])
}

def initialize() {
	log.debug "initialize()"
	def cmds = []

	// Set a limit to the number of times that we run so that we don't run forever and ever
	if (!state.initializeCount) {
		state.initializeCount = 1
	} else if (state.initializeCount <= 10) { // Keep checking for ~2 mins (10 * 12 sec intervals)
		state.initializeCount = state.initializeCount + 1
	} else {
		state.initializeCount = 0
		return // TODO: This might be a good opportunity to mark the device unhealthy
	}

	cmds << getConfigurationCommands()
	if (cmds.size()) {
		sendHubCommand(cmds)
		runIn(12, "initialize", [overwrite: true, forceForLocallyExecuting: true])
	} else {
		state.initializeCount = 0
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	// ignore, to prevent override of NotificationReport
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	// ignore, to prevent override of SensorBinaryReport
	[]
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.debug "Event: ${cmd.event}, Notification type: ${cmd.notificationType}"
	def result = []
	def value
	def description
	if (cmd.notificationType == 0x07) {
		//notificationType == 0x07 (Home Security)
		switch (cmd.event) {
			case 0x00:
				value = "idle"
				description = "Trap cleared"
				break
			case 0x07:
				value = "pestExterminated"
				description = "Pest exterminated"
				break
			default:
				break
		}
		result = createEvent(name: "pestControl", value: value, descriptionText: description)
	} else if (cmd.notificationType == 0x13) {
		//notificationType == 0x13 (Pest Control)
		switch (cmd.event) {
			case 0x00:
				value = "idle"
				description = "Trap cleared"
				break
			case 0x02:
				value = "trapArmed"
				description = "Trap armed"
				break
			case 0x04:
				value = "trapRearmRequired"
				description = "Trap re-arm required"
				break
			case 0x06:
				value = "pestDetected"
				description = "Pest detected"
				break
			case 0x08:
				value = "pestExterminated"
				description = "Pest exterminated"
				break
			default:
				break
		}
		result = createEvent(name: "pestControl", value: value, descriptionText: description)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	log.debug "WakeUpNotification ${cmd}"
	def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
	def cmds = []
	if (!state.MSR) {
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
		cmds << "delay 1200"
	}
	if (device.currentValue("pestControl") == null) { // In case our initial request didn't make it
		cmds << zwave.notificationV3.notificationGet(notificationType: 0x13).format()
	}
	if (!state.lastbat || now() - state.lastbat > (24 * 60 * 60 + 6 * 60) * 1000 /*milliseconds*/) {
		cmds << zwave.batteryV1.batteryGet().format()
	} else {
		// If we check the battery state we will send NoMoreInfo in the handler for BatteryReport so that we definitely get the report
		cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	}
	[event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%"]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		log.debug "Battery report: $cmd"
		map.value = cmd.batteryLevel
	}
	state.lastbat = now()
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

	if (!device.currentState("battery")) {
		result << response(zwave.batteryV1.batteryGet())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: true)
}

def configure() {
	log.debug "config"
	response(getConfigurationCommands())
}

def getConfigurationCommands() {
	log.debug "getConfigurationCommands"
	def cmds = []
	cmds << zwave.batteryV1.batteryGet().format()
	cmds << zwave.notificationV3.notificationGet(notificationType: 0x13).format()
	cmds << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_CO).format()

	// The wake-up interval is set in seconds, and is 43,200 seconds (12 hours) by default.
	cmds << zwave.wakeUpV2.wakeUpIntervalSet(seconds: 12 * 3600, nodeid: zwaveHubNodeId).format()

	// BASIC_SET Level, default: 255
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, configurationValue: [255]).format()
	// Set Firing Mode, default: 2 (Burst fire)
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, configurationValue: [2]).format()
	// 	This parameter defines how long the Mouser will fire continuously before it starts to burst-fire, default: 360 seconds
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 2, configurationValue: [360]).format()
	// Enable/Disable LED Alarm, default: 1 (enabled)
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [1]).format()
	// LED Alarm Duration, default: 0 hours
	cmds << zwave.configurationV1.configurationSet(parameterNumber: 5, size: 1, configurationValue: [0]).format()
	cmds
}