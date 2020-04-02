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
	definition (name: "Z-Wave Sound Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren") {
		capability "Sound Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		fingerprint mfr: "014A", prod: "0005", model: "000F", deviceJoinName: "Ecolink Sound Sensor" //Ecolink Firefighter
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"sound", type: "lighting", width: 6, height: 4) {
			tileAttribute ("device.sound", key: "PRIMARY_CONTROL") {
				attributeState("not detected", label:'${name}', icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
				attributeState("detected", label:'${name}', icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
			}
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "sound"
		details(["sound", "battery"])
	}
}

def installed() {
	sendCheckIntervalEvent()
	sendEvent(name: "sound", value: "not detected", displayed: false, isStateChanged: true)
	response([
			zwave.batteryV1.batteryGet().format(),
			"delay 2000",
			zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	])
}

def updated() {
	sendCheckIntervalEvent()
}

def parse(String description) {
	def results = []
	if (description.startsWith("Err")) {
		results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description, [ 0x80: 1, 0x84: 1, 0x71: 2, 0x72: 1 ])
		if (cmd) {
			results = zwaveEvent(cmd)
		}
	}
	log.debug "'$description' parsed to ${results.inspect()}"
	results
}

private getALARM_TYPE_SMOKE() { 1 }
private getALARM_TYPE_CO() { 2 }

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	log.debug "zwaveAlarmTypes: ${cmd.zwaveAlarmType}"
	def event = null
	if (cmd.zwaveAlarmType == ALARM_TYPE_SMOKE || cmd.zwaveAlarmType == ALARM_TYPE_CO) {
		def value = (cmd.zwaveAlarmEvent == 1 || cmd.zwaveAlarmEvent == 2) ? "detected" : "not detected"
		event = createEvent(name: "sound", value: value, descriptionText: "${device.displayName} sound was ${value}", isStateChanged: true)
	} else {
		event = createEvent(displayed: true, descriptionText: "Alarm $cmd.alarmType ${cmd.alarmLevel == 255 ? 'activated' : cmd.alarmLevel ?: 'deactivated'}".toString())
	}
	event
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def cmds = []
	cmds << createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)
	if (!state.lastbatt || (now() - state.lastbatt) >= 56*60*60*1000) {
		cmds << response([
				zwave.batteryV1.batteryGet().format(),
				"delay 2000",
				zwave.wakeUpV1.wakeUpNoMoreInformation().format()
		])
	} else {
		cmds << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())
	}
	cmds
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%", isStateChange: true ]
	state.lastbatt = now()
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
	[:]
}

private sendCheckIntervalEvent() {
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}