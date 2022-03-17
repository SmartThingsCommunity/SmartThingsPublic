/**
 *  IOT8Z
 *
 *  Copyright 2021 Luis Contreras
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

metadata {
	definition (name: "IOT8-Z", namespace: "Smartenit", author: "Luis Contreras", cstHandler: true, mnmn: "SmartThingsCommunity", vid: "6d510b74-469c-3fa0-be7a-ec0894d38dcb") {
		capability "Contact Sensor"
		capability "Configuration"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"
		capability "Switch"
		capability "monthpublic25501.analogSensor"

		command "childOn", ["string"]
		command "childOff", ["string"]

		fingerprint manufacturer: "Smartenit, Inc", model: "IOT8-Z", deviceJoinName: "Smartenit Switch", profileId: "0104", inClusters: "0000, 0003, 0006, 000C, 000F", outClusters: "0019" 
	}
}

private getANALOG_INPUT_CLUSTER() { 0x000C }
private getBINARY_INPUT_CLUSTER() { 0x000F }
private getPRESENT_VALUE_ATTRIBUTE() { 0x0055 }
private getONOFF_ATTRIBUTE() { 0x0000 }

def installed() {
	log.debug "Installed"
	createChildDevices()
}

def updated() {
	log.debug "Updated"
	refresh()
}

// parse events into attributes
def parse(String description) {	
	Map eventMap = zigbee.getEvent(description)
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)

	if (eventMap) {
		if ((eventDescMap?.sourceEndpoint == "01") || (eventDescMap?.endpoint == "01")) {
			if (eventDescMap?.clusterInt == ANALOG_INPUT_CLUSTER) {
				return createEvent(name: "inputValue", value: eventDescMap?.value)
			} else {
				sendEvent(eventMap)
			}
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
	} else if (eventDescMap) {
		if ((eventDescMap?.sourceEndpoint == "01") || (eventDescMap?.endpoint == "01")) {
			if (eventDescMap?.clusterInt == BINARY_INPUT_CLUSTER) {
				if (eventDescMap?.value == "00") {
					return createEvent(name: "contact", value: "open")
				} else if (eventDescMap?.value == "01") {
					return createEvent(name: "contact", value: "closed")
				}
			} else if (eventDescMap?.clusterInt == ANALOG_INPUT_CLUSTER) {
				long convertedValue = Long.parseLong(eventDescMap?.value, 16)
				Float percentage = Float.intBitsToFloat(convertedValue.intValue())
				percentage = (percentage / 1.60) * 100.0
				def ceilingVal = Math.ceil(percentage)
				if (ceilingVal > 100.0) {
					ceilingVal = 100.0
				}

				int intValue = (int) ceilingVal
				return createEvent(name: "inputValue", value: intValue)
			}
		} else {
			def childDevice = childDevices.find {
				it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.sourceEndpoint}" || it.deviceNetworkId == "$device.deviceNetworkId:${eventDescMap.endpoint}"
			}
			if (childDevice) {
				if (eventDescMap?.clusterInt == BINARY_INPUT_CLUSTER) {
					if (eventDescMap?.value == "00") {
						def map = createEvent(name: "contact", value: "open")
						childDevice.sendEvent(map)
					} else if (eventDescMap?.value == "01") {
						def map = createEvent(name: "contact", value: "closed")
						childDevice.sendEvent(map)
					}
				} else if (eventDescMap?.clusterInt == ANALOG_INPUT_CLUSTER) {
					long convertedValue = Long.parseLong(eventDescMap?.value, 16)
					Float percentage = Float.intBitsToFloat(convertedValue.intValue())
					percentage = (percentage / 1.60) * 100.0
					def ceilingVal = Math.ceil(percentage)
					if (ceilingVal > 100.0) {
						ceilingVal = 100.0
					}

					int intValue = (int) ceilingVal

					def map = createEvent(name: "inputValue", value: intValue)
					childDevice.sendEvent(map)
				}
			}
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

def ping() {
	refresh()
}

def refresh() {
	def refreshCommands = zigbee.onOffRefresh() 
	def numberOfChildDevices = 8

	for (def endpoint : 2..numberOfChildDevices) {
		refreshCommands += zigbee.readAttribute(zigbee.ONOFF_CLUSTER, ONOFF_ATTRIBUTE, [destEndpoint: endpoint])
	}
	for (def endpoint : 1..4) {
		refreshCommands += zigbee.readAttribute(BINARY_INPUT_CLUSTER, PRESENT_VALUE_ATTRIBUTE, [destEndpoint: endpoint])
	}

	refreshCommands += zigbee.readAttribute(ANALOG_INPUT_CLUSTER, PRESENT_VALUE_ATTRIBUTE, [destEndpoint: 0x0001]);
	refreshCommands += zigbee.readAttribute(ANALOG_INPUT_CLUSTER, PRESENT_VALUE_ATTRIBUTE, [destEndpoint: 0x0002]);
	log.debug "refreshCommands: $refreshCommands"

	return refreshCommands
}

private void createChildDevices() {
	def numberOfChildDevices = 8

	for (def endpoint: 2..numberOfChildDevices) {
		try {
			if (endpoint == 2) {
				addChildDevice("Smartenit", "IOT8-Z-child-analog-contact-switch", "${device.deviceNetworkId}:0${endpoint}", device.hubId,
					[completedSetup: true,
					label: "${device.displayName} ${endpoint}",
					isComponent: false
					])
			} else if (endpoint >= 3 && endpoint <= 4) {
				addChildDevice("Smartenit", "IOT8-Z-child-contact-switch", "${device.deviceNetworkId}:0${endpoint}", device.hubId,
					[completedSetup: true,
					label: "${device.displayName} ${endpoint}",
					isComponent: false
					])
			} else if (endpoint >= 5 && endpoint <= 8) {
				addChildDevice("smartthings", "Child Switch", "${device.deviceNetworkId}:0${endpoint}", device.hubId,
					[completedSetup: true,
					label: "${device.displayName} ${endpoint}",
					isComponent: false
					])
			}
		} catch (Exception e) {
			log.debug "Exception creating child device: ${e}"
		}
	}
}

def configure() {
	log.debug "configure"
    
	configureHealthCheck()
	def configurationCommands = zigbee.configureReporting(BINARY_INPUT_CLUSTER, PRESENT_VALUE_ATTRIBUTE, 0x10, 10, 600, null)
	for (def endpoint: 2..4) {
		configurationCommands += zigbee.configureReporting(BINARY_INPUT_CLUSTER, PRESENT_VALUE_ATTRIBUTE, 0x10, 10, 600, null, [destEndpoint: endpoint])
	}
	
	configurationCommands += zigbee.onOffConfig(0, 120)
	for (def endpoint : 2..8) {
		configurationCommands += zigbee.configureReporting(zigbee.ONOFF_CLUSTER, ONOFF_ATTRIBUTE, 0x10, 0, 120, null, [destEndpoint: endpoint])
	}
	
	configurationCommands += zigbee.configureReporting(ANALOG_INPUT_CLUSTER, PRESENT_VALUE_ATTRIBUTE, 0x39, 10, 600, 0x3dcccccd)
	configurationCommands += zigbee.configureReporting(ANALOG_INPUT_CLUSTER, PRESENT_VALUE_ATTRIBUTE, 0x39, 10, 600, 0x3dcccccd, [destEndpoint: 2])
	
	configurationCommands << refresh()
	log.debug "configurationCommands: $configurationCommands"
	return configurationCommands
}

private getChildEndpoint(String dni) {
	dni.split(":")[-1] as Integer
}

def configureHealthCheck() {
	log.debug "configureHealthCheck"
	Integer hcIntervalMinutes = 12
	def healthEvent = [name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID]]
	sendEvent(healthEvent)
}