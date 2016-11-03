/**
 *  Copyright 2015 SmartThings
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
 *	    GE/Jasco ZigBee Dimmer
 *
 *	Author: SmartThings
 *	Date: 2015-07-01
 */

metadata {
	definition (name: "GE ZigBee Dimmer", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
		capability "Switch Level"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"

	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
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
		log.info event
		if (event.name == "power") {
			if (device.getDataValue("manufacturer") != "OSRAM") {       //OSRAM devices do not reliably update power
				event.value = (event.value as Integer) / 10             //TODO: The divisor value needs to be set as part of configuration
				sendEvent(event)
			}
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

def setLevel(value) {
	zigbee.setLevel(value)
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.simpleMeteringPowerRefresh() + zigbee.electricMeasurementPowerRefresh() + zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.simpleMeteringPowerConfig() + zigbee.electricMeasurementPowerConfig()
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	refresh()
}
