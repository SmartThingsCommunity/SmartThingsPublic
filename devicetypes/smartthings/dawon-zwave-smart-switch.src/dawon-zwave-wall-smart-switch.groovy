/**
 *  Copyright 2020 SmartThings
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
	definition (name: "Dawon Z-Wave Wall Smart Switch", namespace: "smartthings", author: "SmartThings", mnmn:"SmartThings", vid:"generic-humidity-3") {

		capability "Configuration"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Sensor"
		capability "Health Check"

		fingerprint mfr: "018C", prod: "0061", model: "0001", deviceJoinName: "Dawon Temp/Humidity Sensor" // addChildDevice "Dawon Smart Switch${endpoint}" 1
		fingerprint mfr: "018C", prod: "0062", model: "0001", deviceJoinName: "Dawon Temp/Humidity Sensor" // addChildDevice "Dawon Smart Switch${endpoint}" 2
		fingerprint mfr: "018C", prod: "0063", model: "0001", deviceJoinName: "Dawon Temp/Humidity Sensor" // addChildDevice "Dawon Smart Switch${endpoint}" 3
	}

	simulator {
	}

	preferences {
		input "tempOffset", "number", title: "Temperature Offset", description: "Adjust temperature by this many degrees", range: "*..*", displayDuringSetup: false
		input "humidityOffset", "number", title: "Humidity Offset", description: "Adjust humidity by this percentage", range: "*..*", displayDuringSetup: false
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label: '${currentValue}°',
					backgroundColors: [
						[value: 31, color: "#153591"],
						[value: 44, color: "#1e9cbb"],
						[value: 59, color: "#90d2a7"],
						[value: 74, color: "#44b621"],
						[value: 84, color: "#f1d801"],
						[value: 95, color: "#d04e00"],
						[value: 96, color: "#bc2323"]
					]
			}
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label: '${currentValue}% humidity', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		main "temperature", "humidity"
		details(["temperature", "humidity", "refresh"])
	}
}

def installed() {
	log.info "Installed called '${device.displayName}'"
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	initialize()
}

def updated() {
	log.info "updated called"
	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	initialize()
	try {
		if (!state.MSR) {
			response(zwave.manufacturerSpecificV2.manufacturerSpecificGet().format())
		}
	} catch (e) {
		log.warn e
	}
}

def initialize() {
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def configure() {
	log.info "configure called"
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	def msrdata = getDataValue("MSR")
	def commands = []
	commands << zwave.multiChannelV3.multiChannelEndPointGet()
	log.debug "configure: commands '${commands}'"
	log.debug "configure: msrdata '${msrdata}'"
	log.debug "configure: tempOffset '${tempOffset}'"
	log.debug "configure: humidityOffset '${humidityOffset}'"

	if (msrdata == null) {
		commands << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	}

	if (tempOffset != null) {
		commands << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: tempOffset as int)
	}
	if (humidityOffset != null) {
		commands << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: humidityOffset as int)
	}
	commands << zwave.configurationV1.configurationGet(parameterNumber: 1)
	commands << zwave.configurationV1.configurationGet(parameterNumber: 2)

	commands << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01) // temperature
	commands << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05) // humidity

	sendCommands(commands,1000)
}

/**
 * Mapping of command classes and associated versions used for this DTH
 */
private getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x25: 1,  // Switch Binary
		0x30: 1,  // Sensor Binary
		0x31: 5,  // Sensor MultiLevel
		0x32: 3,  // Meter
		0x56: 1,  // Crc16Encap
		0x60: 3,  // Multi-Channel
		0x70: 2,  // Configuration
		0x98: 1,  // Security
		0x9C: 1,  // Sensor Alarm
		0x71: 3   // Notification
	]
}

def parse(String description) {
	log.info("parse called: description: ${description}")
	def result = []
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	return createEvent(result)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=null) {
	log.info "BasicReport called: "+cmd +endpoint
	def value = cmd.value ? "on" : "off"
	endpoint ? changeSwitch(endpoint, value) : []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint = null) {
	log.info "SwitchBinaryReport called: ${endpoint}, ${cmd.value}"
	def value = cmd.value ? "on" : "off"
	endpoint ? changeSwitch(endpoint, value) : []
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, endpoint = null) {
	log.info "SecurityMessageEncapsulation called: ${cmd}, ${endpoint}"
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		waveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulatedCommand from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, endpoint = null) {
	log.info "MultiChannelCmdEncap called: '${cmd}'  endpoint '${endpoint}'"
	def encapsulatedCommand = cmd.encapsulatedCommand()
	log.debug "MultiChannelCmdEncap: encapsulatedCommand '${encapsulatedCommand}'"
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, endpoint = null) {
	log.info "NotificationReport called: notificationType: '${cmd.notificationType}', event: '${cmd.event}', endpoint '${endpoint}'"
	def result = []

	if (cmd.notificationType == 0x08) {
		def value = cmd.event== 0x03? "on" : "off"
		endpoint ? result = changeSwitch(endpoint, value) : []
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	log.info "Crc16Encap called: cmd '${cmd}'"
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	log.info "SensorMultilevelReport called, ${cmd}"
	def map = [:]
	def result = []
	switch (cmd.sensorType) {
		case 1:
			map.name = "temperature"
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			break		
		case 5:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			break
		default:
			map.descriptionText = cmd.toString()
	}
	log.debug "SensorMultilevelReport, ${map}, ${map.name}, ${map.value}, ${map.unit}"
	result << createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd, endpoint = null) {
	log.info "***** Unhandled Command called, cmd '${cmd}', endpoint '${endpoint}' *****"
	[descriptionText: "Unhandled $device.displayName: $cmd", isStateChange: true]
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping(endpoint = null) {
	log.info "ping called"
	refresh(endpoint)
}

def refresh(endpoint) {
	log.info "refresh called: endpint '${endpoint}' "
	if(endpoint) {
		secureEncap(zwave.basicV1.basicGet(), endpoint)
	} else {
		def commands = []
		commands << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)
		commands << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 5)
		sendCommands(commands,1000)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd){
	log.info "ConfigurationReport called: ${cmd}"
	def result = []
	switch (cmd.parameterNumber) {
		case 1:
			state.tempOffset = cmd.scaledConfigurationValue
			break
		case 2:
			state.humidityOffset = cmd.scaledConfigurationValue
			break
	}
	result <<  createEvent(name: "configReport", value: "${state.tempOffset}, ${state.humidityOffset}%")
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, endpoint = null) {
	log.info "MultiChannelEndPointReport called: cmd '${cmd}'"
	if(!childDevices) {
		def type = isDawonWallSmartSwitch()
		log.debug "MultiChannelEndPointReport: type '${type}'"
		if (type) {
			addChildSwitches(type)
		} else {
			log.debug "child endpoint=$cmd.endPoints"
			addChildSwitches(cmd.endPoints)
		}
	}
}

private sendCommands(cmds, delay=1000) {
	log.info "sendCommands called: cmds '${cmds}', delay '${delay}'"
	sendHubCommand(cmds, delay)
}

private commands(commands, delay=200) {
	log.info "commands called: commands '${commands}', delay '${delay}'"
	delayBetween(commands.collect{ command(it) }, delay)
}


private secure(cmd) {
	log.info "secure called"
	if (zwaveInfo?.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else if (zwaveInfo?.cc?.contains("56")){
		zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private encap(cmd, endpoint = null) {
	log.info "encap called"
	if (endpoint) {
		zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
	} else {
		cmd
	}
}

private secureEncap(cmd, endpoint = null) {
	log.info "secureEncap called"
	secure(encap(cmd, endpoint))
}

private changeSwitch(endpoint, value) {
	log.info "changeSwitch called: vlaue: '${value}', endpoint: '${endpoint}'"
	def result = []
	if(endpoint) {
		String childDni = "${device.deviceNetworkId}:$endpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		log.debug "changeSwitch: endpoint '${endpoint}', value: '${value}')"
		result << child.sendEvent(name: "switch", value: value)
	}
	result
}

def childOnOff(deviceNetworkId, value) {
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) sendHubCommand onOffCmd(value, switchId)
}

def childRefresh(deviceNetworkId) {
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) sendHubCommand refresh(switchId)
}

private isDawonWallSmartSwitch() {
	if (zwaveInfo.prod.equals("0063")) {
		return 3
	} else if (zwaveInfo.prod.equals("0062")) {
		return 2
	} else if (zwaveInfo.prod.equals("0061")) {
		return 1
	} else {
		return 0
	}
	return 0
}

private onOffCmd(value, endpoint) {
	log.info "onOffCmd called: val:${value}, ep:${endpoint}"
	secureEncap(zwave.basicV1.basicSet(value: value), endpoint)
}

def getSwitchId(deviceNetworkId) {
	log.info "getSwitchId called: ${deviceNetworkId}"
	def split = deviceNetworkId?.split(":")
	return (split.length > 1) ? split[1] as Integer : null
}

private addChildSwitches(numberOfSwitches) {
	log.info "addChildSwitches called: ${numberOfSwitches}"
	for(def endpoint : 1..numberOfSwitches) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = "Dawon Smart Switch${endpoint}"
			addChildDevice("Child Dawon Z-wave Wall Smart Switch Health", childDni, device.getHub().getId(), [
				completedSetup: true,
				label         : componentLabel,
				isComponent   : false
			])
		} catch(Exception e) {
			log.warn "Exception: ${e}"
		}
	}
}

