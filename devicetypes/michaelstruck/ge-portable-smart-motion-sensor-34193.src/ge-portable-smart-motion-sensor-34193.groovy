/**
 *  GE Portable Smart Motion Sensor (Model 34193) DTH
 *
 *  Copyright © 2018 Michael Struck
 *  Original Author: Jeremy Williams (@jwillaz)
 *
 *  Version 1.0.1 9/29/18 
 *
 *  Version 1.0.0 (11/17/17)- Original release by Jeremy Williams . Great Work!
 *  Version 1.0.1 (9/29/18)- Updated by Michael Struck-Streamlined interface, added additional logging
 *
 *  Uses code from SmartThings
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
	definition (name: "GE Portable Smart Motion Sensor 34193", namespace: "MichaelStruck", author: "Michael Struck", ocfDeviceType: "x.com.st.d.sensor.motion") {
		capability "Motion Sensor"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
        
		fingerprint mfr: "0063", prod: "4953", model: "3133", deviceJoinName: "GE Portable Smart Motion Sensor"
	}
	simulator {
		status "inactive": "command: 3003, payload: 00"
		status "active": "command: 3003, payload: FF"
	}
	preferences {
        input "parameterThirteen", "enum", title: "Motion Sensitivity (Default: 'High')", options: [1:"Low", 2:"Medium", 3:"High"],  defaultValue: 3, required: false
        input "parameterEighteen", "number", title: "Motion Timeout Duration (1 to 60 minutes or 255 = 5 seconds) Default: 4 minutes",  defaultValue: 4, required: false
		input "parameterTwentyEight", "bool", title: "Enable LED Flash Indicator", defaultValue: true
    }
	tiles(scale: 2) {
		multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute("device.motion", key: "PRIMARY_CONTROL") {
				attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")
				attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")
			}
            tileAttribute ("battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label:'Battery: ${currentValue}%'
			}
		}
		main "motion"
		details(["motion"])
	}
}
def installed() {
// Device wakes up every 4 hours, this interval allows us to miss one wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}
def updated() {
	//Device wakes up every 4 hours, this interval allows us to miss one wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])   
    response(configure())
}
private getCommandClassVersions() {
	[0x20: 1, 0x30: 1, 0x31: 5, 0x80: 1, 0x84: 1, 0x71: 3, 0x9C: 1]
}
def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
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
def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]

	if (state.MSR == "011A-0601-0901" && device.currentState('motion') == null) {  // Enerwave motion doesn't always get the associationSet that the hub sends on join
		result << response(zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId))
	}
	if (!state.lastbat || (new Date().time) - state.lastbat > 53*60*60*1000) {
		result << response(zwave.batteryV1.batteryGet())
	} else {
		result << response(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result
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
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
	// def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	def version = commandClassVersions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	}
}
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def result = null
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	log.debug "Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}"
	if (encapsulatedCommand) {
		result = zwaveEvent(encapsulatedCommand)
	}
	result
}
def zwaveEvent(physicalgraph.zwave.commands.multicmdv1.MultiCmdEncap cmd) {
	log.debug "MultiCmd with $numberOfCommands inner commands"
	cmd.encapsulatedCommands(commandClassVersions).collect { encapsulatedCommand ->
		zwaveEvent(encapsulatedCommand)
	}.flatten()
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
def configure() { 
    def param13 = parameterThirteen ? parameterThirteen as int : 3    
    def param18 = !parameterEighteen ? 4 : parameterEighteen as int 
    def param28 = parameterTwentyEight ? 1 : 0
    log.info "Sending parameters to device..."
    log.info "Sending parameter 13, motion sensitivity: ${param13==1 ? "1=Low" : param13==2 ? "2=Medium" : "3=High"}"
    if (param18 < 1 || param18 >255 || (param18 > 60 && param18 != 255)) {
    	param18 = 255
        log.warn "Invalid motion timeout. Using 255 (5 seconds)"
    }
    def timing = param18==1 ? "Minute" : "Minutes"
    log.info "Sending parameter 18, Motion timeout: ${param18==255 ? " 5 Seconds" : param18 + " ${timing}"}"
    log.info "Sending parameter 28, LED flash indicator: " + param28 + "${param28==1 ? "=On" : "=Off"}"
    delayBetween([
        zwave.configurationV1.configurationSet(configurationValue: [param13], parameterNumber: 13, size: 1).format(),
        zwave.configurationV1.configurationSet(configurationValue: [param18], parameterNumber: 18, size: 1).format(),
        zwave.configurationV1.configurationSet(configurationValue: [param28], parameterNumber: 28, size: 1).format()
    ], 500)
}