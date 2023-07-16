/**
 *  Copyright 2021 SmartThings
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
import physicalgraph.zigbee.zcl.DataType
metadata {
	definition (name: "ZigBee Metering Dimmer", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.switch",  mnmn: "SmartThings", vid:"generic-dimmer-power-energy") {

		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Switch"
		capability "Switch Level"
		capability "Health Check"

		// Enbrighten/Jasco
		fingerprint manufacturer: "Jasco Products", model: "43082", deviceJoinName: "Enbrighten Dimmer Switch" //Enbrighten, in-Wall Smart Dimmer With Energy Monitoring 43082, Raw Description: 01 0104 0101 00 08 0000 0003 0004 0005 0006 0008 0702 0B05 02 000A 0019
	}
}

def getATTRIBUTE_READING_INFO_SET() { 0x0000 }
def getATTRIBUTE_HISTORICAL_CONSUMPTION() { 0x0400 }

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	List result = []

	def event = zigbee.getEvent(description)
	if (event) {
		log.info event
		if (event.name == "power") {
			def powerDiv = device.getDataValue("divisor")
			powerDiv = powerDiv ? (powerDiv as int) : 1
			event.value = event.value/powerDiv
			event.unit = "W"
		} else if (event.name == "energy") {
			def energyDiv = device.getDataValue("energyDivisor")
			energyDiv = energyDiv ? (energyDiv as int) : 100
			event.value = event.value/energyDiv
			event.unit = "kWh"
		}
		log.info "event: $event"
		result << event
	} else {
		def descMap = zigbee.parseDescriptionAsMap(description)
		log.debug "descMap: $descMap"

		List attrData = [[clusterInt: descMap.clusterInt ,attrInt: descMap.attrInt, value: descMap.value]]
		descMap.additionalAttrs.each {
			attrData << [clusterInt: descMap.clusterInt, attrInt: it.attrInt, value: it.value]
		}

		attrData.each {
			def map = [:]
			if (it.value && it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_HISTORICAL_CONSUMPTION) {
				log.debug "power"
				map.name = "power"
				def powerDiv = device.getDataValue("divisor")
				powerDiv = powerDiv ? (powerDiv as int) : 1
				map.value = zigbee.convertHexToInt(it.value)/powerDiv
				map.unit = "W"
			}
			else if (it.value && it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_READING_INFO_SET) {
				log.debug "energy"
				map.name = "energy"
				def energyDiv = device.getDataValue("energyDivisor")
				energyDiv = energyDiv ? (energyDiv as int) : 100
				map.value = zigbee.convertHexToInt(it.value)/energyDiv
				map.unit = "kWh"
			}

			if (map) {
				result << createEvent(map)
			}
		}
	}
	log.debug "result: ${result}"
	return result
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def setLevel(value, rate = null) {
	zigbee.setLevel(value) + (value?.toInteger() > 0 ? zigbee.on() : [])
}

def resetEnergyMeter() {
	log.debug "resetEnergyMeter: not implemented"
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping"
	return refresh()
}

def refresh() {
	log.debug "refresh"
	zigbee.onOffRefresh() +
	zigbee.levelRefresh() +
	zigbee.simpleMeteringPowerRefresh() +
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET)
}

def configure() {
	log.debug "Configuring Reporting and Bindings."

	if (isJascoProducts())  {
		device.updateDataValue("divisor", "10")
		device.updateDataValue("energyDivisor", "10000")
	} else {
		device.updateDataValue("divisor", "1")
		device.updateDataValue("energyDivisor", "100")
	}

	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	return refresh() +
		zigbee.onOffConfig() +
		zigbee.levelConfig() +
		zigbee.simpleMeteringPowerConfig() +
		zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET, DataType.UINT48, 1, 600, 1)
}

private boolean isJascoProducts() {
	device.getDataValue("manufacturer") == "Jasco Products"
}