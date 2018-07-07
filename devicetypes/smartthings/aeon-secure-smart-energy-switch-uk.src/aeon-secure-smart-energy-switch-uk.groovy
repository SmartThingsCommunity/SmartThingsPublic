// This device file is based on work previous work done by "Mike '@jabbera'"

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
 */
metadata {
	definition (name: "Aeon Secure Smart Energy Switch UK", namespace: "smartthings", author: "jabbera") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Configuration"

		command "reset"
        command "configureAfterSecure"

        fingerprint deviceId: "0x1001", inClusters: "0x25,0x32,0x27,0x2C,0x2B,0x70,0x85,0x56,0x72,0x86,0x98", outClusters: "0x82"
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		valueTile("power", "device.power", decoration: "flat") {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat") {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("configureAfterSecure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configureAfterSecure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","power","energy","reset","configureAfterSecure","refresh"])
	}
}

def parse(String description) {
	def result = null
    
    if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x32: 1, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x72: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
    }
    log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

// Devices that support the Security command class can send messages in an encrypted form;
// they arrive wrapped in a SecurityMessageEncapsulation command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x32: 1, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1]) // can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	def newEvent = null
	if (cmd.scale == 0) {
		newEvent = [name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
	} else if (cmd.scale == 1) {
		newEvent = [name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
	} else {
		newEvent = [name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
	}

	createEvent(newEvent)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	createEvent([
		name: "switch", value: cmd.value ? "on" : "off", type: "physical"
	])
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	createEvent([
		name: "switch", value: cmd.value ? "on" : "off", type: "digital"
	])
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "No handler for $cmd"
	// Handles all Z-Wave commands we aren't interested in
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}

def on() {
	secureSequence([
			zwave.basicV1.basicSet(value: 0xFF),
			zwave.switchBinaryV1.switchBinaryGet()
	])
}

def off() {
	secureSequence([
			zwave.basicV1.basicSet(value: 0x00),
			zwave.switchBinaryV1.switchBinaryGet()
	])
}

def poll() {
	secureSequence([
		zwave.switchBinaryV1.switchBinaryGet(),
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def refresh() {
	secureSequence([
		zwave.switchBinaryV1.switchBinaryGet(),
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def reset() {
	return secureSequence([
		zwave.meterV2.meterReset(),
		zwave.meterV2.meterGet(scale: 0)
	])
}

def configureAfterSecure() {
    log.debug "configureAfterSecure()"

	secureSequence([
		zwave.configurationV1.configurationSet(parameterNumber: 252, size: 1, scaledConfigurationValue: 0),	// Enable/disable Configuration Locked (0 =disable, 1 = enable).
		zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, scaledConfigurationValue: 2),	// Enable to send notifications to associated devices (Group 1) when the state of Micro Switch’s load changed (0=nothing, 1=hail CC, 2=basic CC report).
		zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: 1),	// Enables/disables parameter 91 and 92 below (1=enabled, 0=disabled).
		zwave.configurationV1.configurationSet(parameterNumber: 91, size: 2, scaledConfigurationValue: 2),	// The value here represents minimum change in wattage (in terms of wattage) for a REPORT to be sent (Valid values 0‐ 60000).
		zwave.configurationV1.configurationSet(parameterNumber: 92, size: 1, scaledConfigurationValue: 5),	// The value here represents minimum change in wattage percent (in terms of percentage) for a REPORT to be sent (Valid values 0‐100).
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 4),	// Which reports need to send in Report group 1 (See flags in table below).
		// Disable a time interval to receive immediate updates of power change.
		//zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300),	// The time interval of sending Report group 1 (Valid values 0x01‐0xFFFFFFFF).
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 8),	// Which reports need to send in Report group 2 (See flags in table below).
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 300),	// The time interval of sending Report group 2 (Valid values 0x01‐0xFFFFFFFF).
		zwave.configurationV1.configurationSet(parameterNumber: 252, size: 1, scaledConfigurationValue: 1),	// Enable/disable Configuration Locked (0 =disable, 1 = enable).

		// Register for Group 1
        zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId]),
        // Register for Group 2
        zwave.associationV2.associationSet(groupingIdentifier:2, nodeId: [zwaveHubNodeId])
	])
}

def configure() {
	// Wait until after the secure exchange for this
    log.debug "configure()"
}

def updated() {
	log.debug "updated()"
	response(["delay 2000"] + configureAfterSecure() + refresh())
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}
