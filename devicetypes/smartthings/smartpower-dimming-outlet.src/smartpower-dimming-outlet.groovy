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
 *	SmartPower Dimming Outlet (CentraLite)
 *
 *	Author: SmartThings
 *	Date: 2013-12-04
 */
metadata {
	definition (name: "SmartPower Dimming Outlet", namespace: "smartthings", author: "SmartThings", ocfDeviceType: "oic.d.smartplug") {
		capability "Switch"
		capability "Switch Level"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"
		capability "Actuator"
		capability "Sensor"
		capability "Outlet"

		fingerprint profileId: "0104", inClusters: "0000,0003,0004,0005,0006,0008,0B04,0B05", outClusters: "0019", manufacturer: "CentraLite", model: "4257050-ZHAC"

	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
			tileAttribute ("device.power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'${currentValue} W'
			}
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch", "refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def event = zigbee.getEvent(description)
	if (!event) {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	} else if (event.name == "power") {
		/*
			Dividing by 10 as the Divisor is 10000 and unit is kW for the device. Simplifying to 10 power level is an integer.
		*/
		event.value = event.value / 10
	}

	return event ? createEvent(event) : event
}

def setLevel(value) {
	zigbee.setLevel(value)
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}

def refresh() {
	zigbee.onOffRefresh() + zigbee.levelRefresh() + zigbee.electricMeasurementPowerRefresh()
}

def configure() {
	refresh() + zigbee.onOffConfig() + zigbee.levelConfig() + zigbee.electricMeasurementPowerConfig()
}
