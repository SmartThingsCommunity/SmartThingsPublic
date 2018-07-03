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
 *  Orvibo Contact Sensor
 *
 *  Author: Deng Biaoyi
 *
 *  Date:2018-07-03
 */
import physicalgraph.zigbee.clusters.iaszone.ZoneStatus

metadata {
	definition (name: "Orvibo Contact Sensor", namespace: "smartthings", author: "biaoyi.deng@samsung.com", vid:"generic-contact-3", ocfDeviceType: "x.com.st.d.sensor.contact") {
		capability 	"Contact Sensor"
		capability 	"Sensor"
		capability 	"Battery"
		capability 	"Configuration"
		capability  "Health Check"
		
		fingerprint profileId: "0104",deviceId: "0402",inClusters: "0000,0003,0500,FFFF,0001",outClusters: "0000,0004,0003,0005,0001", manufacturer: "ORVIBO", model: "e70f96b3773a4c9283c6862dbafb6a99"
	}

	simulator {

	}

	tiles(scale: 2) {
		multiAttributeTile(name: "contact", type: "generic", width: 6, height: 4) {
			tileAttribute("device.contact", key: "PRIMARY_CONTROL") {
				attributeState "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
				attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
			}
		}

		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}

		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main(["contact"])
		details(["contact","battery", "refresh"])
	}
}

def parse(String description) {
	log.debug "parse description: $description"

	def resMap  
	if (description.startsWith("zone")) {
		resMap = createEvent(name: "contact", value: zigbee.parseZoneStatus(description).isAlarm1Set() ? "open" : "closed")
	} else if (description?.startsWith("read")) {
		// Map descMap = (description - "read attr - ").split(",").inject([:]) {
		// 	map, param -> def nameAndValue = param.split(":")
		// 	map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
		// }
		Map descMap = zigbee.parseDescriptionAsMap((description - "read attr - ").split(","));
		switch(descMap?.cluster) {
			case "0001":
				if(descMap.attrId == "0021") {
					resMap = createEvent(name: "battery", value: (zigbee.convertToInt(descMap.value, 16)))
					log.debug "Battery Percentage convert to ${resMap.value}%"
				}
				break
			case "0500":
				def zs = new ZoneStatus(zigbee.convertToInt(descMap.value, 16))
				map = getContactResult(zs.isAlarm1Set() ? "open" : "closed")
				break
			default:
				break
		}
	}else{
		log.debug "the command ${description} is wrong"
	}

	log.debug "Parse returned $resMap"
	return resMap
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

def configure() {
	log.debug "Configuring Reporting and Bindings."

    return zigbee.configureReporting(0x0001, 0x0021, 0x20, 0, 10, 0x01) +
    	zigbee.readAttribute(0x0001, 0x0021)
}