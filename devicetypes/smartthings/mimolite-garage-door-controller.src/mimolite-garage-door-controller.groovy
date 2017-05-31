/**
 *  MimoLite Garage Door Controller
 *
 *  Copyright 2014 Todd Wackford
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
 *  Note: This device type is based on the work of Jit Jack (cesaldar) as posted on the SmartThings Community website.
 *
 *  This device type file will configure a Fortrezz MimoLite Wireless Interface/Bridge Module as a Garage Door 
 *  Controller. The Garage Door must be physically configured per the following diagram: 
 *    "http://www.fortrezz.com/index.php/component/jdownloads/finish/4/17?Itemid=0"
 *  for all functionality to work correctly.
 *
 *  This device type will also set the atttibute "powered" to "powerOn" or "powerOff" accordingly. This uses
 *  the alarm capability of the MimoLite and the status will be displayed to the user on a secondary tile. User
 *  can subscribe to the status of this atttribute to be notified when power drops out.
 *
 *  This device type implements a "Configure" action tile which will set the momentary switch timeout to 25ms and
 *  turn on the powerout alarm.
 *
 *  
 */
metadata {
	// Automatically generated. Make future change here.
	definition (name: "MimoLite Garage Door Controller", namespace: "smartthings", author: "Todd Wackford") {
		capability "Configuration"
		capability "Polling"
		capability "Switch"
		capability "Refresh"
		capability "Contact Sensor"
		capability "Light"

		attribute "powered", "string"

		command "on"
		command "off"
        
        fingerprint deviceId: "0x1000", inClusters: "0x72,0x86,0x71,0x30,0x31,0x35,0x70,0x85,0x25,0x03"
	}

	simulator {
	// Simulator stuff
    
	}

	// UI tile definitions 
	tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "doorClosed", label: "Closed", action: "on", icon: "st.doors.garage.garage-closed", backgroundColor: "#00A0DC"
            state "doorOpen", label: "Open", action: "on", icon: "st.doors.garage.garage-open", backgroundColor: "#e86d13"
            state "doorOpening", label: "Opening", action: "on", icon: "st.doors.garage.garage-opening", backgroundColor: "#e86d13"
            state "doorClosing", label: "Closing", action: "on", icon: "st.doors.garage.garage-closing", backgroundColor: "#00A0DC"
            state "on", label: "Actuate", action: "off", icon: "st.doors.garage.garage-closed", backgroundColor: "#00A0DC"
			state "off", label: '${name}', action: "on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        }
        standardTile("contact", "device.contact", inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
		}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        standardTile("powered", "device.powered", inactiveLabel: false) {
			state "powerOn", label: "Power On", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "powerOff", label: "Power Off", icon: "st.switches.switch.off", backgroundColor: "#ffa81e"
		}
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main (["switch", "contact"])
		details(["switch", "powered", "refresh", "configure"])
	}
}

def parse(String description) {
log.debug "description is: ${description}"

	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1])
    
    log.debug "command value is: $cmd.CMD"
    
    if (cmd.CMD == "7105") {				//Mimo sent a power loss report
    	log.debug "Device lost power"
    	sendEvent(name: "powered", value: "powerOff", descriptionText: "$device.displayName lost power")
    } else {
    	sendEvent(name: "powered", value: "powerOn", descriptionText: "$device.displayName regained power")
    }
    
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def sensorValueEvent(Short value) {
	if (value) {
        sendEvent(name: "contact", value: "open")
        sendEvent(name: "switch", value: "doorOpen")
	} else {
        sendEvent(name: "contact", value: "closed")
        sendEvent(name: "switch", value: "doorClosed")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "on" : "off", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def doorState = device.currentValue('contact')
    if ( doorState == "closed")
		[name: "switch", value: cmd.value ? "on" : "doorOpening", type: "digital"]
    else
    	[name: "switch", value: cmd.value ? "on" : "doorClosing", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv1.AlarmReport cmd)
{
    log.debug "We lost power" //we caught this up in the parse method. This method not used.
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def configure() {
	log.debug "Configuring...." //setting up to monitor power alarm and actuator duration
	delayBetween([
		zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format(),
        zwave.configurationV1.configurationSet(configurationValue: [25], parameterNumber: 11, size: 1).format(),
        zwave.configurationV1.configurationGet(parameterNumber: 11).format()
	])
}

def on() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def off() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	])
}

def poll() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

def refresh() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}
