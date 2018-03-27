/**
 *  Fibaro Flood Sensor ZW5
 *
 *  Copyright 2016 Fibar Group S.A.
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
	definition (name: "Fibaro Flood Sensor ZW5", namespace: "fibargroup", author: "Fibar Group S.A.", ocfDeviceType: "x.com.st.d.sensor.moisture") {
		capability "Battery"
		capability "Configuration"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Water Sensor"
		capability "Health Check"

		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x22, 0x85, 0x59, 0x20, 0x80, 0x70, 0x56, 0x5A, 0x7A, 0x72, 0x8E, 0x71, 0x73, 0x98, 0x9C, 0x31, 0x86", outClusters: ""
		fingerprint mfr:"010F", prod:"0B01", model:"2002"
		fingerprint mfr:"010F", prod:"0B01", model:"1002"
	}

	simulator {
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"FGFS", type:"lighting", width:6, height:4) {//with generic type secondary control text is not displayed in Android app
			tileAttribute("device.water", key:"PRIMARY_CONTROL") {
				attributeState("dry", label: "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff")
				attributeState("wet", label: "wet", icon:"st.alarm.water.wet", backgroundColor:"#00a0dc")
			}

			tileAttribute("device.tamper", key:"SECONDARY_CONTROL") {
				attributeState("detected", label:'tampered', backgroundColor:"#cccccc")
				attributeState("clear", label:'tamper clear', backgroundColor:"#00A0DC")
			}
		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
			backgroundColors:[
				[value: 31, color: "#153591"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]
			]
		}

		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main "FGFS"
		details(["FGFS","battery", "temperature"])
	}
}

def installed() {
	sendEvent(name: "tamper", value: "clear", displayed: false)
}

def updated() {
	def tamperValue = device.latestValue("tamper")
    
    if (tamperValue == "active") {
    	sendEvent(name: "tamper", value: "detected", displayed: false)
    } else if (tamperValue == "inactive") {
    	sendEvent(name: "tamper", value: "clear", displayed: false)
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	def result = []

	if (description.startsWith("Err 106")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, displayed:false)
		} else {
			result = createEvent(
				descriptionText: "FGFS failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, [0x31: 5, 0x56: 1, 0x71: 3, 0x72:2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])

		if (cmd) {
			log.debug "Parsed '${cmd}'"
			zwaveEvent(cmd)
		}
	}
}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x71: 3, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

//crc16
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
	def versions = [0x31: 5, 0x72: 2, 0x80: 1]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from $cmd"
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
	def cmds = []
	// cmds << encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0))
	// cmds << "delay 500"
	cmds << encap(zwave.batteryV1.batteryGet())
	[event, response(cmds)]
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

	if (cmd.deviceIdType == 1 && cmd.deviceIdDataFormat == 1) { //serial number in binary format
		String serialNumber = "h'"

		cmd.deviceIdData.each{ data ->
			serialNumber += "${String.format("%02X", data)}"
		}

		updateDataValue("serialNumber", serialNumber)
		log.debug "${device.displayName} - serial number: ${serialNumber}"
	}

	def response_cmds = []
	if (!device.currentState("temperature")) {
		response_cmds << encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0))
	}
	response_cmds << "delay 1000"
	response_cmds << encap(zwave.wakeUpV2.wakeUpNoMoreInformation())
	[[:], response(response_cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
	log.debug "applicationVersion:      ${cmd.applicationVersion}"
	log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
	log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
	log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
	log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def result = []
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel.toString()
	map.unit = "%"

	result << createEvent(map)

	if (!getDataValue("serialNumber")) {
		result << response(encap(zwave.manufacturerSpecificV2.deviceSpecificGet()))
	} else {
		result << response(encap(zwave.wakeUpV2.wakeUpNoMoreInformation()))
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def map = [:]
	if (cmd.notificationType == 5) {
		switch (cmd.event) {
			case 2:
				map.name = "water"
				map.value = "wet"
				map.descriptionText = "${device.displayName} is ${map.value}"
				break

			case 0:
				map.name = "water"
				map.value = "dry"
				map.descriptionText = "${device.displayName} is ${map.value}"
				break
		}
	} else if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				map.name = "tamper"
				map.value = "clear"
				map.descriptionText = "Tamper aleart cleared"
				break

			case 3:
				map.name = "tamper"
				map.value = "detected"
				map.descriptionText = "Tamper alert: sensor removed or covering opened"
				break
		}
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]
	if (cmd.sensorType == 1) {
		// temperature
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		// overwriting the precision here to match other devices
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, 0)
		map.unit = getTemperatureScale()
		map.name = "temperature"
		map.displayed = true
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.info "${device.displayName}: received command: $cmd - device has reset itself"
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: $cmd"
}

def configure() {
	log.debug "Executing 'configure'"
	// Device wakes up every 4 hours, this interval of 8h 2m allows us to miss one wakeup notification before marking offline
	sendEvent(name: "checkInterval", value: 8 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])

	// default initial state
	sendEvent(name: "water", value: "dry")

	def cmds = []

	cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId])
	cmds << zwave.batteryV1.batteryGet()  // other queries sent as response to BatteryReport
	cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1)

	encapSequence(cmds, 200)
}

private secure(physicalgraph.zwave.Command cmd) {
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crc16(physicalgraph.zwave.Command cmd) {
	//zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
	"5601${cmd.format()}0000"
}

private encapSequence(commands, delay=200) {
	delayBetween(commands.collect{ encap(it) }, delay)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw && !zwaveInfo.zw.contains("s")) {
		// Secure inclusion failed
		crc16(cmd)
	} else {
		secure(cmd)
	}
}
