/**
 *	Fibaro Flood Sensor ZW5
 */
metadata {
	definition(name: "Fibaro Flood Sensor ZW5", namespace: "FibarGroup", author: "Fibar Group", ocfDeviceType: "x.com.st.d.sensor.moisture") {
		capability "Battery"
		capability "Configuration"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
		capability "Water Sensor"
		capability "Power Source"
		capability "Health Check"

		attribute "syncStatus", "string"
		attribute "lastAlarmDate", "string"

		command "forceSync"

		fingerprint mfr: "010F", prod: "0B01", model: "1002"
		fingerprint mfr: "010F", prod: "0B01", model: "1003"
		fingerprint mfr: "010F", prod: "0B01", model: "2002"
		fingerprint mfr: "010F", prod: "0B01"
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "FGFS", type: "lighting", width: 6, height: 4) {
			tileAttribute("device.water", key: "PRIMARY_CONTROL") {
				attributeState("dry", label: "Alarm not detected", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/flood/flood0sensor.png", backgroundColor: "#79b821")
				attributeState("wet", label: "Alarm detected", icon: "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/flood/flood1sensor.png", backgroundColor: "#ffa81e")
			}

			tileAttribute("device.multiStatus", key: "SECONDARY_CONTROL") {
				attributeState("multiStatus", label: '${currentValue}')
			}

		}

		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label: '${currentValue}°',
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

		valueTile("batteryStatus", "device.batteryStatus", inactiveLabel: false, decoration: "flat", width: 4, height: 2) {
			state "val", label: '${currentValue}'
		}

		standardTile("syncStatus", "device.syncStatus", decoration: "flat", width: 2, height: 2) {
			def syncIconUrl = "http://fibaro-smartthings.s3-eu-west-1.amazonaws.com/keyfob/sync_icon.png"
			state "synced", label: 'OK', action: "forceSync", backgroundColor: "#00a0dc", icon: syncIconUrl
			state "pending", label: "Pending", action: "forceSync", backgroundColor: "#153591", icon: syncIconUrl
			state "inProgress", label: "Syncing", action: "forceSync", backgroundColor: "#44b621", icon: syncIconUrl
			state "incomplete", label: "Incomplete", action: "forceSync", backgroundColor: "#f1d801", icon: syncIconUrl
			state "failed", label: "Failed", action: "forceSync", backgroundColor: "#bc2323", icon: syncIconUrl
			state "force", label: "Force", action: "forceSync", backgroundColor: "#e86d13", icon: syncIconUrl
		}

		main "FGFS"
		details(["FGFS", "batteryStatus", "temperature", "syncStatus"])
	}

	preferences {
		input(
				title: "Fibaro Flood Sensor ZW5 manual",
				description: "Tap to view the manual.",
				image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/fs_icon.png",
				url: "http://manuals.fibaro.com/content/manuals/en/FGFS-101/FGFS-101-EN-T-v2.1.pdf",
				type: "href",
				element: "href"
		)

		parameterMap().each {
			getPrefsFor(it)
		}

		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

def installed(){
  sendEvent(name: "checkInterval", value: (21600*2)+10*60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

//UI Support functions
def getPrefsFor(parameter) {
	input(
			title: "${parameter.num}. ${parameter.title}",
			description: parameter.descr,
			type: "paragraph",
			element: "paragraph"
	)
	input(
			name: parameter.key,
			title: null,
			type: parameter.type,
			options: parameter.options,
			range: (parameter.min != null && parameter.max != null) ? "${parameter.min}..${parameter.max}" : null,
			defaultValue: parameter.def,
			required: false
	)
}

def updated() {

	if (state.lastUpdated && (now() - state.lastUpdated) < 2000) return

	logging("${device.displayName} - Executing updated()", "info")
	def cmds = []
	def cmdCount = 0

	parameterMap().each {
		if (settings."$it.key" == null || state."$it.key" == null) {
			state."$it.key" = [value: it.def as Integer, state: "notSynced"]
		}

		if (settings."$it.key" != null) {
			if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state == "notSynced") {
				state."$it.key".value = settings."$it.key" as Integer
				state."$it.key".state = "notSynced"
				cmdCount = cmdCount + 1
			}
		} else {
			if (state."$it.key".state == "notSynced") {
				cmdCount = cmdCount + 1
			}
		}
	}

	if (cmdCount > 0) {
		logging("${device.displayName} - sending config.", "info")
		sendEvent(name: "syncStatus", value: "pending")
	}

	state.lastUpdated = now()
}

def forceSync() {
	if (device.currentValue("syncStatus") != "force") {
		state.prevSyncState = device.currentValue("syncStatus")
		sendEvent(name: "syncStatus", value: "force")
	} else {
		if (state.prevSyncState != null) {
			sendEvent(name: "syncStatus", value: state.prevSyncState)
		} else {
			sendEvent(name: "syncStatus", value: "synced")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	log.debug "WakeUpNotification"
	def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
	def cmds = []
	def cmdsSet = []
	def cmdsGet = []
	def cmdCount = 0
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: true)]

	cmdsGet << zwave.batteryV1.batteryGet()
	cmdsGet << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)

	if (device.currentValue("syncStatus") != "synced") {

		parameterMap().each {
			if (device.currentValue("syncStatus") == "force") {
				state."$it.key".state = "notSynced"
			}

			if (state."$it.key"?.value != null && state."$it.key"?.state == "notSynced") {
				cmdsSet << zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$it.key".value, it.size), parameterNumber: it.num, size: it.size)
				cmdsGet << zwave.configurationV2.configurationGet(parameterNumber: it.num)
				cmdCount = cmdCount + 1
			}
		}

		log.debug "Not synced, syncing ${cmdCount} parameters"
		sendEvent(name: "syncStatus", value: "inProgress")
		runIn((5 + cmdCount * 1.5), syncCheck)

	}

	if (cmdsSet) {
		cmds = encapSequence(cmdsSet, 500)
		cmds << "delay 500"
	}

	cmds = cmds + encapSequence(cmdsGet, 1000)
	cmds << "delay " + (5000 + cmdCount * 1500)
	cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
	results = results + response(cmds)

	return results
}


def syncCheck() {
	logging("${device.displayName} - Executing syncCheck()", "info")
	def notSynced = []
	def count = 0

	if (device.currentValue("syncStatus") != "synced") {
		parameterMap().each {
			if (state."$it.key"?.state == "notSynced") {
				notSynced << it
				logging "Sync failed! Verify parameter: ${notSynced[0].num}"
				logging "Sync $it.key " + state."$it.key"
				sendEvent(name: "batteryStatus", value: "Sync incomplited! Check parameter nr. ${notSynced[0].num}")
				count = count + 1
			}
		}
	}
	if (count == 0) {
		logging("${device.displayName} - Sync Complete", "info")
		sendEvent(name: "syncStatus", value: "synced")
	} else {
		logging("${device.displayName} Sync Incomplete", "info")
		if (device.currentValue("syncStatus") != "failed") {
			sendEvent(name: "syncStatus", value: "incomplete")
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "ManufacturerSpecificReport"
	log.debug "manufacturerId:	 ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:		 ${cmd.productId}"
	log.debug "productTypeId:	 ${cmd.productTypeId}"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) {
	log.debug "DeviceSpecificReport"
	log.debug "deviceIdData:				${cmd.deviceIdData}"
	log.debug "deviceIdDataFormat:			${cmd.deviceIdDataFormat}"
	log.debug "deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}"
	log.debug "deviceIdType:				${cmd.deviceIdType}"
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {
	log.debug "VersionReport"
	log.debug "applicationVersion:		${cmd.applicationVersion}"
	log.debug "applicationSubVersion:	${cmd.applicationSubVersion}"
	log.debug "zWaveLibraryType:		${cmd.zWaveLibraryType}"
	log.debug "zWaveProtocolVersion:	${cmd.zWaveProtocolVersion}"
	log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.debug "BatteryReport"
	log.debug "cmd: "+cmd
	log.debug "location: "+location
	def timeDate = location.timeZone ? new Date().format("yyyy MMM dd EEE h:mm:ss a", location.timeZone) : new Date().format("yyyy MMM dd EEE h:mm:ss")

	if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
		sendEvent(name: "battery", value: 1, descriptionText: "${device.displayName} has a low battery", isStateChange: true)
	} else {
		sendEvent(name: "battery", value: cmd.batteryLevel, descriptionText: "Current battery level")
	}
	sendEvent(name: "batteryStatus", value: "Battery: $cmd.batteryLevel%\n($timeDate)")
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.debug "NotificationReport"
	def map = [:]
	def alarmInfo = "Last alarm detection: "
	if (cmd.notificationType == 5) {
		switch (cmd.event) {
			case 2:
				map.name = "water"
				map.value = "wet"
				map.descriptionText = "${device.displayName} is ${map.value}"
				state.lastAlarmDate = "\n"+new Date().format("yyyy MMM dd EEE HH:mm:ss")
				//state.lastAlarmDate = "\n"+new Date().format("yyyy MMM dd EEE HH:mm:ss", location.timeZone)
				multiStatusEvent(alarmInfo + state.lastAlarmDate)
				break

			case 0:
				map.name = "water"
				map.value = "dry"
				map.descriptionText = "${device.displayName} is ${map.value}"
				multiStatusEvent(alarmInfo + state.lastAlarmDate)
				break
		}
	} else if (cmd.notificationType == 7) {
		switch (cmd.event) {
			case 0:
				map.name = "tamper"
				map.value = "clear"
				map.descriptionText = "${device.displayName}: tamper alarm has been deactivated"
				sendEvent(name: "batteryStatus", value: "Tamper alarm inactive")
				break

			case 3:
				map.name = "tamper"
				map.value = "detected"
				map.descriptionText = "${device.displayName}: tamper alarm activated"
				sendEvent(name: "batteryStatus", value: "Tamper alarm activated")
				break
		}
	}
	createEvent(map)
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	log.debug "SensorMultilevelReport"
	def map = [:]
	if (cmd.sensorType == 1) {
		// temperature
		def cmdScale = cmd.scale == 1 ? "F" : "C"
		map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, 1)
		map.unit = getTemperatureScale()
		map.name = "temperature"
		map.displayed = true
		log.debug "Temperature:" + map.value
		createEvent(map)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.warn "Test10: DeviceResetLocallyNotification"
	log.info "${device.displayName}: received command: $cmd - device has reset itself"
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	log.warn cmd
}

/*
####################
## Z-Wave Toolkit ##
####################
*/

def parse(String description) {
	def result = []
	logging("${device.displayName} - Parsing: ${description}")
	if (description.startsWith("Err 106")) {
		result = createEvent(
				descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
		)
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, cmdVersions())
		if (cmd) {
			logging("${device.displayName} - Parsed: ${cmd}")
			zwaveEvent(cmd)
		}
	}
}

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find({ it.num == cmd.parameterNumber }).key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
	if (state."$paramKey".value == cmd.scaledConfigurationValue) {
		state."$paramKey".state = "synced"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract Secure command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract CRC16 command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn "Unable to extract MultiChannel command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	log.debug "Unhandled: ${cmd.toString()}"
	[:]
}

private logging(text, type = "debug") {
	if (settings.logging == "true") {
		log."$type" text
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd", "info")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd", "info")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	logging("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd", "info")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
	encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
	encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
	encap(encapMap.cmd, encapMap.ep)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) {
		secEncap(cmd)
	} else if (zwaveInfo.cc.contains("56")) {
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd", "info")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay = 250) {
	delayBetween(cmds.collect { encap(it) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
	def result = []
	size.times {
		result = result.plus(0, (value & 0xFF) as Short)
		value = (value >> 8)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	log.warn "Flood Sensor rejected configuration!"
	sendEvent(name: "syncStatus", value: "failed")
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.NetworkKeyVerify cmd) {
	log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecuritySchemeReport cmd) {
	log.debug cmd
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityCommandsSupportedReport cmd) {
	log.debug cmd
}

/*
##########################
## Device Configuration ##
##########################
*/

/*	0x31 : 5 - Sensor Multilevel
	0x56 : 1 - Crc16 Encap
	0x71 : 2 - Notification ST supported V3
	0x72 : 2 - Manufacturer Specific
	0x80 : 1 - Battery
	0x84: 2 - Wake Up
	0x85: 2 - Association
	0x86: 1 - Version
	0x98: 1 - Security */

private Map cmdVersions() {
	[0x31: 5, 0x56: 1, 0x71: 3, 0x72: 2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1]
}

def configure() {
	state.lastAlarmDate = "-"
	def cmds = []
	sendEvent(name: "water", value: "dry", displayed: "true")
	cmds += zwave.wakeUpV2.wakeUpIntervalSet(seconds: 21600, nodeid: zwaveHubNodeId)//FGFS' default wake up interval
	cmds += zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
	cmds += zwave.versionV1.versionGet()
	cmds += zwave.batteryV1.batteryGet()
	cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
	cmds += zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId])
	cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()
	encapSequence(cmds, 500)
}

private parameterMap() {
	[
			[key: "AlarmCancellationDelay", num: 1, size: 2, type: "number", def: 0, min: 0, max: 3600, title: "Alarm cancellation delay", descr: "Time period by which a Flood Sensor will retain the flood state after the flooding itself has ceased. 0-3600 (in seconds)"],
			[key: "AcousticVisualSignals", num: 2, size: 1, type: "enum", options: [
					0: "acoustic and visual alarms inactive",
					1: "acoustic alarm inactive, visual alarm active",
					2: "acoustic alarm active, visual alarm inactive",
					3: "acoustic and visual alarms active"],
			 def: 3, title: "Acoustic and visual signals on / off in case of flooding.", descr: ""],
			[key: "tempInterval", num: 10, size: 4, type: "number", def: 300, min: 1, max: 65535, title: "Interval of temperature measurements", descr: "How often the temperature will be measured (1-65535 in seconds)"],
			[key: "floodSensorOnOff", num: 77, size: 1, type: "enum", options: [
					0: "on",
					1: "off"],
			 def: 0, title: "Flood sensor functionality turned on/off", descr: "Allows to turn off the internal flood sensor. Tamper and built in temperature sensor will remain active."]
	]
}
