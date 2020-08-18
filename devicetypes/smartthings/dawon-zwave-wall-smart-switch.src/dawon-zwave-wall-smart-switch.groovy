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

		fingerprint mfr: "018C", prod: "0061", model: "0001", deviceJoinName: "Dawon Multipurpose Sensor"	// KR // addChildDevice "Dawon Smart Switch${endpoint}" 1 //Dawon Temp/Humidity Sensor
		fingerprint mfr: "018C", prod: "0062", model: "0001", deviceJoinName: "Dawon Multipurpose Sensor"	// KR // addChildDevice "Dawon Smart Switch${endpoint}" 2 //Dawon Temp/Humidity Sensor
		fingerprint mfr: "018C", prod: "0063", model: "0001", deviceJoinName: "Dawon Multipurpose Sensor"	// KR // addChildDevice "Dawon Smart Switch${endpoint}" 3 //Dawon Temp/Humidity Sensor
		fingerprint mfr: "018C", prod: "0064", model: "0001", deviceJoinName: "Dawon Multipurpose Sensor"	// US // addChildDevice "Dawon Smart Switch${endpoint}" 1 //Dawon Temp/Humidity Sensor
		fingerprint mfr: "018C", prod: "0065", model: "0001", deviceJoinName: "Dawon Multipurpose Sensor"	// US // addChildDevice "Dawon Smart Switch${endpoint}" 2 //Dawon Temp/Humidity Sensor
		fingerprint mfr: "018C", prod: "0066", model: "0001", deviceJoinName: "Dawon Multipurpose Sensor"	// US // addChildDevice "Dawon Smart Switch${endpoint}" 3 //Dawon Temp/Humidity Sensor
	}

	preferences {
		input "reportingInterval", "number", title: "Reporting interval", defaultValue: 10, description: "How often the device should report in minutes", range: "1..60", displayDuringSetup: false // default value set to 10 minutes, range 1~60 minutes
		//input "tempOffset", "number", title: "Temperature Offset", defaultValue: 2, description: "Adjust temperature by this many degrees", range: "1..100", displayDuringSetup: false // default value 2 °<= Dawon DNS don't want to this configuration item changing for power consumption saving.
		//input "humidityOffset", "number", title: "Humidity Offset", defaultValue: 10, description: "Adjust humidity by this percentage", range: "1..100", displayDuringSetup: false // default value 10 % <= Dawon DNS don't want to this configuration item changing for power consumption saving.
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
	log.info "Installed called '${device.displayName}', reportingInterval '${reportingInterval}'"
	if (reportingInterval != null) {
		sendEvent(name: "checkInterval", value: 2 * (reportingInterval as int) + 10 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	} else {
		sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
}

def updated() {
	log.info "updated called"
	configure()
}

def configure() {
	log.info "configure called"
		log.debug "configure: reportingInterval '${reportingInterval}'"
	if (reportingInterval != null) {
		sendEvent(name: "checkInterval", value: 2 * (reportingInterval as int)*60 + 10 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID]) // (reportingInterval as int)*60 : input value unit is minutes
	} else {
		sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	}
	def commands = []
	commands << zwave.multiChannelV3.multiChannelEndPointGet()
	log.debug "configure: commands '${commands}'"
	//log.debug "configure: tempOffset '${tempOffset}'"
	//log.debug "configure: humidityOffset '${humidityOffset}'"

	if (reportingInterval != null) {
		commands << zwave.configurationV1.configurationSet(parameterNumber: 1, size: 2, scaledConfigurationValue: (reportingInterval as int)*60) // (reportingInterval as int)*60 : input value unit is minutes
	}
	/*
	if (tempOffset != null) {
		commands << zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: (tempOffset as int)*10) // 0.1 -> 1
	}
	if (humidityOffset != null) {
		commands << zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: humidityOffset as int)
	}
	*/
	commands << zwave.configurationV1.configurationGet(parameterNumber: 1)
	//commands << zwave.configurationV1.configurationGet(parameterNumber: 2)
	//commands << zwave.configurationV1.configurationGet(parameterNumber: 3)

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
		0x56: 1,  // Crc16Encap
		0x60: 3,  // Multi-Channel
		0x70: 2,  // Configuration
		0x98: 1,  // Security
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
	log.info "zwaveEvent BasicReport called: "+cmd +endpoint
	def value = cmd.value ? "on" : "off"
	endpoint ? changeSwitch(endpoint, value) : []
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint = null) {
	log.info "zwaveEvent SwitchBinaryReport called: ${endpoint}, ${cmd.value}"
	def value = cmd.value ? "on" : "off"
	endpoint ? changeSwitch(endpoint, value) : []
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd, endpoint = null) {
	log.info "zwaveEvent SecurityMessageEncapsulation called: ${cmd}, ${endpoint}"
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulatedCommand from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd, endpoint = null) {
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message
		// Supervision header is 4 bytes long, two bytes dropped here are the latter two bytes of the supervision header
		cmd.parameter = cmd.parameter.drop(2)
		// Updated Command Class/Command now with the remaining bytes
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	log.info "zwaveEvent MultiChannelCmdEncap called: '${cmd}'  endpoint '${endpoint}'"
	def encapsulatedCommand = cmd.encapsulatedCommand()
	log.debug "MultiChannelCmdEncap: encapsulatedCommand '${encapsulatedCommand}'"
	zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, endpoint = null) {
	log.info "zwaveEvent NotificationReport called: cmd: '${cmd}' notificationType: '${cmd.notificationType}', event: '${cmd.event}', endpoint '${endpoint}'"
	def result = []

	if (cmd.notificationType == 0x08) {
		def value = cmd.event== 0x03? "on" : "off"
		endpoint ? result = changeSwitch(endpoint, value) : []
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	log.info "zwaveEvent Crc16Encap called: cmd '${cmd}'"
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
        log.warn "Unable to extract CRC16 command from $cmd"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	log.info "zwaveEvent SensorMultilevelReport called, ${cmd}"
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
	log.info "zwaveEvent ***** Unhandled Command called, cmd '${cmd}', endpoint '${endpoint}' *****"
	[descriptionText: "Unhandled $device.displayName: $cmd", isStateChange: true]
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping(endpoint = null) {
	log.info "ping called: endpoint '${endpoint}' state '${state}'"
	log.debug "ping : device.currentValue : " + device.currentValue("DeviceWatch-DeviceStatus")
	if(endpoint) {
		refresh(endpoint)
	} else {
		refresh()
	}
}

def refresh(endpoint = null) {
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
	log.info "zwaveEvent ConfigurationReport called: ${cmd}"
	switch (cmd.parameterNumber) {
		case 1:
			state.reportingInterval = cmd.scaledConfigurationValue
			break
		/*
		case 2:
			state.tempOffset = cmd.scaledConfigurationValue
			break
		case 3:
			state.humidityOffset = cmd.scaledConfigurationValue
			break
		*/
	}
	//log.debug "zwaveEvent ConfigurationReport: reportingInterval '${state.reportingInterval}', tempOffset '${state.tempOffset}', humidityOffset '${state.humidityOffset}%'"
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd, endpoint = null) {
	log.info "zwaveEvent MultiChannelEndPointReport called: cmd '${cmd}'"
	if(!childDevices) {
		def numberOfChild = getNumberOfChildFromModel()
		log.debug "MultiChannelEndPointReport: numberOfChild '${numberOfChild}'"
		if (numberOfChild) {
			addChildSwitches(numberOfChild)
		} else {
			log.debug "child endpoint=$cmd.endPoints"
			addChildSwitches(cmd.endPoints)
		}
	}
}

def childOn(deviceNetworkId) {
	log.info "childOn called: deviceNetworkId '${deviceNetworkId}'"
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) sendHubCommand onOffCmd(0xFF, switchId)
}

def childOff(deviceNetworkId) {
	log.info "childOff called: deviceNetworkId '${deviceNetworkId}'"
	def switchId = getSwitchId(deviceNetworkId)
	if (switchId != null) sendHubCommand onOffCmd(0x00, switchId)
}

private sendCommands(cmds, delay=1000) {
	log.info "sendCommands called: cmds '${cmds}', delay '${delay}'"
	sendHubCommand(cmds, delay)
}

private secureEncap(cmd, endpoint = null) {
	log.info "secureEncap called"

	def cmdEncap = []
	if (endpoint) {
		cmdEncap = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd)
	} else {
		cmdEncap = cmd
	}

	if (zwaveInfo?.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmdEncap).format()
	} else if (zwaveInfo?.cc?.contains("56")){
		zwave.crc16EncapV1.crc16Encap().encapsulate(cmdEncap).format()
	} else {
		cmdEncap.format()
	}
	
}

private changeSwitch(endpoint, value) {
	log.info "changeSwitch called: value: '${value}', endpoint: '${endpoint}'"
	def result = []
	if(endpoint) {
		String childDni = "${device.deviceNetworkId}:$endpoint"
		def child = childDevices.find { it.deviceNetworkId == childDni }
		log.debug "changeSwitch: endpoint '${endpoint}', value: '${value}')"
		result << child.sendEvent(name: "switch", value: value)
		log.debug "changeSwitch: result '${result}'"
	}
	result
}

private getNumberOfChildFromModel() {
	if ((zwaveInfo.prod.equals("0063")) || (zwaveInfo.prod.equals("0066"))) {
		return 3
	} else if ((zwaveInfo.prod.equals("0062")) || (zwaveInfo.prod.equals("0065"))) {
		return 2
	} else if ((zwaveInfo.prod.equals("0061")) || (zwaveInfo.prod.equals("0064"))) {
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

private getSwitchId(deviceNetworkId) {
	log.info "getSwitchId called: ${deviceNetworkId}"
	def split = deviceNetworkId?.split(":")
	return (split.length > 1) ? split[1] as Integer : null
}

private addChildSwitches(numberOfSwitches) {
	log.info "addChildSwitches called: numberOfSwitches '${numberOfSwitches}'"
	for(def endpoint : 1..numberOfSwitches) {
		try {
			String childDni = "${device.deviceNetworkId}:$endpoint"
			def componentLabel = "Dawon Smart Switch${endpoint}"
			def child = addChildDevice("Child Switch", childDni, device.hubId,
				[completedSetup: true, label: componentLabel, isComponent: false])
			childOff(childDni)
		} catch(Exception e) {
			log.warn "addChildSwitches Exception: ${e}"
		}
	}
}
