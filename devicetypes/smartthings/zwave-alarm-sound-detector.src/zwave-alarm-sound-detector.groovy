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
	definition (name: "Z-Wave Alarm Sound Detector", namespace: "smartthings", author: "SmartThings", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Sound Sensor"
    		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		attribute "alarmState", "string"

		fingerprint mfr:"014A", prod:"0005", model:"000F", deviceJoinName: "Ecolink Firefighter"
	}

	tiles (scale: 2){
		multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
			tileAttribute ("device.alarmState", key: "PRIMARY_CONTROL") {
				attributeState("clear", label:"clear", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
				attributeState("smoke", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
				attributeState("carbonMonoxide", label:"MONOXIDE", icon:"st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor:"#e86d13")
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
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	def cmds = []
	createSmokeOrCOEvents("allClear", cmds)
	cmds.each { cmd -> sendEvent(cmd) }
}

def updated() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
	    results << createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [ 0x80: 1, 0x84: 1, 0x71: 2, 0x72: 1 ])
		if (cmd) {
			zwaveEvent(cmd, results)
		}
	}
	log.debug "'$description' parsed to ${results.inspect()}"
	return results
}

def createSmokeOrCOEvents(name, results) {
	def text = null
	switch (name) {
		case "smoke":
			text = "$device.displayName smoke was detected!"
			results << createEvent(name: "smoke",          value: "detected", descriptionText: text, displayed: false)
			break
		case "carbonMonoxide":
			text = "$device.displayName carbon monoxide was detected!"
			results << createEvent(name: "carbonMonoxide", value: "detected", descriptionText: text, displayed: false)
			break
		case "smokeClear":
			text = "$device.displayName smoke is clear"
			results << createEvent(name: "smoke",          value: "clear", descriptionText: text, displayed: false)
			name = "clear"
			break
		case "carbonMonoxideClear":
			text = "$device.displayName carbon monoxide is clear"
			results << createEvent(name: "carbonMonoxide", value: "clear", descriptionText: text, displayed: false)
			name = "clear"
			break
		case "allClear":
			text = "$device.displayName all clear"
			results << createEvent(name: "smoke",          value: "clear", descriptionText: text, displayed: false)
			results << createEvent(name: "carbonMonoxide", value: "clear", displayed: false)
			name = "clear"
			break
	}
	results << createEvent(name: "alarmState", value: name, descriptionText: text)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd, results) {
	log.debug "zwaveAlarmType: ${cmd.zwaveAlarmType}"
	if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_SMOKE) {
		createSmokeOrCOEvents((cmd.zwaveAlarmEvent == 1 || cmd.zwaveAlarmEvent == 2) ? "smoke" : "smokeClear", results)
	} else if (cmd.zwaveAlarmType == physicalgraph.zwave.commands.alarmv2.AlarmReport.ZWAVE_ALARM_TYPE_CO) {
		createSmokeOrCOEvents((cmd.zwaveAlarmEvent == 1 || cmd.zwaveAlarmEvent == 2) ? "carbonMonoxide" : "carbonMonoxideClear", results)
	} else {
		results << createEvent(displayed: true, descriptionText: "Alarm $cmd.alarmType ${cmd.alarmLevel == 255 ? 'activated' : cmd.alarmLevel ?: 'deactivated'}".toString())
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
	log.debug "Battery"
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

def zwaveEvent(physicalgraph.zwave.Command cmd, results) {
	def event = [ displayed: false ]
	event.linkText = device.label ?: device.name
	event.descriptionText = "$event.linkText: $cmd"
	results << createEvent(event)
}
