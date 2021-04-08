/**
 *  Vision In-Wall 2Relays Switch (Models: ZL7435xx-5)
 * 
 *  Author:
 *    Lan, Kuo Wei
 *
 *	Product Link:
 *    http://www.visionsecurity.com.tw/index.php?option=product&lang=en&task=pageinfo&id=335&belongid=334&index=0
*/
metadata {
	definition(
		name: "Vision In-Wall 2Relays Switch", 
		namespace: "Vision_KuoWei", 
		author: "Lan, Kuo Wei", 
		ocfDeviceType: "oic.d.switch", 
		deviceTypeId: "Switch",
		mnmn: "0ALw", 
		vid: "812dd85f-ae1e-382e-9289-15cbc7c7fd6f"
	) {
		capability "Health Check"
		capability "Refresh"
		capability "Switch"
		capability "Configuration"
		// This DTH uses 2 switch endpoints. Parent DTH controls endpoint 1 so please use '1' at the end of deviceJoinName
		// Child device (isComponent : false) representing endpoint 2 will substitute 1 with 2 for easier identification.
		fingerprint manufacturer: "0109", prod: "2017", model: "171B", deviceJoinName: "Vision 2Relays Switch 1" //zw:Ls type:1001 mfr:0109 prod:2017 model:171B ver:16.11 zwv:4.38 lib:03 cc:98 sec:5E,72,86,85,59,70,5A,7A,60,8E,73,27,25 epc:2
		fingerprint manufacturer: "0109", prod: "2017", model: "171C", deviceJoinName: "Vision 2Relays Switch 1" //zw:Ls type:1001 mfr:0109 prod:2017 model:171C ver:25.07 zwv:4.54 lib:03 cc:98 sec:5E,72,86,85,59,70,5A,7A,60,8E,73,27,25 epc:2
	}

	preferences {
		parameterMap.each {
			input (title: it.name, description: it.description, type: "paragraph", element: "paragraph")
			switch(it.type) {
				case "enum":
					input(name: it.key, title: "Select", type: "enum", options: it.values, defaultValue: it.defaultValue, required: false)
					break
			}	
		}
	}
}

def installed() {
	/* Child device check */
	if(!childDevices) {
		def d = addChildDevice(
			"smartthings",
			"Z-Wave Binary Switch Endpoint",
			"${device.deviceNetworkId}-child",
			device.hubId,
			[
				completedSetup: true,
				label: "${device.displayName[0..-2]}2",
				isComponent: false
			]
		)
	}
	// Preferences template
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		def preferenceName = it.key + "Boolean"
		settings."$preferenceName" = true
		state.currentPreferencesState."$it.key".status = "synced"
	}
	firstCommand()
}

def firstCommand(){
	def commands = []
	commands << encap(0, zwave.configurationV1.configurationGet(parameterNumber: 0x01))
	commands << "delay 300"			
	commands << encap(0, zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 0x01, nodeId: [0x01,0x01]))
	sendHubCommand(commands, 100)
}

def updated() {
	parameterMap.each {	
		if (isPreferenceChanged(it)) {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "syncPending"
		} else if (!state.currentPreferencesState."$it.key".value) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
	}
	configure()
}

def configure() {
	// Device-Watch simply pings if no device events received for checkInterval duration of 32min = 2 * 15min + 2min lag time
	sendEvent(name: "checkInterval", value: 30 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	def commands = []
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".status == "syncPending") {
			commands << encap(0, zwave.configurationV1.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
			commands << "delay 300"			
			commands << encap(0, zwave.configurationV1.configurationGet(parameterNumber: it.parameterNumber))
		}
	}		
	response(commands + refresh())
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = zwaveEvent(cmd)
	}
	log.debug("'$description' parsed to $result")
	return createEvent(result)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, endpoint=null) {
	(endpoint == 1) ? [name: "switch", value: cmd.value ? "on" : "off"] : [:]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd, endpoint=null) {
	(endpoint == 1) ? [name: "switch", value: cmd.value ? "on" : "off"] : [:]
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	if (cmd.commandClass == 0x6C && cmd.parameter.size >= 4) { // Supervision encapsulated Message		
		cmd.parameter = cmd.parameter.drop(2)
		cmd.commandClass = cmd.parameter[0]
		cmd.command = cmd.parameter[1]
		cmd.parameter = cmd.parameter.drop(2)
	}
	def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x20: 1])
	if (cmd.sourceEndPoint == 1) {
		zwaveEvent(encapsulatedCommand, 1)
	} else { // sourceEndPoint == 2
		childDevices[0]?.handleZWave(encapsulatedCommand)
		[:]
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd, endpoint = null) {
	if (endpoint == null) log.debug("$device.displayName: $cmd")
	else log.debug("$device.displayName: $cmd endpoint: $endpoint")
}

def on() {
	// parent DTH controls endpoint 1
	def endpointNumber = 1
	delayBetween([
		encap(endpointNumber, zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)),
		encap(endpointNumber, zwave.switchBinaryV1.switchBinaryGet())
	])
}

def off() {
	def endpointNumber = 1
	delayBetween([
		encap(endpointNumber, zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)),
		encap(endpointNumber, zwave.switchBinaryV1.switchBinaryGet())
	])
}

// PING is used by Device-Watch in attempt to reach the Device
def ping() {
	refresh()
}

def refresh() {
	[encap(1, zwave.switchBinaryV1.switchBinaryGet()), encap(2, zwave.switchBinaryV1.switchBinaryGet())]
}

// sendCommand is called by endpoint 2 child device handler
def sendCommand(endpointDevice, commands) {
	def endpointNumber = 2
	def result
	if (commands instanceof String) {
		commands = commands.split(',') as List
	}
	result = commands.collect { encap(endpointNumber, it) }
	sendHubCommand(result, 100)
}

def encap(endpointNumber, cmd) {
	if (endpointNumber == 0) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	}
	else if (cmd instanceof physicalgraph.zwave.Command) {
		def cmdTemp = zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint: 0x01, destinationEndPoint: endpointNumber).encapsulate(cmd)
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmdTemp).format()
	} else {
		cmd.format()
	} 
}

private getParameterMap() {[[
		name: "Input switch type (Default: Toggle Switch)", key: "inputSwitchType", type: "enum",
		parameterNumber: 1, size: 1, defaultValue: 0,
		values: [
				0: "Toggle Switch",
				1: "Momentary Switch",
		],
		description: "This item can select input switch type."
	]
]}

private syncConfiguration() {
	def commands = []
	parameterMap.each {
		try {
			if (state.currentPreferencesState."$it.key".status == "syncPending") {
				commands << encap(0, zwave.configurationV1.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
				commands << "delay 300"			
				commands << encap(0, zwave.configurationV1.configurationGet(parameterNumber: it.parameterNumber))
		    }
		} catch (e) {
			log.warn "There's been an issue with preference: ${it.key}"
		}
	}
	sendHubCommand(commands, 100)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find({it.parameterNumber == cmd.parameterNumber})
	def key = preference.key
	def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)
	if (settings."$key" == preferenceValue) {
		state.currentPreferencesState."$key".value = settings."$key"
		state.currentPreferencesState."$key".status = "synced"
	} else {
		state.currentPreferencesState."$key"?.status = "syncPending"
		runIn(5, "syncConfiguration", [overwrite: true])
	}
}

private getPreferenceValue(preference, value = "default") {
	def integerValue = value == "default" ? preference.defaultValue : value.intValue()
	switch (preference.type) {
		case "enum":
			return String.valueOf(integerValue)
		default:
			return integerValue
	}
}

private getCommandValue(preference) {
	def parameterKey = preference.key
	switch (preference.type) {
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isPreferenceChanged(preference) {
	if (settings."$preference.key" != null) {
		return state.currentPreferencesState."$preference.key".value != settings."$preference.key"
	} else {
		return false
	}
}