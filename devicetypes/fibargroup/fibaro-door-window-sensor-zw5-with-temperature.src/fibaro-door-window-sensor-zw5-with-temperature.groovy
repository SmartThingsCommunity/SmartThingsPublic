/**
 *  Fibaro Door/Window Sensor ZW5
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
	definition (name: "Fibaro Door/Window Sensor ZW5 with Temperature", namespace: "fibargroup", author: "Fibar Group S.A.", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Battery"
		capability "Contact Sensor"
		capability "Sensor"
        capability "Configuration"
        capability "Tamper Alert"

        capability "Temperature Measurement"

		fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x85, 0x59, 0x22, 0x20, 0x80, 0x70, 0x56, 0x5A, 0x7A, 0x72, 0x8E, 0x71, 0x73, 0x98, 0x2B, 0x9C, 0x30, 0x31, 0x86", outClusters: ""
        fingerprint deviceId: "0x0701", inClusters: "0x5E, 0x85, 0x59, 0x22, 0x20, 0x80, 0x70, 0x56, 0x5A, 0x7A, 0x72, 0x8E, 0x71, 0x73, 0x98, 0x2B, 0x9C, 0x30, 0x31, 0x86, 0x84", outClusters: ""//actual NIF
	}

	simulator {

	}

    tiles(scale: 2) {
    	multiAttributeTile(name:"FGK", type:"lighting", width:6, height:4) {//with generic type secondary control text is not displayed in Android app
        	tileAttribute("device.contact", key:"PRIMARY_CONTROL") {
                attributeState("open", label: "open", icon:"st.contact.contact.open", backgroundColor:"#e86d13")
                attributeState("closed", label: "closed", icon:"st.contact.contact.closed", backgroundColor:"#00a0dc")
            }

            tileAttribute("device.tamper", key:"SECONDARY_CONTROL") {
				attributeState("detected", label:'tampered', backgroundColor:"#00A0DC")
				attributeState("clear", label:'tamper clear', backgroundColor:"#CCCCCC")
			}
        }

        valueTile("battery", "device.battery", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "battery", label:'${currentValue}% battery', unit:""
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

        main "FGK"
        details(["FGK","battery", "temperature"])
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
				descriptionText: "FGK failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else if (description == "updated") {
		return null
	} else {
    	def cmd = zwave.parse(description, [0x31: 5, 0x56: 1, 0x71: 3, 0x72: 2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])

    	if (cmd) {
    		log.debug "Parsed '${cmd}'"
        	zwaveEvent(cmd)
    	}
    }
}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x71: 3, 0x84: 2, 0x85: 2, 0x98: 1])
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
    def versions = [0x31: 5, 0x72: 2, 0x80: 1, 0x86: 1]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from $cmd"
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	//it is assumed that default notification events are used
    //(parameter 20 was not changed before device's re-inclusion)
    def map = [:]
    if (cmd.notificationType == 6) {
    	switch (cmd.event) {
        	case 22:
            	map.name = "contact"
                map.value = "open"
                map.descriptionText = "${device.displayName} is open"
            	break

            case 23:
            	map.name = "contact"
                map.value = "closed"
                map.descriptionText = "${device.displayName} is closed"
            	break
        }
    } else if (cmd.notificationType == 7) {
    	switch (cmd.event) {
        	case 0:
            	map.name = "tamper"
                map.value = "clear"
                map.descriptionText = "Tamper alert cleared"
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

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel.toString()
	map.unit = "%"
	map.displayed = true
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
    def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
    def cmds = []
    cmds << encap(zwave.batteryV1.batteryGet())
    cmds << "delay 500"
    cmds << encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0))
    cmds << "delay 1200"
    cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
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
	def map = [:]
	if (cmd.sensorType == 1) {
        // temperature
        def cmdScale = cmd.scale == 1 ? "F" : "C"
        map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
        map.unit = getTemperatureScale()
        map.name = "temperature"
        map.displayed = true
	}

    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.info "${device.displayName}: received command: $cmd - device has reset itself"
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
	def map = [:]
	map.value = cmd.sensorValue ? "open" : "closed"
	map.name = "contact"
	if (map.value == "open") {
		map.descriptionText = "${device.displayName} is open"
	}
	else {
		map.descriptionText = "${device.displayName} is closed"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: $cmd"
}

def configure() {
	log.debug "Executing 'configure'"

    def cmds = []

    cmds += zwave.wakeUpV2.wakeUpIntervalSet(seconds:21600, nodeid: zwaveHubNodeId)//FGK's default wake up interval
    cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
    cmds += zwave.batteryV1.batteryGet()
    cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
    cmds += zwave.sensorBinaryV2.sensorBinaryGet()
	cmds += zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId])
    cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()

    encapSequence(cmds, 500)
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
    def secureClasses = [0x20, 0x2B, 0x30, 0x5A, 0x70, 0x71, 0x84, 0x85, 0x8E, 0x9C]

    //todo: check if secure inclusion was successful
    //if not do not send security-encapsulated command
	if (secureClasses.find{ it == cmd.commandClassId }) {
    	secure(cmd)
    } else {
    	crc16(cmd)
    }
}
