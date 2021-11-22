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
		fingerprint inClusters: "0000, 0001, 0003", outClusters: "0003, 0006", manufacturer: "eWeLink", model: "WB01", deviceJoinName: "eWeLink Button" //eWeLink Button
		fingerprint inClusters: "0000, 0001, 0003, 0020, FC57", outClusters: "0003, 0006, 0019", manufacturer: "eWeLink", model: "SNZB-01P", deviceJoinName: "eWeLink Button" //eWeLink Button
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
		def supportedButtons = ((isIkeaRemoteControl() && i == REMOTE_BUTTONS.MIDDLE) || isIkeaOpenCloseRemote() || isSomfy()) ? ["pushed"] : ["pushed", "held"]
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
	def supportedButtons = []

	if (isIkeaRemoteControl()) {
		numberOfButtons = 5
	} else if (isIkeaOnOffSwitch() || isIkeaOpenCloseRemote()) {
		numberOfButtons = 2
	} else if (isSomfySituo1()) {
		numberOfButtons = 3
	}  else if (isSomfySituo4()) {
		numberOfButtons = 12
	}

	if (numberOfButtons > 1) {
		createChildButtonDevices(numberOfButtons)
	}

	if (isIkeaOpenCloseRemote() || isSomfy()) {
		supportedButtons = ["pushed"]
	} else if (isEWeLink()) {
		supportedButtons = ["pushed", "held", "double"]
	} else {
		supportedButtons = ["pushed", "held"]
	}

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
		if ((description?.startsWith("catchall:")) || (description?.startsWith("read attr -"))) {
			def descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap.clusterInt == zigbee.POWER_CONFIGURATION_CLUSTER && descMap.attrInt == 0x0021) {
				def batteryValue = zigbee.convertHexToInt(descMap.value)
				if (!isIkea()) {
					batteryValue = batteryValue / 2
				}
				event = getBatteryEvent(batteryValue)
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
	} else if (isEWeLink()) {
		if (descMap.clusterInt == zigbee.ONOFF_CLUSTER) {
			buttonNumber = 1
			if (descMap.commandInt == 0x00) {
				buttonState = "held"
			} else if (descMap.commandInt == 0x01) {
				buttonState = "double"
			} else {
				buttonState = "pushed"
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

private boolean isEWeLink() {
	device.getDataValue("manufacturer") == "eWeLink"
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