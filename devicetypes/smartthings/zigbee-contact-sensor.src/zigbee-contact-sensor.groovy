/**
 *  Copyright 2018 SmartThings
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
 *
 *  Contact Sensor
 *
 *  Author: Fen Mei
 *
 *  Date:2018-12-15
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus
import physicalgraph.zigbee.zcl.DataType

metadata {
	definition(name: "Zigbee Contact Sensor", namespace: "smartthings", author: "biaoyi.deng@samsung.com", runLocally: false, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, mnmn:"SmartThings", vid:"generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability "Battery"
		capability "Configuration"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Health Check"
		capability "Sensor"

		fingerprint profileId: "0104", deviceId: "0402", inClusters: "0000, 0003, 0500", manufacturer: "LUMI", model: "lumi.magnet.agl02", deviceJoinName: "LUMI Contact Sensor"
	}

	simulator {

		status "open": "zone status 0x0021 -- extended status 0x00"
		status "close": "zone status 0x0000 -- extended status 0x00"

		for (int i = 0; i <= 90; i += 10) {
			status "battery 0x${i}": "read attr - raw: 2E6D01000108210020C8, dni: 2E6D, endpoint: 01, cluster: 0001, size: 08, attrId: 0021, encoding: 20, value: ${i}"
		}
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
				attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
			}
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["contact"])
		details(["contact", "battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "description: $description"

	def result = [:]
	Map map = zigbee.getEvent(description)
	if (!map) {
		if (description?.startsWith('zone status')) {
			ZoneStatus zs = zigbee.parseZoneStatus(description)
			map = zs.isAlarm1Set() ? getContactResult('open') : getContactResult('closed')
			result = createEvent(map)
		} else if (description?.startsWith('enroll request')) {
			List cmds = zigbee.enrollResponse()
			log.debug "enroll response: ${cmds}"
			result = cmds?.collect { new physicalgraph.device.HubAction(it) }
		} else {
			Map descMap = zigbee.parseDescriptionAsMap(description)
			if (descMap?.clusterInt == 0x0500 && descMap?.attrInt == 0x0002) {
                            if (device.getDataValue("manufacturer") == "LUMI") {
                	       sendBatteryResult(description)
                            }
			    def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
			    map = getContactResult(zs.isAlarm1Set() ? "open" : "closed")
			    result = createEvent(map)
			}
		}
	} else {
            if (description?.startsWith('zone status')) {
                if (device.getDataValue("manufacturer") == "LUMI") {
                   sendBatteryResult(description)
                }
	    }
	    result = createEvent(map)
	}
	log.debug "Parse returned $result"

	result
}

def installed(){
	log.debug "call installed()"
	sendEvent(name: "checkInterval", value:30 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
	refresh()
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	log.debug "ping is called"
	refresh()
}

def refresh() {
	log.debug "Refreshing  Battery and ZONE Status"
	def refreshCmds = zigbee.readAttribute(zigbee.IAS_ZONE_CLUSTER, zigbee.ATTRIBUTE_IAS_ZONE_STATUS)
		
	refreshCmds
}

def configure() {
	sendEvent(name: "checkInterval", value:30 * 60 + 1 * 60, displayed: false, data: [protocol: "zigbee", hubHardwareId: device.hub.hardwareID, offlinePingable: "1"])
               
        return zigbee.enrollResponse() + zigbee.iasZoneConfig(30, 60 * 30)
}

def getContactResult(value) {
	log.debug 'Contact Status'
	def linkText = getLinkText(device)
	def descriptionText = "${linkText} was ${value == 'open' ? 'opened' : 'closed'}"
	[
		name           : 'contact',
		value          : value,
		descriptionText: descriptionText
	]
}

/*
 * LUMI IAS sensors use cluster 0x0500, attr 0x0002, the third bit to report battery normal or low.
 * To adjust it,dicuessed with UI metadata developer, send battery 50% to indicate battery normal, 5% to indicate battery low.
 */

def sendBatteryResult(description) {
    def result = [:]
    ZoneStatus zs = zigbee.parseZoneStatus(description)
    def value = zs.isBatterySet()?5:50
    
    result.name = 'battery'
    result.value = value
    result.descriptionText = "${device.displayName} battery value is ${value}"
    result.translatable = true
    
    sendEvent(result)
    log.debug "send battery event"
}
