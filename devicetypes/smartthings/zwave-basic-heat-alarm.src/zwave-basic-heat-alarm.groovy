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
	definition(name: "Z-Wave Basic Heat Alarm", namespace: "smartthings", author: "SmartThings") {
		capability "Temperature Alarm"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		//zw:S type:0701 mfr:026F prod:0001 model:0002 ver:1.07 zwv:4.24 lib:03 cc:5E,86,72,5A,73,80,71,85,59,84 role:06 ff:8C01 ui:8C01
		fingerprint mfr: "026F ", prod: "0001", model: "0002", deviceJoinName: "FireAngel Thermistek ZHT-630 Heat Alarm/Detector"
	}

	simulator {
		status "battery 100%": "command: 8003, payload: 64"
		status "battery 5%": "command: 8003, payload: 05"
		status "HeatNotification": "command: 7105, payload: 00 00 00 FF 04 02 80 4E"
		status "HeatClearNotification": "command: 7105, payload: 00 00 00 FF 04 00 80 05"
		status "HeatTestNotification": "command: 7105, payload: 00 00 00 FF 04 07 80 05"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "heat", type: "lighting", width: 6, height: 4) {
			tileAttribute("device.heat", key: "PRIMARY_CONTROL") {
				attributeState("clear", label: "clear", icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff")
				attributeState("detected", label: "HEAT", icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13")
				attributeState("tested", label: "TEST", icon: "st.alarm.smoke.test", backgroundColor: "#e86d13")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main "heat"
		details(["heat", "battery"])
	}
}

def installed() {
	// Device checks in every hour, this interval allows us to miss one check-in notification before marking offline
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	def cmds = []
	createHeatEvents("heatClear", cmds)
	cmds.each { cmd -> sendEvent(cmd) }
}

def updated() {
	// Device checks in every hour, this interval allows us to miss one check-in notification before marking offline
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def getCommandClassVersions() {
	[
			0x80: 1, // Battery
			0x84: 1, // Wake Up
			0x71: 3, // Alarm
			0x72: 1, // Manufacturer Specific
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

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = []
	results << createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	if (!state.lastbatt || (now() - state.lastbatt) >= 56 * 60 * 60 * 1000) {
		results << response([
				zwave.batteryV1.batteryGet().format(),
				"delay 2000",
				zwave.wakeUpV1.wakeUpNoMoreInformation().format()
		])
	} else {
		results << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%", isStateChange: true]
	state.lastbatt = now()
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}
	return createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	def event = [displayed: false]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: $cmd"
	return createEvent(event)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = null
	if (cmd.notificationType == 0x04) {  // Heat Alarm
		switch (cmd.event) {
			case 0x00:
			case 0xFE:
				result = createHeatEvents("heatClear")
				break
			case 0x01: //Overheat detected
			case 0x02: //Overheat detected Unknown Location
			case 0x03: //Rapid Temperature Rise
			case 0x03: //Rapid Temperature Rise Unknown Location
				result = createHeatEvents("heat")
				break
			case 0x07:
				result = createHeatEvents("tested")
				break
		}
	}
	return result
}

def createHeatEvents(name) {
	def result = null
	def text = null
	switch (name) {
		case "heat":
			text = "$device.displayName heat was detected!"
			result = createEvent(name: "heat", value: "detected", descriptionText: text)
			break
		case "tested":
			text = "$device.displayName heat tested"
			result = createEvent(name: "heat", value: "tested", descriptionText: text)
			break
		case "heatClear":
			text = "$device.displayName heat is clear"
			result = createEvent(name: "heat", value: "clear", descriptionText: text)
			break
		case "testClear":
			text = "$device.displayName heat cleared"
			result = createEvent(name: "heat", value: "clear", descriptionText: text)
			break
	}
	return result
}