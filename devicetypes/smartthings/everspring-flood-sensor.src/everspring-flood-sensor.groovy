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
	definition (name: "Everspring Flood Sensor", namespace: "smartthings", author: "SmartThings") {
		capability "Water Sensor"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"

		fingerprint deviceId: "0xA102", inClusters: "0x86,0x72,0x85,0x84,0x80,0x70,0x9C,0x20,0x71"
	}

	simulator {
		status "dry": "command: 9C02, payload: 00 05 00 00 00"
		status "wet": "command: 9C02, payload: 00 05 FF 00 00"
		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i).incomingMessage()
		}
	}
	
	tiles(scale: 2) {
		multiAttributeTile(name:"water", type: "generic", width: 6, height: 4){
			tileAttribute ("device.water", key: "PRIMARY_CONTROL") {
				attributeState "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
				attributeState "wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc"
			}
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main "water"
		details(["water", "battery", "configure"])
	}
}

def parse(String description) {
	def result = null
	def parsedZwEvent = zwave.parse(description, [0x9C: 1, 0x71: 1, 0x84: 2, 0x30: 1])
	if (parsedZwEvent) {
		result = zwaveEvent(parsedZwEvent)
	}
	log.debug "Parse '${description}' returned ${result}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	def now = new Date().time
	if (!state.battreq || now - state.battreq > 53*60*60*1000) {
		state.battreq = now
		result << response(zwave.batteryV1.batteryGet())
	} else {
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	def map = [:]
	if (cmd.sensorType == 0x05) {
		map.name = "water"
		map.value = cmd.sensorState ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"
	} else {
		map.descriptionText = "${device.displayName}: ${cmd}"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	def map = [:]
	map.name = "water"
	map.value = cmd.sensorValue ? "wet" : "dry"
	map.descriptionText = "${device.displayName} is ${map.value}"
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
	def map = [:]
	if (cmd.alarmType == 1 && cmd.alarmLevel == 0xFF) {
		map.name = "battery"
		map.value = 1
		map.unit = "%"
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
		map
	} else if (cmd.alarmType == 2 && cmd.alarmLevel == 1) {
		map.descriptionText = "${device.displayName} powered up"
		map.displayed = false
		map
	} else {
		map.descriptionText = "${device.displayName}: ${cmd}"
		map.displayed = false
	}
	createEvent(map)
}


def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	if (cmd.batteryLevel == 0xFF) {
		map.name = "battery"
		map.value = 1
		map.unit = "%"
		map.descriptionText = "${device.displayName} has a low battery"
		map.displayed = true
	} else {
		map.name = "battery"
		map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
		map.unit = "%"
		map.displayed = false
	}
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd)
{
	createEvent(descriptionText: "${device.displayName}: ${cmd}", displayed: false)
}

def configure()
{
	if (!device.currentState("battery")) {
		sendEvent(name: "battery", value:100, unit:"%", descriptionText:"(Default battery event)", displayed:false)
	}
	zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]).format()
}
