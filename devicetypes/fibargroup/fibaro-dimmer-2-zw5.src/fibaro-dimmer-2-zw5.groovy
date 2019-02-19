/**
 *	Fibaro Dimmer 2
 */
metadata {
	definition (name: "Fibaro Dimmer 2 ZW5", namespace: "FibarGroup", author: "Fibar Group", runLocally: true, minHubCoreVersion: '000.025.0000', executeCommandsLocally: true, mnmn: "SmartThings", vid:"generic-dimmer-power-energy") {
		capability "Switch"
		capability "Switch Level"
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"

		command "reset"
		command "clearError"

		attribute "errorMode", "string"

		fingerprint mfr: "010F", prod: "0102", model: "2000"
		fingerprint mfr: "010F", prod: "0102", model: "1000"
		fingerprint mfr: "010F", prod: "0102", model: "3000"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 3, height: 4, canChangeIcon: false){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "off", label: 'Off', action: "on", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/dimmer/dimmer0.png", backgroundColor: "#ffffff", nextState:"turningOn"
				attributeState "on", label: 'On', action: "off", icon: "https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/dimmer/dimmer100.png", backgroundColor: "#00a0dc", nextState:"turningOff"
				attributeState "turningOn", label:'Turning On', action:"off", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/dimmer/dimmer50.png", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'Turning Off', action:"on", icon:"https://s3-eu-west-1.amazonaws.com/fibaro-smartthings/dimmer/dimmer50.png", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute("device.multiStatus", key:"SECONDARY_CONTROL") {
				attributeState("combinedMeter", label:'${currentValue}')
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "power", label:'${currentValue}\nW', action:"refresh"
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "energy", label:'${currentValue}\nkWh', action:"refresh"
		}
		valueTile("reset", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "reset", label:'reset\nkWh', action:"reset"
		}
		valueTile("scene", "device.scene", decoration: "flat", width: 2, height: 2) {
			 state "default", label:'Scene: ${currentValue}'
		}

		standardTile("errorMode", "device.errorMode", decoration: "flat", width: 2, height: 2) {
			state "default", label:'No errors.', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ffffff"
			state "overheat", label:'Overheat!', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ff0000"
			state "surge", label:'Surge!', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ff0000"
			state "voltageDrop", label:'Voltage drop!', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ff0000"
			state "overcurrent", label:'Overcurrent!', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ff0000"
			state "overload", label:'Overload!', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ff0000"
			state "loadError", label:'Load Error!', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ff0000"
			state "hardware", label:'Hardware Error!', action:"clearError", icon: "st.secondary.tools", backgroundColor: "#ff0000"
		}

		main "switch"
		details(["switch","power", "energy", "reset", "errorMode", "scene"])

	}

	preferences {
		input (
				title: "Fibaro Dimmer 2 ZW5 manual",
				description: "Tap to view the manual.",
				image: "http://manuals.fibaro.com/wp-content/uploads/2017/02/d2_icon.png",
				url: "http://manuals.fibaro.com/content/manuals/en/FGD-212/FGD-212-EN-T-v1.3.pdf",
				type: "href",
				element: "href"
		)

		parameterMap().each {
			input (
					title: "${it.num}. ${it.title}",
					description: it.descr,
					type: "paragraph",
					element: "paragraph"
			)

			input (
					name: it.key,
					title: null,
					type: it.type,
					options: it.options,
					range: (it.min != null && it.max != null) ? "${it.min}..${it.max}" : null,
					defaultValue: it.def,
					required: false
			)
		}

		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

def on() { encap(zwave.basicV1.basicSet(value: 255)) }

def off() { encap(zwave.basicV1.basicSet(value: 0)) }

def setLevel(level, rate = null ) {
	logging("${device.displayName} - Executing setLevel( $level, $rate )","info")
	level = Math.max(Math.min(level, 99), 0)
	if (level == 0) {
		sendEvent(name: "switch", value: "off")
	} else {
		sendEvent(name: "switch", value: "on")
	}
	if (rate == null) {
		encap(zwave.basicV1.basicSet(value: level))
	} else {
		encap(zwave.switchMultilevelV3.switchMultilevelSet(value: (level > 0) ? level-1 : 0, dimmingDuration: rate))
	}
}

def reset() {
	logging("${device.displayName} - Executing reset()","info")
	def cmds = []
	cmds << zwave.meterV3.meterReset()
	cmds << zwave.meterV3.meterGet(scale: 0)
	encapSequence(cmds,1000)
}

def refresh() {
	logging("${device.displayName} - Executing refresh()","info")
	def cmds = []
	cmds << zwave.meterV3.meterGet(scale: 0)
	cmds << zwave.switchMultilevelV1.switchMultilevelGet()
	encapSequence(cmds,1000)
}

def clearError() {
	logging("${device.displayName} - Executing clearError()","info")
	sendEvent(name: "errorMode", value: "clear")
}

def ping(){
	refresh()
}

def installed(){
	log.debug "installed()"
	sendEvent(name: "checkInterval", value: 1920, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	response(refresh())
}

def configure(){
	  sendEvent(name: "switch", value: "off", displayed: "true") //set the initial state to off.
}

def updated() {
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	logging("${device.displayName} - Executing updated()","info")
	runIn(3, "syncStart")
	state.lastUpdated = now()
}

def syncStart() {
	boolean syncNeeded = false
	parameterMap().each {
		if(settings."$it.key" != null) {
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] }
			if (state."$it.key".value != settings."$it.key" as Integer || state."$it.key".state in ["notSynced","inProgress"]) {
				state."$it.key".value = settings."$it.key" as Integer
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
	if ( syncNeeded ) {
		logging("${device.displayName} - starting sync.", "info")
		multiStatusEvent("Sync in progress.", true, true)
		syncNext()
	}
}

private syncNext() {
	logging("${device.displayName} - Executing syncNext()","info")
	def cmds = []
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"
			cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
			break
		}
	}
	if (cmds) {
		runIn(10, "syncCheck")
		sendHubCommand(cmds,1000)
	} else {
		runIn(1, "syncCheck")
	}
}

private syncCheck() {
	logging("${device.displayName} - Executing syncCheck()","info")
	def failed = []
	def incorrect = []
	def notSynced = []
	parameterMap().each {
		if (state."$it.key"?.state == "incorrect" ) {
			incorrect << it
		} else if ( state."$it.key"?.state == "failed" ) {
			failed << it
		} else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
			notSynced << it
		}
	}
	if (failed) {
		logging("${device.displayName} - Sync failed! Check parameter: ${failed[0].num}","info")
		sendEvent(name: "syncStatus", value: "failed")
		multiStatusEvent("Sync failed! Check parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		logging("${device.displayName} - Sync mismatch! Check parameter: ${incorrect[0].num}","info")
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync mismatch! Check parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		logging("${device.displayName} - Sync incomplete!","info")
		sendEvent(name: "syncStatus", value: "incomplete")
		multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
	} else {
		logging("${device.displayName} - Sync Complete","info")
		sendEvent(name: "syncStatus", value: "synced")
		multiStatusEvent("Sync OK.", true, true)
	}
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } )?.key
	logging("${device.displayName} - Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state?."$paramKey"?.value, "info")
	state."$paramKey"?.state = (state."$paramKey"?.value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
	syncNext()
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("${device.displayName} - rejected request!","warn")
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.state == "inProgress" ) {
			state."$param.key"?.state = "failed"
			break
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logging("${device.displayName} - BasicReport received, ignored, value: ${cmd.value}","info")
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	logging("${device.displayName} - SwitchMultilevelReport received, value: ${cmd.value}","info")
	sendEvent(name: "switch", value: (cmd.value > 0) ? "on" : "off")
	sendEvent(name: "level", value: (cmd.value == 99) ? 100 : cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	logging("${device.displayName} - SensorMultilevelReport received, $cmd","info")
	if ( cmd.sensorType == 4 ) {
		sendEvent(name: "power", value: cmd.scaledSensorValue, unit: "W")
		multiStatusEvent("${(device.currentValue("power") ?: "0.0")} W | ${(device.currentValue("energy") ?: "0.00")} kWh")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	logging("${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale} ep: $ep","info")
	switch (cmd.scale) {
		case 0:
			sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"])
			break
		case 2:
			sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"])
			break
	}
	multiStatusEvent("${(device.currentValue("power") ?: "0.0")} W | ${(device.currentValue("energy") ?: "0.00")} kWh")
}


def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	logging("zwaveEvent(): Scene Activation Set received: ${cmd}","trace")
	def result = []
	result << createEvent(name: "scene", value: "$cmd.sceneId", data: [switchType: "$settings.param20"], descriptionText: "Scene id ${cmd.sceneId} was activated", isStateChange: true)
	logging("Scene #${cmd.sceneId} was activated.","info")

	return result
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
	logging("${device.displayName} - CentralSceneNotification received, sceneNumber: ${cmd.sceneNumber} keyAttributes: ${cmd.keyAttributes}","info")
	log.info cmd
	def String action
	def Integer button
	switch (cmd.sceneNumber as Integer) {
		case [10,11,16]: action = "pushed"; button = 1; break
		case 14: action = "pushed"; button = 2; break
		case [20,21,26]: action = "pushed"; button = 3; break
		case 24: action = "pushed"; button = 4; break
		case 25: action = "pushed"; button = 5; break
		case 12: action = "held"; button = 1; break
		case 22: action = "held"; button = 3; break
		case 13: action = "released"; button = 1; break
		case 23: action = "released"; button = 3; break
	}
	log.info "button $button $action"
	sendEvent(name: "button", value: action, data: [buttonNumber: button], isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	logging("${device.displayName} - NotificationReport received for ${cmd.event}, parameter value: ${cmd.eventParameter[0]}", "info")
	switch (cmd.notificationType) {
		case 4:
			switch (cmd.event) {
				case 0: sendEvent(name: "errorMode", value: "clear"); break;
				case [1,2]: sendEvent(name: "errorMode", value: "overheat"); break;
			}; break;
		case 8:
			switch (cmd.event) {
				case 0: sendEvent(name: "errorMode", value: "clear"); break;
				case 4: sendEvent(name: "errorMode", value: "surge"); break;
				case 5: sendEvent(name: "errorMode", value: "voltageDrop"); break;
				case 6: sendEvent(name: "errorMode", value: "overcurrent"); break;
				case 8: sendEvent(name: "errorMode", value: "overload"); break;
				case 9: sendEvent(name: "errorMode", value: "loadError"); break;
			}; break;
		case 9:
			switch (cmd.event) {
				case 0: sendEvent(name: "errorMode", value: "clear"); break;
				case [1,3]: sendEvent(name: "errorMode", value: "hardware"); break;
			}; break;
		default: logging("${device.displayName} - Unknown zwaveAlarmType: ${cmd.zwaveAlarmType}","warn");
	}
}

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

private logging(text, type = "debug") {
	if (settings.logging == "true") {
		log."$type" text
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
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
	} else if (zwaveInfo.cc.contains("56")){
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
	delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
	def result = []
	size.times {
		result = result.plus(0, (value & 0xFF) as Short)
		value = (value >> 8)
	}
	return result
}
private Map cmdVersions() {
	[0x5E: 1, 0x86: 1, 0x72: 2, 0x59: 1, 0x73: 1, 0x22: 1, 0x31: 5, 0x32: 3, 0x71: 3, 0x56: 1, 0x98: 1, 0x7A: 2, 0x20: 1, 0x5A: 1, 0x85: 2, 0x26: 3, 0x8E: 2, 0x60: 3, 0x70: 2, 0x75: 2, 0x27: 1]
}

private parameterMap() {[
		[key: "autoStepTime", num: 6, size: 2, type: "enum", options: [
				1: "10 ms",
				2: "20 ms",
				3: "30 ms",
				4: "40 ms",
				5: "50 ms",
				10: "100 ms",
				20: "200 ms"
		], def: "1", min: 0, max: 255 , title: " Automatic control - time of a dimming step", descr: "This parameter defines the time of single dimming step during the automatic control."],
		[key: "manualStepTime", num: 8, size: 2, type: "enum", options: [
				1: "10 ms",
				2: "20 ms",
				3: "30 ms",
				4: "40 ms",
				5: "50 ms",
				10: "100 ms",
				20: "200 ms"
		], def: "5", min: 0, max: 255 , title: "Manual control - time of a dimming step", descr: "This parameter defines the time of single dimming step during the manual control."],
		[key: "autoOff", num: 10, size: 2, type: "number", def: 0, min: 0, max: 32767 , title: "Timer functionality (auto - off)",
		 descr: "This parameter allows to automatically switch off the device after specified time from switching on the light source. It may be useful when the Dimmer 2 is installed in the stairway. (1-32767 sec)"],
		[key: "autoCalibration", num: 13, size: 1, type: "enum", options: [
				0: "readout",
				1: "force auto-calibration of the load without FIBARO Bypass 2",
				2: "force auto-calibration of the load with FIBARO Bypass 2"
		], def: "0", min: 0, max: 2 , title: "Force auto-calibration", descr: "Changing value of this parameter will force the calibration process. During the calibration parameter is set to 1 or 2 and switched to 0 upon completion."],
		[key: "switchType", num: 20, size: 1, type: "enum", options: [
				0: "momentary switch",
				1: "toggle switch",
				2: "roller blind switch"
		], def: "0", min: 0, max: 2 , title: "Switch type", descr: "Choose between momentary, toggle and roller blind switch. "],
		[key: "threeWaySwitch", num: 26, size: 1, type: "enum", options: [
				0: "disabled",
				1: "enabled"
		], def: "0", min: 0, max: 1 , title: "The function of 3-way switch", descr: "Switch no. 2 controls the Dimmer 2 additionally (in 3-way switch mode). Function disabled for parameter 20 set to 2 (roller blind switch)."],
		[key: "sceneActivation", num: 28, size: 1, type: "enum", options: [
				0: "disabled",
				1: "enabled"
		], def: "0", min: 0, max: 1 , title: "Scene activation functionality", descr: "SCENE ID depends on the switch type configurations."],
		[key: "loadControllMode", num: 30, size: 1, type: "enum", options: [
				0: "forced leading edge control",
				1: "forced trailing edge control",
				2: "control mode selected automatically (based on auto-calibration)"
		], def: "2", min: 0, max: 2 , title: "Load control mode", descr: "This parameter allows to set the desired load control mode. The device automatically adjusts correct control mode, but the installer may force its change using this parameter."],
		[key: "levelCorrection", num: 38, size: 2, type: "number", def: 255, min: 0, max: 255 , title: "Brightness level correction for flickering loads",
		 descr: "Correction reduces spontaneous flickering of some capacitive load (e.g. dimmable LEDs) at certain brightness levels in 2-wire installation. In countries using ripple-control, correction may cause changes in brightness. In this case it is necessary to disable correction or adjust time of correction for flickering loads. (1-254 – duration of correction in seconds. For further information please see the manual)"]
]}
