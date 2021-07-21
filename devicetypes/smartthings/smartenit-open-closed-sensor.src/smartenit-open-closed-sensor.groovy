/**
 *  SmartSense Open/Closed Sensor
 *
 *  Copyright 2014 SmartThings
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
	definition(name: "Smartenit Open/Closed Sensor", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, genericHandler: "Zigbee") {
		capability "Configuration"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"

		fingerprint manufacturer: "Compacta", model :"ZBWDS", deviceJoinName: "Smartenit Open/Closed Sensor"
	}
}

private getIAS_ZONE_TYPE_ATTRIBUTE() { 0x0001 }
private getIAS_ZONE_TYPE_CONTACT_SWITCH_ATTRIBUTE_VALUE() { 0x0015 }
private getPOLL_CONTROL_CLUSTER() { 0x0020 }
private getCHECK_IN_INTERVAL_ATTRIBUTE() { 0x0000 }
private getFAST_POLL_TIMEOUT_ATTRIBUTE() { 0x0003 }
private getSET_LONG_POLL_INTERVAL_CMD() { 0x02 }
private getSET_SHORT_POLL_INTERVAL_CMD() { 0x03 }

def parse(String description) {
	log.debug "description: $description"

	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status') || description?.startsWith('zone report')) {
			map = parseIasMessage(description)
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.attrInt == zigbee.ATTRIBUTE_IAS_ZONE_STATUS) {
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = getContactResult(zs.isAlarm1Set() ? "open" : "closed")
			} else if (descMap?.clusterInt == zigbee.IAS_ZONE_CLUSTER && descMap.commandInt == 0x07) {
				if (descMap.data[0] == "00") {
					log.debug "IAS ZONE REPORTING CONFIG RESPONSE: $descMap"
					sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
				} else {
					log.warn "IAS ZONE REPORTING CONFIG FAILED- error code: ${descMap.data[0]}"
				}
			} 
		}
	}

	log.debug "Parse returned $map"
	def result = map ? createEvent(map) : [:]

	if (description?.startsWith('enroll request')) {
		List cmds = zigbee.enrollResponse()
		log.debug "enroll response: ${cmds}"
		result = cmds?.collect { new physicalgraph.device.HubAction(it) }
	}
	return result
}


private Map parseIasMessage(String description) {
	ZoneStatus zs = zigbee.parseZoneStatus(description)
	return zs.isAlarm1Set() ? getContactResult('open') : getContactResult('closed')
}

private Map getContactResult(value) {
	log.debug 'Contact Status'
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	return [
		name           : 'contact',
		value          : value,
		descriptionText: descriptionText
	]
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
}

def refresh() {
	log.debug "Refreshing Temperature and state of o/c sensor"
	def refreshCmds = zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS) + zigbee.enrollResponse()

	return refreshCmds
}

def configure() {
	// Device-Watch allows 2 check-in misses from device + ping (plus 1 min lag time)
	// enrolls with default periodic reporting until newer 5 min interval is confirmed
	sendEvent(name: "checkInterval", value: 2 * 60 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])

	log.debug "Configuring Reporting, IAS CIE, and Bindings."
	def cmds = refresh() +
		zigbee.configureReporting(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS, DataType.BITMAP16, 30, 60 * 5, null) +
		zigbee.enrollResponse()
	return cmds
}