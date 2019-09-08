/**
 *	Copyright 2015 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "ZigBee Switch Power", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch", runLocally: true, minHubCoreVersion: '000.019.00012', executeCommandsLocally: true, genericHandler: "Zigbee") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Power Meter"
		capability "Sensor"
		capability "Switch"
		capability "Health Check"
		capability "Light"

		// Generic
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B04"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702"

		// Aurora
		fingerprint profileId: "0104", inClusters: "0000, 0702, 0003, 0009, 0B04, 0006, 0004, 0005, 0002", outClusters: "0000, 0019, 000A, 0003, 0406", manufacturer: "Develco Products A/S", model: "Smart16ARelay51AU", deviceJoinName: "Aurora Smart Inline Relay"
		fingerprint profileId: "0104", inClusters: "0000, 0702, 0003, 0009, 0B04, 0006, 0004, 0005, 0002", outClusters: "0000, 0019, 000A, 0003, 0406", manufacturer: "Aurora", model: "Smart16ARelay51AU", deviceJoinName: "Aurora Smart Inline Relay"

		// GE/Jasco
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B05", outClusters: "0003, 000A, 0019", manufacturer: "Jasco Products", model: "45853", deviceJoinName: "GE ZigBee Plug-In Switch", ocfDeviceType: "oic.d.smartplug"
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702, 0B05", outClusters: "000A, 0019", manufacturer: "Jasco Products", model: "45856", deviceJoinName: "GE ZigBee In-Wall Switch"

		// INGENIUM
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0B04", outClusters: "0000, 0004", manufacturer: "MEGAMAN", model: "SH-PSUKC44B-E", deviceJoinName: "INGENIUM ZB Smart Power Adaptor", ocfDeviceType: "oic.d.smartplug"

		// Ozom
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702", outClusters: "0000", manufacturer: "ClimaxTechnology", model: "PSM_00.00.00.35TC", deviceJoinName: "Ozom Smart Plug", ocfDeviceType: "oic.d.smartplug"

		// Philio
		fingerprint manufacturer: " ", model: "PAN18-v1.0.7", deviceJoinName: "Philio Smart Plug", ocfDeviceType: "oic.d.smartplug" //profileId: "0104", inClusters: "0000, 0003, 0006, 0702", outClusters: "0003, 0019",

		// Salus
		fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006, 0702", manufacturer: "SALUS", model: "SX885ZB", deviceJoinName: "Salus miniSmartplug"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'${currentValue} W'
			}
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main "switch"
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	if (event) {
		if (event.name == "power") {
			def powerValue
			def div = device.getDataValue("divisor")
			div = div ? (div as int) : 10
			powerValue = (event.value as Integer)/div
			sendEvent(name: "power", value: powerValue)
		}
		else {
			sendEvent(event)
		}
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def refresh() {
	Integer reportIntervalMinutes = 5
	def cmds = zigbee.onOffRefresh() + zigbee.simpleMeteringPowerRefresh() + zigbee.electricMeasurementPowerRefresh()
	if (device.getDataValue("manufacturer") == "Jasco Products") {
		// Some versions of hub firmware will incorrectly remove this binding causing manual control of switch to stop working
		// This needs to be the first binding table entry because the device will automatically write this entry each time it restarts
		cmds += ["zdo bind 0x${device.deviceNetworkId} 2 1 0x0006 {${device.zigbeeId}} {${device.zigbeeId}}", "delay 2000"]
	}
	cmds + zigbee.onOffConfig(0, reportIntervalMinutes * 60) + zigbee.simpleMeteringPowerConfig() + zigbee.electricMeasurementPowerConfig()
}

def configure() {
	log.debug "in configure()"
	if ((device.getDataValue("manufacturer") == "Develco Products A/S") || (device.getDataValue("manufacturer") == "Aurora"))  {
		device.updateDataValue("divisor", "1")
	}
	if (device.getDataValue("manufacturer") == "SALUS") {
		device.updateDataValue("divisor", "1")
	}
	return configureHealthCheck()
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 12
	sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	return refresh()
}

def updated() {
	log.debug "in updated()"
	// updated() doesn't have it's return value processed as hub commands, so we have to send them explicitly
	def cmds = configureHealthCheck()
	cmds.each{ sendHubCommand(new physicalgraph.device.HubAction(it)) }
}

def ping() {
	return zigbee.onOffRefresh()
}