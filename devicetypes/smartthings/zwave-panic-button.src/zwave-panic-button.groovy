/**
 *  Copyright 2018 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition(name: "Z-Wave Panic Button", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.remotecontroller") {
		capability "Sensor"
		capability "Health Check"
		capability "Battery"
		capability "Panic Alarm"

		fingerprint mfr: "0258", prod: "0003", model: "108A", deviceJoinName: "NEO SOS"
		fingerprint mfr: "0258", prod: "0003", model: "108F", deviceJoinName: "NEO SOS"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "panic", type: "generic", width: 6, height: 4) {
			tileAttribute("device.panicAlarm", key: "PRIMARY_CONTROL") {
				attributeState "panic", label: "Emergency!", icon:"st.alarm.beep.beep", backgroundColor:"#bc2f2f"
				attributeState "clear", label: "", icon:"st.Weather.weather13", backgroundColor:"#a4a9aa"
			}
		}

		childDeviceTiles("buttons")

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		main(["panic"])

		details(["panic", "buttons", "battery"])
	}
}

def installed() {
	log.debug "Installed $device.displayName"
	//sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "zwave", scheme:"untracked"].encodeAsJson(), displayed: false)
	if(hasMultipleButtons()) {
		addChildButtons()
	}
	def cmds = [
			zwave.batteryV1.batteryGet().format(),
			zwave.notificationV3.notificationGet().format(),
			"delay 1000",
			zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	]
	sendEvent(name: "panicAlarm", value: "clear", descriptionText: "Panic Alarm status: clear", displayed: true, isStateChange: true)
	response(cmds)
}

def parse(String description) {
	def result
	def cmd = zwave.parse(description)

	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug "Parse ${description} to ${result}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	def button = cmd.sceneNumber
	def state = (cmd.keyAttributes == 3) ? "double" : "pushed"
	setButtonState(button, state)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def status = cmd.event ? "panic" : "clear"
	setPanicAlarm(status)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	def status = cmd.sensorValue ? "panic" : "clear"
	setPanicAlarm(status)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]

	if (!state.lastbatt || (now() - state.lastbatt) >= 56*60*60*1000) {
		results += response(zwave.batteryV1.batteryGet().format())
	}
	results += response([
			"delay 1000",
			zwave.wakeUpV1.wakeUpNoMoreInformation().format()
	])
	results
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled zwave command $cmd"
	[:]
}

private setPanicAlarm(status) {
	createEvent(name: "panicAlarm", value: status, descriptionText: "Panic Alarm status: ${status}", displayed: true, isStateChange: true)
}

private setButtonState(buttonId, state) {
	def event
	String childDni = "${device.deviceNetworkId}:$buttonId"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	if (!child) {
		log.error "Child device $childDni not found"
	} else {
		child?.sendEvent(name: "button", value: "$state")
		event = createEvent(descriptionText: "Button $buttonId was $state", isStateChange: true)
	}
	event
}

private addChildButtons() {
	log.debug "Creating child devices"
	for (i in 1..3) {
		String childDni = "${device.deviceNetworkId}:$i"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		addChildDevice("Child Button", childDni, null, [
				completedSetup: true,
				label         : "$device.displayName Button $i",
				isComponent   : true,
				componentName : "button$i",
				componentLabel: "Button $i"
		])
	}
}

private boolean hasMultipleButtons() {
	zwaveInfo.model?.contains("108A")
}