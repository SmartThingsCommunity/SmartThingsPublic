/**
 *  Copyright 2015 SmartThings
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
	definition (name: "Z-Wave Basic Smoke Alarm", namespace: "smartthings", author: "SmartThings", genericHandler: "Z-Wave") {
		capability "Smoke Detector"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		fingerprint deviceId: "0xA100", inClusters: "0x20,0x80,0x70,0x85,0x71,0x72,0x86"
		fingerprint mfr:"0138", prod:"0001", model:"0001", deviceJoinName: "First Alert Smoke Detector"
		//zw:S type:0701 mfr:026F prod:0001 model:0001 ver:1.07 zwv:4.24 lib:03 cc:5E,86,72,5A,73,80,71,85,59,84 role:06 ff:8C01 ui:8C01
		fingerprint mfr: "026F ", prod: "0001", model: "0001", deviceJoinName: "FireAngel Thermoptek Smoke Alarm"
		fingerprint mfr: "013C", prod: "0002", model: "001E", deviceJoinName: "Philio Smoke Alarm PSG01"
		fingerprint mfr: "0154", prod: "0004", model: "0010", deviceJoinName: "POPP 10Year Smoke Sensor"
		fingerprint mfr: "0154", prod: "0100", model: "0201", deviceJoinName: "POPP Smoke Detector with Siren"
	}

	simulator {
		status "smoke": "command: 7105, payload: 01 FF"
		status "clear": "command: 7105, payload: 01 00"
		status "test": "command: 7105, payload: 0C FF"
		status "battery 100%": "command: 8003, payload: 64"
		status "battery 5%": "command: 8003, payload: 05"
		status "smokeNotification": "command: 7105, payload: 00 00 00 FF 01 02 80 4E"
		status "smokeClearNotification": "command: 7105, payload: 00 00 00 FF 01 00 80 05"
		status "smokeTestNotification": "command: 7105, payload: 00 00 00 FF 01 03 80 05"
	}

	tiles (scale: 2){
		multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
			tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
				attributeState("clear", label:"clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
				attributeState("detected", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
				attributeState("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "smoke"
		details(["smoke", "battery"])
	}
}

def installed() {
	def cmds = []
  //This interval allows us to miss one check-in notification before marking offline
	cmds << createEvent(name: "checkInterval", value: checkInterval * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	createSmokeEvents("smokeClear", cmds)
	cmds.each { cmd -> sendEvent(cmd) }
	response(initialPoll())
}

def getCheckInterval() {
	def checkIntervalValue
	switch (zwaveInfo.mfr) {
		case "0138": checkIntervalValue = 2  //First Alert checks in every hour
			break
		default: checkIntervalValue = 8
	}
	return checkIntervalValue
}


def updated() {
  //This interval allows us to miss one check-in notification before marking offline
	sendEvent(name: "checkInterval", value: checkInterval * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def getCommandClassVersions() {
	[
			0x71: 3, // Alarm
			0x72: 1, // Manufacturer Specific
			0x80: 1, // Battery
			0x84: 1, // Wake Up
	]
}


def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results << createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			zwaveEvent(cmd, results)
		}
	}
	log.debug "'$description' parsed to ${results.inspect()}"
	return results
}

def createSmokeEvents(name, results) {
	def text = null
	switch (name) {
		case "smoke":
			text = "$device.displayName smoke was detected!"
			// these are displayed:false because the composite event is the one we want to see in the app
			results << createEvent(name: "smoke", value: "detected", descriptionText: text)
			break
		case "tested":
			text = "$device.displayName was tested"
			results << createEvent(name: "smoke", value: "tested", descriptionText: text)
			break
		case "smokeClear":
			text = "$device.displayName smoke is clear"
			results << createEvent(name: "smoke", value: "clear", descriptionText: text)
			name = "clear"
			break
		case "testClear":
			text = "$device.displayName test cleared"
			results << createEvent(name: "smoke", value: "clear", descriptionText: text)
			name = "clear"
			break
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, results) {
	if (cmd.notificationType == 0x01) {  // Smoke Alarm
		switch (cmd.event) {
			case 0x00:
			case 0xFE:
				createSmokeEvents("smokeClear", results)
				break
			case 0x01:
			case 0x02:
				createSmokeEvents("smoke", results)
				break
			case 0x03:
				createSmokeEvents("tested", results)
				break
		}
	} else switch (cmd.v1AlarmType) {
		case 1:
			createSmokeEvents(cmd.v1AlarmLevel ? "smoke" : "smokeClear", results)
			break
		case 12:  // test button pressed
			createSmokeEvents(cmd.v1AlarmLevel ? "tested" : "testClear", results)
			break
		case 13:  // sent every hour -- not sure what this means, just a wake up notification?
			if (cmd.v1AlarmLevel == 255) {
				results << createEvent(descriptionText: "$device.displayName checked in", isStateChange: false)
			} else {
				results << createEvent(descriptionText: "$device.displayName code 13 is $cmd.v1AlarmLevel", isStateChange: true, displayed: false)
			}

			// Clear smoke in case they pulled batteries and we missed the clear msg
			if (device.currentValue("smoke") != "clear") {
				createSmokeEvents("smokeClear", results)
			}

			// Check battery if we don't have a recent battery event
			if (!state.lastbatt || (now() - state.lastbatt) >= 48 * 60 * 60 * 1000) {
				results << response(zwave.batteryV1.batteryGet())
			}
			break
		default:
			results << createEvent(displayed: true, descriptionText: "Alarm $cmd.v1AlarmType ${cmd.v1AlarmLevel == 255 ? 'activated' : cmd.v1AlarmLevel ?: 'deactivated'}".toString())
			break
	}
}

// SensorBinary and SensorAlarm aren't tested, but included to preemptively support future smoke alarms
def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd, results) {
	if (cmd.sensorType == physicalgraph.zwave.commandclasses.SensorBinaryV2.SENSOR_TYPE_SMOKE) {
		createSmokeEvents(cmd.sensorValue ? "smoke" : "smokeClear", results)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd, results) {
	if (cmd.sensorType == 1) {
		createSmokeEvents(cmd.sensorState ? "smoke" : "smokeClear", results)
	}
	
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd, results) {
	results << createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	if (!state.lastbatt || (now() - state.lastbatt) >= 56*60*60*1000) {
		results << response([
				zwave.batteryV1.batteryGet().format(),
				"delay 2000",
				zwave.wakeUpV1.wakeUpNoMoreInformation().format()
			])
	} else {
		results << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd, results) {
	def map = [ name: "battery", unit: "%", isStateChange: true ]
	state.lastbatt = now()
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}
	results << createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, results) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	state.sec = 1
	log.debug "encapsulated: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, results)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		results << createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd, results) {
	def event = [ displayed: false ]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: $cmd"
	results << createEvent(event)
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
	// check initial battery and smoke sensor state
	request << zwave.batteryV1.batteryGet()
	request << zwave.sensorBinaryV2.sensorBinaryGet(sensorType: zwave.sensorBinaryV2.SENSOR_TYPE_SMOKE)
	commands(request, 500) + ["delay 6000", command(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}
