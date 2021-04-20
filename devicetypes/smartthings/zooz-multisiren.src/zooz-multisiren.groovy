/**
 *	Copyright 2019 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 *	Aeon Siren
 *
 *	Author: SmartThings
 *	Date: 2019-03-18
 */
 
metadata {
 definition (name: "Zooz Multisiren", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren", vid: "generic-siren-11") {
	capability "Actuator"
	capability "Alarm"
	capability "Switch"
	capability "Health Check"
	capability "Temperature Measurement"
	capability "Relative Humidity Measurement"
	capability "Battery"
	capability "Tamper Alert"
	capability "Refresh"
	capability "Configuration"

	fingerprint mfr: "027A", prod: "000C", model: "0003", deviceJoinName: "Zooz Siren" //Zooz S2 Multisiren ZSE19
	fingerprint mfr: "0060", prod: "000C", model: "0003", deviceJoinName: "Everspring Siren" //Everspring Indoor Voice Siren

}

tiles(scale: 2) {
	multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4) {
		tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
			attributeState "off", label:'off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
			attributeState "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
		}
	}
	
	valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
		 state "temperature", label:'${currentValue}°',
			 backgroundColors:[
				 [value: 32, color: "#153591"],
				 [value: 44, color: "#1e9cbb"],
				 [value: 59, color: "#90d2a7"],
				 [value: 74, color: "#44b621"],
				 [value: 84, color: "#f1d801"],
				 [value: 92, color: "#d04e00"],
				 [value: 98, color: "#bc2323"]
			 ]
	 }
		
	valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
		 state "humidity", label:'${currentValue}% humidity', unit:""
	}
	
	valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		 state "battery", label:'${currentValue}% battery', unit:""
	}
	
	standardTile("refresh", "command.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
	}

	valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {
		state "clear", label: 'tamper clear', backgroundColor: "#ffffff"
		state "detected", label: 'tampered', backgroundColor: "#ff0000"
	}

	main "alarm"
	details(["alarm", "humidity", "battery", "temperature", "tamper", "refresh"])
	
	}
}

def installed() {
	runIn(2, "initialize", [overwrite: true])
}

def refresh() {
	def commands = []
	//get temperature value
	commands << secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01))
	//get humidity value
	commands << secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05))
	//get tamper state
	commands << secure(zwave.notificationV3.notificationGet(notificationType: 0x07))
	//get state of device (on or off)
	commands << secure(zwave.basicV1.basicGet())
	//get battery value
	commands << secure(zwave.batteryV1.batteryGet())
	
	commands
}

def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 10 * 60, displayed: true, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	def cmd = []
	//temperature and humidity are set for reporting every 60 min
	cmd << secure(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 2, configurationValue: [60]))
	cmd << refresh()

	sendHubCommand(cmd.flatten(), 2000)
}

def configure() {
	runIn(2, "initialize", [overwrite: true])
}

def parse(String description) {
	def result = null

	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	createEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	createEvents(cmd)
}

private createEvents(cmd) {
	[
		createEvent([name: "switch", value: cmd.value ? "on" : "off"]),
		createEvent([name: "alarm", value: cmd.value ? "both" : "off"])
	]
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def events = []
	//when opening device cover
	if(cmd.notificationType == 7) {
		if(cmd.event == 3) {
			events << createEvent([name: "tamper", value: "detected"])
		} else {
			events << createEvent([name: "tamper", value: "clear"])
		}
	}
	
	events
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def events = []
	
	if(cmd.sensorType == 1) {
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		events << createEvent([name: "temperature", value: convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), unit: getTemperatureScale()])
	} else if(cmd.sensorType == 5) {
		events << createEvent([name: "humidity", value: cmd.scaledSensorValue])
	}
	
	events
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	
	map.name = "battery"
	map.unit = "%"
	
	if(cmd.batteryLevel == 0xFF){
		map.value = 1
	} else {
		map.value = cmd.batteryLevel
	}

	createEvent(map)
}	
	
def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[:]
}

def on() {
	def commands = []
	commands << secure(zwave.basicV1.basicSet(value: 0xFF))
	commands << secure(zwave.basicV1.basicGet())
	
	delayBetween(commands, 100)
}

def off() {
	def commands = []
	commands << secure(zwave.basicV1.basicSet(value: 0x00))
	commands << secure(zwave.basicV1.basicGet())
	
	delayBetween(commands, 100)
}

def strobe() {
	on()
}

def siren() {
	on()
}

def both() {
	on()
}

def ping() {
	def commands = []
	commands << secure(zwave.basicV1.basicGet())
}

private secure(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}
