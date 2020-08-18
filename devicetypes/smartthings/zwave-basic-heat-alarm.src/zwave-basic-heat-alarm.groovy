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
	definition(name: "Z-Wave Basic Heat Alarm", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.smoke") {
		capability "Temperature Alarm"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		//zw:S type:0701 mfr:026F prod:0001 model:0002 ver:1.07 zwv:4.24 lib:03 cc:5E,86,72,5A,73,80,71,85,59,84 role:06 ff:8C01 ui:8C01
		fingerprint mfr: "026F ", prod: "0001", model: "0002", deviceJoinName: "FireAngel Smoke Detector" //FireAngel Thermistek Alarm
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
			tileAttribute("device.temperatureAlarm", key: "PRIMARY_CONTROL") {
				attributeState("cleared", label: "cleared", icon: "st.alarm.smoke.clear", backgroundColor: "#ffffff")
				attributeState("heat", label: "HEAT", icon: "st.alarm.smoke.smoke", backgroundColor: "#e86d13")
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
	def cmds = []
	cmds << checkIntervalEvent
	cmds << createHeatEvents("clear")
	cmds.each { cmd -> sendEvent(cmd) }
	response(initialPoll())
}

def updated() {
	//sendEvent(checkIntervalEvent)
}

def getCheckIntervalEvent() {
	// Device checks in every 4 hours, this interval allows us to miss one check-in notification before marking offline
	createEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
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
				result = createHeatEvents("clear")
				break
			case 0x01: //Overheat detected
			case 0x02: //Overheat detected Unknown Location
			case 0x03: //Rapid Temperature Rise
			case 0x03: //Rapid Temperature Rise Unknown Location
			case 0x07: //Tested
				result = createHeatEvents("heat")
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
			result = createEvent(name: "temperatureAlarm", value: "heat", descriptionText: text)
			break
		case "clear":
			text = "$device.displayName heat is clear"
			result = createEvent(name: "temperatureAlarm", value: "cleared", descriptionText: text, isStateChange: true)
			log.debug "Clear event created"
			break
	}
	return result
}

private command(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo?.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay = 200) {
	delayBetween(commands.collect { command(it) }, delay)
}

def initialPoll() {
	def request = []
	// check initial battery
	request << zwave.batteryV1.batteryGet()
	commands(request, 500) + ["delay 6000", command(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}