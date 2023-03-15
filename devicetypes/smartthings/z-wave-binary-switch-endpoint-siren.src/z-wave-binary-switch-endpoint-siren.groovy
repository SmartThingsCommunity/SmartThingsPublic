/**
 *	Copyright 2018 SmartThings
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
 */
metadata {
	definition(name: "Z-Wave Binary Switch Endpoint Siren", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "SmartThings-smartthings-Z-Wave_Siren", ocfDeviceType: "x.com.st.d.siren") {
		capability "Actuator"
		capability "Health Check"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Alarm"
	}

	tiles {
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "off", label: 'off', action: 'alarm.strobe', icon: "st.alarm.alarm.alarm", backgroundColor: "#ffffff"
			state "both", label: 'alarm!', action: 'alarm.off', icon: "st.alarm.alarm.alarm", backgroundColor: "#e86d13"
		}
		standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "alarm.off", icon: "st.secondary.off"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", label: '', action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "alarm"
		details(["alarm", "off", "refresh"])
	}
}

def installed() {
	configure()
	sendEvent(name: "alarm", value: "off", isStateChange: true)

}

def updated() {
	configure()
}

def configure() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	response(refresh())
}

def handleZWave(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	sendAlarmAndSwitchEvents(cmd)
}

def handleZWave(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sendAlarmAndSwitchEvents(cmd)
}

def handleZWave(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	sendAlarmAndSwitchEvents(cmd)
}

def sendAlarmAndSwitchEvents(physicalgraph.zwave.Command cmd) {
	sendEvent(name: "alarm", value: cmd.value ? "both" : "off")
	sendEvent(name: "switch", value: cmd.value ? "on" : "off")
}

def handleZWave(physicalgraph.zwave.Command cmd) {
	[:]
}

def on() {
	//Endpoint no. 2 is double short beep. Second report is needed to change button display to current state "OFF" 
	if (parent.channelNumber(device.deviceNetworkId) == 2) {
		parent.sendCommand(device.deviceNetworkId, [zwave.basicV1.basicSet(value: 0xFF), zwave.switchBinaryV1.switchBinaryGet(),"delay 2000", zwave.switchBinaryV1.switchBinaryGet()])
	} else {
		parent.sendCommand(device.deviceNetworkId, [zwave.basicV1.basicSet(value: 0xFF), zwave.switchBinaryV1.switchBinaryGet()])
	}
}

def off() {
	parent.sendCommand(device.deviceNetworkId, [zwave.basicV1.basicSet(value: 0), zwave.switchBinaryV1.switchBinaryGet()])
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
	refresh()
}

def refresh() {
	parent.sendCommand(device.deviceNetworkId, zwave.switchBinaryV1.switchBinaryGet())
}
