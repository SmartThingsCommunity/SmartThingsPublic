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
 *  SmartAlert Siren
 *
 *  Author: SmartThings
 *  Date: 2013-03-05
 */
metadata {
	definition (name: "SmartAlert Siren", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.siren", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false) {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
		capability "Alarm"
		capability "Health Check"

		command "test"

		fingerprint deviceId: "0x1100", inClusters: "0x26,0x71"
		fingerprint mfr:"0084", prod:"0313", model:"010B", deviceJoinName: "FortrezZ Siren Strobe Alarm"
	}

	simulator {
		// reply messages
		reply "2001FF,2002": "command: 2003, payload: FF"
		reply "200100,2002": "command: 2003, payload: 00"
		reply "200121,2002": "command: 2003, payload: 21"
		reply "200142,2002": "command: 2003, payload: 42"
		reply "2001FF,delay 3000,200100,2002": "command: 2003, payload: 00"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"alarm", type: "generic", width: 6, height: 4){
			tileAttribute ("device.alarm", key: "PRIMARY_CONTROL") {
				attributeState "off", label:'off', action:'alarm.strobe', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
				attributeState "strobe", label:'strobe!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
				attributeState "siren", label:'siren!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
				attributeState "both", label:'alarm!', action:'alarm.off', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
			}
		}
		standardTile("strobe", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "off", label:'', action:"alarm.strobe", icon:"st.secondary.strobe", backgroundColor:"#cccccc"
			state "siren", label:'', action:"alarm.strobe", icon:"st.secondary.strobe", backgroundColor:"#cccccc"
			state "strobe", label:'', action:'alarm.strobe', icon:"st.secondary.strobe", backgroundColor:"#e86d13"
			state "both", label:'', action:'alarm.strobe', icon:"st.secondary.strobe", backgroundColor:"#e86d13"
		}
		standardTile("siren", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "off", label:'', action:"alarm.siren", icon:"st.secondary.siren", backgroundColor:"#cccccc"
			state "strobe", label:'', action:"alarm.siren", icon:"st.secondary.siren", backgroundColor:"#cccccc"
			state "siren", label:'', action:'alarm.siren', icon:"st.secondary.siren", backgroundColor:"#e86d13"
			state "both", label:'', action:'alarm.siren', icon:"st.secondary.siren", backgroundColor:"#e86d13"
		}
		standardTile("test", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"test", icon:"st.secondary.test"
		}
		standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"alarm.off", icon:"st.secondary.off"
		}
		main "alarm"
		details(["alarm","strobe","siren","test","off"])
	}
}

def installed() {
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def updated() {
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def on() {
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.basicV1.basicGet().format()
	]
}

def off() {
	[
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet().format()
	]
}

def test() {
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		"delay 3000",
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.basicV1.basicGet().format()
	]
}

def strobe() {
	[
		zwave.basicV1.basicSet(value: 0x21).format(),
		zwave.basicV1.basicGet().format()
	]
}

def siren() {
	[
		zwave.basicV1.basicSet(value: 0x42).format(),
		zwave.basicV1.basicGet().format()
	]
}

def both() {
	[
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.basicV1.basicGet().format()
	]
}


def parse(String description) {
	log.debug "parse($description)"
	def result = null
	def cmd = zwave.parse(description, [0x20: 1])
	if (cmd) {
		result = createEvents(cmd)
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def createEvents(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	def switchValue = cmd.value ? "on" : "off"
	def alarmValue
	if (cmd.value == 0) {
		alarmValue = "off"
	}
	else if (cmd.value <= 33) {
		alarmValue = "strobe"
	}
	else if (cmd.value <= 66) {
		alarmValue = "siren"
	}
	else {
		alarmValue = "both"
	}
	[
		createEvent([name: "switch", value: switchValue, type: "digital", displayed: false]),
		createEvent([name: "alarm", value: alarmValue, type: "digital"])
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "UNEXPECTED COMMAND: $cmd"
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	secure(zwave.basicV1.basicGet())
}