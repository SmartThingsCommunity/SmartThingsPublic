/**
 *  Zooz Zen24 Toggle Dimmer v3
 *
 * Revision History:
 * 2019-03-24 - Initial release
 * 2019-04-13 - Added Scene Controls
 * 2019-07-12 - Added functions for v3.01
 * 2019-09-07 - Fix typo in auto turn off timer parameter setting
 * 2019-11-16 - Updated with latest device parameters, changed handling of double tap
 * 2019-12-10 - Clean up, update command class versions, fix for single tap scene control
 * 2020-02-03 - Fix for zero value in ramp rate
 * 2020-02-13 - Fix for double tap settings
 *
 *   Parm Size Description                                   Value
 *      1    1 Toggle Control                                0 (Default)-Toggle up turns light on, 1-Toggle down turns light on, 2-toggle either way toggles light status
 *      3    1 Auto Turn-Off                                 0 (Default)-Timer disabled, 1-Timer enabled; Set time in parameter 4
 *      4    4 Turn-off Timer                                60 (Default)-Time in minutes after turning on to automatically turn off (1-65535 minutes)
 *      5    1 Auto Turn-On                                  0 (Default)-Timer disabled, 1-Timer enabled; Set time in parameter 6
 *      6    4 Turn-on Timer                                 60 (Default)-Time in minutes after turning off to automatically turn on (1-65535 minutes)
 *      8    1 Power Restore                                 2 (Default)-Remember state from pre-power failure, 0-Off after power restored, 1-On after power restore
 *      9    1 Physical Ramp Rate Control                    1 (Default)-Ramp rate in seconds to reach full brightness or off (0-99 seconds)
 *     10    1 Minimum Brightness                            1 (Default)-Minimum brightness that light will set (1-99%)
 *     11    1 Maximum Brightness                            99 (Default)-Maximum brightness that light will set (1-99%)
 *     12    1 Double Tap                                    0 (Default)-Light will go to full brightness with double tap, 1-light will go to max set in Parameter 11 with double tap 
 *     13    1 Scene Control                                 0 (Default)-Scene control disabled, 1-Scene control enabled
 *     14    1 Disable Double Tap                            0 (Default)-Double tap to full/max brightness, 1-double tap disabled-single tap to last brightness, 2-double tap disable-single tap to full/max brightness
 *     15    1 Disable toggle                                1 (Default)-Toggle is used for local control, 0-toggle disabled
 *     16    1 Physical Dimming Speed                        4 (Default)-Time in seconds to from 0 to 100% brightness, 1-99-dimming time
 *     17    1 Zwave Ramp Type                               1 (Default)-Zwave ramp speed set through command class, 0-Zwave ramp speed matches parameter 9
 *     18    1 Default Brightness                            0 (Default)-Last brightness level, 1-99-custom brightness level
 */

metadata {
	definition (name: "Zooz Zen24 Toggle Dimmer v3", namespace: "doncaruana", author: "Don Caruana", ocfDeviceType: "oic.d.light", mnmn: "SmartThings", vid: "generic-dimmer") {
		capability "Switch Level"
		capability "Actuator"
		capability "Health Check"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Light"
		capability "Button"
			command "tapDown1"
			command "tapDown2"
			command "tapDown3"
			command "tapDown4"
			command "tapDown5"
			command "tapUp1"
			command "tapUp2"
			command "tapUp3"
			command "tapUp4"
			command "tapUp5"


 //zw:L type:1101 mfr:027A prod:B112 model:261C ver:3.00 zwv:5.03 lib:03 cc:5E,26,85,8E,59,55,86,72,5A,73,70,5B,9F,6C,7A role:05 ff:8600 ui:8604

	fingerprint mfr:"027A", prod:"B112", model:"261C", ff:"8600", deviceJoinName: "Zooz Zen24 Toggle Dimmer v3"
	fingerprint deviceId:"0x1101", inClusters: "0x26,0x55,0x59,0x5A,0x5B,0x5E,0x6C,0x70,0x72,0x73,0x7A,0x85,0x86,0x8E,0x9F"
	fingerprint cc: "0x26,0x55,0x59,0x5A,0x5B,0x5E,0x6C,0x70,0x72,0x73,0x7A,0x85,0x86,0x8E,0x9F", mfr:"027A", prod:"B112", model:"261C", deviceJoinName: "Zooz Zen24 Toggle Dimmer v3"

	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	preferences {
		input "toggleControl", "enum", title: "Toggle Control", description: "Standard, Inverted, or Toggle", options:["std": "Standard", "invert": "Invert", "toggle": "Toggle"], defaultValue: "std"
		input "rampRate", "number", title: "Ramp Rate", description: "Seconds to reach full brightness (1-99)", required: false, defaultValue: 1, range: "1..99"
		input "powerRestore", "enum", title: "After Power Restore", description: "State after power restore", options:["prremember": "Remember", "proff": "Off", "pron": "On"],defaultValue: "prremember",displayDuringSetup: false
		input "sceneCtrl", "bool", title: "Scene Control", description: "Enable scene control", required: false, defaultValue: false
		input "doubleTap", "enum", title: "Double Tap Behavior", description: "Double/Single Tap behavior", options:["tap2full":"Double Tap to Full brightness", "tap2max": "Double Tap to Custom Max brightness", "tap1last": "Double Tap disabled, Single tap to last brightness", "tap1max" : "Double Tap disabled, Single tap to full brightness"],defaultValue: "tap1full",displayDuringSetup: false
		input "autoTurnoff", "bool", title: "Auto Off", description: "Light will automatically turn off after set time", required: false, defaultValue: false
		input "autoTurnon", "bool", title: "Auto On", description: "Light will automatically turn on after set time", required: false, defaultValue: false
		input "offTimer", "number", title: "Off Timer", description: "Time in minutes to automatically turn off", required: false, defaultValue: 60, range: "1..65535"
		input "onTimer", "number", title: "On Timer", description: "Time in minutes to automatically turn on", required: false, defaultValue: 60, range: "1..65535"
		input "maxBright", "number", title: "Maximum Brightness", description: "Maximum brightness that the light can go to", required: false, defaultValue: 99, range: "1..99"
		input "minBright", "number", title: "Minimum Brightness", description: "Minimum brightness that the light can go to", required: false, defaultValue: 1, range: "1..99"
		input "physDefBright", "number", title: "Physical On Brightness", description: "0 for last level or 1-99", required: false, defaultValue: 0, range: "0..99"
		input "localControl", "enum", title: "Local Control", description: "Local paddle control enabled", options:["lcOn": "Local and Zwave On/Off enabled", "lcOff": "Disable local control", "lcAllOff": "Local and Zwave On/Off disabled"], defaultValue: "lcOn",displayDuringSetup: false
		input "physDimspeed", "number", title: "Physical Dimming Speed", description: "Dimming time from 0 to 99", required: false, defaultValue: 4, range: "1..99"
		input "zwaveOntype", "enum", title: "Zwave Ramp Rate Control", description: "Software Controlled", options:["zwcontrolphys": "Matches Physical Rate", "zwcontrollog": "Controlled by Ramp Type and Ramp Rate parameters"], required: false, defaultValue: zwcontrollog
		input "zwaveramptype","enum",title: "Zwave Ramp Type", description: "Instant, Seconds, Minutes, Switch Default", options:["zwinstant": "Instant", "zwsec": "Seconds", "zwmin": "Minutes", "zwdef" : "Default"],defaultValue: "zwdef",displayDuringSetup: false
		input "zwaverampspeed", "number", title: "Zwave Ramp Rate", description: "Time in seconds or minutes to reach brightness setting (as set in Zwave Ramp Type)", required: false, defaultValue: 4, range: "1..127"
		input (
					type: "paragraph",
					element: "paragraph",
					title: "Configure Association Groups:",
					description: "Devices in association group 2 will receive Basic Set commands directly from the switch when it is turned on or off. Use this to control another device as if it was connected to this switch.\n\n" +"Devices are entered as a comma delimited list of IDs in hexadecimal format."
					)
		input (
					name: "requestedGroup2",
					title: "Association Group 2 Members (Max of 5):",
					type: "text",
					required: false
					)
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		valueTile("level", "device.level", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}

		main(["switch"])
		details(["switch", "level", "refresh"])

	}
}

def installed() {
	log.debug "installed"

	def cmds = []
// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	cmds << mfrGet()
	cmds << zwave.versionV1.versionGet().format()
	cmds << parmGet(1)
	cmds << parmGet(3)
	cmds << parmGet(4)
	cmds << parmGet(5)
	cmds << parmGet(6)
	cmds << parmGet(8)
	cmds << parmGet(9)
	cmds << parmGet(10)
	cmds << parmGet(11)
	cmds << parmGet(12)
	cmds << parmGet(13)
	cmds << parmGet(14)
	cmds << parmGet(15)
	cmds << parmGet(16)
	cmds << parmGet(17)
	cmds << parmGet(18)

	def level = 99
	cmds << zwave.basicV1.basicSet(value: level).format()
	cmds << zwave.switchMultilevelV1.switchMultilevelGet().format()
	return response(delayBetween(cmds,200))
}

def updated(){
	// These are needed as SmartThings is not honoring defaultValue in preferences. They are set to the device defaults
	def setPhysDefBright = 0
	if (physDefBright) {setPhysDefBright = physDefBright}
	def setPhysdimspeed = 4
	if (physDimspeed) {setPhysdimspeed = physDimspeed}
	def setZwaveontype = 1
	if (zwaveOntype!=null) {setZwaveontype = zwaveOntype == "zwcontrollog" ? 1 : 0}
	def setLocalControl = 1
	if (localControl!=null) {setLocalControl = localControl == "lcOn" ? 1 : localControl == "lcOff" ? 0 : 2}
	def setOffTimer = 60
	if (offTimer) {setOffTimer = offTimer}
	def setOnTimer = 60
	if (onTimer) {setOnTimer = onTimer}
	def setRampRate = 1
	if (rampRate!=null) {setRampRate = rampRate}
	def setMaxBright = 99
	if (maxBright) {setMaxBright = maxBright}
	def setMinBright = 1
	if (minBright) {setMinBright = minBright}
	def nodes = []
	def commands = []
	if (getDataValue("MSR") == null) {
		def level = 99
		commands << mfrGet()
		commands << zwave.versionV1.versionGet().format()
		commands << zwave.basicV1.basicSet(value: level).format()
		commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	}
	def setScene = sceneCtrl == true ? 1 : 0
	def setDoubleTap = 0
	def setDtapDisable = 0
	switch (doubleTap) {
		case "tap2full":
			setDoubleTap = 0
			setDtapDisable = 0
			break
		case "tap2max":
			setDoubleTap = 1
			setDtapDisable = 0
			break
		case "tap1last":
			setDoubleTap = 0
			setDtapDisable = 1
			break
		case "tap1max":
			setDoubleTap = 0
			setDtapDisable = 2
			break
		default:
			setDoubleTap = 0
			setDtapDisable = 0
			break
	}
	def setPowerRestore = 2
	if (powerRestore != null) {setPowerRestore = powerRestore == "prremember" ? 2 : powerRestore == "proff" ? 0 : 1}
	def setAutoTurnon = autoTurnon == true ? 1 : 0
	def setAutoTurnoff = autoTurnoff == true ? 1 : 0
	def setToggleControl = 0
	switch (toggleControl) {
		case "std":
			setToggleControl = 0
			break
		case "invert":
			setToggleControl = 1
			break
		case "toggle":
			setToggleControl = 2
			break
		default:
			setToggleControl = 0
			break
	}
	def setZwRType = zwdef
	if (zwaveramptype != null) {setZwRType = zwaveramptype}
	state.dimDuration = 255
	if (zwaverampspeed != null && setZwaveontype == 1) {
		switch (setZwRType) {
			case "zwdef":
				state.dimDuration = 255
				break
			case "zwsec":
				state.dimDuration = zwaverampspeed
				break
			case "zwmin":
				state.dimDuration = zwaverampspeed + 127
				break
			case "zwinstant":
				state.dimDuration = 0
				break
		}
	}

	if (setScene) {
		sendEvent(name: "numberOfButtons", value: 10, displayed: false)
	} else {    
		sendEvent(name: "numberOfButtons", value: 0, displayed: false)
	}

	if (settings.requestedGroup2 != state.currentGroup2) {
		nodes = parseAssocGroupList(settings.requestedGroup2, 2)
		commands << zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: []).format()
		commands << zwave.associationV2.associationSet(groupingIdentifier: 2, nodeId: nodes).format()
		commands << zwave.associationV2.associationGet(groupingIdentifier: 2).format()
		state.currentGroup2 = settings.requestedGroup2
	}

	//parmset takes the parameter number, it's size, and the value - in that order
	commands << parmSet(18, 1, setPhysDefBright)
	commands << parmSet(17, 1, setZwaveontype)
	commands << parmSet(16, 1, setPhysdimspeed)
	commands << parmSet(15, 1, setLocalControl)
	commands << parmSet(14, 1, setDtapDisable)
	commands << parmSet(13, 1, setScene)
	commands << parmSet(12, 1, setDoubleTap)
	commands << parmSet(11, 1, setMaxBright)
	commands << parmSet(10, 1, setMinBright)
	commands << parmSet(9, 1, setRampRate)
	commands << parmSet(8, 1, setPowerRestore)
	commands << parmSet(6, 4, setOnTimer)
	commands << parmSet(5, 1, setAutoTurnon)
	commands << parmSet(4, 4, setOffTimer)
	commands << parmSet(3, 1, setAutoTurnoff)
	commands << parmSet(1, 1, setToggleControl)

	commands << parmGet(18)
	commands << parmGet(17)
	commands << parmGet(16)
	commands << parmGet(15)
	commands << parmGet(14)
	commands << parmGet(13)
	commands << parmGet(12)
	commands << parmGet(11)
	commands << parmGet(10)
	commands << parmGet(9)
	commands << parmGet(8)
	commands << parmGet(6)
	commands << parmGet(5)
	commands << parmGet(4)
	commands << parmGet(3)
	commands << parmGet(1)

	// Device-Watch simply pings if no device events received for 32min(checkInterval)
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	return response(delayBetween(commands, 500))
}

private getCommandClassVersions() {
	[
		0x59: 1,  // AssociationGrpInfo
		0x85: 2,  // Association
		0x5B: 1,  // Central Scene
		0x5A: 1,  // DeviceResetLocally
		0x72: 2,  // ManufacturerSpecific
		0x73: 1,  // Powerlevel
		0x86: 1,  // Version
		0x5E: 2,  // ZwaveplusInfo
		0x26: 2,  // Multilevel Switch
		0x70: 1,  // Configuration
		0x55: 1,  // Transport Service
		0x6C: 1,  // Supervision
		0x7A: 1,  // Firmware Update Metadata
		0x8E: 1,  // Multi Channel Association
		0x20: 1,  // Basic
		0x27: 1,  // All Switch
		0x9F: 1,  // S2
	]
}

def parse(String description) {
	def result = null
	// UI Trick to make 99%, which is actually the platform limit, show 100% on the control
	if (description.indexOf('command: 2603, payload: 63 63 00') > -1) {
		description = description.replaceAll('payload: 63 63 00','payload: 64 64 00')
	}
	if (description.indexOf('command: 2003, payload: 63') > -1) {
		description = description.replaceAll('payload: 63','payload: 64')
	}
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
	log.debug "---ASSOCIATION REPORT V2--- ${device.displayName} sent groupingIdentifier: ${cmd.groupingIdentifier} maxNodesSupported: ${cmd.maxNodesSupported} nodeId: ${cmd.nodeId} reportsToFollow: ${cmd.reportsToFollow}"
	state.group3 = "1,2"
	if (cmd.groupingIdentifier == 3) {
		if (cmd.nodeId.contains(zwaveHubNodeId)) {
			createEvent(name: "numberOfButtons", value: 10, displayed: false)
		}
		else {
			sendEvent(name: "numberOfButtons", value: 0, displayed: false)
				sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()))
				sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationGet(groupingIdentifier: 3).format()))
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv2.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv2.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
//		result << createEvent(name: "level", value: cmd.value, unit: "%")
		result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value)
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def name = ""
	def value = ""
	def reportValue = cmd.configurationValue[0]
	log.debug "---CONFIGURATION REPORT V1--- ${device.displayName} parameter ${cmd.parameterNumber} with a byte size of ${cmd.size} is set to ${cmd.configurationValue}"
	switch (cmd.parameterNumber) {
		case 1:
			name = "toggleCtrl"
			switch (reportValue) {
				case 0:
					value = "up"
					break
				case 1:
					value = "down"
					break
				case 2:
					value = "toggle"
					break
				default:
					break
			}
			break
		case 3:
			name = "autooff"
			value = reportValue == 1 ? "true" : "false"
			break
		case 4:
			name = "autoofftimer"
			value = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			break
		case 5:
			name = "autoon"
			value = reportValue == 1 ? "true" : "false"
			break
		case 6:
			name = "autoontimer"
			value = cmd.configurationValue[3] + (cmd.configurationValue[2] * 0x100) + (cmd.configurationValue[1] * 0x10000) + (cmd.configurationValue[0] * 0x1000000)
			break
		case 8:
			name = "afterfailure"
			switch (reportValue) {
				case 0:
					value = "off"
					break
				case 1:
					value = "on"
					break
				case 2:
					value = "remember"
					break
				default:
					break
			}
			break
		case 9:
			name = "rampspeed"
			value = reportValue
			break
		case 10:
			name = "minbrightness"
			value = reportValue
			break
		case 11:
			name = "maxbrightness"
			value = reportValue
			break
		case 12:
			name = "double_tap"
			value = reportValue == 1 ? "true" : "false"
			break
		case 13:
			name = "scene_control"
			value = reportValue == 1 ? "true" : "false"
			break
		case 14:
			name = "double_tap_disable"
			switch (reportValue) {
				case 0:
					value = "off"
					break
				case 1:
					value = "single_max"
					break
				case 2:
					value = "single_full"
					break
				default:
					value = "off"
					break
			}
			break
		case 15:
			name = "local_control"
			value = reportValue == 1 ? "true" : "false"
			break
		case 16:
			name = "man_dim_speed"
			value = reportValue
			break
		case 17:
			name = "zwave_ramp_type"
			value = reportValue == 1 ? "command" : "parm9"
			break
		case 18:
			name = "def_brightness"
			value = reportValue
			break
		default:
			break
	}
	createEvent([name: name, value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def manufacturerCode = String.format("%04X", cmd.manufacturerId)
	def productTypeCode = String.format("%04X", cmd.productTypeId)
	def productCode = String.format("%04X", cmd.productId)
	def msr = manufacturerCode + "-" + productTypeCode + "-" + productCode
	updateDataValue("MSR", msr)
	updateDataValue("Manufacturer", "Zooz")
	updateDataValue("Manufacturer ID", manufacturerCode)
	updateDataValue("Product Type", productTypeCode)
	updateDataValue("Product Code", productCode)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
// value of 255 restores previous brightness level
	def level = 255
	def dimmingDuration = state.dimDuration
	log.debug "dimmingDuration: $dimmingDuration"
	delayBetween([
			zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],2000)
}

def off() {
	def level = 0
	def dimmingDuration = state.dimDuration
	log.debug "dimmingDuration: $dimmingDuration"
	delayBetween([
			zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],2000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 2000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
		if (getDataValue("MSR") == null) {
			commands << mfrGet()
			commands << zwave.versionV1.versionGet().format()
		}
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	delayBetween(commands,100)
}


def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	def result = []
	switch (cmd.sceneNumber) {
		case 1:
			// Down
			switch (cmd.keyAttributes) {
				case 0:
					// Press Once
						result += createEvent(tapDown1Response("physical"))
						break
					case 3: 
						// 2 Times
						result +=createEvent(tapDown2Response("physical"))
						break
					case 4:
						// 3 times
						result=createEvent(tapDown3Response("physical"))
						break
					case 5:
						// 4 times
						result=createEvent(tapDown4Response("physical"))
						break
					case 6:
						// 5 times
						result=createEvent(tapDown5Response("physical"))
						break
					default:
						log.debug ("unexpected down press keyAttribute: $cmd.keyAttributes")
				}
				break
		case 2:
			// Up
			switch (cmd.keyAttributes) {
				case 0:
					// Press Once
					result += createEvent(tapUp1Response("physical"))
					break
				case 3: 
					// 2 Times
						result+=createEvent(tapUp2Response("physical"))
					break
				case 4:
					// 3 Times
					result=createEvent(tapUp3Response("physical"))
					break
				case 5:
					// 4 Times
					result=createEvent(tapUp4Response("physical"))
					break
				case 6:
					// 5 Times
					result=createEvent(tapUp5Response("physical"))
					break
				default:
					log.debug ("unexpected up press keyAttribute: $cmd.keyAttributes")
			}
			break
		default:
			// unexpected case
			log.debug ("unexpected scene: $cmd.sceneNumber")
			log.debug ("unexpected scene: $cmd")
	}
}

def buttonEvent(button, value) {
	createEvent(name: "button", value: value, data: [buttonNumber: button], descriptionText: "$device.displayName button $button was $value", isStateChange: true)
}

def tapDown1Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "2"], descriptionText: "$device.displayName Tap-Down-1 (button 2) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown2Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "4"], descriptionText: "$device.displayName Tap-Down-2 (button 4) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown3Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "6"], descriptionText: "$device.displayName Tap-Down-3 (button 6) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown4Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "8"], descriptionText: "$device.displayName Tap-Down-4 (button 8) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown5Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "10"], descriptionText: "$device.displayName Tap-Down-5 (button 10) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp1Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "1"], descriptionText: "$device.displayName Tap-Up-1 (button 1) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp2Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "3"], descriptionText: "$device.displayName Tap-Up-2 (button 3) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp3Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "5"], descriptionText: "$device.displayName Tap-Up-3 (button 5) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp4Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "7"], descriptionText: "$device.displayName Tap-Up-4 (button 7) pressed", isStateChange: true, type: "$buttonType"]
}

def tapUp5Response(String buttonType) {
	[name: "button", value: "pushed", data: [buttonNumber: "9"], descriptionText: "$device.displayName Tap-Up-5 (button 9) pressed", isStateChange: true, type: "$buttonType"]
}

def tapDown1() {
	sendEvent(tapDown1Response("digital"))
}

def tapDown2() {
	sendEvent(tapDown2Response("digital"))
}

def tapDown3() {
	sendEvent(tapDown3Response("digital"))
}

def tapDown4() {
	sendEvent(tapDown4Response("digital"))
}

def tapDown5() {
	sendEvent(tapDown5Response("digital"))
}

def tapUp1() {
	sendEvent(tapUp1Response("digital"))
}

def tapUp2() {
	sendEvent(tapUp2Response("digital"))
}

def tapUp3() {
	sendEvent(tapUp3Response("digital"))
}

def tapUp4() {
	sendEvent(tapUp4Response("digital"))
}

def tapUp5() {
	sendEvent(tapUp5Response("digital"))
}


def zwaveEvent(physicalgraph.zwave.commands.firmwareupdatemdv2.FirmwareMdReport cmd) { 
	log.debug ("received Firmware Report")
	log.debug "checksum:       ${cmd.checksum}"
	log.debug "firmwareId:     ${cmd.firmwareId}"
	log.debug "manufacturerId: ${cmd.manufacturerId}"
	[:]
}


def parmSet(parmnum, parmsize, parmval) {
	return zwave.configurationV1.configurationSet(scaledConfigurationValue: parmval, parameterNumber: parmnum, size: parmsize).format()
}

def parmGet(parmnum) {
	return zwave.configurationV1.configurationGet(parameterNumber: parmnum).format()
}

def mfrGet() {
	return zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
	updateDataValue("applicationVersion", "${cmd.applicationVersion}")
	updateDataValue("applicationSubVersion", "${cmd.applicationSubVersion}")
	updateDataValue("zWaveLibraryType", "${cmd.zWaveLibraryType}")
	updateDataValue("zWaveProtocolVersion", "${cmd.zWaveProtocolVersion}")
	updateDataValue("zWaveProtocolSubVersion", "${cmd.zWaveProtocolSubVersion}")
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionCommandClassReport cmd) {
	log.debug "vccr"
	def rcc = ""
	log.debug "version: ${cmd.commandClassVersion}"
	log.debug "class: ${cmd.requestedCommandClass}"
	rcc = Integer.toHexString(cmd.requestedCommandClass.toInteger()).toString() 
	log.debug "${rcc}"
	if (cmd.commandClassVersion > 0) {log.debug "0x${rcc}_V${cmd.commandClassVersion}"}
}

private parseAssocGroupList(list, group) {
	def nodes = group == 2 ? [] : [zwaveHubNodeId]
	if (list) {
		def nodeList = list.split(',')
		def max = group == 2 ? 5 : 4
		def count = 0

		nodeList.each { node ->
			node = node.trim()
			if ( count >= max) {
				log.warn "Association Group ${group}: Number of members is greater than ${max}! The following member was discarded: ${node}"
			}
			else if (node.matches("\\p{XDigit}+")) {
				def nodeId = Integer.parseInt(node,16)
				if (nodeId == zwaveHubNodeId) {
					log.warn "Association Group ${group}: Adding the hub as an association is not allowed (it would break double-tap)."
				}
				else if ( (nodeId > 0) & (nodeId < 256) ) {
					nodes << nodeId
					count++
				}
				else {
					log.warn "Association Group ${group}: Invalid member: ${node}"
				}
			}
			else {
				log.warn "Association Group ${group}: Invalid member: ${node}"
			}
		}
	}
	return nodes
}