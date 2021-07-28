/**
 *  Copyright 2016 SmartThings
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
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition (name: "Smartenit Valve", namespace: "Smartenit", author: "SmartThings", minHubCoreVersion: '000.017.0012') {
		capability "Actuator"
		capability "Configuration"
		capability "Health Check"
		capability "Refresh"
		capability "Valve"

		fingerprint manufacturer: "Compacta", model: "ZBVC1(1023A)", deviceJoinName: "Smartenit Valve"
	}
}

private getCLUSTER_BASIC() { 0x0000 }
private getCLUSTER_POWER() { 0x0001 }

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"
	def event = zigbee.getEvent(description)
	if (event) {
		if(event.name == "switch") {
			event.name = "valve"                  //0006 cluster in valve is tied to contact
			if(event.value == "on") {
				event.value = "open"
			} else if(event.value == "off") {
				event.value = "closed"
			}
			return createEvent(event)
		}
	}
}

def open() {
	zigbee.on() + refresh()
}

def close() {
	zigbee.off() + refresh()
}

def refresh() {
	log.debug "refresh called"

	def cmds = []
	cmds += zigbee.onOffRefresh()
	return cmds
}

def configure() {
	log.debug "Configuring Reporting and Bindings."
	return zigbee.configureReporting(zigbee.ONOFF_CLUSTER, ONOFF_ATTRIBUTE, 0x10, 0, 60 * 10, null) + refresh()
}

def installed() {
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
}

def ping() {
	zigbee.onOffRefresh()
}