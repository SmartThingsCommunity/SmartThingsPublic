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
	definition (name: "Z-Wave Metering Switch Secure", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.smartplug") {
		capability "Actuator"
		capability "Configuration"
		capability "Energy Meter"
		capability "Health Check"
		capability "Light"
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"

		command "reset"

		fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x22, 0x85, 0x59, 0x70, 0x56, 0x5A, 0x7A, 0x72, 0x32, 0x8E, 0x71, 0x73, 0x98, 0x31, 0x25, 0x86", outClusters: "", deviceJoinName: "Outlet"
		fingerprint mfr: "0072", prod: "0501", model: "0F06", deviceJoinName: "Fibaro Outlet" // US //Fibaro Wall Plug ZW5
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

	tiles(scale: 2) {
		multiAttributeTile(name:"lighting", type:"lighting", width:6, height:4) {//with generic type secondary control text is not displayed in Android app
			tileAttribute("device.switch", key:"PRIMARY_CONTROL") {
				attributeState("on", label: '${name}', action: "switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC")
				attributeState("off", label: '${name}', action: "switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff")
			}

			tileAttribute("device.power", key:"SECONDARY_CONTROL") {
				attributeState("default", label:'${currentValue} W', backgroundColor:"#ffffff")
			}
		}

		valueTile("energy", "device.energy", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "lighting"
		details(["lighting", "energy", "reset", "refresh"])
	}
}

def installed() {
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	// Device-Watch simply pings if no device events received for 122min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	response(refresh())
}

// parse events into attributes
def parse(String description) {
	def result
	log.debug "Parsing '${description}'"
	if (description.startsWith("Err 106")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, displayed:false)
		} else {
			result = createEvent(
					descriptionText: "Switch failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
					eventType: "ALERT",
					name: "secureInclusion",
					value: "failed",
					displayed: true,
			)
		}
		state.sec = 0
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, [0x25: 1, 0x31: 5, 0x32: 1, 0x5A: 1, 0x71: 3, 0x72: 2, 0x86: 1])

		if (cmd) {
			log.debug "Parsed '${cmd}'"
			result = zwaveEvent(cmd)
		}
	}

	result
}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x5A: 1])
	if (encapsulatedCommand) {
		state.sec = 1
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

//crc16
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
	def versions = [0x31: 5, 0x32: 1, 0x71: 3, 0x72: 2, 0x86: 1]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from $cmd"
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	if (cmd.scale == 0) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
	} else if (cmd.scale == 1) {
		createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
	} else if (cmd.scale == 2) {
		createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
	log.debug "deviceIdData:                ${cmd.deviceIdData}"
	log.debug "deviceIdDataFormat:          ${cmd.deviceIdDataFormat}"
	log.debug "deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}"
	log.debug "deviceIdType:                ${cmd.deviceIdType}"

	if (cmd.deviceIdType == 1 && cmd.deviceIdDataFormat == 1) {//serial number in binary format
		String serialNumber = "h'"

		cmd.deviceIdData.each{ data ->
			serialNumber += "${String.format("%02X", data)}"
		}

		updateDataValue("serialNumber", serialNumber)
		log.debug "${device.displayName} - serial number: ${serialNumber}"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
	log.debug "applicationVersion:      ${cmd.applicationVersion}"
	log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
	log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
	log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
	log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [ displayed: true ]
	if (cmd.sensorType == 4) {
		createEvent(name: "power", value: Math.round(cmd.scaledSensorValue), unit: "W")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	createEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.info "${device.displayName}: received command: $cmd - device has reset itself"
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	if (cmd.notificationType == 0x08) {
		if (cmd.event == 0x06) {
			createEvent(descriptionText: "Warning: $device.displayName detected over-current", isStateChange: true)
		} else if (cmd.event == 0x08) {
			createEvent(descriptionText: "Warning: $device.displayName detected over-load", isStateChange: true)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "$device.displayName: Unhandled: $cmd"
	[:]
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"

	def cmds = []

	cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
	cmds += zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
	cmds += zwave.meterV2.meterGet(scale:0)
	cmds += zwave.meterV2.meterGet(scale:2)
	cmds += zwave.switchBinaryV1.switchBinaryGet()

	encapSequence(cmds, 500)
}


/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping() called"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"

	def cmds = []
	cmds += zwave.meterV2.meterGet(scale:0)
	cmds += zwave.meterV2.meterGet(scale:2)
	cmds += zwave.switchBinaryV1.switchBinaryGet()

	encapSequence(cmds, 500)
}

def on() {
	log.debug "Executing 'on'"

	def commands = []
	commands += zwave.basicV1.basicSet(value: 0xFF)
	commands += zwave.switchBinaryV1.switchBinaryGet()
	commands += zwave.meterV2.meterGet(scale: 2)

	encapSequence(commands, 500)
}

def off() {
	log.debug "Executing 'off'"

	def commands = []
	commands += zwave.basicV1.basicSet(value: 0x00)
	commands += zwave.switchBinaryV1.switchBinaryGet()
	commands += zwave.meterV2.meterGet(scale: 2)

	encapSequence(commands, 500)
}

def reset() {
	resetEnergyMeter()
}

def resetEnergyMeter() {
	log.debug "Executing 'reset'"
	encap(zwave.meterV2.meterReset())
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
	//zwave.crc16encapV1.crc16Encap().encapsulate(cmd).format()
	"5601${cmd.format()}0000"
}

private encapSequence(commands, delay=200) {
	delayBetween(commands.collect{ encap(it) }, delay)
}

private encap(physicalgraph.zwave.Command cmd) {
	def secureClasses = [0x25, 0x5A, 0x70, 0x85, 0x8E]

	//todo: check if secure inclusion was successful
	//if not do not send security-encapsulated command
	if (secureClasses.find{ it == cmd.commandClassId }) {
		secure(cmd)
	} else {
		crc16(cmd)
	}
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.sec != 0) {
		log.debug "securely sending $cmd"
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		log.debug "unsecurely sending $cmd"
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}
