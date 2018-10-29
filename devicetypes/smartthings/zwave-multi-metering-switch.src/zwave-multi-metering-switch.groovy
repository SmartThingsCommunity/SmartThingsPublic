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
	definition (name: "Z-Wave Multi Metering Switch", namespace: "smartthings", author: "SmartThings", mnmn: "SmartThings", vid: "generic-switch-power-energy") {
		capability "Switch"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Refresh"
		capability "Configuration"
		capability "Actuator"
		capability "Sensor"
		capability "Health Check"

		command "reset"

		fingerprint mfr:"0086", prod:"0003", model:"0084", deviceJoinName: "Aeotec Nano Switch 1"
		fingerprint mfr:"0086", prod:"0103", model:"0084", deviceJoinName: "Aeotec Nano Switch 1"
		fingerprint mfr: "0000", cc: "0x5E,0x25,0x27,0x32,0x81,0x71,0x60,0x8E,0x2C,0x2B,0x70,0x86,0x72,0x73,0x85,0x59,0x98,0x7A,0x5A", ccOut:"0x82", ui:"0x8700", deviceJoinName: "Aeotec Nano Switch 1"
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

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, ep = null) {
	log.debug "Security Message Encap ${cmd}"
	def encapsulatedCommand = cmd.encapsulatedCommand()
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand, null)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, ep = null) {
	log.debug "Multichannel command ${cmd}" + (ep ? " from endpoint $ep" : "")
	def encapsulatedCommand = cmd.encapsulatedCommand()
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, ep = null) {
	log.debug "Basic ${cmd}" + (ep ? " from endpoint $ep" : "")
	def value = cmd.value ? "on" : "off"
	ep ? changeSwitch(ep, value) : []
}

private changeSwitch(endpoint, value) {
	def result = []
	if(endpoint == 1) {
		result += createEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	} else {
		String childDni = "${device.deviceNetworkId}:$endpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(name: "switch", value: value, isStateChange: true, descriptionText: "Switch ${endpoint} is ${value}")
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep) {
	log.debug "Meter ${cmd}" + (ep ? " from endpoint $ep" : "")
	def result = []
	if(ep == 1) {
		result += createEvent(createMeterEventMap(cmd))
	} else if(ep) {
		String childDni = "${device.deviceNetworkId}:$ep"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		child?.sendEvent(createMeterEventMap(cmd))
	} else {
		result += zwaveEvent(cmd)
	}
	result += response(secureEncap(zwave.meterV3.meterGet(scale: 2), ep))
	result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	def event = createEvent([isStateChange:  false, descriptionText: "Wattage change has been detected. Refreshing each endpoint"])
	[event, response(refreshAll())]
}

private createMeterEventMap(cmd) {
	def eventMap = [:]
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			eventMap.name = "energy"
			eventMap.value = cmd.scaledMeterValue
			eventMap.unit = "kWh"
		} else if (cmd.scale == 2) {
			eventMap.name = "power"
			eventMap.value = Math.round(cmd.scaledMeterValue)
			eventMap.unit = "W"
		}
	}
	eventMap
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, ep = null) {
	if(!childDevices) {
		addChildSwitches(cmd.endPoints)
	}
	response([
			resetAll(),
			refreshAll()
	])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd, ep = null) {
	def mfr = Integer.toHexString(cmd.manufacturerId)
	def model = Integer.toHexString(cmd.productId)
	updateDataValue("mfr", mfr)
	updateDataValue("model", model)
	lateConfigure()
}

def zwaveEvent(physicalgraph.zwave.Command cmd, ep) {
	log.warn "Unhandled ${cmd}" + (ep ? " from endpoint $ep" : "")
}

private onOffCmd(value, endpoint = 1) {
	delayBetween([
			secureEncap(zwave.basicV1.basicSet(value: value), endpoint),
			secureEncap(zwave.basicV1.basicGet(), endpoint),
			"delay 3000",
			secureEncap(zwave.meterV3.meterGet(scale: 2), endpoint)
	])
}

def refresh(endpoint = 1) {
	delayBetween([
			secureEncap(zwave.basicV1.basicGet(), endpoint),
			secureEncap(zwave.meterV3.meterGet(scale: 0), endpoint),
			secureEncap(zwave.meterV3.meterGet(scale: 2), endpoint),
			"delay 500"
	], 500)
}

def on() {
	onOffCmd(0xFF)
}

def off() {
	onOffCmd(0x00)
}

def ping() {
	refresh()
}

def reset(endpoint = 1) {
	log.debug "Resetting endpoint: ${endpoint}"
	delayBetween([
			secureEncap(zwave.meterV3.meterReset(), endpoint),
			secureEncap(zwave.meterV3.meterGet(scale: 0), endpoint),
			"delay 500"
	], 500)
}

def childOnOff(deviceNetworkId, value) {
	def switchId = deviceNetworkId?.split(":")[1] as Integer
	sendHubCommand onOffCmd(value, switchId)
}

def childRefresh(deviceNetworkId) {
	def switchId = deviceNetworkId?.split(":")[1] as Integer
	sendHubCommand refresh(switchId)
}

def childReset(deviceNetworkId) {
	def switchId = deviceNetworkId?.split(":")[1] as Integer
	log.debug "Child reset switchId: ${switchId}"
	sendHubCommand reset(switchId)
}

private refreshAll() {
	childDevices.each { childRefresh(it.deviceNetworkId) }
	sendHubCommand refresh()
}

private resetAll() {
	childDevices.each { childReset(it.deviceNetworkId) }
	sendHubCommand reset()
}

def installed() {
	log.debug "Installed ${device.displayName}"
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def updated() {
	sendHubCommand secure(zwave.multiChannelV3.multiChannelEndPointGet())
}

def configure() {
	log.debug "Configure..."
	response([
			secure(zwave.multiChannelV3.multiChannelEndPointGet()),
			secure(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
	])
}

private secure(cmd) {
	if(zwaveInfo.zw.endsWith("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private encap(cmd, endpoint = null) {
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
	} else {
		cmd
	}
}

private secureEncap(cmd, endpoint = null) {
	secure(encap(cmd, endpoint))
}

private addChildSwitches(numberOfSwitches) {
	for(def endpoint : 2..numberOfSwitches) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = device.displayName[0..-2] + "${endpoint}"
			addChildDevice("Child Metering Switch", childDni, device.getHub().getId(), [
					completedSetup: true,
					label         : componentLabel,
					isComponent   : false,
					componentName : "switch$endpoint",
					componentLabel: "Switch $endpoint"
			])
		} catch(Exception e) {
			log.debug "Exception: ${e}"
		}
	}
}

private lateConfigure() {
	def cmds = []
	log.debug "Late configuration..."
	switch(getDeviceModel()) {
		case "ZW132":
			cmds = [
					secure(zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, configurationValue: [0])),    // resets configuration
					secure(zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, configurationValue: [1])),    // enables overheat protection
					secure(zwave.configurationV1.configurationSet(parameterNumber: 80, size: 1, configurationValue: [2])),    // send BasicReport CC
					secure(zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: 2048)),    // enabling kWh energy reports on ep 1
					secure(zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: 600)),    //... every 10 minutes
					secure(zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: 4096)),    // enabling kWh energy reports on ep 2
					secure(zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: 600)),    //... every 10 minutes
					secure(zwave.configurationV1.configurationSet(parameterNumber: 90, size: 1, scaledConfigurationValue: 1) ),    //enables reporting based on wattage change
					secure(zwave.configurationV1.configurationSet(parameterNumber: 91, size: 2, scaledConfigurationValue: 20))    //report any 20W change
			]
			break
		default:
			cmds = [secure(zwave.configurationV1.configurationSet(parameterNumber: 255, size: 1, scaledConfigurationValue: 0))]
			break
	}
	sendHubCommand cmds
}

private getDeviceModel() {
	if((zwaveInfo.mfr?.contains("0086") && zwaveInfo.model?.contains("0084")) || (getDataValue("mfr") == "86") && (getDataValue("model") == "84")) {
		"ZW132"
	} else {
		""
	}
}