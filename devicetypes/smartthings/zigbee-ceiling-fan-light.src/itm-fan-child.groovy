/*
 *  Copyright 2021 SmartThings
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
 *  ZigBee Ceiling Fan Light
 *
 *  Author: SAMSUNG LED
 *  Date: 2021-06-30
 */

import physicalgraph.zigbee.zcl.DataType
import groovy.json.JsonOutput

metadata {
	definition(name: "ITM Fan Child", namespace: "SAMSUNG LED", author: "SAMSUNG LED", ocfDeviceType: "oic.d.fan") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Switch"
		/* Capability "Switch Level" is used to control fan speed for platforms don't support capability "Fan speed"
		 * when you connect other platforms via SmartThings cloud to cloud connection. */
		capability "Switch Level"
		capability "Fan Speed"
	}
}

def off() {
	setFanSpeed(0x00)
}

def on() {
	setFanSpeed(0x01)
}

def setLevel(value) {
	if (value <= 1) {
		setFanSpeed(0x00)
	} else if (value <= 25) {
		setFanSpeed(0x01)
	} else if (value <= 50) {
		setFanSpeed(0x02)
	} else if (value <= 75) {
		setFanSpeed(0x03)
	} else if (value <= 100) {
		setFanSpeed(0x04)
	}	
}

def setFanSpeed(speed) {
	parent.sendFanSpeed(speed)
}

void refresh() {
	parent.refresh()	
}

def ping() {
	parent.ping()
}
