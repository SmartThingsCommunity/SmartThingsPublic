/**
 *  MLC30
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
 
 import groovy.transform.Field
 import physicalgraph.zigbee.zcl.DataType
 
 @Field final CurrentLevel = 0x0000
 @Field final MoveToLevelWOnOff = 0x0004
 @Field final MeteringCurrentSummation = 0x0000
 @Field final MeteringInstantDemand = 0x0400
 @Field final EnergyDivisor = 100000
 @Field final CurrentDivisor = 100
 @Field final Current = 0x00f0
 @Field final Voltage = 0x00f1
 @Field final OnOff = 0x0000
 @Field final SmartenitMfrCode = 0x1075

metadata {
	definition (name: "Smartenit Metering Dual Load Controller", namespace: "Smartenit", author: "Luis Contreras", mnmn: "SmartThingsCommunity", vid: "472dac67-bbdd-344e-944b-43abafeeb82b") {
		capability "Actuator"
		capability "Configuration"
		capability "Refresh"
		capability "Power Meter"
		capability "Energy Meter"
		capability "Health Check"
		capability "Voltage Measurement"
		capability "monthpublic25501.current"
		capability "monthpublic25501.load1"
		capability "monthpublic25501.load2"
		capability "monthpublic25501.levelControl"

		fingerprint model: "ZBMLC30NC", manufacturer: "Smartenit, Inc", deviceJoinName: "Smartenit Switch"
		fingerprint model: "ZBMLC30NO", manufacturer: "Smartenit, Inc", deviceJoinName: "Smartenit Switch"
	}
}

def getFPoint(String FPointHex){
	return (Float)Long.parseLong(FPointHex, 16)
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "Basic description: ${description}"
	def event = zigbee.getEvent(description)
	Map eventDescMap = zigbee.parseDescriptionAsMap(description)

	if (description?.startsWith("on/off")) {
		def cmds = zigbee.readAttribute(zigbee.ONOFF_CLUSTER, OnOff) + zigbee.readAttribute(zigbee.ONOFF_CLUSTER, OnOff, [destEndpoint: 2])
		return cmds.collect { new physicalgraph.device.HubAction(it) }
	}

	if (event && event.name != "switch") {
		log.debug "Collecting event: ${event}, ${event.name}, ${event.value}"
		if ((eventDescMap?.sourceEndpoint == "01") || (eventDescMap?.endpoint == "01")) {
			if (event.name == "power") {
				return createEvent(name: "power", value: (event.value/EnergyDivisor))
			} else {
				return createEvent(event)
			}
		} else if ((eventDescMap?.sourceEndpoint == "03") || (eventDescMap?.endpoint == "03")) {
			if (event.name == "level") {
				log.debug "Creating level event"
				return createEvent(name: "level", value: event.value)
			}
		}
	} else {
		def mapDescription = zigbee.parseDescriptionAsMap(description)
		log.debug "mapDescription... : ${mapDescription}"

		if (mapDescription) {
			if (mapDescription.clusterInt == zigbee.SIMPLE_METERING_CLUSTER) {
				if (mapDescription.attrInt == MeteringCurrentSummation) {
					return createEvent(name:"energy", value: getFPoint(mapDescription.value)/EnergyDivisor)
				} else if (mapDescription.attrInt == MeteringInstantDemand) {
					return createEvent(name:"power", value: getFPoint(mapDescription.value/EnergyDivisor))
				} else if (mapDescription.attrInt == Voltage) {
					return createEvent(name:"voltage", value: getFPoint(mapDescription.value) / 100)
				} else if (mapDescription.attrInt == Current) {
					return createEvent(name:"current", value: getFPoint(mapDescription.value) / 100, unit: "A")
				}
			} else if (mapDescription.clusterInt == zigbee.ONOFF_CLUSTER) {
				if (mapDescription.attrInt == OnOff) {
					def nameVal = mapDescription.sourceEndpoint == "01" ? "loadone" : "loadtwo"
					def status = mapDescription.value == "00" ? "off" : "on"
					return createEvent(name:nameVal, value: status)
				} else if (event) {
					return createEvent(event)
				}
			} else if (mapDescription.clusterInt == zigbee.LEVEL_CONTROL_CLUSTER) {
				if (mapDescription.attrInt == CurrentLevel) {
					log.debug "Received response for level control: ${eventDescMap?.value}"
					long convertedValue = Long.parseLong(eventDescMap?.value, 16)
					def ceilingVal = Math.ceil((convertedValue * 100) / 255.0 )
					return createEvent(name: "level", value: ceilingVal)
				}
			}
		}
	}
}

def setLevel(val) {
	log.debug "Setting level to ${val}"
	int newval = 0
	if (val != 0) {
		newval = (255.0 / (100.0 / val))
	}

	zigbee.command(zigbee.LEVEL_CONTROL_CLUSTER, MoveToLevelWOnOff,
		DataType.pack(newval, DataType.UINT8, 1) + DataType.pack(0xffff, DataType.UINT16, 1), [destEndpoint: 3]) 
}

def setLoadone(val) {
	log.debug "toggling load one to: ${val}"

	if (val == "on") {
		zigbee.on()
	} else if (val == "off") {
		zigbee.off()
	}
}

def setLoadtwo(val) {
	log.debug "Setting load two to: ${val}"

	if (val == "on") {
		zigbee.command(zigbee.ONOFF_CLUSTER, 0x01, "", [destEndpoint: 2])
	} else if (val == "off") {
		zigbee.command(zigbee.ONOFF_CLUSTER, 0x00, "", [destEndpoint: 2])
	}
}

def resetEnergyMeter() {
	log.debug "resetEnergyMeter: not implemented"
}

def refresh() {
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, MeteringCurrentSummation) +
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, MeteringInstantDemand) +
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, Voltage, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(zigbee.SIMPLE_METERING_CLUSTER, Current, [mfgCode: SmartenitMfrCode]) +
	zigbee.readAttribute(zigbee.ONOFF_CLUSTER, OnOff) +
	zigbee.readAttribute(zigbee.ONOFF_CLUSTER, OnOff, [destEndpoint: 2]) +
	zigbee.readAttribute(zigbee.LEVEL_CONTROL_CLUSTER, CurrentLevel, [destEndpoint: 3])
}

def configure() {
	log.debug "in configure()"
	configureHealthCheck()
	return (zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, MeteringCurrentSummation, 0x25, 0, 600, 50) +
		zigbee.configureReporting(zigbee.SIMPLE_METERING_CLUSTER, MeteringInstantDemand, 0x2a, 0, 600, 50) + 
		zigbee.configureReporting(zigbee.ONOFF_CLUSTER, OnOff, 0x10, 0, 120, null, [destEndpoint: 1]) +
		zigbee.configureReporting(zigbee.ONOFF_CLUSTER, OnOff, 0x10, 0, 120, null, [destEndpoint: 2]) +
		refresh()
	)
}

def configureHealthCheck() {
	Integer hcIntervalMinutes = 10
	sendEvent(name: "checkInterval", value: hcIntervalMinutes * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID])
}

def updated() {
	log.debug "in updated()"
	configureHealthCheck()
}

def ping() {
	return zigbee.readAttribute(zigbee.ONOFF_CLUSTER, ONOFF_ATTRIBUTE) + zigbee.readAttribute(zigbee.ONOFF_CLUSTER, OnOff, [destEndpoint: 2])
}