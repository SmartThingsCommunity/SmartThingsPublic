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
 *  Generic Z-Wave Motion/Light Sensor
 *
 *  Author: SmartThings
 *  Date: 2018-03-12
 */

metadata {
	definition(name: "Z-Wave Motion/Light Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Motion Sensor"
		capability "Illuminance Measurement"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

		//zw:S type:0701 mfr:021F prod:0003 model:0083 ver:3.92 zwv:4.05 lib:06 cc:5E,86,72,5A,73,80,31,71,30,70,85,59,84 role:06 ff:8C07 ui:8C07
		fingerprint mfr: "021F", prod: "0003", model: "0083", deviceJoinName: "Dome Motion/Light Sensor"
		//zw:S type:0701 mfr:0258 prod:0003 model:008D ver:3.80 zwv:4.38 lib:06 cc:5E,86,72,5A,73,80,31,71,30,70,85,59,84 role:06 ff:8C07 ui:8C07
		fingerprint mfr: "0258", prod: "0003", model: "008D", deviceJoinName: "NEO Coolcam Motion/Light Sensor"
		//zw:S type:0701 mfr:0258 prod:0003 model:108D ver:3.80 zwv:4.38 lib:06 cc:5E,86,72,5A,73,80,31,71,30,70,85,59,84 role:06 ff:8C07 ui:8C07 EU version
		fingerprint mfr: "0258", prod: "0003", model: "108D", deviceJoinName: "NEO Coolcam Motion/Light Sensor"
	}

	simulator {
		status "inactive": "command: 3003, payload: 00"
		status "active": "command: 3003, payload: FF"
		status "motionActiveNotification": "command: 7105, payload: 00 00 00 FF 07 08 00"
		status "motionInactiveNotification": "command: 7105, payload: 00 00 00 FF 07 00 01 08"
		status "motionInactiveNotification": "command: 7105, payload: 00 00 00 FF 07 00 01 08"
		status "luxHigh": "command: 3105, payload: 03 0A 20 C6"
		status "luxLow": "command: 3105, payload: 03 0A 00 08"
		status "luxMedium": "command: 3105, payload: 03 0A 01 90"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "motion", type: "generic", width: 6, height: 4) {
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label: 'motion', icon: "st.motion.motion.active", backgroundColor: "#00A0DC")
				attributeState("inactive", label: 'no motion', icon: "st.motion.motion.inactive", backgroundColor: "#CCCCCC")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery'
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

		main "motion"
		details(["motion", "battery", "illuminance"])
	}
}


def installed() {
	response([zwave.batteryV1.batteryGet().format(),
			"delay 500",
			zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x0C).format(), // motion
			"delay 500",
			zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x03, scale: 1).format(), // illuminance
			"delay 10000",
			zwave.wakeUpV2.wakeUpNoMoreInformation().format()])
}

def updated() {
	configure()
}

def configure() {
	// Device wakes up every 8 hours (+ 2 minutes), this interval allows us to miss one wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// Setting wakeUpNotification interval for NEO Coolcam and Dome devices
	if (isNeoCoolcam() || isDome()) {
		zwave.wakeUpV2.wakeUpIntervalSet(seconds: 4 * 3600, nodeid: zwaveHubNodeId).format()
	}
}

private getCommandClassVersions() {
	[
			0x71: 3,  // Notification
			0x20: 1,  // Basic
			0x80: 1,  // Battery
			0x72: 2,  // ManufacturerSpecific
			0x31: 5,  // SensorMultilevel
			0x84: 2,  // WakeUp
			0x30: 2   // Sensor Binary
	]
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
		results << createEvent(descriptionText: description, displayed: true)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			results += zwaveEvent(cmd)
		}
	}
	log.debug "'$description' parsed to ${results.inspect()}"
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	return sensorMotionEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	return sensorMotionEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def results = []
	if (cmd.notificationType == 0x07) {          // Burglar
		if (cmd.event == 0x08) {                 // detected
			results << sensorMotionEvent(1)
		} else if (cmd.event == 0x00) {          // inactive
			results << sensorMotionEvent(0)
		}
	}
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def results = []
	def map = [name: "battery", unit: "%", isStateChange: true]
	state.lastbatt = now()
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}
	results << createEvent(map)
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def results = []
	def map = [:]
	switch (cmd.sensorType) {
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			map.isStateChange = true
			break;
		default:
			map.descriptionText = cmd.toString()
	}
	results << createEvent(map)
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def results = []
	results << createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	results << response(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1).format())
	if (!state.lastbatt || (now() - state.lastbatt) >= 10 * 60 * 60 * 1000) {
		results << response(["delay 1000",
							 zwave.batteryV1.batteryGet().format(),
							 "delay 2000"
		])
	}
	results << response(zwave.wakeUpV2.wakeUpNoMoreInformation().format())
	return results
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Unhandled: ${cmd.toString()}"
	[:]
}

def sensorMotionEvent(value) {
	def result = []
	if (value) {
		result << createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		result << createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
	return result
}

private isDome() {
	zwaveInfo.mfr == "021F" && zwaveInfo.model == "0083"
}
private isNeoCoolcam() {
	zwaveInfo.mfr == "0258" && (zwaveInfo.model == "108D" || zwaveInfo.model == "008D")
}
