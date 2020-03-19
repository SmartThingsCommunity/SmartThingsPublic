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
 *  Everspring Motion Sensor
 *
 *  Author: SmartThings
 */

metadata {
	definition (name: "Everspring Motion Sensor", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.sensor.motion", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Z-Wave") {
			capability "Motion Sensor"
			capability "Battery"
			capability "Health Check"
			capability "Tamper Alert"

			//RAW: zw:Ss type:0701 mfr:0060 prod:0001 model:0006 ver:1.00 zwv:4.61 lib:03 cc:5E,55,98 sec:86,85,59,72,5A,73,6C,7A,80,84,71,70 role:06 ff:8C07 ui:8C07                                                           
			fingerprint mfr: "0060", prod: "0001", model: "0006", deviceJoinName: "Everspring PIR Sensor" 
																				
		simulator {                                                                         
			status "inactive": "command: 3003, payload: 00"                                                                         
			status "active": "command: 3003, payload: FF"                                                                           
		}                                                                           
																				
		tiles(scale: 2) {                                                                           
			multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){                                                                            
				tileAttribute("device.motion", key: "PRIMARY_CONTROL") {                                                                            
					attributeState("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00A0DC")                                                                         
					attributeState("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#CCCCCC")                                                                          
				}                                                                           
			}                                                                           
			valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {                                                                         
				state("battery", label:'${currentValue}% battery', unit:"")                                                                         
			}                                                             
			valueTile("tamper", "device.tamper", height: 2, width: 2, decoration: "flat") {                                                                         
				state "clear", label: 'tamper clear', backgroundColor: "#ffffff"                                                                            
				state "detected", label: 'tampered', backgroundColor: "#ff0000"                                                                         
			}                                                                           
																				
			main "motion"                                                                           
			details(["motion", "battery", "tamper"])                                                                            
		}                                                                           
	} 

	preferences {
	parameterMap.each {
		input (
			title: it.name,
			description: it.description,
			type: "paragraph",
			element: "paragraph"
		)

		switch(it.type) {
			case "boolRange":
				input(
					name: it.key + "Boolean", 
					type: "bool",
					title: "Enable",
					description: "If you disable this option, it will overwrite setting below.",
					defaultValue: it.defaultValue != it.disableValue,
					required: false
				)
				input(
					name: it.key, 
					type: "number",
					title: "Set value (range ${it.range})",
					defaultValue: it.defaultValue,
					range: it.range,
					required: false
				)
				break
			case "boolean":
				input(
					type: "paragraph",
					element: "paragraph",
					description: "Option enabled: ${it.activeDescription}\n" + 
						"Option disabled: ${it.inactiveDescription}" 
				)
				input(
					name: it.key, 
					type: "boolean",
					title: "Enable",
					defaultValue: it.defaultValue == it.activeOption,
					required: false
				)
				break
			case "enum":
				input(
					name: it.key, 
					title: "Select",
					type: "enum",
					options: it.values,
					defaultValue: it.defaultValue,
					required: false
				)
				break
			case "range":
				input(
					name: it.key, 
					type: "number",
					title: "Set value (range ${it.range})",
					defaultValue: it.defaultValue,
					range: it.range,
					required: false
				)
				break
		}
	}
}
}

def installed() {
	sendEvent(name: "checkInterval", value: 4 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	sendEvent(name: "tamper", value: "clear", displayed: false)
	sendEvent(name: "motion", value: "inactive")
	state.currentPreferencesState = [:]
	parameterMap.each {
		state.currentPreferencesState."$it.key" = [:]
		state.currentPreferencesState."$it.key".value = getPreferenceValue(it)
		if (it.type == "boolRange" && getPreferenceValue(it) == it.disableValue) {
			state.currentPreferencesState."$it.key".status = "disablePending"
		} else {
			state.currentPreferencesState."$it.key".status = "synced"
		}
	}
	response(initialPoll())
}

def updated() {
	sendEvent(name: "checkInterval", value: 4 * 60 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	parameterMap.each {
		if (isPreferenceChanged(it)) {
			log.debug "Preference ${it.key} has been updated from value: ${state.currentPreferencesState."$it.key".value} to ${settings."$it.key"}"
			state.currentPreferencesState."$it.key".status = "syncPending"
			if (it.type == "boolRange") {
				def preferenceName = it.key + "Boolean"
				if (notNullCheck(settings."$preferenceName")) {
					if (!settings."$preferenceName") {
						state.currentPreferencesState."$it.key".status = "disablePending"
					} else if (state.currentPreferencesState."$it.key".status == "disabled") {
						state.currentPreferencesState."$it.key".status = "syncPending"
					}
				} else {
					state.currentPreferencesState."$it.key".status = "syncPending"
				}
			}
		} else if (!state.currentPreferencesState."$it.key".value) {
			log.warn "Preference ${it.key} no. ${it.parameterNumber} has no value. Please check preference declaration for errors."
		}
	}
	syncConfiguration()
}


private getCommandClassVersions() {
	[0x20: 1, 0x30: 1, 0x80: 1, 0x84: 2, 0x71: 3, 0x9C: 1]
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
		result = createEvent(descriptionText:description)
	} else {
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		} else {
			result = createEvent(value: description, descriptionText: description, isStateChange: false)
		}
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sensorValueEvent(cmd.value)
}

def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "motion", value: "active", descriptionText: "$device.displayName detected motion")
	} else {
		createEvent(name: "tamper", value: "clear")
		createEvent(name: "motion", value: "inactive", descriptionText: "$device.displayName motion has stopped")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []
	if (cmd.notificationType == 0x07) {
		switch(cmd.event) {
			case 0x03:
				result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName covering was removed", isStateChange: true)
				result << response(zwave.batteryV1.batteryGet())
				unschedule(clearTamper, [forceForLocallyExecuting: true])
				runIn(10, clearTamper, [forceForLocallyExecuting: true])
			break
			case 0x08:
				result << sensorValueEvent(1)
			break
			default:
				result << sensorValueEvent(0)
		}
	}
	result
}

def clearTamper() {
	sendEvent(name: "tamper", value: "clear")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	def commands = [state.rememberPreferences]
	if (!state.lastbat || (new Date().time) - state.lastbat > 53*60*60*1000) {
		commands += encap(zwave.batteryV1.batteryGet())
	} else {
		commands += encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
	}
	result += response(commands)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = new Date().time
	[createEvent(map), response(zwave.wakeUpV1.wakeUpNoMoreInformation())]
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(commandClassVersions)
	if (encapsulatedCommand) {
		state.sec = 1
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = commandClassVersions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "Configuration report: ${cmd}"
	def preference = parameterMap.find( {it.parameterNumber == cmd.parameterNumber} )
	def key = preference.key
	def preferenceValue = getPreferenceValue(preference, cmd.scaledConfigurationValue)
	if (settings."$key" == preferenceValue) {
		state.currentPreferencesState."$key".value = settings."$key"
		state.currentPreferencesState."$key".status = "synced"
	} else if (preference.type == "boolRange") {
		if (state.currentPreferencesState."$key".status == "disablePending" && preferenceValue == preference.disableValue) {
			state.currentPreferencesState."$key".status = "disabled"
		} else {
			runIn(5, "syncConfiguration", [overwrite: true])
		}
	} else {
		state.currentPreferencesState."$key"?.status = "syncPending"
		runIn(5, "syncConfiguration", [overwrite: true])
	}
}

def initialPoll() {
	def request = []
	request << zwave.batteryV1.batteryGet()
	request << zwave.notificationV3.notificationGet(notificationType: 0x07, event: 0x08)
	commands(request) + ["delay 20000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]
}

private commands(commands, delay=200) {
	log.info "sending commands: ${commands}"
	delayBetween(commands.collect{ command(it) }, delay)
}

private command(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo && zwaveInfo.zw?.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else if (zwaveInfo && zwaveInfo.cc?.contains("56")) {
		zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private syncConfiguration() {
	def commands = []
	parameterMap.each {
		if (state.currentPreferencesState."$it.key".status == "syncPending") {
			commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: getCommandValue(it), parameterNumber: it.parameterNumber, size: it.size))
			commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		} else if (state.currentPreferencesState."$it.key".status == "disablePending") {
			commands += encap(zwave.configurationV2.configurationSet(scaledConfigurationValue: it.disableValue, parameterNumber: it.parameterNumber, size: it.size))
			commands += encap(zwave.configurationV2.configurationGet(parameterNumber: it.parameterNumber))
		}
	}
	state.rememberPreferences = commands 
	commands
}

private getPreferenceValue(preference, value = "default") {
	def integerValue = value == "default" ? preference.defaultValue : value.intValue()
	switch (preference.type) {
		case "enum":
			return String.valueOf(integerValue)
		case "boolean":
			return String.valueOf(preference.optionActive == integerValue)
		default:
			return integerValue
	}
}

private getCommandValue(preference) {
	def parameterKey = preference.key
	switch (preference.type) {
		case "boolean":
			return settings."$parameterKey" ? preference.optionActive : preference.optionInactive
		case "boolRange":
			def parameterKeyBoolean = parameterKey + "Boolean"
			return !notNullCheck(settings."$parameterKeyBoolean") || settings."$parameterKeyBoolean" ? settings."$parameterKey" : preference.disableValue
		case "range":
			return settings."$parameterKey"
		default:
			return Integer.parseInt(settings."$parameterKey")
	}
}

private isPreferenceChanged(preference) {
	if (notNullCheck(settings."$preference.key")) {
		if (preference.type == "boolRange") {
			def boolName = preference.key + "Boolean"
			if (state.currentPreferencesState."$preference.key".status == "disabled") {
				return settings."$boolName"
			} else {
				return state.currentPreferencesState."$preference.key".value != settings."$preference.key" || !settings."$boolName"
			}
		} else {
			return state.currentPreferencesState."$preference.key".value != settings."$preference.key"
		}
	} else {
		return false
	}
}

private notNullCheck(value) {
	return value != null
}

private encap(physicalgraph.zwave.Command cmd) {
		if (zwaveInfo.zw.contains("s")) {
			zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
		} else {
			cmd.format()
		}
	}


private getParameterMap() {[
	[
		name: "Basic Set Level", key: "basicSetLevel", type: "range",
		parameterNumber: 1, size: 1, defaultValue: 99,
		range: "1..99", 
		description: "When Basic Set Command is sent where contains a value, the receiver will take it for consideration; for instance, if a lamp module is received the Basic Set command of which value is decisive as to how bright of dim level of lamp module shall be."
	],
	[
		name: "Sensitivity Level", key: "sensitivityLevel", type: "range",
		parameterNumber: 3, size: 1, defaultValue: 6,
		range: "1..10", 
		description: "In order to provide a best efficiency of the detector, it is recommended to test the detector with movements from a farthest end of the coverage area at first time of use.  If movements cannot be detected sensitively, simply adjust the sensitivity level with Configuration Parameter #3. This parameter can be configured with the value of 1 through 10, where 1 means low sensitivity and 10 means highest sensitivity."
	],
	[
		name: "Re-trigger Interval", key: "re-TriggerInterval", type: "range",
		parameterNumber: 4, size: 2, defaultValue: 180,
		range: "5..3600", 
		description: "The Configuration parameter that can be used to adjust the interval of being re-triggered after the detector has been triggered as Configuration Parameter #4.  No response will be made during this interval if a movement is presented.  The time interval can be set between 5 secs to 3600 secs."
	],
	[
		name: "Lux Level", key: "luxLevel", type: "range",
		parameterNumber: 5, size: 1, defaultValue: 20,
		range: "1..100", 
		description: "The user can set a detecting percentage of lux level which determines when the light sensor will be activated.  If percentage of lux level of ambient illumination falls below this percentage, and a person moves across or within the protected area, the detector will emit Z-Wave ON Command (i.e. Basic Set Command (Value = Basic Set Level)) to controller and activate connected modules and lighting.  Percentage can be set between 1% to 100%."
	],
	[
		name: "On-Off Duration", key: "on-OffDuration", type: "range",
		parameterNumber: 6, size: 2, defaultValue: 15,
		range: "5..3600", 
		description: "The function of on-off duration setting will be useful if the detector is connected with a module or lighting.  The duration determines how long the module/lighting should stay ON.  For instance, Lamp Module turns off 100 secs after it has been turned on.  This parameter can be configured with the value of 5 through 3600, where 5 means 5 second delay and 3600 means 3600 seconds of delay."
	]	
]}