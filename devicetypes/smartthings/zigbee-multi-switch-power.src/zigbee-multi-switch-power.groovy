/*
 *  Copyright 2019 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

metadata {
	definition(name: "ZigBee Multi Switch Power", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.smartplug", mnmn: "SmartThings", vid: "generic-switch-power") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Switch"
		capability "Power Meter"

		command "childOn", ["string"]
		command "childOff", ["string"]

		fingerprint manufacturer: "Aurora", model: "DoubleSocket50AU", deviceJoinName: "AURORA Outlet 1" //profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0008, 0B04", outClusters: "0019" //AURORA SMART DOUBLE SOCKET 1
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.light.on", backgroundColor: "#00A0DC", nextState: "turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.light.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute("power", key: "SECONDARY_CONTROL") {
				attributeState "power", label: '${currentValue} W'
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "switch"
		details(["switch", "refresh", "power"])
	}
}

def installed() {
	log.debug "Installed"
	updateDataValue("onOff", "catchall")
	createChildDevices()
}

def updated() {
	log.debug "Updated"
	updateDataValue("onOff", "catchall")
	refresh()
}

def parse(String description) {
	Map eventMap = zigbee.getEvent(description)
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)

	if (eventMap) {
		if (eventDescMap?.sourceEndpoint == "01" || eventDescMap?.endpoint == "01") {
			sendEvent(eventMap)
		} else {
			def childDevice = childDevices.find {
				it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.sourceEndpoint}" || it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.endpoint}"
			}
			if (childDevice) {
				childDevice.sendEvent(eventMap)
			} else {
				log.debug "Child device: $device.deviceNetworkId:${eventDescMap.sourceEndpoint} was not found"
			}
		}
	}
}

private void createChildDevices() {
	def numberOfChildDevices = modelNumberOfChildDevices[device.getDataValue("model")]
	log.debug("createChildDevices(), numberOfChildDevices: ${numberOfChildDevices}")

	for(def endpoint : 2..numberOfChildDevices) {
		try {
			log.debug "creating endpoint: ${endpoint}"
			addChildDevice("Child Switch Health Power", "${device.deviceNetworkId}:0${endpoint}", device.hubId,
				[completedSetup: true,
				 label: "${device.displayName[0..-2]}${endpoint}",
				 isComponent: false
				])
		} catch(Exception e) {
			log.debug "Exception: ${e}"
		}
	}
}

def on() {
	zigbee.on()
}

def off() {
	zigbee.off()
}

def childOn(String dni) {
	def childEndpoint = getChildEndpoint(dni)
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: childEndpoint])
}

def childOff(String dni) {
	def childEndpoint = getChildEndpoint(dni)
	zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: childEndpoint])
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	refresh()
}

def refresh() {
	def refreshCommands = zigbee.onOffRefresh() + zigbee.electricMeasurementPowerRefresh()
	def numberOfChildDevices = modelNumberOfChildDevices[device.getDataValue("model")]
	for(def endpoint : 2..numberOfChildDevices) {
		refreshCommands += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, 0x0000, [destEndpoint: endpoint])
		refreshCommands += zigbee.readAttribute(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x050B, [destEndpoint: endpoint])
	}
	log.debug "refreshCommands: $refreshCommands"
	return refreshCommands
}

def configure() {
	log.debug "configure"
	configureHealthCheck()
	def numberOfChildDevices = modelNumberOfChildDevices[device.getDataValue("model")]
	def configurationCommands = zigbee.onOffConfig(0, 120) + zigbee.electricMeasurementPowerConfig()
	for(def endpoint : 2..numberOfChildDevices) {
		configurationCommands += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, 0x0000, 0x10, 0, 120, null, [destEndpoint: endpoint])
		configurationCommands += zigbee.configureReporting(zigbee.ELECTRICAL_MEASUREMENT_CLUSTER, 0x050B, 0x29, 1, 600, 0x0005, [destEndpoint: endpoint])
	}
	configurationCommands << refresh()
	log.debug "configurationCommands: $configurationCommands"
	return configurationCommands
}

def configureHealthCheck() {
	log.debug "configureHealthCheck"
	Integer hcIntervalMinutes = 12
	def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
	sendEvent(healthEvent)
	childDevices.each {
		it.sendEvent(healthEvent)
	}
}

private getChildEndpoint(String dni) {
	dni.split(":")[-1] as Integer
}

private getModelNumberOfChildDevices() {
	[
		"DoubleSocket50AU" : 2
	]
}