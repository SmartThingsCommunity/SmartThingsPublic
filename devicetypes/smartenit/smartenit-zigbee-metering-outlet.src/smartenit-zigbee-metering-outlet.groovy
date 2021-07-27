/*
 *	Copyright 2016 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *	use this file except in compliance with the License. You may obtain a copy
 *	of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *	License for the specific language governing permissions and limitations
 *	under the License.
 */
 import groovy.transform.Field
 
 @Field final MeteringCurrentSummation = 0x0000
 @Field final MeteringInstantDemand = 0x0400
 @Field final Current = 0x0508
 @Field final Voltage = 0x0505
 @Field final EnergyDivisor = 100000
 @Field final SmartenitMfrCode = 0x1075
 @Field final ElectricalMeasurement = 0x0b04
 @Field final ACVoltageMultiplier = 0x0600
 @Field final ACVoltageDivisor = 0x0601
 @Field final ACCurrentMultiplier = 0x0602
 @Field final ACCurrentDivisor = 0x0603
 @Field final ActivePower = 0x050b
 @Field final PowerMultiplier = 0x0604
 @Field final PowerDivisor = 0x0605

metadata {
	// Automatically generated. Make future change here.
	definition(name: "Smartenit Zigbee Metering Outlet", namespace: "Smartenit", author: "Luis Contreras", mnmn: "SmartThingsCommunity", vid: "9f4df74b-f0d4-3515-9384-f5297ee3b11c", ocfDeviceType: "oic.d.smartplug", minHubCoreVersion: '000.017.0012') {
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Voltage Measurement"
		capability "Configuration"
		capability "monthpublic25501.current"
		capability "Refresh"
		capability "Sensor"
		capability "Health Check"
		capability "Outlet"

		fingerprint manufacturer: "Compacta", model: "ZBMSKT1 (4035A)", deviceJoinName: "Smartenit Outlet"
	}
}

def getFPoint(String FPointHex){
	log.debug "printing fpointHex ${FPointHex}"
	return (Float)Long.parseLong(FPointHex, 16)
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def event = zigbee.getEvent(description)
	log.debug "event: ${event}"

	if (event) {
		if (event.name == "power") {
			event = createEvent(name: event.name, value: (event.value as Integer), descriptionText: '{{ device.displayName }} power is {{ value }} Watts', translatable: true)
		} else if (event.name == "switch") {
			def descriptionText = event.value == "on" ? '{{ device.displayName }} is On' : '{{ device.displayName }} is Off'
			event = createEvent(name: event.name, value: event.value, descriptionText: descriptionText, translatable: true)
		}
	} else {
		def cluster = zigbee.parse(description)
		log.debug "cluster def: ${cluster}"
		def mapDescription = zigbee.parseDescriptionAsMap(description)

		if (cluster && cluster.clusterId == 0x0006 && cluster.command == 0x07) {
			if (cluster.data[0] == 0x00) {
				log.debug "ON/OFF REPORTING CONFIG RESPONSE: " + cluster
				event = createEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
			} else {
				log.warn "ON/OFF REPORTING CONFIG FAILED- error code:${cluster.data[0]}"
				event = null
			}
		} else if (mapDescription && (mapDescription.clusterInt == zigbee.SIMPLE_METERING_CLUSTER)) {
			if (mapDescription.attrInt == MeteringCurrentSummation) {
				event = createEvent(name: "energy", value: getFPoint(mapDescription.value)/EnergyDivisor)
			} else if (mapDescription.attrInt == MeteringInstantDemand) {
				event = createEvent(name: "power", value: getFPoint(mapDescription.value)/EnergyDivisor)
			} else {
				log.debug "Could not find attribute mapping for ${mapDescription.clusterInt} ${mapDescription.attrInt}"
			} 
		} else if (mapDescription && (mapDescription.clusterInt == ElectricalMeasurement)) {
			if (mapDescription.attrInt == Voltage) {
				event = createEvent(name: "voltage", value: getFPoint(mapDescription.value))
			} else if (mapDescription.attrInt == Current) {
				event = createEvent(name: "current", value: getFPoint(mapDescription.value)/1000, unit: "A")
			}
		} else {
			log.warn "DID NOT PARSE MESSAGE for description : $description"
			log.debug "${cluster}"
		}
	}
	return event ? createEvent(event) : event
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh() {
	zigbee.onOffRefresh() +
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, MeteringCurrentSummation) + 
	zigbee.readAttribute(ElectricalMeasurement, Voltage) + 
	zigbee.readAttribute(ElectricalMeasurement, Current) +
	zigbee.readAttribute(ElectricalMeasurement, PowerMultiplier) +
	zigbee.readAttribute(ElectricalMeasurement, PowerDivisor) + 
	zigbee.readAttribute(ElectricalMeasurement, ActivePower)
}

def configure() {
	// Setting proper divisor for Aurora AOne 13A Smart Plug
	def deviceModel = device.getDataValue("model")
	def divisorValue = deviceModel == "SingleSocket50AU" ? "1" : "10"
	device.updateDataValue("divisor", divisorValue)

	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 10 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])

	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	refresh() + zigbee.onOffConfig(0, 300) + zigbee.electricMeasurementPowerConfig()
}