/**
 *  Copyright 2019 SmartThings
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
	definition (name: "Zigbee Metering Plug Power Consumption Report", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.smartplug", mnmn: "Dawon",  vid: "STES-1-Dawon-Zigbee_Smart_Plug") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"
		capability "Configuration"
		capability "Power Consumption Report"

		fingerprint manufacturer: "DAWON_DNS", model: "PM-B430-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug
		fingerprint manufacturer: "DAWON_DNS", model: "PM-B530-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug
		fingerprint manufacturer: "DAWON_DNS", model: "PM-C140-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS In-Wall Outlet
		fingerprint manufacturer: "DAWON_DNS", model: "PM-B540-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug
		fingerprint manufacturer: "DAWON_DNS", model: "ST-B550-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug
		fingerprint manufacturer: "DAWON_DNS", model: "PM-C150-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS In-Wall Outlet
		fingerprint manufacturer: "DAWON_DNS", model: "PM-C250-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS In-Wall Outlet
		fingerprint manufacturer: "DAWON_DNS", model: "PM-B440-ZB", deviceJoinName: "Dawon Outlet" // DAWON DNS Smart Plug
	}
}

def getATTRIBUTE_READING_INFO_SET() { 0x0000 }
def getATTRIBUTE_HISTORICAL_CONSUMPTION() { 0x0400 }

def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	def descMap = zigbee.parseDescriptionAsMap(description)

	if (event) {
		log.info "event enter:$event"
		if (event.name == "switch" && !descMap.isClusterSpecific && descMap.commandInt == 0x0B) {
			log.info "Ignoring default response with desc map: $descMap"
			return [:]
		} else if (event.name== "power") {
			event.value = event.value/getPowerDiv()
			event.unit = "W"
		} else if (event.name== "energy") {
			event.value = event.value/getEnergyDiv()
			event.unit = "kWh"
		}
		log.info "event outer:$event"
		sendEvent(event)
	} else {
		List result = []
		log.debug "Desc Map: $descMap"

		List attrData = [[clusterInt: descMap.clusterInt ,attrInt: descMap.attrInt, value: descMap.value]]
		descMap.additionalAttrs.each {
			attrData << [clusterInt: descMap.clusterInt, attrInt: it.attrInt, value: it.value]
		}

		attrData.each {
			def map = [:]
			if (it.value && it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_HISTORICAL_CONSUMPTION) {
				log.debug "power"
				map.name = "power"
				map.value = zigbee.convertHexToInt(it.value)/getPowerDiv()
				map.unit = "W"
			}
			else if (it.value && it.clusterInt == zigbee.SIMPLE_METERING_CLUSTER && it.attrInt == ATTRIBUTE_READING_INFO_SET) {
				log.debug "energy"
				map.name = "energy"
				map.value = zigbee.convertHexToInt(it.value)/getEnergyDiv()
				map.unit = "kWh"

				def currentEnergy = map.value
				def currentPowerConsumption = device.currentState("powerConsumption")?.value
				Map previousMap = currentPowerConsumption ? new groovy.json.JsonSlurper().parseText(currentPowerConsumption) : [:]
				def deltaEnergy = calculateDelta (currentEnergy, previousMap)
				Map reportMap = [:]
				reportMap["energy"] = currentEnergy
				reportMap["deltaEnergy"] = deltaEnergy 
				sendEvent("name": "powerConsumption", "value": reportMap.encodeAsJSON(), displayed: false)
			}

			if (map) {
				result << createEvent(map)
			}
			log.debug "Parse returned $map"
		}
		return result
	}
}

def off() {
	def cmds = zigbee.off()
	return cmds
}

def on() {
	def cmds = zigbee.on()
	return cmds
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return refresh()
}

def refresh() {
	log.debug "refresh"
	zigbee.onOffRefresh() +
	zigbee.electricMeasurementPowerRefresh() +
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET)
}

def configure() {
	// this device will send instantaneous demand and current summation delivered every 1 minute
	sendEvent(name: "checkInterval", value: 2 * 60 + 10 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
	log.debug "Configuring Reporting"
	return refresh() +
		zigbee.onOffConfig() +
		zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, ATTRIBUTE_READING_INFO_SET, DataType.UINT48, 1, 600, 1) +
		zigbee.electricMeasurementPowerConfig(1, 600, 1) +
		zigbee.simpleMeteringPowerConfig()
}

private int getPowerDiv() {
	1
}

private int getEnergyDiv() {
	1000
}

BigDecimal calculateDelta (BigDecimal currentEnergy, Map previousMap) {
	if (previousMap?.'energy' == null) {
		return 0;
	}
	BigDecimal lastAcumulated = BigDecimal.valueOf(previousMap ['energy']);
	return currentEnergy.subtract(lastAcumulated).max(BigDecimal.ZERO);
}
