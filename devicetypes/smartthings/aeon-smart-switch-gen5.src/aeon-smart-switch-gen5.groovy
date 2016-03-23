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
	definition (name: "Aeon Smart Switch GEN5", namespace: "smartthings", author: "SmartThings") {
		capability "Power Meter"
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Configuration"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        
        attribute "ManufacturerCode", "string"
		attribute "ProductCode", "string"
		attribute "ProduceTypeCode", "string"
		attribute "WirelessConfig", "string"


		command "reset"
        

		fingerprint deviceId: "0x1001", inClusters: "0x25,0x32,0x27,0x2C,0x2B,0x70,0x85,0x56,0x72,0x86,0x5E,0x59,0x7A,0x73,0x98,0xEF,0x5A,0x82", outClusters: "0x82"
	}

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV2.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

		// reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

	}

	// tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		valueTile("power", "device.power", decoration: "flat") {
			state "default", label:'${currentValue} W'
		} 
		valueTile("energy", "device.energy", decoration: "flat") {
			state "default", label:'${currentValue} kWh'
		}        
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","power","energy","refresh","configure"])
	}
}

def parse(String description) {

	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x32: 2])
	if (cmd) {
		log.debug cmd
		result = createEvent(zwaveEvent(cmd))
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
		log.debug "MeterReport ${cmd}"

	if (cmd.scale == 0) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]
	} else if (cmd.scale == 1) {
		[name: "energy", value: cmd.scaledMeterValue, unit: "kVAh"]
	}
	else {
		[name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W"]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	[
		name: "switch", value: cmd.value ? "on" : "off", type: "physical"
	]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	[
		name: "switch", value: cmd.value ? "on" : "off", type: "digital"
	]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
   	return createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
	[:]
}

def on() {
	delayBetween([
		secure(zwave.basicV1.basicSet(value: 0xFF)),
		secure(zwave.switchBinaryV1.switchBinaryGet())
	])
}

def off() {
	delayBetween([
		secure(zwave.basicV1.basicSet(value: 0x00)),
		secure(zwave.switchBinaryV1.switchBinaryGet())
	])
}

def poll() {
	delayBetween([
		secure(zwave.switchBinaryV1.switchBinaryGet()),
		secure(zwave.meterV2.meterGet())
	])
}

def refresh() {
	secure(zwave.switchBinaryV1.switchBinaryGet())  
}

def reset() {
	return [
		secure(zwave.meterV2.meterReset()),
		secure(zwave.meterV2.meterGet())
	]
}

def configure() {
	delayBetween([
    	zwave.manufacturerSpecificV2.manufacturerSpecificGet().format(),    
		secure(zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 8)),   // energy in kWh
		secure(zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 300)), // every 1 min
		secure(zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 4)),
		secure(zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 900)),
		secure(zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: 0))
	])
}


def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def manufacturerCode = String.format("%04X", cmd.manufacturerId)
	def productCode = String.format("%04X", cmd.productId)
	def produceTypeCode = String.format("%04X", cmd.productTypeId)
	def wirelessConfig = "ZWP"
	
	sendEvent(name: "ManufacturerCode", value: manufacturerCode)
	sendEvent(name: "ProductCode", value: productCode)
	sendEvent(name: "ProduceTypeCode", value: produceTypeCode)
	sendEvent(name: "WirelessConfig", value: wirelessConfig)
	
	result << createEvent(descriptionText: "$device.displayName", isStateChange: false)

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x85: 2, 0x70: 1])
	log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}


private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	delayBetween(commands.collect{ secure(it) }, delay)
}