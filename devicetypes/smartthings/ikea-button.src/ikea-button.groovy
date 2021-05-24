/**
 *  Ikea Button
 *
 *  Copyright 2018
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

import groovy.json.JsonOutput
import physicalgraph.zigbee.zcl.DataType
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus 

metadata {
	definition (name: "Ikea Button", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "x.com.st.d.remotecontroller", mcdSync: true) {
		capability "Actuator"
		capability "Battery"
		capability "Button"
		capability "Holdable Button"
		capability "Configuration"
		capability "Sensor"
		capability "Health Check"

		fingerprint inClusters: "0000, 0001, 0003, 0009, 0B05, 1000", outClusters: "0003, 0004, 0005, 0006, 0008, 0019, 1000", manufacturer: "IKEA of Sweden", model: "TRADFRI remote control", deviceJoinName: "IKEA Remote Control", mnmn: "SmartThings", vid: "SmartThings-smartthings-IKEA_TRADFRI_Remote_Control" //IKEA TRÅDFRI Remote
		fingerprint inClusters: "0000, 0001, 0003, 0009, 0102, 1000, FC7C", outClusters: "0003, 0004, 0006, 0008, 0019, 0102, 1000", manufacturer:"IKEA of Sweden", model: "TRADFRI on/off switch", deviceJoinName: "IKEA Remote Control", mnmn: "SmartThings", vid: "SmartThings-smartthings-IKEA_TRADFRI_On/Off_Switch" //IKEA TRÅDFRI On/Off switch
		fingerprint manufacturer: "IKEA of Sweden", model: "TRADFRI open/close remote", deviceJoinName: "IKEA Remote Control", mnmn: "SmartThings", vid: "SmartThings-smartthings-IKEA_TRADFRI_open/close_remote" // raw description 01 0104 0203 01 07 0000 0001 0003 0009 0020 1000 FC7C 07 0003 0004 0006 0008 0019 0102 1000 //IKEA TRÅDFRI Open/Close Remote
		fingerprint manufacturer: "KE", model: "TRADFRI open/close remote", deviceJoinName: "IKEA Remote Control", mnmn: "SmartThings", vid: "SmartThings-smartthings-IKEA_TRADFRI_open/close_remote" // raw description 01 0104 0203 01 07 0000 0001 0003 0009 0020 1000 FC7C 07 0003 0004 0006 0008 0019 0102 1000 //IKEA TRÅDFRI Open/Close Remote
		fingerprint manufacturer: "SOMFY", model: "Situo 4 Zigbee", deviceJoinName: "SOMFY Remote Control", mnmn: "SmartThings", vid: "SmartThings-smartthings-Somfy_Situo4_open/close_remote" // raw description 01 0104 0203 00 02 0000 0003 04 0003 0005 0006 0102
		fingerprint manufacturer: "SOMFY", model: "Situo 1 Zigbee", deviceJoinName: "SOMFY Remote Control", mnmn: "SmartThings", vid: "SmartThings-smartthings-Somfy_open/close_remote" // raw description 01 0104 0203 00 02 0000 0003 04 0003 0005 0006 0102
		fingerprint inClusters: "0000,0001,0003,0020", outClusters: "0003,0004,0006,0019", manufacturer: "ShinaSystem", model: "MSM-300Z", deviceJoinName: "SiHAS MSM-300ZB", mnmn: "0Ar2", vid: "ST_9639674b-8026-4f61-9579-585cd0fe1fad" // mnmn: "SmartThings", vid: "generic-4-button"
		fingerprint inClusters: "0000,0001,0003,0020,0500", outClusters: "0003,0004,0019", manufacturer: "ShinaSystem", model: "BSM-300Z", deviceJoinName: "SiHAS BSM-300ZB", mnmn: "SmartThings", vid: "SmartThings-smartthings-SmartSense_Button" // mnmn: "0Ar2", vid: "ST_af7cc6c2-92fc-4a27-b2f4-5c9afb5c7b75" 
	}

	tiles {
		standardTile("button", "device.button", width: 2, height: 2) {
			state "default", label: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			state "button 1 pushed", label: "pushed #1", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#00A0DC"
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		main (["button"])
		details(["button", "battery"])
	}
}

private getCLUSTER_GROUPS() { 0x0004 }
private getCLUSTER_SCENES() { 0x0005 }
private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getPOWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE() { 0x0020 }

private getREMOTE_BUTTONS() {
	[TOP:1,
	 RIGHT:2,
	 BOTTOM:3,
	 LEFT:4,
	 MIDDLE:5]
}

private getONOFFSWITCH_BUTTONS() {
	[TOP:2,
	 BOTTOM:1]
}

private getOPENCLOSE_BUTTONS() {
	[UP:1,
	 DOWN:2]
}

private getOPENCLOSESTOP_BUTTONS_ENDPOINTS() {
	[
		1: [UP:1,
			STOP:2,
			DOWN:3],
		2: [UP:4,
			STOP:5,
			DOWN:6],
		3: [UP:7,
			STOP:8,
			DOWN:9],
		4: [UP:10,
			STOP:11,
			DOWN:12]
	]
}

private getBUTTON_NUMBER_ENDPOINT() {
	[
			1: 1, 2: 1, 3: 1,
	 		4: 2, 5: 2, 6: 2,
			7: 3, 8: 3, 9: 3,
			10:4, 11: 4, 12: 4
	]
}

private channelNumber(String dni) {
	dni.split(":")[-1] as Integer
}

private getIkeaRemoteControlNames() {
	[
		"top button", //"Increase brightness button",
		"right button", //"Right button",
		"bottom button", //"Decrease brightness button",
		"left button", //"Left button",
		"middle button" //"On/Off button"
	]
}
private getIkeaOnOffSwitchNames() {
	[
		"bottom button", //"On button",
		"top button" //"Off button"
	]
}

private getOpenCloseRemoteNames() {
	[
		"Up", // Up button
		"Down" // Down button
	]
}

private getOpenCloseStopRemoteNames() {
	[
		"Up",	// Up button
		"Stop",	// Stop button
		"Down"	// Down button
	]
}

private getButtonLabel(buttonNum) {
	def label = "Button ${buttonNum}"

	if (isIkeaRemoteControl()) {
		label = ikeaRemoteControlNames[buttonNum - 1]
	} else if (isIkeaOnOffSwitch()) {
		label = ikeaOnOffSwitchNames[buttonNum - 1]
	} else if (isIkeaOpenCloseRemote()) {
		label = openCloseRemoteNames[buttonNum - 1]
	} else if (isSomfy()) {
		// UP, STOP, DOWN events in "Somfy Situo 4" come from 4 endpoints, so there are 12 child buttons
		// endpoint 1: buttons 1-3, enpoint 2: buttons 4-6, endpoint 3: buttons 7-9, endpoint 4: buttons 10-12
		// Situo 1 reports from endpoint 1 only
		def endpoint = BUTTON_NUMBER_ENDPOINT[buttonNum]
		def buttonNameIdx = (buttonNum - 1)%3
		def buttonName = openCloseStopRemoteNames[buttonNameIdx]
		label = "endpoint $endpoint $buttonName"
	}

	return label
}

private getButtonName(buttonNum) {
	return "${device.displayName} " + getButtonLabel(buttonNum)
}

private void createChildButtonDevices(numberOfButtons) {
	state.oldLabel = device.label

	log.debug "Creating $numberOfButtons children"

	for (i in 1..numberOfButtons) {
		log.debug "Creating child $i"
		def supportedButtons = (isBSM300() || isMSM300()) ? ["pushed","held","double"] : (((isIkeaRemoteControl() && i == REMOTE_BUTTONS.MIDDLE) || isIkeaOpenCloseRemote() || isSomfy()) ? ["pushed"] : ["pushed", "held"])
		def child = addChildDevice("Child Button", "${device.deviceNetworkId}:${i}", device.hubId,
				[completedSetup: true, label: getButtonName(i),
				 isComponent: true, componentName: "button$i", componentLabel: getButtonLabel(i)])

		child.sendEvent(name: "supportedButtonValues", value: supportedButtons.encodeAsJSON(), displayed: false)
		child.sendEvent(name: "numberOfButtons", value: 1, displayed: false)
		child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
	}
}

def installed() {
	def numberOfButtons = 1

	if (isIkeaRemoteControl()) {
		numberOfButtons = 5
	} else if (isIkeaOnOffSwitch() || isIkeaOpenCloseRemote()) {
		numberOfButtons = 2
	} else if (isSomfySituo1()) {
		numberOfButtons = 3
	}  else if (isSomfySituo4()) {
		numberOfButtons = 12
	} else if (isBSM300()) {
		numberOfButtons = 1
	} else if (isMSM300()) {
		numberOfButtons = 4
	}

	if (numberOfButtons > 1) {
		createChildButtonDevices(numberOfButtons)
	}

	def supportedButtons = (isBSM300() || isMSM300()) ? ["pushed","held","double"] : (isIkeaOpenCloseRemote() || isSomfy() ? ["pushed"] : ["pushed", "held"])
	sendEvent(name: "supportedButtonValues", value: supportedButtons.encodeAsJSON(), displayed: false)
	sendEvent(name: "numberOfButtons", value: numberOfButtons, displayed: false)
	numberOfButtons.times {
		sendEvent(name: "button", value: "pushed", data: [buttonNumber: it+1], displayed: false)
	}

	// These devices don't report regularly so they should only go OFFLINE when Hub is OFFLINE
	sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zigbee", scheme:"untracked"]), displayed: false)
}

def updated() {
	if (childDevices && device.label != state.oldLabel) {
		childDevices.each {
			def newLabel = getButtonName(channelNumber(it.deviceNetworkId))
			it.setLabel(newLabel)
		}
		state.oldLabel = device.label
	}
}

def configure() {
	log.debug "Configuring device ${device.getDataValue("model")}"

	def cmds = []

	if (isSomfy()) {
		cmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x21, ["destEndpoint":0xE8]) +
				zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x21, DataType.UINT8, 30, 21600, 0x01, ["destEndpoint":0xE8]) +
				zigbee.removeBinding(zigbee.ONOFF_CLUSTER, device.zigbeeId, 0x01, device.hub.zigbeeEui, 0x01) +
				zigbee.addBinding(CLUSTER_WINDOW_COVERING, ["destEndpoint":0x01])

		if (isSomfySituo4()) {
			cmds += zigbee.addBinding(CLUSTER_WINDOW_COVERING, ["destEndpoint":0x02]) +
					zigbee.addBinding(CLUSTER_WINDOW_COVERING, ["destEndpoint":0x03]) +
					zigbee.addBinding(CLUSTER_WINDOW_COVERING, ["destEndpoint":0x04])
		}
	} else if(isBSM300() || isMSM300()) {
		cmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE)
		if( isBSM300() ) {
			cmds += zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)        
		}
		cmds += zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE, DataType.UINT8, 30, 21600, 0x01/*100mv*1*/)
		if (isMSM300()) {    	
			cmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x01])
			cmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x02])
			cmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x03])
			cmds += zigbee.addBinding(zigbee.ONOFF_CLUSTER, ["destEndpoint":0x04])
		}
		else if (isBSM300()) {
			cmds += zigbee.addBinding(zigbee.IAS_ZONE_CLUSTER, ["destEndpoint":0x01])
		}
	} else {
		cmds += zigbee.readAttribute(zigbee.POWER_CONFIGURATION_CLUSTER, 0x21) +
				zigbee.configureReporting(zigbee.POWER_CONFIGURATION_CLUSTER, 0x21, DataType.UINT8, 30, 21600, 0x01) +
				zigbee.addBinding(zigbee.ONOFF_CLUSTER)
	}

	cmds += readDeviceBindingTable() // Need to read the binding table to see what group it's using
	cmds
}

def parse(String description) {
	log.debug "Parsing message from device: '$description'"
	def event = zigbee.getEvent(description)
	if (event) {
		log.debug "Creating event: ${event}"
		sendEvent(event)
	} else {
		if (description?.startsWith('zone status')) {
			event = parseIasMessage(description)
		} else if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
			def descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.attrInt == 0x0021) {
				def batteryValue = zigbee.convertHexToInt(descMap.value)
				if (!isIkea()) {
					batteryValue = batteryValue / 2
				}
				event = getBatteryEvent(batteryValue)
			} else if (descMap.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.attrInt == POWER_CONFIGURATION_BATTERY_VOLTAGE_ATTRIBUTE) {
				event = getBatteryResult(Integer.parseInt(descMap.value, 16))
			} else if (descMap.clusterInt == CLUSTER_SCENES ||
					descMap.clusterInt == zigbee.ONOFF_CLUSTER ||
					descMap.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER ||
					descMap.clusterInt == CLUSTER_WINDOW_COVERING) {
				event = getButtonEvent(descMap)
			}
		}

		def result = []
		if (event) {
			log.debug "Creating event: ${event}"
			result = createEvent(event)
		} else if (isBindingTableMessage(description)) {
			Integer groupAddr = getGroupAddrFromBindingTable(description)
			if (groupAddr != null) {
				List cmds = addHubToGroup(groupAddr)
				result = cmds?.collect { new physicalgraph.device.HubAction(it) }
			} else {
				groupAddr = 0x0000
				List cmds = addHubToGroup(groupAddr) +
						zigbee.command(CLUSTER_GROUPS, 0x00, "${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr, 4))} 00")
				result = cmds?.collect { new physicalgraph.device.HubAction(it) }
			}
		}

		return result
	}
}

private Map getBatteryEvent(value) {
	def result = [:]
	result.value = value
	result.name = 'battery'
	result.descriptionText = "${device.displayName} battery was ${result.value}%"
	return result
}

private Map getBatteryResult(rawValue) {
	def linkText = getLinkText(device)
	def result = [:]
	def volts = rawValue / 10
	if (!(rawValue == 0 || rawValue == 255)) {
		result.name = 'battery'
		result.translatable = true
		def minVolts =  2.3
		def maxVolts =  3.0
		// Get the current battery percentage as a multiplier 0 - 1
		def curValVolts = Integer.parseInt(device.currentState("battery")?.value ?: "100") / 100.0
		// Find the corresponding voltage from our range
		curValVolts = curValVolts * (maxVolts - minVolts) + minVolts
		// Round to the nearest 10th of a volt
		curValVolts = Math.round(10 * curValVolts) / 10.0
		// Only update the battery reading if we don't have a last reading,
		// OR we have received the same reading twice in a row
		// OR we don't currently have a battery reading
		// OR the value we just received is at least 2 steps off from the last reported value
		if (state?.lastVolts == null || state?.lastVolts == volts || device.currentState("battery")?.value == null || Math.abs(curValVolts - volts) > 0.1) {
		    def pct = (volts - minVolts) / (maxVolts - minVolts)
		    def roundedPct = Math.round(pct * 100)
		    if (roundedPct <= 0)
		        roundedPct = 1
		    result.value = Math.min(100, roundedPct)
		} else {
		    // Don't update as we want to smooth the battery values, but do report the last battery state for record keeping purposes
		    result.value = device.currentState("battery").value
		}
		result.descriptionText = "${device.displayName} battery was ${result.value}%"
		state.lastVolts = volts
	}
	return result
}

private sendButtonEvent(buttonNumber, buttonState) {
	def child = childDevices?.find { channelNumber(it.deviceNetworkId) == buttonNumber }

	if (child) {
		def descriptionText = "$child.displayName was $buttonState" // TODO: Verify if this is needed, and if capability template already has it handled

		child?.sendEvent([name: "button", value: buttonState, data: [buttonNumber: 1], descriptionText: descriptionText, isStateChange: true])
	} else {
		log.debug "Child device $buttonNumber not found!"
	}
}

private Map getButtonEvent(Map descMap) {
	Map ikeaRemoteControlMapping = [
			(zigbee.ONOFF_CLUSTER):
					[0x02: { [state: "pushed", buttonNumber: REMOTE_BUTTONS.MIDDLE] }],
			(zigbee.LEVEL_CONTROL_CLUSTER):
					[0x01: { [state: "held", buttonNumber: REMOTE_BUTTONS.BOTTOM] },
					 0x02: { [state: "pushed", buttonNumber: REMOTE_BUTTONS.BOTTOM] },
					 0x03: { [state: "", buttonNumber: 0] },
					 0x04: { [state: "", buttonNumber: 0] },
					 0x05: { [state: "held", buttonNumber: REMOTE_BUTTONS.TOP] },
					 0x06: { [state: "pushed", buttonNumber: REMOTE_BUTTONS.TOP] },
					 0x07: { [state: "", buttonNumber: 0] }],
			(CLUSTER_SCENES):
					[0x07: { it == "00"
						? [state: "pushed", buttonNumber: REMOTE_BUTTONS.RIGHT]
						: [state: "pushed", buttonNumber: REMOTE_BUTTONS.LEFT] },
					 0x08: { it == "00"
						? [state: "held", buttonNumber: REMOTE_BUTTONS.RIGHT]
						: [state: "held", buttonNumber: REMOTE_BUTTONS.LEFT] },
					 0x09: { [state: "", buttonNumber: 0] }]
	]

	def buttonState = ""
	def buttonNumber = 0
	Map result = [:]

	if (isIkeaRemoteControl()) {
		Map event = ikeaRemoteControlMapping[descMap.clusterInt][descMap.commandInt](descMap.data[0])
		buttonState = event.state
		buttonNumber = event.buttonNumber
	} else if (isIkeaOnOffSwitch()) {
		if (descMap.clusterInt == zigbee.ONOFF_CLUSTER) {
			buttonState = "pushed"
			if (descMap.commandInt == 0x00) {
				buttonNumber = ONOFFSWITCH_BUTTONS.BOTTOM
			} else if (descMap.commandInt == 0x01) {
				buttonNumber = ONOFFSWITCH_BUTTONS.TOP
			}
		} else if (descMap.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER) {
			buttonState = "held"
			if (descMap.commandInt == 0x01) {
				buttonNumber = ONOFFSWITCH_BUTTONS.BOTTOM
			} else if (descMap.commandInt == 0x05) {
				buttonNumber = ONOFFSWITCH_BUTTONS.TOP
			}
		}
	} else if (isIkeaOpenCloseRemote()){
		if (descMap.clusterInt == CLUSTER_WINDOW_COVERING) {
			buttonState = "pushed"
			if (descMap.commandInt == 0x00) {
				buttonNumber = OPENCLOSE_BUTTONS.UP
			} else if (descMap.commandInt == 0x01) {
				buttonNumber = OPENCLOSE_BUTTONS.DOWN
			}
		}
	} else if (isSomfy() && descMap.data?.size() == 0){
		// Somfy Situo Remotes query their shades directly after "My"(stop) button  is pressed (that's intended behavior)
		// descMap contains 'data':['00', '00'] in such cases, so we have to ignore those redundant misinterpreted UP events
		if (descMap.clusterInt == CLUSTER_WINDOW_COVERING) {
			buttonState = "pushed"
			def endpoint = Integer.parseInt(descMap.sourceEndpoint)
			if (descMap.commandInt == 0x00) {
				buttonNumber = OPENCLOSESTOP_BUTTONS_ENDPOINTS[endpoint].UP
			} else if (descMap.commandInt == 0x01) {
				buttonNumber = OPENCLOSESTOP_BUTTONS_ENDPOINTS[endpoint].DOWN
			} else if (descMap.commandInt == 0x02) {
				buttonNumber = OPENCLOSESTOP_BUTTONS_ENDPOINTS[endpoint].STOP
			}
		}
	} else if(isMSM300()) {
		buttonNumber = descMap.sourceEndpoint.toInteger()
		if (buttonNumber != 0) {
			if (descMap.commandInt == 0) {
				buttonState = "pushed"
			} else if (descMap.commandInt == 1) {
				buttonState = "double"
			} else if (descMap.commandInt == 2) {
				buttonState = "held"
			}
		}
	}
	if (buttonNumber != 0) {
		// Create old style
		def descriptionText = "${getButtonName(buttonNumber)} was $buttonState"
		result = [name: "button", value: buttonState, data: [buttonNumber: buttonNumber], descriptionText: descriptionText, isStateChange: true, displayed: false]

		// Create and send component event
		sendButtonEvent(buttonNumber, buttonState)
	}
	result
}

private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	if (zs.isAlarm1Set() && zs.isAlarm2Set()) {
		return getZoneButtonResult('held')
	} else if (zs.isAlarm1Set()) {
		return getZoneButtonResult('pushed')
	} else if (zs.isAlarm2Set()) {
		return getZoneButtonResult('double')
	} else { 
	}
}

private Map getZoneButtonResult(value) {
	def descriptionText
	if (value == "pushed")
		descriptionText = "${ device.displayName } was pushed"
	else if (value == "held")
		descriptionText = "${ device.displayName } was held"
	else
		descriptionText = "${ device.displayName } was pushed twice"
	
	sendEvent([name: "button", value: buttonState, data: [buttonNumber: 1], descriptionText: descriptionText, isStateChange: true])
		
	return [
			name           : 'button',
			value          : value,
			descriptionText: descriptionText,
			translatable   : true,
			isStateChange  : true,
			data           : [buttonNumber: 1]
	]
}

private boolean isIkeaRemoteControl() {
	device.getDataValue("model") == "TRADFRI remote control"
}

private boolean isIkeaOnOffSwitch() {
	device.getDataValue("model") == "TRADFRI on/off switch"
}

private boolean isIkeaOpenCloseRemote() {
	device.getDataValue("model") == "TRADFRI open/close remote"
}

private boolean isIkea() {
	isIkeaRemoteControl() || isIkeaOnOffSwitch() || isIkeaOpenCloseRemote()
}

private boolean isSomfy() {
	device.getDataValue("manufacturer") == "SOMFY"
}

private boolean isSomfySituo1() {
	isSomfy() && device.getDataValue("model") == "Situo 1 Zigbee"
}

private boolean isSomfySituo4() {
	isSomfy() && device.getDataValue("model") == "Situo 4 Zigbee"
}

private Boolean isBSM300() {
	device.getDataValue("model") == "BSM-300Z"
}

private Boolean isMSM300() {
	device.getDataValue("model") == "MSM-300Z"
}

private Integer getGroupAddrFromBindingTable(description) {
	log.info "Parsing binding table - '$description'"
	def btr = zigbee.parseBindingTableResponse(description)
	def groupEntry = btr?.table_entries?.find { it.dstAddrMode == 1 }
	if (groupEntry != null) {
		log.info "Found group binding in the binding table: ${groupEntry}"
		Integer.parseInt(groupEntry.dstAddr, 16)
	} else {
		log.info "The binding table does not contain a group binding"
		null
	}
}

private List addHubToGroup(Integer groupAddr) {
	["st cmd 0x0000 0x01 ${CLUSTER_GROUPS} 0x00 {${zigbee.swapEndianHex(zigbee.convertToHexString(groupAddr,4))} 00}",
	 "delay 200"]
}

private List readDeviceBindingTable() {
	["zdo mgmt-bind 0x${device.deviceNetworkId} 0",
	 "delay 200"]
}