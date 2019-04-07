/**
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
 *  Enerwave Ceiling Mounted Motion Sensor
 *
 *  Author: erocm123 (Eric Maycock)
 *  Date: 2017-06-18
 */

metadata {
	definition (name: "Enerwave Ceiling Mounted Motion Sensor", namespace: "erocm123", author: "Eric Maycock", ocfDeviceType: "x.com.st.d.sensor.motion", vid:"generic-motion") {
		capability "Motion Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"

		fingerprint mfr: "011A", prod: "0601", model: "0901", deviceJoinName: "Enerwave Motion Sensor"
	}

	simulator {
	}
    
    preferences {
		input "parameter1", "enum", title: "Motion Inactivity Timeout", description: "Time (in seconds) that must elapse before the sensor reports inactivity", value:1, displayDuringSetup: false, options: [1:60, 2:120, 3:180, 4:240]
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
            tileAttribute ("battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label:'${currentValue}% battery'
			}
		}

		main "motion"
		details(["motion"])
	}
}

def installed() {
    log.debug "installed()"
// Device wakes up every 4 minutes (at most), this interval allows us to miss 4 wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 4 * 4 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    return response(commands(updateConfiguration()))
}

def updated() {
    log.debug "updated()"
// Device wakes up every 60 seconds, this interval allows us to miss ten wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 4 * 4 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def parse(String description) {
	def result = null
    log.debug description
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description)
	} else {
		def cmd = zwave.parse(description, [0x20: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1])
        log.debug cmd
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
	return result
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
     state."parameter${cmd.parameterNumber}" = cmd.configurationValue[0].toString()
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	def result = []
	if (cmd.notificationType == 0x07) {
		if (cmd.v1AlarmType == 0x07) {  // special case for nonstandard messages from Monoprice ensors
			result << sensorValueEvent(cmd.v1AlarmLevel)
		} else if (cmd.event == 0x01 || cmd.event == 0x02 || cmd.event == 0x07 || cmd.event == 0x08) {
			result << sensorValueEvent(1)
		} else if (cmd.event == 0x00) {
			result << sensorValueEvent(0)
		} else if (cmd.event == 0x03) {
			result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName covering was removed", isStateChange: true)
			result << response(zwave.batteryV1.batteryGet())
		} else if (cmd.event == 0x05 || cmd.event == 0x06) {
			result << createEvent(descriptionText: "$device.displayName detected glass breakage", isStateChange: true)
		}
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, isStateChange: true, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, isStateChange: true, displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpIntervalReport cmd)
{
	log.debug "WakeUpIntervalReport ${cmd.toString()}"
    state.wakeInterval = cmd.seconds
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    log.debug "Enerwave Motion Sensor Woke Up"
    def cmds = updateConfiguration()
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
    if (cmds) { 
        result += response(commands(updateConfiguration()) + "delay 3000")
    }
	result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
    result
}

def updateConfiguration() {
    def cmds = []
    if (state.parameter1 != (settings.parameter1? settings.parameter1 : "2")) {
        log.debug "Updating Configuration Parameter 1 to ${settings.parameter1? settings.parameter1.toInteger() : 2}"
        cmds << zwave.configurationV1.configurationSet(parameterNumber: 1, configurationValue:[(settings.parameter1? settings.parameter1.toInteger() : 2)])
        cmds << zwave.configurationV1.configurationGet(parameterNumber: 1)
    }
    if (state.wakeInterval != (settings.parameter1? settings.parameter1.toInteger() * 60 : 120)) {
        log.debug "Updating Wake Interval to ${settings.parameter1? settings.parameter1.toInteger() * 60 : 120}"
        cmds << zwave.wakeUpV1.wakeUpIntervalSet(seconds: settings.parameter1? settings.parameter1.toInteger() * 60 : 120, nodeid:zwaveHubNodeId)
        cmds << zwave.wakeUpV1.wakeUpIntervalGet()
    }
	if(!state.association1){
        log.debug "Setting Association Group 1"
        cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId)
        cmds << zwave.associationV2.associationGet(groupingIdentifier:1)
    }
    if (!state.lastbat || (new Date().time) - state.lastbat > 1000 * 60 * 60 * 6) {
        log.debug "Battery Report Not Received for 6 hours. Requesting One Now"
		cmds << zwave.batteryV1.batteryGet()
	}
    return cmds
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = new Date().time
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd)
{
	def map = [ displayed: true, value: cmd.scaledSensorValue.toString() ]
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			map.unit = cmd.scale == 1 ? "F" : "C"
			break;
		case 3:
			map.name = "illuminance"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = "lux"
			break;
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger().toString()
			map.unit = cmd.scale == 0 ? "%" : ""
			break;
		case 0x1E:
			map.name = "loudness"
			map.unit = cmd.scale == 1 ? "dBA" : "dB"
			break;
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    if (zwaveHubNodeId in cmd.nodeId) state."association${cmd.groupingIdentifier}" = true
    else state."association${cmd.groupingIdentifier}" = false
}

private command(physicalgraph.zwave.Command cmd) {
    
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=1000) {
	delayBetween(commands.collect{ command(it) }, delay)
}
