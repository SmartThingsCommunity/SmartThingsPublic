/**
 *  Copyright 2018 SRPOL
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
	definition (name: "Z-Wave Dual Metering Switch", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "SmartThings-smartthings-Z-Wave_Metering_Switch") {
		capability "Switch"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Actuator"
		capability "Sensor"

		command "reset"

		fingerprint type:"1001", mfr:"0086", prod:"0003", model:"0084", deviceJoinName: "Aeotec Nano Switch 1"
		fingerprint type:"1001", mfr:"0086", prod:"0103", model:"0084", deviceJoinname: "Aoetec Nano Switch 1"
	}

	tiles(scale: 2){
		multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState("on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc")
				attributeState("off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff")
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}

		main(["switch"])
		details(["switch","power","energy","refresh","reset"])
	}
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description)
		if (cmd) {
			result = zwaveEvent(cmd, null)
		}
	}
	log.debug "parsed '${description}' to ${result.inspect()}"
	result
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep) {
	log.debug "Multichannel command ${cmd}" + (ep ? " from endpoint $ep" : "")
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep) {
	log.debug "Basic ${cmd}" + (ep ? " from endpoint $ep" : "")
	def value = cmd.value ? "on" : "off"
	ep ? changeSwitch(ep, value) : []
}

private changeSwitch(endpoint, value) {
	def result = []
	if(endpoint == 1) {
		result += createEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	} else {
		String childDni = "${device.deviceNetworkId}/$endpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep) {
	log.debug "Meter ${cmd}" + (ep ? " from endpoint $ep" : "")
	def result
	if(ep == 1) {
		result = handleMeterReport(cmd)
	} else if(ep) {
		result = childHandleMeterReport(cmd, ep)
	} else {
		result = zwaveEvent(cmd)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def event = createEvent([isStateChange:  false, descriptionText: "Wattage change has been detected. Refreshing each endpoint"])
	[event, response(refreshAll())]
}

def handleMeterReport(cmd) {
	def result = []
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			result += createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
			result += response(encap(zwave.meterV3.meterGet(scale: 2), 1))
		} else if (cmd.scale == 2) {
			result += createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		}
	}
	result
}

def childHandleMeterReport(cmd, endpoint) {
	String childDni = "${device.deviceNetworkId}/$endpoint"
	def child = childDevices.find { it.deviceNetworkId == childDni }
	def result = []
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			child?.sendEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
			result += response(encap(zwave.meterV3.meterGet(scale: 2), endpoint))
		} else if (cmd.scale == 2) {
			child?.sendEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		}
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep) {
	updateDataValue("MSR", String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId))
	null
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	log.warn "Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}

private onOffCmd(value, Integer endpoint = null) {
	delayBetween([
			encap(zwave.basicV1.basicSet(value: value), endpoint),
			encap(zwave.basicV1.basicGet(), endpoint),
			"delay 3000",
			encap(zwave.meterV3.meterGet(scale: 2), endpoint)
	])
}

private refreshCmd(endpoint) {
	delayBetween([
			encap(zwave.basicV1.basicGet(), endpoint),
			encap(zwave.meterV3.meterGet(scale: 0), endpoint),
			"delay 500"
	], 500)
}

def on() {
	onOffCmd(255, 1)
}

def off() {
	onOffCmd(0, 1)
}

def refresh() {
	def name = device.displayName.split(" ")
	log.debug "Splited: ${name[0..-1]}"
	refreshCmd(1)
}

def resetCmd(endpoint = null) {
	log.debug "Reseting endpoint: ${endpoint}"
	delayBetween([
			encap(zwave.meterV3.meterReset(), endpoint),
			encap(zwave.meterV3.meterGet(scale: 0), endpoint),
			"delay 500"
	], 500)
}

def reset() {
	resetCmd(1)
}

def childOn(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1] as Integer
	sendHubCommand onOffCmd(255, switchId)
}

def childOff(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1] as Integer
	sendHubCommand onOffCmd(0, switchId)
}

def childRefresh(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1] as Integer
	sendHubCommand refreshCmd(switchId)
}

def childReset(deviceNetworkId) {
	def switchId = deviceNetworkId?.split("/")[1] as Integer
	sendHubCommand resetCmd(switchId)
}

def installed() {
	log.debug "Installed ${device.displayName}"
	addChildSwitch()
}

private refreshAll() {
	childDevices.each { childRefresh(it.deviceNetworkId) }
	sendHubCommand refresh()
}

private resetAll() {
	childDevices.each { childReset(it.deviceNetworkId) }
	sendHubCommand reset()
}

def configure() {
	def cmds = [
			encap(zwave.configurationV2.configurationSet(parameterNumber: 255, size: 1, configurationValue: [0])), 	// resets configuration
			encap(zwave.configurationV2.configurationSet(parameterNumber: 4, size: 1, configurationValue: [1])),	// enables overheat protection
			encap(zwave.configurationV2.configurationSet(parameterNumber: 80, size: 1, configurationValue: [2])),	// send BasicReport CC
			encap(zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 2048)), 	// enabling kWh energy reports on ep 1
			encap(zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 600)),	//... every 10 minutes
			encap(zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 4096)), 	// enabling kWh energy reports on ep 2
			encap(zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 600)), 	//... every 10 minutes
			encap(zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: 1)),	//enables reporting based on wattage change
			encap(zwave.configurationV1.configurationSet(parameterNumber: 91, size: 2, scaledConfigurationValue: 20)) 	//report any 20W change
	]
	delayBetween(cmds) + "delay 1000" + resetAll() + refreshAll()
}

private encap(cmd, endpoint = null) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private addChildSwitch() {
	def endpoint = 2
	String childDni = "${device.deviceNetworkId}/$endpoint"
	def componentLabel = device.displayName[0..-2] + " ${endpoint}"
	addChildDevice("Child Metering Switch", childDni, null, [
			completedSetup	:	true,
			label			: componentLabel,
			isComponent		: false,
			hubId			: device.getHub().getId(),
			componentName	: "switch$endpoint",
			componentLabel	: "Switch $endpoint"
	])
}
